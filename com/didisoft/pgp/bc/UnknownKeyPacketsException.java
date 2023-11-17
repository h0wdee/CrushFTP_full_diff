/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc;

import java.io.IOException;

public class UnknownKeyPacketsException
extends IOException {
    private static final long serialVersionUID = -277403260230069362L;

    public UnknownKeyPacketsException(String string) {
        super(string);
    }

    public UnknownKeyPacketsException(String string, Exception exception) {
        super(string);
    }
}

