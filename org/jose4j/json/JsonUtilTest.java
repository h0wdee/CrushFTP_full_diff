/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.json;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.jose4j.json.JsonUtil;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Test;

public class JsonUtilTest {
    @Test
    public void needsEsc() throws Exception {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("char array", new char[]{'a', '\\', '\"'});
        map.put("some object", new Object());
        map.put("nested", Collections.singletonMap("chars", "\"meh".toCharArray()));
        map.put("nested also", Collections.singletonMap("obj", new Random()));
        String s = JsonUtil.toJson(map);
        System.out.println(s);
        Map<String, Object> parsedMap = JsonUtil.parseJson(s);
    }

    @Test
    public void testParseJson1() throws JoseException {
        String basic = "{\"key\":\"value\"}";
        Map<String, Object> map = JsonUtil.parseJson(basic);
        Assert.assertEquals((long)1L, (long)map.size());
        Assert.assertEquals((Object)"value", (Object)map.get("key"));
    }

    @Test
    public void testParseJsonDisallowDupes() {
        String basic = "{\"key\":\"value\",\"key\":\"value2\"}";
        try {
            Map<String, Object> map = JsonUtil.parseJson(basic);
            Assert.fail((String)("parsing of " + basic + " should fail because the same member name occurs multiple times but returned: " + map));
        }
        catch (JoseException joseException) {
            // empty catch block
        }
    }

    @Test
    public void testParseJsonDisallowDupesMoreComplex() {
        String json = "{\n  \"keys\": [\n    {\n      \"kty\": \"EC\",\n      \"kid\": \"20b05\",\n      \"use\": \"sig\",\n      \"x\": \"baLYE[omitted]DLSIor7\",\n      \"y\": \"Xh2Q4[omitted]AB3GKQ1\",\n      \"crv\": \"P-384\"\n    },\n    {\n      \"kty\": \"EC\",\n      \"kid\": \"20b04\",\n      \"use\": \"sig\",\n      \"x\": \"-Pfjrs_rpNIu4XPMHOhW4DvhZ9sdEKgT8zINkLM6Yvg\",\n      \"y\": \"1FXTX9JGWH4kG0KxUIQDqOIxC2R8w5sLHHYr6sjcTK4\",\n      \"y\": \"1234567890abcdefghijklmnopqrstuvwxyzABCDEFG\",\n      \"crv\": \"P-256\"\n    }\n  ]\n}";
        try {
            Map<String, Object> map = JsonUtil.parseJson(json);
            Assert.fail((String)("parsing of " + json + " should fail because the same member name occurs multiple times but returned: " + map));
        }
        catch (JoseException joseException) {
            // empty catch block
        }
    }

    @Test
    public void testBiggerThanLong() throws Exception {
        String json = "{\"key\":\"value\",\"number\":90210, \"big number\":99990193716474719874987981237498321343555513331108571735145}";
        Map<String, Object> parsed = JsonUtil.parseJson(json);
        Assert.assertEquals((long)3L, (long)parsed.size());
    }
}

