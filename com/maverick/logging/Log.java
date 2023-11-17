/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.logging;

import com.maverick.logging.DefaultLoggerContext;
import com.maverick.logging.IOUtils;
import com.maverick.logging.LoggerContext;
import com.maverick.logging.RootLoggerContext;

public class Log {
    static RootLoggerContext defaultContext = null;
    static ThreadLocal<LoggerContext> currentContext = new ThreadLocal();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static RootLoggerContext getDefaultContext() {
        Class<Log> clazz = Log.class;
        synchronized (Log.class) {
            if (defaultContext == null) {
                defaultContext = new DefaultLoggerContext();
            }
            // ** MonitorExit[var0] (shouldn't be in output)
            return defaultContext;
        }
    }

    public void shutdown() {
        defaultContext.shutdown();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void setDefaultContext(RootLoggerContext loggerContext) {
        Class<Log> clazz = Log.class;
        synchronized (Log.class) {
            defaultContext = loggerContext;
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return;
        }
    }

    public static void enableConsole(Level level) {
        Log.getDefaultContext().enableConsole(level);
    }

    public static void setupCurrentContext(LoggerContext context) {
        currentContext.set(context);
    }

    public static void clearCurrentContext() {
        currentContext.remove();
    }

    public static boolean isWarnEnabled() {
        return Log.isLevelEnabled(Level.WARN);
    }

    public static boolean isErrorEnabled() {
        return Log.isLevelEnabled(Level.ERROR);
    }

    public static boolean isInfoEnabled() {
        return Log.isLevelEnabled(Level.INFO);
    }

    public static boolean isDebugEnabled() {
        return Log.isLevelEnabled(Level.DEBUG);
    }

    public static boolean isTraceEnabled() {
        return Log.isLevelEnabled(Level.TRACE);
    }

    public static boolean isLevelEnabled(Level level) {
        LoggerContext ctx = currentContext.get();
        if (!IOUtils.isNull(ctx) && ctx.isLogging(level)) {
            return ctx.isLogging(level);
        }
        return Log.getDefaultContext().isLogging(level);
    }

    public static void info(String msg, Object ... args) {
        Log.log(Level.INFO, msg, null, args);
    }

    public static void info(String msg, Throwable e, Object ... args) {
        Log.log(Level.INFO, msg, e, args);
    }

    public static void debug(String msg, Object ... args) {
        Log.log(Level.DEBUG, msg, null, args);
    }

    public static void debug(String msg, Throwable e, Object ... args) {
        Log.log(Level.DEBUG, msg, e, args);
    }

    public static void trace(String msg, Object ... args) {
        Log.log(Level.TRACE, msg, null, args);
    }

    public static void trace(String msg, Throwable e, Object ... args) {
        Log.log(Level.TRACE, msg, e, args);
    }

    public static void error(String msg, Throwable e, Object ... args) {
        Log.log(Level.ERROR, msg, e, args);
    }

    public static void warn(String msg, Throwable e, Object ... args) {
        Log.log(Level.WARN, msg, e, args);
    }

    public static void warn(String msg, Object ... args) {
        Log.log(Level.WARN, msg, null, args);
    }

    public static void error(String msg, Object ... args) {
        Log.log(Level.ERROR, msg, null, args);
    }

    protected static void log(Level level, String msg, Throwable e, Object ... args) {
        LoggerContext ctx = currentContext.get();
        if (!IOUtils.isNull(ctx) && ctx.isLogging(level)) {
            Log.contextLog(ctx, level, msg, e, args);
        } else {
            Log.contextLog(Log.getDefaultContext(), level, msg, e, args);
        }
    }

    private static void contextLog(LoggerContext ctx, Level level, String msg, Throwable e, Object ... args) {
        ctx.log(level, msg, e, args);
    }

    public static void raw(Level level, String msg, boolean newline) {
        LoggerContext ctx = currentContext.get();
        if (!IOUtils.isNull(ctx) && ctx.isLogging(level)) {
            ctx.raw(level, msg);
            if (newline) {
                ctx.newline();
            }
        } else {
            Log.getDefaultContext().raw(level, msg);
            if (newline) {
                Log.getDefaultContext().newline();
            }
        }
    }

    public static enum Level {
        NONE,
        ERROR,
        WARN,
        INFO,
        DEBUG,
        TRACE;

    }
}

