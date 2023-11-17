/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh2;

public class AuthenticationResult
extends Throwable {
    private static final long serialVersionUID = 5676223937937799713L;
    int result;
    String auths;

    public AuthenticationResult(int result) {
        this.result = result;
    }

    public AuthenticationResult(int result, String auths) {
        this.result = result;
        this.auths = auths;
    }

    public int getResult() {
        return this.result;
    }

    public String getAuthenticationMethods() {
        return this.auths;
    }
}

