/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh.message;

import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.SshException;
import com.maverick.ssh.message.Message;
import com.maverick.ssh.message.MessageObserver;
import com.maverick.ssh.message.MessageStore;
import com.maverick.ssh.message.SshAbstractChannel;
import com.maverick.ssh.message.SshMessage;
import com.maverick.ssh.message.SshMessageRouter;
import java.io.EOFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshMessageStore
implements MessageStore {
    static Logger log = LoggerFactory.getLogger(SshMessageStore.class);
    public static final int NO_MESSAGES = -1;
    SshAbstractChannel channel;
    SshMessageRouter manager;
    boolean closed = false;
    SshMessage header = new SshMessage();
    int size = 0;
    MessageObserver stickyMessageObserver;
    boolean verbose;

    public SshMessageStore(SshMessageRouter manager, SshAbstractChannel channel, MessageObserver stickyMessageObserver) {
        this.manager = manager;
        this.channel = channel;
        this.stickyMessageObserver = stickyMessageObserver;
        this.verbose = AdaptiveConfiguration.getBoolean("verbose", false, manager.getReader().getHostname(), manager.getReader().getIdent());
        this.header.next = this.header.previous = this.header;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SshMessage nextMessage(MessageObserver observer, long timeout) throws SshException, EOFException {
        try {
            SshMessage msg = this.manager.nextMessage(this.channel, observer, timeout);
            if (this.verbose && log.isDebugEnabled()) {
                log.debug("got managers next message");
            }
            if (msg != null) {
                SshMessage sshMessage = this.header;
                synchronized (sshMessage) {
                    if (this.stickyMessageObserver.wantsNotification(msg)) {
                        return msg;
                    }
                    this.remove(msg);
                    return msg;
                }
            }
        }
        catch (InterruptedException ex) {
            throw new SshException("The thread was interrupted", 5);
        }
        throw new EOFException("The required message could not be found in the message store");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isClosed() {
        SshMessage sshMessage = this.header;
        synchronized (sshMessage) {
            return this.closed;
        }
    }

    private void remove(SshMessage e) {
        if (e == this.header) {
            throw new IndexOutOfBoundsException();
        }
        e.previous.next = e.next;
        e.next.previous = e.previous;
        --this.size;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Message hasMessage(MessageObserver observer) {
        if (this.verbose && log.isDebugEnabled()) {
            log.debug("waiting for header lock");
        }
        SshMessage sshMessage = this.header;
        synchronized (sshMessage) {
            SshMessage e = this.header.next;
            if (e == null) {
                if (this.verbose && log.isDebugEnabled()) {
                    log.debug("header.next is null");
                }
                return null;
            }
            while (e != this.header) {
                if (observer.wantsNotification(e)) {
                    if (this.verbose && log.isDebugEnabled()) {
                        log.debug("found message");
                    }
                    return e;
                }
                e = e.next;
            }
            if (this.verbose && log.isDebugEnabled()) {
                log.debug("no messages");
            }
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void close() {
        SshMessage sshMessage = this.header;
        synchronized (sshMessage) {
            this.closed = true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void addMessage(SshMessage msg) {
        SshMessage sshMessage = this.header;
        synchronized (sshMessage) {
            msg.next = this.header;
            msg.previous = this.header.previous;
            msg.previous.next = msg;
            msg.next.previous = msg;
            ++this.size;
        }
    }

    public boolean hasBlockingThread() {
        return this.manager.hasBackgroundThread();
    }

    public Thread getBlockingThread() {
        return this.manager.getBackgroundThread();
    }
}

