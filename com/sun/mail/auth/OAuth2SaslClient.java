/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.auth;

import com.sun.mail.util.ASCIIUtility;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class OAuth2SaslClient
implements SaslClient {
    private CallbackHandler cbh;
    private boolean complete = false;

    public OAuth2SaslClient(Map<String, ?> props, CallbackHandler cbh) {
        this.cbh = cbh;
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
        byte[] response;
        if (this.complete) {
            return new byte[0];
        }
        NameCallback ncb = new NameCallback("User name:");
        PasswordCallback pcb = new PasswordCallback("OAuth token:", false);
        try {
            this.cbh.handle(new Callback[]{ncb, pcb});
        }
        catch (UnsupportedCallbackException ex) {
            throw new SaslException("Unsupported callback", ex);
        }
        catch (IOException ex) {
            throw new SaslException("Callback handler failed", ex);
        }
        String user = ncb.getName();
        String token = new String(pcb.getPassword());
        pcb.clearPassword();
        String resp = "user=" + user + "\u0001auth=Bearer " + token + "\u0001\u0001";
        try {
            response = resp.getBytes("utf-8");
        }
        catch (UnsupportedEncodingException ex) {
            response = ASCIIUtility.getBytes(resp);
        }
        this.complete = true;
        return response;
    }

    @Override
    public boolean isComplete() {
        return this.complete;
    }

    @Override
    public byte[] unwrap(byte[] incoming, int offset, int len) throws SaslException {
        throw new IllegalStateException("OAUTH2 unwrap not supported");
    }

    @Override
    public byte[] wrap(byte[] outgoing, int offset, int len) throws SaslException {
        throw new IllegalStateException("OAUTH2 wrap not supported");
    }

    @Override
    public Object getNegotiatedProperty(String propName) {
        if (!this.complete) {
            throw new IllegalStateException("OAUTH2 getNegotiatedProperty");
        }
        return null;
    }

    @Override
    public void dispose() throws SaslException {
    }
}

