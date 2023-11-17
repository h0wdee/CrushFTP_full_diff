/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal;

import com.didisoft.pgp.bc.elgamal.interfaces.ElGamalParams;
import com.didisoft.pgp.bc.elgamal.interfaces.ElGamalPublicKey;
import java.math.BigInteger;

public class BaseElGamalPublicKey
implements ElGamalPublicKey {
    protected BigInteger p;
    protected BigInteger g;
    protected BigInteger y;

    public BaseElGamalPublicKey(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3) {
        if (bigInteger == null) {
            throw new NullPointerException("p == null");
        }
        if (bigInteger2 == null) {
            throw new NullPointerException("g == null");
        }
        if (bigInteger3 == null) {
            throw new NullPointerException("y == null");
        }
        this.p = bigInteger;
        this.g = bigInteger2;
        this.y = bigInteger3;
    }

    public BaseElGamalPublicKey(ElGamalParams elGamalParams, BigInteger bigInteger) {
        this(elGamalParams.getP(), elGamalParams.getG(), bigInteger);
    }

    public BigInteger getP() {
        return this.p;
    }

    public BigInteger getG() {
        return this.g;
    }

    public BigInteger getY() {
        return this.y;
    }

    public String getAlgorithm() {
        return "ElGamal";
    }

    public String getFormat() {
        return null;
    }

    public byte[] getEncoded() {
        return null;
    }
}

