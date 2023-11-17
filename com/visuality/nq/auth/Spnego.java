/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.auth.Asn1;
import com.visuality.nq.auth.Credentials;
import com.visuality.nq.auth.Crypt;
import com.visuality.nq.auth.Gss;
import com.visuality.nq.auth.GssContextCredentials;
import com.visuality.nq.auth.HMACMD5;
import com.visuality.nq.auth.MD5;
import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.auth.RC4;
import com.visuality.nq.auth.RealmContext;
import com.visuality.nq.auth.RealmManager;
import com.visuality.nq.auth.SecurityContext;
import com.visuality.nq.auth.SecurityMechanism;
import com.visuality.nq.auth.ServerContext;
import com.visuality.nq.auth.SubjectCredentials;
import com.visuality.nq.client.MountParams;
import com.visuality.nq.client.Server;
import com.visuality.nq.common.Blob;
import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.HexBuilder;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SmbException;
import com.visuality.nq.common.TimeUtility;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Utility;
import com.visuality.nq.config.Config;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.ArrayList;

public class Spnego {
    private static final byte SPNEGO_ACCEPTINCOMPLETE = 1;
    private static final byte SPNEGO_ACCEPTCOMPLETE = 0;
    protected static final String ORACLE_KERBEROS = "com.sun.security.auth.module.Krb5LoginModule";
    protected static final String IBM_KERBEROS = "com.ibm.security.auth.module.Krb5LoginModule";
    protected static final boolean isKerberosSupported = Utility.isClassSupport("com.sun.security.auth.module.Krb5LoginModule") || Utility.isClassSupport("com.ibm.security.auth.module.Krb5LoginModule");
    public static final int ERR_MOREDATA = -1;
    public static final int ERR_TIMEOUT = -2;
    public static final int ERR_SIGNATUREFAIL = -3;
    public static final int ERR_FATALERROR = -4;
    static SecurityMechanism[] clientMechanisms = isKerberosSupported ? new SecurityMechanism[2] : new SecurityMechanism[1];
    public static ArrayList levels = new ArrayList();
    public static int AM_MAXSECURITYLEVEL = (Integer)Config.jnq.getNE("MAXSECURITYLEVEL");
    public static int AM_MINSECURITYLEVEL = (Integer)Config.jnq.getNE("MINSECURITYLEVEL");
    public static int AM_CURRAUTHLEVEL = AM_MAXSECURITYLEVEL;
    private static ArrayList defaultLevels;

    public static void getSessionKey(SecurityContext context) {
        TraceLog.get().enter(700);
        String name = context.credentials.getUser();
        TraceLog.get().message("name=" + name, 2000);
        if (null == context.macSessionKey.data) {
            context.macSessionKey.data = new byte[32];
            context.macSessionKey.len = 32;
            if (!context.mechanism.getSessionKey(context.extendedContext, context.macSessionKey.data, context.macSessionKey.len)) {
                context.status = 2;
                TraceLog.get().message("context.status changed to failed", 2000);
            }
            if (TraceLog.get().canLog(2000)) {
                TraceLog.get().message("context.macSessionKey.data=" + HexBuilder.toHex(context.macSessionKey.data, context.macSessionKey.len), 2000);
            }
        }
        if (TraceLog.get().canLog(2000)) {
            TraceLog.get().message("context.macSessionKey.data=" + HexBuilder.toHex(context.macSessionKey.data, context.macSessionKey.len), 2000);
        }
        TraceLog.get().exit(700);
    }

    static int spnegoClientGetCrypter1(SecurityContext context) {
        SpnegoLevel level = (SpnegoLevel)levels.get(context.level);
        return level.getCrypter1();
    }

    static Blob spnegoClientGetMacSessionKey(Object context) {
        SecurityContext scContext = (SecurityContext)context;
        return scContext.macSessionKey;
    }

    static int spnegoClientGetCrypter2(SecurityContext context) {
        SpnegoLevel level = (SpnegoLevel)levels.get(context.level);
        return level.getCrypter2();
    }

    static int spnegoClientGetCryptLevel(SecurityContext context) {
        return context.level;
    }

