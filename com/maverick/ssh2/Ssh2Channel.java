/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh2;

import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.ChannelEventListener;
import com.maverick.ssh.SocketTimeoutSupport;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.ssh.message.Message;
import com.maverick.ssh.message.MessageObserver;
import com.maverick.ssh.message.SshAbstractChannel;
import com.maverick.ssh.message.SshChannelMessage;
import com.maverick.ssh.message.SshMessageStore;
import com.maverick.ssh2.ConnectionProtocol;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ssh2Channel
extends SshAbstractChannel {
    static Logger log = LoggerFactory.getLogger(Ssh2Channel.class);
    public static final String SESSION_CHANNEL = "session";
    ConnectionProtocol connection;
    int remoteid;
    String name;
    Vector<ChannelEventListener> listeners = new Vector();
    static final int SSH_MSG_CHANNEL_CLOSE = 97;
    static final int SSH_MSG_CHANNEL_EOF = 96;
    static final int SSH_MSG_CHANNEL_REQUEST = 98;
    static final int SSH_MSG_CHANNEL_SUCCESS = 99;
    static final int SSH_MSG_CHANNEL_FAILURE = 100;
    static final int SSH_MSG_WINDOW_ADJUST = 93;
    static final int SSH_MSG_CHANNEL_DATA = 94;
    static final int SSH_MSG_CHANNEL_EXTENDED_DATA = 95;
    boolean autoConsumeInput = false;
    boolean sendKeepAliveOnIdle = false;
    boolean isRemoteEOF = false;
    boolean isLocalEOF = false;
    SshClient client;
    boolean windowAdjustTest = false;
    final MessageObserver WINDOW_ADJUST_MESSAGES = new MessageObserver(){

        @Override
        public boolean wantsNotification(Message msg) {
            switch (msg.getMessageId()) {
                case 93: 
                case 97: {
                    return true;
                }
            }
            return false;
        }
    };
    final MessageObserver CHANNEL_DATA_MESSAGES = new MessageObserver(){

        @Override
        public boolean wantsNotification(Message msg) {
            switch (msg.getMessageId()) {
                case 94: 
                case 96: 
                case 97: {
                    return true;
                }
            }
            return false;
        }
    };
    final MessageObserver EXTENDED_DATA_MESSAGES = new MessageObserver(){

        @Override
        public boolean wantsNotification(Message msg) {
            switch (msg.getMessageId()) {
                case 95: 
                case 96: 
                case 97: {
                    return true;
                }
            }
            return false;
        }
    };
    final MessageObserver CHANNEL_REQUEST_MESSAGES = new MessageObserver(){

        @Override
        public boolean wantsNotification(Message msg) {
            switch (msg.getMessageId()) {
                case 97: 
                case 99: 
                case 100: {
                    return true;
                }
            }
            return false;
        }
    };
    final MessageObserver CHANNEL_CLOSE_MESSAGES = new MessageObserver(){

        @Override
        public boolean wantsNotification(Message msg) {
            switch (msg.getMessageId()) {
                case 97: {
                    return true;
                }
            }
            return false;
        }
    };
    static final MessageObserver STICKY_MESSAGES = new MessageObserver(){

        @Override
        public boolean wantsNotification(Message msg) {
            switch (msg.getMessageId()) {
                case 96: 
                case 97: {
                    return true;
                }
            }
            return false;
        }
    };
    ChannelInputStream in;
    ChannelOutputStream out;
    DataWindow localwindow;
    DataWindow remotewindow;
    boolean closing = false;
    boolean free = false;
    private int remotepacket;
    private boolean disableChannelDataLogs;
    boolean isBlocking = false;

    public Ssh2Channel(String name, int windowsize, int packetsize) {
        this.name = name;
        this.localwindow = new DataWindow(windowsize, packetsize);
        this.in = new ChannelInputStream(this.CHANNEL_DATA_MESSAGES);
        this.out = new ChannelOutputStream();
    }

    @Override
    public SshClient getClient() {
        return this.client;
    }

    public ConnectionProtocol getConnection() {
        return this.connection;
    }

    void setClient(SshClient client) {
        this.client = client;
        this.windowAdjustTest = AdaptiveConfiguration.getBoolean("windowAdjustTest", false, client.getHost(), client.getIdent());
        this.disableChannelDataLogs = AdaptiveConfiguration.getBoolean("disableChannelDataLogs", false, client.getHost(), client.getIdent());
    }

    @Override
    protected MessageObserver getStickyMessageIds() {
        return STICKY_MESSAGES;
    }

    @Override
    public void setAutoConsumeInput(boolean autoConsumeInput) {
        this.autoConsumeInput = autoConsumeInput;
    }

    long getWindowSize() {
        return this.localwindow.available();
    }

    int getPacketSize() {
        return this.localwindow.getPacketSize();
    }

    @Override
    protected SshMessageStore getMessageStore() throws SshException {
        return super.getMessageStore();
    }

    public String getName() {
        return this.name;
    }

    @Override
    public InputStream getInputStream() {
        return this.in;
    }

    @Override
    public OutputStream getOutputStream() {
        return this.out;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addChannelEventListener(ChannelEventListener listener) {
        Vector<ChannelEventListener> vector = this.listeners;
        synchronized (vector) {
            if (listener != null) {
                this.listeners.add(listener);
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

    public boolean isSendKeepAliveOnIdle() {
        return this.sendKeepAliveOnIdle;
    }

    public void setSendKeepAliveOnIdle(boolean sendKeepAliveOnIdle) {
        this.sendKeepAliveOnIdle = sendKeepAliveOnIdle;
    }

    @Override
    public void idle() {
        if (this.sendKeepAliveOnIdle) {
            try {
                this.sendRequest("keep-alive@sshtools.com", false, null, false);
            }
            catch (SshException sshException) {
                // empty catch block
            }
        }
    }

    void init(ConnectionProtocol connection, int channelid) {
        this.connection = connection;
        super.init(connection, channelid);
    }

    protected byte[] create() {
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void open(int remoteid, long remotewindow, int remotepacket) throws IOException {
        this.remoteid = remoteid;
        this.remotewindow = new DataWindow(remotewindow, remotepacket);
        this.remotepacket = remotepacket;
        this.state = 2;
        Vector<ChannelEventListener> vector = this.listeners;
        synchronized (vector) {
            Enumeration<ChannelEventListener> e = this.listeners.elements();
            while (e.hasMoreElements()) {
                e.nextElement().channelOpened(this);
            }
        }
    }

    @Override
    public int getMaximumRemotePacketLength() {
        return this.remotepacket;
    }

    @Override
    public int getMaximumLocalPacketLength() {
        return this.getPacketSize();
    }

    @Override
    public long getMaximumLocalWindowSize() {
        return this.localwindow.initialSize;
    }

    @Override
    public long getMaximumRemoteWindowSize() {
        return this.remotewindow.initialSize;
    }

    protected void open(int remoteid, long remotewindow, int remotepacket, byte[] responsedata) throws IOException {
        this.open(remoteid, remotewindow, remotepacket);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected boolean processChannelMessage(SshChannelMessage msg) throws SshException {
        try {
            switch (msg.getMessageId()) {
                case 98: {
                    String requesttype = msg.readString();
                    boolean wantreply = msg.read() != 0;
                    byte[] requestdata = new byte[msg.available()];
                    msg.read(requestdata);
                    if (log.isDebugEnabled()) {
                        this.connection.getTransport().debug(log, "Received SSH_MSG_CHANNEL_REQUEST id=" + this.channelid + " rid=" + this.remoteid + " request=" + requesttype + " wantreply=" + wantreply, new Object[0]);
                    }
                    this.channelRequest(requesttype, wantreply, requestdata);
                    return true;
                }
                case 93: {
                    msg.mark(4);
                    int len = (int)msg.readInt();
                    if (log.isDebugEnabled()) {
                        this.connection.getTransport().debug(log, "Received SSH_MSG_WINDOW_ADJUST id=" + this.channelid + " rid=" + this.remoteid + " window=" + this.remotewindow.available() + " adjust=" + len, new Object[0]);
                    }
                    msg.reset();
                    return false;
                }
                case 94: {
                    if (log.isDebugEnabled() && !this.disableChannelDataLogs) {
                        this.connection.getTransport().debug(log, "Received SSH_MSG_CHANNEL_DATA id=" + this.channelid + " rid=" + this.remoteid + " len=" + (msg.available() - 4) + " window=" + this.localwindow.available(), new Object[0]);
                    }
                    if (this.autoConsumeInput) {
                        this.localwindow.consume(msg.available() - 4);
                        if (this.localwindow.available() <= this.localwindow.getInitialSize() / 2L) {
                            this.adjustWindow(this.localwindow.getInitialSize() - this.localwindow.available());
                        }
                    }
                    Enumeration<ChannelEventListener> e = this.listeners.elements();
                    while (e.hasMoreElements()) {
                        e.nextElement().dataReceived(this, msg.array(), msg.getPosition() + 4, msg.available() - 4);
                    }
                    return this.autoConsumeInput;
                }
                case 95: {
                    if (log.isDebugEnabled() && !this.disableChannelDataLogs) {
                        this.connection.getTransport().debug(log, "Received SSH_MSG_CHANNEL_EXTENDED_DATA id=" + this.channelid + " rid=" + this.remoteid + " len=" + (msg.available() - 4) + " window=" + this.localwindow.available(), new Object[0]);
                    }
                    int type = (int)ByteArrayReader.readInt(msg.array(), msg.getPosition());
                    if (this.autoConsumeInput) {
                        this.localwindow.consume(msg.available() - 8);
                        if (this.localwindow.available() <= this.localwindow.getInitialSize() / 2L) {
                            this.adjustWindow(this.localwindow.getInitialSize() - this.localwindow.available());
                        }
                    }
                    Enumeration<ChannelEventListener> e = this.listeners.elements();
                    while (e.hasMoreElements()) {
                        e.nextElement().extendedDataReceived(this, msg.array(), msg.getPosition() + 8, msg.available() - 8, type);
                    }
                    return this.autoConsumeInput;
                }
                case 97: {
                    if (log.isDebugEnabled()) {
                        this.connection.getTransport().debug(log, "Received SSH_MSG_CHANNEL_CLOSE id=" + this.channelid + " rid=" + this.remoteid, new Object[0]);
                    }
                    SshMessageStore e = this.ms;
                    synchronized (e) {
                        this.ms.close();
                    }
                    this.checkCloseStatus(true);
                    return false;
                }
                case 96: {
                    if (log.isDebugEnabled()) {
                        this.connection.getTransport().debug(log, "Received SSH_MSG_CHANNEL_EOF id=" + this.channelid + " rid=" + this.remoteid, new Object[0]);
                    }
                    this.isRemoteEOF = true;
                    Enumeration<ChannelEventListener> e = this.listeners.elements();
                    while (e.hasMoreElements()) {
                        e.nextElement().channelEOF(this);
                    }
                    this.channelEOF();
                    if (this.isLocalEOF) {
                        this.close();
                    }
                    return false;
                }
            }
            return false;
        }
        catch (IOException ex) {
            throw new SshException(ex, 5);
        }
    }

    SshChannelMessage processMessages(MessageObserver messagefilter, int timeout) throws SshException, EOFException {
        SshChannelMessage msg = (SshChannelMessage)this.ms.nextMessage(messagefilter, timeout);
        switch (msg.getMessageId()) {
            case 93: {
                try {
                    this.remotewindow.adjust(msg.readInt());
                    if (!log.isDebugEnabled()) break;
                    this.connection.getTransport().debug(log, "Applied window adjust window=" + this.remotewindow.available(), new Object[0]);
                    break;
                }
                catch (IOException ex) {
                    throw new SshException(5, (Throwable)ex);
                }
            }
            case 94: {
                try {
                    int length = (int)msg.readInt();
                    this.processStandardData(length, msg);
                    break;
                }
                catch (IOException e) {
                    throw new SshException(5, (Throwable)e);
                }
            }
            case 95: {
                try {
                    int type = (int)msg.readInt();
                    int length = (int)msg.readInt();
                    this.processExtendedData(type, length, msg);
                    break;
                }
                catch (IOException ex) {
                    throw new SshException(5, (Throwable)ex);
                }
            }
            case 97: {
                this.checkCloseStatus(true);
                throw new EOFException("The channel is closed");
            }
            case 96: {
                throw new EOFException("The channel is EOF");
            }
        }
        return msg;
    }

    protected void processStandardData(int length, SshChannelMessage msg) throws SshException {
        this.in.addMessage(length, msg);
    }

    protected void processExtendedData(int typecode, int length, SshChannelMessage msg) throws SshException {
    }

    protected ChannelInputStream createExtendedDataStream() {
        return new ChannelInputStream(this.EXTENDED_DATA_MESSAGES);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void sendChannelData(byte[] buf, int offset, int len) throws SshException {
        try {
            if (this.state != 2) {
                throw new SshException("The channel is closed", 6);
            }
            if (len > 0) {
                try (ByteArrayWriter msg = new ByteArrayWriter(len + 9);){
                    msg.write(94);
                    msg.writeInt(this.remoteid);
                    msg.writeBinaryString(buf, offset, len);
                    if (log.isDebugEnabled() && !this.disableChannelDataLogs) {
                        this.connection.getTransport().debug(log, "Sending SSH_MSG_CHANNEL_DATA id=" + this.channelid + " rid=" + this.remoteid + " len=" + len + " window=" + this.remotewindow.available(), new Object[0]);
                    }
                    this.connection.sendMessage(msg.toByteArray(), true);
                }
            }
            Enumeration<ChannelEventListener> e = this.listeners.elements();
            while (e.hasMoreElements()) {
                e.nextElement().dataSent(this, buf, offset, len);
            }
        }
        catch (IOException ex) {
            throw new SshException(ex, 5);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void sendExtendedChannelData(byte[] buf, int offset, int len, int type) throws SshException {
        try {
            if (this.state != 2) {
                throw new SshException("The channel is closed", 6);
            }
            if (len > 0) {
                try (ByteArrayWriter msg = new ByteArrayWriter(len + 9);){
                    msg.write(95);
                    msg.writeInt(this.remoteid);
                    msg.writeInt(type);
                    msg.writeBinaryString(buf, offset, len);
                    if (log.isDebugEnabled() && !this.disableChannelDataLogs) {
                        this.connection.getTransport().debug(log, "Sending SSH_MSG_CHANNEL_EXTENDED_DATA id=" + this.channelid + " rid=" + this.remoteid + " type=" + type + " len=" + len + " window=" + this.remotewindow.available(), new Object[0]);
                    }
                    this.connection.sendMessage(msg.toByteArray(), true);
                }
            }
            if (this.listeners != null) {
                for (int i = 0; i < this.listeners.size(); ++i) {
                    this.listeners.elementAt(i).extendedDataReceived(this, buf, offset, len, type);
                }
            }
        }
        catch (IOException ex) {
            throw new SshException(ex, 5);
        }
    }

    private void adjustWindow(long increment) throws SshException {
        ByteArrayWriter msg = new ByteArrayWriter(9);
        try {
            if (this.closing || this.isClosed()) {
                return;
            }
            msg.write(93);
            msg.writeInt(this.remoteid);
            msg.writeInt(increment);
            if (log.isDebugEnabled()) {
                this.connection.getTransport().debug(log, "Sending SSH_MSG_WINDOW_ADJUST id=" + this.channelid + " rid=" + this.remoteid + " window=" + this.localwindow.available() + " adjust=" + increment, new Object[0]);
            }
            this.localwindow.adjust(increment);
            this.connection.sendMessage(msg.toByteArray(), true);
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

    public boolean sendRequest(String requesttype, boolean wantreply, byte[] requestdata) throws SshException {
        return this.sendRequest(requesttype, wantreply, requestdata, true);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean sendRequest(String requesttype, boolean wantreply, byte[] requestdata, boolean isActivity) throws SshException {
        ConnectionProtocol connectionProtocol = this.connection;
        synchronized (connectionProtocol) {
            boolean bl;
            ByteArrayWriter msg = new ByteArrayWriter();
            try {
                msg.write(98);
                msg.writeInt(this.remoteid);
                msg.writeString(requesttype);
                msg.writeBoolean(wantreply);
                if (requestdata != null) {
                    msg.write(requestdata);
                }
                if (log.isDebugEnabled()) {
                    this.connection.getTransport().debug(log, "Sending SSH_MSG_CHANNEL_REQUEST id=" + this.channelid + " rid=" + this.remoteid + " request=" + requesttype + " wantreply=" + wantreply, new Object[0]);
                }
                this.connection.sendMessage(msg.toByteArray(), isActivity);
                boolean result = false;
                if (wantreply) {
                    SshChannelMessage reply = this.processMessages(this.CHANNEL_REQUEST_MESSAGES, AdaptiveConfiguration.getInt("messageTimeout", this.connection.getContext().getMessageTimeout(), this.connection.getTransport().getHost(), this.connection.getTransport().getIdent()));
                    boolean bl2 = result = reply.getMessageId() == 99;
                    if (log.isDebugEnabled()) {
                        this.connection.getTransport().debug(log, "Received " + (result ? "SSH_MSG_CHANNEL_SUCCESS" : "SSH_MSG_CHANNEL_FAILURE") + " id=" + this.channelid + " rid=" + this.remoteid + " request=" + requesttype, new Object[0]);
                    }
                }
                bl = result;
            }
            catch (IOException ex) {
                throw new SshException(ex, 5);
            }
            return bl;
            finally {
                try {
                    msg.close();
                }
                catch (IOException iOException) {}
            }
        }
    }

    @Override
    public long getRemoteWindow() {
        return this.remotewindow.available();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() {
        boolean performClose = false;
        Object object = this;
        synchronized (object) {
            if (!this.closing && this.state == 2) {
                this.closing = true;
                performClose = true;
            }
        }
        if (performClose) {
            object = this.listeners;
            synchronized (object) {
                Enumeration<ChannelEventListener> e = this.listeners.elements();
                while (e.hasMoreElements()) {
                    e.nextElement().channelClosing(this);
                }
            }
            ByteArrayWriter msg = new ByteArrayWriter(5);
            try {
                this.out.close(!this.isLocalEOF);
                this.state = 3;
                msg.write(97);
                msg.writeInt(this.remoteid);
                try {
                    if (log.isDebugEnabled()) {
                        this.connection.getTransport().debug(log, "Sending SSH_MSG_CHANNEL_CLOSE id=" + this.channelid + " rid=" + this.remoteid, new Object[0]);
                    }
                    this.connection.sendMessage(msg.toByteArray(), true);
                }
                catch (SshException ex1) {
                    if (log.isDebugEnabled()) {
                        this.connection.getTransport().debug(log, "Exception attempting to send SSH_MSG_CHANNEL_CLOSE id=" + this.channelid + " rid=" + this.remoteid, ex1);
                    }
                }
            }
            catch (EOFException ex1) {
            }
            catch (SshIOException ex) {
                if (log.isDebugEnabled()) {
                    this.connection.getTransport().debug(log, "SSH Exception during close reason=" + ex.getRealException().getReason() + " id=" + this.channelid + " rid=" + this.remoteid, ex.getRealException());
                }
                this.connection.getTransport().disconnect(10, "IOException during channel close: " + ex.getMessage());
            }
            catch (IOException ex) {
                if (log.isDebugEnabled()) {
                    this.connection.getTransport().debug(log, "Exception during close id=" + this.channelid + " rid=" + this.remoteid, ex);
                }
                this.connection.getTransport().disconnect(10, "IOException during channel close: " + ex.getMessage());
            }
            finally {
                try {
                    msg.close();
                }
                catch (IOException ex) {}
                this.checkCloseStatus(this.ms.isClosed());
            }
        }
    }

    protected void checkCloseStatus(boolean remoteClosed) {
        if (this.state != 3) {
            if (log.isDebugEnabled()) {
                this.connection.getTransport().debug(log, "Local state of channel is not closed id=" + this.channelid + " rid=" + this.remoteid, new Object[0]);
            }
            this.close();
        }
        if (!remoteClosed) {
            if (log.isDebugEnabled()) {
                this.connection.getTransport().debug(log, "Checking remote channel is not already closed id=" + this.channelid + " rid=" + this.remoteid, new Object[0]);
            }
            boolean bl = remoteClosed = this.ms.hasMessage(this.CHANNEL_CLOSE_MESSAGES) != null;
        }
        if (remoteClosed) {
            this.free();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void free() {
        Ssh2Channel ssh2Channel = this;
        synchronized (ssh2Channel) {
            if (!this.free) {
                this.connection.closeChannel(this);
                Vector<ChannelEventListener> vector = this.listeners;
                synchronized (vector) {
                    Enumeration<ChannelEventListener> e = this.listeners.elements();
                    while (e.hasMoreElements()) {
                        e.nextElement().channelClosed(this);
                    }
                }
                this.free = true;
            }
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof Ssh2Channel) {
            return ((Ssh2Channel)obj).getChannelId() == this.channelid;
        }
        return false;
    }

    protected void channelRequest(String requesttype, boolean wantreply, byte[] requestdata) throws SshException {
        if (wantreply) {
            ByteArrayWriter msg = new ByteArrayWriter();
            try {
                msg.write(100);
                msg.writeInt(this.remoteid);
                this.connection.sendMessage(msg.toByteArray(), true);
            }
            catch (IOException e) {
                throw new SshException(e, 5);
            }
            finally {
                try {
                    msg.close();
                }
                catch (IOException iOException) {}
            }
        }
    }

    protected void channelEOF() {
    }

    static class DataWindow {
        long windowsize;
        long initialSize;
        int packetsize;

        DataWindow(long windowsize, int packetsize) {
            this.initialSize = windowsize;
            this.windowsize = windowsize;
            this.packetsize = packetsize;
        }

        int getPacketSize() {
            return this.packetsize;
        }

        long getInitialSize() {
            return this.initialSize;
        }

        void adjust(long count) {
            if (this.initialSize == 0L) {
                this.initialSize = count;
            }
            this.windowsize += count;
        }

        void consume(int count) {
            this.windowsize -= (long)count;
        }

        long available() {
            return this.windowsize;
        }
    }

    class ChannelInputStream
    extends InputStream
    implements SocketTimeoutSupport {
        int unread = 0;
        MessageObserver messagefilter;
        long transfered = 0L;
        SshChannelMessage currentMessage = null;
        int timeout = 0;

        ChannelInputStream(MessageObserver messagefilter) {
            this.messagefilter = messagefilter;
        }

        void addMessage(int length, SshChannelMessage msg) {
            this.unread = length;
            this.currentMessage = msg;
        }

        @Override
        public synchronized int available() throws IOException {
            try {
                if (log.isTraceEnabled()) {
                    log.trace("available() unread=" + this.unread + " id=" + Ssh2Channel.this.getChannelId() + " rid=" + Ssh2Channel.this.remoteid);
                }
                if (this.unread == 0 && Ssh2Channel.this.getMessageStore().hasMessage(this.messagefilter) != null) {
                    Ssh2Channel.this.processMessages(this.messagefilter, 0);
                }
                return this.unread;
            }
            catch (SshException ex) {
                if (log.isDebugEnabled()) {
                    Ssh2Channel.this.connection.getTransport().debug(log, "Throwing SshException from channel inputstream available unread=" + this.unread + " id=" + Ssh2Channel.this.getChannelId() + " rid=" + Ssh2Channel.this.remoteid, new Object[0]);
                }
                throw new SshIOException(ex);
            }
            catch (EOFException e) {
                return 0;
            }
        }

        @Override
        public int read() throws IOException {
            byte[] b = new byte[1];
            int ret = this.read(b, 0, 1);
            if (ret > 0) {
                return b[0] & 0xFF;
            }
            return -1;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public long skip(long len) throws IOException {
            int count;
            block8: {
                if (log.isTraceEnabled()) {
                    log.trace("skip() unread=" + this.unread + " id=" + Ssh2Channel.this.getChannelId() + " rid=" + Ssh2Channel.this.remoteid);
                }
                count = (long)this.unread < len ? this.unread : (int)len;
                try {
                    if (count == 0 && Ssh2Channel.this.isClosed()) {
                        throw new EOFException("The inputstream is closed");
                    }
                    this.currentMessage.skip(count);
                    this.unread -= count;
                    if (log.isTraceEnabled()) {
                        log.trace("Skipping " + len + " bytes of data from channel inputstream unread=" + this.unread + " id=" + Ssh2Channel.this.getChannelId() + " rid=" + Ssh2Channel.this.remoteid);
                    }
                    if ((long)this.unread + Ssh2Channel.this.localwindow.available() >= Ssh2Channel.this.localwindow.getInitialSize() / 2L) break block8;
                    try {
                        Ssh2Channel.this.adjustWindow(Ssh2Channel.this.localwindow.getInitialSize() - Ssh2Channel.this.localwindow.available() - (long)this.unread);
                    }
                    catch (SshException ex) {
                        throw new SshIOException(ex);
                    }
                }
                finally {
                    this.transfered += (long)count;
                }
            }
            return count;
        }

        @Override
        public synchronized int read(byte[] buf, int offset, int len) throws IOException {
            try {
                int count;
                if (log.isTraceEnabled()) {
                    log.trace("read() unread=" + this.unread + " id=" + Ssh2Channel.this.getChannelId() + " rid=" + Ssh2Channel.this.remoteid);
                }
                while (this.unread <= 0) {
                    if (log.isTraceEnabled()) {
                        log.trace("Processing messages unread=" + this.unread + " id=" + Ssh2Channel.this.getChannelId() + " rid=" + Ssh2Channel.this.remoteid);
                    }
                    Ssh2Channel.this.processMessages(this.messagefilter, this.timeout);
                    if (!log.isTraceEnabled()) continue;
                    log.trace("Processed messages unread=" + this.unread + " id=" + Ssh2Channel.this.getChannelId() + " rid=" + Ssh2Channel.this.remoteid);
                }
                int n = count = this.unread < len ? this.unread : len;
                if (count == 0 && Ssh2Channel.this.isClosed()) {
                    if (log.isTraceEnabled()) {
                        log.trace("Returning -1 from channel inputstream unread=" + this.unread + " id=" + Ssh2Channel.this.getChannelId() + " rid=" + Ssh2Channel.this.remoteid);
                    }
                    return -1;
                }
                this.currentMessage.read(buf, offset, count);
                Ssh2Channel.this.localwindow.consume(count);
                this.unread -= count;
                if (log.isTraceEnabled()) {
                    log.trace("Read " + count + " bytes of data from channel inputstream unread=" + this.unread + " id=" + Ssh2Channel.this.getChannelId() + " rid=" + Ssh2Channel.this.remoteid);
                }
                if (Ssh2Channel.this.windowAdjustTest || (long)this.unread + Ssh2Channel.this.localwindow.available() < Ssh2Channel.this.localwindow.getInitialSize() / 2L && !Ssh2Channel.this.isClosed() && !Ssh2Channel.this.closing) {
                    Ssh2Channel.this.adjustWindow(Ssh2Channel.this.localwindow.getInitialSize() - Ssh2Channel.this.localwindow.available() - (long)this.unread);
                }
                this.transfered += (long)count;
                return count;
            }
            catch (SshException ex) {
                if (log.isTraceEnabled()) {
                    log.trace("Caught SshException unread=" + this.unread + " id=" + Ssh2Channel.this.getChannelId() + " rid=" + Ssh2Channel.this.remoteid, (Throwable)ex);
                }
                throw new SshIOException(ex);
            }
            catch (EOFException ex) {
                if (log.isTraceEnabled()) {
                    log.trace("Returning -1 from channel inputstream unread=" + this.unread + " id=" + Ssh2Channel.this.getChannelId() + " rid=" + Ssh2Channel.this.remoteid);
                }
                return -1;
            }
        }

        @Override
        public void setSoTimeout(int timeout) throws IOException {
            this.timeout = timeout;
        }

        @Override
        public int getSoTimeout() throws IOException {
            return this.timeout;
        }
    }

    class ChannelOutputStream
    extends OutputStream {
        private boolean sentEOF;

        ChannelOutputStream() {
        }

        @Override
        public void write(int b) throws IOException {
            this.write(new byte[]{(byte)b}, 0, 1);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void write(byte[] buf, int offset, int len) throws IOException {
            try {
                do {
                    if (Ssh2Channel.this.remotewindow.available() < (long)len && Ssh2Channel.this.getMessageStore().hasMessage(Ssh2Channel.this.WINDOW_ADJUST_MESSAGES) != null) {
                        Ssh2Channel.this.processMessages(Ssh2Channel.this.WINDOW_ADJUST_MESSAGES, 0);
                    }
                    if (Ssh2Channel.this.remotewindow.available() <= 0L) {
                        Ssh2Channel.this.processMessages(Ssh2Channel.this.WINDOW_ADJUST_MESSAGES, 0);
                    }
                    Ssh2Channel ssh2Channel = Ssh2Channel.this;
                    synchronized (ssh2Channel) {
                        long write;
                        if (Ssh2Channel.this.isLocalEOF) {
                            throw new EOFException("The channel is EOF");
                        }
                        if (Ssh2Channel.this.isClosed() || Ssh2Channel.this.closing) {
                            throw new EOFException("The channel is closed");
                        }
                        long l = Ssh2Channel.this.remotewindow.available() < (long)Ssh2Channel.this.remotewindow.getPacketSize() ? (Ssh2Channel.this.remotewindow.available() < (long)len ? Ssh2Channel.this.remotewindow.available() : (long)len) : (write = (long)(Ssh2Channel.this.remotewindow.getPacketSize() < len ? Ssh2Channel.this.remotewindow.getPacketSize() : len));
                        if (write > 0L) {
                            Ssh2Channel.this.sendChannelData(buf, offset, (int)write);
                            Ssh2Channel.this.remotewindow.consume((int)write);
                            len = (int)((long)len - write);
                            offset = (int)((long)offset + write);
                        }
                    }
                } while (len > 0);
            }
            catch (SshException ex) {
                throw new SshIOException(ex);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void close() throws IOException {
            Ssh2Channel ssh2Channel = Ssh2Channel.this;
            synchronized (ssh2Channel) {
                this.close(!Ssh2Channel.this.isClosed() && !Ssh2Channel.this.isLocalEOF && !Ssh2Channel.this.closing);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void close(boolean sendEOF) throws IOException {
            boolean doClose;
            Ssh2Channel ssh2Channel = Ssh2Channel.this;
            synchronized (ssh2Channel) {
                if (sendEOF && !this.sentEOF && !Ssh2Channel.this.closing) {
                    this.sentEOF = true;
                    try (ByteArrayWriter msg = new ByteArrayWriter(5);){
                        msg.write(96);
                        msg.writeInt(Ssh2Channel.this.remoteid);
                        if (log.isDebugEnabled()) {
                            Ssh2Channel.this.connection.getTransport().debug(log, "Sending SSH_MSG_CHANNEL_EOF id=" + Ssh2Channel.this.getChannelId() + " rid=" + Ssh2Channel.this.remoteid, new Object[0]);
                        }
                        Ssh2Channel.this.connection.sendMessage(msg.toByteArray(), true);
                    }
                }
                Ssh2Channel.this.isLocalEOF = true;
                doClose = Ssh2Channel.this.isRemoteEOF;
            }
            if (doClose) {
                Ssh2Channel.this.close();
            }
        }
    }
}

