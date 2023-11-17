/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.ThreadContext
 */
package org.apache.log4j;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import org.apache.logging.log4j.ThreadContext;

public final class MDC {
    private static ThreadLocal<Map<String, Object>> localMap = new InheritableThreadLocal<Map<String, Object>>(){

        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap<String, Object>();
        }

        @Override
        protected Map<String, Object> childValue(Map<String, Object> parentValue) {
            return parentValue == null ? new HashMap<String, Object>() : new HashMap<String, Object>(parentValue);
        }
    };

    private MDC() {
    }

    public static void put(String key, String value) {
        localMap.get().put(key, value);
        ThreadContext.put((String)key, (String)value);
    }

    public static void put(String key, Object value) {
        localMap.get().put(key, value);
        ThreadContext.put((String)key, (String)value.toString());
    }

    public static Object get(String key) {
        return localMap.get().get(key);
    }

    public static void remove(String key) {
        localMap.get().remove(key);
        ThreadContext.remove((String)key);
    }

    public static void clear() {
        localMap.get().clear();
        ThreadContext.clearMap();
    }

    public static Hashtable<String, Object> getContext() {
        return new Hashtable<String, Object>(localMap.get());
    }
}

