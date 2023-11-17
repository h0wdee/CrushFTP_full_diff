/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal.util;

import com.didisoft.pgp.bc.elgamal.util.BigInteger;
import com.didisoft.pgp.bc.elgamal.util.SignedMutableBigInteger;

class MutableBigInteger {
    int[] value;
    int intLen;
    int offset = 0;
    private static final long LONG_MASK = 0xFFFFFFFFL;

    MutableBigInteger() {
        this.value = new int[1];
        this.intLen = 0;
    }

    MutableBigInteger(int n) {
        this.value = new int[1];
        this.intLen = 1;
        this.value[0] = n;
    }

    MutableBigInteger(int[] nArray, int n) {
        this.value = nArray;
        this.intLen = n;
    }

    MutableBigInteger(int[] nArray) {
        this.value = nArray;
        this.intLen = nArray.length;
    }

    MutableBigInteger(BigInteger bigInteger) {
        this.value = (int[])bigInteger.mag.clone();
        this.intLen = this.value.length;
    }

    MutableBigInteger(MutableBigInteger mutableBigInteger) {
        this.intLen = mutableBigInteger.intLen;
        this.value = new int[this.intLen];
        for (int i = 0; i < this.intLen; ++i) {
            this.value[i] = mutableBigInteger.value[mutableBigInteger.offset + i];
        }
    }

    void clear() {
        this.intLen = 0;
        this.offset = 0;
        int n = this.value.length;
        for (int i = 0; i < n; ++i) {
            this.value[i] = 0;
        }
    }

    void reset() {
        this.intLen = 0;
        this.offset = 0;
    }

    final int compare(MutableBigInteger mutableBigInteger) {
        if (this.intLen < mutableBigInteger.intLen) {
            return -1;
        }
        if (this.intLen > mutableBigInteger.intLen) {
            return 1;
        }
        for (int i = 0; i < this.intLen; ++i) {
            int n = this.value[this.offset + i] + Integer.MIN_VALUE;
            int n2 = mutableBigInteger.value[mutableBigInteger.offset + i] + Integer.MIN_VALUE;
            if (n < n2) {
                return -1;
            }
            if (n <= n2) continue;
            return 1;
        }
        return 0;
    }

    private final int getLowestSetBit() {
        int n;
        if (this.intLen == 0) {
            return -1;
        }
        for (n = this.intLen - 1; n > 0 && this.value[n + this.offset] == 0; --n) {
        }
        int n2 = this.value[n + this.offset];
        if (n2 == 0) {
            return -1;
        }
        return (this.intLen - 1 - n << 5) + BigInteger.trailingZeroCnt(n2);
    }

    private final int getInt(int n) {
        return this.value[this.offset + n];
    }

    private final long getLong(int n) {
        return (long)this.value[this.offset + n] & 0xFFFFFFFFL;
    }

    final void normalize() {
        if (this.intLen == 0) {
            this.offset = 0;
            return;
        }
        int n = this.offset;
        if (this.value[n] != 0) {
            return;
        }
        int n2 = n + this.intLen;
        while (++n < n2 && this.value[n] == 0) {
        }
        int n3 = n - this.offset;
        this.intLen -= n3;
        this.offset = this.intLen == 0 ? 0 : this.offset + n3;
    }

    private final void ensureCapacity(int n) {
        if (this.value.length < n) {
            this.value = new int[n];
            this.offset = 0;
            this.intLen = n;
        }
    }

    int[] toIntArray() {
        int[] nArray = new int[this.intLen];
        for (int i = 0; i < this.intLen; ++i) {
            nArray[i] = this.value[this.offset + i];
        }
        return nArray;
    }

    void setInt(int n, int n2) {
        this.value[this.offset + n] = n2;
    }

    void setValue(int[] nArray, int n) {
        this.value = nArray;
        this.intLen = n;
        this.offset = 0;
    }

    void copyValue(MutableBigInteger mutableBigInteger) {
        int n = mutableBigInteger.intLen;
        if (this.value.length < n) {
            this.value = new int[n];
        }
        for (int i = 0; i < n; ++i) {
            this.value[i] = mutableBigInteger.value[mutableBigInteger.offset + i];
        }
        this.intLen = n;
        this.offset = 0;
    }

    void copyValue(int[] nArray) {
        int n = nArray.length;
        if (this.value.length < n) {
            this.value = new int[n];
        }
        for (int i = 0; i < n; ++i) {
            this.value[i] = nArray[i];
        }
        this.intLen = n;
        this.offset = 0;
    }

    boolean isOne() {
        return this.intLen == 1 && this.value[this.offset] == 1;
    }

    boolean isZero() {
        return this.intLen == 0;
    }

    boolean isEven() {
        return this.intLen == 0 || (this.value[this.offset + this.intLen - 1] & 1) == 0;
    }

    boolean isOdd() {
        return (this.value[this.offset + this.intLen - 1] & 1) == 1;
    }

