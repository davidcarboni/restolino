package com.github.davidcarboni.restolino.framework;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implement this interface if you need to get access to requests before they
 * are handled by Restolino.
 * <p>
 * This enables you to do things like add cache headers to static requests.
 * <p>
 * This interface is named {@link Filter} because it provides similar
 * functionality to a Servlet filter, however there are important differences.
 * Restolino doesn't offer a "chain" of filters. Implementations of this class
 * will be called on each request but the order is not guaranteed.
 * <p>
 * The intention is to minimise the convenience of filtering in order to
 * discourage design leakage away from {@link Api} classes (thereby removing a
 * barrier to increased complexity). The aim is to encourage having zero, or, at
 * most one implementation to do things that absolutely can't be done by an
 * {@link Api} - such as seeing non-api requests and setting headers.
 * 
 * @author david
 *
 */
public interface Filter {

	/**
	 * @param req
	 *            The request.
	 * @param res
	 *            The response.
	 * @return If restolino should continue processing this request, true.
	 */
	boolean filter(HttpServletRequest req, HttpServletResponse res);
}
