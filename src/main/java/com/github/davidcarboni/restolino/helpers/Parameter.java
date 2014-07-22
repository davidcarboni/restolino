package com.github.davidcarboni.restolino.helpers;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

/**
 * Provides convenience parameter parsing.
 * 
 * @author david
 *
 */
public class Parameter {

	/**
	 * Convenience method for getting an ID from the last segment of a path.
	 * Internally this uses {@link Path} and {@link #toInt(String)}.
	 * 
	 * @param req
	 *            The request.
	 * @return The final segment of the path as an int. If no final segment can
	 *         be found or parsed, -1.
	 */
	public static int getId(HttpServletRequest req) {
		return toInt(Path.newInstance(req).lastSegment());
	}

	/**
	 * Parses a parameter as an Integer. This is useful for working with query
	 * string parameters and path segments as numbers.
	 * 
	 * @param parameterValue
	 *            The String to parse.
	 * @return If the string is made up of digits only, an Integer parsed from
	 *         the string. Otherwise null.
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
	 * @param parameterValue
	 *            The String to parse.
	 * @return If the string is made up of digits only, an int parsed from the
	 *         string. Otherwise -1.
	 */
	public static int toInt(String parameterValue) {
		int result = -1;
		if (isDigits(parameterValue))
			result = Integer.parseInt(parameterValue);
		return result;
	}

	private static boolean isDigits(String value) {
		return StringUtils.isNotBlank(value) && value.matches("\\d+");
	}
}
