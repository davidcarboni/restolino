package com.github.davidcarboni.restolino;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

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

	public String firstSegment() {
		String result = null;
		if (segments.size() > 0)
			result = segments.get(0);
		return result;
	}

	public String lastSegment() {
		String result = null;
		if (segments.size() > 0)
			result = segments.get(segments.size() - 1);
		return result;
	}

}
