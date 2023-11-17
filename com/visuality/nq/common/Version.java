/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

public abstract class Version {
    private static int major = 1;
    private static int minor = 0;

    public static int getMajor() {
        return major;
    }

    public static int getMinor() {
        return minor;
    }

    static String getAsString() {
        return major + "." + minor;
    }
}

