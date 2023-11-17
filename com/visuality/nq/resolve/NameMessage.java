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
import com.visuality.nq.config.Config;
import com.visuality.nq.resolve.NetbiosDaemon;
import com.visuality.nq.resolve.NetbiosException;
import com.visuality.nq.resolve.NetbiosMessage;
import com.visuality.nq.resolve.NetbiosName;
import com.visuality.nq.resolve.NetbiosService;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Vector;

public class NameMessage
extends NetbiosMessage {
    static Inet4Address[] localIps;
    private int opcode;
    private int type;
    private int codes;
    private int reply;
    private int state;
    private NetbiosName name;
    private Vector ips;
    private boolean waitForResponse;
    static final int RCODE = 15;
    static final int ANY_ERR = 1;
    static final int RESPONSE = 32768;
    static final int AUTHORIZED = 1024;
    static final int OPCODE_SHIFT = 11;
    static final int OPCODE_MASK = 30720;
    static final int OPCODE_QUERY = 0;
    static final int OPCODE_REGISTRATION = 10240;
    static final int OPCODE_RELEASE = 12288;
    static final int OPCODE_WACK = 14336;
    static final int OPCODE_REFRESH = 16384;
    static final int OPCODE_REFRESHALT = 18432;
    static final int INTERNALREFRESHLIST = 24576;
    static final int OPCODE_MHREGISTRATION = 30720;
    static final int QUERY_REQUEST = 0;
    static final int POSITIVE_QUERY = 32768;
    static final int NEGATIVE_QUERY = 32769;
    static final int REGISTRATION_REQUEST = 10240;
    static final int POSITIVE_REGISTRATION = 43008;
    static final int NEGATIVE_REGISTRATION = 43009;
    static final int RELEASE_REQUEST = 12288;
    static final int POSITIVE_RELEASE = 45056;
    static final int NEGATIVE_RELEASE = 45057;
    static final int REFRESH_REQUEST = 16384;
    static final int REFRESHHALT_REQUEST = 18432;
    static final int MULTIHOME_REQUEST = 30720;
    static final int WACK = 47104;
    static final int REPLY_MASK = 15;
    static final int RCODE_NOERR = 0;
    static final int RCODE_FMTERR = 1;
    static final int RCODE_SRVFAIL = 2;
    static final int RCODE_NAMERR = 3;
    static final int RCODE_NOTIMPL = 4;
    static final int RCODE_REFUSED = 5;
    static final int RCODE_ACTIVE = 6;
    static final int RCODE_CONFLICT = 7;
    static final int NAMEFLAGS_G = 32768;
    static final int NAMEFLAGS_ONT = 24576;
    static final int NAMEFLAGS_ONT_B = 0;
    static final int NAMEFLAGS_ONT_P = 8192;
    static final int NAMEFLAGS_ONT_M = 16384;
    static final int NAMEFLAGS_ONT_H = 16384;
    static final int NAMEFLAGS_DRG = 4096;
    static final int NAMEFLAGS_CNF = 2048;
    static final int NAMEFLAGS_ACT = 1024;
    static final int NAMEFLAGS_PRM = 512;
    static final int NAMEFLAGS_AA = 1024;
    static final int NAMEFLAGS_TC = 512;
    static final int NAMEFLAGS_RD = 256;
    static final int NAMEFLAGS_RA = 128;
    static final int NAMEFLAGS_B = 16;
    static final int STATE_NEW = 0;
    static final int STATE_PENDING = 1;
    static final int STATE_DONE = 3;
    static final int STATE_FAILED = 4;
    static final int INTERNALCOMMUNICATION_TIMEOUT = 10000;
    static final int BROADCAST_TIMEOUT = 1000;
    static final int UNICAST_TIMEOUT = 1000;
    static final int BROADCAST_REPEATCOUNT = 1;
    static final int TYPE_NB = 32;
    static final int TYPE_NBSTAT = 33;

    public NameMessage() {
        super(false);
        try {
            localIps = IpAddressHelper.getAllInet4Ips();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.name = new NetbiosName();
        this.ips = new Vector();
        this.setService();
    }

    public NameMessage(DatagramPacket packet) {
        super(false, packet);
        try {
            localIps = IpAddressHelper.getAllInet4Ips();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.name = new NetbiosName();
        this.ips = new Vector();
        this.setService();
    }

    public NameMessage(boolean isMulticast) {
        super(isMulticast);
        try {
            localIps = IpAddressHelper.getAllInet4Ips();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.name = new NetbiosName();
        this.ips = new Vector();
        this.name = null;
        this.state = 0;
        this.setService();
    }

    public NameMessage(NetbiosName name) {
        super(false);
        try {
            localIps = IpAddressHelper.getAllInet4Ips();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.name = new NetbiosName();
        this.ips = new Vector();
        this.name = name;
        this.state = 0;
        this.setService();
    }

    public NameMessage(boolean isMulticast, NetbiosName name) {
        super(isMulticast);
        try {
            localIps = IpAddressHelper.getAllInet4Ips();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.name = new NetbiosName();
        this.ips = new Vector();
        this.name = name;
        this.state = 0;
        this.setService();
    }

    public NameMessage(boolean isMulticast, NameMessage origin) {
        super(isMulticast, origin);
        try {
            localIps = IpAddressHelper.getAllInet4Ips();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.name = new NetbiosName();
        this.ips = new Vector();
        this.opcode = origin.opcode;
        this.name = origin.name;
        this.setService();
        this.state = 0;
    }

    protected void setService() {
        try {
            this.service = null != NetbiosDaemon.getTheDaemon() ? NetbiosDaemon.getTheDaemon().externalName : null;
        }
        catch (NetbiosException e) {
            this.service = null;
        }
    }

    public int getOpcode() {
        return this.opcode;
    }

    public void setOpcode(int opcode) {
        this.opcode = opcode;
    }

    public int getReply() {
        return this.reply;
    }

    public void setReply(int reply) {
        this.reply = reply;
    }

    public int getCodes() {
        return this.codes;
    }

    public void process() throws NqException {
        block30: {
            TraceLog.get().enter(250);
            this.port = this.getPacket().getPort();
            byte[] src = this.getPacket().getData();
            BufferReader reader = new BufferReader(src, 0, true);
            this.tranId = reader.readInt2();
            this.codes = reader.readInt2();
            int opcode = this.codes & 0xF800 | ((this.codes & 0xF) != 0 ? 1 : 0) | ((this.codes & 0xF) != 0 ? 1 : 0);
            this.reply = this.codes & 0xF;
            if (opcode == 24576) {
                NetbiosDaemon.internalNames.releaseAllNames();
                TraceLog.get().exit(250);
                return;
            }
            this.opcode = opcode;
            this.multicast = (this.codes & 0x10) > 0;
            reader.skip(8);
            reader.setOffset(this.name.parse(src, reader.getOffset()));
            if (this.multicast && this.name.getName().equals("*")) {
                TraceLog.get().exit(250);
                return;
            }
            block0 : switch (this.direction) {
                case 1: {
                    this.addPending();
                    switch (opcode) {
                        case 0: {
                            reader.skip(2);
                            this.ips.clear();
                            this.readIps(reader, this);
                            this.type = this.ips.size() > 0 ? 33 : 32;
                            this.doQuery(this.name);
                            break block0;
                        }
                        case 10240: 
                        case 12288: {
                            reader.skip(4);
                            reader.setOffset(NetbiosName.skipName(src, reader.getOffset()));
                            short flags = reader.readInt2();
                            this.name.setGroup(0 != (flags & 0x8000));
                            this.doRegisterRelease(this.name);
                            break block0;
                        }
                    }
                    throw new NetbiosException("Illegal opcode in internal request: " + Integer.toHexString(opcode), -506);
                }
                case 2: {
                    boolean isResponse;
                    NameMessage pendingMsg = null;
                    boolean bl = isResponse = (opcode & 0x8000) != 0;
                    if (isResponse) {
                        pendingMsg = (NameMessage)NameMessage.lookupPending(this.tranId);
                        if (null == pendingMsg) {
                            return;
                        }
                        pendingMsg.setPacket(this.getPacket());
                    }
                    switch (opcode) {
                        case 43008: {
                            if (isResponse) {
                                pendingMsg.processPositiveRegistrationResponse(this.name);
                                break block0;
                            }
                            break block30;
                        }
                        case 43009: {
                            if (isResponse) {
                                pendingMsg.processNegativeRegistrationResponse(this.name);
                                break block0;
                            }
                            break block30;
                        }
                        case 45056: {
                            if (isResponse) {
                                pendingMsg.processPositiveReleaseResponse(this.name);
                                break block0;
                            }
                            break block30;
                        }
                        case 45057: {
                            if (isResponse) {
                                pendingMsg.processNegativeReleaseResponse(this.name);
                                break block0;
                            }
                            break block30;
                        }
                        case 32768: {
                            if (isResponse) {
                                pendingMsg.processPositiveQueryResponse(this.name);
                                break block0;
                            }
                            break block30;
                        }
                        case 32769: {
                            if (isResponse) {
                                pendingMsg.processNegativeQueryResponse(this.name);
                                break block0;
                            }
                            break block30;
                        }
                        case 47104: {
                            if (isResponse) {
                                pendingMsg.processWaitForAck(this.name);
                                break block0;
                            }
                            break block30;
                        }
                        case 0: {
                            this.processQueryRequest(this.name);
                            break block0;
                        }
                        case 10240: {
                            this.processCheckNameConflict(this.name);
                            break block0;
                        }
                    }
                }
            }
        }
        TraceLog.get().exit(250);
    }

    private synchronized void doQuery(NetbiosName name) throws NqException {
        TraceLog.get().enter("name = " + name, 250);
        Vector cachedIps = null;
        if (this.type == 32) {
            TraceLog.get().message("Calling NetbiosDaemon.externalNames.lookup for name = " + name, 250);
            cachedIps = NetbiosDaemon.externalNames.lookup(name);
        }
        if (null == cachedIps) {
            NameMessage[] msgsToSend = this.createMessagesToSendOutside(this);
            if (null == msgsToSend) {
                this.state = 3;
                this.doRespond();
                TraceLog.get().exit(250);
                return;
            }
            for (int i = 0; i < msgsToSend.length; ++i) {
                msgsToSend[i].status = 5;
                msgsToSend[i].encodeRequest(null);
                if (msgsToSend[i].multicast && msgsToSend[i].name.getName().equals("*")) continue;
                msgsToSend[i].waitForResponse = true;
                msgsToSend[i].send();
            }
        } else {
            this.ips = cachedIps;
            this.doRespond();
        }
        TraceLog.get().exit(250);
    }

    private void doRegisterRelease(NetbiosName name) throws NqException {
        TraceLog.get().enter("name = " + name, 250);
        if (null == localIps || 0 == localIps.length) {
            TraceLog.get().exit("No IPs to register", 250);
            throw new NetbiosException("No IPs to register", -503);
        }
        for (int i = 0; i < localIps.length; ++i) {
            NameMessage[] msgsToSend = this.createMessagesToSendOutside(this);
            if (null == msgsToSend) {
                this.doRespond();
                if (this.opcode == 10240) {
                    try {
                        this.ips.clear();
                        Inet4Address[] ipAddrs = IpAddressHelper.getAllInet4Ips();
                        for (int ip = 0; ip < ipAddrs.length; ++ip) {
                            this.ips.add(ipAddrs[ip]);
                        }
                        NetbiosDaemon.internalNames.add(this.name, -1L, this.ips);
                    }
                    catch (Exception ex) {
                        TraceLog.get().exit("Configuration: ", ex, 250);
                        throw new NetbiosException("Configuration: " + ex.getMessage(), -503);
                    }
                } else {
                    NetbiosDaemon.internalNames.remove(this.name);
                }
                TraceLog.get().exit(250);
                return;
            }
            for (int n = 0; n < msgsToSend.length; ++n) {
                msgsToSend[n].encodeRequest(localIps[i]);
                if (msgsToSend[n].multicast && msgsToSend[n].name.getName().equals("*")) continue;
                msgsToSend[n].send();
            }
        }
        TraceLog.get().exit(250);
    }

    private void processQueryRequest(NetbiosName name) throws NqException {
        TraceLog.get().enter("name = " + name, 250);
        byte[] src = this.getPacket().getData();
        BufferReader reader = new BufferReader(src, 0, true);
        reader.skip(12);
        reader.setOffset(this.name.parse(src, reader.getOffset()));
        this.type = reader.readInt2();
        if (this.type == 33) {
            this.returnPositiveQuery(33);
        } else if (this.type == 32) {
            this.ips = NetbiosDaemon.internalNames.lookup(this.name);
            if (null != this.ips) {
                this.returnPositiveQuery(32);
            }
        } else {
            this.returnNegativeQuery(1);
        }
        TraceLog.get().exit(250);
    }

    protected void processCheckNameConflict(NetbiosName name) {
    }

    private void processPositiveRegistrationResponse(NetbiosName name) throws NqException {
        this.state = 3;
        this.wasResponded();
    }

    private void processNegativeRegistrationResponse(NetbiosName name) throws NqException {
        this.wasResponded();
    }

    private void processPositiveReleaseResponse(NetbiosName name) throws NqException {
    }

    private void processNegativeReleaseResponse(NetbiosName name) throws NqException {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void processPositiveQueryResponse(NetbiosName name) throws NqException {
        TraceLog.get().enter("name = " + name, 250);
        this.state = 3;
        this.status = 0;
        byte[] src = this.getPacket().getData();
        BufferReader reader = new BufferReader(src, 0, true);
        reader.skip(12);
        reader.setOffset(NetbiosName.skipName(src, reader.getOffset()));
        short type = reader.readInt2();
        switch (type) {
            case 32: {
                reader.skip(2);
                long ttl = reader.readInt4();
                NetbiosMessage netbiosMessage = this.origin;
                synchronized (netbiosMessage) {
                    this.origin.setEol(ttl + System.currentTimeMillis());
                    this.readIps(reader, (NameMessage)this.origin);
                    this.origin.status = 0;
                    this.wasResponded();
                    this.removePending(3);
                    TraceLog.get().exit(250);
                    return;
                }
            }
            case 33: {
                reader.skip(8);
                int numNames = reader.readByte();
                NetbiosMessage netbiosMessage = this.origin;
                synchronized (netbiosMessage) {
                    while (numNames-- > 0) {
                        int offset = reader.getOffset();
                        String nextName = new String(src, offset, 15);
                        reader.setOffset(offset + 15);
                        byte role = reader.readByte();
                        short flags = reader.readInt2();
                        if ((flags & 0x400) != 1024 || (flags & 0x8000) != 0) continue;
                        ((NameMessage)this.origin).name = new NetbiosName(nextName, role);
                        this.origin.status = 0;
                        ((NameMessage)this.origin).doRespond();
                        this.removePending(3);
                        TraceLog.get().exit(250);
                        return;
                    }
                    this.origin.status = 3;
                    break;
                }
            }
            default: {
                TraceLog.get().exit("Illegal type in response: " + type, 250);
                throw new NetbiosException("Illegal type in response: " + type, -503);
            }
        }
        TraceLog.get().exit(250);
    }

    private void processNegativeQueryResponse(NetbiosName name) throws NetbiosException {
    }

    private void processWaitForAck(NetbiosName name) {
    }

    public Vector getIps() {
        return this.ips;
    }

    private void returnPositiveQuery(int type) throws NetbiosException {
        TraceLog.get().enter("type = " + type, 250);
        try {
            this.status = 0;
            this.getPacket().setLength(this.encodeResponse());
            this.waitForResponse = false;
            this.send();
            this.removePending(0);
        }
        catch (Exception ex) {
            TraceLog.get().exit("Unable to send response to host: ", ex, 250);
            throw new NetbiosException("Unable to send response to host: " + ex.getMessage(), -501);
        }
        TraceLog.get().exit();
    }

    private void returnNegativeQuery(int status) throws NetbiosException {
        TraceLog.get().enter("status = " + status, 250);
        try {
            this.status = 3;
            this.getPacket().setLength(this.encodeResponse());
            this.waitForResponse = false;
            this.send();
            this.removePending(3);
        }
        catch (Exception ex) {
            TraceLog.get().exit("Unable to send response to host: ", ex, 250);
            throw new NetbiosException("Unable to send response to host: " + ex.getMessage(), -501);
        }
        TraceLog.get().exit();
    }

    private void encodeRequest(Inet4Address ip) throws NqException {
        BufferWriter writer;
        TraceLog.get().enter("ip = ", ip, 250);
        int codes = this.opcode | 0x100;
        if (this.multicast) {
            codes |= 0x10;
        }
        DatagramPacket packet = this.getPacket();
        byte[] data = packet.getData();
        switch (this.opcode) {
            case 10240: 
            case 12288: {
                writer = this.writeHeader(codes, 1, 0, 0, 1, 0);
                int questionOffset = writer.getOffset();
                int offset = this.name.encodeName(data, questionOffset);
                writer.setOffset(offset);
                writer.writeInt2(32);
                writer.writeInt2(1);
                offset = this.name.encodeNamePointer(data, writer.getOffset(), questionOffset);
                writer.setOffset(offset);
                writer.writeInt2(32);
                writer.writeInt2(1);
                writer.writeInt4(-1);
                writer.writeInt2(6);
                int flags = 0;
                if (this.name.isGroup()) {
                    flags |= 0x8000;
                }
                flags = Config.getWins() != null ? (flags |= 0x4000) : (flags |= 0);
                writer.writeInt2(flags);
                if (null == ip) break;
                writer.writeBytes(ip.getAddress(), 4);
                break;
            }
            case 0: {
                if (this.origin != null && ((NameMessage)this.origin).type == 33) {
                    codes = 0;
                }
                writer = this.writeHeader(codes, 1, 0, 0, 0, 0);
                int questionOffset = writer.getOffset();
                if (this.type == 33) {
                    if (null != this.name) {
                        throw new NetbiosException("Illegal datagram destination: both name and IPs are not null", -509);
                    }
                    this.name = new NetbiosName("*", 0);
                }
                int offset = this.name.encodeName(data, questionOffset);
                writer.setOffset(offset);
                if (this.origin == null && this.type == 32 || this.origin != null && ((NameMessage)this.origin).type == 32) {
                    writer.writeInt2(32);
                } else {
                    writer.writeInt2(33);
                    if (this.direction == 1) {
                        writer.writeInt2(6);
                        writer.writeInt2(0);
                        writer.writeBytes(((InetAddress)this.ips.get(0)).getAddress(), 4);
                    }
                }
                writer.writeInt2(1);
                break;
            }
            default: {
                TraceLog.get().exit("Illegal opcode: " + this.opcode, 250);
                throw new NetbiosException("Illegal opcode: " + this.opcode, -506);
            }
        }
        packet.setLength(writer.getOffset());
        TraceLog.get().exit(250);
    }

    private int encodeResponse() throws NetbiosException {
        TraceLog.get().enter(250);
        int codes = 0x8400 | this.opcode | this.status;
        if (this.opcode == 0 && this.type == 33) {
            codes |= 0x400;
        }
        BufferWriter writer = this.writeHeader(codes, 0, 1, 0, 0, this.status);
        byte[] data = this.getPacket().getData();
        int questionOffset = writer.getOffset();
        switch (this.opcode) {
            case 10240: 
            case 12288: {
                int offset = this.name.encodeName(data, questionOffset);
                writer.setOffset(offset);
                writer.writeInt2(32);
                writer.writeInt2(1);
                writer.writeInt4(0);
                writer.writeInt2(6);
                int flags = 0;
                if (this.name.isGroup()) {
                    flags |= 0x8000;
                }
                flags = Config.getWins() != null ? (flags |= 0x4000) : (flags |= 0);
                writer.writeInt2(flags);
                break;
            }
            case 0: {
                writer.setOffset(this.name.encodeName(data, questionOffset));
                writer.writeInt2(this.type);
                writer.writeInt2(1);
                if (this.type == 32 && this.status == 0) {
                    writer.writeInt4(0xFFFFFF);
                    writer.writeInt2((byte)(this.ips.size() * 6));
                    for (int i = 0; i < this.ips.size(); ++i) {
                        writer.writeInt2(0);
                        writer.writeBytes(((Inet4Address)this.ips.get(i)).getAddress(), 4);
                    }
                    break;
                }
                writer.writeInt4(0);
                int dataLength = 61;
                int numNames = 1;
                if (Config.isServer()) {
                    dataLength += 18;
                    ++numNames;
                }
                writer.writeInt2((byte)dataLength);
                writer.writeByte((byte)numNames);
                String hostName = Utility.getHostName();
                writer.writeBytes(new NetbiosName(hostName, 0).toBytes(), 16);
                writer.writeInt2(1024);
                if (Config.isServer()) {
                    writer.writeBytes(new NetbiosName(hostName, 32).toBytes(), 16);
                    writer.writeInt2(1024);
                }
                writer.writeZeros(42);
            }
        }
        TraceLog.get().exit(250);
        return writer.getOffset();
    }

    protected void send() throws NetbiosException {
        TraceLog.get().enter(250);
        NetbiosService sock = null;
        try {
            sock = this.service;
            if (null == this.packet || null == sock) {
                TraceLog.get().exit("NULL in send()", 250);
                return;
            }
            TraceLog.get().message("Sending packet to ip = " + this.getPacket().getAddress() + ", port = " + this.getPacket().getPort(), 250);
            this.capturePacket(false, sock, this.getPacket());
            sock.send(this.getPacket());
        }
        catch (Exception ex) {
            String msg = "Unable to send datagram to outside: " + ex.getMessage() + ", destIp = " + this.getPacket().getAddress() + ", port = " + this.getPacket().getPort();
            TraceLog.get().exit(msg, 250);
            throw new NetbiosException(msg + ex.getMessage(), -501);
        }
        TraceLog.get().exit(250);
    }

    private InetAddress[] switchStateAndCreateIps() throws NetbiosException {
        TraceLog.get().enter(250);
        InetAddress[] wins = Config.getWins();
        try {
            switch (this.state) {
                case 0: {
                    switch (this.type) {
                        case 0: 
                        case 32: {
                            this.state = 1;
                            if (this.multicast) {
                                TraceLog.get().exit(250);
                                return IpAddressHelper.getAllBroadcasts();
                            }
                            if (wins != null && wins.length > 0) {
                                TraceLog.get().exit(250);
                                return wins;
                            }
                            this.multicast = true;
                            Object[] results = IpAddressHelper.getAllBroadcasts();
                            TraceLog.get().exit("returning ips addresses = " + Arrays.toString(results), 250);
                            return results;
                        }
                        case 33: {
                            this.state = 1;
                            InetAddress[] ips = new InetAddress[1];
                            if (this.ips.size() > 0) {
                                ips[0] = (InetAddress)this.ips.get(0);
                            } else {
                                ips = null;
                            }
                            TraceLog.get().exit(250);
                            return ips;
                        }
                    }
                    TraceLog.get().exit(250);
                    return null;
                }
                case 1: {
                    if (this.multicast || this.name.getName().equals("*")) {
                        this.state = 3;
                        TraceLog.get().exit(250);
                        return null;
                    }
                    this.state = 1;
                    this.multicast = true;
                    Object[] results = IpAddressHelper.getAllBroadcasts();
                    TraceLog.get().exit("returning ips addresses = " + Arrays.toString(results), 250);
                    return results;
                }
            }
            TraceLog.get().exit(250);
            return null;
        }
        catch (Exception ex) {
            TraceLog.get().exit("State switch failed: ", ex, 250);
            throw new NetbiosException("State switch failed: " + ex.getMessage(), -506);
        }
    }

    private NameMessage[] createMessagesToSendOutside(NameMessage origin) throws NetbiosException {
        TraceLog.get().enter(250);
        InetAddress[] ips = this.switchStateAndCreateIps();
        if (null == ips) {
            TraceLog.get().exit(250);
            return null;
        }
        NameMessage[] msgs = new NameMessage[ips.length];
        for (int i = 0; i < ips.length; ++i) {
            int count;
            long eol;
            msgs[i] = new NameMessage(this.multicast, this);
            msgs[i].direction = 3;
            if (this.multicast) {
                eol = System.currentTimeMillis() + 1000L;
                count = 1;
                msgs[i].waitForResponse = false;
            } else {
                count = 1;
                eol = System.currentTimeMillis() + 1000L;
                count = 1;
                msgs[i].waitForResponse = true;
            }
            msgs[i].state = 0;
            msgs[i].setEol(eol);
            msgs[i].setCount(count);
            msgs[i].addPending();
            DatagramPacket packet = msgs[i].getPacket();
            packet.setAddress(ips[i]);
            packet.setPort(137);
        }
        TraceLog.get().exit(250);
        return msgs;
    }

    public NetbiosName getName() {
        return this.name;
    }

    private BufferWriter writeHeader(int codes, int questions, int answer, int authority, int additional, int err) {
        BufferWriter writer = new BufferWriter(this.getPacket().getData(), 0, true);
        writer.writeInt2(this.tranId);
        writer.writeInt2(codes |= err);
        writer.writeInt2(questions);
        writer.writeInt2(answer);
        writer.writeInt2(authority);
        writer.writeInt2(additional);
        return writer;
    }

    private void doRespond() throws NetbiosException {
        TraceLog.get().enter(250);
        try {
            this.status = 0;
            if (this.opcode == 0) {
                switch (this.type) {
                    case 32: {
                        if (this.ips != null && this.ips.size() != 0) break;
                        this.status = 3;
                        break;
                    }
                    case 33: {
                        if (this.name != null && this.name.getName() != null && this.name.getName().length() != 0) break;
                        this.status = 3;
                    }
                }
            }
            InetAddress destIp = IpAddressHelper.loopbackAddr;
            DatagramPacket packet = new DatagramPacket(new byte[1460], 1460);
            packet.setAddress(destIp);
            packet.setPort(this.port);
            this.setPacket(packet);
            this.encodeResponse();
            if (this.direction == 1 && this.type == 32 && this.ips != null && this.ips.size() > 0) {
                NetbiosDaemon.externalNames.add(this.name, this.eol, this.ips);
            }
            this.waitForResponse = false;
            this.send();
            this.removePending(0);
        }
        catch (Exception ex) {
            String msg = "Unable to send response to application: " + ex.getMessage() + ", destIp = " + this.packet.getAddress() + ", port = " + this.port;
            TraceLog.get().exit(msg, 250);
            throw new NetbiosException(msg + ex.getMessage(), -501);
        }
        TraceLog.get().exit(250);
    }

    public void wasResponded() throws NqException {
        NameMessage theMessage = (NameMessage)this.origin;
        if (null == theMessage || --theMessage.count > 0) {
            return;
        }
        if (3 == this.state) {
            theMessage.state = 3;
        }
        switch (this.opcode) {
            case 0: {
                if (this.multicast && this.name.getName().equals("*")) {
                    return;
                }
                theMessage.doQuery(this.name);
                break;
            }
            case 10240: 
            case 12288: {
                if (this.multicast && this.name.getName().equals("*")) {
                    return;
                }
                theMessage.doRegisterRelease(this.name);
            }
        }
    }

    public void register() throws NqException {
        this.ips.clear();
        this.type = 32;
        this.performByOpcode(10240);
    }

    public void release() throws NqException {
        this.ips.clear();
        this.type = 32;
        this.performByOpcode(12288);
    }

    public void queryByName() throws NqException {
        this.ips.clear();
        this.type = 32;
        this.performByOpcode(0);
    }

    public void queryByIp(InetAddress ip) throws NqException {
        TraceLog.get().enter("ip = ", ip, 250);
        if (!(ip instanceof Inet4Address)) {
            TraceLog.get().exit(250);
            return;
        }
        this.ips.clear();
        this.ips.add(ip);
        this.name = null;
        this.type = 33;
        this.multicast = false;
        this.performByOpcode(0);
        TraceLog.get().exit(250);
    }

    private void performByOpcode(int opcode) throws NqException {
        TraceLog.get().enter("opcode = " + opcode, 250);
        DatagramSocket sock = null;
        try {
            this.direction = 1;
            this.state = 0;
            sock = new DatagramSocket();
            DatagramPacket packet = this.getPacket();
            if (null == NetbiosDaemon.getTheDaemon()) {
                TraceLog.get().exit(250);
                return;
            }
            packet.setPort(NetbiosDaemon.getTheDaemon().internalName.getLocalPort());
            InetAddress destIp = IpAddressHelper.loopbackAddr;
            packet.setAddress(destIp);
            this.switchStateAndCreateIps();
            this.opcode = opcode;
            this.encodeRequest(this.ips.size() == 0 ? null : (Inet4Address)this.ips.get(0));
            if (this.multicast && this.name.getName().equals("*")) {
                TraceLog.get().exit(250);
                return;
            }
            TraceLog.get().message("Sending to ip = " + destIp + ", port = " + packet.getPort(), 250);
            this.capturePacket(false, sock, this.getPacket());
            sock.send(this.getPacket());
            sock.setSoTimeout(10000);
            packet.setLength(1460);
            packet.setPort(sock.getLocalPort());
            sock.receive(packet);
            this.capturePacket(true, sock, packet);
            byte[] src = packet.getData();
            BufferReader reader = new BufferReader(src, 0, true);
            reader.skip(2);
            short codes = reader.readInt2();
            int reply = codes & 0xF;
            TraceLog.get().message("codes = " + codes + ", reply = " + reply, 2000);
            if (0 == reply && opcode == 0) {
                reader.skip(8);
                reader.setOffset(this.name.parse(src, reader.getOffset()));
                if (this.name.getName().equals("*")) {
                    this.name = null;
                }
                reader.skip(8);
                this.ips.clear();
                this.readIps(reader, this);
            }
        }
        catch (SocketTimeoutException ex) {
            TraceLog.get().exit("Operation timeout: " + ex.getMessage() + ", name: " + this.name + ", destIP = " + sock.getInetAddress() + ", port = " + sock.getPort(), 250);
            throw new NetbiosException("Operation timeout: " + ex.getMessage() + ", name: " + this.name + ", destIP = " + sock.getInetAddress() + ", port = " + sock.getPort(), -505);
        }
        catch (Exception ex) {
            TraceLog.get().exit("Unable to perform NetBIOS operation: ", ex, 250);
            if (ex instanceof NqException) {
                NqException nqe = (NqException)ex;
                throw nqe;
            }
            throw new NetbiosException("Unable to perform NetBIOS operation: " + ex.getMessage(), -506);
        }
        finally {
            if (null != sock) {
                sock.close();
            }
        }
        TraceLog.get().exit(250);
    }

    private void readIps(BufferReader reader, NameMessage target) throws NqException {
        int numIps = reader.readInt2() / 6;
        target.ips = new Vector();
        while (numIps-- > 0) {
            reader.skip(2);
            byte[] nextIpBytes = new byte[]{reader.readByte(), reader.readByte(), reader.readByte(), reader.readByte()};
            try {
                target.ips.add(InetAddress.getByAddress(nextIpBytes));
            }
            catch (Exception ex) {
                throw new NetbiosException("illegal IP4 address: " + ex.getMessage(), -503);
            }
        }
    }

    public void setName(NetbiosName name) {
        this.name = name;
    }

    public String toString() {
        return "NameMessage [opcode=" + this.opcode + ", type=" + this.type + ", codes=" + this.codes + ", reply=" + this.reply + ", state=" + this.state + ", name=" + this.name + ", ips=" + this.ips + ", waitForResponse=" + this.waitForResponse + "]";
    }
}

