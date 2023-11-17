/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.core.Filter$Result
 *  org.apache.logging.log4j.core.LogEvent
 *  org.apache.logging.log4j.core.filter.AbstractFilter
 */
package org.apache.log4j.bridge;

import org.apache.log4j.bridge.LogEventAdapter;
import org.apache.log4j.spi.Filter;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;

public class FilterAdapter
extends AbstractFilter {
    private final Filter filter;

    public FilterAdapter(Filter filter) {
        this.filter = filter;
    }

    public Filter.Result filter(LogEvent event) {
        LogEventAdapter loggingEvent = new LogEventAdapter(event);
        Filter next = this.filter;
        while (next != null) {
            switch (this.filter.decide(loggingEvent)) {
                case 1: {
                    return Filter.Result.ACCEPT;
                }
                case -1: {
                    return Filter.Result.DENY;
                }
            }
            next = this.filter.getNext();
        }
        return Filter.Result.NEUTRAL;
    }

    public Filter getFilter() {
        return this.filter;
    }

    public void start() {
        this.filter.activateOptions();
    }
}

