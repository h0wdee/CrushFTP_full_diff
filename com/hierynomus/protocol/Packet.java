/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.protocol;

import com.hierynomus.protocol.commons.buffer.Buffer;

public interface Packet<B extends Buffer<B>> {
    public void write(B var1);

    public void read(B var1) throws Buffer.BufferException;
}

