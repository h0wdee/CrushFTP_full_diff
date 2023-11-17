/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal;

import com.didisoft.pgp.bc.elgamal.BaseElGamalPublicKey;
import com.didisoft.pgp.bc.elgamal.interfaces.ElGamalParams;
import com.didisoft.pgp.bc.elgamal.interfaces.ElGamalPrivateKey;
import java.math.BigInteger;

public class BaseElGamalPrivateKey
extends BaseElGamalPublicKey
implements ElGamalPrivateKey {
    protected BigInteger x;

    public BaseElGamalPrivateKey(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, BigInteger bigInteger4) {
        super(bigInteger, bigInteger2, bigInteger4);
        if (bigInteger3 == null) {
            throw new NullPointerException("x == null");
        }
        this.x = bigInteger3;
    }

    public BaseElGamalPrivateKey(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3) {
        this(bigInteger, bigInteger2, bigInteger3, bigInteger2.modPow(bigInteger3, bigInteger));
    }

    protected BaseElGamalPrivateKey(ElGamalParams elGamalParams, BigInteger bigInteger) {
        this(elGamalParams.getP(), elGamalParams.getG(), bigInteger);
    }

    public BigInteger getX() {
        return this.x;
    }
}

