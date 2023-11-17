/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.bcpg.SignatureSubpacket
 *  lw.bouncycastle.util.Strings
 */
package com.didisoft.pgp.bc;

import lw.bouncycastle.bcpg.SignatureSubpacket;
import lw.bouncycastle.util.Strings;

public class RevocationReason
extends SignatureSubpacket {
    public static final byte REASON_NO_REASON = 0;
    public static final byte REASON_KEY_SUPERSEDED = 1;
    public static final byte REASON_KEY_COMPROMISED = 2;
    public static final byte REASON_KEY_NO_LONGER_USED = 3;
    public static final byte REASON_USER_NO_LONGER_USED = 32;
    private static final boolean isLongLength = false;

    public RevocationReason(boolean bl, byte[] byArray) {
        super(29, bl, false, byArray);
    }

    public RevocationReason(boolean bl, byte by, String string) {
        super(29, bl, false, RevocationReason.createData(by, string));
    }

    private static byte[] createData(byte by, String string) {
        byte[] byArray = Strings.toUTF8ByteArray((String)string);
        byte[] byArray2 = new byte[1 + byArray.length];
        byArray2[0] = by;
        System.arraycopy(byArray, 0, byArray2, 1, byArray.length);
        return byArray2;
    }

    public byte getRevocationReason() {
        return this.data[0];
    }

    public String getRevocationDescription() {
        if (this.data.length == 1) {
            return "";
        }
        byte[] byArray = new byte[this.data.length - 1];
        System.arraycopy(this.data, 1, byArray, 0, byArray.length);
        return Strings.fromUTF8ByteArray((byte[])byArray);
    }
}

