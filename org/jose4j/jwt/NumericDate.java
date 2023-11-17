/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwt;

import java.text.DateFormat;
import java.util.Date;

public class NumericDate {
    private long value;
    private static final long CONVERSION = 1000L;

    private NumericDate(long value) {
        this.value = value;
    }

    public static NumericDate now() {
        return NumericDate.fromMilliseconds(System.currentTimeMillis());
    }

    public static NumericDate fromSeconds(long secondsFromEpoch) {
        return new NumericDate(secondsFromEpoch);
    }

    public static NumericDate fromMilliseconds(long millisecondsFromEpoch) {
        return NumericDate.fromSeconds(millisecondsFromEpoch / 1000L);
    }

    public void addSeconds(long seconds) {
        this.value += seconds;
    }

    public long getValue() {
        return this.value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long getValueInMillis() {
        return this.getValue() * 1000L;
    }

    public boolean isBefore(NumericDate when) {
        return this.value < when.getValue();
    }

    public boolean isOnOrAfter(NumericDate when) {
        return !this.isBefore(when);
    }

    public boolean isAfter(NumericDate when) {
        return this.value > when.getValue();
    }

    public String toString() {
        DateFormat df = DateFormat.getDateTimeInstance(2, 1);
        StringBuilder sb = new StringBuilder();
        Date date = new Date(this.getValueInMillis());
        sb.append("NumericDate").append("{").append(this.getValue()).append(" -> ").append(df.format(date)).append('}');
        return sb.toString();
    }

    public boolean equals(Object other) {
        return this == other || other instanceof NumericDate && this.value == ((NumericDate)other).value;
    }

    public int hashCode() {
        return (int)(this.value ^ this.value >>> 32);
    }
}

