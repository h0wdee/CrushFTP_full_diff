/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.json;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jose4j.json.internal.json_simple.JSONValue;
import org.jose4j.json.internal.json_simple.parser.ContainerFactory;
import org.jose4j.json.internal.json_simple.parser.JSONParser;
import org.jose4j.json.internal.json_simple.parser.ParseException;
import org.jose4j.lang.JoseException;

public class JsonUtil {
    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(){

        @Override
        public List createArrayContainer() {
            return new ArrayList();
        }

        @Override
        public Map createObjectContainer() {
            return new DupeKeyDisallowingLinkedHashMap();
        }
    };

    public static Map<String, Object> parseJson(String jsonString) throws JoseException {
        try {
            JSONParser parser = new JSONParser();
            return (DupeKeyDisallowingLinkedHashMap)parser.parse(jsonString, CONTAINER_FACTORY);
        }
        catch (IllegalArgumentException | ParseException e) {
            throw new JoseException("Parsing error: " + e, e);
        }
    }

    public static String toJson(Map<String, ?> map) {
        return JSONValue.toJSONString(map);
    }

    public static void writeJson(Map<String, ?> map, Writer w) throws IOException {
        JSONValue.writeJSONString(map, w);
    }

    static class DupeKeyDisallowingLinkedHashMap
    extends LinkedHashMap<String, Object> {
        DupeKeyDisallowingLinkedHashMap() {
        }

        @Override
        public Object put(String key, Object value) {
            if (this.containsKey(key)) {
                throw new IllegalArgumentException("An entry for '" + key + "' already exists. Names must be unique.");
            }
            return super.put(key, value);
        }
    }
}