    public static boolean clientNegotiateSecurity(SecurityContext context, Blob blob, boolean restrictCrypt, String hostName, Blob sessionKey, Blob macSessionKey) throws NqException {
        TraceLog.get().enter(700);
        String macSessionKeyString = "null";
        if (null != macSessionKey && null != macSessionKey.data) {
            macSessionKeyString = HexBuilder.toHex(macSessionKey.data).toString();
        }
        TraceLog.get().message("context=" + context + "; sessionKey=" + macSessionKeyString, 2000);
        boolean result = false;
        if (!(context.credentials instanceof GssContextCredentials)) {
            context.sessionKey = sessionKey;
            context.macSessionKey = macSessionKey;
        } else {
            if (null != sessionKey) {
                sessionKey.data = (byte[])context.sessionKey.data.clone();
                sessionKey.len = context.sessionKey.len;
            }
            if (null != macSessionKey) {
                macSessionKey.data = (byte[])context.macSessionKey.data.clone();
                macSessionKey.len = context.macSessionKey.len;
            }
        }
        context.status = 2;
        byte[] principalByteArray = null;
        byte[] ps = null;
        byte[] principalName = null;
        byte[] hostNameA = null;
        int PRINCIPAL_LENGTH = 512;
        if (null == blob || blob.len <= 0) {
            context.mechanism = clientMechanisms[0];
            context.extendedContext = clientMechanisms[0].contextCreate(null, restrictCrypt);
            context.status = 1;
            result = null != context.extendedContext;
            TraceLog.get().exit(700);
            return result;
        }
        BufferReader reader = new BufferReader(blob.data, 0, false);
        int len = 0;
        int mechListLen = 0;
        int[] tag_len = Asn1.asn1ParseTag(reader, len);
        int tag = tag_len[0];
        len = tag_len[1];
        if (tag != 96) {
            Spnego.logBadTag(96, tag);
            TraceLog.get().exit(700);
            throw new NqException(-21);
        }
        if (!Asn1.asn1ParseCompareOid(reader, Gss.gssApiOidSpnego, true)) {
            TraceLog.get().exit(700);
            throw new NqException(-21);
        }
        tag_len = Asn1.asn1ParseTag(reader, len);
        tag = tag_len[0];
        len = tag_len[1];
        if (160 != tag) {
            Spnego.logBadTag(160, tag);
            TraceLog.get().exit(700);
            throw new NqException(-21);
        }
        tag_len = Asn1.asn1ParseTag(reader, len);
        tag = tag_len[0];
        len = tag_len[1];
        if (48 != tag) {
            Spnego.logBadTag(48, tag);
            TraceLog.get().exit(700);
            throw new NqException(-21);
        }
        tag_len = Asn1.asn1ParseTag(reader, len);
        tag = tag_len[0];
        len = tag_len[1];
        if (160 != tag) {
            Spnego.logBadTag(160, tag);
            TraceLog.get().exit(700);
            throw new NqException(-21);
        }
        tag_len = Asn1.asn1ParseTag(reader, mechListLen);
        tag = tag_len[0];
        mechListLen = tag_len[1];
        if (48 != tag) {
            Spnego.logBadTag(48, tag);
            TraceLog.get().exit(700);
            throw new NqException(-21);
        }
        int listStart = reader.getOffset();
        int listEnd = listStart + mechListLen;
        block2: for (int i = clientMechanisms.length - 1; i >= 0; --i) {
            reader.setOffset(listStart);
            while (reader.getOffset() < listEnd) {
                if (!Asn1.asn1ParseCompareOid(reader, clientMechanisms[i].getReqOid(), true) && !Asn1.asn1ParseCompareOid(reader, clientMechanisms[i].getOid(), false) || 0 == (clientMechanisms[i].getMask() & ((SpnegoLevel)levels.get(context.level)).getMask())) continue;
                if (!clientMechanisms[i].setMechanism(context.extendedContext, clientMechanisms[i].getName())) {
                    if (!result) continue block2;
                    TraceLog.get().exit(700);
                    return result;
                }
                context.mechanism = clientMechanisms[i];
                if (clientMechanisms[i].getName().equals("NTLMSSP")) {
                    context.extendedContext = clientMechanisms[i].contextCreate(null, restrictCrypt);
                } else if (clientMechanisms[i].getName().equals("KERBEROS")) {
                    ((clientMechanismKERBEROS)Spnego.clientMechanisms[i]).credentials = context.credentials;
                    reader.setOffset(listEnd);
                    if (reader.getOffset() == blob.len) {
                        len = 0;
                        ps = principalByteArray;
                    } else {
                        tag_len = Asn1.asn1ParseTag(reader, len);
                        tag = tag_len[0];
                        len = tag_len[1];
                        if (162 == tag) {
                            reader.skip(len);
                            tag_len = Asn1.asn1ParseTag(reader, len);
                            tag = tag_len[0];
                            len = tag_len[1];
                        } else if (163 != tag) {
                            Spnego.logBadTag(163, tag);
                            TraceLog.get().exit(700);
                            throw new NqException(-21);
                        }
                        tag_len = Asn1.asn1ParseTag(reader, len);
                        tag = tag_len[0];
                        len = tag_len[1];
                        if (48 != tag) {
                            Spnego.logBadTag(48, tag);
                            TraceLog.get().exit(700);
                            throw new NqException(-21);
                        }
                        tag_len = Asn1.asn1ParseTag(reader, len);
                        tag = tag_len[0];
                        len = tag_len[1];
                        if (160 != tag) {
                            Spnego.logBadTag(160, tag);
                            TraceLog.get().exit(700);
                            throw new NqException(-21);
                        }
                        tag_len = Asn1.asn1ParseTag(reader, len);
                        tag = tag_len[0];
                        len = tag_len[1];
                        if (27 != tag) {
                            Spnego.logBadTag(27, tag);
                            TraceLog.get().exit(700);
                            throw new NqException(-21);
                        }
                    }
                    if (len > 0) {
                        principalByteArray = new byte[len];
                        reader.readBytes(principalByteArray, len);
                        int atIdx = new String(principalByteArray).indexOf(64);
                        int psLen = len - atIdx;
                        if (psLen == len) {
                            ps = principalByteArray;
                        } else {
                            ps = new byte[psLen - 1];
                            System.arraycopy(principalByteArray, atIdx + 1, ps, 0, ps.length);
                        }
                        TraceLog.get().message("principalByteArray = " + HexBuilder.toHex(principalByteArray) + "; ps = " + HexBuilder.toHex(ps), 700);
                    }
                    if (len == 0 || null != ps && new String(ps).equals("please_ignore")) {
                        principalName = new byte[]{};
                        try {
                            hostNameA = null != hostName && !hostName.equals("") ? hostName.getBytes("ASCII") : new byte[]{0};
                            if (0 != hostNameA[0]) {
                                principalName = ("cifs/" + new String(hostNameA)).getBytes("ASCII");
                            }
                        }
                        catch (UnsupportedEncodingException e) {
                            TraceLog.get().error("ASCII is not supported");
                        }
                        TraceLog.get().message("principalName = " + HexBuilder.toHex(principalName), 700);
                        context.extendedContext = (byte[])clientMechanisms[i].contextCreate(principalName, restrictCrypt);
                    } else {
                        String tmpString = new String(principalByteArray);
                        int ptr = tmpString.indexOf(64);
                        if (ptr > 0) {
                            principalByteArray = tmpString.substring(0, ptr).getBytes();
                        }
                        TraceLog.get().message("principalByteArray = " + HexBuilder.toHex(principalByteArray), 700);
                        context.extendedContext = (byte[])clientMechanisms[i].contextCreate(principalByteArray, restrictCrypt);
                    }
                }
                if (context.extendedContext == null) continue;
                context.status = 1;
                TraceLog.get().exit(700);
                return true;
            }
        }
        TraceLog.get().exit(700);
        return result;
    }

