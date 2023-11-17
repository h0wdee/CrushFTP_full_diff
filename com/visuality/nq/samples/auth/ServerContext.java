/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples.auth;

import java.lang.reflect.Method;
import java.security.Key;
import java.security.PrivilegedExceptionAction;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosTicket;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.MessageProp;
import org.ietf.jgss.Oid;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ServerContext {
    private static final String SERVICE = "cifs";
    private GSSContext context;
    private Subject subject;
    private static final String ORACLE_JGSS_INQUIRE_CLASS = "com.sun.security.jgss.InquireType";
    private static final String ORACLE_JGSS_EXT_CLASS = "com.sun.security.jgss.ExtendedGSSContext";
    private static final String IBM_JGSS_INQUIRE_CLASS = "com.ibm.security.jgss.InquireType";
    private static final String IBM_JGSS_EXT_CLASS = "com.ibm.security.jgss.ExtendedGSSContext";
    private static Method inquireSecContext;
    private static Object inquireTypeSessionKey;
    private static Class extGssClass;
    private static Class inquireTypeClass;

    public ServerContext(Subject subject, String serverName, boolean isSmb2, boolean signingOn) throws Exception {
        this.subject = subject;
        GSSCredential clientCreds = null;
        Set<GSSCredential> gssCreds = subject.getPrivateCredentials(GSSCredential.class);
        if (!gssCreds.isEmpty()) {
            clientCreds = gssCreds.iterator().next();
        }
        Oid krb5Oid = new Oid("1.2.840.113554.1.2.2");
        GSSManager manager = GSSManager.getInstance();
        GSSName serverGssName = manager.createName("cifs@" + serverName, GSSName.NT_HOSTBASED_SERVICE, krb5Oid);
        this.context = manager.createContext(serverGssName, krb5Oid, clientCreds, 0);
        this.context.requestMutualAuth(false);
        this.context.requestConf(true);
        this.context.requestInteg(true);
    }

    public byte[] generateFirstRequest() throws Exception {
        return (byte[])Subject.doAs(this.subject, new PrivilegedExceptionAction(){

            public Object run() throws Exception {
                return ServerContext.this.context.initSecContext(new byte[0], 0, 0);
            }
        });
    }

    public byte[] generateNextRequest(final byte[] in, final int offset, final int length) throws Exception {
        return (byte[])Subject.doAs(this.subject, new PrivilegedExceptionAction(){

            public Object run() throws Exception {
                return ServerContext.this.context.initSecContext(in, offset, length);
            }
        });
    }

    public byte[] getSessionKey() throws Exception {
        Key key = null;
        if (null == extGssClass || null == inquireTypeClass) {
            String srcName = ((Object)this.context.getSrcName()).toString();
            String targName = ((Object)this.context.getTargName()).toString();
            if (null == srcName || null == targName || !targName.contains("@")) {
                throw new Exception("session key not found");
            }
            String targServerName = targName.substring(targName.indexOf("@") + 1).toLowerCase();
            for (KerberosTicket ticket : this.subject.getPrivateCredentials(KerberosTicket.class)) {
                String serverName = ticket.getServer().getName().toLowerCase();
                if (!srcName.toLowerCase().equals(ticket.getClient().getName().toLowerCase()) || !serverName.contains(SERVICE) || !serverName.toLowerCase().contains(targServerName)) continue;
                key = ticket.getSessionKey();
                return key.getEncoded();
            }
            throw new Exception("session key not found");
        }
        try {
            key = (Key)inquireSecContext.invoke(this.context, inquireTypeSessionKey);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return key.getEncoded();
    }

    public byte[] wrap(byte[] inBuf, int offset, int len) throws Exception {
        MessageProp prop = new MessageProp(0, true);
        return this.context.wrap(inBuf, offset, len, prop);
    }

    public byte[] unwrap(byte[] inBuf, int offset, int len) throws Exception {
        MessageProp prop = new MessageProp(0, false);
        return this.context.unwrap(inBuf, offset, len, prop);
    }

    public void dispose() throws Exception {
        this.context.dispose();
    }

    private static <T extends Enum> Object getSessionKeyInquireType(Class inquireTypeClass) {
        return Enum.valueOf(inquireTypeClass, "KRB5_GET_SESSION_KEY");
    }

    public String toString() {
        return "ServerContext [context=" + this.context + ", subject=" + this.subject + "]";
    }

    public static boolean isClassSupport(String className) {
        try {
            Class.forName(className);
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static Class isClassForName(String kerberosClass) {
        try {
            return Class.forName(kerberosClass);
        }
        catch (ClassNotFoundException e) {
            return null;
        }
    }

    static {
        extGssClass = null;
        inquireTypeClass = null;
        if (ServerContext.isClassSupport(ORACLE_JGSS_INQUIRE_CLASS)) {
            extGssClass = ServerContext.isClassForName(ORACLE_JGSS_EXT_CLASS);
            inquireTypeClass = ServerContext.isClassForName(ORACLE_JGSS_INQUIRE_CLASS);
        } else if (ServerContext.isClassSupport(IBM_JGSS_INQUIRE_CLASS)) {
            extGssClass = ServerContext.isClassForName(IBM_JGSS_EXT_CLASS);
            inquireTypeClass = ServerContext.isClassForName(IBM_JGSS_INQUIRE_CLASS);
        }
        try {
            if (null != extGssClass && null != inquireTypeClass) {
                inquireTypeSessionKey = ServerContext.getSessionKeyInquireType(inquireTypeClass);
                inquireSecContext = extGssClass.getMethod("inquireSecContext", inquireTypeClass);
            }
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}

