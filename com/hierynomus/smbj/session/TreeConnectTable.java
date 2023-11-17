/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.session;

import com.hierynomus.smbj.share.Share;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class TreeConnectTable {
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Map<Long, Share> lookupById = new HashMap<Long, Share>();
    private Map<String, Share> lookupByShareName = new HashMap<String, Share>();

    TreeConnectTable() {
    }

    void register(Share share) {
        this.lock.writeLock().lock();
        try {
            this.lookupById.put(share.getTreeConnect().getTreeId(), share);
            this.lookupByShareName.put(share.getTreeConnect().getShareName(), share);
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }

    Collection<Share> getOpenTreeConnects() {
        this.lock.readLock().lock();
        try {
            ArrayList<Share> arrayList = new ArrayList<Share>(this.lookupById.values());
            return arrayList;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    Share getTreeConnect(long treeConnectId) {
        this.lock.readLock().lock();
        try {
            Share share = this.lookupById.get(treeConnectId);
            return share;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    Share getTreeConnect(String shareName) {
        this.lock.readLock().lock();
        try {
            Share share = this.lookupByShareName.get(shareName);
            return share;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void closed(long treeConnectId) {
        this.lock.writeLock().lock();
        try {
            Share share = this.lookupById.remove(treeConnectId);
            if (share != null) {
                this.lookupByShareName.remove(share.getTreeConnect().getShareName());
            }
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }
}

