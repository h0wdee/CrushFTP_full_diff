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

import java.security.MessageDigest;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.HashUtil;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.StringUtil;
import org.junit.Assert;
import org.junit.Test;

public class Rfc7638JwkThumbprintTest {
    @Test
    public void testRsaFromRfcExample3_1() throws JoseException {
        String json = "     {\n      \"kty\": \"RSA\",\n      \"n\": \"0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx4cbbfAAt\n            VT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMstn6\n            4tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FD\n            W2QvzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n9\n            1CbOpbISD08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINH\n            aQ-G_xBniIqbw0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw\",\n      \"e\": \"AQAB\",\n      \"alg\": \"RS256\",\n      \"kid\": \"2011-04-29\"\n     }";
        JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk(json);
        String actual = "NzbLsXh8uDCcd-6MNwXF4W_7noWXFZAfHkxZsRGC9Xs";
        String calculated = jsonWebKey.calculateBase64urlEncodedThumbprint("SHA-256");
        Assert.assertThat((Object)actual, (Matcher)CoreMatchers.equalTo((Object)calculated));
    }

    @Test
    public void testOct() throws JoseException {
        String json = "{\"k\":\"ZW8Eg8TiwoT2YamLJfC2leYpLgLmUAh_PcMHqRzBnMg\",\"kty\":\"oct\"}";
        JsonWebKey jwk = JsonWebKey.Factory.newJwk(json);
        byte[] thumb = jwk.calculateThumbprint("SHA-256");
        MessageDigest messageDigest = HashUtil.getMessageDigest("SHA-256");
        byte[] digest = messageDigest.digest(StringUtil.getBytesUtf8(json));
        Assert.assertArrayEquals((byte[])digest, (byte[])thumb);
        JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk("{\"kty\":\"oct\", \"k\":\"ZW8Eg8TiwoT2YamLJfC2leYpLgLmUAh_PcMHqRzBnMg\"}");
        Assert.assertThat((Object)jwk.calculateBase64urlEncodedThumbprint("SHA-256"), (Matcher)CoreMatchers.equalTo((Object)jsonWebKey.calculateBase64urlEncodedThumbprint("SHA-256")));
    }

    @Test
    public void testEc() throws JoseException {
        String json = "{\"crv\":\"P-256\",\"kty\":\"EC\",\"x\":\"CEuRLUISufhcjrj-32N0Bvl3KPMiHH9iSw4ohN9jxrA\",\"y\":\"EldWz_iXSK3l_S7n4w_t3baxos7o9yqX0IjzG959vHc\"}";
        JsonWebKey jwk = JsonWebKey.Factory.newJwk(json);
        byte[] thumb = jwk.calculateThumbprint("SHA-256");
        MessageDigest messageDigest = HashUtil.getMessageDigest("SHA-256");
        byte[] digest = messageDigest.digest(StringUtil.getBytesUtf8(json));
        Assert.assertArrayEquals((byte[])digest, (byte[])thumb);
        json = "{\"kty\":\"EC\",\"y\":\"EldWz_iXSK3l_S7n4w_t3baxos7o9yqX0IjzG959vHc\",\"crv\":\"P-256\",\"x\":\"CEuRLUISufhcjrj-32N0Bvl3KPMiHH9iSw4ohN9jxrA\"}";
        jwk = JsonWebKey.Factory.newJwk(json);
        thumb = jwk.calculateThumbprint("SHA-256");
        Assert.assertArrayEquals((byte[])digest, (byte[])thumb);
    }

    @Test
    public void testEcFromNimb() throws JoseException {
        String json = "{\"crv\":\"P-256\", \"kty\":\"EC\", \"y\":\"4Etl6SRW2YiLUrN5vfvVHuhp7x8PxltmWWlbbM4IFyM\", \"x\":\"MKBCTNIcKUSDii11ySs3526iDZ8AiTo7Tu6KPAqv7D4\"}";
        JsonWebKey jwk = JsonWebKey.Factory.newJwk(json);
        String thumb = jwk.calculateBase64urlEncodedThumbprint("SHA-256");
        Assert.assertThat((Object)"cn-I_WNMClehiVp51i_0VpOENW1upEerA8sEam5hn-s", (Matcher)CoreMatchers.equalTo((Object)thumb));
    }