    boolean isNormal() {
        if (this.intLen + this.offset > this.value.length) {
            return false;
        }
        if (this.intLen == 0) {
            return true;
        }
        return this.value[this.offset] != 0;
    }

    public String toString() {
        BigInteger bigInteger = new BigInteger(this, 1);
        return bigInteger.toString();
    }

    void rightShift(int n) {
        if (this.intLen == 0) {
            return;
        }
        int n2 = n >>> 5;
        int n3 = n & 0x1F;
        this.intLen -= n2;
        if (n3 == 0) {
            return;
        }
        int n4 = BigInteger.bitLen(this.value[this.offset]);
        if (n3 >= n4) {
            this.primitiveLeftShift(32 - n3);
            --this.intLen;
        } else {
            this.primitiveRightShift(n3);
        }
    }

    void leftShift(int n) {
        if (this.intLen == 0) {
            return;
        }
        int n2 = n >>> 5;
        int n3 = n & 0x1F;
        int n4 = BigInteger.bitLen(this.value[this.offset]);
        if (n <= 32 - n4) {
            this.primitiveLeftShift(n3);
            return;
        }
        int n5 = this.intLen + n2 + 1;
        if (n3 <= 32 - n4) {
            --n5;
        }
        if (this.value.length < n5) {
            int[] nArray = new int[n5];
            for (int i = 0; i < this.intLen; ++i) {
                nArray[i] = this.value[this.offset + i];
            }
            this.setValue(nArray, n5);
        } else if (this.value.length - this.offset >= n5) {
            for (int i = 0; i < n5 - this.intLen; ++i) {
                this.value[this.offset + this.intLen + i] = 0;
            }
        } else {
            int n6;
            for (n6 = 0; n6 < this.intLen; ++n6) {
                this.value[n6] = this.value[this.offset + n6];
            }
            for (n6 = this.intLen; n6 < n5; ++n6) {
                this.value[n6] = 0;
            }
            this.offset = 0;
        }
        this.intLen = n5;
        if (n3 == 0) {
            return;
        }
        if (n3 <= 32 - n4) {
            this.primitiveLeftShift(n3);
        } else {
            this.primitiveRightShift(32 - n3);
        }
    }

    private int divadd(int[] nArray, int[] nArray2, int n) {
        long l = 0L;
        for (int i = nArray.length - 1; i >= 0; --i) {
            long l2 = ((long)nArray[i] & 0xFFFFFFFFL) + ((long)nArray2[i + n] & 0xFFFFFFFFL) + l;
            nArray2[i + n] = (int)l2;
            l = l2 >>> 32;
        }
        return (int)l;
    }

    private int mulsub(int[] nArray, int[] nArray2, int n, int n2, int n3) {
        long l = (long)n & 0xFFFFFFFFL;
        long l2 = 0L;
        n3 += n2;
        for (int i = n2 - 1; i >= 0; --i) {
            long l3 = ((long)nArray2[i] & 0xFFFFFFFFL) * l + l2;
            long l4 = (long)nArray[n3] - l3;
            nArray[n3--] = (int)l4;
            l2 = (l3 >>> 32) + (long)((l4 & 0xFFFFFFFFL) > ((long)(~((int)l3)) & 0xFFFFFFFFL) ? 1 : 0);
        }
        return (int)l2;
    }

    private final void primitiveRightShift(int n) {
        int n2;
        int[] nArray = this.value;
        int n3 = 32 - n;
        int n4 = nArray[n2];
        for (n2 = this.offset + this.intLen - 1; n2 > this.offset; --n2) {
            int n5 = n4;
            n4 = nArray[n2 - 1];
            nArray[n2] = n4 << n3 | n5 >>> n;
        }
        int n6 = this.offset;
        nArray[n6] = nArray[n6] >>> n;
    }

    private final void primitiveLeftShift(int n) {
        int n2;
        int[] nArray = this.value;
        int n3 = 32 - n;
        int n4 = nArray[n2];
        int n5 = n2 + this.intLen - 1;
        for (n2 = this.offset; n2 < n5; ++n2) {
            int n6 = n4;
            n4 = nArray[n2 + 1];
            nArray[n2] = n6 << n | n4 >>> n3;
        }
        int n7 = this.offset + this.intLen - 1;
        nArray[n7] = nArray[n7] << n;
    }

