/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.core.LogEvent
 *  org.apache.logging.log4j.core.appender.rewrite.RewritePolicy
 */
package org.apache.log4j.bridge;

import org.apache.log4j.bridge.LogEventAdapter;
import org.apache.log4j.bridge.LogEventWrapper;
import org.apache.log4j.rewrite.RewritePolicy;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.logging.log4j.core.LogEvent;

public class RewritePolicyAdapter
implements org.apache.logging.log4j.core.appender.rewrite.RewritePolicy {
    private final RewritePolicy policy;

    public RewritePolicyAdapter(RewritePolicy policy) {
        this.policy = policy;
    }

    public LogEvent rewrite(LogEvent source) {
        LoggingEvent event = this.policy.rewrite(new LogEventAdapter(source));
        return event instanceof LogEventAdapter ? ((LogEventAdapter)event).getEvent() : new LogEventWrapper(event);
    }

    public RewritePolicy getPolicy() {
        return this.policy;
    }
}

