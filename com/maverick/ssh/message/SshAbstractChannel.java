/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh.message;

import com.maverick.ssh.ChannelAdapter;
import com.maverick.ssh.SshChannel;
import com.maverick.ssh.SshException;
import com.maverick.ssh.message.MessageObserver;
import com.maverick.ssh.message.SshChannelMessage;
import com.maverick.ssh.message.SshMessageRouter;
import com.maverick.ssh.message.SshMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SshAbstractChannel
implements SshChannel {
    static Logger log = LoggerFactory.getLogger(SshAbstractChannel.class);
    public static final int CHANNEL_UNINITIALIZED = 1;
    public static final int CHANNEL_OPEN = 2;
    public static final int CHANNEL_CLOSED = 3;
    protected int channelid = -1;
    protected int state = 1;
    protected SshMessageRouter manager;
    protected SshMessageStore ms;

    protected SshMessageStore getMessageStore() throws SshException {
        if (this.ms == null) {
            throw new SshException("Channel is not initialized!", 5);
        }
        return this.ms;
    }

    @Override
    public int getChannelId() {
        return this.channelid;
    }

    @Override
    public SshMessageRouter getMessageRouter() {
        return this.manager;
    }

    protected void init(SshMessageRouter manager, int channelid) {
        this.channelid = channelid;
        this.manager = manager;
        this.ms = new SshMessageStore(manager, this, this.getStickyMessageIds());
    }

    protected abstract MessageObserver getStickyMessageIds();

    @Override
    public boolean isClosed() {
        return this.state == 3;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void waitForOpen() {
        Object lock;
        Object object = lock = new Object();
        synchronized (object) {
            ChannelAdapter listener = new ChannelAdapter(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public void channelOpened(SshChannel channel) {
                    Object object = lock;
                    synchronized (object) {
                        lock.notifyAll();
                    }
                }

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public void channelClosed(SshChannel channel) {
                    Object object = lock;
                    synchronized (object) {
                        lock.notifyAll();
                    }
                }
            };
            this.addChannelEventListener(listener);
            while (this.state == 1) {
                try {
                    lock.wait(1000L);
                }
                catch (InterruptedException interruptedException) {}
            }
            this.removeChannelEventListener(listener);
        }
    }

    protected abstract boolean processChannelMessage(SshChannelMessage var1) throws SshException;

    public void idle() {
    }
}

