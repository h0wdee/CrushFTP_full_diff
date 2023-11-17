/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.AsyncConsumer;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.ClientException;
import com.visuality.nq.client.ClientSmb;
import com.visuality.nq.client.MountParams;
import com.visuality.nq.client.Share;
import com.visuality.nq.client.Smb200;
import com.visuality.nq.client.Transport;
import com.visuality.nq.client.User;
import com.visuality.nq.client.dfs.Dfs;
import com.visuality.nq.common.Blob;
import com.visuality.nq.common.IpAddressHelper;
import com.visuality.nq.common.Item;
import com.visuality.nq.common.NamedRepository;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.Resolver;
import com.visuality.nq.common.Smb2Header;
import com.visuality.nq.common.SmbDialect;
import com.visuality.nq.common.SmbException;
import com.visuality.nq.common.SyncObject;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.config.Config;
import com.visuality.nq.resolve.NetbiosException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Server
extends Item
implements Transport.ConnectionBrokenCallback {
    private static NamedRepository servers = new NamedRepository();
    protected static final int CC_CAP_MESSAGESIGNING = 1;
    public static final int CC_CAP_DFS = 2;
    public static final int CC_CAP_INFOPASSTHRU = 4;
    public static final int CC_CAP_LARGEMTU = 8;
    private static final long SERVER_IDLETIMEOUT = 900000L;
    private static final String RETURNING_TRUE = "Returning true.";
    private static final String RETURNING_FALSE = "Returning false";
    private static final String RETURNING_NULL = "Returning null";
    private static final String RETURNING_SERVER = "Returning server = ";
    private static final int MAX_TIME_TO_WAIT_FOR_LOCK = 30000;
    protected String calledName;
    protected boolean connected = false;
    protected Lock connectedSync = new ReentrantLock();
    protected long maxLockWaitTime;
    public Object waitingNotifyResponsesSync = new Object();
    protected InetAddress[] ips;
    protected Transport transport;
    public ClientSmb smb = null;
    protected ClientSmb negoSmb = null;
    protected Object smbContext;
    protected Object smbContextSync = new Object();
    public NamedRepository users = new NamedRepository();
    public int capabilities;
    public int maxTrans;
    public int maxRead;
    public int maxWrite;
    public Blob firstSecurityBlob = new Blob();
    public boolean useExtendedSecurity;
    public int vcNumber;
    public Vector async = new Vector();
    public HashMap expectedResponses = new HashMap();
    public Vector waitingNotifyResponses = new Vector();
    public User masterUser;
    public boolean useName;
    public boolean isReconnecting;
    public boolean userSecurity;
    public boolean isTemporary;
    public boolean negoAscii;
    public boolean useAscii;
    public boolean connectionBroke;
    public boolean isAesGcm;
    public boolean isNegotiationValidated;
    public long[] clientGuid = new long[]{0L, 0L};
    public int serverCapabilites;
    public byte[] serverGUID = new byte[16];
    public int serverSecurityMode;
    public int serverDialectRevision;
    public boolean isPreauthIntegOn;
    public byte[] preauthIntegHashVal = new byte[64];
    public byte[] captureHdr = new byte[64];
    private boolean isDisconnecting = false;
    private int hashCode;
    private long ttl;
    boolean isServerSupportingGSSAPI = true;
    public CreditsHandler creditHandler = new CreditsHandler();

    private Server(String name) {
        super(name.toLowerCase());
        this.createNewServer(true, null);
    }

    private Server(String name, boolean extendedSecurity, InetAddress[] ips) {
        super(name.toLowerCase());
        this.createNewServer(extendedSecurity, ips);
    }

    protected Share connectShare(String shareName, Credentials credentials, boolean doDfs) throws NqException {
        return this.connectShare(shareName, credentials, doDfs, new MountParams());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Share connectShare(String shareName, Credentials credentials, boolean doDfs, MountParams mountParams) throws NqException {
        User user;
        TraceLog.get().enter("shareName = " + shareName + "; doDfs = " + doDfs + "; credentials = ", credentials, 700);
        Share share = null;
        boolean resLogon = false;
        NamedRepository namedRepository = this.users;
        synchronized (namedRepository) {
            Object key = credentials.getKey();
            if (null == key) {
                TraceLog.get().error("Credentials key is null, check the credentials", 2000, 0);
                throw new ClientException("Credentials key is null, check the credentials", -105);
            }
            if (null == this.users.get(key)) {
                user = new User(this, credentials);
                this.masterUser = null == this.masterUser ? user : this.masterUser;
            } else {
                user = (User)this.users.get(credentials.getKey());
            }
            TraceLog.get().message("user = ", user, 2000);
        }
        try {
            TraceLog.get().message("User attempting logon = ", user, 2000);
            resLogon = user.logon(mountParams);
            TraceLog.get().message("resLogon = " + resLogon, 2000);
        }
        catch (SmbException e) {
            TraceLog.get().caught(e);
            throw e;
        }
        if (!resLogon) {
            TraceLog.get().message("Logon refused, check the credentials", 2000);
            throw new NqException("Logon refused, check the credentials", -18);
        }
        if (!this.isNegotiationValidated) {
            if (this.smb.getRevision() != 768 && this.smb.getRevision() != 770) {
                this.isNegotiationValidated = true;
            } else {
                Share shareForValidateNego = null;
                short[] dialects = SmbDialect.getSmbDialectList(mountParams.minDialect, mountParams.maxDialect);
                if (0 == dialects.length) {
                    TraceLog.get().error("No valid dialect exists", 2000);
                    throw new NqException("No valid dialect exists.", -22);
                }
                try {
                    shareForValidateNego = Share.connectIpc(this, credentials);
                }
                catch (NqException e) {
                    if (!shareName.equals("IPC$")) {
                        shareForValidateNego = Share.connect(user, shareName, null, credentials, doDfs, mountParams);
                    }
                    throw e;
                }
                if (null == shareForValidateNego) {
                    return null;
                }
                try {
                    this.isNegotiationValidated = this.smb.doValidateNegotiate(this, user, shareForValidateNego, dialects);
                }
                catch (NqException e) {
                    shareForValidateNego.shutdown();
                    TraceLog.get().error("Unable to validate negotiate", 2000);
                    throw e;
                }
                if (!this.isNegotiationValidated) {
                    shareForValidateNego.shutdown();
                    TraceLog.get().error("Unable to validate negotiate", 2000);
                    throw new NqException("Unable to validate negotiate", -23);
                }
                if (!shareForValidateNego.isIpc || shareName.equals("IPC$")) {
                    share = shareForValidateNego;
                }
            }
        }
        if (null == share) {
            share = shareName.equals("IPC$") ? Share.connectIpc(this, credentials) : Share.connect(user, shareName, null, credentials, doDfs, mountParams);
        }
        TraceLog.get().exit(700);
        return share;
    }

    protected Share connectShare(String shareName, MountParams mountParams) throws NqException {
        User user;
        TraceLog.get().enter("shareName = " + shareName, 700);
        Share share = null;
        PasswordCredentials credentials = PasswordCredentials.getDefaultCredentials();
        boolean isNewUser = false;
        if (!this.users.containsKey(credentials.getKey())) {
            user = new User(this, credentials);
            isNewUser = true;
        } else {
            user = (User)this.users.get(credentials.getKey());
        }
        boolean logonResult = true;
        try {
            logonResult = user.logon();
        }
        catch (SmbException e) {
            TraceLog.get().message("Logon did not succeed. Exception is ", e, 2000);
            TraceLog.get().message("for user = ", user, 2000);
            if (isNewUser) {
                User.checkRemoveUser(this, credentials.getKey());
            }
            logonResult = false;
        }
        TraceLog.get().message("user is maybe logged on: logonResult = " + logonResult + ", user = ", user, 2000);
        if (!logonResult) {
            TraceLog.get().message("Logon refused, check the credentials", 2000);
            throw new ClientException("Logon refused, check the credentials", -105);
        }
        if (!this.isNegotiationValidated) {
            if (this.smb.getRevision() != 768 && this.smb.getRevision() != 770) {
                this.isNegotiationValidated = true;
            } else {
                Share shareForValidateNego = null;
                short[] dialects = SmbDialect.getSmbDialectList(mountParams.minDialect, mountParams.maxDialect);
                if (0 == dialects.length) {
                    TraceLog.get().error("No valid dialect exists.", 2000);
                    throw new NqException("No valid dialect exists.", -22);
                }
                try {
                    shareForValidateNego = Share.connectIpc(this, credentials);
                }
                catch (NqException e) {
                    if (!shareName.equals("IPC$")) {
                        TraceLog.get().error("Could not use IPC$ to validate negotiate, the regular share will be used instead", 2000);
                        shareForValidateNego = Share.connect(user, shareName, null, credentials, true, mountParams);
                    }
                    throw e;
                }
                if (null == shareForValidateNego) {
                    return null;
                }
                try {
                    this.isNegotiationValidated = this.smb.doValidateNegotiate(this, user, shareForValidateNego, dialects);
                }
                catch (NqException e) {
                    shareForValidateNego.shutdown();
                    TraceLog.get().error("Unable to validate negotiate", 2000);
                    throw e;
                }
                if (!this.isNegotiationValidated) {
                    shareForValidateNego.shutdown();
                    TraceLog.get().error("Unable to validate negotiate", 2000);
                    throw new NqException("Unable to validate negotiate", -23);
                }
                if (!shareForValidateNego.isIpc || shareName.equals("IPC$")) {
                    share = shareForValidateNego;
                }
            }
        }
        if (null == share) {
            share = shareName.equals("IPC$") ? Share.connectIpc(this, credentials) : Share.connect(user, shareName, null, credentials, true, mountParams);
        }
        TraceLog.get().exit("Share was connected, ", share, 700);
        return share;
    }

    protected static NamedRepository getServers() {
        return servers;
    }

    protected void dispose(Iterator srvItr) throws NqException {
        this.dispose(srvItr, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void dispose(Iterator srvItr, boolean isLogoffUsers) throws NqException {
        TraceLog.get().enter("isLogoffUsers = " + isLogoffUsers, 700);
        if (isLogoffUsers && null != this.users) {
            for (User user : this.users.values()) {
                user.cleanUp();
            }
            this.users.clear();
        }
        this.async.clear();
        Object object = this.expectedResponses;
        synchronized (object) {
            this.expectedResponses.clear();
        }
        object = this.waitingNotifyResponsesSync;
        synchronized (object) {
            this.waitingNotifyResponses.clear();
        }
        if (null != this.ips) {
            this.ips[0] = null;
        }
        this.transport.disconnect();
        object = servers;
        synchronized (object) {
            this.setFindable(false);
            if (null == srvItr) {
                TraceLog.get().message("Removing server ", this, 2000);
                this.getRepository().remove(this.getName());
                servers.remove(this.hashCode());
            } else {
                TraceLog.get().message("Removing server ", this, 2000);
                this.getRepository().remove(this.getName());
                srvItr.remove();
            }
            this.connected = false;
        }
        TraceLog.get().exit(700);
    }

    public String toString() {
        String ipsString = null == this.ips || null == this.ips[0] ? "null" : Arrays.deepToString(this.ips);
        return "Server [object=" + this.hashCode + ", calledName=" + this.calledName + ", connected=" + this.connected + ", ips=" + ipsString + ", locks=" + this.getLocks() + ", credits=" + this.creditHandler.getCredits() + ", useExtendedSecurity=" + this.useExtendedSecurity + ", users=" + this.users + ", useName=" + this.useName + ", isReconnecting=" + this.isReconnecting + ", isTemporary=" + this.isTemporary + ", isAesGcm=" + this.isAesGcm + ", isNegotiationValidated=" + this.isNegotiationValidated + ", isDisconnecting=" + this.isDisconnecting + "]";
    }

    @Override
    protected void dump() {
        TraceLog.get().message("Server : ", this, 1000);
        TraceLog.get().message(" Users: ", 1000);
        this.users.dump();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void removeExpectedResponses() {
        HashMap hashMap = this.expectedResponses;
        synchronized (hashMap) {
            this.expectedResponses.clear();
        }
    }

    @Override
    public void connectionBroken(Transport transport) {
        this.removeExpectedResponses();
    }

    private boolean connectionAttempt(boolean extendedSecurity, ClientSmb dialect) throws NqException {
        return this.connectionAttempt(extendedSecurity, dialect, new MountParams());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean connectionAttempt(boolean extendedSecurity, ClientSmb dialect, MountParams mountParams) throws NqException {
        int status;
        TraceLog.get().enter("extendedSecurity = " + extendedSecurity + ", dialect = " + dialect, 700);
        this.useExtendedSecurity = extendedSecurity;
        if (TraceLog.get().canLog(2000)) {
            TraceLog.get().message("Servers ips = " + Arrays.toString(this.ips) + ", name = " + this.getName() + ", smb = " + this.smb, 2000);
        }
        if (0 == mountParams.port || -1 > mountParams.port || IpAddressHelper.MAX_PORT < mountParams.port) {
            String errMsg = "Bad port number, the port value can be between 1 - 65353, and the current value is : " + mountParams.port;
            TraceLog.get().error(errMsg, 2000, 0);
            throw new NqException(errMsg, -20);
        }
        if (!this.transport.connect(this.ips, this.getName().toLowerCase(), this.smb, this, this.isTemporary, this.captureHdr, mountParams.port)) {
            if (this.transport.isConnectionTimedout()) {
                TraceLog.get().error("Mount timeout error", 10);
                throw new ClientException("Mount timeout error", -109);
            }
            String errMsg = "Unable to connect to the server" + (-1 != mountParams.port ? " or to the given port = " + mountParams.port : "");
            TraceLog.get().error(errMsg, 2000, 0);
            throw new ClientException(errMsg, -101);
        }
        this.negoSmb = null != dialect ? dialect : ClientSmb.getDefaultSmb(mountParams.minDialect);
        this.smb = ClientSmb.getDummySmb();
        Object errMsg = this.smbContextSync;
        synchronized (errMsg) {
            this.smbContext = null;
        }
        this.isNegotiationValidated = false;
        this.transport.setServer(this);
        do {
            if (null == this.firstSecurityBlob) {
                this.firstSecurityBlob = new Blob();
            } else {
                this.firstSecurityBlob.data = null;
                this.firstSecurityBlob.len = 0;
            }
            short[] dialects = SmbDialect.getSmbDialectList(mountParams.minDialect, mountParams.maxDialect);
            if (0 == dialects.length) {
                TraceLog.get().message("No valid dialect exists.", 2000);
                throw new NqException("No valid dialect exists.", -22);
            }
            status = this.negoSmb.doNegotiate(this, this.firstSecurityBlob, dialects);
            if (null == this.firstSecurityBlob || null == this.firstSecurityBlob.data || this.firstSecurityBlob.len == 0) {
                this.isServerSupportingGSSAPI = false;
            }
            if (0 == status || 259 == status) continue;
            TraceLog.get().error("Negotiate failed.", 2000, 0);
            throw new SmbException("Negotiate failed.", status);
        } while (status == 259);
        TraceLog.get().message("Starting transport for server = ", this, 700);
        this.transport.initTransport();
        this.transport.start();
        if (this.smb != this.negoSmb) {
            this.smb = this.negoSmb;
        }
        if (0 == status) {
            this.connected = Boolean.TRUE;
        }
        TraceLog.get().exit("connected = " + this.connected, 700);
        return this.connected;
    }

    @Override
    protected void unlockCallback() throws NqException {
        this.disconnect();
    }

    private static Server findServerByIp(InetAddress[] ips, ClientSmb dialect) throws NqException {
        TraceLog.get().enter("ips = " + Arrays.deepToString(ips) + ", dialect = " + dialect, 700);
        if (null == ips || null == ips[0]) {
            return null;
        }
        for (Server server : servers.values()) {
            if (null == server.ips) continue;
            for (int i = 0; i < server.ips.length; ++i) {
                for (int j = 0; j < ips.length; ++j) {
                    if (ips[j] == null || server.ips[i] == null || !Arrays.equals(ips[j].getAddress(), server.ips[i].getAddress())) continue;
                    if (server.isFindable()) {
                        if (dialect == null && server.isTemporary || dialect != null && !dialect.getClass().isInstance(server.smb)) {
                            TraceLog.get().exit(RETURNING_NULL, 700);
                            return null;
                        }
                        TraceLog.get().exit("server = ", server, 700);
                        return server;
                    }
                    TraceLog.get().message("Not findable: ", server, 2000);
                    throw new NqException("Not findable: " + server, -12);
                }
            }
        }
        TraceLog.get().exit(RETURNING_NULL, 700);
        return null;
    }

    private static Server findServerByName(String name, ClientSmb dialect) throws NqException {
        TraceLog.get().enter("name = " + name + ", dialect = " + dialect, 700);
        for (Server server : servers.values()) {
            if (!server.getName().toLowerCase().equals(name.toLowerCase())) continue;
            if (server.isFindable()) {
                if (dialect == null && server.isTemporary || dialect != null && !dialect.getClass().isInstance(server.smb)) {
                    TraceLog.get().exit(RETURNING_NULL, 700);
                    return null;
                }
                TraceLog.get().exit("server = ", server, 700);
                return server;
            }
            TraceLog.get().message("Not findable: ", server, 2000);
            throw new NqException("Not findable; server = " + server, -12);
        }
        TraceLog.get().exit(700);
        return null;
    }

    private void createNewServer(boolean extendedSecurity, InetAddress[] ips) {
        TraceLog.get().enter(700);
        servers.put(this.hashCode(), this);
        this.resetServer();
        this.users.clear();
        this.ips = ips;
        this.useExtendedSecurity = extendedSecurity;
        TraceLog.get().exit(700);
    }

    static void disconnectAll() throws NqException {
        TraceLog.get().enter(700);
        while (servers.values().size() > 0) {
            Iterator serverItr = servers.values().iterator();
            while (serverItr.hasNext()) {
                Server server = (Server)serverItr.next();
                TraceLog.get().message("Disconnecting server = ", server, 2000);
                server.disconnect(serverItr, true, true);
            }
        }
        TraceLog.get().exit(700);
    }

    protected boolean mustUseSignatures() {
        return (this.capabilities & 1) != 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void checkTimeouts() throws NqException {
        TraceLog.get().enter(700);
        int idleTime = (Integer)Config.jnq.getNE("CLEANUP_THREAD_SERVER_IDLE_PERIOD") * 60 * 1000;
        LinkedList<Server> serversToDisconnect = new LinkedList<Server>();
        NamedRepository namedRepository = servers;
        synchronized (namedRepository) {
            for (Server server : servers.values()) {
                if (!server.transport.isTimeoutExpired(idleTime)) continue;
                serversToDisconnect.add(server);
            }
        }
        Iterator serverIterator = serversToDisconnect.iterator();
        while (serverIterator.hasNext()) {
            Server server = (Server)serverIterator.next();
            TraceLog.get().message("Disconnecting server " + server.getName(), 2000);
            while (!server.canDispose()) {
                server.disconnect();
            }
            serverIterator.remove();
        }
        TraceLog.get().exit(700);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected static ConcurrentHashMap<Integer, String> getConnectedServerNames() {
        TraceLog.get().enter(700);
        ConcurrentHashMap<Integer, String> serverNames = new ConcurrentHashMap<Integer, String>();
        NamedRepository namedRepository = servers;
        synchronized (namedRepository) {
            for (Server server : servers.values()) {
                if (!server.connected || !server.transport.isConnected()) continue;
                serverNames.put(server.getName().hashCode(), server.getName());
            }
        }
        TraceLog.get().exit(700);
        return serverNames;
    }

    protected void finalize() {
        TraceLog.get().enter(1000);
        try {
            if (0 < this.getLocks()) {
                this.disconnect();
            }
        }
        catch (NqException nqException) {
            // empty catch block
        }
        TraceLog.get().exit(1000);
    }

    protected void disconnect(boolean isLogoffUsers) throws NqException {
        this.disconnect(null, isLogoffUsers, new Object[0]);
    }

    protected void disconnect() throws NqException {
        this.disconnect(null, true, new Object[0]);
    }

    protected void disconnect(Iterator srvItr) throws NqException {
        this.disconnect(srvItr, true, new Object[0]);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void disconnect(Iterator srvItr, boolean isLogoffUsers, Object ... args) throws NqException {
        block41: {
            TraceLog.get().enter(700);
            Share share = null;
            boolean isHardDisconnect = 0 == args.length ? false : (Boolean)args[0];
            NqException caughtException = null;
            try {
                Object it;
                if (!isHardDisconnect) {
                    Server.waitTryLock(this);
                }
                share = 2 > args.length ? null : (Share)args[1];
                TraceLog.get().message("locks = " + this.getLocks() + ", isLogoffUsers = " + isLogoffUsers + "; isHardDisconnect = " + isHardDisconnect + "; share = " + share, 1000);
                this.unlock();
                if (!this.connected && !isHardDisconnect) {
                    if (this.canDispose()) {
                        this.dispose(null, isLogoffUsers);
                    }
                    TraceLog.get().exit(700);
                    return;
                }
                if (!this.canDispose() && !isHardDisconnect) {
                    if (null != share && !share.isIpc) {
                        User user = share.getUser();
                        user.logoff();
                    }
                    TraceLog.get().exit("Cannot dispose of server = ", this, 700);
                    return;
                }
                NamedRepository user = servers;
                synchronized (user) {
                    TraceLog.get().message("Calling notifyAll()", 700);
                    servers.notifyAll();
                    this.isDisconnecting = true;
                }
                if (isLogoffUsers) {
                    it = this.users.values().iterator();
                    while (it.hasNext()) {
                        User user2 = (User)it.next();
                        try {
                            if (user2.isLoggedOn && this.connected) {
                                TraceLog.get().message("Logging off user = " + user2, 2000);
                                while (1 < user2.numberOfTimesLoggedOn()) {
                                    user2.subtractFromNumberOfTimesLoggedOn();
                                }
                                user2.logoff();
                            }
                        }
                        catch (NqException e) {
                            // empty catch block
                        }
                        it.remove();
                    }
                }
                if (isHardDisconnect) {
                    it = this.users.values().iterator();
                    while (it.hasNext()) {
                        it.next();
                        it.remove();
                    }
                }
                this.masterUser = null;
                if (null != this.smb && null != this.smbContext) {
                    this.smb.freeContext(this.smbContext, this);
                }
                this.firstSecurityBlob = null;
                if (!this.isReconnecting || isHardDisconnect || this.canDispose()) {
                    this.dispose(srvItr, isLogoffUsers);
                }
            }
            catch (NqException e) {
                caughtException = e;
            }
            finally {
                if (!isHardDisconnect) {
                    Server.releaseLock(this);
                }
                NamedRepository namedRepository = servers;
                synchronized (namedRepository) {
                    this.isDisconnecting = false;
                    servers.notifyAll();
                }
                if (null == caughtException) break block41;
                TraceLog.get().caught(caughtException);
                throw caughtException;
            }
        }
        TraceLog.get().exit(700);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void disconnect(Iterator srvItr, Share share) throws NqException {
        block13: {
            TraceLog.get().enter(700);
            NqException caughtException = null;
            try {
                Server.waitTryLock(this);
                this.unlock();
                if (!this.connected) {
                    if (this.canDispose()) {
                        this.dispose(null, false);
                    }
                    TraceLog.get().exit(700);
                    return;
                }
                if (!this.canDispose()) {
                    if (null != share && !share.isIpc) {
                        User user = share.getUser();
                        user.logoff();
                    }
                    TraceLog.get().exit("Cannot dispose of server = " + this, 700);
                    return;
                }
                this.masterUser = null;
                if (null != this.smb && null != this.smbContext) {
                    this.smb.freeContext(this.smbContext, this);
                }
                this.firstSecurityBlob = null;
                if (!this.isReconnecting || this.canDispose()) {
                    this.dispose(srvItr, false);
                }
            }
            catch (NqException e) {
                caughtException = e;
            }
            finally {
                Server.releaseLock(this);
                if (null == caughtException) break block13;
                TraceLog.get().caught(caughtException);
                throw caughtException;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void disconnect(User user) throws NqException {
        block12: {
            TraceLog.get().enter("user = " + user, 700);
            NqException caughtException = null;
            try {
                Server.waitTryLock(this);
                TraceLog.get().message("server.lock = " + this.getLocks(), 2000);
                if (null != user) {
                    int numberOfTimesUserLoggedOn = user.numberOfTimesLoggedOn();
                    while (numberOfTimesUserLoggedOn-- > 0) {
                        this.unlock();
                        user.cleanUp();
                    }
                } else {
                    this.unlock();
                }
                if (!this.canDispose()) {
                    TraceLog.get().exit("Cannot dispose of server = " + this, 700);
                    return;
                }
                this.masterUser = null;
                if (null != this.smb && null != this.smbContext) {
                    this.smb.freeContext(this.smbContext, this);
                }
                this.firstSecurityBlob = null;
                if (!this.isReconnecting || this.canDispose()) {
                    this.dispose(null, false);
                }
            }
            catch (NqException e) {
                caughtException = e;
            }
            finally {
                Server.releaseLock(this);
                if (null == caughtException) break block12;
                TraceLog.get().caught(caughtException);
                throw caughtException;
            }
        }
        TraceLog.get().exit(700);
    }

    protected static void connect(Server server, boolean extendedSecurity, ClientSmb dialect) throws NqException {
        Server.connect(server, extendedSecurity, dialect, new MountParams(), false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected static Server findOrCreate(String name, boolean extendedSecurity, ClientSmb dialect) throws NqException {
        TraceLog.get().enter("name = " + name + "; extendedSecurity = " + extendedSecurity + "; dialect = " + dialect, 700);
        Server server = null;
        InetAddress[] ips = new InetAddress[1];
        String host = null;
        String pdcName = null;
        boolean isServerCleanupThreadEnabled = (Boolean)Config.jnq.getNE("CLEANUP_THREAD_SERVER_ENABLED");
        if (!isServerCleanupThreadEnabled) {
            Server.checkTimeouts();
        }
        if (null == name) {
            TraceLog.get().message("server name is null", 2000);
            throw new ClientException("server name is null", -103);
        }
        NamedRepository namedRepository = servers;
        synchronized (namedRepository) {
            boolean nameIsIp = IpAddressHelper.isIpAddress(name);
            if (nameIsIp) {
                ips[0] = IpAddressHelper.stringToIp(name);
                if (null == ips[0]) {
                    nameIsIp = false;
                }
            }
            TraceLog.get().message("nameIsIp = " + nameIsIp, 2000);
            if (nameIsIp) {
                server = Server.findServerByIp(ips, dialect);
                TraceLog.get().message("Found server = ", server, 2000);
            } else {
                server = Server.findServerByName(name, dialect);
                TraceLog.get().message("Found server = ", server, 2000);
            }
            if (null != server) {
                if (server.isDisconnecting) {
                    try {
                        TraceLog.get().message("Executing wait", 2000);
                        servers.wait();
                        TraceLog.get().message("Woke up from wait", 2000);
                    }
                    catch (InterruptedException e) {
                        // empty catch block
                    }
                    server = null;
                } else if (null == server.ips || null == server.ips[0]) {
                    TraceLog.get().message("Could not use server (ips is null): ", server.hashCode(), 2000);
                    servers.remove(server.hashCode());
                    server = null;
                } else {
                    if (server.ttl + 900000L > System.currentTimeMillis()) {
                        server.updateTtl();
                        server.lock();
                        TraceLog.get().exit(RETURNING_SERVER, server, 700);
                        return server;
                    }
                    TraceLog.get().message("Using ips from server as server timed out.", 2000);
                    ips = server.ips;
                }
            }
            boolean isOldResolvingSetting = true;
            try {
                isOldResolvingSetting = Config.jnq.getBool("OLDRESOLVINGSETTINGS");
            }
            catch (NqException e) {
                // empty catch block
            }
            InetAddress[] resolvedIps = null;
            if (!nameIsIp && !isOldResolvingSetting) {
                Resolver resolver = new Resolver();
                resolvedIps = resolver.hostToIp(name);
            }
            boolean isDfsEnabled = true;
            try {
                isDfsEnabled = Config.jnq.getBool("DFSENABLE");
            }
            catch (NqException e) {
                // empty catch block
            }
            if (null == server && !nameIsIp && null == dialect && isDfsEnabled && null == resolvedIps) {
                pdcName = Dfs.resolveHost(name);
                TraceLog.get().message("pdcName = ", pdcName, 2000);
                if (null != pdcName) {
                    server = Server.findServerByName(pdcName, null);
                    TraceLog.get().message("Found server = ", server, 2000);
                    if (null != server) {
                        if (server.isDisconnecting) {
                            try {
                                TraceLog.get().message("Executing wait", 2000);
                                servers.wait();
                                TraceLog.get().message("Woke up from wait", 2000);
                            }
                            catch (InterruptedException e) {
                                // empty catch block
                            }
                            server = null;
                        } else if (null == server.ips || null == server.ips[0]) {
                            TraceLog.get().message("Could not use server (ips is null): ", server.hashCode(), 2000);
                            servers.remove(server.hashCode());
                            server = null;
                        } else {
                            server.updateTtl();
                            server.lock();
                            TraceLog.get().exit(RETURNING_SERVER, server, 700);
                            return server;
                        }
                    }
                }
            }
            Resolver resolver = new Resolver();
            host = nameIsIp ? resolver.ipToHost(ips[0]) : (null != pdcName ? pdcName : new String(name));
            TraceLog.get().message("host = ", host, 2000);
            if (null == host) {
                TraceLog.get().message("name is IP, but IP was not resolved to name", 2000);
                host = new String(name);
                TraceLog.get().message("host = ", host, 2000);
            } else {
                TraceLog.get().message("origin IP list :", 2000);
                for (InetAddress obj : ips) {
                    TraceLog.get().message("IP: ", obj, 2000);
                }
                if (null == resolvedIps) {
                    resolvedIps = resolver.hostToIp(host, ips[0]);
                }
                if (null != resolvedIps) {
                    ips = resolvedIps;
                    TraceLog.get().message("resolved all host IPs :", 2000);
                    for (InetAddress obj : ips) {
                        TraceLog.get().message("IP: ", obj, 2000);
                    }
                }
            }
            TraceLog.get().message("Server = ", server, 2000);
            server = Server.findServerByIp(ips, dialect);
            TraceLog.get().message("Found server = ", server, 2000);
            if (null != server) {
                if (server.isDisconnecting) {
                    try {
                        TraceLog.get().message("Executing wait", 2000);
                        servers.wait();
                        TraceLog.get().message("Woke up from wait", 2000);
                    }
                    catch (InterruptedException e) {
                        // empty catch block
                    }
                    server = null;
                } else {
                    server.updateTtl();
                    server.lock();
                    TraceLog.get().exit(RETURNING_SERVER, server, 700);
                    return server;
                }
            }
            server = new Server(host, extendedSecurity, ips);
            server.useName = !nameIsIp;
            server.isTemporary = null != dialect;
            server.put(server.getRepository());
            server.updateTtl();
            TraceLog.get().exit(RETURNING_SERVER, server, 700);
            return server;
        }
    }

    protected static void connect(Server server, boolean extendedSecurity, ClientSmb dialect, boolean forceNetbios) throws NqException {
        Server.connect(server, extendedSecurity, dialect, new MountParams(), forceNetbios);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected static void connect(Server server, boolean extendedSecurity, ClientSmb dialect, MountParams mountParams, boolean forceNetbios) throws NqException {
        block17: {
            TraceLog.get().enter("server=" + server + "; extendedSecurity=" + extendedSecurity + "; dialect=" + dialect + "; mountParams=" + mountParams + "; forceNetbios=" + forceNetbios, 700);
            if (null != dialect && Client.getDialects().hasSmb(dialect.getRevision())) {
                dialect.setSolo(true);
            }
            Server.lock(server);
            try {
                if (server.connected) {
                    TraceLog.get().exit(700);
                    return;
                }
                server.transport.setForceNetbios(forceNetbios);
                server.connectionAttempt(extendedSecurity, dialect, mountParams);
            }
            catch (NqException ex) {
                if (-101 == ex.getErrCode()) {
                    throw ex;
                }
                if (-103 == ex.getErrCode() || !Client.getDialects().supportSmb2() || -1073741637 == ex.getErrCode()) {
                    String err = "Bad Parameter:" + ex.getMessage() + "; forceNetbios=" + forceNetbios + "; extendedSecurity=" + extendedSecurity + "; dialect=" + dialect;
                    if (-1073741637 == ex.getErrCode()) {
                        TraceLog.get().message(err, 2000);
                        throw new NqException(err, -22);
                    }
                    TraceLog.get().message(err, 2000);
                    throw new ClientException(err, -103);
                }
                if (null != dialect) break block17;
                dialect = new Smb200();
                Server.lock(server);
                try {
                    server.connectionAttempt(extendedSecurity, dialect, mountParams);
                }
                catch (NqException e) {
                    if (-103 == ex.getErrCode()) {
                        String err = "Bad Parameter:" + ex.getMessage() + "; forceNetbios=" + forceNetbios + "; extendedSecurity=" + extendedSecurity + "; dialect=" + dialect;
                        TraceLog.get().message(err, 2000);
                        throw new ClientException(err, -103);
                    }
                    TraceLog.get().caught(e, 700);
                    throw e;
                }
                Server.releaseLock(server);
            }
            finally {
                Server.releaseLock(server);
            }
        }
        if (!server.connected) {
            TraceLog.get().message("Server is not connected and failed to connect", 2000);
            throw new ClientException("Server is not connected and failed to connect", -102);
        }
        TraceLog.get().exit(700);
    }

    protected boolean reconnect() throws NqException {
        return this.reconnect(true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected boolean reconnect(boolean doDfs) throws NqException {
        TraceLog.get().enter("Reconnect attempt for " + this.getName() + ", doDfs = " + doDfs, 700);
        boolean result = false;
        Server.lock(this);
        try {
            if (!this.isFindable()) {
                TraceLog.get().message("Server was unmounted due to being idle or due to a previous disconnect.", 2000);
                throw new NqException("Server was unmounted due to being idle or due to a previous disconnect.", -26);
            }
            if (this.isDisconnecting) {
                TraceLog.get().exit("False reconnect alarm - server already disconnecting; Returning false", 700);
                boolean bl = false;
                return bl;
            }
            if (this.isReconnecting) {
                TraceLog.get().exit("False reconnect alarm - server already reconnecting; Returning true.", 700);
                boolean bl = true;
                return bl;
            }
            if (this.transport.isConnected()) {
                TraceLog.get().exit("False reconnect alarm - server already connected; Returning true.", 700);
                boolean bl = true;
                return bl;
            }
            if (0 == this.users.size()) {
                if (this.isFindable()) {
                    this.dispose(null);
                }
                TraceLog.get().exit("False reconnect alarm - no users to logon, disposed server; Returning false", 700);
                boolean bl = false;
                return bl;
            }
            this.isReconnecting = true;
            this.connectionBroke = false;
            this.smb.signalAllMatch(this.transport);
            this.smb.freeContext(this.smbContext, this);
            try {
                this.transport.disconnect();
                this.transport.setServer(null);
                if (this.isFindable()) {
                    this.disconnect(false);
                    this.transport = new Transport(this);
                    TraceLog.get().message("New Transport hashCode = " + this.transport.hashCode(), 2000);
                }
                if (!this.isFindable()) {
                    servers.put(this.hashCode(), this);
                    this.resetServer();
                }
            }
            catch (NetbiosException e) {
                this.isReconnecting = false;
                TraceLog.get().error("False disconnect: ", e, 10, e.getErrCode());
                throw new NetbiosException("False disconnect: " + e.getMessage(), -503);
            }
            this.masterUser = null;
            boolean isUserToReconnect = false;
            for (User user : this.users.values()) {
                for (Share share : user.shares.values()) {
                    share.connected = false;
                }
                isUserToReconnect |= !user.isLoggingOff;
            }
            if (!isUserToReconnect) {
                TraceLog.get().message("False reconnect alarm - no users to logon", 2000);
                this.isReconnecting = false;
                this.connected = false;
                this.dispose(null);
                TraceLog.get().exit(RETURNING_FALSE, 700);
                boolean user = false;
                return user;
            }
            int connectAttemptCounter = 2;
            while (connectAttemptCounter-- > 0) {
                try {
                    if (null == this.ips || null == this.ips[0]) {
                        Resolver resolver = new Resolver();
                        this.ips = resolver.hostToIp(this.getName());
                    }
                    result = this.connectionAttempt(this.useExtendedSecurity, null);
                    break;
                }
                catch (NqException ex) {
                    this.isReconnecting = false;
                    this.connected = false;
                    this.dispose(null, false);
                    TraceLog.get().caught(ex, 2000);
                    if (ex.getErrCode() == -109 && connectAttemptCounter > 0 && !IpAddressHelper.isIpAddress(this.getName())) {
                        this.ips = null;
                        continue;
                    }
                    TraceLog.get().exit("Reconnect failed; ", ex, 700);
                    boolean share = result;
                    this.isReconnecting = false;
                    Server.releaseLock(this);
                    return share;
                }
            }
            Iterator userIterator = this.users.values().iterator();
            while (userIterator.hasNext()) {
                User user = (User)userIterator.next();
                user.setup();
                TraceLog.get().message("called setup to user = " + user, 2000);
                try {
                    result = user.logon();
                    if (result) {
                        result = user.reconnectShares(doDfs);
                    }
                    TraceLog.get().message("user after logon = ", user, 2000);
                    if (!result) {
                        if (0L == user.uid) {
                            user.logoff();
                        }
                        userIterator.remove();
                    }
                    TraceLog.get().message("reconnectShares result = " + result + "; user = ", user, 2000);
                }
                catch (SmbException e) {
                    result = false;
                    if (-1073741267 != e.getErrCode()) {
                        userIterator.remove();
                    }
                    TraceLog.get().message("logon/logoff failed: ", e, 1000);
                }
            }
        }
        finally {
            this.isReconnecting = false;
            Server.releaseLock(this);
        }
        TraceLog.get().exit("Reconnect " + (result ? "succeeded" : "failed"), 700);
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void resetServer() {
        TraceLog.get().enter(700);
        this.hashCode = this.hashCode();
        this.async.clear();
        this.expectedResponses.clear();
        this.waitingNotifyResponses.clear();
        if (null == this.transport) {
            this.transport = new Transport(this);
            TraceLog.get().message("New Transport hashCode = " + this.transport.hashCode(), 2000);
        } else {
            this.transport.setServer(this);
        }
        Object object = this.smbContextSync;
        synchronized (object) {
            this.smbContext = null;
        }
        this.creditHandler.resetCredits();
        this.firstSecurityBlob = null;
        this.calledName = this.getName();
        this.masterUser = null;
        this.useName = true;
        this.isReconnecting = false;
        this.userSecurity = true;
        this.connectionBroke = false;
        this.clientGuid[0] = System.currentTimeMillis();
        this.vcNumber = 1;
        this.isTemporary = false;
        this.useAscii = false;
        this.negoAscii = false;
        this.isAesGcm = false;
        this.capabilities = 0;
        this.isPreauthIntegOn = false;
        Arrays.fill(this.captureHdr, (byte)0);
        this.setFindable(true);
        this.updateTtl();
        this.initLock();
        this.lock();
        TraceLog.get().exit(700);
    }

    protected static Iterator iterateServers() {
        return servers.values().iterator();
    }

    protected Iterator iterateUsers() {
        return this.users.values().iterator();
    }

    public static void closeAllConnections() throws NqException {
        TraceLog.get().enter(700);
        Iterator serverIterator = servers.values().iterator();
        while (serverIterator.hasNext()) {
            Server server = (Server)serverIterator.next();
            server.disconnect(serverIterator, true, true);
        }
        TraceLog.get().exit(700);
    }

    protected ClientSmb getSmb() {
        return this.smb;
    }

    public Transport getTransport() {
        return this.transport;
    }

    public boolean isServerSupportingGSSAPI() {
        return this.isServerSupportingGSSAPI;
    }

    protected void asyncRemoveItem(AsyncConsumer item) {
        this.async.remove(item);
    }

    protected void updateTtl() {
        this.ttl = System.currentTimeMillis();
    }

    protected long getTtl() {
        return this.ttl;
    }

    public static void lock(Server server) {
        ReentrantLock lock = (ReentrantLock)server.connectedSync;
        if (TraceLog.get().canLog(200)) {
            TraceLog.get().message("Lock -> " + Thread.currentThread().getName() + ", lock count = " + lock.getHoldCount() + ", " + server.connectedSync + ", server = " + server.getName(), 200);
        }
        lock.lock();
        server.maxLockWaitTime = System.currentTimeMillis() + 30000L;
    }

    public static void lock(Server server, int numberOfTimesToLock) {
        ReentrantLock lock = (ReentrantLock)server.connectedSync;
        if (TraceLog.get().canLog(200)) {
            TraceLog.get().message("Lock -> " + Thread.currentThread().getName() + ", lock count = " + lock.getHoldCount() + ", number of times to lock = " + numberOfTimesToLock + ", " + server.connectedSync + ", server = " + server.getName(), 200);
        }
        while (numberOfTimesToLock-- > 0) {
            lock.lock();
        }
        server.maxLockWaitTime = System.currentTimeMillis() + 30000L;
    }

    public static void releaseLock(Server server) {
        if (null == server) {
            if (TraceLog.get().canLog(200)) {
                TraceLog.get().message("Unable to unlock, server is null");
            }
            return;
        }
        ReentrantLock lock = (ReentrantLock)server.connectedSync;
        if (TraceLog.get().canLog(200)) {
            TraceLog.get().message("Unlock -> " + Thread.currentThread().getName() + ", lock count = " + lock.getHoldCount() + ", " + server.connectedSync + ", server = " + server.getName(), 200);
        }
        if (0 < lock.getHoldCount()) {
            lock.unlock();
        }
        if (0 == lock.getHoldCount()) {
            server.maxLockWaitTime = System.currentTimeMillis() + 30000L;
        }
    }

    public static int releaseAllLocks(Server server) {
        if (null == server) {
            return 0;
        }
        ReentrantLock lock = (ReentrantLock)server.connectedSync;
        if (TraceLog.get().canLog(200)) {
            TraceLog.get().message("Unlock all -> " + Thread.currentThread().getName() + ", lock count = " + lock.getHoldCount() + ", " + server.connectedSync + ", server = " + server.getName(), 200);
        }
        int cntr = 0;
        while (0 < lock.getHoldCount()) {
            lock.unlock();
            ++cntr;
        }
        if (cntr > 0) {
            server.maxLockWaitTime = System.currentTimeMillis() + 30000L;
        }
        return cntr;
    }

    public static void lockIfNotLocked(Server server) {
        Server.lock(server);
        ReentrantLock lock = (ReentrantLock)server.connectedSync;
        if (TraceLog.get().canLog(200)) {
            TraceLog.get().message("lockIfNotLocked -> " + Thread.currentThread().getName() + ", lock count = " + lock.getHoldCount() + ", " + server.connectedSync + ", server = " + server.getName(), 200);
        }
        if (lock.getHoldCount() > 1) {
            Server.releaseLock(server);
        }
        server.maxLockWaitTime = System.currentTimeMillis() + 30000L;
    }

    public static boolean tryLock(Server server) {
        boolean returnValue = false;
        ReentrantLock lock = (ReentrantLock)server.connectedSync;
        if (TraceLog.get().canLog(200)) {
            TraceLog.get().message("TryLock -> " + Thread.currentThread().getName() + ", lock count = " + lock.getHoldCount() + ", " + server.connectedSync + ", server = " + server.getName(), 200);
        }
        try {
            returnValue = lock.tryLock(100L, TimeUnit.MILLISECONDS);
            server.maxLockWaitTime = System.currentTimeMillis() + 30000L;
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
        return returnValue;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void waitTryLock(Server server) throws NqException {
        server.maxLockWaitTime = System.currentTimeMillis() + 30000L;
        ReentrantLock lock = (ReentrantLock)server.connectedSync;
        while (!Server.tryLock(server)) {
            Server server2 = server;
            synchronized (server2) {
                ReentrantLock updatedLock = (ReentrantLock)server.connectedSync;
                long currentTime = System.currentTimeMillis();
                if (currentTime > server.maxLockWaitTime && lock == updatedLock) {
                    server.connectedSync = new ReentrantLock();
                    TraceLog.get().error("Thread " + Thread.currentThread().getName() + " has waited too long for lock. Old lock = " + lock + ", new lock = " + server.connectedSync, 700, 0);
                    lock = (ReentrantLock)server.connectedSync;
                } else if (lock != updatedLock) {
                    lock = updatedLock;
                    server.maxLockWaitTime = currentTime + 30000L;
                }
            }
        }
        server.maxLockWaitTime = System.currentTimeMillis() + 30000L;
    }

    public static int lockCount(Server server) {
        ReentrantLock lock = (ReentrantLock)server.connectedSync;
        return lock.getHoldCount();
    }

    public static void identifyLock(Server server) {
        ReentrantLock lock = (ReentrantLock)server.connectedSync;
        int queueLength = lock.getQueueLength();
        if (TraceLog.get().canLog(200)) {
            TraceLog.get().message("LOCK = " + Thread.currentThread().getName() + ", " + lock + ", queuelength = " + queueLength, 200);
        }
    }

    protected boolean updateCredits(Smb2Header header, ClientSmb.Match match) {
        boolean result = false;
        if (header.credits > 0) {
            this.creditHandler.postCredits(header.credits);
            result = true;
        } else if (0 != header.status) {
            this.creditHandler.postCredits(match.creditCharge);
            result = true;
        }
        return result;
    }

    protected void updateCredits(Smb2Header header) {
        if (header.credits > 0) {
            this.creditHandler.postCredits(header.credits);
        }
    }

    protected void updateCredits(ClientSmb.Match match) {
        this.creditHandler.postCredits(match.creditCharge);
    }

    protected void updateCredits(int credits) {
        this.creditHandler.postCredits(credits);
    }

    public static class CreditsHandler {
        private final int INITIAL_CREDITS = 2;
        private volatile int credits = 2;
        private final SyncObject creditsSyncObj = new SyncObject();

        public int getCredits() {
            return this.credits;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void resetCredits() {
            SyncObject syncObject = this.creditsSyncObj;
            synchronized (syncObject) {
                this.credits = 2;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public boolean waitForCredits(int credits) {
            long originalMilliSeconds;
            TraceLog.get().enter("Argument credits = " + credits + ", server.credits = " + this.credits, 700);
            long TIMEOUT = 2L * Client.getSmbTimeout();
            long currentMilliSeconds = originalMilliSeconds = System.currentTimeMillis();
            SyncObject syncObject = this.creditsSyncObj;
            synchronized (syncObject) {
                int newCredits = this.credits - credits;
                while (newCredits <= 0) {
                    if (currentMilliSeconds - originalMilliSeconds > TIMEOUT) {
                        TraceLog.get().exit(Server.RETURNING_FALSE, 700);
                        return false;
                    }
                    try {
                        currentMilliSeconds = this.creditsSyncObj.syncWait(Client.getSmbTimeout(), currentMilliSeconds);
                    }
                    catch (InterruptedException e) {
                        TraceLog.get().exit(Server.RETURNING_FALSE, 700);
                        return false;
                    }
                    newCredits = this.credits - credits;
                }
                this.credits = newCredits;
            }
            TraceLog.get().exit(Server.RETURNING_TRUE, 700);
            return true;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void postCredits(int credits) {
            TraceLog.get().enter("credits = " + credits + ", server.credits = " + this.credits, 2000);
            SyncObject syncObject = this.creditsSyncObj;
            synchronized (syncObject) {
                this.credits += credits;
                this.creditsSyncObj.syncNotify();
            }
            TraceLog.get().exit("server.credits = " + this.credits, 2000);
        }
    }
}

