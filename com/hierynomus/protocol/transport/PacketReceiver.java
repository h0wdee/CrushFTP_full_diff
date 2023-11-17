/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.protocol.transport;

import com.hierynomus.protocol.PacketData;
import com.hierynomus.protocol.transport.TransportException;

public interface PacketReceiver<D extends PacketData<?>> {
    public void handle(D var1) throws TransportException;

    public void handleError(Throwable var1);
}

