package com.github.davidcarboni.restolino.reload;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Scanner {

	static Path root;
	static Map<Path, Monitor> monitors = new ConcurrentHashMap<>();

	public static void start(Path root) throws IOException {
		System.out.println("Starting to monitor from " + root);
		Scanner.root = root;
		scan();
	}

	static void scan() throws IOException {
		System.out.println("Scanning root " + root);
		Files.walkFileTree(root, new Visitor());
	}

	static class Visitor extends SimpleFileVisitor<Path> {

		@Override
		public FileVisitResult preVisitDirectory(Path path,
				BasicFileAttributes attributes) {
			if (!monitors.containsKey(path)) {
				System.out.println("Adding monitor for path " + path);
				monitors.put(path, new Monitor(path));
			}
			return CONTINUE;
		}
	}

	static void remove(Path path) {
		monitors.remove(path);
	}
}
