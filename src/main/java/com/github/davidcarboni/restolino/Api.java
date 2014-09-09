package com.github.davidcarboni.restolino;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.github.davidcarboni.restolino.helpers.Path;
import com.github.davidcarboni.restolino.interfaces.Boom;
import com.github.davidcarboni.restolino.interfaces.Endpoint;
import com.github.davidcarboni.restolino.interfaces.Home;
import com.github.davidcarboni.restolino.interfaces.NotFound;
import com.github.davidcarboni.restolino.json.Serialiser;

/**
 * This is the framework controller.
 * 
 * @author David Carboni
 * 
 */
public class Api {

	public Home home;
	public Boom boom;
	public NotFound notFound;

	public Map<String, RequestHandler> get = new HashMap<>();
	public Map<String, RequestHandler> put = new HashMap<>();
	public Map<String, RequestHandler> post = new HashMap<>();
	public Map<String, RequestHandler> delete = new HashMap<>();

	// The default request handler:
	public RequestHandler defaultRequestHandler = new RequestHandler();
	{
		defaultRequestHandler.endpointClass = DefaultRequestHandler.class;
		try {
			defaultRequestHandler.method = DefaultRequestHandler.class
					.getMethod("notImplemented", HttpServletRequest.class,
							HttpServletResponse.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(
					"Code issue - default request handler not found", e);
		}
	}

	public Map<String, RequestHandler> getMap(Annotation annotation) {
		Class<? extends Annotation> type = annotation.getClass();
		if (GET.class.isAssignableFrom(type))
			return get;
		else if (PUT.class.isAssignableFrom(type))
			return put;
		else if (POST.class.isAssignableFrom(type))
			return post;
		else if (DELETE.class.isAssignableFrom(type))
			return delete;
		return null;
	}

	public Api(ClassLoader classLoader) {

		// Build a reflections instance to find classes:
		Reflections reflections = createReflections(classLoader);

		// Set up the API endpoints:
		configureEndpoints(reflections);

		// Configure standard handlers:
		configureHome(reflections);
		configureNotFound(reflections);
		configureBoom(reflections);
	}

	/**
	 * Searches for and configures all your lovely endpoints.
	 * 
	 * @param reflections
	 *            The instance to use to find classes.
	 */
	void configureEndpoints(Reflections reflections) {

		// [Re]initialise the maps:
		get = new HashMap<>();
		put = new HashMap<>();
		post = new HashMap<>();
		delete = new HashMap<>();

		System.out.println("Scanning for endpoints..");
		Set<Class<?>> endpoints = reflections
				.getTypesAnnotatedWith(Endpoint.class);
		// System.out.println(reflections.getConfiguration().getUrls());

		System.out.println("Found " + endpoints.size() + " endpoints.");
		System.out.println("Examining endpoint methods..");

		// Configure the classes:
		for (Class<?> endpointClass : endpoints) {
			String endpointName = StringUtils.lowerCase(endpointClass
					.getSimpleName());
			System.out.println(" - /" + endpointName + " ("
					+ endpointClass.getName() + ")");

			for (Method method : endpointClass.getMethods()) {

				// Skip Object methods
				if (method.getDeclaringClass() == Object.class)
					continue;

				// We're looking for public methods that take reqest, responso
				// and optionally a message type:
				Class<?>[] parameterTypes = method.getParameterTypes();
				if (Modifier.isPublic(method.getModifiers())
						&& parameterTypes.length >= 2
						&& HttpServletRequest.class
								.isAssignableFrom(parameterTypes[0])
						&& HttpServletResponse.class
								.isAssignableFrom(parameterTypes[1])) {

					// Which HTTP method(s) will this method respond to?
					List<Annotation> annotations = Arrays.asList(method
							.getAnnotations());
					for (Annotation annotation : annotations) {

						Map<String, RequestHandler> map = getMap(annotation);
						if (map != null) {
							clashCheck(endpointName, annotation, endpointClass,
									method);
							System.out.print("   - "
									+ annotation.getClass().getInterfaces()[0]
											.getSimpleName());
							RequestHandler requestHandler = new RequestHandler();
							requestHandler.endpointClass = endpointClass;
							requestHandler.method = method;
							System.out.print(" " + method.getName());
							if (parameterTypes.length > 2) {
								requestHandler.requestMessageType = parameterTypes[2];
								System.out.print(" request:"
										+ requestHandler.requestMessageType
												.getSimpleName());
							}
							if (method.getReturnType() != void.class) {
								requestHandler.responseMessageType = method
										.getReturnType();
								System.out.print(" response:"
										+ requestHandler.responseMessageType
												.getSimpleName());
							}
							map.put(endpointName, requestHandler);
							System.out.println();
						}
					}
				}
			}

			// Set default handlers where needed:
			// TODO: could these be set as defaults up above? I guess the class
			// check would need to change.
			if (!get.containsKey(endpointName))
				get.put(endpointName, defaultRequestHandler);
			if (!put.containsKey(endpointName))
				put.put(endpointName, defaultRequestHandler);
			if (!post.containsKey(endpointName))
				post.put(endpointName, defaultRequestHandler);
			if (!delete.containsKey(endpointName))
				delete.put(endpointName, defaultRequestHandler);
		}

	}

	private void clashCheck(String name, Annotation annotation,
			Class<?> endpointClass, Method method) {
		Map<String, RequestHandler> map = getMap(annotation);
		if (map != null) {
			if (map.containsKey(name))
				System.out.println("   ! method " + method.getName() + " in "
						+ endpointClass.getName() + " overwrites "
						+ map.get(name).method.getName() + " in "
						+ map.get(name).endpointClass.getName() + " for "
						+ annotation.getClass().getSimpleName());
		} else {
			System.out.println("WAT? Expected GET/PUT/POST/DELETE but got "
					+ annotation.getClass().getName());
		}
	}

	/**
	 * Searches for and configures the / endpoint.
	 * 
	 * @param reflections
	 *            The instance to use to find classes.
	 * @return {@link Home}
	 */
	void configureHome(Reflections reflections) {

		System.out.println("Checking for a / endpoint..");
		Home home = getEndpoint(Home.class, "/", reflections);
		if (home != null)
			System.out.println("Class " + home.getClass().getSimpleName()
					+ " configured as / endpoint");
		this.home = home;
	}

	/**
	 * Searches for and configures the not found endpoint.
	 * 
	 * @param reflections
	 *            The instance to use to find classes.
	 * @return {@link NotFound}
	 */
	void configureNotFound(Reflections reflections) {

		System.out.println("Checking for a not-found endpoint..");
		NotFound notFound = getEndpoint(NotFound.class, "not-found",
				reflections);
		if (notFound != null)
			System.out.println("Class " + notFound.getClass().getSimpleName()
					+ " configured as not-found endpoint");
		this.notFound = notFound;
	}

	/**
	 * Searches for and configures the not found endpoint.
	 * 
	 * @param reflections
	 *            The instance to use to find classes.
	 * @return {@link Boom}
	 */
	void configureBoom(Reflections reflections) {

		System.out.println("Checking for an error endpoint..");
		Boom boom = getEndpoint(Boom.class, "error", reflections);
		if (boom != null)
			System.out.println("Class " + boom.getClass().getSimpleName()
					+ " configured as error endpoint");
		this.boom = boom;
	}

	public void get(HttpServletRequest request, HttpServletResponse response) {

		if (home != null && isRootRequest(request)) {
			doRootRequest(request, response);
		} else {
			doMethod(request, response, get);
		}
	}

	public void put(HttpServletRequest request, HttpServletResponse response) {
		doMethod(request, response, put);
	}

	public void post(HttpServletRequest request, HttpServletResponse response) {
		doMethod(request, response, post);
	}

	public void delete(HttpServletRequest request, HttpServletResponse response) {
		doMethod(request, response, delete);
	}

	public void options(HttpServletRequest request, HttpServletResponse response) {

		List<String> result = new ArrayList<>();

		if (home != null && isRootRequest(request)) {

			// We only allow GET to the root resource:
			result.add("GET");

		} else {

			// Determine which methods are configured:
			if (mapRequestPath(get, request) != null)
				result.add("GET");
			if (mapRequestPath(put, request) != null)
				result.add("PUT");
			if (mapRequestPath(post, request) != null)
				result.add("POST");
			if (mapRequestPath(delete, request) != null)
				result.add("DELETE");
		}

		response.setHeader("Allow", StringUtils.join(result, ','));
	}

	/**
	 * Determines if the given request is for the root resource (ie /).
	 * 
	 * @param request
	 *            The {@link HttpServletRequest}
	 * @return If {@link HttpServletRequest#getPathInfo()} is null, empty string
	 *         or "/" ten true.
	 */
	static boolean isRootRequest(HttpServletRequest request) {
		String path = request.getPathInfo();
		if (StringUtils.isBlank(path))
			return true;
		else if (StringUtils.equals("/", path))
			return true;
		return false;
	}

	/**
	 * Locates a single endpoint class.
	 * 
	 * @param type
	 * @param name
	 * @param reflections
	 * @return
	 */
	private static <E> E getEndpoint(Class<E> type, String name,
			Reflections reflections) {
		E result = null;

		// Get concrete subclasses:
		Set<Class<? extends E>> foundClasses = reflections.getSubTypesOf(type);
		Set<Class<? extends E>> endpointClasses = new HashSet<>();
		for (Class<? extends E> clazz : foundClasses) {
			if (!Modifier.isAbstract(clazz.getModifiers()))
				endpointClasses.add(clazz);
		}

		if (endpointClasses.size() == 0)

			// No endpoint found:
			System.out.println("No " + name
					+ " endpoint configured. Just letting you know.");

		else {

			// Dump multiple endpoints:
			if (endpointClasses.size() > 1) {
				System.out.println("Warning: found multiple candidates for "
						+ name + " endpoint: " + endpointClasses);
			}

			// Instantiate the endpoint:
			try {
				result = endpointClasses.iterator().next().newInstance();
			} catch (Exception e) {
				System.out.println("Error: cannot instantiate " + name
						+ " endpoint class "
						+ endpointClasses.iterator().next());
				e.printStackTrace();
			}
		}

		return result;
	}

	void doRootRequest(HttpServletRequest request, HttpServletResponse response) {

		try {
			// Handle a / request:
			Object responseMessage = home.get(request, response);
			if (responseMessage != null)
				Serialiser.serialise(response, responseMessage);

		} catch (Throwable t) {

			handleError(request, response, null, t);
		}
	}

	/**
	 * GO!
	 * 
	 * @param request
	 *            The request.
	 * @param response
	 *            The response.
	 * @param requestHandlers
	 *            One of the handler maps.
	 */
	void doMethod(HttpServletRequest request, HttpServletResponse response,
			Map<String, RequestHandler> requestHandlers) {

		// Locate a request handler:
		RequestHandler requestHandler = mapRequestPath(requestHandlers, request);

		try {

			if (requestHandler != null) {
				handleRequest(request, response, requestHandler);
			} else {
				handleNotFound(request, response);
			}

		} catch (Throwable t) {

			handleError(request, response, requestHandler, t);
		}

	}

	private void handleRequest(HttpServletRequest request,
			HttpServletResponse response, RequestHandler requestHandler)
			throws IOException {

		// An API endpoint is defined for this request:
		Object handler = instantiate(requestHandler.endpointClass);
		Object responseMessage = invoke(request, response, handler,
				requestHandler.method, requestHandler.requestMessageType);
		if (requestHandler.responseMessageType != null) {
			Serialiser.serialise(response, responseMessage);
		}
	}

	/**
	 * Handles a request where no API endpoint is defined. If {@link #notFound}
	 * is set, {@link NotFound#handle(HttpServletRequest, HttpServletResponse)}
	 * will be called. Otherwise a simple 404 will be returned.
	 * 
	 * @param response
	 * @param requestHandler
	 * @throws IOException
	 *             If an error occurs in sending the response.
	 */
	private void handleNotFound(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		// Set a default response code:
		response.setStatus(HttpStatus.SC_NOT_FOUND);

		if (notFound != null) {
			// Attempt to handle the not-found gracefully:
			notFound.handle(request, response);
		} else {
			// Default not-found behaviour:
			Serialiser.serialise(response, "No API endpoint is defined for '"
					+ request.getPathInfo() + "'");
		}
	}

	private void handleError(HttpServletRequest request,
			HttpServletResponse response, RequestHandler requestHandler,
			Throwable t) {

		// Set a default response code:
		response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);

		try {
			if (boom != null) {
				// Attempt to handle the error gracefully:
				boom.handle(request, response, requestHandler, t);
			} else {
				String stackTrace = ExceptionUtils.getStackTrace(t);
				System.out.println(stackTrace);

				Serialiser.serialise(response, stackTrace);
			}

		} catch (Throwable t2) {
			t2.printStackTrace();
		}
	}

