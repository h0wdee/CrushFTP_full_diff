/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.core.Appender
 *  org.apache.logging.log4j.core.Filter
 *  org.apache.logging.log4j.core.Layout
 *  org.apache.logging.log4j.core.appender.RollingFileAppender
 *  org.apache.logging.log4j.core.appender.RollingFileAppender$Builder
 *  org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy
 *  org.apache.logging.log4j.core.appender.rolling.RolloverStrategy
 *  org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy
 *  org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy
 *  org.apache.logging.log4j.core.config.plugins.Plugin
 *  org.apache.logging.log4j.status.StatusLogger
 */
package org.apache.log4j.builders.appender;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.status.StatusLogger;
import org.w3c.dom.Element;

@Plugin(name="org.apache.log4j.DailyRollingFileAppender", category="Log4j Builder")
public class DailyRollingFileAppenderBuilder
extends AbstractBuilder
implements AppenderBuilder {
    private static final Logger LOGGER = StatusLogger.getLogger();

    public DailyRollingFileAppenderBuilder() {
    }

    public DailyRollingFileAppenderBuilder(String prefix, Properties props) {
        super(prefix, props);
    }

    @Override
    public Appender parseAppender(Element appenderElement, XmlConfiguration config) {
        String name = appenderElement.getAttribute("name");
        AtomicReference layout = new AtomicReference();
        AtomicReference filter = new AtomicReference();
        AtomicReference fileName = new AtomicReference();
        AtomicReference level = new AtomicReference();
        AtomicBoolean immediateFlush = new AtomicBoolean();
        AtomicBoolean append = new AtomicBoolean();
        AtomicBoolean bufferedIo = new AtomicBoolean();
        AtomicInteger bufferSize = new AtomicInteger(8192);
        XmlConfiguration.forEachElement(appenderElement.getChildNodes(), currentElement -> {
            block5 : switch (currentElement.getTagName()) {
                case "layout": {
                    layout.set(config.parseLayout((Element)currentElement));
                    break;
                }
                case "filter": {
                    filter.set(config.parseFilters((Element)currentElement));
                    break;
                }
                case "param": {
                    switch (currentElement.getAttribute("name")) {
                        case "File": {
                            fileName.set(currentElement.getAttribute("value"));
                            break block5;
                        }
                        case "Append": {
                            String bool = currentElement.getAttribute("value");
                            if (bool != null) {
                                append.set(Boolean.parseBoolean(bool));
                                break block5;
                            }
                            LOGGER.warn("No value provided for append parameter");
                            break block5;
                        }
                        case "BufferedIO": {
                            String bool = currentElement.getAttribute("value");
                            if (bool != null) {
                                bufferedIo.set(Boolean.parseBoolean(bool));
                                break block5;
                            }
                            LOGGER.warn("No value provided for bufferedIo parameter");
                            break block5;
                        }
                        case "BufferSize": {
                            String size = currentElement.getAttribute("value");
                            if (size != null) {
                                bufferSize.set(Integer.parseInt(size));
                                break block5;
                            }
                            LOGGER.warn("No value provide for bufferSize parameter");
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
        return this.createAppender(name, (Layout)layout.get(), (org.apache.log4j.spi.Filter)filter.get(), (String)fileName.get(), append.get(), immediateFlush.get(), (String)level.get(), bufferedIo.get(), bufferSize.get(), config);
    }

    @Override
    public Appender parseAppender(String name, String appenderPrefix, String layoutPrefix, String filterPrefix, Properties props, PropertiesConfiguration configuration) {
        Layout layout = configuration.parseLayout(layoutPrefix, name, props);
        org.apache.log4j.spi.Filter filter = configuration.parseAppenderFilters(props, filterPrefix, name);
        String fileName = this.getProperty("File");
        String level = this.getProperty("Threshold");
        boolean append = this.getBooleanProperty("Append");
        boolean immediateFlush = false;
        boolean bufferedIo = this.getBooleanProperty("BufferedIO");
        int bufferSize = Integer.parseInt(this.getProperty("BufferSize", "8192"));
        return this.createAppender(name, layout, filter, fileName, append, immediateFlush, level, bufferedIo, bufferSize, configuration);
    }

    private <T extends Log4j1Configuration> Appender createAppender(String name, Layout layout, org.apache.log4j.spi.Filter filter, String fileName, boolean append, boolean immediateFlush, String level, boolean bufferedIo, int bufferSize, T configuration) {
        LayoutAdapter fileLayout = null;
        if (bufferedIo) {
            immediateFlush = true;
        }
        if (layout instanceof LayoutWrapper) {
            fileLayout = ((LayoutWrapper)layout).getLayout();
        } else if (layout != null) {
            fileLayout = new LayoutAdapter(layout);
        }
        Filter fileFilter = this.buildFilters(level, filter);
        if (fileName == null) {
            LOGGER.warn("Unable to create File Appender, no file name provided");
            return null;
        }
        String filePattern = fileName + "%d{yyy-MM-dd}";
        TimeBasedTriggeringPolicy policy = TimeBasedTriggeringPolicy.newBuilder().withModulate(true).build();
        DefaultRolloverStrategy strategy = DefaultRolloverStrategy.newBuilder().withConfig(configuration).withMax(Integer.toString(Integer.MAX_VALUE)).build();
        return new AppenderWrapper((org.apache.logging.log4j.core.Appender)((RollingFileAppender.Builder)((RollingFileAppender.Builder)((RollingFileAppender.Builder)((RollingFileAppender.Builder)((RollingFileAppender.Builder)((RollingFileAppender.Builder)RollingFileAppender.newBuilder().setName(name)).setConfiguration(configuration)).setLayout((org.apache.logging.log4j.core.Layout)fileLayout)).setFilter(fileFilter)).withFileName(fileName).withBufferSize(bufferSize)).withImmediateFlush(immediateFlush)).withFilePattern(filePattern).withPolicy((TriggeringPolicy)policy).withStrategy((RolloverStrategy)strategy).build());
    }
}

