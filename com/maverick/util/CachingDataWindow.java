/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.util;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachingDataWindow {
    static Logger log = LoggerFactory.getLogger(CachingDataWindow.class);
    ByteBuffer cache;
    boolean blocking = false;
    boolean open = true;
    long timeout = 30000L;

    public CachingDataWindow(int size, boolean blocking) {
        this.blocking = blocking;
        this.cache = ByteBuffer.allocate(size);
        this.cache.flip();
    }

    public synchronized void enableBlocking() {
        this.blocking = true;
    }

    public synchronized void disableBlocking() {
        this.blocking = false;
    }

    public synchronized boolean hasRemaining() {
        return this.cache.hasRemaining();
    }

    public void close() {
        this.open = false;
    }

    public synchronized void put(ByteBuffer data) {
        int remaining;
        this.cache.compact();
        if (this.blocking) {
            long start = System.currentTimeMillis();
            while (this.cache.remaining() < data.remaining()) {
                this.cache.flip();
                try {
                    this.wait(1000L);
                }
                catch (InterruptedException e) {
                    throw new IllegalStateException("Interrupted during cache put wait");
                }
                this.cache.compact();
                if (System.currentTimeMillis() - start <= this.timeout) continue;
                throw new IllegalStateException(String.format("Timeout trying to put %d bytes into cache with %d remaining", data.remaining(), this.cache.remaining()));
            }
        }
        if ((remaining = data.remaining()) > this.cache.remaining()) {
            throw new BufferOverflowException();
        }
        this.cache.put(data);
        this.cache.flip();
        int count = remaining - data.remaining();
        if (log.isTraceEnabled()) {
            log.trace("Written {} bytes from cached data window position={} remaining={} limit={}", new Object[]{count, this.cache.position(), this.cache.remaining(), this.cache.limit()});
        }
        this.notifyAll();
    }

    public synchronized int get(byte[] tmp, int offset, int length) {
        if (this.blocking) {
            while (!this.cache.hasRemaining() && this.open) {
                try {
                    this.wait(1000L);
                }
                catch (InterruptedException interruptedException) {}
            }
        }
        int count = Math.min(length, this.cache.remaining());
        int limit = this.cache.limit();
        this.cache.limit(this.cache.position() + count);
        this.cache.get(tmp, offset, count);
        this.cache.limit(limit);
        if (log.isTraceEnabled()) {
            log.trace("Read {} bytes from cached data window position={} remaining={} limit={}", new Object[]{count, this.cache.position(), this.cache.remaining(), this.cache.limit()});
        }
        this.notifyAll();
        return count;
    }

    public synchronized int get(ByteBuffer buffer) {
        if (this.blocking) {
            while (!this.cache.hasRemaining() && this.open) {
                try {
                    this.wait(0L);
                }
                catch (InterruptedException interruptedException) {}
            }
        }
        int count = Math.min(buffer.remaining(), this.cache.remaining());
        int limit = this.cache.limit();
        this.cache.limit(this.cache.position() + count);
        buffer.put(this.cache);
        this.cache.limit(limit);
        if (log.isTraceEnabled()) {
            log.trace("Read {} bytes from cached data window position={} remaining={} limit={}", new Object[]{count, this.cache.position(), this.cache.remaining(), this.cache.limit()});
        }
        this.notifyAll();
        return count;
    }

    public synchronized int remaining() {
        return this.cache.remaining();
    }

    public synchronized boolean isOpen() {
        return this.open || this.cache.hasRemaining();
    }

    public synchronized void waitFor(long i) throws InterruptedException {
        this.wait(i);
    }
}

