/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.PseudoTerminalModes;
import com.maverick.ssh.SshChannel;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface SshSession
extends SshChannel {
    public static final int EXITCODE_NOT_RECEIVED = Integer.MIN_VALUE;

    public boolean startShell() throws SshException;

    public String getTerm();

    @Override
    public SshClient getClient();

    public boolean executeCommand(String var1) throws SshException;

    public boolean executeCommand(String var1, String var2) throws SshException;

    public boolean requestPseudoTerminal(String var1, int var2, int var3, int var4, int var5, byte[] var6) throws SshException;

    public boolean requestPseudoTerminal(String var1, int var2, int var3, int var4, int var5, PseudoTerminalModes var6) throws SshException;

    public boolean requestPseudoTerminal(String var1, int var2, int var3, int var4, int var5) throws SshException;

    @Override
    public InputStream getInputStream() throws SshIOException;

    @Override
    public OutputStream getOutputStream() throws SshIOException;

    public InputStream getStderrInputStream() throws SshIOException;

    @Override
    public void close();

    public int exitCode();

    public void changeTerminalDimensions(int var1, int var2, int var3, int var4) throws SshException;

    @Override
    public boolean isClosed();
}

