package com.github.davidcarboni.restolino.reload;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchService;

import org.apache.commons.lang3.StringUtils;

import com.github.davidcarboni.restolino.Configuration;
import com.github.davidcarboni.restolino.jetty.ApiHandler;

public class ClassMonitor implements Closeable {

	private static ClassMonitor classMonitor = new ClassMonitor();

	public static ClassMonitor getInstance() {
		return classMonitor;
	}

	static Path path;
	static WatchService watcher;

	private ClassMonitor() {

	}

	public void start(String path, ClassLoader parent, Configuration configuration) throws IOException {

		if (StringUtils.isNotBlank(path)) {
			ClassMonitor.path = FileSystems.getDefault().getPath(path);
			watcher = FileSystems.getDefault().newWatchService();
			Scanner.start(ClassMonitor.path, configuration, watcher);
		}

		reload();
	}

	public void reload() {
		ApiHandler.setupApi();
	}

	@Override
	public void close() throws IOException {
		if (watcher != null) {
			System.out.println("Closing filesystem monitor.");
			watcher.close();
		}
	}

}
