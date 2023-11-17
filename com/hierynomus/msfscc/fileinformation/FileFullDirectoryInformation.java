/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msfscc.fileinformation;

import com.hierynomus.msdtyp.FileTime;
import com.hierynomus.msfscc.fileinformation.FileDirectoryQueryableInformation;

public class FileFullDirectoryInformation
extends FileDirectoryQueryableInformation {
    private final FileTime creationTime;
    private final FileTime lastAccessTime;
    private final FileTime lastWriteTime;
    private final FileTime changeTime;
    private final long endOfFile;
    private final long allocationSize;
    private final long fileAttributes;
    private final long eaSize;

    FileFullDirectoryInformation(long nextOffset, long fileIndex, String fileName, FileTime creationTime, FileTime lastAccessTime, FileTime lastWriteTime, FileTime changeTime, long endOfFile, long allocationSize, long fileAttributes, long eaSize) {
        super(nextOffset, fileIndex, fileName);
        this.creationTime = creationTime;
        this.lastAccessTime = lastAccessTime;
        this.lastWriteTime = lastWriteTime;
        this.changeTime = changeTime;
        this.endOfFile = endOfFile;
        this.allocationSize = allocationSize;
        this.fileAttributes = fileAttributes;
        this.eaSize = eaSize;
    }

    public FileTime getCreationTime() {
        return this.creationTime;
    }

    public FileTime getLastAccessTime() {
        return this.lastAccessTime;
    }

    public FileTime getLastWriteTime() {
        return this.lastWriteTime;
    }

    public FileTime getChangeTime() {
        return this.changeTime;
    }

    public long getEndOfFile() {
        return this.endOfFile;
    }

    public long getAllocationSize() {
        return this.allocationSize;
    }

    public long getFileAttributes() {
        return this.fileAttributes;
    }

    public long getEaSize() {
        return this.eaSize;
    }
}

