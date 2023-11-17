/*
 * Decompiled with CFR 0.152.
 */
package org.apache.log4j.spi;

import java.util.Map;
import java.util.Set;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.bridge.LogEventAdapter;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.ThrowableInformation;

public class LoggingEvent {
    public LocationInfo getLocationInformation() {
        return null;
    }

    public Level getLevel() {
        return null;
    }

    public String getLoggerName() {
        return null;
    }

    public String getFQNOfLoggerClass() {
        return null;
    }

    public long getTimeStamp() {
        return 0L;
    }

    public Category getLogger() {
        return null;
    }

    public Object getMessage() {
        return null;
    }

    public String getNDC() {
        return null;
    }

    public Object getMDC(String key) {
        return null;
    }

    public void getMDCCopy() {
    }

    public String getRenderedMessage() {
        return null;
    }

    public static long getStartTime() {
        return LogEventAdapter.getStartTime();
    }

    public String getThreadName() {
        return null;
    }

    public ThrowableInformation getThrowableInformation() {
        return null;
    }

    public String[] getThrowableStrRep() {
        return null;
    }

    public void setProperty(String propName, String propValue) {
    }

    public String getProperty(String key) {
        return null;
    }

    public Set getPropertyKeySet() {
        return null;
    }

    public Map getProperties() {
        return null;
    }

    public Object removeProperty(String propName) {
        return null;
    }
}

