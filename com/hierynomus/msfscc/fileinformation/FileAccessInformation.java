/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msfscc.fileinformation;

import com.hierynomus.msfscc.fileinformation.FileQueryableInformation;

public class FileAccessInformation
implements FileQueryableInformation {
    private int accessFlags;

    FileAccessInformation(int accessFlags) {
        this.accessFlags = accessFlags;
    }

    public int getAccessFlags() {
        return this.accessFlags;
    }
}

