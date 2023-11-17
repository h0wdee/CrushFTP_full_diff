/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import com.crushftp.client.Base64;
import com.crushftp.client.File_S;
import com.crushftp.client.Worker;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UpdateHandler {
    public String CRLF = "\r\n";
    Socket sock = null;
    OutputStream o = null;
    BufferedReader i = null;
    public InputStream di = null;
    static final long serialVersionUID = 0L;
    public long updateMaxSize = 51200L;
    public long updateCurrentLoc = 0L;
    public String updateCurrentStatus = "";
    boolean stopNow = false;
    SimpleDateFormat MMddyyHHmmss = new SimpleDateFormat("MMddyyHHmmss", Locale.US);

    public boolean doSilentUpdate(boolean earlyAccess, String thisVersion, boolean webOnly) throws Exception {
        this.stopNow = false;
        this.updateCurrentLoc = 0L;
        this.updateCurrentStatus = "Building list of files...";
        int minMd5Size = 262144;
        String fname = String.valueOf(System.getProperty("appname", "CrushFTP")) + Common.V() + (Common.machine_is_x() && Common.OSXApp() ? "_OSX" : "");
        this.updateCurrentStatus = "Building list of files..." + fname;
        String url = "https://www." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".com/";
        String username = "early" + Common.V();
        String password = "early" + Common.V();
        String home = System.getProperty("crushftp.home");
        if (Common.machine_is_x() && Common.OSXApp()) {
            home = String.valueOf(System.getProperty("crushftp.home")) + "../../../../";
        }
        final String homeF = new File_S(home).getCanonicalPath();
        String backup1 = String.valueOf(System.getProperty("crushftp.backup")) + "backup/" + this.MMddyyHHmmss.format(new Date()) + "_" + thisVersion + " Files_tmp/";
        String backup2 = String.valueOf(System.getProperty("crushftp.backup")) + "backup/" + this.MMddyyHHmmss.format(new Date()) + "_" + thisVersion + " Files/";
        new File_S(backup1).mkdirs();
        new File_S(backup2).mkdirs();
        if (!new File_S(String.valueOf(home) + fname + "_new.zip").exists() && !new File_S(String.valueOf(home) + fname + "_PC_new.zip").exists()) {
            Vector files2 = new Vector();
            int loops = 0;
            while (files2.size() == 0 && loops++ < 5) {
                files2 = this.getServerFileInfo(fname, username, password, url);
                if (files2.size() != 0) continue;
                Thread.sleep(3000L);
            }
            final Vector files = files2;
            files.removeElementAt(files.size() - 1);
            int x = files.size() - 1;
            while (x >= 0) {
                this.updateCurrentStatus = "Building list of files...:" + x;
                Properties p = (Properties)files.elementAt(x);
                if (p.getProperty("name").equals(String.valueOf(System.getProperty("appname", "CrushFTP").toLowerCase()) + "_init.sh")) {
                    files.remove(x);
                } else if (p.getProperty("name").equals("Info.plist")) {
                    files.remove(x);
                } else if (p.getProperty("name").equals(String.valueOf(System.getProperty("appname", "CrushFTP")) + ".command")) {
                    files.remove(x);
                } else if (p.getProperty("name").equals("CrushFTP7_OSX")) {
                    files.remove(x);
                } else if (p.getProperty("name").equals("CrushFTP7_PC")) {
                    files.remove(x);
                } else if (p.getProperty("name").equals("CrushFTP7.app")) {
                    files.remove(x);
                } else if (p.getProperty("name").equals("CrushFTP7.app.zip")) {
                    files.remove(x);
                } else if (p.getProperty("name").equalsIgnoreCase(".DS_Store")) {
                    files.remove(x);
                } else if (p.getProperty("name").equalsIgnoreCase("thumbs.db")) {
                    files.remove(x);
                } else if (p.getProperty("name").equalsIgnoreCase("logo.png")) {
                    files.remove(x);
                } else if (p.getProperty("name").equalsIgnoreCase("mime_types.txt")) {
                    files.remove(x);
                } else if (p.getProperty("name").equalsIgnoreCase("expired.html")) {
                    files.remove(x);
                } else if (p.getProperty("name").equalsIgnoreCase("AttachmentRedirectorSettings.xml")) {
                    files.remove(x);
                } else if (p.getProperty("name").equalsIgnoreCase("favicon.ico")) {
                    files.remove(x);
                } else if (p.getProperty("name").equalsIgnoreCase("win_service.jar") && Common.machine_is_x()) {
                    files.remove(x);
                } else if (p.getProperty("path").toUpperCase().indexOf("/USERS/") >= 0) {
                    files.remove(x);
                } else if (webOnly && p.getProperty("path").indexOf("/WebInterface/") < 0) {
                    files.remove(x);
                }
                if (this.stopNow) {
                    return false;
                }
                --x;
            }
            final StringBuffer localStatus = new StringBuffer();
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    int x = 0;
                    while (x < files.size()) {
                        UpdateHandler.this.updateCurrentStatus = "Building list of file differences...:" + (x + 1) + " of " + (files.size() + 1);
                        if (UpdateHandler.this.stopNow) break;
                        Properties p = (Properties)files.elementAt(x);
                        String tmpPath = p.getProperty("path");
                        tmpPath = tmpPath.substring(tmpPath.indexOf("/", 1));
                        if (new File_S(String.valueOf(homeF) + tmpPath).exists()) {
                            p.put("localSize", String.valueOf(new File_S(String.valueOf(homeF) + tmpPath).length()));
                            p.put("localModified", String.valueOf(new File_S(String.valueOf(homeF) + tmpPath).lastModified()));
                            if (p.getProperty("type").equals("FILE")) {
                                try {
                                    p.put("localMd5", Common.getMD5(new FileInputStream(new File_S(String.valueOf(homeF) + tmpPath))).trim().toUpperCase().substring(24));
                                }
                                catch (Exception e) {
                                    Log.log("UPDATE", 0, e);
                                }
                            }
                        }
                        ++x;
                    }
                    localStatus.append("done");
                }
            });
            int x2 = 0;
            while (x2 < files.size()) {
                this.updateCurrentStatus = "Building list of server differences...:" + (x2 + 1) + " of " + (files.size() + 1);
                Properties p = (Properties)files.elementAt(x2);
                if (p.getProperty("type").equals("FILE") && !p.containsKey("md5")) {
                    Log.log("UPDATE", 0, "Looking up file md5:" + p);
                    HttpURLConnection urlc = (HttpURLConnection)new URL(String.valueOf(url) + "?command=getMd5s&chunked=false&path=" + Base64.encodeBytes(p.getProperty("path").getBytes("UTF8"))).openConnection();
                    String auth = "Basic " + Base64.encodeBytes((String.valueOf(username) + ":" + password).getBytes());
                    urlc.setRequestProperty("Authorization", auth);
                    urlc.setRequestMethod("GET");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    com.crushftp.client.Common.copyStreams(urlc.getInputStream(), baos, true, true);
                    p.put("md5", "");
                    try {
                        p.put("md5", new String(baos.toByteArray()).trim().toUpperCase().substring(24));
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 1, e);
                    }
                }
                if (this.stopNow) {
                    return false;
                }
                ++x2;
            }
            while (localStatus.length() == 0) {
                Thread.sleep(100L);
            }
            StringBuffer paths = new StringBuffer();
            int largeFiles = 0;
            int smallFiles = 0;
            long totalBytes = 0L;
            int x3 = 0;
            while (x3 < files.size()) {
                Properties p = (Properties)files.elementAt(x3);
                if (p.getProperty("type").equals("DIR")) {
                    String tmpPath = p.getProperty("path");
                    tmpPath = tmpPath.substring(tmpPath.indexOf("/", 1));
                    if (!new File_S(String.valueOf(homeF) + tmpPath).exists()) {
                        new File_S(String.valueOf(homeF) + tmpPath).mkdirs();
                    }
                } else if (!p.getProperty("md5", "1").trim().equalsIgnoreCase(p.getProperty("localMd5", "2").trim())) {
                    paths.append(p.getProperty("path"));
                    if (x3 < files.size() - 1) {
                        paths.append(":");
                    }
                    totalBytes += Long.parseLong(p.getProperty("size"));
                    if (p.getProperty("name").endsWith(".jar") || Long.parseLong(p.getProperty("size")) > 262144L) {
                        Log.log("UPDATE", 0, "Update needed on larger file:" + p.getProperty("path"));
                        ++largeFiles;
                    } else {
                        ++smallFiles;
                    }
                }
                if (this.stopNow) {
                    return false;
                }
                ++x3;
            }
            this.updateMaxSize = totalBytes;
            Log.log("UPDATE", 0, "Updating " + smallFiles + " small files, and " + largeFiles + " large files:" + com.crushftp.client.Common.format_bytes_short2(this.updateMaxSize));
            if (this.stopNow) {
                return false;
            }
            Log.log("UPDATE", 0, "Updating files:" + paths);
            try {
                this.updateCurrentStatus = "Downloading " + (smallFiles + largeFiles) + " files.";
                this.doHTTPDownloads(home, fname, earlyAccess, paths.toString());
                this.updateCurrentStatus = "Unzipping " + (smallFiles + largeFiles) + " files.";
            }
            finally {
                this.updateCurrentLoc = 0L;
                this.updateMaxSize = 51200L;
                this.stopNow = false;
            }
            Common.recurseCopy(String.valueOf(home) + fname + "_new.zip", String.valueOf(backup2) + fname + "_new.zip", true);
        } else {
            if (new File_S(String.valueOf(home) + fname + "_PC_new.zip").exists() && !new File_S(String.valueOf(home) + fname + "_new.zip").exists()) {
                fname = String.valueOf(System.getProperty("appname", "CrushFTP")) + Common.V() + "_PC";
            }
            this.updateCurrentStatus = "Using local offline file...: " + fname + "_new.zip";
            Thread.sleep(1000L);
        }
        try {
            try {
                Common.recurseCopy("./" + System.getProperty("appname", "CrushFTP") + ".jar", String.valueOf(backup1) + System.getProperty("appname", "CrushFTP") + ".jar", true);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                e.printStackTrace();
            }
            try {
                Common.recurseCopy("./plugins/", String.valueOf(backup1) + "plugins/", true);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                e.printStackTrace();
            }
            try {
                Common.recurseCopy(String.valueOf(home) + "WebInterface/", String.valueOf(backup1) + "WebInterface/", true);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                e.printStackTrace();
            }
            Vector list = new Vector();
            Common.appendListing(backup1, list, "", 99, true);
            Vector<Properties> list2 = new Vector<Properties>();
            Properties history_hash = new Properties();
            int x = 0;
            while (x < list.size()) {
                Properties p = new Properties();
                String url1 = "" + ((File_S)list.elementAt(x)).toURI().toURL();
                p.put("url", url1);
                if (!history_hash.containsKey(url1)) {
                    list2.addElement(p);
                }
                history_hash.put(url1, "");
                ++x;
            }
            Common.zip(backup1, list2, String.valueOf(backup2) + "full_app.zip");
            Common.recurseDelete(backup1, false);
            UpdateHandler.doUpdate(home, backup2, String.valueOf(fname) + "_new.zip");
            new File_S(String.valueOf(home) + fname + "_new.zip").delete();
            if (this.stopNow) {
                return false;
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
            e.printStackTrace();
        }
        finally {
            this.updateCurrentLoc = 0L;
            this.updateMaxSize = 51200L;
            this.stopNow = false;
        }
        this.updateCurrentStatus = "COMPLETE";
        Thread.sleep(2000L);
        if (webOnly) {
            new File_S(String.valueOf(home) + fname + "_new.zip").delete();
        }
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                try {
                    Thread.sleep(10000L);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                UpdateHandler.this.updateCurrentStatus = "";
            }
        });
        return true;
    }

    public void cancel() {
        this.stopNow = true;
    }

    public Vector getServerFileInfo(String f, String username, String password, String url) {
        Vector<Properties> serverFileList = new Vector<Properties>();
        HttpURLConnection urlc = null;
        int loops = 0;
        while (loops++ < 5) {
            try {
                URL u = new URL(String.valueOf(url) + f + "/:filetree");
                Log.log("UPDATE", 0, "Getting folder contents information " + u.toExternalForm());
                urlc = (HttpURLConnection)u.openConnection();
                String auth = "Basic " + Base64.encodeBytes((String.valueOf(username) + ":" + password).getBytes());
                urlc.setRequestProperty("Authorization", auth);
                urlc.setReadTimeout(70000);
                urlc.setRequestMethod("GET");
                urlc.setUseCaches(false);
                urlc.setDoInput(true);
                InputStream in = urlc.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF8"));
                String data = "";
                try {
                    while ((data = br.readLine()) != null) {
                        StringTokenizer st = new StringTokenizer(data);
                        Properties p = new Properties();
                        p.put("permissions", st.nextToken());
                        if (p.getProperty("permissions").startsWith("d")) {
                            p.put("type", "DIR");
                        } else {
                            p.put("type", "FILE");
                        }
                        st.nextToken();
                        String owner = st.nextToken();
                        String group = st.nextToken();
                        if (owner.equalsIgnoreCase("MD5")) {
                            p.put("md5", group);
                        }
                        long tempfileSize = Long.parseLong(st.nextToken());
                        p.put("modified", st.nextToken());
                        st.nextToken();
                        String year = st.nextToken();
                        String rootdir = data.substring(data.indexOf(String.valueOf(year) + " /") + (String.valueOf(year) + " ").length()).trim();
                        p.put("path", rootdir);
                        p.put("name", Common.last(rootdir));
                        p.put("size", String.valueOf(tempfileSize));
                        serverFileList.addElement(p);
                    }
                    break;
                }
                finally {
                    urlc.getResponseCode();
                }
            }
            catch (Exception e) {
                Log.log("UPDATE", 0, e);
            }
            finally {
                if (urlc != null) {
                    urlc.disconnect();
                }
            }
        }
        return serverFileList;
    }

    public static void doUpdate(String home, String backup, String zip) throws Exception {
        Vector errors = new Vector();
        if (UpdateHandler.unzip(String.valueOf(home) + zip, errors, backup)) {
            String batch = "";
            String manualFiles = "";
            if (Common.machine_is_windows()) {
                batch = String.valueOf(batch) + "net stop \"" + System.getProperty("appname", "CrushFTP") + " Server\"\r\nping 127.0.0.1 -n 5\r\n";
            }
            int x = 0;
            while (x < errors.size()) {
                Properties p = (Properties)errors.elementAt(x);
                String source = p.getProperty("source");
                String dest = p.getProperty("dest");
                String back = p.getProperty("backup");
                if (Common.machine_is_windows()) {
                    manualFiles = String.valueOf(manualFiles) + source + "\r\n";
                    source = source.replace('/', '\\');
                    dest = dest.replace('/', '\\');
                    back = back.replace('/', '\\');
                    batch = String.valueOf(batch) + "move \"" + dest + "\" \"" + back + "\"\r\n";
                    batch = String.valueOf(batch) + "move \"" + source + "\" \"" + dest + "\"\r\n";
                    batch = Common.replace_str(batch, "\\\\", "\\");
                }
                ++x;
            }
            manualFiles = String.valueOf(manualFiles) + "UpdateTemp/nothing_to_do.txt\r\n";
            if (Common.machine_is_windows()) {
                batch = String.valueOf(batch) + "net start \"" + System.getProperty("appname", "CrushFTP") + " Server\"\r\nping 127.0.0.1 -n 10\r\n";
            }
            if (batch.length() > 0) {
                String fname = "update.sh";
                if (Common.machine_is_windows()) {
                    fname = "update.bat";
                    batch = String.valueOf(batch) + "del \"" + new File_S(String.valueOf(home) + fname).getCanonicalPath() + "\"\r\n";
                }
                RandomAccessFile out = new RandomAccessFile(new File_S(String.valueOf(home) + fname), "rw");
                out.setLength(0L);
                out.write(batch.getBytes());
                out.close();
                out = new RandomAccessFile(new File_S(String.valueOf(home) + "update_list.txt"), "rw");
                out.setLength(0L);
                out.write(manualFiles.getBytes());
                out.close();
            }
        }
    }

    public void doHTTPDownloads(String home, String fname, boolean earlyAccess, String paths) throws Exception {
        String url = "https://www." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".com/";
        String username = "update" + Common.V();
        String password = "update" + Common.V();
        if (earlyAccess) {
            username = "early" + Common.V();
            password = "early" + Common.V();
        }
        this.getFileHTTP(home, fname, "zip", url, paths, username, password);
    }

    public static boolean unzip(String sourcePath, Vector errors, String destPath) throws Exception {
        ZipInputStream zin = new ZipInputStream(new FileInputStream(new File_S(sourcePath)));
        Log.log("UPDATE", 3, "Unzipping:" + sourcePath);
        sourcePath = Common.all_but_last(sourcePath);
        try {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                String path2 = entry.getName();
                path2 = path2.replace('\\', '/');
                path2 = path2.replace('\\', '/');
                path2 = Common.replace_str(path2, "..", "");
                path2 = path2.substring(path2.indexOf("/", 1));
                if (entry.isDirectory()) {
                    new File_S(String.valueOf(sourcePath) + path2).mkdirs();
                    if (path2.indexOf("WebInterface") < 0) {
                        Common.updateOSXInfo(String.valueOf(sourcePath) + path2);
                    }
                    Log.log("UPDATE", 0, "Updating directory:" + sourcePath + path2);
                    continue;
                }
                if (new File_S(Common.all_but_last(String.valueOf(sourcePath) + path2)).mkdirs() && path2.indexOf("WebInterface") < 0) {
                    Common.updateOSXInfo(Common.all_but_last(String.valueOf(sourcePath) + path2));
                }
                byte[] b = new byte[32768];
                int bytes_read = 0;
                String ext = "_tmp";
                if ((String.valueOf(sourcePath) + path2).indexOf("/WebInterface/") >= 0) {
                    ext = "";
                }
                new File_S(String.valueOf(sourcePath) + path2 + "_tmp").delete();
                RandomAccessFile out = new RandomAccessFile(new File_S(String.valueOf(sourcePath) + path2 + ext), "rw");
                if ((String.valueOf(sourcePath) + path2).indexOf("/WebInterface/localizations/") >= 0 && out.length() > 0L) {
                    out.close();
                    out = null;
                }
                boolean skip_update = false;
                if (path2.endsWith(String.valueOf(System.getProperty("appname", "CrushFTP").toLowerCase()) + "_init.sh")) {
                    skip_update = true;
                } else if (path2.endsWith("Info.plist")) {
                    skip_update = true;
                } else if (path2.endsWith(String.valueOf(System.getProperty("appname", "CrushFTP")) + ".command")) {
                    skip_update = true;
                } else if (path2.endsWith("CrushFTP7_OSX")) {
                    skip_update = true;
                } else if (path2.endsWith("CrushFTP7_PC")) {
                    skip_update = true;
                } else if (path2.endsWith("CrushFTP7.app")) {
                    skip_update = true;
                } else if (path2.endsWith("CrushFTP7.app.zip")) {
                    skip_update = true;
                } else if (path2.endsWith(".DS_Store")) {
                    skip_update = true;
                } else if (path2.endsWith("thumbs.db")) {
                    skip_update = true;
                } else if (path2.endsWith("logo.png")) {
                    skip_update = true;
                } else if (path2.endsWith("mime_types.txt")) {
                    skip_update = true;
                } else if (path2.endsWith("AttachmentRedirectorSettings.xml")) {
                    skip_update = true;
                } else if (path2.endsWith("favicon.ico")) {
                    skip_update = true;
                } else if (path2.endsWith("win_service.jar") && Common.machine_is_x()) {
                    skip_update = true;
                } else if (path2.toUpperCase().indexOf("/USERS/") >= 0) {
                    skip_update = true;
                }
                if (skip_update) {
                    out.close();
                    out = null;
                    File_S f = new File_S(String.valueOf(sourcePath) + path2 + ext);
                    if (f.getName().endsWith("_tmp")) {
                        f.delete();
                    }
                }
                if (out != null) {
                    out.setLength(0L);
                    if (path2.indexOf("WebInterface") < 0) {
                        Common.updateOSXInfo(String.valueOf(sourcePath) + path2 + ext);
                    }
                }
                while (bytes_read >= 0) {
                    bytes_read = zin.read(b);
                    if (bytes_read <= 0 || out == null) continue;
                    out.write(b, 0, bytes_read);
                }
                boolean ok1 = false;
                boolean ok2 = false;
                if (out != null) {
                    out.close();
                    new File_S(String.valueOf(sourcePath) + path2 + ext).setLastModified(entry.getTime());
                    Log.log("UPDATE", 0, "Updating:" + sourcePath + path2 + "    to:" + Common.all_but_last(String.valueOf(destPath) + path2));
                    if (new File_S(Common.all_but_last(String.valueOf(destPath) + path2)).mkdirs() && path2.indexOf("WebInterface") < 0) {
                        Common.updateOSXInfo(Common.all_but_last(String.valueOf(destPath) + path2));
                    }
                    if (!ext.equals("")) {
                        ok1 = new File_S(String.valueOf(sourcePath) + path2).renameTo(new File_S(String.valueOf(destPath) + path2));
                        boolean bl = ok2 = !new File_S(String.valueOf(sourcePath) + path2).exists();
                    }
                }
                if (ok1 || ok2 || ext.equals("") || out == null) {
                    if (out == null) continue;
                    if (!ext.equals("")) {
                        new File_S(String.valueOf(sourcePath) + path2 + ext).renameTo(new File_S(String.valueOf(sourcePath) + path2));
                    }
                    if (path2.indexOf("WebInterface") < 0) {
                        Common.updateOSXInfo(String.valueOf(sourcePath) + path2);
                    }
                    Log.log("UPDATE", 3, "OK");
                    new File_S(String.valueOf(sourcePath) + path2).setLastModified(entry.getTime());
                    continue;
                }
                Log.log("UPDATE", 0, "DEFERRED:" + sourcePath + path2 + "    to:" + Common.all_but_last(String.valueOf(destPath) + path2));
                if (new File_S(Common.all_but_last(String.valueOf(sourcePath) + "UpdateTemp/" + path2)).mkdirs() && path2.indexOf("WebInterface") < 0) {
                    Common.updateOSXInfo(Common.all_but_last(String.valueOf(sourcePath) + "UpdateTemp/" + path2));
                }
                new File_S(String.valueOf(sourcePath) + path2 + "_tmp").renameTo(new File_S(String.valueOf(sourcePath) + "UpdateTemp/" + path2));
                if (path2.indexOf("WebInterface") < 0) {
                    Common.updateOSXInfo(String.valueOf(sourcePath) + "UpdateTemp/" + path2);
                }
                Properties p = new Properties();
                p.put("source", String.valueOf(sourcePath) + "UpdateTemp/" + path2);
                p.put("dest", String.valueOf(sourcePath) + path2);
                p.put("backup", new File_S(String.valueOf(destPath) + path2).getCanonicalPath());
                errors.addElement(p);
                Log.log("UPDATE", 3, "Updating for later:" + sourcePath + "UpdateTemp/" + path2);
            }
            Common.updateOSXInfo(String.valueOf(sourcePath) + "WebInterface/", "-R");
        }
        catch (Exception e) {
            Log.log("UPDATE", 1, e);
            zin.close();
            return false;
        }
        zin.close();
        return true;
    }

    public String getFileHTTP(String localPath, String name, String ext, String url, String paths, String username, String password) throws Exception {
        HttpURLConnection urlc = (HttpURLConnection)new URL(url).openConnection();
        String auth = "Basic " + Base64.encodeBytes((String.valueOf(username) + ":" + password).getBytes());
        urlc.setRequestProperty("Authorization", auth);
        urlc.setRequestMethod("POST");
        urlc.setDoOutput(true);
        urlc.getOutputStream().write(("command=downloadAsZip&path_shortening=false&paths=" + paths).getBytes("UTF8"));
        this.di = urlc.getInputStream();
        this.updateCurrentLoc = 0L;
        int bytes_read = 0;
        byte[] temp_array = new byte[32768];
        RandomAccessFile of_stream = null;
        new File_S(String.valueOf(localPath) + name + "_new." + ext).delete();
        String out_file = String.valueOf(name) + "_new." + ext;
        of_stream = new RandomAccessFile(new File_S(String.valueOf(localPath) + out_file), "rw");
        try {
            of_stream.setLength(0L);
            bytes_read = this.di.read(temp_array);
            this.updateCurrentLoc += (long)bytes_read;
            while (bytes_read > 0 && !this.stopNow) {
                of_stream.write(temp_array, 0, bytes_read);
                bytes_read = this.di.read(temp_array);
                if (bytes_read <= 0) continue;
                this.updateCurrentLoc += (long)bytes_read;
            }
            this.di.close();
        }
        finally {
            of_stream.close();
        }
        if (this.stopNow) {
            return "cancelled";
        }
        this.updateCurrentLoc = this.updateMaxSize;
        return "";
    }
}

