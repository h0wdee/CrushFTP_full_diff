/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Level
 *  org.apache.logging.log4j.Marker
 *  org.apache.logging.log4j.ThreadContext$ContextStack
 *  org.apache.logging.log4j.core.LogEvent
 *  org.apache.logging.log4j.core.impl.ThrowableProxy
 *  org.apache.logging.log4j.core.time.Instant
 *  org.apache.logging.log4j.core.time.MutableInstant
 *  org.apache.logging.log4j.message.Message
 *  org.apache.logging.log4j.message.SimpleMessage
 *  org.apache.logging.log4j.spi.MutableThreadContextStack
 *  org.apache.logging.log4j.util.BiConsumer
 *  org.apache.logging.log4j.util.ReadOnlyStringMap
 *  org.apache.logging.log4j.util.TriConsumer
 */
package org.apache.log4j.bridge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.log4j.NDC;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.core.time.MutableInstant;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.spi.MutableThreadContextStack;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.TriConsumer;

public class LogEventWrapper
implements LogEvent {
    private final LoggingEvent event;
    private final ContextDataMap contextData;
    private final MutableThreadContextStack contextStack;
    private Thread thread;

    public LogEventWrapper(LoggingEvent event) {
        this.event = event;
        this.contextData = new ContextDataMap(event.getProperties());
        this.contextStack = new MutableThreadContextStack((List)NDC.cloneStack());
        this.thread = Objects.equals(event.getThreadName(), Thread.currentThread().getName()) ? Thread.currentThread() : null;
    }

    public LogEvent toImmutable() {
        return this;
    }

    public Map<String, String> getContextMap() {
        return this.contextData;
    }

    public ReadOnlyStringMap getContextData() {
        return this.contextData;
    }

    public ThreadContext.ContextStack getContextStack() {
        return this.contextStack;
    }

    public String getLoggerFqcn() {
        return null;
    }

    public Level getLevel() {
        return OptionConverter.convertLevel(this.event.getLevel());
    }

    public String getLoggerName() {
        return this.event.getLoggerName();
    }

    public Marker getMarker() {
        return null;
    }

    public Message getMessage() {
        return new SimpleMessage(this.event.getRenderedMessage());
    }

    public long getTimeMillis() {
        return this.event.getTimeStamp();
    }

    public Instant getInstant() {
        MutableInstant mutable = new MutableInstant();
        mutable.initFromEpochMilli(this.event.getTimeStamp(), 0);
        return mutable;
    }

    public StackTraceElement getSource() {
        LocationInfo info = this.event.getLocationInformation();
        return new StackTraceElement(info.getClassName(), info.getMethodName(), info.getFileName(), Integer.parseInt(info.getLineNumber()));
    }

    public String getThreadName() {
        return this.event.getThreadName();
    }

    public long getThreadId() {
        Thread thread = this.getThread();
        return thread != null ? thread.getId() : 0L;
    }

    public int getThreadPriority() {
        Thread thread = this.getThread();
        return thread != null ? thread.getPriority() : 0;
    }

    private Thread getThread() {
        if (this.thread == null && this.event.getThreadName() != null) {
            for (Thread thread : Thread.getAllStackTraces().keySet()) {
                if (!thread.getName().equals(this.event.getThreadName())) continue;
                this.thread = thread;
                return thread;
            }
        }
        return this.thread;
    }

    public Throwable getThrown() {
        ThrowableInformation throwableInformation = this.event.getThrowableInformation();
        return throwableInformation == null ? null : throwableInformation.getThrowable();
    }

    public ThrowableProxy getThrownProxy() {
        return null;
    }

    public boolean isEndOfBatch() {
        return false;
    }

    public boolean isIncludeLocation() {
        return false;
    }

    public void setEndOfBatch(boolean endOfBatch) {
    }

    public void setIncludeLocation(boolean locationRequired) {
    }

    public long getNanoTime() {
        return 0L;
    }

    private static class ContextDataMap
    extends HashMap<String, String>
    implements ReadOnlyStringMap {
        ContextDataMap(Map<String, String> map) {
            if (map != null) {
                super.putAll(map);
            }
        }

        public Map<String, String> toMap() {
            return this;
        }

        public boolean containsKey(String key) {
            return super.containsKey(key);
        }

        @Override
        public <V> void forEach(BiConsumer<String, ? super V> action) {
            super.forEach((? super K k, ? super V v) -> action.accept(k, v));
        }

        public <V, S> void forEach(TriConsumer<String, ? super V, S> action, S state) {
            super.forEach((? super K k, ? super V v) -> action.accept(k, v, state));
        }

        public <V> V getValue(String key) {
            return super.get(key);
        }
    }
}

