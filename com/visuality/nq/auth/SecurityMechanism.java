/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.auth.SecurityContext;
import com.visuality.nq.common.Blob;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.NqException;

public interface SecurityMechanism {
    public static final int AM_CRYPTER_NONE = 0;
    public static final int AM_CRYPTER_LM = 1;
    public static final int AM_CRYPTER_NTLM = 2;
    public static final int AM_CRYPTER_LM2 = 3;
    public static final int AM_CRYPTER_NTLM2 = 4;
    public static final String NTLMSSP_MECHANISM_NAME = "NTLMSSP";
    public static final String KERBEROS_MECHANISM_NAME = "KERBEROS";
    public static final int AM_MECH_NTLMSSP = 1;
    public static final int AM_MECH_KERBEROS = 2;

    public boolean setMechanism(Object var1, String var2);

    public boolean generateFirstRequest(Object var1, SecurityMechanism var2, Blob var3);

    public boolean generateNextRequest(Object var1, byte[] var2, int var3, Blob var4, SecurityContext var5) throws NqException;

    public boolean getSessionKey(Object var1, byte[] var2, int var3);

    public boolean contextIsValid(SecurityContext var1);

    public BufferWriter packNegotBlob(SecurityContext var1, int var2, Blob var3);

    public Object contextCreate(Object var1, boolean var2);

    public int getMask();

    public Blob getOid();

    public Blob getReqOid();

    public String getName();
}

