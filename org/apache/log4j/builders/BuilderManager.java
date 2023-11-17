/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.core.config.plugins.util.PluginManager
 *  org.apache.logging.log4j.core.config.plugins.util.PluginType
 *  org.apache.logging.log4j.status.StatusLogger
 *  org.apache.logging.log4j.util.LoaderUtil
 */
package org.apache.log4j.builders;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.builders.AbstractBuilder;
import org.apache.log4j.builders.appender.AppenderBuilder;
import org.apache.log4j.builders.filter.FilterBuilder;
import org.apache.log4j.builders.layout.LayoutBuilder;
import org.apache.log4j.builders.rewrite.RewritePolicyBuilder;
import org.apache.log4j.config.PropertiesConfiguration;
import org.apache.log4j.rewrite.RewritePolicy;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.xml.XmlConfiguration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LoaderUtil;
import org.w3c.dom.Element;

public class BuilderManager {
    public static final String CATEGORY = "Log4j Builder";
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final Map<String, PluginType<?>> plugins;
    private static Class<?>[] constructorParams = new Class[]{String.class, Properties.class};

    public BuilderManager() {
        PluginManager manager = new PluginManager(CATEGORY);
        manager.collectPlugins();
        this.plugins = manager.getPlugins();
    }

    public Appender parseAppender(String className, Element appenderElement, XmlConfiguration config) {
        PluginType<?> plugin = this.plugins.get(className.toLowerCase());
        if (plugin != null) {
            try {
                AppenderBuilder builder = (AppenderBuilder)LoaderUtil.newInstanceOf((Class)plugin.getPluginClass());
                return builder.parseAppender(appenderElement, config);
            }
            catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
                LOGGER.warn("Unable to load plugin: {} due to: {}", (Object)plugin.getKey(), (Object)ex.getMessage());
            }
        }
        return null;
    }

    public Appender parseAppender(String name, String className, String prefix, String layoutPrefix, String filterPrefix, Properties props, PropertiesConfiguration config) {
        AppenderBuilder builder;
        PluginType<?> plugin = this.plugins.get(className.toLowerCase());
        if (plugin != null && (builder = (AppenderBuilder)this.createBuilder(plugin, prefix, props)) != null) {
            return builder.parseAppender(name, prefix, layoutPrefix, filterPrefix, props, config);
        }
        return null;
    }

    public Filter parseFilter(String className, Element filterElement, XmlConfiguration config) {
        PluginType<?> plugin = this.plugins.get(className.toLowerCase());
        if (plugin != null) {
            try {
                FilterBuilder builder = (FilterBuilder)LoaderUtil.newInstanceOf((Class)plugin.getPluginClass());
                return builder.parseFilter(filterElement, config);
            }
            catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
                LOGGER.warn("Unable to load plugin: {} due to: {}", (Object)plugin.getKey(), (Object)ex.getMessage());
            }
        }
        return null;
    }

    public Filter parseFilter(String className, String filterPrefix, Properties props, PropertiesConfiguration config) {
        FilterBuilder builder;
        PluginType<?> plugin = this.plugins.get(className.toLowerCase());
        if (plugin != null && (builder = (FilterBuilder)this.createBuilder(plugin, filterPrefix, props)) != null) {
            return builder.parseFilter(config);
        }
        return null;
    }

    public Layout parseLayout(String className, Element layoutElement, XmlConfiguration config) {
        PluginType<?> plugin = this.plugins.get(className.toLowerCase());
        if (plugin != null) {
            try {
                LayoutBuilder builder = (LayoutBuilder)LoaderUtil.newInstanceOf((Class)plugin.getPluginClass());
                return builder.parseLayout(layoutElement, config);
            }
            catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
                LOGGER.warn("Unable to load plugin: {} due to: {}", (Object)plugin.getKey(), (Object)ex.getMessage());
            }
        }
        return null;
    }

    public Layout parseLayout(String className, String layoutPrefix, Properties props, PropertiesConfiguration config) {
        LayoutBuilder builder;
        PluginType<?> plugin = this.plugins.get(className.toLowerCase());
        if (plugin != null && (builder = (LayoutBuilder)this.createBuilder(plugin, layoutPrefix, props)) != null) {
            return builder.parseLayout(config);
        }
        return null;
    }

    public RewritePolicy parseRewritePolicy(String className, Element rewriteElement, XmlConfiguration config) {
        PluginType<?> plugin = this.plugins.get(className.toLowerCase());
        if (plugin != null) {
            try {
                RewritePolicyBuilder builder = (RewritePolicyBuilder)LoaderUtil.newInstanceOf((Class)plugin.getPluginClass());
                return builder.parseRewritePolicy(rewriteElement, config);
            }
            catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
                LOGGER.warn("Unable to load plugin: {} due to: {}", (Object)plugin.getKey(), (Object)ex.getMessage());
            }
        }
        return null;
    }

    public RewritePolicy parseRewritePolicy(String className, String policyPrefix, Properties props, PropertiesConfiguration config) {
        RewritePolicyBuilder builder;
        PluginType<?> plugin = this.plugins.get(className.toLowerCase());
        if (plugin != null && (builder = (RewritePolicyBuilder)this.createBuilder(plugin, policyPrefix, props)) != null) {
            return builder.parseRewritePolicy(config);
        }
        return null;
    }

    private <T extends AbstractBuilder> T createBuilder(PluginType<?> plugin, String prefix, Properties props) {
        try {
            Class clazz = plugin.getPluginClass();
            if (AbstractBuilder.class.isAssignableFrom(clazz)) {
                Constructor constructor = clazz.getConstructor(constructorParams);
                return (T)((AbstractBuilder)constructor.newInstance(prefix, props));
            }
            AbstractBuilder builder = (AbstractBuilder)LoaderUtil.newInstanceOf((Class)clazz);
            return (T)builder;
        }
        catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
            LOGGER.warn("Unable to load plugin: {} due to: {}", (Object)plugin.getKey(), (Object)ex.getMessage());
            return null;
        }
    }
}

