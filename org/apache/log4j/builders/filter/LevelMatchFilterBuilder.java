/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Level
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.core.Filter
 *  org.apache.logging.log4j.core.Filter$Result
 *  org.apache.logging.log4j.core.config.plugins.Plugin
 *  org.apache.logging.log4j.core.filter.LevelMatchFilter
 *  org.apache.logging.log4j.core.filter.LevelMatchFilter$Builder
 *  org.apache.logging.log4j.status.StatusLogger
 */
package org.apache.log4j.builders.filter;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.log4j.bridge.FilterWrapper;
import org.apache.log4j.builders.AbstractBuilder;
import org.apache.log4j.builders.filter.FilterBuilder;
import org.apache.log4j.config.PropertiesConfiguration;
import org.apache.log4j.xml.XmlConfiguration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.filter.LevelMatchFilter;
import org.apache.logging.log4j.status.StatusLogger;
import org.w3c.dom.Element;

@Plugin(name="org.apache.log4j.varia.LevelMatchFilter", category="Log4j Builder")
public class LevelMatchFilterBuilder
extends AbstractBuilder
implements FilterBuilder {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final String LEVEL = "LevelToMatch";
    private static final String ACCEPT_ON_MATCH = "AcceptOnMatch";

    public LevelMatchFilterBuilder() {
    }

    public LevelMatchFilterBuilder(String prefix, Properties props) {
        super(prefix, props);
    }

    @Override
    public org.apache.log4j.spi.Filter parseFilter(Element filterElement, XmlConfiguration config) {
        AtomicReference level = new AtomicReference();
        AtomicBoolean acceptOnMatch = new AtomicBoolean();
        XmlConfiguration.forEachElement(filterElement.getElementsByTagName("param"), currentElement -> {
            if (currentElement.getTagName().equals("param")) {
                switch (currentElement.getAttribute("name")) {
                    case "LevelToMatch": {
                        level.set(currentElement.getAttribute("value"));
                        break;
                    }
                    case "AcceptOnMatch": {
                        acceptOnMatch.set(Boolean.parseBoolean(currentElement.getAttribute("value")));
                    }
                }
            }
        });
        return this.createFilter((String)level.get(), acceptOnMatch.get());
    }

    @Override
    public org.apache.log4j.spi.Filter parseFilter(PropertiesConfiguration config) {
        String level = this.getProperty(LEVEL);
        boolean acceptOnMatch = this.getBooleanProperty(ACCEPT_ON_MATCH);
        return this.createFilter(level, acceptOnMatch);
    }

    private org.apache.log4j.spi.Filter createFilter(String level, boolean acceptOnMatch) {
        Level lvl = Level.ERROR;
        if (level != null) {
            lvl = Level.toLevel((String)level, (Level)Level.ERROR);
        }
        Filter.Result onMatch = acceptOnMatch ? Filter.Result.ACCEPT : Filter.Result.DENY;
        return new FilterWrapper((Filter)((LevelMatchFilter.Builder)((LevelMatchFilter.Builder)LevelMatchFilter.newBuilder().setLevel(lvl).setOnMatch(onMatch)).setOnMismatch(Filter.Result.NEUTRAL)).build());
    }
}

