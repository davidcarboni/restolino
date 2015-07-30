package com.github.davidcarboni.restolino.helpers;

import org.junit.Test;

import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class URLTest {

    String exampleSimple = "http://example.com";
    String exampleFull = "https://user:password@example.com:8080?parameter=value#fragment";

    @Test
    public void shouldParseSimple() throws URISyntaxException {

        // When
        URL url = new URL(exampleSimple);

        // Then
        assertEquals("http", url.protocol);
        assertEquals("example.com", url.host);
    }

    @Test
    public void shouldParseFull() throws URISyntaxException {

        // When
        URL url = new URL(exampleFull);

        // Then
        assertEquals("https", url.protocol);
        assertEquals("user:password", url.userInfo);
        assertEquals("example.com", url.host);
        assertEquals(8080, url.port);
        assertEquals(1, url.query.size());
        assertEquals("value", url.query.get("parameter"));
        assertEquals("fragment", url.fragment);
    }

}
