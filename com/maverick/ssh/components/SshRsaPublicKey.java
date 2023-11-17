/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components;

import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshPublicKey;
import java.math.BigInteger;
import java.security.PublicKey;

public interface SshRsaPublicKey
extends SshPublicKey {
    public BigInteger getModulus();

    public BigInteger getPublicExponent();

    public int getVersion();

    @Override
    public PublicKey getJCEPublicKey();

    public BigInteger doPublic(BigInteger var1) throws SshException;
}

