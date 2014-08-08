package com.github.davidcarboni.restolino.reload.classes;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;

public class ReverseClassLoader extends ClassLoader {

	public static ReverseClassLoader newInstance(Path path, ClassLoader parent) {

		ReverseClassLoader result = null;

		// Check the directory is valid:
		if (!Files.exists(path)) {
			throw new IllegalArgumentException("Directory not found: " + path);
		} else if (!Files.isDirectory(path)) {
			throw new IllegalArgumentException("Not a directory: " + path);
		} else {

			// Now create the classloader:
			try {
				URL[] urls = new URL[] { path.toUri().toURL() };
				result = new ReverseClassLoader(urls, parent);
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException(
						"Error parsing URL: " + path, e);
			}
		}

		return result;
	}

	URLClassLoader childClassLoader;

	public ReverseClassLoader(URL[] urls, ClassLoader parent) {
		super(parent);
		childClassLoader = new URLClassLoader(urls, null);
	}

	@Override
	public URL getResource(String name) {
		URL result = childClassLoader.getResource(name);

		if (result != null)
			System.out.println("Loaded resource URL " + name
					+ " from child classloader.");
		else
			result = super.getResource(name);
		if (result != null)
			System.out.println("Loaded resource URL " + name
					+ " from parent classloader.");

		return result;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		InputStream result = childClassLoader.getResourceAsStream(name);

		if (result != null)
			System.out.println("Loaded resource stream " + name
					+ " from child classloader.");
		else
			result = super.getResourceAsStream(name);
		if (result != null)
			System.out.println("Loaded resource stream " + name
					+ " from parent classloader.");

		return result;
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {

		return new UnionEnumeration(childClassLoader.getResources(name),
				super.getResources(name));
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class<?> result;

		try {
			result = childClassLoader.loadClass(name);
			System.out.println("Loaded " + name + " from child classloader.");
		} catch (ClassNotFoundException e) {
			result = super.loadClass(name);
			System.out.println("Loaded " + name + " from parent classloader.");
		}

		return result;
	}

	/**
	 * An enumeration implementation that enumerates two underlying
	 * enumerations. NB this doesn't currently filter out duplicates.
	 * 
	 * @author david
	 *
	 */
	public static class UnionEnumeration implements Enumeration<URL> {

		private Enumeration<URL> e1;
		private Enumeration<URL> e2;

		public UnionEnumeration(Enumeration<URL> e1, Enumeration<URL> e2) {
			this.e1 = e1;
			this.e2 = e2;
		}

		@Override
		public boolean hasMoreElements() {
			return e1.hasMoreElements() || e2.hasMoreElements();
		}

		@Override
		public URL nextElement() {
			if (e1.hasMoreElements())
				return e1.nextElement();
			return e2.nextElement();
		}
	}

}
