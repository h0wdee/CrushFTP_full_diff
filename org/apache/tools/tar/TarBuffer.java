/*
 * Decompiled with CFR 0.152.
 */
package org.apache.tools.tar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class TarBuffer {
    public static final int DEFAULT_RCDSIZE = 512;
    public static final int DEFAULT_BLKSIZE = 10240;
    private InputStream inStream;
    private OutputStream outStream;
    private byte[] blockBuffer;
    private int currBlkIdx;
    private int currRecIdx;
    private int blockSize;
    private int recordSize;
    private int recsPerBlock;
    private boolean debug;

    public TarBuffer(InputStream inputStream) {
        this(inputStream, 10240);
    }

    public TarBuffer(InputStream inputStream, int n) {
        this(inputStream, n, 512);
    }

    public TarBuffer(InputStream inputStream, int n, int n2) {
        this.inStream = inputStream;
        this.outStream = null;
        this.initialize(n, n2);
    }

    public TarBuffer(OutputStream outputStream) {
        this(outputStream, 10240);
    }

    public TarBuffer(OutputStream outputStream, int n) {
        this(outputStream, n, 512);
    }

    public TarBuffer(OutputStream outputStream, int n, int n2) {
        this.inStream = null;
        this.outStream = outputStream;
        this.initialize(n, n2);
    }

    private void initialize(int n, int n2) {
        this.debug = false;
        this.blockSize = n;
        this.recordSize = n2;
        this.recsPerBlock = this.blockSize / this.recordSize;
        this.blockBuffer = new byte[this.blockSize];
        if (this.inStream != null) {
            this.currBlkIdx = -1;
            this.currRecIdx = this.recsPerBlock;
        } else {
            this.currBlkIdx = 0;
            this.currRecIdx = 0;
        }
    }

    public int getBlockSize() {
        return this.blockSize;
    }

    public int getRecordSize() {
        return this.recordSize;
    }

    public void setDebug(boolean bl) {
        this.debug = bl;
    }

    public boolean isEOFRecord(byte[] byArray) {
        int n = this.getRecordSize();
        for (int i = 0; i < n; ++i) {
            if (byArray[i] == 0) continue;
            return false;
        }
        return true;
    }

    public void skipRecord() throws IOException {
        if (this.debug) {
            System.err.println("SkipRecord: recIdx = " + this.currRecIdx + " blkIdx = " + this.currBlkIdx);
        }
        if (this.inStream == null) {
            throw new IOException("reading (via skip) from an output buffer");
        }
        if (this.currRecIdx >= this.recsPerBlock && !this.readBlock()) {
            return;
        }
        ++this.currRecIdx;
    }

    public byte[] readRecord() throws IOException {
        if (this.debug) {
            System.err.println("ReadRecord: recIdx = " + this.currRecIdx + " blkIdx = " + this.currBlkIdx);
        }
        if (this.inStream == null) {
            throw new IOException("reading from an output buffer");
        }
        if (this.currRecIdx >= this.recsPerBlock && !this.readBlock()) {
            return null;
        }
        byte[] byArray = new byte[this.recordSize];
        System.arraycopy(this.blockBuffer, this.currRecIdx * this.recordSize, byArray, 0, this.recordSize);
        ++this.currRecIdx;
        return byArray;
    }

    private boolean readBlock() throws IOException {
        if (this.debug) {
            System.err.println("ReadBlock: blkIdx = " + this.currBlkIdx);
        }
        if (this.inStream == null) {
            throw new IOException("reading from an output buffer");
        }
        this.currRecIdx = 0;
        int n = 0;
        int n2 = this.blockSize;
        while (n2 > 0) {
            long l = this.inStream.read(this.blockBuffer, n, n2);
            if (l == -1L) {
                if (n == 0) {
                    return false;
                }
                Arrays.fill(this.blockBuffer, n, n + n2, (byte)0);
                break;
            }
            n = (int)((long)n + l);
            n2 = (int)((long)n2 - l);
            if (l == (long)this.blockSize || !this.debug) continue;
            System.err.println("ReadBlock: INCOMPLETE READ " + l + " of " + this.blockSize + " bytes read.");
        }
        ++this.currBlkIdx;
        return true;
    }

    public int getCurrentBlockNum() {
        return this.currBlkIdx;
    }

    public int getCurrentRecordNum() {
        return this.currRecIdx - 1;
    }

    public void writeRecord(byte[] byArray) throws IOException {
        if (this.debug) {
            System.err.println("WriteRecord: recIdx = " + this.currRecIdx + " blkIdx = " + this.currBlkIdx);
        }
        if (this.outStream == null) {
            throw new IOException("writing to an input buffer");
        }
        if (byArray.length != this.recordSize) {
            throw new IOException("record to write has length '" + byArray.length + "' which is not the record size of '" + this.recordSize + "'");
        }
        if (this.currRecIdx >= this.recsPerBlock) {
            this.writeBlock();
        }
        System.arraycopy(byArray, 0, this.blockBuffer, this.currRecIdx * this.recordSize, this.recordSize);
        ++this.currRecIdx;
    }

    public void writeRecord(byte[] byArray, int n) throws IOException {
        if (this.debug) {
            System.err.println("WriteRecord: recIdx = " + this.currRecIdx + " blkIdx = " + this.currBlkIdx);
        }
        if (this.outStream == null) {
            throw new IOException("writing to an input buffer");
        }
        if (n + this.recordSize > byArray.length) {
            throw new IOException("record has length '" + byArray.length + "' with offset '" + n + "' which is less than the record size of '" + this.recordSize + "'");
        }
        if (this.currRecIdx >= this.recsPerBlock) {
            this.writeBlock();
        }
        System.arraycopy(byArray, n, this.blockBuffer, this.currRecIdx * this.recordSize, this.recordSize);
        ++this.currRecIdx;
    }

    private void writeBlock() throws IOException {
        if (this.debug) {
            System.err.println("WriteBlock: blkIdx = " + this.currBlkIdx);
        }
        if (this.outStream == null) {
            throw new IOException("writing to an input buffer");
        }
        this.outStream.write(this.blockBuffer, 0, this.blockSize);
        this.outStream.flush();
        this.currRecIdx = 0;
        ++this.currBlkIdx;
        Arrays.fill(this.blockBuffer, (byte)0);
    }

    private void flushBlock() throws IOException {
        if (this.debug) {
            System.err.println("TarBuffer.flushBlock() called.");
        }
        if (this.outStream == null) {
            throw new IOException("writing to an input buffer");
        }
        if (this.currRecIdx > 0) {
            this.writeBlock();
        }
    }

    public void close() throws IOException {
        if (this.debug) {
            System.err.println("TarBuffer.closeBuffer().");
        }
        if (this.outStream != null) {
            this.flushBlock();
            if (this.outStream != System.out && this.outStream != System.err) {
                this.outStream.close();
                this.outStream = null;
            }
        } else if (this.inStream != null && this.inStream != System.in) {
            this.inStream.close();
            this.inStream = null;
        }
    }
}

