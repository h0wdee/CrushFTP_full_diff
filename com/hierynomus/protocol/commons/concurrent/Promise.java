/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.protocol.commons.concurrent;

import com.hierynomus.protocol.commons.concurrent.AFuture;
import com.hierynomus.protocol.commons.concurrent.ExceptionWrapper;
import com.hierynomus.protocol.commons.concurrent.PromiseBackedFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Promise<V, T extends Throwable> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String name;
    private final ExceptionWrapper<T> wrapper;
    private final ReentrantLock lock;
    private final Condition cond;
    private V val;
    private T pendingEx;

    public Promise(String name, ExceptionWrapper<T> wrapper) {
        this(name, wrapper, null);
    }

    public Promise(String name, ExceptionWrapper<T> wrapper, ReentrantLock lock) {
        this.name = name;
        this.wrapper = wrapper;
        this.lock = lock == null ? new ReentrantLock() : lock;
        this.cond = this.lock.newCondition();
    }

    public void deliver(V val) {
        this.lock.lock();
        try {
            this.logger.debug("Setting << {} >> to `{}`", (Object)this.name, (Object)val);
            this.val = val;
            this.cond.signalAll();
        }
        finally {
            this.lock.unlock();
        }
    }

    public void deliverError(Throwable e) {
        this.lock.lock();
        try {
            this.pendingEx = this.wrapper.wrap(e);
            this.cond.signalAll();
        }
        finally {
            this.lock.unlock();
        }
    }

    public void clear() {
        this.lock.lock();
        try {
            this.pendingEx = null;
            this.deliver(null);
        }
        finally {
            this.lock.unlock();
        }
    }

    public V retrieve() throws T {
        return this.tryRetrieve(0L, TimeUnit.SECONDS);
    }

    public V retrieve(long timeout, TimeUnit unit) throws T {
        V value = this.tryRetrieve(timeout, unit);
        if (value == null) {
            throw this.wrapper.wrap(new TimeoutException("Timeout expired"));
        }
        return value;
    }

    public V tryRetrieve(long timeout, TimeUnit unit) throws T {
        this.lock.lock();
        try {
            if (this.pendingEx != null) {
                throw this.pendingEx;
            }
            if (this.val != null) {
                V v = this.val;
                return v;
            }
            this.logger.debug("Awaiting << {} >>", (Object)this.name);
            if (timeout == 0L) {
                while (this.val == null && this.pendingEx == null) {
                    this.cond.await();
                }
            } else if (!this.cond.await(timeout, unit)) {
                V v = null;
                return v;
            }
            if (this.pendingEx != null) {
                this.logger.error("<< {} >> woke to: {}", (Object)this.name, (Object)this.pendingEx);
                throw this.pendingEx;
            }
            V v = this.val;
            return v;
        }
        catch (InterruptedException ie) {
            throw this.wrapper.wrap(ie);
        }
        finally {
            this.lock.unlock();
        }
    }

    public boolean isDelivered() {
        this.lock.lock();
        try {
            boolean bl = this.pendingEx == null && this.val != null;
            return bl;
        }
        finally {
            this.lock.unlock();
        }
    }

    public boolean inError() {
        this.lock.lock();
        try {
            boolean bl = this.pendingEx != null;
            return bl;
        }
        finally {
            this.lock.unlock();
        }
    }

    public boolean isFulfilled() {
        this.lock.lock();
        try {
            boolean bl = this.pendingEx != null || this.val != null;
            return bl;
        }
        finally {
            this.lock.unlock();
        }
    }

    public boolean hasWaiters() {
        this.lock.lock();
        try {
            boolean bl = this.lock.hasWaiters(this.cond);
            return bl;
        }
        finally {
            this.lock.unlock();
        }
    }

    public void lock() {
        this.lock.lock();
    }

    public void unlock() {
        this.lock.unlock();
    }

    public String toString() {
        return this.name;
    }

    public AFuture<V> future() {
        return new PromiseBackedFuture(this);
    }
}

