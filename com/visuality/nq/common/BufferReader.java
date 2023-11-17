/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.HexBuilder;
import com.visuality.nq.common.NqException;
import java.io.UnsupportedEncodingException;

public class BufferReader {
    protected byte[] src;
    protected int offset;
    protected boolean swap;
    protected int phantom = 0;

    public BufferReader(byte[] src, int offset, boolean nbo) {
        this.swap = nbo;
        this.src = src;
        this.offset = offset;
    }

    protected void checkAvailability(int size) throws NqException {
    }

    public byte readByte() throws NqException {
        this.checkAvailability(1);
        return this.src[this.offset++];
    }

    public void readBytes(byte[] dest, int count) throws NqException {
        int i;
        this.checkAvailability(count);
        for (i = 0; i < count; ++i) {
            dest[i] = this.src[this.offset + i];
        }
        this.offset += i;
    }

    public short readInt2() throws NqException {
        short res;
        this.checkAvailability(2);
        if (this.swap) {
            res = (short)((0xFF & (short)this.src[this.offset++]) * 256);
            res = (short)(res + (0xFF & (short)this.src[this.offset++]));
        } else {
            res = (short)(0xFF & this.src[this.offset++]);
            res = (short)(res + (short)((0xFF & this.src[this.offset++]) * 256));
        }
        return res;
    }

    public int readInt4() throws NqException {
        int res;
        this.checkAvailability(4);
        if (this.swap) {
            res = (0xFF & this.src[this.offset++]) * 256 * 256 * 256;
            res += (0xFF & this.src[this.offset++]) * 256 * 256;
            res += (0xFF & this.src[this.offset++]) * 256;
            res += 0xFF & this.src[this.offset++];
        } else {
            res = 0xFF & this.src[this.offset++];
            res += (0xFF & this.src[this.offset++]) * 256;
            res += (0xFF & this.src[this.offset++]) * 256 * 256;
            res += (0xFF & this.src[this.offset++]) * 256 * 256 * 256;
        }
        return res;
    }

    public long readInt8() throws NqException {
        long res;
        this.checkAvailability(8);
        if (this.swap) {
            res = this.readInt4();
            res = 0x100000000L * res + (long)this.readInt4();
        } else {
            res = this.readInt4();
            res += 0x100000000L * (long)this.readInt4();
        }
        return res;
    }

    public long readLong() throws NqException {
        this.checkAvailability(8);
        long res = 0L;
        int i = 0;
        for (i = 0; i < 8 && this.offset + i < this.src.length; ++i) {
            if (this.swap) {
                res = (res << 8) + (long)(this.src[this.offset + i] & 0xFF);
                continue;
            }
            res += ((long)this.src[this.offset + i] & 0xFFL) << 8 * i;
        }
        this.offset += i;
        return res;
    }

    public int getOffset() {
        return this.offset;
    }

    public int getRemaining() {
        return this.src.length - this.offset;
    }

    public void skip(int bytesToSkip) throws NqException {
        this.checkAvailability(bytesToSkip);
        this.offset += bytesToSkip;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String readString(int size) throws NqException {
        String result;
        this.checkAvailability(size);
        try {
            result = new String(this.src, this.offset, size, "UTF-16LE");
        }
        catch (UnsupportedEncodingException e) {
            throw new NqException("Unsupported UTF-16", -22);
        }
        this.offset += size;
        return result;
    }

    public String readString() {
        String result;
        int size = 0;
        int origin = this.offset;
        while (this.src[this.offset] != 0 && this.src[this.offset + 1] == 0) {
            this.offset += 2;
        }
        size = this.offset - origin;
        try {
            result = new String(this.src, origin, size, "UTF-16LE");
        }
        catch (UnsupportedEncodingException e) {
            result = "";
        }
        this.offset += 2;
        return result;
    }

    public byte[] getSrc() {
        return this.src;
    }

    public void align(int hookOffset, int alignment) throws NqException {
        if (0 == alignment) {
            throw new NqException("Bad alignment argument", -20);
        }
        int delta = (this.offset - hookOffset + this.phantom) % alignment;
        if (delta > 0) {
            this.checkAvailability(delta);
            this.offset += alignment - delta;
        }
    }

    public void setSrc(byte[] src) {
        this.src = src;
    }

    public String toString() {
        return "BufferReader: [" + HexBuilder.toHex(this.src) + "]";
    }
}

