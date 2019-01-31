package com.github.davidcarboni.restolino.framework;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@FunctionalInterface
public interface PostFilter {

    void filter(HttpServletRequest req, HttpServletResponse res);
}
