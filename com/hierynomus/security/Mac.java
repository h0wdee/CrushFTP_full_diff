/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.security;

import com.hierynomus.security.SecurityException;

public interface Mac {
    public void init(byte[] var1) throws SecurityException;

    public void update(byte var1);

    public void update(byte[] var1);

    public void update(byte[] var1, int var2, int var3);

    public byte[] doFinal();

    public void reset();
}

