/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.GenericClient;
import com.crushftp.client.HTTPBufferedClient;
import com.crushftp.client.VRL;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.net.SocketException;
import java.net.URI;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

public class CrushDrive
extends Thread {
    public static String version = "1.2.8";
    static Vector logDisk = new Vector();
    Properties prefs = null;
    boolean connected = false;
    static boolean headless = System.getProperty("java.awt.headless", "false").equals("true");
    protected static String truststore_path = "";
    public static String base_path = "./";
    public static String app_name = "CrushDrive";
    public static String default_url = "https://www.crushftp.com/";
    public static String mounted_letter = "";
    boolean started_logger = false;
    Object fuse = null;

    public CrushDrive() {
        Runtime.getRuntime().addShutdownHook(this);
    }

    @Override
    public void run() {
        if (this.connected) {
            this.doDisconnect();
        }
    }

    public static void main(String[] args) {
        System.setProperty("java.net.useSystemProxies", "true");
        Common.trustEverything();
        CrushDrive d = new CrushDrive();
        System.out.println("********************************************************************");
        System.out.println(String.valueOf(app_name) + " " + version + " started.");
        System.out.println("********************************************************************");
        CrushDrive.msg("********************************************************************");
        CrushDrive.msg(String.valueOf(app_name) + " " + version + " started.");
        CrushDrive.msg("********************************************************************");
        if (args != null && args.length > 0) {
            default_url = args[0];
        }
        d.doLoad();
        try {
            d.doConnect();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doLoad() {
        System.getProperties().put("com.crushftp.client.crushdrive.log", logDisk);
        try {
            if (!new File(String.valueOf(base_path) + "prefs.XML").exists()) {
                this.prefs = new Properties();
                this.prefs.put("prefs_version", "1");
                this.prefs.put("base_url", default_url);
                CrushDrive.savePrefs(this.prefs);
            }
            this.prefs = CrushDrive.loadPrefs();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (!this.started_logger) {
            this.started_logger = true;
            new Thread(new Runnable(){

                @Override
                public void run() {
                    Thread.currentThread().setName("Disk logger.");
                    while (true) {
                        CrushDrive.this.flushLog();
                        try {
                            Thread.sleep(1000L);
                        }
                        catch (Exception exception) {
                        }
                    }
                }
            }).start();
        }
    }

    public synchronized void flushLog() {
        while (logDisk.size() > 0) {
            try {
                this.append_log(new Date() + "|" + logDisk.remove(0).toString());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void savePrefs(Properties p) throws IOException {
        if (System.getProperty("crushdrive.writeprefs", "true").equals("true")) {
            Common.writeXMLObject(String.valueOf(base_path) + "prefs.XML.new", p, "CrushDrive");
            new File(String.valueOf(base_path) + "prefs.XML.old").delete();
            new File(String.valueOf(base_path) + "prefs.XML").renameTo(new File(String.valueOf(base_path) + "prefs.XML.old"));
            if (new File(String.valueOf(base_path) + "prefs.XML.new").renameTo(new File(String.valueOf(base_path) + "prefs.XML"))) {
                new File(String.valueOf(base_path) + "prefs.XML.old").delete();
            } else {
                new File(String.valueOf(base_path) + "prefs.XML.old").renameTo(new File(String.valueOf(base_path) + "prefs.XML.save"));
            }
        }
    }

    public void doConnect() {
        try {
            CrushDrive.msg(this.doConnect(this.SG("base_url"), this.SG("drive_username", "").trim(), Common.encryptDecrypt(this.SG("drive_password", "").trim(), false), this.SG("drive_letter", "").trim(), this.SG("buffered", "false").equals("true")));
        }
        catch (Exception e) {
            CrushDrive.msg(e);
        }
    }

    public String doConnect(String url, final String username, final String password, final String letter, final boolean buffered) {
        CrushDrive.msg("Attempting mount of drive " + url + " for " + letter + ".");
        final GenericClient c = Common.getClient(url, "", null);
        try {
            if (username.equals("")) {
                throw new Exception(CrushDrive.l("Username is required."));
            }
            if (password.equals("")) {
                throw new Exception(CrushDrive.l("Password is required."));
            }
            if (letter.trim().equals("") && !headless) {
                throw new Exception(CrushDrive.l("Drive letter is required."));
            }
            try {
                truststore_path = "";
                if (new File(String.valueOf(base_path) + "clientcert.pfx").exists()) {
                    truststore_path = String.valueOf(base_path) + "clientcert.pfx";
                } else if (new File(String.valueOf(base_path) + "clientcert.jks").exists()) {
                    truststore_path = String.valueOf(base_path) + "clientcert.jks";
                }
                CrushDrive.setupTrustStore(c);
                c.login(username, password, "CrushDrive");
            }
            catch (Exception e) {
                if (e.getMessage().indexOf("failure") >= 0) {
                    throw new Exception(CrushDrive.l("Invalid username or password."));
                }
                throw e;
            }
        }
        catch (Exception ee) {
            ee.printStackTrace();
            CrushDrive.msg(ee);
            try {
                if (c != null) {
                    c.logout();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            return String.valueOf(ee.getMessage());
        }
        final Properties status = new Properties();
        HTTPBufferedClient.mem.clear();
        new Thread(new Runnable(){

            /*
             * Unable to fully structure code
             */
            @Override
            public void run() {
                try {
                    if (Common.machine_is_windows()) {
                        Common.exec(("net;use;/delete;" + letter).split(";"));
                        CrushDrive.this.doFuse(String.valueOf(letter) + ":\\", password, buffered);
                        Thread.sleep(5000L);
                        if (CrushDrive.this.SG("drive_auto_open", "true").equals("true") && !CrushDrive.headless) {
                            Desktop.getDesktop().open(new File(String.valueOf(letter) + ":"));
                        }
                    } else if (Common.machine_is_x()) {
                        CrushDrive.this.doFuse(letter, password, buffered);
                        if (CrushDrive.this.SG("drive_auto_open", "true").equals("true") && !CrushDrive.headless) {
                            Desktop.getDesktop().open(new File("/tmp/" + letter));
                        }
                    }
                    CrushDrive.mounted_letter = letter;
                    Thread.sleep(1000L);
                    CrushDrive.this.connected = true;
                    CrushDrive.msg("Mounted successfully.");
                    status.put("status", "");
                    intervals = 0;
                    while (CrushDrive.this.connected) {
                        Thread.sleep(1000L);
                        if (++intervals <= 300) continue;
                        intervals = 0;
                        try {
                            c.list("/", new Vector<E>());
                            continue;
                        }
                        catch (SocketException e) {
                            ** while (CrushDrive.this.connected)
                        }
lbl-1000:
                        // 1 sources

                        {
                            try {
                                Thread.sleep(10000L);
                                if (!CrushDrive.this.connected) continue;
                                c.login(username, password, "CrushDrive");
                                break;
                            }
                            catch (SocketException ee) {
                                ee.printStackTrace();
                                CrushDrive.msg(ee);
                            }
                            continue;
                        }
lbl41:
                        // 2 sources

                        c.list("/", new Vector<E>());
                    }
                }
                catch (Exception ee) {
                    ee.printStackTrace();
                    if (!CrushDrive.headless) {
                        Common.activateFront();
                    }
                    error = "";
                    if (Common.machine_is_mac()) {
                        error = "Is macFUSE installed?\r\n<br/><a href='https://github.com/osxfuse/osxfuse/releases'>https://osxfuse.github.io/</a><br/>\r\n" + ee.getMessage();
                    }
                    error = Common.machine_is_windows() != false ? "Is winFSP installed?\r\n<a href='http://www.secfs.net/winfsp/rel/'>http://www.secfs.net/winfsp/</a>\r\n" + ee.getMessage() : "Is FUSE installed?\r\n" + ee.getMessage();
                    CrushDrive.msg(error);
                    status.put("status", error);
                    CrushDrive.msg(ee);
                }
            }
        }).start();
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 30000L) {
            if (status.containsKey("status")) {
                return status.getProperty("status");
            }
            try {
                Thread.sleep(100L);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
        return "Timeout while trying to connect...?";
    }

    public void doFuse(String mount_point, String pass, boolean buffered) throws Exception {
        StringBuffer status;
        block5: {
            status = new StringBuffer();
            VRL vrl1 = new VRL(this.SG("base_url"));
            VRL vrl2 = new VRL(String.valueOf(vrl1.getProtocol()) + "://" + VRL.vrlEncode(this.SG("drive_username")) + ":" + VRL.vrlEncode(pass) + "@" + vrl1.getHost() + ":" + vrl1.getPort() + vrl1.getPath());
            Class<?> c = Class.forName("com.crushftp.client.CrushFuseJNR");
            Constructor<?> cons = c.getConstructor(Boolean.TYPE, String.class, String.class, Boolean.TYPE);
            try {
                if (this.fuse == null) {
                    this.fuse = cons.newInstance(new File(String.valueOf(base_path) + "debug").exists(), "" + vrl2, mount_point, buffered);
                } else {
                    this.fuse.getClass().getMethod("umount", new Class[0]).invoke(this.fuse, new Object[0]);
                    this.fuse.getClass().getMethod("init_mount", Boolean.TYPE, String.class, String.class, Boolean.TYPE).invoke(this.fuse, new File(String.valueOf(base_path) + "debug").exists(), "" + vrl2, mount_point, buffered);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                CrushDrive.msg(e);
                if (e.getMessage() != null) break block5;
                throw new Exception("FUSE failed to load!");
            }
        }
        Thread mount = new Thread(new Runnable(){

            @Override
            public void run() {
                try {
                    CrushDrive.this.fuse.getClass().getMethod("mount", new Class[0]).invoke(CrushDrive.this.fuse, new Object[0]);
                    CrushDrive.this.fuse.getClass().getMethod("umount", new Class[0]).invoke(CrushDrive.this.fuse, new Object[0]);
                }
                catch (Exception e) {
                    status.append("" + e);
                    CrushDrive.msg(e);
                }
            }
        });
        mount.start();
        Thread.sleep(500L);
        if (status.length() > 0) {
            throw new Exception(status.toString());
        }
    }

    public String doDisconnect() {
        CrushDrive.msg("Attempting disconnect of drive.");
        try {
            HTTPBufferedClient.last_action = 0L;
            long start_time = System.currentTimeMillis();
            while (HTTPBufferedClient.isBusy()) {
                Thread.sleep(1000L);
                if (System.currentTimeMillis() - start_time <= 60000L) continue;
                throw new Exception("Not all changes were successfully synchronized to the server!!!\r\n" + HTTPBufferedClient.unpublished_changes);
            }
            if (this.fuse != null) {
                this.fuse.getClass().getMethod("umount", new Class[0]).invoke(this.fuse, new Object[0]);
            }
            if (Common.machine_is_windows()) {
                String results = Common.exec(("net;use;/del;/y;" + mounted_letter + ":").split(";"));
                CrushDrive.msg("Un-map results:" + results);
                if (results.toLowerCase().indexOf("error") >= 0) {
                    throw new Exception(results);
                }
            } else if (Common.machine_is_x()) {
                String results = Common.exec(("diskutil;unmountDisk;force;/tmp/" + mounted_letter).split(";"));
                CrushDrive.msg("Un-map results:" + results);
                if (results.toLowerCase().indexOf("error") >= 0) {
                    throw new Exception(results);
                }
            } else {
                CrushDrive.msg("You must manually unmount the drive:" + mounted_letter);
            }
            CrushDrive.msg("Disconencted drive successfully.");
            this.connected = false;
        }
        catch (Exception ee) {
            CrushDrive.msg(ee);
            return "" + ee;
        }
        return "Disconnected";
    }

    public static void doSupportEmail() throws Exception {
        try {
            Vector<Properties> zipFiles = new Vector<Properties>();
            Properties item = new Properties();
            item.put("url", new File(new File(String.valueOf(base_path) + app_name + ".log").getCanonicalPath()).toURI().toURL().toExternalForm());
            zipFiles.addElement(item);
            Common.zip(String.valueOf(new File(base_path).getCanonicalPath()) + "/", zipFiles, new File(String.valueOf(base_path) + CrushDrive.l("drive.zip")).getCanonicalPath());
            if (!headless) {
                Desktop.getDesktop().mail(new URI("mailto:?ignore=false&subject=" + Common.replace_str(CrushDrive.l("Drive Logs"), " ", "%20") + "&body=" + Common.replace_str(CrushDrive.l("Please attach drive.zip file."), " ", "%20")));
            }
            if (!headless) {
                Desktop.getDesktop().open(new File(base_path));
            }
        }
        catch (Exception ee) {
            ee.printStackTrace();
            CrushDrive.msg(ee);
        }
    }

    public String SG(String key) {
        return this.prefs.getProperty(key);
    }

    public String SG(String key, String default_val) {
        return this.prefs.getProperty(key, default_val);
    }

    public static synchronized Properties loadPrefs() throws IOException {
        Properties p = new Properties();
        if (new File(String.valueOf(base_path) + "prefs.XML").exists()) {
            p = (Properties)Common.readXMLObject(String.valueOf(base_path) + "prefs.XML");
        }
        return p;
    }

    public static void setupTrustStore(GenericClient c) {
        if (!truststore_path.equals("")) {
            c.setConfig("truststore_path", truststore_path);
            c.setConfig("keystore_path", truststore_path);
            c.setConfig("keystore_pass", "");
            c.setConfig("cert_pass", "");
        }
    }

    public String getLetters() {
        if (Common.machine_is_x()) {
            return System.getProperty("crushdrive.share_name", String.valueOf(app_name) + "Home");
        }
        String s = ",";
        int x = 69;
        while (x <= 90) {
            if (!new File(String.valueOf((char)x) + ":").exists()) {
                s = String.valueOf(s) + (char)x + ",";
            }
            ++x;
        }
        if (s.endsWith(",")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public static String l(String key) {
        key = Common.replace_str(key, "%appname%", app_name);
        String s = System.getProperty("crushdrive.localization." + key, key);
        s = Common.replace_str(s, "%appname%", app_name);
        return s;
    }

    public static void msg(String s) {
        logDisk.addElement("CrushDrive:" + s);
    }

    public static void msg(Exception e) {
        StackTraceElement[] ste = e.getStackTrace();
        CrushDrive.msg(String.valueOf(Thread.currentThread().getName()) + ":" + e.toString());
        int x = 0;
        while (x < ste.length) {
            CrushDrive.msg(String.valueOf(ste[x].getClassName()) + "." + ste[x].getMethodName() + ":" + ste[x].getLineNumber());
            ++x;
        }
    }

    public synchronized void append_log(String s) {
        if (System.getProperty("crushdrive.writelogconsole", "false").equals("true")) {
            System.out.println(s);
        }
        if (System.getProperty("crushdrive.writelog", "true").equals("true")) {
            File f;
            block19: {
                RandomAccessFile logFile = null;
                try {
                    try {
                        new File(base_path).mkdirs();
                        logFile = new RandomAccessFile(String.valueOf(base_path) + app_name + ".log", "rw");
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
                        break block19;
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
            if ((f = new File(String.valueOf(base_path) + app_name + ".log")).length() > 0x100000L * Long.parseLong(this.SG("max_log_size", "10"))) {
                int maxCount;
                int x = maxCount = Integer.parseInt(this.SG("max_log_count", "10"));
                while (x < 100) {
                    new File(String.valueOf(base_path) + app_name + "_" + x + ".log").delete();
                    ++x;
                }
                x = maxCount - 1;
                while (x > 0) {
                    new File(String.valueOf(base_path) + app_name + "_" + (x - 1) + ".log").renameTo(new File(String.valueOf(base_path) + app_name + "_" + x + ".log"));
                    --x;
                }
                f.renameTo(new File(String.valueOf(base_path) + app_name + "_0.log"));
            }
        }
    }
}

