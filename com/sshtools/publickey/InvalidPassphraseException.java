/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

public class InvalidPassphraseException
extends Exception {
    private static final long serialVersionUID = -1458660635959624570L;

    public InvalidPassphraseException() {
        super("The passphrase supplied was invalid!");
    }

    public InvalidPassphraseException(Exception ex) {
        super(ex.getMessage());
    }
}

