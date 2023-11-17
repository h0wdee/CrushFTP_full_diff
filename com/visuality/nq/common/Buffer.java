/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.HexBuilder;

public class Buffer {
    public byte[] data;
    public int dataLen;
    public int remaining;
    public int offset;
    private int originalOffset;
    private int originalSize;
    private static final int SMALLSIZE = 1024;
    private static final int MIDSIZE = 65536;
    private static final int BIGSIZE = 0x100000;

    public Buffer() {
        this.data = null;
        this.remaining = 0;
        this.dataLen = 0;
        this.offset = 0;
    }

    public Buffer(int size) {
        this.data = new byte[size];
        this.remaining = size;
        this.dataLen = size;
        this.offset = 0;
    }

    public Buffer(byte[] data, int off, int len) {
        this.data = data;
        this.offset = off;
        this.dataLen = len;
    }

    public static Buffer getNewBuffer(int size) {
        size = size <= 1024 ? 1024 : (size <= 65536 ? 65536 : 0x100000);
        return new Buffer(size);
    }

    public void save() {
        this.originalOffset = this.offset;
        this.originalSize = this.dataLen;
    }

    public void restore() {
        this.offset = this.originalOffset;
        this.dataLen = this.originalSize;
    }

    public String toString() {
        return "Buffer: [" + HexBuilder.toHex(this.data) + "]";
    }
}

