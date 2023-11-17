/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.logging;

import com.maverick.logging.AbstractLoggingContext;
import com.maverick.logging.DefaultLoggerContext;
import com.maverick.logging.Log;

public class ConsoleLoggingContext
extends AbstractLoggingContext {
    public ConsoleLoggingContext(Log.Level level) {
        super(level);
    }

    public ConsoleLoggingContext() {
    }

    @Override
    public void log(Log.Level level, String msg, Throwable e, Object ... args) {
        if (this.isLogging(level)) {
            try {
                System.out.print(DefaultLoggerContext.prepareLog(level, msg, e, args));
                System.out.flush();
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @Override
    public void raw(Log.Level level, String msg) {
        if (this.isLogging(level)) {
            try {
                System.out.print(DefaultLoggerContext.prepareLog(level, "", null, new Object[0]));
                System.out.println(msg);
                System.out.flush();
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
    }

    @Override
    public void newline() {
        System.out.println();
        System.out.flush();
    }
}

