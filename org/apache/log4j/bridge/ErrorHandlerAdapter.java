/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.core.ErrorHandler
 *  org.apache.logging.log4j.core.LogEvent
 */
package org.apache.log4j.bridge;

import org.apache.log4j.bridge.LogEventAdapter;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.LogEvent;

public class ErrorHandlerAdapter
implements ErrorHandler {
    private final org.apache.log4j.spi.ErrorHandler errorHandler;

    public ErrorHandlerAdapter(org.apache.log4j.spi.ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public org.apache.log4j.spi.ErrorHandler getHandler() {
        return this.errorHandler;
    }

    public void error(String msg) {
        this.errorHandler.error(msg);
    }

    public void error(String msg, Throwable t) {
        if (t instanceof Exception) {
            this.errorHandler.error(msg, (Exception)t, 0);
        } else {
            this.errorHandler.error(msg);
        }
    }

    public void error(String msg, LogEvent event, Throwable t) {
        if (t == null || t instanceof Exception) {
            this.errorHandler.error(msg, (Exception)t, 0, new LogEventAdapter(event));
        } else {
            this.errorHandler.error(msg);
        }
    }
}

