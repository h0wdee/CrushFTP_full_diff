/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client.rpc;

import com.visuality.nq.client.rpc.Dcerpc;
import com.visuality.nq.common.Buffer;
import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.NqException;
import java.nio.ByteOrder;

public class RpcReader
extends BufferReader {
    private Dcerpc rpc;
    private int lastIndex = -1;
    private int recvLen = 0;
    private static final int EXTRA_SIZE = 520;
    protected int packetType;
    protected int callId;

    protected RpcReader(Dcerpc rpc) throws NqException {
        super(new byte[rpc.maxRecv + 16 + 520], 0, false);
        this.rpc = rpc;
        this.recvLen = rpc.maxRecv + 16;
        this.checkAvailability(16);
    }

    protected RpcReader(Dcerpc rpc, int size) throws NqException {
        super(new byte[size + 16], 0, false);
        this.rpc = rpc;
        this.recvLen = size + 16;
        this.checkAvailability(16);
    }

    protected void checkAvailability(int size) throws NqException {
        if (this.lastIndex != -1 && this.offset + size <= this.lastIndex) {
            return;
        }
        int reminder = this.lastIndex == -1 ? 0 : this.lastIndex - this.offset;
        byte[] remainingBytes = null;
        if (reminder > 0) {
            remainingBytes = new byte[this.lastIndex - this.offset];
            System.arraycopy(this.src, this.offset, remainingBytes, 0, remainingBytes.length);
        }
        this.phantom += this.lastIndex == -1 ? 0 : this.lastIndex - reminder;
        this.recv(reminder <= 24 ? 0 : reminder - 16 - 8);
        this.skip(2);
        this.packetType = this.readByte();
        int temp = this.readByte();
        temp = this.readInt4();
        if (0 == (temp & 0x10) && ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            this.swap = true;
        }
        this.lastIndex = this.readInt2();
        this.skip(2);
        this.callId = this.readInt4();
        switch (this.packetType) {
            case 2: {
                this.skip(4);
                this.skip(2);
                this.skip(1);
                this.align(0, 2);
                break;
            }
        }
        if (null != remainingBytes) {
            this.offset -= remainingBytes.length;
            for (int i = 0; i < remainingBytes.length; ++i) {
                this.src[this.offset + i] = remainingBytes[i];
            }
        }
    }

    private void recv(int offset) throws NqException {
        long pos = 0L;
        this.rpc.setPosition(pos);
        Buffer buffer = new Buffer(this.src, offset, this.recvLen);
        int bytesRead = (int)this.rpc.read(buffer);
        this.lastIndex = bytesRead + offset;
        this.offset = offset;
    }

    public long readCardinal() throws NqException {
        return this.rpc.transferSyntax.readCardinal(this);
    }

    public String readReferencedString(boolean nullTerm) throws NqException {
        this.align(0, 4);
        long count = this.readCardinal();
        count = this.readCardinal();
        count = this.readCardinal();
        if (0L == count) {
            return "";
        }
        if (nullTerm) {
            --count;
        }
        String str = this.readString((int)count * 2);
        if (nullTerm) {
            this.skip(2);
        }
        this.align(0, 4);
        return str;
    }

    public void align() {
        try {
            this.align(0, this.rpc.transferSyntax.align);
        }
        catch (NqException e) {
            e.printStackTrace();
        }
    }
}

