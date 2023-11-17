/*
 * Decompiled with CFR 0.152.
 */
package org.apache.log4j.spi;

import org.apache.log4j.bridge.FilterAdapter;
import org.apache.log4j.spi.LoggingEvent;

public abstract class Filter {
    private final FilterAdapter adapter;
    public static final int DENY = -1;
    public static final int NEUTRAL = 0;
    public static final int ACCEPT = 1;
    @Deprecated
    public Filter next;

    public Filter() {
        FilterAdapter filterAdapter = null;
        try {
            Class.forName("org.apache.logging.log4j.core.Filter");
            filterAdapter = new FilterAdapter(this);
        }
        catch (ClassNotFoundException classNotFoundException) {
            // empty catch block
        }
        this.adapter = filterAdapter;
    }

    public void activateOptions() {
    }

    public abstract int decide(LoggingEvent var1);

    public void setNext(Filter next) {
        this.next = next;
    }

    public Filter getNext() {
        return this.next;
    }
}

