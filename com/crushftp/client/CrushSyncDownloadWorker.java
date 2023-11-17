/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.CrushSyncScanner;
import com.crushftp.client.GenericClient;
import com.crushftp.client.SnapshotFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Vector;
import javax.swing.JOptionPane;

public class CrushSyncDownloadWorker
implements Runnable {
    CrushSyncScanner sc = null;
    Vector files_loop = null;
    Vector remotePaths = null;
    Vector threads_used = null;
    Vector threads_free = null;
    Vector downloadFiles = null;
    Vector errors = null;
    boolean consumeErrors = false;
    int rev = -1;

    public CrushSyncDownloadWorker(CrushSyncScanner sc, Vector files_loop, Vector remotePaths, Vector threads_used, Vector threads_free, Vector downloadFiles, Vector errors, boolean consumeErrors, int rev) {
        this.sc = sc;
        this.files_loop = files_loop;
        this.remotePaths = remotePaths;
        this.threads_used = threads_used;
        this.threads_free = threads_free;
        this.downloadFiles = downloadFiles;
        this.errors = errors;
        this.consumeErrors = consumeErrors;
        this.rev = rev;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        block56: {
            SnapshotFile f;
            block53: {
                f = null;
                Vector vector = this.files_loop;
                synchronized (vector) {
                    if (this.files_loop.size() > 0) {
                        f = (SnapshotFile)this.files_loop.remove(0);
                    }
                }
                if (f != null) break block53;
                Vector vector2 = this.threads_free;
                synchronized (vector2) {
                    this.threads_free.addElement(this);
                    this.threads_used.remove(this);
                }
                return;
            }
            try {
                try {
                    String remotePath;
                    GenericClient c;
                    block54: {
                        Thread.currentThread().setName("Download item worker:" + f);
                        int retries = 0;
                        c = this.sc.getClient();
                        remotePath = f.getPath().replace('\\', '/');
                        try {
                            String localPath = new SnapshotFile(String.valueOf(this.sc.syncPath) + remotePath.substring(this.sc.prefs.getProperty("syncServerPath").length())).getCanonicalPath();
                            if (f.isFile()) {
                                boolean renamed;
                                Vector<String> byteRanges = new Vector<String>();
                                byteRanges.addElement("0--1");
                                this.sc.ignoreDownload.put(Common.all_but_last(localPath), String.valueOf(System.currentTimeMillis()));
                                new SnapshotFile(Common.all_but_last(localPath)).mkdirs();
                                if (new SnapshotFile(localPath).exists() && new SnapshotFile(localPath).isDirectory()) break block54;
                                new SnapshotFile(localPath).renameTo(new SnapshotFile(String.valueOf(localPath) + ".downloading"));
                                RandomAccessFile out = new RandomAccessFile(String.valueOf(localPath) + ".downloading", "rw");
                                if (f.length() > 0xA00000L && this.rev < 0) {
                                    if (out.length() > f.length()) {
                                        out.setLength(f.length());
                                    }
                                    if (this.sc.prefs.getProperty("size_only", "false").equals("false")) {
                                        long offset = this.sc.getMd5Diff(String.valueOf(localPath) + ".downloading", remotePath, byteRanges, f.length());
                                        Vector vector = this.files_loop;
                                        synchronized (vector) {
                                            this.sc.transferedBytes += offset;
                                        }
                                    }
                                } else {
                                    out.setLength(0L);
                                }
                                this.sc.downloadStatus.put("transferedBytes", String.valueOf(this.sc.transferedBytes));
                                int x = 0;
                                while (x < byteRanges.size()) {
                                    long start = Long.parseLong(byteRanges.elementAt(x).toString().substring(0, byteRanges.elementAt(x).toString().indexOf("-")));
                                    long end = -1L;
                                    String endPart = byteRanges.elementAt(x).toString().substring(byteRanges.elementAt(x).toString().indexOf("-") + 1);
                                    if (!endPart.equals("")) {
                                        end = Long.parseLong(endPart);
                                    }
                                    InputStream in = null;
                                    if (this.remotePaths.indexOf(remotePath) < 0) {
                                        this.remotePaths.addElement(remotePath);
                                    }
                                    boolean skipDownload = false;
                                    try {
                                        c.setConfig("timeout", "20000");
                                        in = this.rev < 0 ? c.download(remotePath, start, end, true) : c.download("/?c2f=" + this.sc.crushAuth.toString().substring(this.sc.crushAuth.toString().length() - 4) + "&command=download&META_downloadRevision=" + this.rev + "&path=" + Common.url_encode(remotePath), 0L, -1L, true);
                                    }
                                    catch (IOException e) {
                                        if (e.getMessage().startsWith("404:") || e.getMessage().startsWith("403:")) {
                                            out.close();
                                            out = null;
                                            if (new SnapshotFile(String.valueOf(localPath) + ".downloading").length() == 0L) {
                                                new SnapshotFile(String.valueOf(localPath) + ".downloading").delete();
                                            }
                                            skipDownload = true;
                                        }
                                        throw e;
                                    }
                                    this.sc.ignoreDownload.put(localPath, String.valueOf(System.currentTimeMillis()));
                                    this.sc.ignoreDownload.put(String.valueOf(localPath) + ".downloading", String.valueOf(System.currentTimeMillis()));
                                    if (!skipDownload) {
                                        out.seek(start);
                                        long temp_bytes = 0L;
                                        try {
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
                                                this.sc.downloadStatus.put("transferedBytes", String.valueOf(this.sc.transferedBytes));
                                                this.sc.ignoreDownload.put(localPath, String.valueOf(System.currentTimeMillis()));
                                                this.sc.ignoreDownload.put(String.valueOf(localPath) + ".downloading", String.valueOf(System.currentTimeMillis()));
                                            }
                                            retries = 0;
                                            this.sc.downloadStatus.put("transferedItems", String.valueOf(this.downloadFiles.size() - this.files_loop.size()));
                                        }
                                        catch (Exception e) {
                                            Vector vector = this.files_loop;
                                            synchronized (vector) {
                                                this.sc.transferedBytes -= temp_bytes;
                                            }
                                            this.sc.uploadStatus.put("transferedBytes", String.valueOf(this.sc.transferedBytes));
                                            if (retries++ > 5) {
                                                c.close();
                                                throw e;
                                            }
                                            this.files_loop.insertElementAt(f, 0);
                                        }
                                    }
                                    c.close();
                                    ++x;
                                }
                                if (out != null) {
                                    out.close();
                                }
                                if (new SnapshotFile(String.valueOf(localPath) + ".downloading").exists()) {
                                    new SnapshotFile(String.valueOf(localPath) + ".downloading").setLastModified(f.lastModified());
                                }
                                if (!(renamed = new SnapshotFile(String.valueOf(localPath) + ".downloading").renameTo(new SnapshotFile(localPath))) && new SnapshotFile(localPath).exists() && !new SnapshotFile(localPath).isDirectory()) {
                                    this.sc.conflict(remotePath);
                                    final String localPathThread = localPath;
                                    new Thread(new Runnable(){

                                        @Override
                                        public void run() {
                                            JOptionPane.showMessageDialog(null, "Conflict detected with file:" + localPathThread + "\r\nAnother user had made changes.");
                                        }
                                    }).start();
                                }
                                break block54;
                            }
                            new SnapshotFile(localPath).setLastModified(f.lastModified());
                        }
                        catch (IOException e) {
                            if (!this.consumeErrors) {
                                throw e;
                            }
                            this.sc.pathErrors.addElement(e + ":" + f);
                        }
                    }
                    this.sc.releaseClient(c);
                    this.remotePaths.remove(remotePath);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    this.errors.addElement(e);
                    Vector vector = this.threads_free;
                    synchronized (vector) {
                        this.threads_free.addElement(this);
                        this.threads_used.remove(this);
                        break block56;
                    }
                }
            }
            catch (Throwable throwable) {
                Vector vector = this.threads_free;
                synchronized (vector) {
                    this.threads_free.addElement(this);
                    this.threads_used.remove(this);
                }
                throw throwable;
            }
            Vector vector = this.threads_free;
            synchronized (vector) {
                this.threads_free.addElement(this);
                this.threads_used.remove(this);
            }
        }
    }
}

