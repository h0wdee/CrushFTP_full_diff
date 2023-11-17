/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.HTTPClient;
import com.crushftp.client.Worker;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

public class HTTPBufferedClient
extends HTTPClient {
    public static Properties mem = new Properties();
    public static Vector unpublished_changes = new Vector();
    public static long last_action = 0L;
    static boolean http_buffer_cleaner_running = false;
    public RandomAccessFile raf2 = null;

    public HTTPBufferedClient(String url, String header, Vector log) {
        super(url, header, log);
        this.fields = new String[]{"*"};
        if (!url.endsWith("/")) {
            url = String.valueOf(url) + "/";
        }
        this.url = url;
        this.config.put("protocol", "HTTP");
        if (!http_buffer_cleaner_running) {
            http_buffer_cleaner_running = true;
            try {
                Worker.startWorker(new Runnable(){

                    /*
                     * Unable to fully structure code
                     */
                    @Override
                    public void run() {
                        block4: while (true) {
                            try {
                                Thread.sleep(1000L);
                            }
                            catch (InterruptedException var1_2) {}
                            while (true) {
                                if (HTTPBufferedClient.unpublished_changes.size() <= 0) continue block4;
                                if (System.currentTimeMillis() - HTTPBufferedClient.last_action >= 10000L) ** break;
                                continue block4;
                                try {
                                    command = HTTPBufferedClient.unpublished_changes.elementAt(0).toString().split(";")[0];
                                    path = HTTPBufferedClient.unpublished_changes.elementAt(0).toString().split(";")[1];
                                    if (command.equals("upload")) {
                                        if (new File(HTTPBufferedClient.getUid(path)).exists()) {
                                            HTTPBufferedClient.this.log("BUFFERED:" + command + ":" + path);
                                            Common.copyStreams(new FileInputStream(HTTPBufferedClient.getUid(path)), HTTPBufferedClient.this.upload4(path, 0L, true, true), true, true);
                                            new File(HTTPBufferedClient.getUid(path)).delete();
                                            HTTPBufferedClient.mem.remove("stat:" + HTTPBufferedClient.noSlash(path));
                                        }
                                    } else if (command.equals("mdtm")) {
                                        HTTPBufferedClient.this.log("BUFFERED:" + command + ":" + path);
                                        HTTPBufferedClient.this.doAction2("mdtm", path, new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(Long.parseLong(HTTPBufferedClient.unpublished_changes.elementAt(0).toString().split(";")[2])))).equals("");
                                        HTTPBufferedClient.mem.remove("stat:" + HTTPBufferedClient.noSlash(path));
                                    } else if (command.equals("rename")) {
                                        path2 = HTTPBufferedClient.unpublished_changes.elementAt(0).toString().split(";")[2];
                                        HTTPBufferedClient.this.log("BUFFERED:" + command + ":" + path + ":" + path2);
                                        HTTPBufferedClient.this.doAction2("rename", path, path2);
                                        HTTPBufferedClient.mem.remove("stat:" + HTTPBufferedClient.noSlash(path));
                                        HTTPBufferedClient.mem.remove("stat:" + HTTPBufferedClient.noSlash(path2));
                                        HTTPBufferedClient.mem.remove("list:" + HTTPBufferedClient.noSlash(Common.all_but_last(path)));
                                        HTTPBufferedClient.mem.remove("list:" + HTTPBufferedClient.noSlash(Common.all_but_last(path2)));
                                    } else if (command.equals("delete")) {
                                        HTTPBufferedClient.this.log("BUFFERED:" + command + ":" + path);
                                        HTTPBufferedClient.this.doAction2("delete", path, "");
                                        HTTPBufferedClient.mem.remove("stat:" + HTTPBufferedClient.noSlash(path));
                                        HTTPBufferedClient.mem.remove("list:" + HTTPBufferedClient.noSlash(Common.all_but_last(path)));
                                    }
                                    HTTPBufferedClient.unpublished_changes.removeElementAt(0);
                                }
                                catch (Exception var1_3) {
                                    // empty catch block
                                }
                            }
                            break;
                        }
                    }
                });
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
    }

    public static boolean isBusy() {
        return unpublished_changes.size() > 0;
    }

    @Override
    public void close() throws Exception {
        if (this.in != null) {
            this.in.close();
        }
        if (this.out != null) {
            this.out.close();
        }
        this.in = null;
        this.out = null;
    }

    @Override
    public Properties stat(String path) throws Exception {
        if (mem.containsKey("stat:" + HTTPBufferedClient.noSlash(path))) {
            Properties p = (Properties)mem.get("stat:" + HTTPBufferedClient.noSlash(path));
            if (System.currentTimeMillis() - Long.parseLong(p.getProperty("time")) < 60000L) {
                return (Properties)p.get("obj");
            }
            mem.remove("stat:" + HTTPBufferedClient.noSlash(path));
        }
        Properties stat = super.stat(path);
        Properties p = new Properties();
        p.put("time", String.valueOf(System.currentTimeMillis()));
        if (stat != null) {
            p.put("obj", stat);
        }
        if (new File(HTTPBufferedClient.getUid(path)).exists() && stat != null) {
            stat.put("size", String.valueOf(new File(HTTPBufferedClient.getUid(path)).length()));
        }
        mem.put("stat:" + HTTPBufferedClient.noSlash(path), p);
        return stat;
    }

    @Override
    public Properties list2(String path, Vector list) throws Exception {
        this.log("list2:" + path);
        if (mem.containsKey("list:" + HTTPBufferedClient.noSlash(path))) {
            Properties p = (Properties)mem.get("list:" + HTTPBufferedClient.noSlash(path));
            this.log("stat_cache_lookup_list:" + HTTPBufferedClient.noSlash(path) + ":" + (System.currentTimeMillis() - Long.parseLong(p.getProperty("time"))) + "ms");
            if (System.currentTimeMillis() - Long.parseLong(p.getProperty("time")) < 60000L) {
                Properties listingProp = (Properties)p.get("obj");
                Vector list2 = (Vector)listingProp.get("listing");
                this.log("list2-end (cache):" + path + ":" + list2.size());
                list.addAll(list2);
                return (Properties)listingProp.clone();
            }
            mem.remove("list:" + HTTPBufferedClient.noSlash(path));
        }
        Properties listingProp = super.list2(path, list);
        Vector list2 = (Vector)listingProp.get("listing");
        Properties p = new Properties();
        p.put("time", String.valueOf(System.currentTimeMillis()));
        p.put("obj", listingProp.clone());
        mem.put("list:" + HTTPBufferedClient.noSlash(path), p);
        int x = 0;
        while (x < list2.size()) {
            Properties stat = (Properties)list2.elementAt(x);
            Properties p2 = new Properties();
            p2.put("time", String.valueOf(System.currentTimeMillis()));
            p2.put("obj", stat);
            mem.put("stat:" + HTTPBufferedClient.noSlash(path) + "/" + stat.getProperty("name"), p2);
            this.log("stat_cached:" + HTTPBufferedClient.noSlash(path) + "/" + stat.getProperty("name"));
            System.out.println("stat_raw:" + stat);
            ++x;
        }
        this.log("list2-end:" + path + ":" + list2.size());
        return listingProp;
    }

    @Override
    protected InputStream download3(final String path, long startPos, long endPos, boolean binary, String paths, int rev) throws Exception {
        last_action = System.currentTimeMillis();
        if (!new File(HTTPBufferedClient.getUid(path)).exists()) {
            final InputStream in_tmp = super.download3(path, startPos, endPos, binary, paths, rev);
            Thread t = new Thread(new Runnable(){

                @Override
                public void run() {
                    try {
                        new File(HTTPBufferedClient.getUid(path)).delete();
                        Common.copyStreams(in_tmp, new FileOutputStream(HTTPBufferedClient.getUid(path), false), true, true);
                        new File(HTTPBufferedClient.getUid(path)).deleteOnExit();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            });
            t.start();
            t.join(10000L);
            if (t.isAlive()) {
                this.log("download taking too long for " + path + " spawing background thread for double download while we archive a copy.");
                return super.download3(path, startPos, endPos, binary, paths, rev);
            }
        }
        class InputWrapper
        extends InputStream {
            RandomAccessFile raf = null;
            long bytes = 0L;
            private final /* synthetic */ String val$path;
            private final /* synthetic */ long val$startPos;
            private final /* synthetic */ long val$endPos;
            private final /* synthetic */ boolean val$binary;

            public InputWrapper(String path, long startsPos, long endPos, long l, boolean bl, String string, long l2) throws IOException {
                this.val$startPos = l;
                this.val$binary = bl;
                this.val$path = string;
                this.val$endPos = l2;
                HTTPBufferedClient.this.log("download (cache) | " + path + " | " + l + " | " + endPos + " | " + bl + " | " + HTTPBufferedClient.getUid(path));
                HTTPBufferedClient.this.raf2 = this.raf = new RandomAccessFile(HTTPBufferedClient.getUid(path), "r");
                this.raf.seek(l);
            }

            @Override
            public int read() throws IOException {
                last_action = System.currentTimeMillis();
                int i = this.raf.read();
                if (i > 0) {
                    ++this.bytes;
                }
                return i;
            }

            @Override
            public int read(byte[] b) throws IOException {
                last_action = System.currentTimeMillis();
                return this.read(b, 0, b.length);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                last_action = System.currentTimeMillis();
                int i = this.raf.read(b, off, len);
                if (i > 0) {
                    this.bytes += (long)i;
                }
                return i;
            }

            @Override
            public void close() throws IOException {
                last_action = System.currentTimeMillis();
                this.raf.close();
                HTTPBufferedClient.this.log("download-end (cache) | " + this.val$path + " | " + this.val$startPos + " | " + this.val$endPos + " | " + this.val$binary + " | " + this.bytes + " bytes");
            }
        }
        this.in = new InputWrapper(path, startPos, endPos, startPos, binary, path, endPos);
        return this.in;
    }

    protected OutputStream upload4(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        return super.upload3(path, startPos, truncate, binary);
    }

    @Override
    protected OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        last_action = System.currentTimeMillis();
        mem.remove("stat:" + HTTPBufferedClient.noSlash(path));
        unpublished_changes.addElement("upload;" + HTTPBufferedClient.noSlash(path));
        class OutputWrapper
        extends OutputStream {
            RandomAccessFile raf = null;
            long bytes = 0L;
            private final /* synthetic */ String val$path;
            private final /* synthetic */ long val$startPos;
            private final /* synthetic */ boolean val$truncate;
            private final /* synthetic */ boolean val$binary;

            public OutputWrapper(String path, long startPos, boolean truncate, boolean bl, String string, long l, boolean bl2) throws IOException {
                this.val$binary = bl;
                this.val$path = string;
                this.val$startPos = l;
                this.val$truncate = bl2;
                last_action = System.currentTimeMillis();
                HTTPBufferedClient.this.log("upload (cache) | " + path + " | " + startPos + " | " + truncate + " | " + bl + " | " + HTTPBufferedClient.getUid(path));
                HTTPBufferedClient.this.raf2 = this.raf = new RandomAccessFile(HTTPBufferedClient.getUid(path), "rw");
                if (truncate) {
                    this.raf.setLength(startPos);
                }
                this.raf.seek(startPos);
                new File(HTTPBufferedClient.getUid(path)).deleteOnExit();
            }

            @Override
            public void write(int i) throws IOException {
                last_action = System.currentTimeMillis();
                this.raf.write(i);
                ++this.bytes;
            }

            @Override
            public void write(byte[] b) throws IOException {
                last_action = System.currentTimeMillis();
                this.write(b, 0, b.length);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                last_action = System.currentTimeMillis();
                this.raf.write(b, off, len);
                this.bytes += (long)len;
            }

            @Override
            public void close() throws IOException {
                last_action = System.currentTimeMillis();
                long len = 0L;
                try {
                    len = this.raf.getFilePointer();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                try {
                    this.raf.close();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                HTTPBufferedClient.this.log("upload-end (cache) | " + this.val$path + " | " + this.val$startPos + " | " + this.val$truncate + " | " + this.val$binary + " | " + this.bytes + " bytes");
                try {
                    Properties stat = HTTPBufferedClient.this.stat(this.val$path);
                    stat.put("size", String.valueOf(len));
                    stat.put("modified", String.valueOf(System.currentTimeMillis()));
                    Properties p = (Properties)mem.get("stat:" + HTTPBufferedClient.noSlash(this.val$path));
                    p.put("time", String.valueOf(System.currentTimeMillis()));
                    Vector<Properties> list_tmp = new Vector<Properties>();
                    HTTPBufferedClient.this.list(Common.all_but_last(this.val$path), list_tmp);
                    list_tmp.addElement(stat);
                    Properties listingProp = new Properties();
                    listingProp.put("listing", list_tmp);
                    p = new Properties();
                    p.put("time", String.valueOf(System.currentTimeMillis()));
                    p.put("obj", listingProp.clone());
                    mem.put("list:" + HTTPBufferedClient.noSlash(Common.all_but_last(this.val$path)), p);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        this.out = new OutputWrapper(path, startPos, truncate, binary, path, startPos, truncate);
        return this.out;
    }

    @Override
    public boolean upload_0_byte(String path) throws Exception {
        last_action = System.currentTimeMillis();
        mem.remove("stat:" + HTTPBufferedClient.noSlash(path));
        RandomAccessFile raf = new RandomAccessFile(HTTPBufferedClient.getUid(path), "rw");
        raf.setLength(0L);
        raf.close();
        new File(HTTPBufferedClient.getUid(path)).deleteOnExit();
        return this.doAction("upload_0_byte", path, "").trim().equalsIgnoreCase("OK");
    }

    @Override
    public boolean delete(String path) throws Exception {
        last_action = System.currentTimeMillis();
        Properties new_p = this.remove_from_list(HTTPBufferedClient.noSlash(Common.all_but_last(path)), path);
        mem.remove("stat:" + HTTPBufferedClient.noSlash(path));
        new File(HTTPBufferedClient.getUid(path)).delete();
        unpublished_changes.addElement("delete;" + HTTPBufferedClient.noSlash(path));
        mem.put("list:" + HTTPBufferedClient.noSlash(Common.all_but_last(path)), new_p);
        return this.doAction("delete", path, "").equals("");
    }

    @Override
    public boolean makedir(String path) throws Exception {
        last_action = System.currentTimeMillis();
        mem.remove("stat:" + HTTPBufferedClient.noSlash(path));
        mem.remove("list:" + HTTPBufferedClient.noSlash(Common.all_but_last(path)));
        return this.doAction("makedir", path, "").equals("");
    }

    @Override
    public boolean makedirs(String path) throws Exception {
        return this.makedir(path);
    }

    public Properties remove_from_list(String parent_path, String path) throws Exception {
        Vector list_tmp = new Vector();
        this.list(parent_path, list_tmp);
        Properties listingProp = new Properties();
        listingProp.put("listing", list_tmp);
        Properties p = new Properties();
        p.put("time", String.valueOf(System.currentTimeMillis()));
        p.put("obj", listingProp.clone());
        int found = -1;
        int x = 0;
        while (x < list_tmp.size()) {
            Properties stat = (Properties)list_tmp.elementAt(x);
            if (stat.getProperty("name").equals(HTTPBufferedClient.noSlash(Common.last(path)))) {
                found = x;
            }
            ++x;
        }
        if (found >= 0) {
            list_tmp.removeElementAt(found);
        }
        return p;
    }

    @Override
    public boolean rename(String rnfr, String rnto, boolean overwrite) throws Exception {
        last_action = System.currentTimeMillis();
        Properties rnfr_stat_rnto = this.stat(rnfr);
        Properties new_p = this.remove_from_list(Common.all_but_last(rnfr), rnfr);
        mem.remove("stat:" + HTTPBufferedClient.noSlash(rnfr));
        mem.remove("stat:" + HTTPBufferedClient.noSlash(rnto));
        mem.put("list:" + HTTPBufferedClient.noSlash(Common.all_but_last(rnfr)), new_p);
        mem.remove("list:" + HTTPBufferedClient.noSlash(Common.all_but_last(rnfr)));
        if (new File(HTTPBufferedClient.getUid(rnfr)).exists()) {
            rnfr_stat_rnto.put("name", HTTPBufferedClient.noSlash(Common.last(rnto)));
            String url_tmp = rnfr_stat_rnto.getProperty("url");
            rnfr_stat_rnto.put("url", String.valueOf(url_tmp.substring(0, url_tmp.lastIndexOf(rnfr))) + rnto);
            Vector<Properties> list_tmp = new Vector<Properties>();
            this.list(Common.all_but_last(rnto), list_tmp);
            list_tmp.addElement(rnfr_stat_rnto);
            Properties listingProp = new Properties();
            listingProp.put("listing", list_tmp);
            Properties p = new Properties();
            p.put("time", String.valueOf(System.currentTimeMillis()));
            p.put("obj", listingProp.clone());
            mem.remove("list:" + HTTPBufferedClient.noSlash(Common.all_but_last(rnto)));
            p = new Properties();
            p.put("time", String.valueOf(System.currentTimeMillis()));
            p.put("obj", rnfr_stat_rnto.clone());
            mem.put("stat:" + HTTPBufferedClient.noSlash(rnto), p);
            new File(HTTPBufferedClient.getUid(rnto)).delete();
            new File(HTTPBufferedClient.getUid(rnfr)).renameTo(new File(HTTPBufferedClient.getUid(rnto)));
            unpublished_changes.addElement("rename;" + HTTPBufferedClient.noSlash(rnfr) + ";" + HTTPBufferedClient.noSlash(rnto));
        } else if (rnfr_stat_rnto != null) {
            this.doAction2("rename", rnfr, rnto);
        }
        new File(HTTPBufferedClient.getUid(rnfr)).deleteOnExit();
        new File(HTTPBufferedClient.getUid(rnto)).deleteOnExit();
        return true;
    }

    @Override
    public boolean mdtm(String path, long modified) throws Exception {
        last_action = System.currentTimeMillis();
        mem.remove("stat:" + HTTPBufferedClient.noSlash(path));
        unpublished_changes.addElement("mdtm;" + HTTPBufferedClient.noSlash(path) + ";" + modified);
        return true;
    }

    public String doAction2(String command, String param1, String param2) throws Exception {
        return super.doAction(command, param1, param2);
    }

    public static String getUid(String path) {
        String uid = String.valueOf(System.getProperty("crushftpdrive.tmp", System.getProperty("java.io.tmpdir"))) + HTTPBufferedClient.noSlash(path).replace('/', '_');
        return uid;
    }
}

