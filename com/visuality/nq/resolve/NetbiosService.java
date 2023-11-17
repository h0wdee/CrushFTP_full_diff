/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import com.visuality.nq.common.CaptureInternal;
import com.visuality.nq.common.IpAddressHelper;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Utility;
import com.visuality.nq.resolve.NetbiosMessage;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public abstract class NetbiosService
extends DatagramSocket {
    CaptureInternal capture = null;
    boolean continueRunning = true;

    public NetbiosService() throws SocketException {
        try {
            this.startService();
        }
        catch (SocketException ex) {
            TraceLog.get().error("Failed to start NetBios Service: ", ex);
        }
    }

    public NetbiosService(int port, InetAddress ip) throws SocketException {
        super(port, ip);
        try {
            this.startService();
        }
        catch (SocketException ex) {
            TraceLog.get().error("Failed to start NetBios Service: ", ex);
        }
    }

    public NetbiosService(int port) throws SocketException {
        super(port);
        this.startService();
    }

    public abstract void doTimeout() throws NqException;

    public abstract int getTimeout();

    private void startService() throws SocketException {
        this.capture = new CaptureInternal();
        if (Utility.isAndroid()) {
            InetSocketAddress localSocketAddress = new InetSocketAddress(IpAddressHelper.getLocalHostIp(), 0);
            this.connect(localSocketAddress);
        }
        this.setReuseAddress(true);
        this.setBroadcast(true);
        String serverName = this.getClass().getSimpleName();
        new Body(serverName).start();
    }

    public abstract NetbiosMessage createMessage(DatagramPacket var1);

    public void consumeExpiredMessage(NetbiosMessage msg) throws NqException {
    }

    public void stop() {
        this.continueRunning = false;
        this.close();
    }

    public class Body
    extends Thread {
        public Body(String name) {
            super(name);
        }

        public void run() {
            TraceLog.get().enter(250);
            try {
                byte[] msgData = new byte[1460];
                DatagramPacket packet = new DatagramPacket(msgData, msgData.length);
                while (NetbiosService.this.continueRunning) {
                    NetbiosService.this.setSoTimeout(NetbiosService.this.getTimeout());
                    try {
                        packet.setLength(1460);
                        NetbiosService.this.receive(packet);
                        InetAddress localAddress = IpAddressHelper.getLocalHostIp();
                        int localPort = NetbiosService.this.getLocalPort();
                        InetAddress inetAddress = packet.getAddress();
                        int inetPort = packet.getPort();
                        NetbiosMessage netbiosMessage = NetbiosService.this.createMessage(packet);
                        if (null == netbiosMessage) continue;
                        NetbiosService.this.capture.capturePacketWriteUdp(true, localAddress, localPort, inetAddress, inetPort, packet);
                        netbiosMessage.process();
                    }
                    catch (SocketTimeoutException ex) {
                        NetbiosService.this.doTimeout();
                    }
                }
            }
            catch (SocketException ex) {
                TraceLog.get().message(NetbiosService.this.getClass() + " stopped.", 250);
            }
            catch (Exception ex) {
                TraceLog.get().error("General error : ", ex);
                TraceLog.get().caught(ex, 2000);
            }
            TraceLog.get().exit(250);
        }
    }
}

