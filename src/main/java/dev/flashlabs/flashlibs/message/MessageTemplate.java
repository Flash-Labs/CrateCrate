package dev.flashlabs.flashlibs.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.util.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Represents a reusable template for messages containing arguments. The
 * template can be applied to a series of arguments.
 *
 * <p>Arguments are defined inside of {@code ${}}, as in string interpolation,
 * which may be escaped with a leading backslash such as in {@code \${}}. The
 * brackets contain three parts: the format, a literal {@code @} symbol, and the
 * argument key. The format is optional {@code &}-style codes, and the key is
 * any kebab-case string (lowercase and hyphen only). Some examples are {@code
 * ${@player}}, {@code ${&6&l@player}}, and {@code ${&a@player-location}}.
 */
public final class MessageTemplate {

    private static final Pattern ARGUMENT = Pattern.compile("(?<!\\\\)\\$\\{(.*?)}");
    private static final Pattern PLACEHOLDER = Pattern.compile("((?:&[0-9a-fk-or])*)@([a-z-]+)");
    private static final Pattern FORMAT = Pattern.compile("&(?:([0-9a-f])|([k-o])|(r))");

    private final List<ComponentLike> components;

    private MessageTemplate(List<ComponentLike> components) {
        this.components = components;
    }

    /**
     * Creates a template from the given string following the format described
     * in the class javadoc. If an argument is defined with {@code ${}} but the
     * contents are invalid, the contents are included directly in the result.
     *
     * @see MessageTemplate
     */
    public static MessageTemplate of(String string) {
        var components = new ArrayList<ComponentLike>();
        var argument = ARGUMENT.matcher(string);
        var style = Style.empty();
        var index = 0;
        while (argument.find()) {
            if (index < argument.start()) {
                var tuple = deserialize(string.substring(index, argument.start()), style);
                components.add(tuple.first());
                style = tuple.second();
            }
            var placeholder = PLACEHOLDER.matcher(argument.group(1));
            if (placeholder.matches()) {
                var key = placeholder.group(2);
                var format = Optional.ofNullable(placeholder.group(1))
                        .map(f -> LegacyComponentSerializer.legacyAmpersand().deserialize(f + "?").style());
                components.add(new Placeholder(key, format));
            } else {
                components.add(LegacyComponentSerializer.legacyAmpersand().deserialize(argument.group(1)));
            }
            index = argument.end();
        }
        if (index < string.length()) {
            components.add(Component.text(string.substring(index), style));
        }
        return new MessageTemplate(components);
    }

    /**
     * Helper method for deserializing texts starting with the given format and
     * ensuring the returned text contains the ending format.
     */
    private static Tuple<TextComponent, Style> deserialize(String string, Style style) {
        var builder = Component.text();
        var matcher = FORMAT.matcher(string);
        var index = 0;
        while (matcher.find()) {
            if (index < matcher.start()) {
                builder.append(Component.text(string.substring(index, matcher.start()), style));
            }
            var format = LegacyComponentSerializer.legacyAmpersand().deserialize(matcher.group(0) + "?").style();
            if (matcher.group(1) != null) {
                style = format;
            } else if (matcher.group(2) != null) {
                style = style.merge(format);
            } else {
                style = Style.empty();
            }
            index = matcher.end();
        }
        if (index < string.length()) {
            builder.append(Component.text(string.substring(index), style));
        }
        return Tuple.of(builder.build(), style);
    }

    /**
     * Gets this message with the given arguments applied. Unused arguments are
     * ignored and uses of undefined arguments are replaced with {@code ${key}}.
     */
    public TextComponent get(Map<String, ComponentLike> args) {
        return Component.text().append(components.stream()
            .map(c -> c instanceof Placeholder p ? p.replace(args.getOrDefault(p.key, p)) : c.asComponent())
            .toList()).build();
    }

    /**
     * Gets this message with the given argument applied. Arguments are supplied
     * by key-value pairs, raising an exception for an incomplete pair. Unused
     * arguments are ignored and uses of undefined arguments are replaced with
     * {@code @key}.
     *
     * @throws IllegalArgumentException If the type of an argument key/value is
     *     invalid or an argument is missing a value.
     */
    public TextComponent get(Object... args) {
        var map = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            if (!(args[i] instanceof String)) {
                throw new IllegalArgumentException("Invalid argument key " + args[i].getClass().getName() + " at index " + i + ".");
            } else if (i + 1 == args.length) {
                throw new IllegalArgumentException("Argument " + args[i] + " is missing a value at index " + i + ".");
            } else if (!(args[i + 1] instanceof ComponentLike)) {
                throw new IllegalArgumentException("Invalid argument value " + args[i + 1].getClass().getName() + " for argument " + args[i] + ".");
            }
            map.put(args[i], args[i + 1]);
        }
        return get(map);
    }

    private static final class Placeholder implements ComponentLike {

        private final String key;
        private final Optional<Style> style;

        private Placeholder(String key, Optional<Style> style) {
            this.key = key;
            this.style = style;
        }

        public Component replace(ComponentLike value) {
            return value instanceof TextComponent t ? t.style(style.orElse(t.style())) : value.asComponent();
        }

        @Override
        public Component asComponent() {
            return Component.text("${@" + key + "}", style.orElse(Style.empty()));
        }

    }

}
