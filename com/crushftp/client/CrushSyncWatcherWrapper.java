/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.teamdev.filewatch.FileEvent$Added
 *  com.teamdev.filewatch.FileEvent$Changed
 *  com.teamdev.filewatch.FileEvent$Deleted
 *  com.teamdev.filewatch.FileEvent$Renamed
 *  com.teamdev.filewatch.FileEventsListener
 *  com.teamdev.filewatch.FileWatcher
 *  com.teamdev.filewatch.WatchingAttributes
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.CrushSyncScanner;
import com.crushftp.client.SnapshotFile;
import com.teamdev.filewatch.FileEvent;
import com.teamdev.filewatch.FileEventsListener;
import com.teamdev.filewatch.FileWatcher;
import com.teamdev.filewatch.WatchingAttributes;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class CrushSyncWatcherWrapper {
    private FileWatcher watchService = null;
    private CrushSyncScanner scanner = null;
    Socket sock = null;

    public CrushSyncWatcherWrapper(CrushSyncScanner scanner) {
        this.scanner = scanner;
    }

    public void startLiveMonitor() throws Exception {
        this.scanner.msg("Starting live monitor on:" + this.scanner.syncPath, false);
        if (Common.machine_is_x()) {
            try {
                this.sock = new Socket("127.0.0.1", 15151);
                this.scanner.msg("fswatch dameon connected.", false);
                new Thread(new Runnable(){

                    /*
                     * Enabled aggressive block sorting
                     * Enabled unnecessary exception pruning
                     * Enabled aggressive exception aggregation
                     */
                    @Override
                    public void run() {
                        block20: {
                            Thread.currentThread().setName("fswatch socket thread");
                            try {
                                BufferedReader br = new BufferedReader(new InputStreamReader(CrushSyncWatcherWrapper.this.sock.getInputStream()));
                                String data = "";
                                String prior_rename = "";
                                String watch_path = String.valueOf(new SnapshotFile(((CrushSyncWatcherWrapper)CrushSyncWatcherWrapper.this).scanner.syncPath).getCanonicalPath()) + "/";
                                while (true) {
                                    block21: {
                                        if (((CrushSyncWatcherWrapper)CrushSyncWatcherWrapper.this).scanner.stop || (data = br.readLine()) == null) {
                                            if (!((CrushSyncWatcherWrapper)CrushSyncWatcherWrapper.this).scanner.stop) {
                                                CrushSyncWatcherWrapper.this.scanner.msg("fswatch socket closed, aborting scan.", false);
                                            }
                                            break;
                                        }
                                        System.out.println(data);
                                        String file_path = data.substring(2);
                                        if (file_path.startsWith("/System/Volumes/Data/")) {
                                            file_path = file_path.substring("/System/Volumes/Data".length());
                                        }
                                        if (file_path.startsWith(watch_path)) {
                                            if (data.charAt(0) == '0' || data.charAt(0) == '7') {
                                                CrushSyncWatcherWrapper.this.added(new SnapshotFile(new File(file_path)));
                                            } else if (data.charAt(0) == '1') {
                                                CrushSyncWatcherWrapper.this.deleted(new SnapshotFile(new File(file_path)));
                                            } else if (data.charAt(0) == '3') {
                                                if (prior_rename.equals("")) {
                                                    if (new SnapshotFile(data.substring(2)).exists()) {
                                                        CrushSyncWatcherWrapper.this.added(new SnapshotFile(new File(file_path)));
                                                        break block21;
                                                    } else {
                                                        prior_rename = data;
                                                        continue;
                                                    }
                                                }
                                                CrushSyncWatcherWrapper.this.renamed(new SnapshotFile(prior_rename.substring(2)), new SnapshotFile(file_path));
                                            } else if (data.charAt(0) == '4') {
                                                CrushSyncWatcherWrapper.this.changed(new SnapshotFile(file_path));
                                            }
                                        }
                                    }
                                    if (!prior_rename.equals("")) {
                                        CrushSyncWatcherWrapper.this.deleted(new SnapshotFile(prior_rename.substring(2)));
                                    }
                                    prior_rename = "";
                                }
                            }
                            catch (Exception e) {
                                if (((CrushSyncWatcherWrapper)CrushSyncWatcherWrapper.this).scanner.stop) break block20;
                                CrushSyncWatcherWrapper.this.scanner.msg(e);
                            }
                        }
                        try {
                            if (((CrushSyncWatcherWrapper)CrushSyncWatcherWrapper.this).scanner.stop) return;
                            CrushSyncWatcherWrapper.this.scanner.stop();
                            return;
                        }
                        catch (Exception e) {
                            CrushSyncWatcherWrapper.this.scanner.msg(e);
                        }
                    }
                }).start();
                return;
            }
            catch (Exception e) {
                this.scanner.msg("fswatch dameon unreachable, using alternate method.", false);
            }
        }
        this.watchService = FileWatcher.create((File)new File(this.scanner.syncPath));
        EnumSet<WatchingAttributes> watchingAttributes = EnumSet.allOf(WatchingAttributes.class);
        watchingAttributes.remove(WatchingAttributes.AccessDate);
        watchingAttributes.remove(WatchingAttributes.Attributes);
        this.watchService.setOptions(watchingAttributes);
        this.watchService.addListener(new FileEventsListener(){

            public void fileAdded(FileEvent.Added fe) {
                CrushSyncWatcherWrapper.this.added(new SnapshotFile(fe.getFile()));
            }

            public void fileDeleted(FileEvent.Deleted fe) {
                CrushSyncWatcherWrapper.this.deleted(new SnapshotFile(fe.getFile()));
            }

            public void fileChanged(FileEvent.Changed fe) {
                CrushSyncWatcherWrapper.this.changed(new SnapshotFile(fe.getFile()));
            }

            public void fileRenamed(FileEvent.Renamed fe) {
                if (!fe.getOldFile().getPath().equals(fe.getFile().getPath())) {
                    CrushSyncWatcherWrapper.this.renamed(new SnapshotFile(fe.getOldFile()), new SnapshotFile(fe.getFile()));
                }
            }
        });
        this.watchService.start();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void added(SnapshotFile f) {
        try {
            if (System.currentTimeMillis() - Long.parseLong(this.scanner.ignoreDownload.getProperty(f.getCanonicalPath(), "0")) < (long)(this.scanner.stableSeconds * 1000)) {
                return;
            }
        }
        catch (IOException e) {
            this.scanner.msg(e);
        }
        this.scanner.msg("ADDED:" + System.currentTimeMillis() + ":" + f, false);
        if (f.getPath().replace('\\', '/').indexOf("/dfsrPrivate/") < 0 && !f.getName().equals(".DS_Store") && f.getName().indexOf(".conflict0") < 0 && f.getName().indexOf(".conflict1") < 0 && f.getName().indexOf(".conflict2") < 0 && !f.getName().endsWith(".downloading")) {
            Vector vector = this.scanner.tempJournal;
            synchronized (vector) {
                this.scanner.msg("Local:New:" + f, false);
                Properties p = new Properties();
                p.put("type", "upload");
                p.put("file1", f);
                p.put("time1", String.valueOf(System.currentTimeMillis()));
                this.scanner.tempJournal.addElement(p);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void deleted(SnapshotFile f) {
        try {
            Enumeration<Object> keys = this.scanner.ignoreDelete.keys();
            String canon = f.getCanonicalPath();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                if (!canon.startsWith(key) || System.currentTimeMillis() - Long.parseLong(this.scanner.ignoreDelete.getProperty(key, "0")) >= (long)(this.scanner.stableSeconds * 1000)) continue;
                return;
            }
        }
        catch (IOException e) {
            this.scanner.msg(e);
        }
        this.scanner.msg("DELETED:" + System.currentTimeMillis() + ":" + f, false);
        if (f.getPath().replace('\\', '/').indexOf("/dfsrPrivate/") < 0 && !f.getName().equals(".DS_Store") && !f.exists()) {
            Vector vector = this.scanner.tempJournal;
            synchronized (vector) {
                this.scanner.msg("Local:Delete:" + f, false);
                Properties p = new Properties();
                p.put("type", "delete");
                p.put("file1", f);
                this.scanner.tempJournal.addElement(p);
            }
        }
    }

    public void changed(SnapshotFile f) {
        try {
            if (System.currentTimeMillis() - Long.parseLong(this.scanner.ignoreRename.getProperty(f.getCanonicalPath(), "0")) < (long)(this.scanner.stableSeconds * 1000)) {
                return;
            }
        }
        catch (IOException e) {
            this.scanner.msg(e);
        }
        this.scanner.msg("CHANGED:" + System.currentTimeMillis() + ":" + f, false);
        if (f.getPath().replace('\\', '/').indexOf("/dfsrPrivate/") < 0 && !f.getName().equals(".DS_Store") && f.getName().indexOf(".conflict0") < 0 && f.getName().indexOf(".conflict1") < 0 && f.getName().indexOf(".conflict2") < 0 && !f.getName().endsWith(".downloading")) {
            this.scanner.doFileChanged(f);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renamed(SnapshotFile f1, SnapshotFile f2) {
        if (f1.getPath().replace('\\', '/').indexOf("/dfsrPrivate/") < 0 && f2.getPath().replace('\\', '/').indexOf("/dfsrPrivate/") < 0 && f1.getName().equals(".DS_Store") || f1.getName().endsWith(".downloading") || f2.getName().endsWith(".downloading")) {
            return;
        }
        try {
            if (System.currentTimeMillis() - Long.parseLong(this.scanner.ignoreRename.getProperty(f1.getCanonicalPath(), "0")) < (long)(this.scanner.stableSeconds * 1000)) {
                return;
            }
        }
        catch (IOException e) {
            this.scanner.msg(e);
        }
        this.scanner.msg("RENAMED:" + System.currentTimeMillis() + ":" + f1 + ":->:" + f2, false);
        this.scanner.msg("Local:Rename:" + f1 + " to " + f2, false);
        Vector vector = this.scanner.tempJournal;
        synchronized (vector) {
            if (f1.getName().indexOf(".conflict0") >= 0 || f1.getName().indexOf(".conflict1") >= 0 || f1.getName().indexOf(".conflict2") >= 0) {
                Properties p = new Properties();
                p.put("type", "rename");
                p.put("file1", f1);
                p.put("file2", f2);
                this.scanner.tempJournal.addElement(p);
                this.scanner.doFileChanged(f2);
                return;
            }
            Properties p = new Properties();
            p.put("type", "rename");
            p.put("file1", f1);
            p.put("file2", f2);
            this.scanner.tempJournal.addElement(p);
        }
    }

    public void stop() {
        if (this.watchService != null) {
            this.watchService.stop();
        }
        this.watchService = null;
        try {
            if (this.sock != null) {
                this.sock.close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.sock = null;
    }
}

