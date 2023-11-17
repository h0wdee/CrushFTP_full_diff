/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.core.LoggerContext
 *  org.apache.logging.log4j.core.config.Configuration
 *  org.apache.logging.log4j.core.config.ConfigurationFactory
 *  org.apache.logging.log4j.core.config.ConfigurationSource
 *  org.apache.logging.log4j.core.config.Order
 *  org.apache.logging.log4j.core.config.plugins.Plugin
 *  org.apache.logging.log4j.status.StatusLogger
 *  org.apache.logging.log4j.util.PropertiesUtil
 */
package org.apache.log4j.xml;

import org.apache.log4j.xml.XmlConfiguration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.PropertiesUtil;

@Plugin(name="Log4j1XmlConfigurationFactory", category="ConfigurationFactory")
@Order(value=2)
public class XmlConfigurationFactory
extends ConfigurationFactory {
    public static final String FILE_EXTENSION = ".xml";
    private static final Logger LOGGER = StatusLogger.getLogger();
    protected static final String TEST_PREFIX = "log4j-test";
    protected static final String DEFAULT_PREFIX = "log4j";

    protected String[] getSupportedTypes() {
        if (!PropertiesUtil.getProperties().getBooleanProperty("log4j1.compatibility", Boolean.FALSE.booleanValue())) {
            return null;
        }
        return new String[]{FILE_EXTENSION};
    }

    public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource source) {
        int interval = PropertiesUtil.getProperties().getIntegerProperty("log4j1.monitorInterval", 0);
        return new XmlConfiguration(loggerContext, source, interval);
    }

    protected String getTestPrefix() {
        return TEST_PREFIX;
    }

    protected String getDefaultPrefix() {
        return DEFAULT_PREFIX;
    }

    protected String getVersion() {
        return "1";
    }
}

