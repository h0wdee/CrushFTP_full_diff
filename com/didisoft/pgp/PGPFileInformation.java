/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp;

import java.util.List;

public class PGPFileInformation {
    public static final int SIGNED = 1;
    public static final int ENCRYPTED = 2;
    public static final int SIGNED_AND_ENCRYPTED = 3;
    private int action;
    private List files;

    public int getAction() {
        return this.action;
    }

    public void setAction(int n) {
        this.action = n;
    }

    public List getFiles() {
        return this.files;
    }

    public void setFiles(List list) {
        this.files = list;
    }
}

