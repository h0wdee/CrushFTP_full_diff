/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msfscc.fileinformation;

import com.hierynomus.msfscc.fileinformation.FileQueryableInformation;
import com.hierynomus.msfscc.fileinformation.FileSettableInformation;

public class FileModeInformation
implements FileQueryableInformation,
FileSettableInformation {
    private int mode;

    public FileModeInformation(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return this.mode;
    }
}

