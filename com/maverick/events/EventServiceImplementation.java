/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.events;

import com.maverick.events.Event;
import com.maverick.events.EventException;
import com.maverick.events.EventListener;
import com.maverick.events.EventService;
import com.maverick.events.EventTrigger;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventServiceImplementation
implements EventService {
    private static Logger log = LoggerFactory.getLogger(EventServiceImplementation.class);
    private static EventService INSTANCE = new EventServiceImplementation();
    private static boolean got;
    private static StackTraceElement[] gotStack;
    protected final Hashtable<String, EventListener> keyedListeners;
    protected Collection<EventListener> globalListeners = new ConcurrentLinkedQueue<EventListener>();
    protected List<Class> eventCodeDescriptors = new ArrayList<Class>();
    boolean processAllEventsOnEventException = false;
    Map<Integer, String> cachedEventNames = new HashMap<Integer, String>();

    protected EventServiceImplementation() {
        this.keyedListeners = new Hashtable();
        try {
            this.registerEventCodeDescriptor(Class.forName("com.maverick.events.J2SSHEventCodes"));
        }
        catch (ClassNotFoundException classNotFoundException) {
            // empty catch block
        }
        try {
            this.registerEventCodeDescriptor(Class.forName("com.maverick.sshd.events.SSHDEventCodes"));
        }
        catch (ClassNotFoundException classNotFoundException) {
            // empty catch block
        }
    }

    protected static void setInstance(EventService eventService) {
        if (got) {
            StringBuilder trace = new StringBuilder();
            for (StackTraceElement el : gotStack) {
                if (trace.length() > 0) {
                    trace.append('\n');
                }
                trace.append(el);
            }
            throw new IllegalArgumentException(EventServiceImplementation.class + ".setInstance() must be called before the first getInstace() which was called from :-\n" + trace.toString());
        }
        INSTANCE = eventService;
    }

    public static EventService getInstance() {
        if (!got) {
            got = true;
            gotStack = Thread.currentThread().getStackTrace();
        }
        return INSTANCE;
    }

    @Override
    public void registerEventCodeDescriptor(Class cls) {
        this.eventCodeDescriptors.add(cls);
    }

    @Override
    public String getEventName(Integer id) {
        if (this.cachedEventNames.containsKey(id)) {
            return this.cachedEventNames.get(id);
        }
        for (Class cls : this.eventCodeDescriptors) {
            for (Field f : cls.getFields()) {
                if ((f.getModifiers() & 0) != 0 || !f.getType().isAssignableFrom(Integer.TYPE)) continue;
                try {
                    int val = (Integer)f.get(null);
                    if (val != id) continue;
                    this.cachedEventNames.put(id, f.getName());
                    return f.getName();
                }
                catch (IllegalArgumentException illegalArgumentException) {
                }
                catch (IllegalAccessException illegalAccessException) {
                    // empty catch block
                }
            }
        }
        return Integer.toHexString(id);
    }

    @Override
    @Deprecated
    public synchronized void addListener(String threadPrefix, EventListener listener) {
        if (threadPrefix.trim().equals("")) {
            this.globalListeners.add(listener);
        } else {
            this.keyedListeners.put(threadPrefix.trim(), listener);
        }
    }

    @Override
    @Deprecated
    public synchronized void removeListener(String threadPrefix) {
        this.keyedListeners.remove(threadPrefix);
    }

    @Override
    public void fireEvent(Event evt) {
        Object obj;
        if (evt == null) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s - Firing %s success=%s", evt.getUuid(), this.getEventName(evt.getId()), evt.getState() ? "true" : "false"));
        }
        if ((obj = evt.getAttribute("CONNECTION")) != null && obj instanceof EventTrigger) {
            ((EventTrigger)obj).fireEvent(evt);
        }
        EventException lastException = null;
        for (EventListener mListener : this.globalListeners) {
            try {
                mListener.processEvent(evt);
            }
            catch (Throwable t) {
                if (t instanceof EventException) {
                    lastException = (EventException)t;
                    if (this.processAllEventsOnEventException) continue;
                    throw lastException;
                }
                if (!log.isWarnEnabled()) continue;
                log.warn("Caught exception from event listener", t);
            }
        }
        String sourceThread = Thread.currentThread().getName();
        Enumeration<String> keys = this.keyedListeners.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            try {
                String prefix = "";
                if (sourceThread.indexOf(45) <= -1 || !key.startsWith(prefix = sourceThread.substring(0, sourceThread.indexOf(45)))) continue;
                EventListener mListener = this.keyedListeners.get(key);
                mListener.processEvent(evt);
            }
            catch (Throwable t) {
                if (t instanceof EventException) {
                    lastException = (EventException)t;
                    if (this.processAllEventsOnEventException) continue;
                    throw lastException;
                }
                if (!log.isWarnEnabled()) continue;
                log.warn("Caught exception from event listener", t);
            }
        }
        if (this.processAllEventsOnEventException && lastException != null) {
            throw lastException;
        }
    }

    public void setProcessAllEventsOnEventException(boolean processAllEventsOnEventException) {
        this.processAllEventsOnEventException = processAllEventsOnEventException;
    }

    @Override
    public void addListener(EventListener listener) {
        this.globalListeners.add(listener);
    }

    @Override
    public void removeListener(EventListener listener) {
        this.globalListeners.remove(listener);
    }
}

