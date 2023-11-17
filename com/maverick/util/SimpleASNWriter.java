/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import java.io.ByteArrayOutputStream;

public class SimpleASNWriter {
    private ByteArrayOutputStream data = new ByteArrayOutputStream();

    public void writeByte(int b) {
        this.data.write(b);
    }

    public void write(byte[] b) {
        this.data.write(b, 0, b.length);
    }

    public void writeData(byte[] b) {
        this.writeLength(b.length);
        this.data.write(b, 0, b.length);
    }

    public void writeLength(int length) {
        if (length < 128) {
            this.data.write(length);
        } else if (length < 256) {
            this.data.write(129);
            this.data.write(length);
        } else if (length < 65536) {
            this.data.write(130);
            this.data.write(length >>> 8);
            this.data.write(length);
        } else if (length < 0x1000000) {
            this.data.write(131);
            this.data.write(length >>> 16);
            this.data.write(length >>> 8);
            this.data.write(length);
        } else {
            this.data.write(132);
            this.data.write(length >>> 24);
            this.data.write(length >>> 16);
            this.data.write(length >>> 8);
            this.data.write(length);
        }
    }

    public byte[] toByteArray() {
        return this.data.toByteArray();
    }
}

