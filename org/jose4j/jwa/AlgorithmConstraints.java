/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwa;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.jose4j.lang.InvalidAlgorithmException;

public class AlgorithmConstraints {
    public static final AlgorithmConstraints NO_CONSTRAINTS = new AlgorithmConstraints(ConstraintType.BLACKLIST, new String[0]);
    public static final AlgorithmConstraints DISALLOW_NONE = new AlgorithmConstraints(ConstraintType.BLACKLIST, "none");
    public static final AlgorithmConstraints ALLOW_ONLY_NONE = new AlgorithmConstraints(ConstraintType.WHITELIST, "none");
    private final ConstraintType type;
    private final Set<String> algorithms;

    public AlgorithmConstraints(ConstraintType type, String ... algorithms) {
        if (type == null) {
            throw new NullPointerException("ConstraintType cannot be null");
        }
        this.type = type;
        this.algorithms = new HashSet<String>(Arrays.asList(algorithms));
    }

    public void checkConstraint(String algorithm) throws InvalidAlgorithmException {
        switch (this.type) {
            case WHITELIST: {
                if (this.algorithms.contains(algorithm)) break;
                throw new InvalidAlgorithmException("'" + algorithm + "' is not a whitelisted algorithm.");
            }
            case BLACKLIST: {
                if (!this.algorithms.contains(algorithm)) break;
                throw new InvalidAlgorithmException("'" + algorithm + "' is a blacklisted algorithm.");
            }
        }
    }

    public static enum ConstraintType {
        WHITELIST,
        BLACKLIST;

    }
}

