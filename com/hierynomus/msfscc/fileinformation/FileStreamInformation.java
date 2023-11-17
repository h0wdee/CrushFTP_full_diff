/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msfscc.fileinformation;

import com.hierynomus.msfscc.fileinformation.FileQueryableInformation;
import com.hierynomus.msfscc.fileinformation.FileStreamInformationItem;
import java.util.ArrayList;
import java.util.List;

public class FileStreamInformation
implements FileQueryableInformation {
    private List<FileStreamInformationItem> streamList;

    FileStreamInformation(List<FileStreamInformationItem> streamList) {
        this.streamList = streamList;
    }

    public List<FileStreamInformationItem> getStreamList() {
        return this.streamList;
    }

    public List<String> getStreamNames() {
        ArrayList<String> nameList = new ArrayList<String>();
        for (FileStreamInformationItem s : this.streamList) {
            nameList.add(s.getName());
        }
        return nameList;
    }
}

