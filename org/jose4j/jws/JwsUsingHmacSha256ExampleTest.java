/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.TestCase
 */
package org.jose4j.jws;

import junit.framework.TestCase;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

public class JwsUsingHmacSha256ExampleTest
extends TestCase {
    String JWS = "eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
    String PAYLOAD = "{\"iss\":\"joe\",\r\n \"exp\":1300819380,\r\n \"http://example.com/is_root\":true}";
    String JWK = "{\"kty\":\"oct\",\"k\":\"AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow\"}";

    public void testVerifyExample() throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setCompactSerialization(this.JWS);
        JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk(this.JWK);
        jws.setKey(jsonWebKey.getKey());
        JwsUsingHmacSha256ExampleTest.assertTrue((String)"signature (HMAC) should validate", (boolean)jws.verifySignature());
        JwsUsingHmacSha256ExampleTest.assertEquals((String)this.PAYLOAD, (String)jws.getPayload());
    }

    public void testSignExample() throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(this.PAYLOAD);
        JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk(this.JWK);
        jws.setKey(jsonWebKey.getKey());
        jws.getHeaders().setFullHeaderAsJsonString("{\"typ\":\"JWT\",\r\n \"alg\":\"HS256\"}");
        String compactSerialization = jws.getCompactSerialization();
        JwsUsingHmacSha256ExampleTest.assertEquals((String)"example jws value doesn't match calculated compact serialization", (String)this.JWS, (String)compactSerialization);
    }
}

