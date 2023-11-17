/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp;

public interface KeyAlgorithm {
    public static final String RSA = "RSA";
    public static final String ELGAMAL = "ELGAMAL";
    public static final String EC = "EC";

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static enum Enum {
        Unknown,
        RSA,
        ELGAMAL,
        EC;


        public static Enum fromString(String string) {
            if (KeyAlgorithm.RSA.equalsIgnoreCase(string)) {
                return RSA;
            }
            if (KeyAlgorithm.ELGAMAL.equalsIgnoreCase(string) || "DH".equalsIgnoreCase(string) || "DH/DSS".equalsIgnoreCase(string) || KeyAlgorithm.ELGAMAL.equalsIgnoreCase(string)) {
                return ELGAMAL;
            }
            if (KeyAlgorithm.EC.equalsIgnoreCase(string) || "ECC".equalsIgnoreCase(string) || "ECDH".equalsIgnoreCase(string) || "ECDSA".equalsIgnoreCase(string)) {
                return EC;
            }
            throw new IllegalArgumentException("The supplied assymetric key algorithm is invalid");
        }
    }
}

