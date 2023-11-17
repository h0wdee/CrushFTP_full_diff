/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msfscc.fileinformation;

import com.hierynomus.msfscc.fileinformation.FileQueryableInformation;

public class FileEaInformation
implements FileQueryableInformation {
    private long eaSize;

    FileEaInformation(long eaSize) {
        this.eaSize = eaSize;
    }

    public long getEaSize() {
        return this.eaSize;
    }
}

