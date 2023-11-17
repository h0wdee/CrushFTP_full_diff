/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.TraceLog;
import java.util.concurrent.ConcurrentHashMap;

public class NamedRepository
extends ConcurrentHashMap {
    private static final long serialVersionUID = 2564849036967876728L;

    public void dump() {
        for (Object item : this.values()) {
            TraceLog.get().message(item, 1000);
        }
    }

    public String toString() {
        if (this.values() == null) {
            return "Named Repository is null";
        }
        StringBuilder sb = new StringBuilder("NamedRepository [");
        for (Object item : this.values()) {
            sb.append(item.toString());
        }
        sb.append(" ]");
        return sb.toString();
    }
}

