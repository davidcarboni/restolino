package com.github.davidcarboni.restolino.json;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for {@link Serialiser}.
 */
public class SerialiserTest {

    public static class SamIAm {
        long id;
    }

    /**
     * Tests thread-safe json serialisation and deserialisation.
     */
    @Test
    public void testSerialise() throws IOException, InterruptedException, ExecutionException {

        // Given
        // A list of tasks and scores
        List<Future<Exception>> tasks = new ArrayList<>();
        final AtomicInteger same = new AtomicInteger();
        final AtomicInteger different = new AtomicInteger();

        // When
        // We submit lots of tasks to a pool
        final AtomicLong number = new AtomicLong(System.currentTimeMillis());
        ExecutorService pool = Executors.newCachedThreadPool();
        final Path path = Files.createTempFile("json", "serialise");
        for (int i = 0; i < 1000; i++) {
            Callable<Exception> task = new Callable<Exception>() {
                @Override
                public Exception call() throws Exception {
                    Exception result = null;
                    try {
                        SamIAm samIAm = new SamIAm();
                        long id = number.getAndIncrement();
                        samIAm.id = id;
                        Serialiser.serialise(path, samIAm);
                        samIAm = Serialiser.deserialise(path, SamIAm.class);
                        if (samIAm.id == id) {
                            same.incrementAndGet();
                        } else {
                            different.incrementAndGet();
                        }
                    } catch (Exception e) {
                        result = e;
                    }

                    return result;
                }
            };
            tasks.add(pool.submit(task));
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        // Then
        // All tasks should complete without exceptions
        for (Future<Exception> task : tasks) {
            assertNull(task.get());
        }
        // Sometimes another thread will have serialised before deserialisation:
        assertNotEquals(same.get(), different.get());
        assertNotEquals(0, same.get());
        assertNotEquals(0, different.get());
        //System.out.println(same.get());
        //System.out.println(different.get());
    }

}