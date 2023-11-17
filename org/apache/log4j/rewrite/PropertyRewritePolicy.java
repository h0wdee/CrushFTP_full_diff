/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.core.LogEvent
 *  org.apache.logging.log4j.core.impl.Log4jLogEvent
 *  org.apache.logging.log4j.core.impl.Log4jLogEvent$Builder
 *  org.apache.logging.log4j.message.Message
 *  org.apache.logging.log4j.message.SimpleMessage
 *  org.apache.logging.log4j.util.SortedArrayStringMap
 *  org.apache.logging.log4j.util.StringMap
 */
package org.apache.log4j.rewrite;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.log4j.bridge.LogEventAdapter;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.rewrite.RewritePolicy;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.apache.logging.log4j.util.StringMap;

public class PropertyRewritePolicy
implements RewritePolicy {
    private Map<String, String> properties = Collections.EMPTY_MAP;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setProperties(String properties) {
        HashMap<String, String> newMap = new HashMap<String, String>();
        StringTokenizer pairs = new StringTokenizer(properties, ",");
        while (pairs.hasMoreTokens()) {
            StringTokenizer entry = new StringTokenizer(pairs.nextToken(), "=");
            newMap.put(entry.nextElement().toString().trim(), entry.nextElement().toString().trim());
        }
        PropertyRewritePolicy propertyRewritePolicy = this;
        synchronized (propertyRewritePolicy) {
            this.properties = newMap;
        }
    }

    @Override
    public LoggingEvent rewrite(LoggingEvent source) {
        if (!this.properties.isEmpty()) {
            Log4jLogEvent event;
            HashMap<String, String> rewriteProps = source.getProperties() != null ? new HashMap<String, String>(source.getProperties()) : new HashMap();
            for (Map.Entry<String, String> entry : this.properties.entrySet()) {
                if (rewriteProps.containsKey(entry.getKey())) continue;
                rewriteProps.put(entry.getKey(), entry.getValue());
            }
            if (source instanceof LogEventAdapter) {
                event = new Log4jLogEvent.Builder(((LogEventAdapter)source).getEvent()).setContextData((StringMap)new SortedArrayStringMap(rewriteProps)).build();
            } else {
                LocationInfo info = source.getLocationInformation();
                StackTraceElement element = new StackTraceElement(info.getClassName(), info.getMethodName(), info.getFileName(), Integer.parseInt(info.getLineNumber()));
                Thread thread = this.getThread(source.getThreadName());
                long threadId = thread != null ? thread.getId() : 0L;
                int threadPriority = thread != null ? thread.getPriority() : 0;
                event = Log4jLogEvent.newBuilder().setContextData((StringMap)new SortedArrayStringMap(rewriteProps)).setLevel(OptionConverter.convertLevel(source.getLevel())).setLoggerFqcn(source.getFQNOfLoggerClass()).setMarker(null).setMessage((Message)new SimpleMessage(source.getRenderedMessage())).setSource(element).setLoggerName(source.getLoggerName()).setThreadName(source.getThreadName()).setThreadId(threadId).setThreadPriority(threadPriority).setThrown(source.getThrowableInformation().getThrowable()).setTimeMillis(source.getTimeStamp()).setNanoTime(0L).setThrownProxy(null).build();
            }
            return new LogEventAdapter((LogEvent)event);
        }
        return source;
    }

    private Thread getThread(String name) {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (!thread.getName().equals(name)) continue;
            return thread;
        }
        return null;
    }
}

