package com.github.davidcarboni.restolino.jetty;

import com.github.davidcarboni.restolino.framework.Priority;
import com.github.davidcarboni.restolino.framework.Startup;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reflections.Reflections;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class MainHandlerTest {

    @Mock
    private Reflections reflections;

    private Set<Class<? extends Startup>> startUpInstances;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        this.startUpInstances = new HashSet<Class<? extends Startup>>() {{
            add(StartUp2.class);
            add(StartUp1.class);
            add(StartUp4.class);
            add(StartUp3.class);
        }};
    }

    /**
     * Test verifies that the {@link StartUp2} instances are loaded in the order specified by a {@link Priority}
     * annotation.
     */
    @Test
    public void testGetStartUpsOrdered() throws Exception {
        when(reflections.getSubTypesOf(Startup.class))
                .thenReturn(startUpInstances);

        List<Startup> result = MainHandler.getStartUpsOrdered(reflections);

        assertThat(result, is(notNullValue()));
        assertThat(result.size(), equalTo(startUpInstances.size()));
        assertTrue(result.get(0) instanceof StartUp1);
        assertTrue(result.get(1) instanceof StartUp2);
        assertTrue(result.get(2) instanceof StartUp3);
        assertTrue(result.get(3) instanceof StartUp4);
    }

    @Priority(1)
    public static class StartUp1 implements Startup {

        @Override
        public void init() {}
    }

    @Priority(2)
    public static class StartUp2 implements Startup {

        @Override
        public void init() {}
    }

    @Priority(3)
    public static class StartUp3 implements Startup {

        @Override
        public void init() {}
    }

    // No priority - should be last.
    public static class StartUp4 implements Startup {

        @Override
        public void init() {}
    }
}
