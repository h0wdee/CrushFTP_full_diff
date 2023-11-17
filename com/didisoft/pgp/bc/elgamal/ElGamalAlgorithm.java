/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal;

import com.didisoft.pgp.bc.elgamal.CryptixException;
import com.didisoft.pgp.bc.elgamal.util.Debug;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Random;

public final class ElGamalAlgorithm {
    private static final boolean DEBUG = true;
    private static final int debuglevel = Debug.getLevel("ElGamal", "ElGamalAlgorithm");
    private static final PrintWriter err = Debug.getOutput();
    private static final BigInteger ZERO = BigInteger.valueOf(0L);
    private static final BigInteger ONE = BigInteger.valueOf(1L);

    private ElGamalAlgorithm() {
    }

    private static void debug(String string) {
        err.println("ElGamalAlgorithm: " + string);
    }

    public static void encrypt(BigInteger bigInteger, BigInteger[] bigIntegerArray, BigInteger bigInteger2, BigInteger bigInteger3, BigInteger bigInteger4, Random random) {
        BigInteger bigInteger5;
        BigInteger bigInteger6 = bigInteger2.subtract(ONE);
        do {
            if ((bigInteger5 = new BigInteger(bigInteger2.bitLength() - 1, random)).testBit(0)) continue;
            bigInteger5 = bigInteger5.setBit(0);
        } while (!bigInteger5.gcd(bigInteger6).equals(ONE));
        bigIntegerArray[0] = bigInteger3.modPow(bigInteger5, bigInteger2);
        bigIntegerArray[1] = bigInteger4.modPow(bigInteger5, bigInteger2).multiply(bigInteger).mod(bigInteger2);
    }

    public static BigInteger decrypt(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, BigInteger bigInteger4, BigInteger bigInteger5) {
        try {
            return bigInteger2.multiply(bigInteger.modPow(bigInteger5, bigInteger3).modInverse(bigInteger3)).mod(bigInteger3);
        }
        catch (ArithmeticException arithmeticException) {
            throw new CryptixException("ElGamal: " + arithmeticException.getClass().getName() + " while calculating a.modPow(x, p).modInverse(p) - maybe key was not generated properly?");
        }
    }

    public static void sign(BigInteger bigInteger, BigInteger[] bigIntegerArray, BigInteger bigInteger2, BigInteger bigInteger3, BigInteger bigInteger4, Random random) {
        BigInteger bigInteger5;
        BigInteger bigInteger6;
        BigInteger bigInteger7 = bigInteger2.subtract(ONE);
        do {
            if ((bigInteger6 = new BigInteger(bigInteger2.bitLength() - 1, random)).testBit(0)) continue;
            bigInteger6 = bigInteger6.setBit(0);
        } while (!bigInteger6.gcd(bigInteger7).equals(ONE));
        bigIntegerArray[0] = bigInteger5 = bigInteger3.modPow(bigInteger6, bigInteger2);
        try {
            bigIntegerArray[1] = bigInteger6.modInverse(bigInteger7).multiply(bigInteger.subtract(bigInteger4.multiply(bigInteger5)).mod(bigInteger7)).mod(bigInteger7);
        }
        catch (ArithmeticException arithmeticException) {
            throw new CryptixException("ElGamal: ArithmeticException while calculating k.modInverse(p-1)");
        }
    }

    public static boolean verify(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, BigInteger bigInteger4, BigInteger bigInteger5, BigInteger bigInteger6) {
        BigInteger bigInteger7 = bigInteger4.subtract(ONE);
        if (bigInteger.compareTo(ZERO) < 0 || bigInteger.compareTo(bigInteger7) >= 0 || bigInteger2.compareTo(ZERO) < 0 || bigInteger2.compareTo(bigInteger7) >= 0 || bigInteger3.compareTo(ZERO) < 0 || bigInteger3.compareTo(bigInteger7) >= 0) {
            return false;
        }
        return bigInteger6.modPow(bigInteger2, bigInteger4).multiply(bigInteger2.modPow(bigInteger3, bigInteger4)).mod(bigInteger4).equals(bigInteger5.modPow(bigInteger, bigInteger4));
    }
}

