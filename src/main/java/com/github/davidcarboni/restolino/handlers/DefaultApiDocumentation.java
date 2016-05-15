package com.github.davidcarboni.restolino.handlers;

import com.github.davidcarboni.restolino.api.ApiConfiguration;
import com.github.davidcarboni.restolino.framework.Home;
import com.github.davidcarboni.restolino.jetty.ApiHandler;
import com.github.davidcarboni.restolino.json.Serialiser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Default {@link Home} handler that prints out the API configuration (URIs, methods and messages).
 */
public class DefaultApiDocumentation implements Home {
    @Override
    public ApiConfiguration get(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Serialiser.getBuilder().setPrettyPrinting();
        return ApiHandler.api;
    }
}
