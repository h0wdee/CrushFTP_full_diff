/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh1;

import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.ChannelEventListener;
import com.maverick.ssh.ChannelOpenException;
import com.maverick.ssh.ForwardingRequestListener;
import com.maverick.ssh.PasswordAuthentication;
import com.maverick.ssh.PublicKeyAuthentication;
import com.maverick.ssh.SshAuthentication;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshClientConnector;
import com.maverick.ssh.SshClientListener;
import com.maverick.ssh.SshConnector;
import com.maverick.ssh.SshContext;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshSession;
import com.maverick.ssh.SshTransport;
import com.maverick.ssh.SshTunnel;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.Digest;
import com.maverick.ssh.components.SshRsaPrivateCrtKey;
import com.maverick.ssh.components.SshRsaPublicKey;
import com.maverick.ssh.message.SshMessage;
import com.maverick.ssh1.Ssh1ChallengeResponseAuthentication;
import com.maverick.ssh1.Ssh1Protocol;
import com.maverick.ssh1.Ssh1ProtocolListener;
import com.maverick.ssh1.Ssh1RhostsRsaAuthentication;
import com.maverick.ssh1.Ssh1Session;
import com.maverick.util.ByteArrayWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ssh1Client
implements SshClient {
    static Logger log = LoggerFactory.getLogger(Ssh1Client.class);
    static final int SSH_CMSG_AUTH_PASSWORD = 9;
    static final int SSH_CMSG_AUTH_RSA = 6;
    static final int SSH_SMSG_AUTH_RSA_CHALLENGE = 7;
    static final int SSH_CMSG_AUTH_RSA_RESPONSE = 8;
    static final int SSH_MSG_PORT_OPEN = 29;
    static final int SSH_CMSG_PORT_FORWARD_REQUEST = 28;
    static final int SSH_CMSG_AUTH_RHOSTS_RSA = 35;
    static final int SSH_CMSG_AUTH_TIS = 39;
    static final int SSH_SMSG_AUTH_TIS_CHALLENGE = 40;
    static final int SSH_CMSG_AUTH_TIS_RESPONSE = 41;
    String username;
    boolean authenticated = false;
    boolean buffered;
    Ssh1Session session;
    Ssh1Protocol protocol;
    String remoteIdentification;
    SshAuthentication auth;
    SshClientConnector connector;
    SshTransport transport;
    List<SshClientListener> listeners = new ArrayList<SshClientListener>();
    Hashtable<String, Object> attributes = new Hashtable();
    String ident;

    @Override
    public String getUuid() {
        return this.protocol.getUuid();
    }

    @Override
    public void connect(SshTransport transport, SshContext context, SshClientConnector connector, String username, String localIdentification, String remoteIdentification, boolean buffered) throws SshException {
        this.transport = transport;
        this.ident = AdaptiveConfiguration.getIdent(remoteIdentification);
        if (connector == null || !((SshConnector)connector).isLicensed()) {
            throw new SshException("You cannot create Ssh1Client instances directly", 4);
        }
        for (SshClientListener listener : this.listeners) {
            try {
                listener.connected(this, remoteIdentification);
            }
            catch (Throwable throwable) {}
        }
        this.protocol = new Ssh1Protocol(transport, context, this.ident, new Ssh1ProtocolListener(){

            @Override
            public void disconnected() {
                for (SshClientListener listener : Ssh1Client.this.listeners) {
                    try {
                        listener.disconnected(Ssh1Client.this, "Disconnected", 0);
                    }
                    catch (Throwable throwable) {}
                }
            }
        });
        this.username = username;
        this.buffered = buffered;
        this.connector = connector;
        this.remoteIdentification = remoteIdentification;
        if (username == null) {
            throw new SshException("You must supply a valid username!", 4);
        }
        this.protocol.readServersPublicKey();
        this.protocol.setupSession();
        this.authenticated = this.protocol.declareUsername(username);
        for (SshClientListener listener : this.listeners) {
            try {
                listener.authenticationStarted(this, new String[0]);
            }
            catch (Throwable throwable) {}
        }
    }

    @Override
    public void addListener(SshClientListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public String getRemoteIdentification() {
        return this.remoteIdentification;
    }

    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive exception aggregation
     */
    @Override
    public int authenticate(SshAuthentication auth) throws SshException {
        try {
            if (this.authenticated) {
                throw new SshException("The connection has already been authenticated!", 4);
            }
            if (auth.getUsername() == null) {
                auth.setUsername(this.username);
            }
            if (auth instanceof PasswordAuthentication) {
                try (ByteArrayWriter msg = new ByteArrayWriter();){
                    msg.write(9);
                    msg.writeString(((PasswordAuthentication)auth).getPassword());
                    this.protocol.sendMessage(msg.toByteArray());
                    this.authenticated = this.protocol.hasSucceeded();
                    if (this.authenticated) {
                        this.auth = auth;
                        for (SshClientListener listener : this.listeners) {
                            try {
                                listener.authenticated(this, auth.getUsername());
                            }
                            catch (Throwable throwable) {}
                        }
                        int n = 1;
                        return n;
                    }
                    int n = 2;
                    return n;
                }
            }
            if (auth instanceof Ssh1RhostsRsaAuthentication) {
                Ssh1RhostsRsaAuthentication hba = (Ssh1RhostsRsaAuthentication)auth;
                if (hba.getPublicKey() instanceof SshRsaPublicKey && hba.getPrivateKey() instanceof SshRsaPrivateCrtKey) {
                    SshRsaPublicKey publickey = (SshRsaPublicKey)hba.getPublicKey();
                    try (ByteArrayWriter msg = new ByteArrayWriter();){
                        msg.write(35);
                        msg.writeString(hba.getClientUsername());
                        msg.writeInt(publickey.getBitLength());
                        msg.writeMPINT(publickey.getPublicExponent());
                        msg.writeMPINT(publickey.getModulus());
                        if (log.isDebugEnabled()) {
                            log.debug("Sending SSH_CMSG_AUTH_RHOSTS_RSA");
                        }
                        this.protocol.sendMessage(msg.toByteArray());
                        this.authenticated = this.performRSAChallenge(true, (SshRsaPrivateCrtKey)hba.getPrivateKey());
                        if (this.authenticated) {
                            this.auth = auth;
                            for (SshClientListener listener : this.listeners) {
                                try {
                                    listener.authenticated(this, auth.getUsername());
                                }
                                catch (Throwable throwable) {}
                            }
                            int n = 1;
                            return n;
                        }
                        int n = 2;
                        return n;
                    }
                }
                throw new SshException("Only SSH1 RSA keys are suitable for SSH1 hostbased authentication", 4);
            }
            if (auth instanceof PublicKeyAuthentication) {
                PublicKeyAuthentication pka = (PublicKeyAuthentication)auth;
                if (pka.getPublicKey() instanceof SshRsaPublicKey) {
                    SshRsaPublicKey publickey = (SshRsaPublicKey)pka.getPublicKey();
                    try (ByteArrayWriter msg = new ByteArrayWriter();){
                        msg.write(6);
                        msg.writeMPINT(publickey.getModulus());
                        if (log.isDebugEnabled()) {
                            log.debug("Sending SSH_CMSG_AUTH_RSA");
                        }
                        this.protocol.sendMessage(msg.toByteArray());
                        this.authenticated = this.performRSAChallenge(pka.isAuthenticating(), (SshRsaPrivateCrtKey)pka.getPrivateKey());
                        if (pka.isAuthenticating()) {
                            if (this.authenticated) {
                                this.auth = auth;
                                for (SshClientListener listener : this.listeners) {
                                    try {
                                        listener.authenticated(this, auth.getUsername());
                                    }
                                    catch (Throwable throwable) {}
                                }
                                int n = 1;
                                return n;
                            }
                            int n = 2;
                            return n;
                        }
                        int n = 5;
                        return n;
                    }
                }
                throw new SshException("Only SSH1 RSA private keys are acceptable for SSH1 RSA Authentication", 4);
            }
            if (auth instanceof Ssh1ChallengeResponseAuthentication) {
                if (((Ssh1ChallengeResponseAuthentication)auth).getPrompt() == null) {
                    throw new SshException("SSH1 challenge-response requires prompt!", 4);
                }
                try (ByteArrayWriter msg = new ByteArrayWriter();){
                    msg.write(39);
                    if (log.isDebugEnabled()) {
                        log.debug("Sending SSH_CMSG_AUTH_TIS");
                    }
                    this.protocol.sendMessage(msg.toByteArray());
                    this.authenticated = this.performChallengeResponse((Ssh1ChallengeResponseAuthentication)auth);
                    if (this.authenticated) {
                        this.auth = auth;
                        for (SshClientListener listener : this.listeners) {
                            try {
                                listener.authenticated(this, auth.getUsername());
                            }
                            catch (Throwable throwable) {}
                        }
                        int n = 1;
                        return n;
                    }
                    int n = 2;
                    return n;
                }
            }
            throw new SshException("Unsupported SSH1 authentication type!", 4);
        }
        catch (IOException ex) {
            throw new SshException("Ssh1Client.authenticate caught an IOException: " + ex.getMessage(), 5);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean performChallengeResponse(Ssh1ChallengeResponseAuthentication auth) throws SshException {
        ByteArrayWriter msg;
        SshMessage bar = new SshMessage(this.protocol.nextMessage(0L));
        try {
            if (bar.getMessageId() == 40) {
                if (log.isDebugEnabled()) {
                    log.debug("Received SSH_SMSG_AUTH_TIS_CHALLENGE");
                }
                String challenge = bar.readString();
                String response = auth.getPrompt().getResponse(challenge);
                if (response != null) {
                    msg = new ByteArrayWriter();
                    msg.write(41);
                    msg.writeString(response);
                    if (log.isDebugEnabled()) {
                        log.debug("Sending SSH_CMSG_AUTH_TIS_RESPONSE");
                    }
                    this.protocol.sendMessage(msg.toByteArray());
                    boolean bl = this.protocol.hasSucceeded();
                    return bl;
                }
                boolean bl = false;
                return bl;
            }
            boolean challenge = false;
            return challenge;
        }
        catch (IOException ex) {}
        {
            finally {
                msg.close();
            }
        }
        {
            throw new SshException("Ssh1Client.performChallengeResponse() caught an IOException: " + ex.getMessage(), 5);
        }
        finally {
            try {
                bar.close();
            }
            catch (IOException iOException) {}
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean performRSAChallenge(boolean isAuthenticating, SshRsaPrivateCrtKey privatekey) throws SshException {
        ByteArrayWriter msg;
        SshMessage bar = new SshMessage(this.protocol.nextMessage(0L));
        try {
            if (bar.getMessageId() == 7) {
                if (log.isDebugEnabled()) {
                    log.debug("Received SSH_SMSG_AUTH_RSA_CHALLENGE");
                }
                byte[] response = new byte[16];
                if (isAuthenticating && privatekey != null) {
                    BigInteger challenge = bar.readMPINT();
                    challenge = this.removePKCS1(challenge.modPow(privatekey.getPrivateExponent(), privatekey.getModulus()), 2);
                    response = challenge.toByteArray();
                    Digest hash = (Digest)ComponentManager.getInstance().supportedDigests().getInstance("MD5");
                    if (response[0] == 0) {
                        hash.putBytes(response, 1, 32);
                    } else {
                        hash.putBytes(response, 0, 32);
                    }
                    hash.putBytes(this.protocol.sessionId);
                    response = hash.doFinal();
                }
                msg = new ByteArrayWriter();
                msg.write(8);
                msg.write(response);
                if (log.isDebugEnabled()) {
                    log.debug("Sending SSH_CMSG_AUTH_RSA_RESPONSE");
                }
                this.protocol.sendMessage(msg.toByteArray());
                boolean bl = this.protocol.hasSucceeded();
                return bl;
            }
            boolean response = false;
            return response;
        }
        catch (IOException ex) {}
        {
            finally {
                msg.close();
            }
        }
        {
            throw new SshException("Ssh1Client.performRSAChallenge() caught an IOException: " + ex.getMessage(), 5);
        }
        finally {
            try {
                bar.close();
            }
            catch (IOException iOException) {}
        }
    }

    private BigInteger removePKCS1(BigInteger input, int type) throws IllegalStateException {
        int i;
        byte[] strip = input.toByteArray();
        if (strip[0] != type) {
            throw new IllegalStateException("PKCS1 padding type " + type + " is not valid");
        }
        for (i = 1; i < strip.length && strip[i] != 0; ++i) {
            if (type != 1 || strip[i] == -1) continue;
            throw new IllegalStateException("Corrupt data found in expected PKSC1 padding");
        }
        if (i == strip.length) {
            throw new IllegalStateException("Corrupt data found in expected PKSC1 padding");
        }
        byte[] val = new byte[strip.length - i];
        System.arraycopy(strip, i, val, 0, val.length);
        return new BigInteger(1, val);
    }

    @Override
    public SshSession openSessionChannel() throws SshException, ChannelOpenException {
        return this.openSessionChannel(null);
    }

    @Override
    public SshSession openSessionChannel(ChannelEventListener listener) throws SshException, ChannelOpenException {
        return this.openSessionChannel(listener, AdaptiveConfiguration.getInt("messageTimeout", this.getContext().getMessageTimeout(), this.getHost(), this.getIdent()));
    }

    @Override
    public SshSession openSessionChannel(long timeout) throws SshException, ChannelOpenException {
        return this.openSessionChannel(null, timeout);
    }

    @Override
    public SshSession openSessionChannel(ChannelEventListener listener, long timeout) throws SshException, ChannelOpenException {
        if (!this.authenticated) {
            throw new SshException("The connection must be authenticated first!", 4);
        }
        if (this.session == null) {
            this.session = new Ssh1Session(this.protocol, this, listener, this.buffered);
            if (this.protocol.context.getX11Display() != null && !this.session.isXForwarding) {
                this.session.requestXForwarding(this.protocol.context.getX11Display(), this.protocol.context.getX11RequestListener());
            }
            for (SshClientListener clientListener : this.listeners) {
                try {
                    clientListener.sessionOpened(this, this.session);
                }
                catch (Throwable throwable) {}
            }
            return this.session;
        }
        return this.duplicate().openSessionChannel(listener);
    }

    @Override
    public SshSession openSessionChannel(int windowspace, int packetsize, ChannelEventListener listener) throws ChannelOpenException, SshException {
        if (log.isWarnEnabled()) {
            log.warn("Attempting to open SSH1 session with specific window space and packet size parameters which will be ignored");
        }
        return this.openSessionChannel(listener);
    }

    @Override
    public SshSession openSessionChannel(int windowspace, int packetsize, ChannelEventListener listener, long timeout) throws ChannelOpenException, SshException {
        if (log.isWarnEnabled()) {
            log.warn("Attempting to open SSH1 session with specific window space and packet size parameters which will be ignored");
        }
        return this.openSessionChannel(listener, timeout);
    }

    @Override
    public SshTunnel openForwardingChannel(String hostname, int port, String listeningAddress, int listeningPort, String originatingHost, int originatingPort, SshTransport transport, ChannelEventListener listener) throws SshException, ChannelOpenException {
        if (this.session == null || !this.session.interactive) {
            throw new SshException("SSH1 forwarding channels can only be opened after the user's shell has been started!", 4);
        }
        return this.session.openForwardingChannel(hostname, port, listeningAddress, listeningPort, originatingHost, originatingPort, transport, listener);
    }

    @Override
    public SshClient openRemoteClient(String hostname, int port, String username, SshClientConnector con) throws SshException, ChannelOpenException {
        SshTunnel tunnel = this.openForwardingChannel(hostname, port, "127.0.0.1", 22, "127.0.0.1", 22, null, null);
        return con.connect(tunnel, username);
    }

    @Override
    public SshClient openRemoteClient(String hostname, int port, String username) throws SshException, ChannelOpenException {
        return this.openRemoteClient(hostname, port, username, this.connector);
    }

    public boolean requestXForwarding(String display, ForwardingRequestListener listener) throws SshException {
        if (this.session != null && this.session.interactive) {
            throw new SshException("SSH1 X forwarding requests must be made after opening the session but before starting the shell!", 4);
        }
        if (this.session == null) {
            throw new SshException("SSH1 X forwarding requests must be made after opening the session but before starting the shell!", 4);
        }
        this.protocol.context.setX11Display(display);
        this.protocol.context.setX11RequestListener(listener);
        this.session.requestXForwarding(display, listener);
        return this.session.isXForwarding;
    }

    @Override
    public int requestRemoteForwarding(String bindAddress, int bindPort, String hostToConnect, int portToConnect, ForwardingRequestListener listener) throws SshException {
        if (this.session != null && this.session.interactive) {
            throw new SshException("SSH1 forwarding requests must be made after opening the session but before starting the shell!", 4);
        }
        if (this.session == null) {
            throw new SshException("SSH1 forwarding requests must be made after opening the session but before starting the shell!", 4);
        }
        return this.session.requestForwarding(bindPort, hostToConnect, portToConnect, listener);
    }

    @Override
    public boolean cancelRemoteForwarding(String bindAddress, int bindPort) throws SshException {
        return false;
    }

    @Override
    public void disconnect() {
        try {
            if (this.session != null) {
                this.session.signalClosingState();
            }
            this.protocol.disconnect("The user disconnected the application");
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public void exit() {
        try {
            if (this.session != null) {
                this.session.signalClosingState();
            }
            this.protocol.disconnect("The user disconnected the application");
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public boolean isConnected() {
        return this.protocol.getState() == 2;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public SshClient duplicate() throws SshException {
        if (this.username == null || this.auth == null) {
            throw new SshException("Cannot duplicate! The existing connection does not have a set of credentials", 4);
        }
        try {
            SshClient duplicate = this.connector.connect(this.protocol.transport.duplicate(), this.username, this.buffered, this.protocol.context);
            for (SshClientListener listener : this.listeners) {
                duplicate.addListener(listener);
            }
            if (!duplicate.isAuthenticated() && duplicate.authenticate(this.auth) != 1) {
                throw new SshException("Duplication attempt failed to authenicate user!", 5);
            }
            return duplicate;
        }
        catch (IOException ex) {
            throw new SshException("Failed to duplicate SshClient", 10);
        }
    }

    @Override
    public SshContext getContext() {
        return this.protocol.context;
    }

    @Override
    public int getChannelCount() {
        if (this.session == null) {
            return 0;
        }
        return this.session.getChannelCount();
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public boolean isBuffered() {
        return this.buffered;
    }

    @Override
    public SshTransport getTransport() {
        return this.transport;
    }

    public String toString() {
        return "SSH1 " + this.protocol.transport.getHost() + ":" + this.protocol.transport.getPort() + "[cipher=" + (this.protocol.decryption == null ? "none" : this.protocol.decryption.getAlgorithm()) + "]";
    }

    @Override
    public void addAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

    @Override
    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    @Override
    public <T> T getAttribute(String key, T defaultValue) {
        if (this.attributes.contains(key)) {
            return (T)this.attributes.get(key);
        }
        return defaultValue;
    }

    @Override
    public boolean hasAttribute(String key) {
        return this.attributes.containsKey(key);
    }

    @Override
    public String getIdent() {
        return this.ident;
    }

    @Override
    public String getHost() {
        return this.transport.getHost();
    }
}

