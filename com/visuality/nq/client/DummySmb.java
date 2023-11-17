/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.client.AsyncConsumer;
import com.visuality.nq.client.ClientSmb;
import com.visuality.nq.client.Directory;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Request;
import com.visuality.nq.client.Server;
import com.visuality.nq.client.Share;
import com.visuality.nq.client.Transport;
import com.visuality.nq.client.User;
import com.visuality.nq.common.Blob;
import com.visuality.nq.common.Buffer;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SecurityDescriptor;
import com.visuality.nq.common.TraceLog;

public class DummySmb
extends ClientSmb {
    private ClientSmb master;

    public ClientSmb getDialect() {
        return this.master;
    }

    public void setDialect(ClientSmb smb) {
        this.master = smb;
    }

    public void response(Transport transport) throws NqException {
        TraceLog.get().enter(transport, 700);
        TraceLog.get().exit(700);
    }

    public Object allocateContext(Server server) {
        if (this.syncAndCheckForDummySmb(server)) {
            return null;
        }
        return server.smb.allocateContext(server);
    }

    public void freeContext(Object context, Server server) {
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.freeContext(context, server);
    }

    public int doNegotiate(Server server, Blob blob, short[] dialects) throws NqException {
        if (this.syncAndCheckForDummySmb(server)) {
            return -1073741254;
        }
        return server.smb.doNegotiate(server, blob, dialects);
    }

    public int doSessionSetup(User user, Blob outBlob, Blob inBlob) throws NqException {
        Server server = user.getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return -1073741254;
        }
        return server.smb.doSessionSetup(user, outBlob, inBlob);
    }

    public int doSessionSetupExtended(User user, Blob outBlob, Blob inBlob) throws NqException {
        Server server = user.getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return -1073741254;
        }
        return server.smb.doSessionSetupExtended(user, outBlob, inBlob);
    }

    public void doLogOff(User user) throws NqException {
        Server server = user.getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.doLogOff(user);
    }

    public void doTreeConnect(Share share) throws NqException {
        Server server = share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.doTreeConnect(share);
    }

    public void doTreeDisconnect(Share share) throws NqException {
        Server server = share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.doTreeDisconnect(share);
    }

    public void doCreate(File file) throws NqException {
        Server server = file.share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.doCreate(file);
    }

    public void doRestoreHandle(File file) throws NqException {
        Server server = file.share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.doRestoreHandle(file);
    }

    public int doClose(File file) throws NqException {
        Server server = file.share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return -1073741504;
        }
        return server.smb.doClose(file);
    }

    public void doChangeNotify(File dir, int completionFilter, AsyncConsumer consumer) throws NqException {
        Server server = dir.server;
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.doChangeNotify(dir, completionFilter, consumer);
    }

    public void doCancel(File file) throws NqException {
        Server server = file.server;
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.doCancel(file);
    }

    public void doQueryDfsReferrals(Share share, String path, ClientSmb.ParseReferral parser) throws NqException {
        Server server = share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.doQueryDfsReferrals(share, path, parser);
    }

    public SecurityDescriptor doQuerySecurityDescriptor(File file) throws NqException {
        Server server = file.share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return null;
        }
        return server.smb.doQuerySecurityDescriptor(file);
    }

    public void doSetSecurityDescriptor(File file, SecurityDescriptor sd) throws NqException {
        Server server = file.share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.doSetSecurityDescriptor(file, sd);
    }

    public void doQueryFsInfo(Share share) throws NqException {
        Server server = share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.doQueryFsInfo(share);
    }

    public File.Info doQueryFileInfoByName(Share share, String fileName) throws NqException {
        Server server = share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return null;
        }
        return server.smb.doQueryFileInfoByName(share, fileName);
    }

    public File.Info doQueryFileInfoByHandle(File file) throws NqException {
        Server server = file.share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return null;
        }
        return server.smb.doQueryFileInfoByHandle(file);
    }

    public void doSetFileAttributes(File file, int attributes) throws NqException {
        Server server = file.share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.doSetFileAttributes(file, attributes);
    }

    public void doSetFileSize(File file, long size) throws NqException {
        Server server = file.share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.doSetFileSize(file, size);
    }

    public void doSetFileTime(File file, long creationTime, long lastAccessTime, long lastWriteTime) throws NqException {
        Server server = file.share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.doSetFileTime(file, creationTime, lastAccessTime, lastWriteTime);
    }

    public void doSetFileDeleteOnClose(File file) throws NqException {
        Server server = file.share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.doSetFileDeleteOnClose(file);
    }

    public void doRename(File file, String newName, boolean overwriteExistingFile) throws NqException {
        Server server = file.share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.doRename(file, newName, overwriteExistingFile);
    }

    public void doRename(File file, String newName) throws NqException {
        Server server = file.share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.doRename(file, newName);
    }

    public void doFlush(File file) throws NqException {
        Server server = file.share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.doFlush(file);
    }

    public int doRapTransaction(Share share, Buffer inData, Buffer outParams, Buffer outData) throws NqException {
        Server server = share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return -1073741504;
        }
        return server.smb.doRapTransaction(share, inData, outParams, outData);
    }

    public boolean doEcho(Share share) throws NqException {
        Server server = share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return false;
        }
        return server.smb.doEcho(share);
    }

    public File.ResumeKey doQueryResumeFileKey(File srcFile) throws NqException {
        Server server = srcFile.share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return null;
        }
        return server.smb.doQueryResumeFileKey(srcFile);
    }

    public File.ChunksStatus doServerSideDataCopy(File dstFile, boolean readAccess, File.ResumeKey srcFileKey, File.Chunk[] chunks) throws NqException {
        Server server = dstFile.share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return null;
        }
        return server.smb.doServerSideDataCopy(dstFile, readAccess, srcFileKey, chunks);
    }

    protected int sendRequest(Server server, User user, Object request, ClientSmb.Match match) throws NqException {
        if (this.syncAndCheckForDummySmb(server)) {
            return -1073741504;
        }
        return server.smb.sendRequest(server, user, request, match);
    }

    protected int sendReceive(Server server, User user, Object request, Object response) throws NqException {
        if (this.syncAndCheckForDummySmb(server)) {
            return -1073741504;
        }
        return server.smb.sendReceive(server, user, request, response);
    }

    protected void keyDerivation(User user) {
        Server server = user.getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.keyDerivation(user);
    }

    public void signalAllMatch(Transport transport) {
        TraceLog.get().enter(transport, 700);
        TraceLog.get().exit(700);
    }

    public void handleWaitingNotifyResponses(Server server, File file) throws NqException {
    }

    public boolean doValidateNegotiate(Server server, User user, Share share, short[] dialects) throws NqException {
        if (this.syncAndCheckForDummySmb(server)) {
            return false;
        }
        return server.smb.doValidateNegotiate(server, user, share, dialects);
    }

    public boolean removeReadWriteMatch(Object context, Server server, boolean isReadMatch) throws NqException {
        if (this.syncAndCheckForDummySmb(server)) {
            return false;
        }
        return server.smb.removeReadWriteMatch(context, server, isReadMatch);
    }

    public void doFindOpen(Directory search) throws NqException {
        Server server = search.server;
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.doFindOpen(search);
    }

    public boolean doFindMore(Directory search) throws NqException {
        Server server = search.server;
        if (this.syncAndCheckForDummySmb(server)) {
            return false;
        }
        return server.smb.doFindMore(search);
    }

    public int doFindClose(Directory search) throws NqException {
        Server server = search.server;
        if (this.syncAndCheckForDummySmb(server)) {
            return -1073741504;
        }
        return server.smb.doFindClose(search);
    }

    public void doWrite(File file, Buffer buffer, AsyncConsumer callback, Object context, Object hook) throws NqException {
        Server server = file.share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.doWrite(file, buffer, callback, context, hook);
    }

    public void doRead(File file, Buffer buffer, AsyncConsumer callback, Object context, Object hook) throws NqException {
        Server server = file.share.getUser().getServer();
        if (this.syncAndCheckForDummySmb(server)) {
            return;
        }
        server.smb.doRead(file, buffer, callback, context, hook);
    }

    void lockAndUnlock(Server server) {
        Server.lock(server);
        Server.releaseLock(server);
    }

    boolean syncAndCheckForDummySmb(Server server) {
        this.lockAndUnlock(server);
        return server.smb instanceof DummySmb;
    }

    public Request prepareSingleRequestByShare(Request request, Share share, short command, int dataLen) throws NqException {
        return null;
    }

    public Request prepareSingleRequest(Server server, User user, Request request, short command) throws NqException {
        return null;
    }
}

