/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class ObjectPool<T> {
    private ConcurrentLinkedQueue<T> pool;
    private ScheduledExecutorService executorService;

    public ObjectPool(int minIdle) {
        this.initialize(minIdle);
    }

    public ObjectPool(int minIdle, int maxIdle, long validationInterval) {
        this.initialize(minIdle);
        this.executorService = Executors.newScheduledThreadPool(1);
        this.executorService.scheduleWithFixedDelay(new CheckPoolConditions(minIdle, maxIdle), validationInterval, validationInterval, TimeUnit.SECONDS);
    }

    public T borrowObject() {
        T object = this.pool.poll();
        if (object == null) {
            object = this.createObject();
        }
        return object;
    }

    public void returnObject(T object) {
        if (object == null) {
            return;
        }
        this.pool.offer(object);
    }

    public void shutdown() {
        if (this.executorService != null) {
            this.executorService.shutdown();
        }
    }

    protected abstract T createObject();

    private void initialize(int minIdle) {
        this.pool = new ConcurrentLinkedQueue();
        for (int i = 0; i < minIdle; ++i) {
            this.pool.add(this.createObject());
        }
    }

    private class CheckPoolConditions
    implements Runnable {
        int minIdle;
        int maxIdle;

        CheckPoolConditions(int minIdle, int maxIdle) {
            this.minIdle = minIdle;
            this.maxIdle = maxIdle;
        }

        public void run() {
            block3: {
                int size;
                block2: {
                    size = ObjectPool.this.pool.size();
                    if (size >= this.minIdle) break block2;
                    int sizeToBeAdded = this.minIdle - size;
                    for (int i = 0; i < sizeToBeAdded; ++i) {
                        ObjectPool.this.pool.add(ObjectPool.this.createObject());
                    }
                    break block3;
                }
                if (size <= this.maxIdle) break block3;
                int sizeToBeRemoved = size - this.maxIdle;
                for (int i = 0; i < sizeToBeRemoved; ++i) {
                    ObjectPool.this.pool.poll();
                }
            }
        }
    }
}

