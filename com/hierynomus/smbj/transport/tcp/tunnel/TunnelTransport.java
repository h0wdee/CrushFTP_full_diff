/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.transport.tcp.tunnel;

import com.hierynomus.protocol.Packet;
import com.hierynomus.protocol.transport.TransportException;
import com.hierynomus.protocol.transport.TransportLayer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class TunnelTransport<P extends Packet<?>>
implements TransportLayer<P> {
    private TransportLayer<P> tunnel;
    private String tunnelHost;
    private int tunnelPort;

    public TunnelTransport(TransportLayer<P> tunnel, String tunnelHost, int tunnelPort) {
        this.tunnel = tunnel;
        this.tunnelHost = tunnelHost;
        this.tunnelPort = tunnelPort;
    }

    @Override
    public void write(P packet) throws TransportException {
        this.tunnel.write(packet);
    }

    @Override
    public void connect(InetSocketAddress remoteAddress) throws IOException {
        InetSocketAddress localAddress = new InetSocketAddress(this.tunnelHost, this.tunnelPort);
        this.tunnel.connect(localAddress);
    }

    @Override
    public void disconnect() throws IOException {
        this.tunnel.disconnect();
    }

    @Override
    public boolean isConnected() {
        return this.tunnel.isConnected();
    }
}

