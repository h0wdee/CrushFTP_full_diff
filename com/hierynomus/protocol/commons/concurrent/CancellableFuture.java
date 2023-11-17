/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.protocol.commons.concurrent;

import com.hierynomus.protocol.commons.concurrent.AFuture;
import com.hierynomus.smbj.common.SMBRuntimeException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CancellableFuture<V>
extends AFuture<V> {
    private final AFuture<V> wrappedFuture;
    private final CancelCallback callback;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public CancellableFuture(AFuture<V> wrappedFuture, CancelCallback cc) {
        this.wrappedFuture = wrappedFuture;
        this.callback = cc;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        this.lock.writeLock().lock();
        try {
            if (this.isDone() || this.cancelled.getAndSet(true)) {
                boolean bl = false;
                return bl;
            }
            this.callback.cancel();
            boolean bl = true;
            return bl;
        }
        catch (Throwable t) {
            this.cancelled.set(false);
            throw SMBRuntimeException.Wrapper.wrap(t);
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public boolean isCancelled() {
        this.lock.readLock().lock();
        try {
            boolean bl = this.cancelled.get();
            return bl;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public boolean isDone() {
        this.lock.readLock().lock();
        try {
            boolean bl = this.cancelled.get() || this.wrappedFuture.isDone();
            return bl;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return this.wrappedFuture.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.wrappedFuture.get(timeout, unit);
    }

    public static interface CancelCallback {
        public void cancel();
    }
}

