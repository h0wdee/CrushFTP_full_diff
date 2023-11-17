/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.net;

import com.maverick.ssh.SocketTimeoutSupport;
import com.maverick.ssh.SshTransport;
import java.io.IOException;
import java.net.Socket;

public class SocketTransport
extends Socket
implements SshTransport,
SocketTimeoutSupport {
    String hostname;

    public SocketTransport(String hostname, int port) throws IOException {
        super(hostname, port);
        this.hostname = hostname;
    }

    @Override
    public String getHost() {
        return this.hostname;
    }

    @Override
    public SshTransport duplicate() throws IOException {
        return new SocketTransport(this.getHost(), this.getPort());
    }
}

