package com.github.davidcarboni.restolino;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implementing this interface allows you to respond to GET requests to '/'.
 * about your API.
 * 
 * @author david
 *
 */
public interface Home {

	/**
	 * 
	 * You'll typically want to return some basic information and usage
	 * instructions
	 * 
	 * @param req
	 *            The {@link HttpServletRequest}
	 * @param res
	 *            The {@link HttpServletResponse}
	 * @return Something to be converted to JSON.
	 */
	public Object get(HttpServletRequest req, HttpServletResponse res);
}
