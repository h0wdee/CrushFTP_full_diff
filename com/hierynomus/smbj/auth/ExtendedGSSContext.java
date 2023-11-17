/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.auth;

import com.hierynomus.protocol.transport.TransportException;
import com.hierynomus.smbj.auth.SpnegoAuthenticator;
import java.lang.reflect.Method;
import java.security.Key;
import org.ietf.jgss.GSSContext;

class ExtendedGSSContext {
    private static final Method inquireSecContext = ExtendedGSSContext.getInquireSecContextMethod();
    private static Object krb5GetSessionKeyConst;

    private static Method getInquireSecContextMethod() {
        Class<?> inquireTypeClass;
        Class<?> extendedContextClass;
        try {
            extendedContextClass = Class.forName("com.sun.security.jgss.ExtendedGSSContext", false, SpnegoAuthenticator.class.getClassLoader());
            inquireTypeClass = Class.forName("com.sun.security.jgss.InquireType");
        }
        catch (ClassNotFoundException e) {
            try {
                extendedContextClass = Class.forName("com.ibm.security.jgss.ExtendedGSSContext", false, SpnegoAuthenticator.class.getClassLoader());
                inquireTypeClass = Class.forName("com.ibm.security.jgss.InquireType");
            }
            catch (ClassNotFoundException e1) {
                IllegalStateException exception = new IllegalStateException("The code is running in an unknown java vm");
                exception.addSuppressed(e);
                exception.addSuppressed(e1);
                throw exception;
            }
        }
        krb5GetSessionKeyConst = Enum.valueOf(inquireTypeClass.asSubclass(Enum.class), "KRB5_GET_SESSION_KEY");
        try {
            return extendedContextClass.getDeclaredMethod("inquireSecContext", inquireTypeClass);
        }
        catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Key krb5GetSessionKey(GSSContext gssContext) throws TransportException {
        try {
            return (Key)inquireSecContext.invoke(gssContext, krb5GetSessionKeyConst);
        }
        catch (Throwable e) {
            throw new TransportException(e);
        }
    }

    private ExtendedGSSContext() {
    }
}

