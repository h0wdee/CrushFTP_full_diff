/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import java.math.BigInteger;

public class UnsignedInteger64 {
    public static final BigInteger MAX_VALUE = new BigInteger("18446744073709551615");
    public static final BigInteger MIN_VALUE = new BigInteger("0");
    private BigInteger bigInt;

    public UnsignedInteger64(String sval) throws NumberFormatException {
        this.bigInt = new BigInteger(sval);
        if (this.bigInt.compareTo(MIN_VALUE) < 0 || this.bigInt.compareTo(MAX_VALUE) > 0) {
            throw new NumberFormatException();
        }
    }

    public UnsignedInteger64(byte[] bval) throws NumberFormatException {
        this.bigInt = new BigInteger(bval);
        if (this.bigInt.compareTo(MIN_VALUE) < 0 || this.bigInt.compareTo(MAX_VALUE) > 0) {
            throw new NumberFormatException();
        }
    }

    public UnsignedInteger64(long value) {
        this.bigInt = BigInteger.valueOf(value);
    }

    public UnsignedInteger64(BigInteger input) {
        this.bigInt = new BigInteger(input.toString());
        if (this.bigInt.compareTo(MIN_VALUE) < 0 || this.bigInt.compareTo(MAX_VALUE) > 0) {
            throw new NumberFormatException();
        }
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        try {
            UnsignedInteger64 u = (UnsignedInteger64)o;
            return u.bigInt.equals(this.bigInt);
        }
        catch (ClassCastException ce) {
            return false;
        }
    }

    public BigInteger bigIntValue() {
        return this.bigInt;
    }

    public long longValue() {
        return this.bigInt.longValue();
    }

    public String toString() {
        return this.bigInt.toString(10);
    }

    public int hashCode() {
        return this.bigInt.hashCode();
    }

    public static UnsignedInteger64 add(UnsignedInteger64 x, UnsignedInteger64 y) {
        return new UnsignedInteger64(x.bigInt.add(y.bigInt));
    }

    public static UnsignedInteger64 add(UnsignedInteger64 x, int y) {
        return new UnsignedInteger64(x.bigInt.add(BigInteger.valueOf(y)));
    }

    public byte[] toByteArray() {
        byte[] raw = new byte[8];
        byte[] bi = this.bigIntValue().toByteArray();
        System.arraycopy(bi, 0, raw, raw.length - bi.length, bi.length);
        return raw;
    }
}

