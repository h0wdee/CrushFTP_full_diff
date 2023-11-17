/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import java.io.IOException;

public class SimpleASNReader {
    private byte[] data;
    private int offset;

    public SimpleASNReader(byte[] data) {
        this.data = data;
        this.offset = 0;
    }

    public void assertByte(int b) throws IOException {
        int x = this.getByte();
        if (x != b) {
            throw new IOException("Assertion failed, next byte value is " + Integer.toHexString(x) + " instead of asserted " + Integer.toHexString(b));
        }
    }

    public int getByte() {
        return this.data[this.offset++] & 0xFF;
    }

    public byte[] getData() {
        int length = this.getLength();
        return this.getData(length);
    }

    public int getLength() {
        int b;
        if (((b = this.data[this.offset++] & 0xFF) & 0x80) != 0) {
            int length = 0;
            for (int bytes = b & 0x7F; bytes > 0; --bytes) {
                length <<= 8;
                length |= this.data[this.offset++] & 0xFF;
            }
            return length;
        }
        return b;
    }

    private byte[] getData(int length) {
        byte[] result = new byte[length];
        System.arraycopy(this.data, this.offset, result, 0, length);
        this.offset += length;
        return result;
    }

    public boolean hasMoreData() {
        return this.offset < this.data.length;
    }
}

