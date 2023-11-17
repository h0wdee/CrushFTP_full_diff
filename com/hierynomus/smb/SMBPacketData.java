/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smb;

import com.hierynomus.protocol.PacketData;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.smb.SMBBuffer;
import com.hierynomus.smb.SMBHeader;

public abstract class SMBPacketData<H extends SMBHeader>
implements PacketData<SMBBuffer> {
    private H header;
    protected SMBBuffer dataBuffer;

    public SMBPacketData(H header, byte[] data) throws Buffer.BufferException {
        this.header = header;
        this.dataBuffer = new SMBBuffer(data);
        this.readHeader();
    }

    protected void readHeader() throws Buffer.BufferException {
        this.header.readFrom(this.dataBuffer);
    }

    public H getHeader() {
        return this.header;
    }

    @Override
    public SMBBuffer getDataBuffer() {
        return this.dataBuffer;
    }
}

