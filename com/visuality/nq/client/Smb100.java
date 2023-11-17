/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.auth.MD5;
import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.auth.SubjectCredentials;
import com.visuality.nq.client.AsyncConsumer;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.ClientException;
import com.visuality.nq.client.ClientSmb;
import com.visuality.nq.client.ClientUtils;
import com.visuality.nq.client.Directory;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Server;
import com.visuality.nq.client.Share;
import com.visuality.nq.client.Smb200;
import com.visuality.nq.client.Transport;
import com.visuality.nq.client.User;
import com.visuality.nq.common.Blob;
import com.visuality.nq.common.Buffer;
import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.CaptureInternal;
import com.visuality.nq.common.NamedRepository;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SecurityDescriptor;
import com.visuality.nq.common.Smb1Header;
import com.visuality.nq.common.Smb1Params;
import com.visuality.nq.common.SmbException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Utility;
import com.visuality.nq.resolve.NetbiosException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class Smb100
extends ClientSmb {
    private static AsyncConsumer readCallback = new ReadConsumer();
    private static AsyncConsumer writeCallback = new WriteConsumer();
    private static final String BACKSLASH = "\\";
    static final Command[] commandDescriptors = new Command[]{new Command(0, 0, 0, null), new Command(2000, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(100, 3, 0, null), new Command(100, 1, 0, null), new Command(0, 0, 0, null), new Command(1000, 1, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(200, 14, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(7, 1, 1, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(200, 12, 0, readCallback), new Command(200, 14, 0, writeCallback), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(2000, 15, 10, null), new Command(0, 0, 0, null), new Command(200, 1, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(300, 0, 0, null), new Command(300, 255, 17, null), new Command(65535, 255, 3, null), new Command(300, 2, 0, null), new Command(2000, 4, 7, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(3000, 19, 18, null), new Command(0, 0, 0, null), new Command(2000, 255, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(0, 0, 0, null), new Command(2000, 2, 1, null), new Command(0, 0, 0, null), new Command(100, 1, 0, null), new Command(0, 0, 0, null)};

    public Smb100() {
        this.setName("NT LM 0.12");
        this.setRevision((short)256);
        this.createBeforeMove = false;
        this.useFullPath = true;
    }

    public void freeContext(Object smbContext, Server server) {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void signalAllMatch(Transport transport) {
        Server server = transport.getServer();
        HashMap hashMap = server.expectedResponses;
        synchronized (hashMap) {
            Iterator iter = server.expectedResponses.entrySet().iterator();
            while (iter.hasNext()) {
                Object object;
                ClientSmb.Match match;
                Object next = iter.next().getValue();
                if (next instanceof ClientSmb.AsyncMatch) {
                    match = (ClientSmb.AsyncMatch)next;
                    TraceLog.get().message("match.mid = " + match.mid, 2000);
                    object = match.context;
                    synchronized (object) {
                        File.CummulativeAsynConsumer asyncConsumer = (File.CummulativeAsynConsumer)match.context;
                        try {
                            server.updateCredits(match);
                            TraceLog.get().message("match.creditCharge = " + match.creditCharge, 2000);
                            asyncConsumer.complete(new SmbException("Failed to receive response", -1073741267), 0L, match.context);
                        }
                        catch (NqException e) {
                            // empty catch block
                        }
                        continue;
                    }
                }
                if (!(next instanceof ClientSmb.Match)) continue;
                match = (ClientSmb.Match)next;
                object = match.syncObj;
                synchronized (object) {
                    match.matchExtraInfo |= 4;
                    TraceLog.get().message("Notify initiator of transaction", 300);
                    this.responseMatchNotify(match);
                }
            }
            server.expectedResponses.clear();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void response(Transport transport) throws NqException {
        ClientSmb.Match match;
        Smb1Header header;
        TraceLog.get().enter(300);
        Server server = transport.getServer();
        if (!transport.isConnected()) {
            TraceLog.get().error("Transport is not connected in response", 300);
            this.responseNotConnected(server);
            TraceLog.get().exit(300);
            return;
        }
        CaptureInternal capture = transport.getCapture();
        capture.capturePacketWriteStart(true, true, transport.getSocket());
        Buffer headerBuf = new Buffer(32);
        transport.receiveBytes(headerBuf, 0, 32);
        BufferReader reader = new BufferReader(headerBuf.data, 0, false);
        try {
            header = this.readHeader(reader, headerBuf);
            TraceLog.get().message("header.uid = " + header.uid + ", mid = " + header.mid, 2000);
        }
        catch (SmbException e) {
            TraceLog.get().caught(e, 300);
            throw new NetbiosException("Illegal response: " + e.toString(), -502);
        }
        int command = header.command;
        HashMap hashMap = server.expectedResponses;
        synchronized (hashMap) {
            match = (ClientSmb.Match)server.expectedResponses.get(header.mid);
            if (null == match) {
                server.transport.receiveEnd(headerBuf);
                capture.capturePacketWriteEnd();
                TraceLog.get().exit(300);
                return;
            }
            server.expectedResponses.remove(header.mid);
        }
        Response response = (Response)match.response;
        response.header = header;
        response.useAscii = 0 == (header.flags2 & 0x8000);
        TraceLog.get().message(header.toString(), 2000);
        NamedRepository users = server.users;
        Iterator iterator = users.values().iterator();
        User user = null;
        while (iterator.hasNext()) {
            User tmpUser = (User)iterator.next();
            if ((long)header.uid != tmpUser.uid) continue;
            user = tmpUser;
            break;
        }
        TraceLog.get().message("Found user=", user, 2000);
        if (-1073741802 != header.status && 116 != header.command) {
            match.hdrBuf = new byte[32];
            System.arraycopy(headerBuf.data, 0, match.hdrBuf, 0, 32);
        }
        match.status = header.status;
        if (null != Smb100.commandDescriptors[command].consumer) {
            response.tailLen = transport.getReceivingRemain();
            response.wasReceived = true;
            if (match.status == -1073741309 || match.status == -1073740964 || -1073741816 == match.status) {
                TraceLog.get().message("Invalid session for mid=" + match.mid, 2000);
                this.responseUserSessionDeletedOrExpired(server, match);
            }
            Smb100.commandDescriptors[command].consumer.complete(new SmbException(match.status), response.tailLen, match);
        } else if (transport.getReceivingRemain() > 0) {
            response.tailLen = transport.getReceivingRemain();
            response.buffer = Buffer.getNewBuffer(32 + response.tailLen);
            int tailLen = transport.receiveBytes(response.buffer, 32, response.tailLen);
            if (response.tailLen == tailLen) {
                if (-1073741802 != header.status && 116 != header.command && null != user && user.useSigning) {
                    System.arraycopy(match.hdrBuf, 0, response.buffer.data, 0, 32);
                }
                response.reader = new BufferReader(response.buffer.data, 32, false);
            }
        }
        transport.receiveEnd(response.buffer);
        response.wasReceived = true;
        server.updateCredits(1);
        TraceLog.get().message("Notify initiator of transaction", 300);
        this.responseMatchNotify(match);
        capture.capturePacketWriteEnd();
        TraceLog.get().exit(300);
    }

    public Object allocateContext(Server server) {
        return new Context();
    }

    private void writeByteCount(Request request) {
        int offset = request.writer.getOffset();
        int byteCount = offset - request.byteCountIdx - 2;
        request.writer.setOffset(request.byteCountIdx);
        request.writer.writeInt2(byteCount);
        request.writer.setOffset(offset);
    }

    private void writeByteCount(Request request, long moreData) {
        int offset = request.writer.getOffset();
        int byteCount = offset - request.byteCountIdx - 2;
        request.writer.setOffset(request.byteCountIdx);
        request.writer.writeInt2(byteCount + (int)moreData);
        request.writer.setOffset(offset);
    }

    private void markWordCount(Request request, int wordCount) {
        request.wordCountIdx = request.writer.getOffset();
        request.writer.writeByte((byte)wordCount);
    }

    private void markByteCount(Request request, int byteCount) {
        request.byteCountIdx = request.writer.getOffset();
        request.writer.writeInt2(byteCount);
    }

    private Request prepareSingleRequest(Server server, int opcode) {
        TraceLog.get().enter(300);
        Request request = new Request();
        request.buffer = Buffer.getNewBuffer(Smb100.commandDescriptors[opcode].requestBufferSize);
        BufferWriter writer = new BufferWriter(request.buffer.data, 0, false);
        boolean useSigning = null == server.masterUser ? false : server.masterUser.useSigning;
        Smb1Header header = new Smb1Header();
        header.command = opcode;
        header.status = 0;
        header.flags = 24;
        header.flags2 = (server.useAscii ? 0 : 32768) | 0x4000 | 0x40 | 1 | (0 != (server.capabilities & 2) ? 4096 : 0) | (server.useExtendedSecurity ? 2048 : 0) | (useSigning ? 4 : 0);
        header.pid = 0;
        header.tid = 0;
        Context context = (Context)server.smbContext;
        header.mid = null == context ? 0 : context.mid;
        header.uid = 0;
        header.signature = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        request.header = header;
        request.writer = writer;
        TraceLog.get().exit(300);
        return request;
    }

    private Request prepareSingleRequest(User user, int opcode) {
        Request request = this.prepareSingleRequest(user.getServer(), opcode);
        request.header.uid = (int)user.uid;
        return request;
    }

    private Request prepareSingleRequest(Share share, int opcode) {
        Request request = this.prepareSingleRequest(share.getUser(), opcode);
        request.header.tid = share.tid;
        request.header.flags2 = 0 != (share.flags & 1) ? (request.header.flags2 |= 0x1000) : (request.header.flags2 &= 0xFFFFEFFF);
        return request;
    }

    private Smb1Header readHeader(BufferReader reader, Buffer buffer) throws NqException {
        TraceLog.get().enter(300);
        byte[] protocolId = new byte[4];
        reader.readBytes(protocolId, 4);
        if (!Arrays.equals(protocolId, Smb1Params.smbProtocolId)) {
            throw new SmbException("Unexpected protocol ID in response: " + Integer.toHexString(protocolId[0]) + ":" + Integer.toHexString(protocolId[1]) + ":" + Integer.toHexString(protocolId[2]) + ":" + Integer.toHexString(protocolId[3]), -1073741811);
        }
        Smb1Header header = new Smb1Header();
        header.command = reader.readByte() & 0xFF;
        header.status = reader.readInt4();
        header.flags = reader.readByte();
        header.flags2 = reader.readInt2();
        header.pid = reader.readInt2() << 16;
        header.signature = new byte[8];
        reader.readBytes(header.signature, 8);
        reader.skip(2);
        header.tid = reader.readInt2();
        header.pid += reader.readInt2();
        header.uid = reader.readInt2();
        header.mid = reader.readInt2() & 0xFFFF;
        TraceLog.get().exit(300);
        return header;
    }

    private Response prepareResponseAndReadHeader(Request request, Buffer buffer) throws NqException {
        BufferReader reader;
        Response response = new Response();
        response.buffer = buffer;
        response.reader = reader = new BufferReader(buffer.data, buffer.offset, false);
        response.header = this.readHeader(reader, buffer);
        return response;
    }

    private void writeHeader(Request request) {
        TraceLog.get().enter(300);
        BufferWriter writer = request.writer;
        Smb1Header header = request.header;
        writer.skip(4);
        request.headerStart = writer.getOffset();
        writer.writeBytes(Smb1Params.smbProtocolId);
        writer.writeByte((byte)header.command);
        writer.writeInt4(header.status);
        writer.writeByte((byte)header.flags);
        writer.writeInt2(header.flags2);
        writer.writeInt2(header.pid >> 16);
        writer.writeBytes(header.signature);
        writer.writeInt2(0);
        writer.writeInt2(header.tid);
        writer.writeInt2(header.pid);
        writer.writeInt2(header.uid);
        writer.writeInt2(header.mid);
        int command = header.command;
        if (Smb100.commandDescriptors[command].requestWordCount != 255) {
            writer.writeByte((byte)Smb100.commandDescriptors[command].requestWordCount);
        }
        TraceLog.get().exit(300);
    }

    private void writeAndX(Request request) {
        request.writer.writeByte((byte)-1);
        request.writer.writeByte((byte)0);
        request.writer.writeInt2(0);
    }

    private int msgNumber(Smb1Header header) {
        return header.pid * 65536 + header.mid;
    }

    private void exchangeEmptyCommand(Share share, int opcode) throws NqException {
        Server server = share.getUser().getServer();
        Request request = this.prepareSingleRequest(share, opcode);
        this.writeHeader(request);
        request.writer.writeInt2(0);
        int res = this.sendReceive(server, share.getUser(), request, null);
    }

    private Request composeCreateFileRequest(File file) throws NqException {
        TraceLog.get().enter(300);
        Share share = file.share;
        int cmd = share.info.type == 1 ? 192 : 162;
        Request request = this.prepareSingleRequest(share, cmd);
        boolean useAscii = share.getUser().getServer().useAscii;
        this.writeHeader(request);
        BufferWriter writer = request.writer;
        String path = file.getLocalPathFromShare();
        if (file.isDfs() && share.isDfs()) {
            String serverName = file.server.getName();
            path = BACKSLASH + serverName + BACKSLASH + share.getName() + BACKSLASH + path;
        } else if (file.isDir() || share.isIpc) {
            path = BACKSLASH + path;
        }
        switch (cmd) {
            case 192: {
                writer.writeInt2(0);
                writer.writeInt2(1);
                this.markByteCount(request, 0);
                writer.writeByte((byte)(useAscii ? 4 : 0));
                if (!useAscii) {
                    writer.align(4, 2);
                    writer.writeString(path, true);
                } else {
                    writer.writeBytes(path.getBytes());
                    writer.writeByte((byte)0);
                }
                this.writeByteCount(request);
                break;
            }
            case 162: {
                writer.writeByte((byte)24);
                this.writeAndX(request);
                writer.writeByte((byte)0);
                int nameLen = useAscii ? path.length() : path.length() * 2;
                writer.writeInt2(nameLen);
                writer.writeInt4(0);
                writer.writeInt4(0);
                writer.writeInt4(file.accessMask);
                writer.writeInt4(0);
                writer.writeInt4(0);
                writer.writeInt4(file.info.getAttributes());
                writer.writeInt4(file.shareAccess);
                writer.writeInt4(file.disposition);
                writer.writeInt4(file.createOptions);
                writer.writeInt4(2);
                writer.writeByte((byte)3);
                this.markByteCount(request, 0);
                if (!useAscii) {
                    writer.align(4, 2);
                    writer.writeString(path, true);
                } else {
                    writer.writeBytes(path.getBytes());
                    writer.writeByte((byte)0);
                }
                this.writeByteCount(request);
                break;
            }
            default: {
                throw new SmbException("Internal error");
            }
        }
        TraceLog.get().exit(300);
        return request;
    }

    private void parseCreateFileResponse(Response response, File file) throws NqException {
        TraceLog.get().enter(300);
        BufferReader reader = response.reader;
        switch (response.header.command) {
            case 192: {
                reader.skip(1);
                file.fid[0] = reader.readByte();
                file.fid[1] = reader.readByte();
                break;
            }
            case 162: {
                reader.skip(6);
                file.fid[0] = reader.readByte();
                file.fid[1] = reader.readByte();
                reader.skip(4);
                file.info.setCreationTime(reader.readLong());
                file.info.setLastAccessTime(reader.readLong());
                file.info.setLastWriteTime(reader.readLong());
                file.info.setLastWriteTime(reader.readLong());
                file.info.setAttributes(reader.readInt4());
                file.info.setAllocationSize(reader.readLong());
                file.info.setEof(reader.readInt8());
                break;
            }
        }
        TraceLog.get().exit(300);
    }

    private Request composeCloseRequest(File file) {
        Share share = file.share;
        int cmd = share.info.type == 1 ? 194 : 4;
        Request request = this.prepareSingleRequest(share, cmd);
        BufferWriter writer = request.writer;
        switch (cmd) {
            case 194: {
                this.writeHeader(request);
                this.writeFid(writer, file);
                break;
            }
            case 4: {
                request.header.flags2 |= 0x8000;
                this.writeHeader(request);
                this.writeFid(writer, file);
                writer.writeInt4(0);
                writer.writeInt4(0);
                break;
            }
        }
        this.markByteCount(request, 0);
        return request;
    }

    private void writeFid(BufferWriter writer, File file) {
        writer.writeByte(file.fid[0]);
        writer.writeByte(file.fid[1]);
    }

    public int doNegotiate(Server server, Blob blob) throws NqException {
        return this.doNegotiate(server, blob, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int doNegotiate(Server server, Blob blob, short[] dialects) throws NqException {
        int rawBuffSize;
        Buffer receiveBuffer;
        TraceLog.get().enter(250);
        Request request = this.prepareSingleRequest(server, 114);
        if (Client.isSigningEnabled()) {
            request.header.flags2 |= 4;
        }
        this.writeHeader(request);
        this.markWordCount(request, 0);
        this.markByteCount(request, 0);
        if (Client.getDialects().hasSmb((short)256)) {
            request.writer.writeByte((byte)2);
            request.writer.writeBytes("NT LM 0.12".getBytes());
            request.writer.writeByte((byte)0);
        }
        if (!this.solo && Client.getDialects().supportSmb2()) {
            if (Client.getDialects().hasSmb((short)514)) {
                request.writer.writeByte((byte)2);
                request.writer.writeBytes("SMB 2.002".getBytes());
                request.writer.writeByte((byte)0);
            }
            request.writer.writeByte((byte)2);
            request.writer.writeBytes("SMB 2.???".getBytes());
            request.writer.writeByte((byte)0);
        }
        this.writeByteCount(request);
        int dataLen = request.writer.getOffset() - 4;
        CaptureInternal capture = server.transport.getCapture();
        Server server2 = server;
        synchronized (server2) {
            try {
                capture.capturePacketWriteStart(true, false, server.transport.getSocket());
                server.transport.send(request.buffer.data, 0, dataLen, dataLen);
                capture.capturePacketWriteEnd();
            }
            catch (Exception e) {
                TraceLog.get().caught(e, 250);
                throw (SmbException)Utility.throwableInitCauseException(new SmbException("Send failed : " + e.getMessage(), -1073741267), e);
            }
        }
        try {
            capture.capturePacketWriteStart(true, true, server.transport.getSocket());
            receiveBuffer = server.transport.receiveAll();
        }
        catch (NetbiosException e) {
            TraceLog.get().caught(e, 250);
            throw new NetbiosException("Receive failed: " + e.getMessage(), -502);
        }
        if (receiveBuffer.data[0] == -2) {
            server.negoSmb = new Smb200();
            int result = ((Smb200)server.negoSmb).doNegotiateResponse(server, receiveBuffer, blob, dialects);
            TraceLog.get().exit(250);
            return result;
        }
        server.smb = server.negoSmb;
        Response response = this.prepareResponseAndReadHeader(request, receiveBuffer);
        int res = response.header.status;
        server.negoAscii = server.useAscii = 0 == (response.header.flags2 & 0x8000);
        BufferReader reader = response.reader;
        byte wordCount = reader.readByte();
        server.capabilities = 0;
        short dialectIndex = reader.readInt2();
        if (dialectIndex == -1) {
            TraceLog.get().exit(250);
            throw new ClientException("No dialect negotiated", -110);
        }
        byte securityMode = reader.readByte();
        boolean bl = server.userSecurity = 0 != (securityMode & 1);
        if (0 != (securityMode & 4)) {
            server.capabilities |= 1;
            if (0 != (securityMode & 8) && !Client.isSigningEnabled()) {
                TraceLog.get().exit(250);
                throw new ClientException("Server request signature", -103);
            }
        }
        if (0 == (securityMode & 2)) {
            TraceLog.get().exit(250);
            throw new ClientException("Plain passwords not supported", -110);
        }
        reader.readInt2();
        reader.skip(2);
        server.maxTrans = reader.readInt4();
        if (server.maxTrans >= 131072) {
            server.maxTrans = 65535;
        }
        int n = rawBuffSize = (rawBuffSize = reader.readInt4()) != 0 ? rawBuffSize : server.maxTrans;
        if (rawBuffSize == 0 || server.maxTrans == 0) {
            TraceLog.get().exit(250);
            throw new ClientException("Illegal max sizes", -103);
        }
        server.maxWrite = server.maxRead = server.maxTrans - 64;
        reader.skip(4);
        int capabilities = reader.readInt4();
        if (0 != (0x1000 & capabilities)) {
            server.capabilities |= 2;
        }
        if (0 != (0x2000 & capabilities)) {
            server.capabilities |= 4;
        }
        if (0 == (1 & server.capabilities)) {
            if (0 != (0x4000 & capabilities)) {
                server.maxRead = 61440;
            }
            if (0 != (0x8000 & capabilities)) {
                server.maxWrite = rawBuffSize - 64;
            }
        }
        reader.skip(10);
        byte challengeLen = reader.readByte();
        short byteCount = reader.readInt2();
        if (0 == byteCount) {
            TraceLog.get().exit(250);
            throw new ClientException("Short Negotiate not supported", -110);
        }
        if (0 != (capabilities & Integer.MIN_VALUE)) {
            reader.skip(16);
            blob.data = new byte[byteCount - 16];
            blob.len = blob.data.length;
            reader.readBytes(blob.data, byteCount - 16);
        } else {
            server.useExtendedSecurity = false;
            server.firstSecurityBlob.len = challengeLen;
            server.firstSecurityBlob.data = new byte[challengeLen];
            if (challengeLen > 0) {
                reader.readBytes(server.firstSecurityBlob.data, challengeLen);
            }
        }
        Object object = server.smbContextSync;
        synchronized (object) {
            server.smbContext = new Context();
        }
        server.smb = this;
        capture.capturePacketWriteEnd();
        TraceLog.get().exit(250);
        return 0;
    }

    public int doSessionSetup(User user, Blob pass1, Blob pass2) throws NqException {
        String userName;
        int decoratorIdx;
        TraceLog.get().enter(250);
        Server server = user.getServer();
        if (server.smbContext == null) {
            TraceLog.get().exit(250);
            throw new SmbException("Server context is missing");
        }
        server.updateCredits(50 - server.creditHandler.getCredits());
        TraceLog.get().message("server.credits = " + server.creditHandler.getCredits(), 2000);
        Context context = (Context)server.smbContext;
        context.mid = context.mid <= 2 ? 0 : context.mid;
        Request request = this.prepareSingleRequest(user, 115);
        if (0 != (server.capabilities & 1)) {
            request.header.flags2 |= 4;
        }
        this.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.writeByte((byte)13);
        this.writeAndX(request);
        writer.writeInt2(65535);
        writer.writeInt2(server.creditHandler.getCredits());
        writer.writeInt2(server.vcNumber);
        writer.writeInt4(0);
        writer.writeInt2(pass1.data == null ? 0 : pass1.len);
        writer.writeInt2(pass2.data == null ? 0 : pass2.len);
        writer.writeInt4(0);
        writer.writeInt4(49244);
        this.markByteCount(request, 0);
        if (pass1.data != null) {
            writer.writeBytes(pass1.data, pass1.len);
        }
        if (pass2.data != null) {
            writer.writeBytes(pass2.data, pass2.len);
        }
        if (!server.useAscii) {
            writer.align(request.headerStart, 2);
        }
        if ((decoratorIdx = (userName = user.getCredentials() instanceof PasswordCredentials ? ((PasswordCredentials)user.getCredentials()).getUser() : ((SubjectCredentials)user.getCredentials()).getUser()).indexOf(64)) >= 0) {
            writer.writeBytes(userName.substring(0, decoratorIdx).getBytes());
        } else if (server.useAscii) {
            writer.writeBytes(userName.getBytes());
            writer.writeByte((byte)0);
        } else {
            writer.writeString(userName, true);
        }
        String domain = user.getCredentials() instanceof PasswordCredentials ? ((PasswordCredentials)user.getCredentials()).getDomain() : ((SubjectCredentials)user.getCredentials()).getDomain();
        if (server.useAscii) {
            writer.writeBytes(domain.getBytes());
            writer.writeByte((byte)0);
            writer.writeBytes(System.getProperty("os.name").getBytes());
            writer.writeByte((byte)0);
            writer.writeBytes("jNQ".getBytes());
            writer.writeByte((byte)0);
        } else {
            writer.writeString(domain, true);
            writer.writeString(System.getProperty("os.name"), true);
            writer.writeString("jNQ", true);
        }
        this.writeByteCount(request);
        int res = this.sendReceive(server, user, request, null);
        if (0 != res) {
            user.uid = 0L;
            TraceLog.get().exit(250);
            return res;
        }
        Response response = request.response;
        response.reader.skip(5);
        short action = response.reader.readInt2();
        if (0 != (action & 1)) {
            user.isGuest = true;
        }
        user.uid = response.header.uid;
        if (null == server.masterUser && !user.isAnonymous && !user.isGuest) {
            server.masterUser = user;
            context.mid = 2;
        }
        TraceLog.get().exit(250);
        return 0;
    }

    public int doSessionSetupExtended(User user, Blob outBlob, Blob inBlob) throws NqException {
        int res;
        TraceLog.get().enter(250);
        Server server = user.getServer();
        server.useExtendedSecurity = true;
        if (server.smbContext == null) {
            TraceLog.get().exit(250);
            throw new SmbException("Server context is missing");
        }
        Context context = (Context)server.smbContext;
        context.mid = context.mid <= 2 ? 0 : context.mid;
        Request request = this.prepareSingleRequest(user, 115);
        if (0 != (server.capabilities & 1) && Client.isSigningEnabled()) {
            request.header.flags2 |= 4;
        }
        this.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.writeByte((byte)12);
        this.writeAndX(request);
        writer.writeInt2(65535);
        writer.writeInt2(server.creditHandler.getCredits());
        writer.writeInt2(server.vcNumber);
        writer.writeInt4(0);
        writer.writeInt2(outBlob.len);
        writer.writeInt4(0);
        writer.writeInt4(-2147434404);
        this.markByteCount(request, 0);
        writer.writeBytes(outBlob.data, outBlob.len);
        writer.align(0, 2);
        writer.writeString(System.getProperty("os.name"), true);
        writer.writeString("jNQ", true);
        this.writeByteCount(request);
        try {
            res = this.sendReceive(server, user, request, null);
        }
        catch (NqException e) {
            user.uid = 0L;
            TraceLog.get().caught(e, 250);
            throw e;
        }
        if (0 != res && -1073741802 != res) {
            user.uid = 0L;
            TraceLog.get().exit(250);
            throw new SmbException("Session setup error", res);
        }
        Response response = request.response;
        BufferReader reader = response.reader;
        reader.skip(5);
        short action = reader.readInt2();
        user.isGuest = 0 != (action & 1);
        short blobLen = reader.readInt2();
        reader.skip(2);
        int blobPos = reader.getOffset();
        inBlob.data = new byte[blobLen];
        System.arraycopy(reader.getSrc(), blobPos, inBlob.data, 0, blobLen);
        inBlob.len = blobLen;
        user.uid = response.header.uid;
        if (null == server.masterUser && !user.isAnonymous && !user.isGuest) {
            server.masterUser = user;
            context.mid = 2;
        }
        server.updateCredits(1);
        TraceLog.get().exit(250);
        return res;
    }

    public void doLogOff(User user) throws NqException {
        TraceLog.get().enter(250);
        Server server = user.getServer();
        Request request = this.prepareSingleRequest(user, 116);
        this.writeHeader(request);
        this.writeAndX(request);
        BufferWriter writer = request.writer;
        writer.writeInt2(0);
        int res = this.sendReceive(server, user, request, null);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Logoff error", res);
        }
        TraceLog.get().exit(250);
    }

    public void doTreeConnect(Share share) throws NqException {
        TraceLog.get().enter(250);
        User user = share.getUser();
        Server server = user.getServer();
        Request request = this.prepareSingleRequest(share, 117);
        this.writeHeader(request);
        BufferWriter writer = request.writer;
        this.writeAndX(request);
        writer.writeInt2(0);
        if (!server.userSecurity && !share.isIpc) {
            throw new ClientException("Share level security no supported", -110);
        }
        writer.writeInt2(1);
        this.markByteCount(request, 1);
        writer.writeByte((byte)0);
        if (!server.useAscii) {
            writer.align(0, 2);
        }
        String path = server.useName ? ClientUtils.composeRemotePathToShare(server.calledName, share.getName().toUpperCase()) : ClientUtils.composeRemotePathToShare(server.ips[0].getHostAddress().toString(), share.getName().toUpperCase());
        String SERVICE = "?????";
        if (server.useAscii) {
            writer.writeBytes(path.getBytes());
            writer.writeByte((byte)0);
        } else {
            writer.writeString(path, true);
        }
        writer.writeBytes("?????".getBytes());
        writer.writeByte((byte)0);
        this.writeByteCount(request);
        int res = this.sendReceive(server, user, request, null);
        if (0 != res) {
            throw new SmbException("Share doesn't exist or is not reachable", res);
        }
        Response response = request.response;
        share.tid = response.header.tid;
        BufferReader reader = response.reader;
        reader.skip(5);
        short optionalSupport = reader.readInt2();
        share.flags = 0;
        if (0 != (optionalSupport & 2)) {
            share.flags |= 1;
        }
        reader.skip(2);
        String shareName = new String(reader.getSrc(), 0, reader.getOffset());
        if (shareName.equals("LPT1:")) {
            share.info.type = 1;
            share.isPrinter = true;
        } else {
            share.info.type = 0;
        }
        TraceLog.get().exit(250);
    }

    public void doTreeDisconnect(Share share) throws NqException {
        this.exchangeEmptyCommand(share, 113);
    }

    public void doChangeNotify(File dir, int completionFilter, AsyncConsumer consumer) {
    }

    public void doCancel(File file) {
    }

    public void doCreate(File file) throws NqException {
        TraceLog.get().enter(250);
        Request request = this.composeCreateFileRequest(file);
        User user = file.share.getUser();
        int res = this.sendReceive(user.getServer(), user, request, null);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("create error", res);
        }
        TraceLog.get().exit(250);
        this.parseCreateFileResponse(request.response, file);
    }

    public void doRestoreHandle(File file) throws NqException {
        throw new ClientException("SMB1 does not support reconnect", -110);
    }

    public int doClose(File file) throws NqException {
        TraceLog.get().enter(250);
        Request request = this.composeCloseRequest(file);
        User user = file.share.getUser();
        int res = this.sendReceive(user.getServer(), user, request, null);
        if (0 != res) {
            throw new SmbException("close error", res);
        }
        TraceLog.get().exit(250);
        return res;
    }

    public void doQueryDfsReferrals(Share share, String path, ClientSmb.ParseReferral parser) throws NqException {
        TraceLog.get().enter(250);
        User user = share.getUser();
        Server server = user.getServer();
        Request request = this.prepareSingleRequest(share, 50);
        this.writeHeader(request);
        BufferWriter writer = request.writer;
        this.markTrans2Start(request, 16);
        writer.writeInt2(4);
        writer.writeString(path, true);
        this.markTransData(request);
        this.writeTrans2(server, request, 0, 0);
        int res = this.sendReceive(server, user, request, null);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Query Dfs referrals error", res);
        }
        Response response = request.response;
        this.parseTrans(response);
        this.setTransData(response);
        parser.parse(response.reader);
        TraceLog.get().exit(250);
    }

    public void doFindOpen(Directory search) throws NqException {
        TraceLog.get().enter(250);
        SearchContext context = new SearchContext();
        search.context = context;
        context.findFirst = true;
        context.eos = false;
        context.sidAvailable = false;
        TraceLog.get().exit(250);
    }

    private void markTransParams(Request request) {
        request.paramsIdx = request.writer.getOffset();
    }

    private void markTransData(Request request) {
        request.dataIdx = request.writer.getOffset();
    }

    private void writeTrans2(Server server, Request request, int maxParamCount, int maxSetupCount) {
        TraceLog.get().enter(300);
        BufferWriter writer = request.writer;
        int maxBuffer = writer.getDest().length;
        if (maxBuffer > server.maxTrans - 100) {
            maxBuffer = server.maxTrans - 100;
        }
        this.writeByteCount(request);
        int endIdx = writer.getOffset();
        writer.setOffset(request.tranIdx);
        writer.writeInt2(request.dataIdx - request.paramsIdx);
        writer.writeInt2(endIdx - request.dataIdx);
        writer.writeInt2(maxParamCount);
        writer.writeInt2(maxBuffer > 65535 ? 65535 : maxBuffer);
        writer.writeByte((byte)maxSetupCount);
        writer.writeByte((byte)0);
        writer.writeInt2(0);
        writer.writeInt4(0);
        writer.writeInt2(0);
        writer.writeInt2(request.dataIdx - request.paramsIdx);
        writer.writeInt2(request.paramsIdx - 4);
        int dataCount = endIdx - request.dataIdx;
        writer.writeInt2(dataCount);
        writer.writeInt2(dataCount == 0 ? 0 : request.dataIdx - 4);
        writer.setOffset(endIdx);
        TraceLog.get().exit(300);
    }

    private void markTrans2Start(Request request, int code) {
        TraceLog.get().enter(300);
        BufferWriter writer = request.writer;
        request.tranIdx = writer.getOffset();
        writer.skip(26);
        writer.writeByte((byte)1);
        writer.writeByte((byte)0);
        writer.writeInt2(code);
        this.markByteCount(request, 0);
        writer.writeZeros(3);
        this.markTransParams(request);
        TraceLog.get().exit(300);
    }

    private void parseTrans(Response response) throws NqException {
        BufferReader reader = response.reader;
        reader.skip(9);
        response.paramsIdx = reader.readInt2();
        reader.skip(2);
        response.dataCount = reader.readInt2();
        response.dataIdx = reader.readInt2();
    }

    private void setTransData(Response response) {
        response.reader.setOffset(response.dataIdx);
    }

    private void setTransParams(Response response) {
        response.reader.setOffset(response.paramsIdx);
    }

    public boolean doFindMore(Directory search) throws NqException {
        TraceLog.get().enter(250);
        SearchContext context = (SearchContext)search.context;
        if (context.eos) {
            context.sid = 65535;
            return false;
        }
        Server server = search.server;
        Share share = search.share;
        Request request = this.prepareSingleRequest(share, 50);
        this.writeHeader(request);
        this.markTrans2Start(request, context.findFirst ? 1 : 2);
        int level = 260;
        BufferWriter writer = request.writer;
        if (context.findFirst) {
            String tempPattern = (search.path.startsWith(BACKSLASH) || 0 == search.path.length() ? "" : BACKSLASH) + search.path;
            String pattern = tempPattern + BACKSLASH + search.wildcards;
            writer.writeInt2(22);
            writer.writeInt2(65534);
            writer.writeInt2(6);
            writer.writeInt2(level);
            writer.writeInt4(0);
            if (server.useAscii) {
                writer.writeBytes(pattern.getBytes());
                writer.writeByte((byte)0);
            } else {
                writer.writeString(pattern, true);
            }
        } else {
            String pattern = BACKSLASH + search.entry.name;
            writer.writeInt2(context.sid);
            writer.writeInt2(65534);
            writer.writeInt2(level);
            writer.writeInt4(context.resumeKey);
            writer.writeInt2(14);
            if (server.useAscii) {
                writer.writeBytes(pattern.getBytes());
                writer.writeByte((byte)0);
            } else {
                writer.writeString(pattern, true);
            }
        }
        this.markTransData(request);
        this.writeTrans2(server, request, 10, 0);
        int res = this.sendReceive(server, share.getUser(), request, null);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("find more error", res);
        }
        Response response = request.response;
        this.parseTrans(response);
        this.setTransParams(response);
        BufferReader reader = response.reader;
        if (context.findFirst) {
            context.sid = reader.readInt2();
            context.findFirst = false;
        }
        context.sidAvailable = true;
        short searchCount = reader.readInt2();
        if (searchCount == 0) {
            context.eos = true;
            context.sidAvailable = false;
            context.sid = 65535;
            TraceLog.get().exit(250);
            return false;
        }
        context.eos = 0 != reader.readInt2();
        this.setTransData(response);
        search.setParser(reader.getSrc(), reader.getOffset(), searchCount);
        TraceLog.get().exit(250);
        return true;
    }

    public int doFindClose(Directory search) throws NqException {
        TraceLog.get().enter(250);
        SearchContext context = (SearchContext)search.context;
        if (context.eos || !context.sidAvailable) {
            TraceLog.get().exit(250);
            return 0;
        }
        Server server = search.server;
        Request request = this.prepareSingleRequest(server, 52);
        request.header.tid = search.share.tid;
        request.header.uid = (int)search.share.user.uid;
        Response response = request.response;
        BufferWriter writer = request.writer;
        this.writeHeader(request);
        writer.writeInt2(context.sid);
        this.markByteCount(request, 0);
        int res = server.smb.sendReceive(server, search.share.user, request, response);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("find close error", res);
        }
        TraceLog.get().exit(250);
        return res;
    }

    public void doWrite(File file, Buffer buffer, AsyncConsumer callback, Object context, Object hook) throws NqException {
        TraceLog.get().enter(250);
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        Request request = this.prepareSingleRequest(share, 47);
        this.writeHeader(request);
        BufferWriter writer = request.writer;
        this.writeAndX(request);
        this.writeFid(writer, file);
        writer.writeInt4((int)(file.getPosition() % 0x100000000L));
        writer.writeInt4(share.isIpc ? 0 : -1);
        writer.writeInt2(0);
        writer.writeInt2(0);
        writer.writeInt2(buffer.dataLen / 65536);
        writer.writeInt2(buffer.dataLen % 65536);
        int dataOffsetIdx = writer.getOffset();
        writer.writeInt2(0);
        writer.writeInt4((int)(file.getPosition() / 0x100000000L));
        this.markByteCount(request, 0);
        writer.align(0, 4);
        this.writeByteCount(request, buffer.dataLen);
        request.tail = buffer;
        int tempIdx = writer.getOffset();
        writer.setOffset(dataOffsetIdx);
        writer.writeInt2(tempIdx - 4);
        writer.setOffset(tempIdx);
        ClientSmb.AsyncMatch match = new ClientSmb.AsyncMatch();
        match.response = new Response();
        match.server = server;
        match.isResponseAllocated = true;
        match.matchExtraInfo = 1;
        match.consumer = callback;
        match.context = context;
        match.hook = hook;
        match.timeCreated = System.currentTimeMillis();
        match.timeout = Client.getSmbTimeout();
        match.userId = user.uid;
        int res = this.sendRequest(server, user, request, match);
        TraceLog.get().exit(250);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("write error", res);
        }
        TraceLog.get().exit(250);
    }

    protected static void handleNetbiosException(Throwable status, ClientSmb.AsyncMatch match, String text) throws NqException {
        if (status instanceof SmbException && -1073741267 == ((SmbException)status).getErrCode()) {
            match.consumer.complete(new SmbException("need to retry the operation", -1073741267), 0L, match.context);
        } else {
            match.consumer.complete(new ClientException(text, -106), 0L, match.context);
        }
    }

    public void doRead(File file, Buffer buffer, AsyncConsumer callback, Object context, Object hook) throws NqException {
        TraceLog.get().enter(250);
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        Request request = this.prepareSingleRequest(share, 46);
        this.writeHeader(request);
        BufferWriter writer = request.writer;
        this.writeAndX(request);
        this.writeFid(writer, file);
        writer.writeInt4((int)(file.getPosition() % 0x100000000L));
        writer.writeInt2(buffer.dataLen & 0xFFFF);
        writer.writeInt2(0);
        if (share.isIpc) {
            writer.writeInt4(-1);
            writer.writeInt2(0);
        } else {
            writer.writeInt4((int)((long)buffer.dataLen / 0x100000000L));
            writer.writeInt2((buffer.dataLen & 0xFFFF0000) == -65536 ? 65535 : 0);
        }
        writer.writeInt4((int)(file.getPosition() / 0x100000000L));
        this.markByteCount(request, 0);
        this.writeByteCount(request);
        ClientSmb.AsyncMatch match = new ClientSmb.AsyncMatch();
        match.response = new Response();
        match.server = server;
        match.isResponseAllocated = true;
        match.matchExtraInfo = 2;
        match.consumer = callback;
        match.context = context;
        match.hook = hook;
        match.timeCreated = System.currentTimeMillis();
        match.timeout = Client.getSmbTimeout();
        match.userId = user.uid;
        match.buffer = new Buffer(buffer.data, buffer.offset, buffer.dataLen);
        int res = this.sendRequest(server, user, request, match);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("read error", res);
        }
        TraceLog.get().exit(250);
    }

    private void markNtTransStart(Request request, int code) {
        TraceLog.get().enter(300);
        BufferWriter writer = request.writer;
        request.tranIdx = writer.getOffset();
        writer.skip(36);
        writer.writeInt2(code);
        this.markByteCount(request, 0);
        writer.writeZeros(3);
        this.markTransParams(request);
        TraceLog.get().exit(300);
    }

    private void writeNtTrans(Server server, Request request, int maxParamCount, int maxSetupCount, int tailLen) {
        TraceLog.get().enter(300);
        int maxBuffer = server.maxTrans - 100;
        BufferWriter writer = request.writer;
        int endIdx = writer.getOffset();
        int dataCount = endIdx - request.dataIdx + tailLen;
        writer.setOffset(request.tranIdx);
        writer.writeByte((byte)maxSetupCount);
        writer.writeInt2(0);
        writer.writeInt4(request.dataIdx - request.paramsIdx);
        writer.writeInt4(endIdx - request.dataIdx + tailLen);
        writer.writeInt4(maxParamCount);
        writer.writeInt4(maxBuffer);
        writer.writeInt4(request.dataIdx - request.paramsIdx);
        writer.writeInt4(request.paramsIdx - 4);
        writer.writeInt4(dataCount);
        writer.writeInt4(dataCount == 0 ? 0 : request.dataIdx - 4);
        writer.writeByte((byte)0);
        writer.setOffset(endIdx);
        this.writeByteCount(request, tailLen);
        TraceLog.get().exit(300);
    }

    private void parseNtTrans(Response response) throws NqException {
        BufferReader reader = response.reader;
        reader.skip(16);
        response.paramsIdx = reader.readInt4();
        reader.skip(4);
        response.dataCount = reader.readInt4();
        response.dataIdx = reader.readInt4();
    }

    public SecurityDescriptor doQuerySecurityDescriptor(File file) throws NqException {
        TraceLog.get().enter(250);
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        Request request = this.prepareSingleRequest(share, 160);
        this.writeHeader(request);
        BufferWriter writer = request.writer;
        this.markNtTransStart(request, 6);
        this.writeFid(writer, file);
        writer.writeInt2(0);
        writer.writeInt4(7);
        this.markTransData(request);
        this.writeNtTrans(server, request, 4, 0, 0);
        int res = this.sendReceive(server, user, request, null);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Query security descriptor error", res);
        }
        Response response = request.response;
        BufferReader reader = response.reader;
        this.parseNtTrans(response);
        this.setTransParams(response);
        int sdLen = reader.readInt4();
        this.setTransData(response);
        SecurityDescriptor sd = new SecurityDescriptor(reader);
        TraceLog.get().exit(250);
        return sd;
    }

    public void doSetSecurityDescriptor(File file, SecurityDescriptor sd) throws NqException {
        TraceLog.get().enter(250);
        Server server = file.share.getUser().getServer();
        Request request = this.prepareSingleRequest(file.share, 160);
        Response response = request.response;
        this.writeHeader(request);
        this.markNtTransStart(request, 3);
        BufferWriter writer = request.writer;
        this.writeFid(writer, file);
        writer.writeInt2(0);
        writer.writeInt4(4);
        this.markTransData(request);
        sd.write(writer);
        this.writeNtTrans(server, request, 0, 0, 0);
        int res = server.smb.sendReceive(server, file.share.user, request, response);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Set security descriptor error", res);
        }
        TraceLog.get().exit(250);
    }

    private int queryFsInfoByLevel(Share share, int level, Tran2FsInfoParser parser) throws NqException {
        TraceLog.get().enter(300);
        Server server = share.getUser().getServer();
        Request request = this.prepareSingleRequest(share, 50);
        this.writeHeader(request);
        this.markTrans2Start(request, 3);
        BufferWriter writer = request.writer;
        writer.writeInt2(level);
        this.markTransData(request);
        this.writeTrans2(server, request, 0, 0);
        int res = this.sendReceive(server, share.getUser(), request, null);
        if (0 != res) {
            TraceLog.get().exit(300);
            throw new SmbException("Query file system info by level error", res);
        }
        Response response = request.response;
        this.parseTrans(response);
        this.setTransData(response);
        parser.parse(response.reader, share);
        TraceLog.get().exit(300);
        return res;
    }

    public void doQueryFsInfo(Share share) throws NqException {
        TraceLog.get().enter(250);
        int res = this.queryFsInfoByLevel(share, 259, new FsSizeParser());
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Query file system info error", res);
        }
        res = this.queryFsInfoByLevel(share, 258, new FsVolumeParser());
        TraceLog.get().exit(250);
    }

    private void composeQueryFileInfoByNameRequest(Request request, int position, Server server, String fileName, int level) {
        BufferWriter writer = request.writer;
        writer.setOffset(position);
        this.writeHeader(request);
        this.markTrans2Start(request, 5);
        writer.writeInt2(level);
        writer.skip(4);
        if (server.useAscii) {
            writer.writeBytes(fileName.getBytes());
            writer.writeByte((byte)0);
        } else {
            writer.writeString(fileName, true);
        }
        this.markTransData(request);
        this.writeTrans2(server, request, 2, 0);
    }

    private void fileInfoResponseParser(BufferReader reader, File.Info info, int level) throws NqException {
        switch (level) {
            case 257: 
            case 1004: {
                info.setCreationTime(reader.readLong());
                info.setLastAccessTime(reader.readLong());
                info.setLastWriteTime(reader.readLong());
                info.setChangeTime(reader.readLong());
                info.setAttributes(reader.readInt4());
                break;
            }
            case 258: 
            case 1005: {
                info.setAllocationSize(reader.readLong());
                info.setEof(reader.readLong());
                info.setNumberOfLinks(reader.readInt4());
                break;
            }
            case 1006: {
                info.setFileIndex(reader.readLong());
                break;
            }
            default: {
                info.setFileIndex(0L);
            }
        }
    }

    public File.Info doQueryFileInfoByName(Share share, String fileName) throws NqException {
        TraceLog.get().enter(250);
        int[] oldLevels = new int[]{257, 258};
        int[] levelsPassthru = new int[]{1004, 1005, 1006};
        User user = share.getUser();
        Server server = user.getServer();
        Request request = this.prepareSingleRequest(share, 50);
        BufferWriter writer = request.writer;
        int position = writer.getOffset();
        int[] levels = 0 != (server.capabilities & 4) && !server.useAscii ? levelsPassthru : oldLevels;
        File.Info info = new File.Info();
        int badRes = 0;
        boolean isGood = false;
        for (int i = 0; i < levels.length; ++i) {
            this.composeQueryFileInfoByNameRequest(request, position, server, fileName, levels[i]);
            int res = this.sendReceive(server, user, request, null);
            if (0 != res) {
                TraceLog.get().message("Query FS info error fetching level " + levels[i] + " data. res = " + res, 250);
                badRes = res;
                if (-1073741267 == res) continue;
                break;
            }
            isGood = true;
            Response response = request.response;
            this.parseTrans(response);
            this.setTransData(response);
            this.fileInfoResponseParser(response.reader, info, 0);
            this.fileInfoResponseParser(response.reader, info, levels[i]);
        }
        if (!isGood) {
            TraceLog.get().exit(250);
            throw new SmbException("Query file info by name error", badRes);
        }
        TraceLog.get().exit(250);
        return info;
    }

    private void composeQueryFileInfoByHandleRequest(Request request, int position, Server server, File file, int level) {
        BufferWriter writer = request.writer;
        writer.setOffset(position);
        this.writeHeader(request);
        this.markTrans2Start(request, 7);
        this.writeFid(writer, file);
        writer.writeInt2(level);
        this.markTransData(request);
        this.writeTrans2(server, request, 2, 0);
    }

    public File.Info doQueryFileInfoByHandle(File file) throws NqException {
        TraceLog.get().enter(250);
        int[] oldLevels = new int[]{257, 258};
        int[] levelsPassthru = new int[]{1004, 1005, 1006};
        User user = file.share.getUser();
        Server server = user.getServer();
        Request request = this.prepareSingleRequest(file.share, 50);
        BufferWriter writer = request.writer;
        int position = writer.getOffset();
        int[] levels = 0 != (server.capabilities & 4) && !server.useAscii ? levelsPassthru : oldLevels;
        File.Info info = file.info;
        int badRes = 0;
        boolean isGood = false;
        for (int i = 0; i < levels.length; ++i) {
            this.composeQueryFileInfoByHandleRequest(request, position, server, file, levels[i]);
            int res = this.sendReceive(server, user, request, null);
            if (0 != res) {
                TraceLog.get().message("Query FS info error fetching level " + levels[i] + " data. res = " + res, 250);
                badRes = res;
                continue;
            }
            isGood = true;
            Response response = request.response;
            this.parseTrans(response);
            this.setTransData(response);
            this.fileInfoResponseParser(response.reader, info, 0);
            this.fileInfoResponseParser(response.reader, info, levels[i]);
        }
        if (!isGood) {
            TraceLog.get().exit(250);
            throw new SmbException("Query file info by handle error", badRes);
        }
        TraceLog.get().exit(250);
        return info;
    }

    private Request writeSetFileInfo(File file, int level) {
        TraceLog.get().enter(300);
        Request request = this.prepareSingleRequest(file.share, 50);
        this.writeHeader(request);
        this.markTrans2Start(request, 8);
        BufferWriter writer = request.writer;
        this.writeFid(writer, file);
        writer.writeInt2(level);
        writer.writeInt2(0);
        this.markTransData(request);
        TraceLog.get().exit(300);
        return request;
    }

    public void doSetFileAttributes(File file, int attributes) throws NqException {
        TraceLog.get().enter(250);
        long doNotChange = 0L;
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        int level = 0 != (server.capabilities & 4) ? 1004 : 257;
        Request request = this.writeSetFileInfo(file, level);
        BufferWriter writer = request.writer;
        writer.writeLong(0L);
        writer.writeLong(0L);
        writer.writeLong(0L);
        writer.writeLong(0L);
        writer.writeInt4(attributes);
        writer.writeInt4(0);
        this.writeTrans2(server, request, 2, 0);
        int res = this.sendReceive(server, user, request, null);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Set file attribute error", res);
        }
        TraceLog.get().exit(250);
    }

    public void doSetFileSize(File file, long size) throws NqException {
        TraceLog.get().enter(250);
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        int level = 260;
        Request request = this.writeSetFileInfo(file, level);
        BufferWriter writer = request.writer;
        writer.writeLong(size);
        this.writeTrans2(server, request, 2, 0);
        int res = this.sendReceive(server, user, request, null);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Set file size error", res);
        }
        TraceLog.get().exit(250);
    }

    public void doSetFileTime(File file, long creationTime, long lastAccessTime, long lastWriteTime) throws NqException {
        TraceLog.get().enter(250);
        long doNotChange = 0L;
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        int level = 0 != (server.capabilities & 4) ? 1004 : 257;
        Request request = this.writeSetFileInfo(file, level);
        BufferWriter writer = request.writer;
        writer.writeLong(creationTime);
        writer.writeLong(lastAccessTime);
        writer.writeLong(lastWriteTime);
        writer.writeLong(0L);
        writer.writeInt4(0);
        writer.writeInt4(0);
        this.writeTrans2(server, request, 2, 0);
        int res = this.sendReceive(server, user, request, null);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Set file time error", res);
        }
        TraceLog.get().exit(250);
    }

    private Request writeDeleteDirectory(File file) {
        TraceLog.get().enter(300);
        Request request = this.prepareSingleRequest(file.share, 1);
        this.writeHeader(request);
        this.markByteCount(request, 0);
        request.writer.writeByte((byte)4);
        request.writer.writeBytes(file.getLocalPathFromShare().getBytes());
        request.writer.writeByte((byte)0);
        TraceLog.get().exit(300);
        return request;
    }

    public void doSetFileDeleteOnClose(File file) throws NqException {
        Request request;
        TraceLog.get().enter(250);
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        int level = 0;
        if (server.useAscii && 0 != (file.info.getAttributes() & 0x10)) {
            request = this.writeDeleteDirectory(file);
        } else {
            level = 0 != (server.capabilities & 4) ? 1013 : 258;
            request = this.writeSetFileInfo(file, level);
            request.writer.writeByte((byte)1);
            this.writeTrans2(server, request, 2, 0);
        }
        int res = this.sendReceive(server, user, request, null);
        if (0 != res && server.useAscii && level == 1013) {
            request = this.writeSetFileInfo(file, 258);
            request.writer.writeByte((byte)1);
            this.writeTrans2(server, request, 2, 0);
            res = this.sendReceive(server, user, request, null);
        }
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Delete on close error", res);
        }
        TraceLog.get().exit(250);
    }

    public void doRename(File file, String newName, boolean overwriteExistingFile) throws NqException {
        if (overwriteExistingFile) {
            throw new NqException(-22);
        }
        this.doRename(file, newName);
    }

    public void doRename(File file, String newName) throws NqException {
        TraceLog.get().enter(250);
        this.doClose(file);
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        Request request = this.prepareSingleRequest(share, 7);
        int tailLen = 3;
        String oldName = file.getLocalPathFromShare();
        if (file.isDfs() && share.isDfs()) {
            String serverName = file.server.getName();
            oldName = BACKSLASH + serverName + BACKSLASH + share.getName() + BACKSLASH + oldName;
            newName = BACKSLASH + serverName + BACKSLASH + share.getName() + BACKSLASH + newName;
        }
        tailLen = server.useAscii ? (tailLen += oldName.length() + 1 + newName.length() + 1) : (tailLen += oldName.length() * 2 + 2 + newName.length() * 2 + 2);
        this.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.writeInt2(22);
        this.markByteCount(request, 0);
        request.tail = new Buffer(new byte[++tailLen], 0, tailLen);
        writer = new BufferWriter(request.tail.data, 0, false);
        writer.writeByte((byte)4);
        writer.align(-1, 2);
        if (server.useAscii) {
            writer.writeBytes(oldName.getBytes());
            writer.writeByte((byte)0);
        } else {
            writer.writeString(oldName, true);
        }
        writer.writeByte((byte)4);
        writer.align(-1, 2);
        if (server.useAscii) {
            writer.align(0, 2);
            writer.writeBytes(newName.getBytes());
            writer.writeByte((byte)0);
        } else {
            writer.writeString(newName, true);
        }
        this.writeByteCount(request, tailLen);
        int res = this.sendReceive(server, user, request, null);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Rename error", res);
        }
        TraceLog.get().exit(250);
    }

    public void doFlush(File file) throws NqException {
        TraceLog.get().enter(250);
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        Request request = this.prepareSingleRequest(share, 5);
        this.writeHeader(request);
        this.writeFid(request.writer, file);
        this.markByteCount(request, 0);
        int res = this.sendReceive(server, user, request, null);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Flush error", res);
        }
        TraceLog.get().exit(250);
    }

    private void writeTrans(Server server, Request request, int maxParamCount) {
        TraceLog.get().enter(300);
        int maxBuffer = server.maxTrans - 100;
        this.writeByteCount(request, 0L);
        BufferWriter writer = request.writer;
        int endIdx = writer.getOffset();
        writer.setOffset(request.tranIdx);
        writer.writeInt2(request.dataIdx - request.paramsIdx);
        writer.writeInt2(endIdx - request.dataIdx);
        writer.writeInt2(maxParamCount);
        writer.writeInt2(maxBuffer > 65535 ? 65535 : maxBuffer);
        writer.writeByte((byte)0);
        writer.writeByte((byte)0);
        writer.writeInt2(0);
        writer.writeInt4(0);
        writer.writeInt2(0);
        writer.writeInt2(request.dataIdx - request.paramsIdx);
        writer.writeInt2(request.paramsIdx - 4);
        int dataCount = endIdx - request.dataIdx;
        writer.writeInt2(dataCount);
        writer.writeInt2(dataCount == 0 ? 0 : request.dataIdx - 4);
        writer.setOffset(endIdx);
        TraceLog.get().exit(300);
    }

    private void markTransStart(Request request, String pipeName) {
        TraceLog.get().enter(300);
        BufferWriter writer = request.writer;
        request.tranIdx = writer.getOffset();
        writer.skip(26);
        writer.writeByte((byte)0);
        writer.writeByte((byte)0);
        this.markByteCount(request, 0);
        writer.writeBytes(pipeName.getBytes());
        writer.writeByte((byte)0);
        this.markTransParams(request);
        TraceLog.get().exit(300);
    }

    public int doRapTransaction(Share share, Buffer inData, Buffer outParams, Buffer outData) throws NqException {
        TraceLog.get().enter(250);
        User user = share.getUser();
        Server server = user.getServer();
        outParams.data = null;
        outData.data = null;
        Request request = this.prepareSingleRequest(share, 37);
        request.header.flags2 &= 0xFFFF7FFF;
        this.writeHeader(request);
        this.markTransStart(request, "\\PIPE\\LANMAN");
        BufferWriter writer = request.writer;
        writer.writeBytes(inData.data);
        this.markTransData(request);
        this.writeTrans(server, request, inData.data.length);
        int res = this.sendReceive(server, user, request, null);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Rap transaction error", res);
        }
        Response response = request.response;
        this.parseTrans(response);
        this.setTransParams(response);
        BufferReader reader = response.reader;
        outParams.dataLen = response.dataIdx - response.paramsIdx;
        outParams.data = new byte[outParams.dataLen];
        reader.readBytes(outParams.data, outParams.dataLen);
        this.setTransData(response);
        outData.data = new byte[response.dataCount];
        outData.dataLen = response.dataCount;
        reader.setOffset(response.dataIdx);
        reader.readBytes(outData.data, outData.dataLen);
        TraceLog.get().exit(250);
        return res;
    }

    public boolean doEcho(Share share) throws NqException {
        TraceLog.get().enter(250);
        User user = share.getUser();
        Server server = user.getServer();
        Request request = this.prepareSingleRequest(share, 43);
        this.writeHeader(request);
        request.writer.writeInt2(1);
        request.writer.writeInt4(0);
        int res = this.sendReceive(server, user, request, null);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("echo error", res);
        }
        TraceLog.get().exit(250);
        return true;
    }

    public File.ResumeKey doQueryResumeFileKey(File srcFile) throws NqException {
        throw new ClientException("SMB1 does not support query resume key", -110);
    }

    public File.ChunksStatus doServerSideDataCopy(File dstFile, boolean readAccess, File.ResumeKey srcFileKey, File.Chunk[] chunks) throws NqException {
        throw new ClientException("SMB1 does not support server-side data copy", -110);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected int sendRequest(Server server, User user, Object abstractRequest, ClientSmb.Match match) throws NqException {
        Response response;
        TraceLog.get().enter(300);
        Request request = (Request)abstractRequest;
        Transport transport = server.transport;
        if (!transport.isConnected() || !user.isLoggedOn) {
            if (!transport.isConnected()) {
                throw new SmbException("Connection invalid ", -1073741267);
            }
            if (!user.isLoggedOn && request.header.command != 115) {
                TraceLog.get().exit(300);
                throw new SmbException("The user is not logon : " + user.getCredentials().getUser() + "and this is not session start", -1073741267);
            }
        }
        request.response = response = (Response)match.response;
        response.wasReceived = false;
        match.creditCharge = 1;
        if (!server.creditHandler.waitForCredits(1)) {
            throw new SmbException("Time Out.", -1073741267);
        }
        if (null == server.transport || !server.transport.isConnected()) {
            server.updateCredits(match);
            TraceLog.get().error("Transport is null or is not connected");
            throw new SmbException("Transport is null or is not connected", -1073741267);
        }
        Object object = server.smbContextSync;
        synchronized (object) {
            if (null != server.smbContext) {
                Context context = (Context)server.smbContext;
                request.header.mid = context.mid;
                request.header.pid = context.pid;
                match.mid = context.mid;
                match.mid &= 0xFFFFL;
                context.mid += 2;
                if ((context.mid & 0x10000) > 0) {
                    context.mid = 0;
                }
                if (0 == context.mid) {
                    ++context.pid;
                }
                HashMap hashMap = server.expectedResponses;
                synchronized (hashMap) {
                    server.expectedResponses.put((int)match.mid, match);
                }
            } else {
                server.updateCredits(match);
                TraceLog.get().error("Context is null, server = " + server + ", user = " + user + ", request = " + request);
                throw new SmbException("Connection invalid", -1073741267);
            }
        }
        BufferWriter writer = new BufferWriter(request.buffer.data, 30, false);
        writer.writeInt2(request.header.pid);
        writer.skip(2);
        writer.writeInt2(request.header.mid);
        User masterUser = server.masterUser;
        int packetLen = request.writer.getOffset() - 4;
        if (null != masterUser && masterUser.useSigning && !user.isGuest && null != masterUser.macSessionKey) {
            byte[] password = masterUser.getSessionKey().data;
            if (server.useExtendedSecurity) {
                password = null;
            }
            byte[] sigBuffer = null;
            try {
                sigBuffer = Smb100.calculateMessageSignature(masterUser.macSessionKey.data, masterUser.macSessionKey.len, this.msgNumber(request.header), request.buffer.data, packetLen, 4, null != request.tail ? request.tail.data : null, null != request.tail ? request.tail.dataLen : 0, null != request.tail ? request.tail.offset : 0, password, null != password ? password.length : 0);
            }
            catch (Exception e) {
                server.updateCredits(match);
                TraceLog.get().error("Error calling calculateMessageSignature, user = " + user + ", request = " + request);
                TraceLog.get().caught(e, 300);
                throw (SmbException)Utility.throwableInitCauseException(new SmbException("Send failed (signature error): " + e.getClass().getName() + ", " + e.getMessage(), -1073741267), e);
            }
            writer.setOffset(18);
            writer.writeBytes(sigBuffer);
        }
        Server server2 = server;
        synchronized (server2) {
            CaptureInternal capture = transport.getCapture();
            capture.capturePacketWriteStart(true, false, transport.getSocket());
            try {
                int tailLen = request.tail == null ? 0 : request.tail.dataLen;
                transport.send(request.buffer.data, 0, packetLen + tailLen, packetLen);
                if (null != request.tail) {
                    transport.sendTail(request.tail.data, request.tail.offset, tailLen);
                }
            }
            catch (Exception e) {
                TraceLog.get().caught(e, 300);
                throw (SmbException)Utility.throwableInitCauseException(new SmbException("Send request failed: " + e.getMessage(), -1073741267), e);
            }
            capture.capturePacketWriteEnd();
        }
        TraceLog.get().exit(300);
        return 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected int sendReceive(Server server, User user, Object abstractRequest, Object abstractResponse) throws NqException {
        Response response;
        ClientSmb.Match match;
        Request request;
        block32: {
            TraceLog.get().enter(300);
            request = (Request)abstractRequest;
            match = new ClientSmb.Match();
            response = new Response();
            match.response = response;
            match.server = server;
            match.isResponseAllocated = false;
            match.matchExtraInfo = 0;
            match.userId = user.uid;
            int res = this.sendRequest(server, user, request, match);
            if (0 != res) {
                TraceLog.get().exit(300);
                return res;
            }
            boolean isSent = false;
            try {
                isSent = match.syncObj.syncWait(Client.getSmbTimeout());
            }
            catch (InterruptedException e) {
                TraceLog.get().exit(300);
                this.handleTimeout(server, request, response, match);
            }
            if (!isSent) {
                TraceLog.get().exit(300);
                this.handleTimeout(server, request, response, match);
            } else {
                TraceLog.get().message("Received notify message for mid=" + match.mid, 2000);
            }
            if (null != response.header && (response.header.status == -1073741309 || response.header.status == -1073740964)) {
                if (113 != request.header.command && 116 != request.header.command) {
                    try {
                        server.transport.hardDisconnect(false);
                    }
                    catch (IOException e) {
                        TraceLog.get().message("Exception doing a hardDisconnect = ", e, 2000);
                    }
                    TraceLog.get().exit(300);
                    throw (SmbException)Utility.throwableInitCauseException(new SmbException("Session was deleted or expired", -1073741267), new SmbException(response.header.status));
                }
                TraceLog.get().message("status = " + response.header.status, 2000);
                TraceLog.get().exit(300);
                return response.header.status;
            }
            Server e = server;
            synchronized (e) {
                server.useAscii = response.useAscii;
            }
            if (2 != request.header.command) {
                Server.lock(server);
                boolean brokenConnection = server.connectionBroke;
                Server.releaseLock(server);
                if (brokenConnection) {
                    if (request.header.command != 114 && request.header.command != 115) {
                        try {
                            if (!server.reconnect()) {
                                TraceLog.get().exit(300);
                                throw new ClientException("Unable to restore connection", -111);
                            }
                        }
                        catch (Exception e2) {
                            TraceLog.get().caught(e2, 300);
                            throw new ClientException("Unable to reconnect: " + e2.getMessage(), -111);
                        }
                        Server.lock(server);
                        server.connectionBroke = false;
                        Server.releaseLock(server);
                    } else {
                        TraceLog.get().exit(300);
                        throw new SmbException("Connection invalid ", -1073741267);
                    }
                }
            }
            if (!server.transport.isConnected()) {
                this.signalAllMatch(server.transport);
                if (request.header.command != 114 && request.header.command != 115) {
                    try {
                        if (!server.reconnect()) {
                            TraceLog.get().exit(300);
                            throw new ClientException("Unable to restore connection", -111);
                        }
                        break block32;
                    }
                    catch (Exception e3) {
                        TraceLog.get().caught(e3, 300);
                        throw new ClientException("Unable to reconnect: " + e3.getMessage());
                    }
                }
                TraceLog.get().exit(300);
                throw new SmbException("Connection invalid ", -1073741267);
            }
        }
        if (!response.wasReceived) {
            HashMap e3 = server.expectedResponses;
            synchronized (e3) {
                server.expectedResponses.remove(match.mid);
            }
            throw new SmbException("Response was not received", -1073741267);
        }
        User masterUser = server.masterUser;
        if (null != masterUser && masterUser.useSigning && !user.isGuest && -1073741309 != response.header.status && -1073741816 != response.header.status && 115 != response.header.command && 0 != (response.header.flags2 & 4)) {
            byte[] sigBuffer = null;
            if (null != masterUser.macSessionKey.data) {
                int signatureIdx = 14;
                byte[] password = masterUser.getSessionKey().data;
                if (server.useExtendedSecurity) {
                    password = null;
                }
                byte[] tmpBuffData = new byte[response.tailLen];
                System.arraycopy(response.buffer.data, 32, tmpBuffData, 0, tmpBuffData.length);
                sigBuffer = Smb100.calculateMessageSignature(masterUser.macSessionKey.data, masterUser.macSessionKey.len, this.msgNumber(request.header) + 1, match.hdrBuf, 32, 0, tmpBuffData, response.tailLen, 0, password, null != password ? password.length : 0);
            }
            if (!Arrays.equals(response.header.signature, sigBuffer)) {
                TraceLog.get().exit(300);
                throw new SmbException("Signature Mismatch", -1073741819);
            }
        }
        boolean statusNT = 0 != (response.header.flags2 & 0x4000);
        TraceLog.get().exit(300);
        return response.header.status;
    }

    private void handleTimeout(Server server, Request request, Response response, ClientSmb.Match match) throws NqException {
        TraceLog.get().enter(300);
        if (null != match && server.transport.isConnected()) {
            TraceLog.get().message("Calling match.notify for mid=" + match.mid, 2000);
            this.responseMatchNotify(match);
            TraceLog.get().exit(250);
            return;
        }
        this.signalAllMatch(server.transport);
        if (!(server.transport.isConnected() && response.buffer != null || request.header.command == 114 || request.header.command == 115)) {
            try {
                if (!server.reconnect()) {
                    TraceLog.get().exit(300);
                    throw new ClientException("Unable to restore connection", -1073741267);
                }
                server.transport.hardDisconnect();
                TraceLog.get().exit(300);
                throw new SmbException("Connection timed out", -1073741267);
            }
            catch (NetbiosException ex) {
                TraceLog.get().caught(ex, 250);
                throw new ClientException("Unable to reconnect: " + ex.getMessage(), ex.getErrCode());
            }
            catch (IOException ex) {
                TraceLog.get().caught(ex, 300);
                throw new ClientException("Unable to reconnect: " + ex.getMessage(), -111);
            }
        }
        TraceLog.get().exit(300);
        try {
            server.transport.hardDisconnect();
        }
        catch (IOException e1) {
            // empty catch block
        }
        throw new SmbException("Connection timed out", -1073741267);
    }

    protected void anyResponseCallback(Transport transport) {
    }

    protected void keyDerivation(User user) {
    }

    public void handleWaitingNotifyResponses(Server server, File file) throws NqException {
    }

    public boolean doValidateNegotiate(Server server, User user, Share share, short[] dialects) {
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean removeReadWriteMatch(Object context, Server server, boolean isReadMatch) throws NqException {
        HashMap hashMap = server.expectedResponses;
        synchronized (hashMap) {
            int matchType;
            Iterator iterator = server.expectedResponses.entrySet().iterator();
            int n = matchType = isReadMatch ? 2 : 1;
            while (iterator.hasNext()) {
                ClientSmb.Match match = (ClientSmb.Match)iterator.next().getValue();
                if (match.matchExtraInfo != matchType || ((ClientSmb.AsyncMatch)match).context != context) continue;
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public static byte[] calculateMessageSignature(byte[] key, int keyLen, int sequence, byte[] buffer1, int size1, int buffer1Offset, byte[] buffer2, int size2, int buffer2Offset, byte[] password, int passwordLen) throws NqException {
        byte[] hash = new byte[16];
        byte[] signature = new byte[8];
        int sn = sequence;
        Blob[] fragments = new Blob[4];
        for (int i = 0; i < 4; ++i) {
            fragments[i] = new Blob();
        }
        fragments[0].data = key;
        fragments[0].len = keyLen;
        fragments[1].data = password;
        fragments[1].len = passwordLen;
        fragments[2].data = new byte[size1];
        fragments[2].len = size1;
        System.arraycopy(buffer1, buffer1Offset, fragments[2].data, 0, size1);
        fragments[3].data = new byte[size2];
        fragments[3].len = size2;
        if (null != buffer2) {
            System.arraycopy(buffer2, buffer2Offset, fragments[3].data, 0, size2);
        } else {
            fragments[3].data = null;
        }
        BufferWriter writer = new BufferWriter(fragments[2].data, 14, false);
        writer.writeInt4(sn);
        writer.writeInt4(0);
        MD5.md5Internal(null, null, fragments, 4, hash, 8);
        System.arraycopy(hash, 0, signature, 0, 8);
        return signature;
    }

    public com.visuality.nq.client.Request prepareSingleRequestByShare(com.visuality.nq.client.Request request, Share share, short command, int dataLen) throws ClientException {
        throw new ClientException("Calling unimplemented method prepareSingleRequestByShare", -104);
    }

    public com.visuality.nq.client.Request prepareSingleRequest(Server server, User user, com.visuality.nq.client.Request request, short command) throws ClientException {
        throw new ClientException("Calling unimplemented method prepareSingleRequest", -104);
    }

    private class FsLabelParser
    implements Tran2FsInfoParser {
        private FsLabelParser() {
        }

        public void parse(BufferReader reader, Share share) throws NqException {
            int length = reader.readInt4();
            int off = reader.getOffset();
            byte[] src = reader.getSrc();
            try {
                share.info.label = new String(reader.getSrc(), reader.getOffset(), length, "UTF-16LE");
            }
            catch (UnsupportedEncodingException e) {
                throw new ClientException(e.getMessage(), -110);
            }
        }
    }

    private class FsVolumeParser
    implements Tran2FsInfoParser {
        private FsVolumeParser() {
        }

        public void parse(BufferReader reader, Share share) throws NqException {
            reader.skip(8);
            share.info.serialNumber = reader.readInt4();
        }
    }

    private class FsSizeParser
    implements Tran2FsInfoParser {
        private FsSizeParser() {
        }

        public void parse(BufferReader reader, Share share) throws NqException {
            Share.Info info = share.info;
            info.totalClusters = reader.readLong();
            info.freeClusters = reader.readLong();
            info.sectorsPerCluster = reader.readInt4();
            info.bytesPerSector = reader.readInt4();
        }
    }

    private static interface Tran2FsInfoParser {
        public void parse(BufferReader var1, Share var2) throws NqException;
    }

    private static class ReadConsumer
    implements AsyncConsumer {
        private ReadConsumer() {
        }

        public void complete(Throwable status, long length, Object context) throws NqException {
            TraceLog.get().enter(300);
            int count = 0;
            ClientSmb.AsyncMatch match = (ClientSmb.AsyncMatch)context;
            Server server = match.server;
            Response response = (Response)match.response;
            if (-1073741309 == response.header.status || -1073740964 == response.header.status || -1073741816 == response.header.status) {
                try {
                    server.transport.hardDisconnect(false);
                }
                catch (IOException e) {
                    TraceLog.get().message("Exception doing a hardDisconnect = ", e, 2000);
                }
                SmbException se = (SmbException)Utility.throwableInitCauseException(new SmbException(-1073741267), new SmbException(response.header.status));
                TraceLog.get().error("Send STATUS_RETRY with the cause: ", se.getCause(), 10, response.header.status);
                match.consumer.complete(se, 0L, match.context);
                return;
            }
            Buffer structBuffer = new Buffer(new byte[27], 0, 27);
            if (response.tailLen >= 27) {
                try {
                    server.transport.receiveBytes(structBuffer, 0, structBuffer.dataLen);
                }
                catch (NetbiosException e) {
                    Smb100.handleNetbiosException(status, match, "Failed to receive Read response");
                }
                BufferReader reader = new BufferReader(structBuffer.data, structBuffer.offset, false);
                if (0 == response.header.status) {
                    reader.skip(11);
                    count = 0xFFFF & reader.readInt2();
                    int dataOffset = reader.readInt2();
                    count += 65536 * reader.readInt4();
                    if (dataOffset > 0) {
                        dataOffset -= 59;
                        try {
                            server.transport.receiveBytes(structBuffer, 0, dataOffset);
                        }
                        catch (NetbiosException e) {
                            Smb100.handleNetbiosException(status, match, "Failed to receive Read response");
                        }
                    }
                    Buffer dataBuffer = match.buffer;
                    try {
                        server.transport.receiveBytes(dataBuffer, dataBuffer.offset, count);
                    }
                    catch (NetbiosException e) {
                        Smb100.handleNetbiosException(status, match, "Failed to receive Read payload");
                        TraceLog.get().exit(300);
                        return;
                    }
                }
            } else {
                try {
                    server.transport.receiveBytes(structBuffer, 0, response.tailLen);
                }
                catch (NetbiosException e) {
                    Smb100.handleNetbiosException(status, match, "Failed to receive Read response");
                    TraceLog.get().exit(300);
                    return;
                }
                length = 0L;
            }
            try {
                server.transport.receiveEnd(null);
            }
            catch (NetbiosException e) {
                Smb100.handleNetbiosException(status, match, "Failed to receive Read response");
                TraceLog.get().exit(300);
                return;
            }
            long currentTime = System.currentTimeMillis();
            if (-1073741267 == response.header.status) {
                match.consumer.complete(new SmbException(response.header.status), 0L, match.context);
            } else if (response.header.status != 0) {
                match.consumer.complete(new SmbException(response.header.status), 0L, match.context);
            } else if (match.timeCreated + match.timeout > currentTime) {
                if (0 == count) {
                    match.consumer.complete(new SmbException(-1073741807), count, context);
                } else {
                    match.consumer.complete(status, count, context);
                }
            } else if (server.transport.isConnected()) {
                match.consumer.complete(new SmbException(-1073741267), 0L, match.context);
            } else {
                match.consumer.complete(new NqException("Read timed out", -19), length, context);
            }
            server.transport.getCapture().capturePacketWriteEnd();
            TraceLog.get().exit(250);
        }
    }

    private static class WriteConsumer
    implements AsyncConsumer {
        private WriteConsumer() {
        }

        public void complete(Throwable status, long length, Object context) throws NqException {
            TraceLog.get().enter(300);
            ClientSmb.AsyncMatch match = (ClientSmb.AsyncMatch)context;
            Server server = match.server;
            Buffer buffer = Buffer.getNewBuffer((int)length);
            Response response = (Response)match.response;
            if (-1073741309 == response.header.status || -1073740964 == response.header.status || -1073741816 == response.header.status) {
                try {
                    server.transport.hardDisconnect(false);
                }
                catch (IOException e) {
                    TraceLog.get().message("Exception doing a hardDisconnect = ", e, 2000);
                }
                SmbException se = (SmbException)Utility.throwableInitCauseException(new SmbException(-1073741267), new SmbException(response.header.status));
                TraceLog.get().error("Send STATUS_RETRY with the cause: ", se.getCause(), 10, response.header.status);
                match.consumer.complete(se, 0L, match.context);
                return;
            }
            try {
                if (length != (long)server.transport.receiveBytes(buffer, 0, (int)length)) {
                    match.consumer.complete(new ClientException("Failed to receive Write response", -106), 0L, match.context);
                    TraceLog.get().exit(300);
                    return;
                }
            }
            catch (NetbiosException e) {
                Smb100.handleNetbiosException(status, match, "Failed to receive Write response");
                TraceLog.get().exit(300);
                return;
            }
            BufferReader reader = new BufferReader(buffer.data, 0, false);
            if (0 == response.header.status) {
                reader.skip(5);
                int countLow = 0xFFFF & reader.readInt2();
                reader.skip(2);
                short countHigh = reader.readInt2();
                length = countHigh * 65536 + countLow;
            }
            long currentTime = System.currentTimeMillis();
            if (-1073741267 == response.header.status) {
                match.consumer.complete(new SmbException(response.header.status), 0L, match.context);
            } else if (response.header.status != 0) {
                match.consumer.complete(new SmbException(response.header.status), 0L, match.context);
            } else if (match.timeCreated + match.timeout > currentTime) {
                match.consumer.complete(status, length, context);
            } else if (server.transport.isConnected()) {
                match.consumer.complete(new SmbException(-1073741267), 0L, match.context);
            } else {
                match.consumer.complete(new NqException("Write timed out", -19), length, context);
            }
            server.transport.getCapture().capturePacketWriteEnd();
            TraceLog.get().exit(300);
        }
    }

    private class SearchContext {
        public boolean findFirst;
        public int sid;
        public boolean sidAvailable;
        public int resumeKey;
        public boolean eos;

        private SearchContext() {
        }
    }

    private class Context {
        int mid = 0;
        int pid = 0;

        private Context() {
        }
    }

    private class Response {
        public Smb1Header header;
        public int byteCountIdx;
        public int wordCountIdx;
        public int paramsIdx;
        public int dataIdx;
        public int dataCount;
        public BufferReader reader;
        public Buffer buffer;
        public boolean useAscii;
        public int tailLen = 0;
        public boolean wasReceived;

        private Response() {
        }
    }

    private class Request {
        public Smb1Header header;
        public int byteCountIdx;
        public int wordCountIdx;
        public int tranIdx;
        public int paramsIdx;
        public int dataIdx;
        public BufferWriter writer;
        public Buffer buffer;
        public int headerStart;
        public Response response;
        public Buffer tail;

        private Request() {
        }

        public String toString() {
            return "Request [header=" + this.header + ", byteCountIdx=" + this.byteCountIdx + ", wordCountIdx=" + this.wordCountIdx + ", tranIdx=" + this.tranIdx + ", paramsIdx=" + this.paramsIdx + ", dataIdx=" + this.dataIdx + ", headerStart=" + this.headerStart + "]";
        }
    }

    private static class Command {
        int requestBufferSize;
        int requestWordCount;
        int responseWordCount;
        AsyncConsumer consumer;

        Command(int requestBufferSize, int requestWordCount, int responseWordCount, AsyncConsumer consumer) {
            this.requestBufferSize = requestBufferSize;
            this.requestWordCount = requestWordCount;
            this.responseWordCount = responseWordCount;
            this.consumer = consumer;
        }
    }
}

