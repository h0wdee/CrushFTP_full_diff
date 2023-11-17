/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.util.logging;

import com.sun.mail.util.logging.CompactFormatter;
import com.sun.mail.util.logging.LogManagerProperties;
import com.sun.mail.util.logging.SeverityComparator;
import java.lang.reflect.UndeclaredThrowableException;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class CollectorFormatter
extends Formatter {
    private static final long INIT_TIME = System.currentTimeMillis();
    private final String fmt;
    private final Formatter formatter;
    private final Comparator<? super LogRecord> comparator;
    private LogRecord last;
    private long count;
    private long generation = 1L;
    private long thrown;
    private long minMillis = INIT_TIME;
    private long maxMillis = Long.MIN_VALUE;

    public CollectorFormatter() {
        String p = this.getClass().getName();
        this.fmt = this.initFormat(p);
        this.formatter = this.initFormatter(p);
        this.comparator = this.initComparator(p);
    }

    public CollectorFormatter(String format) {
        String p = this.getClass().getName();
        this.fmt = format == null ? this.initFormat(p) : format;
        this.formatter = this.initFormatter(p);
        this.comparator = this.initComparator(p);
    }

    public CollectorFormatter(String format, Formatter f, Comparator<? super LogRecord> c) {
        String p = this.getClass().getName();
        this.fmt = format == null ? this.initFormat(p) : format;
        this.formatter = f;
        this.comparator = c;
    }

    @Override
    public String format(LogRecord record) {
        boolean accepted;
        if (record == null) {
            throw new NullPointerException();
        }
        do {
            LogRecord update;
            LogRecord peek;
            if (peek != (update = this.apply((peek = this.peek()) != null ? peek : record, record))) {
                update.getSourceMethodName();
                accepted = this.acceptAndUpdate(peek, update);
                continue;
            }
            accepted = this.accept(peek, record);
        } while (!accepted);
        return "";
    }

    @Override
    public String getTail(Handler h) {
        super.getTail(h);
        return this.formatRecord(h, true);
    }

    public String toString() {
        String result;
        try {
            result = this.formatRecord(null, false);
        }
        catch (RuntimeException ignore) {
            result = super.toString();
        }
        return result;
    }

    protected LogRecord apply(LogRecord t, LogRecord u) {
        if (t == null || u == null) {
            throw new NullPointerException();
        }
        if (this.comparator != null) {
            return this.comparator.compare(t, u) >= 0 ? t : u;
        }
        return u;
    }

    private synchronized boolean accept(LogRecord e, LogRecord u) {
        long millis = u.getMillis();
        Throwable ex = u.getThrown();
        if (this.last == e) {
            this.minMillis = ++this.count != 1L ? Math.min(this.minMillis, millis) : millis;
            this.maxMillis = Math.max(this.maxMillis, millis);
            if (ex != null) {
                ++this.thrown;
            }
            return true;
        }
        return false;
    }

    private synchronized void reset(long min) {
        if (this.last != null) {
            this.last = null;
            ++this.generation;
        }
        this.count = 0L;
        this.thrown = 0L;
        this.minMillis = min;
        this.maxMillis = Long.MIN_VALUE;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String formatRecord(Handler h, boolean reset) {
        String tail;
        String msg;
        String head;
        long now;
        long msh;
        long msl;
        long t;
        long g;
        long c;
        LogRecord record;
        CollectorFormatter collectorFormatter = this;
        synchronized (collectorFormatter) {
            record = this.last;
            c = this.count;
            g = this.generation;
            t = this.thrown;
            msl = this.minMillis;
            msh = this.maxMillis;
            now = System.currentTimeMillis();
            if (c == 0L) {
                msh = now;
            }
            if (reset) {
                this.reset(msh);
            }
        }
        Formatter f = this.formatter;
        if (f != null) {
            Formatter formatter = f;
            synchronized (formatter) {
                head = f.getHead(h);
                msg = record != null ? f.format(record) : "";
                tail = f.getTail(h);
            }
        } else {
            head = "";
            msg = record != null ? this.formatMessage(record) : "";
            tail = "";
        }
        Locale l = null;
        if (record != null) {
            ResourceBundle rb = record.getResourceBundle();
            l = rb == null ? null : rb.getLocale();
        }
        MessageFormat mf = l == null ? new MessageFormat(this.fmt) : new MessageFormat(this.fmt, l);
        return mf.format(new Object[]{this.finish(head), this.finish(msg), this.finish(tail), c, c - 1L, t, c - t, msl, msh, msh - msl, INIT_TIME, now, now - INIT_TIME, g});
    }

    protected String finish(String s) {
        return s.trim();
    }

    private synchronized LogRecord peek() {
        return this.last;
    }

    private synchronized boolean acceptAndUpdate(LogRecord e, LogRecord u) {
        if (this.accept(e, u)) {
            this.last = u;
            return true;
        }
        return false;
    }

    private String initFormat(String p) {
        String v = LogManagerProperties.fromLogManager(p.concat(".format"));
        if (v == null || v.length() == 0) {
            v = "{0}{1}{2}{4,choice,-1#|0#|0<... {4,number,integer} more}\n";
        }
        return v;
    }

    private Formatter initFormatter(String p) {
        Formatter f;
        String v = LogManagerProperties.fromLogManager(p.concat(".formatter"));
        if (v != null && v.length() != 0) {
            if (!"null".equalsIgnoreCase(v)) {
                try {
                    f = LogManagerProperties.newFormatter(v);
                }
                catch (RuntimeException re) {
                    throw re;
                }
                catch (Exception e) {
                    throw new UndeclaredThrowableException(e);
                }
            } else {
                f = null;
            }
        } else {
            f = (Formatter)Formatter.class.cast(new CompactFormatter());
        }
        return f;
    }

    private Comparator<? super LogRecord> initComparator(String p) {
        Comparator<? super LogRecord> c;
        String name = LogManagerProperties.fromLogManager(p.concat(".comparator"));
        String reverse = LogManagerProperties.fromLogManager(p.concat(".comparator.reverse"));
        try {
            if (name != null && name.length() != 0) {
                if (!"null".equalsIgnoreCase(name)) {
                    c = LogManagerProperties.newComparator(name);
                    if (Boolean.parseBoolean(reverse)) {
                        assert (c != null);
                        c = LogManagerProperties.reverseOrder(c);
                    }
                } else {
                    if (reverse != null) {
                        throw new IllegalArgumentException("No comparator to reverse.");
                    }
                    c = null;
                }
            } else {
                if (reverse != null) {
                    throw new IllegalArgumentException("No comparator to reverse.");
                }
                c = (Comparator<? super LogRecord>)Comparator.class.cast(SeverityComparator.getInstance());
            }
        }
        catch (RuntimeException re) {
            throw re;
        }
        catch (Exception e) {
            throw new UndeclaredThrowableException(e);
        }
        return c;
    }
}

