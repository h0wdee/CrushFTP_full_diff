/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.keys.resolvers;

import java.io.IOException;
import java.security.Key;
import java.util.List;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.VerificationJwkSelector;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.keys.resolvers.VerificationKeyResolver;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UnresolvableKeyException;

public class HttpsJwksVerificationKeyResolver
implements VerificationKeyResolver {
    private HttpsJwks httpsJkws;

    public HttpsJwksVerificationKeyResolver(HttpsJwks httpsJkws) {
        this.httpsJkws = httpsJkws;
    }

    @Override
    public Key resolveKey(JsonWebSignature jws, List<JsonWebStructure> nestingContext) throws UnresolvableKeyException {
        JsonWebKey theChosenOne;
        List<JsonWebKey> jsonWebKeys;
        try {
            jsonWebKeys = this.httpsJkws.getJsonWebKeys();
            VerificationJwkSelector verificationJwkSelector = new VerificationJwkSelector();
            theChosenOne = verificationJwkSelector.select(jws, jsonWebKeys);
            if (theChosenOne == null) {
                this.httpsJkws.refresh();
                jsonWebKeys = this.httpsJkws.getJsonWebKeys();
                theChosenOne = verificationJwkSelector.select(jws, jsonWebKeys);
            }
        }
        catch (IOException | JoseException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unable to find a suitable verification key for JWS w/ header ").append(jws.getHeaders().getFullHeaderAsJsonString());
            sb.append(" due to an unexpected exception (").append(e).append(") while obtaining or using keys from JWKS endpoint at ").append(this.httpsJkws.getLocation());
            throw new UnresolvableKeyException(sb.toString(), e);
        }
        if (theChosenOne == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unable to find a suitable verification key for JWS w/ header ").append(jws.getHeaders().getFullHeaderAsJsonString());
            sb.append(" from JWKs ").append(jsonWebKeys).append(" obtained from ").append(this.httpsJkws.getLocation());
            throw new UnresolvableKeyException(sb.toString());
        }
        return theChosenOne.getKey();
    }
}

