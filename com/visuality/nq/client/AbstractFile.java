/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.client.ClientException;
import com.visuality.nq.client.Directory;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Mount;
import com.visuality.nq.client.Server;
import com.visuality.nq.client.Share;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SecurityDescriptor;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Utility;

public class AbstractFile {
    protected Mount mount;
    protected Share share;
    protected Server server;
    protected int retryCount = -1;
    protected int retryTimeout = -1;
    protected ObjectType objectType;
    static int END_OF_LOOP = 1;

    public AbstractFile() throws NqException {
        this(null);
    }

    public AbstractFile(Mount mount) throws NqException {
        this.mount = mount;
        if (mount != null) {
            this.checkMountConnection();
            this.mount = mount.clone();
            this.share = this.mount.getShare();
            this.server = this.share.getUser().getServer();
            this.retryCount = mount.getRetryCount();
            this.retryTimeout = mount.getRetryTimeout();
        }
    }

    public Object execute(SmbMethods smbMethod, Object ... args) throws NqException {
        return this.connect(smbMethod, args);
    }

    public Object connect(SmbMethods smbMethod, Object ... args) throws NqException {
        int localRetryCount;
        TraceLog.get().enter(200);
        int status = 0;
        int localRetryTimeout = this.retryTimeout;
        for (int counter = localRetryCount = this.retryCount; counter > 0; --counter) {
            try {
                switch (smbMethod) {
                    case doClose: {
                        this.objectType = ObjectType.file;
                        status = this.server.smb.doClose((File)this);
                        TraceLog.get().exit(200);
                        return status;
                    }
                    case doFindClose: {
                        this.objectType = ObjectType.directory;
                        status = this.server.smb.doFindClose((Directory)this);
                        TraceLog.get().exit(200);
                        return status;
                    }
                    case doFindMore: {
                        this.objectType = ObjectType.directory;
                        boolean result = this.server.smb.doFindMore((Directory)this);
                        TraceLog.get().exit(200);
                        return result;
                    }
                    case doFlush: {
                        this.objectType = ObjectType.file;
                        this.server.smb.doFlush((File)this);
                        TraceLog.get().exit(200);
                        return status;
                    }
                    case doQueryFileInfoByHandle: {
                        this.objectType = ObjectType.file;
                        File.Info info = this.server.smb.doQueryFileInfoByHandle((File)this);
                        TraceLog.get().exit(200);
                        return info;
                    }
                    case doQuerySecurityDescriptor: {
                        this.objectType = ObjectType.file;
                        SecurityDescriptor sd1 = this.server.smb.doQuerySecurityDescriptor((File)this);
                        TraceLog.get().exit(200);
                        return sd1;
                    }
                    case doRestoreHandle: {
                        this.objectType = ObjectType.file;
                        if (((File)this).params.isDir) {
                            this.server.smb.doCreate((File)this);
                        } else {
                            this.server.smb.doRestoreHandle((File)this);
                        }
                        TraceLog.get().exit(200);
                        return status;
                    }
                    case doSetFileAttributes: {
                        int attributes = (Integer)args[0];
                        this.objectType = ObjectType.file;
                        this.server.smb.doSetFileAttributes((File)this, attributes);
                        TraceLog.get().exit(200);
                        return status;
                    }
                    case doSetFileDeleteOnClose: {
                        this.objectType = ObjectType.file;
                        this.server.smb.doSetFileDeleteOnClose((File)this);
                        TraceLog.get().exit(200);
                        return status;
                    }
                    case doSetFileSize: {
                        long offset = (Long)args[0];
                        this.objectType = ObjectType.file;
                        this.server.smb.doSetFileSize((File)this, offset);
                        TraceLog.get().exit(200);
                        return status;
                    }
                    case doSetFileTime: {
                        long creationTime = (Long)args[0];
                        long lastAccessTime = (Long)args[1];
                        long lastWriteTime = (Long)args[2];
                        this.objectType = ObjectType.file;
                        this.server.smb.doSetFileTime((File)this, creationTime, lastAccessTime, lastWriteTime);
                        TraceLog.get().exit(200);
                        return status;
                    }
                    case doSetSecurityDescriptor: {
                        SecurityDescriptor sd2 = (SecurityDescriptor)args[0];
                        this.objectType = ObjectType.file;
                        this.server.smb.doSetSecurityDescriptor((File)this, sd2);
                        TraceLog.get().exit(200);
                        return null;
                    }
                }
                throw new NqException("Undefined SMB method.");
            }
            catch (NqException e) {
                status = e.getErrCode();
                if (SmbMethods.doClose == smbMethod || SmbMethods.doFindClose == smbMethod) {
                    TraceLog.get().message("Exception during close: ", e, 2000);
                    TraceLog.get().exit(200);
                    return status;
                }
                if (-1073741267 == status) {
                    try {
                        if (this.didntReconnect() && !this.isOpen()) {
                            TraceLog.get().caught(e);
                            throw e;
                        }
                    }
                    catch (NqException e1) {
                        TraceLog.get().caught(e1);
                        throw e1;
                    }
                    if (counter == END_OF_LOOP) {
                        TraceLog.get().exit(200);
                        throw new ClientException("Server did not respond", -111);
                    }
                    if (counter == localRetryCount) continue;
                    Utility.waitABit(localRetryTimeout);
                    localRetryTimeout *= 2;
                    continue;
                }
                TraceLog.get().caught(e);
                throw e;
            }
        }
        if (status != 0) {
            TraceLog.get().exit(200);
            throw new ClientException("Unable to reconnect via client " + (Object)((Object)smbMethod) + "; " + -111);
        }
        TraceLog.get().exit(200);
        return status;
    }

    private boolean didntReconnect() throws NqException {
        boolean reconnected = this.server.reconnect();
        if (ObjectType.directory == this.objectType) {
            return !reconnected;
        }
        boolean isOpened = this.isOpen();
        return !reconnected || !isOpened;
    }

    private boolean isOpen() {
        if (ObjectType.directory == this.objectType) {
            return true;
        }
        return ((File)this).isOpen();
    }

    public int getRetryCount() {
        return this.retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getRetryTimeout() {
        return this.retryTimeout;
    }

    public void setRetryTimeout(int retryTimeout) {
        this.retryTimeout = retryTimeout;
    }

    protected void checkMountConnection() throws ClientException {
        AbstractFile.checkMountConnection(this.mount);
    }

    protected static void checkMountConnection(Mount mount) throws ClientException {
        TraceLog.get().enter(300);
        if (!mount.getIsConnected()) {
            TraceLog.get().exit(300);
            throw new ClientException("Mount is not connected.", -101);
        }
        TraceLog.get().exit(300);
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    static enum ObjectType {
        file,
        directory;

    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    static enum SmbMethods {
        doClose,
        doFindClose,
        doFindMore,
        doFlush,
        doQueryFileInfoByHandle,
        doQuerySecurityDescriptor,
        doRestoreHandle,
        doSetFileAttributes,
        doSetFileDeleteOnClose,
        doSetFileSize,
        doSetFileTime,
        doSetSecurityDescriptor;

    }
}

