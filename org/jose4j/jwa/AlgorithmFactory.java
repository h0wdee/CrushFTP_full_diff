/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwa;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.jose4j.jwa.Algorithm;
import org.jose4j.lang.InvalidAlgorithmException;

public class AlgorithmFactory<A extends Algorithm> {
    private String parameterName;
    private final Map<String, A> algorithms = new LinkedHashMap<String, A>();

    public AlgorithmFactory(String parameterName, Class<A> type) {
        this.parameterName = parameterName;
    }

    public A getAlgorithm(String algorithmIdentifier) throws InvalidAlgorithmException {
        Algorithm algo = (Algorithm)this.algorithms.get(algorithmIdentifier);
        if (algo == null) {
            throw new InvalidAlgorithmException(String.valueOf(algorithmIdentifier) + " is an unknown, unsupported or unavailable " + this.parameterName + " algorithm (not one of " + this.getSupportedAlgorithms() + ").");
        }
        return (A)algo;
    }

    public boolean isAvailable(String algorithmIdentifier) {
        return this.algorithms.containsKey(algorithmIdentifier);
    }

    public Set<String> getSupportedAlgorithms() {
        return Collections.unmodifiableSet(this.algorithms.keySet());
    }

    public void registerAlgorithm(A algorithm) {
        String algId = algorithm.getAlgorithmIdentifier();
        if (this.isAvailable(algorithm)) {
            this.algorithms.put(algId, algorithm);
        }
    }

    private boolean isAvailable(A algorithm) {
        try {
            return algorithm.isAvailable();
        }
        catch (Throwable e) {
            return false;
        }
    }

    public void unregisterAlgorithm(String algorithmIdentifier) {
        this.algorithms.remove(algorithmIdentifier);
    }
}

