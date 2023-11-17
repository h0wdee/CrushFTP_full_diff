/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.share;

import com.hierynomus.mserref.NtStatus;
import com.hierynomus.mssmb2.SMB2Header;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.mssmb2.messages.SMB2ReadResponse;
import com.hierynomus.protocol.commons.concurrent.Futures;
import com.hierynomus.protocol.transport.TransportException;
import com.hierynomus.smbj.ProgressListener;
import com.hierynomus.smbj.share.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FileInputStream
extends InputStream {
    private final long readTimeout;
    private File file;
    private long offset = 0L;
    private int curr = 0;
    private byte[] buf;
    private ProgressListener progressListener;
    private boolean isClosed;
    private Future<SMB2ReadResponse> nextResponse;
    private static final Logger logger = LoggerFactory.getLogger(FileInputStream.class);
    private int bufferSize;

    FileInputStream(File file, int bufferSize, long readTimeout, ProgressListener progressListener) {
        this.file = file;
        this.bufferSize = bufferSize;
        this.progressListener = progressListener;
        this.readTimeout = readTimeout;
    }

    @Override
    public int read() throws IOException {
        if (this.buf == null || this.curr >= this.buf.length) {
            this.loadBuffer();
        }
        if (this.isClosed) {
            return -1;
        }
        return this.buf[this.curr++] & 0xFF;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (this.buf == null || this.curr >= this.buf.length) {
            this.loadBuffer();
        }
        if (this.isClosed) {
            return -1;
        }
        int l = this.buf.length - this.curr > len ? len : this.buf.length - this.curr;
        System.arraycopy(this.buf, this.curr, b, off, l);
        this.curr += l;
        return l;
    }

    @Override
    public void close() {
        this.isClosed = true;
        this.file = null;
        this.buf = null;
    }

    @Override
    public int available() {
        return 0;
    }

    @Override
    public long skip(long n) {
        if (this.buf == null) {
            this.offset += n;
        } else if ((long)this.curr + n < (long)this.buf.length) {
            this.curr = (int)((long)this.curr + n);
        } else {
            this.offset += (long)this.curr + n - (long)this.buf.length;
            this.buf = null;
            this.nextResponse = null;
        }
        return n;
    }

    private void loadBuffer() throws IOException {
        SMB2ReadResponse res;
        if (this.isClosed) {
            return;
        }
        if (this.nextResponse == null) {
            this.nextResponse = this.sendRequest();
        }
        if (((SMB2Header)(res = Futures.get(this.nextResponse, this.readTimeout, TimeUnit.MILLISECONDS, TransportException.Wrapper)).getHeader()).getStatusCode() == NtStatus.STATUS_SUCCESS.getValue()) {
            this.buf = res.getData();
            this.curr = 0;
            this.offset += (long)res.getDataLength();
            if (this.progressListener != null) {
                this.progressListener.onProgressChanged(res.getDataLength(), this.offset);
            }
        }
        if (((SMB2Header)res.getHeader()).getStatusCode() == NtStatus.STATUS_END_OF_FILE.getValue() || res.getDataLength() == 0) {
            logger.debug("EOF, {} bytes read", (Object)this.offset);
            this.isClosed = true;
            return;
        }
        if (((SMB2Header)res.getHeader()).getStatusCode() != NtStatus.STATUS_SUCCESS.getValue()) {
            throw new SMBApiException((SMB2Header)res.getHeader(), "Read failed for " + this);
        }
        this.nextResponse = this.sendRequest();
    }

    private Future<SMB2ReadResponse> sendRequest() {
        return this.file.readAsync(this.offset, this.bufferSize);
    }
}

