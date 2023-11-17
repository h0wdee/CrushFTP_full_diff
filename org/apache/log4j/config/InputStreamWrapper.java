/*
 * Decompiled with CFR 0.152.
 */
package org.apache.log4j.config;

import java.io.IOException;
import java.io.InputStream;

class InputStreamWrapper
extends InputStream {
    private final String description;
    private final InputStream input;

    public InputStreamWrapper(InputStream input, String description) {
        this.input = input;
        this.description = description;
    }

    @Override
    public int available() throws IOException {
        return this.input.available();
    }

    @Override
    public void close() throws IOException {
        this.input.close();
    }

    public boolean equals(Object obj) {
        return this.input.equals(obj);
    }

    public int hashCode() {
        return this.input.hashCode();
    }

    @Override
    public synchronized void mark(int readlimit) {
        this.input.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return this.input.markSupported();
    }

    @Override
    public int read() throws IOException {
        return this.input.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.input.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return this.input.read(b, off, len);
    }

    @Override
    public synchronized void reset() throws IOException {
        this.input.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return this.input.skip(n);
    }

    public String toString() {
        return this.getClass().getSimpleName() + " [description=" + this.description + ", input=" + this.input + "]";
    }
}

