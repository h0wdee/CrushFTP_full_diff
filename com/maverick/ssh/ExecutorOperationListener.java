/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

public interface ExecutorOperationListener {
    public void addedTask(Runnable var1);

    public void completedTask(Runnable var1);
}

