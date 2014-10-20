package com.github.davidcarboni.restolino.jetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.github.davidcarboni.restolino.Configuration;
import com.github.davidcarboni.restolino.reload.ClassMonitor;

public class MainHandler extends AbstractHandler {

	public static FilesHandler fileHandler;
	public static ApiHandler apiHandler;
	public static Configuration configuration;

	public MainHandler(Configuration configuration) {

		MainHandler.configuration = configuration;
		setupFilesHandler();
		setupApiHandler();

		if (configuration.classesReloadable) {
			ClassMonitor.start(System.getProperty("restolino.classes"), configuration);
		}
	}

	private void setupFilesHandler() {
		fileHandler = FilesHandler.newInstance(configuration);
		if (fileHandler == null) {
			System.out.println("No file handler configured. " + "No resource found on the classpath " + "and reloading is not configured.");
		}
	}

	private void setupApiHandler() {
		apiHandler = new ApiHandler(configuration);
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		// Should we try redirecting to index.html?
		if (isRootRequest(request) && ApiHandler.api.home == null) {
			response.sendRedirect("/index.html");
		} else if (isApiRequest(target)) {
			apiHandler.handle(target, baseRequest, request, response);
		} else if (fileHandler != null) {
			fileHandler.handle(target, baseRequest, request, response);
		} else {
			notFound(target, response);
		}

		baseRequest.setHandled(true);
	}

	/**
	 * Determines if the given request is for the root resource (ie /).
	 * 
	 * @param request
	 *            The {@link HttpServletRequest}
	 * @return If {@link HttpServletRequest#getPathInfo()} is null, empty string
	 *         or "/", then true.
	 */
	boolean isRootRequest(HttpServletRequest request) {
		String path = request.getPathInfo();
		if (StringUtils.isBlank(path)) {
			return true;
		} else if (StringUtils.equals("/", path)) {
			return true;
		}
		return false;
	}

	static boolean isApiRequest(String target) {
		String extension = FilenameUtils.getExtension(target);
		return StringUtils.isBlank(extension);
	}

	static void notFound(String target, HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		response.setContentType(MimeTypes.Type.TEXT_PLAIN_UTF_8.asString());
		response.getWriter().println("Not found: " + target);
	}
}