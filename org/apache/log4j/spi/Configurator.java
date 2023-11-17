/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.core.LoggerContext
 */
package org.apache.log4j.spi;

import java.io.InputStream;
import java.net.URL;
import org.apache.logging.log4j.core.LoggerContext;

public interface Configurator {
    public static final String INHERITED = "inherited";
    public static final String NULL = "null";

    public void doConfigure(InputStream var1, LoggerContext var2);

    public void doConfigure(URL var1, LoggerContext var2);
}

