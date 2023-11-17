/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.TestCase
 */
package org.jose4j.jwe;

import junit.framework.TestCase;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.keys.AesKey;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.InvalidAlgorithmException;
import org.jose4j.lang.JoseException;

public class ZipTest
extends TestCase {
    public void testJweRoundTripWithAndWithoutZip() throws JoseException {
        JsonWebEncryption jwe = new JsonWebEncryption();
        String plaintext = "This should compress pretty good, it should, yes it should pretty good it should pretty good it should it should it should it should pretty good it should pretty good it should pretty good it should pretty good it should pretty good it should pretty good it should pretty good.";
        jwe.setPlaintext(plaintext);
        AesKey key = new AesKey(ByteUtil.randomBytes(32));
        jwe.setKey(key);
        jwe.enableDefaultCompression();
        jwe.setAlgorithmHeaderValue("dir");
        jwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");
        String csWithZip = jwe.getCompactSerialization();
        System.out.println(csWithZip);
        jwe = new JsonWebEncryption();
        jwe.setPlaintext(plaintext);
        jwe.setKey(key);
        jwe.setAlgorithmHeaderValue("dir");
        jwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");
        String csWithOutZip = jwe.getCompactSerialization();
        System.out.println(csWithOutZip);
        ZipTest.assertTrue((csWithZip.length() < csWithOutZip.length() ? 1 : 0) != 0);
        JsonWebEncryption decryptingJwe = new JsonWebEncryption();
        decryptingJwe.setKey(key);
        decryptingJwe.setCompactSerialization(csWithZip);
        String plaintextString = decryptingJwe.getPlaintextString();
        ZipTest.assertEquals((String)plaintext, (String)plaintextString);
        ZipTest.assertEquals((String)"DEF", (String)decryptingJwe.getCompressionAlgorithmHeaderParameter());
        decryptingJwe = new JsonWebEncryption();
        decryptingJwe.setKey(key);
        decryptingJwe.setCompactSerialization(csWithOutZip);
        plaintextString = decryptingJwe.getPlaintextString();
        ZipTest.assertEquals((String)plaintext, (String)plaintextString);
    }

    public void testJweBadZipValueProduce() throws JoseException {
        JsonWebEncryption jwe = new JsonWebEncryption();
        String plaintext = "This should compress pretty good, it should, yes it should pretty good it should it should";
        jwe.setPlaintext(plaintext);
        AesKey key = new AesKey(new byte[32]);
        jwe.setKey(key);
        jwe.setCompressionAlgorithmHeaderParameter("bad");
        jwe.setAlgorithmHeaderValue("dir");
        jwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");
        try {
            String cs = jwe.getCompactSerialization();
            ZipTest.fail((String)("Should fail with invalid zip header value: " + cs));
        }
        catch (InvalidAlgorithmException e) {
            ZipTest.assertTrue((boolean)e.getMessage().contains("zip"));
        }
    }

    public void testJwBadZipValueConsume() throws JoseException {
        String cs = "eyJ6aXAiOiJiYWQiLCJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0..ZZZ0nR5f80ikJtaPot4RpQ.BlDAYKzn9oLH1fhZcR60ZKye7UHslg7s0h7s1ecNZ5A1Df1pq2pBWUwdRKjJRxJAEFbDFoXTFYjV-cLCCE2Uxw.zasDvsZ3U4YkTDgIUchjiA";
        JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk("{\"kty\":\"oct\",\"k\":\"q1qm8z2sLFt_CPqwpLuGm-fX6ZKQKnukPHpoJOeykCw\"}");
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setKey(jsonWebKey.getKey());
        jwe.setCompactSerialization(cs);
        try {
            String plaintextString = jwe.getPlaintextString();
            ZipTest.fail((String)("Should fail with invalid zip header value but gave: " + plaintextString));
        }
        catch (InvalidAlgorithmException e) {
            ZipTest.assertTrue((boolean)e.getMessage().contains("zip"));
        }
    }
}

