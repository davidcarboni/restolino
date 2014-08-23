package com.github.davidcarboni.restolino.interfaces;

import java.io.IOException;

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
	 * @return A response object to be converted to JSON, or null if no message
	 *         needs to be returned.
	 * @throws IOException
	 *             If an error occurs in sending the response. This will
	 *             typically be thrown by attempts to write to the response
	 *             stream.
	 */
	public Object handle(HttpServletRequest req, HttpServletResponse res)
			throws IOException;
}
