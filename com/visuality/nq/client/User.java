/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.auth.Authentications;
import com.visuality.nq.auth.Credentials;
import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.auth.Spnego;
import com.visuality.nq.auth.SubjectCredentials;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.ClientException;
import com.visuality.nq.client.MountParams;
import com.visuality.nq.client.Server;
import com.visuality.nq.client.Share;
import com.visuality.nq.common.Blob;
import com.visuality.nq.common.NamedRepository;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SmbException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.UUID;
import com.visuality.nq.config.Config;
import java.util.Iterator;

public class User {
    public boolean isLoggingOff;
    public NamedRepository shares = new NamedRepository();
    public boolean isLoggedOn;
    public long uid;
    public UUID uuid;
    private int numberOfTimesLoggedOn = 0;
    public boolean useSigning;
    protected boolean isLoggingOn;
    protected Object loggingObj = new Object();
    boolean isAnonymous;
    protected boolean isGuest;
    protected boolean isEncrypted;
    private Blob sessionKey;
    protected Blob macSessionKey;
    byte[] encryptionKey = new byte[16];
    byte[] decryptionKey = new byte[16];
    byte[] applicationKey = new byte[16];
    boolean isPreauthIntegOn;
    byte[] preauthIntegHashVal = new byte[64];
    private Credentials credentials;
    private Server server;

    public Blob getSessionKey() {
        return this.sessionKey;
    }

    public User(Server server, Credentials credentials) {
        this.server = server;
        this.isLoggedOn = false;
        this.shares.clear();
        this.credentials = credentials;
        this.isAnonymous = credentials instanceof PasswordCredentials ? ((PasswordCredentials)this.credentials).isAnonymous() : (credentials instanceof SubjectCredentials ? ((SubjectCredentials)this.credentials).isAnonymous() : false);
        this.uid = 0L;
        this.useSigning = false;
        this.macSessionKey = new Blob();
        this.sessionKey = new Blob();
        this.isEncrypted = false;
        this.isLoggingOff = false;
        this.isLoggingOn = false;
        this.isGuest = false;
        TraceLog.get().message("Adding user to user list=", this, 2000);
        server.users.put(credentials.getKey(), this);
    }

    public void dispose() {
    }

    private int convertSmbStatusIntoSpnegoStatus(int res) {
        switch (res) {
            case 0: {
                return 0;
            }
            case -1073741802: {
                return -1;
            }
            case -1073741643: {
                return -2;
            }
        }
        return -4;
    }

    public static void checkRemoveUser(Server server, Object key) {
        TraceLog.get().enter(2000);
        if (null == server || null == key) {
            TraceLog.get().exit(2000);
            return;
        }
        User user = (User)server.users.get(key);
        if (null != user && user.numberOfTimesLoggedOn() > 0) {
            TraceLog.get().exit(2000);
            return;
        }
        server.users.remove(key);
        TraceLog.get().message("Removed user from server.users = ", user, 2000);
        TraceLog.get().exit(2000);
    }

