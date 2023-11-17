/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.auth.ServerContext;
import com.visuality.nq.auth.Spnego;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Utility;
import com.visuality.nq.config.Config;
import java.lang.reflect.Method;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.kerberos.KerberosTicket;

public class RealmContext {
    private String kdc;
    private String realm;
    private String user;
    private String password = "";
    private Object loginModule;
    private Subject subject;
    private long timeToLive;
    private static Class krbClass = null;
    private static Method initialize;
    private static Method login;
    private static Method commit;
    private static final String TGT = "krbtgt";

    protected void setSubject(Subject subject) {
        this.subject = subject;
    }

    protected Subject getSubject() {
        return this.subject;
    }

    public String getSecurityMechanism() {
        return "GSSAPI";
    }

    public RealmContext(Subject subject, String realm) {
        this.subject = subject;
        this.realm = realm;
        try {
            this.timeToLive = Config.jnq.getInt("TICKETTTL");
            if (this.timeToLive > 0L) {
                this.timeToLive += Utility.getCurrentTimeInSec();
            }
        }
        catch (NqException e) {
            this.timeToLive = 0L;
        }
    }

    public RealmContext(String kdc, String realm, String user) {
        this.kdc = kdc;
        this.realm = realm;
        this.user = user;
        try {
            this.timeToLive = Config.jnq.getInt("TICKETTTL");
            if (this.timeToLive > 0L) {
                this.timeToLive += Utility.getCurrentTimeInSec();
            }
        }
        catch (NqException e) {
            this.timeToLive = 0L;
        }
    }

    public String getRealm() {
        return this.realm;
    }

    public String getUser() {
        return this.user;
    }

    public String getPassword() {
        return this.password;
    }

    public long getTimeToLive() {
        TraceLog.get().enter(700);
        for (KerberosTicket ticket : this.getSubject().getPrivateCredentials(KerberosTicket.class)) {
            String serverName = ticket.getServer().getName().toLowerCase();
            if (!serverName.contains(TGT)) continue;
            long endTime = ticket.getEndTime().getTime() / 1000L;
            TraceLog.get().message("endTime = " + endTime, 2000);
            if (endTime <= 0L || 0L < this.timeToLive && this.timeToLive <= endTime) break;
            this.timeToLive = endTime;
            break;
        }
        TraceLog.get().exit("timeToLive = " + this.timeToLive, 700);
        return this.timeToLive;
    }

    public ServerContext createServerContext(final String serverName, final boolean isSmb2, final boolean signingOn) throws Exception {
        return (ServerContext)Subject.doAs(this.subject, new PrivilegedExceptionAction(){

            public Object run() throws Exception {
                return new ServerContext(RealmContext.this.subject, serverName, isSmb2, signingOn);
            }
        });
    }

    void login(String password) throws Exception {
        TraceLog.get().enter(700);
        boolean oracleKerberos = Utility.isClassSupport("com.sun.security.auth.module.Krb5LoginModule");
        this.password = password;
        if (System.getProperty("java.security.krb5.conf") == null) {
            if (this.kdc.equals("") || this.realm.equals("")) {
                throw new NqException("kdc/realm is not be set", -20);
            }
            System.setProperty("java.security.krb5.kdc", this.kdc);
            System.setProperty("java.security.krb5.realm", this.realm);
        }
        HashMap<String, String> option = new HashMap<String, String>();
        HashMap<String, Object> state = new HashMap<String, Object>();
        option.put("debug", "false");
        if (!Spnego.isKerberosSupported || null == initialize || null == login || null == commit) {
            throw new NqException("Kerberos is not supported by the platform", -22);
        }
        if (!Config.jnq.getBool("USECACHE")) {
            option.put("tryFirstPass", "true");
            if (oracleKerberos) {
                option.put("useTicketCache", "false");
                option.put("doNotPrompt", "false");
                option.put("storePass", "false");
            }
            state.put("javax.security.auth.login.name", this.user);
            state.put("javax.security.auth.login.password", password.toCharArray());
            state.put("javax.security.auth.useSubjectCredsOnly", "false");
        } else {
            option.put("tryFirstPass", "true");
            if (oracleKerberos) {
                option.put("doNotPrompt", "true");
                option.put("useTicketCache", "true");
            } else {
                option.put("moduleBanner", "false");
                option.put("useDefaultCcache", "true");
            }
            if (this.user != null) {
                option.put("principal", this.user);
            }
        }
        if (!Config.jnq.getBool("USECACHE") && Config.jnq.getBool("STOREKEY")) {
            option.put("storeKey", "true");
        }
        this.loginModule = krbClass.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        this.subject = new Subject();
        Object[] objectList = new Object[]{this.subject, null, state, option};
        initialize.invoke(this.loginModule, objectList);
        Class[] noparams = new Class[]{};
        Boolean loginRes = null;
        String eMsg = null;
        Exception exc = null;
        try {
            loginRes = (Boolean)login.invoke(this.loginModule, noparams);
            if (loginRes.booleanValue() == Boolean.TRUE.booleanValue()) {
                commit.invoke(this.loginModule, noparams);
            }
        }
        catch (Exception e) {
            eMsg = e.getMessage();
            exc = e;
        }
        if (null == loginRes || loginRes.booleanValue() == Boolean.FALSE.booleanValue() || null != exc) {
            if (null == eMsg) {
                eMsg = "Unable to login to Kerberos";
            }
            throw new NqException("Unable to login: " + eMsg + "; exception class=" + exc.getClass().getName(), -18);
        }
        TraceLog.get().exit(700);
    }

    void dispose() {
    }

    public String toString() {
        return "RealmContext [kdc=" + this.kdc + ", realm=" + this.realm + ", user=" + this.user + ", loginModule=" + this.loginModule + ", subject=" + this.subject + ", timeToLive=" + this.timeToLive + "]";
    }

    static {
        if (Utility.isClassSupport("com.ibm.security.auth.module.Krb5LoginModule")) {
            krbClass = Utility.isClassForName("com.ibm.security.auth.module.Krb5LoginModule");
        } else if (Utility.isClassSupport("com.sun.security.auth.module.Krb5LoginModule")) {
            krbClass = Utility.isClassForName("com.sun.security.auth.module.Krb5LoginModule");
        }
        try {
            if (null != krbClass) {
                Class[] classesList = new Class[]{Subject.class, CallbackHandler.class, Map.class, Map.class};
                initialize = krbClass.getDeclaredMethod("initialize", classesList);
                Class[] noparams = new Class[]{};
                login = krbClass.getDeclaredMethod("login", noparams);
                commit = krbClass.getMethod("commit", noparams);
            }
        }
        catch (NoSuchMethodException e) {
        }
        catch (SecurityException e) {
            TraceLog.get().error("Kerberos load method faild");
        }
    }
}

