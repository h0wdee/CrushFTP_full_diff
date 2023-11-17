/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.CrushSyncScanner;
import com.crushftp.client.GenericClient;
import com.crushftp.client.SnapshotFile;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Vector;
import javax.swing.JOptionPane;

public class CrushSyncUploadWorker
implements Runnable {
    CrushSyncScanner sc = null;
    Vector files_loop = null;
    Vector remotePaths = null;
    Vector threads_free = null;
    Vector threads_used = null;
    Vector delete_folders = null;
    Vector files2 = null;
    Vector errors = null;
    boolean foldersOnly = false;

    public CrushSyncUploadWorker(CrushSyncScanner sc, Vector files_loop, Vector remotePaths, Vector threads_free, Vector threads_used, Vector delete_folders, Vector files2, Vector errors, boolean foldersOnly) {
        this.sc = sc;
        this.files_loop = files_loop;
        this.remotePaths = remotePaths;
        this.threads_free = threads_free;
        this.threads_used = threads_used;
        this.delete_folders = delete_folders;
        this.files2 = files2;
        this.errors = errors;
        this.foldersOnly = foldersOnly;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        block57: {
            SnapshotFile f;
            String remotePath;
            block55: {
                remotePath = "";
                f = null;
                Vector vector = this.files_loop;
                synchronized (vector) {
                    if (this.files_loop.size() > 0) {
                        f = (SnapshotFile)this.files_loop.remove(0);
                    }
                }
                if (f != null) break block55;
                this.remotePaths.remove(remotePath);
                Vector vector2 = this.threads_free;
                synchronized (vector2) {
                    this.threads_free.addElement(this);
                    this.threads_used.remove(this);
                }
                return;
            }
            try {
                try {
                    Thread.currentThread().setName("Upload item worker:" + f);
                    int retries = 0;
                    GenericClient c = this.sc.getClient();
                    String localPath = f.getPath().replace('\\', '/');
                    if (f.isDirectory() && !localPath.endsWith("/")) {
                        localPath = String.valueOf(localPath) + "/";
                    }
                    if (!localPath.startsWith("/")) {
                        localPath = "/" + localPath;
                    }
                    remotePath = String.valueOf(this.sc.prefs.getProperty("syncServerPath")) + localPath.substring(this.sc.syncPath.length());
                    if (f.isDirectory() || new SnapshotFile(localPath).isDirectory()) {
                        if (!this.sc.lastUploadFolderList.containsKey(remotePath)) {
                            c.makedirs(remotePath);
                            c.mdtm(remotePath, f.lastModified());
                            this.sc.lastUploadFolderList.put(remotePath, (Object)System.currentTimeMillis());
                        }
                    } else if (!this.foldersOnly) {
                        if (this.remotePaths.indexOf(remotePath) < 0) {
                            this.remotePaths.addElement(remotePath);
                        }
                        Vector<String> byteRanges = new Vector<String>();
                        byteRanges.addElement("0--1");
                        if (new SnapshotFile(localPath).exists()) {
                            if (f.length() > 0xA00000L && this.sc.prefs.getProperty("size_only", "false").equals("false")) {
                                long offset = this.sc.getMd5Diff(localPath, remotePath, byteRanges, f.length());
                                Vector vector = this.files_loop;
                                synchronized (vector) {
                                    this.sc.transferedBytes += offset;
                                }
                            }
                            this.sc.uploadStatus.put("transferedBytes", String.valueOf(this.sc.transferedBytes));
                            int x = 0;
                            while (x < byteRanges.size()) {
                                long start = Long.parseLong(byteRanges.elementAt(x).toString().substring(0, byteRanges.elementAt(x).toString().indexOf("-")));
                                long end = -1L;
                                String endPart = byteRanges.elementAt(x).toString().substring(byteRanges.elementAt(x).toString().indexOf("-") + 1);
                                if (!endPart.equals("")) {
                                    end = Long.parseLong(endPart);
                                }
                                OutputStream out = c.upload(remotePath, start, end == -1L, true);
                                RandomAccessFile in = new RandomAccessFile(localPath, "r");
                                long temp_bytes = 0L;
                                try {
                                    in.seek(start);
                                    byte[] b = new byte[65535];
                                    int bytesRead = 0;
                                    while (bytesRead >= 0 && !this.sc.stop) {
                                        bytesRead = in.read(b);
                                        if (bytesRead < 0) continue;
                                        out.write(b, 0, bytesRead);
                                        Vector vector = this.files_loop;
                                        synchronized (vector) {
                                            this.sc.transferedBytes += (long)bytesRead;
                                        }
                                        temp_bytes += (long)bytesRead;
                                        this.sc.uploadStatus.put("transferedBytes", String.valueOf(this.sc.transferedBytes));
                                    }
                                    c.close();
                                    retries = 0;
                                }
                                catch (Exception e) {
                                    in.close();
                                    Vector vector = this.files_loop;
                                    synchronized (vector) {
                                        this.sc.transferedBytes -= temp_bytes;
                                    }
                                    this.sc.uploadStatus.put("transferedBytes", String.valueOf(this.sc.transferedBytes));
                                    this.sc.msg(e);
                                    if (retries++ < 20) {
                                        this.files_loop.insertElementAt(f, 0);
                                    }
                                    if (("" + e).indexOf("ERROR:") >= 0 || retries >= 20) {
                                        if (("" + e).toUpperCase().indexOf("NOT ALLOWED") >= 0) {
                                            this.sc.prefs.put("readOnly", "true");
                                            JOptionPane.showMessageDialog(null, "" + e);
                                        }
                                        c.close();
                                        throw new Exception(e);
                                    }
                                    if (("" + e).toUpperCase().indexOf("ACCESS DENIED") >= 0) {
                                        try {
                                            c.close();
                                        }
                                        catch (Exception exception) {
                                            // empty catch block
                                        }
                                        this.sc.releaseClient(c);
                                        c = this.sc.getClient();
                                        c.makedirs(Common.all_but_last(remotePath));
                                        Thread.sleep(1000L);
                                        this.errors.addElement(e);
                                    }
                                    c.close();
                                }
                                in.close();
                                ++x;
                            }
                            c.mdtm(remotePath, f.lastModified());
                        }
                    }
                    if (!this.foldersOnly && this.sc.prefs.getProperty("upload_delete", "false").equals("true")) {
                        this.sc.ignoreDelete.put(f.getCanonicalPath(), String.valueOf(System.currentTimeMillis()));
                        f.delete();
                    } else if (!this.foldersOnly && this.sc.prefs.getProperty("move_after_sync", "false").equals("true")) {
                        new SnapshotFile(String.valueOf(this.sc.prefs.getProperty("move_after_sync_path", "")) + Common.all_but_last(localPath.substring(this.sc.syncPath.length()))).mkdirs();
                        Common.copy(f.getPath(), String.valueOf(this.sc.prefs.getProperty("move_after_sync_path", "")) + localPath.substring(this.sc.syncPath.length()), true);
                        this.sc.ignoreDelete.put(f.getCanonicalPath(), String.valueOf(System.currentTimeMillis()));
                        if (this.delete_folders.indexOf(f.getParentFile()) < 0) {
                            this.delete_folders.addElement(f.getParentFile());
                        }
                        f.delete();
                    }
                    if (!this.foldersOnly) {
                        this.sc.uploadStatus.put("transferedItems", String.valueOf(this.files2.size() - this.files_loop.size()));
                    }
                    this.sc.releaseClient(c);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    this.errors.addElement(e);
                    this.remotePaths.remove(remotePath);
                    Vector vector = this.threads_free;
                    synchronized (vector) {
                        this.threads_free.addElement(this);
                        this.threads_used.remove(this);
                        break block57;
                    }
                }
            }
            catch (Throwable throwable) {
                this.remotePaths.remove(remotePath);
                Vector vector = this.threads_free;
                synchronized (vector) {
                    this.threads_free.addElement(this);
                    this.threads_used.remove(this);
                }
                throw throwable;
            }
            this.remotePaths.remove(remotePath);
            Vector vector = this.threads_free;
            synchronized (vector) {
                this.threads_free.addElement(this);
                this.threads_used.remove(this);
            }
        }
    }
}

