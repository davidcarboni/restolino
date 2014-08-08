package com.github.davidcarboni.restolino.servlet;

import java.io.File;
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
 * This {@link javax.servlet.Filter} enables the api to run without a servlet
 * context path.
 * 
 * @author David Carboni
 * 
 */
public class Filter implements javax.servlet.Filter {

	boolean reloadFiles;
	boolean reloadClasses;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

		String path = System.getProperty(StaticServlet.KEY_FILES);
		reloadFiles = StringUtils.isNotBlank(path);
		File directory;

		if (StringUtils.isNotBlank(path)) {

			// Get the canonical path
			// This enables us to check that requested files are in the web
			// root.
			try {
				directory = new File(path).getCanonicalFile();
			} catch (IOException e) {
				throw new ServletException("Error getting canonical file for: "
						+ path);
			}

			// Check that the directory is valid:
			if (!directory.exists())
				throw new ServletException("Directory does not exist: "
						+ directory);
			else if (!directory.isDirectory())
				throw new ServletException("Directory is not a directory: "
						+ directory);

			StaticServlet.directory = directory;
		}
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
			if (reloadFiles)
				// Page requests to the reloadable static servlet:
				request.getRequestDispatcher("/static" + path).forward(request,
						response);
			else
				// Delegate requests to the API:
				request.getRequestDispatcher("/api" + path).forward(request,
						response);
		}
	}

	@Override
	public void destroy() {
		// No implementation.
	}

}
