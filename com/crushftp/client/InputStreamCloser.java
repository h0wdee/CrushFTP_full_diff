/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamCloser
extends InputStream {
    InputStream in = null;
    InputStream real_in = null;
    boolean closed = false;

    public InputStreamCloser(InputStream in, InputStream real_in) {
        this.in = in;
        this.real_in = real_in;
    }

    @Override
    public int read() throws IOException {
        return this.in.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.in.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return this.in.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        if (this.closed) {
            return;
        }
        this.closed = true;
        if (this.in != null) {
            this.in.close();
        }
        if (this.real_in != null) {
            this.real_in.close();
        }
    }
}

