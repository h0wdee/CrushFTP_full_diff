/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.NqException;
import java.util.ArrayList;

public class FileNotifyInformation {
    public static final int FILE_ACTION_ADDED = 1;
    public static final int FILE_ACTION_REMOVED = 2;
    public static final int FILE_ACTION_MODIFIED = 3;
    public static final int FILE_ACTION_RENAMED_OLD_NAME = 4;
    public static final int FILE_ACTION_RENAMED_NEW_NAME = 5;
    public static final int FILE_ACTION_ADDED_STREAM = 6;
    public static final int FILE_ACTION_REMOVED_STREAM = 7;
    public static final int FILE_ACTION_MODIFIED_STREAM = 8;
    public static final int FILE_ACTION_REMOVED_BY_DELETE = 9;
    public static final int FILE_ACTION_ID_NOT_TUNNELLED = 10;
    public static final int FILE_ACTION_TUNNELLED_ID_COLLISION = 11;
    int action;
    String fileName;

    public FileNotifyInformation(int action, String fileName) {
        this.action = action;
        this.fileName = fileName;
    }

    public FileNotifyInformation() {
    }

    public int getAction() {
        return this.action;
    }

    public String getFileName() {
        return this.fileName;
    }

    static FileNotifyInformation[] extractFileNotifyInformation(byte[] mydata) throws NqException {
        int nextOffset;
        ArrayList<FileNotifyInformation> fileNotifyInfo = new ArrayList<FileNotifyInformation>();
        BufferReader bufReader = new BufferReader(mydata, 0, false);
        while (0 <= (nextOffset = bufReader.readInt4())) {
            int action = bufReader.readInt4();
            int fnameLength = bufReader.readInt4();
            String fname = bufReader.readString(fnameLength);
            bufReader.align(0, 4);
            FileNotifyInformation fni = new FileNotifyInformation(action, fname);
            fileNotifyInfo.add(fni);
            if (0 != nextOffset) continue;
            break;
        }
        FileNotifyInformation[] results = new FileNotifyInformation[fileNotifyInfo.size()];
        int i = 0;
        for (FileNotifyInformation fni : fileNotifyInfo) {
            results[i++] = fni;
        }
        return results;
    }

    public String toString() {
        return "FileNotifyInformation [action=" + this.action + ", fileName=" + this.fileName + "]";
    }
}

