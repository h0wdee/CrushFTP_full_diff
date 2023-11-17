/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc;

import java.io.ByteArrayOutputStream;

public class DirectByteArrayOutputStream
extends ByteArrayOutputStream {
    public DirectByteArrayOutputStream(int n) {
        super(n);
    }

    public byte[] getArray() {
        return this.buf;
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }
}

