/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components;

import com.maverick.ssh.components.SshPrivateKey;
import java.io.IOException;
import java.math.BigInteger;

public interface SshRsaPrivateKey
extends SshPrivateKey {
    public BigInteger getModulus();

    public BigInteger getPrivateExponent();

    @Override
    public byte[] sign(byte[] var1) throws IOException;
}

