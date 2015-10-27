package com.github.davidcarboni.restolino.helpers;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.UrlEncoded;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class QueryStringTest {

    @Test
    public void shouldCreateBlank() {

        // When
        QueryString queryString = new QueryString();

        // Then
        assertEquals(0, queryString.size());
    }

    @Test
    public void shouldParseUri() {

        // Given
        URI uri = URI.create("http://newport.com/DuchyJerkShack?food=awesome&location=invisible");

        // When
        QueryString queryString = new QueryString(uri);

        // Then
        assertTrue(queryString.containsKey("food"));
        assertTrue(queryString.containsKey("location"));
        assertEquals(queryString.get("food"), "awesome");
        assertEquals(queryString.get("location"), "invisible");
    }

    @Test
    public void shouldEscapeOnPut() throws UnsupportedEncodingException {

        // Given
        String dodgyCharacters = " !@#$^(){}[]/\\?|;:";
        String extraDodgy = "+&=";
        String key = "Strange parameter name : " + dodgyCharacters;
        String value = "Crazy parameter value : " + dodgyCharacters + extraDodgy;

        // When
        QueryString queryString = new QueryString();
        queryString.put(key, value);
        String rendered = queryString.toQueryString();

        // Then
        String[] split = StringUtils.split(rendered, "=");
        assertFalse(StringUtils.containsAny(split[1], dodgyCharacters));
        assertEquals(2, split.length);
        String decodedKey = split[0];
        assertEquals(decodedKey, key);
        String decodedValue = URLDecoder.decode(split[1], StandardCharsets.UTF_8.name());
        // Jetty class *appears* to need Java 8
        //String decodedValue = UrlEncoded.decodeString(split[1], 0, split[1].length(), Charset.forName("UTF8"));
        assertEquals(decodedValue, value);
    }

    @Test
    public void shouldEscapeOnConstruct() throws UnsupportedEncodingException {

        // Given
        String dodgyCharacters = " !@#$^(){}[]/\\?|;:";
        String extraDodgy = "+&=";
        String key = "Strange parameter name : " + dodgyCharacters;
        String value = "Crazy parameter value : " + dodgyCharacters + extraDodgy;
        URI uri = URI.create("http://newport.com/Lilo?" + URLEncoder.encode(key, StandardCharsets.UTF_8.name()) + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
        // Jetty class *appears* to need Java 8
        //URI uri = URI.create("http://newport.com/Lilo?" + UrlEncoded.encodeString(key) + "=" + UrlEncoded.encodeString(value));

        // When
        QueryString queryString = new QueryString(uri);
        String rendered = queryString.toQueryString();

        // Then
        String[] split = StringUtils.split(rendered, "=");
        assertFalse(StringUtils.containsAny(split[1], dodgyCharacters));
        String decodedKey = split[0];
        assertEquals(decodedKey, key);


        String decodedValue = URLDecoder.decode(split[1], StandardCharsets.UTF_8.name());
        // Jetty class *appears* to need Java 8
        //String decodedValue = UrlEncoded.decodeString(split[1], 0, split[1].length(), Charset.forName("UTF8"));
        assertEquals(decodedValue, value);
    }

    @Test
    public void shouldReturnNullForToQueryStringIfNoParameters() {

        // Given
        QueryString queryString = new QueryString();

        // When
        String rendered = queryString.toQueryString();

        // Then
        assertNull(rendered);
    }

    @Test
    public void shouldReturnEmptyForToStringIfNoParameters() {

        // Given
        QueryString queryString = new QueryString();

        // When
        String rendered = queryString.toString();

        // Then
        assertNotNull(rendered);
        assertTrue(StringUtils.isEmpty(rendered));
    }
}
