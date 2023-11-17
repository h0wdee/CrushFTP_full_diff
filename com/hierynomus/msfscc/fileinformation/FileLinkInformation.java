/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msfscc.fileinformation;

import com.hierynomus.msfscc.fileinformation.FileRenameInformation;

public class FileLinkInformation
extends FileRenameInformation {
    public FileLinkInformation(boolean replaceIfExists, String fileName) {
        super(replaceIfExists, 0L, fileName);
    }
}

