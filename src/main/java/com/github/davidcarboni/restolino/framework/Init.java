package com.github.davidcarboni.restolino.framework;

/**
 * Implement this interface if you have things that need to run on startup.
 * <p>
 * When running with reloading, classes implementing this will be called on
 * every reload. If they weren't, you'd have to restart the server, which would
 * slow you down.
 * <p>
 * You'll need to provide a visible no-arg constructor. Implementations will be
 * instantiated and the {@link #init()} method will be called. That's it.
 * <p>
 * If that behaviour doesn't work for you, consider implementing something that
 * flags whether this is the first time or a reload. Bear in mind that each
 * invocation will be in a new classloader, so approaches like static fields
 * won't work because they will effectively reset.
 * <p>
 * If you need to throw a checked exception, wrap it in an unchecked exception.
 * Anything thrown will be logged to the console, but won't halt the
 * application, on the basis that in development you might need to go through
 * quite a number of iterations.
 * 
 * @author david
 *
 */
public interface Init {
	void init();
}
