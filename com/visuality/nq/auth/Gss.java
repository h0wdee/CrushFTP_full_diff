/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.common.Blob;

class Gss {
    public static Blob gssApiOidSpnego = new Blob();
    public static Blob gssApiOidMsKerberos = new Blob();
    public static Blob gssApiOidKerberos = new Blob();
    public static Blob gssApiOidKerberosUserToUser = new Blob();
    public static Blob gssApiOidNtlmSsp = new Blob();
    private static final byte[] _spnego = new byte[]{43, 6, 1, 5, 5, 2};
    private static final byte[] _mskerberos = new byte[]{42, -122, 72, -126, -9, 18, 1, 2, 2};
    private static final byte[] _kerberos = new byte[]{42, -122, 72, -122, -9, 18, 1, 2, 2};
    private static final byte[] _kerberosutu = new byte[]{42, -122, 72, -122, -9, 18, 1, 2, 2, 3};
    private static final byte[] _ntlmssp = new byte[]{43, 6, 1, 4, 1, -126, 55, 2, 2, 10};

    Gss() {
    }

    static {
        Gss.gssApiOidSpnego.data = _spnego;
        Gss.gssApiOidSpnego.len = _spnego.length;
        Gss.gssApiOidMsKerberos.data = _mskerberos;
        Gss.gssApiOidMsKerberos.len = _mskerberos.length;
        Gss.gssApiOidKerberos.data = _kerberos;
        Gss.gssApiOidKerberos.len = _kerberos.length;
        Gss.gssApiOidKerberosUserToUser.data = _kerberosutu;
        Gss.gssApiOidKerberosUserToUser.len = _kerberosutu.length;
        Gss.gssApiOidNtlmSsp.data = _ntlmssp;
        Gss.gssApiOidNtlmSsp.len = _ntlmssp.length;
    }
}

