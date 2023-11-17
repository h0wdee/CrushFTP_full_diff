/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.auth;

import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticateResponse;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.session.Session;
import java.io.IOException;

public interface Authenticator {
    public void init(SmbConfig var1);

    public boolean supports(AuthenticationContext var1);

    public AuthenticateResponse authenticate(AuthenticationContext var1, byte[] var2, Session var3) throws IOException;
}

