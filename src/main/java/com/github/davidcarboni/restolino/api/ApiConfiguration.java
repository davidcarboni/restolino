package com.github.davidcarboni.restolino.api;

import com.github.davidcarboni.restolino.framework.*;
import com.github.davidcarboni.restolino.handlers.DefaultApiDocumentation;
import com.github.davidcarboni.restolino.handlers.DefaultNotFoundHandler;
import com.github.davidcarboni.restolino.handlers.DefaultServerErrorHandler;
import com.github.davidcarboni.restolino.helpers.Path;
import com.github.davidcarboni.restolino.json.Serialiser;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * This is the framework controller.
 *
 * @author David Carboni
 */
public class ApiConfiguration {

    private static final Logger log = getLogger(ApiConfiguration.class);

    public Home home;
    public ServerError serverError;
    public NotFound notFound;

    public Map<String, Map<HttpMethod, RequestHandler>> api = new HashMap<>();

    // The default request handler:
    public RequestHandler defaultRequestHandler = new RequestHandler() {
        {
            endpointClass = DefaultRequestHandler.class;
            try {
                method = DefaultRequestHandler.class.getMethod("notImplemented", HttpServletRequest.class, HttpServletResponse.class);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RuntimeException("Code issue - default request handler method not found", e);
            }
        }
    };

    public Map<HttpMethod, RequestHandler> getHandlerMap(String endpointName) {
        if (!api.containsKey(endpointName)) {
            api.put(endpointName, new HashMap<HttpMethod, RequestHandler>());
        }
        return api.get(endpointName);
    }

    public ApiConfiguration(Reflections reflections) {

        // Set up the API endpoints:
        configureEndpoints(reflections);

        // Configure standard handlers:
        configureHome(reflections);
        configureNotFound(reflections);
        configureServerError(reflections);
    }

    /**
     * Searches for and configures all your lovely endpoints.
     *
     * @param reflections The instance to use to find classes.
     */
    void configureEndpoints(Reflections reflections) {

        // [Re]initialise the api:
        api = new HashMap<>();

        log.info("Scanning for endpoints..");
        Set<Class<?>> endpoints = reflections.getTypesAnnotatedWith(Api.class);
        // log.info(reflections.getConfiguration().getUrls());

        log.info("Found " + endpoints.size() + " endpoints.");
        log.info("Examining endpoint methods..");

        // Configure the classes:
        for (Class<?> endpointClass : endpoints) {
            String endpointName = StringUtils.lowerCase(endpointClass.getSimpleName());
            Map<HttpMethod, RequestHandler> endpointMap = getHandlerMap(endpointName);
            log.info(" - /" + endpointName + " (" + endpointClass.getName() + ")");

            for (Method method : endpointClass.getMethods()) {

                // Skip Object methods
                if (method.getDeclaringClass() == Object.class) {
                    continue;
                }

                // We're looking for public methods that take reqest, response
                // and optionally a message type:
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (Modifier.isPublic(method.getModifiers()) && parameterTypes.length >= 2 && HttpServletRequest.class.isAssignableFrom(parameterTypes[0])
                        && HttpServletResponse.class.isAssignableFrom(parameterTypes[1])) {

                    RequestHandler requestHandler = new RequestHandler();
                    requestHandler.endpointClass = endpointClass;
                    requestHandler.method = method;
                    log.info(" " + method.getName());
                    if (parameterTypes.length > 2) {
                        requestHandler.requestMessageType = parameterTypes[2];
                        log.info(" request:" + requestHandler.requestMessageType.getSimpleName());
                    }
                    if (method.getReturnType() != void.class) {
                        requestHandler.responseMessageType = method.getReturnType();
                        log.info(" response:" + requestHandler.responseMessageType.getSimpleName());
                    }

                    // Which HTTP method(s) will this method respond to?
                    List<Annotation> annotations = Arrays.asList(method.getAnnotations());
                    for (Annotation annotation : annotations) {
                        setHandler(annotation, endpointMap, requestHandler);
                    }
                }
            }
        }

    }

    private void setHandler(Annotation annotation, Map<HttpMethod, RequestHandler> endpointMap, RequestHandler requestHandler) {
        Class<? extends Annotation> type = annotation.getClass();
        if (isOneOf(type, GET.class, PUT.class, POST.class, DELETE.class)) {
            endpointMap.put(HttpMethod.method(annotation), requestHandler);
            log.info("   - " + annotation.getClass().getInterfaces()[0].getSimpleName());
        }
    }

    private boolean isOneOf(Class<? extends Annotation> annotationType, Class<?>... types) {
        for (Class<?> type : types) {
            if (type.isAssignableFrom(annotationType)) return true;
        }
        return false;
    }

