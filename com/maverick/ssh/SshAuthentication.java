/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

public interface SshAuthentication {
    public static final int COMPLETE = 1;
    public static final int FAILED = 2;
    public static final int FURTHER_AUTHENTICATION_REQUIRED = 3;
    public static final int CANCELLED = 4;
    public static final int PUBLIC_KEY_ACCEPTABLE = 5;

    public void setUsername(String var1);

    public String getUsername();

    public String getMethod();
}

