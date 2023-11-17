/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client.dfs;

import com.visuality.nq.client.dfs.Referral;
import java.util.Iterator;
import java.util.List;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Entry
implements Cloneable {
    protected String name;
    protected List<Referral> referrals;

    protected Entry clone() throws CloneNotSupportedException {
        Entry entry = (Entry)super.clone();
        return entry;
    }

    protected void setReferrals(List<Referral> referrals) {
        this.referrals = referrals;
    }

    public String getName() {
        return this.name;
    }

    public List<Referral> getReferrals() {
        return this.referrals;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected String printReferrals() {
        if (null == this.referrals) {
            return "null";
        }
        if (this.referrals.size() == 0) {
            return "empty";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<Referral> i$ = this.referrals.iterator();
        while (i$.hasNext()) {
            Referral ref;
            Referral referral = ref = i$.next();
            sb.append("(" + referral);
            sb.append("),");
        }
        return sb.toString();
    }

    public String toString() {
        return "Entry [name=" + this.name + ", referrals=" + this.printReferrals() + "]";
    }
}

