/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

public class SyncObject {
    private volatile boolean isNotifySent = false;

    public boolean isNotifySent() {
        return this.isNotifySent;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void syncNotify() {
        SyncObject syncObject = this;
        synchronized (syncObject) {
            this.notify();
            this.isNotifySent = true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean syncWait(long timeout) throws InterruptedException {
        boolean isSent = false;
        try {
            SyncObject syncObject = this;
            synchronized (syncObject) {
                if (!this.isNotifySent) {
                    this.wait(timeout);
                }
                isSent = this.isNotifySent;
            }
        }
        finally {
            this.isNotifySent = false;
        }
        return isSent;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long syncWait(long timeout, long currentMilliSeconds) throws InterruptedException {
        long newCurr = currentMilliSeconds;
        try {
            SyncObject syncObject = this;
            synchronized (syncObject) {
                if (!this.isNotifySent) {
                    this.wait(timeout);
                    newCurr = System.currentTimeMillis();
                }
            }
        }
        finally {
            this.isNotifySent = false;
        }
        return newCurr;
    }
}

