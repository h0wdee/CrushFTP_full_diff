/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.protocol.commons;

import java.util.Arrays;

public class Objects {
    private Objects() {
    }

    public static boolean equals(Object a, Object b) {
        return a == b || a != null && a.equals(b);
    }

    public static int hash(Object ... values) {
        return Arrays.hashCode(values);
    }
}

