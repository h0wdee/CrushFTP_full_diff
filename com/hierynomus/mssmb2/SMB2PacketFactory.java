/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.mssmb2;

import com.hierynomus.mssmb2.SMB2PacketData;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.protocol.transport.PacketFactory;

public class SMB2PacketFactory
implements PacketFactory<SMB2PacketData> {
    @Override
    public SMB2PacketData read(byte[] data) throws Buffer.BufferException {
        return new SMB2PacketData(data);
    }

    @Override
    public boolean canHandle(byte[] data) {
        return data[0] == -2 && data[1] == 83 && data[2] == 77 && data[3] == 66;
    }
}

