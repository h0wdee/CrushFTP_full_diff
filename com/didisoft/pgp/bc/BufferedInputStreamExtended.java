/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BufferedInputStreamExtended
extends BufferedInputStream {
    BufferedInputStreamExtended(InputStream inputStream) {
        super(inputStream);
    }

    public synchronized int available() throws IOException {
        int n = super.available();
        if (n < 0) {
            n = Integer.MAX_VALUE;
        }
        return n;
    }
}

