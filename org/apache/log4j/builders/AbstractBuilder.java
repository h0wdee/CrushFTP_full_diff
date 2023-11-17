/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Level
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.core.Filter
 *  org.apache.logging.log4j.core.Filter$Result
 *  org.apache.logging.log4j.core.filter.CompositeFilter
 *  org.apache.logging.log4j.core.filter.ThresholdFilter
 *  org.apache.logging.log4j.core.lookup.ConfigurationStrSubstitutor
 *  org.apache.logging.log4j.core.lookup.StrSubstitutor
 *  org.apache.logging.log4j.status.StatusLogger
 */
package org.apache.log4j.builders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.function.BiConsumer;
import org.apache.log4j.bridge.FilterAdapter;
import org.apache.log4j.bridge.FilterWrapper;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.filter.CompositeFilter;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.lookup.ConfigurationStrSubstitutor;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.status.StatusLogger;

public abstract class AbstractBuilder {
    private static Logger LOGGER = StatusLogger.getLogger();
    protected static final String FILE_PARAM = "File";
    protected static final String APPEND_PARAM = "Append";
    protected static final String BUFFERED_IO_PARAM = "BufferedIO";
    protected static final String BUFFER_SIZE_PARAM = "BufferSize";
    protected static final String MAX_SIZE_PARAM = "MaxFileSize";
    protected static final String MAX_BACKUP_INDEX = "MaxBackupIndex";
    protected static final String RELATIVE = "RELATIVE";
    private final String prefix;
    private final Properties props;
    private final StrSubstitutor strSubstitutor;

    public AbstractBuilder() {
        this.prefix = null;
        this.props = new Properties();
        this.strSubstitutor = new ConfigurationStrSubstitutor(System.getProperties());
    }

    public AbstractBuilder(String prefix, Properties props) {
        this.prefix = prefix + ".";
        this.props = props;
        HashMap map = new HashMap();
        System.getProperties().forEach((BiConsumer<? super Object, ? super Object>)((BiConsumer<Object, Object>)(k, v) -> map.put(k.toString(), v.toString())));
        props.forEach((BiConsumer<? super Object, ? super Object>)((BiConsumer<Object, Object>)(k, v) -> map.put(k.toString(), v.toString())));
        this.strSubstitutor = new ConfigurationStrSubstitutor(map);
    }

    public String getProperty(String key) {
        return this.strSubstitutor.replace(this.props.getProperty(this.prefix + key));
    }

    public String getProperty(String key, String defaultValue) {
        return this.strSubstitutor.replace(this.props.getProperty(this.prefix + key, defaultValue));
    }

    public boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(this.strSubstitutor.replace(this.props.getProperty(this.prefix + key, Boolean.FALSE.toString())));
    }

    public int getIntegerProperty(String key, int defaultValue) {
        String value = this.getProperty(key);
        try {
            if (value != null) {
                return Integer.parseInt(value);
            }
        }
        catch (Exception ex) {
            LOGGER.warn("Error converting value {} of {} to an integer: {}", (Object)value, (Object)key, (Object)ex.getMessage());
        }
        return defaultValue;
    }

    public Properties getProperties() {
        return this.props;
    }

    protected Filter buildFilters(String level, org.apache.log4j.spi.Filter filter) {
        if (level != null && filter != null) {
            ArrayList<Object> filterList = new ArrayList<Object>();
            ThresholdFilter thresholdFilter = ThresholdFilter.createFilter((Level)OptionConverter.convertLevel(level, Level.TRACE), (Filter.Result)Filter.Result.NEUTRAL, (Filter.Result)Filter.Result.DENY);
            filterList.add(thresholdFilter);
            org.apache.log4j.spi.Filter f = filter;
            while (f != null) {
                if (filter instanceof FilterWrapper) {
                    filterList.add(((FilterWrapper)f).getFilter());
                } else {
                    filterList.add((Object)new FilterAdapter(f));
                }
                f = f.next;
            }
            return CompositeFilter.createFilters((Filter[])filterList.toArray(new Filter[0]));
        }
        if (level != null) {
            return ThresholdFilter.createFilter((Level)OptionConverter.convertLevel(level, Level.TRACE), (Filter.Result)Filter.Result.NEUTRAL, (Filter.Result)Filter.Result.DENY);
        }
        if (filter != null) {
            if (filter instanceof FilterWrapper) {
                return ((FilterWrapper)filter).getFilter();
            }
            return new FilterAdapter(filter);
        }
        return null;
    }
}

