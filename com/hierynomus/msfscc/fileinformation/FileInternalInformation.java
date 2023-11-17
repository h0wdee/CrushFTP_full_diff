/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msfscc.fileinformation;

import com.hierynomus.msfscc.fileinformation.FileQueryableInformation;

public class FileInternalInformation
implements FileQueryableInformation {
    private long indexNumber;

    FileInternalInformation(long eaSize) {
        this.indexNumber = eaSize;
    }

    public long getIndexNumber() {
        return this.indexNumber;
    }
}