    public static Blob clientGenerateFirstBlob(SecurityContext context) {
        TraceLog.get().enter(700);
        Blob blob = new Blob();
        Blob resBlob = new Blob();
        BufferWriter writer = null;
        Blob mechTokenBlob = new Blob();
        context.status = 2;
        if (!context.mechanism.contextIsValid(context)) {
            TraceLog.get().exit(700);
            return null;
        }
        if (!context.mechanism.generateFirstRequest(context.extendedContext, context.mechanism, mechTokenBlob)) {
            TraceLog.get().exit(700);
            return null;
        }
        if (!context.isServerSupportingGSSAPI) {
            blob.data = (byte[])mechTokenBlob.data.clone();
            blob.len = mechTokenBlob.len;
            writer = new BufferWriter(blob.data, 0, false);
        } else if (!(context.credentials instanceof GssContextCredentials)) {
            writer = context.mechanism.packNegotBlob(context, mechTokenBlob.len, blob);
        } else {
            blob.data = (byte[])mechTokenBlob.data.clone();
            blob.len = mechTokenBlob.len;
            writer = new BufferWriter(blob.data, 0, false);
        }
        writer.writeBytes(mechTokenBlob.data, mechTokenBlob.len);
        context.status = 3;
        resBlob = blob;
        if (context.mechanism.getName().equals("KERBEROS")) {
            Spnego.getSessionKey(context);
        }
        TraceLog.get().exit(700);
        return resBlob;
    }

    public static Blob clientAcceptNextBlob(SecurityContext context, Blob inBlob) throws NqException {
        int tag;
        int[] tag_leng;
        TraceLog.get().enter(700);
        if (null == inBlob) {
            throw new NqException("inBlob is null", -20);
        }
        Blob mechBlob = new Blob();
        int len = 0;
        Blob newBlob = new Blob();
        Blob resBlob = new Blob();
        if (context.isServerSupportingGSSAPI) {
            if (!context.mechanism.contextIsValid(context)) {
                context.status = 2;
                TraceLog.get().exit(700);
                return resBlob;
            }
            if (0 == inBlob.len) {
                if (context.status == 3) {
                    context.status = 0;
                    Spnego.getSessionKey(context);
                    TraceLog.get().exit(700);
                    return resBlob;
                }
                context.status = 2;
                TraceLog.get().exit(700);
                return resBlob;
            }
        }
        context.status = 2;
        BufferReader reader = new BufferReader(inBlob.data, 0, false);
        if (context.isServerSupportingGSSAPI) {
            boolean complete;
            tag_leng = Asn1.asn1ParseTag(reader, len);
            tag = tag_leng[0];
            if (161 != tag) {
                Spnego.logBadTag(161, tag);
                TraceLog.get().exit(700);
                return resBlob;
            }
            tag_leng = Asn1.asn1ParseTag(reader, len);
            tag = tag_leng[0];
            if (48 != tag) {
                Spnego.logBadTag(48, tag);
                TraceLog.get().exit(700);
                return resBlob;
            }
            tag_leng = Asn1.asn1ParseTag(reader, len);
            tag = tag_leng[0];
            if (160 != tag) {
                Spnego.logBadTag(160, tag);
                TraceLog.get().exit(700);
                return resBlob;
            }
            tag_leng = Asn1.asn1ParseTag(reader, len);
            tag = tag_leng[0];
            len = tag_leng[1];
            if (10 != tag || len != 1) {
                Spnego.logBadTag(10, tag);
                TraceLog.get().exit(700);
                return resBlob;
            }
            byte b = reader.readByte();
            switch (b) {
                case 1: {
                    complete = false;
                    break;
                }
                case 0: {
                    complete = true;
                    break;
                }
                default: {
                    TraceLog.get().exit(700);
                    return resBlob;
                }
            }
            if (complete) {
                Spnego.getSessionKey(context);
                context.status = 0;
                TraceLog.get().exit(700);
                return resBlob;
            }
            tag_leng = Asn1.asn1ParseTag(reader, len);
            tag = tag_leng[0];
            if (161 != tag) {
                Spnego.logBadTag(161, tag);
                TraceLog.get().exit(700);
                return resBlob;
            }
            if (!Asn1.asn1ParseCompareOid(reader, context.mechanism.getOid(), true)) {
                TraceLog.get().exit(700);
                return resBlob;
            }
        }
        if (inBlob.len > reader.getOffset()) {
            if (context.isServerSupportingGSSAPI) {
                tag_leng = Asn1.asn1ParseTag(reader, len);
                tag = tag_leng[0];
                if (162 != tag) {
                    Spnego.logBadTag(162, tag);
                    TraceLog.get().exit(700);
                    return resBlob;
                }
                tag_leng = Asn1.asn1ParseTag(reader, len);
                tag = tag_leng[0];
                len = tag_leng[1];
                if (4 != tag) {
                    Spnego.logBadTag(4, tag);
                    TraceLog.get().exit(700);
                    return resBlob;
                }
            }
            if (!context.isServerSupportingGSSAPI) {
                len = inBlob.len;
            }
            mechBlob.len = len;
            mechBlob.data = new byte[mechBlob.len];
            reader.readBytes(mechBlob.data, mechBlob.len);
            Blob tmp = new Blob(mechBlob.len);
            if (!context.mechanism.generateNextRequest(context.extendedContext, mechBlob.data, mechBlob.len, tmp, context)) {
                TraceLog.get().exit(700);
                return resBlob;
            }
            mechBlob = tmp;
            if (context.isServerSupportingGSSAPI) {
                newBlob.len = 30 + mechBlob.len;
                newBlob.data = new byte[newBlob.len];
                int mechtokenLen = 1 + Asn1.asn1PackLen(mechBlob.len) + mechBlob.len;
                int negtokenLen = 1 + Asn1.asn1PackLen(mechtokenLen) + mechtokenLen;
                int gssapiLen = 1 + Asn1.asn1PackLen(negtokenLen) + negtokenLen;
                if (newBlob.len < 1 + Asn1.asn1PackLen(gssapiLen) + gssapiLen) {
                    newBlob = null;
                    TraceLog.get().exit(700);
                    return resBlob;
                }
                newBlob.len = 1 + Asn1.asn1PackLen(gssapiLen) + gssapiLen;
                BufferWriter writer = new BufferWriter(newBlob.data, 0, false);
                Asn1.asn1PackTag(writer, 161, gssapiLen);
                Asn1.asn1PackTag(writer, 48, negtokenLen);
                Asn1.asn1PackTag(writer, 162, mechtokenLen);
                Asn1.asn1PackTag(writer, 4, mechBlob.len);
                writer.writeBytes(mechBlob.data, mechBlob.len);
            }
        }
        if (!context.isServerSupportingGSSAPI) {
            context.status = 0;
            resBlob = mechBlob;
        } else {
            context.status = 3;
            resBlob = newBlob;
        }
        TraceLog.get().exit(700);
        return resBlob;
    }

