/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.auth.Crypt;
import com.visuality.nq.auth.Spnego;
import com.visuality.nq.common.Blob;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TimeUtility;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.config.Config;

public class Authentications {
    public static final short CIPHER_NO_CIPHER = 0;
    public static final short CIPHER_AES128CCM = 1;
    public static final short CIPHER_AES128GCM = 2;
    public static final int AM_SPNEGO_SUCCESS = 0;
    public static final int AM_SPNEGO_NONE = 1;
    public static final int AM_SPNEGO_FAILED = 2;
    public static final int AM_SPNEGO_CONTINUE = 3;
    public static final int AM_SPNEGO_DENIED = 4;
    public static int AM_MAXSECURITYLEVEL = (Integer)Config.jnq.getNE("MAXSECURITYLEVEL");
    public static int AM_MINSECURITYLEVEL;
    public static int AM_CURRAUTHLEVEL;
    public static final int AM_CRYPTER_NONE = 0;
    public static final int AM_CRYPTER_LM = 1;
    public static final int AM_CRYPTER_NTLM = 2;
    public static final int AM_CRYPTER_LM2 = 3;
    public static final int AM_CRYPTER_NTLM2 = 4;
    public static final int AM_MECH_NTLMSSP = 1;
    public static final int AM_MECH_KERBEROS = 2;

    public static int generatePasswordBlobs(Credentials credentials, int level, Blob pass1, Blob pass2, Blob sessionKey, Blob macSessionKey) throws NqException {
        Crypt crypt = new Crypt();
        int[] timeStamp = new int[]{0, 0};
        int res = 2;
        try {
            if (!Config.jnq.getBool("ENABLENONSECUREAUTHMETHODS")) {
                return res;
            }
        }
        catch (NqException e) {
            TraceLog.get().error("ENABLENONSECUREAUTHMETHODS parameter is not found");
        }
        if (((Spnego.SpnegoLevel)Spnego.levels.get((int)level)).crypter1 == 0 || ((Spnego.SpnegoLevel)Spnego.levels.get((int)level)).crypter2 == 0) {
            return res;
        }
        timeStamp = TimeUtility.getCurrentTimeAsArray();
        if (null == sessionKey.data) {
            return res;
        }
        if (!Crypt.cryptEncrypt(credentials, ((Spnego.SpnegoLevel)Spnego.levels.get((int)level)).crypter1, ((Spnego.SpnegoLevel)Spnego.levels.get((int)level)).crypter2, sessionKey.data, null, timeStamp, crypt)) {
            return res;
        }
        pass1.data = crypt.pass1.data;
        pass1.len = crypt.pass1.len;
        pass2.data = crypt.pass2.data;
        pass2.len = crypt.pass2.len;
        if (null != crypt.macKey.data && null != macSessionKey) {
            macSessionKey.data = crypt.macKey.data;
            macSessionKey.len = crypt.macKey.len;
        }
        if (null != crypt.response.data && null != sessionKey) {
            sessionKey.data = crypt.response.data;
            sessionKey.len = crypt.response.len;
        }
        res = 0;
        return res;
    }

    public static void setNonSecureAuthentication(boolean enableNonSecureAuthentication) {
        try {
            if (enableNonSecureAuthentication) {
                Spnego.defineSpnegoLevel(0, 1, 0, 0);
                Spnego.defineSpnegoLevel(1, 1, 2, 0);
                Spnego.defineSpnegoLevel(2, 1, 2, 3);
                Config.jnq.set("ENABLENONSECUREAUTHMETHODS", true);
            } else {
                Spnego.defineSpnegoLevel(0, 0, 0, 0);
                Spnego.defineSpnegoLevel(1, 0, 0, 0);
                Spnego.defineSpnegoLevel(2, 0, 0, 0);
                Config.jnq.set("ENABLENONSECUREAUTHMETHODS", false);
            }
        }
        catch (NqException e) {
            TraceLog.get().error("ENABLENONSECUREAUTHMETHODS parameter is not found");
        }
    }

    public static void setMaxSecurityLevel(int level) {
        AM_MAXSECURITYLEVEL = level;
    }

    public static int getMaxSecurityLevel() {
        return AM_MAXSECURITYLEVEL;
    }

    public static void setMinSecurityLevel(int level) {
        AM_MINSECURITYLEVEL = level;
    }

    static {
        if (AM_MAXSECURITYLEVEL > 4 || AM_MAXSECURITYLEVEL < 0) {
            AM_MAXSECURITYLEVEL = 4;
        }
        if ((AM_MINSECURITYLEVEL = ((Integer)Config.jnq.getNE("MINSECURITYLEVEL")).intValue()) < 0 || AM_MINSECURITYLEVEL > AM_MAXSECURITYLEVEL) {
            AM_MINSECURITYLEVEL = 0;
        }
        AM_CURRAUTHLEVEL = AM_MAXSECURITYLEVEL;
    }
}

