package com.github.davidcarboni.restolino.reload;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;

import com.github.davidcarboni.restolino.jetty.ApiHandler;

public class ClassMonitor {

	public static URL url;
	public static URL[] urls;
	static Path path;

	public static void start(String path, ClassLoader parent)
			throws IOException {

		if (StringUtils.isNotBlank(path)) {
			ClassMonitor.path = FileSystems.getDefault().getPath(path);
			ClassMonitor.url = ClassMonitor.path.toUri().toURL();
			ClassMonitor.urls = new URL[] { url };
			Scanner.start(ClassMonitor.path);
		}

		reload();
	}

	public static void reload() {
		ApiHandler.setupApi();
	}

}
