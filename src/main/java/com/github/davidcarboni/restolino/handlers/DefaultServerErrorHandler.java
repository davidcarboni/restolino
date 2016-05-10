package com.github.davidcarboni.restolino.handlers;

import com.github.davidcarboni.restolino.api.RequestHandler;
import com.github.davidcarboni.restolino.framework.ServerError;
import com.github.davidcarboni.restolino.json.Serialiser;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Default {@link ServerError} handler.
 *
 * This prints the error stack trace to stdout and also serialises it as a Json string to the response.
 */
public class DefaultServerErrorHandler implements ServerError {

    @Override
    public String handle(HttpServletRequest request, HttpServletResponse response, RequestHandler requestHandler, Throwable t) throws IOException {
        String stackTrace = ExceptionUtils.getStackTrace(t);
        System.out.println(stackTrace);
        return stackTrace;
    }
}
