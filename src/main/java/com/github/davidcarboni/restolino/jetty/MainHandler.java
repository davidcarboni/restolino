package com.github.davidcarboni.restolino.jetty;

import com.github.davidcarboni.restolino.Main;
import com.github.davidcarboni.restolino.framework.PostFilter;
import com.github.davidcarboni.restolino.framework.PreFilter;
import com.github.davidcarboni.restolino.framework.PriorityComparator;
import com.github.davidcarboni.restolino.framework.Startup;
import com.github.davidcarboni.restolino.reload.ClassFinder;
import com.github.davidcarboni.restolino.reload.ClassReloader;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.reflections.Reflections;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class MainHandler extends HandlerCollection {

    private static final Logger log = getLogger(MainHandler.class);

    /**
     * Just in case you need to change it.
     */
    public static String filesResourceName = "/web";

    ResourceHandler filesHandler;
    ApiHandler apiHandler;
    Collection<PreFilter> preFilters;
    Collection<PostFilter> postFilters;
    Collection<Startup> startups;

    public MainHandler() throws IOException {

        Reflections reflections = ClassFinder.newReflections();

        // Handlers
        setupFilesHandler(reflections);
        setupApiHandler(reflections);

        // Handlers can be null, so check before adding them to the collection:
        ArrayList<Handler> handlers = new ArrayList<Handler>();
        if (filesHandler != null) {
            handlers.add(filesHandler);
        }
        if (apiHandler != null) {
            handlers.add(apiHandler);
        }
        setHandlers(handlers.toArray(new Handler[0]));

        // "meta-handling"
        setupPreFilters(reflections);
        setupPostFilters(reflections);
        runStartups(reflections);

        // Class reloading
        if (Main.configuration.classesReloadable) {
            ClassReloader.start(System.getProperty("restolino.classes"));
        }
    }

    private void setupFilesHandler(Reflections reflections) throws IOException {

        // Set up the handler if there's anything to be served:
        URL url = getFilesUrl(reflections);
        if (url != null) {

            // Set up the resource handler:
            ResourceHandler filesHandler = new ResourceHandler();
            Resource resource = Resource.newResource(url);
            filesHandler.setBaseResource(resource);

            this.filesHandler = filesHandler;

            log.info("Set up static file handler for URL: " + url);
        } else {
            log.info("No static file handler configured.");
        }
    }

    private URL getFilesUrl(Reflections reflections) {
        URL result = null;

        if (Main.configuration.filesReloadable) {
            // If the reloadable property is set, reload from a local directory
            // (in development):
            result = Main.configuration.filesUrl;
        } else {
            // Otherwise, check for a resource on the classpath (when deployed):
            for (ClassLoader classLoader : reflections.getConfiguration().getClassLoaders()) {
                URL candidate = classLoader.getResource(filesResourceName);
                if (candidate != null) {
                    result = candidate;
                }
            }
        }

        return result;
    }

    private void setupApiHandler(Reflections reflections) {
        apiHandler = new ApiHandler();
        ApiHandler.setupApi(reflections);
    }

    public void reload() throws IOException {

        Reflections reflections = ClassFinder.newReflections();

        ApiHandler.setupApi(reflections);
        setupPreFilters(reflections);
        setupPostFilters(reflections);
        runStartups(reflections);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        // Should we try redirecting to index.html?
        boolean isRootRequest = isRootRequest(request);
        boolean isApiRequest = isApiRequest(target);
        if (preFilter(request, response)) {
            if (isApiRequest) {
                try {
                    apiHandler.handle(target, baseRequest, request, response);
                } finally {
                    postFilters.stream().forEach(pf -> pf.filter(request, response));
                }
            } else if (filesHandler != null) {
                filesHandler.handle(target, baseRequest, request, response);
            } else {
                notFound(target, response);
            }
        }

        baseRequest.setHandled(true);
    }

    /**
     * Determines if the given request is for the root resource (ie /).
     *
     * @param request The {@link HttpServletRequest}
     * @return If {@link HttpServletRequest#getPathInfo()} is null, empty string
     * or "/", then true.
     */
    boolean isRootRequest(HttpServletRequest request) {
        String path = request.getPathInfo();
        if (StringUtils.isBlank(path)) {
            return true;
        } else if (StringUtils.equals("/", path)) {
            return true;
        }
        return false;
    }

    static boolean isApiRequest(String target) {
        String extension = FilenameUtils.getExtension(target);
        return StringUtils.isBlank(extension);
    }

    boolean preFilter(HttpServletRequest req, HttpServletResponse res) {
        boolean result = true;
        for (PreFilter preFilter : preFilters) {
            result &= preFilter.filter(req, res);
        }
        return result;
    }

    static void notFound(String target, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType(MimeTypes.Type.TEXT_PLAIN_UTF_8.asString());
        response.getWriter().println("Not found: " + target);
    }

    public void setupPreFilters(Reflections reflections) {
        List<PreFilter> sortedPreFilters = new ArrayList<>();
        Set<Class<? extends PreFilter>> filterClasses = reflections.getSubTypesOf(PreFilter.class);
        for (Class<? extends PreFilter> filterClass : filterClasses) {
            PreFilter preFilter = null;
            try {
                preFilter = filterClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                log.error("Error instantiating filter class {}", filterClass.getName());
                e.printStackTrace();
            }
            sortedPreFilters.add(preFilter);
        }

        Collections.sort(sortedPreFilters, new PriorityComparator(sortedPreFilters.size()));
        preFilters = sortedPreFilters;
        log.info("registered Filter classes {} ",
                preFilters.stream().map(f -> f.getClass().getSimpleName()).collect(Collectors.toList()));
    }


    public void setupPostFilters(Reflections reflections) {
        List<PostFilter> sortedPostFilters = new ArrayList<>();

        Set<Class<? extends PostFilter>> classes = reflections.getSubTypesOf(PostFilter.class);
        for (Class<? extends PostFilter> clazz : classes) {

            PostFilter postFilter = null;
            try {
                postFilter = clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                log.error("Error instantiating PostFilter {}", clazz.getName());
                e.printStackTrace();
            }
            sortedPostFilters.add(postFilter);
        }

        Collections.sort(sortedPostFilters, new PriorityComparator(sortedPostFilters.size()));
        postFilters = sortedPostFilters;
        log.info("registered  PostFilter classes {}",
                postFilters.stream().map(f -> f.getClass().getSimpleName()).collect(Collectors.toList()));
    }

    public void runStartups(Reflections reflections) {
        this.startups = getStartUpsOrdered(reflections);
        this.startups.stream().forEach(startup -> startup.init());
    }

    static List<Startup> getStartUpsOrdered(Reflections reflections) {
        Set<Class<? extends Startup>> startupClasses = reflections.getSubTypesOf(Startup.class);

        List<Startup> startUpInstances = new ArrayList<>();
        for (Class<? extends Startup> startupClass : startupClasses) {
            try {
                startUpInstances.add(startupClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                log.info("Error instantiating startup class {}", startupClass.getName());
                e.printStackTrace();
            }
        }

        Collections.sort(startUpInstances, new PriorityComparator(startupClasses.size()));
        return startUpInstances;
    }

}