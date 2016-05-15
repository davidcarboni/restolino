package com.github.davidcarboni.restolino.framework;

import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.GET;
import java.lang.annotation.Annotation;

/**
 * Http method enumeration.
 */
public enum HttpMethod {

    GET(javax.ws.rs.GET.class), POST(javax.ws.rs.POST.class), PUT(javax.ws.rs.PUT.class), DELETE(javax.ws.rs.DELETE.class);

    private Class<?> annotationClass;

    HttpMethod(Class<?> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public static HttpMethod method(Annotation annotation) {
        HttpMethod result = null;
        for (HttpMethod httpMethod : values()) {
            if (httpMethod.annotationClass.isAssignableFrom(annotation.getClass())) {
                result = httpMethod;
                break;
            }
        }
        return result;
    }

    public static HttpMethod method(String method) {
        HttpMethod result = null;
        for (HttpMethod httpMethod : values()) {
            if (StringUtils.equals(httpMethod.name(), StringUtils.upperCase(method))) {
                result = httpMethod;
                break;
            }
        }
        return result;
    }
}
