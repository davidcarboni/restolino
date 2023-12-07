package com.github.davidcarboni.restolino.reload;

import com.github.davidcarboni.restolino.Main;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;

import java.net.URL;
import java.net.URLClassLoader;

import static org.slf4j.LoggerFactory.getLogger;

public class ClassFinder {

    private static final Logger log = getLogger(ClassFinder.class);
    private static ClassLoader classLoader;

    public static Reflections newReflections() {
        if (classLoader == null) {
            classLoader = ClassFinder.class.getClassLoader();
        }
        if (Main.configuration.classesReloadable) {
            ClassLoader reloadableClassLoader = new URLClassLoader(new URL[]{Main.configuration.classesUrl}, classLoader);
            return createReflections(reloadableClassLoader, Main.configuration.packagePrefix);
        } else {
            return createReflections(classLoader, null);
        }
    }

    /**
     * Builds a {@link Reflections} instance that will scan for classes in, and
     * load them from, the given class loader.
     *
     * @param classLoader The class loader to scan and load from.
     * @return A new {@link Reflections} instance.
     */
    static Reflections createReflections(ClassLoader classLoader, String packagePrefix) {

        // We set up reflections to use the classLoader for loading classes
        // and also to use the classLoader to determine the list of URLs:
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder().addClassLoaders(classLoader);
        if (StringUtils.isNotBlank(packagePrefix)) {
            configurationBuilder.addUrls(ClasspathHelper.forPackage(packagePrefix, classLoader));
        } else {
            configurationBuilder.addUrls(ClasspathHelper.forClassLoader(classLoader));
        }
        Reflections reflections = new Reflections(configurationBuilder);

        log.info("Reflections URLs: {}", reflections.getConfiguration().getUrls());
        if (Main.configuration.classesReloadable && reflections.getConfiguration().getUrls().size() == 0 && StringUtils.isNotEmpty(Main.configuration.packagePrefix)) {
            log.info("It looks like no reloadable classes were found. Is '{}' the correct package prefix for your app?", Main.configuration.packagePrefix);
        }
        return reflections;
    }
}
