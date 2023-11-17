/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jcifs.Config
 *  jcifs.smb.NtlmPasswordAuthentication
 *  jcifs.smb.SmbException
 *  jcifs.smb.SmbFile
 *  jcifs.smb.SmbFileInputStream
 *  jcifs.smb.SmbRandomAccessFile
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.GenericClient;
import com.crushftp.client.SmbRandomOutputStream;
import com.crushftp.client.VRL;
import com.crushftp.client.ZipClient;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import jcifs.Config;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbRandomAccessFile;

public class SMB1Client
extends GenericClient {
    int lsBytesRead = 0;
    Properties dirCache = new Properties();
    NtlmPasswordAuthentication auth = null;

    public SMB1Client(String url, String header, Vector log) {
        super(header, log);
        this.fields = new String[]{"username", "password", "clientid", "domain", "count_dir_items", "checkEncryptedHeader"};
        System.setProperty("jcifs.resolveOrder", "DNS");
        if (url.indexOf("@\\") >= 0) {
            url = url.replace('\\', '/');
        }
        if (url.indexOf("@//") >= 0) {
            url = Common.replace_str(url, "@//", "@");
        }
        if (url.toUpperCase().startsWith("SMB:/") && !url.toUpperCase().startsWith("SMB://")) {
            url = "SMB://" + url.substring(5);
        }
        this.url = url;
        this.config.put("protocol", "SMB");
        Enumeration<Object> keys = System.getProperties().keys();
        while (keys.hasMoreElements()) {
            String key = "" + keys.nextElement();
            if (!key.startsWith("jcifs.")) continue;
            String val = System.getProperty(key, "");
            Config.setProperty((String)key, (String)val);
        }
    }

    public String simpleUrl(String s) {
        if (s.indexOf("@") >= 0) {
            s = "SMB://" + s.substring(s.indexOf("@") + 1);
        }
        return s;
    }

    @Override
    public String login2(String username, String password, String clientid) throws Exception {
        String domain = null;
        username = VRL.vrlDecode(username);
        password = VRL.vrlDecode(password);
        if (username.indexOf("\\") >= 0) {
            domain = username.substring(0, username.indexOf("\\"));
            username = username.substring(username.indexOf("\\") + 1);
        }
        this.config.put("username", username);
        this.config.put("password", password);
        if (domain != null) {
            this.config.put("domain", domain);
        }
        if (clientid != null) {
            this.config.put("clientid", clientid);
        }
        if (domain == null && this.config.containsKey("domain")) {
            domain = this.config.getProperty("domain");
        }
        this.auth = new NtlmPasswordAuthentication(domain, username, password);
        try {
            new SmbFile(this.url, this.auth).connect();
            return "";
        }
        catch (Exception e) {
            throw new Exception("login failed:" + e);
        }
    }

    private String getAbsolutePath(SmbFile f) {
        return Common.machine_is_windows() ? "/" + f.getPath().replace('\\', '/') : f.getPath();
    }

    @Override
    public Properties stat(String path) throws Exception {
        path = String.valueOf(this.url) + path.substring(1);
        SmbFile test = new SmbFile(path, this.auth);
        if (!test.exists() && System.getProperty("crushftp.isTestCall" + Thread.currentThread().getId(), "false").equals("true")) {
            throw new Exception("Item not found...");
        }
        if (!test.exists()) {
            return null;
        }
        return this.stat(test, path);
    }

    public Properties stat(SmbFile test, String path) throws Exception {
        Properties dir_item = new Properties();
        String name = test.getName();
        if (test.isDirectory() && name.endsWith("/")) {
            name = name.substring(0, name.length() - 1);
        }
        dir_item.put("name", name.replaceAll("\r", "%0A").replaceAll("\n", "%0D"));
        dir_item.put("size", "0");
        Common.log("FILE_CLIENT", 2, "List on " + path + "sub item name  : " + name + " is directory:  " + test.isDirectory());
        if (test.isDirectory()) {
            if (!path.endsWith("/")) {
                path = String.valueOf(path) + "/";
                test = new SmbFile(path, this.auth);
            }
            test = new SmbFile(path, this.auth);
            dir_item.put("type", "DIR");
            dir_item.put("permissions", "drwxrwxrwx");
            dir_item.put("size", "1");
            if (this.config.getProperty("count_dir_items", "false").equals("true")) {
                int i = 0;
                SmbFile[] list = test.listFiles();
                int x = 0;
                while (list != null && x < list.length) {
                    if (!list[x].getName().startsWith(".")) {
                        ++i;
                    }
                    ++x;
                }
                dir_item.put("size", String.valueOf(i));
            }
            if (!path.endsWith("/")) {
                path = String.valueOf(path) + "/";
            }
        } else if (test.isFile()) {
            dir_item.put("type", "FILE");
            dir_item.put("permissions", "-rwxrwxrwx");
            dir_item.put("size", String.valueOf(this.getSize(test)));
        }
        try {
            test.exists();
        }
        catch (Exception e) {
            Common.log("FILE_CLIENT", 2, e);
        }
        dir_item.put("url", test.getURL().toExternalForm());
        dir_item.put("link", "false");
        if (Common.isSymbolicLink(this.getAbsolutePath(test))) {
            dir_item.put("link", "true");
        }
        dir_item.put("num_items", "1");
        dir_item.put("owner", "user");
        dir_item.put("group", "group");
        dir_item.put("protocol", "file");
        dir_item.put("root_dir", Common.all_but_last(new VRL(dir_item.getProperty("url")).getPath()));
        this.setFileDateInfo(test, dir_item);
        return dir_item;
    }

    private long getSize(SmbFile test) throws SmbException {
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
        String originalPath = path;
        path = String.valueOf(this.url) + path.substring(1);
        if (!path.endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        Common.log("FILE_CLIENT", 2, "Getting list for:" + path);
        SmbFile item = new SmbFile(path, this.auth);
        SmbFile[] items = item.listFiles();
        if ((this.getAbsolutePath(item).toLowerCase().endsWith(".zip") || this.getAbsolutePath(item).toLowerCase().indexOf(".zip/") >= 0) && path.endsWith("/")) {
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
            items = new SmbFile[]{item};
        }
        if (items == null) {
            items = new SmbFile[]{};
        }
        this.dirCache.clear();
        int x = 0;
        while (x < items.length) {
            SmbFile test = items[x];
            try {
                String tempName = test.getName();
                Common.log("FILE_CLIENT", 2, "List on " + path + "found name : " + tempName);
                if (test.isDirectory() && tempName.endsWith("/")) {
                    tempName = tempName.substring(0, tempName.length() - 1);
                }
                if (Common.machine_is_windows() && path.equals("/")) {
                    tempName = items[x].getPath().substring(0, 2);
                }
                String tempPath = String.valueOf(originalPath) + tempName;
                tempPath = String.valueOf(this.url) + tempPath.substring(1);
                Common.log("FILE_CLIENT", 2, "List on " + path + "found path : " + tempPath);
                Properties dir_item = this.stat(test, tempPath);
                if (dir_item.getProperty("type").equalsIgnoreCase("FILE")) {
                    dir_item.put("size", String.valueOf(this.getSize(test)));
                }
                list.add(dir_item);
            }
            catch (Exception e) {
                Common.log("FILE_CLIENT", 1, String.valueOf(x) + " of " + items.length + ":Invalid file, or dead alias:" + test);
                Common.log("FILE_CLIENT", 1, e);
            }
            ++x;
        }
        this.dirCache.clear();
        return list;
    }

    private void setFileDateInfo(SmbFile test, Properties dir_item) throws SmbException {
        Date itemDate = new Date(test.lastModified());
        dir_item.put("modified", String.valueOf(itemDate.getTime()));
        dir_item.put("month", months[Integer.parseInt(this.mm.format(itemDate))]);
        dir_item.put("day", this.dd.format(itemDate));
        String time_or_year = this.hhmm.format(itemDate);
        if (!this.yyyy.format(itemDate).equals(this.yyyy.format(new Date())) || System.getProperty("crushftp.ls.year", "false").equals("true")) {
            time_or_year = this.yyyy.format(itemDate);
        }
        dir_item.put("time_or_year", time_or_year);
    }

    @Override
    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        path = String.valueOf(this.url) + path.substring(1);
        Object fin = new SmbFileInputStream(new SmbFile(path, this.auth));
        try {
            if (startPos > 0L) {
                fin.skip(startPos);
            }
        }
        catch (Exception e) {
            fin.close();
            throw e;
        }
        if (endPos > 0L) {
            fin = this.getLimitedInputStream((InputStream)fin, startPos, endPos);
        }
        this.in = fin;
        return this.in;
    }

    @Override
    public boolean mdtm(String path, long modified) throws Exception {
        path = String.valueOf(this.url) + path.substring(1);
        new SmbFile(path, this.auth).setLastModified(modified);
        return true;
    }

    @Override
    public boolean rename(String rnfr, String rnto, boolean overwrite) {
        boolean f1_exists1;
        SmbFile f1;
        SmbFile f2;
        block6: {
            block5: {
                rnfr = String.valueOf(this.url) + rnfr.substring(1);
                rnto = String.valueOf(this.url) + rnto.substring(1);
                f2 = new SmbFile(rnto, this.auth);
                if (!f2.exists()) break block5;
                return false;
            }
            f1 = new SmbFile(rnfr, this.auth);
            f1_exists1 = f1.exists();
            boolean f2_exists1 = f2.exists();
            f1.renameTo(f2);
            if (f2_exists1 || !f2.exists()) break block6;
            return true;
        }
        try {
            if (f1_exists1 && !f1.exists() && f2.exists()) {
                return true;
            }
        }
        catch (Exception e) {
            this.log(e);
        }
        return false;
    }

    @Override
    protected OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        path = String.valueOf(this.url) + path.substring(1);
        Common.log("FILE_CLIENT", 2, "Uploading:" + path);
        SmbFile smb = new SmbFile(path, this.auth);
        try {
            if (smb.exists() && truncate) {
                smb.delete();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        SmbRandomOutputStream fout = new SmbRandomOutputStream(smb, false);
        if (truncate && startPos > 0L && startPos != smb.length()) {
            fout.setLength(startPos);
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
        try {
            new SmbFile(path, this.auth).delete();
        }
        catch (Exception e) {
            if (("" + e).indexOf("find the file") < 0) {
                this.log(e);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean makedir(String path) {
        path = String.valueOf(this.url) + path.substring(1);
        try {
            SmbFile f = new SmbFile(path, this.auth);
            if (!f.exists()) {
                f.mkdir();
            }
        }
        catch (Exception e) {
            this.log(e);
            return false;
        }
        return true;
    }

    @Override
    public boolean makedirs(String path) throws Exception {
        boolean ok = true;
        String[] parts = path.split("/");
        String path2 = "";
        int x = 0;
        while (x < parts.length && ok) {
            path2 = String.valueOf(path2) + parts[x] + "/";
            if (x >= 1 && this.stat(path2) == null) {
                ok = this.makedir(path2);
            }
            ++x;
        }
        return ok;
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
            SmbRandomAccessFile fout = new SmbRandomAccessFile(new SmbFile(path, this.auth), "rw");
            int offset = ("-----BEGIN PGP MESSAGE-----\r\n" + System.getProperty("appname", "CrushFTP").toUpperCase() + "#").length() + 10;
            fout.seek((long)offset);
            fout.write(String.valueOf(size).getBytes("UTF8"));
            fout.close();
            return "214 OK";
        }
        return "";
    }
}

