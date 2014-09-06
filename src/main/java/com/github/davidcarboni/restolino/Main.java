package com.github.davidcarboni.restolino;

import javax.servlet.Servlet;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.BaseHolder.Source;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import com.github.davidcarboni.restolino.handlers.MainHandler;

/**
 * 
 * This class launches the web application in an embedded Jetty container. This
 * is the entry point to your application. The Java command that is used for
 * launching should fire this main method.
 *
 */
public class Main {

	public static void main(String[] args) throws Exception {

		String port = System.getenv("PORT");
		if (StringUtils.isBlank(port))
			port = "8080";
		System.out.println("Using port " + port);
		Server server = new Server(Integer.parseInt(port));
		Handler mainHandler = new MainHandler();
		server.setHandler(mainHandler);
		server.start();
		// server.dumpStdErr();
		server.join();
	}

	// public static void main(String[] args) throws Exception {
	// Server server = new Server(8080);
	//
	// // Web app:
	// // WebAppContext webApp = new WebAppContext();
	// ServletContextHandler webApp = new ServletContextHandler();
	// // ContextHandler webApp = new ContextHandler();
	// webApp.setContextPath("/");
	// webApp.setResourceBase("src/main/webapp");
	//
	// // Filter: (Default: EnumSet.of(DispatcherType.REQUEST))
	// webApp.addFilter(Filter.class, "/*", null);
	//
	// // Servlets:
	// addServlet(webApp, ApiServlet.class, "/api");
	// addServlet(webApp, StaticServlet.class, "/static");
	//
	// // Start:
	// server.setHandler(webApp);
	// server.start();
	// server.join();
	// }

	// /**
	// * @param args
	// */
	// public static void main(String[] args) throws Exception {
	// String webappDirLocation = "src/main/webapp/";
	//
	// // The port that we should run on can be set into an environment
	// // variable
	// // Look for that variable and default to 8080 if it isn't there.
	// String webPort = System.getenv("PORT");
	// if (StringUtils.isBlank(webPort)) {
	// webPort = "8080";
	// }
	//
	// Server server = new Server(Integer.parseInt(webPort));
	// ServletHandler handler = new ServletHandler();
	//
	// // Filter:
	// handler.addFilterWithMapping(Filter.class, "/*",
	// EnumSet.of(DispatcherType.FORWARD));
	//
	// // Servlets:
	// addServlet(handler, ApiServlet.class, "/api");
	// addServlet(handler, StaticServlet.class, "/static");
	//
	// WebAppContext root = new WebAppContext();
	// root.setServletHandler(handler);
	// //
	// root.setContextPath("/");
	// // // Resource webxml =
	// // Resource.newClassPathResource("WEB-INF/web.xml");
	// // // root.setDescriptor(webxml);
	// //
	// // root.addFilter(Filter.class, "/*",
	// // EnumSet.of(DispatcherType.FORWARD, DispatcherType.values()));
	// // addServlet(root, ApiServlet.class, "/api");
	// // addServlet(root, StaticServlet.class, "/static");
	// root.setResourceBase(webappDirLocation);
	// //
	// // // Parent loader priority is a class loader setting that Jetty
	// // accepts.
	// // // By default Jetty will behave like most web containers in that it
	// // will
	// // // allow your application to replace non-server libraries that are
	// // part
	// // // of the
	// // // container. Setting parent loader priority to true changes this
	// // // behavior.
	// // // Read more here:
	// // // http://wiki.eclipse.org/Jetty/Reference/Jetty_Classloading
	// // root.setParentLoaderPriority(true);
	//
	// server.setHandler(root);
	// // server.setHandler(handler);
	//
	// server.start();
	// // root.dumpStdErr();
	// server.join();
	// }

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
