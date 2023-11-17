/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.security;

import com.hierynomus.security.SecurityException;

public interface Cipher {
    public void init(CryptMode var1, byte[] var2) throws SecurityException;

    public int update(byte[] var1, int var2, int var3, byte[] var4, int var5) throws SecurityException;

    public int doFinal(byte[] var1, int var2) throws SecurityException;

    public void reset();

    public static enum CryptMode {
        ENCRYPT,
        DECRYPT;

    }
}

