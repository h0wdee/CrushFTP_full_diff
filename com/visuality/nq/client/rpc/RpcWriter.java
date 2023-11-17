/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client.rpc;

import com.visuality.nq.client.rpc.Dcerpc;
import com.visuality.nq.common.Buffer;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;

public class RpcWriter
extends BufferWriter {
    private Dcerpc rpc;
    private int fragLenPos = -1;
    private int flagsPos = -1;
    private boolean isFirst = true;
    private boolean isLast = false;

    protected RpcWriter(Dcerpc rpc, int size) {
        super(new byte[size + 16], 0, false);
        this.rpc = rpc;
        this.offset = 16;
    }

    protected RpcWriter(Dcerpc rpc) {
        super(new byte[rpc.maxXmit + 16], 0, false);
        this.rpc = rpc;
        this.writeHeader();
        this.offset = 16;
    }

    protected void send() throws NqException {
        Buffer buffer = new Buffer(this.dest, 0, this.offset);
        buffer.dataLen = this.offset;
        this.rpc.transport.write(buffer);
    }

    protected void sendLast() throws NqException {
        this.isLast = true;
        if (this.flagsPos > 0) {
            this.updateHeader();
        }
        Buffer buffer = new Buffer(this.dest, 0, this.offset);
        buffer.dataLen = this.offset;
        this.rpc.transport.write(buffer);
    }

    protected void writeHeader(int type, boolean first, boolean last, int stubLen) {
        int savedOffset = this.getOffset();
        this.setOffset(0);
        this.writeByte((byte)5);
        this.writeByte((byte)0);
        this.writeByte((byte)type);
        this.writeByte((byte)((first ? 1 : 0) | (last ? 2 : 0)));
        this.writeInt4(this.be ? 0 : 16);
        this.writeInt2(stubLen + 16);
        this.writeInt2(0);
        this.writeInt4(2);
        this.setOffset(savedOffset);
    }

    protected void writeHeader() {
        int savedOffset = this.getOffset();
        this.setOffset(0);
        this.writeByte((byte)5);
        this.writeByte((byte)0);
        this.writeByte((byte)0);
        this.flagsPos = this.getOffset();
        this.writeByte((byte)0);
        this.writeInt4(this.be ? 0 : 16);
        this.fragLenPos = this.getOffset();
        this.writeInt2(0);
        this.writeInt2(0);
        this.rpc.callId += 2;
        this.writeInt4(this.rpc.callId);
        this.setOffset(savedOffset);
    }

    protected void checkAvailability(int size) {
        if (this.offset + size >= this.dest.length) {
            this.updateHeader();
            try {
                this.send();
            }
            catch (NqException e) {
                TraceLog.get().error("NqException = ", e, 10, e.getErrCode());
            }
            this.offset = 16;
        }
    }

    private void updateHeader() {
        int fragLen = this.getOffset();
        this.writeInt2(this.fragLenPos, fragLen);
        byte flags = (byte)((this.isFirst ? 1 : 0) | (this.isLast ? 2 : 0));
        this.writeByte(this.flagsPos, flags);
        if (this.isFirst) {
            this.isFirst = false;
        }
    }

    public void writeCardinal(long value) throws NqException {
        this.rpc.transferSyntax.writeCardinal(this, value);
    }

    public void writeCardinal(int pos, long value) throws NqException {
        int savedPos = this.getOffset();
        this.setOffset(pos);
        this.rpc.transferSyntax.writeCardinal(this, value);
        this.setOffset(savedPos);
    }

    public void writeRpcString(String str, boolean nullTerm) throws NqException {
        int strLen = str.length() + 1;
        this.writeCardinal(strLen);
        this.writeCardinal(0L);
        this.writeCardinal(nullTerm ? (long)strLen : (long)(strLen - 1));
        this.writeString(str, nullTerm);
    }

    public void align() throws NqException {
        this.align(0, this.rpc.transferSyntax.align);
    }
}

