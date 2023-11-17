/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshClientListener;
import com.maverick.ssh.SshSession;
import com.maverick.ssh.components.SshPublicKey;

public class SshClientAdapter
implements SshClientListener {
    @Override
    public void connected(SshClient client, String remoteIdentification) {
    }

    @Override
    public void authenticationStarted(SshClient client, String[] methods) {
    }

    @Override
    public void authenticated(SshClient client, String username) {
    }

    @Override
    public void sessionOpened(SshClient client, SshSession channel) {
    }

    @Override
    public void sessionClosed(SshClient client, SshSession channel) {
    }

    @Override
    public void disconnecting(SshClient client, String msg, int reason) {
    }

    @Override
    public void disconnected(SshClient client, String msg, int reason) {
    }

    @Override
    public void idle(SshClient client, long lastActivity) {
    }

    @Override
    public void executingCommand(SshClient client, SshSession session, String cmd) {
    }

    @Override
    public void executedCommand(SshClient client, SshSession session, String cmd) {
    }

    @Override
    public void startingShell(SshClient client, SshSession sSession) {
    }

    @Override
    public void startedShell(SshClient client, SshSession session) {
    }

    @Override
    public void startingSubsystem(SshClient client, SshSession session, String subsystem) {
    }

    @Override
    public void startedSubsystem(SshClient client, SshSession session, String subsystem) {
    }

    @Override
    public void commandExecuted(SshClient client, SshSession session, String cmd, int exitCode) {
    }

    @Override
    public void subsystemClosed(SshClient client, SshSession session, String subsystem) {
    }

    @Override
    public void shellClosed(SshClient client, SshSession session) {
    }

    @Override
    public void keyExchangeComplete(SshClient client, SshPublicKey hostkey, String keyExchange, String cipherCS, String cipherSC, String macCS, String macSC, String compressionCS, String compressionSC) {
    }
}

