/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal.security;

import com.didisoft.pgp.bc.elgamal.security.InvalidParameterTypeException;
import com.didisoft.pgp.bc.elgamal.security.NoSuchParameterException;
import java.security.InvalidParameterException;

public interface Parameterized {
    public void setParameter(String var1, Object var2) throws NoSuchParameterException, InvalidParameterException, InvalidParameterTypeException;

    public Object getParameter(String var1) throws NoSuchParameterException, InvalidParameterException;
}

