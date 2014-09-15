package com.github.davidcarboni.restolino;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

/**
 * Determines the correct configuration, based on environment variables, system
 * properties and checking the classloader hierarchy for a classes URL.
 * 
 * @author david
 *
 */
public class Configuration {
	public static final String PORT = "PORT";
	public static final String CLASSES = "restolino.classes";
	public static final String PACKAGE_PREFIX = "restolino.packageprefix";
	public static final String FILES = "restolino.files";
	public static final String FILES_RESOURCE = "files";

	/** The Jetty server port. */
	public int port = 8080;

	/** If files will be dynamically reloaded, true. */
	public boolean filesReloadable;

	/**
	 * If files will be dynamically reloaded, the URL from which they will be
	 * loaded. If not reloading, this will typically be URL that points to a
	 * <code>files/...</code> resource directory in your JAR.
	 */
	public URL filesUrl;

	/** If classes will be dynamically reloaded, true. */
	public boolean classesReloadable;

	/**
	 * If a <code>.../classes</code> entry is present on the classpath, that URL
	 * from the classloader hierarchy. This is designed to prevent uncertainty
	 * and frustration if you have correctly configured class reloading, but
	 * have also accidentally includedd your classes on the classpath. This
	 * would leand to code not being reloaded and possibly even confusing error
	 * messages because the classes on the classpath will take precedence
	 * (because class loaders delegate upwards).
	 */
	public URL classesInClasspath;

	/**
	 * If classes will be dynamically reloaded, a file URL for the path which
	 * will be monitored for changes in order to trigger reloading.
	 */
	public URL classesUrl;

	/**
	 * If classes will be dynamically reloaded, the package prefix to scan. This
	 * is optional but, if set, it avoids scanning all classes in all
	 * dependencies, making reloads faster. This is passed directly to
	 * {@link Reflections}.
	 */
	public String packagePrefix;

	public Configuration() {

		// The server port:
		String port = getValue("PORT");

		// The reloadable parameters:
		String files = getValue(FILES);
		String classes = getValue(CLASSES);

		// Set up the configuration:
		configurePort(port);
		configureFiles(files);
		configureClasses(classes);
	}

	/**
	 * Configures the server port by attempting to parse the given parameter,
	 * but failing gracefully if that doesn't work out.
	 * 
	 * @param port
	 *            The value of the {@value Port} parameter.
	 */
	void configurePort(String port) {

		if (StringUtils.isNotBlank(port)) {
			try {
				this.port = Integer.parseInt(port);
			} catch (NumberFormatException e) {
				System.out.println("Unable to parse server PORT variable ("
						+ port + ") using port " + port);
			}
		}
	}

	/**
	 * Sets up configuration for serving static files (if any).
	 * 
	 * @param path
	 *            The directory that contains static files.
	 */
	void configureFiles(String path) {

		// If the property is set, reload from a local directory:
		if (StringUtils.isNotBlank(path)) {
			configureFilesReloadable(path);
		} else {
			configureFilesResource();
		}
		filesReloadable = filesUrl != null;

		// Communicate:
		showFilesConfiguration();
	}

	/**
	 * Sets up configuration for reloading classes.
	 * 
	 * @param path
	 *            The directory that contains compiled classes. This will be
	 *            monitored for changes.
	 */
	void configureClasses(String path) {

		findClassesInClasspath();

		if (StringUtils.isNotBlank(path)) {
			// If the path is set, set up class reloading:
			configureClassesReloadable(path);
		}
		packagePrefix = getValue(PACKAGE_PREFIX);
		classesReloadable = classesUrl != null && classesInClasspath == null;

		// Communicate:
		showClassesConfiguration();
	}

	/**
	 * Configures static file serving from a directory. This will be reloadable,
	 * so is most useful for development (rather than deployment). This
	 * typically serves files from the <code>src/main/resources/files/...</code>
	 * directory of your development project.
	 * <p>
	 * NB This provides an efficient development workflow, allowing you to see
	 * static file changes without having to rebuild.
	 */
	void configureFilesReloadable(String path) {

		try {
			// Running with reloading:
			Path filesPath = FileSystems.getDefault().getPath(path);
			filesUrl = filesPath.toUri().toURL();
		} catch (IOException e) {
			throw new RuntimeException("Error determining files path/url for: "
					+ path, e);
		}
	}

	/**
	 * Configures static file serving from the classpath. This will not be
	 * reloadable, so is most useful for deployment (rather than development).
	 * This typically serves files from the <code>files/...</code> directory at
	 * the root of a <code>*-jar-with-dependencies.jar</code> artifact.
	 * <p>
	 * NB Part of the intent here is to support a compact and simple deployment
	 * model (single JAR) that discourages changes in the target environment
	 * (because the JAR is not unpacked) and favours automated deployment of a
	 * new version (or rollback to a previous version) as the way to make
	 * changes.
	 */
	void configureFilesResource() {

		// Check for a resource on the classpath (production):
		ClassLoader classLoader = Configuration.class.getClassLoader();
		filesUrl = classLoader.getResource(FILES_RESOURCE);
	}

