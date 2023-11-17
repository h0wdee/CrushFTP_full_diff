/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class RandomAccessOutputStream
extends OutputStream {
    RandomAccessFile f;

    public RandomAccessOutputStream(RandomAccessFile f) {
        this.f = f;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.f.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        this.f.write(b);
    }

    @Override
    public void close() throws IOException {
        this.f.close();
    }
}

