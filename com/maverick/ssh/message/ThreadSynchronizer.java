/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh.message;

import com.maverick.ssh.message.MessageHolder;
import com.maverick.ssh.message.MessageObserver;
import com.maverick.ssh.message.MessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadSynchronizer {
    static Logger log = LoggerFactory.getLogger(ThreadSynchronizer.class);
    boolean isBlocking;
    boolean permanentBlock;
    Thread blockingThread = null;
    boolean verbose;
    long timeout;

    public ThreadSynchronizer(boolean permanentBlock, boolean verbose, long timeout) {
        this.permanentBlock = permanentBlock;
        this.verbose = verbose;
        this.timeout = timeout;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean requestBlock(MessageStore store, MessageObserver observer, MessageHolder holder) throws InterruptedException {
        holder.msg = store.hasMessage(observer);
        if (holder.msg != null) {
            return false;
        }
        ThreadSynchronizer threadSynchronizer = this;
        synchronized (threadSynchronizer) {
            boolean canBlock;
            if (this.verbose && log.isDebugEnabled()) {
                log.debug("requesting block");
            }
            boolean bl = canBlock = !this.permanentBlock && (!this.isBlocking || this.isBlockOwner(Thread.currentThread()));
            if (canBlock) {
                this.isBlocking = true;
                this.blockingThread = Thread.currentThread();
            } else {
                if (this.verbose) {
                    if (log.isDebugEnabled()) {
                        log.debug("can't block so wait");
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("isBlocking:" + this.isBlocking);
                    }
                }
                this.wait(this.timeout);
            }
            return canBlock;
        }
    }

    public synchronized boolean isBlockOwner(Thread thread) {
        return this.blockingThread != null && this.blockingThread.equals(thread);
    }

    public synchronized void releaseWaiting() {
        this.notifyAll();
    }

    public synchronized void releaseBlock() {
        this.isBlocking = false;
        this.blockingThread = null;
        this.notifyAll();
    }
}

