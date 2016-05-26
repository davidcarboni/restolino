package com.github.davidcarboni.restolino.api;

import com.github.davidcarboni.restolino.framework.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an api endpoint, e.g. {@code /example}, mapped to a class of the same name annotated with {@link com.github.davidcarboni.restolino.framework.Api Api}.
 */
public class Route {

    public Class<?> endpointClass;
    public Map<HttpMethod, RequestHandler> requestHandlers = new HashMap<>();
}
