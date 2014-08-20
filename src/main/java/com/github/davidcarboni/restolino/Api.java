package com.github.davidcarboni.restolino;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.github.davidcarboni.restolino.interfaces.Boom;
import com.github.davidcarboni.restolino.interfaces.Endpoint;
import com.github.davidcarboni.restolino.interfaces.Home;
import com.github.davidcarboni.restolino.interfaces.NotFound;
import com.github.davidcarboni.restolino.servlet.ApiServlet;
import com.github.davidcarboni.restolino.servlet.RequestHandler;

/**
 * This is the framework controller.
 * 
 * @author David Carboni
 * 
 */
public class Api {

	public static void setup(ClassLoader classLoader) {

		// We set up reflections to use the classLoader for loading classes
		// and also to use the classLoader to determine the list of URLs:
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.addClassLoader(classLoader).addUrls(
						ClasspathHelper.forClassLoader(classLoader)));

		configureEndpoints(reflections, classLoader);
		ApiServlet.home = configureHome(reflections);
		ApiServlet.notFound = configureNotFound(reflections);
		ApiServlet.boom = configureBoom(reflections);
	}

	/**
	 * Searches for and configures all your lovely endpoints.
	 * 
	 * @param reflections
	 *            The instance to use to find classes.
	 */
	static void configureEndpoints(Reflections reflections,
			ClassLoader classLoader) {

		// [Re]initialise the maps:
		ApiServlet.get = new HashMap<>();
		ApiServlet.put = new HashMap<>();
		ApiServlet.post = new HashMap<>();
		ApiServlet.delete = new HashMap<>();

		System.out.println("Scanning for endpoints..");
		Set<Class<?>> endpoints = reflections
				.getTypesAnnotatedWith(Endpoint.class);
		System.out.println(reflections.getConfiguration().getUrls());

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

						Map<String, RequestHandler> map = ApiServlet
								.getMap(annotation.getClass());
						if (map != null) {
							clashCheck(endpointName, annotation.getClass(),
									endpointClass, method);
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
			if (!ApiServlet.get.containsKey(endpointName))
				ApiServlet.get.put(endpointName,
						ApiServlet.defaultRequestHandler);
			if (!ApiServlet.put.containsKey(endpointName))
				ApiServlet.put.put(endpointName,
						ApiServlet.defaultRequestHandler);
			if (!ApiServlet.post.containsKey(endpointName))
				ApiServlet.post.put(endpointName,
						ApiServlet.defaultRequestHandler);
			if (!ApiServlet.delete.containsKey(endpointName))
				ApiServlet.delete.put(endpointName,
						ApiServlet.defaultRequestHandler);
		}

	}

	private static void clashCheck(String name,
			Class<? extends Annotation> annotation, Class<?> endpointClass,
			Method method) {
		Map<String, RequestHandler> map = ApiServlet.getMap(annotation);
		if (map != null) {
			if (map.containsKey(name))
				System.out.println("   ! method " + method.getName() + " in "
						+ endpointClass.getName() + " overwrites "
						+ map.get(name).method.getName() + " in "
						+ map.get(name).endpointClass.getName() + " for "
						+ annotation.getSimpleName());
		} else {
			System.out.println("WAT. Expected GET/PUT/POST/DELETE but got "
					+ annotation.getName());
		}
	}

	/**
	 * Searches for and configures the / endpoint.
	 * 
	 * @param reflections
	 *            The instance to use to find classes.
	 * @return {@link Home}
	 */
	static Home configureHome(Reflections reflections) {

		System.out.println("Checking for a / endpoint..");
		Home home = getEndpoint(Home.class, "/", reflections);
		if (home != null)
			System.out.println("Class " + home.getClass().getSimpleName()
					+ " configured as / endpoint");
		return home;
	}

	/**
	 * Searches for and configures the not found endpoint.
	 * 
	 * @param reflections
	 *            The instance to use to find classes.
	 * @return {@link NotFound}
	 */
	static NotFound configureNotFound(Reflections reflections) {

		System.out.println("Checking for a not-found endpoint..");
		NotFound notFound = getEndpoint(NotFound.class, "not-found",
				reflections);
		if (notFound != null)
			System.out.println("Class " + notFound.getClass().getSimpleName()
					+ " configured as not-found endpoint");
		return notFound;
	}

	/**
	 * Searches for and configures the not found endpoint.
	 * 
	 * @param reflections
	 *            The instance to use to find classes.
	 * @return {@link Boom}
	 */
	static Boom configureBoom(Reflections reflections) {

		System.out.println("Checking for an error endpoint..");
		Boom boom = getEndpoint(Boom.class, "error", reflections);
		if (boom != null)
			System.out.println("Class " + boom.getClass().getSimpleName()
					+ " configured as error endpoint");
		return boom;
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

		// Get annotated classes:
		Set<Class<? extends E>> endpointClasses = reflections
				.getSubTypesOf(type);

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

}