    void add(MutableBigInteger mutableBigInteger) {
        int n = this.intLen;
        int n2 = mutableBigInteger.intLen;
        int n3 = this.intLen > mutableBigInteger.intLen ? this.intLen : mutableBigInteger.intLen;
        int[] nArray = this.value.length < n3 ? new int[n3] : this.value;
        int n4 = nArray.length - 1;
        long l = 0L;
        while (n > 0 && n2 > 0) {
            l = ((long)this.value[--n + this.offset] & 0xFFFFFFFFL) + ((long)mutableBigInteger.value[--n2 + mutableBigInteger.offset] & 0xFFFFFFFFL) + (l >>> 32);
            nArray[n4--] = (int)l;
        }
        while (n > 0) {
            l = ((long)this.value[--n + this.offset] & 0xFFFFFFFFL) + (l >>> 32);
            nArray[n4--] = (int)l;
        }
        while (n2 > 0) {
            l = ((long)mutableBigInteger.value[--n2 + mutableBigInteger.offset] & 0xFFFFFFFFL) + (l >>> 32);
            nArray[n4--] = (int)l;
        }
        if (l >>> 32 > 0L) {
            if (nArray.length < ++n3) {
                int[] nArray2 = new int[n3];
                for (int i = n3 - 1; i > 0; --i) {
                    nArray2[i] = nArray[i - 1];
                }
                nArray2[0] = 1;
                nArray = nArray2;
            } else {
                nArray[n4--] = 1;
            }
        }
        this.value = nArray;
        this.intLen = n3;
        this.offset = nArray.length - n3;
    }

    int subtract(MutableBigInteger mutableBigInteger) {
        int n;
        MutableBigInteger mutableBigInteger2 = this;
        int[] nArray = this.value;
        int n2 = mutableBigInteger2.compare(mutableBigInteger);
        if (n2 == 0) {
            this.reset();
            return 0;
        }
        if (n2 < 0) {
            MutableBigInteger mutableBigInteger3 = mutableBigInteger2;
            mutableBigInteger2 = mutableBigInteger;
            mutableBigInteger = mutableBigInteger3;
        }
        if (nArray.length < (n = mutableBigInteger2.intLen)) {
            nArray = new int[n];
        }
        long l = 0L;
        int n3 = mutableBigInteger2.intLen;
        int n4 = mutableBigInteger.intLen;
        int n5 = nArray.length - 1;
        while (n4 > 0) {
            l = ((long)mutableBigInteger2.value[--n3 + mutableBigInteger2.offset] & 0xFFFFFFFFL) - ((long)mutableBigInteger.value[--n4 + mutableBigInteger.offset] & 0xFFFFFFFFL) - (long)((int)(-(l >> 32)));
            nArray[n5--] = (int)l;
        }
        while (n3 > 0) {
            l = ((long)mutableBigInteger2.value[--n3 + mutableBigInteger2.offset] & 0xFFFFFFFFL) - (long)((int)(-(l >> 32)));
            nArray[n5--] = (int)l;
        }
        this.value = nArray;
        this.intLen = n;
        this.offset = this.value.length - n;
        this.normalize();
        return n2;
    }

    private int difference(MutableBigInteger mutableBigInteger) {
        MutableBigInteger mutableBigInteger2 = this;
        int n = mutableBigInteger2.compare(mutableBigInteger);
        if (n == 0) {
            return 0;
        }
        if (n < 0) {
            MutableBigInteger mutableBigInteger3 = mutableBigInteger2;
            mutableBigInteger2 = mutableBigInteger;
            mutableBigInteger = mutableBigInteger3;
        }
        long l = 0L;
        int n2 = mutableBigInteger2.intLen;
        int n3 = mutableBigInteger.intLen;
        while (n3 > 0) {
            l = ((long)mutableBigInteger2.value[mutableBigInteger2.offset + --n2] & 0xFFFFFFFFL) - ((long)mutableBigInteger.value[mutableBigInteger.offset + --n3] & 0xFFFFFFFFL) - (long)((int)(-(l >> 32)));
            mutableBigInteger2.value[mutableBigInteger2.offset + n2] = (int)l;
        }
        while (n2 > 0) {
            l = ((long)mutableBigInteger2.value[mutableBigInteger2.offset + --n2] & 0xFFFFFFFFL) - (long)((int)(-(l >> 32)));
            mutableBigInteger2.value[mutableBigInteger2.offset + n2] = (int)l;
        }
        mutableBigInteger2.normalize();
        return n;
    }

    void multiply(MutableBigInteger mutableBigInteger, MutableBigInteger mutableBigInteger2) {
        int n = this.intLen;
        int n2 = mutableBigInteger.intLen;
        int n3 = n + n2;
        if (mutableBigInteger2.value.length < n3) {
            mutableBigInteger2.value = new int[n3];
        }
        mutableBigInteger2.offset = 0;
        mutableBigInteger2.intLen = n3;
        long l = 0L;
        int n4 = n2 - 1;
        int n5 = n2 + n - 1;
        while (n4 >= 0) {
            long l2 = ((long)mutableBigInteger.value[n4 + mutableBigInteger.offset] & 0xFFFFFFFFL) * ((long)this.value[n - 1 + this.offset] & 0xFFFFFFFFL) + l;
            mutableBigInteger2.value[n5] = (int)l2;
            l = l2 >>> 32;
            --n4;
            --n5;
        }
        mutableBigInteger2.value[n - 1] = (int)l;
        for (n4 = n - 2; n4 >= 0; --n4) {
            l = 0L;
            n5 = n2 - 1;
            int n6 = n2 + n4;
            while (n5 >= 0) {
                long l3 = ((long)mutableBigInteger.value[n5 + mutableBigInteger.offset] & 0xFFFFFFFFL) * ((long)this.value[n4 + this.offset] & 0xFFFFFFFFL) + ((long)mutableBigInteger2.value[n6] & 0xFFFFFFFFL) + l;
                mutableBigInteger2.value[n6] = (int)l3;
                l = l3 >>> 32;
                --n5;
                --n6;
            }
            mutableBigInteger2.value[n4] = (int)l;
        }
        mutableBigInteger2.normalize();
    }

