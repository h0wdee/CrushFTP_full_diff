/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msfscc.fsctl;

import com.hierynomus.protocol.commons.Charsets;
import com.hierynomus.protocol.commons.buffer.Buffer;
import java.util.concurrent.TimeUnit;

public class FsCtlPipeWaitRequest {
    private final TimeUnit timeoutUnit;
    private String name;
    private long timeout;
    private boolean timeoutSpecified;

    public FsCtlPipeWaitRequest(String name, long timeout, TimeUnit timeoutUnit, boolean timeoutSpecified) {
        this.name = name;
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        this.timeoutSpecified = timeoutSpecified;
    }

    public String getName() {
        return this.name;
    }

    public long getTimeout() {
        return this.timeout;
    }

    public TimeUnit getTimeoutUnit() {
        return this.timeoutUnit;
    }

    public void write(Buffer buffer) {
        buffer.putUInt64(this.timeoutSpecified ? this.timeoutUnit.toMillis(this.timeout) / 100L : 0L);
        int nameLengthPos = buffer.wpos();
        buffer.putUInt32(0L);
        buffer.putBoolean(this.timeoutSpecified);
        buffer.putByte((byte)0);
        long nameStartPos = buffer.wpos();
        buffer.putString(this.name, Charsets.UTF_16);
        int endPos = buffer.wpos();
        buffer.wpos(nameLengthPos);
        buffer.putUInt32((long)endPos - nameStartPos);
        buffer.wpos(endPos);
    }
}
