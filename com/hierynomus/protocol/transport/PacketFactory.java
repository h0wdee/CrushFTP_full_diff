/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.protocol.transport;

import com.hierynomus.protocol.PacketData;
import com.hierynomus.protocol.commons.buffer.Buffer;
import java.io.IOException;

public interface PacketFactory<D extends PacketData<?>> {
    public D read(byte[] var1) throws Buffer.BufferException, IOException;

    public boolean canHandle(byte[] var1);
}

