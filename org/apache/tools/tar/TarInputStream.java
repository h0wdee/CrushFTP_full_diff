/*
 * Decompiled with CFR 0.152.
 */
package org.apache.tools.tar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import org.apache.tools.tar.TarBuffer;
import org.apache.tools.tar.TarEntry;

public class TarInputStream
extends FilterInputStream {
    private static final int SMALL_BUFFER_SIZE = 256;
    private static final int BUFFER_SIZE = 8192;
    private static final int LARGE_BUFFER_SIZE = 32768;
    private static final int BYTE_MASK = 255;
    protected boolean debug;
    protected boolean hasHitEOF;
    protected long entrySize;
    protected long entryOffset;
    protected byte[] readBuf;
    protected TarBuffer buffer;
    protected TarEntry currEntry;
    protected byte[] oneBuf;

    public TarInputStream(InputStream inputStream) {
        this(inputStream, 10240, 512);
    }

    public TarInputStream(InputStream inputStream, int n) {
        this(inputStream, n, 512);
    }

    public TarInputStream(InputStream inputStream, int n, int n2) {
        super(inputStream);
        this.buffer = new TarBuffer(inputStream, n, n2);
        this.readBuf = null;
        this.oneBuf = new byte[1];
        this.debug = false;
        this.hasHitEOF = false;
    }

    public void setDebug(boolean bl) {
        this.debug = bl;
        this.buffer.setDebug(bl);
    }

    public void close() throws IOException {
        this.buffer.close();
    }

    public int getRecordSize() {
        return this.buffer.getRecordSize();
    }

    public int available() throws IOException {
        if (this.entrySize - this.entryOffset > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int)(this.entrySize - this.entryOffset);
    }

    public long skip(long l) throws IOException {
        int n;
        long l2;
        int n2;
        byte[] byArray = new byte[8192];
        for (l2 = l; l2 > 0L && (n2 = this.read(byArray, 0, n = (int)(l2 > (long)byArray.length ? (long)byArray.length : l2))) != -1; l2 -= (long)n2) {
        }
        return l - l2;
    }

    public boolean markSupported() {
        return false;
    }

    public void mark(int n) {
    }

    public void reset() {
    }

    public TarEntry getNextEntry() throws IOException {
        byte[] byArray;
        if (this.hasHitEOF) {
            return null;
        }
        if (this.currEntry != null) {
            long l = this.entrySize - this.entryOffset;
            if (this.debug) {
                System.err.println("TarInputStream: SKIP currENTRY '" + this.currEntry.getName() + "' SZ " + this.entrySize + " OFF " + this.entryOffset + "  skipping " + l + " bytes");
            }
            while (l > 0L) {
                long l2 = this.skip(l);
                if (l2 <= 0L) {
                    throw new RuntimeException("failed to skip current tar entry");
                }
                l -= l2;
            }
            this.readBuf = null;
        }
        if ((byArray = this.buffer.readRecord()) == null) {
            if (this.debug) {
                System.err.println("READ NULL RECORD");
            }
            this.hasHitEOF = true;
        } else if (this.buffer.isEOFRecord(byArray)) {
            if (this.debug) {
                System.err.println("READ EOF RECORD");
            }
            this.hasHitEOF = true;
        }
        if (this.hasHitEOF) {
            this.currEntry = null;
        } else {
            this.currEntry = new TarEntry(byArray);
            if (this.debug) {
                System.err.println("TarInputStream: SET CURRENTRY '" + this.currEntry.getName() + "' size = " + this.currEntry.getSize());
            }
            this.entryOffset = 0L;
            this.entrySize = this.currEntry.getSize();
        }
        if (this.currEntry != null && this.currEntry.isGNULongNameEntry()) {
            StringBuffer stringBuffer = new StringBuffer();
            byte[] byArray2 = new byte[256];
            int n = 0;
            while ((n = this.read(byArray2)) >= 0) {
                stringBuffer.append(new String(byArray2, 0, n));
            }
            this.getNextEntry();
            if (this.currEntry == null) {
                return null;
            }
            if (stringBuffer.length() > 0 && stringBuffer.charAt(stringBuffer.length() - 1) == '\u0000') {
                stringBuffer.deleteCharAt(stringBuffer.length() - 1);
            }
            this.currEntry.setName(stringBuffer.toString());
        }
        return this.currEntry;
    }

    public int read() throws IOException {
        int n = this.read(this.oneBuf, 0, 1);
        return n == -1 ? -1 : this.oneBuf[0] & 0xFF;
    }

    public int read(byte[] byArray, int n, int n2) throws IOException {
        int n3;
        int n4 = 0;
        if (this.entryOffset >= this.entrySize) {
            return -1;
        }
        if ((long)n2 + this.entryOffset > this.entrySize) {
            n2 = (int)(this.entrySize - this.entryOffset);
        }
        if (this.readBuf != null) {
            int n5 = n2 > this.readBuf.length ? this.readBuf.length : n2;
            System.arraycopy(this.readBuf, 0, byArray, n, n5);
            if (n5 >= this.readBuf.length) {
                this.readBuf = null;
            } else {
                n3 = this.readBuf.length - n5;
                byte[] byArray2 = new byte[n3];
                System.arraycopy(this.readBuf, n5, byArray2, 0, n3);
                this.readBuf = byArray2;
            }
            n4 += n5;
            n2 -= n5;
            n += n5;
        }
        while (n2 > 0) {
            byte[] byArray3 = this.buffer.readRecord();
            if (byArray3 == null) {
                throw new IOException("unexpected EOF with " + n2 + " bytes unread");
            }
            int n6 = byArray3.length;
            n3 = n2;
            if (n6 > n3) {
                System.arraycopy(byArray3, 0, byArray, n, n3);
                this.readBuf = new byte[n6 - n3];
                System.arraycopy(byArray3, n3, this.readBuf, 0, n6 - n3);
            } else {
                n3 = n6;
                System.arraycopy(byArray3, 0, byArray, n, n6);
            }
            n4 += n3;
            n2 -= n3;
            n += n3;
        }
        this.entryOffset += (long)n4;
        return n4;
    }

    public void copyEntryContents(OutputStream outputStream) throws IOException {
        int n;
        byte[] byArray = new byte[32768];
        while ((n = this.read(byArray, 0, byArray.length)) != -1) {
            outputStream.write(byArray, 0, n);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String[] extractAll(String string) throws IOException {
        ArrayList<String> arrayList = new ArrayList<String>();
        TarEntry tarEntry = this.getNextEntry();
        while (tarEntry != null) {
            File file;
            String string2 = tarEntry.getName();
            String string3 = string + File.separator + string2;
            if (tarEntry.isDirectory()) {
                file = new File(string3.substring(0, string3.length() - 1));
                if (!file.exists()) {
                    file.mkdir();
                }
                arrayList.add(file.getAbsolutePath());
            } else {
                file = new File(string3.substring(0, string3.lastIndexOf(File.separatorChar)));
                if (!file.exists()) {
                    file.mkdir();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(string3);
                try {
                    this.copyEntryContents(fileOutputStream);
                }
                finally {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                }
                arrayList.add(string3);
            }
            tarEntry = this.getNextEntry();
        }
        return arrayList.toArray(new String[arrayList.size()]);
    }
}

