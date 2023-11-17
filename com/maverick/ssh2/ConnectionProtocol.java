/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh2;

import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.ChannelOpenException;
import com.maverick.ssh.HostKeyUpdater;
import com.maverick.ssh.SshContext;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.message.Message;
import com.maverick.ssh.message.MessageObserver;
import com.maverick.ssh.message.SshAbstractChannel;
import com.maverick.ssh.message.SshChannelMessage;
import com.maverick.ssh.message.SshMessage;
import com.maverick.ssh.message.SshMessageRouter;
import com.maverick.ssh2.ChannelFactory;
import com.maverick.ssh2.GlobalRequest;
import com.maverick.ssh2.GlobalRequestHandler;
import com.maverick.ssh2.Ssh2Channel;
import com.maverick.ssh2.TransportProtocol;
import com.maverick.ssh2.TransportProtocolListener;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import com.sshtools.publickey.SshPublicKeyFileFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConnectionProtocol
extends SshMessageRouter
implements TransportProtocolListener {
    static Logger log = LoggerFactory.getLogger(ConnectionProtocol.class);
    public static final String SERVICE_NAME = "ssh-connection";
    static final int SSH_MSG_CHANNEL_OPEN = 90;
    static final int SSH_MSG_CHANNEL_OPEN_CONFIRMATION = 91;
    static final int SSH_MSG_CHANNEL_OPEN_FAILURE = 92;
    static final int SSH_MSG_GLOBAL_REQUEST = 80;
    static final int SSH_MSG_REQUEST_SUCCESS = 81;
    static final int SSH_MSG_REQUEST_FAILURE = 82;
    Object channelOpenLock = new Object();
    static final MessageObserver CHANNEL_OPEN_RESPONSE_MESSAGES = new MessageObserver(){

        @Override
        public boolean wantsNotification(Message msg) {
            switch (msg.getMessageId()) {
                case 91: 
                case 92: {
                    return true;
                }
            }
            return false;
        }
    };
    static final MessageObserver GLOBAL_REQUEST_MESSAGES = new MessageObserver(){

        @Override
        public boolean wantsNotification(Message msg) {
            switch (msg.getMessageId()) {
                case 81: 
                case 82: {
                    return true;
                }
            }
            return false;
        }
    };
    TransportProtocol transport;
    Map<String, ChannelFactory> channelfactories = new HashMap<String, ChannelFactory>();
    Map<String, GlobalRequestHandler> requesthandlers = new HashMap<String, GlobalRequestHandler>();

    public ConnectionProtocol(TransportProtocol transport, SshContext context, boolean buffered) {
        super(transport, transport.getInt("channelLimit", context.getChannelLimit()), buffered);
        this.transport = transport;
        this.transport.addListener(this);
        this.addRequestHandler(new HostKeys00GlobalRequest());
    }

    TransportProtocol getTransport() {
        return this.transport;
    }

    public void addChannelFactory(ChannelFactory factory) throws SshException {
        String[] types = factory.supportedChannelTypes();
        for (int i = 0; i < types.length; ++i) {
            if (this.channelfactories.containsKey(types[i])) {
                throw new SshException(types[i] + " channel is already registered!", 4);
            }
            this.channelfactories.put(types[i], factory);
        }
    }

    public void addRequestHandler(GlobalRequestHandler handler) {
        String[] types = handler.supportedRequests();
        for (int i = 0; i < types.length; ++i) {
            if (this.requesthandlers.containsKey(types[i])) {
                throw new IllegalStateException(types[i] + " request is already registered!");
            }
            this.requesthandlers.put(types[i], handler);
        }
    }

    public boolean sendGlobalRequest(GlobalRequest request, boolean wantreply) throws SshException {
        return this.sendGlobalRequest(request, wantreply, 0L);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean sendGlobalRequest(GlobalRequest request, boolean wantreply, long timeout) throws SshException {
        ConnectionProtocol connectionProtocol = this;
        synchronized (connectionProtocol) {
            boolean reply2;
            ByteArrayWriter msg;
            block27: {
                boolean bl;
                block26: {
                    boolean bl2;
                    block25: {
                        if (log.isDebugEnabled()) {
                            this.transport.debug(log, "Sending SSH_MSG_GLOBAL_REQUEST request=" + request.getName() + " wantreply=" + wantreply, new Object[0]);
                        }
                        msg = new ByteArrayWriter();
                        try {
                            msg.write(80);
                            msg.writeString(request.getName());
                            msg.writeBoolean(wantreply);
                            if (request.getData() != null) {
                                msg.write(request.getData());
                            }
                            this.sendMessage(msg.toByteArray(), true);
                            if (wantreply) {
                                SshMessage reply2 = this.getGlobalMessages().nextMessage(GLOBAL_REQUEST_MESSAGES, timeout);
                                if (reply2.getMessageId() == 81) {
                                    if (log.isDebugEnabled()) {
                                        this.transport.debug(log, "Received SSH_MSG_REQUEST_SUCCESS request=" + request.getName(), new Object[0]);
                                    }
                                    if (reply2.available() > 0) {
                                        byte[] tmp = new byte[reply2.available()];
                                        reply2.read(tmp);
                                        request.setData(tmp);
                                    } else {
                                        request.setData(null);
                                    }
                                    bl2 = true;
                                    break block25;
                                }
                                if (log.isDebugEnabled()) {
                                    this.transport.debug(log, "Received SSH_MSG_REQUEST_FAILURE request=" + request.getName(), new Object[0]);
                                }
                                bl = false;
                                break block26;
                            }
                            reply2 = true;
                            break block27;
                        }
                        catch (IOException ex) {
                            throw new SshException(ex, 5);
                        }
                    }
                    return bl2;
                }
                return bl;
            }
            return reply2;
            finally {
                try {
                    msg.close();
                }
                catch (IOException iOException) {}
            }
        }
    }

    public void closeChannel(Ssh2Channel channel) {
        this.freeChannel(channel);
    }

    public SshContext getContext() {
        return this.transport.transportContext;
    }

    public void openChannel(Ssh2Channel channel, byte[] requestdata) throws SshException, ChannelOpenException {
        this.openChannel(channel, requestdata, 0L);
    }

    public void openChannel(Ssh2Channel channel, byte[] requestdata, long timeout) throws SshException, ChannelOpenException {
        ByteArrayWriter msg = new ByteArrayWriter();
        try {
            int channelid = this.allocateChannel(channel);
            if (channelid == -1) {
                if (log.isDebugEnabled()) {
                    this.transport.debug(log, "Maximum number of channels exceeded! active=" + this.getChannelCount() + " channels=" + this.getMaxChannels(), new Object[0]);
                }
                throw new ChannelOpenException("Maximum number of channels exceeded", 4);
            }
            channel.init(this, channelid);
            channel.setClient(this.transport.getClient());
            msg.write(90);
            msg.writeString(channel.getName());
            msg.writeInt(channel.getChannelId());
            msg.writeInt(channel.getWindowSize());
            msg.writeInt(channel.getPacketSize());
            if (requestdata != null) {
                msg.write(requestdata);
            }
            if (log.isDebugEnabled()) {
                this.transport.debug(log, "Sending SSH_MSG_CHANNEL_OPEN type=" + channel.getName() + " id=" + channel.getChannelId() + " window=" + channel.getWindowSize() + " packet=" + channel.getPacketSize(), new Object[0]);
            }
            this.transport.sendMessage(msg.toByteArray(), true);
            SshMessage reply = channel.getMessageStore().nextMessage(CHANNEL_OPEN_RESPONSE_MESSAGES, timeout);
            if (reply.getMessageId() == 92) {
                this.freeChannel(channel);
                int reason = (int)reply.readInt();
                if (log.isDebugEnabled()) {
                    this.transport.debug(log, "Received SSH_MSG_CHANNEL_OPEN_FAILURE id=" + channel.getChannelId() + " reason=" + reason, new Object[0]);
                }
                throw new ChannelOpenException(reply.readString(), reason);
            }
            int remoteid = (int)reply.readInt();
            long remotewindow = reply.readInt();
            int remotepacket = (int)reply.readInt();
            byte[] responsedata = new byte[reply.available()];
            reply.read(responsedata);
            if (log.isDebugEnabled()) {
                this.transport.debug(log, "Received SSH_MSG_CHANNEL_OPEN_CONFIRMATION id=" + channel.getChannelId() + " rid=" + remoteid + " window=" + remotewindow + " packet=" + remotepacket, new Object[0]);
            }
            channel.open(remoteid, remotewindow, remotepacket, responsedata);
            return;
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

    protected void sendMessage(byte[] msg, boolean isActivity) throws SshException {
        this.transport.sendMessage(msg, isActivity);
    }

    @Override
    protected SshMessage createMessage(byte[] msg) throws SshException {
        if (msg[0] >= 91 && msg[0] <= 100) {
            return new SshChannelMessage(msg);
        }
        return new SshMessage(msg);
    }

    @Override
    protected boolean processGlobalMessage(SshMessage message) throws SshException {
        try {
            switch (message.getMessageId()) {
                case 90: {
                    byte[] requestdata;
                    final String type = message.readString();
                    final int remoteid = (int)message.readInt();
                    final int remotewindow = (int)message.readInt();
                    final int remotepacket = (int)message.readInt();
                    byte[] byArray = requestdata = message.available() > 0 ? new byte[message.available()] : null;
                    if (requestdata != null) {
                        message.read(requestdata);
                    }
                    if (log.isDebugEnabled()) {
                        this.transport.debug(log, "Received SSH_MSG_CHANNEL_OPEN type=" + type + " rid=" + remoteid + " window=" + remotewindow + " packet=" + remotepacket, new Object[0]);
                    }
                    this.executeTask(new Runnable(){

                        @Override
                        public void run() {
                            try {
                                ConnectionProtocol.this.processChannelOpenRequest(type, remoteid, remotewindow, remotepacket, requestdata);
                            }
                            catch (SshException ex) {
                                log.error("Failed to process open channel request", (Throwable)ex);
                            }
                        }
                    });
                    return true;
                }
                case 80: {
                    String requestname = message.readString();
                    boolean wantreply = message.read() != 0;
                    byte[] requestdata = new byte[message.available()];
                    message.read(requestdata);
                    if (log.isDebugEnabled()) {
                        this.transport.debug(log, "Received SSH_MSG_GLOBAL_REQUEST request=" + requestname + " wantreply=" + wantreply, new Object[0]);
                    }
                    this.processGlobalRequest(requestname, wantreply, requestdata);
                    return true;
                }
            }
            if (log.isDebugEnabled()) {
                this.transport.debug(log, "Connection protocol does not want to process message " + message.getMessageId(), new Object[0]);
            }
            return false;
        }
        catch (IOException ex) {
            throw new SshException(ex, 5);
        }
    }

    private void executeTask(Runnable runnable) {
        this.transport.getExecutorService().execute(runnable);
    }

    /*
     * Exception decompiling
     */
    void processChannelOpenRequest(String type, int remoteid, int remotewindow, int remotepacket, byte[] requestdata) throws SshException {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [1[TRYBLOCK], 10[CATCHBLOCK]], but top level block is 5[TRYBLOCK]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    void processGlobalRequest(String requestname, boolean wantreply, byte[] requestdata) throws SshException {
        ByteArrayWriter response = new ByteArrayWriter();
        try {
            boolean success = false;
            GlobalRequest request = new GlobalRequest(requestname, requestdata);
            if (this.requesthandlers.containsKey(requestname)) {
                success = this.requesthandlers.get(requestname).processGlobalRequest(request);
            }
            if (wantreply) {
                if (success) {
                    response.write(81);
                    if (request.getData() != null) {
                        response.write(request.getData());
                    }
                    if (log.isDebugEnabled()) {
                        this.transport.debug(log, "Sending SSH_MSG_REQUEST_SUCCESS request=" + requestname, new Object[0]);
                    }
                    this.transport.sendMessage(response.toByteArray(), true);
                } else {
                    this.transport.sendMessage(new byte[]{82}, true);
                }
            }
        }
        catch (IOException ex) {
            throw new SshException(ex, 5);
        }
        finally {
            try {
                response.close();
            }
            catch (IOException iOException) {}
        }
    }

    @Override
    protected void onThreadExit() {
        if (this.transport != null && this.transport.isConnected()) {
            this.transport.disconnect(10, "Exiting");
        }
        this.stop();
    }

    @Override
    public void onDisconnect(String msg, int reason) {
    }

    @Override
    public void onIdle(long lastActivity) {
        SshAbstractChannel[] channels = this.getActiveChannels();
        for (int i = 0; i < channels.length; ++i) {
            channels[i].idle();
        }
    }

    @Override
    public void onReceivedDisconnect(String description, int reason) {
    }

    public void performHostKeysProve00(List<SshPublicKey> keys, HostKeyUpdater updater) throws IOException, SshException {
        block29: {
            ArrayList<SshPublicKey> verifyKeys = new ArrayList<SshPublicKey>();
            for (SshPublicKey key : keys) {
                if (updater.isKnownHost(this.transport.getKnownHostName(), key)) continue;
                verifyKeys.add(key);
            }
            if (!verifyKeys.isEmpty()) {
                try (ByteArrayWriter msg = new ByteArrayWriter();){
                    for (SshPublicKey key : verifyKeys) {
                        msg.writeBinaryString(key.getEncoded());
                    }
                    GlobalRequest request = new GlobalRequest("hostkeys-prove-00@openssh.com", msg.toByteArray());
                    if (!this.sendGlobalRequest(request, true)) break block29;
                    try (ByteArrayReader reader = new ByteArrayReader(request.getData());){
                        for (SshPublicKey key : verifyKeys) {
                            byte[] signature = reader.readBinaryString();
                            if (!this.generateHostKeyProve00Signature(signature, key)) {
                                log.warn("Server provided an invalid signature in response to a hostkeys-prove-00@openssh.com request for key " + key.getAlgorithm());
                                continue;
                            }
                            updater.updateHostKey(this.transport.getKnownHostName(), key);
                        }
                    }
                }
            }
        }
    }

    private boolean generateHostKeyProve00Signature(byte[] signature, SshPublicKey key) throws IOException, SshException {
        try (ByteArrayWriter msg = new ByteArrayWriter();){
            msg.writeString("hostkeys-prove-00@openssh.com");
            msg.writeBinaryString(this.transport.getSessionIdentifier());
            msg.writeBinaryString(key.getEncoded());
            boolean bl = key.verifySignature(signature, msg.toByteArray());
            return bl;
        }
    }

    class ProveHostKeysTask
    implements Runnable {
        List<SshPublicKey> keys;

        ProveHostKeysTask(List<SshPublicKey> keys) {
            this.keys = keys;
        }

        @Override
        public void run() {
            try {
                if (ConnectionProtocol.this.getContext().getHostKeyVerification() instanceof HostKeyUpdater) {
                    ConnectionProtocol.this.performHostKeysProve00(this.keys, (HostKeyUpdater)((Object)ConnectionProtocol.this.getContext().getHostKeyVerification()));
                }
            }
            catch (SshException | IOException e) {
                log.error("Host key update failed", (Throwable)e);
            }
        }
    }

    class HostKeys00GlobalRequest
    implements GlobalRequestHandler {
        HostKeys00GlobalRequest() {
        }

        @Override
        public String[] supportedRequests() {
            return new String[]{"hostkeys-00@openssh.com"};
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        @Override
        public boolean processGlobalRequest(GlobalRequest request) throws SshException {
            boolean allow = AdaptiveConfiguration.getBoolean("allowHostKeyUpdates", ConnectionProtocol.this.transport.transportContext.allowHostKeyUpdates(), ConnectionProtocol.this.transport.getHost(), ConnectionProtocol.this.transport.getIdent());
            if (!allow) return false;
            if (!(ConnectionProtocol.this.getContext().getHostKeyVerification() instanceof HostKeyUpdater)) {
                return false;
            }
            try (ByteArrayReader reader = new ByteArrayReader(request.getData());){
                ArrayList<SshPublicKey> keys = new ArrayList<SshPublicKey>();
                while (reader.available() > 0) {
                    try {
                        keys.add(SshPublicKeyFileFactory.decodeSSH2PublicKey(reader.readBinaryString()));
                    }
                    catch (Throwable e) {
                        log.warn("Failed to parse a server host key provided by hostkeys-00@openssh.com. This may just indicate we do not support the type provided.", e);
                    }
                }
                if (keys.size() > 0) {
                    new Thread(new ProveHostKeysTask(keys)).start();
                }
                boolean bl = true;
                return bl;
            }
            catch (IOException e) {
                return false;
            }
        }
    }
}

