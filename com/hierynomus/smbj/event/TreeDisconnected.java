/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.event;

import com.hierynomus.smbj.event.SMBEvent;
import com.hierynomus.smbj.event.SessionEvent;

public class TreeDisconnected
extends SessionEvent
implements SMBEvent {
    private long treeId;

    public TreeDisconnected(long sessionId, long treeId) {
        super(sessionId);
        this.treeId = treeId;
    }

    public long getTreeId() {
        return this.treeId;
    }
}

