/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components;

import com.maverick.ssh.components.SshDsaPublicKey;
import com.maverick.ssh.components.SshPrivateKey;
import java.io.IOException;
import java.math.BigInteger;
import java.security.interfaces.DSAPrivateKey;

public interface SshDsaPrivateKey
extends SshPrivateKey {
    public BigInteger getX();

    @Override
    public byte[] sign(byte[] var1) throws IOException;

    @Override
    public DSAPrivateKey getJCEPrivateKey();

    public SshDsaPublicKey getPublicKey();
}

