/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.tunnel;

import com.crushftp.client.Common;
import com.crushftp.client.GenericClient;
import com.crushftp.client.HTTPClient;
import com.crushftp.tunnel.AutoChannelProxy;
import com.crushftp.tunnel.FileArchiveEntry;
import com.crushftp.tunnel.FileArchiveOutputStream;
import com.crushftp.tunnel2.Tunnel2;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

public class Uploader2 {
    public Properties controller = new Properties();
    long totalBytes = 1L;
    long transferedBytes = 0L;
    int totalItems = 0;
    int transferedItems = 0;
    String status2 = "";
    StringBuffer action = new StringBuffer();
    public Properties statusInfo = null;
    StringBuffer crushAuth = null;
    SimpleDateFormat sdf_rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    public Object statLock = new Object();
    Properties crossRef = new Properties();
    Vector ask = new Vector();
    Properties ask_response = new Properties();
    Vector clientPool = new Vector();

    public Uploader2(Properties statusInfo, StringBuffer crushAuth) {
        this.statusInfo = statusInfo;
        this.crushAuth = crushAuth;
    }

    public long getTotalBytes() {
        return this.totalBytes;
    }

    public long getTransferedBytes() {
        return this.transferedBytes;
    }

    public int getTotalItems() {
        return this.totalItems;
    }

    public int getTransferedItems() {
        return this.transferedItems;
    }

    public void refreshStatusInfo() {
        this.statusInfo.put("totalBytes", String.valueOf(this.totalBytes));
        this.statusInfo.put("transferedBytes", String.valueOf(this.transferedBytes));
        this.statusInfo.put("totalItems", String.valueOf(this.totalItems));
        this.statusInfo.put("transferedItems", String.valueOf(this.transferedItems));
    }

    public String getStatus() {
        return String.valueOf(this.statusInfo.getProperty("uploadStatus", "")) + this.statusInfo.getProperty("tunnelInfo", "");
    }

