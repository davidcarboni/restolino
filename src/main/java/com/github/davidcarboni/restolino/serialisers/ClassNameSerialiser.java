package com.github.davidcarboni.restolino.serialisers;

import com.github.davidcarboni.restolino.Main;
import org.apache.commons.lang3.StringUtils;

/**
 * Gets a class name, .
 */
public abstract class ClassNameSerialiser {

    /** For abbreviating class names like String and Integer. */
    static final String LANG_PACKAGE = "java.lang.";

    /** For abbreviating class names like List, Map and Set. */
    static final String UTIL_PACKAGE = "java.util.";

    /** For abbreviating request and response class names. */
    static final String HTTP_PACKAGE = "javax.servlet.http.";

    protected String className(Class<?> clazz) {
        String result = clazz.getName();
        if (StringUtils.startsWith(result, ".")) {
            result = StringUtils.substring(result, 1);
        }
        return removePrefixes(result, LANG_PACKAGE, UTIL_PACKAGE, HTTP_PACKAGE, Main.configuration.packagePrefix);
    }

    private String removePrefixes(String className, String... prefixes) {
        String result = className;
        for (String prefix : prefixes) {
            if (StringUtils.isNotBlank(prefix) && StringUtils.startsWith(result, LANG_PACKAGE)) {
                result = StringUtils.replace(result, LANG_PACKAGE, "");
            }
        }
        return result;
    }
}
