/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

public class DynamicBuffer {
    protected static final int DEFAULT_BUFFER_SIZE = 32768;
    protected byte[] buf = new byte[32768];
    protected int writepos = 0;
    protected int readpos = 0;
    protected InputStream in = new DynamicBufferInputStream();
    protected OutputStream out = new DynamicBufferOutputStream();
    private boolean closed = false;
    private int interrupt = 1000;
    private long timeout = 0L;

    public InputStream getInputStream() {
        return this.in;
    }

    public OutputStream getOutputStream() {
        return this.out;
    }

    public long getTimeout() {
        return this.timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    private synchronized void verifyBufferSize(int count) {
        if (count > this.buf.length - this.writepos) {
            System.arraycopy(this.buf, this.readpos, this.buf, 0, this.writepos - this.readpos);
            this.writepos -= this.readpos;
            this.readpos = 0;
        }
        while (count > this.buf.length - this.writepos) {
            byte[] tmp = new byte[this.buf.length + 32768];
            System.arraycopy(this.buf, 0, tmp, 0, this.writepos - this.readpos);
            this.buf = tmp;
        }
    }

    protected synchronized int available() {
        return this.writepos - this.readpos > 0 ? this.writepos - this.readpos : (this.closed ? -1 : 0);
    }

    private synchronized void block() throws InterruptedException, InterruptedIOException {
        long start = System.currentTimeMillis();
        if (!this.closed) {
            while (this.readpos >= this.writepos && !this.closed) {
                this.wait(this.interrupt);
                if (this.timeout <= 0L || System.currentTimeMillis() - start <= this.timeout) continue;
                throw new InterruptedIOException();
            }
        }
    }

    public synchronized void close() {
        if (!this.closed) {
            this.closed = true;
            this.notifyAll();
        }
    }

    protected synchronized void write(int b) throws IOException {
        if (this.closed) {
            throw new IOException("The buffer is closed");
        }
        this.verifyBufferSize(1);
        this.buf[this.writepos] = (byte)b;
        ++this.writepos;
        this.notifyAll();
    }

    protected synchronized void write(byte[] data, int offset, int len) throws IOException {
        if (this.closed) {
            throw new IOException("The buffer is closed");
        }
        this.verifyBufferSize(len);
        System.arraycopy(data, offset, this.buf, this.writepos, len);
        this.writepos += len;
        this.notifyAll();
    }

    public void setBlockInterrupt(int interrupt) {
        this.interrupt = interrupt;
    }

    protected synchronized int read() throws IOException {
        try {
            this.block();
        }
        catch (InterruptedException ex) {
            throw new InterruptedIOException("The blocking operation was interrupted");
        }
        if (this.closed && this.available() <= 0) {
            return -1;
        }
        return this.buf[this.readpos++] & 0xFF;
    }

    protected synchronized int read(byte[] data, int offset, int len) throws IOException {
        try {
            this.block();
        }
        catch (InterruptedException ex) {
            throw new InterruptedIOException("The blocking operation was interrupted");
        }
        if (this.closed && this.available() <= 0) {
            return -1;
        }
        int read = len > this.writepos - this.readpos ? this.writepos - this.readpos : len;
        System.arraycopy(this.buf, this.readpos, data, offset, read);
        this.readpos += read;
        return read;
    }

    protected synchronized void flush() throws IOException {
        this.notifyAll();
    }

    class DynamicBufferOutputStream
    extends OutputStream {
        DynamicBufferOutputStream() {
        }

        @Override
        public void write(int b) throws IOException {
            DynamicBuffer.this.write(b);
        }

        @Override
        public void write(byte[] data, int offset, int len) throws IOException {
            DynamicBuffer.this.write(data, offset, len);
        }

        @Override
        public void flush() throws IOException {
            DynamicBuffer.this.flush();
        }

        @Override
        public void close() {
            DynamicBuffer.this.close();
        }
    }

    class DynamicBufferInputStream
    extends InputStream {
        DynamicBufferInputStream() {
        }

        @Override
        public int read() throws IOException {
            return DynamicBuffer.this.read();
        }

        @Override
        public int read(byte[] data, int offset, int len) throws IOException {
            return DynamicBuffer.this.read(data, offset, len);
        }

        @Override
        public int available() {
            return DynamicBuffer.this.available();
        }

        @Override
        public void close() {
            DynamicBuffer.this.close();
        }
    }
}

