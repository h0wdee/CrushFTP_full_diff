/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.dsn;

public abstract class Report {
    protected String type;

    protected Report(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}

