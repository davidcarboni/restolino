package com.github.davidcarboni.restolino.helpers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PathTest {

	@Mock
	HttpServletRequest request;

	@Test
	public void shouldGeFirstSegment() {

		// Given
		when(request.getPathInfo()).thenReturn("/first/second/third");

		// When
		Path path = new Path(request);

		// Then
		assertEquals("first", path.firstSegment());
	}

	@Test
	public void shouldGeLastSegment() {

		// Given
		when(request.getPathInfo()).thenReturn("/first/second/third");

		// When
		Path path = new Path(request);

		// Then
		assertEquals("third", path.lastSegment());
	}

	@Test
	public void shouldGeSegments() {

		// Given
		when(request.getPathInfo()).thenReturn("/first/second/third");

		// When
		Path path = new Path(request);
		List<String> segments = path.segments();

		// Then
		assertEquals(3, segments.size());
		assertEquals("first", segments.get(0));
		assertEquals("second", segments.get(1));
		assertEquals("third", segments.get(2));
	}

	@Test
	public void shouldHandleEmptyPath() {

		// Given
		when(request.getPathInfo()).thenReturn("/");

		// When
		Path path = new Path(request);
		List<String> segments = path.segments();

		// Then
		assertEquals(0, segments.size());
	}

}