    private static void logBadTag(int asn1Application, int tag) {
    }

    public static synchronized int spnegoLogon(Object callingContext, Server server, Credentials credentials, boolean restrictCrypters, Blob sessionKey, Blob macKey, SpnegoClientExchange spnegoExchange, MountParams mountParams) throws NqException {
        TraceLog.get().enter(700);
        String serverName = server.getName();
        Blob firstSecurityBlob = server.firstSecurityBlob;
        Blob outBlob = null;
        Blob inBlob = new Blob();
        SecurityContext securityContext = null;
        int KERBEROSLEVEL = 4;
        boolean isSubCred = credentials instanceof SubjectCredentials;
        boolean isGssCred = credentials instanceof GssContextCredentials;
        block9: for (int level = mountParams.maxSecurityLevel; level >= mountParams.minSecurityLevel; --level) {
            int status;
            TraceLog.get().message("Spnego level= " + level, 2000);
            if (null != securityContext) {
                securityContext = null;
                if (null != sessionKey) {
                    sessionKey.data = null;
                }
                if (null != macKey) {
                    macKey.data = null;
                }
            }
            securityContext = new SecurityContext(credentials, level);
            securityContext.isServerSupportingGSSAPI = server.isServerSupportingGSSAPI();
            if ((isSubCred || isGssCred) && KERBEROSLEVEL > securityContext.level) {
                TraceLog.get().exit(700);
                return 2;
            }
            if (!Spnego.clientNegotiateSecurity(securityContext, firstSecurityBlob, restrictCrypters, serverName, sessionKey, macKey)) {
                securityContext = null;
                if (null != sessionKey) {
                    sessionKey.data = null;
                }
                TraceLog.get().message("clientNegotiateSecurity returned false", 2000);
                continue;
            }
            if (!(!isSubCred && !isGssCred || KERBEROSLEVEL <= securityContext.level && securityContext.mechanism.getName().equals("KERBEROS"))) {
                TraceLog.get().exit(700);
                return 2;
            }
            inBlob.data = null;
            do {
                status = securityContext.status;
                switch (status) {
                    case 1: {
                        outBlob = Spnego.clientGenerateFirstBlob(securityContext);
                        break;
                    }
                    case 3: {
                        outBlob = Spnego.clientAcceptNextBlob(securityContext, inBlob);
                        break;
                    }
                    case 4: {
                        inBlob.data = null;
                        break;
                    }
                    case 2: {
                        inBlob.data = null;
                        break;
                    }
                    case 0: {
                        inBlob.data = null;
                        securityContext = null;
                        return 0;
                    }
                }
                inBlob.data = null;
                if (null == outBlob || null == outBlob.data || 2 == securityContext.status) {
                    if (0 != securityContext.status) continue block9;
                    securityContext = null;
                    TraceLog.get().exit(700);
                    return 0;
                }
                try {
                    status = spnegoExchange.exchange(callingContext, outBlob, inBlob);
                    TraceLog.get().message("sstp res = " + status, 700);
                }
                catch (SmbException e) {
                    TraceLog.get().error("Connection failed with level " + level + ": ", e, 700, e.getErrCode());
                    continue block9;
                }
                outBlob = null;
            } while (0 == status || -1 == status);
            if (-2 == status || status == -3) {
                inBlob.data = null;
                securityContext = null;
                if (null != sessionKey) {
                    sessionKey = null;
                }
                if (null != macKey) {
                    macKey = null;
                }
                TraceLog.get().exit(700);
                return 2;
            }
            inBlob.data = null;
        }
        securityContext = null;
        TraceLog.get().exit(700);
        return 2;
    }

    public static void defineSpnegoLevel(int level, int crypter1, int crypter2, int mechanisms) {
        if (level > AM_MAXSECURITYLEVEL + 1) {
            return;
        }
        ((SpnegoLevel)Spnego.levels.get((int)level)).crypter1 = crypter1;
        ((SpnegoLevel)Spnego.levels.get((int)level)).crypter2 = crypter2;
        ((SpnegoLevel)Spnego.levels.get((int)level)).mask = mechanisms;
        if (mechanisms != 0 && crypter1 != 0 && crypter2 != 0) {
            AM_CURRAUTHLEVEL = level;
        }
    }

    public static void spnegoSetAuthLevel(int authenticationLevel) {
        if (authenticationLevel > AM_MAXSECURITYLEVEL || authenticationLevel < AM_MINSECURITYLEVEL) {
            return;
        }
        for (int i = AM_MINSECURITYLEVEL; i < AM_MAXSECURITYLEVEL + 1; ++i) {
            if (i == authenticationLevel) {
                Spnego.defineSpnegoLevel(i, ((SpnegoLevel)Spnego.defaultLevels.get((int)i)).crypter1, ((SpnegoLevel)Spnego.defaultLevels.get((int)i)).crypter2, ((SpnegoLevel)Spnego.defaultLevels.get((int)i)).mask);
                continue;
            }
            Spnego.defineSpnegoLevel(i, 0, 0, 0);
        }
        AM_CURRAUTHLEVEL = authenticationLevel;
    }

    static {
        Spnego.clientMechanisms[0] = new clientMechanismNTLM();
        if (isKerberosSupported) {
            Spnego.clientMechanisms[1] = new clientMechanismKERBEROS();
        }
        levels.add(new SpnegoLevel(0, 0, 0));
        levels.add(new SpnegoLevel(0, 0, 0));
        levels.add(new SpnegoLevel(0, 0, 0));
        levels.add(new SpnegoLevel(3, 4, 1));
        levels.add(new SpnegoLevel(3, 4, 2));
        levels.add(new SpnegoLevel(3, 4, 0));
        defaultLevels = new ArrayList(levels);
    }

    public static class SpnegoLevel {
        int crypter1;
        int crypter2;
        int mask;

        public int getCrypter1() {
            return this.crypter1;
        }

        public void setCrypter1(int crypter1) {
            this.crypter1 = crypter1;
        }

        public int getCrypter2() {
            return this.crypter2;
        }

        public void setCrypter2(int crypter2) {
            this.crypter2 = crypter2;
        }

        public int getMask() {
            return this.mask;
        }

        public void setMask(int mask) {
            this.mask = mask;
        }

