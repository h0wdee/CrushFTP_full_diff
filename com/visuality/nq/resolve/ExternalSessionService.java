/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import com.visuality.nq.resolve.NetbiosException;
import com.visuality.nq.resolve.NetbiosMessage;
import com.visuality.nq.resolve.NetbiosService;
import com.visuality.nq.resolve.SessionMessage;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

public class ExternalSessionService
extends NetbiosService {
    public ExternalSessionService() throws SocketException {
    }

    public ExternalSessionService(int port, InetAddress ip) throws SocketException {
        super(port, ip);
    }

    public ExternalSessionService(int port) throws SocketException {
        super(port);
    }

    public void doTimeout() throws NetbiosException {
    }

    public int getTimeout() {
        return 100000;
    }

    public NetbiosMessage createMessage(DatagramPacket packet) {
        SessionMessage msg = new SessionMessage(packet);
        msg.direction = 2;
        msg.setService(this);
        return msg;
    }
}

