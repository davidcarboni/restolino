package com.github.davidcarboni.restolino.reload;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

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
                            System.out.println("Reload triggered by " + OVERFLOW + " on " + path);
                            reload = true;
                            continue;
                        }

                        // The filename is the context of the event.
                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();

                        // Reload classes:
                        if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
                            System.out.println("Reload triggered by " + kind + " on " + filename);
                            System.out.println(kind.name());
                            System.out.println(kind.type().getName());
                            reload = true;
                        } else {
                            System.out.println("Not triggering reload for " + kind + ": " + filename);
                        }
                    }

                    // Note that on creation you get both ENTRY_CREATE and
                    // ENTRY_MODIFY so this avoids reloading twice:
                    if (reload) {
                        System.out.println("Reloading...");
                        ClassReloader.requestReload();
                    }

                    if (!key.reset()) {
                        System.out.println("No longer able to access " + path + ". Exiting monitor");
                        Scanner.remove(path);
                        break;
                    }

                } catch (InterruptedException e) {
                    System.out.println("Error taking a WatchKey for " + path + ". Exiting this monitor.");
                    Scanner.remove(path);
                    break;
                }

            } while (true);

        } catch (IOException e) {
            System.out.println("Error closing WatchService for " + path);
            e.printStackTrace();
        }

        System.out.println("Quit: " + path);

        // Rescan:
        // This covers errors and exiting when a folder is deleted,
        // and generally any kind of exit from the loop:
        Scanner.class.notify();
    }
}
