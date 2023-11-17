/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.util;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Session;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class MailLogger {
    private final Logger logger;
    private final String prefix;
    private final boolean debug;
    private final PrintStream out;

    public MailLogger(String name, String prefix, boolean debug, PrintStream out) {
        this.logger = Logger.getLogger(name);
        this.prefix = prefix;
        this.debug = debug;
        this.out = out != null ? out : System.out;
    }

    public MailLogger(Class<?> clazz, String prefix, boolean debug, PrintStream out) {
        String name = this.packageOf(clazz);
        this.logger = Logger.getLogger(name);
        this.prefix = prefix;
        this.debug = debug;
        this.out = out != null ? out : System.out;
    }

    public MailLogger(Class<?> clazz, String subname, String prefix, boolean debug, PrintStream out) {
        String name = this.packageOf(clazz) + "." + subname;
        this.logger = Logger.getLogger(name);
        this.prefix = prefix;
        this.debug = debug;
        this.out = out != null ? out : System.out;
    }

    public MailLogger(String name, String prefix, Session session) {
        this(name, prefix, session.getDebug(), session.getDebugOut());
    }

    public MailLogger(Class<?> clazz, String prefix, Session session) {
        this(clazz, prefix, session.getDebug(), session.getDebugOut());
    }

    public MailLogger getLogger(String name, String prefix) {
        return new MailLogger(name, prefix, this.debug, this.out);
    }

    public MailLogger getLogger(Class<?> clazz, String prefix) {
        return new MailLogger(clazz, prefix, this.debug, this.out);
    }

    public MailLogger getSubLogger(String subname, String prefix) {
        return new MailLogger(this.logger.getName() + "." + subname, prefix, this.debug, this.out);
    }

    public MailLogger getSubLogger(String subname, String prefix, boolean debug) {
        return new MailLogger(this.logger.getName() + "." + subname, prefix, debug, this.out);
    }

    public void log(Level level, String msg) {
        this.ifDebugOut(msg);
        if (this.logger.isLoggable(level)) {
            StackTraceElement frame = this.inferCaller();
            this.logger.logp(level, frame.getClassName(), frame.getMethodName(), msg);
        }
    }

    public void log(Level level, String msg, Object param1) {
        if (this.debug) {
            msg = MessageFormat.format(msg, param1);
            this.debugOut(msg);
        }
        if (this.logger.isLoggable(level)) {
            StackTraceElement frame = this.inferCaller();
            this.logger.logp(level, frame.getClassName(), frame.getMethodName(), msg, param1);
        }
    }

    public void log(Level level, String msg, Object[] params) {
        if (this.debug) {
            msg = MessageFormat.format(msg, params);
            this.debugOut(msg);
        }
        if (this.logger.isLoggable(level)) {
            StackTraceElement frame = this.inferCaller();
            this.logger.logp(level, frame.getClassName(), frame.getMethodName(), msg, params);
        }
    }

    public void logf(Level level, String msg, Object ... params) {
        msg = String.format(msg, params);
        this.ifDebugOut(msg);
        this.logger.log(level, msg);
    }

    public void log(Level level, String msg, Throwable thrown) {
        if (this.debug) {
            if (thrown != null) {
                this.debugOut(msg + ", THROW: ");
                thrown.printStackTrace(this.out);
            } else {
                this.debugOut(msg);
            }
        }
        if (this.logger.isLoggable(level)) {
            StackTraceElement frame = this.inferCaller();
            this.logger.logp(level, frame.getClassName(), frame.getMethodName(), msg, thrown);
        }
    }

    public void config(String msg) {
        this.log(Level.CONFIG, msg);
    }

    public void fine(String msg) {
        this.log(Level.FINE, msg);
    }

    public void finer(String msg) {
        this.log(Level.FINER, msg);
    }

    public void finest(String msg) {
        this.log(Level.FINEST, msg);
    }

    public boolean isLoggable(Level level) {
        return this.debug || this.logger.isLoggable(level);
    }

    private void ifDebugOut(String msg) {
        if (this.debug) {
            this.debugOut(msg);
        }
    }

    private void debugOut(String msg) {
        if (this.prefix != null) {
            this.out.println(this.prefix + ": " + msg);
        } else {
            this.out.println(msg);
        }
    }

    private String packageOf(Class<?> clazz) {
        Package p = clazz.getPackage();
        if (p != null) {
            return p.getName();
        }
        String cname = clazz.getName();
        int i = cname.lastIndexOf(46);
        if (i > 0) {
            return cname.substring(0, i);
        }
        return "";
    }

    private StackTraceElement inferCaller() {
        StackTraceElement frame;
        String cname;
        int ix;
        StackTraceElement[] stack = new Throwable().getStackTrace();
        for (ix = 0; ix < stack.length && !this.isLoggerImplFrame(cname = (frame = stack[ix]).getClassName()); ++ix) {
        }
        while (ix < stack.length) {
            frame = stack[ix];
            cname = frame.getClassName();
            if (!this.isLoggerImplFrame(cname)) {
                return frame;
            }
            ++ix;
        }
        return new StackTraceElement(MailLogger.class.getName(), "log", MailLogger.class.getName(), -1);
    }

    private boolean isLoggerImplFrame(String cname) {
        return MailLogger.class.getName().equals(cname);
    }
}

