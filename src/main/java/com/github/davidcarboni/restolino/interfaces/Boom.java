package com.github.davidcarboni.restolino.interfaces;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.davidcarboni.restolino.RequestHandler;

/**
 * Implementing this interface allows you to handle exceptions.
 * 
 * @author david
 *
 */
public interface Boom {

	/**
	 * 
	 * @param req
	 *            The {@link HttpServletRequest}
	 * @param res
	 *            The {@link HttpServletResponse}
	 * @param e
	 *            The {@link Exception}
	 * @param endpoint
	 *            The class that the error occurred in.
	 * @return Something to be converted to JSON, or null.
	 */
	public Object handle(HttpServletRequest req, HttpServletResponse res,
			RequestHandler requestHandler, Throwable t);
}