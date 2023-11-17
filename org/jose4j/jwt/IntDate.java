/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwt;

import java.util.Date;

public class IntDate {
    private long value;
    private static final long CONVERSION = 1000L;

    private IntDate(long value) {
        this.value = value;
    }

    public static IntDate now() {
        return IntDate.fromMillis(System.currentTimeMillis());
    }

    public static IntDate fromSeconds(long secondsFromEpoch) {
        return new IntDate(secondsFromEpoch);
    }

    public static IntDate fromMillis(long millisecondsFromEpoch) {
        return IntDate.fromSeconds(millisecondsFromEpoch / 1000L);
    }

    public void addSeconds(long seconds) {
        this.value += seconds;
    }

    public void addSeconds(int seconds) {
        this.addSeconds((long)seconds);
    }

    public long getValue() {
        return this.value;
    }

    public long getValueInMillis() {
        return this.getValue() * 1000L;
    }

    public boolean before(IntDate when) {
        return this.value < when.getValue();
    }

    public boolean after(IntDate when) {
        return this.value > when.getValue();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IntDate");
        sb.append("{").append(this.getValue()).append(" --> ");
        sb.append(new Date(this.getValueInMillis()));
        sb.append('}');
        return sb.toString();
    }

    public boolean equals(Object other) {
        return this == other || other instanceof IntDate && this.value == ((IntDate)other).value;
    }

    public int hashCode() {
        return (int)(this.value ^ this.value >>> 32);
    }
}

