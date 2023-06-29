package org.parchmentmc.jam.identity;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.parchmentmc.feather.mapping.MappingDataBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class IdentityManager {
    final MappingDataBuilder loadedData = new MappingDataBuilder();
    private final AtomicInteger identityCounter = new AtomicInteger(0);

    private final BiMap<String, Integer> identityMap = HashBiMap.create(1_000);
    final Map<Integer, Object> identityToRetainedData = new HashMap<>();

    Integer assignIdentityToData(Object data, String key) {
        final Integer id = assignIdentity(key);
        identityToRetainedData.put(id, data);
        return id;
    }

    Integer getOrAssignIdentity(String key) {
        return identityMap.computeIfAbsent(key, k -> identityCounter.getAndIncrement());
    }

    Integer assignIdentity(String key) {
        final Integer id = identityCounter.getAndIncrement();
        identityMap.put(key, id);
        return id;
    }

    private static final String KEY_SEPARATOR = "//";

    static String classKey(String className) {
        return className;
    }

    static String fieldKey(String className, String fieldName, String fieldDesc) {
        return classKey(className) + KEY_SEPARATOR + fieldName + KEY_SEPARATOR + fieldDesc;
    }

    static String methodKey(String className, String methodName, String methodDesc) {
        return classKey(className) + KEY_SEPARATOR + methodName + KEY_SEPARATOR + methodDesc;
    }

    static String paramKey(String className, String methodName, String methodDesc, int paramIndex) {
        return methodKey(className, methodName, methodDesc) + KEY_SEPARATOR + paramIndex;
    }
}
