/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal;

import com.didisoft.pgp.bc.elgamal.BaseElGamalParams;
import com.didisoft.pgp.bc.elgamal.interfaces.ElGamalParams;
import java.math.BigInteger;
import java.security.InvalidParameterException;

public class GenericElGamalParameterSet {
    private int[] primeLengths;
    private String[][] precomputed;

    protected GenericElGamalParameterSet(int[] nArray, String[][] stringArray) {
        if (stringArray.length != nArray.length) {
            throw new IllegalArgumentException("array lengths do not match");
        }
        this.primeLengths = nArray;
        this.precomputed = stringArray;
    }

    public ElGamalParams getParameters(int n) {
        for (int i = 0; i < this.primeLengths.length; ++i) {
            if (n != this.primeLengths[i]) continue;
            return new BaseElGamalParams(new BigInteger(this.precomputed[i][0], 16), this.precomputed[i][1] != null ? new BigInteger(this.precomputed[i][1], 16) : null);
        }
        return null;
    }

    public void checkSane() throws InvalidParameterException {
        for (int i = 0; i < this.primeLengths.length; ++i) {
            BigInteger bigInteger = new BigInteger(this.precomputed[i][0]);
            if (bigInteger.bitLength() < this.primeLengths[i]) {
                throw new InvalidParameterException(bigInteger + " has incorrect bit length");
            }
            BigInteger bigInteger2 = new BigInteger(this.precomputed[i][1]);
            if (!bigInteger.isProbablePrime(80)) {
                throw new InvalidParameterException(bigInteger + " is not prime");
            }
            if (bigInteger2.compareTo(bigInteger) < 0) continue;
            throw new InvalidParameterException("g >= p");
        }
    }
}

