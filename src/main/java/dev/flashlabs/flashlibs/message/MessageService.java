package dev.flashlabs.flashlibs.message;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Maps;
import dev.flashlabs.flashlibs.translation.TranslationService;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.TextComponent;
import org.spongepowered.api.util.locale.LocaleSource;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/**
 * Provides an interface for retrieving {@link MessageTemplate}s with support
 * for translations. Messages are provided through a {@link TranslationService}
 * and cached to maximize reuse of the template.
 */
public final class MessageService {

    private final TranslationService translations;
    private final LoadingCache<Map.Entry<String, Locale>, MessageTemplate> cache;

    private MessageService(TranslationService translations) {
        this.translations = translations;
        cache = Caffeine.newBuilder().build(k -> MessageTemplate.of(translations.getString(k.getKey(), k.getValue())));
    }

    /**
     * Creates a service delegating to the given {@link TranslationService}.
     */
    public static MessageService of(TranslationService translations) {
        return new MessageService(translations);
    }

    /**
     * Creates a service delegating to a {@link TranslationService} using the
     * given name and path.
     *
     * @see TranslationService#of(String, Path)
     */
    public static MessageService of(String name, Path path) throws MalformedURLException {
        return new MessageService(TranslationService.of(name, path));
    }

    /**
     * Returns the {@link MessageTemplate} corresponding to the given key and
     * {@link Locale}. If no such message exists, the key is used.
     *
     * @see TranslationService#getString(String, Locale)
     */
    public MessageTemplate get(String key, Locale locale) {
        return cache.get(Maps.immutableEntry(key, locale));
    }

    /**
     * Returns a {@link TextComponent} consisting of the template for the given
     * key and {@link Locale} applied to the given arguments.
     *
     * @see MessageService#get(String, Locale)
     * @see MessageTemplate#get(Object...)
     */
    public TextComponent get(String key, Locale locale, Object... args) {
        return get(key, locale).get(args);
    }

    /**
     * Sends a message to the given source from the given identity retrieved
     * using the given key and source's {@link Locale} applied to the given
     * arguments.
     *
     * @see MessageService#get(String, Locale, Object...)
     */
    public <T extends Audience & LocaleSource> void send(T source, Identity identity, String key, Object... args) {
        source.sendMessage(identity, get(key, source.locale(), args));
    }

    /**
     * Reloads this service, including the backing {@link TranslationService}
     * and message cache.
     */
    public void reload() {
        translations.reload();
        cache.invalidateAll();
    }

}
