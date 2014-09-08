package com.github.davidcarboni.restolino.handlers;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;

import org.apache.commons.lang3.StringUtils;
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

	static String KEY_FILES = "restolino.files";
	static String filesResourceName = "files";
	Configuration configuration;

	FilesHandler(URL url, Configuration configuration) {
		this.configuration = configuration;
		Resource base = Resource.newResource(url);
		setBaseResource(base);
	}

	public static ResourceHandler newInstance(Configuration configuration) {
		URL url = null;

		// If the property is set, reload from a local directory:
		if (StringUtils.isNotBlank(System.getProperty(KEY_FILES))) {

			String path = System.getProperty(KEY_FILES);
			if (StringUtils.isNotBlank(path)) {
				try {
					// Running with reloading:
					url = FileSystems.getDefault().getPath(path).toUri()
							.toURL();
				} catch (IOException e) {
					throw new RuntimeException("Error starting class reloader",
							e);
				}
			}

		} else {

			// Check for a resource on the classpath (production):
			url = MainHandler.class.getClassLoader().getResource(
					filesResourceName);
		}

		// Set up the result only if there's anything to be served:
		FilesHandler result = null;
		if (url != null) {
			System.out.println("Set up file handler for URL: " + url);
			result = new FilesHandler(url, configuration);
		}
		return result;
	}
}