        public SpnegoLevel(int crypter1, int crypter2, int mask) {
            this.crypter1 = crypter1;
            this.crypter2 = crypter2;
            this.mask = mask;
        }
    }

    static class clientMechanismKERBEROS
    implements SecurityMechanism {
        private RealmContext realmContext;
        private ServerContext serverContext;
        private String kdc = null;
        private String realm = null;
        public Credentials credentials;

        clientMechanismKERBEROS() {
        }

        public Object contextCreate(Object object, boolean restrictCrypt) {
            TraceLog.get().enter(2000);
            try {
                String principal;
                String host = principal = new String((byte[])object);
                if (principal.length() >= "cifs/".length() && principal.substring(0, "cifs/".length()).equals("cifs/")) {
                    host = principal.substring("cifs/".length());
                }
                if (principal.endsWith("$")) {
                    host = principal.substring(0, principal.length() - 1);
                }
                TraceLog.get().message("host=" + host, 2000);
                if (this.credentials instanceof SubjectCredentials) {
                    TraceLog.get().message("Using SubjectCredentials", 2000);
                    this.realmContext = RealmManager.getInstance().getRealmContext(((SubjectCredentials)this.credentials).getSubject(), ((SubjectCredentials)this.credentials).getDomain());
                    this.serverContext = this.realmContext.createServerContext(host, false, false);
                } else if (!(this.credentials instanceof GssContextCredentials)) {
                    TraceLog.get().message("Using PasswordCredentials", 2000);
                    if (null == this.realm) {
                        this.realm = Config.jnq.getString("REALM");
                    }
                    if (null == this.kdc) {
                        this.kdc = Config.jnq.getString("KDC");
                    }
                    String user = ((PasswordCredentials)this.credentials).getUser();
                    String password = ((PasswordCredentials)this.credentials).getPassword();
                    this.realmContext = RealmManager.getInstance().getRealmContext(this.kdc, this.realm, user, password);
                    this.serverContext = this.realmContext.createServerContext(host, false, false);
                }
                TraceLog.get().exit(2000);
                return new byte[2000];
            }
            catch (Exception e) {
                TraceLog.get().message("Unable to authenticate with Kerberos: " + e.getClass().getName() + ":", e, 2000);
                TraceLog.get().exit(2000);
                return null;
            }
        }

        public String getKdc() {
            return this.kdc;
        }

        public void setKdc(String kdc) {
            this.kdc = kdc;
        }

        public String getRealm() {
            return this.realm;
        }

        public void setRealm(String realm) {
            this.realm = realm;
        }

        public boolean setMechanism(Object ctx, String name) {
            return true;
        }

        public boolean generateFirstRequest(Object ctxt, SecurityMechanism mechList, Blob blob) {
            boolean result = true;
            try {
                if (this.credentials instanceof GssContextCredentials) {
                    GssContextCredentials gssCreds = (GssContextCredentials)this.credentials;
                    if (null == gssCreds.getGssApiToken() || null == gssCreds.getGssApiToken().data || 0 == gssCreds.getGssApiToken().len) {
                        result = false;
                    } else {
                        blob.data = gssCreds.getGssApiToken().data;
                        blob.len = gssCreds.getGssApiToken().len;
                    }
                } else {
                    byte[] firstRequest = this.serverContext.generateFirstRequest();
                    blob.data = firstRequest;
                    blob.len = firstRequest.length;
                }
            }
            catch (Exception e) {
                TraceLog.get().error("exception=" + e.getClass().getName() + " ; ", e, 10, 0);
                TraceLog.get().caught(e, 10);
                result = false;
            }
            return result;
        }

        public boolean generateNextRequest(Object ctxt, byte[] inBlob, int inBlobLen, Blob outBlob, SecurityContext spnegoContext) {
            try {
                byte[] nextRequest = this.serverContext.generateNextRequest(inBlob, 0, inBlobLen);
                outBlob.data = new byte[outBlob.data.length];
                outBlob.len = outBlob.data.length;
                System.arraycopy(nextRequest, 0, outBlob.data, 0, nextRequest.length);
                return true;
            }
            catch (Exception e) {
                TraceLog.get().error("Exception = ", e, 2000, 0);
                return false;
            }
        }

        public boolean getSessionKey(Object p, byte[] buffer, int len) {
            TraceLog.get().enter(700);
            byte[] sessionKey = null;
            try {
                if (this.credentials instanceof GssContextCredentials) {
                    GssContextCredentials gssCreds = (GssContextCredentials)this.credentials;
                    if (null == gssCreds.getSessionKey() || null == gssCreds.getSessionKey().data) {
                        TraceLog.get().exit(700);
                        return false;
                    }
                    sessionKey = gssCreds.getSessionKey().data;
                } else {
                    sessionKey = this.serverContext.getSessionKey();
                    if (null == sessionKey) {
                        TraceLog.get().exit(700);
                        return false;
                    }
                }
                if (sessionKey.length > len) {
                    System.arraycopy(sessionKey, 0, buffer, 0, len);
                } else {
                    System.arraycopy(sessionKey, 0, buffer, 0, sessionKey.length);
                }
                TraceLog.get().exit(700);
                return true;
            }
            catch (Exception e) {
                TraceLog.get().error("Exception = ", e, 2000, 0);
                TraceLog.get().caught(e, 2000);
                TraceLog.get().exit(700);
                return false;
            }
        }

        public boolean contextIsValid(SecurityContext context) {
            return context != null;
        }

        public BufferWriter packNegotBlob(SecurityContext context, int mechtokenBlobLen, Blob blob) {
            int oidLen = Gss.gssApiOidKerberos.len;
            int mechtypesSeqLen = 1 + Asn1.asn1PackLen(oidLen) + oidLen;
            int mechtypesLen = 1 + Asn1.asn1PackLen(mechtypesSeqLen) + mechtypesSeqLen;
            int mechtokenAppLen = 1 + Asn1.asn1PackLen(oidLen) + oidLen + 2 + mechtokenBlobLen;
            int mechtokenBinLen = 1 + Asn1.asn1PackLen(mechtokenAppLen) + mechtokenAppLen;
            int mechtokenLen = 1 + Asn1.asn1PackLen(mechtokenBlobLen) + mechtokenBlobLen;
            int negtokenLen = 1 + Asn1.asn1PackLen(mechtypesLen) + mechtypesLen + 1 + Asn1.asn1PackLen(mechtokenLen) + mechtokenLen;
            int spnegoLen = 1 + Asn1.asn1PackLen(negtokenLen) + negtokenLen;
            int gssapiLen = 1 + Asn1.asn1PackLen(spnegoLen) + spnegoLen + 1 + Asn1.asn1PackLen(Gss.gssApiOidSpnego.len) + Gss.gssApiOidSpnego.len;
            int totalLen = 1 + Asn1.asn1PackLen(gssapiLen) + gssapiLen;
            blob.data = new byte[totalLen];
            blob.len = totalLen;
            BufferWriter writer = new BufferWriter(blob.data, 0, false);
            Asn1.asn1PackTag(writer, 96, gssapiLen);
            Asn1.asn1PackOid(writer, Gss.gssApiOidSpnego);
            Asn1.asn1PackTag(writer, 160, spnegoLen);
            Asn1.asn1PackTag(writer, 48, negtokenLen);
            Asn1.asn1PackTag(writer, 160, mechtypesLen);
            Asn1.asn1PackTag(writer, 48, mechtypesSeqLen);
            Asn1.asn1PackOid(writer, Gss.gssApiOidKerberos);
            Asn1.asn1PackTag(writer, 162, mechtokenLen);
            Asn1.asn1PackTag(writer, 4, mechtokenBlobLen);
            return writer;
        }

        public int getMask() {
            return 2;
        }

        public Blob getOid() {
            return Gss.gssApiOidMsKerberos;
        }

        public Blob getReqOid() {
            return Gss.gssApiOidKerberos;
        }

        public String getName() {
            return "KERBEROS";
        }
    }

