/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.common;

import java.util.Arrays;

public class Check {
    public static void ensureEquals(byte[] real, byte[] expected, String errorMessage) {
        if (!Arrays.equals(real, expected)) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static void ensure(boolean condition, String errorMessage) {
        if (!condition) {
            throw new IllegalStateException(errorMessage);
        }
    }
}

