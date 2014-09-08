package com.github.davidcarboni.restolino.handlers;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;

import com.github.davidcarboni.restolino.Configuration;
import com.github.davidcarboni.restolino.reload.classes.ClassMonitor;

public class MainHandler extends AbstractHandler {

	ResourceHandler fileHandler;
	Handler apiHandler;
	Configuration configuration;

	public MainHandler() {

		configuration = new Configuration();
		setupFilesHandler();
		setupApiHandler();

		try {
			ClassMonitor.start(System.getProperty("restolino.classes"), null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void setupFilesHandler() {
		fileHandler = FilesHandler.newInstance(configuration);
		if (fileHandler == null) {
			System.out.println("No file handler configured. "
					+ "No resource found on the classpath "
					+ "and reloading is not configured.");
		}
	}

	private void setupApiHandler() {

		ClassLoader classLoader = MainHandler.class.getClassLoader();
		URL classesUrl = null;
		apiHandler = new ApiHandler(classLoader, classesUrl, configuration);

		if (URLClassLoader.class.isAssignableFrom(classLoader.getClass())) {
			@SuppressWarnings("resource")
			URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
			ArrayList<URL> urls = new ArrayList<>();

			// Separate out the "classes" folder URL:
			for (URL url : urlClassLoader.getURLs()) {
				String path = StringUtils.lowerCase(url.getPath());
				boolean isClassesUrl = StringUtils.endsWithAny(path,
						"/classes", "/classes/");
				boolean isFileProtocol = StringUtils.equalsIgnoreCase("file",
						url.getProtocol());
				if (isClassesUrl && isFileProtocol)
					classesUrl = url;
				else
					urls.add(url);

				// Construct a classloader that excludes the classes URL:
				classLoader = new URLClassLoader(urls.toArray(new URL[0]),
						classLoader.getParent());
			}
		}

		// apiHandler = new ApiHandler(classLoader, classesUrl);

		// Debug printout of classloader hierarchy:
		while (classLoader != null
				&& URLClassLoader.class
						.isAssignableFrom(classLoader.getClass())) {
			System.out.println(" > " + classLoader.getClass().getSimpleName());

			for (URL url : ((URLClassLoader) classLoader).getURLs()) {
				System.out.println("  - " + url);
			}

			classLoader = classLoader.getParent();
		}
	}

	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		// response.setContentType("text/html;charset=utf-8");
		// response.setStatus(HttpServletResponse.SC_OK);
		// baseRequest.setHandled(true);
		//
		// response.getWriter().println("<h1>Hi</h1>");

		// System.out.println("Target: " + target);
		// System.out.println("Request: " + baseRequest.getRequestURI() + " - "
		// + request.getRequestURL());

		if (!isFileRequest(target))
			apiHandler.handle(target, baseRequest, request, response);
		else if (fileHandler != null)
			fileHandler.handle(target, baseRequest, request, response);
		else
			notFound(response);

		baseRequest.setHandled(true);
	}

	static boolean isFileRequest(String target) {

		String extension = FilenameUtils.getExtension(target);
		return StringUtils.isNotBlank(extension);
	}

	static void notFound(HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_OK);
	}
}