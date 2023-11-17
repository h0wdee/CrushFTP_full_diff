/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.common.NqException;

public interface AsyncConsumer {
    public void complete(Throwable var1, long var2, Object var4) throws NqException;
}

