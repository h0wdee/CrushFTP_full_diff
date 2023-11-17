/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hamcrest.CoreMatchers
 *  org.hamcrest.Matcher
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.jws;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.ExampleEcKeysFromJws;
import org.junit.Assert;
import org.junit.Test;

public class DetachedContentTest {
    @Test
    public void testSomeDetachedContent() throws Exception {
        String payload = "Issue #48";
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(payload);
        jws.setKey(ExampleEcKeysFromJws.PRIVATE_256);
        jws.setAlgorithmHeaderValue("ES256");
        String detachedContentCompactSerialization = jws.getDetachedContentCompactSerialization();
        String encodedPayload = jws.getEncodedPayload();
        String compactSerialization = jws.getCompactSerialization();
        jws = new JsonWebSignature();
        jws.setCompactSerialization(detachedContentCompactSerialization);
        jws.setEncodedPayload(encodedPayload);
        jws.setKey(ExampleEcKeysFromJws.PUBLIC_256);
        Assert.assertTrue((boolean)jws.verifySignature());
        Assert.assertThat((Object)payload, (Matcher)CoreMatchers.equalTo((Object)jws.getPayload()));
        jws = new JsonWebSignature();
        jws.setCompactSerialization(compactSerialization);
        jws.setKey(ExampleEcKeysFromJws.PUBLIC_256);
        Assert.assertTrue((boolean)jws.verifySignature());
        Assert.assertThat((Object)payload, (Matcher)CoreMatchers.equalTo((Object)jws.getPayload()));
        jws = new JsonWebSignature();
        jws.setCompactSerialization(detachedContentCompactSerialization);
        jws.setKey(ExampleEcKeysFromJws.PUBLIC_256);
        Assert.assertFalse((boolean)jws.verifySignature());
    }
}