    void mul(int n, MutableBigInteger mutableBigInteger) {
        if (n == 1) {
            mutableBigInteger.copyValue(this);
            return;
        }
        if (n == 0) {
            mutableBigInteger.clear();
            return;
        }
        long l = (long)n & 0xFFFFFFFFL;
        int[] nArray = mutableBigInteger.value.length < this.intLen + 1 ? new int[this.intLen + 1] : mutableBigInteger.value;
        long l2 = 0L;
        for (int i = this.intLen - 1; i >= 0; --i) {
            long l3 = l * ((long)this.value[i + this.offset] & 0xFFFFFFFFL) + l2;
            nArray[i + 1] = (int)l3;
            l2 = l3 >>> 32;
        }
        if (l2 == 0L) {
            mutableBigInteger.offset = 1;
            mutableBigInteger.intLen = this.intLen;
        } else {
            mutableBigInteger.offset = 0;
            mutableBigInteger.intLen = this.intLen + 1;
            nArray[0] = (int)l2;
        }
        mutableBigInteger.value = nArray;
    }

    void divideOneWord(int n, MutableBigInteger mutableBigInteger) {
        long l = (long)n & 0xFFFFFFFFL;
        if (this.intLen == 1) {
            long l2 = (long)this.value[this.offset] & 0xFFFFFFFFL;
            mutableBigInteger.value[0] = (int)(l2 / l);
            mutableBigInteger.intLen = mutableBigInteger.value[0] == 0 ? 0 : 1;
            mutableBigInteger.offset = 0;
            this.value[0] = (int)(l2 - (long)mutableBigInteger.value[0] * l);
            this.offset = 0;
            this.intLen = this.value[0] == 0 ? 0 : 1;
            return;
        }
        if (mutableBigInteger.value.length < this.intLen) {
            mutableBigInteger.value = new int[this.intLen];
        }
        mutableBigInteger.offset = 0;
        mutableBigInteger.intLen = this.intLen;
        int n2 = 32 - BigInteger.bitLen(n);
        int n3 = this.value[this.offset];
        long l3 = (long)n3 & 0xFFFFFFFFL;
        if (l3 < l) {
            mutableBigInteger.value[0] = 0;
        } else {
            mutableBigInteger.value[0] = (int)(l3 / l);
            n3 = (int)(l3 - (long)mutableBigInteger.value[0] * l);
            l3 = (long)n3 & 0xFFFFFFFFL;
        }
        int n4 = this.intLen;
        int[] nArray = new int[2];
        while (--n4 > 0) {
            long l4 = l3 << 32 | (long)this.value[this.offset + this.intLen - n4] & 0xFFFFFFFFL;
            if (l4 >= 0L) {
                nArray[0] = (int)(l4 / l);
                nArray[1] = (int)(l4 - (long)nArray[0] * l);
            } else {
                this.divWord(nArray, l4, n);
            }
            mutableBigInteger.value[this.intLen - n4] = nArray[0];
            n3 = nArray[1];
            l3 = (long)n3 & 0xFFFFFFFFL;
        }
        this.value[0] = n2 > 0 ? (n3 %= n) : n3;
        this.intLen = this.value[0] == 0 ? 0 : 1;
        mutableBigInteger.normalize();
    }

