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
import com.maverick.ssh.SshIOException;
import com.maverick.ssh.message.Message;
import com.maverick.ssh.message.MessageHolder;
import com.maverick.ssh.message.MessageObserver;
import com.maverick.ssh.message.SshAbstractChannel;
import com.maverick.ssh.message.SshChannelMessage;
import com.maverick.ssh.message.SshMessage;
import com.maverick.ssh.message.SshMessageReader;
import com.maverick.ssh.message.SshMessageStore;
import com.maverick.ssh.message.ThreadSynchronizer;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SshMessageRouter {
    static Logger log = LoggerFactory.getLogger(SshMessageRouter.class);
    private SshAbstractChannel[] channels;
    SshMessageReader reader;
    SshMessageStore global;
    ThreadSynchronizer sync;
    private int count = 0;
    boolean buffered;
    MessagePump messagePump;
    boolean isClosing = false;
    Vector<SshAbstractChannel> activeChannels = new Vector();
    Vector<Runnable> shutdownHooks = new Vector();
    boolean verbose;

    public SshMessageRouter(SshMessageReader reader, int maxChannels, boolean buffered) {
        this.reader = reader;
        this.buffered = buffered;
        this.channels = new SshAbstractChannel[maxChannels];
        this.verbose = AdaptiveConfiguration.getBoolean("verbose", false, reader.getHostname(), reader.getIdent());
        this.global = new SshMessageStore(this, null, new MessageObserver(){

            @Override
            public boolean wantsNotification(Message msg) {
                return false;
            }
        });
        this.sync = new ThreadSynchronizer(buffered, this.verbose, AdaptiveConfiguration.getLong("pseudoBlockTimeout", 1000L, reader.getHostname(), reader.getIdent()));
        if (buffered) {
            this.messagePump = new MessagePump();
        }
    }

    public SshMessageReader getReader() {
        return this.reader;
    }

    public Thread getBackgroundThread() {
        if (this.messagePump == null) {
            throw new IllegalStateException("Invalid background thread access");
        }
        return this.messagePump.getThread();
    }

    public boolean hasBackgroundThread() {
        return this.messagePump != null && this.messagePump.getThread() != null;
    }

    public void start() {
        if (this.verbose && log.isDebugEnabled()) {
            log.debug("{} - starting message pump", (Object)this.reader.getUuid());
        }
        if (this.messagePump != null && !this.messagePump.isRunning()) {
            this.reader.getExecutorService().execute(this.messagePump);
        }
    }

    public void addShutdownHook(Runnable r) {
        if (r != null) {
            this.shutdownHooks.addElement(r);
        }
    }

    public boolean isBuffered() {
        return this.buffered;
    }

    public void stop() {
        this.signalClosingState();
        if (this.messagePump != null) {
            this.messagePump.stopThread();
        }
        if (this.shutdownHooks != null) {
            for (int i = 0; i < this.shutdownHooks.size(); ++i) {
                try {
                    this.shutdownHooks.elementAt(i).run();
                    continue;
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void signalClosingState() {
        if (this.buffered && this.messagePump != null) {
            MessagePump messagePump = this.messagePump;
            synchronized (messagePump) {
                this.isClosing = true;
            }
        }
    }

    protected SshMessageStore getGlobalMessages() {
        return this.global;
    }

    public int getMaxChannels() {
        return this.channels.length;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected int allocateChannel(SshAbstractChannel channel) {
        SshAbstractChannel[] sshAbstractChannelArray = this.channels;
        synchronized (this.channels) {
            for (int i = 0; i < this.channels.length; ++i) {
                if (this.channels[i] != null) continue;
                this.channels[i] = channel;
                this.activeChannels.addElement(channel);
                ++this.count;
                if (log.isDebugEnabled()) {
                    log.debug("{} - Allocated channel " + i, (Object)this.reader.getUuid());
                }
                // ** MonitorExit[var2_2] (shouldn't be in output)
                return i;
            }
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return -1;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void freeChannel(SshAbstractChannel channel) {
        SshAbstractChannel[] sshAbstractChannelArray = this.channels;
        synchronized (this.channels) {
            if (this.channels[channel.getChannelId()] != null && channel.equals(this.channels[channel.getChannelId()])) {
                this.channels[channel.getChannelId()] = null;
                this.activeChannels.removeElement(channel);
                --this.count;
                if (log.isDebugEnabled()) {
                    log.debug("{} - Freed channel {}", (Object)this.reader.getUuid(), (Object)channel.getChannelId());
                }
            }
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return;
        }
    }

    protected SshAbstractChannel[] getActiveChannels() {
        return this.activeChannels.toArray(new SshAbstractChannel[0]);
    }

    protected int maximumChannels() {
        return this.channels.length;
    }

    public int getChannelCount() {
        return this.count;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected SshMessage nextMessage(SshAbstractChannel channel, MessageObserver observer, long timeout) throws SshException, InterruptedException {
        SshMessageStore store;
        long startTime = System.currentTimeMillis();
        SshMessageStore sshMessageStore = store = channel == null ? this.global : channel.getMessageStore();
        if (this.verbose && log.isDebugEnabled()) {
            log.debug("{} - using " + (channel == null ? "global store" : "channel store"), (Object)this.reader.getUuid());
        }
        MessageHolder holder = new MessageHolder();
        while (holder.msg == null && (timeout == 0L || System.currentTimeMillis() - startTime < timeout)) {
            if (this.buffered && this.messagePump != null) {
                if (this.verbose && log.isDebugEnabled()) {
                    log.debug("{} - waiting for messagePump lock", (Object)this.reader.getUuid());
                }
                MessagePump messagePump = this.messagePump;
                synchronized (messagePump) {
                    if (this.messagePump.lastError != null) {
                        Throwable tmpEx = this.messagePump.lastError;
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("%s - %s", this.reader.getUuid(), tmpEx.getMessage()), tmpEx);
                        }
                        if (tmpEx instanceof SshException) {
                            throw (SshException)tmpEx;
                        }
                        if (tmpEx instanceof SshIOException) {
                            throw ((SshIOException)tmpEx).getRealException();
                        }
                        throw new SshException(tmpEx);
                    }
                }
            }
            if (this.isClosing) {
                throw new SshException("Connection has been disconnected", 2);
            }
            if (!this.sync.requestBlock(store, observer, holder)) continue;
            try {
                if (this.verbose && log.isDebugEnabled()) {
                    log.debug("{} - block for message", (Object)this.reader.getUuid());
                }
                this.blockForMessage(timeout);
            }
            finally {
                this.sync.releaseBlock();
            }
        }
        if (holder.msg == null) {
            if (log.isDebugEnabled()) {
                log.debug("{} - Mesage timeout reached timeout={}", (Object)this.reader.getUuid(), (Object)timeout);
            }
            throw new SshException("The message was not received before the specified timeout period timeout=" + timeout, 21);
        }
        return (SshMessage)holder.msg;
    }

    public boolean isBlockingThread(Thread thread) {
        return this.sync.isBlockOwner(thread);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    public void blockForMessage(long timeout) throws SshException {
        SshMessage message = this.createMessage(this.reader.nextMessage(timeout));
        if (this.verbose && log.isDebugEnabled()) {
            log.debug("{} - read next message", (Object)this.reader.getUuid());
        }
        SshAbstractChannel destination = null;
        if (message instanceof SshChannelMessage) {
            SshAbstractChannel[] sshAbstractChannelArray = this.channels;
            // MONITORENTER : this.channels
            destination = this.channels[((SshChannelMessage)message).getChannelId()];
            if (destination == null) {
                log.error("{} - Received channel message with id " + message.getMessageId() + " for channel id " + ((SshChannelMessage)message).getChannelId() + " but no channel with that id currently exists!", (Object)this.reader.getUuid());
                throw new SshException("Received message for channel id " + ((SshChannelMessage)message).getChannelId() + " but no channel with that id exists!", 3);
            }
            // MONITOREXIT : sshAbstractChannelArray
        }
        boolean processed = destination == null ? this.processGlobalMessage(message) : destination.processChannelMessage((SshChannelMessage)message);
        if (processed) return;
        SshMessageStore ms = destination == null ? this.global : destination.getMessageStore();
        ms.addMessage(message);
    }

    protected abstract void onThreadExit();

    protected abstract SshMessage createMessage(byte[] var1) throws SshException;

    protected abstract boolean processGlobalMessage(SshMessage var1) throws SshException;

    class MessagePump
    implements Runnable {
        Throwable lastError;
        boolean running = false;
        Thread currentThread;

        MessagePump() {
        }

        public Thread getThread() {
            return this.currentThread;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void run() {
            SshMessageRouter.this.sync.blockingThread = this.currentThread = Thread.currentThread();
            try {
                this.running = true;
                while (this.running) {
                    try {
                        SshMessageRouter.this.blockForMessage(0L);
                        SshMessageRouter.this.sync.releaseWaiting();
                    }
                    catch (Throwable t) {
                        MessagePump messagePump = this;
                        synchronized (messagePump) {
                            if (!SshMessageRouter.this.isClosing) {
                                if (log.isDebugEnabled()) {
                                    log.debug("{} - Message pump caught exception", (Object)SshMessageRouter.this.reader.getUuid(), (Object)t);
                                }
                                this.lastError = t;
                            }
                            this.stopThread();
                        }
                    }
                }
                SshMessageRouter.this.sync.releaseBlock();
            }
            finally {
                SshMessageRouter.this.onThreadExit();
            }
        }

        public void stopThread() {
            this.running = false;
            if (!Thread.currentThread().equals(this.currentThread)) {
                this.currentThread.interrupt();
            }
        }

        public boolean isRunning() {
            return this.running;
        }
    }
}

