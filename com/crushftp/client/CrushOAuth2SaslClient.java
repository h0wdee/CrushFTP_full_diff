/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

class CrushOAuth2SaslClient
implements SaslClient {
    private final String oauthToken;
    private final CallbackHandler callbackHandler;
    private boolean isComplete = false;

    public CrushOAuth2SaslClient(String oauthToken, CallbackHandler callbackHandler) {
        this.oauthToken = oauthToken;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public String getMechanismName() {
        return "XOAUTH2";
    }

    @Override
    public boolean hasInitialResponse() {
        return true;
    }

    @Override
    public byte[] evaluateChallenge(byte[] challenge) throws SaslException {
        if (this.isComplete) {
            return new byte[0];
        }
        NameCallback nameCallback = new NameCallback("Enter name");
        Callback[] callbacks = new Callback[]{nameCallback};
        try {
            this.callbackHandler.handle(callbacks);
        }
        catch (UnsupportedCallbackException e) {
            throw new SaslException("Unsupported callback: " + e);
        }
        catch (IOException e) {
            throw new SaslException("Failed to execute callback: " + e);
        }
        String email = nameCallback.getName();
        byte[] response = null;
        try {
            response = ("user=" + email + "\u0001auth=Bearer " + this.oauthToken + "\u0001\u0001").getBytes("UTF8");
        }
        catch (Exception e) {
            throw new SaslException("Failed to execute callback: " + e);
        }
        this.isComplete = true;
        return response;
    }

    @Override
    public boolean isComplete() {
        return this.isComplete;
    }

    @Override
    public byte[] unwrap(byte[] incoming, int offset, int len) throws SaslException {
        return null;
    }

    @Override
    public byte[] wrap(byte[] outgoing, int offset, int len) throws SaslException {
        return null;
    }

    @Override
    public Object getNegotiatedProperty(String propName) {
        return null;
    }

    @Override
    public void dispose() throws SaslException {
    }
}

