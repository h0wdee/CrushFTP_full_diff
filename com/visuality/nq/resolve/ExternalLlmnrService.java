/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import com.visuality.nq.resolve.NetbiosException;
import com.visuality.nq.resolve.NetbiosMessage;
import com.visuality.nq.resolve.NetbiosService;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

public class ExternalLlmnrService
extends NetbiosService {
    public ExternalLlmnrService() throws SocketException {
    }

    public ExternalLlmnrService(int port, InetAddress ip) throws SocketException {
        super(port, ip);
    }

    public ExternalLlmnrService(int port) throws SocketException {
        super(port);
    }

    public void doTimeout() throws NetbiosException {
    }

    public int getTimeout() {
        return 100000;
    }

    public NetbiosMessage createMessage(DatagramPacket packet) {
        return null;
    }
}

