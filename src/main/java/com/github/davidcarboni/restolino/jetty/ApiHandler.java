package com.github.davidcarboni.restolino.jetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.reflections.Reflections;

import com.github.davidcarboni.restolino.Configuration;
import com.github.davidcarboni.restolino.api.Api;

public class ApiHandler extends AbstractHandler {

	static final String KEY_CLASSES = "restolino.classes";

	private static Configuration configuration;
	static volatile Api api;

	public ApiHandler(Configuration configuration, Reflections reflections) {
		ApiHandler.configuration = configuration;
		if (configuration.classesInClasspath != null) {
			System.out.println("Classes are included in the classpath. No reloading will be configured (" + configuration.classesInClasspath + ")");
		}
	}

	public ApiHandler() {
		if (configuration.classesInClasspath != null) {
			System.out.println("Classes are included in the classpath. No reloading will be configured (" + configuration.classesInClasspath + ")");
		}
	}

	public static void setupApi(Reflections reflections) {
		api = new Api(reflections);
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