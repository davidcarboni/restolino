package com.github.davidcarboni.restolino.reload;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.davidcarboni.restolino.Configuration;

public class Scanner {

	static Path root;
	static Map<Path, Monitor> monitors = new ConcurrentHashMap<>();
	static Configuration configuration;
	static WatchService watcher;

	public static void start(Path root, Configuration configuration,
			WatchService watcher) throws IOException {
		Scanner.watcher = watcher;
		System.out.println("Starting to monitor from " + root);
		Scanner.root = root;
		scan(configuration, watcher);
	}

	static void scan(Configuration configuration, WatchService watcher)
			throws IOException {
		Scanner.configuration = configuration;
		System.out.println("Scanning root " + root);
		Files.walkFileTree(root, new Visitor());
	}

	static class Visitor extends SimpleFileVisitor<Path> {

		@Override
		public FileVisitResult preVisitDirectory(Path path,
				BasicFileAttributes attributes) {
			// System.out.println(path + " = " + configuration.filesUrl);
			if (!monitors.containsKey(path)) {
				System.out.println("Adding monitor for path " + path);
				monitors.put(path, new Monitor(path, configuration, watcher));
			}
			return CONTINUE;
		}
	}

	static void remove(Path path) {
		monitors.remove(path);
	}
}
