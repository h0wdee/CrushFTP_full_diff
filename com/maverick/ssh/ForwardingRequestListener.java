/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.SshException;
import com.maverick.ssh.SshTransport;
import com.maverick.ssh.SshTunnel;

public interface ForwardingRequestListener {
    public SshTransport createConnection(String var1, int var2) throws SshException;

    public void initializeTunnel(SshTunnel var1);
}

