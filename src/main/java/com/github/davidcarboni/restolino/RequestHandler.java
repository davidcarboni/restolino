package com.github.davidcarboni.restolino;

import java.lang.reflect.Method;

class RequestHandler {

	Class<?> endpointClass;
	Method method;
	Class<?> requestMessageType;
	Class<?> responseMessageType;
}
