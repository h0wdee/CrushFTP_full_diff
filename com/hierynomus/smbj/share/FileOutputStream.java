/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.share;

import com.hierynomus.smbj.ProgressListener;
import com.hierynomus.smbj.io.ByteChunkProvider;
import com.hierynomus.smbj.share.RingBuffer;
import com.hierynomus.smbj.share.SMB2Writer;
import java.io.IOException;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FileOutputStream
extends OutputStream {
    private SMB2Writer writer;
    private ProgressListener progressListener;
    private boolean isClosed = false;
    private ByteArrayProvider provider;
    private static final Logger logger = LoggerFactory.getLogger(FileOutputStream.class);

    FileOutputStream(SMB2Writer writer, int bufferSize, long offset, ProgressListener progressListener) {
        this.writer = writer;
        this.progressListener = progressListener;
        this.provider = new ByteArrayProvider(bufferSize, offset);
    }

    @Override
    public void write(int b) throws IOException {
        this.verifyConnectionNotClosed();
        if (this.provider.isBufferFull()) {
            this.flush();
        }
        if (!this.provider.isBufferFull()) {
            this.provider.writeByte(b);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        int writeLen;
        this.verifyConnectionNotClosed();
        int offset = off;
        int length = len;
        do {
            writeLen = Math.min(length, this.provider.maxSize());
            while (this.provider.isBufferFull(writeLen)) {
                this.flush();
            }
            if (!this.provider.isBufferFull()) {
                this.provider.writeBytes(b, offset, writeLen);
            }
            offset += writeLen;
        } while ((length -= writeLen) > 0);
    }

    @Override
    public void flush() throws IOException {
        this.verifyConnectionNotClosed();
        if (this.provider.isAvailable()) {
            this.sendWriteRequest();
        }
    }

    private void sendWriteRequest() {
        this.writer.write(this.provider, this.progressListener);
    }

    @Override
    public void close() throws IOException {
        while (this.provider.isAvailable()) {
            this.sendWriteRequest();
        }
        this.provider.reset();
        this.isClosed = true;
        this.writer = null;
        logger.debug("EOF, {} bytes written", (Object)this.provider.getOffset());
    }

    private void verifyConnectionNotClosed() throws IOException {
        if (this.isClosed) {
            throw new IOException("Stream is closed");
        }
    }

    private static class ByteArrayProvider
    extends ByteChunkProvider {
        private RingBuffer buf;

        private ByteArrayProvider(int maxWriteSize, long offset) {
            this.buf = new RingBuffer(maxWriteSize);
            this.offset = offset;
        }

        @Override
        public boolean isAvailable() {
            return this.buf != null && !this.buf.isEmpty();
        }

        @Override
        protected int getChunk(byte[] chunk) {
            return this.buf.read(chunk);
        }

        @Override
        public int bytesLeft() {
            return this.buf.size();
        }

        public void writeBytes(byte[] b, int off, int len) {
            this.buf.write(b, off, len);
        }

        public void writeByte(int b) {
            this.buf.write(b);
        }

        public boolean isBufferFull() {
            return this.buf.isFull();
        }

        public boolean isBufferFull(int len) {
            return this.buf.isFull(len);
        }

        public int maxSize() {
            return this.buf.maxSize();
        }

        private void reset() {
            this.buf = null;
        }
    }
}

