package com.github.davidcarboni.restolino.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;

import com.github.davidcarboni.restolino.Configuration;
import com.github.davidcarboni.restolino.reload.ClassMonitor;

public class MainHandler extends AbstractHandler {

	ResourceHandler fileHandler;
	Handler apiHandler;
	Configuration configuration;

	public MainHandler(Configuration configuration) {

		this.configuration = configuration;
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
		apiHandler = new ApiHandler(configuration);
	}

	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		if (isApiRequest(target))
			apiHandler.handle(target, baseRequest, request, response);
		else if (fileHandler != null)
			fileHandler.handle(target, baseRequest, request, response);
		else
			notFound(target, response);

		baseRequest.setHandled(true);
	}

	static boolean isApiRequest(String target) {
		String extension = FilenameUtils.getExtension(target);
		return StringUtils.isBlank(extension);
	}

	static void notFound(String target, HttpServletResponse response)
			throws IOException {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		response.setContentType(ContentType.TEXT_PLAIN.getMimeType());
		response.setCharacterEncoding("UTF8");
		response.getWriter().println("Not found: " + target);
	}
}