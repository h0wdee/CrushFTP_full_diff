/*
 * Decompiled with CFR 0.152.
 */
package jline.internal;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import jline.internal.Configuration;
import jline.internal.Preconditions;
import jline.internal.TestAccessible;

public final class Log {
    public static final boolean TRACE = Configuration.getBoolean(Log.class.getName() + ".trace");
    public static final boolean DEBUG = TRACE || Configuration.getBoolean(Log.class.getName() + ".debug");
    private static PrintStream output = System.err;
    private static boolean useJul = Configuration.getBoolean("jline.log.jul");

    public static PrintStream getOutput() {
        return output;
    }

    public static void setOutput(PrintStream out) {
        output = Preconditions.checkNotNull(out);
    }

    @TestAccessible
    static void render(PrintStream out, Object message) {
        if (message.getClass().isArray()) {
            Object[] array = (Object[])message;
            out.print("[");
            for (int i = 0; i < array.length; ++i) {
                out.print(array[i]);
                if (i + 1 >= array.length) continue;
                out.print(",");
            }
            out.print("]");
        } else {
            out.print(message);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @TestAccessible
    static void log(Level level, Object ... messages) {
        if (useJul) {
            Log.logWithJul(level, messages);
            return;
        }
        PrintStream printStream = output;
        synchronized (printStream) {
            output.format("[%s] ", new Object[]{level});
            for (int i = 0; i < messages.length; ++i) {
                if (i + 1 == messages.length && messages[i] instanceof Throwable) {
                    output.println();
                    ((Throwable)messages[i]).printStackTrace(output);
                    continue;
                }
                Log.render(output, messages[i]);
            }
            output.println();
            output.flush();
        }
    }

    static void logWithJul(Level level, Object ... messages) {
        Logger logger = Logger.getLogger("jline");
        Throwable cause = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        for (int i = 0; i < messages.length; ++i) {
            if (i + 1 == messages.length && messages[i] instanceof Throwable) {
                cause = (Throwable)messages[i];
                continue;
            }
            Log.render(ps, messages[i]);
        }
        ps.close();
        LogRecord r = new LogRecord(Log.toJulLevel(level), baos.toString());
        r.setThrown(cause);
        logger.log(r);
    }

    private static java.util.logging.Level toJulLevel(Level level) {
        switch (level) {
            case TRACE: {
                return java.util.logging.Level.FINEST;
            }
            case DEBUG: {
                return java.util.logging.Level.FINE;
            }
            case INFO: {
                return java.util.logging.Level.INFO;
            }
            case WARN: {
                return java.util.logging.Level.WARNING;
            }
            case ERROR: {
                return java.util.logging.Level.SEVERE;
            }
        }
        throw new IllegalArgumentException();
    }

    public static void trace(Object ... messages) {
        if (TRACE) {
            Log.log(Level.TRACE, messages);
        }
    }

    public static void debug(Object ... messages) {
        if (TRACE || DEBUG) {
            Log.log(Level.DEBUG, messages);
        }
    }

    public static void info(Object ... messages) {
        Log.log(Level.INFO, messages);
    }

    public static void warn(Object ... messages) {
        Log.log(Level.WARN, messages);
    }

    public static void error(Object ... messages) {
        Log.log(Level.ERROR, messages);
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static enum Level {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR;

    }
}

