package com.github.davidcarboni.restolino.handlers;

import com.github.davidcarboni.restolino.framework.NotFound;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Default {@link NotFound} handler.
 */
public class NotFoundHandler implements NotFound {
    @Override
    public String handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return "No API endpoint is defined for " + request.getPathInfo();
    }
}
