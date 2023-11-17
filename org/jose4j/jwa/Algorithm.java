/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwa;

import org.jose4j.keys.KeyPersuasion;

public interface Algorithm {
    public String getJavaAlgorithm();

    public String getAlgorithmIdentifier();

    public KeyPersuasion getKeyPersuasion();

    public String getKeyType();

    public boolean isAvailable();
}

