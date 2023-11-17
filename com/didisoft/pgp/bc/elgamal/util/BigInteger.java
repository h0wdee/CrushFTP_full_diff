/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal.util;

import com.didisoft.pgp.bc.elgamal.util.BitSieve;
import com.didisoft.pgp.bc.elgamal.util.MutableBigInteger;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.StreamCorruptedException;
import java.util.Random;

public class BigInteger
extends Number
implements Comparable {
    int signum;
    int[] mag;
    private int bitCount = -1;
    private int bitLength = -1;
    private int lowestSetBit = -2;
    private int firstNonzeroByteNum = -2;
    private int firstNonzeroIntNum = -2;
    private static final long LONG_MASK = 0xFFFFFFFFL;
    private static long[] bitsPerDigit;
    private static final int SMALL_PRIME_THRESHOLD = 95;
    private static final BigInteger SMALL_PRIME_PRODUCT;
    private static final int MAX_CONSTANT = 16;
    private static BigInteger[] posConst;
    private static BigInteger[] negConst;
    public static final BigInteger ZERO;
    public static final BigInteger ONE;
    private static final BigInteger TWO;
    static int[] bnExpModThreshTable;
    static final byte[] trailingZeroTable;
    private static String[] zeros;
    private static int[] digitsPerLong;
    private static BigInteger[] longRadix;
    private static int[] digitsPerInt;
    private static int[] intRadix;
    private static final long serialVersionUID = -8287574255936472291L;
    private static final ObjectStreamField[] serialPersistentFields;

    public BigInteger(byte[] byArray) {
        if (byArray.length == 0) {
            throw new NumberFormatException("Zero length BigInteger");
        }
        if (byArray[0] < 0) {
            this.mag = BigInteger.makePositive(byArray);
            this.signum = -1;
        } else {
            this.mag = BigInteger.stripLeadingZeroBytes(byArray);
            this.signum = this.mag.length == 0 ? 0 : 1;
        }
    }

    private BigInteger(int[] nArray) {
        if (nArray.length == 0) {
            throw new NumberFormatException("Zero length BigInteger");
        }
        if (nArray[0] < 0) {
            this.mag = BigInteger.makePositive(nArray);
            this.signum = -1;
        } else {
            this.mag = BigInteger.trustedStripLeadingZeroInts(nArray);
            this.signum = this.mag.length == 0 ? 0 : 1;
        }
    }

    public BigInteger(int n, byte[] byArray) {
        this.mag = BigInteger.stripLeadingZeroBytes(byArray);
        if (n < -1 || n > 1) {
            throw new NumberFormatException("Invalid signum value");
        }
        if (this.mag.length == 0) {
            this.signum = 0;
        } else {
            if (n == 0) {
                throw new NumberFormatException("signum-magnitude mismatch");
            }
            this.signum = n;
        }
    }

    private BigInteger(int n, int[] nArray) {
        this.mag = BigInteger.stripLeadingZeroInts(nArray);
        if (n < -1 || n > 1) {
            throw new NumberFormatException("Invalid signum value");
        }
        if (this.mag.length == 0) {
            this.signum = 0;
        } else {
            if (n == 0) {
                throw new NumberFormatException("signum-magnitude mismatch");
            }
            this.signum = n;
        }
    }

    public BigInteger(String string, int n) {
        int n2 = 0;
        int n3 = string.length();
        if (n < 2 || n > 36) {
            throw new NumberFormatException("Radix out of range");
        }
        if (string.length() == 0) {
            throw new NumberFormatException("Zero length BigInteger");
        }
        this.signum = 1;
        int n4 = string.indexOf(45);
        if (n4 != -1) {
            if (n4 == 0) {
                if (string.length() == 1) {
                    throw new NumberFormatException("Zero length BigInteger");
                }
                this.signum = -1;
                n2 = 1;
            } else {
                throw new NumberFormatException("Illegal embedded minus sign");
            }
        }
        while (n2 < n3 && Character.digit(string.charAt(n2), n) == 0) {
            ++n2;
        }
        if (n2 == n3) {
            this.signum = 0;
            this.mag = BigInteger.ZERO.mag;
            return;
        }
        int n5 = n3 - n2;
        int n6 = (int)(((long)n5 * bitsPerDigit[n] >>> 10) + 1L);
        int n7 = (n6 + 31) / 32;
        this.mag = new int[n7];
        int n8 = n5 % digitsPerInt[n];
        if (n8 == 0) {
            n8 = digitsPerInt[n];
        }
        String string2 = string.substring(n2, n2 += n8);
        this.mag[this.mag.length - 1] = Integer.parseInt(string2, n);
        if (this.mag[this.mag.length - 1] < 0) {
            throw new NumberFormatException("Illegal digit");
        }
        int n9 = intRadix[n];
        int n10 = 0;
        while (n2 < string.length()) {
            if ((n10 = Integer.parseInt(string2 = string.substring(n2, n2 += digitsPerInt[n]), n)) < 0) {
                throw new NumberFormatException("Illegal digit");
            }
            BigInteger.destructiveMulAdd(this.mag, n9, n10);
        }
        this.mag = BigInteger.trustedStripLeadingZeroInts(this.mag);
    }

    BigInteger(char[] cArray) {
        int n;
        int n2;
        int n3 = 0;
        int n4 = cArray.length;
        this.signum = 1;
        if (cArray[0] == '-') {
            if (n4 == 1) {
                throw new NumberFormatException("Zero length BigInteger");
            }
            this.signum = -1;
            n3 = 1;
        }
        while (n3 < n4 && Character.digit(cArray[n3], 10) == 0) {
            ++n3;
        }
        if (n3 == n4) {
            this.signum = 0;
            this.mag = BigInteger.ZERO.mag;
            return;
        }
        int n5 = n4 - n3;
        if (n4 < 10) {
            n2 = 1;
        } else {
            n = (int)(((long)n5 * bitsPerDigit[10] >>> 10) + 1L);
            n2 = (n + 31) / 32;
        }
        this.mag = new int[n2];
        n = n5 % digitsPerInt[10];
        if (n == 0) {
            n = digitsPerInt[10];
        }
        this.mag[this.mag.length - 1] = this.parseInt(cArray, n3, n3 += n);
        while (n3 < n4) {
            int n6 = this.parseInt(cArray, n3, n3 += digitsPerInt[10]);
            BigInteger.destructiveMulAdd(this.mag, intRadix[10], n6);
        }
        this.mag = BigInteger.trustedStripLeadingZeroInts(this.mag);
    }

    private int parseInt(char[] cArray, int n, int n2) {
        int n3;
        if ((n3 = Character.digit(cArray[n++], 10)) == -1) {
            throw new NumberFormatException(new String(cArray));
        }
        for (int i = n; i < n2; ++i) {
            int n4 = Character.digit(cArray[i], 10);
            if (n4 == -1) {
                throw new NumberFormatException(new String(cArray));
            }
            n3 = 10 * n3 + n4;
        }
        return n3;
    }

    private static void destructiveMulAdd(int[] nArray, int n, int n2) {
        long l = (long)n & 0xFFFFFFFFL;
        long l2 = (long)n2 & 0xFFFFFFFFL;
        int n3 = nArray.length;
        long l3 = 0L;
        long l4 = 0L;
        for (int i = n3 - 1; i >= 0; --i) {
            l3 = l * ((long)nArray[i] & 0xFFFFFFFFL) + l4;
            nArray[i] = (int)l3;
            l4 = l3 >>> 32;
        }
        long l5 = ((long)nArray[n3 - 1] & 0xFFFFFFFFL) + l2;
        nArray[n3 - 1] = (int)l5;
        l4 = l5 >>> 32;
        for (int i = n3 - 2; i >= 0; --i) {
            l5 = ((long)nArray[i] & 0xFFFFFFFFL) + l4;
            nArray[i] = (int)l5;
            l4 = l5 >>> 32;
        }
    }

    public BigInteger(String string) {
        this(string, 10);
    }

    public BigInteger(int n, Random random) {
        this(1, BigInteger.randomBits(n, random));
    }

    private static byte[] randomBits(int n, Random random) {
        if (n < 0) {
            throw new IllegalArgumentException("numBits must be non-negative");
        }
        int n2 = (n + 7) / 8;
        byte[] byArray = new byte[n2];
        if (n2 > 0) {
            random.nextBytes(byArray);
            int n3 = 8 * n2 - n;
            byArray[0] = (byte)(byArray[0] & (1 << 8 - n3) - 1);
        }
        return byArray;
    }

    public BigInteger(int n, int n2, Random random) {
        if (n < 2) {
            throw new ArithmeticException("bitLength < 2");
        }
        BigInteger bigInteger = n < 95 ? BigInteger.smallPrime(n, n2, random) : BigInteger.largePrime(n, n2, random);
        this.signum = 1;
        this.mag = bigInteger.mag;
    }

    public static BigInteger probablePrime(int n, Random random) {
        if (n < 2) {
            throw new ArithmeticException("bitLength < 2");
        }
        return n < 95 ? BigInteger.smallPrime(n, 100, random) : BigInteger.largePrime(n, 100, random);
    }

    /*
     * Unable to fully structure code
     */
    private static BigInteger smallPrime(int var0, int var1_1, Random var2_2) {
        var3_3 = var0 + 31 >>> 5;
        var4_4 = new int[var3_3];
        var5_5 = 1 << (var0 + 31 & 31);
        var6_6 = (var5_5 << 1) - 1;
        while (true) {
            for (var7_8 = 0; var7_8 < var3_3; ++var7_8) {
                var4_4[var7_8] = var2_2.nextInt();
            }
            var4_4[0] = var4_4[0] & var6_6 | var5_5;
            if (var0 > 2) {
                v0 = var3_3 - 1;
                var4_4[v0] = var4_4[v0] | 1;
            }
            var7_7 = new BigInteger(var4_4, 1);
            if (var0 > 6 && ((var8_9 = var7_7.remainder(BigInteger.SMALL_PRIME_PRODUCT).longValue()) % 3L == 0L || var8_9 % 5L == 0L || var8_9 % 7L == 0L || var8_9 % 11L == 0L || var8_9 % 13L == 0L || var8_9 % 17L == 0L || var8_9 % 19L == 0L || var8_9 % 23L == 0L || var8_9 % 29L == 0L || var8_9 % 31L == 0L || var8_9 % 37L == 0L || var8_9 % 41L == 0L)) ** continue;
            if (var0 < 4) {
                return var7_7;
            }
            if (var7_7.primeToCertainty(var1_1)) break;
        }
        return var7_7;
    }

    private static BigInteger largePrime(int n, int n2, Random random) {
        BigInteger bigInteger = new BigInteger(n, random).setBit(n - 1);
        int n3 = bigInteger.mag.length - 1;
        bigInteger.mag[n3] = bigInteger.mag[n3] & 0xFFFFFFFE;
        int n4 = n / 20 * 64;
        BitSieve bitSieve = new BitSieve(bigInteger, n4);
        BigInteger bigInteger2 = bitSieve.retrieve(bigInteger, n2);
        while (bigInteger2 == null || bigInteger2.bitLength() != n) {
            if ((bigInteger = bigInteger.add(BigInteger.valueOf(2 * n4))).bitLength() != n) {
                bigInteger = new BigInteger(n, random).setBit(n - 1);
            }
            int n5 = bigInteger.mag.length - 1;
            bigInteger.mag[n5] = bigInteger.mag[n5] & 0xFFFFFFFE;
            bitSieve = new BitSieve(bigInteger, n4);
            bigInteger2 = bitSieve.retrieve(bigInteger, n2);
        }
        return bigInteger2;
    }

    boolean primeToCertainty(int n) {
        int n2 = 0;
        int n3 = (Math.min(n, 0x7FFFFFFE) + 1) / 2;
        int n4 = this.bitLength();
        if (n4 < 100) {
            n2 = 50;
            n2 = n3 < n2 ? n3 : n2;
            return this.passesMillerRabin(n2);
        }
        n2 = n4 < 256 ? 27 : (n4 < 512 ? 15 : (n4 < 768 ? 8 : (n4 < 1024 ? 4 : 2)));
        n2 = n3 < n2 ? n3 : n2;
        return this.passesMillerRabin(n2);
    }

    private boolean passesLucasLehmer() {
        BigInteger bigInteger = this.add(ONE);
        int n = 5;
        while (this.jacobiSymbol(n, this) != -1) {
            n = n < 0 ? Math.abs(n) + 2 : -(n + 2);
        }
        BigInteger bigInteger2 = BigInteger.lucasLehmerSequence(n, bigInteger, this);
        return bigInteger2.mod(this).equals(ZERO);
    }

    int jacobiSymbol(int n, BigInteger bigInteger) {
        int n2;
        if (n == 0) {
            return 0;
        }
        int n3 = 1;
        int n4 = bigInteger.mag[bigInteger.mag.length - 1];
        if (n < 0) {
            n = -n;
            n2 = n4 & 7;
            if (n2 == 3 || n2 == 7) {
                n3 = -n3;
            }
        }
        while ((n & 3) == 0) {
            n >>= 2;
        }
        if ((n & 1) == 0) {
            n >>= 1;
            if (((n4 ^ n4 >> 1) & 2) != 0) {
                n3 = -n3;
            }
        }
        if (n == 1) {
            return n3;
        }
        if ((n & n4 & 2) != 0) {
            n3 = -n3;
        }
        for (n4 = bigInteger.mod(BigInteger.valueOf(n)).intValue(); n4 != 0; n4 %= n) {
            while ((n4 & 3) == 0) {
                n4 >>= 2;
            }
            if ((n4 & 1) == 0) {
                n4 >>= 1;
                if (((n ^ n >> 1) & 2) != 0) {
                    n3 = -n3;
                }
            }
            if (n4 == 1) {
                return n3;
            }
            if (n4 >= n || ((n4 = n) & (n = (n2 = n4)) & 2) == 0) continue;
            n3 = -n3;
        }
        return 0;
    }

    private static BigInteger lucasLehmerSequence(int n, BigInteger bigInteger, BigInteger bigInteger2) {
        BigInteger bigInteger3 = BigInteger.valueOf(n);
        BigInteger bigInteger4 = ONE;
        BigInteger bigInteger5 = ONE;
        for (int i = bigInteger.bitLength() - 2; i >= 0; --i) {
            BigInteger bigInteger6 = bigInteger4.multiply(bigInteger5).mod(bigInteger2);
            BigInteger bigInteger7 = bigInteger5.square().add(bigInteger3.multiply(bigInteger4.square())).mod(bigInteger2);
            if (bigInteger7.testBit(0)) {
                bigInteger7 = bigInteger2.subtract(bigInteger7);
                bigInteger7.signum = -bigInteger7.signum;
            }
            bigInteger7 = bigInteger7.shiftRight(1);
            bigInteger4 = bigInteger6;
            bigInteger5 = bigInteger7;
            if (!bigInteger.testBit(i)) continue;
            bigInteger6 = bigInteger4.add(bigInteger5).mod(bigInteger2);
            if (bigInteger6.testBit(0)) {
                bigInteger6 = bigInteger2.subtract(bigInteger6);
                bigInteger6.signum = -bigInteger6.signum;
            }
            bigInteger6 = bigInteger6.shiftRight(1);
            bigInteger7 = bigInteger5.add(bigInteger3.multiply(bigInteger4)).mod(bigInteger2);
            if (bigInteger7.testBit(0)) {
                bigInteger7 = bigInteger2.subtract(bigInteger7);
                bigInteger7.signum = -bigInteger7.signum;
            }
            bigInteger7 = bigInteger7.shiftRight(1);
            bigInteger4 = bigInteger6;
            bigInteger5 = bigInteger7;
        }
        return bigInteger4;
    }

    private boolean passesMillerRabin(int n) {
        BigInteger bigInteger;
        BigInteger bigInteger2 = bigInteger = this.subtract(ONE);
        int n2 = bigInteger2.getLowestSetBit();
        bigInteger2 = bigInteger2.shiftRight(n2);
        Random random = new Random();
        for (int i = 0; i < n; ++i) {
            BigInteger bigInteger3;
            while ((bigInteger3 = new BigInteger(this.bitLength(), random)).compareTo(ONE) <= 0 || bigInteger3.compareTo(this) >= 0) {
            }
            int n3 = 0;
            BigInteger bigInteger4 = bigInteger3.modPow(bigInteger2, this);
            while (!(n3 == 0 && bigInteger4.equals(ONE) || bigInteger4.equals(bigInteger))) {
                if (n3 > 0 && bigInteger4.equals(ONE) || ++n3 == n2) {
                    return false;
                }
                bigInteger4 = bigInteger4.modPow(TWO, this);
            }
        }
        return true;
    }

    private BigInteger(int[] nArray, int n) {
        this.signum = nArray.length == 0 ? 0 : n;
        this.mag = nArray;
    }

    private BigInteger(byte[] byArray, int n) {
        this.signum = byArray.length == 0 ? 0 : n;
        this.mag = BigInteger.stripLeadingZeroBytes(byArray);
    }

    BigInteger(MutableBigInteger mutableBigInteger, int n) {
        if (mutableBigInteger.offset > 0 || mutableBigInteger.value.length != mutableBigInteger.intLen) {
            this.mag = new int[mutableBigInteger.intLen];
            for (int i = 0; i < mutableBigInteger.intLen; ++i) {
                this.mag[i] = mutableBigInteger.value[mutableBigInteger.offset + i];
            }
        } else {
            this.mag = mutableBigInteger.value;
        }
        this.signum = mutableBigInteger.intLen == 0 ? 0 : n;
    }

    public static BigInteger valueOf(long l) {
        if (l == 0L) {
            return ZERO;
        }
        if (l > 0L && l <= 16L) {
            return posConst[(int)l];
        }
        if (l < 0L && l >= -16L) {
            return negConst[(int)(-l)];
        }
        return new BigInteger(l);
    }

    private BigInteger(long l) {
        if (l < 0L) {
            this.signum = -1;
            l = -l;
        } else {
            this.signum = 1;
        }
        int n = (int)(l >>> 32);
        if (n == 0) {
            this.mag = new int[1];
            this.mag[0] = (int)l;
        } else {
            this.mag = new int[2];
            this.mag[0] = n;
            this.mag[1] = (int)l;
        }
    }

    private static BigInteger valueOf(int[] nArray) {
        return nArray[0] > 0 ? new BigInteger(nArray, 1) : new BigInteger(nArray);
    }

    public BigInteger add(BigInteger bigInteger) {
        if (bigInteger.signum == 0) {
            return this;
        }
        if (this.signum == 0) {
            return bigInteger;
        }
        if (bigInteger.signum == this.signum) {
            return new BigInteger(BigInteger.add(this.mag, bigInteger.mag), this.signum);
        }
        int n = BigInteger.intArrayCmp(this.mag, bigInteger.mag);
        if (n == 0) {
            return ZERO;
        }
        int[] nArray = n > 0 ? BigInteger.subtract(this.mag, bigInteger.mag) : BigInteger.subtract(bigInteger.mag, this.mag);
        nArray = BigInteger.trustedStripLeadingZeroInts(nArray);
        return new BigInteger(nArray, n * this.signum);
    }

    private static int[] add(int[] nArray, int[] nArray2) {
        boolean bl;
        if (nArray.length < nArray2.length) {
            int[] nArray3 = nArray;
            nArray = nArray2;
            nArray2 = nArray3;
        }
        int n = nArray.length;
        int n2 = nArray2.length;
        int[] nArray4 = new int[n];
        long l = 0L;
        while (n2 > 0) {
            l = ((long)nArray[--n] & 0xFFFFFFFFL) + ((long)nArray2[--n2] & 0xFFFFFFFFL) + (l >>> 32);
            nArray4[n] = (int)l;
        }
        boolean bl2 = bl = l >>> 32 != 0L;
        while (n > 0 && bl) {
            nArray4[--n] = nArray[n] + 1;
            bl = nArray4[--n] == 0;
        }
        while (n > 0) {
            nArray4[--n] = nArray[n];
        }
        if (bl) {
            int n3 = nArray4.length + 1;
            int[] nArray5 = new int[n3];
            for (int i = 1; i < n3; ++i) {
                nArray5[i] = nArray4[i - 1];
            }
            nArray5[0] = 1;
            nArray4 = nArray5;
        }
        return nArray4;
    }

    public BigInteger subtract(BigInteger bigInteger) {
        if (bigInteger.signum == 0) {
            return this;
        }
        if (this.signum == 0) {
            return bigInteger.negate();
        }
        if (bigInteger.signum != this.signum) {
            return new BigInteger(BigInteger.add(this.mag, bigInteger.mag), this.signum);
        }
        int n = BigInteger.intArrayCmp(this.mag, bigInteger.mag);
        if (n == 0) {
            return ZERO;
        }
        int[] nArray = n > 0 ? BigInteger.subtract(this.mag, bigInteger.mag) : BigInteger.subtract(bigInteger.mag, this.mag);
        nArray = BigInteger.trustedStripLeadingZeroInts(nArray);
        return new BigInteger(nArray, n * this.signum);
    }

    private static int[] subtract(int[] nArray, int[] nArray2) {
        boolean bl;
        int n = nArray.length;
        int[] nArray3 = new int[n];
        int n2 = nArray2.length;
        long l = 0L;
        while (n2 > 0) {
            l = ((long)nArray[--n] & 0xFFFFFFFFL) - ((long)nArray2[--n2] & 0xFFFFFFFFL) + (l >> 32);
            nArray3[n] = (int)l;
        }
        boolean bl2 = bl = l >> 32 != 0L;
        while (n > 0 && bl) {
            nArray3[--n] = nArray[n] - 1;
            bl = nArray3[--n] == -1;
        }
        while (n > 0) {
            nArray3[--n] = nArray[n];
        }
        return nArray3;
    }

    public BigInteger multiply(BigInteger bigInteger) {
        if (this.signum == 0 || bigInteger.signum == 0) {
            return ZERO;
        }
        int[] nArray = this.multiplyToLen(this.mag, this.mag.length, bigInteger.mag, bigInteger.mag.length, null);
        nArray = BigInteger.trustedStripLeadingZeroInts(nArray);
        return new BigInteger(nArray, this.signum * bigInteger.signum);
    }

    private int[] multiplyToLen(int[] nArray, int n, int[] nArray2, int n2, int[] nArray3) {
        int n3 = n - 1;
        int n4 = n2 - 1;
        if (nArray3 == null || nArray3.length < n + n2) {
            nArray3 = new int[n + n2];
        }
        long l = 0L;
        int n5 = n4;
        int n6 = n4 + 1 + n3;
        while (n5 >= 0) {
            long l2 = ((long)nArray2[n5] & 0xFFFFFFFFL) * ((long)nArray[n3] & 0xFFFFFFFFL) + l;
            nArray3[n6] = (int)l2;
            l = l2 >>> 32;
            --n5;
            --n6;
        }
        nArray3[n3] = (int)l;
        for (n5 = n3 - 1; n5 >= 0; --n5) {
            l = 0L;
            n6 = n4;
            int n7 = n4 + 1 + n5;
            while (n6 >= 0) {
                long l3 = ((long)nArray2[n6] & 0xFFFFFFFFL) * ((long)nArray[n5] & 0xFFFFFFFFL) + ((long)nArray3[n7] & 0xFFFFFFFFL) + l;
                nArray3[n7] = (int)l3;
                l = l3 >>> 32;
                --n6;
                --n7;
            }
            nArray3[n5] = (int)l;
        }
        return nArray3;
    }

    private BigInteger square() {
        if (this.signum == 0) {
            return ZERO;
        }
        int[] nArray = BigInteger.squareToLen(this.mag, this.mag.length, null);
        return new BigInteger(BigInteger.trustedStripLeadingZeroInts(nArray), 1);
    }

    private static final int[] squareToLen(int[] nArray, int n, int[] nArray2) {
        int n2;
        int n3 = n << 1;
        if (nArray2 == null || nArray2.length < n3) {
            nArray2 = new int[n3];
        }
        int n4 = 0;
        int n5 = 0;
        for (n2 = 0; n2 < n; ++n2) {
            long l = (long)nArray[n2] & 0xFFFFFFFFL;
            long l2 = l * l;
            nArray2[n5++] = n4 << 31 | (int)(l2 >>> 33);
            nArray2[n5++] = (int)(l2 >>> 1);
            n4 = (int)l2;
        }
        n2 = n;
        n5 = 1;
        while (n2 > 0) {
            int n6 = nArray[n2 - 1];
            n6 = BigInteger.mulAdd(nArray2, nArray, n5, n2 - 1, n6);
            BigInteger.addOne(nArray2, n5 - 1, n2, n6);
            --n2;
            n5 += 2;
        }
        BigInteger.primitiveLeftShift(nArray2, n3, 1);
        int n7 = n3 - 1;
        nArray2[n7] = nArray2[n7] | nArray[n - 1] & 1;
        return nArray2;
    }

    public BigInteger divide(BigInteger bigInteger) {
        MutableBigInteger mutableBigInteger = new MutableBigInteger();
        MutableBigInteger mutableBigInteger2 = new MutableBigInteger();
        MutableBigInteger mutableBigInteger3 = new MutableBigInteger(this.mag);
        MutableBigInteger mutableBigInteger4 = new MutableBigInteger(bigInteger.mag);
        mutableBigInteger3.divide(mutableBigInteger4, mutableBigInteger, mutableBigInteger2);
        return new BigInteger(mutableBigInteger, this.signum * bigInteger.signum);
    }

    public BigInteger[] divideAndRemainder(BigInteger bigInteger) {
        BigInteger[] bigIntegerArray = new BigInteger[2];
        MutableBigInteger mutableBigInteger = new MutableBigInteger();
        MutableBigInteger mutableBigInteger2 = new MutableBigInteger();
        MutableBigInteger mutableBigInteger3 = new MutableBigInteger(this.mag);
        MutableBigInteger mutableBigInteger4 = new MutableBigInteger(bigInteger.mag);
        mutableBigInteger3.divide(mutableBigInteger4, mutableBigInteger, mutableBigInteger2);
        bigIntegerArray[0] = new BigInteger(mutableBigInteger, this.signum * bigInteger.signum);
        bigIntegerArray[1] = new BigInteger(mutableBigInteger2, this.signum);
        return bigIntegerArray;
    }

    public BigInteger remainder(BigInteger bigInteger) {
        MutableBigInteger mutableBigInteger = new MutableBigInteger();
        MutableBigInteger mutableBigInteger2 = new MutableBigInteger();
        MutableBigInteger mutableBigInteger3 = new MutableBigInteger(this.mag);
        MutableBigInteger mutableBigInteger4 = new MutableBigInteger(bigInteger.mag);
        mutableBigInteger3.divide(mutableBigInteger4, mutableBigInteger, mutableBigInteger2);
        return new BigInteger(mutableBigInteger2, this.signum);
    }

    public BigInteger pow(int n) {
        if (n < 0) {
            throw new ArithmeticException("Negative exponent");
        }
        if (this.signum == 0) {
            return n == 0 ? ONE : this;
        }
        int n2 = this.signum < 0 && (n & 1) == 1 ? -1 : 1;
        int[] nArray = this.mag;
        int[] nArray2 = new int[]{1};
        while (n != 0) {
            if ((n & 1) == 1) {
                nArray2 = this.multiplyToLen(nArray2, nArray2.length, nArray, nArray.length, null);
                nArray2 = BigInteger.trustedStripLeadingZeroInts(nArray2);
            }
            if ((n >>>= 1) == 0) continue;
            nArray = BigInteger.squareToLen(nArray, nArray.length, null);
            nArray = BigInteger.trustedStripLeadingZeroInts(nArray);
        }
        return new BigInteger(nArray2, n2);
    }

    public BigInteger gcd(BigInteger bigInteger) {
        if (bigInteger.signum == 0) {
            return this.abs();
        }
        if (this.signum == 0) {
            return bigInteger.abs();
        }
        MutableBigInteger mutableBigInteger = new MutableBigInteger(this);
        MutableBigInteger mutableBigInteger2 = new MutableBigInteger(bigInteger);
        MutableBigInteger mutableBigInteger3 = mutableBigInteger.hybridGCD(mutableBigInteger2);
        return new BigInteger(mutableBigInteger3, 1);
    }

    private static int[] leftShift(int[] nArray, int n, int n2) {
        int n3 = n2 >>> 5;
        int n4 = n2 & 0x1F;
        int n5 = BigInteger.bitLen(nArray[0]);
        if (n2 <= 32 - n5) {
            BigInteger.primitiveLeftShift(nArray, n, n4);
            return nArray;
        }
        if (n4 <= 32 - n5) {
            int[] nArray2 = new int[n3 + n];
            for (int i = 0; i < n; ++i) {
                nArray2[i] = nArray[i];
            }
            BigInteger.primitiveLeftShift(nArray2, nArray2.length, n4);
            return nArray2;
        }
        int[] nArray3 = new int[n3 + n + 1];
        for (int i = 0; i < n; ++i) {
            nArray3[i] = nArray[i];
        }
        BigInteger.primitiveRightShift(nArray3, nArray3.length, 32 - n4);
        return nArray3;
    }

    static void primitiveRightShift(int[] nArray, int n, int n2) {
        int n3;
        int n4 = 32 - n2;
        int n5 = nArray[n3];
        for (n3 = n - 1; n3 > 0; --n3) {
            int n6 = n5;
            n5 = nArray[n3 - 1];
            nArray[n3] = n5 << n4 | n6 >>> n2;
        }
        nArray[0] = nArray[0] >>> n2;
    }

    static void primitiveLeftShift(int[] nArray, int n, int n2) {
        int n3;
        if (n == 0 || n2 == 0) {
            return;
        }
        int n4 = 32 - n2;
        int n5 = nArray[n3];
        int n6 = n3 + n - 1;
        for (n3 = 0; n3 < n6; ++n3) {
            int n7 = n5;
            n5 = nArray[n3 + 1];
            nArray[n3] = n7 << n2 | n5 >>> n4;
        }
        int n8 = n - 1;
        nArray[n8] = nArray[n8] << n2;
    }

    private static int bitLength(int[] nArray, int n) {
        if (n == 0) {
            return 0;
        }
        return (n - 1 << 5) + BigInteger.bitLen(nArray[0]);
    }

    public BigInteger abs() {
        return this.signum >= 0 ? this : this.negate();
    }

    public BigInteger negate() {
        return new BigInteger(this.mag, -this.signum);
    }

    public int signum() {
        return this.signum;
    }

    public BigInteger mod(BigInteger bigInteger) {
        if (bigInteger.signum <= 0) {
            throw new ArithmeticException("BigInteger: modulus not positive");
        }
        BigInteger bigInteger2 = this.remainder(bigInteger);
        return bigInteger2.signum >= 0 ? bigInteger2 : bigInteger2.add(bigInteger);
    }

    public BigInteger modPow(BigInteger bigInteger, BigInteger bigInteger2) {
        BigInteger bigInteger3;
        BigInteger bigInteger4;
        if (bigInteger2.signum <= 0) {
            throw new ArithmeticException("BigInteger: modulus not positive");
        }
        if (bigInteger.signum == 0) {
            return bigInteger2.equals(ONE) ? ZERO : ONE;
        }
        if (this.equals(ONE)) {
            return bigInteger2.equals(ONE) ? ZERO : ONE;
        }
        if (this.equals(ZERO) && bigInteger.signum >= 0) {
            return ZERO;
        }
        if (this.equals(negConst[1]) && !bigInteger.testBit(0)) {
            return bigInteger2.equals(ONE) ? ZERO : ONE;
        }
        boolean bl = bigInteger.signum < 0;
        if (bl) {
            bigInteger = bigInteger.negate();
        }
        BigInteger bigInteger5 = bigInteger4 = this.signum < 0 || this.compareTo(bigInteger2) >= 0 ? this.mod(bigInteger2) : this;
        if (bigInteger2.testBit(0)) {
            bigInteger3 = bigInteger4.oddModPow(bigInteger, bigInteger2);
        } else {
            int n = bigInteger2.getLowestSetBit();
            BigInteger bigInteger6 = bigInteger2.shiftRight(n);
            BigInteger bigInteger7 = ONE.shiftLeft(n);
            BigInteger bigInteger8 = this.signum < 0 || this.compareTo(bigInteger6) >= 0 ? this.mod(bigInteger6) : this;
            BigInteger bigInteger9 = bigInteger6.equals(ONE) ? ZERO : bigInteger8.oddModPow(bigInteger, bigInteger6);
            BigInteger bigInteger10 = bigInteger4.modPow2(bigInteger, n);
            BigInteger bigInteger11 = bigInteger7.modInverse(bigInteger6);
            BigInteger bigInteger12 = bigInteger6.modInverse(bigInteger7);
            bigInteger3 = bigInteger9.multiply(bigInteger7).multiply(bigInteger11).add(bigInteger10.multiply(bigInteger6).multiply(bigInteger12)).mod(bigInteger2);
        }
        return bl ? bigInteger3.modInverse(bigInteger2) : bigInteger3;
    }

    private BigInteger oddModPow(BigInteger bigInteger, BigInteger bigInteger2) {
        int n;
        int n2;
        int n3;
        int[] nArray;
        int n4;
        if (bigInteger.equals(ONE)) {
            return this;
        }
        if (this.signum == 0) {
            return ZERO;
        }
        int[] nArray2 = (int[])this.mag.clone();
        int[] nArray3 = bigInteger.mag;
        int[] nArray4 = bigInteger2.mag;
        int n5 = nArray4.length;
        int n6 = 0;
        int n7 = BigInteger.bitLength(nArray3, nArray3.length);
        while (n7 > bnExpModThreshTable[n6]) {
            ++n6;
        }
        int n8 = 1 << n6;
        int[][] nArrayArray = new int[n8][];
        for (n4 = 0; n4 < n8; ++n4) {
            nArrayArray[n4] = new int[n5];
        }
        n4 = -MutableBigInteger.inverseMod32(nArray4[n5 - 1]);
        int[] nArray5 = BigInteger.leftShift(nArray2, nArray2.length, n5 << 5);
        MutableBigInteger mutableBigInteger = new MutableBigInteger();
        MutableBigInteger mutableBigInteger2 = new MutableBigInteger();
        MutableBigInteger mutableBigInteger3 = new MutableBigInteger(nArray5);
        MutableBigInteger mutableBigInteger4 = new MutableBigInteger(nArray4);
        mutableBigInteger3.divide(mutableBigInteger4, mutableBigInteger, mutableBigInteger2);
        nArrayArray[0] = mutableBigInteger2.toIntArray();
        if (nArrayArray[0].length < n5) {
            int n9 = n5 - nArrayArray[0].length;
            nArray = new int[n5];
            for (n3 = 0; n3 < nArrayArray[0].length; ++n3) {
                nArray[n3 + n9] = nArrayArray[0][n3];
            }
            nArrayArray[0] = nArray;
        }
        int[] nArray6 = BigInteger.squareToLen(nArrayArray[0], n5, null);
        nArray6 = BigInteger.montReduce(nArray6, nArray4, n5, n4);
        nArray = new int[n5];
        for (n3 = 0; n3 < n5; ++n3) {
            nArray[n3] = nArray6[n3];
        }
        for (n3 = 1; n3 < n8; ++n3) {
            int[] nArray7 = this.multiplyToLen(nArray, n5, nArrayArray[n3 - 1], n5, null);
            nArrayArray[n3] = BigInteger.montReduce(nArray7, nArray4, n5, n4);
        }
        n3 = 1 << (n7 - 1 & 0x1F);
        int n10 = 0;
        int n11 = nArray3.length;
        int n12 = 0;
        for (n2 = 0; n2 <= n6; ++n2) {
            n10 = n10 << 1 | ((nArray3[n12] & n3) != 0 ? 1 : 0);
            if ((n3 >>>= 1) != 0) continue;
            ++n12;
            n3 = Integer.MIN_VALUE;
            --n11;
        }
        n2 = n7--;
        boolean bl = true;
        n2 = n7 - n6;
        while ((n10 & 1) == 0) {
            n10 >>>= 1;
            ++n2;
        }
        int[] nArray8 = nArrayArray[n10 >>> 1];
        n10 = 0;
        if (n2 == n7) {
            bl = false;
        }
        while (true) {
            --n7;
            n10 <<= 1;
            if (n11 != 0) {
                n10 |= (nArray3[n12] & n3) != 0 ? 1 : 0;
                if ((n3 >>>= 1) == 0) {
                    ++n12;
                    n3 = Integer.MIN_VALUE;
                    --n11;
                }
            }
            if ((n10 & n8) != 0) {
                n2 = n7 - n6;
                while ((n10 & 1) == 0) {
                    n10 >>>= 1;
                    ++n2;
                }
                nArray8 = nArrayArray[n10 >>> 1];
                n10 = 0;
            }
            if (n7 == n2) {
                if (bl) {
                    nArray6 = (int[])nArray8.clone();
                    bl = false;
                } else {
                    nArray = nArray6;
                    nArray5 = this.multiplyToLen(nArray, n5, nArray8, n5, nArray5);
                    nArray5 = BigInteger.montReduce(nArray5, nArray4, n5, n4);
                    nArray = nArray5;
                    nArray5 = nArray6;
                    nArray6 = nArray;
                }
            }
            if (n7 == 0) break;
            if (bl) continue;
            nArray = nArray6;
            nArray5 = BigInteger.squareToLen(nArray, n5, nArray5);
            nArray5 = BigInteger.montReduce(nArray5, nArray4, n5, n4);
            nArray = nArray5;
            nArray5 = nArray6;
            nArray6 = nArray;
        }
        int[] nArray9 = new int[2 * n5];
        for (n = 0; n < n5; ++n) {
            nArray9[n + n5] = nArray6[n];
        }
        nArray6 = BigInteger.montReduce(nArray9, nArray4, n5, n4);
        nArray9 = new int[n5];
        for (n = 0; n < n5; ++n) {
            nArray9[n] = nArray6[n];
        }
        return new BigInteger(1, nArray9);
    }

    private static int[] montReduce(int[] nArray, int[] nArray2, int n, int n2) {
        int n3 = 0;
        int n4 = n;
        int n5 = 0;
        do {
            int n6 = nArray[nArray.length - 1 - n5];
            int n7 = BigInteger.mulAdd(nArray, nArray2, n5, n, n2 * n6);
            n3 += BigInteger.addOne(nArray, n5, n, n7);
            ++n5;
        } while (--n4 > 0);
        while (n3 > 0) {
            n3 += BigInteger.subN(nArray, nArray2, n);
        }
        while (BigInteger.intArrayCmpToLen(nArray, nArray2, n) >= 0) {
            BigInteger.subN(nArray, nArray2, n);
        }
        return nArray;
    }

    private static int intArrayCmpToLen(int[] nArray, int[] nArray2, int n) {
        for (int i = 0; i < n; ++i) {
            long l = (long)nArray[i] & 0xFFFFFFFFL;
            long l2 = (long)nArray2[i] & 0xFFFFFFFFL;
            if (l < l2) {
                return -1;
            }
            if (l <= l2) continue;
            return 1;
        }
        return 0;
    }

    private static int subN(int[] nArray, int[] nArray2, int n) {
        long l = 0L;
        while (--n >= 0) {
            l = ((long)nArray[n] & 0xFFFFFFFFL) - ((long)nArray2[n] & 0xFFFFFFFFL) + (l >> 32);
            nArray[n] = (int)l;
        }
        return (int)(l >> 32);
    }

    static int mulAdd(int[] nArray, int[] nArray2, int n, int n2, int n3) {
        long l = (long)n3 & 0xFFFFFFFFL;
        long l2 = 0L;
        n = nArray.length - n - 1;
        for (int i = n2 - 1; i >= 0; --i) {
            long l3 = ((long)nArray2[i] & 0xFFFFFFFFL) * l + ((long)nArray[n] & 0xFFFFFFFFL) + l2;
            nArray[n--] = (int)l3;
            l2 = l3 >>> 32;
        }
        return (int)l2;
    }

    static int addOne(int[] nArray, int n, int n2, int n3) {
        n = nArray.length - 1 - n2 - n;
        long l = ((long)nArray[n] & 0xFFFFFFFFL) + ((long)n3 & 0xFFFFFFFFL);
        nArray[n] = (int)l;
        if (l >>> 32 == 0L) {
            return 0;
        }
        while (--n2 >= 0) {
            if (--n < 0) {
                return 1;
            }
            int n4 = n;
            nArray[n4] = nArray[n4] + 1;
            if (nArray[n] == 0) continue;
            return 0;
        }
        return 1;
    }

    private BigInteger modPow2(BigInteger bigInteger, int n) {
        BigInteger bigInteger2 = BigInteger.valueOf(1L);
        BigInteger bigInteger3 = this.mod2(n);
        int n2 = 0;
        int n3 = bigInteger.bitLength();
        if (this.testBit(0)) {
            int n4 = n3 = n - 1 < n3 ? n - 1 : n3;
        }
        while (n2 < n3) {
            if (bigInteger.testBit(n2)) {
                bigInteger2 = bigInteger2.multiply(bigInteger3).mod2(n);
            }
            if (++n2 >= n3) continue;
            bigInteger3 = bigInteger3.square().mod2(n);
        }
        return bigInteger2;
    }

    private BigInteger mod2(int n) {
        int n2;
        if (this.bitLength() <= n) {
            return this;
        }
        int n3 = (n + 31) / 32;
        int[] nArray = new int[n3];
        for (n2 = 0; n2 < n3; ++n2) {
            nArray[n2] = this.mag[n2 + (this.mag.length - n3)];
        }
        n2 = (n3 << 5) - n;
        nArray[0] = (int)((long)nArray[0] & (1L << 32 - n2) - 1L);
        return nArray[0] == 0 ? new BigInteger(1, nArray) : new BigInteger(nArray, 1);
    }

    public BigInteger modInverse(BigInteger bigInteger) {
        if (bigInteger.signum != 1) {
            throw new ArithmeticException("BigInteger: modulus not positive");
        }
        if (bigInteger.equals(ONE)) {
            return ZERO;
        }
        BigInteger bigInteger2 = this;
        if (this.signum < 0 || BigInteger.intArrayCmp(this.mag, bigInteger.mag) >= 0) {
            bigInteger2 = this.mod(bigInteger);
        }
        if (bigInteger2.equals(ONE)) {
            return ONE;
        }
        MutableBigInteger mutableBigInteger = new MutableBigInteger(bigInteger2);
        MutableBigInteger mutableBigInteger2 = new MutableBigInteger(bigInteger);
        MutableBigInteger mutableBigInteger3 = mutableBigInteger.mutableModInverse(mutableBigInteger2);
        return new BigInteger(mutableBigInteger3, 1);
    }

    public BigInteger shiftLeft(int n) {
        if (this.signum == 0) {
            return ZERO;
        }
        if (n == 0) {
            return this;
        }
        if (n < 0) {
            return this.shiftRight(-n);
        }
        int n2 = n >>> 5;
        int n3 = n & 0x1F;
        int n4 = this.mag.length;
        int[] nArray = null;
        if (n3 == 0) {
            nArray = new int[n4 + n2];
            for (int i = 0; i < n4; ++i) {
                nArray[i] = this.mag[i];
            }
        } else {
            int n5 = 0;
            int n6 = 32 - n3;
            int n7 = this.mag[0] >>> n6;
            if (n7 != 0) {
                nArray = new int[n4 + n2 + 1];
                nArray[n5++] = n7;
            } else {
                nArray = new int[n4 + n2];
            }
            int n8 = 0;
            while (n8 < n4 - 1) {
                nArray[n5++] = this.mag[n8++] << n3 | this.mag[n8] >>> n6;
            }
            nArray[n5] = this.mag[n8] << n3;
        }
        return new BigInteger(nArray, this.signum);
    }

    public BigInteger shiftRight(int n) {
        int n2;
        int n3;
        int n4;
        if (n == 0) {
            return this;
        }
        if (n < 0) {
            return this.shiftLeft(-n);
        }
        int n5 = n >>> 5;
        int n6 = n & 0x1F;
        int n7 = this.mag.length;
        int[] nArray = null;
        if (n5 >= n7) {
            return this.signum >= 0 ? ZERO : negConst[1];
        }
        if (n6 == 0) {
            n4 = n7 - n5;
            nArray = new int[n4];
            for (n3 = 0; n3 < n4; ++n3) {
                nArray[n3] = this.mag[n3];
            }
        } else {
            n4 = 0;
            n3 = this.mag[0] >>> n6;
            if (n3 != 0) {
                nArray = new int[n7 - n5];
                nArray[n4++] = n3;
            } else {
                nArray = new int[n7 - n5 - 1];
            }
            n2 = 32 - n6;
            int n8 = 0;
            while (n8 < n7 - n5 - 1) {
                nArray[n4++] = this.mag[n8++] << n2 | this.mag[n8] >>> n6;
            }
        }
        if (this.signum < 0) {
            n4 = 0;
            n2 = n7 - n5;
            for (n3 = n7 - 1; n3 >= n2 && n4 == 0; --n3) {
                n4 = this.mag[n3] != 0 ? 1 : 0;
            }
            if (n4 == 0 && n6 != 0) {
                int n9 = n4 = this.mag[n7 - n5 - 1] << 32 - n6 != 0 ? 1 : 0;
            }
            if (n4 != 0) {
                nArray = this.javaIncrement(nArray);
            }
        }
        return new BigInteger(nArray, this.signum);
    }

    int[] javaIncrement(int[] nArray) {
        boolean bl = false;
        int n = 0;
        int n2 = nArray.length - 1;
        while (n2 >= 0 && n == 0) {
            int n3 = n2--;
            int n4 = nArray[n3] + 1;
            nArray[n3] = n4;
            n = n4;
        }
        if (n == 0) {
            nArray = new int[nArray.length + 1];
            nArray[0] = 1;
        }
        return nArray;
    }

    public BigInteger and(BigInteger bigInteger) {
        int[] nArray = new int[Math.max(this.intLength(), bigInteger.intLength())];
        for (int i = 0; i < nArray.length; ++i) {
            nArray[i] = this.getInt(nArray.length - i - 1) & bigInteger.getInt(nArray.length - i - 1);
        }
        return BigInteger.valueOf(nArray);
    }

    public BigInteger or(BigInteger bigInteger) {
        int[] nArray = new int[Math.max(this.intLength(), bigInteger.intLength())];
        for (int i = 0; i < nArray.length; ++i) {
            nArray[i] = this.getInt(nArray.length - i - 1) | bigInteger.getInt(nArray.length - i - 1);
        }
        return BigInteger.valueOf(nArray);
    }

    public BigInteger xor(BigInteger bigInteger) {
        int[] nArray = new int[Math.max(this.intLength(), bigInteger.intLength())];
        for (int i = 0; i < nArray.length; ++i) {
            nArray[i] = this.getInt(nArray.length - i - 1) ^ bigInteger.getInt(nArray.length - i - 1);
        }
        return BigInteger.valueOf(nArray);
    }

    public BigInteger not() {
        int[] nArray = new int[this.intLength()];
        for (int i = 0; i < nArray.length; ++i) {
            nArray[i] = ~this.getInt(nArray.length - i - 1);
        }
        return BigInteger.valueOf(nArray);
    }

    public BigInteger andNot(BigInteger bigInteger) {
        int[] nArray = new int[Math.max(this.intLength(), bigInteger.intLength())];
        for (int i = 0; i < nArray.length; ++i) {
            nArray[i] = this.getInt(nArray.length - i - 1) & ~bigInteger.getInt(nArray.length - i - 1);
        }
        return BigInteger.valueOf(nArray);
    }

    public boolean testBit(int n) {
        if (n < 0) {
            throw new ArithmeticException("Negative bit address");
        }
        return (this.getInt(n / 32) & 1 << n % 32) != 0;
    }

    public BigInteger setBit(int n) {
        if (n < 0) {
            throw new ArithmeticException("Negative bit address");
        }
        int n2 = n / 32;
        int[] nArray = new int[Math.max(this.intLength(), n2 + 2)];
        for (int i = 0; i < nArray.length; ++i) {
            nArray[nArray.length - i - 1] = this.getInt(i);
        }
        int n3 = nArray.length - n2 - 1;
        nArray[n3] = nArray[n3] | 1 << n % 32;
        return BigInteger.valueOf(nArray);
    }

    public BigInteger clearBit(int n) {
        if (n < 0) {
            throw new ArithmeticException("Negative bit address");
        }
        int n2 = n / 32;
        int[] nArray = new int[Math.max(this.intLength(), (n + 1) / 32 + 1)];
        for (int i = 0; i < nArray.length; ++i) {
            nArray[nArray.length - i - 1] = this.getInt(i);
        }
        int n3 = nArray.length - n2 - 1;
        nArray[n3] = nArray[n3] & ~(1 << n % 32);
        return BigInteger.valueOf(nArray);
    }

    public BigInteger flipBit(int n) {
        if (n < 0) {
            throw new ArithmeticException("Negative bit address");
        }
        int n2 = n / 32;
        int[] nArray = new int[Math.max(this.intLength(), n2 + 2)];
        for (int i = 0; i < nArray.length; ++i) {
            nArray[nArray.length - i - 1] = this.getInt(i);
        }
        int n3 = nArray.length - n2 - 1;
        nArray[n3] = nArray[n3] ^ 1 << n % 32;
        return BigInteger.valueOf(nArray);
    }

    public int getLowestSetBit() {
        if (this.lowestSetBit == -2) {
            if (this.signum == 0) {
                this.lowestSetBit = -1;
            } else {
                int n;
                int n2 = 0;
                while ((n = this.getInt(n2)) == 0) {
                    ++n2;
                }
                this.lowestSetBit = (n2 << 5) + BigInteger.trailingZeroCnt(n);
            }
        }
        return this.lowestSetBit;
    }

    public int bitLength() {
        if (this.bitLength == -1) {
            if (this.signum == 0) {
                this.bitLength = 0;
            } else {
                int n = (this.mag.length - 1 << 5) + BigInteger.bitLen(this.mag[0]);
                if (this.signum < 0) {
                    boolean bl = BigInteger.bitCnt(this.mag[0]) == 1;
                    for (int i = 1; i < this.mag.length && bl; ++i) {
                        bl = this.mag[i] == 0;
                    }
                    this.bitLength = bl ? n - 1 : n;
                } else {
                    this.bitLength = n;
                }
            }
        }
        return this.bitLength;
    }

    static int bitLen(int n) {
        return n < 32768 ? (n < 128 ? (n < 8 ? (n < 2 ? (n < 1 ? (n < 0 ? 32 : 0) : 1) : (n < 4 ? 2 : 3)) : (n < 32 ? (n < 16 ? 4 : 5) : (n < 64 ? 6 : 7))) : (n < 2048 ? (n < 512 ? (n < 256 ? 8 : 9) : (n < 1024 ? 10 : 11)) : (n < 8192 ? (n < 4096 ? 12 : 13) : (n < 16384 ? 14 : 15)))) : (n < 0x800000 ? (n < 524288 ? (n < 131072 ? (n < 65536 ? 16 : 17) : (n < 262144 ? 18 : 19)) : (n < 0x200000 ? (n < 0x100000 ? 20 : 21) : (n < 0x400000 ? 22 : 23))) : (n < 0x8000000 ? (n < 0x2000000 ? (n < 0x1000000 ? 24 : 25) : (n < 0x4000000 ? 26 : 27)) : (n < 0x20000000 ? (n < 0x10000000 ? 28 : 29) : (n < 0x40000000 ? 30 : 31))));
    }

    public int bitCount() {
        if (this.bitCount == -1) {
            int n;
            int n2 = 0;
            for (n = 0; n < this.mag.length; ++n) {
                n2 += BigInteger.bitCnt(this.mag[n]);
            }
            if (this.signum < 0) {
                n = 0;
                int n3 = this.mag.length - 1;
                while (this.mag[n3] == 0) {
                    n += 32;
                    --n3;
                }
                this.bitCount = n2 + (n += BigInteger.trailingZeroCnt(this.mag[n3])) - 1;
            } else {
                this.bitCount = n2;
            }
        }
        return this.bitCount;
    }

    static int bitCnt(int n) {
        n -= (0xAAAAAAAA & n) >>> 1;
        n = (n & 0x33333333) + (n >>> 2 & 0x33333333);
        n = n + (n >>> 4) & 0xF0F0F0F;
        n += n >>> 8;
        n += n >>> 16;
        return n & 0xFF;
    }

    static int trailingZeroCnt(int n) {
        int n2 = n & 0xFF;
        if (n2 != 0) {
            return trailingZeroTable[n2];
        }
        n2 = n >>> 8 & 0xFF;
        if (n2 != 0) {
            return trailingZeroTable[n2] + 8;
        }
        n2 = n >>> 16 & 0xFF;
        if (n2 != 0) {
            return trailingZeroTable[n2] + 16;
        }
        n2 = n >>> 24 & 0xFF;
        return trailingZeroTable[n2] + 24;
    }

    public boolean isProbablePrime(int n) {
        if (n <= 0) {
            return true;
        }
        BigInteger bigInteger = this.abs();
        if (bigInteger.equals(TWO)) {
            return true;
        }
        if (!bigInteger.testBit(0) || bigInteger.equals(ONE)) {
            return false;
        }
        return bigInteger.primeToCertainty(n);
    }

    public int compareTo(BigInteger bigInteger) {
        return this.signum == bigInteger.signum ? this.signum * BigInteger.intArrayCmp(this.mag, bigInteger.mag) : (this.signum > bigInteger.signum ? 1 : -1);
    }

    public int compareTo(Object object) {
        return this.compareTo((BigInteger)object);
    }

    private static int intArrayCmp(int[] nArray, int[] nArray2) {
        if (nArray.length < nArray2.length) {
            return -1;
        }
        if (nArray.length > nArray2.length) {
            return 1;
        }
        for (int i = 0; i < nArray.length; ++i) {
            long l = (long)nArray[i] & 0xFFFFFFFFL;
            long l2 = (long)nArray2[i] & 0xFFFFFFFFL;
            if (l < l2) {
                return -1;
            }
            if (l <= l2) continue;
            return 1;
        }
        return 0;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof BigInteger)) {
            return false;
        }
        BigInteger bigInteger = (BigInteger)object;
        if (bigInteger.signum != this.signum || bigInteger.mag.length != this.mag.length) {
            return false;
        }
        for (int i = 0; i < this.mag.length; ++i) {
            if (bigInteger.mag[i] == this.mag[i]) continue;
            return false;
        }
        return true;
    }

    public BigInteger min(BigInteger bigInteger) {
        return this.compareTo(bigInteger) < 0 ? this : bigInteger;
    }

    public BigInteger max(BigInteger bigInteger) {
        return this.compareTo(bigInteger) > 0 ? this : bigInteger;
    }

    public int hashCode() {
        int n = 0;
        for (int i = 0; i < this.mag.length; ++i) {
            n = (int)((long)(31 * n) + ((long)this.mag[i] & 0xFFFFFFFFL));
        }
        return n * this.signum;
    }

    public String toString(int n) {
        Comparable comparable;
        if (this.signum == 0) {
            return "0";
        }
        if (n < 2 || n > 36) {
            n = 10;
        }
        int n2 = (4 * this.mag.length + 6) / 7;
        String[] stringArray = new String[n2];
        BigInteger bigInteger = this.abs();
        int n3 = 0;
        while (bigInteger.signum != 0) {
            comparable = longRadix[n];
            MutableBigInteger mutableBigInteger = new MutableBigInteger();
            MutableBigInteger mutableBigInteger2 = new MutableBigInteger();
            MutableBigInteger mutableBigInteger3 = new MutableBigInteger(bigInteger.mag);
            MutableBigInteger mutableBigInteger4 = new MutableBigInteger(((BigInteger)comparable).mag);
            mutableBigInteger3.divide(mutableBigInteger4, mutableBigInteger, mutableBigInteger2);
            BigInteger bigInteger2 = new BigInteger(mutableBigInteger, bigInteger.signum * ((BigInteger)comparable).signum);
            BigInteger bigInteger3 = new BigInteger(mutableBigInteger2, bigInteger.signum * ((BigInteger)comparable).signum);
            stringArray[n3++] = Long.toString(bigInteger3.longValue(), n);
            bigInteger = bigInteger2;
        }
        comparable = new StringBuffer(n3 * digitsPerLong[n] + 1);
        if (this.signum < 0) {
            ((StringBuffer)comparable).append('-');
        }
        ((StringBuffer)comparable).append(stringArray[n3 - 1]);
        for (int i = n3 - 2; i >= 0; --i) {
            int n4 = digitsPerLong[n] - stringArray[i].length();
            if (n4 != 0) {
                ((StringBuffer)comparable).append(zeros[n4]);
            }
            ((StringBuffer)comparable).append(stringArray[i]);
        }
        return ((StringBuffer)comparable).toString();
    }

    public String toString() {
        return this.toString(10);
    }

    public byte[] toByteArray() {
        int n = this.bitLength() / 8 + 1;
        byte[] byArray = new byte[n];
        int n2 = 4;
        int n3 = 0;
        int n4 = 0;
        for (int i = n - 1; i >= 0; --i) {
            if (n2 == 4) {
                n3 = this.getInt(n4++);
                n2 = 1;
            } else {
                n3 >>>= 8;
                ++n2;
            }
            byArray[i] = (byte)n3;
        }
        return byArray;
    }

    public int intValue() {
        int n = 0;
        n = this.getInt(0);
        return n;
    }

    public long longValue() {
        long l = 0L;
        for (int i = 1; i >= 0; --i) {
            l = (l << 32) + ((long)this.getInt(i) & 0xFFFFFFFFL);
        }
        return l;
    }

    public float floatValue() {
        return Float.valueOf(this.toString()).floatValue();
    }

    public double doubleValue() {
        return Double.valueOf(this.toString());
    }

    private static int[] stripLeadingZeroInts(int[] nArray) {
        int n;
        int n2 = nArray.length;
        for (n = 0; n < nArray.length && nArray[n] == 0; ++n) {
        }
        int[] nArray2 = new int[nArray.length - n];
        for (int i = 0; i < nArray.length - n; ++i) {
            nArray2[i] = nArray[n + i];
        }
        return nArray2;
    }

    private static int[] trustedStripLeadingZeroInts(int[] nArray) {
        int n;
        int n2 = nArray.length;
        for (n = 0; n < nArray.length && nArray[n] == 0; ++n) {
        }
        if (n > 0) {
            int[] nArray2 = new int[nArray.length - n];
            for (int i = 0; i < nArray.length - n; ++i) {
                nArray2[i] = nArray[n + i];
            }
            return nArray2;
        }
        return nArray;
    }

    private static int[] stripLeadingZeroBytes(byte[] byArray) {
        int n;
        int n2 = byArray.length;
        for (n = 0; n < byArray.length && byArray[n] == 0; ++n) {
        }
        int n3 = (n2 - n + 3) / 4;
        int[] nArray = new int[n3];
        int n4 = n2 - 1;
        for (int i = n3 - 1; i >= 0; --i) {
            nArray[i] = byArray[n4--] & 0xFF;
            int n5 = n4 - n + 1;
            int n6 = Math.min(3, n5);
            for (int j = 8; j <= 8 * n6; j += 8) {
                int n7 = i;
                nArray[n7] = nArray[n7] | (byArray[n4--] & 0xFF) << j;
            }
        }
        return nArray;
    }

    private static int[] makePositive(byte[] byArray) {
        int n;
        int n2;
        int n3;
        int n4 = byArray.length;
        for (n3 = 0; n3 < n4 && byArray[n3] == -1; ++n3) {
        }
        for (n2 = n3; n2 < n4 && byArray[n2] == 0; ++n2) {
        }
        int n5 = n2 == n4 ? 1 : 0;
        int n6 = (n4 - n3 + n5 + 3) / 4;
        int[] nArray = new int[n6];
        int n7 = n4 - 1;
        for (n = n6 - 1; n >= 0; --n) {
            int n8;
            nArray[n] = byArray[n7--] & 0xFF;
            int n9 = Math.min(3, n7 - n3 + 1);
            if (n9 < 0) {
                n9 = 0;
            }
            for (n8 = 8; n8 <= 8 * n9; n8 += 8) {
                int n10 = n;
                nArray[n10] = nArray[n10] | (byArray[n7--] & 0xFF) << n8;
            }
            n8 = -1 >>> 8 * (3 - n9);
            nArray[n] = ~nArray[n] & n8;
        }
        for (n = nArray.length - 1; n >= 0; --n) {
            nArray[n] = (int)(((long)nArray[n] & 0xFFFFFFFFL) + 1L);
            if (nArray[n] != 0) break;
        }
        return nArray;
    }

    private static int[] makePositive(int[] nArray) {
        int n;
        int n2;
        int n3;
        int n4;
        for (n4 = 0; n4 < nArray.length && nArray[n4] == -1; ++n4) {
        }
        for (n3 = n4; n3 < nArray.length && nArray[n3] == 0; ++n3) {
        }
        int n5 = n3 == nArray.length ? 1 : 0;
        int[] nArray2 = new int[nArray.length - n4 + n5];
        for (n2 = n4; n2 < nArray.length; ++n2) {
            nArray2[n2 - n4 + n5] = ~nArray[n2];
        }
        n2 = nArray2.length - 1;
        do {
            n = n2--;
        } while ((nArray2[n] = nArray2[n] + 1) == 0);
        return nArray2;
    }

    private int intLength() {
        return this.bitLength() / 32 + 1;
    }

    private int signBit() {
        return this.signum < 0 ? 1 : 0;
    }

    private int signInt() {
        return this.signum < 0 ? -1 : 0;
    }

    private int getInt(int n) {
        if (n < 0) {
            return 0;
        }
        if (n >= this.mag.length) {
            return this.signInt();
        }
        int n2 = this.mag[this.mag.length - n - 1];
        return this.signum >= 0 ? n2 : (n <= this.firstNonzeroIntNum() ? -n2 : ~n2);
    }

    private int firstNonzeroIntNum() {
        if (this.firstNonzeroIntNum == -2) {
            int n;
            for (n = this.mag.length - 1; n >= 0 && this.mag[n] == 0; --n) {
            }
            this.firstNonzeroIntNum = this.mag.length - n - 1;
        }
        return this.firstNonzeroIntNum;
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField getField = objectInputStream.readFields();
        this.signum = getField.get("signum", -2);
        byte[] byArray = (byte[])getField.get("magnitude", null);
        if (this.signum < -1 || this.signum > 1) {
            String string = "BigInteger: Invalid signum value";
            if (getField.defaulted("signum")) {
                string = "BigInteger: Signum not present in stream";
            }
            throw new StreamCorruptedException(string);
        }
        if (byArray.length == 0 != (this.signum == 0)) {
            String string = "BigInteger: signum-magnitude mismatch";
            if (getField.defaulted("magnitude")) {
                string = "BigInteger: Magnitude not present in stream";
            }
            throw new StreamCorruptedException(string);
        }
        this.bitLength = -1;
        this.bitCount = -1;
        this.lowestSetBit = -2;
        this.mag = BigInteger.stripLeadingZeroBytes(byArray);
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        ObjectOutputStream.PutField putField = objectOutputStream.putFields();
        putField.put("signum", this.signum);
        putField.put("magnitude", this.magSerializedForm());
        putField.put("bitCount", -1);
        putField.put("bitLength", -1);
        putField.put("lowestSetBit", -2);
        putField.put("firstNonzeroByteNum", -2);
        objectOutputStream.writeFields();
    }

    private byte[] magSerializedForm() {
        int n = this.mag.length == 0 ? 0 : (this.mag.length - 1 << 5) + BigInteger.bitLen(this.mag[0]);
        int n2 = (n + 7) / 8;
        byte[] byArray = new byte[n2];
        int n3 = 4;
        int n4 = this.mag.length - 1;
        int n5 = 0;
        for (int i = n2 - 1; i >= 0; --i) {
            if (n3 == 4) {
                n5 = this.mag[n4--];
                n3 = 1;
            } else {
                n5 >>>= 8;
                ++n3;
            }
            byArray[i] = (byte)n5;
        }
        return byArray;
    }

    static {
        int n;
        bitsPerDigit = new long[]{0L, 0L, 1024L, 1624L, 2048L, 2378L, 2648L, 2875L, 3072L, 3247L, 3402L, 3543L, 3672L, 3790L, 3899L, 4001L, 4096L, 4186L, 4271L, 4350L, 4426L, 4498L, 4567L, 4633L, 4696L, 4756L, 4814L, 4870L, 4923L, 4975L, 5025L, 5074L, 5120L, 5166L, 5210L, 5253L, 5295L};
        SMALL_PRIME_PRODUCT = BigInteger.valueOf(152125131763605L);
        posConst = new BigInteger[17];
        negConst = new BigInteger[17];
        for (n = 1; n <= 16; ++n) {
            int[] nArray = new int[]{n};
            BigInteger.posConst[n] = new BigInteger(nArray, 1);
            BigInteger.negConst[n] = new BigInteger(nArray, -1);
        }
        ZERO = new BigInteger(new int[0], 0);
        ONE = BigInteger.valueOf(1L);
        TWO = BigInteger.valueOf(2L);
        bnExpModThreshTable = new int[]{7, 25, 81, 241, 673, 1793, Integer.MAX_VALUE};
        trailingZeroTable = new byte[]{-25, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 7, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0};
        zeros = new String[64];
        BigInteger.zeros[63] = "000000000000000000000000000000000000000000000000000000000000000";
        for (n = 0; n < 63; ++n) {
            BigInteger.zeros[n] = zeros[63].substring(0, n);
        }
        digitsPerLong = new int[]{0, 0, 62, 39, 31, 27, 24, 22, 20, 19, 18, 18, 17, 17, 16, 16, 15, 15, 15, 14, 14, 14, 14, 13, 13, 13, 13, 13, 13, 12, 12, 12, 12, 12, 12, 12, 12};
        longRadix = new BigInteger[]{null, null, BigInteger.valueOf(0x4000000000000000L), BigInteger.valueOf(4052555153018976267L), BigInteger.valueOf(0x4000000000000000L), BigInteger.valueOf(7450580596923828125L), BigInteger.valueOf(4738381338321616896L), BigInteger.valueOf(3909821048582988049L), BigInteger.valueOf(0x1000000000000000L), BigInteger.valueOf(1350851717672992089L), BigInteger.valueOf(1000000000000000000L), BigInteger.valueOf(5559917313492231481L), BigInteger.valueOf(2218611106740436992L), BigInteger.valueOf(8650415919381337933L), BigInteger.valueOf(2177953337809371136L), BigInteger.valueOf(6568408355712890625L), BigInteger.valueOf(0x1000000000000000L), BigInteger.valueOf(2862423051509815793L), BigInteger.valueOf(6746640616477458432L), BigInteger.valueOf(799006685782884121L), BigInteger.valueOf(1638400000000000000L), BigInteger.valueOf(3243919932521508681L), BigInteger.valueOf(6221821273427820544L), BigInteger.valueOf(504036361936467383L), BigInteger.valueOf(876488338465357824L), BigInteger.valueOf(1490116119384765625L), BigInteger.valueOf(2481152873203736576L), BigInteger.valueOf(4052555153018976267L), BigInteger.valueOf(6502111422497947648L), BigInteger.valueOf(353814783205469041L), BigInteger.valueOf(531441000000000000L), BigInteger.valueOf(787662783788549761L), BigInteger.valueOf(0x1000000000000000L), BigInteger.valueOf(1667889514952984961L), BigInteger.valueOf(2386420683693101056L), BigInteger.valueOf(3379220508056640625L), BigInteger.valueOf(4738381338321616896L)};
        digitsPerInt = new int[]{0, 0, 30, 19, 15, 13, 11, 11, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5};
        intRadix = new int[]{0, 0, 0x40000000, 1162261467, 0x40000000, 1220703125, 362797056, 1977326743, 0x40000000, 387420489, 1000000000, 214358881, 429981696, 815730721, 1475789056, 170859375, 0x10000000, 410338673, 612220032, 893871739, 1280000000, 1801088541, 113379904, 148035889, 191102976, 244140625, 308915776, 387420489, 481890304, 594823321, 729000000, 887503681, 0x40000000, 1291467969, 1544804416, 1838265625, 60466176};
        serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("signum", Integer.TYPE), new ObjectStreamField("magnitude", byte[].class), new ObjectStreamField("bitCount", Integer.TYPE), new ObjectStreamField("bitLength", Integer.TYPE), new ObjectStreamField("firstNonzeroByteNum", Integer.TYPE), new ObjectStreamField("lowestSetBit", Integer.TYPE)};
    }
}

