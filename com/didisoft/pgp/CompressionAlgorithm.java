/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp;

public interface CompressionAlgorithm {
    public static final String ZLIB = "ZLIB";
    public static final String ZIP = "ZIP";
    public static final String BZIP2 = "BZIP2";
    public static final String UNCOMPRESSED = "UNCOMPRESSED";

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static enum Enum {
        ZLIB,
        ZIP,
        BZIP2,
        UNCOMPRESSED;


        public static Enum fromString(String string) {
            if (CompressionAlgorithm.ZIP.equalsIgnoreCase(string)) {
                return ZIP;
            }
            if (CompressionAlgorithm.ZLIB.equalsIgnoreCase(string)) {
                return ZLIB;
            }
            if (CompressionAlgorithm.BZIP2.equalsIgnoreCase(string)) {
                return BZIP2;
            }
            if (CompressionAlgorithm.UNCOMPRESSED.equalsIgnoreCase(string)) {
                return UNCOMPRESSED;
            }
            throw new IllegalArgumentException("The supplied compression parameter is invalid");
        }

        public static Enum fromInt(int n) {
            if (1 == n) {
                return ZIP;
            }
            if (2 == n) {
                return ZLIB;
            }
            if (3 == n) {
                return BZIP2;
            }
            if (0 == n) {
                return UNCOMPRESSED;
            }
            throw new IllegalArgumentException("The supplied compression parameter is invalid");
        }

        public String toString() {
            if (ZLIB.equals((Object)this)) {
                return CompressionAlgorithm.ZLIB;
            }
            if (ZIP.equals((Object)this)) {
                return CompressionAlgorithm.ZIP;
            }
            if (BZIP2.equals((Object)this)) {
                return CompressionAlgorithm.BZIP2;
            }
            if (UNCOMPRESSED.equals((Object)this)) {
                return CompressionAlgorithm.UNCOMPRESSED;
            }
            return "Unknown";
        }

        public int intValue() {
            switch (this) {
                case ZLIB: {
                    return 2;
                }
                case ZIP: {
                    return 1;
                }
                case BZIP2: {
                    return 3;
                }
                case UNCOMPRESSED: {
                    return 0;
                }
            }
            return -1;
        }
    }
}

