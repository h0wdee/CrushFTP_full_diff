/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.compress.archivers.zip.ZipArchiveEntry
 *  org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
 */
package com.crushftp.tunnel;

import com.crushftp.tunnel.FileArchiveEntry;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

public class FileArchiveInputStream
extends ZipArchiveInputStream {
    BufferedInputStream in = null;
    byte[] b1 = new byte[1];
    long chunkSize = 0L;
    boolean zip = false;
    boolean first_read = true;

    public FileArchiveInputStream(BufferedInputStream in) throws IOException {
        super((InputStream)in);
        this.in = in;
    }

    public int read() throws IOException {
        int bytesRead = this.read(this.b1);
        if (bytesRead >= 0) {
            return this.b1[0];
        }
        return -1;
    }

    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int bytesRead;
        if (this.first_read) {
            this.check_header();
        }
        if (this.zip) {
            return super.read(b, off, len);
        }
        if (this.chunkSize == 0L) {
            String size = "";
            while (!size.endsWith(":")) {
                int bytesRead2 = this.in.read(this.b1);
                if (bytesRead2 == 1) {
                    size = String.valueOf(size) + new String(this.b1);
                    continue;
                }
                return -1;
            }
            this.chunkSize = Long.parseLong(size.substring(0, size.getBytes("UTF8").length - 1));
        }
        if (this.chunkSize <= 0L) {
            return -1;
        }
        if ((long)len > this.chunkSize) {
            len = (int)this.chunkSize;
        }
        if ((bytesRead = this.in.read(b, off, len)) >= 0) {
            this.chunkSize -= (long)bytesRead;
        }
        return bytesRead;
    }

    public ZipArchiveEntry getNextZipEntry() throws IOException {
        if (this.first_read) {
            this.check_header();
        }
        if (this.zip) {
            return super.getNextZipEntry();
        }
        this.chunkSize = 0L;
        return FileArchiveEntry.parseObj(this.in);
    }

    private void check_header() throws IOException {
        this.in.mark(3);
        byte[] header = new byte[2];
        this.in.read(header, 0, 1);
        this.in.read(header, 1, 1);
        if (new String(header).startsWith("PK")) {
            this.zip = true;
        }
        this.in.reset();
        this.first_read = false;
    }

    public void close() throws IOException {
        if (this.zip) {
            super.close();
        } else {
            this.in.close();
        }
    }
}

