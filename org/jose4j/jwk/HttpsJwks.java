/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwk;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.jose4j.http.Get;
import org.jose4j.http.SimpleGet;
import org.jose4j.http.SimpleResponse;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.lang.JoseException;

public class HttpsJwks {
    private String location;
    private long defaultCacheDuration = 3600L;
    private SimpleGet simpleHttpGet = new Get();
    private long retainCacheOnErrorDurationMills = 0L;
    private Cache cache = new Cache(Collections.emptyList(), 0L);

    public HttpsJwks(String location) {
        this.location = location;
    }

    public void setDefaultCacheDuration(long defaultCacheDuration) {
        this.defaultCacheDuration = defaultCacheDuration;
    }

    public void setRetainCacheOnErrorDuration(long retainCacheOnErrorDuration) {
        this.retainCacheOnErrorDurationMills = retainCacheOnErrorDuration * 1000L;
    }

    public void setSimpleHttpGet(SimpleGet simpleHttpGet) {
        this.simpleHttpGet = simpleHttpGet;
    }

    public String getLocation() {
        return this.location;
    }

    public List<JsonWebKey> getJsonWebKeys() throws JoseException, IOException {
        long now = System.currentTimeMillis();
        if (this.cache.getExp() < now) {
            try {
                this.refresh();
            }
            catch (Exception e) {
                if (this.retainCacheOnErrorDurationMills > 0L && !this.cache.keys.isEmpty()) {
                    this.cache.exp = now + this.retainCacheOnErrorDurationMills;
                }
                throw e;
            }
        }
        return this.cache.getKeys();
    }

    public void refresh() throws JoseException, IOException {
        SimpleResponse simpleResponse = this.simpleHttpGet.get(this.location);
        JsonWebKeySet jwks = new JsonWebKeySet(simpleResponse.getBody());
        List<JsonWebKey> keys = jwks.getJsonWebKeys();
        long cacheLife = HttpsJwks.getCacheLife(simpleResponse);
        if (cacheLife <= 0L) {
            cacheLife = this.defaultCacheDuration;
        }
        long exp = System.currentTimeMillis() + cacheLife * 1000L;
        this.cache = new Cache(keys, exp);
    }

    static long getDateHeaderValue(SimpleResponse response, String headerName, long defaultValue) {
        List<String> values = HttpsJwks.getHeaderValues(response, headerName);
        for (String value : values) {
            try {
                if (!value.endsWith("GMT")) {
                    value = String.valueOf(value) + " GMT";
                }
                return Date.parse(value);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return defaultValue;
    }

    private static List<String> getHeaderValues(SimpleResponse response, String headerName) {
        List<String> values = response.getHeaderValues(headerName);
        return values == null ? Collections.emptyList() : values;
    }

    static long getExpires(SimpleResponse response) {
        return HttpsJwks.getDateHeaderValue(response, "expires", 0L);
    }

    static long getCacheLife(SimpleResponse response) {
        return HttpsJwks.getCacheLife(response, System.currentTimeMillis());
    }

    static long getCacheLife(SimpleResponse response, long currentTime) {
        long expires = HttpsJwks.getExpires(response);
        long life = (expires - currentTime) / 1000L;
        List<String> values = HttpsJwks.getHeaderValues(response, "cache-control");
        for (String value : values) {
            try {
                value = value == null ? "" : value.toLowerCase();
                int indexOfMaxAge = value.indexOf("max-age");
                int indexOfComma = value.indexOf(44, indexOfMaxAge);
                int end = indexOfComma == -1 ? value.length() : indexOfComma;
                String part = value.substring(indexOfMaxAge, end);
                part = part.substring(part.indexOf(61) + 1);
                part = part.trim();
                life = Long.parseLong(part);
                break;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return life;
    }

    private static class Cache {
        private List<JsonWebKey> keys;
        private long exp;

        private Cache(List<JsonWebKey> keys, long exp) {
            this.keys = keys;
            this.exp = exp;
        }

        private List<JsonWebKey> getKeys() {
            return this.keys;
        }

        private long getExp() {
            return this.exp;
        }
    }
}

