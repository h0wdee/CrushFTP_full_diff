/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client.dfs;

import com.visuality.nq.client.dfs.DfsReferral;
import com.visuality.nq.client.dfs.Entry;
import com.visuality.nq.client.dfs.Referral;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class DfsEntry
extends Entry
implements Cloneable {
    protected List<DfsReferral> referrals = Collections.synchronizedList(new LinkedList());
    boolean isRoot;
    long ttl;
    boolean isExactMatch;
    int lastIOStatus;

    public List<DfsReferral> getDfsReferrals() {
        return this.referrals;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String printReferrals() {
        if (null == this.referrals) {
            return "null";
        }
        if (this.referrals.size() == 0) {
            return "empty";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<DfsReferral> i$ = this.referrals.iterator();
        while (i$.hasNext()) {
            DfsReferral ref;
            DfsReferral referral = ref = i$.next();
            sb.append("(" + referral);
            sb.append("),");
        }
        return sb.toString();
    }

    @Override
    public List<Referral> getReferrals() {
        ArrayList<Referral> refList = new ArrayList<Referral>();
        for (DfsReferral realRef : this.referrals) {
            Referral ref = new Referral();
            ref.flags = realRef.flags;
            ref.dfsPath = realRef.dfsPath;
            ref.netPath = realRef.netPath;
            ref.serverType = realRef.serverType;
            ref.ttl = realRef.ttl;
            refList.add(ref);
        }
        return refList;
    }

    protected void setDfsReferrals(List<DfsReferral> referrals) {
        this.referrals = referrals;
    }

    @Override
    protected DfsEntry clone() throws CloneNotSupportedException {
        DfsEntry dfsEntry = (DfsEntry)super.clone();
        return dfsEntry;
    }

    @Override
    public String toString() {
        Date date = new Date(this.ttl * 1000L);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("d/M/y:h:m:s");
        return "DfsEntry [name=" + this.name + ", isRoot=" + this.isRoot + ", ttl=" + this.ttl + " (" + dateFormatter.format(date) + "), isExactMatch=" + this.isExactMatch + ", lastIOStatus=" + this.lastIOStatus + ", referrals=" + this.printReferrals() + "]";
    }
}

