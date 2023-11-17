/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.kbx;

import java.io.EOFException;

public class KBXBlobType {
    public static KBXBlobType Empty = new KBXBlobType(0);
    public static KBXBlobType First = new KBXBlobType(1);
    public static KBXBlobType OpenPGP = new KBXBlobType(2);
    public static KBXBlobType X509 = new KBXBlobType(3);
    private int val;

    public KBXBlobType(int n) {
        this.val = n;
    }

    public static KBXBlobType fromInt(int n) throws EOFException {
        if (n == -1) {
            throw new EOFException();
        }
        if (n == 0) {
            return Empty;
        }
        if (n == 1) {
            return First;
        }
        if (n == 2) {
            return OpenPGP;
        }
        if (n == 3) {
            return X509;
        }
        throw new IllegalArgumentException();
    }
}

