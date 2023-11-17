/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh1;

import com.maverick.ssh.ChannelEventListener;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.ssh.message.Message;
import com.maverick.ssh.message.MessageObserver;
import com.maverick.ssh.message.SshAbstractChannel;
import com.maverick.ssh.message.SshChannelMessage;
import com.maverick.ssh.message.SshMessage;
import com.maverick.ssh.message.SshMessageStore;
import com.maverick.ssh1.Ssh1Session;
import com.maverick.util.ByteArrayWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Ssh1Channel
extends SshAbstractChannel {
    static Logger log = LoggerFactory.getLogger(Ssh1Channel.class);
    Ssh1Session session;
    int remoteid;
    ChannelInputStream in;
    ChannelOutputStream out;
    static final int SSH_MSG_CHANNEL_DATA = 23;
    static final int SSH_MSG_CHANNEL_CLOSE = 24;
    static final int SSH_MSG_CHANNEL_CLOSE_CONFIRMATION = 25;
    boolean closed = false;
    boolean outputEOF = false;
    boolean autoConsumeInput = false;
    Vector<ChannelEventListener> listeners = new Vector();
    MessageObserver stickyMessages = new MessageObserver(){

        @Override
        public boolean wantsNotification(Message msg) {
            switch (msg.getMessageId()) {
                case 24: 
                case 25: {
                    return true;
                }
            }
            return false;
        }
    };

    Ssh1Channel() {
    }

    @Override
    public SshClient getClient() {
        return this.session.getClient();
    }

    void init(Ssh1Session session, int channelid) {
        this.session = session;
        super.init(session, channelid);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void open(int remoteid) {
        this.remoteid = remoteid;
        this.in = new ChannelInputStream(23);
        this.out = new ChannelOutputStream();
        Vector<ChannelEventListener> vector = this.listeners;
        synchronized (vector) {
            for (int i = 0; i < this.listeners.size(); ++i) {
                this.listeners.elementAt(i).channelOpened(this);
            }
        }
    }

    @Override
    public void setAutoConsumeInput(boolean autoConsumeInput) {
        this.autoConsumeInput = autoConsumeInput;
    }

    @Override
    protected SshMessageStore getMessageStore() throws SshException {
        return super.getMessageStore();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected boolean processChannelMessage(SshChannelMessage m) throws SshException {
        switch (m.getMessageId()) {
            case 23: {
                if (this.listeners != null) {
                    for (int i = 0; i < this.listeners.size(); ++i) {
                        this.listeners.elementAt(i).dataReceived(this, m.array(), m.getPosition() + 4, m.available() - 4);
                    }
                }
                return this.autoConsumeInput;
            }
            case 24: {
                if (!this.outputEOF) {
                    ByteArrayWriter cmsg = new ByteArrayWriter();
                    try {
                        cmsg.write(25);
                        cmsg.writeInt(this.remoteid);
                        if (log.isDebugEnabled()) {
                            log.debug("Sending SSH_MSG_CHANNEL_CLOSE_CONFIRMATION");
                        }
                        this.session.sendMessage(cmsg.toByteArray());
                        this.closed = true;
                        this.outputEOF = true;
                        Vector<ChannelEventListener> vector = this.listeners;
                        synchronized (vector) {
                            for (int i = 0; i < this.listeners.size(); ++i) {
                                this.listeners.elementAt(i).channelClosed(this);
                            }
                        }
                    }
                    catch (IOException ex) {
                        throw new SshException(5, (Throwable)ex);
                    }
                    finally {
                        try {
                            cmsg.close();
                        }
                        catch (IOException iOException) {}
                    }
                }
                return false;
            }
            case 25: {
                this.closed = true;
                Vector<ChannelEventListener> vector = this.listeners;
                synchronized (vector) {
                    for (int i = 0; i < this.listeners.size(); ++i) {
                        this.listeners.elementAt(i).channelClosed(this);
                    }
                }
                return false;
            }
        }
        return false;
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
    public InputStream getInputStream() {
        return this.in;
    }

    @Override
    public OutputStream getOutputStream() {
        return this.out;
    }

    @Override
    public void close() {
        try {
            this.out.close();
        }
        catch (IOException ex) {
            this.session.close();
        }
    }

    @Override
    protected MessageObserver getStickyMessageIds() {
        return this.stickyMessages;
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
        return this.getRemoteWindow();
    }

    @Override
    public long getMaximumLocalWindowSize() {
        return 0L;
    }

    class ChannelOutputStream
    extends OutputStream {
        ChannelOutputStream() {
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
            if (Ssh1Channel.this.session.isClosed()) {
                throw new SshIOException(new SshException("The session is closed", 6));
            }
            try (ByteArrayWriter msg = new ByteArrayWriter(len + 9);){
                msg.write(23);
                msg.writeInt(Ssh1Channel.this.remoteid);
                msg.writeBinaryString(buf, off, len);
                if (log.isDebugEnabled()) {
                    log.debug("Sending SSH_MSG_CHANNEL_DATA");
                }
                try {
                    Ssh1Channel.this.session.sendMessage(msg.toByteArray());
                }
                catch (SshException ex) {
                    throw new SshIOException(ex);
                }
                if (Ssh1Channel.this.listeners != null) {
                    for (int i = 0; i < Ssh1Channel.this.listeners.size(); ++i) {
                        Ssh1Channel.this.listeners.elementAt(i).dataSent(Ssh1Channel.this, buf, off, len);
                    }
                }
            }
        }

        @Override
        public void close() throws IOException {
            if (!Ssh1Channel.this.closed) {
                try (ByteArrayWriter msg = new ByteArrayWriter();){
                    msg.write(24);
                    msg.writeInt(Ssh1Channel.this.remoteid);
                    if (log.isDebugEnabled()) {
                        log.debug("Sending SSH_MSG_CHANNEL_CLOSE");
                    }
                    try {
                        Ssh1Channel.this.session.sendMessage(msg.toByteArray());
                    }
                    catch (SshException ex) {
                        throw new EOFException();
                    }
                    Ssh1Channel.this.outputEOF = true;
                }
            }
        }
    }

    class ChannelInputStream
    extends InputStream {
        int type;
        SshMessage msg;
        int pos;
        MessageObserver messagefilter;

        ChannelInputStream(int type) {
            this.type = type;
            this.messagefilter = new MessageObserver(){

                @Override
                public boolean wantsNotification(Message msg) {
                    switch (msg.getMessageId()) {
                        case 24: 
                        case 25: {
                            return true;
                        }
                    }
                    return ChannelInputStream.this.type == msg.getMessageId();
                }
            };
        }

        @Override
        public int available() throws IOException {
            try {
                if (this.msg == null || this.msg.available() == 0) {
                    if (Ssh1Channel.this.getMessageStore().hasMessage(this.messagefilter) != null) {
                        this.maybeBlock();
                    } else {
                        return 0;
                    }
                }
                return this.msg.available() > 0 ? this.msg.available() : (Ssh1Channel.this.closed ? -1 : 0);
            }
            catch (SshException ex) {
                throw new SshIOException(ex);
            }
        }

        @Override
        public int read() throws IOException {
            try {
                if (Ssh1Channel.this.closed && this.available() <= 0) {
                    return -1;
                }
                this.maybeBlock();
                return this.msg.read();
            }
            catch (EOFException ex) {
                return -1;
            }
        }

        void maybeBlock() throws IOException {
            try {
                if (this.msg == null || this.msg.available() == 0) {
                    SshMessage m = Ssh1Channel.this.getMessageStore().nextMessage(this.messagefilter, 0L);
                    switch (m.getMessageId()) {
                        case 23: {
                            if (log.isDebugEnabled()) {
                                log.debug("Received SSH_MSG_CHANNEL_DATA");
                            }
                            m.skip(4L);
                            this.msg = m;
                            break;
                        }
                        case 24: {
                            if (log.isDebugEnabled()) {
                                log.debug("Received SSH_MSG_CHANNEL_CLOSE");
                            }
                            throw new EOFException("The channel has been closed");
                        }
                        case 25: {
                            if (log.isDebugEnabled()) {
                                log.debug("Received SSH_MSG_CHANNEL_CLOSE_CONFIRMATION");
                            }
                            throw new EOFException("The channel has been closed");
                        }
                    }
                }
            }
            catch (SshException ex1) {
                throw new SshIOException(ex1);
            }
        }

        @Override
        public int read(byte[] buf, int offset, int len) throws IOException {
            try {
                if (Ssh1Channel.this.closed && this.available() <= 0) {
                    return -1;
                }
                this.maybeBlock();
                return this.msg.read(buf, offset, len);
            }
            catch (EOFException ex) {
                return -1;
            }
        }
    }
}

