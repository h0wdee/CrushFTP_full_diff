/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.protocol.commons.concurrent;

import com.hierynomus.protocol.commons.concurrent.TransformedFuture;
import java.util.concurrent.Future;

public abstract class AFuture<V>
implements Future<V> {
    public <T> AFuture<T> map(Function<V, T> f) {
        return new TransformedFuture<V, T>(this, f);
    }

    public static interface Function<A, B> {
        public B apply(A var1);
    }
}

