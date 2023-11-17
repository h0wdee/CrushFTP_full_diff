/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import com.visuality.nq.resolve.NetbiosException;
import com.visuality.nq.resolve.NetbiosMessage;
import java.net.DatagramPacket;

public class SessionMessage
extends NetbiosMessage {
    public SessionMessage(DatagramPacket packet) {
        super(false, packet);
    }

    public void process() throws NetbiosException {
    }
}

