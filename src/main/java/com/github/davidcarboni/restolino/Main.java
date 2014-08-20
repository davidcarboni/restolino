package com.github.davidcarboni.restolino;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Servlet;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.BaseHolder.Source;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

import com.github.davidcarboni.restolino.servlet.ApiServlet;
import com.github.davidcarboni.restolino.servlet.Filter;
import com.github.davidcarboni.restolino.servlet.StaticServlet;

/**
 * 
 * This class launches the web application in an embedded Jetty container. This
 * is the entry point to your application. The Java command that is used for
 * launching should fire this main method.
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String webappDirLocation = "src/main/webapp/";

		// The port that we should run on can be set into an environment
		// variable
		// Look for that variable and default to 8080 if it isn't there.
		String webPort = System.getenv("PORT");
		if (StringUtils.isBlank(webPort)) {
			webPort = "8080";
		}

		Server server = new Server(Integer.parseInt(webPort));
		WebAppContext root = new WebAppContext();

		root.setContextPath("/");
		Resource webxml = Resource.newClassPathResource("WEB-INF/web.xml");
		// root.setDescriptor(webxml);

		root.addFilter(Filter.class, "/*", EnumSet.of(DispatcherType.FORWARD));
		addServlet(root, ApiServlet.class, "/api");
		addServlet(root, StaticServlet.class, "/static");

		// try (InputStream webxml = Main.class.getClassLoader()
		// .getResourceAsStream("WEB-INF/web.xml")) {
		// System.out.println(webxml);
		// }
		root.setResourceBase(webappDirLocation);

		// Parent loader priority is a class loader setting that Jetty accepts.
		// By default Jetty will behave like most web containers in that it will
		// allow your application to replace non-server libraries that are part
		// of the
		// container. Setting parent loader priority to true changes this
		// behavior.
		// Read more here:
		// http://wiki.eclipse.org/Jetty/Reference/Jetty_Classloading
		root.setParentLoaderPriority(true);

		server.setHandler(root);

		server.start();
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

}
