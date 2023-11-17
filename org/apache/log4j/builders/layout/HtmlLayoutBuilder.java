/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.core.Layout
 *  org.apache.logging.log4j.core.config.plugins.Plugin
 *  org.apache.logging.log4j.core.layout.HtmlLayout
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
import org.apache.log4j.config.PropertiesConfiguration;
import org.apache.log4j.xml.XmlConfiguration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.HtmlLayout;
import org.apache.logging.log4j.status.StatusLogger;
import org.w3c.dom.Element;

@Plugin(name="org.apache.log4j.HTMLLayout", category="Log4j Builder")
public class HtmlLayoutBuilder
extends AbstractBuilder
implements LayoutBuilder {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final String TITLE = "Title";
    private static final String LOCATION_INFO = "LocationInfo";

    public HtmlLayoutBuilder() {
    }

    public HtmlLayoutBuilder(String prefix, Properties props) {
        super(prefix, props);
    }

    @Override
    public Layout parseLayout(Element layoutElement, XmlConfiguration config) {
        AtomicReference title = new AtomicReference();
        AtomicBoolean locationInfo = new AtomicBoolean();
        XmlConfiguration.forEachElement(layoutElement.getElementsByTagName("param"), currentElement -> {
            if (currentElement.getTagName().equals("param")) {
                if (TITLE.equalsIgnoreCase(currentElement.getAttribute("name"))) {
                    title.set(currentElement.getAttribute("value"));
                } else if (LOCATION_INFO.equalsIgnoreCase(currentElement.getAttribute("name"))) {
                    locationInfo.set(Boolean.parseBoolean(currentElement.getAttribute("value")));
                }
            }
        });
        return this.createLayout((String)title.get(), locationInfo.get());
    }

    @Override
    public Layout parseLayout(PropertiesConfiguration config) {
        String title = this.getProperty(TITLE);
        boolean locationInfo = this.getBooleanProperty(LOCATION_INFO);
        return this.createLayout(title, locationInfo);
    }

    private Layout createLayout(String title, boolean locationInfo) {
        return new LayoutWrapper((org.apache.logging.log4j.core.Layout<?>)HtmlLayout.newBuilder().withTitle(title).withLocationInfo(locationInfo).build());
    }
}

