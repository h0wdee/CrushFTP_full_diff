/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.protocol.transport;

import com.hierynomus.protocol.Packet;
import com.hierynomus.protocol.transport.TransportException;
import java.io.IOException;
import java.net.InetSocketAddress;

public interface TransportLayer<P extends Packet<?>> {
    public void write(P var1) throws TransportException;

    public void connect(InetSocketAddress var1) throws IOException;

    public void disconnect() throws IOException;

    public boolean isConnected();
}

