/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.pool;

import com.maverick.pool.SshClientFactory;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshConnector;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshTransport;
import com.sshtools.net.SocketTransport;
import java.io.IOException;

public class SshClientAdapter
implements SshClientFactory {
    @Override
    public void configureConnector(SshConnector con) throws SshException {
    }

    @Override
    public SshTransport createTransport(String host, int port) throws IOException {
        return new SocketTransport(host, port);
    }

    @Override
    public void authenticateClient(SshClient client) throws SshException {
    }
}

