/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.mssmb2;

import com.hierynomus.mserref.NtStatus;
import com.hierynomus.mssmb2.SMB2Dialect;
import com.hierynomus.mssmb2.SMB2Error;
import com.hierynomus.mssmb2.SMB2Header;
import com.hierynomus.mssmb2.SMB2MessageCommandCode;
import com.hierynomus.mssmb2.SMB2MessageFlag;
import com.hierynomus.mssmb2.SMB2PacketData;
import com.hierynomus.protocol.commons.EnumWithValue;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.smb.SMBBuffer;
import com.hierynomus.smb.SMBPacket;
import com.hierynomus.smbj.common.Check;

public class SMB2Packet
extends SMBPacket<SMB2PacketData, SMB2Header> {
    public static final int SINGLE_CREDIT_PAYLOAD_SIZE = 65536;
    protected int structureSize;
    private SMBBuffer buffer;
    private SMB2Error error;
    private int messageEndPos;

    protected SMB2Packet() {
        super(new SMB2Header());
    }

    protected SMB2Packet(int structureSize, SMB2Dialect dialect, SMB2MessageCommandCode messageType) {
        this(structureSize, dialect, messageType, 0L, 0L);
    }

    protected SMB2Packet(int structureSize, SMB2Dialect dialect, SMB2MessageCommandCode messageType, long sessionId) {
        this(structureSize, dialect, messageType, sessionId, 0L);
    }

    protected SMB2Packet(int structureSize, SMB2Dialect dialect, SMB2MessageCommandCode messageType, long sessionId, long treeId) {
        super(new SMB2Header());
        this.structureSize = structureSize;
        ((SMB2Header)this.header).setDialect(dialect);
        ((SMB2Header)this.header).setMessageType(messageType);
        ((SMB2Header)this.header).setSessionId(sessionId);
        ((SMB2Header)this.header).setTreeId(treeId);
    }

    public long getSequenceNumber() {
        return ((SMB2Header)this.header).getMessageId();
    }

    public int getStructureSize() {
        return this.structureSize;
    }

    public SMBBuffer getBuffer() {
        return this.buffer;
    }

    public int getMessageStartPos() {
        return ((SMB2Header)this.header).getHeaderStartPosition();
    }

    public int getMessageEndPos() {
        return this.messageEndPos;
    }

    @Override
    public void write(SMBBuffer buffer) {
        ((SMB2Header)this.header).writeTo(buffer);
        this.writeTo(buffer);
    }

    protected void writeTo(SMBBuffer buffer) {
        throw new UnsupportedOperationException("Should be implemented by specific message type");
    }

    @Override
    protected final void read(SMB2PacketData packetData) throws Buffer.BufferException {
        this.buffer = packetData.getDataBuffer();
        this.header = packetData.getHeader();
        this.readMessage(this.buffer);
        this.messageEndPos = this.buffer.rpos();
    }

    final void readError(SMB2PacketData packetData) throws Buffer.BufferException {
        this.buffer = packetData.getDataBuffer();
        this.header = packetData.getHeader();
        this.error = new SMB2Error().read((SMB2Header)this.header, this.buffer);
        this.messageEndPos = (long)((SMB2Header)this.header).getNextCommandOffset() != 0L ? ((SMB2Header)this.header).getHeaderStartPosition() + ((SMB2Header)this.header).getNextCommandOffset() : this.buffer.wpos();
        Check.ensure(this.messageEndPos >= this.buffer.rpos(), "The message end position should be at or beyond the buffer read position");
        this.buffer.rpos(this.messageEndPos);
    }

    protected void readMessage(SMBBuffer buffer) throws Buffer.BufferException {
        throw new UnsupportedOperationException("Should be implemented by specific message type");
    }

    public final boolean isSuccess() {
        return this.error == null;
    }

    public boolean isIntermediateAsyncResponse() {
        return EnumWithValue.EnumUtils.isSet(((SMB2Header)this.header).getFlags(), SMB2MessageFlag.SMB2_FLAGS_ASYNC_COMMAND) && ((SMB2Header)this.header).getStatusCode() == NtStatus.STATUS_PENDING.getValue();
    }

    public int getMaxPayloadSize() {
        return 65536;
    }

    public int getCreditsAssigned() {
        return ((SMB2Header)this.getHeader()).getCreditCharge();
    }

    public void setCreditsAssigned(int creditsAssigned) {
        ((SMB2Header)this.getHeader()).setCreditCharge(creditsAssigned);
    }

    public SMB2Error getError() {
        return this.error;
    }

    public SMB2Packet getPacket() {
        return this;
    }

    public String toString() {
        return (Object)((Object)((SMB2Header)this.header).getMessage()) + " with message id << " + ((SMB2Header)this.header).getMessageId() + " >>";
    }
}

