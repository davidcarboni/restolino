package com.github.davidcarboni.restolino.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by david on 08/09/2015.
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
        // A list of tasks
        List<Future<Exception>> tasks = new ArrayList<>();

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
                        //System.out.println(Thread.currentThread().getName() + " - " + samIAm.test);
                        //if (id != samIAm.id) {
                        //    System.out.println("Yarp! " + id + " : " + samIAm.id);
                        //} else {
                        //    System.out.println("Narp.");
                        //}
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
            Assert.assertNull(task.get());
        }
    }

}