	/**
	 * Scans the {@link ClassLoader} hierarchy to check if there is a
	 * <code>.../classes</code> entry present. This is designed to prevent
	 * uncertainty and frustration if you have correctly configured class
	 * reloading, but have also accidentally included your classes on the
	 * classpath. This would lead to code not being reloaded and possibly even
	 * confusing error messages because the classes on the classpath will take
	 * precedence over reloaded classes (because class loaders normally delegate
	 * upwards).
	 */
	void findClassesInClasspath() {

		ClassLoader classLoader = Configuration.class.getClassLoader();
		do {
			if (URLClassLoader.class.isAssignableFrom(classLoader.getClass())) {
				URLClassLoader urlClassLoader = (URLClassLoader) classLoader;

				findClassesInClassloader(urlClassLoader);
				if (classesInClasspath != null)
					break;
			}

			// Check the parent:
			classLoader = classLoader.getParent();

		} while (classLoader != null);
	}

	/**
	 * Scans the {@link ClassLoader} hierarchy to check if there is a
	 * <code>.../classes</code> entry present. This is designed to prevent
	 * uncertainty and frustration if you have correctly configured class
	 * reloading, but have also accidentally included your classes on the
	 * classpath. This would lead to code not being reloaded and possibly even
	 * confusing error messages because the classes on the classpath will take
	 * precedence over reloaded classes (because class loaders normally delegate
	 * upwards).
	 */
	void findClassesInClassloader(URLClassLoader urlClassLoader) {

		// Check for a "classes" URL:
		for (URL url : urlClassLoader.getURLs()) {
			String urlPath = StringUtils.lowerCase(url.getPath());
			if (StringUtils.endsWithAny(urlPath, "/classes", "/classes/"))
				classesInClasspath = url;
		}
	}

	/**
	 * Configures dynamic class reloading. This is most useful for development
	 * (rather than deployment). This typically reloads classes from the
	 * <code>target/classes/...</code> directory of your development project.
	 * <p>
	 * NB This provides an efficient development workflow, allowing you to see
	 * code changes without having to redeploy. It also supports stateless
	 * webapp design because the entire classes classloader is replaced every
	 * time there is a change (so you'll lose stuff like static variable
	 * values).
	 */
	void configureClassesReloadable(String path) {

		try {
			// Set up reloading:
			Path classesPath = FileSystems.getDefault().getPath(path);
			classesUrl = classesPath.toUri().toURL();
		} catch (IOException e) {
			throw new RuntimeException("Error starting class reloader", e);
		}
	}

	/**
	 * Prints out a message confirming the static file serving configuration.
	 */
	void showFilesConfiguration() {

		// Message to communicate the resolved configuration:
		String message;
		if (filesUrl != null) {
			String reload = filesReloadable ? "reloadable" : "non-reloadable";
			message = "Files will be served from: " + filesUrl + " (" + reload
					+ ")";
		} else {
			message = "No static files will be served.";
		}
		System.out.println("Files reloading: " + message);
	}

	/**
	 * Prints out a message confirming the class reloading configuration.
	 */
	void showClassesConfiguration() {

		// Warning about a classes folder present in the classpath:
		if (classesInClasspath != null) {
			System.out
					.println("WARNING: Dynamic class reloading is disabled because a classes URL is present in the classpath. P"
							+ "lease launch without including your classes directory: "
							+ classesInClasspath);
		}

		// Message to communicate the resolved configuration:
		String message;
		if (classesReloadable) {
			if (StringUtils.isNotBlank(packagePrefix))
				message = "Classes will be reloaded from: " + classesUrl;
			else
				message = "Classes will be reloaded from package "
						+ packagePrefix + " at: " + classesUrl;
		} else {
			message = "Classes will not be dynamically reloaded.";
		}
		System.out.println("Files reloading: " + message);
	}

	/**
	 * Gets a configured value for the given key from either the system
	 * properties or an environment variable.
	 * 
	 * @param key
	 *            The name of the configuration value.
	 * @return The system property corresponding to the given key (e.g.
	 *         -Dkey=value). If that is blank, the environment variable
	 *         corresponding to the given key (e.g. EXPORT key=value). If that
	 *         is blank, {@link StringUtils#EMPTY}.
	 */
	static String getValue(String key) {
		String result = StringUtils.defaultIfBlank(System.getProperty(key),
				StringUtils.EMPTY);
		result = StringUtils.defaultIfBlank(result, System.getenv(key));
		return result;
	}

}
