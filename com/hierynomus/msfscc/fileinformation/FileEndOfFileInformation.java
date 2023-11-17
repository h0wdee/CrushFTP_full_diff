/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msfscc.fileinformation;

import com.hierynomus.msfscc.fileinformation.FileSettableInformation;

public class FileEndOfFileInformation
implements FileSettableInformation {
    private long endOfFile;

    public FileEndOfFileInformation(long endOfFile) throws IllegalArgumentException {
        if (endOfFile < 0L) {
            throw new IllegalArgumentException("endOfFile MUST be greater than or equal to 0");
        }
        this.endOfFile = endOfFile;
    }

    public long getEndOfFile() {
        return this.endOfFile;
    }
}

