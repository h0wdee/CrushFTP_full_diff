/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.util.logging;

import com.sun.mail.util.logging.LogManagerProperties;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class DurationFilter
implements Filter {
    private final long records;
    private final long duration;
    private long count;
    private long peak;
    private long start;

    public DurationFilter() {
        this.records = DurationFilter.checkRecords(this.initLong(".records"));
        this.duration = DurationFilter.checkDuration(this.initLong(".duration"));
    }

    public DurationFilter(long records, long duration) {
        this.records = DurationFilter.checkRecords(records);
        this.duration = DurationFilter.checkDuration(duration);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean equals(Object obj) {
        long s;
        long p;
        long c;
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        DurationFilter other = (DurationFilter)obj;
        if (this.records != other.records) {
            return false;
        }
        if (this.duration != other.duration) {
            return false;
        }
        DurationFilter durationFilter = this;
        synchronized (durationFilter) {
            c = this.count;
            p = this.peak;
            s = this.start;
        }
        durationFilter = other;
        synchronized (durationFilter) {
            if (c != other.count || p != other.peak || s != other.start) {
                return false;
            }
        }
        return true;
    }

    public boolean isIdle() {
        return this.test(0L, System.currentTimeMillis());
    }

    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (int)(this.records ^ this.records >>> 32);
        hash = 89 * hash + (int)(this.duration ^ this.duration >>> 32);
        return hash;
    }

    public boolean isLoggable(LogRecord record) {
        return this.accept(record.getMillis());
    }

    public boolean isLoggable() {
        return this.test(this.records, System.currentTimeMillis());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String toString() {
        boolean loggable;
        boolean idle;
        DurationFilter durationFilter = this;
        synchronized (durationFilter) {
            long millis = System.currentTimeMillis();
            idle = this.test(0L, millis);
            loggable = this.test(this.records, millis);
        }
        return this.getClass().getName() + "{records=" + this.records + ", duration=" + this.duration + ", idle=" + idle + ", loggable=" + loggable + '}';
    }

    protected DurationFilter clone() throws CloneNotSupportedException {
        DurationFilter clone = (DurationFilter)super.clone();
        clone.count = 0L;
        clone.peak = 0L;
        clone.start = 0L;
        return clone;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean test(long limit, long millis) {
        long s;
        long c;
        assert (limit >= 0L) : limit;
        DurationFilter durationFilter = this;
        synchronized (durationFilter) {
            c = this.count;
            s = this.start;
        }
        return c > 0L ? millis - s >= this.duration || c < limit : millis - s >= 0L || c == 0L;
    }

    private synchronized boolean accept(long millis) {
        boolean allow;
        if (this.count > 0L) {
            if (millis - this.peak > 0L) {
                this.peak = millis;
            }
            if (this.count != this.records) {
                ++this.count;
                allow = true;
            } else if (this.peak - this.start >= this.duration) {
                this.count = 1L;
                this.start = this.peak;
                allow = true;
            } else {
                this.count = -1L;
                this.start = this.peak + this.duration;
                allow = false;
            }
        } else if (millis - this.start >= 0L || this.count == 0L) {
            this.count = 1L;
            this.start = millis;
            this.peak = millis;
            allow = true;
        } else {
            allow = false;
        }
        return allow;
    }

    private long initLong(String suffix) {
        long result = 0L;
        String p = this.getClass().getName();
        String value = LogManagerProperties.fromLogManager(p.concat(suffix));
        if (value != null && value.length() != 0) {
            if (this.isTimeEntry(suffix, value = value.trim())) {
                try {
                    result = LogManagerProperties.parseDurationToMillis(value);
                }
                catch (RuntimeException runtimeException) {
                }
                catch (Exception exception) {
                }
                catch (LinkageError linkageError) {
                    // empty catch block
                }
            }
            if (result == 0L) {
                try {
                    result = 1L;
                    for (String s : DurationFilter.tokenizeLongs(value)) {
                        if (s.endsWith("L") || s.endsWith("l")) {
                            s = s.substring(0, s.length() - 1);
                        }
                        result = DurationFilter.multiplyExact(result, Long.parseLong(s));
                    }
                }
                catch (RuntimeException ignore) {
                    result = Long.MIN_VALUE;
                }
            }
        } else {
            result = Long.MIN_VALUE;
        }
        return result;
    }

    private boolean isTimeEntry(String suffix, String value) {
        return (value.charAt(0) == 'P' || value.charAt(0) == 'p') && suffix.equals(".duration");
    }

    private static String[] tokenizeLongs(String value) {
        String[] e;
        int i = value.indexOf(42);
        if (i > -1 && (e = value.split("\\s*\\*\\s*")).length != 0) {
            if (i == 0 || value.charAt(value.length() - 1) == '*') {
                throw new NumberFormatException(value);
            }
            if (e.length == 1) {
                throw new NumberFormatException(e[0]);
            }
        } else {
            e = new String[]{value};
        }
        return e;
    }

    private static long multiplyExact(long x, long y) {
        long r = x * y;
        if ((Math.abs(x) | Math.abs(y)) >>> 31 != 0L && (y != 0L && r / y != x || x == Long.MIN_VALUE && y == -1L)) {
            throw new ArithmeticException();
        }
        return r;
    }

    private static long checkRecords(long records) {
        return records > 0L ? records : 1000L;
    }

    private static long checkDuration(long duration) {
        return duration > 0L ? duration : 900000L;
    }
}

