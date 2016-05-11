package com.github.davidcarboni.restolino.handlers;

import com.github.davidcarboni.restolino.api.RequestHandler;
import com.github.davidcarboni.restolino.framework.ServerError;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Default {@link ServerError} handler.
 *
 * This prints the error stack trace to stdout and also serialises it as a Json string to the response.
 */
public class DefaultServerErrorHandler implements ServerError {

    private static final Logger log = getLogger(DefaultServerErrorHandler.class);

    @Override
    public String handle(HttpServletRequest request, HttpServletResponse response, RequestHandler requestHandler, Throwable t) throws IOException {
        String stackTrace = ExceptionUtils.getStackTrace(t);
        log.info(stackTrace);
        return stackTrace;
    }
}
