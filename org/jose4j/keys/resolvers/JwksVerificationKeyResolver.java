/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.keys.resolvers;

import java.security.Key;
import java.util.List;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.VerificationJwkSelector;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.keys.resolvers.VerificationKeyResolver;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UnresolvableKeyException;

public class JwksVerificationKeyResolver
implements VerificationKeyResolver {
    private List<JsonWebKey> jsonWebKeys;
    private VerificationJwkSelector selector = new VerificationJwkSelector();

    public JwksVerificationKeyResolver(List<JsonWebKey> jsonWebKeys) {
        this.jsonWebKeys = jsonWebKeys;
    }

    @Override
    public Key resolveKey(JsonWebSignature jws, List<JsonWebStructure> nestingContext) throws UnresolvableKeyException {
        JsonWebKey selected;
        try {
            selected = this.selector.select(jws, this.jsonWebKeys);
        }
        catch (JoseException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unable to find a suitable verification key for JWS w/ header ").append(jws.getHeaders().getFullHeaderAsJsonString());
            sb.append(" due to an unexpected exception (").append(e).append(") selecting from keys: ").append(this.jsonWebKeys);
            throw new UnresolvableKeyException(sb.toString(), e);
        }
        if (selected == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unable to find a suitable verification key for JWS w/ header ").append(jws.getHeaders().getFullHeaderAsJsonString());
            sb.append(" from JWKs ").append(this.jsonWebKeys);
            throw new UnresolvableKeyException(sb.toString());
        }
        return selected.getKey();
    }
}

