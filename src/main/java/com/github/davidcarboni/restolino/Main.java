package com.github.davidcarboni.restolino;

import com.github.davidcarboni.restolino.jetty.BasicAuth;
import com.github.davidcarboni.restolino.jetty.MainHandler;
import com.github.davidcarboni.restolino.reload.ClassReloader;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;

/**
 * 
 * This class launches an embedded Jetty server. This is the entry point to your
 * application. The Java command lie you use should reference this class, or you
 * can set it as the <code>Main-Class</code> in your JAR manifest.
 *
 */
public class Main {

	public static Configuration configuration;
	public static Server server;

	// Handlers
	public static MainHandler mainHandler;
	public static GzipHandler gzipHandler;
	public static SecurityHandler securityHandler;

	public static void main(String[] args) throws Exception {

		try {
			// Set up the configuration:
			configuration = new Configuration();

			// Create the Jetty server:
			server = new Server(configuration.port);

			// Create the handlers
			mainHandler = new MainHandler();
			gzipHandler = new GzipHandler();
			gzipHandler.setHandler(mainHandler);

			// Select the handler to be used:
			if (configuration.authenticationEnabled) {
				securityHandler = new BasicAuth();
				securityHandler.setHandler(gzipHandler);
				server.setHandler(securityHandler);
			} else {
				server.setHandler(gzipHandler);
			}

			// And we're done.
			server.start();
			System.out.println(configuration);
			System.out.println("\nCompleted startup process.");
			server.join();

		} finally {
			ClassReloader.shutdown();
		}
	}

}
