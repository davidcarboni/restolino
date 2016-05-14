package com.github.davidcarboni.restolino.handlers;

import com.github.davidcarboni.restolino.Configuration;
import com.github.davidcarboni.restolino.Main;
import com.github.davidcarboni.restolino.framework.Home;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Returns the Restolino configuration.
 */
public class DefaultRestolinoConfiguration  implements Home {

    @Override
    public Configuration get(HttpServletRequest req, HttpServletResponse res) throws IOException {
        return Main.configuration;
    }
}
