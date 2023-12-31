/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

public class TrustManagerCustom
implements X509TrustManager {
    X509TrustManager origTrustmanager = null;
    boolean trustAll = false;
    boolean trustExpired = false;

    public TrustManagerCustom() {
        this.trustAll = true;
    }

    public TrustManagerCustom(X509TrustManager origTrustmanager, boolean trustAll, boolean trustExpired) {
        this.origTrustmanager = origTrustmanager;
        this.trustAll = trustAll;
        this.trustExpired = trustExpired;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        if (this.origTrustmanager == null || this.trustAll) {
            return null;
        }
        return this.origTrustmanager.getAcceptedIssuers();
    }

    @Override
    public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        if (this.origTrustmanager == null || this.trustAll) {
            return;
        }
        try {
            this.origTrustmanager.checkClientTrusted(certs, authType);
        }
        catch (CertificateExpiredException certificateExpiredException) {
            // empty catch block
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        if (this.origTrustmanager == null || this.trustAll) {
            return;
        }
        this.origTrustmanager.checkServerTrusted(certs, authType);
    }
}