    static class clientMechanismNTLM
    implements SecurityMechanism {
        private static final int NTLMSSP_NEGOTIATE = 1;
        private static final int NTLMSSP_CHALLENGE = 2;
        private static final int NTLMSSP_AUTH = 3;
        private static final int NTLMSSP_SIGNATURE = 4;
        private static final int NTLMSSP_NEGOTIATE_UNICODE = 1;
        private static final int NTLMSSP_NEGOTIATE_REQUEST_TARGET = 4;
        private static final int NTLMSSP_NEGOTIATE_NTLM = 512;
        private static final int NTLMSSP_NEGOTIATE_EXTENDED_SECURITY = 524288;
        private static final int NTLMSSP_NEGOTIATE_128 = 0x20000000;
        private static final int NTLMSSP_NEGOTIATE_56 = Integer.MIN_VALUE;
        private static final int NTLMSSP_NEGOTIATE_KEY_EXCH = 0x40000000;
        private static final int NTLMSSP_NEGOTIATE_LAN_MANAGER = 128;
        private static final int NTLMSSP_NEGOTIATE_TARGET_INFO = 0x800000;
        private static final int NTLMSSP_NEGOTIATE_ANONYMOUS = 2048;
        private static final int NTLMSSP_NEGOTIATE_TARGET_TYPE_DOMAIN = 65536;
        private static final int NTLMSSP_NEGOTIATE_TARGET_TYPE_SERVER = 131072;
        private static final int NTLMSSP_NEGOTIATE_SIGN = 16;
        private static final int NTLMSSP_NEGOTIATE_SEAL = 32;
        public static final int CM_CRYPT_NTLMV2RESPONSESIZE = 52;
        public static final int CM_CRYPT_MAX_NTLMV2NTLMSSPRESPONSESIZE = 1952;
        private static final short ITEMTYPE_TERMINATOR = 0;
        private static final short ITEMTYPE_NETBIOSHOST = 1;
        private static final short ITEMTYPE_NETBIOSDOMAIN = 2;
        private static final short ITEMTYPE_DNSHOST = 3;
        private static final short ITEMTYPE_DNSDOMAIN = 4;
        private static final short ITEMTYPE_TIMESTAMP = 7;
        private static final int NTLMSSP_CLIENT_NEGOTIATE_FLAGS = 1611137557;

        clientMechanismNTLM() {
        }

        public Object contextCreate(Object object, boolean restrictCrypt) {
            return new NtlmContext(new byte[1952]);
        }

        public boolean setMechanism(Object ctx, String name) {
            return true;
        }

        public boolean generateFirstRequest(Object ctxt, SecurityMechanism mechList, Blob blob) {
            NtlmContext ntlmContext = (NtlmContext)ctxt;
            ntlmContext.negotiateFlags = 1611137557;
            BufferWriter writer = new BufferWriter(ntlmContext.data, 0, false);
            writer.writeBytes(this.getName().getBytes(), this.getName().length());
            writer.writeByte((byte)0);
            writer.writeInt4(1);
            writer.writeInt4(ntlmContext.negotiateFlags);
            writer.writeInt4(0);
            writer.writeInt4(0);
            writer.writeInt4(0);
            writer.writeInt4(0);
            blob.len = writer.getOffset();
            blob.data = new byte[blob.len];
            System.arraycopy(ntlmContext.data, 0, blob.data, 0, blob.len);
            return true;
        }

        public boolean generateNextRequest(Object ctxt, byte[] inBlob, int inBlobLen, Blob outBlob, SecurityContext spnegoContext) throws NqException {
            Blob macSessionKey;
            NtlmContext ntlmContext = (NtlmContext)ctxt;
            byte[] ntlmChallenge = new byte[8];
            byte[] sessKeyEnc = new byte[16];
            boolean ret = false;
            BufferReader reader = new BufferReader(inBlob, 0, false);
            int base = reader.getOffset();
            reader.skip(8);
            reader.skip(4);
            reader.skip(2);
            reader.skip(2);
            reader.skip(4);
            int flags = reader.readInt4();
            if (0 == (flags & 0x40000000)) {
                ntlmContext.negotiateFlags &= -1073741825;
            }
            reader.readBytes(ntlmChallenge, ntlmChallenge.length);
            Blob sessionKey = spnegoContext.sessionKey;
            if (null != sessionKey) {
                Blob sessionKeyTmp = new Blob(ntlmChallenge);
                sessionKey.data = sessionKeyTmp.data;
                sessionKey.len = sessionKeyTmp.len;
            }
            reader.skip(8);
            short targetInfoLength = reader.readInt2();
            reader.skip(2);
            int targetInfoOffset = reader.readInt4();
            Blob namesBlob = new Blob(targetInfoLength);
            if (targetInfoLength > 0) {
                int savedOffseg = reader.getOffset();
                reader.setOffset(base + targetInfoOffset);
                reader.readBytes(namesBlob.data, targetInfoLength);
                reader.setOffset(savedOffseg);
                namesBlob.len = targetInfoLength;
            } else {
                namesBlob.data = null;
                namesBlob.len = 0;
            }
            int[] timeNow = TimeUtility.getCurrentTimeAsArray();
            int[] timeStamp = new int[]{0, 0};
            timeStamp = null == timeNow ? clientMechanismNTLM.getTimeStamp(inBlob) : timeNow;
            PasswordCredentials credentials = (PasswordCredentials)spnegoContext.credentials;
            boolean userIsAnonymous = credentials.isAnonymous();
            Crypt crypt = new Crypt();
            if (!userIsAnonymous && !Crypt.cryptEncrypt(spnegoContext.credentials, Spnego.spnegoClientGetCrypter1(spnegoContext), Spnego.spnegoClientGetCrypter2(spnegoContext), sessionKey.data, namesBlob, timeStamp, crypt)) {
                return ret;
            }
            if (!userIsAnonymous && Spnego.spnegoClientGetCrypter1(spnegoContext) == 1 && Spnego.spnegoClientGetCrypter2(spnegoContext) == 2) {
                byte[] keyBuf = new byte[16];
                Blob newMac = new Blob(crypt.macKey);
                Crypt oldCrypt = new Crypt();
                oldCrypt.pass1 = new Blob(crypt.pass1);
                System.arraycopy(sessionKey.data, 0, keyBuf, 0, sessionKey.len);
                System.arraycopy(crypt.pass1.data, 0, keyBuf, sessionKey.len, sessionKey.len);
                newMac.data = new byte[16];
                newMac.len = newMac.data.length;
                System.arraycopy(keyBuf, 0, newMac.data, 0, keyBuf.length);
                MD5.cmMD5(keyBuf, keyBuf, 16);
                sessionKey.data = new byte[sessionKey.len];
                System.arraycopy(keyBuf, 0, sessionKey.data, 0, sessionKey.len);
                crypt.pass1.data = null;
                crypt.pass2.data = null;
                crypt.macKey.data = null;
                crypt.response.data = null;
                Crypt.cryptEncrypt(credentials, Spnego.spnegoClientGetCrypter1(spnegoContext), Spnego.spnegoClientGetCrypter2(spnegoContext), sessionKey.data, namesBlob, timeStamp, crypt);
                crypt.pass1.data = null;
                crypt.pass1 = oldCrypt.pass1;
                HMACMD5.generateExtSecuritySessionKey(crypt.macKey.data, newMac.data, crypt.macKey.data);
                oldCrypt = null;
                newMac = null;
            }
            if (!userIsAnonymous && 0 != (ntlmContext.negotiateFlags & 0x40000000)) {
                byte[] newMacKey = new byte[16];
                SecureRandom secRandom = new SecureRandom();
                secRandom.nextBytes(newMacKey);
                System.arraycopy(newMacKey, 0, sessKeyEnc, 0, 16);
                RC4.Crypt(sessKeyEnc, sessKeyEnc.length, crypt.macKey.data, crypt.macKey.len);
                crypt.macKey.data = newMacKey;
                crypt.macKey.len = newMacKey.length;
            }
            if (!userIsAnonymous && null != (macSessionKey = Spnego.spnegoClientGetMacSessionKey(spnegoContext)) && null == macSessionKey.data && null != crypt.macKey.data) {
                Blob macSessionKeyTmp = new Blob(crypt.macKey);
                macSessionKey.data = macSessionKeyTmp.data;
                macSessionKey.len = macSessionKeyTmp.len;
            }
            BufferWriter out = new BufferWriter(ntlmContext.data, 0, false);
            base = out.getOffset();
            out.writeBytes("NTLMSSP".getBytes(), "NTLMSSP".length());
            out.writeByte((byte)0);
            out.writeInt4(3);
            int refLM = out.getOffset();
            out.skip(8);
            int refNTLM = out.getOffset();
            out.skip(8);
            int refDomain = out.getOffset();
            out.skip(8);
            int refUser = out.getOffset();
            out.skip(8);
            int refHost = out.getOffset();
            out.skip(8);
            int refSession = out.getOffset();
            out.skip(8);
            out.writeInt4(userIsAnonymous ? ntlmContext.negotiateFlags | 0x800 : ntlmContext.negotiateFlags);
            int p = out.getOffset();
            if (!new String(credentials.getUser()).contains("@")) {
                try {
                    out.writeBytes(credentials.getDomain().toUpperCase().getBytes("UTF-16LE"));
                }
                catch (UnsupportedEncodingException e1) {
                    throw new NqException("Unsupported UTF-16", -22);
                }
            }
            clientMechanismNTLM.packRefData(out, base, refDomain, p, out.getOffset());
            p = out.getOffset();
            try {
                out.writeBytes(credentials.getUser().getBytes("UTF-16LE"));
            }
            catch (UnsupportedEncodingException e1) {
                throw new NqException("Unsupported UTF-16", -22);
            }
            clientMechanismNTLM.packRefData(out, base, refUser, p, out.getOffset());
            try {
                String hostName = Utility.getHostName(true).toUpperCase();
                p = out.getOffset();
                out.writeBytes(hostName.getBytes("UTF-16LE"));
                clientMechanismNTLM.packRefData(out, base, refHost, p, out.getOffset());
            }
            catch (UnsupportedEncodingException e) {
                throw new NqException("Unsupported UTF-16", -22);
            }
            p = out.getOffset();
            if (!userIsAnonymous) {
                out.writeBytes(crypt.pass1.data, crypt.pass1.len);
            } else {
                out.writeByte((byte)0);
            }
            clientMechanismNTLM.packRefData(out, base, refLM, p, out.getOffset());
            p = out.getOffset();
            if (!userIsAnonymous) {
                out.writeBytes(crypt.pass2.data, crypt.pass2.len);
            }
            clientMechanismNTLM.packRefData(out, base, refNTLM, p, out.getOffset());
            if (!userIsAnonymous) {
                crypt = null;
            }
            p = out.getOffset();
            if (0 != (ntlmContext.negotiateFlags & 0x40000000)) {
                if (userIsAnonymous) {
                    SecureRandom secRandom = new SecureRandom();
                    secRandom.nextBytes(sessKeyEnc);
                }
                out.writeBytes(sessKeyEnc, sessKeyEnc.length);
            }
            clientMechanismNTLM.packRefData(out, base, refSession, p, out.getOffset());
            outBlob.data = new byte[out.getOffset()];
            System.arraycopy(ntlmContext.data, 0, outBlob.data, 0, outBlob.data.length);
            outBlob.len = out.getOffset();
            ret = true;
            return ret;
        }

        public boolean getSessionKey(Object p, byte[] buffer, int len) {
            return true;
        }

        public boolean contextIsValid(SecurityContext context) {
            return context != null;
        }

        public BufferWriter packNegotBlob(SecurityContext context, int mechtokenBlobLen, Blob blob) {
            int mechtokenBinLen = 1 + Asn1.asn1PackLen(mechtokenBlobLen) + mechtokenBlobLen;
            int mechtokenLen = 1 + Asn1.asn1PackLen(mechtokenBinLen) + mechtokenBinLen;
            int oidLen = 1 + Asn1.asn1PackLen(Gss.gssApiOidNtlmSsp.len) + Gss.gssApiOidNtlmSsp.len;
            int mechtypesSeqLen = 1 + Asn1.asn1PackLen(oidLen) + oidLen;
            int negtokenLen = 1 + Asn1.asn1PackLen(mechtypesSeqLen) + mechtypesSeqLen + mechtokenLen;
            int spnegoLen = 1 + Asn1.asn1PackLen(negtokenLen) + negtokenLen;
            int gssapiLen = 1 + Asn1.asn1PackLen(Gss.gssApiOidSpnego.len) + Gss.gssApiOidSpnego.len + 1 + Asn1.asn1PackLen(4) + spnegoLen;
            int totalLen = 1 + Asn1.asn1PackLen(gssapiLen) + gssapiLen;
            blob.data = new byte[totalLen];
            blob.len = totalLen;
            BufferWriter writer = new BufferWriter(blob.data, 0, false);
            Asn1.asn1PackTag(writer, 96, gssapiLen);
            Asn1.asn1PackOid(writer, Gss.gssApiOidSpnego);
            Asn1.asn1PackTag(writer, 160, spnegoLen);
            Asn1.asn1PackTag(writer, 48, negtokenLen);
            Asn1.asn1PackTag(writer, 160, mechtypesSeqLen);
            Asn1.asn1PackTag(writer, 48, oidLen);
            Asn1.asn1PackOid(writer, Gss.gssApiOidNtlmSsp);
            Asn1.asn1PackTag(writer, 162, mechtokenBinLen);
            Asn1.asn1PackTag(writer, 4, mechtokenBlobLen);
            return writer;
        }

        private static void packRefData(BufferWriter writer, int base, int writePosition, int start, int end) {
            int saved = writer.getOffset();
            writer.setOffset(writePosition);
            writer.writeInt2(end - start);
            writer.writeInt2(end - start);
            writer.writeInt4(start - base);
            writer.setOffset(saved);
        }

        private static int[] getTimeStamp(byte[] in) throws NqException {
            int[] timeStamp = new int[]{0, 0};
            BufferReader reader = new BufferReader(in, 0, false);
            int tag = 0;
            int len = 0;
            int[] tag_len = Asn1.asn1ParseTag(reader, len);
            tag = tag_len[0];
            len = tag_len[1];
            if (161 == tag) {
                tag_len = Asn1.asn1ParseTag(reader, len);
                tag = tag_len[0];
                len = tag_len[1];
                if (48 != tag) {
                    return timeStamp;
                }
                tag_len = Asn1.asn1ParseTag(reader, len);
                tag = tag_len[0];
                len = tag_len[1];
                if (160 != tag) {
                    return timeStamp;
                }
                tag_len = Asn1.asn1ParseTag(reader, len);
                tag = tag_len[0];
                len = tag_len[1];
                if (10 != tag || len != 1) {
                    return timeStamp;
                }
                tag_len = Asn1.asn1ParseTag(reader, len);
                tag = tag_len[0];
                len = tag_len[1];
                if (161 != tag) {
                    return timeStamp;
                }
                if (!Asn1.asn1ParseCompareOid(reader, Gss.gssApiOidNtlmSsp, true)) {
                    return timeStamp;
                }
                tag_len = Asn1.asn1ParseTag(reader, len);
                tag = tag_len[0];
                len = tag_len[1];
                if (162 != tag) {
                    return timeStamp;
                }
                tag_len = Asn1.asn1ParseTag(reader, len);
                tag = tag_len[0];
                len = tag_len[1];
                if (4 != tag) {
                    return timeStamp;
                }
            } else {
                reader.setOffset(0);
            }
            reader.skip(20);
            int flags = reader.readInt4();
            reader.skip(16);
            if (0 != (flags & 0x800000)) {
                reader.skip(4);
                int offTargetInfo = reader.readInt4();
                reader.setOffset(offTargetInfo);
                int timeStampPosition = clientMechanismNTLM.parseAddressListItem(reader, (short)7);
                if (-1 != timeStampPosition) {
                    reader.setOffset(timeStampPosition);
                    timeStamp[0] = reader.readInt4();
                    timeStamp[1] = reader.readInt4();
                }
            }
            return timeStamp;
        }

        private static int parseAddressListItem(BufferReader in, short type) throws NqException {
            int result = -1;
            while (true) {
                short id = in.readInt2();
                switch (id) {
                    case 0: {
                        return result;
                    }
                }
                short length = in.readInt2();
                if (type == id) {
                    result = in.getOffset();
                    return result;
                }
                in.skip(length);
            }
        }

        public int getMask() {
            return 1;
        }

        public Blob getOid() {
            return Gss.gssApiOidNtlmSsp;
        }

        public Blob getReqOid() {
            return Gss.gssApiOidNtlmSsp;
        }

        public String getName() {
            return "NTLMSSP";
        }

        static class NtlmContext {
            private int negotiateFlags = 0;
            private byte[] data;

            public NtlmContext(byte[] data) {
                this.data = data;
            }
        }
    }

    public static interface SpnegoClientExchange {
        public int exchange(Object var1, Blob var2, Blob var3) throws NqException;
    }
}

