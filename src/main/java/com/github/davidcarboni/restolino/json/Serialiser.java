package com.github.davidcarboni.restolino.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class Serialiser {

    final static String UTF8 = "UTF8";

    private static GsonBuilder builder;

    /**
     * Serialises the given object to Json.
     *
     * @param object The object be serialised.
     * @return The Json as a String.
     */
    public static String serialise(Object object) {
        Gson gson = getBuilder().create();
        return gson.toJson(object);
    }

    /**
     * Deserialises the given json String.
     *
     * @param json The Json to deserialise.
     * @param type The type to deserialise into.
     * @param <O>  The type to deserialise to.
     * @return A new instance of the given type.
     */
    public static <O> O deserialise(String json, Class<O> type) {
        Gson gson = getBuilder().create();
        return gson.fromJson(json, type);
    }

    /**
     * Serialises the given object to Json and writes it to the given
     * {@link OutputStream}.
     *
     * @param output          The output stream to serialise to.
     * @param responseMessage The message to be serialised.
     * @throws IOException If an error occurs in writing the output.
     */
    public static void serialise(OutputStream output, Object responseMessage)
            throws IOException {

        try (OutputStreamWriter writer = new OutputStreamWriter(output, UTF8)) {
            Gson gson = getBuilder().create();
            gson.toJson(responseMessage, writer);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding " + UTF8 + "?", e);
        }
    }

    /**
     * Deserialises the given {@link InputStream} to a JSON String.
     *
     * @param input              The stream to deserialise.
     * @param requestMessageType The message type to deserialise into.
     * @param <O>                The type to deserialise to.
     * @return A new instance of the given type.
     * @throws IOException If an error occurs in reading from the input stream.
     */
    public static <O> O deserialise(InputStream input,
                                    Class<O> requestMessageType) throws IOException {

        try (InputStreamReader streamReader = new InputStreamReader(input, UTF8)) {
            Gson gson = getBuilder().create();
            return gson.fromJson(streamReader, requestMessageType);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding " + UTF8 + "?", e);
        }
    }

    /**
     * Serialises the given object to Json and writes it to the given
     * {@link HttpServletResponse}.
     *
     * @param response        The http response to serialise to.
     * @param responseMessage The message to be serialised.
     * @throws IOException If an error occurs in writing the output.
     */
    public static void serialise(HttpServletResponse response,
                                 Object responseMessage) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding(UTF8);
        serialise(response.getOutputStream(), responseMessage);
    }

    /**
     * Deserialises the given {@link HttpServletRequest} to a JSON String.
     *
     * @param request            The request to deserialise.
     * @param requestMessageType The message type to deserialise into.
     * @param <O>                The type to deserialise to.
     * @return A new instance of the given type.
     * @throws IOException If an error occurs in reading from the request input stream.
     */
    public static <O> O deserialise(HttpServletRequest request,
                                    Class<O> requestMessageType) throws IOException {

        return deserialise(request.getInputStream(), requestMessageType);
    }

    /**
     * @return A lazily instantiated and cached {@link GsonBuilder}.
     */
    public static GsonBuilder getBuilder() {
        if (builder == null) {
            builder = new GsonBuilder();
        }
        return builder;
    }

}
