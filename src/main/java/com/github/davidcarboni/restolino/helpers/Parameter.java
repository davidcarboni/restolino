package com.github.davidcarboni.restolino.helpers;

import org.apache.commons.lang3.StringUtils;

/**
 * Provides convenience parsing for String parameters that need to be handled as
 * numbers.
 *
 * @author david
 */
public class Parameter {

    /**
     * Convenience method for getting an ID from the last segment of a path.
     * Internally this uses {@link #toInt(String)}.
     *
     * @param path The {@link Path} to inspect.
     * @return The final segment of the path as an int. If no final segment can
     * be found, or it cannot be parsed, -1.
     */
    public static int getId(Path path) {
        return toInt(path.lastSegment());
    }

    /**
     * Parses a parameter as an Integer. This is useful for working with query
     * string parameters and path segments as numbers.
     *
     * @param parameterValue The String to parse.
     * @return If the string is made up of digits only, an Integer parsed from
     * the string. Otherwise null.
     */
    public static Integer toInteger(String parameterValue) {
        Integer result = null;
        if (isDigits(parameterValue))
            result = Integer.valueOf(parameterValue);
        return result;
    }

    /**
     * Parses a parameter as an int. This is useful for working with query
     * string parameters and path segments as numbers.
     *
     * @param parameterValue The String to parse.
     * @return If the string is made up of digits only, an int parsed from the
     * string. Otherwise -1.
     */
    public static int toInt(String parameterValue) {
        int result = -1;
        if (isDigits(parameterValue))
            result = Integer.parseInt(parameterValue);
        return result;
    }

    /**
     * Convenience method for determining whether a string contains only digits,
     * handling null.
     *
     * @param value The value to test.
     * @return If the parameter is not null and contains only 0-9, true.
     */
    public static boolean isDigits(String value) {
        return StringUtils.isNotBlank(value) && value.matches("\\d+");
    }
}
