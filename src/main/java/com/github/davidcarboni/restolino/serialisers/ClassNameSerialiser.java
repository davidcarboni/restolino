package com.github.davidcarboni.restolino.serialisers;

import com.github.davidcarboni.restolino.Main;
import org.apache.commons.lang3.StringUtils;

/**
 * Gets a class name, .
 */
public abstract class ClassNameSerialiser {

    static final String LANG_PACKAGE = "java.lang.";
    static final String HTTP_PACKAGE = "javax.servlet.http.";

    protected String className(Class<?> clazz) {
        String result = clazz.getName();

        // Remove "java.lang" - for things like String and Integer:
        if (StringUtils.startsWith(result, LANG_PACKAGE)) {
            result = StringUtils.replace(result, LANG_PACKAGE, "");
        }

        // Remove "javax.servlet.http" - for request and response objects:
        if (StringUtils.startsWith(result, HTTP_PACKAGE)) {
            result = StringUtils.replace(result, HTTP_PACKAGE, "");
        }

        // Remove the package prefix, if configured:
        String packagePrefix = Main.configuration.packagePrefix;
        if (StringUtils.isNotBlank(packagePrefix)) {
            if (!StringUtils.endsWith(packagePrefix, ".")) {
                packagePrefix += ".";
            }
            result = StringUtils.replace(result, packagePrefix, "");
        }

        return result;
    }
}
