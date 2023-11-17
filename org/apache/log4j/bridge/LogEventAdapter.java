/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.core.LogEvent
 *  org.apache.logging.log4j.core.util.Loader
 *  org.apache.logging.log4j.core.util.Throwables
 *  org.apache.logging.log4j.spi.StandardLevel
 *  org.apache.logging.log4j.status.StatusLogger
 *  org.apache.logging.log4j.util.Strings
 */
package org.apache.log4j.bridge;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.core.util.Throwables;
import org.apache.logging.log4j.spi.StandardLevel;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;

public class LogEventAdapter
extends LoggingEvent {
    private static final long JVM_START_TIME = LogEventAdapter.initStartTime();
    private final LogEvent event;

    public LogEventAdapter(LogEvent event) {
        this.event = event;
    }

    public static long getStartTime() {
        return JVM_START_TIME;
    }

    private static long initStartTime() {
        try {
            Class factoryClass = Loader.loadSystemClass((String)"java.lang.management.ManagementFactory");
            Method getRuntimeMXBean = factoryClass.getMethod("getRuntimeMXBean", new Class[0]);
            Object runtimeMXBean = getRuntimeMXBean.invoke(null, new Object[0]);
            Class runtimeMXBeanClass = Loader.loadSystemClass((String)"java.lang.management.RuntimeMXBean");
            Method getStartTime = runtimeMXBeanClass.getMethod("getStartTime", new Class[0]);
            return (Long)getStartTime.invoke(runtimeMXBean, new Object[0]);
        }
        catch (Throwable t) {
            StatusLogger.getLogger().error("Unable to call ManagementFactory.getRuntimeMXBean().getStartTime(), using system time for OnStartupTriggeringPolicy", t);
            return System.currentTimeMillis();
        }
    }

    public LogEvent getEvent() {
        return this.event;
    }

    @Override
    public LocationInfo getLocationInformation() {
        return new LocationInfo(this.event.getSource());
    }

    @Override
    public Level getLevel() {
        switch (StandardLevel.getStandardLevel((int)this.event.getLevel().intLevel())) {
            case TRACE: {
                return Level.TRACE;
            }
            case DEBUG: {
                return Level.DEBUG;
            }
            case INFO: {
                return Level.INFO;
            }
            case WARN: {
                return Level.WARN;
            }
            case FATAL: {
                return Level.FATAL;
            }
            case OFF: {
                return Level.OFF;
            }
            case ALL: {
                return Level.ALL;
            }
        }
        return Level.ERROR;
    }

    @Override
    public String getLoggerName() {
        return this.event.getLoggerName();
    }

    @Override
    public long getTimeStamp() {
        return this.event.getTimeMillis();
    }

    @Override
    public Category getLogger() {
        return Category.getInstance(this.event.getLoggerName());
    }

    @Override
    public Object getMessage() {
        return this.event.getMessage();
    }

    @Override
    public String getNDC() {
        return this.event.getContextStack().toString();
    }

    @Override
    public Object getMDC(String key) {
        if (this.event.getContextData() != null) {
            return this.event.getContextData().getValue(key);
        }
        return null;
    }

    @Override
    public void getMDCCopy() {
    }

    @Override
    public String getRenderedMessage() {
        return this.event.getMessage().getFormattedMessage();
    }

    @Override
    public String getThreadName() {
        return this.event.getThreadName();
    }

    @Override
    public ThrowableInformation getThrowableInformation() {
        if (this.event.getThrown() != null) {
            return new ThrowableInformation(this.event.getThrown());
        }
        return null;
    }

    @Override
    public String[] getThrowableStrRep() {
        if (this.event.getThrown() != null) {
            return Throwables.toStringList((Throwable)this.event.getThrown()).toArray(Strings.EMPTY_ARRAY);
        }
        return null;
    }

    @Override
    public String getProperty(String key) {
        return (String)this.event.getContextData().getValue(key);
    }

    @Override
    public Set getPropertyKeySet() {
        return this.event.getContextData().toMap().keySet();
    }

    @Override
    public Map getProperties() {
        return this.event.getContextData().toMap();
    }
}

