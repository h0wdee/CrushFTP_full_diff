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

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.base64url.Base64Url;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeyTest;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.keys.ExampleEcKeysFromJws;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Test;

public class EllipticCurveJsonWebKeyTest {
    @Test
    public void testParseExampleWithPrivate256() throws JoseException {
        String jwkJson = "{\"kty\":\"EC\",\n \"crv\":\"P-256\",\n \"x\":\"f83OJ3D2xF1Bg8vub9tLe1gHMzV76e8Tus9uPHvRVEU\",\n \"y\":\"x_FEzRu9m36HLN_tue659LNpXW6pCyStikYjKIWI5a0\",\n \"d\":\"jpsQnnGQmL-YBIffH1136cspYG6-0iY7X1fCE9-E9LI\"\n}";
        JsonWebKey jwk = JsonWebKey.Factory.newJwk(jwkJson);
        PublicJsonWebKey pubJwk = (PublicJsonWebKey)jwk;
        Assert.assertEquals((Object)ExampleEcKeysFromJws.PRIVATE_256, (Object)pubJwk.getPrivateKey());
        Assert.assertEquals((Object)ExampleEcKeysFromJws.PUBLIC_256, (Object)pubJwk.getPublicKey());
        Assert.assertEquals((Object)"P-256", (Object)((EllipticCurveJsonWebKey)jwk).getCurveName());
        String jsonOut = jwk.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);
        Assert.assertFalse((boolean)jsonOut.contains("\"d\""));
    }

    @Test
    public void testFromKeyWithPrivate256() throws JoseException {
        PublicJsonWebKey jwk = PublicJsonWebKey.Factory.newPublicJwk(ExampleEcKeysFromJws.PUBLIC_256);
        Assert.assertEquals((Object)"P-256", (Object)((EllipticCurveJsonWebKey)jwk).getCurveName());
        String jsonNoPrivateKey = jwk.toJson();
        jwk.setPrivateKey(ExampleEcKeysFromJws.PRIVATE_256);
        String d = "jpsQnnGQmL-YBIffH1136cspYG6-0iY7X1fCE9-E9LI";
        Assert.assertFalse((boolean)jwk.toJson().contains(d));
        Assert.assertEquals((Object)jsonNoPrivateKey, (Object)jwk.toJson());
        Assert.assertFalse((boolean)jwk.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY).contains(d));
        Assert.assertFalse((boolean)jwk.toJson().contains(d));
        Assert.assertFalse((boolean)jwk.toJson(JsonWebKey.OutputControlLevel.INCLUDE_SYMMETRIC).contains(d));
        Assert.assertTrue((boolean)jwk.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE).contains(d));
    }

    @Test
    public void testParseExampleWithPrivate512() throws JoseException {
        String jwkJson = "{\"kty\":\"EC\",\n \"crv\":\"P-521\",\n \"x\":\"AekpBQ8ST8a8VcfVOTNl353vSrDCLLJXmPk06wTjxrrjcBpXp5EOnYG_\n      NjFZ6OvLFV1jSfS9tsz4qUxcWceqwQGk\",\n \"y\":\"ADSmRA43Z1DSNx_RvcLI87cdL07l6jQyyBXMoxVg_l2Th-x3S1WDhjDl\n      y79ajL4Kkd0AZMaZmh9ubmf63e3kyMj2\",\n \"d\":\"AY5pb7A0UFiB3RELSD64fTLOSV_jazdF7fLYyuTw8lOfRhWg6Y6rUrPA\n      xerEzgdRhajnu0ferB0d53vM9mE15j2C\"\n}";
        JsonWebKey jwk = JsonWebKey.Factory.newJwk(jwkJson);
        PublicJsonWebKey pubJwk = (PublicJsonWebKey)jwk;
        Assert.assertEquals((Object)ExampleEcKeysFromJws.PRIVATE_521, (Object)pubJwk.getPrivateKey());
        Assert.assertEquals((Object)ExampleEcKeysFromJws.PUBLIC_521, (Object)pubJwk.getPublicKey());
        Assert.assertEquals((Object)"P-521", (Object)((EllipticCurveJsonWebKey)jwk).getCurveName());
        String jsonOut = jwk.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);
        Assert.assertFalse((boolean)jsonOut.contains("\"d\""));
        JsonWebKeyTest.checkEncoding(pubJwk.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE), "x", "y", "d");
    }

    @Test
    public void testFromKeyWithPrivate512() throws JoseException {
        PublicJsonWebKey jwk = PublicJsonWebKey.Factory.newPublicJwk(ExampleEcKeysFromJws.PUBLIC_521);
        Assert.assertEquals((Object)"P-521", (Object)((EllipticCurveJsonWebKey)jwk).getCurveName());
        String jsonNoPrivateKey = jwk.toJson();
        jwk.setPrivateKey(ExampleEcKeysFromJws.PRIVATE_521);
        String d = "AY5pb7A0UFiB3RELSD64fTLOSV_jazdF7fLYyuTw8lOfRhWg6Y6rUrPAxerEzgdRhajnu0ferB0d53vM9mE15j2C";
        Assert.assertFalse((boolean)jwk.toJson().contains(d));
        Assert.assertEquals((Object)jsonNoPrivateKey, (Object)jwk.toJson());
        Assert.assertFalse((boolean)jwk.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY).contains(d));
        Assert.assertFalse((boolean)jwk.toJson().contains(d));
        Assert.assertFalse((boolean)jwk.toJson(JsonWebKey.OutputControlLevel.INCLUDE_SYMMETRIC).contains(d));
        Assert.assertTrue((boolean)jwk.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE).contains(d));
        System.out.println(jwk);
    }

    @Test
    public void testToJsonWithPublicKeyOnlyJWKAndIncludePrivateSettings() throws JoseException {
        PublicJsonWebKey jwk = PublicJsonWebKey.Factory.newPublicJwk(ExampleEcKeysFromJws.PUBLIC_521);
        String jsonNoPrivateKey = jwk.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);
        PublicJsonWebKey publicOnlyJWK = PublicJsonWebKey.Factory.newPublicJwk(jsonNoPrivateKey);
        Assert.assertThat((Object)jsonNoPrivateKey, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)publicOnlyJWK.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE))));
    }

    @Test
    public void testCryptoBinaryThread() throws Exception {
        String keySpec = "MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQBCCAc9n4N7ZOr_tTu_wAOmPKi4qTp5X3su6O3010hxmBYj9zI4u_0dm6UZa0LsjdfvcAET6vH3mEApvGKpDWrRsAA_nJhyQ20ca7Nn0Zvyiq54FfCAblGK7kuduFBTPkxv9eOjiaeGp7V_f3qV1kxS_Il2LY7Tc5l2GSlW_-SzYKxgek";
        Base64Url base64Url = new Base64Url();
        byte[] bytes = base64Url.base64UrlDecode(keySpec);
        PublicKey ecPubKey = KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(bytes));
        PublicJsonWebKey jwk = PublicJsonWebKey.Factory.newPublicJwk(ecPubKey);
        String jwkJson = jwk.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);
        Map<String, Object> parsed = JsonUtil.parseJson(jwkJson);
        String x = (String)parsed.get("x");
        Assert.assertThat((Object)"AQggHPZ-De2Tq_7U7v8ADpjyouKk6eV97Lujt9NdIcZgWI_cyOLv9HZulGWtC7I3X73ABE-rx95hAKbxiqQ1q0bA", (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)x)));
        String y = (String)parsed.get("y");
        Assert.assertThat((Object)"AP5yYckNtHGuzZ9Gb8oqueBXwgG5Riu5LnbhQUz5Mb_Xjo4mnhqe1f396ldZMUvyJdi2O03OZdhkpVv_ks2CsYHp", (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)y)));
        String noLeftZeroPaddingBytes = "{\"kty\":\"EC\",\"x\":\"AQggHPZ-De2Tq_7U7v8ADpjyouKk6eV97Lujt9NdIcZgWI_cyOLv9HZulGWtC7I3X73ABE-rx95hAKbxiqQ1q0bA\",\"y\":\"_nJhyQ20ca7Nn0Zvyiq54FfCAblGK7kuduFBTPkxv9eOjiaeGp7V_f3qV1kxS_Il2LY7Tc5l2GSlW_-SzYKxgek\",\"crv\":\"P-521\"}";
        String withLeftZeroPaddingBytes = "{\"kty\":\"EC\",\"x\":\"AQggHPZ-De2Tq_7U7v8ADpjyouKk6eV97Lujt9NdIcZgWI_cyOLv9HZulGWtC7I3X73ABE-rx95hAKbxiqQ1q0bA\",\"y\":\"AP5yYckNtHGuzZ9Gb8oqueBXwgG5Riu5LnbhQUz5Mb_Xjo4mnhqe1f396ldZMUvyJdi2O03OZdhkpVv_ks2CsYHp\",\"crv\":\"P-521\"}";
        PublicJsonWebKey jwkWithNoZeroLeftPaddingBytes = PublicJsonWebKey.Factory.newPublicJwk(noLeftZeroPaddingBytes);
        PublicJsonWebKey jwkWithZeroLeftPaddingBytes = PublicJsonWebKey.Factory.newPublicJwk(withLeftZeroPaddingBytes);
        Assert.assertEquals((Object)jwkWithNoZeroLeftPaddingBytes.getPublicKey(), (Object)jwkWithZeroLeftPaddingBytes.getPublicKey());
    }
}

