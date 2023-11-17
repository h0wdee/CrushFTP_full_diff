/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal.interfaces;

import com.didisoft.pgp.bc.elgamal.interfaces.ElGamalKey;
import java.math.BigInteger;
import java.security.PrivateKey;

public interface ElGamalPrivateKey
extends ElGamalKey,
PrivateKey {
    public BigInteger getX();
}

