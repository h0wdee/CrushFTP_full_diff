/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.sftp;

public interface FileTransferProgress {
    public void started(long var1, String var3);

    public boolean isCancelled();

    public void progressed(long var1);

    public void completed();
}

