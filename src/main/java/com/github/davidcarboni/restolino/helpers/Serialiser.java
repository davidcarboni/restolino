package com.github.davidcarboni.restolino.helpers;

//import org.bson.types.ObjectId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Serialiser {

	// public static final String bsonDateFormat = "MMMM dd, yyyy HH:mm:ss";

	private static GsonBuilder builder;

	/**
	 * Serialises the given object to Json.
	 * 
	 * @param object
	 *            To be serialised.
	 * @return The Json as a String.
	 */
	public static String serialise(Object object) {
		Gson gson = getBuilder().create();
		return gson.toJson(object);
	}

	/**
	 * Deserialises the given json String.
	 * 
	 * @param json
	 *            The Json to deserialise.
	 * @param type
	 *            The type to deserialise into.
	 * @return A new instance of the given type.
	 */
	public static <T> T deserialise(String json, Class<T> type) {
		Gson gson = getBuilder().create();
		return gson.fromJson(json, type);
	}

	/**
	 * @return A {@link GsonBuilder} with an {@link ObjectIdSerialiser} type
	 *         adapter registered, plus any additional adapters from
	 *         {@link #typeAdapters}.
	 */
	public static GsonBuilder getBuilder() {
		if (builder == null) {
			// Map<Class<?>, Object> typeAdapters = new HashMap<>();
			builder = new GsonBuilder();

			// May well need type adapters at some point:

			// // result.registerTypeAdapter(ObjectId.class, new
			// // ObjectIdSerialiser());
			// for (Class<?> type : typeAdapters.keySet()) {
			// builder.registerTypeAdapter(type, typeAdapters.get(type));
			// }
			// builder.registerTypeAdapter(DateTime.class,
			// DateTimeSerialiser.class);
			// builder.registerTypeAdapter(ObjectId.class,
			// ObjectIdSerialiser.class);
			// builder.setDateFormat(bsonDateFormat);
		}
		return builder;
	}

}