    void divide(MutableBigInteger mutableBigInteger, MutableBigInteger mutableBigInteger2, MutableBigInteger mutableBigInteger3) {
        int n;
        int n2;
        if (mutableBigInteger.intLen == 0) {
            throw new ArithmeticException("BigInteger divide by zero");
        }
        if (this.intLen == 0) {
            mutableBigInteger3.offset = 0;
            mutableBigInteger3.intLen = 0;
            mutableBigInteger2.offset = 0;
            mutableBigInteger2.intLen = 0;
            return;
        }
        int n3 = this.compare(mutableBigInteger);
        if (n3 < 0) {
            mutableBigInteger2.offset = 0;
            mutableBigInteger2.intLen = 0;
            mutableBigInteger3.copyValue(this);
            return;
        }
        if (n3 == 0) {
            mutableBigInteger2.intLen = 1;
            mutableBigInteger2.value[0] = 1;
            mutableBigInteger3.offset = 0;
            mutableBigInteger3.intLen = 0;
            mutableBigInteger2.offset = 0;
            return;
        }
        mutableBigInteger2.clear();
        if (mutableBigInteger.intLen == 1) {
            mutableBigInteger3.copyValue(this);
            mutableBigInteger3.divideOneWord(mutableBigInteger.value[mutableBigInteger.offset], mutableBigInteger2);
            return;
        }
        int[] nArray = new int[mutableBigInteger.intLen];
        for (n2 = 0; n2 < mutableBigInteger.intLen; ++n2) {
            nArray[n2] = mutableBigInteger.value[mutableBigInteger.offset + n2];
        }
        n2 = mutableBigInteger.intLen;
        if (mutableBigInteger3.value.length < this.intLen + 1) {
            mutableBigInteger3.value = new int[this.intLen + 1];
        }
        for (n = 0; n < this.intLen; ++n) {
            mutableBigInteger3.value[n + 1] = this.value[n + this.offset];
        }
        mutableBigInteger3.intLen = this.intLen;
        mutableBigInteger3.offset = 1;
        n = mutableBigInteger3.intLen;
        int n4 = n - n2 + 1;
        if (mutableBigInteger2.value.length < n4) {
            mutableBigInteger2.value = new int[n4];
            mutableBigInteger2.offset = 0;
        }
        mutableBigInteger2.intLen = n4;
        int[] nArray2 = mutableBigInteger2.value;
        int n5 = 32 - BigInteger.bitLen(nArray[0]);
        if (n5 > 0) {
            BigInteger.primitiveLeftShift(nArray, n2, n5);
            mutableBigInteger3.leftShift(n5);
        }
        if (mutableBigInteger3.intLen == n) {
            mutableBigInteger3.offset = 0;
            mutableBigInteger3.value[0] = 0;
            ++mutableBigInteger3.intLen;
        }
        int n6 = nArray[0];
        long l = (long)n6 & 0xFFFFFFFFL;
        int n7 = nArray[1];
        int[] nArray3 = new int[2];
        for (int i = 0; i < n4; ++i) {
            long l2;
            long l3;
            long l4;
            int n8 = 0;
            int n9 = 0;
            boolean bl = false;
            int n10 = mutableBigInteger3.value[i + mutableBigInteger3.offset];
            int n11 = n10 + Integer.MIN_VALUE;
            int n12 = mutableBigInteger3.value[i + 1 + mutableBigInteger3.offset];
            if (n10 == n6) {
                n8 = -1;
                n9 = n10 + n12;
                bl = n9 + Integer.MIN_VALUE < n11;
            } else {
                l4 = (long)n10 << 32 | (long)n12 & 0xFFFFFFFFL;
                if (l4 >= 0L) {
                    n8 = (int)(l4 / l);
                    n9 = (int)(l4 - (long)n8 * l);
                } else {
                    this.divWord(nArray3, l4, n6);
                    n8 = nArray3[0];
                    n9 = nArray3[1];
                }
            }
            if (n8 == 0) continue;
            if (!bl && this.unsignedLongCompare(l3 = ((long)n7 & 0xFFFFFFFFL) * ((long)n8 & 0xFFFFFFFFL), l2 = ((long)n9 & 0xFFFFFFFFL) << 32 | (l4 = (long)mutableBigInteger3.value[i + 2 + mutableBigInteger3.offset] & 0xFFFFFFFFL)) && ((long)(n9 = (int)(((long)n9 & 0xFFFFFFFFL) + l)) & 0xFFFFFFFFL) >= l && this.unsignedLongCompare(l3 = ((long)n7 & 0xFFFFFFFFL) * ((long)(--n8) & 0xFFFFFFFFL), l2 = ((long)n9 & 0xFFFFFFFFL) << 32 | l4)) {
                --n8;
            }
            mutableBigInteger3.value[i + mutableBigInteger3.offset] = 0;
            int n13 = this.mulsub(mutableBigInteger3.value, nArray, n8, n2, i + mutableBigInteger3.offset);
            if (n13 + Integer.MIN_VALUE > n11) {
                this.divadd(nArray, mutableBigInteger3.value, i + 1 + mutableBigInteger3.offset);
            }
            nArray2[i] = --n8;
        }
        if (n5 > 0) {
            mutableBigInteger3.rightShift(n5);
        }
        mutableBigInteger3.normalize();
        mutableBigInteger2.normalize();
    }

    private boolean unsignedLongCompare(long l, long l2) {
        return l + Long.MIN_VALUE > l2 + Long.MIN_VALUE;
    }

    private void divWord(int[] nArray, long l, int n) {
        long l2 = (long)n & 0xFFFFFFFFL;
        if (l2 == 1L) {
            nArray[0] = (int)l;
            nArray[1] = 0;
            return;
        }
        long l3 = (l >>> 1) / (l2 >>> 1);
        long l4 = l - l3 * l2;
        while (l4 < 0L) {
            l4 += l2;
            --l3;
        }
        while (l4 >= l2) {
            l4 -= l2;
            ++l3;
        }
        nArray[0] = (int)l3;
        nArray[1] = (int)l4;
    }

