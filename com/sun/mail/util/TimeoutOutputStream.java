/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

class TimeoutOutputStream
extends OutputStream {
    private final OutputStream os;
    private final ScheduledExecutorService ses;
    private final Callable<Object> timeoutTask;
    private final int timeout;
    private byte[] b1;

    public TimeoutOutputStream(OutputStream os0, ScheduledExecutorService ses, int timeout) throws IOException {
        this.os = os0;
        this.ses = ses;
        this.timeout = timeout;
        this.timeoutTask = new Callable<Object>(){

            @Override
            public Object call() throws Exception {
                TimeoutOutputStream.this.os.close();
                return null;
            }
        };
    }

    public synchronized void write(int b) throws IOException {
        if (this.b1 == null) {
            this.b1 = new byte[1];
        }
        this.b1[0] = (byte)b;
        this.write(this.b1);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public synchronized void write(byte[] bs, int off, int len) throws IOException {
        if (off < 0 || off > bs.length || len < 0 || off + len > bs.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        ScheduledFuture<Object> sf = null;
        try {
            try {
                if (this.timeout > 0) {
                    sf = this.ses.schedule(this.timeoutTask, (long)this.timeout, TimeUnit.MILLISECONDS);
                }
            }
            catch (RejectedExecutionException rejectedExecutionException) {
                // empty catch block
            }
            this.os.write(bs, off, len);
            Object var7_6 = null;
            if (sf == null) return;
        }
        catch (Throwable throwable) {
            Object var7_7 = null;
            if (sf == null) throw throwable;
            sf.cancel(true);
            throw throwable;
        }
        sf.cancel(true);
    }

    public void close() throws IOException {
        this.os.close();
    }
}

