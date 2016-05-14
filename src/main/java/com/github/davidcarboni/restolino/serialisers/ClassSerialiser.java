package com.github.davidcarboni.restolino.serialisers;


import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Custom serialiser for {@link Class} that represents a class by name.
 */
public class ClassSerialiser extends ClassNameSerialiser implements JsonSerializer<Class<?>> {
    @Override
    public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(className(src));
    }
}
