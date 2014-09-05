package com.github.davidcarboni.restolino.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class ParameterTest {

	@Test
	public void shouldParseLastSegmentOfPath() {

		// Given
		int id = 7;
		Path path = mock(Path.class);
		when(path.lastSegment()).thenReturn(String.valueOf(id));

		// When
		int result = Parameter.getId(path);

		// Then
		assertEquals(id, result);
	}

	@Test
	public void shouldNotParseLastSegmentOfPathIfString() {

		// Given
		Path path = mock(Path.class);
		when(path.lastSegment()).thenReturn("some string");

		// When
		int result = Parameter.getId(path);

		// Then
		assertEquals(-1, result);
	}

	@Test
	public void shouldReadIntParameter() {

		// Given
		int parameter = 23;
		String parameterValue = String.valueOf(parameter);

		// When
		int result = Parameter.toInt(parameterValue);

		// Then
		assertEquals(parameter, result);
	}

	@Test
	public void shouldHandleNonIntParameter() {

		// Given
		String parameterValue = "definitely not a number";

		// When
		int result = Parameter.toInt(parameterValue);

		// Then
		assertEquals(-1, result);
	}

	@Test
	public void shouldHandleNullIntParameter() {

		// Given
		String parameterValue = null;

		// When
		int result = Parameter.toInt(parameterValue);

		// Then
		assertEquals(-1, result);
	}

	@Test
	public void shouldReadIntegerParameter() {

		// Given
		Integer parameter = Integer.valueOf(23);
		String parameterValue = String.valueOf(parameter);

		// When
		Integer result = Parameter.toInteger(parameterValue);

		// Then
		assertEquals(parameter, result);
	}

	@Test
	public void shouldHandleNonIntegerParameter() {

		// Given
		String parameterValue = "definitely not a number";

		// When
		Integer result = Parameter.toInteger(parameterValue);

		// Then
		assertNull(result);
	}

	@Test
	public void shouldHandleNullIntegerParameter() {

		// Given
		String parameterValue = null;

		// When
		Integer result = Parameter.toInteger(parameterValue);

		// Then
		assertNull(result);
	}

}
