/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp;

public interface HashAlgorithm {
    public static final String SHA1 = "SHA1";
    public static final String SHA256 = "SHA256";
    public static final String SHA384 = "SHA384";
    public static final String SHA512 = "SHA512";
    public static final String SHA224 = "SHA224";
    public static final String MD5 = "MD5";
    public static final String RIPEMD160 = "RIPEMD160";
    public static final String MD2 = "MD2";

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static enum Enum {
        SHA1,
        SHA256,
        SHA384,
        SHA512,
        SHA224,
        MD5,
        RIPEMD160,
        MD2;


        public static Enum fromString(String string) {
            if (HashAlgorithm.SHA1.equalsIgnoreCase(string) || "SHA_1".equalsIgnoreCase(string) || "SHA-1".equalsIgnoreCase(string)) {
                return SHA1;
            }
            if (HashAlgorithm.SHA256.equalsIgnoreCase(string) || "SHA_256".equalsIgnoreCase(string) || "SHA-256".equalsIgnoreCase(string)) {
                return SHA256;
            }
            if (HashAlgorithm.SHA384.equalsIgnoreCase(string) || "SHA_384".equalsIgnoreCase(string) || "SHA-384".equalsIgnoreCase(string)) {
                return SHA384;
            }
            if (HashAlgorithm.SHA512.equalsIgnoreCase(string) || "SHA_512".equalsIgnoreCase(string) || "SHA-512".equalsIgnoreCase(string)) {
                return SHA512;
            }
            if (HashAlgorithm.SHA224.equalsIgnoreCase(string) || "SHA_224".equalsIgnoreCase(string) || "SHA-224".equalsIgnoreCase(string)) {
                return SHA224;
            }
            if (HashAlgorithm.MD5.equalsIgnoreCase(string) || "MD_5".equalsIgnoreCase(string) || "MD-5".equalsIgnoreCase(string)) {
                return MD5;
            }
            if (HashAlgorithm.MD2.equalsIgnoreCase(string)) {
                return MD2;
            }
            if (HashAlgorithm.RIPEMD160.equalsIgnoreCase(string)) {
                return RIPEMD160;
            }
            throw new IllegalArgumentException("The supplied hash algorithm parameter is invalid: " + string);
        }

        public static Enum fromInt(int n) {
            switch (n) {
                case 2: {
                    return SHA1;
                }
                case 8: {
                    return SHA256;
                }
                case 9: {
                    return SHA384;
                }
                case 10: {
                    return SHA512;
                }
                case 11: {
                    return SHA224;
                }
                case 1: {
                    return MD5;
                }
                case 3: {
                    return RIPEMD160;
                }
                case 5: {
                    return MD2;
                }
            }
            return SHA1;
        }

        public int intValue() {
            switch (this) {
                case SHA1: {
                    return 2;
                }
                case SHA256: {
                    return 8;
                }
                case SHA384: {
                    return 9;
                }
                case SHA512: {
                    return 10;
                }
                case SHA224: {
                    return 11;
                }
                case MD5: {
                    return 1;
                }
                case RIPEMD160: {
                    return 3;
                }
                case MD2: {
                    return 5;
                }
            }
            return -1;
        }
    }
}

