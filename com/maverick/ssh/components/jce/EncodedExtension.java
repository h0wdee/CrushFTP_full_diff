/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

public class EncodedExtension {
    String name;
    byte[] value;
    boolean known;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected void setKnown(boolean known) {
        this.known = known;
    }

    protected byte[] getStoredValue() {
        if (this.value != null) {
            return this.value;
        }
        return new byte[0];
    }

    public boolean isKnown() {
        return this.known;
    }

    protected void setStoredValue(byte[] value) {
        this.value = value;
    }

    public String getValue() {
        return null;
    }
}

