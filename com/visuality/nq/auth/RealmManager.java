/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.auth.RealmContext;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Utility;
import java.util.HashMap;
import javax.security.auth.Subject;

public class RealmManager {
    private static RealmManager instance = new RealmManager();
    private HashMap contextsByRealmAndUser = new HashMap();

    private RealmManager() {
    }

    public static RealmManager getInstance() {
        return instance;
    }

    public synchronized RealmContext getRealmContext(String kdc, String realm, String user, String password) throws Exception {
        TraceLog.get().enter(2000);
        String key = RealmManager.getContextKey(realm, user, password);
        RealmContext rc = (RealmContext)this.contextsByRealmAndUser.get(key);
        if (rc == null) {
            TraceLog.get().message("Initializing RealmContext", 2000);
            rc = new RealmContext(kdc, realm, user);
            TraceLog.get().message("About to execute RealmContext.login()", 2000);
            rc.login(password);
            TraceLog.get().message("RealmContext.login() was successful", 2000);
            this.contextsByRealmAndUser.put(key, rc);
        } else {
            TraceLog.get().message("Checking TTL", 2000);
            long currentNumberOfSeconds = Utility.getCurrentTimeInSec();
            long ttl = rc.getTimeToLive();
            if (ttl > 0L && currentNumberOfSeconds > ttl) {
                this.contextsByRealmAndUser.remove(rc);
                rc = null;
                TraceLog.get().message("Renewing RealmContext with new RealmContext", 2000);
                rc = new RealmContext(kdc, realm, user);
                TraceLog.get().message("About to execute RealmContext.login()", 2000);
                rc.login(password);
                TraceLog.get().message("RealmContext.login() was successful", 2000);
                this.contextsByRealmAndUser.put(key, rc);
            }
        }
        TraceLog.get().exit(2000);
        return rc;
    }

    public synchronized RealmContext getRealmContext(Subject subject, String realm) throws Exception {
        String key = RealmManager.getContextKey(subject, realm);
        RealmContext rc = (RealmContext)this.contextsByRealmAndUser.get(key);
        if (rc == null) {
            rc = new RealmContext(subject, realm);
            this.contextsByRealmAndUser.put(key, rc);
        } else {
            TraceLog.get().message("Checking TTL", 2000);
            long currentNumberOfSeconds = Utility.getCurrentTimeInSec();
            long ttl = rc.getTimeToLive();
            if (ttl > 0L && currentNumberOfSeconds > ttl) {
                this.contextsByRealmAndUser.remove(rc);
                rc = null;
                TraceLog.get().message("Renewing RealmContext with new RealmContext", 2000);
                rc = new RealmContext(subject, realm);
                this.contextsByRealmAndUser.put(key, rc);
            }
        }
        return rc;
    }

    public synchronized void disposeRealmContext(RealmContext context) {
        RealmContext rc = (RealmContext)this.contextsByRealmAndUser.remove(RealmManager.getContextKey(context));
        if (rc != null) {
            rc.dispose();
        }
    }

    private static String getContextKey(RealmContext context) {
        if (null == context.getSubject()) {
            return RealmManager.getContextKey(context.getRealm(), context.getUser(), context.getPassword());
        }
        return RealmManager.getContextKey(context.getSubject(), context.getRealm());
    }

    private static String getContextKey(String realm, String user, String password) {
        return user.toUpperCase() + ":" + password.toUpperCase() + "@" + realm.toUpperCase();
    }

    private static String getContextKey(Subject subject, String realm) {
        return ((Object)subject.getPrincipals()).hashCode() + "@" + realm.toUpperCase();
    }
}

