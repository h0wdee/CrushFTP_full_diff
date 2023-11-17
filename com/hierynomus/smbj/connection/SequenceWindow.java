/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.connection;

import com.hierynomus.smbj.common.SMBRuntimeException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

class SequenceWindow {
    static final int PREFERRED_MINIMUM_CREDITS = 512;
    private AtomicLong lowestAvailable = new AtomicLong(0L);
    private Semaphore available = new Semaphore(1);
    private static final long MAX_WAIT = 5000L;

    SequenceWindow() {
    }

    long get() {
        return this.get(1)[0];
    }

    long[] get(int credits) {
        try {
            if (this.available.tryAcquire(credits, 5000L, TimeUnit.MILLISECONDS)) {
                long lowest = this.lowestAvailable.getAndAdd(credits);
                return this.range(lowest, lowest + (long)credits);
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SMBRuntimeException("Got interrupted waiting for " + credits + " to be available. Credits available at this moment: " + this.available.availablePermits());
        }
        throw new SMBRuntimeException("Not enough credits (" + this.available.availablePermits() + " available) to hand out " + credits + " sequence numbers");
    }

    void disableCredits() {
        this.available = new NoopSemaphore();
    }

    int available() {
        return this.available.availablePermits();
    }

    void creditsGranted(int credits) {
        this.available.release(credits);
    }

    private long[] range(long start, long stop) {
        int l = (int)(stop - start);
        long[] result = new long[l];
        for (int i = 0; i < l; ++i) {
            result[i] = start + (long)i;
        }
        return result;
    }

    private static class NoopSemaphore
    extends Semaphore {
        public NoopSemaphore() {
            super(1);
        }

        @Override
        public boolean tryAcquire() {
            return true;
        }

        @Override
        public boolean tryAcquire(long timeout, TimeUnit unit) {
            return true;
        }

        @Override
        public boolean tryAcquire(int permits) {
            return true;
        }

        @Override
        public boolean tryAcquire(int permits, long timeout, TimeUnit unit) {
            return true;
        }

        @Override
        public void release(int permits) {
        }

        @Override
        public int availablePermits() {
            return Integer.MAX_VALUE;
        }
    }
}

