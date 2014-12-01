package com.github.davidcarboni.restolino;

import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;

import com.github.davidcarboni.restolino.jetty.ApiHandler;
import com.github.davidcarboni.restolino.jetty.BasicAuth;
import com.github.davidcarboni.restolino.jetty.FilesHandler;
import com.github.davidcarboni.restolino.jetty.MainHandler;
import com.github.davidcarboni.restolino.reload.ClassMonitor;

/**
 * 
 * This class launches an embedded Jetty server. This is the entry point to your
 * application. The Java command lie you use should reference this class, or you
 * can set it as the <code>Main-Class</code> in your JAR manifest.
 *
 */
public class Main {

	public static Server server;
	public static MainHandler mainHandler;
	public static ApiHandler apiHandler;
	public static FilesHandler filesHandler;
	public static GzipHandler gzipHandler;
	public static SecurityHandler securityHandler;
	public static Configuration configuration;

	public static void main(String[] args) throws Exception {

		try {
			// Set up the configuration:
			configuration = new Configuration();

			// Create the Jetty server:
			server = new Server(configuration.port);
			securityHandler = new ConstraintSecurityHandler();

			// Create the handlers
			mainHandler = new MainHandler(configuration);
			gzipHandler = new GzipHandler();
			gzipHandler.setHandler(mainHandler);

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
			ClassMonitor.getInstance().close();
		}
	}

}
