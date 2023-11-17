/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.inspect;

import java.io.Serializable;
import java.util.Date;

public class ContentItem
implements Serializable {
    private static final long serialVersionUID = 590263891205390633L;
    private String fileName;
    private Date modificationDate;
    private boolean directory;

    ContentItem(String string, Date date) {
        this(string, date, false);
    }

    ContentItem(String string, Date date, boolean bl) {
        this.fileName = string;
        this.modificationDate = date;
        this.directory = bl;
    }

    void setFileName(String string) {
        this.fileName = string;
    }

    public String getFileName() {
        return this.fileName;
    }

    void setModificationDate(Date date) {
        this.modificationDate = date;
    }

    public Date getModificationDate() {
        return this.modificationDate;
    }

    void setDirectory(boolean bl) {
        this.directory = bl;
    }

    public boolean isDirectory() {
        return this.directory;
    }
}

