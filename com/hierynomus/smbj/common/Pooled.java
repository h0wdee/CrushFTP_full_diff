/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.common;

import java.util.concurrent.atomic.AtomicInteger;

public class Pooled<A extends Pooled<A>> {
    private final AtomicInteger leases = new AtomicInteger(1);

    public A lease() {
        if (this.leases.getAndIncrement() > 0) {
            return (A)this;
        }
        return null;
    }

    public boolean release() {
        return this.leases.decrementAndGet() <= 0;
    }
}

