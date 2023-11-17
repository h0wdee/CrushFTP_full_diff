/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client.dfs;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Referral
implements Cloneable {
    protected int flags;
    protected long ttl;
    protected int serverType;
    protected String dfsPath;
    protected String netPath;

    public boolean isRootTargetReturned() {
        return this.serverType == 1;
    }

    public int getFlags() {
        return this.flags;
    }

    public long getTtl() {
        return this.ttl;
    }

    public String getPath() {
        return this.dfsPath;
    }

    public String getNode() {
        return this.netPath;
    }

    protected Referral clone() throws CloneNotSupportedException {
        Referral newRef = (Referral)super.clone();
        return newRef;
    }

    public String toString() {
        Date date = new Date(this.ttl * 1000L);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("d/M/y:h:m:s");
        return "Referral [flags=" + this.flags + ", ttl=" + this.ttl + " (" + dateFormatter.format(date) + ", serverType=" + this.serverType + ", dfsPath=" + this.dfsPath + ", netPath=" + this.netPath + "]";
    }
}

