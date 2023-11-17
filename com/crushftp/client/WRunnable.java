/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import java.util.Properties;

public class WRunnable
implements Runnable {
    Runnable r = null;
    Properties p = new Properties();

    public WRunnable() {
    }

    public WRunnable(Runnable r, WRunnable wr) {
        this.r = r;
        this.p = wr.p;
    }

    @Override
    public void run() {
        this.r.run();
    }

    public void put(String key, Object val) {
        this.p.put(key, val);
    }

    public Object get(String key) {
        return this.p.get(key);
    }
}

