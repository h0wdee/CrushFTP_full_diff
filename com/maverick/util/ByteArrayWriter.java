/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import com.maverick.util.ByteArrayReader;
import com.maverick.util.UnsignedInteger32;
import com.maverick.util.UnsignedInteger64;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class ByteArrayWriter
extends ByteArrayOutputStream {
    public ByteArrayWriter() {
    }

    public ByteArrayWriter(int length) {
        super(length);
    }

    public byte[] array() {
        return this.buf;
    }

    public void move(int numBytes) {
        this.count += numBytes;
    }

    public void writeBigInteger(BigInteger bi) throws IOException {
        byte[] raw = bi.toByteArray();
        this.writeInt(raw.length);
        this.write(raw);
    }

    public void writeBoolean(boolean b) {
        this.write(b ? 1 : 0);
    }

    public void writeBinaryString(byte[] data) throws IOException {
        if (data == null) {
            this.writeInt(0);
        } else {
            this.writeBinaryString(data, 0, data.length);
        }
    }

    public void writeBinaryString(byte[] data, int offset, int len) throws IOException {
        if (data == null) {
            this.writeInt(0);
        } else {
            this.writeInt(len);
            this.write(data, offset, len);
        }
    }

    public void writeMPINT(BigInteger b) {
        short bytes = (short)((b.bitLength() + 7) / 8);
        byte[] raw = b.toByteArray();
        this.writeShort((short)b.bitLength());
        if (raw[0] == 0) {
            this.write(raw, 1, bytes);
        } else {
            this.write(raw, 0, bytes);
        }
    }

    public void writeShort(short s) {
        this.write(s >>> 8 & 0xFF);
        this.write(s >>> 0 & 0xFF);
    }

    public void writeInt(long i) throws IOException {
        byte[] raw = new byte[]{(byte)(i >> 24), (byte)(i >> 16), (byte)(i >> 8), (byte)i};
        this.write(raw);
    }

    public void writeInt(int i) throws IOException {
        byte[] raw = new byte[]{(byte)(i >> 24), (byte)(i >> 16), (byte)(i >> 8), (byte)i};
        this.write(raw);
    }

    public static byte[] encodeInt(int i) {
        byte[] raw = new byte[]{(byte)(i >> 24), (byte)(i >> 16), (byte)(i >> 8), (byte)i};
        return raw;
    }

    public static void encodeInt(byte[] buf, int off, int i) {
        buf[off++] = (byte)(i >> 24);
        buf[off++] = (byte)(i >> 16);
        buf[off++] = (byte)(i >> 8);
        buf[off] = (byte)i;
    }

    public void writeUINT32(UnsignedInteger32 value) throws IOException {
        this.writeInt(value.longValue());
    }

    public void writeUINT64(UnsignedInteger64 value) throws IOException {
        byte[] raw = new byte[8];
        byte[] bi = ByteArrayWriter.stripLeadingZeros(value.bigIntValue().toByteArray());
        System.arraycopy(bi, 0, raw, raw.length - bi.length, bi.length);
        this.write(raw);
    }

    public static byte[] stripLeadingZeros(byte[] data) {
        int x;
        for (x = 0; x < data.length && data[x] == 0; ++x) {
        }
        if (x > 0) {
            byte[] tmp = new byte[data.length - x];
            System.arraycopy(data, x, tmp, 0, tmp.length);
            return tmp;
        }
        return data;
    }

    public void writeUINT64(long value) throws IOException {
        this.writeUINT64(new UnsignedInteger64(value));
    }

    public void writeString(String str) throws IOException {
        this.writeString(str, ByteArrayReader.getCharsetEncoding());
    }

    public void writeString(String str, String charset) throws IOException {
        if (str == null) {
            this.writeInt(0);
        } else {
            byte[] tmp = ByteArrayReader.encode ? str.getBytes(charset) : str.getBytes();
            this.writeInt(tmp.length);
            this.write(tmp);
        }
    }

    public void silentClose() {
        try {
            this.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public void dispose() {
        this.buf = null;
    }
}

