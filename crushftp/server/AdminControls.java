/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.didisoft.pgp.KeyStore
 *  com.didisoft.pgp.PGPLib
 */
package crushftp.server;

import com.crushftp.client.AgentUI;
import com.crushftp.client.Base64;
import com.crushftp.client.File_B;
import com.crushftp.client.File_S;
import com.crushftp.client.File_U;
import com.crushftp.client.GenericClient;
import com.crushftp.client.GenericClientMulti;
import com.crushftp.client.HeapDumper;
import com.crushftp.client.URLConnection;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import com.didisoft.pgp.PGPLib;
import crushftp.gui.LOC;
import crushftp.handlers.Common;
import crushftp.handlers.JobScheduler;
import crushftp.handlers.Log;
import crushftp.handlers.SSLKeyManager;
import crushftp.handlers.SessionCrush;
import crushftp.handlers.SharedSessionReplicated;
import crushftp.handlers.SyncTools;
import crushftp.handlers.UserTools;
import crushftp.handlers.log.LoggingProviderDisk;
import crushftp.reports8.ReportTools;
import crushftp.server.QuickConnect;
import crushftp.server.ServerSessionAJAX;
import crushftp.server.ServerStatus;
import crushftp.server.VFS;
import crushftp.server.daemon.DMZServerCommon;
import crushftp.server.daemon.GenericServer;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.crypto.Cipher;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class AdminControls {
    public static Properties jobs_summary_cache = new Properties();
    public static long last_job_cache_clean = System.currentTimeMillis();
    static Properties expandUsernames_cache = new Properties();
    public static Vector runningSchedules = new Vector();
    static Properties tmp_telnet_sockets = new Properties();

    public static Object runInstanceAction(Properties request, String site) throws Exception {
        return AdminControls.runInstanceAction(request, site, "127.0.0.1");
    }

    public static Object runInstanceAction(Properties request, String site, String user_ip) throws Exception {
        request.remove("instance");
        if (request.getProperty("command").equals("setServerItem")) {
            return AdminControls.setServerItem(request, site);
        }
        if (request.getProperty("command").equals("getUser")) {
            return AdminControls.getUser(request, site, null);
        }
        if (request.getProperty("command").equals("getPublicKeys")) {
            return AdminControls.getPublicKeys(request);
        }
        if (request.getProperty("command").equals("setUserItem")) {
            return AdminControls.setUserItem(request, null, site);
        }
        if (request.getProperty("command").equals("getUserList")) {
            return AdminControls.getUserList(request, site, null);
        }
        if (request.getProperty("command").equals("getUserXML")) {
            return AdminControls.getUserXML(request, site, null);
        }
        if (request.getProperty("command").equals("getUserXMLListing")) {
            return AdminControls.getUserXMLListing(request, site, null);
        }
        if (request.getProperty("command").equals("getAdminXMLListing")) {
            return AdminControls.getAdminXMLListing(request, null, site);
        }
        if (request.getProperty("command").equals("getLog")) {
            return AdminControls.getLog(request, site);
        }
        if (request.getProperty("command").equals("adminAction")) {
            return AdminControls.adminAction(request, site, user_ip);
        }
        if (request.getProperty("command").equals("updateNow")) {
            return AdminControls.updateNow(request, site);
        }
        if (request.getProperty("command").equals("getRestartShutdownIdleStatus")) {
            return AdminControls.getRestartShutdownIdleStatus(request, site);
        }
        if (request.getProperty("command").equals("restartIdle")) {
            return AdminControls.restartIdle(request, site);
        }
        if (request.getProperty("command").equals("shutdownIdle")) {
            return AdminControls.shutdownIdle(request, site);
        }
        if (request.getProperty("command").equals("stopLogins")) {
            return AdminControls.stopLogins(request, site);
        }
        if (request.getProperty("command").equals("startLogins")) {
            return AdminControls.startLogins(request, site);
        }
        if (request.getProperty("command").equals("updateWebNow")) {
            return AdminControls.updateWebNow(request, site);
        }
        if (request.getProperty("command").equals("updateNowProgress")) {
            return AdminControls.updateNowProgress(request, site);
        }
        if (request.getProperty("command").equals("cancelUpdateProgress")) {
            return AdminControls.cancelUpdateProgress(request, site);
        }
        if (request.getProperty("command").equals("dumpStack")) {
            return AdminControls.dumpStack(request, site);
        }
        if (request.getProperty("command").equals("dumpHeap")) {
            return AdminControls.dumpHeap(request, site);
        }
        if (request.getProperty("command").equals("pgpGenerateKeyPair")) {
            return AdminControls.pgpGenerateKeyPair(request, site);
        }
        if (request.getProperty("command").equals("runReport")) {
            return AdminControls.runReport(request, site);
        }
        if (request.getProperty("command").equals("testReportSchedule")) {
            return AdminControls.testReportSchedule(request, site);
        }
        if (request.getProperty("command").equals("testPGP")) {
            return AdminControls.testPGP(request, site);
        }
        if (request.getProperty("command").equals("testJobSchedule")) {
            return AdminControls.testJobSchedule(request, site);
        }
        if (request.getProperty("command").equals("testSMTP")) {
            return AdminControls.testSMTP(request, site);
        }
        if (request.getProperty("command").equals("testOTP")) {
            return AdminControls.testOTP(request, site);
        }
        if (request.getProperty("command").equals("sendEventEmail")) {
            return AdminControls.sendEventEmail(request, site);
        }
        if (request.getProperty("command").equals("migrateUsersVFS")) {
            return AdminControls.migrateUsersVFS(request, site);
        }
        if (request.getProperty("command").equals("getJob")) {
            return AdminControls.getJob(request, site);
        }
        if (request.getProperty("command").equals("addJob")) {
            return AdminControls.addJob(request, site);
        }
        if (request.getProperty("command").equals("renameJob")) {
            return AdminControls.renameJob(request, site);
        }
        if (request.getProperty("command").equals("getJobsSummary")) {
            return AdminControls.getJobsSummary(request, site);
        }
        if (request.getProperty("command").equals("getJobsSettings")) {
            return AdminControls.getJobsSettings(request, site);
        }
        if (request.getProperty("command").equals("getJobInfo")) {
            return AdminControls.getJobInfo(request, site);
        }
        if (request.getProperty("command").equals("removeJob")) {
            return AdminControls.removeJob(request, site);
        }
        if (request.getProperty("command").equals("getServerRoots")) {
            return AdminControls.getServerRoots(request, site, null);
        }
        if (request.getProperty("command").equals("convertUsers")) {
            return AdminControls.convertUsers(request, site);
        }
        if (request.getProperty("command").equals("generateSSL")) {
            return AdminControls.generateSSL(request, site);
        }
        if (request.getProperty("command").equals("generateCSR")) {
            return AdminControls.generateCSR(request, site);
        }
        if (request.getProperty("command").equals("importReply")) {
            return AdminControls.importReply(request, site);
        }
        if (request.getProperty("command").equals("listSSL")) {
            return AdminControls.listSSL(request, site);
        }
        if (request.getProperty("command").equals("deleteSSL")) {
            return AdminControls.deleteSSL(request, site);
        }
        if (request.getProperty("command").equals("renameSSL")) {
            return AdminControls.renameSSL(request, site);
        }
        if (request.getProperty("command").equals("exportSSL")) {
            return AdminControls.exportSSL(request, site);
        }
        if (request.getProperty("command").equals("addPrivateSSL")) {
            return AdminControls.addPrivateSSL(request, site);
        }
        if (request.getProperty("command").equals("addPublicSSL")) {
            return AdminControls.addPublicSSL(request, site);
        }
        if (request.getProperty("command").equals("restorePrefs")) {
            return AdminControls.restorePrefs(request, site);
        }
        if (request.getProperty("command").equals("telnetSocket")) {
            return AdminControls.telnetSocket(request, site);
        }
        if (request.getProperty("command").equals("testKeystore")) {
            return AdminControls.testKeystore(request, site);
        }
        if (request.getProperty("command").equals("testDB")) {
            return AdminControls.testDB(request, site);
        }
        if (request.getProperty("command").equals("testQuery")) {
            return AdminControls.testQuery(request, site);
        }
        if (request.getProperty("command").equals("pluginMethodCall")) {
            return AdminControls.pluginMethodCall(request, site);
        }
        if (request.getProperty("command").equals("convertXMLSQLUsers")) {
            return AdminControls.convertXMLSQLUsers(request, site);
        }
        if (request.getProperty("command").equals("registerCrushFTP")) {
            return AdminControls.registerCrushFTP(request, site);
        }
        if (request.getProperty("command").equals("importUsers")) {
            return AdminControls.importUsers(request, site);
        }
        if (request.getProperty("command").equals("sendPassEmail")) {
            return AdminControls.sendPassEmail(request, null, site);
        }
        if (request.getProperty("command").equals("getTempAccounts")) {
            return AdminControls.getTempAccounts(request, site);
        }
        if (request.getProperty("command").equals("addTempAccount")) {
            return AdminControls.addTempAccount(request, site);
        }
        if (request.getProperty("command").equals("removeTempAccount")) {
            return AdminControls.removeTempAccount(request, site);
        }
        if (request.getProperty("command").equals("getTempAccountFiles")) {
            return AdminControls.getTempAccountFiles(request, site);
        }
        if (request.getProperty("command").equals("removeTempAccountFile")) {
            return AdminControls.removeTempAccountFile(request, site);
        }
        if (request.getProperty("command").equals("addTempAccountFile")) {
            return AdminControls.addTempAccountFile(request, site);
        }
        if (request.getProperty("command").equals("deleteReplication")) {
            return AdminControls.deleteReplication(request, site);
        }
        if (request.getProperty("command").equals("setReportSchedules")) {
            return AdminControls.setReportSchedules(request, site);
        }
        if (request.getProperty("command").equals("deleteReportSchedules")) {
            return AdminControls.deleteReportSchedules(request, site);
        }
        if (request.getProperty("command").equals("setMaxServerMemory")) {
            return AdminControls.setMaxServerMemory(request, site);
        }
        if (request.getProperty("command").equals("setEncryptionPassword")) {
            return AdminControls.setEncryptionPassword(request, site);
        }
        if (request.getProperty("command").equals("restartProcess")) {
            return AdminControls.restartProcess(request, site);
        }
        if (request.getProperty("command").equals("getDashboardItems")) {
            return AdminControls.getDashboardItems(request, site);
        }
        if (request.getProperty("command").equals("getDashboardHistory")) {
            return AdminControls.getDashboardHistory(request, site);
        }
        if (request.getProperty("command").equals("getDataFlowItems")) {
            return AdminControls.getDataFlowItems(request, site);
        }
        if (request.getProperty("command").equals("getServerSettingItems")) {
            return AdminControls.getServerSettingItems(request, site);
        }
        if (request.getProperty("command").equals("getServerInfoItems")) {
            return AdminControls.getServerInfoItems(request, site);
        }
        if (request.getProperty("command").equals("saveHttpChallengeToken")) {
            return AdminControls.saveHttpChallengeToken(request);
        }
        if (request.getProperty("command").equals("putTLSALPNChallengeJKS")) {
            return AdminControls.putTLSALPNChallengeJKS(request);
        }
        if (request.getProperty("command").equals("removeTLSALPNChallengeJKS")) {
            return AdminControls.removeTLSALPNChallengeJKS(request);
        }
        if (request.getProperty("command").equals("restartAllHttpsPorts")) {
            return AdminControls.restartAllHttpsPorts(request);
        }
        return "";
    }

    public static Object getServerItem(String admin_group_name, Properties request, String site, Properties user) {
        try {
            String[] keys = request.getProperty("key").split("/");
            String last_key = "";
            Object o = null;
            try {
                int x = 0;
                while (x < keys.length) {
                    Properties p;
                    String id;
                    String key;
                    last_key = key = keys[x];
                    if (key.equals("server_settings")) {
                        if (request.getProperty("instance", "").equals("")) {
                            o = ServerStatus.server_settings;
                        } else {
                            id = Common.makeBoundary();
                            DMZServerCommon.sendCommand(request.getProperty("instance", ""), new Properties(), "GET:SERVER_SETTINGS", id);
                            p = DMZServerCommon.getResponse(id, 20);
                            o = p.get("data");
                        }
                        if (request.getProperty("defaults", "false").equals("true")) {
                            o = ServerStatus.thisObj.default_settings;
                        }
                        if (site.indexOf("(CONNECT)") < 0 && site.indexOf("(PREF_VIEW)") < 0 && site.indexOf("(PREF_EDIT)") < 0 && site.indexOf("(SERVER_VIEW)") < 0 && site.indexOf("(SERVER_EDIT)") < 0 && site.indexOf("(UPDATE_RUN)") < 0) {
                            o = ServerStatus.thisObj.default_settings.clone();
                            ((Properties)o).put("CustomForms", AdminControls.stripUnrelatedAdminItems("CustomForms", admin_group_name));
                            ((Properties)o).put("tunnels", AdminControls.stripUnrelatedAdminItems("tunnels", admin_group_name));
                            ((Properties)o).put("email_templates", AdminControls.stripUnrelatedAdminItems("email_templates", admin_group_name));
                            Vector plugins_holder = new Vector();
                            Vector<Properties> plugins1 = new Vector<Properties>();
                            Vector<Properties> plugins2 = new Vector<Properties>();
                            plugins_holder.addElement(plugins1);
                            plugins_holder.addElement(plugins2);
                            Properties plug = new Properties();
                            plug.put("pluginName", "AutoUnzip");
                            plug.put("subItem", "");
                            plug.put("debug", "false");
                            plug.put("enabled", "false");
                            plugins1.addElement(plug);
                            plug = new Properties();
                            plug.put("pluginName", "CrushTask");
                            plug.put("subItem", "");
                            plug.put("debug", "false");
                            plug.put("enabled", "false");
                            plugins2.addElement(plug);
                            ((Properties)o).put("plugins", plugins_holder);
                            Properties password_rules = SessionCrush.build_password_rules(user);
                            ((Properties)o).putAll((Map<?, ?>)password_rules);
                            ((Properties)o).put("blank_passwords", ServerStatus.SG("blank_passwords"));
                            ((Properties)o).put("user_default_folder_privs", ServerStatus.SG("user_default_folder_privs"));
                            if (site.indexOf("(REPORT_VIEW)") >= 0 && site.indexOf("(REPORT_EDIT)") >= 0) {
                                ((Properties)o).put("reportSchedules", ServerStatus.VG("reportSchedules"));
                            }
                        }
                    } else if (key.equals("server_info")) {
                        if (request.getProperty("instance", "").equals("")) {
                            o = ServerStatus.thisObj.server_info.clone();
                        } else {
                            o = null;
                            id = Common.makeBoundary();
                            DMZServerCommon.sendCommand(request.getProperty("instance", ""), request, "GET:SERVER_INFO", id);
                            p = DMZServerCommon.getResponse(id, 20);
                            o = p.get("data");
                        }
                        if (site.indexOf("(CONNECT)") < 0 && site.indexOf("(SERVER_VIEW)") < 0 && site.indexOf("(SERVER_EDIT)") < 0) {
                            Properties o2 = new Properties();
                            if (site.indexOf("(UPDATE_RUN)") >= 0) {
                                o2.put("version_info_str", ((Properties)o).getProperty("version_info_str"));
                                o2.put("sub_version_info_str", ((Properties)o).getProperty("sub_version_info_str"));
                                o2.put("about_info_str", ((Properties)o).getProperty("about_info_str"));
                                o2.put("about_info_str", ((Properties)o).getProperty("about_info_str"));
                            }
                            o2.put("current_datetime_ddmmyyhhmmss", ((Properties)o).getProperty("current_datetime_ddmmyyhhmmss"));
                            o = new Properties();
                            ((Properties)o).putAll((Map<?, ?>)o2);
                        }
                    } else if (o instanceof Properties) {
                        o = ((Properties)o).get(key);
                    } else if (o instanceof Vector) {
                        o = ((Vector)o).elementAt(Integer.parseInt(key));
                    }
                    ++x;
                }
            }
            catch (Exception e) {
                return "FAILURE:" + e.toString();
            }
            if (last_key.equals("user_list") || last_key.equals("recent_user_list")) {
                o = AdminControls.stripUserList(o);
            }
            if (last_key.equals("server_info")) {
                ((Properties)o).remove("plugins");
                ((Properties)o).remove("running_tasks");
                ((Properties)o).remove("user_list");
                ((Properties)o).remove("recent_user_list");
            }
            if (o instanceof Properties) {
                o = AdminControls.stripUser(o);
            }
            return o;
        }
        catch (Exception e) {
            return e.toString();
        }
    }

    public static Object getDashboardItems(Properties request, String site) {
        Properties o2 = new Properties();
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 5);
                o2 = (Properties)p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return e.toString();
            }
            return o2;
        }
        try {
            Properties o = null;
            try {
                o = (Properties)ServerStatus.thisObj.server_info.clone();
                String[] keys = "jce_installed,low_memory,machine_is_linux,machine_is_solaris,machine_is_unix,machine_is_windows,machine_is_x,machine_is_x_10_5_plus,sub_version_info_str,version_info_str,registration_name,rid,enterprise_level,max_max_users,update_available,update_available_version,update_available_html,about_info_str,server_start_time,registration_email,server_list,recent_drives,recent_hammering,last_logins,ram_max,ram_free,thread_pool_available,thread_pool_busy,downloaded_files,uploaded_files,total_server_bytes_sent,total_server_bytes_received,successful_logins,failed_logins,current_download_speed,current_upload_speed,concurrent_users,replicated_servers,replicated_servers_count,replicated_servers_pending_user_sync,replicated_servers_pendingResponses,replicated_servers_lastActive,replicated_servers_sent_1,replicated_servers_sent_2,replicated_servers_sent_3,replicated_servers_sent_4,replicated_servers_sent_5,replicated_servers_queue_1,replicated_servers_queue_2,replicated_servers_queue_3,replicated_servers_queue_4,replicated_servers_queue_5,java_info,memcache_objects,keywords_cache_size,exif_item_count,replicated_received_message_count,replicated_write_prefs_count,replicated_user_changes_count,replicated_job_changes_count,server_cpu,os_cpu,open_files,max_open_files,connected_unique_ips,replication_vfs_count,ram_pending_bytes,ram_pending_bytes_s3_upload,ram_pending_bytes_s3_download,running_event_threads".split(",");
                int x = 0;
                while (x < keys.length) {
                    Object tmp = o.get(keys[x]);
                    if (tmp == null) {
                        tmp = "";
                    }
                    o2.put(keys[x], tmp);
                    ++x;
                }
                o2.put("replication_status", GenericClientMulti.replication_status);
                o2.put("max_threads", ServerStatus.SG("max_threads"));
                o2.put("hostname", ServerStatus.hostname);
                if (ServerStatus.BG("encryption_pass_needed") && new String(com.crushftp.client.Common.encryption_password).equals("crushftp")) {
                    o2.put("encryption_pass_needed", String.valueOf(ServerStatus.BG("encryption_pass_needed")));
                }
            }
            catch (Exception e) {
                return "FAILURE:" + e.toString();
            }
            return o2;
        }
        catch (Exception e) {
            return e.toString();
        }
    }

    /*
     * Loose catch block
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static Object getDashboardHistory(Properties request, String site) {
        Properties o2 = new Properties();
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 5);
                return (Properties)p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return e.toString();
            }
        }
        try {
            String archived_history = String.valueOf(new File_S(ServerStatus.change_vars_to_values_static(ServerStatus.SG("user_log_location"), null, null, null)).getCanonicalFile().getParentFile().getPath()) + "/archived_history/";
            if (request.getProperty("history_date", "").equals("")) {
                File[] dir = new File_S(archived_history).listFiles();
                int oldest = Integer.MAX_VALUE;
                File oldest_folder = null;
                int x = 0;
                while (x < dir.length) {
                    try {
                        if (dir[x].isDirectory() && Integer.parseInt(dir[x].getName()) < oldest) {
                            oldest = Integer.parseInt(dir[x].getName());
                            oldest_folder = dir[x];
                        }
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 2, e);
                    }
                    ++x;
                }
                dir = new File_S(String.valueOf(archived_history) + oldest_folder.getName() + "/").listFiles();
                oldest = Integer.MAX_VALUE;
                File oldest_time = null;
                int x2 = 0;
                while (x2 < dir.length) {
                    try {
                        if (dir[x2].isFile() && Integer.parseInt(dir[x2].getName().substring(0, dir[x2].getName().indexOf("."))) < oldest) {
                            oldest = Integer.parseInt(dir[x2].getName().substring(0, dir[x2].getName().indexOf(".")));
                            oldest_time = dir[x2];
                        }
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 2, e);
                    }
                    ++x2;
                }
                Properties history_obj = new Properties();
                history_obj.put("history_date", oldest_folder.getName());
                history_obj.put("history_time", oldest_time.getName().substring(0, oldest_time.getName().indexOf(".")));
                return history_obj;
            }
            archived_history = String.valueOf(archived_history) + Common.dots(request.getProperty("history_date")) + "/";
            if (!new File_S(archived_history = String.valueOf(archived_history) + Common.dots(request.getProperty("history_time")) + ".history_obj").exists()) return "FAILURE:Specified date and time unavailable.";
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File_S(archived_history)));
            o2 = (Properties)ois.readObject();
            ois.close();
            return o2;
        }
        catch (Exception e) {
            return "FAILURE:" + e.toString();
            {
                catch (Exception e2) {
                    return e2.toString();
                }
            }
        }
    }

    public static Object getDataFlowItems(Properties request, String site) {
        Properties o2 = new Properties();
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 5);
                o2 = (Properties)p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return e.toString();
            }
            return o2;
        }
        try {
            Properties o = new Properties();
            o.put("server_lat", String.valueOf(ServerStatus.thisObj.geoip.server_lat));
            o.put("server_lon", String.valueOf(ServerStatus.thisObj.geoip.server_lon));
            try {
                Vector incoming = (Vector)ServerStatus.siVG("incoming_transfers").clone();
                int x = 0;
                while (x < incoming.size()) {
                    Properties p = (Properties)incoming.elementAt(x);
                    Properties loc = ServerStatus.thisObj.geoip.getLoc(p.getProperty("user_ip"));
                    loc.putAll((Map<?, ?>)p);
                    loc.put("direction", "incoming");
                    loc.put("transfer_type", "user");
                    if (!o.containsKey(loc.getProperty("user_protocol").toUpperCase())) {
                        o.put(loc.getProperty("user_protocol").toUpperCase(), new Vector());
                    }
                    Vector v = (Vector)o.get(loc.getProperty("user_protocol").toUpperCase());
                    v.addElement(loc);
                    ++x;
                }
                Vector outgoing = (Vector)ServerStatus.siVG("outgoing_transfers").clone();
                int x2 = 0;
                while (x2 < outgoing.size()) {
                    Properties p = (Properties)outgoing.elementAt(x2);
                    Properties loc = ServerStatus.thisObj.geoip.getLoc(p.getProperty("user_ip"));
                    loc.putAll((Map<?, ?>)p);
                    loc.put("direction", "outgoing");
                    loc.put("transfer_type", "user");
                    if (!o.containsKey(loc.getProperty("user_protocol").toUpperCase())) {
                        o.put(loc.getProperty("user_protocol").toUpperCase(), new Vector());
                    }
                    Vector v = (Vector)o.get(loc.getProperty("user_protocol").toUpperCase());
                    v.addElement(loc);
                    ++x2;
                }
            }
            catch (Exception e) {
                return "FAILURE:" + e.toString();
            }
            return o;
        }
        catch (Exception e) {
            return e.toString();
        }
    }

    public static Object getServerInfoItems(Properties request, String site) {
        Properties o2 = new Properties();
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 5);
                o2 = (Properties)p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return e.toString();
            }
            return o2;
        }
        try {
            String allowed_list = "";
            if (site.indexOf("(USER_ADMIN)") >= 0) {
                allowed_list = "machine_is_linux,machine_is_solaris,machine_is_unix,machine_is_windows,machine_is_x,machine_is_x_10_5_plus,sub_version_info_str,version_info_str";
            }
            Properties o = null;
            try {
                if (request.getProperty("instance", "").equals("")) {
                    o = (Properties)ServerStatus.thisObj.server_info.clone();
                }
                String[] keys = request.getProperty("keys").split(",");
                int x = 0;
                while (x < keys.length) {
                    Object tmp = o.get(keys[x]);
                    if (tmp == null) {
                        tmp = "";
                    }
                    if (allowed_list.indexOf(keys[x]) >= 0 || allowed_list.equals("")) {
                        o2.put(keys[x], tmp);
                    }
                    ++x;
                }
            }
            catch (Exception e) {
                return "FAILURE:" + e.toString();
            }
            return o2;
        }
        catch (Exception e) {
            return e.toString();
        }
    }

    public static Object getServerSettingItems(Properties request, String site) {
        String admin_group_name_raw = request.getProperty("admin_group_name");
        Properties o2 = new Properties();
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 5);
                o2 = (Properties)p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return e.toString();
            }
            return o2;
        }
        try {
            Properties o = null;
            String admin_group_name = request.getProperty("admin_group_name", "");
            try {
                if (request.getProperty("instance", "").equals("")) {
                    o = (Properties)ServerStatus.server_settings.clone();
                } else {
                    o = null;
                    String id = Common.makeBoundary();
                    DMZServerCommon.sendCommand(request.getProperty("instance", ""), request, "GET:SERVER_SETTINGS", id);
                    Properties p = DMZServerCommon.getResponse(id, 20);
                    o = (Properties)p.get("data");
                }
                if (site.indexOf("(CONNECT)") < 0 && site.indexOf("(PREF_VIEW)") < 0 && site.indexOf("(PREF_EDIT)") < 0 && site.indexOf("(SERVER_VIEW)") < 0 && site.indexOf("(SERVER_EDIT)") < 0 && site.indexOf("(UPDATE_RUN)") < 0) {
                    o = (Properties)ServerStatus.thisObj.default_settings.clone();
                    o.put("CustomForms", AdminControls.stripUnrelatedAdminItems("CustomForms", admin_group_name));
                    o.put("tunnels", AdminControls.stripUnrelatedAdminItems("tunnels", admin_group_name));
                    o.put("email_templates", AdminControls.stripUnrelatedAdminItems("email_templates", admin_group_name));
                    Properties password_rules = SessionCrush.build_password_rules(null);
                    o.putAll((Map<?, ?>)password_rules);
                    o.put("blank_passwords", ServerStatus.SG("blank_passwords"));
                    o.put("user_default_folder_privs", ServerStatus.SG("user_default_folder_privs"));
                    Vector tasks = new Vector();
                    Vector<Properties> crush_task_subitems = new Vector<Properties>();
                    Properties fake_task = new Properties();
                    fake_task.put("pluginName", "");
                    fake_task.put("subItem", "");
                    Vector<Properties> fake_subitems = new Vector<Properties>();
                    fake_subitems.add(fake_task);
                    tasks.add(fake_subitems);
                    Vector plugins = ServerStatus.VG("plugins");
                    int x = 0;
                    while (x < plugins.size()) {
                        Vector subitems = (Vector)plugins.get(x);
                        if (subitems.size() != 0 && ((Properties)subitems.elementAt(0)).getProperty("pluginName", "").equals("CrushTask")) {
                            tasks.add(crush_task_subitems);
                            int xx = 0;
                            while (xx < subitems.size()) {
                                Properties p = (Properties)subitems.elementAt(xx);
                                if (p.getProperty("enabled", "false").equals("true")) {
                                    Properties task_names = new Properties();
                                    task_names.put("pluginName", p.getProperty("pluginName", ""));
                                    task_names.put("subItem", p.getProperty("subItem", ""));
                                    task_names.put("enabled", "true");
                                    crush_task_subitems.add(task_names);
                                }
                                ++xx;
                            }
                        }
                        ++x;
                    }
                    o.put("plugins", tasks);
                    if (site.indexOf("(REPORT_VIEW)") >= 0 && site.indexOf("(REPORT_EDIT)") >= 0) {
                        o.put("reportSchedules", ServerStatus.VG("reportSchedules"));
                    }
                    if (!admin_group_name_raw.equals("")) {
                        Vector<String> server_groups = new Vector<String>();
                        String[] fake_ucg = admin_group_name_raw.split(",");
                        int x2 = 0;
                        while (x2 < fake_ucg.length) {
                            server_groups.addElement(fake_ucg[x2].trim());
                            ++x2;
                        }
                        o.put("server_groups", server_groups);
                    }
                }
                String[] keys = request.getProperty("keys").split(",");
                int x = 0;
                while (x < keys.length) {
                    Object tmp = o.get(keys[x]);
                    if (tmp == null) {
                        tmp = "";
                    }
                    o2.put(keys[x], tmp);
                    ++x;
                }
            }
            catch (Exception e) {
                return "FAILURE:" + e.toString();
            }
            return o2;
        }
        catch (Exception e) {
            return e.toString();
        }
    }

    public static Object getJob(Properties request, String site) {
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 20);
                return p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return "FAILURE:Job not found.";
            }
        }
        Vector jobs = JobScheduler.getJobList(false);
        if (request.getProperty("name", "").equals("")) {
            File_S f;
            Properties subfolder_parents = new Properties();
            int found_limited_admin_folder = 0;
            Vector<Object> all = new Vector<Object>();
            int x = 0;
            while (x < jobs.size()) {
                f = (File_S)jobs.elementAt(x);
                if (!(f.getName().startsWith("_") || site.indexOf("(USER_ADMIN)") >= 0 && (new VRL(f.getAbsolutePath()) + "/").indexOf("/jobs/" + request.getProperty("admin_group_name", "") + "/") < 0)) {
                    Properties p;
                    ++found_limited_admin_folder;
                    if (request.getProperty("schedule_info", "").equals("true")) {
                        if (new File_S(String.valueOf(f.getPath()) + "/job.XML").exists()) {
                            subfolder_parents.put(f.getPath(), "");
                            subfolder_parents.put(f.getParentFile().getPath(), "");
                            p = (Properties)Common.readXMLObject(String.valueOf(f.getPath()) + "/job.XML");
                            if (p != null) {
                                Properties p2 = new Properties();
                                String job_name = AdminControls.jobName(f);
                                p2.put("name", job_name);
                                p2.put("type", "FILE");
                                p2.put("scheduleType", p.getProperty("scheduleType", "manual"));
                                p2.put("minutelyAmount", p.getProperty("minutelyAmount", "1"));
                                p2.put("weekDays", p.getProperty("weekDays", ""));
                                p2.put("weeklyAmount", p.getProperty("weeklyAmount", "1"));
                                p2.put("dailyAmount", p.getProperty("dailyAmount", "1"));
                                p2.put("monthlyAmount", p.getProperty("monthlyAmount", "1"));
                                p2.put("scheduleTime", p.getProperty("scheduleTime", ""));
                                p2.put("created", p.getProperty("created", ""));
                                p2.put("modified", p.getProperty("modified", ""));
                                p2.put("monthDays", p.getProperty("monthDays", "1"));
                                p2.put("enabled", p.getProperty("enabled", "false"));
                                p2.put("nextRun", p.getProperty("nextRun", ""));
                                if ((site.indexOf("(CONNECT)") <= 0 || site.indexOf("(JOB_EDIT)") <= 0) && site.indexOf("(JOB_MONITOR)") >= 0) {
                                    if (AdminControls.expandUsernames(p.getProperty("allowed_usernames", "")).indexOf(request.getProperty("calling_user", "~NONE~").toUpperCase()) >= 0) {
                                        all.addElement(p2);
                                    }
                                } else {
                                    all.addElement(p2);
                                }
                            }
                        } else {
                            subfolder_parents.put(f.getPath(), "");
                            subfolder_parents.put(f.getParentFile().getPath(), "");
                            Properties p2 = new Properties();
                            String job_name = AdminControls.jobName(f);
                            p2.put("name", job_name);
                            p2.put("type", "DIR");
                            all.addElement(p2);
                        }
                    } else if ((site.indexOf("(CONNECT)") <= 0 || site.indexOf("(JOB_EDIT)") <= 0) && site.indexOf("(JOB_MONITOR)") >= 0) {
                        if (new File_S(String.valueOf(f.getPath()) + "/job.XML").exists() && (p = (Properties)Common.readXMLObject(String.valueOf(f.getPath()) + "/job.XML")) != null && AdminControls.expandUsernames(p.getProperty("allowed_usernames", "")).indexOf(request.getProperty("calling_user", "~NONE~").toUpperCase()) >= 0) {
                            all.addElement(AdminControls.jobName(f));
                        }
                    } else {
                        all.addElement(AdminControls.jobName(f));
                    }
                }
                ++x;
            }
            if (site.indexOf("(USER_ADMIN)") >= 0) {
                x = 0;
                while (x < jobs.size()) {
                    f = (File_S)jobs.elementAt(x);
                    if (!(f.getName().startsWith("_") || site.indexOf("(USER_ADMIN)") >= 0 && (new VRL(f.getAbsolutePath()) + "/").indexOf("/jobs/" + request.getProperty("admin_group_name", "") + "/") < 0 || subfolder_parents.containsKey(f.getPath()))) {
                        ++found_limited_admin_folder;
                    }
                    ++x;
                }
            }
            if (found_limited_admin_folder == 0 && site.indexOf("(USER_ADMIN)") >= 0) {
                Properties p2 = new Properties();
                p2.put("name", request.getProperty("admin_group_name", ""));
                p2.put("type", "DIR");
                all.addElement(p2);
            }
            return all;
        }
        if (site.indexOf("(CONNECT)") >= 0 || site.indexOf("(JOB_EDIT)") >= 0 || site.indexOf("(JOB_VIEW)") >= 0) {
            int x = 0;
            while (x < jobs.size()) {
                String job_name = AdminControls.jobName((File_S)jobs.elementAt(x));
                if (request.getProperty("name", "").equalsIgnoreCase(job_name)) {
                    Properties p = (Properties)Common.readXMLObject(String.valueOf(((File_S)jobs.elementAt(x)).getPath()) + "/job.XML");
                    if (p.containsKey("tasks") && p.get("tasks") instanceof Vector) {
                        Vector v = (Vector)p.get("tasks");
                        int xx = 0;
                        while (xx < v.size()) {
                            Properties pp = (Properties)v.elementAt(xx);
                            Enumeration<Object> keys = pp.keys();
                            while (keys.hasMoreElements()) {
                                String key = keys.nextElement().toString();
                                if (pp.get(key).toString().contains("<LINE>")) {
                                    pp.put(key, Common.replace_str(pp.get(key).toString(), "<LINE>", "{line_start}"));
                                }
                                if (pp.get(key).toString().contains("&lt;LINE&gt;")) {
                                    pp.put(key, Common.replace_str(pp.get(key).toString(), "&lt;LINE&gt;", "{line_start}"));
                                }
                                if (pp.get(key).toString().contains("&amp;lt;LINE&gt;")) {
                                    pp.put(key, Common.replace_str(pp.get(key).toString(), "&amp;lt;LINE&gt;", "{line_start}"));
                                }
                                if (pp.get(key).toString().contains("</LINE>")) {
                                    pp.put(key, Common.replace_str(pp.get(key).toString(), "</LINE>", "{line_end}"));
                                }
                                if (pp.get(key).toString().contains("&lt;/LINE&gt;")) {
                                    pp.put(key, Common.replace_str(pp.get(key).toString(), "&lt;/LINE&gt;", "{line_end}"));
                                }
                                if (!pp.get(key).toString().contains("&amp;lt;/LINE&gt;")) continue;
                                pp.put(key, Common.replace_str(pp.get(key).toString(), "&amp;lt;/LINE&gt;", "{line_end}"));
                            }
                            ++xx;
                        }
                    }
                    p.put("scheduleName", job_name);
                    return p;
                }
                ++x;
            }
        }
        return "FAILURE:Job not found.";
    }

    public static String jobName(File_S f) {
        return f.getPath().replace('\\', '/').substring((String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/").length());
    }

    public static Object saveReport(Properties request, String site, boolean replicate) {
        new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "SavedReports/").mkdirs();
        if (ServerStatus.BG("replicate_reports") && replicate) {
            Properties pp = new Properties();
            pp.put("request", request);
            pp.put("site", "");
            SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.AdminControls.saveReport", "info", pp);
        }
        try {
            RandomAccessFile out = new RandomAccessFile(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "SavedReports/" + request.getProperty("report_token")), "rw");
            out.setLength(0L);
            out.write(request.getProperty("s").getBytes("UTF8"));
            out.close();
        }
        catch (Exception e) {
            Log.log("SERVER", 1, e);
        }
        return null;
    }

    public static Object renameJob(Properties request, String site) {
        return AdminControls.renameJob(request, site, true);
    }

    public static Object renameJob(Properties request, String site, boolean replicate) {
        if (ServerStatus.BG("replicate_jobs") && replicate) {
            Properties pp = new Properties();
            pp.put("request", request);
            pp.put("site", site);
            SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.AdminControls.renameJob", "info", pp);
        }
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 20);
                return p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return "FAILURE:Job not found.";
            }
        }
        if (JobScheduler.jobRunning(request.getProperty("priorName", ""))) {
            return "FAILURE:Rename is not allowed on running job. Frist stop the job  : " + request.getProperty("priorName", "");
        }
        Vector jobs = JobScheduler.getJobList(false);
        int x = 0;
        while (x < jobs.size()) {
            if (request.getProperty("priorName", "").equalsIgnoreCase(AdminControls.jobName((File_S)jobs.elementAt(x)))) {
                if (site.indexOf("(USER_ADMIN)") >= 0 && (new VRL(((File_S)jobs.elementAt(x)).getAbsolutePath()) + "/").indexOf("/jobs/" + request.getProperty("admin_group_name", "") + "/") < 0) {
                    return "FAILURE:Job could not be renamed because you don't have access to this job:" + request.getProperty("priorName", "");
                }
                String new_name = JobScheduler.safeName(request.getProperty("name"));
                if (site.indexOf("(USER_ADMIN)") >= 0 && !("./jobs/" + new_name).startsWith("./jobs/" + request.getProperty("admin_group_name", ""))) {
                    return "FAILURE:Job could not be renamed because you don't have access to this job:" + new_name;
                }
                File_S newJob = new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + new_name);
                if (((File_S)jobs.elementAt(x)).renameTo(newJob)) {
                    Properties job = (Properties)Common.readXMLObject(new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + JobScheduler.safeName(request.getProperty("name")) + "/job.XML"));
                    job.put("scheduleName", JobScheduler.safeName(request.getProperty("name")));
                    try {
                        Common.writeXMLObject(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + request.getProperty("name") + "/job.XML", (Object)job, "job");
                        File_S[] f = (File_S[])((File)newJob).listFiles();
                        int xx = 0;
                        while (f != null && xx < f.length) {
                            if (f[xx].getName().indexOf("_") > 0 && f[xx].getName().lastIndexOf(".XML") > 0 && f[xx].isFile()) {
                                try {
                                    Properties tracker = (Properties)Common.readXMLObject(f[xx].getPath());
                                    if (tracker.containsKey("settings")) {
                                        Properties settings = (Properties)tracker.get("settings");
                                        settings.put("scheduleName", JobScheduler.safeName(request.getProperty("name")));
                                        Common.writeXMLObject(f[xx].getPath(), (Object)tracker, "tracker");
                                    }
                                }
                                catch (Exception e) {
                                    Log.log("SERVER", 1, e);
                                }
                            }
                            ++xx;
                        }
                        return "SUCCESS:" + JobScheduler.safeName(request.getProperty("name"));
                    }
                    catch (Exception e) {
                        return "FAILURE:" + e.toString();
                    }
                }
                return "FAILURE:Job could not be renamed to:" + JobScheduler.safeName(request.getProperty("name"));
            }
            ++x;
        }
        return "FAILURE:Job not found.";
    }

    public static Object removeJob(Properties request, String site) {
        return AdminControls.removeJob(request, site, true);
    }

    public static Object removeJob(Properties request, String site, boolean replicate) {
        if (ServerStatus.BG("replicate_jobs") && replicate) {
            Properties pp = new Properties();
            pp.put("request", request);
            pp.put("site", site);
            SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.AdminControls.removeJob", "info", pp);
        }
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 20);
                return p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return "FAILURE:Job not found.";
            }
        }
        Vector jobs = JobScheduler.getJobList(false);
        int x = 0;
        while (x < jobs.size()) {
            if (request.getProperty("name", "").equalsIgnoreCase(AdminControls.jobName((File_S)jobs.elementAt(x)))) {
                if (site.indexOf("(USER_ADMIN)") >= 0 && (new VRL(((File_S)jobs.elementAt(x)).getAbsolutePath()) + "/").indexOf("/jobs/" + request.getProperty("admin_group_name", "") + "/") < 0) {
                    return "FAILURE:Job could not be renamed because you don't have access to this job:" + request.getProperty("priorName", "");
                }
                Common.recurseDelete(((File_S)jobs.elementAt(x)).getPath(), false);
                return "SUCCESS:" + JobScheduler.safeName(request.getProperty("name"));
            }
            ++x;
        }
        return "FAILURE:Job not found.";
    }

    public static Object addJob(Properties request, String site) {
        return AdminControls.addJob(request, site, true);
    }

    public static Object addJob(Properties request, String site, boolean replicate) {
        if (ServerStatus.BG("replicate_jobs") && replicate) {
            Properties pp = new Properties();
            pp.put("request", request);
            pp.put("site", site);
            SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.AdminControls.addJob", "info", pp);
        }
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 20);
                return p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return "FAILURE:Job create failed.";
            }
        }
        Properties job = null;
        try {
            job = (Properties)Common.readXMLObject(new ByteArrayInputStream(Common.url_decode(request.getProperty("data").replace('+', ' ')).getBytes("UTF8")));
            if (job == null) {
                throw new Exception("Invalid xml for job");
            }
            Common.urlDecodePost(request);
            if (site.indexOf("(USER_ADMIN)") >= 0) {
                UserTools.testLimitedTasks(job, request);
            }
            String jobName = JobScheduler.safeName(com.crushftp.client.Common.dots(request.getProperty("name")));
            if (site.indexOf("(USER_ADMIN)") >= 0 && !jobName.startsWith(request.getProperty("admin_group_name", ""))) {
                jobName = String.valueOf(request.getProperty("admin_group_name", "")) + "/" + jobName;
            }
            new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + jobName).mkdirs();
            new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + jobName + "/job2.XML").delete();
            Common.writeXMLObject(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + jobName + "/job2.XML", (Object)job, "job");
            boolean update = false;
            String update_log = "";
            String new_audit_trail = "";
            if (new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + jobName + "/job.XML").exists()) {
                update = true;
                if (ServerStatus.BG("v10_beta")) {
                    Properties old_job = (Properties)Common.readXMLObject(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + jobName + "/job.XML");
                    Properties new_job = new Properties();
                    new_job.putAll((Map<?, ?>)job);
                    try {
                        if (Math.abs(Float.parseFloat(old_job.getProperty("startPointPosition", "0,0").split(",")[0]) - Float.parseFloat(new_job.getProperty("startPointPosition", "0,0").split(",")[0])) <= 2.0f) {
                            old_job.put("startPointPosition", new_job.getProperty("startPointPosition", "0,0"));
                        }
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 2, e);
                    }
                    old_job.remove("audit_trail");
                    new_audit_trail = (String)new_job.remove("audit_trail");
                    StringBuffer update_log_summary = new StringBuffer();
                    Common.updateObjectLog(new_job, old_job, String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + jobName + "/job.XML", true, update_log_summary);
                    update_log = update_log_summary.toString();
                }
                new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/" + jobName + "_19.XML").delete();
                int x = 18;
                while (x >= 0) {
                    try {
                        new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/" + jobName + "_" + x + ".XML").renameTo(new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/" + jobName + "_" + (x + 1) + ".XML"));
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 1, e);
                    }
                    --x;
                }
                new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + jobName + "/job.XML").renameTo(new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/" + jobName + "_0.XML"));
                new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + jobName + "/job.XML").delete();
            }
            if (new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + jobName + "/job2.XML").length() <= 0L) {
                throw new Exception("Failed to save job...0 byte save, aborting.");
            }
            new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + jobName + "/job2.XML").renameTo(new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + jobName + "/job.XML"));
            if (update && !update_log.equals("")) {
                if (ServerStatus.BG("v10_beta")) {
                    try {
                        final String alert_schedule_name = jobName;
                        final String alert_schedule_changes = update_log;
                        final String alert_schedule_audit_trail = new_audit_trail;
                        Worker.startWorker(new Runnable(){

                            @Override
                            public void run() {
                                Properties info = new Properties();
                                info.put("alert_schedule_name", alert_schedule_name);
                                info.put("alert_schedule_changes", alert_schedule_changes);
                                info.put("alert_schedule_audit_trail", alert_schedule_audit_trail);
                                ServerStatus.thisObj.runAlerts("job_update", info, null, null);
                            }
                        }, "Run Job Update alert");
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 1, e);
                    }
                }
            }
            return "SUCCESS:" + jobName;
        }
        catch (Exception e) {
            return "FAILURE:" + e.getMessage();
        }
    }

    public static Object makedirJob(Properties request, String site, boolean replicate) {
        String s1 = Common.dots(Common.url_decode(request.getProperty("item_name").replace('+', ' ')));
        if (site.indexOf("(USER_ADMIN)") >= 0) {
            if ((new VRL(new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + s1).getAbsolutePath()) + "/").indexOf("/jobs/" + request.getProperty("admin_group_name", "") + "/") < 0) {
                return "FAILURE:Job could not be created because you don't have access to this job:" + request.getProperty("item_name", "");
            }
        }
        new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + s1).mkdirs();
        return "SUCCESS:" + s1 + " created.";
    }

    public static Object renamedirJob(Properties request, String site, boolean replicate) {
        String s1 = Common.dots(Common.url_decode(request.getProperty("item_name1").replace('+', ' ')));
        String s2 = Common.dots(Common.url_decode(request.getProperty("item_name2").replace('+', ' ').replace('/', '_')));
        Vector jobs = JobScheduler.getJobList(false);
        int x = 0;
        while (x < jobs.size()) {
            if (s1.equalsIgnoreCase(AdminControls.jobName((File_S)jobs.elementAt(x)))) {
                if (site.indexOf("(USER_ADMIN)") >= 0 && (new VRL(((File_S)jobs.elementAt(x)).getAbsolutePath()) + "/").indexOf("/jobs/" + request.getProperty("admin_group_name", "") + "/") < 0) {
                    return "FAILURE:Job could not be renamed because you don't have access to this job:" + request.getProperty("item_name1", "");
                }
                new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + s1).renameTo(new File_S(String.valueOf(new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + s1).getParent()) + "/" + s2));
                return "SUCCESS:" + s1 + " renamed to " + s2;
            }
            ++x;
        }
        return "FAILURE:" + s1 + " not found.";
    }

    public static Object deletedirJob(Properties request, String site, boolean replicate) {
        String s1 = Common.dots(Common.url_decode(request.getProperty("item_name1").replace('+', ' ')));
        int x = 0;
        Vector jobs = JobScheduler.getJobList(false);
        if (x < jobs.size()) {
            if (AdminControls.jobName((File_S)jobs.elementAt(x)).startsWith(String.valueOf(s1) + "/")) {
                return "FAILURE:Job folder could not be deleted because it is not empty.";
            }
            if (site.indexOf("(USER_ADMIN)") >= 0 && (new VRL(((File_S)jobs.elementAt(x)).getAbsolutePath()) + "/").indexOf("/jobs/" + request.getProperty("admin_group_name", "") + "/") < 0) {
                return "FAILURE:Job could not be deleted because you don't have access to this job:" + request.getProperty("priorName", "");
            }
            new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + s1).delete();
            return "SUCCESS:" + s1 + " deleted.";
        }
        return "SUCCESS:" + s1 + " not found.";
    }

    public static Object getServerRoots(Properties request, String site, SessionCrush thisSession) {
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 20);
                return p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return "FAILURE:getServerRoots failed.";
            }
        }
        Properties p = new Properties();
        p.put("server.root", System.getProperty("crushftp.server.root", ""));
        p.put("user.root", System.getProperty("crushftp.user.root", ""));
        if (site.indexOf("(USER_ADMIN)") >= 0 && System.getProperty("crushftp.user.root", "").equals("") && thisSession != null) {
            String parentUser = thisSession.getAdminGroupName(request);
            if (parentUser.startsWith(",")) {
                parentUser = parentUser.substring(1);
            }
            if (parentUser.indexOf(",") >= 0) {
                parentUser = parentUser.substring(0, parentUser.indexOf(","));
            }
            VFS tempVFS = UserTools.ut.getVFS(request.getProperty("serverGroup"), parentUser);
            try {
                Properties item;
                VRL vrl;
                Vector listing = UserTools.ut.get_virtual_list_fake(tempVFS, "/", request.getProperty("serverGroup"), parentUser);
                if (listing.size() > 0 && (vrl = new VRL(tempVFS.get_item("/" + (item = (Properties)listing.elementAt(0)).getProperty("name") + "/").getProperty("url"))).getProtocol().equalsIgnoreCase("FILE")) {
                    p.put("user.root", vrl.getPath());
                    p.put("server.root", vrl.getPath());
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        }
        return p;
    }

    public static Vector stripUnrelatedAdminItems(String key, String admin_group_name) {
        Vector v = (Vector)ServerStatus.VG(key).clone();
        int xx = v.size() - 1;
        while (xx >= 0) {
            Properties pp = (Properties)v.elementAt(xx);
            if (!pp.getProperty("name", "").toUpperCase().startsWith(admin_group_name.toUpperCase())) {
                v.removeElementAt(xx);
            }
            --xx;
        }
        if (v.size() == 0) {
            v = ServerStatus.VG(key);
        }
        return v;
    }

    public static String getStatHistory(Properties request) throws Exception {
        StringBuffer xml = new StringBuffer();
        String[] params = null;
        params = request.getProperty("params").indexOf("-") >= 0 ? request.getProperty("params").split("-") : request.getProperty("params").split(",");
        int x = 0;
        while (x < params.length) {
            String param = params[x].trim();
            Properties si = null;
            if (request.getProperty("instance", "").equals("")) {
                si = ServerStatus.thisObj.server_info;
            } else {
                String id = Common.makeBoundary();
                DMZServerCommon.sendCommand(request.getProperty("instance", ""), request, "GET:SERVER_INFO", id);
                Properties p = DMZServerCommon.getResponse(id, 20);
                si = (Properties)p.get("data");
            }
            Vector<String> v = (Vector<String>)si.get(String.valueOf(param) + "_history");
            if (v == null) {
                v = new Vector<String>();
                v.addElement(si.getProperty(param));
            }
            int loc = v.size() - 1;
            int intervals = Integer.parseInt(request.getProperty("priorIntervals", "1"));
            xml.append("<" + param + ">");
            while (intervals > 0 && loc >= 0) {
                xml.append(v.elementAt(loc).toString()).append(intervals > 1 || loc > 0 ? "," : "");
                --intervals;
                --loc;
            }
            xml.append("</" + param + ">");
            ++x;
        }
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
        response = String.valueOf(response) + "<result><response_data>" + xml + "</response_data></result>";
        return response;
    }

    static String expandUsernames(String users) {
        int xx;
        if (expandUsernames_cache.containsKey(users) && System.currentTimeMillis() - Long.parseLong(expandUsernames_cache.getProperty(String.valueOf(users) + "time", "0")) < 30000L) {
            return expandUsernames_cache.getProperty(users);
        }
        String userNamesStr = "";
        Properties groupsAll = new Properties();
        if (users.indexOf("...") >= 0) {
            Vector the_list = (Vector)((Vector)ServerStatus.server_settings.get("server_list")).clone();
            xx = 0;
            while (xx < the_list.size()) {
                Properties server_item = (Properties)the_list.elementAt(xx);
                Properties groups = UserTools.getGroups(server_item.getProperty("linkedServer"));
                Enumeration<Object> keys = groups.keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement().toString();
                    groupsAll.put("..." + key.toUpperCase(), groups.get(key));
                }
                ++xx;
            }
        }
        String[] usernames = com.crushftp.client.Common.html_clean_usernames(users.split(","));
        xx = 0;
        while (xx < usernames.length) {
            String username = usernames[xx].toUpperCase().trim();
            if (username.startsWith("...")) {
                Vector usernames2 = (Vector)groupsAll.get(username);
                int xxx = 0;
                while (xxx < usernames2.size()) {
                    userNamesStr = String.valueOf(userNamesStr) + usernames2.elementAt(xxx).toString().toUpperCase().trim() + ",";
                    ++xxx;
                }
            } else {
                userNamesStr = String.valueOf(userNamesStr) + username + ",";
            }
            ++xx;
        }
        expandUsernames_cache.put(String.valueOf(users) + "_time", String.valueOf(System.currentTimeMillis()));
        expandUsernames_cache.put(users, userNamesStr);
        return userNamesStr;
    }

    public static Properties getJobsSettings(Properties request, String site) throws Exception {
        if (ServerStatus.siIG("enterprise_level") <= 0) {
            return new Properties();
        }
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 20);
                return (Properties)p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return new Properties();
            }
        }
        Properties p = new Properties();
        p.put("job_scheduler_enabled", ServerStatus.SG("job_scheduler_enabled"));
        p.put("single_job_scheduler_serverbeat", ServerStatus.SG("single_job_scheduler_serverbeat"));
        p.put("store_job_items", ServerStatus.SG("store_job_items"));
        p.put("job_statistics_enabled", ServerStatus.SG("job_statistics_enabled"));
        p.put("audit_job_logs", ServerStatus.SG("audit_job_logs"));
        return p;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    public static Vector getJobsSummary(Properties request, String site) throws Exception {
        if (ServerStatus.siIG("enterprise_level") <= 0) {
            return new Vector<E>();
        }
        si = null;
        if (!request.getProperty("instance", "").equals("")) {
            id = Common.makeBoundary();
            instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                p = DMZServerCommon.getResponse(id, 20);
                return (Vector)p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return null;
            }
        }
        si = ServerStatus.thisObj.server_info;
        jobs = (Vector)si.get("running_tasks");
        vv = new Vector<Properties>();
        x = 0;
        while (x < jobs.size()) {
            tracker = (Properties)jobs.elementAt(x);
            summaryJob = new Properties();
            summaryJob.put("name", "");
            summaryJob.put("start", tracker.getProperty("start", ""));
            summaryJob.put("id", tracker.getProperty("id", ""));
            summaryJob.put("log_file", tracker.getProperty("log_file", ""));
            summaryJob.put("stop", tracker.getProperty("stop", ""));
            summaryJob.put("status", tracker.getProperty("status", ""));
            settings = (Properties)tracker.get("settings");
            if (settings == null) {
                settings = new Properties();
            }
            set2 = new Properties();
            set2.put("pluginName", settings.getProperty("pluginName", ""));
            set2.put("subItem", settings.getProperty("subItem", ""));
            set2.put("scheduleName", settings.getProperty("scheduleName", ""));
            set2.put("name", settings.getProperty("name", ""));
            set2.put("id", settings.getProperty("id", ""));
            summaryJob.put("settings", set2);
            if (site.indexOf("(JOB_MONITOR)") < 0 || AdminControls.expandUsernames(settings.getProperty("allowed_usernames", "")).indexOf(request.getProperty("calling_user", "~NONE~").toUpperCase()) >= 0) {
                vv.insertElementAt(summaryJob, 0);
            }
            ++x;
        }
        start_time = Long.parseLong(request.getProperty("start_time", "0"));
        end_time = Long.parseLong(request.getProperty("end_time", "0"));
        jobs = JobScheduler.getJobList(request.getProperty("hideUserActiveSchedules", "false").equals("false"));
        sdf = new SimpleDateFormat("yyMMddHHmmss", Locale.US);
        vv2 = new Vector<Properties>();
        filter = request.getProperty("filter", "").toUpperCase();
        var12_18 = AdminControls.jobs_summary_cache;
        synchronized (var12_18) {
            x = 0;
            while (x < jobs.size()) {
                f = (File_S)jobs.elementAt(x);
                f2 = (File_S[])f.listFiles();
                xx = 0;
                while (f2 != null && xx < f2.length) {
                    block39: {
                        job_name = f2[xx].getName();
                        if (!job_name.equalsIgnoreCase("job.XML") && !job_name.equalsIgnoreCase("inprogress.XML") && !job_name.equalsIgnoreCase("inprogress") && job_name.toUpperCase().endsWith(".XML") && (job_name = job_name.substring(0, job_name.lastIndexOf(".XML"))).indexOf("_") >= 0) {
                            job_id = job_name.split("_")[0];
                            try {
                                if (job_name.split("_")[1].equals("new") || sdf.parse(job_name.split("_")[1]).getTime() < start_time || sdf.parse(job_name.split("_")[1]).getTime() > end_time) break block39;
                                summaryJob = null;
                                if (AdminControls.jobs_summary_cache.containsKey(f2[xx].getPath())) {
                                    sj = (Properties)AdminControls.jobs_summary_cache.get(f2[xx].getPath());
                                    if (site.indexOf("(JOB_MONITOR)") >= 0 && AdminControls.expandUsernames((settings = (Properties)sj.get("settings")).getProperty("allowed_usernames", "")).indexOf(request.getProperty("calling_user", "~NONE~").toUpperCase()) < 0) break block39;
                                    if (sj.getProperty("modified", "0").equals(String.valueOf(f2[xx].lastModified()))) {
                                        summaryJob = sj;
                                    }
                                }
                                ok = false;
                                if (summaryJob == null) {
                                    tracker = (Properties)Common.readXMLObject(f2[xx].getPath());
                                    settings = (Properties)tracker.get("settings");
                                    if (site.indexOf("(JOB_MONITOR)") >= 0 && AdminControls.expandUsernames(settings.getProperty("allowed_usernames", "")).indexOf(request.getProperty("calling_user", "~NONE~").toUpperCase()) < 0) break block39;
                                    summaryJob = new Properties();
                                    summaryJob.put("name", "");
                                    summaryJob.put("start", tracker.getProperty("start", ""));
                                    summaryJob.put("end", tracker.getProperty("end", "0"));
                                    summaryJob.put("id", job_id);
                                    summaryJob.put("log_file", tracker.getProperty("log_file", ""));
                                    summaryJob.put("stop", tracker.getProperty("stop", ""));
                                    summaryJob.put("status", tracker.getProperty("status", ""));
                                    if (settings == null) {
                                        settings = new Properties();
                                    }
                                    set2 = new Properties();
                                    set2.put("pluginName", settings.getProperty("pluginName", ""));
                                    set2.put("subItem", settings.getProperty("subItem", ""));
                                    set2.put("scheduleName", settings.getProperty("scheduleName", ""));
                                    set2.put("name", settings.getProperty("name", ""));
                                    set2.put("id", settings.getProperty("id", ""));
                                    if (site.indexOf("(JOB_MONITOR)") >= 0) {
                                        set2.put("allowed_usernames", settings.getProperty("allowed_usernames", ""));
                                    }
                                    summaryJob.put("settings", set2);
                                    summaryJob.put("modified", String.valueOf(f2[xx].lastModified()));
                                    AdminControls.jobs_summary_cache.put(f2[xx].getPath(), summaryJob);
                                }
                                if (!filter.equals("")) {
                                    settings = (Properties)summaryJob.get("settings");
                                    if (settings.getProperty("scheduleName", "").toUpperCase().indexOf(filter) >= 0) {
                                        ok = true;
                                    } else if (summaryJob.getProperty("status", "").toUpperCase().indexOf(filter) >= 0) {
                                        ok = true;
                                    } else {
                                        start_d = new Date(Long.parseLong(summaryJob.getProperty("start", "0")));
                                        end_d = new Date(Long.parseLong(summaryJob.getProperty("end", "0")));
                                        duration_d = new Date(Long.parseLong(summaryJob.getProperty("end", "0")) - Long.parseLong(summaryJob.getProperty("start", "0")));
                                        sdf2 = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa");
                                        sdf3 = new SimpleDateFormat("hh:mm:ss aa");
                                        s1 = sdf2.format(start_d).toUpperCase();
                                        s2 = sdf2.format(end_d).toUpperCase();
                                        s3 = sdf3.format(duration_d).toUpperCase();
                                        if (s1.indexOf(filter) >= 0) {
                                            ok = true;
                                        } else if (s2.indexOf(filter) >= 0) {
                                            ok = true;
                                        } else if (s3.indexOf(filter) >= 0) {
                                            ok = true;
                                        }
                                    }
                                } else {
                                    ok = true;
                                }
                                if (ok) {
                                    vv2.insertElementAt(summaryJob, 0);
                                }
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 1, e);
                            }
                        }
                    }
                    ++xx;
                }
                ++x;
            }
        }
        Common.do_sort(vv, "", "start");
        Common.do_sort(vv2, "", "start");
        if (true) ** GOTO lbl168
        do {
            vv2.removeElementAt(vv2.size() - 1);
lbl168:
            // 2 sources

        } while (vv2.size() > ServerStatus.IG("max_job_summary_scan"));
        vv2.addAll(vv);
        vv = vv2;
        var12_18 = AdminControls.jobs_summary_cache;
        synchronized (var12_18) {
            if (System.currentTimeMillis() - AdminControls.last_job_cache_clean > 60000L) {
                keys = AdminControls.jobs_summary_cache.keys();
                while (keys.hasMoreElements()) {
                    key = keys.nextElement().toString();
                    sj = (Properties)AdminControls.jobs_summary_cache.get(key);
                    f = new File_S(key);
                    if (f.exists() && sj.getProperty("modified", "0").equals(String.valueOf(f.lastModified()))) continue;
                    AdminControls.jobs_summary_cache.remove(key);
                }
                AdminControls.last_job_cache_clean = System.currentTimeMillis();
            }
        }
        return vv;
    }

    public static Vector getJobInfo(Properties request, String site) throws Exception {
        if (ServerStatus.siIG("enterprise_level") <= 0) {
            return new Vector();
        }
        Properties si = null;
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 20);
                return (Vector)p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return null;
            }
        }
        si = ServerStatus.thisObj.server_info;
        Vector jobs = (Vector)si.get("running_tasks");
        Vector<Object> vv = new Vector<Object>();
        int x = 0;
        while (vv.size() == 0 && x < jobs.size()) {
            Properties settings;
            Properties job = (Properties)jobs.elementAt(x);
            if (job.getProperty("id").equals(request.getProperty("job_id")) && ((settings = (Properties)job.get("settings")) == null || site.indexOf("(JOB_MONITOR)") < 0 || AdminControls.expandUsernames(settings.getProperty("allowed_usernames", "")).indexOf(request.getProperty("calling_user", "~NONE~").toUpperCase()) >= 0)) {
                vv.addElement(job.clone());
            }
            ++x;
        }
        if (vv.size() == 0) {
            if (!request.getProperty("scheduleName", "").equals("")) {
                File_S f = new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + com.crushftp.client.Common.dots(request.getProperty("scheduleName", "")));
                AdminControls.checkJobFolder(site, request, vv, f);
            } else {
                jobs = JobScheduler.getJobList(false);
                x = 0;
                while (vv.size() == 0 && x < jobs.size()) {
                    AdminControls.checkJobFolder(site, request, vv, (File_S)jobs.elementAt(x));
                    ++x;
                }
            }
        }
        x = 0;
        while (x < vv.size()) {
            Vector active_items;
            Properties tracker = (Properties)vv.elementAt(x);
            if (request.getProperty("extra_keys", "settings").indexOf("settings") < 0) {
                tracker.remove("settings");
            }
            if ((active_items = (Vector)tracker.get("active_items")) != null) {
                active_items = (Vector)active_items.clone();
                tracker.put("active_items", active_items);
                int xx = 0;
                while (xx < active_items.size()) {
                    Properties item;
                    int xxx;
                    Vector items2;
                    Properties p;
                    Properties active_item = (Properties)active_items.elementAt(xx);
                    active_item = (Properties)active_item.clone();
                    active_items.setElementAt(active_item, xx);
                    Vector newItems = (Vector)active_item.get("newItems");
                    Vector items = (Vector)active_item.get("items");
                    active_item.put("newItems_count", "0");
                    active_item.put("items_count", "0");
                    if (newItems != null) {
                        active_item.put("newItems_count", String.valueOf(newItems.size()));
                    }
                    if (items != null) {
                        active_item.put("items_count", String.valueOf(items.size()));
                    }
                    if (items != null && items.size() == 1 && (p = (Properties)items.elementAt(0)).containsKey("incoming_count")) {
                        active_item.put("items_count", p.getProperty("incoming_count"));
                    }
                    if (newItems != null && newItems.size() == 1 && (p = (Properties)newItems.elementAt(0)).containsKey("incoming_count")) {
                        active_item.put("newItems_count", p.getProperty("outgoing_count"));
                    }
                    if (request.getProperty("extra_keys", "active_prefs").indexOf("active_prefs") < 0 || site.indexOf("(JOB_MONITOR)") >= 0) {
                        active_item.remove("prefs");
                    }
                    if (request.getProperty("extra_keys", "active_items").indexOf("active_items") < 0 || site.indexOf("(JOB_MONITOR)") >= 0) {
                        active_item.remove("items");
                    }
                    if (request.getProperty("extra_keys", "active_newItems").indexOf("active_newItems") < 0 || site.indexOf("(JOB_MONITOR)") >= 0) {
                        active_item.remove("newItems");
                    }
                    if (active_item.containsKey("items")) {
                        items2 = (Vector)com.crushftp.client.Common.CLONE(active_item.get("items"));
                        active_item.put("items", items2);
                        xxx = 0;
                        while (xxx < items2.size()) {
                            item = (Properties)items2.elementAt(xxx);
                            if (!item.getProperty("url", "FILE:").toUpperCase().startsWith("FILE:")) {
                                item.put("url", new VRL(item.getProperty("url")).safe());
                            }
                            ++xxx;
                        }
                    }
                    if (active_item.containsKey("newItems")) {
                        items2 = (Vector)com.crushftp.client.Common.CLONE(active_item.get("newItems"));
                        active_item.put("newItems", items2);
                        xxx = 0;
                        while (xxx < items2.size()) {
                            item = (Properties)items2.elementAt(xxx);
                            if (!item.getProperty("url", "FILE:").toUpperCase().startsWith("FILE:")) {
                                item.put("url", new VRL(item.getProperty("url")).safe());
                            }
                            ++xxx;
                        }
                    }
                    ++xx;
                }
            }
            ++x;
        }
        return vv;
    }

    public static void checkJobFolder(String site, Properties request, Vector vv, File_S f) {
        File_S[] f2 = (File_S[])f.listFiles();
        int xx = 0;
        while (vv.size() == 0 && f2 != null && xx < f2.length) {
            String job_id;
            String job_name = f2[xx].getName();
            if (!job_name.equalsIgnoreCase("job.XML") && !job_name.equalsIgnoreCase("inprogress.XML") && !job_name.equalsIgnoreCase("inprogress") && job_name.toUpperCase().endsWith(".XML") && (job_id = (job_name = job_name.substring(0, job_name.lastIndexOf(".XML"))).split("_")[0]).equals(request.getProperty("job_id"))) {
                Properties settings;
                Properties tracker = (Properties)Common.readXMLObject(f2[xx].getPath());
                tracker.put("job_history_obj_path", f2[xx].getPath());
                if (site.indexOf("(JOB_MONITOR)") < 0 || AdminControls.expandUsernames((settings = (Properties)tracker.get("settings")).getProperty("allowed_usernames", "")).indexOf(request.getProperty("calling_user", "~NONE~").toUpperCase()) >= 0) {
                    vv.addElement(tracker.clone());
                }
            }
            ++xx;
        }
    }

    public static Vector getSessionList(Properties request) throws Exception {
        Properties si = null;
        if (request.getProperty("instance", "").equals("")) {
            si = ServerStatus.thisObj.server_info;
        } else {
            String id = Common.makeBoundary();
            DMZServerCommon.sendCommand(request.getProperty("instance", ""), request, "GET:SERVER_INFO", id);
            Properties p = DMZServerCommon.getResponse(id, 20);
            si = (Properties)p.get("data");
        }
        Vector v = (Vector)((Vector)si.get(request.getProperty("session_list"))).clone();
        Vector<Properties> vv = new Vector<Properties>();
        int x = 0;
        while (x < v.size()) {
            Properties user_info = (Properties)v.elementAt(x);
            Properties p = new Properties();
            p.put("user_name", user_info.getProperty("user_name"));
            p.put("user_number", user_info.getProperty("user_number"));
            p.put("user_ip", user_info.getProperty("user_ip"));
            p.put("user_protocol", user_info.getProperty("user_protocol"));
            p.put("current_dir", user_info.getProperty("current_dir"));
            p.put("last_activity", user_info.getProperty("last_activity", "0"));
            vv.addElement(p);
            ++x;
        }
        return vv;
    }

    public static String newFolder(Properties request, String site, boolean replicate) {
        if (replicate) {
            Properties pp = new Properties();
            pp.put("request", request);
            pp.put("site", site);
            SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.AdminControls.newFolder", "info", pp);
        }
        if (Common.machine_is_x() && !new File_U(request.getProperty("path")).exists()) {
            request.put("path", "/Volumes" + request.getProperty("path"));
        }
        if (!new File_U(String.valueOf(request.getProperty("path")) + request.getProperty("name")).mkdirs()) {
            return "New Folder Failed!";
        }
        Common.updateOSXInfo(String.valueOf(request.getProperty("path")) + request.getProperty("name"));
        return "OK";
    }

    public static String renameItem(Properties request, String site, boolean replicate) {
        if (replicate) {
            Properties pp = new Properties();
            pp.put("request", request);
            pp.put("site", site);
            SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.AdminControls.renameItem", "info", pp);
        }
        if (Common.machine_is_x() && !new File_U(request.getProperty("path")).exists()) {
            request.put("path", "/Volumes" + request.getProperty("path"));
        }
        if (!new File_U(String.valueOf(request.getProperty("path")) + request.getProperty("name")).renameTo(new File_U(String.valueOf(request.getProperty("path")) + request.getProperty("newName")))) {
            return "Rename Failed!";
        }
        return "OK";
    }

    public static String duplicateItem(Properties request, String site, boolean replicate) {
        if (replicate) {
            Properties pp = new Properties();
            pp.put("request", request);
            pp.put("site", site);
            SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.AdminControls.duplicateItem", "info", pp);
        }
        if (Common.machine_is_x() && !new File_U(request.getProperty("path")).exists()) {
            request.put("path", "/Volumes" + request.getProperty("path"));
        }
        Vector list = new Vector();
        try {
            Common.getAllFileListing(list, new File_U(String.valueOf(request.getProperty("path")) + request.getProperty("name")).getCanonicalPath(), 5, true);
            if (list.size() > 100) {
                return "Too many items to allow duplicate! " + list.size();
            }
            Common.recurseCopy_U(new File_U(String.valueOf(request.getProperty("path")) + request.getProperty("name")).getCanonicalPath(), String.valueOf(new File_U(String.valueOf(request.getProperty("path")) + request.getProperty("name")).getCanonicalPath()) + "_tmp_" + Common.makeBoundary(), false);
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
            return "" + e;
        }
        return "OK";
    }

    public static String deleteItem(Properties request, String site, boolean replicate) {
        if (replicate) {
            Properties pp = new Properties();
            pp.put("request", request);
            pp.put("site", site);
            SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.AdminControls.deleteItem", "info", pp);
        }
        if (Common.machine_is_x() && !new File_U(request.getProperty("path")).exists()) {
            request.put("path", "/Volumes" + request.getProperty("path"));
        }
        if (!new File_U(String.valueOf(request.getProperty("path")) + request.getProperty("name")).exists()) {
            return "Item not found.";
        }
        if (!new File_U(String.valueOf(request.getProperty("path")) + request.getProperty("name")).delete()) {
            return "Delete Failed. (Folders must be empty to be deleted.)";
        }
        return "OK";
    }

    public static String setServerItem(Properties request, String site) {
        return AdminControls.setServerItem(request, site, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    public static String setServerItem(Properties request, String site, boolean replicate) {
        status = "OK";
        try {
            block64: {
                if (!request.getProperty("instance", "").equals("")) {
                    var4_4 = DMZServerCommon.stop_send_prefs;
                    synchronized (var4_4) {
                        id = Common.makeBoundary();
                        instance = request.remove("instance").toString();
                        DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
                        p = DMZServerCommon.getResponse(id, 20);
                        if (request.getProperty("key").indexOf("server_settings/") >= 0) {
                            id2 = Common.makeBoundary();
                            DMZServerCommon.sendCommand(instance, new Properties(), "GET:SERVER_SETTINGS", id2);
                            pp = DMZServerCommon.getResponse(id2, 20);
                            SharedSessionReplicated.send("", "WRITE_PREFS", instance, (Properties)pp.get("data"));
                            Thread.sleep(200L);
                            Common.write_server_settings((Properties)pp.get("data"), instance);
                        }
                        return p.get("data").toString();
                    }
                }
                if (replicate && request.getProperty("key").indexOf("/server_list/") < 0 && request.getProperty("data", "").indexOf("<replicate_session_host_port>") < 0) {
                    pp = new Properties();
                    pp.put("request", request);
                    pp.put("site", site);
                    SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.AdminControls.setServerItem", "info", pp);
                }
                original_disabled_ciphers = ServerStatus.SG("disabled_ciphers");
                keys = request.getProperty("key").split("/");
                o = null;
                log_summary = new StringBuffer();
                try {
                    x = 0;
                    while (x < keys.length - 1) {
                        key = keys[x];
                        if (key.equals("server_settings")) {
                            o = ServerStatus.server_settings;
                        } else if (key.equals("server_info")) {
                            o = ServerStatus.thisObj.server_info;
                        } else if (o instanceof Properties) {
                            o = o.get(key);
                        } else if (o instanceof Vector) {
                            o = ((Vector)o).elementAt(Integer.parseInt(key));
                        }
                        ++x;
                    }
                    lastKey = keys[keys.length - 1];
                    secondLastKey = "";
                    if (keys.length >= 2) {
                        secondLastKey = keys[keys.length - 2];
                    }
                    if (request.getProperty("key").equals("server_settings")) {
                        o = ServerStatus.server_settings;
                        lastKey = "server_prefs";
                    }
                    preview_config = ServerStatus.VG("preview_configs");
                    locked_preview = new Vector<Properties>();
                    x = 0;
                    while (x < preview_config.size()) {
                        p = (Properties)preview_config.elementAt(x);
                        locked_p = new Properties();
                        locked_p.put("preview_command_line", p.getProperty("preview_command_line", ""));
                        locked_p.put("preview_working_dir", p.getProperty("preview_working_dir", ""));
                        locked_p.put("preview_environment", p.getProperty("preview_environment", ""));
                        locked_p.put("preview_frames", p.getProperty("preview_frames", "1"));
                        locked_p.put("preview_movie_info_command_line", p.getProperty("preview_movie_info_command_line", ""));
                        locked_p.put("preview_exif_get_command_line", p.getProperty("preview_exif_get_command_line", ""));
                        locked_p.put("preview_exif_set_command_line", p.getProperty("preview_exif_set_command_line", ""));
                        locked_preview.addElement(locked_p);
                        ++x;
                    }
                    new_o = null;
                    new_o = !request.getProperty("data_type").equals("text") ? (request.getProperty("data").equals("") && request.getProperty("data_type").equals("vector") ? new Vector<E>() : Common.readXMLObject(new ByteArrayInputStream(Common.url_decode(request.getProperty("data").replace('+', ' ')).getBytes("UTF8")))) : Common.url_decode(request.getProperty("data", "").replace('+', ' '));
                    if (o instanceof Properties) {
                        if (lastKey.equals("server_prefs") && (new_o instanceof Properties || new_o instanceof Vector)) {
                            Common.updateObjectLog(new_o, o, request.getProperty("key"), true, log_summary);
                        } else if (new_o instanceof Properties || new_o instanceof Vector) {
                            Common.updateObjectLog(new_o, ((Properties)o).get(lastKey), request.getProperty("key"), true, log_summary);
                        } else {
                            ((Properties)o).put(lastKey, new_o.toString());
                        }
                        if (!System.getProperty("crushftp.user.root", "").equals("") || !System.getProperty("crushftp.server.root", "").equals("")) {
                            preview_config = ServerStatus.VG("preview_configs");
                            x = 0;
                            while (x < locked_preview.size()) {
                                locked_p = (Properties)locked_preview.elementAt(x);
                                p = (Properties)preview_config.elementAt(x);
                                p.putAll((Map<?, ?>)locked_p);
                                ++x;
                            }
                        }
                        if (request.getProperty("data", "").indexOf("<replicate_session_host_port>") >= 0) {
                            SharedSessionReplicated.reset_sockets();
                        }
                        break block64;
                    }
                    if (!(o instanceof Vector)) break block64;
                    v = (Vector)o;
                    if (request.getProperty("data_action", "").equals("reset")) {
                        if (request.getProperty("key").indexOf("/plugins/") >= 0) {
                            if (new_o == null) {
                                new_o = new Vector<E>();
                            }
                            Common.updateObjectLog(new_o, v, request.getProperty("key"), true, log_summary);
                        } else {
                            if (new_o == null) {
                                new_o = new Vector<E>();
                            }
                            if (request.getProperty("key").startsWith("server_settings/ip_restrictions/")) {
                                new_o_tmp = (Vector)com.crushftp.client.Common.CLONE(new_o);
                                v_temp = (Vector)com.crushftp.client.Common.CLONE(v);
                                Collections.reverse(new_o_tmp);
                                Collections.reverse(v_temp);
                                Common.updateObjectLog(new_o_tmp, v_temp, request.getProperty("key"), true, log_summary);
                                Common.updateObjectLog(new_o, v, new StringBuffer(), true);
                            } else {
                                Common.updateObjectLog(new_o, v, request.getProperty("key"), true, log_summary);
                            }
                        }
                    } else {
                        i = Integer.parseInt(lastKey);
                        if (request.getProperty("data_action", "").equals("remove")) {
                            delO = v.remove(i);
                            Common.updateObjectLogOnly(delO, String.valueOf(request.getProperty("key")) + ":remove ", log_summary);
                        } else if (request.getProperty("data_action", "").equals("move_left")) {
                            o2 = v.elementAt(i);
                            if (i > 0) {
                                o1 = v.elementAt(i - 1);
                                v.setElementAt(o2, i - 1);
                                v.setElementAt(o1, i);
                                if (i - 1 == 0) {
                                    ((Properties)o1).put("subItem", "");
                                }
                            }
                            Common.updateObjectLogOnly(o2, String.valueOf(request.getProperty("key")) + ":move_left " + i, log_summary);
                        } else if (request.getProperty("data_action", "").equals("move_right")) {
                            o2 = v.elementAt(i);
                            if (i <= v.size() - 2) {
                                o1 = v.elementAt(i + 1);
                                v.setElementAt(o2, i + 1);
                                v.setElementAt(o1, i);
                            }
                            Common.updateObjectLogOnly(o2, String.valueOf(request.getProperty("key")) + ":move_right " + i, log_summary);
                        } else if (i > v.size() - 1) {
                            v.addElement(new_o);
                            Common.updateObjectLogOnly(new_o, String.valueOf(request.getProperty("key")) + ":add " + v.size(), log_summary);
                            if (v == (Vector)ServerStatus.server_settings.get("server_list")) {
                                ServerStatus.thisObj.start_this_server(i);
                            }
                        } else if (new_o instanceof Properties || new_o instanceof Vector) {
                            Common.updateObjectLog(new_o, v.elementAt(i), request.getProperty("key"), true, log_summary);
                        } else {
                            v.setElementAt(new_o.toString(), i);
                            Common.updateObjectLogOnly(new_o, String.valueOf(request.getProperty("key")) + "/" + i + " " + i + "=", log_summary);
                        }
                    }
                    if (!System.getProperty("crushftp.user.root", "").equals("") || !System.getProperty("crushftp.server.root", "").equals("")) {
                        preview_config = ServerStatus.VG("preview_configs");
                        x = 0;
                        while (x < locked_preview.size()) {
                            locked_p = (Properties)locked_preview.elementAt(x);
                            p = (Properties)preview_config.elementAt(x);
                            p.putAll((Map<?, ?>)locked_p);
                            ++x;
                        }
                    }
                    if (request.getProperty("key").indexOf("/plugins/") >= 0) {
                        ServerStatus.thisObj.common_code.loadPluginsSync(ServerStatus.server_settings, ServerStatus.thisObj.server_info);
                    }
                    if (request.getProperty("key").indexOf("server_settings") >= 0) {
                        ServerStatus.thisObj.reset_preview_workers();
                    }
                    found_servers = new Vector<GenericServer>();
                    x = 0;
                    ** GOTO lbl191
lbl-1000:
                    // 1 sources

                    {
                        new_server_item = (Properties)ServerStatus.VG("server_list").elementAt(x);
                        xx = 0;
                        while (xx < ServerStatus.thisObj.main_servers.size()) {
                            gs = (GenericServer)ServerStatus.thisObj.main_servers.elementAt(xx);
                            if (gs.server_item.getProperty("serverType", "").equals(new_server_item.getProperty("serverType", "")) && gs.server_item.getProperty("ip", "").equals(new_server_item.getProperty("ip", "")) && gs.server_item.getProperty("port", "").equals(new_server_item.getProperty("port", ""))) {
                                gs.server_item = new_server_item;
                                gs.updateStatus();
                                found_servers.addElement(gs);
                                break;
                            }
                            ++xx;
                        }
                        ++x;
lbl191:
                        // 2 sources

                        ** while (x < ServerStatus.VG((String)"server_list").size())
                    }
lbl192:
                    // 1 sources

                    xx = ServerStatus.thisObj.main_servers.size() - 1;
                    while (xx >= 0) {
                        gs = (GenericServer)ServerStatus.thisObj.main_servers.elementAt(xx);
                        if (found_servers.indexOf(gs) < 0) {
                            ServerStatus.thisObj.stop_this_server(xx);
                            ServerStatus.thisObj.main_servers.remove(xx);
                        }
                        --xx;
                    }
                    if (!original_disabled_ciphers.equals(ServerStatus.SG("disabled_ciphers"))) {
                        x = 0;
                        while (x < ServerStatus.thisObj.main_servers.size()) {
                            gs = (GenericServer)ServerStatus.thisObj.main_servers.elementAt(x);
                            if (gs.server_item.getProperty("serverType", "").equals("HTTPS") || gs.server_item.getProperty("serverType", "").equals("FTPS")) {
                                ServerStatus.thisObj.stop_this_server(x);
                                ServerStatus.thisObj.start_this_server(x);
                            }
                            ++x;
                        }
                    }
                    QuickConnect.ip_cache.clear();
                }
                catch (Exception e) {
                    Log.log("HTTP_SERVER", 1, e);
                    status = "FAILURE:" + e.toString();
                }
            }
            if ((template = Common.get_email_template("Change Setting Email")) != null) {
                body = template.getProperty("emailBody");
                body = Common.replace_str(body, "{keys}", request.getProperty("key"));
                body = Common.replace_str(body, "{summary}", log_summary.toString());
                body = Common.replace_str(body, "{username}", request.getProperty("username", ""));
                body = Common.replace_str(body, "{user_name}", request.getProperty("username", ""));
                subject = template.getProperty("emailSubject");
                subject = Common.replace_str(subject, "{keys}", request.getProperty("key"));
                subject = Common.replace_str(subject, "{summary}", log_summary.toString());
                subject = Common.replace_str(subject, "{username}", request.getProperty("username", ""));
                subject = Common.replace_str(subject, "{user_name}", request.getProperty("username", ""));
                email_info = new Properties();
                email_info.put("server", ServerStatus.SG("smtp_server"));
                email_info.put("user", ServerStatus.SG("smtp_user"));
                email_info.put("pass", ServerStatus.SG("smtp_pass"));
                email_info.put("ssl", ServerStatus.SG("smtp_ssl"));
                email_info.put("html", ServerStatus.SG("smtp_html"));
                email_info.put("from", template.getProperty("emailFrom"));
                email_info.put("reply_to", template.getProperty("emailReplyTo"));
                email_info.put("to", template.getProperty("emailCC"));
                email_info.put("subject", subject);
                email_info.put("body", body);
                ServerStatus.thisObj.sendEmail(email_info);
            }
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            Log.log("HTTP_SERVER", 1, request.getProperty("key"));
            status = e.toString();
        }
        ServerStatus.thisObj.save_server_settings(false);
        return status;
    }

    public static Object getUser(Properties request, String site, SessionCrush thisSession) {
        Properties p;
        if (site.indexOf("(CONNECT)") < 0) {
            request.put("serverGroup", thisSession.server_item.getProperty("linkedServer"));
        }
        if (request.getProperty("serverGroup_original", "").equals("extra_vfs")) {
            request.put("serverGroup", "extra_vfs");
        }
        String status = "OK";
        String username = Common.url_decode(request.getProperty("username").replace('+', ' '));
        Vector<Properties> extra_vfs = new Vector<Properties>();
        if (request.getProperty("serverGroup").endsWith("_restored_backup")) {
            String source_path = String.valueOf(System.getProperty("crushftp.backup")) + "backup/" + request.getProperty("user_zip_file");
            String dest_path = String.valueOf(System.getProperty("crushftp.backup")) + "backup/" + request.getProperty("username");
            status = AdminControls.unzip_backup_folder(status, new File_S(source_path), dest_path);
            if (!(request.getProperty("user_zip_file").contains("~") || site.indexOf("(CONNECT)") <= 0 && site.indexOf("(USER_EDIT)") <= 0)) {
                String file_end = request.getProperty("user_zip_file").substring(request.getProperty("user_zip_file").indexOf("-"), request.getProperty("user_zip_file").length());
                File_S[] folders = (File_S[])new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/").listFiles();
                extra_vfs = new Vector();
                int x = 0;
                while (x < folders.length) {
                    File_S f = folders[x];
                    if (f.getName().startsWith(String.valueOf(username) + "~") && f.getName().endsWith(file_end)) {
                        p = new Properties();
                        p.put(f.getName().subSequence(f.getName().indexOf("~") + 1, f.getName().indexOf("-")), f.getName());
                        extra_vfs.add(p);
                    }
                    ++x;
                }
            }
        }
        try {
            if (!request.getProperty("instance", "").equals("")) {
                String id = Common.makeBoundary();
                String instance = request.remove("instance").toString();
                DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
                Properties p2 = DMZServerCommon.getResponse(id, 20);
                return p2.get("data");
            }
            VFS uVFS = UserTools.ut.getVFS(request.getProperty("serverGroup"), username);
            Properties new_user = UserTools.ut.getUser(request.getProperty("serverGroup"), username, false);
            if (new_user == null || !new_user.getProperty("username", "not found").equalsIgnoreCase(username)) {
                throw new Exception("User not found:" + username);
            }
            if (site.indexOf("(CONNECT)") < 0 && site.indexOf("(USER_VIEW)") < 0 && site.indexOf("(USER_EDIT)") < 0) {
                Vector list = new Vector();
                Properties info = (Properties)thisSession.get("user_admin_info");
                list = (Vector)info.get("list");
                String admin_group_name = request.getProperty("admin_group_name", "");
                if (admin_group_name.equals("")) {
                    admin_group_name = request.getProperty("serverGroup_original", "");
                }
                if (!username.equals(admin_group_name)) {
                    if (request.getProperty("serverGroup_original", "").equals("extra_vfs")) {
                        if (list.indexOf(username.substring(0, username.lastIndexOf("~"))) < 0) {
                            throw new Exception("Username " + username + " not found.");
                        }
                    } else {
                        if (new_user != null && !username.equals("default") && list.indexOf(username) < 0) {
                            return new Properties();
                        }
                        if (list.indexOf(username) < 0 && !username.equals(thisSession.SG("admin_group_name"))) {
                            throw new Exception("Username " + username + " not found.");
                        }
                    }
                }
            }
            if (request.getProperty("serverGroup").endsWith("_restored_backup") && !request.getProperty("user_zip_file", "").contains("~")) {
                if (!extra_vfs.isEmpty()) {
                    new_user.put("extra_vfs", extra_vfs);
                }
            } else {
                UserTools.getExtraVFS(request.getProperty("serverGroup"), username, null, new_user);
            }
            Vector vfs_items = new Vector();
            Properties virtual = (Properties)uVFS.homes.elementAt(0);
            Enumeration<Object> keys = virtual.keys();
            while (keys.hasMoreElements()) {
                String virtualPath;
                String key = keys.nextElement().toString();
                if (key.equals("vfs_permissions_object") || (virtualPath = (p = (Properties)virtual.get(key)).getProperty("virtualPath")).equals("")) continue;
                Properties dir_item = new Properties();
                if (p.getProperty("type").equalsIgnoreCase("DIR")) {
                    if (!virtualPath.endsWith("/")) {
                        virtualPath = String.valueOf(virtualPath) + "/";
                    }
                    dir_item.put("url", "");
                    dir_item.put("type", "DIR");
                    if (!p.getProperty("modified", "0").equals("0")) {
                        dir_item.put("modified", p.getProperty("modified", "0"));
                    }
                } else {
                    Vector vItems = (Vector)p.get("vItems");
                    dir_item = (Properties)((Properties)vItems.elementAt(0)).clone();
                }
                dir_item.put("path", Common.all_but_last(virtualPath));
                dir_item.put("name", p.getProperty("name"));
                Vector<Properties> wrapper = new Vector<Properties>();
                wrapper.addElement(dir_item);
                vfs_items.addElement(wrapper);
            }
            String pass = new_user.getProperty("password", "");
            if (!(pass.startsWith("SHA:") || pass.startsWith("SHA512:") || pass.startsWith("SHA256:") || pass.startsWith("SHA3:") || pass.startsWith("MD5:") || pass.startsWith("CRYPT3:") || pass.startsWith("BCRYPT:") || pass.startsWith("MD5CRYPT:") || pass.startsWith("PBKDF2SHA256:") || pass.startsWith("SHA512CRYPT:") || pass.startsWith("ARGOND:"))) {
                pass = ServerStatus.thisObj.common_code.decode_pass(pass);
                new_user.put("password", pass);
            } else {
                new_user.put("password", "SHA3:XXXXXXXXXXXXXXXXXXXX");
            }
            if (!new_user.getProperty("userVersion", "").equals("6") && !new_user.getProperty("as2EncryptKeystorePassword", "").equals("")) {
                new_user.put("as2EncryptKeystorePassword", ServerStatus.thisObj.common_code.encode_pass(new_user.getProperty("as2EncryptKeystorePassword", ""), "DES", ""));
                new_user.put("as2EncryptKeyPassword", ServerStatus.thisObj.common_code.encode_pass(new_user.getProperty("as2EncryptKeyPassword", ""), "DES", ""));
            }
            Properties user_items = new Properties();
            user_items.put("user", new_user);
            if (uVFS.permissions == null) {
                uVFS.permissions = new Vector();
            }
            if (uVFS.permissions.size() == 0) {
                uVFS.permissions.addElement(new Properties());
            }
            user_items.put("permissions", uVFS.permissions.elementAt(0));
            user_items.put("vfs_items", vfs_items);
            if (new_user.containsKey("web_buttons")) {
                Vector buttons = (Vector)new_user.get("web_buttons");
                ServerSessionAJAX.fixButtons(buttons);
            }
            if (request.getProperty("serverGroup").endsWith("_restored_backup")) {
                Common.recurseDelete(String.valueOf(System.getProperty("crushftp.backup")) + "backup/" + request.getProperty("username"), false);
            }
            return user_items;
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            status = e.toString();
            return status;
        }
    }

    private static String unzip_backup_folder(String status, File_S source_file, String dest_path) {
        new File_S(dest_path).mkdir();
        try {
            ZipEntry entry;
            ZipInputStream zin = new ZipInputStream(new FileInputStream(source_file));
            while ((entry = zin.getNextEntry()) != null) {
                String path = entry.getName();
                if (entry.isDirectory()) {
                    new File_S(String.valueOf(dest_path) + path).mkdirs();
                    continue;
                }
                File_S file_entry = new File_S(String.valueOf(dest_path) + path);
                if (!new File_S(Common.all_but_last(String.valueOf(dest_path) + path)).exists()) {
                    new File_S(Common.all_but_last(String.valueOf(dest_path) + path)).mkdirs();
                }
                RandomAccessFile out = new RandomAccessFile(file_entry, "rw");
                byte[] b = new byte[32768];
                int bytes_read = 0;
                while (bytes_read >= 0) {
                    bytes_read = zin.read(b);
                    if (bytes_read <= 0 || out == null) continue;
                    out.write(b, 0, bytes_read);
                }
                out.close();
            }
            zin.close();
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            status = e.toString();
        }
        return status;
    }

    public static Object getPublicKeys(Properties request) {
        return "";
    }

    public static String setUserItem(Properties request, SessionCrush thisSession, String site) {
        String status;
        block100: {
            status = "OK";
            try {
                StringBuffer log_summary = new StringBuffer();
                String s = AdminControls.handleInstance(request, site);
                if (s != null) {
                    return s;
                }
                try {
                    if (request.getProperty("xmlItem", "").equals("groups")) {
                        Properties groups = null;
                        if (request.getProperty("data_action", "").equals("add")) {
                            groups = UserTools.getGroups(request.getProperty("serverGroup"));
                            Vector<String> group = (Vector<String>)groups.get(request.getProperty("group_name"));
                            if (group == null) {
                                group = new Vector<String>();
                            }
                            groups.put(request.getProperty("group_name"), group);
                            if (!request.containsKey("usernames")) {
                                request.put("usernames", request.getProperty("username", ""));
                            }
                            String[] usernames = com.crushftp.client.Common.html_clean_usernames(Common.url_decode(request.getProperty("usernames").replace('+', ' ')).split(";"));
                            int x = 0;
                            while (x < usernames.length) {
                                group.addElement(usernames[x].trim());
                                ++x;
                            }
                            Common.updateObjectLogOnly("add " + request.getProperty("usernames"), "users/" + request.getProperty("serverGroup") + "/inheritance/" + request.getProperty("group_name"), log_summary);
                        } else if (request.getProperty("data_action", "").equals("delete")) {
                            String[] usernames;
                            groups = UserTools.getGroups(request.getProperty("serverGroup"));
                            Vector group = (Vector)groups.get(request.getProperty("group_name"));
                            if (group == null) {
                                group = new Vector();
                            }
                            groups.put(request.getProperty("group_name"), group);
                            if (!request.containsKey("usernames")) {
                                request.put("usernames", request.getProperty("username", ""));
                            }
                            if ((usernames = com.crushftp.client.Common.html_clean_usernames(Common.url_decode(request.getProperty("usernames").replace('+', ' ')).split(";"))).length == 0 || request.getProperty("usernames").equals("")) {
                                groups.remove(request.getProperty("group_name"));
                            } else {
                                int x = 0;
                                while (x < usernames.length) {
                                    group.remove(usernames[x].trim());
                                    ++x;
                                }
                            }
                            Common.updateObjectLogOnly("delete " + request.getProperty("usernames"), "users/" + request.getProperty("serverGroup") + "/groups/" + request.getProperty("group_name"), log_summary);
                        } else {
                            groups = (Properties)Common.readXMLObjectError(new ByteArrayInputStream(Common.url_decode(request.getProperty("groups").replace('+', ' ')).getBytes("UTF8")));
                            Properties groups_original = UserTools.getGroups(request.getProperty("serverGroup"));
                            Common.updateObjectLog(groups, groups_original, "users/" + request.getProperty("serverGroup") + "/groups", false, log_summary);
                        }
                        if (groups == null) {
                            groups = new Properties();
                        }
                        UserTools.writeGroups(request.getProperty("serverGroup"), groups, true, request);
                        break block100;
                    }
                    if (request.getProperty("xmlItem", "").equals("inheritance")) {
                        Properties inheritances = null;
                        if (request.getProperty("data_action", "").equals("add")) {
                            inheritances = UserTools.getInheritance(request.getProperty("serverGroup"));
                            if (!request.containsKey("usernames")) {
                                request.put("usernames", request.getProperty("username", ""));
                            }
                            String[] usernames = com.crushftp.client.Common.html_clean_usernames(Common.url_decode(request.getProperty("usernames").replace('+', ' ')).split(";"));
                            int x = 0;
                            while (x < usernames.length) {
                                Vector<String> inherit = (Vector<String>)inheritances.get(usernames[x]);
                                if (inherit == null) {
                                    inherit = new Vector<String>();
                                }
                                inherit.addElement(request.getProperty("inheritance_name"));
                                inheritances.put(usernames[x], inherit);
                                ++x;
                            }
                            Common.updateObjectLogOnly("add " + request.getProperty("usernames"), "users/" + request.getProperty("serverGroup") + "/inheritance/" + request.getProperty("inheritance_name"), log_summary);
                        } else if (request.getProperty("data_action", "").equals("delete")) {
                            String[] usernames;
                            inheritances = UserTools.getInheritance(request.getProperty("serverGroup"));
                            if (!request.containsKey("usernames")) {
                                request.put("usernames", request.getProperty("username", ""));
                            }
                            if ((usernames = com.crushftp.client.Common.html_clean_usernames(Common.url_decode(request.getProperty("usernames").replace('+', ' ')).split(";"))).length == 0 || request.getProperty("usernames").equals("")) {
                                Enumeration<Object> keys = inheritances.keys();
                                while (keys.hasMoreElements()) {
                                    String key = keys.nextElement().toString();
                                    Vector inherit = (Vector)inheritances.get(key);
                                    int x = inherit.size() - 1;
                                    while (x >= 0) {
                                        if (inherit.elementAt(x).toString().equalsIgnoreCase(request.getProperty("inheritance_name"))) {
                                            inherit.removeElementAt(x);
                                        }
                                        --x;
                                    }
                                    if (inherit.size() != 0) continue;
                                    inheritances.remove(key);
                                }
                            } else {
                                int x = 0;
                                while (x < usernames.length) {
                                    Vector inherit = (Vector)inheritances.get(usernames[x]);
                                    if (inherit == null) {
                                        inherit = new Vector();
                                    }
                                    inherit.remove(request.getProperty("inheritance_name"));
                                    inheritances.put(usernames[x], inherit);
                                    ++x;
                                }
                            }
                            Common.updateObjectLogOnly("delete " + request.getProperty("usernames"), "users/" + request.getProperty("serverGroup") + "/inheritance/" + request.getProperty("inheritance_name"), log_summary);
                        } else {
                            inheritances = (Properties)Common.readXMLObjectError(new ByteArrayInputStream(Common.url_decode(request.getProperty("inheritance").replace('+', ' ')).getBytes("UTF8")));
                            if (!request.getProperty("old_username", "").equals("")) {
                                Enumeration<Object> keys = inheritances.keys();
                                while (keys.hasMoreElements()) {
                                    Vector parents = (Vector)inheritances.get(keys.nextElement().toString());
                                    if (!parents.contains(request.getProperty("old_username", ""))) continue;
                                    parents.remove(request.getProperty("old_username", ""));
                                    parents.add(request.getProperty("username", ""));
                                }
                            }
                            Properties inheritances_original = UserTools.getInheritance(request.getProperty("serverGroup"));
                            Common.updateObjectLog(inheritances, inheritances_original, "users/" + request.getProperty("serverGroup") + "/inheritance", false, log_summary);
                        }
                        if (inheritances == null) {
                            inheritances = new Properties();
                        }
                        UserTools.writeInheritance(request.getProperty("serverGroup"), inheritances, true, request);
                        break block100;
                    }
                    if (request.getProperty("xmlItem", "").equals("user")) {
                        if (!request.containsKey("usernames")) {
                            request.put("usernames", request.getProperty("username", ""));
                        }
                        String[] usernames = com.crushftp.client.Common.html_clean_usernames(Common.url_decode(request.getProperty("usernames").replace('+', ' ')).split(";"));
                        int x = 0;
                        while (x < usernames.length) {
                            String username = usernames[x].trim();
                            if (!username.equals("")) {
                                if (request.getProperty("data_action").equals("delete")) {
                                    Properties user;
                                    if (request.getProperty("expire_user", "false").equals("true") && (user = UserTools.ut.getUser(request.getProperty("serverGroup"), username, true)) != null) {
                                        UserTools.expireUserVFSTask(user, request.getProperty("serverGroup"), username);
                                    }
                                    Common.updateObjectLogOnly("delete ", "users/" + request.getProperty("serverGroup") + "/" + username, log_summary);
                                    UserTools.deleteUser(request.getProperty("serverGroup"), username);
                                    Vector user_list = new Vector();
                                    UserTools.refreshUserList(request.getProperty("serverGroup"), user_list);
                                    int xx = 0;
                                    while (xx < user_list.size()) {
                                        File_S f;
                                        String newUser = com.crushftp.client.Common.dots(user_list.elementAt(xx).toString());
                                        if (newUser.toUpperCase().endsWith(".SHARED") && (f = new File_S(String.valueOf(System.getProperty("crushftp.users")) + "/" + request.getProperty("serverGroup") + "/" + newUser + "/VFS/Shares/" + username)).exists()) {
                                            Common.recurseDelete(f.getCanonicalPath(), false);
                                            f = new File_S(String.valueOf(System.getProperty("crushftp.users")) + "/" + request.getProperty("serverGroup") + "/" + newUser + "/VFS/Shares/");
                                            if (f.listFiles() == null || f.listFiles().length == 0) {
                                                UserTools.deleteUser(request.getProperty("serverGroup"), newUser);
                                            }
                                        }
                                        ++xx;
                                    }
                                    File_U[] accounts = (File_U[])new File_U(String.valueOf(ServerStatus.SG("temp_accounts_path")) + "accounts/").listFiles();
                                    int xx2 = 0;
                                    while (accounts != null && xx2 < accounts.length) {
                                        try {
                                            if (accounts[xx2].getName().indexOf(",,") >= 0 && accounts[xx2].isDirectory()) {
                                                String[] tokens = accounts[xx2].getName().split(",,");
                                                Properties pp = new Properties();
                                                int loop = 0;
                                                while (loop < tokens.length) {
                                                    pp.put(tokens[loop].substring(0, tokens[loop].indexOf("=")).toUpperCase(), tokens[loop].substring(tokens[loop].indexOf("=") + 1));
                                                    ++loop;
                                                }
                                                if (username.equalsIgnoreCase(pp.getProperty("M"))) {
                                                    Common.recurseDelete_U(String.valueOf(ServerStatus.SG("temp_accounts_path")) + "storage/" + pp.getProperty("U") + pp.getProperty("P"), false);
                                                    Common.recurseDelete_U(accounts[xx2].getCanonicalPath(), false);
                                                }
                                            }
                                        }
                                        catch (Exception e) {
                                            Log.log("HTTP_SERVER", 1, e);
                                        }
                                        ++xx2;
                                    }
                                } else {
                                    Properties old_user;
                                    Properties template;
                                    Properties default_user = UserTools.ut.getUser(request.getProperty("serverGroup"), "default", false);
                                    Properties new_user = new Properties();
                                    if (!request.getProperty("user", "").equals("")) {
                                        new_user = (Properties)Common.readXMLObject(new ByteArrayInputStream(Common.url_decode(request.getProperty("user").replace('+', ' ')).getBytes("UTF8")));
                                    }
                                    new_user.put("userVersion", "6");
                                    if (new_user.containsKey("password")) {
                                        Properties old_user2;
                                        if (!new_user.getProperty("password").equals("SHA3:XXXXXXXXXXXXXXXXXXXX")) {
                                            if (thisSession != null) {
                                                Log.log("SERVER", 0, String.valueOf(username) + " password changed by admin (" + thisSession.uiSG("user_name") + ").");
                                            }
                                            String pass = new_user.getProperty("password", "");
                                            if (new_user.getProperty("salt", "").equals("") && default_user.getProperty("salt", "").equalsIgnoreCase("random")) {
                                                new_user.put("salt", Common.makeBoundary(8));
                                            }
                                            if (!(pass.startsWith("SHA:") || pass.startsWith("SHA512:") || pass.startsWith("SHA256:") || pass.startsWith("SHA3:") || pass.startsWith("MD5:") || pass.startsWith("CRYPT3:") || pass.startsWith("BCRYPT:") || pass.startsWith("MD5CRYPT:") || pass.startsWith("PBKDF2SHA256:") || pass.startsWith("SHA512CRYPT:") || pass.startsWith("ARGOND:"))) {
                                                pass = ServerStatus.thisObj.common_code.encode_pass(pass, ServerStatus.SG("password_encryption"), new_user.getProperty("salt", ""));
                                                new_user.put("password", pass);
                                            } else {
                                                new_user.put("password", pass);
                                            }
                                        } else if (!request.getProperty("old_username", "").equals("") && (old_user2 = UserTools.ut.getUser(request.getProperty("serverGroup"), Common.url_decode(request.getProperty("old_username", "")).replace('+', ' '), false)) != null) {
                                            new_user.put("password", old_user2.getProperty("password", ""));
                                        }
                                    }
                                    if (!request.getProperty("old_username", "").equals("")) {
                                        try {
                                            Vector user_list = new Vector();
                                            UserTools.refreshUserList(request.getProperty("serverGroup"), user_list);
                                            if (user_list.contains(String.valueOf(request.getProperty("old_username", "")) + ".SHARED")) {
                                                Properties shared_user = UserTools.ut.getUser(request.getProperty("serverGroup"), String.valueOf(request.getProperty("old_username", "")) + ".SHARED", false);
                                                shared_user.put("user_name", username);
                                                if (shared_user.containsKey("username")) {
                                                    shared_user.put("username", username);
                                                }
                                                if (thisSession != null && thisSession.user != null) {
                                                    shared_user.put("updated_by_username", thisSession.user.getProperty("user_name"));
                                                }
                                                new File_S(String.valueOf(System.getProperty("crushftp.users")) + "/" + request.getProperty("serverGroup") + "/" + request.getProperty("old_username", "") + ".SHARED").renameTo(new File_S(String.valueOf(System.getProperty("crushftp.users")) + "/" + request.getProperty("serverGroup") + "/" + username + ".SHARED"));
                                                UserTools.writeUser(request.getProperty("serverGroup"), String.valueOf(username) + ".SHARED", shared_user);
                                            }
                                        }
                                        catch (Exception e) {
                                            Log.log("HTTP_SERVER", 1, e);
                                        }
                                        File_U[] accounts = (File_U[])new File_U(String.valueOf(ServerStatus.SG("temp_accounts_path")) + "accounts/").listFiles();
                                        int xx = 0;
                                        while (accounts != null && xx < accounts.length) {
                                            try {
                                                if (accounts[xx].getName().indexOf(",,") >= 0 && accounts[xx].isDirectory()) {
                                                    String[] tokens = accounts[xx].getName().split(",,");
                                                    Properties pp = new Properties();
                                                    int loop = 0;
                                                    while (loop < tokens.length) {
                                                        pp.put(tokens[loop].substring(0, tokens[loop].indexOf("=")).toUpperCase(), tokens[loop].substring(tokens[loop].indexOf("=") + 1));
                                                        ++loop;
                                                    }
                                                    if (request.getProperty("old_username", "").equalsIgnoreCase(pp.getProperty("M"))) {
                                                        String folderName = "u=" + pp.getProperty("U") + ",,p=" + pp.getProperty("P") + ",,m=" + username + ",,t=" + pp.getProperty("T") + ",,ex=" + pp.getProperty("EX");
                                                        Properties info = (Properties)Common.readXMLObject_U(String.valueOf(accounts[xx].getPath()) + "/INFO.XML");
                                                        info.put("master", username);
                                                        info.put("account_path", String.valueOf(ServerStatus.SG("temp_accounts_path")) + "accounts/" + folderName + "/");
                                                        accounts[xx].renameTo(new File_U(String.valueOf(ServerStatus.SG("temp_accounts_path")) + "accounts/" + folderName + "/"));
                                                        Common.writeXMLObject_U(String.valueOf(ServerStatus.SG("temp_accounts_path")) + "accounts/" + folderName + "/" + "INFO.XML", info, "INFO");
                                                    }
                                                }
                                            }
                                            catch (Exception e) {
                                                Log.log("HTTP_SERVER", 1, e);
                                            }
                                            ++xx;
                                        }
                                    }
                                    if (thisSession != null) {
                                        new_user.put("updated_by_username", thisSession.uiSG("user_name"));
                                        new_user.put("updated_by_email", thisSession.uiSG("user_email").equals("") ? thisSession.user.getProperty("email", "") : thisSession.uiSG("user_email"));
                                    }
                                    new_user.put("updated_time", String.valueOf(System.currentTimeMillis()));
                                    new_user.remove("created_by_username");
                                    new_user.remove("created_by_email");
                                    if (request.getProperty("data_action").equals("update") || request.getProperty("data_action").equals("update_vfs") || request.getProperty("data_action").equals("update_vfs_remove")) {
                                        Properties user = UserTools.ut.getUser(request.getProperty("serverGroup"), username, false);
                                        if (user != null && new_user.getProperty("password") != null && new_user.getProperty("password").equals("SHA3:XXXXXXXXXXXXXXXXXXXX")) {
                                            new_user.put("password", user.getProperty("password", ""));
                                        }
                                        if (user != null && !user.getProperty("created_by_username", "").equals("")) {
                                            new_user.put("created_by_username", user.getProperty("created_by_username", ""));
                                        }
                                        if (user != null && !user.getProperty("created_by_email", "").equals("")) {
                                            new_user.put("created_by_email", user.getProperty("created_by_email", ""));
                                        }
                                        Common.updateObjectLog(new_user, user, "users/" + request.getProperty("serverGroup") + "/" + username, true, log_summary);
                                        new_user = user;
                                    }
                                    if (request.getProperty("data_action").equals("replace")) {
                                        Properties user = UserTools.ut.getUser(request.getProperty("serverGroup"), username, false);
                                        if (user != null && new_user.getProperty("password").equals("SHA3:XXXXXXXXXXXXXXXXXXXX")) {
                                            new_user.put("password", user.getProperty("password", ""));
                                        }
                                        if (user != null && !user.getProperty("created_by_username", "").equals("")) {
                                            new_user.put("created_by_username", user.getProperty("created_by_username", ""));
                                        }
                                        if (user != null && !user.getProperty("created_by_email", "").equals("")) {
                                            new_user.put("created_by_email", user.getProperty("created_by_email", ""));
                                        }
                                        if (user != null) {
                                            Common.updateObjectLog(new_user, user, "users/" + request.getProperty("serverGroup") + "/" + username, false, log_summary);
                                        }
                                    }
                                    if (request.getProperty("data_action").equals("new")) {
                                        if (new_user.getProperty("password").equals("SHA3:XXXXXXXXXXXXXXXXXXXX")) {
                                            new_user.put("password", "");
                                        }
                                        Common.updateObjectLogOnly("new ", "users/" + request.getProperty("serverGroup") + "/" + username, log_summary);
                                        Common.updateObjectLog(new_user, new Properties(), "users/" + request.getProperty("serverGroup") + "/" + username, false, new StringBuffer());
                                        new_user.put("created_time", String.valueOf(System.currentTimeMillis()));
                                        if (thisSession != null && thisSession.uiSG("user_name") != null) {
                                            new_user.put("created_by_username", thisSession.uiSG("user_name"));
                                        }
                                        if (thisSession != null && thisSession.uiSG("user_name") != null) {
                                            new_user.put("created_by_email", thisSession.uiSG("user_email").equals("") ? thisSession.user.getProperty("email", "") : thisSession.uiSG("user_email"));
                                        }
                                    } else if (!new_user.containsKey("created_time")) {
                                        new_user.put("created_time", String.valueOf(System.currentTimeMillis()));
                                        if (thisSession != null) {
                                            new_user.put("created_by_username", thisSession.uiSG("user_name"));
                                            new_user.put("created_by_email", thisSession.uiSG("user_email").equals("") ? thisSession.user.getProperty("email", "") : thisSession.uiSG("user_email"));
                                        }
                                    }
                                    if (request.getProperty("data_action").equals("update_remove")) {
                                        Properties user = UserTools.ut.getUser(request.getProperty("serverGroup"), username, false);
                                        if (user != null && new_user.getProperty("password") != null && new_user.getProperty("password").equals("SHA3:XXXXXXXXXXXXXXXXXXXX")) {
                                            new_user.put("password", user.getProperty("password", ""));
                                        }
                                        if (user != null && !user.getProperty("created_by_username", "").equals("")) {
                                            new_user.put("created_by_username", user.getProperty("created_by_username", ""));
                                        }
                                        if (user != null && !user.getProperty("created_by_email", "").equals("")) {
                                            new_user.put("created_by_email", user.getProperty("created_by_email", ""));
                                        }
                                        Common.updateObjectLogOnly(request.getProperty("update_remove_key", ""), "users/" + request.getProperty("serverGroup") + "/" + username, log_summary);
                                        String[] keys = request.getProperty("update_remove_key", "").split(";");
                                        int xx = 0;
                                        while (xx < keys.length) {
                                            user.remove(keys[xx]);
                                            ++xx;
                                        }
                                        new_user = user;
                                    }
                                    UserTools.writeUser(request.getProperty("serverGroup"), username, new_user, true, true, request);
                                    UserTools.ut.getUser(request.getProperty("serverGroup"), username, true);
                                    Log.log("HTTP_SERVER", 1, "Updated user :" + username);
                                    if (request.containsKey("vfs_items") || request.containsKey("permissions")) {
                                        UserTools.writeVFS(request.getProperty("serverGroup"), username, (Properties)AdminControls.processVFSSubmission((Properties)request, (String)username, (String)site, (SessionCrush)thisSession, (boolean)true, (StringBuffer)log_summary).homes.elementAt(0), true, request);
                                        Log.log("HTTP_SERVER", 1, "Updated user vfs :" + username);
                                    }
                                    if ((template = Common.get_email_template("Change Email")) != null && (old_user = UserTools.ut.getUser(request.getProperty("serverGroup"), username, false)) != null && new_user != null && !old_user.getProperty("email", "").equals(new_user.getProperty("email", ""))) {
                                        String body = template.getProperty("emailBody");
                                        body = Common.replace_str(body, "{old_email}", old_user.getProperty("email"));
                                        body = Common.replace_str(body, "{new_email}", new_user.getProperty("email"));
                                        body = Common.replace_str(body, "{summary}", log_summary.toString());
                                        String subject = template.getProperty("emailSubject");
                                        subject = Common.replace_str(subject, "{old_email}", old_user.getProperty("email"));
                                        subject = Common.replace_str(subject, "{new_email}", new_user.getProperty("email"));
                                        subject = Common.replace_str(subject, "{summary}", log_summary.toString());
                                        new_user.put("username", username);
                                        new_user.put("user_name", username);
                                        body = ServerStatus.change_vars_to_values_static(body, new_user, new_user, null);
                                        subject = ServerStatus.change_vars_to_values_static(subject, new_user, new_user, null);
                                        String cc = ServerStatus.change_vars_to_values_static(template.getProperty("emailCC"), new_user, new_user, null);
                                        String bcc = ServerStatus.change_vars_to_values_static(template.getProperty("emailBCC"), new_user, new_user, null);
                                        Properties email_info = new Properties();
                                        email_info.put("server", ServerStatus.SG("smtp_server"));
                                        email_info.put("user", ServerStatus.SG("smtp_user"));
                                        email_info.put("pass", ServerStatus.SG("smtp_pass"));
                                        email_info.put("ssl", ServerStatus.SG("smtp_ssl"));
                                        email_info.put("html", ServerStatus.SG("smtp_html"));
                                        email_info.put("from", template.getProperty("emailFrom"));
                                        email_info.put("reply_to", template.getProperty("emailReplyTo"));
                                        email_info.put("to", String.valueOf(new_user.getProperty("email")) + "," + old_user.getProperty("email"));
                                        email_info.put("cc", cc);
                                        email_info.put("bcc", bcc);
                                        email_info.put("subject", subject);
                                        email_info.put("body", body);
                                        ServerStatus.thisObj.sendEmail(email_info);
                                    }
                                    if ((template = Common.get_email_template("Change User Email")) != null) {
                                        String body = template.getProperty("emailBody");
                                        body = Common.replace_str(body, "{new_email}", new_user.getProperty("email"));
                                        body = Common.replace_str(body, "{summary}", log_summary.toString());
                                        String subject = template.getProperty("emailSubject");
                                        subject = Common.replace_str(subject, "{new_email}", new_user.getProperty("email"));
                                        subject = Common.replace_str(subject, "{summary}", log_summary.toString());
                                        new_user.put("username", username);
                                        new_user.put("user_name", username);
                                        body = ServerStatus.change_vars_to_values_static(body, new_user, new_user, null);
                                        subject = ServerStatus.change_vars_to_values_static(subject, new_user, new_user, null);
                                        String cc = ServerStatus.change_vars_to_values_static(template.getProperty("emailCC"), new_user, new_user, null);
                                        String bcc = ServerStatus.change_vars_to_values_static(template.getProperty("emailBCC"), new_user, new_user, null);
                                        Properties email_info = new Properties();
                                        email_info.put("server", ServerStatus.SG("smtp_server"));
                                        email_info.put("user", ServerStatus.SG("smtp_user"));
                                        email_info.put("pass", ServerStatus.SG("smtp_pass"));
                                        email_info.put("ssl", ServerStatus.SG("smtp_ssl"));
                                        email_info.put("html", ServerStatus.SG("smtp_html"));
                                        email_info.put("from", template.getProperty("emailFrom"));
                                        email_info.put("reply_to", template.getProperty("emailReplyTo"));
                                        email_info.put("to", cc);
                                        email_info.put("cc", cc);
                                        email_info.put("bcc", bcc);
                                        email_info.put("subject", subject);
                                        email_info.put("body", body);
                                        ServerStatus.thisObj.sendEmail(email_info);
                                    }
                                }
                                UserTools.ut.forceMemoryReload(username);
                            }
                            ++x;
                        }
                        break block100;
                    }
                    status = "Unknown xmlItem:" + request.getProperty("xmlitem");
                }
                catch (Exception e) {
                    Log.log("HTTP_SERVER", 1, e);
                    status = "FAILURE:" + e.toString();
                }
            }
            catch (Exception e) {
                Log.log("HTTP_SERVER", 1, e);
                status = e.toString();
            }
        }
        return status;
    }

    public static Properties getUserList(Properties request, String site, SessionCrush thisSession) {
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 20);
                return (Properties)p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return null;
            }
        }
        if (site.indexOf("(CONNECT)") < 0) {
            request.put("serverGroup", thisSession.server_item.getProperty("linkedServer"));
        }
        Vector list = new Vector();
        UserTools.refreshUserList(request.getProperty("serverGroup"), list);
        if (site.indexOf("(CONNECT)") < 0 && site.indexOf("(USER_VIEW)") < 0 && site.indexOf("(USER_EDIT)") < 0) {
            list = AdminControls.getLimitedAdminUserList(request, thisSession, list);
        }
        Properties user_list = new Properties();
        user_list.put("user_list", list);
        return user_list;
    }

    private static Vector getLimitedAdminUserList(Properties request, SessionCrush thisSession, Vector list) {
        String groupName = thisSession.getAdminGroupName(request);
        Properties info = UserTools.getAllowedUsers(groupName, request.getProperty("serverGroup"), list);
        Properties info2 = UserTools.getAllowedUsers("pendingSelfRegistration", request.getProperty("serverGroup"), list);
        list = (Vector)info.get("list");
        Vector list2 = (Vector)info2.get("list");
        int x = 0;
        while (x < list2.size()) {
            String tempUsername = list2.elementAt(x).toString();
            if (list.indexOf(tempUsername) < 0) {
                list.addElement(tempUsername);
            }
            ++x;
        }
        thisSession.put("user_admin_info", info);
        return list;
    }

    static Object getUserXML(Properties request, String site, SessionCrush session) {
        try {
            if (!request.getProperty("instance", "").equals("")) {
                String id = Common.makeBoundary();
                String instance = request.remove("instance").toString();
                DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
                Properties p = DMZServerCommon.getResponse(id, 20);
                return p.get("data");
            }
            Properties obj = null;
            if (request.getProperty("xmlItem", "").equals("group")) {
                if (site.indexOf("(USER_ADMIN)") >= 0) {
                    String groupName = session.getProperty("admin_group_name", "");
                    Properties obj2 = UserTools.getGroups(request.getProperty("serverGroup"));
                    obj = new Properties();
                    Enumeration<Object> keys = obj2.keys();
                    while (keys.hasMoreElements()) {
                        String key = keys.nextElement().toString();
                        if (!key.toUpperCase().startsWith(String.valueOf(groupName.toUpperCase()) + "_")) continue;
                        obj.put(key.substring((String.valueOf(groupName.toUpperCase()) + "_").length()), obj2.get(key));
                    }
                } else if (site.indexOf("(CONNECT)") >= 0 || site.indexOf("(REPORT_EDIT)") >= 0) {
                    obj = UserTools.getGroups(request.getProperty("serverGroup"));
                } else if (site.indexOf("(USER_VIEW)") >= 0 || site.indexOf("(USER_EDIT)") >= 0) {
                    obj = UserTools.getGroups(request.getProperty("serverGroup"));
                }
            } else if (request.getProperty("xmlItem", "").equals("inheritance")) {
                if (site.indexOf("(CONNECT)") >= 0) {
                    obj = UserTools.getInheritance(request.getProperty("serverGroup"));
                } else if (site.indexOf("(USER_ADMIN)") >= 0) {
                    Properties info = (Properties)session.get("user_admin_info");
                    obj = (Properties)info.get("inheritance");
                } else if (site.indexOf("(USER_VIEW)") >= 0 || site.indexOf("(USER_EDIT)") >= 0) {
                    obj = UserTools.getInheritance(request.getProperty("serverGroup"));
                }
            }
            Properties result = new Properties();
            result.put("result_item", obj);
            return result;
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            return e.toString();
        }
    }

    public static Properties getUserXMLListing(Properties request, String site, SessionCrush thisSession) throws Exception {
        Vector listing;
        Properties item;
        VRL vrl;
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            Properties p = DMZServerCommon.getResponse(id, 20);
            return (Properties)p.get("data");
        }
        if (site.indexOf("(CONNECT)") < 0) {
            request.put("serverGroup", thisSession.server_item.getProperty("linkedServer"));
        }
        String username = Common.url_decode(request.getProperty("username", "").replace('+', ' '));
        String parentUser = null;
        String path = Common.url_decode(request.getProperty("path", "").replace('+', ' '));
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        path = com.crushftp.client.Common.dots(path);
        VFS tempVFS = AdminControls.processVFSSubmission(request, username, site, thisSession, false, new StringBuffer());
        if (site.indexOf("(CONNECT)") < 0 && site.indexOf("(USER_VIEW)") < 0 && site.indexOf("(USER_EDIT)") < 0 && site.indexOf("(JOB_EDIT)") < 0 && (vrl = new VRL((item = tempVFS.get_item(path)).getProperty("url"))).getProtocol().equalsIgnoreCase("file")) {
            String groupName;
            Properties info = (Properties)thisSession.get("user_admin_info");
            Vector list = (Vector)info.get("list");
            if (request.getProperty("serverGroup_original", "").equals("extra_vfs") ? list.indexOf(username.substring(0, username.lastIndexOf("~"))) < 0 : list.indexOf(username) < 0) {
                throw new Exception("Username " + username + " not found.");
            }
            parentUser = groupName = thisSession.getAdminGroupName(request);
        }
        if (request.getProperty("command", "").equals("testVFS")) {
            Properties parent_item = tempVFS.get_item_parent(path);
            if (parent_item == null) {
                throw new Exception("Invalid path reference:" + path);
            }
            vrl = new VRL(parent_item.getProperty("url"));
            if (vrl.getProtocol().equalsIgnoreCase("virtual")) {
                throw new Exception("VFS item not found in user profile:" + path);
            }
        }
        if ((listing = UserTools.ut.get_virtual_list_fake(tempVFS, path, request.getProperty("serverGroup"), parentUser)).size() > 0 && listing.elementAt(0) instanceof String) {
            Properties p = new Properties();
            p.put("error", listing.elementAt(0));
            return p;
        }
        return AdminControls.getListingInfo(listing, path);
    }

    public static Properties getAdminXMLListing(Properties request, SessionCrush thisSession, String site) throws Exception {
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            Properties p = DMZServerCommon.getResponse(id, 20);
            return (Properties)p.get("data");
        }
        String path = Common.url_decode(request.getProperty("path", "").replace('+', ' '));
        if (path.startsWith("///") && !path.startsWith("////")) {
            path = "/" + path;
        }
        if (path.startsWith("~")) {
            path = Common.replace_str(path, "~", System.getProperty("user.home"));
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        try {
            File_B[] items = new File_B[]{};
            if (request.getProperty("file_mode", "").equals("server")) {
                if (!new File_S(path).exists() && Common.machine_is_x()) {
                    path = "/Volumes" + path;
                }
                path = com.crushftp.client.Common.dots(path);
                items = AdminControls.getFileItems(path);
            } else {
                if (!new File_U(path).exists() && Common.machine_is_x()) {
                    path = "/Volumes" + path;
                }
                path = com.crushftp.client.Common.dots(path);
                items = AdminControls.getFileItems_U(path);
            }
            Vector<Properties> listing = new Vector<Properties>();
            int x = 0;
            while (x < items.length) {
                Properties p = new Properties();
                p.put("name", items[x].getName());
                p.put("path", Common.all_but_last(items[x].getPath()));
                p.put("type", items[x].isDirectory() ? "DIR" : "FILE");
                p.put("size", String.valueOf(items[x].length()));
                if (Common.machine_is_windows() && path.equals("/")) {
                    p.put("name", items[x].getPath().substring(0, 2));
                    p.put("path", "/");
                }
                p.put("boot", String.valueOf(path.equals("/") && Common.machine_is_x() && !items[x].getCanonicalPath().startsWith("/Volumes/")));
                p.put("privs", "(read)(view)");
                p.put("owner", "user");
                p.put("group", "group");
                p.put("permissionsNum", "777");
                p.put("keywords", "");
                p.put("num_items", "1");
                p.put("preview", "");
                p.put("owner", "");
                p.put("owner", "");
                p.put("root_dir", p.getProperty("path"));
                p.put("url", String.valueOf(items[x].toURI().toURL().toExternalForm()));
                listing.addElement(p);
                ++x;
            }
            return AdminControls.getListingInfo(listing, path);
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
            return new Properties();
        }
    }

    public static Properties searchUserSettings(Properties request, String site, SessionCrush thisSession) {
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 20);
                return (Properties)p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return null;
            }
        }
        if (!ServerStatus.BG("v10_beta")) {
            Properties user_settings_list = new Properties();
            user_settings_list.put("user_settings_list", new Vector());
            return user_settings_list;
        }
        if (site.indexOf("(CONNECT)") < 0) {
            request.put("serverGroup", thisSession.server_item.getProperty("linkedServer"));
        }
        Vector list = new Vector();
        UserTools.refreshUserList(request.getProperty("serverGroup"), list);
        if (site.indexOf("(CONNECT)") < 0 && site.indexOf("(USER_VIEW)") < 0 && site.indexOf("(USER_EDIT)") < 0) {
            list = AdminControls.getLimitedAdminUserList(request, thisSession, list);
        }
        Vector<Properties> result_list = new Vector<Properties>();
        Properties inheritance = UserTools.getInheritance(request.getProperty("serverGroup"));
        int x = 0;
        while (x < list.size()) {
            String username = list.elementAt(x).toString();
            try {
                Properties found_settings = new Properties();
                found_settings.put("username", username);
                if (inheritance.containsKey(username)) {
                    String parents = "";
                    Vector elements = (Vector)inheritance.get(username);
                    int xx = 0;
                    while (xx < elements.size()) {
                        parents = String.valueOf(parents) + elements.get(xx) + ",";
                        ++xx;
                    }
                    if (parents.endsWith(",")) {
                        parents = parents.substring(0, parents.length() - 1);
                    }
                    if (parents.contains(request.getProperty("search_text"))) {
                        found_settings.put("inheritance", parents);
                    }
                }
                Properties user = UserTools.ut.getUser(request.getProperty("serverGroup"), username, false);
                Enumeration<Object> keys = user.keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement().toString();
                    if (key.equals("user_name") || key.equals("username") || !user.getProperty(key, "").contains(request.getProperty("search_text"))) continue;
                    found_settings.put(key, user.getProperty(key, ""));
                }
                if (found_settings.size() != 0 && found_settings.size() > 1) {
                    result_list.add(found_settings);
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
            }
            ++x;
        }
        Properties user_settings_list = new Properties();
        user_settings_list.put("user_settings_list", result_list);
        return user_settings_list;
    }

    private static File_B[] getFileItems(String path) {
        File_B[] items = null;
        if (path.equals("/") && Common.machine_is_x()) {
            try {
                File_B[] other_volumes = Common.convert_files_to_files_both(new File_S("/Volumes/").listFiles());
                if (other_volumes == null) {
                    other_volumes = new File_B[]{new File_B(new File_S("/"))};
                }
                items = new File_B[other_volumes.length];
                int x = 0;
                while (x < other_volumes.length) {
                    items[x] = other_volumes[x];
                    ++x;
                }
            }
            catch (Exception exception) {}
        } else {
            items = path.equals("/") && Common.machine_is_windows() ? Common.convert_files_to_files_both(File_S.listRoots()) : Common.convert_files_to_files_both(new File_S(path).listFiles());
        }
        return items;
    }

    private static File_B[] getFileItems_U(String path) {
        File_B[] items = null;
        if (path.equals("/") && Common.machine_is_x()) {
            try {
                File_B[] other_volumes = Common.convert_files_to_files_both(new File_U("/Volumes/").listFiles());
                if (other_volumes == null) {
                    other_volumes = new File_B[]{new File_B(new File_U("/"))};
                }
                items = new File_B[other_volumes.length];
                int x = 0;
                while (x < other_volumes.length) {
                    items[x] = other_volumes[x];
                    ++x;
                }
            }
            catch (Exception exception) {}
        } else {
            items = path.equals("/") && Common.machine_is_windows() ? Common.convert_files_to_files_both(File_U.listRoots()) : Common.convert_files_to_files_both(new File_U(path).listFiles());
        }
        return items;
    }

    public static Properties getLog(Properties request, String site) throws IOException {
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 20);
                return (Properties)p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return null;
            }
        }
        if (site.indexOf("(CONNECT)") < 0 && !request.getProperty("log_file", "").toUpperCase().endsWith(".LOG") && !request.getProperty("log_file", "").equals("")) {
            return null;
        }
        if (site.indexOf("(LOG_ACCESS)") < 0 && (site.indexOf("(JOB_LIST_HISTORY)") >= 0 || site.indexOf("(JOB_VIEW)") >= 0 || site.indexOf("(JOB_RUN)") >= 0)) {
            String job_log_path = String.valueOf(new File_S(Common.all_but_last(System.getProperty("crushftp.log_location", "./"))).getCanonicalFile().getPath().replace('\\', '/')) + "/logs/jobs/";
            String user_log = Common.dots(request.getProperty("log_file", ""));
            if (!request.getProperty("log_file", "").equals("") && !user_log.startsWith(job_log_path)) {
                Properties log = new Properties();
                log.put("log_start_date", "" + new Date());
                log.put("log_end_date", "" + new Date());
                log.put("log_segment", "********************************************************************************************************************\r\nAccess denied for log:" + user_log + "\r\n********************************************************************************************************************\r\n");
                log.put("log_start", "0");
                log.put("log_end", "0");
                log.put("log_max", "0");
                log.put("log_data", "");
                return log;
            }
        }
        return LoggingProviderDisk.getLogSegmentStatic(Long.parseLong(request.getProperty("segment_start", "0")), Long.parseLong(request.getProperty("segment_len", "32768")), request.getProperty("log_file", ""));
    }

    static String buildXML(Object o, String key, String status) {
        String xml = "";
        if (o instanceof String) {
            status = o.toString();
            o = null;
        }
        try {
            if (o != null) {
                Common cfr_ignored_0 = ServerStatus.thisObj.common_code;
                xml = Common.getXMLString(o, key, null);
                if (xml.startsWith("<?")) {
                    xml = xml.substring(xml.indexOf("?>") + 2).trim();
                }
            }
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
        }
        String response_type = "";
        if (o == null || o instanceof String) {
            response_type = "text";
        }
        if (o instanceof Properties) {
            response_type = "properties";
        } else if (o instanceof Vector) {
            response_type = "vector";
        }
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
        response = String.valueOf(response) + "<result><response_status>" + status + "</response_status><response_type>" + response_type + "</response_type><response_data>" + xml + "</response_data></result>";
        return response;
    }

    public static boolean checkRole(String command, String site) {
        return AdminControls.checkRole(command, site, "127.0.0.1");
    }

    public static boolean checkRole(String command, String site, String user_ip) {
        boolean allowed = false;
        String[] admin_ips = ("127.0.0.1," + ServerStatus.SG("admin_ips")).split(",");
        int x = 0;
        while (x < admin_ips.length && !allowed) {
            if (!admin_ips[x].trim().equals("") && com.crushftp.client.Common.do_search(admin_ips[x].trim(), user_ip, false, 0)) {
                allowed = true;
            }
            ++x;
        }
        if (!allowed) {
            return false;
        }
        if (site.indexOf("(CONNECT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("getServerItem") && (site.indexOf("(USER_ADMIN)") >= 0 || site.indexOf("(USER_VIEW)") >= 0 || site.indexOf("(USER_EDIT)") >= 0 || site.indexOf("(JOB_VIEW)") >= 0 || site.indexOf("(JOB_EDIT)") >= 0 || site.indexOf("(JOB_RUN)") >= 0 || site.indexOf("(SHARE_VIEW)") >= 0 || site.indexOf("(PREF_VIEW)") >= 0 || site.indexOf("(SERVER_VIEW)") >= 0 || site.indexOf("(SERVER_EDIT)") >= 0 || site.indexOf("(UPDATE_RUN)") >= 0 || site.indexOf("(REPORT_VIEW)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("getJob") && (site.indexOf("(JOB_EDIT)") >= 0 || site.indexOf("(JOB_VIEW)") >= 0 || site.indexOf("(JOB_RUN)") >= 0 || site.indexOf("(JOB_LIST)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("renameJob") && site.indexOf("(JOB_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("removeJob") && site.indexOf("(JOB_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("makedirJob") && site.indexOf("(JOB_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("renamedirJob") && site.indexOf("(JOB_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("deletedirJob") && site.indexOf("(JOB_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("changeJobStatus") && (site.indexOf("(JOB_EDIT)") >= 0 || site.indexOf("(JOB_RUN)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("addJob") && site.indexOf("(JOB_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("getStatHistory") && site.indexOf("(SERVER_VIEW)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("getJobsSummary") && (site.indexOf("(JOB_EDIT)") >= 0 || site.indexOf("(JOB_VIEW)") >= 0 || site.indexOf("(JOB_LIST_HISTORY)") >= 0 || site.indexOf("(JOB_MONITOR)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("getJobsSettings") && (site.indexOf("(JOB_EDIT)") >= 0 || site.indexOf("(JOB_VIEW)") >= 0 || site.indexOf("(JOB_LIST_HISTORY)") >= 0 || site.indexOf("(JOB_MONITOR)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("getJobInfo") && (site.indexOf("(JOB_EDIT)") >= 0 || site.indexOf("(JOB_VIEW)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("getJobInfo") && site.indexOf("(JOB_MONITOR)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("getSessionList") && site.indexOf("(SERVER_VIEW)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("getLog") && (site.indexOf("(LOG_ACCESS)") >= 0 || site.indexOf("(JOB_LIST_HISTORY)") >= 0 || site.indexOf("(JOB_VIEW)") >= 0 || site.indexOf("(JOB_RUN)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("getServerRoots") && (site.indexOf("(JOB_EDIT)") >= 0 || site.indexOf("(JOB_VIEW)") >= 0 || site.indexOf("(JOB_LIST_HISTORY)") >= 0 || site.indexOf("(JOB_MONITOR)") >= 0 || site.indexOf("(JOB_RUN)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("getDashboardItems") && (site.indexOf("(SERVER_VIEW)") >= 0 || site.indexOf("(SERVER_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("getDashboardHistory") && (site.indexOf("(SERVER_VIEW)") >= 0 || site.indexOf("(SERVER_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("getServerInfoItems") && (site.indexOf("(USER_ADMIN)") >= 0 || site.indexOf("(SERVER_VIEW)") >= 0 || site.indexOf("(SERVER_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("getServerSettingItems") && (site.indexOf("(USER_EDIT)") >= 0 || site.indexOf("(SERVER_VIEW)") >= 0 || site.indexOf("(SERVER_EDIT)") >= 0 || site.indexOf("(USER_ADMIN)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("setServerItem") && site.indexOf("(PREF_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("getUser") && (site.indexOf("(USER_ADMIN)") >= 0 || site.indexOf("(USER_EDIT)") >= 0 || site.indexOf("(USER_VIEW)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("getUserVersions") && (site.indexOf("(USER_ADMIN)") >= 0 || site.indexOf("(USER_EDIT)") >= 0 || site.indexOf("(USER_VIEW)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("getDeletedUsers") && (site.indexOf("(USER_EDIT)") >= 0 || site.indexOf("(USER_VIEW)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("setUserItem") && site.indexOf("(USER_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("refreshUser") && (site.indexOf("(USER_ADMIN)") >= 0 || site.indexOf("(USER_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("resetLdapCaches") && (site.indexOf("(SERVER_EDIT)") >= 0 || site.indexOf("(PREF_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("getUserXMLListing") && (site.indexOf("(USER_ADMIN)") >= 0 || site.indexOf("(USER_EDIT)") >= 0 || site.indexOf("(USER_VIEW)") >= 0 || site.indexOf("(JOB_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("getUserList") && (site.indexOf("(USER_ADMIN)") >= 0 || site.indexOf("(USER_EDIT)") >= 0 || site.indexOf("(USER_VIEW)") >= 0 || site.indexOf("(REPORT_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("getUserXML") && (site.indexOf("(USER_ADMIN)") >= 0 || site.indexOf("(USER_EDIT)") >= 0 || site.indexOf("(USER_VIEW)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("kick") && site.indexOf("(SERVER_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("ban") && (site.indexOf("(SERVER_EDIT)") >= 0 || site.indexOf("(PREF_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("getUserInfo") && (site.indexOf("(SERVER_EDIT)") >= 0 || site.indexOf("(SERVER_VIEW)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("startAllServers") && (site.indexOf("(SERVER_EDIT)") >= 0 || site.indexOf("(PREF_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("stopAllServers") && (site.indexOf("(SERVER_EDIT)") >= 0 || site.indexOf("(PREF_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("startServer") && (site.indexOf("(SERVER_EDIT)") >= 0 || site.indexOf("(PREF_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("stopServer") && (site.indexOf("(SERVER_EDIT)") >= 0 || site.indexOf("(PREF_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("restartServer") && (site.indexOf("(SERVER_EDIT)") >= 0 || site.indexOf("(PREF_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("allStats") && (site.indexOf("(SERVER_EDIT)") >= 0 || site.indexOf("(PREF_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("loginStats") && (site.indexOf("(SERVER_EDIT)") >= 0 || site.indexOf("(PREF_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("uploadDownloadStats") && (site.indexOf("(SERVER_EDIT)") >= 0 || site.indexOf("(PREF_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("transferStats") && (site.indexOf("(SERVER_EDIT)") >= 0 || site.indexOf("(PREF_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("serverStats") && (site.indexOf("(SERVER_EDIT)") >= 0 || site.indexOf("(PREF_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("dumpStack") && (site.indexOf("(SERVER_VIEW)") >= 0 || site.indexOf("(SERVER_EDIT)") >= 0 || site.indexOf("(PREF_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("system.gc") && (site.indexOf("(SERVER_VIEW)") >= 0 || site.indexOf("(SERVER_EDIT)") >= 0 || site.indexOf("(PREF_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("newFolder") && (site.indexOf("(PREF_EDIT)") >= 0 || site.indexOf("(USER_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("renameItem") && (site.indexOf("(PREF_EDIT)") >= 0 || site.indexOf("(USER_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("duplicateItem") && (site.indexOf("(PREF_EDIT)") >= 0 || site.indexOf("(USER_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("deleteItem") && (site.indexOf("(PREF_EDIT)") >= 0 || site.indexOf("(USER_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("updateNow") && site.indexOf("(UPDATE_RUN)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("getRestartShutdownIdleStatus") && site.indexOf("(UPDATE_RUN)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("restartIdle") && site.indexOf("(UPDATE_RUN)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("shutdownIdle") && site.indexOf("(UPDATE_RUN)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("stopLogins") && site.indexOf("(UPDATE_RUN)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("startLogins") && site.indexOf("(UPDATE_RUN)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("checkForUpdate") && site.indexOf("(UPDATE_RUN)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("updateWebNow") && site.indexOf("(UPDATE_RUN)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("updateNowProgress") && site.indexOf("(UPDATE_RUN)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("cancelUpdateProgress") && site.indexOf("(UPDATE_RUN)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("pgpGenerateKeyPair") && site.indexOf("(PREF_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("runReport") && (site.indexOf("(REPORT_RUN)") >= 0 || site.indexOf("(REPORT_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("testReportSchedule") && (site.indexOf("(REPORT_RUN)") >= 0 || site.indexOf("(REPORT_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("testJobSchedule") && (site.indexOf("(JOB_RUN)") >= 0 || site.indexOf("(JOB_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("testSMTP") && site.indexOf("(PREF_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("testOTP") && site.indexOf("(PREF_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("importUsers") && site.indexOf("(USER_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("sendPassEmail") && (site.indexOf("(USER_ADMIN)") >= 0 || site.indexOf("(USER_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("sendEventEmail") && (site.indexOf("(USER_ADMIN)") >= 0 || site.indexOf("(USER_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("getTempAccounts") && (site.indexOf("(SHARE_EDIT)") >= 0 || site.indexOf("(SHARE_VIEW)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("addTempAccount") && site.indexOf("(SHARE_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("removeTempAccount") && site.indexOf("(SHARE_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("getTempAccountFiles") && (site.indexOf("(SHARE_EDIT)") >= 0 || site.indexOf("(SHARE_VIEW)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("removeTempAccountFile") && site.indexOf("(SHARE_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("addTempAccountFile") && site.indexOf("(SHARE_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("migrateUsersVFS") && site.indexOf("(USER_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("convertUsers") && site.indexOf("(USER_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("generateSSL") && site.indexOf("(PREF_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("importReply") && site.indexOf("(PREF_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("testKeystore") && site.indexOf("(PREF_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("generateFileKey") && site.indexOf("(PREF_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("listSSL") && site.indexOf("(PREF_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("deleteSSL") && site.indexOf("(PREF_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("renameSSL") && site.indexOf("(PREF_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("addPrivateSSL") && site.indexOf("(PREF_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("addPublicSSL") && site.indexOf("(PREF_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("testDB") && site.indexOf("(PREF_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("pluginMethodCall") && site.indexOf("(PREF_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("convertXMLSQLUsers") && site.indexOf("(PREF_EDIT)") >= 0) {
            return true;
        }
        if (command.equalsIgnoreCase("registerCrushFTP") && (site.indexOf("(SERVER_EDIT)") >= 0 || site.indexOf("(PREF_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("setReportSchedules") && (site.indexOf("(REPORT_EDIT)") >= 0 || site.indexOf("(PREF_EDIT)") >= 0)) {
            return true;
        }
        if (command.equalsIgnoreCase("deleteReportSchedules") && (site.indexOf("(REPORT_EDIT)") >= 0 || site.indexOf("(PREF_EDIT)") >= 0)) {
            return true;
        }
        return command.equalsIgnoreCase("searchUserSettings") && (site.indexOf("(USER_ADMIN)") >= 0 || site.indexOf("(USER_EDIT)") >= 0 || site.indexOf("(USER_VIEW)") >= 0);
    }

    public static Object adminAction(Properties request, String site, String user_ip) {
        String status;
        block49: {
            status = "";
            try {
                if (!request.getProperty("instance", "").equals("") && AdminControls.checkRole("getServerItem", site, user_ip)) {
                    String id = Common.makeBoundary();
                    String instance = request.remove("instance").toString();
                    DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
                    Properties p = DMZServerCommon.getResponse(id, 20);
                    return p.get("data");
                }
                Vector<String> indexes = new Vector<String>();
                String[] indexesStr = request.getProperty("index", "").split(",");
                int x = 0;
                while (x < indexesStr.length) {
                    indexes.addElement(indexesStr[x].trim());
                    ++x;
                }
                Vector v = new Vector();
                v.addAll(ServerStatus.siVG("user_list"));
                if (request.getProperty("action", "").equals("kick") && AdminControls.checkRole(request.getProperty("action", ""), site, user_ip)) {
                    int x2 = v.size() - 1;
                    while (x2 >= 0) {
                        Properties user_info = (Properties)v.elementAt(x2);
                        int xx = 0;
                        while (xx < indexes.size()) {
                            if (user_info.getProperty("user_number").equals(indexes.elementAt(xx).toString())) {
                                status = String.valueOf(status) + ServerStatus.thisObj.kick(user_info, true) + "\r\n";
                                indexes.remove(xx);
                                try {
                                    Properties info = new Properties();
                                    info.put("alert_type", "kick");
                                    info.put("alert_sub_type", "admin");
                                    info.put("alert_timeout", "0");
                                    info.put("alert_max", "0");
                                    info.put("alert_msg", user_info.getProperty("user_name"));
                                    ServerStatus.thisObj.runAlerts("security_alert", info, user_info, null);
                                }
                                catch (Exception e) {
                                    Log.log("BAN", 1, e);
                                }
                                break;
                            }
                            ++xx;
                        }
                        --x2;
                    }
                    break block49;
                }
                if ((request.getProperty("action", "").equals("ban") || request.getProperty("action", "").equals("temporaryBan")) && AdminControls.checkRole("ban", site, user_ip)) {
                    v.addAll(ServerStatus.siVG("recent_user_list"));
                    int x3 = v.size() - 1;
                    while (x3 >= 0) {
                        Properties user_info = (Properties)v.elementAt(x3);
                        int xx = 0;
                        while (xx < indexes.size()) {
                            if (user_info.getProperty("user_number").equals(indexes.elementAt(xx).toString())) {
                                Properties info;
                                if (request.getProperty("action", "").equals("ban")) {
                                    status = String.valueOf(status) + ServerStatus.thisObj.ban(user_info, 0, "admin banned") + ",";
                                    try {
                                        info = new Properties();
                                        info.put("alert_type", "ban");
                                        info.put("alert_sub_type", "admin");
                                        info.put("alert_timeout", "0");
                                        info.put("alert_max", "0");
                                        info.put("alert_msg", "permanent");
                                        ServerStatus.thisObj.runAlerts("security_alert", info, user_info, null);
                                    }
                                    catch (Exception e) {
                                        Log.log("BAN", 1, e);
                                    }
                                } else if (request.getProperty("action", "").equals("temporaryBan")) {
                                    status = String.valueOf(status) + ServerStatus.thisObj.ban(user_info, Integer.parseInt(request.getProperty("banTimeout")), "admin banned") + ",";
                                    try {
                                        info = new Properties();
                                        info.put("alert_type", "ban");
                                        info.put("alert_sub_type", "admin");
                                        info.put("alert_timeout", String.valueOf(request.getProperty("banTimeout")));
                                        info.put("alert_max", "0");
                                        info.put("alert_msg", "temporary");
                                        ServerStatus.thisObj.runAlerts("security_alert", info, user_info, null);
                                    }
                                    catch (Exception e) {
                                        Log.log("BAN", 1, e);
                                    }
                                }
                                status = String.valueOf(status) + ServerStatus.thisObj.kick(user_info, true) + ",";
                                indexes.remove(xx);
                                break;
                            }
                            ++xx;
                        }
                        --x3;
                    }
                    break block49;
                }
                if (request.getProperty("action", "").equals("getUserInfo") && AdminControls.checkRole(request.getProperty("action", ""), site, user_ip)) {
                    v.addAll(ServerStatus.siVG("recent_user_list"));
                    int x4 = v.size() - 1;
                    while (x4 >= 0) {
                        Properties user_info = (Properties)v.elementAt(x4);
                        int xx = 0;
                        while (xx < indexes.size()) {
                            if (user_info.getProperty("user_number").equals(indexes.elementAt(xx).toString())) {
                                Properties user_info2 = (Properties)user_info.clone();
                                return AdminControls.stripUser(user_info2);
                            }
                            ++xx;
                        }
                        --x4;
                    }
                } else if (request.getProperty("action", "").equals("startAllServers") && AdminControls.checkRole(request.getProperty("action", ""), site, user_ip)) {
                    ServerStatus.thisObj.start_all_servers();
                    status = "OK";
                } else if (request.getProperty("action", "").equals("stopAllServers") && AdminControls.checkRole(request.getProperty("action", ""), site, user_ip)) {
                    ServerStatus.thisObj.stop_all_servers();
                    Thread.sleep(1000L);
                    ServerStatus.thisObj.stop_all_servers();
                    status = "OK";
                } else if (request.getProperty("action", "").equals("startServer") && AdminControls.checkRole(request.getProperty("action", ""), site, user_ip)) {
                    ServerStatus.thisObj.start_this_server(Integer.parseInt(request.getProperty("index")));
                    status = "OK";
                } else if (request.getProperty("action", "").equals("stopServer") && AdminControls.checkRole(request.getProperty("action", ""), site, user_ip)) {
                    ServerStatus.thisObj.stop_this_server(Integer.parseInt(request.getProperty("index")));
                    status = "OK";
                } else if (request.getProperty("action", "").equals("restartServer") && AdminControls.checkRole(request.getProperty("action", ""), site, user_ip)) {
                    int connected = ((GenericServer)ServerStatus.thisObj.main_servers.elementAt((int)Integer.parseInt((String)request.getProperty((String)"index")))).connected_users;
                    ServerStatus.thisObj.stop_this_server(Integer.parseInt(request.getProperty("index")));
                    Thread.sleep(1000L);
                    ServerStatus.thisObj.start_this_server(Integer.parseInt(request.getProperty("index")));
                    ((GenericServer)ServerStatus.thisObj.main_servers.elementAt((int)Integer.parseInt((String)request.getProperty((String)"index")))).connected_users = connected;
                    status = "OK";
                } else if (request.getProperty("action", "").equals("allStats") && AdminControls.checkRole(request.getProperty("action", ""), site, user_ip)) {
                    ServerStatus.thisObj.reset_server_login_counts();
                    ServerStatus.thisObj.reset_server_bytes_in_out();
                    ServerStatus.thisObj.reset_upload_download_counter();
                    status = "OK";
                } else if (request.getProperty("action", "").equals("loginStats") && AdminControls.checkRole(request.getProperty("action", ""), site, user_ip)) {
                    ServerStatus.thisObj.reset_server_login_counts();
                    status = "OK";
                } else if (request.getProperty("action", "").equals("uploadDownloadStats") && AdminControls.checkRole(request.getProperty("action", ""), site, user_ip)) {
                    ServerStatus.thisObj.reset_upload_download_counter();
                    status = "OK";
                } else if (request.getProperty("action", "").equals("transferStats") && AdminControls.checkRole(request.getProperty("action", ""), site, user_ip)) {
                    ServerStatus.thisObj.reset_server_bytes_in_out();
                    status = "OK";
                } else if (request.getProperty("action", "").equals("serverStats") && AdminControls.checkRole(request.getProperty("action", ""), site, user_ip)) {
                    status = "OK";
                } else if (request.getProperty("action", "").equals("clearMaxTransferAmounts") && AdminControls.checkRole(request.getProperty("action", ""), site, user_ip)) {
                    ServerStatus.thisObj.statTools.clearMaxTransferAmounts(request);
                    status = "OK";
                } else if (request.getProperty("action", "").equals("newFolder") && AdminControls.checkRole(request.getProperty("action", ""), site, user_ip) ? !(status = AdminControls.newFolder(request, site, true)).equalsIgnoreCase("OK") : (request.getProperty("action", "").equals("renameItem") && AdminControls.checkRole(request.getProperty("action", ""), site, user_ip) ? !(status = AdminControls.renameItem(request, site, true)).equalsIgnoreCase("OK") : (request.getProperty("action", "").equals("duplicateItem") && AdminControls.checkRole(request.getProperty("action", ""), site, user_ip) ? !(status = AdminControls.duplicateItem(request, site, true)).equalsIgnoreCase("OK") : request.getProperty("action", "").equals("deleteItem") && AdminControls.checkRole(request.getProperty("action", ""), site, user_ip) && !(status = AdminControls.deleteItem(request, site, true)).equalsIgnoreCase("OK")))) {
                    throw new Exception(status);
                }
            }
            catch (Exception e) {
                Log.log("HTTP_SERVER", 1, e);
                status = e.toString();
            }
        }
        return status;
    }

    public static String updateNow(Properties request, String site) {
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
        String xml = "";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            ServerStatus.thisObj.do_auto_update_early(false);
            xml = "Success";
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            xml = e.toString();
        }
        response = String.valueOf(response) + "<result><response>" + xml + "</response></result>";
        return response;
    }

    public static String checkForUpdate(Properties request, String site) {
        ServerStatus.thisObj.doCheckForUpdate(true);
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
        String xml = "";
        try {
            xml = String.valueOf(ServerStatus.siBG("update_available"));
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            xml = e.toString();
        }
        response = String.valueOf(response) + "<result><response>" + xml + "</response></result>";
        return response;
    }

    public static String updateWebNow(Properties request, String site) {
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
        String xml = "";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            ServerStatus.thisObj.do_auto_update_early(true);
            xml = "Success";
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            xml = e.toString();
        }
        response = String.valueOf(response) + "<result><response>" + xml + "</response></result>";
        return response;
    }

    public static String updateNowProgress(Properties request, String site) {
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
        response = String.valueOf(response) + "<result><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            response = String.valueOf(response) + "<currentStatus>" + ServerStatus.thisObj.updateHandler.updateCurrentStatus + "</currentStatus>";
            response = String.valueOf(response) + "<currentLoc>" + ServerStatus.thisObj.updateHandler.updateCurrentLoc + "</currentLoc>";
            response = String.valueOf(response) + "<maximumLoc>" + ServerStatus.thisObj.updateHandler.updateMaxSize + "</maximumLoc>";
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            response = String.valueOf(response) + e.toString();
        }
        response = String.valueOf(response) + "</response></result>";
        return response;
    }

    public static String cancelUpdateProgress(Properties request, String site) {
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
        response = String.valueOf(response) + "<result><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            ServerStatus.thisObj.updateHandler.cancel();
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            response = String.valueOf(response) + e.toString();
        }
        response = String.valueOf(response) + "</response></result>";
        return response;
    }

    public static String getRestartShutdownIdleStatus(Properties request, String site) {
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
        String xml = "";
        String xml2 = "";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            xml = "Success";
            xml2 = String.valueOf(xml2) + "<allow_logins>" + ServerStatus.siBG("allow_logins") + "</allow_logins>";
            xml2 = String.valueOf(xml2) + "<restart_when_idle>" + ServerStatus.siBG("restart_when_idle") + "</restart_when_idle>";
            xml2 = String.valueOf(xml2) + "<shutdown_when_idle>" + ServerStatus.siBG("shutdown_when_idle") + "</shutdown_when_idle>";
            xml2 = String.valueOf(xml2) + "<current_server_downloading_count>" + ServerStatus.thisObj.count_users_down() + "</current_server_downloading_count>";
            xml2 = String.valueOf(xml2) + "<current_server_uploading_count>" + ServerStatus.thisObj.count_users_up() + "</current_server_uploading_count>";
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            xml = e.toString();
        }
        response = String.valueOf(response) + "<result><response>" + xml + "</response>" + xml2 + "</result>";
        return response;
    }

    public static String restartIdle(Properties request, String site) {
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
        String xml = "";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            AdminControls.stopLogins(request, site);
            ServerStatus.siPUT("restart_when_idle", "true");
            xml = "Success";
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            xml = e.toString();
        }
        response = String.valueOf(response) + "<result><response>" + xml + "</response></result>";
        return response;
    }

    public static String shutdownIdle(Properties request, String site) {
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
        String xml = "";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            AdminControls.stopLogins(request, site);
            ServerStatus.siPUT("shutdown_when_idle", "true");
            xml = "Success";
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            xml = e.toString();
        }
        response = String.valueOf(response) + "<result><response>" + xml + "</response></result>";
        return response;
    }

    public static String startLogins(Properties request, String site) {
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
        String xml = "";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            ServerStatus.siPUT("allow_logins", "true");
            xml = "Success";
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            xml = e.toString();
        }
        response = String.valueOf(response) + "<result><response>" + xml + "</response></result>";
        return response;
    }

    public static String stopLogins(Properties request, String site) {
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
        String xml = "";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            ServerStatus.siPUT("allow_logins", "false");
            xml = "Success";
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            xml = e.toString();
        }
        response = String.valueOf(response) + "<result><response>" + xml + "</response></result>";
        return response;
    }

    public static String dumpStack(Properties request, String site) {
        String response = "";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            response = String.valueOf(response) + e.toString();
        }
        response = String.valueOf(response) + com.crushftp.client.Common.dumpStack(String.valueOf(ServerStatus.version_info_str) + ServerStatus.sub_version_info_str);
        return response;
    }

    public static String dumpHeap(Properties request, String site) {
        String response = "";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            response = String.valueOf(response) + e.toString();
        }
        response = String.valueOf(response) + new HeapDumper().dump();
        return response;
    }

    public static String pgpGenerateKeyPair(Properties request, String site) {
        String xml = "";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            String pass = VRL.vrlDecode(Common.url_decode(request.getProperty("pgpPrivateKeyPasswordGenerate")));
            String url = Common.url_decode(request.getProperty("pgpPivateKeyPathGenerate").replace('+', ' '));
            String pgp_key_path = new VRL(url).getPath();
            if (request.containsKey("encryption_cypher")) {
                com.crushftp.client.Common.generateKeyPair(pgp_key_path, Integer.parseInt(request.getProperty("pgpKeySizeGenerate")), Integer.parseInt(request.getProperty("pgpKeyDaysGenerate")), pass, Common.url_decode(request.getProperty("pgpCommonNameGenerate").replace('+', ' ')), request.getProperty("encryption_cypher").split(";"));
            } else {
                com.crushftp.client.Common.generateKeyPair(pgp_key_path, Integer.parseInt(request.getProperty("pgpKeySizeGenerate")), Integer.parseInt(request.getProperty("pgpKeyDaysGenerate")), pass, Common.url_decode(request.getProperty("pgpCommonNameGenerate").replace('+', ' ')));
            }
            xml = "Success";
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            xml = e.toString();
        }
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
        response = String.valueOf(response) + "<result><response>" + xml + "</response></result>";
        return response;
    }

    public static String runReport(Properties request, String site) {
        String xml;
        block14: {
            xml = "";
            try {
                if (request.getProperty("reportName").equalsIgnoreCase("ExportUserPass") && site.toUpperCase().indexOf("(CONNECT)") < 0) {
                    throw new Exception("Access denied");
                }
                String s = AdminControls.handleInstance(request, site);
                if (s != null) {
                    return s;
                }
                if (request.containsKey("report_token")) {
                    String report_token = Common.dots(request.getProperty("report_token")).trim();
                    if (new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "SavedReports/" + report_token).exists()) {
                        byte[] b = new byte[]{};
                        try (RandomAccessFile in = new RandomAccessFile(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "SavedReports/" + report_token), "r");){
                            b = new byte[(int)in.length()];
                            in.readFully(b);
                        }
                        xml = new String(b, "UTF8");
                        break block14;
                    }
                    Thread.sleep(1000L);
                    xml = "<html><body><h1>ERROR:No such report</h1></body></html>";
                    break block14;
                }
                Object reportItem = ServerStatus.thisObj.rt.getReportItem(request.getProperty("reportName"), ServerStatus.server_settings);
                Properties params = request;
                Vector<String> v = new Vector<String>();
                if (request.containsKey("usernames")) {
                    String[] usernames = com.crushftp.client.Common.html_clean_usernames(request.getProperty("usernames").split(","));
                    int x = 0;
                    while (x < usernames.length) {
                        s = usernames[x].trim();
                        if (!s.equals("")) {
                            v.addElement(s);
                        }
                        ++x;
                    }
                    request.put("usernames", v);
                }
                request.put("usernames", v);
                params.put("server_settings", ServerStatus.server_settings);
                Properties status = new Properties();
                xml = ServerStatus.thisObj.rt.writeReport("", "", status, params, reportItem, ServerStatus.server_settings, request.getProperty("export", "false").equals("true"), request.getProperty("reportName"), request);
                if (status.getProperty("report_empty", "true").equals("true")) {
                    xml = status.remove("report_text").toString();
                    xml = String.valueOf(xml) + "<hr/><center><h1>No data to report.</h1></center><hr/>";
                }
            }
            catch (Exception e) {
                Log.log("HTTP_SERVER", 1, e);
                xml = "<html><body><h1>" + e.toString() + "</h1></body></html>";
            }
        }
        return xml;
    }

    public static String handleInstance(Properties request, String site) throws Exception {
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            Properties p = DMZServerCommon.getResponse(id, 20);
            return p.get("data").toString();
        }
        return null;
    }

    public static String testReportSchedule(Properties request, String site) {
        String response = "<commandResult><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            Vector schedules = ServerStatus.VG("reportSchedules");
            Properties params = (Properties)schedules.elementAt(Integer.parseInt(request.getProperty("scheduleIndex")));
            Properties config = (Properties)params.get("config");
            config = (Properties)config.clone();
            config.put("server_settings", ServerStatus.server_settings);
            Properties status = new Properties();
            String dir = params.getProperty("reportFolder");
            if (!dir.endsWith("/")) {
                dir = String.valueOf(dir) + "/";
            }
            String filename = params.getProperty("reportFilename");
            filename = ServerStatus.thisObj.rt.replaceVars(filename, params, config);
            if (params.getProperty("reportOverwrite").equals("false") && new File_S(String.valueOf(dir) + filename).exists()) {
                response = String.valueOf(response) + "The report file already exists.";
            } else {
                if (config.get("usernames") == null) {
                    config.put("usernames", new Vector());
                }
                config.put("export", params.getProperty("export", ""));
                ReportTools.skipEmail(config.getProperty("reportName"), config);
                ServerStatus.thisObj.rt.writeReport(filename, dir, status, config, ServerStatus.thisObj.rt.getReportItem(config.getProperty("reportName"), ServerStatus.server_settings), ServerStatus.server_settings, params.getProperty("export", "").equals("true"), config.getProperty("reportName"), params);
                ReportTools.unSkipEmail(config.getProperty("reportName"), config);
                response = String.valueOf(response) + LOC.G("Report written to") + ":" + dir + filename;
            }
            response = String.valueOf(response) + "</response></commandResult>";
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
        }
        return response;
    }

    public static String testPGP(Properties request, String site) {
        String response = "<commandResult><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            try {
                PGPLib pgp = new PGPLib();
                pgp.setUseExpiredKeys(true);
                ByteArrayOutputStream baos_key = new ByteArrayOutputStream();
                boolean pbe = false;
                String keyLocation = request.getProperty("publicKey");
                if (keyLocation.toLowerCase().startsWith("password:")) {
                    pbe = true;
                } else {
                    if (keyLocation.indexOf(":") < 0 || keyLocation.indexOf(":") < 3 && !keyLocation.toLowerCase().startsWith("s3:") && !keyLocation.toLowerCase().startsWith("b2:")) {
                        keyLocation = "FILE://" + keyLocation;
                    }
                    VRL key_vrl = new VRL(keyLocation);
                    GenericClient c_key = com.crushftp.client.Common.getClient(Common.getBaseUrl(key_vrl.toString()), "CrushFTP", new Vector());
                    if (ServerStatus.BG("v10_beta") && key_vrl.getConfig() != null && key_vrl.getConfig().size() > 0) {
                        c_key.setConfigObj(key_vrl.getConfig());
                    }
                    c_key.login(key_vrl.getUsername(), key_vrl.getPassword(), "");
                    Common.streamCopier(c_key.download(key_vrl.getPath(), 0L, -1L, true), baos_key, false, true, true);
                    c_key.logout();
                }
                ByteArrayInputStream bytesInKey = new ByteArrayInputStream(baos_key.toByteArray());
                pgp.setCompression("UNCOMPRESSED");
                String source_data = "This is a test.";
                ByteArrayOutputStream baos_encrypted = new ByteArrayOutputStream();
                ByteArrayInputStream bais_source = new ByteArrayInputStream(source_data.getBytes());
                if (pbe) {
                    pgp.encryptStreamPBE((InputStream)bais_source, "test_data", com.crushftp.client.Common.encryptDecrypt(keyLocation.substring(keyLocation.indexOf(":") + 1), false), (OutputStream)baos_encrypted, false, false);
                } else {
                    pgp.encryptStream((InputStream)bais_source, "test_data", (InputStream)bytesInKey, (OutputStream)baos_encrypted, false, false);
                }
                bytesInKey.close();
                String encrypted_data = Base64.encodeBytes(baos_encrypted.toByteArray());
                baos_key = new ByteArrayOutputStream();
                pbe = false;
                keyLocation = request.getProperty("privateKey");
                bais_source = new ByteArrayInputStream(baos_encrypted.toByteArray());
                ByteArrayOutputStream baos_decrypted = new ByteArrayOutputStream();
                if (keyLocation.toLowerCase().startsWith("password:")) {
                    pbe = true;
                } else {
                    if (keyLocation.indexOf(":") < 0 || keyLocation.indexOf(":") < 3 && !keyLocation.toLowerCase().startsWith("s3:") && !keyLocation.toLowerCase().startsWith("b2:")) {
                        keyLocation = "FILE://" + keyLocation;
                    }
                    VRL key_vrl = new VRL(keyLocation);
                    GenericClient c_key = com.crushftp.client.Common.getClient(Common.getBaseUrl(key_vrl.toString()), "CrushFTP", new Vector());
                    if (ServerStatus.BG("v10_beta") && key_vrl.getConfig() != null && key_vrl.getConfig().size() > 0) {
                        c_key.setConfigObj(key_vrl.getConfig());
                    }
                    c_key.login(key_vrl.getUsername(), key_vrl.getPassword(), "");
                    Common.streamCopier(c_key.download(key_vrl.getPath(), 0L, -1L, true), baos_key, false, true, true);
                    c_key.logout();
                }
                ByteArrayInputStream bytesIn1 = new ByteArrayInputStream(baos_key.toByteArray());
                ByteArrayInputStream bytesIn2 = new ByteArrayInputStream(baos_key.toByteArray());
                pgp.setCompression("UNCOMPRESSED");
                if (pbe) {
                    pgp.decryptStreamPBE((InputStream)bais_source, com.crushftp.client.Common.encryptDecrypt(keyLocation.substring(keyLocation.indexOf(":") + 1), false), (OutputStream)baos_decrypted);
                } else if (new com.didisoft.pgp.KeyStore().importPrivateKey((InputStream)bytesIn1)[0].checkPassword(request.getProperty("privateKeyPass"))) {
                    pgp.decryptStream((InputStream)bais_source, (InputStream)bytesIn2, request.getProperty("privateKeyPass"), (OutputStream)baos_decrypted);
                } else {
                    pgp.decryptStream((InputStream)bais_source, (InputStream)bytesIn2, com.crushftp.client.Common.encryptDecrypt(request.getProperty("privateKeyPass"), false), (OutputStream)baos_decrypted);
                }
                bytesIn1.close();
                bytesIn2.close();
                String decrypted_data = new String(baos_decrypted.toByteArray());
                if (!decrypted_data.trim().equals(source_data.trim())) {
                    throw new Exception("Source and decrypted data are not equal!!!  " + source_data + " vs. " + decrypted_data);
                }
                response = String.valueOf(response) + "SUCCESS:\r\n<br/>";
                response = String.valueOf(response) + "Source data:" + source_data + "\r\n<br/>";
                response = String.valueOf(response) + "Encrypted data:<br/>";
                response = String.valueOf(response) + encrypted_data + "<br/>";
                response = String.valueOf(response) + "\r\n<br/>";
                response = String.valueOf(response) + "Decrypted value:\r\n<br/>";
                response = String.valueOf(response) + decrypted_data;
            }
            catch (Exception e) {
                response = String.valueOf(response) + "ERROR:\r\n<br/>" + e;
            }
            response = String.valueOf(response) + "</response></commandResult>";
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
        }
        return response;
    }

    public static Object changeJobStatus(Properties request, String site) {
        if (request.getProperty("status", "").equals("restart")) {
            Vector vv = new Vector();
            String[] schedule_names = com.crushftp.client.Common.dots(request.getProperty("scheduleName", "")).split(";");
            String[] job_ids = com.crushftp.client.Common.dots(request.getProperty("job_id", "")).split(";");
            String summary_name = "";
            int x = 0;
            while (x < schedule_names.length) {
                request = (Properties)request.clone();
                request.put("scheduleName", schedule_names[x].trim());
                request.put("job_id", job_ids[x]);
                if (summary_name.length() < 100) {
                    summary_name = String.valueOf(summary_name) + schedule_names[x].trim() + ",";
                } else if (summary_name.length() >= 100 && summary_name.indexOf("...") < 0) {
                    summary_name = String.valueOf(summary_name) + "...(and more)...";
                }
                try {
                    File_S f1 = new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + schedule_names[x].trim());
                    AdminControls.checkJobFolder(site, request, vv, f1);
                    if (vv.size() > 1) {
                        return "FAILURE:Job ID not found! " + vv.size() + ":" + schedule_names[x].trim();
                    }
                    Log.log("SERVER", 0, "Restarting job:" + schedule_names[x].trim() + ": Matching ids:" + vv.size());
                    Properties tracker = (Properties)vv.remove(0);
                    if (!tracker.getProperty("status", "").equalsIgnoreCase("completed-errors") && !tracker.getProperty("status", "").equalsIgnoreCase("cancelled")) {
                        return "FAILURE:Only jobs in a cancelled or completed-errors status can be restarted. (" + tracker.getProperty("status") + ")";
                    }
                    tracker.remove("errors");
                    tracker.put("status", "running");
                    Vector active_items = (Vector)tracker.get("active_items");
                    if (active_items != null) {
                        Properties task_item = (Properties)active_items.elementAt(active_items.size() - 1);
                        task_item.put("status", "active");
                        task_item.remove("error");
                    }
                    try {
                        Common.writeXMLObject(tracker.getProperty("job_history_obj_path"), (Object)tracker, "tracker");
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 1, e);
                    }
                    f1 = new File_S(tracker.getProperty("job_history_obj_path"));
                    File_S f2 = new File_S(String.valueOf(f1.getParent()) + "/inprogress/" + tracker.getProperty("id") + ".XML");
                    f1.renameTo(f2);
                    AdminControls.startJob(new File_S(f1.getParent()), true, new StringBuffer(tracker.getProperty("id")), null);
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                }
                ++x;
            }
            return "SUCCESS:" + summary_name + " restarted.";
        }
        Vector jobs = ServerStatus.siVG("running_tasks");
        int x = jobs.size() - 1;
        while (x >= 0) {
            Properties tracker = (Properties)jobs.elementAt(x);
            if (request.getProperty("job_id", "").equalsIgnoreCase(tracker.getProperty("id"))) {
                if (tracker.getProperty("status").equalsIgnoreCase("running") || tracker.getProperty("status").toLowerCase().indexOf("paused") >= 0) {
                    tracker.put("status", request.getProperty("status"));
                    if (tracker.getProperty("restore_job", "false").equals("true") && request.getProperty("status").equalsIgnoreCase("running")) {
                        request.put("restore_job", "true");
                        AdminControls.testJobSchedule(request, site);
                    }
                    tracker.remove("restore_job");
                }
                return "SUCCESS:" + tracker.getProperty("status");
            }
            --x;
        }
        return "FAILURE:Job not found.";
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static String testJobSchedule(Properties request, String site) {
        StringBuffer jobid;
        File job;
        String response;
        block9: {
            if (ServerStatus.siIG("enterprise_level") <= 0) {
                return "ERROR:Enterprise License only feature.";
            }
            response = "";
            try {
                String s = AdminControls.handleInstance(request, site);
                if (s != null) {
                    return s;
                }
                Vector jobs = JobScheduler.getJobList(false);
                job = null;
                int x = 0;
                while (job == null && x < jobs.size()) {
                    File_S f = (File_S)jobs.elementAt(x);
                    if (AdminControls.jobName(f).equalsIgnoreCase(request.getProperty("scheduleName"))) {
                        job = f;
                    }
                    ++x;
                }
                jobid = new StringBuffer();
                if (site.indexOf("(USER_ADMIN)") >= 0) {
                    UserTools.testLimitedTasks((Properties)Common.readXMLObject(String.valueOf(job.getPath()) + "/job.XML"), request);
                }
                if (site.indexOf("(CONNECT)") > 0 && site.indexOf("(JOB_EDIT)") > 0 || site.indexOf("(JOB_MONITOR)") < 0) break block9;
                if (!new File_S(String.valueOf(job.getPath()) + "/job.XML").exists()) return "<commandResult><response>" + response + "</response><jobid>" + jobid.toString() + "</jobid></commandResult>";
                Properties p = (Properties)Common.readXMLObject(String.valueOf(job.getPath()) + "/job.XML");
                if (p == null) {
                    return "<commandResult><response>ERROR:Could not find job : " + request.getProperty("scheduleName", "") + "</response></commandResult>";
                }
                if (AdminControls.expandUsernames(p.getProperty("allowed_usernames", "")).indexOf(request.getProperty("calling_user", "~NONE~").toUpperCase()) < 0) return "<commandResult><response>ERROR:Access denied.</response></commandResult>";
                response = AdminControls.startJob((File_S)job, request.getProperty("restore_job", "").equals("true"), jobid, request);
                return "<commandResult><response>" + response + "</response><jobid>" + jobid.toString() + "</jobid></commandResult>";
            }
            catch (Exception e) {
                Log.log("HTTP_SERVER", 1, e);
                response = "ERROR:" + e;
                return "<commandResult><response>ERROR:" + e.getMessage() + "</response></commandResult>";
            }
        }
        response = AdminControls.startJob((File_S)job, request.getProperty("restore_job", "").equals("true"), jobid, request);
        return "<commandResult><response>" + response + "</response><jobid>" + jobid.toString() + "</jobid></commandResult>";
    }

    static String startJob(File_S job, final boolean restore, StringBuffer jobid, final Properties request) {
        String response = "";
        final Properties params = (Properties)Common.readXMLObject(String.valueOf(job.getPath()) + "/job.XML");
        params.put("scheduleName", AdminControls.jobName(job));
        boolean ok = true;
        if (params.getProperty("scheduleName", "").toUpperCase().endsWith("_SINGLE") || params.getProperty("single", "").equalsIgnoreCase("true")) {
            boolean bl = ok = restore || !JobScheduler.jobRunning(params.getProperty("scheduleName", ""));
        }
        if (ok && params.getProperty("singleServer", "").equals("true")) {
            Properties pp = new Properties();
            pp.put("scheduleName", params.getProperty("scheduleName", ""));
            pp.put("need_response", "true");
            SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.JobScheduler.jobRunning", "info", pp);
            long start = System.currentTimeMillis();
            while (pp.getProperty("response_num", "0").equals("0") && System.currentTimeMillis() - start < 5000L) {
                try {
                    Thread.sleep(100L);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            Properties val = (Properties)pp.get("val");
            if (val != null) {
                Enumeration<Object> keys = val.keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement().toString();
                    if (!key.startsWith("running_") || !val.getProperty(key).equalsIgnoreCase("true")) continue;
                    ok = false;
                }
            }
        }
        response = ok ? String.valueOf(response) + "Job Started" : String.valueOf(response) + "Job is already running, stop existing job first.";
        Log.log("SERVER", 1, "Job Schedule:" + params.getProperty("scheduleName") + ":" + response);
        if (ok) {
            if (jobid.length() == 0) {
                jobid.append(Common.makeBoundary(20));
            }
            params.put("new_job_id_run", jobid.toString());
            try {
                Runnable r = new Runnable(){

                    @Override
                    public void run() {
                        Properties event = new Properties();
                        if (restore) {
                            event.put("restore_job", "true");
                        }
                        event.putAll((Map<?, ?>)params);
                        event.put("enabled", "true");
                        event.put("event_plugin_list", params.getProperty("plugin", params.getProperty("event_plugin_list", "")));
                        event.put("name", "ScheduledPluginEvent:" + params.getProperty("scheduleName"));
                        try {
                            runningSchedules.addElement(params.getProperty("scheduleName"));
                            Properties info = request == null ? new Properties() : request;
                            Properties properties = ServerStatus.thisObj.events6.doEventPlugin(info, event, null, new Vector());
                        }
                        finally {
                            runningSchedules.remove(params.getProperty("scheduleName"));
                        }
                    }
                };
                if (request == null || request.getProperty("async", "true").equals("true")) {
                    Worker.startWorker(r);
                } else {
                    request.put("return_tracker", "true");
                    r.run();
                    Properties tracker = (Properties)((Properties)request.get("tracker")).clone();
                    tracker.remove("settings");
                    tracker.remove("active_items");
                    tracker.remove("connections");
                    if (request.getProperty("response_type", "simple").equals("simple")) {
                        response = tracker.getProperty("status");
                    } else if (request.getProperty("response_type", "simple").equals("log")) {
                        response = AdminControls.getJobLog(tracker);
                    } else {
                        if (request.getProperty("response_type", "simple").equals("all")) {
                            tracker.put("full_log", AdminControls.getJobLog(tracker));
                        }
                        response = Common.getXMLString(tracker, "job", "");
                        response = response.substring(response.indexOf("<job")).trim();
                    }
                }
            }
            catch (Exception e) {
                Log.log("HTTP_SERVER", 1, e);
            }
        }
        return response;
    }

    public static String getJobLog(Properties tracker) {
        String full_log = "Error reading task log...";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            long len = new File_U(tracker.getProperty("log_file"), System.getProperty("crushftp.server.root", "")).length();
            FileInputStream in = new FileInputStream(new File_U(tracker.getProperty("log_file"), System.getProperty("crushftp.server.root", "")));
            long skip = 0L;
            if (len > 0x100000L) {
                skip = len - 0x100000L;
            }
            in.skip(skip);
            com.crushftp.client.Common.copyStreams(in, baos, true, true);
            full_log = new String(baos.toByteArray(), "UTF8");
        }
        catch (IOException e) {
            full_log = String.valueOf(full_log) + "\r\n" + e + "\r\n" + tracker.getProperty("log_file");
            Log.log("SERVER", 0, e);
        }
        return full_log;
    }

    public static String testSMTP(Properties request, String site) {
        String response = "<commandResult><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            Properties p = request;
            String results = com.crushftp.client.Common.send_mail(ServerStatus.SG("discovered_ip"), p.getProperty("to", ""), p.getProperty("cc", ""), p.getProperty("bcc", ""), p.getProperty("from", ""), p.getProperty("subject", ""), p.getProperty("body", ""), p.getProperty("server", ""), p.getProperty("user", ""), p.getProperty("pass", ""), p.getProperty("ssl", "").equals("true"), p.getProperty("html", "").equals("true"), null);
            response = String.valueOf(response) + Common.url_encode(results);
            response = String.valueOf(response) + "</response></commandResult>";
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
        }
        return response;
    }

    public static String testOTP(Properties request, String site) {
        String response = "<commandResult><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            Common.send_otp_for_auth_sms(request.getProperty("otp_to", ""), "Test sms!");
            response = String.valueOf(response) + "Success!";
        }
        catch (Exception e) {
            response = String.valueOf(response) + "ERROR: " + e.getMessage();
            Log.log("HTTP_SERVER", 1, e);
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String sendEventEmail(Properties request, String site) {
        String response = "<commandResult><response>";
        Common.urlDecodePost(request);
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            Properties fake_event = new Properties();
            fake_event.put("name", "fake_event");
            fake_event.put("to", Common.replace_str(Common.replace_str(request.getProperty("email_to"), "&gt;", ">"), "&lt;", "<"));
            fake_event.put("from", Common.replace_str(Common.replace_str(request.getProperty("email_from"), "&gt;", ">"), "&lt;", "<"));
            fake_event.put("cc", Common.replace_str(Common.replace_str(request.getProperty("email_cc"), "&gt;", ">"), "&lt;", "<"));
            fake_event.put("bcc", Common.replace_str(Common.replace_str(request.getProperty("email_bcc"), "&gt;", ">"), "&lt;", "<"));
            fake_event.put("body", request.getProperty("email_body"));
            fake_event.put("subject", request.getProperty("email_subject"));
            fake_event.put("event_user_action_list", "(disconnect)");
            fake_event.put("event_now_cb", "true");
            fake_event.put("event_now_cb", "true");
            Properties fake_user = UserTools.ut.getUser(request.getProperty("serverGroup"), request.getProperty("username"), true);
            Properties fake_user_info = new Properties();
            fake_user_info.put("user_name", request.getProperty("username"));
            fake_user_info.put("username", request.getProperty("username"));
            Vector<Properties> fake_items = new Vector<Properties>();
            Properties p = new Properties();
            p.put("name", "test item name");
            p.put("the_file_name", "test item name");
            p.put("url", "file://test/path/to/folder/test item name");
            p.put("type", "FILE");
            p.put("size", "500");
            p.put("modified", String.valueOf(System.currentTimeMillis()));
            p.put("sizeFormatted", com.crushftp.client.Common.format_bytes2(p.getProperty("size")));
            p.put("privs", "(read)(write)(delete)(view)(resume)");
            p.put("path", "/fake_uploads/");
            p.put("the_file_path", "/fake_uploads/");
            p.put("num_items", "1");
            p.put("owner", "user");
            p.put("group", "group");
            p.put("month", "1");
            p.put("day", "1");
            p.put("time_or_year", "1970");
            p.put("permissions", "drwxrwxrwx");
            p.put("root_dir", "/");
            p.put("protocol", "file");
            p.put("link", "false");
            p.put("the_file_size", "500");
            p.put("the_command", "STOR");
            p.put("the_file_speed", "500K/sec");
            p.put("the_file_status", "");
            p.put("the_file_start", String.valueOf(System.currentTimeMillis()));
            p.put("the_file_end", String.valueOf(System.currentTimeMillis()));
            p.put("the_file_resume_loc", "0");
            p.put("the_file_md5", Common.getMD5(new ByteArrayInputStream("a".getBytes())));
            p.put("the_file_resume_loc", "0");
            p.put("the_file_resume_loc", "0");
            fake_items.addElement(p);
            response = String.valueOf(response) + ServerStatus.thisObj.events6.doEventEmail(fake_event, fake_user, fake_user_info, fake_items, new Vector());
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 1, ee);
            response = String.valueOf(response) + "Error:" + Common.url_encode(ee.toString()) + "\r\n";
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String migrateUsersVFS(Properties request, String site) {
        String response = "<commandResult><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            response = String.valueOf(response) + ServerStatus.thisObj.common_code.migrateUsersVFS(request.getProperty("serverGroup"), request.getProperty("findPath"), request.getProperty("replacePath"));
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 1, ee);
            response = String.valueOf(response) + "Error:" + Common.url_encode(ee.toString()) + "\r\n";
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String convertUsers(Properties request, String site) {
        String response = "<commandResult><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            Vector<String> users = new Vector<String>();
            String[] usersStr = request.getProperty("users").split(";");
            int x = 0;
            while (x < usersStr.length) {
                if (!usersStr[x].trim().equals("")) {
                    users.addElement(usersStr[x].trim());
                }
                ++x;
            }
            response = String.valueOf(response) + UserTools.convertUsers(request.getProperty("allUsers").equalsIgnoreCase("true"), users, request.getProperty("serverGroup"), request.getProperty("username", ""));
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 1, ee);
            response = String.valueOf(response) + "Error:" + Common.url_encode(ee.toString()) + "\r\n";
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String testDB(Properties request, String site) {
        String response = "<commandResult><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            Connection conn = null;
            if (!request.getProperty("db_driver").equalsIgnoreCase("org.apache.derby.jdbc.EmbeddedDriver")) {
                String[] db_drv_files = request.getProperty("db_driver_file").split(";");
                URL[] urls = new URL[db_drv_files.length];
                int x = 0;
                while (x < db_drv_files.length) {
                    urls[x] = new File_S(db_drv_files[x]).toURI().toURL();
                    ++x;
                }
                URLClassLoader cl = new URLClassLoader(urls);
                Class<?> drvCls = Class.forName(request.getProperty("db_driver"), true, cl);
                Driver driver = (Driver)drvCls.newInstance();
                Properties props = new Properties();
                props.setProperty("user", request.getProperty("db_user"));
                props.setProperty("password", ServerStatus.thisObj.common_code.decode_pass(request.getProperty("db_pass")));
                conn = driver.connect(request.getProperty("db_url"), props);
            } else {
                try {
                    conn = ServerStatus.thisObj.statTools.getConnection();
                }
                catch (Throwable e) {
                    Log.log("HTTP_SERVER", 1, e);
                }
            }
            conn.close();
            response = String.valueOf(response) + "Success";
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 1, ee);
            response = String.valueOf(response) + "Error:" + Common.url_encode(ee.toString());
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String testQuery(Properties request, String site) {
        String response;
        block53: {
            response = "<commandResult><response>";
            try {
                String s = AdminControls.handleInstance(request, site);
                if (s != null) {
                    return s;
                }
                Connection conn = null;
                if (!request.getProperty("db_driver").equalsIgnoreCase("org.apache.derby.jdbc.EmbeddedDriver")) {
                    String[] db_drv_files = request.getProperty("db_driver_file").split(";");
                    URL[] urls = new URL[db_drv_files.length];
                    int x = 0;
                    while (x < db_drv_files.length) {
                        urls[x] = new File_S(db_drv_files[x]).toURI().toURL();
                        ++x;
                    }
                    URLClassLoader cl = new URLClassLoader(urls);
                    Class<?> drvCls = Class.forName(request.getProperty("db_driver"), true, cl);
                    Driver driver = (Driver)drvCls.newInstance();
                    Properties props = new Properties();
                    props.setProperty("user", request.getProperty("db_user"));
                    props.setProperty("password", ServerStatus.thisObj.common_code.decode_pass(request.getProperty("db_pass")));
                    conn = driver.connect(request.getProperty("db_url"), props);
                } else if (request.getProperty("db_url", "").indexOf("statsDB") >= 0) {
                    try {
                        conn = ServerStatus.thisObj.statTools.getConnection();
                    }
                    catch (Throwable e) {
                        Log.log("HTTP_SERVER", 1, e);
                    }
                } else if (request.getProperty("db_url", "").indexOf("syncsDB") >= 0) {
                    try {
                        conn = SyncTools.dbt.getConnection();
                    }
                    catch (Throwable e) {
                        Log.log("HTTP_SERVER", 1, e);
                    }
                } else if (request.getProperty("db_url", "").indexOf("searchDB") >= 0) {
                    try {
                        conn = ServerStatus.thisObj.searchTools.getConnection();
                    }
                    catch (Throwable e) {
                        Log.log("HTTP_SERVER", 1, e);
                    }
                }
                Statement st = conn.createStatement();
                try {
                    String table;
                    String sql = request.getProperty("sql");
                    if (sql.startsWith("IMPORTCSV:")) {
                        table = Common.last(sql.substring(sql.indexOf(":") + 1).trim());
                        int count = 0;
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File_S(table))));){
                            String line = "";
                            while ((line = br.readLine()) != null) {
                                String sql2 = "";
                                try {
                                    String[] cols = line.split(";");
                                    sql2 = "insert into " + table.substring(0, table.indexOf(".")) + " values (";
                                    int x = 0;
                                    while (x < cols.length) {
                                        if (x > 0) {
                                            sql2 = String.valueOf(sql2) + ",";
                                        }
                                        sql2 = String.valueOf(sql2) + cols[x];
                                        ++x;
                                    }
                                    sql2 = String.valueOf(sql2) + ");";
                                    st.executeUpdate(sql2);
                                    ++count;
                                }
                                catch (Exception e) {
                                    Log.log("SERVER", 0, e);
                                    Log.log("SERVER", 0, String.valueOf(count));
                                    Log.log("SERVER", 0, sql2);
                                }
                            }
                        }
                        response = String.valueOf(response) + count + " rows inserted.";
                        break block53;
                    }
                    if (sql.startsWith("EXPORTCSV:")) {
                        table = Common.last(sql.substring(sql.indexOf(":") + 1).trim());
                        if (!table.toUpperCase().endsWith(".CSV")) {
                            throw new Exception("Must be a CSV file.");
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                        RandomAccessFile raf = new RandomAccessFile(new File_S(table), "rw");
                        raf.setLength(0L);
                        int count = 0;
                        try {
                            ResultSet rs = st.executeQuery("select * from " + table.substring(0, table.indexOf(".")));
                            while (rs.next()) {
                                int columnCount = rs.getMetaData().getColumnCount();
                                String line = "";
                                int x = 1;
                                while (x <= columnCount) {
                                    String val = rs.getString(x);
                                    if (val == null) {
                                        val = "";
                                    }
                                    if (x > 1) {
                                        line = String.valueOf(line) + ",";
                                    }
                                    if (rs.getMetaData().getColumnTypeName(x).equalsIgnoreCase("TIMESTAMP")) {
                                        try {
                                            line = String.valueOf(line) + "\"" + sdf.format(new Date(rs.getTimestamp(x).getTime())) + "\"";
                                        }
                                        catch (Exception exception) {}
                                    } else if (rs.getMetaData().getColumnTypeName(x).equalsIgnoreCase("DOUBLE")) {
                                        try {
                                            line = String.valueOf(line) + "\"" + rs.getLong(x) + "\"";
                                        }
                                        catch (Exception exception) {}
                                    } else {
                                        try {
                                            line = String.valueOf(line) + "\"" + rs.getString(x) + "\"";
                                        }
                                        catch (Exception exception) {
                                            // empty catch block
                                        }
                                    }
                                    ++x;
                                }
                                line = String.valueOf(line) + "\r\n";
                                raf.write(line.getBytes("UTF8"));
                                line = "";
                                ++count;
                            }
                        }
                        finally {
                            raf.close();
                        }
                        response = String.valueOf(response) + count + " rows exported.";
                        break block53;
                    }
                    boolean update = false;
                    if (sql.toUpperCase().indexOf("USE ") >= 0) {
                        throw new Exception("Invalid SQL statement.");
                    }
                    if (!sql.toUpperCase().startsWith("SELECT")) {
                        update = true;
                    }
                    if (update) {
                        response = String.valueOf(response) + st.executeUpdate(sql) + " rows updated.";
                    } else {
                        ResultSet rs = st.executeQuery(sql);
                        Vector v = AdminControls.loadTable(rs, Integer.parseInt(request.getProperty("sql_limit")));
                        s = Common.getXMLString(v, "SQL", null).trim();
                        response = String.valueOf(response) + s.substring(s.indexOf("<SQL"));
                    }
                }
                finally {
                    st.close();
                    conn.close();
                }
            }
            catch (Exception ee) {
                Log.log("HTTP_SERVER", 1, ee);
                response = String.valueOf(response) + "Error:" + Common.url_encode(ee.toString());
            }
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    static Vector loadTable(ResultSet rs, int limit) {
        Vector<Properties> v = new Vector<Properties>();
        try {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                while (rs.next()) {
                    Properties saver = new Properties();
                    try {
                        int columnCount = rs.getMetaData().getColumnCount();
                        int x = 1;
                        while (x <= columnCount) {
                            String val;
                            String key = rs.getMetaData().getColumnName(x);
                            if (key.toUpperCase().startsWith("ORACLE_")) {
                                key = key.substring("ORACLE_".length());
                            }
                            if (key.toUpperCase().startsWith("SQL_FIELD_")) {
                                key = key.substring("SQL_FIELD_".length());
                            }
                            if ((val = rs.getString(x)) == null) {
                                val = "";
                            }
                            if (rs.getMetaData().getColumnTypeName(x).equalsIgnoreCase("TIMESTAMP")) {
                                try {
                                    saver.put(key, sdf.format(new Date(rs.getTimestamp(x).getTime())));
                                }
                                catch (Exception exception) {}
                            } else if (rs.getMetaData().getColumnTypeName(x).equalsIgnoreCase("DOUBLE")) {
                                try {
                                    saver.put(key, String.valueOf(rs.getLong(x)));
                                }
                                catch (Exception exception) {}
                            } else {
                                try {
                                    saver.put(key, String.valueOf(rs.getString(x)));
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                            ++x;
                        }
                    }
                    catch (Throwable ee) {
                        Log.log("SERVER", 0, ee);
                    }
                    v.addElement(saver);
                    if (v.size() >= limit) break;
                }
                rs.close();
            }
            catch (Throwable e) {
                Log.log("SERVER", 0, e);
                try {
                    rs.close();
                }
                catch (SQLException sQLException) {}
            }
        }
        finally {
            try {
                rs.close();
            }
            catch (SQLException sQLException) {}
        }
        return v;
    }

    public static String pluginMethodCall(Properties request, String site) {
        String response = "<commandResult><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            Object parent = Common.getPlugin(request.getProperty("pluginName"), null, request.getProperty("pluginSubItem", ""));
            if (parent == null && request.getProperty("pluginSubItem", "").equals("")) {
                parent = Common.getPlugin(request.getProperty("pluginName"), null, "false");
            }
            Method method = parent.getClass().getMethod(request.getProperty("method", "testSettings"), new Properties().getClass());
            response = String.valueOf(response) + method.invoke(parent, request).toString();
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 1, ee);
            response = String.valueOf(response) + "Error:" + Common.url_encode(ee.toString());
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String convertXMLSQLUsers(Properties request, String site) {
        String response = "<commandResult><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            UserTools.convertUsersSQLXML(request.getProperty("fromMode"), request.getProperty("toMode"), request.getProperty("serverGroup"));
            response = String.valueOf(response) + "Success.";
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 1, ee);
            response = String.valueOf(response) + "Error:" + Common.url_encode(ee.toString());
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String registerCrushFTP(Properties request, String site) {
        String response = "<commandResult><response>";
        try {
            String registration_code;
            String registration_email;
            String instance = request.getProperty("instance", "");
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                String id2 = Common.makeBoundary();
                DMZServerCommon.sendCommand(instance, new Properties(), "GET:SERVER_SETTINGS", id2);
                Properties p = DMZServerCommon.getResponse(id2, 10);
                Common.write_server_settings((Properties)p.get("data"), instance);
                return s;
            }
            String registration_name = Common.url_encode_all(request.getProperty("registration_name").toUpperCase().trim());
            if (ServerStatus.thisObj.common_code.register(registration_name, registration_email = request.getProperty("registration_email").toUpperCase().trim(), registration_code = request.getProperty("registration_code").trim())) {
                String v = ServerStatus.thisObj.common_code.getRegistrationAccess("V", registration_code);
                if (v != null && (v.equals("4") || v.equals("5") || v.equals("6") || v.equals("7"))) {
                    response = String.valueOf(response) + "CrushFTP " + v + " needs an upgrade license for CrushFTP 8.  http://www.crushftp.com/pricing.html";
                } else {
                    ServerStatus.server_settings.put("registration_name", registration_name);
                    ServerStatus.server_settings.put("registration_email", registration_email);
                    ServerStatus.server_settings.put("registration_code", registration_code);
                    ServerStatus.put_in("registration_name", registration_name);
                    ServerStatus.put_in("registration_email", registration_email);
                    ServerStatus.put_in("registration_code", registration_code);
                    ServerStatus.server_settings.put("max_max_users", ServerStatus.thisObj.common_code.getRegistrationAccess("MAX", registration_code));
                    ServerStatus.server_settings.put("max_users", ServerStatus.thisObj.common_code.getRegistrationAccess("MAX", registration_code));
                    response = String.valueOf(response) + "Registration Information Accepted";
                    SharedSessionReplicated.reset_sockets();
                }
                ServerStatus.thisObj.prefsProvider.check_code();
                ServerStatus.thisObj.save_server_settings(false);
            } else {
                response = String.valueOf(response) + LOC.G("Invalid Name, Email or Code!");
            }
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 1, ee);
            response = String.valueOf(response) + "Error:" + Common.url_encode(ee.toString());
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String restorePrefs(Properties request, String site) throws Exception {
        String backup_id = request.getProperty("backup_id", "");
        if (backup_id == null || backup_id.equals("")) {
            Vector<String> v = new Vector<String>();
            int index = 0;
            while (index < 100) {
                File f = new File(String.valueOf(System.getProperty("crushftp.backup")) + "backup/prefs" + index + ".XML");
                if (f.exists() || f.length() > 0L) {
                    v.addElement(String.valueOf(index) + ":" + f.lastModified());
                }
                ++index;
            }
            Common cfr_ignored_0 = ServerStatus.thisObj.common_code;
            return Common.getXMLString(v, "prefs", null);
        }
        ServerStatus.thisObj.prefsProvider.savePrefs(ServerStatus.thisObj.prefsProvider.getBackupPrefs(backup_id), null);
        ServerStatus.thisObj.server_info.put("currentFileDate", "0");
        Thread.sleep(3000L);
        return "<commandResult><response>SUCCESS</response></commandResult>";
    }

    public static String importUsers(Properties request, String site) {
        String response = "<commandResult><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            String the_dir = request.getProperty("the_dir");
            if (request.getProperty("user_type").equals("Import CrushFTP3 Users...")) {
                ServerStatus.thisObj.common_code.ConvertCrushFTP3Users(the_dir, String.valueOf(request.getProperty("serverGroup")) + "/", ServerStatus.SG("password_encryption"), "");
            } else if (request.getProperty("user_type").equals("Import Folders As Users...")) {
                ServerStatus.thisObj.common_code.ConvertFolderUsers(the_dir, String.valueOf(request.getProperty("serverGroup")) + "/");
            } else if (request.getProperty("user_type").equals("Import Mac OS X Users...")) {
                ServerStatus.thisObj.common_code.ConvertOSXUsers(String.valueOf(request.getProperty("serverGroup")) + "/");
            } else if (request.getProperty("user_type").equals("Import Serv-U Users...")) {
                ServerStatus.thisObj.common_code.ConvertServUUsers(the_dir, String.valueOf(request.getProperty("serverGroup")) + "/");
            } else if (request.getProperty("user_type").equals("Import BulletProof Users...")) {
                ServerStatus.thisObj.common_code.ConvertBPFTPsers(the_dir, String.valueOf(request.getProperty("serverGroup")) + "/");
            } else if (request.getProperty("user_type").equals("Import Rumpus Users...")) {
                ServerStatus.thisObj.common_code.ConvertRumpusUsers(the_dir, String.valueOf(request.getProperty("serverGroup")) + "/");
            } else if (request.getProperty("user_type").equals("Import ProFTPD Users...")) {
                ServerStatus.thisObj.common_code.ConvertPasswdUsers(the_dir, String.valueOf(request.getProperty("serverGroup")) + "/", "CRYPT3:");
            } else if (request.getProperty("user_type").equals("Import VSFTPD MD5Crypt Users...")) {
                ServerStatus.thisObj.common_code.ConvertPasswdUsers(the_dir, String.valueOf(request.getProperty("serverGroup")) + "/", "MD5CRYPT:");
            } else if (request.getProperty("user_type").equals("Import VSFTPD SHA512Crypt Users...")) {
                ServerStatus.thisObj.common_code.ConvertPasswdUsers(the_dir, String.valueOf(request.getProperty("serverGroup")) + "/", "SHA512CRYPT:");
            } else if (request.getProperty("user_type").equals("Import ProFTPD Groups...")) {
                ServerStatus.thisObj.common_code.ConvertProFTPDGroups(request.getProperty("serverGroup"), the_dir, String.valueOf(request.getProperty("serverGroup")) + "/");
            } else if (request.getProperty("user_type").equals("Import Tab Delimited Text...")) {
                ServerStatus.thisObj.common_code.convertTabDelimited(the_dir, String.valueOf(request.getProperty("serverGroup")) + "/");
            } else if (request.getProperty("user_type").equals("Import FileZilla Users...")) {
                ServerStatus.thisObj.common_code.convertFilezilla(the_dir, String.valueOf(request.getProperty("serverGroup")) + "/");
            } else if (request.getProperty("user_type").equals("Import WingFTP Users...")) {
                ServerStatus.thisObj.common_code.convertWingFTP(the_dir, String.valueOf(request.getProperty("serverGroup")) + "/", "MD5:");
            } else if (request.getProperty("user_type").equals("Import Gene6 Users...")) {
                ServerStatus.thisObj.common_code.ConvertGene6Users(the_dir, String.valueOf(request.getProperty("serverGroup")) + "/");
            } else if (request.getProperty("user_type").equals("Import CSV...")) {
                if (request.getProperty("preview", "false").equals("true")) {
                    String snippet = "";
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File_S(the_dir))));
                    String data = "";
                    int lines = 0;
                    while ((data = br.readLine()) != null && lines++ < 5) {
                        snippet = String.valueOf(snippet) + data + "\r\n";
                    }
                    br.close();
                    response = String.valueOf(response) + snippet;
                    response = String.valueOf(response) + "</response></commandResult>";
                    return response;
                }
                response = String.valueOf(response) + Common.importCSV(request, String.valueOf(request.getProperty("serverGroup")) + "/");
            } else {
                throw new Exception("User import type not supported:" + request.getProperty("user_type"));
            }
            response = String.valueOf(response) + "SUCCESS: Users imported.";
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 0, e);
            response = String.valueOf(response) + e.toString();
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String sendPassEmail(Properties request, SessionCrush session, String site) {
        String subject = ServerStatus.SG("emailReminderSubjectText");
        String body = ServerStatus.SG("emailReminderBodyText");
        String from = ServerStatus.SG("smtp_from");
        String reply_to = "";
        String cc = "";
        String bcc = "";
        String templateName = Common.url_decode(request.getProperty("email_template", "").replace('+', ' ')).trim();
        Common.debug(2, "Looking up template info for admin pass email:" + templateName);
        Properties template = Common.get_email_template(templateName);
        if (template != null) {
            Common.debug(2, "Found template:" + template);
            subject = request.getProperty("emailSubject", "").equals("") ? template.getProperty("emailSubject", "") : Common.url_decode(request.getProperty("emailSubject", "").replace('+', ' ')).trim();
            body = request.getProperty("emailBody", "").equals("") ? template.getProperty("emailBody", "") : Common.url_decode(request.getProperty("emailBody", "").replace('+', ' ')).trim();
            from = request.getProperty("emailFrom", "").equals("") ? template.getProperty("emailFrom", from) : Common.url_decode(request.getProperty("emailFrom", "").replace('+', ' ')).trim();
            cc = request.getProperty("emailCC", "").equals("") ? template.getProperty("emailCC", "") : Common.url_decode(request.getProperty("emailCC", "").replace('+', ' ')).trim();
            reply_to = request.getProperty("emailReplyTo", "").equals("") ? template.getProperty("emailReplyTo", "") : Common.url_decode(request.getProperty("emailReplyTo", "").replace('+', ' ')).trim();
            bcc = request.getProperty("emailBCC", "").equals("") ? template.getProperty("emailBCC", "") : Common.url_decode(request.getProperty("emailBCC", "").replace('+', ' ')).trim();
        }
        String response = "<commandResult><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            String to = request.getProperty("user_email");
            if (from.equals("")) {
                from = to;
            }
            from = Common.replace_str(Common.replace_str(from, "&gt;", ">"), "&lt;", "<");
            to = Common.replace_str(Common.replace_str(to, "&gt;", ">"), "&lt;", "<");
            cc = Common.replace_str(Common.replace_str(cc, "&gt;", ">"), "&lt;", "<");
            bcc = Common.replace_str(Common.replace_str(bcc, "&gt;", ">"), "&lt;", "<");
            if (ServerStatus.SG("smtp_server").equals("")) {
                response = String.valueOf(response) + LOC.G("This server is not configured to send email password reminders.") + "\r\n";
            } else if (!to.equals("")) {
                Properties userTemp = UserTools.ut.getUser(request.getProperty("serverGroup"), request.getProperty("user_name"), false);
                body = Common.replace_str(body, "%user_name%", request.getProperty("user_name"));
                body = Common.replace_str(body, "%username%", request.getProperty("user_name"));
                body = Common.replace_str(body, "%user_pass%", request.getProperty("user_pass"));
                body = Common.replace_str(body, "%user_password%", request.getProperty("user_pass"));
                body = Common.replace_str(body, "%user_email%", request.getProperty("user_email"));
                body = Common.replace_str(body, "%user_first_name%", request.getProperty("user_first_name"));
                body = Common.replace_str(body, "%user_last_name%", request.getProperty("user_last_name"));
                if (session != null) {
                    body = Common.replace_str(body, "%admin_user_name%", session.user.getProperty("username"));
                }
                if (session != null) {
                    body = Common.replace_str(body, "%admin_username%", session.user.getProperty("username"));
                }
                body = Common.replace_str(body, "{user_name}", request.getProperty("user_name"));
                body = Common.replace_str(body, "{username}", request.getProperty("user_name"));
                body = Common.replace_str(body, "{user_pass}", request.getProperty("user_pass"));
                body = Common.replace_str(body, "{user_password}", request.getProperty("user_pass"));
                body = Common.replace_str(body, "{user_email}", request.getProperty("user_email"));
                body = Common.replace_str(body, "{user_first_name}", request.getProperty("user_first_name"));
                body = Common.replace_str(body, "{user_last_name}", request.getProperty("user_last_name"));
                if (session != null) {
                    body = Common.replace_str(body, "{admin_user_name}", session.user.getProperty("username"));
                }
                if (session != null) {
                    body = Common.replace_str(body, "{admin_username}", session.user.getProperty("username"));
                }
                body = ServerStatus.change_vars_to_values_static(body, userTemp, new Properties(), session);
                subject = Common.replace_str(subject, "%user_name%", request.getProperty("user_name"));
                subject = Common.replace_str(subject, "%username%", request.getProperty("user_name"));
                subject = Common.replace_str(subject, "%user_pass%", request.getProperty("user_pass"));
                subject = Common.replace_str(subject, "%user_password%", request.getProperty("user_pass"));
                subject = Common.replace_str(subject, "%user_email%", request.getProperty("user_email"));
                subject = Common.replace_str(subject, "%user_first_name%", request.getProperty("user_first_name"));
                subject = Common.replace_str(subject, "%user_last_name%", request.getProperty("user_last_name"));
                if (session != null) {
                    subject = Common.replace_str(subject, "%admin_user_name%", session.user.getProperty("username"));
                }
                if (session != null) {
                    subject = Common.replace_str(subject, "%admin_username%", session.user.getProperty("username"));
                }
                subject = Common.replace_str(subject, "{user_name}", request.getProperty("user_name"));
                subject = Common.replace_str(subject, "{username}", request.getProperty("user_name"));
                subject = Common.replace_str(subject, "{user_pass}", request.getProperty("user_pass"));
                subject = Common.replace_str(subject, "{user_password}", request.getProperty("user_pass"));
                subject = Common.replace_str(subject, "{user_email}", request.getProperty("user_email"));
                subject = Common.replace_str(subject, "{user_first_name}", request.getProperty("user_first_name"));
                subject = Common.replace_str(subject, "{user_last_name}", request.getProperty("user_last_name"));
                if (session != null) {
                    subject = Common.replace_str(subject, "{admin_user_name}", session.user.getProperty("username"));
                }
                if (session != null) {
                    subject = Common.replace_str(subject, "{admin_username}", session.user.getProperty("username"));
                }
                subject = ServerStatus.change_vars_to_values_static(subject, userTemp, new Properties(), session);
                Properties email_info = new Properties();
                email_info.put("server", ServerStatus.SG("smtp_server"));
                email_info.put("user", ServerStatus.SG("smtp_user"));
                email_info.put("pass", ServerStatus.SG("smtp_pass"));
                email_info.put("ssl", ServerStatus.SG("smtp_ssl"));
                email_info.put("html", ServerStatus.SG("smtp_html"));
                email_info.put("from", from);
                email_info.put("reply_to", reply_to);
                email_info.put("to", to);
                email_info.put("cc", cc);
                email_info.put("bcc", bcc);
                email_info.put("subject", subject);
                email_info.put("body", body);
                ServerStatus.thisObj.sendEmail(email_info);
                response = String.valueOf(response) + email_info.getProperty("results", "") + "\r\n";
                if (response.toUpperCase().indexOf("ERROR") < 0) {
                    response = "<commandResult><response>" + LOC.G("An email was just sent to the email address associated with this user.") + "\r\n";
                }
                Log.log("HTTP_SERVER", 0, String.valueOf(LOC.G("Password Emailed to user:")) + request.getProperty("user_name") + "  " + to + "   " + LOC.G("Email Result:") + response);
            }
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 1, ee);
            response = String.valueOf(response) + "Error:" + Common.url_encode(ee.toString()) + "\r\n";
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static String testKeystore(Properties request, String site) {
        String response = "<commandResult><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            if (request.getProperty("dump_pass", "false").equals("true")) {
                throw new Exception("\r\nkeystore:" + ServerStatus.thisObj.common_code.decode_pass(request.getProperty("keystorePass")) + "\r\n" + "key:" + ServerStatus.thisObj.common_code.decode_pass(request.getProperty("keyPass")));
            }
            if (!request.getProperty("keystorePath").equals("builtin") && !com.crushftp.client.Common.dmz_mode) {
                if (ServerStatus.BG("v10_beta") && (request.getProperty("keystorePath").toUpperCase().startsWith("FILE://") || !new VRL(request.getProperty("keystorePath")).getProtocol().toUpperCase().equals("FILE"))) {
                    VRL vrl = new VRL(request.getProperty("keystorePath"));
                    GenericClient c = Common.getClient(Common.getBaseUrl(vrl.toString()), "SSL Key store load", new Vector());
                    if (vrl.getConfig() != null && vrl.getConfig().size() > 0) {
                        c.setConfigObj(vrl.getConfig());
                    }
                    c.login(vrl.getUsername(), vrl.getPassword(), null);
                    Properties p = c.stat(vrl.getPath());
                    if (p == null) throw new IOException("Keystore file not found:" + vrl.getPath());
                    if (Long.parseLong(p.getProperty("size", "0")) <= 0L) {
                        throw new IOException("Keystore file not found:" + vrl.getPath());
                    }
                } else {
                    RandomAccessFile testIn = new RandomAccessFile(new File_S(request.getProperty("keystorePath")), "r");
                    if (testIn.length() == 0L) {
                        throw new IOException("Keystore file not found:" + request.getProperty("keystorePath"));
                    }
                    testIn.close();
                }
            }
            SSLServerSocket ss = (SSLServerSocket)ServerStatus.thisObj.common_code.getServerSocket(0, "127.0.0.1", request.getProperty("keystorePath"), request.getProperty("keystorePass"), request.getProperty("keyPass"), "", false, 10, false, true, null);
            SSLSocketFactory factory = new Common().getSSLContext("builtin", "builtin", "", "", "TLS", false, true).getSocketFactory();
            final SSLSocket s1 = (SSLSocket)factory.createSocket(new Socket("127.0.0.1", ss.getLocalPort()), "127.0.0.1", ss.getLocalPort(), true);
            com.crushftp.client.Common.configureSSLTLSSocket(s1, "TLSv1,TLSv1.1,TLSv1,TLSv1.2");
            final SSLSocket s2 = (SSLSocket)ss.accept();
            ss.close();
            s1.setSoTimeout(1000);
            s2.setSoTimeout(1000);
            s2.setUseClientMode(false);
            s1.setUseClientMode(true);
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        s1.startHandshake();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            });
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        s2.startHandshake();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            });
            s1.getOutputStream().write("1".getBytes());
            s2.getOutputStream().write("1".getBytes());
            s1.close();
            s2.close();
            response = String.valueOf(response) + "Cert test successful.";
            return String.valueOf(response) + "</response></commandResult>";
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 1, ee);
            response = String.valueOf(response) + "Cert test failed:";
            try {
                if (Cipher.getMaxAllowedKeyLength("AES") < Integer.MAX_VALUE) {
                    response = AdminControls.getCipherFix(response);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            response = String.valueOf(response) + "\r\n\r\n" + Common.url_encode(ee.toString());
            Log.log("SERVER", 0, response);
        }
        return String.valueOf(response) + "</response></commandResult>";
    }

    public static String getCipherFix(String response) {
        String help_url = "";
        if (System.getProperty("java.version").startsWith("1.6")) {
            help_url = "http://www.oracle.com/technetwork/java/javase/downloads/jce-6-download-429243.html";
        } else if (System.getProperty("java.version").startsWith("1.7")) {
            help_url = "http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html";
        } else if (System.getProperty("java.version").startsWith("1.8")) {
            help_url = "http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html";
        }
        response = String.valueOf(response) + "Your java install needs to have the policy files installed allowing for strong cryptography.  \r\n\r\nPlease download them from here:\r\n" + help_url;
        try {
            response = String.valueOf(response) + "\r\n\r\nCopy them here:\r\n" + new File(String.valueOf(System.getProperty("java.home")) + "/lib/security/").getCanonicalPath();
            Desktop.getDesktop().open(new File(String.valueOf(System.getProperty("java.home")) + "/lib/security/"));
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return response;
    }

    public static String generateSSL(Properties request, String site) {
        String response = "<commandResult><response>";
        String csr = "";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            csr = SSLKeyManager.buildNew(request.getProperty("key_alg"), Integer.parseInt(request.getProperty("key_size")), request.getProperty("sig_alg"), Integer.parseInt(request.getProperty("days")), request.getProperty("cn"), request.getProperty("ou"), request.getProperty("o"), request.getProperty("l"), request.getProperty("st"), request.getProperty("c"), request.getProperty("e"), request.getProperty("keystore_path"), com.crushftp.client.Common.encryptDecrypt(request.getProperty("keystore_pass", request.getProperty("key_pass")), false), com.crushftp.client.Common.encryptDecrypt(request.getProperty("keystore_pass", request.getProperty("key_pass")), false), request.getProperty("sans", ""));
            response = String.valueOf(response) + "\r\n" + csr + "\r\n";
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 0, ee);
            response = String.valueOf(response) + "Error:" + Common.url_encode(ee.toString()) + "\r\n";
            response = String.valueOf(response) + "\r\n" + Common.url_encode("ERROR:" + request.getProperty("keystore_path") + " failed to be generated.") + "\r\n";
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String generateCSR(Properties request, String site) {
        String response = "<commandResult><response>";
        String csr = "";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            String keystore_path = new VRL(request.getProperty("keystore_path")).getPath();
            csr = SSLKeyManager.makeCSR(keystore_path, ServerStatus.thisObj.common_code.decode_pass(request.getProperty("keystore_pass", request.getProperty("key_pass"))), ServerStatus.thisObj.common_code.decode_pass(request.getProperty("keystore_pass", request.getProperty("key_pass"))));
            response = String.valueOf(response) + "\r\n" + csr + "\r\n";
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 0, ee);
            response = String.valueOf(response) + "Error:" + Common.url_encode(ee.toString()) + "\r\n";
            response = String.valueOf(response) + "\r\n" + Common.url_encode("ERROR:" + request.getProperty("keystore_path") + " failed to be generated.") + "\r\n";
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String importReply(Properties request, String site) {
        String response = "<commandResult><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            String keystore_path = new VRL(request.getProperty("keystore_path")).getPath();
            String import_path = new VRL(request.getProperty("import_path")).getPath();
            String trusted_paths = new VRL(request.getProperty("trusted_paths")).getPath();
            String result = SSLKeyManager.importReply(keystore_path, com.crushftp.client.Common.encryptDecrypt(request.getProperty("keystore_pass", request.getProperty("key_pass")), false), com.crushftp.client.Common.encryptDecrypt(request.getProperty("keystore_pass", request.getProperty("key_pass")), false), import_path, trusted_paths);
            response = String.valueOf(response) + "\r\n" + result + "\r\n";
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 0, ee);
            response = String.valueOf(response) + "Error:" + Common.url_encode(ee.toString()) + "\r\n";
            response = String.valueOf(response) + "\r\n" + Common.url_encode("ERROR:" + request.getProperty("import_path") + " failed to be imported into " + request.getProperty("keystore_path") + ".") + "\r\n";
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String listSSL(Properties request, String site) {
        String response = "<commandResult><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            Vector v = SSLKeyManager.list(request.getProperty("keystore_path"), com.crushftp.client.Common.encryptDecrypt(request.getProperty("keystore_pass"), false));
            s = Common.getXMLString(v, "SSL", null).trim();
            response = String.valueOf(response) + s.substring(s.indexOf("<SSL"));
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 0, ee);
            response = String.valueOf(response) + "Error:" + Common.url_encode(ee.toString()) + "\r\n";
            response = String.valueOf(response) + "\r\n" + Common.url_encode("ERROR:" + request.getProperty("keystore_path") + " failed to be listed.") + "\r\n";
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String deleteSSL(Properties request, String site) {
        String response = "<commandResult><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            boolean ok = SSLKeyManager.delete(request.getProperty("keystore_path"), com.crushftp.client.Common.encryptDecrypt(request.getProperty("keystore_pass"), false), request.getProperty("alias"));
            response = String.valueOf(response) + "\r\n" + ok + "\r\n";
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 0, ee);
            response = String.valueOf(response) + "Error:" + Common.url_encode(ee.toString()) + "\r\n";
            response = String.valueOf(response) + "\r\n" + Common.url_encode("ERROR:" + request.getProperty("alias") + " failed to be deleted.") + "\r\n";
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String renameSSL(Properties request, String site) {
        String response = "<commandResult><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            boolean ok = SSLKeyManager.rename(request.getProperty("keystore_path"), com.crushftp.client.Common.encryptDecrypt(request.getProperty("keystore_pass"), false), request.getProperty("alias1"), request.getProperty("alias2"));
            response = String.valueOf(response) + "\r\n" + ok + "\r\n";
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 0, ee);
            response = String.valueOf(response) + "Error:" + Common.url_encode(ee.toString()) + "\r\n";
            response = String.valueOf(response) + "\r\n" + Common.url_encode("ERROR:" + request.getProperty("alias1") + " failed to be generated.") + "\r\n";
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String exportSSL(Properties request, String site) {
        String response = "<commandResult><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            s = SSLKeyManager.export(request.getProperty("keystore_path"), com.crushftp.client.Common.encryptDecrypt(request.getProperty("keystore_pass"), false), request.getProperty("alias"));
            response = String.valueOf(response) + "\r\n" + s + "\r\n";
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 0, ee);
            response = String.valueOf(response) + "Error:" + Common.url_encode(ee.toString()) + "\r\n";
            response = String.valueOf(response) + "\r\n" + Common.url_encode("ERROR:" + request.getProperty("alias") + " failed to be exported.") + "\r\n";
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String addPrivateSSL(Properties request, String site) {
        String response = "<commandResult><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            SSLKeyManager.addPrivate(request.getProperty("keystore_path"), com.crushftp.client.Common.encryptDecrypt(request.getProperty("keystore_pass"), false), request.getProperty("alias"), request.getProperty("key_path"), com.crushftp.client.Common.encryptDecrypt(request.getProperty("key_pass"), false));
            response = String.valueOf(response) + "\r\nSUCCESS\r\n";
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 0, ee);
            response = String.valueOf(response) + "Error:" + Common.url_encode(ee.toString()) + "\r\n";
            response = String.valueOf(response) + "\r\n" + Common.url_encode("ERROR:" + request.getProperty("alias") + " failed to be generated.") + "\r\n";
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String addPublicSSL(Properties request, String site) {
        String response = "<commandResult><response>";
        try {
            String s = AdminControls.handleInstance(request, site);
            if (s != null) {
                return s;
            }
            SSLKeyManager.addPublic(request.getProperty("keystore_path"), com.crushftp.client.Common.encryptDecrypt(request.getProperty("keystore_pass"), false), request.getProperty("alias"), request.getProperty("key_path"));
            response = String.valueOf(response) + "\r\nSUCCESS\r\n";
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 0, ee);
            response = String.valueOf(response) + "Error:" + Common.url_encode(ee.toString()) + "\r\n";
            response = String.valueOf(response) + "\r\n" + Common.url_encode("ERROR:" + request.getProperty("alias") + " failed to be generated.") + "\r\n";
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String telnetSocket(Properties request, String site) {
        String response;
        block17: {
            response = "<commandResult><response>";
            try {
                String s = AdminControls.handleInstance(request, site);
                if (s != null) {
                    return s;
                }
                Socket tmp = (Socket)tmp_telnet_sockets.get(request.getProperty("id", ""));
                try {
                    if (request.getProperty("sub_command", "").equals("connect")) {
                        tmp = new Socket();
                        tmp.setSoTimeout(5000);
                        tmp.connect(new InetSocketAddress(request.getProperty("host").trim(), Integer.parseInt(request.getProperty("port", "").trim())));
                        String id = Common.makeBoundary(3);
                        tmp.setSoTimeout(500);
                        tmp_telnet_sockets.put(id, tmp);
                        response = String.valueOf(response) + "<id>" + id + "</id>";
                        response = String.valueOf(response) + "<data>Connected.\r\n</data>";
                        break block17;
                    }
                    if (tmp == null) {
                        response = String.valueOf(response) + "<error>Not connected.\r\n</error>";
                        break block17;
                    }
                    if (request.getProperty("sub_command", "").equals("read")) {
                        String result = "";
                        byte[] b = new byte[16384];
                        try {
                            int bytesRead = tmp.getInputStream().read(b);
                            if (bytesRead > 0) {
                                result = new String(b, 0, bytesRead);
                            } else if (bytesRead < 0) {
                                tmp.close();
                                tmp_telnet_sockets.remove(request.getProperty("id"));
                                result = "Socket Closed.";
                            }
                        }
                        catch (SocketTimeoutException socketTimeoutException) {
                            // empty catch block
                        }
                        response = String.valueOf(response) + "<data>" + Common.url_encode(result) + "</data>";
                        break block17;
                    }
                    if (request.getProperty("sub_command", "").equals("write")) {
                        tmp.getOutputStream().write((String.valueOf(request.getProperty("data")) + "\r\n").getBytes());
                        response = String.valueOf(response) + "<data></data>";
                    } else if (request.getProperty("sub_command", "").equals("close")) {
                        tmp.close();
                        tmp_telnet_sockets.remove(request.getProperty("id"));
                        response = String.valueOf(response) + "<data>Closed.\r\n</data>";
                    }
                }
                catch (Exception e) {
                    Log.log("HTTP_SERVER", 0, e);
                    response = String.valueOf(response) + "<error>ERROR:" + Common.url_encode("" + e) + "</error>";
                    if (tmp != null) {
                        tmp.close();
                    }
                    tmp_telnet_sockets.remove(request.getProperty("id"));
                }
            }
            catch (Exception ee) {
                Log.log("HTTP_SERVER", 0, ee);
                response = String.valueOf(response) + "Error:" + Common.url_encode(ee.toString()) + "\r\n";
            }
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static Object stripUserList(Object o) {
        Vector v = (Vector)((Vector)o).clone();
        Vector<Object> vv = new Vector<Object>();
        int x = 0;
        while (x < v.size()) {
            Object o2 = AdminControls.stripUser(v.elementAt(x));
            ((Properties)o2).remove("user_log");
            vv.addElement(o2);
            ++x;
        }
        return vv;
    }

    public static Object stripUser(Object o) {
        Properties p2 = (Properties)((Properties)o).clone();
        p2.remove("stat");
        p2.remove("session");
        p2.remove("session_uploads");
        p2.remove("session_downloads");
        p2.remove("failed_commands");
        p2.remove("lastUploadStats");
        p2.remove("current_password");
        p2.remove("post_parameters");
        return p2;
    }

    public static VFS processVFSSubmission(Properties request, String username, String site, SessionCrush thisSession, boolean real_update, StringBuffer log_summary) throws Exception {
        VFS tempVFS;
        Properties virtual = null;
        Properties virtual_orig = null;
        Properties permission0 = null;
        if (request.containsKey("permissions")) {
            permission0 = request.get("permissions") != null && request.get("permissions") instanceof Properties ? (Properties)request.get("permissions") : (Properties)Common.readXMLObjectError(new ByteArrayInputStream(Common.url_decode(Common.replace_str(request.getProperty("permissions").replace('+', ' '), "%26", "&amp;")).getBytes("UTF8")));
        } else {
            VFS tempVFS2 = UserTools.ut.getVFS(request.getProperty("serverGroup"), username);
            permission0 = tempVFS2.getPermission0();
        }
        boolean remove = false;
        if (request.getProperty("data_action", "").startsWith("update_vfs")) {
            remove = request.getProperty("data_action").equals("update_vfs_remove");
            tempVFS = UserTools.ut.getVFS(request.getProperty("serverGroup"), username);
            Properties permission_current = tempVFS.getPermission0();
            Enumeration<Object> keys = permission0.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                if (remove && !key.equals("/")) {
                    permission_current.remove(key);
                    continue;
                }
                if (key.equals("/")) continue;
                permission_current.put(key, permission0.getProperty(key));
            }
            permission0 = permission_current;
            virtual = (Properties)tempVFS.homes.elementAt(0);
        } else {
            virtual = UserTools.generateEmptyVirtual();
        }
        try {
            tempVFS = UserTools.ut.getVFS(request.getProperty("serverGroup"), username);
            virtual_orig = (Properties)tempVFS.homes.elementAt(0);
        }
        catch (Exception e) {
            virtual_orig = UserTools.generateEmptyVirtual();
        }
        Vector<Properties> permissions = new Vector<Properties>();
        permissions.addElement(permission0);
        virtual.put("vfs_permissions_object", permissions);
        if (request.containsKey("vfs_items")) {
            Object o = null;
            o = request.get("vfs_items") != null && (request.get("vfs_items") instanceof Properties || request.get("vfs_items") instanceof Vector) ? request.get("vfs_items") : Common.readXMLObjectError(new ByteArrayInputStream(Common.url_decode(Common.replace_str(request.getProperty("vfs_items").replace('+', ' '), "%26", "&amp;")).getBytes("UTF8")));
            if (o instanceof Properties) {
                o = null;
            }
            Vector vfs_items = (Vector)o;
            int x = 0;
            while (vfs_items != null && x < vfs_items.size()) {
                Properties p = (Properties)vfs_items.elementAt(x);
                Vector v = (Vector)p.get("vfs_item");
                if (!p.getProperty("name").equals("VFS") || !p.getProperty("path").equals("")) {
                    Log.log("HTTP_SERVER", 2, "" + p);
                    String path = com.crushftp.client.Common.dots(String.valueOf(p.getProperty("path").substring(1)) + p.getProperty("name"));
                    if (site.indexOf("(CONNECT)") < 0 && site.indexOf("(USER_VIEW)") < 0 && site.indexOf("(USER_EDIT)") < 0 && site.indexOf("(JOB_EDIT)") < 0) {
                        String groupName = thisSession.getAdminGroupName(request);
                        Properties pp = new Properties();
                        pp.put("virtualPath", "/" + path);
                        pp.put("name", p.getProperty("name"));
                        if (v.size() == 0 || v.size() == 1 && ((Properties)v.elementAt(0)).getProperty("url", "").equals("")) {
                            pp.put("type", "DIR");
                            if (remove) {
                                virtual.remove("/" + path);
                            } else {
                                virtual.put("/" + path, pp);
                            }
                        } else if (UserTools.parentPathOK(request.getProperty("serverGroup_backup", request.getProperty("serverGroup")), groupName, ((Properties)v.elementAt(0)).getProperty("url", ""))) {
                            pp.put("type", "FILE");
                            pp.put("vItems", v);
                            if (remove) {
                                virtual.remove("/" + path);
                            } else {
                                virtual.put("/" + path, pp);
                            }
                        } else {
                            Log.log("SERVER", 0, new Date() + ":User " + thisSession.uiSG("user_name") + " Violated Security Constraint for a USER_ADMIN:" + groupName + ".  URL of VFS item not allowed:" + ((Properties)v.elementAt(0)).getProperty("url", ""));
                        }
                    } else {
                        Properties pp = new Properties();
                        pp.put("virtualPath", "/" + path);
                        if (remove) {
                            virtual.remove("/" + path);
                        } else {
                            virtual.put("/" + path, pp);
                        }
                        pp.put("name", p.getProperty("name"));
                        if (v.size() == 0 || v.size() == 1 && ((Properties)v.elementAt(0)).getProperty("url", "").equals("")) {
                            pp.put("type", "DIR");
                            if (v.size() == 1 && ((Properties)v.elementAt(0)).getProperty("url", "").equals("") && !((Properties)v.elementAt(0)).getProperty("modified", "0").equals("0")) {
                                pp.put("modified", ((Properties)v.elementAt(0)).getProperty("modified", "0"));
                            }
                        } else {
                            pp.put("type", "FILE");
                            pp.put("vItems", v);
                        }
                    }
                }
                ++x;
            }
            Properties pp = new Properties();
            pp.put("type", "DIR");
            pp.put("virtualPath", "");
            pp.put("name", "VFS");
            virtual.put("/", pp);
        } else {
            VFS tempVFS3 = UserTools.ut.getVFS(request.getProperty("serverGroup"), username);
            Properties virtual2 = (Properties)tempVFS3.homes.elementAt(0);
            virtual2.remove("vfs_permissions_object");
            virtual.putAll((Map<?, ?>)virtual2);
        }
        if (real_update) {
            Common.updateObjectLog(virtual, virtual_orig, "users/" + request.getProperty("serverGroup") + "/" + username + "/vfs/", false, log_summary);
        }
        return VFS.getVFS(virtual);
    }

    public static Properties getListingInfo(Vector listing, String the_dir) {
        Vector<Properties> items = new Vector<Properties>();
        try {
            int x = 0;
            while (x < listing.size()) {
                Properties list_item = (Properties)listing.elementAt(x);
                Log.log("HTTP_SERVER", 3, "Adding:" + list_item.getProperty("name"));
                list_item.put("preview", "0");
                list_item.put("sizeFormatted", com.crushftp.client.Common.format_bytes2(list_item.getProperty("size")));
                list_item.put("modified", list_item.getProperty("modified", "0"));
                list_item.remove("url");
                list_item.put("itemType", list_item.getProperty("type"));
                list_item.put("root_dir", list_item.getProperty("root_dir", "/"));
                items.addElement(list_item);
                ++x;
            }
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
        }
        Common.do_sort(items, "name");
        int x = 0;
        while (x < items.size()) {
            Properties lp = (Properties)items.elementAt(x);
            if (lp.getProperty("dir", "").indexOf("\"") >= 0) {
                lp.put("dir", lp.getProperty("dir", "").replaceAll("\\\"", "%22"));
            }
            if (lp.getProperty("name", "").indexOf("\"") >= 0) {
                lp.put("name", lp.getProperty("name", "").replaceAll("\\\"", "%22"));
            }
            if (lp.getProperty("name", "").endsWith(" ") || lp.getProperty("name", "").startsWith(" ")) {
                lp.put("name", lp.getProperty("name", "").replaceAll(" ", "%20"));
            }
            if (lp.getProperty("path", "").indexOf("\"") >= 0) {
                lp.put("path", lp.getProperty("path", "").replaceAll("\\\"", "%22"));
            }
            if (lp.getProperty("root_dir", "").indexOf("\"") >= 0) {
                lp.put("root_dir", lp.getProperty("root_dir", "").replaceAll("\\\"", "%22"));
            }
            String itemName = lp.getProperty("name");
            String itemPath = String.valueOf(the_dir) + lp.getProperty("name");
            String root_dir = lp.getProperty("root_dir");
            String href_path = String.valueOf(lp.getProperty("root_dir")) + lp.getProperty("name");
            if (href_path.startsWith("//") && !href_path.startsWith("////")) {
                href_path = "//" + href_path;
            }
            lp.put("source", "/WebInterface/function/?command=getPreview&size=3&path=" + itemPath);
            lp.put("href_path", href_path);
            lp.put("root_dir", root_dir);
            lp.put("name", itemName);
            ++x;
        }
        Properties listingProp = new Properties();
        listingProp.put("privs", "(read)(view)");
        listingProp.put("path", the_dir);
        listingProp.put("listing", items);
        return listingProp;
    }

    public static String getTempAccounts(Properties request, String site) {
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 20);
                return p.get("data").toString();
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return "FAILURE:Job not found.";
            }
        }
        try {
            Vector<Properties> items = new Vector<Properties>();
            File_U[] accounts = (File_U[])new File_U(String.valueOf(ServerStatus.SG("temp_accounts_path")) + "accounts/").listFiles();
            SimpleDateFormat ex_sdf = new SimpleDateFormat("MMddyyyyHHmm", Locale.US);
            SimpleDateFormat expire_sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm aa", Locale.US);
            if (accounts != null) {
                int x = 0;
                while (x < accounts.length) {
                    try {
                        File_U f = accounts[x];
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
                            Properties info = (Properties)Common.readXMLObject_U(String.valueOf(f.getPath()) + "/INFO.XML");
                            pp.put("expire", expire_sdf.format(ex_sdf.parseObject(pp.getProperty("EX"))));
                            info.putAll((Map<?, ?>)pp);
                            Enumeration<Object> keys = info.keys();
                            while (keys.hasMoreElements()) {
                                String key = keys.nextElement().toString();
                                if (!key.startsWith("ldap_")) continue;
                                info.remove(key);
                            }
                            info.remove("web_customizations");
                            info.remove("web_buttons");
                            Properties ppp = new Properties();
                            ppp.put("info", info);
                            ppp.put("tempaccount_user", info.get("U"));
                            ppp.put("tempaccount_pass", info.get("P"));
                            ppp.put("tempaccount_folder", f.getName());
                            if (new File_U(String.valueOf(f.getPath()) + "/VFS.XML").exists()) {
                                Properties permissions = (Properties)Common.readXMLObject_U(String.valueOf(f.getPath()) + "/VFS.XML");
                                ppp.put("permissions", permissions);
                            }
                            items.addElement(ppp);
                        }
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 1, e);
                    }
                    ++x;
                }
            }
            Common cfr_ignored_0 = ServerStatus.thisObj.common_code;
            return Common.getXMLString(items, "temp_accounts", null);
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 1, ee);
            return null;
        }
    }

    public static String addTempAccount(Properties request, String site) {
        return AdminControls.addTempAccount(request, site, true);
    }

    public static String addTempAccount(Properties request, String site, boolean replicate) {
        String response;
        block8: {
            if (ServerStatus.BG("replicate_shares") && replicate) {
                Properties p = new Properties();
                p.put("request", request);
                p.put("site", site);
                SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.share.addTempAccount", "info", p);
            }
            request.put("tempaccount_user", com.crushftp.client.Common.dots(request.getProperty("tempaccount_user")));
            request.put("tempaccount_pass", com.crushftp.client.Common.dots(request.getProperty("tempaccount_pass")));
            request.put("tempaccount_folder", com.crushftp.client.Common.dots(request.getProperty("tempaccount_folder")));
            response = "<commandResult><response>";
            if (request.getProperty("instance", "").equals("")) break block8;
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 20);
                return p.get("data").toString();
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return "FAILURE:Job not found.";
            }
        }
        try {
            new File_U(String.valueOf(ServerStatus.SG("temp_accounts_path")) + "accounts/" + request.getProperty("tempaccount_folder") + "/VFS/").mkdirs();
            new File_U(String.valueOf(ServerStatus.SG("temp_accounts_path")) + "storage/" + request.getProperty("tempaccount_user") + request.getProperty("tempaccount_pass")).mkdirs();
            Object permissions = Common.readXMLObject(new ByteArrayInputStream(Common.url_decode(request.getProperty("permissions").replace('+', ' ')).getBytes("UTF8")));
            Properties info = (Properties)Common.readXMLObject(new ByteArrayInputStream(Common.url_decode(request.getProperty("info").replace('+', ' ')).getBytes("UTF8")));
            Common.writeXMLObject_U(String.valueOf(ServerStatus.SG("temp_accounts_path")) + "accounts/" + request.getProperty("tempaccount_folder") + "/VFS.XML", permissions, "VFS");
            Common.writeXMLObject_U(String.valueOf(ServerStatus.SG("temp_accounts_path")) + "accounts/" + request.getProperty("tempaccount_folder") + "/INFO.XML", info, "INFO");
            String[] part_names = request.getProperty("tempaccount_folder").split(",,");
            Date td = new SimpleDateFormat("MM/dd/yyyy hh:mm aa", Locale.US).parse(info.getProperty("expire"));
            String fname = "";
            int x = 0;
            while (x < part_names.length) {
                fname = !part_names[x].startsWith("ex=") ? String.valueOf(fname) + part_names[x] : String.valueOf(fname) + "ex=" + new SimpleDateFormat("MMddyyyyHHmm", Locale.US).format(td);
                if (x < part_names.length) {
                    fname = String.valueOf(fname) + ",,";
                }
                ++x;
            }
            new File_U(String.valueOf(ServerStatus.SG("temp_accounts_path")) + "accounts/" + request.getProperty("tempaccount_folder")).renameTo(new File_U(String.valueOf(ServerStatus.SG("temp_accounts_path")) + "accounts/" + fname));
            response = String.valueOf(response) + "Success.";
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 1, ee);
            response = String.valueOf(response) + "ERROR:" + ee.toString();
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String removeTempAccount(Properties request, String site) {
        return AdminControls.removeTempAccount(request, site, true);
    }

    public static String removeTempAccount(Properties request, String site, boolean replicate) {
        String response;
        block8: {
            if (ServerStatus.BG("replicate_shares") && replicate) {
                Properties p = new Properties();
                p.put("request", request);
                p.put("site", site);
                SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.share.removeTempAccount", "info", p);
            }
            request.put("tempaccount_user", com.crushftp.client.Common.dots(request.getProperty("tempaccount_user")));
            request.put("tempaccount_pass", com.crushftp.client.Common.dots(request.getProperty("tempaccount_pass")));
            request.put("tempaccount_folder", com.crushftp.client.Common.dots(request.getProperty("tempaccount_folder")));
            response = "<commandResult><response>";
            if (request.getProperty("instance", "").equals("")) break block8;
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 20);
                return p.get("data").toString();
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return "FAILURE:Job not found.";
            }
        }
        try {
            Properties account_files = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(AdminControls.getTempAccountFiles(request, site).getBytes("UTF8"))).getRootElement());
            Common.recurseDelete_U(String.valueOf(ServerStatus.SG("temp_accounts_path")) + "accounts/" + request.getProperty("tempaccount_folder"), false);
            Common.recurseDelete_U(String.valueOf(ServerStatus.SG("temp_accounts_path")) + "storage/" + request.getProperty("tempaccount_user") + request.getProperty("tempaccount_pass"), false);
            response = String.valueOf(response) + "Success.";
            Vector files = (Vector)account_files.get("refFiles");
            if (files != null && files.size() > 0) {
                int x = 0;
                while (x < files.size()) {
                    Log.log("SERVER", 0, "Removed shared file or folder: " + files.get(x));
                    ++x;
                }
            }
            Log.log("SERVER", 0, "Removed temporary account: " + request.getProperty("tempaccount_user"));
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 1, ee);
            response = String.valueOf(response) + "ERROR:" + ee.toString();
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String getTempAccountFiles(Properties request, String site) {
        String response;
        block7: {
            request.put("tempaccount_user", com.crushftp.client.Common.dots(request.getProperty("tempaccount_user")));
            request.put("tempaccount_pass", com.crushftp.client.Common.dots(request.getProperty("tempaccount_pass")));
            request.put("tempaccount_folder", com.crushftp.client.Common.dots(request.getProperty("tempaccount_folder")));
            response = "<commandResult><response>";
            if (request.getProperty("instance", "").equals("")) break block7;
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 20);
                return p.get("data").toString();
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return "FAILURE:Job not found.";
            }
        }
        try {
            Properties p = new Properties();
            Vector<String> fileNames = new Vector<String>();
            Vector<String> realUrls = new Vector<String>();
            p.put("fileNames", fileNames);
            p.put("realUrls", realUrls);
            File_U[] files = (File_U[])new File_U(String.valueOf(ServerStatus.SG("temp_accounts_path")) + "accounts/" + request.getProperty("tempaccount_folder") + "/VFS/").listFiles();
            if (files != null) {
                int x = 0;
                while (x < files.length) {
                    fileNames.addElement(files[x].getName());
                    Vector v = (Vector)Common.readXMLObject(files[x]);
                    Properties item = (Properties)v.elementAt(0);
                    realUrls.addElement(new VRL(item.getProperty("url")).safe());
                    ++x;
                }
            }
            Common cfr_ignored_0 = ServerStatus.thisObj.common_code;
            return Common.getXMLString(p, "temp_accounts_files", null, true);
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 1, ee);
            response = String.valueOf(response) + "ERROR:" + ee.toString();
            response = String.valueOf(response) + "</response></commandResult>";
            return response;
        }
    }

    public static String removeTempAccountFile(Properties request, String site) {
        return AdminControls.removeTempAccountFile(request, site, true);
    }

    public static String removeTempAccountFile(Properties request, String site, boolean replicate) {
        String response;
        block6: {
            if (ServerStatus.BG("replicate_shares") && replicate) {
                Properties p = new Properties();
                p.put("request", request);
                p.put("site", site);
                SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.share.removeTempAccountFile", "info", p);
            }
            request.put("tempaccount_user", com.crushftp.client.Common.dots(request.getProperty("tempaccount_user")));
            request.put("tempaccount_pass", com.crushftp.client.Common.dots(request.getProperty("tempaccount_pass")));
            request.put("tempaccount_file", com.crushftp.client.Common.dots(request.getProperty("tempaccount_file")));
            request.put("tempaccount_folder", com.crushftp.client.Common.dots(request.getProperty("tempaccount_folder")));
            response = "<commandResult><response>";
            if (request.getProperty("instance", "").equals("")) break block6;
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 20);
                return p.get("data").toString();
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return "FAILURE:Job not found.";
            }
        }
        try {
            Common.recurseDelete_U(String.valueOf(ServerStatus.SG("temp_accounts_path")) + "accounts/" + request.getProperty("tempaccount_folder") + "/VFS/" + request.getProperty("tempaccount_file"), false);
            Common.recurseDelete_U(String.valueOf(ServerStatus.SG("temp_accounts_path")) + "storage/" + request.getProperty("tempaccount_user") + request.getProperty("tempaccount_pass") + "/" + request.getProperty("tempaccount_file"), false);
            response = String.valueOf(response) + "Success.";
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 1, ee);
            response = String.valueOf(response) + "ERROR:" + ee.toString();
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String addTempAccountFile(Properties request, String site) {
        return AdminControls.addTempAccountFile(request, site, true);
    }

    public static String addTempAccountFile(Properties request, String site, boolean replicate) {
        String response;
        block12: {
            if (ServerStatus.BG("replicate_shares") && replicate) {
                Properties p = new Properties();
                p.put("request", request);
                p.put("site", site);
                SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.share.addTempAccountFile", "info", p);
            }
            request.put("tempaccount_user", com.crushftp.client.Common.dots(request.getProperty("tempaccount_user")));
            request.put("tempaccount_pass", com.crushftp.client.Common.dots(request.getProperty("tempaccount_pass")));
            request.put("tempaccount_file", com.crushftp.client.Common.dots(request.getProperty("tempaccount_file")));
            request.put("tempaccount_folder", com.crushftp.client.Common.dots(request.getProperty("tempaccount_folder")));
            response = "<commandResult><response>";
            if (request.getProperty("instance", "").equals("")) break block12;
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 20);
                return p.get("data").toString();
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return "FAILURE:Job not found.";
            }
        }
        try {
            File_U fileItem = new File_U(request.getProperty("tempaccount_file"));
            if (!fileItem.exists()) {
                response = String.valueOf(response) + "ERROR:File does not exist.";
            } else {
                String userHome = String.valueOf(ServerStatus.SG("temp_accounts_path")) + "accounts/" + request.getProperty("tempaccount_folder") + "/";
                String userStorage = String.valueOf(ServerStatus.SG("temp_accounts_path")) + "storage/" + request.getProperty("tempaccount_user") + request.getProperty("tempaccount_pass") + "/";
                if (request.getProperty("tempaccount_reference", "false").equals("false")) {
                    Common.recurseCopyThreaded_U(fileItem.getPath(), String.valueOf(userStorage) + fileItem.getName() + (fileItem.isDirectory() ? "/" : ""), true, false);
                }
                Properties vItem = new Properties();
                if (request.getProperty("tempaccount_reference", "false").equals("false")) {
                    vItem.put("url", new File_U(String.valueOf(userStorage) + fileItem.getName()).toURI().toURL().toExternalForm());
                } else {
                    vItem.put("url", fileItem.toURI().toURL().toExternalForm());
                }
                vItem.put("type", fileItem.isDirectory() ? "dir" : "file");
                Vector<Properties> v = new Vector<Properties>();
                v.addElement(vItem);
                Common.writeXMLObject_U(String.valueOf(userHome) + "VFS/" + fileItem.getName(), v, "VFS");
                if (request.getProperty("tempaccount_reference", "false").equals("false")) {
                    Thread.sleep(500L);
                }
                response = String.valueOf(response) + "Success.";
            }
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 1, ee);
            response = String.valueOf(response) + "ERROR:" + ee.toString();
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String deleteReplication(Properties request, String site) {
        String response;
        block7: {
            response = "<commandResult><response>";
            if (request.getProperty("instance", "").equals("")) break block7;
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 20);
                return p.get("data").toString();
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return "FAILURE:Replication not found.";
            }
        }
        try {
            String client_id = Common.dots(request.getProperty("client_id"));
            String item_id = Common.dots(request.getProperty("item_id"));
            if (client_id.length() < 3 || item_id.length() < 5) {
                throw new Exception("Invalid client or item id!");
            }
            if (new File_S("./multi_journal/" + client_id + "/" + item_id).exists()) {
                Common.recurseDelete("./multi_journal/" + client_id + "/" + item_id, false);
            }
            ServerStatus.thisObj.server_info.put("replicated_vfs_ping_interval", "0");
            response = String.valueOf(response) + client_id + "/" + item_id + " deleted.";
        }
        catch (Exception ee) {
            Log.log("HTTP_SERVER", 1, ee);
            response = String.valueOf(response) + "ERROR:" + ee.toString();
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static void purgeSync(Properties request, VFS uVFS, String root_dir) throws Exception {
    }

    public static Object getUserVersions(Properties request) throws ParseException {
        String username = Common.url_decode(request.getProperty("username").replace('+', ' '));
        File_S[] folders = (File_S[])new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/").listFiles();
        Vector<Properties> userVersions = new Vector<Properties>();
        int x = 0;
        while (x < folders.length) {
            File_S f = folders[x];
            if (f.getName().startsWith(username) && !f.getName().startsWith(String.valueOf(username) + "~") && f.getName().endsWith(".zip")) {
                Properties user_version = new Properties();
                String version_date = f.getName().substring(f.getName().indexOf("-") + 1, f.getName().indexOf("."));
                SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy_HHmmss", Locale.US);
                Date date = sdf.parse(version_date);
                SimpleDateFormat sdf_readable = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
                user_version.put(f.getName(), sdf_readable.format(date));
                userVersions.addElement(user_version);
            }
            ++x;
        }
        Properties response = new Properties();
        response.put("user_versions", userVersions);
        return response;
    }

    public static Object getDeletedUsers(Properties request) throws ParseException {
        Object files;
        String server_group = Common.url_decode(request.getProperty("serverGroup").replace('+', ' '));
        File_S[] folders = (File_S[])new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/").listFiles();
        Vector user_list = new Vector();
        UserTools.refreshUserList(server_group, user_list);
        Properties deleted_users_files = new Properties();
        int x = 0;
        while (x < folders.length) {
            String username;
            File_S f = folders[x];
            if (!f.getName().contains("~") && f.getName().endsWith(".zip") && !user_list.contains(username = f.getName().substring(0, f.getName().indexOf("-")))) {
                if (!deleted_users_files.containsKey(username)) {
                    files = new Vector();
                    ((Vector)files).add(f);
                    deleted_users_files.put(username, files);
                } else {
                    files = (Vector)deleted_users_files.get(username);
                    ((Vector)files).add(f);
                }
            }
            ++x;
        }
        Vector<Properties> deleted_users = new Vector<Properties>();
        Enumeration<?> e = deleted_users_files.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            files = ((Vector)deleted_users_files.get(key)).toArray();
            Arrays.sort(files, Common.get_file_last_modified_Comparator());
            Properties p = new Properties();
            p.put(key, ((File_S)files[((Object[])files).length - 1]).getName());
            deleted_users.add(p);
        }
        Properties response = new Properties();
        response.put("deleted_users", deleted_users);
        return response;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String setReportSchedules(Properties request, String site) {
        String status = "OK";
        try {
            if (!request.getProperty("instance", "").equals("")) {
                Object object = DMZServerCommon.stop_send_prefs;
                synchronized (object) {
                    String id = Common.makeBoundary();
                    String instance = request.remove("instance").toString();
                    DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
                    Properties p = DMZServerCommon.getResponse(id, 20);
                    if (request.getProperty("key").indexOf("server_settings/") >= 0) {
                        String id2 = Common.makeBoundary();
                        DMZServerCommon.sendCommand(instance, new Properties(), "GET:SERVER_SETTINGS", id2);
                        Properties pp = DMZServerCommon.getResponse(id2, 20);
                        SharedSessionReplicated.send("", "WRITE_PREFS", instance, (Properties)pp.get("data"));
                        Thread.sleep(200L);
                        Common.write_server_settings((Properties)pp.get("data"), instance);
                    }
                    return p.get("data").toString();
                }
            }
            Properties new_schedule = (Properties)Common.readXMLObject(new ByteArrayInputStream(Common.url_decode(request.getProperty("data").replace('+', ' ')).getBytes("UTF8")));
            Vector repoortSchdedules = (Vector)ServerStatus.server_settings.get("reportSchedules");
            String[] keys = request.getProperty("key").split("/");
            int index = Integer.parseInt(keys[2]);
            if (index < repoortSchdedules.size()) {
                Common.updateObjectLog(new_schedule, repoortSchdedules.elementAt(index), request.getProperty("key"), true, new StringBuffer());
            } else {
                repoortSchdedules.add(new_schedule);
            }
            ServerStatus.thisObj.save_server_settings(false);
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            status = "FAILURE:" + e.toString();
        }
        return status;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String deleteReportSchedules(Properties request, String site) {
        String status = "OK";
        try {
            if (!request.getProperty("instance", "").equals("")) {
                Object object = DMZServerCommon.stop_send_prefs;
                synchronized (object) {
                    String id = Common.makeBoundary();
                    String instance = request.remove("instance").toString();
                    DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
                    Properties p = DMZServerCommon.getResponse(id, 20);
                    if (request.getProperty("key").indexOf("server_settings/") >= 0) {
                        String id2 = Common.makeBoundary();
                        DMZServerCommon.sendCommand(instance, new Properties(), "GET:SERVER_SETTINGS", id2);
                        Properties pp = DMZServerCommon.getResponse(id2, 20);
                        SharedSessionReplicated.send("", "WRITE_PREFS", instance, (Properties)pp.get("data"));
                        Thread.sleep(200L);
                        Common.write_server_settings((Properties)pp.get("data"), instance);
                    }
                    return p.get("data").toString();
                }
            }
            String[] keys = request.getProperty("key").split("/");
            int index = Integer.parseInt(keys[2]);
            ((Vector)ServerStatus.server_settings.get("reportSchedules")).remove(index);
            ServerStatus.thisObj.save_server_settings(false);
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            status = "FAILURE:" + e.toString();
        }
        return status;
    }

    public static String setMaxServerMemory(Properties request, String site) {
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties properties = DMZServerCommon.getResponse(id, 20);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        } else {
            com.crushftp.client.Common.update_service_memory(Integer.parseInt(request.getProperty("memory", "512")), "CrushFTP");
        }
        return "";
    }

    public static String setEncryptionPassword(Properties request, String site) {
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
        response = String.valueOf(response) + "<result><response>";
        String pass = Common.url_decode(request.getProperty("encryption_password", ""));
        while (pass.length() % 8 != 0) {
            pass = String.valueOf(pass) + "Z";
        }
        boolean ok = true;
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties properties = DMZServerCommon.getResponse(id, 20);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        } else {
            if (!ServerStatus.SG("encryption_pass_needed_test").equals("")) {
                try {
                    String chars = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
                    String result = com.crushftp.client.Common.encryptDecrypt(ServerStatus.SG("encryption_pass_needed_test"), false, pass);
                    int x = 0;
                    while (x < result.length()) {
                        if (chars.indexOf(String.valueOf(result.charAt(x))) < 0) {
                            ok = false;
                        }
                        ++x;
                    }
                    if (ok && result.length() > 3) {
                        ok = false;
                    }
                    if (!ok) {
                        response = String.valueOf(response) + "Encryption password test failed.";
                    }
                }
                catch (Exception e) {
                    ok = false;
                    response = String.valueOf(response) + "Encryption password error:" + e;
                }
            }
            if (ok) {
                com.crushftp.client.Common.set_encryption_password(pass);
                try {
                    ServerStatus.server_settings.put("encryption_pass_needed_test", com.crushftp.client.Common.encryptDecrypt(Common.makeBoundary(3), true));
                    ServerStatus.thisObj.save_server_settings(false);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        if (ok) {
            ServerStatus.thisObj.stop_all_servers();
            try {
                Thread.sleep(3000L);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
            ServerStatus.thisObj.start_all_servers();
            response = String.valueOf(response) + "Activated";
        }
        response = String.valueOf(response) + "</response></result>";
        return response;
    }

    public static String returnAdminListing(Properties request, SessionCrush thisSession, String site) throws Exception {
        Properties listingProp = AdminControls.getAdminXMLListing(request, thisSession, site);
        String altList = "";
        if (listingProp != null && request.getProperty("format", "").equalsIgnoreCase("JSON")) {
            altList = AgentUI.getJsonList(listingProp, ServerStatus.BG("exif_listings"), true);
        } else if (listingProp != null && request.getProperty("format", "").equalsIgnoreCase("STAT")) {
            altList = AgentUI.getStatList(listingProp);
        }
        String response = "";
        try {
            Common cfr_ignored_0 = ServerStatus.thisObj.common_code;
            response = Common.getXMLString(listingProp, "listingInfo", null);
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
        }
        if (!altList.equals("")) {
            response = String.valueOf(response.substring(0, response.indexOf("</privs>") + "</privs>".length())) + altList + response.substring(response.indexOf("</privs>") + "</privs>".length());
        }
        return response;
    }

    public static String restartProcess(Properties request, String site) {
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, site, "RUN:INSTANCE_ACTION", id);
            try {
                Properties properties = DMZServerCommon.getResponse(id, 5);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        } else {
            ServerStatus.thisObj.restart_crushftp();
        }
        return "";
    }

    public static String saveHttpChallengeToken(Properties request) {
        String result = "Success";
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, "", "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 5);
                result = (String)p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        } else {
            try {
                final String challenge_path = request.getProperty("challenge_path", "");
                final String challenge_token = request.getProperty("challenge_token", "none.txt");
                RandomAccessFile raf = new RandomAccessFile(String.valueOf(challenge_path) + challenge_token, "rw");
                raf.setLength(0L);
                raf.write((byte[])request.remove("authorization"));
                raf.close();
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        block3: {
                            try {
                                Thread.sleep(60000L);
                                if (new File_S(String.valueOf(challenge_path) + challenge_token).exists()) {
                                    new File_S(String.valueOf(challenge_path) + challenge_token).delete();
                                }
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 1, e);
                                if (!new File_S(String.valueOf(challenge_path) + challenge_token).exists()) break block3;
                                new File_S(String.valueOf(challenge_path) + challenge_token).delete();
                            }
                        }
                    }
                }, "Remove http challenge file");
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
                result = "Failed.";
            }
        }
        return result;
    }

    public static Properties putTLSALPNChallengeJKS(Properties request) {
        Properties result = null;
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, "", "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 5);
                result = (Properties)p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        } else {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update((byte[])request.remove("authorization"));
                byte[] acmeValidation = md.digest();
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(2048, SecureRandom.getInstance("SHA1PRNG"));
                KeyPair keypair = keyGen.generateKeyPair();
                X509Certificate challenage_cert = SSLKeyManager.createTlsAlpn01Certificate(keypair, request.getProperty("domain", ""), acmeValidation);
                KeyStore jks = KeyStore.getInstance(KeyStore.getDefaultType());
                jks.load(null, null);
                jks.setCertificateEntry(request.getProperty("domain", "").trim(), challenage_cert);
                Certificate[] certChain = new Certificate[]{challenage_cert};
                jks.setKeyEntry(request.getProperty("domain", "").trim(), keypair.getPrivate(), "tls_alpn_challenge".toCharArray(), certChain);
                SSLKeyManager.addReply(jks, challenage_cert, request.getProperty("domain", "").trim(), "tls_alpn_challenge", null);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                jks.store(baos, "tls_alpn_challenge".toCharArray());
                Properties p = new Properties();
                p.put("bytes", baos.toByteArray());
                com.crushftp.client.Common.System2.put("crushftp.keystores." + (String.valueOf(System.getProperty("crushftp.prefs")) + "tls_challenge.jks").toUpperCase(), p);
                Vector server_list = ServerStatus.VG("server_list");
                int x = 0;
                while (x < server_list.size()) {
                    Properties server_item = (Properties)server_list.elementAt(x);
                    if (server_item.getProperty("serverType", "").equalsIgnoreCase("HTTPS") && server_item.getProperty("port", "").equals(request.getProperty("tls_alpn_https_port", "443").trim())) {
                        Properties server_item_original;
                        result = server_item_original = (Properties)server_item.clone();
                        server_item.put("tls_alpn_org_customKeystore", server_item.getProperty("customKeystore", ""));
                        server_item.put("customKeystore", String.valueOf(System.getProperty("crushftp.prefs")) + "tls_challenge.jks");
                        server_item.put("tls_alpn_org_customKeystorePass", server_item.getProperty("customKeystorePass", ""));
                        server_item.put("customKeystorePass", com.crushftp.client.Common.encryptDecrypt("tls_alpn_challenge", true));
                        server_item.put("tls_alpn_org_customKeystoreCertPass", server_item.getProperty("customKeystoreCertPass", ""));
                        server_item.put("customKeystoreCertPass", com.crushftp.client.Common.encryptDecrypt("tls_alpn_challenge", true));
                        ServerStatus.siPUT2("server_list", server_list);
                        ServerStatus.thisObj.save_server_settings(false);
                        ServerStatus.thisObj.stop_this_server(x);
                        Thread.sleep(1000L);
                        System.getProperties().put("crushftp.letsencrypt.acme4j_alpn", "true");
                        ServerStatus.thisObj.start_this_server(x);
                        break;
                    }
                    ++x;
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
            }
        }
        return result;
    }

    public static String removeTLSALPNChallengeJKS(Properties request) {
        String result = "Success";
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, "", "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 5);
                result = (String)p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        } else {
            try {
                Vector server_list = ServerStatus.VG("server_list");
                int x = 0;
                while (x < server_list.size()) {
                    Properties server_item = (Properties)server_list.elementAt(x);
                    if (server_item.getProperty("serverType", "").equalsIgnoreCase("HTTPS") && server_item.getProperty("port", "").equals(request.getProperty("tls_alpn_https_port", "443").trim())) {
                        server_list.setElementAt((Properties)request.get("server_item_original"), x);
                        ServerStatus.siPUT2("server_list", server_list);
                        ServerStatus.thisObj.save_server_settings(false);
                        ServerStatus.thisObj.stop_this_server(x);
                        Thread.sleep(1000L);
                        System.getProperties().put("crushftp.letsencrypt.acme4j_alpn", "false");
                        ServerStatus.thisObj.start_this_server(x);
                        break;
                    }
                    ++x;
                }
                if (com.crushftp.client.Common.System2.containsKey("crushftp.keystores." + (String.valueOf(System.getProperty("crushftp.prefs")) + "tls_challenge.jks").toUpperCase())) {
                    com.crushftp.client.Common.System2.remove("crushftp.keystores." + (String.valueOf(System.getProperty("crushftp.prefs")) + "tls_challenge.jks").toUpperCase());
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
                result = "Failed! Error :" + e;
            }
        }
        return result;
    }

    public static String updateJKS(Properties request) {
        String result = "Success!";
        if (!request.getProperty("instance", "").equals("")) {
            try {
                DMZServerCommon.sendFileToMemory(request.getProperty("keystore_path", ""), request.getProperty("instance", ""));
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
                result = "Failed! Error :" + e;
            }
        }
        if (com.crushftp.client.Common.System2.containsKey("crushftp.keystores." + request.getProperty("keystore_path", "").replace('\\', '/'))) {
            SSLKeyManager.loadKeyStoreToMemory(request.getProperty("keystore_path", ""));
        }
        return result;
    }

    public static String restartAllHttpsPorts(Properties request) {
        String result = "Success!";
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, "", "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 5);
                result = (String)p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        } else {
            try {
                Vector server_list = ServerStatus.VG("server_list");
                int x = 0;
                while (x < server_list.size()) {
                    Properties server_item = (Properties)server_list.elementAt(x);
                    if (server_item.getProperty("serverType", "").equalsIgnoreCase("HTTPS")) {
                        ServerStatus.thisObj.stop_this_server(x);
                        Thread.sleep(1000L);
                        ServerStatus.thisObj.start_this_server(x);
                    }
                    ++x;
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
                result = "Failed! Error :" + e;
            }
        }
        return result;
    }

    public static String checkHostPortForChallenge(Properties request) {
        String result = "";
        if (!request.getProperty("instance", "").equals("")) {
            String id = Common.makeBoundary();
            String instance = request.remove("instance").toString();
            DMZServerCommon.sendCommand(instance, request, "", "RUN:INSTANCE_ACTION", id);
            try {
                Properties p = DMZServerCommon.getResponse(id, 5);
                result = (String)p.get("data");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        } else {
            try {
                String[] domains = (String[])request.get("domains");
                String protocol = request.get("challenge_type").equals("http-01") ? "http" : "https";
                int x = 0;
                while (x < domains.length) {
                    try {
                        URLConnection urlc = URLConnection.openConnection(new VRL("https://www.crushftp.com/domain.jsp?host=" + domains[x] + "&protocol=" + protocol), new Properties());
                        urlc.setRequestMethod("GET");
                        urlc.setUseCaches(false);
                        urlc.setRequestProperty("Accept", "*/*");
                        urlc.setLength(0L);
                        String response = URLConnection.consumeResponse(urlc.getInputStream());
                        result = urlc.getResponseCode() < 200 || urlc.getResponseCode() > 299 ? String.valueOf(result) + "Host : " + domains[x] + " Result : No response." : String.valueOf(result) + "Host : " + domains[x] + " Result : " + response;
                    }
                    catch (Exception e) {
                        result = String.valueOf(result) + "Host : " + domains[x] + " Result : No response.";
                    }
                    ++x;
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        }
        return result;
    }

    public static Properties testFolderListing(Properties request) {
        Vector list = new Vector();
        String error = "";
        try {
            Properties user = UserTools.ut.getUser(request.getProperty("serverGroup", "MainUsers"), request.getProperty("username", ""), false);
            if (user != null) {
                String the_dir = Common.url_decode(request.getProperty("path", ""));
                if ((the_dir = com.crushftp.client.Common.dots(the_dir)).equals("/")) {
                    the_dir = user.getProperty("root_dir", "/");
                }
                if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(user.getProperty("root_dir", "/").toUpperCase())) {
                    the_dir = String.valueOf(user.getProperty("root_dir", "/")) + the_dir.substring(1);
                }
                VFS vfs = UserTools.ut.getVFS(request.getProperty("serverGroup", "MainUsers"), request.getProperty("username", ""));
                Properties perms = vfs.getCombinedPermissions();
                Properties p = vfs.get_item(Common.url_decode(request.getProperty("path", "/")));
                if (p == null) {
                    error = "Error : The given path " + Common.url_decode(request.getProperty("path", "/")) + " does not exists!";
                }
            } else {
                error = "Error : The given user " + request.getProperty("path", "/") + " does not exists!";
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 1, e);
            error = e.getMessage();
        }
        Properties list_result = new Properties();
        list_result.put("list_result", list);
        if (!error.equals("")) {
            list_result.put("list_result", error);
        }
        return list_result;
    }
}

