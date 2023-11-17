/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.NamedRepository;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;

public abstract class Item {
    private String name;
    private NamedRepository repository;
    private int locks = 0;
    private boolean findable = false;
    private boolean beingDisposed = false;

    protected synchronized void dispose() throws NqException {
        if (null == this.repository) {
            throw new NqException("Entity not in repository", -13);
        }
        this.repository.remove(this.name);
    }

    public Item(String name) {
        this.name = name;
        this.repository = new NamedRepository();
    }

    public synchronized String getName() {
        return this.name;
    }

    public NamedRepository getRepository() {
        return this.repository;
    }

    public synchronized void put(NamedRepository repository) {
        this.repository = repository;
        repository.put(this.name, this);
    }

    protected abstract void dump();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void check() throws NqException {
        boolean callUnlockCallBack = false;
        Item item = this;
        synchronized (item) {
            if (0 == this.locks && !this.beingDisposed) {
                callUnlockCallBack = true;
                this.findable = false;
            }
        }
        if (callUnlockCallBack) {
            this.unlockCallback();
            this.findable = true;
        }
    }

    public synchronized boolean canDispose() throws NqException {
        if (this.locks < 0) {
            TraceLog.get().error("Negative number of locks");
            return true;
        }
        return this.locks == 0;
    }

    public synchronized void unlock() throws NqException {
        if (0 > --this.locks) {
            TraceLog.get().error("Negative number of locks");
        }
    }

    protected abstract void unlockCallback() throws NqException;

    public synchronized void lock() {
        ++this.locks;
    }

    public synchronized void initLock() {
        TraceLog.get().message("init the locks to 0", 2000);
        this.locks = 0;
    }

    public boolean isFindable() {
        return this.findable;
    }

    public synchronized void setFindable(boolean findable) {
        this.findable = findable;
    }

    public synchronized int getLocks() {
        return this.locks;
    }

    public synchronized void setName(String name) {
        this.name = name;
    }
}

