package com.github.davidcarboni.restolino.helpers;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

/**
 * Provides path parsing. This helps with getting parameters from the path, or
 * the name of the current endpoint.
 * 
 * @author david
 *
 */
public class Path {

	public static Path newInstance(HttpServletRequest request) {
		return new Path(request);
	}

	private List<String> segments = new ArrayList<String>();

	Path(HttpServletRequest request) {
		URIBuilder builder;
		try {
			builder = new URIBuilder(request.getPathInfo());
		} catch (URISyntaxException e) {
			throw new RuntimeException("Error parsing request URI", e);
		}
		String path = builder.getPath();
		String[] segments = StringUtils.split(path, '/');
		this.segments = Arrays.asList(segments);
	}

	/**
	 * @return The first path segment. This should be the endpoint name.
	 */
	public String firstSegment() {
		String result = null;
		if (segments.size() > 0)
			result = segments.get(0);
		return result;
	}

	/**
	 * @return The last path segment. This is useful if you're expecting a
	 *         parameter such as an ID, e.g.:
	 * 
	 *         <pre>
	 * /users/{ID}
	 * </pre>
	 */
	public String lastSegment() {
		String result = null;
		if (segments.size() > 0)
			result = segments.get(segments.size() - 1);
		return result;
	}

	public List<String> segments() {
		return segments;
	}

}
