package dev.flashlabs.flashlibs.translation;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Represents a {@link ResourceBundle} loaded from an {@link ConfigurationNode}.
 * All values are registered under the path to the node, consisting of all keys
 * joined with {@code '.'}.
 */
final class NodeResourceBundle extends ResourceBundle {

    static final Control CONTROL = new Control();

    private final Map<String, Object> map = new HashMap<>();

    private NodeResourceBundle(ConfigurationNode node) {
        load(node);
    }

    private void load(ConfigurationNode node) {
        if (node.isList()) {
            node.childrenList().forEach(this::load);
        } else if (node.isMap()) {
            node.childrenMap().values().forEach(this::load);
        } else {
            map.put(Arrays.stream(node.path().array())
                .map(Object::toString)
                .collect(Collectors.joining(".")), node.raw());
        }
    }

    @Override
    protected Object handleGetObject(String key) {
        return map.get(key);
    }

    @Override
    public Enumeration<String> getKeys() {
        return new Enumeration<>() {

            private final Iterator<String> keys = map.keySet().iterator();

            @Override
            public boolean hasMoreElements() {
                return keys.hasNext();
            }

            @Override
            public String nextElement() {
                return keys.next();
            }

        };
    }

    /**
     * Custom {@link ResourceBundle.Control} implementation. This supports Hocon
     * (.conf), Json (.json), and Yaml (.yaml) via Configurate, as well as UTF-8
     * encoded properties files .
     */
    private static final class Control extends ResourceBundle.Control {

        private static final List<String> FORMATS = List.of("conf", "json", "yaml", "properties");

        @Override
        public List<String> getFormats(String baseName) {
            return FORMATS;
        }

        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IOException {
            var url = loader.getResource(toResourceName(toBundleName(baseName, locale), format));
            if (url == null) {
                return null;
            }
            return switch (format) {
                case "conf" -> new NodeResourceBundle(HoconConfigurationLoader.builder().url(url).build().load());
                case "json" -> new NodeResourceBundle(GsonConfigurationLoader.builder().url(url).build().load());
                case "yaml" -> new NodeResourceBundle(YamlConfigurationLoader.builder().url(url).build().load());
                case "properties" -> {
                    var connection = url.openConnection();
                    connection.setUseCaches(!reload);
                    try (var stream = connection.getInputStream()) {
                        yield new PropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8));
                    }
                }
                default -> throw new AssertionError(format);
            };
        }

    }

}
