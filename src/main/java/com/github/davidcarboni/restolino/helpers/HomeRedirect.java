package com.github.davidcarboni.restolino.helpers;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.davidcarboni.restolino.interfaces.Home;

public class HomeRedirect implements Home {

	private String path;

	public HomeRedirect(URL url) {
		this.path = url.toString();
	}

	public HomeRedirect(String path) {
		this.path = path;
	}

	public HomeRedirect(URI uri) {
		this.path = uri.toString();
	}

	@Override
	public Object get(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		res.sendRedirect(path);
		return null;
	}

}
