/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smb;

import com.hierynomus.protocol.Packet;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.smb.SMBBuffer;
import com.hierynomus.smb.SMBHeader;
import com.hierynomus.smb.SMBPacketData;

public abstract class SMBPacket<D extends SMBPacketData<H>, H extends SMBHeader>
implements Packet<SMBBuffer> {
    protected H header;

    public SMBPacket(H header) {
        this.header = header;
    }

    public H getHeader() {
        return this.header;
    }

    @Override
    protected abstract void read(D var1) throws Buffer.BufferException;

    @Override
    public final void read(SMBBuffer buffer) throws Buffer.BufferException {
        throw new UnsupportedOperationException("Call read(D extends PacketData<H>) instead of this method");
    }
}

