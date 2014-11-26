package com.github.davidcarboni.restolino;

import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Server;

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
	public static SecurityHandler securityHandler;
	public static Configuration configuration;

	public static void main(String[] args) throws Exception {

		try {
			// Set up the configuration:
			configuration = new Configuration();

			// Create the Jetty server:
			server = new Server(configuration.port);
			securityHandler = new ConstraintSecurityHandler();

			// Select the handler to be used:
			mainHandler = new MainHandler(configuration);
			if (configuration.authenticationEnabled) {
				securityHandler = new BasicAuth(configuration);
				securityHandler.setHandler(mainHandler);
				server.setHandler(securityHandler);
			} else {
				server.setHandler(mainHandler);
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
