/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.core.Appender
 *  org.apache.logging.log4j.core.Filter
 *  org.apache.logging.log4j.core.Layout
 *  org.apache.logging.log4j.core.appender.SocketAppender
 *  org.apache.logging.log4j.core.appender.SocketAppender$Builder
 *  org.apache.logging.log4j.core.config.Configuration
 *  org.apache.logging.log4j.core.config.plugins.Plugin
 *  org.apache.logging.log4j.core.layout.SyslogLayout
 *  org.apache.logging.log4j.core.layout.SyslogLayout$Builder
 *  org.apache.logging.log4j.core.net.Facility
 *  org.apache.logging.log4j.core.net.Protocol
 *  org.apache.logging.log4j.status.StatusLogger
 */
package org.apache.log4j.builders.appender;

import java.util.Properties;
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
import org.apache.logging.log4j.core.appender.SocketAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.SyslogLayout;
import org.apache.logging.log4j.core.net.Facility;
import org.apache.logging.log4j.core.net.Protocol;
import org.apache.logging.log4j.status.StatusLogger;
import org.w3c.dom.Element;

@Plugin(name="org.apache.log4j.net.SyslogAppender", category="Log4j Builder")
public class SyslogAppenderBuilder
extends AbstractBuilder
implements AppenderBuilder {
    private static final String DEFAULT_HOST = "localhost";
    private static int DEFAULT_PORT = 514;
    private static final String DEFAULT_FACILITY = "LOCAL0";
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final String FACILITY_PARAM = "Facility";
    private static final String SYSLOG_HOST_PARAM = "SyslogHost";
    private static final String PROTOCOL_PARAM = "protocol";

    public SyslogAppenderBuilder() {
    }

    public SyslogAppenderBuilder(String prefix, Properties props) {
        super(prefix, props);
    }

    @Override
    public Appender parseAppender(Element appenderElement, XmlConfiguration config) {
        String name = appenderElement.getAttribute("name");
        AtomicReference layout = new AtomicReference();
        AtomicReference filter = new AtomicReference();
        AtomicReference facility = new AtomicReference();
        AtomicReference level = new AtomicReference();
        AtomicReference host = new AtomicReference();
        AtomicReference protocol = new AtomicReference();
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
                        case "SyslogHost": {
                            host.set(currentElement.getAttribute("value"));
                            break block5;
                        }
                        case "Facility": {
                            facility.set(currentElement.getAttribute("value"));
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
                        case "protocol": {
                            protocol.set(Protocol.valueOf((String)currentElement.getAttribute("value")));
                        }
                    }
                }
            }
        });
        return this.createAppender(name, config, (Layout)layout.get(), (String)facility.get(), (org.apache.log4j.spi.Filter)filter.get(), (String)host.get(), (String)level.get(), (Protocol)protocol.get());
    }

    @Override
    public Appender parseAppender(String name, String appenderPrefix, String layoutPrefix, String filterPrefix, Properties props, PropertiesConfiguration configuration) {
        org.apache.log4j.spi.Filter filter = configuration.parseAppenderFilters(props, filterPrefix, name);
        Layout layout = configuration.parseLayout(layoutPrefix, name, props);
        String level = this.getProperty("Threshold");
        String facility = this.getProperty(FACILITY_PARAM, DEFAULT_FACILITY);
        String syslogHost = this.getProperty(SYSLOG_HOST_PARAM, "localhost:" + DEFAULT_PORT);
        String protocol = this.getProperty(PROTOCOL_PARAM, Protocol.TCP.name());
        return this.createAppender(name, configuration, layout, facility, filter, syslogHost, level, Protocol.valueOf((String)protocol));
    }

    private Appender createAppender(String name, Log4j1Configuration configuration, Layout layout, String facility, org.apache.log4j.spi.Filter filter, String syslogHost, String level, Protocol protocol) {
        AtomicReference<String> host = new AtomicReference<String>();
        AtomicInteger port = new AtomicInteger();
        this.resolveSyslogHost(syslogHost, host, port);
        LayoutAdapter appenderLayout = layout instanceof LayoutWrapper ? ((LayoutWrapper)layout).getLayout() : (layout != null ? new LayoutAdapter(layout) : ((SyslogLayout.Builder)SyslogLayout.newBuilder().setFacility(Facility.toFacility((String)facility)).setConfiguration((Configuration)configuration)).build());
        Filter fileFilter = this.buildFilters(level, filter);
        return new AppenderWrapper((org.apache.logging.log4j.core.Appender)((SocketAppender.Builder)((SocketAppender.Builder)((SocketAppender.Builder)((SocketAppender.Builder)((SocketAppender.Builder)((SocketAppender.Builder)((SocketAppender.Builder)SocketAppender.newBuilder().setName(name)).setConfiguration((Configuration)configuration)).setLayout((org.apache.logging.log4j.core.Layout)appenderLayout)).setFilter(fileFilter)).withPort(port.get())).withProtocol(protocol)).withHost(host.get())).build());
    }

    private void resolveSyslogHost(String syslogHost, AtomicReference<String> host, AtomicInteger port) {
        String[] parts = syslogHost.split(":");
        if (parts.length == 1) {
            host.set(parts[0]);
            port.set(DEFAULT_PORT);
        } else if (parts.length == 2) {
            host.set(parts[0]);
            port.set(Integer.parseInt(parts[1]));
        } else {
            LOGGER.warn("Invalid {} setting: {}. Using default.", (Object)SYSLOG_HOST_PARAM, (Object)syslogHost);
            host.set(DEFAULT_HOST);
            port.set(DEFAULT_PORT);
        }
    }
}

