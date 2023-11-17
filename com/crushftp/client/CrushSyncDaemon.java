/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Base64;
import com.crushftp.client.Common;
import com.crushftp.client.CrushSyncScanner;
import com.crushftp.client.FileClient;
import com.crushftp.client.GenericClient;
import com.crushftp.client.SnapshotFile;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import com.crushftp.tunnel2.Tunnel2;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class CrushSyncDaemon {
    public static final String version = "3.12.17";
    public static String app_name = "CrushSync";
    static CrushSyncDaemon thisObj = null;
    public Properties prefs = new Properties();
    public StringBuffer crushAuth = new StringBuffer();
    Properties scanners = new Properties();
    Properties oldScanners = new Properties();
    public static boolean updateNotified = false;
    public String base_path = "./";
    Vector growls = new Vector();
    public String status = "";

    public CrushSyncDaemon(String base_path) {
        this.base_path = base_path;
        thisObj = this;
        System.setProperty("java.awt.headless", "true");
        System.setProperty("java.net.useSystemProxies", "true");
        System.out.println("Home Folder:" + base_path);
        Common.trustEverything();
        new SnapshotFile(base_path).mkdirs();
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    long last_print = System.currentTimeMillis();
                    boolean stack_dumped = false;
                    int stacks_dumped = 0;
                    boolean first_run = true;
                    while (true) {
                        long ram_max = Runtime.getRuntime().maxMemory();
                        long ram_free = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory();
                        long ram_used = ram_max - ram_free;
                        boolean print_memory = false;
                        if (ram_free < 0x100000L) {
                            print_memory = true;
                            if (!stack_dumped && stacks_dumped < 5) {
                                Common.dumpStack("CrushSync 3.12.17");
                                ++stacks_dumped;
                            }
                            stack_dumped = true;
                        } else if (ram_free < 0xA00000L) {
                            stack_dumped = false;
                            print_memory = true;
                        } else if (System.currentTimeMillis() - last_print > 600000L) {
                            print_memory = true;
                            last_print = System.currentTimeMillis();
                        }
                        if (print_memory || first_run) {
                            System.out.println("Memory stats: ram_max=" + Common.format_bytes_short(ram_max) + ", ram_free=" + Common.format_bytes_short(ram_free) + ", ram_used=" + Common.format_bytes_short(ram_used));
                        }
                        if (ram_free > 0x2000000L) {
                            stack_dumped = false;
                            stacks_dumped = 0;
                        }
                        first_run = false;
                        try {
                            Thread.sleep(1000L);
                            continue;
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                            continue;
                        }
                        break;
                    }
                }
            });
            Thread.sleep(5000L);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public String doStartup() {
        this.doLoad();
        String s = this.checkPrefsConfig();
        if (!s.equals("")) {
            return s;
        }
        this.startSyncs();
        return "";
    }

    public void growl(String s) {
        this.growls.addElement(s);
    }

    public String checkPrefsConfig() {
        if (this.prefs.getProperty("syncUrl", "").equals("")) {
            return "Sync URL missing.";
        }
        if (this.prefs.getProperty("admin_password", "").equals("")) {
            return "Sync agent control password missing.";
        }
        if (this.prefs.getProperty("syncUsername", "").equals("") || this.prefs.getProperty("syncPassword", "").equals("")) {
            return "Server username or password missing.";
        }
        this.crushAuth.setLength(0);
        try {
            String s = this.checkAuth();
            if (!s.equals("")) {
                return s;
            }
        }
        catch (Exception e) {
            CrushSyncDaemon.msg(e);
            return "LOGIN ERROR:" + e;
        }
        return "";
    }

    public void doLoad() {
        block25: {
            InputStream in = null;
            FileOutputStream out = null;
            try {
                try {
                    if (!new SnapshotFile(String.valueOf(this.base_path) + "prefs.XML").exists()) {
                        this.prefs = new Properties();
                        this.prefs.put("admin_password", "");
                        this.prefs.put("syncUrl", "");
                        this.prefs.put("syncUsername", "");
                        this.prefs.put("syncPassword", "");
                        this.prefs.put("syncs", new Vector());
                        this.prefs.put("prefs_version", "2");
                        this.savePrefs(this.prefs);
                    }
                    this.prefs = this.loadPrefs();
                    if (!this.prefs.containsKey("syncs")) {
                        this.prefs.put("syncs", new Vector());
                    }
                    this.prefs.put("clientid", this.prefs.getProperty("clientid", Common.makeBoundary(4)));
                    Tunnel2.setMaxRam(Integer.parseInt(this.prefs.getProperty("tunnel_ram_cache", "64")));
                }
                catch (IOException e) {
                    e.printStackTrace();
                    this.growl("Error:" + e.getMessage());
                    if (in != null) {
                        try {
                            in.close();
                        }
                        catch (Exception e2) {
                            Common.log("SYNC", 1, e2);
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        }
                        catch (Exception e3) {
                            Common.log("SYNC", 1, e3);
                        }
                    }
                    break block25;
                }
            }
            catch (Throwable throwable) {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (Exception e) {
                        Common.log("SYNC", 1, e);
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    }
                    catch (Exception e) {
                        Common.log("SYNC", 1, e);
                    }
                }
                throw throwable;
            }
            if (in != null) {
                try {
                    in.close();
                }
                catch (Exception e) {
                    Common.log("SYNC", 1, e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                }
                catch (Exception e) {
                    Common.log("SYNC", 1, e);
                }
            }
        }
        new Thread(new Runnable(){

            @Override
            public void run() {
                Thread.currentThread().setName("Disk logger.");
                while (true) {
                    if (CrushSyncScanner.logDisk.size() > 0) {
                        CrushSyncDaemon.this.append_log(CrushSyncScanner.logDisk.remove(0).toString());
                        continue;
                    }
                    try {
                        Thread.sleep(500L);
                    }
                    catch (Exception exception) {
                        continue;
                    }
                    break;
                }
            }
        }).start();
        new Thread(new Runnable(){

            /*
             * Unable to fully structure code
             */
            @Override
            public void run() {
                Thread.currentThread().setName("Growler.");
                block2: while (true) {
                    try {
                        Thread.sleep(30000L);
                    }
                    catch (Exception var1_2) {
                        // empty catch block
                    }
                    syncs = (Vector)CrushSyncDaemon.this.prefs.get("syncs");
                    if (!CrushSyncDaemon.this.prefs.getProperty("growl_sync_warning", "true").equals("true")) continue;
                    x = 0;
                    while (true) {
                        if (x < syncs.size()) ** break;
                        continue block2;
                        p = (Properties)syncs.elementAt(x);
                        if (!CrushSyncDaemon.this.scanners.containsKey(p.getProperty("syncPath").toUpperCase()) && !p.getProperty("remote_last_rid", "-1").equals("-1")) {
                            temp_path = p.getProperty("syncPath");
                            CrushSyncDaemon.this.growl("Sync engine is not running!\r\nChanges are not being monitored!\r\n" + temp_path);
                        }
                        ++x;
                    }
                    break;
                }
            }
        }).start();
        new Thread(new Runnable(){

            @Override
            public void run() {
                Thread.currentThread().setName("serverSettingsRetriever");
                int loops = 30;
                int idleCount = 120;
                while (true) {
                    block14: {
                        try {
                            if (loops++ < 30 && idleCount++ >= 120) break block14;
                            loops = 0;
                            Vector v = CrushSyncDaemon.this.getCrushSyncPrefs();
                            if (v.size() > 0) {
                                idleCount = 0;
                                CrushSyncDaemon.msg("Done getting settings:" + v.size());
                            }
                            while (v.size() > 0) {
                                CrushSyncDaemon.this.processSetting((Properties)v.remove(0));
                            }
                            try {
                                Enumeration<Object> keys = CrushSyncDaemon.this.scanners.keys();
                                while (keys.hasMoreElements()) {
                                    String key = keys.nextElement().toString();
                                    CrushSyncScanner sc = (CrushSyncScanner)CrushSyncDaemon.this.scanners.get(key);
                                    if (sc == null) continue;
                                    CrushSyncDaemon.this.status = String.valueOf(CrushSyncDaemon.this.prefs.getProperty("syncUsername")) + ":" + sc.statusInfo.getProperty("syncStatus", "Not Running");
                                }
                            }
                            catch (Exception keys) {}
                        }
                        catch (SocketTimeoutException v) {
                        }
                        catch (Exception e) {
                            CrushSyncDaemon.msg(e);
                            if (e.toString().indexOf("end of file") >= 0) break block14;
                            CrushSyncDaemon.this.crushAuth.setLength(0);
                        }
                    }
                    try {
                        Enumeration<Object> keys = CrushSyncDaemon.this.scanners.keys();
                        while (keys.hasMoreElements()) {
                            String key = keys.nextElement().toString();
                            CrushSyncScanner sc = (CrushSyncScanner)CrushSyncDaemon.this.scanners.get(key);
                            if (sc == null || sc.prefs.getProperty("saveNow", "").equals(sc.saveNow)) continue;
                            sc.saveNow = sc.prefs.getProperty("saveNow", "");
                            CrushSyncDaemon.this.savePrefs(CrushSyncDaemon.this.prefs);
                        }
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    try {
                        Thread.sleep(1000L);
                    }
                    catch (Exception exception) {
                        continue;
                    }
                    break;
                }
            }
        }).start();
    }

    public String checkAuth() throws Exception {
        block4: {
            if (this.crushAuth.length() == 0) {
                GenericClient c = null;
                try {
                    c = Common.getClient(this.prefs.getProperty("syncUrl"), "", null);
                    c.login(this.prefs.getProperty("syncUsername"), Common.encryptDecrypt(this.prefs.getProperty("syncPassword"), false), this.prefs.getProperty("clientid"));
                    c.close();
                    this.crushAuth.setLength(0);
                    this.crushAuth.append(c.getConfig("crushAuth"));
                }
                catch (Exception e) {
                    if (c != null) {
                        c.close();
                    }
                    e.printStackTrace();
                    if (("" + e).indexOf("failure") < 0) break block4;
                    this.crushAuth.setLength(0);
                    this.growl("Invalid password!  Login again with correct password.");
                    return "Login again with correct password.";
                }
            }
        }
        return "";
    }

    public Vector getCrushSyncPrefs() throws Exception {
        this.checkAuth();
        HttpURLConnection urlc = null;
        try {
            URL u = new URL(this.prefs.getProperty("syncUrl"));
            Properties agentInfo = (Properties)this.prefs.clone();
            agentInfo.remove("syncs");
            agentInfo.remove("admin_password");
            agentInfo.remove("sync_password");
            agentInfo.remove("syncPassword");
            agentInfo.put("version", version);
            int x = 0;
            while (x < 3) {
                try {
                    urlc = (HttpURLConnection)u.openConnection();
                    urlc.setReadTimeout(20000);
                    urlc.setRequestMethod("POST");
                    urlc.setRequestProperty("Cookie", "CrushAuth=" + this.crushAuth.toString() + ";");
                    urlc.setUseCaches(false);
                    urlc.setDoOutput(true);
                    urlc.getOutputStream().write(("c2f=" + this.crushAuth.toString().substring(this.crushAuth.toString().length() - 4) + "&command=getCrushSyncPrefs").getBytes("UTF8"));
                    break;
                }
                catch (SocketException e) {
                    Thread.sleep(10000L);
                    ++x;
                }
            }
            Enumeration<Object> keys = agentInfo.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                urlc.getOutputStream().write(("&" + key + "=" + agentInfo.getProperty(key)).getBytes("UTF8"));
            }
            urlc.getResponseCode();
            ObjectInputStream ois = new ObjectInputStream(urlc.getInputStream());
            Vector v = (Vector)ois.readObject();
            ois.close();
            Vector vector = v;
            return vector;
        }
        finally {
            if (urlc != null) {
                urlc.disconnect();
            }
        }
    }

    public void processSetting(Properties p) throws Exception {
        Properties sync;
        int x;
        Vector syncs;
        Cloneable o = null;
        if (p.getProperty("UPDATE_REQUIRED", "false").equals("true") && !updateNotified) {
            updateNotified = true;
            final Properties pp = p;
            new Thread(new Runnable(){

                @Override
                public void run() {
                    CrushSyncDaemon.this.growl("An update is required for " + app_name + ".\r\nCurrent Version : " + CrushSyncDaemon.version + "\r\nMinimum Version : " + pp.getProperty("MIN_VERSION") + ".\r\n\r\nPlease restart app for automatic update.");
                }
            }).start();
            syncs = (Vector)this.prefs.get("syncs");
            x = 0;
            while (x < syncs.size()) {
                sync = (Properties)syncs.elementAt(x);
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        CrushSyncDaemon.this.stopSync(sync);
                    }
                }).start();
                Thread.sleep(1000L);
                ++x;
            }
        }
        if (p.getProperty("PASSWORD", "").equals("")) {
            return;
        }
        if (Common.encryptDecrypt(p.getProperty("PASSWORD"), true).equals(this.prefs.getProperty("admin_password", ""))) {
            if (p.containsKey("NEW_PASSWORD") && p.getProperty("PASSWORD_TARGET", "ADMIN").equalsIgnoreCase("ADMIN")) {
                this.prefs.put("admin_password", Common.encryptDecrypt(p.getProperty("NEW_PASSWORD"), true));
                this.savePrefs(this.prefs);
            } else if (p.containsKey("NEW_PASSWORD") && p.getProperty("PASSWORD_TARGET", "ADMIN").equalsIgnoreCase("USER") && System.getProperty("crushsync.allow_password_update", "false").equalsIgnoreCase("true")) {
                this.prefs.put("syncPassword", Common.encryptDecrypt(p.getProperty("NEW_PASSWORD"), true));
                this.savePrefs(this.prefs);
            }
            Properties p2 = (Properties)p.clone();
            p2.remove("PASSWORD");
            p2.remove("NEW_PASSWORD");
            CrushSyncDaemon.msg("" + p2);
            syncs = (Vector)this.prefs.get("syncs");
            o = syncs;
            if (p.getProperty("COMMAND", "").equalsIgnoreCase("NOOP")) {
                o = new Properties();
            } else if (p.getProperty("COMMAND", "").equalsIgnoreCase("GET_SYNCS")) {
                o = syncs;
            } else if (p.getProperty("COMMAND", "").equalsIgnoreCase("SET_SYNC")) {
                Properties sync_item2 = (Properties)p.get("OBJ");
                int i = Integer.parseInt(p.getProperty("INDEX", "-1"));
                if (p.getProperty("ACTION", "").equalsIgnoreCase("INSERT")) {
                    syncs.addElement(sync_item2);
                } else if (p.getProperty("ACTION", "").equalsIgnoreCase("UPDATE")) {
                    Properties sync_item1 = (Properties)syncs.elementAt(i);
                    sync_item1.putAll((Map<?, ?>)sync_item2);
                } else if (p.getProperty("ACTION", "").equalsIgnoreCase("DELETE")) {
                    syncs.removeElementAt(i);
                }
                this.savePrefs(this.prefs);
                o = syncs;
            } else if (p.getProperty("COMMAND", "").equalsIgnoreCase("SET_PREF")) {
                Object val = null;
                String key = p.getProperty("KEY");
                val = p.containsKey("OBJ") ? p.get("OBJ") : p.get("VAL");
                this.prefs.put(key, val);
                this.savePrefs(this.prefs);
                o = syncs;
            } else if (p.getProperty("COMMAND", "").equalsIgnoreCase("LIST_FOLDER")) {
                String path = p.getProperty("PATH");
                if (path.equals("/") && Common.machine_is_x()) {
                    path = "/Volumes/";
                }
                if (!new File(path).exists() && Common.machine_is_x()) {
                    path = "/Volumes" + path;
                }
                o = new Vector();
                FileClient fc = new FileClient("file:///", "", new Vector());
                Vector v = new Vector();
                fc.list(new VRL(path).getPath(), v);
                int x2 = 0;
                while (x2 < v.size()) {
                    Properties pp = (Properties)v.elementAt(x2);
                    pp.put("itemType", pp.getProperty("type").toUpperCase());
                    pp.put("href_path", String.valueOf(pp.getProperty("root_dir")) + pp.getProperty("name"));
                    pp.put("preview", "0");
                    pp.put("boot", String.valueOf(path.equals("/") && Common.machine_is_x() && !new File(path).getCanonicalPath().startsWith("/Volumes/")));
                    pp.put("privs", "(read)(view)");
                    pp.put("keywords", "");
                    pp.put("path", pp.getProperty("root_dir"));
                    if (pp.getProperty("type").equalsIgnoreCase("DIR")) {
                        ((Vector)o).addElement(pp);
                    }
                    ++x2;
                }
            } else if (p.getProperty("COMMAND", "").equalsIgnoreCase("START_SYNC")) {
                x = 0;
                while (x < syncs.size()) {
                    sync = (Properties)syncs.elementAt(x);
                    if (sync.getProperty("syncPath", "").equals(p.getProperty("PATH"))) {
                        new Thread(new Runnable(){

                            @Override
                            public void run() {
                                CrushSyncDaemon.this.startSync(sync);
                            }
                        }).start();
                        Thread.sleep(1000L);
                        o = this.getSyncStatus(sync);
                    }
                    ++x;
                }
            } else if (p.getProperty("COMMAND", "").equalsIgnoreCase("STOP_SYNC")) {
                x = 0;
                while (x < syncs.size()) {
                    sync = (Properties)syncs.elementAt(x);
                    if (sync.getProperty("syncPath", "").equals(p.getProperty("PATH"))) {
                        new Thread(new Runnable(){

                            @Override
                            public void run() {
                                CrushSyncDaemon.this.stopSync(sync);
                                try {
                                    Thread.sleep(2000L);
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                        }).start();
                        Thread.sleep(1000L);
                        o = this.getSyncStatus(sync);
                    }
                    ++x;
                }
            } else if (p.getProperty("COMMAND", "").equalsIgnoreCase("GET_STATUS")) {
                o = null;
                int i = Integer.parseInt(p.getProperty("INDEX", "-1"));
                o = this.getSyncStatus((Properties)syncs.elementAt(i));
                if (o == null) {
                    o = this.getSyncStatus(new Properties());
                }
            } else if (p.getProperty("COMMAND", "").equalsIgnoreCase("GET_LOG")) {
                int index;
                Vector v = new Vector();
                int index2 = index = Integer.parseInt(p.getProperty("INDEX", "0"));
                int x3 = 0;
                while (x3 < syncs.size()) {
                    Properties sync2 = (Properties)syncs.elementAt(x3);
                    if (sync2.getProperty("syncPath", "").equals(p.getProperty("PATH")) && (this.scanners.containsKey(p.getProperty("PATH").toUpperCase()) || this.oldScanners.containsKey(p.getProperty("PATH").toUpperCase()))) {
                        CrushSyncScanner sc = (CrushSyncScanner)this.scanners.get(sync2.getProperty("syncPath").toUpperCase());
                        if (sc == null) {
                            sc = (CrushSyncScanner)this.oldScanners.get(sync2.getProperty("syncPath").toUpperCase());
                        }
                        Vector v2 = sc.log;
                        int xx = index;
                        while (xx < v2.size()) {
                            v.addElement(v2.elementAt(xx));
                            ++xx;
                        }
                        while (v2.size() > 200) {
                            v2.removeElementAt(0);
                        }
                        index2 = v2.size();
                    }
                    ++x3;
                }
                o = new Properties();
                ((Properties)o).put("sync_index", String.valueOf(index2));
                ((Properties)o).put("sync_log", v);
            }
        } else {
            o = new Properties();
            ((Properties)o).put("agentInfo", "BAD_PASS");
        }
        this.checkAuth();
        this.sendResult(o, p.getProperty("RESULTID", "0"));
    }

    public void sendResult(Object o, String resultid) throws Exception {
        HttpURLConnection urlc = null;
        try {
            URL u = new URL(this.prefs.getProperty("syncUrl"));
            urlc = (HttpURLConnection)u.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Cookie", "CrushAuth=" + this.crushAuth.toString() + ";");
            urlc.setUseCaches(false);
            urlc.setDoOutput(true);
            String result = Base64.encodeBytes(Common.getXMLString(o, "data", false).getBytes("UTF8")).trim();
            urlc.getOutputStream().write(("c2f=" + this.crushAuth.toString().substring(this.crushAuth.toString().length() - 4) + "&command=syncCommandResult&clientid=" + this.prefs.getProperty("clientid") + "&resultid=" + resultid + "&result=" + result).getBytes("UTF8"));
            urlc.getResponseCode();
            return;
        }
        finally {
            if (urlc != null) {
                urlc.disconnect();
            }
        }
    }

    public void startSyncs() {
        Vector v = (Vector)this.prefs.get("syncs");
        int x = 0;
        while (x < v.size()) {
            final Properties p = (Properties)v.elementAt(x);
            p.remove("clientid");
            if (p.getProperty("syncAutoStart", "false").equals("true")) {
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        CrushSyncDaemon.this.startSync(p);
                    }
                }).start();
            }
            ++x;
        }
    }

    public void startSync(Properties p) {
        block21: {
            if (!this.scanners.containsKey(p.getProperty("syncPath").toUpperCase())) {
                this.oldScanners.remove(p.getProperty("syncPath").toUpperCase());
                CrushSyncScanner sc = null;
                try {
                    sc = new CrushSyncScanner(p, this);
                    this.scanners.put(p.getProperty("syncPath").toUpperCase(), sc);
                    sc.setAuth(this.crushAuth);
                    if (!p.getProperty("scheduled_times", "").equals("")) {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        while (!sc.stop) {
                            String current = sdf.format(new Date());
                            String[] times = p.getProperty("scheduled_times", "").split(",");
                            boolean ok = false;
                            int x = 0;
                            while (x < times.length) {
                                String time = times[x].trim();
                                if (time.indexOf(":") == 1) {
                                    time = "0" + time;
                                }
                                if (time.equals(current)) {
                                    ok = true;
                                }
                                ++x;
                            }
                            if (ok) {
                                sc.scan(null, null, null, null, true, true, true);
                                sc.statusInfo.put("syncStatus", "Finished: Waiting for next scheduled time. (" + p.getProperty("scheduled_times", "") + ")");
                            } else {
                                sc.statusInfo.put("syncStatus", "Active: Waiting for scheduled time. (" + p.getProperty("scheduled_times", "") + ")");
                            }
                            while (current.equals(sdf.format(new Date())) && !sc.stop) {
                                Thread.sleep(200L);
                            }
                        }
                        break block21;
                    }
                    boolean ok = false;
                    while (!ok) {
                        block22: {
                            try {
                                ok = sc.scan(null, null, null, null, true, true, true);
                            }
                            catch (Exception e) {
                                if (!this.prefs.getProperty("keep_trying_startup", "true").equals("true")) {
                                    throw e;
                                }
                                Properties syncStatus = sc.statusInfo;
                                Properties downloadStatus = (Properties)syncStatus.get("downloadStatus");
                                Properties uploadStatus = (Properties)syncStatus.get("uploadStatus");
                                if (uploadStatus != null) {
                                    uploadStatus.put("uploadStatus", "Retrying sync startup.");
                                }
                                if (downloadStatus == null) break block22;
                                downloadStatus.put("downloadStatus", "Retrying sync startup.");
                            }
                        }
                        if (ok) continue;
                        int x = 0;
                        while (x < 60 && !sc.stop) {
                            Thread.sleep(1000L);
                            ++x;
                        }
                    }
                    if (ok) {
                        if (!sc.stop) {
                            sc.startJournalPublisher();
                        }
                        if (sc.watcherWrapper != null && !sc.stop) {
                            sc.watcherWrapper.startLiveMonitor();
                        }
                    } else {
                        this.stopSync(p);
                    }
                }
                catch (Exception e) {
                    CrushSyncDaemon.msg(e);
                    this.stopSync(p);
                }
            }
        }
    }

    public void stopSync(Properties p) {
        if (this.scanners.containsKey(p.getProperty("syncPath").toUpperCase())) {
            CrushSyncScanner sc = (CrushSyncScanner)this.scanners.remove(p.getProperty("syncPath").toUpperCase());
            this.oldScanners.put(p.getProperty("syncPath").toUpperCase(), sc);
            try {
                sc.stop();
            }
            catch (Exception e) {
                CrushSyncDaemon.msg(e);
            }
        }
    }

    public Properties getSyncStatus(Properties p) {
        if (this.scanners.containsKey(p.getProperty("syncPath", "").toUpperCase())) {
            CrushSyncScanner sc = (CrushSyncScanner)this.scanners.get(p.getProperty("syncPath").toUpperCase());
            Properties syncStatus = sc.statusInfo;
            syncStatus.remove("downloadStatus");
            syncStatus.remove("uploadStatus");
            if (!sc.downloadStatus.getProperty("totalItems", "0").equals("0")) {
                syncStatus.put("downloadStatus", sc.downloadStatus);
            }
            if (!sc.uploadStatus.getProperty("totalItems", "0").equals("0")) {
                syncStatus.put("uploadStatus", sc.uploadStatus);
            }
            return syncStatus;
        }
        Properties statusInfo = new Properties();
        statusInfo.put("syncStatus", "Not running.");
        return statusInfo;
    }

    public static void msg(String s) {
        CrushSyncScanner.logDisk.add(new Date() + ":" + s);
    }

    public static void msg(Exception e) {
        StackTraceElement[] ste = e.getStackTrace();
        CrushSyncDaemon.msg(String.valueOf(Thread.currentThread().getName()) + ":" + e.toString());
        int x = 0;
        while (x < ste.length) {
            CrushSyncDaemon.msg(String.valueOf(ste[x].getClassName()) + "." + ste[x].getMethodName() + ":" + ste[x].getLineNumber());
            ++x;
        }
    }

    public synchronized void append_log(String s) {
        File f;
        block17: {
            RandomAccessFile logFile = null;
            try {
                try {
                    logFile = new RandomAccessFile(String.valueOf(this.base_path) + app_name + ".log", "rw");
                    logFile.seek(logFile.length());
                    logFile.write((String.valueOf(s) + "\r\n").getBytes("UTF8"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                    if (logFile != null) {
                        try {
                            logFile.close();
                        }
                        catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                    break block17;
                }
            }
            catch (Throwable throwable) {
                if (logFile != null) {
                    try {
                        logFile.close();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                throw throwable;
            }
            if (logFile != null) {
                try {
                    logFile.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if ((f = new File(String.valueOf(this.base_path) + app_name + ".log")).length() > 0x100000L * Long.parseLong(this.prefs.getProperty("max_log_size", "10"))) {
            int maxCount = Integer.parseInt(this.prefs.getProperty("max_log_count", "10"));
            int x = maxCount - 1;
            while (x < 100) {
                new File(String.valueOf(this.base_path) + app_name + "_" + x + ".log").delete();
                ++x;
            }
            x = maxCount - 1;
            while (x > 0) {
                new File(String.valueOf(this.base_path) + app_name + "_" + (x - 1) + ".log").renameTo(new File(String.valueOf(this.base_path) + app_name + "_" + x + ".log"));
                --x;
            }
            f.renameTo(new File(String.valueOf(this.base_path) + app_name + "_0.log"));
        }
    }

    public String get_sync_folder() {
        Enumeration<Object> keys = this.scanners.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            CrushSyncScanner sc = (CrushSyncScanner)this.scanners.get(key);
            if (sc == null) continue;
            try {
                return sc.syncPath;
            }
            catch (Exception e) {
                e.printStackTrace();
                this.growl("Sync Folder:" + e.toString());
                return "Sync Folder:" + e.toString();
            }
        }
        return "";
    }

    public String sync_now() {
        int count = 0;
        Enumeration<Object> keys = this.scanners.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            CrushSyncScanner sc = (CrushSyncScanner)this.scanners.get(key);
            if (sc == null) continue;
            try {
                ++count;
                sc.scan(null, null, null, null, false, true, true);
            }
            catch (Exception e) {
                e.printStackTrace();
                this.growl("Sync Now:" + e.toString());
                return "Sync Now:" + e.toString();
            }
        }
        this.growl(String.valueOf(CrushSyncDaemon.l("Synching now:")) + count);
        return String.valueOf(CrushSyncDaemon.l("Synching now:")) + count;
    }

    public String stop_syncs() {
        try {
            Vector syncs = (Vector)this.prefs.get("syncs");
            int x = 0;
            while (x < syncs.size()) {
                final Properties sync = (Properties)syncs.elementAt(x);
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        CrushSyncDaemon.this.stopSync(sync);
                    }
                }).start();
                Thread.sleep(1000L);
                ++x;
            }
            this.growl(CrushSyncDaemon.l("Syncs stopped"));
            return CrushSyncDaemon.l("Syncs stopped.");
        }
        catch (Exception ee) {
            ee.printStackTrace();
            this.growl(String.valueOf(CrushSyncDaemon.l("Stop Syncs failed:")) + ee.toString());
            return String.valueOf(CrushSyncDaemon.l("Stop Syncs failed:")) + ee.toString();
        }
    }

    public String start_syncs() {
        try {
            Vector syncs = (Vector)this.prefs.get("syncs");
            int x = 0;
            while (x < syncs.size()) {
                final Properties sync = (Properties)syncs.elementAt(x);
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        CrushSyncDaemon.this.startSync(sync);
                    }
                }).start();
                Thread.sleep(1000L);
                ++x;
            }
            this.growl(CrushSyncDaemon.l("Syncs Started"));
            return CrushSyncDaemon.l("Syncs started.");
        }
        catch (Exception ee) {
            ee.printStackTrace();
            this.growl(String.valueOf(CrushSyncDaemon.l("Start Syncs failed:")) + ee.toString());
            return String.valueOf(CrushSyncDaemon.l("Start Syncs failed:")) + ee.toString();
        }
    }

    public static String l(String key) {
        String s = System.getProperties().getProperty("crushsync.localization." + key, key);
        s = Common.replace_str(s, "%appname%", app_name);
        return s;
    }

    public synchronized void savePrefs(Properties p) throws IOException {
        if (!p.getProperty("syncUrl", "").endsWith("/") && !p.getProperty("syncUrl", "").equals("")) {
            p.put("syncUrl", String.valueOf(p.getProperty("syncUrl", "")) + "/");
        }
        if (!p.containsKey("clientid")) {
            p.put("clientid", Common.makeBoundary(4));
        }
        Common.writeXMLObject(String.valueOf(this.base_path) + "prefs.XML.new", p, "CrushSync");
        new File(String.valueOf(this.base_path) + "prefs.XML.old").delete();
        new File(String.valueOf(this.base_path) + "prefs.XML").renameTo(new File(String.valueOf(this.base_path) + "prefs.XML.old"));
        if (new File(String.valueOf(this.base_path) + "prefs.XML.new").renameTo(new File(String.valueOf(this.base_path) + "prefs.XML"))) {
            new File(String.valueOf(this.base_path) + "prefs.XML.old").delete();
        } else {
            new File(String.valueOf(this.base_path) + "prefs.XML.old").renameTo(new File(String.valueOf(this.base_path) + "prefs.XML.save"));
        }
    }

    public synchronized Properties loadPrefs() throws IOException {
        Properties p = new Properties();
        p.put("syncs", new Vector());
        if (new SnapshotFile(String.valueOf(this.base_path) + "prefs.XML").exists()) {
            p = (Properties)Common.readXMLObject(String.valueOf(this.base_path) + "prefs.XML");
        }
        if (!p.getProperty("syncUrl", "").endsWith("/") && !p.getProperty("syncUrl", "").equals("")) {
            p.put("syncUrl", String.valueOf(p.getProperty("syncUrl", "")) + "/");
        }
        if (Integer.parseInt(p.getProperty("pref_version", "1")) < 2) {
            Vector syncs = (Vector)p.get("syncs");
            if (syncs != null && syncs.size() > 0) {
                int x = 0;
                while (x < syncs.size()) {
                    Properties sync = (Properties)syncs.elementAt(x);
                    p.put("syncUrl", sync.get("syncUrl"));
                    p.put("syncUsername", sync.get("syncUsername"));
                    p.put("syncPassword", sync.get("syncPassword"));
                    ++x;
                }
            }
            p.put("pref_version", "2");
            this.savePrefs(p);
        }
        return p;
    }
}

