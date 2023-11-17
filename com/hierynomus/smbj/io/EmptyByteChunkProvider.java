/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.io;

import com.hierynomus.smbj.io.ByteChunkProvider;

public class EmptyByteChunkProvider
extends ByteChunkProvider {
    public EmptyByteChunkProvider(long fileOffset) {
        this.offset = fileOffset;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    protected int getChunk(byte[] chunk) {
        return 0;
    }

    @Override
    public int bytesLeft() {
        return 0;
    }
}

