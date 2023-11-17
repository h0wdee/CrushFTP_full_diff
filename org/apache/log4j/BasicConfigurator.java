/*
 * Decompiled with CFR 0.152.
 */
package org.apache.log4j;

import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;

public class BasicConfigurator {
    protected BasicConfigurator() {
    }

    public static void configure() {
        LogManager.reconfigure();
    }

    public static void configure(Appender appender) {
    }

    public static void resetConfiguration() {
    }
}

