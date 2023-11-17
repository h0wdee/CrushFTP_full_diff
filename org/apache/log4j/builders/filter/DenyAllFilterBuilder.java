/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.core.Filter
 *  org.apache.logging.log4j.core.config.plugins.Plugin
 *  org.apache.logging.log4j.core.filter.DenyAllFilter
 *  org.apache.logging.log4j.status.StatusLogger
 */
package org.apache.log4j.builders.filter;

import org.apache.log4j.bridge.FilterWrapper;
import org.apache.log4j.builders.filter.FilterBuilder;
import org.apache.log4j.config.PropertiesConfiguration;
import org.apache.log4j.xml.XmlConfiguration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.filter.DenyAllFilter;
import org.apache.logging.log4j.status.StatusLogger;
import org.w3c.dom.Element;

@Plugin(name="org.apache.log4j.varia.DenyAllFilter", category="Log4j Builder")
public class DenyAllFilterBuilder
implements FilterBuilder {
    private static final Logger LOGGER = StatusLogger.getLogger();

    @Override
    public org.apache.log4j.spi.Filter parseFilter(Element filterElement, XmlConfiguration config) {
        return new FilterWrapper((Filter)DenyAllFilter.newBuilder().build());
    }

    @Override
    public org.apache.log4j.spi.Filter parseFilter(PropertiesConfiguration config) {
        return new FilterWrapper((Filter)DenyAllFilter.newBuilder().build());
    }
}

