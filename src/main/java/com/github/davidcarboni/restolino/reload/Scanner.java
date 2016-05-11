package com.github.davidcarboni.restolino.reload;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.file.FileVisitResult.CONTINUE;
import static org.slf4j.LoggerFactory.getLogger;

public class Scanner implements Runnable {

    private static final Logger log = getLogger(Scanner.class);

    static Path root;
    static Map<Path, Monitor> monitors = new ConcurrentHashMap<>();
    static WatchService watcher;

    /**
     * Starts the scanning thread.
     *
     * @param root    The directory under which to scan.
     * @param watcher The {@link WatchService}
     */
    public static void start(Path root, WatchService watcher) {
        Scanner.watcher = watcher;
        Scanner.root = root;

        log.info("Monitoring changes under " + root);
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
                log.info("Scanning for directories under " + root);
                Files.walkFileTree(root, new Visitor());
            } catch (IOException e) {
                log.info("Error in scanning for directories to monitor:");
                e.printStackTrace();
            }

            // Block until notified that a new scan is needed:
            synchronized (Scanner.class) {
                try {
                    Scanner.class.wait();
                } catch (InterruptedException e) {
                    log.info("Scanner interrupted:");
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
                log.info("Adding monitor for path " + path);
                monitors.put(path, new Monitor(path, watcher));
            }
            return CONTINUE;
        }
    }

    /**
     * @param path To be removed from {@link Scanner#monitors}.
     */
    static void remove(Path path) {
        monitors.remove(path);
    }

}
