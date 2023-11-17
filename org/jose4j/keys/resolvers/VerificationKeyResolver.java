/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.keys.resolvers;

import java.security.Key;
import java.util.List;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.lang.UnresolvableKeyException;

public interface VerificationKeyResolver {
    public Key resolveKey(JsonWebSignature var1, List<JsonWebStructure> var2) throws UnresolvableKeyException;
}

