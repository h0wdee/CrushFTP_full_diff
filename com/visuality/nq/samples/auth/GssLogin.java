/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples.auth;

import com.visuality.nq.auth.GssContextCredentials;
import com.visuality.nq.auth.ServerContext;
import com.visuality.nq.common.Blob;
import com.visuality.nq.common.NqException;
import com.visuality.nq.samples.auth.KerberosAuthenticator;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.security.auth.Subject;

public class GssLogin {
    public static GssContextCredentials login(final String smbServer, String user, String password, String kdc, String realm) throws NqException {
        KerberosAuthenticator ka = new KerberosAuthenticator();
        final Subject subject = ka.login(user, password, kdc, realm);
        ServerContext serverContext = null;
        try {
            serverContext = (ServerContext)Subject.doAs(subject, new PrivilegedExceptionAction(){

                public Object run() throws Exception {
                    return new ServerContext(subject, smbServer, true, false);
                }
            });
        }
        catch (PrivilegedActionException e) {
            e.printStackTrace();
            throw new NqException("Server context error = " + e);
        }
        byte[] token = null;
        byte[] sessionKey = null;
        try {
            token = serverContext.generateFirstRequest();
            sessionKey = serverContext.getSessionKey();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new NqException("Token or session key error = " + e);
        }
        if (null == token || null == sessionKey) {
            throw new NqException("Token or session key are empty");
        }
        Blob credsBlob = new Blob(token);
        Blob sessionBlob = new Blob(sessionKey);
        return new GssContextCredentials(credsBlob, sessionBlob, realm);
    }
}

