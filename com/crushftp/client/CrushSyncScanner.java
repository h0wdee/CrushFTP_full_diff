/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.CrushSyncDaemon;
import com.crushftp.client.CrushSyncDownloadWorker;
import com.crushftp.client.CrushSyncUploadWorker;
import com.crushftp.client.CrushSyncWatcherWrapper;
import com.crushftp.client.GenericClient;
import com.crushftp.client.SnapshotFile;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import com.crushftp.tunnel.FileArchiveEntry;
import com.crushftp.tunnel.FileArchiveOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class CrushSyncScanner {
    int stableSeconds = 5;
    int remoteRefreshSeconds = 9;
    public String saveNow = "";
    Properties prefs = new Properties();
    String syncPath = null;
    CrushSyncWatcherWrapper watcherWrapper = null;
    boolean stop = false;
    Vector log = new Vector();
    int playRemoteJournalLoops = 0;
    public Properties statusInfo = new Properties();
    public Properties downloadStatus = new Properties();
    public Properties uploadStatus = new Properties();
    Vector tempJournal = new Vector();
    public Properties ignoreDownload = new Properties();
    public Properties ignoreDelete = new Properties();
    public Properties ignoreRename = new Properties();
    SimpleDateFormat sdfNormalize = new SimpleDateFormat("mm:ss");
    StringBuffer crushAuth = new StringBuffer();
    public static final Vector logDisk = new Vector();
    boolean uploadsWaiting = false;
    Vector clientCache = new Vector();
    Properties http_config = new Properties();
    Vector pathErrors = new Vector();
    public Object clientLock = new Object();
    CrushSyncDaemon parent = null;
    long transferedBytes = 0L;
    public Properties lastUploadFolderList = new Properties();
    int lastSize = -1;

    public CrushSyncScanner(Properties prefs, CrushSyncDaemon parent) throws IOException {
        this.prefs = prefs;
        this.parent = parent;
        try {
            if (prefs.getProperty("readOnly", "false").equals("false")) {
                this.watcherWrapper = new CrushSyncWatcherWrapper(this);
            }
            this.syncPath = prefs.getProperty("syncPath");
            this.syncPath = String.valueOf(new SnapshotFile(this.syncPath).getCanonicalPath().replace('\\', '/')) + "/";
            if (!this.syncPath.startsWith("/")) {
                this.syncPath = "/" + this.syncPath;
            }
            this.saveNow = prefs.getProperty("saveNow", "");
            this.msg("Sync Started", false);
        }
        catch (Exception e) {
            this.msg(e);
        }
    }

    public void setAuth(StringBuffer crushAuth) {
        this.crushAuth = crushAuth;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public GenericClient getClient() throws Exception {
        GenericClient c = null;
        Object object = this.clientLock;
        synchronized (object) {
            c = this.clientCache.size() == 0 ? Common.getClient(CrushSyncDaemon.thisObj.prefs.getProperty("syncUrl"), "SYNC:", logDisk) : (GenericClient)this.clientCache.remove(0);
            c.setConfigObj(this.http_config);
            c.setConfig("crushAuth", this.crushAuth.toString());
            c.setConfig("send_compressed", String.valueOf(this.prefs.getProperty("send_compressed", "false").equalsIgnoreCase("true")));
            c.setConfig("receive_compressed", String.valueOf(this.prefs.getProperty("receive_compressed", "false").equalsIgnoreCase("true")));
            c.setConfig("crushAuth", this.crushAuth.toString());
        }
        c.login(CrushSyncDaemon.thisObj.prefs.getProperty("syncUsername"), Common.encryptDecrypt(CrushSyncDaemon.thisObj.prefs.getProperty("syncPassword"), false), CrushSyncDaemon.thisObj.prefs.getProperty("clientid"));
        return c;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void releaseClient(GenericClient c) throws Exception {
        c.close();
        Object object = this.clientLock;
        synchronized (object) {
            this.clientCache.addElement(c);
        }
    }

    /*
     * Unable to fully structure code
     */
    public synchronized boolean scan(Vector remoteFiles, Vector remoteJournal, Vector uploadFiles, Vector downloadFiles, boolean playJournal, boolean doUploads, boolean doDownloads) throws Exception {
        block71: {
            ok_sync = true;
            if (CrushSyncDaemon.updateNotified) {
                throw new Exception("Update required, can't start sync.");
            }
            this.statusInfo.put("syncStatus", "Scanning for changes...");
            if (downloadFiles == null) {
                downloadFiles = new Vector<SnapshotFile>();
            }
            if (uploadFiles == null) {
                uploadFiles = new Vector<SnapshotFile>();
            }
            v = new Vector<E>();
            this.msg("Start list files.", false);
            fileLookupLocal = new Properties();
            fileLookupRemote = new Properties();
            remote_last_rid = this.prefs.getProperty("remote_last_rid", "-1");
            if (remote_last_rid.trim().equals("")) {
                remote_last_rid = "-1";
            }
            this.msg(new Exception("Start sync thread trace:"));
            if (!this.prefs.getProperty("realtimeOnly", "false").equals("false")) break block71;
            CrushSyncScanner.getAllFileListing(v, this.syncPath, 999, true);
            this.msg("Done list files:" + v.size(), false);
            while (v.size() > 0) {
                f = (SnapshotFile)v.remove(0);
                path = f.getAbsolutePath().replace('\\', '/');
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
                fileLookupLocal.put(String.valueOf(path.substring(this.syncPath.length() - 1)) + (f.isDirectory() != false ? "/" : ""), f);
                this.msg("Lookup local:" + path.substring(this.syncPath.length() - 1) + (f.isDirectory() != false ? "/" : ""), false);
            }
            this.msg("Hashed list files.  Starting get remote file list.", false);
            this.statusInfo.put("syncStatus", "Scanning for changes...local:" + fileLookupLocal.size() + " remote:0");
            if (remoteFiles == null) {
                c = this.getClient();
                stat = c.stat(this.prefs.getProperty("syncServerPath"));
                if (stat == null) {
                    c.makedirs(this.prefs.getProperty("syncServerPath"));
                    stat = c.stat(this.prefs.getProperty("syncServerPath"));
                }
                if (stat == null) {
                    this.statusInfo.put("syncStatus", "Server directory doesn't exist, and we failed to create it:" + this.prefs.getProperty("syncServerPath"));
                    x = 0;
                    while (!this.stop && x < 10) {
                        Thread.sleep(1000L);
                        ++x;
                    }
                    throw new Exception("Server directory doesn't exist, and we failed to create it:" + this.prefs.getProperty("syncServerPath"));
                }
                if (Common.url_decode(stat.getProperty("privs", "")).indexOf("(syncName") < 0) {
                    this.statusInfo.put("syncStatus", "Server directory is not enabled for synchronization:" + this.prefs.getProperty("syncServerPath"));
                    x = 0;
                    while (!this.stop && x < 10) {
                        Thread.sleep(1000L);
                        ++x;
                    }
                    throw new Exception("Server directory is not enabled for synchronization:" + this.prefs.getProperty("syncServerPath"));
                }
                this.releaseClient(c);
                remoteFiles = new Vector<E>();
                pos = 0;
                bad_result = 0;
                while (bad_result < 30) {
                    rf = this.getRemoteTableData("file_" + pos, 0L, CrushSyncDaemon.thisObj.prefs.getProperty("clientid"), "", "");
                    if (rf == null) {
                        ++bad_result;
                        Thread.sleep(100L);
                        continue;
                    }
                    bad_result = 0;
                    this.msg("Received file list from server:" + rf.size(), false);
                    if (rf.size() == 0) break;
                    pos += rf.size();
                    remoteFiles.addAll(rf);
                    this.statusInfo.put("syncStatus", "Scanning for changes...local:" + fileLookupLocal.size() + " remote:" + remoteFiles.size());
                }
                if (bad_result > 29) {
                    throw new Exception("Unable to get server file listing reliably...");
                }
            }
            this.msg("Done get remote file list:" + remoteFiles.size(), false);
            this.msg("Start get journal from rid:" + remote_last_rid, true);
            if (remoteJournal == null) {
                remoteJournal = this.getRemoteTableData("journal", Long.parseLong(remote_last_rid), CrushSyncDaemon.thisObj.prefs.getProperty("clientid"), "", "");
            }
            this.msg("Done get journal:" + remoteJournal.size(), false);
            if (CrushSyncDaemon.updateNotified) {
                throw new Exception("Update required, can't start sync.");
            }
            if (this.stop) {
                throw new Exception("Stopped.");
            }
            x = 0;
            while (x < remoteFiles.size()) {
                block70: {
                    this.statusInfo.put("syncStatus", "Scanning for changes...local:" + fileLookupLocal.size() + " remote:" + remoteFiles.size() + " (" + x + ")");
                    if (this.stop) {
                        throw new Exception("Stopped.");
                    }
                    p = (Properties)remoteFiles.elementAt(x);
                    try {
                        remotePath = p.getProperty("ITEM_PATH");
                        if (remotePath.equals("/")) break block70;
                        fileLookupRemote.put(remotePath, "");
                        if (!fileLookupLocal.containsKey(remotePath)) {
                            ok = true;
                            xx = 0;
                            while (xx < remoteJournal.size() && ok) {
                                pp = (Properties)remoteJournal.elementAt(xx);
                                if (pp.getProperty("EVENT_TYPE").equalsIgnoreCase("rename")) {
                                    path2 = pp.getProperty("ITEM_PATH").split(";")[1];
                                    if (p.getProperty("ITEM_PATH").startsWith(path2)) {
                                        ok = false;
                                    }
                                }
                                ++xx;
                            }
                            if (remotePath.indexOf(".conflict") >= 0) {
                                ok = false;
                            }
                            if (ok) {
                                this.msg("Download:We are missing a server item:" + remotePath, false);
                                f2 = new SnapshotFile(String.valueOf(this.prefs.getProperty("syncServerPath")) + remotePath.substring(1));
                                if (p.getProperty("ITEM_TYPE").equals("D")) {
                                    f1 = new SnapshotFile(String.valueOf(this.syncPath) + remotePath.substring(1));
                                    f1.mkdirs();
                                    f1.setLastModified(f2.snapshotModified);
                                } else {
                                    f2.snapshotSize = Long.parseLong(p.getProperty("ITEM_SIZE", "0"));
                                    f2.snapshotModified = Long.parseLong(p.getProperty("ITEM_MODIFIED", "0"));
                                    if (!f2.getName().endsWith(".uploading") && !f2.getName().endsWith(".downloading")) {
                                        downloadFiles.addElement(f2);
                                    }
                                }
                                if (Long.parseLong(p.getProperty("UPDATED_RID", "0")) > Long.parseLong(remote_last_rid)) {
                                    remote_last_rid = p.getProperty("UPDATED_RID", "0");
                                    this.msg("remote_last_rid now:" + remote_last_rid, true);
                                }
                            }
                            break block70;
                        }
                        f = (SnapshotFile)fileLookupLocal.get(remotePath);
                        timeDiff = Math.abs(f.lastModified() - Long.parseLong(p.getProperty("ITEM_MODIFIED")));
                        if (this.prefs.getProperty("size_only", "false").equals("true")) {
                            timeDiff = 0L;
                        }
                        hourDiff = (float)timeDiff / 3600000.0f;
                        if (!f.isFile() || f.getName().equals(".DS_Store") || f.getName().endsWith(".downloading") || (timeDiff <= 3000L || (float)((int)hourDiff) == hourDiff) && f.length() == Long.parseLong(p.getProperty("ITEM_SIZE"))) break block70;
                        if (f.length() != Long.parseLong(p.getProperty("ITEM_SIZE")) || !this.prefs.getProperty("size_only", "false").equals("false")) ** GOTO lbl-1000
                        this.msg("Sizes are the same but date isn't. Computing MD5 hash:" + remotePath + ":" + f.length() + " bytes to calculate.", false);
                        this.statusInfo.put("syncStatus", "Computing MD5:" + f);
                        md5Str = Common.getMD5(new FileInputStream(f));
                        this.statusInfo.put("syncStatus", "Scanning for Changes...");
                        if (md5Str.equalsIgnoreCase(p.getProperty("MD5_HASH", "."))) {
                            this.msg("Contents are the same, changing our date to match server:" + remotePath, false);
                            f.setLastModified(Long.parseLong(p.getProperty("ITEM_MODIFIED")));
                        } else lbl-1000:
                        // 2 sources

                        {
                            this.msg("Upload:We have a different item from the server:" + remotePath + "  TimeDiff:" + timeDiff + "   SizeDiff:" + (f.length() - Long.parseLong(p.getProperty("ITEM_SIZE"))), false);
                            if (this.prefs.getProperty("readOnly", "false").equals("true")) {
                                f2 = new SnapshotFile(String.valueOf(this.prefs.getProperty("syncServerPath")) + remotePath.substring(1));
                                f2.snapshotSize = f.length();
                                downloadFiles.addElement(f2);
                                this.msg("Upload:ReadOnly mode, re-downloading item.", false);
                            } else {
                                ok = true;
                                xx = 0;
                                while (xx < remoteJournal.size() && ok) {
                                    pp = (Properties)remoteJournal.elementAt(xx);
                                    if (pp.getProperty("EVENT_TYPE").equalsIgnoreCase("change")) {
                                        ok = false;
                                    }
                                    ++xx;
                                }
                                if (ok && f.isFile()) {
                                    uploadFiles.addElement(f);
                                }
                            }
                        }
                    }
                    catch (Exception e) {
                        this.msg("Error on item:" + p, false);
                        this.msg(e);
                        throw e;
                    }
                }
                ++x;
            }
            this.prefs.put("saveNow", String.valueOf(System.currentTimeMillis()));
            this.msg("Checking for local files the remote is missing, and comparing with pending journal...local items:" + fileLookupLocal.size() + " journal size:" + remoteJournal.size(), true);
            keys = fileLookupLocal.keys();
            while (keys.hasMoreElements()) {
                key = keys.nextElement().toString();
                if (fileLookupRemote.containsKey(key)) continue;
                f = (SnapshotFile)fileLookupLocal.get(key);
                ok = true;
                xx = 0;
                while (xx < remoteJournal.size() && ok) {
                    block73: {
                        block72: {
                            pp = (Properties)remoteJournal.elementAt(xx);
                            if (!pp.getProperty("EVENT_TYPE").equalsIgnoreCase("rename")) break block72;
                            this.msg("Remote journal rename item:" + pp, true);
                            if (!pp.getProperty("ITEM_PATH").toLowerCase().split(";")[0].startsWith(this.prefs.getProperty("syncServerPath").toLowerCase())) break block73;
                            path1 = pp.getProperty("ITEM_PATH").split(";")[0].substring(this.prefs.getProperty("syncServerPath").length() - 1);
                            try {
                                path_tmp = (String.valueOf(f.getPath().substring(this.syncPath.length() - 1)) + (f.isDirectory() != false ? "/" : "")).replace('\\', '/');
                                if (!path_tmp.startsWith("/") && path1.startsWith("/")) {
                                    path_tmp = "/" + path_tmp;
                                }
                                if (path_tmp.startsWith("/") && !path1.startsWith("/")) {
                                    path_tmp = path_tmp.substring(1);
                                }
                                this.msg("RENAME:Checking pending journal on missing server file so we don't re-upload...:" + path_tmp + " vs: " + path1, true);
                                if (path_tmp.startsWith(path1)) {
                                    ok = false;
                                    this.msg("RENAME:Preventing re-upload...:" + path1, true);
                                }
                            }
                            catch (Exception e) {
                                this.msg("Bad path, ignoring:" + f.getPath() + "  isdir:" + f.isDirectory() + "   syncPath:" + this.syncPath, true);
                                this.msg(e);
                                ok = false;
                            }
                        }
                        if (pp.getProperty("EVENT_TYPE").equalsIgnoreCase("delete") && this.prefs.getProperty("honor_deletes", "true").equalsIgnoreCase("true")) {
                            this.msg("Remote journal delete item:" + pp, true);
                            if (pp.getProperty("ITEM_PATH").toLowerCase().startsWith(this.prefs.getProperty("syncServerPath").toLowerCase())) {
                                path1 = pp.getProperty("ITEM_PATH").substring(this.prefs.getProperty("syncServerPath").length() - 1);
                                try {
                                    path_tmp = (String.valueOf(f.getPath().substring(this.syncPath.length() - 1)) + (f.isDirectory() != false ? "/" : "")).replace('\\', '/');
                                    if (!path_tmp.startsWith("/") && path1.startsWith("/")) {
                                        path_tmp = "/" + path_tmp;
                                    }
                                    if (path_tmp.startsWith("/") && !path1.startsWith("/")) {
                                        path_tmp = path_tmp.substring(1);
                                    }
                                    this.msg("DELETE:Checking pending journal on missing server file so we don't re-upload...:" + path_tmp + " vs: " + path1, true);
                                    if (path_tmp.equals(path1)) {
                                        ok = false;
                                        this.msg("DELETE:Preventing re-upload...:" + path1, true);
                                    }
                                }
                                catch (Exception e) {
                                    this.msg("Bad path, ignoring:" + f.getPath() + "  isdir:" + f.isDirectory() + "   syncPath:" + this.syncPath, true);
                                    this.msg(e);
                                    ok = false;
                                }
                            }
                        }
                    }
                    ++xx;
                }
                if (!ok || f.getName().equals(".DS_Store") || f.getName().endsWith(".downloading") || !this.prefs.getProperty("readOnly", "false").equals("false") || f.getName().indexOf(".conflict") >= 0) continue;
                this.msg("Upload:We have a new item:" + key, false);
                uploadFiles.addElement(f);
            }
        }
        if (CrushSyncDaemon.updateNotified) {
            throw new Exception("Update required, can't start sync.");
        }
        if (this.stop) {
            throw new Exception("Stopped.");
        }
        if (this.prefs.getProperty("realtimeOnly", "false").equals("false")) {
            if (doUploads) {
                this.msg("Uploading " + uploadFiles.size() + " items.", false);
                this.uploadChanges(uploadFiles, false, fileLookupRemote);
            }
            if (CrushSyncDaemon.updateNotified) {
                throw new Exception("Update required, can't start sync.");
            }
            if (this.stop) {
                throw new Exception("Stopped.");
            }
            if (doDownloads) {
                this.msg("Downloading " + downloadFiles.size() + " items.", false);
                this.downloadChanges(downloadFiles, -1, true);
                if (this.pathErrors.size() > 0) {
                    while (this.pathErrors.size() > 0) {
                        this.msg("ERROR:" + this.pathErrors.remove(0), false);
                    }
                    throw new Exception("Errors while downloading:");
                }
            }
        }
        if (CrushSyncDaemon.updateNotified) {
            throw new Exception("Update required, can't start sync.");
        }
        if (this.stop) {
            throw new Exception("Stopped.");
        }
        this.prefs.put("remote_last_rid", remote_last_rid);
        this.prefs.put("saveNow", String.valueOf(System.currentTimeMillis()));
        if (playJournal) {
            CrushSyncScanner.playRemoteJournal(this, remoteJournal);
        }
        if (CrushSyncDaemon.updateNotified) {
            throw new Exception("Update required, can't start sync.");
        }
        if (this.stop) {
            throw new Exception("Stopped.");
        }
        vrl = new VRL(CrushSyncDaemon.thisObj.prefs.getProperty("syncUrl"));
        this.statusInfo.put("syncStatus", "Active");
        return ok_sync;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized void downloadChanges(Vector downloadFiles, int rev, boolean consumeErrors) throws Exception {
        this.http_config.put("use_tunnel", this.prefs.getProperty("allowTunnel", "true"));
        if (downloadFiles.size() == 0) {
            return;
        }
        if (downloadFiles.size() < 1000) {
            this.msg("Downloading: " + downloadFiles, true);
        } else {
            this.msg("Downloading: " + downloadFiles.size(), true);
        }
        this.statusInfo.put("syncStatus", "Downloading...(" + downloadFiles.size() + ")");
        int x = downloadFiles.size() - 1;
        while (x >= 0) {
            SnapshotFile f = (SnapshotFile)downloadFiles.elementAt(x);
            if ((String.valueOf(f.getPath()) + "/").equals(this.prefs.getProperty("syncServerPath"))) {
                downloadFiles.removeElementAt(x);
            } else if (f.getName().endsWith(".uploading")) {
                downloadFiles.removeElementAt(x);
            }
            this.statusInfo.put("syncStatus", "Downloading...removed in-use items(" + x + ")");
            --x;
        }
        this.statusInfo.put("syncStatus", "Downloading...removed in-use items(" + downloadFiles.size() + ")");
        if (downloadFiles.size() == 0) {
            return;
        }
        long totalSize = 0L;
        String filePaths = "";
        int x2 = 0;
        while (x2 < downloadFiles.size()) {
            SnapshotFile f = (SnapshotFile)downloadFiles.elementAt(x2);
            totalSize += f.length();
            String filePath = f.getPath().replace('\\', '/');
            Properties p = new Properties();
            p.put("path", filePath);
            if (x2 < 1000) {
                filePaths = String.valueOf(filePaths) + (x2 > 0 ? "," : "") + filePath;
            }
            if (f.isFile()) {
                p.put("type", "FILE");
                p.put("size", String.valueOf(f.length()));
            } else {
                p.put("type", "DIR");
                p.put("size", "0");
                f.mkdirs();
            }
            this.statusInfo.put("syncStatus", "Downloading...making folders (" + x2 + "/" + downloadFiles.size() + ")");
            ++x2;
        }
        this.statusInfo.put("syncStatus", "Downloading...(" + downloadFiles.size() + ")");
        this.statusInfo.put("toDownloadStatus", "Downloads:" + filePaths);
        this.downloadStatus.put("downloadStatus", "");
        this.downloadStatus.put("totalItems", String.valueOf(downloadFiles.size()));
        this.downloadStatus.put("totalBytes", String.valueOf(totalSize));
        this.downloadStatus.put("transferedItems", "0");
        this.downloadStatus.put("transferedBytes", "0");
        this.transferedBytes = 0L;
        Vector files_loop = (Vector)downloadFiles.clone();
        Vector<CrushSyncDownloadWorker> threads_used = new Vector<CrushSyncDownloadWorker>();
        Vector<CrushSyncDownloadWorker> threads_free = new Vector<CrushSyncDownloadWorker>();
        Vector errors = new Vector();
        Vector remotePaths = new Vector();
        int x3 = 0;
        while (x3 < Integer.parseInt(this.prefs.getProperty("download_threads", "1"))) {
            threads_free.addElement(new CrushSyncDownloadWorker(this, files_loop, remotePaths, threads_used, threads_free, downloadFiles, errors, consumeErrors, rev));
            ++x3;
        }
        while (files_loop.size() > 0) {
            Vector<CrushSyncDownloadWorker> vector = threads_free;
            synchronized (vector) {
                while (threads_free.size() > 0) {
                    CrushSyncDownloadWorker dw = (CrushSyncDownloadWorker)threads_free.remove(0);
                    threads_used.addElement(dw);
                    Worker.startWorker(dw);
                    Thread.sleep(10L);
                }
            }
            Thread.sleep(100L);
            if (errors.size() > 0) {
                throw (Exception)errors.remove(0);
            }
            this.uploadStatus.put("downloadStatus", "Threads:" + threads_used.size() + " " + remotePaths + " " + this.http_config.getProperty("tunnel_status", ""));
            this.statusInfo.put("syncStatus", "Downloading...(" + downloadFiles.size() + " remaining:" + files_loop.size() + ")");
        }
        while (threads_used.size() > 0) {
            Thread.sleep(100L);
            this.downloadStatus.put("downloadStatus", "Threads:" + threads_used.size() + " " + remotePaths + " " + this.http_config.getProperty("tunnel_status", ""));
        }
        this.downloadStatus.put("totalItems", "0");
        this.downloadStatus.put("totalBytes", "0");
        this.downloadStatus.put("transferedItems", "0");
        this.downloadStatus.put("transferedBytes", "0");
        this.downloadStatus.put("downloadStatus", "");
        this.statusInfo.put("syncStatus", "Active");
        this.statusInfo.put("toDownloadStatus", "");
    }

    public long getMd5Diff(String localPath, String remotePath, Vector byteRanges, long fileSize) throws InterruptedException {
        this.msg("Asking server for list of remote MD5 hashes for " + remotePath, false);
        StringBuffer status1 = new StringBuffer();
        StringBuffer status2 = new StringBuffer();
        Vector chunksF1 = new Vector();
        Vector chunksF2 = new Vector();
        Common.doMD5Comparisons(new VRL("file://" + localPath), new VRL(CrushSyncDaemon.thisObj.prefs.getProperty("syncUrl")), "upload", this.statusInfo, remotePath, chunksF1, chunksF2, this.crushAuth, status1, status2, byteRanges);
        this.msg("Got " + chunksF1.size() + " remote and " + chunksF2.size() + " local MD5 hashes for " + localPath, false);
        this.msg("Hash comparison: " + localPath + ":" + byteRanges.size(), false);
        if (byteRanges.size() == 1 && byteRanges.elementAt(0).equals("0--1")) {
            byteRanges.setElementAt("0-" + fileSize, 0);
        }
        long amount = 0L;
        int x = 0;
        while (x < byteRanges.size()) {
            if (!byteRanges.elementAt(x).toString().trim().equals("")) {
                long end;
                long start = Long.parseLong(byteRanges.elementAt(x).toString().substring(0, byteRanges.elementAt(x).toString().indexOf("-")));
                String endPart = byteRanges.elementAt(x).toString().substring(byteRanges.elementAt(x).toString().indexOf("-") + 1);
                if (endPart.equals("")) {
                    endPart = String.valueOf(fileSize);
                }
                if ((end = Long.parseLong(endPart)) >= 0L) {
                    amount += end - start;
                }
            }
            ++x;
        }
        this.transferedBytes += fileSize - amount;
        return this.transferedBytes;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized void uploadChanges(Vector files2, boolean recurse_find, Properties fileLookupRemote) throws Exception {
        SnapshotFile f;
        this.http_config.put("use_tunnel", this.prefs.getProperty("allowTunnel", "true"));
        if (files2.size() < 1000) {
            this.msg("Uploading: " + files2, true);
        } else {
            this.msg("Uploading: " + files2.size(), true);
        }
        if (files2.size() == 0) {
            return;
        }
        if (this.prefs.getProperty("readOnly", "false").equals("true")) {
            this.msg("Upload: skipping upload of " + files2.size() + " items because we are in read only mode.", false);
            return;
        }
        Vector<SnapshotFile> files = new Vector<SnapshotFile>();
        files.addElement(new SnapshotFile(this.syncPath));
        Vector<String> files3 = new Vector<String>();
        int x = files2.size() - 1;
        while (x >= 0) {
            f = (SnapshotFile)files2.elementAt(x);
            f = this.fixChars(f);
            files2.setElementAt(f, x);
            if (f.isDirectory() && recurse_find) {
                CrushSyncScanner.appendListing(f.getPath(), files2, "/", 99, true);
            }
            --x;
        }
        x = files2.size() - 1;
        while (x >= 0) {
            this.statusInfo.put("syncStatus", "Eliminating duplicates and in progress files:" + files3.size() + "/" + files2.size());
            f = (SnapshotFile)files2.elementAt(x);
            if (!new SnapshotFile(f.getPath()).exists()) {
                files2.removeElementAt(x);
            } else if (f.getName().endsWith(".downloading")) {
                files2.removeElementAt(x);
            } else if (f.getName().startsWith(".")) {
                files2.removeElementAt(x);
            } else if (f.getName().endsWith(".uploading")) {
                files2.removeElementAt(x);
            } else if (f.getCanonicalPath().equals(new SnapshotFile(this.syncPath).getCanonicalPath())) {
                files2.removeElementAt(x);
            } else if (recurse_find) {
                if (files3.indexOf(f.getPath()) < 0) {
                    files3.addElement(f.getPath());
                } else {
                    files2.removeElementAt(x);
                }
            }
            if (this.stop) {
                throw new Exception("Stopped.");
            }
            --x;
        }
        files3.removeAllElements();
        Properties folderList = new Properties();
        if (files2.size() > 0) {
            File f2;
            SnapshotFile f3;
            if (this.stop) {
                throw new Exception("Stopped.");
            }
            this.statusInfo.put("syncStatus", "Uploading...(" + files2.size() + ")");
            long totalSize = 0L;
            String filePaths = "";
            this.msg("Uploading stable items:" + files2.size(), false);
            int loops3 = 0;
            int x2 = 0;
            while (x2 < files2.size()) {
                f3 = (SnapshotFile)files2.elementAt(x2);
                totalSize += f3.length();
                if (loops3++ == 1000) {
                    this.statusInfo.put("syncStatus", "Uploading...(" + files2.size() + ") (" + x2 + "/" + files2.size() + ") Calculating size:" + Common.format_bytes_short(totalSize));
                    loops3 = 0;
                }
                this.msg("Uploading:" + f3, false);
                if (filePaths.length() < 1000) {
                    try {
                        filePaths = String.valueOf(filePaths) + (x2 > 0 ? "," : "") + f3.getPath().substring(this.syncPath.length() - 1);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (f3.isDirectory()) {
                    folderList.put(f3.getCanonicalPath(), "");
                }
                if (this.stop) {
                    throw new Exception("Stopped.");
                }
                ++x2;
            }
            if (filePaths.length() > 1000) {
                filePaths = String.valueOf(filePaths) + "...";
            }
            for (int loop = 0; loop < files2.size(); ++loop) {
                String f2path;
                this.statusInfo.put("syncStatus", "Uploading...(" + files2.size() + ") (" + loop + "/" + files2.size() + ") Building folder list.");
                f3 = (SnapshotFile)files2.elementAt(loop);
                if (!f3.isFile() || folderList.containsKey(f2path = f3.getParentFile().getCanonicalPath())) continue;
                folderList.put(f2path, "");
                files2.insertElementAt(new SnapshotFile(String.valueOf(f2path) + "/"), loop);
                if (!this.stop) continue;
                throw new Exception("Stopped.");
            }
            this.statusInfo.put("toUploadStatus", "Uploads:" + filePaths);
            this.statusInfo.put("syncStatus", "Uploading...(" + files2.size() + ")");
            if (this.stop) {
                throw new Exception("Stopped.");
            }
            this.uploadStatus.put("uploadStatus", "");
            this.uploadStatus.put("totalItems", String.valueOf(files2.size()));
            this.uploadStatus.put("totalBytes", String.valueOf(totalSize));
            this.uploadStatus.put("transferedItems", "0");
            this.uploadStatus.put("transferedBytes", "0");
            this.transferedBytes = 0L;
            Vector files_loop1 = (Vector)files2.clone();
            Vector delete_folders = new Vector();
            Vector<CrushSyncUploadWorker> threads_used = new Vector<CrushSyncUploadWorker>();
            Vector<CrushSyncUploadWorker> threads_free = new Vector<CrushSyncUploadWorker>();
            Vector errors = new Vector();
            Vector remotePaths = new Vector();
            if (System.getProperty("crushftp.zipstream_folders", "true").equals("true")) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                FileArchiveOutputStream zout = new FileArchiveOutputStream(baos, true);
                int count = 0;
                int total_count = 0;
                while (files_loop1.size() > 0) {
                    f2 = (SnapshotFile)files_loop1.remove(0);
                    String new_folder = f2.getPath().replace('\\', '/');
                    if (((SnapshotFile)f2).isDirectory() && !new_folder.endsWith("/")) {
                        new_folder = String.valueOf(new_folder) + "/";
                    }
                    if (!new_folder.startsWith("/")) {
                        new_folder = "/" + new_folder;
                    }
                    String remotePath = String.valueOf(this.prefs.getProperty("syncServerPath")) + new_folder.substring(this.syncPath.length());
                    if ((((SnapshotFile)f2).isDirectory() || new SnapshotFile(new_folder).isDirectory()) && !this.lastUploadFolderList.containsKey(remotePath)) {
                        String remote_folder_path = remotePath.substring(this.prefs.getProperty("syncServerPath").length() - 1);
                        this.msg("Creating server side folder:new_folder=" + new_folder + " remotePath=" + remotePath + " remote_folder_path=" + remote_folder_path, true);
                        if (!fileLookupRemote.containsKey(remote_folder_path) && !remote_folder_path.equals("/")) {
                            FileArchiveEntry zipEntry = new FileArchiveEntry(remote_folder_path);
                            zipEntry.setTime(((SnapshotFile)f2).lastModified());
                            zout.putArchiveEntry(zipEntry);
                            zout.closeArchiveEntry();
                            this.lastUploadFolderList.put(remotePath, (Object)System.currentTimeMillis());
                            ++count;
                        }
                        ++total_count;
                        this.statusInfo.put("syncStatus", "Building folder info...(" + files2.size() + " remaining:" + files_loop1.size() + ")");
                    }
                    if (count < 1000) continue;
                    count = 0;
                    zout.finish();
                    zout.close();
                    GenericClient c = this.getClient();
                    this.statusInfo.put("syncStatus", "Uploading " + total_count + "/" + folderList.size() + " folders info...(" + Common.format_bytes_short(baos.size()) + ") remaining:" + (folderList.size() - total_count) + ")");
                    OutputStream out = c.upload(String.valueOf(this.prefs.getProperty("syncServerPath")) + "folders.zipstream", 0L, true, true);
                    out.write(baos.toByteArray());
                    out.close();
                    this.releaseClient(c);
                    baos = new ByteArrayOutputStream();
                    zout = new FileArchiveOutputStream(baos, true);
                }
                zout.finish();
                zout.close();
                if (count > 0) {
                    GenericClient c = this.getClient();
                    this.statusInfo.put("syncStatus", "Uploading " + folderList.size() + " folders info...(" + Common.format_bytes_short(baos.size()) + ")");
                    OutputStream out = c.upload(String.valueOf(this.prefs.getProperty("syncServerPath")) + "folders.zipstream", 0L, true, true);
                    out.write(baos.toByteArray());
                    out.close();
                    this.releaseClient(c);
                }
            }
            this.checkQuota(totalSize, true);
            Vector files_loop2 = (Vector)files2.clone();
            int x32 = 0;
            while (x32 < Integer.parseInt(this.prefs.getProperty("upload_threads", "1"))) {
                threads_free.addElement(new CrushSyncUploadWorker(this, files_loop2, remotePaths, threads_free, threads_used, delete_folders, files2, errors, false));
                ++x32;
            }
            while (files_loop2.size() > 0) {
                Vector<CrushSyncUploadWorker> x32 = threads_free;
                synchronized (x32) {
                    while (threads_free.size() > 0) {
                        CrushSyncUploadWorker uw = (CrushSyncUploadWorker)threads_free.remove(0);
                        threads_used.addElement(uw);
                        Worker.startWorker(uw);
                    }
                }
                Thread.sleep(100L);
                if (errors.size() > 0) {
                    throw (Exception)errors.remove(0);
                }
                this.uploadStatus.put("uploadStatus", "Threads:" + threads_used.size() + " " + remotePaths + " " + this.http_config.getProperty("tunnel_status", ""));
                this.statusInfo.put("syncStatus", "Uploading...(" + files2.size() + " remaining:" + files_loop2.size() + ")");
            }
            while (threads_used.size() > 0) {
                Thread.sleep(100L);
                this.uploadStatus.put("uploadStatus", "Threads:" + threads_used.size() + " " + remotePaths + " " + this.http_config.getProperty("tunnel_status", ""));
            }
            String scan_folder = new SnapshotFile(this.syncPath).getCanonicalPath();
            int loops = 0;
            while (loops < 10) {
                int loops2 = delete_folders.size() - 1;
                while (loops2 >= 0) {
                    f2 = (File)delete_folders.elementAt(loops2);
                    this.ignoreDelete.put(f2.getCanonicalPath(), String.valueOf(System.currentTimeMillis()));
                    if (f2.exists() && !f2.getCanonicalPath().equals(scan_folder) && f2.delete()) {
                        delete_folders.removeElementAt(loops2);
                    }
                    --loops2;
                }
                ++loops;
            }
            this.uploadStatus.put("totalItems", "0");
            this.uploadStatus.put("totalBytes", "0");
            this.uploadStatus.put("transferedItems", "0");
            this.uploadStatus.put("transferedBytes", "0");
            this.uploadStatus.put("uploadStatus", "");
            this.checkQuota(totalSize, false);
            this.statusInfo.put("syncStatus", "Active");
            this.statusInfo.put("toUploadStatus", "");
        }
        this.lastUploadFolderList.clear();
    }

    public void checkQuota(long totalSize, boolean before) throws Exception {
        long avail;
        GenericClient c = this.getClient();
        this.statusInfo.put("syncStatus", "Checking quota ...");
        String quota = c.doCommand("SITE QUOTA " + this.prefs.getProperty("syncServerPath"));
        this.releaseClient(c);
        if (quota.startsWith("214 ") && (avail = Long.parseLong((quota = quota.substring(4).trim()).split(":")[0])) != -12345L) {
            long max = Long.parseLong(quota.split(":")[1]);
            if (avail < totalSize && before) {
                this.parent.growl("You are about to run out of space!  Available:" + Common.format_bytes_short(avail) + " Upload size:" + Common.format_bytes_short(totalSize));
            } else if ((double)((float)avail / (float)max) <= 0.01) {
                this.parent.growl("Your quota is 100% full!");
            } else if ((double)((float)avail / (float)max) <= 0.1) {
                this.parent.growl("Your quota is over 90% full!  Available:" + Common.format_bytes_short(avail));
            } else if ((double)((float)avail / (float)max) <= 0.2) {
                this.parent.growl("Your quota is over 80% full!  Available:" + Common.format_bytes_short(avail));
            }
        }
    }

    public void doRemoteChangedFile(Properties p, Vector alreadyProcessed, Vector files2) throws Exception {
        if (!p.getProperty("ITEM_PATH").startsWith(this.prefs.getProperty("syncServerPath"))) {
            this.msg("Ignoring item since its not part of our server path.  " + p.getProperty("ITEM_PATH") + " does not start with " + this.prefs.getProperty("syncServerPath") + ".", false);
            return;
        }
        String path = Common.dots(p.getProperty("ITEM_PATH").substring(this.prefs.getProperty("syncServerPath").length() - 1));
        if (alreadyProcessed.indexOf(path) < 0) {
            alreadyProcessed.addElement(path);
            SnapshotFile f = new SnapshotFile(String.valueOf(this.syncPath) + p.getProperty("ITEM_PATH").substring(this.prefs.getProperty("syncServerPath").length() - 1));
            SnapshotFile f2 = null;
            f2 = this.prefs.getProperty("syncServerPath").endsWith("/") && path.startsWith("/") ? new SnapshotFile(String.valueOf(this.prefs.getProperty("syncServerPath")) + path.substring(1)) : new SnapshotFile(String.valueOf(this.prefs.getProperty("syncServerPath")) + path);
            if (f.exists() && f.isFile() && this.prefs.getProperty("readOnly", "false").equals("false")) {
                String prior_md5 = p.getProperty("PRIOR_MD5", "");
                String current_md5 = Common.getMD5(new FileInputStream(f));
                if (!current_md5.equalsIgnoreCase(prior_md5) && current_md5.length() > 0 && prior_md5.length() > 0) {
                    Vector priors = this.getRemoteTableData("journal", 0L, CrushSyncDaemon.thisObj.prefs.getProperty("clientid"), "", p.getProperty("ITEM_PATH").substring(this.prefs.getProperty("syncServerPath").length() - 1));
                    String prior_mds5s = "";
                    int x = 0;
                    while (x < priors.size()) {
                        Properties ppp = (Properties)priors.elementAt(x);
                        prior_mds5s = String.valueOf(prior_mds5s) + ppp.getProperty("PRIOR_MD5").toUpperCase() + ",";
                        ++x;
                    }
                    if (prior_mds5s.indexOf(current_md5.toUpperCase()) < 0) {
                        this.msg("Conflict " + p.getProperty("ITEM_PATH") + " based on md5 hashes: prior:" + prior_md5 + "  actual:" + current_md5, false);
                        this.conflict(p.getProperty("ITEM_PATH"));
                        return;
                    }
                }
            }
            if (p.getProperty("ITEM_MODIFIED", "0").equals("0")) {
                GenericClient c = this.getClient();
                Properties stat = c.stat(p.getProperty("ITEM_PATH"));
                if (stat != null) {
                    p.put("ITEM_MODIFIED", stat.getProperty("modified"));
                }
                if (p.getProperty("ITEM_MODIFIED", "0").equals("0")) {
                    p.put("ITEM_MODIFIED", String.valueOf(System.currentTimeMillis()));
                }
                this.releaseClient(c);
            }
            f2.put("ITEM_PATH", p.getProperty("ITEM_PATH").substring(this.prefs.getProperty("syncServerPath").length() - 1));
            f2.snapshotModified = Long.parseLong(p.getProperty("ITEM_MODIFIED", "0")) / 1000L * 1000L;
            f2.snapshotSize = Long.parseLong(p.getProperty("ITEM_SIZE", "0"));
            this.msg("Remote:" + p.getProperty("EVENT_TYPE") + ":" + p.getProperty("ITEM_PATH") + p, false);
            if (p.getProperty("ITEM_TYPE", "F").equals("D") || p.getProperty("ITEM_PATH").endsWith("/")) {
                this.ignoreDownload.put(f.getCanonicalPath(), String.valueOf(System.currentTimeMillis()));
                f.mkdirs();
            } else {
                this.msg("Remote:" + f + ":" + f2 + ":" + f.lastModified() + ":" + f2.lastModified() + ":" + f.length() + ":" + f2.length(), false);
                if (f.lastModified() != f2.lastModified() || f.length() != f2.length() || !f.exists()) {
                    this.msg("Adding to download queue:" + f2 + ":remote size/date:" + f2.length() + "/" + f2.lastModified() + " vs local size/date:" + f.length() + "/" + f.lastModified(), false);
                    files2.addElement(f2);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void doFileChanged(SnapshotFile f) {
        try {
            if (System.currentTimeMillis() - Long.parseLong(this.ignoreDownload.getProperty(f.getCanonicalPath(), "0")) < (long)(this.stableSeconds * 1000)) {
                return;
            }
        }
        catch (IOException e) {
            this.msg(e);
        }
        if (!(f.getName().equals(".DS_Store") || f.getName().equals("Thumbs.db") || f.isDirectory())) {
            Vector vector = this.tempJournal;
            synchronized (vector) {
                this.msg("In Progress files:" + System.currentTimeMillis() + ":" + this.ignoreDownload.toString(), false);
                int x22 = this.tempJournal.size() - 1;
                while (x22 >= 0) {
                    Properties p = (Properties)this.tempJournal.elementAt(x22);
                    if (!p.getProperty("type").equals("upload")) break;
                    SnapshotFile f1 = (SnapshotFile)p.get("file1");
                    if (f1.getPath().equals(f.getPath())) {
                        return;
                    }
                    --x22;
                }
                try {
                    this.ignoreDownload.put(f.getCanonicalPath(), String.valueOf(System.currentTimeMillis() + 10000L));
                }
                catch (IOException x22) {
                    // empty catch block
                }
                this.msg("Local:Changed:" + f, false);
                Properties p = new Properties();
                p.put("type", "upload");
                p.put("file1", f);
                p.put("time1", String.valueOf(System.currentTimeMillis()));
                this.tempJournal.addElement(p);
            }
        }
    }

    public SnapshotFile fixChars(SnapshotFile f) {
        String s = System.getProperty("crushsync.bad_chars", ":/\\");
        SnapshotFile f2 = f;
        try {
            int x = 0;
            while (x < s.length()) {
                if (f.getName().indexOf(String.valueOf(s.charAt(x))) >= 0) {
                    f2 = new SnapshotFile(String.valueOf(f.getParentFile().getCanonicalPath()) + File.separator + f.getName().replace(s.charAt(x), '_'));
                    this.ignoreRename.put(f.getCanonicalPath(), String.valueOf(System.currentTimeMillis() - 4000L));
                    this.ignoreRename.put(f2.getPath(), String.valueOf(System.currentTimeMillis() - 4000L));
                    f.renameTo(f2);
                    f = f2;
                }
                ++x;
            }
        }
        catch (IOException e) {
            this.msg(e);
        }
        return f2;
    }

    public void doDelete(String delete) throws Exception {
        if (this.prefs.getProperty("upload_delete", "false").equals("true")) {
            this.msg("Ignoring delete event for " + delete + " because this is an upload and delete sync.", false);
            return;
        }
        if (delete.length() > 0) {
            this.msg("Playing local journal to delete items.", false);
            this.msg(delete, false);
            GenericClient c = this.getClient();
            c.delete(delete);
            c.close();
            this.releaseClient(c);
        }
    }

    public boolean doRename(String pathTmp1, String pathTmp2) throws Exception {
        if (pathTmp1.startsWith("/")) {
            pathTmp1 = pathTmp1.substring(1);
        }
        if (pathTmp2.startsWith("/")) {
            pathTmp2 = pathTmp2.substring(1);
        }
        GenericClient c = this.getClient();
        Properties statSrc = c.stat(String.valueOf(this.prefs.getProperty("syncServerPath")) + pathTmp1);
        Properties statDest = c.stat(String.valueOf(this.prefs.getProperty("syncServerPath")) + pathTmp2);
        if (statDest != null && statSrc != null) {
            c.delete(String.valueOf(this.prefs.getProperty("syncServerPath")) + pathTmp2);
        }
        boolean ok = c.rename(String.valueOf(this.prefs.getProperty("syncServerPath")) + pathTmp1, String.valueOf(this.prefs.getProperty("syncServerPath")) + pathTmp2, false);
        this.releaseClient(c);
        return ok;
    }

    public void storeVector(Vector v) throws Exception {
        if (this.lastSize == 0 && v.size() == 0) {
            return;
        }
        new SnapshotFile(String.valueOf(this.parent.base_path) + "tempJournalProcessing.obj").delete();
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(String.valueOf(this.parent.base_path) + "tempJournalProcessing.obj"));
        oos.writeObject(v);
        oos.close();
        this.lastSize = v.size();
    }

    public void loadVector(Vector v) throws Exception {
        if (new SnapshotFile(String.valueOf(this.parent.base_path) + "tempJournalProcessing.obj").exists()) {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(String.valueOf(this.parent.base_path) + "tempJournalProcessing.obj"));
            Vector v2 = (Vector)ois.readObject();
            ois.close();
            v.addAll(v2);
        }
    }

    public void playLocalJournalToRemote() throws Exception {
        if (this.tempJournal.size() > 0) {
            this.msg("Start processing local journal, have " + this.tempJournal.size() + " items.", false);
        }
        try {
            int skip_offset = 0;
            while (this.tempJournal.size() > skip_offset) {
                SnapshotFile f2;
                boolean skip_one = false;
                Properties p = (Properties)this.tempJournal.elementAt(skip_offset);
                this.msg("--------------------------------------", false);
                int x = skip_offset;
                while (x < this.tempJournal.size()) {
                    this.msg("PENDING:" + x + ":" + this.tempJournal.elementAt(x), false);
                    ++x;
                }
                this.msg("--------------------------------------", false);
                this.msg("tempJournal:" + System.currentTimeMillis() + ":" + p, false);
                if (p.getProperty("type", "").equals("delete") && this.prefs.getProperty("honor_deletes", "true").equalsIgnoreCase("true")) {
                    this.tempJournal.remove(skip_offset);
                    SnapshotFile f1 = (SnapshotFile)p.get("file1");
                    String pathTmp = f1.getPath().substring(this.syncPath.length() - 1).replace('\\', '/');
                    if (pathTmp.startsWith("/")) {
                        pathTmp = pathTmp.substring(1);
                    }
                    this.doDelete(String.valueOf(this.prefs.getProperty("syncServerPath")) + pathTmp);
                } else if (p.getProperty("type", "").equals("delete") && this.prefs.getProperty("honor_deletes", "true").equalsIgnoreCase("false")) {
                    this.msg("Honor deletes not enabled, skipping item:" + p, false);
                    this.tempJournal.remove(skip_offset);
                }
                if (p.getProperty("type", "").equals("rename")) {
                    SnapshotFile f1 = (SnapshotFile)p.get("file1");
                    SnapshotFile f22 = (SnapshotFile)p.get("file2");
                    f22 = this.fixChars(f22);
                    p.put("file2", f22);
                    String pathTmp1 = f1.getPath().substring(this.syncPath.length() - 1).replace('\\', '/');
                    String pathTmp2 = f22.getPath().substring(this.syncPath.length() - 1).replace('\\', '/');
                    if (this.doRename(pathTmp1, pathTmp2)) {
                        this.msg("Renaming file " + pathTmp1 + "  to  " + pathTmp2, false);
                        this.tempJournal.remove(skip_offset);
                    } else {
                        this.msg("Renaming file " + pathTmp1 + "  to  " + pathTmp2 + "  failed, uploading instead.", false);
                        p.put("type", "upload");
                        p.put("file1", f22);
                        p.put("time1", "0");
                    }
                }
                if (!p.getProperty("type", "").equals("upload")) continue;
                this.uploadsWaiting = true;
                this.msg("Figuring out if upload item is stable:" + p, false);
                boolean ok = false;
                SnapshotFile f1 = (SnapshotFile)p.get("file1");
                if (f1.isDirectory() || System.currentTimeMillis() - Long.parseLong(p.getProperty("time1", "0")) > (long)(this.stableSeconds * 1000)) {
                    ok = true;
                    f2 = new SnapshotFile(f1.getPath());
                    if (f2.exists() && (f1.length() != f2.length() || f2.lastModified() != f1.lastModified()) && this.prefs.getProperty("allowUnstable", "false").equals("false")) {
                        p.put("file1", f2);
                        p.put("time1", String.valueOf(System.currentTimeMillis()));
                        ok = false;
                    }
                    try (RandomAccessFile in = null;){
                        try {
                            if (f2.isFile() && f2.exists()) {
                                in = new RandomAccessFile(f2.getPath(), "r");
                            } else if (!f2.exists()) {
                                Properties pp;
                                ok = false;
                                Vector<Properties> to_remove = new Vector<Properties>();
                                int times_changed = 0;
                                int x2 = 1 + skip_offset;
                                while (x2 < this.tempJournal.size()) {
                                    pp = (Properties)this.tempJournal.elementAt(x2);
                                    if (pp.getProperty("type", "").equals("rename") && ((SnapshotFile)pp.get("file1")).getPath().equals(f1.getPath())) {
                                        if (times_changed == 0) {
                                            this.msg("UPDATING NAME:" + f1 + " to " + pp.get("file2"), false);
                                            f1 = (SnapshotFile)pp.get("file2");
                                            p.put("file1", f1);
                                            this.msg("REMOVING FUTURE ITEM1:" + x2 + ":" + pp, false);
                                        }
                                        ok = true;
                                        to_remove.addElement(pp);
                                        ++times_changed;
                                    }
                                    ++x2;
                                }
                                x2 = 1 + skip_offset;
                                while (times_changed > 0 && x2 < this.tempJournal.size()) {
                                    pp = (Properties)this.tempJournal.elementAt(x2);
                                    if (pp.getProperty("type", "").equals("rename") && ((SnapshotFile)pp.get("file1")).getPath().equals(f1.getPath()) && to_remove.indexOf(pp) < 0) {
                                        this.msg("REMOVING FUTURE ITEM2:" + x2 + ":" + pp, false);
                                        to_remove.addElement(pp);
                                    }
                                    ++x2;
                                }
                                while (to_remove.size() > 0) {
                                    this.tempJournal.removeElement(to_remove.remove(0));
                                }
                                if (!f1.exists()) {
                                    this.tempJournal.remove(skip_offset);
                                }
                                this.msg("------------++++++++++---------------", false);
                                x2 = skip_offset;
                                while (x2 < this.tempJournal.size()) {
                                    this.msg("PENDING:" + x2 + ":" + this.tempJournal.elementAt(x2), false);
                                    ++x2;
                                }
                                this.msg("------------++++++++++---------------", false);
                            }
                        }
                        catch (IOException e) {
                            if (("" + e).indexOf("being used") >= 0) {
                                ++skip_offset;
                                skip_one = true;
                            }
                            ok = false;
                            if (in != null) {
                                in.close();
                            }
                        }
                    }
                }
                if (this.prefs.getProperty("allowUnstable", "false").equals("true") && !ok && (f2 = new SnapshotFile(f1.getPath())).exists() && (f1.length() != f2.length() || f2.lastModified() != f1.lastModified())) {
                    p.put("file1", f2);
                    p.put("time1", String.valueOf(System.currentTimeMillis()));
                    this.msg("Uploading changes in " + f2 + "again since its still changing. (" + f1.length() + " != " + f2.length() + ")", false);
                    ok = true;
                    this.tempJournal.addElement(p);
                }
                if (ok) {
                    Vector<SnapshotFile> uploads = new Vector<SnapshotFile>();
                    uploads.addElement(f1);
                    this.uploadChanges(uploads, true, new Properties());
                    this.tempJournal.remove(skip_offset);
                    continue;
                }
                if (skip_one) continue;
                break;
            }
        }
        finally {
            this.storeVector(this.tempJournal);
            this.msg("Done processing local journal, still have " + this.tempJournal.size() + " items.", false);
            this.uploadsWaiting = false;
        }
    }

    public void startJournalPublisher() {
        new Thread(new Runnable(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             * Unable to fully structure code
             */
            @Override
            public void run() {
                Thread.currentThread().setName("journalPublisher");
                var1_1 = CrushSyncScanner.this.tempJournal;
                synchronized (var1_1) {
                    v = new Vector<E>();
                    try {
                        CrushSyncScanner.this.loadVector(v);
                    }
                    catch (Exception e) {
                        CrushSyncScanner.this.msg(e);
                    }
                    CrushSyncScanner.this.msg("Loaded " + v.size() + " items that were still pending from the last sync attempt. (tempJournalProcessing.obj)", false);
                    v.addAll(CrushSyncScanner.this.tempJournal);
                    CrushSyncScanner.this.tempJournal.removeAllElements();
                    CrushSyncScanner.this.tempJournal.addAll(v);
                    // MONITOREXIT @DISABLED, blocks:[0, 6] lbl17 : MonitorExitStatement: MONITOREXIT : var1_1
                    if (true) ** GOTO lbl46
                }
                do {
                    try {
                        var1_1 = CrushSyncScanner.this.tempJournal;
                        synchronized (var1_1) {
                            CrushSyncScanner.this.storeVector(CrushSyncScanner.this.tempJournal);
                        }
                        try {
                            CrushSyncScanner.this.playLocalJournalToRemote();
                        }
                        catch (Exception e) {
                            CrushSyncScanner.this.msg(e);
                            e.printStackTrace();
                        }
                        CrushSyncScanner.this.uploadsWaiting = false;
                        CrushSyncScanner.this.storeVector(CrushSyncScanner.this.tempJournal);
                    }
                    catch (Exception e) {
                        CrushSyncScanner.this.msg(e);
                    }
                    try {
                        Thread.sleep(1000L);
                    }
                    catch (Exception var1_4) {
                        // empty catch block
                    }
lbl46:
                    // 3 sources

                } while (!CrushSyncScanner.this.stop);
            }
        }).start();
        final CrushSyncScanner scanner = this;
        new Thread(new Runnable(){

            @Override
            public void run() {
                Thread.currentThread().setName("journalUpdater");
                while (!CrushSyncScanner.this.stop) {
                    try {
                        if (CrushSyncScanner.this.playRemoteJournalLoops++ > CrushSyncScanner.this.remoteRefreshSeconds) {
                            CrushSyncScanner.this.playRemoteJournalLoops = 0;
                            Vector v = CrushSyncScanner.this.getRemoteTableData("journal", Long.parseLong(CrushSyncScanner.this.prefs.getProperty("remote_last_rid", "0")), CrushSyncDaemon.thisObj.prefs.getProperty("clientid"), "", "");
                            if (v.size() > 0) {
                                CrushSyncScanner.this.msg("Done get journal:" + v.size(), false);
                            }
                            CrushSyncScanner.playRemoteJournal(scanner, v);
                        }
                        try {
                            CrushSyncScanner.this.stripOldKeys(CrushSyncScanner.this.ignoreDownload);
                            CrushSyncScanner.this.stripOldKeys(CrushSyncScanner.this.ignoreRename);
                            CrushSyncScanner.this.stripOldKeys(CrushSyncScanner.this.ignoreDelete);
                        }
                        catch (Exception e) {
                            CrushSyncScanner.this.msg(e);
                        }
                    }
                    catch (Exception e) {
                        String msg = "Remote system " + CrushSyncDaemon.thisObj.prefs.getProperty("syncUrl") + " is unavailable at the moment...(" + e.getMessage() + ")";
                        CrushSyncScanner.this.msg(msg, false);
                        CrushSyncScanner.this.statusInfo.put("syncStatus", msg);
                        CrushSyncScanner.this.msg(e);
                    }
                    try {
                        Thread.sleep(1000L);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
        }).start();
    }

    public void stripOldKeys(Properties p) {
        Enumeration<Object> keys = p.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            long time = Long.parseLong(p.getProperty(key));
            if (System.currentTimeMillis() - time <= (long)(this.stableSeconds * 2 * 1000)) continue;
            p.remove(key);
        }
    }

    public static void playRemoteJournal(CrushSyncScanner scanner, Vector v) throws Exception {
        if (scanner.uploadsWaiting) {
            scanner.statusInfo.put("syncStatus", "Waiting for uploads to complete before synching journal...");
            return;
        }
        scanner.statusInfo.put("syncStatus", "Synching remote changes.");
        if (v.size() > 0) {
            scanner.msg("Playing remote journal:" + v.size(), false);
        }
        Vector files2 = new Vector();
        Vector alreadyProcessed = new Vector();
        String remote_last_rid = scanner.prefs.getProperty("remote_last_rid", "-1");
        boolean processedItem = v.size() > 0;
        int skip_offset = 0;
        while (v.size() > skip_offset) {
            Properties p = (Properties)v.elementAt(skip_offset);
            if (p.getProperty("EVENT_TYPE", "").equalsIgnoreCase("SETTINGS")) {
                v.remove(skip_offset);
                CrushSyncDaemon.thisObj.processSetting(p);
                continue;
            }
            try {
                if (Long.parseLong(p.getProperty("RID", "0")) > Long.parseLong(remote_last_rid)) {
                    remote_last_rid = p.getProperty("RID");
                    scanner.msg("remote_last_rid now:" + remote_last_rid, true);
                }
                if (p.getProperty("EVENT_TYPE").equalsIgnoreCase("change")) {
                    if (!p.containsKey("ITEM_MODIFIED")) {
                        v.remove(skip_offset);
                        continue;
                    }
                    try {
                        scanner.doRemoteChangedFile(p, alreadyProcessed, files2);
                    }
                    catch (IOException e) {
                        if (("" + e).indexOf("being used") >= 0) {
                            ++skip_offset;
                            continue;
                        }
                        throw e;
                    }
                } else if (p.getProperty("EVENT_TYPE").equalsIgnoreCase("conflict")) {
                    if (!p.getProperty("ITEM_PATH").toLowerCase().startsWith(scanner.prefs.getProperty("syncServerPath").toLowerCase())) {
                        v.remove(skip_offset);
                        continue;
                    }
                    boolean ignoreConflict = false;
                    int x = skip_offset;
                    while (x < v.size()) {
                        Properties pp = (Properties)v.elementAt(x);
                        if (p.getProperty("ITEM_PATH", "").equals(pp.getProperty("ITEM_PATH", "")) && (pp.getProperty("EVENT_TYPE").equalsIgnoreCase("rename") || pp.getProperty("EVENT_TYPE").equalsIgnoreCase("change") || pp.getProperty("EVENT_TYPE").equalsIgnoreCase("delete"))) {
                            ignoreConflict = true;
                        }
                        ++x;
                    }
                    if (ignoreConflict) {
                        scanner.msg("Conflict being ignored since its already been dealt with:" + p.getProperty("ITEM_PATH"), false);
                        v.remove(skip_offset);
                        continue;
                    }
                    scanner.msg("Conflict:" + p.getProperty("ITEM_PATH"), false);
                    SnapshotFile f2 = new SnapshotFile(String.valueOf(scanner.syncPath) + p.getProperty("ITEM_PATH").substring(scanner.prefs.getProperty("syncServerPath").length()));
                    if (f2.getName().indexOf(".") > 0 && f2.isFile() && f2.getName().indexOf(".conflict") < 0) {
                        SnapshotFile conflict0 = new SnapshotFile(String.valueOf(scanner.syncPath) + (String.valueOf(p.getProperty("ITEM_PATH").substring(0, p.getProperty("ITEM_PATH").lastIndexOf("."))) + ".conflict0" + p.getProperty("ITEM_PATH").substring(p.getProperty("ITEM_PATH").lastIndexOf("."))).substring(scanner.prefs.getProperty("syncServerPath").length()));
                        SnapshotFile conflict1 = new SnapshotFile(String.valueOf(scanner.syncPath) + (String.valueOf(p.getProperty("ITEM_PATH").substring(0, p.getProperty("ITEM_PATH").lastIndexOf("."))) + ".conflict1" + p.getProperty("ITEM_PATH").substring(p.getProperty("ITEM_PATH").lastIndexOf("."))).substring(scanner.prefs.getProperty("syncServerPath").length()));
                        SnapshotFile conflict2 = new SnapshotFile(String.valueOf(scanner.syncPath) + (String.valueOf(p.getProperty("ITEM_PATH").substring(0, p.getProperty("ITEM_PATH").lastIndexOf("."))) + ".conflict2" + p.getProperty("ITEM_PATH").substring(p.getProperty("ITEM_PATH").lastIndexOf("."))).substring(scanner.prefs.getProperty("syncServerPath").length()));
                        scanner.ignoreRename.put(f2.getCanonicalPath(), String.valueOf(System.currentTimeMillis()));
                        f2.renameTo(conflict0);
                        Vector files3 = new Vector();
                        scanner.doRemoteChangedFile(p, alreadyProcessed, files3);
                        try {
                            scanner.downloadChanges(files3, 0, false);
                            scanner.ignoreRename.put(f2.getCanonicalPath(), String.valueOf(System.currentTimeMillis()));
                            f2.renameTo(conflict1);
                            scanner.downloadChanges(files3, 1, false);
                            scanner.ignoreRename.put(f2.getCanonicalPath(), String.valueOf(System.currentTimeMillis()));
                            f2.renameTo(conflict2);
                        }
                        catch (Exception e) {
                            scanner.msg(e);
                        }
                    }
                } else if (p.getProperty("EVENT_TYPE").equalsIgnoreCase("delete") && scanner.prefs.getProperty("honor_deletes", "true").equalsIgnoreCase("true")) {
                    if (!p.getProperty("ITEM_PATH").toLowerCase().startsWith(scanner.prefs.getProperty("syncServerPath").toLowerCase())) {
                        v.remove(skip_offset);
                        continue;
                    }
                    String path = Common.dots(p.getProperty("ITEM_PATH").substring(scanner.prefs.getProperty("syncServerPath").length() - 1));
                    SnapshotFile f1 = new SnapshotFile(String.valueOf(scanner.syncPath) + path);
                    alreadyProcessed.removeAllElements();
                    if (f1.exists()) {
                        scanner.ignoreDelete.put(f1.getCanonicalPath(), String.valueOf(System.currentTimeMillis()));
                        Common.recurseDelete(f1.getPath(), false);
                        scanner.msg("Remote:" + p.getProperty("EVENT_TYPE") + ":" + path, false);
                        int xx = 0;
                        while (xx < 10 && new SnapshotFile(f1.getPath()).exists()) {
                            Thread.sleep(3000L);
                            scanner.msg("Attempting to delete item again after prior failure due to locking (" + (xx + 1) + "/10):" + f1.getPath(), false);
                            Common.recurseDelete(f1.getPath(), false);
                            ++xx;
                        }
                        if (new SnapshotFile(f1.getPath()).exists()) {
                            scanner.msg("Item not entirely deleted, re-uploading back to the server:" + f1.getPath(), false);
                            scanner.doFileChanged(f1);
                        }
                    }
                } else if (p.getProperty("EVENT_TYPE").equalsIgnoreCase("rename")) {
                    if (!p.getProperty("ITEM_PATH").toLowerCase().split(";")[0].startsWith(scanner.prefs.getProperty("syncServerPath").toLowerCase())) {
                        v.remove(skip_offset);
                        continue;
                    }
                    String path1 = Common.dots(p.getProperty("ITEM_PATH").split(";")[0].substring(scanner.prefs.getProperty("syncServerPath").length() - 1));
                    SnapshotFile f1 = new SnapshotFile(String.valueOf(scanner.syncPath) + path1);
                    alreadyProcessed.removeAllElements();
                    if (f1.exists()) {
                        scanner.ignoreRename.put(f1.getCanonicalPath(), String.valueOf(System.currentTimeMillis()));
                        String path2 = Common.dots(p.getProperty("ITEM_PATH").split(";")[1].substring(scanner.prefs.getProperty("syncServerPath").length() - 1));
                        SnapshotFile f2 = new SnapshotFile(String.valueOf(scanner.syncPath) + path2);
                        scanner.ignoreRename.put(f2.getCanonicalPath(), String.valueOf(System.currentTimeMillis()));
                        f1.renameTo(f2);
                        f2.setLastModified(f1.lastModified());
                        scanner.msg("Remote:" + p.getProperty("EVENT_TYPE") + ":" + path1 + " to " + path2, false);
                    } else {
                        if (!p.containsKey("ITEM_MODIFIED")) {
                            v.remove(skip_offset);
                            continue;
                        }
                        if (p.getProperty("ITEM_PATH").split(";").length > 1) {
                            p.put("ITEM_PATH", p.getProperty("ITEM_PATH").split(";")[1]);
                            scanner.doRemoteChangedFile(p, alreadyProcessed, files2);
                        } else {
                            scanner.msg("Ignoring item since its path is invalid : " + p.getProperty("ITEM_PATH"), false);
                        }
                    }
                }
                v.remove(skip_offset);
            }
            catch (Exception e) {
                scanner.msg("Error: Lost network connection, will automatically retry.", false);
                scanner.msg(p.toString(), true);
                scanner.msg(e);
                Thread.sleep(2000L);
            }
        }
        if (scanner.ignoreRename.size() > 0 || scanner.ignoreDelete.size() > 0) {
            Thread.sleep(scanner.stableSeconds / 2 * 1000);
            scanner.ignoreRename.clear();
            scanner.ignoreDelete.clear();
        }
        while (scanner.tempJournal.size() > 0) {
            Thread.sleep(1000L);
        }
        if (files2.size() > 0) {
            if (scanner.stop) {
                throw new Exception("Stopped.");
            }
            scanner.downloadChanges(files2, -1, false);
            Thread.sleep(scanner.stableSeconds / 2 * 1000);
            scanner.ignoreDownload.clear();
        }
        scanner.prefs.put("remote_last_rid", remote_last_rid);
        if (processedItem) {
            scanner.prefs.put("saveNow", String.valueOf(System.currentTimeMillis()));
        }
        scanner.statusInfo.put("syncStatus", "Active");
    }

    public void stop() throws Exception {
        this.stop = true;
        this.msg("Stopping live monitor on:" + this.syncPath, false);
        this.msg(new Exception("Stop sync thread trace."));
        try {
            if (this.watcherWrapper != null) {
                this.watcherWrapper.stop();
            }
        }
        catch (Exception e) {
            this.msg(e);
        }
        this.statusInfo.put("syncStatus", "Stopped");
        while (this.clientCache.size() > 0) {
            this.getClient().logout();
        }
    }

    public static String u(String s) throws IOException {
        String r = Common.makeBoundary(20);
        s = s.replaceAll(" ", r);
        s = URLEncoder.encode(s, "utf-8");
        s = s.replaceAll(r, "%20");
        return s;
    }

    public void conflict(String path) throws Exception {
        int loops = 0;
        HttpURLConnection urlc = null;
        while (!this.stop) {
            ++loops;
            try {
                URL u = new URL(String.valueOf(CrushSyncDaemon.thisObj.prefs.getProperty("syncUrl")) + this.prefs.getProperty("syncServerPath").substring(1));
                urlc = (HttpURLConnection)u.openConnection();
                urlc.setReadTimeout(600000);
                urlc.setRequestMethod("POST");
                urlc.setRequestProperty("Cookie", "CrushAuth=" + this.crushAuth.toString() + ";");
                urlc.setUseCaches(false);
                urlc.setDoOutput(true);
                urlc.getOutputStream().write(("c2f=" + this.crushAuth.toString().substring(this.crushAuth.toString().length() - 4) + "&command=syncConflict&table=journal&path=" + CrushSyncScanner.u(this.prefs.getProperty("syncServerPath")) + "&item_path=" + CrushSyncScanner.u(path)).getBytes("UTF8"));
                int code = urlc.getResponseCode();
                if (code >= 200 && code < 300) {
                    return;
                }
                throw new Exception("HTTP Code incorrect:" + code);
            }
            catch (Exception e2) {
                this.statusInfo.put("syncStatus", "Error...server is offline.");
                if (loops < 3) {
                    this.msg(e2);
                } else if (loops == 6) {
                    this.msg(e2.getMessage(), false);
                } else {
                    this.msg(e2.getMessage(), true);
                }
                if (urlc != null) {
                    urlc.disconnect();
                }
                this.crushAuth.setLength(0);
                CrushSyncDaemon.thisObj.checkAuth();
                try {
                    Thread.sleep(10000L);
                }
                catch (Exception e2) {
                    // empty catch block
                }
            }
        }
        try {
            this.stop();
        }
        catch (Exception e) {
            this.msg(e);
        }
        throw new Exception("Sync Stopped");
    }

    public Vector getRemoteTableData(String table, long rid, String clientid, String paths, String prior_md5s_item_path) throws Exception {
        int loops = 0;
        HttpURLConnection urlc = null;
        while (!this.stop) {
            ++loops;
            try {
                URL u = new URL(String.valueOf(CrushSyncDaemon.thisObj.prefs.getProperty("syncUrl")) + this.prefs.getProperty("syncServerPath").substring(1));
                urlc = (HttpURLConnection)u.openConnection();
                urlc.setReadTimeout(120000);
                urlc.setRequestMethod("POST");
                urlc.setRequestProperty("Cookie", "CrushAuth=" + this.crushAuth.toString() + ";");
                urlc.setUseCaches(false);
                urlc.setDoOutput(true);
                urlc.getOutputStream().write(("c2f=" + this.crushAuth.toString().substring(this.crushAuth.toString().length() - 4) + "&command=getSyncTableData&table=" + table + "&lastRID=" + rid + "&path=" + CrushSyncScanner.u(this.prefs.getProperty("syncServerPath")) + "&clientid=" + clientid + "&paths=" + CrushSyncScanner.u(paths) + "&prior_md5s_item_path=" + CrushSyncScanner.u(prior_md5s_item_path)).getBytes("UTF8"));
                urlc.getResponseCode();
                ObjectInputStream ois = new ObjectInputStream(urlc.getInputStream());
                Vector v = (Vector)ois.readObject();
                ois.close();
                return v;
            }
            catch (Exception e2) {
                if (loops < 3) {
                    this.msg(e2);
                } else if (loops == 6) {
                    this.msg(e2.getMessage(), false);
                } else {
                    this.statusInfo.put("syncStatus", "Error...server is offline.");
                    this.msg(e2.getMessage(), true);
                    this.crushAuth.setLength(0);
                }
                if (urlc != null) {
                    urlc.disconnect();
                }
                CrushSyncDaemon.thisObj.checkAuth();
                try {
                    Thread.sleep(10000L);
                }
                catch (Exception e2) {
                    // empty catch block
                }
            }
        }
        try {
            this.stop();
        }
        catch (Exception e) {
            this.msg(e);
        }
        throw new Exception("Sync Stopped");
    }

    public void msg(Exception e) {
        StackTraceElement[] ste = e.getStackTrace();
        this.msg(e.toString(), true);
        int x = 0;
        while (x < ste.length) {
            this.msg(String.valueOf(ste[x].getClassName()) + "." + ste[x].getMethodName() + ":" + ste[x].getLineNumber(), true);
            ++x;
        }
    }

    public void msg(String s, boolean diskOnly) {
        if (!diskOnly) {
            this.log.addElement(new Date() + ":" + this.syncPath + ":" + s);
        }
        logDisk.add(new Date() + ":" + this.syncPath + ":" + s);
        while (this.log.size() > 500) {
            this.log.removeElementAt(0);
        }
        while (logDisk.size() > 500) {
            logDisk.removeElementAt(0);
        }
    }

    public static void getAllFileListing(Vector list, String path, int depth, boolean includeFolders) throws Exception {
        SnapshotFile item = new SnapshotFile(path);
        if (item.isFile()) {
            list.addElement(item);
        } else {
            CrushSyncScanner.appendListing(path, list, "", depth, includeFolders);
        }
    }

    public static void appendListing(String path, Vector list, String dir, int depth, boolean includeFolders) throws Exception {
        String[] items;
        if (depth == 0) {
            return;
        }
        --depth;
        if (!path.endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        if ((items = new SnapshotFile(String.valueOf(path) + dir).list()) == null) {
            return;
        }
        int x = 0;
        while (x < items.length) {
            SnapshotFile item = new SnapshotFile(String.valueOf(path) + dir + items[x]);
            if (item.isFile() || includeFolders) {
                if (item.lastModified() < 172800000L) {
                    item.setLastModified(new SimpleDateFormat("MM/dd/yy").parse("04/10/1998").getTime());
                    item = new SnapshotFile(String.valueOf(path) + dir + items[x]);
                }
                list.addElement(item);
            }
            if (item.isDirectory()) {
                CrushSyncScanner.appendListing(path, list, String.valueOf(dir) + items[x] + "/", depth, includeFolders);
            }
            ++x;
        }
        if (items.length == 0) {
            list.addElement(new SnapshotFile(String.valueOf(path) + dir));
        }
    }
}

