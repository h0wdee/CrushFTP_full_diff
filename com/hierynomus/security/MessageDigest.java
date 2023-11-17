/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.security;

public interface MessageDigest {
    public void update(byte[] var1);

    public byte[] digest();

    public void reset();
}

