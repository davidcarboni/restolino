package com.github.davidcarboni.restolino;

import com.github.davidcarboni.restolino.jetty.BasicAuth;
import com.github.davidcarboni.restolino.jetty.MainHandler;
import com.github.davidcarboni.restolino.reload.ClassReloader;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;

/**
 * This class launches an embedded Jetty server. This is the entry point to your
 * application. The Java command lie you use should reference this class, or you
 * can set it as the <code>Main-Class</code> in your JAR manifest.
 */
public class Main {

    public static Configuration configuration;
    public static Server server;

    // Handlers
    public static MainHandler mainHandler;
    public static GzipHandler gzipHandler;
    public static SecurityHandler securityHandler;
    /**
     * The statistics handler provides graceful shutdown.
     */
    public static StatisticsHandler statisticsHandler;

    public static void main(String[] args) throws Exception {

        try {
            // Set up the configuration
            configuration = new Configuration();

            // Create the Jetty server
            server = new Server(configuration.port);

            // Create the handlers
            mainHandler = new MainHandler();
            gzipHandler = new GzipHandler();
            gzipHandler.setHandler(mainHandler);

            // Select the handler to be used
            Handler handler;
            if (configuration.authenticationEnabled) {
                securityHandler = new BasicAuth();
                securityHandler.setHandler(gzipHandler);
                handler = securityHandler;
            } else {
                handler = gzipHandler;
            }

            // Graceful shutdown
            statisticsHandler = new StatisticsHandler();
            statisticsHandler.setHandler(handler);
            server.setHandler(statisticsHandler);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("Initiating graceful shutdown...");
                        //server.getHandler().stop();
                        server.stop();
                        System.out.println("Shutdown completed gracefully.");
                    } catch (Exception e) {
                        System.out.println("Shutdown error:");
                        e.printStackTrace();
                    }
                }
            }));

            // And we're good to go
            server.start();
            System.out.println(configuration);
            System.out.println("\nCompleted startup process.");
            server.join();

        } finally {
            ClassReloader.shutdown();
        }
    }

}
