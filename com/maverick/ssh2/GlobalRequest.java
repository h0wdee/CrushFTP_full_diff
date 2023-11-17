/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh2;

public class GlobalRequest {
    String name;
    byte[] requestdata;

    public GlobalRequest(String name, byte[] requestdata) {
        this.name = name;
        this.requestdata = requestdata;
    }

    public String getName() {
        return this.name;
    }

    public byte[] getData() {
        return this.requestdata;
    }

    public void setData(byte[] requestdata) {
        this.requestdata = requestdata;
    }
}

