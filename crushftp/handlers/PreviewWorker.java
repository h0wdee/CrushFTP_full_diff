/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import com.crushftp.client.File_S;
import com.crushftp.client.File_U;
import com.crushftp.client.GenericClient;
import com.crushftp.client.S3CrushClient;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import crushftp.db.SearchHandler;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.server.ServerStatus;
import crushftp.server.daemon.ServerBeat;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PreviewWorker
implements Serializable {
    private static final long serialVersionUID = 1L;
    Object convertSync = new Object();
    Object convertSync2 = new Object();
    Thread dirScannerThread = null;
    public Vector convertItems = new Vector();
    int conversionThreadsRunning = 0;
    Vector messages = null;
    public Properties badFiles = new Properties();
    public Properties prefs = null;
    public boolean abort = false;
    public static Properties byte_validation = new Properties();
    public static Properties exif_cache = new Properties();

    public PreviewWorker(Properties prefs) {
        this.prefs = prefs;
        try {
            Common.updateOSXInfo_U(String.valueOf(new File(String.valueOf(ServerStatus.SG("previews_path")) + "Preview").getCanonicalPath()) + File.pathSeparator, "-R");
        }
        catch (IOException iOException) {
            // empty catch block
        }
        byte_validation.put("jpg0", new byte[]{-1, -40, -1, -37});
        byte_validation.put("jpg1", new byte[]{-1, -40, -1, -32});
        byte_validation.put("jpg2", new byte[]{-1, -40, -1, -31});
        byte_validation.put("jpg3", new byte[]{-1, -40, -1, -18});
        byte_validation.put("jpg4", new byte[]{-1, -40, -1, -30});
        byte_validation.put("jpeg0", new byte[]{-1, -40, -1, -37});
        byte_validation.put("jpeg1", new byte[]{-1, -40, -1, -32});
        byte_validation.put("jpeg2", new byte[]{-1, -40, -1, -31});
        byte_validation.put("jpeg3", new byte[]{-1, -40, -1, -18});
        byte_validation.put("jpeg4", new byte[]{-1, -40, -1, -30});
        byte_validation.put("jpeg5", new byte[]{-1, -40, -1, -2});
        byte_validation.put("gif0", new byte[]{71, 73, 70, 56, 55, 97});
        byte_validation.put("gif1", new byte[]{71, 73, 70, 56, 57, 97});
        byte_validation.put("png0", new byte[]{-119, 80, 78, 71, 13, 10, 26, 10});
        byte_validation.put("bmp0", new byte[]{66, 77});
        byte_validation.put("pdf0", new byte[]{37, 80, 68, 70});
        byte_validation.put("psd0", new byte[]{56, 66, 80, 83});
        byte[] byArray = new byte[4];
        byArray[0] = 73;
        byArray[1] = 73;
        byArray[2] = 42;
        byte_validation.put("tif0", byArray);
        byte[] byArray2 = new byte[4];
        byArray2[0] = 77;
        byArray2[1] = 77;
        byArray2[3] = 42;
        byte_validation.put("tif1", byArray2);
        byte[] byArray3 = new byte[4];
        byArray3[0] = 73;
        byArray3[1] = 73;
        byArray3[2] = 42;
        byte_validation.put("tiff0", byArray3);
        byte[] byArray4 = new byte[4];
        byArray4[0] = 77;
        byArray4[1] = 77;
        byArray4[3] = 42;
        byte_validation.put("tiff1", byArray4);
        byte[] byArray5 = new byte[10];
        byArray5[0] = 73;
        byArray5[1] = 73;
        byArray5[2] = 42;
        byArray5[4] = 16;
        byArray5[8] = 67;
        byArray5[9] = 82;
        byte_validation.put("cr20", byArray5);
        byte_validation.put("ai0", new byte[]{37, 33, 80, 83});
        byte_validation.put("ai1", new byte[]{37, 80, 68, 70});
        byte_validation.put("eps0", new byte[]{37, 33, 80, 83});
        byte_validation.put("eps1", new byte[]{-59, -48, -45, -58});
    }

    public static void getDefaults(Properties prefs) {
        Vector<Properties> preview_config = new Vector<Properties>();
        Properties p = new Properties();
        p.put("preview_enabled", "false");
        p.put("preview_debug", "true");
        p.put("preview_scan_interval", "10");
        p.put("preview_conversion_threads", "1");
        p.put("preview_file_extensions", ".jpg, .jpeg, .gif, .png, .bmp, .pdf, .psd, .tif, .tiff, .zip");
        if (Common.machine_is_x_10_5_plus()) {
            p.put("preview_file_extensions", "*");
        }
        Vector<String> sizes = new Vector<String>();
        sizes.addElement("80x80");
        sizes.addElement("160x160");
        sizes.addElement("800x800");
        p.put("preview_sizes", sizes);
        p.put("preview_subdirectories", "true");
        p.put("preview_reverseSubdirectories", "true");
        p.put("preview_frames", "1");
        p.put("preview_movie_info_command_line", "");
        p.put("preview_exif", "false");
        p.put("preview_exif_get_command_line", "exiftool -S %src%");
        p.put("preview_exif_set_command_line", "exiftool -overwrite_original_in_place -%key%=%val% %src%");
        p.put("preview_wait_timeout", "600");
        if (Common.machine_is_windows()) {
            p.put("preview_working_dir", "C:\\Program Files\\ImageMagick-6.5.0-Q16\\");
        } else {
            p.put("preview_working_dir", "");
        }
        p.put("preview_environment", "");
        if (Common.machine_is_windows()) {
            p.put("preview_command_line", "convert.exe -colorspace RGB -strip -alpha off -geometry %width%x%width% -quality 75 %src%[0] %dst%");
            p.put("preview_environment", "MAGICK_HOME_OFF=./;DYLD_LIBRARY_PATH_OFF=./lib");
        } else if (Common.machine_is_x_10_5_plus()) {
            p.put("preview_command_line", "./OSX_scripts/qlmanage_wrapper.sh %width% %previews%temp/%random%/ %src% %dst%");
            p.put("preview_file_extensions", ".jpg, .jpeg, .gif, .png, .bmp, .pdf, .psd, .tif, .tiff, .zip, *.txt, *.rtf, *.doc, *.docx, *.xls, *.xlsx, *.pdf");
        } else if (Common.machine_is_x()) {
            p.put("preview_command_line", "sips -Z %width% -s format jpeg %src% -m /System/Library/ColorSync/Profiles/Generic\\ RGB\\ Profile.icc --out %dst%");
        } else {
            p.put("preview_command_line", "convert -colorspace RGB -strip -alpha off -geometry %width%x%width% -quality 75 %src%[0] %dst%");
            p.put("preview_environment", "MAGICK_HOME=/Applications/ImageMagick-6.3.6/;DYLD_LIBRARY_PATH=/Applications/ImageMagick-6.3.6/lib;PATH=/opt/local/bin:/opt/local/sbin:/bin");
        }
        p.put("preview_folder_list", "");
        preview_config.addElement(p);
        prefs.put("preview_configs", preview_config);
    }

    public void run(Properties info) {
        if (!ServerBeat.current_master) {
            if (ServerStatus.BG("single_preview_serverbeat")) {
                return;
            }
        }
        if (!this.prefs.getProperty("preview_enabled", "false").equalsIgnoreCase("true")) {
            return;
        }
        if (this.abort) {
            return;
        }
        if (this.dirScannerThread == null) {
            if (this.prefs.getProperty("preview_file_extensions").equals(".jpg, .jpeg, .gif, .png, .bmp, .ai, .pdf, .psd, .tif, .tiff, .cr2, .dng, .crw, .dcr, .mrw, .nef, .orf, .pef, .srf, .eps")) {
                this.prefs.put("preview_file_extensions", ".jpg, .jpeg, .gif, .png, .bmp, .ai, .pdf, .psd, .tif, .tiff, .cr2, .eps");
            }
            if (Common.machine_is_x_10_5_plus()) {
                try {
                    com.crushftp.client.Common.check_exec();
                    Common.exec(new String[]{"chmod", "+x", new File_S(String.valueOf(System.getProperty("crushftp.home")) + "qlmanage_wrapper.sh").getCanonicalPath()});
                    Common.exec(new String[]{"chmod", "+x", new File_S(String.valueOf(System.getProperty("crushftp.home")) + "OSX_scripts/qlmanage_wrapper.sh").getCanonicalPath()});
                }
                catch (Exception e) {
                    Log.log("PREVIEW", 1, e);
                }
                try {
                    com.crushftp.client.Common.check_exec();
                    Common.exec(new String[]{"chmod", "+x", new File_S(String.valueOf(System.getProperty("crushftp.home")) + "pcastaction_wrapper.sh").getCanonicalPath()});
                    Common.exec(new String[]{"chmod", "+x", new File_S(String.valueOf(System.getProperty("crushftp.home")) + "OSX_scripts/pcastaction_wrapper.sh").getCanonicalPath()});
                }
                catch (Exception e) {
                    Log.log("PREVIEW", 1, e);
                }
            }
            this.msg("Started directory scanner.");
            class Runner
            implements Runnable {
                Runner() {
                }

                /*
                 * Unable to fully structure code
                 */
                @Override
                public void run() {
                    try {
                        block4: do {
                            if (!(PreviewWorker.this.prefs.get("preview_folder_list") instanceof String)) {
                                folderList = (Vector<E>)PreviewWorker.this.prefs.get("preview_folder_list");
                                if (folderList == null) {
                                    folderList = new Vector<E>();
                                }
                                x = 0;
                                while (x < folderList.size()) {
                                    url = "";
                                    settings = new Properties();
                                    if (!folderList.elementAt(x).toString().equals("null") && !folderList.elementAt(x).toString().equals("")) {
                                        if (folderList.elementAt(x) instanceof String) {
                                            url = folderList.elementAt(x).toString();
                                        }
                                        if (folderList.elementAt(x) instanceof Properties) {
                                            settings = (Properties)folderList.elementAt(x);
                                            url = settings.getProperty("url", "");
                                        }
                                        if (folderList.elementAt(x) instanceof Vector && (v = (Vector)folderList.elementAt(x)).size() != 0) {
                                            settings = (Properties)v.get(0);
                                            url = settings.getProperty("url", "");
                                        }
                                        if (url.startsWith("/")) {
                                            url = "file:/" + url;
                                        } else if (url.charAt(1) == ':') {
                                            url = "file://" + url;
                                        }
                                        c = Common.getClient(Common.getBaseUrl(url), "PREVIEW", new Vector<E>());
                                        if (settings.size() != 0) {
                                            c.setConfigObj(settings);
                                        }
                                        vrl = new VRL(url);
                                        if (c instanceof S3CrushClient) {
                                            c.login(vrl.getUsername(), vrl.getPassword(), Common.all_but_last(vrl.getPath()));
                                        } else {
                                            c.login(vrl.getUsername(), vrl.getPassword(), null);
                                        }
                                        dest = PreviewWorker.getDestPath2(url);
                                        if (!new File_U(dest).exists()) {
                                            new File_U(dest).mkdirs();
                                        }
                                        Common.updateOSXInfo_U(dest, "-R");
                                        dest = String.valueOf(dest) + vrl.getName() + "/";
                                        if (PreviewWorker.this.get("preview_reverseSubdirectories").equals("true")) {
                                            if (vrl.getProtocol().equalsIgnoreCase("file")) {
                                                PreviewWorker.this.msg("Checking for deleted items:" + dest);
                                                PreviewWorker.this.reverseRecurseConvert(dest, 0, 10);
                                            } else {
                                                PreviewWorker.this.msg("Skipping check for deleted items for non local filesystem:" + com.crushftp.client.Common.getCanonicalPath(url));
                                            }
                                        }
                                        PreviewWorker.this.msg("Checking for new items:" + vrl.getPath());
                                        PreviewWorker.this.recurseConvert(c, vrl, 0, Integer.parseInt(PreviewWorker.this.prefs.getProperty("preview_subdirectories", "").equals("true") != false ? "10" : "0"));
                                        PreviewWorker.this.msg("New item check complete:" + vrl.getPath());
                                    }
                                    ++x;
                                }
                                PreviewWorker.this.msg("New item check completed.");
                            }
                            intervals = 0;
                            ** GOTO lbl81
                            {
                                p = (Properties)PreviewWorker.this.convertItems.elementAt(0);
                                PreviewWorker.this.msg("Processing item:" + p.toString());
                                path = "";
                                try {
                                    path = new URL(p.getProperty("url", "")).getPath();
                                }
                                catch (Exception e) {
                                    PreviewWorker.this.msg(e);
                                }
                                PreviewWorker.this.msg("Processing item's path:" + path);
                                c = Common.getClient(Common.getBaseUrl(p.getProperty("url", "")), "PREVIEW", new Vector<E>());
                                vrl = new VRL(p.getProperty("url", ""));
                                if (c instanceof S3CrushClient) {
                                    c.login(vrl.getUsername(), vrl.getPassword(), Common.all_but_last(vrl.getPath()));
                                } else {
                                    c.login(vrl.getUsername(), vrl.getPassword(), null);
                                }
                                stat = c.stat(vrl.getPath());
                                if (Long.parseLong(stat.getProperty("size", "0")) > 0L) {
                                    PreviewWorker.this.doConvert(c, stat, null, true, new Properties(), false);
                                }
                                PreviewWorker.this.convertItems.removeElementAt(0);
                                do {
                                    if (PreviewWorker.this.convertItems.size() > 0 && PreviewWorker.this.prefs.getProperty("preview_enabled").equalsIgnoreCase("true") && !PreviewWorker.this.abort) continue block6;
                                    Thread.sleep(1000L);
                                    if (!PreviewWorker.this.prefs.getProperty("preview_enabled").equalsIgnoreCase("true") || PreviewWorker.this.abort) continue block4;
lbl81:
                                    // 2 sources

                                } while (intervals++ < Integer.parseInt(PreviewWorker.this.prefs.getProperty("preview_scan_interval", "10")) * 60);
                            }
                        } while (PreviewWorker.this.prefs.getProperty("preview_enabled").equalsIgnoreCase("true") && !PreviewWorker.this.abort);
                    }
                    catch (Exception e) {
                        PreviewWorker.this.msg(e);
                    }
                    PreviewWorker.this.dirScannerThread = null;
                }
            }
            Runner runner = new Runner();
            if (this.dirScannerThread == null) {
                this.dirScannerThread = new Thread(runner);
                this.dirScannerThread.setName("Preview:dirScanner");
                this.dirScannerThread.setPriority(1);
                this.dirScannerThread.start();
            }
        }
        if (info != null && info.getProperty("action", "").equals("event")) {
            Vector items = (Vector)info.get("items");
            this.msg("item list size:" + items.size());
            this.msg("items:" + items);
            int x = 0;
            while (x < items.size()) {
                Properties p = (Properties)items.elementAt(x);
                this.convertItems.addElement(p.clone());
                ++x;
            }
        }
    }

    public boolean checkExtension(String name, Properties stat) {
        String ext = name.substring(name.lastIndexOf(".")).toUpperCase();
        String[] exts = this.prefs.getProperty("preview_file_extensions", "").toUpperCase().split(",");
        boolean ok_ext = false;
        boolean ok_size = true;
        int x = 0;
        while (x < exts.length) {
            if (exts[x].indexOf(ext) >= 0) {
                ok_ext = true;
                if (exts[x].indexOf(":") < 0) break;
                long size = Long.parseLong(exts[x].substring(exts[x].indexOf(":") + 1)) * 1024L * 1024L;
                if (Long.parseLong(stat.getProperty("size", "0")) <= size) break;
                ok_size = false;
                break;
            }
            ++x;
        }
        if (this.prefs.getProperty("preview_file_extensions", "").equals("*")) {
            ok_ext = true;
        }
        if (!ok_ext) {
            return false;
        }
        if (!ok_size) {
            this.msg("Image too large, skipping:" + new VRL(stat.getProperty("url")).getPath());
            return false;
        }
        return ok_ext;
    }

    public boolean doConvert(GenericClient c, Properties stat_src, Properties stat_dst, boolean multiThread, Properties info, boolean override) {
        String name;
        if (new File_S(String.valueOf(System.getProperty("crushftp.home")) + "reset_preview_bad_files").exists()) {
            new File_S(String.valueOf(System.getProperty("crushftp.home")) + "reset_preview_bad_files").delete();
            this.badFiles.clear();
        }
        if ((name = stat_src.getProperty("name")).indexOf(".") >= 0 && !name.startsWith(".")) {
            if (!this.checkExtension(name, stat_src) && !override) {
                return false;
            }
            if (stat_dst == null) {
                stat_dst = stat_src;
            }
            String dest = PreviewWorker.getDestPath2(stat_dst.getProperty("url"));
            VRL vrl = new VRL(stat_dst.getProperty("url"));
            if (!new File_U(dest = String.valueOf(dest) + vrl.getName() + "/").exists()) {
                new File_U(dest).mkdirs();
            }
            Common.updateOSXInfo_U(dest, "-R");
            if (!new File_U(String.valueOf(dest) + "/p1/1.jpg").exists() || Long.parseLong(stat_src.getProperty("modified")) != new File_U(dest).lastModified() || override) {
                block10: {
                    if (!this.badFiles.containsKey(stat_src.getProperty("url")) || !this.badFiles.getProperty(stat_src.getProperty("url")).equals(stat_src.getProperty("modified"))) break block10;
                    this.msg("Skipping file that can't be converted:" + vrl.getPath());
                    return false;
                }
                try {
                    this.msg("Converting: " + vrl.getPath() + "      to: " + dest + "  exists:" + new File_U(String.valueOf(dest) + "/p1/1.jpg").exists() + "  modified:" + stat_src.getProperty("modified") + " vs " + new File_U(dest).lastModified());
                    if (stat_src != null) {
                        Properties p = new Properties();
                        c.setConfigObj(p);
                        stat_src.put("settings", p);
                    }
                    this.convert(stat_src, dest, multiThread, info);
                    return true;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        return false;
    }

    public void msg(String s) {
        if (this.messages != null) {
            this.messages.addElement("Preview:" + s);
        } else if (this.prefs.getProperty("preview_debug").equals("true")) {
            Log.log("PREVIEW", 0, s);
        }
    }

    public void msg(Exception e) {
        if (this.messages != null) {
            this.messages.addElement(e);
        } else if (this.prefs.getProperty("preview_debug").equals("true")) {
            Log.log("PREVIEW", 0, e);
        }
    }

    public static String getDestPath2(String url) {
        VRL vrl = new VRL(url);
        String destPath = vrl.getPath();
        if (vrl.getProtocol().equalsIgnoreCase("file")) {
            try {
                if (Common.machine_is_windows() && url.toLowerCase().startsWith("file:////")) {
                    destPath = String.valueOf(new File_U(Common.url_decode(url.substring(6))).getParentFile().getPath()) + "/";
                    destPath = String.valueOf(new File_U(Common.url_decode(url.substring(6))).getCanonicalFile().getParentFile().getPath()) + "/";
                } else {
                    destPath = String.valueOf(new File_U(vrl.getPath()).getParentFile().getPath()) + "/";
                    destPath = String.valueOf(new File_U(vrl.getPath()).getCanonicalFile().getParentFile().getPath()) + "/";
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (destPath.indexOf(":") >= 0 && destPath.toUpperCase().startsWith("C:")) {
                destPath = destPath.substring(destPath.indexOf(":") + 1);
            } else if (destPath.indexOf(":") >= 0) {
                destPath = "/_-_" + destPath.substring(0, destPath.indexOf(":")) + "_-_" + destPath.substring(destPath.indexOf(":") + 1);
            } else if (Common.machine_is_windows() && destPath.replace('\\', '/').startsWith("//")) {
                destPath = "/_UNC_" + destPath.substring(2);
            }
            destPath = destPath.replace('\\', '/');
        } else {
            try {
                destPath = Common.all_but_last(com.crushftp.client.Common.getCanonicalPath(url));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return String.valueOf(ServerStatus.SG("previews_path")) + "Preview" + destPath;
    }

    public String get(String key) {
        return this.prefs.getProperty(key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean convert(Properties stat, String destFile, boolean multiThread, Properties info) {
        try {
            class MultiConvert
            implements Runnable {
                private final /* synthetic */ Properties val$stat;
                private final /* synthetic */ String val$destFile;
                private final /* synthetic */ Properties val$info;

                MultiConvert(Properties properties, String string, Properties properties2) {
                    this.val$stat = properties;
                    this.val$destFile = string;
                    this.val$info = properties2;
                }

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public void run() {
                    try {
                        String temp_loc = PreviewWorker.this.convertCommandLine(this.val$stat, new File_U(this.val$destFile).getCanonicalPath());
                        VRL vrl = new VRL(this.val$stat.getProperty("url"));
                        Properties metaInfo = new Properties();
                        if (vrl.getProtocol().equalsIgnoreCase("file")) {
                            metaInfo = PreviewWorker.this.getExifInfo(vrl.getPath(), this.val$destFile);
                        } else if (temp_loc != null) {
                            metaInfo = PreviewWorker.this.getExifInfo(temp_loc, this.val$destFile);
                        }
                        this.val$info.putAll((Map<?, ?>)metaInfo);
                        if (temp_loc != null) {
                            new File_U(temp_loc).delete();
                        }
                    }
                    catch (Exception e) {
                        PreviewWorker.this.msg(e);
                    }
                    Object object = PreviewWorker.this.convertSync2;
                    synchronized (object) {
                        --PreviewWorker.this.conversionThreadsRunning;
                    }
                }
            }
            VRL vrl = new VRL(stat.getProperty("url"));
            this.msg("Creating thumbnail for:" + vrl.getPath());
            Object object = this.convertSync;
            synchronized (object) {
                while (this.conversionThreadsRunning >= Integer.parseInt(this.get("preview_conversion_threads"))) {
                    Thread.sleep(500L);
                }
            }
            object = this.convertSync2;
            synchronized (object) {
                ++this.conversionThreadsRunning;
            }
            if (!multiThread) {
                new MultiConvert(stat, destFile, info).run();
            } else {
                Worker.startWorker(new MultiConvert(stat, destFile, info), "Preview:converting " + vrl.getPath());
            }
        }
        catch (Exception e) {
            this.msg(e);
        }
        return true;
    }

    public String getZipEntries(String srcFile) throws Exception {
        if (!srcFile.toUpperCase().endsWith(".ZIP")) {
            return "";
        }
        StringBuffer entries = new StringBuffer();
        ZipInputStream zin = new ZipInputStream(new FileInputStream(new File_U(srcFile)));
        ZipEntry entry = null;
        int loops = 0;
        while ((entry = zin.getNextEntry()) != null) {
            String path = com.crushftp.client.Common.dots(entry.getName().replace('\\', '/'));
            if (path.indexOf("__MACOSX/") >= 0 || path.indexOf(".DS_Store") >= 0) continue;
            entries.append(String.valueOf(path) + "\r\n");
            if (++loops > 100) break;
        }
        zin.close();
        return entries.toString();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String convertCommandLine(Properties stat, String destFile) throws Exception {
        if (Common.machine_is_windows() && destFile.length() > 245) {
            this.badFiles.put(stat.getProperty("url"), stat.getProperty("modified"));
            return null;
        }
        VRL vrl = new VRL(stat.getProperty("url"));
        Vector sizes = (Vector)this.prefs.get("preview_sizes");
        try {
            new File_U(destFile).mkdirs();
            Common.updateOSXInfo_U(String.valueOf(destFile) + "/", "-R");
            boolean exists = new File_U(String.valueOf(destFile) + "/index.txt").exists();
            RandomAccessFile index = new RandomAccessFile(new File_U(String.valueOf(destFile) + "/index.txt"), "rw");
            byte[] b = new byte[(int)index.length()];
            index.readFully(b);
            index.seek(index.length());
            if (new String(b).toUpperCase().indexOf(stat.getProperty("name").toUpperCase()) < 0 && vrl.getProtocol().equalsIgnoreCase("file") && !exists) {
                index.write(this.getZipEntries(vrl.getPath()).getBytes());
                if (new File_S(String.valueOf(System.getProperty("crushftp.search")) + "tika-app.jar").exists() && this.prefs.getProperty("preview_file_extensions", "").indexOf("tika") >= 0) {
                    byte[] b2 = SearchHandler.getContents(vrl, ServerStatus.IG("search_max_content_kb"));
                    index.write("\r\n".getBytes());
                    index.write(b2);
                }
            }
            index.close();
        }
        catch (Exception e) {
            this.msg(e);
        }
        String[] envp = this.get("preview_environment").split(";");
        if (envp.length == 0 || envp.length == 1 && envp[0].trim().length() == 0) {
            envp = null;
        }
        float duration = 0.0f;
        float loops = Integer.parseInt(this.prefs.getProperty("preview_frames", "1"));
        if (loops == 0.0f) {
            loops = 1.0f;
        }
        String srcFile2 = "";
        boolean temp = false;
        if (vrl.getProtocol().equalsIgnoreCase("file")) {
            srcFile2 = vrl.getPath();
            if (Common.machine_is_windows() && srcFile2.startsWith("/")) {
                if (stat.getProperty("url").toLowerCase().startsWith("file:////")) {
                    srcFile2 = Common.url_decode(stat.getProperty("url").substring(6));
                }
                srcFile2 = srcFile2.substring(1).replace('/', '\\');
            }
        } else {
            GenericClient c = Common.getClient(Common.getBaseUrl(stat.getProperty("url")), "PREVIEW", new Vector());
            if (stat.containsKey("settings") && stat.get("settings") != null) {
                c.setConfigObj((Properties)stat.get("settings"));
            }
            if (c instanceof S3CrushClient) {
                c.login(vrl.getUsername(), vrl.getPassword(), Common.all_but_last(vrl.getPath()));
            } else {
                c.login(vrl.getUsername(), vrl.getPassword(), null);
            }
            srcFile2 = String.valueOf(ServerStatus.SG("previews_path")) + "Preview/tmp/" + Common.makeBoundary(3) + "_" + vrl.getName();
            new File_U(String.valueOf(ServerStatus.SG("previews_path")) + "Preview/tmp/").mkdirs();
            temp = true;
            if (c.stat(vrl.getPath()) == null) {
                this.msg("Missing file : " + vrl.safe());
            }
            com.crushftp.client.Common.streamCopier(null, null, c.download(vrl.getPath(), 0L, -1L, true), new FileOutputStream(new File_U(srcFile2)), false, true, true);
            srcFile2 = new File_U(srcFile2).getCanonicalPath();
            this.msg("Copying file down to temp folder:" + vrl.getPath() + "-->" + srcFile2 + ":Done");
        }
        if (loops > 1.0f) {
            String s = this.prefs.getProperty("preview_movie_info_command_line", "");
            if (Common.machine_is_windows() && !s.toUpperCase().startsWith("CMD /")) {
                s = "CMD /C " + s.trim();
            }
            String[] args = s.split(" ");
            this.msg("Getting duration of movie:" + this.change_vars(s, srcFile2, destFile, 0.0f, 0));
            int l = 0;
            while (l < args.length) {
                args[l] = this.change_vars(args[l], srcFile2, destFile, 0.0f, 0);
                ++l;
            }
            File_S f = new File_S(this.get("preview_working_dir"));
            if (this.get("preview_working_dir").equals("")) {
                f = new File_S(System.getProperty("crushftp.home"));
            }
            try {
                if (s.toLowerCase().indexOf("convert") >= 0 || s.toLowerCase().indexOf("magic") >= 0) {
                    this.validateBytes(new File_U(srcFile2));
                }
                com.crushftp.client.Common.check_exec();
                Process proc = Runtime.getRuntime().exec(args, envp, (File)f);
                BufferedReader br1 = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                BufferedReader br2 = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                duration = this.getDuration(br1);
                if (duration < 0.0f) {
                    duration = this.getDuration(br2);
                }
                if (duration < 0.0f) {
                    duration = 0.0f;
                }
                Worker.startWorker(new discarder(proc.getInputStream()), "Discard input stream:" + vrl.getPath());
                Worker.startWorker(new discarder(proc.getErrorStream()), "Discard error stream:" + vrl.getPath());
                Thread.sleep(1000L);
                proc.destroy();
            }
            catch (Exception e) {
                this.msg(e);
            }
            this.msg("Duration:" + duration);
        }
        float step = duration / loops;
        float loc = 0.0f;
        int loop = 1;
        while ((float)loop <= loops) {
            loc += step;
            int xx = sizes.size() - 1;
            while (xx >= 0) {
                int page_loop;
                String size = sizes.elementAt(xx).toString();
                String width = size.substring(0, size.indexOf("x"));
                String height = size.substring(size.indexOf("x") + 1);
                String page = "p" + loop;
                int total_pages = 0;
                if (xx <= sizes.size() - 2) {
                    page_loop = 2;
                    while (new File_U(String.valueOf(destFile) + "/p" + page_loop++ + "/3.jpg").exists()) {
                        ++total_pages;
                    }
                }
                page_loop = 0;
                while (page_loop <= total_pages || loop > 1) {
                    if (loop == 1) {
                        page = "p" + (page_loop + 1);
                    }
                    try {
                        String s;
                        String command_line = this.get("preview_command_line");
                        if (Common.machine_is_windows() && !command_line.toUpperCase().startsWith("CMD /")) {
                            command_line = "CMD /C " + command_line.trim();
                        }
                        if ((command_line = Common.replace_str(command_line, "\\ ", "\u00fe")).startsWith("./ql_manage_wrapper.sh") && new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "OSX_scripts/ql_manage_wrapper.sh").exists()) {
                            command_line = Common.replace_str(command_line, "ql_manage_wrapper.sh", "OSX_scripts/ql_manage_wrapper.sh");
                        }
                        if (command_line.startsWith("./pcastaction_wrapper.sh") && new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "OSX_scripts/pcastaction_wrapper.sh").exists()) {
                            command_line = Common.replace_str(command_line, "pcastaction_wrapper.sh", "OSX_scripts/pcastaction_wrapper.sh");
                        }
                        String[] convert = command_line.split(" ");
                        String[] convertCmd = null;
                        convertCmd = new String[convert.length];
                        String command_line_result = "";
                        String altSrcFile = srcFile2;
                        int x = 0;
                        while (x < convert.length) {
                            s = convert[x];
                            s = s.replace('\u00fe', ' ');
                            s = Common.replace_str(s, "%width%", width);
                            s = Common.replace_str(s, "%height%", height);
                            if (xx < sizes.size() - 1) {
                                altSrcFile = String.valueOf(destFile) + "/" + page + "/" + sizes.size() + ".jpg";
                            }
                            s = this.change_vars(s, altSrcFile, String.valueOf(destFile) + "/" + page + "/" + (xx + 1) + ".jpg", loc, 0);
                            convertCmd[x] = s = Common.url_decode(s);
                            command_line_result = String.valueOf(command_line_result) + s + " ";
                            ++x;
                        }
                        new File_U(String.valueOf(destFile) + "/" + page).mkdirs();
                        Common.updateOSXInfo_U(String.valueOf(destFile) + "/" + page + "/", "-R");
                        this.msg(command_line_result);
                        if (loops > 1.0f && xx < sizes.size() - 1) {
                            Common.copy_U(altSrcFile, String.valueOf(destFile) + "/" + page + "/" + (xx + 1) + ".jpg", true);
                            new File_U(String.valueOf(destFile) + "/" + page + "/" + (xx + 1) + ".jpg").setLastModified(Long.parseLong(stat.getProperty("modified")));
                            break;
                        }
                        File_S f = new File_S(this.get("preview_working_dir"));
                        if (this.get("preview_working_dir").equals("")) {
                            f = new File_S(System.getProperty("crushftp.home"));
                        }
                        if ((s = command_line).toLowerCase().indexOf("convert") >= 0 || s.toLowerCase().indexOf("magic") >= 0) {
                            this.validateBytes(new File_U(srcFile2));
                        }
                        com.crushftp.client.Common.check_exec();
                        final Process proc = Runtime.getRuntime().exec(convertCmd, envp, (File)f);
                        Thread.sleep(1000L);
                        Worker.startWorker(new discarder(proc.getErrorStream()), "Preview:ErrorStream:" + command_line_result);
                        Worker.startWorker(new discarder(proc.getInputStream()), "Preview:InputStream:" + command_line_result);
                        final Properties exitInfo = new Properties();
                        Worker.startWorker(new Runnable(){

                            /*
                             * WARNING - Removed try catching itself - possible behaviour change.
                             */
                            @Override
                            public void run() {
                                exitInfo.put("thread", Thread.currentThread());
                                try {
                                    exitInfo.put("exitCode", String.valueOf(proc.waitFor()));
                                }
                                catch (Exception e) {
                                    exitInfo.put("exitCode", "50");
                                    proc.destroy();
                                }
                                try {
                                    Properties properties = exitInfo;
                                    synchronized (properties) {
                                        exitInfo.remove("thread");
                                    }
                                    Thread.sleep(1000L);
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                                try {
                                    proc.getErrorStream().close();
                                    proc.getInputStream().close();
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                        });
                        long start = System.currentTimeMillis();
                        if (this.prefs.getProperty("preview_wait_timeout", "600").equals("")) {
                            this.prefs.put("preview_wait_timeout", "121");
                        }
                        int timeout = Integer.parseInt(this.prefs.getProperty("preview_wait_timeout", "600"));
                        while (exitInfo.getProperty("exitCode", "").equals("")) {
                            Thread.sleep(100L);
                            Properties properties = exitInfo;
                            synchronized (properties) {
                                if (System.currentTimeMillis() - start >= (long)(timeout * 1000) || !this.prefs.getProperty("preview_enabled").equalsIgnoreCase("true") || this.abort) {
                                    Thread tt = (Thread)exitInfo.get("thread");
                                    if (tt != null) {
                                        tt.interrupt();
                                    }
                                    break;
                                }
                            }
                        }
                        if ((vrl.getPath().toUpperCase().endsWith(".PSD") || vrl.getPath().toUpperCase().endsWith(".PDF")) && !new File_U(String.valueOf(destFile) + "/p1/3.jpg").exists()) {
                            int x2 = 0;
                            while (x2 < 500) {
                                if (!new File_U(String.valueOf(destFile) + "/p1/3-" + x2 + ".jpg").exists()) break;
                                new File_U(String.valueOf(destFile) + "/p" + (x2 + 1) + "/").mkdirs();
                                new File_U(String.valueOf(destFile) + "/p1/3-" + x2 + ".jpg").renameTo(new File_U(String.valueOf(destFile) + "/p" + (x2 + 1) + "/3.jpg"));
                                ++x2;
                            }
                        }
                        if (!exitInfo.getProperty("exitCode", "").equals("0") || !new File_U(String.valueOf(destFile) + "/" + page + "/" + (xx + 1) + ".jpg").exists()) {
                            this.badFiles.put(stat.getProperty("url"), stat.getProperty("modified"));
                            this.msg("Preview cannot be generated, adding to list of bad files:" + vrl.getPath());
                            break;
                        }
                        new File_U(String.valueOf(destFile) + "/" + page + "/" + (xx + 1) + ".jpg").setLastModified(Long.parseLong(stat.getProperty("modified")));
                    }
                    catch (Exception e) {
                        this.msg(e);
                    }
                    if (loop > 1) break;
                    ++page_loop;
                }
                --xx;
            }
            ++loop;
        }
        new File_U(destFile).setLastModified(Long.parseLong(stat.getProperty("modified")));
        if (temp) {
            return new File_U(srcFile2).getCanonicalPath();
        }
        return null;
    }

    public float getDuration(final BufferedReader br) throws IOException {
        final Properties status = new Properties();
        Runnable r = new Runnable(){

            @Override
            public void run() {
                try {
                    float duration = -1.0f;
                    String data = "";
                    int lines = 0;
                    while ((data = br.readLine()) != null) {
                        if (++lines <= 3) {
                            try {
                                duration = Float.parseFloat(data.trim());
                                status.put("duration", String.valueOf(duration));
                                break;
                            }
                            catch (NumberFormatException numberFormatException) {
                                // empty catch block
                            }
                        }
                        data = data.toUpperCase();
                        PreviewWorker.this.msg(data);
                        if (data.indexOf("DURATION") < 0) continue;
                        data = data.substring(data.indexOf(":") + 1);
                        String num = "";
                        boolean inNum = false;
                        int c = 0;
                        while (c < data.length()) {
                            if (data.charAt(c) >= '0' && data.charAt(c) <= '9' || data.charAt(c) == '.' || data.charAt(c) == ':') {
                                num = String.valueOf(num) + data.charAt(c);
                                inNum = true;
                            } else if (inNum) break;
                            ++c;
                        }
                        String[] date = num.split(":");
                        duration = Float.parseFloat(date[0]) * 3600.0f + Float.parseFloat(date[1]) * 60.0f + Float.parseFloat(date[2]) * 1.0f;
                        if (duration > 2.0f) {
                            duration -= 1.0f;
                        }
                        status.put("duration", String.valueOf(duration));
                    }
                }
                catch (Exception e) {
                    PreviewWorker.this.msg(e);
                }
            }
        };
        Thread t = new Thread(r);
        t.setName(String.valueOf(Thread.currentThread().getName()) + ":Getting movie duration.");
        t.start();
        try {
            t.join(30000L);
        }
        catch (Exception e) {
            this.msg(e);
        }
        t.interrupt();
        return Float.parseFloat(status.getProperty("duration", "-1.0"));
    }

    public String change_vars(String s, String src, String dest, float time, int intervals) {
        ++intervals;
        s = Common.replace_str(s, "%src%", src);
        s = Common.replace_str(s, "%dest%", dest);
        s = Common.replace_str(s, "%dst%", dest);
        s = Common.replace_str(s, "%time%", String.valueOf((int)time));
        s = Common.replace_str(s, "%random%", Common.makeBoundary(4));
        try {
            s = Common.replace_str(s, "%previews%", String.valueOf(new File_U(String.valueOf(ServerStatus.SG("previews_path")) + "/Preview").getCanonicalPath()) + "/");
        }
        catch (IOException e) {
            this.msg(e);
        }
        if (intervals < 3) {
            s = this.change_vars(s, src, dest, time, intervals);
        }
        return s;
    }

    public void recurseConvert(GenericClient c, VRL vrl, int depth, int max_depth) throws Exception {
        if (depth > max_depth) {
            return;
        }
        c.setConfig("no_stat", "true");
        Properties stat = c.stat(vrl.getPath());
        if (stat == null) {
            return;
        }
        if (stat.getProperty("type").equalsIgnoreCase("dir")) {
            Vector list = new Vector();
            c.list(vrl.getPath(), list);
            int x = 0;
            while (x < list.size()) {
                Properties p = (Properties)list.elementAt(x);
                if (p.getProperty("type").equalsIgnoreCase("dir")) {
                    this.recurseConvert(c, new VRL(p.getProperty("url")), depth + 1, max_depth);
                } else {
                    this.doConvert(c, p, null, true, new Properties(), false);
                }
                if (!this.prefs.getProperty("preview_enabled").equalsIgnoreCase("true") || this.abort) {
                    return;
                }
                ++x;
            }
        } else {
            if (!this.prefs.getProperty("preview_enabled").equalsIgnoreCase("true") || this.abort) {
                return;
            }
            this.doConvert(c, stat, null, true, new Properties(), false);
        }
    }

    public void reverseRecurseConvert(String real_path, int depth, int max_depth) {
        if (depth > max_depth) {
            return;
        }
        File_U f = new File_U(real_path);
        try {
            real_path = String.valueOf(f.getCanonicalPath()) + "/";
            f = new File_U(real_path);
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (f.isDirectory()) {
            String[] files = f.list();
            int x = 0;
            while (files != null && x < files.length) {
                if (!this.prefs.getProperty("preview_enabled").equalsIgnoreCase("true") || this.abort) {
                    return;
                }
                File_U f2 = new File_U(String.valueOf(real_path) + files[x]);
                if (!Common.isSymbolicLink_U(f2.getAbsolutePath()) && f2.isDirectory()) {
                    if (new File_U(String.valueOf(real_path) + files[x] + "/index.txt").exists() || new File_U(String.valueOf(real_path) + files[x] + "/p1").exists()) {
                        File_U home = new File_U(String.valueOf(ServerStatus.SG("previews_path")) + "/Preview");
                        boolean ok = true;
                        try {
                            if (f2.getCanonicalPath().startsWith(home.getCanonicalPath())) {
                                String checkPath = f2.getCanonicalPath().substring(home.getCanonicalPath().length()).replace('\\', '/');
                                if (Common.machine_is_windows()) {
                                    String driveLetter = checkPath.split("/")[1];
                                    if (driveLetter.startsWith("_-_") && driveLetter.endsWith("_-_")) {
                                        driveLetter = String.valueOf(driveLetter.charAt(3));
                                        checkPath = String.valueOf(driveLetter) + ":" + checkPath.substring(checkPath.indexOf("/", 6));
                                        this.msg("Checking if file exists:" + checkPath);
                                    } else if (driveLetter.startsWith("_UNC_")) {
                                        driveLetter = "//";
                                        checkPath = String.valueOf(driveLetter) + checkPath.substring(6);
                                        this.msg("Checking if file exists:" + checkPath);
                                    }
                                }
                                if (!new File_U(checkPath).exists()) {
                                    ok = false;
                                }
                            }
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        if (!ok) {
                            if (!this.prefs.getProperty("preview_enabled").equalsIgnoreCase("true") || this.abort) {
                                return;
                            }
                            this.msg("Deleting old thubmnail for deleted file:" + real_path + files[x] + "/");
                            Common.recurseDelete_U(String.valueOf(real_path) + files[x] + "/", false);
                        }
                    } else {
                        this.reverseRecurseConvert(String.valueOf(real_path) + files[x] + "/", depth + 1, max_depth);
                    }
                }
                ++x;
            }
        }
    }

    public Properties getExifInfo(String srcFile, String destFile) throws Exception {
        Properties metaInfo = new Properties();
        if (this.get("preview_exif_get_command_line") != null && !this.get("preview_exif_get_command_line").trim().equals("")) {
            if (Common.machine_is_windows()) {
                srcFile = new File_U(srcFile).getCanonicalPath();
            }
            this.msg("Making " + srcFile + " info.xml exif file...");
            BufferedReader br = null;
            Process proc = null;
            try {
                String[] command = null;
                String[] envp = this.get("preview_environment").split(";");
                command = Common.machine_is_windows() ? ("CMD /C " + this.get("preview_exif_get_command_line")).split(" ") : this.get("preview_exif_get_command_line").split(" ");
                String command_str = "";
                int x = 0;
                while (x < command.length) {
                    if (command[x].equalsIgnoreCase("%SRC%")) {
                        command[x] = srcFile;
                    }
                    command_str = String.valueOf(command_str) + command[x] + " ";
                    ++x;
                }
                if (envp.length == 0 || envp.length == 1 && envp[0].trim().length() == 0) {
                    envp = null;
                }
                this.msg("Exif:" + command_str);
                File_S f = null;
                f = this.get("preview_working_dir").equals("") ? new File_S(System.getProperty("crushftp.home")) : new File_S(this.get("preview_working_dir"));
                com.crushftp.client.Common.check_exec();
                proc = Runtime.getRuntime().exec(command, envp, (File)f);
                br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                Worker.startWorker(new discarder(proc.getErrorStream()));
                String data = "";
                int lines = 0;
                while ((data = br.readLine()) != null) {
                    ++lines;
                    if (data.indexOf(": ") < 0) continue;
                    String key = data.substring(0, data.indexOf(":")).trim().toLowerCase();
                    if ((key = key.replaceAll(" ", "_")).startsWith("0x") || key.indexOf("[") >= 0 || key.indexOf("]") >= 0) continue;
                    try {
                        Integer.parseInt(key);
                    }
                    catch (Exception e) {
                        metaInfo.put(key, data.substring(data.indexOf(":") + 1).trim());
                        this.msg(data);
                    }
                }
                br.close();
                proc.waitFor();
            }
            catch (Exception e) {
                this.msg(e);
            }
        }
        if (metaInfo.size() > 0) {
            if (new File_U(String.valueOf(destFile) + "info.xml").exists()) {
                Properties metaInfo_old = (Properties)Common.readXMLObject_U(new File_U(String.valueOf(destFile) + "info.xml"));
                Enumeration<Object> keys = metaInfo_old.keys();
                while (keys.hasMoreElements()) {
                    String key = "" + keys.nextElement();
                    if (!key.startsWith(String.valueOf(System.getProperty("appname", "CrushFTP").toLowerCase()) + "_") && !key.equalsIgnoreCase("keywords")) continue;
                    metaInfo.put(key, metaInfo_old.getProperty(key));
                }
            }
            long destFile_mdtm = new File_U(destFile).lastModified();
            Common.writeXMLObject_U(String.valueOf(destFile) + "info.xml", metaInfo, "EXIF");
            new File_U(destFile).setLastModified(destFile_mdtm);
        }
        return metaInfo;
    }

    public void validateBytes(File_U f) throws Exception {
        String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1).toLowerCase().trim();
        if ("mvg,msl,svg,".indexOf(String.valueOf(ext) + ",") >= 0) {
            throw new Exception(f + " failed byte validation security check due to unsafe file type!");
        }
        int found = 0;
        int invalid = 0;
        String failures = "";
        int x = 0;
        while (x < 10) {
            if (byte_validation.containsKey(String.valueOf(ext) + x)) {
                ++found;
                byte[] b1 = (byte[])byte_validation.get(String.valueOf(ext) + x);
                byte[] b2 = new byte[b1.length];
                RandomAccessFile raf = new RandomAccessFile(f, "r");
                try {
                    raf.readFully(b2);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                raf.close();
                int xx = 0;
                while (xx < b1.length) {
                    if (b1[xx] != b2[xx]) {
                        failures = String.valueOf(failures) + f + " failed byte validation security check before Preview generation! " + PreviewWorker.getHex(b1) + " vs. " + PreviewWorker.getHex(b2) + "\r\n";
                        ++invalid;
                        break;
                    }
                    ++xx;
                }
            }
            ++x;
        }
        if (found == invalid && found > 0) {
            this.msg(failures.trim());
            throw new Exception(f + " failed byte validation security check before Preview generation!");
        }
    }

    static String getHex(byte[] raw) {
        StringBuffer sb = new StringBuffer();
        int x = 0;
        while (x < raw.length) {
            sb.append("0123456789ABCDEF".charAt((raw[x] & 0xF0) >> 4));
            sb.append("0123456789ABCDEF".charAt(raw[x] & 0xF));
            ++x;
        }
        return sb.toString();
    }

    public static Properties getMetaInfo(String destFile) throws Exception {
        Properties metaInfo = new Properties();
        if (new File_U(String.valueOf(destFile) + "info.xml").exists()) {
            metaInfo = (Properties)Common.readXMLObject_U(new File_U(String.valueOf(destFile) + "info.xml"));
        }
        if (metaInfo == null) {
            metaInfo = new Properties();
        }
        return metaInfo;
    }

    public static Properties setMetaInfo(String destFile, Properties metaInfo) throws Exception {
        new File_U(destFile).mkdirs();
        Common.writeXMLObject(String.valueOf(destFile) + "info.xml", (Object)metaInfo, "EXIF");
        return metaInfo;
    }

    public Properties setExifInfo(String srcFile, String destFile, String exif_key, String exif_val) {
        Properties metaInfo = null;
        if (this.get("preview_exif_set_command_line") != null && !this.get("preview_exif_set_command_line").trim().equals("") && !exif_key.startsWith(String.valueOf(System.getProperty("appname", "CrushFTP").toLowerCase()) + "_")) {
            try {
                metaInfo = PreviewWorker.getMetaInfo(destFile);
                if (Common.machine_is_windows()) {
                    srcFile = new File_U(srcFile).getCanonicalPath();
                }
                if (Common.machine_is_windows()) {
                    destFile = new File_U(destFile).getCanonicalPath();
                }
            }
            catch (Exception e1) {
                this.msg(e1);
            }
            this.msg("Updating " + srcFile + " with exif key change.");
            BufferedReader br = null;
            Process proc = null;
            String keywords = null;
            int xx = 0;
            while (xx < exif_val.split(",").length) {
                try {
                    srcFile = new File_U(srcFile).getCanonicalPath();
                    String[] command = null;
                    String[] envp = this.get("preview_environment").split(";");
                    command = Common.machine_is_windows() ? ("CMD /C " + this.get("preview_exif_set_command_line")).split(" ") : this.get("preview_exif_set_command_line").split(" ");
                    int x = 0;
                    while (x < command.length) {
                        if (command[x].equalsIgnoreCase("%SRC%")) {
                            command[x] = srcFile;
                        }
                        if (xx == 0 && command[x].toUpperCase().indexOf("%KEY%") >= 0) {
                            command[x] = String.valueOf(command[x].substring(0, command[x].toUpperCase().indexOf("%KEY%"))) + exif_key + command[x].substring(command[x].toUpperCase().indexOf("%KEY%") + "%KEY%".length());
                        }
                        if (xx > 0 && command[x].toUpperCase().indexOf("%KEY%") >= 0) {
                            command[x] = String.valueOf(command[x].substring(0, command[x].toUpperCase().indexOf("%KEY%"))) + exif_key + "+" + command[x].substring(command[x].toUpperCase().indexOf("%KEY%") + "%KEY%".length());
                        }
                        if (command[x].toUpperCase().indexOf("%VAL%") >= 0) {
                            command[x] = String.valueOf(command[x].substring(0, command[x].toUpperCase().indexOf("%VAL%"))) + exif_val.split(",")[xx].trim() + command[x].substring(command[x].toUpperCase().indexOf("%VAL%") + "%VAL%".length());
                        }
                        ++x;
                    }
                    if (envp.length == 0 || envp.length == 1 && envp[0].trim().length() == 0) {
                        envp = null;
                    }
                    File_S f = null;
                    f = this.get("preview_working_dir").equals("") ? new File_S(System.getProperty("crushftp.home")) : new File_S(this.get("preview_working_dir"));
                    com.crushftp.client.Common.check_exec();
                    proc = Runtime.getRuntime().exec(command, envp, (File)f);
                    br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    Worker.startWorker(new discarder(proc.getErrorStream()));
                    String data = "";
                    while ((data = br.readLine()) != null) {
                        this.msg(data);
                    }
                    br.close();
                    proc.waitFor();
                    if (exif_key.equalsIgnoreCase("keywords")) {
                        keywords = exif_val;
                    }
                }
                catch (Exception e) {
                    this.msg(e);
                }
                ++xx;
            }
            try {
                metaInfo = this.getExifInfo(srcFile, destFile);
                if (keywords != null) {
                    metaInfo.put("keywords", keywords);
                }
                PreviewWorker.setMetaInfo(destFile, metaInfo);
            }
            catch (Exception e) {
                this.msg(e);
            }
        }
        return metaInfo;
    }

    class discarder
    implements Runnable {
        InputStream in = null;

        public discarder(InputStream in) {
            this.in = in;
        }

        @Override
        public void run() {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(this.in));
                String data = "";
                while ((data = br.readLine()) != null) {
                    PreviewWorker.this.msg(data);
                }
            }
            catch (Exception e) {
                PreviewWorker.this.msg(e);
            }
            try {
                br.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }
}

