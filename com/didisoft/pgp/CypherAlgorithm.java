/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp;

public interface CypherAlgorithm {
    public static final String NONE = "NONE";
    public static final String TRIPLE_DES = "TRIPLE_DES";
    public static final String CAST5 = "CAST5";
    public static final String BLOWFISH = "BLOWFISH";
    public static final String AES_128 = "AES_128";
    public static final String AES_192 = "AES_192";
    public static final String AES_256 = "AES_256";
    public static final String TWOFISH = "TWOFISH";
    public static final String DES = "DES";
    public static final String SAFER = "SAFER";
    public static final String IDEA = "IDEA";
    public static final String CAMELLIA_128 = "CAMELLIA_128";
    public static final String CAMELLIA_192 = "CAMELLIA_192";
    public static final String CAMELLIA_256 = "CAMELLIA_256";

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static enum Enum {
        NONE,
        TRIPLE_DES,
        CAST5,
        BLOWFISH,
        AES_128,
        AES_192,
        AES_256,
        TWOFISH,
        DES,
        SAFER,
        IDEA,
        CAMELLIA_128,
        CAMELLIA_192,
        CAMELLIA_256;


        public static Enum fromString(String string) {
            if (CypherAlgorithm.TRIPLE_DES.equalsIgnoreCase(string) || CypherAlgorithm.TRIPLE_DES.equalsIgnoreCase(string) || "TRIPLE-DES".equalsIgnoreCase(string) || "3DES".equalsIgnoreCase(string) || "3-DES".equalsIgnoreCase(string)) {
                return TRIPLE_DES;
            }
            if (CypherAlgorithm.CAST5.equalsIgnoreCase(string) || "CAST_5".equalsIgnoreCase(string) || "CAST-5".equalsIgnoreCase(string)) {
                return CAST5;
            }
            if (CypherAlgorithm.BLOWFISH.equalsIgnoreCase(string)) {
                return BLOWFISH;
            }
            if (CypherAlgorithm.AES_128.equalsIgnoreCase(string) || "AES128".equalsIgnoreCase(string) || "AES-128".equalsIgnoreCase(string)) {
                return AES_128;
            }
            if (CypherAlgorithm.AES_256.equalsIgnoreCase(string) || "AES256".equalsIgnoreCase(string) || "AES-256".equalsIgnoreCase(string)) {
                return AES_256;
            }
            if (CypherAlgorithm.AES_192.equalsIgnoreCase(string) || "AES192".equalsIgnoreCase(string) || "AES-192".equalsIgnoreCase(string)) {
                return AES_192;
            }
            if (CypherAlgorithm.TWOFISH.equalsIgnoreCase(string)) {
                return TWOFISH;
            }
            if (CypherAlgorithm.DES.equalsIgnoreCase(string)) {
                return DES;
            }
            if (CypherAlgorithm.SAFER.equalsIgnoreCase(string)) {
                return SAFER;
            }
            if (CypherAlgorithm.IDEA.equalsIgnoreCase(string)) {
                return IDEA;
            }
            if (CypherAlgorithm.CAMELLIA_128.equalsIgnoreCase(string)) {
                return CAMELLIA_128;
            }
            if (CypherAlgorithm.CAMELLIA_192.equalsIgnoreCase(string)) {
                return CAMELLIA_192;
            }
            if (CypherAlgorithm.CAMELLIA_256.equalsIgnoreCase(string)) {
                return CAMELLIA_256;
            }
            throw new IllegalArgumentException("The supplied cypher algorithm parameter is invalid : " + string);
        }

        public static Enum fromInt(int n) {
            switch (n) {
                case 7: {
                    return AES_128;
                }
                case 9: {
                    return AES_256;
                }
                case 8: {
                    return AES_192;
                }
                case 4: {
                    return BLOWFISH;
                }
                case 11: {
                    return CAMELLIA_128;
                }
                case 12: {
                    return CAMELLIA_192;
                }
                case 13: {
                    return CAMELLIA_256;
                }
                case 3: {
                    return CAST5;
                }
                case 6: {
                    return DES;
                }
                case 1: {
                    return IDEA;
                }
                case 5: {
                    return SAFER;
                }
                case 2: {
                    return TRIPLE_DES;
                }
                case 10: {
                    return TWOFISH;
                }
            }
            return null;
        }

        public int intValue() {
            switch (this) {
                case AES_128: {
                    return 7;
                }
                case AES_256: {
                    return 9;
                }
                case AES_192: {
                    return 8;
                }
                case BLOWFISH: {
                    return 4;
                }
                case CAMELLIA_128: {
                    return 11;
                }
                case CAMELLIA_192: {
                    return 12;
                }
                case CAMELLIA_256: {
                    return 13;
                }
                case CAST5: {
                    return 3;
                }
                case DES: {
                    return 6;
                }
                case IDEA: {
                    return 1;
                }
                case SAFER: {
                    return 5;
                }
                case TRIPLE_DES: {
                    return 2;
                }
                case TWOFISH: {
                    return 10;
                }
            }
            return 0;
        }
    }
}