    MutableBigInteger hybridGCD(MutableBigInteger mutableBigInteger) {
        MutableBigInteger mutableBigInteger2 = this;
        MutableBigInteger mutableBigInteger3 = new MutableBigInteger();
        MutableBigInteger mutableBigInteger4 = new MutableBigInteger();
        while (mutableBigInteger.intLen != 0) {
            if (Math.abs(mutableBigInteger2.intLen - mutableBigInteger.intLen) < 2) {
                return mutableBigInteger2.binaryGCD(mutableBigInteger);
            }
            mutableBigInteger2.divide(mutableBigInteger, mutableBigInteger3, mutableBigInteger4);
            MutableBigInteger mutableBigInteger5 = mutableBigInteger2;
            mutableBigInteger2 = mutableBigInteger;
            mutableBigInteger = mutableBigInteger4;
            mutableBigInteger4 = mutableBigInteger5;
        }
        return mutableBigInteger2;
    }

    private MutableBigInteger binaryGCD(MutableBigInteger mutableBigInteger) {
        int n;
        int n2;
        int n3;
        int n4;
        MutableBigInteger mutableBigInteger2 = this;
        MutableBigInteger mutableBigInteger3 = new MutableBigInteger();
        MutableBigInteger mutableBigInteger4 = new MutableBigInteger();
        int n5 = mutableBigInteger2.getLowestSetBit();
        int n6 = n4 = n5 < (n3 = mutableBigInteger.getLowestSetBit()) ? n5 : n3;
        if (n4 != 0) {
            mutableBigInteger2.rightShift(n4);
            mutableBigInteger.rightShift(n4);
        }
        boolean bl = n4 == n5;
        MutableBigInteger mutableBigInteger5 = bl ? mutableBigInteger : mutableBigInteger2;
        int n7 = n2 = bl ? -1 : 1;
        while ((n = mutableBigInteger5.getLowestSetBit()) >= 0) {
            mutableBigInteger5.rightShift(n);
            if (n2 > 0) {
                mutableBigInteger2 = mutableBigInteger5;
            } else {
                mutableBigInteger = mutableBigInteger5;
            }
            if (mutableBigInteger2.intLen < 2 && mutableBigInteger.intLen < 2) {
                int n8 = mutableBigInteger2.value[mutableBigInteger2.offset];
                int n9 = mutableBigInteger.value[mutableBigInteger.offset];
                mutableBigInteger4.value[0] = n8 = MutableBigInteger.binaryGcd(n8, n9);
                mutableBigInteger4.intLen = 1;
                mutableBigInteger4.offset = 0;
                if (n4 > 0) {
                    mutableBigInteger4.leftShift(n4);
                }
                return mutableBigInteger4;
            }
            n2 = mutableBigInteger2.difference(mutableBigInteger);
            if (n2 == 0) break;
            mutableBigInteger5 = n2 >= 0 ? mutableBigInteger2 : mutableBigInteger;
        }
        if (n4 > 0) {
            mutableBigInteger2.leftShift(n4);
        }
        return mutableBigInteger2;
    }

    static int binaryGcd(int n, int n2) {
        int n3;
        int n4;
        if (n2 == 0) {
            return n;
        }
        if (n == 0) {
            return n2;
        }
        int n5 = 0;
        while ((n4 = n & 0xFF) == 0) {
            n >>>= 8;
            n5 += 8;
        }
        byte by = BigInteger.trailingZeroTable[n4];
        n5 += by;
        n >>>= by;
        int n6 = 0;
        while ((n4 = n2 & 0xFF) == 0) {
            n2 >>>= 8;
            n6 += 8;
        }
        by = BigInteger.trailingZeroTable[n4];
        n2 >>>= by;
        int n7 = n3 = n5 < (n6 += by) ? n5 : n6;
        while (n != n2) {
            if (n + Integer.MIN_VALUE > n2 + Integer.MIN_VALUE) {
                n -= n2;
                while ((n4 = n & 0xFF) == 0) {
                    n >>>= 8;
                }
                n >>>= BigInteger.trailingZeroTable[n4];
                continue;
            }
            n2 -= n;
            while ((n4 = n2 & 0xFF) == 0) {
                n2 >>>= 8;
            }
            n2 >>>= BigInteger.trailingZeroTable[n4];
        }
        return n << n3;
    }

