/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import java.io.File;
import java.net.URI;
import java.util.Properties;

public class SnapshotFile
extends File {
    private static final long serialVersionUID = 1L;
    public long snapshotModified = 0L;
    public long snapshotSize = 0L;
    boolean isDir = false;
    public Properties info = null;

    public SnapshotFile(File f) {
        super(f.getPath());
        this.snapshot();
    }

    public SnapshotFile(String s) {
        super(s);
        this.snapshot();
    }

    public SnapshotFile(URI u) {
        super(u);
        this.snapshot();
    }

    public void snapshot() {
        this.snapshotModified = super.lastModified() / 1000L * 1000L;
        this.snapshotSize = super.length();
        this.isDir = super.isDirectory();
    }

    public void put(String key, String val) {
        if (this.info == null) {
            this.info = new Properties();
        }
        this.info.put(key, val);
    }

    public String get(String key) {
        if (this.info == null) {
            this.info = new Properties();
        }
        return this.info.getProperty(key);
    }

    public String get(String key, String defaultVal) {
        if (this.info == null) {
            this.info = new Properties();
        }
        return this.info.getProperty(key, defaultVal);
    }

    @Override
    public boolean isDirectory() {
        return this.isDir;
    }

    @Override
    public boolean isFile() {
        return !this.isDir;
    }

    @Override
    public long length() {
        return this.snapshotSize;
    }

    @Override
    public long lastModified() {
        return this.snapshotModified / 1000L * 1000L;
    }

    @Override
    public File[] listFiles() {
        File[] files = super.listFiles();
        File[] files2 = new SnapshotFile[files.length];
        int x = 0;
        while (x < files.length) {
            files2[x] = new SnapshotFile(files[x]);
            ++x;
        }
        return files2;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}

