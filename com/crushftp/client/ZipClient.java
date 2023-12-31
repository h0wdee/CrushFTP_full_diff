/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.FileClient;
import com.crushftp.client.GenericClient;
import com.crushftp.client.VRL;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipClient
extends GenericClient {
    ZipFile lastZip = null;

    public ZipClient(String url, String header, Vector log) {
        super(header, log);
        this.fields = new String[]{"*"};
        this.url = new VRL(url).getPath();
    }

    @Override
    public Properties stat(String path) throws Exception {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String originalPath = path;
        try (ZipFile z = null;){
            Properties dir_item;
            z = new ZipFile(String.valueOf(this.url) + originalPath.substring(1).substring(0, originalPath.substring(1).indexOf("!")));
            path = path.substring(path.indexOf("!") + 1);
            if (path.equals("")) {
                FileClient c = new FileClient(String.valueOf(this.url) + originalPath.substring(1).substring(0, originalPath.substring(1).indexOf("!")), this.logHeader, this.logQueue);
                Properties dir_item2 = c.stat("/");
                dir_item2.put("type", "DIR");
                dir_item2.put("protocol", "ZIP");
                dir_item2.put("url", "zip://" + this.url + originalPath.substring(1));
                Properties properties = dir_item2;
                return properties;
            }
            ZipEntry test = z.getEntry(String.valueOf(path) + "/");
            if (test == null) {
                test = z.getEntry(path);
            }
            if (test == null) {
                return null;
            }
            Properties properties = dir_item = this.stat2(test, originalPath.substring(1));
            return properties;
        }
    }

    private Properties stat2(ZipEntry test, String path) {
        Properties dir_item = new Properties();
        String name = Common.last(test.getName());
        if (name.endsWith("/")) {
            name = name.substring(0, name.length() - 1);
        }
        dir_item.put("name", name.replaceAll("\r", "%0A").replaceAll("\n", "%0D"));
        if (test.isDirectory()) {
            dir_item.put("type", "DIR");
            dir_item.put("permissions", "drwxrwxrwx");
            dir_item.put("size", "1");
            if (!path.endsWith("/")) {
                path = String.valueOf(path) + "/";
            }
        } else {
            dir_item.put("type", "FILE");
            dir_item.put("permissions", "-rwxrwxrwx");
            dir_item.put("size", String.valueOf(test.getSize()));
        }
        dir_item.put("url", "zip://" + this.url + path);
        dir_item.put("link", "false");
        dir_item.put("num_items", "1");
        dir_item.put("owner", "user");
        dir_item.put("group", "group");
        dir_item.put("protocol", "zip");
        this.setFileDateInfo(test, dir_item);
        return dir_item;
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String originalPath = path;
        try (ZipFile z = null;){
            z = new ZipFile(String.valueOf(this.url) + originalPath.substring(1).substring(0, originalPath.substring(1).indexOf("!")));
            path = path.substring(path.indexOf("!") + 1);
            Enumeration<? extends ZipEntry> entries = z.entries();
            String shortName = "";
            while (entries.hasMoreElements()) {
                ZipEntry test = entries.nextElement();
                String itemName = Common.normalize2(test.getName());
                if (Common.all_but_last(itemName).equals("")) {
                    shortName = itemName;
                }
                if (shortName.equals("")) {
                    shortName = Common.all_but_last(itemName);
                }
                if (Common.all_but_last(itemName).length() < shortName.length()) {
                    shortName = Common.all_but_last(itemName);
                }
                if (!Common.all_but_last(itemName).equalsIgnoreCase(path)) continue;
                Properties dir_item = this.stat2(test, "!" + itemName);
                list.add(dir_item);
            }
            if (list.size() == 0) {
                Properties dir_item = new Properties();
                String name = Common.last(shortName);
                if (name.endsWith("/")) {
                    name = name.substring(0, name.length() - 1);
                }
                dir_item.put("name", name.replaceAll("\r", "%0A").replaceAll("\n", "%0D"));
                dir_item.put("type", "DIR");
                dir_item.put("permissions", "drwxrwxrwx");
                dir_item.put("size", "1");
                if (!path.endsWith("/")) {
                    path = String.valueOf(path) + "/";
                }
                dir_item.put("url", "zip://" + this.url + path + shortName);
                dir_item.put("link", "false");
                dir_item.put("num_items", "1");
                dir_item.put("owner", "user");
                dir_item.put("group", "group");
                dir_item.put("protocol", "zip");
                Date itemDate = new Date();
                dir_item.put("modified", String.valueOf(itemDate.getTime()));
                dir_item.put("month", months[Integer.parseInt(this.mm.format(itemDate))]);
                dir_item.put("day", this.dd.format(itemDate));
                String time_or_year = this.hhmm.format(itemDate);
                if (!this.yyyy.format(itemDate).equals(this.yyyy.format(new Date()))) {
                    time_or_year = this.yyyy.format(itemDate);
                }
                dir_item.put("time_or_year", time_or_year);
                list.add(dir_item);
            }
        }
        return list;
    }

    private void setFileDateInfo(ZipEntry test, Properties dir_item) {
        Date itemDate = new Date(test.getTime());
        dir_item.put("modified", String.valueOf(itemDate.getTime()));
        dir_item.put("month", months[Integer.parseInt(this.mm.format(itemDate))]);
        dir_item.put("day", this.dd.format(itemDate));
        String time_or_year = this.hhmm.format(itemDate);
        if (!this.yyyy.format(itemDate).equals(this.yyyy.format(new Date()))) {
            time_or_year = this.yyyy.format(itemDate);
        }
        dir_item.put("time_or_year", time_or_year);
    }

    @Override
    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String originalPath = path;
        this.lastZip = new ZipFile(String.valueOf(this.url) + originalPath.substring(1).substring(0, originalPath.substring(1).indexOf("!")));
        path = path.substring(path.indexOf("!") + 1);
        this.in = this.lastZip.getInputStream(this.lastZip.getEntry(path));
        if (startPos > 0L) {
            this.in.skip(startPos);
        }
        return this.in;
    }

    @Override
    protected OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        throw new IOException("Writing inside zip file is not allowed.");
    }

    @Override
    public boolean mdtm(String path, long modified) throws Exception {
        return false;
    }

    @Override
    public boolean rename(String rnfr, String rnto, boolean overwrite) throws Exception {
        return false;
    }

    @Override
    public boolean delete(String path) {
        return false;
    }

    @Override
    public boolean makedir(String path) {
        return false;
    }

    @Override
    public boolean makedirs(String path) throws Exception {
        return false;
    }

    @Override
    public void close() throws Exception {
        if (this.in != null) {
            this.in.close();
        }
        if (this.out != null) {
            this.out.close();
        }
        if (this.lastZip != null) {
            this.lastZip.close();
        }
    }
}

