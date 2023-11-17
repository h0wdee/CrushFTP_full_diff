/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.lang;

import java.util.List;
import java.util.Map;
import org.jose4j.jwt.IntDate;
import org.jose4j.lang.JoseException;

public class JsonHelp {
    public static String getString(Map<String, Object> map, String name) {
        Object object = map.get(name);
        return (String)object;
    }

    public static String getStringChecked(Map<String, Object> map, String name) throws JoseException {
        Object o = map.get(name);
        try {
            return (String)o;
        }
        catch (ClassCastException e) {
            throw new JoseException("'" + name + "' parameter was " + JsonHelp.jsonTypeName(o) + " type but is required to be a String.");
        }
    }

    public static List<String> getStringArray(Map<String, Object> map, String name) {
        Object object = map.get(name);
        return (List)object;
    }

    public static IntDate getIntDate(Map<String, Object> map, String name) {
        long l = JsonHelp.getLong(map, name);
        return IntDate.fromSeconds(l);
    }

    public static Long getLong(Map<String, ?> map, String name) {
        Object o = map.get(name);
        return o != null ? Long.valueOf(((Number)o).longValue()) : null;
    }

    public static String jsonTypeName(Object value) {
        String jsonTypeName = value instanceof Number ? "Number" : (value instanceof Boolean ? "Boolean" : (value instanceof List ? "Array" : (value instanceof Map ? "Object" : (value instanceof String ? "String" : "unknown"))));
        return jsonTypeName;
    }
}

