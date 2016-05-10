package com.github.davidcarboni.restolino.reload;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Monitors a {@link Path} for changes, including subfolders.
 *
 * Thanks to the following resources:
 * <ul>
 * <li>
 * <a href=
 * "http://andreinc.net/2013/12/06/java-7-nio-2-tutorial-writing-a-simple-filefolder-monitor-using-the-watch-service-api/"
 * >http://andreinc.net/2013/12/06/java-7-nio-2-tutorial-writing-a-simple-
 * filefolder-monitor-using-the-watch-service-api/</a></li>
 * <li>
 * <a href=
 * "http://docs.oracle.com/javase/tutorial/essential/io/notification.html"
 * >http://docs.oracle.com/javase/tutorial/essential/io/notification.html</a></li>
 * </ul>
 *
 * @author david
 */
public class Monitor implements Runnable {

    private static final Logger log = getLogger(Monitor.class);

    private Path path;
    private WatchService watcher;

    public Monitor(Path path, WatchService watcher) {

        this.watcher = watcher;
        // Sanity check:
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException(path + " is not a directory.");
        }
        this.path = path;

        Thread t = new Thread(this, path.toString());
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void run() {

        try {

            // Watch for file changes in this directory:
            path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

            // Loop for each change detected:
            do {

                try {
                    // This blocks until an event occurs:
                    WatchKey key = watcher.take();

                    // Process the events:
                    boolean reload = false;
                    for (WatchEvent<?> event : key.pollEvents()) {

                        // What was the event?
                        WatchEvent.Kind<?> kind = event.kind();

                        // An OVERFLOW event can occur, even thought it's not
                        // explicitly registered, if events are lost or
                        // discarded:
                        if (kind == OVERFLOW) {
                            log.info("Reload triggered by " + OVERFLOW + " on " + path);
                            reload = true;
                            continue;
                        }

                        // The filename is the context of the event.
                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();

                        // Reload classes:
                        if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
                            log.info("Reload triggered by " + kind + " on " + filename);
                            log.info(kind.name());
                            log.info(kind.type().getName());
                            reload = true;
                        } else {
                            log.info("Not triggering reload for " + kind + ": " + filename);
                        }
                    }

                    // Note that on creation you get both ENTRY_CREATE and
                    // ENTRY_MODIFY so this avoids reloading twice:
                    if (reload) {
                        log.info("Reloading...");
                        ClassReloader.requestReload();
                    }

                    if (!key.reset()) {
                        log.info("No longer able to access " + path + ". Exiting monitor");
                        Scanner.remove(path);
                        break;
                    }

                } catch (InterruptedException e) {
                    log.info("Error taking a WatchKey for " + path + ". Exiting this monitor.");
                    Scanner.remove(path);
                    break;
                }

            } while (true);

        } catch (IOException e) {
            log.info("Error closing WatchService for " + path);
            e.printStackTrace();
        }

        log.info("Quit: " + path);

        // Rescan:
        // This covers errors and exiting when a folder is deleted,
        // and generally any kind of exit from the loop:
        Scanner.class.notify();
    }
}
