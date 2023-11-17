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

import java.util.List;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.base64url.Base64Url;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.JsonHelp;
import org.junit.Assert;
import org.junit.Test;

public class JwkGlitchesInDeploymentTest {
    @Test
    public void salesforce() throws JoseException {
        String jwks = "{\"keys\":[{\"kty\":\"RSA\",\"n\":\"AMCELStParLtaggkLtZh4enfxMsjpW6jAlfFjGnDsoWZ4NbG2hSWPtDyB-OisNboY2x4PeP69lBC2Hd9LxfMcFYhoQpqT7khoZMTaE-QjKCT0uiVvswaUe7Lh6gVJ2hnWehtrmGQ6cFmLP-EiQ7ls8VQa0KiDP2VYFKrrZ4kD5ozAF-TKs5wU5xt85u9vAZjc0u09oLc8bN4wIA7EgLtysadw-jQxhEYWgCfIzoMB75kCucRYvQHcO7L9pwh_sDPguXyyWJqRjkq0z9Ryzpavvk0TgL1i_YHDRHquGq68iGLsebMoOuqx0_FNlIW9T3V7e0XkGPMAZz9gQR9UB-68zme1G6hS20FELGRQFTHH5u4CTfCVi5XEiWXQts2mNMCOavD1jfjfxoACuuBSmUO6QdG0UOQEMfg91OLGBOBHIEr1fH1vOj2hdVV2hzBXKJuPIGdRsxP1dubj2_tMrntHL_ZXo6yCg70YieVIslD6Ya6OAMNmXA4v_K_K6n4JVoXJweGxkq4uJBAW_yHcL6isEQqsZTXUZ1NaKEHlAWlUcHW6Y9t2darWIweeVn9ijgiensDMnXauGxABuiiKj5rLE-_3sb8oFMrluqoZwlfoE2RMBSNAOnY7BzOYrX5MzWOOwXrgLRl47mkZ1WCBL0650o9y8e2H7wiIhqhaxust9QJ\",\"e\":\"AQAB\",\"alg\":\"RS256\",\"use\":\"sig\",\"kid\":\"188\"},{\"kty\":\"RSA\",\"n\":\"AKPBc9I142dEc-Srdk5sz9MVaJH_kOAM_jEIOYuTAsTTU0Imae1ZMAGXjNJifpig2wsz5vcLON7_HMXoiJFWUKqwKHJ52_dDAwp1Pu6A-zLzlOEm5obi81QslWTyAUauc9DoI3MC3g-LazqKIJCzrtJMrsszaBZK-9dpvxmdcYnPl9DJRSqt_tnCOFNpxLrofl3Mu21KgsdM0yRTzjioIRmBGWem4mdOFQvhEXFunAtfaRFpurwqmSRLCjwn2s1QKBymQLpDXdFyz0Hf1usQGhp6fHu2ubRR5-nVOopISPeGYnlaeliLVrEEw8CR_g-21aVURvpVi--JYHLkHRQLZXZv_5Oxb5U13aoi63dK2Lg50xYsFErFF1gSW5hlbBDspWVT0AC_iuxu6dwUWOF9urzoH5bncAjo-y-1hW3dCF84k5u-MXtimRirBaaAoySNM_w-TnuW6H7MK9Qnmn4Zfe7LhuzqCJ6G7e0AEJ5y3AVc1D8_035Tlw3OVInj6bQNG8XXfDFRDYg20xhjc-gws3y_fOkH3CSzwfGmWt5RTdJFjwZDnJWWoC-FqciJZQenrr2doX6bfCGNv25lDdOpbqOjexctIUkhTFr382g_PxX93M29Sr_m9MSOlIJOHeu0TTUm03ETNjLr8fnSYDsl6q3P4RPejjqnDI6xmSaF5wgD\",\"e\":\"AQAB\",\"alg\":\"RS256\",\"use\":\"sig\",\"kid\":\"190\"},{\"kty\":\"RSA\",\"n\":\"AJznEDrx1fK3PoXHz_0ZsTBo8lZa7ki3hV06I2HG4sWgB9-rHFHo42sLN9aK1I5mKgeYrBPZ7XbC-A57HT_zAydprWA9hSIfLQZCY4F4rY3XA3Ja8BCwMfOOsASJUhEvMEenM6XSWX0sIS__dhBqQx-s-5ShApaoQ5W7WfshShY_QUEcGhF1le7rqtt4MVzqshDdFl5d2ST4LKHQp5V0Z_cv6-QjVfVeML81xpSYU9zb_zf2eVzWSI2Zx3QrhP4rU-GtcRDRBHbOyY4OZkU5VRc2L-YkLQaO43WOaIDE4Cj5kYeoWLqi3pItwDgFH39QBmjfU2R-tFMcE8NN_g0CS-Qtkrgv7zOSiFsWcUJ4rm33oFAgV6SUgCWy7fM0hc7U3Ky0uPsIFB6NQPEwzWjtvPyrAVE1rK4njq9zXwp-GzzW-7fBvdFOtJVtBiIRHWt3zWJ1dFlqWVTtYwkTcvyWFLNxAqNBNWUCWQ-9g5ulI4rh-3kd2YDSkfbZSzXcmUqWVGTxKy61yfdHeV25iWL0V_a_d8-hkKjr-RUMtSYWrcHn8YSncoZAxB7KhCztFw6pw55oMZBBFPpR2ElRs_og5VGTlGE0wrcbDw5gSFzjLsKdFMnSaYTt-qkUGg8hIxzbGCi4-Slb4wx0vBsNRYWxb7KFKwR63uIS2PT2uZnmrVf5\",\"e\":\"AQAB\",\"alg\":\"RS256\",\"use\":\"sig\",\"kid\":\"192\"}]}";
        JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(jwks);
        List<JsonWebKey> jsonWebKeys = jsonWebKeySet.getJsonWebKeys();
        Assert.assertThat((Object)jsonWebKeys.size(), (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)3)));
        JsonWebKey jwk188 = jsonWebKeySet.findJsonWebKey("188", null, "sig", null);
        Map<String, Object> parsed188 = JsonUtil.parseJson(jwk188.toJson());
        String jose4jModulus188 = JsonHelp.getString(parsed188, "n");
        byte[] decodedJose4jModulus = Base64Url.decode(jose4jModulus188);
        Assert.assertThat((Object)decodedJose4jModulus[0], (Matcher)CoreMatchers.not((Matcher)CoreMatchers.equalTo((Object)0)));
    }

    @Test
    public void google() throws JoseException {
        String jwks = "{\n \"keys\": [\n  {\n   \"kty\": \"RSA\",\n   \"alg\": \"RS256\",\n   \"use\": \"sig\",\n   \"kid\": \"ce808d4fb2eabff22a608e0c7a14300cc04f2606\",\n   \"n\": \"vl9eiLnGMX7r0f7i7sSqCN5zpISYRtqrZA8JfcVSq3FrqZFoUNcMCDbSaWGzWWCTkvN3jQEkgYpCpwRAOMYM08IXm46UwxMWlcb8c47LGbdFWzyf3t_3FcqASMp6BuEnCCciifAcDeiqG4JYmkux-KUSWYjXGFOxgjL0xZ4M3O8=\",\n   \"e\": \"AQAB\"\n  },\n  {\n   \"kty\": \"RSA\",\n   \"alg\": \"RS256\",\n   \"use\": \"sig\",\n   \"kid\": \"ce3dde4df07fe0794fcff86642b4b11f8026f43f\",\n   \"n\": \"x_s89G0aZsHdL81sgDN8-zPi9oq-5rlP5j850QllJUMD4PBEEo9KnfoKC9WaSJ2_oOI3W8KOLk4i993J4IGzJFlrNKt2xNSL60iQ9nDwGMhIXnieGyXosKRXhepaySCBQysuW8OiVlDVEoFS2VHvC_6bt5QZaitl7AYZxPoPujk=\",\n   \"e\": \"AQAB\"\n  }\n ]\n}\n";
        JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(jwks);
        List<JsonWebKey> jsonWebKeys = jsonWebKeySet.getJsonWebKeys();
        Assert.assertThat((Object)jsonWebKeys.size(), (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)2)));
        String jose4jJwkJson = jsonWebKeySet.toJson();
        Assert.assertThat((Object)jose4jJwkJson, (Matcher)CoreMatchers.not((Matcher)CoreMatchers.containsString((String)"=")));
    }
}

