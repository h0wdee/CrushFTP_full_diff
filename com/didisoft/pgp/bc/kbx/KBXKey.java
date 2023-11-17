/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.kbx;

import java.io.IOException;
import java.io.InputStream;

public class KBXKey {
    byte[] fingerprint;
    int Offset;
    short KeyFlags;
    short Reserved1;
    byte[] filler = new byte[0];
    public byte[] KeyID = new byte[0];

    public static KBXKey create(InputStream inputStream, int n) throws IOException {
        KBXKey kBXKey = new KBXKey();
        kBXKey.fingerprint = new byte[20];
        inputStream.read(kBXKey.fingerprint, 0, kBXKey.fingerprint.length);
        kBXKey.Offset = inputStream.read() << 24 | inputStream.read() << 16 | inputStream.read() << 8 | inputStream.read();
        kBXKey.KeyFlags = (short)(inputStream.read() << 8 | inputStream.read());
        kBXKey.Reserved1 = (short)(inputStream.read() << 8 | inputStream.read());
        int n2 = n - 28;
        if (n2 > 0) {
            kBXKey.filler = new byte[n2];
            inputStream.read(kBXKey.filler, 0, kBXKey.filler.length);
        }
        return kBXKey;
    }
}

