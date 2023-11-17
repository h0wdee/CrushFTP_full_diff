/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.protocol;

import com.hierynomus.protocol.commons.buffer.Buffer;

public interface PacketData<B extends Buffer<B>> {
    public B getDataBuffer();
}

