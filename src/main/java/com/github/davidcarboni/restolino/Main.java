package com.github.davidcarboni.restolino;

import javax.servlet.Servlet;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.BaseHolder.Source;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import com.github.davidcarboni.restolino.jetty.MainHandler;

/**
 * 
 * This class launches the web application in an embedded Jetty container. This
 * is the entry point to your application. The Java command that is used for
 * launching should fire this main method.
 *
 */
public class Main {

	public static void main(String[] args) throws Exception {

		Configuration configuration = new Configuration();
		System.out.println("Using port " + configuration.port
				+ " (specify a PORT environment variable to change it)");
		Server server = new Server(configuration.port);
		Handler mainHandler = new MainHandler(configuration);
		server.setHandler(mainHandler);
		server.start();
		// server.dumpStdErr();
		server.join();
	}

	static int order = 0;

	/**
	 * Adds a servlet that will get initialised on startup.
	 * 
	 * @param root
	 * @param servletClass
	 * @param pathSpec
	 */
	static void addServlet(WebAppContext root,
			Class<? extends Servlet> servletClass, String pathSpec) {
		ServletHolder holder = new ServletHolder(Source.EMBEDDED);
		holder.setClassName(servletClass.getName());
		holder.setInitOrder(++order);
		root.addServlet(holder, pathSpec);
	}

	/**
	 * Adds a servlet that will get initialised on startup.
	 * 
	 * @param root
	 * @param servletClass
	 * @param pathSpec
	 */
	static void addServlet(ServletHandler root,
			Class<? extends Servlet> servletClass, String pathSpec) {
		ServletHolder holder = new ServletHolder(Source.EMBEDDED);
		holder.setClassName(servletClass.getName());
		holder.setInitOrder(++order);
		root.addServletWithMapping(holder, pathSpec);
	}

	/**
	 * Adds a servlet that will get initialised on startup.
	 * 
	 * @param root
	 * @param servletClass
	 * @param pathSpec
	 */
	static void addServlet(ServletContextHandler root,
			Class<? extends Servlet> servletClass, String pathSpec) {
		ServletHolder holder = new ServletHolder(Source.EMBEDDED);
		holder.setClassName(servletClass.getName());
		holder.setInitOrder(++order);
		root.addServlet(holder, pathSpec);
	}

}
