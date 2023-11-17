/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshSession;
import com.maverick.ssh.components.SshPublicKey;

public interface SshClientListener {
    public void connected(SshClient var1, String var2);

    public void authenticationStarted(SshClient var1, String[] var2);

    public void authenticated(SshClient var1, String var2);

    public void sessionOpened(SshClient var1, SshSession var2);

    public void sessionClosed(SshClient var1, SshSession var2);

    public void disconnecting(SshClient var1, String var2, int var3);

    public void disconnected(SshClient var1, String var2, int var3);

    public void idle(SshClient var1, long var2);

    public void executingCommand(SshClient var1, SshSession var2, String var3);

    public void executedCommand(SshClient var1, SshSession var2, String var3);

    public void startingShell(SshClient var1, SshSession var2);

    public void startedShell(SshClient var1, SshSession var2);

    public void startingSubsystem(SshClient var1, SshSession var2, String var3);

    public void startedSubsystem(SshClient var1, SshSession var2, String var3);

    public void commandExecuted(SshClient var1, SshSession var2, String var3, int var4);

    public void subsystemClosed(SshClient var1, SshSession var2, String var3);

    public void shellClosed(SshClient var1, SshSession var2);

    public void keyExchangeComplete(SshClient var1, SshPublicKey var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9);
}

