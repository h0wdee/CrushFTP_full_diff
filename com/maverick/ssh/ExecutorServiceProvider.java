/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import java.util.concurrent.ExecutorService;

public interface ExecutorServiceProvider {
    public ExecutorService getExecutorService();

    public void setExecutorService(ExecutorService var1);
}

