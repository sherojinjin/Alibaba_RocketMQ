package com.ndpmedia.rocketmq.cockpit.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * util for collection transition.
 */
public class CollectionUtil {
    /**
     * change the collection from list to set
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> Set<T> changeListToSet(List<T> list) {
        Object[] ns = list.toArray();

        Set<T> resultSet = new HashSet(Arrays.asList(ns));
        ;

        return resultSet;
    }
}
