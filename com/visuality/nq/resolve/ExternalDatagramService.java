/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import com.visuality.nq.resolve.DatagramMessage;
import com.visuality.nq.resolve.NetbiosException;
import com.visuality.nq.resolve.NetbiosMessage;
import com.visuality.nq.resolve.NetbiosService;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

public class ExternalDatagramService
extends NetbiosService {
    public ExternalDatagramService() throws SocketException {
    }

    public ExternalDatagramService(int port, InetAddress ip) throws SocketException {
        super(port, ip);
    }

    public ExternalDatagramService(int port) throws SocketException {
        super(port);
    }

    public void doTimeout() throws NetbiosException {
    }

    public int getTimeout() {
        return 10000;
    }

    public NetbiosMessage createMessage(DatagramPacket packet) {
        DatagramMessage msg = new DatagramMessage(packet);
        msg.direction = 2;
        msg.setService(this);
        return msg;
    }
}

