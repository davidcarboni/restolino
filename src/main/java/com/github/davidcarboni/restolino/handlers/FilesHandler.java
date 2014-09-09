package com.github.davidcarboni.restolino.handlers;

import java.net.URL;

import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

import com.github.davidcarboni.restolino.Configuration;

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
	static Configuration configuration;

	FilesHandler(URL url) {
		Resource base = Resource.newResource(url);
		setBaseResource(base);
	}

	public static ResourceHandler newInstance(Configuration configuration) {
		FilesHandler.configuration = configuration;
		URL url = null;

		// If the property is set, reload from a local directory:
		if (configuration.filesReloadable) {
			url = configuration.filesUrl;
		} else {
			// Check for a resource on the classpath (production):
			url = MainHandler.class.getClassLoader().getResource(
					filesResourceName);
		}

		// Set up the result only if there's anything to be served:
		FilesHandler result = null;
		if (url != null) {
			System.out.println("Set up file handler for URL: " + url);
			result = new FilesHandler(url);
		}
		return result;
	}
}