/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal.security;

public interface Padding {
    public int pad(byte[] var1, int var2, int var3);

    public int unpad(byte[] var1, int var2, int var3);

    public int padLength(int var1);

    public String paddingScheme();
}

