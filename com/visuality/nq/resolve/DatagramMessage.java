/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.IpAddressHelper;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Utility;
import com.visuality.nq.resolve.NetbiosDaemon;
import com.visuality.nq.resolve.NetbiosException;
import com.visuality.nq.resolve.NetbiosMessage;
import com.visuality.nq.resolve.NetbiosName;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedList;

public class DatagramMessage
extends NetbiosMessage {
    private static LinkedList pendingEntries = new LinkedList();
    private int datagramLengthOffset;
    private String srcName;
    private static int internalDatagramPort;

    public DatagramMessage(DatagramPacket packet) {
        super(false, packet);
    }

    public DatagramMessage() {
        super(true, new DatagramPacket(new byte[1460], 1460));
    }

    public BufferWriter createHeader(NetbiosName destName) throws NetbiosException {
        byte[] data = this.getPacket().getData();
        BufferWriter writer = new BufferWriter(data, 0, true);
        writer.writeByte((byte)17);
        writer.writeByte((byte)2);
        writer.writeInt2(this.tranId);
        InetAddress localhostIp = IpAddressHelper.getLocalHostIp();
        writer.writeBytes(localhostIp.getAddress(), 4);
        writer.writeInt2(138);
        this.datagramLengthOffset = writer.getOffset();
        writer.skip(2);
        writer.writeInt2(0);
        NetbiosName hostName = new NetbiosName(Utility.getNetbiosNameFromFQN(localhostIp.getHostName()), 0);
        int offset = writer.getOffset();
        offset = hostName.encodeName(data, offset);
        offset = destName.encodeName(data, offset);
        writer.setOffset(offset);
        return writer;
    }

    public BufferReader sendReceive(int length) throws NetbiosException {
        DatagramSocket sock = null;
        try {
            this.direction = 1;
            sock = new DatagramSocket();
            DatagramPacket packet = this.getPacket();
            BufferWriter writer = new BufferWriter(this.getPacket().getData(), this.datagramLengthOffset, true);
            writer.writeInt2(length - 14);
            packet.setLength(length);
            packet.setPort(internalDatagramPort);
            InetAddress destIp = IpAddressHelper.loopbackAddr;
            packet.setAddress(destIp);
            sock.setSoTimeout(NetbiosDaemon.getDatagramTimeout());
            TraceLog.get().message("Sending packet to ip = " + packet.getAddress() + ", port = " + packet.getPort(), 250);
            this.capturePacket(false, destIp, packet.getPort(), sock, packet);
            sock.send(packet);
            packet.setLength(1460);
            packet.setPort(sock.getLocalPort());
            sock.receive(packet);
            this.capturePacket(true, sock, packet);
            byte[] src = packet.getData();
            BufferReader reader = new BufferReader(src, 0, true);
            reader.skip(82);
            BufferReader bufferReader = reader;
            return bufferReader;
        }
        catch (SocketTimeoutException ex) {
            throw new NetbiosException("Operation timeout: " + ex.getMessage(), -505);
        }
        catch (Exception ex) {
            throw new NetbiosException("Unable to perform NetBIOS operation: " + ex.getMessage(), -506);
        }
        finally {
            if (null != sock) {
                sock.close();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void process() throws NqException {
        TraceLog.get().enter(250);
        this.port = this.getPacket().getPort();
        byte[] src = this.getPacket().getData();
        BufferReader reader = new BufferReader(src, 0, true);
        reader.skip(2);
        this.tranId = reader.readInt2();
        reader.skip(10);
        NetbiosName srcName = new NetbiosName();
        reader.setOffset(srcName.parse(reader.getSrc(), reader.getOffset()));
        NetbiosName dstName = new NetbiosName();
        reader.setOffset(dstName.parse(reader.getSrc(), reader.getOffset()));
        if (1 == this.direction) {
            LinkedList linkedList = pendingEntries;
            synchronized (linkedList) {
                pendingEntries.add(new PendingEntry(this.tranId, srcName));
            }
            this.srcName = srcName.getName();
            this.sendBcast();
        } else {
            LinkedList linkedList = pendingEntries;
            synchronized (linkedList) {
                reader.skip(88);
                int tokenId = Integer.reverseBytes(reader.readInt4());
                for (PendingEntry entry : pendingEntries) {
                    DatagramMessage msg = entry.getMessage();
                    if (!msg.srcName.equals(dstName.getName()) || tokenId != entry.tranId + 1) continue;
                    DatagramSocket sock = null;
                    try {
                        sock = new DatagramSocket();
                        this.packet.setAddress(IpAddressHelper.getLocalHostIp());
                        this.packet.setPort(msg.port);
                        this.capturePacket(false, sock, this.packet);
                        sock.send(this.packet);
                    }
                    catch (UnknownHostException e) {
                        TraceLog.get().exit("Internal error", 250);
                        throw new NetbiosException("Internal error", -503);
                    }
                    catch (SocketException e) {
                        TraceLog.get().exit("Socket invalid", 250);
                        throw new NetbiosException("Socket invalid", -504);
                    }
                    catch (IOException e) {
                        TraceLog.get().exit("Error reponding internally from NetBios", 250);
                        throw new NetbiosException("Error reponding internally from NetBios", -506);
                    }
                    finally {
                        if (null == sock) continue;
                        sock.close();
                    }
                }
            }
        }
        TraceLog.get().exit(250);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void sendBcast() throws NetbiosException {
        TraceLog.get().enter(250);
        try {
            this.direction = 3;
            DatagramPacket packet = this.getPacket();
            InetAddress[] ips = IpAddressHelper.getAllBroadcasts();
            for (int i = 0; i < ips.length; ++i) {
                DatagramSocket sock = null;
                try {
                    sock = new DatagramSocket();
                    packet.setAddress(ips[i]);
                    sock.setBroadcast(true);
                    packet.setPort(138);
                    if (null != NetbiosDaemon.getTheDaemon()) {
                        this.capturePacket(false, ips[i], packet.getPort(), sock, packet);
                        NetbiosDaemon.getTheDaemon().externalDatagrame.send(this.packet);
                        continue;
                    }
                    TraceLog.get().exit("Unable to send datagram outside. There is no Netbios daemon running.", 250);
                    throw new NetbiosException("Unable to send datagram outside. There is no Netbios daemon running.", -501);
                }
                finally {
                    if (null != sock) {
                        sock.close();
                    }
                }
            }
        }
        catch (Exception ex) {
            TraceLog.get().exit("Unable to send datagram outside: ", ex, 250);
            throw new NetbiosException("Unable to send datagram outside: " + ex.getMessage(), -501);
        }
        TraceLog.get().exit(250);
    }

    static {
        try {
            if (null != NetbiosDaemon.getTheDaemon()) {
                internalDatagramPort = NetbiosDaemon.getTheDaemon().internalDatagram.getLocalPort();
            }
        }
        catch (NetbiosException e) {
            e.printStackTrace();
        }
    }

    private class PendingEntry {
        private int tranId;
        private NetbiosName srcName;

        private PendingEntry(int tranId, NetbiosName srcName) {
            this.tranId = tranId;
            this.srcName = srcName;
        }

        private DatagramMessage getMessage() {
            return DatagramMessage.this;
        }
    }
}

