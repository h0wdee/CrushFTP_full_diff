/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client.dfs;

import com.visuality.nq.client.Share;
import com.visuality.nq.client.dfs.DfsEntry;
import com.visuality.nq.client.dfs.Referral;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DfsReferral
extends Referral
implements Cloneable {
    int numPathConsumed;
    long originalTtl;
    Share share;
    boolean isConnected;
    boolean isIOPerformed;
    long lastIOStatus;
    DfsEntry master;
    public boolean isGood = false;

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public int getServerType() {
        return this.serverType;
    }

    public void setServerType(int serverType) {
        this.serverType = serverType;
    }

    public String getDfsPath() {
        return this.dfsPath;
    }

    public void setDfsPath(String dfsPath) {
        this.dfsPath = dfsPath;
    }

    public String getNetPath() {
        return this.netPath;
    }

    public void setNetPath(String netPath) {
        this.netPath = netPath;
    }

    protected DfsReferral clone() throws CloneNotSupportedException {
        DfsReferral newRef = (DfsReferral)super.clone();
        return newRef;
    }

    public String toString() {
        Date date = new Date(this.ttl * 1000L);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("d/M/y:h:m:s");
        return "Referral [isGood = " + this.isGood + ", numPathConsumed = " + this.numPathConsumed + ", dfsPath=" + this.dfsPath + ", netPath=" + this.netPath + ", isConnected=" + this.isConnected + ", ttl = " + this.ttl + " (" + dateFormatter.format(date) + "), originalTtl = " + this.originalTtl + "]";
    }
}

