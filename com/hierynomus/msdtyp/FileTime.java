/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msdtyp;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class FileTime {
    public static final int NANO100_TO_MILLI = 10000;
    public static final int NANO100_TO_NANO = 100;
    public static final long WINDOWS_TO_UNIX_EPOCH = 116444736000000000L;
    private final long windowsTimeStamp;

    public static FileTime fromDate(Date date) {
        return new FileTime(date.getTime() * 10000L + 116444736000000000L);
    }

    public static FileTime now() {
        return FileTime.ofEpochMillis(System.currentTimeMillis());
    }

    public static FileTime ofEpochMillis(long epochMillis) {
        return FileTime.ofEpoch(epochMillis, TimeUnit.MILLISECONDS);
    }

    public static FileTime ofEpoch(long epoch, TimeUnit unit) {
        long nanoEpoch = TimeUnit.NANOSECONDS.convert(epoch, unit);
        return new FileTime(nanoEpoch / 100L + 116444736000000000L);
    }

    public FileTime(long windowsTimeStamp) {
        this.windowsTimeStamp = windowsTimeStamp;
    }

    public long getWindowsTimeStamp() {
        return this.windowsTimeStamp;
    }

    public long toEpochMillis() {
        return this.toEpoch(TimeUnit.MILLISECONDS);
    }

    public long toEpoch(TimeUnit unit) {
        return unit.convert((this.windowsTimeStamp - 116444736000000000L) * 100L, TimeUnit.NANOSECONDS);
    }

    public Date toDate() {
        return new Date(this.toEpochMillis());
    }

    public String toString() {
        return this.toDate().toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        FileTime fileTime = (FileTime)o;
        return this.windowsTimeStamp == fileTime.windowsTimeStamp;
    }

    public int hashCode() {
        return (int)(this.windowsTimeStamp ^ this.windowsTimeStamp >>> 32);
    }
}

