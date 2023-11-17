/*
 * Decompiled with CFR 0.152.
 */
package org.apache.log4j;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.OptionHandler;

public abstract class AppenderSkeleton
implements Appender,
OptionHandler {
    protected Layout layout;
    protected String name;
    protected Priority threshold;
    protected ErrorHandler errorHandler = new NoOpErrorHandler();
    protected Filter headFilter;
    protected Filter tailFilter;
    protected boolean closed = false;

    public AppenderSkeleton() {
    }

    protected AppenderSkeleton(boolean isActive) {
    }

    @Override
    public void activateOptions() {
    }

    @Override
    public void addFilter(Filter newFilter) {
        if (this.headFilter == null) {
            this.headFilter = this.tailFilter = newFilter;
        } else {
            this.tailFilter.setNext(newFilter);
            this.tailFilter = newFilter;
        }
    }

    protected abstract void append(LoggingEvent var1);

    @Override
    public void clearFilters() {
        this.tailFilter = null;
        this.headFilter = null;
    }

    public void finalize() {
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    @Override
    public Filter getFilter() {
        return this.headFilter;
    }

    public final Filter getFirstFilter() {
        return this.headFilter;
    }

    @Override
    public Layout getLayout() {
        return this.layout;
    }

    @Override
    public final String getName() {
        return this.name;
    }

    public Priority getThreshold() {
        return this.threshold;
    }

    public boolean isAsSevereAsThreshold(Priority priority) {
        return this.threshold == null || priority.isGreaterOrEqual(this.threshold);
    }

    @Override
    public void doAppend(LoggingEvent event) {
        this.append(event);
    }

    @Override
    public synchronized void setErrorHandler(ErrorHandler eh) {
        if (eh != null) {
            this.errorHandler = eh;
        }
    }

    @Override
    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public void setThreshold(Priority threshold) {
        this.threshold = threshold;
    }

    public static class NoOpErrorHandler
    implements ErrorHandler {
        @Override
        public void setLogger(Logger logger) {
        }

        @Override
        public void error(String message, Exception e, int errorCode) {
        }

        @Override
        public void error(String message) {
        }

        @Override
        public void error(String message, Exception e, int errorCode, LoggingEvent event) {
        }

        @Override
        public void setAppender(Appender appender) {
        }

        @Override
        public void setBackupAppender(Appender appender) {
        }
    }
}

