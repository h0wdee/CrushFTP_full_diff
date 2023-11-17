/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components;

import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshRsaPrivateKey;
import java.math.BigInteger;
import java.security.PrivateKey;

public interface SshRsaPrivateCrtKey
extends SshRsaPrivateKey {
    public BigInteger getPublicExponent();

    public BigInteger getPrimeP();

    public BigInteger getPrimeQ();

    public BigInteger getPrimeExponentP();

    public BigInteger getPrimeExponentQ();

    public BigInteger getCrtCoefficient();

    public BigInteger doPrivate(BigInteger var1) throws SshException;

    @Override
    public PrivateKey getJCEPrivateKey();
}

