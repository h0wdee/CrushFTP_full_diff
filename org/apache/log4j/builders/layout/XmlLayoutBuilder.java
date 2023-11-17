/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.core.Layout
 *  org.apache.logging.log4j.core.config.plugins.Plugin
 *  org.apache.logging.log4j.core.layout.XmlLayout
 *  org.apache.logging.log4j.core.layout.XmlLayout$Builder
 *  org.apache.logging.log4j.status.StatusLogger
 */
package org.apache.log4j.builders.layout;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.log4j.Layout;
import org.apache.log4j.bridge.LayoutWrapper;
import org.apache.log4j.builders.AbstractBuilder;
import org.apache.log4j.builders.layout.LayoutBuilder;
import org.apache.log4j.config.PropertiesConfiguration;
import org.apache.log4j.xml.XmlConfiguration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.XmlLayout;
import org.apache.logging.log4j.status.StatusLogger;
import org.w3c.dom.Element;

@Plugin(name="org.apache.log4j.xml.XMLLayout", category="Log4j Builder")
public class XmlLayoutBuilder
extends AbstractBuilder
implements LayoutBuilder {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final String LOCATION_INFO = "LocationInfo";
    private static final String PROPERTIES = "Properties";

    public XmlLayoutBuilder() {
    }

    public XmlLayoutBuilder(String prefix, Properties props) {
        super(prefix, props);
    }

    @Override
    public Layout parseLayout(Element layoutElement, XmlConfiguration config) {
        AtomicBoolean properties = new AtomicBoolean();
        AtomicBoolean locationInfo = new AtomicBoolean();
        XmlConfiguration.forEachElement(layoutElement.getElementsByTagName("param"), currentElement -> {
            if (PROPERTIES.equalsIgnoreCase(currentElement.getAttribute("name"))) {
                properties.set(Boolean.parseBoolean(currentElement.getAttribute("value")));
            } else if (LOCATION_INFO.equalsIgnoreCase(currentElement.getAttribute("name"))) {
                locationInfo.set(Boolean.parseBoolean(currentElement.getAttribute("value")));
            }
        });
        return this.createLayout(properties.get(), locationInfo.get());
    }

    @Override
    public Layout parseLayout(PropertiesConfiguration config) {
        boolean properties = this.getBooleanProperty(PROPERTIES);
        boolean locationInfo = this.getBooleanProperty(LOCATION_INFO);
        return this.createLayout(properties, locationInfo);
    }

    private Layout createLayout(boolean properties, boolean locationInfo) {
        return new LayoutWrapper((org.apache.logging.log4j.core.Layout<?>)((XmlLayout.Builder)((XmlLayout.Builder)XmlLayout.newBuilder().setLocationInfo(locationInfo)).setProperties(properties)).build());
    }
}