    /**
     * Searches for and configures the / endpoint.
     *
     * @param reflections The instance to use to find classes.
     * @return {@link Home}
     */
    void configureHome(Reflections reflections) {

        log.info("Checking for a / endpoint..");
        Home home = getEndpoint(Home.class, "/", reflections);
        if (home == null) home = new DefaultApiDocumentation();
        printEndpoint(home, "/");
        this.home = home;
    }

    /**
     * Searches for and configures the not found endpoint.
     *
     * @param reflections The instance to use to find classes.
     */
    void configureNotFound(Reflections reflections) {

        log.info("Checking for a not-found endpoint..");
        NotFound notFound = getEndpoint(NotFound.class, "not-found", reflections);
        if (notFound == null) notFound = new DefaultNotFoundHandler();
        printEndpoint(notFound, "not-found");
        this.notFound = notFound;
    }

    /**
     * Searches for and configures the not error endpoint.
     *
     * @param reflections The instance to use to find classes.
     */
    void configureServerError(Reflections reflections) {

        log.info("Checking for an error endpoint..");
        ServerError serverError = getEndpoint(ServerError.class, "error", reflections);
        if (serverError == null) serverError = new DefaultServerErrorHandler();
        printEndpoint(serverError, "error");
        this.serverError = serverError;
    }

    private void printEndpoint(Object endpoint, String name) {
        if (endpoint != null) {
            log.info("Class " + endpoint.getClass().getSimpleName() + " configured as " + name + " endpoint");
        } else {
            log.info("No " + name + " enpoint configured.");
        }
    }

    public void get(HttpServletRequest request, HttpServletResponse response) {

        if (isRootRequest(request)) {
            doRootRequest(request, response);
        } else {
            doMethod(request, response, GET.class);
        }
    }

    public void put(HttpServletRequest request, HttpServletResponse response) {
        doMethod(request, response, PUT.class);
    }

    public void post(HttpServletRequest request, HttpServletResponse response) {
        doMethod(request, response, POST.class);
    }

    public void delete(HttpServletRequest request, HttpServletResponse response) {
        doMethod(request, response, DELETE.class);
    }

    public void options(HttpServletRequest request, HttpServletResponse response) {

        List<String> result = new ArrayList<>();

        if (isRootRequest(request)) {

            // We only allow GET to the root resource:
            result.add(HttpMethod.GET.name());

        } else {

            // Determine which methods are configured:
            String requestPath = mapRequestPath(request);
            if (api.containsKey(requestPath)) {
                for (HttpMethod httpMethod : api.get(requestPath).keySet()) {
                    result.add(httpMethod.name());
                }
            }
        }

        response.setHeader("Allow", StringUtils.join(result, ','));
    }

    /**
     * Determines if the given request is for the root resource (ie /).
     *
     * @param request The {@link HttpServletRequest}
     * @return If {@link HttpServletRequest#getPathInfo()} is null, empty string
     * or "/" ten true.
     */
    static boolean isRootRequest(HttpServletRequest request) {
        String path = request.getPathInfo();
        if (StringUtils.isBlank(path)) {
            return true;
        } else if (StringUtils.equals("/", path)) {
            return true;
        }
        return false;
    }

