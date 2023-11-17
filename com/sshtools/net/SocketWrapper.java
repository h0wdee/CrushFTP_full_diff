/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.net;

import com.maverick.ssh.SocketTimeoutSupport;
import com.maverick.ssh.SshTransport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketWrapper
implements SshTransport,
SocketTimeoutSupport {
    protected Socket socket;

    public SocketWrapper(Socket socket) {
        this.socket = socket;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.socket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return this.socket.getOutputStream();
    }

    @Override
    public String getHost() {
        return this.socket.getInetAddress() == null ? "proxied" : this.socket.getInetAddress().getHostAddress();
    }

    @Override
    public int getPort() {
        return this.socket.getPort();
    }

    @Override
    public void close() throws IOException {
        this.socket.close();
    }

    @Override
    public SshTransport duplicate() throws IOException {
        return new SocketWrapper(new Socket(this.getHost(), this.socket.getPort()));
    }

    @Override
    public void setSoTimeout(int timeout) throws IOException {
        this.socket.setSoTimeout(timeout);
    }

    @Override
    public int getSoTimeout() throws IOException {
        return this.socket.getSoTimeout();
    }

    public Socket getSocket() {
        return this.socket;
    }
}

