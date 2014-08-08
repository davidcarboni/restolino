package com.github.davidcarboni.restolino.reload.classes;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import com.github.davidcarboni.restolino.Api;

public class ClassMonitor {

	static Path path;
	static ClassLoader parent;
	public static volatile ClassLoader classLoader;

	public static void start(String path, ClassLoader parent)
			throws IOException {

		// Check the path has a value
		// (could use StringUtils, but this is the only call,
		// so it avoids a dependency just for this line):
		if (path != null && !path.trim().equals("")) {
			ClassMonitor.parent = parent;
			ClassMonitor.path = FileSystems.getDefault().getPath(path);
			Scanner.start(ClassMonitor.path);
		}
	}

	public static void reload() {
		classLoader = ReverseClassLoader.newInstance(path, parent);
		if (classLoader != null) {
			System.out.println("New class loader created for path " + path);
			Api.init();
			// setup();
		} else
			System.out.println("Class loader not created for path " + path);
	}

	// public static void main(String[] args) throws IOException,
	// InterruptedException, ClassNotFoundException,
	// IllegalAccessException, IllegalArgumentException,
	// InvocationTargetException, NoSuchMethodException,
	// SecurityException, InstantiationException {
	//
	// // The path:
	// Path folder = Paths.get("./target/classes");
	// System.out.println(folder.toUri().normalize().toURL());
	//
	// start(folder.toString(), ClassMonitor.class.getClassLoader());
	//
	// ClassLoader previous = null;
	// while (true) {
	// Thread.sleep(1000);
	// if (classLoader != previous) {
	// previous = classLoader;
	// System.out
	// .println(" --- Attempting to access reloaded classes...");
	// Class<?> type = Class.forName(
	// "com.github.davidcarboni.jload.ClassMonitor", true,
	// classLoader);
	// System.out.println(" --- ");
	// type.getMethod("thing").invoke(type.newInstance());
	// }
	// }
	// }

}
