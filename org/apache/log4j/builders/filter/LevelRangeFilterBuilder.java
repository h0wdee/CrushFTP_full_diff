/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Level
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.core.Filter
 *  org.apache.logging.log4j.core.Filter$Result
 *  org.apache.logging.log4j.core.config.plugins.Plugin
 *  org.apache.logging.log4j.core.filter.LevelRangeFilter
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
import org.apache.logging.log4j.core.filter.LevelRangeFilter;
import org.apache.logging.log4j.status.StatusLogger;
import org.w3c.dom.Element;

@Plugin(name="org.apache.log4j.varia.LevelRangeFilter", category="Log4j Builder")
public class LevelRangeFilterBuilder
extends AbstractBuilder
implements FilterBuilder {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final String LEVEL_MAX = "LevelMax";
    private static final String LEVEL_MIN = "LevelMin";
    private static final String ACCEPT_ON_MATCH = "AcceptOnMatch";

    public LevelRangeFilterBuilder() {
    }

    public LevelRangeFilterBuilder(String prefix, Properties props) {
        super(prefix, props);
    }

    @Override
    public org.apache.log4j.spi.Filter parseFilter(Element filterElement, XmlConfiguration config) {
        AtomicReference levelMax = new AtomicReference();
        AtomicReference levelMin = new AtomicReference();
        AtomicBoolean acceptOnMatch = new AtomicBoolean();
        XmlConfiguration.forEachElement(filterElement.getElementsByTagName("param"), currentElement -> {
            if (currentElement.getTagName().equals("param")) {
                switch (currentElement.getAttribute("name")) {
                    case "LevelMax": {
                        levelMax.set(currentElement.getAttribute("value"));
                        break;
                    }
                    case "LevelMin": {
                        levelMax.set(currentElement.getAttribute("value"));
                        break;
                    }
                    case "AcceptOnMatch": {
                        acceptOnMatch.set(Boolean.parseBoolean(currentElement.getAttribute("value")));
                    }
                }
            }
        });
        return this.createFilter((String)levelMax.get(), (String)levelMin.get(), acceptOnMatch.get());
    }

    @Override
    public org.apache.log4j.spi.Filter parseFilter(PropertiesConfiguration config) {
        String levelMax = this.getProperty(LEVEL_MAX);
        String levelMin = this.getProperty(LEVEL_MIN);
        boolean acceptOnMatch = this.getBooleanProperty(ACCEPT_ON_MATCH);
        return this.createFilter(levelMax, levelMin, acceptOnMatch);
    }

    private org.apache.log4j.spi.Filter createFilter(String levelMax, String levelMin, boolean acceptOnMatch) {
        Level max = Level.FATAL;
        Level min = Level.TRACE;
        if (levelMax != null) {
            max = Level.toLevel((String)levelMax, (Level)Level.FATAL);
        }
        if (levelMin != null) {
            min = Level.toLevel((String)levelMin, (Level)Level.DEBUG);
        }
        Filter.Result onMatch = acceptOnMatch ? Filter.Result.ACCEPT : Filter.Result.NEUTRAL;
        return new FilterWrapper((Filter)LevelRangeFilter.createFilter((Level)min, (Level)max, (Filter.Result)onMatch, (Filter.Result)Filter.Result.DENY));
    }
}

