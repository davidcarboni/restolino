package com.github.davidcarboni.restolino.reload.classes;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;

import com.github.davidcarboni.restolino.Api;

public class ClassMonitor {

	public static URL url;
	static Path path;
	static ClassLoader parent;
	public static volatile ClassLoader classLoader;

	public static void start(String path, ClassLoader parent)
			throws IOException {

		if (StringUtils.isNotBlank(path)) {
			ClassMonitor.parent = parent;
			ClassMonitor.path = FileSystems.getDefault().getPath(path);
			ClassMonitor.url = ClassMonitor.path.toUri().toURL();
			Scanner.start(ClassMonitor.path);
		}
	}

	public static void reload() {
		ClassLoader classLoader;
		try {
			classLoader = ReverseClassLoader.newInstance(path, parent);
			if (classLoader != null) {
				System.out.println("New class loader created for path " + path);
				ClassMonitor.classLoader = classLoader;
				Api.setup(classLoader);
			} else
				System.out.println("Class loader not created for path " + path);
		} catch (Exception e) {
			System.out.println("Error reloading classes.");
			e.printStackTrace();
		}
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