	/**
	 * Locates a {@link RequestHandler} for the path of the given request.
	 * 
	 * @param requestHandlers
	 *            One of the handler maps.
	 * @param request
	 *            The request.
	 * @return A matching handler, if one exists.
	 */
	public RequestHandler mapRequestPath(
			Map<String, RequestHandler> requestHandlers,
			HttpServletRequest request) {

		String endpointName = Path.newInstance(request).firstSegment();
		endpointName = StringUtils.lowerCase(endpointName);
		// System.out.println("Mapping endpoint " + endpointName);
		return requestHandlers.get(endpointName);
	}

	private Object instantiate(Class<?> endpointClass) {

		// Instantiate:
		Object result = null;
		try {
			result = endpointClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException("Unable to instantiate "
					+ endpointClass.getSimpleName(), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to access "
					+ endpointClass.getSimpleName(), e);
		} catch (NullPointerException e) {
			throw new RuntimeException("No class to instantiate", e);
		}
		return result;

	}

	private Object invoke(HttpServletRequest request,
			HttpServletResponse response, Object handler, Method method,
			Class<?> requestMessage) {
		Object result = null;

		System.out.println("Invoking method " + method.getName() + " on "
				+ handler.getClass().getSimpleName() + " for request message "
				+ requestMessage);
		try {
			if (requestMessage != null) {
				Object message = Serialiser
						.deserialise(request, requestMessage);
				result = method.invoke(handler, request, response, message);
			} else {
				result = method.invoke(handler, request, response);
			}
		} catch (Exception e) {
			System.out.println("!Error: " + e.getMessage());
			throw new RuntimeException("Error invoking method "
					+ method.getName() + " on "
					+ handler.getClass().getSimpleName(), e);
		}

		System.out.println("Result is " + result);
		return result;
	}

	private static Reflections createReflections(ClassLoader classLoader) {

		// We set up reflections to use the classLoader for loading classes
		// and also to use the classLoader to determine the list of URLs:
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.addClassLoader(classLoader).addUrls(
						ClasspathHelper.forClassLoader(classLoader)));
		// System.out.println(reflections.getConfiguration().getUrls());
		return reflections;
	}

}
