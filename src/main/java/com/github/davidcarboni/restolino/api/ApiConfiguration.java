package com.github.davidcarboni.restolino.api;

import com.github.davidcarboni.restolino.framework.*;
import com.github.davidcarboni.restolino.framework.HttpMethod;
import com.github.davidcarboni.restolino.handlers.DefaultApiDocumentation;
import com.github.davidcarboni.restolino.handlers.DefaultNotFound;
import com.github.davidcarboni.restolino.handlers.DefaultServerError;
import com.github.davidcarboni.restolino.helpers.Path;
import com.github.davidcarboni.restolino.json.Serialiser;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    public Map<String, Endpoint> api = new HashMap<>();

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

        log.info("Scanning for endpoint classes..");
        Set<Class<?>> endpoints = reflections.getTypesAnnotatedWith(Api.class);
        // log.info(reflections.getConfiguration().getUrls());

        log.info("Found " + endpoints.size() + " endpoint classes.");
        log.info("Examining endpoint class methods:");

        // Configure the classes:
        for (Class<?> endpointClass : endpoints) {

            Endpoint endpoint = getEndpoint(endpointClass);
            endpoint.endpointClass = endpointClass;

            for (Method method : endpointClass.getMethods()) {

                // Skip Object methods
                if (method.getDeclaringClass() == Object.class) {
                    continue;
                }

                // Find public methods:
                if (Modifier.isPublic(method.getModifiers())) {

                    // Which HTTP method(s) will this method respond to?
                    annotation:
                    for (Annotation annotation : method.getAnnotations()) {
                        HttpMethod httpMethod = HttpMethod.method(annotation);
                        if (httpMethod != null) {
                            log.info("Http method: {}", httpMethod);

                            RequestHandler requestHandler = new RequestHandler();
                            requestHandler.endpointMethod = method;
                            log.info("Java method: {}", method.getName());

                            // Look for an optional Json message type parameter:
                            for (Class<?> parameterType : method.getParameterTypes()) {
                                if (!HttpServletRequest.class.isAssignableFrom(parameterType)
                                        && !HttpServletResponse.class.isAssignableFrom(parameterType)) {
                                    if (requestHandler.requestMessageType != null) {
                                        log.error("Too many parameters on {} method {}. " +
                                                        "Message type already set to {} but also found a {} parameter.",
                                                httpMethod, method.getName(),
                                                requestHandler.requestMessageType.getSimpleName(), parameterType.getSimpleName());
                                        break annotation;
                                    }
                                    requestHandler.requestMessageType = parameterType;
                                    log.info("request Json: {}", requestHandler.requestMessageType.getSimpleName());
                                }
                            }

                            // Check the response Json message type:
                            if (method.getReturnType() != void.class) {
                                requestHandler.responseMessageType = method.getReturnType();
                                log.info("Response Json: {}", requestHandler.responseMessageType.getSimpleName());
                            }

                            endpoint.requestHandlers.put(httpMethod, requestHandler);

                        }
                    }
                }
            }
        }

    }

    private Endpoint getEndpoint(Class<?> endpointClass) {
        String endpointName = StringUtils.lowerCase(endpointClass.getSimpleName());
        log.info("Endpoint: /{} (Class {})", endpointName, endpointClass.getName());
        if (!api.containsKey(endpointName)) {
            api.put(endpointName, new Endpoint());
        }
        return api.get(endpointName);
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
        if (notFound == null) notFound = new DefaultNotFound();
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
        if (serverError == null) serverError = new DefaultServerError();
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
            doMethod(request, response, HttpMethod.GET);
        }
    }

    public void put(HttpServletRequest request, HttpServletResponse response) {
        doMethod(request, response, HttpMethod.PUT);
    }

    public void post(HttpServletRequest request, HttpServletResponse response) {
        doMethod(request, response, HttpMethod.POST);
    }

    public void delete(HttpServletRequest request, HttpServletResponse response) {
        doMethod(request, response, HttpMethod.DELETE);
    }

    public void options(HttpServletRequest request, HttpServletResponse response) {

        List<String> result = new ArrayList<>();

        if (isRootRequest(request)) {

            // We only allow GET to the root resource:
            result.add(HttpMethod.GET.name());

        } else {

            // Determine which http methods are configured:
            String requestPath = mapRequestPath(request);
            if (api.containsKey(requestPath)) {
                for (HttpMethod httpMethod : api.get(requestPath).requestHandlers.keySet()) {
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
     * @param request  The request.
     * @param response The response.
     */
    void doMethod(HttpServletRequest request, HttpServletResponse response, HttpMethod httpMethod) {

        // Locate a request handler:
        String requestPath = mapRequestPath(request);
        Endpoint endpoint = api.get(requestPath);

        try {

            if (endpoint != null && endpoint.requestHandlers.containsKey(httpMethod)) {
                handleRequest(request, response, endpoint, httpMethod);
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

            RequestHandler requestHandler = (endpoint == null ? null : endpoint.requestHandlers.get(httpMethod));
            handleError(request, response, requestHandler, caught);
        }

    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response, Endpoint endpoint, HttpMethod httpMethod) throws Exception {

        // An API endpoint is defined for this request:
        Object handler = instantiate(endpoint.endpointClass);
        RequestHandler requestHandler = endpoint.requestHandlers.get(httpMethod);
        Object responseMessage = invoke(request, response, handler, requestHandler.endpointMethod, requestHandler.requestMessageType);
        if (requestHandler.responseMessageType != null && responseMessage != null) {
            Serialiser.serialise(response, responseMessage);
        }
    }

    /**
     * Handles a request where no API endpoint is defined. If {@link #notFound}
     * is set, {@link NotFound#handle(HttpServletRequest, HttpServletResponse)}
     * will be called. Otherwise a simple 404 will be returned.
     *
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @throws IOException If an error occurs in sending the response.
     */
    private void handleNotFound(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Set a default response code:
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);

        // Attempt to handle the not-found:
        Object notFoundResponse = notFound.handle(request, response);
        if (notFoundResponse != null) {
            Serialiser.serialise(response, notFoundResponse);
        }
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
            log.error("Error invoking error handler", t2);
            log.error("Original error being handled", t);
        }
    }

    /**
     * Determines the endpoint name for the path of the given request.
     *
     * @param request The request.
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

        // Build the parameter list:
        ArrayList<Object> args = new ArrayList<>();
        for (Class<?> parameterType : method.getParameterTypes()) {
            if (parameterType.isAssignableFrom(HttpServletRequest.class)) {
                args.add(request);
            } else if (parameterType.isAssignableFrom(HttpServletResponse.class)) {
                args.add(response);
            } else if (requestMessage != null && parameterType.isAssignableFrom(requestMessage)) {
                args.add(Serialiser.deserialise(request, requestMessage));
            } else {
                log.warn("Warning: unexpected parameter type {}. Attempting to assign null", parameterType.getSimpleName());
                args.add(null);
            }
        }

        log.info("Invoking method {} on {}", method.getName(), handler.getClass().getSimpleName());
        result = method.invoke(handler, args.toArray());

        // log.info("Result is " + result);
        return result;
    }
}
