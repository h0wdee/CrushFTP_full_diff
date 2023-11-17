/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.CommandBufferFlusher;
import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import com.crushftp.client.File_U;
import com.crushftp.client.GenericClient;
import com.crushftp.client.RandomOutputStream;
import com.crushftp.client.VRL;
import com.crushftp.client.ZipClient;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

public class FileClient
extends GenericClient {
    public static Properties dirCachePerm = new Properties();
    public static Properties dirCachePermTemp = new Properties();
    public static Object dirCachePermTemp_lock = new Object();
    public static boolean memCache = System.getProperty("crushftp.memcache", "false").equals("true");
    int lsBytesRead = 0;
    Properties dirCache = new Properties();

    public FileClient(String url, String header, Vector log) {
        super(header, log);
        this.fields = new String[]{"zip_list", "count_dir_items", "count_dir_size", "checkEncryptedHeader", "lsla2", "file_recurse_delete"};
        this.url = new VRL(url).getPath();
    }

    @Override
    public void logout() throws Exception {
        this.close();
        this.dirCache = new Properties();
        this.logQueue = new Vector();
    }

    public void freeCache() {
        this.dirCache = new Properties();
        this.logQueue = new Vector();
    }

    private String getAbsolutePath(File_U f) {
        return Common.machine_is_windows() ? "/" + f.getAbsolutePath().replace('\\', '/') : f.getAbsolutePath();
    }

    @Override
    public Properties stat(String path) throws Exception {
        String temp_url;
        path = String.valueOf(this.url) + path.substring(1);
        File_U test = new File_U(path);
        if (this.getAbsolutePath(test).toLowerCase().indexOf(".zip/") >= 0 && System.getProperty("crushftp.zipstat", "true").equals("true") && this.config.getProperty("zip_list", "true").equals("true")) {
            int pos = path.toLowerCase().indexOf(".zip/");
            ZipClient zc = new ZipClient(path.substring(0, pos + 4), this.logHeader, this.logQueue);
            Properties zi = zc.stat("!" + this.getAbsolutePath(test).substring(pos + 5));
            test = new File_U(path.substring(0, pos + 4));
            if (zi != null) {
                return zi;
            }
        }
        if (!test.exists() && System.getProperty("crushftp.isTestCall" + Thread.currentThread().getId(), "false").equals("true")) {
            throw new Exception("Item not found...");
        }
        if (!test.exists()) {
            return null;
        }
        Properties allitems = new Properties();
        if (test.getParentFile() != null) {
            this.getOSXListing(this.getAbsolutePath((File_U)test.getParentFile()), allitems, true);
        }
        Properties dir_item = new Properties();
        String name = test.getName();
        if (name.equals("") && (Common.machine_is_x() || Common.machine_is_unix() || Common.machine_is_linux())) {
            name = "localhost";
        } else if (name.equals("") && Common.machine_is_windows()) {
            name = test.getPath().substring(0, 2);
        }
        dir_item.put("name", name.replaceAll("\r", "%0A").replaceAll("\n", "%0D"));
        dir_item.put("size", "0");
        dir_item.put("type", "FILE");
        if (test.isDirectory()) {
            dir_item.put("type", "DIR");
            dir_item.put("permissions", "drwxrwxrwx");
            dir_item.put("size", "1");
            if (this.config.getProperty("count_dir_items", "false").equals("true")) {
                int i = 0;
                File_U[] list = (File_U[])test.listFiles();
                int x = 0;
                while (list != null && x < list.length) {
                    if (!list[x].getName().startsWith(".")) {
                        ++i;
                    }
                    ++x;
                }
                dir_item.put("size", String.valueOf(i));
            }
            if (this.config.getProperty("count_dir_size", "false").equals("true")) {
                long i = 0L;
                File_U[] list = (File_U[])test.listFiles();
                int x = 0;
                while (list != null && x < list.length) {
                    if (list[x].isFile()) {
                        i += list[x].length();
                    }
                    ++x;
                }
                dir_item.put("size", String.valueOf(i));
            }
            if (!path.endsWith("/")) {
                path = String.valueOf(path) + "/";
            }
        } else if (test.isFile()) {
            dir_item.put("permissions", "-rwxrwxrwx");
            dir_item.put("size", String.valueOf(this.getSize(test)));
        }
        if ((temp_url = Common.url_decode(test.toURI().toURL().toExternalForm()).replaceAll("\r", "%0A").replaceAll("\n", "%0D")).toLowerCase().startsWith("file:/") && !temp_url.toLowerCase().startsWith("file://")) {
            temp_url = Common.replace_str(temp_url, "file:/", "file://");
        }
        dir_item.put("url", temp_url);
        dir_item.put("link", "false");
        if (System.getProperty("crushftp.allow_symlink_checking", "true").equals("true") && Common.isSymbolicLink(this.getAbsolutePath(test))) {
            dir_item.put("link", "true");
        }
        if (System.getProperty("crushftp.block_symlinks", "false").equals("true") && dir_item.getProperty("link", "").equals("true")) {
            return null;
        }
        dir_item.put("num_items", "1");
        dir_item.put("owner", "user");
        dir_item.put("group", "group");
        dir_item.put("protocol", "file");
        dir_item.put("root_dir", Common.all_but_last(path));
        this.setFileDateInfo(test, dir_item);
        if (allitems.get(test.getName()) != null) {
            this.setOSXInfo((Properties)allitems.get(test.getName()), dir_item);
        }
        if (memCache) {
            try {
                Properties dir_item2 = this.remove_uneeded_memcache_items((Properties)dir_item.clone());
                dirCachePerm.put(test.getCanonicalPath().replace('\\', '/'), dir_item2);
                if (dirCachePermTemp != null) {
                    dirCachePermTemp.put(test.getCanonicalPath().replace('\\', '/'), dir_item2);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return dir_item;
    }

    private Properties remove_uneeded_memcache_items(Properties dir_item2) {
        dir_item2.remove("owner");
        dir_item2.remove("root_dir");
        dir_item2.remove("link");
        dir_item2.remove("protocol");
        dir_item2.remove("month");
        dir_item2.remove("num_items");
        dir_item2.remove("permissions");
        dir_item2.remove("time_or_year");
        dir_item2.remove("day");
        dir_item2.remove("group");
        return dir_item2;
    }

    private long getSize(File_U test) {
        if (this.config.getProperty("checkEncryptedHeader", "false").equals("true")) {
            return Common.getFileSize(test.getPath());
        }
        return test.length();
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        if (!path.replace('\\', '/').endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        Common.log("FILE_CLIENT", 2, "LIST:" + path);
        String originalPath = path;
        path = String.valueOf(this.url) + path.substring(1);
        File_U item = new File_U(path);
        if (!item.exists() && System.getProperty("crushftp.isTestCall" + Thread.currentThread().getId(), "false").equals("true")) {
            throw new Exception("Item not found...");
        }
        if (!this.getAbsolutePath(item).toLowerCase().endsWith(".zip") && this.getAbsolutePath(item).toLowerCase().indexOf(".zip/") < 0 && !item.exists() && System.getProperty("crushftp.file_client_not_found_error", "true").equals("true")) {
            throw new FileNotFoundException("No such item:" + item);
        }
        File_U[] items = (File_U[])item.listFiles();
        if (path.equals("/") && Common.machine_is_windows()) {
            items = (File_U[])File_U.listRoots();
        }
        Properties allitems = new Properties();
        this.getOSXInfo(allitems, this.getAbsolutePath(item), true);
        if (this.config.getProperty("zip_list", "true").equals("true") && (this.getAbsolutePath(item).toLowerCase().endsWith(".zip") || this.getAbsolutePath(item).toLowerCase().indexOf(".zip/") >= 0) && path.endsWith("/") && item.isFile()) {
            int pos = this.getAbsolutePath(item).toLowerCase().indexOf(".zip");
            ZipClient zc = new ZipClient(this.getAbsolutePath(item).substring(0, pos + 4), this.logHeader, this.logQueue);
            String addOn = "";
            if (this.getAbsolutePath(item).toLowerCase().indexOf(".zip/") >= 0) {
                ++pos;
                addOn = "/";
            }
            zc.list("!" + this.getAbsolutePath(item).substring(pos + 4) + addOn, list);
            items = null;
        } else if (item.isFile()) {
            items = new File_U[]{item};
            items = new File_U[]{item};
            try {
                originalPath = originalPath.endsWith("/") ? Common.all_but_last(originalPath.substring(0, originalPath.length() - 1)) : Common.all_but_last(originalPath);
            }
            catch (Exception e) {
                Common.log("FILE_CLIENT", 2, e);
            }
        }
        if (items == null) {
            items = new File_U[]{};
        }
        this.dirCache.clear();
        Vector<String> permCacheList = new Vector<String>();
        int x = 0;
        while (x < items.length) {
            String itemName;
            String tempPath;
            Properties dir_item;
            File_U test = items[x];
            String tempName = test.getName();
            if (Common.machine_is_windows() && path.equals("/")) {
                tempName = items[x].getPath().substring(0, 2);
            }
            if ((dir_item = this.stat(tempPath = String.valueOf(originalPath) + tempName)) == null) {
                Common.log("FILE_CLIENT", 1, "Linux?  Bad LANG setting in " + System.getProperty("appname", "CrushFTP").toLowerCase() + "_init.sh?  Couldn't find item:" + tempPath);
            }
            if (allitems.get(itemName = Common.normalize2(tempName)) != null) {
                this.setOSXInfo((Properties)allitems.get(itemName), dir_item);
            }
            long size = 0L;
            try {
                if (dir_item != null) {
                    size = Long.parseLong(dir_item.getProperty("size", "0"));
                    if (dir_item.getProperty("type").equalsIgnoreCase("FILE")) {
                        size = this.getSize(test);
                    }
                }
            }
            catch (Exception e) {
                Common.log("FILE_CLIENT", 1, "Invalid file, or dead alias:" + test);
                Common.log("FILE_CLIENT", 1, e);
            }
            if (dir_item != null) {
                dir_item.put("size", String.valueOf(size));
                list.add(dir_item);
                if (memCache) {
                    try {
                        permCacheList.addElement(test.getCanonicalPath().replace('\\', '/'));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
            ++x;
        }
        if (memCache) {
            try {
                dirCachePerm.put(String.valueOf(item.getCanonicalPath().replace('\\', '/')) + "/", permCacheList);
                if (dirCachePermTemp != null) {
                    dirCachePermTemp.put(String.valueOf(item.getCanonicalPath().replace('\\', '/')) + "/", permCacheList);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        this.dirCache.clear();
        return list;
    }

    private void getOSXInfo(Properties allitems, String realPath, boolean useOSXInfo) {
        File_U item = new File_U(realPath);
        this.getOSXListing(this.getAbsolutePath(item), allitems, useOSXInfo);
        if ((Common.machine_is_x() || Common.machine_is_linux() || Common.machine_is_unix()) && item.isFile()) {
            if (allitems.get(this.getAbsolutePath(item)) != null) {
                allitems.put(item.getName(), allitems.get(this.getAbsolutePath(item)));
            }
            allitems.remove(this.getAbsolutePath(item));
        }
    }

    private void setOSXInfo(Properties p, Properties dir_item) {
        try {
            dir_item.put("owner", p.getProperty("owner"));
            dir_item.put("group", p.getProperty("group"));
            if (dir_item.getProperty("type", "").equals("DIR")) {
                dir_item.put("permissions", "d" + p.getProperty("permissions").substring(1));
            } else if (dir_item.getProperty("type", "").equals("FILE")) {
                dir_item.put("permissions", "-" + p.getProperty("permissions").substring(1));
            }
            dir_item.put("month", p.getProperty("month"));
            dir_item.put("day", p.getProperty("day"));
            dir_item.put("time_or_year", p.getProperty("time_or_year"));
            dir_item.put("num_items", p.getProperty("num_items"));
            dir_item.put("link", p.getProperty("link", "false"));
            dir_item.put("linkedFile", p.getProperty("linkedFile", ""));
            String perm = String.valueOf(dir_item.getProperty("permissions", "-------------------")) + "-----------------------";
            String userP = this.changePermissions(perm.substring(1, 4));
            String userG = this.changePermissions(perm.substring(4, 7));
            String userW = this.changePermissions(perm.substring(7, 10));
            dir_item.put("permissionsNum", String.valueOf(userP) + userG + userW);
        }
        catch (Exception e) {
            Common.log("FILE_CLIENT", 1, e);
        }
    }

    private String changePermissions(String s) {
        if (s.equalsIgnoreCase("--x")) {
            return "1";
        }
        if (s.equalsIgnoreCase("-w-")) {
            return "2";
        }
        if (s.equalsIgnoreCase("-wx")) {
            return "3";
        }
        if (s.equalsIgnoreCase("r--")) {
            return "4";
        }
        if (s.equalsIgnoreCase("r-x")) {
            return "5";
        }
        if (s.equalsIgnoreCase("rw-")) {
            return "6";
        }
        if (s.equalsIgnoreCase("rwx")) {
            return "7";
        }
        return "0";
    }

    private void setFileDateInfo(File_U test, Properties dir_item) {
        Date itemDate = new Date(test.lastModified());
        dir_item.put("created", "0");
        try {
            FileTime creationTime = (FileTime)Files.getAttribute(Paths.get(test.getPath(), new String[0]), "creationTime", new LinkOption[0]);
            dir_item.put("created", String.valueOf(creationTime.toMillis()));
        }
        catch (IOException creationTime) {
            // empty catch block
        }
        dir_item.put("modified", String.valueOf(itemDate.getTime()));
        dir_item.put("month", months[Integer.parseInt(this.mm.format(itemDate))]);
        dir_item.put("day", this.dd.format(itemDate));
        String time_or_year = this.hhmm.format(itemDate);
        if (!this.yyyy.format(itemDate).equals(this.yyyy.format(new Date())) || System.getProperty("crushftp.ls.year", "false").equals("true")) {
            time_or_year = this.yyyy.format(itemDate);
        }
        dir_item.put("time_or_year", time_or_year);
    }

    private void getOSXListing(String realPath, Properties all_dir_items, boolean useOSXInfo) {
        if (!useOSXInfo) {
            return;
        }
        boolean lsla = System.getProperty("crushftp.lsla", "").equalsIgnoreCase("true");
        if (this.config.containsKey("lsla2")) {
            lsla = this.config.getProperty("lsla2", "false").equalsIgnoreCase("true");
        }
        if (!lsla) {
            return;
        }
        if (this.dirCache.containsKey(realPath)) {
            Properties oldCache = (Properties)this.dirCache.get(realPath);
            all_dir_items.putAll((Map<?, ?>)oldCache);
            return;
        }
        if (Common.machine_is_x() || Common.machine_is_linux() || Common.machine_is_unix()) {
            BufferedReader ls_in = null;
            Thread monitorThread = null;
            try {
                String data = "";
                this.lsBytesRead = 0;
                Common.log("FILE_CLIENT", 3, "ls -la " + realPath);
                Process ls_proc = Runtime.getRuntime().exec(new String[]{System.getProperty("crushftp.ls", "ls"), System.getProperty("crushftp.la", "-la"), realPath}, new String[]{System.getProperty("crushftp.lang", "LANG=C")});
                class Monitor
                implements Runnable {
                    Process ls_proc;
                    int lsLastBytesRead = 0;

                    public Monitor(Process ls_proc) {
                        this.ls_proc = ls_proc;
                    }

                    @Override
                    public void run() {
                        try {
                            while (FileClient.this.lsBytesRead >= 0) {
                                Thread.sleep(3000L);
                                if (FileClient.this.lsBytesRead == this.lsLastBytesRead) {
                                    this.ls_proc.destroy();
                                    break;
                                }
                                this.lsLastBytesRead = FileClient.this.lsBytesRead;
                            }
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                }
                monitorThread = new Thread(new Monitor(ls_proc));
                monitorThread.setName("ls -la " + realPath);
                monitorThread.start();
                ls_in = new BufferedReader(new InputStreamReader(ls_proc.getInputStream(), "UTF8"));
                while ((data = ls_in.readLine()) != null) {
                    Common.log("FILE_CLIENT", 5, data);
                    this.lsBytesRead += data.length();
                    try {
                        Properties dir_item = new Properties();
                        if (data.toUpperCase().startsWith("TOTAL ")) continue;
                        if (data.toUpperCase().startsWith("D") || data.toUpperCase().startsWith("L")) {
                            dir_item.put("type", "DIR");
                        } else {
                            dir_item.put("type", "FILE");
                        }
                        StringTokenizer get_em = new StringTokenizer(data, " ");
                        String permissions = String.valueOf(get_em.nextToken().trim()) + "-----------------";
                        permissions = permissions.substring(0, 10);
                        dir_item.put("permissions", permissions);
                        dir_item.put("num_items", get_em.nextToken().trim());
                        if (dir_item.getProperty("num_items", "0").length() > 3) {
                            dir_item.put("num_items", "999");
                        }
                        dir_item.put("owner", (String.valueOf(get_em.nextToken().trim()) + "         ").substring(0, 8));
                        dir_item.put("group", (String.valueOf(get_em.nextToken().trim()) + "         ").substring(0, 8));
                        dir_item.put("size", get_em.nextToken().trim());
                        dir_item.put("month", get_em.nextToken().trim());
                        if (dir_item.getProperty("month").charAt(0) != String.valueOf(dir_item.getProperty("month").charAt(0)).toUpperCase().charAt(0)) {
                            dir_item.put("month", String.valueOf(String.valueOf(dir_item.getProperty("month").charAt(0)).toUpperCase()) + dir_item.getProperty("month").substring(1));
                        }
                        if (dir_item.getProperty("month").indexOf("-") > 0) {
                            dir_item.put("time_or_year", get_em.nextToken().trim());
                            Date itemDate = this.yyyymmddHHmm.parse(String.valueOf(dir_item.getProperty("month")) + " " + dir_item.getProperty("time_or_year"));
                            dir_item.put("month", months[Integer.parseInt(this.mm.format(itemDate))]);
                            dir_item.put("day", this.dd.format(itemDate));
                            String time_or_year = this.hhmm.format(itemDate);
                            if (!this.yyyy.format(itemDate).equals(this.yyyy.format(new Date()))) {
                                time_or_year = this.yyyy.format(itemDate);
                            }
                            dir_item.put("time_or_year", time_or_year);
                        } else if (dir_item.getProperty("month").trim().length() < 3) {
                            String realDay = dir_item.getProperty("month", "");
                            dir_item.put("month", get_em.nextToken().trim());
                            dir_item.put("day", realDay);
                            dir_item.put("time_or_year", get_em.nextToken().trim());
                        } else {
                            dir_item.put("day", get_em.nextToken().trim());
                            dir_item.put("time_or_year", get_em.nextToken().trim());
                        }
                        String name_data = get_em.nextToken();
                        String searchName = String.valueOf(dir_item.getProperty("time_or_year")) + " " + name_data;
                        name_data = data.substring(data.indexOf(name_data, data.indexOf(searchName) + dir_item.getProperty("time_or_year").length() + 1));
                        if (name_data.equals("")) continue;
                        name_data = name_data.replaceAll("\r", "%0A").replaceAll("\n", "%0D");
                        dir_item.put("name", name_data);
                        if (data.toUpperCase().startsWith("L")) {
                            dir_item.put("name", name_data.substring(0, name_data.indexOf(" ->")));
                            dir_item.put("linkedFile", name_data.substring(name_data.indexOf(" ->") + 3).trim());
                            if (!dir_item.getProperty("linkedFile").endsWith("/")) {
                                dir_item.put("type", "FILE");
                            }
                        }
                        if (name_data.startsWith("Icon")) continue;
                        all_dir_items.put(Common.normalize2(name_data), dir_item);
                    }
                    catch (Exception e) {
                        if (ls_proc != null) {
                            ls_proc.destroy();
                        }
                        Common.log("FILE_CLIENT", 1, e);
                    }
                }
                try {
                    if (ls_proc != null) {
                        ls_proc.destroy();
                    }
                }
                catch (Exception e) {
                    Common.log("FILE_CLIENT", 5, e);
                }
            }
            catch (Exception e) {
                Common.log("FILE_CLIENT", 1, e);
            }
            try {
                if (ls_in != null) {
                    ls_in.close();
                }
            }
            catch (Exception e) {
                Common.log("FILE_CLIENT", 5, e);
            }
            try {
                if (monitorThread != null) {
                    monitorThread.interrupt();
                }
            }
            catch (Exception e) {
                Common.log("FILE_CLIENT", 5, e);
            }
            this.lsBytesRead = -1;
        }
        this.dirCache.put(realPath, all_dir_items.clone());
    }

    @Override
    protected InputStream download3(String path, long startPos, long endPos, boolean binary, boolean server_file) throws Exception {
        path = String.valueOf(this.url) + path.substring(1);
        Common.log("FILE_CLIENT", 2, "DOWNLOAD3:" + path);
        InputStream fin = new FileInputStream(server_file ? new File_S(path) : new File_U(path));
        try {
            if (startPos > 0L) {
                ((InputStream)fin).skip(startPos);
            }
        }
        catch (Exception e) {
            ((InputStream)fin).close();
            throw e;
        }
        if (endPos > 0L) {
            fin = this.getLimitedInputStream(fin, startPos, endPos);
        }
        this.in = fin;
        return this.in;
    }

    @Override
    public boolean mdtm(String path, long modified) throws Exception {
        path = String.valueOf(this.url) + path.substring(1);
        return new File_U(path).setLastModified(modified);
    }

    @Override
    public boolean rename(String rnfr0, String rnto0, boolean overwrite) throws Exception {
        String[] f_list;
        boolean ok;
        String rnfr = String.valueOf(this.url) + rnfr0.substring(1);
        String rnto = String.valueOf(this.url) + rnto0.substring(1);
        Common.log("FILE_CLIENT", 2, "RENAME:" + rnfr + ":" + rnto);
        String cpath1 = null;
        if (memCache) {
            cpath1 = new File_U(rnfr).getCanonicalPath().replace('\\', '/');
        }
        boolean bl = ok = !new File_U(rnto).exists();
        if (!ok && rnfr.toUpperCase().endsWith(".UPLOADING") && new File_U(rnto).length() == 0L) {
            ok = new File_U(rnto).delete();
        }
        if (ok) {
            ok = new File_U(rnfr).renameTo(new File_U(rnto));
        }
        if (Common.machine_is_windows() && new File_U(rnfr).isDirectory() && new File_U(rnfr).exists() && new File_U(rnto).exists() && !new File_U(rnfr).equals(new File_U(rnto)) && ((f_list = new File_U(rnfr).list()) == null || f_list.length == 0)) {
            if (System.getProperty("crushftp.file.securedelete", "false").equals("true")) {
                ok = true;
                if (Common.machine_is_linux()) {
                    Common.exec(("shred;-z;-u;" + rnfr).split(";"));
                } else if (Common.machine_is_windows()) {
                    Common.exec(("sdelete;-nobanner;" + new File(rnfr).getCanonicalPath()).split(";"));
                } else {
                    ok = !new File_U(rnfr).delete();
                }
            } else if (!new File_U(rnfr).delete()) {
                Common.log("FILE_CLIENT", 1, "File " + rnfr + " is used by another process");
            } else {
                ok = true;
            }
        }
        if (memCache && ok) {
            String cpath2 = new File_U(rnto).getCanonicalPath().replace('\\', '/');
            dirCachePerm.remove(cpath1);
            Vector permCacheList1 = (Vector)dirCachePerm.get(Common.all_but_last(cpath1));
            if (permCacheList1 != null) {
                permCacheList1.remove(cpath1);
            }
            this.stat(rnto0);
            Vector permCacheList2 = (Vector)dirCachePerm.get(Common.all_but_last(cpath2));
            if (permCacheList2 != null) {
                permCacheList2.addElement(cpath2);
            }
        }
        return ok;
    }

    @Override
    protected OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        path = String.valueOf(this.url) + path.substring(1);
        Common.log("FILE_CLIENT", 2, "UPLOAD3:" + path);
        RandomOutputStream fout = new RandomOutputStream(new File_U(path), false);
        if (truncate && startPos > 0L) {
            fout.setLength(startPos);
        } else if (truncate && startPos <= 0L) {
            fout.setLength(0L);
        }
        if (startPos > 0L) {
            fout.seek(startPos);
        }
        this.out = fout;
        return fout;
    }

    @Override
    public boolean delete(String path) {
        path = String.valueOf(this.url) + path.substring(1);
        Common.log("FILE_CLIENT", 2, "DELETE:" + path);
        if (memCache) {
            try {
                String cpath = new File_U(path).getCanonicalPath().replace('\\', '/');
                dirCachePerm.remove(cpath);
                Vector permCacheList = (Vector)dirCachePerm.get(Common.all_but_last(cpath));
                if (permCacheList != null) {
                    permCacheList.remove(cpath);
                }
            }
            catch (Exception cpath) {
                // empty catch block
            }
        }
        File_U f = new File_U(path);
        if (this.config.getProperty("file_recurse_delete", "false").equals("true") && f.isDirectory()) {
            return Common.recurseDelete_U(f.getPath(), false);
        }
        if (System.getProperty("crushftp.file.securedelete", "false").equals("true")) {
            String result;
            if (Common.machine_is_linux()) {
                block14: {
                    result = Common.exec(("shred;-z;-u;" + path).split(";"));
                    Common.log("FILE_CLIENT", 2, result);
                    if (!result.trim().equals("")) break block14;
                    Common.log("FILE_CLIENT", 1, "File " + path + " shreded.");
                    return true;
                }
                try {
                    Common.log("SERVER", 0, result);
                }
                catch (Exception e) {
                    Common.log("SERVER", 0, e);
                }
                Common.log("FILE_CLIENT", 1, "File " + path + " shredded.");
                return false;
            }
            if (Common.machine_is_windows()) {
                block15: {
                    result = Common.exec(("sdelete;-nobanner;" + new File(path).getCanonicalPath()).split(";"));
                    Common.log("FILE_CLIENT", 2, result);
                    if (result.indexOf("...deleted.") < 0) break block15;
                    Common.log("FILE_CLIENT", 1, "File " + path + " sdeleted.");
                    return true;
                }
                try {
                    Common.log("SERVER", 0, result);
                }
                catch (Exception e) {
                    Common.log("SERVER", 0, e);
                }
                return false;
            }
            return new File_U(path).delete();
        }
        return f.delete();
    }

    @Override
    public boolean makedir(String path0) {
        boolean ok;
        String path = String.valueOf(this.url) + path0.substring(1);
        if (Common.log("FILE_CLIENT", 2, "")) {
            Common.log("FILE_CLIENT", 2, new Exception("FileClient:MAKEDIR:" + path));
        }
        boolean bl = ok = new File_U(path).exists() && new File_U(path).isDirectory() || new File_U(path).mkdir();
        if (memCache && ok) {
            try {
                String cpath = new File_U(path).getCanonicalPath().replace('\\', '/');
                this.stat(path0);
                Vector<String> permCacheList = (Vector<String>)dirCachePerm.get(Common.all_but_last(cpath));
                if (permCacheList == null) {
                    permCacheList = new Vector<String>();
                    dirCachePerm.put(Common.all_but_last(cpath), permCacheList);
                }
                if (permCacheList.indexOf(cpath) < 0) {
                    permCacheList.addElement(cpath);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return ok;
    }

    @Override
    public boolean makedirs(String path) throws Exception {
        boolean ok;
        path = String.valueOf(this.url) + path.substring(1);
        if (Common.log("FILE_CLIENT", 2, "")) {
            Common.log("FILE_CLIENT", 2, new Exception("FileClient:MAKEDIR2:" + path));
        }
        boolean bl = ok = new File_U(path).exists() || new File_U(path).mkdirs();
        if (memCache && ok) {
            this.makedir(path);
        }
        return ok;
    }

    @Override
    public void setMod(String path, String val, String param) {
        path = path.replace(';', '_').replace('&', '_');
        val = val.replace(';', '_').replace('&', '_');
        param = param.replace(';', '_').replace('&', '_');
        this.doOSCommand(System.getProperty("crushftp.chmod", "chmod"), param, val, path);
    }

    @Override
    public void setOwner(String path, String val, String param) {
        if (val == null || val.equals("")) {
            return;
        }
        path = path.replace(';', '_').replace('&', '_');
        val = val.replace(';', '_').replace('&', '_');
        param = param.replace(';', '_').replace('&', '_');
        if (Common.machine_is_windows()) {
            this.doOSCommand("icacls.exe", path.replace('/', '\\'), "/setowner", val);
        } else {
            this.doOSCommand(System.getProperty("crushftp.chown", "chown"), param, val, path);
        }
    }

    @Override
    public void setGroup(String path, String val, String param) {
        path = path.replace(';', '_').replace('&', '_');
        val = val.replace(';', '_').replace('&', '_');
        param = param.replace(';', '_').replace('&', '_');
        this.doOSCommand(System.getProperty("crushftp.chgrp", "chgrp"), param, val, path);
    }

    public void doOSCommand(String app, String param, String val, String path) {
        Properties p = new Properties();
        p.put("app", app);
        p.put("param", param);
        p.put("val", val);
        p.put("path", path);
        CommandBufferFlusher.commandBuffer.addElement(p);
        CommandBufferFlusher.flushBuffer();
    }

    @Override
    public String doCommand(String command) throws Exception {
        if (command.startsWith("SITE PGP_HEADER_SIZE")) {
            command = command.substring(command.indexOf(" ") + 1);
            command = command.substring(command.indexOf(" ") + 1);
            long size = Long.parseLong(command.substring(0, command.indexOf(" ")).trim());
            if ((command = command.substring(command.indexOf(" ") + 1)).startsWith("/")) {
                command = command.substring(1);
            }
            String path = String.valueOf(this.url) + command.trim();
            RandomOutputStream fout = new RandomOutputStream(new File_U(path), false);
            int offset = ("-----BEGIN PGP MESSAGE-----\r\n" + System.getProperty("appname", "CrushFTP").toUpperCase() + "#").length() + 10;
            fout.seek(offset);
            fout.write(String.valueOf(size).getBytes("UTF8"));
            fout.close();
            return "214 OK";
        }
        return "";
    }

    @Override
    public String getLastMd5() {
        if (this.out != null && this.out instanceof RandomOutputStream) {
            return ((RandomOutputStream)this.out).getLastMd5();
        }
        return super.getLastMd5();
    }
}

