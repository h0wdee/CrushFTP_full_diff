/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.junit.Assert
 */
package org.jose4j.jws;

import java.security.Key;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.CompactSerializer;
import org.jose4j.lang.ExceptionHelp;
import org.jose4j.lang.InvalidKeyException;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwsTestSupport {
    private static final Logger log = LoggerFactory.getLogger(JwsTestSupport.class);

    static void testBasicRoundTrip(String payload, String jwsAlgo, Key signingKey1, Key verificationKey1, Key signingKey2, Key verificationKey2) throws JoseException {
        JsonWebSignature jwsWithKey1 = new JsonWebSignature();
        jwsWithKey1.setPayload(payload);
        jwsWithKey1.setAlgorithmHeaderValue(jwsAlgo);
        jwsWithKey1.setKey(signingKey1);
        String serializationWithKey1 = jwsWithKey1.getCompactSerialization();
        log.debug("{} {}", (Object)jwsAlgo, (Object)serializationWithKey1);
        JsonWebSignature jwsWithKey2 = new JsonWebSignature();
        jwsWithKey2.setKey(signingKey2);
        jwsWithKey2.setAlgorithmHeaderValue(jwsAlgo);
        jwsWithKey2.setPayload(payload);
        String serializationWithKey2 = jwsWithKey2.getCompactSerialization();
        JwsTestSupport.validateBasicStructure(serializationWithKey1);
        JwsTestSupport.validateBasicStructure(serializationWithKey2);
        Assert.assertFalse((boolean)serializationWithKey1.equals(serializationWithKey2));
        JsonWebSignature jws = new JsonWebSignature();
        jws.setCompactSerialization(serializationWithKey1);
        jws.setKey(verificationKey1);
        Assert.assertTrue((boolean)jws.verifySignature());
        Assert.assertEquals((Object)payload, (Object)jws.getPayload());
        jws = new JsonWebSignature();
        jws.setCompactSerialization(serializationWithKey2);
        jws.setKey(verificationKey1);
        Assert.assertFalse((boolean)jws.verifySignature());
        jws = new JsonWebSignature();
        jws.setCompactSerialization(serializationWithKey2);
        jws.setKey(verificationKey2);
        Assert.assertTrue((boolean)jws.verifySignature());
        Assert.assertEquals((Object)payload, (Object)jws.getPayload());
        jws = new JsonWebSignature();
        jws.setCompactSerialization(serializationWithKey1);
        jws.setKey(verificationKey2);
        Assert.assertFalse((boolean)jws.verifySignature());
        Assert.assertEquals((Object)payload, (Object)jwsWithKey1.getUnverifiedPayload());
        Assert.assertEquals((Object)payload, (Object)jwsWithKey2.getUnverifiedPayload());
    }

    static void validateBasicStructure(String compactSerialization) throws JoseException {
        Assert.assertNotNull((Object)compactSerialization);
        Assert.assertEquals((Object)compactSerialization.trim(), (Object)compactSerialization);
        String[] parts = CompactSerializer.deserialize(compactSerialization);
        Assert.assertEquals((long)3L, (long)parts.length);
    }

    static void testBadKeyOnSign(String alg, Key key) {
        try {
            JsonWebSignature jwsWithKey1 = new JsonWebSignature();
            jwsWithKey1.setPayload("whatever");
            jwsWithKey1.setAlgorithmHeaderValue(alg);
            jwsWithKey1.setKey(key);
            String cs = jwsWithKey1.getCompactSerialization();
            Assert.fail((String)("Should have failed with some kind of invalid key message but got " + cs));
        }
        catch (JoseException e) {
            log.debug("Expected something like this: {}", (Object)ExceptionHelp.toStringWithCauses(e));
        }
    }

    static void testBadKeyOnVerify(String compactSerialization, Key key) throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setCompactSerialization(compactSerialization);
        jws.setKey(key);
        try {
            jws.verifySignature();
            Assert.fail((String)"Should have failed with some kind of invalid key message");
        }
        catch (InvalidKeyException e) {
            log.debug("Expected something like this: {}", (Object)ExceptionHelp.toStringWithCauses(e));
        }
    }
}

