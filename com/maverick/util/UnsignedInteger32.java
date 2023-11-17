/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

public class UnsignedInteger32 {
    public static final long MAX_VALUE = 0xFFFFFFFFL;
    public static final long MIN_VALUE = 0L;
    private Long value;

    public UnsignedInteger32(long a) {
        if (a < 0L || a > 0xFFFFFFFFL) {
            throw new NumberFormatException();
        }
        this.value = new Long(a);
    }

    public UnsignedInteger32(String a) throws NumberFormatException {
        long longValue = Long.parseLong(a);
        if (longValue < 0L || longValue > 0xFFFFFFFFL) {
            throw new NumberFormatException();
        }
        this.value = new Long(longValue);
    }

    public int intValue() {
        return (int)this.value.longValue();
    }

    public long longValue() {
        return this.value;
    }

    public String toString() {
        return this.value.toString();
    }

    public int hashCode() {
        return this.value.hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof UnsignedInteger32)) {
            return false;
        }
        return ((UnsignedInteger32)o).value.equals(this.value);
    }

    public static UnsignedInteger32 add(UnsignedInteger32 x, UnsignedInteger32 y) {
        return new UnsignedInteger32(x.longValue() + y.longValue());
    }

    public static UnsignedInteger32 add(UnsignedInteger32 x, int y) {
        return new UnsignedInteger32(x.longValue() + (long)y);
    }
}

