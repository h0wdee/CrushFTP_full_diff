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

import java.security.Key;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.base64url.Base64Url;
import org.jose4j.jca.ProviderContext;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jws.HmacUsingShaAlgorithm;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.StringUtil;
import org.junit.Assert;
import org.junit.Test;

public class JwsUnencodedPayloadOptionTest {
    @Test
    public void testExamplesFromDraftEvenWithoutDirectSupportForTheHeader() throws Exception {
        String jwkJson = "{  \"kty\":\"oct\",  \"k\":\"AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75      aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow\"}";
        JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk(jwkJson);
        Key key = jsonWebKey.getKey();
        String payload = "$.02";
        String encodedPayload = Base64Url.encode(payload, "US-ASCII");
        Assert.assertThat((Object)encodedPayload, (Matcher)CoreMatchers.equalTo((Object)"JC4wMg"));
        String jwscsWithB64 = "eyJhbGciOiJIUzI1NiJ9.JC4wMg.5mvfOroL-g7HyqJoozehmsaqmvTYGEq5jTI1gVvoEoQ";
        JsonWebSignature jws = new JsonWebSignature();
        jws.setCompactSerialization(jwscsWithB64);
        jws.setKey(key);
        Assert.assertThat((Object)jws.getPayload(), (Matcher)CoreMatchers.equalTo((Object)payload));
        Assert.assertTrue((boolean)jws.verifySignature());
        jws = new JsonWebSignature();
        jws.setPayload(payload);
        jws.setKey(key);
        jws.setAlgorithmHeaderValue("HS256");
        Assert.assertThat((Object)jws.getCompactSerialization(), (Matcher)CoreMatchers.equalTo((Object)jwscsWithB64));
        String jwscsWithoutB64andDetachedPaylod = "eyJhbGciOiJIUzI1NiIsImI2NCI6ZmFsc2UsImNyaXQiOlsiYjY0Il19..A5dxf2s96_n5FLueVuW1Z_vh161FwXZC4YLPff6dmDY";
        jws = new JsonWebSignature();
        jws.setCompactSerialization(jwscsWithoutB64andDetachedPaylod);
        Assert.assertThat((Object)jws.getHeaders().getFullHeaderAsJsonString(), (Matcher)CoreMatchers.equalTo((Object)"{\"alg\":\"HS256\",\"b64\":false,\"crit\":[\"b64\"]}"));
        HmacUsingShaAlgorithm.HmacSha256 hmacSha256 = new HmacUsingShaAlgorithm.HmacSha256();
        String signingInputString = String.valueOf(jws.getHeaders().getEncodedHeader()) + "." + payload;
        byte[] signatureBytes = Base64Url.decode(jws.getEncodedSignature());
        byte[] securedInputBytes = StringUtil.getBytesAscii(signingInputString);
        ProviderContext providerContext = new ProviderContext();
        boolean okay = hmacSha256.verifySignature(signatureBytes, key, securedInputBytes, providerContext);
        Assert.assertTrue((boolean)okay);
        byte[] signed = hmacSha256.sign(key, securedInputBytes, providerContext);
        Assert.assertThat((Object)Base64Url.encode(signed), (Matcher)CoreMatchers.equalTo((Object)jws.getEncodedSignature()));
    }
}

