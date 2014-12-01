package com.github.davidcarboni.restolino.jetty;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.reflections.Reflections;

import com.github.davidcarboni.restolino.Configuration;
import com.github.davidcarboni.restolino.framework.Filter;
import com.github.davidcarboni.restolino.framework.Startup;
import com.github.davidcarboni.restolino.reload.ClassFinder;
import com.github.davidcarboni.restolino.reload.ClassMonitor;

public class MainHandler extends AbstractHandler {

	/** Just in case you need to change it. */
	public static String filesResourceName = "/files";

	Configuration configuration;
	public static ResourceHandler fileHandler;
	public static ApiHandler apiHandler;
	public static Collection<Filter> filters;
	public static Collection<Startup> startups;

	public MainHandler(Configuration configuration) {

		this.configuration = configuration;
		Reflections reflections = ClassFinder.newReflections();
		setupFilesHandler();
		setupApiHandler(reflections);
		setupFilters(reflections);
		runStartups(reflections);

		try {
			ClassMonitor.getInstance().start(System.getProperty("restolino.classes"), null, configuration);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void reload() {
		Reflections reflections = ClassFinder.newReflections();
		ApiHandler.setupApi(reflections);
		setupFilters(reflections);
		runStartups(reflections);
	}

	private void setupFilesHandler() {
		ResourceHandler fileHandler = FilesHandler.newInstance();
		if (fileHandler == null) {
			System.out.println("No file handler configured. " + "No resource found on the classpath " + "and reloading is not configured.");
		} else {
			MainHandler.fileHandler = fileHandler;
		}
	}

	private void setupApiHandler(Reflections reflections) {
		apiHandler = new ApiHandler(configuration, reflections);
	}

	@Override
	protected void doStart() throws Exception {
		super.doStart();
		fileHandler.start();
		apiHandler.start();
	}

	@Override
	protected void doStop() throws Exception {
		super.doStop();
		fileHandler.stop();
		apiHandler.stop();
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		boolean isRootRequest = isRootRequest(request);
		boolean isApiRequest = isApiRequest(target);
		if (filter(request, response)) {
			if (isRootRequest && ApiHandler.api.home == null) {
				response.sendRedirect("/index.html");
			} else if (isApiRequest) {
				apiHandler.handle(target, baseRequest, request, response);
			} else if (fileHandler != null) {
				fileHandler.handle(target, baseRequest, request, response);
			} else {
				notFound(target, response);
			}
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

	boolean filter(HttpServletRequest req, HttpServletResponse res) {
		boolean result = true;
		for (Filter filter : filters) {
			result &= filter.filter(req, res);
		}
		return result;
	}

	static void notFound(String target, HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		response.setContentType(MimeTypes.Type.TEXT_PLAIN_UTF_8.asString());
		response.getWriter().println("Not found: " + target);
	}

	public static void setupFilters(Reflections reflections) {

		Set<Filter> result = new HashSet<>();
		Set<Class<? extends Filter>> filterClasses = reflections.getSubTypesOf(Filter.class);
		for (Class<? extends Filter> filterClass : filterClasses) {
			try {
				result.add(filterClass.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				System.out.println("Error instantiating filter class " + filterClass.getName());
				e.printStackTrace();
			}
		}
		filters = result;
	}

	public static void runStartups(Reflections reflections) {

		Set<Startup> startups = new HashSet<>();
		Set<Class<? extends Startup>> startupClasses = reflections.getSubTypesOf(Startup.class);
		for (Class<? extends Startup> startupClass : startupClasses) {
			try {
				startups.add(startupClass.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				System.out.println("Error instantiating startup class " + startupClass.getName());
				e.printStackTrace();
			}
		}
		for (Startup startup : startups) {
			startup.init();
		}
		MainHandler.startups = startups;
	}
}