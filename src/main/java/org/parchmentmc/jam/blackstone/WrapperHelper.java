package org.parchmentmc.jam.blackstone;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Map;

final class WrapperHelper {
    public static <T, C extends Collection<T>> @Nullable C emptyToNull(C collection) {
        if (collection.isEmpty()) return null;
        return collection;
    }

    public static <K, V, M extends Map<K, V>> @Nullable M emptyToNull(M map) {
        if (map.isEmpty()) return null;
        return map;
    }
}
