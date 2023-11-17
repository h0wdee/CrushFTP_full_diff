/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import com.maverick.util.UnsignedInteger32;
import com.maverick.util.UnsignedInteger64;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

public class ByteArrayReader
extends ByteArrayInputStream {
    private static String CHARSET_ENCODING = "UTF8";
    public static boolean encode;

    public ByteArrayReader(byte[] buffer, int start, int len) {
        super(buffer, start, len);
    }

    public ByteArrayReader(byte[] buffer) {
        super(buffer, 0, buffer.length);
    }

    public byte[] array() {
        return this.buf;
    }

    public static void setCharsetEncoding(String charset) {
        try {
            String test = "123456890";
            test.getBytes(charset);
            CHARSET_ENCODING = charset;
            encode = true;
        }
        catch (UnsupportedEncodingException ex) {
            CHARSET_ENCODING = "";
            encode = false;
        }
    }

    public static String getCharsetEncoding() {
        return CHARSET_ENCODING;
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        int count;
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        this.checkLength(len);
        for (int n = 0; n < len; n += count) {
            count = this.read(b, off + n, len - n);
            if (count >= 0) continue;
            throw new EOFException("Could not read number of bytes requested: " + len + ", got " + n + " into buffer size " + b.length + " at offset " + off);
        }
    }

    public boolean readBoolean() throws IOException {
        return this.read() == 1;
    }

    public void readFully(byte[] b) throws IOException {
        this.readFully(b, 0, b.length);
    }

    public BigInteger readBigInteger() throws IOException {
        int len = (int)this.readInt();
        this.checkLength(len);
        byte[] raw = new byte[len];
        this.readFully(raw);
        return new BigInteger(raw);
    }

    public UnsignedInteger64 readUINT64() throws IOException {
        byte[] raw = new byte[9];
        this.readFully(raw, 1, 8);
        return new UnsignedInteger64(raw);
    }

    public UnsignedInteger32 readUINT32() throws IOException {
        return new UnsignedInteger32(this.readInt());
    }

    public static long readInt(byte[] data, int start) {
        long ret = (long)(data[start] & 0xFF) << 24 & 0xFFFFFFFFL | (long)((data[start + 1] & 0xFF) << 16) | (long)((data[start + 2] & 0xFF) << 8) | (long)((data[start + 3] & 0xFF) << 0);
        return ret;
    }

    public static short readShort(byte[] data, int start) {
        short ret = (short)((data[start] & 0xFF) << 8 | (data[start + 1] & 0xFF) << 0);
        return ret;
    }

    private void checkLength(long len) throws IOException {
        if (len > (long)this.available()) {
            throw new IOException(String.format("Unexpected length of %d bytes exceeds available data of %d bytes", len, this.available()));
        }
    }

    public byte[] readBinaryString() throws IOException {
        int len = (int)this.readInt();
        this.checkLength(len);
        byte[] buf = new byte[len];
        this.readFully(buf);
        return buf;
    }

    public long readInt() throws IOException {
        int ch4;
        int ch3;
        int ch2;
        int ch1 = this.read();
        if ((ch1 | (ch2 = this.read()) | (ch3 = this.read()) | (ch4 = this.read())) < 0) {
            throw new EOFException();
        }
        return (long)((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0)) & 0xFFFFFFFFL;
    }

    public String readString() throws IOException {
        return this.readString(CHARSET_ENCODING);
    }

    public String readString(String charset) throws IOException {
        long len = this.readInt();
        this.checkLength(len);
        byte[] raw = new byte[(int)len];
        this.readFully(raw);
        if (encode) {
            return new String(raw, charset);
        }
        return new String(raw);
    }

    public short readShort() throws IOException {
        int ch2;
        int ch1 = this.read();
        if ((ch1 | (ch2 = this.read())) < 0) {
            throw new EOFException();
        }
        return (short)((ch1 << 8) + (ch2 << 0));
    }

    public BigInteger readMPINT32() throws IOException {
        int bits = (int)this.readInt();
        this.checkLength((bits + 7) / 8);
        byte[] raw = new byte[(bits + 7) / 8 + 1];
        raw[0] = 0;
        this.readFully(raw, 1, raw.length - 1);
        return new BigInteger(raw);
    }

    public BigInteger readMPINT() throws IOException {
        short bits = this.readShort();
        this.checkLength((bits + 7) / 8);
        byte[] raw = new byte[(bits + 7) / 8 + 1];
        raw[0] = 0;
        this.readFully(raw, 1, raw.length - 1);
        return new BigInteger(raw);
    }

    public int getPosition() {
        return this.pos;
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

    static {
        ByteArrayReader.setCharsetEncoding(CHARSET_ENCODING);
    }
}

