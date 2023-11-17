/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.Shell;
import com.maverick.ssh.ShellInputStream;
import com.maverick.ssh.SshIOException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ShellProcess {
    Shell shell;
    ShellInputStream in;
    BufferedInputStream bin;

    ShellProcess(Shell shell, ShellInputStream in) {
        this.shell = shell;
        this.in = in;
        this.bin = new BufferedInputStream(in);
    }

    public void mark(int readlimit) {
        this.bin.mark(readlimit);
    }

    public void reset() throws IOException {
        this.bin.reset();
    }

    public InputStream getInputStream() {
        return this.bin;
    }

    public OutputStream getOutputStream() throws SshIOException {
        return this.shell.sessionOut;
    }

    public int getExitCode() {
        return this.in.getExitCode();
    }

    public boolean hasSucceeded() {
        return this.in.hasSucceeded();
    }

    public boolean isActive() {
        return this.in.isActive();
    }

    public String getCommandOutput() {
        return this.in.getCommandOutput();
    }

    public Shell getShell() {
        return this.shell;
    }

    public void drain() throws IOException {
        while (this.in.read() > -1) {
        }
    }
}

