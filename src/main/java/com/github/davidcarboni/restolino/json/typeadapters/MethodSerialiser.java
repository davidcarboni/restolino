package com.github.davidcarboni.restolino.json.typeadapters;

import com.google.gson.*;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Custome serialiser for {@link Method} that represents a method by name, return and parameter types array.
 */
public class MethodSerialiser extends ClassNameSerialiser implements JsonSerializer<Method> {

    @Override
    public JsonElement serialize(Method src, Type typeOfSrc, JsonSerializationContext context) {
        //JsonObject object = new JsonObject();
        //object.addProperty("name", src.getName());
        //object.addProperty("return", className(src.getReturnType()));
        //JsonArray parameters = new JsonArray();
        //for (Class<?> parameterType : src.getParameterTypes()) {
        //    parameters.add(new JsonPrimitive(className(parameterType)));
        //}
        //object.add("parameters", parameters);
        //return object;
        return new JsonPrimitive(src.getName());
    }
}
