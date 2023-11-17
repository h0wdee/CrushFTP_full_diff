/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.HexBuilder;

public class Blob {
    public byte[] data;
    public int len;

    public Blob() {
        this.data = null;
        this.len = 0;
    }

    public Blob(int size) {
        this.data = new byte[size];
        this.len = this.data.length;
    }

    public Blob(byte[] data) {
        this.data = data;
        this.len = data.length;
    }

    public Blob(Blob blob) {
        this(blob.len);
        System.arraycopy(blob.data, 0, this.data, 0, blob.len);
    }

    public String toString() {
        return "Blob [len=" + this.len + ", data=" + HexBuilder.toHex(this.data, this.len) + "]";
    }
}

