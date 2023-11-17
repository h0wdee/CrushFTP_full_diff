/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwt.consumer;

import java.util.List;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwx.JsonWebStructure;

public interface JweCustomizer {
    public void customize(JsonWebEncryption var1, List<JsonWebStructure> var2);
}

