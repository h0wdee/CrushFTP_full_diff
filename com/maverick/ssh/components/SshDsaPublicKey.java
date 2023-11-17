/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components;

import com.maverick.ssh.components.SshPublicKey;
import java.math.BigInteger;
import java.security.interfaces.DSAPublicKey;

public interface SshDsaPublicKey
extends SshPublicKey {
    public BigInteger getP();

    public BigInteger getQ();

    public BigInteger getG();

    public BigInteger getY();

    @Override
    public DSAPublicKey getJCEPublicKey();
}

