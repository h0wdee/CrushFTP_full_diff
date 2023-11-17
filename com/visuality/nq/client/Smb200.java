/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.auth.Sha256;
import com.visuality.nq.auth.Sha512;
import com.visuality.nq.client.AsyncConsumer;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.ClientException;
import com.visuality.nq.client.ClientSmb;
import com.visuality.nq.client.ContextDescriptor;
import com.visuality.nq.client.Directory;
import com.visuality.nq.client.File;
import com.visuality.nq.client.FileNotifyInformation;
import com.visuality.nq.client.Request;
import com.visuality.nq.client.Response;
import com.visuality.nq.client.Server;
import com.visuality.nq.client.Share;
import com.visuality.nq.client.Smb300;
import com.visuality.nq.client.Smb311;
import com.visuality.nq.client.Transport;
import com.visuality.nq.client.User;
import com.visuality.nq.common.Blob;
import com.visuality.nq.common.Buffer;
import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.CaptureInternal;
import com.visuality.nq.common.HexBuilder;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SecurityDescriptor;
import com.visuality.nq.common.Smb2Header;
import com.visuality.nq.common.Smb2Params;
import com.visuality.nq.common.SmbException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.UUID;
import com.visuality.nq.common.Utility;
import com.visuality.nq.resolve.NetbiosException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class Smb200
extends ClientSmb {
    private static int BUFSIZE = 4096;
    private static int DIR_FIND_BUFSIZE = 65536;
    private static AsyncConsumer readCallback = new ReadConsumer();
    private static AsyncConsumer writeCallback = new WriteConsumer();
    private static AsyncConsumer changeNotifyCallback = new ChangeNotifyConsumer();
    private static NotifyConsumer breakNotificationHandler = new BreakNotification();
    private static Command[] commandDescriptors = new Command[]{new Command(128, 36, 65, null, null), new Command(26, 25, 9, null, null), new Command(6, 4, 4, null, null), new Command(10, 9, 16, null, null), new Command(6, 4, 4, null, null), new Command(4096, 57, 89, null, null), new Command(26, 24, 60, null, null), new Command(26, 24, 4, null, null), new Command(64, 49, 17, readCallback, null), new Command(64, 49, 17, writeCallback, null), new Command(0, 0, 0, null, null), new Command(100, 57, 49, null, null), new Command(0, 4, 0, changeNotifyCallback, null), new Command(4, 4, 4, null, null), new Command(40, 33, 9, null, null), new Command(24, 32, 9, changeNotifyCallback, null), new Command(44, 41, 9, null, null), new Command(80, 33, 2, null, null), new Command(100, 24, 0, null, breakNotificationHandler)};
    private static SecureRandom randObj = new SecureRandom();

    public Request prepareSingleRequest(Server server, User user, Request request, short command) {
        TraceLog.get().enter(300);
        request.buffer = Buffer.getNewBuffer(Smb200.commandDescriptors[command].requestBufferSize + 64 + 4);
        BufferWriter writer = new BufferWriter(request.buffer.data, 0, false);
        Smb2Header header = new Smb2Header();
        header.protocolId = Smb2Params.smb2ProtocolId;
        header.size = (short)64;
        header.creditCharge = 1;
        header.status = 0;
        header.command = command;
        header.credits = 1;
        header.pid = Utility.getPid();
        request.writer = writer;
        request.command = command;
        request.tail = new Buffer();
        request.encrypt = null == user ? false : user.isEncrypted && !user.isAnonymous;
        writer.skip(4);
        if (null != server && 0 != (server.capabilities & 8)) {
            header.creditCharge = 1;
        }
        header.sid = null == user ? 0L : user.uid;
        header.flags = command != 1 && null != server && server.mustUseSignatures() && null != user && user.useSignatures() ? 8 : 0;
        request.header = header;
        TraceLog.get().exit(300);
        return request;
    }

    public Request prepareSingleRequestByShare(Request request, Share share, short command, int dataLen) throws NqException {
        TraceLog.get().enter("dataLen = " + dataLen, 300);
        User user = share.getUser();
        Server server = user.getServer();
        server.smb.prepareSingleRequest(server, user, request, command);
        boolean bl = request.encrypt = user.isEncrypted ? true : share.encrypt;
        if (0 != (server.capabilities & 8)) {
            switch (command) {
                case 8: 
                case 9: 
                case 14: {
                    request.header.creditCharge = (short)(dataLen > 0 ? 1 + (dataLen - 1) / 65536 : 1);
                    break;
                }
                default: {
                    request.header.creditCharge = 1;
                }
            }
            if (request.header.creditCharge > 1) {
                request.header.credits = request.header.creditCharge;
            }
        }
        request.header.tid = share.tid;
        if (0 == (share.flags & 1)) {
            request.header.flags &= 0xEFFFFFFF;
        }
        TraceLog.get().exit("request.header.credits = " + request.header.credits, 300);
        return request;
    }

    private int writeQueryInfoRequest(Request request, File file, byte infoType, byte infoClass, int maxResLen, int addInfo) throws NqException {
        TraceLog.get().enter(300);
        int SIZE_64KB = 65536;
        Server server = file.share.getUser().getServer();
        server.smb.prepareSingleRequestByShare(request, file.share, (short)16, 0);
        Smb200.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.writeByte(infoType);
        writer.writeByte(infoClass);
        writer.writeInt4(65536);
        writer.writeInt2(0);
        writer.writeInt2(0);
        writer.writeInt4(0);
        writer.writeInt4(addInfo);
        writer.writeInt4(0);
        writer.writeBytes(file.fid, 16);
        TraceLog.get().exit(300);
        return 0;
    }

    private int writeSetInfoRequest(Request request, File file, byte infoType, byte infoClass, int addInfo, int dataLen) throws NqException {
        TraceLog.get().enter(300);
        Server server = file.share.getUser().getServer();
        server.smb.prepareSingleRequestByShare(request, file.share, (short)17, 0);
        Smb200.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.writeByte(infoType);
        writer.writeByte(infoClass);
        writer.writeInt4(dataLen);
        int pBufferOffset = writer.getOffset();
        writer.writeInt2(0);
        writer.writeInt2(0);
        writer.writeInt4(addInfo);
        writer.writeBytes(file.fid, 16);
        int bufferOffset = writer.getOffset() - 4;
        int pTemp = writer.getOffset();
        writer.setOffset(pBufferOffset);
        writer.writeInt2(bufferOffset);
        writer.setOffset(pTemp);
        TraceLog.get().exit(300);
        return 0;
    }

    public Smb200() {
        this.restrictCrypters = true;
        this.setName("SMB 2.002");
        this.setRevision((short)514);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void response(Transport transport) throws NqException {
        ClientSmb.Match match;
        TraceLog.get().enter(300);
        boolean isCreditsPosted = false;
        Server server = transport.getServer();
        if (!transport.isConnected()) {
            TraceLog.get().error("Transport is not connected in response", 300);
            this.responseNotConnected(server);
            TraceLog.get().exit(300);
            return;
        }
        CaptureInternal capture = transport.getCapture();
        capture.capturePacketWriteStart(true, true, transport.getSocket());
        Buffer buffer = new Buffer(66);
        transport.receiveBytes(buffer, 0, buffer.data.length);
        byte[] tmpBuff = new byte[Smb2Params.smb2ProtocolId.length];
        System.arraycopy(buffer.data, 0, tmpBuff, 0, tmpBuff.length);
        if (!Arrays.equals(Smb2Params.smb2ProtocolId, tmpBuff)) {
            transport.receiveEnd(buffer);
            capture.capturePacketWriteEnd();
            TraceLog.get().exit(300);
            throw new ClientException("Wrong protocol Id");
        }
        BufferReader reader = new BufferReader(buffer.data, 0, false);
        Smb2Header header = this.readHeader(reader);
        TraceLog.get().message(header.toString(), 2000);
        HashMap hashMap = server.expectedResponses;
        synchronized (hashMap) {
            match = (ClientSmb.Match)server.expectedResponses.get(header.mid);
        }
        if (null != match) {
            ((Response)match.response).header = header;
            match.status = header.status;
            if (0 != (header.flags & 2) && 259 == header.status) {
                TraceLog.get().message("Pending response received; mid=" + header.mid, 2000);
                this.responsePending(transport, buffer, header, match);
                server.transport.discardReceive();
            } else {
                hashMap = server.expectedResponses;
                synchronized (hashMap) {
                    server.expectedResponses.remove(match.mid);
                }
                Response response = (Response)match.response;
                response.tailLen = server.transport.getReceivingRemain();
                System.arraycopy(buffer.data, 0, match.hdrBuf, 0, 66);
                if (null != Smb200.commandDescriptors[header.command].consumer) {
                    this.responseConsumer(server, buffer, header, match, response);
                } else {
                    if (response.tailLen > 0) {
                        Buffer responsBuffer = new Buffer(response.tailLen);
                        if (null != responsBuffer) {
                            if (response.tailLen == transport.receiveBytes(responsBuffer, 0, response.tailLen)) {
                                response.buffer = responsBuffer.data;
                            }
                            reader = new BufferReader(responsBuffer.data, 0, false);
                        }
                    } else {
                        response.tailLen = 0;
                    }
                    server.transport.receiveEnd(buffer);
                    response.wasReceived = true;
                    response.reader = reader;
                    isCreditsPosted = server.updateCredits(header, match);
                    TraceLog.get().message("Notify initiator of transaction", 300);
                    this.responseMatchNotify(match);
                }
            }
            if (header.credits > 0 && !isCreditsPosted) {
                isCreditsPosted = server.updateCredits(header, match);
            }
            capture.capturePacketWriteEnd();
            TraceLog.get().exit(300);
            return;
        }
        TraceLog.get().message("No match found in response; header = " + header, 2000);
        this.responseNoMatch(server, buffer, header);
        capture.capturePacketWriteEnd();
        TraceLog.get().exit(300);
    }

    private void responseConsumer(Server server, Buffer buffer, Smb2Header header, ClientSmb.Match match, Response response) throws NqException, NetbiosException {
        TraceLog.get().enter(300);
        if (match.status == -1073741309 || match.status == -1073740964) {
            TraceLog.get().message("Session was deleted; mid=" + match.mid, 2000);
            this.responseUserSessionDeletedOrExpired(server, match);
        }
        response.wasReceived = true;
        Smb200.commandDescriptors[header.command].consumer.complete(new SmbException(match.status), response.tailLen, match);
        server.transport.receiveEnd(buffer);
        TraceLog.get().message("Notify initiator of transaction", 300);
        this.responseMatchNotify(match);
        TraceLog.get().exit(300);
    }

    protected void responseNoMatch(Server server, Buffer buffer, Smb2Header header) throws NqException, NetbiosException {
        TraceLog.get().enter(300);
        if (header.command < commandDescriptors.length && null != Smb200.commandDescriptors[header.command].notifyConsumer) {
            this.handleNotification(server, header);
        } else {
            server.updateCredits(header);
        }
        server.transport.receiveEnd(buffer);
        TraceLog.get().exit(300);
    }

    protected void responsePending(Transport transport, Buffer buffer, Smb2Header header, ClientSmb.Match match) throws NetbiosException {
        TraceLog.get().enter(300);
        transport.receiveEnd(buffer);
        if (9 == header.command || 8 == header.command) {
            ClientSmb.AsyncMatch wMatch = (ClientSmb.AsyncMatch)match;
            wMatch.timeout += wMatch.timeout * 2L;
        }
        if (15 == header.command) {
            TraceLog.get().message("Notify initiator of transaction", 300);
            this.responseMatchNotify(match);
        }
        TraceLog.get().exit(300);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void freeContext(Object smbContext, Server server) {
        Object object = server.smbContextSync;
        synchronized (object) {
            server.smbContext = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void signalAllMatch(Transport transport) {
        TraceLog.get().enter(250);
        Server server = transport.getServer();
        if (null == server) {
            return;
        }
        HashMap hashMap = server.expectedResponses;
        synchronized (hashMap) {
            Iterator iterator = server.expectedResponses.entrySet().iterator();
            while (iterator.hasNext()) {
                Object object;
                ClientSmb.Match match;
                Object next = iterator.next().getValue();
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
                    TraceLog.get().message("Notify initiator of transaction", 300);
                    this.responseMatchNotify(match);
                    match.matchExtraInfo |= 4;
                }
                if (!match.isResponseAllocated) continue;
                match.response = null;
            }
            server.expectedResponses.clear();
        }
        TraceLog.get().exit(250);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Object allocateContext(Server server) {
        TraceLog.get().enter(300);
        Context smbContext = new Context();
        ++smbContext.mid;
        Object object = server.smbContextSync;
        synchronized (object) {
            server.smbContext = smbContext;
        }
        TraceLog.get().exit(300);
        return server.smbContext;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int doNegotiate(Server server, Blob blob, short[] dialects) throws NqException {
        TraceLog.get().enter(300);
        if (!Client.getDialects().supportSmb2()) {
            TraceLog.get().exit(300);
            throw new ClientException("Only SMB1 is supported.", -110);
        }
        Request request = new Request();
        this.prepareSingleRequest(server, null, request, (short)0);
        short securityMode = (short)(Client.isSigningEnabled() ? 1 : 0);
        int capabilities = 0;
        capabilities |= 0x44;
        long[] clientGuid = server.clientGuid;
        long clientStartTime = 0L;
        int dialectCount = dialects.length;
        int contextOffset = 0;
        int contextCount = 2;
        Object object = server.smbContextSync;
        synchronized (object) {
            request.header.mid = null != server.smbContext ? ((Context)server.smbContext).mid++ : 0L;
        }
        Smb200.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.writeInt2(dialectCount);
        writer.writeInt2(securityMode);
        writer.writeInt2(0);
        writer.writeInt4(capabilities);
        writer.writeLong(clientGuid[0]);
        writer.writeLong(clientGuid[1]);
        if (Client.getDialects().hasSmb((short)785)) {
            contextOffset = writer.getOffset() - 4;
            writer.writeInt4(contextOffset += (contextOffset += dialectCount * 2 + 4 + 2 + 2) % 8 != 0 ? 8 - contextOffset % 8 : 0);
            writer.writeInt2(contextCount);
            writer.writeInt2(0);
        } else {
            writer.writeLong(clientStartTime);
        }
        for (int i = 0; i < dialectCount; ++i) {
            writer.writeInt2(dialects[i]);
        }
        int packetLen = writer.getOffset() - 4;
        if (Client.getDialects().hasSmb((short)785)) {
            writer.align(4, 8);
            writer.setOffset(contextOffset + 4);
            writer.writeInt2(1);
            writer.writeInt2(38);
            writer.writeInt4(0);
            writer.writeInt2(1);
            writer.writeInt2(32);
            writer.writeInt2(1);
            byte[] random = new byte[32];
            randObj.nextBytes(random);
            writer.writeBytes(random, 32);
            writer.align(4, 8);
            writer.writeInt2(2);
            writer.writeInt2(6);
            writer.writeInt4(0);
            writer.writeInt2(2);
            writer.writeInt2(2);
            writer.writeInt2(1);
            packetLen = writer.getOffset() - 4;
            Arrays.fill(server.preauthIntegHashVal, (byte)0);
            Smb200.calcMessagesHash(request.buffer.data, packetLen, 4, server.preauthIntegHashVal, null);
        }
        CaptureInternal capture = server.transport.getCapture();
        Server server2 = server;
        synchronized (server2) {
            try {
                capture.capturePacketWriteStart(true, false, server.transport.getSocket());
                server.transport.send(request.buffer.data, 0, packetLen, packetLen);
                capture.capturePacketWriteEnd();
            }
            catch (Exception e) {
                TraceLog.get().caught(e, 300);
                throw (SmbException)Utility.throwableInitCauseException(new SmbException("Send failed : " + e.getMessage(), -1073741267), e);
            }
        }
        try {
            capture.capturePacketWriteStart(true, true, server.transport.getSocket());
            Buffer receiveBuffer = null;
            receiveBuffer = server.transport.receiveAll();
            capture.capturePacketWriteEnd();
            this.doNegotiateResponse(server, receiveBuffer, blob, dialects);
            TraceLog.get().exit(300);
            return 0;
        }
        catch (NetbiosException e) {
            TraceLog.get().caught(e, 300);
            throw new NetbiosException("Request failed: " + e.getMessage(), -502);
        }
        catch (ClientException e) {
            TraceLog.get().caught(e, 300);
            throw new SmbException("Negotiation failed", -1073741823);
        }
    }

    public int doNegotiateResponse(Server server, Buffer buffer, Blob inBlob, short[] dialects) throws NqException {
        TraceLog.get().enter(250);
        int res = -1073741823;
        BufferReader reader = new BufferReader(buffer.data, 0, false);
        Smb2Header header = this.readHeader(reader);
        res = header.status;
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Wrong response status", res);
        }
        short structureSize = reader.readInt2();
        if (structureSize != Smb2Params.getSmb2StructureSize((short)0, Smb2Params.smb2Res)) {
            TraceLog.get().exit(250);
            throw new ClientException("Wrong structure size", -103);
        }
        server.negoSmb = this;
        server.capabilities = 0;
        server.serverSecurityMode = reader.readInt2();
        if (0 != (server.serverSecurityMode & 1)) {
            server.capabilities |= 1;
        }
        if (0 != (server.serverSecurityMode & 2) && !Client.isSigningEnabled()) {
            TraceLog.get().exit(250);
            throw new ClientException("Server requires a signature", -103);
        }
        if (null == server.smbContext) {
            server.smbContext = this.allocateContext(server);
            if (null == server.smbContext) {
                TraceLog.get().exit(250);
                throw new ClientException("Internal error", -104);
            }
        }
        server.serverDialectRevision = reader.readInt2();
        switch (server.serverDialectRevision) {
            case 767: {
                res = server.negoSmb.doNegotiate(server, inBlob, dialects);
                TraceLog.get().exit(250);
                return res;
            }
            case 768: {
                server.smb = new Smb300();
                server.smb.setName("SMB 3.0.0");
                break;
            }
            case 770: {
                server.smb = new Smb300();
                server.smb.setRevision((short)770);
                server.smb.setName("SMB 3.0.2");
                break;
            }
            case 785: {
                server.isPreauthIntegOn = true;
                server.smb = new Smb311();
                server.smb.setName("SMB 3.1.1");
                break;
            }
            case 528: {
                server.negoSmb.setName("SMB 2.100");
                server.negoSmb.setRevision((short)528);
            }
            default: {
                server.smb = server.negoSmb;
            }
        }
        server.negoSmb = server.smb;
        short contextCount = 0;
        if (server.isPreauthIntegOn) {
            Smb200.calcMessagesHash(reader.getSrc(), buffer.dataLen, 0, server.preauthIntegHashVal, null);
            contextCount = reader.readInt2();
            if (contextCount < 1) {
                TraceLog.get().exit(250);
                throw new ClientException("Wrong context num", -103);
            }
        } else {
            reader.skip(2);
        }
        reader.readBytes(server.serverGUID, server.serverGUID.length);
        server.serverCapabilites = reader.readInt4();
        if (0 != (1 & server.serverCapabilites)) {
            server.capabilities |= 2;
        }
        if (server.smb.getRevision() != -1 && server.smb.getRevision() >= 528 && 0 != (4 & server.serverCapabilites)) {
            server.capabilities |= 8;
        }
        server.maxTrans = reader.readInt4();
        server.maxRead = reader.readInt4();
        server.maxWrite = reader.readInt4();
        if (0 != (server.capabilities & 8)) {
            server.maxRead = server.maxRead > 0x100000 ? 0x100000 : server.maxRead;
            server.maxWrite = server.maxWrite > 0x100000 ? 0x100000 : server.maxWrite;
            server.maxTrans = server.maxTrans > 0x100000 ? 0x100000 : server.maxTrans;
        } else {
            server.maxRead = server.maxRead > 65535 ? 65535 : server.maxRead;
            server.maxWrite = server.maxWrite > 65535 ? 65535 : server.maxWrite;
            int n = server.maxTrans = server.maxTrans > 65535 ? 65535 : server.maxTrans;
        }
        if (server.maxRead == 0 || server.maxWrite == 0 || server.maxTrans == 0) {
            TraceLog.get().exit(250);
            throw new ClientException("Negotiation response bad param", -103);
        }
        reader.skip(16);
        short blobOffSet = reader.readInt2();
        short tmpShort = reader.readInt2();
        int tempInt = 0;
        if (server.isPreauthIntegOn) {
            tempInt = reader.readInt4();
        }
        Blob blob = new Blob(new byte[tmpShort]);
        reader.setOffset(blobOffSet);
        reader.readBytes(blob.data, tmpShort);
        inBlob.data = null;
        inBlob.len = 0;
        if (blob.len > 0) {
            inBlob.data = new byte[blob.len];
            System.arraycopy(blob.data, 0, inBlob.data, 0, blob.len);
            inBlob.len = blob.len;
            if (null != blob.data && null == inBlob.data) {
                TraceLog.get().exit(250);
                throw new ClientException("Internal error", -104);
            }
        } else if (blob.len == 0) {
            inBlob.len = 0;
            inBlob.data = null;
        }
        if (server.isPreauthIntegOn) {
            reader.setOffset(tempInt);
            res = this.readNegotiateContexts(reader, server, contextCount);
        }
        server.smb = this;
        TraceLog.get().exit(250);
        return res;
    }

    public int doSessionSetup(User user, Blob outBlob, Blob inBlob) throws NqException {
        throw new NqException("This API does not exist over SMB2 and above", -22);
    }

    public int doSessionSetupExtended(User user, Blob outBlob, Blob inBlob) throws NqException {
        int res;
        TraceLog.get().enter(250);
        Server server = user.getServer();
        Request request = new Request();
        server.smb.prepareSingleRequest(server, null, request, (short)1);
        Response response = new Response();
        request.header.credits = (short)128;
        request.header.sid = user.uid;
        Smb200.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.writeByte((byte)0);
        writer.writeByte((byte)(Client.isSigningEnabled() ? 1 : 0));
        writer.writeInt4(0);
        writer.writeInt4(0);
        int blobOffPosition = writer.getOffset();
        writer.skip(2);
        writer.writeInt2(outBlob.len);
        writer.writeLong(0L);
        int savedPosition = writer.getOffset();
        writer.setOffset(blobOffPosition);
        writer.writeInt2(savedPosition - 4);
        writer.setOffset(savedPosition);
        request.tail = new Buffer(outBlob.data, 0, outBlob.len);
        try {
            res = server.smb.sendReceive(server, user, request, response);
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
        if (1 >= server.creditHandler.getCredits()) {
            TraceLog.get().exit("Session setup error -- received zero credits.", 250);
            throw new ClientException("Session setup error -- received zero credits.", -112);
        }
        user.uid = response.header.sid;
        TraceLog.get().message("user.uid = " + user.uid, 2000);
        short temp = 0;
        BufferReader reader = new BufferReader(response.buffer, 0, false);
        temp = reader.readInt2();
        user.isGuest = 0 != (temp & 1);
        user.isEncrypted = 0 != (temp & 4);
        reader.skip(2);
        Blob blob = new Blob();
        blob.len = reader.readInt2();
        int readerOffset = reader.getOffset();
        blob.data = new byte[response.buffer.length - readerOffset];
        System.arraycopy(response.buffer, readerOffset, blob.data, 0, blob.data.length);
        if (0 != blob.len) {
            inBlob.data = new byte[blob.len];
            System.arraycopy(blob.data, 0, inBlob.data, 0, blob.len);
            inBlob.len = blob.len;
            if (null != blob.data && null == inBlob.data && null == server.smbContext) {
                TraceLog.get().exit(250);
                throw new ClientException("Internal error", -104);
            }
        }
        TraceLog.get().exit(250);
        return res;
    }

    public void doLogOff(User user) throws NqException {
        TraceLog.get().enter(250);
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        server.smb.prepareSingleRequest(server, user, request, (short)2);
        request.header.sid = user.uid;
        Smb200.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.writeInt2(0);
        int res = server.smb.sendReceive(server, user, request, response);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("LogOff error", res);
        }
        TraceLog.get().exit(250);
    }

    public void doTreeConnect(Share share) throws NqException {
        int intTmp;
        TraceLog.get().enter("share = ", share, 250);
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        server.smb.prepareSingleRequest(server, user, request, (short)3);
        Blob remotePathName = new Blob();
        try {
            if (server.useName) {
                remotePathName = new Blob(("\\\\" + server.calledName + "\\" + share.info.name).getBytes("UTF-16LE"));
            } else {
                String transportIp = server.transport.getTransportIp();
                if (null == transportIp) {
                    throw new NqException("Unable to identify transport IP address", -14);
                }
                remotePathName = new Blob(("\\\\" + transportIp + "\\" + share.info.name).getBytes("UTF-16LE"));
            }
        }
        catch (UnsupportedEncodingException ex) {
            TraceLog.get().caught(ex, 250);
            throw new NqException(ex.getMessage(), -21);
        }
        if (785 == server.smb.getRevision() && !user.isGuest && !user.isAnonymous) {
            request.header.flags |= 8;
        }
        Smb200.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.writeInt2(0);
        short tmp = (short)writer.getOffset();
        writer.writeInt2(tmp);
        writer.writeInt2(remotePathName.len);
        request.tail = new Buffer(remotePathName.data, 0, remotePathName.len);
        int res = server.smb.sendReceive(server, user, request, response);
        if (res != 0) {
            TraceLog.get().message("tree connect failed with " + res, 250);
            TraceLog.get().exit(250);
            throw new SmbException("tree connect failed with " + res, res);
        }
        share.tid = response.header.tid;
        BufferReader reader = response.reader;
        byte shareType = reader.readByte();
        if (3 == shareType) {
            share.info.type = 1;
            share.isPrinter = true;
        } else {
            share.info.type = 0;
        }
        reader.skip(1);
        share.flags = 0;
        share.rawFlags = intTmp = reader.readInt4();
        if (0 != (1 & intTmp)) {
            share.flags |= 1;
        }
        if (0 != (2 & intTmp)) {
            share.flags |= 4;
        }
        if (0 != (0x8000 & intTmp)) {
            share.encrypt = true;
        }
        if (0 != (8 & (intTmp = reader.readInt4()))) {
            share.flags |= 1;
        }
        if (0 != (0x20 & intTmp)) {
            share.capabilities |= 2;
        }
        share.access = reader.readInt4();
        TraceLog.get().exit(250);
    }

    public void doTreeDisconnect(Share share) throws NqException {
        TraceLog.get().enter(250);
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        server.smb.prepareSingleRequestByShare(request, share, (short)4, 0);
        Smb200.writeHeader(request);
        request.writer.writeInt2(0);
        int res = server.smb.sendReceive(server, user, request, response);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Tree disconnect error", res);
        }
        TraceLog.get().exit(250);
    }

    public void doCreate(File file) throws NqException {
        this.create(file, true);
    }

    public void doRestoreHandle(File file) throws NqException {
        boolean doReplay;
        TraceLog.get().enter(250);
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        server.smb.prepareSingleRequestByShare(request, share, (short)5, 0);
        boolean doDurHandleV2 = server.getSmb().getRevision() != -1 && server.getSmb().getRevision() >= 768;
        boolean bl = doReplay = !file.isOpen();
        if (doDurHandleV2 && doReplay) {
            request.header.flags |= 0x20000000;
        }
        Smb200.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.writeByte((byte)0);
        writer.writeByte(file.oplockLevel);
        writer.writeInt4(0);
        writer.writeInt4(0);
        writer.writeInt4(0);
        writer.writeZeros(8);
        if (doDurHandleV2) {
            writer.writeInt4(file.accessMask);
            writer.writeInt4(file.info.getAttributes());
            writer.writeInt4(file.shareAccess);
            writer.writeInt4(file.disposition);
            writer.writeInt4(file.createOptions);
        } else {
            writer.writeZeros(20);
        }
        String name = file.getLocalPathFromShare();
        short nameLength = (short)(name.length() * 2);
        writer.writeInt2(120);
        writer.writeInt2(nameLength);
        int contextOffsetPosition = writer.getOffset();
        writer.writeInt4(0);
        writer.writeInt4(0);
        try {
            writer.writeBytes(name.getBytes("UTF-16LE"), nameLength);
        }
        catch (UnsupportedEncodingException e) {
            TraceLog.get().caught(e, 250);
            throw new ClientException("Unsupported UTF-16");
        }
        writer.align(4, 8);
        int contextOffset = writer.getOffset() - 4;
        ContextDescriptor ctx = null;
        int tmp = writer.getOffset();
        if (!file.share.isIpc) {
            int ctxId = 1;
            if (doDurHandleV2) {
                ctxId = doReplay ? 2 : 3;
                file.durableFlags = 0;
            }
            ctx = ContextDescriptor.ctxDscCreator(ctxId);
            ctx.pack(writer, file);
            if (0 == ctx.pack(writer, file)) {
                TraceLog.get().exit(250);
                throw new ClientException("file does not have restore handle", -110);
            }
        }
        int contextLength = writer.getOffset() - tmp;
        tmp = writer.getOffset();
        writer.setOffset(contextOffsetPosition);
        writer.writeInt4(contextOffset);
        writer.writeInt4(contextLength);
        writer.setOffset(tmp);
        int res = server.smb.sendReceive(server, user, request, response);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Restore handle error", res);
        }
        BufferReader reader = response.reader;
        reader.skip(1);
        reader.skip(1);
        reader.skip(4);
        reader.skip(8);
        reader.skip(8);
        reader.skip(8);
        reader.skip(8);
        reader.skip(8);
        reader.skip(8);
        file.info.setAttributes(reader.readInt4());
        reader.skip(4);
        reader.readBytes(file.fid, 16);
        contextOffset = reader.readInt4();
        contextLength = reader.readInt4();
        if (contextLength > 0 && contextLength <= response.buffer.length - response.reader.getOffset() && ctx != null) {
            ctx.process(reader, file);
        }
        TraceLog.get().exit(250);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void doChangeNotify(File dir, int completionFilter, AsyncConsumer callback) throws NqException {
        boolean isMatchSignaled;
        TraceLog.get().enter(250);
        Share share = dir.share;
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        server.smb.prepareSingleRequestByShare(request, share, (short)15, 0);
        Smb200.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.writeInt2(1);
        writer.writeInt4(BUFSIZE);
        writer.writeBytes(dir.fid);
        writer.writeInt4(completionFilter);
        writer.writeInt4(0);
        ClientSmb.AsyncMatch match = new ClientSmb.AsyncMatch(this);
        match.response = response;
        match.server = server;
        match.isResponseAllocated = true;
        match.matchExtraInfo = 2;
        match.timeCreated = System.currentTimeMillis();
        match.timeout = Client.getSmbTimeout();
        match.consumer = callback;
        match.file = dir;
        match.userId = user.uid;
        byte[] buffer = new byte[BUFSIZE];
        match.buffer = new Buffer(buffer, 0, buffer.length);
        try {
            int resp = server.smb.sendRequest(server, user, request, match);
            if (0 != resp) {
                TraceLog.get().exit(250);
                throw new SmbException("ChangeNotify failed with error " + resp);
            }
        }
        catch (NetbiosException e) {
            HashMap hashMap = server.expectedResponses;
            synchronized (hashMap) {
                server.expectedResponses.remove(match.mid);
            }
            TraceLog.get().caught(e, 250);
            throw new ClientException("changeNotify failed", e.getErrCode());
        }
        try {
            isMatchSignaled = match.syncObj.syncWait(Client.getSmbTimeout());
        }
        catch (InterruptedException e) {
            TraceLog.get().caught(e, 250);
            throw new NqException("syncObj got interrupted during wait() by another thread - this should not happen", -1073741267);
        }
        if (isMatchSignaled) {
            long aid = response.header.aid;
            if (0 != (response.header.flags & 2)) {
                dir.addToAidChangeNotifyQueue(aid, callback);
            }
        } else {
            TraceLog.get().exit(250);
            throw new SmbException("Time out", -1073741643);
        }
        TraceLog.get().exit(250);
    }

    protected void doCancel(File dir) throws NqException {
        TraceLog.get().enter(250);
        Share share = dir.share;
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        server.smb.prepareSingleRequestByShare(request, share, (short)12, 0);
        File.AidObject aidObject = dir.retrieveAidChangeNotifyQueueEntry();
        if (null != aidObject) {
            TraceLog.get().message("aid in doCancel request = " + aidObject.aid, 2000);
            request.header.aid = aidObject.aid;
            dir.removeAidChangeNotifyQueueEntry(aidObject);
        } else {
            TraceLog.get().error("aid in doCancel request = null", 2000, 0);
        }
        request.header.flags |= 2;
        Smb200.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.writeInt2(0);
        server.smb.sendRequest(server, user, request, null);
        TraceLog.get().exit(250);
    }

    public int doClose(File file) throws NqException {
        TraceLog.get().enter(250);
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        server.smb.prepareSingleRequestByShare(request, share, (short)6, 0);
        BufferWriter writer = request.writer;
        Smb200.writeHeader(request);
        writer.writeInt2(0);
        writer.writeInt4(0);
        writer.writeBytes(file.fid);
        int res = server.smb.sendReceive(server, user, request, response);
        if (0 != res) {
            throw new SmbException("Close error", res);
        }
        TraceLog.get().exit(250);
        return res;
    }

    public void doQueryDfsReferrals(Share share, String path, ClientSmb.ParseReferral parser) throws NqException {
        TraceLog.get().enter(250);
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        server.smb.prepareSingleRequestByShare(request, share, (short)11, 0);
        request.header.flags |= 0x10000000;
        Smb200.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.skip(2);
        writer.writeInt4(393620);
        writer.writeInt4(-1);
        writer.writeInt4(-1);
        writer.writeInt4(-1);
        writer.writeInt4(-1);
        int pOffset = writer.getOffset();
        writer.skip(20);
        writer.writeInt4(4096);
        writer.writeInt4(1);
        writer.writeInt4(0);
        int offset = writer.getOffset() - 4;
        writer.writeInt2(4);
        try {
            request.tail = new Buffer(path.getBytes("UTF-16LE"), 0, path.length() * 2);
        }
        catch (UnsupportedEncodingException e) {
            TraceLog.get().caught(e, 250);
            throw new ClientException("Unsupported UTF-16");
        }
        int tmp = writer.getOffset();
        writer.setOffset(pOffset);
        writer.writeInt4(offset);
        writer.writeInt4(request.tail.dataLen + 2);
        writer.writeInt4(0);
        writer.writeInt4(offset);
        writer.writeInt4(0);
        writer.setOffset(tmp);
        int res = server.smb.sendReceive(server, user, request, response);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Query dfs referrals error", res);
        }
        BufferReader reader = response.reader;
        reader.skip(2);
        reader.skip(4);
        reader.skip(16);
        reader.skip(8);
        offset = reader.readInt4();
        reader.skip(4);
        reader.skip(8);
        parser.parse(reader);
        TraceLog.get().exit(250);
    }

    public void doFindOpen(Directory search) throws NqException {
        TraceLog.get().enter(250);
        boolean isFullPath = false;
        SearchContext context = new SearchContext();
        search.context = context;
        context.findFirst = true;
        Share share = search.getShare();
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        server.smb.prepareSingleRequestByShare(request, share, (short)5, 0);
        if ((share.flags & 1) == 0) {
            request.header.flags &= 0xEFFFFFFF;
        } else {
            if ((share.flags & 4) != 0) {
                isFullPath = true;
            }
            request.header.flags |= 0x10000000;
        }
        String dirName = search.path;
        if (isFullPath) {
            String fullname = server.getName() + "\\" + share.getName();
            dirName = dirName.length() > 0 ? fullname + "\\" + dirName : fullname;
        }
        dirName = dirName.equals("") ? "\u0000" : dirName;
        short nameLength = (short)(dirName.length() * 2);
        Smb200.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.writeByte((byte)0);
        writer.writeByte((byte)0);
        writer.writeInt4(2);
        writer.writeInt4(0);
        writer.writeInt4(0);
        writer.writeZeros(8);
        writer.writeInt4(0x100081);
        writer.writeInt4(0);
        writer.writeInt4(7);
        writer.writeInt4(1);
        writer.writeInt4(32);
        int nameOffsetPosition = writer.getOffset();
        writer.skip(2);
        writer.writeInt2(dirName.equals("\u0000") ? (short)0 : nameLength);
        writer.writeInt4(0);
        writer.writeInt4(0);
        writer.align(4, 8);
        int tmp = writer.getOffset();
        int nameOffset = writer.getOffset() - 4;
        writer.setOffset(nameOffsetPosition);
        writer.writeInt2(nameOffset);
        writer.setOffset(tmp);
        if (0 == nameLength) {
            writer.writeInt2(0);
        }
        try {
            request.tail = new Buffer(dirName.getBytes("UTF-16LE"), 0, nameLength);
        }
        catch (UnsupportedEncodingException e) {
            TraceLog.get().caught(e, 250);
            throw new ClientException("Unsupported UTF-16");
        }
        int res = server.smb.sendReceive(server, user, request, response);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Find open error", res);
        }
        BufferReader reader = response.reader;
        reader.skip(1);
        reader.skip(1);
        reader.skip(4);
        reader.skip(8);
        reader.skip(8);
        reader.skip(8);
        reader.skip(8);
        reader.skip(8);
        reader.skip(8);
        reader.skip(4);
        reader.skip(4);
        reader.readBytes(context.fid, 16);
        TraceLog.get().exit(250);
    }

    public boolean doFindMore(Directory search) throws NqException {
        TraceLog.get().enter(250);
        SearchContext context = (SearchContext)search.context;
        Share share = search.getShare();
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        server.smb.prepareSingleRequestByShare(request, share, (short)14, 0);
        String pattern = search.wildcards;
        int nameLength = pattern.length() * 2;
        request.header.flags = (share.flags & 1) == 0 ? (request.header.flags &= 0xEFFFFFFF) : (request.header.flags |= 0x10000000);
        this.composeDoFindMore(context, request, pattern, nameLength, (byte)3);
        int res = server.smb.sendReceive(server, user, request, response);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Find more error", res);
        }
        BufferReader reader = response.reader;
        reader.skip(2);
        reader.skip(4);
        context.findFirst = false;
        search.setParser(response.buffer, reader.getOffset(), 0);
        TraceLog.get().exit(250);
        return true;
    }

    private void composeDoFindMore(SearchContext context, Request request, String pattern, int nameLength, byte fileInfoByte) throws ClientException {
        Smb200.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.writeByte(fileInfoByte);
        writer.writeByte((byte)(context.findFirst ? 1 : 0));
        writer.writeInt4(0);
        writer.writeBytes(context.fid);
        int nameOffsetPosition = writer.getOffset();
        writer.skip(2);
        writer.writeInt2(nameLength);
        writer.writeInt4(DIR_FIND_BUFSIZE);
        int nameOffset = 0;
        nameOffset = 0 == nameLength ? 0 : writer.getOffset() - 4;
        int tmp = writer.getOffset();
        writer.setOffset(nameOffsetPosition);
        writer.writeInt2(nameOffset);
        writer.setOffset(tmp);
        try {
            request.tail = new Buffer(pattern.getBytes("UTF-16LE"), 0, nameLength);
        }
        catch (UnsupportedEncodingException e) {
            TraceLog.get().caught(e, 250);
            throw new ClientException("Unsupported UTF-16");
        }
    }

    public int doFindClose(Directory search) throws NqException {
        TraceLog.get().enter(250);
        SearchContext context = (SearchContext)search.context;
        Share share = search.getShare();
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        server.smb.prepareSingleRequestByShare(request, share, (short)6, 0);
        Smb200.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.writeInt2(0);
        writer.writeInt4(0);
        writer.writeBytes(context.fid);
        int res = server.smb.sendReceive(server, user, request, response);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Find close error", res);
        }
        TraceLog.get().exit(250);
        return res;
    }

    public void doWrite(File file, Buffer buffer, AsyncConsumer callback, Object context, Object hook) throws NqException {
        TraceLog.get().enter(250);
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        server.smb.prepareSingleRequestByShare(request, share, (short)9, buffer.dataLen);
        Smb200.writeHeader(request);
        BufferWriter writer = request.writer;
        int pDataOffset = writer.getOffset();
        writer.writeInt2(0);
        writer.writeInt4(buffer.dataLen);
        writer.writeLong(file.getPosition());
        writer.writeBytes(file.fid, 16);
        writer.writeInt4(0);
        writer.writeInt4(0);
        writer.writeInt2(0);
        writer.writeInt2(0);
        writer.writeInt4(0);
        int dataOffset = writer.getOffset() - 4;
        int tmp = writer.getOffset();
        writer.setOffset(pDataOffset);
        writer.writeInt2(dataOffset);
        writer.setOffset(tmp);
        request.tail = buffer;
        ClientSmb.AsyncMatch match = new ClientSmb.AsyncMatch(this);
        match.response = response;
        match.server = server;
        match.isResponseAllocated = true;
        match.matchExtraInfo = 1;
        match.timeCreated = System.currentTimeMillis();
        match.timeout = Client.getSmbTimeout();
        match.consumer = callback;
        match.context = context;
        match.hook = hook;
        match.userId = user.uid;
        try {
            server.smb.sendRequest(server, user, request, match);
        }
        catch (ClientException e) {
            match.response = null;
            match = null;
            TraceLog.get().caught(e, 250);
            throw new ClientException(e.getMessage(), e.getErrCode());
        }
        TraceLog.get().exit(250);
    }

    public void doRead(File file, Buffer buffer, AsyncConsumer callback, Object context, Object hook) throws NqException {
        TraceLog.get().enter(250);
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        server.smb.prepareSingleRequestByShare(request, share, (short)8, buffer.dataLen);
        Smb200.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.writeByte((byte)80);
        writer.writeByte((byte)0);
        writer.writeInt4(buffer.dataLen);
        writer.writeLong(file.getPosition());
        writer.writeBytes(file.fid, 16);
        writer.writeInt4(0);
        writer.writeInt4(0);
        writer.writeInt4(0);
        writer.writeInt2(0);
        writer.writeInt2(0);
        writer.writeByte((byte)0);
        ClientSmb.AsyncMatch match = new ClientSmb.AsyncMatch(this);
        match.response = response;
        match.server = server;
        match.isResponseAllocated = true;
        match.matchExtraInfo = 2;
        match.timeCreated = System.currentTimeMillis();
        match.timeout = Client.getSmbTimeout();
        match.consumer = callback;
        match.context = context;
        match.buffer = new Buffer(buffer.data, buffer.offset, buffer.dataLen);
        match.userId = user.uid;
        server.smb.sendRequest(server, user, request, match);
        TraceLog.get().exit(250);
    }

    protected static void handleNetbiosException(Throwable status, ClientSmb.AsyncMatch match, String text) throws NqException {
        if (status instanceof SmbException && -1073741267 == ((SmbException)status).getErrCode()) {
            match.consumer.complete(new SmbException("need to retry the operation", -1073741267), 0L, match.context);
        } else {
            match.consumer.complete(new ClientException(text, -106), 0L, match.context);
        }
    }

    public SecurityDescriptor doQuerySecurityDescriptor(File file) throws NqException {
        TraceLog.get().enter(250);
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        this.writeQueryInfoRequest(request, file, (byte)3, (byte)0, 0, 7);
        int res = server.smb.sendReceive(server, user, request, response);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Query SD error", res);
        }
        BufferReader reader = response.reader;
        reader.skip(2);
        reader.readInt4();
        SecurityDescriptor sd = new SecurityDescriptor(reader);
        TraceLog.get().exit(250);
        return sd;
    }

    public void doSetSecurityDescriptor(File file, SecurityDescriptor sd) throws NqException {
        TraceLog.get().enter(250);
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        this.writeSetInfoRequest(request, file, (byte)3, (byte)0, 0x20000004, 0);
        BufferWriter writer = request.writer;
        int off1 = writer.getOffset();
        sd.write(writer);
        int off2 = writer.getOffset();
        writer.setOffset(72);
        writer.writeInt4(off2 - off1);
        writer.setOffset(off2);
        int res = server.smb.sendReceive(server, user, request, response);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Set SD error", res);
        }
        TraceLog.get().exit(250);
    }

    public void doQueryFsInfo(Share share) throws NqException {
        TraceLog.get().enter(250);
        User user = share.getUser();
        Server server = user.getServer();
        File file = new File();
        file.oplockLevel = 0;
        file.accessMask = 0x100081;
        file.info.setAttributes(0);
        file.disposition = 1;
        file.createOptions = 33;
        file.share = share;
        file.shareAccess = 7;
        file.setShareRelativePath("\u0000");
        file.durableState = 1;
        file.durableHandle = new UUID();
        file.durableFlags = 0;
        file.durableTimeout = 0;
        int res = this.create(file, false);
        if (0 != res) {
            this.doClose(file);
            TraceLog.get().exit(250);
            throw new SmbException("Query FS info error", res);
        }
        Request request = new Request();
        Response response = new Response();
        request.tail = new Buffer();
        this.writeQueryInfoRequest(request, file, (byte)2, (byte)3, 4096, 0);
        res = server.smb.sendReceive(server, user, request, response);
        if (0 != res) {
            this.doClose(file);
            TraceLog.get().exit(250);
            throw new SmbException("Query FS info error", res);
        }
        BufferReader reader = response.reader;
        reader.skip(6);
        share.info.totalClusters = reader.readLong();
        share.info.freeClusters = reader.readLong();
        share.info.sectorsPerCluster = reader.readInt4();
        share.info.bytesPerSector = reader.readInt4();
        this.writeQueryInfoRequest(request, file, (byte)2, (byte)1, 4096, 0);
        res = server.smb.sendReceive(server, user, request, response);
        if (0 != res) {
            this.doClose(file);
            TraceLog.get().exit(250);
            throw new SmbException("Query FS info error", res);
        }
        reader = response.reader;
        reader.skip(6);
        reader.skip(8);
        share.info.serialNumber = reader.readInt4();
        this.doClose(file);
        TraceLog.get().exit(250);
    }

    public File.Info doQueryFileInfoByName(Share share, String fileName) throws NqException {
        TraceLog.get().enter(250);
        File file = new File();
        boolean isEmptyName = false;
        if (fileName.length() == 0) {
            isEmptyName = false;
        } else if ('\\' == fileName.charAt(0)) {
            isEmptyName = true;
        }
        file.oplockLevel = 0;
        file.accessMask = 0x100080;
        file.info.setAttributes(0);
        file.disposition = 1;
        file.createOptions = 0;
        file.share = share;
        file.shareAccess = 7;
        if (isEmptyName) {
            file.durableState = 1;
            file.durableHandle = new UUID();
        } else {
            file.durableState = 2;
            file.durableHandle.clear();
        }
        file.durableFlags = 0;
        file.durableTimeout = 0;
        file.setShareRelativePath(fileName);
        this.create(file, true);
        File.Info info = this.doQueryFileInfoByHandle(file);
        this.doClose(file);
        TraceLog.get().exit(250);
        return info;
    }

    public File.Info doQueryFileInfoByHandle(File file) throws NqException {
        TraceLog.get().enter(250);
        byte[] level = new byte[]{4, 5, 6};
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        int badRes = 0;
        boolean isGood = false;
        for (int i = 0; i < level.length; ++i) {
            Request request = new Request();
            Response response = new Response();
            this.writeQueryInfoRequest(request, file, (byte)1, level[i], 4096, 0);
            request.tail = new Buffer();
            int res = server.smb.sendReceive(server, user, request, response);
            if (0 != res) {
                TraceLog.get().message("Query FS info error fetching level " + level[i] + " data. res = " + res, 250);
                badRes = res;
                continue;
            }
            isGood = true;
            BufferReader reader = response.reader;
            reader.skip(6);
            Smb200.fileInfoResponseParser(reader, file.info, level[i]);
            response.buffer = null;
        }
        if (!isGood) {
            TraceLog.get().exit(250);
            throw new SmbException("Query FS info by handle error", badRes);
        }
        TraceLog.get().exit(250);
        return file.info;
    }

    public void doSetFileAttributes(File file, int attributes) throws NqException {
        TraceLog.get().enter(250);
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        this.writeSetInfoRequest(request, file, (byte)1, (byte)4, 0, 40);
        long doNotChange = -1L;
        BufferWriter writer = request.writer;
        writer.writeLong(doNotChange);
        writer.writeLong(doNotChange);
        writer.writeLong(doNotChange);
        writer.writeLong(doNotChange);
        writer.writeInt4(attributes);
        writer.writeInt4(0);
        request.tail = new Buffer();
        int res = server.smb.sendReceive(server, user, request, response);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Set File Attribute error", res);
        }
        TraceLog.get().exit(250);
    }

    public void doSetFileSize(File file, long size) throws NqException {
        TraceLog.get().enter(250);
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        this.writeSetInfoRequest(request, file, (byte)1, (byte)20, 0, 8);
        request.writer.writeLong(size);
        request.tail = new Buffer();
        int res = server.smb.sendReceive(server, user, request, response);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Set file size error", res);
        }
        TraceLog.get().exit(250);
    }

    public void doSetFileTime(File file, long creationTime, long lastAccessTime, long lastWriteTime) throws NqException {
        TraceLog.get().enter(250);
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        this.writeSetInfoRequest(request, file, (byte)1, (byte)4, 0, 40);
        BufferWriter writer = request.writer;
        writer.writeLong(creationTime);
        writer.writeLong(lastAccessTime);
        writer.writeLong(lastWriteTime);
        writer.writeLong(-1L);
        writer.writeInt4(0);
        writer.writeInt4(0);
        request.tail = new Buffer();
        int res = server.smb.sendReceive(server, user, request, response);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Set file time error", res);
        }
        TraceLog.get().exit(250);
    }

    public void doSetFileDeleteOnClose(File file) throws NqException {
        TraceLog.get().enter(250);
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        int res = this.writeSetInfoRequest(request, file, (byte)1, (byte)13, 0, 1);
        request.writer.writeByte((byte)1);
        request.tail = new Buffer();
        res = server.smb.sendReceive(server, user, request, response);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Set file deletion error", res);
        }
        TraceLog.get().exit(250);
    }

    public void doRename(File file, String newName) throws NqException {
        this.doRename(file, newName, false);
    }

    public void doRename(File file, String newName, boolean overwriteExistingFile) throws NqException {
        TraceLog.get().enter(250);
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        int nameLen = newName.length() * 2;
        int dataLen = 2 == nameLen ? nameLen + 2 + 20 : nameLen + 20;
        Request request = new Request();
        Response response = new Response();
        int res = this.writeSetInfoRequest(request, file, (byte)1, (byte)10, 0, dataLen);
        BufferWriter writer = request.writer;
        writer.writeByte((byte)(overwriteExistingFile ? 1 : 0));
        writer.skip(7);
        writer.writeZeros(8);
        writer.writeInt4(nameLen);
        try {
            byte[] newNameBytes = newName.getBytes("UTF-16LE");
            if (2 == nameLen) {
                byte[] tmpName = new byte[4];
                byte[] padding = new byte[]{-1, -1};
                System.arraycopy(newNameBytes, 0, tmpName, 0, nameLen);
                System.arraycopy(padding, 0, tmpName, 2, 2);
                newNameBytes = tmpName;
                nameLen += 2;
            }
            request.tail = new Buffer(newNameBytes, 0, nameLen);
        }
        catch (UnsupportedEncodingException e) {
            TraceLog.get().caught(e, 250);
            throw new ClientException("Unsupported UTF-16");
        }
        res = server.smb.sendReceive(server, user, request, response);
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
        Request request = new Request();
        Response response = new Response();
        server.smb.prepareSingleRequestByShare(request, share, (short)7, 0);
        Smb200.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.writeInt2(0);
        writer.writeInt4(0);
        writer.writeBytes(file.fid, 16);
        int res = server.smb.sendReceive(server, user, request, response);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Flush error", res);
        }
        TraceLog.get().exit(250);
    }

    public int doRapTransaction(Share share, Buffer inData, Buffer outParams, Buffer outData) throws NqException {
        throw new NqException("This API does not exist over SMB2 and above", -22);
    }

    public boolean doEcho(Share share) throws NqException {
        TraceLog.get().enter(250);
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        server.smb.prepareSingleRequestByShare(request, share, (short)13, 0);
        if (!user.isEncrypted) {
            request.encrypt = false;
        }
        Smb200.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.writeInt2(0);
        int res = server.smb.sendReceive(server, user, request, response);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Do echo error", res);
        }
        TraceLog.get().exit(250);
        return true;
    }

    public File.ResumeKey doQueryResumeFileKey(File srcFile) throws NqException {
        TraceLog.get().enter(250);
        Share share = srcFile.share;
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        server.smb.prepareSingleRequestByShare(request, share, (short)11, 0);
        Smb200.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.skip(2);
        writer.writeInt4(1310840);
        writer.writeBytes(srcFile.fid, 16);
        writer.writeZeros(20);
        writer.writeInt4(32);
        writer.writeInt4(1);
        writer.writeInt4(0);
        int res = server.smb.sendReceive(server, user, request, response);
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Query resume file key error", res);
        }
        BufferReader reader = response.reader;
        reader.skip(2);
        reader.skip(4);
        reader.skip(16);
        reader.skip(8);
        reader.skip(8);
        reader.skip(8);
        File.ResumeKey key = new File.ResumeKey();
        reader.readBytes(key.key, 24);
        TraceLog.get().exit(250);
        return key;
    }

    public File.ChunksStatus doServerSideDataCopy(File dstFile, boolean readAccess, File.ResumeKey srcFileKey, File.Chunk[] chunks) throws NqException {
        TraceLog.get().enter(250);
        Share share = dstFile.share;
        User user = share.getUser();
        Server server = user.getServer();
        Request request = new Request();
        Response response = new Response();
        server.smb.prepareSingleRequestByShare(request, share, (short)11, 0);
        Smb200.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.skip(2);
        if (readAccess) {
            writer.writeInt4(1327346);
        } else {
            writer.writeInt4(1343730);
        }
        writer.writeBytes(dstFile.fid, 16);
        int pOffset = writer.getOffset();
        writer.skip(20);
        writer.writeInt4(12);
        writer.writeInt4(1);
        writer.writeInt4(0);
        int offset = writer.getOffset() - 4;
        int bufferSize = chunks.length * 24 + 24 + 4 + 4;
        request.tail = Buffer.getNewBuffer(bufferSize);
        BufferWriter chunksWriter = new BufferWriter(request.tail.data, 0, false);
        chunksWriter.writeBytes(srcFileKey.key, 24);
        chunksWriter.writeInt4(chunks.length);
        chunksWriter.writeInt4(0);
        for (int i = 0; i < chunks.length; ++i) {
            chunksWriter.writeInt8(chunks[i].sourceOffset);
            chunksWriter.writeInt8(chunks[i].targetOffset);
            chunksWriter.writeInt4(chunks[i].length);
            chunksWriter.writeInt4(0);
        }
        int tmp = writer.getOffset();
        writer.setOffset(pOffset);
        writer.writeInt4(offset);
        writer.writeInt4(bufferSize);
        writer.writeInt4(0);
        writer.writeInt4(offset);
        writer.writeInt4(0);
        writer.setOffset(tmp);
        int res = server.smb.sendReceive(server, user, request, response);
        if (0 != res && -1073741811 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Server side data copy error", res);
        }
        BufferReader reader = response.reader;
        reader.skip(2);
        reader.skip(4);
        reader.skip(16);
        reader.skip(8);
        reader.skip(8);
        reader.skip(8);
        File.ChunksStatus chunksStatus = new File.ChunksStatus();
        chunksStatus.chunksWritten = reader.readInt4();
        chunksStatus.chunkBytesWritten = reader.readInt4();
        chunksStatus.totalBytesWritten = reader.readInt4();
        chunksStatus.status = res;
        TraceLog.get().exit(250);
        return chunksStatus;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected int sendRequest(Server server, User user, Object request, ClientSmb.Match match) throws NqException {
        int packetLen;
        Object object;
        TraceLog.get().enter(300);
        Request currRequest = (Request)request;
        short creditCharge = 1;
        if (0 != (server.capabilities & 8)) {
            creditCharge = currRequest.header.creditCharge;
        }
        if (!server.transport.isConnected()) {
            TraceLog.get().exit(300);
            throw new SmbException("Connection invalid", -1073741267);
        }
        if (!user.isLoggedOn && 1 != currRequest.command) {
            TraceLog.get().exit(300);
            throw new SmbException("Connection invalid", -1073741267);
        }
        if (match != null) {
            match.creditCharge = creditCharge;
            if (!server.creditHandler.waitForCredits(creditCharge)) {
                TraceLog.get().exit(300);
                throw new SmbException("Time Out.", -1073741267);
            }
            if (null == server.transport || !server.transport.isConnected()) {
                server.updateCredits(creditCharge);
                TraceLog.get().error("Transport is null or is not connected");
                throw new SmbException("Transport is null or is not connected", -1073741267);
            }
            ((Response)match.response).wasReceived = false;
            object = server.smbContextSync;
            synchronized (object) {
                Context context;
                if (null != server.smbContext) {
                    context = (Context)server.smbContext;
                    currRequest.header.mid = match.mid = context.mid;
                    packetLen = currRequest.writer.getOffset() - 4;
                    BufferWriter writer = new BufferWriter(currRequest.buffer.data, 28, false);
                    writer.writeLong(context.mid);
                    HashMap hashMap = server.expectedResponses;
                    synchronized (hashMap) {
                        server.expectedResponses.put(match.mid, match);
                    }
                } else {
                    server.updateCredits(creditCharge);
                    TraceLog.get().error("Context is null, server = " + server + ", user = " + user + ", request = " + request);
                    throw new SmbException("Connection invalid", -1073741267);
                }
                context.mid = context.mid + (long)(currRequest.header.creditCharge > 0 ? (int)currRequest.header.creditCharge : 1);
            }
        } else {
            packetLen = currRequest.writer.getOffset() - 4;
            BufferWriter writer = new BufferWriter(currRequest.buffer.data, 28, false);
            writer.writeLong(0L);
        }
        if (0 != (currRequest.header.flags & 8)) {
            byte[] signature = null;
            try {
                signature = Smb200.calculateMessageSignature(user.macSessionKey.data, user.macSessionKey.len, currRequest.buffer.data, packetLen, 4, currRequest.tail.data, currRequest.tail.dataLen, currRequest.tail.offset);
            }
            catch (Exception e) {
                server.updateCredits(creditCharge);
                TraceLog.get().error("Error calling calculateMessageSignature, user = " + user + ", request = " + request);
                TraceLog.get().caught(e, 300);
                throw (SmbException)Utility.throwableInitCauseException(new SmbException("Send failed (signature error): " + e.getClass().getName() + ", " + e.getMessage(), -1073741267), e);
            }
            currRequest.writer.setOffset(52);
            currRequest.writer.writeBytes(signature);
        }
        if (currRequest.header.sid != user.uid) {
            server.updateCredits(creditCharge);
            TraceLog.get().exit(300);
            throw new SmbException("connection was reestablished", -1073741267);
        }
        object = server;
        synchronized (object) {
            CaptureInternal capture = server.transport.getCapture();
            capture.capturePacketWriteStart(true, false, server.transport.getSocket());
            try {
                server.transport.send(currRequest.buffer.data, 0, packetLen + currRequest.tail.dataLen, packetLen);
            }
            catch (Exception e) {
                server.updateCredits(creditCharge);
                TraceLog.get().caught(e, 300);
                throw (SmbException)Utility.throwableInitCauseException(new SmbException("Send failed : " + e.getMessage(), -1073741267), e);
            }
            try {
                if (0 != currRequest.tail.dataLen) {
                    server.transport.sendTail(currRequest.tail.data, currRequest.tail.offset, currRequest.tail.dataLen);
                }
            }
            catch (NetbiosException e) {
                server.updateCredits(creditCharge);
                TraceLog.get().caught(e, 300);
                throw (SmbException)Utility.throwableInitCauseException(new SmbException("Send failed : " + e.getMessage(), -1073741267), e);
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
        byte[] signature;
        boolean areSignaturesEqual;
        TraceLog.get().enter(300);
        Response response = (Response)abstractResponse;
        Request request = (Request)abstractRequest;
        response.buffer = null;
        ClientSmb.Match match = new ClientSmb.Match(this);
        match.server = server;
        match.isResponseAllocated = false;
        match.matchExtraInfo = 0;
        match.response = response;
        match.userId = user.uid;
        try {
            server.smb.sendRequest(server, user, request, match);
        }
        catch (NetbiosException e) {
            HashMap hashMap = server.expectedResponses;
            synchronized (hashMap) {
                server.expectedResponses.remove(match.mid);
            }
            TraceLog.get().caught(e, 300);
            throw new NetbiosException("Send failed: " + e.getErrCode(), -501);
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
            if (2 != request.header.command && 4 != request.header.command) {
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
        if (2 != request.header.command) {
            Server.lock(server);
            boolean brokenConnection = server.connectionBroke;
            Server.releaseLock(server);
            if (brokenConnection) {
                block26: {
                    if (request.header.command != 0 && request.header.command != 1) {
                        try {
                            if (!server.reconnect()) {
                                TraceLog.get().exit(300);
                                throw new ClientException("Unable to restore connection", -111);
                            }
                            break block26;
                        }
                        catch (Exception e) {
                            TraceLog.get().caught(e, 300);
                            throw new ClientException("Unable to reconnect: " + e.getMessage(), -111);
                        }
                    }
                    TraceLog.get().exit(300);
                    throw new SmbException("Connection invalid ", -1073741267);
                }
                Server.lock(server);
                server.connectionBroke = false;
                Server.releaseLock(server);
            }
        }
        if (!server.transport.isConnected()) {
            server.smb.signalAllMatch(server.transport);
            TraceLog.get().exit(300);
            throw new SmbException("Connection invalid ", -1073741267);
        }
        if (!((Response)match.response).wasReceived) {
            HashMap brokenConnection = server.expectedResponses;
            synchronized (brokenConnection) {
                server.expectedResponses.remove(match.mid);
            }
            TraceLog.get().exit(300);
            throw new SmbException("Response was not received", -1073741267);
        }
        if (server.mustUseSignatures() && user.useSignatures() && 0 != (response.header.flags & 8) && response.header.command != 1 && response.header.command != 2 && -1073741309 != response.header.status && !(areSignaturesEqual = Arrays.equals(signature = Smb200.calculateMessageSignature(user.macSessionKey.data, user.macSessionKey.len, match.hdrBuf, 66, 0, response.buffer, null == response.buffer ? 0 : response.buffer.length, 0), response.header.signature))) {
            if (TraceLog.get().canLog(10)) {
                TraceLog.get().message("Signature mismatch, received signature=" + HexBuilder.toHex(response.header.signature) + ", calculated signature=" + HexBuilder.toHex(signature) + ", macSessionKey=" + HexBuilder.toHex(user.macSessionKey.data, user.macSessionKey.len), 10);
            }
            throw new SmbException("Signature Mismatch", -1073741819);
        }
        TraceLog.get().message("status = " + response.header.status, 2000);
        TraceLog.get().exit(300);
        return response.header.status;
    }

    protected void keyDerivation(User user) {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void handleWaitingNotifyResponses(Server server, File file) throws NqException {
        TraceLog.get().enter(250);
        ArrayList waitingResponses = null;
        Object object = server.waitingNotifyResponsesSync;
        synchronized (object) {
            waitingResponses = new ArrayList((Vector)server.waitingNotifyResponses.clone());
        }
        for (WaitingResponse responseItem : waitingResponses) {
            if (!Arrays.equals(responseItem.fid, file.fid)) continue;
            if (null == Smb200.commandDescriptors[responseItem.notifyResponse.header.command].notifyConsumer) {
                TraceLog.get().exit(250);
                return;
            }
            Smb200.commandDescriptors[responseItem.notifyResponse.header.command].notifyConsumer.complete(server, responseItem.notifyResponse, file);
        }
        TraceLog.get().exit(250);
    }

    protected File fileFindById(Server server, byte[] fid) {
        TraceLog.get().enter(300);
        for (User user : server.users.values()) {
            for (Share share : user.shares.values()) {
                for (File file : share.files.values()) {
                    if (!Arrays.equals(file.fid, fid)) continue;
                    TraceLog.get().exit(300);
                    return file;
                }
            }
        }
        TraceLog.get().exit(300);
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void handleNotification(Server server, Smb2Header header) throws NqException {
        BufferReader reader;
        TraceLog.get().enter(300);
        Response response = new Response();
        Buffer rcvBuffer = null;
        if (server.transport.getReceivingRemain() > 0) {
            response.tailLen = server.transport.getReceivingRemain();
            rcvBuffer = new Buffer(response.tailLen);
            response.buffer = rcvBuffer.data;
            if (response.tailLen != server.transport.receiveBytes(rcvBuffer, 0, response.tailLen)) {
                TraceLog.get().exit(300);
                return;
            }
        }
        server.transport.receiveEnd(rcvBuffer);
        response.reader = reader = new BufferReader(response.buffer, 0, false);
        response.header = header;
        if (-1073741790 == response.header.status) {
            TraceLog.get().exit("ACCESS_DENIED for mid = " + response.header.mid, 300);
            return;
        }
        reader.skip(6);
        byte[] fid = new byte[16];
        reader.readBytes(fid, 16);
        reader.setOffset(0);
        File file = this.fileFindById(server, fid);
        if (null == file) {
            WaitingResponse waitingResponse = new WaitingResponse();
            Object object = server.waitingNotifyResponsesSync;
            synchronized (object) {
                server.waitingNotifyResponses.add(waitingResponse);
            }
            waitingResponse.notifyResponse = response;
            waitingResponse.fid = fid;
            TraceLog.get().exit(300);
            return;
        }
        try {
            Smb200.commandDescriptors[header.command].notifyConsumer.complete(server, response, file);
        }
        catch (NqException e) {
            TraceLog.get().error("NqException = ", e, 10, e.getErrCode());
        }
        TraceLog.get().exit(300);
    }

    public boolean doValidateNegotiate(Server server, User user, Share share, short[] dialects) throws NqException {
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean removeReadWriteMatch(Object context, Server server, boolean isReadMatch) throws NqException {
        TraceLog.get().enter(250);
        HashMap hashMap = server.expectedResponses;
        synchronized (hashMap) {
            Iterator iterator = server.expectedResponses.entrySet().iterator();
            short matchType = (short)(isReadMatch ? 2 : 1);
            while (iterator.hasNext()) {
                ClientSmb.Match match = (ClientSmb.Match)iterator.next().getValue();
                if (0 == (match.matchExtraInfo & matchType)) continue;
                match.response = null;
                iterator.remove();
                break;
            }
        }
        TraceLog.get().exit(250);
        return true;
    }

    private int create(File file, boolean setDfsFlag) throws NqException {
        TraceLog.get().enter(300);
        TraceLog.get().message("file = " + file + "; setDfsFlag = " + setDfsFlag, 2000);
        int res = 0;
        boolean doContext = 1 == file.durableState && 9 == file.oplockLevel;
        Share share = file.share;
        User user = share.getUser();
        Server server = user.getServer();
        Response response = new Response();
        ContextDescriptor ctx = null;
        for (int i = 0; i < 2; ++i) {
            boolean isFullPath = false;
            Request request = new Request();
            server.smb.prepareSingleRequestByShare(request, share, (short)5, 0);
            if (!setDfsFlag || (share.flags & 1) == 0 || file.getParams() != null && !file.getParams().isDir) {
                request.header.flags &= 0xEFFFFFFF;
            } else {
                if ((share.flags & 4) != 0) {
                    isFullPath = true;
                }
                request.header.flags |= 0x10000000;
            }
            Smb200.writeHeader(request);
            BufferWriter writer = request.writer;
            String name = file.getLocalPathFromShare();
            if (isFullPath) {
                String fullname = server.getName() + "\\" + share.getName();
                name = name.length() > 0 ? fullname + "\\" + name : fullname;
            }
            name = name.equals("") ? "\u0000" : name;
            short nameLength = (short)(name.length() * 2);
            writer.writeByte((byte)0);
            writer.writeByte(file.oplockLevel);
            writer.writeInt4(2);
            writer.writeInt4(0);
            writer.writeInt4(0);
            writer.writeZeros(8);
            writer.writeInt4(file.accessMask);
            writer.writeInt4(file.info.getAttributes());
            writer.writeInt4(file.shareAccess);
            writer.writeInt4(file.disposition);
            writer.writeInt4(file.createOptions);
            int nameOffsetPosition = writer.getOffset();
            writer.skip(2);
            writer.writeInt2(name.equals("\u0000") ? (short)0 : nameLength);
            int contextOffsetPosition = writer.getOffset();
            writer.writeInt4(0);
            writer.writeInt4(0);
            writer.align(4, 8);
            int tmp = writer.getOffset();
            int nameOffset = writer.getOffset() - 4;
            writer.setOffset(nameOffsetPosition);
            writer.writeInt2(nameOffset);
            writer.setOffset(tmp);
            try {
                writer.writeBytes(name.getBytes("UTF-16LE"), nameLength);
            }
            catch (UnsupportedEncodingException e) {
                TraceLog.get().caught(e, 300);
                throw new ClientException("Unsupported UTF-16");
            }
            if (doContext && !file.share.isIpc) {
                boolean doDurHandleV2 = server.getSmb().getRevision() != -1 && server.getSmb().getRevision() >= 768;
                int ctxId = 0;
                if (doDurHandleV2) {
                    ctxId = 2;
                    file.durableFlags = 0;
                }
                writer.align(4, 8);
                int contextOffset = writer.getOffset() - 4;
                ctx = ContextDescriptor.ctxDscCreator(ctxId);
                int contextLength = ctx.pack(writer, file);
                tmp = writer.getOffset();
                writer.setOffset(contextOffsetPosition);
                writer.writeInt4(contextOffset);
                writer.writeInt4(contextLength);
                writer.setOffset(tmp);
            } else {
                tmp = writer.getOffset();
                writer.setOffset(contextOffsetPosition);
                writer.writeInt4(0);
                writer.writeInt4(0);
                writer.setOffset(tmp);
            }
            res = server.smb.sendReceive(server, user, request, response);
            if (-1073741811 != res) break;
            doContext = false;
        }
        if (0 != res) {
            TraceLog.get().exit(300);
            throw new SmbException("File creation error", res);
        }
        BufferReader reader = response.reader;
        file.oplockLevel = reader.readByte();
        reader.skip(1);
        reader.skip(4);
        file.info.setCreationTime(reader.readLong());
        file.info.setLastAccessTime(reader.readLong());
        file.info.setLastWriteTime(reader.readLong());
        file.info.setChangeTime(reader.readLong());
        file.info.setAllocationSize(reader.readLong());
        file.info.setEof(reader.readLong());
        file.info.setAttributes(reader.readInt4());
        reader.skip(4);
        reader.readBytes(file.fid, 16);
        reader.readInt4();
        int contextLength = reader.readInt4();
        if (ctx != null && contextLength > 0 && contextLength <= response.buffer.length - response.reader.getOffset()) {
            ctx.process(reader, file);
        }
        TraceLog.get().exit(300);
        return res;
    }

    private static void fileInfoResponseParser(BufferReader reader, File.Info info, byte level) throws NqException {
        TraceLog.get().enter(300);
        switch (level) {
            case 4: {
                info.setCreationTime(reader.readLong());
                info.setLastAccessTime(reader.readLong());
                info.setLastWriteTime(reader.readLong());
                info.setChangeTime(reader.readLong());
                info.setAttributes(reader.readInt4());
                reader.skip(4);
                break;
            }
            case 5: {
                info.setAllocationSize(reader.readLong());
                info.setEof(reader.readLong());
                info.setNumberOfLinks(reader.readInt4());
                break;
            }
            case 6: {
                info.setFileIndex(reader.readLong());
                break;
            }
        }
        TraceLog.get().exit(300);
    }

    protected Smb2Header readHeader(BufferReader reader) throws NqException {
        TraceLog.get().enter(300);
        Smb2Header header = new Smb2Header();
        reader.readBytes(header.protocolId, 4);
        header.size = reader.readInt2();
        header.creditCharge = reader.readInt2();
        header.status = reader.readInt4();
        header.command = reader.readInt2();
        header.credits = reader.readInt2();
        header.flags = reader.readInt4();
        header.chainOffset = reader.readInt4();
        header.mid = reader.readLong();
        if (0 != (header.flags & 2)) {
            header.aid = reader.readLong();
            header.pid = 0;
            header.tid = 0;
        } else {
            header.aid = 0L;
            header.pid = reader.readInt4();
            header.tid = reader.readInt4();
        }
        header.sid = reader.readLong();
        reader.readBytes(header.signature, header.signature.length);
        TraceLog.get().exit(300);
        return header;
    }

    protected static boolean writeHeader(Request request) {
        TraceLog.get().enter(300);
        BufferWriter writer = request.writer;
        Smb2Header header = request.header;
        writer.writeBytes(header.protocolId, header.protocolId.length);
        writer.writeInt2(header.size);
        writer.writeInt2(header.creditCharge);
        writer.writeInt4(header.status);
        writer.writeInt2(header.command);
        writer.writeInt2(header.credits);
        writer.writeInt4(header.flags);
        writer.writeInt4(header.chainOffset);
        writer.writeLong(header.mid);
        if (0 != (header.flags & 2)) {
            writer.writeLong(header.aid);
        } else {
            writer.writeInt4(header.pid);
            writer.writeInt4(header.tid);
        }
        writer.writeLong(header.sid);
        writer.writeBytes(header.signature, 16);
        writer.writeInt2(Smb2Params.getSmb2StructureSize(header.command, Smb2Params.smb2Req));
        TraceLog.get().exit(300);
        return true;
    }

    private static byte[] calculateMessageSignature(byte[] key, int keyLen, byte[] buffer1, int size1, int buffer1Offset, byte[] buffer2, int size2, int buffer2Offset) {
        int tmp;
        TraceLog.get().enter(300);
        byte[] ipad = new byte[64];
        byte[] opad = new byte[64];
        byte[] hash = new byte[32];
        Blob[] frag1 = new Blob[3];
        Blob[] frag2 = new Blob[2];
        for (tmp = 0; tmp < 3; ++tmp) {
            frag1[tmp] = new Blob();
        }
        for (tmp = 0; tmp < 2; ++tmp) {
            frag2[tmp] = new Blob();
        }
        frag1[0].data = ipad;
        frag1[0].len = ipad.length;
        frag1[1].data = new byte[size1];
        System.arraycopy(buffer1, buffer1Offset, frag1[1].data, 0, size1);
        frag1[1].len = size1;
        if (null != buffer2) {
            frag1[2].data = new byte[size2];
            System.arraycopy(buffer2, buffer2Offset, frag1[2].data, 0, size2);
        } else {
            frag1[2].data = null;
        }
        frag1[2].len = size2;
        frag2[0].data = opad;
        frag2[0].len = opad.length;
        frag2[1].data = hash;
        frag2[1].len = hash.length;
        Arrays.fill(ipad, (byte)54);
        Arrays.fill(opad, (byte)92);
        for (int i = 0; i < keyLen; ++i) {
            int n = i;
            ipad[n] = (byte)(ipad[n] ^ key[i]);
            int n2 = i;
            opad[n2] = (byte)(opad[n2] ^ key[i]);
        }
        BufferWriter writer = new BufferWriter(frag1[1].data, 48, false);
        writer.writeZeros(16);
        Sha256.sha256Internal(null, null, frag1, 3, hash, hash.length);
        Sha256.sha256Internal(null, null, frag2, 2, hash, hash.length);
        byte[] signature = new byte[16];
        System.arraycopy(hash, 0, signature, 0, signature.length);
        TraceLog.get().exit(300);
        return signature;
    }

    public static void calcMessagesHash(byte[] buffer, int size, int bufferOffset, byte[] digest, byte[] ctxBuff) throws NqException {
        TraceLog.get().enter(300);
        Blob[] fragments = new Blob[]{new Blob(), new Blob(size)};
        fragments[0].data = digest;
        fragments[0].len = 64;
        System.arraycopy(buffer, bufferOffset, fragments[1].data, 0, size);
        Sha512.sha512Internal(null, null, fragments, 2, digest, 64, ctxBuff);
        TraceLog.get().exit(300);
    }

    private int readNegotiateContexts(BufferReader reader, Server server, int contextCount) throws NqException {
        TraceLog.get().enter(300);
        int numPreauthIntegContext = 0;
        while (contextCount > 0) {
            short contextType = reader.readInt2();
            reader.readInt2();
            reader.skip(4);
            switch (contextType) {
                case 1: {
                    short algorithmCount = reader.readInt2();
                    short tmp = reader.readInt2();
                    short hashAuthAlgorithm = reader.readInt2();
                    ++numPreauthIntegContext;
                    if (algorithmCount > 1 || hashAuthAlgorithm != 1) {
                        TraceLog.get().exit(300);
                        throw new ClientException("Bad algorithm params", -103);
                    }
                    reader.skip(tmp);
                    break;
                }
                case 2: {
                    short cipherCount = reader.readInt2();
                    short cipher = reader.readInt2();
                    if (cipherCount > 1) {
                        TraceLog.get().exit(300);
                        throw new ClientException("Bad cipher count", -103);
                    }
                    if (cipher == 2) {
                        server.isAesGcm = true;
                        break;
                    }
                    if (cipher == 1) {
                        server.isAesGcm = false;
                        break;
                    }
                    TraceLog.get().exit(300);
                    throw new ClientException("Bad cipher", -103);
                }
            }
            if (contextCount > 1) {
                reader.align(0, 8);
            }
            --contextCount;
        }
        if (numPreauthIntegContext != 1) {
            TraceLog.get().exit(300);
            throw new ClientException("Bad preauthInteg context", -103);
        }
        TraceLog.get().exit(300);
        return 0;
    }

    protected static interface NotifyConsumer {
        public void complete(Server var1, Response var2, File var3) throws NqException;
    }

    class WaitingResponse {
        Response notifyResponse;
        byte[] fid = new byte[16];

        WaitingResponse() {
        }
    }

    private class SearchContext {
        byte[] fid = new byte[16];
        boolean findFirst;

        private SearchContext() {
        }
    }

    public static class BreakNotification
    implements NotifyConsumer {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void complete(Server server, Response response, File file) throws NqException {
            Object signature;
            TraceLog.get().enter(300);
            long negMid = -1L;
            CaptureInternal capture = server.transport.getCapture();
            capture.capturePacketWriteEnd();
            Request request = new Request();
            Share share = file.share;
            User user = share.getUser();
            byte oplockLevel = response.reader.readByte();
            if (TraceLog.get().canLog(2000)) {
                TraceLog.get().message("Receive Break response : break from level " + Smb2Params.OplockName.getEnum(file.oplockLevel).toString() + " to level " + Smb2Params.OplockName.getEnum(oplockLevel).toString(), 2000);
            }
            if (0 == oplockLevel && file.oplockLevel == 1 || response.header.mid != negMid) {
                file.oplockLevel = oplockLevel;
                if (response.header.credits > 1) {
                    server.updateCredits(response.header.credits - 1);
                }
                TraceLog.get().exit(300);
                return;
            }
            file.oplockLevel = oplockLevel;
            if (file.durableState == 3) {
                file.durableState = 4;
                file.durableFlags = 0;
                file.durableHandle = new UUID();
            }
            if (null == server.smb.prepareSingleRequestByShare(request, share, (short)18, 0)) {
                TraceLog.get().exit(300);
                return;
            }
            Object object = server.smbContextSync;
            synchronized (object) {
                Context context = (Context)server.smbContext;
                request.header.mid = context.mid++;
                Smb200.writeHeader(request);
            }
            BufferWriter writer = request.writer;
            writer.writeByte(oplockLevel);
            writer.writeZeros(5);
            writer.writeBytes(file.fid, 16);
            int packetLen = writer.getOffset() - 4;
            if (server.mustUseSignatures() && user.useSignatures()) {
                signature = Smb200.calculateMessageSignature(user.macSessionKey.data, user.macSessionKey.len, request.buffer.data, packetLen, 4, request.tail.data, request.tail.dataLen, request.tail.offset);
                request.writer.setOffset(52);
                request.writer.writeBytes((byte[])signature);
            }
            capture.capturePacketWriteStart(true, false, server.transport.getSocket());
            try {
                signature = server;
                synchronized (signature) {
                    server.transport.send(request.buffer.data, 0, packetLen, packetLen);
                }
            }
            catch (Exception e) {
                TraceLog.get().caught(e, 300);
                throw (SmbException)Utility.throwableInitCauseException(new SmbException("Send failed : " + e.getMessage(), -1073741267), e);
            }
            capture.capturePacketWriteEnd();
            TraceLog.get().exit(300);
        }
    }

    private static class ChangeNotifyConsumer
    implements AsyncConsumer {
        private ChangeNotifyConsumer() {
        }

        public void complete(Throwable status, long length, Object context) throws NqException {
            int count = 0;
            ClientSmb.AsyncMatch match = (ClientSmb.AsyncMatch)context;
            Server server = match.server;
            Response response = (Response)match.response;
            File fileDir = match.file;
            CaptureInternal capture = server.transport.getCapture();
            if (-1073741536 == response.header.status) {
                Buffer structBuffer = new Buffer(new byte[64], 0, 64);
                server.transport.receiveEnd(structBuffer);
                server.transport.addToSpoolerQueue(match.consumer, new SmbException(response.header.status), 0, new FileNotifyInformation[0]);
                File file = fileDir;
                file.getClass();
                fileDir.removeAidChangeNotifyQueueEntry(file.new File.AidObject(response.header.aid, null));
                return;
            }
            Buffer structBuffer = new Buffer(new byte[64], 0, 64);
            try {
                server.transport.receiveBytes(structBuffer, 0, 6);
            }
            catch (NetbiosException e) {
                Smb200.handleNetbiosException(status, match, "Unable to receive Notify response");
            }
            BufferReader bufferReader = new BufferReader(structBuffer.data, 0, false);
            if (0 == response.header.status) {
                bufferReader.readInt2();
                length = bufferReader.readInt4();
                try {
                    structBuffer = new Buffer((int)length);
                    server.transport.receiveBytes(structBuffer, 0, (int)length);
                    FileNotifyInformation[] fni = FileNotifyInformation.extractFileNotifyInformation(structBuffer.data);
                    server.transport.addToSpoolerQueue(match.consumer, status, count, fni);
                    File file = fileDir;
                    file.getClass();
                    fileDir.removeAidChangeNotifyQueueEntry(file.new File.AidObject(response.header.aid, null));
                }
                catch (NetbiosException e) {
                    Smb200.handleNetbiosException(status, match, "Failed to receive Notify response");
                    server.transport.addToSpoolerQueue(match.consumer, new ClientException("Failed to receive Notify response", response.header.status), 0, context);
                    File file = fileDir;
                    file.getClass();
                    fileDir.removeAidChangeNotifyQueueEntry(file.new File.AidObject(response.header.aid, null));
                }
            } else if (267 == response.header.status || 268 == response.header.status) {
                server.transport.addToSpoolerQueue(match.consumer, new SmbException(response.header.status), 0, new FileNotifyInformation[0]);
                File file = fileDir;
                file.getClass();
                fileDir.removeAidChangeNotifyQueueEntry(file.new File.AidObject(response.header.aid, null));
            } else {
                server.transport.addToSpoolerQueue(match.consumer, new ClientException("Failed to receive successful status Notify response", response.header.status), 0, context);
                File file = fileDir;
                file.getClass();
                fileDir.removeAidChangeNotifyQueueEntry(file.new File.AidObject(response.header.aid, null));
            }
            server.transport.receiveEnd(null);
            capture.capturePacketWriteEnd();
        }
    }

    private static class ReadConsumer
    implements AsyncConsumer {
        private ReadConsumer() {
        }

        public void complete(Throwable status, long length, Object context) throws NqException {
            CaptureInternal capture;
            Buffer structBuffer;
            Response response;
            Server server;
            ClientSmb.AsyncMatch match;
            int count;
            block25: {
                count = 0;
                match = (ClientSmb.AsyncMatch)context;
                server = match.server;
                response = (Response)match.response;
                structBuffer = new Buffer(new byte[64], 0, 64);
                if (-1073741309 == response.header.status || -1073740964 == response.header.status) {
                    SmbException se = (SmbException)Utility.throwableInitCauseException(new SmbException(-1073741267), new SmbException(response.header.status));
                    TraceLog.get().error("Send STATUS_RETRY with the cause: ", se.getCause(), 10, response.header.status);
                    match.consumer.complete(se, 0L, match.context);
                    return;
                }
                capture = server.transport.getCapture();
                try {
                    if (response.tailLen >= 14 && 14 == server.transport.receiveBytes(structBuffer, 0, 14)) {
                        BufferReader bufferReader = new BufferReader(structBuffer.data, 0, false);
                        if (-1073741267 == response.header.status) {
                            match.consumer.complete(new SmbException(response.header.status), 0L, match.context);
                        }
                        if (0 != response.header.status) break block25;
                        int offset = bufferReader.readInt2();
                        length -= (long)(offset - 66);
                        count = bufferReader.readInt4();
                        if ((offset -= 80) > 0) {
                            try {
                                server.transport.receiveBytes(structBuffer, 0, offset);
                            }
                            catch (NetbiosException e) {
                                capture.capturePacketWriteEnd();
                                Smb200.handleNetbiosException(status, match, "Failed to receive Read response");
                            }
                        }
                        Buffer dataBuffer = match.buffer;
                        try {
                            server.transport.receiveBytes(dataBuffer, dataBuffer.offset, (int)length);
                            break block25;
                        }
                        catch (NetbiosException e) {
                            capture.capturePacketWriteEnd();
                            Smb200.handleNetbiosException(status, match, "Failed to receive Read payload");
                            return;
                        }
                    }
                    if (response.tailLen < 14) {
                        try {
                            server.transport.receiveBytes(structBuffer, 0, response.tailLen);
                        }
                        catch (NetbiosException e) {
                            capture.capturePacketWriteEnd();
                            Smb200.handleNetbiosException(status, match, "Failed to receive Read response");
                            return;
                        }
                        count = 0;
                        break block25;
                    }
                    count = 0;
                }
                catch (NetbiosException e1) {
                    capture.capturePacketWriteEnd();
                    Smb200.handleNetbiosException(status, match, "Failed to receive Read response");
                    return;
                }
            }
            try {
                server.transport.receiveEnd(structBuffer);
            }
            catch (NetbiosException e) {
                capture.capturePacketWriteEnd();
                Smb200.handleNetbiosException(status, match, "Failed to receive Read response");
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
                match.consumer.complete(new NqException("Request timed out", -19), length, context);
            }
            capture.capturePacketWriteEnd();
        }
    }

    private static class WriteConsumer
    implements AsyncConsumer {
        private WriteConsumer() {
        }

        public void complete(Throwable status, long length, Object context) throws NqException {
            ClientSmb.AsyncMatch match = (ClientSmb.AsyncMatch)context;
            Server server = match.server;
            Buffer buffer = Buffer.getNewBuffer((int)length);
            Response response = (Response)match.response;
            if (-1073741309 == response.header.status || -1073740964 == response.header.status) {
                SmbException se = (SmbException)Utility.throwableInitCauseException(new SmbException(-1073741267), new SmbException(response.header.status));
                TraceLog.get().error("Send STATUS_RETRY with the cause: ", se.getCause(), 10, response.header.status);
                match.consumer.complete(se, 0L, match.context);
                return;
            }
            CaptureInternal capture = server.transport.getCapture();
            try {
                if (length != (long)server.transport.receiveBytes(buffer, 0, (int)length)) {
                    match.consumer.complete(new ClientException("Failed to receive Write response", -106), 0L, match.context);
                    return;
                }
            }
            catch (NetbiosException e) {
                Smb200.handleNetbiosException(status, match, "Failed to receive Write response");
                return;
            }
            BufferReader reader = new BufferReader(buffer.data, 0, false);
            if (0 == response.header.status) {
                reader.skip(2);
                length = reader.readInt4();
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
                match.consumer.complete(new NqException("Request timed out", -19), length, context);
            }
            capture.capturePacketWriteEnd();
        }
    }

    protected class Context {
        long mid = 0L;
        int pid = 0;

        protected Context() {
        }
    }

    protected static class Command {
        int requestBufferSize;
        int requestStructSize;
        int responseStructSize;
        AsyncConsumer consumer;
        NotifyConsumer notifyConsumer;

        Command(int requestBufferSize, int requestStructSize, int responseStructSize, AsyncConsumer consumer, NotifyConsumer notifyConsumer) {
            this.requestBufferSize = requestBufferSize;
            this.requestStructSize = requestStructSize;
            this.responseStructSize = responseStructSize;
            this.consumer = consumer;
            this.notifyConsumer = notifyConsumer;
        }
    }
}

