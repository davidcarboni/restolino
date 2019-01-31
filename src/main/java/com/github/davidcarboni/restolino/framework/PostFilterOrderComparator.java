package com.github.davidcarboni.restolino.framework;

import java.util.Comparator;

public class PostFilterOrderComparator implements Comparator<PostFilter> {

    private int defaultPriority;

    public PostFilterOrderComparator(int defaultPriority) {
        this.defaultPriority = defaultPriority;
    }

    @Override
    public int compare(PostFilter o1, PostFilter o2) {
        int priority = getFilterPriority(o1).compareTo(getFilterPriority(o2));
        if (priority != 0) {
            return priority;
        }

        // if the priority values are equal then sort by the Filter class name.
        return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
    }

    private Integer getFilterPriority(PostFilter filter) {
        Class<? extends PostFilter> filterClass = filter.getClass();

        if (!filterClass.isAnnotationPresent(Order.class)) {
            return defaultPriority;
        }

        int declaredPriority = filterClass.getAnnotation(Order.class).priority();
        if (declaredPriority > -1) {
            return declaredPriority;
        }

        return defaultPriority;
    }
}
