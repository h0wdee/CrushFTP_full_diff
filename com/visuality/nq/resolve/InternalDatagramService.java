/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import com.visuality.nq.common.TraceLog;
import com.visuality.nq.resolve.DatagramMessage;
import com.visuality.nq.resolve.NetbiosException;
import com.visuality.nq.resolve.NetbiosMessage;
import com.visuality.nq.resolve.NetbiosService;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

public class InternalDatagramService
extends NetbiosService {
    public InternalDatagramService(int port, InetAddress ip) throws SocketException {
        super(port, ip);
    }

    public InternalDatagramService(int port) throws SocketException {
        super(port);
    }

    public void doTimeout() throws NetbiosException {
    }

    public int getTimeout() {
        return 100000;
    }

    public NetbiosMessage createMessage(DatagramPacket packet) {
        DatagramMessage msg = new DatagramMessage(packet);
        TraceLog.get().message(msg, 250);
        msg.direction = 1;
        msg.setService(this);
        return msg;
    }
}

