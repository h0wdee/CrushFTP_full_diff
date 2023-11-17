/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smb;

import com.hierynomus.protocol.commons.Charsets;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.protocol.commons.buffer.Endian;
import java.util.Arrays;

public class SMBBuffer
extends Buffer<SMBBuffer> {
    private static final byte[] RESERVED_2 = new byte[]{0, 0};
    private static final byte[] RESERVED_4 = new byte[]{0, 0, 0, 0};

    public SMBBuffer() {
        super(Endian.LE);
    }

    public SMBBuffer(byte[] data) {
        super(data, Endian.LE);
    }

    public Buffer<SMBBuffer> putReserved(int length) {
        byte[] nullBytes = new byte[length];
        Arrays.fill(nullBytes, (byte)0);
        this.putRawBytes(nullBytes);
        return this;
    }

    public Buffer<SMBBuffer> putReserved1() {
        this.putByte((byte)0);
        return this;
    }

    public Buffer<SMBBuffer> putReserved2() {
        this.putRawBytes(RESERVED_2);
        return this;
    }

    public Buffer<SMBBuffer> putReserved4() {
        this.putRawBytes(RESERVED_4);
        return this;
    }

    public Buffer<SMBBuffer> putString(String string) {
        return this.putString(string, Charsets.UTF_16);
    }

    public Buffer<SMBBuffer> putStringLengthUInt16(String string) {
        if (string == null) {
            return this.putUInt16(0);
        }
        return this.putUInt16(string.length() * 2);
    }
}

