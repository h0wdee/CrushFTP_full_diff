/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.HexBuilder;
import com.visuality.nq.common.IpAddressHelper;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Utility;
import com.visuality.nq.config.Config;
import com.visuality.nq.resolve.NetbiosException;
import com.visuality.nq.resolve.NetbiosMessage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Vector;

public class DnsMessage
extends NetbiosMessage {
    private static final int RECORDTYPE_A = 1;
    private static final int RECORDTYPE_AAAA = 28;
    private static final int RECORDTYPE_SRV = 33;
    private static final int RECORDTYPE_PTR = 12;
    private static final int RECORDTYPE_CNAME = 5;
    private static final int RECORDTYPE_TKEY = 249;
    private static final int DNS_QUERY = 1;
    private static final int DNS_UPDATE = 40;
    private static final int DNS_QUERY_RESPONSE = 128;
    private static final int DNS_CNAME = 5;
    private static final int DNS_SOA = 6;
    private static final int DNS_REPLY_CODE = 15;
    private static final int DNS_REPLY_CODE_NO_SUCH_NAME = 3;
    private static final int DNS_REPLY_CODE_REFUSED = 5;
    private static final int DNS_UPDATE_RESPONSE = 168;
    private static final int PORT_DNS = 53;
    private static final int PORT_LLMNR = 5355;
    private static int DNS_TIMEOUT;
    private static int UNICAST_RETRY_COUNT;
    private Vector ips = new Vector();
    private String name;
    private InetAddress serverIp;
    private int port;
    private DatagramSocket socket = null;
    private boolean addDomain;
    static short tranId;

    public DnsMessage(InetAddress server) throws SocketException {
        super(false, new DatagramPacket(new byte[1460], 1460));
        TraceLog.get().enter("server = ", server, 700);
        DNS_TIMEOUT = (Integer)Config.jnq.getNE("DNS_TIMEOUT");
        UNICAST_RETRY_COUNT = (Integer)Config.jnq.getNE("UNICAST_RETRY_COUNT");
        this.serverIp = server;
        this.port = server.isMulticastAddress() ? 5355 : 53;
        this.packet.setAddress(server);
        this.packet.setPort(this.port);
        this.socket = new DatagramSocket();
        TraceLog.get().exit(1000);
    }

    public void process() throws NetbiosException {
    }

    public void queryByName(String name) throws NqException {
        boolean res;
        block3: {
            TraceLog.get().enter("name=" + name, 700);
            this.name = name;
            this.addDomain = true;
            this.ips.clear();
            res = false;
            try {
                res = this.queryByType(1);
                res |= this.queryByType(28);
            }
            catch (NqException e) {
                TraceLog.get().caught(e, 700);
                if (this.ips.size() != 0) break block3;
                throw new NetbiosException("Failed to resolve '" + name + "'", -503);
            }
        }
        if (!res) {
            TraceLog.get().exit("Failed to resolve '" + name + "'", 700);
            throw new NetbiosException("Failed to resolve '" + name + "'", -503);
        }
        TraceLog.get().exit(700);
    }

    public String queryByIp(InetAddress ip) throws NqException {
        TraceLog.get().enter("ip = ", ip, 700);
        this.ips.clear();
        this.ips.add(ip);
        this.name = this.composeReversedIp(ip);
        if (!this.queryByType(12)) {
            TraceLog.get().exit("Failed to resolve '" + this.name + "'", 700);
            throw new NetbiosException("Failed to resolve '" + this.name + "'", -503);
        }
        TraceLog.get().exit("name = " + this.name, 700);
        return this.name;
    }

    public void queryDCByDomain(String domain) throws NqException {
        TraceLog.get().enter("domain = " + domain, 700);
        this.name = "_ldap._tcp.dc._msdcs." + domain;
        this.addDomain = false;
        if (!this.queryByType(33)) {
            TraceLog.get().exit("Failed to resolve '" + this.name + "'", 700);
            throw new NetbiosException("Failed to resolve '" + this.name + "'", -503);
        }
        TraceLog.get().exit(700);
    }

    private boolean queryByType(int recordType) throws NqException {
        TraceLog.get().enter("recordType = " + recordType, 700);
        for (int i = 0; i < UNICAST_RETRY_COUNT; ++i) {
            try {
                this.packet.setLength(this.createQueryRequest(this.packet.getData(), recordType, this.name));
                this.capturePacket(false, this.socket, this.packet);
                this.socket.send(this.packet);
                this.socket.setSoTimeout(DNS_TIMEOUT);
                this.packet.setLength(1460);
                this.socket.receive(this.packet);
                this.capturePacket(true, this.socket, this.packet);
                this.parseQueryResponse(this.packet.getData(), this.packet.getLength(), recordType);
            }
            catch (SocketTimeoutException ex) {
                continue;
            }
            catch (IOException e) {
                TraceLog.get().exit(700);
                return false;
            }
            TraceLog.get().exit(700);
            return true;
        }
        TraceLog.get().exit(700);
        return false;
    }

    private String composeReversedIp(InetAddress ip) throws NetbiosException {
        TraceLog.get().enter(1000);
        byte[] ipBytes = ip.getAddress();
        String ipStr = "";
        if (ip instanceof Inet4Address) {
            for (int i = ipBytes.length - 1; i >= 0; --i) {
                ipStr = ipStr + (ipBytes[i] & 0xFF) + ".";
            }
            TraceLog.get().exit(1000);
            return ipStr + "in-addr.arpa";
        }
        if (ip instanceof Inet6Address) {
            for (int i = ipBytes.length - 1; i >= 0; --i) {
                ipStr = ipStr + Integer.toHexString(ipBytes[i] & 0xF) + "." + Integer.toHexString((ipBytes[i] & 0xFF) / 16 & 0xF) + ".";
            }
            TraceLog.get().exit(1000);
            return ipStr + "ip6-arpa";
        }
        TraceLog.get().exit(1000);
        throw new NetbiosException("unsupported address type: " + ip.getClass(), -503);
    }

    public void registerHost() throws NqException {
        this.addDomain = true;
        this.doUpdate(true);
    }

    public void unregisterHost() throws NqException {
        this.addDomain = true;
        this.doUpdate(false);
    }

    private void doUpdate(boolean register) throws NqException {
        TraceLog.get().enter(1000);
        InetAddress[] hostIps = IpAddressHelper.getAllInet4Ips();
        this.doUpdateByIps(register, hostIps);
        hostIps = IpAddressHelper.getAllInet6Ips();
        this.doUpdateByIps(register, hostIps);
        TraceLog.get().exit(1000);
    }

    private void doUpdateByIps(boolean register, InetAddress[] ipsToRegister) throws NqException {
        int i;
        TraceLog.get().enter(1000);
        if (ipsToRegister.length == 0) {
            TraceLog.get().exit(1000);
            return;
        }
        BufferWriter writer = new BufferWriter(this.packet.getData(), 0, true);
        DnsHeader header = new DnsHeader();
        header.id = tranId = (short)(tranId + 1);
        header.flags1 = (byte)40;
        header.flags2 = 0;
        header.questions = 1;
        header.answers = 1;
        header.authority = 1;
        if (register) {
            header.authority = (short)(header.authority + (short)ipsToRegister.length);
        }
        header.additional = 0;
        this.writeDnsHeader(writer, header);
        byte[] zone = new byte[]{0, 6, 0, 1};
        byte[] prereq = new byte[]{0, 5, 0, -2, 0, 0, 0, 0, 0, 0};
        byte[] update1 = new byte[]{0, 0, 0, -1, 0, 0, 0, 0, 0, 0};
        byte[] update2 = new byte[]{0, 0, 0, 1, 0, 0, 3, -124};
        try {
            String domain = Config.jnq.getString("DNSDOMAIN");
            if (!Utility.isValidDnsDomainName(domain)) {
                domain = null;
            }
            if (domain == null || domain.length() == 0) {
                domain = Config.getDomainName();
            }
            this.writeBlock(writer, domain, zone);
        }
        catch (NqException e1) {
            TraceLog.get().error("No DNSDOMAIN defined or it is invalid: ", e1);
        }
        String hostName = Utility.getHostName();
        this.writeBlock(writer, hostName, prereq);
        int type = ipsToRegister[0] instanceof Inet4Address ? 1 : 28;
        update1[1] = (byte)type;
        this.writeBlock(writer, hostName, update1);
        if (register) {
            for (i = 0; i < ipsToRegister.length; ++i) {
                update2[1] = (byte)type;
                this.writeBlock(writer, hostName, update2);
                writer.writeByte((byte)0);
                byte[] ipBytes = ipsToRegister[i].getAddress();
                writer.writeByte((byte)ipBytes.length);
                writer.writeBytes(ipBytes);
            }
        }
        for (i = 0; i < UNICAST_RETRY_COUNT; ++i) {
            this.packet.setLength(writer.getOffset());
            try {
                this.capturePacket(false, this.socket, this.packet);
                this.socket.send(this.packet);
                this.socket.setSoTimeout(DNS_TIMEOUT);
                try {
                    this.packet.setLength(1460);
                    this.socket.receive(this.packet);
                    this.capturePacket(true, this.socket, this.packet);
                    BufferReader reader = new BufferReader(this.packet.getData(), 0, true);
                    this.readDnsHeader(reader, header);
                    if (5 != (header.flags2 & 0xF) && 0 == (header.flags2 & 0xF)) continue;
                    TraceLog.get().exit(1000);
                    return;
                }
                catch (SocketTimeoutException ex) {}
                continue;
            }
            catch (IOException e) {
                // empty catch block
            }
        }
        TraceLog.get().exit(1000);
    }

    private void writeBlock(BufferWriter writer, String name, byte[] block) {
        this.encodeName(writer, name);
        writer.writeBytes(block, block.length);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int createQueryRequest(byte[] data, int type, String lookupName) {
        BufferWriter writer = new BufferWriter(data, 0, true);
        DnsHeader header = new DnsHeader();
        Object object = tranIdGuard;
        synchronized (object) {
            header.id = tranId = (short)(tranId + 1);
        }
        header.flags1 = 1;
        header.flags2 = 0;
        header.questions = 1;
        header.answers = 0;
        header.authority = 0;
        header.additional = 0;
        byte[] query = new byte[]{0, 0, 0, 1};
        query[1] = (byte)type;
        this.writeDnsHeader(writer, header);
        if (this.addDomain && -1 == lookupName.indexOf(46)) {
            String domain = "";
            try {
                domain = Config.jnq.getString("DNSDOMAIN");
                if (!Utility.isValidDnsDomainName(domain)) {
                    domain = "";
                }
            }
            catch (NqException e) {
                TraceLog.get().error("No DNSDOMAIN defined or it is invalid: ", e);
            }
            if (null != domain && domain.length() > 0) {
                lookupName = lookupName + "." + domain;
            }
        }
        this.encodeName(writer, lookupName);
        writer.writeBytes(query, query.length);
        return writer.getOffset();
    }

    private void writeDnsHeader(BufferWriter writer, DnsHeader header) {
        writer.writeInt2(header.id);
        writer.writeByte(header.flags1);
        writer.writeByte(header.flags2);
        writer.writeInt2(header.questions);
        writer.writeInt2(header.answers);
        writer.writeInt2(header.authority);
        writer.writeInt2(header.additional);
    }

    private void readDnsHeader(BufferReader reader, DnsHeader header) throws NqException {
        header.id = reader.readInt2();
        header.flags1 = reader.readByte();
        header.flags2 = reader.readByte();
        header.questions = reader.readInt2();
        header.answers = reader.readInt2();
        header.authority = reader.readInt2();
        header.additional = reader.readInt2();
    }

    private void encodeName(BufferWriter writer, String name) {
        int labelIdx = 0;
        while (true) {
            int dotIdx;
            String nextLabel = (dotIdx = name.indexOf(46, labelIdx)) == -1 ? name.substring(labelIdx) : name.substring(labelIdx, dotIdx);
            writer.writeByte((byte)nextLabel.length());
            writer.writeBytes(nextLabel.getBytes(), nextLabel.length());
            if (dotIdx == -1) break;
            labelIdx = dotIdx + 1;
        }
        writer.writeByte((byte)0);
    }

    private void parseQueryResponse(byte[] data, int length, int type) throws NqException {
        String nextName;
        TraceLog.get().enter(1000);
        if (TraceLog.get().canLog(2000)) {
            TraceLog.get().message("data=" + HexBuilder.toHex(data, length) + ", length=" + length + ", type=" + type, 2000);
        }
        DnsHeader header = new DnsHeader();
        BufferReader reader = new BufferReader(data, 0, true);
        this.readDnsHeader(reader, header);
        if (5 == (header.flags2 & 0xF)) {
            TraceLog.get().exit(1000);
            throw new NetbiosException("DNS request was refused by " + this.serverIp, -503);
        }
        if (header.answers < 1 || 3 == (header.flags2 & 0xF)) {
            TraceLog.get().exit(1000);
            throw new NetbiosException("Unknown DNS name reported by " + this.serverIp, -503);
        }
        int i = header.questions;
        while (--i >= 0) {
            nextName = this.decodeName(reader);
            reader.skip(4);
        }
        block12: for (int answer = 0; answer < header.answers; ++answer) {
            int dataLen;
            nextName = this.decodeName(reader);
            short nameType = reader.readInt2();
            reader.skip(6);
            if (nameType == type) {
                switch (nameType) {
                    case 1: {
                        byte[] rawIp4 = new byte[4];
                        for (dataLen = reader.readInt2(); dataLen > 0; dataLen -= 4) {
                            reader.readBytes(rawIp4, 4);
                            try {
                                InetAddress nextIp = InetAddress.getByAddress(rawIp4);
                                this.ips.add(nextIp);
                                continue;
                            }
                            catch (UnknownHostException e) {
                                TraceLog.get().exit(1000);
                                throw new NetbiosException(e.getMessage(), -503);
                            }
                        }
                        continue block12;
                    }
                    case 28: {
                        byte[] rawIp6 = new byte[16];
                        while (dataLen > 0) {
                            reader.readBytes(rawIp6, 16);
                            try {
                                InetAddress nextIp = InetAddress.getByAddress(rawIp6);
                                this.ips.add(nextIp);
                            }
                            catch (UnknownHostException e) {
                                TraceLog.get().exit(1000);
                                throw new NetbiosException(e.getMessage(), -503);
                            }
                            dataLen -= 16;
                        }
                        continue block12;
                    }
                    case 5: {
                        String temp = this.name;
                        this.name = this.decodeName(reader);
                        this.queryByType(type);
                        this.name = temp;
                        break;
                    }
                    case 33: {
                        reader.skip(6);
                        this.name = this.decodeName(reader);
                        break;
                    }
                    case 12: {
                        this.name = this.decodeName(reader);
                        break;
                    }
                    default: {
                        TraceLog.get().exit(1000);
                        throw new NetbiosException("Unknown DNS record type: " + nameType, -503);
                    }
                }
                continue;
            }
            reader.skip(dataLen);
        }
        TraceLog.get().exit(1000);
    }

    private String decodeName(BufferReader reader) throws NqException {
        TraceLog.get().enter(1000);
        boolean backRef = false;
        byte[] data = reader.getSrc();
        int pos = reader.getOffset();
        String res = "";
        boolean addDot = false;
        while (data[pos] != 0) {
            byte length;
            if (data[pos] == -64 && !backRef) {
                pos = data[pos + 1];
                backRef = true;
                reader.skip(2);
                continue;
            }
            if (0 >= (length = data[pos++])) {
                return "";
            }
            if (addDot) {
                res = res + ".";
            }
            addDot = true;
            res = res + new String(data, pos, (int)length);
            pos += length;
        }
        if (!backRef) {
            reader.setOffset(pos + 1);
        }
        TraceLog.get().exit(1000);
        return res;
    }

    public Vector getIps() {
        return this.ips;
    }

    public String getName() {
        return this.name;
    }

    public void terminate() {
        if (this.socket != null) {
            this.socket.close();
            this.socket = null;
        }
    }

    static {
        tranId = 0;
    }

    private static class DnsHeader {
        short id;
        byte flags1;
        byte flags2;
        short questions;
        short answers;
        short authority;
        short additional;

        private DnsHeader() {
        }
    }
}

