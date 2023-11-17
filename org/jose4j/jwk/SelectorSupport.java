/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwk;

import org.jose4j.jwk.SimpleJwkFilter;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.lang.JoseException;

class SelectorSupport {
    SelectorSupport() {
    }

    public static SimpleJwkFilter commonFilterForInbound(JsonWebStructure jwx) throws JoseException {
        SimpleJwkFilter filter = new SimpleJwkFilter();
        String kid = jwx.getKeyIdHeaderValue();
        if (kid != null) {
            filter.setKid(kid, SimpleJwkFilter.VALUE_REQUIRED);
        }
        String x5t = jwx.getX509CertSha1ThumbprintHeaderValue();
        String x5tS256 = jwx.getX509CertSha256ThumbprintHeaderValue();
        filter.setAllowFallbackDeriveFromX5cForX5Thumbs(true);
        if (x5t != null) {
            filter.setX5t(x5t, SimpleJwkFilter.OMITTED_OKAY);
        }
        if (x5tS256 != null) {
            filter.setX5tS256(x5tS256, SimpleJwkFilter.OMITTED_OKAY);
        }
        String keyType = jwx.getAlgorithmNoConstraintCheck().getKeyType();
        filter.setKty(keyType);
        String use = jwx instanceof JsonWebSignature ? "sig" : "enc";
        filter.setUse(use, SimpleJwkFilter.OMITTED_OKAY);
        return filter;
    }
}