    MutableBigInteger mutableModInverse(MutableBigInteger mutableBigInteger) {
        if (mutableBigInteger.isOdd()) {
            return this.modInverse(mutableBigInteger);
        }
        if (this.isEven()) {
            throw new ArithmeticException("BigInteger not invertible.");
        }
        int n = mutableBigInteger.getLowestSetBit();
        MutableBigInteger mutableBigInteger2 = new MutableBigInteger(mutableBigInteger);
        mutableBigInteger2.rightShift(n);
        if (mutableBigInteger2.isOne()) {
            return this.modInverseMP2(n);
        }
        MutableBigInteger mutableBigInteger3 = this.modInverse(mutableBigInteger2);
        MutableBigInteger mutableBigInteger4 = this.modInverseMP2(n);
        MutableBigInteger mutableBigInteger5 = MutableBigInteger.modInverseBP2(mutableBigInteger2, n);
        MutableBigInteger mutableBigInteger6 = mutableBigInteger2.modInverseMP2(n);
        MutableBigInteger mutableBigInteger7 = new MutableBigInteger();
        MutableBigInteger mutableBigInteger8 = new MutableBigInteger();
        MutableBigInteger mutableBigInteger9 = new MutableBigInteger();
        mutableBigInteger3.leftShift(n);
        mutableBigInteger3.multiply(mutableBigInteger5, mutableBigInteger9);
        mutableBigInteger4.multiply(mutableBigInteger2, mutableBigInteger7);
        mutableBigInteger7.multiply(mutableBigInteger6, mutableBigInteger8);
        mutableBigInteger9.add(mutableBigInteger8);
        mutableBigInteger9.divide(mutableBigInteger, mutableBigInteger7, mutableBigInteger8);
        return mutableBigInteger8;
    }

    MutableBigInteger modInverseMP2(int n) {
        if (this.isEven()) {
            throw new ArithmeticException("Non-invertible. (GCD != 1)");
        }
        if (n > 64) {
            return this.euclidModInverse(n);
        }
        int n2 = MutableBigInteger.inverseMod32(this.value[this.offset + this.intLen - 1]);
        if (n < 33) {
            n2 = n == 32 ? n2 : n2 & (1 << n) - 1;
            return new MutableBigInteger(n2);
        }
        long l = (long)this.value[this.offset + this.intLen - 1] & 0xFFFFFFFFL;
        if (this.intLen > 1) {
            l |= (long)this.value[this.offset + this.intLen - 2] << 32;
        }
        long l2 = (long)n2 & 0xFFFFFFFFL;
        l2 *= 2L - l * l2;
        l2 = n == 64 ? l2 : l2 & (1L << n) - 1L;
        MutableBigInteger mutableBigInteger = new MutableBigInteger(new int[2]);
        mutableBigInteger.value[0] = (int)(l2 >>> 32);
        mutableBigInteger.value[1] = (int)l2;
        mutableBigInteger.intLen = 2;
        mutableBigInteger.normalize();
        return mutableBigInteger;
    }

    static int inverseMod32(int n) {
        int n2 = n;
        n2 *= 2 - n * n2;
        n2 *= 2 - n * n2;
        n2 *= 2 - n * n2;
        n2 *= 2 - n * n2;
        return n2;
    }

    static MutableBigInteger modInverseBP2(MutableBigInteger mutableBigInteger, int n) {
        return MutableBigInteger.fixup(new MutableBigInteger(1), new MutableBigInteger(mutableBigInteger), n);
    }

    private MutableBigInteger modInverse(MutableBigInteger mutableBigInteger) {
        int n;
        MutableBigInteger mutableBigInteger2 = new MutableBigInteger(mutableBigInteger);
        MutableBigInteger mutableBigInteger3 = new MutableBigInteger(this);
        MutableBigInteger mutableBigInteger4 = new MutableBigInteger(mutableBigInteger2);
        SignedMutableBigInteger signedMutableBigInteger = new SignedMutableBigInteger(1);
        SignedMutableBigInteger signedMutableBigInteger2 = new SignedMutableBigInteger();
        MutableBigInteger mutableBigInteger5 = null;
        SignedMutableBigInteger signedMutableBigInteger3 = null;
        int n2 = 0;
        if (mutableBigInteger3.isEven()) {
            n = mutableBigInteger3.getLowestSetBit();
            mutableBigInteger3.rightShift(n);
            signedMutableBigInteger2.leftShift(n);
            n2 = n;
        }
        while (!mutableBigInteger3.isOne()) {
            if (mutableBigInteger3.isZero()) {
                throw new ArithmeticException("BigInteger not invertible.");
            }
            if (mutableBigInteger3.compare(mutableBigInteger4) < 0) {
                mutableBigInteger5 = mutableBigInteger3;
                mutableBigInteger3 = mutableBigInteger4;
                mutableBigInteger4 = mutableBigInteger5;
                signedMutableBigInteger3 = signedMutableBigInteger2;
                signedMutableBigInteger2 = signedMutableBigInteger;
                signedMutableBigInteger = signedMutableBigInteger3;
            }
            if (((mutableBigInteger3.value[mutableBigInteger3.offset + mutableBigInteger3.intLen - 1] ^ mutableBigInteger4.value[mutableBigInteger4.offset + mutableBigInteger4.intLen - 1]) & 3) == 0) {
                mutableBigInteger3.subtract(mutableBigInteger4);
                signedMutableBigInteger.signedSubtract(signedMutableBigInteger2);
            } else {
                mutableBigInteger3.add(mutableBigInteger4);
                signedMutableBigInteger.signedAdd(signedMutableBigInteger2);
            }
            n = mutableBigInteger3.getLowestSetBit();
            mutableBigInteger3.rightShift(n);
            signedMutableBigInteger2.leftShift(n);
            n2 += n;
        }
        while (signedMutableBigInteger.sign < 0) {
            signedMutableBigInteger.signedAdd(mutableBigInteger2);
        }
        return MutableBigInteger.fixup(signedMutableBigInteger, mutableBigInteger2, n2);
    }

