/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msfscc.fileinformation;

import com.hierynomus.msfscc.fileinformation.FileSettableInformation;

public class FileAllocationInformation
implements FileSettableInformation {
    private long allocationSize;

    public FileAllocationInformation(long allocationSize) {
        this.allocationSize = allocationSize;
    }

    public long getAllocationSize() {
        return this.allocationSize;
    }
}

