/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.ShellTimeoutException;
import com.maverick.ssh.SshException;

public interface ShellReader {
    public String readLine() throws SshException, ShellTimeoutException;

    public String readLine(long var1) throws SshException, ShellTimeoutException;
}

