/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import java.security.Provider;

public class CrushOAuth2Provider
extends Provider {
    private static final long serialVersionUID = 9038124458540162644L;

    public CrushOAuth2Provider() {
        super(String.valueOf(System.getProperty("appname", "CrushFTP")) + " OAuth2 Provider", 1.0, "XOAUTH2 SASL Mechanism");
        this.put("SaslClientFactory.XOAUTH2", "com.crushftp.client.CrushOAuth2SaslClientFactory");
    }

    @Override
    public String getName() {
        return String.valueOf(System.getProperty("appname", "CrushFTP")) + "_SASL_OAuth2";
    }
}

