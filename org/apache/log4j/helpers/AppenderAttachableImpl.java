/*
 * Decompiled with CFR 0.152.
 */
package org.apache.log4j.helpers;

import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.log4j.Appender;
import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.spi.LoggingEvent;

public class AppenderAttachableImpl
implements AppenderAttachable {
    private final ConcurrentMap<String, Appender> appenders = new ConcurrentHashMap<String, Appender>();

    @Override
    public void addAppender(Appender newAppender) {
        if (newAppender != null) {
            this.appenders.put(newAppender.getName(), newAppender);
        }
    }

    @Override
    public Enumeration<Appender> getAllAppenders() {
        return Collections.enumeration(this.appenders.values());
    }

    @Override
    public Appender getAppender(String name) {
        return (Appender)this.appenders.get(name);
    }

    @Override
    public boolean isAttached(Appender appender) {
        return this.appenders.containsValue(appender);
    }

    @Override
    public void removeAllAppenders() {
        this.appenders.clear();
    }

    @Override
    public void removeAppender(Appender appender) {
        this.appenders.remove(appender.getName(), appender);
    }

    @Override
    public void removeAppender(String name) {
        this.appenders.remove(name);
    }

    public int appendLoopOnAppenders(LoggingEvent event) {
        for (Appender appender : this.appenders.values()) {
            appender.doAppend(event);
        }
        return this.appenders.size();
    }

    public void close() {
        for (Appender appender : this.appenders.values()) {
            appender.close();
        }
    }
}

