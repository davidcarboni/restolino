package com.github.davidcarboni.restolino.api;

import java.lang.reflect.Method;

public class RequestHandler {

    public Method endpointMethod;
    public Class<?> requestMessageType;
    public Class<?> responseMessageType;
}
