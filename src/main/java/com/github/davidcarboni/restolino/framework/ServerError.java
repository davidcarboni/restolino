package com.github.davidcarboni.restolino.framework;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.davidcarboni.restolino.api.RequestHandler;

/**
 * Implementing this interface allows you to handle exceptions.
 * 
 * @author david
 *
 */
public interface ServerError {

	/**
	 * 
	 * @param req
	 *            The {@link HttpServletRequest}
	 * @param res
	 *            The {@link HttpServletResponse}
	 * @param t
	 *            The {@link Throwable}
	 * @param requestHandler
	 *            The details of the endpoint that the error occurred in. This
	 *            will be null for a root ('/') request.
	 * @return Something to be converted to JSON, or null if either you want no
	 *         response message, or have written a response directly to
	 *         <code>res</code>.
	 * @throws IOException
	 *             If an error occurs in sending the response. This will
	 *             typically be thrown by attempts to write to the response
	 *             stream.
	 */
	public Object handle(HttpServletRequest req, HttpServletResponse res,
			RequestHandler requestHandler, Throwable t) throws IOException;
}
