/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Utility;
import java.lang.reflect.Method;
import java.security.Key;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
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
        TraceLog.get().enter(700);
        TraceLog.get().message("serverName=" + serverName + "; isSmb2=" + isSmb2 + "; signingOn=" + signingOn, 2000);
        this.subject = subject;
        GSSCredential clientCreds = null;
        Set<GSSCredential> gssCreds = subject.getPrivateCredentials(GSSCredential.class);
        if (!gssCreds.isEmpty()) {
            clientCreds = gssCreds.iterator().next();
        }
        if (null == gssCreds || gssCreds.isEmpty()) {
            TraceLog.get().message("clientCreds=null or empty", 2000);
        } else {
            TraceLog.get().message("clientCreds=" + clientCreds.toString(), 2000);
        }
        Oid krb5Oid = new Oid("1.2.840.113554.1.2.2");
        GSSManager manager = GSSManager.getInstance();
        GSSName serverGssName = manager.createName("cifs@" + serverName, GSSName.NT_HOSTBASED_SERVICE, krb5Oid);
        this.context = manager.createContext(serverGssName, krb5Oid, clientCreds, 0);
        TraceLog.get().message("context=" + this.context, 2000);
        this.context.requestMutualAuth(false);
        this.context.requestConf(true);
        this.context.requestInteg(true);
        TraceLog.get().exit(700);
    }

    public boolean isEstablished() {
        return this.context.isEstablished();
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
        TraceLog.get().enter(700);
        Key key = null;
        if (null == extGssClass || null == inquireTypeClass) {
            String srcName = ((Object)this.context.getSrcName()).toString();
            String targName = ((Object)this.context.getTargName()).toString();
            if (null == srcName || null == targName || !targName.contains("@")) {
                TraceLog.get().exit(700);
                throw new Exception("session key not found");
            }
            String targServerName = targName.substring(targName.indexOf("@") + 1).toLowerCase();
            for (KerberosTicket ticket : this.subject.getPrivateCredentials(KerberosTicket.class)) {
                String serverName = ticket.getServer().getName().toLowerCase();
                TraceLog.get().message("srcName = " + srcName + ", ticket client name = " + ticket.getClient().getName(), 2000);
                if (!srcName.equalsIgnoreCase(ticket.getClient().getName()) || !serverName.startsWith(SERVICE)) continue;
                String pName = serverName.substring(SERVICE.length() + 1, serverName.indexOf("@"));
                String realmSuffix = "." + serverName.substring(serverName.indexOf("@") + 1);
                TraceLog.get().message("serverName = " + serverName + ", pName = " + pName + ", realmSuffix = " + realmSuffix + ", targServerName = " + targServerName, 2000);
                if (pName.length() == targServerName.length()) {
                    if (!pName.equals(targServerName)) continue;
                    key = ticket.getSessionKey();
                    break;
                }
                if (pName.length() > targServerName.length()) {
                    if (pName.length() <= realmSuffix.length() || !pName.endsWith(realmSuffix) || !pName.substring(0, pName.length() - realmSuffix.length()).equals(targServerName)) continue;
                    key = ticket.getSessionKey();
                    break;
                }
                if (targServerName.length() <= realmSuffix.length() || !targServerName.endsWith(realmSuffix) || !targServerName.substring(0, targServerName.length() - realmSuffix.length()).equals(pName)) continue;
                key = ticket.getSessionKey();
                break;
            }
            if (null != key) {
                TraceLog.get().message("Session Key (encoded) =" + Arrays.toString(key.getEncoded()), 2000);
                TraceLog.get().exit(700);
                return key.getEncoded();
            }
            TraceLog.get().exit("Session key not found", 700);
            throw new Exception("Session key not found");
        }
        try {
            TraceLog.get().message("context=" + this.context + "; inquireTypeSessionKey=" + inquireTypeSessionKey, 2000);
            key = (Key)inquireSecContext.invoke(this.context, inquireTypeSessionKey);
        }
        catch (Exception e) {
            TraceLog.get().error("Unable to access the Kerberos SessionKey: " + e.getClass().getName() + "; ", e);
            TraceLog.get().caught(e);
        }
        TraceLog.get().message("Session Key (encoded) =" + Arrays.toString(key.getEncoded()), 2000);
        TraceLog.get().exit(700);
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

    static {
        extGssClass = null;
        inquireTypeClass = null;
        if (Utility.isClassSupport(ORACLE_JGSS_INQUIRE_CLASS)) {
            extGssClass = Utility.isClassForName(ORACLE_JGSS_EXT_CLASS);
            inquireTypeClass = Utility.isClassForName(ORACLE_JGSS_INQUIRE_CLASS);
        } else if (Utility.isClassSupport(IBM_JGSS_INQUIRE_CLASS)) {
            extGssClass = Utility.isClassForName(IBM_JGSS_EXT_CLASS);
            inquireTypeClass = Utility.isClassForName(IBM_JGSS_INQUIRE_CLASS);
        }
        try {
            if (null != extGssClass && null != inquireTypeClass) {
                inquireTypeSessionKey = ServerContext.getSessionKeyInquireType(inquireTypeClass);
                inquireSecContext = extGssClass.getMethod("inquireSecContext", inquireTypeClass);
            }
        }
        catch (NoSuchMethodException e) {
            TraceLog.get().error("Kerberos load method failed: ", e);
        }
        catch (SecurityException e) {
            TraceLog.get().error("Kerberos load method failed: ", e);
        }
    }
}

