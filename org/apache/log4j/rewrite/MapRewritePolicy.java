/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.core.LogEvent
 *  org.apache.logging.log4j.core.impl.Log4jLogEvent
 *  org.apache.logging.log4j.core.impl.Log4jLogEvent$Builder
 *  org.apache.logging.log4j.message.MapMessage
 *  org.apache.logging.log4j.message.Message
 *  org.apache.logging.log4j.message.SimpleMessage
 *  org.apache.logging.log4j.util.SortedArrayStringMap
 *  org.apache.logging.log4j.util.StringMap
 */
package org.apache.log4j.rewrite;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.bridge.LogEventAdapter;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.rewrite.RewritePolicy;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.apache.logging.log4j.util.StringMap;

public class MapRewritePolicy
implements RewritePolicy {
    @Override
    public LoggingEvent rewrite(LoggingEvent source) {
        Object msg = source.getMessage();
        if (msg instanceof MapMessage || msg instanceof Map) {
            Log4jLogEvent event;
            HashMap props = source.getProperties() != null ? new HashMap(source.getProperties()) : new HashMap();
            Map eventProps = msg instanceof Map ? (Map)msg : ((MapMessage)msg).getData();
            SimpleMessage newMessage = null;
            Object newMsg = eventProps.get("message");
            if (newMsg != null) {
                newMessage = new SimpleMessage(newMsg.toString());
                for (Map.Entry entry : eventProps.entrySet()) {
                    if ("message".equals(entry.getKey())) continue;
                    props.put(entry.getKey(), entry.getValue().toString());
                }
            } else {
                return source;
            }
            if (source instanceof LogEventAdapter) {
                event = new Log4jLogEvent.Builder(((LogEventAdapter)source).getEvent()).setMessage((Message)newMessage).setContextData((StringMap)new SortedArrayStringMap(props)).build();
            } else {
                LocationInfo info = source.getLocationInformation();
                StackTraceElement element = new StackTraceElement(info.getClassName(), info.getMethodName(), info.getFileName(), Integer.parseInt(info.getLineNumber()));
                Thread thread = this.getThread(source.getThreadName());
                long threadId = thread != null ? thread.getId() : 0L;
                int threadPriority = thread != null ? thread.getPriority() : 0;
                event = Log4jLogEvent.newBuilder().setContextData((StringMap)new SortedArrayStringMap(props)).setLevel(OptionConverter.convertLevel(source.getLevel())).setLoggerFqcn(source.getFQNOfLoggerClass()).setMarker(null).setMessage((Message)newMessage).setSource(element).setLoggerName(source.getLoggerName()).setThreadName(source.getThreadName()).setThreadId(threadId).setThreadPriority(threadPriority).setThrown(source.getThrowableInformation().getThrowable()).setTimeMillis(source.getTimeStamp()).setNanoTime(0L).setThrownProxy(null).build();
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

