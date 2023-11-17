/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.auth.Aes128ccm;
import com.visuality.nq.auth.Aes128cmac;
import com.visuality.nq.auth.Aes128gcm;
import com.visuality.nq.auth.Sha256;
import com.visuality.nq.client.AsyncConsumer;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.ClientException;
import com.visuality.nq.client.ClientSmb;
import com.visuality.nq.client.File;
import com.visuality.nq.client.FileNotifyInformation;
import com.visuality.nq.client.Request;
import com.visuality.nq.client.Response;
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
import com.visuality.nq.common.HexBuilder;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.Smb2Header;
import com.visuality.nq.common.Smb2Params;
import com.visuality.nq.common.Smb2TransformHeader;
import com.visuality.nq.common.SmbException;
import com.visuality.nq.common.TimeUtility;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.UUID;
import com.visuality.nq.common.Utility;
import com.visuality.nq.resolve.NetbiosException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class Smb300
extends Smb200 {
    protected static AsyncConsumer readCallback = new ReadConsumer();
    protected static AsyncConsumer writeCallback = new WriteConsumer();
    protected static AsyncConsumer changeNotifyCallback = new ChangeNotifyConsumer();
    protected static Smb200.NotifyConsumer breakNotificationHandler = new BreakNotification();
    protected static Command[] commandDescriptors = new Command[]{new Command(128, 36, 65, null, null), new Command(26, 25, 9, null, null), new Command(6, 4, 4, null, null), new Command(10, 9, 16, null, null), new Command(6, 4, 4, null, null), new Command(4096, 57, 89, null, null), new Command(26, 24, 60, null, null), new Command(26, 24, 4, null, null), new Command(64, 49, 17, readCallback, null), new Command(64, 49, 17, writeCallback, null), new Command(0, 0, 0, null, null), new Command(100, 57, 49, null, null), new Command(0, 4, 0, changeNotifyCallback, null), new Command(4, 4, 4, null, null), new Command(40, 33, 9, null, null), new Command(24, 32, 9, changeNotifyCallback, null), new Command(44, 41, 9, null, null), new Command(80, 33, 2, null, null), new Command(100, 24, 0, null, breakNotificationHandler)};

    public Smb300() {
        this.restrictCrypters = true;
        this.setRevision((short)768);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected int sendRequest(Server server, User user, Object abstractRequest, ClientSmb.Match match) throws NqException {
        BufferWriter writer;
        int packetLen;
        TraceLog.get().enter(300);
        Request request = (Request)abstractRequest;
        int creditCharge = 1;
        if (0 != (server.capabilities & 8)) {
            TraceLog.get().message("request.header.creditCharge = " + request.header.creditCharge, 2000);
            creditCharge = request.header.creditCharge;
        }
        if (!server.transport.isConnected() || !user.isLoggedOn) {
            if (!server.transport.isConnected()) {
                TraceLog.get().exit(300);
                throw new SmbException("Connection invalid", -1073741267);
            }
            if (!user.isLoggedOn && 1 != request.command) {
                TraceLog.get().exit(300);
                throw new SmbException("Connection invalid", -1073741267);
            }
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
            Object object = server.smbContextSync;
            synchronized (object) {
                Smb200.Context context;
                if (null != server.smbContext) {
                    context = (Smb200.Context)server.smbContext;
                    request.header.mid = match.mid = context.mid;
                    packetLen = request.writer.getOffset() - 4;
                    writer = new BufferWriter(request.buffer.data, 28, false);
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
                context.mid = context.mid + (long)(request.header.creditCharge > 0 ? (int)request.header.creditCharge : 1);
            }
        } else {
            packetLen = request.writer.getOffset() - 4;
            writer = new BufferWriter(request.buffer.data, 28, false);
            writer.writeLong(0L);
        }
        if (0 != (request.header.flags & 8) && !request.encrypt) {
            byte[] signature;
            try {
                signature = Smb300.calculateMessageSignature(user.macSessionKey.data, user.macSessionKey.len, request.buffer.data, packetLen, 4, request.tail.data, request.tail.dataLen, request.tail.offset);
            }
            catch (Exception e) {
                server.updateCredits(creditCharge);
                TraceLog.get().error("Error calling calculateMessageSignature, user = " + user + ", request = " + request);
                TraceLog.get().caught(e, 300);
                throw (SmbException)Utility.throwableInitCauseException(new SmbException("Send failed (signature error): " + e.getClass().getName() + ", " + e.getMessage(), -1073741267), e);
            }
            request.writer.setOffset(52);
            request.writer.writeBytes(signature);
        }
        if (request.encrypt && 1 != request.command) {
            int msgLen = packetLen + request.tail.dataLen;
            Buffer encryptedBuf = new Buffer(msgLen + 52 + 4);
            Smb2TransformHeader transformHeader = new Smb2TransformHeader();
            transformHeader.setEncryptionArgorithm((short)1);
            transformHeader.setOriginalMsgSize(msgLen);
            TraceLog.get().message("new sid value=" + user.uid, 2000);
            transformHeader.setSid(user.uid);
            Smb300.composeEncryptionNonce(transformHeader.nonce, null != match ? match.mid : 0L);
            writer = new BufferWriter(encryptedBuf.data, 4, false);
            int addPoint = writer.getOffset();
            addPoint += 20;
            Smb2TransformHeader.writeHeader(transformHeader, writer);
            int msgPoint = writer.getOffset();
            int copyLen = request.buffer.dataLen < msgLen ? request.buffer.dataLen - 4 : msgLen;
            CaptureInternal capture = server.transport.getCapture();
            capture.capturePacketWriteStart(true, false, server.transport.getSocket());
            byte[] tmpWriteBuff = new byte[copyLen];
            System.arraycopy(request.buffer.data, 4, tmpWriteBuff, 0, packetLen);
            capture.capturePacketWritePacket(tmpWriteBuff, packetLen);
            writer.writeBytes(tmpWriteBuff, packetLen);
            if (null != request.tail.data) {
                byte[] tmpbuf = new byte[request.tail.dataLen];
                System.arraycopy(request.tail.data, request.tail.offset, tmpbuf, 0, request.tail.dataLen);
                writer.writeBytes(tmpbuf, request.tail.dataLen);
                capture.capturePacketWritePacket(tmpbuf, request.tail.dataLen);
            }
            capture.capturePacketWriteEnd();
            Smb300.encryptMessage(user.decryptionKey, transformHeader.nonce, encryptedBuf.data, msgPoint, msgLen, addPoint, 32, addPoint - 16, user.getServer().isAesGcm);
            if (request.header.sid != user.uid) {
                server.updateCredits(creditCharge);
                TraceLog.get().exit(300);
                throw new SmbException("connection was reestablished", -1073741267);
            }
            Server server2 = server;
            synchronized (server2) {
                try {
                    server.transport.send(encryptedBuf.data, 0, msgLen + 52, msgLen + 52, false);
                }
                catch (Exception e) {
                    server.updateCredits(creditCharge);
                    TraceLog.get().caught(e, 300);
                    throw (SmbException)Utility.throwableInitCauseException(new SmbException("Send failed : " + e.getMessage(), -1073741267), e);
                }
            }
        }
        if (user.isPreauthIntegOn && 1 == request.command && server.getSmb().getRevision() == 785) {
            if (request.tail.dataLen > 0) {
                Buffer packetBuf = new Buffer(packetLen + request.tail.dataLen);
                System.arraycopy(request.buffer.data, 4, packetBuf.data, 0, packetLen);
                System.arraycopy(request.tail.data, 0, packetBuf.data, packetLen, request.tail.dataLen);
                Smb300.calcMessagesHash(packetBuf.data, packetLen + request.tail.dataLen, 0, user.preauthIntegHashVal, null);
            } else {
                Smb300.calcMessagesHash(request.buffer.data, 4, packetLen, user.preauthIntegHashVal, null);
            }
        }
        if (request.header.sid != user.uid) {
            server.updateCredits(creditCharge);
            TraceLog.get().exit(300);
            throw new SmbException("connection was reestablished", -1073741267);
        }
        Server server3 = server;
        synchronized (server3) {
            CaptureInternal capture = server.transport.getCapture();
            capture.capturePacketWriteStart(true, false, server.transport.getSocket());
            try {
                server.transport.send(request.buffer.data, 0, packetLen + request.tail.dataLen, packetLen);
            }
            catch (Exception e) {
                server.updateCredits(creditCharge);
                TraceLog.get().caught(e, 300);
                throw (SmbException)Utility.throwableInitCauseException(new SmbException("Send failed : " + e.getMessage(), -1073741267), e);
            }
            if (0 != request.tail.dataLen) {
                try {
                    server.transport.sendTail(request.tail.data, request.tail.offset, request.tail.dataLen);
                }
                catch (NetbiosException e) {
                    server.updateCredits(creditCharge);
                    TraceLog.get().caught(e, 300);
                    throw (SmbException)Utility.throwableInitCauseException(new SmbException("Send failed : " + e.getMessage(), -1073741267), e);
                }
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
        TraceLog.get().enter(300);
        Request request = (Request)abstractRequest;
        Response response = (Response)abstractResponse;
        response.buffer = null;
        ClientSmb.Match match = new ClientSmb.Match(this);
        match.server = server;
        match.isResponseAllocated = false;
        match.matchExtraInfo = 0;
        match.response = response;
        match.userId = user.uid;
        int res = server.smb.sendRequest(server, user, request, match);
        if (0 != res) {
            HashMap hashMap = server.expectedResponses;
            synchronized (hashMap) {
                server.expectedResponses.remove(match.mid);
            }
            TraceLog.get().exit(300);
            throw new ClientException("send request failed", res);
        }
        boolean isSent = false;
        try {
            isSent = match.syncObj.syncWait(Client.getSmbTimeout());
        }
        catch (InterruptedException e) {
            TraceLog.get().caught(e, 300);
            this.handleTimeout(server, request, response, match);
            throw (SmbException)Utility.throwableInitCauseException(new SmbException("Timeout waiting for response from transaction mid=" + match.mid, -1073741267), e);
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
                block33: {
                    if (request.header.command != 0 && request.header.command != 1) {
                        try {
                            if (!server.reconnect()) {
                                TraceLog.get().exit(300);
                                throw new ClientException("Unable to restore connection", -111);
                            }
                            break block33;
                        }
                        catch (NqException e) {
                            TraceLog.get().caught(e, 300);
                            throw new ClientException("Unable to reconnect: " + e, -111);
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
            TraceLog.get().exit("Response not received; removing mid " + match.mid, 300);
            throw new SmbException("Response was not received", -1073741267);
        }
        if (user.isPreauthIntegOn && response.header.command == 1 && server.smb.getRevision() == 785) {
            if (null == response.buffer && response.tailLen > 0) {
                TraceLog.get().exit(300);
                throw new SmbException("Data error", -1073741762);
            }
            if (response.header.status != 0 && response.header.status != -1073741802) {
                System.arraycopy(server.preauthIntegHashVal, 0, user.preauthIntegHashVal, 0, 64);
                TraceLog.get().exit(300);
                throw new SmbException("Session setup error", response.header.status);
            }
            if (response.header.status != 0) {
                byte[] buf = new byte[66 + response.tailLen];
                System.arraycopy(match.hdrBuf, 0, buf, 0, 66);
                if (response.tailLen > 0) {
                    System.arraycopy(response.buffer, 0, buf, 66, response.tailLen);
                }
                Smb300.calcMessagesHash(buf, 66 + response.tailLen, 0, user.preauthIntegHashVal, null);
            } else {
                int readerOffset = response.reader.getOffset();
                short sessionFlags = response.reader.readInt2();
                response.reader.setOffset(readerOffset);
                if (!user.isAnonymous && 0 == (sessionFlags & 1)) {
                    if (null == user.macSessionKey.data) {
                        TraceLog.get().exit(300);
                        throw new SmbException("Signature Mismatch", -1073741819);
                    }
                    server.smb.keyDerivation(user);
                    user.isPreauthIntegOn = false;
                    if (-1073741309 != response.header.status && !request.encrypt && !Smb300.checkMessageSignature(user, match.hdrBuf, 66, response.buffer, response.tailLen)) {
                        TraceLog.get().exit("Signature mismatch for match.mid = " + match.mid + ", response.mid = " + response.header.mid, 300);
                        throw new SmbException("Signature mismatch for match.mid = " + match.mid + ", response.mid = " + response.header.mid, -1073741819);
                    }
                }
            }
            TraceLog.get().message("status = " + response.header.status, 2000);
            TraceLog.get().exit(300);
            return response.header.status;
        }
        if (!request.encrypt && server.mustUseSignatures() && user.useSignatures() && 0 != (8 & response.header.flags) && 1 != response.header.command && 2 != response.header.command && -1073741309 != response.header.status && !Smb300.checkMessageSignature(user, match.hdrBuf, 66, response.buffer, response.tailLen)) {
            TraceLog.get().exit("Signature mismatch for match.mid = " + match.mid + ", response.mid = " + response.header.mid, 300);
            throw new SmbException("Signature mismatch for match.mid = " + match.mid + ", response.mid = " + response.header.mid, -1073741819);
        }
        TraceLog.get().message("status = " + response.header.status, 2000);
        TraceLog.get().exit(300);
        return response.header.status;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void response(Transport transport) throws NqException {
        ClientSmb.Match match;
        Object user;
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
        Buffer buffer = new Buffer(66);
        transport.receiveBytes(buffer, 0, 4, false);
        Buffer decryptPacket = null;
        byte[] tmpBuff = new byte[4];
        System.arraycopy(buffer.data, 0, tmpBuff, 0, 4);
        if (Arrays.equals(tmpBuff, Smb2TransformHeader.smb2TrnsfrmHdrProtocolId)) {
            Buffer tHdr = new Buffer(52);
            System.arraycopy(Smb2TransformHeader.smb2TrnsfrmHdrProtocolId, 0, tHdr.data, 4, Smb2TransformHeader.smb2TrnsfrmHdrProtocolId.length);
            transport.receiveBytes(tHdr, 4, 48, false);
            BufferReader reader = new BufferReader(tHdr.data, 0, false);
            Smb2TransformHeader transHeader = new Smb2TransformHeader();
            Smb2TransformHeader.readHeader(transHeader, reader);
            user = User.findUser(server, transHeader.getSid());
            if (null == user) {
                server.transport.receiveEnd(buffer);
                TraceLog.get().exit(300);
                return;
            }
            TraceLog.get().message("Decrypting packet; mid=" + transHeader.mid, 2000);
            decryptPacket = this.responseDecryptPacket(transport, buffer, tHdr, transHeader, (User)user);
        } else {
            transport.receiveBytes(buffer, 4, 62, false);
        }
        capture.capturePacketWriteStart(true, true, transport.getSocket());
        capture.capturePacketWritePacket(buffer.data, 66);
        if (null != decryptPacket) {
            capture.capturePacketWritePacket(decryptPacket.data, 66, decryptPacket.dataLen - 66);
            capture.capturePacketWriteEnd();
        }
        BufferReader reader = new BufferReader(buffer.data, 0, false);
        Smb2Header header = this.readHeader(reader);
        user = server.expectedResponses;
        synchronized (user) {
            match = (ClientSmb.Match)server.expectedResponses.get(header.mid);
        }
        TraceLog.get().message(header, 2000);
        if (null == match) {
            TraceLog.get().message("No match found in response; header = " + header, 2000);
            this.responseNoMatch(server, buffer, decryptPacket, header);
            capture.capturePacketWriteEnd();
            TraceLog.get().exit(300);
            return;
        }
        ((Response)match.response).header = header;
        match.status = header.status;
        short length = reader.readInt2();
        if (0 == header.status && length != Smb300.commandDescriptors[header.command].responseStructSize) {
            ((Response)match.response).header.status = -1;
        }
        if (0 != (header.flags & 2) && header.status == 259) {
            TraceLog.get().message("Pending response received; mid=" + header.mid, 2000);
            this.responsePending(transport, buffer, header, match);
            server.transport.discardReceive();
        } else {
            Response response;
            HashMap hashMap = server.expectedResponses;
            synchronized (hashMap) {
                server.expectedResponses.remove(match.mid);
            }
            System.arraycopy(buffer.data, 0, match.hdrBuf, 0, 66);
            if (null != Smb300.commandDescriptors[header.command].consumer) {
                response = (Response)match.response;
                if (null != decryptPacket) {
                    response.tailLen = decryptPacket.dataLen - 66;
                    response.buffer = new byte[response.tailLen];
                    if (null == response.buffer) {
                        isCreditsPosted = server.updateCredits(header, match);
                        server.transport.receiveEnd(buffer);
                        TraceLog.get().exit(300);
                        return;
                    }
                    System.arraycopy(decryptPacket.data, 66, response.buffer, 0, response.tailLen);
                } else {
                    response.tailLen = server.transport.getReceivingRemain();
                    response.buffer = null;
                }
                if (match.status == -1073741309 || match.status == -1073740964) {
                    TraceLog.get().message("Session was deleted or expired; mid=" + match.mid, 2000);
                    this.responseUserSessionDeletedOrExpired(server, match);
                }
                response.wasReceived = true;
                Smb300.commandDescriptors[header.command].consumer.complete(new SmbException(match.status), response.tailLen, match);
                transport.receiveEnd(buffer);
                TraceLog.get().message("Notify initiator of transaction", 300);
                this.responseMatchNotify(match);
            } else {
                if (null != decryptPacket) {
                    response = (Response)match.response;
                    response.tailLen = decryptPacket.dataLen - 66;
                    response.buffer = new byte[response.tailLen];
                    if (null != response.buffer) {
                        System.arraycopy(decryptPacket.data, 66, response.buffer, 0, response.tailLen);
                        response.reader = new BufferReader(response.buffer, 0, false);
                    }
                } else if (server.transport.getReceivingRemain() > 0) {
                    response = (Response)match.response;
                    response.tailLen = server.transport.getReceivingRemain();
                    Buffer responseBuffer = new Buffer(transport.getReceivingRemain());
                    response.buffer = responseBuffer.data;
                    if (null == response.buffer) {
                        isCreditsPosted = server.updateCredits(header, match);
                        server.transport.receiveEnd(buffer);
                        capture.capturePacketWriteEnd();
                        TraceLog.get().exit(300);
                        return;
                    }
                    if (transport.receiveBytes(responseBuffer, 0, response.tailLen) != response.tailLen) {
                        isCreditsPosted = server.updateCredits(header, match);
                        server.transport.receiveEnd(buffer);
                        capture.capturePacketWriteEnd();
                        TraceLog.get().exit(300);
                        return;
                    }
                    response.reader = new BufferReader(response.buffer, 0, false);
                    if (server.smb.getRevision() == 785 && 1 == header.command) {
                        System.arraycopy(buffer.data, 0, match.hdrBuf, 0, 66);
                    }
                } else {
                    ((Response)match.response).tailLen = 0;
                }
                server.transport.receiveEnd(buffer);
                ((Response)match.response).wasReceived = true;
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
    }

    private Buffer responseDecryptPacket(Transport transport, Buffer buffer, Buffer tHdr, Smb2TransformHeader transHeader, User user) throws NetbiosException, NqException, ClientException {
        Buffer decryptPacket = new Buffer(transHeader.getOriginalMsgSize());
        transport.receiveBytes(decryptPacket, 0, decryptPacket.dataLen, false);
        byte[] tmpDecPack = new byte[32];
        System.arraycopy(tHdr.data, 20, tmpDecPack, 0, tmpDecPack.length);
        if (!this.decryptMessage(user.encryptionKey, transHeader.nonce, decryptPacket.data, decryptPacket.dataLen, tmpDecPack, 32, transHeader.getSignature(), user.getServer().isAesGcm)) {
            TraceLog.get().exit(300);
            throw new ClientException("Decryption error");
        }
        System.arraycopy(decryptPacket.data, 0, buffer.data, 0, buffer.dataLen);
        return decryptPacket;
    }

    private void responseNoMatch(Server server, Buffer buffer, Buffer decryptPacket, Smb2Header header) throws NqException, NetbiosException {
        TraceLog.get().enter(300);
        if (header.command < commandDescriptors.length && null != Smb300.commandDescriptors[header.command].notifyConsumer) {
            this.handleNotification(server, header, decryptPacket);
            TraceLog.get().exit(300);
        } else {
            server.updateCredits(header);
        }
        server.transport.receiveEnd(buffer);
        TraceLog.get().exit(300);
    }

    protected static void handleNetbiosException(Throwable status, ClientSmb.AsyncMatch match, String text) throws NqException {
        if (status instanceof SmbException && -1073741267 == ((SmbException)status).getErrCode()) {
            match.consumer.complete(new SmbException("need to retry the operation", -1073741267), 0L, match.context);
        } else {
            match.consumer.complete(new ClientException(text, -106), 0L, match.context);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void handleNotification(Server server, Smb2Header header, Buffer decryptPacket) throws NqException {
        TraceLog.get().enter(300);
        Response response = new Response();
        Buffer rcvBuffer = null;
        if (null != decryptPacket && null != decryptPacket.data) {
            try {
                server.transport.receiveEnd(rcvBuffer);
            }
            catch (NetbiosException e) {
                TraceLog.get().error("NetbiosException = ", e, 10, e.getErrCode());
            }
            response.tailLen = decryptPacket.dataLen - 66;
            response.buffer = new byte[response.tailLen];
            System.arraycopy(decryptPacket.data, 66, response.buffer, 0, response.tailLen);
        } else if (server.transport.getReceivingRemain() > 0) {
            response.tailLen = server.transport.getReceivingRemain();
            rcvBuffer = new Buffer(response.tailLen);
            response.buffer = rcvBuffer.data;
            if (response.tailLen != server.transport.receiveBytes(rcvBuffer, 0, response.tailLen)) {
                TraceLog.get().exit(300);
                throw new ClientException("Receve data error");
            }
            server.transport.receiveEnd(rcvBuffer);
        }
        BufferReader reader = response.reader = new BufferReader(response.buffer, 0, false);
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
            Smb200.WaitingResponse waitingResponse = new Smb200.WaitingResponse(this);
            Object object = server.waitingNotifyResponsesSync;
            synchronized (object) {
                server.waitingNotifyResponses.add(waitingResponse);
            }
            waitingResponse.notifyResponse = response;
            waitingResponse.fid = fid;
            TraceLog.get().exit(300);
            return;
        }
        Smb300.commandDescriptors[header.command].notifyConsumer.complete(server, response, file);
        TraceLog.get().exit(300);
    }

    public static byte[] calculateMessageSignature(byte[] key, int keyLen, byte[] buffer1, int size1, int buffer1Offset, byte[] buffer2, int size2, int buffer2Offset) {
        TraceLog.get().enter(300);
        Blob[] fragments = new Blob[2];
        Blob keyBlob = new Blob();
        fragments[0] = new Blob(size1);
        fragments[1] = new Blob(size2);
        System.arraycopy(buffer1, buffer1Offset, fragments[0].data, 0, size1);
        if (null != buffer2) {
            System.arraycopy(buffer2, buffer2Offset, fragments[1].data, 0, size2);
        } else {
            fragments[1].data = null;
        }
        keyBlob.data = key;
        keyBlob.len = keyLen;
        BufferWriter writer = new BufferWriter(fragments[0].data, 48, false);
        writer.writeZeros(16);
        byte[] signature = new byte[16];
        Aes128cmac.aes128cmacInternal(keyBlob, null, fragments, 2, signature, 16);
        TraceLog.get().exit(300);
        return signature;
    }

    static boolean checkMessageSignature(User user, byte[] pHeaderIn, int headerDataLength, byte[] buffer, int bufLength) {
        TraceLog.get().enter(300);
        byte[] sigReceived = new byte[16];
        System.arraycopy(pHeaderIn, 48, sigReceived, 0, 16);
        byte[] sig = Smb300.calculateMessageSignature(user.macSessionKey.data, user.macSessionKey.len, pHeaderIn, headerDataLength, 0, buffer, bufLength, 0);
        boolean areSignaturesEquals = Arrays.equals(sigReceived, sig);
        if (!areSignaturesEquals && TraceLog.get().canLog(10)) {
            TraceLog.get().message("Signature mismatch, received signature=" + HexBuilder.toHex(sigReceived) + ", calculated signature=" + HexBuilder.toHex(sig) + ", maxSessionKey=" + HexBuilder.toHex(user.macSessionKey.data, user.macSessionKey.len), 10);
        }
        TraceLog.get().exit(300);
        return areSignaturesEquals;
    }

    public static void encryptMessage(byte[] key, byte[] nonce, byte[] msg, int orgMsgPosition, int orgMsgLen, int authPosition, int authLen, int sigPostion, boolean isAesGCM) throws NqException {
        TraceLog.get().enter(300);
        if (isAesGCM) {
            Aes128gcm.aes128GcmEncrypt(key, nonce, msg, orgMsgPosition, orgMsgLen, authPosition, authLen, sigPostion, null, null);
        } else {
            Aes128ccm.aes128CcmEncrypt(key, nonce, msg, orgMsgPosition, orgMsgLen, authPosition, authLen, sigPostion);
        }
        TraceLog.get().exit(300);
    }

    boolean decryptMessage(byte[] key, byte[] nonce, byte[] crptMsg, int msgLen, byte[] authMsg, int authLen, byte[] signature, boolean IsAesGCM) throws NqException {
        TraceLog.get().enter(300);
        if (IsAesGCM) {
            TraceLog.get().exit(300);
            return Aes128gcm.aes128GcmDecrypt(key, nonce, crptMsg, msgLen, authMsg, authLen, signature, null, null);
        }
        TraceLog.get().exit(300);
        return Aes128ccm.aes128CcmDecrypt(key, nonce, crptMsg, msgLen, authMsg, authLen, signature);
    }

    private static void composeEncryptionNonce(byte[] buf, long mid) {
        TraceLog.get().enter(300);
        if (buf == null) {
            TraceLog.get().exit(300);
            return;
        }
        BufferWriter writer = new BufferWriter(buf, 0, false);
        int[] time = TimeUtility.getCurrentTimeAsArray();
        writer.writeInt4(time[0]);
        writer.skip(3);
        writer.writeInt4((int)(mid & 0xFFFFFFFFFFFFFFFFL));
        TraceLog.get().exit(300);
    }

    protected void keyDerivation(User user) {
        TraceLog.get().enter(300);
        if (null != user.macSessionKey.data) {
            if (user.macSessionKey.len > user.getServer().getSmb().maxSigningKeyLen) {
                user.macSessionKey.len = user.getServer().getSmb().maxSigningKeyLen;
            }
            user.encryptionKey = new byte[16];
            user.decryptionKey = new byte[16];
            user.applicationKey = new byte[16];
            Sha256.keyDerivation(user.macSessionKey.data, user.encryptionKey.length, "SMB2AESCCM\u0000".getBytes(), 11, "ServerOut\u0000".getBytes(), 10, user.encryptionKey);
            Sha256.keyDerivation(user.macSessionKey.data, user.decryptionKey.length, "SMB2AESCCM\u0000".getBytes(), 11, "ServerIn \u0000".getBytes(), 10, user.decryptionKey);
            Sha256.keyDerivation(user.macSessionKey.data, user.applicationKey.length, "SMB2APP\u0000".getBytes(), 8, "SmbRpc\u0000".getBytes(), 7, user.applicationKey);
            Sha256.keyDerivation(user.macSessionKey.data, user.macSessionKey.len, "SMB2AESCMAC\u0000".getBytes(), 12, "SmbSign\u0000".getBytes(), 8, user.macSessionKey.data);
        }
        TraceLog.get().exit(300);
    }

    public boolean doValidateNegotiate(Server server, User user, Share share, short[] dialects) throws NqException {
        TraceLog.get().enter(250);
        int res = -1;
        int actualDialects = 0;
        boolean result = false;
        int capabilities = 0;
        if (null == share) {
            throw new NqException("Share must not be null", -20);
        }
        if (!user.useSignatures()) {
            TraceLog.get().exit(250);
            return true;
        }
        Request request = new Request();
        Response response = new Response();
        if (server.isNegotiationValidated) {
            TraceLog.get().exit(250);
            return true;
        }
        server.smb.prepareSingleRequestByShare(request, share, (short)11, 0);
        Smb300.writeHeader(request);
        BufferWriter writer = request.writer;
        writer.writeInt2(0);
        writer.writeInt4(1311236);
        writer.writeInt4(-1);
        writer.writeInt4(-1);
        writer.writeInt4(-1);
        writer.writeInt4(-1);
        int pInputOffset = writer.getOffset();
        writer.skip(12);
        writer.writeInt4(0);
        writer.writeInt4(0);
        writer.writeInt4(24);
        writer.writeInt4(1);
        writer.writeInt4(0);
        int offset = writer.getOffset() - 4;
        writer.writeInt4(capabilities |= 0x44);
        writer.writeLong(server.clientGuid[0]);
        writer.writeLong(server.clientGuid[1]);
        writer.writeInt2(Client.isSigningEnabled() ? 1 : 0);
        actualDialects = dialects.length;
        writer.writeInt2(actualDialects);
        for (int i = 0; i < actualDialects; ++i) {
            writer.writeInt2(dialects[i]);
        }
        int pTemp = writer.getOffset();
        writer.setOffset(pInputOffset);
        writer.writeInt4(offset);
        writer.writeInt4(24 + 2 * actualDialects);
        writer.writeInt4(0);
        writer.setOffset(pTemp);
        boolean useSigning = user.useSigning;
        int capab = server.capabilities;
        user.useSigning = true;
        server.capabilities &= 1;
        res = server.smb.sendReceive(server, user, request, response);
        user.useSigning = useSigning;
        server.capabilities = capab;
        if (res == -1073741637 || res == -1073741811) {
            return true;
        }
        if (0 != res) {
            TraceLog.get().exit(250);
            throw new SmbException("Negotiation validation error");
        }
        if (!request.encrypt && 0 == (response.header.flags & 8)) {
            TraceLog.get().exit(250);
            throw new ClientException("Negotiation validation : not signed");
        }
        BufferReader reader = response.reader;
        reader.skip(2);
        int temp32Uint = reader.readInt4();
        if (temp32Uint != 1311236) {
            TraceLog.get().exit(250);
            return result;
        }
        for (int i = 4; i > 0; --i) {
            temp32Uint = reader.readInt4();
            if (temp32Uint == -1) continue;
            TraceLog.get().exit(250);
            return result;
        }
        reader.skip(4);
        temp32Uint = reader.readInt4();
        if (temp32Uint != 0) {
            TraceLog.get().exit(250);
            return result;
        }
        offset = reader.readInt4();
        reader.skip(4);
        reader.skip(4);
        reader.skip(4);
        reader.setOffset(response.reader.getOffset());
        reader.setOffset(offset - 66);
        temp32Uint = reader.readInt4();
        if (temp32Uint != server.serverCapabilites) {
            TraceLog.get().exit(250);
            throw new ClientException("Negotiation validation : Capabilities error");
        }
        byte[] serverGUID = new byte[16];
        reader.readBytes(serverGUID, 16);
        if (!Arrays.equals(serverGUID, server.serverGUID)) {
            TraceLog.get().exit(250);
            throw new ClientException("Negotiation validation : GUID error");
        }
        short temp16Uint = reader.readInt2();
        if (temp16Uint != server.serverSecurityMode) {
            TraceLog.get().exit(250);
            throw new ClientException("Negotiation validation : Security mode error");
        }
        temp16Uint = reader.readInt2();
        if (temp16Uint != server.serverDialectRevision) {
            TraceLog.get().exit(250);
            throw new ClientException("Negotiation validation : Dialect revision error");
        }
        TraceLog.get().exit(250);
        return true;
    }

    public static class BreakNotification
    implements Smb200.NotifyConsumer {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void complete(Server server, Response response, File file) throws NqException {
            Object encryptedBuf;
            TraceLog.get().enter(300);
            CaptureInternal capture = server.transport.getCapture();
            capture.capturePacketWriteEnd();
            Share share = file.share;
            User user = share.user;
            long negMid = -1L;
            byte oplockLevel = response.reader.readByte();
            if (TraceLog.get().canLog(2000)) {
                TraceLog.get().message("Receive Break response : break from level " + Smb2Params.OplockName.getEnum(file.oplockLevel).toString() + " to level " + Smb2Params.OplockName.getEnum(oplockLevel).toString(), 2000);
            }
            if (0 == oplockLevel && 1 == file.oplockLevel || response.header.mid != negMid) {
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
            Request request = new Request();
            server.smb.prepareSingleRequestByShare(request, share, (short)18, 0);
            BufferWriter writer = request.writer;
            Smb200.writeHeader(request);
            writer.writeByte(oplockLevel);
            writer.writeZeros(5);
            writer.writeBytes(file.fid, file.fid.length);
            int packetLen = writer.getOffset() - 4;
            Object object = server.smbContextSync;
            synchronized (object) {
                Smb200.Context context = (Smb200.Context)server.smbContext;
                request.header.mid = context.mid;
                writer.setOffset(writer.getOffset() + 28);
                writer = new BufferWriter(request.buffer.data, 28, false);
                writer.writeLong(context.mid);
                ++context.mid;
            }
            if (!request.encrypt && server.mustUseSignatures() && user.useSignatures()) {
                byte[] signature = Smb300.calculateMessageSignature(user.macSessionKey.data, user.macSessionKey.len, request.buffer.data, packetLen, 4, request.tail.data, request.tail.dataLen, request.tail.offset);
                request.writer.setOffset(52);
                request.writer.writeBytes(signature);
            }
            capture.capturePacketWriteStart(true, false, server.transport.getSocket());
            if (request.encrypt) {
                encryptedBuf = new Buffer(packetLen + 52 + 4);
                if (null != encryptedBuf) {
                    Smb2TransformHeader transformHeader = new Smb2TransformHeader();
                    transformHeader.setEncryptionArgorithm((short)1);
                    transformHeader.setOriginalMsgSize(packetLen);
                    transformHeader.setSid(user.uid);
                    Smb300.composeEncryptionNonce(transformHeader.nonce, request.header.mid);
                    writer = new BufferWriter(((Buffer)encryptedBuf).data, 4, false);
                    int addPoint = writer.getOffset();
                    addPoint += 20;
                    Smb2TransformHeader.writeHeader(transformHeader, writer);
                    int msgPoint = writer.getOffset();
                    byte[] tmpWriteBuff = new byte[packetLen];
                    System.arraycopy(request.buffer.data, 4, tmpWriteBuff, 0, packetLen);
                    writer.writeBytes(tmpWriteBuff, packetLen);
                    Smb300.encryptMessage(user.decryptionKey, transformHeader.nonce, ((Buffer)encryptedBuf).data, msgPoint, packetLen, addPoint, 32, addPoint - 16, user.getServer().isAesGcm);
                    try {
                        Server server2 = server;
                        synchronized (server2) {
                            server.transport.send(((Buffer)encryptedBuf).data, 0, packetLen + 52, packetLen + 52, false);
                        }
                        capture.capturePacketWritePacket(request.buffer.data, 4, packetLen + 52 - 4);
                    }
                    catch (Exception e) {
                        TraceLog.get().caught(e, 300);
                        throw (SmbException)Utility.throwableInitCauseException(new SmbException("Sending failed : " + e.getMessage(), -1073741267), e);
                    }
                }
            } else {
                try {
                    encryptedBuf = server;
                    synchronized (encryptedBuf) {
                        server.transport.send(request.buffer.data, 0, packetLen, packetLen);
                    }
                }
                catch (Exception e) {
                    TraceLog.get().caught(e, 300);
                    throw (SmbException)Utility.throwableInitCauseException(new SmbException("Sending failed : " + e.getMessage(), -1073741267), e);
                }
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
            try {
                if (-1073741536 == response.header.status) {
                    Buffer structBuffer = new Buffer(new byte[64], 0, 64);
                    server.transport.receiveEnd(structBuffer);
                    server.transport.addToSpoolerQueue(match.consumer, new SmbException(response.header.status), 0, new FileNotifyInformation[0]);
                    File file = fileDir;
                    file.getClass();
                    fileDir.removeAidChangeNotifyQueueEntry(file.new File.AidObject(response.header.aid, null));
                    capture.capturePacketWriteEnd();
                    return;
                }
                Buffer structBuffer = new Buffer(new byte[64], 0, 64);
                if (0 == response.header.status) {
                    FileNotifyInformation[] fni;
                    if (null == response.buffer) {
                        server.transport.receiveBytes(structBuffer, 0, 6);
                        BufferReader bufferReader = new BufferReader(structBuffer.data, 0, false);
                        bufferReader.skip(2);
                        length = bufferReader.readInt4();
                        structBuffer = new Buffer((int)length);
                        server.transport.receiveBytes(structBuffer, 0, (int)length);
                        fni = FileNotifyInformation.extractFileNotifyInformation(structBuffer.data);
                    } else {
                        BufferReader bufferReader = new BufferReader(response.buffer, 0, false);
                        bufferReader.skip(2);
                        length = bufferReader.readInt4();
                        byte[] dataBuffer = new byte[(int)length];
                        System.arraycopy(bufferReader.getSrc(), bufferReader.getOffset(), dataBuffer, 0, (int)length);
                        fni = FileNotifyInformation.extractFileNotifyInformation(dataBuffer);
                    }
                    File file = fileDir;
                    file.getClass();
                    fileDir.removeAidChangeNotifyQueueEntry(file.new File.AidObject(response.header.aid, null));
                    server.transport.addToSpoolerQueue(match.consumer, status, count, fni);
                } else {
                    if (267 == response.header.status || 268 == response.header.status) {
                        server.transport.addToSpoolerQueue(match.consumer, new SmbException(response.header.status), 0, new FileNotifyInformation[0]);
                        File file = fileDir;
                        file.getClass();
                        fileDir.removeAidChangeNotifyQueueEntry(file.new File.AidObject(response.header.aid, null));
                        capture.capturePacketWriteEnd();
                        return;
                    }
                    server.transport.addToSpoolerQueue(match.consumer, new ClientException("Failed to receive successful status Notify response", response.header.status), 0, context);
                    File file = fileDir;
                    file.getClass();
                    fileDir.removeAidChangeNotifyQueueEntry(file.new File.AidObject(response.header.aid, null));
                }
            }
            catch (NetbiosException e) {
                Smb300.handleNetbiosException(status, match, "Failed to receive successful status Notify response");
            }
            capture.capturePacketWriteEnd();
        }
    }

    private static class ReadConsumer
    implements AsyncConsumer {
        private ReadConsumer() {
        }

        public void complete(Throwable status, long length, Object context) throws NqException {
            int READSTRUCT_SIZE = 14;
            int count = 0;
            byte offset = 0;
            ClientSmb.AsyncMatch match = (ClientSmb.AsyncMatch)context;
            Server server = match.server;
            Response response = (Response)match.response;
            Buffer buffer = new Buffer(64);
            if (-1073741309 == response.header.status || -1073740964 == response.header.status) {
                SmbException se = (SmbException)Utility.throwableInitCauseException(new SmbException(-1073741267), new SmbException(response.header.status));
                TraceLog.get().error("Send STATUS_RETRY with the cause: ", se.getCause(), 10, response.header.status);
                match.consumer.complete(se, 0L, match.context);
                return;
            }
            try {
                if (null != response.buffer) {
                    if (response.tailLen < 14) {
                        count = 0;
                    } else {
                        System.arraycopy(response.buffer, 0, buffer.data, 0, 14);
                        response.reader = new BufferReader(buffer.data, 0, false);
                        offset = response.reader.readByte();
                        response.reader.skip(1);
                        count = response.reader.readInt4();
                        System.arraycopy(response.buffer, 14 + (offset - 80), match.buffer.data, match.buffer.offset, count);
                    }
                } else if (response.tailLen < 14) {
                    server.transport.receiveBytes(buffer, 0, response.tailLen);
                    count = 0;
                } else if (14 == server.transport.receiveBytes(buffer, 0, 14)) {
                    response.reader = new BufferReader(buffer.data, 0, false);
                    if (0 == response.header.status) {
                        offset = response.reader.readByte();
                        response.reader.skip(1);
                        count = response.reader.readInt4();
                        offset = (byte)(offset - 80);
                        if (offset > 0) {
                            server.transport.receiveBytes(buffer, 0, offset);
                        }
                        server.transport.receiveBytes(match.buffer, match.buffer.offset, count);
                    }
                } else {
                    count = 0;
                }
                server.transport.receiveEnd(buffer);
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
            }
            catch (NetbiosException e) {
                Smb300.handleNetbiosException(status, match, "Failed to receive successful status Notify response");
            }
            server.transport.getCapture().capturePacketWriteEnd();
        }
    }

    private static class WriteConsumer
    implements AsyncConsumer {
        private WriteConsumer() {
        }

        public void complete(Throwable status, long length, Object context) throws NqException {
            ClientSmb.AsyncMatch match = (ClientSmb.AsyncMatch)context;
            Server server = match.server;
            Buffer buffer = new Buffer(20);
            Response response = (Response)match.response;
            int tailLen = server.transport.getReceivingRemain();
            if (-1073741309 == response.header.status || -1073740964 == response.header.status) {
                SmbException se = (SmbException)Utility.throwableInitCauseException(new SmbException(-1073741267), new SmbException(response.header.status));
                TraceLog.get().error("Send STATUS_RETRY with the cause: ", se.getCause(), 10, response.header.status);
                match.consumer.complete(se, 0L, match.context);
                return;
            }
            if (null != response.buffer) {
                System.arraycopy(response.buffer, 0, buffer.data, 0, response.tailLen > 20 ? 20 : response.tailLen);
            } else {
                try {
                    if (tailLen != server.transport.receiveBytes(buffer, 0, tailLen)) {
                        server.transport.getCapture().capturePacketWriteEnd();
                        match.consumer.complete(new ClientException("Failed to receive Write response", -106), 0L, match.context);
                        return;
                    }
                }
                catch (NetbiosException e) {
                    Smb300.handleNetbiosException(status, match, "Failed to receive Write response");
                    return;
                }
            }
            response.reader = new BufferReader(buffer.data, 0, false);
            if (0 == response.header.status) {
                response.reader.skip(2);
                length = response.reader.readInt4();
            }
            long currentTime = System.currentTimeMillis();
            if (-1073741267 == response.header.status) {
                match.consumer.complete(new SmbException(-1073741267), 0L, match.context);
            } else if (response.header.status != 0) {
                match.consumer.complete(new SmbException(response.header.status), 0L, match.context);
            } else if (match.timeCreated + match.timeout > currentTime) {
                match.consumer.complete(status, length, context);
            } else if (server.transport.isConnected()) {
                match.consumer.complete(new SmbException(-1073741267), 0L, match.context);
            } else {
                match.consumer.complete(new NqException("Request timed out", -19), length, context);
            }
            server.transport.getCapture().capturePacketWriteEnd();
        }
    }

    protected static class Command {
        int requestBufferSize;
        int requestStructSize;
        int responseStructSize;
        AsyncConsumer consumer;
        Smb200.NotifyConsumer notifyConsumer;

        Command(int requestBufferSize, int requestStructSize, int responseStructSize, AsyncConsumer consumer, Smb200.NotifyConsumer notifyConsumer) {
            this.requestBufferSize = requestBufferSize;
            this.requestStructSize = requestStructSize;
            this.responseStructSize = responseStructSize;
            this.consumer = consumer;
            this.notifyConsumer = notifyConsumer;
        }
    }
}

