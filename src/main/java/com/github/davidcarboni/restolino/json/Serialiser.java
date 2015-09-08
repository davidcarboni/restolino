package com.github.davidcarboni.restolino.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
     * {@link Path}. This method will acquire a filesystem lock on the
     * given path in order to avoid corruption in the event that multple
     * threads attempt to write to the same file at the same time.
     *
     * @param output The Path to serialise to.
     * @param json   The Json to be serialised.
     * @throws IOException If an error occurs in writing the output.
     */
    public static void serialise(Path output, Object json) throws IOException {
        try (FileChannel channel = FileChannel.open(output, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
             OutputStreamWriter writer = new OutputStreamWriter(
                     new BufferedOutputStream(
                             Channels.newOutputStream(channel))
                     , UTF8);
             FileLock lock = writeLock(channel)) {
            Gson gson = getBuilder().create();
            gson.toJson(json, writer);
        }
    }

    private static FileLock writeLock(FileChannel channel) throws IOException {

        // Be lenient in getting a lock:
        FileLock lock = null;
        do {
            try {
                // Get an exclusive lock for writing
                lock = channel.lock();
            } catch (OverlappingFileLockException e) {
                Thread.yield();
            }
        } while (lock == null);

        return lock;
    }

    /**
     * Deserialises the given {@link InputStream} to a JSON String.
     *
     * @param input    The stream to deserialise.
     * @param jsonType The object type to deserialise into.
     * @param <O>      The type to deserialise to.
     * @return A new instance of the given type.
     * @throws IOException If an error occurs in reading from the input stream.
     */
    public static <O> O deserialise(Path input, Class<O> jsonType) throws IOException {

        try (FileChannel channel = FileChannel.open(input, StandardOpenOption.READ);
             InputStreamReader reader = new InputStreamReader(
                     new BufferedInputStream(
                             Channels.newInputStream(channel))
                     , UTF8);
             FileLock lock = readLock(channel)) {
            Gson gson = getBuilder().create();
            return gson.fromJson(reader, jsonType);
        }
    }

    private static FileLock readLock(FileChannel channel) throws IOException {

        // Be lenient in getting a lock:
        FileLock lock = null;
        do {
            try {
                // Get a shared lock for reading:
                lock = channel.lock(0L, Long.MAX_VALUE, true);
            } catch (OverlappingFileLockException e) {
                Thread.yield();
            }
        } while (lock == null);

        return lock;
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

    public static class Bob {
        String test = "content";
    }

    public static void main(String[] args) throws IOException {

        AtomicInteger integer = new AtomicInteger();
        final AtomicLong number = new AtomicLong(System.currentTimeMillis());

        for (int i = 0; i < 1000; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Bob bob = new Bob();
                        bob.test = "" + number.getAndIncrement();
                        Serialiser.serialise(Paths.get("./test.txt"), bob);
                        bob = Serialiser.deserialise(Paths.get("./test.txt"), Bob.class);
                        System.out.println(Thread.currentThread().getName() + " - " + bob.test);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, "s-" + integer.getAndIncrement()).start();
        }

    }

}
