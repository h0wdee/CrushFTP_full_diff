/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.transport;

import com.hierynomus.protocol.Packet;
import com.hierynomus.protocol.PacketData;
import com.hierynomus.protocol.transport.PacketHandlers;
import com.hierynomus.protocol.transport.TransportLayer;
import com.hierynomus.smbj.SmbConfig;

public interface TransportLayerFactory<D extends PacketData<?>, P extends Packet<?>> {
    public TransportLayer<P> createTransportLayer(PacketHandlers<D, P> var1, SmbConfig var2);
}

