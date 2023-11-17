/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.openpgp.PGPException
 */
package com.didisoft.pgp;

public class PGPException
extends lw.bouncycastle.openpgp.PGPException {
    private static final long serialVersionUID = -7698669548427089832L;

    public PGPException(String string) {
        super(string);
    }

    public PGPException(String string, Exception exception) {
        super(string + (exception != null ? " : " + exception.getMessage() : ""), exception);
    }
}

