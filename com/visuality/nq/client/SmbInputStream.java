/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.client.AbstractFile;
import com.visuality.nq.client.AsyncConsumer;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Mount;
import com.visuality.nq.common.Buffer;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SmbException;
import com.visuality.nq.common.SyncObject;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Utility;
import com.visuality.nq.common.classinfo.SmbFileInfo;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class SmbInputStream
extends InputStream {
    private LocalAsyncConsumer asyncConsumer = new LocalAsyncConsumer();
    private static final int MIN_ASYNC_LEN = 10000;
    protected File file;
    private boolean isInternalFile = true;
    private int actualLen;
    private Throwable status;
    private Object smbFile;
    static final int END_OF_LOOP = 1;

    public SmbInputStream(File file) {
        TraceLog.get().enter(200);
        this.file = file;
        this.isInternalFile = false;
        TraceLog.get().exit(200);
    }

    public SmbInputStream(Mount mount, String path) throws NqException {
        TraceLog.get().enter(200);
        try {
            this.file = new File(mount, path, new File.Params(1, 0, 1));
        }
        catch (NqException e) {
            TraceLog.get().caught(e, 200);
            TraceLog.get().exit(200);
            throw e;
        }
        TraceLog.get().exit(200);
    }

    public SmbInputStream(Mount mount, String path, File.Params params) throws NqException {
        TraceLog.get().enter(200);
        try {
            this.file = new File(mount, path, params);
        }
        catch (NqException e) {
            TraceLog.get().caught(e, 200);
            TraceLog.get().exit(200);
            throw e;
        }
        TraceLog.get().exit(200);
    }

    public SmbInputStream(URI uri) throws NqException {
        TraceLog.get().enter(200);
        if (!SmbFileInfo.isSmbFileClassSupported()) {
            TraceLog.get().exit(200);
            throw new NqException("This JNQ version does not support SmbFile", -22);
        }
        this.smbFile = SmbFileInfo.getNewSmbFileInstance(uri);
        this.file = SmbFileInfo.smbFileToFile(this.smbFile, new File.Params(1, 1, 1));
        TraceLog.get().exit(200);
    }

    public int read() throws IOException {
        TraceLog.get().enter(200);
        int res = -1;
        byte[] buf = new byte[1];
        try {
            res = this.read(buf, 0, buf.length);
        }
        catch (IOException e) {
            TraceLog.get().caught(e, 200);
            TraceLog.get().exit(200);
            throw e;
        }
        if (1 != res && -1 != res) {
            TraceLog.get().exit(200);
            throw new IOException("Wrong data length: " + res);
        }
        if (-1 == res) {
            TraceLog.get().exit(200);
            return -1;
        }
        TraceLog.get().exit(200);
        return buf[0];
    }

    public int read(byte[] buf) throws IOException {
        TraceLog.get().enter(200);
        int res = -1;
        if (null == buf) {
            TraceLog.get().exit(200);
            throw new IllegalArgumentException("Input buffer cannot be null");
        }
        try {
            res = this.read(buf, 0, buf.length);
        }
        catch (IOException e) {
            TraceLog.get().caught(e, 200);
            TraceLog.get().exit(200);
            throw e;
        }
        TraceLog.get().exit(200);
        return res;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int read(byte[] buf, int off, int len) throws IOException {
        TraceLog.get().enter(200);
        if (null == buf) {
            TraceLog.get().exit(200);
            throw new IllegalArgumentException("Input buffer cannot be null");
        }
        if (len > buf.length - off) {
            TraceLog.get().exit(200);
            throw new IllegalArgumentException("len is greater than buf.length - off");
        }
        if (0 > off) {
            TraceLog.get().exit(200);
            throw new IllegalArgumentException("off is negative");
        }
        if (0 > len) {
            TraceLog.get().exit(200);
            throw new IllegalArgumentException("len is negative");
        }
        if (0 == buf.length) {
            TraceLog.get().exit(200);
            return 0;
        }
        Buffer dataBuffer = new Buffer(buf, off, len);
        int localTimeout = this.file.getRetryTimeout();
        int localRetryCount = this.file.getRetryCount();
        long adaptiveTimeout = 0L;
        boolean isfirstItr = true;
        for (int i = localRetryCount; i > 0; --i) {
            try {
                if (len < 10000) {
                    if (TraceLog.get().canLog(2000)) {
                        TraceLog.get().message("Calling file.read() off = " + off + ", len = " + len + ", position = " + this.file.getPosition(), 2000);
                    }
                    this.actualLen = (int)this.file.read(dataBuffer);
                } else {
                    adaptiveTimeout = isfirstItr ? Client.getSmbTimeout() * (long)(buf.length + this.file.server.maxWrite) / (long)this.file.server.maxWrite : (adaptiveTimeout *= 2L);
                    SmbInputStream smbInputStream = this;
                    synchronized (smbInputStream) {
                        if (TraceLog.get().canLog(2000)) {
                            TraceLog.get().message("Calling file.read() off = " + off + ", len = " + len + ", position = " + this.file.getPosition(), 2000);
                        }
                        this.file.read(dataBuffer, this.asyncConsumer, this.asyncConsumer);
                        this.asyncConsumer.syncObj.syncWait(adaptiveTimeout);
                        if (null != this.status && this.status instanceof SmbException && ((SmbException)this.status).getErrCode() == -1073741267) {
                            dataBuffer.restore();
                            if (i == 1) {
                                TraceLog.get().exit(200);
                                throw (IOException)Utility.throwableInitCauseException(new IOException("read() operation failed after retries"), new SmbException("Operation failed", -1073741267));
                            }
                            Utility.waitABit(localTimeout);
                            localTimeout *= 2;
                            long pos = this.file.getPosition() - (long)len;
                            if (pos < 0L) {
                                pos = 0L;
                            }
                            this.file.setPosition(pos);
                            continue;
                        }
                        if (!(null == this.status || this.status instanceof SmbException && -1073741807 == ((SmbException)this.status).getErrCode())) {
                            TraceLog.get().exit(200);
                            throw this.status;
                        }
                    }
                }
                return this.actualLen;
            }
            catch (NqException e) {
                if (i >= localRetryCount || -1073741267 != e.getErrCode()) {
                    TraceLog.get().exit(200);
                    throw (IOException)Utility.throwableInitCauseException(new IOException(e.getMessage()), e);
                }
                if (-1073741267 == e.getErrCode()) {
                    try {
                        if (!this.file.getServer().reconnect() || !this.file.isOpen()) {
                            if (!this.file.isOpen()) {
                                super.close();
                                TraceLog.get().exit(200);
                                throw (IOException)Utility.throwableInitCauseException(new IOException("File is not open"), e);
                            }
                            TraceLog.get().exit(200);
                            throw (IOException)Utility.throwableInitCauseException(new IOException("Unable to reconnect"), e);
                        }
                        if (i != AbstractFile.END_OF_LOOP) continue;
                        TraceLog.get().exit(200);
                        throw (IOException)Utility.throwableInitCauseException(new IOException("read() operation failed after retries"), e);
                    }
                    catch (NqException ex) {
                        TraceLog.get().exit(200);
                        throw (IOException)Utility.throwableInitCauseException(new IOException(e.getMessage()), e);
                    }
                }
                TraceLog.get().exit(200);
                throw (IOException)Utility.throwableInitCauseException(new IOException(e.getMessage()), e);
            }
            catch (InterruptedException e) {
                TraceLog.get().exit(200);
                throw (IOException)Utility.throwableInitCauseException(new IOException(e.getMessage()), e);
            }
            catch (Throwable e) {
                TraceLog.get().exit(200);
                throw (IOException)Utility.throwableInitCauseException(new IOException(e.getMessage()), e);
            }
        }
        TraceLog.get().exit(200);
        return 0;
    }

    public void close() throws IOException {
        TraceLog.get().enter(200);
        try {
            if (this.isInternalFile) {
                this.file.close();
            }
        }
        catch (NqException e) {
            TraceLog.get().caught(e, 200);
            TraceLog.get().exit(200);
            throw (IOException)Utility.throwableInitCauseException(new IOException(e.getMessage()), e);
        }
        finally {
            if (null != this.smbFile) {
                try {
                    SmbFileInfo.closeSmbFile(this.smbFile);
                }
                catch (NqException e) {
                    TraceLog.get().error("Failed to close smbFile: ", e);
                }
            }
            super.close();
        }
        TraceLog.get().exit(200);
    }

    public Throwable getStatus() {
        return this.status;
    }

    private class LocalAsyncConsumer
    implements AsyncConsumer {
        public final SyncObject syncObj = new SyncObject();

        private LocalAsyncConsumer() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void complete(Throwable status, long length, Object context) {
            TraceLog.get().enter(300);
            if (0L == length && status instanceof SmbException && -1073741807 == ((SmbException)status).getErrCode()) {
                SmbInputStream.this.actualLen = -1;
                status = null;
            } else {
                SmbInputStream.this.actualLen = (int)length;
            }
            LocalAsyncConsumer localAsyncConsumer = this;
            synchronized (localAsyncConsumer) {
                SmbInputStream.this.status = status;
            }
            this.syncObj.syncNotify();
            TraceLog.get().exit(300);
        }
    }
}

