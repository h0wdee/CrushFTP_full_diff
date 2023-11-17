/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.HexBuilder;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.UUID;

public class BufferWriter {
    protected byte[] dest;
    protected boolean swap;
    protected int offset;
    protected boolean be;

    public BufferWriter(byte[] dest, int offset, boolean nbo) {
        this.swap = nbo;
        this.offset = offset;
        this.dest = dest;
    }

    protected void checkAvailability(int size) {
    }

    public void align(int hookOffset, int alignment) throws NqException {
        if (0 == alignment) {
            throw new NqException("Bad alignment argument", -20);
        }
        int delta = (this.offset - hookOffset) % alignment;
        if (delta > 0) {
            this.checkAvailability(delta);
            this.offset += alignment - delta;
        }
    }

    public void writeString(String txt, boolean nullTerm) {
        this.checkAvailability(2 * txt.length() + (nullTerm ? 2 : 0));
        for (int i = 0; i < txt.length(); ++i) {
            char c = txt.charAt(i);
            this.writeInt2((short)c);
        }
        if (nullTerm) {
            this.writeInt2(0);
        }
    }

    public void skip(int bytesToSkip) {
        this.checkAvailability(bytesToSkip);
        this.offset += bytesToSkip;
    }

    public void writeInt2(int data) {
        data &= 0xFFFF;
        this.checkAvailability(2);
        if (this.swap) {
            this.dest[this.offset++] = (byte)(data / 256);
            this.dest[this.offset++] = (byte)(data % 256);
        } else {
            this.dest[this.offset++] = (byte)(data % 256);
            this.dest[this.offset++] = (byte)(data / 256);
        }
    }

    public void writeInt2(int offset, int data) {
        int temp = this.getOffset();
        this.setOffset(offset);
        this.writeInt2(data &= 0xFFFF);
        this.setOffset(temp);
    }

    public void writeInt4(int data) {
        this.checkAvailability(4);
        if (this.swap) {
            this.dest[this.offset++] = (byte)(data >> 24 & 0xFF);
            this.dest[this.offset++] = (byte)(data >> 16 & 0xFF);
            this.dest[this.offset++] = (byte)(data >> 8 & 0xFF);
            this.dest[this.offset++] = (byte)(data % 256);
        } else {
            this.dest[this.offset++] = (byte)(data % 256);
            this.dest[this.offset++] = (byte)(data >> 8 & 0xFF);
            this.dest[this.offset++] = (byte)(data >> 16 & 0xFF);
            this.dest[this.offset++] = (byte)(data >> 24 & 0xFF);
        }
    }

    public void writeInt4(int offset, int data) {
        int temp = this.getOffset();
        this.setOffset(offset);
        this.writeInt4(data);
        this.setOffset(temp);
    }

    public void writeInt8(long val) {
        this.checkAvailability(8);
        if (this.swap) {
            this.writeInt4((int)(val / 0x100000000L));
            this.writeInt4((int)(val % 0x100000000L));
        } else {
            this.writeInt4((int)(val % 0x100000000L));
            this.writeInt4((int)(val / 0x100000000L));
        }
    }

    public void writeLong(long data) {
        this.checkAvailability(8);
        if (this.swap) {
            this.dest[this.offset++] = (byte)(data >> 56 & 0xFFL);
            this.dest[this.offset++] = (byte)(data >> 48 & 0xFFL);
            this.dest[this.offset++] = (byte)(data >> 40 & 0xFFL);
            this.dest[this.offset++] = (byte)(data >> 32 & 0xFFL);
            this.dest[this.offset++] = (byte)(data >> 24 & 0xFFL);
            this.dest[this.offset++] = (byte)(data >> 16 & 0xFFL);
            this.dest[this.offset++] = (byte)(data >> 8 & 0xFFL);
            this.dest[this.offset++] = (byte)(data % 256L);
        } else {
            this.dest[this.offset++] = (byte)(data % 256L);
            this.dest[this.offset++] = (byte)(data >> 8 & 0xFFL);
            this.dest[this.offset++] = (byte)(data >> 16 & 0xFFL);
            this.dest[this.offset++] = (byte)(data >> 24 & 0xFFL);
            this.dest[this.offset++] = (byte)(data >> 32 & 0xFFL);
            this.dest[this.offset++] = (byte)(data >> 40 & 0xFFL);
            this.dest[this.offset++] = (byte)(data >> 48 & 0xFFL);
            this.dest[this.offset++] = (byte)(data >> 56 & 0xFFL);
        }
    }

    public void writeBytes(byte[] data, int size) {
        this.checkAvailability(size);
        for (int i = 0; i < data.length && i < size; ++i) {
            this.dest[this.offset++] = data[i];
        }
    }

    public void writeByte(byte data) {
        this.checkAvailability(1);
        this.dest[this.offset++] = data;
    }

    public void writeByte(int offset, byte data) {
        int temp = this.getOffset();
        this.setOffset(offset);
        this.writeByte(data);
        this.setOffset(temp);
    }

    public void writeBytes(byte[] data) {
        this.checkAvailability(data.length);
        for (int i = 0; i < data.length; ++i) {
            this.dest[this.offset++] = data[i];
        }
    }

    public void writeUuid(UUID uuid) {
        this.writeInt4(uuid.timeLow);
        this.writeInt2(uuid.timeHiVersion);
        this.writeInt2(uuid.timeMid);
        this.writeBytes(uuid.clockSeq, 2);
        this.writeBytes(uuid.node, 6);
    }

    public void writeZeros(int num) {
        this.checkAvailability(num);
        for (int i = 0; i < num; ++i) {
            this.dest[this.offset++] = 0;
        }
    }

    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public byte[] getDest() {
        return this.dest;
    }

    public void setDest(byte[] dest) {
        this.dest = dest;
    }

    public String toString() {
        return "BufferWriter: [" + HexBuilder.toHex(this.dest, this.getOffset()) + "]";
    }
}

