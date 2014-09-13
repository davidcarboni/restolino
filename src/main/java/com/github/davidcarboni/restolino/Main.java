package com.github.davidcarboni.restolino;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;

import com.github.davidcarboni.restolino.jetty.MainHandler;

/**
 * 
 * This class launches an embedded Jetty server. This is the entry point to your
 * application. The Java command lie you use should reference this class, or you
 * can set it as the <code>Main-Class</code> in your JAR manifest.
 *
 */
public class Main {

	public static void main(String[] args) throws Exception {

		// Set up the configuration:
		Configuration configuration = new Configuration();
		System.out.println("Using port " + configuration.port
				+ " (specify a PORT environment variable to change it)");

		// Create the Jetty server:
		Server server = new Server(configuration.port);
		Handler mainHandler = new MainHandler(configuration);
		server.setHandler(mainHandler);
		server.start();

		// And we're done.
		server.join();
	}

}
