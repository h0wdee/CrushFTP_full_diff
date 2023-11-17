/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.mssmb2.messages;

import com.hierynomus.mssmb2.SMB2Dialect;
import com.hierynomus.mssmb2.SMB2GlobalCapability;
import com.hierynomus.mssmb2.SMB2MessageCommandCode;
import com.hierynomus.mssmb2.SMB2Packet;
import com.hierynomus.protocol.commons.EnumWithValue;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.smb.SMBBuffer;
import java.util.Set;

public class SMB2SessionSetup
extends SMB2Packet {
    private SMB2Dialect negotiatedDialect;
    private byte securityMode;
    private long clientCapabilities;
    private byte[] securityBuffer;
    private long previousSessionId;
    private Set<SMB2SessionFlags> sessionFlags;

    public SMB2SessionSetup() {
    }

    public SMB2SessionSetup(SMB2Dialect negotiatedDialect, Set<SMB2SecurityMode> securityMode, Set<SMB2GlobalCapability> capabilities) {
        super(25, negotiatedDialect, SMB2MessageCommandCode.SMB2_SESSION_SETUP);
        this.negotiatedDialect = negotiatedDialect;
        this.securityMode = (byte)EnumWithValue.EnumUtils.toLong(securityMode);
        this.clientCapabilities = EnumWithValue.EnumUtils.toLong(capabilities);
    }

    @Override
    protected void writeTo(SMBBuffer buffer) {
        buffer.putUInt16(this.structureSize);
        this.putFlags(buffer);
        buffer.putByte(this.securityMode);
        buffer.putUInt32(this.clientCapabilities & 1L);
        buffer.putReserved4();
        buffer.putUInt16(88);
        buffer.putUInt16(this.securityBuffer != null ? this.securityBuffer.length : 0);
        buffer.putUInt64(this.previousSessionId);
        if (this.securityBuffer != null) {
            buffer.putRawBytes(this.securityBuffer);
        }
    }

    @Override
    protected void readMessage(SMBBuffer buffer) throws Buffer.BufferException {
        buffer.readUInt16();
        this.sessionFlags = EnumWithValue.EnumUtils.toEnumSet(buffer.readUInt16(), SMB2SessionFlags.class);
        int securityBufferOffset = buffer.readUInt16();
        int securityBufferLength = buffer.readUInt16();
        this.securityBuffer = this.readSecurityBuffer(buffer, securityBufferOffset, securityBufferLength);
    }

    private byte[] readSecurityBuffer(SMBBuffer buffer, int securityBufferOffset, int securityBufferLength) throws Buffer.BufferException {
        if (securityBufferLength > 0) {
            buffer.rpos(securityBufferOffset);
            return buffer.readRawBytes(securityBufferLength);
        }
        return new byte[0];
    }

    private void putFlags(SMBBuffer buffer) {
        if (this.negotiatedDialect.isSmb3x() && this.previousSessionId != 0L) {
            buffer.putByte((byte)1);
        } else {
            buffer.putByte((byte)0);
        }
    }

    public Set<SMB2SessionFlags> getSessionFlags() {
        return this.sessionFlags;
    }

    public void setPreviousSessionId(long previousSessionId) {
        this.previousSessionId = previousSessionId;
    }

    public void setSecurityBuffer(byte[] securityBuffer) {
        this.securityBuffer = securityBuffer;
    }

    public byte[] getSecurityBuffer() {
        return this.securityBuffer;
    }

    public static enum SMB2SecurityMode implements EnumWithValue<SMB2SecurityMode>
    {
        SMB2_NEGOTIATE_SIGNING_ENABLED(1L),
        SMB2_NEGOTIATE_SIGNING_REQUIRED(2L);

        private long value;

        private SMB2SecurityMode(long value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return this.value;
        }
    }

    public static enum SMB2SessionFlags implements EnumWithValue<SMB2SessionFlags>
    {
        SMB2_SESSION_FLAG_IS_GUEST(1L),
        SMB2_SESSION_FLAG_IS_NULL(2L),
        SMB2_SESSION_FLAG_ENCRYPT_DATA(4L);

        private long value;

        private SMB2SessionFlags(long value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return this.value;
        }
    }
}

