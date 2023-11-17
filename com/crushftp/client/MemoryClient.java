/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.GenericClient;
import com.crushftp.client.VRL;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

public class MemoryClient
extends GenericClient {
    public static Properties ram = new Properties();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public MemoryClient(String url, String header, Vector log) {
        super(header, log);
        this.fields = new String[]{"*"};
        this.url = new VRL(url).getPath();
        Properties properties = ram;
        synchronized (properties) {
            if (ram.get("/") == null) {
                Vector v = new Vector();
                Properties item = new Properties();
                item.put("list", v);
                ram.put("", item);
                ram.put("/", item);
            }
        }
    }

    @Override
    public void logout() throws Exception {
        this.close();
        this.logQueue = new Vector();
    }

    public void freeCache() {
        this.logQueue = new Vector();
    }

    @Override
    public Properties stat(String path) throws Exception {
        path = String.valueOf(this.url) + path.substring(1);
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return this.strip((Properties)ram.get(path));
    }

    private Properties strip(Properties item) {
        if (item != null) {
            item = (Properties)item.clone();
            item.remove("object");
            item.remove("list");
        }
        return item;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Vector list(String path, Vector list) throws Exception {
        Properties item;
        Vector v;
        path = String.valueOf(this.url) + path.substring(1);
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if ((v = (Vector)(item = (Properties)((Properties)ram.get(path)).clone()).get("list")) == null) {
            v = new Vector();
        }
        Properties properties = ram;
        synchronized (properties) {
            int x = 0;
            while (x < v.size()) {
                list.addElement(this.strip((Properties)v.elementAt(x)));
                ++x;
            }
        }
        return list;
    }

    @Override
    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        path = String.valueOf(this.url) + path.substring(1);
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        Properties item = (Properties)ram.get(path);
        this.in = new ByteArrayInputStream(((ByteArrayOutputStream)item.get("object")).toByteArray());
        if (startPos > 0L) {
            this.in.skip(startPos);
        }
        if (endPos > 0L) {
            this.in = this.getLimitedInputStream(this.in, startPos, endPos);
        }
        return this.in;
    }

    @Override
    public boolean mdtm(String path, long modified) throws Exception {
        Properties item;
        path = String.valueOf(this.url) + path.substring(1);
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if ((item = (Properties)ram.get(path)) == null) {
            return false;
        }
        Date itemDate = new Date(modified);
        item.put("modified", String.valueOf(itemDate.getTime()));
        item.put("month", months[Integer.parseInt(this.mm.format(itemDate))]);
        item.put("day", this.dd.format(itemDate));
        String time_or_year = this.hhmm.format(itemDate);
        if (!this.yyyy.format(itemDate).equals(this.yyyy.format(new Date())) || System.getProperty("crushftp.ls.year", "false").equals("true")) {
            time_or_year = this.yyyy.format(itemDate);
        }
        item.put("time_or_year", time_or_year);
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean rename(String rnfr0, String rnto0, boolean overwrite) throws Exception {
        Vector v;
        String rnfr = String.valueOf(this.url) + rnfr0.substring(1);
        String rnto = String.valueOf(this.url) + rnto0.substring(1);
        if (rnfr.endsWith("/")) {
            rnfr = rnfr.substring(0, rnfr.length() - 1);
        }
        if (rnto.endsWith("/")) {
            rnto = rnto.substring(0, rnto.length() - 1);
        }
        Properties rnfr_p = (Properties)ram.get(rnfr);
        Properties rnto_p = (Properties)ram.get(rnto);
        if (rnfr_p == null) {
            return false;
        }
        if (rnto_p != null) {
            return false;
        }
        rnfr_p.put("name", Common.last(rnto));
        rnfr_p.put("url", "MEMORY://" + rnto);
        ram.put(rnto, ram.remove(rnfr));
        if (!Common.all_but_last(rnfr).equals(Common.all_but_last(rnto))) {
            String parent_path = Common.all_but_last(rnfr);
            parent_path = parent_path.substring(0, parent_path.length() - 1);
            Properties parent_item = (Properties)ram.get(parent_path);
            Vector v2 = (Vector)parent_item.get("list");
            v2.remove(rnfr_p);
            parent_path = Common.all_but_last(rnto);
            parent_path = parent_path.substring(0, parent_path.length() - 1);
            parent_item = (Properties)ram.get(parent_path);
            v2 = (Vector)parent_item.get("list");
            v2.addElement(rnfr_p);
        }
        if ((v = (Vector)rnfr_p.get("list")) != null) {
            Properties properties = ram;
            synchronized (properties) {
                int x = 0;
                while (x < v.size()) {
                    Properties item2 = (Properties)v.elementAt(x);
                    this.rename(String.valueOf(rnfr) + "/" + item2.getProperty("name"), String.valueOf(rnto) + "/" + item2.getProperty("name"), overwrite);
                    ++x;
                }
            }
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected OutputStream upload3(String path0, long startPos, boolean truncate, boolean binary) throws Exception {
        Properties item;
        String path = String.valueOf(this.url) + path0.substring(1);
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if ((item = (Properties)ram.get(path)) != null && item.getProperty("type").equalsIgnoreCase("DIR")) {
            throw new Exception("Can't overwrite memory dir with file.");
        }
        this.delete(path0);
        Properties properties = ram;
        synchronized (properties) {
            String name = Common.last(path);
            Properties dir_item = new Properties();
            String parent_path = Common.all_but_last(path);
            parent_path = parent_path.substring(0, parent_path.length() - 1);
            Properties parent_item = (Properties)ram.get(parent_path);
            Vector v = (Vector)parent_item.get("list");
            v.addElement(dir_item);
            dir_item.put("name", name);
            dir_item.put("size", "0");
            dir_item.put("type", "FILE");
            dir_item.put("permissions", "-rwxrwxrwx");
            dir_item.put("size", "0");
            dir_item.put("url", "MEMORY://" + path);
            dir_item.put("link", "false");
            dir_item.put("num_items", "1");
            dir_item.put("owner", "user");
            dir_item.put("group", "group");
            dir_item.put("protocol", "memory");
            dir_item.put("root_dir", Common.all_but_last(path));
            Date itemDate = new Date();
            dir_item.put("modified", String.valueOf(itemDate.getTime()));
            dir_item.put("month", months[Integer.parseInt(this.mm.format(itemDate))]);
            dir_item.put("day", this.dd.format(itemDate));
            String time_or_year = this.hhmm.format(itemDate);
            if (!this.yyyy.format(itemDate).equals(this.yyyy.format(new Date())) || System.getProperty("crushftp.ls.year", "false").equals("true")) {
                time_or_year = this.yyyy.format(itemDate);
            }
            dir_item.put("time_or_year", time_or_year);
            class ByteArrayOutputStreamWrapper
            extends ByteArrayOutputStream {
                Properties item = null;

                public ByteArrayOutputStreamWrapper(Properties item) {
                    this.item = item;
                }

                @Override
                public void close() throws IOException {
                    super.close();
                    this.item.put("size", String.valueOf(this.toByteArray().length));
                    this.item.put("modified", String.valueOf(System.currentTimeMillis()));
                }
            }
            this.out = new ByteArrayOutputStreamWrapper(dir_item);
            dir_item.put("object", this.out);
            ram.put(path, dir_item);
        }
        return this.out;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean delete(String path) {
        Properties item;
        path = String.valueOf(this.url) + path.substring(1);
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if ((item = (Properties)ram.get(path)) == null) {
            return false;
        }
        String parent_path = Common.all_but_last(path);
        parent_path = parent_path.substring(0, parent_path.length() - 1);
        Properties parent_item = (Properties)ram.get(parent_path);
        Vector v = (Vector)parent_item.get("list");
        v.remove(item);
        v = (Vector)item.get("list");
        if (v != null) {
            Properties properties = ram;
            synchronized (properties) {
                int x = 0;
                while (x < v.size()) {
                    Properties item2 = (Properties)v.elementAt(x);
                    this.delete(String.valueOf(path) + "/" + item2.getProperty("name"));
                    ++x;
                }
            }
        }
        ram.remove(path);
        return item != null;
    }

    @Override
    public boolean makedir(String path0) {
        Properties item;
        String path = String.valueOf(this.url) + path0.substring(1);
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if ((item = (Properties)ram.get(path)) != null) {
            return false;
        }
        String name = Common.last(path);
        Properties dir_item = new Properties();
        dir_item.put("name", name);
        dir_item.put("size", "0");
        dir_item.put("type", "DIR");
        dir_item.put("permissions", "drwxrwxrwx");
        dir_item.put("size", "0");
        dir_item.put("url", "MEMORY://" + path + "/");
        dir_item.put("link", "false");
        dir_item.put("num_items", "1");
        dir_item.put("owner", "user");
        dir_item.put("group", "group");
        dir_item.put("protocol", "memory");
        dir_item.put("root_dir", Common.all_but_last(path));
        Date itemDate = new Date();
        dir_item.put("modified", String.valueOf(itemDate.getTime()));
        dir_item.put("month", months[Integer.parseInt(this.mm.format(itemDate))]);
        dir_item.put("day", this.dd.format(itemDate));
        dir_item.put("list", new Vector());
        String time_or_year = this.hhmm.format(itemDate);
        if (!this.yyyy.format(itemDate).equals(this.yyyy.format(new Date())) || System.getProperty("crushftp.ls.year", "false").equals("true")) {
            time_or_year = this.yyyy.format(itemDate);
        }
        dir_item.put("time_or_year", time_or_year);
        String parent_path = Common.all_but_last(path);
        parent_path = parent_path.substring(0, parent_path.length() - 1);
        Properties parent_item = (Properties)ram.get(parent_path);
        Vector v = (Vector)parent_item.get("list");
        v.addElement(dir_item);
        ram.put(path, dir_item);
        return true;
    }

    @Override
    public boolean makedirs(String path0) throws Exception {
        String path = this.url;
        String[] parts = path0.split("/");
        boolean ok = false;
        int x = 0;
        while (x < parts.length) {
            if (!parts[x].trim().equals("")) {
                path = String.valueOf(path) + parts[x] + "/";
                ok |= this.makedir(path);
            }
            ++x;
        }
        return ok;
    }

    @Override
    public void setMod(String path, String val, String param) {
        path = String.valueOf(this.url) + path.substring(1);
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        Properties item = (Properties)ram.get(path);
        if (val != null) {
            item.put("mod", val);
        }
    }

    @Override
    public void setOwner(String path, String val, String param) {
        path = String.valueOf(this.url) + path.substring(1);
        Properties item = (Properties)ram.get(path);
        if (val != null) {
            item.put("owner", val);
        }
    }

    @Override
    public void setGroup(String path, String val, String param) {
        path = String.valueOf(this.url) + path.substring(1);
        Properties item = (Properties)ram.get(path);
        if (val != null) {
            item.put("group", val);
        }
    }

    public void doOSCommand(String app, String param, String val, String path) {
    }

    @Override
    public String doCommand(String command) throws Exception {
        return "";
    }
}

