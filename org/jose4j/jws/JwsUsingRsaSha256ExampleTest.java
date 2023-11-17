/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.TestCase
 */
package org.jose4j.jws;

import java.util.Arrays;
import java.util.Map;
import junit.framework.TestCase;
import org.jose4j.base64url.Base64Url;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.ExampleRsaKeyFromJws;
import org.jose4j.lang.JoseException;

public class JwsUsingRsaSha256ExampleTest
extends TestCase {
    String JWS = "eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.cC4hiUPoj9Eetdgtv3hF80EGrhuB__dzERat0XF9g2VtQgr9PJbu3XOiZj5RZmh7AAuHIm4Bh-0Qc_lF5YKt_O8W2Fp5jujGbds9uJdbF9CUAr7t1dnZcAcQjbKBYNX4BAynRFdiuB--f_nZLgrnbyTyWzO75vRK5h6xBArLIARNPvkSjtQBMHlb1L07Qe7K0GarZRmB_eSN9383LcOLn6_dO--xi12jzDwusC-eOkHWEsqtFZESc6BfI7noOPqvhJ1phCnvWh6IeYI2w9QOYEUipUTI8np6LbgGY9Fs98rqVt5AXLIhWkWywlVmtVrBp0igcN_IoypGlUPQGe77Rw";

    public void testVerifyExample() throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setCompactSerialization(this.JWS);
        jws.setKey(ExampleRsaKeyFromJws.PUBLIC_KEY);
        JwsUsingRsaSha256ExampleTest.assertTrue((String)"signature should validate", (boolean)jws.verifySignature());
    }

    public void testSignExample() throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload("{\"iss\":\"joe\",\r\n \"exp\":1300819380,\r\n \"http://example.com/is_root\":true}");
        jws.getHeaders().setFullHeaderAsJsonString("{\"alg\":\"RS256\"}");
        jws.setKey(ExampleRsaKeyFromJws.PRIVATE_KEY);
        String compactSerialization = jws.getCompactSerialization();
        JwsUsingRsaSha256ExampleTest.assertEquals((String)"example jws value doesn't match calculated compact serialization", (String)this.JWS, (String)compactSerialization);
    }

    public void testKey11to12() throws Exception {
        String jwkJson = "     {\"kty\":\"RSA\",\n      \"n\":\"ofgWCuLjybRlzo0tZWJjNiuSfb4p4fAkd_wWJcyQoTbji9k0l8W26mPddx\n           HmfHQp-Vaw-4qPCJrcS2mJPMEzP1Pt0Bm4d4QlL-yRT-SFd2lZS-pCgNMs\n           D1W_YpRPEwOWvG6b32690r2jZ47soMZo9wGzjb_7OMg0LOL-bSf63kpaSH\n           SXndS5z5rexMdbBYUsLA9e-KXBdQOS-UTo7WTBEMa2R2CapHg665xsmtdV\n           MTBQY4uDZlxvb3qCo5ZwKh9kG4LT6_I5IhlJH7aGhyxXFvUK-DWNmoudF8\n           NAco9_h9iaGNj8q2ethFkMLs91kzk2PAcDTW9gb54h4FRWyuXpoQ\",\n      \"e\":\"AQAB\",\n      \"d\":\"Eq5xpGnNCivDflJsRQBXHx1hdR1k6Ulwe2JZD50LpXyWPEAeP88vLNO97I\n           jlA7_GQ5sLKMgvfTeXZx9SE-7YwVol2NXOoAJe46sui395IW_GO-pWJ1O0\n           BkTGoVEn2bKVRUCgu-GjBVaYLU6f3l9kJfFNS3E0QbVdxzubSu3Mkqzjkn\n           439X0M_V51gfpRLI9JYanrC4D4qAdGcopV_0ZHHzQlBjudU2QvXt4ehNYT\n           CBr6XCLQUShb1juUO1ZdiYoFaFQT5Tw8bGUl_x_jTj3ccPDVZFD9pIuhLh\n           BOneufuBiB4cS98l2SR_RQyGWSeWjnczT0QU91p1DhOVRuOopznQ\"\n     }";
        Map<String, Object> parsed = JsonUtil.parseJson(jwkJson);
        JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk(parsed);
        JwsUsingRsaSha256ExampleTest.assertTrue((boolean)jsonWebKey.getKey().equals(ExampleRsaKeyFromJws.PUBLIC_KEY));
        String d = (String)parsed.get("d");
        Base64Url base64Url = new Base64Url();
        byte[] privateExp = base64Url.base64UrlDecode(d);
        JwsUsingRsaSha256ExampleTest.assertTrue((boolean)Arrays.equals(ExampleRsaKeyFromJws.D_SIGNED_BYTES, privateExp));
    }
}

