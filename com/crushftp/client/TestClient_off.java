/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.GenericClient;
import com.crushftp.client.VRL;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

public class TestClient_off
extends GenericClient {
    public String client_class_str = "com.crushftp.client.TestClient";
    String base_path = null;

    public TestClient_off(String url, String header, Vector log) {
        super(header, log);
        this.url = url;
    }

    @Override
    public String login2(String username, String password, String clientid) throws Exception {
        throw new Exception("DISABLED");
    }

    public void setConfig(Properties config) {
        this.config = config;
    }

    @Override
    public void logout() throws Exception {
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        File folder;
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        if (!(folder = new File(String.valueOf(this.base_path) + path)).exists()) {
            throw new IOException("No such folder:" + path);
        }
        File[] files = folder.listFiles();
        int x = 0;
        while (x < files.length) {
            list.add(this.stat(String.valueOf(path) + files[x].getName()));
            ++x;
        }
        return list;
    }

    @Override
    public Properties stat(String path) throws Exception {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        Properties p = new Properties();
        File f = new File(String.valueOf(this.base_path) + path);
        if (!f.exists()) {
            return null;
        }
        if (f.isFile()) {
            p.put("permissions", "-rwxrwxrwx");
            p.put("type", "FILE");
        } else {
            p.put("type", "DIR");
            p.put("permissions", "drwxrwxrwx");
            p.put("check_all_recursive_deletes", "true");
        }
        p.put("name", f.getName());
        p.put("path", path);
        p.put("size", String.valueOf(f.length()));
        p.put("url", "custom." + this.client_class_str + "://" + this.config.getProperty("username") + ":" + VRL.vrlEncode(this.config.getProperty("password")) + "@no.host.com" + path);
        p.put("owner", "owner");
        p.put("group", "group");
        p.put("modified", String.valueOf(f.lastModified()));
        p.put("modified", String.valueOf(f.lastModified()));
        p.put("modified", String.valueOf(f.lastModified()));
        Date d = new Date(f.lastModified());
        p.put("month", new SimpleDateFormat("MMM", Locale.US).format(d));
        p.put("day", new SimpleDateFormat("dd", Locale.US).format(d));
        return p;
    }

    @Override
    public InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        this.in = new FileInputStream(String.valueOf(this.base_path) + path);
        this.in.skip(startPos);
        return this.in;
    }

    @Override
    public OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        this.out = new FileOutputStream(String.valueOf(this.base_path) + path, truncate);
        return this.out;
    }

    @Override
    public boolean delete(String path) throws Exception {
        Properties p = this.stat(path);
        if (p == null) {
            return false;
        }
        return new File(String.valueOf(this.base_path) + path).delete();
    }

    @Override
    public boolean makedirs(String path) throws Exception {
        return new File(String.valueOf(this.base_path) + path).mkdirs();
    }

    @Override
    public boolean makedir(String path) throws Exception {
        return new File(String.valueOf(this.base_path) + path).mkdir();
    }

    @Override
    public boolean mdtm(String path, long modified) throws Exception {
        Properties p = this.stat(path);
        if (p == null) {
            return false;
        }
        return new File(String.valueOf(this.base_path) + path).setLastModified(modified);
    }

    @Override
    public boolean rename(String rnfr, String rnto, boolean overwrite) throws Exception {
        return new File(String.valueOf(this.base_path) + rnfr).renameTo(new File(String.valueOf(this.base_path) + rnto));
    }
}

