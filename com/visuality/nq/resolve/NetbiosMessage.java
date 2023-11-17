/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import com.visuality.nq.common.CaptureInternal;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.resolve.NetbiosException;
import com.visuality.nq.resolve.NetbiosService;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public abstract class NetbiosMessage {
    static final int DATAGRAM_LENGTH = 1460;
    private static Map pendingOperations = new HashMap();
    static int nextTranId = 0;
    static Object tranIdGuard = new Object();
    protected DatagramPacket packet;
    protected int tranId;
    protected NetbiosMessage origin;
    protected int count = 0;
    protected long eol;
    protected int status;
    protected NetbiosService service;
    protected boolean multicast = false;
    protected int direction;
    protected int port;
    protected static final int DIRECTION_INTERNAL = 1;
    protected static final int DIRECTION_EXTERNAL = 2;
    protected static final int DIRECTION_PENDING = 3;
    CaptureInternal capture = null;

    public void setService(NetbiosService service) {
        this.service = service;
    }

    public NetbiosMessage(boolean isMulticast) {
        this.multicast = isMulticast;
        this.create(new DatagramPacket(new byte[1460], 1460), null);
    }

    public NetbiosMessage(boolean isMulticast, DatagramPacket packet) {
        this.multicast = isMulticast;
        this.create(packet, null);
    }

    public NetbiosMessage(boolean isMulticast, NetbiosMessage origin) {
        this.multicast = isMulticast;
        this.create(new DatagramPacket(new byte[1460], 1460), origin);
    }

    public long getEol() {
        return this.eol;
    }

    public NetbiosMessage getOrigin() {
        return this.origin;
    }

    public void setEol(long eol) {
        this.eol = eol;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void create(DatagramPacket packet, NetbiosMessage origin) {
        Object object = tranIdGuard;
        synchronized (object) {
            this.tranId = ++nextTranId;
        }
        this.packet = packet;
        this.origin = origin;
        this.count = 0;
        this.status = 0;
        if (null != origin) {
            ++origin.count;
        }
        this.capture = new CaptureInternal();
    }

    public int getTranId() {
        return this.tranId;
    }

    public DatagramPacket getPacket() {
        return this.packet;
    }

    public void setPacket(DatagramPacket packet) {
        this.packet = packet;
    }

    public abstract void process() throws NqException;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addPending() {
        Integer tranId = this.tranId;
        Map map = pendingOperations;
        synchronized (map) {
            TraceLog.get().message("tranId:" + tranId + ", ", this, 5);
            pendingOperations.put(tranId, this);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removePending(int status) throws NqException {
        if (status != 0 && this.status == 0) {
            this.status = status;
        }
        if (0 == this.count) {
            this.wasResponded();
        }
        Map map = pendingOperations;
        synchronized (map) {
            Integer tranId = this.tranId;
            NetbiosMessage theResult = (NetbiosMessage)pendingOperations.get(tranId);
            TraceLog.get().message("tranId:" + tranId + "," + theResult + "   removed from the map", 2000);
            pendingOperations.remove(tranId);
        }
    }

    public void wasResponded() throws NqException {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static NetbiosMessage lookupPending(int tranId) {
        NetbiosMessage theResult;
        Integer tranIdObj = tranId;
        Map map = pendingOperations;
        synchronized (map) {
            theResult = (NetbiosMessage)pendingOperations.get(tranIdObj);
        }
        return theResult;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void consumeExpiredMessages(NetbiosService service, Class expectedClass) throws NetbiosException {
        long currentTime = System.currentTimeMillis();
        Map map = pendingOperations;
        synchronized (map) {
            for (NetbiosMessage nextMessage : pendingOperations.values()) {
                if (!expectedClass.isInstance(nextMessage) || nextMessage.eol == 0L || nextMessage.eol >= currentTime) continue;
                new Thread(new ConsumeRunner(service, nextMessage)).start();
            }
        }
    }

    protected void capturePacket(boolean isInboundPacket, DatagramSocket socket, DatagramPacket packet) {
        this.capture.capturePacketWriteUdp(isInboundPacket, socket.getLocalAddress(), socket.getLocalPort(), packet.getAddress(), packet.getPort(), packet);
    }

    protected void capturePacket(boolean isInboundPacket, InetAddress inetAddress, int inetPort, DatagramSocket socket, DatagramPacket packet) {
        this.capture.capturePacketWriteUdp(isInboundPacket, socket.getLocalAddress(), socket.getLocalPort(), inetAddress, inetPort, packet);
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String toString() {
        return "NetbiosMessage [packet=" + this.packet + ", tranId=" + this.tranId + ", origin=" + this.origin + ", count=" + this.count + ", eol=" + this.eol + ", status=" + this.status + ", service=" + this.service + ", multicast=" + this.multicast + ", direction=" + this.direction + ", port=" + this.port + "]";
    }

    private static class ConsumeRunner
    implements Runnable {
        private NetbiosMessage msg;
        private NetbiosService service;

        protected ConsumeRunner(NetbiosService service, NetbiosMessage msg) {
            this.msg = msg;
            this.service = service;
        }

        public void run() {
            try {
                this.service.consumeExpiredMessage(this.msg);
            }
            catch (Exception ex) {
                TraceLog.get().error("Failed to consumeExpiredMessage: ", ex);
            }
        }
    }
}

