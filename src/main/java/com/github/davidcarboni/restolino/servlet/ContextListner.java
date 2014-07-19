package com.github.davidcarboni.restolino.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ContextListner implements ServletContextListener {

	// Add this inside <web-app>:

	// <listener>
	// <listener-class>
	// com.github.davidcarboni.restolino.servlet.ContextListner
	// </listener-class>
	// </listener>

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		//
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		//
	}
}
