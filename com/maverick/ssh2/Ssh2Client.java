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
import com.maverick.ssh.ChannelAdapter;
import com.maverick.ssh.ChannelEventListener;
import com.maverick.ssh.ChannelOpenException;
import com.maverick.ssh.ForwardingRequestListener;
import com.maverick.ssh.PasswordAuthentication;
import com.maverick.ssh.PublicKeyAuthentication;
import com.maverick.ssh.SshAuthentication;
import com.maverick.ssh.SshChannel;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshClientConnector;
import com.maverick.ssh.SshClientListener;
import com.maverick.ssh.SshContext;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshSession;
import com.maverick.ssh.SshTransport;
import com.maverick.ssh.SshTunnel;
import com.maverick.ssh.components.SshComponent;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.Utils;
import com.maverick.ssh.message.SshAbstractChannel;
import com.maverick.ssh2.AuthenticationClient;
import com.maverick.ssh2.AuthenticationProtocol;
import com.maverick.ssh2.ChannelFactory;
import com.maverick.ssh2.ConnectionProtocol;
import com.maverick.ssh2.GlobalRequest;
import com.maverick.ssh2.GlobalRequestHandler;
import com.maverick.ssh2.KBIAuthentication;
import com.maverick.ssh2.KBIPrompt;
import com.maverick.ssh2.KBIRequestHandler;
import com.maverick.ssh2.Ssh2Channel;
import com.maverick.ssh2.Ssh2Context;
import com.maverick.ssh2.Ssh2ForwardingChannel;
import com.maverick.ssh2.Ssh2PasswordAuthentication;
import com.maverick.ssh2.Ssh2PublicKeyAuthentication;
import com.maverick.ssh2.Ssh2Session;
import com.maverick.ssh2.SshKeyExchangeClient;
import com.maverick.ssh2.TransportProtocol;
import com.maverick.ssh2.TransportProtocolListener;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ssh2Client
implements SshClient {
    static Logger log = LoggerFactory.getLogger(Ssh2Client.class);
    TransportProtocol transport;
    SshTransport io;
    AuthenticationProtocol authentication;
    ConnectionProtocol connection;
    String localIdentification;
    String remoteIdentification;
    String[] authenticationMethods;
    boolean bypassNoneAuthentication;
    String username;
    Hashtable<String, ForwardingRequestListener> forwardingListeners = new Hashtable();
    Hashtable<String, String> forwardingDestinations = new Hashtable();
    ForwardingRequestChannelFactory requestFactory = new ForwardingRequestChannelFactory();
    SshAuthentication auth;
    SshClientConnector connector;
    boolean isXForwarding = false;
    boolean buffered;
    Ssh2Context context;
    List<SshClientListener> listeners = new ArrayList<SshClientListener>();
    Hashtable<String, Object> attributes = new Hashtable();

    @Override
    public String getUuid() {
        return this.transport.getUuid();
    }

    @Override
    public void connect(SshTransport io, SshContext context, SshClientConnector connector, String username, String localIdentification, String remoteIdentification, boolean buffered) throws SshException {
        if (connector == null) {
            throw new SshException("You cannot create Ssh2Client instances directly", 4);
        }
        for (SshClientListener listener : this.listeners) {
            try {
                listener.connected(this, remoteIdentification);
            }
            catch (Throwable throwable) {}
        }
        this.io = io;
        this.localIdentification = localIdentification;
        this.remoteIdentification = remoteIdentification;
        this.username = username;
        this.buffered = buffered;
        this.connector = connector;
        if (username == null) {
            block21: {
                try {
                    io.close();
                }
                catch (IOException ex) {
                    if (!log.isDebugEnabled()) break block21;
                    this.transport.debug(log, "RECIEVED IOException IN Ssh2Client.connect:" + ex.getMessage(), new Object[0]);
                }
            }
            throw new SshException("You must supply a valid username!", 4);
        }
        if (!(context instanceof Ssh2Context)) {
            block22: {
                try {
                    io.close();
                }
                catch (IOException ex) {
                    if (!log.isDebugEnabled()) break block22;
                    this.transport.debug(log, "RECIEVED IOException IN Ssh2Client.connect:" + ex.getMessage(), new Object[0]);
                }
            }
            throw new SshException("Ssh2Context required!", 4);
        }
        this.context = (Ssh2Context)context;
        this.transport = this.createTransportProtocol(buffered);
        if (log.isDebugEnabled()) {
            this.transport.debug(log, "Connecting " + username + "@" + io.getHost() + ":" + io.getPort(), new Object[0]);
        }
        if (log.isDebugEnabled()) {
            this.transport.debug(log, "Remote identification is " + remoteIdentification, new Object[0]);
        }
        this.transport.addListener(new TransportProtocolListener(){

            @Override
            public void onIdle(long lastActivity) {
                for (SshClientListener clientListener : Ssh2Client.this.listeners) {
                    try {
                        clientListener.idle(Ssh2Client.this, lastActivity);
                    }
                    catch (Throwable throwable) {}
                }
            }

            @Override
            public void onDisconnect(String msg, int reason) {
                if (log.isDebugEnabled()) {
                    Ssh2Client.this.transport.debug(log, "Notifying " + Ssh2Client.this.listeners.size() + " client listeners", new Object[0]);
                }
                for (SshClientListener clientListener : Ssh2Client.this.listeners) {
                    try {
                        clientListener.disconnected(Ssh2Client.this, msg, reason);
                    }
                    catch (Throwable e) {
                        if (!log.isDebugEnabled()) continue;
                        Ssh2Client.this.transport.debug(log, "Error in listener", e);
                    }
                }
            }

            @Override
            public void onReceivedDisconnect(String msg, int reason) {
                for (SshClientListener clientListener : Ssh2Client.this.listeners) {
                    try {
                        clientListener.disconnecting(Ssh2Client.this, msg, reason);
                    }
                    catch (Throwable throwable) {}
                }
            }
        });
        EventServiceImplementation.getInstance().fireEvent(new Event((Object)this, 100, true, this.getUuid()).addAttribute("CLIENT", this).addAttribute("REMOTE_IDENT", remoteIdentification).addAttribute("LOCAL_IDENT", localIdentification.trim()));
        if (log.isDebugEnabled()) {
            this.transport.debug(log, "Starting transport protocol", new Object[0]);
        }
        this.transport.startTransportProtocol(io, (Ssh2Context)context, localIdentification, remoteIdentification, this);
        for (SshClientListener listener : this.listeners) {
            try {
                listener.keyExchangeComplete(this, this.transport.hostkey, this.algorithmOrNone(this.transport.keyExchange), this.algorithmOrNone(this.transport.encryption), this.algorithmOrNone(this.transport.decryption), this.algorithmOrNone(this.transport.outgoingMac), this.algorithmOrNone(this.transport.incomingMac), this.algorithmOrNone(this.transport.outgoingCompression), this.algorithmOrNone(this.transport.incomingCompression));
            }
            catch (Throwable throwable) {}
        }
        if (log.isDebugEnabled()) {
            this.transport.debug(log, "Starting authentication protocol", new Object[0]);
        }
        this.authentication = new AuthenticationProtocol(this.transport);
        this.authentication.setBannerDisplay(((Ssh2Context)context).getBannerDisplay());
        this.connection = new ConnectionProtocol(this.transport, context, buffered);
        this.connection.addChannelFactory(this.requestFactory);
        String[] methods = this.getAuthenticationMethods(username);
        for (SshClientListener listener : this.listeners) {
            try {
                listener.authenticationStarted(this, methods);
            }
            catch (Throwable throwable) {}
        }
        if (log.isDebugEnabled()) {
            this.transport.debug(log, "SSH connection established", new Object[0]);
        }
    }

    private String algorithmOrNone(SshComponent component) {
        return component == null ? "none" : component.getAlgorithm();
    }

    protected TransportProtocol createTransportProtocol(boolean buffered) {
        return new TransportProtocol(buffered, this.context.getSecurityPolicy().isManagedSecurity());
    }

    public String[] getAuthenticationMethods(String username) throws SshException {
        this.verifyConnection(false);
        if (this.authenticationMethods != null) {
            return this.authenticationMethods;
        }
        if (AdaptiveConfiguration.getBoolean("noneAuthentication", true, this.getHost(), this.getIdent())) {
            if (log.isDebugEnabled()) {
                this.transport.debug(log, "Requesting authentication methods", new Object[0]);
            }
            String methods = this.authentication.getAuthenticationMethods(username, "ssh-connection");
            if (log.isDebugEnabled()) {
                this.transport.debug(log, "Available authentications are " + methods, new Object[0]);
            }
            Vector<String> tmp = new Vector<String>();
            while (methods != null) {
                int idx = methods.indexOf(44);
                if (idx > -1) {
                    tmp.addElement(methods.substring(0, idx));
                    methods = methods.substring(idx + 1);
                    continue;
                }
                tmp.addElement(methods);
                methods = null;
            }
            this.authenticationMethods = new String[tmp.size()];
            tmp.copyInto(this.authenticationMethods);
            if (this.isAuthenticated()) {
                this.connection.start();
            }
        } else {
            this.authenticationMethods = Utils.toArray("password", "publickey", "keyboard-interactive");
        }
        return this.authenticationMethods;
    }

    @Override
    public void addListener(SshClientListener listener) {
        this.listeners.add(listener);
    }

    private boolean supportsKeyboardInteractive() {
        boolean kbiAuthenticationPossible = false;
        for (int i = 0; i < this.authenticationMethods.length; ++i) {
            if (!this.authenticationMethods[i].equals("keyboard-interactive")) continue;
            kbiAuthenticationPossible = true;
        }
        return kbiAuthenticationPossible;
    }

    private boolean supportsPassword() {
        boolean passwordSupported = false;
        for (int i = 0; i < this.authenticationMethods.length; ++i) {
            if (!this.authenticationMethods[i].equals("password")) continue;
            passwordSupported = true;
        }
        return passwordSupported;
    }

    private boolean prefersKeyboardInteractive() {
        return AdaptiveConfiguration.getBoolean("preferKeyboardInteractive", this.context.isPreferKeyboardInteractiveOverPassword(), this.getHost(), this.getIdent()) || !this.supportsPassword();
    }

    private int doPasswordOverKeyboardInteractive(SshAuthentication auth) throws SshException {
        if (log.isDebugEnabled()) {
            this.transport.debug(log, "Switching password authentication to keyboard-interactive passwordSupported=" + this.supportsPassword() + " preferKBI=" + AdaptiveConfiguration.getBoolean("preferKeyboardInteractive", this.context.isPreferKeyboardInteractiveOverPassword(), this.getHost(), this.getIdent()), new Object[0]);
        }
        KBIAuthentication kbi = new KBIAuthentication();
        kbi.setUsername(((PasswordAuthentication)auth).getUsername());
        kbi.setKBIRequestHandler(new KBIRequestHandlerWhenUserUsingPasswordAuthentication((PasswordAuthentication)auth));
        return this.authentication.authenticate(kbi, "ssh-connection");
    }

    public boolean isAuthenticationSupported(String method) throws SshException {
        for (String m : this.getAuthenticationMethods(this.username)) {
            if (!m.equals(method)) continue;
            return true;
        }
        return false;
    }

    @Override
    public int authenticate(SshAuthentication auth) throws SshException {
        this.verifyConnection(false);
        if (this.isAuthenticated()) {
            throw new SshException("User is already authenticated! Did you check isAuthenticated?", 4);
        }
        if (auth.getUsername() == null) {
            auth.setUsername(this.username);
        }
        int result = 2;
        if ((auth instanceof PasswordAuthentication || auth instanceof Ssh2PasswordAuthentication) && this.prefersKeyboardInteractive() && this.supportsKeyboardInteractive() && (result = this.doPasswordOverKeyboardInteractive(auth)) != 1 && !this.supportsPassword()) {
            return result;
        }
        if (result != 1) {
            if (log.isDebugEnabled()) {
                this.transport.debug(log, "Authenticating with " + auth.getMethod(), new Object[0]);
            }
            if (auth instanceof PasswordAuthentication && !(auth instanceof Ssh2PasswordAuthentication)) {
                Ssh2PasswordAuthentication pwd = new Ssh2PasswordAuthentication();
                pwd.setUsername(((PasswordAuthentication)auth).getUsername());
                pwd.setPassword(((PasswordAuthentication)auth).getPassword());
                result = this.authentication.authenticate(pwd, "ssh-connection");
                if (pwd.requiresPasswordChange()) {
                    this.disconnect();
                    throw new SshException("Password change required!", 8);
                }
            } else if (auth instanceof PublicKeyAuthentication && !(auth instanceof Ssh2PublicKeyAuthentication)) {
                Ssh2PublicKeyAuthentication pk = new Ssh2PublicKeyAuthentication();
                pk.setUsername(((PublicKeyAuthentication)auth).getUsername());
                pk.setPublicKey(((PublicKeyAuthentication)auth).getPublicKey());
                pk.setPrivateKey(((PublicKeyAuthentication)auth).getPrivateKey());
                result = this.authentication.authenticate(pk, "ssh-connection");
            } else if (auth instanceof AuthenticationClient) {
                result = this.authentication.authenticate((AuthenticationClient)auth, "ssh-connection");
            } else {
                throw new SshException("Invalid authentication client", 4);
            }
        }
        if (result == 1) {
            this.auth = auth;
            this.connection.start();
            try {
                AdaptiveConfiguration.saveMatchingConfiguration(this.getHost(), Utils.csv(this.transport.getLocalKeyExchanges()), Utils.csv(this.transport.getLocalPublicKeys()), Utils.csv(this.transport.getLocalCiphersCS()), Utils.csv(this.transport.getLocalMacsCS()), Utils.csv(this.transport.getLocalCompressionsCS()));
            }
            catch (IOException e1) {
                log.error("Could not save last known good configuration", (Throwable)e1);
            }
            for (SshClientListener listener : this.listeners) {
                try {
                    listener.authenticated(this, auth.getUsername());
                }
                catch (Throwable throwable) {}
            }
        }
        switch (result) {
            case 1: {
                if (!log.isDebugEnabled()) break;
                this.transport.debug(log, "Authentication complete", new Object[0]);
                break;
            }
            case 2: {
                if (!log.isDebugEnabled()) break;
                this.transport.debug(log, "Authentication failed", new Object[0]);
                break;
            }
            case 3: {
                if (!log.isDebugEnabled()) break;
                this.transport.debug(log, "Authentication successful but further authentication required", new Object[0]);
                break;
            }
            case 4: {
                if (!log.isDebugEnabled()) break;
                this.transport.debug(log, "Authentication cancelled", new Object[0]);
                break;
            }
            case 5: {
                if (!log.isDebugEnabled()) break;
                this.transport.debug(log, "Server accepts the public key provided", new Object[0]);
                break;
            }
            default: {
                if (!log.isDebugEnabled()) break;
                this.transport.debug(log, "Unknown authentication result " + result, new Object[0]);
            }
        }
        return result;
    }

    @Override
    public boolean isAuthenticated() {
        return this.authentication != null && this.authentication.isAuthenticated();
    }

    @Override
    public void disconnect() {
        try {
            if (log.isDebugEnabled()) {
                this.transport.debug(log, "Disconnecting", new Object[0]);
            }
            this.connection.signalClosingState();
            this.transport.disconnect(11, "The user disconnected the application");
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (log.isDebugEnabled()) {
            this.transport.debug(log, "Disconnected", new Object[0]);
        }
    }

    @Override
    public void exit() {
        try {
            if (log.isDebugEnabled()) {
                this.transport.debug(log, "Disconnecting", new Object[0]);
            }
            this.connection.signalClosingState();
            this.transport.disconnect(11, "The user disconnected the application");
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (log.isDebugEnabled()) {
            this.transport.debug(log, "Disconnected", new Object[0]);
        }
    }

    @Override
    public boolean isConnected() {
        return this.transport.isConnected();
    }

    public void forceKeyExchange() throws SshException {
        if (log.isDebugEnabled()) {
            this.transport.debug(log, "Forcing key exchange", new Object[0]);
        }
        this.transport.sendKeyExchangeInit(false);
    }

    @Override
    public SshSession openSessionChannel() throws SshException, ChannelOpenException {
        return this.openSessionChannel(AdaptiveConfiguration.getInt("sessionMaxWindowSpace", this.transport.getContext().getSessionMaxWindowSpace(), this.getHost(), this.getIdent()), AdaptiveConfiguration.getInt("sessionMaxPacketSize", this.transport.getContext().getSessionMaxPacketSize(), this.getHost(), this.getIdent()), null);
    }

    @Override
    public SshSession openSessionChannel(long timeout) throws SshException, ChannelOpenException {
        return this.openSessionChannel(AdaptiveConfiguration.getInt("sessionMaxWindowSpace", this.transport.getContext().getSessionMaxWindowSpace(), this.getHost(), this.getIdent()), AdaptiveConfiguration.getInt("sessionMaxPacketSize", this.transport.getContext().getSessionMaxPacketSize(), this.getHost(), this.getIdent()), null, timeout);
    }

    @Override
    public SshSession openSessionChannel(ChannelEventListener listener, long timeout) throws SshException, ChannelOpenException {
        return this.openSessionChannel(AdaptiveConfiguration.getInt("sessionMaxWindowSpace", this.transport.getContext().getSessionMaxWindowSpace(), this.getHost(), this.getIdent()), AdaptiveConfiguration.getInt("sessionMaxPacketSize", this.transport.getContext().getSessionMaxPacketSize(), this.getHost(), this.getIdent()), listener, timeout);
    }

    @Override
    public SshSession openSessionChannel(ChannelEventListener listener) throws SshException, ChannelOpenException {
        return this.openSessionChannel(AdaptiveConfiguration.getInt("sessionMaxWindowSpace", this.transport.getContext().getSessionMaxWindowSpace(), this.getHost(), this.getIdent()), AdaptiveConfiguration.getInt("sessionMaxPacketSize", this.transport.getContext().getSessionMaxPacketSize(), this.getHost(), this.getIdent()), listener);
    }

    @Override
    public Ssh2Session openSessionChannel(int windowspace, int packetsize, ChannelEventListener listener) throws ChannelOpenException, SshException {
        return this.openSessionChannel(windowspace, packetsize, listener, AdaptiveConfiguration.getInt("messageTimeout", this.getContext().getMessageTimeout(), this.getHost(), this.getIdent()));
    }

    @Override
    public Ssh2Session openSessionChannel(int windowspace, int packetsize, ChannelEventListener listener, long timeout) throws ChannelOpenException, SshException {
        this.verifyConnection(true);
        if (log.isDebugEnabled()) {
            this.transport.debug(log, "Opening session channel windowspace=" + windowspace + " packetsize=" + packetsize, new Object[0]);
        }
        Ssh2Session channel = new Ssh2Session(windowspace, packetsize, this);
        if (listener != null) {
            channel.addChannelEventListener(listener);
        }
        channel.addChannelEventListener(new ChannelAdapter(){

            @Override
            public void channelOpened(SshChannel channel) {
                for (SshClientListener clientListener : Ssh2Client.this.listeners) {
                    try {
                        clientListener.sessionOpened(Ssh2Client.this, (SshSession)channel);
                    }
                    catch (Throwable throwable) {}
                }
            }

            @Override
            public void channelClosed(SshChannel channel) {
                for (SshClientListener clientListener : Ssh2Client.this.listeners) {
                    try {
                        clientListener.sessionClosed(Ssh2Client.this, (SshSession)channel);
                    }
                    catch (Throwable throwable) {}
                }
            }
        });
        this.connection.openChannel(channel, null, timeout);
        if (log.isDebugEnabled()) {
            this.transport.debug(log, "Channel has been opened channelid=" + channel.getChannelId(), new Object[0]);
        }
        if (this.connection.getContext().getX11Display() != null) {
            String display = this.connection.getContext().getX11Display();
            int idx = display.indexOf(58);
            int screen = 0;
            if (idx != -1) {
                display = display.substring(idx + 1);
            }
            if ((idx = display.indexOf(46)) > -1) {
                screen = Integer.parseInt(display.substring(idx + 1));
            }
            byte[] x11FakeCookie = this.connection.getContext().getX11AuthenticationCookie();
            StringBuffer cookieBuf = new StringBuffer();
            for (int i = 0; i < 16; ++i) {
                String b = Integer.toHexString(x11FakeCookie[i] & 0xFF);
                if (b.length() == 1) {
                    b = "0" + b;
                }
                cookieBuf.append(b);
            }
            if (channel.requestX11Forwarding(false, "MIT-MAGIC-COOKIE-1", cookieBuf.toString(), screen)) {
                this.isXForwarding = true;
            }
        }
        return channel;
    }

    @Override
    public SshClient openRemoteClient(String hostname, int port, String username, SshClientConnector con) throws SshException, ChannelOpenException {
        if (log.isDebugEnabled()) {
            this.transport.debug(log, "Opening a remote SSH client from " + this.io.getHost() + " to " + username + "@" + hostname + ":" + port, new Object[0]);
        }
        SshTunnel tunnel = this.openForwardingChannel(hostname, port, "127.0.0.1", 22, "127.0.0.1", 22, null, null);
        return con.connect((SshTransport)tunnel, username, this.buffered);
    }

    @Override
    public SshClient openRemoteClient(String hostname, int port, String username) throws SshException, ChannelOpenException {
        return this.openRemoteClient(hostname, port, username, this.connector);
    }

    @Override
    public SshTunnel openForwardingChannel(String hostname, int port, String listeningAddress, int listeningPort, String originatingHost, int originatingPort, SshTransport sock, ChannelEventListener listener) throws SshException, ChannelOpenException {
        ByteArrayWriter request = new ByteArrayWriter();
        try {
            if (log.isDebugEnabled()) {
                this.transport.debug(log, "Opening forwarding channel from " + listeningAddress + ":" + listeningPort + " to " + hostname + ":" + port, new Object[0]);
            }
            Ssh2ForwardingChannel tunnel = new Ssh2ForwardingChannel("direct-tcpip", 1024000, 34000, hostname, port, listeningAddress, listeningPort, originatingHost, originatingPort, sock);
            request.writeString(hostname);
            request.writeInt(port);
            request.writeString(originatingHost);
            request.writeInt(originatingPort);
            tunnel.addChannelEventListener(listener);
            this.openChannel(tunnel, request.toByteArray());
            Ssh2ForwardingChannel ssh2ForwardingChannel = tunnel;
            return ssh2ForwardingChannel;
        }
        catch (IOException ex) {
            throw new SshException(ex, 5);
        }
        finally {
            try {
                request.close();
            }
            catch (IOException iOException) {}
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int requestRemoteForwarding(String bindAddress, int bindPort, String hostToConnect, int portToConnect, ForwardingRequestListener listener) throws SshException {
        ByteArrayWriter baw = new ByteArrayWriter();
        try {
            if (listener == null) {
                throw new SshException("You must specify a listener to receive connection requests", 4);
            }
            if (log.isDebugEnabled()) {
                this.transport.debug(log, "Requesting remote forwarding from " + bindAddress + ":" + bindPort + " to " + hostToConnect + ":" + portToConnect, new Object[0]);
            }
            baw.writeString(bindAddress);
            baw.writeInt(bindPort);
            GlobalRequest request = new GlobalRequest("tcpip-forward", baw.toByteArray());
            if (this.sendGlobalRequest(request, true)) {
                if (bindPort == 0) {
                    try (ByteArrayReader bar = new ByteArrayReader(request.getData());){
                        bindPort = (int)bar.readInt();
                    }
                }
                this.forwardingListeners.put(bindAddress + ":" + String.valueOf(bindPort), listener);
                this.forwardingDestinations.put(bindAddress + ":" + String.valueOf(bindPort), hostToConnect + ":" + String.valueOf(portToConnect));
                int n = bindPort;
                return n;
            }
            int n = 0;
            return n;
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

    @Override
    public boolean cancelRemoteForwarding(String bindAddress, int bindPort) throws SshException {
        ByteArrayWriter baw = new ByteArrayWriter();
        try {
            if (log.isDebugEnabled()) {
                this.transport.debug(log, "Cancelling remote forwarding from " + bindAddress + ":" + bindPort, new Object[0]);
            }
            baw.writeString(bindAddress);
            baw.writeInt(bindPort);
            GlobalRequest request = new GlobalRequest("cancel-tcpip-forward", baw.toByteArray());
            if (this.sendGlobalRequest(request, true)) {
                this.forwardingListeners.remove(bindAddress + ":" + String.valueOf(bindPort));
                this.forwardingDestinations.remove(bindAddress + ":" + String.valueOf(bindPort));
                boolean bl = true;
                return bl;
            }
            boolean bl = false;
            return bl;
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

    public void openChannel(Ssh2Channel channel, byte[] requestdata) throws SshException, ChannelOpenException {
        this.verifyConnection(true);
        this.connection.openChannel(channel, requestdata);
    }

    public void openChannel(SshAbstractChannel channel) throws SshException, ChannelOpenException {
        this.verifyConnection(true);
        if (!(channel instanceof Ssh2Channel)) {
            throw new SshException("The channel is not an SSH2 channel!", 4);
        }
        this.connection.openChannel((Ssh2Channel)channel, null);
    }

    public void addChannelFactory(ChannelFactory factory) throws SshException {
        this.connection.addChannelFactory(factory);
    }

    @Override
    public SshContext getContext() {
        return this.transport.transportContext;
    }

    public void addRequestHandler(GlobalRequestHandler handler) throws SshException {
        String requests = "";
        for (int i = 0; i < handler.supportedRequests().length; ++i) {
            requests = requests + handler.supportedRequests()[i] + " ";
        }
        if (log.isDebugEnabled()) {
            this.transport.debug(log, "Installing global request handler for " + requests.trim(), new Object[0]);
        }
        this.connection.addRequestHandler(handler);
    }

    public boolean sendGlobalRequest(GlobalRequest request, boolean wantreply) throws SshException {
        this.verifyConnection(true);
        return this.connection.sendGlobalRequest(request, wantreply, 60000L);
    }

    public boolean sendGlobalRequest(GlobalRequest request, boolean wantreply, long timeout) throws SshException {
        this.verifyConnection(true);
        return this.connection.sendGlobalRequest(request, wantreply, timeout);
    }

    @Override
    public String getRemoteIdentification() {
        return this.remoteIdentification;
    }

    void verifyConnection(boolean requireAuthentication) throws SshException {
        if (this.authentication == null || this.transport == null || this.connection == null) {
            throw new SshException("Not connected!", 4);
        }
        if (!this.transport.isConnected()) {
            throw new SshException("The connection has been terminated!", 2);
        }
        if (!this.authentication.isAuthenticated() && requireAuthentication) {
            throw new SshException("The connection is not authenticated!", 4);
        }
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
            SshClient duplicate;
            if (log.isDebugEnabled()) {
                this.transport.debug(log, "Duplicating SSH client", new Object[0]);
            }
            if ((duplicate = this.connector.connect(this.io.duplicate(), this.username, this.buffered, this.transport.transportContext)).authenticate(this.auth) != 1) {
                duplicate.disconnect();
                throw new SshException("Duplication attempt failed to authenicate user!", 5);
            }
            return duplicate;
        }
        catch (IOException ex) {
            throw new SshException(ex, 10);
        }
    }

    @Override
    public int getChannelCount() {
        return this.connection.getChannelCount();
    }

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public boolean isBuffered() {
        return this.buffered;
    }

    public void processMessages(long timeout) throws SshException {
        if (this.buffered) {
            throw new SshException("You cannot call processMessages on a buffered SshClient!", 4);
        }
        if (this.connection == null) {
            throw new SshException("You cannot call processMessage before authentication has completed!", 4);
        }
        this.connection.blockForMessage(timeout);
    }

    public String getKeyExchangeInUse() {
        return this.transport.keyExchange == null ? "none" : this.transport.keyExchange.getAlgorithm();
    }

    public SshKeyExchangeClient getKeyExchangeInstanceInUse() {
        return this.transport.keyExchange;
    }

    public String getHostKeyInUse() {
        return this.transport.hostkey == null ? "none" : this.transport.hostkey.getAlgorithm();
    }

    public SshPublicKey getHostKey() {
        return this.transport.hostkey;
    }

    public String getCipherInUseCS() {
        return this.transport.encryption == null ? "none" : this.transport.encryption.getAlgorithm();
    }

    public String getCipherInUseSC() {
        return this.transport.decryption == null ? "none" : this.transport.decryption.getAlgorithm();
    }

    public String getMacInUseCS() {
        return this.transport.outgoingMac == null ? "none" : this.transport.outgoingMac.getAlgorithm();
    }

    public String getMacInUseSC() {
        return this.transport.incomingMac == null ? "none" : this.transport.incomingMac.getAlgorithm();
    }

    public String getCompressionInUseCS() {
        return this.transport.outgoingCompression == null ? "none" : this.transport.outgoingCompression.getAlgorithm();
    }

    public String getCompressionInUseSC() {
        return this.transport.incomingCompression == null ? "none" : this.transport.incomingCompression.getAlgorithm();
    }

    @Override
    public SshTransport getTransport() {
        return this.io;
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

    public String toString() {
        return "SSH2 " + this.io.getHost() + ":" + this.io.getPort() + " [kex=" + (this.transport.keyExchange == null ? "none" : this.transport.keyExchange.getAlgorithm()) + " hostkey=" + (this.transport.hostkey == null ? "none" : this.transport.hostkey.getAlgorithm()) + " client->server=" + (this.transport.encryption == null ? "none" : this.transport.encryption.getAlgorithm()) + "," + (this.transport.outgoingMac == null ? "none" : this.transport.outgoingMac.getAlgorithm()) + "," + (this.transport.outgoingCompression == null ? "none" : this.transport.outgoingCompression.getAlgorithm()) + " server->client=" + (this.transport.decryption == null ? "none" : this.transport.decryption.getAlgorithm()) + "," + (this.transport.incomingMac == null ? "none" : this.transport.incomingMac.getAlgorithm()) + "," + (this.transport.incomingCompression == null ? "none" : this.transport.incomingCompression.getAlgorithm()) + "]";
    }

    public String[] getRemoteKeyExchanges() {
        return this.transport.getRemoteKeyExchanges();
    }

    public String[] getRemotePublicKeys() {
        return this.transport.getRemotePublicKeys();
    }

    public String[] getRemoteCiphersCS() {
        return this.transport.getRemoteCiphersCS();
    }

    public String[] getRemoteCiphersSC() {
        return this.transport.getRemoteCiphersSC();
    }

    public String[] getRemoteMacsCS() {
        return this.transport.getRemoteMacsCS();
    }

    public String[] getRemoteMacsSC() {
        return this.transport.getRemoteMacsSC();
    }

    public String[] getRemoteCompressionsCS() {
        return this.transport.getRemoteCompressionsCS();
    }

    public String[] getRemoteCompressionsSC() {
        return this.transport.getRemoteCompressionsSC();
    }

    public boolean hasBackgroundThread() {
        return this.connection != null && this.connection.hasBackgroundThread();
    }

    public Thread getBackgroundThread() {
        if (this.connection == null) {
            throw new IllegalStateException("Invalid background thread access");
        }
        return this.connection.getBackgroundThread();
    }

    @Override
    public String getIdent() {
        return this.transport.getIdent();
    }

    @Override
    public String getHost() {
        return this.transport.getProvider().getHost();
    }

    class ForwardingRequestChannelFactory
    implements ChannelFactory {
        String[] types = new String[]{"forwarded-tcpip", "x11"};

        ForwardingRequestChannelFactory() {
        }

        @Override
        public String[] supportedChannelTypes() {
            return this.types;
        }

        @Override
        public Ssh2Channel createChannel(String channeltype, byte[] requestdata) throws SshException, ChannelOpenException {
            if (channeltype.equals("forwarded-tcpip")) {
                ByteArrayReader bar = new ByteArrayReader(requestdata);
                try {
                    String address = bar.readString();
                    int port = (int)bar.readInt();
                    String originatorIP = bar.readString();
                    int originatorPort = (int)bar.readInt();
                    String key = address + ":" + String.valueOf(port);
                    if (Ssh2Client.this.forwardingListeners.containsKey(key)) {
                        ForwardingRequestListener listener = Ssh2Client.this.forwardingListeners.get(key);
                        String destination = Ssh2Client.this.forwardingDestinations.get(key);
                        String hostToConnect = destination.substring(0, destination.indexOf(58));
                        int portToConnect = Integer.parseInt(destination.substring(destination.indexOf(58) + 1));
                        if (log.isDebugEnabled()) {
                            Ssh2Client.this.transport.debug(log, "Creating remote forwarding channel from " + address + ":" + port + " to " + hostToConnect + ":" + portToConnect, new Object[0]);
                        }
                        Ssh2ForwardingChannel channel = new Ssh2ForwardingChannel("forwarded-tcpip", 0x200000, 34000, hostToConnect, portToConnect, address, port, originatorIP, originatorPort, listener.createConnection(hostToConnect, portToConnect));
                        listener.initializeTunnel(channel);
                        Ssh2ForwardingChannel ssh2ForwardingChannel = channel;
                        return ssh2ForwardingChannel;
                    }
                    try {
                        throw new ChannelOpenException("Forwarding had not previously been requested", 1);
                    }
                    catch (IOException ex) {
                        throw new ChannelOpenException(ex.getMessage(), 4);
                    }
                    catch (SshException ex) {
                        throw new ChannelOpenException(ex.getMessage(), 2);
                    }
                }
                finally {
                    try {
                        bar.close();
                    }
                    catch (IOException iOException) {}
                }
            }
            if (channeltype.equals("x11")) {
                if (!Ssh2Client.this.isXForwarding) {
                    throw new ChannelOpenException("X Forwarding had not previously been requested", 1);
                }
                ByteArrayReader bar = new ByteArrayReader(requestdata);
                try {
                    int targetPort;
                    String targetAddr;
                    String originatorIP = bar.readString();
                    int originatorPort = (int)bar.readInt();
                    String display = Ssh2Client.this.connection.getContext().getX11Display();
                    int i = display.indexOf(":");
                    int num = 0;
                    int screen = 0;
                    if (i != -1) {
                        targetAddr = display.substring(0, i);
                        if ((i = (display = display.substring(i + 1)).indexOf(46)) > -1) {
                            num = Integer.parseInt(display.substring(0, i));
                            screen = Integer.parseInt(display.substring(i + 1));
                        } else {
                            num = Integer.parseInt(display);
                        }
                        targetPort = num;
                    } else {
                        targetAddr = display;
                        targetPort = 6000;
                    }
                    if (targetPort <= 10) {
                        targetPort += 6000;
                    }
                    if (log.isDebugEnabled()) {
                        Ssh2Client.this.transport.debug(log, "Creating X11 forwarding channel for display " + targetAddr + ":" + screen, new Object[0]);
                    }
                    ForwardingRequestListener listener = Ssh2Client.this.connection.getContext().getX11RequestListener();
                    Ssh2ForwardingChannel channel = new Ssh2ForwardingChannel("x11", 1024000, 34000, targetAddr, targetPort, targetAddr, screen, originatorIP, originatorPort, listener.createConnection(targetAddr, targetPort));
                    listener.initializeTunnel(channel);
                    Ssh2ForwardingChannel ssh2ForwardingChannel = channel;
                    return ssh2ForwardingChannel;
                }
                catch (Throwable ex) {
                    throw new ChannelOpenException(ex.getMessage(), 2);
                }
                finally {
                    try {
                        bar.close();
                    }
                    catch (IOException iOException) {}
                }
            }
            throw new ChannelOpenException(channeltype + " is not supported", 3);
        }
    }

    private static class KBIRequestHandlerWhenUserUsingPasswordAuthentication
    implements KBIRequestHandler {
        private String password;

        public KBIRequestHandlerWhenUserUsingPasswordAuthentication(PasswordAuthentication pwdAuth) {
            this.password = pwdAuth.getPassword();
        }

        @Override
        public boolean showPrompts(String name, String instruction, KBIPrompt[] prompts) {
            for (int i = 0; i < prompts.length; ++i) {
                prompts[i].setResponse(this.password);
            }
            return true;
        }
    }
}

