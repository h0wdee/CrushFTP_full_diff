/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.core.Filter
 */
package org.apache.log4j.bridge;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.logging.log4j.core.Filter;

public class FilterWrapper
extends org.apache.log4j.spi.Filter {
    private final Filter filter;

    public FilterWrapper(Filter filter) {
        this.filter = filter;
    }

    public Filter getFilter() {
        return this.filter;
    }

    @Override
    public int decide(LoggingEvent event) {
        return 0;
    }
}

