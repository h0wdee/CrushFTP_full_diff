/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import com.visuality.nq.resolve.NetbiosName;
import java.util.Iterator;
import java.util.Vector;

public class NameCache {
    Vector entries = new Vector();

    synchronized void releaseAllNames() {
        this.entries.clear();
    }

    synchronized void add(NetbiosName name, long eol, Vector ips) {
        if (null != name) {
            if (null != this.lookup(name)) {
                this.remove(name);
            }
            this.entries.add(new Entry(name, eol, ips));
        }
    }

    synchronized void remove(NetbiosName name) {
        Iterator iter = this.entries.iterator();
        while (iter.hasNext()) {
            Entry entry = (Entry)iter.next();
            if (!entry.name.equals(name)) continue;
            iter.remove();
        }
    }

    synchronized Vector lookup(NetbiosName name) {
        Iterator iter = this.entries.iterator();
        while (iter.hasNext()) {
            Entry entry = (Entry)iter.next();
            if (!entry.name.equals(name)) continue;
            if (entry.eol > 0L && entry.eol < System.currentTimeMillis()) {
                iter.remove();
                continue;
            }
            return entry.ips;
        }
        return null;
    }

    private static class Entry {
        private NetbiosName name;
        private long eol;
        private Vector ips;

        private Entry(NetbiosName name, long eol, Vector ips) {
            this.name = name;
            this.eol = eol;
            this.ips = (Vector)ips.clone();
        }
    }
}

