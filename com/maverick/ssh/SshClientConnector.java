/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshContext;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshTransport;

public interface SshClientConnector {
    public SshClient connect(SshTransport var1, String var2) throws SshException;

    public SshClient connect(SshTransport var1, String var2, boolean var3) throws SshException;

    public SshClient connect(SshTransport var1, String var2, SshContext var3) throws SshException;

    public SshClient connect(SshTransport var1, String var2, boolean var3, SshContext var4) throws SshException;
}

