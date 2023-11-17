/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.transport.tcp.direct;

import com.hierynomus.protocol.Packet;
import com.hierynomus.protocol.PacketData;
import com.hierynomus.protocol.transport.PacketHandlers;
import com.hierynomus.protocol.transport.TransportLayer;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.transport.TransportLayerFactory;
import com.hierynomus.smbj.transport.tcp.direct.DirectTcpTransport;

public class DirectTcpTransportFactory<D extends PacketData<?>, P extends Packet<?>>
implements TransportLayerFactory<D, P> {
    @Override
    public TransportLayer<P> createTransportLayer(PacketHandlers<D, P> handlers, SmbConfig config) {
        return new DirectTcpTransport<D, P>(config.getSocketFactory(), config.getSoTimeout(), handlers);
    }
}

