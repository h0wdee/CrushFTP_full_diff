/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh;

import com.maverick.events.EventListener;
import com.maverick.events.EventServiceImplementation;
import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.DefaultSecurityPolicy;
import com.maverick.ssh.HostKeyVerification;
import com.maverick.ssh.IncompatibleAlgorithm;
import com.maverick.ssh.LicenseVerification;
import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SecurityPolicy;
import com.maverick.ssh.SocketTimeoutSupport;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshClientConnector;
import com.maverick.ssh.SshClientListener;
import com.maverick.ssh.SshContext;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshTransport;
import com.maverick.ssh.components.ComponentFactory;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh1.Ssh1Context;
import com.maverick.ssh2.Ssh2Context;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SshConnector
implements SshClientConnector {
    static Logger log = LoggerFactory.getLogger(SshConnector.class);
    public static final int SSH1 = 1;
    public static final int SSH2 = 2;
    private static final int SSH1_3 = 4;
    static LicenseVerification license = new LicenseVerification();
    String softwareComments;
    String originalSoftwareComments;
    Ssh1Context ssh1Context;
    Ssh2Context ssh2Context;
    int supported;
    String product;
    boolean fipsEnabled;
    public static Throwable initException = null;
    static ExecutorService globalExecutor;
    ExecutorService executor;
    SecurityPolicy securityPolicy;
    List<SshClientListener> listeners;
    static boolean loggedLicensedTo;
    int maximumErrors;
    int retryIntervalSeconds;
    int initialTimeout;

    SshConnector(ExecutorService executor, SecurityPolicy securityPolicy) throws SshException {
        block2: {
            this.softwareComments = "maverick_legacy_1.7.54-SNAPSHOT";
            this.originalSoftwareComments = "maverick_legacy_1.7.54-SNAPSHOT";
            this.supported = 0;
            this.product = "J2SSH";
            this.fipsEnabled = false;
            this.listeners = new ArrayList<SshClientListener>();
            this.maximumErrors = 10;
            this.retryIntervalSeconds = 10;
            this.initialTimeout = 30000;
            this.executor = executor;
            this.securityPolicy = securityPolicy;
            try {
                this.ssh1Context = new Ssh1Context();
                this.ssh1Context.setExecutorService(executor);
                this.supported |= 1;
            }
            catch (Throwable t) {
                if (!log.isInfoEnabled()) break block2;
                log.info("SSH1 is not supported", t);
            }
        }
        this.ssh2Context = new Ssh2Context(securityPolicy);
        this.ssh2Context.setExecutorService(executor);
        this.supported |= 2;
    }

    public synchronized void addListener(SshClientListener listener) {
        this.listeners.add(listener);
    }

    public synchronized void removeListener(SshClientListener listener) {
        this.listeners.remove(listener);
    }

    @Deprecated
    public static void setCoreThreadPoolSize(int coreThreadPoolSize) {
    }

    @Deprecated
    public static void setMaximumNumberThreads(int maximumNumberThreads) {
    }

    @Deprecated
    public static void setThreadTimeout(long threadTimeout) {
    }

    @Deprecated
    public static SshConnector createInstance() throws SshException {
        return SshConnector.createInstance(ComponentManager.getInstance().getSecurityLevel(), false);
    }

    @Deprecated
    public static SshConnector createInstance(ExecutorService executorService) throws SshException {
        return SshConnector.createInstance(executorService, ComponentManager.getInstance().getSecurityLevel(), false);
    }

    public static SshConnector createManagedInstance(SecurityLevel securityLevel) throws SshException {
        return SshConnector.createInstance(securityLevel, true);
    }

    public static SshConnector createInstance(SecurityLevel securityLevel, boolean managedSecurity) throws SshException {
        return SshConnector.createInstance(null, securityLevel, managedSecurity);
    }

    public static SshConnector createInstance(ExecutorService executor, SecurityLevel securityLevel, boolean managedSecurity) throws SshException {
        return SshConnector.createInstance(executor, new DefaultSecurityPolicy(securityLevel, managedSecurity));
    }

    public static SshConnector createInstance(SecurityPolicy securityPolicy) throws SshException {
        return SshConnector.createInstance(null, securityPolicy);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static SshConnector createInstance(ExecutorService executor, SecurityPolicy securityPolicy) throws SshException {
        Class<SshConnector> clazz = SshConnector.class;
        synchronized (SshConnector.class) {
            if (globalExecutor == null) {
                globalExecutor = Executors.newCachedThreadPool();
                Runtime.getRuntime().addShutdownHook(new Thread(){

                    @Override
                    public void run() {
                        globalExecutor.shutdown();
                    }
                });
            }
            // ** MonitorExit[var2_2] (shouldn't be in output)
            int status = license.verifyLicense();
            switch (status & 0x1F) {
                case 4: {
                    if (log.isInfoEnabled() && !loggedLicensedTo) {
                        log.info("This Maverick Legacy API product is licensed to " + license.getLicensee());
                        loggedLicensedTo = true;
                    }
                    return new SshConnector(executor == null ? globalExecutor : executor, securityPolicy);
                }
                case 2: {
                    throw new SshException("Your license is invalid!", 11);
                }
                case 1: {
                    throw new SshException("Your license has expired! visit http://www.sshtools.com to purchase a license", 11);
                }
                case 8: {
                    throw new SshException("This copy of Maverick Legacy Client is not licensed!", 11);
                }
                case 16: {
                    throw new SshException("Your subscription has expired! visit http://www.sshtools.com to purchase a subscription", 11);
                }
            }
            throw new SshException("Unexpected license status!", 11);
        }
    }

    public static void addEventListener(EventListener listener) {
        EventServiceImplementation.getInstance().addListener("", listener);
    }

    public static void addEventListener(String threadPrefix, EventListener listener) {
        EventServiceImplementation.getInstance().addListener(threadPrefix, listener);
    }

    public static void removeEventListener(String threadPrefix) {
        EventServiceImplementation.getInstance().removeListener(threadPrefix);
    }

    @Deprecated
    public final void enableFIPSMode() throws SshException {
        this.ssh1Context.enableFIPSMode();
        this.ssh2Context.enableFIPSMode();
        this.fipsEnabled = true;
    }

    public final boolean isLicensed() {
        return (license.getStatus() & 4) == 4;
    }

    static void addLicense(String license) {
        SshConnector.license.setLicense(license);
    }

    @Deprecated
    public SshContext getContext(int version) throws SshException {
        if ((version & 1) == 0 && (version & 2) == 0) {
            throw new SshException("SshContext.getContext(int) requires value of either SSH1 or SSH2", 4);
        }
        if (version == 1 && (this.supported & 1) != 0) {
            return this.ssh1Context;
        }
        if (version == 2 && (this.supported & 2) != 0) {
            return this.ssh2Context;
        }
        throw new SshException((version == 1 ? "SSH1" : "SSH2") + " context is not available because it is not supported by this configuration", 4);
    }

    public Ssh2Context getContext() throws SshException {
        return this.ssh2Context;
    }

    @Deprecated
    public void setSupportedVersions(int supported) throws SshException {
        if ((supported & 1) != 0 && this.ssh1Context == null) {
            throw new SshException("SSH1 protocol support is not installed!", 4);
        }
        if ((supported & 2) != 0 && this.ssh2Context == null) {
            throw new SshException("SSH2 protocol support is not installed!" + initException.getMessage() + initException.getClass(), 4);
        }
        if ((supported & 1) == 0 && (supported & 2) == 0) {
            throw new SshException("You must specify at least one supported version of the SSH protocol!", 4);
        }
        this.supported = supported;
    }

    @Deprecated
    public int getSupportedVersions() {
        return this.supported;
    }

    public void setKnownHosts(HostKeyVerification hkv) {
        if ((this.supported & 1) != 0 && this.ssh1Context != null) {
            this.ssh1Context.setHostKeyVerification(hkv);
        }
        if ((this.supported & 2) != 0 && this.ssh2Context != null) {
            this.ssh2Context.setHostKeyVerification(hkv);
        }
    }

    @Override
    public SshClient connect(SshTransport transport, String username) throws SshException {
        return this.connect(transport, username, false, null);
    }

    @Override
    public SshClient connect(SshTransport transport, String username, boolean buffered) throws SshException {
        return this.connect(transport, username, buffered, null);
    }

    @Override
    public SshClient connect(SshTransport transport, String username, SshContext context) throws SshException {
        return this.connect(transport, username, false, context);
    }

    public void setSoftwareVersionComments(String softwareComments) {
        this.softwareComments = softwareComments;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public SshClient connect(SshTransport transport, String username, boolean buffered, SshContext context) throws SshException {
        String remoteIdentification;
        Throwable lastError;
        block75: {
            SshClient client;
            String localIdentification;
            block71: {
                boolean valid;
                StringTokenizer tokens;
                String validHosts;
                if (log.isDebugEnabled()) {
                    log.debug("Connecting " + username + "@" + transport.getHost() + ":" + transport.getPort() + " [buffered=" + buffered + "]");
                }
                if (license.getType() == 65536 && transport.getHost() != "127.0.0.1" && transport.getHost() != "0:0:0:0:0:0:0:1" && transport.getHost() != "::1" && transport.getHost() != "localhost") {
                    validHosts = license.getComments();
                    tokens = new StringTokenizer(validHosts, ",");
                    valid = false;
                    while (tokens.hasMoreTokens() && !valid) {
                        String host = tokens.nextToken().trim();
                        if ((!host.startsWith("*.") || !transport.getHost().endsWith(host.substring(2))) && !host.equalsIgnoreCase(transport.getHost())) continue;
                        valid = true;
                    }
                    if (!valid) {
                        throw new SshException("You are not licensed to connect to " + transport.getHost() + " [VALID HOSTS " + validHosts + "]", 11);
                    }
                }
                if (license.getType() == 131072) {
                    Socket s;
                    block68: {
                        validHosts = license.getComments();
                        tokens = new StringTokenizer(validHosts, ",");
                        valid = false;
                        s = null;
                        try {
                            if (Socket.class.isAssignableFrom(transport.getClass())) {
                                s = (Socket)((Object)transport);
                                break block68;
                            }
                            if (transport.getClass().isAssignableFrom(Class.forName("com.sshtools.net.SocketWrapper"))) {
                                try {
                                    Method m = transport.getClass().getMethod("getSocket", new Class[0]);
                                    s = (Socket)m.invoke(transport, new Object[0]);
                                    break block68;
                                }
                                catch (Exception e) {
                                    throw new SshException("Error attempting to determine localhost for licensing: " + e.getMessage(), 11);
                                }
                            }
                            throw new SshException("You are not licensed to connect using non-socket transports", 11);
                        }
                        catch (ClassNotFoundException e) {
                            throw new SshException("Error attempting to determine localhost for licensing: " + e.getMessage(), 11);
                        }
                    }
                    if (s.getLocalAddress().getHostAddress() != "127.0.0.1" && s.getLocalAddress().getHostAddress() != "0:0:0:0:0:0:0:1" && s.getLocalAddress().getHostAddress() != "::1") {
                        while (tokens.hasMoreTokens() && !valid) {
                            String host = tokens.nextToken();
                            valid = s.getLocalAddress().getHostAddress().equals(host);
                        }
                        if (!valid) {
                            throw new SshException("You are not licensed to connect through " + s.getLocalAddress().getHostAddress() + " [VALID HOSTS " + validHosts + "]", 11);
                        }
                    }
                }
                lastError = null;
                localIdentification = null;
                remoteIdentification = null;
                boolean sentIdentification = false;
                if ((this.supported & 2) != 0 && (this.ssh2Context != null || context != null && context.getClass().getName().equals("com.maverick.ssh2.Ssh2Context"))) {
                    if (log.isDebugEnabled()) {
                        log.debug("Attempting SSH2 connection");
                    }
                    if ((localIdentification = "SSH-2.0-" + this.softwareComments.replace(' ', '_').replace('-', '_')).length() > 253) {
                        localIdentification = localIdentification.substring(0, 253);
                    }
                    localIdentification = localIdentification + "\r\n";
                    try {
                        transport.getOutputStream().write(localIdentification.getBytes());
                    }
                    catch (Throwable t) {
                        lastError = t;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Local identification: " + localIdentification.trim() + " [" + this.originalSoftwareComments + "]");
                    }
                    sentIdentification = true;
                    if (log.isDebugEnabled()) {
                        log.debug("Attempting to determine remote version");
                    }
                    if ((this.selectVersion(remoteIdentification = this.getRemoteIdentification(transport)) & 2) != 0) {
                        SshClient sshClient;
                        block69: {
                            block70: {
                                try {
                                    Ssh2Context theContext;
                                    client = (SshClient)Class.forName("com.maverick.ssh2.Ssh2Client").newInstance();
                                    for (SshClientListener sshClientListener : this.listeners) {
                                        client.addListener(sshClientListener);
                                    }
                                    if (log.isDebugEnabled()) {
                                        log.debug("Remote identification: " + remoteIdentification);
                                    }
                                    if (!(theContext = (Ssh2Context)(context == null ? this.ssh2Context : context)).getTemporaryValue("triedDHBackwardsCompatibility", false)) {
                                        theContext.setDHGroupExchangeBackwardsCompatible(AdaptiveConfiguration.getBoolean("useDHBackwardsCompatibility", theContext.isDHGroupExchangeBackwardsCompatible(), transport.getHost(), AdaptiveConfiguration.getIdent(remoteIdentification)));
                                    }
                                    client.connect(transport, theContext, this, username, localIdentification.trim(), remoteIdentification, buffered);
                                    sshClient = client;
                                    if (lastError == null) break block69;
                                }
                                catch (Throwable t) {
                                    block72: {
                                        try {
                                            log.error("Failed to create connection", t);
                                            lastError = t;
                                            if (lastError == null) break block71;
                                        }
                                        catch (Throwable throwable) {
                                            if (lastError != null) {
                                                block73: {
                                                    if (lastError instanceof SshException) {
                                                        SshException ex = (SshException)lastError;
                                                        try {
                                                            if (ex.getReason() == 57354) {
                                                                return this.connect(transport.duplicate(), username, buffered, context);
                                                            }
                                                            if (ex.getReason() == 57353) {
                                                                return this.processEOFDuringKeyExchangeException(ex, transport, username, buffered, context);
                                                            }
                                                            return this.processManagedSecurityException(ex, transport, username, buffered, context);
                                                        }
                                                        catch (SshException | IOException e) {
                                                            lastError = e;
                                                            if (!(lastError instanceof SshException)) break block73;
                                                            throw (SshException)lastError;
                                                        }
                                                    }
                                                }
                                                throw new SshException(lastError.getMessage() != null ? lastError.getMessage() : lastError.getClass().getName(), 10, lastError);
                                            }
                                            throw throwable;
                                        }
                                        if (lastError instanceof SshException) {
                                            SshException ex = (SshException)lastError;
                                            try {
                                                if (ex.getReason() == 57354) {
                                                    return this.connect(transport.duplicate(), username, buffered, context);
                                                }
                                                if (ex.getReason() == 57353) {
                                                    return this.processEOFDuringKeyExchangeException(ex, transport, username, buffered, context);
                                                }
                                                return this.processManagedSecurityException(ex, transport, username, buffered, context);
                                            }
                                            catch (SshException | IOException exception) {
                                                lastError = exception;
                                                if (!(lastError instanceof SshException)) break block72;
                                                throw (SshException)lastError;
                                            }
                                        }
                                    }
                                    throw new SshException(lastError.getMessage() != null ? lastError.getMessage() : lastError.getClass().getName(), 10, lastError);
                                }
                                if (lastError instanceof SshException) {
                                    SshException ex = (SshException)lastError;
                                    try {
                                        if (ex.getReason() == 57354) {
                                            return this.connect(transport.duplicate(), username, buffered, context);
                                        }
                                        if (ex.getReason() == 57353) {
                                            return this.processEOFDuringKeyExchangeException(ex, transport, username, buffered, context);
                                        }
                                        return this.processManagedSecurityException(ex, transport, username, buffered, context);
                                    }
                                    catch (SshException | IOException e) {
                                        lastError = e;
                                        if (!(lastError instanceof SshException)) break block70;
                                        throw (SshException)lastError;
                                    }
                                }
                            }
                            throw new SshException(lastError.getMessage() != null ? lastError.getMessage() : lastError.getClass().getName(), 10, lastError);
                        }
                        return sshClient;
                    }
                }
            }
            if (license.getType() == 262144) {
                throw new SshException("Failed to negotiate a version with the server! SSH1 is not supported by your license", 10);
            }
            try {
                SshTransport secondAttempt = transport.duplicate();
                try {
                    transport.close();
                }
                catch (Throwable throwable) {
                }
                finally {
                    transport = secondAttempt;
                }
                remoteIdentification = this.getRemoteIdentification(transport);
                int n = this.selectVersion(remoteIdentification);
                if ((n & 1) == 1) {
                    localIdentification = "SSH-1.5-" + this.softwareComments.replace(' ', '_') + "\n";
                } else if (n == 4) {
                    localIdentification = "SSH-1.3-" + this.softwareComments.replace(' ', '_') + "\n";
                } else {
                    throw new SshException("Failed to negotiate a version with the server! Attempted SSH1 but server returned SSH2", 10);
                }
                transport.getOutputStream().write(localIdentification.getBytes());
                if ((this.ssh1Context != null || context != null && context.getClass().getName().equals("com.maverick.ssh1.Ssh1Context")) && (this.supported & 1) != 0) {
                    client = (SshClient)Class.forName("com.maverick.ssh1.Ssh1Client").newInstance();
                    for (SshClientListener listener : this.listeners) {
                        client.addListener(listener);
                    }
                    if (this.softwareComments.length() > 40) {
                        this.softwareComments = this.softwareComments.substring(0, 40);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Remote identification: " + remoteIdentification);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Local identification: " + localIdentification.trim() + " [" + this.originalSoftwareComments + "]");
                    }
                    client.connect(transport, this.ssh1Context == null ? context : this.ssh1Context, this, username, localIdentification.trim(), remoteIdentification.trim(), buffered);
                    return client;
                }
            }
            catch (Throwable t) {
                lastError = t;
            }
            try {
                transport.close();
            }
            catch (IOException ex) {
                if (!log.isDebugEnabled()) break block75;
                log.debug("IOException when closing transport");
            }
        }
        if (lastError == null) {
            throw new SshException("Failed to negotiate a version with the server! supported=" + this.getSupportedVersions() + " id=" + (remoteIdentification == null ? "" : remoteIdentification), 10);
        }
        if (lastError instanceof SshException) {
            throw (SshException)lastError;
        }
        log.error("Connection failure", lastError);
        throw new SshException(lastError.getMessage() != null ? lastError.getMessage() : lastError.getClass().getName(), 10);
    }

    private SshClient processEOFDuringKeyExchangeException(SshException ex, SshTransport transport, String username, boolean buffered, SshContext context) throws SshException, IOException {
        for (IncompatibleAlgorithm alg : ex.getIncompatibleAlgorithms()) {
            alg.getComponentFactory().removeAllBut(alg.getRemoteAlgorithms()[0]);
        }
        return this.connect(transport.duplicate(), username, buffered, context);
    }

    private SshClient processManagedSecurityException(SshException ex, SshTransport transport, String username, boolean buffered, SshContext context) throws SshException, IOException {
        boolean retry = false;
        switch (ex.getReason()) {
            case 57345: {
                String component = ex.getComponent();
                this.ssh2Context.supportedCiphersCS().remove(component);
                this.ssh2Context.supportedCiphersSC().remove(component);
                retry = this.verifyPolicyRetry(this.ssh2Context.supportedCiphersCS(), this.ssh2Context.supportedCiphersSC());
                if (!log.isInfoEnabled()) break;
                log.info("Removed cipher {} due to failure", (Object)component);
                if (!retry) break;
                log.info("Remaining cipher are {}", (Object)this.ssh2Context.supportedCiphersCS().list());
                break;
            }
            case 57346: {
                String component = ex.getComponent();
                this.ssh2Context.supportedMacsCS().remove(component);
                this.ssh2Context.supportedMacsSC().remove(component);
                retry = this.verifyPolicyRetry(this.ssh2Context.supportedMacsCS(), this.ssh2Context.supportedMacsSC());
                if (!log.isInfoEnabled()) break;
                log.info("Removed MAC {} due to failure", (Object)component);
                if (!retry) break;
                log.info("Remaining MACs are {}", (Object)this.ssh2Context.supportedMacsCS().list());
                break;
            }
            case 57348: {
                String component = ex.getComponent();
                this.ssh2Context.supportedCompressionsCS().remove(component);
                this.ssh2Context.supportedCompressionsSC().remove(component);
                retry = this.ssh2Context.supportedCompressionsCS().hasComponents();
                if (!log.isInfoEnabled()) break;
                log.info("Removed compression {} due to failure", (Object)component);
                if (!retry) break;
                log.info("Remaining compressions are {}", (Object)this.ssh2Context.supportedCompressionsCS().list());
                break;
            }
            case 57347: {
                String component = ex.getComponent();
                this.ssh2Context.supportedKeyExchanges().remove(component);
                retry = this.verifyPolicyRetry(this.ssh2Context.supportedKeyExchanges());
                if (!log.isInfoEnabled()) break;
                log.info("Removed key exchange {} due to failure", (Object)component);
                if (!retry) break;
                log.info("Remaining key exchanges are {}", (Object)this.ssh2Context.supportedKeyExchanges().list());
                break;
            }
            case 57349: {
                String component = ex.getComponent();
                this.ssh2Context.supportedPublicKeys().remove(component);
                retry = this.verifyPolicyRetry(this.ssh2Context.supportedPublicKeys());
                if (!log.isInfoEnabled()) break;
                log.info("Removed public key type {} due to failure", (Object)component);
                if (!retry) break;
                log.info("Remaining public key types are {}", (Object)this.ssh2Context.supportedPublicKeys().list());
                break;
            }
            case 57350: {
                if (!this.ssh2Context.getSecurityPolicy().isDropSecurityAsLastResort()) break;
                for (IncompatibleAlgorithm alg : ex.getIncompatibleAlgorithms()) {
                    if (log.isWarnEnabled()) {
                        log.warn("Attempting to drop {} security for component {}", (Object)alg.getComponentFactory().getSecurityLevel().name(), (Object)alg.getType().name());
                    }
                    SecurityLevel next = this.nextSecurityLevel(alg.getComponentFactory().getSecurityLevel());
                    if (log.isWarnEnabled()) {
                        log.warn("Dropped security from {} to {} for component {}", new Object[]{alg.getComponentFactory().getSecurityLevel().name(), next.name(), alg.getType().name()});
                    }
                    alg.getComponentFactory().configureSecurityLevel(next);
                    retry = true;
                }
                break;
            }
        }
        if (retry) {
            return this.connect(transport.duplicate(), username, buffered, context);
        }
        throw ex;
    }

    private SecurityLevel nextSecurityLevel(SecurityLevel securityLevel) throws SshException {
        switch (securityLevel) {
            case PARANOID: {
                return SecurityLevel.STRONG;
            }
            case STRONG: {
                return SecurityLevel.WEAK;
            }
        }
        throw new SshException("Cannot negotiate algorithm or drop security level any further.", 10);
    }

    private boolean verifyPolicyRetry(ComponentFactory<?> factoryCS) throws SshException {
        return this.verifyPolicyRetry(factoryCS, null);
    }

    private boolean verifyPolicyRetry(ComponentFactory<?> factoryCS, ComponentFactory<?> factorySC) throws SshException {
        if (!factoryCS.hasComponents()) {
            if (factoryCS.getSecurityLevel().ordinal() > SecurityLevel.WEAK.ordinal() && this.ssh2Context.getSecurityPolicy().isDropSecurityAsLastResort()) {
                factoryCS.configureSecurityLevel(this.nextSecurityLevel(factoryCS.getSecurityLevel()));
                if (factorySC != null) {
                    factorySC.configureSecurityLevel(this.nextSecurityLevel(factoryCS.getSecurityLevel()));
                }
                return true;
            }
            return false;
        }
        return true;
    }

    public static String getVersion() {
        return "1.7.54-SNAPSHOT";
    }

    public static Date getReleaseDate() {
        return new Date(1697127759215L);
    }

    public int determineVersion(SshTransport transport) throws SshException {
        int version = this.selectVersion(this.getRemoteIdentification(transport));
        try {
            transport.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return version;
    }

    public int getInitialTimeout() {
        return this.initialTimeout;
    }

    public void setInitialTimeout(int initialTimeout) {
        this.initialTimeout = initialTimeout;
    }

    String getRemoteIdentification(SshTransport transport) throws SshException {
        try {
            int timeout = 0;
            if (transport instanceof SocketTimeoutSupport && (timeout = ((SocketTimeoutSupport)((Object)transport)).getSoTimeout()) == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Temporarily setting socket timeout to 30000ms for remote identification exchange");
                }
                ((SocketTimeoutSupport)((Object)transport)).setSoTimeout(this.initialTimeout);
            }
            String remoteIdentification = "";
            InputStream in = transport.getInputStream();
            int MAX_BUFFER_LENGTH = 255;
            while (!remoteIdentification.startsWith("SSH-")) {
                int ch;
                StringBuffer lineBuffer = new StringBuffer(MAX_BUFFER_LENGTH);
                while ((ch = in.read()) != 10 && lineBuffer.length() < MAX_BUFFER_LENGTH && ch > -1) {
                    if (ch == 13) continue;
                    lineBuffer.append((char)ch);
                }
                if (ch == -1) {
                    throw new SshException("Failed to read remote identification " + lineBuffer.toString(), 10);
                }
                remoteIdentification = lineBuffer.toString();
            }
            if (transport instanceof SocketTimeoutSupport) {
                ((SocketTimeoutSupport)((Object)transport)).setSoTimeout(timeout);
            }
            return remoteIdentification;
        }
        catch (Throwable ex) {
            throw new SshException(ex, 10);
        }
    }

    int selectVersion(String remoteIdentification) throws SshException {
        int l = remoteIdentification.indexOf("-");
        int r = remoteIdentification.indexOf("-", l + 1);
        if (l == -1) {
            throw new SshException("Unexpected remote identification value " + remoteIdentification, 3);
        }
        String remoteVersion = remoteIdentification.substring(l + 1, r == -1 ? remoteIdentification.length() : r);
        if (remoteVersion.equals("2.0")) {
            return 2;
        }
        if (remoteVersion.equals("1.99")) {
            return 3;
        }
        if (remoteVersion.equals("1.5")) {
            return 1;
        }
        if (remoteVersion.equals("1.3")) {
            return 4;
        }
        if (remoteVersion.equals("2.99")) {
            return 2;
        }
        throw new SshException("Unsupported version " + remoteVersion + " detected!", 10);
    }

    public String getProduct() {
        return this.product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    static {
        loggedLicensedTo = false;
    }
}

