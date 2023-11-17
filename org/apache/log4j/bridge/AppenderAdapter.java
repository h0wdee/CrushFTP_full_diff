/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.core.Filter
 *  org.apache.logging.log4j.core.Layout
 *  org.apache.logging.log4j.core.LogEvent
 *  org.apache.logging.log4j.core.appender.AbstractAppender
 *  org.apache.logging.log4j.core.config.Property
 *  org.apache.logging.log4j.core.filter.CompositeFilter
 */
package org.apache.log4j.bridge;

import java.io.Serializable;
import java.util.ArrayList;
import org.apache.log4j.Appender;
import org.apache.log4j.bridge.FilterAdapter;
import org.apache.log4j.bridge.LogEventAdapter;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.filter.CompositeFilter;

public class AppenderAdapter {
    private final Appender appender;
    private final Adapter adapter;

    public AppenderAdapter(Appender appender) {
        this.appender = appender;
        FilterAdapter appenderFilter = null;
        if (appender.getFilter() != null) {
            if (appender.getFilter().getNext() != null) {
                ArrayList<FilterAdapter> filters = new ArrayList<FilterAdapter>();
                for (org.apache.log4j.spi.Filter filter = appender.getFilter(); filter != null; filter = filter.getNext()) {
                    filters.add(new FilterAdapter(filter));
                }
                appenderFilter = CompositeFilter.createFilters((Filter[])filters.toArray(Filter.EMPTY_ARRAY));
            } else {
                appenderFilter = new FilterAdapter(appender.getFilter());
            }
        }
        this.adapter = new Adapter(appender.getName(), (Filter)appenderFilter, null, true, null);
    }

    public Adapter getAdapter() {
        return this.adapter;
    }

    public class Adapter
    extends AbstractAppender {
        protected Adapter(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties) {
            super(name, filter, layout, ignoreExceptions, properties);
        }

        public void append(LogEvent event) {
            AppenderAdapter.this.appender.doAppend(new LogEventAdapter(event));
        }

        public void stop() {
            AppenderAdapter.this.appender.close();
        }

        public Appender getAppender() {
            return AppenderAdapter.this.appender;
        }
    }
}

