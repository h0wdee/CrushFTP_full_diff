/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.jws;

import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Test;

public class GetPayloadTest {
    @Test
    public void testGetPayloadVerifiedAndUnverifiedAndSysPropOverride() throws JoseException {
        String payload;
        JsonWebSignature jws;
        JsonWebKey jwk = JsonWebKey.Factory.newJwk("{\"kty\":\"oct\",\"k\":\"Y7T0ygpIvYvz9kSVRod2tcGhekjiQh4t_AF7GE-v0o8\"}");
        String cs = "eyJhbGciOiJIUzI1NiJ9.VUExNTgyIHRvIFNGTyBmb3IgYSBOQVBQUyBGMkYgd29ya3Nob3AgaW4gUGFsbyBBbHRv.YjnCNkxrv86F6GufxddTYS_4URo3kmLKrREquZSEKDo";
        String propertyName = "org.jose4j.jws.getPayload-skip-verify";
        try {
            System.setProperty(propertyName, "true");
            jws = new JsonWebSignature();
            jws.setCompactSerialization(cs);
            payload = jws.getPayload();
            Assert.assertNotNull((Object)payload);
        }
        finally {
            System.clearProperty(propertyName);
        }
        try {
            jws = new JsonWebSignature();
            jws.setCompactSerialization(cs);
            payload = jws.getPayload();
            Assert.fail((String)("getPayload should have failed with no key set but did return: " + payload));
        }
        catch (JoseException jws2) {
            // empty catch block
        }
        try {
            System.setProperty(propertyName, "true");
            jws = new JsonWebSignature();
            jws.setCompactSerialization(cs);
            jws.setKey(new HmacKey(new byte[32]));
            payload = jws.getPayload();
            Assert.assertNotNull((Object)payload);
        }
        finally {
            System.clearProperty(propertyName);
        }
        try {
            jws = new JsonWebSignature();
            jws.setCompactSerialization(cs);
            jws.setKey(new HmacKey(new byte[32]));
            payload = jws.getPayload();
            Assert.fail((String)("getPayload should have failed with wrong key set but did return: " + payload));
        }
        catch (JoseException jws3) {
            // empty catch block
        }
        jws = new JsonWebSignature();
        jws.setCompactSerialization(cs);
        payload = jws.getUnverifiedPayload();
        Assert.assertNotNull((Object)payload);
        jws.setKey(jwk.getKey());
    }
}

