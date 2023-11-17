/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.mssmb2.copy;

import com.hierynomus.smb.SMBBuffer;
import java.util.ArrayList;
import java.util.List;

public class CopyChunkRequest {
    private static final long ctlCode = 1343730L;
    private byte[] resumeKey;
    private List<Chunk> chunks = new ArrayList<Chunk>();

    public CopyChunkRequest(byte[] resumeKey, List<Chunk> chunks) {
        this.resumeKey = resumeKey;
        this.chunks.addAll(chunks);
    }

    public static long getCtlCode() {
        return 1343730L;
    }

    public byte[] getResumeKey() {
        return this.resumeKey;
    }

    public List<Chunk> getChunks() {
        return this.chunks;
    }

    public void write(SMBBuffer buffer) {
        buffer.putRawBytes(this.getResumeKey());
        buffer.putUInt32(this.getChunks().size());
        buffer.putUInt32(0L);
        for (Chunk chunk : this.getChunks()) {
            buffer.putUInt64(chunk.getSrcOffset());
            buffer.putUInt64(chunk.getTgtOffset());
            buffer.putUInt32(chunk.getLength());
            buffer.putUInt32(0L);
        }
    }

    public static final class Chunk {
        private long srcOffset;
        private long tgtOffset;
        private long length;

        public Chunk(long srcOffset, long tgtOffset, long length) {
            this.srcOffset = srcOffset;
            this.tgtOffset = tgtOffset;
            this.length = length;
        }

        public long getSrcOffset() {
            return this.srcOffset;
        }

        public long getTgtOffset() {
            return this.tgtOffset;
        }

        public long getLength() {
            return this.length;
        }
    }
}

