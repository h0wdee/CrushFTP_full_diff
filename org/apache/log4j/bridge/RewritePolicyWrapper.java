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

public class RewritePolicyWrapper
implements RewritePolicy {
    private final org.apache.logging.log4j.core.appender.rewrite.RewritePolicy policy;

    public RewritePolicyWrapper(org.apache.logging.log4j.core.appender.rewrite.RewritePolicy policy) {
        this.policy = policy;
    }

    @Override
    public LoggingEvent rewrite(LoggingEvent source) {
        LogEventWrapper event = source instanceof LogEventAdapter ? ((LogEventAdapter)source).getEvent() : new LogEventWrapper(source);
        return new LogEventAdapter(this.policy.rewrite((LogEvent)event));
    }

    public org.apache.logging.log4j.core.appender.rewrite.RewritePolicy getPolicy() {
        return this.policy;
    }
}

