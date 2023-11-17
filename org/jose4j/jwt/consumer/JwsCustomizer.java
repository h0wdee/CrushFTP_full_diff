/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwt.consumer;

import java.util.List;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.JsonWebStructure;

public interface JwsCustomizer {
    public void customize(JsonWebSignature var1, List<JsonWebStructure> var2);
}

