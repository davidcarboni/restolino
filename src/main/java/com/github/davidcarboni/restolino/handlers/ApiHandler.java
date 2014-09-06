package com.github.davidcarboni.restolino.handlers;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.github.davidcarboni.restolino.Api;

public class ApiHandler extends AbstractHandler {

	static final String KEY_CLASSES = "restolino.classes";

	static ClassLoader classLoader;
	static ClassLoader reloadableClassLoader;
	static URL reloadableClassesUrl;
	static volatile Api api;

	public ApiHandler(ClassLoader classLoader, URL classpathClassesUrll) {
		ApiHandler.classLoader = classLoader;
		if (classpathClassesUrll != null)
			System.out
					.println("Classes are included in the classpath. No reloading will be configured ("
							+ classpathClassesUrll + ")");
		else {
			reloadableClassesUrl = getClassesUrl();
		}

		setupApi();
	}

	public static void setupApi() {
		// try {
		if (reloadableClassesUrl != null) {
			reloadableClassLoader = new URLClassLoader(
					new URL[] { reloadableClassesUrl }, classLoader);
			System.out.println("Passing reloadable ClassLoader to API");
			// api = (Api) Class.forName(Api.class.getName(), false,
			// reloadableClassLoader).newInstance();
			api = new Api(reloadableClassLoader);
		} else {
			System.out.println("Passing non-reloadable ClassLoader to API");
			// api = (Api) Class.forName(Api.class.getName(), false,
			// classLoader).newInstance();
			api = new Api(classLoader);
		}
		// } catch (InstantiationException | IllegalAccessException
		// | ClassNotFoundException e) {
		// throw new RuntimeException(e);
		// }
	}

	private static URL getClassesUrl() {
		URL result = null;

		String path = System.getProperty(KEY_CLASSES);
		if (StringUtils.isNotBlank(path)) {
			try {
				// Running with reloading:
				result = FileSystems.getDefault().getPath(path).toUri().toURL();
			} catch (IOException e) {
				throw new RuntimeException("Error starting class reloader", e);
			}
		}

		if (result == null) {
			System.out.println("No URL set up for reloading classes.");
		}

		return result;
	}

	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String method = request.getMethod();
		if (StringUtils.equals("GET", method))
			api.get(request, response);
		else if (StringUtils.equals("PUT", method))
			api.put(request, response);
		else if (StringUtils.equals("POST", method))
			api.post(request, response);
		else if (StringUtils.equals("DELETE", method))
			api.delete(request, response);
		else if (StringUtils.equals("OPTIONS", method))
			api.options(request, response);
		else
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}
}