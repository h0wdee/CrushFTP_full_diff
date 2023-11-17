/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwa;

import org.jose4j.jwa.Algorithm;
import org.jose4j.keys.KeyPersuasion;

public abstract class AlgorithmInfo
implements Algorithm {
    private String algorithmIdentifier;
    private String javaAlgorithm;
    private KeyPersuasion keyPersuasion;
    private String keyType;

    public void setAlgorithmIdentifier(String algorithmIdentifier) {
        this.algorithmIdentifier = algorithmIdentifier;
    }

    public void setJavaAlgorithm(String javaAlgorithm) {
        this.javaAlgorithm = javaAlgorithm;
    }

    @Override
    public String getJavaAlgorithm() {
        return this.javaAlgorithm;
    }

    @Override
    public String getAlgorithmIdentifier() {
        return this.algorithmIdentifier;
    }

    @Override
    public KeyPersuasion getKeyPersuasion() {
        return this.keyPersuasion;
    }

    public void setKeyPersuasion(KeyPersuasion keyPersuasion) {
        this.keyPersuasion = keyPersuasion;
    }

    @Override
    public String getKeyType() {
        return this.keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }
}

