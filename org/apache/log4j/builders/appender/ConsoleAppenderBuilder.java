/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.core.Appender
 *  org.apache.logging.log4j.core.Filter
 *  org.apache.logging.log4j.core.Layout
 *  org.apache.logging.log4j.core.appender.ConsoleAppender
 *  org.apache.logging.log4j.core.appender.ConsoleAppender$Builder
 *  org.apache.logging.log4j.core.appender.ConsoleAppender$Target
 *  org.apache.logging.log4j.core.config.plugins.Plugin
 *  org.apache.logging.log4j.status.StatusLogger
 */
package org.apache.log4j.builders.appender;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.bridge.AppenderWrapper;
import org.apache.log4j.bridge.LayoutAdapter;
import org.apache.log4j.bridge.LayoutWrapper;
import org.apache.log4j.builders.AbstractBuilder;
import org.apache.log4j.builders.appender.AppenderBuilder;
import org.apache.log4j.config.Log4j1Configuration;
import org.apache.log4j.config.PropertiesConfiguration;
import org.apache.log4j.xml.XmlConfiguration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.status.StatusLogger;
import org.w3c.dom.Element;

@Plugin(name="org.apache.log4j.ConsoleAppender", category="Log4j Builder")
public class ConsoleAppenderBuilder
extends AbstractBuilder
implements AppenderBuilder {
    private static final String SYSTEM_OUT = "System.out";
    private static final String SYSTEM_ERR = "System.err";
    private static final String TARGET = "target";
    private static final Logger LOGGER = StatusLogger.getLogger();

    public ConsoleAppenderBuilder() {
    }

    public ConsoleAppenderBuilder(String prefix, Properties props) {
        super(prefix, props);
    }

    @Override
    public Appender parseAppender(Element appenderElement, XmlConfiguration config) {
        String name = appenderElement.getAttribute("name");
        AtomicReference<String> target = new AtomicReference<String>(SYSTEM_OUT);
        AtomicReference layout = new AtomicReference();
        AtomicReference filters = new AtomicReference(new ArrayList());
        AtomicReference level = new AtomicReference();
        XmlConfiguration.forEachElement(appenderElement.getChildNodes(), currentElement -> {
            block5 : switch (currentElement.getTagName()) {
                case "layout": {
                    layout.set(config.parseLayout((Element)currentElement));
                    break;
                }
                case "filter": {
                    ((List)filters.get()).add(config.parseFilters((Element)currentElement));
                    break;
                }
                case "param": {
                    switch (currentElement.getAttribute("name")) {
                        case "target": {
                            String value = currentElement.getAttribute("value");
                            if (value == null) {
                                LOGGER.warn("No value supplied for target parameter. Defaulting to System.out.");
                                break block5;
                            }
                            switch (value) {
                                case "System.out": {
                                    target.set(SYSTEM_OUT);
                                    break block5;
                                }
                                case "System.err": {
                                    target.set(SYSTEM_ERR);
                                    break block5;
                                }
                            }
                            LOGGER.warn("Invalid value \"{}\" for target parameter. Using default of System.out", (Object)value);
                            break block5;
                        }
                        case "Threshold": {
                            String value = currentElement.getAttribute("value");
                            if (value == null) {
                                LOGGER.warn("No value supplied for Threshold parameter, ignoring.");
                                break block5;
                            }
                            level.set(value);
                            break block5;
                        }
                    }
                }
            }
        });
        org.apache.log4j.spi.Filter head = null;
        org.apache.log4j.spi.Filter current = null;
        for (org.apache.log4j.spi.Filter f : (List)filters.get()) {
            if (head == null) {
                head = f;
            } else {
                current.next = f;
            }
            current = f;
        }
        return this.createAppender(name, (Layout)layout.get(), head, (String)level.get(), target.get(), config);
    }

    @Override
    public Appender parseAppender(String name, String appenderPrefix, String layoutPrefix, String filterPrefix, Properties props, PropertiesConfiguration configuration) {
        Layout layout = configuration.parseLayout(layoutPrefix, name, props);
        org.apache.log4j.spi.Filter filter = configuration.parseAppenderFilters(props, filterPrefix, name);
        String level = this.getProperty("Threshold");
        String target = this.getProperty(TARGET);
        return this.createAppender(name, layout, filter, level, target, configuration);
    }

    private <T extends Log4j1Configuration> Appender createAppender(String name, Layout layout, org.apache.log4j.spi.Filter filter, String level, String target, T configuration) {
        LayoutAdapter consoleLayout = null;
        if (layout instanceof LayoutWrapper) {
            consoleLayout = ((LayoutWrapper)layout).getLayout();
        } else if (layout != null) {
            consoleLayout = new LayoutAdapter(layout);
        }
        Filter consoleFilter = this.buildFilters(level, filter);
        ConsoleAppender.Target consoleTarget = SYSTEM_ERR.equals(target) ? ConsoleAppender.Target.SYSTEM_ERR : ConsoleAppender.Target.SYSTEM_OUT;
        return new AppenderWrapper((org.apache.logging.log4j.core.Appender)((ConsoleAppender.Builder)((ConsoleAppender.Builder)((ConsoleAppender.Builder)((ConsoleAppender.Builder)ConsoleAppender.newBuilder().setName(name)).setTarget(consoleTarget).setLayout((org.apache.logging.log4j.core.Layout)consoleLayout)).setFilter(consoleFilter)).setConfiguration(configuration)).build());
    }
}

