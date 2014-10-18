package com.github.davidcarboni.restolino.reload;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchService;

import com.github.davidcarboni.restolino.Configuration;
import com.github.davidcarboni.restolino.jetty.ApiHandler;

public class ClassMonitor {

	static Path path;
	static WatchService watcher;

	public static void start(String path, Configuration configuration) throws IOException {
		if (watcher == null) {
			ClassMonitor.path = FileSystems.getDefault().getPath(path);
			watcher = FileSystems.getDefault().newWatchService();
			Scanner.start(ClassMonitor.path, configuration, watcher);
		}
	}

	public static void reload() {
		ApiHandler.setupApi();
	}

	public static void shutdown() throws IOException {
		if (watcher != null) {
			System.out.println("Closing filesystem monitor.");
			watcher.close();
			watcher = null;
		}
	}

}
