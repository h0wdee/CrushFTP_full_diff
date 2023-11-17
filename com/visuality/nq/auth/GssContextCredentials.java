/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.common.Blob;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;

public class GssContextCredentials
extends Credentials {
    private Blob gssApiToken = new Blob();
    private Blob sessionKey = new Blob();

    public GssContextCredentials(Blob gssApiToken, Blob sessionKey, String domain) throws NqException {
        super("", domain);
        if (null == sessionKey || null == sessionKey.data) {
            throw new NqException("Invalid sessionKey input", -20);
        }
        if (null == gssApiToken || null == gssApiToken.data) {
            throw new NqException("Invalid gssApiToken input", -20);
        }
        super.setKey(new String(sessionKey.data));
        this.gssApiToken.data = (byte[])gssApiToken.data.clone();
        this.gssApiToken.len = gssApiToken.len;
        this.sessionKey.data = (byte[])sessionKey.data.clone();
        this.sessionKey.len = sessionKey.len;
    }

    public GssContextCredentials(Blob gssApiToken, Blob sessionKey, String domain, Credentials.Updater updater, Object hook) throws NqException {
        super("", domain, updater, hook);
        if (null == sessionKey || null == sessionKey.data) {
            throw new NqException("Invalid sessionKey input", -20);
        }
        if (null == gssApiToken || null == gssApiToken.data) {
            throw new NqException("Invalid gssApiToken input", -20);
        }
        super.setKey(new String(sessionKey.data));
        this.gssApiToken.data = (byte[])gssApiToken.data.clone();
        this.gssApiToken.len = gssApiToken.len;
        this.sessionKey.data = (byte[])sessionKey.data.clone();
        this.sessionKey.len = sessionKey.len;
    }

    public Credentials cloneCred() {
        GssContextCredentials newCred = null;
        try {
            newCred = new GssContextCredentials(new Blob(this.gssApiToken), new Blob(this.sessionKey), new String(this.domain), this.updater, this.hook);
        }
        catch (NqException e) {
            TraceLog.get().error("Unable to clone GssContextCredentials");
        }
        return newCred;
    }

    public void setDomain(String domain) {
        super.setDomain(domain);
    }

    public String getUser() {
        return null;
    }

    public Blob getGssApiToken() {
        return this.gssApiToken;
    }

    public void setGssApiToken(Blob gssApiToken) {
        this.gssApiToken.data = (byte[])gssApiToken.data.clone();
        this.gssApiToken.len = gssApiToken.len;
    }

    public Blob getSessionKey() {
        return this.sessionKey;
    }

    public void setSessionKey(Blob sessionKey) {
        this.sessionKey.data = (byte[])sessionKey.data.clone();
        this.sessionKey.len = sessionKey.len;
    }
}

