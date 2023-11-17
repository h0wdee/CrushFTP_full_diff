/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msfscc.fileinformation;

import com.hierynomus.msfscc.fileinformation.FileQueryableInformation;

public class FilePositionInformation
implements FileQueryableInformation {
    private long currentByteOffset;

    FilePositionInformation(long currentByteOffset) {
        this.currentByteOffset = currentByteOffset;
    }

    public long getCurrentByteOffset() {
        return this.currentByteOffset;
    }
}

