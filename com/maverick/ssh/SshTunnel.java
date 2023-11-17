/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.SshChannel;
import com.maverick.ssh.SshTransport;

public interface SshTunnel
extends SshChannel,
SshTransport {
    @Override
    public int getPort();

    public String getListeningAddress();

    public int getListeningPort();

    public String getOriginatingHost();

    public int getOriginatingPort();

    public boolean isLocal();

    public boolean isX11();

    public SshTransport getTransport();

    public boolean isLocalEOF();

    public boolean isRemoteEOF();
}

