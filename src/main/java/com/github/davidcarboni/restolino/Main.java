package com.github.davidcarboni.restolino;

import com.github.davidcarboni.restolino.jetty.BasicAuth;
import com.github.davidcarboni.restolino.jetty.MainHandler;
import com.github.davidcarboni.restolino.reload.ClassReloader;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * This class launches an embedded Jetty server. This is the entry point to your
 * application. The Java command lie you use should reference this class, or you
 * can set it as the <code>Main-Class</code> in your JAR manifest.
 */
public class Main {

    private static final Logger log = getLogger(Main.class);

    public static Configuration configuration;
    public static Server server;

    // Handlers
    public static MainHandler mainHandler;
    public static GzipHandler gzipHandler;
    public static SecurityHandler securityHandler;
    /**
     * The statistics handler provides graceful shutdown.
     *
     * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=420142"
     * >https://bugs.eclipse.org/bugs/show_bug.cgi?id=420142</a>
     */
    public static StatisticsHandler statisticsHandler;

    public static void main(String[] args) throws Exception {

        try {
            // Set up the configuration
            configuration = new Configuration();

            // Create the Jetty server
            QueuedThreadPool qtp = new QueuedThreadPool(configuration.maxThreads);
            org.eclipse.jetty.server.Server server = new Server(qtp);
            ServerConnector http = new ServerConnector(server, new HttpConnectionFactory());
            http.setPort(configuration.port);
            server.addConnector(http);

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
                        log.info("Initiating graceful shutdown...");
                        //server.getHandler().stop();
                        server.stop();
                        log.info("Shutdown completed gracefully.");
                    } catch (Exception e) {
                        log.info("Shutdown error:");
                        e.printStackTrace();
                    }
                }
            }));

            // And we're good to go
            server.start();
            log.info("configuration {}", configuration);
            log.info("Completed startup process.");
            server.join();

        } finally {
            ClassReloader.shutdown();
        }
    }

}