    public boolean logon() throws NqException {
        return this.logon(new MountParams());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean logon(MountParams mountParams) throws NqException {
        TraceLog.get().enter(this.toString(), 300);
        int status = 2;
        boolean result = false;
        boolean isException = false;
        this.addToNumberOfTimesLoggedOn();
        while (true) {
            Object object = this.loggingObj;
            synchronized (object) {
                TraceLog.get().message("In synchronized block.", 2000);
                if (this.isLoggingOn || this.isLoggingOff) {
                    try {
                        TraceLog.get().message("Executing wait", 2000);
                        this.loggingObj.wait();
                        TraceLog.get().message("Woke up from wait", 2000);
                    }
                    catch (InterruptedException e) {
                        // empty catch block
                    }
                }
                if (Server.tryLock(this.server)) {
                    this.isLoggingOn = true;
                    break;
                }
                Server.identifyLock(this.server);
                try {
                    this.loggingObj.wait(100L);
                }
                catch (InterruptedException e) {
                    // empty catch block
                }
            }
        }
        try {
            if (this.isLoggedOn) {
                result = true;
                TraceLog.get().exit("result = " + result, 300);
                boolean bl = result;
                return bl;
            }
            Server server = this.server;
            if (this.isAnonymous) {
                this.useSigning = false;
            }
            this.isPreauthIntegOn = true;
            System.arraycopy(server.preauthIntegHashVal, 0, this.preauthIntegHashVal, 0, 64);
            this.sessionKey.data = null;
            this.macSessionKey.data = null;
            this.encryptionKey = null;
            this.decryptionKey = null;
            this.applicationKey = null;
            if (Config.jnq.getBool("ENABLENONSECUREAUTHMETHODS")) {
                Authentications.setNonSecureAuthentication(true);
            } else {
                Authentications.setNonSecureAuthentication(false);
            }
            if (server.useExtendedSecurity) {
                InternalSpnegoExchange spnegoExchange = new InternalSpnegoExchange();
                boolean restrictCrypters = 0 != (server.capabilities & 1) && server.smb.restrictCrypters;
                try {
                    status = Spnego.spnegoLogon(this, server, this.getCredentials(), restrictCrypters, this.sessionKey, this.macSessionKey, spnegoExchange, mountParams);
                }
                catch (Exception e) {
                    isException = true;
                    TraceLog.get().message("Caught exception from Spnego.spnegoLogon: ", e, 2000);
                    if (e instanceof ClientException) {
                        ClientException ce = (ClientException)e;
                        TraceLog.get().exit("Caught exception = ", ce, 300);
                        throw ce;
                    }
                    TraceLog.get().message("Caught exception = ", e, 300);
                    throw new NqException(e.getMessage(), e.getStackTrace(), -18);
                }
                if (0 == status) {
                    this.isLoggedOn = true;
                    if (0 != (server.capabilities & 1) && Client.isSigningEnabled() && !this.isAnonymous) {
                        this.useSigning = true;
                    }
                }
                if (0 == status && null != this.macSessionKey.data && this.macSessionKey.len > server.smb.maxSigningKeyLen) {
                    this.macSessionKey.len = server.smb.maxSigningKeyLen;
                }
                if (server.smb.getRevision() < 785) {
                    this.server.smb.keyDerivation(this);
                }
                if (0 != status) {
                    this.encryptionKey = null;
                    this.decryptionKey = null;
                    this.applicationKey = null;
                }
                result = 0 == status;
                TraceLog.get().exit("result =  " + result, 300);
                boolean e = result;
                return e;
            }
            for (int level = mountParams.maxSecurityLevel; level >= mountParams.minSecurityLevel; --level) {
                int res;
                if (this.credentials instanceof SubjectCredentials) {
                    result = false;
                    TraceLog.get().exit("result =  " + result, 300);
                    boolean spnegoExchange = result;
                    return spnegoExchange;
                }
                Blob pass1 = new Blob();
                Blob pass2 = new Blob();
                if (null == server.firstSecurityBlob.data) {
                    result = false;
                    TraceLog.get().exit("result = " + result, 300);
                    boolean ce = result;
                    return ce;
                }
                TraceLog.get().message("Creating first security blob", 2000);
                this.sessionKey = new Blob(server.firstSecurityBlob);
                if (this.isAnonymous || !server.userSecurity) {
                    try {
                        TraceLog.get().message("Calling doSessionSetup", 2000);
                        status = server.smb.doSessionSetup(this, pass1, pass2);
                    }
                    catch (NqException e) {
                        isException = true;
                        TraceLog.get().message("smb.doSessionSetup() threw exception: ", e, 2000);
                        TraceLog.get().caught(e);
                        throw e;
                    }
                    if (0 != status) continue;
                    if (0 != (server.capabilities & 1) && Client.isSigningEnabled() && !this.isAnonymous) {
                        this.useSigning = true;
                    }
                    this.isLoggedOn = true;
                    result = true;
                    TraceLog.get().exit("result = " + result, 300);
                    boolean e = result;
                    return e;
                }
                try {
                    TraceLog.get().message("Calling generatePasswordBlobs", 2000);
                    res = Authentications.generatePasswordBlobs(this.credentials, level, pass1, pass2, this.sessionKey, this.macSessionKey);
                }
                catch (NqException e) {
                    isException = true;
                    TraceLog.get().message("generatePasswordBlobs() threw exception: ", e, 2000);
                    TraceLog.get().caught(e);
                    throw e;
                }
                if (res == 0) {
                    try {
                        TraceLog.get().message("Calling doSessionSetup", 2000);
                        status = server.smb.doSessionSetup(this, pass1, pass2);
                    }
                    catch (NqException e) {
                        isException = true;
                        TraceLog.get().message("smb.doSessionSetup() threw exception: ", e, 2000);
                        TraceLog.get().caught(e);
                        throw e;
                    }
                    if (0 != status) continue;
                    if (0 != (server.capabilities & 1) && Client.isSigningEnabled() && !this.isAnonymous) {
                        this.useSigning = true;
                    }
                    this.isLoggedOn = true;
                    if (null != this.macSessionKey.data && this.macSessionKey.len > 16) {
                        this.macSessionKey.len = 16;
                    }
                    result = true;
                    Object object = this.loggingObj;
                    synchronized (object) {
                        this.isLoggingOn = false;
                        Server.releaseLock(server);
                        this.loggingObj.notify();
                    }
                    TraceLog.get().exit("result = " + result, 300);
                    boolean bl = result;
                    return bl;
                }
                isException = true;
                throw new NqException("Authentication refused", -22);
            }
        }
        finally {
            Object e = this.loggingObj;
            synchronized (e) {
                this.isLoggingOn = false;
                if (!result || isException) {
                    this.subtractFromNumberOfTimesLoggedOn();
                }
                Server.releaseLock(this.server);
                this.loggingObj.notify();
            }
        }
        TraceLog.get().exit("logon res : " + result, 300);
        return result;
    }

    protected void dump() {
        TraceLog.get().message(this.toString(), 1000);
    }

    protected void unlockCallback() throws NqException {
    }

    public String toString() {
        return " User [object=" + this.hashCode() + ", numberOfTimesLoggedOn=" + this.numberOfTimesLoggedOn + ", isLoggingOff=" + this.isLoggingOff + ", shares=" + this.shares + ", isLoggedOn=" + this.isLoggedOn + ", uid=" + this.uid + ", uuid=" + this.uuid + ", useSigning=" + this.useSigning + ", isLoggingOn=" + this.isLoggingOn + ", loggingObj=" + this.loggingObj + ", isAnonymous=" + this.isAnonymous + ", isGuest=" + this.isGuest + ", isEncrypted=" + this.isEncrypted + ", isPreauthIntegOn=" + this.isPreauthIntegOn + ", credentials=" + this.credentials + "]";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void logoff() throws NqException {
        Object object;
        TraceLog.get().enter(this.toString(), 300);
        if (!this.isLoggedOn) {
            TraceLog.get().exit(300);
            return;
        }
        this.subtractFromNumberOfTimesLoggedOn();
        while (true) {
            object = this.loggingObj;
            synchronized (object) {
                TraceLog.get().message("In synchronized block.", 2000);
                if (this.isLoggingOff || this.isLoggingOn || 0 < this.numberOfTimesLoggedOn()) {
                    this.loggingObj.notify();
                    TraceLog.get().exit(300);
                    return;
                }
                if (this.isLoggingOn) {
                    try {
                        TraceLog.get().message("Executing wait", 2000);
                        this.loggingObj.wait();
                        TraceLog.get().message("Woke up from wait", 2000);
                    }
                    catch (InterruptedException e) {
                        // empty catch block
                    }
                }
                if (!this.isLoggedOn) {
                    this.loggingObj.notify();
                    TraceLog.get().exit(300);
                    return;
                }
                if (Server.tryLock(this.server)) {
                    this.isLoggingOff = true;
                    break;
                }
                Server.identifyLock(this.server);
                try {
                    this.loggingObj.wait(100L);
                }
                catch (InterruptedException e) {
                    // empty catch block
                }
            }
        }
        try {
            Iterator shareItr = this.shares.values().iterator();
            while (shareItr.hasNext()) {
                block32: {
                    Share share = (Share)shareItr.next();
                    share.lock();
                    try {
                        share.shutdown();
                        share.unlock();
                    }
                    catch (NqException e) {
                        if (e.getErrCode() == -1073741819) break block32;
                        TraceLog.get().caught(e);
                        throw e;
                    }
                }
                shareItr.remove();
            }
            Object object2 = this.loggingObj;
            synchronized (object2) {
                if (this.isLoggedOn && this.numberOfTimesLoggedOn() <= 0) {
                    try {
                        if (this.server.transport.isConnected()) {
                            this.server.smb.doLogOff(this);
                        }
                    }
                    catch (NqException e) {
                        TraceLog.get().error("NqException = ", e, 2000, 0);
                        TraceLog.get().caught(e);
                    }
                    this.isLoggedOn = false;
                }
                User.checkRemoveUser(this.server, this.credentials.getKey());
                this.loggingObj.notify();
            }
        }
        finally {
            object = this.loggingObj;
            synchronized (object) {
                this.isLoggingOff = false;
                Server.releaseLock(this.server);
                this.loggingObj.notify();
            }
        }
        TraceLog.get().exit(300);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void cleanUp() throws NqException {
        TraceLog.get().enter(this.toString(), 300);
        if (!this.isLoggedOn) {
            TraceLog.get().exit(300);
            return;
        }
        Object object = this.loggingObj;
        synchronized (object) {
            this.subtractFromNumberOfTimesLoggedOn();
            if (this.isLoggingOff || this.isLoggingOn || 0 < this.numberOfTimesLoggedOn()) {
                TraceLog.get().exit(300);
                return;
            }
            this.isLoggingOff = true;
        }
        Iterator shareItr = this.shares.values().iterator();
        while (shareItr.hasNext()) {
            shareItr.next();
            shareItr.remove();
        }
        Server server = this.getServer();
        Object object2 = this.loggingObj;
        synchronized (object2) {
            if (null != server && this.isLoggedOn && this.numberOfTimesLoggedOn() <= 0) {
                this.isLoggingOff = false;
                this.isLoggingOn = false;
                this.loggingObj.notify();
                this.isLoggedOn = false;
            } else {
                this.isLoggingOff = false;
                this.isLoggingOn = false;
                this.loggingObj.notify();
            }
            User.checkRemoveUser(server, this.credentials.getKey());
        }
        TraceLog.get().exit(300);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setup() {
        Object object = this.loggingObj;
        synchronized (object) {
            this.isLoggedOn = false;
            this.uid = 0L;
            this.isLoggingOn = false;
            this.loggingObj.notify();
        }
    }

    public boolean connectShares() {
        return false;
    }

    public boolean reconnectShares(boolean doDfs) throws NqException {
        TraceLog.get().enter("doDfs = " + doDfs, 700);
        boolean res = false;
        for (Share share : this.shares.values()) {
            if (null == Share.connect(this, share.info.name, null, this.credentials, doDfs, new MountParams())) continue;
            share.reopenFiles();
            res = true;
        }
        TraceLog.get().exit("res = " + res, 700);
        return res;
    }

    public boolean useSignatures() {
        return !this.isAnonymous && !this.isGuest && this.useSigning;
    }

    public Credentials getCredentials() {
        return this.credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public Server getServer() {
        return this.server;
    }

    protected static User findUser(Server server, long uid) {
        NamedRepository users = server.users;
        Iterator iterator = users.values().iterator();
        User user = null;
        while (iterator.hasNext()) {
            User tmpUser = (User)iterator.next();
            if (uid != tmpUser.uid) continue;
            user = tmpUser;
            break;
        }
        TraceLog.get().message("Found user=", user, 2000);
        return user;
    }

    public int numberOfTimesLoggedOn() {
        return this.numberOfTimesLoggedOn;
    }

    protected synchronized void addToNumberOfTimesLoggedOn() {
        ++this.numberOfTimesLoggedOn;
    }

    public synchronized void subtractFromNumberOfTimesLoggedOn() {
        --this.numberOfTimesLoggedOn;
    }

    private class InternalSpnegoExchange
    implements Spnego.SpnegoClientExchange {
        private InternalSpnegoExchange() {
        }

        public int exchange(Object context, Blob outBlob, Blob inBlob) throws NqException {
            User user = (User)context;
            Server server = user.getServer();
            int res = -1073741823;
            Blob outBlobFragment = outBlob;
            int remainingLen = outBlob.len;
            int maxFragmentLen = user.getServer().maxTrans - 120;
            try {
                while (true) {
                    inBlob.data = null;
                    outBlobFragment.len = remainingLen > maxFragmentLen ? maxFragmentLen : remainingLen;
                    res = server.smb.doSessionSetupExtended(user, outBlobFragment, inBlob);
                    if (res != 0 && res != -1073741802) {
                        user.setup();
                        throw new SmbException("Logon of user failed during session setup with an error response.", res);
                    }
                    if (remainingLen != outBlobFragment.len) {
                        remainingLen -= maxFragmentLen;
                        outBlobFragment.data = new byte[outBlobFragment.len];
                        System.arraycopy(outBlobFragment.data, maxFragmentLen, outBlobFragment.data, 0, outBlobFragment.len);
                        continue;
                    }
                    break;
                }
            }
            catch (SmbException e) {
                user.setup();
                throw new SmbException("Dialect : " + Integer.toHexString(server.serverDialectRevision) + ", " + "Logon of user failed during session setup", e.getErrCode());
            }
            return User.this.convertSmbStatusIntoSpnegoStatus(res);
        }
    }
}

