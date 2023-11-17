/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwk;

import java.util.Collection;
import java.util.List;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.SelectorSupport;
import org.jose4j.jwk.SimpleJwkFilter;
import org.jose4j.jws.EcdsaUsingShaAlgorithm;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jws.JsonWebSignatureAlgorithm;
import org.jose4j.lang.JoseException;

public class VerificationJwkSelector {
    public JsonWebKey select(JsonWebSignature jws, Collection<JsonWebKey> keys) throws JoseException {
        List<JsonWebKey> jsonWebKeys = this.selectList(jws, keys);
        return jsonWebKeys.isEmpty() ? null : jsonWebKeys.get(0);
    }

    public List<JsonWebKey> selectList(JsonWebSignature jws, Collection<JsonWebKey> keys) throws JoseException {
        SimpleJwkFilter filter = SelectorSupport.commonFilterForInbound(jws);
        List<JsonWebKey> filtered = filter.filter(keys);
        if (this.hasMoreThanOne(filtered)) {
            filter.setAlg(jws.getAlgorithmHeaderValue(), SimpleJwkFilter.OMITTED_OKAY);
            filtered = filter.filter(filtered);
        }
        if (this.hasMoreThanOne(filtered) && "EC".equals(jws.getKeyType())) {
            JsonWebSignatureAlgorithm algorithm = jws.getAlgorithmNoConstraintCheck();
            EcdsaUsingShaAlgorithm ecdsaAlgorithm = (EcdsaUsingShaAlgorithm)algorithm;
            filter.setCrv(ecdsaAlgorithm.getCurveName(), SimpleJwkFilter.OMITTED_OKAY);
            filtered = filter.filter(filtered);
        }
        return filtered;
    }

    private boolean hasMoreThanOne(List<JsonWebKey> filtered) {
        return filtered.size() > 1;
    }
}

