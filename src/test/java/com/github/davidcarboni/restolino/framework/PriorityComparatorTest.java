package com.github.davidcarboni.restolino.framework;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class PriorityComparatorTest {

    @Test
    public void testCompare() {
        PriorityComparator comparator = new PriorityComparator(4);
        // Same priorty
        assertThat(comparator.compare(new A(), new A()), equalTo(0));

        // Same priority order by class name
        assertThat(comparator.compare(new C(), new B()), equalTo(1));


        // arg 1 has higher priority
        assertThat(comparator.compare(new A(), new B()), equalTo(-1));
        assertThat(comparator.compare(new A(), new C()), equalTo(-1));
        assertThat(comparator.compare(new A(), new D()), equalTo(-1));

        // arg 2 has higher priority
        assertThat(comparator.compare(new B(), new A()), equalTo(1));
        assertThat(comparator.compare(new C(), new A()), equalTo(1));
        assertThat(comparator.compare(new D(), new A()), equalTo(1));
    }

    @Test
    public void testSortList() {
        List<PreFilter> preFilters = new ArrayList<PreFilter>() {{
            add(new B());
            add(new D());
            add(new C());
            add(new A());
        }};

        Collections.sort(preFilters, new PriorityComparator(4));
        assertThat(preFilters.get(0).getClass().getSimpleName(), equalTo("A"));
        assertThat(preFilters.get(1).getClass().getSimpleName(), equalTo("B"));
        assertThat(preFilters.get(2).getClass().getSimpleName(), equalTo("C"));
        assertThat(preFilters.get(3).getClass().getSimpleName(), equalTo("D"));
    }

    @Priority(1)
    static class A implements PreFilter {

        @Override
        public boolean filter(HttpServletRequest req, HttpServletResponse res) {
            return false;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }
    }

    @Priority(2)
    static class B implements PreFilter {

        @Override
        public boolean filter(HttpServletRequest req, HttpServletResponse res) {
            return false;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }
    }

    // Same priority as a "B"
    @Priority(2)
    static class C implements PreFilter {

        @Override
        public boolean filter(HttpServletRequest req, HttpServletResponse res) {
            return false;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }
    }

    // Has no priority set
    static class D implements PreFilter {

        @Override
        public boolean filter(HttpServletRequest req, HttpServletResponse res) {
            return false;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }
    }

}
