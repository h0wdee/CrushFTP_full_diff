/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.kbx;

import java.io.IOException;
import java.io.InputStream;

public class KBXUserID {
    int Offset;
    int Length;
    short Flags;
    byte Validity;
    byte Reserved1;
    public static final int Size = 12;

    public KBXUserID(InputStream inputStream) throws IOException {
        this.Offset = inputStream.read() << 24 | inputStream.read() << 16 | inputStream.read() << 8 | inputStream.read();
        this.Length = inputStream.read() << 24 | inputStream.read() << 16 | inputStream.read() << 8 | inputStream.read();
        this.Flags = (short)(inputStream.read() << 8 | inputStream.read());
        this.Validity = (byte)inputStream.read();
        this.Reserved1 = (byte)inputStream.read();
    }
}