    /**
     * Locates a single endpoint class.
     *
     * @param type        The type of the endpoint class.
     * @param name        The name of the endpoint.
     * @param reflections The {@link Reflections} instance to use to locate the
     *                    endpoint.
     * @return The endpoint.
     */
    private static <E> E getEndpoint(Class<E> type, String name, Reflections reflections) {
        E result = null;

        // Get concrete subclasses:
        Set<Class<? extends E>> foundClasses = reflections.getSubTypesOf(type);
        Set<Class<? extends E>> endpointClasses = new HashSet<>();
        for (Class<? extends E> clazz : foundClasses) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                endpointClasses.add(clazz);
            }
        }

        // Filter out any default handlers:
        //log.info("Filtering " + type.getName());
        Iterator<Class<? extends E>> iterator = endpointClasses.iterator();
        while (iterator.hasNext()) {
            Class<? extends E> next = iterator.next();
            if (StringUtils.startsWithIgnoreCase(next.getName(), "com.github.davidcarboni.restolino.handlers.")) {
                //log.info("Filtered out " + next.getName());
                iterator.remove();
            } //else {
                //log.info("Filtered in " + next.getName());
            //}
        }
        //log.info("Filtered.");

        if (endpointClasses.size() != 0) {

            // Dump multiple endpoints:
            if (endpointClasses.size() > 1) {
                log.info("Warning: found multiple candidates for " + name + " endpoint: " + endpointClasses);
            }

            // Instantiate the endpoint if possible:
            try {
                result = endpointClasses.iterator().next().newInstance();
            } catch (Exception e) {
                log.info("Error: cannot instantiate " + name + " endpoint class " + endpointClasses.iterator().next());
                e.printStackTrace();
            }
        }

        return result;
    }

    void doRootRequest(HttpServletRequest request, HttpServletResponse response) {

        try {
            // Handle a / request:
            Object responseMessage = home.get(request, response);
            if (responseMessage != null) {
                Serialiser.serialise(response, responseMessage);
            }

        } catch (Throwable t) {

            handleError(request, response, null, t);
        }
    }

    /**
     * GO!
     *
     * @param request         The request.
     * @param response        The response.
     */
    void doMethod(HttpServletRequest request, HttpServletResponse response, Class<?> method) {

        // Locate a request handler:
        String requestPath = mapRequestPath(request);
        Map<HttpMethod, RequestHandler> handlers = api.get(requestPath);
        RequestHandler requestHandler = null;
        if (handlers!=null) {
            requestHandler = handlers.get(method.getSimpleName());
        }

        try {

            if (requestHandler != null) {
                handleRequest(request, response, requestHandler);
            } else {
                handleNotFound(request, response);
            }

        } catch (Throwable t) {

            // Chances are the exception we've actually caught is the reflection
            // one from Method.invoke(...)
            Throwable caught = t;
            if (InvocationTargetException.class.isAssignableFrom(t.getClass())) {
                caught = t.getCause();
            }
            handleError(request, response, requestHandler, caught);
        }

    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response, RequestHandler requestHandler) throws Exception {

        // An API endpoint is defined for this request:
        Object handler = instantiate(requestHandler.endpointClass);
        Object responseMessage = invoke(request, response, handler, requestHandler.method, requestHandler.requestMessageType);
        if (requestHandler.responseMessageType != null && responseMessage != null) {
            Serialiser.serialise(response, responseMessage);
        }
    }

    /**
     * Handles a request where no API endpoint is defined. If {@link #notFound}
     * is set, {@link NotFound#handle(HttpServletRequest, HttpServletResponse)}
     * will be called. Otherwise a simple 404 will be returned.
     *
     * @param request
     * @param response
     * @throws IOException If an error occurs in sending the response.
     */
    private void handleNotFound(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Set a default response code:
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);

        // Attempt to handle the not-found:
        notFound.handle(request, response);
    }

    private void handleError(HttpServletRequest request, HttpServletResponse response, RequestHandler requestHandler, Throwable t) {

        // Set a default response code:
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        try {

            // Attempt to handle the error gracefully:
            Object errorResponse = serverError.handle(request, response, requestHandler, t);
            if (errorResponse != null) {
                Serialiser.serialise(response, errorResponse);
            }

        } catch (Throwable t2) {

            // Fall back to printing
            log.info("Error invoking error handler:");
            t2.printStackTrace();
            log.info("Original error being handled:");
            t.printStackTrace();
        }
    }

    /**
     * Determines the endpoint name for the path of the given request.
     *
     * @param request         The request.
     * @return A matching handler, if one exists.
     */
    public String mapRequestPath(HttpServletRequest request) {

        String endpointName = Path.newInstance(request).firstSegment();
        return StringUtils.lowerCase(endpointName);
    }

    private static Object instantiate(Class<?> endpointClass) {

        // Instantiate:
        Object result = null;
        try {
            result = endpointClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Unable to instantiate " + endpointClass.getSimpleName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to access " + endpointClass.getSimpleName(), e);
        } catch (NullPointerException e) {
            throw new RuntimeException("No class to instantiate", e);
        }
        return result;

    }

    private static Object invoke(HttpServletRequest request, HttpServletResponse response, Object handler, Method method, Class<?> requestMessage) throws Exception {
        Object result = null;

        log.info("Invoking method {} on {}", method.getName(), handler.getClass().getSimpleName());
        // + " for request message "
        // + requestMessage);
        if (requestMessage != null) {
            // try (InputStreamReader streamReader = new InputStreamReader(
            // request.getInputStream(), "UTF8")) {
            // int c;
            // while ((c = streamReader.read()) != -1) {
            // log.info((char) c);
            // }
            // } catch (UnsupportedEncodingException e) {
            // throw new RuntimeException("Unsupported encoding " + "UTF8"
            // + "?", e);
            // }
            // result = null;
            Object message = Serialiser.deserialise(request, requestMessage);
            result = method.invoke(handler, request, response, message);
        } else {
            result = method.invoke(handler, request, response);
        }

        // log.info("Result is " + result);
        return result;
    }
}
