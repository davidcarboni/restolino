package com.github.davidcarboni.restolino.reload;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchService;

import org.apache.commons.lang3.StringUtils;

import com.github.davidcarboni.restolino.Configuration;

public class ClassMonitor implements Closeable {

	private static ClassMonitor classMonitor = new ClassMonitor();

	public static ClassMonitor getInstance() {
		return classMonitor;
	}

	public static URL url;
	public static URL[] urls;
	static Path path;
	static WatchService watcher;

	private ClassMonitor() {

	}

	public void start(String path, ClassLoader parent, Configuration configuration) throws IOException {

		if (StringUtils.isNotBlank(path)) {
			ClassMonitor.path = FileSystems.getDefault().getPath(path);
			ClassMonitor.url = ClassMonitor.path.toUri().toURL();
			ClassMonitor.urls = new URL[] { url };
			watcher = FileSystems.getDefault().newWatchService();
			Scanner.start(ClassMonitor.path, watcher);
		}
	}

	@Override
	public void close() throws IOException {
		if (watcher != null) {
			System.out.println("Closing filesystem monitor.");
			watcher.close();
		}
	}

}
