/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples;

import com.visuality.nq.client.AsyncConsumer;
import com.visuality.nq.common.NqException;
import com.visuality.nq.samples.UserContext;

class InternalSync
implements AsyncConsumer {
    public long length;
    public Throwable status;
    public boolean isNotifySent = false;

    InternalSync() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void complete(Throwable status, long length, Object context) {
        this.status = status;
        this.length = length;
        if (null != status) {
            NqException nqe = (NqException)status;
            if (-1073741807 == nqe.getErrCode()) {
                System.out.println("Reached end of file.");
            } else {
                System.out.println("Error in complete method = " + Long.toHexString(nqe.getErrCode()));
            }
            if (context instanceof UserContext) {
                UserContext uc = (UserContext)context;
                uc.errCode = nqe.getErrCode();
            }
            InternalSync internalSync = this;
            synchronized (internalSync) {
                this.notify();
                this.isNotifySent = true;
            }
            return;
        }
        if (context instanceof UserContext) {
            UserContext uc = (UserContext)context;
            if (!uc.started) {
                uc.started = true;
                System.out.println("\nStarted processing the " + uc.activity + "...");
            }
            uc.numberOfBytes += length;
            if (-1L != uc.expectedNumberOfBytesToWrite && uc.numberOfBytes >= uc.expectedNumberOfBytesToWrite) {
                InternalSync internalSync = this;
                synchronized (internalSync) {
                    this.notify();
                    this.isNotifySent = true;
                }
            }
        }
    }
}

