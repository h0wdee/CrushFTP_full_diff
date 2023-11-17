/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal.util;

import com.didisoft.pgp.bc.elgamal.util.BigInteger;
import com.didisoft.pgp.bc.elgamal.util.MutableBigInteger;

class BitSieve {
    private long[] bits;
    private int length;
    private static BitSieve smallSieve = new BitSieve();

    private BitSieve() {
        this.length = 9600;
        this.bits = new long[BitSieve.unitIndex(this.length - 1) + 1];
        this.set(0);
        int n = 1;
        int n2 = 3;
        do {
            this.sieveSingle(this.length, n + n2, n2);
            n = this.sieveSearch(this.length, n + 1);
            n2 = 2 * n + 1;
        } while (n > 0 && n2 < this.length);
    }

    BitSieve(BigInteger bigInteger, int n) {
        this.bits = new long[BitSieve.unitIndex(n - 1) + 1];
        this.length = n;
        int n2 = 0;
        int n3 = smallSieve.sieveSearch(BitSieve.smallSieve.length, n2);
        int n4 = n3 * 2 + 1;
        MutableBigInteger mutableBigInteger = new MutableBigInteger();
        MutableBigInteger mutableBigInteger2 = new MutableBigInteger();
        do {
            mutableBigInteger.copyValue(bigInteger.mag);
            mutableBigInteger.divideOneWord(n4, mutableBigInteger2);
            n2 = mutableBigInteger.value[mutableBigInteger.offset];
            n2 = n4 - n2;
            if (n2 % 2 == 0) {
                n2 += n4;
            }
            this.sieveSingle(n, (n2 - 1) / 2, n4);
            n3 = smallSieve.sieveSearch(BitSieve.smallSieve.length, n3 + 1);
            n4 = n3 * 2 + 1;
        } while (n3 > 0);
    }

    private static int unitIndex(int n) {
        return n >>> 6;
    }

    private static long bit(int n) {
        return 1L << (n & 0x3F);
    }

    private boolean get(int n) {
        int n2 = BitSieve.unitIndex(n);
        return (this.bits[n2] & BitSieve.bit(n)) != 0L;
    }

    private void set(int n) {
        int n2;
        int n3 = n2 = BitSieve.unitIndex(n);
        this.bits[n3] = this.bits[n3] | BitSieve.bit(n);
    }

    private int sieveSearch(int n, int n2) {
        if (n2 >= n) {
            return -1;
        }
        int n3 = n2;
        do {
            if (this.get(n3)) continue;
            return n3;
        } while (++n3 < n - 1);
        return -1;
    }

    private void sieveSingle(int n, int n2, int n3) {
        while (n2 < n) {
            this.set(n2);
            n2 += n3;
        }
    }

    BigInteger retrieve(BigInteger bigInteger, int n) {
        int n2 = 1;
        for (int i = 0; i < this.bits.length; ++i) {
            long l = this.bits[i] ^ 0xFFFFFFFFFFFFFFFFL;
            for (int j = 0; j < 64; ++j) {
                BigInteger bigInteger2;
                if ((l & 1L) == 1L && (bigInteger2 = bigInteger.add(BigInteger.valueOf(n2))).primeToCertainty(n)) {
                    return bigInteger2;
                }
                l >>>= 1;
                n2 += 2;
            }
        }
        return null;
    }
}

