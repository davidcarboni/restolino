package com.github.davidcarboni.restolino;

import org.glassfish.jersey.server.ResourceConfig;

public class Login extends ResourceConfig {
	public Login() {
		packages("com.github.davidcarboni.restolino");
		// register(org.glassfish.jersey.media.multipart.MultiPartFeature.class);
	}
}
