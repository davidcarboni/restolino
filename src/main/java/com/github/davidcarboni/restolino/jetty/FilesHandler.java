package com.github.davidcarboni.restolino.jetty;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

import com.github.davidcarboni.restolino.Main;

/**
 * A {@link ResourceHandler} implementation that serves resources from the given
 * base {@link URL}. This is used to serve static content from the classpath,
 * whether a folder, or from within a jar file.
 * 
 * @author david
 *
 */
public class FilesHandler extends ResourceHandler {

	static String filesResourceName = "files";

	FilesHandler(URL url) {
		Resource base = Resource.newResource(url);
		setBaseResource(base);

	}

	public static FilesHandler newInstance() {
		URL url = null;

		// If the property is set, reload from a local directory (in
		// development):
		if (Main.configuration.filesReloadable) {
			url = Main.configuration.filesUrl;
		} else {
			// Check for a resource on the classpath (when deployed):
			url = MainHandler.class.getClassLoader().getResource(filesResourceName);
		}

		// Set up the result only if there's anything to be served:
		FilesHandler result = null;
		if (url != null) {
			System.out.println("Set up file handler for URL: " + url);
			result = new FilesHandler(url);
			// Use basic ETag handling from the Jetty Resource class to
			// facilitate caching
			result.setEtags(true);
		} else {
			System.out.println("No static file serving configured.");
		}
		return result;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		super.handle(target, baseRequest, request, response);
		if (!baseRequest.isHandled()) {
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF8");
			response.setStatus(HttpStatus.NOT_FOUND_404);
			response.getWriter().print("404 Not found: " + baseRequest.getRequestURI());
		}
	}
}