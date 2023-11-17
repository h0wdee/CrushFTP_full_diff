/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.core.Layout
 *  org.apache.logging.log4j.core.config.Configuration
 *  org.apache.logging.log4j.core.config.plugins.Plugin
 *  org.apache.logging.log4j.core.layout.PatternLayout
 *  org.apache.logging.log4j.status.StatusLogger
 */
package org.apache.log4j.builders.layout;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.log4j.Layout;
import org.apache.log4j.bridge.LayoutWrapper;
import org.apache.log4j.builders.AbstractBuilder;
import org.apache.log4j.builders.layout.LayoutBuilder;
import org.apache.log4j.config.Log4j1Configuration;
import org.apache.log4j.config.PropertiesConfiguration;
import org.apache.log4j.xml.XmlConfiguration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.status.StatusLogger;
import org.w3c.dom.Element;

@Plugin(name="org.apache.log4j.TTCCLayout", category="Log4j Builder")
public class TTCCLayoutBuilder
extends AbstractBuilder
implements LayoutBuilder {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final String THREAD_PRINTING_PARAM = "ThreadPrinting";
    private static final String CATEGORY_PREFIXING_PARAM = "CategoryPrefixing";
    private static final String CONTEXT_PRINTING_PARAM = "ContextPrinting";
    private static final String DATE_FORMAT_PARAM = "DateFormat";
    private static final String TIMEZONE_FORMAT = "TimeZone";

    public TTCCLayoutBuilder() {
    }

    public TTCCLayoutBuilder(String prefix, Properties props) {
        super(prefix, props);
    }

    @Override
    public Layout parseLayout(Element layoutElement, XmlConfiguration config) {
        AtomicBoolean threadPrinting = new AtomicBoolean();
        AtomicBoolean categoryPrefixing = new AtomicBoolean();
        AtomicBoolean contextPrinting = new AtomicBoolean();
        AtomicReference dateFormat = new AtomicReference();
        AtomicReference timezone = new AtomicReference();
        XmlConfiguration.forEachElement(layoutElement.getElementsByTagName("param"), currentElement -> {
            if (currentElement.getTagName().equals("param")) {
                switch (currentElement.getAttribute("name")) {
                    case "ThreadPrinting": {
                        threadPrinting.set(Boolean.parseBoolean(currentElement.getAttribute("value")));
                        break;
                    }
                    case "CategoryPrefixing": {
                        categoryPrefixing.set(Boolean.parseBoolean(currentElement.getAttribute("value")));
                        break;
                    }
                    case "ContextPrinting": {
                        contextPrinting.set(Boolean.parseBoolean(currentElement.getAttribute("value")));
                        break;
                    }
                    case "DateFormat": {
                        dateFormat.set(currentElement.getAttribute("value"));
                        break;
                    }
                    case "TimeZone": {
                        timezone.set(currentElement.getAttribute("value"));
                    }
                }
            }
        });
        return this.createLayout(threadPrinting.get(), categoryPrefixing.get(), contextPrinting.get(), (String)dateFormat.get(), (String)timezone.get(), config);
    }

    @Override
    public Layout parseLayout(PropertiesConfiguration config) {
        boolean threadPrinting = this.getBooleanProperty(THREAD_PRINTING_PARAM);
        boolean categoryPrefixing = this.getBooleanProperty(CATEGORY_PREFIXING_PARAM);
        boolean contextPrinting = this.getBooleanProperty(CONTEXT_PRINTING_PARAM);
        String dateFormat = this.getProperty(DATE_FORMAT_PARAM);
        String timezone = this.getProperty(TIMEZONE_FORMAT);
        return this.createLayout(threadPrinting, categoryPrefixing, contextPrinting, dateFormat, timezone, config);
    }

    private Layout createLayout(boolean threadPrinting, boolean categoryPrefixing, boolean contextPrinting, String dateFormat, String timezone, Log4j1Configuration config) {
        StringBuilder sb = new StringBuilder();
        if (dateFormat != null) {
            if ("RELATIVE".equalsIgnoreCase(dateFormat)) {
                sb.append("%r ");
            } else {
                sb.append("%d{").append(dateFormat).append("}");
                if (timezone != null) {
                    sb.append("{").append(timezone).append("}");
                }
                sb.append(" ");
            }
        }
        if (threadPrinting) {
            sb.append("[%t] ");
        }
        sb.append("%p ");
        if (categoryPrefixing) {
            sb.append("%c ");
        }
        if (contextPrinting) {
            sb.append("%notEmpty{%ndc }");
        }
        sb.append("- %m%n");
        return new LayoutWrapper((org.apache.logging.log4j.core.Layout<?>)PatternLayout.newBuilder().withPattern(sb.toString()).withConfiguration((Configuration)config).build());
    }
}

