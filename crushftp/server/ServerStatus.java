/*
 * Decompiled with CFR 0.152.
 */
package crushftp.server;

import com.crushftp.client.CommandBufferFlusher;
import com.crushftp.client.FileClient;
import com.crushftp.client.File_S;
import com.crushftp.client.File_U;
import com.crushftp.client.GenericClientMulti;
import com.crushftp.client.HTTPClient;
import com.crushftp.client.HeapDumper;
import com.crushftp.client.MemoryClient;
import com.crushftp.client.S3Client;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import com.crushftp.tunnel2.DVector;
import com.crushftp.tunnel2.Tunnel2;
import com.crushftp.tunnel3.StreamController;
import com.maverick.ssh.components.ComponentManager;
import crushftp.db.SearchHandler;
import crushftp.db.SearchTools;
import crushftp.db.StatTools;
import crushftp.gui.LOC;
import crushftp.handlers.AlertTools;
import crushftp.handlers.Common;
import crushftp.handlers.GeoIP;
import crushftp.handlers.IdleMonitor;
import crushftp.handlers.JobFilesHandler;
import crushftp.handlers.JobScheduler;
import crushftp.handlers.Log;
import crushftp.handlers.LoggingProvider;
import crushftp.handlers.PreferencesProvider;
import crushftp.handlers.PreviewWorker;
import crushftp.handlers.QuotaWorker;
import crushftp.handlers.SessionCrush;
import crushftp.handlers.SharedSession;
import crushftp.handlers.SharedSessionReplicated;
import crushftp.handlers.ShutdownHandler;
import crushftp.handlers.SyncTools;
import crushftp.handlers.UpdateHandler;
import crushftp.handlers.UpdateTimer;
import crushftp.handlers.UserTools;
import crushftp.handlers.log.LoggingProviderDisk;
import crushftp.license.Maverick;
import crushftp.reports8.ReportTools;
import crushftp.server.AdminControls;
import crushftp.server.Events;
import crushftp.server.LIST_handler;
import crushftp.server.ServerSessionTunnel3;
import crushftp.server.VFS;
import crushftp.server.daemon.DMZServerCommon;
import crushftp.server.daemon.GenericServer;
import crushftp.server.daemon.ServerBeat;
import crushftp.server.ssh.SSHDaemon;
import crushftp.user.SQLUsers;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Vector;
import javax.crypto.Cipher;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class ServerStatus {
    public static String sub_version_info_str = "_38";
    public static String version_info_str = "Version 10.5.5";
    public static ClassLoader clasLoader = null;
    public static ServerStatus thisObj = null;
    public static Properties server_settings = new Properties();
    public Properties server_info = new Properties();
    public Date server_start_time = new Date();
    public Vector commandBuffer = new Vector();
    public StatTools statTools = new StatTools();
    public SearchTools searchTools = new SearchTools();
    public Events events6 = new Events();
    CommandBufferFlusher commandBufferFlusher = null;
    UpdateHandler updateHandler = new UpdateHandler();
    ReportTools rt = new ReportTools();
    public long total_server_bytes_sent = 0L;
    public long total_server_bytes_received = 0L;
    public Thread logging_thread = null;
    public Thread extra_update_timer_thread = null;
    public Thread update_timer_thread = null;
    public Thread report_scheduler_thread = null;
    public Thread scheduler_thread = null;
    public Thread alerts_thread = null;
    public Thread new_version_thread = null;
    public Thread stats_saver_thread = null;
    public Object stats_saver_lock = new Object();
    public boolean vfs_url_cache_inprogress = false;
    public Thread hammer_timer_thread = null;
    public Thread hammer_timer_http_thread = null;
    public Thread ban_timer_thread = null;
    public Thread phammer_timer_thread = null;
    public Thread cban_timer_thread = null;
    public Thread discover_ip_timer_thread = null;
    public Thread log_rolling_thread = null;
    public Thread events_thread = null;
    public Thread monitor_folders_thread = null;
    public Thread monitor_folders_thread_instant = null;
    public Thread http_cleaner_thread = null;
    public Thread vfs_replication_pinger_thread = null;
    public Thread update_2_timer_thread = null;
    public Thread expireThread = null;
    public Thread jobs_resumer_thread = null;
    public Vector main_servers = new Vector();
    public String CRLF = "\r\n";
    public Properties dayofweek = new Properties();
    public Common common_code = new Common();
    public Properties default_settings = new Properties();
    public Vector previewWorkers = new Vector();
    public SimpleDateFormat expire_sdf = new SimpleDateFormat("MMddyy");
    public SimpleDateFormat logDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.US);
    public Object eventLock = new Object();
    public Object userListLock = new Object();
    public Object loginsLock = new Object();
    public boolean starting = true;
    ShutdownHandler shutdown = new ShutdownHandler();
    public LoggingProvider loggingProvider1 = null;
    public LoggingProvider loggingProvider2 = null;
    public PreferencesProvider prefsProvider = new PreferencesProvider();
    public static String hostname = "unknown";
    Properties in_progress_bans = new Properties();
    Object ban_lock = new Object();
    String last_logging_provider2 = "";
    public GeoIP geoip = new GeoIP();
    public static ServerSocket thread_dump_socket = null;

    public ServerStatus(boolean start_threads, Properties server_settings2) {
        System.getProperties().put("crushftp.worker.v9", System.getProperty("crushftp.worker.v9", "true"));
        System.getProperties().put("crushftp.version", "10");
        try {
            Thread t = new Thread(new Runnable(){

                @Override
                public void run() {
                    try {
                        hostname = InetAddress.getLocalHost().getCanonicalHostName();
                    }
                    catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
            t.join(5000L);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (clasLoader == null) {
            clasLoader = Thread.currentThread().getContextClassLoader();
        }
        try {
            Maverick.initLicense();
        }
        catch (Throwable e) {
            System.out.println("Maverick failed to initialize:" + e);
            e.printStackTrace();
        }
        thisObj = this;
        System.setProperty("mail.mime.ignoreunknownencoding", "true");
        this.server_info.put("user_list", new Vector());
        this.server_info.put("user_list_prop", new Properties());
        this.server_info.put("html5_transfers", new Properties());
        this.server_info.put("last_logins", new Vector());
        this.server_info.put("domain_cross_reference", new Properties());
        this.server_info.put("login_frequency", new Properties());
        this.server_info.put("login_attempt_frequency", new Properties());
        this.server_info.put("recent_user_list", new Vector());
        this.server_info.put("invalid_usernames", new Properties());
        this.server_info.put("running_tasks", new Vector());
        com.crushftp.client.Common.System2.put("running_tasks", this.server_info.get("running_tasks"));
        com.crushftp.client.Common.System2.put("alerts_queue", new Vector());
        this.server_info.put("machine_is_x_10_5_plus", String.valueOf(Common.machine_is_x_10_5_plus()));
        this.server_info.put("machine_is_x", String.valueOf(Common.machine_is_x()));
        this.server_info.put("machine_is_windows", String.valueOf(Common.machine_is_windows()));
        this.server_info.put("machine_is_linux", String.valueOf(Common.machine_is_linux()));
        this.server_info.put("machine_is_unix", String.valueOf(Common.machine_is_unix()));
        this.server_info.put("machine_is_solaris", String.valueOf(Common.machine_is_solaris()));
        this.server_info.put("os.name", System.getProperties().getProperty("os.name", "").toUpperCase());
        this.server_info.put("os.version", System.getProperties().getProperty("os.version", "").toUpperCase());
        this.server_info.put("update_available", "false");
        this.server_info.put("update_available_version", version_info_str);
        this.server_info.put("update_available_html", "");
        this.server_info.put("low_memory", "");
        this.server_info.put("connected_unique_ips", "0");
        this.server_info.put("replication_vfs_count", "0");
        this.server_info.put("ram_pending_bytes", "0");
        this.server_info.put("ram_pending_bytes_s3_download", "0");
        this.server_info.put("ram_pending_bytes_s3_upload", "0");
        this.server_info.put("ram_pending_bytes_multisegment_download", "0");
        this.server_info.put("ram_pending_bytes_multisegment_upload", "0");
        this.server_info.put("running_event_threads", "0");
        this.server_info.put("allow_logins", "true");
        this.server_info.put("update_when_idle", "false");
        this.server_info.put("restart_when_idle", "false");
        this.server_info.put("shutdown_when_idle", "false");
        Properties ip_data = new Properties();
        ip_data.put("type", "A");
        ip_data.put("start_ip", "0.0.0.0");
        ip_data.put("stop_ip", "255.255.255.255");
        Vector<Properties> ip_restrictions_temp = new Vector<Properties>();
        ip_restrictions_temp.addElement(ip_data);
        this.server_info.put("ip_restrictions_temp", ip_restrictions_temp);
        if (server_settings2 != null) {
            server_settings = server_settings2;
        }
        com.crushftp.client.Common.System2.put("persistent_variables", new Properties());
        com.crushftp.client.Common.loadPersistentVariables();
        try {
            ComponentManager.setPerContextAlgorithmPreferences(true);
        }
        catch (Throwable e) {
            System.out.println("Maverick failed to initialize:" + e);
        }
        this.init_setup(start_threads);
        this.server_info.put("successful_logins", ServerStatus.SG("successful_logins"));
        this.server_info.put("failed_logins", ServerStatus.SG("failed_logins"));
        if (ServerStatus.IG("phammer_attempts") == 5) {
            server_settings.put("phammer_attempts", "15");
        }
        ServerStatus.killUpdateFiles();
        Vector pref_server_items = (Vector)server_settings.get("server_list");
        int x = 0;
        while (x < pref_server_items.size()) {
            Properties the_server = (Properties)pref_server_items.elementAt(x);
            if (server_settings.containsKey("pasv_ports") && the_server.getProperty("serverType", "FTP").equalsIgnoreCase("FTP")) {
                the_server.put("pasv_ports", server_settings.getProperty("pasv_ports", "1025-65535"));
            }
            if (the_server.getProperty("server_ip", "").equals("")) {
                the_server.put("server_ip", "auto");
            }
            if (server_settings.containsKey("ftp_aware_router") && the_server.getProperty("serverType", "FTP").equalsIgnoreCase("FTP")) {
                the_server.put("ftp_aware_router", server_settings.getProperty("ftp_aware_router", "false"));
            }
            ++x;
        }
        server_settings.remove("server_ip");
        server_settings.remove("pasv_ports");
        server_settings.remove("ftp_aware_router");
        new SyncTools(server_settings);
        SharedSession.find("recent_user_list");
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        Thread.sleep(10000L);
                    }
                    catch (InterruptedException interruptedException) {
                        // empty catch block
                    }
                    ServerStatus.this.runAlerts("started", null);
                }
            });
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static boolean killUpdateFiles() {
        new File_S("./WebInterface/Reports/nohup.out").delete();
        new File_S(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/nohup.out").delete();
        ServerStatus.killJar("log4j-1.2.6.jar", "log4j.jar");
        ServerStatus.killJar("slf4j-api-1.7.2.jar", "slf4j-api.jar");
        ServerStatus.killJar("jgroups.jar", "jgroups.jar");
        ServerStatus.killJar("commons-compress-1.3.jar", "commons-compress.jar");
        ServerStatus.killJar("pgplib-2.5.jar", "pgplib.jar");
        ServerStatus.killJar("bcmail-jdk15on-147.jar", "bcmail-jdk15on.jar");
        ServerStatus.killJar("bcpg-jdk15on-147.jar", "bcpg-jdk15on.jar");
        ServerStatus.killJar("bcpkix-jdk15on-147.jar", "bcpkix-jdk15on.jar");
        ServerStatus.killJar("bcprov-jdk15on-147.jar", "bcprov-jdk15on.jar");
        ServerStatus.killJar("log4j.jar", "log4j.jar");
        ServerStatus.killJar("log4j.xml", "log4j.xml");
        ServerStatus.killJar("slf4j-log4j12-1.7.2.jar", "slf4j-log4j12-1.7.2.jar");
        ServerStatus.killJar("log4j-api.jar", "log4j-api.jar");
        ServerStatus.killJar("log4j-1.2.17.jar", "log4j-1.2.17.jar");
        ServerStatus.killJar("bcpg-jdk15on-lw.jar", "bcpg-jdk15on-lw.jar");
        ServerStatus.killJar("bcprov-jdk15on-lw.jar", "bcprov-jdk15on-lw.jar");
        String updateHome = "./";
        if (Common.OSXApp()) {
            updateHome = "../../../../";
        }
        if (!new File_S(String.valueOf(updateHome) + "update.bat").exists()) {
            new File_S(String.valueOf(updateHome) + "update.sh").delete();
            new File_S(String.valueOf(updateHome) + "update_list.txt").delete();
            Common.recurseDelete(String.valueOf(updateHome) + "UpdateTemp/", false);
            new File_S(String.valueOf(updateHome) + "CrushFTP4_PC_new.zip").delete();
            new File_S(String.valueOf(updateHome) + "CrushFTP4_OSX_new.zip").delete();
            new File_S(String.valueOf(updateHome) + "CrushFTP5_OSX_new.zip").delete();
            new File_S(String.valueOf(updateHome) + "CrushFTP5_PC_new.zip").delete();
            new File_S(String.valueOf(updateHome) + "CrushFTP6_OSX_new.zip").delete();
            new File_S(String.valueOf(updateHome) + "CrushFTP6_PC_new.zip").delete();
            new File_S(String.valueOf(updateHome) + "CrushFTP7_OSX_new.zip").delete();
            new File_S(String.valueOf(updateHome) + "CrushFTP7_PC_new.zip").delete();
            return true;
        }
        return false;
    }

    public static void killJar(String oldjar, String newjar) {
        if (new File_S("./plugins/lib/" + oldjar).exists() && new File_S("./plugins/lib/" + newjar).exists()) {
            new File_S("./plugins/lib/" + oldjar).delete();
        }
    }

    public void checkCrushExpiration() {
        if (!ServerStatus.SG("registration_name").equals("crush") && !ServerStatus.SG("registration_email").equals("ftp")) {
            try {
                boolean ok = this.common_code.register(ServerStatus.SG("registration_name"), ServerStatus.SG("registration_email"), ServerStatus.SG("registration_code"));
                String v = null;
                if (ok) {
                    v = this.common_code.getRegistrationAccess("V", ServerStatus.SG("registration_code"));
                }
                if (v != null && (v.equals("4") || v.equals("5") || v.equals("6") || v.equals("7"))) {
                    String msg = String.valueOf(System.getProperty("appname", "CrushFTP")) + " " + version_info_str + " will not work with a " + System.getProperty("appname", "CrushFTP") + " " + v + " license.";
                    Log.log("SERVER", 0, msg);
                    ServerStatus.put_in("max_max_users", "5");
                    ServerStatus.put_in("max_users", "5");
                } else if (v == null && this.expireThread == null) {
                    String msg = "Your license is expired.\r\n" + System.getProperty("appname", "CrushFTP") + " will automatically quit in 5 minutes.";
                    Log.log("SERVER", 0, msg);
                    this.expireThread = new Thread(new Runnable(){

                        @Override
                        public void run() {
                            try {
                                int x = 0;
                                while (x < 5) {
                                    Thread.sleep(60000L);
                                    ServerStatus.this.checkCrushExpiration();
                                    ++x;
                                }
                                String msg = "Your license is expired.\r\nYour 5 minutes is up. " + System.getProperty("appname", "CrushFTP") + " is quitting now.";
                                Log.log("SERVER", 0, msg);
                                ServerStatus.this.quit_server(true);
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                    });
                    this.expireThread.start();
                } else if (this.expireThread != null) {
                    this.expireThread.interrupt();
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                this.quit_server(true);
            }
        }
    }

    public void init_setup(boolean start_threads) {
        int x;
        Properties p;
        this.dayofweek.put("Sun", "1");
        this.dayofweek.put("Mon", "2");
        this.dayofweek.put("Tue", "3");
        this.dayofweek.put("Wed", "4");
        this.dayofweek.put("Thu", "5");
        this.dayofweek.put("Fri", "6");
        this.dayofweek.put("Sat", "7");
        ServerStatus.siPUT("logged_in_users", "0");
        ServerStatus.siPUT("concurrent_users", "0");
        ServerStatus.siPUT("version_info_str", version_info_str);
        ServerStatus.siPUT("sub_version_info_str", sub_version_info_str);
        ServerStatus.siPUT("about_info_str", String.valueOf(System.getProperty("appname", "CrushFTP")) + " " + ServerStatus.siSG("version_info_str") + ServerStatus.siSG("sub_version_info_str"));
        ServerStatus.siPUT("java_info", String.valueOf(System.getProperty("java.home")) + "/bin/java\r\n" + System.getProperty("java.version") + ", " + System.getProperty("sun.arch.data.model") + " bit\r\n" + System.getProperties().getProperty("os.name"));
        ServerStatus.siPUT("server_start_time", "" + this.server_start_time);
        ServerStatus.siPUT("current_download_speed", "0");
        ServerStatus.siPUT("current_upload_speed", "0");
        SimpleDateFormat weekday = new SimpleDateFormat("EEE", Locale.US);
        SimpleDateFormat MM = new SimpleDateFormat("MM", Locale.US);
        ServerStatus.siPUT("last_day_of_week", weekday.format(new Date()));
        ServerStatus.siPUT("last_month", MM.format(new Date()));
        ServerStatus.siPUT("hammer_history", "");
        ServerStatus.siPUT("hammer_history_http", "");
        ServerStatus.siPUT("incoming_transfers", new Vector());
        ServerStatus.siPUT("outgoing_transfers", new Vector());
        ServerStatus.siPUT("ram_max", String.valueOf(Runtime.getRuntime().maxMemory()));
        ServerStatus.siPUT("ram_free", String.valueOf(Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()));
        ServerStatus.siPUT("ram_used", String.valueOf(ServerStatus.siLG("ram_max") - ServerStatus.siLG("ram_free")));
        ServerStatus.siPUT("ram_used_percent", String.valueOf((int)((float)ServerStatus.siLG("ram_used") / (float)ServerStatus.siLG("ram_max") * 100.0f)));
        System.getProperties().put("crushftp.ram_used_percent", ServerStatus.siSG("ram_used_percent"));
        String cpu_usage = com.crushftp.client.Common.getCpuUsage();
        ServerStatus.siPUT("server_cpu", "0");
        ServerStatus.siPUT("os_cpu", "0");
        ServerStatus.siPUT("open_files", "0");
        ServerStatus.siPUT("max_open_files", "0");
        if (!cpu_usage.equals("")) {
            ServerStatus.siPUT("server_cpu", String.valueOf((int)Float.parseFloat(cpu_usage.split(":")[0])));
            ServerStatus.siPUT("os_cpu", String.valueOf((int)Float.parseFloat(cpu_usage.split(":")[1])));
            if (cpu_usage.split(":").length > 2) {
                ServerStatus.siPUT("open_files", cpu_usage.split(":")[2]);
            }
            if (cpu_usage.split(":").length > 3) {
                ServerStatus.siPUT("max_open_files", cpu_usage.split(":")[3]);
            }
        }
        ServerStatus.siPUT("user_login_num", this.server_info.getProperty("user_login_num", "0"));
        try {
            ServerStatus.siPUT("currentFileDate", String.valueOf(this.prefsProvider.getPrefsTime(null)));
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.common_code.set_defaults(this.default_settings);
        if (!com.crushftp.client.Common.dmz_mode) {
            server_settings = (Properties)this.default_settings.clone();
        }
        new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/").mkdirs();
        Common.updateOSXInfo(String.valueOf(System.getProperty("crushftp.backup")) + "backup/");
        try {
            if (!com.crushftp.client.Common.dmz_mode) {
                server_settings = this.prefsProvider.loadPrefs(null);
            }
            this.server_info.put("currentFileDate", String.valueOf(this.prefsProvider.getPrefsTime(null)));
            this.prefsProvider.check_code();
        }
        catch (Exception ee) {
            Log.log("SERVER", 0, "Prefs.XML was corrupt.  Looking for automatic backup...");
            Log.log("SERVER", 0, ee);
            server_settings = this.prefsProvider.getBackupPrefs(null);
            ServerStatus.thisObj.starting = false;
            thisObj.save_server_settings(true);
            ServerStatus.thisObj.starting = true;
            this.prefsProvider.check_code();
        }
        this.setupGlobalPrefs();
        try {
            if (start_threads) {
                Thread.sleep(ServerStatus.IG("startup_delay") * 1000);
            }
        }
        catch (InterruptedException ee) {
            // empty catch block
        }
        boolean needSave = false;
        try {
            if (this.loggingProvider2 != null) {
                this.loggingProvider2.shutdown();
            }
            this.loggingProvider2 = null;
            if (!ServerStatus.SG("logging_provider").equals("") && !ServerStatus.SG("logging_provider").equals("crushftp.handlers.log.LoggingProviderDisk")) {
                this.loggingProvider2 = (LoggingProvider)Class.forName(ServerStatus.SG("logging_provider")).newInstance();
            }
            this.last_logging_provider2 = ServerStatus.SG("logging_provider");
        }
        catch (Exception e) {
            this.loggingProvider2 = null;
            e.printStackTrace();
            Log.log("SERVER", 0, e);
        }
        try {
            if (this.loggingProvider1 == null) {
                this.loggingProvider1 = new LoggingProviderDisk();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.log("SERVER", 0, e);
        }
        if (ServerStatus.VG("plugins") != null) {
            int x2 = ServerStatus.VG("plugins").size() - 1;
            while (x2 >= 0) {
                try {
                    Vector subitems = (Vector)ServerStatus.VG("plugins").elementAt(x2);
                    p = (Properties)subitems.elementAt(0);
                    if (p.getProperty("pluginName").equalsIgnoreCase("mm.mysql-2.0.14-bin") || p.getProperty("pluginName").equalsIgnoreCase("mysql-connector-java-5.0.4-bin")) {
                        ServerStatus.VG("plugins").removeElementAt(x2);
                        needSave = true;
                    } else if (!new File_S(String.valueOf(System.getProperty("crushftp.plugins")) + "plugins/" + p.getProperty("pluginName") + ".jar").exists()) {
                        ServerStatus.VG("plugins").removeElementAt(x2);
                        needSave = true;
                    }
                }
                catch (Exception subitems) {
                    // empty catch block
                }
                --x2;
            }
        }
        if (!ServerStatus.SG("prefs_version").startsWith("6") && !ServerStatus.SG("prefs_version").startsWith("7")) {
            needSave = true;
            server_settings.put("prefs_version", "6");
            Vector<Properties> previews = (Vector<Properties>)server_settings.get("preview_configs");
            if (previews == null || previews.size() == 0 && server_settings.containsKey("preview_enabled")) {
                previews = new Vector<Properties>();
                try {
                    Properties pre = new Properties();
                    pre.put("preview_enabled", server_settings.getProperty("preview_enabled"));
                    pre.put("preview_debug", server_settings.getProperty("preview_debug"));
                    pre.put("preview_scan_interval", server_settings.getProperty("preview_scan_interval"));
                    pre.put("preview_command_line", server_settings.getProperty("preview_command_line"));
                    pre.put("preview_conversion_threads", server_settings.getProperty("preview_conversion_threads"));
                    pre.put("preview_file_extensions", server_settings.getProperty("preview_file_extensions"));
                    pre.put("preview_sizes", server_settings.get("preview_sizes"));
                    pre.put("preview_working_dir", server_settings.getProperty("preview_working_dir"));
                    pre.put("preview_environment", server_settings.getProperty("preview_environment"));
                    pre.put("preview_exif", server_settings.getProperty("preview_exif"));
                    pre.put("preview_subdirectories", server_settings.getProperty("preview_subdirectories"));
                    pre.put("preview_reverseSubdirectories", server_settings.getProperty("preview_reverseSubdirectories"));
                    pre.put("preview_folder_list", server_settings.get("preview_folder_list"));
                    previews.addElement(pre);
                }
                catch (Exception e) {
                    Log.log("SERVER", 0, e);
                }
                server_settings.put("preview_configs", previews);
            }
            if (!ServerStatus.SG("smtp_pass").equals("")) {
                server_settings.put("smtp_pass", this.common_code.encode_pass(ServerStatus.SG("smtp_pass"), "DES", ""));
            }
            if (!ServerStatus.SG("search_db_pass").equals("")) {
                server_settings.put("search_db_pass", this.common_code.encode_pass(ServerStatus.SG("search_db_pass"), "DES", ""));
            }
            if (!ServerStatus.SG("db_pass").equals("")) {
                server_settings.put("db_pass", this.common_code.encode_pass(ServerStatus.SG("db_pass"), "DES", ""));
            }
            if (!ServerStatus.SG("syncs_db_pass").equals("")) {
                server_settings.put("syncs_db_pass", this.common_code.encode_pass(ServerStatus.SG("syncs_db_pass"), "DES", ""));
            }
            if (!ServerStatus.SG("stats_db_pass").equals("")) {
                server_settings.put("stats_db_pass", this.common_code.encode_pass(ServerStatus.SG("stats_db_pass"), "DES", ""));
            }
            if (!ServerStatus.SG("filter1").equals("")) {
                server_settings.put("globalKeystoreCertPass", server_settings.remove("filter1"));
            }
            if (!ServerStatus.SG("filter2").equals("")) {
                server_settings.put("globalKeystorePass", server_settings.remove("filter2"));
            }
            if (ServerStatus.SG("log_allow_str").equals("(ERROR)(START)(STOP)(QUIT_SERVER)(RUN_SERVER)(KICK)(BAN)(DENIAL)(ACCEPT)(DISCONNECT)(USER)(PASS)(SYST)(NOOP)(SIZE)(MDTM)(RNFR)(RNTO)(PWD)(CWD)(TYPE)(REST)(DELE)(MKD)(RMD)(MACB)(ABOR)(RETR)(STOR)(APPE)(LIST)(NLST)(CDUP)(PASV)(PORT)(AUTH)(PBSZ)(PROT)(SITE)(QUIT)(GET)(PUT)(DELETE)(MOVE)(STAT)(HELP)(PAUSE_RESUME)(PROXY)")) {
                server_settings.put("log_allow_str", this.default_settings.getProperty("log_allow_str"));
            }
            int x3 = ServerStatus.VG("plugins").size() - 1;
            while (x3 >= 0) {
                try {
                    Vector subitems = (Vector)ServerStatus.VG("plugins").elementAt(x3);
                    int xx = 0;
                    while (xx < subitems.size()) {
                        Properties p2 = (Properties)subitems.elementAt(xx);
                        if (p2.getProperty("pluginName").equalsIgnoreCase("CrushNOIP")) {
                            if (!p2.getProperty("pass", "").equals("")) {
                                p2.put("pass", this.common_code.encode_pass(p2.getProperty("pass"), "DES", ""));
                            }
                        } else if (p2.getProperty("pluginName").equalsIgnoreCase("CrushSQL")) {
                            if (!p2.getProperty("db_pass", "").equals("")) {
                                p2.put("db_pass", this.common_code.encode_pass(p2.getProperty("db_pass"), "DES", ""));
                            }
                        } else if (p2.getProperty("pluginName").equalsIgnoreCase("CrushTask")) {
                            Vector tasks = (Vector)p2.get("tasks");
                            int xxx = 0;
                            while (tasks != null && xxx < tasks.size()) {
                                Properties t = (Properties)tasks.elementAt(xxx);
                                if (t.getProperty("type", "").equalsIgnoreCase("HTTP")) {
                                    if (!t.getProperty("password", "").equals("")) {
                                        t.put("password", this.common_code.encode_pass(t.getProperty("password"), "DES", ""));
                                    }
                                } else if (t.getProperty("type", "").equalsIgnoreCase("FIND") || t.getProperty("type", "").equalsIgnoreCase("COPY")) {
                                    if (!t.getProperty("ssh_private_key_pass", "").equals("")) {
                                        t.put("ssh_private_key_pass", this.common_code.encode_pass(t.getProperty("ssh_private_key_pass"), "DES", ""));
                                    }
                                } else if (t.getProperty("type", "").equalsIgnoreCase("PGP")) {
                                    if (!t.getProperty("key_password", "").equals("")) {
                                        t.put("key_password", this.common_code.encode_pass(t.getProperty("key_password"), "DES", ""));
                                    }
                                } else if (t.getProperty("type", "").equalsIgnoreCase("PopImap") && !t.getProperty("mail_pass", "").equals("")) {
                                    t.put("mail_pass", this.common_code.encode_pass(t.getProperty("mail_pass"), "DES", ""));
                                }
                                ++xxx;
                            }
                        } else if (p2.getProperty("pluginName").equalsIgnoreCase("PostBack") && !p2.getProperty("password", "").equals("")) {
                            p2.put("password", this.common_code.encode_pass(p2.getProperty("password"), "DES", ""));
                        }
                        ++xx;
                    }
                }
                catch (Exception e) {
                    Log.log("PLUGIN", 0, e);
                }
                --x3;
            }
            Vector pref_server_items = (Vector)server_settings.get("server_list");
            int x4 = 0;
            while (x4 < pref_server_items.size()) {
                Properties server_item = (Properties)pref_server_items.elementAt(x4);
                if (server_item.getProperty("serverType", "FTP").equalsIgnoreCase("SFTP")) {
                    SSHDaemon.setupDaemon(server_item);
                }
                ++x4;
            }
        }
        if (!ServerStatus.SG("prefs_version").equals("7.0") && !com.crushftp.client.Common.dmz_mode) {
            Vector schedules = (Vector)server_settings.get("schedules");
            new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/").mkdirs();
            int x5 = 0;
            while (schedules != null && x5 < schedules.size()) {
                p = (Properties)schedules.elementAt(x5);
                String scheduleName = p.getProperty("scheduleName");
                scheduleName = JobScheduler.safeName(scheduleName);
                new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + scheduleName).mkdirs();
                try {
                    JobFilesHandler.writeXMLObject(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + scheduleName + "/job.XML", p, "job");
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
                ++x5;
            }
            needSave = true;
            server_settings.put("prefs_version", "7.0");
        }
        System.getProperties().put("jdk.tls.useExtendedMasterSecret", System.getProperty("crushftp.tls.resume_session", "false"));
        if (!ServerStatus.SG("extra_system_properties").equals("")) {
            String[] params = ServerStatus.SG("extra_system_properties").split("-D");
            int x6 = 0;
            while (x6 < params.length) {
                if (params[x6].indexOf("=") >= 0) {
                    System.getProperties().put(params[x6].split("=")[0].trim(), params[x6].split("=")[1].trim());
                }
                ++x6;
            }
        }
        server_settings.put("v8_beta", "true");
        if (ServerStatus.SG("Access-Control-Allow-Origin").equals("true")) {
            server_settings.put("Access-Control-Allow-Origin", "*");
        }
        if (ServerStatus.SG("tls_version").equals("") || ServerStatus.SG("tls_version").equals("1") || ServerStatus.SG("tls_version").equals("TLSv1")) {
            server_settings.put("tls_version", "SSLv2Hello,TLSv1,TLSv1.1,TLSv1.2");
        }
        if (ServerStatus.SG("log_roll_date_format").equals("yyyyMMdd_hhmmss")) {
            server_settings.put("log_roll_date_format", "yyyyMMdd_HHmmss");
        }
        if (ServerStatus.SG("disabled_ciphers").equals("")) {
            server_settings.put("disabled_ciphers", this.default_settings.getProperty("disabled_ciphers"));
        }
        if (ServerStatus.SG("ssh_sha1_kex_allowed").equals("true")) {
            Vector pref_server_items = (Vector)server_settings.get("server_list");
            int x7 = 0;
            while (x7 < pref_server_items.size()) {
                Properties server_item = (Properties)pref_server_items.elementAt(x7);
                if (server_item.getProperty("serverType", "FTP").equalsIgnoreCase("SFTP")) {
                    server_item.put("key_exchanges", "diffie-hellman-group1-sha1," + server_item.getProperty("key_exchanges", "diffie-hellman-group14-sha1, diffie-hellman-group-exchange-sha1, diffie-hellman-group-exchange-sha256, ecdh-sha2-nistp256, ecdh-sha2-nistp384, ecdh-sha2-nistp521"));
                }
                ++x7;
            }
        }
        if (ServerStatus.SG("disabled_ciphers").toUpperCase().indexOf("_EXPORT_") < 0) {
            String disabled_ciphers = ServerStatus.SG("disabled_ciphers").toUpperCase();
            try {
                SSLServerSocketFactory ssf = this.common_code.getSSLContext("builtin", "builtin", "", "", "TLS", false, true).getServerSocketFactory();
                SSLServerSocket serverSock = (SSLServerSocket)ssf.createServerSocket(0, 1);
                String[] ciphers = serverSock.getSupportedCipherSuites();
                serverSock.close();
                int x8 = 0;
                while (x8 < ciphers.length) {
                    if (ciphers[x8].toUpperCase().indexOf("EXPORT") >= 0 && disabled_ciphers.indexOf(ciphers[x8].toUpperCase()) < 0) {
                        disabled_ciphers = String.valueOf(disabled_ciphers) + "(" + ciphers[x8].toUpperCase() + ")";
                        needSave = true;
                    } else if (ciphers[x8].toUpperCase().indexOf("ANON") >= 0 && disabled_ciphers.indexOf(ciphers[x8].toUpperCase()) < 0) {
                        disabled_ciphers = String.valueOf(disabled_ciphers) + "(" + ciphers[x8].toUpperCase() + ")";
                        needSave = true;
                    } else if (ciphers[x8].toUpperCase().indexOf("NULL") >= 0 && disabled_ciphers.indexOf(ciphers[x8].toUpperCase()) < 0) {
                        disabled_ciphers = String.valueOf(disabled_ciphers) + "(" + ciphers[x8].toUpperCase() + ")";
                        needSave = true;
                    } else if (ciphers[x8].toUpperCase().indexOf("INFO") >= 0 && disabled_ciphers.indexOf(ciphers[x8].toUpperCase()) < 0) {
                        disabled_ciphers = String.valueOf(disabled_ciphers) + "(" + ciphers[x8].toUpperCase() + ")";
                        needSave = true;
                    }
                    ++x8;
                }
                server_settings.put("disabled_ciphers", disabled_ciphers);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        }
        this.checkCrushExpiration();
        Properties localization = new Properties();
        String localized = server_settings.getProperty("localization", "English");
        if (localized.equals("ENGLISH")) {
            localized = "English";
        }
        server_settings.put("localization", localized);
        localization.put("localization", localized);
        this.logDateFormat = new SimpleDateFormat(ServerStatus.SG("log_date_format"), Locale.US);
        try {
            new File_S(String.valueOf(new File_S(ServerStatus.change_vars_to_values_static(ServerStatus.SG("log_location"), null, null, null)).getCanonicalFile().getParentFile().getPath()) + "/logs/session_logs/").mkdirs();
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
        }
        if (server_settings.containsKey("s3_threads")) {
            server_settings.put("s3_threads_download", server_settings.getProperty("s3_threads"));
            server_settings.put("s3_threads_upload", server_settings.getProperty("s3_threads"));
        }
        try {
            ServerStatus.startStatsLoader(this.server_info, String.valueOf(System.getProperty("crushftp.stats")) + "stats.XML");
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
        }
        Enumeration<?> the_list = this.default_settings.propertyNames();
        while (the_list.hasMoreElements()) {
            Object cur = the_list.nextElement();
            if (server_settings.get(cur.toString()) != null) continue;
            server_settings.put(cur.toString(), this.default_settings.get(cur.toString()));
            needSave = true;
        }
        Properties sqlItems = (Properties)server_settings.get("sqlItems");
        Properties sqlItems2 = new Properties();
        SQLUsers.setDefaults(sqlItems2);
        this.default_settings.put("sqlItems", sqlItems2.clone());
        Enumeration<Object> keys = sqlItems2.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            if (sqlItems.containsKey(key)) continue;
            sqlItems.put(key, sqlItems2.get(key));
        }
        the_list = server_settings.propertyNames();
        while (the_list.hasMoreElements()) {
            Object cur = the_list.nextElement();
            if (this.default_settings.containsKey(cur.toString()) || cur.toString().equals("ftp_aware_router") || cur.toString().equals("pasv_ports") || cur.toString().equals("server_ip")) continue;
            server_settings.remove(cur.toString());
            needSave = true;
        }
        server_settings.put("v10_beta", "true");
        this.prefsProvider.check_code();
        server_settings.put("v10_beta", "true");
        if (ServerStatus.BG("block_client_renegotiation")) {
            System.setProperty("jdk.tls.rejectClientInitiatedRenegotiation", "true");
        }
        long last_stats_db_size = com.crushftp.client.Common.recurseSize("./statsDB", 0L);
        long last_sessions_obj_size = com.crushftp.client.Common.recurseSize("./sessions.obj", 0L);
        ServerStatus.siPUT("last_stats_db_size", String.valueOf(last_stats_db_size));
        ServerStatus.siPUT("last_sessions_obj_size", String.valueOf(last_sessions_obj_size));
        if (ServerStatus.BG("encryption_pass_needed") && new String(com.crushftp.client.Common.encryption_password).equals("crushftp")) {
            this.append_log("WARNING: Encryption password needed for the server!  Please login to admin console to provide it.", "RUN_SERVER");
        }
        String memory_threads = "Server Memory Stats: Max=" + com.crushftp.client.Common.format_bytes_short2(ServerStatus.siLG("ram_max")) + ", Free=" + com.crushftp.client.Common.format_bytes_short2(ServerStatus.siLG("ram_free")) + ", Threads:" + Worker.busyWorkers.size() + ", " + System.getProperty("java.version") + ":" + System.getProperty("sun.arch.data.model") + " bit," + Runtime.getRuntime().availableProcessors() + "cores  OS:" + System.getProperties().getProperty("os.name") + " CPU usage Server/OS:" + ServerStatus.siSG("server_cpu") + "/" + ServerStatus.siSG("os_cpu") + " OpenFiles:" + ServerStatus.siSG("open_files") + "/" + ServerStatus.siSG("max_open_files") + ", statsDB size=" + com.crushftp.client.Common.format_bytes_short(last_stats_db_size) + ", sessions.obj size=" + com.crushftp.client.Common.format_bytes_short(last_sessions_obj_size) + " :" + ServerStatus.siSG("version_info_str") + ServerStatus.siSG("sub_version_info_str");
        this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|********" + System.getProperty("appname", "CrushFTP") + " " + LOC.G("Run") + "******** " + memory_threads, "RUN_SERVER");
        try {
            this.server_info.put("jce_installed", String.valueOf(Cipher.getMaxAllowedKeyLength("AES") == Integer.MAX_VALUE));
            String ipList = "";
            Vector allow_list = (Vector)server_settings.get("ip_restrictions");
            x = allow_list.size() - 1;
            while (x >= 0) {
                Properties ip_data = (Properties)allow_list.elementAt(x);
                String s = String.valueOf(ip_data.getProperty("start_ip")) + ":" + ip_data.getProperty("stop_ip") + ":" + ip_data.getProperty("type");
                if (ipList.indexOf(s) < 0) {
                    ipList = String.valueOf(ipList) + s;
                } else {
                    allow_list.remove(ip_data);
                }
                --x;
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 1, e);
        }
        if (!ServerStatus.BG("csrf_flipped")) {
            server_settings.put("csrf", "true");
            server_settings.put("csrf_flipped", "true");
            needSave = true;
        }
        ServerStatus.checkServerGroups();
        Vector v = new Vector();
        try {
            String username;
            UserTools.refreshUserList("extra_vfs", v);
            int tildas = 0;
            x = 0;
            while (tildas == 0 && x < v.size()) {
                username = v.elementAt(x).toString();
                if (username.indexOf("~") >= 0) {
                    if (tildas == 0) {
                        Log.log("SERVER", 0, "Found a tilda extra_vfs, no update needed:" + username);
                    }
                    ++tildas;
                }
                ++x;
            }
            x = 0;
            while (tildas == 0 && x < v.size()) {
                username = v.elementAt(x).toString();
                Properties user = UserTools.ut.getUser("extra_vfs", username, false);
                if (username.indexOf("~") < 0 && username.indexOf("_") >= 0) {
                    String username2 = String.valueOf(username.substring(0, username.lastIndexOf("_"))) + "~" + username.substring(username.lastIndexOf("_") + 1);
                    UserTools.changeUsername("extra_vfs", username, username2, user.getProperty("password", ""));
                    Log.log("SERVER", 0, "Updating extra_vfs username:" + username + "->" + username2);
                }
                ++x;
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
        }
        this.total_server_bytes_sent = ServerStatus.LG("total_server_bytes_sent");
        this.total_server_bytes_received = ServerStatus.LG("total_server_bytes_received");
        if (!System.getProperty("crushftp.previews", "").equals("")) {
            server_settings.put("previews_path", System.getProperty("crushftp.previews", ""));
        }
        this.reset_threads(start_threads);
        this.reset_preview_workers();
        if (start_threads) {
            try {
                this.update_timer_thread.interrupt();
            }
            catch (Exception e) {
                // empty catch block
            }
            this.update_timer_thread = null;
            UpdateTimer the_thread = new UpdateTimer(this, 1000, "ServerStatus", "gui_timer");
            this.update_timer_thread = new Thread(the_thread);
            this.update_timer_thread.setName("ServerStatus:update_timer:gui_timer:");
            this.update_timer_thread.setPriority(1);
            this.update_timer_thread.start();
            try {
                this.extra_update_timer_thread.interrupt();
            }
            catch (Exception x9) {
                // empty catch block
            }
            this.extra_update_timer_thread = null;
            the_thread = new UpdateTimer(this, 1000, "ServerStatus", "extra_update_timer");
            this.extra_update_timer_thread = new Thread(the_thread);
            this.extra_update_timer_thread.setName("ServerStatus:update_timer:extra_update_timer:");
            this.extra_update_timer_thread.setPriority(1);
            this.extra_update_timer_thread.start();
            try {
                this.logging_thread.interrupt();
            }
            catch (Exception x9) {
                // empty catch block
            }
            this.logging_thread = null;
            the_thread = new UpdateTimer(this, 1000, "ServerStatus", "log_handler");
            this.logging_thread = new Thread(the_thread);
            this.logging_thread.setName("ServerStatus:update_timer:log_handler:");
            this.logging_thread.setPriority(10);
            this.logging_thread.start();
            try {
                IdleMonitor.init();
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        }
        if (this.commandBufferFlusher == null) {
            this.commandBufferFlusher = new CommandBufferFlusher(ServerStatus.IG("command_flush_interval"));
            try {
                Worker.startWorker(this.commandBufferFlusher, "ServerStatus:CommandBufferFlusher");
            }
            catch (IOException the_thread) {
                // empty catch block
            }
        }
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        Vector server_groups;
                        if (!new File("./prefs.XML").exists()) {
                            Thread.sleep(20000L);
                        }
                        if (!(server_groups = (Vector)com.crushftp.client.Common.CLONE(server_settings.get("server_groups"))).contains("extra_vfs")) {
                            server_groups.add("extra_vfs");
                        }
                        int xx = 0;
                        while (xx < server_groups.size()) {
                            Properties groups = UserTools.getGroups(server_groups.elementAt(xx).toString());
                            UserTools.writeGroups(server_groups.elementAt(xx).toString(), groups);
                            Properties inheritance = UserTools.getInheritance(server_groups.elementAt(xx).toString());
                            UserTools.writeInheritance(server_groups.elementAt(xx).toString(), inheritance);
                            ++xx;
                        }
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 0, e);
                    }
                }
            });
        }
        catch (IOException the_thread) {
            // empty catch block
        }
        this.commandBufferFlusher.setInterval(ServerStatus.IG("command_flush_interval"));
        if (start_threads && System.getProperty("crushftp.start_servers", "true").equals("true") && Common.isValidTemplateUserOfDMZ()) {
            this.start_all_servers();
        }
        this.starting = false;
        if (needSave) {
            this.save_server_settings(true);
        }
        if (ServerStatus.siIG("enterprise_level") > 0) {
            try {
                RandomAccessFile raf = new RandomAccessFile(String.valueOf(System.getProperty("crushftp.backup")) + "backup/yc.yaml", "rw");
                raf.setLength(0L);
                raf.write("version: '1'\r\n".getBytes("UTF8"));
                raf.write("options:\r\n".getBytes("UTF8"));
                raf.write(("   j: " + System.getProperty("java.home") + "\r\n").getBytes("UTF8"));
                raf.write("   k: M2hVZFJxajF5NVlQc2ZaS25kcjRNZz09@193dc151-ed91-4c77-acb4-dd08cb5bf23d\r\n".getBytes("UTF8"));
                raf.write("   s: https://receiver.ycrash.io\r\n".getBytes("UTF8"));
                raf.write(("   a: " + ServerStatus.SG("registration_email") + "_CFTP\r\n").getBytes("UTF8"));
                raf.close();
                raf = new RandomAccessFile(String.valueOf(System.getProperty("crushftp.backup")) + "backup/yc.sh", "rw");
                raf.setLength(0L);
                raf.write(("#!/usr/bin/env bash\nget_pid()\n{\n CRUSH_PID=\"`ps -ef | grep java | grep CrushFTP | grep .jar | awk '{print $2}'`\"\n}\nget_pid\n" + new File_S("./plugins/lib/yc").getCanonicalPath() + " -c " + new File_S(System.getProperty("crushftp.backup")).getCanonicalPath() + "/backup/yc.yaml -p $CRUSH_PID\n").getBytes("UTF8"));
                raf.close();
                if (Common.machine_is_linux()) {
                    com.crushftp.client.Common.exec(new String[]{"chmod", "+x", new File_S("./plugins/lib/yc").getCanonicalPath()});
                }
                if (Common.machine_is_linux()) {
                    com.crushftp.client.Common.exec(new String[]{"chmod", "+x", new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/yc.sh").getCanonicalPath()});
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        }
    }

    /*
     * Unable to fully structure code
     */
    public void reset_preview_workers() {
        ok = false;
        if (this.previewWorkers.size() == ServerStatus.VG("preview_configs").size()) {
            ok = true;
            x = 0;
            while (x < this.previewWorkers.size()) {
                preview = (PreviewWorker)this.previewWorkers.elementAt(x);
                prefs = (Properties)ServerStatus.VG("preview_configs").elementAt(x);
                if (preview.prefs != prefs) {
                    ok = false;
                }
                ++x;
            }
        }
        if (!ok) ** GOTO lbl17
        return;
lbl-1000:
        // 1 sources

        {
            preview = (PreviewWorker)this.previewWorkers.elementAt(0);
            preview.abort = true;
            this.previewWorkers.removeElementAt(0);
lbl17:
            // 2 sources

            ** while (this.previewWorkers.size() > 0)
        }
lbl18:
        // 1 sources

        x = 0;
        while (x < ServerStatus.VG("preview_configs").size()) {
            prefs = (Properties)ServerStatus.VG("preview_configs").elementAt(x);
            preview = new PreviewWorker(prefs);
            this.previewWorkers.addElement(preview);
            ++x;
        }
    }

    public static void startStatsLoader(Properties server_info, String statsPath) {
        ServerStatus.thisObj.statTools.init();
    }

    public void setSettings(Properties p) {
        Properties source = (Properties)p.get("data");
        Vector log = new Vector();
        try {
            Common.diffObjects(source, server_settings, log, "", false);
        }
        catch (RuntimeException e) {
            Log.log("SERVER", 0, e);
        }
        thisObj.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|" + "Server Settings Changed", "RUN_SERVER");
        int x = 0;
        while (x < log.size()) {
            if (log.elementAt(x).toString().toUpperCase().indexOf("PASSWORD") < 0) {
                thisObj.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|" + log.elementAt(x).toString(), "RUN_SERVER");
            }
            ++x;
        }
        Properties dest = server_settings;
        Enumeration<?> the_list = this.default_settings.propertyNames();
        while (the_list.hasMoreElements()) {
            String cur = the_list.nextElement().toString();
            if (!source.containsKey(cur)) continue;
            Object sourceO = source.get(cur);
            Object destO = server_settings.get(cur);
            if (destO == null || destO instanceof String) {
                dest.put(cur, sourceO);
                continue;
            }
            if (cur.equals("ip_restrictions")) {
                Vector source_temp = (Vector)com.crushftp.client.Common.CLONE(sourceO);
                Vector dest_temp = (Vector)com.crushftp.client.Common.CLONE(destO);
                Collections.reverse(source_temp);
                Collections.reverse(dest_temp);
                Common.updateObjectLog(source_temp, dest_temp, null);
                Common.updateObjectLog(sourceO, destO, new StringBuffer(), true);
                continue;
            }
            Common.updateObjectLog(sourceO, destO, null);
        }
        ServerStatus.thisObj.common_code.loadPluginsSync(server_settings, this.server_info);
        thisObj.save_server_settings(true);
    }

    public void reset_threads(boolean start_threads) {
        if (start_threads) {
            this.setup_hammer_banning();
            this.setup_phammer_banning();
            this.setup_ban_timer();
            this.setup_discover_ip_refresh();
            this.setup_log_rolling();
            this.setup_events();
            this.setup_monitor_folders();
            this.setup_http_cleaner();
            this.setup_vfs_replication_pinger();
            this.setup_update_2_timer();
            this.setup_stats_saver();
            this.setup_jobs_resumer();
            this.setup_report_scheduler();
            this.setup_scheduler();
            this.setup_alerts();
            this.setup_new_version();
            this.common_code.loadPlugins(server_settings, this.server_info);
        }
    }

    public void reset_server_login_counts() {
        ServerStatus.siPUT2("successful_logins", "0");
        ServerStatus.siPUT2("failed_logins", "0");
        thisObj.save_server_settings(true);
    }

    public void reset_server_bytes_in_out() {
        this.total_server_bytes_sent = 0L;
        this.total_server_bytes_received = 0L;
        ServerStatus.siPUT2("total_server_bytes_sent", "0");
        ServerStatus.siPUT2("total_server_bytes_received", "0");
        thisObj.save_server_settings(true);
    }

    public void reset_upload_download_counter() {
        ServerStatus.siPUT2("downloaded_files", "0");
        ServerStatus.siPUT2("uploaded_files", "0");
        thisObj.save_server_settings(true);
    }

    public void setup_log_rolling() {
        try {
            if (this.log_rolling_thread != null) {
                int loops = 0;
                while (this.log_rolling_thread.isAlive() && loops++ < 100) {
                    this.log_rolling_thread.interrupt();
                    Thread.sleep(100L);
                }
            }
        }
        catch (Exception loops) {
            // empty catch block
        }
        this.log_rolling_thread = null;
        UpdateTimer the_thread = new UpdateTimer(this, 25000, "ServerStatus", "log_rolling");
        this.log_rolling_thread = new Thread(the_thread);
        this.log_rolling_thread.setName("ServerStatus:log_rolling:");
        this.log_rolling_thread.setPriority(1);
        this.log_rolling_thread.start();
    }

    public void setup_events() {
        try {
            this.events_thread.interrupt();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.events_thread = null;
        UpdateTimer the_thread = new UpdateTimer(this, 1000, "ServerStatus", "events_thread");
        this.events_thread = new Thread(the_thread);
        this.events_thread.setName("ServerStatus:events_thread:");
        this.events_thread.setPriority(1);
        this.events_thread.start();
    }

    public void setup_monitor_folders() {
        try {
            this.monitor_folders_thread.interrupt();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.monitor_folders_thread = null;
        UpdateTimer the_thread = new UpdateTimer(this, 60000, "ServerStatus", "monitor_folders");
        this.monitor_folders_thread = new Thread(the_thread);
        this.monitor_folders_thread.setName("ServerStatus:monitor_folders:");
        this.monitor_folders_thread.setPriority(1);
        this.monitor_folders_thread.start();
        try {
            this.monitor_folders_thread_instant.interrupt();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.monitor_folders_thread_instant = new Thread(new UpdateTimer(this, 1000, "ServerStatus", "monitor_folders_instant"));
        this.monitor_folders_thread_instant.setName("ServerStatus:monitor_folders_instant:");
        this.monitor_folders_thread_instant.setPriority(1);
        this.monitor_folders_thread_instant.start();
    }

    public void setup_http_cleaner() {
        try {
            if (this.http_cleaner_thread != null) {
                int loops = 0;
                while (this.http_cleaner_thread.isAlive() && loops++ < 100) {
                    this.http_cleaner_thread.interrupt();
                    Thread.sleep(100L);
                }
            }
        }
        catch (Exception loops) {
            // empty catch block
        }
        this.http_cleaner_thread = null;
        UpdateTimer the_thread = new UpdateTimer(this, 1000 * ServerStatus.IG("http_cleaner_interval") * 1, "ServerStatus", "http_cleaner");
        this.http_cleaner_thread = new Thread(the_thread);
        this.http_cleaner_thread.setName("ServerStatus:http_cleaner:");
        this.http_cleaner_thread.setPriority(1);
        this.http_cleaner_thread.start();
    }

    public void setup_vfs_replication_pinger() {
        try {
            if (this.vfs_replication_pinger_thread != null) {
                int loops = 0;
                while (this.vfs_replication_pinger_thread.isAlive() && loops++ < 100) {
                    this.vfs_replication_pinger_thread.interrupt();
                    Thread.sleep(100L);
                }
            }
        }
        catch (Exception loops) {
            // empty catch block
        }
        this.vfs_replication_pinger_thread = null;
        UpdateTimer the_thread = new UpdateTimer(this, 1000, "ServerStatus", "vfs_replication_pinger");
        this.vfs_replication_pinger_thread = new Thread(the_thread);
        this.vfs_replication_pinger_thread.setName("ServerStatus_replication_pinger:");
        this.vfs_replication_pinger_thread.setPriority(1);
        this.vfs_replication_pinger_thread.start();
    }

    public void setup_discover_ip_refresh() {
        try {
            if (this.discover_ip_timer_thread != null) {
                int loops = 0;
                while (this.discover_ip_timer_thread.isAlive() && loops++ < 100) {
                    this.discover_ip_timer_thread.interrupt();
                    Thread.sleep(100L);
                }
            }
        }
        catch (Exception loops) {
            // empty catch block
        }
        this.discover_ip_timer_thread = null;
        int mins = ServerStatus.IG("discover_ip_refresh");
        if (mins < 1) {
            mins = 1;
        }
        UpdateTimer the_thread = new UpdateTimer(this, mins * 60000, "ServerStatus", "discover_ip_timer");
        this.discover_ip_timer_thread = new Thread(the_thread);
        this.discover_ip_timer_thread.setName("ServerStatus:discover_ip_timer:");
        this.discover_ip_timer_thread.setPriority(1);
        this.discover_ip_timer_thread.start();
    }

    public void setup_ban_timer() {
        try {
            if (this.ban_timer_thread != null) {
                int loops = 0;
                while (this.ban_timer_thread.isAlive() && loops++ < 100) {
                    this.ban_timer_thread.interrupt();
                    Thread.sleep(100L);
                }
            }
        }
        catch (Exception loops) {
            // empty catch block
        }
        this.ban_timer_thread = null;
        UpdateTimer the_thread = new UpdateTimer(this, 10000, "ServerStatus", "ban_timer");
        this.ban_timer_thread = new Thread(the_thread);
        this.ban_timer_thread.setName("ServerStatus:ban_timer:");
        this.ban_timer_thread.setPriority(1);
        this.ban_timer_thread.start();
        try {
            this.cban_timer_thread.interrupt();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.cban_timer_thread = null;
        the_thread = new UpdateTimer(this, 1000, "ServerStatus", "cban_timer");
        this.cban_timer_thread = new Thread(the_thread);
        this.cban_timer_thread.setName("ServerStatus:cban_timer:");
        this.cban_timer_thread.setPriority(1);
        this.cban_timer_thread.start();
    }

    public void setup_hammer_banning() {
        try {
            if (this.hammer_timer_thread != null) {
                int loops = 0;
                while (this.hammer_timer_thread.isAlive() && loops++ < 100) {
                    this.hammer_timer_thread.interrupt();
                    Thread.sleep(100L);
                }
            }
        }
        catch (Exception loops) {
            // empty catch block
        }
        this.hammer_timer_thread = new Thread(new UpdateTimer(this, ServerStatus.IG("hammer_banning") * 1000, "ServerStatus", "hammer_timer"));
        this.hammer_timer_thread.setName("ServerStatus:hammer_timer:");
        this.hammer_timer_thread.setPriority(1);
        this.hammer_timer_thread.start();
        try {
            if (this.hammer_timer_http_thread != null) {
                int loops = 0;
                while (this.hammer_timer_http_thread.isAlive() && loops++ < 100) {
                    this.hammer_timer_http_thread.interrupt();
                    Thread.sleep(100L);
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.hammer_timer_http_thread = new Thread(new UpdateTimer(this, ServerStatus.IG("hammer_banning_http") * 1000, "ServerStatus", "hammer_timer_http"));
        this.hammer_timer_http_thread.setName("ServerStatus:hammer_timer:");
        this.hammer_timer_http_thread.setPriority(1);
        this.hammer_timer_http_thread.start();
    }

    public void setup_phammer_banning() {
        try {
            this.phammer_timer_thread.interrupt();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.phammer_timer_thread = null;
        UpdateTimer the_thread = new UpdateTimer(this, 10000, "ServerStatus", "phammer_timer");
        this.phammer_timer_thread = new Thread(the_thread);
        this.phammer_timer_thread.setName("ServerStatus:phammer_timer:");
        this.phammer_timer_thread.setPriority(1);
        if (System.getProperty("crushftp.disablephammer", "false").equals("false")) {
            this.phammer_timer_thread.start();
        }
    }

    public void setup_update_2_timer() {
        try {
            this.update_2_timer_thread.interrupt();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.update_2_timer_thread = null;
        UpdateTimer the_thread = new UpdateTimer(this, 5000, "ServerStatus", "update_2_timer");
        this.update_2_timer_thread = new Thread(the_thread);
        this.update_2_timer_thread.setName("ServerStatus:update_2_timer:");
        this.update_2_timer_thread.setPriority(1);
        this.update_2_timer_thread.start();
    }

    public void setup_stats_saver() {
        try {
            if (this.stats_saver_thread != null) {
                int loops = 0;
                while (this.stats_saver_thread.isAlive() && loops++ < 100) {
                    this.stats_saver_thread.interrupt();
                    Thread.sleep(100L);
                }
            }
        }
        catch (Exception loops) {
            // empty catch block
        }
        this.stats_saver_thread = null;
        UpdateTimer the_thread = new UpdateTimer(this, ServerStatus.IG("stats_min") * 60000, "ServerStatus", "stats_saver");
        this.stats_saver_thread = new Thread(the_thread);
        this.stats_saver_thread.setName("ServerStatus:stats_saver:");
        this.stats_saver_thread.setPriority(1);
        this.stats_saver_thread.start();
    }

    public void setup_jobs_resumer() {
        try {
            if (this.jobs_resumer_thread != null) {
                int loops = 0;
                while (this.jobs_resumer_thread.isAlive() && loops++ < 100) {
                    this.jobs_resumer_thread.interrupt();
                    Thread.sleep(100L);
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.jobs_resumer_thread = new Thread(new Runnable(){

            /*
             * Unable to fully structure code
             */
            @Override
            public void run() {
                while (true) {
                    try {
                        block3: while (true) {
                            if (ServerStatus.IG("resume_idle_job_delay") <= 0) {
                                Thread.sleep(ServerStatus.IG("resume_idle_job_delay") * -1000);
                                Thread.sleep(100L);
                                continue;
                            }
                            Thread.sleep(ServerStatus.IG("resume_idle_job_delay") * 1000);
                            if (System.getProperty("crushftp.singleuser", "false").equals("true")) continue;
                            jobs = JobScheduler.getJobList(true);
                            x = jobs.size() - 1;
                            while (true) {
                                if (x >= 0) ** break;
                                continue block3;
                                f = (File_S)jobs.elementAt(x);
                                if (new File_S(String.valueOf(f.getPath()) + "/inprogress.XML").exists()) {
                                    new File_S(String.valueOf(f.getPath()) + "/inprogress/").mkdirs();
                                    tracker = (Properties)JobFilesHandler.readXMLObject(new File_S(String.valueOf(f.getPath()) + "/inprogress.XML"));
                                    new File_S(String.valueOf(f.getPath()) + "/inprogress.XML").renameTo(new File_S(String.valueOf(f.getPath()) + "/inprogress/" + tracker.getProperty("id") + ".XML"));
                                }
                                if (new File_S(String.valueOf(f.getPath()) + "/inprogress/").exists()) {
                                    ids = (File_S[])new File_S(String.valueOf(f.getPath()) + "/inprogress/").listFiles();
                                    xx = 0;
                                    while (xx < ids.length) {
                                        if (ids[xx].getName().toUpperCase().endsWith(".XML") && System.currentTimeMillis() - ids[xx].lastModified() > (long)(ServerStatus.IG("resume_idle_job_delay") * 1000)) {
                                            if (!new File_S(String.valueOf(f.getPath()) + "/job.XML").exists()) {
                                                ids[xx].renameTo(new File_S(String.valueOf(f.getPath()) + "/" + ids[xx].getName()));
                                            } else {
                                                delay = System.currentTimeMillis() - ids[xx].lastModified();
                                                ids[xx].setLastModified(System.currentTimeMillis() + (long)(ServerStatus.IG("resume_idle_job_delay") * 1000));
                                                if (ids[xx].length() > 0x100000L * ServerStatus.LG("max_resume_job_size_mb")) {
                                                    Log.log("SERVER", 0, "SKIPPING RESUME IDLE JOB!  Too large! :" + ids[xx] + ":" + com.crushftp.client.Common.format_bytes_short(ids[xx].length()) + ", its been sitting idle:" + delay + "ms");
                                                } else {
                                                    Log.log("SERVER", 0, "Resuming idle job...:" + ids[xx] + ":" + com.crushftp.client.Common.format_bytes_short(ids[xx].length()) + ", its been sitting idle:" + delay + "ms");
                                                    AdminControls.startJob(f, true, new StringBuffer(ids[xx].getName().substring(0, ids[xx].getName().lastIndexOf("."))), null);
                                                }
                                            }
                                        }
                                        ++xx;
                                    }
                                }
                                --x;
                            }
                            break;
                        }
                    }
                    catch (Exception e) {
                        if (("" + e).indexOf("Interrupted") < 0) {
                            Log.log("SERVER", 0, e);
                            continue;
                        }
                        return;
                    }
                    break;
                }
            }
        });
        this.jobs_resumer_thread.setName("ServerStatus:jobs_resumer:");
        this.jobs_resumer_thread.setPriority(1);
        this.jobs_resumer_thread.start();
    }

    public void setup_report_scheduler() {
        try {
            if (this.report_scheduler_thread != null) {
                int loops = 0;
                while (this.report_scheduler_thread.isAlive() && loops++ < 100) {
                    this.report_scheduler_thread.interrupt();
                    Thread.sleep(100L);
                }
            }
        }
        catch (Exception loops) {
            // empty catch block
        }
        this.report_scheduler_thread = null;
        UpdateTimer the_thread = new UpdateTimer(this, 40000, "ServerStatus", "report_scheduler");
        this.report_scheduler_thread = new Thread(the_thread);
        this.report_scheduler_thread.setName("ServerStatus:report_scheduler:");
        this.report_scheduler_thread.setPriority(1);
        this.report_scheduler_thread.start();
    }

    public void setup_scheduler() {
        try {
            this.scheduler_thread.interrupt();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.scheduler_thread = null;
        UpdateTimer the_thread = new UpdateTimer(this, 1000, "ServerStatus", "schedules");
        this.scheduler_thread = new Thread(the_thread);
        this.scheduler_thread.setName("ServerStatus:schedules:");
        this.scheduler_thread.setPriority(1);
        this.scheduler_thread.start();
    }

    public void setup_alerts() {
        try {
            this.alerts_thread.interrupt();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.alerts_thread = null;
        UpdateTimer the_thread = new UpdateTimer(this, 60000, "ServerStatus", "alerts");
        this.alerts_thread = new Thread(the_thread);
        this.alerts_thread.setName("ServerStatus:alerts:");
        this.alerts_thread.setPriority(1);
        this.alerts_thread.start();
    }

    public void setup_new_version() {
        try {
            this.new_version_thread.interrupt();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.new_version_thread = null;
        UpdateTimer the_thread = new UpdateTimer(this, 60000, "ServerStatus", "new_version");
        this.new_version_thread = new Thread(the_thread);
        this.new_version_thread.setName("ServerStatus:new_version:");
        this.new_version_thread.setPriority(1);
        this.new_version_thread.start();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void hold_user_pointer(Properties user_info) {
        Object object = SharedSession.sessionLock;
        synchronized (object) {
            Object object2 = this.userListLock;
            synchronized (object2) {
                if (ServerStatus.siVG("user_list").indexOf(user_info) < 0 && !ServerStatus.siPG("user_list_prop").containsKey(user_info.getProperty("id"))) {
                    ServerStatus.siVG("user_list").addElement(user_info);
                    ServerStatus.siPG("user_list_prop").put(user_info.getProperty("id"), "");
                    ServerStatus.siPUT("logged_in_users", "" + ServerStatus.siVG("user_list").size());
                    this.updateConcurrentUsers();
                }
                ServerStatus.siVG("recent_user_list").remove(user_info);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void updateConcurrentUsers() {
        if (ServerStatus.IG("max_max_users") > 99) {
            ServerStatus.siPUT("concurrent_users", "-1");
            return;
        }
        Properties uniqueSessions = new Properties();
        Properties concurrentUsers = new Properties();
        Object object = SharedSession.sessionLock;
        synchronized (object) {
            Object object2 = this.userListLock;
            synchronized (object2) {
                int x = ServerStatus.siVG("user_list").size() - 1;
                while (x >= 0) {
                    Properties p = (Properties)ServerStatus.siVG("user_list").elementAt(x);
                    SessionCrush theSession = (SessionCrush)p.get("session");
                    if (theSession != null) {
                        String protocol = theSession.uiSG("user_protocol");
                        float cur = Float.parseFloat(concurrentUsers.getProperty(String.valueOf(protocol) + ":" + theSession.uiSG("user_ip"), "0"));
                        String sessionID = theSession.uiSG("sessionID");
                        if (sessionID == null) {
                            sessionID = "";
                        }
                        if ((protocol.toUpperCase().startsWith("HTTP") || protocol.toUpperCase().startsWith("WEBDAV")) && uniqueSessions.containsKey(sessionID)) {
                            cur += 0.25f;
                        } else {
                            cur += 1.0f;
                            uniqueSessions.put(sessionID, "");
                        }
                        concurrentUsers.put(String.valueOf(protocol) + ":" + theSession.uiSG("user_ip"), String.valueOf(cur));
                    }
                    --x;
                }
            }
        }
        Enumeration<Object> e = concurrentUsers.keys();
        float total = 0.0f;
        while (e.hasMoreElements()) {
            String key = e.nextElement().toString();
            String val = concurrentUsers.getProperty(key);
            total += Float.parseFloat(val);
        }
        if (total > 0.0f && total < 1.0f) {
            total = 1.0f;
        }
        ServerStatus.siPUT("concurrent_users", String.valueOf((int)total));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void set_user_pointer(Properties user_info) {
        String ip_text = user_info.getProperty("user_ip");
        ip_text = ip_text.substring(ip_text.indexOf("/") + 1, ip_text.length());
        this.server_info.put("last_login_user", user_info.getProperty("user_name"));
        this.server_info.put("last_login_date_time", user_info.getProperty("login_date_formatted"));
        this.server_info.put("last_login_ip", ip_text);
        final Properties p = new Properties();
        p.put("user_name", user_info.getProperty("user_name"));
        p.put("login_date_formatted", user_info.getProperty("login_date_formatted"));
        p.put("ip", ip_text);
        p.put("dns", "");
        Object object = this.loginsLock;
        synchronized (object) {
            int x = ServerStatus.siVG("last_logins").size() - 1;
            while (x >= 0) {
                try {
                    Properties pp = (Properties)ServerStatus.siVG("last_logins").elementAt(x);
                    if (pp.getProperty("user_name").equals(p.getProperty("user_name")) && pp.getProperty("ip").equals(p.getProperty("ip"))) {
                        Properties ppp = (Properties)ServerStatus.siVG("last_logins").remove(x);
                        p.put("dns", ppp.getProperty("dns", ""));
                    }
                }
                catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
                    // empty catch block
                }
                --x;
            }
        }
        if (!ServerStatus.BG("reverse_dns_user_ip")) {
            p.put("dns", "d.isab.led");
        }
        if (p.getProperty("dns", "").equals("")) {
            try {
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        try {
                            p.put("dns", "");
                            p.put("dns", InetAddress.getByName(p.getProperty("ip")).getCanonicalHostName());
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                });
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        ServerStatus.siVG("last_logins").addElement(p);
        while (ServerStatus.siVG("last_logins").size() > 20) {
            ServerStatus.siVG("last_logins").remove(0);
        }
    }

    public int count_users(SessionCrush this_user) {
        int num_users = 0;
        Vector v = (Vector)ServerStatus.siVG("user_list").clone();
        int x = v.size() - 1;
        while (x >= 0) {
            try {
                Properties p = (Properties)v.elementAt(x);
                if (p.getProperty("user_name").equalsIgnoreCase(this_user.uiSG("user_name"))) {
                    ++num_users;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            --x;
        }
        return num_users;
    }

    public static int count_users_ip(SessionCrush this_user, String protocol) {
        int num_users = 0;
        Vector v = (Vector)ServerStatus.siVG("user_list").clone();
        int x = v.size() - 1;
        while (x >= 0) {
            try {
                Properties p = (Properties)v.elementAt(x);
                if (p.getProperty("user_name").equalsIgnoreCase(this_user.uiSG("user_name")) && p.getProperty("user_ip").equalsIgnoreCase(this_user.uiSG("user_ip")) && (protocol == null || protocol != null && p.getProperty("user_protocol").equals(protocol))) {
                    ++num_users;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            --x;
        }
        return num_users;
    }

    public boolean kill_first_same_name_same_ip(Properties user_info) {
        Vector v = (Vector)ServerStatus.siVG("user_list").clone();
        int x = 0;
        while (x < v.size()) {
            block5: {
                try {
                    Properties p = (Properties)v.elementAt(x);
                    if (!p.getProperty("user_name").equalsIgnoreCase(user_info.getProperty("user_name")) || !p.getProperty("user_ip").equalsIgnoreCase(user_info.getProperty("user_ip"))) break block5;
                    try {
                        this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|---" + LOC.G("Kicking first same usernames with this IP") + "--- " + user_info.getProperty("user_name") + ":" + user_info.getProperty("user_ip"), "KICK");
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    this.kick(p);
                    return true;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            ++x;
        }
        return false;
    }

    public boolean kill_same_name_same_ip(Properties user_info, boolean logit) {
        boolean user_kicked = false;
        Vector v = (Vector)ServerStatus.siVG("user_list").clone();
        int x = v.size() - 1;
        while (x >= 0) {
            block5: {
                try {
                    Properties p = (Properties)v.elementAt(x);
                    if ((!p.getProperty("user_name").equalsIgnoreCase(user_info.getProperty("user_name")) || !p.getProperty("user_ip").equalsIgnoreCase(user_info.getProperty("user_ip"))) && !p.getProperty("CrushAuth", "1").equalsIgnoreCase(user_info.getProperty("CrushAuth", "2"))) break block5;
                    try {
                        this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|---" + LOC.G("Kicking usernames with this IP") + "--- " + user_info.getProperty("user_name") + ":" + user_info.getProperty("user_ip"), "KICK");
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    this.kick(p, logit);
                    user_kicked = true;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            --x;
        }
        return user_kicked;
    }

    public boolean kill_same_ip(String ip, boolean logit) {
        boolean user_kicked = false;
        Vector v = (Vector)ServerStatus.siVG("user_list").clone();
        int x = v.size() - 1;
        while (x >= 0) {
            block5: {
                try {
                    Properties p = (Properties)v.elementAt(x);
                    if (!p.getProperty("user_ip").equalsIgnoreCase(ip)) break block5;
                    try {
                        this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|---" + LOC.G("Kicking usernames with this IP") + "--- " + ip, "KICK");
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    this.kick(p, logit);
                    user_kicked = true;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            --x;
        }
        return user_kicked;
    }

    public void remove_user(Properties user_info) {
        this.remove_user(user_info, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void remove_user(Properties user_info, boolean decrementServerCount) {
        block31: {
            Object x2;
            SessionCrush session = null;
            boolean removed = false;
            try {
                int x2 = 0;
                while (x2 < this.main_servers.size()) {
                    GenericServer s = (GenericServer)this.main_servers.elementAt(x2);
                    if (s.server_item.getProperty("ip").equals(((SessionCrush)user_info.get((Object)"session")).server_item.getProperty("ip")) && s.server_item.getProperty("port").equals(((SessionCrush)user_info.get((Object)"session")).server_item.getProperty("port"))) {
                        GenericServer genericServer = s;
                        synchronized (genericServer) {
                            if (s.connected_users > 0 && decrementServerCount) {
                                --s.connected_users;
                            }
                            s.updateStatus();
                        }
                    }
                    ++x2;
                }
            }
            catch (Exception x2) {
                // empty catch block
            }
            try {
                session = (SessionCrush)user_info.get("session");
                x2 = SharedSession.sessionLock;
                synchronized (x2) {
                    Object object = this.userListLock;
                    synchronized (object) {
                        while (ServerStatus.siVG("user_list").indexOf(user_info) >= 0) {
                            removed = ServerStatus.siVG("user_list").remove(user_info);
                        }
                        if (ServerStatus.siPG("user_list_prop").containsKey(user_info.getProperty("id"))) {
                            ServerStatus.siPG("user_list_prop").remove(user_info.getProperty("id"));
                            removed = true;
                            int x3 = ServerStatus.siVG("user_list").size() - 1;
                            while (x3 >= 0) {
                                Properties ui_tmp = (Properties)ServerStatus.siVG("user_list").elementAt(x3);
                                if (ui_tmp.getProperty("id").equals(user_info.getProperty("id"))) {
                                    ServerStatus.siVG("user_list").remove(x3);
                                }
                                --x3;
                            }
                        }
                    }
                }
                user_info.put("root_dir", session.user.getProperty("root_dir"));
            }
            catch (Exception x2) {
                // empty catch block
            }
            ServerStatus.siPUT("logged_in_users", "" + ServerStatus.siVG("user_list").size());
            this.updateConcurrentUsers();
            try {
                if (!removed) break block31;
                try {
                    session.drain_log();
                }
                catch (NullPointerException x2) {
                    // empty catch block
                }
                if (LoggingProvider.checkFilters(ServerStatus.SG("filter_log_text"), String.valueOf(user_info.getProperty("user_ip")) + ":" + user_info.getProperty("user_name"))) {
                    x2 = SharedSession.sessionLock;
                    synchronized (x2) {
                        user_info.put("root_dir", "/");
                        ServerStatus.siVG("recent_user_list").remove(user_info);
                        if (ServerStatus.siVG("recent_user_list").indexOf(user_info) < 0 && !user_info.getProperty("hack_username", "false").equals("true")) {
                            ServerStatus.siVG("recent_user_list").addElement(user_info);
                        }
                    }
                }
                try {
                    user_info.put("root_dir", session.user.getProperty("root_dir"));
                }
                catch (NullPointerException x2) {}
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        }
        user_info.put("user_log", new Vector());
    }

    public int getTotalConnectedUsers() {
        int total = 0;
        try {
            total = ServerStatus.siIG("concurrent_users");
        }
        catch (Exception exception) {
            // empty catch block
        }
        return total;
    }

    public void stop_all_servers_including_serverbeat() {
        int x = this.main_servers.size() - 1;
        while (x >= 0) {
            this.stop_this_server(x);
            --x;
        }
    }

    public void stop_all_servers() {
        int x = this.main_servers.size() - 1;
        while (x >= 0) {
            GenericServer gs = (GenericServer)this.main_servers.elementAt(x);
            if (!(gs instanceof ServerBeat)) {
                this.stop_this_server(x);
            }
            --x;
        }
    }

    public void kick_all_users() {
        try {
            this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|---" + LOC.G("Kicking all users.") + "---", "KICK");
        }
        catch (Exception exception) {
            // empty catch block
        }
        Vector v = (Vector)ServerStatus.siVG("user_list").clone();
        int x = v.size() - 1;
        while (x >= 0) {
            Properties p = (Properties)v.elementAt(x);
            this.kick(p);
            --x;
        }
        ServerStatus.siPUT("logged_in_users", "0");
        this.updateConcurrentUsers();
    }

    public void start_all_servers() {
        this.start_all_servers(false);
    }

    public void start_all_servers(boolean starts_down_only) {
        try {
            Vector the_server_list = null;
            try {
                the_server_list = (Vector)server_settings.get("server_list");
            }
            catch (Exception e) {
                the_server_list = (Vector)this.default_settings.get("server_list");
                server_settings.put("server_list", the_server_list);
            }
            int x = 0;
            while (x < the_server_list.size()) {
                Properties server_item = (Properties)the_server_list.elementAt(x);
                if (!starts_down_only || !server_item.getProperty("running", "false").equals("true")) {
                    this.start_this_server(x);
                }
                ++x;
            }
            this.setup_discover_ip_refresh();
        }
        catch (Exception ee) {
            Log.log("SERVER", 0, ee);
        }
    }

    public void doServerAction(Properties p) {
        Vector the_server_list = (Vector)server_settings.get("server_list");
        if (p.getProperty("action").equals("create")) {
            Properties server_item = new Properties();
            server_item.put("serverType", p.getProperty("protocol").toUpperCase());
            server_item.put("ip", p.getProperty("ip", "lookup"));
            server_item.put("port", p.getProperty("port"));
            server_item.put("require_encryption", "false");
            server_item.put("https_redirect", "false");
            server_item.put("explicit_ssl", "false");
            server_item.put("explicit_tls", "false");
            server_item.put("http", "false");
            server_item.put("server_ip", "auto");
            server_item.put("pasv_ports", "1025-65535");
            server_item.put("ftp_aware_router", "false");
            if (p.containsKey("port_id")) {
                server_item.put("port_id", p.getProperty("port_id"));
            }
            this.common_code.setServerStatus(server_item, server_item.getProperty("ip", "lookup"));
            the_server_list.addElement(server_item);
            thisObj.save_server_settings(true);
            p.put("server_item", server_item);
            return;
        }
        Log.log("SERVER", 3, "the_server_list:" + the_server_list);
        int x = 0;
        while (x < the_server_list.size()) {
            Log.log("SERVER", 3, "ServerAction:" + p.toString());
            Properties pp = (Properties)the_server_list.elementAt(x);
            Log.log("SERVER", 3, "ServerAction2:" + pp.toString());
            if (pp.getProperty("port_id", "a").equals(p.getProperty("port_id", "b")) || pp.getProperty("serverType", "a").equalsIgnoreCase(p.getProperty("protocol", "b")) && pp.getProperty("ip", "lookup").equalsIgnoreCase(p.getProperty("ip", "lookup")) && pp.getProperty("port", "-3").equalsIgnoreCase(p.getProperty("port", "-2"))) {
                if (p.getProperty("action").equals("start")) {
                    Log.log("SERVER", 2, "starting server:" + pp);
                    Thread t = this.start_this_server(x);
                    p.put("thread", t);
                    thisObj.save_server_settings(true);
                    break;
                }
                if (p.getProperty("action").equals("stop")) {
                    Log.log("SERVER", 2, "stopping server:" + pp);
                    Thread t = this.stop_this_server(x);
                    p.put("thread", t);
                    thisObj.save_server_settings(true);
                    break;
                }
                if (p.getProperty("action").equals("query")) {
                    p.put("server_item", pp);
                    break;
                }
                if (p.getProperty("action").equals("delete")) {
                    try {
                        Thread t = this.stop_this_server(x);
                        p.put("thread", t);
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 1, e);
                    }
                    the_server_list.removeElement(pp);
                    this.main_servers.removeElementAt(x);
                    thisObj.save_server_settings(true);
                    break;
                }
            }
            ++x;
        }
    }

    public Thread start_this_server(int x) {
        Vector the_server_list = (Vector)server_settings.get("server_list");
        Properties server_item = (Properties)the_server_list.elementAt(x);
        if (this.main_servers.size() - 1 < x) {
            this.main_servers.addElement(GenericServer.buildServer(server_item));
        } else {
            try {
                GenericServer gs = (GenericServer)this.main_servers.elementAt(x);
                if (gs instanceof ServerBeat && gs.thread != null && gs.thread.isAlive()) {
                    return gs.thread;
                }
                this.stop_this_server(x);
            }
            catch (Exception gs) {
                // empty catch block
            }
            this.main_servers.setElementAt(GenericServer.buildServer(server_item), x);
        }
        Thread t = new Thread((GenericServer)this.main_servers.elementAt(x));
        t.setName("main_server_thread:" + server_item.getProperty("ip") + ":" + Integer.parseInt(server_item.getProperty("port")));
        t.setPriority(10);
        t.start();
        return t;
    }

    public Thread stop_this_server(int x) {
        String ip = ((GenericServer)this.main_servers.elementAt((int)x)).listen_ip;
        try {
            if (ip.equals("lookup")) {
                ip = com.crushftp.client.Common.getLocalIP();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|---" + LOC.G("Server Stopped") + "--- LAN IP=" + ip + " WAN IP=" + ServerStatus.SG("discovered_ip") + " PORT=" + ((GenericServer)this.main_servers.elementAt((int)x)).listen_port, "STOP");
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            ((GenericServer)this.main_servers.elementAt((int)x)).die_now.append(System.currentTimeMillis());
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            ((GenericServer)this.main_servers.elementAt((int)x)).thread.interrupt();
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            ((GenericServer)this.main_servers.elementAt((int)x)).server_sock.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            return ((GenericServer)this.main_servers.elementAt((int)x)).thread;
        }
        catch (Exception exception) {
            return null;
        }
    }

    public void server_started(String ip, int the_port) {
        try {
            if (ip.equals("lookup")) {
                ip = InetAddress.getLocalHost().getHostAddress();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.logDateFormat = new SimpleDateFormat(ServerStatus.SG("log_date_format"), Locale.US);
        if (ServerStatus.SG("discovered_ip").equals("0.0.0.0")) {
            try {
                this.update_now("discover_ip_timer");
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (ServerStatus.SG("discovered_ip").equals("0.0.0.0")) {
            try {
                server_settings.put("discovered_ip", InetAddress.getLocalHost().getHostAddress());
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        try {
            this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|---" + LOC.G("Server Started") + "--- LAN IP=" + ip + " WAN IP=" + ServerStatus.SG("discovered_ip") + " PORT=" + the_port, "START");
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void update_now(String arg) throws Exception {
        if (arg.equals("hammer_timer")) {
            ServerStatus.siPUT("hammer_history", "");
        } else if (arg.equals("hammer_timer_http")) {
            ServerStatus.siPUT("hammer_history_http", "");
        } else if (arg.equals("phammer_timer")) {
            Vector password_attempts;
            Properties user_info2;
            Properties ips = new Properties();
            Vector v = (Vector)ServerStatus.siVG("user_list").clone();
            int x = v.size() - 1;
            while (x >= 0) {
                try {
                    user_info2 = (Properties)v.elementAt(x);
                    password_attempts = (Vector)user_info2.get("password_attempts");
                    if (ips.get(user_info2.getProperty("user_ip")) == null) {
                        ips.put(user_info2.getProperty("user_ip"), new Vector());
                        ((Vector)ips.get(user_info2.getProperty("user_ip"))).add(user_info2);
                    }
                    ((Vector)ips.get(user_info2.getProperty("user_ip"))).addAll(password_attempts);
                }
                catch (Exception user_info2) {
                    // empty catch block
                }
                --x;
            }
            x = 0;
            while (x < ServerStatus.siVG("recent_user_list").size()) {
                try {
                    user_info2 = (Properties)ServerStatus.siVG("recent_user_list").elementAt(x);
                    password_attempts = (Vector)user_info2.get("password_attempts");
                    if (ips.get(user_info2.getProperty("user_ip")) == null) {
                        ips.put(user_info2.getProperty("user_ip"), new Vector());
                        ((Vector)ips.get(user_info2.getProperty("user_ip"))).add(user_info2);
                    }
                    ((Vector)ips.get(user_info2.getProperty("user_ip"))).addAll(password_attempts);
                }
                catch (Exception user_info3) {
                    // empty catch block
                }
                ++x;
            }
            int phammer_attempts = (int)ServerStatus.get_partial_val_or_all("phammer_attempts", 1);
            long phammer_banning = ServerStatus.get_partial_val_or_all("phammer_banning", 1);
            int pban_timeout = (int)ServerStatus.get_partial_val_or_all("pban_timeout", 1);
            Enumeration<Object> keys = ips.keys();
            while (keys.hasMoreElements()) {
                Properties p2;
                String ip = keys.nextElement().toString();
                Vector password_attempts2 = (Vector)ips.get(ip);
                int count = 0;
                int x22 = 1;
                while (x22 < password_attempts2.size()) {
                    long time = Long.parseLong(password_attempts2.elementAt(x22).toString());
                    if (time > new Date().getTime() - 1000L * phammer_banning) {
                        ++count;
                    }
                    ++x22;
                }
                if (count <= phammer_attempts) continue;
                Log.log("SERVER", 2, "Attempting to BAN IP for excessive failed logins.");
                if (!this.ban((Properties)password_attempts2.elementAt(0), pban_timeout, true, "password attempts")) continue;
                try {
                    this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|---" + LOC.G("Kicking session because of password hammer trigger.") + "---", "KICK");
                }
                catch (Exception x22) {
                    // empty catch block
                }
                Properties user_info4 = (Properties)password_attempts2.elementAt(0);
                SessionCrush thisSession = null;
                try {
                    thisSession = (SessionCrush)user_info4.get("session");
                }
                catch (Exception e) {
                    Log.log("BAN", 1, e);
                }
                this.kick(user_info4);
                try {
                    Vector user_log;
                    Properties user = null;
                    String username = "";
                    boolean fire_event = false;
                    if (thisSession != null) {
                        user = thisSession.user;
                    }
                    if (user != null) {
                        username = user.getProperty("user_name", "");
                    }
                    if (username.equals("") && user_info4 != null && !user_info4.getProperty("user_name", "").equals("")) {
                        username = user_info4.getProperty("user_name", "");
                    } else if (username.equals("") && user_info4 != null && !user_info4.getProperty("username", "").equals("")) {
                        username = user_info4.getProperty("username", "");
                    } else if (username.equals("") && user_info4 != null && user_info4.getProperty("user_name", "").equals("") && user_info4.containsKey("request")) {
                        Properties request = (Properties)user_info4.get("request");
                        username = request.getProperty("username", "");
                    } else if (username.equals("") && user_info4 != null && (user_log = (Vector)user_info4.get("user_log")) != null && user_log.size() > 0) {
                        boolean found = false;
                        int x3 = user_log.size() - 1;
                        while (x3 >= 0) {
                            Object o = user_log.get(x3);
                            if (o != null && o instanceof String && o.toString().startsWith("Password attempt. Username :")) {
                                username = o.toString().substring("Password attempt. Username :".length());
                                found = true;
                                break;
                            }
                            --x3;
                        }
                    }
                    Log.log("BAN", 1, "Failed logins alert info : Fire event : " + fire_event + " Username : " + username);
                    if (!fire_event && !username.equals("")) {
                        Log.log("BAN", 1, "Failed logins alert info : Check if user exits : " + username);
                        String connectionGroup = "MainUsers";
                        if (thisSession != null && thisSession.server_item != null) {
                            connectionGroup = thisSession.server_item.getProperty("linkedServer");
                        }
                        if ((user = UserTools.ut.getUser(connectionGroup, username, false)) != null) {
                            user.put("alert_timeout", String.valueOf(pban_timeout));
                            user_info4.put("alert_timeout", String.valueOf(pban_timeout));
                            user_info4.put("user_name", username);
                            fire_event = true;
                            Log.log("BAN", 1, "Failed logins alert info :  Found user : " + username);
                        } else {
                            Log.log("BAN", 1, "Failed logins alert info :  User does not exits : " + username);
                        }
                    }
                    if (fire_event) {
                        Log.log("BAN", 1, "Failed logins alert info : Run the alert!");
                        AlertTools.runAlerts("ip_banned_logins", user, user_info4, user, null, null, com.crushftp.client.Common.dmz_mode);
                    }
                }
                catch (Exception e) {
                    Log.log("BAN", 1, e);
                }
                try {
                    Properties info = new Properties();
                    info.put("alert_type", "hammering");
                    info.put("alert_sub_type", "password");
                    info.put("alert_timeout", String.valueOf(pban_timeout));
                    info.put("alert_max", String.valueOf(phammer_attempts));
                    info.put("alert_msg", user_info4.getProperty("user_name"));
                    this.runAlerts("security_alert", info, user_info4, thisSession);
                }
                catch (Exception e) {
                    Log.log("BAN", 1, e);
                }
                v = (Vector)ServerStatus.siVG("user_list").clone();
                int x4 = v.size() - 1;
                while (x4 >= 0) {
                    try {
                        p2 = (Properties)v.elementAt(x4);
                        if (p2.getProperty("user_ip").equals(ip)) {
                            ((Vector)p2.get("password_attempts")).removeAllElements();
                        }
                    }
                    catch (Exception p2) {
                        // empty catch block
                    }
                    --x4;
                }
                x4 = 0;
                while (x4 < ServerStatus.siVG("recent_user_list").size()) {
                    try {
                        p2 = (Properties)ServerStatus.siVG("recent_user_list").elementAt(x4);
                        if (p2.getProperty("user_ip").equals(ip)) {
                            ((Vector)p2.get("password_attempts")).removeAllElements();
                        }
                    }
                    catch (Exception p3) {
                        // empty catch block
                    }
                    ++x4;
                }
            }
            this.runAlerts("user_hammering", null);
        } else if (arg.equals("report_scheduler")) {
            this.rt.runScheduledReports(server_settings, this.server_info);
        } else if (arg.equals("schedules")) {
            try {
                String last_m = this.server_info.getProperty("job_scheduler_last_run_mm", "");
                SimpleDateFormat mm = new SimpleDateFormat("mm");
                String current_m = mm.format(new Date());
                if (!last_m.equals(current_m)) {
                    this.server_info.put("job_scheduler_last_run_mm", current_m);
                    Thread.sleep(3000L);
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            JobScheduler.runSchedules(new Properties());
                        }
                    }, "Scanning for jobs that need to be run...(every minute) " + new Date());
                }
            }
            catch (Exception e) {
                System.out.println("" + new Date());
                e.printStackTrace();
                Log.log("SERVER", 0, e);
            }
        } else if (arg.equals("alerts")) {
            if (this.server_info.get("recent_drives") != null) {
                ((Properties)this.server_info.get("recent_drives")).clear();
            }
            this.runAlerts("disk", null);
            this.runAlerts("variables", null);
        } else if (arg.equals("new_version")) {
            if (ServerStatus.SG("newversion") == null || ServerStatus.BG("newversion")) {
                try {
                    Thread.sleep(1000L);
                }
                catch (Exception e) {
                    // empty catch block
                }
                this.doCheckForUpdate(false);
                try {
                    Thread.sleep(259200000L);
                }
                catch (Exception e) {}
            }
        } else {
            if (arg.equals("stats_saver")) {
                Thread.sleep(10000L);
                Object e = this.stats_saver_lock;
                synchronized (e) {
                    long last_stats_time = Long.parseLong(this.server_info.getProperty("last_stats_time", "0"));
                    if (System.currentTimeMillis() - last_stats_time > 30000L) {
                        if (ServerBeat.current_master && !ServerStatus.BG("disable_stats")) {
                            last_stats_time = System.currentTimeMillis();
                            GregorianCalendar c = new GregorianCalendar();
                            c.setTime(new Date());
                            ((Calendar)c).add(5, ServerStatus.IG("stats_transfer_days") * -1);
                            int last_transfer_rids_size = 1;
                            while (last_transfer_rids_size > 0) {
                                DVector transfer_rids = this.statTools.executeSqlQuery(ServerStatus.SG("stats_get_transfers_time"), new Object[]{c.getTime()}, false, new Properties(), 1000);
                                last_transfer_rids_size = transfer_rids.size();
                                Log.log("STATISTICS", 2, "Stats Transfer Cleanup: Deleting transfer meta " + transfer_rids.size() + " items.");
                                Thread.currentThread().setName("ServerStatus:stats_saver:stats_get_transfers_time:" + last_transfer_rids_size + ":start_time=" + new Date(last_stats_time) + ":elapsed=" + (System.currentTimeMillis() - last_stats_time) / 1000L + "secs");
                                StringBuffer transferRidsStr = new StringBuffer();
                                int x = 0;
                                while (x < transfer_rids.size()) {
                                    Properties p = (Properties)transfer_rids.elementAt(x);
                                    if (x > 0) {
                                        transferRidsStr.append(",");
                                    }
                                    transferRidsStr.append(p.getProperty("RID"));
                                    ++x;
                                }
                                transfer_rids.close();
                                if (transferRidsStr.length() > 0) {
                                    this.statTools.executeSql(Common.replace_str(ServerStatus.SG("stats_delete_meta_transfers"), "%transfers%", transferRidsStr.toString()), new Object[0]);
                                }
                                Thread.sleep(1000L);
                                if (transferRidsStr.toString().length() <= 0) continue;
                                Thread.currentThread().setName("ServerStatus:stats_saver:stats_delete_transfers_time:" + last_transfer_rids_size + ":start_time=" + new Date(last_stats_time) + ":elapsed=" + (System.currentTimeMillis() - last_stats_time) / 1000L + "secs");
                                String stats_delete_transfers_time = ServerStatus.SG("stats_delete_transfers_time");
                                stats_delete_transfers_time = stats_delete_transfers_time.indexOf(" and RID ") < 0 ? String.valueOf(stats_delete_transfers_time) + " and RID IN (" + transferRidsStr.toString() + ")" : Common.replace_str(stats_delete_transfers_time, "%transfers%", transferRidsStr.toString());
                                this.statTools.executeSql(stats_delete_transfers_time, new Object[]{c.getTime()});
                            }
                            c = new GregorianCalendar();
                            c.setTime(new Date());
                            ((Calendar)c).add(5, ServerStatus.IG("stats_session_days") * -1);
                            int last_session_rids_size = 1;
                            while (last_session_rids_size > 0) {
                                DVector session_rids = this.statTools.executeSqlQuery(ServerStatus.SG("stats_get_sessions_time"), new Object[]{c.getTime()}, false, new Properties(), 1000);
                                last_session_rids_size = session_rids.size();
                                Log.log("STATISTICS", 2, "Stats Transfer Cleanup: Deleting sessions " + session_rids.size() + " items.");
                                Thread.currentThread().setName("ServerStatus:stats_saver:stats_get_sessions_time:" + last_transfer_rids_size + ":start_time=" + new Date(last_stats_time) + ":elapsed=" + (System.currentTimeMillis() - last_stats_time) / 1000L + "secs");
                                StringBuffer sessionRidsStr = new StringBuffer();
                                int x = 0;
                                while (x < session_rids.size()) {
                                    Properties p = (Properties)session_rids.elementAt(x);
                                    if (x > 0) {
                                        sessionRidsStr.append(",");
                                    }
                                    sessionRidsStr.append(p.getProperty("RID"));
                                    ++x;
                                }
                                session_rids.close();
                                DVector transfer_rids = new DVector();
                                if (sessionRidsStr.length() > 0) {
                                    transfer_rids = this.statTools.executeSqlQuery(Common.replace_str(ServerStatus.SG("stats_get_transfers_sessions"), "%sessions%", sessionRidsStr.toString()), new Object[0], false, new Properties(), 1000);
                                }
                                Thread.currentThread().setName("ServerStatus:stats_saver:stats_get_transfers_sessions:" + transfer_rids.size() + ":start_time=" + new Date(last_stats_time) + ":elapsed=" + (System.currentTimeMillis() - last_stats_time) / 1000L + "secs");
                                StringBuffer transferRidsStr = new StringBuffer();
                                int x5 = 0;
                                while (x5 < transfer_rids.size()) {
                                    Properties p = (Properties)transfer_rids.elementAt(x5);
                                    if (x5 > 0) {
                                        transferRidsStr.append(",");
                                    }
                                    transferRidsStr.append(p.getProperty("RID"));
                                    ++x5;
                                }
                                Thread.currentThread().setName("ServerStatus:stats_saver:stats_delete_meta_transfers:" + transfer_rids.size() + ":start_time=" + new Date(last_stats_time) + ":elapsed=" + (System.currentTimeMillis() - last_stats_time) / 1000L + "secs");
                                if (transferRidsStr.length() > 0) {
                                    this.statTools.executeSql(Common.replace_str(ServerStatus.SG("stats_delete_meta_transfers"), "%transfers%", transferRidsStr.toString()), new Object[0]);
                                }
                                transfer_rids.close();
                                Thread.sleep(1000L);
                                Thread.currentThread().setName("ServerStatus:stats_saver:stats_delete_sessions_time:start_time=" + new Date(last_stats_time) + ":elapsed=" + (System.currentTimeMillis() - last_stats_time) / 1000L + "secs");
                                if (sessionRidsStr.toString().length() <= 0) continue;
                                String stats_delete_sessions_time = ServerStatus.SG("stats_delete_sessions_time");
                                stats_delete_sessions_time = stats_delete_sessions_time.indexOf(" and RID ") < 0 ? String.valueOf(stats_delete_sessions_time) + " and RID IN (" + sessionRidsStr.toString() + ")" : Common.replace_str(stats_delete_sessions_time, "%sessions%", sessionRidsStr.toString());
                                this.statTools.executeSql(stats_delete_sessions_time, new Object[]{c.getTime()});
                            }
                        }
                        this.checkCrushExpiration();
                        if (ServerStatus.BG("allow_session_caching")) {
                            SharedSession.flush();
                        }
                        this.server_info.put("last_stats_time", String.valueOf(System.currentTimeMillis()));
                    }
                    Thread.currentThread().setName("ServerStatus:stats_saver:DONE:start_time=" + new Date(last_stats_time) + ":elapsed=" + (System.currentTimeMillis() - last_stats_time) / 1000L + "secs");
                }
            }
            if (arg.equals("ban_timer")) {
                Vector ip_vec = (Vector)server_settings.get("ip_restrictions");
                this.common_code.remove_expired_bans(ip_vec);
                server_settings.put("ip_restrictions", ip_vec);
                this.common_code.remove_expired_bans(ServerStatus.siVG("ip_restrictions_temp"));
            } else if (arg.equals("cban_timer")) {
                Vector<Properties> kick_list = new Vector<Properties>();
                int x = 0;
                while (x < ServerStatus.siVG("user_list").size()) {
                    try {
                        Properties user_info = (Properties)ServerStatus.siVG("user_list").elementAt(x);
                        if (user_info != null) {
                            int search_loc = -1;
                            Vector ip_list = (Vector)server_settings.get("ip_restrictions");
                            int loop = 0;
                            while (loop < ip_list.size()) {
                                Properties ip_data = (Properties)ip_list.elementAt(loop);
                                if ((String.valueOf(ip_data.getProperty("start_ip")) + "," + ip_data.getProperty("stop_ip")).equals(user_info.get("user_ip") + "," + user_info.get("user_ip"))) {
                                    search_loc = loop;
                                    break;
                                }
                                ++loop;
                            }
                            if (search_loc < 0) {
                                long time_now = new Date().getTime();
                                int xx = 0;
                                while (xx < ((Vector)user_info.get("failed_commands")).size()) {
                                    long the_time = Long.parseLong("" + ((Vector)user_info.get("failed_commands")).elementAt(xx));
                                    if (time_now - the_time > (long)(ServerStatus.IG("chammer_banning") * 1000)) {
                                        ((Vector)user_info.get("failed_commands")).removeElementAt(xx);
                                        continue;
                                    }
                                    ++xx;
                                }
                                if (((Vector)user_info.get("failed_commands")).size() >= ServerStatus.IG("chammer_attempts")) {
                                    String ip = user_info.getProperty("user_ip");
                                    if (!this.ban_ip(ip, ServerStatus.IG("cban_timeout"), false, "failed commands")) continue;
                                    ((Vector)user_info.get("failed_commands")).removeAllElements();
                                    try {
                                        this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|---IP " + LOC.G("Banned") + "---:" + ip + " for failed commands.", "BAN");
                                    }
                                    catch (Exception transferRidsStr) {
                                        // empty catch block
                                    }
                                    kick_list.addElement(user_info);
                                    continue;
                                }
                                ++x;
                                continue;
                            }
                            ++x;
                            continue;
                        }
                        ++x;
                    }
                    catch (ArrayIndexOutOfBoundsException user_info) {
                        // empty catch block
                    }
                }
                int xxx = 0;
                while (xxx < kick_list.size()) {
                    try {
                        this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|---" + LOC.G("Kicking sessions because of too many failed commands.") + "---", "KICK");
                    }
                    catch (Exception search_loc) {
                        // empty catch block
                    }
                    Properties user_info = (Properties)kick_list.elementAt(xxx);
                    this.kick(user_info);
                    try {
                        Properties info = new Properties();
                        info.put("alert_type", "hammering");
                        info.put("alert_sub_type", "command");
                        info.put("alert_timeout", String.valueOf(ServerStatus.IG("cban_timeout")));
                        info.put("alert_max", String.valueOf(ServerStatus.IG("chammer_attempts")));
                        info.put("alert_msg", user_info.getProperty("user_name"));
                        this.runAlerts("security_alert", info, user_info, null);
                    }
                    catch (Exception e) {
                        Log.log("BAN", 1, e);
                    }
                    ++xxx;
                }
                Thread.sleep(5000L);
            } else if (arg.equals("discover_ip_timer")) {
                if (ServerStatus.BG("auto_ip_discovery")) {
                    this.update_ip();
                }
                this.geoip.init(ServerStatus.SG("discovered_ip"));
                Thread.sleep(20000L);
            } else if (arg.equals("update_2_timer")) {
                long low_memory_trigger_value3;
                long low_memory_trigger_value2;
                long low_memory_trigger_value1;
                block274: {
                    try {
                        if (this.prefsProvider.getPrefsTime(null) == ServerStatus.siLG("currentFileDate") && this.last_logging_provider2.equalsIgnoreCase(ServerStatus.SG("logging_provider"))) break block274;
                        Thread.sleep(2000L);
                        Object kick_list = GenericServer.updateServerStatuses;
                        synchronized (kick_list) {
                            Properties previousObject = server_settings;
                            Vector pref_server_items = (Vector)server_settings.get("server_list");
                            String prevServeritemsStr = "";
                            int x = 0;
                            while (x < pref_server_items.size()) {
                                Properties the_server = (Properties)((Properties)pref_server_items.elementAt(x)).clone();
                                prevServeritemsStr = GenericServer.getPropertiesHash(the_server);
                                ++x;
                            }
                            prevServeritemsStr = Common.replace_str(prevServeritemsStr, "null", "");
                            this.init_setup(false);
                            Common.updateObjectLog(server_settings, previousObject, null);
                            pref_server_items = (Vector)server_settings.get("server_list");
                            String newServerItemsStr = "";
                            int x6 = 0;
                            while (x6 < pref_server_items.size()) {
                                Properties the_server = (Properties)((Properties)pref_server_items.elementAt(x6)).clone();
                                newServerItemsStr = GenericServer.getPropertiesHash(the_server);
                                ++x6;
                            }
                            newServerItemsStr = Common.replace_str(newServerItemsStr, "null", "");
                            boolean doServerBounce = false;
                            if (!newServerItemsStr.equals(prevServeritemsStr)) {
                                doServerBounce = true;
                            }
                            if (doServerBounce) {
                                this.stop_all_servers();
                            }
                            server_settings = previousObject;
                            pref_server_items = (Vector)server_settings.get("server_list");
                            int x7 = 0;
                            while (x7 < pref_server_items.size()) {
                                Properties the_server = (Properties)pref_server_items.elementAt(x7);
                                if (the_server.containsKey("encryptKeystorePasswords")) {
                                    the_server.remove("encryptKeystorePasswords");
                                    the_server.put("customKeystorePass", this.common_code.encode_pass(the_server.getProperty("customKeystorePass"), "DES", ""));
                                    the_server.put("customKeystoreCertPass", this.common_code.encode_pass(the_server.getProperty("customKeystoreCertPass"), "DES", ""));
                                }
                                ++x7;
                            }
                            this.setup_hammer_banning();
                            this.setup_ban_timer();
                            this.setup_discover_ip_refresh();
                            this.setup_log_rolling();
                            this.setup_http_cleaner();
                            this.setup_stats_saver();
                            this.setup_jobs_resumer();
                            this.setup_report_scheduler();
                            if (doServerBounce) {
                                this.start_all_servers();
                            }
                            this.server_info.put("currentFileDate", String.valueOf(this.prefsProvider.getPrefsTime(null)));
                            this.setupGlobalPrefs();
                        }
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 0, "Prefs.XML was corrupt again.  Could not read changes made...");
                        Log.log("SERVER", 0, e);
                    }
                }
                if (new File_S("./reload_ssl").exists()) {
                    new File_S("./reload_ssl").delete();
                    int x = this.main_servers.size() - 1;
                    while (x >= 0) {
                        GenericServer gs = (GenericServer)this.main_servers.elementAt(x);
                        if (!(gs instanceof ServerBeat)) {
                            if (gs.server_item.getProperty("serverType", "").equalsIgnoreCase("HTTPS") && gs.server_item.getProperty("enabled", "true").equals("true")) {
                                this.start_this_server(x);
                            } else if (gs.server_item.getProperty("serverType", "").equalsIgnoreCase("FTPS") && gs.server_item.getProperty("enabled", "true").equals("true")) {
                                this.start_this_server(x);
                            } else if (gs.server_item.getProperty("serverType", "").toUpperCase().indexOf("DMZ") >= 0 && gs.server_item.getProperty("enabled", "true").equals("true")) {
                                DMZServerCommon.load_and_send_prefs(false, gs.server_item);
                            }
                        }
                        --x;
                    }
                }
                if (new File_S("./ports_restart").exists()) {
                    new File_S("./ports_restart").delete();
                    this.stop_all_servers();
                    this.start_all_servers();
                }
                if (new File_S("./ports_stop").exists()) {
                    new File_S("./ports_stop").delete();
                    this.stop_all_servers();
                }
                if (new File_S("./ports_start").exists()) {
                    new File_S("./ports_start").delete();
                    this.start_all_servers();
                }
                if (new File_S("./restart_idle").exists()) {
                    new File_S("./restart_idle").delete();
                    AdminControls.restartIdle(new Properties(), "(CONNECT)");
                }
                if (new File_S("./shutdown_idle").exists()) {
                    new File_S("./shutdown_idle").delete();
                    AdminControls.shutdownIdle(new Properties(), "(CONNECT)");
                }
                if (new File_S("./stop_logins").exists()) {
                    new File_S("./stop_logins").delete();
                    AdminControls.stopLogins(new Properties(), "(CONNECT)");
                }
                if (new File_S("./" + System.getProperty("appname", "CrushFTP").toLowerCase() + "_restart").exists()) {
                    new File_S("./" + System.getProperty("appname", "CrushFTP").toLowerCase() + "_restart").delete();
                    this.restart_crushftp();
                }
                if (new File_S("./" + System.getProperty("appname", "CrushFTP").toLowerCase() + "_quit").exists()) {
                    new File_S("./" + System.getProperty("appname", "CrushFTP").toLowerCase() + "_quit").delete();
                    this.quit_server(true);
                }
                if (new File_S("./" + System.getProperty("appname", "CrushFTP").toLowerCase() + "_update").exists()) {
                    new File_S("./" + System.getProperty("appname", "CrushFTP").toLowerCase() + "_update").delete();
                    this.do_auto_update_early(false, false);
                }
                if (ServerStatus.siBG("refresh_users")) {
                    Vector v = (Vector)ServerStatus.siVG("user_list").clone();
                    int x = v.size() - 1;
                    while (x >= 0) {
                        try {
                            Properties p = (Properties)v.elementAt(x);
                            p.put("refresh_user", "true");
                        }
                        catch (Exception p) {
                            // empty catch block
                        }
                        --x;
                    }
                    ServerStatus.siPUT("refresh_users", "false");
                }
                if (ServerStatus.siOG("waiting_quit_user_name") != null && System.getProperty("crushftp.security.stop_start", "true").equals("true")) {
                    try {
                        if (ServerStatus.siVG("user_list").indexOf((Properties)ServerStatus.siOG("waiting_quit_user_name")) < 0) {
                            this.quit_server(false);
                        }
                    }
                    catch (Exception v) {
                        // empty catch block
                    }
                }
                if (ServerStatus.siOG("waiting_restart_user_name") != null && System.getProperty("crushftp.security.stop_start", "true").equals("true")) {
                    try {
                        if (ServerStatus.siVG("user_list").indexOf((Properties)ServerStatus.siOG("waiting_restart_user_name")) < 0) {
                            this.restart_crushftp();
                        }
                    }
                    catch (Exception v) {
                        // empty catch block
                    }
                }
                if (ServerStatus.siBG("update_when_idle")) {
                    try {
                        if (!this.starting && ServerStatus.count_users_down() == 0 && ServerStatus.count_users_up() == 0 && ServerStatus.siVG("running_tasks").size() == 0) {
                            this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|********Server is Idle...updating******** " + ServerStatus.siSG("version_info_str") + ServerStatus.siSG("sub_version_info_str"), "QUIT_SERVER");
                            ServerStatus.siPUT("allow_logins", "false");
                            ServerStatus.siPUT("update_when_idle", "false");
                            if (!com.crushftp.client.Common.dmz_mode) {
                                String instance = "";
                                Vector server_list = ServerStatus.VG("server_list");
                                int x = 0;
                                while (x < server_list.size()) {
                                    Properties server_item = (Properties)server_list.elementAt(x);
                                    if (server_item.getProperty("serverType", "").equalsIgnoreCase("DMZ") && server_item.getProperty("enabled", "true").equals("true")) {
                                        instance = server_item.getProperty("server_item_name");
                                        this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|********Server is Idle...updating DMZ(" + instance + ")********", "QUIT_SERVER");
                                        Properties request = new Properties();
                                        request.put("command", "adminAction");
                                        request.put("action", "stopAllServers");
                                        request.put("instance", instance);
                                        AdminControls.handleInstance(request, "(CONNECT)");
                                        request = new Properties();
                                        request.put("command", "updateNow");
                                        request.put("single_thread", "true");
                                        request.put("instance", instance);
                                        AdminControls.handleInstance(request, "(CONNECT)", 120);
                                    }
                                    ++x;
                                }
                            }
                            this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|********Server is Idle...updating MAIN******** " + ServerStatus.siSG("version_info_str") + ServerStatus.siSG("sub_version_info_str"), "QUIT_SERVER");
                            this.do_auto_update_early(false, false);
                        }
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 0, e);
                    }
                }
                if (ServerStatus.siBG("restart_when_idle") && System.getProperty("crushftp.security.stop_start", "true").equals("true")) {
                    try {
                        if (!this.starting && ServerStatus.count_users_down() == 0 && ServerStatus.count_users_up() == 0 && ServerStatus.siVG("running_tasks").size() == 0) {
                            this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|********Server is Idle...restarting******** " + ServerStatus.siSG("version_info_str") + ServerStatus.siSG("sub_version_info_str"), "QUIT_SERVER");
                            ServerStatus.siPUT("restart_when_idle", "false");
                            this.restart_crushftp();
                        }
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 0, e);
                    }
                }
                if (ServerStatus.siBG("shutdown_when_idle") && System.getProperty("crushftp.security.stop_start", "true").equals("true")) {
                    try {
                        if (!this.starting && ServerStatus.count_users_down() == 0 && ServerStatus.count_users_up() == 0 && ServerStatus.siVG("running_tasks").size() == 0) {
                            this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|********Server is Idle...shutting down******** " + ServerStatus.siSG("version_info_str") + ServerStatus.siSG("sub_version_info_str"), "QUIT_SERVER");
                            ServerStatus.siPUT("shutdown_when_idle", "false");
                            this.quit_server(false);
                        }
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 0, e);
                    }
                }
                this.setupGlobalPrefs();
                if (this.loggingProvider1 != null) {
                    this.loggingProvider1.checkLogPath();
                }
                if (this.loggingProvider2 != null) {
                    this.loggingProvider2.checkLogPath();
                }
                if (!this.server_info.containsKey("last_job_cache_clean")) {
                    this.server_info.put("last_job_cache_clean", String.valueOf(System.currentTimeMillis() - 10000L));
                }
                if (Long.parseLong(this.server_info.getProperty("last_job_cache_clean")) < System.currentTimeMillis() - 60000L * ServerStatus.LG("job_cache_update_interval_minutes")) {
                    this.server_info.put("last_job_cache_clean", String.valueOf(System.currentTimeMillis()));
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            JobScheduler.refreshJobsCache();
                        }
                    }, "last_job_cache_clean:" + JobScheduler.jobs_summary_cache_size + " items");
                }
                if (!this.server_info.containsKey("last_expired_accounts_check")) {
                    this.server_info.put("last_expired_accounts_check", String.valueOf(System.currentTimeMillis() - 60000L));
                }
                if (Long.parseLong(this.server_info.getProperty("last_expired_accounts_check")) < System.currentTimeMillis() - 3600000L || server_settings.getProperty("expired_accounts_notify_now").equals("true") && Long.parseLong(this.server_info.getProperty("last_expired_accounts_check")) < System.currentTimeMillis() - 60000L || server_settings.getProperty("expired_passwords_notify_now").equals("true") && Long.parseLong(this.server_info.getProperty("last_expired_accounts_check")) < System.currentTimeMillis() - 60000L) {
                    this.server_info.put("last_expired_accounts_check", String.valueOf(System.currentTimeMillis()));
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            Log.log("SERVER", 2, "Checking for expired accounts...");
                            String username = "";
                            try {
                                Vector sgs = (Vector)server_settings.get("server_groups");
                                int x = 0;
                                while (x < sgs.size()) {
                                    String serverGroup = sgs.elementAt(x).toString();
                                    Log.log("SERVER", 2, "Checking for expired accounts:" + serverGroup);
                                    Vector v = new Vector();
                                    UserTools.refreshUserList(serverGroup, v);
                                    int xx = 0;
                                    while (xx < v.size()) {
                                        ServerStatus.this.server_info.put("last_expired_accounts_check", String.valueOf(System.currentTimeMillis()));
                                        username = v.elementAt(xx).toString();
                                        try {
                                            Vector items;
                                            Properties info;
                                            Properties event;
                                            SimpleDateFormat sdf_compare;
                                            Vector items2;
                                            Properties info2;
                                            Properties event2;
                                            SimpleDateFormat midnight;
                                            int days;
                                            GregorianCalendar gc;
                                            Properties user = UserTools.ut.getUser(serverGroup, username, false);
                                            if (user != null) {
                                                Log.log("SERVER", 2, "Checking for expired accounts:" + serverGroup + ":" + username + ":" + user.getProperty("password_expire_advance_days_sent2", "") + ":" + user.getProperty("password_expire_advance_days_notify", ""));
                                            }
                                            if (!(user == null || user.getProperty("password_expire_advance_days_sent2", "").equals("true") || user.getProperty("password_expire_advance_days_notify", "").equals("") || user.getProperty("password_expire_advance_days_notify", "").equals("0"))) {
                                                gc = new GregorianCalendar();
                                                gc.setTime(new Date());
                                                days = Integer.parseInt(user.getProperty("password_expire_advance_days_notify"));
                                                gc.add(5, days);
                                                midnight = new SimpleDateFormat("MMddyy");
                                                gc.setTime(midnight.parse(midnight.format(gc.getTime())));
                                                gc.add(5, 1);
                                                gc.add(13, -1);
                                                if (ServerStatus.this.common_code.check_date_expired(user.getProperty("expire_password_when"), gc.getTime().getTime())) {
                                                    Log.log("SERVER", 0, "Notify expired password in advance days:" + serverGroup + "/" + username + ":days:" + days);
                                                    event2 = new Properties();
                                                    event2.put("event_plugin_list", user.getProperty("password_expire_notify_task"));
                                                    event2.put("name", "PasswordExpireNotify:" + username + ":" + user.getProperty("expire_password_when", ""));
                                                    info2 = new Properties();
                                                    info2.put("user", user);
                                                    info2.put("user_info", user);
                                                    items2 = new Vector();
                                                    ServerStatus.thisObj.events6.doEventPlugin(info2, event2, null, items2);
                                                    user.setProperty("password_expire_advance_days_sent2", "true");
                                                    UserTools.ut.put_in_user(serverGroup, username, "password_expire_advance_days_sent2", "true", true, true);
                                                }
                                            }
                                            if (user != null && user.getProperty("password_expire_advance_notify", "").equals("true")) {
                                                sdf_compare = new SimpleDateFormat("MMddyyyyHHmm", Locale.US);
                                                String current_time = sdf_compare.format(new Date());
                                                if (sdf_compare.format(ServerStatus.this.common_code.get_expired_date_format(user.getProperty("expire_password_when")).parse(user.getProperty("expire_password_when"))).equals(current_time)) {
                                                    Log.log("SERVER", 0, "Notify expired password:" + serverGroup + "/" + username);
                                                    event = new Properties();
                                                    event.put("id", Common.makeBoundary(10));
                                                    event.put("pluginName", "CrushTask");
                                                    event.put("event_action_list", "(run_plugin)");
                                                    event.put("subItem", "");
                                                    event.put("async", "true");
                                                    event.put("event_plugin_list", user.getProperty("password_expire_notify_task"));
                                                    event.put("name", "PasswordExpireNotify:" + serverGroup + ":" + username);
                                                    info = new Properties();
                                                    info.put("user", user);
                                                    info.put("user_info", user);
                                                    items = new Vector();
                                                    ServerStatus.thisObj.events6.doEventPlugin(info, event, null, items);
                                                }
                                            }
                                            if (!(user == null || user.getProperty("account_expire_advance_days_sent", "").equals("true") || user.getProperty("account_expire_advance_days_notify", "").equals("") || user.getProperty("account_expire_advance_days_notify", "").equals("0"))) {
                                                gc = new GregorianCalendar();
                                                gc.setTime(new Date());
                                                days = Integer.parseInt(user.getProperty("account_expire_advance_days_notify"));
                                                gc.add(5, days);
                                                midnight = new SimpleDateFormat("MMddyy");
                                                gc.setTime(midnight.parse(midnight.format(gc.getTime())));
                                                gc.add(5, 1);
                                                gc.add(13, -1);
                                                if (!ServerStatus.this.common_code.check_date_expired_roll(user.getProperty("account_expire")) && ServerStatus.this.common_code.check_date_expired(user.getProperty("account_expire"), gc.getTime().getTime())) {
                                                    Log.log("SERVER", 0, "Notify expired account in advance days:" + serverGroup + "/" + username + ":days:" + days);
                                                    event2 = new Properties();
                                                    event2.put("id", Common.makeBoundary(10));
                                                    event2.put("pluginName", "CrushTask");
                                                    event2.put("event_action_list", "(run_plugin)");
                                                    event2.put("subItem", "");
                                                    event2.put("async", "true");
                                                    event2.put("event_plugin_list", user.getProperty("account_expire_notify_task"));
                                                    event2.put("name", "AccountExpireNotify:" + username + ":" + user.getProperty("account_expire", ""));
                                                    info2 = new Properties();
                                                    info2.put("user", user);
                                                    info2.put("user_info", user);
                                                    items2 = new Vector();
                                                    ServerStatus.thisObj.events6.doEventPlugin(info2, event2, null, items2);
                                                    user.setProperty("account_expire_advance_days_sent", "true");
                                                    UserTools.ut.put_in_user(serverGroup, username, "account_expire_advance_days_sent", "true", true, true);
                                                }
                                            }
                                            if (user != null && user.getProperty("account_expire_advance_notify", "").equals("true")) {
                                                sdf_compare = new SimpleDateFormat("MMddyyyyHHmm", Locale.US);
                                                String current_time = sdf_compare.format(new Date());
                                                if (sdf_compare.format(ServerStatus.this.common_code.get_expired_date_format(user.getProperty("account_expire")).parse(user.getProperty("account_expire"))).equals(current_time)) {
                                                    Log.log("SERVER", 0, "Notify expired account:" + serverGroup + "/" + username);
                                                    event = new Properties();
                                                    event.put("event_plugin_list", user.getProperty("account_expire_notify_task"));
                                                    event.put("name", "AccountExpireNotify:" + serverGroup + ":" + username);
                                                    info = new Properties();
                                                    info.put("user", user);
                                                    info.put("user_info", user);
                                                    items = new Vector();
                                                    ServerStatus.thisObj.events6.doEventPlugin(info, event, null, items);
                                                }
                                            }
                                            if (user != null && user.getProperty("account_expire_delete", "").equals("true") && ServerStatus.this.common_code.check_date_expired_roll(user.getProperty("account_expire"))) {
                                                if (user.getProperty("account_expire_rolling_days", "").equals("0") || user.getProperty("account_expire_rolling_days", "").equals("account_expire_rolling_days") || !user.getProperty("account_expire_rolling_days", "").equals("") && Integer.parseInt(user.getProperty("account_expire_rolling_days")) < 0) {
                                                    Log.log("SERVER", 0, "Skipping delete of expired account:" + serverGroup + "/" + username + " because its a template with a negative expire days.");
                                                } else {
                                                    Log.log("SERVER", 0, "Deleting expired account:" + serverGroup + "/" + username);
                                                    UserTools.expireUserVFSTask(user, serverGroup, username);
                                                    Log.log("SERVER", 0, "Removing account:" + serverGroup + "/" + username);
                                                    UserTools.deleteUser(serverGroup, username);
                                                }
                                            }
                                            if (ServerStatus.BG("reverse_events") && user.get("events") != null) {
                                                try {
                                                    Vector events = (Vector)user.get("events");
                                                    Vector<Properties> invalid_events = new Vector<Properties>();
                                                    Properties user_flatten = null;
                                                    SessionCrush tempSession = null;
                                                    int xxx = 0;
                                                    while (xxx < events.size()) {
                                                        Properties event3 = (Properties)events.elementAt(xxx);
                                                        if (event3.getProperty("name", "").startsWith("subscribe_") && event3.getProperty("event_user_action_list", "").contains("r_")) {
                                                            String item_path;
                                                            VFS vfs;
                                                            String path = Common.url_decode(Common.replace_str(event3.getProperty("name", "").substring("subscribe".length()), "_", "/"));
                                                            if (user_flatten == null) {
                                                                user_flatten = UserTools.ut.getUser(serverGroup, username, true);
                                                            }
                                                            Properties item = null;
                                                            if (tempSession == null) {
                                                                tempSession = new SessionCrush(null, 1, "127.0.0.1", 0, "0.0.0.0", serverGroup, new Properties());
                                                                tempSession.verify_user(username, Common.makeBoundary(), true, false);
                                                            }
                                                            if ((item = (vfs = UserTools.ut.get_full_VFS(serverGroup, username, user_flatten)).get_item(item_path = Common.replace_str(String.valueOf(SessionCrush.getRootDir(null, vfs, user, false)) + path, "//", "/"))) == null) {
                                                                Log.log("EVENT", 1, "Event subscribes cleanup: Remove invalid event: " + event3.getProperty("name", ""));
                                                                invalid_events.add(event3);
                                                            }
                                                        }
                                                        ++xxx;
                                                    }
                                                    if (invalid_events.size() > 0) {
                                                        events.removeAll(invalid_events);
                                                        user.put("events", events);
                                                        UserTools.writeUser(serverGroup, username, user);
                                                    }
                                                }
                                                catch (Exception e) {
                                                    Log.log("SERVER", 1, "Checking event subscribes for user: " + username + " Error:" + e.toString());
                                                    Log.log("SERVER", 1, e);
                                                }
                                            }
                                            Thread.sleep(10L);
                                        }
                                        catch (Exception e) {
                                            Log.log("SERVER", 1, "Checking " + username + " for expiration...error:" + e.toString());
                                            Log.log("SERVER", 1, e);
                                        }
                                        ++xx;
                                    }
                                    ++x;
                                }
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 1, "Checking " + username + " for expiration...error:" + e.toString());
                                Log.log("SERVER", 1, e);
                            }
                            Log.log("SERVER", 2, "Checking for expired accounts...done.");
                            ServerStatus.this.server_info.put("last_expired_accounts_check", String.valueOf(System.currentTimeMillis()));
                        }
                    });
                }
                if (!this.server_info.containsKey("last_expired_shares_check")) {
                    this.server_info.put("last_expired_shares_check", "0");
                }
                if (!this.server_info.getProperty("last_expired_shares_check", "0").equals(this.expire_sdf.format(new Date()))) {
                    this.server_info.put("last_expired_shares_check", new SimpleDateFormat("MMddyy").format(new Date()));
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            block12: {
                                Log.log("SERVER", 2, "Checking for upcoming share expirations...");
                                String username = "";
                                try {
                                    String tempAccountsPath = ServerStatus.SG("temp_accounts_path");
                                    File_U[] accounts = (File_U[])new File_U(String.valueOf(tempAccountsPath) + "accounts/").listFiles();
                                    boolean found = false;
                                    if (accounts == null) break block12;
                                    int x = 0;
                                    while (!found && x < accounts.length) {
                                        try {
                                            File_U f = accounts[x];
                                            Log.log("SERVER", 2, "Temp:" + f.getName());
                                            if (f.getName().indexOf(",,") >= 0 && f.isDirectory()) {
                                                String[] tokens = f.getName().split(",,");
                                                Properties pp = new Properties();
                                                int xx = 0;
                                                while (xx < tokens.length) {
                                                    String key = tokens[xx].substring(0, tokens[xx].indexOf("="));
                                                    String val = tokens[xx].substring(tokens[xx].indexOf("=") + 1);
                                                    pp.put(key.toUpperCase(), val);
                                                    ++xx;
                                                }
                                                if (ServerStatus.thisObj.common_code.check_date_expired_roll(pp.getProperty("EX"))) {
                                                    Log.log("SERVER", 0, "Deleting share " + username + " due to expiration...");
                                                    if (!ServerStatus.SG("temp_accounts_account_expire_task").equals("")) {
                                                        Vector<Properties> items = new Vector<Properties>();
                                                        Properties item = new Properties();
                                                        Properties info = (Properties)Common.readXMLObject_U(String.valueOf(f.getPath()) + "/INFO.XML");
                                                        item.putAll((Map<?, ?>)info);
                                                        item.putAll((Map<?, ?>)pp);
                                                        item.put("url", f.toURI().toURL().toExternalForm());
                                                        item.put("the_file_name", f.getName());
                                                        item.put("the_file_path", "/");
                                                        item.put("account_path", String.valueOf(f.getCanonicalPath().replace('\\', '/')) + "/");
                                                        item.put("storage_path", String.valueOf(new File_U(String.valueOf(f.getCanonicalPath()) + "/../../storage/" + pp.getProperty("U") + pp.getProperty("P")).getCanonicalPath().replace('\\', '/')) + "/");
                                                        item.put("the_file_size", String.valueOf(f.length()));
                                                        item.put("type", f.isDirectory() ? "DIR" : "FILE");
                                                        items.addElement(item);
                                                        Properties event = new Properties();
                                                        event.put("event_plugin_list", ServerStatus.SG("temp_accounts_account_expire_task"));
                                                        event.put("name", "TempAccountEvent:" + pp.getProperty("U"));
                                                        ServerStatus.thisObj.events6.doEventPlugin(null, event, null, items);
                                                    }
                                                    Common.recurseDelete_U(String.valueOf(f.getCanonicalPath()) + "/../../storage/" + pp.getProperty("U") + pp.getProperty("P"), false);
                                                    Common.recurseDelete_U(f.getCanonicalPath(), false);
                                                } else {
                                                    Properties info = (Properties)Common.readXMLObject_U(String.valueOf(f.getPath()) + "/INFO.XML");
                                                    if (info != null && !info.getProperty("share_expire_notify_task_sent", "false").equals("true")) {
                                                        GregorianCalendar gc1 = new GregorianCalendar();
                                                        GregorianCalendar gc2 = new GregorianCalendar();
                                                        gc1.setTime(new Date());
                                                        gc2.setTime(new Date());
                                                        gc1.add(5, ServerStatus.IG("expire_share_notify_days") + 1);
                                                        gc2.add(5, ServerStatus.IG("expire_share_notify_days"));
                                                        SimpleDateFormat midnight = new SimpleDateFormat("MMddyy");
                                                        gc1.setTime(midnight.parse(midnight.format(gc1.getTime())));
                                                        gc1.add(5, 1);
                                                        gc1.add(13, -1);
                                                        gc2.setTime(midnight.parse(midnight.format(gc2.getTime())));
                                                        gc2.add(5, 1);
                                                        gc2.add(13, -1);
                                                        if (ServerStatus.thisObj.common_code.check_date_expired(pp.getProperty("EX"), gc1.getTime().getTime()) && !ServerStatus.thisObj.common_code.check_date_expired(pp.getProperty("EX"), gc2.getTime().getTime())) {
                                                            Log.log("SERVER", 0, "Notify expiring share account:" + username);
                                                            Properties event = new Properties();
                                                            event.put("id", Common.makeBoundary(10));
                                                            event.put("pluginName", "CrushTask");
                                                            event.put("event_action_list", "(run_plugin)");
                                                            event.put("subItem", "");
                                                            event.put("async", "true");
                                                            event.put("event_plugin_list", ServerStatus.SG("share_expire_notify_task"));
                                                            event.put("name", "ShareExpireNotify:" + username);
                                                            Vector items = new Vector();
                                                            ServerStatus.thisObj.events6.doEventPlugin(info, event, null, items);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        catch (Exception e) {
                                            Log.log("SERVER", 1, e);
                                        }
                                        ++x;
                                    }
                                }
                                catch (Exception e) {
                                    Log.log("SERVER", 1, "Checking " + username + " for expiration...error:" + e.toString());
                                    Log.log("SERVER", 1, e);
                                }
                            }
                            Log.log("SERVER", 2, "Checking for expired accounts...done.");
                            ServerStatus.this.server_info.put("last_expired_accounts_check", String.valueOf(System.currentTimeMillis()));
                        }
                    });
                }
                this.server_info.put("memcache_objects", String.valueOf(FileClient.dirCachePerm.size()));
                if (!this.server_info.containsKey("last_search_index_interval")) {
                    this.server_info.put("last_search_index_interval", "0");
                    Thread.sleep(1000L);
                }
                if (ServerStatus.IG("search_index_interval") > 0 && Long.parseLong(this.server_info.getProperty("last_search_index_interval")) < System.currentTimeMillis() - 60000L * ServerStatus.LG("search_index_interval")) {
                    this.server_info.put("last_search_index_interval", String.valueOf(System.currentTimeMillis()));
                    Worker.startWorker(new Runnable(){

                        /*
                         * WARNING - Removed try catching itself - possible behaviour change.
                         */
                        @Override
                        public void run() {
                            try {
                                Object object = FileClient.dirCachePermTemp_lock;
                                synchronized (object) {
                                    String[] usernames = ServerStatus.SG("search_index_usernames").split(",");
                                    FileClient.dirCachePermTemp = new Properties();
                                    if (FileClient.dirCachePerm != null && FileClient.dirCachePerm.size() == 0) {
                                        FileClient.dirCachePerm = FileClient.dirCachePermTemp;
                                    }
                                    int x = 0;
                                    while (x < usernames.length) {
                                        if (!usernames[x].trim().equals("")) {
                                            Vector server_groups = (Vector)server_settings.get("server_groups");
                                            int xx = 0;
                                            while (xx < server_groups.size()) {
                                                VFS uVFS = UserTools.ut.getVFS(server_groups.elementAt(xx).toString(), usernames[x].trim());
                                                Properties pp = uVFS.get_item("/");
                                                SearchHandler.buildEntry(pp, uVFS, "new", null);
                                                uVFS.disconnect();
                                                uVFS.free();
                                                ServerStatus.this.server_info.put("last_search_index_interval", String.valueOf(System.currentTimeMillis()));
                                                ++xx;
                                            }
                                        }
                                        ++x;
                                    }
                                    FileClient.dirCachePerm = FileClient.dirCachePermTemp;
                                    FileClient.dirCachePermTemp = null;
                                    ServerStatus.this.server_info.put("last_search_index_interval", String.valueOf(System.currentTimeMillis()));
                                }
                            }
                            catch (Exception e) {
                                Log.log("SEARCH", 0, e);
                            }
                            ServerStatus.this.server_info.put("last_search_index_interval", String.valueOf(System.currentTimeMillis()));
                        }
                    });
                }
                if (!this.server_info.containsKey("last_secondary_login_via_email_check")) {
                    this.server_info.put("last_secondary_login_via_email_check", "0");
                }
                if (ServerStatus.BG("secondary_login_via_email") && Long.parseLong(this.server_info.getProperty("last_secondary_login_via_email_check")) < System.currentTimeMillis() - 60000L * ServerStatus.LG("secondary_login_via_email_cache_interval")) {
                    this.server_info.put("last_secondary_login_via_email_check", String.valueOf(System.currentTimeMillis()));
                    if (this.server_info.getProperty("checking_email_cache", "false").equals("false")) {
                        Worker.startWorker(new Runnable(){

                            @Override
                            public void run() {
                                ServerStatus.this.server_info.put("checking_email_cache", "true");
                                try {
                                    UserTools.cacheEmailUsernames();
                                }
                                catch (Exception e) {
                                    Log.log("SERVER", 0, e);
                                }
                                ServerStatus.this.server_info.put("checking_email_cache", "false");
                                ServerStatus.this.server_info.put("last_secondary_login_via_email_check", String.valueOf(System.currentTimeMillis()));
                            }
                        });
                    }
                    this.server_info.put("last_secondary_login_via_email_check", String.valueOf(System.currentTimeMillis()));
                }
                if (!this.server_info.containsKey("last_expired_sync_check")) {
                    this.server_info.put("last_expired_sync_check", String.valueOf(System.currentTimeMillis()));
                }
                if (Long.parseLong(this.server_info.getProperty("last_expired_sync_check")) < System.currentTimeMillis() - 3600000L) {
                    this.server_info.put("last_expired_sync_check", String.valueOf(System.currentTimeMillis()));
                    if (ServerStatus.IG("sync_history_days") > 0) {
                        final Properties status = new Properties();
                        Worker.startWorker(new Runnable(){

                            @Override
                            public void run() {
                                try {
                                    GregorianCalendar c = new GregorianCalendar();
                                    c.setTime(new Date());
                                    ((Calendar)c).add(5, ServerStatus.IG("sync_history_days") * -1);
                                    SyncTools.purgeExpired(c.getTime().getTime());
                                }
                                catch (Exception e) {
                                    Log.log("SYNC", 0, e);
                                }
                                status.put("done", "done");
                                ServerStatus.this.server_info.put("last_expired_sync_check", String.valueOf(System.currentTimeMillis()));
                            }
                        });
                        Worker.startWorker(new Runnable(){

                            @Override
                            public void run() {
                                try {
                                    while (status.size() == 0) {
                                        ServerStatus.this.server_info.put("last_expired_sync_check", String.valueOf(System.currentTimeMillis()));
                                        Thread.sleep(10000L);
                                        ServerStatus.this.server_info.put("last_expired_sync_check", String.valueOf(System.currentTimeMillis()));
                                    }
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                        });
                    }
                }
                if (!this.server_info.containsKey("last_server_info_history")) {
                    this.server_info.put("last_server_info_history", String.valueOf(System.currentTimeMillis()));
                }
                if (!this.server_info.containsKey("last_server_info_history_delete")) {
                    this.server_info.put("last_server_info_history_delete", "0");
                }
                if (Long.parseLong(this.server_info.getProperty("last_server_info_history")) < System.currentTimeMillis() - 40000L) {
                    this.server_info.put("last_server_info_history", String.valueOf(System.currentTimeMillis()));
                    char mm = new SimpleDateFormat("mm").format(new Date()).charAt(1);
                    if (mm == '0' || mm == '5') {
                        Worker.startWorker(new Runnable(){

                            @Override
                            public void run() {
                                SimpleDateFormat yyMMdd = new SimpleDateFormat("yyMMdd");
                                try {
                                    Properties request = new Properties();
                                    request.put("params", "current_download_speed,current_upload_speed,logged_in_users,ram_free,ram_max,server_cpu,os_cpu,open_files,connected_unique_ips");
                                    request.put("priorIntervals", "300");
                                    Object getDashboardItems = AdminControls.getDashboardItems(request, "(CONNECT)");
                                    String getStatHistory = AdminControls.getStatHistory(request);
                                    Vector getJobsSummary = AdminControls.getJobsSummary(request, "(CONNECT)");
                                    Properties history_object = new Properties();
                                    history_object.put("getDashboardItems", getDashboardItems);
                                    history_object.put("getJobsSummary", getJobsSummary);
                                    history_object.put("getStatHistory", getStatHistory);
                                    SimpleDateFormat HHmm = new SimpleDateFormat("HHmm");
                                    String archived_history = String.valueOf(new File_S(ServerStatus.change_vars_to_values_static(ServerStatus.SG("user_log_location"), null, null, null)).getCanonicalFile().getParentFile().getPath()) + "/archived_history/" + yyMMdd.format(new Date()) + "/";
                                    new File_S(archived_history).mkdirs();
                                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(String.valueOf(archived_history) + HHmm.format(new Date()) + ".history_obj"));
                                    oos.writeObject(history_object);
                                    oos.close();
                                }
                                catch (Exception e) {
                                    Log.log("SERVER", 0, e);
                                }
                                if (Long.parseLong(ServerStatus.this.server_info.getProperty("last_server_info_history_delete")) < System.currentTimeMillis() - 14400000L) {
                                    ServerStatus.this.server_info.put("last_server_info_history_delete", String.valueOf(System.currentTimeMillis()));
                                    try {
                                        GregorianCalendar c = new GregorianCalendar();
                                        c.setTime(new Date());
                                        ((Calendar)c).add(5, ServerStatus.IG("server_info_history_days") * -1);
                                        int min_date = Integer.parseInt(yyMMdd.format(c.getTime()));
                                        String archived_history = String.valueOf(new File_S(ServerStatus.change_vars_to_values_static(ServerStatus.SG("user_log_location"), null, null, null)).getCanonicalFile().getParentFile().getPath()) + "/archived_history/";
                                        File[] dirs = new File_S(archived_history).listFiles();
                                        int x = 0;
                                        while (dirs != null && x < dirs.length) {
                                            if (dirs[x].getName().length() == 6) {
                                                try {
                                                    if (Integer.parseInt(dirs[x].getName()) < min_date) {
                                                        Common.recurseDelete(dirs[x].getPath(), false);
                                                    }
                                                }
                                                catch (Exception e) {
                                                    Log.log("SERVER", 2, e);
                                                }
                                            }
                                            ++x;
                                        }
                                    }
                                    catch (Exception e) {
                                        Log.log("SERVER", 0, e);
                                    }
                                }
                                try {
                                    Thread.sleep(21000L);
                                }
                                catch (InterruptedException interruptedException) {
                                    // empty catch block
                                }
                                ServerStatus.this.server_info.put("last_server_info_history", String.valueOf(System.currentTimeMillis()));
                            }
                        });
                    }
                }
                long last_stats_db_size = com.crushftp.client.Common.recurseSize("./statsDB", 0L);
                long last_sessions_obj_size = com.crushftp.client.Common.recurseSize("./sessions.obj", 0L);
                ServerStatus.siPUT("last_stats_db_size", String.valueOf(last_stats_db_size));
                ServerStatus.siPUT("last_sessions_obj_size", String.valueOf(last_sessions_obj_size));
                ServerStatus.siPUT("used_threads", String.valueOf(Worker.busyWorkers.size()));
                ServerStatus.siPUT("max_threads", String.valueOf(ServerStatus.IG("max_threads")));
                ServerStatus.updateMemoryStats();
                final String memory_threads = "Server Memory Stats: Max=" + com.crushftp.client.Common.format_bytes_short2(ServerStatus.siLG("ram_max")) + ", Free=" + com.crushftp.client.Common.format_bytes_short2(ServerStatus.siLG("ram_free")) + ", Threads:" + Worker.busyWorkers.size() + ", " + System.getProperty("java.version") + ":" + System.getProperty("sun.arch.data.model") + " bit," + Runtime.getRuntime().availableProcessors() + "cores  OS:" + System.getProperties().getProperty("os.name") + " CPU usage Server/OS:" + ServerStatus.siSG("server_cpu") + "/" + ServerStatus.siSG("os_cpu") + " OpenFiles:" + ServerStatus.siSG("open_files") + "/" + ServerStatus.siSG("max_open_files") + ", statsDB size=" + com.crushftp.client.Common.format_bytes_short(last_stats_db_size) + ", sessions.obj size=" + com.crushftp.client.Common.format_bytes_short(last_sessions_obj_size) + " :" + ServerStatus.siSG("version_info_str") + ServerStatus.siSG("sub_version_info_str");
                if (!this.server_info.containsKey("last_memory_check")) {
                    this.server_info.put("last_memory_check", String.valueOf(System.currentTimeMillis()));
                }
                if (Long.parseLong(this.server_info.getProperty("last_memory_check")) < System.currentTimeMillis() - 1000L * ServerStatus.LG("memory_log_interval")) {
                    this.server_info.put("last_memory_check", String.valueOf(System.currentTimeMillis()));
                    Log.log("SERVER", 0, memory_threads);
                }
                if ((low_memory_trigger_value1 = ServerStatus.LG("low_memory_trigger_value1")) > 99L) {
                    low_memory_trigger_value1 = 40L;
                }
                if ((low_memory_trigger_value2 = ServerStatus.LG("low_memory_trigger_value2")) > 99L) {
                    low_memory_trigger_value2 = 30L;
                }
                if ((low_memory_trigger_value3 = ServerStatus.LG("low_memory_trigger_value3")) > 99L) {
                    low_memory_trigger_value3 = 20L;
                }
                final long f_low_memory_trigger_value3 = low_memory_trigger_value3;
                if ((float)ServerStatus.siLG("ram_free") / (float)ServerStatus.siLG("ram_max") < (float)low_memory_trigger_value3 / 100.0f) {
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            System.gc();
                            System.gc();
                            try {
                                Thread.sleep(3000L);
                            }
                            catch (InterruptedException interruptedException) {
                                // empty catch block
                            }
                            System.gc();
                            ServerStatus.updateMemoryStats();
                            if ((float)ServerStatus.siLG("ram_free") / (float)ServerStatus.siLG("ram_max") < (float)f_low_memory_trigger_value3 / 100.0f) {
                                Log.log("SERVER", 0, "LOW_MEMORY:" + memory_threads);
                                ServerStatus.this.server_info.put("low_memory", "Critically low on memory!<br/>Crash is imminent! Less than " + f_low_memory_trigger_value3 + "%!<br/>" + ServerStatus.this.logDateFormat.format(new Date()) + "|" + memory_threads);
                                Properties info = new Properties();
                                info.put("alert_type", "low_memory");
                                info.put("alert_ram_free", String.valueOf(ServerStatus.siLG("ram_free")));
                                info.put("alert_ram_max", String.valueOf(ServerStatus.siLG("ram_max")));
                                info.put("alert_memory_threads", memory_threads);
                                info.put("alert_timeout", "0");
                                info.put("alert_max", "0");
                                info.put("alert_msg", "");
                                ServerStatus.this.runAlerts("low_memory", info, null, null);
                                ServerStatus.this.runAlerts("low_memory3", info, null, null);
                                System.gc();
                            }
                        }
                    });
                } else if ((float)ServerStatus.siLG("ram_free") / (float)ServerStatus.siLG("ram_max") < (float)low_memory_trigger_value2 / 100.0f) {
                    Log.log("SERVER", 0, "LOW_MEMORY:" + memory_threads);
                    this.server_info.put("low_memory", "Very low on memory! Less than " + low_memory_trigger_value2 + "%!<br/>" + this.logDateFormat.format(new Date()) + "|" + memory_threads);
                    Properties info = new Properties();
                    info.put("alert_type", "low_memory");
                    info.put("alert_ram_free", String.valueOf(ServerStatus.siLG("ram_free")));
                    info.put("alert_ram_max", String.valueOf(ServerStatus.siLG("ram_max")));
                    info.put("alert_memory_threads", memory_threads);
                    info.put("alert_timeout", "0");
                    info.put("alert_max", "0");
                    info.put("alert_msg", "");
                    this.runAlerts("low_memory", info, null, null);
                    this.runAlerts("low_memory2", info, null, null);
                    System.gc();
                } else if ((float)ServerStatus.siLG("ram_free") / (float)ServerStatus.siLG("ram_max") < (float)low_memory_trigger_value1 / 100.0f) {
                    Log.log("SERVER", 0, "LOW_MEMORY:" + memory_threads);
                    this.server_info.put("low_memory", "Low on memory! Less than " + low_memory_trigger_value1 + "%!<br/>" + this.logDateFormat.format(new Date()) + "|" + memory_threads);
                    Properties info = new Properties();
                    info.put("alert_type", "low_memory");
                    info.put("alert_ram_free", String.valueOf(ServerStatus.siLG("ram_free")));
                    info.put("alert_ram_max", String.valueOf(ServerStatus.siLG("ram_max")));
                    info.put("alert_memory_threads", memory_threads);
                    info.put("alert_timeout", "0");
                    info.put("alert_max", "0");
                    info.put("alert_msg", "");
                    this.runAlerts("low_memory", info, null, null);
                    this.runAlerts("low_memory1", info, null, null);
                    System.gc();
                } else {
                    this.server_info.remove("low_memory");
                }
                if (!this.server_info.containsKey("last_vfs_check")) {
                    this.server_info.put("last_vfs_check", "0");
                }
                if (Long.parseLong(this.server_info.getProperty("last_vfs_check")) < System.currentTimeMillis() - 1000L * ServerStatus.LG("vfs_cache_interval")) {
                    this.server_info.put("last_vfs_check", String.valueOf(System.currentTimeMillis()));
                    if (ServerStatus.BG("vfs_cache_enabled")) {
                        this.fill_vfs_cache();
                    }
                }
                if (!this.server_info.containsKey("last_quota_check")) {
                    this.server_info.put("last_quota_check", String.valueOf(System.currentTimeMillis() - 1000L * ServerStatus.LG("quota_async_cache_interval") + 60000L));
                }
                if (Long.parseLong(this.server_info.getProperty("last_quota_check")) < System.currentTimeMillis() - 1000L * ServerStatus.LG("quota_async_cache_interval")) {
                    this.server_info.put("last_quota_check", String.valueOf(System.currentTimeMillis()));
                    if (ServerStatus.BG("quota_async")) {
                        try {
                            Worker.startWorker(new QuotaWorker());
                        }
                        catch (IOException info) {
                            // empty catch block
                        }
                    }
                }
                if (!this.server_info.containsKey("last_dump_threads")) {
                    this.server_info.put("last_dump_threads", String.valueOf(System.currentTimeMillis()));
                }
                if (ServerStatus.LG("dump_threads_log_interval") > 0L && Long.parseLong(this.server_info.getProperty("last_dump_threads")) < System.currentTimeMillis() - 1000L * ServerStatus.LG("dump_threads_log_interval")) {
                    this.server_info.put("last_dump_threads", String.valueOf(System.currentTimeMillis()));
                    System.out.println(new Date() + "THREAD DUMP");
                    System.out.println(com.crushftp.client.Common.dumpStack(String.valueOf(version_info_str) + sub_version_info_str));
                }
                Worker.startWorker(new Runnable(){

                    /*
                     * WARNING - Removed try catching itself - possible behaviour change.
                     * Unable to fully structure code
                     */
                    @Override
                    public void run() {
                        alert_tmp_f = new Vector<E>();
                        var2_2 = com.crushftp.client.Common.System2.get("alerts_queue");
                        synchronized (var2_2) {
                            alert_tmp_f.addAll((Vector)com.crushftp.client.Common.System2.get("alerts_queue"));
                            ((Vector)com.crushftp.client.Common.System2.get("alerts_queue")).clear();
                            // MONITOREXIT @DISABLED, blocks:[0, 1] lbl8 : MonitorExitStatement: MONITOREXIT : var2_2
                            if (true) ** GOTO lbl15
                        }
                        do {
                            alert = (Properties)alert_tmp_f.remove(0);
                            ServerStatus.thisObj.runAlerts(alert.getProperty("msg", ""), (SessionCrush)alert.get("session"));
lbl15:
                            // 2 sources

                        } while (alert_tmp_f.size() > 0);
                    }
                });
                if (!this.server_info.containsKey("last_put_in_user_flush")) {
                    this.server_info.put("last_put_in_user_flush", String.valueOf(System.currentTimeMillis()));
                }
                if (Long.parseLong(this.server_info.getProperty("last_put_in_user_flush")) < System.currentTimeMillis() - 60000L) {
                    this.server_info.put("last_put_in_user_flush", String.valueOf(System.currentTimeMillis()));
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            ServerStatus.this.server_info.put("last_put_in_user_flush", String.valueOf(System.currentTimeMillis()));
                            UserTools.ut.put_in_user_flush();
                        }
                    });
                }
            } else if (arg.equals("vfs_replication_pinger")) {
                if (!this.server_info.containsKey("replicated_vfs_ping_interval")) {
                    this.server_info.put("replicated_vfs_ping_interval", String.valueOf(System.currentTimeMillis()));
                }
                if (ServerStatus.LG("replicated_vfs_ping_interval") > 0L && Long.parseLong(this.server_info.getProperty("replicated_vfs_ping_interval")) < System.currentTimeMillis() - 1000L * ServerStatus.LG("replicated_vfs_ping_interval")) {
                    this.server_info.put("replicated_vfs_ping_interval", String.valueOf(System.currentTimeMillis()));
                    if (!ServerStatus.SG("replicated_vfs_root_url").equals("")) {
                        try {
                            Properties vItem = new Properties();
                            Properties virtual = new Properties();
                            VFS uVFS = VFS.getVFS(virtual);
                            vItem.put("url", ServerStatus.SG("replicated_vfs_root_url"));
                            vItem.put("type", "DIR");
                            Properties item = new Properties();
                            item.put("vItem", vItem);
                            Vector<Properties> vItems = new Vector<Properties>();
                            Vector<MemoryClient> clients = new Vector<MemoryClient>();
                            clients.addElement(new MemoryClient("MEMORY:///", "", null));
                            vItems.addElement(vItem);
                            uVFS.addReplicatedVFSAndClient(item, vItems, clients, true);
                            new GenericClientMulti("PROXY", com.crushftp.client.Common.log, vItem, vItems, clients, true).close();
                            uVFS.disconnect();
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 1, e);
                        }
                    }
                    this.server_info.put("replicated_vfs_ping_interval", String.valueOf(System.currentTimeMillis()));
                }
            } else if (arg.equals("http_cleaner")) {
                if (ServerStatus.BG("encryption_pass_needed") && new String(com.crushftp.client.Common.encryption_password).equals("crushftp")) {
                    this.append_log("WARNING: Encryption password needed for the server!  Please login to admin console to provide it.", "RUN_SERVER");
                }
                long sessions = 0L;
                long http_keys = 0L;
                long http_keys_expired1 = 0L;
                long http_keys_expired2 = 0L;
                long http_activity_keys = 0L;
                long http_activity_keys_expired = 0L;
                try {
                    Enumeration keys = SharedSession.find("crushftp.sessions").keys();
                    while (keys.hasMoreElements()) {
                        String id = keys.nextElement().toString();
                        ++http_activity_keys;
                        Object o = SharedSession.find("crushftp.sessions").get(id);
                        long time = 0L;
                        long timeout = 60L * ServerStatus.LG("http_session_timeout");
                        if (o instanceof SessionCrush) {
                            time = Long.parseLong(((SessionCrush)o).getProperty("last_activity", "0"));
                            if (((SessionCrush)o).user != null) {
                                long timeout2 = Long.parseLong(((SessionCrush)o).user.getProperty("max_idle_time", "10"));
                                if (timeout2 < 0L) {
                                    timeout = timeout2 * -1L;
                                } else if (timeout2 != 0L && timeout2 < timeout) {
                                    timeout = 60L * timeout2;
                                }
                            }
                            Properties ui = ((SessionCrush)o).user_info;
                            if (Log.log("SERVER", 2, "")) {
                                Log.log("SERVER", 2, "Checking session:" + id + " ui present:" + (ui != null));
                            }
                            if (System.currentTimeMillis() - time > 1000L && ui != null && ui.getProperty("webdav_login", "").equals("true")) {
                                timeout = ServerStatus.IG("webdav_timeout_secs");
                                if (Log.log("SERVER", 2, "")) {
                                    Log.log("SERVER", 2, "Detected HTTP WebDAV session:" + id + " timeout set to:" + timeout + " secs");
                                }
                            }
                        }
                        if (new Date().getTime() - time <= 1000L * timeout) continue;
                        boolean allow_removal = true;
                        Enumeration e = SharedSession.find("crushftp.usernames").keys();
                        while (e.hasMoreElements() && allow_removal) {
                            String key2 = e.nextElement().toString();
                            if (key2.indexOf("_" + id + "_") < 0) continue;
                            Enumeration<Object> tunnel_keys = ServerSessionTunnel3.running_tunnels.keys();
                            while (tunnel_keys.hasMoreElements() && allow_removal) {
                                String tunnel_id = tunnel_keys.nextElement().toString();
                                if (!tunnel_id.startsWith(String.valueOf(key2) + "_")) continue;
                                StreamController sc = (StreamController)ServerSessionTunnel3.running_tunnels.get(tunnel_id);
                                if (System.currentTimeMillis() - sc.last_receive_activity > 60000L) {
                                    Log.log("TUNNEL", 0, "Current tunnel ID list:" + ServerSessionTunnel3.running_tunnels);
                                    Log.log("TUNNEL", 0, "Tunnel is dead and the session has timed out, closing it:" + tunnel_id + " inactive time:" + (System.currentTimeMillis() - sc.last_receive_activity));
                                    sc.startStopTunnel(false);
                                    continue;
                                }
                                allow_removal = false;
                            }
                            ++http_keys_expired1;
                            SharedSession.find("crushftp.usernames").remove(key2);
                            Tunnel2.stopTunnel(key2);
                            if (!(o instanceof SessionCrush) || ((SessionCrush)o).uVFS == null) continue;
                            ((SessionCrush)o).uVFS.disconnect();
                        }
                        if (!allow_removal) continue;
                        ++http_activity_keys_expired;
                        if (o instanceof SessionCrush) {
                            this.remove_user(((SessionCrush)o).user_info);
                        }
                        SharedSession.find("crushftp.sessions").remove(id);
                        if (!Log.log("SERVER", 2, "")) continue;
                        Log.log("SERVER", 2, "Removing HTTP session:" + id);
                    }
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                }
                try {
                    long timeout = 60L * ServerStatus.LG("http_session_timeout");
                    Vector v = (Vector)ServerStatus.siVG("user_list").clone();
                    int x = v.size() - 1;
                    while (x >= 0) {
                        Properties user_info = (Properties)v.elementAt(x);
                        SessionCrush thisSession = (SessionCrush)user_info.get("session");
                        if ((thisSession == null || SharedSession.find("crushftp.sessions").get(thisSession.getId()) == null) && (System.currentTimeMillis() - Long.parseLong(user_info.getProperty("last_activity", "0"))) / 1000L > timeout) {
                            this.kick(user_info);
                        }
                        --x;
                    }
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                }
                try {
                    Enumeration<Object> keys = ServerStatus.siPG("domain_cross_reference").keys();
                    while (keys.hasMoreElements()) {
                        String key = keys.nextElement().toString();
                        String val = ServerStatus.siPG("domain_cross_reference").getProperty(key);
                        if (System.currentTimeMillis() - Long.parseLong(val.split(":")[0]) <= 300000L) continue;
                        ServerStatus.siPG("domain_cross_reference").remove(key);
                    }
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                }
                try {
                    Enumeration e = SharedSession.find("crushftp.usernames").keys();
                    while (e.hasMoreElements()) {
                        String key2 = e.nextElement().toString();
                        ++http_keys;
                        String id = key2.substring(key2.indexOf("_") + 1, key2.lastIndexOf("_"));
                        if (SharedSession.find("crushftp.sessions").containsKey(id)) continue;
                        ++http_keys_expired2;
                        SharedSession.find("crushftp.usernames").remove(key2);
                    }
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                }
                Log.log("SERVER", 1, "Cleaning up sessions:" + sessions + " sessions tracked, " + http_activity_keys + " activity items tracked, " + http_keys + " sessions tracked, " + http_activity_keys_expired + " activities expired, " + http_keys_expired1 + " sessions expired in first pass, and " + http_keys_expired2 + " expired in second pass.");
                try {
                    Properties resetTokens = ServerStatus.siPG("resetTokens");
                    if (resetTokens == null) {
                        resetTokens = new Properties();
                    }
                    ServerStatus.thisObj.server_info.put("resetTokens", resetTokens);
                    Enumeration<Object> e = resetTokens.keys();
                    while (e.hasMoreElements()) {
                        String key2 = e.nextElement().toString();
                        Properties reset = (Properties)resetTokens.get(key2);
                        long generated = Long.parseLong(reset.getProperty("generated"));
                        if (System.currentTimeMillis() <= generated + (long)(60000 * ServerStatus.IG("reset_token_timeout"))) continue;
                        resetTokens.remove(key2);
                    }
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                }
                Enumeration<Object> tunnel_keys = ServerSessionTunnel3.running_tunnels.keys();
                while (tunnel_keys.hasMoreElements()) {
                    String tunnel_id = tunnel_keys.nextElement().toString();
                    StreamController sc = (StreamController)ServerSessionTunnel3.running_tunnels.get(tunnel_id);
                    if (System.currentTimeMillis() - sc.last_receive_activity <= 60000L) continue;
                    Log.log("TUNNEL", 0, "Current tunnel ID list:" + ServerSessionTunnel3.running_tunnels);
                    Log.log("TUNNEL", 0, "Tunnel is dead, closing it:" + tunnel_id + " inactive time:" + (System.currentTimeMillis() - sc.last_receive_activity));
                    ServerSessionTunnel3.running_tunnels.remove(tunnel_id);
                    sc.startStopTunnel(false);
                }
                try {
                    Vector v = (Vector)ServerStatus.siVG("user_list").clone();
                    int x = v.size() - 1;
                    while (x >= 0) {
                        Properties user_info = (Properties)v.elementAt(x);
                        SessionCrush thisSession = (SessionCrush)user_info.get("session");
                        if (thisSession != null && thisSession.uVFS != null && thisSession.uVFS.cacheList != null) {
                            Properties cache = thisSession.uVFS.cacheList;
                            Enumeration<Object> keys = cache.keys();
                            while (keys.hasMoreElements()) {
                                Properties h;
                                String key = "" + keys.nextElement();
                                Object o2 = cache.get(key);
                                if (!(o2 instanceof Properties) || (h = (Properties)o2) == null || System.currentTimeMillis() - Long.parseLong(h.getProperty("time")) <= 60000L) continue;
                                cache.remove(key);
                                cache.remove(String.valueOf(key) + "...count");
                            }
                        }
                        --x;
                    }
                }
                catch (Exception e) {
                    Log.log("SERVER", 2, e);
                }
            } else {
                if (arg.equals("events_thread")) {
                    Object sessions = this.eventLock;
                    synchronized (sessions) {
                        try {
                            this.events6.checkEventsNow();
                        }
                        catch (Exception e) {
                            Log.log("EVENT", 0, e);
                        }
                    }
                }
                if (arg.equals("log_rolling")) {
                    Log.log("SERVER", 3, "Log Rolling:Checking is log rolling enabled? roll_log=" + ServerStatus.BG("roll_log"));
                    if (ServerStatus.BG("roll_log") && this.loggingProvider1 != null) {
                        this.loggingProvider1.checkForLogRoll();
                    }
                    if (ServerStatus.BG("roll_log") && this.loggingProvider2 != null) {
                        this.loggingProvider2.checkForLogRoll();
                    }
                    Thread.sleep(5000L);
                    String job_log_path = String.valueOf(new File_S(ServerStatus.change_vars_to_values_static(ServerStatus.SG("log_location"), null, null, null)).getCanonicalFile().getParentFile().getPath()) + "/logs/jobs/";
                    File_S logFiles = new File_S(job_log_path);
                    logFiles.mkdirs();
                    if (!this.server_info.containsKey("last_expired_log_check")) {
                        this.server_info.put("last_expired_log_check", String.valueOf(System.currentTimeMillis() - 60000L));
                    }
                    if (Long.parseLong(this.server_info.getProperty("last_expired_log_check")) < System.currentTimeMillis() - 600000L) {
                        Worker.startWorker(new Runnable(){

                            @Override
                            public void run() {
                                Thread.currentThread().setName("Checking for expired logs and cleaning them up...");
                                ServerStatus.expired_log_cleanup();
                            }
                        });
                    }
                } else if (arg.equals("monitor_folders") || arg.equals("monitor_folders_instant")) {
                    this.doFolderMonitor(arg);
                } else if (arg.equals("log_handler")) {
                    this.doLogFlush();
                } else if (arg.equals("extra_update_timer")) {
                    this.doExtraUpdateActions();
                } else if (arg.equals("gui_timer")) {
                    this.doGuiTimerUpdates();
                }
            }
        }
    }

    public static void expired_log_cleanup() {
        ServerStatus.thisObj.server_info.put("last_expired_log_check", String.valueOf(System.currentTimeMillis()));
        Log.log("SERVER", 2, "Checking for expired session logs and job logs...");
        try {
            File_S[] log_dates;
            if (ServerStatus.SG("user_log_location").indexOf("session_logs") < 0) {
                server_settings.put("user_log_location", String.valueOf(ServerStatus.SG("user_log_location")) + "session_logs/");
            }
            if ((log_dates = (File_S[])new File_S(String.valueOf(new File_S(ServerStatus.change_vars_to_values_static(ServerStatus.SG("user_log_location"), null, null, null)).getCanonicalFile().getParentFile().getPath()) + "/session_logs/").listFiles()) != null) {
                Log.log("SERVER", 2, "Found log_dates items:" + log_dates.length);
            }
            int x = 0;
            while (log_dates != null && x < log_dates.length) {
                Thread.sleep(1L);
                ServerStatus.thisObj.server_info.put("last_expired_log_check", String.valueOf(System.currentTimeMillis()));
                if (log_dates[x].isDirectory()) {
                    File_S[] logs = (File_S[])new File_S(String.valueOf(new File_S(ServerStatus.change_vars_to_values_static(ServerStatus.SG("user_log_location"), null, null, null)).getCanonicalFile().getParentFile().getPath()) + "/session_logs/" + log_dates[x].getName() + "/").listFiles();
                    int xx = 0;
                    while (logs != null && xx < logs.length) {
                        Thread.sleep(1L);
                        ServerStatus.thisObj.server_info.put("last_expired_log_check", String.valueOf(System.currentTimeMillis()));
                        if (logs[xx].isFile() && (logs[xx].getName().toUpperCase().endsWith(".LOG") || logs[xx].getName().toUpperCase().startsWith(".")) && (System.currentTimeMillis() - logs[xx].lastModified() > 86400000L * ServerStatus.LG("recent_user_log_days") || logs[xx].getName().toUpperCase().startsWith(".")) && !logs[xx].delete()) {
                            Log.log("SERVER", 2, "0:Log file delete failed:" + logs[xx]);
                        }
                        ++xx;
                    }
                    if (!(logs != null && logs.length >= 10 || (logs = (File_S[])new File_S(String.valueOf(new File_S(ServerStatus.change_vars_to_values_static(ServerStatus.SG("user_log_location"), null, null, null)).getCanonicalFile().getParentFile().getPath()) + "/session_logs/" + log_dates[x].getName() + "/").listFiles()) != null && logs.length != 0 || log_dates[x].delete())) {
                        Log.log("SERVER", 2, "1:Log folder delete failed:" + log_dates[x]);
                    }
                } else if (log_dates[x].isFile() && (log_dates[x].getName().toUpperCase().endsWith(".LOG") || log_dates[x].getName().toUpperCase().startsWith(".")) && System.currentTimeMillis() - log_dates[x].lastModified() > 86400000L * ServerStatus.LG("recent_user_log_days")) {
                    ServerStatus.thisObj.server_info.put("last_expired_log_check", String.valueOf(System.currentTimeMillis()));
                    if (!log_dates[x].delete()) {
                        Log.log("SERVER", 2, "2:Log folder delete failed:" + log_dates[x]);
                    }
                }
                ++x;
            }
            ServerStatus.thisObj.server_info.put("last_expired_log_check", String.valueOf(System.currentTimeMillis()));
            Vector logs = new Vector();
            Common.getAllFileListing(logs, String.valueOf(new File_S(String.valueOf(new File_S(ServerStatus.change_vars_to_values_static(ServerStatus.SG("log_location"), null, null, null)).getCanonicalFile().getParentFile().getPath()) + "/logs/jobs/").getPath()) + "/", 9, false);
            int x2 = 0;
            while (logs != null && x2 < logs.size()) {
                Thread.sleep(1L);
                ServerStatus.thisObj.server_info.put("last_expired_log_check", String.valueOf(System.currentTimeMillis()));
                File_S log = (File_S)logs.elementAt(x2);
                if (log.isFile() && log.getName().toUpperCase().endsWith(".LOG")) {
                    if (log.getName().toUpperCase().startsWith("_") && System.currentTimeMillis() - log.lastModified() > 86400000L * ServerStatus.LG("recent_temp_job_log_days")) {
                        log.delete();
                    } else if (!log.getName().toUpperCase().startsWith("_") && System.currentTimeMillis() - log.lastModified() > 86400000L * ServerStatus.LG("recent_job_log_days")) {
                        log.delete();
                    }
                }
                ++x2;
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
        }
        ServerStatus.thisObj.server_info.put("last_expired_log_check", String.valueOf(System.currentTimeMillis()));
    }

    public void doFolderMonitor(String arg) throws Exception {
        Thread.sleep(1000L);
        Vector monitored_folders = ServerStatus.VG("monitored_folders");
        Vector filelist = new Vector();
        if (System.getProperty("crushftp.singleuser", "false").equals("true")) {
            return;
        }
        int x = 0;
        while (x < monitored_folders.size()) {
            block50: {
                long multiplier;
                long timeAmount;
                int scan_depth;
                File_U rFolder;
                Properties p;
                block52: {
                    block51: {
                        p = (Properties)monitored_folders.elementAt(x);
                        if (!p.getProperty("enabled", "true").equals("true") || p.getProperty("folder") == null || !(rFolder = new File_U(p.getProperty("folder"))).exists()) break block50;
                        filelist = new Vector();
                        scan_depth = 1;
                        if (p.getProperty("monitor_sub_folders", "true").equals("true")) {
                            scan_depth = 99;
                        }
                        timeAmount = Long.parseLong(p.getProperty("time_units_no"));
                        multiplier = 1000L;
                        multiplier = p.getProperty("time_units").equals("0") ? 60000L : (p.getProperty("time_units").equals("1") ? 3600000L : 86400000L);
                        if (!arg.equals("monitor_folders_instant") || timeAmount >= 0L) break block51;
                        timeAmount *= -1L;
                        multiplier = 1000L;
                        break block52;
                    }
                    if (arg.equals("monitor_folders_instant") && timeAmount >= 0L) break block50;
                }
                Common.getAllFileListing_U(filelist, p.getProperty("folder"), scan_depth, true);
                Vector<File_U> foundItems = new Vector<File_U>();
                int i = 0;
                while (i < filelist.size()) {
                    File_U currFilePointer;
                    if (p.getProperty("enabled", "true").equals("true") && !(currFilePointer = (File_U)filelist.elementAt(i)).getCanonicalPath().equals(rFolder.getCanonicalPath())) {
                        long lastMod = currFilePointer.lastModified() + multiplier * timeAmount;
                        if (System.currentTimeMillis() - lastMod > 0L) {
                            if (Common.machine_is_windows()) {
                                if (p.getProperty("folder_match", "*").indexOf("\\") > 0 && p.getProperty("folder_match", "*").indexOf("\\\\") < 0) {
                                    p.put("folder_match", p.getProperty("folder_match", "*").replace("\\", "\\\\"));
                                }
                                if (p.getProperty("folder_not_match", "*").indexOf("\\") > 0 && p.getProperty("folder_not_match", "*").indexOf("\\\\") < 0) {
                                    p.put("folder_not_match", p.getProperty("folder_not_match", "*").replace("\\", "\\\\"));
                                }
                                if (p.getProperty("folder_not_match_name", "*").indexOf("\\") > 0 && p.getProperty("folder_not_match_name", "*").indexOf("\\\\") < 0) {
                                    p.put("folder_not_match_name", p.getProperty("folder_not_match_name", "*").replace("\\", "\\\\"));
                                }
                                if (p.getProperty("folder_match", "*").indexOf("\\") < 0 && p.getProperty("folder_match", "*").indexOf("/") >= 0) {
                                    p.put("folder_match", p.getProperty("folder_match", "*").replace("/", "\\\\"));
                                }
                                if (p.getProperty("folder_not_match", "*").indexOf("\\") < 0 && p.getProperty("folder_not_match", "*").indexOf("/") >= 0) {
                                    p.put("folder_not_match", p.getProperty("folder_not_match", "*").replace("/", "\\\\"));
                                }
                                if (p.getProperty("folder_not_match_name", "*").indexOf("\\") < 0 && p.getProperty("folder_not_match_name", "*").indexOf("/") >= 0) {
                                    p.put("folder_not_match_name", p.getProperty("folder_not_match_name", "*").replace("/", "\\\\"));
                                }
                            }
                            if (!(!com.crushftp.client.Common.do_searches(p.getProperty("folder_match", "*"), currFilePointer.getAbsolutePath(), false, 0) || !p.getProperty("folder_not_match", "").equals("") && com.crushftp.client.Common.do_searches(p.getProperty("folder_not_match", ""), currFilePointer.getAbsolutePath(), false, 0) || !p.getProperty("folder_not_match_name", "").equals("") && com.crushftp.client.Common.do_searches(p.getProperty("folder_not_match_name", ""), currFilePointer.getName(), false, 0))) {
                                Vector emptyFolder;
                                Log.log("SERVER", 2, "Folder Monitor Match:" + p.getProperty("folder_match", "") + "  vs.  " + currFilePointer.getAbsolutePath());
                                Log.log("SERVER", 2, "Folder Monitor Not Match:" + p.getProperty("folder_not_match", "") + "  vs.  " + currFilePointer.getAbsolutePath());
                                if (p.getProperty("delete").equals("true")) {
                                    if (currFilePointer.isFile() && p.getProperty("monitor_files", "true").equals("true")) {
                                        if (p.getProperty("folderMonitorAction", "Archive or Delete").equals("Archive or Delete")) {
                                            Log.log("SERVER", 0, "FolderMonitor:Deleting file " + currFilePointer.getAbsolutePath());
                                            currFilePointer.delete();
                                        } else {
                                            foundItems.addElement(currFilePointer);
                                        }
                                    } else if (currFilePointer.isDirectory() && (p.getProperty("monitor_empty_folders", "false").equals("true") || p.getProperty("monitor_non_empty_folders", "false").equals("true"))) {
                                        Log.log("SERVER", 2, "FolderMonitor:Checking to see if folder is OK to delete: " + currFilePointer.getAbsolutePath());
                                        emptyFolder = new Vector();
                                        Common.getAllFileListing_U(emptyFolder, String.valueOf(currFilePointer.getCanonicalPath()) + "/", 99, true);
                                        boolean empty = true;
                                        int xx = 0;
                                        while (xx < emptyFolder.size()) {
                                            File_U ef = (File_U)emptyFolder.elementAt(xx);
                                            if (!ef.getName().startsWith(".") && (ef.isFile() && p.getProperty("empty_count_files", "true").equals("true") || ef.isDirectory() && p.getProperty("empty_count_folders", "false").equals("true"))) {
                                                empty = false;
                                                break;
                                            }
                                            ++xx;
                                        }
                                        if (empty || p.getProperty("monitor_non_empty_folders", "false").equals("true")) {
                                            String action;
                                            String string = action = p.getProperty("folderMonitorAction", "Archive or Delete").equals("Archive or Delete") ? "delete" : "archive";
                                            if (!currFilePointer.getCanonicalPath().equals(rFolder.getCanonicalPath())) {
                                                Log.log("SERVER", 0, "FolderMonitor:" + action + " folder " + currFilePointer.getAbsolutePath());
                                                Vector filelist2 = new Vector();
                                                Common.getAllFileListing_U(filelist2, String.valueOf(currFilePointer.getCanonicalPath()) + "/", 99, true);
                                                while (filelist2.size() > 0) {
                                                    File_U f2 = (File_U)filelist2.remove(filelist2.size() - 1);
                                                    long lastMod2 = f2.lastModified() + multiplier * timeAmount;
                                                    if (System.currentTimeMillis() - lastMod2 > 0L || f2.isDirectory()) {
                                                        if (!(!p.getProperty("folder_not_match", "").equals("") && com.crushftp.client.Common.do_searches(p.getProperty("folder_not_match", ""), f2.getCanonicalPath(), false, 0) || !p.getProperty("folder_not_match_name", "").equals("") && com.crushftp.client.Common.do_searches(p.getProperty("folder_not_match_name", ""), f2.getName(), false, 0))) {
                                                            Log.log("SERVER", 0, "FolderMonitor:" + action + " item " + f2.getAbsolutePath());
                                                            if (p.getProperty("folderMonitorAction", "Archive or Delete").equals("Archive or Delete")) {
                                                                f2.delete();
                                                                continue;
                                                            }
                                                            foundItems.addElement(f2);
                                                            continue;
                                                        }
                                                        Log.log("SERVER", 0, "FolderMonitor:Skipping item " + f2.getAbsolutePath() + " because of 'not match'.");
                                                        continue;
                                                    }
                                                    Log.log("SERVER", 2, "FolderMonitor:Skipping item " + f2.getAbsolutePath() + " because of date being too new on this subitem.");
                                                }
                                            }
                                        }
                                    }
                                } else if (currFilePointer.isFile() && p.getProperty("monitor_files", "true").equals("true")) {
                                    String srcFold = currFilePointer.getCanonicalPath();
                                    String destFold = String.valueOf(p.getProperty("zippath")) + currFilePointer.getCanonicalPath().substring(rFolder.getCanonicalPath().length());
                                    int count = 0;
                                    while (new File_U(destFold).exists() && count++ < 99) {
                                        destFold = String.valueOf(destFold) + count;
                                    }
                                    if (count >= 99) {
                                        destFold = String.valueOf(destFold) + Common.makeBoundary(4);
                                    }
                                    if (p.getProperty("folderMonitorAction", "Archive or Delete").equals("Archive or Delete")) {
                                        boolean moved;
                                        Log.log("SERVER", 0, "FolderMonitor:Moving file " + srcFold + " to " + destFold);
                                        new File_U(destFold).getCanonicalFile().getParentFile().mkdirs();
                                        boolean bl = moved = ServerStatus.BG("posix") ? false : new File_U(srcFold).renameTo(new File_U(destFold));
                                        if (!moved) {
                                            Common.recurseCopy_U(srcFold, destFold, true);
                                            Common.updateOSXInfo_U(destFold, "-R");
                                            currFilePointer.delete();
                                        }
                                    } else {
                                        foundItems.addElement(currFilePointer);
                                    }
                                } else if (currFilePointer.isDirectory() && (p.getProperty("monitor_empty_folders", "false").equals("true") || p.getProperty("monitor_non_empty_folders", "false").equals("true"))) {
                                    Log.log("SERVER", 2, "FolderMonitor:Checking to see if folder is OK to move: " + currFilePointer.getAbsolutePath());
                                    emptyFolder = new Vector();
                                    Common.getAllFileListing_U(emptyFolder, String.valueOf(currFilePointer.getCanonicalPath()) + "/", 99, true);
                                    boolean empty = true;
                                    int xx = 0;
                                    while (xx < emptyFolder.size()) {
                                        File_U ef = (File_U)emptyFolder.elementAt(xx);
                                        if (!ef.getName().startsWith(".") && ef.isFile()) {
                                            empty = false;
                                            break;
                                        }
                                        ++xx;
                                    }
                                    Log.log("SERVER", 2, "FolderMonitor:Checking to see if folder is OK to move: " + currFilePointer.getAbsolutePath() + " : empty=" + empty + " items=" + emptyFolder.size());
                                    if (empty || p.getProperty("monitor_non_empty_folders", "false").equals("true")) {
                                        String srcFold = currFilePointer.getAbsolutePath();
                                        String destFold = String.valueOf(p.getProperty("zippath")) + currFilePointer.getCanonicalPath().substring(rFolder.getCanonicalPath().length()) + "/";
                                        int count = 0;
                                        while (new File_U(destFold).exists() && count++ < 99) {
                                            destFold = String.valueOf(destFold) + count;
                                        }
                                        if (count >= 99) {
                                            destFold = String.valueOf(destFold) + Common.makeBoundary(4);
                                        }
                                        if (p.getProperty("folderMonitorAction", "Archive or Delete").equals("Archive or Delete")) {
                                            boolean moved;
                                            Log.log("SERVER", 0, "FolderMonitor:empty=" + empty + ":Moving folder " + srcFold + " to " + destFold);
                                            new File_U(destFold).getCanonicalFile().getParentFile().mkdirs();
                                            boolean bl = moved = ServerStatus.BG("posix") ? false : new File_U(srcFold).renameTo(new File_U(destFold));
                                            if (!moved) {
                                                Common.recurseCopy_U(srcFold, destFold, true);
                                                Common.updateOSXInfo_U(destFold, "-R");
                                                Common.recurseDelete_U(String.valueOf(currFilePointer.getCanonicalPath()) + "/", false);
                                            }
                                        } else {
                                            foundItems.addElement(currFilePointer);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    ++i;
                }
                if (foundItems.size() > 0) {
                    Vector<Properties> items = new Vector<Properties>();
                    int xx = 0;
                    while (xx < foundItems.size()) {
                        File_U f = (File_U)foundItems.elementAt(xx);
                        Properties item = new Properties();
                        item.put("url", f.toURI().toURL().toExternalForm());
                        item.put("the_file_name", f.getName());
                        item.put("name", f.getName());
                        item.put("modified", String.valueOf(f.lastModified()));
                        item.put("the_file_path", Common.all_but_last(f.getCanonicalPath()).substring(rFolder.getCanonicalPath().length()).replace('\\', '/'));
                        item.put("path", Common.all_but_last(f.getCanonicalPath()).substring(rFolder.getCanonicalPath().length()).replace('\\', '/'));
                        item.put("the_file_size", String.valueOf(f.length()));
                        item.put("size", String.valueOf(f.length()));
                        item.put("type", f.isDirectory() ? "DIR" : "FILE");
                        items.addElement(item);
                        ++xx;
                    }
                    Properties event = new Properties();
                    event.put("event_plugin_list", p.getProperty("folderMonitorAction", "Archive or Delete"));
                    event.put("name", "FolderMonitorEvent:" + p.getProperty("folder"));
                    this.events6.doEventPlugin(null, event, null, items);
                }
            }
            ++x;
        }
    }

    public void doGuiTimerUpdates() {
        ServerStatus.siPUT("total_server_bytes_transfered", "" + (this.total_server_bytes_sent + this.total_server_bytes_received));
        ServerStatus.siPUT("total_server_bytes_sent", "" + this.total_server_bytes_sent);
        ServerStatus.siPUT("total_server_bytes_received", "" + this.total_server_bytes_received);
        ServerStatus.siPUT("thread_pool_available", String.valueOf(Worker.availableWorkers.size()));
        ServerStatus.siPUT("thread_pool_busy", String.valueOf(Worker.busyWorkers.size()));
        ServerStatus.updateMemoryStats();
        ServerStatus.siPUT("dmz_mode", String.valueOf(com.crushftp.client.Common.dmz_mode));
        String cpu_usage = com.crushftp.client.Common.getCpuUsage();
        if (!cpu_usage.equals("")) {
            ServerStatus.siPUT("server_cpu", String.valueOf((int)Float.parseFloat(cpu_usage.split(":")[0])));
            ServerStatus.siPUT("os_cpu", String.valueOf((int)Float.parseFloat(cpu_usage.split(":")[1])));
            if (cpu_usage.split(":").length > 2) {
                ServerStatus.siPUT("open_files", cpu_usage.split(":")[2]);
            }
            if (cpu_usage.split(":").length > 3) {
                ServerStatus.siPUT("max_open_files", cpu_usage.split(":")[3]);
            }
        } else {
            ServerStatus.siPUT("server_cpu", "0");
            ServerStatus.siPUT("os_cpu", "0");
            ServerStatus.siPUT("open_files", "0");
            ServerStatus.siPUT("max_open_files", "0");
        }
        ServerStatus.calc_server_speeds(null, null);
        Vector<Properties> server_list_vec = null;
        try {
            server_list_vec = (Vector<Properties>)server_settings.get("server_list");
        }
        catch (Exception e) {
            Properties the_item = (Properties)server_settings.get("server_list");
            server_list_vec = new Vector<Properties>();
            server_list_vec.addElement(the_item);
        }
        int x = 0;
        while (x < this.main_servers.size()) {
            GenericServer the_server = (GenericServer)this.main_servers.elementAt(x);
            the_server.updateStatus();
            ++x;
        }
        while (ServerStatus.siVG("recent_user_list").size() > ServerStatus.IG("recent_user_count")) {
            ServerStatus.siVG("recent_user_list").removeElementAt(0);
        }
        ServerStatus.siPUT("total_logins", String.valueOf(ServerStatus.siIG("failed_logins") + ServerStatus.siIG("successful_logins")));
        ServerStatus.siPUT("users_connected", String.valueOf(this.getTotalConnectedUsers()));
        ServerStatus.siPUT("current_datetime_millis", String.valueOf(System.currentTimeMillis()));
        ServerStatus.siPUT("current_datetime_ddmmyyhhmmss", new SimpleDateFormat("MMddyyyyHHmmss", Locale.US).format(new Date()));
        this.update_history("logged_in_users");
        this.update_history("current_download_speed");
        this.update_history("current_upload_speed");
        this.update_history("ram_max");
        this.update_history("ram_free");
        this.update_history("server_cpu");
        this.update_history("os_cpu");
        this.update_history("open_files");
        this.update_history("incoming_transfers");
        this.update_history("outgoing_transfers");
        this.update_history("connected_unique_ips");
        Properties p = new Properties();
        p.put("server_settings", server_settings);
        p.put("server_info", this.server_info);
        p.put("action", "update_server_status");
        this.runPlugins(p);
    }

    public static void updateMemoryStats() {
        ServerStatus.siPUT("ram_max", String.valueOf(Runtime.getRuntime().maxMemory()));
        ServerStatus.siPUT("ram_free", String.valueOf(Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()));
        ServerStatus.siPUT("ram_used", String.valueOf(ServerStatus.siLG("ram_max") - ServerStatus.siLG("ram_free")));
        ServerStatus.siPUT("ram_used_percent", String.valueOf((int)((float)ServerStatus.siLG("ram_used") / (float)ServerStatus.siLG("ram_max") * 100.0f)));
        System.getProperties().put("crushftp.ram_used_percent", ServerStatus.siSG("ram_used_percent"));
    }

    public void doExtraUpdateActions() {
        Properties pp;
        int x = 0;
        while (x < this.previewWorkers.size()) {
            PreviewWorker preview = (PreviewWorker)this.previewWorkers.elementAt(x);
            preview.run(null);
            ++x;
        }
        if (ServerStatus.BG("s3crush_replicated")) {
            if (!System.getProperties().containsKey("crushftp.s3_replicated")) {
                System.getProperties().put("crushftp.s3_replicated", new Vector());
            }
            Vector v = (Vector)System.getProperties().get("crushftp.s3_replicated");
            while (v.size() > 0) {
                pp = (Properties)v.remove(0);
                pp.put("need_response", "false");
                SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.s3CrushClient.writeFs", "info", pp);
            }
        }
        if (ServerStatus.BG("glaciercrush_replicated")) {
            if (!System.getProperties().containsKey("crushftp.glacier_replicated")) {
                System.getProperties().put("crushftp.glacier_replicated", new Vector());
            }
            Vector v = (Vector)System.getProperties().get("crushftp.glacier_replicated");
            while (v.size() > 0) {
                pp = (Properties)v.remove(0);
                pp.put("need_response", "false");
                SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.glacierCrushClient.writeFs", "info", pp);
            }
        }
        this.monitor_thread_dump_port();
    }

    public void doLogFlush() {
        if (com.crushftp.client.Common.log != null && (this.loggingProvider1 != null || this.loggingProvider2 != null)) {
            while (com.crushftp.client.Common.log.size() > 0) {
                Object o = com.crushftp.client.Common.log.remove(0);
                String s = "";
                String tag = "";
                if (o instanceof String) {
                    tag = "PROXY";
                    s = o.toString();
                } else {
                    Properties p = (Properties)o;
                    s = p.getProperty("data");
                    tag = p.getProperty("tag");
                    if (ServerStatus.IG("log_debug_level") < Integer.parseInt(p.getProperty("level", "0"))) continue;
                }
                if (!System.getProperty("appname", "CrushFTP").equals("CrushFTP")) {
                    s = Common.replace_str(s, "com.crushftp.", "com." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".");
                    s = Common.replace_str(s, "crushftp.", "com." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".");
                }
                if (this.loggingProvider1 != null) {
                    this.loggingProvider1.append_log(s, tag, true);
                }
                if (this.loggingProvider2 == null) continue;
                this.loggingProvider2.append_log(s, tag, true);
            }
        }
    }

    public void setupGlobalPrefs() {
        System.getProperties().put("java.net.preferIPv4Stack", String.valueOf(ServerStatus.BG("force_ipv4")));
        System.getProperties().put("crushftp.debug", String.valueOf(ServerStatus.IG("log_debug_level")));
        System.getProperties().put("crushftp.lsla", String.valueOf(ServerStatus.BG("lsla2")));
        System.getProperties().put("crushftp.socketpooltimeout", String.valueOf(ServerStatus.IG("socketpool_timeout") * 1000));
        System.getProperties().put("crushftp.ls.year", String.valueOf(ServerStatus.BG("lsla_year")));
        System.getProperties().put("crushftp.max_threads", String.valueOf(ServerStatus.IG("max_threads")));
        System.getProperties().put("crushftp.http_buffer", String.valueOf(ServerStatus.IG("http_buffer")));
        System.getProperties().put("crushftp.file_client_not_found_error", String.valueOf(ServerStatus.BG("file_client_not_found_error")));
        System.getProperties().put("crushftp.memcache", System.getProperty("crushftp.memcache", String.valueOf(ServerStatus.BG("memcache"))));
        System.getProperties().put("crushftp.proxy.list.max", String.valueOf(ServerStatus.IG("proxy_list_max")));
        System.getProperties().put("crushftp.multi_journal", String.valueOf(ServerStatus.BG("multi_journal")));
        System.getProperties().put("crushftp.multi_journal_timeout", String.valueOf(ServerStatus.IG("multi_journal_timeout")));
        System.getProperties().put("crushftp.hash_algorithm", String.valueOf(ServerStatus.SG("hash_algorithm")));
        System.getProperties().put("crushftp.crushtask.store_job_items", String.valueOf(ServerStatus.BG("store_job_items")));
        System.getProperties().put("crushftp.ssl_renegotiation_blocked", String.valueOf(ServerStatus.BG("ssl_renegotiation_blocked")));
        System.getProperties().put("crushftp.tls_version_client", String.valueOf(ServerStatus.SG("tls_version_client")));
        System.getProperties().put("crushftp.s3_sha256", String.valueOf(ServerStatus.BG("s3_sha256")));
        System.getProperties().put("crushftp.replicated_vfs_url", String.valueOf(ServerStatus.SG("replicated_vfs_url")));
        System.getProperties().put("crushftp.replicated_vfs_root_url", String.valueOf(ServerStatus.SG("replicated_vfs_root_url")));
        System.getProperties().put("crushftp.replicated_vfs_user", String.valueOf(ServerStatus.SG("replicated_vfs_user")));
        System.getProperties().put("crushftp.replicated_vfs_pass", String.valueOf(ServerStatus.SG("replicated_vfs_pass")));
        System.getProperties().put("crushftp.replicated_vfs_ping_interval", String.valueOf(ServerStatus.SG("replicated_vfs_ping_interval")));
        System.getProperties().put("crushftp.replicated_auto_play_journal", String.valueOf(ServerStatus.SG("replicated_auto_play_journal")));
        System.getProperties().put("crushftp.s3_partial", String.valueOf(!ServerStatus.BG("s3_ignore_partial")));
        System.getProperties().put("crushftp.line_separator_crlf", String.valueOf(ServerStatus.BG("line_separator_crlf")));
        System.getProperties().put("crushftp.audit_job_logs", String.valueOf(ServerStatus.BG("audit_job_logs")));
        System.getProperties().put("crushftp.disable_mdtm_modifications", String.valueOf(ServerStatus.BG("disable_mdtm_modifications")));
        System.getProperties().put("crushftp.log_date_format", String.valueOf(ServerStatus.SG("log_date_format")));
        System.getProperties().put("crushftp.terrabytes_label_short", String.valueOf(ServerStatus.SG("terrabytes_label_short")));
        System.getProperties().put("crushftp.gigabytes_label_short", String.valueOf(ServerStatus.SG("gigabytes_label_short")));
        System.getProperties().put("crushftp.megabytes_label_short", String.valueOf(ServerStatus.SG("megabytes_label_short")));
        System.getProperties().put("crushftp.kilobytes_label_short", String.valueOf(ServerStatus.SG("kilobytes_label_short")));
        System.getProperties().put("crushftp.bytes_label_short", String.valueOf(ServerStatus.SG("bytes_label_short")));
        System.getProperties().put("crushftp.terrabytes_label", String.valueOf(ServerStatus.SG("terrabytes_label")));
        System.getProperties().put("crushftp.gigabytes_label", String.valueOf(ServerStatus.SG("gigabytes_label")));
        System.getProperties().put("crushftp.megabytes_label", String.valueOf(ServerStatus.SG("megabytes_label")));
        System.getProperties().put("crushftp.kilobytes_label", String.valueOf(ServerStatus.SG("kilobytes_label")));
        System.getProperties().put("crushftp.bytes_label", String.valueOf(ServerStatus.SG("bytes_label")));
        System.getProperties().put("crushftp.jobs_location", String.valueOf(ServerStatus.SG("jobs_location")));
        System.getProperties().put("crushftp.log_location", ServerStatus.change_vars_to_values_static(ServerStatus.SG("log_location"), null, null, null));
        System.getProperties().put("crushftp.log_debug_level", String.valueOf(ServerStatus.SG("log_debug_level")));
        System.getProperties().put("crushftp.smtp_helo_ip", String.valueOf(ServerStatus.SG("smtp_helo_ip")));
        System.getProperties().put("crushftp.smtp_subject_utf8", String.valueOf(ServerStatus.SG("smtp_subject_utf8")));
        System.getProperties().put("crushftp.smtp_subject_encoded", String.valueOf(ServerStatus.SG("smtp_subject_encoded")));
        System.getProperties().put("crushftp.smtp_xoauth2", String.valueOf(ServerStatus.SG("smtp_xoauth2")));
        System.getProperties().put("crushftp.debug_socks_log", String.valueOf(ServerStatus.BG("debug_socks_log")));
        System.getProperties().put("crushftp.enabled_ciphers", String.valueOf(ServerStatus.siSG("enabled_ciphers")));
        System.getProperties().put("crushftp.smtp.sasl", String.valueOf(ServerStatus.BG("crushftp_smtp_sasl")));
        System.getProperties().put("crushftp.pgp_check_downloads", String.valueOf(ServerStatus.BG("pgp_check_downloads")));
        System.getProperties().put("crushftp.ftpclient.list.log", String.valueOf(ServerStatus.BG("log_ftp_client_listings")));
        System.getProperties().put("crushftp.as2.sha256", String.valueOf(ServerStatus.SG("as2_sha256")));
        System.getProperties().put("crushftp.ftp_cwd_validate", String.valueOf(ServerStatus.SG("ftp_cwd_validate")));
        System.getProperties().put("maverick.dhBypassJCE", String.valueOf(ServerStatus.SG("ssh_bypass_jce2")));
        System.getProperties().put("crushftp.ssh_bouncycastle", String.valueOf(ServerStatus.SG("ssh_bouncycastle")));
        System.getProperties().put("crushftp.azure_upload_max_threads", String.valueOf(ServerStatus.SG("azure_upload_max_threads")));
        System.getProperties().put("crushftp.azure_share_list_threads_count", String.valueOf(ServerStatus.SG("azure_share_list_threads_count")));
        System.getProperties().put("crushftp.file.securedelete", String.valueOf(ServerStatus.SG("securedelete")));
        System.getProperties().put("crushftp.azure_upload_max_threads", String.valueOf(ServerStatus.SG("azure_upload_max_threads")));
        System.getProperties().put("crushftp.lowercase_all_s3_paths", String.valueOf(ServerStatus.BG("lowercase_all_s3_paths")));
        System.getProperties().put("crushftp.pgp_integrity_protect", String.valueOf(ServerStatus.BG("pgp_integrity_protect")));
        System.getProperties().put("crushftp.dmz_memory_queue", String.valueOf(ServerStatus.IG("dmz_memory_queue")));
        System.getProperties().put("crushftp.dmz_ping_test", String.valueOf(ServerStatus.BG("dmz_ping_test")));
        System.getProperties().put("crushftp.s3_one_delete_attempt", String.valueOf(ServerStatus.BG("s3_one_delete_attempt")));
        System.getProperties().put("crushftp.dmz_chunk_temp_storage", String.valueOf(ServerStatus.SG("dmz_chunk_temp_storage")));
        System.getProperties().put("crushftp.fips140_sftp_client", String.valueOf(ServerStatus.SG("fips140_sftp_client")));
        System.getProperties().put("crushftp.allow_symlink_checking", String.valueOf(ServerStatus.BG("allow_symlink_checking")));
        System.getProperties().put("crushftp.s3_use_contianer_credentials_relative_uri", String.valueOf(ServerStatus.BG("s3_use_contianer_credentials_relative_uri")));
        System.getProperties().put("crushftp.smtp_start_tls_allowed", String.valueOf(ServerStatus.BG("smtp_start_tls_allowed")));
        System.getProperties().put("crushftp.geoip_access_key", ServerStatus.SG("geoip_access_key"));
        System.getProperties().put("maverick.disableAutoFlush", String.valueOf(ServerStatus.BG("sftp_client_disableAutoFlush")));
        System.getProperties().put("crushftp.sftpclient_ls_dot", String.valueOf(ServerStatus.BG("sftpclient_ls_dot")));
        System.getProperties().put("crushftp.s3_global_cache", String.valueOf(ServerStatus.BG("s3_global_cache")));
        System.getProperties().put("crushftp.dfs_default_enabled", String.valueOf(ServerStatus.BG("dfs_default_enabled")));
        System.getProperties().put("crushftp.block_symlinks", String.valueOf(ServerStatus.BG("block_symlinks")));
        System.getProperties().put("crushftp.ssh_client_key_exchanges", String.valueOf(ServerStatus.SG("ssh_client_key_exchanges")));
        System.getProperties().put("crushftp.ssh_client_cipher_list", String.valueOf(ServerStatus.SG("ssh_client_cipher_list")));
        System.getProperties().put("crushftp.ssh_client_mac_list", String.valueOf(ServerStatus.SG("ssh_client_mac_list")));
        System.getProperties().put("crushftp.smb3_kerberos_kdc", String.valueOf(ServerStatus.SG("smb3_kerberos_kdc")));
        System.getProperties().put("crushftp.smb3_kerberos_realm", String.valueOf(ServerStatus.SG("smb3_kerberos_realm")));
        System.getProperties().put("crushftp.s3_ec2_imdsv2", String.valueOf(ServerStatus.SG("s3_ec2_imdsv2")));
        System.getProperties().put("maverick.disableDirectoryCheck", String.valueOf(ServerStatus.BG("sftp_client_listing_disableDirectoryCheck")));
        System.getProperties().put("crushftp.version_info_str", version_info_str);
        System.getProperties().put("crushftp.v11_beta", String.valueOf(ServerStatus.BG("v11_beta")));
        System.getProperties().put("crushftp.v10_beta", "true");
        com.crushftp.client.Common.System2.put("enterprise_level", String.valueOf(ServerStatus.siIG("enterprise_level")));
        DMZServerCommon.MAX_DMZ_SOCKET_IDLE_TIME = ServerStatus.IG("max_dmz_socket_idle_time");
        this.server_info.put("replication_vfs_count", System.getProperties().getProperty("crushftp.replciation.vfs.count", "0"));
        this.server_info.put("ram_pending_bytes_s3_upload", String.valueOf(S3Client.ram_used_upload));
        this.server_info.put("ram_pending_bytes_s3_download", String.valueOf(S3Client.ram_used_download));
        this.server_info.put("ram_pending_bytes_multisegment_upload", HTTPClient.ram_pending_bytes_multisegment.getProperty("upload", "0"));
        this.server_info.put("ram_pending_bytes_multisegment_download", HTTPClient.ram_pending_bytes_multisegment.getProperty("download", "0"));
        if (!ServerStatus.SG("jvm_timezone").equals("") && !ServerStatus.SG("jvm_timezone").equals("default")) {
            try {
                TimeZone.setDefault(TimeZone.getTimeZone(ServerStatus.SG("jvm_timezone")));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public void update_history(String key) {
        Vector<Object> v = (Vector<Object>)this.server_info.get(String.valueOf(key) + "_history");
        Object v2 = this.server_info.get(key);
        if (v == null) {
            v = new Vector<Object>();
            this.server_info.put(String.valueOf(key) + "_history", v);
        }
        if (v2 instanceof Vector) {
            this.server_info.put(String.valueOf(key) + "_count", String.valueOf(((Vector)v2).size()));
        }
        while (v.size() > 299) {
            v.remove(0);
        }
        Object o = ServerStatus.siOG(key);
        if (o instanceof String) {
            v.addElement(o);
        } else {
            v.addElement(com.crushftp.client.Common.CLONE(o));
        }
    }

    public void runPlugins(Properties info) {
        this.runPlugins(info, false);
    }

    public void runPlugins(Properties info, boolean debug) {
        Vector plugins = (Vector)server_settings.get("plugins");
        if (plugins != null) {
            int x = 0;
            while (x < plugins.size()) {
                Vector pluginPrefs = null;
                if (plugins.elementAt(x) instanceof Vector) {
                    pluginPrefs = (Vector)plugins.elementAt(x);
                } else {
                    pluginPrefs = new Vector();
                    pluginPrefs.addElement(plugins.elementAt(x));
                }
                int xx = 0;
                while (xx < pluginPrefs.size()) {
                    block9: {
                        if (!(pluginPrefs.elementAt(xx) instanceof String)) {
                            Properties pluginPref = (Properties)pluginPrefs.elementAt(xx);
                            if (debug) {
                                Log.log("PLUGIN", 2, String.valueOf(pluginPref.getProperty("pluginName")) + " : " + pluginPref.getProperty("subItem", ""));
                            }
                            try {
                                info.put("plugin_pref", pluginPref);
                                Common.runPlugin(pluginPref.getProperty("pluginName"), info, pluginPref.getProperty("subItem", ""));
                            }
                            catch (Exception e) {
                                if (e.getCause() == null) break block9;
                                Log.log("SERVER", 1, e.getCause());
                            }
                        }
                    }
                    ++xx;
                }
                ++x;
            }
        }
    }

    public void update_ip() {
        String new_ip = this.common_code.discover_ip();
        if (new_ip.equals("0.0.0.0")) {
            new_ip = ServerStatus.SG("discovered_ip");
            if (new_ip.equals("0.0.0.0")) {
                new_ip = com.crushftp.client.Common.getLocalIP();
            }
            this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|---" + LOC.G("Auto IP lookup failure (could not reach server)!") + "---", "ERROR");
        }
        if (!new_ip.equals("0.0.0.0") && Common.count_str(new_ip, ".") == 3) {
            server_settings.put("discovered_ip", new_ip);
        } else {
            this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|---" + LOC.G("Auto IP lookup failure (could not reach server, and could not detect local IP)!") + "---", "ERROR");
        }
        int x = 0;
        while (x < this.main_servers.size()) {
            GenericServer the_server = (GenericServer)this.main_servers.elementAt(x);
            the_server.updateStatus();
            ++x;
        }
    }

    public static int calc_server_up_speeds(String username, String ip) throws Exception {
        int speed = 0;
        Vector v = (Vector)ServerStatus.siVG("user_list").clone();
        int x = v.size() - 1;
        while (x >= 0) {
            try {
                SessionCrush theSession = (SessionCrush)((Properties)v.elementAt(x)).get("session");
                if (theSession.uiBG("receiving_file") && (username == null || username.equalsIgnoreCase(theSession.uiSG("user_name"))) && (ip == null || ip.equalsIgnoreCase(theSession.uiSG("user_ip")))) {
                    speed = (int)((long)speed + theSession.uiLG("current_transfer_speed"));
                }
            }
            catch (Exception e) {
                if (("" + e).indexOf("Interrupted") >= 0) {
                    throw e;
                }
                Log.log("SERVER", 1, e);
            }
            --x;
        }
        return speed + ServerStatus.getJobSpeeds("OUTGOING");
    }

    public static int count_users_up() throws Exception {
        int num_items = 0;
        Vector v = (Vector)ServerStatus.siVG("user_list").clone();
        int x = v.size() - 1;
        while (x >= 0) {
            try {
                SessionCrush theSession = (SessionCrush)((Properties)v.elementAt(x)).get("session");
                if (theSession.stor_files_pool_used.size() > 0 && !theSession.uiBG("pause_now")) {
                    ++num_items;
                }
            }
            catch (Exception e) {
                if (("" + e).indexOf("Interrupted") >= 0) {
                    throw e;
                }
                Log.log("SERVER", 2, e);
            }
            --x;
        }
        return num_items;
    }

    public static Vector get_transfer_times() throws Exception {
        Vector<String> timer = new Vector<String>();
        Vector v = (Vector)ServerStatus.siVG("user_list").clone();
        int x = v.size() - 1;
        while (x >= 0) {
            block7: {
                try {
                    SessionCrush theSession = (SessionCrush)((Properties)v.elementAt(x)).get("session");
                    if (theSession.uiLG("seconds_remaining") > 0L) {
                        timer.addElement(String.valueOf(theSession.uiLG("seconds_remaining")));
                    }
                }
                catch (Exception e) {
                    if (("" + e).indexOf("Interrupted") < 0) break block7;
                    throw e;
                }
            }
            --x;
        }
        x = 0;
        while (x < timer.size()) {
            int xx = x + 1;
            while (xx < timer.size()) {
                int num1 = Integer.parseInt(timer.elementAt(x).toString());
                int num2 = Integer.parseInt(timer.elementAt(xx).toString());
                if (num2 < num1) {
                    timer.setElementAt(String.valueOf(num2), x);
                    timer.setElementAt(String.valueOf(num1), xx);
                }
                ++xx;
            }
            ++x;
        }
        return timer;
    }

    public static int count_users_down() throws Exception {
        int num_items = 0;
        Vector v = (Vector)ServerStatus.siVG("user_list").clone();
        int x = v.size() - 1;
        while (x >= 0) {
            try {
                SessionCrush theSession = (SessionCrush)((Properties)v.elementAt(x)).get("session");
                if (theSession.retr_files_pool_used.size() > 0 && !theSession.uiBG("pause_now")) {
                    ++num_items;
                }
            }
            catch (Exception e) {
                if (("" + e).indexOf("Interrupted") >= 0) {
                    throw e;
                }
                Log.log("SERVER", 3, e);
            }
            --x;
        }
        return num_items;
    }

    public static int calc_server_down_speeds(String username, String ip) throws Exception {
        int speed = 0;
        Vector v = (Vector)ServerStatus.siVG("user_list").clone();
        int x = v.size() - 1;
        while (x >= 0) {
            try {
                SessionCrush theSession = (SessionCrush)((Properties)v.elementAt(x)).get("session");
                if (theSession.uiBG("sending_file") && (username == null || username.equalsIgnoreCase(theSession.uiSG("user_name"))) && (ip == null || ip.equalsIgnoreCase(theSession.uiSG("user_ip")))) {
                    speed = (int)((long)speed + theSession.uiLG("current_transfer_speed"));
                }
            }
            catch (Exception e) {
                if (("" + e).indexOf("Interrupted") >= 0) {
                    throw e;
                }
                Log.log("SERVER", 3, e);
            }
            --x;
        }
        return speed + ServerStatus.getJobSpeeds("INCOMING");
    }

    public static int getJobSpeeds(String transfer_direction) {
        int speed = 0;
        Vector vv = ServerStatus.siVG("running_tasks");
        int x = 0;
        while (x < vv.size()) {
            Properties tracker = (Properties)vv.elementAt(x);
            Vector active_items = (Vector)tracker.get("active_items");
            if (active_items != null) {
                int xx = 0;
                while (xx < active_items.size()) {
                    Properties active_item = (Properties)active_items.elementAt(xx);
                    if (active_item.getProperty(transfer_direction, "false").equals("true") && active_item.containsKey("the_file_transfer_speed")) {
                        long the_file_transfer_speed = Long.parseLong(active_item.getProperty("the_file_transfer_speed", "0"));
                        speed = (int)((long)speed + the_file_transfer_speed / 1024L);
                    }
                    ++xx;
                }
            }
            ++x;
        }
        return speed;
    }

    public static int calc_server_speeds(String username, String ip) {
        int speed = 0;
        try {
            int downSpeed = ServerStatus.calc_server_down_speeds(username, ip);
            speed += downSpeed;
            int upSpeed = ServerStatus.calc_server_up_speeds(username, ip);
            speed += upSpeed;
            ServerStatus.siPUT("current_download_speed", "" + downSpeed);
            ServerStatus.siPUT("current_upload_speed", "" + upSpeed);
        }
        catch (Exception e) {
            Log.log("SERVER", 3, e);
        }
        return speed;
    }

    public void quit_server(boolean override_restricted) {
        if (!System.getProperty("crushftp.security.stop_start", "true").equals("true") && !override_restricted) {
            Log.log("SERVER", 0, "###CRUSHFTP RESTRICTED MODE IN USE, SHUTDOWN BLOCKED!");
            return;
        }
        this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|********" + System.getProperty("appname", "CrushFTP") + " " + LOC.G("Quit") + "******** " + ServerStatus.siSG("version_info_str") + ServerStatus.siSG("sub_version_info_str"), "QUIT_SERVER");
        try {
            this.loggingProvider2.shutdown();
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            this.loggingProvider1.shutdown();
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            Thread.sleep(2000L);
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
        System.exit(0);
    }

    public void append_log(String log_data, String tag) {
        Properties p = new Properties();
        p.put("tag", tag);
        p.put("level", "0");
        p.put("data", log_data);
        Log.log(tag, 0, log_data);
    }

    public void save_server_settings(boolean autoSave) {
        this.prefsProvider.check_code();
        if (this.starting) {
            return;
        }
        if (autoSave && !ServerStatus.BG("allow_auto_save")) {
            return;
        }
        this.prefsProvider.savePrefs(server_settings, null);
    }

    public static void put_in(String key, Object data) {
        try {
            ServerStatus.thisObj.server_info.put(key, data);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void do_auto_update_early(final boolean webOnly, final boolean single_thread) throws Exception {
        ServerStatus.siPUT("update_when_idle", "false");
        final Properties status = new Properties();
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        if (ServerStatus.this.updateHandler.doSilentUpdate(true, version_info_str, webOnly)) {
                            status.put("status", "completed");
                            if (single_thread) {
                                Thread.sleep(5000L);
                            }
                            if (!webOnly) {
                                ServerStatus.this.restart_crushftp();
                            }
                        } else {
                            status.put("status", "failed");
                        }
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 0, e);
                    }
                }
            });
        }
        catch (Exception e) {
            Log.log("SERVER", 1, e);
            throw e;
        }
        if (single_thread) {
            int x = 0;
            while (x < 240 && status.size() == 0) {
                Thread.sleep(500L);
                ++x;
            }
        }
    }

    public void doCheckForUpdate(boolean checkBuild) {
        Properties p = new Properties();
        p.put("version", version_info_str);
        p.put("check_build", String.valueOf(checkBuild));
        Common.checkForUpdate(p);
        if (!p.getProperty("version", "").equals(version_info_str)) {
            if (checkBuild && p.getProperty("version", "").equals(String.valueOf(version_info_str) + sub_version_info_str)) {
                return;
            }
            this.server_info.put("update_available", "true");
            this.server_info.put("update_available_version", p.getProperty("version"));
            this.server_info.put("update_available_html", p.getProperty("html"));
            this.runAlerts("update", null);
        }
    }

    public void restart_crushftp() {
        if (!System.getProperty("crushftp.security.stop_start", "true").equals("true")) {
            Log.log("SERVER", 0, "###CRUSHFTP RESTRICTED MODE IN USE, RESTART BLOCKED!  YOU MUST RESTART MANUALLY!");
            return;
        }
        this.save_server_settings(false);
        this.starting = true;
        this.stop_all_servers();
        this.shutdown.run();
        try {
            Thread.sleep(1000L);
            if (Common.machine_is_windows()) {
                Runtime.getRuntime().exec(("sc start " + System.getProperty("appname", "CrushFTP") + "Restart").split(" "));
            } else if (Common.machine_is_x()) {
                Runtime.getRuntime().exec(new String[]{"launchctl", "start", "com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + "Update"});
            } else if (!ServerStatus.SG("restart_script").trim().equals("")) {
                Runtime.getRuntime().exec(ServerStatus.SG("restart_script").split(";"));
            } else {
                Runtime.getRuntime().exec(new String[]{"/usr/sbin/service", System.getProperty("appname", "CrushFTP").toLowerCase(), "restart"});
            }
        }
        catch (Exception ee) {
            ee.printStackTrace();
        }
        this.quit_server(false);
    }

    public boolean kick(String the_user, boolean logit) {
        Vector v = (Vector)ServerStatus.siVG("user_list").clone();
        int x = v.size() - 1;
        while (x >= 0) {
            Properties p = (Properties)v.elementAt(x);
            if (p.getProperty("id").equalsIgnoreCase(the_user)) {
                return this.kick(p, logit);
            }
            --x;
        }
        return false;
    }

    public boolean kick(String the_user) {
        return this.kick(the_user, true);
    }

    public boolean kick(Properties user_info) {
        return this.kick(user_info, true);
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean kick(Properties user_info, boolean logit) {
        try {
            theSession = (SessionCrush)user_info.get("session");
            if (logit) {
                try {
                    this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|---" + LOC.G("User Kicked") + "---:" + theSession.uiSG("user_number") + "-" + theSession.uiSG("user_name"), "KICK");
                    Log.log("SERVER", 1, new Exception("User kicked trace:" + theSession.uiSG("user_number") + "-" + theSession.uiSG("user_name")));
                }
                catch (Exception var4_5) {
                    // empty catch block
                }
            }
            try {
                this.remove_user(user_info);
            }
            catch (Exception var4_6) {
                // empty catch block
            }
            if (theSession == null) return true;
            if (theSession.uiBG("dieing") != false) return true;
            if (logit) {
                theSession.uiPUT("termination_message", "KICKED");
            }
            theSession.uiPUT("friendly_quit", "true");
            theSession.not_done = false;
            theSession.killSession();
            try {
                while (theSession.session_socks.size() > 0) {
                    sock = (Socket)theSession.session_socks.remove(0);
                    try {
                        Worker.startWorker(new Runnable(){

                            @Override
                            public void run() {
                                try {
                                    sock.setSoTimeout(2000);
                                    sock.setSoLinger(true, 2);
                                    sock.close();
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                        });
                    }
                    catch (IOException var5_11) {
                        // empty catch block
                    }
                }
                ** GOTO lbl-1000
            }
            catch (Exception sock) {
                try lbl-1000:
                // 3 sources

                {
                    while (theSession.old_data_socks.size() > 0) {
                        tempSock = (Socket)theSession.old_data_socks.remove(0);
                        tempSock.close();
                    }
                }
                catch (Exception tempSock) {
                    // empty catch block
                }
            }
            if (user_info.getProperty("hack_username", "false").equals("true") != false) return true;
            try {
                info = new Properties();
                info.put("alert_type", "kick");
                info.put("alert_sub_type", "ip");
                info.put("alert_timeout", "0");
                info.put("alert_max", "0");
                info.put("alert_msg", user_info.getProperty("user_name"));
                this.runAlerts("security_alert", info, user_info, theSession);
                return true;
            }
            catch (Exception e) {
                Log.log("BAN", 1, e);
            }
            return true;
        }
        catch (Exception e) {
            Log.log("SERVER", 1, e);
            return false;
        }
    }

    public boolean passive_kick(Properties user_info) {
        boolean success = true;
        try {
            SessionCrush theSession = (SessionCrush)user_info.get("session");
            try {
                this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|---" + LOC.G("User Kicked") + "---:" + theSession.uiSG("user_number") + "-" + theSession.uiSG("user_name"), "KICK");
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|---" + LOC.G("Kicking session: passive_kick.") + "---", "KICK");
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (!theSession.uiBG("dieing")) {
                theSession.uiPUT("termination_message", "KICKED");
                theSession.uiPUT("friendly_quit", "true");
                theSession.not_done = false;
                theSession.do_kill(null);
            }
        }
        catch (Exception e) {
            success = false;
            Log.log("SERVER", 1, e);
        }
        return success;
    }

    public boolean ban(String the_user, String reason) {
        return this.ban(the_user, 0, reason);
    }

    public boolean ban(String the_user, int timeout, String reason) {
        Properties p;
        Vector v = (Vector)ServerStatus.siVG("user_list").clone();
        int x = v.size() - 1;
        while (x >= 0) {
            p = (Properties)v.elementAt(x);
            if (p.getProperty("id").equalsIgnoreCase(the_user)) {
                return this.ban(p, timeout, false, reason);
            }
            --x;
        }
        x = 0;
        while (x < ServerStatus.siVG("recent_user_list").size()) {
            p = (Properties)ServerStatus.siVG("recent_user_list").elementAt(x);
            if (p.getProperty("id").equalsIgnoreCase(the_user)) {
                return this.ban(p, timeout, false, reason);
            }
            ++x;
        }
        return false;
    }

    public boolean ban(Properties user_info, int timeout, String reason) {
        return this.ban(user_info, timeout, false, reason);
    }

    public boolean ban(Properties user_info, int timeout, boolean onlyRealBan, String reason) {
        block4: {
            try {
                String new_ip_text = user_info.getProperty("user_ip");
                new_ip_text = new_ip_text.substring(new_ip_text.indexOf("/") + 1, new_ip_text.length());
                if (!this.ban_ip(new_ip_text, timeout, onlyRealBan, String.valueOf(reason) + ":" + user_info.getProperty("user_name"))) break block4;
                try {
                    this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|---" + LOC.G("User Banned") + "---:" + user_info.getProperty("user_number") + "-" + user_info.getProperty("user_name") + "  " + new_ip_text, "BAN");
                }
                catch (Exception exception) {
                    // empty catch block
                }
                return true;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return false;
    }

    public boolean ban_ip(String ip, int timeout, String reason) throws Exception {
        return this.ban_ip(ip, timeout, false, reason);
    }

    public boolean ban_ip(String ip, int timeout, boolean onlyRealBan, String reason) throws Exception {
        if (ip.contains(".")) {
            return this.ban_ipv4(ip, timeout, false, reason, true);
        }
        if (ip.contains(":")) {
            return this.ban_ipv6(ip, timeout, false, reason, true);
        }
        return false;
    }

    /*
     * Exception decompiling
     */
    public boolean ban_ipv4(String ip, int timeout, boolean onlyRealBan, String reason, boolean replicate) throws Exception {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [7[TRYBLOCK]], but top level block is 42[WHILELOOP]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    /*
     * Exception decompiling
     */
    public boolean ban_ipv6(String ip, int timeout, boolean onlyRealBan, String reason, boolean replicate) throws Exception {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [3[TRYBLOCK], 2[TRYBLOCK]], but top level block is 30[WHILELOOP]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    private static String uSG(Properties user_info, String key) {
        if (user_info == null) {
            return "";
        }
        return user_info.getProperty(key, "");
    }

    private static int uIG(Properties user_info, String data) {
        try {
            return Integer.parseInt(ServerStatus.uSG(user_info, data));
        }
        catch (Exception exception) {
            return 0;
        }
    }

    private static long uLG(Properties user_info, String data) {
        try {
            return Long.parseLong(ServerStatus.uSG(user_info, data));
        }
        catch (Exception exception) {
            return 0L;
        }
    }

    private static boolean uBG(Properties user_info, String data) {
        return ServerStatus.uSG(user_info, data).toLowerCase().equals("true");
    }

    public String change_vars_to_values(String in_str, SessionCrush the_session) {
        if (the_session != null) {
            return this.change_vars_to_values(in_str, the_session.user, the_session.user_info, the_session);
        }
        return this.change_vars_to_values(in_str, new Properties(), new Properties(), the_session);
    }

    public String change_vars_to_values(String in_str, Properties user, Properties user_info, SessionCrush the_session) {
        return ServerStatus.change_vars_to_values_static(in_str, user, user_info, the_session);
    }

    public static String change_vars_to_values_static(String in_str, Properties user, Properties user_info, SessionCrush the_session) {
        try {
            if (in_str.indexOf(37) < 0 && in_str.indexOf(123) < 0 && in_str.indexOf(125) < 0 && in_str.indexOf(60) < 0) {
                return in_str;
            }
            String r1 = "%";
            String r2 = "%";
            int r = 0;
            while (r < 2) {
                String user_key2;
                String user_key;
                String key;
                int loc;
                String key2;
                if (in_str.indexOf(r1) >= 0) {
                    in_str = ServerStatus.parse_server_messages(in_str);
                }
                if (in_str.indexOf(String.valueOf(r1) + "ldap_") >= 0) {
                    while (in_str.indexOf(String.valueOf(r1) + "ldap_") >= 0) {
                        key2 = in_str.substring(in_str.indexOf(String.valueOf(r1) + "ldap_"), in_str.indexOf(r2, in_str.indexOf(String.valueOf(r1) + "ldap_") + 1) + 1);
                        in_str = Common.replace_str(in_str, key2, ServerStatus.uSG(user, key2.substring(1, key2.length() - 1)));
                    }
                }
                if (in_str.indexOf(String.valueOf(r1) + "admin_user_") >= 0 && the_session != null) {
                    while (in_str.indexOf(String.valueOf(r1) + "admin_user_") >= 0) {
                        key2 = in_str.substring(in_str.indexOf(String.valueOf(r1) + "admin_user_"), in_str.indexOf(r2, in_str.indexOf(String.valueOf(r1) + "admin_user_") + 1) + 1);
                        in_str = Common.replace_str(in_str, key2, the_session.user.getProperty(key2.substring(1 + "admin_user_".length(), key2.length() - 1)));
                    }
                }
                String user_var = "user_";
                if (in_str.indexOf(String.valueOf(r1) + user_var) >= 0) {
                    loc = in_str.indexOf(String.valueOf(r1) + user_var);
                    while (loc >= 0) {
                        key = in_str.substring(loc, in_str.indexOf(r2, loc + 1) + 1);
                        user_key = key.substring((String.valueOf(r1) + user_var).length(), key.length() - 1);
                        user_key2 = String.valueOf(user_var) + user_key;
                        if (user_key.equals("user_sfv")) {
                            user_key = "user_md5";
                            in_str = in_str.replaceAll("CRC32", "MD5");
                        }
                        if (user_key2.equals("user_sfv")) {
                            user_key2 = "user_md5";
                            in_str = in_str.replaceAll("CRC32", "MD5");
                        }
                        if (!user_key2.equals("user_password")) {
                            if (user != null && user.containsKey(user_key)) {
                                in_str = Common.replace_str(in_str, key, ServerStatus.uSG(user, user_key));
                            } else if (user_info != null && user_info.containsKey(user_key)) {
                                in_str = Common.replace_str(in_str, key, ServerStatus.uSG(user_info, user_key));
                            } else if (user != null && user.containsKey(user_key2)) {
                                in_str = Common.replace_str(in_str, key, ServerStatus.uSG(user, user_key2));
                            } else if (user_info != null && user_info.containsKey(user_key2)) {
                                in_str = Common.replace_str(in_str, key, ServerStatus.uSG(user_info, user_key2));
                            } else if (user_key2.equalsIgnoreCase("user_dir")) {
                                String cd = user_info.getProperty("current_dir", "/");
                                if (cd != null && user != null && cd.toUpperCase().startsWith(user.getProperty("root_dir", "").toUpperCase())) {
                                    cd = cd.substring(user.getProperty("root_dir").length() - 1);
                                }
                                in_str = Common.replace_str(in_str, key, cd);
                            }
                        }
                        ++loc;
                        loc = in_str.indexOf(String.valueOf(r1) + user_var, loc);
                    }
                }
                if (com.crushftp.client.Common.dmz_mode) {
                    user_var = "user_dmz_";
                    if (in_str.indexOf(String.valueOf(r1) + user_var) >= 0) {
                        loc = in_str.indexOf(String.valueOf(r1) + user_var);
                        while (loc >= 0) {
                            key = in_str.substring(loc, in_str.indexOf(r2, loc + 1) + 1);
                            user_key = key.substring((String.valueOf(r1) + user_var).length(), key.length() - 1);
                            user_key2 = String.valueOf(user_var) + user_key;
                            if (!user_key2.equals("user_password")) {
                                if (user != null && user.containsKey(user_key)) {
                                    in_str = Common.replace_str(in_str, key, ServerStatus.uSG(user, user_key));
                                } else if (user_info != null && user_info.containsKey(user_key)) {
                                    in_str = Common.replace_str(in_str, key, ServerStatus.uSG(user_info, user_key));
                                } else if (user != null && user.containsKey(user_key2)) {
                                    in_str = Common.replace_str(in_str, key, ServerStatus.uSG(user, user_key2));
                                } else if (user_info != null && user_info.containsKey(user_key2)) {
                                    in_str = Common.replace_str(in_str, key, ServerStatus.uSG(user_info, user_key2));
                                }
                            }
                            ++loc;
                            loc = in_str.indexOf(String.valueOf(r1) + user_var, loc);
                        }
                    }
                }
                if (in_str.indexOf(String.valueOf(r1) + "beep" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "beep" + r2, "");
                }
                if (in_str.indexOf(String.valueOf(r1) + "hostname" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "hostname" + r2, hostname);
                }
                if (in_str.indexOf(String.valueOf(r1) + "server_time_date" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "server_time_date" + r2, new Date().toString());
                }
                if (in_str.indexOf(String.valueOf(r1) + "login_number" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "login_number" + r2, ServerStatus.uSG(user_info, "user_number"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "users_connected" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "users_connected" + r2, "" + thisObj.getTotalConnectedUsers());
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_password" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_password" + r2, ServerStatus.uSG(user_info, "current_password"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_name" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_name" + r2, ServerStatus.uSG(user, "username"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_anonymous_password" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_anonymous_password" + r2, ServerStatus.uSG(user_info, "user_name").equalsIgnoreCase("anonymous") ? ServerStatus.uSG(user_info, "current_password") : "");
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_current_dir" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_current_dir" + r2, the_session.get_PWD());
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_sessionid" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_sessionid" + r2, ServerStatus.uSG(user_info, "CrushAuth"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_site_commands_text" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_site_commands_text" + r2, ServerStatus.uSG(user, "site"));
                }
                try {
                    if (in_str.indexOf(String.valueOf(r1) + "user_time_remaining" + r2) >= 0) {
                        String time_str = String.valueOf(ServerStatus.uLG(user_info, "seconds_remaining")) + " secs";
                        if (ServerStatus.uLG(user_info, "seconds_remaining") == 0L) {
                            time_str = "<None Active>";
                        }
                        user_info.put("last_time_remaining", time_str);
                        if (ServerStatus.uLG(user_info, "seconds_remaining") > 60L) {
                            time_str = String.valueOf(ServerStatus.uLG(user_info, "seconds_remaining") / 60L) + "min, " + (ServerStatus.uLG(user_info, "seconds_remaining") - ServerStatus.uLG(user_info, "seconds_remaining") / 60L * 60L) + " secs";
                        }
                        in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_time_remaining" + r2, time_str);
                        user_info.put("last_time_remaining", time_str);
                    }
                }
                catch (Exception e) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_time_remaining" + r2, ServerStatus.uSG(user_info, "last_time_remaining"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_paused" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_paused" + r2, ServerStatus.uBG(user_info, "pause_now") ? "!PAUSED!" : "");
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_bytes_remaining" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_bytes_remaining" + r2, "" + (ServerStatus.uLG(user_info, "file_length") - (ServerStatus.uLG(user_info, "bytes_sent") - ServerStatus.uLG(user_info, "start_transfer_byte_amount"))));
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_pasv_port" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_pasv_port" + r2, "" + ServerStatus.uIG(user_info, "PASV_port"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_ratio" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_ratio" + r2, ServerStatus.uSG(user, "ratio") + " to 1");
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_perm_ratio" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_perm_ratio" + r2, ServerStatus.uBG(user, "perm_ratio") ? "Yes" : "No");
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_reverse_ip" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_reverse_ip" + r2, InetAddress.getByName(ServerStatus.uSG(user, "user_ip")).getHostName());
                }
                if (in_str.indexOf(String.valueOf(r1) + "tunnels" + r2) >= 0) {
                    String userTunnels = String.valueOf(user.getProperty("tunnels", "")) + ",";
                    Vector tunnels = ServerStatus.VG("tunnels");
                    ByteArrayOutputStream baot = new ByteArrayOutputStream();
                    int x = 0;
                    while (x < tunnels.size()) {
                        ByteArrayOutputStream baot2 = new ByteArrayOutputStream();
                        Properties p = (Properties)tunnels.elementAt(x);
                        if (userTunnels.indexOf(String.valueOf(p.getProperty("id")) + ",") >= 0 && !p.getProperty("tunnelType", "HTTP").equals("SSH")) {
                            p.store(baot2, "");
                            String s = new String(baot2.toByteArray(), "UTF8");
                            s = Common.url_encode(s);
                            baot.write(s.getBytes("UTF8"));
                            baot.write(";;;".getBytes());
                        }
                        ++x;
                    }
                    String tunnelsStr = new String(baot.toByteArray(), "UTF8").replace('%', '~');
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "tunnels" + r2, tunnelsStr);
                }
                if (in_str.indexOf(String.valueOf(r1) + "last_login_date_time" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "last_login_date_time" + r2, ServerStatus.siSG("last_login_date_time"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "last_login_ip" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "last_login_ip" + r2, ServerStatus.siSG("last_login_ip"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "last_login_user" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "last_login_user" + r2, ServerStatus.siSG("last_login_user"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "failed_logins" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "failed_logins" + r2, "" + ServerStatus.siIG("failed_logins"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "successful_logins" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "successful_logins" + r2, "" + ServerStatus.siIG("successful_logins"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "total_logins" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "total_logins" + r2, "" + (ServerStatus.siIG("failed_logins") + ServerStatus.siIG("successful_logins")));
                }
                if (in_str.indexOf(String.valueOf(r1) + "downloaded_files" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "downloaded_files" + r2, "" + ServerStatus.siIG("downloaded_files"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "uploaded_files" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "uploaded_files" + r2, "" + ServerStatus.siIG("uploaded_files"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "bytes_received_f" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "bytes_received_f" + r2, ServerStatus.siSG("total_server_bytes_received"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "bytes_sent_f" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "bytes_sent_f" + r2, ServerStatus.siSG("total_server_bytes_sent"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "total_bytes_f" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "total_bytes_f" + r2, ServerStatus.siSG("total_server_bytes_transfered"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "max_server_download_speed" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "max_server_download_speed" + r2, ServerStatus.SG("max_server_download_speed"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "max_server_upload_speed" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "max_server_upload_speed" + r2, ServerStatus.SG("max_server_upload_speed"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "bytes_received" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "bytes_received" + r2, ServerStatus.siSG("total_server_bytes_received"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "bytes_sent" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "bytes_sent" + r2, ServerStatus.siSG("total_server_bytes_sent"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "total_bytes" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "total_bytes" + r2, ServerStatus.siSG("total_server_bytes_transfered"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "current_server_downloading_count" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "current_server_downloading_count" + r2, "" + ServerStatus.count_users_down());
                }
                if (in_str.indexOf(String.valueOf(r1) + "current_server_uploading_count" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "current_server_uploading_count" + r2, "" + ServerStatus.count_users_up());
                }
                if (in_str.indexOf(String.valueOf(r1) + "current_download_speed" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "current_download_speed" + r2, ServerStatus.siSG("current_download_speed"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "current_upload_speed" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "current_upload_speed" + r2, ServerStatus.siSG("current_upload_speed"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "max_users" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "max_users" + r2, ServerStatus.SG("max_users"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "ip" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "ip" + r2, ServerStatus.siSG("discovered_ip"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "beep_connect" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "beep_connect" + r2, ServerStatus.SG("beep_connect"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "deny_reserved_ports" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "deny_reserved_ports" + r2, ServerStatus.SG("deny_reserved_ports"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "deny_fxp" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "deny_fxp" + r2, ServerStatus.SG("deny_fxp"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "about_info_str" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "about_info" + r2, ServerStatus.siSG("about_info_str"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "version_info" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "version_info" + r2, ServerStatus.siSG("version_info_str"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "start_time" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "start_time" + r2, ServerStatus.siSG("server_start_time"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "thread_count" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "thread_count" + r2, "" + Thread.activeCount());
                }
                if (in_str.indexOf(String.valueOf(r1) + "free_memory" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "free_memory" + r2, "" + Runtime.getRuntime().freeMemory() / 1024L);
                }
                if (in_str.indexOf(String.valueOf(r1) + "thread_dump" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "thread_dump" + r2, com.crushftp.client.Common.dumpStack("THREAD_DUMP"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "heap_dump" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "heap_dump" + r2, new HeapDumper().dump());
                }
                if (in_str.indexOf(String.valueOf(r1) + "plus" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "plus" + r2, "+");
                }
                if (in_str.indexOf(String.valueOf(r1) + "working_dir" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "working_dir" + r2, String.valueOf(new File("./").getCanonicalPath().replace('\\', '/')) + "/");
                }
                if (in_str.indexOf(String.valueOf(r1) + "global_") >= 0) {
                    if (com.crushftp.client.Common.System2.get("global_variables") == null) {
                        com.crushftp.client.Common.System2.put("global_variables", new Properties());
                    }
                    Properties global_variables = (Properties)com.crushftp.client.Common.System2.get("global_variables");
                    Enumeration<Object> keys = global_variables.keys();
                    while (keys.hasMoreElements()) {
                        String key3 = keys.nextElement().toString();
                        if (in_str.indexOf(String.valueOf(r1) + key3 + r2) < 0) continue;
                        in_str = Common.replace_str(in_str, String.valueOf(r1) + key3 + r2, global_variables.getProperty(key3, ""));
                    }
                }
                if (the_session != null && the_session.server_item != null) {
                    Enumeration<Object> keys = the_session.server_item.keys();
                    while (keys.hasMoreElements()) {
                        key = keys.nextElement().toString();
                        if (in_str.indexOf(String.valueOf(r1) + key + r2) < 0) continue;
                        in_str = Common.replace_str(in_str, String.valueOf(r1) + key + r2, the_session.server_item.getProperty(key, ""));
                    }
                }
                while (in_str.indexOf(String.valueOf(r1) + "customData_") >= 0) {
                    String custom = in_str.substring(in_str.indexOf(String.valueOf(r1) + "customData_") + (String.valueOf(r1) + "customData_").length());
                    custom = custom.substring(0, custom.indexOf(r2));
                    Properties customData = (Properties)server_settings.get("customData");
                    String val = customData.getProperty(custom, "");
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "customData_" + custom + r2, val);
                }
                if (user != null && user.containsKey("ichain") && (in_str.indexOf("group") >= 0 || in_str.indexOf("inheritance") >= 0)) {
                    Vector ichain = (Vector)user.get("ichain");
                    int x = 0;
                    while (x < ichain.size()) {
                        in_str = Common.replace_str(in_str, String.valueOf(r1) + x + "group" + r2, "" + ichain.elementAt(x));
                        in_str = Common.replace_str(in_str, String.valueOf(r1) + x + "inheritance" + r2, "" + ichain.elementAt(x));
                        ++x;
                    }
                    x = ichain.size() - 1;
                    while (x >= 0) {
                        in_str = Common.replace_str(in_str, String.valueOf(r1) + "group" + x + r2, "" + ichain.elementAt(x));
                        in_str = Common.replace_str(in_str, String.valueOf(r1) + "inheritance" + x + r2, "" + ichain.elementAt(x));
                        --x;
                    }
                }
                if (in_str.indexOf(String.valueOf(r1) + "ban" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "ban" + r2, "");
                    thisObj.ban(user_info, 0, "msg variable");
                }
                if (in_str.indexOf(String.valueOf(r1) + "kick" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "kick" + r2, "");
                    thisObj.passive_kick(user_info);
                }
                if (in_str.indexOf("<SPACE>") >= 0) {
                    in_str = Common.space_encode(in_str);
                }
                if (in_str.indexOf("<FREESPACE>") >= 0) {
                    in_str = Common.free_space(in_str);
                }
                if (in_str.indexOf("<URL>") >= 0) {
                    in_str = Common.url_encoder(in_str);
                }
                if (in_str.indexOf("<REVERSE_IP>") >= 0) {
                    in_str = Common.reverse_ip(in_str);
                }
                if (in_str.indexOf("<SOUND>") >= 0) {
                    in_str = ServerStatus.thisObj.common_code.play_sound(in_str);
                }
                if (in_str.indexOf("<LIST>") >= 0) {
                    in_str = thisObj.get_dir_list(in_str, the_session);
                }
                if (in_str.indexOf("<INCLUDE>") >= 0) {
                    in_str = thisObj.do_include_file_command(in_str);
                }
                r1 = "{";
                r2 = "}";
                ++r;
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 2, e);
        }
        return in_str;
    }

    public static String change_user_safe_vars_to_values_static(String in_str, Properties user, Properties user_info, SessionCrush the_session) {
        try {
            if (in_str.indexOf(37) < 0 && in_str.indexOf(123) < 0 && in_str.indexOf(125) < 0 && in_str.indexOf(60) < 0) {
                return in_str;
            }
            String r1 = "%";
            String r2 = "%";
            int r = 0;
            while (r < 2) {
                if (in_str.indexOf(r1) >= 0) {
                    in_str = ServerStatus.parse_server_messages(in_str);
                }
                if (in_str.indexOf(String.valueOf(r1) + "server_time_date" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "server_time_date" + r2, new Date().toString());
                }
                if (in_str.indexOf(String.valueOf(r1) + "login_number" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "login_number" + r2, ServerStatus.uSG(user_info, "user_number"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_password" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_password" + r2, ServerStatus.uSG(user_info, "current_password"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_name" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_name" + r2, ServerStatus.uSG(user, "username"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_anonymous_password" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_anonymous_password" + r2, ServerStatus.uSG(user_info, "user_name").equalsIgnoreCase("anonymous") ? ServerStatus.uSG(user_info, "current_password") : "");
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_current_dir" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_current_dir" + r2, the_session.get_PWD());
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_site_commands_text" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_site_commands_text" + r2, ServerStatus.uSG(user, "site"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_the_command_data" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_the_command_data" + r2, ServerStatus.uSG(user_info, "the_command_data"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "the_command_data" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "the_command_data" + r2, ServerStatus.uSG(user_info, "the_command_data"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_the_command" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_the_command" + r2, ServerStatus.uSG(user_info, "the_command"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "the_command" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "the_command" + r2, ServerStatus.uSG(user_info, "the_command"));
                }
                try {
                    if (in_str.indexOf(String.valueOf(r1) + "user_time_remaining" + r2) >= 0) {
                        String time_str = String.valueOf(ServerStatus.uLG(user_info, "seconds_remaining")) + " secs";
                        if (ServerStatus.uLG(user_info, "seconds_remaining") == 0L) {
                            time_str = "<None Active>";
                        }
                        user_info.put("last_time_remaining", time_str);
                        if (ServerStatus.uLG(user_info, "seconds_remaining") > 60L) {
                            time_str = String.valueOf(ServerStatus.uLG(user_info, "seconds_remaining") / 60L) + "min, " + (ServerStatus.uLG(user_info, "seconds_remaining") - ServerStatus.uLG(user_info, "seconds_remaining") / 60L * 60L) + " secs";
                        }
                        in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_time_remaining" + r2, time_str);
                        user_info.put("last_time_remaining", time_str);
                    }
                }
                catch (Exception e) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_time_remaining" + r2, ServerStatus.uSG(user_info, "last_time_remaining"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_paused" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_paused" + r2, ServerStatus.uBG(user_info, "pause_now") ? "!PAUSED!" : "");
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_bytes_remaining" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_bytes_remaining" + r2, "" + (ServerStatus.uLG(user_info, "file_length") - (ServerStatus.uLG(user_info, "bytes_sent") - ServerStatus.uLG(user_info, "start_transfer_byte_amount"))));
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_pasv_port" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_pasv_port" + r2, "" + ServerStatus.uIG(user_info, "PASV_port"));
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_ratio" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_ratio" + r2, ServerStatus.uSG(user, "ratio") + " to 1");
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_perm_ratio" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_perm_ratio" + r2, ServerStatus.uBG(user, "perm_ratio") ? "Yes" : "No");
                }
                if (in_str.indexOf(String.valueOf(r1) + "user_reverse_ip" + r2) >= 0) {
                    in_str = Common.replace_str(in_str, String.valueOf(r1) + "user_reverse_ip" + r2, InetAddress.getByName(ServerStatus.uSG(user, "user_ip")).getHostName());
                }
                r1 = "{";
                r2 = "}";
                ++r;
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 2, e);
        }
        return in_str;
    }

    public String strip_variables(String in_str, SessionCrush the_session) {
        in_str = Common.replace_str(in_str, "<SPACE>", "INVALIDTEXTFOUND");
        in_str = Common.replace_str(in_str, "<URL>", "INVALIDTEXTFOUND");
        in_str = Common.replace_str(in_str, "<SPEAK>", "INVALIDTEXTFOUND");
        in_str = Common.replace_str(in_str, "<SOUND>", "INVALIDTEXTFOUND");
        in_str = Common.replace_str(in_str, "<LIST>", "INVALIDTEXTFOUND");
        in_str = Common.replace_str(in_str, "<INCLUDE>", "INVALIDTEXTFOUND");
        return in_str;
    }

    public String do_include_file_command(String in_str) {
        try {
            String file_name = in_str.substring(in_str.indexOf("<INCLUDE>") + 9, in_str.indexOf("</INCLUDE>"));
            RandomAccessFile includer = new RandomAccessFile(new File_S(file_name), "r");
            byte[] temp_array = new byte[(int)includer.length()];
            includer.read(temp_array);
            includer.close();
            String include_data = String.valueOf(new String(temp_array)) + this.CRLF;
            return Common.replace_str(in_str, "<INCLUDE>" + file_name + "</INCLUDE>", include_data);
        }
        catch (Exception exception) {
            return in_str;
        }
    }

    public String get_dir_list(String in_str, SessionCrush the_session) throws Exception {
        String command = in_str.substring(in_str.indexOf("<LIST>") + 6, in_str.indexOf("</LIST>"));
        String path = command.trim();
        Vector list = new Vector();
        if (!path.startsWith(the_session.user.getProperty("root_dir"))) {
            path = String.valueOf(the_session.user.getProperty("root_dir")) + path.substring(1);
        }
        the_session.uVFS.getListing(list, path);
        StringBuffer add_str = new StringBuffer();
        int x = 0;
        while (x < list.size()) {
            Properties item = (Properties)list.elementAt(x);
            LIST_handler.generateLineEntry(item, add_str, false, path, false, the_session, false);
            ++x;
        }
        in_str = Common.replace_str(in_str, "<LIST>" + command + "</LIST>", add_str.toString());
        return in_str;
    }

    public static String parse_server_messages(String in_str) {
        Enumeration<Object> the_list = LOC.localization.keys();
        while (the_list.hasMoreElements()) {
            String cur = the_list.nextElement().toString();
            if (!cur.startsWith("%") || in_str.indexOf(cur) < 0) continue;
            in_str = Common.replace_str(in_str, cur, ServerStatus.SG(cur));
        }
        return in_str;
    }

    public boolean check_hammer_ip(String ip) {
        block6: {
            try {
                if (Common.count_str(ServerStatus.siSG("hammer_history"), ip) < ServerStatus.IG("hammer_attempts") || !this.ban_ip(ip, ServerStatus.IG("ban_timeout"), "hammering")) break block6;
                try {
                    Properties info = new Properties();
                    info.put("alert_type", "hammering");
                    info.put("alert_sub_type", "ip");
                    info.put("alert_timeout", String.valueOf(ServerStatus.IG("ban_timeout")));
                    info.put("alert_max", String.valueOf(ServerStatus.IG("hammer_attempts")));
                    info.put("user_ip", ip);
                    info.put("alert_msg", "");
                    this.runAlerts("security_alert", info, null, null);
                }
                catch (Exception e) {
                    Log.log("BAN", 1, e);
                }
                ServerStatus.siPUT("hammer_history", Common.replace_str(ServerStatus.siSG("hammer_history"), ip, ""));
                try {
                    this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|---IP Banned---:" + ip + " for hammering connections.", "BAN");
                }
                catch (Exception e) {
                    // empty catch block
                }
                return false;
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
            }
        }
        return true;
    }

    public boolean check_hammer_ip_http(String ip) {
        block6: {
            try {
                if (Common.count_str(ServerStatus.siSG("hammer_history_http"), ip) < ServerStatus.IG("hammer_attempts_http") || !this.ban_ip(ip, ServerStatus.IG("ban_timeout_http"), "hammering http")) break block6;
                try {
                    Properties info = new Properties();
                    info.put("alert_type", "hammering");
                    info.put("alert_sub_type", "ip");
                    info.put("alert_timeout", String.valueOf(ServerStatus.IG("ban_timeout")));
                    info.put("alert_max", String.valueOf(ServerStatus.IG("hammer_attempts")));
                    info.put("user_ip", ip);
                    info.put("alert_msg", "");
                    this.runAlerts("security_alert", info, null, null);
                }
                catch (Exception e) {
                    Log.log("BAN", 1, e);
                }
                ServerStatus.siPUT("hammer_history_http", Common.replace_str(ServerStatus.siSG("hammer_history_http"), ip, ""));
                try {
                    this.append_log(String.valueOf(this.logDateFormat.format(new Date())) + "|---IP Banned---:" + ip + " for hammering HTTP connections.", "BAN");
                }
                catch (Exception e) {
                    // empty catch block
                }
                return false;
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
            }
        }
        return true;
    }

    public void runAlerts(String action, SessionCrush the_user) {
        AlertTools.runAlerts(action, null, null, null, the_user, null, com.crushftp.client.Common.dmz_mode);
    }

    public void runAlerts(String alert_action, Properties info, Properties user_info, SessionCrush the_user) {
        AlertTools.runAlerts(alert_action, info, user_info, null, the_user, null, com.crushftp.client.Common.dmz_mode);
    }

    public void runAlerts(String alert_action, Properties info, Properties user_info, SessionCrush the_user, Properties the_alert, boolean dmz_mode) {
        AlertTools.runAlerts(alert_action, info, user_info, null, the_user, the_alert, dmz_mode);
    }

    public void sendEmail(Properties p) {
        String results = com.crushftp.client.Common.send_mail(server_settings.getProperty("discovered_ip"), p.getProperty("to", ""), p.getProperty("cc", ""), p.getProperty("bcc", ""), p.getProperty("from", ""), p.getProperty("reply_to", ""), p.getProperty("subject", ""), p.getProperty("body", ""), p.getProperty("server", ""), p.getProperty("user", ""), p.getProperty("pass", ""), p.getProperty("ssl", "").equals("true"), p.getProperty("html", "").equals("true"), null);
        p.put("results", results);
        if (ServerStatus.siVG("server_queue") != null) {
            ServerStatus.siVG("server_queue").addElement(p);
        }
        if (results.toUpperCase().indexOf("SUCCESS") < 0) {
            Properties m = new Properties();
            m.put("result", results);
            m.put("body", p.getProperty("body", ""));
            m.put("subject", p.getProperty("subject", ""));
            m.put("to", p.getProperty("to", ""));
            m.put("from", p.getProperty("from", ""));
            m.put("reply_to", p.getProperty("reply_to", ""));
            m.put("cc", p.getProperty("cc", ""));
            m.put("bcc", p.getProperty("bcc", ""));
            thisObj.runAlerts("invalid_email", m, null, null);
        }
    }

    public static void checkServerGroups() {
        int x;
        Vector sgs = (Vector)server_settings.get("server_groups");
        boolean addItems = sgs.size() == 0;
        String lastServerGroupName = "MainUsers";
        File_S[] f = (File_S[])new File_S(System.getProperty("crushftp.users")).listFiles();
        if (f != null) {
            x = 0;
            while (x < f.length) {
                if (!f[x].getName().equals("extra_vfs") && f[x].isDirectory() && f[x].listFiles().length > 0) {
                    if (addItems) {
                        sgs.addElement(f[x].getName());
                    }
                    lastServerGroupName = f[x].getName();
                }
                ++x;
            }
        }
        if (sgs.size() == 0) {
            sgs.addElement("MainUsers");
        }
        x = sgs.size() - 1;
        while (x >= 0) {
            if (sgs.elementAt(x).toString().equals("extra_vfs")) {
                sgs.remove(x);
            }
            --x;
        }
        Vector pref_server_items = (Vector)server_settings.get("server_list");
        int x2 = 0;
        while (x2 < pref_server_items.size()) {
            Properties p = (Properties)pref_server_items.elementAt(x2);
            if (p.getProperty("linkedServer", "").equals("")) {
                p.put("linkedServer", lastServerGroupName);
            }
            ++x2;
        }
    }

    public static int IG(String data) {
        if (data != null && (data.equals("max_server_upload_speed") || data.equals("max_server_download_speed"))) {
            String[] intervals = ServerStatus.SG(data).split(";");
            SimpleDateFormat HHmm = new SimpleDateFormat("HHmm");
            int now = Integer.parseInt(HHmm.format(new Date()));
            String last_interval = null;
            int x = 0;
            while (x < intervals.length) {
                if (intervals[x].indexOf(":") < 0 && intervals[x].length() > 0) {
                    last_interval = "0:" + intervals[x];
                } else if (intervals[x].indexOf(":") >= 0) {
                    String interval = intervals[x].split(":")[0];
                    if (last_interval == null || Integer.parseInt(interval) <= now) {
                        last_interval = intervals[x];
                    }
                }
                ++x;
            }
            try {
                return Integer.parseInt(last_interval.split(":")[1]);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        try {
            return Integer.parseInt(ServerStatus.SG(data));
        }
        catch (Exception exception) {
            return 0;
        }
    }

    public static long LG(String data) {
        try {
            return Long.parseLong(ServerStatus.SG(data));
        }
        catch (Exception exception) {
            return 0L;
        }
    }

    public static boolean BG(String data) {
        if (thisObj == null) {
            return false;
        }
        return ServerStatus.SG(data).toLowerCase().equals("true");
    }

    public static Vector VG(String data) {
        if (thisObj == null) {
            return null;
        }
        return (Vector)server_settings.get(data);
    }

    public static String SG(String data) {
        if (thisObj == null) {
            return null;
        }
        if (server_settings.containsKey(data)) {
            return server_settings.getProperty(data);
        }
        if (LOC.localization.containsKey(data)) {
            return LOC.localization.getProperty(data);
        }
        if (!ServerStatus.thisObj.default_settings.containsKey(data)) {
            return data;
        }
        server_settings.put(data, ServerStatus.thisObj.default_settings.getProperty(data));
        return ServerStatus.thisObj.default_settings.getProperty(data);
    }

    public static int siIG(String data) {
        try {
            return Integer.parseInt(ServerStatus.siSG(data));
        }
        catch (Exception exception) {
            return 0;
        }
    }

    public static long siLG(String data) {
        try {
            return Long.parseLong(ServerStatus.siSG(data));
        }
        catch (Exception exception) {
            return 0L;
        }
    }

    public static boolean siBG(String data) {
        return ServerStatus.siSG(data).toLowerCase().equals("true");
    }

    public static void siPUT(String key, Object val) {
        ServerStatus.thisObj.server_info.put(key, val);
    }

    public static void siPUT2(String key, Object val) {
        ServerStatus.thisObj.server_info.put(key, val);
        server_settings.put(key, val);
    }

    public static String siSG(String data) {
        return ServerStatus.thisObj.server_info.getProperty(data, "");
    }

    public static Vector siVG(String data) {
        return (Vector)ServerStatus.thisObj.server_info.get(data);
    }

    public static Properties siPG(String data) {
        return (Properties)ServerStatus.thisObj.server_info.get(data);
    }

    public static Object siOG(String data) {
        return ServerStatus.thisObj.server_info.get(data);
    }

    private void fill_vfs_cache() {
        try {
            if (this.vfs_url_cache_inprogress) {
                return;
            }
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    ServerStatus.this.vfs_url_cache_inprogress = true;
                    try {
                        boolean refresh = ServerStatus.thisObj.server_info.containsKey("vfs_url_cache");
                        int count = 0;
                        Properties vfs_url_cache = new Properties();
                        Vector sgs = (Vector)server_settings.get("server_groups");
                        int x = 0;
                        while (x < sgs.size()) {
                            String serverGroup = sgs.elementAt(x).toString();
                            Vector user_list = new Vector();
                            UserTools.refreshUserList(serverGroup, user_list);
                            int xx = 0;
                            while (xx < user_list.size()) {
                                String username = com.crushftp.client.Common.dots(user_list.elementAt(xx).toString());
                                Properties virtual = UserTools.ut.getVirtualVFS(serverGroup, username);
                                Enumeration<?> e = virtual.propertyNames();
                                while (e.hasMoreElements()) {
                                    Vector vfs_users;
                                    Vector vItems;
                                    Properties p;
                                    String key = (String)e.nextElement();
                                    if (key.equals("vfs_permissions_object") || !(p = (Properties)virtual.get(key)).containsKey("vItems") || (vItems = (Vector)p.get("vItems")) == null || vItems.isEmpty()) continue;
                                    ++count;
                                    try {
                                        if (count > 100) {
                                            Thread.sleep(10L);
                                        }
                                    }
                                    catch (InterruptedException interruptedException) {
                                        // empty catch block
                                    }
                                    try {
                                        if (count > 1000) {
                                            Thread.sleep(50L);
                                        }
                                    }
                                    catch (InterruptedException interruptedException) {
                                        // empty catch block
                                    }
                                    try {
                                        if (refresh) {
                                            Thread.sleep(100L);
                                        }
                                    }
                                    catch (InterruptedException interruptedException) {
                                        // empty catch block
                                    }
                                    Properties pp = (Properties)vItems.get(0);
                                    String url = VRL.fileFix(pp.getProperty("url"));
                                    if (!vfs_url_cache.containsKey(String.valueOf(serverGroup) + ":" + url)) {
                                        vfs_users = new Vector();
                                        vfs_users.add(username);
                                        if (username.endsWith(".SHARED")) {
                                            String virtual_path = p.getProperty("virtualPath");
                                            String user_of_shared_path = virtual_path.substring("/Shares/".length(), virtual_path.indexOf("/", "/Shares/".length()));
                                            vfs_users.add(user_of_shared_path);
                                        }
                                        vfs_url_cache.put(String.valueOf(serverGroup) + ":" + url, vfs_users);
                                        continue;
                                    }
                                    vfs_users = (Vector)vfs_url_cache.get(String.valueOf(serverGroup) + ":" + url);
                                    vfs_users.add(username);
                                }
                                ++xx;
                            }
                            if (refresh) {
                                ((Properties)ServerStatus.thisObj.server_info.get("vfs_url_cache")).clear();
                                ((Properties)ServerStatus.thisObj.server_info.get("vfs_url_cache")).putAll((Map<?, ?>)vfs_url_cache);
                            } else {
                                ServerStatus.thisObj.server_info.put("vfs_url_cache", vfs_url_cache);
                            }
                            ++x;
                        }
                    }
                    finally {
                        ServerStatus.this.vfs_url_cache_inprogress = false;
                    }
                }
            }, "fill_vfs_cache");
        }
        catch (IOException e) {
            Log.log("SERVER", 1, e);
        }
    }

    public static long get_partial_val_or_all(String key, int index) {
        long l = 0L;
        String s = ServerStatus.SG(key).trim();
        l = s.indexOf(",") > 0 ? Long.parseLong(s.split(",")[index].trim()) : Long.parseLong(s.trim());
        return l;
    }

    public void monitor_thread_dump_port() {
        try {
            if (!ServerStatus.SG("thread_dump_port").equals("")) {
                if (thread_dump_socket != null && thread_dump_socket.getLocalPort() == Integer.parseInt(ServerStatus.SG("thread_dump_port"))) {
                    return;
                }
                if (thread_dump_socket != null) {
                    thread_dump_socket.close();
                }
                thread_dump_socket = new ServerSocket(Integer.parseInt(ServerStatus.SG("thread_dump_port")), 10, InetAddress.getByName("127.0.0.1"));
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        ServerSocket ss = thread_dump_socket;
                        while (!ss.isClosed()) {
                            try {
                                Socket sock = ss.accept();
                                Properties request = new Properties();
                                request.put("inputstream", sock.getInputStream());
                                request.put("outputstream", sock.getOutputStream());
                                AdminControls.upload_debug_info(request, "(CONNECT)");
                                sock.close();
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                                Log.log("SERVER", 1, e);
                            }
                        }
                    }
                });
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.log("SERVER", 1, e);
        }
    }
}

