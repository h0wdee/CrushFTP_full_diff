/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.pool;

import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshConnector;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshTransport;
import java.io.IOException;

public interface SshClientFactory {
    public void configureConnector(SshConnector var1) throws SshException;

    public SshTransport createTransport(String var1, int var2) throws IOException;

    public void authenticateClient(SshClient var1) throws SshException;
}

