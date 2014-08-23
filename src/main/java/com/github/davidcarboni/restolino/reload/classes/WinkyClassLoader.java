package com.github.davidcarboni.restolino.reload.classes;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

public class WinkyClassLoader extends URLClassLoader {

	public WinkyClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	@Override
	public URL findResource(String name) {
		print("finding resource " + name);
		return super.findResource(name);
	}

	@Override
	public Enumeration<URL> findResources(String name) throws IOException {
		print("finding resources for name " + name);
		return super.findResources(name);
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		print("getting resource stream " + name);
		return super.getResourceAsStream(name);
	}

	@Override
	public void clearAssertionStatus() {
		print("clearing assertion status");
		super.clearAssertionStatus();
	}

	@Override
	public URL getResource(String name) {
		print("getting resource URL " + name);
		return super.getResource(name);
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		print("getting resource URLs for name " + name);
		return super.getResources(name);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		print("loading class " + name);
		return super.loadClass(name);
	}

	@Override
	public void setClassAssertionStatus(String className, boolean enabled) {
		print("setting class assertion status for " + className + " to "
				+ enabled);
		super.setClassAssertionStatus(className, enabled);
	}

	@Override
	public void setDefaultAssertionStatus(boolean enabled) {
		print("setting default assertion status to " + enabled);
		super.setDefaultAssertionStatus(enabled);
	}

	@Override
	public void setPackageAssertionStatus(String packageName, boolean enabled) {
		print("setting package assertion status for " + packageName + " to "
				+ enabled);
		super.setPackageAssertionStatus(packageName, enabled);
	}

	private void print(String message) {
		System.out.println(this.getClass().getSimpleName() + ": " + message);
	}

}
