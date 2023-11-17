/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jcifs.smb.SmbFile
 *  jcifs.smb.SmbRandomAccessFile
 */
package com.crushftp.client;

import java.io.IOException;
import java.io.OutputStream;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbRandomAccessFile;

public class SmbRandomOutputStream
extends OutputStream {
    SmbRandomAccessFile raf = null;
    Object rafLock = new Object();
    long pos = 0L;
    boolean shared = false;

    public SmbRandomOutputStream(SmbFile f, boolean shared) throws IOException {
        this.shared = shared;
        this.raf = new SmbRandomAccessFile(f, "rw");
        this.pos = this.raf.getFilePointer();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void append() throws IOException {
        if (this.raf == null) {
            return;
        }
        Object object = this.rafLock;
        synchronized (object) {
            this.seek(this.length());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setLength(long l) throws IOException {
        if (this.raf == null) {
            return;
        }
        Object object = this.rafLock;
        synchronized (object) {
            this.raf.setLength(l);
            this.pos = this.raf.getFilePointer();
        }
    }

    @Override
    public void flush() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void seek(long l) throws IOException {
        if (this.raf == null) {
            return;
        }
        Object object = this.rafLock;
        synchronized (object) {
            this.raf.seek(l);
            this.pos = this.raf.getFilePointer();
        }
    }

    public long length() throws IOException {
        return this.raf.length();
    }

    @Override
    public void write(int i) throws IOException {
        this.write(new byte[]{(byte)i}, 0, 1);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(byte[] b, int start, int len) throws IOException {
        if (this.raf == null) {
            return;
        }
        Object object = this.rafLock;
        synchronized (object) {
            this.raf.seek(this.pos);
            this.raf.write(b, start, len);
            this.pos = this.raf.getFilePointer();
        }
    }

    @Override
    public void close() throws IOException {
        if (this.raf == null) {
            return;
        }
        this.raf.close();
        this.raf = null;
    }
}

