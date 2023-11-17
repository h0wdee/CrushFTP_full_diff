/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UnChunkInputStream
extends BufferedInputStream {
    boolean closed;
    InputStream in = null;
    boolean needSize = true;
    long chunkSize = 0L;
    long markChunkSize = 0L;
    boolean markNeedSize = true;
    boolean chunked = true;

    public UnChunkInputStream(InputStream in) {
        super(in);
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        byte[] b1 = new byte[1];
        int bytesRead = this.read(b1, 0, 1);
        if (bytesRead < 0) {
            return -1;
        }
        return b1[0] & 0xFF;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    public void setChunked(boolean chunked) {
        this.chunked = chunked;
    }

    public void reInitialize() {
        this.needSize = true;
        this.chunkSize = 0L;
        this.markChunkSize = 0L;
        this.markNeedSize = true;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int bytes_read;
        if (!this.chunked) {
            return this.in.read(b);
        }
        if (this.needSize) {
            this.chunkSize = Common.getChunkSize(this.in);
            this.needSize = false;
            if (this.chunkSize == 0L) {
                return -1;
            }
        }
        if (this.chunkSize < 0L) {
            return -1;
        }
        int minLen = b.length;
        if (this.chunkSize < (long)minLen) {
            minLen = (int)this.chunkSize;
        }
        if (len < minLen) {
            minLen = len;
        }
        if ((bytes_read = this.in.read(b, off, minLen)) > 0) {
            this.chunkSize -= (long)bytes_read;
        }
        if (this.chunkSize == 0L) {
            Common.getChunkSize(this.in);
            this.needSize = true;
        }
        return bytes_read;
    }

    @Override
    public long skip(long n) throws IOException {
        return this.read(new byte[(int)n]);
    }

    @Override
    public int available() throws IOException {
        return this.in.available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        this.in.mark(readlimit);
        this.markChunkSize = this.chunkSize;
        this.markNeedSize = this.needSize;
    }

    @Override
    public synchronized void reset() throws IOException {
        this.in.reset();
        this.chunkSize = this.markChunkSize;
        this.needSize = this.markNeedSize;
    }

    @Override
    public boolean markSupported() {
        return this.in.markSupported();
    }

    @Override
    public void close() throws IOException {
        if (this.closed) {
            return;
        }
        this.closed = true;
        this.in.close();
    }
}

