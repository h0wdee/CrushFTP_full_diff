/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal;

import com.didisoft.pgp.bc.elgamal.interfaces.ElGamalParams;
import java.math.BigInteger;

public class BaseElGamalParams
implements ElGamalParams {
    protected BigInteger p;
    protected BigInteger g;

    public BaseElGamalParams(BigInteger bigInteger, BigInteger bigInteger2) {
        this.p = bigInteger;
        this.g = bigInteger2;
    }

    public BigInteger getP() {
        return this.p;
    }

    public BigInteger getG() {
        return this.g;
    }
}

