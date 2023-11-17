/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.auth.Credentials;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosTicket;

public class SubjectCredentials
extends Credentials {
    private String user;

    public SubjectCredentials() {
        super(new Subject(), "", null, null);
    }

    public SubjectCredentials(Subject subject) {
        super(subject, "", null, null);
        this.determineDomain();
        this.determineUser();
    }

    public SubjectCredentials(Subject subject, String domain) {
        super(subject, domain.toLowerCase(), null, null);
        this.determineUser();
    }

    public SubjectCredentials(Subject subject, String domain, Credentials.Updater updater, Object hook) {
        super(subject, domain.toLowerCase(), updater, hook);
        this.determineUser();
    }

    public Subject getSubject() {
        return (Subject)this.key;
    }

    public String getUser() {
        return this.user;
    }

    public String getDomain() {
        return this.domain;
    }

    public boolean isAnonymous() {
        return null == this.key;
    }

    public Credentials cloneCred() {
        SubjectCredentials newCred = new SubjectCredentials((Subject)this.key, new String(this.domain), this.updater, this.hook);
        newCred.user = this.user;
        return newCred;
    }

    private void determineDomain() {
        String domainName = null;
        for (KerberosTicket ticket : this.getSubject().getPrivateCredentials(KerberosTicket.class)) {
            domainName = ticket.getClient().getRealm().toString();
            if (null == domainName) continue;
            if (domainName.length() != 0) break;
            domainName = null;
        }
        this.domain = null == domainName ? null : domainName.toLowerCase();
    }

    private void determineUser() {
        String userName = null;
        for (KerberosTicket ticket : this.getSubject().getPrivateCredentials(KerberosTicket.class)) {
            userName = ticket.getClient().getName();
            if (null == userName) continue;
            if (userName.contains("@")) {
                userName = userName.split("@")[0];
            }
            if (userName.length() != 0) break;
            userName = null;
        }
        this.user = null == userName ? null : userName.toLowerCase();
    }
}

