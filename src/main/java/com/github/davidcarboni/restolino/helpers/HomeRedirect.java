package com.github.davidcarboni.restolino.helpers;

import com.github.davidcarboni.restolino.framework.Home;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * Extend this class and add the {@link Home} interface to your subclass. This
 * class provides the functionality needed to send a redirect in response to
 * <code>GET /</code> - typically to a static index page.
 *
 * @author david
 */
public abstract class HomeRedirect implements Home {

    private String path;

    public HomeRedirect(URL url) {
        this.path = url.toString();
    }

    public HomeRedirect(String path) {
        this.path = path;
    }

    public HomeRedirect(URI uri) {
        this.path = uri.toString();
    }

    /**
     * Extending this class and implementing {@link Home} will make use of this
     * method.
     */
    @Override
    public Object get(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.sendRedirect(path);
        return null;
    }

}
