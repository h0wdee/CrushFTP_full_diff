/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.compress.archivers.zip.Zip64Mode
 */
package com.crushftp.tunnel;

import com.crushftp.client.Common;
import com.crushftp.tunnel.FileArchiveEntry;
import com.crushftp.tunnel.FileArchiveOutputStream;
import com.crushftp.tunnel2.Tunnel2;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipException;
import org.apache.commons.compress.archivers.zip.Zip64Mode;

public class Uploader {
    public Properties controller = new Properties();
    long totalBytes = 1L;
    long transferedBytes = 0L;
    int totalItems = 0;
    int transferedItems = 0;
    String status2 = "";
    StringBuffer action = new StringBuffer();
    boolean standAlone = false;
    public Properties statusInfo = null;
    StringBuffer CrushAuth = null;
    SimpleDateFormat sdf_rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    public Object statLock = new Object();
    Properties crossRef = new Properties();

    public Uploader(Properties statusInfo, StringBuffer CrushAuth) {
        this.statusInfo = statusInfo;
        this.CrushAuth = CrushAuth;
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
                while (Uploader.this.action.toString().equals("cancel") && loops++ < 100) {
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

    /*
     * Exception decompiling
     */
    public void go() {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [1[TRYBLOCK]], but top level block is 25[WHILELOOP]
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

    public void getServerFileSizes(String partialPath) {
        if (!Tunnel2.checkAction(this.action)) {
            return;
        }
        try {
            Properties serverFiles = (Properties)this.controller.get("serverFiles");
            URL u = new URL(String.valueOf(this.controller.getProperty("URL_REAL", this.controller.getProperty("URL"))) + this.controller.getProperty("UPLOADPATH", "") + partialPath + "/:filetree");
            Tunnel2.msg("Getting folder contents information " + u.toExternalForm());
            HttpURLConnection urlc = (HttpURLConnection)u.openConnection();
            urlc.setReadTimeout(70000);
            urlc.setRequestMethod("GET");
            urlc.setRequestProperty("Cookie", "CrushAuth=" + this.CrushAuth.toString() + ";");
            urlc.setUseCaches(false);
            urlc.setDoInput(true);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            try (BufferedReader br = null;){
                br = new BufferedReader(new InputStreamReader(urlc.getInputStream(), "UTF8"));
                String data = "";
                long tempSize = 0L;
                long tempfileSize = 0L;
                long modified = 0L;
                while ((data = br.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(data);
                    st.nextToken();
                    st.nextToken();
                    st.nextToken();
                    st.nextToken();
                    tempfileSize = Long.parseLong(st.nextToken());
                    tempSize += tempfileSize;
                    String mdate = st.nextToken();
                    modified = sdf.parse(mdate).getTime();
                    st.nextToken();
                    st.nextToken();
                    String rootdir = st.nextToken();
                    while (st.hasMoreTokens()) {
                        rootdir = String.valueOf(rootdir) + " " + st.nextToken();
                    }
                    Properties p = new Properties();
                    p.put("size", String.valueOf(tempfileSize));
                    p.put("modified", String.valueOf(modified));
                    serverFiles.put(rootdir, p);
                    if (Tunnel2.checkAction(this.action)) continue;
                    break;
                }
            }
            urlc.getResponseCode();
            urlc.disconnect();
        }
        catch (Exception e) {
            Tunnel2.msg(String.valueOf(partialPath) + " not found:" + e.toString());
        }
    }

    public static boolean login(Properties controller, boolean standAlone, StringBuffer CrushAuth2) {
        block9: {
            try {
                System.getProperties().put("sun.net.http.retryPost", "false");
                String clientid = "";
                if (controller.containsKey("CLIENTID")) {
                    clientid = "&clientid=" + controller.getProperty("CLIENTID");
                }
                if (CrushAuth2.toString().equals("")) {
                    URL u = new URL(controller.getProperty("URL_REAL", controller.getProperty("URL")));
                    HttpURLConnection urlc = (HttpURLConnection)u.openConnection();
                    urlc.setRequestMethod("POST");
                    urlc.setUseCaches(false);
                    urlc.setDoOutput(true);
                    urlc.getOutputStream().write(("command=login&username=" + controller.getProperty("USERNAME") + "&password=" + controller.getProperty("PASSWORD") + clientid).getBytes("UTF8"));
                    int code = urlc.getResponseCode();
                    String result = Tunnel2.consumeResponse(urlc);
                    String cookie = urlc.getHeaderField("Set-Cookie");
                    Tunnel2.msg("Got login result:" + code + " and result:" + result + " and cookie:" + cookie);
                    CrushAuth2.setLength(0);
                    CrushAuth2.append(cookie.substring(cookie.indexOf("CrushAuth=") + "CrushAuth=".length(), cookie.indexOf(";", cookie.indexOf("CrushAuth="))));
                    urlc.disconnect();
                    if (result.indexOf("<response>failure</response>") >= 0) {
                        return false;
                    }
                }
                return true;
            }
            catch (SocketException e) {
                Tunnel2.msg(e);
                try {
                    if (("" + e).toUpperCase().indexOf("SOCKET CLOSED") >= 0) {
                        Tunnel2.msg("Forced wait to overcome java bug on HTTP Client.");
                        Thread.sleep(31000L);
                    }
                }
                catch (Exception exception) {}
            }
            catch (Exception e) {
                Tunnel2.msg(e);
                if (!standAlone) break block9;
                System.exit(1);
            }
        }
        return false;
    }

    public void findUploadInfo(Vector v, Vector parentfiles) {
        Properties serverFiles = (Properties)this.controller.get("serverFiles");
        Vector<File> toDelete = new Vector<File>();
        int x = 0;
        while (x < v.size()) {
            if (!Tunnel2.checkAction(this.action)) {
                return;
            }
            File f = (File)v.elementAt(x);
            this.statusInfo.put("uploadStatus", "Upload:Finding file " + f.getPath() + "...");
            Tunnel2.msg(this.getStatus());
            String partialPath = "";
            int index = -1;
            try {
                index = this.indexOfParent(f, parentfiles);
            }
            catch (IOException iOException) {
                // empty catch block
            }
            if (index >= 0) {
                File parent = (File)parentfiles.elementAt(index);
                try {
                    partialPath = this.getCanonicalPath(f).substring(this.getCanonicalPath(parent).length()).replace('\\', '/').substring(1);
                }
                catch (IOException iOException) {}
            } else {
                partialPath = f.getName();
            }
            try {
                Properties serverItem = (Properties)serverFiles.get(String.valueOf(new URL(this.controller.getProperty("URL_REAL", this.controller.getProperty("URL"))).getPath()) + partialPath);
                if (serverItem == null) {
                    this.getServerFileSizes(partialPath);
                }
                if ((serverItem = (Properties)serverFiles.get(String.valueOf(new URL(this.controller.getProperty("URL_REAL", this.controller.getProperty("URL"))).getPath()) + partialPath)) != null && Long.parseLong(serverItem.getProperty("size", "-1")) == f.length() && Math.abs(Long.parseLong(serverItem.getProperty("modified", "-1")) - f.lastModified()) < 3000L) {
                    toDelete.addElement(f);
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
            ++x;
        }
        x = 0;
        while (x < toDelete.size()) {
            v.remove(toDelete.elementAt(x));
            ++x;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void doUpload(Vector parentfiles, Vector uploadFiles, Properties params, String keywords) {
        File f;
        if (this.controller.containsKey("PARENTFILES")) {
            parentfiles = (Vector)this.controller.remove("PARENTFILES");
        }
        int loops = 0;
        Vector<File> bigFiles = new Vector<File>();
        Vector<File> files2 = new Vector<File>();
        this.statusInfo.put("uploadStatus", "Upload:Checking for duplicate files, and sorting small and large files...");
        int totalCount = uploadFiles.size();
        int pos = 0;
        while (uploadFiles.size() > 0) {
            this.statusInfo.put("uploadStatus", "Upload:Checking for duplicate files, and sorting small and large files... " + Common.percent(pos, totalCount));
            boolean addBig = false;
            boolean addSmall = false;
            f = (File)uploadFiles.remove(0);
            if (totalCount < 50000 || files2.indexOf(f) < 0) {
                addSmall = true;
            }
            if (addBig) {
                bigFiles.addElement(f);
            } else if (addSmall) {
                files2.addElement(f);
            }
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
                int x22 = 0;
                while (x22 < parentfiles.size()) {
                    if (!Tunnel2.checkAction(this.action)) {
                        return;
                    }
                    f = (File)parentfiles.elementAt(x22);
                    this.statusInfo.put("uploadStatus", "Upload:Finding file " + f.getPath() + "...");
                    Tunnel2.msg(this.getStatus());
                    this.getServerFileSizes(f.getName());
                    ++x22;
                }
                this.findUploadInfo(files2, parentfiles);
                this.findUploadInfo(bigFiles, parentfiles);
                Object x22 = this.statLock;
                synchronized (x22) {
                    this.transferedBytes = 0L;
                    this.transferedItems = 0;
                }
            }
            this.statusInfo.put("uploadStatus", "Uploading files...");
            Tunnel2.msg(this.getStatus());
            HttpURLConnection urlc = null;
            try {
                Tunnel2.msg("Connecting to URL:" + this.controller.getProperty("URL"));
                this.statusInfo.put("uploadStatus", "Upload:Connecting to URL:" + this.controller.getProperty("URL"));
                URL u = new URL(this.controller.getProperty("URL"));
                this.uploadZippedFiles(u, params, keywords, files2, parentfiles, initialSize);
                files2.removeAllElements();
                this.uploadNormalFiles(u, params, keywords, bigFiles, parentfiles, initialSize);
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
                this.statusInfo.put("uploadStatus", "Upload:WARN:" + e.getMessage());
                Tunnel2.msg(this.getStatus());
                if (e.getMessage().indexOf(" 403 ") > 0) {
                    this.statusInfo.put("uploadStatus", "ERROR:Uploads are not allowed.");
                }
                try {
                    URL u = new URL(String.valueOf(this.controller.getProperty("URL_REAL", this.controller.getProperty("URL"))) + "?c2f=" + this.CrushAuth.toString().substring(this.CrushAuth.toString().length() - 4) + "command=getLastUploadError");
                    urlc = (HttpURLConnection)u.openConnection();
                    urlc.setRequestMethod("GET");
                    urlc.setRequestProperty("Cookie", "CrushAuth=" + this.CrushAuth.toString() + ";");
                    urlc.setUseCaches(false);
                    String result = Tunnel2.consumeResponse(urlc);
                    urlc.disconnect();
                    if (!result.equals("")) {
                        this.statusInfo.put("uploadStatus", "Upload:" + result);
                    }
                    if (result.indexOf("ERROR:") >= 0) {
                        break;
                    }
                }
                catch (Exception exception) {}
                if (e instanceof FileNotFoundException || loops > 10) {
                    this.CrushAuth.setLength(0);
                    Uploader.login(this.controller, this.standAlone, this.CrushAuth);
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
            }
        }
        this.refreshStatusInfo();
        if (loops >= 59 && this.standAlone) {
            System.exit(1);
        }
    }

    public OutputStream prepForUpload(URL u, HttpURLConnection urlc, String boundary, Properties params, String keywords, String path, String filename, String byteRange, long fileSize, File item) throws Exception {
        urlc.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary.substring(2, boundary.length()));
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Cookie", "CrushAuth=" + this.CrushAuth.toString() + ";");
        urlc.setUseCaches(false);
        urlc.setChunkedStreamingMode(9999);
        urlc.setDoOutput(true);
        BufferedOutputStream out = new BufferedOutputStream(urlc.getOutputStream());
        if (params != null) {
            Enumeration<Object> en = params.keys();
            while (en.hasMoreElements()) {
                String key = en.nextElement().toString();
                if (!key.toUpperCase().startsWith("META_")) continue;
                String val = params.getProperty(key, "");
                while (key.toUpperCase().startsWith("META_")) {
                    key = key.substring("META_".length());
                }
                this.writeEntry("META_" + key, val, out, boundary);
            }
        }
        this.writeEntry("the_action", "STOR", out, boundary);
        this.writeEntry("c2f", this.CrushAuth.toString().substring(this.CrushAuth.toString().length() - 4), out, boundary);
        out.flush();
        if (keywords != null && !keywords.trim().equals("")) {
            this.writeEntry("keywords", keywords, out, boundary);
        }
        this.writeEntry("uploadPath", path, out, boundary);
        if (byteRange != null) {
            long startPos = Long.parseLong(byteRange.substring(0, byteRange.indexOf("-")));
            String endPart = byteRange.substring(byteRange.indexOf("-") + 1);
            long endPos = -1L;
            if (!endPart.equals("")) {
                endPos = Long.parseLong(endPart);
            }
            this.writeEntry("start_resume_loc", String.valueOf(startPos), out, boundary);
            this.writeEntry("randomaccess", String.valueOf(endPos > -1L), out, boundary);
        }
        if (fileSize >= 0L && byteRange == null) {
            this.writeEntry("speedCheat", "true", out, boundary);
            this.writeEntry("speedCheatSize", String.valueOf(fileSize), out, boundary);
        }
        if (item != null) {
            this.writeEntry("Last-Modified", this.sdf_rfc1123.format(new Date(item.lastModified())), out, boundary);
        }
        out.flush();
        String cheat = "";
        out.write((String.valueOf(boundary) + "\r\n").getBytes("UTF8"));
        out.write(("Content-Disposition: form-data; name=\"fileupload" + cheat + "\"; filename=\"" + filename + "\"\r\n").getBytes("UTF8"));
        out.write("Content-Type: application/octet-stream\r\n".getBytes("UTF8"));
        out.write("\r\n".getBytes("UTF8"));
        out.flush();
        return out;
    }

    public void finishHttpConnection(HttpURLConnection urlc, OutputStream out, String boundary, Properties status) throws Exception {
        out.write("\r\n".getBytes("UTF8"));
        this.writeEnd(out, boundary);
        urlc.getResponseCode();
        String result = Tunnel2.consumeResponse(urlc);
        urlc.disconnect();
        if (!Tunnel2.checkAction(this.action)) {
            return;
        }
        Tunnel2.msg(String.valueOf(status.getProperty("index")) + ":" + result);
        if (result.toUpperCase().indexOf("SUCCESS") < 0) {
            Tunnel2.msg(String.valueOf(status.getProperty("index")) + ":result:" + result);
            throw new Exception(result);
        }
    }

    public void uploadNormalFiles(final URL u, final Properties params, final String keywords, final Vector files2, final Vector parentfiles, final int initialSize) throws Exception {
        final String boundary = "--" + Common.makeBoundary(11);
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
                HttpURLConnection urlc = null;
                File item = null;
                while (files2.size() > 0) {
                    Uploader.this.refreshStatusInfo();
                    try {
                        Vector byteRanges;
                        if (!Tunnel2.checkAction(Uploader.this.action)) {
                            throw new Exception("Cancelled");
                        }
                        Vector vector = files2;
                        synchronized (vector) {
                            if (files2.size() > 0) {
                                item = (File)files2.remove(0);
                            }
                        }
                        if (item == null) break;
                        if (item.exists()) {
                            String itemName;
                            urlc = (HttpURLConnection)u.openConnection();
                            urlc.setRequestProperty("Last-Modified", Uploader.this.sdf_rfc1123.format(new Date(item.lastModified())));
                            Tunnel2.msg(String.valueOf(status.getProperty("index")) + ":Uploading normally:" + item.getName());
                            byteRanges = new Vector();
                            StringBuffer delta = new StringBuffer();
                            int offset = 0;
                            int index = Uploader.this.indexOfParent(item, parentfiles);
                            if (index >= 0) {
                                File parent = (File)parentfiles.elementAt(index);
                                offset = Uploader.this.getCanonicalPath(parent).length();
                            }
                            String uploadPath = u.getPath();
                            if (!Uploader.this.controller.getProperty("UPLOADPATH", "").equals("")) {
                                uploadPath = Uploader.this.controller.getProperty("UPLOADPATH", "");
                            }
                            if ((itemName = Uploader.this.getFileDownloadInfo(item, offset, byteRanges, delta, uploadPath, status)) == null) continue;
                            if ((uploadPath = String.valueOf(uploadPath) + Common.all_but_last(itemName)).endsWith("//")) {
                                uploadPath = uploadPath.substring(0, uploadPath.length() - 1);
                            }
                            int x = 0;
                            while (x < byteRanges.size()) {
                                String byteRange = byteRanges.elementAt(x).toString();
                                long startPos = Long.parseLong(byteRange.substring(0, byteRange.indexOf("-")));
                                String endPart = byteRange.substring(byteRange.indexOf("-") + 1);
                                long endPos = -1L;
                                if (!endPart.equals("")) {
                                    endPos = Long.parseLong(endPart);
                                }
                                long byteAmount = -1L;
                                if (startPos >= 0L) {
                                    byteAmount = item.length();
                                    byteAmount -= startPos;
                                    if (endPos > 0L) {
                                        byteAmount = item.length() - byteAmount;
                                    }
                                }
                                if (x == byteRanges.size() - 1 && endPos >= 0L && item.length() < endPos) {
                                    byteRange = String.valueOf(startPos) + "--1";
                                }
                                Tunnel2.msg(String.valueOf(status.getProperty("index")) + ":Uploading bytes:" + item.getName() + ":" + startPos + "-" + endPos);
                                OutputStream out = Uploader.this.prepForUpload(u, urlc, boundary, params, keywords, uploadPath, item.getName(), byteRange, byteAmount, item);
                                OutputStream out2 = null;
                                Uploader.this.uploadItem(byteRange, itemName, item, false, out2 != null ? out2 : out, itemIndex, initialSize, status);
                                if (!Tunnel2.checkAction(Uploader.this.action)) break;
                                Tunnel2.msg(String.valueOf(status.getProperty("index")) + ":Checking for upload completion message..." + itemName);
                                if (out2 != null) {
                                    out2.close();
                                }
                                Uploader.this.finishHttpConnection(urlc, out, boundary, status);
                                Tunnel2.msg(String.valueOf(status.getProperty("index")) + ":Got upload completion message:" + itemName);
                                ++x;
                            }
                        }
                        if (!Tunnel2.checkAction(Uploader.this.action)) {
                            throw new Exception("Cancelled");
                        }
                        byteRanges = Uploader.this.statLock;
                        synchronized (byteRanges) {
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
                        if (urlc != null) {
                            urlc.disconnect();
                        }
                        if (!Tunnel2.checkAction(Uploader.this.action)) break;
                        status.put("error", e);
                        break;
                    }
                }
                Uploader.this.refreshStatusInfo();
                status.put("status", "DONE");
            }
        };
        int maxThreads = Integer.parseInt(this.controller.getProperty("UPLOAD_THREADS", "1"));
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
    public void uploadZippedFiles(URL u, Properties params, String keywords, Vector files2, Vector parentfiles, int initialSize) throws Exception {
        Properties status = new Properties();
        status.put("index", "0");
        if (files2.size() == 0) {
            return;
        }
        HttpURLConnection urlc = (HttpURLConnection)u.openConnection();
        final StringBuffer bonusStatus = new StringBuffer();
        try {
            try {
                urlc.setReadTimeout(300000);
                String boundary = "--" + Common.makeBoundary(11);
                String uploadPath = u.getPath();
                if (!this.controller.getProperty("UPLOADPATH", "").equals("")) {
                    uploadPath = this.controller.getProperty("UPLOADPATH", "");
                }
                OutputStream out = this.prepForUpload(u, urlc, boundary, params, keywords, uploadPath, "uploader" + new Date().getTime() + ".zipstream", null, -1L, null);
                FileArchiveOutputStream zout = new FileArchiveOutputStream(out, this.controller.getProperty("NOCOMPRESSION", "false").equals("false"));
                zout.setUseZip64(Zip64Mode.Always);
                if (this.controller.getProperty("NOCOMPRESSION", "false").equals("true")) {
                    Tunnel2.msg("No compression being used.");
                } else {
                    zout.setLevel(8);
                }
                int xx = 0;
                while (xx < files2.size()) {
                    block25: {
                        this.refreshStatusInfo();
                        try {
                            File item = (File)files2.elementAt(xx);
                            if (!item.exists()) break block25;
                            Tunnel2.msg("Working on item:" + item);
                            int offset = 0;
                            int index = this.indexOfParent(item, parentfiles);
                            if (index >= 0) {
                                File parent = (File)parentfiles.elementAt(index);
                                offset = this.getCanonicalPath(parent).length();
                            }
                            if (item.isDirectory()) {
                                String itemName = (String.valueOf(this.getCanonicalPath(item).substring(offset)) + "/").replace('\\', '/');
                                if (itemName.startsWith("/")) {
                                    itemName = itemName.substring(1);
                                }
                                this.statusInfo.put("uploadStatus", "Upload:Creating folder:" + itemName);
                                Tunnel2.msg(this.getStatus());
                                FileArchiveEntry zipEntry = new FileArchiveEntry(itemName);
                                zipEntry.setTime(item.lastModified());
                                zout.putArchiveEntry(zipEntry);
                                zout.closeArchiveEntry();
                                Object object = this.statLock;
                                synchronized (object) {
                                    ++this.transferedItems;
                                    break block25;
                                }
                            }
                            if (item.isFile()) {
                                if (!Tunnel2.checkAction(this.action)) break;
                                Vector byteRanges = new Vector();
                                String itemName = this.getFileDownloadInfo(item, offset, byteRanges, new StringBuffer(), uploadPath, status);
                                if (itemName != null) {
                                    Tunnel2.msg("Adding zip entry:" + itemName);
                                    int x = 0;
                                    while (x < byteRanges.size()) {
                                        this.uploadItem(byteRanges.elementAt(x).toString(), itemName, item, true, (OutputStream)((Object)zout), new StringBuffer().append(String.valueOf(xx + 1)), initialSize, status);
                                        if (!Tunnel2.checkAction(this.action)) break;
                                        ++x;
                                    }
                                    Tunnel2.msg("Zip entry complete:" + itemName);
                                }
                            }
                        }
                        catch (ZipException e) {
                            if (e.toString().toUpperCase().indexOf("DUPLICATE") >= 0) {
                                Tunnel2.msg("Ignoring duplicate item:" + e);
                            }
                            throw e;
                        }
                    }
                    ++xx;
                }
                if (this.transferedItems > 0) {
                    this.statusInfo.put("uploadStatus", "Upload:...");
                    Tunnel2.msg(this.getStatus());
                    zout.finish();
                    zout.flush();
                }
                Tunnel2.msg("Getting completion message for zip entries...");
                urlc.setReadTimeout(300000);
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        Thread.currentThread().setName("Uploader:UploadZippedFiles - message waiter");
                        int loops = 0;
                        while (bonusStatus.length() == 0) {
                            try {
                                Thread.sleep(1000L);
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                            if (!Tunnel2.checkAction(Uploader.this.action)) {
                                return;
                            }
                            if (bonusStatus.length() != 0 || loops++ < 10) continue;
                            Uploader.this.statusInfo.put("uploadStatus", "Upload:Server is decompressing files...please wait. (" + loops + ")");
                            Tunnel2.msg("Upload:Server is decompressing files...please wait. (" + loops + ")");
                        }
                    }
                }).start();
                if (!Tunnel2.checkAction(this.action)) {
                    throw new Exception("Cancelled");
                }
                this.finishHttpConnection(urlc, out, boundary, status);
                Tunnel2.msg("Got completion message for zip entries.");
            }
            catch (Exception e) {
                bonusStatus.append("done");
                this.refreshStatusInfo();
                if (urlc != null) {
                    urlc.disconnect();
                }
                throw e;
            }
        }
        finally {
            bonusStatus.append("done");
            this.refreshStatusInfo();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void uploadItem(String byteRange, String itemName, File item, boolean zip, OutputStream out, StringBuffer itemIndex, int initialSize, Properties status) throws Exception {
        long startPos = 0L;
        startPos = Long.parseLong(byteRange.substring(0, byteRange.indexOf("-")));
        long endPos = -1L;
        String endPart = byteRange.substring(byteRange.indexOf("-") + 1);
        if (!endPart.equals("")) {
            endPos = Long.parseLong(endPart);
        }
        FileArchiveEntry zipEntry = new FileArchiveEntry(itemName);
        if (startPos > 0L) {
            this.statusInfo.put("uploadStatus", String.valueOf(status.getProperty("index")) + ":Upload:Resuming item:" + itemName + " at position:" + startPos + " to " + endPos + " (" + itemIndex + " of " + initialSize + ")");
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
        try (RandomAccessFile in = new RandomAccessFile(item.getCanonicalPath(), "r");){
            if (startPos > 0L) {
                in.seek(startPos);
            }
            byte[] b = new byte[65536];
            int bytesRead = 0;
            long bytesThisFile = startPos;
            long pos = 0L;
            long start = System.currentTimeMillis();
            while (bytesRead >= 0) {
                if (endPos >= 0L && (long)b.length > endPos - pos) {
                    b = new byte[(int)(endPos - pos)];
                }
                if ((bytesRead = in.read(b)) > 0) {
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
                if (!Tunnel2.checkAction(this.action)) {
                } else if (endPos < 0L || pos != endPos) continue;
                break;
            }
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String getFileDownloadInfo(File f, int offset, Vector byteRanges, StringBuffer delta, String path, Properties status) throws Exception {
        this.statusInfo.put("uploadStatus", String.valueOf(status.getProperty("index")) + ":Uploading file " + f.getPath() + "...");
        String itemName = this.getCanonicalPath(f).substring(offset).replace('\\', '/');
        if (itemName.startsWith("/")) {
            itemName = itemName.substring(1);
        }
        itemName = String.valueOf(path) + itemName;
        long serverSize = -1L;
        URL u = new URL(String.valueOf(this.controller.getProperty("URL_REAL", this.controller.getProperty("URL"))) + itemName + "/:filetree");
        Tunnel2.msg("Getting folder contents information " + u.toExternalForm());
        HttpURLConnection urlc = (HttpURLConnection)u.openConnection();
        urlc.setReadTimeout(70000);
        urlc.setRequestMethod("GET");
        urlc.setRequestProperty("Cookie", "CrushAuth=" + this.CrushAuth.toString() + ";");
        urlc.setUseCaches(false);
        urlc.setDoInput(true);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(urlc.getInputStream(), "UTF8"));
            String data = "";
            while ((data = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(data);
                st.nextToken();
                st.nextToken();
                st.nextToken();
                st.nextToken();
                serverSize = Long.parseLong(st.nextToken());
                if (Tunnel2.checkAction(this.action)) {
                    continue;
                }
                break;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (br != null) {
            br.close();
        }
        urlc.disconnect();
        delta.append(String.valueOf(this.controller.getProperty("delta", "false").equals("true") || this.controller.getProperty("DELTA", "false").equals("true")));
        if (!delta.toString().equals("true") && serverSize == f.length()) {
            Tunnel2.msg(String.valueOf(status.getProperty("index")) + ":Skipping item since it already exists:" + itemName);
            this.statusInfo.put("uploadStatus", String.valueOf(status.getProperty("index")) + ":Upload:Skipping item since it already exists:" + itemName);
            Object e = this.statLock;
            synchronized (e) {
                this.transferedBytes += serverSize;
            }
            return null;
        }
        if (serverSize > 0L && serverSize < f.length()) {
            delta.setLength(0);
            delta.append("true");
        }
        byteRanges.addElement("0--1");
        if (delta.toString().equals("true") && f.length() > 0xA00000L) {
            Tunnel2.msg(String.valueOf(status.getProperty("index")) + ":Asking server for list of remote MD5 hashes for " + itemName);
            StringBuffer status1 = new StringBuffer();
            StringBuffer status2 = new StringBuffer();
            Vector chunksF1 = new Vector();
            Vector chunksF2 = new Vector();
            Tunnel2.doMD5Comparisons("upload", this.statusInfo, itemName, this.controller, chunksF1, chunksF2, this.CrushAuth, status1, status2, f, byteRanges, this.action);
            Tunnel2.msg(String.valueOf(status.getProperty("index")) + ":Got " + chunksF1.size() + " remote and " + chunksF2.size() + " local MD5 hashes for " + itemName);
            Tunnel2.msg(String.valueOf(status.getProperty("index")) + ":Hash comparison: " + itemName + ":" + byteRanges.size());
            if (byteRanges.size() == 1 && byteRanges.elementAt(0).equals("0--1")) {
                byteRanges.setElementAt("0-" + f.length(), 0);
            }
            long amount = 0L;
            int x = 0;
            while (x < byteRanges.size()) {
                if (!byteRanges.elementAt(x).toString().trim().equals("")) {
                    long end;
                    long start = Long.parseLong(byteRanges.elementAt(x).toString().substring(0, byteRanges.elementAt(x).toString().indexOf("-")));
                    String endPart = byteRanges.elementAt(x).toString().substring(byteRanges.elementAt(x).toString().indexOf("-") + 1);
                    if (endPart.equals("")) {
                        endPart = String.valueOf(f.length());
                    }
                    if ((end = Long.parseLong(endPart)) >= 0L) {
                        amount += end - start;
                    }
                }
                ++x;
            }
            Object object = this.statLock;
            synchronized (object) {
                this.transferedBytes += f.length() - amount;
            }
        }
        return itemName.substring(path.length());
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

    public void writeEnd(OutputStream out, String boundary) throws Exception {
        if (!Tunnel2.checkAction(this.action)) {
            throw new Exception("Cancelled");
        }
        out.write((String.valueOf(boundary) + "--\r\n").getBytes("UTF8"));
        out.flush();
        out.close();
    }

    public void writeEntry(String key, String val, OutputStream dos, String boundary) throws Exception {
        Tunnel2.msg(String.valueOf(key) + ":" + val);
        if (!Tunnel2.checkAction(this.action)) {
            throw new Exception("Cancelled");
        }
        dos.write((String.valueOf(boundary) + "\r\n").getBytes("UTF8"));
        dos.write(("Content-Disposition: form-data; name=\"" + key + "\"\r\n").getBytes("UTF8"));
        dos.write("\r\n".getBytes("UTF8"));
        dos.write((String.valueOf(val) + "\r\n").getBytes("UTF8"));
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

