/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import com.crushftp.client.FileClient;
import com.crushftp.client.File_B;
import com.crushftp.client.File_S;
import com.crushftp.client.File_U;
import com.crushftp.client.GenericClient;
import com.crushftp.client.GenericClientMulti;
import com.crushftp.client.HTTPClient;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import crushftp.db.SearchHandler;
import crushftp.gui.LOC;
import crushftp.handlers.Common;
import crushftp.handlers.IdleMonitor;
import crushftp.handlers.Log;
import crushftp.handlers.PreviewWorker;
import crushftp.handlers.QuotaWorker;
import crushftp.handlers.SharedSession;
import crushftp.handlers.SharedSessionReplicated;
import crushftp.handlers.UserTools;
import crushftp.server.AdminControls;
import crushftp.server.RETR_handler;
import crushftp.server.STOR_handler;
import crushftp.server.ServerSessionFTP;
import crushftp.server.ServerStatus;
import crushftp.server.VFS;
import crushftp.server.ssh.SSHSocket;
import crushftp.user.XMLUsers;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Pattern;

public class SessionCrush
implements Serializable {
    static final long serialVersionUID = 0L;
    public static final String CRLF = "\r\n";
    public static transient Object set_quota_lock = new Object();
    public transient Vector session_socks = new Vector();
    public transient Vector data_socks = new Vector();
    public transient Vector old_data_socks = new Vector();
    public transient Vector pasv_socks = new Vector();
    public transient Vector stor_files_pool_free = new Vector();
    public transient Vector retr_files_pool_free = new Vector();
    public transient Vector stor_files_pool_used = new Vector();
    public transient Vector retr_files_pool_used = new Vector();
    public transient SimpleDateFormat hh = new SimpleDateFormat("HH", Locale.US);
    public transient SimpleDateFormat sdf_yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
    public transient SimpleDateFormat sdf_yyyyMMddHHmmssGMT = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
    public Properties user = null;
    public Properties user_info = new Properties();
    public boolean not_done = true;
    public VFS uVFS = null;
    public VFS expired_uVFS = null;
    public String rnfr_file_path = null;
    public transient ServerSessionFTP ftp = null;
    public Properties server_item = null;
    public final Properties accessExceptions = new Properties();
    public final Properties quotaDelta = new Properties();
    public SimpleDateFormat date_time = new SimpleDateFormat("MM/dd/yy", Locale.US);
    transient boolean allow_replication = true;
    long last_active_replicate = System.currentTimeMillis();
    public SimpleDateFormat logDateFormat;
    public static Properties session_counts = new Properties();
    public transient IdleMonitor thread_killer_item;
    public Properties vfs_bad_credentials_email_sent;
    public transient Object close_session_sync;
    boolean shareVFS;
    static Properties hack_cache = new Properties();

    public SessionCrush(Socket sock, int user_number, String user_ip, int listen_port, String listen_ip, String listen_ip_port, Properties server_item) {
        this.logDateFormat = (SimpleDateFormat)ServerStatus.thisObj.logDateFormat.clone();
        this.thread_killer_item = null;
        this.vfs_bad_credentials_email_sent = new Properties();
        this.close_session_sync = new Object();
        this.shareVFS = false;
        this.allow_replication = false;
        if (sock != null) {
            this.session_socks.addElement(sock);
            this.uiPUT("sock_port", String.valueOf(sock.getPort()));
        }
        this.server_item = server_item;
        try {
            SimpleDateFormat logHour = new SimpleDateFormat("yyMMddHH");
            this.uiPUT("user_log_path", ServerStatus.change_vars_to_values_static(String.valueOf(ServerStatus.SG("user_log_location")) + logHour.format(new Date()) + "/", null, null, null));
            if (!new File_S(this.uiSG("user_log_path")).exists()) {
                new File_S(this.uiSG("user_log_path")).mkdirs();
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
        }
        this.uiPUT("session", this);
        this.uiPUT("id", String.valueOf(user_number));
        this.uiPUT("user_number", String.valueOf(user_number));
        this.uiPUT("listen_ip_port", server_item.getProperty("linkedServer", ""));
        this.uiPUT("listen_ip", listen_ip);
        this.uiPUT("bind_port", server_item.getProperty("port"));
        String real_bind_ip = "0.0.0.0";
        if (sock != null && sock instanceof SSHSocket) {
            real_bind_ip = ((SSHSocket)sock).sockIn.getLocalAddress().getHostAddress();
        } else if (sock != null) {
            real_bind_ip = sock.getLocalAddress().getHostAddress();
        }
        this.uiPUT("bind_ip", real_bind_ip);
        this.uiPUT("bind_ip_config", server_item.getProperty("ip", listen_ip));
        this.uiPUT("user_ip", user_ip);
        this.uiPUT("user_protocol", server_item.getProperty("serverType", "ftp"));
        this.uiPUT("user_protocol_proxy", server_item.getProperty("serverType", "ftp"));
        this.uiPUT("user_port", sock == null ? "0" : String.valueOf(sock.getPort()));
        this.uiPUT("user_name", "");
        this.uiPUT("current_password", "");
        this.uiPUT("the_command", "");
        this.uiPUT("the_command_data", "");
        this.uiPUT("current_dir", "/");
        this.uiPUT("current_file", "");
        this.uiPUT("user_logged_in", "false");
        this.uiPUT("user_log", new Vector());
        this.uiPUT("user_log_file", "session_" + this.user_info.getProperty("user_protocol") + "_" + user_number + ".log");
        this.uiPUT("failed_commands", new Vector());
        this.uiPUT("refresh_user", "false");
        this.uiPUT("stat", new Properties());
        this.uiPUT("password_expired", "false");
        this.uiPUT("password_attempts", new Vector());
        this.uiPUT("lastUploadStats", new Vector());
        this.uiPUT("proxy_mode", "none");
        this.uiPUT("dieing", "false");
        this.uiPUT("pasv_connect", "false");
        this.uiPUT("last_logged_command", "");
        this.uiPUT("session_uploads", "");
        this.uiPUT("session_downloads", "");
        this.uiPUT("list_filetree_status", "");
        this.uiPUT("session_download_count", "0");
        this.uiPUT("session_upload_count", "0");
        this.uiPUT("list_zip_dir", "false");
        this.uiPUT("list_zip_file", "false");
        this.uiPUT("list_zip_only", "false");
        this.uiPUT("list_zip_app", ServerStatus.SG("list_zip_app"));
        this.uiPUT("list_dot", "true");
        this.uiPUT("zlibLevel", "8");
        this.uiPUT("last_file_real_path", "");
        this.uiPUT("last_file_name", "");
        this.uiPUT("login_date_stamp", "");
        this.uiPUT("login_date", "");
        this.uiPUT("login_date_formatted", "");
        this.uiPUT("termination_message", "");
        this.uiPUT("file_transfer_mode", ServerStatus.SG("file_transfer_mode"));
        this.uiPUT("modez", "false");
        this.uiPUT("dataSecure", "false");
        this.uiPUT("secureType", "TLS");
        this.uiPUT("friendly_quit", "false");
        this.uiPUT("randomaccess", "false");
        this.uiPUT("mlst_format", "Type*;Size*;Modify*;Perm*;UNIX.owner*;UNIX.group*;");
        this.uiPUT("last_port_string", "");
        this.uiPUT("last_time_remaining", "");
        this.uiPUT("last_action", "");
        this.uiPUT("crc", "");
        this.uiPUT("pause_now", "false");
        this.uiPUT("new_pass1", "");
        this.uiPUT("new_pass2", "");
        this.uiPUT("PASV_port", "2000");
        this.uiPUT("sending_file", "false");
        this.uiPUT("receiving_file", "false");
        this.uiPUT("listing_files", "false");
        this.uiPUT("dont_write", "false");
        this.uiPUT("dont_read", "false");
        this.uiPUT("dont_log", "false");
        this.uiPUT("didDisconnect", "false");
        this.uiPUT("adminAllowed", "true");
        this.uiPUT("sscn_mode", "false");
        this.uiPUT("file_length", "0");
        this.uiPUT("start_transfer_time", "0");
        this.uiPUT("end_part_transfer_time", "0");
        this.uiPUT("overall_transfer_speed", "0");
        this.uiPUT("current_transfer_speed", "0");
        this.uiPUT("seconds_remaining", "0");
        this.uiPUT("start_transfer_byte_amount", "0");
        this.uiPUT("bytes_sent", "0");
        this.uiPUT("bytes_sent_formatted", "0b");
        this.uiPUT("bytes_received", "0");
        this.uiPUT("bytes_received_formatted", "0b");
        this.uiPUT("ratio_bytes_sent", "0");
        this.uiPUT("ratio_bytes_received", "0");
        this.uiPUT("start_resume_loc", "0");
        this.uiPUT("no_zip_compression", "false");
        this.uiPUT("zip64", "false");
        this.uiPUT("secure", "false");
        this.uiPUT("explicit_ssl", "false");
        this.uiPUT("explicit_tls", "false");
        this.uiPUT("sftp_login_complete", "false");
        this.uiPUT("require_encryption", "false");
        this.uiPUT("login_date_stamp", String.valueOf(new Date().getTime()));
        this.uiPUT("login_date_stamp_unique", String.valueOf(new Date().getTime()));
        this.uiPUT("login_date", new Date().toString());
        this.uiPUT("login_date_formatted", this.logDateFormat.format(new Date()));
        this.uiPUT("time", this.logDateFormat.format(new Date()));
        if (server_item.getProperty("serverType", "FTP").toUpperCase().equals("FTPS")) {
            this.uiPUT("secure", "true");
            this.uiPUT("dataSecure", "true");
            this.uiPUT("sscn_mode", "false");
        }
        if (server_item.getProperty("explicit_ssl", "false").toUpperCase().equals("TRUE")) {
            this.uiPUT("explicit_ssl", "true");
        }
        if (server_item.getProperty("explicit_tls", "false").toUpperCase().equals("TRUE")) {
            this.uiPUT("explicit_tls", "true");
        }
        if (server_item.getProperty("require_encryption", "false").toUpperCase().equals("TRUE")) {
            this.uiPUT("require_encryption", "true");
        }
        this.sdf_yyyyMMddHHmmssGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
        this.allow_replication = true;
        this.active();
    }

    public void setFtp(ServerSessionFTP ftp) {
        this.ftp = ftp;
    }

    public int uiIG(String data) {
        try {
            return Integer.parseInt(this.uiSG(data));
        }
        catch (Exception exception) {
            return 0;
        }
    }

    public long uiLG(String data) {
        try {
            return Long.parseLong(this.uiSG(data));
        }
        catch (Exception exception) {
            return 0L;
        }
    }

    public boolean uiBG(String data) {
        return this.uiSG(data).toLowerCase().equals("true");
    }

    public String uiSG(String data) {
        if (this.user_info.containsKey(data)) {
            return this.user_info.getProperty(data);
        }
        return "";
    }

    public void uiPUT(String key, Object val) {
        this.put(key, val);
    }

    public void uiPUT(String key, boolean val) {
        this.uiPUT(key, String.valueOf(val));
    }

    public void uiPPUT(String key, long val) {
        this.uiPUT(key, String.valueOf(this.uiLG(key) + val));
    }

    public Vector uiVG(String key) {
        return (Vector)this.user_info.get(key);
    }

    public Properties uiPG(String key) {
        return (Properties)this.user_info.get(key);
    }

    public void put(String key, Object val) {
        this.put(key, val, true);
    }

    public void put(String key, Object val2, boolean replicate) {
        Properties session = this.user_info;
        if (val2 == null) {
            session.remove(key);
        } else {
            String val1 = "" + session.put(key, val2);
            if (val1 != null && val1.equals(val2)) {
                return;
            }
            if (replicate && this.allow_replication) {
                if (key.equals("dont_read")) {
                    return;
                }
                if (key.equals("dont_write")) {
                    return;
                }
                if (key.equals("last_logged_command")) {
                    return;
                }
                if (key.equals("request")) {
                    return;
                }
                if (key.equals("last_priv_dir")) {
                    return;
                }
                if (key.equals("skip_proxy_check")) {
                    return;
                }
                if (key.equals("bytes_received")) {
                    return;
                }
                if (key.equals("bytes_received_formatted")) {
                    return;
                }
                if (key.equals("bytes_sent")) {
                    return;
                }
                if (key.equals("bytes_sent_formatted")) {
                    return;
                }
                if (SharedSessionReplicated.send_queues.size() > 0 && ServerStatus.BG("replicate_sessions9")) {
                    SharedSessionReplicated.send(this.getId(), "crushftp.session.update", key, val2);
                }
            }
        }
    }

    public String getProperty(String key) {
        Properties session = this.user_info;
        return session.getProperty(key);
    }

    public String getProperty(String key, String defaultVal) {
        Properties session = this.user_info;
        return session.getProperty(key, defaultVal);
    }

    public Object get(String key) {
        Properties session = this.user_info;
        return session.get(key);
    }

    public boolean containsKey(String key) {
        Properties session = this.user_info;
        return session.containsKey(key);
    }

    public void putAll(Properties p) {
        Properties session = this.user_info;
        session.putAll((Map<?, ?>)p);
    }

    public Object remove(String key) {
        Properties session = this.user_info;
        return session.remove(key);
    }

    public void active() {
        this.put("last_activity", String.valueOf(System.currentTimeMillis()), true);
        this.last_active_replicate = System.currentTimeMillis();
    }

    public void active_transfer() {
        if (System.currentTimeMillis() - this.last_active_replicate > 60000L) {
            this.last_active_replicate = System.currentTimeMillis();
            this.put("last_activity", String.valueOf(System.currentTimeMillis()), true);
            this.last_active_replicate = System.currentTimeMillis();
        } else {
            this.put("last_activity", String.valueOf(System.currentTimeMillis()), false);
        }
    }

    public void do_kill() {
        this.do_kill(this.thread_killer_item);
    }

    public void do_kill(IdleMonitor thread_killer_item) {
        block45: {
            this.not_done = false;
            if (thread_killer_item != null) {
                try {
                    thread_killer_item.die_now = true;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            try {
                ServerStatus.thisObj.statTools.executeSql(ServerStatus.SG("stats_update_sessions"), new Object[]{new Date(), this.user_info.getProperty("SESSION_RID")});
            }
            catch (Exception e) {
                Log.log("SERVER", 2, e);
            }
            try {
                if (!this.uiBG("didDisconnect")) {
                    this.uiPUT("didDisconnect", "true");
                    this.do_event5("LOGOUT", null);
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 2, e);
            }
            try {
                if (this.BG("ratio_field_permanent")) {
                    if (!com.crushftp.client.Common.dmz_mode) {
                        UserTools.ut.put_in_user(this.uiSG("listen_ip_port"), this.uiSG("user_name"), "user_bytes_sent", String.valueOf(this.uiLG("bytes_sent") + this.uiLG("ratio_bytes_sent")), false, true);
                    }
                    if (!com.crushftp.client.Common.dmz_mode) {
                        UserTools.ut.put_in_user(this.uiSG("listen_ip_port"), this.uiSG("user_name"), "user_bytes_received", String.valueOf(this.uiLG("bytes_received") + this.uiLG("ratio_bytes_received")), false, true);
                    }
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 2, e);
            }
            if (this.uVFS != null) {
                this.uVFS.free();
            }
            if (this.uVFS != null && !this.server_item.getProperty("serverType", "ftp").toUpperCase().startsWith("HTTP")) {
                this.uVFS.disconnect();
            }
            ServerStatus.thisObj.remove_user(this.user_info);
            try {
                while (this.pasv_socks.size() > 0) {
                    ((ServerSocket)this.pasv_socks.remove(0)).close();
                }
            }
            catch (Exception e) {
                // empty catch block
            }
            if (!this.server_item.getProperty("serverType", "ftp").toUpperCase().startsWith("HTTP")) {
                if (System.getProperties().getProperty("crushftp.sftp.wait_transfers", "true").equals("true")) {
                    try {
                        long start = System.currentTimeMillis();
                        while (this.retr_files_pool_used.size() + this.stor_files_pool_used.size() > 0 && System.currentTimeMillis() - start < 10000L) {
                            Thread.sleep(100L);
                            if (System.currentTimeMillis() - start <= 3000L) continue;
                            Log.log("SERVER", 2, "Waiting for STOR/RETR threads to finish..." + Thread.currentThread().getName());
                        }
                    }
                    catch (Exception start) {
                        // empty catch block
                    }
                }
                try {
                    this.kill_retr_files(this.retr_files_pool_free);
                    this.kill_retr_files(this.retr_files_pool_used);
                }
                catch (Exception start) {
                    // empty catch block
                }
                try {
                    this.kill_stor_files(this.stor_files_pool_free);
                    this.kill_stor_files(this.stor_files_pool_used);
                }
                catch (Exception start) {
                    // empty catch block
                }
            }
            int x22 = this.session_socks.size() - 1;
            while (x22 >= 0) {
                Socket sock = (Socket)this.session_socks.elementAt(x22);
                try {
                    if (this.uiSG("user_protocol").equalsIgnoreCase("SFTP") && sock instanceof SSHSocket) {
                        sock.close();
                    }
                    if (this.uiSG("user_protocol").toUpperCase().startsWith("HTTP")) {
                        sock.close();
                    }
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                if (sock.isClosed()) {
                    this.session_socks.remove(sock);
                }
                --x22;
            }
            if (this.ftp == null) break block45;
            try {
                this.ftp.sockOriginal.setSoTimeout(2000);
                this.ftp.sockOriginal.setSoLinger(true, 2);
                this.ftp.sockOriginal.close();
            }
            catch (Exception x22) {}
        }
        try {
            while (this.old_data_socks.size() > 0) {
                Object obj = this.old_data_socks.remove(0);
                if (obj instanceof Socket) {
                    ((Socket)obj).setSoTimeout(2000);
                    ((Socket)obj).close();
                }
                if (!(obj instanceof ServerSocket)) continue;
                ((ServerSocket)obj).close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            if (this.ftp != null) {
                this.ftp.os.close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            if (this.ftp != null) {
                this.ftp.is.close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void kill_stor_files(Vector v) {
        while (v.size() > 0) {
            STOR_handler sf = (STOR_handler)v.remove(0);
            sf.die_now = true;
            if (sf.thisThread != null) {
                sf.thisThread.interrupt();
            }
            try {
                if (sf.data_is == null) continue;
                sf.data_is.close();
            }
            catch (IOException e) {
                Log.log("SERVER", 1, e);
            }
        }
    }

    public void kill_retr_files(Vector v) {
        while (v.size() > 0) {
            RETR_handler rf = (RETR_handler)v.remove(0);
            rf.die_now = true;
            if (rf.thisThread == null) continue;
            rf.thisThread.interrupt();
        }
    }

    public void log_pauses() {
        this.add_log("[" + this.server_item.getProperty("serverType", "ftp") + ":" + this.uiSG("user_number") + "_" + this.uiSG("sock_port") + ":" + this.uiSG("user_name") + ":" + this.uiSG("user_ip") + "] *" + (this.uiBG("pause_now") ? LOC.G("Paused") + ".*" : LOC.G("Unpaused") + ".*"), "PAUSE_RESUME");
    }

    public boolean check_access_privs(String the_dir, String command) throws Exception {
        try {
            Properties item = this.uVFS.get_fake_item(the_dir, "FILE");
            return this.check_access_privs(the_dir, command, item);
        }
        catch (Exception e) {
            Log.log("ACCESS", 2, e);
            return false;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean check_access_privs(String the_dir, String command, Properties item) throws Exception {
        if (the_dir.indexOf(":filetree") >= 0) {
            the_dir = the_dir.substring(0, the_dir.indexOf(":filetree"));
            if (item == null) {
                item = this.uVFS.get_item(the_dir);
            }
        }
        try {
            boolean locked;
            String privs;
            String last_dir;
            String additionalAccess;
            block48: {
                Properties item2 = null;
                additionalAccess = this.check_access_exception(the_dir, command, item);
                Properties p = new Properties();
                p.put("command", command);
                p.put("the_command_data", this.uiSG("the_command_data"));
                if (item != null) {
                    p.put("item", item);
                }
                if ((item == null && command.equals("MKD") || command.equals("XMKD")) && (item2 = the_dir.equals(this.SG("root_dir")) ? this.uVFS.get_item(the_dir) : this.uVFS.get_item_parent(the_dir)) != null) {
                    p.put("item", item2);
                }
                p.put("the_dir", the_dir);
                p.put("additionalAccess", additionalAccess);
                this.runPlugin("access", p);
                additionalAccess = p.getProperty("additionalAccess", additionalAccess);
                command = p.getProperty("command", command);
                the_dir = p.getProperty("the_dir", the_dir);
                if (p.get("item") != null && item2 == null) {
                    item = (Properties)p.get("item");
                }
                last_dir = this.uiSG("last_priv_dir");
                this.uiPUT("last_priv_dir", the_dir);
                privs = item == null ? "" : String.valueOf(item.getProperty("privs", "")) + additionalAccess;
                Properties combinedPermissions = this.uVFS.getCombinedPermissions();
                boolean aclPermissions = combinedPermissions.getProperty("acl_permissions", "false").equals("true");
                if (aclPermissions) {
                    if (item == null) {
                        item = this.uVFS.get_item(Common.all_but_last(the_dir));
                    }
                    privs = this.uVFS.getPriv(the_dir, item);
                }
                Pattern pattern = null;
                String block_access = this.SG("block_access").trim();
                block_access = String.valueOf(block_access) + CRLF;
                block_access = String.valueOf(block_access) + ServerStatus.SG("block_access").trim();
                block_access = block_access.trim();
                BufferedReader br = new BufferedReader(new StringReader(block_access));
                String searchPattern = "";
                do {
                    if ((searchPattern = br.readLine()) == null) {
                        Properties metaInfo = new Properties();
                        locked = false;
                        if (item != null) {
                            metaInfo = PreviewWorker.getMetaInfo(PreviewWorker.getDestPath2(String.valueOf(item.getProperty("url")) + "/p1/"));
                            boolean bl = locked = !metaInfo.getProperty("crushftp_locked_user", "").equals("") && !metaInfo.getProperty("crushftp_locked_user", "").equalsIgnoreCase(this.uiSG("user_name"));
                        }
                        break block48;
                    }
                    searchPattern = searchPattern.trim();
                    try {
                        pattern = null;
                        pattern = com.crushftp.client.Common.getPattern(searchPattern, true);
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 1, e);
                    }
                    if (searchPattern.startsWith("~") || pattern == null || !pattern.matcher(the_dir).matches()) continue;
                    return false;
                } while (!searchPattern.startsWith("~") || !com.crushftp.client.Common.do_search(searchPattern.substring(1), the_dir, false, 0));
                return false;
            }
            if (command.equals("WWW")) {
                return privs.indexOf("(www)") >= 0;
            }
            if (command.equals("CWD") && the_dir.equals("/")) {
                return true;
            }
            if (command.equals("CWD")) {
                return item != null;
            }
            if (command.equals("RETR") && this.uiLG("start_resume_loc") > 0L) {
                if (this.SG("allow_locked_download").equalsIgnoreCase("true") && locked) {
                    locked = false;
                }
                if (!locked && privs.indexOf("(resume)") >= 0 && privs.indexOf("(read)") >= 0) {
                    return this.check_filename_extensions_dir(privs, "read_types", the_dir);
                }
                return false;
            }
            if (command.equals("RETR")) {
                if (this.SG("allow_locked_download").equalsIgnoreCase("true") && locked) {
                    locked = false;
                }
                if (!locked && (privs == null || privs.indexOf("(read)") < 0) && item != null && item.getProperty("size", "0").equals("0")) {
                    return this.check_filename_extensions_dir(privs, "read_types", the_dir);
                }
            }
            if (command.equals("RETR")) {
                if (this.SG("allow_locked_download").equalsIgnoreCase("true") && locked) {
                    locked = false;
                }
                if (!locked && privs.indexOf("(read)") >= 0) {
                    return this.check_filename_extensions_dir(privs, "read_types", the_dir);
                }
                return false;
            }
            if (command.equals("DELE")) {
                String privs2 = this.getLockedPrivs(privs, the_dir);
                if (!locked && privs.indexOf("(delete)") >= 0 && (privs2.indexOf("(inherited)") >= 0 || privs2.indexOf("(locked)") < 0 || this.uVFS.getCombinedPermissions().getProperty("acl_permissions", "false").equals("true"))) {
                    return this.check_filename_extensions_dir(privs, "delete_types", the_dir);
                }
                return false;
            }
            if (command.equals("RNFR") && !locked && privs.indexOf("(rename)") < 0 && item != null && item.getProperty("name").toUpperCase().startsWith("NEW FOLDER")) {
                return true;
            }
            if (command.equals("RNFR")) {
                String privs2 = this.getLockedPrivs(privs, the_dir);
                return !locked && privs.indexOf("(rename)") >= 0 && (privs2.indexOf("(inherited)") >= 0 || privs2.indexOf("(locked)") < 0 || this.uVFS.getCombinedPermissions().getProperty("acl_permissions", "false").equals("true"));
            }
            if (command.equals("STOR") && this.uiLG("start_resume_loc") > 0L && item != null) {
                String privs2 = this.getLockedPrivs(privs, the_dir);
                if (!(locked || privs.indexOf("(resume)") < 0 || privs.indexOf("(write)") < 0 || privs2.indexOf("(inherited)") < 0 && privs2.indexOf("(locked)") >= 0)) {
                    return this.check_filename_extensions_dir(privs, "write_types", the_dir);
                }
                return false;
            }
            if (command.equals("APPE") && item != null) {
                String privs2 = this.getLockedPrivs(privs, the_dir);
                if (!(locked || privs.indexOf("(resume)") < 0 || privs.indexOf("(write)") < 0 || privs2.indexOf("(inherited)") < 0 && privs2.indexOf("(locked)") >= 0)) {
                    return this.check_filename_extensions_dir(privs, "write_types", the_dir);
                }
                return false;
            }
            if (command.equals("SIZE")) {
                return privs.indexOf("(view)") >= 0 || privs.indexOf("(read)") >= 0;
            }
            if (command.equals("MDTM")) {
                return privs.indexOf("(view)") >= 0 || privs.indexOf("(read)") >= 0;
            }
            if (command.equals("STAT")) {
                return privs.indexOf("(view)") >= 0 || privs.indexOf("(read)") >= 0;
            }
            if (command.equals("MLSD") || command.equals("MLST")) {
                return privs.indexOf("(view)") >= 0;
            }
            if (command.equals("LIST") || command.equals("NLST")) {
                return privs.indexOf("(view)") >= 0;
            }
            if (command.equals("SHARE")) {
                return privs.indexOf("(share)") >= 0;
            }
            item = the_dir.equals(this.SG("root_dir")) ? this.uVFS.get_item(the_dir) : this.uVFS.get_item_parent(the_dir);
            String string = privs = item == null ? "" : String.valueOf(item.getProperty("privs", "")) + additionalAccess;
            if (command.equals("STOR") || command.equals("APPE") || command.equals("STOU")) {
                String privs2 = this.getLockedPrivs(privs, String.valueOf(the_dir) + "check_locked.txt");
                if (!(locked || privs.indexOf("(write)") < 0 || privs2.indexOf("(inherited)") < 0 && privs2.indexOf("(locked)") >= 0)) {
                    return this.check_filename_extensions_dir(privs, "write_types", the_dir);
                }
                return false;
            }
            if (command.equals("MKD") || command.equals("XMKD")) {
                String privs2 = this.getLockedPrivs(privs, the_dir);
                return privs.indexOf("(makedir)") >= 0 && (privs2.indexOf("(inherited)") >= 0 || privs2.indexOf("(locked)") < 0);
            }
            if (command.equals("RMD") || command.equals("XRMD")) {
                String privs2 = this.getLockedPrivs(privs, the_dir);
                return privs.indexOf("(deletedir)") >= 0 && (privs2.indexOf("(inherited)") >= 0 || privs2.indexOf("(locked)") < 0 || this.uVFS.getCombinedPermissions().getProperty("acl_permissions", "false").equals("true"));
            }
            if (command.equals("RNTO") && privs.indexOf("(rename)") >= 0 && Common.all_but_last(last_dir).equals(Common.all_but_last(the_dir))) {
                return this.check_filename_extensions_dir(privs, "rename_types", the_dir);
            }
            if (!command.equals("RNTO")) {
                return false;
            }
            if (privs.indexOf("(write)") >= 0 && (privs.indexOf("(inherited)") >= 0 || privs.indexOf("(locked)") < 0)) {
                return this.check_filename_extensions_dir(privs, "rename_types", the_dir);
            }
            return false;
        }
        catch (Exception e) {
            if (("" + e).indexOf("Interrupted") >= 0) {
                throw e;
            }
            Log.log("ACCESS", 1, e);
            return false;
        }
    }

    public boolean check_filename_extensions_dir(String privs, String priv_key, String the_dir) {
        if (privs.indexOf("(" + priv_key + ":") >= 0) {
            String write_types = privs.substring(privs.indexOf("(" + priv_key + ":"));
            write_types = write_types.substring(write_types.indexOf(":") + 1, write_types.indexOf(")")).trim().toUpperCase();
            String ext = "";
            ext = the_dir.indexOf(".") < 0 ? "NONE" : the_dir.substring(the_dir.lastIndexOf(".") + 1).toUpperCase();
            int x = 0;
            while (x < write_types.split(",").length) {
                if (write_types.split(",")[x].equals(ext)) {
                    return true;
                }
                ++x;
            }
            Log.log("ACCESS", 1, "File extension blocked for specific directory:" + the_dir + " : " + priv_key + " :" + write_types);
            return false;
        }
        return true;
    }

    public String getLockedPrivs(String privs, String the_dir) throws Exception {
        if (privs.indexOf("(locked)") >= 0) {
            Properties item_tmp = this.uVFS.get_item(Common.all_but_last(the_dir));
            privs = item_tmp == null ? "" : item_tmp.getProperty("privs", "");
        }
        return privs;
    }

    public String check_access_exception(String the_dir, String command, Properties item) throws Exception {
        String original_the_dir = the_dir;
        if (command.equals("RNTO")) {
            the_dir = this.rnfr_file_path;
        }
        if (this.accessExceptions.get(the_dir) == null) {
            return "";
        }
        Properties master_item = (Properties)this.accessExceptions.get(the_dir);
        if (item != null && master_item.getProperty("modified", "-1").equals(item.getProperty("modified", "-2"))) {
            if (command.equals("RNTO")) {
                this.accessExceptions.remove(the_dir);
                this.accessExceptions.put(original_the_dir, master_item);
            }
            return "(read)(rename)(delete)";
        }
        return "";
    }

    public long get_quota(String the_dir) throws Exception {
        return SessionCrush.get_quota(the_dir, this.uVFS, this.SG("parent_quota_dir"), this.quotaDelta, this, true);
    }

    public static long get_quota(String the_dir, VFS uVFS, String parentQuotaDir, Properties quotaDelta, SessionCrush thisSession, boolean available) throws Exception {
        block5: {
            try {
                Log.log("QUOTA", 3, "get_quota the_dir:" + the_dir + ", parentQuotaDir:" + parentQuotaDir + ", quotaDelta:" + quotaDelta);
                Properties item = uVFS.get_item(uVFS.getPrivPath(the_dir));
                if (item.getProperty("privs", "").indexOf("(quota") >= 0) {
                    long totalQuota = SessionCrush.get_total_quota(the_dir, uVFS, quotaDelta);
                    Log.log("QUOTA", 3, "get_quota totalQuota:" + totalQuota);
                    if (item.getProperty("privs", "").indexOf("(real_quota)") >= 0) {
                        long used = SessionCrush.get_quota_used(the_dir, uVFS, parentQuotaDir, thisSession);
                        if (available) {
                            totalQuota -= used;
                        }
                        Log.log("QUOTA", 3, "get_quota_used:" + used);
                        Log.log("QUOTA", 3, "get_quota_used totalQuota:" + totalQuota);
                    }
                    return totalQuota;
                }
            }
            catch (Exception e) {
                if (("" + e).indexOf("Interrupted") < 0) break block5;
                throw e;
            }
        }
        return -12345L;
    }

    public long get_quota_used(String the_dir) throws Exception {
        return SessionCrush.get_quota_used(the_dir, this.uVFS, this.SG("parent_quota_dir"), this);
    }

    public static long get_quota_used(String the_dir, VFS uVFS, String parentQuotaDir, SessionCrush thisSession) throws Exception {
        return QuotaWorker.get_quota_used(the_dir, uVFS, parentQuotaDir, thisSession);
    }

    public long get_total_quota(String the_dir) throws Exception {
        return SessionCrush.get_total_quota(the_dir, this.uVFS, this.quotaDelta);
    }

    public static long get_total_quota(String the_dir, VFS uVFS, Properties quotaDelta) throws Exception {
        block3: {
            try {
                Properties item = uVFS.get_item(uVFS.getPrivPath(the_dir));
                if (item.getProperty("privs", "").indexOf("(quota") >= 0) {
                    String data = item.getProperty("privs", "");
                    data = data.substring(data.indexOf("(quota") + 6, data.indexOf(")", data.indexOf("(quota")));
                    quotaDelta.put(the_dir, data);
                    return Long.parseLong(data);
                }
            }
            catch (Exception e) {
                if (("" + e).indexOf("Interrupted") < 0) break block3;
                throw e;
            }
        }
        return -12345L;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void set_quota(String the_dir, long quota_val) throws Exception {
        if (ServerStatus.BG("quota_async")) {
            return;
        }
        Object object = set_quota_lock;
        synchronized (object) {
            block13: {
                try {
                    UserTools.loadPermissions(this.uVFS);
                    Properties item = this.uVFS.get_item(this.uVFS.getPrivPath(the_dir));
                    if (item.getProperty("privs", "").indexOf("(quota") >= 0 && item.getProperty("privs", "").indexOf("(real_quota)") < 0) {
                        long originalQuota = Long.parseLong(this.quotaDelta.getProperty(the_dir));
                        long quotaDiff = originalQuota - quota_val;
                        String data = item.getProperty("privs", "");
                        data = data.substring(data.indexOf("(quota") + 6, data.indexOf(")", data.indexOf("(quota")));
                        data = Common.replace_str(item.getProperty("privs", ""), data, String.valueOf(originalQuota - quotaDiff));
                        item.put("privs", data);
                        String privPath = this.uVFS.getPrivPath(the_dir);
                        UserTools.addPriv(this.uiSG("listen_ip_port"), this.uiSG("user_name"), privPath, data, Integer.parseInt(this.uVFS.getPrivPath(the_dir, true, true)), this.uVFS);
                        this.uVFS.reset();
                        Properties p = new Properties();
                        p.put("permissions", this.uVFS.getCombinedPermissions());
                        this.runPlugin("quotaUpdate", p);
                    } else if (item.getProperty("privs", "").indexOf("(quota") >= 0 && item.getProperty("privs", "").indexOf("(real_quota)") >= 0) {
                        String data = item.getProperty("privs", "");
                        data = data.substring(data.indexOf("(quota") + 6, data.indexOf(")", data.indexOf("(quota")));
                        String parentQuotaDir = this.SG("parent_quota_dir");
                        String real_path = "";
                        if (parentQuotaDir.startsWith("FILE:") || parentQuotaDir.startsWith("file:")) {
                            real_path = new VRL(parentQuotaDir).getPath();
                        } else {
                            String parentAddon = this.SG("parent_quota_dir");
                            if (parentAddon.equals("parent_quota_dir")) {
                                parentAddon = "";
                            }
                            real_path = String.valueOf(new VRL(item.getProperty("url")).getCanonicalPath()) + "/" + parentAddon;
                        }
                        if (VFS.quotaCache.containsKey(real_path.toUpperCase())) {
                            Properties p = (Properties)VFS.quotaCache.get(real_path.toUpperCase());
                            p.put("size", String.valueOf(Long.parseLong(data) - quota_val));
                        }
                    }
                }
                catch (Exception e) {
                    if (("" + e).indexOf("Interrupted") < 0) break block13;
                    throw e;
                }
            }
        }
    }

    public void add_log_formatted(String log_data, String check_data) {
        this.add_log_formatted(log_data, check_data, "");
    }

    public void add_log_formatted(String log_data, String check_data, String uid) {
        if (this.uiBG("dont_log")) {
            return;
        }
        if (this.logDateFormat == null) {
            this.logDateFormat = (SimpleDateFormat)ServerStatus.thisObj.logDateFormat.clone();
        }
        if (!((check_data = String.valueOf(check_data) + " ").trim().equals("DIR_LIST") || log_data.trim().startsWith("RETR END") || log_data.trim().startsWith("STOR END"))) {
            Properties p = new Properties();
            p.put("the_command", check_data.substring(0, check_data.indexOf(" ")));
            p.put("user_time", this.logDateFormat.format(new Date()));
            String command_data = this.uiSG("the_command_data");
            if (this.uiSG("the_command").toUpperCase().equals("PASS")) {
                command_data = "**************";
            }
            p.put("stamp", String.valueOf(new Date().getTime()));
        }
        if ((log_data.contains("?u=") || log_data.contains("&u=")) && (log_data.contains("?p=") || log_data.contains("&p="))) {
            int index_end;
            int index_start = log_data.indexOf("?p=");
            if (index_start <= 0) {
                index_start = log_data.indexOf("&p=");
            }
            if ((index_end = log_data.indexOf("&", index_start + 3)) <= 0) {
                index_end = log_data.length();
            }
            if (index_start < index_end) {
                String pass = log_data.substring(index_start + 3, index_end);
                log_data = log_data.replace(pass, "**************");
            }
        }
        if (check_data.trim().equals("DIR_LIST")) {
            this.add_log(log_data, check_data.trim());
        } else {
            this.add_log("[" + this.server_item.getProperty("serverType", "ftp") + ":" + this.uiSG("user_number") + "_" + this.uiSG("sock_port") + uid + ":" + this.uiSG("user_name") + ":" + this.uiSG("user_ip") + "] " + this.SG("READ") + ": *" + log_data + "*", check_data.trim());
        }
    }

    public void add_log(String log_data, String check_data) {
        this.add_log(log_data, check_data, check_data);
    }

    public void add_log(String log_data, String short_data, String check_data) {
        if (this.uiBG("dont_log")) {
            return;
        }
        if (this.logDateFormat == null) {
            this.logDateFormat = (SimpleDateFormat)ServerStatus.thisObj.logDateFormat.clone();
        }
        if (log_data.indexOf("WROTE: *220-") < 0 && log_data.indexOf("WROTE: *230-") < 0) {
            log_data = String.valueOf(log_data.trim()) + CRLF;
            BufferedReader lines = new BufferedReader(new StringReader(log_data));
            String data = "";
            try {
                while ((data = lines.readLine()) != null) {
                    if (check_data.equals("DIR_LIST")) {
                        data = "[" + this.uiSG("user_number") + "_" + this.uiSG("sock_port") + ":" + this.uiSG("user_name") + ":" + this.uiSG("user_ip") + "] WROTE: " + data;
                    }
                    if (check_data.equals("PROXY")) {
                        data = "[" + this.uiSG("user_number") + "_" + this.uiSG("sock_port") + ":" + this.uiSG("user_name") + ":" + this.uiSG("user_ip") + "] : " + data;
                    }
                    ServerStatus.thisObj.append_log(String.valueOf(data) + CRLF, check_data);
                    if (!ServerStatus.BG("write_session_logs")) continue;
                    this.uiVG("user_log").addElement("SESSION|" + this.logDateFormat.format(new Date()) + "|" + data);
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        this.drain_log();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drain_log() {
        if (!ServerStatus.BG("write_session_logs")) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        Object object = SharedSession.sessionLock;
        synchronized (object) {
            Vector vector = this.uiVG("user_log");
            synchronized (vector) {
                while (this.uiVG("user_log").size() > 0) {
                    int loops = 0;
                    while (this.uiVG("user_log").size() > 0 && loops++ < 1000) {
                        try {
                            sb.append(this.uiVG("user_log").remove(0).toString()).append(CRLF);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                }
            }
        }
        object = this.uiVG("user_log");
        synchronized (object) {
            if (!this.uiSG("user_log_path_custom").equals("")) {
                String new_loc = "" + this.user_info.remove("user_log_path_custom");
                String old_loc = this.uiSG("user_log_path");
                this.uiPUT("user_log_path", new_loc);
                new File_S(Common.all_but_last(String.valueOf(this.uiSG("user_log_path")) + this.uiSG("user_log_file"))).mkdirs();
                if (new File_S(String.valueOf(old_loc) + this.uiSG("user_log_file")).exists() && !new File_S(String.valueOf(old_loc) + this.uiSG("user_log_file")).renameTo(new File_S(String.valueOf(new_loc) + this.uiSG("user_log_file")))) {
                    try {
                        Common.copy(String.valueOf(old_loc) + this.uiSG("user_log_file"), String.valueOf(new_loc) + this.uiSG("user_log_file"), true);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    new File_S(String.valueOf(old_loc) + this.uiSG("user_log_file")).delete();
                }
            }
            try {
                com.crushftp.client.Common.copyStreams(new ByteArrayInputStream(sb.toString().getBytes("UTF8")), new FileOutputStream(new File_S(String.valueOf(this.uiSG("user_log_path")) + this.uiSG("user_log_file")), true), true, true);
            }
            catch (FileNotFoundException e) {
                try {
                    new File_S(Common.all_but_last(String.valueOf(this.uiSG("user_log_path")) + this.uiSG("user_log_file"))).mkdirs();
                    com.crushftp.client.Common.copyStreams(new ByteArrayInputStream(sb.toString().getBytes("UTF8")), new FileOutputStream(new File_S(String.valueOf(this.uiSG("user_log_path")) + this.uiSG("user_log_file")), true), true, true);
                }
                catch (IOException ee) {
                    Log.log("SERVER", 1, ee);
                }
            }
            catch (IOException e) {
                Log.log("SERVER", 1, e);
            }
        }
    }

    public Properties do_event5(String type, Properties fileItem1) {
        return this.do_event5(type, fileItem1, null);
    }

    public Properties do_event5(String type, Properties fileItem1, Properties fileItem2) {
        if (fileItem1 != null && !fileItem1.getProperty("the_file_error", "").equals("") && !fileItem1.getProperty("mark_error", "").equals("true")) {
            if (ServerStatus.BG("block_failed_filetransfer_events")) {
                return null;
            }
        }
        Log.log("EVENT", 2, "Checking fileitem1 type:" + VRL.safe(fileItem1));
        if (fileItem1 != null && fileItem1.containsKey("the_file_type")) {
            fileItem1.put("type", fileItem1.getProperty("the_file_type"));
        }
        if (fileItem2 != null && fileItem2.containsKey("the_file_type")) {
            fileItem2.put("type", fileItem2.getProperty("the_file_type"));
        }
        Properties info = null;
        Properties originalUser = this.user;
        try {
            Log.log("EVENT", 2, "Checking user object1:" + (this.user == null) + ":username=" + this.uiSG("user_name"));
            if (this.user == null && type.equalsIgnoreCase("ERROR")) {
                this.user = UserTools.ut.getUser(this.uiSG("listen_ip_port"), this.uiSG("user_name"), true);
            }
            Log.log("EVENT", 2, "Checking user object2:" + (this.user == null) + ":username=" + this.uiSG("user_name"));
            if (this.user == null) {
                Properties properties = info;
                return properties;
            }
            Properties fileItem1_2 = null;
            if (fileItem1 != null) {
                fileItem1_2 = (Properties)fileItem1.clone();
            }
            Properties fileItem2_2 = null;
            if (fileItem2 != null) {
                fileItem2_2 = (Properties)fileItem2.clone();
            }
            Log.log("EVENT", 2, "Processing event1:" + type + ":username=" + this.uiSG("user_name"));
            info = ServerStatus.thisObj.events6.process(type, fileItem1_2, fileItem2_2, (Vector)this.user.get("events"), this);
            Log.log("EVENT", 2, "Processing event2:" + type + ":username=" + this.uiSG("user_name"));
            if (fileItem1_2 != null && fileItem1_2.containsKey("execute_log")) {
                fileItem1.put("execute_log", fileItem1_2.get("execute_log"));
            }
            if (fileItem2_2 != null && fileItem2_2.containsKey("execute_log")) {
                fileItem2.put("execute_log", fileItem2_2.get("execute_log"));
            }
        }
        finally {
            this.user = originalUser;
        }
        return info;
    }

    public Properties runPlugin(String action, Properties p) {
        Log.log("PLUGIN", 3, "PLUGIN:Calling " + action);
        if (p == null) {
            p = new Properties();
        }
        p.put("action", action);
        p.put("server_item", this.server_item);
        if (p.get("user") == null && this.user != null) {
            p.put("user", this.user);
        }
        p.put("user_info", this.user_info);
        p.put("ServerGroup", this.uiSG("listen_ip_port"));
        p.put("ServerSession", this);
        p.put("ServerSessionObject", this);
        p.put("server_settings", ServerStatus.server_settings);
        ServerStatus.thisObj.runPlugins(p, action.equals("login"));
        Log.log("PLUGIN", 3, "PLUGIN:Completed " + action);
        return p;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void checkTempAccounts(Properties p, String serverGroup) {
        block34: {
            try {
                Properties knownBadTempAccounts;
                if (!ServerStatus.thisObj.server_info.containsKey("knownBadTempAccounts")) {
                    ServerStatus.thisObj.server_info.put("knownBadTempAccounts", new Properties());
                }
                Properties properties = knownBadTempAccounts = (Properties)ServerStatus.thisObj.server_info.get("knownBadTempAccounts");
                synchronized (properties) {
                    Enumeration<Object> keys = knownBadTempAccounts.keys();
                    while (keys.hasMoreElements()) {
                        String key = keys.nextElement().toString();
                        if (System.currentTimeMillis() - Long.parseLong(knownBadTempAccounts.getProperty(key)) <= (long)(1000 * ServerStatus.IG("temp_account_bad_timeout"))) continue;
                        knownBadTempAccounts.remove(key);
                    }
                    if (knownBadTempAccounts.containsKey(p.getProperty("username"))) {
                        Log.log("SERVER", 2, "Ignoring temp account request for username:" + p.getProperty("username"));
                        return;
                    }
                }
                String tempAccountsPath = ServerStatus.SG("temp_accounts_path");
                if (!new File_U(String.valueOf(tempAccountsPath) + "accounts/").exists()) break block34;
                File_U[] accounts = (File_U[])new File_U(String.valueOf(tempAccountsPath) + "accounts/").listFiles();
                boolean found = false;
                boolean exausted_usage = false;
                if (accounts == null) break block34;
                int x = 0;
                while (!found && x < accounts.length) {
                    block35: {
                        try {
                            File_U f = accounts[x];
                            Log.log("SERVER", 2, "Temp:" + f.getName());
                            if (f.getName().indexOf(",,") >= 0 && f.isDirectory()) {
                                Properties info;
                                String[] tokens = f.getName().split(",,");
                                Properties pp = new Properties();
                                int xx = 0;
                                while (xx < tokens.length) {
                                    boolean skip = false;
                                    String key = tokens[xx].substring(0, tokens[xx].indexOf("="));
                                    String val = tokens[xx].substring(tokens[xx].indexOf("=") + 1);
                                    if (key.equals("C")) {
                                        key = val.split("=")[0];
                                        val = val.split("=").length > 1 ? val.split("=")[1] : "";
                                        Vector<Properties> v = (Vector<Properties>)pp.get("web_customizations");
                                        if (v == null) {
                                            v = new Vector<Properties>();
                                        }
                                        Properties ppp = new Properties();
                                        ppp.put("key", key);
                                        ppp.put("value", val);
                                        v.addElement(ppp);
                                        pp.put("web_customizations", v);
                                        skip = true;
                                    }
                                    if (!skip) {
                                        pp.put(key.toUpperCase(), val);
                                    }
                                    ++xx;
                                }
                                if (!pp.getProperty("I", "").equals("") && pp.getProperty("U").equalsIgnoreCase(p.getProperty("username")) && pp.getProperty("P").equalsIgnoreCase(p.getProperty("password"))) {
                                    File_U f2 = f;
                                    int i = Integer.parseInt(pp.getProperty("I")) - 1;
                                    if (i < 0) {
                                        exausted_usage = true;
                                    } else {
                                        f2 = new File_U(f.getPath().replaceAll(",,i=" + (i + 1), ",,i=" + i));
                                        f.renameTo(f2);
                                        f = f2;
                                    }
                                }
                                if (ServerStatus.thisObj.common_code.check_date_expired_roll(pp.getProperty("EX"))) {
                                    if (!ServerStatus.SG("temp_accounts_account_expire_task").equals("")) {
                                        Vector<Properties> items = new Vector<Properties>();
                                        Properties item = new Properties();
                                        info = (Properties)Common.readXMLObject_U(String.valueOf(f.getPath()) + "/INFO.XML");
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
                                } else if (p.getProperty("username").equalsIgnoreCase(pp.getProperty("U")) && (p.getProperty("password").equalsIgnoreCase(pp.getProperty("P")) || p.getProperty("anyPass").equals("true"))) {
                                    Properties tempUser = UserTools.ut.getUser(serverGroup, pp.getProperty("T"), true);
                                    tempUser.put("username", p.getProperty("username"));
                                    tempUser.put("password", p.getProperty("password"));
                                    tempUser.put("account_expire", pp.getProperty("EX"));
                                    Properties u = (Properties)p.get("user");
                                    info = (Properties)Common.readXMLObject_U(String.valueOf(f.getPath()) + "/INFO.XML");
                                    info.remove("command");
                                    info.remove("type");
                                    u.putAll((Map<?, ?>)tempUser);
                                    u.putAll((Map<?, ?>)pp);
                                    u.putAll((Map<?, ?>)info);
                                    u.put("email", info.getProperty("emailTo", ""));
                                    Vector events = (Vector)u.get("events");
                                    if (events != null) {
                                        int xx2 = 0;
                                        while (xx2 < events.size()) {
                                            Properties event = (Properties)events.elementAt(xx2);
                                            if (event.getProperty("resolveShareEvent", "false").equals("true") && event.getProperty("linkUser") != null) {
                                                Properties linkUser = UserTools.ut.getUser(serverGroup, event.getProperty("linkUser"), true);
                                                Vector events2 = null;
                                                events2 = linkUser == null ? (Vector)com.crushftp.client.Common.CLONE(events) : (Vector)linkUser.get("events");
                                                int xxx = 0;
                                                while (events2 != null && xxx < events2.size()) {
                                                    Properties event2 = (Properties)events2.elementAt(xxx);
                                                    if (event2.getProperty("name", "").equals(event.getProperty("linkEvent", ""))) {
                                                        event.putAll((Map<?, ?>)((Properties)event2.clone()));
                                                        String event_user_action_list = ")" + event.getProperty("event_user_action_list", "") + "(";
                                                        String[] parts = event_user_action_list.split("\\)\\(");
                                                        String new_event_user_action_list = "";
                                                        int xxxx = 0;
                                                        while (xxxx < parts.length) {
                                                            if (parts[xxxx].startsWith("share_")) {
                                                                new_event_user_action_list = String.valueOf(new_event_user_action_list) + "(" + parts[xxxx].substring("share_".length()) + ")";
                                                            }
                                                            ++xxxx;
                                                        }
                                                        event.put("event_user_action_list", new_event_user_action_list);
                                                        break;
                                                    }
                                                    ++xxx;
                                                }
                                            }
                                            ++xx2;
                                        }
                                    }
                                    UserTools.mergeWebCustomizations(u, pp);
                                    UserTools.mergeWebCustomizations(u, info);
                                    UserTools.mergeWebCustomizations(u, tempUser);
                                    p.remove("permissions");
                                    p.put("virtual", XMLUsers.buildVFSXML(String.valueOf(f.getPath()) + "/"));
                                    p.put("action", "success");
                                    p.put("overwrite_permissions", "false");
                                    if (exausted_usage) {
                                        String fname = "invalid_link.html";
                                        String buildPrivs = "(read)(view)";
                                        Properties permissions = new Properties();
                                        permissions.put("/", buildPrivs);
                                        Properties dir_item = new Properties();
                                        dir_item.put("url", new File_S(String.valueOf(System.getProperty("crushftp.web", "")) + "WebInterface/" + fname).toURI().toURL().toExternalForm());
                                        dir_item.put("type", "file");
                                        Vector<Properties> v = new Vector<Properties>();
                                        v.addElement(dir_item);
                                        Properties virtual = UserTools.generateEmptyVirtual();
                                        String path = "/" + fname;
                                        if (path.endsWith("/")) {
                                            path = path.substring(0, path.length() - 1);
                                        }
                                        Properties vItem = new Properties();
                                        vItem.put("virtualPath", path);
                                        vItem.put("name", fname);
                                        vItem.put("type", "FILE");
                                        vItem.put("vItems", v);
                                        virtual.put(path, vItem);
                                        vItem = new Properties();
                                        vItem.put("name", "VFS");
                                        vItem.put("type", "DIR");
                                        vItem.put("virtualPath", "/");
                                        virtual.put("/", vItem);
                                        p.put("virtual", virtual);
                                        Vector<Properties> web_customizations = (Vector<Properties>)u.get("web_customizations");
                                        if (web_customizations == null) {
                                            web_customizations = new Vector<Properties>();
                                        }
                                        Properties replaceListingWithPage = new Properties();
                                        replaceListingWithPage.put("key", "replaceListingWithPage");
                                        replaceListingWithPage.put("value", "invalid_link.html");
                                        web_customizations.addElement(replaceListingWithPage);
                                        u.put("web_customizations", web_customizations);
                                    }
                                    found = true;
                                }
                            }
                        }
                        catch (Exception e) {
                            if (("" + e).indexOf("java.nio.file.NoSuchFileException") >= 0 || ("" + e).replace('\\', '/').indexOf("./TempAccounts/accounts") >= 0) break block35;
                            Log.log("SERVER", 1, e);
                        }
                    }
                    ++x;
                }
                if (!found && !p.getProperty("password", "").equals("")) {
                    knownBadTempAccounts.put(p.getProperty("username"), String.valueOf(System.currentTimeMillis()));
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
            }
        }
    }

    public static SimpleDateFormat updateDateCustomizations(SimpleDateFormat date_time, Properties user) {
        Vector customizations = (Vector)user.get("web_customizations");
        if (customizations == null) {
            customizations = new Vector();
        }
        String date = "";
        String time = "";
        int x = 0;
        while (x < customizations.size()) {
            Properties pp = (Properties)customizations.elementAt(x);
            String key = pp.getProperty("key");
            if (key.equalsIgnoreCase("DATE_FORMAT_TEXT")) {
                date = pp.getProperty("value");
            }
            if (key.equalsIgnoreCase("TIME_FORMAT_TEXT")) {
                time = pp.getProperty("value");
            }
            ++x;
        }
        if (date.length() != 0 || time.length() != 0) {
            date_time = new SimpleDateFormat(String.valueOf(date) + " " + time, Locale.US);
        }
        return date_time;
    }

    public boolean verify_user(String theUser, String thePass) {
        return this.verify_user(theUser, thePass, false, true);
    }

    public boolean verify_user(String theUser, String thePass, boolean anyPass) {
        return this.verify_user(theUser, thePass, anyPass, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean verify_user(String theUser, String thePass, boolean anyPass, boolean doAfterLogin) {
        int invalid_username_attempts;
        long invalid_username_time;
        Properties loginReason;
        block184: {
            Properties p;
            String SAMLResponse;
            String templateUser;
            block186: {
                Object tempUser;
                if (!ServerStatus.siBG("allow_logins")) {
                    return false;
                }
                if (theUser.startsWith("~")) {
                    this.shareVFS = true;
                    theUser = theUser.substring(1);
                    this.uiPUT("user_name", theUser);
                }
                if ((theUser = VRL.vrlDecode(theUser)).toUpperCase().startsWith("$ASCII$")) {
                    theUser = theUser.substring(7);
                    this.uiPUT("user_name", theUser);
                    this.uiPUT("proxy_ascii_binary", "ascii");
                }
                if (ServerStatus.BG("strip_slashes") && theUser.indexOf("\\") >= 0) {
                    theUser = theUser.substring(theUser.indexOf("\\") + 1);
                    this.uiPUT("user_name", theUser);
                }
                if (ServerStatus.BG("strip_slashes") && theUser.indexOf("/") >= 0) {
                    theUser = theUser.substring(theUser.indexOf("/") + 1);
                    this.uiPUT("user_name", theUser);
                }
                if (ServerStatus.BG("secondary_login_via_email") && theUser.indexOf("@") >= 0 && UserTools.user_email_cache.containsKey(String.valueOf(this.uiSG("listen_ip_port")) + ":" + theUser.toUpperCase())) {
                    String username2 = UserTools.user_email_cache.getProperty(String.valueOf(this.uiSG("listen_ip_port")) + ":" + theUser.toUpperCase());
                    Log.log("SERVER", 0, "Using XF table for email " + theUser + " to convert to:" + username2);
                    theUser = username2;
                    this.uiPUT("user_name", theUser);
                    this.uiPUT("user_name_original", theUser);
                }
                if (UserTools.checkPassword(thePass)) {
                    anyPass = true;
                }
                if (theUser.equalsIgnoreCase("default")) {
                    return false;
                }
                if (theUser.equalsIgnoreCase("TempAccount")) {
                    return false;
                }
                loginReason = new Properties();
                loginReason.put("no_log_invalid_password", this.user_info.getProperty("no_log_invalid_password", "false"));
                invalid_username_time = Long.parseLong(ServerStatus.siPG("invalid_usernames").getProperty(theUser.toUpperCase(), "0:0").split(":")[0]);
                invalid_username_attempts = Integer.parseInt(ServerStatus.siPG("invalid_usernames").getProperty(theUser.toUpperCase(), "0:0").split(":")[1]);
                if (invalid_username_time > 0L && invalid_username_time > new Date().getTime() - (long)(ServerStatus.IG("invalid_usernames_seconds") * 1000) && invalid_username_attempts > ServerStatus.IG("invalid_usernames_seconds_attempts")) {
                    ServerStatus.siPG("invalid_usernames").put(theUser.toUpperCase(), String.valueOf(invalid_username_time) + ":" + (invalid_username_attempts + 1));
                    this.add_log("Ignoring user login request and returning failure due to invalid_usernames_seconds not being long enough for retry:" + new Date(invalid_username_time + (long)(ServerStatus.IG("invalid_usernames_seconds") * 1000)) + " attempts:" + invalid_username_attempts, "USER");
                    return false;
                }
                this.user = null;
                Properties u = new Properties();
                Properties temp_p = new Properties();
                temp_p.put("user", u);
                temp_p.put("username", theUser);
                temp_p.put("password", thePass);
                temp_p.put("anyPass", String.valueOf(anyPass));
                SessionCrush.checkTempAccounts(temp_p, this.uiSG("listen_ip_port"));
                templateUser = "";
                SAMLResponse = this.uiSG("SAMLResponse");
                if (!temp_p.getProperty("action", "").equalsIgnoreCase("success")) {
                    this.user = UserTools.ut.verify_user(ServerStatus.thisObj, theUser, thePass, this.uiSG("listen_ip_port"), this, this.uiIG("user_number"), this.uiSG("user_ip"), this.uiIG("user_port"), this.server_item, loginReason, anyPass);
                }
                if (!anyPass && this.user == null && !theUser.toLowerCase().equals("anonymous")) {
                    this.user_info.put("plugin_user_auth_info", "Password incorrect.");
                }
                if (this.user != null && !theUser.equals("SSO_UNI") && !theUser.toUpperCase().startsWith("SSO_SAML")) break block186;
                p = temp_p;
                if (!p.getProperty("action", "").equalsIgnoreCase("success")) {
                    tempUser = UserTools.ut.verify_user(ServerStatus.thisObj, theUser, thePass, this.uiSG("listen_ip_port"), this, this.uiIG("user_number"), this.uiSG("user_ip"), this.uiIG("user_port"), this.server_item, loginReason, true);
                    p.put("authenticationOnlyExists", String.valueOf(tempUser != null));
                    p = this.runPlugin("login", p);
                    templateUser = p.getProperty("templateUser", "");
                }
                tempUser = ServerStatus.thisObj;
                synchronized (tempUser) {
                    block181: {
                        if (!p.getProperty("action", "").equalsIgnoreCase("success") && !p.getProperty("dump_xml_user", "false").equals("true")) break block181;
                        theUser = p.getProperty("username", theUser);
                        if (p.getProperty("authenticationOnly", "false").equalsIgnoreCase("true")) {
                            Properties user2;
                            Vector extraLinkedVfs;
                            Log.log("LOGIN", 2, String.valueOf(LOC.G("Plugin authenticated user (not user manager):")) + theUser);
                            this.user = UserTools.ut.verify_user(ServerStatus.thisObj, theUser, thePass, this.uiSG("listen_ip_port"), this, this.uiIG("user_number"), this.uiSG("user_ip"), this.uiIG("user_port"), this.server_item, loginReason, true);
                            if (p.getProperty("create_local_user", "").equals("true") && this.user == null) {
                                Properties temp_user = UserTools.ut.getUser(this.uiSG("listen_ip_port"), p.getProperty("create_local_user_template", ""), false);
                                temp_user.put("password", ServerStatus.thisObj.common_code.encode_pass(Common.makeBoundary(), "SHA512"));
                                temp_user.put("email", p.getProperty("email", ""));
                                UserTools.writeUser(this.uiSG("listen_ip_port"), theUser, temp_user);
                                VFS tempVFS = UserTools.ut.getVFS(this.uiSG("listen_ip_port"), p.getProperty("create_local_user_template", ""));
                                UserTools.writeVFS(this.uiSG("listen_ip_port"), theUser, tempVFS);
                                this.user = UserTools.ut.verify_user(ServerStatus.thisObj, theUser, thePass, this.uiSG("listen_ip_port"), this, this.uiIG("user_number"), this.uiSG("user_ip"), this.uiIG("user_port"), this.server_item, loginReason, true);
                                if (!p.getProperty("create_local_user_template_task", "").equals("")) {
                                    Properties item = (Properties)p.clone();
                                    item.putAll((Map<?, ?>)p);
                                    item.put("url", "file://" + this.uiSG("listen_ip_port") + "/" + theUser);
                                    item.put("the_file_name", theUser);
                                    item.put("the_file_path", this.uiSG("listen_ip_port"));
                                    Vector<Properties> items = new Vector<Properties>();
                                    items.addElement(item);
                                    Properties event = new Properties();
                                    event.put("event_plugin_list", p.getProperty("create_local_user_template_task", ""));
                                    event.put("name", "PluginCreateUserTask:" + theUser);
                                    ServerStatus.thisObj.events6.doEventPlugin(null, event, this, items);
                                }
                            }
                            if (p.getProperty("CrushSSO_trusted", "").equals("true")) {
                                this.user.put("site", Common.replace_str(this.user.getProperty("site").toUpperCase(), "(CONNECT)", ""));
                                this.user.put("site", Common.replace_str(this.user.getProperty("site").toUpperCase(), "(PREF", "(NOTHING"));
                                this.user.put("site", Common.replace_str(this.user.getProperty("site").toUpperCase(), "(USER", "(NOTHING"));
                                this.user.put("site", Common.replace_str(this.user.getProperty("site").toUpperCase(), "(JOB", "(NOTHING"));
                                this.user.put("site", Common.replace_str(this.user.getProperty("site").toUpperCase(), "(SERVER", "(NOTHING"));
                                this.user.put("site", Common.replace_str(this.user.getProperty("site").toUpperCase(), "(UPDATE", "(NOTHING"));
                                this.user.put("site", Common.replace_str(this.user.getProperty("site").toUpperCase(), "(REPORT", "(NOTHING"));
                                this.user.put("site", Common.replace_str(this.user.getProperty("site").toUpperCase(), "(SHARE", "(NOTHING"));
                            }
                            if ((extraLinkedVfs = (Vector)p.get("linked_vfs")) != null) {
                                Vector linked_vfs = (Vector)this.user.get("linked_vfs");
                                if (linked_vfs == null) {
                                    linked_vfs = new Vector();
                                }
                                this.user.put("linked_vfs", linked_vfs);
                                linked_vfs.addAll(extraLinkedVfs);
                            }
                            if ((user2 = (Properties)p.get("user")) != null) {
                                Enumeration<Object> keys = user2.keys();
                                while (keys.hasMoreElements()) {
                                    Object val;
                                    String key = keys.nextElement().toString();
                                    if (key == null || !key.startsWith("ldap_") || this.user == null || (val = user2.get(key)) == null) continue;
                                    this.user.put(key, val);
                                }
                            }
                            if (p.containsKey("user_info") && ((Properties)p.get("user_info")).getProperty("skip_otp", "false").equalsIgnoreCase("true")) {
                                UserTools.disable_OTP(this.user);
                            }
                        } else {
                            try {
                                loginReason.put("reason", "valid user");
                                this.user = u;
                                Properties virtual = UserTools.generateEmptyVirtual();
                                if (!p.getProperty("templateUser", "").equals("")) {
                                    Vector extraLinkedVfs = (Vector)p.get("linked_vfs");
                                    if (extraLinkedVfs != null && extraLinkedVfs.size() > 0) {
                                        this.user_info.put("ldap_role_template_users", extraLinkedVfs);
                                    }
                                    Vector<String> ichain = new Vector<String>();
                                    ichain.addElement("default");
                                    int x = 0;
                                    while (x < p.getProperty("templateUser", "").split(";").length) {
                                        if (ichain.indexOf(p.getProperty("templateUser", "").split(";")[x].trim()) < 0) {
                                            ichain.addElement(p.getProperty("templateUser", "").split(";")[x].trim());
                                        }
                                        ++x;
                                    }
                                    if (extraLinkedVfs != null) {
                                        ichain.addAll(extraLinkedVfs);
                                    }
                                    Log.log("LOGIN", 1, "Building list of user configuations in layers for inheritance chain:" + ichain);
                                    this.user.put("ichain", ichain);
                                    x = 0;
                                    while (x < ichain.size()) {
                                        Properties tempUser2 = UserTools.ut.getUser(this.uiSG("listen_ip_port"), ichain.elementAt(x).toString(), ServerStatus.BG("resolve_inheritance"));
                                        if (tempUser2 != null) {
                                            UserTools.mergeWebCustomizations(this.user, tempUser2);
                                            UserTools.mergeEvents(this.user, tempUser2);
                                            Enumeration<Object> keys = tempUser2.keys();
                                            Log.log("LOGIN", 1, String.valueOf(LOC.G("Setting templateUser's settings:")) + p.size());
                                            while (keys.hasMoreElements()) {
                                                String key = keys.nextElement().toString();
                                                if (key.equalsIgnoreCase("username") || key.equalsIgnoreCase("user_name") || key.equalsIgnoreCase("password") || key.equals("max_logins") && tempUser2.get(key).equals("-1") || key.equals("email") && tempUser2.getProperty(key, "").equals("") || key.equals("first_name") && tempUser2.getProperty(key, "").equals("") || key.equals("last_name") && tempUser2.getProperty(key, "").equals("") || key.equals("account_expire") && tempUser2.getProperty(key, "").equals("") && !p.containsKey("magic_dir_account_expire")) continue;
                                                if (key.indexOf("expire") >= 0) {
                                                    Log.log("SERVER", 2, "key=" + key + " val=" + tempUser2.getProperty(key, ""));
                                                }
                                                try {
                                                    if (key.equals("admin_group_name")) {
                                                        this.user.put(key, String.valueOf(this.user.getProperty(key, "")) + (this.user.getProperty(key, "").equals("") ? "" : ",") + tempUser2.get(key));
                                                        continue;
                                                    }
                                                    this.user.put(key, tempUser2.get(key));
                                                }
                                                catch (Exception exception) {
                                                    // empty catch block
                                                }
                                            }
                                        }
                                        ++x;
                                    }
                                    if (p.containsKey("magic_dir_account_expire") && !p.getProperty("magic_dir_account_expire", "").equals("")) {
                                        if (this.user.getProperty("account_expire", "").equals("")) {
                                            this.user.put("account_expire", p.getProperty("magic_dir_account_expire", ""));
                                        } else {
                                            SimpleDateFormat sdfExpire = new SimpleDateFormat("MM/dd/yy hh:mm aa", Locale.US);
                                            if (sdfExpire.parse(p.getProperty("magic_dir_account_expire", "")).getTime() < sdfExpire.parse(this.user.getProperty("account_expire", "")).getTime()) {
                                                this.user.put("account_expire", p.getProperty("magic_dir_account_expire", ""));
                                            }
                                        }
                                    }
                                    if (extraLinkedVfs != null) {
                                        Vector linked_vfs = (Vector)this.user.get("linked_vfs");
                                        if (linked_vfs == null) {
                                            linked_vfs = new Vector();
                                        }
                                        this.user.put("linked_vfs", linked_vfs);
                                        linked_vfs.addAll(extraLinkedVfs);
                                    }
                                    Properties virtual2 = null;
                                    int x2 = 0;
                                    while (x2 < p.getProperty("templateUser", "").split(";").length) {
                                        VFS tempVFS = UserTools.ut.getVFS(this.uiSG("listen_ip_port"), p.getProperty("templateUser", "").split(";")[x2].trim());
                                        if (virtual2 == null) {
                                            virtual2 = (Properties)tempVFS.homes.elementAt(0);
                                        } else {
                                            virtual2.putAll((Map<?, ?>)((Properties)tempVFS.homes.elementAt(0)));
                                        }
                                        try {
                                            Properties permissions = (Properties)p.get("permissions");
                                            Vector v = (Vector)virtual2.get("vfs_permissions_object");
                                            Properties permissions2 = (Properties)v.elementAt(0);
                                            if (permissions2.containsKey("/") && permissions.containsKey("/") && permissions != permissions2) {
                                                permissions2.remove("/");
                                            }
                                            permissions.putAll((Map<?, ?>)permissions2);
                                            permissions2.putAll((Map<?, ?>)permissions);
                                        }
                                        catch (Exception e) {
                                            Log.log("LOGIN", 1, e);
                                        }
                                        ++x2;
                                    }
                                    virtual = virtual2;
                                    this.user.put("root_dir", "/");
                                    Log.log("SERVER", 3, "Dump of user properties from plugin:" + this.user);
                                }
                                if (p.containsKey("user_info") && ((Properties)p.get("user_info")).getProperty("skip_otp", "false").equalsIgnoreCase("true")) {
                                    UserTools.disable_OTP(this.user);
                                }
                                if (p.containsKey("virtual")) {
                                    virtual = (Properties)p.get("virtual");
                                } else {
                                    Vector VFSItems = (Vector)p.get("VFSItems");
                                    int x = 0;
                                    while (x < VFSItems.size()) {
                                        Properties pp = (Properties)VFSItems.elementAt(x);
                                        String path2 = String.valueOf(pp.getProperty("dir")) + pp.getProperty("name");
                                        if (path2.endsWith("/")) {
                                            path2 = path2.substring(0, path2.length() - 1);
                                        }
                                        Properties vItem = new Properties();
                                        vItem.put("name", pp.getProperty("name"));
                                        vItem.put("type", pp.getProperty("type", "FILE"));
                                        vItem.put("virtualPath", path2);
                                        if (pp.containsKey("data")) {
                                            VRL vrl;
                                            Vector data;
                                            Properties item;
                                            if (pp.get("data") != null && pp.get("data") instanceof Vector && ((Vector)pp.get("data")).size() > 0 && ((Vector)pp.get("data")).get(0) instanceof Properties && (item = (Properties)(data = (Vector)pp.get("data")).get(0)) != null && item.containsKey("url") && !item.getProperty("url", "").equals("") && (vrl = new VRL(item.getProperty("url", ""))) != null && vrl.getConfig() != null && vrl.getConfig().size() > 0) {
                                                item.putAll((Map<?, ?>)vrl.getConfig());
                                                item.put("url", vrl.toString());
                                            }
                                            vItem.put("vItems", pp.get("data"));
                                        } else {
                                            vItem.put("vItems", new Vector());
                                        }
                                        virtual.put(path2, vItem);
                                        ++x;
                                    }
                                    if (p.getProperty("overwrite_permissions", "true").equals("true")) {
                                        Properties permissions = (Properties)p.get("permissions");
                                        Vector v = (Vector)virtual.get("vfs_permissions_object");
                                        Properties permissions2 = (Properties)v.elementAt(0);
                                        if (permissions2.containsKey("/") && permissions.containsKey("/") && permissions != permissions2) {
                                            permissions2.remove("/");
                                        }
                                        permissions.putAll((Map<?, ?>)permissions2);
                                        permissions2.putAll((Map<?, ?>)permissions);
                                    }
                                }
                                this.setVFS(VFS.getVFS(virtual));
                            }
                            catch (Exception e) {
                                Log.log("LOGIN", 1, e);
                            }
                        }
                        if (p.getProperty("dump_xml_user", "false").equals("true")) {
                            this.user.remove("username");
                            this.user.remove("userName");
                            this.user.remove("userpass");
                            this.user.remove("userPass");
                            this.user.remove("virtualUser");
                            this.user.remove("id");
                            this.user.remove("SQL_ID");
                            this.user.put("root_dir", "/");
                            this.user.remove("real_path_to_user");
                            this.user.remove("vfs_modified");
                            this.user.remove("x_lastName");
                            this.user.remove("admin_group_name");
                            this.user.remove("user_name");
                            this.user.remove("defaultsVersion");
                            UserTools.stripUser(this.user, UserTools.ut.getUser(this.uiSG("listen_ip_port"), "default", false));
                            UserTools.writeUser(this.uiSG("listen_ip_port"), theUser, this.user);
                            UserTools.writeVFS(this.uiSG("listen_ip_port"), theUser, this.uVFS);
                            this.add_log("Dump xml user, return false.", "USER");
                            return false;
                        }
                        break block186;
                    }
                    if (!p.getProperty("redirect_url", "").equals("")) {
                        if (p.getProperty("redirect_url", "").startsWith(ServerStatus.SG("http_redirect_base")) || p.getProperty("redirect_url", "").indexOf(":/") < 0) {
                            this.user_info.put("redirect_url", p.getProperty("redirect_url"));
                        }
                    }
                }
            }
            Log.log("LOGIN", 3, "Loggining in...");
            if (this.user != null && this.uVFS == null && this.user.getProperty("virtualUser", "false").equalsIgnoreCase("false")) {
                this.setVFS(UserTools.ut.getVFS(this.uiSG("listen_ip_port"), this.user.getProperty("username")));
                Log.log("LOGIN", 2, String.valueOf(LOC.G("Got VFS from real user:")) + this.uVFS);
            }
            if (this.user != null) {
                p = new Properties();
                p.put("user", this.user);
                if (!this.user.getProperty("username", "").equals("template")) {
                    theUser = this.user.getProperty("username", theUser);
                }
                p.put("username", theUser);
                p.put("password", thePass);
                p.put("allowLogin", "true");
                if (this.uVFS != null) {
                    p.put("uVFSObject", this.uVFS);
                }
                if (doAfterLogin) {
                    this.runPlugin("afterLogin", p);
                }
                if (!p.getProperty("allowLogin", "true").equals("true")) {
                    this.user = null;
                    this.setVFS(null);
                    this.add_log("A plugin rejected the login. Login failed.", "USER");
                    return false;
                }
                Log.log("LOGIN", 3, "After login...");
                UserTools.setupVFSLinking(this.uiSG("listen_ip_port"), theUser, this.uVFS, this.user);
                if (this.uVFS != null) {
                    this.uVFS.setUserPassIpPortProtocol(this.uiSG("user_name"), this.uiSG("current_password"), this.uiSG("user_ip"), this.uiIG("user_port"), this.uiSG("user_protocol"), this.user_info, this.user, this);
                    Vector homes = this.uVFS.homes;
                    int x = 0;
                    while (x < homes.size()) {
                        Properties virtual = (Properties)homes.elementAt(x);
                        Vector permissions = (Vector)virtual.get("vfs_permissions_object");
                        Properties vfs_permissions_object = (Properties)permissions.elementAt(0);
                        Enumeration<Object> keys = virtual.keys();
                        while (keys.hasMoreElements()) {
                            Properties vItem;
                            String val2;
                            String val22;
                            String key2;
                            String key = keys.nextElement().toString();
                            if (key.equals("vfs_permissions_object") || (key2 = ServerStatus.change_vars_to_values_static(key, this.user, this.user_info, this)).equals(key)) continue;
                            if (vfs_permissions_object.containsKey(key.toUpperCase())) {
                                val22 = vfs_permissions_object.remove(key.toUpperCase()).toString();
                                vfs_permissions_object.put(key2.toUpperCase(), val22);
                            }
                            if (vfs_permissions_object.containsKey(String.valueOf(key.toUpperCase()) + "/")) {
                                val22 = vfs_permissions_object.remove(String.valueOf(key.toUpperCase()) + "/").toString();
                                vfs_permissions_object.put(String.valueOf(key2.toUpperCase()) + "/", val22);
                            }
                            if ((val2 = (vItem = (Properties)virtual.remove(key)).getProperty("virtualPath")) != null) {
                                vItem.put("virtualPath", ServerStatus.change_vars_to_values_static(val2, this.user, this.user_info, this));
                            }
                            if ((val2 = vItem.getProperty("name")) != null) {
                                vItem.put("name", ServerStatus.change_vars_to_values_static(val2, this.user, this.user_info, this));
                            }
                            virtual.put(key2, vItem);
                        }
                        ++x;
                    }
                }
                if (ServerStatus.BG("track_user_md4_hashes") && !thePass.startsWith("NTLM:")) {
                    String md4_user = ServerStatus.thisObj.common_code.encode_pass(theUser, "MD4", "").substring("MD4:".length());
                    String md4_pass = ServerStatus.thisObj.common_code.encode_pass(thePass, "MD4", "").substring("MD4:".length());
                    Properties md4_hashes = (Properties)ServerStatus.thisObj.server_info.get("md4_hashes");
                    if (md4_hashes == null) {
                        md4_hashes = new Properties();
                    }
                    ServerStatus.thisObj.server_info.put("md4_hashes", md4_hashes);
                    if (!md4_hashes.getProperty(md4_user, "").equals(md4_pass)) {
                        md4_hashes.put(md4_user, md4_pass);
                        Properties permissions = md4_hashes;
                        synchronized (permissions) {
                            block183: {
                                ObjectOutputStream oos = null;
                                try {
                                    try {
                                        new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "md4_hashes2.obj").delete();
                                        oos = new ObjectOutputStream(new FileOutputStream(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "md4_hashes2.obj")));
                                        oos.writeObject(md4_hashes);
                                        oos.flush();
                                        oos.close();
                                        oos = null;
                                        new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "md4_hashes.obj").delete();
                                        new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "md4_hashes2.obj").renameTo(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "md4_hashes.obj"));
                                    }
                                    catch (Exception e) {
                                        Log.log("SERVER", 0, e);
                                        try {
                                            if (oos != null) {
                                                oos.close();
                                            }
                                            break block183;
                                        }
                                        catch (Exception key2) {}
                                        break block183;
                                    }
                                }
                                catch (Throwable key) {
                                    try {
                                        if (oos != null) {
                                            oos.close();
                                        }
                                    }
                                    catch (Exception key2) {
                                        // empty catch block
                                    }
                                    throw key;
                                }
                                try {
                                    if (oos != null) {
                                        oos.close();
                                    }
                                }
                                catch (Exception key2) {
                                    // empty catch block
                                }
                            }
                        }
                    }
                }
            }
            this.uiPUT("current_dir", this.SG("root_dir"));
            if (this.user != null && this.uVFS != null && (this.user.getProperty("username", "").equalsIgnoreCase("TEMPLATE") || this.user.getProperty("unlocked_proxy_homedir", "false").equals("true"))) {
                Vector listing = new Vector();
                this.uiPUT("user_name", this.uiSG("user_name").replace(':', ';'));
                this.uVFS.setUserPassIpPortProtocol(this.uiSG("user_name"), this.uiSG("current_password"), this.uiSG("user_ip"), this.uiIG("user_port"), this.uiSG("user_protocol"), this.user_info, this.user, this);
                if ((this.uiSG("user_name").equalsIgnoreCase("anonymous") || this.uiSG("user_name").trim().equals("")) && ServerStatus.BG("ignore_web_anonymous_proxy")) {
                    return false;
                }
                try {
                    Properties p2;
                    this.uVFS.getListing(listing, "/");
                    if (listing.size() <= 0 || !(p2 = (Properties)listing.elementAt(0)).getProperty("type").equalsIgnoreCase("DIR")) break block184;
                    p2 = this.uVFS.get_item(String.valueOf(p2.getProperty("root_dir")) + p2.getProperty("name") + "/");
                    GenericClient c = this.uVFS.getClient(p2);
                    try {
                        if (!this.uiBG("skip_proxy_check")) {
                            this.uiPUT("skip_proxy_check", "false");
                            String userMessage = c.getConfig("userMessage", null);
                            this.user.remove("welcome_message2");
                            if (userMessage != null) {
                                String[] lines = userMessage.split("\\r\\n");
                                userMessage = "";
                                int x = 0;
                                while (x < lines.length - 1) {
                                    String param;
                                    if (lines[x].startsWith("230-user.")) {
                                        param = lines[x].substring("230-user.".length()).trim();
                                        this.user.put(param.split("=")[0], param.split("=")[1]);
                                    } else if (lines[x].startsWith("230-user_info.")) {
                                        param = lines[x].substring("230-user_info.".length()).trim();
                                        this.user_info.put(param.split("=")[0], param.split("=")[1]);
                                    } else if (lines[x].startsWith("230-")) {
                                        userMessage = String.valueOf(userMessage) + lines[x].substring(4) + CRLF;
                                        if (lines[x].substring(4).startsWith("PASSWORD EXPIRATION:")) {
                                            String expireDate = lines[x].substring(lines[x].indexOf(":") + 1).trim();
                                            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
                                            SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.US);
                                            this.put("expire_password_when", sdf2.format(sdf1.parse(expireDate)));
                                        }
                                    } else {
                                        userMessage = String.valueOf(userMessage) + lines[x] + CRLF;
                                    }
                                    ++x;
                                }
                                if (!userMessage.equals("")) {
                                    this.user.put("welcome_message2", userMessage.trim());
                                }
                            }
                            if (c.getConfig("default_dir", "").indexOf("/") >= 0) {
                                String defaultDir = c.getConfig("default_dir", "/");
                                if (!this.server_item.getProperty("root_directory", "/").equals("/")) {
                                    this.uiPUT("default_current_dir", this.server_item.getProperty("root_directory", "/"));
                                }
                                if (!defaultDir.equals("/")) {
                                    this.put("default_current_dir", defaultDir);
                                }
                                if (c.getConfig("default_pwd", "").indexOf("(unlocked)") >= 0 || this.user.getProperty("unlocked_proxy_homedir", "false").equals("true")) {
                                    this.put("default_current_dir_unlocked", "true");
                                }
                            }
                        }
                        if (this.containsKey("default_current_dir")) {
                            this.uiPUT("default_current_dir", this.getProperty("default_current_dir"));
                        }
                    }
                    finally {
                        c = this.uVFS.releaseClient(c);
                    }
                    if (com.crushftp.client.Common.dmz_mode) {
                        Vector queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                        Properties action = new Properties();
                        action.put("type", "GET:USER");
                        action.put("id", Common.makeBoundary());
                        action.put("username", theUser);
                        action.put("password", thePass);
                        action.put("SAMLResponse", SAMLResponse);
                        action.put("user_ip", this.uiSG("user_ip"));
                        action.put("need_response", "true");
                        try {
                            action.put("preferred_port", String.valueOf(new VRL(p2.getProperty("url")).getPort()));
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 0, e);
                            Log.log("SERVER", 0, "GET_USER1:PREFERRED_PORT:" + new VRL(p2.getProperty("url")).safe());
                        }
                        queue.addElement(action);
                        Thread.currentThread().setName(String.valueOf(Thread.currentThread().getName()) + ": Waiting for lookup:" + theUser + ":" + new Date());
                        action = UserTools.waitResponse(action, 60);
                        if (!(templateUser.equals("") || action != null && action.containsKey("user"))) {
                            action = new Properties();
                            action.put("type", "GET:USER");
                            action.put("id", Common.makeBoundary());
                            action.put("username", templateUser);
                            action.put("password", thePass);
                            action.put("SAMLResponse", SAMLResponse);
                            action.put("need_response", "true");
                            action.put("user_ip", this.uiSG("user_ip"));
                            try {
                                action.put("preferred_port", String.valueOf(new VRL(p2.getProperty("url")).getPort()));
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 0, e);
                                Log.log("SERVER", 0, "GET_USER2:PREFERRED_PORT:" + new VRL(p2.getProperty("url")).safe());
                            }
                            queue.addElement(action);
                            action = UserTools.waitResponse(action, 60);
                        }
                        if (action != null && action.containsKey("user")) {
                            if (action.containsKey("internal_server_data")) {
                                this.putAll((Properties)action.get("internal_server_data"));
                            }
                            this.user = (Properties)action.get("user");
                            Vector homes = (Vector)action.get("vfs");
                            Properties permission = this.uVFS.getPermission0();
                            Properties tempPermission0 = null;
                            Vector<String> log_user_vfs_items = new Vector<String>();
                            int x = homes.size() - 1;
                            while (x >= 0) {
                                String key;
                                Properties tempPermission;
                                Properties tempVFS = (Properties)homes.elementAt(x);
                                tempVFS.remove("");
                                Vector tempPermissionHomes = (Vector)tempVFS.get("vfs_permissions_object");
                                tempPermission0 = tempPermission = (Properties)tempPermissionHomes.elementAt(0);
                                Enumeration<Object> keys = tempPermission.keys();
                                while (keys.hasMoreElements()) {
                                    key = keys.nextElement().toString();
                                    if (key.indexOf("/", 1) > 0) {
                                        permission.put("/" + p2.getProperty("name").toUpperCase() + key, tempPermission.getProperty(key));
                                        continue;
                                    }
                                    if (tempPermission.size() != 1 || !key.equals("/")) continue;
                                    permission.put("/INTERNAL/", tempPermission.getProperty(key));
                                }
                                keys = tempVFS.keys();
                                while (keys.hasMoreElements()) {
                                    key = keys.nextElement().toString();
                                    if (key.equalsIgnoreCase("/") || key.equals("vfs_permissions_object")) continue;
                                    Properties vItem = (Properties)tempVFS.get(key);
                                    try {
                                        log_user_vfs_items.addElement(String.valueOf(key) + ":" + VRL.safe((Properties)((Vector)vItem.get("vItems")).elementAt(0)).getProperty("url") + ":" + tempPermission.getProperty(String.valueOf(key.toUpperCase()) + "/"));
                                    }
                                    catch (Exception e) {
                                        Log.log("SERVER", 1, "Error with key:" + key);
                                        Log.log("SERVER", 1, e);
                                    }
                                }
                                --x;
                            }
                            Vector<String> unique_items = new Vector<String>();
                            Enumeration<Object> perm_keys = permission.keys();
                            while (perm_keys.hasMoreElements()) {
                                String unique_item;
                                String path;
                                String key = perm_keys.nextElement().toString();
                                if (key.startsWith("/INTERNAL/") && !key.equals("/INTERNAL/")) {
                                    path = key.substring("/INTERNAL/".length(), key.length());
                                    unique_item = path.substring(0, path.indexOf("/"));
                                    if (unique_items.contains(unique_item)) continue;
                                    unique_items.add(unique_item);
                                    continue;
                                }
                                if (!key.startsWith("/INTERNAL1/") || key.equals("/INTERNAL1/") || unique_items.contains(unique_item = (path = key.substring("/INTERNAL1/".length(), key.length())).substring(0, path.indexOf("/")))) continue;
                                unique_items.add(unique_item);
                            }
                            this.add_log("[" + this.server_item.getProperty("serverType", "ftp") + ":" + this.uiSG("user_number") + ":" + this.uiSG("user_name") + ":" + this.uiSG("user_ip") + "] " + ":VFS items at login:" + log_user_vfs_items.size(), "USER");
                            int x3 = 0;
                            while (x3 < log_user_vfs_items.size()) {
                                this.add_log("[" + this.server_item.getProperty("serverType", "ftp") + ":" + this.uiSG("user_number") + ":" + this.uiSG("user_name") + ":" + this.uiSG("user_ip") + "] " + ":VFS item " + (x3 + 1) + ":" + log_user_vfs_items.elementAt(x3), "USER");
                                ++x3;
                            }
                            if (unique_items.size() == 1) {
                                Enumeration<Object> keys = permission.keys();
                                Properties perm2 = new Properties();
                                while (keys.hasMoreElements()) {
                                    String key = (String)keys.nextElement();
                                    if (key.equals("/INTERNAL/" + unique_items.get(0) + "/") || key.equals("/INTERNAL1/" + unique_items.get(0) + "/")) continue;
                                    if (key.equals("/INTERNAL/")) {
                                        perm2.put("/INTERNAL/", permission.getProperty("/INTERNAL/" + unique_items.get(0) + "/"));
                                        continue;
                                    }
                                    if (key.equals("/INTERNAL1/")) {
                                        perm2.put("/INTERNAL1/", permission.getProperty("/INTERNAL1/" + unique_items.get(0) + "/"));
                                        continue;
                                    }
                                    if (key.equals("/")) {
                                        perm2.put("/", permission.get("/"));
                                        continue;
                                    }
                                    String newKey = "/INTERNAL/" + key.substring(key.indexOf(String.valueOf((String)unique_items.get(0)) + "/") + (String.valueOf((String)unique_items.get(0)) + "/").length(), key.length());
                                    perm2.put(newKey, permission.get(key));
                                    newKey = "/INTERNAL1/" + key.substring(key.indexOf(String.valueOf((String)unique_items.get(0)) + "/") + (String.valueOf((String)unique_items.get(0)) + "/").length(), key.length());
                                    perm2.put(newKey, permission.get(key));
                                }
                                permission.clear();
                                permission.putAll((Map<?, ?>)new HashMap<Object, Object>(perm2));
                            }
                            if (unique_items.size() > 1) {
                                permission.put("/INTERNAL/", permission.getProperty("/"));
                                permission.put("/INTERNAL1/", permission.getProperty("/"));
                            }
                        }
                    }
                    if (ServerStatus.BG("learning_proxy")) {
                        Properties temp_user = new Properties();
                        temp_user.put("username", theUser);
                        temp_user.put("password", ServerStatus.thisObj.common_code.encode_pass(thePass, ServerStatus.SG("password_encryption"), ""));
                        temp_user.put("root_dir", "/");
                        temp_user.put("userVersion", "6");
                        temp_user.put("version", "1.0");
                        temp_user.put("max_logins", "0");
                        UserTools.writeUser(String.valueOf(this.uiSG("listen_ip_port")) + "_learning", theUser, temp_user);
                    }
                }
                catch (Exception e) {
                    Log.log("LOGIN", 2, e);
                    this.user_info.put("lastProxyError", String.valueOf(e.getMessage()));
                    boolean hack = this.checkHackUsernames(theUser);
                    this.doErrorEvent(e);
                    if (!(hack || theUser.equals("") || theUser.equals("anonymous") || com.crushftp.client.Common.dmz_mode)) {
                        try {
                            Properties info = new Properties();
                            info.put("alert_type", "bad_login");
                            info.put("alert_sub_type", "username");
                            info.put("alert_timeout", "0");
                            info.put("alert_max", "0");
                            info.put("alert_msg", String.valueOf(theUser) + (loginReason.getProperty("reason", "").equals("valid user") ? " does exist" : " does not exist"));
                            if (loginReason.getProperty("no_log_invalid_password", "false").equals("false")) {
                                ServerStatus.thisObj.runAlerts("security_alert", info, this.user_info, this);
                            }
                        }
                        catch (Exception ee) {
                            Log.log("BAN", 1, ee);
                        }
                    }
                    this.add_log("Error during user validation, debug level 2 for details" + e, "USER");
                    return false;
                }
            }
        }
        if (this.BG("expire_password") || this.SG("expire_password_when").equals("01/01/1978 12:00:00 AM")) {
            try {
                String s = this.SG("expire_password_when");
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.US);
                Date d = null;
                try {
                    d = sdf.parse(s);
                }
                catch (ParseException e) {
                    sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    d = sdf.parse(s);
                }
                this.uiPUT("password_expired", "false");
                if (new Date().getTime() > d.getTime()) {
                    this.add_log("User password expired, prompting to have them udpate password", "USER");
                    this.uiPUT("password_expired", "true");
                    if (!this.uiSG("user_protocol").equalsIgnoreCase("SFTP")) {
                        String fname = "expired.html";
                        String buildPrivs = "(read)(view)";
                        Properties permissions = new Properties();
                        permissions.put("/", buildPrivs);
                        Properties dir_item = new Properties();
                        dir_item.put("url", new File_S(String.valueOf(System.getProperty("crushftp.web", "")) + "WebInterface/" + fname).toURI().toURL().toExternalForm());
                        dir_item.put("type", "file");
                        Vector<Properties> v = new Vector<Properties>();
                        v.addElement(dir_item);
                        Properties virtual = UserTools.generateEmptyVirtual();
                        String path = "/" + fname;
                        if (path.endsWith("/")) {
                            path = path.substring(0, path.length() - 1);
                        }
                        Properties vItem = new Properties();
                        vItem.put("virtualPath", path);
                        vItem.put("name", fname);
                        vItem.put("type", "FILE");
                        vItem.put("vItems", v);
                        virtual.put(path, vItem);
                        vItem = new Properties();
                        vItem.put("name", "VFS");
                        vItem.put("type", "DIR");
                        vItem.put("virtualPath", "/");
                        virtual.put("/", vItem);
                        this.expired_uVFS = this.uVFS;
                        this.setVFS(VFS.getVFS(virtual));
                        final Properties tempUser = UserTools.ut.getUser(this.uiSG("listen_ip_port"), theUser, false);
                        if (this.uiSG("user_protocol").toUpperCase().startsWith("HTTP") || this.uiSG("user_protocol").toUpperCase().startsWith("HTTPS")) {
                            if (ServerStatus.BG("expire_password_email_token_only")) {
                                Properties resetTokens = ServerStatus.siPG("resetTokens");
                                if (resetTokens == null) {
                                    resetTokens = new Properties();
                                }
                                ServerStatus.thisObj.server_info.put("resetTokens", resetTokens);
                                final String token = Common.makeBoundary();
                                tempUser.put("generated", String.valueOf(System.currentTimeMillis()));
                                resetTokens.put(token, tempUser);
                                Worker.startWorker(new Runnable(){

                                    @Override
                                    public void run() {
                                        try {
                                            Common.sendResetPasswordTokenEmail(tempUser, "en", SessionCrush.this.uiSG("listen_ip"), token);
                                        }
                                        catch (Exception e) {
                                            Log.log("SERVER", 0, e);
                                        }
                                    }
                                }, "Send Reset password email to " + theUser);
                            }
                        }
                        if (this.uiSG("user_protocol").toUpperCase().startsWith("FTP")) {
                            tempUser.put("auto_set_pass", "true");
                        }
                        if (!com.crushftp.client.Common.dmz_mode) {
                            UserTools.writeUser(this.uiSG("listen_ip_port"), theUser, tempUser);
                        }
                    }
                }
            }
            catch (Exception e) {
                Log.log("LOGIN", 2, e);
            }
        }
        if (loginReason.getProperty("changedPassword", "").equals("true")) {
            loginReason.remove("changedPassword");
            ServerStatus.thisObj.runAlerts("password_change", this);
        }
        if (this.user == null) {
            boolean hack;
            if (loginReason.getProperty("reason", "").equals("")) {
                String inv_val = ServerStatus.siPG("invalid_usernames").getProperty(theUser.toUpperCase(), String.valueOf(new Date().getTime()) + ":0");
                if (invalid_username_time > 0L && invalid_username_time > new Date().getTime() - (long)(ServerStatus.IG("invalid_usernames_seconds") * 1000) && invalid_username_attempts <= ServerStatus.IG("invalid_usernames_seconds_attempts")) {
                    inv_val = String.valueOf(inv_val.split(":")[0]) + ":" + (Integer.parseInt(inv_val.split(":")[1]) + 1);
                } else if (invalid_username_time > 0L && invalid_username_time <= new Date().getTime() - (long)(ServerStatus.IG("invalid_usernames_seconds") * 1000)) {
                    inv_val = String.valueOf(new Date().getTime()) + ":0";
                }
                ServerStatus.siPG("invalid_usernames").put(theUser.toUpperCase(), inv_val);
            }
            if (!((hack = this.checkHackUsernames(theUser)) || theUser.equals("") || theUser.equals("anonymous") || com.crushftp.client.Common.dmz_mode)) {
                try {
                    Properties info = new Properties();
                    info.put("alert_type", "bad_login");
                    info.put("alert_sub_type", "username");
                    info.put("alert_timeout", "0");
                    info.put("alert_max", "0");
                    info.put("alert_msg", String.valueOf(theUser) + (loginReason.getProperty("reason", "").equals("valid user") ? " does exist" : " does not exist"));
                    if (loginReason.getProperty("no_log_invalid_password", "false").equals("false")) {
                        ServerStatus.thisObj.runAlerts("security_alert", info, this.user_info, this);
                    }
                }
                catch (Exception ee) {
                    Log.log("BAN", 1, ee);
                }
            }
            if (!hack) {
                UserTools.ut.check_login_count_max(UserTools.ut.getUser(this.uiSG("listen_ip_port"), theUser, true), this.uiSG("listen_ip_port"), theUser, this.uiSG("user_ip"), this.uiSG("user_port"));
            }
            return false;
        }
        try {
            if (this.ftp != null) {
                this.ftp.is = new BufferedReader(new InputStreamReader(this.ftp.sock.getInputStream(), this.SG("char_encoding")));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            if (this.ftp != null) {
                this.ftp.os = this.ftp.sock.getOutputStream();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (!this.SG("quota_mb").trim().equals("") && !this.SG("quota_mb").trim().equals("quota_mb")) {
            QuotaWorker.add_quota_to_all_vfs_entries(Long.parseLong(this.SG("quota_mb").trim()), this.uVFS);
        }
        Log.log("LOGIN", 3, LOC.G("Login complete."));
        if (this.SG("user_log_path") != null && !this.SG("user_log_path").equals("") && !this.SG("user_log_path").equals("user_log_path")) {
            this.uiPUT("user_log_path_custom", Common.dots(ServerStatus.change_vars_to_values_static(this.SG("user_log_path"), this.user, this.user_info, this)));
        }
        return true;
    }

    public boolean checkHackUsernames(String theUser) {
        return this.checkHackUsernames(theUser, true);
    }

    public boolean checkHackUsernames(String theUser, boolean logit) {
        if (SessionCrush.isHackUsername(theUser, this.SG("hack_usernames"))) {
            this.uiPUT("hack_username", "true");
            boolean banned = ServerStatus.thisObj.ban(this.user_info, ServerStatus.IG("hban_timeout"), "hack username:" + theUser);
            ServerStatus.thisObj.kick(this.user_info, logit);
            if (banned) {
                try {
                    Properties info = new Properties();
                    info.put("alert_type", "hack");
                    info.put("alert_sub_type", "username");
                    info.put("alert_timeout", String.valueOf(ServerStatus.IG("hban_timeout")));
                    info.put("alert_max", "0");
                    info.put("alert_msg", "hack username : " + theUser);
                    ServerStatus.thisObj.runAlerts("security_alert", info, this.user_info, this);
                    info.put("alert_msg", "hack username : " + theUser);
                    ServerStatus.thisObj.runAlerts("ip_banned_logins", info, this.user_info, null);
                }
                catch (Exception e) {
                    Log.log("BAN", 1, e);
                }
            }
            return true;
        }
        return false;
    }

    public static boolean isHackUsername(String theUser, String hack_usernames) {
        if (theUser == null || theUser.trim().equals("")) {
            return false;
        }
        String[] hack_users = hack_usernames.split(",");
        if (!hack_cache.containsKey(hack_usernames)) {
            hack_cache.clear();
        }
        if (new File_S("./hack_usernames_reset").exists()) {
            new File_S("./hack_usernames_reset").delete();
            hack_cache.clear();
        }
        hack_cache.put(hack_usernames, "");
        Vector<String> v = new Vector<String>();
        int x = 0;
        while (x < hack_users.length) {
            v.addElement(hack_users[x]);
            ++x;
        }
        Vector v2 = new Vector();
        int x2 = 0;
        while (x2 < v.size()) {
            if (v.elementAt(x2).toString().trim().indexOf(":/") >= 0) {
                GenericClient c;
                block22: {
                    String the_url = v.elementAt(x2).toString().trim();
                    String r1 = "{";
                    String r2 = "}";
                    String addon = "";
                    try {
                        if (the_url.indexOf("working_dir") >= 0) {
                            the_url = Common.replace_str(the_url, String.valueOf(r1) + "working_dir" + addon + r2, String.valueOf(new File_S("./").getCanonicalPath().replace('\\', '/')) + "/");
                        }
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                    VRL vrl = new VRL(the_url);
                    c = Common.getClient(Common.getBaseUrl(vrl.toString()), "hack_username:", new Vector());
                    try {
                        if (hack_cache.containsKey(vrl.toString())) {
                            v2.addAll((Vector)hack_cache.get(vrl.toString()));
                            break block22;
                        }
                        c.login(vrl.getUsername(), vrl.getPassword(), "");
                        Vector<String> v3 = new Vector<String>();
                        hack_cache.put(vrl.toString(), v3);
                        Properties stat = c.stat(vrl.getPath());
                        if (stat == null) break block22;
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(c.download(vrl.getPath(), 0L, -1L, true)));){
                            String data = "";
                            while ((data = br.readLine()) != null) {
                                if (data.trim().startsWith("#")) continue;
                                if (data.indexOf(",") >= 0) {
                                    data = data.substring(0, data.indexOf(","));
                                }
                                if (data.indexOf(";") >= 0) {
                                    data = data.substring(0, data.indexOf(";"));
                                }
                                data = Common.replace_str(data, "\"", "");
                                v3.addElement(data.trim());
                            }
                            v2.addAll(v3);
                        }
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 1, e);
                    }
                }
                try {
                    c.close();
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            ++x2;
        }
        v.addAll(v2);
        x2 = 0;
        while (x2 < v.size()) {
            if (Common.compare_with_hack_username(theUser, v.elementAt(x2).toString())) {
                return true;
            }
            ++x2;
        }
        return false;
    }

    public void doErrorEvent(Exception e) {
        Properties error_info = new Properties();
        error_info.put("the_command", this.uiSG("the_command"));
        error_info.put("the_command_data", this.uiSG("the_command_data"));
        error_info.put("url", String.valueOf(e.toString()));
        error_info.put("the_file_status", "FAILED");
        error_info.put("the_file_error", String.valueOf(e.toString()));
        error_info.put("the_file_name", String.valueOf(e.toString()));
        error_info.put("the_file_path", String.valueOf(e.toString()));
        error_info.put("the_file_start", String.valueOf(System.currentTimeMillis()));
        error_info.put("the_file_end", String.valueOf(System.currentTimeMillis()));
        error_info.put("the_file_speed", "0");
        error_info.put("the_file_size", "0");
        error_info.put("the_file_resume_loc", "0");
        error_info.put("the_file_md5", "");
        error_info.put("modified", "0");
        if (com.crushftp.client.Common.dmz_mode) {
            try {
                Vector queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                Properties action = new Properties();
                action.put("type", "PUT:ERROR_EVENT");
                action.put("id", Common.makeBoundary());
                action.put("error_info", error_info);
                action.put("error_user_info", AdminControls.stripUser(this.user_info));
                action.put("need_response", "false");
                Properties root_item = this.uVFS.get_item(this.SG("root_dir"));
                if (!root_item.getProperty("url").startsWith("virtual")) {
                    final GenericClient c = this.uVFS.getClient(root_item);
                    action.put("crushAuth", c.getConfig("crushAuth"));
                    if (ServerStatus.BG("send_dmz_error_events_to_internal")) {
                        queue.addElement(action);
                    }
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            try {
                                Thread.sleep(5000L);
                                SessionCrush.this.uVFS.releaseClient(c);
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                    });
                    Thread.sleep(5000L);
                }
            }
            catch (Exception ee) {
                Log.log("SERVER", 0, ee);
            }
        } else {
            this.do_event5("ERROR", error_info);
        }
    }

    public void setVFS(VFS newVFS) {
        if (this.uVFS != null) {
            this.uVFS.disconnect();
        }
        this.uVFS = newVFS;
    }

    public String getId() {
        if ((this.uiSG("user_protocol").startsWith("HTTP") || this.uiSG("user_protocol_actual").startsWith("HTTP")) && this.uiSG("CrushAuth").length() > 30) {
            return this.uiSG("CrushAuth");
        }
        if (ServerStatus.BG("relaxed_event_grouping")) {
            return String.valueOf(this.uiSG("user_protocol")) + this.uiSG("user_name") + this.uiSG("user_ip");
        }
        return String.valueOf(this.uiSG("user_protocol")) + this.uiSG("user_name") + this.uiSG("user_ip") + this.uiIG("user_port") + this.uiIG("user_number");
    }

    public boolean login_user_pass() throws Exception {
        return this.login_user_pass(false, true);
    }

    public boolean login_user_pass(boolean anyPass) throws Exception {
        return this.login_user_pass(anyPass, true);
    }

    public boolean login_user_pass(boolean anyPass, boolean doAfterLogin) throws Exception {
        return this.login_user_pass(anyPass, doAfterLogin, this.uiSG("user_name"), this.uiSG("current_password"));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    public boolean login_user_pass(boolean anyPass, boolean doAfterLogin, String user_name, String user_pass) throws Exception {
        block155: {
            block156: {
                block157: {
                    block158: {
                        block154: {
                            block153: {
                                block152: {
                                    if (user_name.length() > 2000) break block152;
                                    if (user_pass.length() <= ServerStatus.IG("max_password_length") || user_name.equalsIgnoreCase("crush_oauth2") || user_name.equalsIgnoreCase("crush_oauth2_ms") || user_name.equalsIgnoreCase("crush_oauth2_azure_b2c") || user_name.equalsIgnoreCase("crush_oauth2_cognito")) break block153;
                                }
                                this.not_done = this.ftp_write_command("550", "Invalid");
                                this.doErrorEvent(new Exception(this.uiSG("lastLog")));
                                Log.log("LOGIN", 1, "Password is too long. Username: " + user_name);
                                return false;
                            }
                            Log.log("LOGIN", 3, new Exception(String.valueOf(LOC.G("INFO:Logging in with user:")) + user_name));
                            this.uiPUT("last_logged_command", "USER");
                            stripped_char = false;
                            if (user_name.startsWith("!")) {
                                user_name = user_name.substring(1);
                                this.uiPUT("user_name", user_name);
                                stripped_char = true;
                            }
                            if (this.user_info.getProperty("user_name_original", "").equals("") || this.user_info.getProperty("user_name_original", "").equalsIgnoreCase("anonymous")) {
                                this.uiPUT("user_name_original", user_name);
                            }
                            if (this.server_item.getProperty("linkedServer", "").equals("@AutoDomain") && this.uiSG("user_name_original").indexOf("@") > 0) {
                                newLinkedServer = this.uiSG("user_name_original").split("@")[this.uiSG("user_name_original").split("@").length - 1];
                                newLinkedServer2 = com.crushftp.client.Common.dots(newLinkedServer);
                                if (newLinkedServer.equals(newLinkedServer2 = newLinkedServer2.replace('/', '-').replace('\\', '-').replace('%', '-').replace(':', '-').replace(';', '-'))) {
                                    v = ServerStatus.VG("server_groups");
                                    x = 0;
                                    while (x < v.size()) {
                                        ucg = "" + v.elementAt(x);
                                        if (newLinkedServer.equalsIgnoreCase(ucg)) {
                                            newLinkedServer = ucg;
                                        }
                                        ++x;
                                    }
                                    this.uiPUT("user_name", this.uiSG("user_name_original").substring(0, this.uiSG("user_name_original").lastIndexOf("@")));
                                    this.uiPUT("listen_ip_port", newLinkedServer);
                                    user_name = this.uiSG("user_name");
                                }
                            }
                            if (ServerStatus.BG("lowercase_usernames")) {
                                this.uiPUT("user_name", user_name.toLowerCase());
                            }
                            if (ServerStatus.BG("block_hack_username_immediately") && !user_name.equals("") && !user_name.equalsIgnoreCase("anonymous") && (hack = this.checkHackUsernames(user_name, false))) {
                                Log.log("SERVER", 0, "User " + user_name + " kicked immediately because they are in the hack usernames list. IP: " + this.uiSG("user_ip"));
                                this.uiPUT("hack_username", "true");
                                return false;
                            }
                            this.setVFS(null);
                            login_prop = null;
                            otp_valid = false;
                            verified = false;
                            verify_password = user_pass;
                            if (com.crushftp.client.Common.dmz_mode) break block154;
                            if (!user_pass.contains(":")) ** GOTO lbl-1000
                            if (ServerStatus.BG("otp_validated_logins")) {
                                v0 = user_pass.substring(0, user_pass.indexOf(":"));
                            } else lbl-1000:
                            // 2 sources

                            {
                                v0 = verify_password = user_pass;
                            }
                        }
                        if (!(verified = this.verify_user(user_name, verify_password, anyPass, doAfterLogin)) || this.user == null || !this.user.getProperty("otp_auth", "").equals("true")) break block155;
                        if (!ServerStatus.BG("otp_validated_logins")) break block155;
                        if (ServerStatus.siIG("enterprise_level") <= 0) {
                            throw new Exception("OTP only valid for Enterprise licenses.");
                        }
                        otp_protocol_check = this.user.getProperty("otp_auth_" + this.uiSG("user_protocol").toLowerCase(), "true").equals("true");
                        if (!otp_protocol_check) break block156;
                        if (!(this.user.getProperty("otp_auth_last_login", "").equals("") || this.user.getProperty("otp_auth_valid_for_days", "0").equals("") || this.user.getProperty("otp_auth_valid_for_days", "0").equals("0"))) {
                            try {
                                Log.log("LOGIN", 1, "CHALLENGE_OTP : Check otp auth last login.");
                                days = Integer.parseInt(this.user.getProperty("otp_auth_valid_for_days", "0"));
                                if (days > 0 && (otp_auth_last_login = (sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.US)).parse(this.user.getProperty("otp_auth_last_login")).getTime()) + (long)(86400000 * days) > System.currentTimeMillis()) {
                                    Log.log("LOGIN", 1, "CHALLENGE_OTP : otp auth still valid.");
                                    Log.log("LOGIN", 2, "CHALLENGE_OTP : otp auth valid till : " + new Date(otp_auth_last_login + (long)(86400000 * days)));
                                    otp_valid = true;
                                }
                            }
                            catch (Exception e) {
                                Log.log("LOGIN", 1, "CHALLENGE_OTP : Check last otp auth valid login: " + e);
                            }
                        }
                        if (otp_valid) break block155;
                        if (!ServerStatus.thisObj.server_info.containsKey("otp_tokens")) {
                            ServerStatus.thisObj.server_info.put("otp_tokens", new Properties());
                        }
                        if (!(otp_tokens = (Properties)ServerStatus.thisObj.server_info.get("otp_tokens")).containsKey(String.valueOf(user_name) + this.uiSG("user_ip")) || user_pass.indexOf(":") < 0) break block157;
                        token = (Properties)otp_tokens.get(String.valueOf(user_name) + this.uiSG("user_ip"));
                        token_timout = ServerStatus.LG("otp_token_timeout");
                        if (!this.user.getProperty("otp_token_timeout", "").equals("") && !this.user.getProperty("otp_token_timeout", "0").equals("0")) {
                            try {
                                token_timout = Long.parseLong(this.user.getProperty("otp_token_timeout", "0"));
                            }
                            catch (Exception e) {
                                Log.log("LOGIN", 1, "CHALLENGE_OTP : Wrong token timout: " + e);
                            }
                        }
                        if (token_timout < 60000L) {
                            token_timout = 60000L;
                        }
                        if (System.currentTimeMillis() - Long.parseLong(token.getProperty("time")) <= token_timout) break block158;
                        otp_tokens.remove(String.valueOf(user_name) + this.uiSG("user_ip"));
                        break block155;
                    }
                    Log.log("LOGIN", 1, "CHALLENGE_OTP : Checking OTP token.");
                    if (!token.getProperty("token", "").startsWith("TOTP:")) ** GOTO lbl123
                    otp_valid = com.crushftp.client.Common.totp_checkCode(token.getProperty("token").substring(5), Long.parseLong(user_pass.substring(user_pass.lastIndexOf(":") + 1)), System.currentTimeMillis());
                    if (otp_valid) {
                        user_pass = user_pass.substring(0, user_pass.lastIndexOf(":"));
                        Log.log("LOGIN", 1, "CHALLENGE_OTP : TOTP token is valid.");
                        if (!(com.crushftp.client.Common.dmz_mode || this.user.getProperty("otp_auth_valid_for_days", "0").equals("") || this.user.getProperty("otp_auth_valid_for_days", "0").equals("0"))) {
                            sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.US);
                            otp_auth_last_login = sdf.format(new Date());
                            UserTools.ut.put_in_user(this.uiSG("listen_ip_port"), user_name, "otp_auth_last_login", otp_auth_last_login, false, false);
                        }
                    } else {
                        Log.log("LOGIN", 1, "CHALLENGE_OTP : TOTP token is invalid.");
                        this.user_info.put("lastProxyError", "CHALLENGE_OTP:OTP invalid.");
                        this.uiPUT("user_logged_in", "false");
                        this.not_done = false;
                        UserTools.ut.check_login_count_max(this.user, this.uiSG("listen_ip_port"), user_name, this.uiSG("user_ip"), this.uiSG("user_port"));
                        return false;
lbl123:
                        // 1 sources

                        if (user_pass.indexOf(":") >= 0 && token.getProperty("token", "").equalsIgnoreCase(user_pass.substring(user_pass.lastIndexOf(":") + 1))) {
                            Log.log("LOGIN", 1, "CHALLENGE_OTP : OTP token is valid.");
                            user_pass = user_pass.substring(0, user_pass.lastIndexOf(":"));
                            if (!(com.crushftp.client.Common.dmz_mode || this.user.getProperty("otp_auth_valid_for_days", "0").equals("") || this.user.getProperty("otp_auth_valid_for_days", "0").equals("0"))) {
                                sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.US);
                                otp_auth_last_login = sdf.format(new Date());
                                UserTools.ut.put_in_user(this.uiSG("listen_ip_port"), user_name, "otp_auth_last_login", otp_auth_last_login, false, false);
                            }
                            otp_valid = true;
                        } else if (!com.crushftp.client.Common.dmz_mode) {
                            Log.log("LOGIN", 1, "CHALLENGE_OTP : OTP token is invalid.");
                            this.user_info.put("lastProxyError", "CHALLENGE_OTP:OTP invalid.");
                            this.uiPUT("user_logged_in", "false");
                            this.not_done = false;
                            UserTools.ut.check_login_count_max(this.user, this.uiSG("listen_ip_port"), user_name, this.uiSG("user_ip"), this.uiSG("user_port"));
                            return false;
                        }
                    }
                    break block155;
                }
                if (!com.crushftp.client.Common.dmz_mode && this.user_info.getProperty("skip_two_factor", "false").equals("false")) {
                    auth_token = "";
                    auth_token = ServerStatus.BG("otp_numeric") != false ? Common.makeBoundaryNumeric(ServerStatus.IG("temp_accounts_length")) : Common.makeBoundary(ServerStatus.IG("temp_accounts_length")).toUpperCase();
                    token = new Properties();
                    otp_tokens.put(String.valueOf(user_name) + this.uiSG("user_ip"), token);
                    token.put("time", String.valueOf(System.currentTimeMillis()));
                    token.put("token", auth_token);
                    if (!this.user.getProperty("twofactor_secret", "").equals("")) {
                        Log.log("LOGIN", 1, "CHALLENGE_OTP : Using google authenticator.");
                        token.put("token", "TOTP:" + ServerStatus.thisObj.common_code.decode_pass(this.user.getProperty("twofactor_secret")));
                    } else if (ServerStatus.SG("otp_url").trim().equalsIgnoreCase("SMTP") || this.user.getProperty("phone", "").equals("")) {
                        email_info = new Properties();
                        email_info.put("server", ServerStatus.SG("smtp_server"));
                        email_info.put("user", ServerStatus.SG("smtp_user"));
                        email_info.put("pass", ServerStatus.SG("smtp_pass"));
                        email_info.put("ssl", ServerStatus.SG("smtp_ssl"));
                        email_info.put("html", ServerStatus.SG("smtp_html"));
                        email_info.put("to", this.user.getProperty("email", ""));
                        from = ServerStatus.SG("smtp_from");
                        reply_to = "";
                        cc = "";
                        bcc = "";
                        subject = String.valueOf(System.getProperty("appname", "CrushFTP")) + " Two Factor Authentication";
                        body = "OTP password : " + auth_token;
                        template = Common.get_email_template("Two Factor Auth");
                        if (template != null) {
                            if (!template.getProperty("emailFrom").equals("")) {
                                from = ServerStatus.thisObj.change_vars_to_values(template.getProperty("emailFrom"), this);
                            }
                            reply_to = ServerStatus.thisObj.change_vars_to_values(template.getProperty("emailReplyTo"), this);
                            cc = ServerStatus.thisObj.change_vars_to_values(template.getProperty("emailCC"), this);
                            bcc = ServerStatus.thisObj.change_vars_to_values(template.getProperty("emailBCC"), this);
                            subject = ServerStatus.thisObj.change_vars_to_values(template.getProperty("emailSubject"), this);
                            body = ServerStatus.thisObj.change_vars_to_values(template.getProperty("emailBody"), this);
                            body = body.replaceAll("\\{auth_token\\}", auth_token);
                        }
                        email_info.put("from", from);
                        email_info.put("to", this.user.getProperty("email", ""));
                        email_info.put("reply_to", reply_to);
                        email_info.put("cc", cc);
                        email_info.put("bcc", bcc);
                        email_info.put("body", body);
                        email_info.put("subject", subject);
                        ServerStatus.thisObj.sendEmail(email_info);
                        Log.log("LOGIN", 1, "CHALLENGE_OTP : Sent otp email. User :" + user_name + " with user IP : " + this.uiSG("user_ip"));
                    } else {
                        Common.send_otp_for_auth_sms(this.user.getProperty("phone", ""), auth_token);
                        Log.log("LOGIN", 1, "CHALLENGE_OTP : Sent otp sms User :" + user_name + " with user IP : " + this.uiSG("user_ip"));
                    }
                }
                this.user_info.put("lastProxyError", "CHALLENGE_OTP:OTP invalid.");
                break block155;
            }
            otp_valid = true;
        }
        if (com.crushftp.client.Common.dmz_mode && this.user != null && !otp_valid) {
            otp_valid = this.user.getProperty("otp_valid", "false").equals("true");
            Log.log("LOGIN", 1, "DMZ CHALLENGE_OTP : User :" + user_name + " otp_valid=" + this.user.getProperty("otp_valid", "false") + " otp_auth=" + this.user.getProperty("otp_auth", "") + " verified=" + verified);
        }
        if (!this.checkGlobalLoginAttemptFrequencyTooHigh(this)) {
            this.not_done = this.ftp_write_command("530", "Login failed due to frequency of attempted logins.");
            this.uiPUT("user_logged_in", "false");
        } else if (verified && (this.user == null || !this.user.getProperty("otp_auth", "").equals("true")) || verified && otp_valid) {
            if (!this.uiSG("user_name").equals("")) {
                if (user_name.toUpperCase().startsWith("SSO_SAML") && user_pass.equals("none")) {
                    user_pass = this.uiSG("current_password");
                }
                user_name = this.uiSG("user_name");
                SharedSession.find("crushftp.usernames").put(String.valueOf(Common.getPartialIp(this.uiSG("user_ip"))) + "_" + this.getId() + "_user", user_name);
            }
            this.uiPUT("user_name", user_name);
            this.uiPUT("current_password", user_pass);
            this.uVFS.setUserPassIpPortProtocol(user_name, user_pass, this.uiSG("user_ip"), this.uiIG("user_port"), this.uiSG("user_protocol"), this.user_info, this.user, this);
            Log.log("LOGIN", 2, LOC.G("User $0 authenticated, VFS set to:$1", user_name, this.uVFS.toString()));
            if (ServerStatus.BG("create_home_folder") || this.BG("create_home_folder")) {
                try {
                    v = new Vector();
                    this.uVFS.getListing(v, "/");
                    xx = 0;
                    while (xx < v.size()) {
                        p = (Properties)v.elementAt(xx);
                        v.setElementAt(this.uVFS.get_item(String.valueOf(p.getProperty("root_dir")) + p.getProperty("name") + "/"), xx);
                        ++xx;
                    }
                    v = this.uVFS.homes;
                    x = 0;
                    while (x < v.size()) {
                        virtual = (Properties)v.elementAt(x);
                        keys = virtual.keys();
                        while (keys.hasMoreElements()) {
                            value = virtual.get(keys.nextElement());
                            if (!(value instanceof Properties) || (vItems = (Vector)(val = (Properties)value).get("vItems")) == null) continue;
                            p = (Properties)vItems.elementAt(0);
                            p.put("url", this.uVFS.updateUrlVariables(p.getProperty("url", "")));
                            if (!p.getProperty("url").endsWith("/") || !p.getProperty("url").toUpperCase().startsWith("FILE:/")) continue;
                            url = p.getProperty("url");
                            url = Common.replace_str(url, "{username}", user_name);
                            url = Common.replace_str(url, "{user_name}", user_name);
                            Common.verifyOSXVolumeMounted(url);
                            if (new File_S(new VRL(url).getPath()).exists()) continue;
                            new File_S(new VRL(url).getPath()).mkdirs();
                        }
                        ++x;
                    }
                }
                catch (Exception e) {
                    Log.log("LOGIN", 1, e);
                }
            }
            this.setupRootDir(null, false);
            if (ServerStatus.BG("jailproxy") && this.getProperty("default_current_dir_unlocked", "false").equals("false")) {
                this.uiPUT("current_dir", this.SG("root_dir"));
            }
            if (this.user.get("ip_list") != null) {
                block150: {
                    ips = String.valueOf(this.user.getProperty("ip_list").trim()) + "\r\n";
                    ips = Common.replace_str(ips, "\r", "~");
                    get_em = new StringTokenizer(ips, "~");
                    num_to_do = get_em.countTokens();
                    ip_list = new Vector<Properties>();
                    try {
                        x = 0;
                        while (x < num_to_do) {
                            ip_str = get_em.nextToken().trim();
                            ip_data = new Properties();
                            ip_data.put("type", String.valueOf(ip_str.charAt(0)));
                            ip_data.put("start_ip", ip_str.substring(1, ip_str.indexOf(",")));
                            ip_data.put("stop_ip", ip_str.substring(ip_str.indexOf(",") + 1));
                            ip_list.addElement(ip_data);
                            ++x;
                        }
                    }
                    catch (Exception e) {
                        if (("" + e).indexOf("Interrupted") < 0) break block150;
                        throw e;
                    }
                }
                this.user.put("ip_restrictions", ip_list);
                this.user.remove("ip_list");
            }
            auto_kicked = false;
            allowedHours = new Vector<String>();
            if (this.SG("hours_of_day").equals("") || this.SG("hours_of_day").equals("hours_of_day")) {
                this.user.put("hours_of_day", "0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23");
            }
            if (this.user.get("allowed_protocols") == null || this.SG("allowed_protocols").equals("allowed_protocols")) {
                this.user.put("allowed_protocols", ",ftp:0,ftps:0,sftp:0,http:0,https:0,webdav:0,");
            }
            hours = this.SG("hours_of_day").split(",");
            x = 0;
            while (x < hours.length) {
                try {
                    allowedHours.addElement(String.valueOf(Integer.parseInt(hours[x])));
                }
                catch (Exception e) {
                    Log.log("LOGIN", 1, e);
                }
                ++x;
            }
            if (this.IG("max_logins_ip") != 0 && this.BG("logins_ip_auto_kick") && ServerStatus.count_users_ip(this, null) > this.IG("max_logins_ip")) {
                auto_kicked = ServerStatus.thisObj.kill_first_same_name_same_ip(this.user_info);
                Thread.sleep(5000L);
                this.verify_user(user_name, user_pass, false, doAfterLogin);
            }
            if (stripped_char) {
                stripped_char = ServerStatus.thisObj.kill_same_name_same_ip(this.user_info, true);
            }
            if (this.IG("max_logins") < 0) {
                this.not_done = this.user.getProperty("failure_count_max", "0").equals("0") == false && this.user.getProperty("failure_count_max", "0").equals("") == false && this.IG("failure_count") > 0 ? this.ftp_write_command("530", LOC.G("%PASS-bad%")) : this.ftp_write_command("421", String.valueOf(LOC.G("%account_disabled%")) + "\r\n" + LOC.G("Control connection closed") + ".");
                this.uiPUT("user_logged_in", "false");
            } else if (ServerStatus.siIG("concurrent_users") >= ServerStatus.IG("max_users") + 1 && !this.BG("ignore_max_logins")) {
                this.not_done = this.ftp_write_command("421", String.valueOf(LOC.G("%max_users_server%")) + "\r\n" + LOC.G("Control connection closed") + ".");
                this.uiPUT("user_logged_in", "false");
            } else if (ServerStatus.siIG("concurrent_users") >= ServerStatus.IG("max_max_users") + 1) {
                this.not_done = this.ftp_write_command("421", String.valueOf(LOC.G("%max_max_users_server%")) + "\r\n" + LOC.G("Control connection closed") + ".");
                this.uiPUT("user_logged_in", "false");
            } else if (Integer.parseInt(this.server_item.getProperty("connected_users")) > Integer.parseInt(this.server_item.getProperty("max_connected_users", "32768"))) {
                this.not_done = this.ftp_write_command("421", String.valueOf(LOC.G("%max_users_server%")) + "\r\n" + LOC.G("Control connection closed") + ".");
                this.uiPUT("user_logged_in", "false");
            } else {
                ServerStatus.thisObj.common_code;
                if (!Common.check_ip((Vector)this.user.get("ip_restrictions"), this.uiSG("user_ip")).equals("")) {
                    ServerStatus.thisObj.common_code;
                    this.not_done = this.ftp_write_command("550", String.valueOf(LOC.G("%bad_ip%")) + ":" + Common.check_ip((Vector)this.user.get("ip_restrictions"), this.uiSG("user_ip")));
                    this.uiPUT("user_logged_in", "false");
                } else if (!Common.check_day_of_week(ServerStatus.SG("day_of_week_allow"), new Date())) {
                    this.not_done = this.ftp_write_command("530", String.valueOf(LOC.G("%day_restricted%")) + "\r\n" + LOC.G("Control connection closed") + ".");
                    this.uiPUT("user_logged_in", "false");
                } else if (Common.check_protocol(this.uiSG("user_protocol"), this.SG("allowed_protocols")) < 0) {
                    this.not_done = this.ftp_write_command("530", String.valueOf(LOC.G("This user is not allowed to use this protocol.")) + "\r\n" + LOC.G("Control connection closed") + ".");
                    this.uiPUT("user_logged_in", "false");
                } else if (ServerStatus.count_users_ip(this, this.uiSG("user_protocol")) > Common.check_protocol(this.uiSG("user_protocol"), this.SG("allowed_protocols"))) {
                    this.not_done = this.ftp_write_command("421", String.valueOf(LOC.G("%max_simultaneous_connections_ip%")) + " " + LOC.G("(For this protocol.)") + "\r\n" + LOC.G("Control connection closed") + ". (" + ServerStatus.count_users_ip(this, this.uiSG("user_protocol")) + "/" + Common.check_protocol(this.uiSG("user_protocol"), this.SG("allowed_protocols")) + ")");
                    this.uiPUT("user_logged_in", "false");
                } else if (!Common.check_day_of_week(this.SG("day_of_week_allow"), new Date())) {
                    this.not_done = this.ftp_write_command("530", String.valueOf(LOC.G("%user_day_restricted%")) + "\r\n" + LOC.G("Control connection closed") + ".");
                    this.uiPUT("user_logged_in", "false");
                } else if (allowedHours.indexOf(String.valueOf(Integer.parseInt(this.hh.format(new Date())))) < 0) {
                    this.not_done = this.ftp_write_command("530", String.valueOf(LOC.G("Not allowed to login at the present hour ($0), try later.", String.valueOf(Integer.parseInt(this.hh.format(new Date()))))) + "\r\n" + LOC.G("Control connection closed") + ".");
                    this.uiPUT("user_logged_in", "false");
                } else if (this.IG("max_logins_ip") != 0 && ServerStatus.count_users_ip(this, null) > this.IG("max_logins_ip") && !auto_kicked && !stripped_char) {
                    this.not_done = this.ftp_write_command("421", String.valueOf(LOC.G("%max_simultaneous_connections_ip%")) + "\r\n" + LOC.G("Control connection closed") + ".");
                    this.uiPUT("user_logged_in", "false");
                } else if (this.IG("max_logins") != 0 && ServerStatus.thisObj.count_users(this) > this.IG("max_logins") && !stripped_char) {
                    this.not_done = this.ftp_write_command("421", String.valueOf(LOC.G("%max_simultaneous_connections%")) + "\r\n" + LOC.G("Control connection closed") + ".");
                    this.uiPUT("user_logged_in", "false");
                } else if (ServerStatus.thisObj.common_code.check_date_expired_roll(this.SG("account_expire"))) {
                    if (this.BG("account_expire_delete")) {
                        try {
                            UserTools.deleteUser(this.uiSG("listen_ip_port"), user_name);
                        }
                        catch (NullPointerException x) {
                            // empty catch block
                        }
                        this.not_done = this.ftp_write_command("530", LOC.G("%account_expired_deleted%"));
                        this.uiVG("failed_commands").addElement("" + new Date().getTime());
                    } else {
                        this.not_done = this.ftp_write_command("530", LOC.G("%account_expired%"));
                        this.uiVG("failed_commands").addElement("" + new Date().getTime());
                    }
                    this.uiPUT("user_logged_in", "false");
                } else if (System.getProperty("crushftp.singleuser", "false").equals("true") && (this.SG("site").toUpperCase().indexOf("(CONNECT)") < 0 || this.BG("ignore_max_logins"))) {
                    this.not_done = this.ftp_write_command("530", String.valueOf(LOC.G("Not allowed to login during maintenance, try again later.")) + "\r\n" + LOC.G("Control connection closed") + ".");
                    this.uiPUT("user_logged_in", "false");
                } else if (!this.checkGlobalLoginFrequencyTooHigh(this)) {
                    this.not_done = this.ftp_write_command("530", "Login failed due to frequency of logins.");
                    this.uiPUT("user_logged_in", "false");
                } else {
                    block151: {
                        this.uiPUT("user_name", user_name);
                        this.uiPUT("current_password", user_pass);
                        if (!(this.SG("account_expire") == null || this.SG("account_expire").equals("") || this.SG("account_expire").equals("0") || this.SG("account_expire_rolling_days").equals("") || this.IG("account_expire_rolling_days") <= 0)) {
                            gc = new GregorianCalendar();
                            gc.setTime(new Date());
                            gc.add(5, this.IG("account_expire_rolling_days"));
                            sdf = null;
                            sdf = this.SG("account_expire").indexOf("/") >= 0 ? new SimpleDateFormat("MM/dd/yy hh:mm aa", Locale.US) : new SimpleDateFormat("MMddyyyyHHmm", Locale.US);
                            try {
                                if (sdf.parse(this.SG("account_expire")).getTime() < gc.getTime().getTime()) {
                                    this.user.put("account_expire", sdf.format(gc.getTime()));
                                    if (!com.crushftp.client.Common.dmz_mode) {
                                        UserTools.ut.put_in_user(this.uiSG("listen_ip_port"), user_name, "account_expire", sdf.format(gc.getTime()), true, true);
                                    }
                                    if (!com.crushftp.client.Common.dmz_mode) {
                                        UserTools.ut.put_in_user(this.uiSG("listen_ip_port"), user_name, "account_expire_rolling_days", String.valueOf(this.IG("account_expire_rolling_days")), true, true);
                                    }
                                }
                            }
                            catch (Exception e) {
                                if (!this.user.getProperty("failure_count_max", "0").equals("0") && !this.user.getProperty("failure_count_max", "0").equals("") && this.IG("failure_count") > 0) {
                                    UserTools.ut.put_in_user(this.uiSG("listen_ip_port"), user_name, "failure_count", "0", true, true);
                                }
                                return true;
                            }
                        }
                        if ((last_logins = this.SG("last_logins")).equals("last_logins")) {
                            last_logins = "";
                        }
                        sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.US);
                        last_logins = String.valueOf(sdf.format(new Date())) + "," + last_logins;
                        last_logins2 = "";
                        x = 0;
                        while (x < last_logins.split(",").length && x < 10) {
                            if (x > 0) {
                                last_logins2 = String.valueOf(last_logins2) + ",";
                            }
                            last_logins2 = String.valueOf(last_logins2) + last_logins.split(",")[x];
                            ++x;
                        }
                        if (!com.crushftp.client.Common.dmz_mode && ServerStatus.BG("track_last_logins")) {
                            UserTools.ut.put_in_user(this.uiSG("listen_ip_port"), user_name, "last_logins", last_logins2, false, false, true);
                        }
                        p = login_frequency = ServerStatus.siPG("login_frequency");
                        synchronized (p) {
                            login_prop = (Properties)login_frequency.get(user_name.toLowerCase());
                            if (login_prop == null) {
                                login_prop = new Properties();
                                login_prop.put("v", new Vector<E>());
                                login_frequency.put(user_name.toLowerCase(), login_prop);
                            }
                        }
                        v = (Vector)login_prop.get("v");
                        if (!this.uiSG("user_name").equalsIgnoreCase("anonymous") && !this.uiSG("user_name").equals("")) {
                            v.addElement(String.valueOf(System.currentTimeMillis()));
                        }
                        while (v.size() > 1000) {
                            v.remove(0);
                        }
                        info = this.checkAlertLoginFrequencyTooHigh(this, login_prop);
                        if (info != null) {
                            ServerStatus.thisObj.runAlerts("login_frequency", info, null, this, null, com.crushftp.client.Common.dmz_mode);
                        }
                        try {
                            if (this.ftp != null) {
                                priorTimeout = this.ftp.sock.getSoTimeout() / 1000;
                                timeout = this.IG("max_idle_time");
                                timeout = timeout < 0 ? (timeout *= -1) : (timeout *= 60);
                                this.ftp.sock.setSoTimeout((this.IG("max_idle_time") == 0 ? priorTimeout : timeout) * 1000);
                            }
                        }
                        catch (SocketException priorTimeout) {
                            // empty catch block
                        }
                        login_message = "";
                        if (auto_kicked) {
                            login_message = String.valueOf(login_message) + LOC.G("First user with same name, same IP, was autokicked.") + "\r\n";
                        }
                        if (stripped_char) {
                            login_message = String.valueOf(login_message) + LOC.G("Previous sessions were kicked.") + "\r\n";
                        }
                        ServerStatus.thisObj.set_user_pointer(this.user_info);
                        try {
                            msg2 = this.server_item.getProperty("user_welcome_message", "");
                            if (!this.user.getProperty("welcome_message2", "").equals("")) {
                                msg2 = this.user.getProperty("welcome_message2", "");
                            }
                            login_message = String.valueOf(login_message) + msg2 + "\r\n";
                            welcome_msg = ServerStatus.thisObj.change_vars_to_values(this.SG("welcome_message"), this).trim();
                            if (welcome_msg.equals("welcome_msg")) {
                                welcome_msg = "";
                            }
                            if (welcome_msg.length() > 0) {
                                welcome_msg = String.valueOf(welcome_msg) + "\r\n";
                            }
                            this.user.put("user_name", user_name);
                            login_message = String.valueOf(login_message.trim()) + "\r\n" + welcome_msg + "%PASS%";
                            this.uiPUT("last_login_message", login_message);
                            if (!this.uiBG("dont_write")) {
                                this.not_done = this.ftp_write_command("230", login_message);
                            }
                        }
                        catch (Exception e) {
                            if (("" + e).indexOf("Interrupted") < 0) break block151;
                            throw e;
                        }
                    }
                    date_time = SessionCrush.updateDateCustomizations(this.logDateFormat, this.user);
                    this.uiPUT("login_date_formatted", date_time.format(new Date()));
                    this.uiPUT("user_logged_in", "true");
                    this.uiPUT("sharedId", this.getId());
                    if (this.BG("ratio_field_permanent")) {
                        this.uiPUT("ratio_bytes_sent", String.valueOf(this.IG("user_bytes_sent")));
                        this.uiPUT("ratio_bytes_received", String.valueOf(this.IG("user_bytes_received")));
                    }
                    if (this.IG("max_login_time") != 0) {
                        max_minutes = this.IG("max_login_time");
                        Worker.startWorker(new Killer(max_minutes), String.valueOf(Thread.currentThread().getName()) + " (max_time)");
                    }
                }
            }
            if (this.uiBG("user_logged_in") && doAfterLogin) {
                ServerStatus.siPUT2("successful_logins", "" + (ServerStatus.IG("successful_logins") + 1));
            } else if (doAfterLogin) {
                if (!this.uiSG("user_protocol").toUpperCase().startsWith("HTTP")) {
                    ServerStatus.siPUT2("failed_logins", "" + (ServerStatus.IG("failed_logins") + 1));
                }
                if (this.uiVG("failed_commands").size() - 10 > 0) {
                    Thread.sleep(1000 * (this.uiVG("failed_commands").size() - 10));
                }
            }
        } else {
            if (!this.user_info.getProperty("lastProxyError", "").equals("")) {
                this.not_done = ServerStatus.BG("rfc_proxy") ? this.ftp_write_command_raw(this.user_info.getProperty("lastProxyError", "")) : this.ftp_write_command("530", this.user_info.getProperty("lastProxyError", ""));
            } else if (this.server_item.getProperty("serverType", "ftp").toUpperCase().startsWith("FTP") || !user_name.equals("") && !user_name.equalsIgnoreCase("anonymous")) {
                this.not_done = this.ftp_write_command("530", "%PASS-bad%");
            }
            this.uiVG("failed_commands").addElement("" + new Date().getTime());
            this.uiPUT("user_logged_in", "false");
            if (!this.uiSG("user_protocol").toUpperCase().startsWith("HTTP")) {
                ServerStatus.siPUT2("failed_logins", "" + (ServerStatus.IG("failed_logins") + 1));
            }
            this.uiPUT("user_logged_in", "false");
            if (this.uiVG("failed_commands").size() + this.uiVG("password_attempts").size() - 10 > 0) {
                Thread.sleep(1000 * (this.uiVG("failed_commands").size() + this.uiVG("password_attempts").size() - 10));
            }
        }
        this.uiPUT("stat", new Properties());
        if (!this.uiBG("skip_proxy_check")) {
            this.uiPUT("stat", ServerStatus.thisObj.statTools.add_login_stat(this.server_item, user_name, this.uiSG("user_ip"), this.uiBG("user_logged_in"), this));
        }
        this.uiPUT("user_name", user_name);
        this.uiPUT("current_password", user_pass);
        if (this.uiBG("user_logged_in")) {
            this.active();
            if (!this.user.getProperty("failure_count_max", "0").equals("0") && !this.user.getProperty("failure_count_max", "0").equals("") && this.IG("failure_count") > 0) {
                UserTools.ut.put_in_user(this.uiSG("listen_ip_port"), user_name, "failure_count", "0", true, true);
            }
            var10_15 = SessionCrush.session_counts;
            synchronized (var10_15) {
                if (com.crushftp.client.Common.dmz_mode) {
                    SessionCrush.session_counts.put(this.getId(), String.valueOf(Integer.parseInt(SessionCrush.session_counts.getProperty(this.getId(), "0")) + 1));
                }
            }
            return true;
        }
        if (doAfterLogin) {
            this.uiVG("user_log").add("Password attempt. Username :" + user_name);
            if (!this.uiSG("user_protocol").toUpperCase().startsWith("HTTP")) {
                ServerStatus.siPUT2("failed_logins", "" + (ServerStatus.IG("failed_logins") + 1));
                this.uiVG("password_attempts").addElement(String.valueOf(new Date().getTime()));
            }
            if (this.uiVG("failed_commands").size() + this.uiVG("password_attempts").size() - 10 > 0) {
                Thread.sleep(1000 * (this.uiVG("failed_commands").size() + this.uiVG("password_attempts").size() - 10));
            }
        }
        this.doErrorEvent(new Exception(this.uiSG("lastLog")));
        return false;
    }

    public Properties checkAlertLoginFrequencyTooHigh(SessionCrush the_user, Properties login_prop) {
        if (login_prop == null || login_prop.containsKey("expire")) {
            return null;
        }
        Vector alerts = ServerStatus.VG("alerts");
        int x = 0;
        while (x < alerts.size()) {
            Properties the_alert = (Properties)alerts.elementAt(x);
            if (the_alert.getProperty("type").equalsIgnoreCase("Login Frequency")) {
                int count = 0;
                if (the_alert != null) {
                    long past_timestamp = System.currentTimeMillis() - Long.parseLong(the_alert.getProperty("login_interval")) * 1000L;
                    long past_timestamp5 = System.currentTimeMillis() - Long.parseLong(the_alert.getProperty("login_interval")) * 5000L;
                    Vector v = (Vector)login_prop.get("v");
                    int xx = v.size() - 1;
                    while (xx >= 0) {
                        long login_time = Long.parseLong(v.elementAt(xx).toString());
                        if (login_time > past_timestamp) {
                            ++count;
                        }
                        if (login_time < past_timestamp5) {
                            v.removeElementAt(xx);
                        }
                        --xx;
                    }
                }
                if (count > Integer.parseInt(the_alert.getProperty("login_count", "100"))) {
                    Properties info = new Properties();
                    info.put("count", String.valueOf(count));
                    return info;
                }
            }
            ++x;
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean checkGlobalLoginFrequencyTooHigh(SessionCrush thisSession) {
        Properties login_frequency;
        if (thisSession.uiSG("user_name").equalsIgnoreCase("anonymous") || thisSession.uiSG("user_name").equals("")) {
            return true;
        }
        String[] successful_login_hammering_neverban = ServerStatus.SG("successful_login_hammering_neverban").split(",");
        int x = 0;
        while (x < successful_login_hammering_neverban.length) {
            if (!successful_login_hammering_neverban[x].trim().equals("") && com.crushftp.client.Common.do_search(successful_login_hammering_neverban[x].trim(), thisSession.uiSG("user_name"), false, 0)) {
                return true;
            }
            ++x;
        }
        Properties login_prop = null;
        Properties properties = login_frequency = ServerStatus.siPG("login_frequency");
        synchronized (properties) {
            login_prop = (Properties)login_frequency.get(thisSession.uiSG("user_name").toLowerCase());
            if (login_prop == null) {
                login_prop = new Properties();
                login_prop.put("v", new Vector());
                login_frequency.put(thisSession.uiSG("user_name").toLowerCase(), login_prop);
            }
        }
        ServerStatus.siPG("login_attempt_frequency").remove(thisSession.uiSG("user_name").toLowerCase());
        if (login_prop == null) {
            return false;
        }
        if (!login_prop.getProperty("expire", "0").equals("0")) {
            long expire = Long.parseLong(login_prop.getProperty("expire"));
            if (System.currentTimeMillis() < expire) {
                Log.log("SERVER", 0, "LOGIN DENIED STILL!  Login blocked for user \"" + thisSession.uiSG("user_name") + "\" due to too frequent of logins until " + new Date(expire));
                return false;
            }
            login_prop.remove("expire");
            login_prop.put("v", new Vector());
            Log.log("SERVER", 0, "LOGIN DENIAL CLEARED!  Login denial cleared for user \"" + thisSession.uiSG("user_name") + "\"");
            return true;
        }
        int count = 0;
        int login_hammer_attempts = ServerStatus.IG("login_hammer_attempts");
        long login_hammer_interval = ServerStatus.LG("login_hammer_interval");
        long login_hammer_timeout = ServerStatus.LG("login_hammer_timeout");
        if (thisSession.user != null && thisSession.user.containsKey("login_hammer_attempts")) {
            login_hammer_attempts = Integer.parseInt(thisSession.user.getProperty("login_hammer_attempts", String.valueOf(login_hammer_attempts)));
        }
        if (thisSession.user != null && thisSession.user.containsKey("login_hammer_interval")) {
            login_hammer_interval = Long.parseLong(thisSession.user.getProperty("login_hammer_interval", String.valueOf(login_hammer_interval)));
        }
        if (thisSession.user != null && thisSession.user.containsKey("login_hammer_timeout")) {
            login_hammer_timeout = Long.parseLong(thisSession.user.getProperty("login_hammer_timeout", String.valueOf(login_hammer_timeout)));
        }
        long past_timestamp = System.currentTimeMillis() - login_hammer_interval * 1000L;
        long past_timestamp5 = System.currentTimeMillis() - login_hammer_interval * 5000L;
        int history_size = 0;
        Properties properties2 = login_prop;
        synchronized (properties2) {
            Vector v = (Vector)login_prop.get("v");
            history_size = v.size();
            int xx = v.size() - 1;
            while (xx >= 0) {
                long login_time = Long.parseLong(v.elementAt(xx).toString());
                if (login_time > past_timestamp) {
                    ++count;
                }
                if (login_time < past_timestamp5) {
                    v.removeElementAt(xx);
                }
                --xx;
            }
        }
        Log.log("SERVER", 2, String.valueOf(thisSession.uiSG("user_name")) + " has a recent success login history of:" + count + " of history size:" + history_size);
        if (count > login_hammer_attempts) {
            long expire = System.currentTimeMillis() + login_hammer_timeout * 1000L * 60L;
            login_prop.put("expire", String.valueOf(expire));
            String msg = "LOGIN DENIED!  Login blocked for user \"" + thisSession.uiSG("user_name") + "\" due to too frequent of logins (" + count + "/" + login_hammer_attempts + ") in " + login_hammer_interval + " seconds interval until " + new Date(expire) + ":Hammering Successful Logins";
            Log.log("SERVER", 0, msg);
            Properties info = new Properties();
            info.put("user_name", thisSession.uiSG("user_name"));
            info.put("count", String.valueOf(count));
            info.put("alert_msg", msg);
            info.put("expire", "" + new Date(expire));
            info.put("attempts", String.valueOf(login_hammer_attempts));
            info.put("timeout", String.valueOf(login_hammer_timeout));
            info.put("interval", String.valueOf(login_hammer_interval));
            ServerStatus.thisObj.runAlerts("login_frequency_banned", info, null, this, null, com.crushftp.client.Common.dmz_mode);
            if (SharedSessionReplicated.send_queues.size() > 0) {
                Properties sync_info = new Properties();
                sync_info.put("user_name", thisSession.uiSG("user_name").toLowerCase());
                sync_info.put("login_prop", login_prop);
                SharedSessionReplicated.send(Common.makeBoundary(), "SYNC_LOGIN_FREQUENCY", "login_frequency", sync_info);
            }
            return false;
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean checkGlobalLoginAttemptFrequencyTooHigh(SessionCrush thisSession) {
        Properties login_attempt_frequency;
        if (thisSession.uiSG("user_name").equalsIgnoreCase("anonymous") || thisSession.uiSG("user_name").equals("")) {
            return true;
        }
        String[] successful_login_hammering_neverban = ServerStatus.SG("successful_login_hammering_neverban").split(",");
        int x = 0;
        while (x < successful_login_hammering_neverban.length) {
            if (!successful_login_hammering_neverban[x].trim().equals("") && com.crushftp.client.Common.do_search(successful_login_hammering_neverban[x].trim(), thisSession.uiSG("user_name"), false, 0)) {
                return true;
            }
            ++x;
        }
        Properties login_prop = null;
        Properties properties = login_attempt_frequency = ServerStatus.siPG("login_attempt_frequency");
        synchronized (properties) {
            login_prop = (Properties)login_attempt_frequency.get(thisSession.uiSG("user_name").toLowerCase());
            if (login_prop == null) {
                login_prop = new Properties();
                login_prop.put("v", new Vector());
                login_prop.put("banned_count", "0");
                login_attempt_frequency.put(thisSession.uiSG("user_name").toLowerCase(), login_prop);
            }
        }
        if (login_prop == null) {
            return false;
        }
        if (!login_prop.getProperty("expire", "0").equals("0")) {
            long expire = Long.parseLong(login_prop.getProperty("expire"));
            if (System.currentTimeMillis() < expire) {
                Log.log("SERVER", 0, "LOGIN ATTEMPT DENIED STILL!  Login blocked for user \"" + thisSession.uiSG("user_name") + "\" due to too frequent of login attempts until " + new Date(expire));
                return false;
            }
            login_prop.remove("expire");
            login_prop.put("v", new Vector());
            Log.log("SERVER", 0, "LOGIN ATTEMPT DENIAL CLEARED!  Login attempts denial cleared for user \"" + thisSession.uiSG("user_name") + "\"");
            return true;
        }
        int count = 0;
        int phammer_attempts = (int)ServerStatus.get_partial_val_or_all("phammer_attempts", 0);
        long phammer_banning = ServerStatus.get_partial_val_or_all("phammer_banning", 0);
        long pban_timeout = ServerStatus.get_partial_val_or_all("pban_timeout", 0);
        long past_timestamp = System.currentTimeMillis() - phammer_banning * 1000L;
        long past_timestamp5 = System.currentTimeMillis() - phammer_banning * 5000L;
        int history_size = 0;
        Properties properties2 = login_prop;
        synchronized (properties2) {
            Vector v = (Vector)login_prop.get("v");
            history_size = v.size();
            int xx = v.size() - 1;
            while (xx >= 0) {
                long login_time = Long.parseLong(v.elementAt(xx).toString());
                if (login_time > past_timestamp) {
                    ++count;
                }
                if (login_time < past_timestamp5) {
                    v.removeElementAt(xx);
                }
                --xx;
            }
            v.addElement(String.valueOf(System.currentTimeMillis()));
            while (v.size() > 1000) {
                v.remove(0);
            }
        }
        String msg = String.valueOf(thisSession.uiSG("user_name")) + " has a recent attempt login history of:" + count + " of history size:" + history_size;
        String msg2 = "login failures but is not banned yet";
        Log.log("SERVER", 2, msg);
        long banned_count = 1L;
        Properties info = new Properties();
        if (count > phammer_attempts) {
            banned_count = Long.parseLong(login_prop.getProperty("banned_count", "0")) + 1L;
            long expire = System.currentTimeMillis() + pban_timeout * banned_count * 1000L * 60L;
            login_prop.put("expire", String.valueOf(expire));
            login_prop.put("banned_count", String.valueOf(banned_count));
            msg2 = "has login failures and is banned until " + new Date(expire);
            msg = "LOGIN DENIED!  Login blocked for user \"" + thisSession.uiSG("user_name") + "\" due to too frequent of login attempts (" + count + "/" + phammer_attempts + ") in " + phammer_banning + " seconds interval until " + new Date(expire) + ":Hammering Failed Logins";
            Log.log("SERVER", 0, msg);
            info.put("expire", "" + new Date(expire));
            if (SharedSessionReplicated.send_queues.size() > 0) {
                Properties sync_info = new Properties();
                sync_info.put("user_name", thisSession.uiSG("user_name").toLowerCase());
                sync_info.put("login_prop", login_prop);
                SharedSessionReplicated.send(Common.makeBoundary(), "SYNC_LOGIN_ATTEMPT_FREQUENCY", "login_attempt_frequency", sync_info);
            }
            info.put("count", String.valueOf(count));
            info.put("attempts", String.valueOf(phammer_attempts));
            info.put("interval", String.valueOf(pban_timeout));
            info.put("timeout", String.valueOf(pban_timeout * banned_count));
            info.put("alert_msg", String.valueOf(msg));
            info.put("alert_msg2", String.valueOf(msg2));
            info.put("user_name", thisSession.uiSG("user_name"));
            UserTools.ut.doLoginFailureAlert(thisSession, thisSession.uiSG("user_name"), this.uiSG("listen_ip_port"), this.user, info, "repeated_login_failure_user_banned");
            return false;
        }
        return true;
    }

    public void do_Recycle(GenericClient c, VRL vrl, String the_dir) throws Exception {
        String path;
        String recycle = ServerStatus.SG("recycle_path");
        if (!recycle.startsWith("/")) {
            recycle = "/" + recycle;
        }
        if (!recycle.endsWith("/")) {
            recycle = String.valueOf(recycle) + "/";
        }
        if ((path = the_dir).equals("/")) {
            return;
        }
        Log.log("SERVER", 0, "Moving item to recycle location instead of deleting: " + vrl.safe());
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (vrl.getProtocol().equalsIgnoreCase("file")) {
            try {
                File_B v = new File_B(vrl.getCanonicalPath());
                new File_B(String.valueOf(recycle) + Common.all_but_last(path)).mkdirs();
                String addOn = "";
                int pos = 1;
                while (new File_B(String.valueOf(recycle) + Common.all_but_last(path) + v.getName() + addOn).exists()) {
                    addOn = String.valueOf(pos++);
                }
                File_B trash_item = new File_B(String.valueOf(recycle) + Common.all_but_last(path) + v.getName() + addOn);
                boolean ok = v.renameTo(trash_item);
                if (!ok) {
                    Common.recurseCopy(v.getCanonicalPath(), trash_item.getCanonicalPath(), true);
                    v.delete();
                }
                trash_item.setLastModified(System.currentTimeMillis());
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
                throw e;
            }
        }
        boolean i = false;
        String addOn = "";
        int pos = 1;
        boolean isfile = true;
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
            isfile = false;
        }
        new File_B(String.valueOf(recycle) + Common.all_but_last(path)).mkdirs();
        while (new File_B(String.valueOf(recycle) + path + addOn).exists()) {
            addOn = String.valueOf(pos++);
        }
        if (!isfile) {
            addOn = String.valueOf(addOn) + "/";
        }
        VRL vrl_dest = new VRL(String.valueOf(recycle) + path + addOn);
        FileClient f_dest = new FileClient("/", "Recycle", new Vector());
        boolean delete = false;
        try {
            try {
                StringBuffer log = new StringBuffer();
                Common.recurseCopy(vrl, vrl_dest, c, f_dest, 0, true, log);
                delete = c.delete(vrl.getPath());
                if (!delete) {
                    f_dest.delete(vrl_dest.getPath());
                }
                Log.log("SERVER", 2, log.toString());
            }
            catch (Exception e) {
                if (!delete && f_dest.stat(vrl_dest.getPath()) != null) {
                    f_dest.delete(vrl_dest.getPath());
                }
                Log.log("SERVER", 1, e);
                throw e;
            }
        }
        finally {
            f_dest.logout();
        }
    }

    public void removeCacheItem(Properties item) {
        boolean ok = true;
        int x = -1;
        while (x < 10 && ok) {
            String tmpKey = String.valueOf(x) + item.getProperty("root_dir").substring(1) + item.getProperty("name") + this.uiSG("user_name");
            ok = this.uVFS.cacheItem.remove(tmpKey) == null;
            this.uVFS.cacheItemStamp.remove(tmpKey);
            ++x;
        }
    }

    /*
     * Unable to fully structure code
     */
    public String do_DELE(boolean recurse, String user_dir) throws Exception {
        block51: {
            this.uiPUT("the_command", "DELE");
            this.uiPUT("last_logged_command", "DELE");
            the_dir = this.fixupDir(user_dir);
            parentPath = this.uVFS.getPrivPath(the_dir, false, false);
            dir_item = this.uVFS.get_item(parentPath, -1);
            item = this.uVFS.get_item(the_dir, -1);
            if (item == null) {
                dir_item.put("privs", String.valueOf(dir_item.getProperty("privs")) + "(inherited)");
                if (this.check_access_privs(the_dir, this.uiSG("the_command"), dir_item)) {
                    Common.trackSync("DELETE", the_dir, null, false, 0L, 0L, this.SG("root_dir"), dir_item.getProperty("privs"), this.uiSG("clientid"), "");
                }
                this.not_done = this.ftp_write_command("550", LOC.G("%DELE-not found%"));
                this.doErrorEvent(new Exception(this.uiSG("lastLog")));
                this.uiVG("failed_commands").addElement("" + new Date().getTime());
                return "%DELE-not found%";
            }
            if (!this.check_access_privs(the_dir, this.uiSG("the_command"), item) || !Common.filter_check("X", Common.last(the_dir), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.SG("file_filter")) || !Common.filter_check("F", String.valueOf(item.getProperty("name")) + (item.getProperty("type").equalsIgnoreCase("DIR") != false && item.getProperty("name").endsWith("/") == false ? "/" : ""), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.SG("file_filter")) || !Common.filter_check("DIR", String.valueOf(item.getProperty("name")) + (item.getProperty("type").equalsIgnoreCase("DIR") != false && item.getProperty("name").endsWith("/") == false ? "/" : ""), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.SG("file_filter"))) break block51;
            check_all = false;
            if (this.uiSG("the_command").equalsIgnoreCase("DELE") && !this.check_access_privs(the_dir, "RMD", item)) {
                check_all = true;
            }
            this.changeProxyToCurrentDir(item);
            Common.trackSync("DELETE", the_dir, null, false, 0L, 0L, this.SG("root_dir"), dir_item.getProperty("privs"), this.uiSG("clientid"), "");
            stat = null;
            quota = -12345L;
            if (item == null) {
                this.not_done = this.ftp_write_command("550", LOC.G("%DELE-not found%"));
                this.doErrorEvent(new Exception(this.uiSG("lastLog")));
                this.uiVG("failed_commands").addElement("" + new Date().getTime());
                return "%DELE-not found%";
            }
            c = this.uVFS.getClient(item);
            try {
                block52: {
                    block53: {
                        block50: {
                            block49: {
                                fix_url = item.getProperty("url");
                                if (fix_url.endsWith(" ")) {
                                    fix_url = Common.replace_str(fix_url, " ", "%20");
                                }
                                if ((stat = c.stat(new VRL(fix_url).getPath())).getProperty("type").equalsIgnoreCase("DIR")) {
                                    this.uiPUT("the_command", "RMD");
                                    if (!this.check_access_privs(the_dir, this.uiSG("the_command"))) {
                                        this.uiPUT("the_command", "DELE");
                                        this.not_done = this.ftp_write_command("550", LOC.G("%DELE-bad%"));
                                        this.doErrorEvent(new Exception(this.uiSG("lastLog")));
                                        this.uiVG("failed_commands").addElement("" + new Date().getTime());
                                        return "%DELE-bad%";
                                    }
                                    this.uiPUT("the_command", "DELE");
                                }
                                quota = this.get_quota(the_dir);
                                if (item == null || stat == null) break block52;
                                fileItem = (Properties)item.clone();
                                fileItem = (Properties)fileItem.clone();
                                Log.log("FTP_SERVER", 2, "Tracking delete:" + the_dir);
                                fileItem.put("the_command", "DELE");
                                fileItem.put("the_command_data", the_dir);
                                fileItem.put("url", item.getProperty("url", ""));
                                fileItem.put("the_file_path", the_dir);
                                fileItem.put("the_file_name", stat.getProperty("name"));
                                fileItem.put("the_file_size", stat.getProperty("size"));
                                fileItem.put("the_file_speed", "0");
                                fileItem.put("the_file_start", String.valueOf(new Date().getTime()));
                                fileItem.put("the_file_end", String.valueOf(new Date().getTime()));
                                fileItem.put("the_file_error", "");
                                fileItem.put("the_file_type", stat.getProperty("type", ""));
                                fileItem.put("the_file_status", "SUCCESS");
                                ServerStatus.thisObj.statTools.add_item_stat(this, fileItem, "DELETE");
                                this.do_event5("DELETE", fileItem);
                                totalSize = Long.parseLong(item.getProperty("size", "0"));
                                deleted = false;
                                info = new Properties();
                                info.put("crushftp_user_name", this.uiSG("user_name"));
                                Common.trackSyncRevision(c, new VRL(fix_url), the_dir, this.SG("root_dir"), item.getProperty("privs"), true, info);
                                SearchHandler.buildEntry(item, this.uVFS, "delete", null);
                                track_delete_sub_items = new Vector<Properties>();
                                if (fileItem.getProperty("type", "").equals("DIR") && this.user.getProperty("track_delete_sub_items", "false").equals("true")) {
                                    list1 = new Vector<E>();
                                    the_dir_f = the_dir;
                                    errors = new Vector<E>();
                                    status = new Properties();
                                    Worker.startWorker(new Runnable(){

                                        @Override
                                        public void run() {
                                            try {
                                                try {
                                                    SessionCrush.this.uVFS.getListing(list1, the_dir_f, 99, 999, true);
                                                    status.put("done", "true");
                                                }
                                                catch (Exception e) {
                                                    Log.log("SERVER", 1, e);
                                                    errors.addElement(e);
                                                    status.put("done", "true");
                                                }
                                            }
                                            finally {
                                                status.put("done", "true");
                                            }
                                        }
                                    });
                                    while (list1.size() > 0 || !status.containsKey("done")) {
                                        if (list1.size() == 0) {
                                            Thread.sleep(100L);
                                            continue;
                                        }
                                        p = (Properties)list1.remove(0);
                                        if (p.getProperty("url", "").equals(fileItem.getProperty("url", ""))) continue;
                                        track_delete_sub_items.add(p);
                                    }
                                }
                                c.setConfig("recurse_delete", String.valueOf(recurse));
                                if (ServerStatus.BG("recycle")) {
                                    Log.log("FTP_SERVER", 3, String.valueOf(LOC.G("Attempting to recycle file:")) + the_dir);
                                    this.do_Recycle(c, new VRL(fix_url), the_dir);
                                    if (c instanceof GenericClientMulti) {
                                        c.setConfig("skip_first_client", "true");
                                        deleted = c.delete(new VRL(fix_url).getPath());
                                        c.setConfig("skip_first_client", "false");
                                    }
                                } else {
                                    Log.log("FTP_SERVER", 3, String.valueOf(LOC.G("Attempting to delete file:")) + the_dir);
                                    deleted = c.delete(new VRL(fix_url).getPath());
                                }
                                stat = null;
                                if (!deleted) {
                                    stat = c.stat(new VRL(fix_url).getPath());
                                }
                                if (stat == null && track_delete_sub_items.size() > 0 && this.user.getProperty("track_delete_sub_items", "false").equals("true")) {
                                    ServerStatus.thisObj.statTools.add_items_stat(this, track_delete_sub_items, "DELETE", "DELE");
                                }
                                if (deleted || stat == null || !recurse) break block53;
                                try {
                                    totalSize = Common.recurseSize_U(new VRL(fix_url).getCanonicalPath(), 0L, this);
                                }
                                catch (Exception e) {
                                    Log.log("FTP_SERVER", 1, e);
                                }
                                v0 = has_events = this.user.get("events") != null && ((Vector)this.user.get("events")).size() > 0;
                                if (has_events) {
                                    has_events = false;
                                    events = (Vector)this.user.get("events");
                                    x = 0;
                                    while (x < events.size()) {
                                        event = (Properties)events.elementAt(x);
                                        if (event.getProperty("event_user_action_list", "").indexOf("(delete)") >= 0) {
                                            has_events = true;
                                        }
                                        ++x;
                                    }
                                }
                                has_delete_event = has_events;
                                if (!check_all && !ServerStatus.BG("check_all_recursive_deletes") && !stat.getProperty("check_all_recursive_deletes", "").equals("true")) ** GOTO lbl223
                                list1 = new Vector<E>();
                                the_dir_f = the_dir;
                                errors = new Vector<E>();
                                status = new Properties();
                                Worker.startWorker(new Runnable(){

                                    @Override
                                    public void run() {
                                        try {
                                            try {
                                                SessionCrush.this.uVFS.getListing(list1, the_dir_f, 99, 999, true);
                                                status.put("done", "true");
                                            }
                                            catch (Exception e) {
                                                Log.log("SERVER", 1, e);
                                                errors.addElement(e);
                                                status.put("done", "true");
                                            }
                                        }
                                        finally {
                                            status.put("done", "true");
                                        }
                                    }
                                });
                                threads = new Vector<String>();
                                list2 = new Vector<Properties>();
                                while (list1.size() > 0 || !status.containsKey("done")) {
                                    if (list1.size() == 0) {
                                        Thread.sleep(100L);
                                        continue;
                                    }
                                    p = (Properties)list1.remove(0);
                                    thisObj = this;
                                    if (p.getProperty("type", "").equals("FILE")) {
                                        while (threads.size() > ServerStatus.IG("delete_threads")) {
                                            Thread.sleep(100L);
                                        }
                                        threads.addElement(p.getProperty("url"));
                                        Worker.startWorker(new Runnable(){

                                            @Override
                                            public void run() {
                                                VRL vrl;
                                                boolean ok2;
                                                block11: {
                                                    ok2 = true;
                                                    String temp_dir = String.valueOf(p.getProperty("root_dir")) + p.getProperty("name");
                                                    vrl = new VRL(p.getProperty("url"));
                                                    try {
                                                        try {
                                                            if (SessionCrush.this.check_access_privs(temp_dir, "DELE", p)) {
                                                                if (has_delete_event || SessionCrush.this.user.getProperty("track_delete_sub_items", "false").equals("true")) {
                                                                    Properties fileItem = c.stat(vrl.getPath());
                                                                    String path = vrl.getPath();
                                                                    Log.log("FTP_SERVER", 2, String.valueOf(LOC.G("Tracking delete:")) + path);
                                                                    fileItem.put("the_command", "DELE");
                                                                    fileItem.put("the_command_data", path);
                                                                    fileItem.put("url", "" + vrl);
                                                                    fileItem.put("the_file_path", path);
                                                                    fileItem.put("the_file_name", vrl.getName());
                                                                    fileItem.put("the_file_size", String.valueOf(fileItem.getProperty("size")));
                                                                    fileItem.put("the_file_speed", "0");
                                                                    fileItem.put("the_file_start", String.valueOf(new Date().getTime()));
                                                                    fileItem.put("the_file_end", String.valueOf(new Date().getTime()));
                                                                    fileItem.put("the_file_error", "");
                                                                    fileItem.put("the_file_type", fileItem.getProperty("type"));
                                                                    fileItem.put("the_file_status", "SUCCESS");
                                                                    ServerStatus.thisObj.statTools.add_item_stat(thisObj, fileItem, "DELETE");
                                                                    if (has_delete_event) {
                                                                        SessionCrush.this.do_event5("DELETE", fileItem);
                                                                    }
                                                                }
                                                                if (ok2 = c.delete(vrl.getPath())) {
                                                                    if (ServerStatus.BG("remove_keywords_on_delete")) {
                                                                        Common.remove_keywords(p);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        catch (Exception e) {
                                                            Log.log("SERVER", 1, e);
                                                            errors.addElement(e);
                                                            threads.remove(p.getProperty("url"));
                                                            break block11;
                                                        }
                                                    }
                                                    catch (Throwable throwable) {
                                                        threads.remove(p.getProperty("url"));
                                                        throw throwable;
                                                    }
                                                    threads.remove(p.getProperty("url"));
                                                }
                                                if (!ok2) {
                                                    errors.addElement(vrl.safe());
                                                }
                                            }
                                        });
                                        continue;
                                    }
                                    list2.addElement(p);
                                }
                                loops = 0;
                                while (loops < 6000 && threads.size() > 0) {
                                    Thread.sleep(100L);
                                    ++loops;
                                }
                                if (errors.size() <= 0) break block49;
                                Log.log("SERVER", 1, "Failed to delete:" + errors);
                                return "%DELE-error%";
                            }
                            try {
                                loop = 0;
                                while (list2.size() > 0 && loop < 5) {
                                    x = list2.size() - 1;
                                    while (x >= 0) {
                                        p = (Properties)list2.elementAt(x);
                                        if (p.getProperty("type", "").equals("DIR") && this.check_access_privs(temp_dir = String.valueOf(p.getProperty("root_dir")) + p.getProperty("name"), "RMD", p)) {
                                            delete_url = new VRL(p.getProperty("url"));
                                            if (this.user.getProperty("track_delete_sub_items", "false").equals("true")) {
                                                deleteItem = c.stat(delete_url.getPath());
                                                path = delete_url.getPath();
                                                Log.log("FTP_SERVER", 2, String.valueOf(LOC.G("Tracking delete:")) + path);
                                                deleteItem.put("the_command", "DELE");
                                                deleteItem.put("the_command_data", path);
                                                deleteItem.put("url", "" + delete_url);
                                                deleteItem.put("the_file_path", path);
                                                deleteItem.put("the_file_name", delete_url.getName());
                                                deleteItem.put("the_file_size", String.valueOf(deleteItem.getProperty("size")));
                                                deleteItem.put("the_file_speed", "0");
                                                deleteItem.put("the_file_start", String.valueOf(new Date().getTime()));
                                                deleteItem.put("the_file_end", String.valueOf(new Date().getTime()));
                                                deleteItem.put("the_file_error", "");
                                                deleteItem.put("the_file_type", deleteItem.getProperty("type"));
                                                deleteItem.put("the_file_status", "SUCCESS");
                                                ServerStatus.thisObj.statTools.add_item_stat(this, deleteItem, "DELETE");
                                            }
                                            if (c.delete(delete_url.getPath())) {
                                                if (ServerStatus.BG("remove_keywords_on_delete")) {
                                                    Common.remove_keywords(p);
                                                }
                                                list2.removeElementAt(x);
                                            }
                                        }
                                        --x;
                                    }
                                    ++loop;
                                }
                                break block50;
lbl223:
                                // 1 sources

                                if (track_delete_sub_items.size() > 0 && this.user.getProperty("track_delete_sub_items", "false").equals("true")) {
                                    ServerStatus.thisObj.statTools.add_items_stat(this, track_delete_sub_items, "DELETE", "DELE");
                                }
                                Common.recurseDelete_U(new VRL(fix_url).getCanonicalPath(), false);
                                c.delete(the_dir);
                            }
                            catch (NullPointerException var19_20) {
                                // empty catch block
                            }
                        }
                        if (item != null) {
                            this.trackAndUpdateUploads(this.uiVG("lastUploadStats"), new VRL(fix_url), new VRL(fix_url), "DELETE");
                        }
                    }
                    stat = null;
                    if (!deleted) {
                        stat = c.stat(new VRL(fix_url).getPath());
                    }
                    if (!deleted && stat != null) {
                        if (!new File_B(ServerStatus.SG("recycle_path")).exists() && ServerStatus.BG("recycle")) {
                            this.not_done = this.ftp_write_command("550", LOC.G("%DELE-error%:Recycle bin not found."));
                            this.doErrorEvent(new Exception(this.uiSG("lastLog")));
                            this.uiVG("failed_commands").addElement("" + new Date().getTime());
                            return "%DELE-error%:Recycle bin not found.";
                        }
                        Log.log("FTP_SERVER", 3, LOC.G("Delete failure.  Deleted:$0 Exists:$1", String.valueOf(deleted), "" + (stat != null)));
                        this.not_done = this.ftp_write_command("550", LOC.G("%DELE-error%"));
                        this.doErrorEvent(new Exception(this.uiSG("lastLog")));
                        this.uiVG("failed_commands").addElement("" + new Date().getTime());
                        return "%DELE-error%";
                    }
                    this.not_done = ServerStatus.BG("generic_ftp_responses") != false ? this.ftp_write_command("250", "Delete operation successful.") : this.ftp_write_command("250", ServerStatus.SG("custom_delete_msg"));
                    this.removeCacheItem(item);
                    if (quota != -12345L) {
                        quota = item.getProperty("privs", "").indexOf("(real_quota)") >= 0 ? (quota += totalSize) : (quota += totalSize);
                        this.set_quota(the_dir, quota);
                    }
                    if (ServerStatus.BG("remove_keywords_on_delete")) {
                        Common.remove_keywords(item);
                    }
                    return "";
                }
                this.not_done = this.ftp_write_command("550", LOC.G("%DELE-not found%"));
                this.doErrorEvent(new Exception(this.uiSG("lastLog")));
                this.uiVG("failed_commands").addElement("" + new Date().getTime());
                return "%DELE-not found%";
            }
            finally {
                this.uVFS.releaseClient(c);
            }
        }
        this.not_done = this.ftp_write_command("550", LOC.G("%DELE-bad%"));
        this.doErrorEvent(new Exception(this.uiSG("lastLog")));
        this.uiVG("failed_commands").addElement("" + new Date().getTime());
        return "%DELE-bad%";
    }

    public String do_RNFR(String path1) throws Exception {
        this.uiPUT("the_command", "RNFR");
        this.uiPUT("last_logged_command", "RNFR");
        this.rnfr_file_path = null;
        if (ServerStatus.BG("filepart_silent_ignore") && path1.endsWith(".filepart")) {
            path1 = path1.substring(0, path1.lastIndexOf(".filepart"));
        }
        String the_dir = this.fixupDir(path1);
        String parentPath = this.uVFS.getRootVFS(the_dir, -1);
        Properties dir_item = this.uVFS.get_item(parentPath, -1);
        Properties item = this.uVFS.get_fake_item(the_dir, "FILE");
        if (item == null) {
            Thread.sleep(500L);
            item = this.uVFS.get_fake_item(the_dir, "FILE");
        }
        if (this.check_access_privs(the_dir, this.uiSG("the_command"), item)) {
            this.changeProxyToCurrentDir(item);
            GenericClient c = this.uVFS.getClient(item);
            try {
                if (c.stat(new VRL(item.getProperty("url")).getPath()) != null) {
                    this.not_done = this.ftp_write_command("350", LOC.G("%RNFR%"));
                    this.rnfr_file_path = the_dir;
                    return "";
                }
                this.not_done = this.ftp_write_command("550", LOC.G("%RNFR-not found%"));
                this.doErrorEvent(new Exception(this.uiSG("lastLog")));
                this.uiVG("failed_commands").addElement("" + new Date().getTime());
                return "%RNFR-not found%";
            }
            finally {
                c = this.uVFS.releaseClient(c);
            }
        }
        this.not_done = this.ftp_write_command("550", LOC.G("%RNFR-bad%"));
        this.doErrorEvent(new Exception(this.uiSG("lastLog")));
        this.uiVG("failed_commands").addElement("" + new Date().getTime());
        return "%RNFR-bad%";
    }

    public void trackAndUpdateUploads(Vector lastUploadStats, VRL src, VRL dest, String type) {
        if (lastUploadStats == null) {
            return;
        }
        int x = lastUploadStats.size() - 1;
        while (x >= 0) {
            Properties p2 = (Properties)lastUploadStats.elementAt(x);
            if (VRL.fileFix(p2.getProperty("url", "")).toUpperCase().equals(VRL.fileFix(src.toString()).toUpperCase()) && type.equals("RENAME")) {
                p2.put("url", "" + dest);
                p2.put("path", dest.getPath());
                p2.put("name", dest.getName());
                break;
            }
            if (VRL.fileFix(p2.getProperty("url", "")).toUpperCase().equals(VRL.fileFix(src.toString()).toUpperCase()) && type.equals("DELETE")) {
                this.do_event5("DELETE", p2, null);
                lastUploadStats.removeElementAt(x);
                break;
            }
            --x;
        }
    }

    public String do_RNTO(boolean overwrite, String path1, String path2) throws Exception {
        if (ServerStatus.BG("filepart_silent_ignore") && path1.endsWith(".filepart")) {
            return "";
        }
        this.uiPUT("the_command", "RNTO");
        this.uiPUT("last_logged_command", "RNTO");
        String the_dir = this.fixupUnsafeChars(this.fixupDir(path2));
        Properties combinedPermissions = this.uVFS.getCombinedPermissions();
        boolean aclPermissions = combinedPermissions.getProperty("acl_permissions", "false").equals("true");
        Properties actual_item = this.uVFS.get_item(the_dir);
        Properties item = this.uVFS.get_item_parent(the_dir);
        Properties rnfr_file = this.uVFS.get_item(path1);
        boolean merged_vfs = false;
        if (ServerStatus.BG("merged_vfs") && rnfr_file.containsKey("vItem") && item.containsKey("vItem")) {
            Properties rnfr_vItem = (Properties)rnfr_file.get("vItem");
            Properties item_vItem = (Properties)item.get("vItem");
            if (!rnfr_vItem.getProperty("vfs_home_index", "0").equalsIgnoreCase(item_vItem.getProperty("vfs_home_index", "0"))) {
                item = this.uVFS.get_item_parent(the_dir, Integer.parseInt(rnfr_vItem.getProperty("vfs_home_index", "0")));
                merged_vfs = true;
            }
        }
        if (!aclPermissions) {
            actual_item = item;
        }
        boolean file_filter = true;
        if (!(rnfr_file == null || actual_item == null || !rnfr_file.getProperty("type").equalsIgnoreCase("FILE") || Common.filter_check("F", actual_item.getProperty("name"), String.valueOf(ServerStatus.SG("filename_filters_str")) + CRLF + this.SG("file_filter")) && Common.filter_check("F", rnfr_file.getProperty("name"), String.valueOf(ServerStatus.SG("filename_filters_str")) + CRLF + this.SG("file_filter")))) {
            file_filter = false;
        }
        boolean dir_filter = true;
        if (!(rnfr_file == null || actual_item == null || !rnfr_file.getProperty("type").equalsIgnoreCase("DIR") || Common.filter_check("DIR", String.valueOf(actual_item.getProperty("name")) + (!actual_item.getProperty("name").endsWith("/") ? "/" : ""), String.valueOf(ServerStatus.SG("filename_filters_str")) + CRLF + this.SG("file_filter")) && Common.filter_check("DIR", String.valueOf(rnfr_file.getProperty("name")) + (!rnfr_file.getProperty("name").endsWith("/") ? "/" : ""), String.valueOf(ServerStatus.SG("filename_filters_str")) + CRLF + this.SG("file_filter")))) {
            dir_filter = false;
        }
        if (this.check_access_privs(the_dir, this.uiSG("the_command"), item) && (!overwrite || overwrite && this.check_access_privs(Common.all_but_last(the_dir), "DELE", actual_item)) && Common.filter_check("R", Common.last(the_dir), String.valueOf(ServerStatus.SG("filename_filters_str")) + CRLF + this.SG("file_filter")) && file_filter && dir_filter) {
            this.changeProxyToCurrentDir(this.uVFS.get_item_parent(Common.all_but_last(the_dir)));
            GenericClient c = null;
            try {
                c = merged_vfs ? this.uVFS.getClientSingle(rnfr_file) : this.uVFS.getClient(item);
            }
            catch (Exception e) {
                Log.log("RNTO", 0, "Invalid path used on rename, attempting to fix..." + the_dir);
                Properties temp_item = this.uVFS.get_item(Common.all_but_last(the_dir));
                item.put("url", String.valueOf(temp_item.getProperty("url")) + Common.last(item.getProperty("url")));
                c = this.uVFS.getClient(item);
            }
            try {
                VRL vrl = new VRL(item.getProperty("url"));
                boolean exists = c.stat(vrl.getPath()) != null;
                boolean view = this.check_access_privs(Common.all_but_last(the_dir), "LIST", actual_item);
                if (rnfr_file == null) {
                    Common.trackSync("RENAME", this.rnfr_file_path, String.valueOf(item.getProperty("root_dir", "")) + item.getProperty("name", "") + (this.rnfr_file_path.endsWith("/") ? "/" : ""), false, 0L, 0L, this.SG("root_dir"), item.getProperty("privs"), this.uiSG("clientid"), "");
                    return "";
                }
                if (!exists || exists && overwrite || exists && !view || rnfr_file.getProperty("url").equalsIgnoreCase(item.getProperty("url")) || rnfr_file.getProperty("url").toUpperCase().equals(String.valueOf(item.getProperty("url").toUpperCase()) + "/") || new VRL(rnfr_file.getProperty("url")).getPath().equalsIgnoreCase(vrl.getPath())) {
                    if (vrl.toString().length() > ServerStatus.IG("max_url_length")) {
                        throw new IOException("File url length too long:" + vrl.toString().length() + " vs. " + ServerStatus.IG("max_url_length"));
                    }
                    UserTools.updatePrivpath(this.uiSG("listen_ip_port"), this.uiSG("user_name"), String.valueOf(rnfr_file.getProperty("root_dir", "")) + rnfr_file.getProperty("name", ""), the_dir, item, null, this.uVFS);
                    if (overwrite && !vrl.getPath().equalsIgnoreCase(new VRL(rnfr_file.getProperty("url")).getPath()) && c.stat(new VRL(rnfr_file.getProperty("url")).getPath()) != null) {
                        boolean is_deleted = false;
                        try {
                            is_deleted = c.delete(vrl.getPath());
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 1, "Rename error. Path from: " + path1 + " Path to " + path2 + " Error message : " + e);
                        }
                        if (!is_deleted && rnfr_file.getProperty("type").equalsIgnoreCase("DIR") && vrl.getProtocol().equalsIgnoreCase("FILE") && (c.getConfig("file_recurse_delete") == null || !c.getConfig("file_recurse_delete").equals("true"))) {
                            try {
                                c.setConfig("file_recurse_delete", "true");
                                is_deleted = c.delete(vrl.getPath());
                                c.setConfig("file_recurse_delete", "false");
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 1, "Rename error. Path from: " + path1 + " Path to " + path2 + " Error message : " + e);
                            }
                        }
                    }
                    if (exists && !view && !overwrite) {
                        STOR_handler.do_unique_rename(actual_item, vrl, c, false, Common.last(vrl.toString()), c.stat(vrl.getPath()));
                    }
                    boolean rename_result = false;
                    try {
                        rename_result = c.rename(new VRL(rnfr_file.getProperty("url")).getPath(), vrl.getPath(), overwrite);
                    }
                    catch (Exception e) {
                        this.not_done = this.ftp_write_command("550", String.valueOf(LOC.G("%RNTO-bad%")) + " " + e);
                        this.doErrorEvent(new Exception(String.valueOf(this.uiSG("lastLog")) + " " + e));
                        this.uiVG("failed_commands").addElement("" + new Date().getTime());
                        rnfr_file = null;
                        this.rnfr_file_path = null;
                        String string = String.valueOf(e.getMessage());
                        c = this.uVFS.releaseClient(c);
                        return string;
                    }
                    if (rename_result) {
                        this.trackAndUpdateUploads(this.uiVG("lastUploadStats"), new VRL(rnfr_file.getProperty("url")), new VRL(item.getProperty("url")), "RENAME");
                    } else {
                        String srcPath = new VRL(rnfr_file.getProperty("url")).getCanonicalPath();
                        String dstPath = new VRL(item.getProperty("url")).getCanonicalPath();
                        if (dstPath.startsWith(srcPath) || !new VRL(rnfr_file.getProperty("url")).getProtocol().equalsIgnoreCase("file") || !new VRL(item.getProperty("url")).getProtocol().equalsIgnoreCase("file")) {
                            this.not_done = this.ftp_write_command("550", LOC.G("%RNTO-bad%"));
                            this.doErrorEvent(new Exception(this.uiSG("lastLog")));
                            this.uiVG("failed_commands").addElement("" + new Date().getTime());
                            rnfr_file = null;
                            this.rnfr_file_path = null;
                            return "%RNTO-bad%";
                        }
                        if (rnfr_file.getProperty("type").equalsIgnoreCase("DIR")) {
                            if ((srcPath = String.valueOf(srcPath) + "/").equals(dstPath = String.valueOf(dstPath) + "/")) {
                                dstPath = String.valueOf(dstPath) + " Copy/";
                            }
                            Common.recurseCopy_U(srcPath, dstPath, true);
                        } else {
                            if (srcPath.equals(dstPath)) {
                                dstPath = String.valueOf(dstPath) + " Copy";
                            }
                            Common.recurseCopy_U(srcPath, dstPath, true);
                        }
                        Common.recurseDelete_U(srcPath, false);
                        this.trackAndUpdateUploads(this.uiVG("lastUploadStats"), new VRL(rnfr_file.getProperty("url")), new VRL(item.getProperty("url")), "RENAME");
                    }
                    Properties fileItem1 = item;
                    fileItem1 = (Properties)fileItem1.clone();
                    Log.log("FTP_SERVER", 2, String.valueOf(LOC.G("Tracking rename:")) + the_dir);
                    fileItem1.put("the_command", "RNTO");
                    fileItem1.put("the_command_data", the_dir);
                    fileItem1.put("the_file_path2", rnfr_file.getProperty("root_dir", ""));
                    fileItem1.put("url_2", rnfr_file.getProperty("url", ""));
                    fileItem1.put("the_file_name_2", rnfr_file.getProperty("name"));
                    fileItem1.put("the_file_path", the_dir);
                    fileItem1.put("the_file_name", item.getProperty("name"));
                    fileItem1.put("the_file_size", rnfr_file.getProperty("size", "0"));
                    fileItem1.put("the_file_speed", "0");
                    fileItem1.put("the_file_start", String.valueOf(new Date().getTime()));
                    fileItem1.put("the_file_end", String.valueOf(new Date().getTime()));
                    fileItem1.put("the_file_error", "");
                    fileItem1.put("the_file_status", "SUCCESS");
                    fileItem1.put("the_file_type", rnfr_file.getProperty("type", ""));
                    fileItem1.put("type", rnfr_file.getProperty("type", ""));
                    Properties fileItem2 = (Properties)fileItem1.clone();
                    fileItem2.put("url", fileItem2.getProperty("url_2"));
                    fileItem2.put("the_file_name", fileItem2.getProperty("the_file_name_2"));
                    Properties temp_rename = (Properties)fileItem1.clone();
                    temp_rename.put("the_file_name", String.valueOf(temp_rename.getProperty("the_file_name_2")) + ":" + temp_rename.getProperty("the_file_name"));
                    temp_rename.put("the_file_path", String.valueOf(temp_rename.getProperty("the_file_path2")) + ":" + temp_rename.getProperty("the_file_path"));
                    temp_rename.put("the_file_type", rnfr_file.getProperty("type", ""));
                    temp_rename.put("url", String.valueOf(temp_rename.getProperty("url_2")) + ":" + temp_rename.getProperty("url"));
                    ServerStatus.thisObj.statTools.add_item_stat(this, temp_rename, "RENAME");
                    this.do_event5("RENAME", fileItem1, fileItem2);
                    if (!fileItem1.getProperty("the_file_path2").equals(fileItem1.getProperty("root_dir"))) {
                        ServerStatus.thisObj.statTools.add_item_stat(this, temp_rename, "MOVE");
                    }
                    this.not_done = ServerStatus.BG("generic_ftp_responses") ? this.ftp_write_command("250", LOC.G("Rename successful.")) : this.ftp_write_command("250", LOC.G("%RNTO%"));
                    boolean isDir = rnfr_file.getProperty("type").equalsIgnoreCase("DIR");
                    item.put("type", isDir ? "DIR" : "FILE");
                    Common.trackSync("RENAME", String.valueOf(rnfr_file.getProperty("root_dir", "")) + rnfr_file.getProperty("name", "") + (isDir ? "/" : ""), String.valueOf(item.getProperty("root_dir", "")) + item.getProperty("name", "") + (isDir ? "/" : ""), false, 0L, 0L, this.SG("root_dir"), item.getProperty("privs"), this.uiSG("clientid"), "");
                    SearchHandler.buildEntry(rnfr_file, this.uVFS, "rename", item);
                    rnfr_file = null;
                    this.rnfr_file_path = null;
                    this.uVFS.reset();
                    return "";
                }
                this.not_done = this.ftp_write_command("550", LOC.G("%RNTO-error%"));
                this.doErrorEvent(new Exception(this.uiSG("lastLog")));
                this.uiVG("failed_commands").addElement("" + new Date().getTime());
                rnfr_file = null;
                this.rnfr_file_path = null;
                return "%RNTO-error%";
            }
            finally {
                c = this.uVFS.releaseClient(c);
            }
        }
        this.not_done = this.ftp_write_command("550", LOC.G("%RNTO-bad%"));
        this.doErrorEvent(new Exception(this.uiSG("lastLog")));
        this.uiVG("failed_commands").addElement("" + new Date().getTime());
        rnfr_file = null;
        this.rnfr_file_path = null;
        return "%RNTO-bad%";
    }

    public String do_MKD(boolean mkdirs, String user_dir) throws Exception {
        this.uiPUT("the_command", "MKD");
        this.uiPUT("last_logged_command", "MKD");
        String the_dir = this.fixupUnsafeChars(this.fixupDir(user_dir));
        if (!the_dir.endsWith("/")) {
            the_dir = String.valueOf(the_dir) + "/";
        }
        if (the_dir != null && the_dir.endsWith(" /")) {
            the_dir = String.valueOf(the_dir.substring(0, the_dir.length() - 2)) + "/";
        }
        Properties item = this.uVFS.get_item_parent(the_dir);
        if (item.getProperty("url").length() > ServerStatus.IG("max_url_length")) {
            throw new IOException("File url length too long:" + item.getProperty("url").length() + " vs. " + ServerStatus.IG("max_url_length"));
        }
        if (this.check_access_privs(the_dir, this.uiSG("the_command"), item) && Common.filter_check("DIR", String.valueOf(item.getProperty("name")) + (item.getProperty("type").equalsIgnoreCase("DIR") && !item.getProperty("name").endsWith("/") ? "/" : ""), String.valueOf(ServerStatus.SG("filename_filters_str")) + CRLF + this.SG("file_filter"))) {
            this.changeProxyToCurrentDir(item);
            Log.log("FTP_SERVER", 3, String.valueOf(LOC.G("Using item:")) + item);
            GenericClient c = this.uVFS.getClient(item);
            try {
                boolean result = false;
                boolean skip_make_dir = false;
                if (ServerStatus.BG("ftp_pre_check_mkdir") && c.stat(new VRL(item.getProperty("url")).getPath()) != null) {
                    skip_make_dir = true;
                }
                if (mkdirs && !skip_make_dir) {
                    Common.verifyOSXVolumeMounted(item.getProperty("url"));
                    result = c.makedirs(new VRL(item.getProperty("url")).getPath());
                } else if (!skip_make_dir) {
                    result = c.makedir(new VRL(item.getProperty("url")).getPath());
                }
                if (!result && c.stat(new VRL(item.getProperty("url")).getPath()) != null) {
                    this.not_done = this.ftp_write_command(System.getProperty("crushftp.mkd.451", "521"), LOC.G("%MKD-exists%"));
                    this.doErrorEvent(new Exception(this.uiSG("lastLog")));
                    this.uiVG("failed_commands").addElement("" + new Date().getTime());
                    return "%MKD-exists%";
                }
                Common.trackSync("CHANGE", the_dir, null, true, 0L, 0L, this.SG("root_dir"), item.getProperty("privs"), this.uiSG("clientid"), "");
                if (!result) {
                    this.not_done = this.ftp_write_command("550", LOC.G("%MKD-bad%"));
                    this.doErrorEvent(new Exception(this.uiSG("lastLog")));
                    this.uiVG("failed_commands").addElement("" + new Date().getTime());
                    return "%MKD-bad%";
                }
                this.setFolderPrivs(c, item);
                if (the_dir.startsWith(this.SG("root_dir"))) {
                    the_dir = the_dir.substring(this.SG("root_dir").length() - 1);
                }
                this.not_done = this.ftp_write_command("257", LOC.G("\"$0\" directory created.", the_dir));
                VRL vrl = new VRL(item.getProperty("url"));
                String path = vrl.getPath();
                Properties fileItem = c.stat(vrl.getPath());
                Log.log("FTP_SERVER", 2, String.valueOf(LOC.G("Tracking make directory:")) + path);
                fileItem.put("the_command", "MAKEDIR");
                fileItem.put("the_command_data", the_dir);
                fileItem.put("url", "" + vrl);
                fileItem.put("the_file_path", path);
                fileItem.put("the_file_name", vrl.getName());
                fileItem.put("the_file_size", "0");
                fileItem.put("the_file_speed", "0");
                fileItem.put("the_file_start", String.valueOf(System.currentTimeMillis()));
                fileItem.put("the_file_end", String.valueOf(System.currentTimeMillis()));
                fileItem.put("the_file_error", "");
                fileItem.put("the_file_type", "DIR");
                fileItem.put("the_file_status", "SUCCESS");
                ServerStatus.thisObj.statTools.add_item_stat(this, fileItem, "MAKEDIR");
                this.do_event5("MAKEDIR", fileItem);
            }
            finally {
                c = this.uVFS.releaseClient(c);
            }
            this.uVFS.reset();
            return "";
        }
        this.not_done = this.ftp_write_command("550", LOC.G("%MKD-bad%"));
        this.doErrorEvent(new Exception(this.uiSG("lastLog")));
        this.uiVG("failed_commands").addElement("" + new Date().getTime());
        return "%MKD-bad%";
    }

    public void setFolderPrivs(GenericClient c, Properties item) throws Exception {
        Properties parentItem;
        Properties vfs_posix_settings = Common.get_vfs_posix_settings(item.getProperty("privs", ""), false);
        if (!vfs_posix_settings.getProperty("vfs_owner", "").equals("")) {
            c.setOwner(new VRL(item.getProperty("url")).getPath(), ServerStatus.change_vars_to_values_static(vfs_posix_settings.getProperty("vfs_owner", ""), this.user, this.user_info, this), "");
            Log.log("FTP_SERVER", 2, String.valueOf(LOC.G("VFS permission setting: Set owner of new folder to:")) + vfs_posix_settings.getProperty("vfs_owner", ""));
        } else if (!this.SG("default_owner_command").equals("")) {
            c.setOwner(new VRL(item.getProperty("url")).getPath(), ServerStatus.change_vars_to_values_static(this.SG("default_owner_command"), this.user, this.user_info, this), "");
            Log.log("FTP_SERVER", 2, String.valueOf(LOC.G("Set owner of new folder to:")) + this.SG("default_owner_command"));
        } else if (!item.getProperty("owner", "").equals("user") && !item.getProperty("owner", "").equals("owner")) {
            try {
                parentItem = item;
                c.setOwner(new VRL(item.getProperty("url")).getPath(), parentItem.getProperty("owner", "").trim(), "");
                Log.log("FTP_SERVER", 2, String.valueOf(LOC.G("Set owner of new folder to:")) + parentItem.getProperty("owner", "").trim());
            }
            catch (Exception e) {
                Log.log("FTP_SERVER", 2, e);
            }
        }
        if (!vfs_posix_settings.getProperty("vfs_group", "").equals("")) {
            c.setGroup(new VRL(item.getProperty("url")).getPath(), ServerStatus.change_vars_to_values_static(vfs_posix_settings.getProperty("vfs_group", ""), this.user, this.user_info, this), "");
            Log.log("FTP_SERVER", 2, String.valueOf(LOC.G("VFS permission setting: Set group of new folder to:")) + vfs_posix_settings.getProperty("vfs_group", ""));
        } else if (!this.SG("default_group_command").equals("")) {
            c.setGroup(new VRL(item.getProperty("url")).getPath(), ServerStatus.change_vars_to_values_static(this.SG("default_group_command"), this.user, this.user_info, this), "");
            Log.log("FTP_SERVER", 2, String.valueOf(LOC.G("Set group of new folder to:")) + this.SG("default_group_command"));
        } else if (!item.getProperty("group", "").equals("group")) {
            try {
                parentItem = item;
                c.setGroup(new VRL(item.getProperty("url")).getPath(), parentItem.getProperty("group", "").trim(), "");
                Log.log("FTP_SERVER", 2, String.valueOf(LOC.G("Set group of new folder to:")) + parentItem.getProperty("group", "").trim());
            }
            catch (Exception e) {
                Log.log("FTP_SERVER", 2, e);
            }
        }
        String folderPrivs = vfs_posix_settings.getProperty("vfs_privs", "");
        if (folderPrivs.equals("")) {
            folderPrivs = this.SG("default_folder_privs_command");
        }
        if (folderPrivs == null || folderPrivs.equals("") && !this.SG("default_privs_command").equals("")) {
            folderPrivs = this.SG("default_privs_command");
        }
        if (!folderPrivs.equals("")) {
            c.setMod(new VRL(item.getProperty("url")).getPath(), folderPrivs, "");
            Log.log("FTP_SERVER", 2, String.valueOf(LOC.G("Set privs of new folder to:")) + folderPrivs);
        }
    }

    public String do_RMD(String user_dir) throws Exception {
        block12: {
            this.uiPUT("the_command", "RMD");
            this.uiPUT("last_logged_command", "RMD");
            String the_dir = user_dir;
            if (!this.uiSG("the_command_data").equals("")) {
                the_dir = this.uiSG("the_command_data").startsWith("/") ? this.uiSG("the_command_data") : String.valueOf(the_dir) + this.uiSG("the_command_data");
                if ((the_dir = com.crushftp.client.Common.dots(the_dir)).equals("/")) {
                    the_dir = this.SG("root_dir");
                }
                if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.SG("root_dir").toUpperCase())) {
                    the_dir = String.valueOf(this.SG("root_dir")) + the_dir.substring(1);
                }
            }
            String parentPath = this.uVFS.getRootVFS(the_dir, -1);
            Properties dir_item = this.uVFS.get_item(parentPath, -1);
            Properties item = this.uVFS.get_fake_item(the_dir, "DIR");
            if (!the_dir.endsWith("/")) {
                the_dir = String.valueOf(the_dir) + "/";
            }
            if (this.check_access_privs(the_dir, this.uiSG("the_command"), item) && item != null) {
                if (the_dir.equals(parentPath) || the_dir.equals(String.valueOf(parentPath) + "/")) {
                    return "%RMD-bad%";
                }
                this.changeProxyToCurrentDir(item);
                GenericClient c = this.uVFS.getClient(item);
                try {
                    Properties stat1 = c.stat(new VRL(item.getProperty("url")).getPath());
                    if (stat1 != null && stat1.getProperty("type").equalsIgnoreCase("dir")) {
                        if (c.delete(new VRL(this.uVFS.get_item(the_dir).getProperty("url")).getPath())) {
                            this.not_done = this.ftp_write_command("250", LOC.G("%RMD%"));
                            break block12;
                        }
                        this.not_done = this.ftp_write_command("550", LOC.G("%RMD-not_empty%"));
                        this.doErrorEvent(new Exception(this.uiSG("lastLog")));
                        this.uiVG("failed_commands").addElement("" + new Date().getTime());
                        return "%RMD-not_empty%";
                    }
                    this.not_done = this.ftp_write_command("550", LOC.G("%RMD-not_found%"));
                    this.doErrorEvent(new Exception(this.uiSG("lastLog")));
                    this.uiVG("failed_commands").addElement("" + new Date().getTime());
                    return "%RMD-not_found%";
                }
                finally {
                    c = this.uVFS.releaseClient(c);
                }
            }
            this.not_done = this.ftp_write_command("550", LOC.G("%RMD-bad%"));
            this.doErrorEvent(new Exception(this.uiSG("lastLog")));
            this.uiVG("failed_commands").addElement("" + new Date().getTime());
            return "%RMD-bad%";
        }
        return "";
    }

    public void changeProxyToCurrentDir(Properties item) throws Exception {
    }

    public String do_SIZE() throws Exception {
        String the_dir = this.uiSG("current_dir");
        if (!this.uiSG("the_command_data").equals("")) {
            the_dir = this.uiSG("the_command_data").startsWith("/") ? this.uiSG("the_command_data") : String.valueOf(the_dir) + this.uiSG("the_command_data");
            if ((the_dir = com.crushftp.client.Common.dots(the_dir)).equals("/")) {
                the_dir = this.SG("root_dir");
            }
            if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.SG("root_dir").toUpperCase())) {
                the_dir = String.valueOf(this.SG("root_dir")) + the_dir.substring(1);
            }
        }
        String parentPath = this.uVFS.getRootVFS(the_dir, -1);
        Properties dir_item = this.uVFS.get_item(parentPath, -1);
        Properties item = this.uVFS.get_item(the_dir);
        if (!this.check_access_privs(the_dir, this.uiSG("the_command"), dir_item) && (this.uiSG("the_command_data").toUpperCase().endsWith(".BIN") || this.uiSG("the_command_data").toUpperCase().endsWith(".ZIP"))) {
            this.uiPUT("the_command_data", this.uiSG("the_command_data").substring(0, this.uiSG("the_command_data").lastIndexOf(".")));
            the_dir = the_dir.substring(0, the_dir.lastIndexOf("."));
        }
        if (this.check_access_privs(the_dir, this.uiSG("the_command"), item) && the_dir.indexOf(":filetree") < 0) {
            this.changeProxyToCurrentDir(item);
            if (item != null && item.getProperty("type", "").equals("FILE")) {
                this.not_done = this.ftp_write_command("213", item.getProperty("size"));
                return "";
            }
            this.not_done = this.ftp_write_command("550", LOC.G("%SIZE-wrong%"));
            return "%SIZE-wrong%";
        }
        this.not_done = this.ftp_write_command("550", LOC.G("File not found, or access denied."));
        this.doErrorEvent(new Exception(this.uiSG("lastLog")));
        this.uiVG("failed_commands").addElement("" + new Date().getTime());
        return "%SIZE-bad%";
    }

    /*
     * Exception decompiling
     */
    public String do_MDTM() throws Exception {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [8[CATCHBLOCK]], but top level block is 4[TRYBLOCK]
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

    public String get_PWD() {
        try {
            return this.uiSG("current_dir").substring(this.SG("root_dir").length() - 1);
        }
        catch (Exception e) {
            return this.uiSG("current_dir");
        }
    }

    public String do_CWD() throws Exception {
        this.uiPUT("the_command", "CWD");
        this.uiPUT("last_logged_command", "CWD");
        if (this.uiSG("the_command_data").trim().equals("")) {
            this.uiPUT("the_command_data", ".");
        }
        String originalCommandData = this.uiSG("the_command_data");
        this.uiPUT("the_command_data", Common.url_decode(this.uiSG("the_command_data")));
        if (this.uiSG("the_command_data").startsWith("//")) {
            this.uiPUT("the_command_data", this.uiSG("the_command_data").substring(1));
        }
        if (this.uiSG("the_command_data").startsWith("//") && !this.uiSG("the_command_data").endsWith("/")) {
            this.uiPUT("the_command_data", String.valueOf(this.uiSG("the_command_data").substring(1)) + "/");
        }
        if (!this.uiSG("the_command_data").equals("~")) {
            String the_dir = this.uiSG("current_dir");
            the_dir = this.uiSG("the_command_data").startsWith("/") ? this.uiSG("the_command_data") : String.valueOf(the_dir) + this.uiSG("the_command_data");
            if (!the_dir.endsWith("/")) {
                the_dir = String.valueOf(the_dir) + "/";
            }
            if (the_dir.equals("/")) {
                the_dir = this.SG("root_dir");
            }
            if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.SG("root_dir").toUpperCase())) {
                the_dir = String.valueOf(this.SG("root_dir")) + the_dir.substring(1);
            }
            if (!(the_dir = com.crushftp.client.Common.dots(the_dir)).startsWith(this.SG("root_dir"))) {
                the_dir = String.valueOf(this.SG("root_dir")) + (the_dir.startsWith("/") ? the_dir.substring(1) : the_dir);
            }
            Properties item = null;
            item = System.getProperty("crushftp.ftp_cwd_validate", "true").equalsIgnoreCase("true") ? this.uVFS.get_item(the_dir) : this.uVFS.get_item_parent(the_dir);
            if (this.check_access_privs(the_dir, this.uiSG("the_command"), item)) {
                if (item == null && !the_dir.equals("/")) {
                    this.not_done = this.ftp_write_command("550", LOC.G("$0 : No such file or directory.", the_dir));
                    this.doErrorEvent(new Exception(this.uiSG("lastLog")));
                    this.uiVG("failed_commands").addElement("" + new Date().getTime());
                    if (ServerStatus.BG("slow_directory_scanners") && this.uiVG("failed_commands").size() - 10 > 0) {
                        Thread.sleep(100 * (this.uiVG("failed_commands").size() - 10));
                    }
                    return String.valueOf(the_dir) + ": " + LOC.G("No such file or directory.");
                }
                if (the_dir.equals("/")) {
                    this.uiPUT("current_dir", this.SG("root_dir"));
                    this.not_done = this.ftp_write_command("250", LOC.G("\"$0\" CWD command successful.", this.get_PWD()));
                    return "";
                }
                if (item.getProperty("type").equals("DIR") || item.getProperty("name").toLowerCase().endsWith(".zip") && originalCommandData.endsWith("/")) {
                    if (the_dir.equals("")) {
                        the_dir = this.SG("root_dir");
                    }
                    this.uiPUT("current_dir", the_dir);
                    this.not_done = ServerStatus.BG("generic_ftp_responses") ? this.ftp_write_command("250", LOC.G("Directory successfully changed.")) : this.ftp_write_command("250", LOC.G("\"$0\" CWD command successful.", this.get_PWD()));
                    if (!new VRL(item.getProperty("url")).getProtocol().equalsIgnoreCase("virtual")) {
                        GenericClient c = this.uVFS.getClient(item);
                        try {
                            if (c.getConfig("server_type", "").toUpperCase().indexOf("UNIX") < 0 && c.getConfig("server_type", "").toUpperCase().indexOf("WIND") < 0) {
                                c.doCommand("CWD " + originalCommandData);
                            }
                        }
                        finally {
                            c = this.uVFS.releaseClient(c);
                        }
                    }
                    return "";
                }
                this.not_done = this.ftp_write_command("550", "\"" + this.uiSG("the_command_data") + "\": " + LOC.G("No such file or directory."));
                this.doErrorEvent(new Exception(this.uiSG("lastLog")));
                this.uiVG("failed_commands").addElement("" + new Date().getTime());
                if (ServerStatus.BG("slow_directory_scanners") && this.uiVG("failed_commands").size() - 10 > 0) {
                    Thread.sleep(100 * (this.uiVG("failed_commands").size() - 10));
                }
                return "%CWD-not found%";
            }
            this.not_done = this.ftp_write_command("550", "\"" + this.uiSG("the_command_data") + "\": " + LOC.G("No such file or directory."));
            this.doErrorEvent(new Exception(this.uiSG("lastLog")));
            this.uiVG("failed_commands").addElement("" + new Date().getTime());
            if (ServerStatus.BG("slow_directory_scanners") && this.uiVG("failed_commands").size() - 10 > 0) {
                Thread.sleep(100 * (this.uiVG("failed_commands").size() - 10));
            }
            return "%CWD-not found%";
        }
        this.uiPUT("current_dir", Common.replace_str(String.valueOf(this.SG("root_dir")) + this.user_info.getProperty("default_current_dir", ""), "//", "/"));
        this.not_done = this.ftp_write_command("250", LOC.G("\"$0\" CWD command successful.", this.get_PWD()));
        return "";
    }

    public static Properties build_password_rules(Properties user) {
        if (user == null) {
            user = new Properties();
        }
        Properties password_rules = new Properties();
        password_rules.put("min_password_length", user.getProperty("min_password_length", ServerStatus.SG("min_password_length")));
        password_rules.put("min_password_numbers", user.getProperty("min_password_numbers", ServerStatus.SG("min_password_numbers")));
        password_rules.put("min_password_lowers", user.getProperty("min_password_lowers", ServerStatus.SG("min_password_lowers")));
        password_rules.put("min_password_uppers", user.getProperty("min_password_uppers", ServerStatus.SG("min_password_uppers")));
        password_rules.put("min_password_specials", user.getProperty("min_password_specials", ServerStatus.SG("min_password_specials")));
        password_rules.put("unsafe_password_chars", user.getProperty("unsafe_password_chars", ServerStatus.SG("unsafe_password_chars")));
        password_rules.put("password_history_count", user.getProperty("password_history_count", ServerStatus.SG("password_history_count")));
        password_rules.put("random_password_length", user.getProperty("random_password_length", ServerStatus.SG("random_password_length")));
        return password_rules;
    }

    public String do_ChangePass(String theUser, String new_password) {
        String result = String.valueOf(LOC.G("ERROR:")) + " " + LOC.G("Password not changed.");
        if (this.uiBG("no_password_change_on_user")) {
            result = String.valueOf(result) + " Not allowed.";
            return result;
        }
        Properties password_rules = SessionCrush.build_password_rules(this.user);
        if (!Common.checkPasswordRequirements(new_password, this.user.getProperty("password_history", ""), password_rules).equals("")) {
            return String.valueOf(LOC.G("ERROR:")) + " " + Common.checkPasswordRequirements(new_password, this.user.getProperty("password_history", ""), password_rules);
        }
        if (Common.checkPasswordBlacklisted(new_password)) {
            return String.valueOf(LOC.G("ERROR:")) + " password is blacklisted.";
        }
        boolean ok = false;
        if (!new_password.equals(this.uiSG("current_password"))) {
            String response;
            String old_password;
            block27: {
                old_password = this.uiSG("current_password");
                response = "";
                try {
                    Properties dir_item;
                    VRL vrl;
                    VFS realVfs = this.uVFS;
                    if (this.expired_uVFS != null) {
                        realVfs = this.expired_uVFS;
                    }
                    if (((vrl = new VRL((dir_item = realVfs.get_item(SessionCrush.getRootDir(null, realVfs, this.user, false), -1)).getProperty("url"))).getProtocol().equalsIgnoreCase("http") || vrl.getProtocol().equalsIgnoreCase("https") || vrl.getProtocol().equalsIgnoreCase("ftp")) && ServerStatus.BG("change_remote_password")) {
                        GenericClient c = realVfs.getClient(dir_item);
                        try {
                            if (c instanceof HTTPClient) {
                                String split = Common.makeBoundary();
                                response = c.doCommand("SITE PASS " + split + " " + old_password + split + new_password);
                            } else {
                                response = c.doCommand("SITE PASS " + new_password);
                                if (response.startsWith("2")) {
                                    response = c.doCommand("SITE PASS " + new_password);
                                }
                            }
                            if (response.startsWith("2")) {
                                ok = true;
                            }
                            break block27;
                        }
                        finally {
                            c = this.uVFS.releaseClient(c);
                        }
                    }
                    String salt = this.user.getProperty("salt", "");
                    if (this.user.getProperty("salt", "").equals("random")) {
                        salt = Common.makeBoundary(8);
                    }
                    if (this.server_item.getProperty("linkedServer", "").equals("@AutoDomain") && theUser.contains("@")) {
                        theUser = theUser.substring(0, theUser.lastIndexOf("@"));
                    }
                    UserTools.changeUsername(this.uiSG("listen_ip_port"), theUser, theUser, ServerStatus.thisObj.common_code.encode_pass(new_password, ServerStatus.SG("password_encryption"), salt));
                    Properties tempUser = UserTools.ut.getUser(this.uiSG("listen_ip_port"), theUser, false);
                    if (tempUser != null) {
                        if (tempUser.containsKey("expire_password_when")) {
                            GregorianCalendar gc = new GregorianCalendar();
                            gc.setTime(new Date());
                            ((Calendar)gc).add(5, this.IG("expire_password_days"));
                            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.US);
                            String s = sdf.format(gc.getTime());
                            tempUser.put("expire_password_when", s);
                            tempUser.put("expire_password_days", String.valueOf(this.IG("expire_password_days")));
                        }
                        tempUser.put("auto_set_pass", "false");
                        tempUser.put("password", ServerStatus.thisObj.common_code.encode_pass(new_password, ServerStatus.SG("password_encryption"), salt));
                        if (this.user.getProperty("salt", "").equals("random")) {
                            tempUser.put("salt", salt);
                        }
                        String history = Common.getPasswordHistory(old_password, tempUser.getProperty("password_history", ""), password_rules);
                        tempUser.put("password_history", Common.getPasswordHistory(new_password, history, password_rules));
                        if (!(this.user.getProperty("min_pass_age", "0").equals("0") || this.user.getProperty("min_pass_age", "0").equals("") || this.user.getProperty("min_pass_age", "0").equals("min_pass_age"))) {
                            tempUser.put("last_pass_change_time", String.valueOf(System.currentTimeMillis()));
                            long last_change = Long.parseLong(this.user.getProperty("last_pass_change_time", "0"));
                            if (Long.parseLong(this.user.getProperty("min_pass_age", "0")) * 3600L * 1000L + last_change > System.currentTimeMillis()) {
                                throw new Exception("Minimum password age restriction denied the password change!");
                            }
                        }
                        if (tempUser.containsKey("password_expire_advance_days_sent2") && tempUser.getProperty("password_expire_advance_days_sent2", "false").equals("true")) {
                            tempUser.put("password_expire_advance_days_sent2", "false");
                        }
                        if (!com.crushftp.client.Common.dmz_mode) {
                            UserTools.writeUser(this.uiSG("listen_ip_port"), theUser, tempUser);
                        }
                    }
                    ok = true;
                    response = "214 " + LOC.G("Password changed.");
                    Log.log("SERVER", 0, String.valueOf(theUser) + " password changed by user.");
                }
                catch (Exception e) {
                    Log.log("LOGIN", 0, e);
                    return e.getMessage();
                }
            }
            result = response.substring(4);
            if (ok) {
                this.uiPUT("current_password", new_password);
                Properties p = new Properties();
                p.put("user_name", theUser);
                p.put("old_password", old_password);
                p.put("new_password", new_password);
                this.runPlugin("changePass", p);
                ServerStatus.thisObj.runAlerts("password_change", this);
                Common.send_change_pass_email(this);
                try {
                    ServerStatus serverStatus = ServerStatus.thisObj;
                    long rid = serverStatus.statTools.u();
                    ServerStatus.thisObj.statTools.add_login_stat("CHANGE_PASS", theUser, this.uiSG("user_ip"), true, String.valueOf(this.uiSG("listen_ip_port")) + "_USER_INITIATED_" + Common.makeBoundary(), rid);
                    ServerStatus.thisObj.statTools.executeSql(ServerStatus.SG("stats_update_sessions"), new Object[]{new Date(), rid});
                }
                catch (Exception e) {
                    Log.log("SERVER", 0, e);
                }
            }
        }
        return result;
    }

    public void kill_active_socks() {
        while (this.data_socks.size() > 0) {
            try {
                ((Socket)this.data_socks.remove(0)).close();
            }
            catch (Exception e) {
                Log.log("FTP_SERVER", 1, e);
            }
        }
    }

    public String fixupDir(String user_dir) {
        String the_dir = "";
        if (user_dir == null) {
            the_dir = Common.url_decode(this.uiSG("current_dir"));
            if (!this.uiSG("the_command_data").equals("")) {
                the_dir = this.uiSG("the_command_data").startsWith("/") ? this.uiSG("the_command_data") : String.valueOf(the_dir) + this.uiSG("the_command_data");
                if ((the_dir = com.crushftp.client.Common.dots(the_dir)).equals("/")) {
                    the_dir = this.SG("root_dir");
                }
                if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.SG("root_dir").toUpperCase())) {
                    the_dir = String.valueOf(this.SG("root_dir")) + the_dir.substring(1);
                }
            }
            this.uiPUT("the_command_data", com.crushftp.client.Common.dots(this.uiSG("the_command_data")));
        } else {
            the_dir = com.crushftp.client.Common.dots(Common.url_decode(user_dir));
            if (the_dir.equals("/")) {
                the_dir = this.SG("root_dir");
            }
            if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.SG("root_dir").toUpperCase())) {
                the_dir = String.valueOf(this.SG("root_dir")) + the_dir.substring(1);
            }
        }
        return the_dir;
    }

    public String fixupUnsafeChars(String the_dir) {
        String the_dir_root = Common.all_but_last(the_dir);
        String last_item = Common.last(the_dir);
        boolean need_slash = last_item.endsWith("/");
        if (need_slash) {
            last_item = last_item.substring(0, last_item.length() - 1);
        }
        last_item = Common.normalize2(last_item);
        int x = 0;
        while (x < ServerStatus.SG("unsafe_filename_chars_rename").length()) {
            last_item = last_item.replace(ServerStatus.SG("unsafe_filename_chars_rename").charAt(x), '_');
            ++x;
        }
        x = 0;
        while (x < ServerStatus.SG("unsafe_filename_chars").length()) {
            last_item = last_item.replace(ServerStatus.SG("unsafe_filename_chars").charAt(x), '_');
            ++x;
        }
        return String.valueOf(the_dir_root) + last_item + (need_slash ? "/" : "");
    }

    public void setupRootDir(String domain, boolean reset) throws Exception {
        if (this.user != null) {
            if (ServerStatus.BG("jailproxy") && this.getProperty("default_current_dir_unlocked", "false").equals("false")) {
                this.user.put("root_dir", SessionCrush.getRootDir(domain, this.uVFS, this.user, reset));
            } else {
                if (!this.user_info.containsKey("configured_root_dir")) {
                    this.user_info.put("configured_root_dir", SessionCrush.getRootDir(domain, this.uVFS, this.user, reset, false));
                }
                this.user.put("root_dir", this.uiSG("configured_root_dir"));
                this.uiPUT("current_dir", String.valueOf(this.SG("root_dir")) + this.uVFS.user_info.getProperty("default_current_dir", "/").substring(1));
            }
            this.user_info.put("root_dir", this.user.getProperty("root_dir"));
        }
    }

    public static String getRootDir(String domain, VFS uVFS, Properties user, boolean reset) {
        return SessionCrush.getRootDir(domain, uVFS, user, reset, true);
    }

    public static String getRootDir(String domain, VFS uVFS, Properties user, boolean reset, boolean include_default_current_dir) {
        Properties p;
        if (uVFS == null) {
            return "/";
        }
        String root_dir = "/";
        Vector v = new Vector();
        try {
            uVFS.getListing(v, "/", ServerStatus.BG("scan_vfs_for_initial_listing"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (reset) {
            uVFS.reset();
        }
        Properties dir_item = null;
        Properties names = new Properties();
        int x = 0;
        while (x < v.size()) {
            p = (Properties)v.elementAt(x);
            if (p.getProperty("type").equalsIgnoreCase("DIR") && dir_item == null) {
                dir_item = p;
            }
            if (!names.containsKey(p.getProperty("name"))) {
                names.put(p.getProperty("name"), "");
            }
            ++x;
        }
        if (dir_item != null && names.size() == 1) {
            root_dir = "/" + dir_item.getProperty("name") + "/";
        }
        if (include_default_current_dir && !uVFS.user_info.getProperty("default_current_dir", "").equals("/") && !uVFS.user_info.getProperty("default_current_dir", "").equals("") && !(root_dir = String.valueOf(root_dir) + uVFS.user_info.getProperty("default_current_dir").substring(1)).endsWith("/")) {
            root_dir = String.valueOf(root_dir) + "/";
        }
        if (domain != null && !domain.equals("") && user != null && user.get("domain_root_list") != null) {
            v = (Vector)user.get("domain_root_list");
            x = 0;
            while (x < v.size()) {
                p = (Properties)v.elementAt(x);
                if (com.crushftp.client.Common.do_search(p.getProperty("domain"), domain, false, 0)) {
                    String path = p.getProperty("path");
                    if (!path.startsWith("/")) {
                        path = "/" + path;
                    }
                    if (!path.endsWith("/")) {
                        path = String.valueOf(path) + "/";
                    }
                    root_dir = path;
                    break;
                }
                ++x;
            }
        }
        return root_dir;
    }

    public int IG(String data) {
        int x = 0;
        try {
            x = Integer.parseInt(this.user.getProperty(data));
        }
        catch (Exception exception) {
            // empty catch block
        }
        return x;
    }

    public long LG(String data) {
        long x = 0L;
        try {
            x = Long.parseLong(this.user.getProperty(data));
        }
        catch (Exception exception) {
            // empty catch block
        }
        return x;
    }

    public double DG(String data) {
        double x = 0.0;
        try {
            x = Double.parseDouble(this.user.getProperty(data));
        }
        catch (Exception exception) {
            // empty catch block
        }
        return x;
    }

    public String SG(String data) {
        String return_data = null;
        if (this.user != null) {
            return_data = this.user.getProperty(data);
        }
        if (return_data == null) {
            if (data.equals("root_dir")) {
                return_data = "/";
            } else {
                try {
                    return_data = ServerStatus.SG(data);
                }
                catch (Exception e) {
                    return_data = "";
                }
            }
        }
        return return_data;
    }

    public boolean BG(String data) {
        boolean test = false;
        try {
            test = this.user.getProperty(data).equals("true");
        }
        catch (Exception exception) {
            // empty catch block
        }
        return test;
    }

    public void doFileAbortBlock(final String the_command_data, final boolean event) throws Exception {
        this.put("blockUploads", "true");
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                try {
                    Thread.sleep(2900L);
                    SessionCrush.this.put("blockUploads", "false");
                    SessionCrush.this.doFileAbortEvent(the_command_data, event);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        });
        this.doFileAbortEvent(the_command_data, event);
    }

    public void doFileAbortEvent(String the_command_data, boolean event) throws Exception {
        Log.log("EVENT", 0, "Processing abort event:" + event + " for:" + the_command_data);
        if (the_command_data.startsWith("ABOR") && event) {
            String filePath = the_command_data.substring(5).trim();
            if (!filePath.startsWith(this.SG("root_dir"))) {
                filePath = String.valueOf(this.SG("root_dir")) + (filePath.startsWith("/") ? filePath.substring(1) : filePath);
            }
            Log.log("EVENT", 0, "Processing abort event:" + event + " for filePath:" + filePath + " and uVFS:" + this.uVFS + " root_dir:" + this.SG("root_dir"));
            if (this.uVFS != null) {
                Properties fileItem = this.uVFS.get_item(filePath);
                Log.log("EVENT", 0, "Processing abort event:" + event + " for filePath:" + filePath + " and fileItem:" + VRL.safe(fileItem));
                if (fileItem != null) {
                    fileItem.put("mark_error", "true");
                    fileItem.put("the_file_error", "HTTP aborted");
                    fileItem.put("the_file_status", "FAILURE");
                    fileItem.put("the_file_path", the_command_data.substring(5).trim());
                    fileItem.put("the_file_size", fileItem.getProperty("size"));
                    fileItem.put("the_file_name", fileItem.getProperty("name"));
                    fileItem.put("the_file_start", String.valueOf(System.currentTimeMillis()));
                    fileItem.put("the_file_end", String.valueOf(System.currentTimeMillis()));
                    Log.log("EVENT", 0, "Processing abort event:" + event + " for filePath:" + filePath + " and modified fileItem:" + VRL.safe(fileItem));
                    Log.log("EVENT", 2, com.crushftp.client.Common.dumpStack(String.valueOf(ServerStatus.version_info_str) + ServerStatus.sub_version_info_str));
                    this.do_event5("UPLOAD", fileItem);
                }
            }
        }
    }

    public String stripRoot(String s) {
        if (s.toUpperCase().startsWith(this.SG("root_dir").toUpperCase())) {
            s = s.substring(this.SG("root_dir").length() - 1);
        }
        return s;
    }

    public String getStandardizedDir(String path) {
        if (path.startsWith("/WebInterface/function/")) {
            return "/";
        }
        if (!(path = com.crushftp.client.Common.dots(path)).toUpperCase().startsWith(this.SG("root_dir").toUpperCase())) {
            path = String.valueOf(this.SG("root_dir")) + (path.startsWith("/") ? path.substring(1) : path);
        }
        if (path.indexOf("\\") >= 0) {
            path = path.replace('\\', '/');
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }

    public void killSession() {
        if ((this.uiSG("user_protocol").startsWith("HTTP") || this.uiSG("user_protocol_actual").startsWith("HTTP")) && this.uiSG("CrushAuth").length() > 30) {
            try {
                Properties html5_transfers = ServerStatus.siPG("html5_transfers");
                String transfer_chunks = "";
                Enumeration<Object> keys = html5_transfers.keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement().toString();
                    if (!key.startsWith(this.getId())) continue;
                    transfer_chunks = String.valueOf(transfer_chunks) + key + "~";
                }
                if (!transfer_chunks.equals("")) {
                    String[] html_transfer = transfer_chunks.split("~");
                    int x = 0;
                    while (x < html_transfer.length) {
                        if (!html_transfer[x].equals("")) {
                            html5_transfers.remove(html_transfer[x]);
                        }
                        ++x;
                    }
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
            }
        }
        SharedSession.find("crushftp.usernames").remove(String.valueOf(Common.getPartialIp(this.uiSG("user_ip"))) + "_" + this.getId() + "_user");
        SharedSession.find("crushftp.sessions").remove(this.getId());
        this.uiPUT("CrushAuth", "");
        SharedSession.find("crushftp.usernames").remove(String.valueOf(Common.getPartialIp(this.uiSG("user_ip"))) + "_" + this.getId() + "_user");
        SharedSession.find("crushftp.usernames").remove("127.0.0.1_" + this.getId() + "_user");
        SharedSession.find("crushftp.usernames").remove(String.valueOf(Common.getPartialIp("127.0.0.1")) + "_" + this.getId() + "_user");
    }

    public boolean ftp_write_command(String code, String data) throws Exception {
        if (this.ftp != null) {
            return this.ftp.write_command(code, data);
        }
        data = ServerStatus.thisObj.change_vars_to_values(data, this);
        Properties p = new Properties();
        p.put("command_code", code);
        p.put("command_data", data);
        this.runPlugin("afterCommand", p);
        data = p.getProperty("command_data", data);
        code = p.getProperty("command_code", code);
        data = ServerStatus.thisObj.common_code.format_message(code, data).trim();
        this.uiPUT("lastLog", data);
        this.add_log("[" + this.server_item.getProperty("serverType", "ftp") + ":" + this.uiSG("user_number") + "_" + this.uiSG("sock_port") + ":" + this.uiSG("user_name") + ":" + this.uiSG("user_ip") + "] " + this.SG("WROTE") + ": *" + data + "*", "STOR");
        return true;
    }

    public boolean ftp_write_command_logged(String code, String data, String logged_command) throws Exception {
        if (this.ftp != null) {
            return this.ftp.write_command(code, data);
        }
        data = ServerStatus.thisObj.change_vars_to_values(data, this);
        Properties p = new Properties();
        p.put("command_code", code);
        p.put("command_data", data);
        this.runPlugin("afterCommand", p);
        data = p.getProperty("command_data", data);
        code = p.getProperty("command_code", code);
        data = ServerStatus.thisObj.common_code.format_message(code, data).trim();
        this.uiPUT("lastLog", data);
        this.add_log("[" + this.server_item.getProperty("serverType", "ftp") + ":" + this.uiSG("user_number") + "_" + this.uiSG("sock_port") + ":" + this.uiSG("user_name") + ":" + this.uiSG("user_ip") + "] " + this.SG("WROTE") + ": *" + data + "*", logged_command);
        return true;
    }

    public boolean ftp_write_command_raw(String data) throws Exception {
        if (this.ftp != null) {
            return this.ftp.write_command_raw(data);
        }
        this.add_log(data, "STOR");
        return true;
    }

    public boolean ftp_write_command(String data) throws Exception {
        if (this.ftp != null) {
            return this.ftp.write_command(data);
        }
        return true;
    }

    public boolean ftp_write_command_logged(String data, String logged_command) throws Exception {
        if (this.ftp != null) {
            return this.ftp.write_command(data);
        }
        this.add_log("[" + this.server_item.getProperty("serverType", "ftp") + ":" + this.uiSG("user_number") + "_" + this.uiSG("sock_port") + ":" + this.uiSG("user_name") + ":" + this.uiSG("user_ip") + "] " + this.SG("WROTE") + ": *" + ServerStatus.thisObj.change_vars_to_values(data, this) + "*", logged_command);
        return true;
    }

    public String getAdminGroupName(Properties request) {
        String groupName = this.SG("admin_group_name").trim();
        if (groupName.equals("")) {
            Properties groups = UserTools.getGroups(request.getProperty("serverGroup"));
            Enumeration<Object> keys = groups.keys();
            boolean found = false;
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                if (!key.equals(this.uiSG("user_name"))) continue;
                found = true;
                break;
            }
            if (found) {
                groupName = this.uiSG("user_name");
            } else {
                return "Limited Admin : Group name was not specified!";
            }
        }
        if (groupName.equals("admin_group_name")) {
            groupName = this.uiSG("user_name");
        }
        if (groupName.contains(", ")) {
            groupName = groupName.replaceAll(",\\s+", ",");
        }
        if (groupName.contains(" ,")) {
            groupName = groupName.replaceAll("\\s+,", ",");
        }
        if ((groupName = "," + groupName + ",").indexOf("," + request.getProperty("serverGroup_original").replace('+', ' ') + ",") >= 0) {
            groupName = request.getProperty("serverGroup_original").replace('+', ' ');
        }
        return groupName;
    }

    public void logLogin(boolean success, String msg) {
        Log.log("SERVER", 0, "SERVER_LOGIN:" + (success ? "SUCCESS" : "FAILURE") + ":" + this.uiSG("user_number") + ":" + this.uiSG("listen_ip_port") + ":" + this.uiSG("user_name") + ":" + this.uiSG("user_protocol") + ":" + this.uiSG("user_ip") + ":" + msg);
    }

    public String change_phone_number(String phone_number) {
        String result;
        block4: {
            result = "Success!";
            try {
                if (phone_number.length() > 200) {
                    throw new Exception("The given phone number is too long!");
                }
                if (this.user.getProperty("phone", "").equals(phone_number)) break block4;
                if (phone_number.matches("^[\\d+-]+$") || phone_number.equals("")) {
                    UserTools.ut.put_in_user(this.uiSG("listen_ip_port"), this.uiSG("user_name"), "phone", phone_number, false, false);
                    this.user.put("phone", phone_number);
                    break block4;
                }
                throw new Exception("The given phone number has an invalid format!");
            }
            catch (Exception e) {
                result = "Error : " + e;
            }
        }
        return result;
    }

    public static boolean is_url_part_of_search_index(String check_url) {
        boolean included = false;
        String[] usernames = ServerStatus.SG("search_index_usernames").split(",");
        FileClient.dirCachePermTemp = new Properties();
        int x = 0;
        while (x < usernames.length && !included) {
            if (!usernames[x].trim().equals("")) {
                Vector server_groups = (Vector)ServerStatus.server_settings.get("server_groups");
                int xx = 0;
                while (xx < server_groups.size() && !included) {
                    VFS temp_vfs = UserTools.ut.getVFS(server_groups.elementAt(xx).toString(), usernames[x].trim());
                    try {
                        Properties pp = temp_vfs.get_item("/");
                        if (check_url.toUpperCase().startsWith(pp.getProperty("url"))) {
                            included = true;
                        }
                    }
                    catch (Exception e) {
                        Log.log("SEARCH", 2, e);
                    }
                    temp_vfs.disconnect();
                    temp_vfs.free();
                    ++xx;
                }
            }
            ++x;
        }
        return included;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void doPaste(SessionCrush thisSession, StringBuffer status, String[] names, String destPath, String command) {
        try {
            String msg = "OK";
            int x = 0;
            while (x < names.length) {
                block63: {
                    String url1;
                    String src_item_path;
                    Properties item;
                    String the_dir1;
                    block66: {
                        block65: {
                            thisSession.active();
                            the_dir1 = names[x].trim();
                            if (the_dir1.startsWith(thisSession.SG("root_dir"))) {
                                the_dir1 = the_dir1.substring(thisSession.SG("root_dir").length() - 1);
                            }
                            if ((item = thisSession.uVFS.get_item(src_item_path = thisSession.getStandardizedDir(the_dir1))) != null) break block65;
                            msg = String.valueOf(msg) + "\r\nItem not found:" + names[x];
                            break block63;
                        }
                        if (!item.getProperty("type", "FILE").equalsIgnoreCase("DIR")) break block66;
                        boolean skip = false;
                        int xx = 0;
                        while (xx < x) {
                            String the_dir_tmp = names[xx].trim();
                            if (names[x].trim().startsWith(the_dir_tmp)) {
                                skip = true;
                            }
                            ++xx;
                        }
                        if (skip) break block63;
                    }
                    if (!(url1 = item.getProperty("url")).endsWith("/") && item.getProperty("type", "").equalsIgnoreCase("DIR")) {
                        url1 = String.valueOf(url1) + "/";
                    }
                    VRL vrl = new VRL(url1);
                    Properties stat = null;
                    GenericClient c = thisSession.uVFS.getClient(item);
                    try {
                        c.login(vrl.getUsername(), vrl.getPassword(), null);
                        stat = c.stat(vrl.getPath());
                    }
                    finally {
                        c = thisSession.uVFS.releaseClient(c);
                    }
                    boolean deleteAllowed = thisSession.check_access_privs(src_item_path, "DELE");
                    if (thisSession.check_access_privs(src_item_path, "RETR")) {
                        String dest_item_path;
                        Properties item2;
                        String the_dir2 = Common.url_decode(destPath);
                        if (the_dir2.startsWith(thisSession.SG("root_dir"))) {
                            the_dir2 = the_dir2.substring(thisSession.SG("root_dir").length() - 1);
                        }
                        if ((item2 = thisSession.uVFS.get_item(dest_item_path = thisSession.getStandardizedDir(the_dir2))) == null) {
                            throw new Exception("Wrong destination path: " + the_dir2);
                        }
                        VRL vrl2 = new VRL(String.valueOf(item2.getProperty("url")) + (item2.getProperty("url").endsWith("/") ? "" : "/"));
                        if (thisSession.check_access_privs(dest_item_path, "STOR")) {
                            String addon = "";
                            boolean ok = true;
                            if (new VRL(String.valueOf(vrl2.toString()) + vrl.getName() + (stat.getProperty("type").equalsIgnoreCase("DIR") ? "/" : "")).toString().startsWith(vrl.toString()) || Common.all_but_last(names[x].trim()).equals(destPath)) {
                                ok = false;
                                String s1 = new VRL(String.valueOf(vrl2.toString()) + vrl.getName() + (stat.getProperty("type").equalsIgnoreCase("DIR") ? "/" : "")).toString();
                                String s2 = vrl.toString();
                                while (s1.endsWith("/")) {
                                    s1 = s1.substring(0, s1.length() - 1);
                                }
                                while (s2.endsWith("/")) {
                                    s2 = s2.substring(0, s2.length() - 1);
                                }
                                if (s1.equals(s2)) {
                                    ok = true;
                                    addon = String.valueOf(addon) + "_copy_" + Common.makeBoundary(3);
                                } else {
                                    msg = String.valueOf(msg) + CRLF + LOC.G("Cannot copy item into itself.");
                                }
                            }
                            if (ok) {
                                CharSequence sub_path;
                                Serializable list;
                                thisSession.trackAndUpdateUploads(thisSession.uiVG("lastUploadStats"), vrl, vrl2, "RENAME");
                                SearchHandler.buildEntry(item, thisSession.uVFS, "delete", null);
                                the_dir2 = String.valueOf(the_dir2) + vrl.getName() + (stat.getProperty("type").equalsIgnoreCase("DIR") ? "/" : "");
                                String dest_url = String.valueOf(item2.getProperty("url")) + (item2.getProperty("url").endsWith("/") ? "" : "/") + Common.url_encode(vrl.getName()) + addon + (stat.getProperty("type").equalsIgnoreCase("DIR") ? "/" : "");
                                VRL vrl_dest = new VRL(dest_url);
                                GenericClient c1 = thisSession.uVFS.getClient(item);
                                c1.login(vrl.getUsername(), vrl.getPassword(), null);
                                Properties item_dest = (Properties)item2.clone();
                                item_dest.put("url", dest_url);
                                if (item_dest.getProperty("url").length() > ServerStatus.IG("max_url_length")) {
                                    throw new Exception("File url length too long:" + item_dest.getProperty("url").length() + " vs. " + ServerStatus.IG("max_url_length"));
                                }
                                GenericClient c2 = thisSession.uVFS.getClient(item_dest);
                                c2.login(vrl_dest.getUsername(), vrl_dest.getPassword(), null);
                                if (item.getProperty("type", "").equals("DIR") && !thisSession.check_access_privs(src_item_path, "MKD")) {
                                    if (c2.stat(vrl_dest.getPath()) == null) {
                                        msg = String.valueOf(msg) + CRLF + LOC.G("Cannot copy $0 because you don't have create folder permission here.", vrl.getName());
                                        throw new Exception("Error : Cannot create folder: " + vrl.getName() + " because you don't have create folder permission here.");
                                    }
                                    list = new Vector();
                                    Common.getAllSubFolders(vrl, c1, 0, status, (Vector)list);
                                    int xx = 0;
                                    while (xx < ((Vector)list).size()) {
                                        Properties sub_folder = (Properties)((Vector)list).get(xx);
                                        VRL sub_vrl = new VRL(sub_folder.getProperty("url"));
                                        sub_path = sub_vrl.getPath().substring(vrl.getPath().length());
                                        if (c2.stat(String.valueOf(vrl_dest.getPath()) + sub_path) == null) {
                                            msg = String.valueOf(msg) + CRLF + LOC.G("Cannot copy $0 because you don't have create folder permission here.", sub_vrl.getName());
                                            throw new Exception("Error : Cannot create folder: " + sub_vrl.getName() + " because you don't have create folder permission here.");
                                        }
                                        ++xx;
                                    }
                                }
                                list = status;
                                synchronized (list) {
                                    if (status.toString().equals("CANCELLED")) {
                                        throw new Exception("CANCELLED");
                                    }
                                }
                                Common.recurseCopy(vrl, vrl_dest, c1, c2, 0, true, status);
                                c1 = thisSession.uVFS.releaseClient(c1);
                                c2 = thisSession.uVFS.releaseClient(c2);
                                list = status;
                                synchronized (list) {
                                    if (status.toString().equals("CANCELLED")) {
                                        throw new Exception("CANCELLED");
                                    }
                                    status.setLength(0);
                                    status.append("Updating search references...");
                                }
                                if (SessionCrush.is_url_part_of_search_index(item2.getProperty("url"))) {
                                    SearchHandler.buildEntry(item2, thisSession.uVFS, "new", null);
                                }
                                if (!the_dir1.startsWith(thisSession.SG("root_dir"))) {
                                    the_dir1 = String.valueOf(thisSession.SG("root_dir")) + the_dir1.substring(1);
                                }
                                if (!the_dir2.startsWith(thisSession.SG("root_dir"))) {
                                    the_dir2 = String.valueOf(thisSession.SG("root_dir")) + the_dir2.substring(1);
                                }
                                String the_dir_index1 = SearchHandler.getPreviewPath(item.getProperty("url"), "1", 1);
                                String the_dir_index2 = SearchHandler.getPreviewPath(item_dest.getProperty("url"), "1", 1);
                                String index1 = String.valueOf(ServerStatus.SG("previews_path")) + the_dir_index1.substring(1);
                                String index2 = String.valueOf(ServerStatus.SG("previews_path")) + the_dir_index2.substring(1);
                                if (new File_U(String.valueOf(Common.all_but_last(index1)) + "../index.txt").exists()) {
                                    new File_U(Common.all_but_last(index2)).mkdirs();
                                    Common.copy_U(String.valueOf(Common.all_but_last(index1)) + "../index.txt", String.valueOf(Common.all_but_last(index2)) + "../index.txt", true);
                                }
                                if (command.equalsIgnoreCase("cut_paste")) {
                                    if (deleteAllowed) {
                                        sub_path = status;
                                        synchronized (sub_path) {
                                            if (status.toString().equals("CANCELLED")) {
                                                throw new Exception("CANCELLED");
                                            }
                                            status.setLength(0);
                                            status.append("Removing original...");
                                        }
                                        c1 = thisSession.uVFS.getClient(item);
                                        c1.login(vrl.getUsername(), vrl.getPassword(), null);
                                        Common.recurseDelete(vrl, false, c1, 0);
                                        c1 = thisSession.uVFS.releaseClient(c2);
                                        Common.trackSync("RENAME", the_dir1, the_dir2, false, 0L, 0L, thisSession.SG("root_dir"), item.getProperty("privs"), thisSession.uiSG("clientid"), "");
                                    } else {
                                        msg = String.valueOf(msg) + CRLF + LOC.G("Item $0 copied, but not 'cut' as you did not have delete permissions.", vrl.getName());
                                        Common.trackSync("CHANGE", the_dir2, null, true, 0L, 0L, thisSession.SG("root_dir"), item.getProperty("privs"), thisSession.uiSG("clientid"), "");
                                    }
                                } else {
                                    Common.trackSync("CHANGE", the_dir2, null, true, 0L, 0L, thisSession.SG("root_dir"), item.getProperty("privs"), thisSession.uiSG("clientid"), "");
                                }
                                thisSession.active();
                                try {
                                    sub_path = status;
                                    synchronized (sub_path) {
                                        if (status.toString().equals("CANCELLED")) {
                                            throw new Exception("CANCELLED");
                                        }
                                        status.setLength(0);
                                        status.append("Generating event for copy/paste...");
                                    }
                                    Properties fileItem1 = item;
                                    fileItem1 = (Properties)fileItem1.clone();
                                    Log.log("FTP_SERVER", 2, String.valueOf(LOC.G("Tracking rename:")) + the_dir2);
                                    fileItem1.put("the_command", "RNTO");
                                    fileItem1.put("the_command_data", the_dir2);
                                    fileItem1.put("the_file_path2", stat.getProperty("root_dir", ""));
                                    fileItem1.put("url_2", stat.getProperty("url", ""));
                                    fileItem1.put("the_file_name_2", stat.getProperty("name"));
                                    fileItem1.put("the_file_path", the_dir2);
                                    fileItem1.put("the_file_name", item.getProperty("name"));
                                    fileItem1.put("the_file_size", stat.getProperty("size", "0"));
                                    fileItem1.put("the_file_speed", "0");
                                    fileItem1.put("the_file_start", String.valueOf(new Date().getTime()));
                                    fileItem1.put("the_file_end", String.valueOf(new Date().getTime()));
                                    fileItem1.put("the_file_error", "");
                                    fileItem1.put("the_file_status", "SUCCESS");
                                    Properties fileItem2 = (Properties)fileItem1.clone();
                                    fileItem2.put("url", fileItem2.getProperty("url_2"));
                                    fileItem2.put("the_file_name", fileItem2.getProperty("the_file_name_2"));
                                    Properties temp_rename = (Properties)fileItem1.clone();
                                    temp_rename.put("the_file_name", String.valueOf(temp_rename.getProperty("the_file_name_2")) + ":" + temp_rename.getProperty("the_file_name"));
                                    temp_rename.put("the_file_path", String.valueOf(temp_rename.getProperty("the_file_path2")) + ":" + temp_rename.getProperty("the_file_path"));
                                    temp_rename.put("url", String.valueOf(temp_rename.getProperty("url_2")) + ":" + temp_rename.getProperty("url"));
                                    ServerStatus.thisObj.statTools.add_item_stat(thisSession, temp_rename, "RENAME");
                                    thisSession.do_event5("RENAME", fileItem1, fileItem2);
                                    if (command.equalsIgnoreCase("cut_paste")) {
                                        Properties moveItem1 = (Properties)item.clone();
                                        moveItem1.put("the_command", "RNTO");
                                        moveItem1.put("the_command_data", the_dir2);
                                        moveItem1.put("the_file_path2", stat.getProperty("root_dir", ""));
                                        moveItem1.put("url_2", stat.getProperty("url", ""));
                                        moveItem1.put("the_file_name_2", stat.getProperty("name"));
                                        moveItem1.put("the_file_path", the_dir2);
                                        moveItem1.put("the_file_name", item.getProperty("name"));
                                        moveItem1.put("the_file_size", stat.getProperty("size", "0"));
                                        moveItem1.put("the_file_speed", "0");
                                        moveItem1.put("the_file_start", String.valueOf(new Date().getTime()));
                                        moveItem1.put("the_file_end", String.valueOf(new Date().getTime()));
                                        moveItem1.put("the_file_error", "");
                                        moveItem1.put("the_file_status", "SUCCESS");
                                        Properties temp_move = (Properties)moveItem1.clone();
                                        temp_move.put("the_file_name", String.valueOf(temp_move.getProperty("the_file_name_2")) + ":" + temp_move.getProperty("the_file_name"));
                                        temp_move.put("the_file_path", String.valueOf(temp_move.getProperty("the_file_path2")) + ":" + temp_move.getProperty("the_file_path"));
                                        temp_move.put("url", String.valueOf(temp_move.getProperty("url_2")) + ":" + temp_move.getProperty("url"));
                                        ServerStatus.thisObj.statTools.add_item_stat(thisSession, temp_move, "MOVE");
                                    }
                                }
                                catch (Exception e) {
                                    Log.log("SERVER", 1, e);
                                }
                            }
                        } else {
                            msg = String.valueOf(msg) + CRLF + LOC.G("Cannot copy $0 because you don't have write permission here.", vrl.getName());
                        }
                    } else {
                        msg = String.valueOf(msg) + CRLF + LOC.G("Cannot copy $0 because you don't have read permission here.", vrl.getName());
                    }
                }
                ++x;
            }
            msg = msg.equals("OK") ? "COMPLETED:OK" : "ERROR:" + msg;
            StringBuffer stringBuffer = status;
            synchronized (stringBuffer) {
                status.setLength(0);
                status.append(msg);
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
            StringBuffer stringBuffer = status;
            synchronized (stringBuffer) {
                status.setLength(0);
                status.append("ERROR:" + e);
            }
        }
    }

    public void start_idle_timer() throws Exception {
        if (this.uiBG("user_logged_in")) {
            this.stop_idle_timer();
            this.start_idle_timer(this.IG("max_idle_time"));
        }
    }

    public void start_idle_timer(int timeout) throws Exception {
        block3: {
            try {
                if (this.uiBG("user_logged_in")) {
                    this.active();
                    this.thread_killer_item.timeout = timeout;
                    this.thread_killer_item.last_activity = System.currentTimeMillis();
                    this.thread_killer_item.enabled = timeout != 0;
                }
            }
            catch (Exception e) {
                if (("" + e).indexOf("Interrupted") < 0) break block3;
                throw e;
            }
        }
    }

    public void stop_idle_timer() throws Exception {
        block2: {
            try {
                this.thread_killer_item.enabled = false;
            }
            catch (Exception e) {
                if (("" + e).indexOf("Interrupted") < 0) break block2;
                throw e;
            }
        }
    }
}

