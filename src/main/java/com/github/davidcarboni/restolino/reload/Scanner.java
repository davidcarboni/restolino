package com.github.davidcarboni.restolino.reload;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.davidcarboni.restolino.Configuration;

public class Scanner implements Runnable {

	static Path root;
	static Map<Path, Monitor> monitors = new ConcurrentHashMap<>();
	static Configuration configuration;
	static WatchService watcher;

	/**
	 * Starts the scanning thread.
	 * 
	 * @param root
	 *            The directory under which to scan.
	 * @param configuration
	 *            The {@link Configuration}
	 * @param watcher
	 *            The {@link WatchService}
	 */
	public static void start(Path root, Configuration configuration, WatchService watcher) {
		Scanner.watcher = watcher;
		Scanner.root = root;
		Scanner.configuration = configuration;

		System.out.println("Monitoring changes under " + root);
		Thread t = new Thread(new Scanner(), "Directory scanner");
		t.setDaemon(true);
		t.start();
	}

	/**
	 * Infinite loop that scans {@link #root} and then blocks until notified
	 * that a new scan is needed.
	 */
	@Override
	public void run() {
		do {

			// Scan for directories:
			try {
				System.out.println("Scanning for directories under " + root);
				Files.walkFileTree(root, new Visitor());
			} catch (IOException e) {
				System.out.println("Error in scanning for directories to monitor:");
				e.printStackTrace();
			}

			// Block until notified that a new scan is needed:
			synchronized (this) {
				try {
					Scanner.class.wait();
				} catch (InterruptedException e) {
					System.out.println("Scanner interrupted:");
					e.printStackTrace();
				}
			}

		} while (true);

	}

	/**
	 * A {@link FileVisitor} implementation that adds directories to
	 * {@link Scanner#monitors}.
	 * 
	 * @author david
	 */
	static class Visitor extends SimpleFileVisitor<Path> {

		@Override
		public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attributes) {
			if (!monitors.containsKey(path)) {
				// Not too worried about race conditions here,
				// so long as one ends up in the Map.
				System.out.println("Adding monitor for path " + path);
				monitors.put(path, new Monitor(path, configuration, watcher));
			}
			return CONTINUE;
		}
	}

	/**
	 * @param path
	 *            To be removed from {@link Scanner#monitors}.
	 */
	static void remove(Path path) {
		monitors.remove(path);
	}

}
