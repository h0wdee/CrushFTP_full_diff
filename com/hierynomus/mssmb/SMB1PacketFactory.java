/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.mssmb;

import com.hierynomus.mssmb.SMB1NotSupportedException;
import com.hierynomus.mssmb.SMB1PacketData;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.protocol.transport.PacketFactory;
import java.io.IOException;

public class SMB1PacketFactory
implements PacketFactory<SMB1PacketData> {
    @Override
    public SMB1PacketData read(byte[] data) throws Buffer.BufferException, IOException {
        throw new SMB1NotSupportedException();
    }

    @Override
    public boolean canHandle(byte[] data) {
        return data[0] == -1 && data[1] == 83 && data[2] == 77 && data[3] == 66;
    }
}