    static MutableBigInteger fixup(MutableBigInteger mutableBigInteger, MutableBigInteger mutableBigInteger2, int n) {
        int n2;
        MutableBigInteger mutableBigInteger3 = new MutableBigInteger();
        int n3 = -MutableBigInteger.inverseMod32(mutableBigInteger2.value[mutableBigInteger2.offset + mutableBigInteger2.intLen - 1]);
        int n4 = n >> 5;
        for (n2 = 0; n2 < n4; ++n2) {
            int n5 = n3 * mutableBigInteger.value[mutableBigInteger.offset + mutableBigInteger.intLen - 1];
            mutableBigInteger2.mul(n5, mutableBigInteger3);
            mutableBigInteger.add(mutableBigInteger3);
            --mutableBigInteger.intLen;
        }
        n2 = n & 0x1F;
        if (n2 != 0) {
            n4 = n3 * mutableBigInteger.value[mutableBigInteger.offset + mutableBigInteger.intLen - 1];
            mutableBigInteger2.mul(n4 &= (1 << n2) - 1, mutableBigInteger3);
            mutableBigInteger.add(mutableBigInteger3);
            mutableBigInteger.rightShift(n2);
        }
        while (mutableBigInteger.compare(mutableBigInteger2) >= 0) {
            mutableBigInteger.subtract(mutableBigInteger2);
        }
        return mutableBigInteger;
    }

    MutableBigInteger euclidModInverse(int n) {
        MutableBigInteger mutableBigInteger = new MutableBigInteger(1);
        mutableBigInteger.leftShift(n);
        MutableBigInteger mutableBigInteger2 = new MutableBigInteger(mutableBigInteger);
        MutableBigInteger mutableBigInteger3 = new MutableBigInteger(this);
        MutableBigInteger mutableBigInteger4 = new MutableBigInteger();
        MutableBigInteger mutableBigInteger5 = new MutableBigInteger();
        mutableBigInteger.divide(mutableBigInteger3, mutableBigInteger4, mutableBigInteger5);
        MutableBigInteger mutableBigInteger6 = mutableBigInteger;
        mutableBigInteger = mutableBigInteger5;
        mutableBigInteger5 = mutableBigInteger6;
        MutableBigInteger mutableBigInteger7 = new MutableBigInteger(mutableBigInteger4);
        MutableBigInteger mutableBigInteger8 = new MutableBigInteger(1);
        MutableBigInteger mutableBigInteger9 = new MutableBigInteger();
        while (!mutableBigInteger.isOne()) {
            mutableBigInteger3.divide(mutableBigInteger, mutableBigInteger4, mutableBigInteger5);
            if (mutableBigInteger5.intLen == 0) {
                throw new ArithmeticException("BigInteger not invertible.");
            }
            mutableBigInteger6 = mutableBigInteger5;
            mutableBigInteger5 = mutableBigInteger3;
            mutableBigInteger3 = mutableBigInteger6;
            if (mutableBigInteger4.intLen == 1) {
                mutableBigInteger7.mul(mutableBigInteger4.value[mutableBigInteger4.offset], mutableBigInteger9);
            } else {
                mutableBigInteger4.multiply(mutableBigInteger7, mutableBigInteger9);
            }
            mutableBigInteger6 = mutableBigInteger4;
            mutableBigInteger4 = mutableBigInteger9;
            mutableBigInteger9 = mutableBigInteger6;
            mutableBigInteger8.add(mutableBigInteger4);
            if (mutableBigInteger3.isOne()) {
                return mutableBigInteger8;
            }
            mutableBigInteger.divide(mutableBigInteger3, mutableBigInteger4, mutableBigInteger5);
            if (mutableBigInteger5.intLen == 0) {
                throw new ArithmeticException("BigInteger not invertible.");
            }
            mutableBigInteger6 = mutableBigInteger;
            mutableBigInteger = mutableBigInteger5;
            mutableBigInteger5 = mutableBigInteger6;
            if (mutableBigInteger4.intLen == 1) {
                mutableBigInteger8.mul(mutableBigInteger4.value[mutableBigInteger4.offset], mutableBigInteger9);
            } else {
                mutableBigInteger4.multiply(mutableBigInteger8, mutableBigInteger9);
            }
            mutableBigInteger6 = mutableBigInteger4;
            mutableBigInteger4 = mutableBigInteger9;
            mutableBigInteger9 = mutableBigInteger6;
            mutableBigInteger7.add(mutableBigInteger4);
        }
        mutableBigInteger2.subtract(mutableBigInteger7);
        return mutableBigInteger2;
    }
}

