package com.github.davidcarboni.restolino.serialisers;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Custom serialiser for the {@link Class} of an object that represents the class by name.
 */
public class ObjectClassSerialser  extends ClassNameSerialiser implements JsonSerializer<Object> {

    @Override
    public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(className(src.getClass()));
    }
}
