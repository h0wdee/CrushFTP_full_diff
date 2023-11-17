/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

public abstract class TimeUtility {
    private static final int SECONDS_IN_MINUTE = 60;
    private static final long MILLISECONDS_IN_SECOND = 1000L;
    private static final long NANOSECONDS_IN_SECOND = 1000000000L;
    private static final long WINDOWSTIMETICKS_IN_SECOND = 10000000L;
    private static final long WINDOWSTIMETICKS_IN_MILLISECOND = 10000L;
    private static final long diff1601to1970 = 116444736000000000L;

    public static long timeToUtcMillisec(long time) {
        return (time - 116444736000000000L) / 10000L;
    }

    public static long utcMilliSecToTime(long utc) {
        return utc * 10000L + 116444736000000000L;
    }

    public static long getCurrentTime() {
        int[] tmpTime = TimeUtility.getCurrentTimeAsArray();
        long result = (long)tmpTime[0] + ((long)tmpTime[1] << 32);
        return result;
    }

    public static int[] getCurrentTimeAsArray() {
        long nowTime = System.currentTimeMillis();
        int a0 = (int)(nowTime & 0xFFFFL);
        int a1 = (int)(nowTime >>> 16 & 0xFFFFL);
        int a2 = (int)(nowTime >>> 32 & 0xFFFFL);
        a1 = a1 * 10000 + ((a0 *= 10000) >> 16);
        a2 = a2 * 10000 + (a1 >> 16);
        a0 &= 0xFFFF;
        a1 &= 0xFFFF;
        int[] timeNow = new int[]{((a1 &= 0xFFFF) << 16) + (a0 &= 0xFFFF), a2 += 27111902 + ((a1 += 54590 + ((a0 += 32768) >>> 16)) >>> 16)};
        return timeNow;
    }

    public static long convertMinutesToMilliseconds(int minutes) {
        return (long)(minutes * 60) * 1000L;
    }
}

