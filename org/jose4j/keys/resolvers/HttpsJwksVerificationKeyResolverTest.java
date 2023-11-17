/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hamcrest.CoreMatchers
 *  org.hamcrest.Matcher
 *  org.junit.Assert
 *  org.junit.Test
 *  org.mockito.Mockito
 */
package org.jose4j.keys.resolvers;

import java.io.IOException;
import java.security.Key;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.http.Get;
import org.jose4j.http.Response;
import org.jose4j.http.SimpleResponse;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.jose4j.lang.UnresolvableKeyException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpsJwksVerificationKeyResolverTest {
    private static final Logger log = LoggerFactory.getLogger(HttpsJwksVerificationKeyResolverTest.class);

    @Test
    public void simpleKeyFoundThenNotFoundAndRefreshToFindAndThenCantFind() throws Exception {
        String firstJkwsJson = "{\"keys\":[{\"kty\":\"EC\",\"kid\":\"k1\",\"x\":\"1u9oeAkLQJcAnrv_m4fupf-lF43yFqmNjMsrukKDhEE\",\"y\":\"RG0cyWzinUl8NpfVVw2DqfH6zRqU_yF6aL1swssNv4E\",\"crv\":\"P-256\"}]}";
        String secondJwkJson = "{\"keys\":[{\"kty\":\"EC\",\"kid\":\"k2\",\"x\":\"865vGRGnwRFf1YWFI-ODhHkQwYs7dc9VlI8zleEUqyA\",\"y\":\"W-7d1hvHrhNqNGVVNZjTUopIdaegL3jEjWOPX284AOk\",\"crv\":\"P-256\"}]}";
        JsonWebKeySet jwks = new JsonWebKeySet(firstJkwsJson);
        JsonWebKey k1 = jwks.getJsonWebKeys().iterator().next();
        jwks = new JsonWebKeySet(secondJwkJson);
        JsonWebKey k2 = jwks.getJsonWebKeys().iterator().next();
        String location = "https://www.example.org/";
        HttpsJwks httpsJkws = new HttpsJwks(location);
        Get mockGet = (Get)Mockito.mock(Get.class);
        Map<String, List<String>> headers = Collections.emptyMap();
        Response ok1 = new Response(200, "OK", headers, firstJkwsJson);
        Response ok2 = new Response(200, "OK", headers, secondJwkJson);
        Mockito.when((Object)mockGet.get(location)).thenReturn((Object)ok1, (Object[])new SimpleResponse[]{ok2});
        httpsJkws.setSimpleHttpGet(mockGet);
        HttpsJwksVerificationKeyResolver resolver = new HttpsJwksVerificationKeyResolver(httpsJkws);
        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue("ES256");
        jws.setKeyIdHeaderValue("k1");
        Key key = resolver.resolveKey(jws, Collections.emptyList());
        Assert.assertThat((Object)key, (Matcher)CoreMatchers.equalTo((Object)k1.getKey()));
        jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue("ES256");
        jws.setKeyIdHeaderValue("k1");
        key = resolver.resolveKey(jws, Collections.emptyList());
        Assert.assertThat((Object)key, (Matcher)CoreMatchers.equalTo((Object)k1.getKey()));
        jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue("ES256");
        jws.setKeyIdHeaderValue("k1");
        key = resolver.resolveKey(jws, Collections.emptyList());
        Assert.assertThat((Object)key, (Matcher)CoreMatchers.equalTo((Object)k1.getKey()));
        jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue("ES256");
        jws.setKeyIdHeaderValue("k2");
        key = resolver.resolveKey(jws, Collections.emptyList());
        Assert.assertThat((Object)key, (Matcher)CoreMatchers.equalTo((Object)k2.getKey()));
        jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue("ES256");
        jws.setKeyIdHeaderValue("k2");
        key = resolver.resolveKey(jws, Collections.emptyList());
        Assert.assertThat((Object)key, (Matcher)CoreMatchers.equalTo((Object)k2.getKey()));
        jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue("ES256");
        jws.setKeyIdHeaderValue("nope");
        try {
            key = resolver.resolveKey(jws, Collections.emptyList());
            Assert.fail((String)("shouldn't have resolved a key but got " + key));
        }
        catch (UnresolvableKeyException e) {
            log.debug("this was expected and is okay: {}", (Object)e.toString());
            Assert.assertFalse((String)"do you really need UnresolvableKeyException inside a UnresolvableKeyException?", (boolean)(e.getCause() instanceof UnresolvableKeyException));
        }
    }

    @Test
    public void testAnEx() throws Exception {
        String location = "https://www.example.org/";
        Get mockGet = (Get)Mockito.mock(Get.class);
        Mockito.when((Object)mockGet.get(location)).thenThrow(new Throwable[]{new IOException(String.valueOf(location) + "says 'no GET for you!'")});
        HttpsJwks httpsJkws = new HttpsJwks(location);
        httpsJkws.setSimpleHttpGet(mockGet);
        HttpsJwksVerificationKeyResolver resolver = new HttpsJwksVerificationKeyResolver(httpsJkws);
        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue("ES256");
        jws.setKeyIdHeaderValue("nope");
        try {
            Key key = resolver.resolveKey(jws, Collections.emptyList());
            Assert.fail((String)("shouldn't have resolved a key but got " + key));
        }
        catch (UnresolvableKeyException e) {
            log.debug("this was expected and is okay: {}", (Object)e.toString());
        }
    }
}

