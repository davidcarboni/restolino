package com.github.davidcarboni.restolino;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;

import com.github.davidcarboni.restolino.jetty.ApiHandler;
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

	public static void main(String[] args) throws Exception {

		try {
			// Set up the configuration:
			Configuration configuration = new Configuration();

			// Create the Jetty server:
			server = new Server(configuration.port);
			securityHandler = new ConstraintSecurityHandler();

			// Select the handler to be used:
			mainHandler = new MainHandler(configuration);
			if (configuration.authenticationEnabled) {
				securityHandler = newBasicAuth(configuration);
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

	/**
	 * Adapted from <a href=
	 * "https://github.com/jesperfj/jetty-secured-sample/blob/master/src/main/java/HelloWorld.java"
	 * >https://github.com/jesperfj/jetty-secured-sample/blob/master/src/main/
	 * java/HelloWorld.java</a>
	 * 
	 * @param username
	 *            The username you want to set.
	 * @param password
	 *            The password you want to set.
	 * @param realm
	 *            "It's basically something you make up. It doesn't have to match anything, it should just make sense for your application. –  stevedbrown"
	 * @see <a href=
	 *      "http://stackoverflow.com/questions/10892336/realm-name-in-tomcat-web-xml"
	 *      >http://stackoverflow.com/questions/10892336/realm-name-in-

	 *      tomcat-web-xml</a>
	 * @return
	 */
	static SecurityHandler newBasicAuth(Configuration configuration) {

		HashLoginService l = new HashLoginService();
		l.putUser(configuration.username,
				Credential.getCredential(configuration.password),
				new String[] { "user" });
		l.setName(configuration.realm);

		Constraint constraint = new Constraint();
		constraint.setName(Constraint.__BASIC_AUTH);
		constraint.setRoles(new String[] { "user" });
		constraint.setAuthenticate(true);

		ConstraintMapping cm = new ConstraintMapping();
		cm.setConstraint(constraint);
		cm.setPathSpec("/*");

		ConstraintSecurityHandler basicAuthHandler = new ConstraintSecurityHandler();
		basicAuthHandler.setAuthenticator(new BasicAuthenticator());
		basicAuthHandler.setRealmName("myrealm");
		basicAuthHandler.addConstraintMapping(cm);
		basicAuthHandler.setLoginService(l);

		return basicAuthHandler;
	}

}
