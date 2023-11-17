/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal.interfaces;

import com.didisoft.pgp.bc.elgamal.interfaces.ElGamalParams;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.security.SecureRandom;

public interface ElGamalKeyPairGenerator {
    public void initialize(ElGamalParams var1, SecureRandom var2) throws InvalidParameterException;

    public void initialize(BigInteger var1, BigInteger var2, SecureRandom var3) throws InvalidParameterException;

    public void initialize(int var1, boolean var2, SecureRandom var3) throws InvalidParameterException;

    public ElGamalParams generateParams(int var1, SecureRandom var2) throws InvalidParameterException;

    public void setWithLucasLehmerTest(boolean var1);
}

