/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal.util;

import com.didisoft.pgp.bc.elgamal.util.BigInteger;
import com.didisoft.pgp.bc.elgamal.util.MutableBigInteger;

class SignedMutableBigInteger
extends MutableBigInteger {
    int sign = 1;

    SignedMutableBigInteger() {
    }

    SignedMutableBigInteger(int n) {
        super(n);
    }

    SignedMutableBigInteger(MutableBigInteger mutableBigInteger) {
        super(mutableBigInteger);
    }

    void signedAdd(SignedMutableBigInteger signedMutableBigInteger) {
        if (this.sign == signedMutableBigInteger.sign) {
            this.add(signedMutableBigInteger);
        } else {
            this.sign *= this.subtract(signedMutableBigInteger);
        }
    }

    void signedAdd(MutableBigInteger mutableBigInteger) {
        if (this.sign == 1) {
            this.add(mutableBigInteger);
        } else {
            this.sign *= this.subtract(mutableBigInteger);
        }
    }

    void signedSubtract(SignedMutableBigInteger signedMutableBigInteger) {
        if (this.sign == signedMutableBigInteger.sign) {
            this.sign *= this.subtract(signedMutableBigInteger);
        } else {
            this.add(signedMutableBigInteger);
        }
    }

    void signedSubtract(MutableBigInteger mutableBigInteger) {
        if (this.sign == 1) {
            this.sign *= this.subtract(mutableBigInteger);
        } else {
            this.add(mutableBigInteger);
        }
        if (this.intLen == 0) {
            this.sign = 1;
        }
    }

    public String toString() {
        BigInteger bigInteger = new BigInteger(this, this.sign);
        return bigInteger.toString();
    }
}

