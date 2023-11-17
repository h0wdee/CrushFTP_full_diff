/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.client.AbstractFile;
import com.visuality.nq.client.AsyncConsumer;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.ClientException;
import com.visuality.nq.client.ClientUtils;
import com.visuality.nq.client.Mount;
import com.visuality.nq.client.Server;
import com.visuality.nq.client.Share;
import com.visuality.nq.client.Smb100;
import com.visuality.nq.client.User;
import com.visuality.nq.client.dfs.Dfs;
import com.visuality.nq.client.dfs.Result;
import com.visuality.nq.client.rpc.Dcerpc;
import com.visuality.nq.client.rpc.Lsar;
import com.visuality.nq.common.Buffer;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SecurityDescriptor;
import com.visuality.nq.common.Sid;
import com.visuality.nq.common.SmbException;
import com.visuality.nq.common.SyncObject;
import com.visuality.nq.common.TimeUtility;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.UUID;
import com.visuality.nq.common.Utility;
import com.visuality.nq.config.Config;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class File
extends AbstractFile {
    private String localPath;
    Params params;
    private boolean isOpen = false;
    private boolean disconnected = false;
    private boolean isDfsPath = false;
    private long position = 0L;
    private int dfsCounter = 50;
    private static final int EOF = -1;
    private static final String BACKSLASH = "\\";
    private static final String SLASH = "/";
    private static final String UNABLE_RECONNECT = "Unable to reconnect";
    private static final String UNABLE_RECONNECT_COLON = "Unable to reconnect: ";
    private static final String UNABLE_CONNECT = "Unable to connect ";
    private static final String FILE_NOT_OPEN = "File is not open";
    private static final String WRITE_FAILED = "Write failed";
    private static final String OPERATION_FAILED = "Operation failed";
    private static final String FILE_CLOSED = "File is closed";
    private static final String BROKEN_CONNECTION = "Broken connection";
    private static final String NOT_SUPPORT_COPY_WITH_DIFFERENT_SHARES = "The server does not support server-side data copy with different shares";
    private static final String NOT_SUPPORT_SERVER_SIDE_COPY = "The server does not support server-side data copy";
    private static final String MISSING_ACCESS_READ_SYSTEM_SECURITY = "No access to read security descriptor, use ACCESS_READ_SYSTEM_SECURITY or ACCESS_SYSTEM_SECURITY";
    private static final String MISSING_ACCESS_SYSTEM_SECURITY = "Missing flag ACCESS_SYSTEM_SECURITY in file params";
    private static final int ACCESSMASKDEFAULT = 0x100080;
    private static final int MAX_CHUNKS_NUM = 16;
    private static final int MAX_CHUNK_COPY_SIZE = 0x100000;
    protected Info info = new Info();
    protected int accessMask;
    protected int shareAccess;
    protected int disposition;
    protected byte oplockLevel = 0;
    protected int createOptions = 0;
    protected byte[] fid = new byte[16];
    protected int durableState = 1;
    protected UUID durableHandle = new UUID();
    protected int durableTimeout = 0;
    protected int durableFlags = 0;
    protected String previousPath = "";
    public static final int defaultAccess = 0;
    public static final int defaultShareAccess = 0;
    public static final int defaultDisposition = 0;
    private Queue aidChangeNotifyQueue = new LinkedList();
    public static final int DURABLE_REQUIRED = 1;
    public static final int DURABLE_NOTREQUIRED = 2;
    public static final int DURABLE_GRANTED = 3;
    public static final int DURABLE_CANCELED = 4;
    public static final int ACCESS_SPECIAL = 0x8000000;
    public static final int ACCESS_READ = 1;
    public static final int ACCESS_WRITE = 2;
    public static final int ACCESS_APPEND = 4;
    public static final int ACCESS_DELETE = 8;
    public static final int ACCESS_READ_ATTR = 16;
    public static final int ACCESS_WRITE_ATTR = 32;
    public static final int ACCESS_SYSTEM_SECURITY = 64;
    public static final int ACCESS_READ_SYSTEM_SECURITY = 128;
    protected static final int ACCESS_READ_DIRECTORY = 0x100081;
    public static final int SHARE_READ = 1;
    public static final int SHARE_WRITE = 2;
    public static final int SHARE_DELETE = 4;
    public static final int SHARE_EXCLUSIVE = 0;
    public static final int SHARE_READWRITE = 3;
    public static final int SHARE_FULL = 7;
    public static final int DISPOSITION_SUPERSEDE = 0;
    public static final int DISPOSITION_OPEN = 1;
    public static final int DISPOSITION_CREATE = 2;
    public static final int DISPOSITION_OPEN_IF = 3;
    public static final int DISPOSITION_OVERWRITE = 4;
    public static final int DISPOSITION_OVERWRITE_IF = 5;
    public static final int ATTR_READONLY = 1;
    public static final int ATTR_HIDDEN = 2;
    public static final int ATTR_SYSTEM = 4;
    public static final int ATTR_VOLUME = 8;
    public static final int ATTR_DIR = 16;
    public static final int ATTR_ARCHIVE = 32;
    public static final int FILE_NOTIFY_CHANGE_FILE_NAME = 1;
    public static final int FILE_NOTIFY_CHANGE_DIR_NAME = 2;
    public static final int FILE_NOTIFY_CHANGE_ATTRIBUTES = 4;
    public static final int FILE_NOTIFY_CHANGE_SIZE = 8;
    public static final int FILE_NOTIFY_CHANGE_LAST_WRITE = 16;
    public static final int FILE_NOTIFY_CHANGE_LAST_ACCESS = 32;
    public static final int FILE_NOTIFY_CHANGE_CREATION = 64;
    public static final int FILE_NOTIFY_CHANGE_EA = 128;
    public static final int FILE_NOTIFY_CHANGE_SECURITY = 256;
    public static final int FILE_NOTIFY_CHANGE_STREAM_NAME = 512;
    public static final int FILE_NOTIFY_CHANGE_STREAM_SIZE = 1024;
    public static final int FILE_NOTIFY_ALL = 2047;
    public static final int INFO_ATTR = 1;
    public static final int INFO_CREATION = 2;
    public static final int INFO_LASTACCESS = 4;
    public static final int INFO_LASTWRITE = 8;
    public static final int INFO_EOF = 16;
    public static final int INFO_ALLOCATION = 32;

    protected boolean isDir() {
        return 0 != (this.createOptions & 1);
    }

    protected File() throws NqException {
        try {
            this.retryCount = Config.jnq.getInt("RETRYCOUNT");
        }
        catch (NqException e) {
            this.retryCount = 3;
        }
        try {
            this.retryTimeout = Config.jnq.getInt("RETRYTIMEOUT");
        }
        catch (NqException e) {
            this.retryTimeout = 0;
        }
        this.dfsCounter = this.getRetryCount() * 50;
    }

    void initializeRetryInfo(Mount mount) {
        mount.fetchRetryValues();
        this.setRetryCount(mount.getRetryCount());
        this.setRetryTimeout(mount.getRetryTimeout());
    }

    protected File(Mount mount, Params params) throws NqException {
        TraceLog.get().enter(200);
        if (null == mount || null == params) {
            throw new NqException("Invalid parameters: mount or params is null", -20);
        }
        this.initializeRetryInfo(mount);
        this.dfsCounter = this.getRetryCount() * 50;
        this.create(mount, params);
        TraceLog.get().exit(200);
    }

    protected File(Mount mount, String localPath) throws NqException {
        TraceLog.get().enter("localPath = " + localPath, 2000);
        if (null == mount || null == localPath) {
            throw new NqException("Invalid parameters: mount or localPath is null", -20);
        }
        this.initializeRetryInfo(mount);
        this.dfsCounter = this.getRetryCount() * 50;
        this.open(mount, localPath);
        TraceLog.get().exit(200);
    }

    public File(Mount mount, String localPath, Params params) throws NqException {
        TraceLog.get().enter("localPath = " + localPath, 2000);
        if (null == mount || null == localPath || null == params) {
            TraceLog.get().exit(200);
            throw new NqException("Invalid parameters: mount or params or localPath is null", -20);
        }
        this.initializeRetryInfo(mount);
        this.dfsCounter = this.getRetryCount() * 50;
        try {
            this.create(mount, localPath, params);
        }
        catch (NqException e) {
            TraceLog.get().caught(e, 200);
            TraceLog.get().exit(200);
            throw e;
        }
        TraceLog.get().exit(200);
    }

    protected void finalize() throws Throwable {
        if (this.isOpen()) {
            try {
                this.close();
            }
            catch (NqException nqException) {
                // empty catch block
            }
        }
    }

    public void create(Mount mount, String localPath, Params params) throws NqException {
        TraceLog.get().enter("localPath=" + localPath, 2000);
        String relative = mount.getRelativeShareName();
        relative = relative.equals("") ? relative : (localPath.equals("") ? relative : relative + BACKSLASH);
        this.setShareRelativePath(relative + localPath);
        this.setLocalPath(localPath);
        TraceLog.get().message("localPath=" + this.getLocalPath() + ", shareRelativePath=" + this.getLocalPathFromShare(), 2000);
        this.params = params;
        this.mount = mount;
        try {
            this.checkMountConnection();
            this.openOrCreate();
        }
        catch (NqException e) {
            TraceLog.get().caught(e);
            throw e;
        }
        TraceLog.get().exit(200);
    }

    private void open(Mount mount, String localPath) throws NqException {
        TraceLog.get().enter(200);
        String relative = mount.getRelativeShareName();
        relative = relative.equals("") ? relative : (localPath.equals("") ? relative : relative + BACKSLASH);
        this.setShareRelativePath(relative + localPath);
        this.setLocalPath(localPath);
        TraceLog.get().message("localPath=" + this.getLocalPath() + ", shareRelativePath=" + this.getLocalPathFromShare(), 2000);
        this.params = new Params(3, 7, 1, false);
        this.mount = mount;
        this.openOrCreate();
        TraceLog.get().exit(200);
    }

    private void create(Mount mount, Params params) throws NqException {
        TraceLog.get().enter(200);
        this.setLocalPath("");
        this.setShareRelativePath(mount.getRelativeShareName());
        TraceLog.get().message("localPath=" + this.getLocalPath() + ", shareRelativePath=" + this.getLocalPathFromShare(), 2000);
        this.params = params;
        this.mount = mount;
        this.openOrCreate();
        TraceLog.get().exit(200);
    }

    protected String getLocalPathFromShare() {
        return this.info.path;
    }

    protected void setShareRelativePath(String path) {
        this.info.path = path;
    }

    public byte[] getFid() {
        return this.fid;
    }

    public long getPosition() {
        return this.position;
    }

    public void setPosition(long newPosition) {
        this.position = newPosition;
    }

    public String getLocalPath() {
        return this.localPath;
    }

    protected void setLocalPath(String localPath) {
        if (localPath.contains(SLASH)) {
            localPath = localPath.replace(SLASH, BACKSLASH);
        }
        this.localPath = localPath;
    }

    public boolean isOpen() {
        return this.isOpen;
    }

    public boolean close() throws NqException {
        TraceLog.get().enter(200);
        boolean result = false;
        int res = 0;
        if (!this.isOpen) {
            TraceLog.get().exit(200);
            return true;
        }
        if (this.server.connectionBroke) {
            TraceLog.get().exit(200);
            throw new ClientException("File is closed due to Broken connection", -111);
        }
        try {
            res = (Integer)this.execute(AbstractFile.SmbMethods.doClose, new Object[0]);
        }
        catch (NqException e) {
            TraceLog.get().caught(e);
            TraceLog.get().exit(200);
            throw e;
        }
        this.share.files.remove(this.fid);
        this.isOpen = false;
        try {
            Share.disconnectShareInternally(this.mount, this.server, this.share);
        }
        catch (NqException e) {
            TraceLog.get().caught(e);
            TraceLog.get().exit(200);
            throw e;
        }
        result = res == 0;
        TraceLog.get().exit(200);
        return result;
    }

    /*
     * Exception decompiling
     */
    public long write(Buffer data, long offset) throws NqException {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [8[DOLOOP]], but top level block is 19[SIMPLE_IF_TAKEN]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    public void write(Buffer data) throws NqException {
        this.write(data, this.position);
    }

    public void write(Buffer data, long filePosition, AsyncConsumer consumer, Object context) throws NqException {
        int maxWrite;
        int bytesToWrite;
        TraceLog.get().enter(200);
        data.save();
        CummulativeAsynConsumer internalConsumer = new CummulativeAsynConsumer(consumer, context, data);
        if (!this.isOpen) {
            data.restore();
            TraceLog.get().exit(200);
            throw new SmbException(FILE_NOT_OPEN, -1073741267);
        }
        try {
            if (!(this.server.transport.isConnected() || this.server.reconnect() && this.isOpen)) {
                if (!this.isOpen) {
                    TraceLog.get().exit(200);
                    throw new SmbException(FILE_NOT_OPEN, -1073741267);
                }
                TraceLog.get().exit(200);
                throw new ClientException(UNABLE_RECONNECT, -111);
            }
        }
        catch (NqException exNq) {
            if (-15 != exNq.getErrCode()) {
                data.restore();
                TraceLog.get().caught(exNq, 200);
                TraceLog.get().exit(200);
                throw new ClientException(UNABLE_RECONNECT_COLON + exNq.getMessage(), -111);
            }
        }
        catch (Exception ex) {
            data.restore();
            TraceLog.get().caught(ex, 200);
            TraceLog.get().exit(200);
            throw new ClientException(UNABLE_RECONNECT_COLON + ex.getMessage(), -111);
        }
        internalConsumer.numRequests = (bytesToWrite = data.dataLen) > (maxWrite = this.mount.share.isPrinter() ? this.server.maxTrans : this.server.maxWrite) ? (long)(bytesToWrite % maxWrite != 0 ? bytesToWrite / maxWrite + 1 : bytesToWrite / maxWrite) : 1L;
        this.position = filePosition;
        while (bytesToWrite > 0) {
            int localRetryCount;
            data.dataLen = bytesToWrite <= maxWrite ? bytesToWrite : maxWrite;
            int localTimeout = this.getRetryTimeout();
            for (int i = localRetryCount = this.getRetryCount(); i > 0; --i) {
                try {
                    this.server.smb.doWrite(this, data, internalConsumer, internalConsumer, null);
                    break;
                }
                catch (NqException ex) {
                    TraceLog.get().caught(ex, 200);
                    if (-1073741267 == ex.getErrCode()) {
                        try {
                            if (!this.server.reconnect() || !this.isOpen) {
                                if (!this.isOpen) {
                                    TraceLog.get().exit(200);
                                    throw new SmbException(FILE_NOT_OPEN, -1073741267);
                                }
                                TraceLog.get().exit(200);
                                throw new ClientException(UNABLE_RECONNECT, -111);
                            }
                            if (i == END_OF_LOOP) {
                                throw new ClientException(UNABLE_CONNECT, -111);
                            }
                        }
                        catch (NqException exSmb) {
                            if (exSmb.getErrCode() != -15 && i == END_OF_LOOP) {
                                data.restore();
                                TraceLog.get().caught(exSmb, 200);
                                TraceLog.get().exit(200);
                                throw (SmbException)Utility.throwableInitCauseException(new SmbException(OPERATION_FAILED, -1073741267), exSmb);
                            }
                        }
                        catch (Exception ex1) {
                            data.restore();
                            TraceLog.get().caught(ex1, 200);
                            TraceLog.get().exit(200);
                            throw new ClientException(UNABLE_RECONNECT_COLON + ex1.getMessage(), -111);
                        }
                        if (localRetryCount == i) continue;
                        Utility.waitABit(localTimeout);
                        localTimeout *= 2;
                        long pos = this.getPosition() - (long)data.dataLen;
                        if (pos < 0L) {
                            pos = 0L;
                        }
                        this.setPosition(pos);
                        if (END_OF_LOOP != i) continue;
                        data.restore();
                        TraceLog.get().caught(ex);
                        TraceLog.get().exit(200);
                        throw new ClientException("Write error: " + ex.getMessage(), ex.getErrCode());
                    }
                    TraceLog.get().caught(ex);
                    TraceLog.get().exit(200);
                    throw ex;
                }
            }
            bytesToWrite -= data.dataLen;
            this.position += (long)data.dataLen;
            data.offset += data.dataLen;
        }
        data.restore();
        TraceLog.get().exit(200);
    }

    public void write(Buffer data, AsyncConsumer consumer, Object context) throws NqException {
        this.write(data, this.position, consumer, context);
    }

    public long read(Buffer data, long position) throws NqException {
        int localRetryCount;
        InternalSync sync;
        block21: {
            TraceLog.get().enter(200);
            data.save();
            sync = new InternalSync();
            if (!this.isOpen) {
                data.restore();
                TraceLog.get().exit(200);
                throw new SmbException(FILE_NOT_OPEN, -1073741267);
            }
            if (0 == data.dataLen) {
                data.restore();
                TraceLog.get().exit(200);
                return 0L;
            }
            try {
                if (!(this.server.transport.isConnected() || this.server.reconnect() && this.isOpen)) {
                    data.restore();
                    if (!this.isOpen) {
                        TraceLog.get().exit(200);
                        throw new SmbException(FILE_NOT_OPEN, -1073741267);
                    }
                    TraceLog.get().exit(200);
                    throw new ClientException(UNABLE_RECONNECT, -111);
                }
            }
            catch (NqException ex) {
                if (-15 == ex.getErrCode()) break block21;
                data.restore();
                TraceLog.get().caught(ex);
                TraceLog.get().exit(200);
                throw new ClientException(UNABLE_RECONNECT_COLON + ex.getMessage(), -111);
            }
        }
        int localTimeout = this.getRetryTimeout();
        for (int i = localRetryCount = this.getRetryCount(); i > 0; --i) {
            long adaptiveTimeout = 0L;
            boolean isfirstItr = true;
            this.position = position;
            this.read(data, sync, sync);
            try {
                do {
                    adaptiveTimeout = isfirstItr ? Client.getSmbTimeout() * (long)(data.dataLen + this.server.maxWrite) / (long)this.server.maxWrite : (adaptiveTimeout *= 2L);
                    sync.syncObj.syncWait(adaptiveTimeout);
                    if (!isfirstItr) break;
                    isfirstItr = false;
                } while (sync.status instanceof SmbException && 259 == ((SmbException)sync.status).getErrCode());
            }
            catch (InterruptedException e) {
                this.server.asyncRemoveItem(sync);
                if (localRetryCount == i) continue;
                Utility.waitABit(localTimeout);
                localTimeout *= 2;
                continue;
            }
            if (sync.timedOut) {
                Utility.waitABit(localTimeout);
                localTimeout *= 2;
                continue;
            }
            int status = sync.status == null ? 0 : (sync.status instanceof SmbException ? ((SmbException)sync.status).getErrCode() : (sync.status instanceof ClientException ? ((ClientException)sync.status).getErrCode() : (sync.status instanceof NqException ? ((NqException)sync.status).getErrCode() : -104)));
            if (status != 0) {
                data.offset = 0;
            }
            if (-1073741267 == status) {
                try {
                    if (i != END_OF_LOOP && this.server.reconnect()) {
                        sync.status = null;
                        Utility.waitABit(localTimeout);
                        localTimeout *= 2;
                        continue;
                    }
                    if (i == END_OF_LOOP) {
                        data.restore();
                        TraceLog.get().exit(200);
                        throw new SmbException(OPERATION_FAILED, -1073741267);
                    }
                }
                catch (NqException e) {
                    data.restore();
                    TraceLog.get().caught(e);
                    TraceLog.get().exit(200);
                    throw e;
                }
                data.restore();
                TraceLog.get().exit(200);
                throw new ClientException(UNABLE_RECONNECT, -111);
            }
            if (this.share.isIpc && status == -1073741607) {
                if (i == END_OF_LOOP) {
                    String err = "Read retry count expired";
                    TraceLog.get().exit(200);
                    throw new NqException(err, status);
                }
                try {
                    Thread.sleep(100L);
                }
                catch (InterruptedException e) {
                    // empty catch block
                }
                Utility.waitABit(localTimeout);
                localTimeout *= 2;
                continue;
            }
            if (status == -1073741267 || status == 0 || status == -1073741807) break;
            data.restore();
            TraceLog.get().exit(200);
            throw new SmbException("Read error", status);
        }
        data.restore();
        this.position = -1L == sync.length ? position : position + sync.length;
        TraceLog.get().exit(200);
        return sync.length;
    }

    public long read(Buffer data) throws NqException {
        return this.read(data, this.position);
    }

    public void read(Buffer data, long position, AsyncConsumer consumer, Object context) throws NqException {
        TraceLog.get().enter(200);
        data.save();
        CummulativeAsynConsumer internalConsumer = new CummulativeAsynConsumer(consumer, context, data);
        if (!this.isOpen) {
            data.restore();
            TraceLog.get().exit(200);
            throw new SmbException(FILE_NOT_OPEN, -1073741267);
        }
        try {
            if (!(this.server.transport.isConnected() || this.server.reconnect() && this.isOpen)) {
                data.restore();
                if (!this.isOpen) {
                    TraceLog.get().exit(200);
                    throw new SmbException(FILE_NOT_OPEN, -1073741267);
                }
                TraceLog.get().exit(200);
                throw new ClientException(UNABLE_RECONNECT, -111);
            }
        }
        catch (NqException exNq) {
            if (-15 != exNq.getErrCode()) {
                data.restore();
                TraceLog.get().caught(exNq, 200);
                TraceLog.get().exit(200);
                throw new ClientException(UNABLE_RECONNECT_COLON + exNq.getMessage(), -111);
            }
        }
        catch (Exception ex) {
            data.restore();
            TraceLog.get().caught(ex, 200);
            TraceLog.get().exit(200);
            throw new ClientException(UNABLE_RECONNECT_COLON + ex.getMessage(), -111);
        }
        this.position = position;
        internalConsumer.totalBytes = data.dataLen;
        int bytesToRead = data.dataLen;
        int maxRead = this.server.maxRead;
        long l = bytesToRead > maxRead ? (long)(bytesToRead % maxRead != 0 ? bytesToRead / maxRead + 1 : bytesToRead / maxRead) : (internalConsumer.numRequests = 1L);
        while (bytesToRead > 0) {
            int localRetryCount;
            int readNow;
            data.dataLen = readNow = bytesToRead <= maxRead ? bytesToRead : maxRead;
            int localTimeout = this.getRetryTimeout();
            for (int i = localRetryCount = this.getRetryCount(); i > 0; --i) {
                int status = 0;
                try {
                    this.server.smb.doRead(this, data, internalConsumer, internalConsumer, null);
                    data.offset += readNow;
                    break;
                }
                catch (SmbException ex) {
                    status = ex.getErrCode();
                    if (-1073741267 == status) {
                        TraceLog.get().caught(ex, 200);
                        try {
                            if (!this.server.reconnect() || !this.isOpen) {
                                data.restore();
                                if (!this.isOpen) {
                                    TraceLog.get().caught(ex, 200);
                                    TraceLog.get().exit(200);
                                    throw new SmbException(FILE_NOT_OPEN, -1073741267);
                                }
                                TraceLog.get().caught(ex, 200);
                                TraceLog.get().exit(200);
                                throw new ClientException(UNABLE_RECONNECT, -111);
                            }
                            if (i == END_OF_LOOP) {
                                TraceLog.get().caught(ex, 200);
                                TraceLog.get().exit(200);
                                throw new SmbException(OPERATION_FAILED, -1073741267);
                            }
                        }
                        catch (NqException exNq) {
                            if (-15 != exNq.getErrCode()) {
                                data.restore();
                                TraceLog.get().caught(exNq, 200);
                                TraceLog.get().exit(200);
                                throw new ClientException(UNABLE_RECONNECT_COLON + exNq.getMessage(), -111);
                            }
                        }
                        catch (Exception e) {
                            data.restore();
                            TraceLog.get().caught(e, 200);
                            TraceLog.get().exit(200);
                            throw new ClientException(UNABLE_RECONNECT_COLON + e.getMessage(), -111);
                        }
                        if (localRetryCount == i) continue;
                        Utility.waitABit(localTimeout);
                        localTimeout *= 2;
                        long pos = this.getPosition() - (long)data.dataLen;
                        if (pos < 0L) {
                            pos = 0L;
                        }
                        this.setPosition(pos);
                        if (END_OF_LOOP != i) continue;
                        data.restore();
                        TraceLog.get().caught(ex, 200);
                        TraceLog.get().exit(200);
                        throw new ClientException("Read error: " + ex.getMessage(), ex.getErrCode());
                    }
                    data.restore();
                    TraceLog.get().caught(ex, 200);
                    TraceLog.get().exit(200);
                    throw new ClientException("Read error: " + ex.getMessage(), status);
                }
            }
            bytesToRead -= readNow;
            this.position += (long)readNow;
        }
        data.restore();
        TraceLog.get().exit(200);
    }

    public void read(Buffer data, AsyncConsumer consumer, Object context) throws NqException {
        this.read(data, this.position, consumer, context);
    }

    public void flush() throws NqException {
        TraceLog.get().enter(200);
        try {
            this.execute(AbstractFile.SmbMethods.doFlush, new Object[0]);
        }
        catch (NqException e) {
            TraceLog.get().caught(e);
            TraceLog.get().exit(200);
            throw e;
        }
        TraceLog.get().exit(200);
    }

    public static void rename(Mount mount, String oldPath, String newPath) throws NqException {
        File.rename(mount, oldPath, newPath, false);
    }

    public static void rename(Mount mount, String oldPath, String newPath, boolean overwriteExistingFile) throws NqException {
        int retryCount;
        TraceLog.get().enter("oldPath=" + oldPath + ", newPath=" + newPath + ", overwriteExistingFile=" + overwriteExistingFile, 200);
        if (oldPath == null || newPath == null) {
            TraceLog.get().exit(200);
            throw new ClientException("Invalid file name", -103);
        }
        if (oldPath.contains(SLASH)) {
            oldPath = oldPath.replace(SLASH, BACKSLASH);
        }
        if (newPath.contains(SLASH)) {
            newPath = newPath.replace(SLASH, BACKSLASH);
        }
        if (oldPath.equals(newPath)) {
            TraceLog.get().exit(200);
            return;
        }
        if (mount.share.isPrinter()) {
            TraceLog.get().exit(200);
            throw new ClientException("Cannot move files on a printer share", -103);
        }
        File file = new File(mount, oldPath, new Params(8, 7, 1));
        Server server = file.share.getUser().getServer();
        boolean createBeforeMove = file.share.getUser().getServer().smb.createBeforeMove;
        File tmpFile = null;
        try {
            tmpFile = File.checkPath(mount, newPath, true);
        }
        catch (NqException e) {
            TraceLog.get().caught(e);
            if (createBeforeMove) {
                file.close();
            }
            TraceLog.get().exit(200);
            throw new ClientException("New Path is in a wrong share point", -103);
        }
        if (tmpFile.share != file.share) {
            if (createBeforeMove) {
                file.close();
            }
            TraceLog.get().exit(200);
            throw new ClientException("Dfs received wrong share name", -103);
        }
        newPath = tmpFile.getLocalPathFromShare() + ClientUtils.filePathGetLastComponent(newPath, tmpFile.getLocalPathFromShare().length() <= 0);
        int localTimeout = mount.getRetryTimeout();
        for (int i = retryCount = mount.getRetryCount(); i > 0; --i) {
            try {
                file.share.getUser().getServer().smb.doRename(file, newPath, overwriteExistingFile);
                break;
            }
            catch (NqException e) {
                if (i == END_OF_LOOP || -1073741267 != e.getErrCode()) {
                    if (createBeforeMove) {
                        file.close();
                    }
                    TraceLog.get().caught(e, 200);
                    TraceLog.get().exit(200);
                    throw new ClientException("Rename error : " + e.getMessage(), e.getErrCode());
                }
                if (-1073741267 == e.getErrCode()) {
                    try {
                        if (!server.reconnect()) {
                            TraceLog.get().caught(e, 200);
                            TraceLog.get().exit(200);
                            throw new ClientException(UNABLE_RECONNECT, -111);
                        }
                        if (i == END_OF_LOOP) {
                            if (createBeforeMove) {
                                file.close();
                            }
                            TraceLog.get().caught(e, 200);
                            TraceLog.get().exit(200);
                            throw new SmbException(OPERATION_FAILED, -1073741267);
                        }
                    }
                    catch (Exception ex) {
                        TraceLog.get().caught(ex, 200);
                        TraceLog.get().exit(200);
                        throw new ClientException(UNABLE_RECONNECT_COLON + ex.getMessage(), -111);
                    }
                }
                if (retryCount == i) continue;
                Utility.waitABit(localTimeout);
                localTimeout *= 2;
                continue;
            }
        }
        if (createBeforeMove) {
            file.close();
        }
        TraceLog.get().exit(200);
    }

    public static void delete(Mount mount, String localPath) throws NqException {
        File file;
        block8: {
            TraceLog.get().enter(200);
            file = null;
            boolean isRoot = localPath.equals("");
            if (isRoot) {
                TraceLog.get().exit("cannot delete root folder", 200);
                return;
            }
            try {
                file = new File(mount, localPath, new Params(8, 7, 1, false, false));
            }
            catch (NqException e) {
                if (-1073741638 == e.getErrCode()) break block8;
                TraceLog.get().caught(e);
                TraceLog.get().exit(200);
                throw e;
            }
        }
        try {
            if (null == file) {
                file = new File(mount, localPath, new Params(8, 7, 1, true, false));
            }
        }
        catch (NqException e) {
            TraceLog.get().caught(e);
            TraceLog.get().exit(200);
            throw e;
        }
        try {
            file.execute(AbstractFile.SmbMethods.doSetFileDeleteOnClose, new Object[0]);
        }
        catch (NqException e) {
            file.close();
            TraceLog.get().caught(e);
            TraceLog.get().exit(200);
            throw e;
        }
        file.close();
        TraceLog.get().exit(200);
    }

    @Deprecated
    public static void delete(Mount mount, String localPath, boolean isDir) throws NqException {
        TraceLog.get().enter(200);
        File file = new File(mount, localPath, new Params(8, 7, 1, isDir, true));
        try {
            file.execute(AbstractFile.SmbMethods.doSetFileDeleteOnClose, new Object[0]);
        }
        catch (NqException e) {
            file.close();
            TraceLog.get().caught(e);
            throw e;
        }
        file.close();
        TraceLog.get().exit(200);
    }

    public void deleteOnClose() throws NqException {
        TraceLog.get().enter(200);
        if (!this.isOpen) {
            TraceLog.get().exit(200);
            throw new SmbException(FILE_NOT_OPEN, -1073741267);
        }
        try {
            this.execute(AbstractFile.SmbMethods.doSetFileDeleteOnClose, new Object[0]);
        }
        catch (NqException e) {
            TraceLog.get().caught(e);
            TraceLog.get().exit(200);
            throw e;
        }
        TraceLog.get().exit(200);
    }

    public Info getInfo() throws NqException {
        TraceLog.get().enter(200);
        if (!this.isOpen) {
            TraceLog.get().exit(200);
            throw new SmbException(FILE_NOT_OPEN, -1073741267);
        }
        Info info = null;
        try {
            info = (Info)this.execute(AbstractFile.SmbMethods.doQueryFileInfoByHandle, new Object[0]);
            this.info.attributes = info.attributes;
            this.info.eof = info.eof;
            this.info.creationTime = info.creationTime;
            this.info.lastAccessTime = info.lastAccessTime;
            this.info.lastWriteTime = info.lastWriteTime;
        }
        catch (NqException e) {
            TraceLog.get().caught(e);
            TraceLog.get().exit(200);
            throw e;
        }
        TraceLog.get().exit(info, 200);
        return info;
    }

    public int getDurableFlags() {
        return this.durableFlags;
    }

    public int getDurableState() {
        return this.durableState;
    }

    public void setInfo(Info info) throws NqException {
        TraceLog.get().enter(info, 2000);
        NqException exceptionThrown = null;
        boolean isGood = false;
        if (!this.isOpen) {
            TraceLog.get().exit(200);
            throw new SmbException(FILE_NOT_OPEN, -1073741267);
        }
        try {
            this.execute(AbstractFile.SmbMethods.doSetFileAttributes, info.attributes);
            this.info.attributes = info.attributes;
            isGood = true;
        }
        catch (NqException e) {
            TraceLog.get().message("setInfo error executing doSetFileAttributes = ", e, 2000);
            exceptionThrown = e;
        }
        try {
            this.execute(AbstractFile.SmbMethods.doSetFileSize, info.eof);
            this.info.eof = info.eof;
            isGood = true;
        }
        catch (NqException e) {
            TraceLog.get().message("setInfo error executing doSetFileSize = ", e, 2000);
            exceptionThrown = e;
        }
        try {
            this.execute(AbstractFile.SmbMethods.doSetFileTime, info.creationTime, info.lastAccessTime, info.lastWriteTime);
            this.info.creationTime = info.creationTime;
            this.info.lastAccessTime = info.lastAccessTime;
            this.info.lastWriteTime = info.lastWriteTime;
            isGood = true;
        }
        catch (NqException e) {
            TraceLog.get().message("setInfo error executing doSetFileTime = ", e, 2000);
            exceptionThrown = e;
        }
        if (!isGood) {
            TraceLog.get().caught(exceptionThrown);
            TraceLog.get().exit(200);
            throw exceptionThrown;
        }
        TraceLog.get().exit(200);
    }

    public static boolean isExist(Mount mount, String localPath, boolean isDir) throws NqException {
        boolean res;
        TraceLog.get().enter(200);
        try {
            Params pr = new Params(1, 7, 1, isDir);
            File file = new File(mount, localPath, pr);
            res = true;
            file.close();
        }
        catch (NqException e) {
            res = false;
        }
        TraceLog.get().exit("res = " + res, 200);
        return res;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected static Info getInfo(Mount mount, String localPath, String fullPath) throws NqException {
        TraceLog.get().enter(200);
        File.checkMountConnection(mount);
        Info info = null;
        if (localPath.contains(SLASH)) {
            localPath = localPath.replace(SLASH, BACKSLASH);
        }
        Params fileParams = new Params(16, 1, 1, false);
        File file = null;
        try {
            file = new File(mount, localPath, fileParams);
        }
        catch (NqException e) {
            TraceLog.get().caught(e, 200);
            throw e;
        }
        for (int i = mount.getRetryCount(); i > 0; --i) {
            try {
                info = file.server.smb.doQueryFileInfoByHandle(file);
                break;
            }
            catch (NqException e) {
                if (-1073741267 != e.getErrCode()) {
                    TraceLog.get().caught(e);
                    throw e;
                }
                try {
                    if (!mount.getServer().reconnect()) {
                        TraceLog.get().exit(200);
                        throw new ClientException(UNABLE_RECONNECT, -111);
                    }
                    if (i != END_OF_LOOP) continue;
                    TraceLog.get().exit(200);
                    throw new SmbException(OPERATION_FAILED, -1073741267);
                }
                catch (Exception ex) {
                    TraceLog.get().caught(ex, 200);
                    throw new ClientException(UNABLE_RECONNECT_COLON + ex.getMessage(), -111);
                }
                finally {
                    if (null != file) {
                        file.close();
                        file = null;
                    }
                }
            }
            finally {
                if (null != file) {
                    file.close();
                }
            }
        }
        TraceLog.get().exit(info, 200);
        return info;
    }

    private static Info getInfoForSmb1(Mount mount, String localPath, String fullPath) throws NqException {
        TraceLog.get().enter(200);
        File.checkMountConnection(mount);
        Info info = null;
        for (int i = mount.getRetryCount(); i > 0; --i) {
            try {
                info = mount.server.smb.doQueryFileInfoByName(mount.share, fullPath + BACKSLASH + localPath);
                break;
            }
            catch (NqException e) {
                if (-1073741267 != e.getErrCode()) {
                    TraceLog.get().caught(e);
                    throw e;
                }
                try {
                    if (!mount.getServer().reconnect()) {
                        TraceLog.get().exit(200);
                        throw new ClientException(UNABLE_RECONNECT, -111);
                    }
                    if (i != END_OF_LOOP) continue;
                    TraceLog.get().exit(200);
                    throw new SmbException(OPERATION_FAILED, -1073741267);
                }
                catch (Exception ex) {
                    TraceLog.get().caught(ex, 200);
                    throw new ClientException(UNABLE_RECONNECT_COLON + ex.getMessage(), -111);
                }
            }
        }
        TraceLog.get().exit(info, 200);
        return info;
    }

    public static Info getInfo(Mount mount, String localPath) throws NqException {
        TraceLog.get().enter(200);
        String fullPath = null;
        Info info = null;
        try {
            info = File.getInfo(mount, localPath, fullPath);
        }
        catch (NqException e) {
            TraceLog.get().caught(e, 200);
            TraceLog.get().exit(200);
            throw e;
        }
        TraceLog.get().exit(200);
        return info;
    }

    public static void setInfo(Mount mount, String localPath, Info info) throws NqException {
        TraceLog.get().enter(200);
        try {
            File file = new File(mount, localPath);
            file.setInfo(info);
            file.close();
        }
        catch (NqException e) {
            TraceLog.get().caught(e, 200);
            TraceLog.get().exit(200);
            throw e;
        }
        TraceLog.get().exit(200);
    }

    public void setInfo(int flags, Info info) throws NqException {
        TraceLog.get().enter(200);
        if (!this.isOpen) {
            TraceLog.get().exit(200);
            throw new SmbException(FILE_NOT_OPEN, -1073741267);
        }
        if (0 != (flags & 1)) {
            TraceLog.get().message("info.attributes = " + info.attributes, 2000);
            try {
                this.execute(AbstractFile.SmbMethods.doSetFileAttributes, info.attributes);
            }
            catch (NqException e) {
                TraceLog.get().caught(e, 2000);
                TraceLog.get().exit("setInfo error executing doSetFileAttributes = ", e, 200);
                throw e;
            }
        }
        if (0 != (flags & 0x10)) {
            TraceLog.get().message("info.eof = " + info.eof, 2000);
            try {
                this.execute(AbstractFile.SmbMethods.doSetFileSize, info.eof);
            }
            catch (NqException e) {
                TraceLog.get().caught(e, 2000);
                TraceLog.get().exit("setInfo error executing doSetFileSize = ", e, 200);
                throw e;
            }
        }
        long creationTime = 0L;
        long lastAccessTime = 0L;
        long lastWriteTime = 0L;
        boolean isThereTimeUpdate = false;
        if (0 != (flags & 2)) {
            creationTime = info.creationTime;
            isThereTimeUpdate = true;
        }
        if (0 != (flags & 4)) {
            lastAccessTime = info.lastAccessTime;
            isThereTimeUpdate = true;
        }
        if (0 != (flags & 8)) {
            lastWriteTime = info.lastWriteTime;
            isThereTimeUpdate = true;
        }
        if (isThereTimeUpdate) {
            try {
                this.execute(AbstractFile.SmbMethods.doSetFileTime, creationTime, lastAccessTime, lastWriteTime);
                this.info.creationTime = creationTime;
                this.info.lastWriteTime = lastWriteTime;
                this.info.lastAccessTime = lastAccessTime;
            }
            catch (NqException e) {
                TraceLog.get().caught(e, 2000);
                TraceLog.get().exit("setInfo error executing doSetFileTime = ", e, 200);
                throw e;
            }
        }
        TraceLog.get().exit(200);
    }

    public static void setInfo(Mount mount, String localPath, int flags, Info info) throws NqException {
        TraceLog.get().enter(200);
        try {
            File file = new File(mount, localPath);
            file.setInfo(flags, info);
            file.close();
        }
        catch (NqException e) {
            TraceLog.get().caught(e, 200);
            TraceLog.get().exit(200);
            throw e;
        }
        TraceLog.get().exit(200);
    }

    public void setSecurityDescriptor(SecurityDescriptor sd) throws NqException {
        TraceLog.get().enter(200);
        if (!this.isOpen) {
            TraceLog.get().exit(200);
            throw new SmbException(FILE_NOT_OPEN, -1073741267);
        }
        if (0 == (0x40 & this.params.access)) {
            TraceLog.get().exit(200);
            throw new NqException(MISSING_ACCESS_SYSTEM_SECURITY, -20);
        }
        try {
            this.execute(AbstractFile.SmbMethods.doSetSecurityDescriptor, sd);
        }
        catch (NqException e) {
            TraceLog.get().caught(e);
            TraceLog.get().exit(200);
            throw e;
        }
        TraceLog.get().exit(200);
    }

    public SecurityDescriptor querySecurityDescriptor() throws NqException {
        TraceLog.get().enter(200);
        if (!this.isOpen) {
            TraceLog.get().exit(200);
            throw new SmbException(FILE_NOT_OPEN, -1073741267);
        }
        if (0 == (0xC0 & this.params.access) && (0 == (0x8000000 & this.params.access) || 0 == (0x20000 & this.params.access))) {
            TraceLog.get().exit(200);
            throw new NqException(MISSING_ACCESS_READ_SYSTEM_SECURITY, -20);
        }
        SecurityDescriptor sd = null;
        try {
            sd = (SecurityDescriptor)this.execute(AbstractFile.SmbMethods.doQuerySecurityDescriptor, new Object[0]);
        }
        catch (NqException e) {
            TraceLog.get().caught(e);
            TraceLog.get().exit(200);
            throw e;
        }
        TraceLog.get().exit(200);
        return sd;
    }

    public boolean isExclusiveAccess() throws NqException {
        TraceLog.get().enter(200);
        SecurityDescriptor sd = this.querySecurityDescriptor();
        int numberOfAllowed = 0;
        int numberOfExclusAllowed = 0;
        User user = this.share.user;
        String userName = user.getCredentials().getUser();
        Credentials[] creds = new Credentials[]{user.getCredentials()};
        Sid userSid = null;
        Sid adminSid = null;
        for (int j = 0; j < creds.length; ++j) {
            try {
                Lsar lsa = new Lsar(user.getServer().getName(), creds[j]);
                Dcerpc.Handle handle = lsa.openPolicy(2048);
                userSid = lsa.lookupName(handle, userName);
                adminSid = lsa.lookupName(handle, "Administrators");
                lsa.close(handle);
                lsa.close();
            }
            catch (NqException e) {
                continue;
            }
            SecurityDescriptor.Dacl dacl = sd.getDacl();
            for (SecurityDescriptor.Ace ace : dacl.aces) {
                if (!ace.allowed) continue;
                ++numberOfAllowed;
                if (!userSid.compare(ace.sid) && !adminSid.compare(ace.sid)) continue;
                ++numberOfExclusAllowed;
            }
            TraceLog.get().exit(200);
            return numberOfAllowed == numberOfExclusAllowed;
        }
        TraceLog.get().exit(200);
        return false;
    }

    public void setExclusiveAccess(boolean exclusive) throws NqException {
        TraceLog.get().enter(200);
        User user = this.share.user;
        String userName = user.getCredentials().getUser();
        Credentials[] creds = new Credentials[]{user.getCredentials()};
        Sid userSid = null;
        Sid adminSid = null;
        NqException ex = null;
        for (int j = 0; j < creds.length; ++j) {
            try {
                Lsar lsa = new Lsar(user.getServer().getName(), creds[j]);
                Dcerpc.Handle handle = lsa.openPolicy(2048);
                userSid = lsa.lookupName(handle, userName);
                adminSid = lsa.lookupName(handle, "Administrators");
                lsa.close(handle);
                lsa.close();
            }
            catch (NqException e) {
                ex = e;
                continue;
            }
            ex = null;
            SecurityDescriptor sd = this.querySecurityDescriptor();
            if (exclusive) {
                SecurityDescriptor.Ace ace2;
                for (SecurityDescriptor.Ace ace2 : sd.getDacl().aces) {
                    ace2.allowed = false;
                }
                ace2 = new SecurityDescriptor.Ace();
                ace2.sid = userSid;
                ace2.allowed = true;
                ace2.flags = 3;
                ace2.access = 0x1F01FF;
                sd.getDacl().aces.add(0, ace2);
                ace2 = new SecurityDescriptor.Ace();
                ace2.sid = adminSid;
                ace2.allowed = true;
                ace2.flags = 3;
                ace2.access = 0x1F01FF;
                sd.getDacl().aces.add(1, ace2);
                this.setSecurityDescriptor(sd);
            } else {
                SecurityDescriptor.Dacl dacl = sd.getDacl();
                Iterator it = dacl.aces.iterator();
                while (it.hasNext()) {
                    SecurityDescriptor.Ace ace = (SecurityDescriptor.Ace)it.next();
                    if (ace.allowed) {
                        it.remove();
                        continue;
                    }
                    ace.allowed = true;
                }
                this.setSecurityDescriptor(sd);
            }
            TraceLog.get().exit(200);
            return;
        }
        if (null != ex) {
            TraceLog.get().caught(ex);
            TraceLog.get().exit(200);
            throw ex;
        }
        TraceLog.get().exit(200);
    }

    protected Server getServer() {
        return this.share.user.getServer();
    }

    private void normalizePath() {
        this.setShareRelativePath(this.getLocalPathFromShare().replace('/', '\\'));
    }

    private void openOrCreate() throws NqException {
        int localRetryCount;
        TraceLog.get().enter(300);
        this.isDfsPath = this.mount.isDfs();
        if (null == this.info.share) {
            this.info.share = this.mount.share;
        }
        if (null == this.share) {
            this.share = this.mount.share;
        }
        if (null == this.server) {
            this.server = this.mount.share.getUser().getServer();
        }
        if (null != this.server.transport && !this.server.transport.isConnected() && !this.server.reconnect()) {
            throw new ClientException("Server is not connected.", -102);
        }
        this.normalizePath();
        if (null == this.share) {
            throw new ClientException("Unable to connect, check credentials", -111);
        }
        if (0 < (this.params.access & 4)) {
            this.params.access |= 2;
        }
        if (0 < (0x8000000 & this.params.access)) {
            this.accessMask = this.params.access & 0xF7FFFFFF;
            if (0 < (3 & this.params.access)) {
                this.oplockLevel = (byte)9;
            }
        } else if (0 < (this.params.access & 0xFF)) {
            this.accessMask = 0 < (0xFFFFFFBF & this.params.access) ? 0x100080 : 0;
            this.accessMask |= 0 < (0x22 & this.params.access) ? 256 : 0;
            this.accessMask |= 0 < (8 & this.params.access) ? 65536 : 0;
            this.accessMask |= 0 < (0x80 & this.params.access) ? 131072 : 0;
            this.accessMask |= 0 < (0x40 & this.params.access) ? 393344 : 0;
            this.accessMask |= 0 < (1 & this.params.access) ? 1 : 0;
            if (!this.params.isDir) {
                this.accessMask |= 0 < (2 & this.params.access) ? 2 : 0;
                this.accessMask |= 0 < (4 & this.params.access) ? 4 : 0;
                if (!this.getLocalPathFromShare().equals("")) {
                    this.oplockLevel = (byte)9;
                }
            }
        } else {
            throw new ClientException("Illegal access mode : " + this.params.access, -103);
        }
        switch (this.params.shareAccess) {
            case 4: {
                this.shareAccess = 1;
                if (0 != (this.accessMask & 2)) {
                    this.shareAccess |= 2;
                }
                if (0 == (this.accessMask & 0x10000)) break;
                this.shareAccess |= 4;
                break;
            }
            case 7: {
                this.shareAccess = 7;
                break;
            }
            case 2: {
                this.shareAccess = 6;
                break;
            }
            case 1: {
                this.shareAccess = 5;
                break;
            }
            case 3: {
                this.shareAccess = 3;
                break;
            }
            case 0: {
                this.shareAccess = 0;
                break;
            }
            default: {
                throw new ClientException("Illegal share mode : " + this.params.shareAccess, -103);
            }
        }
        this.info.attributes = 0;
        if (this.params.isDir) {
            this.info.attributes = 16;
        }
        this.disposition = this.params.disposition;
        this.createOptions = 0;
        if (this.params.isDir) {
            this.createOptions |= 1;
            this.createOptions &= 0xFFFFFFBF;
        }
        if (this.params.deleteOnClose) {
            this.createOptions |= 0x1000;
        }
        if (this.params.writeThrough) {
            this.createOptions |= 2;
        }
        if (!this.params.isDir) {
            this.createOptions = !this.params.randomAccess ? (this.createOptions |= 4) : (this.createOptions |= 0x800);
        }
        if (this.accessMask == 65664) {
            this.createOptions = 0;
        }
        this.isOpen = false;
        boolean isDfsEnabled = true;
        try {
            isDfsEnabled = Config.jnq.getBool("DFSENABLE");
        }
        catch (NqException e) {
            // empty catch block
        }
        int localTimeout = this.getRetryTimeout();
        for (int i = localRetryCount = this.getRetryCount(); i > 0; --i) {
            block61: {
                int status = 0;
                SmbException origError = null;
                try {
                    block60: {
                        Result res;
                        if (isDfsEnabled && null != (res = Dfs.findInCache(this.mount, this.share, this.getLocalPathFromShare()))) {
                            this.share = res.share;
                            this.server = res.server;
                            this.setShareRelativePath(ClientUtils.fileNameFromRemotePath(res.path, true));
                            this.info.share = this.share;
                        }
                        if (this.server.smb instanceof Smb100 && (this.share.flags & 1) > 0) {
                            String serverName = null != this.mount.serverName ? this.mount.serverName : this.mount.getInfo().getServerName();
                            String fullPath = BACKSLASH + serverName + BACKSLASH + this.share.getName();
                            try {
                                File.getInfoForSmb1(this.mount, this.getLocalPathFromShare(), fullPath);
                            }
                            catch (SmbException e) {
                                if (e.getErrCode() != -1073741225) break block60;
                                status = -1073741225;
                                origError = e;
                            }
                        }
                    }
                    if (!Dfs.isDfsError(status)) {
                        this.server.smb.doCreate(this);
                    }
                }
                catch (SmbException e) {
                    if (this.previousPath.equals(this.getLocalPathFromShare())) {
                        TraceLog.get().caught(e);
                        throw e;
                    }
                    status = e.getErrCode();
                    origError = e;
                }
                TraceLog.get().message("Results from call to doCreate(), status=" + status + "; file=", this, 2000);
                if (-1073741267 == status) {
                    this.durableHandle = new UUID();
                }
                if (isDfsEnabled && status != 0 && Dfs.isDfsError(status) && !this.share.isIpc) {
                    Dfs dfs = new Dfs(50);
                    while (--this.dfsCounter >= 0) {
                        Result dfsResult;
                        try {
                            dfsResult = dfs.resolvePath(this.mount, this.mount.getMountParams(), this.share, this.getLocalPathFromShare(), null);
                        }
                        catch (NqException e) {
                            if (-1073741225 != status) {
                                TraceLog.get().caught(origError);
                                throw origError;
                            }
                            TraceLog.get().caught(e);
                            throw new ClientException("Dfs error: " + e.getMessage(), e.getErrCode());
                        }
                        if (null != dfsResult && null != dfsResult.share) {
                            String previousShareRelativePath;
                            this.isDfsPath = true;
                            Share previousShare = this.share;
                            this.share = dfsResult.share;
                            Server previousServer = this.server;
                            this.server = dfsResult.share.getUser().getServer();
                            dfsResult.path = ClientUtils.filePathStripNull(dfsResult.path);
                            this.previousPath = previousShareRelativePath = this.getLocalPathFromShare();
                            this.setShareRelativePath(ClientUtils.fileNameFromRemotePath(dfsResult.path, true));
                            Info previousInfo = this.info;
                            this.info.path = this.getLocalPathFromShare();
                            this.info.share = this.share;
                            if (this.info.path.equals("")) {
                                this.params.access &= 0xFFFFFFFD;
                                this.params.access |= 1;
                            }
                            try {
                                this.openOrCreate();
                                if (!dfs.referral.isGood) {
                                    dfs.setIsGood(true);
                                    dfs.referral.isGood = true;
                                }
                                TraceLog.get().message("Referral used = ", dfs.referral, 2000);
                            }
                            catch (NqException nqe) {
                                TraceLog.get().message("Caught exception from openOrCreate()=", nqe, 2000);
                                if (!Dfs.isDfsError(nqe.getErrCode())) {
                                    TraceLog.get().caught(nqe);
                                    throw nqe;
                                }
                                Share.disconnectShareInternally(this.mount, this.server, this.share);
                                this.share = previousShare;
                                this.server = previousServer;
                                this.setShareRelativePath(previousShareRelativePath);
                                this.info = previousInfo;
                                dfs.setIsGood(false);
                                dfs.referral.isGood = false;
                                ++this.dfsCounter;
                                continue;
                            }
                            this.isOpen = true;
                            this.position = 0L;
                            TraceLog.get().exit(300);
                            return;
                        }
                        if (this.isDfsPath) {
                            TraceLog.get().exit(300);
                            throw new SmbException("DFS resolution failed accessing shareRelativePath " + this.getLocalPathFromShare(), status);
                        }
                        TraceLog.get().exit(300);
                        throw new SmbException("Access error for file " + this.getLocalPathFromShare(), status);
                    }
                    if (this.dfsCounter < 0) {
                        TraceLog.get().exit(300);
                        throw new ClientException("DFS resolution failed - too many attempts", status);
                    }
                }
                if (status == 0) {
                    this.isOpen = true;
                    this.position = 0 != (this.params.access & 4) ? this.info.eof : 0L;
                    this.share.files.put(this.getFid(), this);
                    this.server.smb.handleWaitingNotifyResponses(this.server, this);
                    TraceLog.get().exit(300);
                    return;
                }
                if (-1073741267 == status) {
                    try {
                        if (!this.server.reconnect()) {
                            throw new ClientException(UNABLE_RECONNECT, -111);
                        }
                        if (i == END_OF_LOOP) {
                            throw new SmbException(OPERATION_FAILED, -1073741267);
                        }
                        break block61;
                    }
                    catch (NqException e) {
                        TraceLog.get().caught(e);
                        throw e;
                    }
                }
                String msg = "Unable to create/open - ";
                if (-1073741790 == status) {
                    msg = "Access denied to ";
                }
                throw new SmbException(msg + this.getLocalPathFromShare(), status);
            }
            if (localRetryCount == i) continue;
            Utility.waitABit(localTimeout);
            localTimeout *= 2;
        }
        TraceLog.get().exit(300);
    }

    public void notifyChanges(int completionFilter, AsyncConsumer changeNotifyListener) throws NqException {
        TraceLog.get().enter(200);
        if (!this.isOpen()) {
            TraceLog.get().exit(200);
            throw new NqException("Cannot operate on a closed file object.");
        }
        if (!this.isDir()) {
            TraceLog.get().exit(200);
            throw new NqException("Cannot initiate change notifications on a file.");
        }
        try {
            this.server.smb.doChangeNotify(this, completionFilter, changeNotifyListener);
        }
        catch (NqException e) {
            TraceLog.get().caught(e, 200);
            TraceLog.get().exit(200);
            throw e;
        }
        TraceLog.get().exit(200);
    }

    public void cancelNotify() throws NqException {
        TraceLog.get().enter(200);
        if (this.isDataInAidChangeNotifyQueue()) {
            try {
                this.server.smb.doCancel(this);
            }
            catch (NqException e) {
                if (e.getErrCode() == -1073741267) {
                    TraceLog.get().error("Unable to execute cancel command", 10, 0);
                    this.server.reconnect();
                    AidObject aidObject = this.retrieveAidChangeNotifyQueueEntry();
                    if (null != aidObject) {
                        this.removeAidChangeNotifyQueueEntry(aidObject);
                        if (null != aidObject.consumer) {
                            aidObject.consumer.complete(new SmbException(267), 0L, null);
                        }
                    }
                }
                TraceLog.get().caught(e, 200);
                TraceLog.get().exit(200);
                throw e;
            }
        }
        TraceLog.get().exit(200);
    }

    protected static File checkPath(Mount mount, String path, boolean stripLast) throws NqException {
        TraceLog.get().enter(200);
        String pathToCheck = path;
        if (stripLast) {
            pathToCheck = ClientUtils.filePathStripLastComponent(path);
        }
        File file = new File(mount, pathToCheck, new Params(0x8100081, 7, 1));
        file.close();
        TraceLog.get().exit(200);
        return file;
    }

    protected Params getParams() {
        return this.params;
    }

    protected boolean isDisconnected() {
        return this.disconnected;
    }

    protected void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
        this.isOpen = false;
    }

    protected boolean restore() throws NqException {
        if (this.isDisconnected()) {
            return false;
        }
        int status = 0;
        try {
            status = (Integer)this.execute(AbstractFile.SmbMethods.doRestoreHandle, new Object[0]);
        }
        catch (NqException e) {
            status = e.getErrCode();
        }
        if (status != 0) {
            this.isOpen = false;
            return false;
        }
        this.isOpen = true;
        return true;
    }

    public String toString() {
        return "File [isDir()=" + this.isDir() + ", getShareRelativePath()=" + ClientUtils.filePathStripNull(this.getLocalPathFromShare()) + ", getLocalPath()=" + this.getLocalPath() + ", getServer()=" + this.getServer() + ", isDisconnected()=" + this.isDisconnected() + "]";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected AidObject retrieveAidChangeNotifyQueueEntry() {
        Queue queue = this.aidChangeNotifyQueue;
        synchronized (queue) {
            return (AidObject)this.aidChangeNotifyQueue.peek();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void removeAidChangeNotifyQueueEntry(AidObject aidObject) {
        Queue queue = this.aidChangeNotifyQueue;
        synchronized (queue) {
            this.aidChangeNotifyQueue.remove(aidObject);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void addToAidChangeNotifyQueue(Long entry, AsyncConsumer consumer) {
        Queue queue = this.aidChangeNotifyQueue;
        synchronized (queue) {
            this.aidChangeNotifyQueue.add(new AidObject(entry, consumer));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected boolean isDataInAidChangeNotifyQueue() {
        Queue queue = this.aidChangeNotifyQueue;
        synchronized (queue) {
            return !this.aidChangeNotifyQueue.isEmpty();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void clearAidQueue() {
        Queue queue = this.aidChangeNotifyQueue;
        synchronized (queue) {
            this.aidChangeNotifyQueue.clear();
        }
    }

    public boolean isDfs() {
        boolean isDfsEnabled = true;
        try {
            isDfsEnabled = Config.jnq.getBool("DFSENABLE");
        }
        catch (NqException nqException) {
            // empty catch block
        }
        return isDfsEnabled && this.isDfsPath;
    }

    public static boolean serverSideDataCopy(File srcFile, File dstFile, ServerSideDataCopyCallback callback) throws NqException {
        TraceLog.get().enter(200);
        if (null == srcFile || null == dstFile) {
            TraceLog.get().exit(200);
            throw new NqException("Invalid parameters: srcFile or dstFile is null", -20);
        }
        if (!srcFile.isOpen || !dstFile.isOpen) {
            TraceLog.get().exit(200);
            throw new NqException(FILE_NOT_OPEN, -20);
        }
        Server srcServer = srcFile.getServer();
        Server dstServer = dstFile.getServer();
        if (!srcServer.getName().equals(dstServer.getName())) {
            TraceLog.get().exit(200);
            throw new NqException("Source and destination server should be the same", -20);
        }
        if (srcFile.share.getName().equals(dstFile.share.getName()) && srcFile.getLocalPathFromShare().equals(dstFile.getLocalPathFromShare())) {
            TraceLog.get().exit(200);
            throw new NqException("Source and destination path can't be the same", -20);
        }
        ResumeKey key = null;
        for (int i = srcFile.mount.getRetryCount(); i > 0; --i) {
            try {
                key = srcServer.smb.doQueryResumeFileKey(srcFile);
                break;
            }
            catch (NqException e) {
                if (-1073741637 == e.getErrCode() || -1073741808 == e.getErrCode() || -110 == e.getErrCode()) {
                    TraceLog.get().caught(e);
                    TraceLog.get().exit("The server does not support server-side data copy, ", e, 300);
                    throw new ClientException(NOT_SUPPORT_SERVER_SIDE_COPY, -110);
                }
                if (-1073741267 != e.getErrCode()) {
                    TraceLog.get().caught(e);
                    TraceLog.get().exit("Unable to request the resume file key, ", e, 200);
                    throw e;
                }
                try {
                    if (!srcServer.reconnect()) {
                        TraceLog.get().exit(200);
                        throw new ClientException(UNABLE_RECONNECT, -111);
                    }
                    if (i != END_OF_LOOP) continue;
                    TraceLog.get().exit(200);
                    throw new SmbException(OPERATION_FAILED, -1073741267);
                }
                catch (Exception ex) {
                    TraceLog.get().caught(ex, 200);
                    TraceLog.get().exit(UNABLE_RECONNECT_COLON, ex, 200);
                    throw new ClientException(UNABLE_RECONNECT_COLON + ex.getMessage(), -111);
                }
            }
        }
        boolean res = false;
        CopyChunkThreadBody copyChunkThreadBody = new CopyChunkThreadBody(srcFile, dstFile, key, callback);
        if (null != callback) {
            new Thread((Runnable)copyChunkThreadBody, "copyChunkThread").start();
            res = true;
        } else {
            res = copyChunkThreadBody.copyChunkOperations();
        }
        TraceLog.get().exit(200);
        return res;
    }

    public boolean serverSideDataCopy(File dstFile, ServerSideDataCopyCallback callback) throws NqException {
        TraceLog.get().enter(200);
        boolean res = File.serverSideDataCopy(this, dstFile, callback);
        TraceLog.get().exit(200);
        return res;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean serverSideDataCopy(Mount mount, String srcLocalPath, String dstLocalPath) throws NqException {
        TraceLog.get().enter("srcLocalPath=" + srcLocalPath + ", dstLocalPath=" + dstLocalPath, 200);
        if (null == mount) {
            TraceLog.get().exit(200);
            throw new NqException("Invalid parameter: mount is null", -20);
        }
        if (null == srcLocalPath || null == dstLocalPath) {
            TraceLog.get().exit(200);
            throw new NqException("Invalid file name", -20);
        }
        if (srcLocalPath.contains(SLASH)) {
            srcLocalPath = srcLocalPath.replace(SLASH, BACKSLASH);
        }
        if (dstLocalPath.contains(SLASH)) {
            dstLocalPath = dstLocalPath.replace(SLASH, BACKSLASH);
        }
        if (srcLocalPath.equals(dstLocalPath)) {
            TraceLog.get().exit(200);
            throw new NqException("Source and destination path can't be the same", -20);
        }
        File srcFile = null;
        File dstFile = null;
        boolean res = false;
        try {
            try {
                srcFile = new File(mount, srcLocalPath, new Params(1, 7, 1));
                dstFile = new File(mount, dstLocalPath, new Params(3, 7, 3));
            }
            catch (NqException e) {
                TraceLog.get().caught(e, 200);
                TraceLog.get().exit(200);
                throw e;
            }
            res = File.serverSideDataCopy(srcFile, dstFile, null);
        }
        finally {
            if (null != srcFile) {
                srcFile.close();
            }
            if (null != dstFile) {
                dstFile.close();
            }
        }
        TraceLog.get().exit(200);
        return res;
    }

    public static void mkdirs(Mount mount, String localPath) throws NqException {
        TraceLog.get().enter(200);
        if (null == mount || null == localPath || 0 == localPath.length()) {
            TraceLog.get().exit("Failed due to null argument or zero length path", 200);
            throw new NqException("Failed due to null argument or zero length path", -20);
        }
        if (localPath.startsWith(SLASH) || localPath.startsWith(BACKSLASH)) {
            String originalPath = localPath;
            if ((localPath = localPath.substring(1)).startsWith(SLASH) || localPath.startsWith(BACKSLASH)) {
                TraceLog.get().exit("Local path must not start with slashes, path: " + originalPath, 200);
                throw new NqException("Local path must not start with slashes, path: " + originalPath, -20);
            }
            TraceLog.get().message("Ignoring leading slash since it forbidden for local paths", 200);
        }
        if (localPath.endsWith(SLASH) || localPath.endsWith(BACKSLASH)) {
            localPath = localPath.substring(0, localPath.length() - 1);
        }
        localPath = localPath.replace('/', '\\');
        int currentIndex = 0;
        Params dirParams = new Params(3, 7, 3, true);
        while (-1 != currentIndex) {
            File dir;
            currentIndex = localPath.indexOf(92, currentIndex + 1);
            try {
                dir = new File(mount, -1 == currentIndex ? localPath : localPath.substring(0, currentIndex), dirParams);
            }
            catch (NqException e) {
                TraceLog.get().caught(e, 200);
                TraceLog.get().exit("Failed to open or create " + (-1 == currentIndex ? localPath : localPath.substring(0, currentIndex)) + " due to: " + e.getMessage() + ". Error code: " + e.getErrCode(), 200);
                throw e;
            }
            dir.close();
        }
        TraceLog.get().exit(200);
    }

    public static interface ServerSideDataCopyCallback {
        public void callback(Throwable var1, long var2, int var4);
    }

    protected static class ChunksStatus {
        protected int status;
        protected int chunksWritten;
        protected int chunkBytesWritten;
        protected int totalBytesWritten;

        protected ChunksStatus() {
        }
    }

    protected static class Chunk {
        protected long sourceOffset;
        protected long targetOffset;
        protected int length;

        protected Chunk(long sourceOffset, long targetOffset, int length) {
            this.sourceOffset = sourceOffset;
            this.targetOffset = targetOffset;
            this.length = length;
        }
    }

    protected static class ResumeKey {
        protected byte[] key = new byte[24];

        protected ResumeKey() {
        }
    }

    private static class CopyChunkThreadBody
    implements Runnable {
        private File srcFile;
        private File dstFile;
        private ResumeKey key;
        private ServerSideDataCopyCallback callback;
        private ChunksStatus chunksStatus;
        private long srcEof;

        public CopyChunkThreadBody(File srcFile, File dstFile, ResumeKey key, ServerSideDataCopyCallback callback) {
            this.srcFile = srcFile;
            this.dstFile = dstFile;
            this.key = key;
            this.callback = callback;
        }

        public void run() {
            TraceLog.get().enter(300);
            try {
                this.copyChunkOperations();
            }
            catch (Exception e) {
                if (null != this.callback) {
                    this.callback.callback(e, this.srcEof, null == this.chunksStatus ? 0 : this.chunksStatus.totalBytesWritten);
                }
                TraceLog.get().error("Exception caught in copy chunks operation; ex = ", e, 300, 0);
                TraceLog.get().exit("Exception caught in copy chunks operation", e, 300);
                return;
            }
            TraceLog.get().exit(300);
        }

        public boolean copyChunkOperations() throws NqException {
            TraceLog.get().enter(300);
            boolean readAccess = (this.dstFile.accessMask & 1) > 0;
            this.srcEof = this.srcFile.getInfo().getEof();
            Info info = this.dstFile.getInfo();
            info.setEof(this.srcEof);
            this.dstFile.setInfo(16, info);
            long offset = 0L;
            int maxChunksNum = 16;
            int maxChunkCopySize = 0x100000;
            boolean firstStatusInvalidParameter = false;
            if (0L != this.srcEof) {
                block4: while (this.srcEof > offset) {
                    Chunk[] chunks = this.createChunkArray(offset, this.srcEof, maxChunksNum, maxChunkCopySize);
                    for (int i = this.srcFile.mount.getRetryCount(); i > 0; --i) {
                        try {
                            this.chunksStatus = this.dstFile.server.smb.doServerSideDataCopy(this.dstFile, readAccess, this.key, chunks);
                            if (null == this.chunksStatus) {
                                throw new SmbException(File.OPERATION_FAILED, -1073741267);
                            }
                            if (-1073741811 == this.chunksStatus.status) {
                                if (!firstStatusInvalidParameter && 0 != this.chunksStatus.chunksWritten) {
                                    maxChunksNum = this.chunksStatus.totalBytesWritten / this.chunksStatus.chunkBytesWritten;
                                    maxChunkCopySize = this.chunksStatus.chunkBytesWritten;
                                    firstStatusInvalidParameter = true;
                                    continue block4;
                                }
                                String srcShare = this.srcFile.share.getName();
                                String dstShare = this.dstFile.share.getName();
                                if (0 == this.chunksStatus.chunksWritten && !srcShare.equals(dstShare)) {
                                    throw new ClientException(File.NOT_SUPPORT_COPY_WITH_DIFFERENT_SHARES, -110);
                                }
                                throw new SmbException(File.OPERATION_FAILED, -1073741811);
                            }
                            if (null != this.callback) {
                                this.callback.callback(null, this.srcEof, this.chunksStatus.totalBytesWritten);
                            }
                            offset += (long)this.chunksStatus.totalBytesWritten;
                            continue block4;
                        }
                        catch (NqException e) {
                            if (-1073741772 == e.getErrCode()) {
                                TraceLog.get().caught(e);
                                TraceLog.get().exit("The server does not support server-side data copy with different shares, ", e, 300);
                                throw new ClientException(File.NOT_SUPPORT_COPY_WITH_DIFFERENT_SHARES, -110);
                            }
                            if (-1073741267 != e.getErrCode()) {
                                TraceLog.get().caught(e);
                                TraceLog.get().exit("Unable to request server-side copy, ", e, 300);
                                throw e;
                            }
                            try {
                                if (!this.dstFile.server.reconnect()) {
                                    TraceLog.get().exit(300);
                                    throw new ClientException(File.UNABLE_RECONNECT, -111);
                                }
                                if (i != AbstractFile.END_OF_LOOP) continue;
                                TraceLog.get().exit(300);
                                throw new SmbException(File.OPERATION_FAILED, -1073741267);
                            }
                            catch (Exception ex) {
                                TraceLog.get().caught(ex, 300);
                                TraceLog.get().exit(File.UNABLE_RECONNECT_COLON, ex, 300);
                                throw new ClientException(File.UNABLE_RECONNECT_COLON + ex.getMessage(), -111);
                            }
                        }
                    }
                }
            } else if (null != this.callback) {
                this.callback.callback(null, this.srcEof, 0);
            }
            TraceLog.get().exit(300);
            return 0L != this.srcEof ? (null == this.chunksStatus ? false : 0 == this.chunksStatus.status) : true;
        }

        private Chunk[] createChunkArray(long startOffset, long eof, int maxChunksNum, int maxChunkCopySize) {
            long bytesLeftToCopy = eof - startOffset;
            int chunksNum = (int)Math.ceil((double)bytesLeftToCopy / (double)maxChunkCopySize);
            chunksNum = chunksNum > maxChunksNum ? maxChunksNum : chunksNum;
            Chunk[] chunks = new Chunk[chunksNum];
            long offset = startOffset;
            for (int i = 0; i < chunksNum; ++i) {
                long newOffsetAfterCopyOneChunk = offset + (long)maxChunkCopySize;
                int chunkCopySize = newOffsetAfterCopyOneChunk > eof ? (int)(eof - offset) : maxChunkCopySize;
                chunks[i] = new Chunk(offset, offset, chunkCopySize);
                offset += (long)chunkCopySize;
            }
            return chunks;
        }
    }

    class AidObject {
        long aid;
        AsyncConsumer consumer;

        public AidObject(long aid, AsyncConsumer consumer) {
            this.aid = aid;
            this.consumer = consumer;
        }

        public int hashCode() {
            int prime = 31;
            int result = 1;
            result = 31 * result + (int)(this.aid ^ this.aid >>> 32);
            return result;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            AidObject other = (AidObject)obj;
            return this.aid == other.aid;
        }
    }

    protected class InternalSync
    implements AsyncConsumer {
        public boolean timedOut = true;
        public long length;
        public Throwable status;
        public final SyncObject syncObj = new SyncObject();

        protected InternalSync() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void complete(Throwable status, long length, Object context) {
            TraceLog.get().enter("length = " + length + "; status = " + status, 300);
            this.timedOut = false;
            InternalSync internalSync = this;
            synchronized (internalSync) {
                this.status = status;
                this.length = 0L == length && status instanceof SmbException && -1073741807 == ((SmbException)status).getErrCode() ? -1L : length;
            }
            this.syncObj.syncNotify();
            TraceLog.get().exit("this.length = " + this.length, 300);
        }
    }

    protected class CummulativeAsynConsumer
    implements AsyncConsumer {
        public long length;
        public long actualBytes = 0L;
        public long numRequests = 0L;
        public long numResponses = 0L;
        public long totalBytes = 0L;
        public Buffer data;
        public Throwable status;
        public AsyncConsumer parentConsumer;
        public Object parentContext;

        public CummulativeAsynConsumer(AsyncConsumer parentConsumer, Object parentContext, Buffer data) {
            this.parentConsumer = parentConsumer;
            this.parentContext = parentContext;
            this.data = data;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void complete(Throwable status, long length, Object context) throws NqException {
            TraceLog.get().enter("length = " + length + "; status = " + status, 2000);
            CummulativeAsynConsumer cummulativeAsynConsumer = this;
            synchronized (cummulativeAsynConsumer) {
                this.status = status;
                this.length = length;
                this.actualBytes += length;
                ++this.numResponses;
                if (status == null && this.numRequests == this.numResponses) {
                    this.parentConsumer.complete(status, this.actualBytes, this.parentContext);
                } else if (status instanceof SmbException) {
                    SmbException smbStatus = (SmbException)status;
                    int code = smbStatus.getErrCode();
                    if (0 == code) {
                        this.status = null;
                        if (this.numRequests == this.numResponses) {
                            this.parentConsumer.complete(null, this.actualBytes, this.parentContext);
                        }
                    } else if (-1073741267 == code) {
                        TraceLog.get().message("length=" + length + "; status=" + status, 2000);
                        this.parentConsumer.complete(status, length, context);
                    } else if (259 == code) {
                        --this.numResponses;
                        TraceLog.get().message("length=" + length + "; status=" + status, 2000);
                        this.parentConsumer.complete(status, length, context);
                    } else if (this.numRequests == this.numResponses) {
                        TraceLog.get().message("actualBytes=" + this.actualBytes + "; status=" + status, 2000);
                        this.parentConsumer.complete(status, this.actualBytes, this.parentContext);
                    }
                } else if (status instanceof ClientException && -106 == ((ClientException)status).getErrCode()) {
                    TraceLog.get().error("Got ClientException.ERR_IO send STATUS_RETRY", 10, -106);
                    this.parentConsumer.complete(new SmbException(-1073741267), length, context);
                } else {
                    TraceLog.get().message("actualBytes=" + this.actualBytes + "; status=" + status, 2000);
                    this.parentConsumer.complete(status, this.actualBytes, this.parentContext);
                }
            }
            TraceLog.get().exit(300);
        }
    }

    public static class Info {
        @Deprecated
        public int attributes;
        @Deprecated
        public long creationTime;
        @Deprecated
        public long lastAccessTime;
        @Deprecated
        public long lastWriteTime;
        @Deprecated
        public long changeTime;
        @Deprecated
        public long eof;
        @Deprecated
        public long allocationSize;
        @Deprecated
        public int numberOfLinks;
        @Deprecated
        public long fileIndex;
        @Deprecated
        public String path;
        protected Share share;

        public boolean isDirectory() {
            return (this.attributes & 0x10) > 0;
        }

        public boolean isReadOnly() {
            return (this.attributes & 1) > 0;
        }

        public boolean isHidden() {
            return (this.attributes & 2) > 0;
        }

        public boolean isSystemFile() {
            return (this.attributes & 4) > 0;
        }

        public boolean isArchive() {
            return (this.attributes & 0x20) > 0;
        }

        public Date getCreationTime() {
            return new Date(TimeUtility.timeToUtcMillisec(this.creationTime));
        }

        public long getCreationTimeRaw() {
            return this.creationTime;
        }

        public Date getLastAccessTime() {
            return new Date(TimeUtility.timeToUtcMillisec(this.lastAccessTime));
        }

        public long getLastAccessTimeRaw() {
            return this.lastAccessTime;
        }

        public Date getLastWriteTime() {
            return new Date(TimeUtility.timeToUtcMillisec(this.lastWriteTime));
        }

        public long getLastWriteTimeRaw() {
            return this.lastWriteTime;
        }

        public Date getChangeTime() {
            return new Date(TimeUtility.timeToUtcMillisec(this.changeTime));
        }

        public long getChangeTimeRaw() {
            return this.changeTime;
        }

        public int getAttributes() {
            return this.attributes;
        }

        public long getEof() {
            return this.eof;
        }

        public long getAllocationSize() {
            return this.allocationSize;
        }

        public int getNumberOfLinks() {
            return this.numberOfLinks;
        }

        public long getFileIndex() {
            return this.fileIndex;
        }

        public String getPath() {
            return this.path;
        }

        public void setAttributes(int attributes) {
            this.attributes = attributes;
        }

        public void setCreationTime(long creationTime) {
            this.creationTime = creationTime;
        }

        public void setLastAccessTime(long lastAccessTime) {
            this.lastAccessTime = lastAccessTime;
        }

        public void setLastWriteTime(long lastWriteTime) {
            this.lastWriteTime = lastWriteTime;
        }

        public void setChangeTime(long changeTime) {
            this.changeTime = changeTime;
        }

        public void setEof(long eof) {
            this.eof = eof;
        }

        public void setAllocationSize(long allocationSize) {
            this.allocationSize = allocationSize;
        }

        public void setNumberOfLinks(int numberOfLinks) {
            this.numberOfLinks = numberOfLinks;
        }

        public void setFileIndex(long fileIndex) {
            this.fileIndex = fileIndex;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public void setShare(Share share) {
            this.share = share;
        }

        public String toString() {
            return "Info [attributes=" + this.attributes + ", creationTime=" + this.creationTime + ", lastAccessTime=" + this.lastAccessTime + ", lastWriteTime=" + this.lastWriteTime + ", changeTime=" + this.changeTime + ", eof=" + this.eof + ", allocationSize=" + this.allocationSize + ", numberOfLinks=" + this.numberOfLinks + ", fileIndex=" + this.fileIndex + ", path=" + this.path + ", share=" + this.share + "]";
        }
    }

    public static class Params {
        public int access = 16;
        public int shareAccess = 1;
        public int disposition = 1;
        public boolean isDir = false;
        public boolean writeThrough = false;
        public boolean randomAccess = true;
        public boolean deleteOnClose = false;

        public Params(int access, int shareAccess, int disposition) {
            this.access = access;
            this.shareAccess = shareAccess;
            this.disposition = disposition;
        }

        public Params(int access, int shareAccess, int disposition, boolean directory) {
            this.access = access;
            this.shareAccess = shareAccess;
            this.disposition = disposition;
            this.isDir = directory;
        }

        public Params(int access, int shareAccess, int disposition, boolean directory, boolean deleteOnClose) {
            this(access, shareAccess, disposition, directory);
            this.deleteOnClose = deleteOnClose;
        }

        public Params() {
        }
    }
}

