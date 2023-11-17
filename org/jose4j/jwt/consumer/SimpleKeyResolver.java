/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwt.consumer;

import java.security.Key;
import java.util.List;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.keys.resolvers.DecryptionKeyResolver;
import org.jose4j.keys.resolvers.VerificationKeyResolver;

class SimpleKeyResolver
implements VerificationKeyResolver,
DecryptionKeyResolver {
    private Key key;

    SimpleKeyResolver(Key key) {
        this.key = key;
    }

    @Override
    public Key resolveKey(JsonWebEncryption jwe, List<JsonWebStructure> nestingContext) {
        return this.key;
    }

    @Override
    public Key resolveKey(JsonWebSignature jws, List<JsonWebStructure> nestingContext) {
        return this.key;
    }
}

