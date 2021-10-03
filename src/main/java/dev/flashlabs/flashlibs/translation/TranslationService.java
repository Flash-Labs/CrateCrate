package dev.flashlabs.flashlibs.translation;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Provides an interface for reading translations from {@link ResourceBundle}s
 * through the provided class loader and name. Translations may be formatted as
 * property files as well as in Hocon (.conf), Json (.json), and Yaml (.yaml).
 *
 * @see ResourceBundle
 */
public final class TranslationService {

    private final String name;
    private final ClassLoader loader;

    private TranslationService(String name, ClassLoader loader) {
        this.name = name;
        this.loader = loader;
    }

    /**
     * Creates a service loading resource bundles using the given name and class
     * loader.
     */
    public static TranslationService of(String name, ClassLoader loader) {
        return new TranslationService(name, loader);
    }

    /**
     * Creates a service loading resource bundles using the given name and path.
     *
     * @throws MalformedURLException If the path's URL could not be created.
     */
    public static TranslationService of(String name, Path path) throws MalformedURLException {
        return new TranslationService(name, new URLClassLoader(new URL[] {path.toUri().toURL()}));
    }

    /**
     * Returns a {@link ResourceBundle} for the given locale.
     *
     * @see ResourceBundle#getBundle(String, Locale)
     * @throws java.util.MissingResourceException If no resource bundle exists
     */
    public ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(name, locale, loader, NodeResourceBundle.CONTROL);
    }

    /**
     * Returns a string value retrieved through the bundle for the given locale.
     * If the given key is not present, the key itself is returned.
     */
    public String getString(String key, Locale locale) {
        ResourceBundle bundle = getBundle(locale);
        return bundle.containsKey(key) ? bundle.getObject(key).toString() : key;
    }

    /**
     * Reloads the resource bundle cache corresponding to this class loader.
     */
    public void reload() {
        ResourceBundle.clearCache(loader);
    }

}
