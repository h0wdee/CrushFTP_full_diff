/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.pop3;

import com.sun.mail.pop3.AppendStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.mail.util.SharedFileInputStream;

class WritableSharedFile
extends SharedFileInputStream {
    private RandomAccessFile raf;
    private AppendStream af;

    public WritableSharedFile(File file) throws IOException {
        super(file);
        try {
            this.raf = new RandomAccessFile(file, "rw");
        }
        catch (IOException ex) {
            super.close();
        }
    }

    public RandomAccessFile getWritableFile() {
        return this.raf;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void close() throws IOException {
        try {
            super.close();
            Object var2_1 = null;
        }
        catch (Throwable throwable) {
            Object var2_2 = null;
            this.raf.close();
            throw throwable;
        }
        this.raf.close();
    }

    synchronized long updateLength() throws IOException {
        this.datalen = this.in.length();
        this.af = null;
        return this.datalen;
    }

    public synchronized AppendStream getAppendStream() throws IOException {
        if (this.af != null) {
            throw new IOException("POP3 file cache only supports single threaded access");
        }
        this.af = new AppendStream(this);
        return this.af;
    }
}

