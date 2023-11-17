/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

public interface ShellMatcher {
    public Continue matches(String var1, String var2);

    public static enum Continue {
        MORE_CONTENT_NEEDED,
        CONTENT_MATCHES,
        CONTENT_DOES_NOT_MATCH;

    }
}

