/*
 * Decompiled with CFR 0.152.
 */
package org.apache.log4j.xml;

import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;
import javax.xml.parsers.FactoryConfigurationError;
import org.apache.log4j.config.PropertySetter;
import org.apache.log4j.spi.LoggerRepository;
import org.w3c.dom.Element;

public class DOMConfigurator {
    public void doConfigure(String filename, LoggerRepository repository) {
    }

    public void doConfigure(URL url, LoggerRepository repository) {
    }

    public void doConfigure(InputStream inputStream, LoggerRepository repository) throws FactoryConfigurationError {
    }

    public void doConfigure(Reader reader, LoggerRepository repository) throws FactoryConfigurationError {
    }

    public void doConfigure(Element element, LoggerRepository repository) {
    }

    public static void configure(Element element) {
    }

    public static void configureAndWatch(String configFilename) {
    }

    public static void configureAndWatch(String configFilename, long delay) {
    }

    public static void configure(String filename) throws FactoryConfigurationError {
    }

    public static void configure(URL url) throws FactoryConfigurationError {
    }

    public static String subst(String value, Properties props) {
        return value;
    }

    public static void setParameter(Element elem, PropertySetter propSetter, Properties props) {
    }

    public static Object parseElement(Element element, Properties props, Class expectedClass) throws Exception {
        return null;
    }
}

