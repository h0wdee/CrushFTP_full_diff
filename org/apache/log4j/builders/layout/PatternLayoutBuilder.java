/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.core.Layout
 *  org.apache.logging.log4j.core.config.Configuration
 *  org.apache.logging.log4j.core.config.plugins.Plugin
 *  org.apache.logging.log4j.core.config.plugins.PluginAliases
 *  org.apache.logging.log4j.core.layout.PatternLayout
 *  org.apache.logging.log4j.status.StatusLogger
 */
package org.apache.log4j.builders.layout;

import java.util.Properties;
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
import org.apache.logging.log4j.core.config.plugins.PluginAliases;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.status.StatusLogger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Plugin(name="org.apache.log4j.PatternLayout", category="Log4j Builder")
@PluginAliases(value={"org.apache.log4j.EnhancedPatternLayout"})
public class PatternLayoutBuilder
extends AbstractBuilder
implements LayoutBuilder {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final String PATTERN = "ConversionPattern";

    public PatternLayoutBuilder() {
    }

    public PatternLayoutBuilder(String prefix, Properties props) {
        super(prefix, props);
    }

    @Override
    public Layout parseLayout(Element layoutElement, XmlConfiguration config) {
        NodeList params = layoutElement.getElementsByTagName("param");
        int length = params.getLength();
        String pattern = null;
        for (int index = 0; index < length; ++index) {
            Element currentElement;
            Node currentNode = params.item(index);
            if (currentNode.getNodeType() != 1 || !(currentElement = (Element)currentNode).getTagName().equals("param") || !PATTERN.equalsIgnoreCase(currentElement.getAttribute("name"))) continue;
            pattern = currentElement.getAttribute("value");
            break;
        }
        return this.createLayout(pattern, config);
    }

    @Override
    public Layout parseLayout(PropertiesConfiguration config) {
        String pattern = this.getProperty(PATTERN);
        return this.createLayout(pattern, config);
    }

    private Layout createLayout(String pattern, Log4j1Configuration config) {
        if (pattern == null) {
            LOGGER.info("No pattern provided for pattern layout, using default pattern");
            pattern = "%m%n";
        }
        return new LayoutWrapper((org.apache.logging.log4j.core.Layout<?>)PatternLayout.newBuilder().withPattern(pattern.replace("%x", "%ndc").replace("%X", "%properties")).withConfiguration((Configuration)config).build());
    }
}

