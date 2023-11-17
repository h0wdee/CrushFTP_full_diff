/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.AbstractFile;
import com.visuality.nq.client.ClientException;
import com.visuality.nq.client.ClientUtils;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Mount;
import com.visuality.nq.client.MountParams;
import com.visuality.nq.client.Server;
import com.visuality.nq.client.User;
import com.visuality.nq.client.dfs.Dfs;
import com.visuality.nq.client.rpc.Srvsvc;
import com.visuality.nq.common.Item;
import com.visuality.nq.common.NamedRepository;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SecurityDescriptor;
import com.visuality.nq.common.SmbException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.UUID;
import com.visuality.nq.common.Utility;
import com.visuality.nq.config.Config;
import java.util.Iterator;

public class Share
extends Item {
    public static final int TYPE_FILE = 0;
    public static final int TYPE_PRINTER = 1;
    public static final int TYPE_DEVICE = 2;
    public static final int TYPE_IPC = 3;
    protected static final int FLAG_DFS = 1;
    protected static final int CC_SHARE_IN_DFS = 1;
    protected static final int CC_SHARE_SCALEOUT = 2;
    protected static final int CC_SHARE_DFS_ROOT = 4;
    protected NamedRepository files = new NamedRepository();
    protected NamedRepository searches = new NamedRepository();
    protected boolean connected;
    protected boolean isIpc;
    protected boolean isPrinter;
    protected int mountsRelated = 0;
    protected Share dfsReferral;
    protected User user;
    protected int flags;
    protected int rawFlags;
    protected int tid;
    protected int capabilities;
    protected int access;
    public Info info = new Info();
    protected boolean encrypt;

    protected Share(User user, String name) {
        super(name);
        this.user = user;
        this.info.name = name.toLowerCase();
        this.connected = false;
        this.files.clear();
        this.searches.clear();
        this.dfsReferral = null;
    }

    protected void dump() {
    }

    protected void unlockCallback() throws NqException {
        Share.disconnect(this);
    }

    protected void shutdown() throws NqException {
        for (File file : this.files.values()) {
            file.setDisconnected(true);
        }
        for (Item search : this.searches.values()) {
            int numOfLocks = search.getLocks();
            for (int lockCntr = 0; lockCntr < numOfLocks; ++lockCntr) {
                search.unlock();
            }
        }
        if (this.connected && null != this.user) {
            Server server = this.user.getServer();
            this.connected = false;
            if (null != server) {
                server.smb.doTreeDisconnect(this);
            }
        }
    }

    protected Iterator iterateFiles() {
        return this.files.values().iterator();
    }

    protected boolean reopenFiles() throws NqException {
        boolean res = true;
        if (this.isIpc) {
            return false;
        }
        Iterator shareIterator = this.files.values().iterator();
        while (shareIterator.hasNext()) {
            File file = (File)shareIterator.next();
            while (file.isDataInAidChangeNotifyQueue()) {
                File.AidObject aidObject = file.retrieveAidChangeNotifyQueueEntry();
                if (null == aidObject) continue;
                aidObject.consumer.complete(new SmbException(267), 0L, null);
                file.removeAidChangeNotifyQueueEntry(aidObject);
            }
            if (file.restore()) continue;
            res = false;
            shareIterator.remove();
        }
        return res;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    protected static Share connect(User user, String path, Mount mount, Credentials credentials, boolean doDfs, MountParams mountParams) throws NqException {
        block39: {
            TraceLog.get().enter("path = " + path + "; doDfs = " + doDfs + "; mountParams = ", mountParams, 700);
            TraceLog.get().message(user, 2000);
            TraceLog.get().message(mount, 2000);
            server = user.getServer();
            share = null;
            Server.lock(server);
            try {
                shareItr = user.shares.values().iterator();
                pathLowerCase = path.toLowerCase();
                while (shareItr.hasNext()) {
                    shareTmp = (Share)shareItr.next();
                    if (!pathLowerCase.equals(shareTmp.info.name)) continue;
                    share = shareTmp;
                    break;
                }
                if (null == share) {
                    share = new Share(user, path);
                }
                if (path.equals("IPC$")) {
                    share.isIpc = true;
                }
                if (!share.connected) ** GOTO lbl44
                try {
                    if (!share.echo(mount, doDfs) && !server.reconnect(doDfs)) {
                        Share.disconnect(share);
                        TraceLog.get().exit("Returning null", 700);
                        shareTmp = null;
                        return shareTmp;
                    }
                    if (!(share.isIpc || share.isPrinter || share.isDfs())) {
                        share.checkConnection(mount, doDfs);
                    }
                    TraceLog.get().exit("Share = ", share, 700);
                    shareTmp = share;
                    return shareTmp;
                }
                catch (SmbException e) {
                    block40: {
                        block37: {
                            if (-1073741267 == e.getErrCode()) {
                                TraceLog.get().caught(e);
                                throw e;
                            }
                            if (-1073741623 == e.getErrCode()) {
                                share.connected = false;
                                user.shares.remove(share.getName());
                            } else {
                                share.connected = false;
                                TraceLog.get().error("Exception = ", e);
                                throw new ClientException("Connected share fail, server reconnect fail (" + e.getErrCode() + ") : " + e.getMessage(), -101);
                            }
lbl44:
                            // 2 sources

                            isDfsEnabled = true;
                            try {
                                isDfsEnabled = Config.jnq.getBool("DFSENABLE");
                            }
                            catch (NqException e) {
                                // empty catch block
                            }
                            dfs = new Dfs(50);
                            try {
                                block38: {
                                    if (!isDfsEnabled || !doDfs || share.isIpc || share.isPrinter()) break block37;
                                    try {
                                        dfsRes = dfs.resolvePath(mount, mountParams, share, null, server);
                                    }
                                    catch (NqException e) {
                                        status = e.getErrCode();
                                        if (Dfs.isSkipReferralError(status)) {
                                            dfsRes = null;
                                            break block38;
                                        }
                                        TraceLog.get().error("Dfs error = ", e, 10, e.getErrCode());
                                        ce = new ClientException("Dfs error", e.getErrCode());
                                        ce.initCause(e);
                                        throw e;
                                    }
                                }
                                if (null != dfsRes && null != dfsRes.path && null != dfsRes.share) {
                                    if (!dfs.referral.isGood) {
                                        dfs.setIsGood(true);
                                        dfs.referral.isGood = true;
                                    }
                                    share.getUser().logoff();
                                    share = dfsRes.share;
                                    server.disconnect();
                                    server = dfsRes.share.getUser().getServer();
                                    dfsRes.path = ClientUtils.filePathStripNull(dfsRes.path);
                                    share.dfsReferral = dfsRes.share;
                                }
                            }
                            catch (ClientException e) {
                                if (-1073741790 != e.getErrCode()) break block37;
                                TraceLog.get().error("DFS was attempted, but got access-related error : ", e, 10, e.getErrCode());
                                throw new ClientException("DFS was attempted, but got access-related error : " + e.getMessage(), -105);
                            }
                        }
                        if (share.connected) break block39;
                        if (isDfsEnabled && doDfs && -1073741790 == dfs.lastError) {
                            TraceLog.get().error("DFS was attempted, but got access-related error");
                            throw new ClientException("DFS was attempted, but got access-related error", -105);
                        }
                        try {
                            server.smb.doTreeConnect(share);
                        }
                        catch (NqException e) {
                            TraceLog.get().error("Connection refused = ", e, 10, e.getErrCode());
                            throw (NqException)Utility.throwableInitCauseException(new NqException("Connection refused : " + e.getMessage() + ", error code = " + e.getErrCode() + ", for share = " + share, -24), e);
                        }
                        try {
                            if (share.isFile() && !share.isIpc) {
                                server.smb.doQueryFsInfo(share);
                            }
                        }
                        catch (NqException e) {
                            TraceLog.get().caught(e, 2000);
                            if (mountParams.enableAccessDeniedUpdater) {
                                TraceLog.get().message("Access denied for " + share.getName(), 10);
                                throw new SmbException("Access denied for " + share.getName(), -1073741790);
                            }
                            TraceLog.get().error("NqException = ", e, 10, e.getErrCode());
                            if (e.getErrCode() != -1073741202) break block40;
                            throw new SmbException("Access denied for " + share.getName(), -1073741202);
                        }
                    }
                    share.connected = true;
                    user.shares.put(share.getName().toLowerCase(), share);
                    if (share.isIpc || null != share.info.sd) break block39;
                    pipe = null;
                    try {
                        pipe = new Srvsvc(credentials, server);
                        info = pipe.shareGetInfo(path);
                        share.info.sd = info.sd;
                        share.info.type = info.type;
                        share.info.permissions = info.permissions;
                        share.info.maxusers = info.maxusers;
                        share.info.name = info.name.toLowerCase();
                        share.info.comment = info.comment;
                        share.info.path = info.path;
                    }
                    catch (Exception e) {
                        TraceLog.get().error("Failed to get the share info, Exception = ", e);
                        share.info.name = path.toLowerCase();
                    }
                    finally {
                        if (null != pipe) {
                            pipe.close();
                        }
                    }
                }
            }
            finally {
                Server.releaseLock(server);
            }
        }
        TraceLog.get().exit(700);
        return share;
    }

    private boolean echo(Mount mount, boolean doDfs) throws NqException {
        TraceLog.get().enter("doDfs = " + doDfs + "; mount = ", mount, 700);
        boolean res = false;
        Server server = this.user.getServer();
        int localTimeout = Config.jnq.getInt("RETRYTIMEOUT");
        int retryCount = Config.jnq.getInt("RETRYCOUNT");
        if (null != mount) {
            localTimeout = mount.getRetryTimeout();
            retryCount = mount.getRetryCount();
        }
        for (int i = retryCount; i > 0; --i) {
            try {
                res = server.smb.doEcho(this);
                break;
            }
            catch (NqException e) {
                int status = e.getErrCode();
                if (-1073741267 == status) {
                    if (Mount.checkForSessionDeletedOrExpired(e)) {
                        TraceLog.get().caught(e, 700);
                        throw e;
                    }
                    if (!server.reconnect(doDfs)) {
                        TraceLog.get().error("Unable to reconnect", 700, 0);
                        throw new ClientException("Unable to reconnect", -111);
                    }
                    if (retryCount == i) continue;
                    if (i == AbstractFile.END_OF_LOOP) {
                        TraceLog.get().error("Operation failed", 700, 0);
                        throw new SmbException("Operation failed", -1073741267);
                    }
                    Utility.waitABit(localTimeout);
                    localTimeout *= 2;
                    continue;
                }
                TraceLog.get().caught(e);
                throw e;
            }
        }
        TraceLog.get().exit(700);
        return res;
    }

    public static Share connectShareInternally(String path, Mount mount, MountParams mountParams, Credentials credentials, boolean doDfs, Server originalServer, Share originalShare) throws NqException {
        TraceLog.get().enter("path=" + path + "; doDfs=" + doDfs, 700);
        TraceLog.get().message(mount, 2000);
        Server server = null;
        Share share = null;
        boolean isExceptionTriggered = false;
        int lockCounter = 0;
        int updaterCounter = 1;
        boolean isMountUsingCredentialsUpdater = true;
        String serverName = ClientUtils.hostNameFromRemotePath(path).toLowerCase();
        String shareName = ClientUtils.shareNameFromRemotePath(path);
        TraceLog.get().message("serverName = " + serverName + ", shareName = " + shareName, 2000);
        if (null == originalServer && null != mount) {
            originalServer = mount.getServer();
        }
        NqException savedException = null;
        while (isMountUsingCredentialsUpdater) {
            isMountUsingCredentialsUpdater = false;
            isExceptionTriggered = false;
            try {
                server = Server.findOrCreate(serverName, true, null);
                if (null == server) {
                    isExceptionTriggered = true;
                    TraceLog.get().exit("Server is not connected", 700);
                    throw new ClientException("Server is not connected", -102);
                }
                if (null != originalServer && 0 != Server.lockCount(originalServer) && !server.getName().toLowerCase().equals(originalServer.getName().toLowerCase())) {
                    TraceLog.get().message("originalServer = " + originalServer.getName() + ", server = " + server.getName(), 2000);
                    lockCounter = Server.releaseAllLocks(originalServer);
                }
                if (!server.connected) {
                    Server.lock(server, lockCounter);
                    Server.connect(server, true, null);
                } else {
                    if (!server.transport.isConnected() && !server.reconnect(doDfs)) {
                        server.disconnect();
                        server = null;
                        isExceptionTriggered = true;
                        TraceLog.get().error("Server is not connected and failed to reconnect", 700, 0);
                        throw new ClientException("Server is not connected and failed to reconnect", -102);
                    }
                    Server.lock(server, lockCounter);
                }
                if (null != originalServer && null != originalShare && server.getName().toLowerCase().equals(originalServer.getName().toLowerCase()) && shareName.toLowerCase().equals(originalShare.getName().toLowerCase()) && originalShare.connected) {
                    share = originalShare;
                    server.unlock();
                } else {
                    if (null == credentials) {
                        credentials = new PasswordCredentials();
                    }
                    share = server.connectShare(shareName, credentials, false, mountParams);
                }
                savedException = null;
                break;
            }
            catch (NqException e) {
                savedException = e;
                if (null != server) {
                    Server.releaseAllLocks(server);
                    server.disconnect();
                    server = null;
                }
                if ((e.getErrCode() == -1073741715 || e.getErrCode() == -1073741790 || e.getErrCode() == -18) && null != credentials.getUpdater()) {
                    Credentials.Updater updater;
                    if (updaterCounter-- > 0 && null != (updater = credentials.getUpdater())) {
                        TraceLog.get().message("Calling user's update() method", 2000);
                        credentials = updater.update(credentials, credentials.getHook());
                        if (null == credentials) {
                            isExceptionTriggered = true;
                            TraceLog.get().exit("Credentials returned by update method cannot be null", 700);
                            throw new NqException("Credentials returned by update method cannot be null", -18);
                        }
                        isMountUsingCredentialsUpdater = true;
                        continue;
                    }
                    isExceptionTriggered = true;
                    TraceLog.get().error("Connection refused : ", e, 700, e.getErrCode());
                    throw new ClientException("Connection refused : " + e, e.getErrCode());
                }
                isExceptionTriggered = true;
                TraceLog.get().error("Connection refused : ", e, 10, e.getErrCode());
                TraceLog.get().caught(e);
                ClientException ce = new ClientException("Connection refused : " + e.getMessage(), -101);
                ce.initCause(e);
                throw ce;
            }
            finally {
                if (isExceptionTriggered && null != originalServer) {
                    Server.lock(originalServer, lockCounter);
                }
                if (null == server) continue;
                Server.releaseAllLocks(server);
            }
        }
        if (null != savedException) {
            TraceLog.get().error("Could not connect to server ", savedException, 700, savedException.getErrCode());
            TraceLog.get().caught(savedException, 700);
            throw savedException;
        }
        TraceLog.get().exit(700);
        return share;
    }

    protected static void disconnectShareInternally(Mount mount, Server server, Share share) throws NqException {
        TraceLog.get().enter(300);
        Server originalServer = mount.getServer();
        Share originalShare = mount.getShare();
        TraceLog.get().message(null != originalServer ? "originalServer name = " + originalServer.getName() + ", server name = " + server.getName() : "originalServer is null", 2000);
        TraceLog.get().message(null != originalShare ? "originalShare name = " + originalShare.getName() + ", share name = " + share.getName() : "originalShare is null", 2000);
        if (null == originalServer || null == originalShare || !server.getName().toLowerCase().equals(originalServer.getName().toLowerCase()) || !share.getName().toLowerCase().equals(originalShare.getName().toLowerCase())) {
            server.disconnect(null, true, false, share);
        }
        TraceLog.get().exit(300);
    }

    private void checkConnection(Mount mount, boolean doDfs) throws NqException {
        TraceLog.get().enter("doDfs = " + doDfs, mount, 700);
        int localTimeout = Config.jnq.getInt("RETRYTIMEOUT");
        int retryCount = Config.jnq.getInt("RETRYCOUNT");
        if (null != mount) {
            localTimeout = mount.getRetryTimeout();
            retryCount = mount.getRetryCount();
        }
        User user = this.getUser();
        Server server = user.getServer();
        File file = new File();
        file.oplockLevel = 0;
        file.accessMask = 0x100081;
        file.accessMask = 0 == (file.accessMask & this.access) ? this.access : file.accessMask;
        file.info.setAttributes(0);
        file.disposition = 1;
        file.createOptions = 0;
        file.share = this;
        file.shareAccess = 7;
        file.setShareRelativePath("\u0000");
        file.durableState = 1;
        file.durableHandle = new UUID();
        file.durableFlags = 0;
        file.durableTimeout = 0;
        for (int i = retryCount; i > 0; --i) {
            try {
                server.smb.doCreate(file);
                break;
            }
            catch (SmbException e) {
                int status = e.getErrCode();
                if (-1073741267 == status) {
                    if (!server.reconnect(doDfs)) {
                        TraceLog.get().caught(e);
                        throw new ClientException("Unable to reconnect", -111);
                    }
                    if (i == AbstractFile.END_OF_LOOP) {
                        TraceLog.get().caught(e);
                        throw new SmbException("Operation failed", -1073741267);
                    }
                    if (retryCount == i) continue;
                    TraceLog.get().message("Inside retry loop, waiting " + localTimeout + " seconds.", 2000);
                    Utility.waitABit(localTimeout);
                    localTimeout *= 2;
                    continue;
                }
                if (-1073741790 == status) {
                    TraceLog.get().message("Caught an access denied trying to create an empty file.", 2000);
                    TraceLog.get().exit(700);
                    return;
                }
                TraceLog.get().caught(e);
                throw e;
            }
        }
        server.smb.doClose(file);
        TraceLog.get().exit(700);
    }

    public static Share connectIpc(Server server, Credentials credentials) throws NqException {
        TraceLog.get().enter(700);
        User user = (User)server.users.get(credentials.getKey());
        TraceLog.get().message("connectIpc user=", user, 2000);
        if (null == user) {
            TraceLog.get().exit(700);
            return null;
        }
        Share share = null;
        share = Share.connect(user, "IPC$", null, credentials, false, new MountParams());
        TraceLog.get().exit(700);
        return share;
    }

    protected static boolean disconnect(Share share) throws NqException {
        Server server;
        TraceLog.get().enter("share = ", share, 700);
        User user = share.user;
        if (null != user && null != (server = user.getServer())) {
            try {
                server.smb.doTreeDisconnect(share);
            }
            catch (SmbException e) {
                TraceLog.get().error("SmbException = ", e, 10, e.getErrCode());
            }
            share.connected = false;
            user.shares.remove(share.getName());
        }
        TraceLog.get().exit(700);
        return true;
    }

    public Info getInfo() throws NqException {
        if (this.isIpc || this.isPrinter) {
            throw new ClientException("The share is not File System", -103);
        }
        Server server = this.getUser().getServer();
        server.smb.doQueryFsInfo(this);
        return this.info;
    }

    public User getUser() {
        return this.user;
    }

    protected void setUser(User user) {
        this.user = user;
    }

    public boolean isPrinter() {
        return this.info.type == 1;
    }

    public boolean isFile() {
        return this.info.type == 0;
    }

    protected boolean isDfs() {
        return (this.flags & 1) != 0;
    }

    public String toString() {
        return "Share [connected=" + this.connected + ", isIpc=" + this.isIpc + ", isPrinter=" + this.isPrinter + ", info=" + this.info + "]";
    }

    public static class Info {
        public String name = "";
        public String comment = "";
        public boolean isHidden = false;
        public int type = 0;
        public int sectorsPerCluster;
        public int bytesPerSector;
        public long freeClusters;
        public long totalClusters;
        public int serialNumber;
        public String label = "";
        public int permissions;
        public int maxusers;
        public SecurityDescriptor sd = null;
        public String path = "";

        public String toString() {
            return "Info [name=" + this.name + ", path=" + this.path + "]";
        }
    }
}

