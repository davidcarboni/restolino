package com.github.davidcarboni.restolino.framework;

import java.util.Comparator;

/**
 * {@link Comparator} impl for classes that use the {@link Priority} annotation. Objects are order by {@link Priority#value()}
 * if they implement it or by Class name if they don't / if two priorities are equal.
 */
public class PriorityComparator implements Comparator<Object> {

    private int defaultPriority;

    public PriorityComparator(int defaultPriority) {
        this.defaultPriority = defaultPriority;
    }

    @Override
    public int compare(Object o1, Object o2) {
        int priority = getFilterPriority(o1).compareTo(getFilterPriority(o2));
        if (priority != 0) {
            return priority;
        }

        // if the priority values are equal then sort by the Filter class name.
        return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
    }

    private Integer getFilterPriority(Object obj) {
        Class objClass = obj.getClass();

        if (!objClass.isAnnotationPresent(Priority.class)) {
            return defaultPriority;
        }

        Priority priority = (Priority) objClass.getAnnotation(Priority.class);
        int declaredPriority = priority.value();
        if (declaredPriority > -1) {
            return declaredPriority;
        }

        return defaultPriority;
    }
}
