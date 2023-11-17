/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hamcrest.CoreMatchers
 *  org.hamcrest.Matcher
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.jwx;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwx.Headers;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Test;

public class HeadersTest {
    @Test
    public void testRoundTripJwkHeader() throws JoseException {
        Headers headers = new Headers();
        String ephemeralJwkJson = "\n{\"kty\":\"EC\",\n \"crv\":\"P-256\",\n \"x\":\"gI0GAILBdu7T53akrFmMyGcsF3n5dO7MmwNBHKW5SV0\",\n \"y\":\"SLW_xSffzlPWrHEVI30DHM_4egVwt3NQqeUD7nMFpps\",\n \"d\":\"0_NxaRPUMQoAJt50Gz8YiTr8gRTwyEaCumd-MToTmIo\"\n}";
        PublicJsonWebKey ephemeralJwk = PublicJsonWebKey.Factory.newPublicJwk(ephemeralJwkJson);
        String name = "jwk";
        headers.setJwkHeaderValue(name, ephemeralJwk);
        JsonWebKey jwk = headers.getJwkHeaderValue(name);
        Assert.assertThat((Object)ephemeralJwk.getKey(), (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)jwk.getKey())));
        String encodedHeader = headers.getEncodedHeader();
        Headers parsedHeaders = new Headers();
        parsedHeaders.setEncodedHeader(encodedHeader);
        JsonWebKey jwkFromParsed = parsedHeaders.getJwkHeaderValue(name);
        Assert.assertThat((Object)ephemeralJwk.getKey(), (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)jwkFromParsed.getKey())));
    }

    @Test
    public void multiValueHeader() throws JoseException {
        Headers headers = new Headers();
        headers.setStringHeaderValue("iss", "me");
        headers.setObjectHeaderValue("aud", Arrays.asList("you", "them"));
        Map<String, Object> map = JsonUtil.parseJson(headers.getFullHeaderAsJsonString());
        Assert.assertThat((Object)map.get("aud"), (Matcher)CoreMatchers.is((Matcher)CoreMatchers.instanceOf(List.class)));
    }
}

