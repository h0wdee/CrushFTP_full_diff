/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal.security;

public class IllegalBlockSizeException
extends RuntimeException {
    public int blockSize;
    public int dataSize;

    public int getBlockSize() {
        return this.blockSize;
    }

    public int getDataSize() {
        return this.dataSize;
    }

    public IllegalBlockSizeException(String string) {
        super(string);
    }

    public IllegalBlockSizeException(int n, int n2) {
        super("blockSize = " + n + ", dataSize = " + n2);
        this.blockSize = n;
        this.dataSize = n2;
    }

    public IllegalBlockSizeException(int n, int n2, String string) {
        super(string);
        this.blockSize = n;
        this.dataSize = n2;
    }
}

