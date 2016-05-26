package com.github.davidcarboni.restolino.jetty;

import com.github.davidcarboni.restolino.Main;
import com.github.davidcarboni.restolino.api.Router;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.reflections.Reflections;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class ApiHandler extends AbstractHandler {

    private static final Logger log = getLogger(ApiHandler.class);
    static final String KEY_CLASSES = "restolino.classes";

    public static volatile Router api;

    public ApiHandler() {
        if (Main.configuration.classesInClasspath != null) {
            log.info("Classes are included in the classpath. No reloading will be configured (" + Main.configuration.classesInClasspath + ")");
        }
    }

    public static void setupApi(Reflections reflections) {
        api = new Router(reflections);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String method = request.getMethod();
        if (StringUtils.equals("GET", method)) {
            api.get(request, response);
        } else if (StringUtils.equals("PUT", method)) {
            api.put(request, response);
        } else if (StringUtils.equals("POST", method)) {
            api.post(request, response);
        } else if (StringUtils.equals("DELETE", method)) {
            api.delete(request, response);
        } else if (StringUtils.equals("OPTIONS", method)) {
            api.options(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }
}