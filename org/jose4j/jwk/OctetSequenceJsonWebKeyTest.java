/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hamcrest.CoreMatchers
 *  org.hamcrest.Matcher
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.jwk;

import java.util.Arrays;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.OctetSequenceJsonWebKey;
import org.jose4j.keys.AesKey;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Test;

public class OctetSequenceJsonWebKeyTest {
    @Test
    public void testExampleFromJws() throws Exception {
        String base64UrlKey = "AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow";
        String jwkJson = "{\"kty\":\"oct\",\n \"k\":\"" + base64UrlKey + "\"\n" + "}";
        JsonWebKey parsedKey = JsonWebKey.Factory.newJwk(jwkJson);
        Assert.assertEquals(OctetSequenceJsonWebKey.class, parsedKey.getClass());
        Assert.assertTrue((boolean)parsedKey.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE).contains(base64UrlKey));
        Assert.assertTrue((boolean)parsedKey.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE).contains("\"k\""));
        Assert.assertTrue((boolean)parsedKey.toJson(JsonWebKey.OutputControlLevel.INCLUDE_SYMMETRIC).contains(base64UrlKey));
        Assert.assertTrue((boolean)parsedKey.toJson(JsonWebKey.OutputControlLevel.INCLUDE_SYMMETRIC).contains("\"k\""));
        Assert.assertFalse((boolean)parsedKey.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY).contains(base64UrlKey));
        Assert.assertFalse((boolean)parsedKey.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY).contains("\"k\""));
        int[] keyInts = new int[]{3, 35, 53, 75, 43, 15, 165, 188, 131, 126, 6, 101, 119, 123, 166, 143, 90, 179, 40, 230, 240, 84, 201, 40, 169, 15, 132, 178, 210, 80, 46, 191, 211, 251, 90, 146, 210, 6, 71, 239, 150, 138, 180, 195, 119, 98, 61, 34, 61, 46, 33, 114, 5, 46, 79, 8, 192, 205, 154, 245, 103, 208, 128, 163};
        byte[] keyBytes = ByteUtil.convertUnsignedToSignedTwosComp(keyInts);
        Assert.assertTrue((boolean)Arrays.equals(keyBytes, parsedKey.getKey().getEncoded()));
        JsonWebKey jwk = JsonWebKey.Factory.newJwk(new HmacKey(keyBytes));
        Assert.assertEquals((Object)"oct", (Object)jwk.getKeyType());
        Assert.assertTrue((boolean)jwk.toJson().contains(base64UrlKey));
        Assert.assertTrue((boolean)jwk.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE).contains(base64UrlKey));
        Assert.assertTrue((boolean)jwk.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE).contains("\"k\""));
        Assert.assertTrue((boolean)jwk.toJson(JsonWebKey.OutputControlLevel.INCLUDE_SYMMETRIC).contains(base64UrlKey));
        Assert.assertTrue((boolean)jwk.toJson(JsonWebKey.OutputControlLevel.INCLUDE_SYMMETRIC).contains("\"k\""));
        Assert.assertFalse((boolean)jwk.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY).contains(base64UrlKey));
        Assert.assertFalse((boolean)jwk.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY).contains("\"k\""));
    }

    @Test
    public void testLeadingAndTrailingZeros() throws JoseException {
        byte[] byArray = new byte[16];
        byArray[2] = 111;
        byArray[3] = 16;
        byArray[4] = 51;
        byArray[5] = 98;
        byArray[6] = -4;
        byArray[8] = -72;
        byArray[9] = 9;
        byArray[10] = -111;
        byArray[11] = 60;
        byArray[12] = 41;
        byArray[13] = -66;
        byArray[14] = 94;
        byte[] rawInputBytes = byArray;
        JsonWebKey jwk = JsonWebKey.Factory.newJwk(new AesKey(rawInputBytes));
        String json = jwk.toJson(JsonWebKey.OutputControlLevel.INCLUDE_SYMMETRIC);
        JsonWebKey jwkFromJson = JsonWebKey.Factory.newJwk(json);
        byte[] encoded = jwkFromJson.getKey().getEncoded();
        Assert.assertThat((Object)rawInputBytes.length, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)encoded.length)));
        Assert.assertArrayEquals((byte[])rawInputBytes, (byte[])encoded);
    }
}

