/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msfscc.fileinformation;

import com.hierynomus.msfscc.fileinformation.FileAccessInformation;
import com.hierynomus.msfscc.fileinformation.FileAlignmentInformation;
import com.hierynomus.msfscc.fileinformation.FileBasicInformation;
import com.hierynomus.msfscc.fileinformation.FileEaInformation;
import com.hierynomus.msfscc.fileinformation.FileInternalInformation;
import com.hierynomus.msfscc.fileinformation.FileModeInformation;
import com.hierynomus.msfscc.fileinformation.FilePositionInformation;
import com.hierynomus.msfscc.fileinformation.FileQueryableInformation;
import com.hierynomus.msfscc.fileinformation.FileStandardInformation;

public class FileAllInformation
implements FileQueryableInformation {
    private FileBasicInformation basicInformation;
    private FileStandardInformation standardInformation;
    private FileInternalInformation internalInformation;
    private FileEaInformation eaInformation;
    private FileAccessInformation accessInformation;
    private FilePositionInformation positionInformation;
    private FileModeInformation modeInformation;
    private FileAlignmentInformation alignmentInformation;
    private String nameInformation;

    FileAllInformation(FileBasicInformation basicInformation, FileStandardInformation standardInformation, FileInternalInformation internalInformation, FileEaInformation eaInformation, FileAccessInformation accessInformation, FilePositionInformation positionInformation, FileModeInformation modeInformation, FileAlignmentInformation alignmentInformation, String nameInformation) {
        this.basicInformation = basicInformation;
        this.standardInformation = standardInformation;
        this.internalInformation = internalInformation;
        this.eaInformation = eaInformation;
        this.accessInformation = accessInformation;
        this.positionInformation = positionInformation;
        this.modeInformation = modeInformation;
        this.alignmentInformation = alignmentInformation;
        this.nameInformation = nameInformation;
    }

    public FileBasicInformation getBasicInformation() {
        return this.basicInformation;
    }

    public FileStandardInformation getStandardInformation() {
        return this.standardInformation;
    }

    public FileInternalInformation getInternalInformation() {
        return this.internalInformation;
    }

    public FileEaInformation getEaInformation() {
        return this.eaInformation;
    }

    public FileAccessInformation getAccessInformation() {
        return this.accessInformation;
    }

    public FilePositionInformation getPositionInformation() {
        return this.positionInformation;
    }

    public FileModeInformation getModeInformation() {
        return this.modeInformation;
    }

    public FileAlignmentInformation getAlignmentInformation() {
        return this.alignmentInformation;
    }

    public String getNameInformation() {
        return this.nameInformation;
    }
}

