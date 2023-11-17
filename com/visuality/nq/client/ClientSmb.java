/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.client.AsyncConsumer;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.ClientException;
import com.visuality.nq.client.Directory;
import com.visuality.nq.client.DummySmb;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Request;
import com.visuality.nq.client.Response;
import com.visuality.nq.client.Server;
import com.visuality.nq.client.Share;
import com.visuality.nq.client.Smb100;
import com.visuality.nq.client.Smb200;
import com.visuality.nq.client.Smb300;
import com.visuality.nq.client.Smb311;
import com.visuality.nq.client.Transport;
import com.visuality.nq.client.User;
import com.visuality.nq.common.Blob;
import com.visuality.nq.common.Buffer;
import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SecurityDescriptor;
import com.visuality.nq.common.SmbException;
import com.visuality.nq.common.SyncObject;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.resolve.NetbiosException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public abstract class ClientSmb
implements Transport.ResponseCallback {
    public static final int SIGNING_ENABLED = 1;
    public static final int SIGNING_REQUIRED = 2;
    static ClientSmb defaultSmb;
    static ClientSmb dummySmb;
    private String name;
    private short revision;
    protected boolean solo;
    public int maxSigningKeyLen = 16;
    public boolean restrictCrypters;
    public String rpcNamePrefix;
    public boolean useFullPath = false;
    public boolean createBeforeMove = true;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setRevision(short revision) {
        this.revision = revision;
    }

    public short getRevision() {
        return this.revision;
    }

    protected static ClientSmb getDefaultSmb(int minDialect) {
        try {
            defaultSmb = -1 == minDialect ? (Client.getDialects().hasSmb((short)256) ? new Smb100() : new Smb200()) : ClientSmb.fetchClientSmbInstance(minDialect);
        }
        catch (NqException e) {
            defaultSmb = new Smb100();
        }
        return defaultSmb;
    }

    public static ClientSmb getDummySmb() {
        return dummySmb;
    }

    public synchronized void setSolo(boolean solo) {
        this.solo = solo;
    }

    public abstract Object allocateContext(Server var1);

    public abstract void freeContext(Object var1, Server var2);

    public abstract int doNegotiate(Server var1, Blob var2, short[] var3) throws NqException;

    public abstract int doSessionSetup(User var1, Blob var2, Blob var3) throws NqException;

    public abstract int doSessionSetupExtended(User var1, Blob var2, Blob var3) throws NqException;

    public abstract void doLogOff(User var1) throws NqException;

    public abstract void doTreeConnect(Share var1) throws NqException;

    public abstract void doTreeDisconnect(Share var1) throws NqException;

    public abstract void doCreate(File var1) throws NqException;

    public abstract void doRestoreHandle(File var1) throws NqException;

    protected abstract void doChangeNotify(File var1, int var2, AsyncConsumer var3) throws NqException;

    protected abstract void doCancel(File var1) throws NqException;

    public abstract int doClose(File var1) throws NqException;

    public abstract void doQueryDfsReferrals(Share var1, String var2, ParseReferral var3) throws NqException;

    public abstract void doFindOpen(Directory var1) throws NqException;

    public abstract boolean doFindMore(Directory var1) throws NqException;

    public abstract int doFindClose(Directory var1) throws NqException;

    public abstract void doWrite(File var1, Buffer var2, AsyncConsumer var3, Object var4, Object var5) throws NqException;

    public abstract void doRead(File var1, Buffer var2, AsyncConsumer var3, Object var4, Object var5) throws NqException;

    public abstract SecurityDescriptor doQuerySecurityDescriptor(File var1) throws NqException;

    public abstract void doSetSecurityDescriptor(File var1, SecurityDescriptor var2) throws NqException;

    public abstract void doQueryFsInfo(Share var1) throws NqException;

    public abstract File.Info doQueryFileInfoByName(Share var1, String var2) throws NqException;

    public abstract File.Info doQueryFileInfoByHandle(File var1) throws NqException;

    public abstract void doSetFileAttributes(File var1, int var2) throws NqException;

    public abstract void doSetFileSize(File var1, long var2) throws NqException;

    public abstract void doSetFileTime(File var1, long var2, long var4, long var6) throws NqException;

    public abstract void doSetFileDeleteOnClose(File var1) throws NqException;

    public abstract void doRename(File var1, String var2) throws NqException;

    public abstract void doRename(File var1, String var2, boolean var3) throws NqException;

    public abstract void doFlush(File var1) throws NqException;

    public abstract int doRapTransaction(Share var1, Buffer var2, Buffer var3, Buffer var4) throws NqException;

    public abstract boolean doEcho(Share var1) throws NqException;

    public abstract File.ResumeKey doQueryResumeFileKey(File var1) throws NqException;

    public abstract File.ChunksStatus doServerSideDataCopy(File var1, boolean var2, File.ResumeKey var3, File.Chunk[] var4) throws NqException;

    protected abstract int sendRequest(Server var1, User var2, Object var3, Match var4) throws NqException;

    protected abstract int sendReceive(Server var1, User var2, Object var3, Object var4) throws NqException;

    protected abstract void keyDerivation(User var1);

    public abstract void signalAllMatch(Transport var1);

    public abstract void handleWaitingNotifyResponses(Server var1, File var2) throws NqException;

    public abstract boolean doValidateNegotiate(Server var1, User var2, Share var3, short[] var4) throws NqException;

    public abstract boolean removeReadWriteMatch(Object var1, Server var2, boolean var3) throws NqException;

    public abstract Request prepareSingleRequestByShare(Request var1, Share var2, short var3, int var4) throws NqException;

    public abstract Request prepareSingleRequest(Server var1, User var2, Request var3, short var4) throws NqException;

    protected void handleTimeout(Server server, Request request, Response response, Match match) throws NqException, SmbException, ClientException {
        TraceLog.get().enter(250);
        if (null != match && server.transport.isConnected()) {
            TraceLog.get().message("Calling match.notify for mid=" + match.mid, 2000);
            this.responseMatchNotify(match);
            TraceLog.get().exit(250);
            return;
        }
        server.smb.signalAllMatch(server.transport);
        if (!(server.transport.isConnected() && null != response.buffer || 0 == request.command || 1 == request.command || 2 == request.command || 4 == request.command)) {
            try {
                if (!server.reconnect()) {
                    TraceLog.get().exit(250);
                    throw new ClientException("Unable to restore connection", -1073741267);
                }
                TraceLog.get().exit(250);
                throw new SmbException("Connection timed out", -1073741267);
            }
            catch (NetbiosException ex) {
                TraceLog.get().exit(250);
                throw new ClientException("Unable to reconnect: " + ex.getMessage(), ex.getErrCode());
            }
        }
        try {
            server.transport.hardDisconnect();
        }
        catch (IOException e1) {
            // empty catch block
        }
        TraceLog.get().exit(250);
        throw new SmbException("Connection timed out", -1073741267);
    }

    protected static ClientSmb fetchClientSmbInstance(int dialectVersion) {
        ClientSmb dialect = null;
        if (256 == dialectVersion) {
            dialect = new Smb100();
        } else if (514 == dialectVersion || 528 == dialectVersion) {
            dialect = new Smb200();
        } else if (768 == dialectVersion || 770 == dialectVersion) {
            dialect = new Smb300();
        } else if (785 == dialectVersion) {
            dialect = new Smb311();
        }
        return dialect;
    }

    protected void responseMatchNotify(Match match) {
        TraceLog.get().message("Calling match.notify for mid=" + match.mid, 300);
        match.syncObj.syncNotify();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void responseNotConnected(Server server) throws NqException {
        TraceLog.get().enter(300);
        HashMap hashMap = server.expectedResponses;
        synchronized (hashMap) {
            Iterator iterator = server.expectedResponses.entrySet().iterator();
            while (iterator.hasNext()) {
                Match match;
                Object next = iterator.next().getValue();
                if (next instanceof AsyncMatch) {
                    match = (AsyncMatch)next;
                    File.CummulativeAsynConsumer asyncConsumer = (File.CummulativeAsynConsumer)match.context;
                    asyncConsumer.complete(new SmbException("Failed to receive response", -1073741267), 0L, match.context);
                    File file = match.file;
                    if (null == file) continue;
                    while (file.isDataInAidChangeNotifyQueue()) {
                        File.AidObject aidObject = file.retrieveAidChangeNotifyQueueEntry();
                        if (null == aidObject || null == aidObject.consumer) continue;
                        aidObject.consumer.complete(new SmbException(267), 0L, null);
                        file.removeAidChangeNotifyQueueEntry(aidObject);
                    }
                    continue;
                }
                if (!(next instanceof Match)) continue;
                match = (Match)next;
                SyncObject syncObject = match.syncObj;
                synchronized (syncObject) {
                    TraceLog.get().message("Notify initiator of transaction", 300);
                    this.responseMatchNotify(match);
                    match.matchExtraInfo |= 4;
                }
                if (!match.isResponseAllocated) continue;
                match.response = null;
            }
            server.expectedResponses.clear();
        }
        server.transport.receiveAll();
        TraceLog.get().exit(300);
    }

    protected void responseUserSessionDeletedOrExpired(Server server, Match match) {
        TraceLog.get().enter("user session deleted or user session expired was received", 300);
        try {
            server.transport.hardDisconnect(false);
        }
        catch (IOException e) {
            TraceLog.get().message("Exception doing a hardDisconnect = ", e, 2000);
        }
        match.status = -1073741267;
        TraceLog.get().exit(300);
    }

    static {
        try {
            defaultSmb = Client.getDialects().hasSmb((short)256) ? new Smb100() : new Smb200();
        }
        catch (NqException e) {
            defaultSmb = new Smb100();
        }
        dummySmb = new DummySmb();
    }

    abstract class Command {
        short requestBufferSize;

        Command() {
        }

        abstract void callback(Server var1, Match var2);
    }

    class AsyncMatch
    extends Match {
        public AsyncConsumer consumer;
        public Object context;
        public Object hook;
        public long timeCreated;
        public long timeout;
        public Buffer buffer;
        public File file;

        AsyncMatch() {
        }
    }

    protected class Match {
        public static final int MATCHINFO_NONE = 0;
        public static final int MATCHINFO_WRITE = 1;
        public static final int MATCHINFO_READ = 2;
        public static final int MATCHINFO_WASSIGNALED = 4;
        Object response;
        Server server;
        long mid;
        byte[] hdrBuf = new byte[66];
        int creditCharge = 0;
        long userId;
        boolean isResponseAllocated;
        int matchExtraInfo;
        int status;
        public final SyncObject syncObj = new SyncObject();

        protected Match() {
        }
    }

    public static interface ParseReferral {
        public void parse(BufferReader var1) throws NqException;
    }
}

