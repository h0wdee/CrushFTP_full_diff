/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components;

import com.maverick.ssh.components.Digest;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DigestInputStream
extends FilterInputStream {
    Digest digest;

    public DigestInputStream(InputStream in, Digest digest) {
        super(in);
        this.digest = digest;
    }

    @Override
    public int read() throws IOException {
        int ch = super.read();
        if (ch != -1) {
            this.digest.putByte((byte)ch);
        }
        return ch;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int r = super.read(b, off, len);
        if (r > -1) {
            this.digest.putBytes(b, off, r);
        }
        return r;
    }

    public byte[] doFinal() {
        return this.digest.doFinal();
    }
}

