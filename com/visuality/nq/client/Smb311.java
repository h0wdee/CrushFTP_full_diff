/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.auth.Sha256;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Request;
import com.visuality.nq.client.Response;
import com.visuality.nq.client.Server;
import com.visuality.nq.client.Share;
import com.visuality.nq.client.Smb200;
import com.visuality.nq.client.Smb300;
import com.visuality.nq.client.User;
import com.visuality.nq.common.Buffer;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SecurityDescriptor;
import com.visuality.nq.common.Smb2Header;
import com.visuality.nq.common.Smb2Params;
import com.visuality.nq.common.SmbException;
import com.visuality.nq.common.Utility;

public class Smb311
extends Smb300 {
    public Smb311() {
        this.restrictCrypters = true;
        this.setRevision((short)785);
    }

    protected void keyDerivation(User user) {
        if (null != user.macSessionKey.data) {
            if (user.macSessionKey.len > user.getServer().getSmb().maxSigningKeyLen) {
                user.macSessionKey.len = user.getServer().getSmb().maxSigningKeyLen;
            }
            user.encryptionKey = new byte[16];
            user.decryptionKey = new byte[16];
            user.applicationKey = new byte[16];
            Sha256.keyDerivation(user.macSessionKey.data, user.encryptionKey.length, "SMBS2CCipherKey\u0000".getBytes(), 16, user.preauthIntegHashVal, 64, user.encryptionKey);
            Sha256.keyDerivation(user.macSessionKey.data, user.decryptionKey.length, "SMBC2SCipherKey\u0000".getBytes(), 16, user.preauthIntegHashVal, 64, user.decryptionKey);
            Sha256.keyDerivation(user.macSessionKey.data, user.applicationKey.length, "SMBAppKey\u0000".getBytes(), 10, user.preauthIntegHashVal, 64, user.applicationKey);
            Sha256.keyDerivation(user.macSessionKey.data, user.macSessionKey.len, "SMBSigningKey\u0000".getBytes(), 14, user.preauthIntegHashVal, 64, user.macSessionKey.data);
        }
    }

    public boolean doValidateNegotiate(Server server, User user, Share share, short[] dialects) {
        return true;
    }

    public static boolean writeHeader(Request request) {
        Smb200.writeHeader(request);
        return true;
    }

    private static short calculateCreditCharge(Server server, int requestLength) {
        return (short)(requestLength > 0 ? 1 + (requestLength - 1) / 65536 : 1);
    }

    public Request prepareSingleRequest(Server server, User user, Request request, short command) {
        BufferWriter writer;
        request.buffer = Buffer.getNewBuffer(Smb311.commandDescriptors[command].requestBufferSize + 64 + 4);
        request.command = command;
        request.tail = new Buffer();
        request.encrypt = null == user ? false : user.isEncrypted && !user.isAnonymous;
        request.writer = writer = new BufferWriter(request.buffer.data, 0, false);
        writer.skip(4);
        request.header = new Smb2Header();
        request.header.protocolId = Smb2Params.smb2ProtocolId;
        request.header.size = (short)64;
        request.header.creditCharge = 0;
        request.header.command = command;
        request.header.credits = 1;
        request.header.pid = Utility.getPid();
        if (null != server && 0 != (server.capabilities & 8)) {
            request.header.creditCharge = 1;
        }
        request.header.sid = null == user ? 0L : user.uid;
        request.header.flags = (short)(command != 1 && null != server && server.mustUseSignatures() && null != user && user.useSignatures() ? 8 : 0 | (null != server && 0 != (server.capabilities & 2) ? 0x10000000 : 0));
        return request;
    }

    public Request prepareSingleRequestByShare(Request request, Share share, short command, int dataLen) {
        if (null == this.prepareSingleRequest(share.getUser().getServer(), share.getUser(), request, command)) {
            return null;
        }
        boolean bl = request.encrypt = share.getUser().isEncrypted ? true : share.encrypt;
        if (0 == (share.flags & 1)) {
            request.header.flags &= 0xEFFFFFFF;
        }
        if (0 != (share.getUser().getServer().capabilities & 8)) {
            switch (command) {
                case 8: 
                case 9: 
                case 14: {
                    request.header.creditCharge = Smb311.calculateCreditCharge(share.getUser().getServer(), dataLen);
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
        return request;
    }

    private void writeQueryInfoRequest(Request request, File file, byte infoType, byte infoClass, int maxResLen, int addInfo) throws NqException {
        Server server = file.share.getUser().getServer();
        server.smb.prepareSingleRequestByShare(request, file.share, (short)16, 0);
        Smb311.writeHeader(request);
        request.writer.writeByte(infoType);
        request.writer.writeByte(infoClass);
        request.writer.writeInt4(maxResLen);
        request.writer.writeInt2(0);
        request.writer.writeInt2(0);
        request.writer.writeInt4(0);
        request.writer.writeInt4(addInfo);
        request.writer.writeInt4(0);
        request.writer.writeBytes(file.fid, 16);
    }

    public SecurityDescriptor doQuerySecurityDescriptor(File file) throws NqException {
        Request request = new Request();
        Response response = new Response();
        Server server = file.share.getUser().getServer();
        this.writeQueryInfoRequest(request, file, (byte)3, (byte)0, 0, 7);
        request.tail = new Buffer();
        int res = server.smb.sendReceive(server, file.share.getUser(), request, response);
        if (-1073741789 != res) {
            throw new SmbException("Get security descriptor error", res);
        }
        byte errorContextCount = response.reader.readByte();
        response.reader.skip(1);
        if (0 < errorContextCount) {
            response.reader.skip(4);
            response.reader.skip(4);
            response.reader.skip(4);
        } else {
            response.reader.skip(4);
        }
        int responseSize = response.reader.readInt4();
        response.buffer = null;
        this.writeQueryInfoRequest(request, file, (byte)3, (byte)0, responseSize, 7);
        res = server.smb.sendReceive(server, file.share.getUser(), request, response);
        if (0 != res) {
            throw new SmbException("Get security descriptor error", res);
        }
        response.reader.skip(2);
        response.reader.readInt4();
        SecurityDescriptor sd = new SecurityDescriptor(response.reader);
        return sd;
    }
}

