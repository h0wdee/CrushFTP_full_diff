/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import com.visuality.nq.common.NqException;
import com.visuality.nq.resolve.NameMessage;
import com.visuality.nq.resolve.NetbiosMessage;
import com.visuality.nq.resolve.NetbiosService;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

public class ExternalNameService
extends NetbiosService {
    public ExternalNameService() throws SocketException {
    }

    public ExternalNameService(int port, InetAddress ip) throws SocketException {
        super(port, ip);
    }

    public ExternalNameService(int port) throws SocketException {
        super(port);
    }

    public void doTimeout() throws NqException {
        NetbiosMessage.consumeExpiredMessages(this, NameMessage.class);
    }

    public int getTimeout() {
        return 1000;
    }

    public NetbiosMessage createMessage(DatagramPacket packet) {
        NameMessage msg = new NameMessage(packet);
        msg.direction = 2;
        msg.setService(this);
        return msg;
    }

    public void consumeExpiredMessage(NetbiosMessage msg) throws NqException {
        if (0 == --msg.count) {
            msg.removePending(msg.status);
        }
    }
}