    public void pause() {
        Tunnel2.msg("PAUSE");
        this.action.setLength(0);
        this.action.append("pause");
        this.status2 = this.getStatus();
        try {
            Thread.sleep(200L);
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.statusInfo.put("uploadStatus", "Paused:" + this.status2);
    }

    public void resume() {
        Tunnel2.msg("RESUME");
        this.statusInfo.put("uploadStatus", this.status2);
        this.action.setLength(0);
    }

    public void cancel() {
        Tunnel2.msg("CANCEL");
        this.status2 = this.getStatus();
        this.action.setLength(0);
        this.action.append("cancel");
        this.statusInfo.put("uploadStatus", "Cancelled:" + this.status2);
        new Thread(new Runnable(){

            @Override
            public void run() {
                int loops = 0;
                while (Uploader2.this.action.toString().equals("cancel") && loops++ < 100) {
                    try {
                        Thread.sleep(100L);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
        }).start();
    }

    public String getAsk() {
        if (this.ask.size() > 0) {
            Properties p = (Properties)this.ask.elementAt(0);
            this.ask_response.put(p.getProperty("uid"), p);
            this.ask.remove(0);
            return ":::ask=true:::path=" + p.getProperty("path") + ":::modified=" + p.getProperty("modified") + ":::size=" + p.getProperty("size") + ":::uid=" + p.getProperty("uid");
        }
        return "";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    public void go() {
        block43: {
            block40: {
                block42: {
                    block41: {
                        block39: {
                            var1_1 = this.statLock;
                            synchronized (var1_1) {
                                this.totalBytes = 1L;
                                this.transferedBytes = 0L;
                                this.totalItems = 0;
                                this.transferedItems = 0;
                            }
                            this.status2 = "";
                            this.action.setLength(0);
                            this.controller.put("statusInfo", this.statusInfo);
                            this.refreshStatusInfo();
                            Tunnel2.msg("Using Server URL:" + this.controller.getProperty("URL"));
                            this.controller.put("serverFiles", new Properties());
                            this.statusInfo.put("uploadStatus", "Finding files...");
                            parentfiles1 = new Vector<File>();
                            parentfiles = new Vector<File>();
                            files2 = new Vector<E>();
                            loop = 1;
                            while (this.controller.containsKey("P" + loop)) {
                                parentfiles1.addElement(new File(this.controller.getProperty("P" + loop)));
                                ++loop;
                            }
                            try {
                                Tunnel2.msg("Files:" + parentfiles1.size());
                                x = 0;
                                while (x < parentfiles1.size()) {
                                    if (!Tunnel2.checkAction(this.action)) {
                                        return;
                                    }
                                    f = (File)parentfiles1.elementAt(x);
                                    this.statusInfo.put("uploadStatus", "Finding files " + f.getPath() + "... " + Common.percent(x, parentfiles1.size()));
                                    this.getAllFileListing(files2, f.getCanonicalPath(), 999);
                                    if (parentfiles.indexOf(f.getParentFile()) < 0) {
                                        parentfiles.addElement(f.getParentFile());
                                    }
                                    ++x;
                                }
                                this.statusInfo.put("uploadStatus", "Getting file sizes...");
                                Tunnel2.msg("Files2:" + files2.size());
                                x = 0;
                                while (x < files2.size()) {
                                    if (!Tunnel2.checkAction(this.action)) {
                                        return;
                                    }
                                    this.statusInfo.put("uploadStatus", "Getting file sizes... " + Common.percent(x, files2.size()));
                                    f = (File)files2.elementAt(x);
                                    var7_12 = this.statLock;
                                    synchronized (var7_12) {
                                        this.totalBytes += f.length();
                                        ++this.totalItems;
                                    }
                                    ++x;
                                }
                            }
                            catch (Exception e) {
                                Tunnel2.msg(e);
                            }
                            t = null;
                            try {
                                if (this.totalBytes > 0x100000L && this.controller.getProperty("ALLOWTUNNEL", "true").equals("true")) {
                                    t = AutoChannelProxy.enableAppletTunnel(this.controller, false, this.crushAuth);
                                }
                            }
                            catch (Exception e) {
                                Tunnel2.msg("Error checking for tunnel.");
                                Tunnel2.msg(e);
                            }
                            if (Tunnel2.checkAction(this.action)) break block39;
                            this.controller.put("stopTunnel", "true");
                            if (t != null) {
                                while (this.controller.containsKey("stopTunnel")) {
                                    try {
                                        Thread.sleep(100L);
                                    }
                                    catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            this.action.setLength(0);
                            return;
                        }
                        try {
                            try {
                                this.doUpload(parentfiles, files2, this.controller, "");
                                break block40;
                            }
                            catch (Exception e) {
                                Tunnel2.msg(e);
                                this.controller.put("stopTunnel", "true");
                                if (t == null) break block41;
                                ** while (this.controller.containsKey((Object)"stopTunnel"))
                            }
                        }
                        catch (Throwable var7_13) {
                            this.controller.put("stopTunnel", "true");
                            if (t == null) break block42;
                            ** while (this.controller.containsKey((Object)"stopTunnel"))
                        }
lbl-1000:
                        // 1 sources

                        {
                            try {
                                Thread.sleep(100L);
                            }
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            continue;
                        }
                    }
                    this.action.setLength(0);
                    break block43;
lbl-1000:
                    // 1 sources

                    {
                        try {
                            Thread.sleep(100L);
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                }
                this.action.setLength(0);
                throw var7_13;
            }
            this.controller.put("stopTunnel", "true");
            if (t != null) {
                while (this.controller.containsKey("stopTunnel")) {
                    try {
                        Thread.sleep(100L);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            this.action.setLength(0);
        }
        while (this.clientPool.size() > 0) {
            try {
                ((GenericClient)this.clientPool.remove(0)).close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void doUpload(Vector parentfiles, Vector uploadFiles, Properties params, String keywords) {
        if (this.controller.containsKey("PARENTFILES")) {
            parentfiles = (Vector)this.controller.remove("PARENTFILES");
        }
        int loops = 0;
        Vector<File> bigFiles = new Vector<File>();
        Vector files2 = new Vector();
        this.statusInfo.put("uploadStatus", "Upload:Checking for duplicate files, and sorting small and large files...");
        int totalCount = uploadFiles.size();
        int pos = 0;
        while (uploadFiles.size() > 0) {
            this.statusInfo.put("uploadStatus", "Upload:Checking for duplicate files, and sorting small and large files... " + Common.percent(pos, totalCount));
            File f = (File)uploadFiles.remove(0);
            bigFiles.addElement(f);
            ++pos;
            this.refreshStatusInfo();
        }
        int initialSize = files2.size() + bigFiles.size();
        while (loops++ < 60) {
            this.refreshStatusInfo();
            if (!Tunnel2.checkAction(this.action)) {
                return;
            }
            if (loops > 1) {
                this.statusInfo.put("uploadStatus", "Upload:Recovering from an error, re-checking what the server received so we can resume.");
                Tunnel2.msg(this.getStatus());
            }
            this.statusInfo.put("uploadStatus", "Uploading files...");
            Tunnel2.msg(this.getStatus());
            String original_overwrite = params.getProperty("OVERWRITE", "");
            try {
                Tunnel2.msg("Connecting to URL:" + this.controller.getProperty("URL"));
                this.statusInfo.put("uploadStatus", "Upload:Connecting to URL:" + this.controller.getProperty("URL"));
                files2.removeAllElements();
                if (loops > 1 && params.getProperty("OVERWRITE", "").equals("OVERWRITE")) {
                    params.put("OVERWRITE", "RESUME");
                }
                this.uploadNormalFiles(this.controller.getProperty("URL"), params, keywords, bigFiles, parentfiles, initialSize);
                this.statusInfo.put("uploadStatus", "");
                Tunnel2.msg(this.getStatus());
                Object object = this.statLock;
                synchronized (object) {
                    this.transferedBytes = this.totalBytes;
                    break;
                }
            }
            catch (Exception e) {
                Tunnel2.msg(e);
                if (!Tunnel2.checkAction(this.action)) {
                    return;
                }
                if (e.getMessage().startsWith("ERROR:")) {
                    this.statusInfo.put("uploadStatus", "Upload:" + e.getMessage());
                    Tunnel2.msg(this.getStatus());
                    break;
                }
                if (e.getMessage().toUpperCase().indexOf("ACCESS IS DENIED") >= 0) {
                    this.statusInfo.put("uploadStatus", "Upload:ERROR:" + e.getMessage());
                    Tunnel2.msg(this.getStatus());
                    break;
                }
                if ((e.getMessage().toUpperCase().indexOf("BROKEN PIPE") >= 0 || e.getMessage().toUpperCase().indexOf("HAS BEEN SHUTDOWN") >= 0) && loops > 1) {
                    this.statusInfo.put("uploadStatus", "Upload:ERROR:Server denied the file due to a restriction in filename or permissions:" + e.getMessage());
                    Tunnel2.msg(this.getStatus());
                    break;
                }
                this.statusInfo.put("uploadStatus", "Upload:WARN:" + e.getMessage());
                Tunnel2.msg(this.getStatus());
                if (e.getMessage().indexOf("403") > 0) {
                    this.statusInfo.put("uploadStatus", "ERROR:Uploads are not allowed.");
                }
                if (e instanceof FileNotFoundException || loops > 10) {
                    this.crushAuth.setLength(0);
                }
                try {
                    Thread.sleep(loops * 1000);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            finally {
                this.action.setLength(0);
                params.put("OVERWRITE", original_overwrite);
            }
        }
        this.refreshStatusInfo();
    }

    public void sendMeta(GenericClient c, Properties params, String keywords) throws Exception {
        Properties params2 = new Properties();
        if (params != null) {
            Enumeration<Object> en = params.keys();
            while (en.hasMoreElements()) {
                String key = en.nextElement().toString();
                if (!key.toUpperCase().startsWith("META_")) continue;
                String val = params.getProperty(key, "");
                while (key.toUpperCase().startsWith("META_")) {
                    key = key.substring("META_".length());
                }
                params2.put(key, val);
            }
        }
        if (keywords != null && !keywords.trim().equals("")) {
            params2.put("keywords", keywords);
        }
        if (params2.size() > 0) {
            ((HTTPClient)c).sendMetaInfo(params2);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private GenericClient getClient(String url) {
        Vector vector = this.clientPool;
        synchronized (vector) {
            if (this.clientPool.size() > 0) {
                return (GenericClient)this.clientPool.remove(0);
            }
        }
        GenericClient c = Common.getClient(url, "UPLOADER", null);
        c.setConfig("crushAuth", this.crushAuth.toString());
        return c;
    }

    private void releaseClient(GenericClient c) {
        this.clientPool.addElement(c);
    }

    public void uploadNormalFiles(final String url, final Properties params, final String keywords, final Vector files2, final Vector parentfiles, final int initialSize) throws Exception {
        final Properties threads = new Properties();
        final StringBuffer itemIndex = new StringBuffer();
        itemIndex.append("1");
        Runnable r = new Runnable(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                Properties status = (Properties)threads.get(Thread.currentThread());
                File item = null;
                while (files2.size() > 0) {
                    GenericClient c;
                    block66: {
                        Object stat;
                        String itemName;
                        String uploadPath;
                        block65: {
                            Uploader2.this.refreshStatusInfo();
                            c = Uploader2.this.getClient(url);
                            if (!Tunnel2.checkAction(Uploader2.this.action)) {
                                throw new Exception("Cancelled");
                            }
                            Vector vector = files2;
                            synchronized (vector) {
                                if (files2.size() > 0) {
                                    item = (File)files2.remove(0);
                                }
                            }
                            if (item != null) break block65;
                            Uploader2.this.releaseClient(c);
                            break;
                        }
                        if (!item.exists()) break block66;
                        Tunnel2.msg(String.valueOf(status.getProperty("index")) + ":Uploading normally:" + item.getName());
                        int offset = 0;
                        int index = Uploader2.this.indexOfParent(item, parentfiles);
                        if (index >= 0) {
                            File parent = (File)parentfiles.elementAt(index);
                            offset = Uploader2.this.getCanonicalPath(parent).length();
                        }
                        if ((uploadPath = Uploader2.this.controller.getProperty("UPLOADPATH", "")).endsWith("//")) {
                            uploadPath = uploadPath.substring(0, uploadPath.length() - 1);
                        }
                        String new_part = item.getCanonicalPath().replace('\\', '/').substring(offset);
                        if (uploadPath.endsWith("/") && new_part.startsWith("/")) {
                            new_part = new_part.substring(1);
                        }
                        if ((itemName = String.valueOf(uploadPath) + new_part).startsWith("//")) {
                            itemName = itemName.substring(1);
                        }
                        long startPos = 0L;
                        boolean skip = false;
                        if (!params.getProperty("OVERWRITE", "").equals("OVERWRITE") && item.isFile() && (stat = c.stat(itemName)) != null) {
                            if (params.getProperty("OVERWRITE", "").equals("RESUME")) {
                                startPos = Long.parseLong(((Properties)stat).getProperty("size"));
                            } else if (params.getProperty("OVERWRITE", "").equals("SKIP")) {
                                skip = true;
                            } else if (params.getProperty("OVERWRITE", "").equals("ASK")) {
                                ((Properties)stat).put("uid", Common.makeBoundary(8).toUpperCase());
                                ((Properties)stat).put("path", itemName);
                                Uploader2.this.ask.addElement(stat);
                                while (Uploader2.this.ask.indexOf(stat) >= 0) {
                                    if (!Tunnel2.checkAction(Uploader2.this.action)) {
                                        throw new Exception("Cancelled");
                                    }
                                    Thread.sleep(300L);
                                }
                                while (Uploader2.this.ask_response.containsKey(((Properties)stat).getProperty("uid"))) {
                                    if (!Tunnel2.checkAction(Uploader2.this.action)) {
                                        throw new Exception("Cancelled");
                                    }
                                    Thread.sleep(300L);
                                }
                                if (params.getProperty("OVERWRITE", "").equals("RESUME")) {
                                    startPos = Long.parseLong(((Properties)stat).getProperty("size"));
                                } else if (params.getProperty("OVERWRITE", "").equals("OVERWRITE")) {
                                    startPos = 0L;
                                } else if (params.getProperty("OVERWRITE", "").equals("SKIP")) {
                                    skip = true;
                                } else if (params.getProperty("OVERWRITE", "").equals("ASK")) {
                                    if (((Properties)stat).getProperty("response", "overwrite").equalsIgnoreCase("overwrite")) {
                                        startPos = 0L;
                                    } else if (((Properties)stat).getProperty("response", "").equalsIgnoreCase("overwrite_all")) {
                                        params.put("OVERWRITE", "OVERWRITE");
                                        startPos = 0L;
                                        Uploader2.this.ask.removeAllElements();
                                        Uploader2.this.ask_response.clear();
                                    } else if (((Properties)stat).getProperty("response", "").equalsIgnoreCase("resume")) {
                                        startPos = Long.parseLong(((Properties)stat).getProperty("size"));
                                    } else if (((Properties)stat).getProperty("response", "").equalsIgnoreCase("resume_all")) {
                                        params.put("OVERWRITE", "RESUME");
                                        startPos = Long.parseLong(((Properties)stat).getProperty("size"));
                                        Uploader2.this.ask.removeAllElements();
                                        Uploader2.this.ask_response.clear();
                                    } else if (((Properties)stat).getProperty("response", "").equalsIgnoreCase("skip")) {
                                        skip = true;
                                    } else if (((Properties)stat).getProperty("response", "").equalsIgnoreCase("skip_all")) {
                                        params.put("OVERWRITE", "SKIP");
                                        skip = true;
                                        Uploader2.this.ask.removeAllElements();
                                        Uploader2.this.ask_response.clear();
                                    }
                                }
                            }
                        }
                        if (!skip) {
                            if (item.isDirectory()) {
                                c.makedirs(itemName);
                                stat = Uploader2.this.statLock;
                                synchronized (stat) {
                                    ++Uploader2.this.transferedItems;
                                }
                            }
                            if (startPos > 0L) {
                                stat = Uploader2.this.statLock;
                                synchronized (stat) {
                                    Uploader2.this.transferedBytes += startPos;
                                }
                            }
                            Uploader2.this.sendMeta(c, params, keywords);
                            c.setConfig("send_compressed", String.valueOf(Uploader2.this.controller.getProperty("NOCOMPRESSION", "false").equals("false")));
                            OutputStream out = c.upload(itemName, startPos, true, true);
                            Uploader2.this.uploadItem(itemName, startPos, item, false, out, c, itemIndex, initialSize, status);
                            out.close();
                        } else {
                            Object object = Uploader2.this.statLock;
                            synchronized (object) {
                                ++Uploader2.this.transferedItems;
                                Uploader2.this.transferedBytes += item.length();
                            }
                        }
                        if (Tunnel2.checkAction(Uploader2.this.action)) break block66;
                        Uploader2.this.releaseClient(c);
                        break;
                    }
                    try {
                        if (!Tunnel2.checkAction(Uploader2.this.action)) {
                            throw new Exception("Cancelled");
                        }
                        Object offset = Uploader2.this.statLock;
                        synchronized (offset) {
                            int i = Integer.parseInt(itemIndex.toString());
                            itemIndex.setLength(0);
                            itemIndex.append(String.valueOf(++i));
                        }
                        status.put("itemIndex", "" + itemIndex);
                    }
                    catch (Exception e) {
                        if (item != null) {
                            files2.insertElementAt(item, 0);
                        }
                        if (!Tunnel2.checkAction(Uploader2.this.action)) {
                            Uploader2.this.releaseClient(c);
                            break;
                        }
                        try {
                            status.put("error", e);
                            break;
                        }
                        catch (Throwable throwable) {
                            throw throwable;
                        }
                        finally {
                            Uploader2.this.releaseClient(c);
                        }
                    }
                    Uploader2.this.releaseClient(c);
                }
                Uploader2.this.refreshStatusInfo();
                status.put("status", "DONE");
            }
        };
        int maxThreads = Integer.parseInt(this.controller.getProperty("UPLOAD_THREADS", "1"));
        maxThreads = 1;
        int x = 0;
        while (x < maxThreads) {
            Thread t = new Thread(r);
            Properties status = new Properties();
            status.put("status", "RUNNING");
            status.put("index", String.valueOf(x));
            threads.put(t, status);
            t.start();
            ++x;
        }
        Exception lastError = null;
        while (threads.size() > 0) {
            Enumeration<Object> keys = threads.keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Properties p = (Properties)threads.get(key);
                if (!p.getProperty("status", "").equals("DONE")) continue;
                threads.remove(key);
                if (!p.containsKey("error")) continue;
                lastError = (Exception)p.get("error");
            }
            Thread.sleep(100L);
        }
        if (lastError != null) {
            throw lastError;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void uploadItem(String itemName, long startPos, File item, boolean zip, OutputStream out, GenericClient c, StringBuffer itemIndex, int initialSize, Properties status) throws Exception {
        block23: {
            FileArchiveEntry zipEntry = new FileArchiveEntry(itemName);
            if (startPos > 0L) {
                this.statusInfo.put("uploadStatus", String.valueOf(status.getProperty("index")) + ":Upload:Resuming item:" + itemName + " at position:" + startPos + " (" + itemIndex + " of " + initialSize + ")");
                Tunnel2.msg(this.getStatus());
                zipEntry = new FileArchiveEntry(String.valueOf(itemName) + ":REST=" + startPos);
            }
            zipEntry.setTime(item.lastModified());
            if (zip) {
                this.statusInfo.put("uploadStatus", String.valueOf(status.getProperty("index")) + ":Upload:Adding zip=" + zip + " entry:" + itemName);
            } else {
                this.statusInfo.put("uploadStatus", String.valueOf(status.getProperty("index")) + ":Upload:" + itemName + " (" + itemIndex + " of " + initialSize + ")");
            }
            if (zip) {
                ((FileArchiveOutputStream)((Object)out)).putArchiveEntry(zipEntry);
            }
            Tunnel2.msg(this.getStatus());
            RandomAccessFile in = new RandomAccessFile(item.getCanonicalPath(), "r");
            long bytesThisFile = startPos;
            try {
                try {
                    if (startPos > 0L) {
                        in.seek(startPos);
                    }
                    byte[] b = new byte[65536];
                    int bytesRead = 0;
                    long pos = 0L;
                    long start = System.currentTimeMillis();
                    while (bytesRead >= 0) {
                        bytesRead = in.read(b);
                        if (bytesRead > 0) {
                            out.write(b, 0, bytesRead);
                            Object object = this.statLock;
                            synchronized (object) {
                                this.transferedBytes += (long)bytesRead;
                            }
                            bytesThisFile += (long)bytesRead;
                            pos += (long)bytesRead;
                            if (System.currentTimeMillis() - start > 1000L) {
                                start = System.currentTimeMillis();
                                this.statusInfo.put("uploadStatus", String.valueOf(status.getProperty("index")) + ":Upload:" + itemName + "... " + Common.format_bytes_short(bytesThisFile) + "/" + Common.format_bytes_short(item.length()) + " (" + itemIndex + " of " + initialSize + ")");
                            }
                        }
                        this.refreshStatusInfo();
                        if (Tunnel2.checkAction(this.action)) continue;
                        System.out.println(c.doCommand("SITE BLOCK_UPLOADS"));
                        Thread.sleep(1000L);
                        break;
                    }
                }
                catch (Exception e) {
                    Object object = this.statLock;
                    synchronized (object) {
                        this.transferedBytes -= bytesThisFile;
                    }
                    in.close();
                    break block23;
                }
            }
            catch (Throwable throwable) {
                in.close();
                throw throwable;
            }
            in.close();
        }
        if (zip) {
            ((FileArchiveOutputStream)((Object)out)).closeArchiveEntry();
        }
        Object object = this.statLock;
        synchronized (object) {
            ++this.transferedItems;
        }
        Tunnel2.msg("Bytes written:" + itemName);
    }

    public int indexOfParent(File item, Vector files) throws IOException {
        int i = -1;
        int offset = 0;
        int loop = 0;
        while (loop < files.size()) {
            int newOffset;
            File f = (File)files.elementAt(loop);
            if (this.getCanonicalPath(item).startsWith(this.getCanonicalPath(f)) && (newOffset = this.getCanonicalPath(f).length()) > offset) {
                offset = newOffset;
                i = loop;
            }
            ++loop;
        }
        return i;
    }

    public void getAllFileListing(Vector list, String path, int depth) throws Exception {
        if (!Tunnel2.checkAction(this.action)) {
            return;
        }
        File item = new File(path);
        this.buildCrossRef(item);
        if (item.isFile()) {
            list.addElement(item);
        } else {
            this.appendListing(path, list, "", depth);
        }
    }

    public void buildCrossRef(File f) throws IOException {
        if (Common.machine_is_x()) {
            if (this.crossRef.containsKey(f.getCanonicalPath())) {
                return;
            }
            boolean hasBadPathChar = false;
            StringBuffer sb = new StringBuffer();
            File f2 = f;
            while (f != null) {
                String name = f.getName();
                sb.insert(0, "/" + name.replace('/', '.').replace(':', '.'));
                if (name.indexOf("/") >= 0 || name.indexOf(":") >= 0) {
                    hasBadPathChar = true;
                }
                if ((f = f.getParentFile()).getPath().equals("/")) break;
            }
            if (hasBadPathChar) {
                this.crossRef.put(f2.getCanonicalPath(), sb.toString());
            }
        }
    }

    public String getCanonicalPath(File f) throws IOException {
        return this.crossRef.getProperty(f.getCanonicalPath(), f.getCanonicalPath());
    }

    public void appendListing(String path, Vector list, String dir, int depth) throws Exception {
        if (!Tunnel2.checkAction(this.action)) {
            return;
        }
        if (depth == 0) {
            return;
        }
        --depth;
        if (!path.endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        String[] items = new File(String.valueOf(path) + dir).list();
        list.addElement(new File(String.valueOf(path) + dir));
        if (items == null) {
            return;
        }
        int x = 0;
        while (x < items.length) {
            if (!Tunnel2.checkAction(this.action)) {
                return;
            }
            File item = new File(String.valueOf(path) + dir + items[x]);
            this.buildCrossRef(item);
            if (item.isFile()) {
                list.addElement(item);
            } else {
                this.appendListing(path, list, String.valueOf(dir) + items[x] + "/", depth);
            }
            ++x;
        }
    }
}

