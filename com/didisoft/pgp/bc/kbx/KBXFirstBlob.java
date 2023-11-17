/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.kbx;

import java.io.IOException;
import java.io.InputStream;

public class KBXFirstBlob {
    public int Length;
    byte BlobType;
    byte VersionNumber;
    short HeaderFlags;
    byte[] Magic;
    int Reserved1;
    int file_created_at;
    int last_maintenance_run;
    int Reserved2;
    int Reserved3;

    public KBXFirstBlob(InputStream inputStream) throws IOException {
        this.Length = inputStream.read() << 24 | inputStream.read() << 16 | inputStream.read() << 8 | inputStream.read();
        this.BlobType = (byte)inputStream.read();
        this.VersionNumber = (byte)inputStream.read();
        this.HeaderFlags = (short)(inputStream.read() << 8 | inputStream.read());
        this.Magic = new byte[4];
        inputStream.read(this.Magic, 0, this.Magic.length);
        this.Reserved1 = inputStream.read() << 24 | inputStream.read() << 16 | inputStream.read() << 8 | inputStream.read();
        this.file_created_at = inputStream.read() << 24 | inputStream.read() << 16 | inputStream.read() << 8 | inputStream.read();
        this.last_maintenance_run = inputStream.read() << 24 | inputStream.read() << 16 | inputStream.read() << 8 | inputStream.read();
        this.Reserved2 = inputStream.read() << 24 | inputStream.read() << 16 | inputStream.read() << 8 | inputStream.read();
        this.Reserved3 = inputStream.read() << 24 | inputStream.read() << 16 | inputStream.read() << 8 | inputStream.read();
    }
}

