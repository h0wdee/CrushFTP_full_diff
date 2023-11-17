/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.keys.resolvers;

import java.security.Key;
import java.util.List;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.DecryptionJwkSelector;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.keys.resolvers.DecryptionKeyResolver;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UnresolvableKeyException;

public class JwksDecryptionKeyResolver
implements DecryptionKeyResolver {
    private List<JsonWebKey> jsonWebKeys;
    private DecryptionJwkSelector selector = new DecryptionJwkSelector();

    public JwksDecryptionKeyResolver(List<JsonWebKey> jsonWebKeys) {
        this.jsonWebKeys = jsonWebKeys;
    }

    @Override
    public Key resolveKey(JsonWebEncryption jwe, List<JsonWebStructure> nestingContext) throws UnresolvableKeyException {
        JsonWebKey selected;
        try {
            selected = this.selector.select(jwe, this.jsonWebKeys);
        }
        catch (JoseException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unable to find a suitable key for JWE w/ header ").append(jwe.getHeaders().getFullHeaderAsJsonString());
            sb.append(" due to an unexpected exception (").append(e).append(") selecting from keys: ").append(this.jsonWebKeys);
            throw new UnresolvableKeyException(sb.toString(), e);
        }
        if (selected == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unable to find a suitable key for JWE w/ header ").append(jwe.getHeaders().getFullHeaderAsJsonString());
            sb.append(" from JWKs ").append(this.jsonWebKeys);
            throw new UnresolvableKeyException(sb.toString());
        }
        try {
            PublicJsonWebKey publicJsonWebKey = (PublicJsonWebKey)selected;
            return publicJsonWebKey.getPrivateKey();
        }
        catch (ClassCastException e) {
            return selected.getKey();
        }
    }
}

