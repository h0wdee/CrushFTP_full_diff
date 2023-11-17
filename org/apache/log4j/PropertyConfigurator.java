/*
 * Decompiled with CFR 0.152.
 */
package org.apache.log4j;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.apache.log4j.spi.LoggerRepository;

public class PropertyConfigurator {
    public void doConfigure(String configFileName, LoggerRepository hierarchy) {
    }

    public void doConfigure(Properties properties, LoggerRepository hierarchy) {
    }

    public void doConfigure(InputStream inputStream, LoggerRepository hierarchy) {
    }

    public void doConfigure(URL configURL, LoggerRepository hierarchy) {
    }

    public static void configure(String configFileName) {
    }

    public static void configure(URL configURL) {
    }

    public static void configure(InputStream inputStream) {
    }

    public static void configure(Properties properties) {
    }

    public static void configureAndWatch(String configFilename) {
    }

    public static void configureAndWatch(String configFilename, long delay) {
    }
}

