/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.client.AbstractFile;
import com.visuality.nq.client.ClientException;
import com.visuality.nq.client.ClientUtils;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Mount;
import com.visuality.nq.client.Share;
import com.visuality.nq.client.Smb100;
import com.visuality.nq.client.dfs.Dfs;
import com.visuality.nq.client.dfs.Result;
import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SmbException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Utility;
import com.visuality.nq.config.Config;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Directory
extends AbstractFile {
    private static final String FILE_CLOSED = "File is closed";
    private static final String NO_CONNECTION = "No connection";
    private boolean isOpened = false;
    private BufferReader reader = null;
    private int nextCount = -1;
    protected byte[] fid = new byte[16];
    private static final String BACKSLASH = "\\";
    private boolean isDfs = false;
    private boolean isGoodData = false;
    private ArrayList<String> firstNames = new ArrayList();
    private boolean isFirstEntry = false;
    protected Entry entry;
    protected String wildcards = null;
    protected String path;
    protected Object context;
    private String localPath;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Directory(Mount mount, String localPath) throws NqException {
        super(mount);
        int retryCount;
        TraceLog.get().enter(200);
        if (null == mount || null == localPath) {
            if (null != mount) {
                this.mount.close();
            }
            TraceLog.get().exit("Null argument is invalid.", 200);
            throw new NqException("Null argument is invalid.", -20);
        }
        if (localPath.startsWith("/") || localPath.startsWith(BACKSLASH) || localPath.endsWith("/") || localPath.endsWith(BACKSLASH)) {
            this.mount.close();
            TraceLog.get().exit("Illegal path - " + localPath + " - start or end with \"slash\" is not allowed", 200);
            throw new ClientException("Illegal path - " + localPath + " - start or end with \"slash\" is not allowed", -103);
        }
        this.path = this.localPath = (localPath = localPath.replace('/', '\\'));
        String relative = mount.getRelativeShareName();
        if (this.server.smb instanceof Smb100 && (this.share.flags & 1) > 0) {
            String serverName = null != mount.serverName ? mount.serverName : mount.getInfo().getServerName();
            this.path = serverName + BACKSLASH + this.share.getName() + BACKSLASH + localPath;
        } else {
            relative = relative.equals("") ? relative : (localPath.equals("") ? relative : relative + BACKSLASH);
            this.path = relative + localPath;
        }
        String originalPath = this.path;
        this.wildcards = "*";
        int wildcardIdAsterix = this.path.indexOf(42);
        int wildcardIdQuestion = this.path.indexOf(63);
        if (wildcardIdAsterix != -1 || wildcardIdQuestion != -1) {
            int lastSlash = localPath.lastIndexOf(92);
            if (-1 == lastSlash) {
                lastSlash = 0;
                this.wildcards = localPath;
            } else {
                this.wildcards = localPath.substring(lastSlash + 1);
            }
            localPath = this.path = localPath.substring(0, lastSlash);
        }
        int status = 0;
        int localTimeout = mount.getRetryTimeout();
        boolean isDfsEnabled = true;
        try {
            isDfsEnabled = Config.jnq.getBool("DFSENABLE");
        }
        catch (NqException e) {
            // empty catch block
        }
        NqException originalException = null;
        for (int counter = retryCount = mount.getRetryCount(); counter > 0; --counter) {
            boolean closeMount;
            status = 0;
            File.Params pr = new File.Params(0x8100081, 1, 1, true);
            File file = null;
            try {
                Result res;
                block44: {
                    try {
                        file = new File(this.mount, localPath, pr);
                    }
                    catch (SmbException smbe) {
                        if (smbe.getErrCode() == -1073741565) {
                            localPath = this.handleStatusNotADirectory(localPath);
                            file = new File(this.mount, localPath, pr);
                        }
                        if (smbe.getErrCode() == -1073741772) {
                            localPath = localPath + "\\HEAD";
                            try {
                                file = new File(this.mount, localPath, pr);
                            }
                            catch (SmbException smbe1) {
                                if (smbe1.getErrCode() == -1073741565) {
                                    localPath = localPath.substring(0, localPath.lastIndexOf(BACKSLASH));
                                    localPath = this.handleStatusNotADirectory(localPath);
                                    file = new File(this.mount, localPath, pr);
                                    break block44;
                                }
                                TraceLog.get().caught(smbe, 200);
                                throw smbe;
                            }
                        }
                        TraceLog.get().caught(smbe, 200);
                        throw smbe;
                    }
                }
                this.setIsDfs(file.isDfs());
                this.mount.share = file.share;
                this.mount.shareName = file.share.getName();
                this.mount.relativeSharePath = file.getLocalPathFromShare();
                this.mount.server = file.share.getUser().getServer();
                this.mount.serverName = file.share.getUser().getServer().getName();
                this.share = file.share;
                this.server = this.share.getUser().getServer();
                if (this.server.smb instanceof Smb100 && (this.share.flags & 1) > 0) {
                    String serverName = null != this.mount.serverName ? this.mount.serverName : this.mount.getInfo().getServerName();
                    this.path = serverName + BACKSLASH + this.share.getName() + BACKSLASH + file.getLocalPathFromShare();
                } else {
                    this.path = file.getLocalPathFromShare();
                }
                System.arraycopy(file.fid, 0, this.fid, 0, 16);
                file.close();
                if (isDfsEnabled && null != (res = Dfs.findInCache(mount, this.share, localPath))) {
                    this.mount.share = res.share;
                    this.mount.server = res.server;
                    localPath = ClientUtils.fileNameFromRemotePath(res.path, true);
                }
            }
            catch (NqException e) {
                originalException = e;
                status = e.getErrCode();
            }
            if (0 != status) {
                block45: {
                    closeMount = true;
                    if (-1073741267 == status) {
                        try {
                            localTimeout = this.handleStatusRetry(counter, retryCount, localTimeout, localPath);
                            closeMount = false;
                            continue;
                        }
                        catch (NqException e) {
                            TraceLog.get().caught(e, 200);
                            TraceLog.get().exit("Unable to connect to " + localPath + ", ", e, 200);
                            throw e;
                        }
                    }
                    if (-1073741790 != status) break block45;
                    TraceLog.get().exit("Access denied: '" + localPath + "'", 200);
                    throw new ClientException("Access denied: '" + localPath + "'", -105);
                }
                if (originalException instanceof NqException) {
                    TraceLog.get().exit("Exception = ", originalException, 200);
                    throw originalException;
                }
                TraceLog.get().exit("Unable to connect: " + status, 200);
                throw new ClientException("Unable to connect: " + status, -111);
                finally {
                    if (closeMount) {
                        this.mount.close();
                    }
                }
            }
            try {
                this.server.smb.doFindOpen(this);
            }
            catch (NqException e) {
                status = e.getErrCode();
            }
            if (0 == status) break;
            closeMount = true;
            try {
                if (-1073741267 == status) {
                    try {
                        localTimeout = this.handleStatusRetry(counter, retryCount, localTimeout, localPath);
                        closeMount = false;
                        continue;
                    }
                    catch (NqException e) {
                        TraceLog.get().caught(e, 200);
                        TraceLog.get().exit("Unable to connect to " + localPath + ", ", e, 200);
                        throw e;
                    }
                }
                if (-1073741790 == status) {
                    TraceLog.get().exit("Access denied: '" + localPath + "'", 200);
                    throw new ClientException("Access denied: '" + localPath + "'", -105);
                }
                if (-1073741772 == status) {
                    TraceLog.get().exit("Object not found: '" + localPath + "'", 200);
                    throw new SmbException("Object not found: '" + localPath + "'", -1073741772);
                }
                TraceLog.get().exit("Unable to resolve search path: " + originalPath, 200);
                throw new ClientException("Unable to resolve search path: " + originalPath, status);
            }
            finally {
                if (closeMount) {
                    this.mount.close();
                }
            }
        }
        this.isOpened = true;
        TraceLog.get().exit(200);
    }

    private int handleStatusRetry(int loopCounter, int retryCount, int localTimeout, String localPath) throws NqException {
        TraceLog.get().enter(300);
        try {
            if (END_OF_LOOP == loopCounter) {
                throw new SmbException("Operation failed connecting to " + localPath, -1073741267);
            }
            if (!this.server.reconnect()) {
                throw new ClientException("Unable to reconnect to " + localPath, -102);
            }
        }
        catch (Exception e) {
            TraceLog.get().caught(e, 300);
            throw new ClientException("Unable to connect to " + localPath + ", " + e.getMessage(), -102);
        }
        if (loopCounter != retryCount) {
            TraceLog.get().message("Caught STATUS_RETRY, waiting " + localTimeout, 2000);
            Utility.waitABit(localTimeout);
            localTimeout *= 2;
        }
        TraceLog.get().exit(300);
        return localTimeout;
    }

    private String handleStatusNotADirectory(String localPath) {
        TraceLog.get().enter("localPath = ", localPath, 300);
        int slashPtr = localPath.lastIndexOf(BACKSLASH);
        if (-1 == slashPtr) {
            this.wildcards = localPath;
            localPath = "";
        } else {
            this.wildcards = localPath.substring(slashPtr + 1);
            localPath = localPath.substring(0, slashPtr);
        }
        TraceLog.get().exit(300);
        return localPath;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean close() throws NqException {
        TraceLog.get().enter(200);
        if (!this.isOpened) {
            TraceLog.get().exit(200);
            throw new NqException("Directory is not open", -23);
        }
        try {
            if (!this.server.transport.isConnected()) {
                TraceLog.get().exit(200);
                throw new ClientException("File is closed due to No connection", -111);
            }
            try {
                this.execute(AbstractFile.SmbMethods.doFindClose, new Object[0]);
            }
            catch (NqException e) {
                TraceLog.get().caught(e);
                TraceLog.get().exit(200);
                throw e;
            }
        }
        finally {
            this.firstNames.clear();
            this.isOpened = false;
            this.mount.close();
        }
        TraceLog.get().exit(200);
        return true;
    }

    public Entry next() throws NqException {
        TraceLog.get().enter(200);
        if (!this.isOpened) {
            TraceLog.get().exit(200);
            throw new NqException("Directory is not open", -23);
        }
        try {
            if (!this.server.transport.isConnected() && !this.server.reconnect()) {
                TraceLog.get().exit(200);
                throw new ClientException("Unable to reconnect", -111);
            }
        }
        catch (Exception ex) {
            TraceLog.get().caught(ex, 200);
            TraceLog.get().exit(200);
            throw new ClientException("Unable to reconnect: " + ex.getMessage(), -111);
        }
        boolean continueLoop = true;
        int countOfDotEntries = 0;
        while (continueLoop) {
            int localTimeout = this.getRetryTimeout();
            int retryCount = this.mount.getRetryCount();
            for (int i = 0; i < retryCount && null == this.reader; ++i) {
                try {
                    this.isFirstEntry = true;
                    if (this.server.smb.doFindMore(this)) continue;
                    TraceLog.get().exit(200);
                    return null;
                }
                catch (NqException e) {
                    TraceLog.get().caught(e, 200);
                    if (e.getErrCode() == -1073741528) {
                        try {
                            this.server.smb.doFindOpen(this);
                        }
                        catch (NqException ex) {
                            TraceLog.get().message("Caught exception = " + ex, 2000);
                            TraceLog.get().caught(e, 200);
                        }
                        continue;
                    }
                    if (e.getErrCode() == -2147483642 || e.getErrCode() == -1073741809) {
                        TraceLog.get().exit(200);
                        return null;
                    }
                    if (retryCount == i + 1) {
                        TraceLog.get().exit(200);
                        throw new NqException("Find more files error : " + e.getMessage(), e.getErrCode());
                    }
                    Utility.waitABit(localTimeout);
                    localTimeout *= 2;
                }
            }
            this.entry = new Entry();
            File.Info info = this.entry.info;
            do {
                if (null == this.reader) {
                    this.entry = null;
                    break;
                }
                int curOffset = this.reader.getOffset();
                int nextOffset = curOffset + this.reader.readInt4();
                this.reader.skip(4);
                if (0 > this.reader.getRemaining()) {
                    throw new ClientException("the next offset is out of bound : offset : " + nextOffset, -107);
                }
                info.setCreationTime(this.reader.readLong());
                info.setLastAccessTime(this.reader.readLong());
                info.setLastWriteTime(this.reader.readLong());
                info.setChangeTime(this.reader.readLong());
                info.setEof(this.reader.readLong());
                info.setAllocationSize(this.reader.readLong());
                info.setAttributes(this.reader.readInt4());
                int nameLen = this.reader.readInt4();
                this.reader.skip(30);
                if (this.server.useAscii) {
                    this.entry.name = new String(this.reader.getSrc(), this.reader.getOffset(), nameLen);
                } else {
                    try {
                        int off = this.reader.getOffset();
                        byte[] src = this.reader.getSrc();
                        this.entry.name = new String(src, off, nameLen, "UTF-16LE");
                    }
                    catch (UnsupportedEncodingException e) {
                        TraceLog.get().caught(e, 200);
                        TraceLog.get().exit(200);
                        throw new NqException("Unsupported UTF-16", -21);
                    }
                }
                String separator = this.getLocalPath().equals("") ? "" : BACKSLASH;
                info.setPath(this.getLocalPath() + separator + this.entry.name);
                this.reader.setOffset(nextOffset);
                if (-1 < this.nextCount) {
                    --this.nextCount;
                }
                if (!this.isGoodData && (this.entry.name.equals(".") || this.entry.name.equals(".."))) {
                    if (++countOfDotEntries > 2) {
                        this.reader = null;
                    }
                } else {
                    this.isGoodData = true;
                    continueLoop = false;
                }
                if (0 != this.nextCount && nextOffset != curOffset || !this.isGoodData) continue;
                this.reader = null;
                continueLoop = false;
                break;
            } while (this.entry.name.equals(".") || this.entry.name.equals(".."));
            if (null != this.entry && this.entry.name.length() > 0) {
                if (this.isFirstEntry) {
                    this.isFirstEntry = false;
                    if (this.firstNames.contains(this.entry.name)) {
                        this.reader = null;
                        continueLoop = true;
                        continue;
                    }
                    this.firstNames.add(this.entry.name);
                }
                continueLoop = false;
                this.isGoodData = true;
                continue;
            }
            this.entry = null;
        }
        TraceLog.get().exit(200);
        return this.entry;
    }

    protected void setParser(byte[] source, int offset, int searchCount) {
        this.reader = new BufferReader(source, offset, false);
        if (searchCount > 0) {
            this.nextCount = searchCount;
        }
    }

    protected Share getShare() {
        return this.share;
    }

    public String getLocalPath() {
        return this.localPath;
    }

    public String getPath() {
        return this.path;
    }

    public boolean isDfs() {
        return this.isDfs;
    }

    protected void setIsDfs(boolean isDfs) {
        this.isDfs = isDfs;
    }

    public String getServerName() {
        return this.mount.getInfo().getServerName();
    }

    protected void finalize() throws Throwable {
        if (this.isOpened) {
            try {
                this.close();
            }
            catch (NqException nqException) {
                // empty catch block
            }
        }
    }

    public static class Entry {
        public String name;
        public File.Info info = new File.Info();

        public String toString() {
            return this.name;
        }
    }
}

