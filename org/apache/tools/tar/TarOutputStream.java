/*
 * Decompiled with CFR 0.152.
 */
package org.apache.tools.tar;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.tools.tar.TarBuffer;
import org.apache.tools.tar.TarEntry;

public class TarOutputStream
extends FilterOutputStream {
    private static final int DEFAULT_BUFFER_SIZE = 102400;
    public static final int LONGFILE_ERROR = 0;
    public static final int LONGFILE_TRUNCATE = 1;
    public static final int LONGFILE_GNU = 2;
    protected boolean debug;
    protected long currSize;
    protected String currName;
    protected long currBytes;
    protected byte[] oneBuf;
    protected byte[] recordBuf;
    protected int assemLen;
    protected byte[] assemBuf;
    protected TarBuffer buffer;
    protected int longFileMode = 2;
    private boolean closed = false;

    public TarOutputStream(OutputStream outputStream) {
        this(outputStream, 10240, 512);
    }

    public TarOutputStream(OutputStream outputStream, int n) {
        this(outputStream, n, 512);
    }

    public TarOutputStream(OutputStream outputStream, int n, int n2) {
        super(outputStream);
        this.buffer = new TarBuffer(outputStream, n, n2);
        this.debug = false;
        this.assemLen = 0;
        this.assemBuf = new byte[n2];
        this.recordBuf = new byte[n2];
        this.oneBuf = new byte[1];
    }

    public void setLongFileMode(int n) {
        this.longFileMode = n;
    }

    public void setDebug(boolean bl) {
        this.debug = bl;
    }

    public void setBufferDebug(boolean bl) {
        this.buffer.setDebug(bl);
    }

    public void finish() throws IOException {
        this.writeEOFRecord();
    }

    public void close() throws IOException {
        if (!this.closed) {
            this.finish();
            this.buffer.close();
            this.out.close();
            this.closed = true;
        }
    }

    public int getRecordSize() {
        return this.buffer.getRecordSize();
    }

    public void putNextEntry(TarEntry tarEntry) throws IOException {
        if (tarEntry.getName().length() >= 100) {
            if (this.longFileMode == 2) {
                TarEntry tarEntry2 = new TarEntry("././@LongLink", 76);
                tarEntry2.setSize(tarEntry.getName().length() + 1);
                this.putNextEntry(tarEntry2);
                this.write(tarEntry.getName().getBytes());
                this.write(0);
                this.closeEntry();
            } else if (this.longFileMode != 1) {
                throw new RuntimeException("file name '" + tarEntry.getName() + "' is too long ( > " + 100 + " bytes)");
            }
        }
        tarEntry.writeEntryHeader(this.recordBuf);
        this.buffer.writeRecord(this.recordBuf);
        this.currBytes = 0L;
        this.currSize = tarEntry.isDirectory() ? 0L : tarEntry.getSize();
        this.currName = tarEntry.getName();
    }

    public void closeEntry() throws IOException {
        if (this.assemLen > 0) {
            for (int i = this.assemLen; i < this.assemBuf.length; ++i) {
                this.assemBuf[i] = 0;
            }
            this.buffer.writeRecord(this.assemBuf);
            this.currBytes += (long)this.assemLen;
            this.assemLen = 0;
        }
        if (this.currBytes < this.currSize) {
            throw new IOException("entry '" + this.currName + "' closed at '" + this.currBytes + "' before the '" + this.currSize + "' bytes specified in the header were written");
        }
    }

    public void write(int n) throws IOException {
        this.oneBuf[0] = (byte)n;
        this.write(this.oneBuf, 0, 1);
    }

    public void write(byte[] byArray) throws IOException {
        this.write(byArray, 0, byArray.length);
    }

    public void write(byte[] byArray, int n, int n2) throws IOException {
        int n3;
        if (this.currBytes + (long)n2 > this.currSize) {
            throw new IOException("request to write '" + n2 + "' bytes exceeds size in header of '" + this.currSize + "' bytes for entry '" + this.currName + "'");
        }
        if (this.assemLen > 0) {
            if (this.assemLen + n2 >= this.recordBuf.length) {
                n3 = this.recordBuf.length - this.assemLen;
                System.arraycopy(this.assemBuf, 0, this.recordBuf, 0, this.assemLen);
                System.arraycopy(byArray, n, this.recordBuf, this.assemLen, n3);
                this.buffer.writeRecord(this.recordBuf);
                this.currBytes += (long)this.recordBuf.length;
                n += n3;
                n2 -= n3;
                this.assemLen = 0;
            } else {
                System.arraycopy(byArray, n, this.assemBuf, this.assemLen, n2);
                n += n2;
                this.assemLen += n2;
                n2 = 0;
            }
        }
        while (n2 > 0) {
            if (n2 < this.recordBuf.length) {
                System.arraycopy(byArray, n, this.assemBuf, this.assemLen, n2);
                this.assemLen += n2;
                break;
            }
            this.buffer.writeRecord(byArray, n);
            n3 = this.recordBuf.length;
            this.currBytes += (long)n3;
            n2 -= n3;
            n += n3;
        }
    }

    private void writeEOFRecord() throws IOException {
        for (int i = 0; i < this.recordBuf.length; ++i) {
            this.recordBuf[i] = 0;
        }
        this.buffer.writeRecord(this.recordBuf);
    }

    public void writeFileEntry(TarEntry tarEntry) throws IOException {
        this.writeFileEntry(tarEntry, "");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void writeFileEntry(TarEntry tarEntry, String string) throws IOException {
        if (tarEntry.isDirectory()) {
            this.putNextEntry(tarEntry);
            File[] fileArray = tarEntry.getFile().listFiles(new FileFilter(){

                public boolean accept(File file) {
                    return file.isDirectory();
                }
            });
            String string2 = string + File.separatorChar + tarEntry.getFile().getName();
            for (int i = 0; i < fileArray.length; ++i) {
                File file = fileArray[i];
                if (file.getName().equals("..")) continue;
                TarEntry tarEntry2 = new TarEntry(file.getAbsolutePath(), string2);
                this.writeFileEntry(tarEntry2, string2);
            }
            File[] fileArray2 = tarEntry.getFile().listFiles(new FileFilter(){

                public boolean accept(File file) {
                    return !file.isDirectory();
                }
            });
            for (int i = 0; i < fileArray2.length; ++i) {
                this.writeFileEntry(new TarEntry(fileArray2[i].getAbsolutePath(), string2), string2);
            }
            this.closeEntry();
        } else {
            this.putNextEntry(tarEntry);
            FileInputStream fileInputStream = null;
            try {
                int n;
                fileInputStream = new FileInputStream(tarEntry.getFile());
                byte[] byArray = new byte[102400];
                while ((n = ((InputStream)fileInputStream).read(byArray, 0, byArray.length)) > 0) {
                    this.write(byArray, 0, n);
                }
            }
            finally {
                if (fileInputStream != null) {
                    try {
                        ((InputStream)fileInputStream).close();
                    }
                    catch (IOException iOException) {}
                }
            }
            this.closeEntry();
        }
    }
}

