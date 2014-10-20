package com.github.davidcarboni.restolino.jetty;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.github.davidcarboni.restolino.Main;
import com.github.davidcarboni.restolino.api.Api;

public class ApiHandler extends AbstractHandler {

	static final String KEY_CLASSES = "restolino.classes";

	private static ClassLoader classLoader;
	public static volatile Api api;

	public ApiHandler() {
		classLoader = ApiHandler.class.getClassLoader();
		if (Main.configuration.classesInClasspath != null) {
			System.out.println("Classes are included in the classpath. No reloading will be configured (" + Main.configuration.classesInClasspath + ")");
		}
		setupApi();
	}

	public static void setupApi() {
		if (Main.configuration.classesReloadable) {
			ClassLoader reloadableClassLoader = new URLClassLoader(new URL[] { Main.configuration.classesUrl }, classLoader);
			api = new Api(reloadableClassLoader, Main.configuration.packagePrefix);
		} else {
			api = new Api(classLoader, null);
		}
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String method = request.getMethod();
		if (StringUtils.equals("GET", method)) {
			api.get(request, response);
		} else if (StringUtils.equals("PUT", method)) {
			api.put(request, response);
		} else if (StringUtils.equals("POST", method)) {
			api.post(request, response);
		} else if (StringUtils.equals("DELETE", method)) {
			api.delete(request, response);
		} else if (StringUtils.equals("OPTIONS", method)) {
			api.options(request, response);
		} else {
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		}
	}
}