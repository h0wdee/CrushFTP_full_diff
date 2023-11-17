/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components;

import com.maverick.ssh.SshException;

public interface SshSecureRandomGenerator {
    public void nextBytes(byte[] var1);

    public void nextBytes(byte[] var1, int var2, int var3) throws SshException;

    public int nextInt();
}

