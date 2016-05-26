package com.github.davidcarboni.restolino.routes;

import com.github.davidcarboni.restolino.api.Router;
import com.github.davidcarboni.restolino.framework.Home;
import com.github.davidcarboni.restolino.jetty.ApiHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Default {@link Home} handler that prints out the API configuration (endpoints, http method request handlers and request/response Json messages).
 */
public class DefaultApiDocumentation implements Home {
    @Override
    public Router get(HttpServletRequest req, HttpServletResponse res) throws IOException {
        //Serialiser.getBuilder().setPrettyPrinting();
        return ApiHandler.api;
    }
}
