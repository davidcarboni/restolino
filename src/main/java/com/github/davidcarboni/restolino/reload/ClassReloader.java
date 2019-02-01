package com.github.davidcarboni.restolino.reload;

import com.github.davidcarboni.restolino.Main;
import com.github.davidcarboni.restolino.jetty.ApiHandler;
import org.reflections.Reflections;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchService;

import static org.slf4j.LoggerFactory.getLogger;

public class ClassReloader implements Runnable {

    private static final Logger log = getLogger(ClassReloader.class);
    static ClassReloader classMonitor;
    static WatchService watcher;
    String path;
    volatile boolean reloadRequested;

    /**
     * Sets up and starts a monitor for the given path.
     *
     * @param path The path to be monitored (including subfolders).
     */
    public static void start(String path) {
        classMonitor = new ClassReloader(path);
        Thread thread = new Thread(classMonitor, ClassReloader.class.getSimpleName());
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * @param path The path to be monitored (including subfolders).
     */
    ClassReloader(String path) {
        this.path = path;
    }

    /**
     * Infinite loop that sets up the {@link WatchService} and triggers reloads
     * as necessary whenever the instance is notified.
     */
    @Override
    public void run() {

        try {

            // Set up
            Path path = FileSystems.getDefault().getPath(this.path);
            watcher = FileSystems.getDefault().newWatchService();
            Scanner.start(path, watcher);

            while (true) {

                // Reload when notified
                synchronized (this) {
                    this.wait();
                }

                // Keep reloading until all changes have notified:
                while (reloadRequested) {
                    reloadRequested = false;
                    Reflections reflections = ClassFinder.newReflections();
                    ApiHandler.setupApi(reflections);
                    Main.mainHandler.setupPreFilters(reflections);
                    Main.mainHandler.setupPostFilters(reflections);
                    Main.mainHandler.runStartups(reflections);
                }

            }

        } catch (IOException | InterruptedException e) {
            try {
                shutdown();
                throw new RuntimeException("Error in " + ClassReloader.class.getSimpleName() + ", exiting.", e);
            } catch (IOException e1) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Requests a reload. This method can be called multiple times by multiple
     * threads and reloads will be triggered asynchronously until
     * {@link #reloadRequested} is no longer set to true.
     */
    public static void requestReload() {
        synchronized (classMonitor) {
            // Set the reload flag and notify:
            classMonitor.reloadRequested = true;
            classMonitor.notify();
        }
    }

    /**
     * Closes the {@link WatchService}.
     *
     * @throws IOException If an error occurs on {@link WatchService#close()}.
     */
    public static void shutdown() throws IOException {
        if (watcher != null) {
            synchronized (watcher) {
                log.info("Closing filesystem monitor.");
                watcher.close();
                watcher = null;
            }
        }
    }

}
