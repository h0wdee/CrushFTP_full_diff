/*
 * Decompiled with CFR 0.152.
 */
package org.slf4j.impl;

import com.maverick.logging.MaverickLoggerFactory;
import org.slf4j.ILoggerFactory;

public class StaticLoggerBinder {
    static StaticLoggerBinder instance;
    public static String REQUESTED_API_VERSION;

    public ILoggerFactory getLoggerFactory() {
        return MaverickLoggerFactory.getInstance();
    }

    public String getLoggerFactoryClassStr() {
        return "com.maverick.logging.MaverickLoggerFactory";
    }

    public static StaticLoggerBinder getSingleton() {
        return instance == null ? (instance = new StaticLoggerBinder()) : instance;
    }

    static {
        REQUESTED_API_VERSION = "1.7.2";
    }
}

