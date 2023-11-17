/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.core.LifeCycle$State
 *  org.apache.logging.log4j.core.LoggerContext
 *  org.apache.logging.log4j.core.config.AbstractConfiguration
 *  org.apache.logging.log4j.core.config.Configuration
 *  org.apache.logging.log4j.core.config.ConfigurationSource
 *  org.apache.logging.log4j.core.config.Reconfigurable
 */
package org.apache.log4j.config;

import org.apache.log4j.builders.BuilderManager;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Reconfigurable;

public class Log4j1Configuration
extends AbstractConfiguration
implements Reconfigurable {
    public static final String MONITOR_INTERVAL = "log4j1.monitorInterval";
    public static final String APPENDER_REF_TAG = "appender-ref";
    public static final String THRESHOLD_PARAM = "Threshold";
    public static final String INHERITED = "inherited";
    public static final String NULL = "null";
    protected final BuilderManager manager = new BuilderManager();

    public Log4j1Configuration(LoggerContext loggerContext, ConfigurationSource source, int monitorIntervalSeconds) {
        super(loggerContext, source);
        this.initializeWatchers(this, source, monitorIntervalSeconds);
    }

    public BuilderManager getBuilderManager() {
        return this.manager;
    }

    public void initialize() {
        this.getStrSubstitutor().setConfiguration((Configuration)this);
        this.getConfigurationStrSubstitutor().setConfiguration((Configuration)this);
        super.getScheduler().start();
        this.doConfigure();
        this.setState(LifeCycle.State.INITIALIZED);
        LOGGER.debug("Configuration {} initialized", (Object)this);
    }

    public Configuration reconfigure() {
        return null;
    }
}

