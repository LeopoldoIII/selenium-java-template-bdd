package com.template.context;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread-safe scenario context (inspired by Python Behave 'context').
 * Used to share state (such as auth tokens, response objects, or generated IDs)
 * between step definitions in a clean and decoupled way.
 */
public final class ScenarioContext {

    private static final ThreadLocal<Map<String, Object>> contextHolder = ThreadLocal.withInitial(HashMap::new);

    private ScenarioContext() {
    }

    public static void set(String key, Object value) {
        contextHolder.get().put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) contextHolder.get().get(key);
    }

    public static boolean contains(String key) {
        return contextHolder.get().containsKey(key);
    }

    public static void clear() {
        contextHolder.get().clear();
    }
}
