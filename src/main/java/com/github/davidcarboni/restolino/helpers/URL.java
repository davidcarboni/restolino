package com.github.davidcarboni.restolino.helpers;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * A helper class for building and manipulating URLs. This helps work around the
 * {@link URI} escaping of query strings and provides some of the functionality
 * of the Apache URIBuilder without requiring HTTP Client as a dependency.
 *
 * @author david
 */
public class URL {

    public String protocol;
    public String userInfo;
    public String host;
    public int port;
    public String path;
    public QueryString query;
    public String fragment;

    /**
     * Constructs an instance from a String representation of a URL.
     *
     * @param url The ULR to parse.
     * @throws URISyntaxException If an error occurs in parsing the URL.
     */
    public URL(String url) throws URISyntaxException {
        URI uri = new URI(url);
        protocol = uri.getScheme();
        userInfo = uri.getUserInfo();
        host = uri.getHost();
        port = uri.getPort();
        path = uri.getPath();
        query = new QueryString(uri);
        fragment = uri.getFragment();
    }

    /**
     * Convenience constructor aimed at dealing with the return type of
     * {@link HttpServletRequest#getRequestURL()}.
     * <p>NB remember that you might need to check for headers such as
     * "X-Forwarded-For" if you're behind a web server or proxy.</p>
     *
     * @param url The ULR to parse.
     * @throws URISyntaxException If an error occurs in parsing the URL.
     */
    public URL(StringBuilder url) throws URISyntaxException {
        this(url.toString());
    }

    /**
     * Default constructor.
     */
    public URL() {
        // No initialisation.
    }

    /**
     * Builds a String representation of this URL. This method works around
     * double-escaping when passing the result of
     * {@link QueryString#toQueryString()} to the {@link URI} constructor.
     *
     * @return A string representation of this URL.
     * @throws URISyntaxException If an error occurs in parsing the URL.
     */
    private String build() throws URISyntaxException {

        URI uri = new URI(protocol, userInfo, host, port, path, null, null);
        StringBuilder url = new StringBuilder(uri.toString());
        if (query != null && query.size() > 0) {
            url.append("?");
            url.append(query.toQueryString());
        }
        if (StringUtils.isNotBlank(fragment)) {
            url.append("#");
            url.append(fragment);
        }

        return url.toString();
    }

    /**
     * @return A {@link java.net.URL} for this class.
     * @throws MalformedURLException If an error occurs in parsing the URL.
     */
    public java.net.URL toURL() throws MalformedURLException {
        try {
            return new java.net.URL(build());
        } catch (URISyntaxException e) {
            throw new MalformedURLException(e.getMessage());
        }
    }

    /**
     * This method works around double-escaping when passing the result of
     * {@link QueryString#toQueryString()} to the {@link URI} constructor.
     */
    @Override
    public String toString() {
        try {
            return build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Error building URL", e);
        }
    }
}
