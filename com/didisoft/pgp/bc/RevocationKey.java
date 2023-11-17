/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.bcpg.SignatureSubpacket
 */
package com.didisoft.pgp.bc;

import lw.bouncycastle.bcpg.SignatureSubpacket;

public class RevocationKey
extends SignatureSubpacket {
    public static final byte CLASS_DEFAULT = -128;
    public static final byte CLASS_SENSITIVE = 64;
    private static final boolean isLongLength = false;

    public RevocationKey(boolean bl, byte[] byArray) {
        super(12, bl, false, byArray);
    }

    public RevocationKey(boolean bl, byte by, byte by2, byte[] byArray) {
        super(12, bl, false, RevocationKey.createData(by, by2, byArray));
    }

    private static byte[] createData(byte by, byte by2, byte[] byArray) {
        byte[] byArray2 = new byte[2 + byArray.length];
        byArray2[0] = by;
        byArray2[1] = by2;
        System.arraycopy(byArray, 0, byArray2, 2, byArray.length);
        return byArray2;
    }

    public byte getSignatureClass() {
        return this.data[0];
    }

    public byte getAlgorithm() {
        return this.data[1];
    }

    public byte[] getFingerprint() {
        byte[] byArray = new byte[this.data.length - 2];
        System.arraycopy(this.data, 2, byArray, 0, byArray.length);
        return byArray;
    }
}

