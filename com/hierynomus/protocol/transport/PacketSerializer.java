/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.protocol.transport;

import com.hierynomus.protocol.Packet;
import com.hierynomus.protocol.commons.buffer.Buffer;

public interface PacketSerializer<P extends Packet<B>, B extends Buffer<B>> {
    public B write(P var1);
}

