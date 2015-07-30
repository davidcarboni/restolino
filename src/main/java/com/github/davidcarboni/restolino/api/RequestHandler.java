package com.github.davidcarboni.restolino.api;

import java.lang.reflect.Method;

public class RequestHandler {

    public Class<?> endpointClass;
    public Method method;
    public Class<?> requestMessageType;
    public Class<?> responseMessageType;
}
