/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.auth.MD5;
import com.visuality.nq.common.NqException;

public abstract class Credentials {
    protected Object key;
    protected String domain;
    protected Updater updater;
    protected Object hook;

    protected Credentials(Object key, String domain, Updater updater, Object hook) {
        this.updater = updater;
        this.hook = hook;
        this.calculateKey(key);
        this.domain = this.calculateDomain(domain);
    }

    protected Credentials(String user, String password, String domain, Updater updater, Object hook) {
        this.updater = updater;
        this.hook = hook;
        String userTmp = null == user ? null : user.toLowerCase();
        String domainTmp = null == domain ? null : domain.toLowerCase();
        this.calculateKey(userTmp + password + domainTmp);
        this.domain = this.calculateDomain(domainTmp);
    }

    protected void calculateKey(Object key) {
        if (key instanceof String && ((String)key).length() > 0) {
            try {
                byte[] keyBuf = new byte[16];
                MD5.cmMD5(keyBuf, ((String)key).getBytes(), ((String)key).getBytes().length);
                this.key = new String(keyBuf);
            }
            catch (NqException e) {
                this.key = key;
            }
        } else {
            this.key = key;
        }
    }

    protected String calculateDomain(String domain) {
        if (null == domain || domain.equals(".")) {
            domain = "";
        }
        return domain;
    }

    protected Credentials(Object key, String domain) {
        this.calculateKey(key);
        this.domain = this.calculateDomain(domain);
    }

    protected Credentials(String user, String password, String domain) {
        String userTmp = null == user ? null : user.toLowerCase();
        String domainTmp = null == domain ? null : domain.toLowerCase();
        this.calculateKey(userTmp + password + domainTmp);
        this.domain = this.calculateDomain(domainTmp);
    }

    protected void setKey(Object key) {
        this.calculateKey(key);
    }

    public Object getKey() {
        return this.key;
    }

    public String getDomain() {
        return this.domain;
    }

    protected void setDomain(String domain) {
        this.domain = domain;
    }

    public abstract String getUser();

    public abstract Credentials cloneCred();

    public Updater getUpdater() {
        return this.updater;
    }

    public void setUpdater(Updater updater) {
        this.updater = updater;
    }

    public Object getHook() {
        return this.hook;
    }

    public void setHook(Object hook) {
        this.hook = hook;
    }

    public static interface Updater {
        public Credentials update(Credentials var1, Object var2);
    }
}

