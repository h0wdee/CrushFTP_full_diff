/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.logging;

import com.maverick.logging.Log;
import org.slf4j.Logger;
import org.slf4j.Marker;

public class MaverickLogger
implements Logger {
    @Override
    public String getName() {
        return "Maverick";
    }

    @Override
    public boolean isTraceEnabled() {
        return Log.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        Log.trace(msg, new Object[0]);
    }

    @Override
    public void trace(String format, Object arg) {
        Log.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        Log.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object ... arguments) {
        Log.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        Log.trace(msg, t, new Object[0]);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return Log.isTraceEnabled();
    }

    @Override
    public void trace(Marker marker, String msg) {
        Log.trace(msg, new Object[0]);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        Log.trace(format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        Log.trace(format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object ... argArray) {
        Log.trace(format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        Log.trace(msg, t, new Object[0]);
    }

    @Override
    public boolean isDebugEnabled() {
        return Log.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        Log.debug(msg, new Object[0]);
    }

    @Override
    public void debug(String format, Object arg) {
        Log.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        Log.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object ... arguments) {
        Log.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        Log.debug(msg, t, new Object[0]);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return Log.isDebugEnabled();
    }

    @Override
    public void debug(Marker marker, String msg) {
        Log.debug(msg, new Object[0]);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        Log.debug(format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        Log.debug(format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object ... arguments) {
        Log.debug(format, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        Log.debug(msg, t, new Object[0]);
    }

    @Override
    public boolean isInfoEnabled() {
        return Log.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        Log.info(msg, new Object[0]);
    }

    @Override
    public void info(String format, Object arg) {
        Log.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        Log.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object ... arguments) {
        Log.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        Log.info(msg, t, new Object[0]);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return Log.isInfoEnabled();
    }

    @Override
    public void info(Marker marker, String msg) {
        Log.info(msg, new Object[0]);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        Log.info(format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        Log.info(format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object ... arguments) {
        Log.info(format, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        Log.info(msg, t, new Object[0]);
    }

    @Override
    public boolean isWarnEnabled() {
        return Log.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        Log.warn(msg, new Object[0]);
    }

    @Override
    public void warn(String format, Object arg) {
        Log.warn(format, arg);
    }

    @Override
    public void warn(String format, Object ... arguments) {
        Log.warn(format, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        Log.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        Log.warn(msg, t, new Object[0]);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return Log.isWarnEnabled();
    }

    @Override
    public void warn(Marker marker, String msg) {
        Log.warn(msg, new Object[0]);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        Log.warn(format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        Log.warn(format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object ... arguments) {
        Log.warn(format, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        Log.warn(msg, t, new Object[0]);
    }

    @Override
    public boolean isErrorEnabled() {
        return Log.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        Log.error(msg, new Object[0]);
    }

    @Override
    public void error(String format, Object arg) {
        Log.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        Log.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object ... arguments) {
        Log.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        Log.error(msg, t, new Object[0]);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return Log.isErrorEnabled();
    }

    @Override
    public void error(Marker marker, String msg) {
        Log.error(msg, new Object[0]);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        Log.error(format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        Log.error(format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object ... arguments) {
        Log.error(format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        Log.error(msg, t, new Object[0]);
    }
}

