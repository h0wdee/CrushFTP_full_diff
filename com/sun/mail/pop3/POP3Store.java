/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.pop3;

import com.sun.mail.pop3.DefaultFolder;
import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.pop3.Protocol;
import com.sun.mail.util.MailConnectException;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.PropUtil;
import com.sun.mail.util.SocketConnectException;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class POP3Store
extends Store {
    private String name = "pop3";
    private int defaultPort = 110;
    private boolean isSSL = false;
    private Protocol port = null;
    private POP3Folder portOwner = null;
    private String host = null;
    private int portNum = -1;
    private String user = null;
    private String passwd = null;
    private boolean useStartTLS = false;
    private boolean requireStartTLS = false;
    private boolean usingSSL = false;
    private Map<String, String> capabilities;
    private MailLogger logger;
    volatile Constructor<?> messageConstructor = null;
    volatile boolean rsetBeforeQuit = false;
    volatile boolean disableTop = false;
    volatile boolean forgetTopHeaders = false;
    volatile boolean supportsUidl = true;
    volatile boolean cacheWriteTo = false;
    volatile boolean useFileCache = false;
    volatile File fileCacheDir = null;
    volatile boolean keepMessageContent = false;
    volatile boolean finalizeCleanClose = false;

    public POP3Store(Session session, URLName url) {
        this(session, url, "pop3", false);
    }

    public POP3Store(Session session, URLName url, String name, boolean isSSL) {
        super(session, url);
        if (url != null) {
            name = url.getProtocol();
        }
        this.name = name;
        this.logger = new MailLogger(this.getClass(), "DEBUG POP3", session);
        if (!isSSL) {
            isSSL = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".ssl.enable", false);
        }
        this.defaultPort = isSSL ? 995 : 110;
        this.isSSL = isSSL;
        this.rsetBeforeQuit = this.getBoolProp("rsetbeforequit");
        this.disableTop = this.getBoolProp("disabletop");
        this.forgetTopHeaders = this.getBoolProp("forgettopheaders");
        this.cacheWriteTo = this.getBoolProp("cachewriteto");
        this.useFileCache = this.getBoolProp("filecache.enable");
        String dir = session.getProperty("mail." + name + ".filecache.dir");
        if (dir != null && this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("mail." + name + ".filecache.dir: " + dir);
        }
        if (dir != null) {
            this.fileCacheDir = new File(dir);
        }
        this.keepMessageContent = this.getBoolProp("keepmessagecontent");
        this.useStartTLS = this.getBoolProp("starttls.enable");
        this.requireStartTLS = this.getBoolProp("starttls.required");
        this.finalizeCleanClose = this.getBoolProp("finalizecleanclose");
        String s = session.getProperty("mail." + name + ".message.class");
        if (s != null) {
            this.logger.log(Level.CONFIG, "message class: {0}", s);
            try {
                ClassLoader cl = this.getClass().getClassLoader();
                Class<?> messageClass = null;
                try {
                    messageClass = Class.forName(s, false, cl);
                }
                catch (ClassNotFoundException ex1) {
                    messageClass = Class.forName(s);
                }
                Class[] c = new Class[]{Folder.class, Integer.TYPE};
                this.messageConstructor = messageClass.getConstructor(c);
            }
            catch (Exception ex) {
                this.logger.log(Level.CONFIG, "failed to load message class", ex);
            }
        }
    }

    private final synchronized boolean getBoolProp(String prop) {
        prop = "mail." + this.name + "." + prop;
        boolean val = PropUtil.getBooleanSessionProperty(this.session, prop, false);
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config(prop + ": " + val);
        }
        return val;
    }

    synchronized Session getSession() {
        return this.session;
    }

    @Override
    protected synchronized boolean protocolConnect(String host, int portNum, String user, String passwd) throws MessagingException {
        if (host == null || passwd == null || user == null) {
            return false;
        }
        if (portNum == -1) {
            portNum = PropUtil.getIntSessionProperty(this.session, "mail." + this.name + ".port", -1);
        }
        if (portNum == -1) {
            portNum = this.defaultPort;
        }
        this.host = host;
        this.portNum = portNum;
        this.user = user;
        this.passwd = passwd;
        try {
            this.port = this.getPort(null);
        }
        catch (EOFException eex) {
            throw new AuthenticationFailedException(eex.getMessage());
        }
        catch (SocketConnectException scex) {
            throw new MailConnectException(scex);
        }
        catch (IOException ioex) {
            throw new MessagingException("Connect failed", ioex);
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized boolean isConnected() {
        if (!super.isConnected()) {
            return false;
        }
        try {
            if (this.port == null) {
                this.port = this.getPort(null);
            } else if (!this.port.noop()) {
                throw new IOException("NOOP failed");
            }
            return true;
        }
        catch (IOException ioex) {
            try {
                try {
                    super.close();
                }
                catch (MessagingException messagingException) {
                    Object var4_3 = null;
                    return false;
                }
                Object var4_2 = null;
                return false;
            }
            catch (Throwable throwable) {
                Object var4_4 = null;
                return false;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    synchronized Protocol getPort(POP3Folder owner) throws IOException {
        Protocol p;
        block21: {
            block22: {
                if (this.port != null && this.portOwner == null) {
                    this.portOwner = owner;
                    return this.port;
                }
                p = new Protocol(this.host, this.portNum, this.logger, this.session.getProperties(), "mail." + this.name, this.isSSL);
                if (!this.useStartTLS && !this.requireStartTLS) break block21;
                if (!p.hasCapability("STLS")) break block22;
                if (p.stls()) {
                    p.setCapabilities(p.capa());
                    break block21;
                } else if (this.requireStartTLS) {
                    this.logger.fine("STLS required but failed");
                    try {
                        try {
                            p.quit();
                        }
                        catch (IOException iOException) {
                            Object var5_4 = null;
                            throw new EOFException("STLS required but failed");
                        }
                        Object var5_3 = null;
                        throw new EOFException("STLS required but failed");
                    }
                    catch (Throwable throwable) {
                        Object var5_5 = null;
                        throw new EOFException("STLS required but failed");
                    }
                }
                break block21;
            }
            if (this.requireStartTLS) {
                this.logger.fine("STLS required but not supported");
                try {
                    try {
                        p.quit();
                    }
                    catch (IOException iOException) {
                        Object var7_12 = null;
                        throw new EOFException("STLS required but not supported");
                    }
                    Object var7_11 = null;
                    throw new EOFException("STLS required but not supported");
                }
                catch (Throwable throwable) {
                    Object var7_13 = null;
                    throw new EOFException("STLS required but not supported");
                }
            }
        }
        this.capabilities = p.getCapabilities();
        this.usingSSL = p.isSSL();
        if (!this.disableTop && this.capabilities != null && !this.capabilities.containsKey("TOP")) {
            this.disableTop = true;
            this.logger.fine("server doesn't support TOP, disabling it");
        }
        this.supportsUidl = this.capabilities == null || this.capabilities.containsKey("UIDL");
        String msg = null;
        msg = p.login(this.user, this.passwd);
        if (msg != null) {
            try {
                try {
                    p.quit();
                }
                catch (IOException iOException) {
                    Object var9_16 = null;
                    throw new EOFException(msg);
                }
                Object var9_15 = null;
                throw new EOFException(msg);
            }
            catch (Throwable throwable) {
                Object var9_17 = null;
                throw new EOFException(msg);
            }
        }
        if (this.port == null && owner != null) {
            this.port = p;
            this.portOwner = owner;
        }
        if (this.portOwner != null) return p;
        this.portOwner = owner;
        return p;
    }

    synchronized void closePort(POP3Folder owner) {
        if (this.portOwner == owner) {
            this.port = null;
            this.portOwner = null;
        }
    }

    @Override
    public synchronized void close() throws MessagingException {
        this.close(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    synchronized void close(boolean force) throws MessagingException {
        try {
            block5: {
                try {
                    if (this.port == null) break block5;
                    if (force) {
                        this.port.close();
                        break block5;
                    }
                    this.port.quit();
                }
                catch (IOException iOException) {
                    Object var4_3 = null;
                    this.port = null;
                    super.close();
                    return;
                }
            }
            Object var4_2 = null;
            this.port = null;
            super.close();
            return;
        }
        catch (Throwable throwable) {
            Object var4_4 = null;
            this.port = null;
            super.close();
            throw throwable;
        }
    }

    @Override
    public Folder getDefaultFolder() throws MessagingException {
        this.checkConnected();
        return new DefaultFolder(this);
    }

    @Override
    public Folder getFolder(String name) throws MessagingException {
        this.checkConnected();
        return new POP3Folder(this, name);
    }

    @Override
    public Folder getFolder(URLName url) throws MessagingException {
        this.checkConnected();
        return new POP3Folder(this, url.getFile());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Map<String, String> capabilities() throws MessagingException {
        Map<String, String> c;
        POP3Store pOP3Store = this;
        synchronized (pOP3Store) {
            c = this.capabilities;
        }
        if (c != null) {
            return Collections.unmodifiableMap(c);
        }
        return Collections.emptyMap();
    }

    public synchronized boolean isSSL() {
        return this.usingSSL;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            if (this.port != null) {
                this.close(!this.finalizeCleanClose);
            }
            Object var2_1 = null;
        }
        catch (Throwable throwable) {
            Object var2_2 = null;
            super.finalize();
            throw throwable;
        }
        super.finalize();
    }

    private void checkConnected() throws MessagingException {
        if (!super.isConnected()) {
            throw new MessagingException("Not connected");
        }
    }
}

