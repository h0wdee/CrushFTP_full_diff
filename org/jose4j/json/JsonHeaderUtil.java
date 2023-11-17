/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.json;

import java.util.Map;
import org.jose4j.json.JsonUtil;
import org.jose4j.lang.JoseException;

public class JsonHeaderUtil {
    public static Map<String, Object> parseJson(String jsonString) throws JoseException {
        return JsonUtil.parseJson(jsonString);
    }

    public static String toJson(Map<String, ?> map) {
        return JsonUtil.toJson(map);
    }
}

