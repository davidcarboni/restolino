package com.github.davidcarboni.restolino.jetty;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.reflections.Reflections;

import com.github.davidcarboni.restolino.Main;
import com.github.davidcarboni.restolino.framework.Filter;
import com.github.davidcarboni.restolino.framework.Startup;
import com.github.davidcarboni.restolino.reload.ClassFinder;
import com.github.davidcarboni.restolino.reload.ClassReloader;

public class MainHandler extends HandlerCollection {

	/** Just in case you need to change it. */
	public static String filesResourceName = "/web";

	ResourceHandler filesHandler;
	ApiHandler apiHandler;
	Collection<Filter> filters;
	Collection<Startup> startups;

	public MainHandler() throws IOException {

		Reflections reflections = ClassFinder.newReflections();

		// Handlers
		setupFilesHandler(reflections);
		setupApiHandler(reflections);
		setHandlers(new Handler[] { filesHandler, apiHandler });

		// "meta-handling"
		setupFilters(reflections);
		runStartups(reflections);

		// Class reloading
		if (Main.configuration.classesReloadable) {
			ClassReloader.start(System.getProperty("restolino.classes"));
		}
	}

	private void setupFilesHandler(Reflections reflections) throws IOException {

		// Set up the handler if there's anything to be served:
		URL url = getFilesUrl(reflections);
		if (url != null) {

			// Set up the resource handler:
			ResourceHandler filesHandler = new ResourceHandler();
			Resource resource = Resource.newResource(url);
			filesHandler.setBaseResource(resource);

			this.filesHandler = filesHandler;

			System.out.println("Set up static file handler for URL: " + url);
		} else {
			System.out.println("No static file handler configured.");
		}
	}

	private URL getFilesUrl(Reflections reflections) {
		URL result = null;

		if (Main.configuration.filesReloadable) {
			// If the reloadable property is set, reload from a local directory
			// (in development):
			result = Main.configuration.filesUrl;
		} else {
			// Otherwise, check for a resource on the classpath (when deployed):
			for (ClassLoader classLoader : reflections.getConfiguration().getClassLoaders()) {
				URL candidate = classLoader.getResource(filesResourceName);
				if (candidate != null) {
					result = candidate;
				}
			}
		}

		return result;
	}

	private void setupApiHandler(Reflections reflections) {
		apiHandler = new ApiHandler();
		ApiHandler.setupApi(reflections);
	}

	public void reload() throws IOException {

		Reflections reflections = ClassFinder.newReflections();

		ApiHandler.setupApi(reflections);
		setupFilters(reflections);
		runStartups(reflections);
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		// Should we try redirecting to index.html?
		boolean isRootRequest = isRootRequest(request);
		boolean isApiRequest = isApiRequest(target);
		if (filter(request, response)) {
			if (isRootRequest && ApiHandler.api.home == null) {
				response.sendRedirect("/index.html");
			} else if (isApiRequest) {
				apiHandler.handle(target, baseRequest, request, response);
			} else if (filesHandler != null) {
				filesHandler.handle(target, baseRequest, request, response);
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

	public void setupFilters(Reflections reflections) {

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

	public void runStartups(Reflections reflections) {

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
		this.startups = startups;
	}
}