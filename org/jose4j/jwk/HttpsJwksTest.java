/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hamcrest.CoreMatchers
 *  org.hamcrest.Matcher
 *  org.junit.Assert
 *  org.junit.Ignore
 *  org.junit.Test
 */
package org.jose4j.jwk;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.http.Get;
import org.jose4j.http.Response;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.keys.X509Util;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpsJwksTest {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testExpiresDateHeadersPerRfc() throws Exception {
        long actualDateMs = 784111777000L;
        long actualCacheLife = 60L;
        long fakeCurrentTime = 784111717000L;
        Map<String, List<String>> headers = Collections.singletonMap("Expires", Collections.singletonList("Sun, 06 Nov 1994 08:49:37 GMT"));
        Response simpleResponse = new Response(200, "OK", headers, "doesn't matter");
        Assert.assertThat((Object)actualDateMs, (Matcher)CoreMatchers.equalTo((Object)HttpsJwks.getExpires(simpleResponse)));
        Assert.assertThat((Object)actualCacheLife, (Matcher)CoreMatchers.equalTo((Object)HttpsJwks.getCacheLife(simpleResponse, fakeCurrentTime)));
        headers = Collections.singletonMap("Expires", Collections.singletonList("Sunday, 06-Nov-94 08:49:37 GMT"));
        simpleResponse = new Response(200, "OK", headers, "doesn't matter");
        Assert.assertThat((Object)actualDateMs, (Matcher)CoreMatchers.equalTo((Object)HttpsJwks.getExpires(simpleResponse)));
        Assert.assertThat((Object)actualCacheLife, (Matcher)CoreMatchers.equalTo((Object)HttpsJwks.getCacheLife(simpleResponse, fakeCurrentTime)));
        headers = Collections.singletonMap("Expires", Collections.singletonList("Sun Nov  6 08:49:37 1994"));
        simpleResponse = new Response(200, "OK", headers, "*still* doesn't matter");
        Assert.assertThat((Object)actualDateMs, (Matcher)CoreMatchers.equalTo((Object)HttpsJwks.getExpires(simpleResponse)));
        Assert.assertThat((Object)actualCacheLife, (Matcher)CoreMatchers.equalTo((Object)HttpsJwks.getCacheLife(simpleResponse, fakeCurrentTime)));
    }

    @Test
    public void testCacheLifeFromCacheControlMaxAge() throws Exception {
        String[] headerValues;
        String[] stringArray = headerValues = new String[]{"public, max-age=23760, must-revalidate, no-transform", "public, max-age=    23760 , must-revalidate", "public,max-age = 23760, must-revalidate", "public, max-age=23760, must-revalidate, no-transform", "must-revalidate,public,max-age=23760,no-transform", "max-age =23760, must-revalidate, public", "max-age=23760", "max-age =23760", "max-age = 23760 ", "max-age=23760,", "fake=\"f,a,k,e\",public, max-age=23760, must-revalidate=\"this , shouldn't be here\", whatever"};
        int n = headerValues.length;
        int n2 = 0;
        while (n2 < n) {
            String headerValue = stringArray[n2];
            HashMap<String, List<String>> headers = new HashMap<String, List<String>>();
            headers.put("Expires", Collections.singletonList("Expires: Tue, 27 Jan 2015 16:00:10 GMT"));
            headers.put("Cache-Control", Collections.singletonList(headerValue));
            Response simpleResponse = new Response(200, "OK", headers, "doesn't matter");
            long cacheLife = HttpsJwks.getCacheLife(simpleResponse);
            Assert.assertThat((String)("it done broke on this one " + headerValue), (Object)23760L, (Matcher)CoreMatchers.equalTo((Object)cacheLife));
            ++n2;
        }
    }

    @Test
    @Ignore
    public void testKindaSimplisticConcurrent() throws Exception {
        X509Util x509Util = new X509Util();
        X509Certificate certificate = x509Util.fromBase64Der("MIICUDCCAbkCBETczdcwDQYJKoZIhvcNAQEFBQAwbzELMAkGA1UEBhMCVVMxCzAJ\nBgNVBAgTAkNPMQ8wDQYDVQQHEwZEZW52ZXIxFTATBgNVBAoTDFBpbmdJZGVudGl0\neTEXMBUGA1UECxMOQnJpYW4gQ2FtcGJlbGwxEjAQBgNVBAMTCWxvY2FsaG9zdDAe\nFw0wNjA4MTExODM1MDNaFw0zMzEyMjcxODM1MDNaMG8xCzAJBgNVBAYTAlVTMQsw\nCQYDVQQIEwJDTzEPMA0GA1UEBxMGRGVudmVyMRUwEwYDVQQKEwxQaW5nSWRlbnRp\ndHkxFzAVBgNVBAsTDkJyaWFuIENhbXBiZWxsMRIwEAYDVQQDEwlsb2NhbGhvc3Qw\ngZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAJLrpeiY/Ai2gGFxNY8Tm/QSO8qg\nPOGKDMAT08QMyHRlxW8fpezfBTAtKcEsztPzwYTLWmf6opfJT+5N6cJKacxWchn/\ndRrzV2BoNuz1uo7wlpRqwcaOoi6yHuopNuNO1ms1vmlv3POq5qzMe6c1LRGADyZh\ni0KejDX6+jVaDiUTAgMBAAEwDQYJKoZIhvcNAQEFBQADgYEAMojbPEYJiIWgQzZc\nQJCQeodtKSJl5+lA8MWBBFFyZmvZ6jUYglIQdLlc8Pu6JF2j/hZEeTI87z/DOT6U\nuqZA83gZcy6re4wMnZvY2kWX9CsVWDCaZhnyhjBNYfhcOf0ZychoKShaEpTQ5UAG\nwvYYcbqIWC04GAZYVsZxlPl9hoA=\n");
        String location = "https://localhost:9031/pf/JWKS";
        Get get = new Get();
        get.setTrustedCertificates(certificate);
        HttpsJwks httpsJwks = new HttpsJwks(location);
        httpsJwks.setSimpleHttpGet(get);
        httpsJwks.setDefaultCacheDuration(1L);
        httpsJwks.setRetainCacheOnErrorDuration(1L);
        Callable<List> task = new Callable<List>(){

            @Override
            public List<JsonWebKey> call() throws Exception {
                List<JsonWebKey> jsonWebKeys = null;
                long i = 1000000000L;
                while (i > 0L) {
                    jsonWebKeys = httpsJwks.getJsonWebKeys();
                    Assert.assertFalse((boolean)jsonWebKeys.isEmpty());
                    if (i % 10000000L == 0L) {
                        HttpsJwksTest.this.log.debug("... working ... " + i + " ... " + Thread.currentThread().toString());
                    }
                    --i;
                }
                return jsonWebKeys;
            }
        };
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<1> tasks = Collections.nCopies(threadCount, task);
        List futures = executorService.invokeAll(tasks);
        this.log.debug("=== and done ===");
        for (Future future : futures) {
            this.log.debug(((List)future.get()).toString());
        }
    }

    static /* synthetic */ Logger access$0(HttpsJwksTest httpsJwksTest) {
        return httpsJwksTest.log;
    }
}

