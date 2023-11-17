/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.message;

import com.maverick.ssh.SshException;
import java.util.concurrent.ExecutorService;

public interface SshMessageReader {
    public ExecutorService getExecutorService();

    public byte[] nextMessage(long var1) throws SshException;

    public boolean isConnected();

    public String getHostname();

    public String getIdent();

    public String getUuid();
}

