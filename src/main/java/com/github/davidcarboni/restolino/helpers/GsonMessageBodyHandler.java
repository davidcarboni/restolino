package com.github.davidcarboni.restolino.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

/**
 * From <a href=
 * "http://eclipsesource.com/blogs/2012/11/02/integrating-gson-into-a-jax-rs-based-application/"
 * >http://eclipsesource.com/blogs/2012/11/02/integrating-gson-into-a-jax-rs-
 * based-application/</a>
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public final class GsonMessageBodyHandler {

	private static final String UTF_8 = "UTF-8";

	public Object read(Class<Object> type, Type genericType,
			InputStream entityStream) throws IOException {
		try (InputStreamReader streamReader = new InputStreamReader(
				entityStream, UTF_8)) {
			Type jsonType;
			if (type.equals(genericType)) {
				jsonType = type;
			} else {
				jsonType = genericType;
			}
			return Serialiser.getBuilder().create()
					.fromJson(streamReader, jsonType);
		}
	}

	public void write(Object object, Class<?> type, Type genericType,
			OutputStream entityStream) throws IOException {
		try (OutputStreamWriter writer = new OutputStreamWriter(entityStream,
				UTF_8)) {
			Type jsonType;
			if (type.equals(genericType)) {
				jsonType = type;
			} else {
				jsonType = genericType;
			}
			Serialiser.getBuilder().create().toJson(object, jsonType, writer);
		}
	}
}