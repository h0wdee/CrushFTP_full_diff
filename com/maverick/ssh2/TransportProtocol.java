/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh2;

import com.maverick.events.Event;
import com.maverick.events.EventServiceImplementation;
import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.ExecutorOperationSupport;
import com.maverick.ssh.IncompatibleAlgorithm;
import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SocketTimeoutSupport;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshTransport;
import com.maverick.ssh.components.ComponentFactory;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.Digest;
import com.maverick.ssh.components.SshCipher;
import com.maverick.ssh.components.SshHmac;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.Utils;
import com.maverick.ssh.components.jce.ChaCha20Poly1305;
import com.maverick.ssh.compression.SshCompression;
import com.maverick.ssh.message.SshMessageReader;
import com.maverick.ssh2.AbstractClientTransport;
import com.maverick.ssh2.Ssh2Client;
import com.maverick.ssh2.Ssh2Context;
import com.maverick.ssh2.SshKeyExchangeClient;
import com.maverick.ssh2.TransportProtocolListener;
import com.maverick.util.Arrays;
import com.maverick.util.Base64;
import com.maverick.util.BinaryLogger;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import com.maverick.util.IOUtil;
import com.maverick.util.SshKeyUtils;
import com.maverick.util.UnsignedInteger64;
import com.sshtools.publickey.KnownHostsKeyVerification;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransportProtocol
extends ExecutorOperationSupport<Ssh2Context>
implements SshMessageReader,
AbstractClientTransport {
    static Logger log = LoggerFactory.getLogger(TransportProtocol.class);
    public static String CHARSET_ENCODING = "UTF8";
    DataInputStream transportIn;
    OutputStream transportOut;
    SshTransport provider;
    Ssh2Context transportContext;
    Ssh2Client client;
    String localIdentification;
    String remoteIdentification;
    byte[] localkex;
    byte[] remotekex;
    byte[] sessionIdentifier;
    static final int SSH_MSG_DISCONNECT = 1;
    static final int SSH_MSG_IGNORE = 2;
    static final int SSH_MSG_UNIMPLEMENTED = 3;
    static final int SSH_MSG_DEBUG = 4;
    static final int SSH_MSG_SERVICE_REQUEST = 5;
    static final int SSH_MSG_SERVICE_ACCEPT = 6;
    static final int SSH_MSG_EXT_INFO = 7;
    static final int SSH_MSG_KEX_INIT = 20;
    static final int SSH_MSG_NEWKEYS = 21;
    public static final int NEGOTIATING_PROTOCOL = 1;
    public static final int PERFORMING_KEYEXCHANGE = 2;
    public static final int CONNECTED = 3;
    public static final int DISCONNECTED = 4;
    int currentState;
    int lastState;
    Throwable lastError;
    String disconnectReason;
    boolean managedSecurity;
    SshKeyExchangeClient keyExchange;
    SshKeyExchangeClient guessedKeyExchange;
    SshCipher encryption;
    SshCipher decryption;
    SshHmac outgoingMac;
    SshHmac incomingMac;
    SshCompression outgoingCompression;
    SshCompression incomingCompression;
    SshPublicKey hostkey;
    boolean isIncomingCompressing = false;
    boolean isOutgoingCompressing = false;
    int outgoingCipherLength = 8;
    int outgoingMacLength = 0;
    boolean ignoreHostKeyifEmpty = false;
    byte[] incomingMessage;
    ByteArrayWriter outgoingMessage;
    int incomingCipherLength = 8;
    int incomingMacLength = 0;
    long outgoingSequence = 0L;
    long incomingSequence = 0L;
    int numIncomingBytesSinceKEX;
    int numIncomingPacketsSinceKEX;
    int numOutgoingBytesSinceKEX;
    int numOutgoingPacketsSinceKEX;
    long outgoingBytes = 0L;
    long incomingBytes = 0L;
    List<byte[]> outgoingKexQueue = new ArrayList<byte[]>();
    List<byte[]> incomingKexQueue = new ArrayList<byte[]>();
    List<Runnable> shutdownHooks = new ArrayList<Runnable>();
    List<TransportProtocolListener> listeners = new ArrayList<TransportProtocolListener>();
    long lastActivity = System.currentTimeMillis();
    public static final int HOST_NOT_ALLOWED = 1;
    public static final int PROTOCOL_ERROR = 2;
    public static final int KEY_EXCHANGE_FAILED = 3;
    public static final int RESERVED = 4;
    public static final int MAC_ERROR = 5;
    public static final int COMPRESSION_ERROR = 6;
    public static final int SERVICE_NOT_AVAILABLE = 7;
    public static final int PROTOCOL_VERSION_NOT_SUPPORTED = 8;
    public static final int HOST_KEY_NOT_VERIFIABLE = 9;
    public static final int CONNECTION_LOST = 10;
    public static final int BY_APPLICATION = 11;
    public static final int TOO_MANY_CONNECTIONS = 12;
    public static final int AUTH_CANCELLED_BY_USER = 13;
    public static final int NO_MORE_AUTH_METHODS_AVAILABLE = 14;
    public static final int ILLEGAL_USER_NAME = 15;
    String remoteKeyExchanges;
    String remotePublicKeys;
    String remoteCiphersCS;
    String remoteCiphersSC;
    String remoteMacsCS;
    String remoteMacsSC;
    String remoteCompressionsCS;
    String remoteCompressionsSC;
    String hostKeyAlg = "<unknown>";
    String keyExchangeAlg = "<unknown>";
    String cipherCS = "<unknown>";
    String cipherSC = "<unknown>";
    String macCS = "<unknown>";
    String macSC = "<unknown>";
    String compressionSC = "<unknown>";
    String compressionCS = "<unknown>";
    boolean verbose;
    boolean binary;
    boolean buffered;
    long lastIdle;
    boolean disableAutoFlush;
    List<String> ignoreMacs = new ArrayList<String>();
    List<String> ignoreCiphers = new ArrayList<String>();
    List<String> ignoreCompressions = new ArrayList<String>();
    List<String> ignorePublicKeys = new ArrayList<String>();
    List<String> ignoreKeyExchanges = new ArrayList<String>();
    private String localCompressionsSC;
    private String localCompressionsCS;
    private String localMacsCS;
    private String localMacsSC;
    private String localCiphersSC;
    private String localCiphersCS;
    private String localPublicKeys;
    private String localKeyExchanges;
    private String ident;
    private int maximumPacketLength;
    private boolean keyReExchangeDisabled;
    private long idleConnectionTimeout;
    private long idleAuthenticationTimeout;
    private boolean treatIdleConnectionAsError;
    private boolean sendIgnoreOnIdle;
    private int keepAliveDataLength;
    private int socketTimeout;
    private long numPacketsBeforeRekey;
    private long numBytesBeforeRekey;
    private boolean disableIdleProcessDuringKeyExchange = false;
    private boolean disableKEXProtocolWorkaround;
    private static Object signatureLock = new Object();
    BinaryLogger binaryInput;
    BinaryLogger binaryOutput;
    UUID uuid = UUID.randomUUID();
    private boolean supportsExtensionNegotiation;

    public TransportProtocol(boolean buffered, boolean managedSecurity) {
        this.buffered = buffered;
        this.managedSecurity = managedSecurity;
    }

    public SshTransport getProvider() {
        return this.provider;
    }

    public void addListener(TransportProtocolListener listener) {
        this.listeners.add(listener);
    }

    public Ssh2Client getClient() {
        return this.client;
    }

    @Override
    protected String getName() {
        return String.format("Transport Protocol %s", this.getUuid());
    }

    @Override
    public boolean isConnected() {
        return this.currentState == 3 || this.currentState == 2;
    }

    public Throwable getLastError() {
        return this.lastError;
    }

    @Override
    public Ssh2Context getContext() {
        return this.transportContext;
    }

    public boolean getIgnoreHostKeyifEmpty() {
        return this.ignoreHostKeyifEmpty;
    }

    public void setIgnoreHostKeyifEmpty(boolean ignoreHostKeyifEmpty) {
        this.ignoreHostKeyifEmpty = ignoreHostKeyifEmpty;
    }

    @Override
    public String getHostname() {
        return this.provider.getHost();
    }

    int getInt(String key, int defaultValue) {
        return AdaptiveConfiguration.getInt(key, defaultValue, this.getHost(), this.getIdent());
    }

    long getLong(String key, long defaultValue) {
        return AdaptiveConfiguration.getLong(key, defaultValue, this.getHost(), this.getIdent());
    }

    boolean getBoolean(String key, boolean defaultValue) {
        return AdaptiveConfiguration.getBoolean(key, defaultValue, this.getHost(), this.getIdent());
    }

    String getString(String key, String defaultValue) {
        return AdaptiveConfiguration.getProperty(key, defaultValue, this.getHost(), this.getIdent());
    }

    public void startTransportProtocol(SshTransport provider, Ssh2Context context, String localIdentification, String remoteIdentification, Ssh2Client client) throws SshException {
        try {
            this.transportIn = new DataInputStream(provider.getInputStream());
            this.transportOut = provider.getOutputStream();
            this.provider = provider;
            this.localIdentification = localIdentification;
            this.remoteIdentification = remoteIdentification;
            this.transportContext = context;
            this.client = client;
            this.ident = AdaptiveConfiguration.getIdent(remoteIdentification);
            this.verbose = AdaptiveConfiguration.getBoolean("verbose", false, provider.getHost(), this.ident);
            this.binary = AdaptiveConfiguration.getBoolean("binary", false, provider.getHost(), this.ident);
            this.disableAutoFlush = AdaptiveConfiguration.getBoolean("disableAutoFlush", false, provider.getHost(), this.ident);
            this.disableKEXProtocolWorkaround = AdaptiveConfiguration.getBoolean("disableKEXProtocolViolationWorkaround", false, provider.getHost(), this.getIdent());
            this.maximumPacketLength = this.getInt("maximumPacketLength", context.getMaximumPacketLength());
            this.incomingMessage = new byte[this.maximumPacketLength];
            this.outgoingMessage = new ByteArrayWriter(this.maximumPacketLength);
            this.keyReExchangeDisabled = this.getBoolean("disableKeyReExchange", this.transportContext.isKeyReExchangeDisabled());
            this.idleConnectionTimeout = this.getLong("idleConnectionTimeoutSeconds", this.transportContext.getIdleConnectionTimeoutSeconds());
            this.idleAuthenticationTimeout = this.getLong("idleAuthenticationTimeoutSeconds", this.transportContext.getIdleAuthenticationTimeoutSeconds());
            this.treatIdleConnectionAsError = this.getBoolean("treatIdleConnectionAsError", this.getContext().isTreatIdleConnectionAsError());
            this.sendIgnoreOnIdle = this.getBoolean("sendIgnoreOnIdle", this.getContext().isSendIgnorePacketOnIdle());
            this.keepAliveDataLength = this.getInt("keepAliveDataLength", this.getContext().getKeepAliveMaxDataLength());
            this.socketTimeout = this.getInt("socketTimeout", this.transportContext.getSocketTimeout());
            this.numBytesBeforeRekey = IOUtil.fromByteSize(this.getString("maxNumBytesBeforeRekey", String.valueOf(this.transportContext.getMaxNumBytesBeforeReKey())));
            this.numPacketsBeforeRekey = IOUtil.fromByteSize(this.getString("maxNumPacketsBeforeRekey", String.valueOf(this.transportContext.getMaxNumPacketsBeforeReKey())));
            this.disableIdleProcessDuringKeyExchange = this.getBoolean("disableIdleProcessDuringKeyExchange", false);
            this.supportsExtensionNegotiation = this.getBoolean("SupportsExtensionNegotiation", true);
            if (context.getSocketTimeout() > 0 && provider instanceof SocketTimeoutSupport) {
                ((SocketTimeoutSupport)((Object)provider)).setSoTimeout(context.getSocketTimeout());
            } else if (context.getSocketTimeout() > 0 && log.isDebugEnabled()) {
                this.debug("Socket timeout is set on SshContext but SshTransport does not support socket timeouts", new Object[0]);
            }
            if (AdaptiveConfiguration.getBoolean("binaryLogging", false, provider.getHost(), this.ident)) {
                try {
                    this.binaryInput = new BinaryLogger(this.uuid.toString() + "-ssh.in");
                }
                catch (FileNotFoundException e) {
                    log.error("Cannot create binary input file", (Throwable)e);
                }
                try {
                    this.binaryOutput = new BinaryLogger(this.uuid.toString() + "-ssh.out");
                }
                catch (FileNotFoundException e) {
                    log.error("Cannot create binary output file", (Throwable)e);
                }
            }
            this.generateAlgorithms();
            this.currentState = 1;
            this.sendKeyExchangeInit(false);
            if (log.isDebugEnabled()) {
                this.debug("Waiting for transport protocol to complete initialization", new Object[0]);
            }
            while (this.processMessage(this.readMessage(0L)) && this.currentState != 3) {
            }
        }
        catch (IOException ex) {
            throw new SshException(ex, 10);
        }
        if (log.isDebugEnabled()) {
            this.debug("Transport protocol initialized", new Object[0]);
        }
    }

    private void generateAlgorithms() throws SshException {
        if (!this.getBoolean("enableETM", this.transportContext.isEnableETM())) {
            this.ignoreMacs.add("hmac-md5-etm@openssh.com");
            this.ignoreMacs.add("hmac-ripemd160-etm@openssh.com");
            this.ignoreMacs.add("hmac-sha1-etm@openssh.com");
            this.ignoreMacs.add("hmac-sha2-256-etm@openssh.com");
            this.ignoreMacs.add("hmac-sha2-512-etm@openssh.com");
        }
        if (this.getContext().getSecurityPolicy().getMinimumSecurityLevel().ordinal() < SecurityLevel.PARANOID.ordinal() && !this.getBoolean("enableNonStandardAlgorithms", this.transportContext.isNonStandardAlgorithmsEnabled())) {
            for (String alg : this.transportContext.supportedMacsCS().names()) {
                if (!alg.contains("@")) continue;
                this.ignoreMacs.add(alg);
            }
            for (String alg : this.transportContext.supportedCiphersCS().names()) {
                if (!alg.contains("@")) continue;
                this.ignoreCiphers.add(alg);
            }
            for (String alg : this.transportContext.supportedCompressionsCS().names()) {
                if (!alg.contains("@")) continue;
                this.ignoreCompressions.add(alg);
            }
            for (String alg : this.transportContext.supportedPublicKeys().names()) {
                if (!alg.contains("@")) continue;
                this.ignorePublicKeys.add(alg);
            }
            for (String alg : this.transportContext.supportedKeyExchanges().names()) {
                if (!alg.contains("@")) continue;
                this.ignoreKeyExchanges.add(alg);
            }
        }
        this.localKeyExchanges = AdaptiveConfiguration.createAlgorithmList(this.transportContext.supportedKeyExchanges(), "Kex", this.checkManagedSecurity(this.getString("preferredKeyExchange", this.transportContext.getPreferredKeyExchange())), this.ident, this.provider.getHost(), this.ignoreKeyExchanges.toArray(new String[0]));
        if (AdaptiveConfiguration.getBoolean("limitPublicKeysToKnownHosts", this.transportContext.isLimitPublicKeysToKnownHosts(), this.ident, this.provider.getHost()) && this.transportContext.getHostKeyVerification() instanceof KnownHostsKeyVerification) {
            KnownHostsKeyVerification v = (KnownHostsKeyVerification)this.transportContext.getHostKeyVerification();
            Set<String> knownHosts = v.getAlgorithmsForHost(this.getHost());
            if (knownHosts.isEmpty()) {
                throw new SshException("Context is limiting host keys to only known_hosts but there are no known keys for this server", 57349);
            }
            for (String alg : this.transportContext.supportedPublicKeys().names()) {
                if (knownHosts.contains(alg)) continue;
                this.ignorePublicKeys.add(alg);
            }
        }
        this.localPublicKeys = AdaptiveConfiguration.createAlgorithmList(this.transportContext.supportedPublicKeys(), "Publickeys", this.checkManagedSecurity(this.getString("preferredPublicKey", this.transportContext.getPreferredPublicKey())), this.ident, this.provider.getHost(), this.ignorePublicKeys.toArray(new String[0]));
        this.localCiphersCS = AdaptiveConfiguration.createAlgorithmList(this.transportContext.supportedCiphersCS(), "Ciphers", this.checkManagedSecurity(this.getString("preferredCipherCS", this.transportContext.getPreferredCipherCS())), this.ident, this.provider.getHost(), this.ignoreCiphers.toArray(new String[0]));
        this.localCiphersSC = AdaptiveConfiguration.createAlgorithmList(this.transportContext.supportedCiphersSC(), "Ciphers", this.checkManagedSecurity(this.getString("preferredCipherSC", this.transportContext.getPreferredCipherSC())), this.ident, this.provider.getHost(), this.ignoreCiphers.toArray(new String[0]));
        this.localMacsCS = AdaptiveConfiguration.createAlgorithmList(this.transportContext.supportedMacsCS(), "Macs", this.checkManagedSecurity(this.getString("preferredMacCS", this.transportContext.getPreferredMacCS())), this.ident, this.provider.getHost(), this.ignoreMacs.toArray(new String[0]));
        this.localMacsSC = AdaptiveConfiguration.createAlgorithmList(this.transportContext.supportedMacsSC(), "Macs", this.checkManagedSecurity(this.getString("preferredMacSC", this.transportContext.getPreferredMacSC())), this.ident, this.provider.getHost(), this.ignoreMacs.toArray(new String[0]));
        this.localCompressionsCS = AdaptiveConfiguration.createAlgorithmList(this.transportContext.supportedCompressionsCS(), "Compressions", this.checkManagedSecurity(this.getString("preferredCompressionCS", this.transportContext.getPreferredCompressionCS())), this.ident, this.provider.getHost(), this.ignoreCompressions.toArray(new String[0]));
        this.localCompressionsSC = AdaptiveConfiguration.createAlgorithmList(this.transportContext.supportedCompressionsSC(), "Compressions", this.checkManagedSecurity(this.getString("preferredCompressionSC", this.transportContext.getPreferredCompressionSC())), this.ident, this.provider.getHost(), this.ignoreCompressions.toArray(new String[0]));
    }

    private String checkManagedSecurity(String preferred) {
        if (this.transportContext.getSecurityPolicy().isManagedSecurity()) {
            return "";
        }
        return preferred;
    }

    public String getRemoteIdentification() {
        return this.remoteIdentification;
    }

    public byte[] getSessionIdentifier() {
        return this.sessionIdentifier;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void disconnect(int reason, String disconnectReason) {
        ByteArrayWriter baw = new ByteArrayWriter();
        try {
            this.disconnectReason = disconnectReason;
            baw.write(1);
            baw.writeInt(reason);
            baw.writeString(disconnectReason);
            baw.writeString("");
            if (log.isDebugEnabled()) {
                this.debug("Sending SSH_MSG_DISCONNECT [" + disconnectReason + "]", new Object[0]);
            }
            this.sendMessage(baw.toByteArray(), true);
        }
        catch (Throwable throwable) {
        }
        finally {
            try {
                baw.close();
            }
            catch (IOException iOException) {}
            this.internalDisconnect();
        }
    }

    @Override
    public void sendMessage(final byte[] msg, final boolean isActivity) throws SshException {
        if (this.buffered && !this.isTransportMessage(msg[0])) {
            this.addTask(new Runnable(){

                @Override
                public void run() {
                    TransportProtocol.this.sendMessageSync(msg, isActivity);
                }
            });
        } else {
            this.sendMessageSync(msg, isActivity);
        }
    }

    void sendOriginalPacketFormat(byte[] msgdata, boolean isActivity) throws IOException, SshException {
        this.outgoingMessage.reset();
        int padding = 4;
        if (this.outgoingCompression != null && this.isOutgoingCompressing) {
            try {
                msgdata = this.outgoingCompression.compress(msgdata, 0, msgdata.length);
            }
            catch (Throwable t) {
                throw new SshException(t.getMessage(), 57348, this.outgoingCompression.getAlgorithm());
            }
        }
        padding = this.encryption != null && this.encryption.isMAC() ? (padding += (this.outgoingCipherLength - (msgdata.length + 1 + padding) % this.outgoingCipherLength) % this.outgoingCipherLength) : (padding += (this.outgoingCipherLength - (msgdata.length + 5 + padding) % this.outgoingCipherLength) % this.outgoingCipherLength);
        this.outgoingMessage.writeInt(msgdata.length + 1 + padding);
        this.outgoingMessage.write(padding);
        this.outgoingMessage.write(msgdata, 0, msgdata.length);
        ComponentManager.getInstance().getRND().nextBytes(this.outgoingMessage.array(), this.outgoingMessage.size(), padding);
        this.outgoingMessage.move(padding);
        if (this.outgoingMac != null && this.outgoingMac.getMacLength() > 0) {
            try {
                this.outgoingMac.generate(this.outgoingSequence, this.outgoingMessage.array(), 0, this.outgoingMessage.size(), this.outgoingMessage.array(), this.outgoingMessage.size());
            }
            catch (Throwable t) {
                throw new SshException(t.getMessage(), 57346, this.outgoingMac.getAlgorithm());
            }
        }
        if (this.encryption != null) {
            try {
                this.encryption.transform(this.outgoingMessage.array(), 0, this.outgoingMessage.array(), 0, this.outgoingMessage.size());
            }
            catch (Throwable t) {
                throw new SshException(t.getMessage(), 57345, this.encryption.getAlgorithm());
            }
        }
        this.outgoingMessage.move(this.outgoingMacLength);
        this.outgoingBytes += (long)this.outgoingMessage.size();
        this.transportOut.write(this.outgoingMessage.array(), 0, this.outgoingMessage.size());
        if (!this.disableAutoFlush) {
            this.transportOut.flush();
        }
        if (isActivity) {
            this.lastActivity = System.currentTimeMillis();
        }
        if (this.verbose && log.isDebugEnabled()) {
            this.debug("Sent " + this.outgoingMessage.size() + " bytes of transport data outgoingSequence=" + this.outgoingSequence + " totalBytesSinceKEX=" + this.numOutgoingBytesSinceKEX, new Object[0]);
        }
        ++this.outgoingSequence;
        this.numOutgoingBytesSinceKEX += msgdata.length;
        ++this.numOutgoingPacketsSinceKEX;
        if (this.outgoingSequence >= 0x100000000L) {
            this.outgoingSequence = 0L;
        }
        if (!(this.keyReExchangeDisabled || this.numOutgoingBytesSinceKEX < this.getContext().getMaxNumBytesBeforeReKey() && this.numOutgoingPacketsSinceKEX < this.getContext().getMaxNumPacketsBeforeReKey())) {
            if (log.isDebugEnabled()) {
                this.debug("Requesting key re-exchange", new Object[0]);
            }
            this.sendKeyExchangeInit(false);
        }
    }

    void sendETMPacketFormat(byte[] msgdata, boolean isActivity) throws IOException, SshException {
        this.outgoingMessage.reset();
        int padding = 4;
        if (this.outgoingCompression != null && this.isOutgoingCompressing) {
            try {
                msgdata = this.outgoingCompression.compress(msgdata, 0, msgdata.length);
            }
            catch (Throwable t) {
                throw new SshException(t.getMessage(), 57348, this.outgoingCompression.getAlgorithm());
            }
        }
        padding += (this.outgoingCipherLength - (msgdata.length + 1 + padding) % this.outgoingCipherLength) % this.outgoingCipherLength;
        int length = msgdata.length + 1 + padding;
        this.outgoingMessage.writeInt(length);
        this.outgoingMessage.write(padding);
        this.outgoingMessage.write(msgdata, 0, msgdata.length);
        ComponentManager.getInstance().getRND().nextBytes(this.outgoingMessage.array(), this.outgoingMessage.size(), padding);
        this.outgoingMessage.move(padding);
        if (this.encryption != null) {
            try {
                this.encryption.transform(this.outgoingMessage.array(), 4, this.outgoingMessage.array(), 4, this.outgoingMessage.size() - 4);
            }
            catch (Throwable t) {
                throw new SshException(t.getMessage(), 57345, this.encryption.getAlgorithm());
            }
        }
        if (this.outgoingMac != null && this.outgoingMac.getMacLength() > 0) {
            try {
                this.outgoingMac.generate(this.outgoingSequence, this.outgoingMessage.array(), 0, this.outgoingMessage.size(), this.outgoingMessage.array(), this.outgoingMessage.size());
            }
            catch (Throwable t) {
                throw new SshException(t.getMessage(), 57346, this.outgoingMac.getAlgorithm());
            }
        }
        this.outgoingMessage.move(this.outgoingMacLength);
        this.outgoingBytes += (long)this.outgoingMessage.size();
        this.transportOut.write(this.outgoingMessage.array(), 0, this.outgoingMessage.size());
        if (!this.disableAutoFlush) {
            this.transportOut.flush();
        }
        if (isActivity) {
            this.lastActivity = System.currentTimeMillis();
        }
        if (this.verbose && log.isDebugEnabled()) {
            this.debug("Sent " + this.outgoingMessage.size() + " bytes of transport data outgoingSequence=" + this.outgoingSequence + " totalBytesSinceKEX=" + this.numOutgoingBytesSinceKEX, new Object[0]);
        }
        ++this.outgoingSequence;
        this.numOutgoingBytesSinceKEX += msgdata.length;
        ++this.numOutgoingPacketsSinceKEX;
        if (this.outgoingSequence >= 0x100000000L) {
            this.outgoingSequence = 0L;
        }
        if (!(this.keyReExchangeDisabled || this.numOutgoingBytesSinceKEX < this.getContext().getMaxNumBytesBeforeReKey() && this.numOutgoingPacketsSinceKEX < this.getContext().getMaxNumPacketsBeforeReKey())) {
            if (log.isDebugEnabled()) {
                this.debug("Requesting key re-exchange", new Object[0]);
            }
            this.sendKeyExchangeInit(false);
        }
    }

    void sendChaCha20Poly1305PacketFormat(byte[] msgdata, boolean isActivity) throws IOException, SshException {
        ChaCha20Poly1305 cipher = (ChaCha20Poly1305)this.encryption;
        this.outgoingMessage.reset();
        int padding = 4;
        if (this.outgoingCompression != null && this.isOutgoingCompressing) {
            try {
                msgdata = this.outgoingCompression.compress(msgdata, 0, msgdata.length);
            }
            catch (Throwable t) {
                throw new SshException(t.getMessage(), 57348, this.outgoingCompression.getAlgorithm());
            }
        }
        padding += (this.outgoingCipherLength - (msgdata.length + 1 + padding) % this.outgoingCipherLength) % this.outgoingCipherLength;
        int length = msgdata.length + 1 + padding;
        try {
            this.outgoingMessage.write(cipher.writePacketLength(length, new UnsignedInteger64(this.outgoingSequence)));
        }
        catch (Throwable t) {
            throw new SshException(t.getMessage(), 57345, cipher.getAlgorithm());
        }
        this.outgoingMessage.write(padding);
        this.outgoingMessage.write(msgdata, 0, msgdata.length);
        ComponentManager.getInstance().getRND().nextBytes(this.outgoingMessage.array(), this.outgoingMessage.size(), padding);
        this.outgoingMessage.move(padding);
        try {
            cipher.transform(this.outgoingMessage.array(), 4, this.outgoingMessage.array(), 4, this.outgoingMessage.size() - 4 + this.incomingMacLength);
        }
        catch (Throwable t) {
            throw new SshException(t.getMessage(), 57345, cipher.getAlgorithm());
        }
        this.outgoingMessage.move(this.outgoingMacLength);
        this.outgoingBytes += (long)this.outgoingMessage.size();
        this.transportOut.write(this.outgoingMessage.array(), 0, this.outgoingMessage.size());
        if (!this.disableAutoFlush) {
            this.transportOut.flush();
        }
        if (isActivity) {
            this.lastActivity = System.currentTimeMillis();
        }
        if (this.verbose && log.isDebugEnabled()) {
            this.debug("Sent " + this.outgoingMessage.size() + " bytes of transport data outgoingSequence=" + this.outgoingSequence + " totalBytesSinceKEX=" + this.numOutgoingBytesSinceKEX, new Object[0]);
        }
        ++this.outgoingSequence;
        this.numOutgoingBytesSinceKEX += msgdata.length;
        ++this.numOutgoingPacketsSinceKEX;
        if (this.outgoingSequence >= 0x100000000L) {
            this.outgoingSequence = 0L;
        }
        if (!(this.keyReExchangeDisabled || this.numOutgoingBytesSinceKEX < this.getContext().getMaxNumBytesBeforeReKey() && this.numOutgoingPacketsSinceKEX < this.getContext().getMaxNumPacketsBeforeReKey())) {
            if (log.isDebugEnabled()) {
                this.debug("Requesting key re-exchange", new Object[0]);
            }
            this.sendKeyExchangeInit(false);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void sendMessageSync(byte[] msg, boolean isActivity) {
        byte[] msgdata = msg;
        if (this.binary) {
            this.info("Sending application data{}{}", System.lineSeparator(), Utils.bytesToHex(msg, Math.min(msg.length, 32), true, true));
        }
        try {
            if (this.binaryOutput != null) {
                this.binaryOutput.log(ByteArrayWriter.encodeInt(msg.length));
                this.binaryOutput.log(msg);
            }
        }
        catch (IOException e) {
            log.error("Failed to log binary data to file", (Throwable)e);
            this.disconnect(11, "Internal error");
            return;
        }
        List<byte[]> list = this.outgoingKexQueue;
        synchronized (list) {
            if (this.currentState == 2 && !this.isTransportMessage(msgdata[0])) {
                this.outgoingKexQueue.add(msgdata);
                return;
            }
            if (this.verbose && log.isDebugEnabled()) {
                this.debug("Sending transport protocol message", new Object[0]);
            }
            try {
                if (this.encryption != null && this.encryption instanceof ChaCha20Poly1305) {
                    this.sendChaCha20Poly1305PacketFormat(msgdata, isActivity);
                } else if (this.outgoingMac != null && this.outgoingMac.isETM()) {
                    this.sendETMPacketFormat(msgdata, isActivity);
                } else {
                    this.sendOriginalPacketFormat(msgdata, isActivity);
                }
            }
            catch (IOException ex) {
                this.internalDisconnect();
            }
            catch (SshException ex) {
                this.internalDisconnect();
            }
        }
    }

    public void info(String msg, Object ... args) {
        log.info(String.format("%s - ", this.getUuid()) + msg, args);
    }

    public void debug(String msg, Object ... args) {
        log.debug(String.format("%s - ", this.getUuid()) + msg, args);
    }

    @Override
    public void info(Logger log, String msg, Object ... args) {
        log.info(String.format("%s - ", this.getUuid()) + msg, args);
    }

    @Override
    public void debug(Logger log, String msg, Object ... args) {
        log.debug(String.format("%s - ", this.getUuid()) + msg, args);
    }

    @Override
    public void error(Logger log, String msg, Exception e) {
        log.error(String.format("%s - ", this.getUuid()) + msg, (Throwable)e);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public byte[] nextMessage(long timeout) throws SshException {
        if (this.verbose && log.isDebugEnabled()) {
            this.debug("transport next message", new Object[0]);
        }
        DataInputStream dataInputStream = this.transportIn;
        synchronized (dataInputStream) {
            byte[] msg;
            while (this.processMessage(msg = this.readMessage(timeout))) {
            }
            return msg;
        }
    }

    void processTimeout() throws SshException {
        boolean hasIdled;
        long sinceLastIdle = System.currentTimeMillis() - this.lastIdle;
        boolean bl = hasIdled = sinceLastIdle > (long)this.socketTimeout;
        if (hasIdled) {
            this.lastIdle = System.currentTimeMillis();
        }
        this.assertIdleTimeout();
        if (this.sendIgnoreOnIdle && hasIdled) {
            ByteArrayWriter baw = new ByteArrayWriter();
            try {
                if (log.isDebugEnabled()) {
                    this.debug("Sending SSH_MSG_IGNORE", new Object[0]);
                }
                baw.write(2);
                int tmplen = (int)(Math.random() * (double)this.keepAliveDataLength + 1.0);
                byte[] tmp = new byte[tmplen];
                ComponentManager.getInstance().getRND().nextBytes(tmp);
                baw.writeBinaryString(tmp);
                this.sendMessage(baw.toByteArray(), false);
            }
            catch (IOException e) {
                this.internalDisconnect("Connection failed during SSH_MSG_IGNORE packet", 10);
                throw new SshException("Connection failed during SSH_MSG_IGNORE packet", 12);
            }
            finally {
                try {
                    baw.close();
                }
                catch (IOException iOException) {}
            }
        }
        if (this.socketTimeout > 0) {
            if (hasIdled) {
                for (TransportProtocolListener l : this.listeners) {
                    try {
                        l.onIdle(this.lastActivity);
                    }
                    catch (Throwable throwable) {}
                }
            }
        } else {
            throw new SshException("Socket connection timed out.", 19);
        }
    }

    void assertIdleTimeout() throws SshException {
        long sinceLastActivity = System.currentTimeMillis() - this.lastActivity;
        long maxIdleTimeoutSeconds = this.getClient().isAuthenticated() ? this.idleConnectionTimeout : this.idleAuthenticationTimeout;
        if (maxIdleTimeoutSeconds > 0L && sinceLastActivity > maxIdleTimeoutSeconds * 1000L) {
            if (this.currentState == 2) {
                if (log.isDebugEnabled()) {
                    this.debug("Throwing EOF exception after receiving timeout in key exchange", new Object[0]);
                }
                throw new SshException("Socket timeout received from remote side during key exchange", 57352);
            }
            if (log.isDebugEnabled()) {
                this.debug("Connection is idle, disconnecting idleMax=" + maxIdleTimeoutSeconds, new Object[0]);
            }
            if (this.treatIdleConnectionAsError) {
                this.internalDisconnect();
            } else {
                this.disconnect(11, "Idle connection");
            }
            throw new SshException("Connection has been dropped as it reached max idle time of " + maxIdleTimeoutSeconds + " seconds.", 12);
        }
        if (maxIdleTimeoutSeconds > 0L) {
            if (log.isDebugEnabled()) {
                this.debug("Connection is idle but below connection timeout threshold idleMax=" + maxIdleTimeoutSeconds, new Object[0]);
            }
        } else if (log.isDebugEnabled()) {
            this.debug("Connection is idle but no connection timeout has been configured", new Object[0]);
        }
    }

    void readWithTimeout(byte[] buf, int off, int len, boolean isPartialMessage, long timeout) throws SshException {
        int count = 0;
        do {
            try {
                int read = this.transportIn.read(buf, off + count, len - count);
                if (read == -1) {
                    if (this.currentState == 2) {
                        throw new SshException("EOF received from remote side during key exchange", 57352);
                    }
                    throw new SshException("EOF received from remote side", 1);
                }
                count += read;
            }
            catch (InterruptedIOException ex) {
                if (log.isTraceEnabled()) {
                    log.trace("Socket timed out during read!  isPartialMessage=" + isPartialMessage + " bytesTransfered=" + ex.bytesTransferred);
                }
                if (isPartialMessage && ex.bytesTransferred > 0) {
                    count += ex.bytesTransferred;
                    continue;
                }
                if (this.disableIdleProcessDuringKeyExchange && this.currentState == 2) {
                    throw new SshException("EOF received from remote side during key exchange", 57352);
                }
                this.processTimeout();
            }
            catch (IOException ex) {
                throw new SshException("IO error received from remote" + ex.getMessage(), 1, ex);
            }
        } while (count < len);
    }

    byte[] readOriginalPacketFormat(long timeout) throws IOException, SshException {
        int msglen;
        if (this.verbose && log.isDebugEnabled()) {
            this.debug("Waiting for transport message", new Object[0]);
        }
        this.readWithTimeout(this.incomingMessage, 0, this.incomingCipherLength, false, timeout);
        if (this.decryption != null && !this.decryption.isMAC()) {
            try {
                this.decryption.transform(this.incomingMessage, 0, this.incomingMessage, 0, this.incomingCipherLength);
            }
            catch (Throwable t) {
                throw new SshException(t.getMessage(), 57345, this.decryption.getAlgorithm());
            }
        }
        if ((msglen = (int)ByteArrayReader.readInt(this.incomingMessage, 0)) <= 0) {
            throw new SshException("Server sent invalid message length of " + msglen + "!", 3);
        }
        int padlen = this.incomingMessage[4] & 0xFF;
        int remaining = msglen - (this.incomingCipherLength - 4);
        if (this.verbose && this.decryption != null && !this.decryption.isMAC() && log.isDebugEnabled()) {
            this.debug("Incoming transport message msglen=" + msglen + " padlen=" + padlen + " msgid=" + this.incomingMessage[5], new Object[0]);
        }
        if (remaining < 0) {
            this.internalDisconnect();
            throw new SshException("EOF whilst reading message data block", 1);
        }
        if (remaining > this.incomingMessage.length - this.incomingCipherLength) {
            if (remaining + this.incomingCipherLength + this.incomingMacLength > this.maximumPacketLength) {
                this.internalDisconnect();
                throw new SshException("Incoming packet length of " + (remaining + this.incomingCipherLength + this.incomingMacLength) + " bytes violates our maximum packet threshold of " + this.maximumPacketLength, 1);
            }
            byte[] tmp = new byte[remaining + this.incomingCipherLength + this.incomingMacLength];
            System.arraycopy(this.incomingMessage, 0, tmp, 0, this.incomingCipherLength);
            this.incomingMessage = tmp;
        }
        if (remaining > 0) {
            this.readWithTimeout(this.incomingMessage, this.incomingCipherLength, remaining, true, timeout);
            if (this.decryption != null && !this.decryption.isMAC()) {
                try {
                    this.decryption.transform(this.incomingMessage, this.incomingCipherLength, this.incomingMessage, this.incomingCipherLength, remaining);
                }
                catch (Throwable t) {
                    throw new SshException(t.getMessage(), 57345, this.decryption.getAlgorithm());
                }
            }
        }
        if (this.incomingMacLength > 0) {
            this.readWithTimeout(this.incomingMessage, this.incomingCipherLength + remaining, this.incomingMacLength, true, timeout);
            if (this.incomingMac != null) {
                if (!this.incomingMac.verify(this.incomingSequence, this.incomingMessage, 0, this.incomingCipherLength + remaining, this.incomingMessage, this.incomingCipherLength + remaining)) {
                    this.disconnect(5, "Corrupt Mac on input");
                    throw new SshException("Corrupt Mac on input", 57346, this.incomingMac.getAlgorithm());
                }
            } else if (this.decryption != null && this.decryption.isMAC()) {
                try {
                    this.decryption.transform(this.incomingMessage, 0, this.incomingMessage, 0, msglen + 4 + this.incomingMacLength);
                }
                catch (Throwable t) {
                    throw new SshException(t.getMessage(), 57345, this.decryption.getAlgorithm());
                }
                padlen = this.incomingMessage[4] & 0xFF;
                if (this.verbose && this.decryption != null && this.decryption.isMAC() && log.isDebugEnabled()) {
                    this.debug("Incoming transport message msglen=" + msglen + " padlen=" + padlen + " msgid=" + this.incomingMessage[5], new Object[0]);
                }
            }
        }
        if (++this.incomingSequence >= 0x100000000L) {
            this.incomingSequence = 0L;
        }
        this.incomingBytes += (long)(this.incomingCipherLength + remaining + this.incomingMacLength);
        byte[] payload = new byte[msglen - padlen - 1];
        System.arraycopy(this.incomingMessage, 5, payload, 0, payload.length);
        if (this.incomingCompression != null && this.isIncomingCompressing) {
            try {
                payload = this.incomingCompression.uncompress(payload, 0, payload.length);
            }
            catch (Throwable t) {
                throw new SshException(t.getMessage(), 57348, this.incomingCompression.getAlgorithm());
            }
        }
        this.numIncomingBytesSinceKEX += payload.length;
        ++this.numIncomingPacketsSinceKEX;
        if (!(this.keyReExchangeDisabled || (long)this.numIncomingBytesSinceKEX < this.numBytesBeforeRekey && (long)this.numIncomingPacketsSinceKEX < this.numPacketsBeforeRekey)) {
            this.sendKeyExchangeInit(false);
        }
        return payload;
    }

    byte[] readChaCha20Poly1305Format(long timeout) throws IOException, SshException {
        int msglen;
        if (this.verbose && log.isDebugEnabled()) {
            this.debug("Waiting for transport message", new Object[0]);
        }
        ChaCha20Poly1305 cipher = (ChaCha20Poly1305)this.decryption;
        this.readWithTimeout(this.incomingMessage, 0, 4, false, timeout);
        try {
            msglen = (int)cipher.readPacketLength(this.incomingMessage, new UnsignedInteger64(this.incomingSequence));
        }
        catch (Throwable t) {
            throw new SshException(t.getMessage(), 57345, cipher.getAlgorithm());
        }
        if (msglen <= 0) {
            throw new SshException("Server sent invalid message length of " + msglen + "!", 3);
        }
        int remaining = msglen;
        if (remaining < 0) {
            this.internalDisconnect();
            throw new SshException("EOF whilst reading message data block", 1);
        }
        if (remaining > this.incomingMessage.length - this.incomingCipherLength) {
            if (remaining + this.incomingCipherLength + this.incomingMacLength > this.maximumPacketLength) {
                this.internalDisconnect();
                throw new SshException("Incoming packet length of " + (remaining + this.incomingCipherLength + this.incomingMacLength) + " bytes violates our maximum packet threshold of " + this.maximumPacketLength, 1);
            }
            byte[] tmp = new byte[remaining + 4 + this.incomingMacLength];
            System.arraycopy(this.incomingMessage, 0, tmp, 0, 4);
            this.incomingMessage = tmp;
        }
        if (remaining > 0) {
            this.readWithTimeout(this.incomingMessage, 4, remaining + this.incomingMacLength, true, timeout);
            try {
                cipher.transform(this.incomingMessage, 4, this.incomingMessage, 4, remaining + this.incomingMacLength);
            }
            catch (Throwable t) {
                throw new SshException(t.getMessage(), 57345, this.decryption.getAlgorithm());
            }
        }
        int padlen = this.incomingMessage[4] & 0xFF;
        if (this.verbose && log.isDebugEnabled()) {
            this.debug("Incoming transport message msglen=" + msglen + " padlen=" + padlen + " msgid=" + this.incomingMessage[5], new Object[0]);
        }
        if (++this.incomingSequence >= 0x100000000L) {
            this.incomingSequence = 0L;
        }
        this.incomingBytes += (long)(4 + remaining + this.incomingMacLength);
        byte[] payload = new byte[remaining - padlen - 1];
        System.arraycopy(this.incomingMessage, 5, payload, 0, payload.length);
        if (this.incomingCompression != null && this.isIncomingCompressing) {
            try {
                payload = this.incomingCompression.uncompress(payload, 0, payload.length);
            }
            catch (Throwable t) {
                throw new SshException(t.getMessage(), 57348, this.incomingCompression.getAlgorithm());
            }
        }
        this.numIncomingBytesSinceKEX += payload.length;
        ++this.numIncomingPacketsSinceKEX;
        if (!(this.keyReExchangeDisabled || (long)this.numIncomingBytesSinceKEX < this.numBytesBeforeRekey && (long)this.numIncomingPacketsSinceKEX < this.numPacketsBeforeRekey)) {
            this.sendKeyExchangeInit(false);
        }
        return payload;
    }

    byte[] readETMPacketFormat(long timeout) throws IOException, SshException {
        if (this.verbose && log.isDebugEnabled()) {
            this.debug("Waiting for transport message", new Object[0]);
        }
        this.readWithTimeout(this.incomingMessage, 0, 4, false, timeout);
        int msglen = (int)ByteArrayReader.readInt(this.incomingMessage, 0);
        if (msglen <= 0) {
            throw new SshException("Server sent invalid message length of " + msglen + "!", 3);
        }
        int remaining = msglen;
        if (remaining < 0) {
            this.internalDisconnect();
            throw new SshException("EOF whilst reading message data block", 1);
        }
        if (remaining > this.incomingMessage.length - this.incomingCipherLength) {
            if (remaining + this.incomingCipherLength + this.incomingMacLength > this.maximumPacketLength) {
                this.internalDisconnect();
                throw new SshException("Incoming packet length of " + (remaining + this.incomingCipherLength + this.incomingMacLength) + " bytes violates our maximum packet threshold of " + this.maximumPacketLength, 1);
            }
            byte[] tmp = new byte[remaining + 4 + this.incomingMacLength];
            System.arraycopy(this.incomingMessage, 0, tmp, 0, 4);
            this.incomingMessage = tmp;
        }
        if (remaining > 0) {
            this.readWithTimeout(this.incomingMessage, 4, remaining + this.incomingMacLength, true, timeout);
            if (!this.incomingMac.verify(this.incomingSequence, this.incomingMessage, 0, remaining + 4, this.incomingMessage, remaining + 4)) {
                this.disconnect(5, "Corrupt Mac on input");
                throw new SshException("Corrupt Mac on input", 57346);
            }
            if (this.decryption != null) {
                try {
                    this.decryption.transform(this.incomingMessage, 4, this.incomingMessage, 4, remaining);
                }
                catch (Throwable t) {
                    throw new SshException(t.getMessage(), 57345, this.decryption.getAlgorithm());
                }
            }
        }
        int padlen = this.incomingMessage[4] & 0xFF;
        if (this.verbose && log.isDebugEnabled()) {
            this.debug("Incoming transport message msglen=" + msglen + " padlen=" + padlen + " msgid=" + this.incomingMessage[5], new Object[0]);
        }
        if (++this.incomingSequence >= 0x100000000L) {
            this.incomingSequence = 0L;
        }
        this.incomingBytes += (long)(4 + remaining + this.incomingMacLength);
        byte[] payload = new byte[remaining - padlen - 1];
        System.arraycopy(this.incomingMessage, 5, payload, 0, payload.length);
        if (this.incomingCompression != null && this.isIncomingCompressing) {
            try {
                payload = this.incomingCompression.uncompress(payload, 0, payload.length);
            }
            catch (Throwable t) {
                throw new SshException(t.getMessage(), 57348, this.incomingCompression.getAlgorithm());
            }
        }
        this.numIncomingBytesSinceKEX += payload.length;
        ++this.numIncomingPacketsSinceKEX;
        if (!(this.keyReExchangeDisabled || (long)this.numIncomingBytesSinceKEX < this.numBytesBeforeRekey && (long)this.numIncomingPacketsSinceKEX < this.numPacketsBeforeRekey)) {
            this.sendKeyExchangeInit(false);
        }
        return payload;
    }

    byte[] readMessage(long timeout) throws SshException {
        if (this.verbose && log.isDebugEnabled()) {
            this.debug("transport read message", new Object[0]);
        }
        DataInputStream dataInputStream = this.transportIn;
        synchronized (dataInputStream) {
            try {
                SocketTimeoutSupport tmp;
                long l = timeout = timeout > 0L ? timeout : (long)this.socketTimeout;
                if (this.provider instanceof SocketTimeoutSupport && (long)(tmp = (SocketTimeoutSupport)((Object)this.provider)).getSoTimeout() != timeout) {
                    if (log.isDebugEnabled()) {
                        this.debug("Reconfigure socket timeout to {}", timeout);
                    }
                    tmp.setSoTimeout((int)timeout);
                }
                byte[] msg = null;
                if (!this.disableKEXProtocolWorkaround) {
                    if (this.currentState == 3 && !this.incomingKexQueue.isEmpty()) {
                        return this.incomingKexQueue.remove(0);
                    }
                    do {
                        if (msg != null) {
                            this.incomingKexQueue.add(msg);
                        }
                        msg = this.decryption != null && this.decryption instanceof ChaCha20Poly1305 ? this.readChaCha20Poly1305Format(timeout) : (this.incomingMac != null && this.incomingMac.isETM() ? this.readETMPacketFormat(timeout) : this.readOriginalPacketFormat(timeout));
                    } while (this.currentState == 2 && !this.isTransportMessage(msg[0]));
                } else {
                    msg = this.decryption != null && this.decryption instanceof ChaCha20Poly1305 ? this.readChaCha20Poly1305Format(timeout) : (this.incomingMac != null && this.incomingMac.isETM() ? this.readETMPacketFormat(timeout) : this.readOriginalPacketFormat(timeout));
                }
                return msg;
            }
            catch (InterruptedIOException ex) {
                throw new SshException("Interrupted IO; possible socket timeout detected?", 19);
            }
            catch (IOException ex) {
                this.internalDisconnect();
                throw new SshException("Unexpected terminaton: " + (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName()) + " sequenceNo = " + this.incomingSequence + " bytesIn = " + this.incomingBytes + " bytesOut = " + this.outgoingBytes, 1, ex);
            }
        }
    }

    public SshKeyExchangeClient getKeyExchange() {
        return this.keyExchange;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void performKeyExchange(byte[] msg) throws SshException {
        ByteArrayInputStream bar = null;
        try {
            List<byte[]> list = this.outgoingKexQueue;
            synchronized (list) {
                if (this.localkex == null) {
                    this.sendKeyExchangeInit(false);
                }
                this.currentState = 2;
                this.remotekex = msg;
                bar = new ByteArrayReader(this.remotekex, 0, this.remotekex.length);
                bar.skip(17L);
                this.remoteKeyExchanges = this.checkValidString("key exchange", ((ByteArrayReader)bar).readString());
                this.remotePublicKeys = this.checkValidString("public key", ((ByteArrayReader)bar).readString());
                this.remoteCiphersCS = this.checkValidString("client->server cipher", ((ByteArrayReader)bar).readString());
                this.remoteCiphersSC = this.checkValidString("server->client cipher", ((ByteArrayReader)bar).readString());
                this.remoteMacsCS = this.checkValidString("client->server mac", ((ByteArrayReader)bar).readString());
                this.remoteMacsSC = this.checkValidString("server->client mac", ((ByteArrayReader)bar).readString());
                this.remoteCompressionsCS = this.checkValidString("client->server comp", ((ByteArrayReader)bar).readString());
                this.remoteCompressionsSC = this.checkValidString("server->client comp", ((ByteArrayReader)bar).readString());
                String lang1 = ((ByteArrayReader)bar).readString();
                String lang2 = ((ByteArrayReader)bar).readString();
                boolean guessed = ((ByteArrayReader)bar).readBoolean();
                EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.client, 3, true, this.getUuid()).addAttribute("CLIENT", this.getClient()).addAttribute("REMOTE_KEY_EXCHANGES", this.remoteKeyExchanges).addAttribute("LOCAL_KEY_EXCHANGES", this.localKeyExchanges).addAttribute("REMOTE_PUBLICKEYS", this.remotePublicKeys).addAttribute("LOCAL_PUBLICKEYS", this.localPublicKeys).addAttribute("REMOTE_CIPHERS_CS", this.remoteCiphersCS).addAttribute("LOCAL_CIPHERS_CS", this.localCiphersCS).addAttribute("REMOTE_CIPHERS_SC", this.remoteCiphersSC).addAttribute("LOCAL_CIPHERS_SC", this.localCiphersSC).addAttribute("REMOTE_CS_MACS", this.remoteMacsCS).addAttribute("LOCAL_CS_MACS", this.localMacsCS).addAttribute("REMOTE_SC_MACS", this.remoteMacsSC).addAttribute("LOCAL_SC_MACS", this.localMacsSC).addAttribute("REMOTE_CS_COMPRESSIONS", this.remoteCompressionsCS).addAttribute("LOCAL_CS_COMPRESSIONS", this.localCompressionsCS).addAttribute("REMOTE_SC_COMPRESSIONS", this.remoteCompressionsSC).addAttribute("LOCAL_SC_COMPRESSIONS", this.localCompressionsSC));
                this.processKeyExchangeAlgorithms();
                if (log.isDebugEnabled()) {
                    this.debug("Negotiated client->server cipher: " + this.cipherCS, new Object[0]);
                }
                if (log.isDebugEnabled()) {
                    this.debug("Negotiated server->client cipher: " + this.cipherSC, new Object[0]);
                }
                SshCipher encryption = (SshCipher)this.transportContext.supportedCiphersCS().getInstance(this.cipherCS);
                SshCipher decryption = (SshCipher)this.transportContext.supportedCiphersSC().getInstance(this.cipherSC);
                SshHmac outgoingMac = null;
                SshHmac incomingMac = null;
                if (!encryption.isMAC()) {
                    if (log.isDebugEnabled()) {
                        this.debug("Negotiated client->server hmac: " + this.macCS, new Object[0]);
                    }
                    outgoingMac = (SshHmac)this.transportContext.supportedMacsCS().getInstance(this.macCS);
                } else if (log.isDebugEnabled()) {
                    this.debug("Negotiated client->server hmac: " + this.cipherCS, new Object[0]);
                }
                if (!decryption.isMAC()) {
                    if (log.isDebugEnabled()) {
                        this.debug("Negotiated server->client hmac: " + this.macSC, new Object[0]);
                    }
                    incomingMac = (SshHmac)this.transportContext.supportedMacsSC().getInstance(this.macSC);
                } else if (log.isDebugEnabled()) {
                    this.debug("Negotiated client->server hmac: " + this.cipherSC, new Object[0]);
                }
                boolean ignoreFirstPacket = false;
                if (guessed) {
                    if (!this.keyExchangeAlg.equals(this.transportContext.getPreferredKeyExchange())) {
                        ignoreFirstPacket = true;
                    }
                    if (!ignoreFirstPacket && !this.hostKeyAlg.equals(this.transportContext.getPreferredPublicKey())) {
                        ignoreFirstPacket = true;
                    }
                }
                if (log.isDebugEnabled()) {
                    this.debug("Negotiated client->server compression: " + this.compressionCS, new Object[0]);
                }
                if (log.isDebugEnabled()) {
                    this.debug("Negotiated server->client compression: " + this.compressionSC, new Object[0]);
                }
                SshCompression outgoingCompression = null;
                if (!this.compressionCS.equals("none")) {
                    outgoingCompression = (SshCompression)this.transportContext.supportedCompressionsCS().getInstance(this.compressionCS);
                    outgoingCompression.init(1, 6);
                }
                SshCompression incomingCompression = null;
                if (!this.compressionSC.equals("none")) {
                    incomingCompression = (SshCompression)this.transportContext.supportedCompressionsSC().getInstance(this.compressionSC);
                    incomingCompression.init(0, 6);
                }
                if (this.guessedKeyExchange == null || !this.keyExchangeAlg.equals(this.guessedKeyExchange.getAlgorithm())) {
                    this.keyExchange = (SshKeyExchangeClient)this.transportContext.supportedKeyExchanges().getInstance(this.keyExchangeAlg);
                }
                if (log.isDebugEnabled()) {
                    this.debug("Negotiated key exchange: " + this.keyExchange.getAlgorithm(), new Object[0]);
                }
                this.keyExchange.init(this, ignoreFirstPacket);
                try {
                    this.keyExchange.performClientExchange(this.localIdentification, this.remoteIdentification, this.localkex, this.remotekex);
                }
                catch (SshException e) {
                    SshException rootError = e;
                    if (e.getCause() instanceof SshException) {
                        rootError = (SshException)e.getCause();
                    }
                    this.processKnownIssues(rootError.getReason(), rootError.getMessage(), rootError);
                }
                catch (Throwable t) {
                    throw new SshException(t.getMessage(), 57347, this.keyExchange.getAlgorithm());
                }
                if (log.isDebugEnabled()) {
                    this.debug("Negotiated public key: " + this.hostKeyAlg, new Object[0]);
                }
                this.hostkey = (SshPublicKey)this.transportContext.supportedPublicKeys().getInstance(this.hostKeyAlg);
                if (log.isDebugEnabled()) {
                    this.debug("Encoded public key: {}", Base64.encodeBytes(this.keyExchange.getHostKey(), true));
                }
                if (!this.ignoreHostKeyifEmpty || !Arrays.areEqual(this.keyExchange.getHostKey(), "".getBytes())) {
                    this.hostkey.init(this.keyExchange.getHostKey(), 0, this.keyExchange.getHostKey().length);
                    EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.client, 0, true, this.getUuid()).addAttribute("CLIENT", this.getClient()).addAttribute("HOST_KEY", new String(this.keyExchange.getHostKey())).addAttribute("HOST_PUBLIC_KEY", this.hostkey));
                    if (this.transportContext.getHostKeyVerification() != null && !this.transportContext.getHostKeyVerification().verifyHost(this.getKnownHostName(), this.hostkey)) {
                        EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.client, 1, false, this.getUuid()).addAttribute("CLIENT", this.getClient()).addAttribute("HOST_KEY", new String(this.keyExchange.getHostKey())).addAttribute("HOST_PUBLIC_KEY", this.hostkey));
                        this.disconnect(9, "Host key not accepted");
                        throw new SshException("The host key was not accepted", 8);
                    }
                    this.processVerboseSignature(this.hostkey);
                    if (!this.hostkey.verifySignature(this.keyExchange.getSignature(), this.keyExchange.getExchangeHash())) {
                        EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.client, 1, false, this.getUuid()).addAttribute("CLIENT", this.getClient()).addAttribute("HOST_KEY", new String(this.keyExchange.getHostKey())).addAttribute("HOST_PUBLIC_KEY", this.hostkey));
                        if (this.getContext().getTemporaryValue("triedInvalidSignatureKexChange", false)) {
                            throw new SshException("The host key signature is invalid", 57349, this.hostKeyAlg);
                        }
                        this.getContext().setTemporaryValue("triedInvalidSignatureKexChange", true);
                        throw new SshException("The host key signature is invalid", 57347, this.keyExchangeAlg);
                    }
                    EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.client, 2, true, this.getUuid()).addAttribute("CLIENT", this.getClient()).addAttribute("HOST_KEY", new String(this.keyExchange.getHostKey())).addAttribute("HOST_PUBLIC_KEY", this.hostkey));
                }
                if (this.sessionIdentifier == null) {
                    this.sessionIdentifier = this.keyExchange.getExchangeHash();
                }
                if (log.isDebugEnabled()) {
                    this.debug("Sending SSH_MSG_NEWKEYS", new Object[0]);
                }
                this.sendMessageSync(new byte[]{21}, true);
                encryption.init(0, this.makeSshKey('A', encryption.getBlockSize()), this.makeSshKey('C', encryption.getKeyLength()));
                this.outgoingCipherLength = encryption.getBlockSize();
                this.encryption = encryption;
                if (outgoingMac != null) {
                    outgoingMac.init(this.makeSshKey('E', outgoingMac.getMacSize()));
                    this.outgoingMacLength = outgoingMac.getMacLength();
                    this.outgoingMac = outgoingMac;
                } else if (encryption != null && encryption.isMAC()) {
                    this.outgoingMacLength = encryption.getMacLength();
                }
                this.outgoingCompression = outgoingCompression;
                do {
                    if (this.processMessage(msg = this.readMessage(0L))) continue;
                    EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.client, 4, true, this.getUuid()).addAttribute("CLIENT", this.getClient()));
                    this.disconnect(2, String.format("Invalid message received id=%d", msg[0] & 0xFF));
                    throw new SshException(String.format("Invalid message received id=%d", msg[0] & 0xFF), 3);
                } while (msg[0] != 21);
                EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.client, 5, true, this.getUuid()).addAttribute("CLIENT", this.getClient()).addAttribute("USING_PUBLICKEY", this.hostKeyAlg).addAttribute("USING_KEY_EXCHANGE", this.keyExchangeAlg).addAttribute("USING_CS_CIPHER", this.cipherCS).addAttribute("USING_SC_CIPHERC", this.cipherSC).addAttribute("USING_CS_MAC", this.macCS).addAttribute("USING_SC_MAC", this.macSC).addAttribute("USING_CS_COMPRESSION", this.compressionCS).addAttribute("USING_SC_COMPRESSION", this.compressionSC));
                decryption.init(1, this.makeSshKey('B', decryption.getBlockSize()), this.makeSshKey('D', decryption.getKeyLength()));
                this.incomingCipherLength = decryption.getBlockSize();
                this.decryption = decryption;
                if (incomingMac != null) {
                    incomingMac.init(this.makeSshKey('F', incomingMac.getMacSize()));
                    this.incomingMacLength = incomingMac.getMacLength();
                    this.incomingMac = incomingMac;
                } else if (decryption != null && decryption.isMAC()) {
                    this.incomingMacLength = decryption.getMacLength();
                }
                this.incomingCompression = incomingCompression;
                if (incomingCompression != null) {
                    boolean bl = this.isIncomingCompressing = !incomingCompression.isDelayed();
                }
                if (outgoingCompression != null) {
                    this.isOutgoingCompressing = !outgoingCompression.isDelayed();
                }
                this.currentState = 3;
                for (byte[] m : this.outgoingKexQueue) {
                    this.sendMessageSync(m, true);
                }
                this.outgoingKexQueue.clear();
                this.localkex = null;
                this.remotekex = null;
            }
        }
        catch (Throwable ex) {
            EventServiceImplementation.getInstance().fireEvent(new Event((Object)this, 5, ex, this.getUuid()).addAttribute("CLIENT", this.getClient()).addAttribute("USING_PUBLICKEY", this.hostKeyAlg).addAttribute("USING_KEY_EXCHANGE", this.keyExchangeAlg).addAttribute("USING_CS_CIPHER", this.cipherCS).addAttribute("USING_SC_CIPHERC", this.cipherSC).addAttribute("USING_CS_MAC", this.macCS).addAttribute("USING_SC_MAC", this.macSC).addAttribute("USING_CS_COMPRESSION", this.compressionCS).addAttribute("USING_SC_COMPRESSION", this.compressionSC));
            EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.client, 4, true, this.getUuid()).addAttribute("CLIENT", this.getClient()));
            if (ex instanceof SshException) {
                throw (SshException)ex;
            }
            throw new SshException(ex, 5);
        }
        finally {
            if (bar != null) {
                try {
                    bar.close();
                }
                catch (IOException iOException) {}
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void processVerboseSignature(SshPublicKey hostKey) {
        if (AdaptiveConfiguration.getBoolean("verboseSignatures", false, this.provider.getHost(), this.getIdent())) {
            String filename = AdaptiveConfiguration.getProperty("verboseSignatures.filename", "client-signatures.dat", this.client.getHost(), this.client.getIdent());
            try {
                ByteArrayWriter writer = new ByteArrayWriter();
                writer.writeUINT64(System.currentTimeMillis());
                writer.writeString(this.remoteIdentification);
                writer.writeString(this.provider.getHost());
                writer.writeString(System.getProperty("java.version") + " " + System.getProperty("java.vendor"));
                if (hostKey.getJCEPublicKey() != null) {
                    writer.writeString(hostKey.getJCEPublicKey().getClass().getCanonicalName());
                } else {
                    writer.writeString("");
                }
                writer.writeString(hostKey.getSigningAlgorithm());
                writer.writeBinaryString(this.keyExchange.getExchangeHash());
                ByteArrayWriter sig = new ByteArrayWriter();
                sig.writeString(hostKey.getSigningAlgorithm());
                sig.writeBinaryString(this.keyExchange.getSignature());
                writer.writeBinaryString(sig.toByteArray());
                writer.writeString(SshKeyUtils.getFormattedKey(hostKey, ""));
                Object object = signatureLock;
                synchronized (object) {
                    FileOutputStream out = new FileOutputStream(new File(filename), true);
                    try {
                        byte[] encoded = writer.toByteArray();
                        out.write(ByteArrayWriter.encodeInt(encoded.length));
                        out.write(encoded);
                        out.flush();
                    }
                    finally {
                        IOUtil.closeStream(out);
                    }
                }
            }
            catch (Throwable e) {
                log.error("Verbose signature logging failed", e);
            }
        }
    }

    private void processKeyExchangeAlgorithms() throws IOException, SshException {
        ArrayList<IncompatibleAlgorithm> incompatibleReports = new ArrayList<IncompatibleAlgorithm>();
        if (log.isDebugEnabled()) {
            this.debug("Remote computer supports client->server ciphers: " + this.remoteCiphersCS, new Object[0]);
        }
        this.cipherCS = this.selectNegotiatedComponent(this.transportContext.supportedCiphersCS(), this.localCiphersCS, this.checkValidString("client->server cipher", this.remoteCiphersCS), IncompatibleAlgorithm.ComponentType.CIPHER_CS, incompatibleReports);
        if (log.isDebugEnabled()) {
            this.debug("Remote computer supports server->client ciphers: " + this.remoteCiphersSC, new Object[0]);
        }
        this.cipherSC = this.selectNegotiatedComponent(this.transportContext.supportedCiphersSC(), this.localCiphersSC, this.checkValidString("server->client cipher", this.remoteCiphersSC), IncompatibleAlgorithm.ComponentType.CIPHER_SC, incompatibleReports);
        if (log.isDebugEnabled()) {
            this.debug("Remote computer supports client->server hmacs: " + this.remoteMacsCS, new Object[0]);
        }
        this.macCS = this.selectNegotiatedComponent(this.transportContext.supportedMacsCS(), this.localMacsCS, this.checkValidString("client->server hmac", this.remoteMacsCS), IncompatibleAlgorithm.ComponentType.MAC_CS, incompatibleReports);
        if (log.isDebugEnabled()) {
            this.debug("Remote computer supports server->client hmacs: " + this.remoteMacsSC, new Object[0]);
        }
        this.macSC = this.selectNegotiatedComponent(this.transportContext.supportedMacsSC(), this.localMacsSC, this.checkValidString("server->client hmac", this.remoteMacsSC), IncompatibleAlgorithm.ComponentType.MAC_SC, incompatibleReports);
        if (log.isDebugEnabled()) {
            this.debug("Remote computer supports client->server compression: " + this.remoteCompressionsCS, new Object[0]);
        }
        this.compressionCS = this.selectNegotiatedComponent(this.transportContext.supportedCompressionsCS(), this.localCompressionsCS, this.checkValidString("client->server compression", this.remoteCompressionsCS), IncompatibleAlgorithm.ComponentType.COMPRESSION_CS, incompatibleReports);
        if (log.isDebugEnabled()) {
            this.debug("Remote computer supports server->client compression: " + this.remoteCompressionsSC, new Object[0]);
        }
        this.compressionSC = this.selectNegotiatedComponent(this.transportContext.supportedCompressionsSC(), this.localCompressionsSC, this.checkValidString("server->client compression", this.remoteCompressionsSC), IncompatibleAlgorithm.ComponentType.COMPRESSION_CS, incompatibleReports);
        if (log.isDebugEnabled()) {
            this.debug("Remote computer supports key exchanges: " + this.remoteKeyExchanges, new Object[0]);
        }
        this.keyExchangeAlg = this.selectNegotiatedComponent(this.transportContext.supportedKeyExchanges(), this.localKeyExchanges, this.checkValidString("keyexchanges", this.remoteKeyExchanges), IncompatibleAlgorithm.ComponentType.KEYEXCHANGE, incompatibleReports);
        if (log.isDebugEnabled()) {
            this.debug("Remote computer supports public keys: " + this.remotePublicKeys, new Object[0]);
        }
        this.hostKeyAlg = this.selectNegotiatedComponent(this.transportContext.supportedPublicKeys(), this.localPublicKeys, this.checkValidString("keyexchanges", this.remotePublicKeys), IncompatibleAlgorithm.ComponentType.PUBLICKEY, incompatibleReports);
        if (incompatibleReports.isEmpty()) {
            return;
        }
        this.getContext().getSecurityPolicy().onIncompatibleSecurity(this.provider.getHost(), this.provider.getPort(), this.remoteIdentification, incompatibleReports.toArray(new IncompatibleAlgorithm[0]));
        throw new SshException("Failed to negotiate algorithms", 57350, incompatibleReports);
    }

    public String getKnownHostName() {
        String host = this.provider.getHost();
        if (!AdaptiveConfiguration.getBoolean("knownHosts.disablePortValidate", false, this.provider.getHost(), this.getIdent()) && this.provider.getPort() != 22) {
            host = "[" + host + "]:" + this.provider.getPort();
        }
        return host;
    }

    void completedAuthentication() {
        if (this.incomingCompression != null && this.incomingCompression.getAlgorithm().equals("zlib@openssh.com")) {
            if (log.isDebugEnabled()) {
                this.debug("Enabling compression on server->client input", new Object[0]);
            }
            this.isIncomingCompressing = true;
        }
        if (this.outgoingCompression != null && this.outgoingCompression.getAlgorithm().equals("zlib@openssh.com") && log.isDebugEnabled()) {
            this.debug("Enabling compression on client->server output", new Object[0]);
        }
        this.isOutgoingCompressing = true;
    }

    public void startService(String servicename) throws SshException {
        ByteArrayWriter baw = new ByteArrayWriter();
        try {
            byte[] msg;
            baw.write(5);
            baw.writeString(servicename);
            if (log.isDebugEnabled()) {
                this.debug("Sending SSH_MSG_SERVICE_REQUEST", new Object[0]);
            }
            this.sendMessage(baw.toByteArray(), true);
            while (this.processMessage(msg = this.readMessage(0L)) || msg[0] != 6) {
            }
            if (log.isDebugEnabled()) {
                this.debug("Received SSH_MSG_SERVICE_ACCEPT", new Object[0]);
            }
        }
        catch (IOException ex) {
            throw new SshException(ex, 5);
        }
        finally {
            try {
                baw.close();
            }
            catch (IOException iOException) {}
        }
    }

    void internalDisconnect(String msg, int reason) {
        if (this.currentState == 4) {
            return;
        }
        if (log.isDebugEnabled()) {
            this.debug("Performing internal disconnect listeners=" + this.listeners.size(), new Object[0]);
        }
        this.lastState = this.currentState;
        this.currentState = 4;
        try {
            this.provider.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        if (this.binaryInput != null) {
            this.binaryInput.close();
        }
        if (this.binaryOutput != null) {
            this.binaryOutput.close();
        }
        for (TransportProtocolListener l : this.listeners) {
            try {
                l.onDisconnect(msg, reason);
            }
            catch (Throwable t) {
                if (!log.isDebugEnabled()) continue;
                this.debug("Error in transport listener", t);
            }
        }
        EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.client, 20, true, this.getUuid()).addAttribute("CLIENT", this.getClient()));
        for (int i = 0; i < this.shutdownHooks.size(); ++i) {
            try {
                this.shutdownHooks.get(i).run();
                continue;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        if (log.isDebugEnabled()) {
            this.debug("Client has been disconnected", new Object[0]);
        }
    }

    void internalDisconnect() {
        this.internalDisconnect("Disconnected", 11);
    }

    void addShutdownHook(Runnable r) {
        if (r != null) {
            this.shutdownHooks.add(r);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean processMessage(byte[] msg) throws SshException {
        try {
            if (msg.length == 0) {
                String reason = "Invalid zero length message payload";
                if (this.incomingCompression != null && this.isIncomingCompressing) {
                    reason = reason + " [compression error?]";
                }
                this.disconnect(2, reason);
                throw new SshException(reason, 5);
            }
            if (this.binary) {
                this.info("Received application data{}{}", System.lineSeparator(), Utils.bytesToHex(msg, Math.min(msg.length, 32), true, true));
            }
            if (this.binaryInput != null) {
                this.binaryInput.log(ByteArrayWriter.encodeInt(msg.length));
                this.binaryInput.log(msg, 0, msg.length);
            }
            switch (msg[0]) {
                case 1: {
                    if (log.isDebugEnabled()) {
                        this.debug("Received SSH_MSG_DISCONNECT", new Object[0]);
                    }
                    try (ByteArrayReader bar = new ByteArrayReader(msg);){
                        bar.skip(1L);
                        int reason = (int)bar.readInt();
                        String description = bar.readString();
                        EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.client, 21, true, this.getUuid()).addAttribute("CLIENT", this.getClient()));
                        for (TransportProtocolListener l : this.listeners) {
                            try {
                                l.onReceivedDisconnect(description, reason);
                            }
                            catch (Throwable throwable) {}
                        }
                        this.internalDisconnect();
                        this.processKnownIssues(reason, description, new SshException(description, 2));
                    }
                }
                case 2: {
                    if (log.isDebugEnabled()) {
                        this.debug("Received SSH_MSG_IGNORE", new Object[0]);
                    }
                    this.assertIdleTimeout();
                    return true;
                }
                case 4: {
                    this.lastActivity = System.currentTimeMillis();
                    if (log.isDebugEnabled()) {
                        this.debug("Received SSH_MSG_DEBUG", new Object[0]);
                    }
                    return true;
                }
                case 7: {
                    this.lastActivity = System.currentTimeMillis();
                    if (log.isDebugEnabled()) {
                        this.debug("Received SSH_MSG_EXT_INFO", new Object[0]);
                    }
                    this.processExtensionInfo(msg);
                    return true;
                }
                case 21: {
                    this.lastActivity = System.currentTimeMillis();
                    if (log.isDebugEnabled()) {
                        this.debug("Received SSH_MSG_NEWKEYS", new Object[0]);
                    }
                    return true;
                }
                case 20: {
                    this.lastActivity = System.currentTimeMillis();
                    if (log.isDebugEnabled()) {
                        this.debug("Received SSH_MSG_KEX_INIT", new Object[0]);
                    }
                    if (this.remotekex != null) {
                        this.disconnect(2, "Key exchange already in progress!");
                        throw new SshException("Key exchange already in progress!", 3);
                    }
                    this.performKeyExchange(msg);
                    return true;
                }
                case 3: {
                    this.lastActivity = System.currentTimeMillis();
                    try (ByteArrayReader bar = new ByteArrayReader(msg);){
                        bar.skip(1L);
                        if (log.isDebugEnabled()) {
                            this.debug("Received SSH_MSG_UNIMPLEMENTED for sequence {}", bar.readInt());
                        }
                    }
                    if (AdaptiveConfiguration.getBoolean("failOnUnimplemented", false, this.provider.getHost(), this.getIdent())) {
                        throw new IllegalStateException("SSH_MSG_UNIMPLEMENTED message returned by remote");
                    }
                    return true;
                }
            }
            this.lastActivity = System.currentTimeMillis();
            return false;
        }
        catch (IOException ex1) {
            throw new SshException(ex1.getMessage(), 5);
        }
    }

    private void processKnownIssues(int reason, String description, SshException e) throws SshException {
        if (log.isInfoEnabled()) {
            this.info("{}. Checking common issues", e.getMessage());
        }
        if (reason == 57352) {
            this.checkDiffieHellmanBackwardsCompatibilityIssues(reason, description, e);
            this.checkForAESGSMKnownIssues();
            this.tryMinimalKeyExchangePacket();
        } else if (this.currentState == 2 || this.lastState == 2) {
            this.checkDiffieHellmanBackwardsCompatibilityIssues(reason, description, e);
            this.reconfigureKeyExchange();
        }
        throw e;
    }

    private void checkForAESGSMKnownIssues() throws SshException {
        if (!AdaptiveConfiguration.getBoolean("disableAESGCMChecks", false, this.getHost(), this.ident)) {
            if (this.cipherCS.equals("aes128-gcm@openssh.com") || this.cipherCS.equals("aes256-gcm@openssh.com")) {
                if (log.isInfoEnabled()) {
                    this.info("Detected EOF when negotiated cipher is AES in GCM mode. Retry with different cipher preference.", new Object[0]);
                }
                throw new SshException("Detected EOF when negotiated cipher is AES in GCM mode. Retry with different cipher preference.", 57345, this.cipherCS);
            }
            if (this.cipherSC.equals("aes128-gcm@openssh.com") || this.cipherSC.equals("aes256-gcm@openssh.com")) {
                if (log.isInfoEnabled()) {
                    this.info("Detected EOF when negotiated cipher is AES in GCM mode. Retry with different cipher preference.", new Object[0]);
                }
                throw new SshException("Detected EOF when negotiated cipher is AES in GCM mode. Retry with different cipher preference.", 57345, this.cipherSC);
            }
        }
    }

    private void tryMinimalKeyExchangePacket() throws SshException {
        if (this.getContext().getTemporaryValue("triedMinimalKex", false)) {
            if (log.isInfoEnabled()) {
                this.info("Looks like we already tried to minimize key exchange packet size. Giving up.", new Object[0]);
            }
            throw new SshException("Received EOF during key exchange", 9);
        }
        this.getContext().setTemporaryValue("triedMinimalKex", true);
        if (log.isInfoEnabled()) {
            this.info("Trying minimal key exchange packet with just the negotiated algorithms.", new Object[0]);
        }
        throw new SshException("Received EOF during key exchange", 57353, Arrays.asList(new IncompatibleAlgorithm(this.transportContext.supportedPublicKeys(), IncompatibleAlgorithm.ComponentType.PUBLICKEY, null, new String[]{this.hostKeyAlg}), new IncompatibleAlgorithm(this.transportContext.supportedKeyExchanges(), IncompatibleAlgorithm.ComponentType.KEYEXCHANGE, null, new String[]{this.keyExchangeAlg}), new IncompatibleAlgorithm(this.transportContext.supportedCiphersCS(), IncompatibleAlgorithm.ComponentType.CIPHER_CS, null, new String[]{this.cipherCS}), new IncompatibleAlgorithm(this.transportContext.supportedCiphersSC(), IncompatibleAlgorithm.ComponentType.CIPHER_SC, null, new String[]{this.cipherSC}), new IncompatibleAlgorithm(this.transportContext.supportedMacsCS(), IncompatibleAlgorithm.ComponentType.MAC_CS, null, new String[]{this.macCS}), new IncompatibleAlgorithm(this.transportContext.supportedMacsSC(), IncompatibleAlgorithm.ComponentType.MAC_SC, null, new String[]{this.macSC}), new IncompatibleAlgorithm(this.transportContext.supportedCompressionsCS(), IncompatibleAlgorithm.ComponentType.COMPRESSION_CS, null, new String[]{this.compressionCS}), new IncompatibleAlgorithm(this.transportContext.supportedCompressionsSC(), IncompatibleAlgorithm.ComponentType.COMPRESSION_SC, null, new String[]{this.compressionSC})));
    }

    private void reconfigureKeyExchange() throws SshException {
        if (AdaptiveConfiguration.getBoolean("reconfigureKexOnFailure", true, this.getHost(), this.getIdent())) {
            this.getContext().supportedKeyExchanges().remove(this.keyExchangeAlg);
            throw new SshException("Removed key exchange configuration " + this.keyExchangeAlg + " due to error", 57354);
        }
    }

    private void checkDiffieHellmanBackwardsCompatibilityIssues(int reason, String description, SshException e) throws SshException {
        if (this.keyExchangeAlg.contains("-exchange")) {
            if (!this.getContext().isDHGroupExchangeBackwardsCompatible() && !this.getContext().getTemporaryValue("triedDHBackwardsCompatibility", false)) {
                if (log.isInfoEnabled()) {
                    this.info("{} detected. Turning on backwards compatibility", this.keyExchangeAlg);
                }
                this.getContext().setDHGroupExchangeBackwardsCompatible(true);
                this.getContext().setTemporaryValue("triedDHBackwardsCompatibility", true);
                throw new SshException("Changed key exchange configuration", 57354);
            }
            if (this.getContext().isDHGroupExchangeBackwardsCompatible() && !this.getContext().getTemporaryValue("triedDHBackwardsCompatibility", false)) {
                if (log.isInfoEnabled()) {
                    this.info("{} detected. Turning off backwards compatibility", this.keyExchangeAlg);
                }
                this.getContext().setDHGroupExchangeBackwardsCompatible(false);
                this.getContext().setTemporaryValue("triedDHBackwardsCompatibility", true);
                throw new SshException("Changed key exchange configuration", 57354);
            }
            this.getContext().setTemporaryValue("triedDHBackwardsCompatibility", false);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void processExtensionInfo(byte[] msg) throws IOException {
        try (ByteArrayReader reader = new ByteArrayReader(msg);){
            reader.skip(1L);
            int count = (int)reader.readInt();
            if (log.isDebugEnabled()) {
                this.debug("Server supports {} extensions", count);
            }
            for (int i = 0; i < count; ++i) {
                String name = reader.readString();
                String value = reader.readString();
                if (log.isDebugEnabled()) {
                    this.debug("{}={}", name, value);
                }
                this.client.addAttribute(name, value);
            }
        }
    }

    boolean isTransportMessage(int messageid) {
        switch (messageid) {
            case 1: 
            case 2: 
            case 4: 
            case 20: 
            case 21: {
                return true;
            }
        }
        if (this.keyExchange != null) {
            return this.keyExchange.isKeyExchangeMessage(messageid);
        }
        return false;
    }

    String selectNegotiatedComponent(ComponentFactory<?> factory, String locallist, String remotelist, IncompatibleAlgorithm.ComponentType type, List<IncompatibleAlgorithm> incompatibleReports) {
        int idx;
        String list = remotelist;
        String llist = locallist;
        Vector<String> r = new Vector<String>();
        while ((idx = list.indexOf(",")) > -1) {
            r.addElement(list.substring(0, idx).trim());
            list = list.substring(idx + 1).trim();
        }
        r.addElement(list.trim());
        while ((idx = llist.indexOf(",")) > -1) {
            String name = llist.substring(0, idx).trim();
            if (r.contains(name)) {
                return name;
            }
            llist = llist.substring(idx + 1).trim();
        }
        if (r.contains(llist)) {
            return llist;
        }
        EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.client, 32, true, this.getUuid()).addAttribute("CLIENT", this.getClient()).addAttribute("LOCAL_COMPONENT_LIST", locallist).addAttribute("REMOTE_COMPONENT_LIST", remotelist));
        incompatibleReports.add(new IncompatibleAlgorithm(factory, type, locallist.split(","), remotelist.split(",")));
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void sendKeyExchangeInit(boolean guess) throws SshException {
        ByteArrayWriter msg = new ByteArrayWriter();
        try {
            List<byte[]> list = this.outgoingKexQueue;
            synchronized (list) {
                this.numIncomingBytesSinceKEX = 0;
                this.numIncomingPacketsSinceKEX = 0;
                this.numOutgoingBytesSinceKEX = 0;
                this.numOutgoingPacketsSinceKEX = 0;
                this.currentState = 2;
                byte[] cookie = new byte[16];
                ComponentManager.getInstance().getRND().nextBytes(cookie);
                msg.write(20);
                msg.write(cookie);
                if (this.supportsExtensionNegotiation) {
                    msg.writeString(this.logList(this.localKeyExchanges + ",ext-info-c", "Local key exchanges"));
                } else {
                    msg.writeString(this.logList(this.localKeyExchanges, "Local key exchanges"));
                }
                msg.writeString(this.logList(this.localPublicKeys, "Local public keys"));
                msg.writeString(this.logList(this.localCiphersCS, "Local ciphers client->server"));
                msg.writeString(this.logList(this.localCiphersSC, "Local ciphers server->client"));
                msg.writeString(this.logList(this.localMacsCS, "Local hmacs client->server"));
                msg.writeString(this.logList(this.localMacsSC, "Local hmacs server->client"));
                msg.writeString(this.logList(this.localCompressionsCS, "Local compressions client->server"));
                msg.writeString(this.logList(this.localCompressionsSC, "Local compressions server-client"));
                msg.writeString("");
                msg.writeString("");
                msg.writeBoolean(guess);
                msg.writeInt(0);
                if (log.isDebugEnabled()) {
                    this.debug("Sending SSH_MSG_KEX_INIT", new Object[0]);
                }
                this.localkex = msg.toByteArray();
                this.sendMessage(this.localkex, true);
            }
        }
        catch (IOException ex) {
            throw new SshException(ex, 5);
        }
        finally {
            try {
                msg.close();
            }
            catch (IOException iOException) {}
        }
    }

    private String logList(String list, String name) {
        if (log.isDebugEnabled()) {
            this.debug(name + " " + list, new Object[0]);
        }
        return list;
    }

    public byte[] makeSshKey(char chr, int sizeRequired) throws IOException {
        try (ByteArrayWriter keydata = new ByteArrayWriter();){
            byte[] data = new byte[20];
            Digest hash = (Digest)ComponentManager.getInstance().supportedDigests().getInstance(this.keyExchange.getHashAlgorithm());
            hash.putBigInteger(this.keyExchange.getSecret());
            hash.putBytes(this.keyExchange.getExchangeHash());
            hash.putByte((byte)chr);
            hash.putBytes(this.sessionIdentifier);
            data = hash.doFinal();
            keydata.write(data);
            while (keydata.size() < sizeRequired) {
                hash.reset();
                hash.putBigInteger(this.keyExchange.getSecret());
                hash.putBytes(this.keyExchange.getExchangeHash());
                hash.putBytes(keydata.toByteArray());
                data = hash.doFinal();
                keydata.write(data);
            }
            byte[] byArray = keydata.toByteArray();
            return byArray;
        }
    }

    private String checkValidString(String id, String str) throws IOException {
        if (str.trim().equals("")) {
            throw new IOException("Server sent invalid " + id + " value '" + str + "'");
        }
        StringTokenizer t = new StringTokenizer(str, ",");
        if (!t.hasMoreElements()) {
            throw new IOException("Server sent invalid " + id + " value '" + str + "'");
        }
        return str;
    }

    @Override
    public ExecutorService getExecutorService() {
        return this.getContext().getExecutorService();
    }

    public String[] getRemoteKeyExchanges() {
        return this.remoteKeyExchanges.split(",");
    }

    public String[] getRemotePublicKeys() {
        return this.remotePublicKeys.split(",");
    }

    public String[] getRemoteCiphersCS() {
        return this.remoteCiphersCS.split(",");
    }

    public String[] getRemoteCiphersSC() {
        return this.remoteCiphersSC.split(",");
    }

    public String[] getRemoteMacsCS() {
        return this.remoteMacsCS.split(",");
    }

    public String[] getRemoteMacsSC() {
        return this.remoteMacsSC.split(",");
    }

    public String[] getRemoteCompressionsCS() {
        return this.remoteCompressionsCS.split(",");
    }

    public String[] getRemoteCompressionsSC() {
        return this.remoteCompressionsSC.split(",");
    }

    public String[] getLocalKeyExchanges() {
        return this.localKeyExchanges.split(",");
    }

    public String[] getLocalPublicKeys() {
        return this.localPublicKeys.split(",");
    }

    public String[] getLocalCiphersCS() {
        return this.localCiphersCS.split(",");
    }

    public String[] getLocalCiphersSC() {
        return this.localCiphersSC.split(",");
    }

    public String[] getLocalMacsCS() {
        return this.localMacsCS.split(",");
    }

    public String[] getLocalMacsSC() {
        return this.localMacsSC.split(",");
    }

    public String[] getLocalCompressionsCS() {
        return this.localCompressionsCS.split(",");
    }

    public String[] getLocalCompressionsSC() {
        return this.localCompressionsSC.split(",");
    }

    @Override
    public String getIdent() {
        return this.ident;
    }

    @Override
    public String getHost() {
        return this.provider.getHost();
    }

    @Override
    public String getUuid() {
        return this.uuid.toString();
    }
}