    @Test
    public void testOctFromNimb() throws JoseException {
        String json = "{\"kty\":\"oct\",\"k\":\"GawgguFyGrWKav7AX4VKUg\"}";
        JsonWebKey jwk = JsonWebKey.Factory.newJwk(json);
        String thumb = jwk.calculateBase64urlEncodedThumbprint("SHA-256");
        Assert.assertThat((Object)"k1JnWRfC-5zzmL72vXIuBgTLfVROXBakS4OmGcrMCoc", (Matcher)CoreMatchers.equalTo((Object)thumb));
    }

    @Test
    public void joseWgListTestVectors() throws Exception {
        JsonWebKey jwk = JsonWebKey.Factory.newJwk("{\"kty\":\"oct\", \"k\":\"ZW8Eg8TiwoT2YamLJfC2leYpLgLmUAh_PcMHqRzBnMg\"}");
        String thumb = "7WWD36NF4WCpPaYtK47mM4o0a5CCeOt01JXSuMayv5g";
        Assert.assertThat((Object)thumb, (Matcher)CoreMatchers.equalTo((Object)jwk.calculateBase64urlEncodedThumbprint("SHA-256")));
        jwk = JsonWebKey.Factory.newJwk("{\"kty\":\"EC\",\n \"x\":\"CEuRLUISufhcjrj-32N0Bvl3KPMiHH9iSw4ohN9jxrA\",\n \"y\":\"EldWz_iXSK3l_S7n4w_t3baxos7o9yqX0IjzG959vHc\",\n \"crv\":\"P-256\"}");
        thumb = "j4UYwo9wrtllSHaoLDJNh7MhVCL8t0t8cGPPzChpYDs";
        Assert.assertThat((Object)thumb, (Matcher)CoreMatchers.equalTo((Object)jwk.calculateBase64urlEncodedThumbprint("SHA-256")));
        jwk = JsonWebKey.Factory.newJwk("{\"kty\":\"EC\",\n \"x\":\"Aeq3uMrb3iCQEt0PzSeZMmrmYhsKP5DM1oMP6LQzTFQY9-F3Ab45xiK4AJxltXEI-87g3gRwId88hTyHgq180JDt\",\n \"y\":\"ARA0lIlrZMEzaXyXE4hjEkc50y_JON3qL7HSae9VuWpOv_2kit8p3pyJBiRb468_U5ztLT7FvDvtimyS42trhDTu\",\n \"crv\":\"P-521\"}");
        thumb = "rz4Ohmpxg-UOWIWqWKHlOe0bHSjNUFlHW5vwG_M7qYg";
        Assert.assertThat((Object)thumb, (Matcher)CoreMatchers.equalTo((Object)jwk.calculateBase64urlEncodedThumbprint("SHA-256")));
        jwk = JsonWebKey.Factory.newJwk("{\"kty\":\"EC\",\n \"x\":\"2jCG5DmKUql9YPn7F2C-0ljWEbj8O8-vn5Ih1k7Wzb-y3NpBLiG1BiRa392b1kcQ\",\n \"y\":\"7Ragi9rT-5tSzaMbJlH_EIJl6rNFfj4V4RyFM5U2z4j1hesX5JXa8dWOsE-5wPIl\",\n \"crv\":\"P-384\"}");
        thumb = "vZtaWIw-zw95JNzzURg1YB7mWNLlm44YZDZzhrPNetM";
        Assert.assertThat((Object)thumb, (Matcher)CoreMatchers.equalTo((Object)jwk.calculateBase64urlEncodedThumbprint("SHA-256")));
        jwk = JsonWebKey.Factory.newJwk("{\"kty\":\"oct\",\"k\":\"NGbwp1rC4n85A1SaNxoHow\"}");
        thumb = "5_qb56G0OJDw-lb5mkDaWS4MwuY0fatkn9LkNqUHqMk";
        Assert.assertThat((Object)thumb, (Matcher)CoreMatchers.equalTo((Object)jwk.calculateBase64urlEncodedThumbprint("SHA-256")));
    }
}

