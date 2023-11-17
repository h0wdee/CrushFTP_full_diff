/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.auth.Authentications;
import com.visuality.nq.auth.Credentials;
import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.auth.Spnego;
import com.visuality.nq.client.ClientException;
import com.visuality.nq.client.Mount;
import com.visuality.nq.client.Server;
import com.visuality.nq.client.ServerCleanup;
import com.visuality.nq.client.Share;
import com.visuality.nq.client.User;
import com.visuality.nq.client.dfs.DfsCache;
import com.visuality.nq.client.dfs.Entry;
import com.visuality.nq.common.Capture;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SmbDialect;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.config.Config;
import com.visuality.nq.resolve.NetbiosDaemon;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class Client {
    private static int backupListTimeout = 15000;
    private static long smbTimeout = 15000L;
    private static long maxSmbTimeout = 35000L;
    private static boolean signingEnabled = true;
    private static SmbDialect dialects = new SmbDialect();
    protected static final int NUMBER_OF_TIMES_TO_CALL_UPDATER = 1;
    public static final int DFS_NUMOFRETRIES = 50;
    public static final int AM_SECURITY_LEVEL_NONE = 0;
    public static final int AM_SECURITY_LEVEL_NTLMV1 = 1;
    public static final int AM_SECURITY_LEVEL_KRB5_NTLMV1 = 2;
    public static final int AM_SECURITY_LEVEL_NTLMV2 = 3;
    public static final int AM_SECURITY_LEVEL_KRB5_NTLMV2 = 4;
    public static final int AM_SECURITY_LEVEL_NTLMSSP = 3;
    public static final int AM_SECURITY_LEVEL_KRB5 = 4;

    public static long getSmbTimeout() {
        return smbTimeout;
    }

    public static long getMaxSmbTimeout() {
        return maxSmbTimeout;
    }

    public static void setSmbTimeout(long smbTimeout) {
        if (0L <= smbTimeout) {
            Client.smbTimeout = smbTimeout;
            TraceLog.get().message("Set SMBTIMEOUT=" + smbTimeout, 2000);
        }
    }

    @Deprecated
    public static int getAuthenticationLevel() {
        return Spnego.AM_CURRAUTHLEVEL;
    }

    @Deprecated
    public static void setAuthenticationLevel(int authenticationLevel) {
        if (5 >= authenticationLevel && 0 <= authenticationLevel) {
            Spnego.AM_CURRAUTHLEVEL = authenticationLevel;
        }
    }

    public static boolean isSigningEnabled() {
        return signingEnabled;
    }

    public static void setSigningEnabled(boolean signingEnabled) {
        Client.signingEnabled = signingEnabled;
    }

    public static void setBackupListTimeout(int backupListTimeout) {
        if (0 <= backupListTimeout) {
            Client.backupListTimeout = backupListTimeout;
            NetbiosDaemon.setDatagramTimeout(backupListTimeout);
        }
    }

    public static int getBackupListTimeout() {
        return backupListTimeout;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void checkCredentials(String server, Credentials creds) throws NqException {
        TraceLog.get().enter(200);
        if (null == creds) {
            TraceLog.get().exit("Invalid parameter : ", server, 200);
            throw new ClientException("Invalid parameter : " + server + " : " + creds, -103);
        }
        if (null == server || server.equals("")) {
            server = Config.getDomainName();
        }
        Server checkServer = null;
        try {
            checkServer = Server.findOrCreate(server, true, null);
        }
        catch (NqException e) {
            TraceLog.get().caught(e);
            TraceLog.get().exit("Connection refused : " + e.getMessage() + ", server name=" + server, 10);
            throw new ClientException("Connection refused : " + e.getMessage() + " server name='" + server + "'", -102);
        }
        if (null == checkServer) {
            TraceLog.get().exit("Server is not connected: " + server, 10);
            throw new ClientException("Server is not connected", -102);
        }
        if (!checkServer.connected) {
            try {
                Server.connect(checkServer, true, null);
            }
            catch (NqException e) {
                checkServer.disconnect();
                checkServer = null;
                TraceLog.get().caught(e);
                TraceLog.get().exit("Unable to connect to server + server", e, 10);
                throw e;
            }
        } else if (!checkServer.transport.isConnected() && !checkServer.reconnect()) {
            checkServer.disconnect();
            checkServer = null;
            TraceLog.get().exit("Server is not connected and failed to reconnect", 10);
            throw new ClientException("Server is not connected and failed to reconnect", -102);
        }
        boolean res = true;
        boolean isNewUser = false;
        User checkUser = null;
        Server.lock(checkServer);
        try {
            checkUser = (User)checkServer.users.get(creds.getKey());
            if (null == checkUser) {
                checkUser = new User(checkServer, creds);
                TraceLog.get().message("New user.", 2000);
                try {
                    res = checkUser.logon();
                }
                catch (NqException e) {
                    res = false;
                    TraceLog.get().message("CheckUser logon failed: ", e, 2000);
                }
                isNewUser = true;
            }
            if (isNewUser) {
                if (res) {
                    try {
                        checkUser.logoff();
                    }
                    catch (NqException e) {
                        res = false;
                        TraceLog.get().caught(e, 2000);
                        TraceLog.get().message("CheckUser logon failed: ", e, 2000);
                    }
                }
            } else {
                TraceLog.get().message("Identical user exists with creds ", creds, 2000);
                try {
                    if (!checkUser.isLoggedOn) {
                        TraceLog.get().message("User exists but is not logged on", 2000);
                        res = checkUser.logon();
                        checkUser.logoff();
                    }
                }
                catch (NqException e) {
                    res = false;
                    TraceLog.get().caught(e, 2000);
                    TraceLog.get().message("CheckUser logon failed: ", e, 2000);
                }
            }
            if (null != checkServer) {
                TraceLog.get().message("Disconnecting from server ", checkServer, 2000);
                checkServer.disconnect(null, false, new Object[0]);
            }
            if (!res) {
                TraceLog.get().exit("Connection refused : The user cannot authenticate with these credentials to server '" + server + "'", 200);
                throw new NqException("Connection refused : The user cannot authenticate with these credentials to server '" + server + "'", -18);
            }
        }
        finally {
            Server.releaseLock(checkServer);
            checkServer = null;
        }
        TraceLog.get().exit(200);
    }

    public static void checkCredentials(String server) throws NqException {
        Client.checkCredentials(server, PasswordCredentials.getDefaultCredentials());
    }

    public static void closeAllConnections() throws NqException {
        Server.disconnectAll();
    }

    public static void closeHiddenConnections() throws NqException {
        TraceLog.get().enter(200);
        Iterator serverIterator = Server.iterateServers();
        while (serverIterator.hasNext()) {
            Server server = (Server)serverIterator.next();
            for (User user : server.users.values()) {
                for (Share share : user.shares.values()) {
                    boolean doDisconnect = true;
                    Iterator fileItr = share.iterateFiles();
                    if (fileItr.hasNext()) {
                        doDisconnect = false;
                    }
                    if (!doDisconnect || 0 != share.mountsRelated) continue;
                    share.check();
                }
            }
        }
        TraceLog.get().exit(200);
    }

    public static void stop() throws NqException {
        try {
            TraceLog.get().message("calling closeHiddenConnections()", 200);
            Client.closeHiddenConnections();
        }
        catch (NqException e) {
            TraceLog.get().caught(e, 200);
        }
        try {
            TraceLog.get().message("calling closeAllConnections()", 200);
            Client.closeAllConnections();
        }
        catch (NqException e) {
            TraceLog.get().caught(e, 200);
        }
        TraceLog.get().message("calling NetbiosDaemon.stop()", 200);
        NetbiosDaemon.stop();
        TraceLog.get().message("calling ServerCleanup.terminate()", 200);
        ServerCleanup.terminate();
        TraceLog traceLog = TraceLog.get();
        if (null != traceLog) {
            traceLog.stop();
        }
        Capture.stop();
    }

    public static void setDialect(short dialect, boolean setActive) throws NqException {
        TraceLog.get().enter("dialect = " + dialect + "; setActive = " + setActive, 200);
        dialects.enableDialect(dialect, setActive);
        TraceLog.get().exit(200);
    }

    protected static SmbDialect getDialects() {
        return dialects;
    }

    public static void setMaxSecurityLevel(int level) throws NqException {
        if (level < 0 || level > 4) {
            throw new NqException("Illegal security level = " + level, -20);
        }
        Authentications.setMaxSecurityLevel(level);
    }

    public static int getMaxSecurityLevel() {
        return Authentications.getMaxSecurityLevel();
    }

    public static void setMinSecurityLevel(int level) throws NqException {
        if (level < 0 || level > 4) {
            throw new NqException("Illegal security level = " + level, -20);
        }
        Authentications.setMinSecurityLevel(level);
    }

    public static List<Entry> getReferralInfo() {
        return DfsCache.getReferralInfo();
    }

    public static List<Mount> getActiveMounts() {
        TraceLog.get().enter(200);
        LinkedList<Mount> activeMounts = new LinkedList<Mount>();
        try {
            Server.checkTimeouts();
        }
        catch (NqException e) {
            TraceLog.get().error("Error disconnecting from some server: ", e, 2000, e.getErrCode());
        }
        ConcurrentHashMap<Integer, String> connectedServerNames = Server.getConnectedServerNames();
        ConcurrentHashMap<Integer, Mount> mounts = Mount.getMounts();
        Iterator<Mount> mountIterator = mounts.values().iterator();
        try {
            while (mountIterator.hasNext()) {
                int key;
                Server server;
                Mount mount = mountIterator.next();
                if (null == mount || null == mount.getServer() || null == (server = mount.getServer()) || null == server.getName() || !server.connected || !connectedServerNames.containsKey(key = server.getName().hashCode())) continue;
                activeMounts.add(mount);
            }
        }
        catch (NoSuchElementException nse) {
            // empty catch block
        }
        TraceLog.get().exit(200);
        return activeMounts;
    }

    static {
        try {
            signingEnabled = Config.jnq.getBool("SIGNINGPOLICY");
        }
        catch (NqException e) {
            TraceLog.get().error("NqException = ", e, 10, e.getErrCode());
        }
    }
}

