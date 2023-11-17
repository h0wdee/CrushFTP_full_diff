/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwa;

import java.security.Security;
import java.util.Set;

public class AlgorithmAvailability {
    public static boolean isAvailable(String serviceName, String algorithm) {
        Set<String> algorithms = Security.getAlgorithms(serviceName);
        for (String serviceAlg : algorithms) {
            if (!serviceAlg.equalsIgnoreCase(algorithm)) continue;
            return true;
        }
        return false;
    }
}

