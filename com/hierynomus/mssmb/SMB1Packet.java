/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.mssmb;

import com.hierynomus.mssmb.SMB1Header;
import com.hierynomus.mssmb.SMB1PacketData;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.smb.SMBBuffer;
import com.hierynomus.smb.SMBPacket;

public class SMB1Packet
extends SMBPacket<SMB1PacketData, SMB1Header> {
    protected SMB1Packet() {
        super(new SMB1Header());
    }

    @Override
    public final void write(SMBBuffer buffer) {
        ((SMB1Header)this.header).writeTo(buffer);
        this.writeTo(buffer);
    }

    protected void writeTo(SMBBuffer buffer) {
        throw new UnsupportedOperationException("Should be implemented by specific message type");
    }

    @Override
    protected void read(SMB1PacketData packetData) throws Buffer.BufferException {
        throw new UnsupportedOperationException("Receiving SMBv1 Messages not supported in SMBJ");
    }
}

