/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components;

import java.io.IOException;
import java.security.PrivateKey;

public interface SshPrivateKey {
    public byte[] sign(byte[] var1) throws IOException;

    public byte[] sign(byte[] var1, String var2) throws IOException;

    public String getAlgorithm();

    public PrivateKey getJCEPrivateKey();
}

