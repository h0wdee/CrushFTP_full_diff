/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.logging;

import com.maverick.logging.MaverickLogger;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class MaverickLoggerFactory
implements ILoggerFactory {
    static MaverickLoggerFactory instance;
    Logger logger = new MaverickLogger();

    @Override
    public Logger getLogger(String arg0) {
        return this.logger;
    }

    public static MaverickLoggerFactory getInstance() {
        return instance == null ? (instance = new MaverickLoggerFactory()) : instance;
    }
}

