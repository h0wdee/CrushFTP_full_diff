/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.TestCase
 *  org.junit.Assert
 */
package org.jose4j.jws;

import java.security.Key;
import junit.framework.TestCase;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jws.JwsTestSupport;
import org.jose4j.keys.FakeHsmNonExtractableSecretKeySpec;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.InvalidKeyException;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.StringUtil;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HmacShaTest
extends TestCase {
    private static final Logger log = LoggerFactory.getLogger(HmacShaTest.class);
    private static final FakeHsmNonExtractableSecretKeySpec FAKE_HSM_NON_EXTRACTABLE_SECRET_KEY_SPEC = new FakeHsmNonExtractableSecretKeySpec(StringUtil.getBytesUtf8("12345678901234567890123456789012"), "HmacSHA256");
    private static final String JWS_CS_WITH_FAKE_NON_EX_KEY = "eyJhbGciOiJIUzI1NiJ9.aHR0cHM6Ly9iaXRidWNrZXQub3JnL2JfYy9qb3NlNGovaXNzdWVzLzM5Lw.Bp7boj9FhfSdAX7Sq2GkjiS3RAVZrA9rcH5xxCQ1fRo";
    Key KEY1 = new HmacKey(new byte[]{-41, -1, 60, 1, 1, 45, -92, -114, 8, -1, -60, 7, 54, -16, 16, 14, -20, -85, 56, 103, 4, 10, -56, 120, 37, -48, 6, 9, 110, -96, 27, -4, 41, -99, 60, 91, 49, 70, -99, -14, -108, -81, 60, 37, 104, -116, 106, 104, -2, -95, 56, 103, 64, 10, -56, 120, 37, -48, 6, 9, 110, -96, 27, -4});
    Key KEY2 = new HmacKey(new byte[]{-67, 34, -45, 50, 13, 84, -79, 124, -16, -44, 26, -39, 4, -1, 26, 9, 38, 78, -107, 39, -81, 75, -18, 38, 96, 34, 13, 79, -73, 62, -60, 52, 71, -99, 60, 91, 124, 70, -9, -14, -108, -104, 6, 7, 104, -116, 6, 64, -2, -95, 56, 103, 64, 10, -56, 120, 37, -48, 6, 9, 110, -92, 27, -4});

    public void testHmacSha256A() throws JoseException {
        this.testBasicRoundTrip("some content that is the payload", "HS256");
    }

    public void testHmacSha256B() throws JoseException {
        this.testBasicRoundTrip("{\"iss\":\"https://jwt-idp.example.com\",\n    \"prn\":\"mailto:mike@example.com\",\n    \"aud\":\"https://jwt-rp.example.net\",\n    \"iat\":1300815780,\n    \"exp\":1300819380,\n    \"http://claims.example.com/member\":true}", "HS256");
    }

    public void testHmacSha384A() throws JoseException {
        this.testBasicRoundTrip("Looking good, Billy Ray!", "HS384");
    }

    public void testHmacSha348B() throws JoseException {
        this.testBasicRoundTrip("{\"meh\":\"meh\"}", "HS384");
    }

    public void testHmacSha512A() throws JoseException {
        this.testBasicRoundTrip("Feeling good, Louis!", "HS512");
    }

    public void testHmacSha512B() throws JoseException {
        this.testBasicRoundTrip("{\"meh\":\"mehvalue\"}", "HS512");
    }

    void testBasicRoundTrip(String payload, String jwsAlgo) throws JoseException {
        JwsTestSupport.testBasicRoundTrip(payload, jwsAlgo, this.KEY1, this.KEY1, this.KEY2, this.KEY2);
    }

    public void testMinKeySize256ForSign() {
        JwsTestSupport.testBadKeyOnSign("HS256", new HmacKey(new byte[1]));
    }

    public void testMinKeySize256ForSign2() {
        JwsTestSupport.testBadKeyOnSign("HS256", new HmacKey(new byte[31]));
    }

    public void testMinKeySize384ForSign() {
        JwsTestSupport.testBadKeyOnSign("HS384", new HmacKey(new byte[47]));
    }

    public void testMinKeySize512ForSign() {
        JwsTestSupport.testBadKeyOnSign("HS512", new HmacKey(new byte[63]));
    }

    public void testMinKeySize256ForVerify() throws JoseException {
        String compactSerialization = "eyJhbGciOiJIUzI1NiJ9.c29tZSBjb250ZW50IHRoYXQgaXMgdGhlIHBheWxvYWQ.qGO7O7W2ECVl6uO7lfsXDgEF-EUEti0i-a_AimulIRA";
        HmacKey key = new HmacKey(new byte[31]);
        JwsTestSupport.testBadKeyOnVerify(compactSerialization, key);
    }

    public void testMinKeySize384ForVerify() throws JoseException {
        String compactSerialization = "eyJhbGciOiJIUzM4NCJ9.eyJtZWgiOiJtZWgifQ.fptKQJmGN3fBP_FiQzdAGdmx-Q5iWjQvJrLfdmFnebxbQuzOmzejBrzYh4MyS01a";
        HmacKey key = new HmacKey(new byte[47]);
        JwsTestSupport.testBadKeyOnVerify(compactSerialization, key);
    }

    public void testMinKeySize512ForVerify() throws JoseException {
        String compactSerialization = "eyJhbGciOiJIUzUxMiJ9.eyJtZWgiOiJtZWh2YWx1ZSJ9.NeB669dYkPmqgLqgd_sVqwIfCvb4XN-K67gpMJR93wfw_DylpxB1ell2opHM-E5P9jNKE2GYxTxwcI68Z2CTxw";
        HmacKey key = new HmacKey(new byte[63]);
        JwsTestSupport.testBadKeyOnVerify(compactSerialization, key);
    }

    public void testVailidateKeySwitch() throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload("whatever");
        jws.setAlgorithmHeaderValue("HS256");
        byte[] byArray = new byte[9];
        byArray[0] = 1;
        byArray[1] = 2;
        byArray[2] = 5;
        byArray[3] = -9;
        byArray[4] = 99;
        byArray[5] = -99;
        byArray[7] = 40;
        byArray[8] = 21;
        jws.setKey(new HmacKey(byArray));
        jws.setDoKeyValidation(false);
        String cs = jws.getCompactSerialization();
        HmacShaTest.assertNotNull((Object)cs);
        try {
            jws.setDoKeyValidation(true);
            jws.getCompactSerialization();
            Assert.fail((String)("Should have failed with some kind of invalid key message but got " + cs));
        }
        catch (InvalidKeyException e) {
            log.debug("Expected something like this: {}", (Object)e.toString());
        }
    }

    public void testNpeWithNonExtractableKeyDataCreate() throws Exception {
        FakeHsmNonExtractableSecretKeySpec key = FAKE_HSM_NON_EXTRACTABLE_SECRET_KEY_SPEC;
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload("https://bitbucket.org/b_c/jose4j/issues/39/");
        jws.setAlgorithmHeaderValue("HS256");
        jws.setKey(key);
        String jwscs = jws.getCompactSerialization();
        HmacShaTest.assertEquals((String)JWS_CS_WITH_FAKE_NON_EX_KEY, (String)jwscs);
    }

    public void testNpeWithNonExtractableKeyDataVerify() throws Exception {
        FakeHsmNonExtractableSecretKeySpec key = FAKE_HSM_NON_EXTRACTABLE_SECRET_KEY_SPEC;
        JsonWebSignature jws = new JsonWebSignature();
        jws.setCompactSerialization(JWS_CS_WITH_FAKE_NON_EX_KEY);
        jws.setKey(key);
        HmacShaTest.assertTrue((boolean)jws.verifySignature());
    }
}

