/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.ClientException;
import com.visuality.nq.client.ClientSmb;
import com.visuality.nq.client.File;
import com.visuality.nq.client.MountParams;
import com.visuality.nq.client.Server;
import com.visuality.nq.client.Share;
import com.visuality.nq.client.User;
import com.visuality.nq.common.JnqVersion;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SmbException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Validator;
import com.visuality.nq.config.Config;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Mount
implements Cloneable {
    private static ConcurrentHashMap<Integer, Mount> mounts = new ConcurrentHashMap();
    protected String serverName;
    protected String shareName;
    protected String relativeSharePath = "";
    private Credentials credentials;
    protected Server server = null;
    private ClientSmb dialect = null;
    private boolean extendedSecurity = true;
    private boolean forceNetbios = false;
    private int retryCount = -1;
    private int retryTimeout = -1;
    private boolean isConnected = false;
    private boolean isDfs = false;
    protected boolean isClone = false;
    private boolean isOnlyValidateSmbServer = false;
    private int SMB_SERVER_TEST_TIMEOUT = 1000;
    private MountParams mountParams;
    private Info info = new Info();
    protected Share share;
    private static boolean haveWeLoggedJnqVersion = false;

    protected boolean getIsConnected() {
        return this.isConnected;
    }

    @Deprecated
    public String getServerName() {
        return this.serverName;
    }

    public String getRelativeShareName() {
        return this.relativeSharePath;
    }

    public String getShareName() {
        String res = this.shareName;
        if (null != this.relativeSharePath && !this.relativeSharePath.equals("")) {
            res = res + "\\" + this.relativeSharePath;
        }
        return res;
    }

    public Credentials getCredentials() {
        return this.credentials;
    }

    private String getRelativeShareName(String share) throws NqException {
        String relativeShare = "";
        if (share.contains("/")) {
            share = share.replace("/", "\\");
        }
        if (share.startsWith("\\")) {
            throw new ClientException("Illegal path - " + share + " - start with \"slash\" is not allowed", -103);
        }
        if (share.contains("\\")) {
            relativeShare = share.substring(share.indexOf("\\") + 1);
        }
        return relativeShare;
    }

    public static String getOnlyShareName(String share) throws NqException {
        String newShare = share;
        if (newShare.contains("/")) {
            newShare = newShare.replace("/", "\\");
        }
        if (newShare.startsWith("\\")) {
            throw new ClientException("Illegal path - " + share + " - start with \"slash\" is not allowed", -103);
        }
        if (newShare.contains("\\")) {
            newShare = newShare.split("\\\\")[0];
        }
        return newShare;
    }

    private Mount() {
    }

    public Mount(String serverName, String shareName) throws NqException {
        this(serverName, shareName, false);
    }

    public Mount(String serverName, String shareName, boolean isReadAccess) throws NqException {
        TraceLog.get().enter("serverName = " + serverName + "; shareName = " + shareName + "; isReadAccess = " + isReadAccess, 200);
        Mount.logJnqVersion();
        this.credentials = PasswordCredentials.getDefaultCredentials();
        if (!this.testInputParameters(serverName, shareName, this.credentials)) {
            TraceLog.get().exit(200);
            throw new NqException("Invalid parameters to Mount", -20);
        }
        this.fetchConfigValues();
        this.serverName = serverName;
        this.info.originalServerName = serverName;
        this.shareName = Mount.getOnlyShareName(shareName);
        this.relativeSharePath = this.getRelativeShareName(shareName);
        this.setMountParams(new MountParams());
        try {
            this.doMount(isReadAccess);
        }
        catch (NqException e) {
            TraceLog.get().caught(e, 200);
            TraceLog.get().exit(200);
            throw e;
        }
        TraceLog.get().exit(200);
    }

    public Mount(String serverName, String shareName, Credentials credentials) throws NqException {
        this(serverName, shareName, credentials, false);
    }

    public Mount(String serverName, String shareName, Credentials credentials, boolean isReadAccess) throws NqException {
        TraceLog.get().enter("serverName = " + serverName + "; shareName = " + shareName + "; isReadAccess = " + isReadAccess, 200);
        Mount.logJnqVersion();
        if (!this.testInputParameters(serverName, shareName, credentials)) {
            TraceLog.get().exit(200);
            throw new NqException("Invalid parameters to Mount", -20);
        }
        this.fetchConfigValues();
        this.serverName = serverName;
        this.info.originalServerName = serverName;
        this.shareName = Mount.getOnlyShareName(shareName);
        this.relativeSharePath = this.getRelativeShareName(shareName);
        this.credentials = credentials;
        this.setMountParams(new MountParams());
        try {
            this.doMount(isReadAccess);
        }
        catch (NqException e) {
            TraceLog.get().caught(e, 200);
            TraceLog.get().exit(200);
            throw e;
        }
        TraceLog.get().exit(200);
    }

    protected Mount(String serverName, String shareName, Credentials credentials, boolean extendedSecurity, ClientSmb dialect, boolean forceNetbios) throws NqException {
        TraceLog.get().enter("serverName = " + serverName + "; shareName = " + shareName + "; extendedSecurity = " + extendedSecurity + "; dialect = " + dialect + "; forceNetbios = " + forceNetbios, 200);
        if (!this.testInputParameters(serverName, shareName, credentials)) {
            throw new NqException("Invalid parameters to Mount", -20);
        }
        this.fetchConfigValues();
        this.serverName = serverName;
        this.info.originalServerName = serverName;
        this.shareName = Mount.getOnlyShareName(shareName);
        this.relativeSharePath = this.getRelativeShareName(shareName);
        this.forceNetbios = forceNetbios;
        this.credentials = null == credentials ? PasswordCredentials.getDefaultCredentials() : credentials;
        this.dialect = dialect;
        this.extendedSecurity = extendedSecurity;
        this.setMountParams(new MountParams());
        this.doMount(false);
        TraceLog.get().exit(200);
    }

    public Mount(String serverName) throws NqException {
        TraceLog.get().enter("serverName = " + serverName, 200);
        this.isOnlyValidateSmbServer = true;
        Config.jnq.set("SMBTIMEOUT", this.SMB_SERVER_TEST_TIMEOUT);
        this.serverName = serverName;
        this.info.originalServerName = serverName;
        this.fetchConfigValues();
        try {
            this.doMount(true);
        }
        catch (NqException e) {
            TraceLog.get().caught(e, 200);
            TraceLog.get().exit(200);
            throw e;
        }
        TraceLog.get().exit(200);
    }

    public Mount(String serverName, String shareName, Credentials credentials, MountParams mountParams) throws NqException {
        TraceLog.get().enter("serverName = " + serverName + "; shareName = " + shareName + "; mountParams = ", mountParams, 200);
        Mount.logJnqVersion();
        if (!this.testInputParameters(serverName, shareName, credentials)) {
            TraceLog.get().exit(200);
            throw new NqException("Invalid parameters.", -20);
        }
        if (null == mountParams) {
            TraceLog.get().exit(200);
            throw new NqException("mountParams cannot be null", -20);
        }
        if (!mountParams.valid()) {
            TraceLog.get().exit(200);
            throw new NqException("Invalid values in mountParams", -20);
        }
        this.fetchConfigValues();
        this.serverName = serverName;
        this.info.originalServerName = serverName;
        this.shareName = Mount.getOnlyShareName(shareName);
        this.relativeSharePath = this.getRelativeShareName(shareName);
        this.credentials = credentials;
        this.setMountParams(mountParams);
        try {
            this.doMount(mountParams.isReadAccessSufficient, mountParams);
        }
        catch (NqException e) {
            TraceLog.get().caught(e, 200);
            TraceLog.get().exit(200);
            throw e;
        }
        TraceLog.get().exit(200);
    }

    public Mount(Server server, String shareName, Credentials credentials, boolean isReadAccess) throws NqException {
        this.shareName = Mount.getOnlyShareName(shareName);
        this.relativeSharePath = this.getRelativeShareName(shareName);
        this.setMountParams(new MountParams());
        this.credentials = credentials;
        this.info.originalServerName = this.serverName = server.getName();
        this.fetchConfigValues();
        this.doMount(isReadAccess, this.mountParams, server);
    }

    protected boolean testInputParameters(String serverName, String shareName, Credentials creds) {
        PasswordCredentials pc;
        if (!Validator.validServer(serverName)) {
            return false;
        }
        if (!Validator.validName(shareName)) {
            return false;
        }
        if (null == creds || null == creds.getDomain()) {
            return false;
        }
        return !(creds instanceof PasswordCredentials) || null != (pc = (PasswordCredentials)creds).getPassword() && null != pc.getUser();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void close() {
        TraceLog.get().enter("Closing mount on share: " + (null == this.share ? "share is null" : this.share.getName()), 300);
        try {
            Mount mount = this;
            synchronized (mount) {
                block20: {
                    if (this.isConnected) break block20;
                    TraceLog.get().exit("The mount is already been closed", 300);
                    return;
                }
                if (this.isConnected && null != this.server) {
                    try {
                        if (0 < this.server.getLocks()) {
                            if (this.isClone) {
                                this.server.disconnect();
                            } else if (this.share.getName().equals("IPC$")) {
                                this.server.disconnect(null, this.share);
                            } else {
                                this.server.disconnect(null, true, false, this.share);
                            }
                        }
                    }
                    catch (NqException e) {
                        TraceLog.get().caught(e, 2000);
                        TraceLog.get().message("Server disconnect error: ", e, 2000);
                    }
                    finally {
                        if (null != this.share) {
                            --this.share.mountsRelated;
                        }
                        this.server = null;
                        this.isConnected = false;
                    }
                }
            }
        }
        finally {
            this.removeFromMountsMap();
        }
        TraceLog.get().exit(300);
    }

    protected void finalize() {
        TraceLog.get().enter(1000);
        this.close();
        TraceLog.get().exit(1000);
    }

    private void doMount(boolean isReadAccess) throws NqException {
        this.doMount(isReadAccess, new MountParams());
    }

    private void doMount(boolean isReadAccess, MountParams mountParams) throws NqException {
        this.doMount(isReadAccess, mountParams, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void doMount(boolean isReadAccess, MountParams mountParams, Server knownServer) throws NqException {
        TraceLog.get().enter(300);
        updaterCounter = 1;
        MAX_TIMES_TRY_CONNECTING_AGAIN = true;
        tryConnectingAgainCounter = 0;
        isSessionDeleted = false;
        savedException = null;
        do {
            block44: {
                block43: {
                    block42: {
                        server = null;
                        if (null == knownServer) {
                            server = Server.findOrCreate(this.serverName, this.extendedSecurity, this.dialect);
                        } else {
                            server = knownServer;
                            server.lock();
                        }
                        TraceLog.get().message("found server = ", server, 2000);
                        if (null == server) {
                            TraceLog.get().exit("Server is not found or failed to connect.", 2000);
                            throw new ClientException("Server is not found or failed to connect.", -102);
                        }
                        Server.lock(server);
                        if (server.connected) ** GOTO lbl38
                        if (null != server.ips && null != server.ips[0]) break block42;
                        TraceLog.get().message("Server IPs could not be resolved", 2000);
                        savedException = new NqException("Server IPs could not be resolved", -14);
                        ++tryConnectingAgainCounter;
                        if (null == server) continue;
                        Server.releaseLock(server);
                        continue;
                    }
                    try {
                        Server.connect(server, this.extendedSecurity, this.dialect, mountParams, this.forceNetbios);
                    }
                    catch (NqException e) {
                        server.disconnect();
                        TraceLog.get().message("Server disconnected, caught NqException = ", e, 200);
                        TraceLog.get().caught(e);
                        throw e;
                    }
lbl38:
                    // 2 sources

                    if (!this.isOnlyValidateSmbServer) break block43;
                    TraceLog.get().exit(200);
                    if (null == server) return;
                    Server.releaseLock(server);
                    return;
                }
                if (!server.connected) ** GOTO lbl120
                if (server.transport.isConnected() || server.reconnect()) break block44;
                TraceLog.get().message("Disconnecting server", 2000);
                server.disconnect();
                savedException = new ClientException("The server failed to reconnect", -102);
                ++tryConnectingAgainCounter;
                if (null == server) continue;
                Server.releaseLock(server);
                continue;
            }
            try {
                this.share = server.connectShare(this.shareName, this.credentials, true, mountParams);
                if (null == this.share) {
                    TraceLog.get().exit("Connection refused : share :  " + this.shareName, 2000);
                    throw new ClientException("Connection refused : share :  " + this.shareName, -105);
                }
                if (null != this.share.dfsReferral) {
                    this.setIsDfs(true);
                }
                ** GOTO lbl98
            }
            catch (NqException e) {
                TraceLog.get().message("Caught exception: ", e, 2000);
                TraceLog.get().message("server = ", server, 2000);
                TraceLog.get().message("mount = ", this, 2000);
                TraceLog.get().caught(e, 2000);
                if (Mount.checkForSessionDeletedOrExpired(e) && !isSessionDeleted) {
                    TraceLog.get().message("Received response USER_SESSION_DELETED", 2000);
                    savedException = new ClientException("Received response USER_SESSION_DELETED", -102);
                    ++tryConnectingAgainCounter;
                    isSessionDeleted = true;
                    server.disconnect((User)server.users.get(this.credentials.getKey()));
                    if (null == server) continue;
                    Server.releaseLock(server);
                    continue;
                }
                try {
                    block46: {
                        block47: {
                            if (isSessionDeleted) {
                                TraceLog.get().exit("Received response USER_SESSION_DELETED two times", 10);
                                throw new ClientException("Received response USER_SESSION_DELETED two times", -102);
                            }
                            if (null != server) {
                                TraceLog.get().message("Disconnecting server", 2000);
                                server.disconnect();
                            }
                            if ((e.getErrCode() == -1073741715 || e.getErrCode() == -1073741790 || e.getErrCode() == -18) && null != this.credentials.getUpdater() && updaterCounter-- > 0 && null != (updater = this.credentials.getUpdater())) {
                                TraceLog.get().message("Calling user's update() method", 2000);
                                this.credentials = updater.update(this.credentials, this.credentials.getHook());
                                if (null == this.credentials) {
                                    TraceLog.get().exit("Credentials returned by update method cannot be null", 2000);
                                    throw new NqException("Credentials returned by update method cannot be null", -18);
                                }
                                savedException = e;
                                ++tryConnectingAgainCounter;
                                server.disconnect();
                                Server.releaseLock(server);
                                server = null;
                                continue;
                            }
                            this.throwAppropriateException(e);
lbl98:
                            // 2 sources

                            ++this.share.mountsRelated;
                            TraceLog.get().message("results from server.connectShare(), share=", this.share, 2000);
                            TraceLog.get().message("this.server=", server, 2000);
                            if (null == this.share) {
                                TraceLog.get().message("Disconnecting server", 2000);
                                server.disconnect();
                                Server.releaseLock(server);
                                server = null;
                                TraceLog.get().exit("Connection refused : share :  " + this.shareName + " or credentials error", 2000);
                                throw new ClientException("Connection refused : share :  " + this.shareName + " or credentials error", -105);
                            }
                            if (this.share.getUser().getServer() != server) {
                                Server.releaseLock(server);
                                Server.lock(this.share.getUser().getServer());
                                server = this.share.getUser().getServer();
                                TraceLog.get().message("We have a new server = ", server, 2000);
                            }
                            if (this.relativeSharePath.equals("")) break block46;
                            file = null;
                            pr = isReadAccess != false ? new File.Params(1, 1, 1, true) : new File.Params(2, 7, 1, true);
                            this.isConnected = true;
                            rerunNewFile = true;
                            isTryConnectingAgain = false;
                            break block47;
lbl120:
                            // 1 sources

                            TraceLog.get().message("Disconnecting server", 2000);
                            server.disconnect();
                            TraceLog.get().exit("Negotiation failed", 2000);
                            throw new ClientException("Negotiation failed", -111);
                        }
                        while (rerunNewFile) {
                            try {
                                this.server = server;
                                file = new File(this, pr);
                                rerunNewFile = false;
                            }
                            catch (NqException e) {
                                TraceLog.get().caught(e, 2000);
                                if (-1073741790 == e.getErrCode() && !isReadAccess && mountParams.isReadAccessSufficient) {
                                    isReadAccess = true;
                                    continue;
                                }
                                if (-1073741790 == e.getErrCode() && mountParams.enableAccessDeniedUpdater) {
                                    if (updaterCounter-- <= 0) {
                                        server.disconnect();
                                        Server.releaseLock(server);
                                        server = null;
                                        throw new ClientException(e.getMessage() + ", Error code = " + e.getErrCode() + ", could not open " + this.relativeSharePath, -24);
                                    }
                                    updater = this.credentials.getUpdater();
                                    if (null == updater) {
                                        server.disconnect();
                                        Server.releaseLock(server);
                                        server = null;
                                        throw new ClientException(e.getMessage() + ", Error code = " + e.getErrCode() + ", could not open " + this.relativeSharePath, -24);
                                    }
                                    TraceLog.get().message("Calling user's update() method", 2000);
                                    this.credentials = updater.update(this.credentials, this.credentials.getHook());
                                    if (null == this.credentials) {
                                        server.disconnect();
                                        Server.releaseLock(server);
                                        server = null;
                                        throw new NqException("Credentials returned by update method cannot be null", -18);
                                    }
                                    server.disconnect();
                                    Server.releaseLock(server);
                                    server = null;
                                    savedException = e;
                                    isTryConnectingAgain = true;
                                    rerunNewFile = false;
                                    continue;
                                }
                                if (-1073741790 == e.getErrCode()) {
                                    TraceLog.get().message("Disconnecting server", 2000);
                                    server.disconnect();
                                    Server.releaseLock(server);
                                    server = null;
                                    throw new ClientException("Access-related error = " + e, -105);
                                }
                                server.disconnect();
                                Server.releaseLock(server);
                                server = null;
                                throw new ClientException(e.getMessage() + ", Error code = " + e.getErrCode() + ", could not open " + this.relativeSharePath, -24);
                            }
                        }
                        if (isTryConnectingAgain) {
                            ++tryConnectingAgainCounter;
                            continue;
                        }
                        this.isConnected = false;
                        if (file.share != this.share || file.server != server) {
                            this.share.getUser().logoff();
                            this.setIsDfs(true);
                            --this.share.mountsRelated;
                            this.share = file.share;
                            ++this.share.mountsRelated;
                            this.shareName = file.share.getName();
                            this.relativeSharePath = file.getLocalPathFromShare();
                            TraceLog.get().message("Disconnecting server", 2000);
                            server.disconnect();
                            Server.releaseLock(server);
                            server = file.share.getUser().getServer();
                            Server.lock(server);
                            TraceLog.get().message("We have a new server = ", server, 2000);
                            this.serverName = server.getName();
                            this.server = server;
                        }
                        file.close();
                    }
                    if (this.share.getUser().getServer() != server) {
                        TraceLog.get().message("Disconnecting server ", server, 2000);
                        this.share.getUser().logoff();
                        server.disconnect();
                        Server.releaseLock(server);
                        server = this.share.getUser().getServer();
                        Server.lock(server);
                        this.setIsDfs(true);
                        TraceLog.get().message("Disconnected the server, we have a new server = ", server, 2000);
                        this.shareName = this.share.getName();
                    }
                    Server.lockIfNotLocked(server);
                    if (!server.getName().equals(this.serverName)) {
                        this.serverName = server.getName();
                    }
                    this.server = server;
                    TraceLog.get().message("We have a new server = ", this.server, 2000);
                    TraceLog.get().message("old server was ", server, 2000);
                    this.info.inetAddress = server.ips;
                    this.info.dialect = server.smb.getName();
                    this.info.serverName = server.getName();
                }
                finally {
                    if (null != server) {
                        Server.releaseLock(server);
                    }
                }
            }
            this.isConnected = true;
            break;
        } while (0 < tryConnectingAgainCounter && 1 >= tryConnectingAgainCounter);
        if (null != this.server) {
            this.addToMountsMap();
            TraceLog.get().message("Mount exiting with locks = " + Server.lockCount(this.server), 200);
            TraceLog.get().exit(300);
            return;
        }
        if (null != savedException) {
            TraceLog.get().error("Could not connect to the server: ", savedException, 700, savedException.getErrCode());
            TraceLog.get().exit(300);
            throw savedException;
        }
        TraceLog.get().exit(300);
        throw new ClientException("Could not connect to the server", -101);
    }

    public static boolean checkForSessionDeletedOrExpired(NqException e) {
        Throwable cause = e.getCause();
        NqException smbe = null;
        if (null != cause && cause instanceof SmbException) {
            smbe = (SmbException)cause;
        }
        return null != smbe && (smbe.getErrCode() == -1073741309 || smbe.getErrCode() == -1073740964) || e.getErrCode() == -1073741309 || e.getErrCode() == -1073740964 || e.getErrCode() == -26;
    }

    private void throwAppropriateException(NqException e) throws ClientException, NqException {
        switch (e.getErrCode()) {
            case -1073741565: {
                TraceLog.get().exit("Mount path is not directory : " + this.shareName, 2000);
                throw new ClientException("Mount path is not directory : " + this.shareName, e.getStackTrace(), -105);
            }
            case -1073741620: {
                TraceLog.get().exit("Wrong share name : ", e, 2000);
                throw new NqException("Wrong share name : " + e.getMessage(), e.getStackTrace(), -24);
            }
            case -1073741623: {
                TraceLog.get().exit("Share folder does not exist : " + this.shareName, 2000);
                throw new NqException("Share folder does not exist : " + this.shareName, e.getStackTrace(), -24);
            }
            case -1073741267: {
                TraceLog.get().exit("Mount failed : ", 2000);
                throw new ClientException("Mount failed : ", e.getStackTrace(), -101);
            }
            case -1073741715: 
            case -18: {
                TraceLog.get().exit("Mount failed due to logon failure or bad credentials: ", 2000);
                throw new NqException("Mount failed due to logon failure or bad credentials: ", e.getStackTrace(), -18);
            }
            case -1073741790: {
                TraceLog.get().exit("Connection refused : ", e, 2000);
                throw new ClientException("Connection refused : " + e.getMessage(), e.getStackTrace(), -105);
            }
        }
        TraceLog.get().exit("Unknown exception : ", e, 2000);
        TraceLog.get().caught(e);
        throw e;
    }

    public Server getServer() {
        return this.server;
    }

    protected void addShareLink(Share share) {
    }

    public Share getShare() {
        return this.share;
    }

    protected void fetchRetryValues() {
        try {
            if (-1 == this.retryCount) {
                this.retryCount = Config.jnq.getInt("RETRYCOUNT");
            }
        }
        catch (NqException e) {
            this.retryCount = 3;
        }
        try {
            if (-1 == this.retryTimeout) {
                this.retryTimeout = Config.jnq.getInt("RETRYTIMEOUT");
            }
        }
        catch (NqException e) {
            this.retryTimeout = 0;
        }
    }

    protected void fetchConfigValues() {
        try {
            int timeout = Config.jnq.getInt("SMBTIMEOUT");
            if (timeout > 0) {
                Client.setSmbTimeout(timeout);
                Config.jnq.set("SMBTIMEOUT", 0);
            }
        }
        catch (NqException e) {
            e.printStackTrace();
        }
        this.fetchRetryValues();
    }

    public int getRetryCount() {
        return this.retryCount;
    }

    public void setRetryCount(int retryCount) throws NqException {
        if (retryCount < 0) {
            throw new NqException("Illegal argument: cannot be negative = " + retryCount, -20);
        }
        this.retryCount = retryCount;
    }

    public int getRetryTimeout() {
        return this.retryTimeout;
    }

    public void setRetryTimeout(int retryTimeout) throws NqException {
        if (retryTimeout < 0) {
            throw new NqException("Illegal argument: cannot be negative = " + retryTimeout, -20);
        }
        this.retryTimeout = retryTimeout;
    }

    public Info getInfo() {
        return this.info;
    }

    protected Mount clone() {
        Mount mount = new Mount();
        mount.dialect = this.dialect;
        mount.extendedSecurity = this.extendedSecurity;
        mount.forceNetbios = this.forceNetbios;
        mount.info = this.info;
        mount.relativeSharePath = this.relativeSharePath;
        mount.retryCount = this.retryCount;
        mount.retryTimeout = this.retryTimeout;
        mount.server = this.server;
        mount.share = this.share;
        mount.isConnected = this.isConnected;
        mount.isDfs = this.isDfs();
        mount.setMountParams(this.getMountParams());
        mount.server.lock();
        mount.isClone = true;
        return mount;
    }

    private static void logJnqVersion() {
        if (!haveWeLoggedJnqVersion) {
            haveWeLoggedJnqVersion = true;
            JnqVersion jnqVersion = new JnqVersion();
            String version = jnqVersion.getJnqVersion();
            String buildTime = jnqVersion.getJnqBuildTime();
            String misc = jnqVersion.getJnqMiscInfo();
            TraceLog.get().message("jNQ version = " + version + ", build time = " + buildTime + ", " + misc, 200);
        }
    }

    public MountParams getMountParams() {
        return this.mountParams;
    }

    protected void setMountParams(MountParams mountParams) {
        this.mountParams = mountParams;
    }

    public boolean isDfs() {
        boolean isDfsEnabled = true;
        try {
            isDfsEnabled = Config.jnq.getBool("DFSENABLE");
        }
        catch (NqException nqException) {
            // empty catch block
        }
        return isDfsEnabled && this.isDfs;
    }

    protected void setIsDfs(boolean isDfs) {
        this.isDfs = isDfs;
    }

    public String toString() {
        return "Mount [serverName=" + this.serverName + ", shareName=" + this.shareName + ", relativeSharePath=" + this.relativeSharePath + ", credentials=" + this.credentials + ", server=" + this.server + ", dialect=" + this.dialect + ", extendedSecurity=" + this.extendedSecurity + ", forceNetbios=" + this.forceNetbios + ", retryCount=" + this.retryCount + ", retryTimeout=" + this.retryTimeout + ", isConnected=" + this.isConnected + ", info=" + this.info + ", share=" + this.share + "]";
    }

    private void addToMountsMap() {
        TraceLog.get().enter(300);
        mounts.put(this.hashCode(), this);
        TraceLog.get().exit(300);
    }

    private void removeFromMountsMap() {
        TraceLog.get().enter(300);
        mounts.remove(this.hashCode());
        TraceLog.get().exit(300);
    }

    protected static ConcurrentHashMap<Integer, Mount> getMounts() {
        return mounts;
    }

    public class Info {
        InetAddress[] inetAddress;
        String serverName;
        String dialect;
        String originalServerName;

        public InetAddress[] getIpAddress() {
            return this.inetAddress;
        }

        public String getServerName() {
            return this.serverName;
        }

        public String getDialectVersion() throws NqException {
            return this.dialect;
        }

        public String getOriginalServerName() {
            return this.originalServerName;
        }

        public String toString() {
            return "Info [inetAddress=" + Arrays.toString(this.inetAddress) + ", serverName=" + this.serverName + ", dialect=" + this.dialect + "]" + ", originalServerName=" + this.originalServerName;
        }
    }
}

