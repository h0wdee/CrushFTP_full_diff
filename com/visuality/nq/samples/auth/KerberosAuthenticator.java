/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples.auth;

import com.sun.security.auth.module.Krb5LoginModule;
import com.visuality.nq.common.NqException;
import java.util.HashMap;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

public class KerberosAuthenticator {
    private Subject subject;
    private Krb5LoginModule loginModule;

    public Subject login(String user, String password, String kdc, String realm) throws NqException {
        System.setProperty("java.security.krb5.kdc", kdc);
        System.setProperty("java.security.krb5.realm", realm);
        HashMap<String, String> option = new HashMap<String, String>();
        HashMap<String, Object> state = new HashMap<String, Object>();
        option.put("tryFirstPass", "true");
        state.put("javax.security.auth.login.name", user);
        state.put("javax.security.auth.login.password", password.toCharArray());
        try {
            this.loginModule = new Krb5LoginModule();
            this.subject = new Subject();
            this.loginModule.initialize(this.subject, null, state, option);
            Boolean loginRes = this.loginModule.login();
            if (loginRes.booleanValue() == Boolean.TRUE.booleanValue()) {
                this.loginModule.commit();
            }
        }
        catch (LoginException ie) {
            ie.printStackTrace();
            throw new NqException("Kerberos error = " + ie, -18);
        }
        return this.subject;
    }
}

