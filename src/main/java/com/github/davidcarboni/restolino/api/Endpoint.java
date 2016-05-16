package com.github.davidcarboni.restolino.api;

import com.github.davidcarboni.restolino.framework.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by david on 16/05/2016.
 */
public class Endpoint {

    public Class<?> endpointClass;
    public Map<HttpMethod, RequestHandler> methods = new HashMap<>();
}
