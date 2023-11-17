/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Validator;
import com.visuality.nq.config.Config;

public class PasswordCredentials
extends Credentials {
    private String user;
    private String password;
    private static PasswordCredentials defaultCredentials = null;

    public static PasswordCredentials getDefaultCredentials() {
        return defaultCredentials;
    }

    public PasswordCredentials() {
        super("", "", null, null);
        this.user = "";
        this.password = "";
    }

    public PasswordCredentials(String user, String password, String domain) throws NqException {
        super(user, password, domain);
        String userTmp = null == user ? null : user.toLowerCase();
        String domainTmp = null == domain ? null : domain.toLowerCase();
        this.initialize(userTmp, password, domainTmp);
    }

    private void initialize(String user, String password, String domain) throws NqException {
        if (null == user) {
            throw new NqException("User cannot be null", -20);
        }
        int atSignPtr = user.indexOf(64);
        int backSlashPtr = user.indexOf(92);
        if (backSlashPtr >= 0) {
            if (null != domain && domain.length() != 0) {
                throw new NqException("Domain defined twice", -20);
            }
            domain = user.substring(0, backSlashPtr);
            user = user.substring(backSlashPtr + 1);
            atSignPtr = user.indexOf(64);
        }
        domain = this.calculateDomain(domain);
        if (0 == atSignPtr) {
            throw new NqException("Illegal user argument", -20);
        }
        if (0 < atSignPtr) {
            if (null != domain && domain.length() != 0) {
                throw new NqException("Domain defined twice", -20);
            }
            if (atSignPtr + 1 == user.length()) {
                throw new NqException("Domain name is missing", -20);
            }
        }
        if (!Validator.validDomain(domain)) {
            throw new NqException("Invalid domain name", -20);
        }
        this.calculateKey(user + password + domain);
        this.user = user;
        this.password = password;
        this.domain = domain;
    }

    public PasswordCredentials(String user, String password, String domain, Credentials.Updater updater, Object hook) throws NqException {
        super(user + password + domain, domain, updater, hook);
        String userTmp = null == user ? null : user.toLowerCase();
        String domainTmp = null == domain ? null : domain.toLowerCase();
        this.initialize(userTmp, password, domainTmp);
    }

    public String getUser() {
        return this.user;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean isAnonymous() {
        return null == this.user || this.user.length() == 0;
    }

    public Credentials cloneCred() {
        PasswordCredentials newCred = new PasswordCredentials();
        newCred.setUser(this.user);
        newCred.setPassword(this.password);
        newCred.setDomain(this.domain);
        newCred.setUpdater(this.updater);
        newCred.setHook(this.hook);
        return newCred;
    }

    public void setUser(String user) {
        this.user = null == user ? null : user.toLowerCase();
        this.setKey(this.user + this.password + this.domain);
    }

    public void setPassword(String password) {
        this.password = password;
        this.setKey(this.user + password + this.domain);
    }

    public void setDomain(String domain) {
        String domn = null == domain ? null : domain.toLowerCase();
        super.setDomain(domn);
        this.setKey(this.user + this.password + domn);
    }

    public String toString() {
        return "PasswordCredentials [user=" + this.user + ", domain=" + this.domain + "]";
    }

    static {
        try {
            defaultCredentials = new PasswordCredentials((String)Config.jnq.getNE("DEFAULTUSER"), (String)Config.jnq.getNE("DEFAULTPASS"), (String)Config.jnq.getNE("DEFAULTDOMAIN"));
        }
        catch (NqException e) {
            TraceLog.get().message("DEFAULTDOMAIN is not defined, defaultCredentials defined as empty.", 2000);
            defaultCredentials = new PasswordCredentials();
        }
    }
}

