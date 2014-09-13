package com.github.davidcarboni.restolino.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

/**
 * This request handler gets set as the default for each HTTP method on each
 * endpoint.
 * 
 * @author david
 *
 */
public class DefaultRequestHandler {

	static String notImplemented = " is not available for ";

	/**
	 * Sets a status of {@link HttpStatus#SC_METHOD_NOT_ALLOWED}.
	 * 
	 * @param req
	 *            {@link HttpServletRequest}
	 * @param res
	 *            {@link HttpServletResponse}
	 */
	public String notImplemented(HttpServletRequest req, HttpServletResponse res) {
		res.setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
		return req.getMethod() + notImplemented + req.getPathInfo();
	}
}
