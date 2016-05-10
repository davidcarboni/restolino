package com.github.davidcarboni.restolino.handlers;

import com.github.davidcarboni.restolino.Configuration;
import com.github.davidcarboni.restolino.Main;
import com.github.davidcarboni.restolino.framework.Home;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Default {@link Home} handler.
 */
public class DefaultApiDocumentation implements Home {
    @Override
    public Configuration get(HttpServletRequest req, HttpServletResponse res) throws IOException {
        return Main.configuration;
    }
}
