/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.io;

import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.smbj.io.ByteChunkProvider;
import java.io.IOException;

public class BufferByteChunkProvider
extends ByteChunkProvider {
    private Buffer<?> buffer;

    public BufferByteChunkProvider(Buffer<?> buffer) {
        this.buffer = buffer;
    }

    @Override
    public boolean isAvailable() {
        return this.buffer.available() > 0;
    }

    @Override
    protected int getChunk(byte[] chunk) throws IOException {
        int toRead = chunk.length;
        if (this.buffer.available() < chunk.length) {
            toRead = this.buffer.available();
        }
        try {
            this.buffer.readRawBytes(chunk, 0, toRead);
        }
        catch (Buffer.BufferException e) {
            throw new IOException(e);
        }
        return toRead;
    }

    @Override
    public int bytesLeft() {
        return this.buffer.available();
    }
}

