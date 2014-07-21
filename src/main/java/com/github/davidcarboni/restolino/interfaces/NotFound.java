package com.github.davidcarboni.restolino.interfaces;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implementing this class enables you to handle requests that don't map to an
 * endpoint.
 * 
 * @author david
 *
 */
public interface NotFound {

	/**
	 * 
	 * You'll typically want to set either a 404 message, or a redirect. Maybe
	 * log it to help you debug.
	 * 
	 * @param req
	 *            The {@link HttpServletRequest}
	 * @param res
	 *            The {@link HttpServletResponse}
	 * @return Something to be converted to JSON, or null.
	 */
	public Object handle(HttpServletRequest req, HttpServletResponse res);
}
