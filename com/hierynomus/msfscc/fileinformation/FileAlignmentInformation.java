/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msfscc.fileinformation;

import com.hierynomus.msfscc.fileinformation.FileQueryableInformation;

public class FileAlignmentInformation
implements FileQueryableInformation {
    private long alignmentRequirement;

    FileAlignmentInformation(long alignmentRequirement) {
        this.alignmentRequirement = alignmentRequirement;
    }

    public long getAlignmentRequirement() {
        return this.alignmentRequirement;
    }
}

