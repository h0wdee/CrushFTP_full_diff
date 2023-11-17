/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.auth.Authentications;
import com.visuality.nq.auth.Credentials;
import com.visuality.nq.auth.GssContextCredentials;
import com.visuality.nq.auth.SecurityMechanism;
import com.visuality.nq.common.Blob;
import com.visuality.nq.common.HexBuilder;

class SecurityContext {
    int defAuthLevel;
    int status;
    SecurityMechanism mechanism;
    Object extendedContext = null;
    Credentials credentials;
    Blob sessionKey;
    Blob macSessionKey;
    int level;
    boolean isServerSupportingGSSAPI;

    public SecurityContext(Credentials credentials, int level) {
        this.credentials = credentials;
        this.sessionKey = null;
        this.macSessionKey = null;
        this.status = 1;
        this.level = level > Authentications.AM_MAXSECURITYLEVEL ? Authentications.AM_MAXSECURITYLEVEL : level;
        int n = this.level = this.level < Authentications.AM_MINSECURITYLEVEL ? Authentications.AM_MINSECURITYLEVEL : this.level;
        if (credentials instanceof GssContextCredentials) {
            GssContextCredentials gssCreds = (GssContextCredentials)credentials;
            this.sessionKey = gssCreds.getSessionKey();
            this.macSessionKey = gssCreds.getSessionKey();
        }
    }

    public String toString() {
        String macSessionKeyString = "null";
        if (null != this.macSessionKey && null != this.macSessionKey.data) {
            macSessionKeyString = HexBuilder.toHex(this.macSessionKey.data).toString();
        }
        return "SecurityContext [defAuthLevel=" + this.defAuthLevel + ", status=" + this.status + ", mechanism=" + this.mechanism + ", extendedContext=" + (null != this.extendedContext ? Integer.valueOf(this.extendedContext.hashCode()) : "null") + ", credentials=" + this.credentials + ", macSessionKey=" + macSessionKeyString + ", level=" + this.level + "]";
    }
}

