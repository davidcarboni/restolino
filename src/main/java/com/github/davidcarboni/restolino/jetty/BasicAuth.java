package com.github.davidcarboni.restolino.jetty;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;

import com.github.davidcarboni.restolino.Main;

/**
 * Provides HTTP Basic authentication. This is useful if you're deploying a
 * prototype on the web and need to keep it a bit more private.
 * <p>
 * Adapted from <a href=
 * "https://github.com/jesperfj/jetty-secured-sample/blob/master/src/main/java/HelloWorld.java"
 * >https://github.com/jesperfj/jetty-secured-sample/blob/master/src/main/
 * java/HelloWorld.java</a>
 * 
 * @author david
 *
 */
public class BasicAuth extends ConstraintSecurityHandler {

	public BasicAuth() {

		HashLoginService l = new HashLoginService();
		l.putUser(Main.configuration.username, Credential.getCredential(Main.configuration.password), new String[] { "user" });
		l.setName(Main.configuration.realm);

		Constraint constraint = new Constraint();
		constraint.setName(Constraint.__BASIC_AUTH);
		constraint.setRoles(new String[] { "user" });
		constraint.setAuthenticate(true);

		ConstraintMapping cm = new ConstraintMapping();
		cm.setConstraint(constraint);
		cm.setPathSpec("/*");

		// Configure this ConstraintSecurityHandler:
		setAuthenticator(new BasicAuthenticator());
		setRealmName("myrealm");
		addConstraintMapping(cm);
		setLoginService(l);
	}
}
