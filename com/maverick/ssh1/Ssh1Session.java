/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh1;

import com.maverick.ssh.ChannelEventListener;
import com.maverick.ssh.ChannelOpenException;
import com.maverick.ssh.ForwardingRequestListener;
import com.maverick.ssh.PseudoTerminalModes;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshClientListener;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.ssh.SshSession;
import com.maverick.ssh.SshTransport;
import com.maverick.ssh.SshTunnel;
import com.maverick.ssh.message.Message;
import com.maverick.ssh.message.MessageObserver;
import com.maverick.ssh.message.SshAbstractChannel;
import com.maverick.ssh.message.SshChannelMessage;
import com.maverick.ssh.message.SshMessage;
import com.maverick.ssh.message.SshMessageRouter;
import com.maverick.ssh.message.SshMessageStore;
import com.maverick.ssh1.Ssh1Channel;
import com.maverick.ssh1.Ssh1Client;
import com.maverick.ssh1.Ssh1ForwardingChannel;
import com.maverick.ssh1.Ssh1Protocol;
import com.maverick.util.ByteArrayWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Ssh1Session
extends SshMessageRouter
implements SshSession {
    static Logger log = LoggerFactory.getLogger(Ssh1Session.class);
    static final int SSH_CMSG_REQUEST_PTY = 10;
    static final int SSH_CMSG_EXEC_SHELL = 12;
    static final int SSH_CMSG_WINDOW_SIZE = 11;
    static final int SSH_CMSG_EXEC_CMD = 13;
    static final int SSH_CMSG_STDIN_DATA = 16;
    static final int SSH_SMSG_STDOUT_DATA = 17;
    static final int SSH_SMSG_STDERR_DATA = 18;
    static final int SSH_CMSG_EOF = 19;
    static final int SSH_SMSG_EXITSTATUS = 20;
    static final int SSH_MSG_CHANNEL_OPEN_CONFIRMATION = 21;
    static final int SSH_MSG_CHANNEL_OPEN_FAILURE = 22;
    static final int SSH_MSG_CHANNEL_DATA = 23;
    static final int SSH_MSG_CHANNEL_CLOSE = 24;
    static final int SSH_MSG_CHANNEL_CLOSE_CONFIRMATION = 25;
    static final int SSH_SMSG_X11_OPEN = 27;
    static final int SSH_CMSG_PORT_FORWARD_REQUEST = 28;
    static final int SSH_MSG_PORT_OPEN = 29;
    static final int SSH_CMSG_AGENT_REQUEST_FORWARDING = 30;
    static final int SSH_SMSG_AGENT_OPEN = 31;
    static final int SSH_CMSG_EXIT_CONFIRMATION = 33;
    static final int SSH_CMSG_X11_REQUEST_FORWARDING = 34;
    static final String X11_AUTHENTICATION_PROTOCOL = "MIT-MAGIC-COOKIE-1";
    static final MessageObserver CHANNEL_OPEN_MESSAGES = new MessageObserver(){

        @Override
        public boolean wantsNotification(Message msg) {
            switch (msg.getMessageId()) {
                case 21: 
                case 22: {
                    return true;
                }
            }
            return false;
        }
    };
    Ssh1Protocol ssh;
    Ssh1Client client;
    InputStream in = new SessionInputStream(17);
    InputStream err = new SessionInputStream(18);
    OutputStream out = new SessionOutputStream();
    boolean interactive = false;
    int exitcode = Integer.MIN_VALUE;
    boolean closed = false;
    boolean isXForwarding = false;
    Vector<ChannelEventListener> listeners = new Vector();
    boolean autoConsumeInput = false;
    String term = null;
    Map<String, ForwardingRequestListener> forwardingListeners = new HashMap<String, ForwardingRequestListener>();
    ForwardingRequestChannelFactory requestFactory = new ForwardingRequestChannelFactory();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    Ssh1Session(Ssh1Protocol ssh, Ssh1Client client, ChannelEventListener listener, boolean buffered) {
        super(ssh, ssh.context.getChannelLimit(), buffered);
        this.ssh = ssh;
        this.client = client;
        if (listener != null) {
            this.addChannelEventListener(listener);
            Vector<ChannelEventListener> vector = this.listeners;
            synchronized (vector) {
                for (int i = 0; i < this.listeners.size(); ++i) {
                    this.listeners.elementAt(i).channelOpened(this);
                }
            }
        }
        for (SshClientListener l : client.listeners) {
            try {
                l.sessionOpened(client, this);
            }
            catch (Throwable throwable) {}
        }
    }

    @Override
    public void setAutoConsumeInput(boolean autoConsumeInput) {
        this.autoConsumeInput = autoConsumeInput;
    }

    @Override
    protected int allocateChannel(SshAbstractChannel channel) {
        return super.allocateChannel(channel);
    }

    @Override
    public SshClient getClient() {
        return this.client;
    }

    @Override
    public SshMessageRouter getMessageRouter() {
        return this;
    }

    @Override
    protected SshMessage createMessage(byte[] msg) throws SshException {
        if (msg[0] >= 21 && msg[0] <= 25) {
            return new SshChannelMessage(msg);
        }
        return new SshMessage(msg);
    }

    @Override
    public int getChannelId() {
        return -1;
    }

    @Override
    public String getTerm() {
        return this.term;
    }

    @Override
    public void waitForOpen() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addChannelEventListener(ChannelEventListener listener) {
        Vector<ChannelEventListener> vector = this.listeners;
        synchronized (vector) {
            if (listener != null) {
                this.listeners.addElement(listener);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removeChannelEventListener(ChannelEventListener listener) {
        Vector<ChannelEventListener> vector = this.listeners;
        synchronized (vector) {
            if (listener != null) {
                this.listeners.remove(listener);
            }
        }
    }

    @Override
    protected SshMessageStore getGlobalMessages() {
        return super.getGlobalMessages();
    }

    @Override
    public boolean startShell() throws SshException {
        if (this.interactive) {
            throw new SshException("The session is already in interactive mode!", 4);
        }
        for (SshClientListener l : this.client.listeners) {
            try {
                l.startingShell(this.client, this);
            }
            catch (Throwable throwable) {}
        }
        this.ssh.sendMessage(new byte[]{12});
        this.interactive = true;
        this.start();
        for (SshClientListener l : this.client.listeners) {
            try {
                l.startedShell(this.client, this);
            }
            catch (Throwable throwable) {}
        }
        return true;
    }

    @Override
    public boolean executeCommand(String cmd) throws SshException {
        return this.executeCommand(cmd, "UTF-8");
    }

    @Override
    public boolean executeCommand(String cmd, String charset) throws SshException {
        if (this.interactive) {
            throw new SshException("The session is already in interactive mode!", 4);
        }
        for (SshClientListener sshClientListener : this.client.listeners) {
            try {
                sshClientListener.executingCommand(this.client, this, cmd);
            }
            catch (Throwable throwable) {}
        }
        ByteArrayWriter msg = new ByteArrayWriter();
        try {
            msg.write(13);
            msg.writeString(cmd, charset);
            if (log.isDebugEnabled()) {
                log.debug("Sending SSH_CMSG_EXEC_CMD");
            }
            this.ssh.sendMessage(msg.toByteArray());
            this.interactive = true;
            this.start();
            for (SshClientListener l : this.client.listeners) {
                try {
                    l.executedCommand(this.client, this, cmd);
                }
                catch (Throwable throwable) {}
            }
            boolean bl = true;
            return bl;
        }
        catch (IOException iOException) {
            throw new SshException("Ssh1Session.executeCommand caught an IOException: " + iOException.getMessage(), 5);
        }
        finally {
            try {
                msg.close();
            }
            catch (IOException iOException) {}
        }
    }

    @Override
    public boolean isClosed() {
        return this.ssh.state == 3;
    }

    @Override
    public boolean requestPseudoTerminal(String term, int cols, int rows, int width, int height) throws SshException {
        return this.requestPseudoTerminal(term, cols, rows, width, height, new byte[]{0});
    }

    @Override
    public boolean requestPseudoTerminal(String term, int cols, int rows, int width, int height, PseudoTerminalModes terminalModes) throws SshException {
        return this.requestPseudoTerminal(term, cols, rows, width, height, terminalModes.toByteArray());
    }

    @Override
    public boolean requestPseudoTerminal(String term, int cols, int rows, int width, int height, byte[] modes) throws SshException {
        if (this.interactive) {
            throw new SshException("The session is already in interactive mode!", 4);
        }
        ByteArrayWriter msg = new ByteArrayWriter();
        try {
            msg.write(10);
            msg.writeString(term);
            msg.writeInt(rows);
            msg.writeInt(cols);
            msg.writeInt(width);
            msg.writeInt(height);
            msg.write(modes);
            if (log.isDebugEnabled()) {
                log.debug("Sending SSH_CMSG_REQUEST_PTY");
            }
            this.ssh.sendMessage(msg.toByteArray());
            boolean success = this.ssh.hasSucceeded();
            if (success) {
                this.term = term;
            }
            boolean bl = success;
            return bl;
        }
        catch (IOException ex) {
            throw new SshException("Ssh1Client.requestPseudoTerminal() caught an IOException: " + ex.getMessage(), 5);
        }
        finally {
            try {
                msg.close();
            }
            catch (IOException iOException) {}
        }
    }

    @Override
    public void changeTerminalDimensions(int cols, int rows, int width, int height) throws SshException {
        if (!this.interactive) {
            throw new SshException("Dimensions can only be changed whilst in interactive mode", 4);
        }
        ByteArrayWriter msg = new ByteArrayWriter();
        try {
            msg.write(11);
            msg.writeInt(rows);
            msg.writeInt(cols);
            msg.writeInt(height);
            msg.writeInt(width);
            if (log.isDebugEnabled()) {
                log.debug("Sending SSH_CMSG_WINDOW_SIZE");
            }
            this.ssh.sendMessage(msg.toByteArray());
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

    void sendMessage(byte[] msg) throws SshException {
        this.ssh.sendMessage(msg);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected boolean processGlobalMessage(SshMessage msg) throws SshException {
        switch (msg.getMessageId()) {
            case 17: {
                if (this.listeners != null) {
                    for (int i = 0; i < this.listeners.size(); ++i) {
                        this.listeners.elementAt(i).dataReceived(this, msg.array(), msg.getPosition() + 4, msg.available() - 4);
                    }
                }
                return this.autoConsumeInput;
            }
            case 18: {
                if (this.listeners != null) {
                    for (int i2 = 0; i2 < this.listeners.size(); ++i2) {
                        this.listeners.elementAt(i2).extendedDataReceived(this, msg.array(), msg.getPosition() + 4, msg.available() - 4, 1);
                    }
                }
                return this.autoConsumeInput;
            }
            case 20: {
                int i;
                Vector<ChannelEventListener> i2 = this.listeners;
                synchronized (i2) {
                    for (i = 0; i < this.listeners.size(); ++i) {
                        this.listeners.elementAt(i).channelClosing(this);
                    }
                }
                try {
                    this.exitcode = (int)msg.readInt();
                }
                catch (IOException ex) {
                    throw new SshException(5, (Throwable)ex);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Received SSH_SMSG_EXITSTATUS with an exit code of {}", (Object)this.exitcode);
                }
                this.closed = true;
                if (log.isDebugEnabled()) {
                    log.debug("Sending SSH_CMSG_EXIT_CONFIRMATION");
                }
                try {
                    this.ssh.sendMessage(new byte[]{33});
                }
                catch (Throwable ex) {
                }
                finally {
                    this.getGlobalMessages().close();
                    this.ssh.close();
                    Vector<ChannelEventListener> ex = this.listeners;
                    synchronized (ex) {
                        for (i = 0; i < this.listeners.size(); ++i) {
                            this.listeners.elementAt(i).channelClosed(this);
                        }
                    }
                    this.stop();
                }
                return true;
            }
            case 29: {
                int remoteid = 0;
                ByteArrayWriter response = new ByteArrayWriter();
                try {
                    remoteid = (int)msg.readInt();
                    String hostToConnect = msg.readString();
                    int portToConnect = (int)msg.readInt();
                    String originatorString = "";
                    if ((this.ssh.serverProtocolFlags & 2) != 0) {
                        originatorString = msg.readString();
                    }
                    Ssh1Channel tunnel = this.requestFactory.createForwardingChannel(hostToConnect, portToConnect, originatorString);
                    response.write(21);
                    response.writeInt(remoteid);
                    response.writeInt(tunnel.getChannelId());
                    if (log.isDebugEnabled()) {
                        log.debug("Sending SSH_MSG_CHANNEL_OPEN_CONFIRMATION");
                    }
                    this.sendMessage(response.toByteArray());
                    tunnel.open(remoteid);
                }
                catch (Exception ex) {
                    try {
                        response.write(22);
                        response.writeInt(remoteid);
                        if (log.isDebugEnabled()) {
                            log.debug("Sending SSH_MSG_CHANNEL_OPEN_FAILURE");
                        }
                        this.sendMessage(response.toByteArray());
                    }
                    catch (Exception portToConnect) {
                        // empty catch block
                    }
                }
                finally {
                    try {
                        response.close();
                    }
                    catch (IOException ex) {}
                }
                return true;
            }
            case 27: {
                ByteArrayWriter response = new ByteArrayWriter();
                int remoteid = 0;
                try {
                    int screen;
                    remoteid = (int)msg.readInt();
                    String originatorString = "";
                    if ((this.ssh.serverProtocolFlags & 2) != 0) {
                        originatorString = msg.readString();
                    }
                    String display = this.client.getContext().getX11Display();
                    int idx = display.indexOf(58);
                    String hostname = "localhost";
                    if (idx != -1) {
                        hostname = display.substring(0, idx);
                        screen = Integer.parseInt(display.substring(idx + 1));
                    } else {
                        screen = Integer.parseInt(display);
                    }
                    Ssh1Channel tunnel = this.requestFactory.createXForwardingChannel(display, hostname, screen <= 10 ? 6000 + screen : screen, originatorString);
                    response.write(21);
                    response.writeInt(remoteid);
                    response.writeInt(tunnel.getChannelId());
                    if (log.isDebugEnabled()) {
                        log.debug("Sending SSH_MSG_CHANNEL_OPEN_CONFIRMATION");
                    }
                    this.sendMessage(response.toByteArray());
                    tunnel.open(remoteid);
                }
                catch (Exception ex1) {
                    try {
                        response.write(22);
                        response.writeInt(remoteid);
                        if (log.isDebugEnabled()) {
                            log.debug("Sending SSH_MSG_CHANNEL_OPEN_FAILURE");
                        }
                        this.sendMessage(response.toByteArray());
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                finally {
                    try {
                        response.close();
                    }
                    catch (IOException iOException) {}
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int exitCode() {
        return this.exitcode;
    }

    @Override
    public InputStream getInputStream() {
        return this.in;
    }

    @Override
    public InputStream getStderrInputStream() {
        return this.err;
    }

    @Override
    public OutputStream getOutputStream() {
        return this.out;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() {
        int i;
        Object object = this.listeners;
        synchronized (object) {
            for (i = 0; i < this.listeners.size(); ++i) {
                this.listeners.elementAt(i).channelClosing(this);
            }
        }
        try {
            this.out.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        this.getGlobalMessages().close();
        this.signalClosingState();
        this.ssh.disconnect("The user disconnected the application");
        object = this.listeners;
        synchronized (object) {
            for (i = 0; i < this.listeners.size(); ++i) {
                this.listeners.elementAt(i).channelClosed(this);
            }
        }
        for (SshClientListener listener : this.client.listeners) {
            try {
                listener.sessionClosed(this.client, this);
            }
            catch (Throwable throwable) {}
        }
    }

    void openChannel(int messageid, byte[] msg, Ssh1Channel channel) throws SshException, ChannelOpenException {
        this.openChannel(messageid, msg, channel, 0L);
    }

    void openChannel(int messageid, byte[] msg, Ssh1Channel channel, long timeout) throws SshException, ChannelOpenException {
        block13: {
            ByteArrayWriter request = new ByteArrayWriter();
            try {
                if (!this.interactive) {
                    throw new SshException("The session must be in interactive mode! Start the user's shell before attempting this operation", 4);
                }
                int channelid = this.allocateChannel(channel);
                if (channelid == -1) {
                    throw new ChannelOpenException("Maximum number of channels exceeded", 4);
                }
                channel.init(this, channelid);
                request.write(messageid);
                request.writeInt(channelid);
                request.write(msg);
                this.ssh.sendMessage(request.toByteArray());
                SshMessage reply = channel.getMessageStore().nextMessage(CHANNEL_OPEN_MESSAGES, timeout);
                if (reply.getMessageId() == 21) {
                    if (log.isDebugEnabled()) {
                        log.debug("Received SSH_MSG_CHANNEL_OPEN_CONFIRMATION");
                    }
                    channel.open((int)reply.readInt());
                    break block13;
                }
                throw new SshException("The remote computer failed to open a channel", 6);
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
    }

    boolean requestXForwarding(String display, ForwardingRequestListener listener) throws SshException {
        int idx;
        if (!this.client.getContext().getX11Display().equals(display)) {
            this.client.getContext().setX11Display(display);
        }
        int screen = (idx = display.indexOf(58)) != -1 ? Integer.parseInt(display.substring(idx + 1)) : Integer.parseInt(display);
        byte[] x11FakeCookie = this.client.getContext().getX11AuthenticationCookie();
        StringBuffer cookieBuf = new StringBuffer();
        for (int i = 0; i < 16; ++i) {
            String b = Integer.toHexString(x11FakeCookie[i] & 0xFF);
            if (b.length() == 1) {
                b = "0" + b;
            }
            cookieBuf.append(b);
        }
        ByteArrayWriter msg = new ByteArrayWriter();
        try {
            msg.write(34);
            msg.writeString(X11_AUTHENTICATION_PROTOCOL);
            msg.writeString(cookieBuf.toString());
            msg.writeInt(screen);
            if (log.isDebugEnabled()) {
                log.debug("Sending SSH_CMSG_X11_REQUEST_FORWARDING");
            }
            this.ssh.sendMessage(msg.toByteArray());
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
        this.isXForwarding = this.ssh.hasSucceeded();
        if (this.isXForwarding) {
            this.forwardingListeners.put(display, listener);
            return true;
        }
        return false;
    }

    int requestForwarding(int port, String hostToConnect, int portToConnect, ForwardingRequestListener listener) throws SshException {
        String key = hostToConnect + ":" + String.valueOf(portToConnect);
        if (this.forwardingListeners.containsKey(key)) {
            throw new SshException(key + " has already been requested!", 4);
        }
        ByteArrayWriter msg = new ByteArrayWriter();
        try {
            msg.write(28);
            msg.writeInt(port);
            msg.writeString(hostToConnect);
            msg.writeInt(portToConnect);
            if (log.isDebugEnabled()) {
                log.debug("Sending SSH_CMSG_PORT_FORWARD_REQUEST");
            }
            this.ssh.sendMessage(msg.toByteArray());
            if (this.ssh.hasSucceeded()) {
                this.forwardingListeners.put(key, listener);
                int n = port;
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
                msg.close();
            }
            catch (IOException iOException) {}
        }
    }

    public SshTunnel openForwardingChannel(String hostname, int port, String listeningAddress, int listeningPort, String originatingHost, int originatingPort, SshTransport transport, ChannelEventListener listener) throws SshException, ChannelOpenException {
        ByteArrayWriter msg = new ByteArrayWriter();
        try {
            msg.writeString(hostname);
            msg.writeInt(port);
            if ((this.ssh.serverProtocolFlags & 2) != 0) {
                msg.writeString(originatingHost + ":" + String.valueOf(originatingPort));
            }
            Ssh1ForwardingChannel tunnel = new Ssh1ForwardingChannel(this.ssh.context, hostname, port, listeningAddress, listeningPort, originatingHost, originatingPort, 1, transport);
            tunnel.addChannelEventListener(listener);
            if (log.isDebugEnabled()) {
                log.debug("Sending SSH_MSG_PORT_OPEN");
            }
            this.openChannel(29, msg.toByteArray(), tunnel);
            Ssh1ForwardingChannel ssh1ForwardingChannel = tunnel;
            return ssh1ForwardingChannel;
        }
        catch (IOException ex) {
            throw new SshException(ex, 6);
        }
        finally {
            try {
                msg.close();
            }
            catch (IOException iOException) {}
        }
    }

    @Override
    protected void onThreadExit() {
    }

    @Override
    public int getMaximumRemotePacketLength() {
        return 32768;
    }

    @Override
    public int getMaximumLocalPacketLength() {
        return 32768;
    }

    @Override
    public long getRemoteWindow() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getMaximumRemoteWindowSize() {
        return 0L;
    }

    @Override
    public long getMaximumLocalWindowSize() {
        return 0L;
    }

    class ForwardingRequestChannelFactory {
        ForwardingRequestChannelFactory() {
        }

        public Ssh1Channel createXForwardingChannel(String display, String hostToConnect, int portToConnect, String originatorString) throws SshException {
            if (Ssh1Session.this.forwardingListeners.containsKey(display)) {
                int x11Screen;
                String x11Host;
                ForwardingRequestListener listener = Ssh1Session.this.forwardingListeners.get(display);
                int idx = display.indexOf(":");
                if (idx > -1) {
                    x11Host = display.substring(0, idx);
                    x11Screen = Integer.parseInt(display.substring(idx + 1));
                } else {
                    x11Host = "";
                    x11Screen = Integer.parseInt(display.substring(idx + 1));
                }
                Ssh1ForwardingChannel tunnel = new Ssh1ForwardingChannel(Ssh1Session.this.ssh.context, hostToConnect, portToConnect, x11Host, x11Screen, originatorString, -1, 3, listener.createConnection(hostToConnect, portToConnect));
                int localid = Ssh1Session.this.allocateChannel(tunnel);
                tunnel.init(Ssh1Session.this, localid);
                listener.initializeTunnel(tunnel);
                return tunnel;
            }
            throw new SshException("Forwarding had not previously been requested", 6);
        }

        public Ssh1Channel createForwardingChannel(String hostToConnect, int portToConnect, String originatorString) throws SshException {
            String key = hostToConnect + ":" + String.valueOf(portToConnect);
            if (Ssh1Session.this.forwardingListeners.containsKey(key)) {
                ForwardingRequestListener listener = Ssh1Session.this.forwardingListeners.get(key);
                Ssh1ForwardingChannel tunnel = new Ssh1ForwardingChannel(Ssh1Session.this.ssh.context, hostToConnect, portToConnect, "127.0.0.1", portToConnect, originatorString, -1, 2, listener.createConnection(hostToConnect, portToConnect));
                int localid = Ssh1Session.this.allocateChannel(tunnel);
                tunnel.init(Ssh1Session.this, localid);
                listener.initializeTunnel(tunnel);
                return tunnel;
            }
            throw new SshException("Forwarding had not previously been requested", 6);
        }
    }

    class SessionOutputStream
    extends OutputStream {
        SessionOutputStream() {
        }

        @Override
        public void write(int c) throws IOException {
            this.write(new byte[]{(byte)c}, 0, 1);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void write(byte[] buf, int off, int len) throws IOException {
            if (Ssh1Session.this.ssh.getState() == 3) {
                throw new SshIOException(new SshException("The session is closed!", 6));
            }
            try (ByteArrayWriter msg = new ByteArrayWriter(len + 5);){
                msg.write(16);
                msg.writeBinaryString(buf, off, len);
                try {
                    Ssh1Session.this.ssh.sendMessage(msg.toByteArray());
                }
                catch (SshException ex) {
                    throw new EOFException();
                }
                if (Ssh1Session.this.listeners != null) {
                    for (int i = 0; i < Ssh1Session.this.listeners.size(); ++i) {
                        Ssh1Session.this.listeners.elementAt(i).dataSent(Ssh1Session.this, buf, off, len);
                    }
                }
            }
        }

        @Override
        public void close() throws IOException {
            try {
                Ssh1Session.this.ssh.sendMessage(new byte[]{19});
            }
            catch (SshException sshException) {
                // empty catch block
            }
        }
    }

    class SessionInputStream
    extends InputStream {
        int type;
        SshMessage msg;
        int pos;
        MessageObserver messagefilter;

        SessionInputStream(int type) {
            this.type = type;
            this.messagefilter = new MessageObserver(){

                @Override
                public boolean wantsNotification(Message msg) {
                    return msg.getMessageId() == SessionInputStream.this.type;
                }
            };
        }

        @Override
        public int available() throws IOException {
            try {
                if ((this.msg == null || this.msg.available() == 0) && Ssh1Session.this.getGlobalMessages().hasMessage(this.messagefilter) != null) {
                    this.processMessages();
                }
                return this.msg == null ? 0 : this.msg.available();
            }
            catch (EOFException ex) {
                return -1;
            }
            catch (SshException ex) {
                throw new SshIOException(ex);
            }
        }

        void processMessages() throws SshException, EOFException {
            this.msg = Ssh1Session.this.getGlobalMessages().nextMessage(this.messagefilter, 0L);
            this.msg.skip(4L);
        }

        @Override
        public int read() throws IOException {
            try {
                if (this.msg == null || this.msg.available() == 0) {
                    this.processMessages();
                }
                return this.msg.read();
            }
            catch (EOFException ex) {
                return -1;
            }
            catch (SshException ex) {
                throw new SshIOException(ex);
            }
        }

        @Override
        public int read(byte[] buf, int offset, int len) throws IOException {
            try {
                if (this.msg == null || this.msg.available() == 0) {
                    this.processMessages();
                }
                return this.msg.read(buf, offset, len);
            }
            catch (SshException ex) {
                throw new SshIOException(ex);
            }
            catch (EOFException ex) {
                return -1;
            }
        }
    }
}

