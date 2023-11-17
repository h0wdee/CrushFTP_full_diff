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
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;

public class SmbOutputStream
extends OutputStream {
    private LocalAsyncConsumer asyncConsumer = new LocalAsyncConsumer();
    private static final int MIN_ASYNC_LEN = 10000;
    private Throwable status;
    protected File file;
    private boolean isInternalFile = true;
    private Object smbFile;
    static final int END_OF_LOOP = 1;

    public SmbOutputStream(File file) {
        TraceLog.get().enter(200);
        this.file = file;
        this.isInternalFile = false;
        TraceLog.get().exit(200);
    }

    public SmbOutputStream(Mount mount, String path) throws NqException {
        TraceLog.get().enter(200);
        try {
            this.file = new File(mount, path, new File.Params(2, 0, 0));
        }
        catch (NqException e) {
            TraceLog.get().caught(e, 200);
            TraceLog.get().exit(200);
            throw e;
        }
        TraceLog.get().exit(200);
    }

    public SmbOutputStream(Mount mount, String path, File.Params params) throws NqException {
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

    public SmbOutputStream(URI uri) throws NqException {
        TraceLog.get().enter(200);
        if (!SmbFileInfo.isSmbFileClassSupported()) {
            TraceLog.get().exit(200);
            throw new NqException("This JNQ version does not support SmbFile", -22);
        }
        this.smbFile = SmbFileInfo.getNewSmbFileInstance(uri);
        this.file = SmbFileInfo.smbFileToFile(this.smbFile, new File.Params(2, 2, 1));
        TraceLog.get().exit(200);
    }

    public void write(int val) throws IOException {
        TraceLog.get().enter(200);
        byte tmpVal = (byte)(val & 0xFF);
        try {
            this.write(ByteBuffer.allocate(1).put(tmpVal).array());
        }
        catch (IOException e) {
            TraceLog.get().caught(e, 200);
            TraceLog.get().exit(200);
            throw e;
        }
        TraceLog.get().exit(200);
    }

    public void write(byte[] data) throws IOException {
        TraceLog.get().enter(200);
        if (null == data) {
            TraceLog.get().exit(200);
            throw new IllegalArgumentException("data cannot be null");
        }
        try {
            this.write(data, 0, data.length);
        }
        catch (IOException e) {
            TraceLog.get().caught(e, 200);
            TraceLog.get().exit(200);
            throw e;
        }
        TraceLog.get().exit(200);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void write(byte[] data, int off, int len) throws IOException {
        TraceLog.get().enter(200);
        if (null == data) {
            TraceLog.get().exit(200);
            throw new IllegalArgumentException("data cannot be null");
        }
        if (len > data.length - off) {
            TraceLog.get().exit(200);
            throw new IllegalArgumentException("len is greater than data.length - off");
        }
        if (0 > off) {
            TraceLog.get().exit(200);
            throw new IllegalArgumentException("off is negative");
        }
        if (0 > len) {
            TraceLog.get().exit(200);
            throw new IllegalArgumentException("len is negative");
        }
        Buffer dataBuffer = new Buffer(data, off, len);
        int localTimeout = this.file.getRetryTimeout();
        int localRetryCount = this.file.getRetryCount();
        long adaptiveTimeout = 0L;
        boolean isfirstItr = true;
        for (int i = localRetryCount; i > 0; --i) {
            try {
                if (len < 10000) {
                    this.file.write(dataBuffer);
                    break;
                }
                adaptiveTimeout = isfirstItr ? Client.getSmbTimeout() * (long)(data.length + this.file.server.maxWrite) / (long)this.file.server.maxWrite : (adaptiveTimeout *= 2L);
                SmbOutputStream smbOutputStream = this;
                synchronized (smbOutputStream) {
                    this.file.write(dataBuffer, this.asyncConsumer, this.asyncConsumer);
                    this.asyncConsumer.syncObj.syncWait(adaptiveTimeout);
                    if (null != this.status && this.status instanceof SmbException && ((SmbException)this.status).getErrCode() == -1073741267) {
                        dataBuffer.restore();
                        if (i == 1) {
                            TraceLog.get().exit(200);
                            throw Utility.throwableInitCauseException(new IOException("write() operation failed after retries"), new SmbException("Operation failed", -1073741267));
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
                    if (this.status != null) {
                        TraceLog.get().exit(200);
                        throw this.status;
                    }
                    break;
                }
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
                        throw (IOException)Utility.throwableInitCauseException(new IOException("write() operation failed after retries"), e);
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
    }

    public void flush() throws IOException {
        TraceLog.get().enter(200);
        if (!this.file.isOpen()) {
            TraceLog.get().exit(200);
            return;
        }
        try {
            if (this.isInternalFile) {
                this.file.flush();
            }
            super.flush();
        }
        catch (NqException e) {
            TraceLog.get().caught(e, 200);
            TraceLog.get().exit(200);
            throw (IOException)Utility.throwableInitCauseException(new IOException(e.getMessage()), e);
        }
        TraceLog.get().exit(200);
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
            LocalAsyncConsumer localAsyncConsumer = this;
            synchronized (localAsyncConsumer) {
                SmbOutputStream.this.status = status;
            }
            this.syncObj.syncNotify();
            TraceLog.get().exit(300);
        }
    }
}

