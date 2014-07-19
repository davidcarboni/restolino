package com.github.davidcarboni.restolino.servlet;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * This {@link javax.servlet.Filter} enables the app to run without a servlet
 * context path.
 * 
 * @author David Carboni
 * 
 */
public class Filter implements javax.servlet.Filter {

	@Override
	public void init(FilterConfig filterConfig) {
		// No implementation.
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		// Determine if this is a static content request:
		HttpServletRequest req = (HttpServletRequest) request;
		String path = req.getRequestURI().substring(
				req.getContextPath().length());
		String extension = FilenameUtils.getExtension(path);
		boolean isStaticContent = StringUtils.isNotBlank(extension);

		if (isStaticContent) {
			// Static content goes to default servlet:
			chain.doFilter(request, response);
		} else {
			// Page requests to Webulizor:
			request.getRequestDispatcher("/app" + path).forward(request,
					response);
		}
	}

	@Override
	public void destroy() {
		// No implementation.
	}

}
