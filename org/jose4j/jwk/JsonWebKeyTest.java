/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.jwk;

import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.KeyOperations;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.keys.ExampleEcKeysFromJws;
import org.jose4j.keys.ExampleRsaKeyFromJws;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Test;

public class JsonWebKeyTest {
    @Test
    public void testFactoryWithRsaPublicKey() throws JoseException {
        JsonWebKey jwk = JsonWebKey.Factory.newJwk(ExampleRsaKeyFromJws.PUBLIC_KEY);
        this.assertIsRsa(jwk);
    }

    private void assertIsRsa(JsonWebKey jwk) {
        Assert.assertTrue((boolean)(jwk instanceof RsaJsonWebKey));
        Assert.assertTrue((boolean)(jwk.getKey() instanceof RSAPublicKey));
        Assert.assertEquals((Object)"RSA", (Object)jwk.getKeyType());
    }

    @Test
    public void testFactoryWithEcPublicKey() throws JoseException {
        JsonWebKey jwk = JsonWebKey.Factory.newJwk(ExampleEcKeysFromJws.PUBLIC_256);
        this.assertIsEllipticCurve(jwk);
    }

    private void assertIsEllipticCurve(JsonWebKey jwk) {
        Assert.assertTrue((boolean)(jwk.getKey() instanceof ECPublicKey));
        Assert.assertTrue((boolean)(jwk instanceof EllipticCurveJsonWebKey));
        Assert.assertEquals((Object)"EC", (Object)jwk.getKeyType());
    }

    @Test
    public void testEcSingleJwkToAndFromJson() throws JoseException {
        String jwkJson = "       {\"kty\":\"EC\",\n        \"crv\":\"P-256\",\n        \"x\":\"MKBCTNIcKUSDii11ySs3526iDZ8AiTo7Tu6KPAqv7D4\",\n        \"y\":\"4Etl6SRW2YiLUrN5vfvVHuhp7x8PxltmWWlbbM4IFyM\",\n        \"use\":\"enc\",\n        \"kid\":\"1\"}";
        JsonWebKey jwk = JsonWebKey.Factory.newJwk(jwkJson);
        this.assertIsEllipticCurve(jwk);
        String jsonOut = jwk.toJson();
        JsonWebKey jwk2 = JsonWebKey.Factory.newJwk(jsonOut);
        this.assertIsEllipticCurve(jwk2);
        JsonWebKeyTest.checkEncoding(jsonOut, "x", "y");
    }

    @Test
    public void testRsaSingleJwkToAndFromJson() throws JoseException {
        String jwkJson = "       {\"kty\":\"RSA\",\n        \"n\": \"0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx   4cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMs   tn64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2   QvzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbI   SD08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqb   w0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw\",\n        \"e\":\"AQAB\",\n        \"alg\":\"RS256\"}";
        JsonWebKey jwk = JsonWebKey.Factory.newJwk(jwkJson);
        this.assertIsRsa(jwk);
        String jsonOut = jwk.toJson();
        JsonWebKey jwk2 = JsonWebKey.Factory.newJwk(jsonOut);
        this.assertIsRsa(jwk2);
        JsonWebKeyTest.checkEncoding(jwk.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY), "n");
    }

    static void checkEncoding(String jwkJson, String ... members) throws JoseException {
        Map<String, Object> parsed = JsonUtil.parseJson(jwkJson);
        String[] stringArray = members;
        int n = members.length;
        int n2 = 0;
        while (n2 < n) {
            String name = stringArray[n2];
            String value = (String)parsed.get(name);
            Assert.assertEquals((long)-1L, (long)value.indexOf(13));
            Assert.assertEquals((long)-1L, (long)value.indexOf(10));
            Assert.assertEquals((long)-1L, (long)value.indexOf(61));
            Assert.assertEquals((long)-1L, (long)value.indexOf(43));
            Assert.assertEquals((long)-1L, (long)value.indexOf(47));
            ++n2;
        }
    }

    @Test
    public void testKeyOps() throws Exception {
        String json = "{\"kty\":\"oct\",\"k\":\"Hdd5Uqtga_B4UilmahWJR8juxF_zw1_xaWeUGAvbg9c\"}";
        JsonWebKey jwk = JsonWebKey.Factory.newJwk(json);
        Assert.assertNull(jwk.getKeyOps());
        List<String> keyOps = Arrays.asList(KeyOperations.DECRYPT, KeyOperations.DERIVE_BITS, KeyOperations.DERIVE_KEY, KeyOperations.ENCRYPT, KeyOperations.SIGN, KeyOperations.VERIFY, KeyOperations.UNWRAP_KEY, KeyOperations.WRAP_KEY);
        jwk.setKeyOps(keyOps);
        json = jwk.toJson(JsonWebKey.OutputControlLevel.INCLUDE_SYMMETRIC);
        Assert.assertTrue((boolean)json.contains("\"key_ops\""));
        jwk = JsonWebKey.Factory.newJwk(json);
        List<String> keyOpsFromParsed = jwk.getKeyOps();
        Assert.assertTrue((boolean)Arrays.equals(keyOps.toArray(), keyOpsFromParsed.toArray()));
        json = "{\"kty\":\"oct\",\"key_ops\":[\"decrypt\",\"encrypt\"],\"k\":\"add14qyge_v4sscm2hWJR8juxF_____cpW8U3ahcp__\"}";
        jwk = JsonWebKey.Factory.newJwk(json);
        Assert.assertEquals((long)2L, (long)jwk.getKeyOps().size());
        Assert.assertTrue((boolean)jwk.getKeyOps().contains(KeyOperations.ENCRYPT));
        Assert.assertTrue((boolean)jwk.getKeyOps().contains(KeyOperations.DECRYPT));
    }

    @Test(expected=JoseException.class)
    public void howHandleWrongType1() throws Exception {
        JsonWebKey.Factory.newJwk("{\"kty\":1}");
    }

    @Test(expected=JoseException.class)
    public void howHandleWrongType2() throws Exception {
        String jwkJson = "       {\"kty\":\"RSA\",\n        \"n\": 8929747471717373711113313454114,\n        \"e\":\"AQAB\",\n        \"alg\":\"RS256\"}";
        JsonWebKey.Factory.newJwk(jwkJson);
    }

    @Test(expected=JoseException.class)
    public void howHandleWrongType3() throws Exception {
        String jwkJson = "       {\"kty\":\"EC\",\n        \"crv\":\"P-256\",\n        \"x\":\"MKBCTNIcKUSDii11ySs3526iDZ8AiTo7Tu6KPAqv7D4\",\n        \"y\":\"4Etl6SRW2YiLUrN5vfvVHuhp7x8PxltmWWlbbM4IFyM\",\n        \"use\":true,\n        \"kid\":\"1\"}";
        JsonWebKey.Factory.newJwk(jwkJson);
    }

    @Test(expected=JoseException.class)
    public void howHandleWrongType4() throws Exception {
        String jwkJson = "       {\"kty\":\"EC\",\n        \"crv\":\"P-256\",\n        \"x\":[\"MKBCTNIcKUSDii11ySs3526iDZ8AiTo7Tu6KPAqv7D4\"],\n        \"y\":\"4Etl6SRW2YiLUrN5vfvVHuhp7x8PxltmWWlbbM4IFyM\",\n        \"kid\":\"1s\"}";
        JsonWebKey.Factory.newJwk(jwkJson);
    }
}

