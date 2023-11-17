/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp;

public interface EcCurve {
    public static final String P256 = "P256";
    public static final String P384 = "P384";
    public static final String P521 = "P521";
    public static final String Brainpool256 = "Brainpool256";
    public static final String Brainpool384 = "Brainpool384";
    public static final String Brainpool512 = "Brainpool512";
    public static final String EdDsa = "EdDsa";
    public static final String Curve25519 = "Curve25519";

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static enum Enum {
        None,
        NIST_P_256,
        NIST_P_384,
        NIST_P_521,
        Brainpool256,
        Brainpool384,
        Brainpool512,
        EdDsa,
        Curve25519;


        public static Enum fromString(String string) {
            if (EcCurve.P256.equalsIgnoreCase(string)) {
                return NIST_P_256;
            }
            if (EcCurve.P384.equalsIgnoreCase(string)) {
                return NIST_P_384;
            }
            if (EcCurve.P521.equalsIgnoreCase(string)) {
                return NIST_P_521;
            }
            if (EcCurve.Brainpool256.equalsIgnoreCase(string)) {
                return Brainpool256;
            }
            if (EcCurve.Brainpool384.equalsIgnoreCase(string)) {
                return Brainpool384;
            }
            if (EcCurve.Brainpool512.equalsIgnoreCase(string)) {
                return Brainpool512;
            }
            if (EcCurve.EdDsa.equalsIgnoreCase(string)) {
                return EdDsa;
            }
            if (EcCurve.Curve25519.equalsIgnoreCase(string)) {
                return Curve25519;
            }
            throw new IllegalArgumentException("The supplied EC Curve parameter is invalid");
        }

        public String toString() {
            if (NIST_P_256.equals((Object)this)) {
                return EcCurve.P256;
            }
            if (NIST_P_384.equals((Object)this)) {
                return EcCurve.P384;
            }
            if (NIST_P_521.equals((Object)this)) {
                return EcCurve.P521;
            }
            if (Brainpool256.equals((Object)this)) {
                return EcCurve.Brainpool256;
            }
            if (Brainpool384.equals((Object)this)) {
                return EcCurve.Brainpool384;
            }
            if (Brainpool512.equals((Object)this)) {
                return EcCurve.Brainpool512;
            }
            if (EdDsa.equals((Object)this)) {
                return EcCurve.EdDsa;
            }
            if (Curve25519.equals((Object)this)) {
                return EcCurve.Curve25519;
            }
            return "Unknown";
        }
    }
}

