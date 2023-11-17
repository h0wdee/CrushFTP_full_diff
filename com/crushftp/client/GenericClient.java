/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jdom.Element
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.FileClient;
import com.crushftp.client.HADownload;
import com.crushftp.client.HAUpload;
import com.crushftp.client.ICAPProxyClient;
import com.crushftp.client.InputStreamCloser;
import com.crushftp.client.OutputStreamCloser;
import com.crushftp.client.S3CrushClient;
import com.crushftp.client.SFTPClient;
import com.crushftp.client.URLConnection;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import com.crushftp.tunnel2.Tunnel2;
import com.didisoft.pgp.KeyStore;
import com.didisoft.pgp.PGPLib;
import com.didisoft.pgp.SignatureCheckResult;
import com.didisoft.pgp.inspect.PGPInspectLib;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import org.jdom.Element;

public class GenericClient {
    GenericClient thisObj = null;
    InputStream in = null;
    OutputStream out = null;
    InputStream in3 = null;
    OutputStream out3 = null;
    String url = null;
    Properties config = new Properties();
    Properties transfer_info = new Properties();
    public static final String[] months = new String[]{"not zero based", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    public final SimpleDateFormat yyyymmddHHmm = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
    public final SimpleDateFormat rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    public final SimpleDateFormat mm = new SimpleDateFormat("MM", Locale.US);
    public final SimpleDateFormat mmm = new SimpleDateFormat("MMM", Locale.US);
    public final SimpleDateFormat dd = new SimpleDateFormat("dd", Locale.US);
    public final SimpleDateFormat yyyy = new SimpleDateFormat("yyyy", Locale.US);
    public final SimpleDateFormat hhmm = new SimpleDateFormat("HH:mm", Locale.US);
    static Common common_code = null;
    PGPLib pgp = null;
    Vector logQueue = null;
    String logHeader = "";
    Properties statCache = null;
    public static Object tunnelLock = new Object();
    public static Object socketCleanerLock = new Object();
    static Thread socketCleaner = null;
    int max_cache_time = 60000;
    int max_cache_list_count = 10000;
    StringBuffer last_md5_buf = new StringBuffer();
    String[] fields = new String[]{"*"};
    static final String[] common_fields = new String[]{"url", "link", "root_dir", "protocol", "num_items", "type", "permissions", "size", "name", "path", "owner", "group", "month", "time_or_year", "day", "linkedFile", "permissionsNum", "created", "modified", "pgpDecryptUpload", "pgpPrivateKeyUploadPath", "pgpPrivateKeyUploadPassword", "pgpEncryptUpload", "pgpAsciiUpload", "pgpPublicKeyUploadPath", "pgpDecryptDownload", "pgpPrivateKeyDownloadPath", "pgpPrivateKeyDownloadPassword", "pgpEncryptDownload", "pgpAsciiDownload", "pgpPublicKeyDownloadPath", "pgpDecryptVerify", "syncName", "syncRevisionsPath", "syncUploadOnly"};
    public static Vector ftp_client_sockets = new Vector();

    public GenericClient(String logHeader, Vector logQueue) {
        this.thisObj = this;
        this.logHeader = logHeader;
        this.logQueue = logQueue;
        this.startSocketCleaner();
    }

    public String log(String s) {
        SimpleDateFormat logDateFormat = new SimpleDateFormat(System.getProperty("crushftp.log_date_format", "MM/dd/yyyy hh:mm:ss aa"), Locale.US);
        String time = String.valueOf(logDateFormat.format(new Date())) + "|";
        String s2 = String.valueOf(time) + Common.last(this.getClass().getName().replace('.', '/')) + ":" + this.logHeader + s;
        if (this.logQueue != null) {
            this.logQueue.addElement(String.valueOf(time) + Common.last(this.getClass().getName().replace('.', '/')) + ":" + this.logHeader + s);
        } else {
            System.out.print(String.valueOf(time) + Common.last(this.getClass().getName().replace('.', '/')) + ":" + new Date() + ":" + this.logHeader + s + "\r\n");
        }
        return s2;
    }

    public void log(String tag, int level, String log) {
        if (Common.log(tag, level, "")) {
            Common.log(tag, level, log);
        }
        if (this.logQueue != null && this.logHeader.equals("CrushTask7")) {
            SimpleDateFormat logDateFormat = new SimpleDateFormat(System.getProperty("crushftp.log_date_format", "MM/dd/yyyy hh:mm:ss aa"), Locale.US);
            String time = String.valueOf(logDateFormat.format(new Date())) + "|";
            this.logQueue.addElement("~" + time + Common.last(this.getClass().getName().replace('.', '/')) + ":" + this.logHeader + log);
        }
    }

    public void log(String tag, int level, Exception e) {
        Common.log(tag, level, e);
        if (this.logQueue != null && this.logHeader.equals("CrushTask7")) {
            SimpleDateFormat logDateFormat = new SimpleDateFormat(System.getProperty("crushftp.log_date_format", "MM/dd/yyyy hh:mm:ss aa"), Locale.US);
            String time = String.valueOf(logDateFormat.format(new Date())) + "|";
            this.logQueue.addElement(String.valueOf(time) + Common.last(this.getClass().getName().replace('.', '/')) + ":" + this.logHeader + e + "\r\n");
            StackTraceElement[] ste = e.getStackTrace();
            int x = 0;
            while (x < ste.length) {
                this.logQueue.addElement("~" + time + Common.last(this.getClass().getName().replace('.', '/')) + ":" + this.logHeader + ste[x].getClassName() + "." + ste[x].getMethodName() + ":" + ste[x].getLineNumber() + "\r\n");
                ++x;
            }
        }
    }

    public void setCache(Properties statCache) {
        this.statCache = statCache;
        if (statCache != null) {
            this.max_cache_time = 1000 * Integer.parseInt(statCache.getProperty("max_time", "60"));
            this.max_cache_list_count = Integer.parseInt(statCache.getProperty("max_items", "10000"));
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Properties getCache() {
        return this.statCache;
    }

    public String log(Throwable e) {
        String s2 = String.valueOf(this.log(Thread.currentThread().getName())) + "\r\n";
        s2 = String.valueOf(s2) + this.log("" + e) + "\r\n";
        StackTraceElement[] ste = e.getStackTrace();
        int x = 0;
        while (x < ste.length) {
            s2 = String.valueOf(s2) + this.log(String.valueOf(ste[x].getClassName()) + "." + ste[x].getMethodName() + ":" + ste[x].getLineNumber()) + "\r\n";
            ++x;
        }
        return s2;
    }

    public String log(Exception e) {
        String s2 = String.valueOf(this.log(Thread.currentThread().getName())) + "\r\n";
        s2 = String.valueOf(s2) + this.log("" + e) + "\r\n";
        StackTraceElement[] ste = e.getStackTrace();
        int x = 0;
        while (x < ste.length) {
            s2 = String.valueOf(s2) + this.log(String.valueOf(ste[x].getClassName()) + "." + ste[x].getMethodName() + ":" + ste[x].getLineNumber()) + "\r\n";
            ++x;
        }
        return s2;
    }

    public String login(String username, String password, String clientid) throws Exception {
        if (password.startsWith("DES:")) {
            try {
                password = Common.encryptDecrypt(password.substring(4), false);
            }
            catch (Exception e) {
                password = Common.encryptDecrypt(password.replace('\\', '/').substring(4), false);
            }
        }
        if (this.config.getProperty("timeout", "").equals("") || this.config.getProperty("timeout", "").equals("60")) {
            this.config.put("timeout", "200000");
        }
        if (this.logQueue != null && this.logHeader.equals("CrushTask7")) {
            this.config.put("http_log", this.logQueue);
            this.config.put("http_log_header", String.valueOf(Common.last(this.getClass().getName().replace('.', '/'))) + ":" + this.logHeader);
        }
        return this.login2(username, password, clientid);
    }

    public String login2(String username, String password, String clientid) throws Exception {
        return "";
    }

    public void setupConfig(Properties prefs, Properties item) {
        Enumeration<Object> keys = prefs.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            if (key.startsWith("config_")) {
                this.setConfig(key.substring("config_".length()), prefs.get(key));
                continue;
            }
            if (this.config.containsKey(key)) continue;
            this.setConfig(key, prefs.get(key));
        }
        if (!prefs.getProperty("keystore_path", "").equals("")) {
            this.setConfig("keystore_path", prefs.getProperty("keystore_path", ""));
        }
        if (!prefs.getProperty("trustore_path", "").equals("")) {
            this.setConfig("trustore_path", prefs.getProperty("trustore_path", ""));
        }
        if (!prefs.getProperty("keystore_pass", "").equals("")) {
            this.setConfig("keystore_pass", prefs.getProperty("keystore_pass", ""));
        }
        if (!prefs.getProperty("key_pass", "").equals("")) {
            this.setConfig("key_pass", prefs.getProperty("key_pass", ""));
        }
        if (!prefs.getProperty("acceptAnyCert", "").equals("")) {
            this.setConfig("acceptAnyCert", prefs.getProperty("acceptAnyCert", "false"));
        }
        if (!prefs.getProperty("ssh_private_key", "").equals("")) {
            this.setConfig("ssh_private_key", prefs.getProperty("ssh_private_key", ""));
        }
        if (!prefs.getProperty("ssh_private_key_pass", "").equals("")) {
            this.setConfig("ssh_private_key_pass", prefs.getProperty("ssh_private_key_pass", ""));
        }
        if (!prefs.getProperty("ssh_two_factor", "").equals("")) {
            this.setConfig("ssh_two_factor", prefs.getProperty("ssh_two_factor", ""));
        }
        if (item != null) {
            if (item.containsKey("vItem") && item.get("vItem") != null && item.get("vItem") instanceof Properties) {
                this.setupConfig(prefs, (Properties)item.get("vItem"));
            }
            this.setConfig("use_dmz", item.getProperty("use_dmz", "false"));
            this.setConfig("pasv", item.getProperty("pasv", "true"));
            this.setConfig("ascii", item.getProperty("ascii", "false"));
            this.setConfig("no_os400", item.getProperty("no_os400", "false"));
            this.setConfig("simple", item.getProperty("simple", "false"));
            this.setConfig("cwd_list", item.getProperty("cwd_list", "false"));
            this.setConfig("no_stat", item.getProperty("no_stat", "false"));
            this.setConfig("secure_data", item.getProperty("secure_data", "true"));
            this.setConfig("server_side_encrypt", item.getProperty("server_side_encrypt", "false"));
            this.setConfig("s3_accelerate", item.getProperty("s3_accelerate", "false"));
            this.setConfig("s3_bucket_in_path", item.getProperty("s3_bucket_in_path", "false"));
            this.setConfig("s3_stat_head_calls_double", item.getProperty("s3_stat_head_calls_double", "false"));
            this.setConfig("s3_stat_head_calls", item.getProperty("s3_stat_head_calls", "true"));
            this.setConfig("multithreaded_s3", item.getProperty("multithreaded_s3", "false"));
            this.setConfig("multithreaded_s3_download", item.getProperty("multithreaded_s3_download", "false"));
            this.setConfig("s3_max_buffer_download", item.getProperty("s3_max_buffer_download", "100"));
            this.setConfig("s3_buffer_download", item.getProperty("s3_buffer_download", "10"));
            this.setConfig("s3_threads_download", item.getProperty("s3_threads_download", "3"));
            this.setConfig("server_side_encrypt_kms", item.getProperty("server_side_encrypt_kms", ""));
            if (!item.getProperty("s3_sha256", "").equals("")) {
                this.setConfig("s3_sha256", item.getProperty("s3_sha256", ""));
            }
            if (!item.getProperty("s3_acl", "").equals("")) {
                this.setConfig("s3_acl", item.getProperty("s3_acl", ""));
            }
            if (!item.getProperty("s3_storage_class", "").equals("")) {
                this.setConfig("s3_storage_class", item.getProperty("s3_storage_class", ""));
            }
            if (!item.getProperty("s3_partial", "").equals("")) {
                this.setConfig("s3_partial", item.getProperty("s3_partial", ""));
            }
            if (!item.getProperty("s3_list_v2", "").equals("")) {
                this.setConfig("s3_list_v2", item.getProperty("s3_list_v2", ""));
            }
            if (!item.getProperty("s3_rename_allowed_speed", "").equals("")) {
                this.setConfig("s3_rename_allowed_speed", item.getProperty("s3_rename_allowed_speed", ""));
            }
            if (!item.getProperty("s3_sha256_request_header", "").equals("")) {
                this.setConfig("s3_sha256_request_header", item.getProperty("s3_sha256_request_header", ""));
            }
            if (!item.getProperty("before_login_script", "").equals("")) {
                this.setConfig("before_login_script", item.getProperty("before_login_script", ""));
            }
            if (!item.getProperty("after_login_script", "").equals("")) {
                this.setConfig("after_login_script", item.getProperty("after_login_script", ""));
            }
            if (!item.getProperty("before_logout_script", "").equals("")) {
                this.setConfig("before_logout_script", item.getProperty("before_logout_script", ""));
            }
            if (!item.getProperty("before_download_script", "").equals("")) {
                this.setConfig("before_download_script", item.getProperty("before_download_script", ""));
            }
            if (!item.getProperty("after_download_script", "").equals("")) {
                this.setConfig("after_download_script", item.getProperty("after_download_script", ""));
            }
            if (!item.getProperty("before_upload_script", "").equals("")) {
                this.setConfig("before_upload_script", item.getProperty("before_upload_script", ""));
            }
            if (!item.getProperty("after_upload_script", "").equals("")) {
                this.setConfig("after_upload_script", item.getProperty("after_upload_script", ""));
            }
            if (!item.getProperty("before_dir_script", "").equals("")) {
                this.setConfig("before_dir_script", item.getProperty("before_dir_script", ""));
            }
            if (!item.getProperty("after_dir_script", "").equals("")) {
                this.setConfig("after_dir_script", item.getProperty("after_dir_script", ""));
            }
            if (!item.getProperty("custom_setKeyReExchangeDisabled", "").equals("")) {
                this.setConfig("custom_setKeyReExchangeDisabled", item.getProperty("custom_setKeyReExchangeDisabled", ""));
            }
            if (!item.getProperty("custom_setIdleConnectionTimeoutSeconds", "").equals("")) {
                this.setConfig("custom_setIdleConnectionTimeoutSeconds", item.getProperty("custom_setIdleConnectionTimeoutSeconds", ""));
            }
            if (!item.getProperty("custom_enableCompression", "").equals("")) {
                this.setConfig("custom_enableCompression", item.getProperty("custom_enableCompression", ""));
            }
            if (!item.getProperty("custom_enableFIPSMode", "").equals("")) {
                this.setConfig("custom_enableFIPSMode", item.getProperty("custom_enableFIPSMode", ""));
            }
            if (!item.getProperty("custom_setDHGroupExchangeBackwardsCompatible", "").equals("")) {
                this.setConfig("custom_setDHGroupExchangeBackwardsCompatible", item.getProperty("custom_setDHGroupExchangeBackwardsCompatible", ""));
            }
            if (!item.getProperty("custom_preferredCipher", "").equals("")) {
                this.setConfig("custom_preferredCipher", item.getProperty("custom_preferredCipher", ""));
            }
            if (!item.getProperty("custom_setDHGroupExchangeKeySize", "").equals("")) {
                this.setConfig("custom_setDHGroupExchangeKeySize", item.getProperty("custom_setDHGroupExchangeKeySize", ""));
            }
            if (!item.getProperty("custom_preferredKex", "").equals("")) {
                this.setConfig("custom_preferredKex", item.getProperty("custom_preferredKex", ""));
            }
            if (!item.getProperty("custom_setUseRSAKey", "").equals("")) {
                this.setConfig("custom_setUseRSAKey", item.getProperty("custom_setUseRSAKey", ""));
            }
            if (!item.getProperty("custom_preferredMac", "").equals("")) {
                this.setConfig("custom_preferredMac", item.getProperty("custom_preferredMac", ""));
            }
            if (!item.getProperty("custom_setMaximumPacketLength", "").equals("")) {
                this.setConfig("custom_setMaximumPacketLength", item.getProperty("custom_setMaximumPacketLength", ""));
            }
            if (!item.getProperty("custom_setSessionMaxPacketSize", "").equals("")) {
                this.setConfig("custom_setSessionMaxPacketSize", item.getProperty("custom_setSessionMaxPacketSize", ""));
            }
            if (!item.getProperty("custom_setSessionMaxWindowSpace", "").equals("")) {
                this.setConfig("custom_setSessionMaxWindowSpace", item.getProperty("custom_setSessionMaxWindowSpace", ""));
            }
            if (!item.getProperty("custom_enableETM", "").equals("")) {
                this.setConfig("custom_enableETM", item.getProperty("custom_enableETM", ""));
            }
            if (!item.getProperty("custom_enableNonStandardAlgorithms", "").equals("")) {
                this.setConfig("custom_enableNonStandardAlgorithms", item.getProperty("custom_enableNonStandardAlgorithms", ""));
            }
            if (!item.getProperty("custom_charEncoding", "").equals("")) {
                this.setConfig("custom_charEncoding", item.getProperty("custom_charEncoding", ""));
            }
            if (!item.getProperty("delete_xml_representation_files", "").equals("")) {
                this.setConfig("delete_xml_representation_files", item.getProperty("delete_xml_representation_files", ""));
            }
            if (!item.getProperty("no_bucket_check", "").equals("")) {
                this.setConfig("no_bucket_check", item.getProperty("no_bucket_check", "false"));
            }
            if (!item.getProperty("dfs_enabled", "").equals("")) {
                this.setConfig("dfs_enabled", item.getProperty("dfs_enabled", ""));
            }
            if (!item.getProperty("write_timeout", "").equals("")) {
                this.setConfig("write_timeout", item.getProperty("write_timeout", ""));
            }
            if (!item.getProperty("read_timeout", "").equals("")) {
                this.setConfig("read_timeout", item.getProperty("read_timeout", ""));
            }
            if (!item.getProperty("smb3_required_signing", "").equals("")) {
                this.setConfig("smb3_required_signing", item.getProperty("smb3_required_signing", ""));
            }
            if (!item.getProperty("sas_token", "").equals("")) {
                this.setConfig("sas_token", item.getProperty("sas_token", ""));
            }
            if (!item.getProperty("upload_blob_type", "").equals("")) {
                this.setConfig("upload_blob_type", item.getProperty("upload_blob_type", ""));
            }
            if (!item.getProperty("block_blob_upload_buffer_size", "").equals("")) {
                this.setConfig("block_blob_upload_buffer_size", item.getProperty("block_blob_upload_buffer_size", ""));
            }
            if (!item.getProperty("block_blob_upload_threads", "").equals("")) {
                this.setConfig("block_blob_upload_threads", item.getProperty("block_blob_upload_threads", ""));
            }
            if (!item.getProperty("team_drive", "").equals("")) {
                this.setConfig("team_drive", item.getProperty("team_drive", ""));
            }
            if (!item.getProperty("gstorage_with_s3_api", "").equals("")) {
                this.setConfig("gstorage_with_s3_api", item.getProperty("gstorage_with_s3_api", ""));
            }
            if (!item.getProperty("onedriveTenant", "").equals("")) {
                this.setConfig("onedriveTenant", item.getProperty("onedriveTenant", ""));
            }
            if (!item.getProperty("onedrive_user_id", "").equals("")) {
                this.setConfig("onedrive_user_id", item.getProperty("onedrive_user_id", ""));
            }
            if (!item.getProperty("onedrive_my_shares", "").equals("")) {
                this.setConfig("onedrive_my_shares", item.getProperty("onedrive_my_shares", ""));
            }
            if (!item.getProperty("onedrive_share_name", "").equals("")) {
                this.setConfig("onedrive_share_name", item.getProperty("onedrive_share_name", ""));
            }
            if (!item.getProperty("one_drive_conflict_behaviour", "").equals("")) {
                this.setConfig("one_drive_conflict_behaviour", item.getProperty("one_drive_conflict_behaviour", ""));
            }
            if (!item.getProperty("sharepoint_site_id", "").equals("")) {
                this.setConfig("sharepoint_site_id", item.getProperty("sharepoint_site_id", ""));
            }
            if (!item.getProperty("sharepoint_site_path", "").equals("")) {
                this.setConfig("sharepoint_site_path", item.getProperty("sharepoint_site_path", ""));
            }
            if (!item.getProperty("sharepoint_site_drive_name", "").equals("")) {
                this.setConfig("sharepoint_site_drive_name", item.getProperty("sharepoint_site_drive_name", ""));
            }
            if (!item.getProperty("login_config", "").equals("")) {
                this.setConfig("login_config", item.getProperty("login_config", ""));
            }
            if (!item.getProperty("box_client_id", "").equals("")) {
                this.setConfig("box_client_id", item.getProperty("box_client_id", ""));
            }
            if (!item.getProperty("box_enterprise_id", "").equals("")) {
                this.setConfig("box_enterprise_id", item.getProperty("box_enterprise_id", ""));
            }
            if (!item.getProperty("box_public_key_id", "").equals("")) {
                this.setConfig("box_public_key_id", item.getProperty("box_public_key_id", ""));
            }
            if (!item.getProperty("box_private_key", "").equals("")) {
                this.setConfig("box_private_key", item.getProperty("box_private_key", ""));
            }
            if (!item.getProperty("box_private_pass_phrase", "").equals("")) {
                this.setConfig("box_private_pass_phrase", item.getProperty("box_private_pass_phrase", ""));
            }
            if (!item.getProperty("box_store_jwt_json", "").equals("")) {
                this.setConfig("box_store_jwt_json", item.getProperty("box_private_pass_phrase", "false"));
            }
            if (!item.getProperty("box_jwt_config_content", "").equals("")) {
                this.setConfig("box_jwt_config_content", item.getProperty("box_jwt_config_content", ""));
            }
        } else {
            if (prefs.containsKey("vItem") && prefs.get("vItem") != null && prefs.get("vItem") instanceof Properties) {
                this.setupConfig(prefs, (Properties)prefs.get("vItem"));
            }
            if (!prefs.getProperty("s3_sha256", "").equals("")) {
                this.setConfig("s3_sha256", prefs.getProperty("s3_sha256", ""));
            }
            if (!prefs.getProperty("s3_acl", "").equals("")) {
                this.setConfig("s3_acl", prefs.getProperty("s3_acl", ""));
            }
            if (!prefs.getProperty("s3_storage_class", "").equals("")) {
                this.setConfig("s3_storage_class", prefs.getProperty("s3_storage_class", ""));
            }
            if (!prefs.getProperty("s3_partial", "").equals("")) {
                this.setConfig("s3_partial", prefs.getProperty("s3_partial", ""));
            }
            if (!prefs.getProperty("before_login_script", "").equals("")) {
                this.setConfig("before_login_script", prefs.getProperty("before_login_script", ""));
            }
            if (!prefs.getProperty("after_login_script", "").equals("")) {
                this.setConfig("after_login_script", prefs.getProperty("after_login_script", ""));
            }
            if (!prefs.getProperty("before_logout_script", "").equals("")) {
                this.setConfig("before_logout_script", prefs.getProperty("before_logout_script", ""));
            }
            if (!prefs.getProperty("before_download_script", "").equals("")) {
                this.setConfig("before_download_script", prefs.getProperty("before_download_script", ""));
            }
            if (!prefs.getProperty("after_download_script", "").equals("")) {
                this.setConfig("after_download_script", prefs.getProperty("after_download_script", ""));
            }
            if (!prefs.getProperty("before_upload_script", "").equals("")) {
                this.setConfig("before_upload_script", prefs.getProperty("before_upload_script", ""));
            }
            if (!prefs.getProperty("after_upload_script", "").equals("")) {
                this.setConfig("after_upload_script", prefs.getProperty("after_upload_script", ""));
            }
            if (!prefs.getProperty("before_dir_script", "").equals("")) {
                this.setConfig("before_dir_script", prefs.getProperty("before_dir_script", ""));
            }
            if (!prefs.getProperty("after_dir_script", "").equals("")) {
                this.setConfig("after_dir_script", prefs.getProperty("after_dir_script", ""));
            }
            if (!prefs.getProperty("custom_setKeyReExchangeDisabled", "").equals("")) {
                this.setConfig("custom_setKeyReExchangeDisabled", prefs.getProperty("custom_setKeyReExchangeDisabled", ""));
            }
            if (!prefs.getProperty("custom_setIdleConnectionTimeoutSeconds", "").equals("")) {
                this.setConfig("custom_setIdleConnectionTimeoutSeconds", prefs.getProperty("custom_setIdleConnectionTimeoutSeconds", ""));
            }
            if (!prefs.getProperty("custom_enableCompression", "").equals("")) {
                this.setConfig("custom_enableCompression", prefs.getProperty("custom_enableCompression", ""));
            }
            if (!prefs.getProperty("custom_enableFIPSMode", "").equals("")) {
                this.setConfig("custom_enableFIPSMode", prefs.getProperty("custom_enableFIPSMode", ""));
            }
            if (!prefs.getProperty("custom_setDHGroupExchangeBackwardsCompatible", "").equals("")) {
                this.setConfig("custom_setDHGroupExchangeBackwardsCompatible", prefs.getProperty("custom_setDHGroupExchangeBackwardsCompatible", ""));
            }
            if (!prefs.getProperty("custom_preferredCipher", "").equals("")) {
                this.setConfig("custom_preferredCipher", prefs.getProperty("custom_preferredCipher", ""));
            }
            if (!prefs.getProperty("custom_setDHGroupExchangeKeySize", "").equals("")) {
                this.setConfig("custom_setDHGroupExchangeKeySize", prefs.getProperty("custom_setDHGroupExchangeKeySize", ""));
            }
            if (!prefs.getProperty("custom_preferredKex", "").equals("")) {
                this.setConfig("custom_preferredKex", prefs.getProperty("custom_preferredKex", ""));
            }
            if (!prefs.getProperty("custom_setUseRSAKey", "").equals("")) {
                this.setConfig("custom_setUseRSAKey", prefs.getProperty("custom_setUseRSAKey", ""));
            }
            if (!prefs.getProperty("custom_preferredMac", "").equals("")) {
                this.setConfig("custom_preferredMac", prefs.getProperty("custom_preferredMac", ""));
            }
            if (!prefs.getProperty("custom_setMaximumPacketLength", "").equals("")) {
                this.setConfig("custom_setMaximumPacketLength", prefs.getProperty("custom_setMaximumPacketLength", ""));
            }
            if (!prefs.getProperty("custom_setSessionMaxPacketSize", "").equals("")) {
                this.setConfig("custom_setSessionMaxPacketSize", prefs.getProperty("custom_setSessionMaxPacketSize", ""));
            }
            if (!prefs.getProperty("custom_setSessionMaxWindowSpace", "").equals("")) {
                this.setConfig("custom_setSessionMaxWindowSpace", prefs.getProperty("custom_setSessionMaxWindowSpace", ""));
            }
            if (!prefs.getProperty("custom_enableETM", "").equals("")) {
                this.setConfig("custom_enableETM", prefs.getProperty("custom_enableETM", ""));
            }
            if (!prefs.getProperty("custom_enableNonStandardAlgorithms", "").equals("")) {
                this.setConfig("custom_enableNonStandardAlgorithms", prefs.getProperty("custom_enableNonStandardAlgorithms", ""));
            }
            if (!prefs.getProperty("custom_charEncoding", "").equals("")) {
                this.setConfig("custom_charEncoding", prefs.getProperty("custom_charEncoding", ""));
            }
            if (!prefs.getProperty("delete_xml_representation_files", "").equals("")) {
                this.setConfig("delete_xml_representation_files", prefs.getProperty("delete_xml_representation_files", ""));
            }
            if (!prefs.getProperty("no_bucket_check", "").equals("")) {
                this.setConfig("no_bucket_check", prefs.getProperty("no_bucket_check", "false"));
            }
            if (!prefs.getProperty("s3_sha256_request_header", "").equals("")) {
                this.setConfig("s3_sha256_request_header", prefs.getProperty("s3_sha256_request_header", "false"));
            }
            if (!prefs.getProperty("dfs_enabled", "").equals("")) {
                this.setConfig("dfs_enabled", prefs.getProperty("dfs_enabled", ""));
            }
            if (!prefs.getProperty("write_timeout", "").equals("")) {
                this.setConfig("write_timeout", prefs.getProperty("write_timeout", ""));
            }
            if (!prefs.getProperty("read_timeout", "").equals("")) {
                this.setConfig("read_timeout", prefs.getProperty("read_timeout", ""));
            }
            if (!prefs.getProperty("smb3_required_signing", "").equals("")) {
                this.setConfig("smb3_required_signing", prefs.getProperty("smb3_required_signing", ""));
            }
            if (!prefs.getProperty("sas_token", "").equals("")) {
                this.setConfig("sas_token", prefs.getProperty("sas_token", ""));
            }
            if (!prefs.getProperty("upload_blob_type", "").equals("")) {
                this.setConfig("upload_blob_type", prefs.getProperty("upload_blob_type", ""));
            }
            if (!prefs.getProperty("team_drive", "").equals("")) {
                this.setConfig("team_drive", prefs.getProperty("team_drive", ""));
            }
            if (!prefs.getProperty("gstorage_with_s3_api", "").equals("")) {
                this.setConfig("gstorage_with_s3_api", prefs.getProperty("gstorage_with_s3_api", ""));
            }
            if (!prefs.getProperty("onedriveTenant", "").equals("")) {
                this.setConfig("onedriveTenant", prefs.getProperty("onedriveTenant", ""));
            }
            if (!prefs.getProperty("onedrive_user_id", "").equals("")) {
                this.setConfig("onedrive_user_id", prefs.getProperty("onedrive_user_id", ""));
            }
            if (!prefs.getProperty("onedrive_my_shares", "").equals("")) {
                this.setConfig("onedrive_my_shares", prefs.getProperty("onedrive_my_shares", ""));
            }
            if (!prefs.getProperty("onedrive_share_name", "").equals("")) {
                this.setConfig("onedrive_share_name", prefs.getProperty("onedrive_share_name", ""));
            }
            if (!prefs.getProperty("one_drive_conflict_behaviour", "").equals("")) {
                this.setConfig("one_drive_conflict_behaviour", prefs.getProperty("one_drive_conflict_behaviour", ""));
            }
            if (!prefs.getProperty("sharepoint_site_id", "").equals("")) {
                this.setConfig("sharepoint_site_id", prefs.getProperty("sharepoint_site_id", ""));
            }
            if (!prefs.getProperty("sharepoint_site_path", "").equals("")) {
                this.setConfig("sharepoint_site_path", prefs.getProperty("sharepoint_site_path", ""));
            }
            if (!prefs.getProperty("sharepoint_site_drive_name", "").equals("")) {
                this.setConfig("sharepoint_site_drive_name", prefs.getProperty("sharepoint_site_drive_name", ""));
            }
            if (!prefs.getProperty("login_config", "").equals("")) {
                this.setConfig("login_config", prefs.getProperty("login_config", ""));
            }
            if (!prefs.getProperty("box_client_id", "").equals("")) {
                this.setConfig("box_client_id", prefs.getProperty("box_client_id", ""));
            }
            if (!prefs.getProperty("box_enterprise_id", "").equals("")) {
                this.setConfig("box_enterprise_id", prefs.getProperty("box_enterprise_id", ""));
            }
            if (!prefs.getProperty("box_public_key_id", "").equals("")) {
                this.setConfig("box_public_key_id", prefs.getProperty("box_public_key_id", ""));
            }
            if (!prefs.getProperty("box_private_key", "").equals("")) {
                this.setConfig("box_private_key", prefs.getProperty("box_private_key", ""));
            }
            if (!prefs.getProperty("box_private_pass_phrase", "").equals("")) {
                this.setConfig("box_private_pass_phrase", prefs.getProperty("box_private_pass_phrase", ""));
            }
            if (!prefs.getProperty("box_store_jwt_json", "").equals("")) {
                this.setConfig("box_store_jwt_json", prefs.getProperty("box_store_jwt_json", "false"));
            }
            if (!prefs.getProperty("box_jwt_config_content", "").equals("")) {
                this.setConfig("box_jwt_config_content", prefs.getProperty("box_jwt_config_content", "false"));
            }
        }
    }

    public static Properties copyConnectionItems(Properties item) {
        Properties tempPrefs = new Properties();
        if (item.get("vItem") != null && item.get("vItem") instanceof Properties) {
            Properties vItem = (Properties)item.get("vItem");
            String name = item.getProperty("name");
            String the_file_name = item.getProperty("the_file_name");
            String path = item.getProperty("path");
            String the_file_path = item.getProperty("the_file_path");
            String type = item.getProperty("type");
            String url = item.getProperty("url");
            String modified = item.getProperty("modified");
            item.putAll((Map<?, ?>)vItem);
            if (name != null) {
                item.put("name", name);
            }
            if (the_file_name != null) {
                item.put("the_file_name", the_file_name);
            }
            if (path != null) {
                item.put("path", path);
            }
            if (the_file_path != null) {
                item.put("the_file_path", the_file_path);
            }
            if (type != null) {
                item.put("type", type);
            }
            if (url != null) {
                item.put("url", url);
            }
            if (modified != null) {
                item.put("modified", modified);
            }
        }
        tempPrefs.put("ssh_private_key", item.getProperty("ssh_private_key", ""));
        tempPrefs.put("ssh_private_key_pass", item.getProperty("ssh_private_key_pass", ""));
        tempPrefs.put("ssh_two_factor", item.getProperty("ssh_two_factor", "false"));
        tempPrefs.put("keystore_path", item.getProperty("keystore_path", ""));
        tempPrefs.put("trustore_path", item.getProperty("trustore_path", ""));
        tempPrefs.put("keystore_pass", item.getProperty("keystore_pass", ""));
        tempPrefs.put("key_pass", item.getProperty("key_pass", ""));
        tempPrefs.put("acceptAnyCert", item.getProperty("acceptAnyCert", "false"));
        tempPrefs.put("use_dmz", item.getProperty("use_dmz", "false"));
        tempPrefs.put("pasv", item.getProperty("pasv", "true"));
        tempPrefs.put("ascii", item.getProperty("ascii", "false"));
        tempPrefs.put("no_os400", item.getProperty("no_os400", "false"));
        tempPrefs.put("simple", item.getProperty("simple", "false"));
        tempPrefs.put("cwd_list", item.getProperty("cwd_list", "false"));
        tempPrefs.put("no_stat", item.getProperty("no_stat", "false"));
        tempPrefs.put("secure_data", item.getProperty("secure_data", "true"));
        tempPrefs.put("server_side_encrypt", item.getProperty("server_side_encrypt", "false"));
        tempPrefs.put("s3_accelerate", item.getProperty("s3_accelerate", "false"));
        tempPrefs.put("s3_bucket_in_path", item.getProperty("s3_bucket_in_path", "false"));
        tempPrefs.put("s3_stat_head_calls_double", item.getProperty("s3_stat_head_calls_double", "false"));
        tempPrefs.put("s3_stat_head_calls", item.getProperty("s3_stat_head_calls", "true"));
        tempPrefs.put("multithreaded_s3", item.getProperty("multithreaded_s3", "false"));
        tempPrefs.put("server_side_encrypt_kms", item.getProperty("server_side_encrypt_kms", ""));
        if (!item.getProperty("s3_sha256", "").equals("")) {
            tempPrefs.put("s3_sha256", item.getProperty("s3_sha256", ""));
        }
        if (!item.getProperty("s3_acl", "").equals("")) {
            tempPrefs.put("s3_acl", item.getProperty("s3_acl", ""));
        }
        if (!item.getProperty("s3_partial", "").equals("")) {
            tempPrefs.put("s3_partial", item.getProperty("s3_partial", ""));
        }
        if (!item.getProperty("s3_list_v2", "").equals("")) {
            tempPrefs.put("s3_list_v2", item.getProperty("s3_list_v2", ""));
        }
        if (!item.getProperty("s3_rename_allowed_speed", "").equals("")) {
            tempPrefs.put("s3_rename_allowed_speed", item.getProperty("s3_rename_allowed_speed", ""));
        }
        if (!item.getProperty("before_login_script", "").equals("")) {
            tempPrefs.put("before_login_script", item.getProperty("before_login_script", ""));
        }
        if (!item.getProperty("after_login_script", "").equals("")) {
            tempPrefs.put("after_login_script", item.getProperty("after_login_script", ""));
        }
        if (!item.getProperty("before_logout_script", "").equals("")) {
            tempPrefs.put("before_logout_script", item.getProperty("before_logout_script", ""));
        }
        if (!item.getProperty("before_download_script", "").equals("")) {
            tempPrefs.put("before_download_script", item.getProperty("before_download_script", ""));
        }
        if (!item.getProperty("after_download_script", "").equals("")) {
            tempPrefs.put("after_download_script", item.getProperty("after_download_script", ""));
        }
        if (!item.getProperty("before_upload_script", "").equals("")) {
            tempPrefs.put("before_upload_script", item.getProperty("before_upload_script", ""));
        }
        if (!item.getProperty("after_upload_script", "").equals("")) {
            tempPrefs.put("after_upload_script", item.getProperty("after_upload_script", ""));
        }
        if (!item.getProperty("before_dir_script", "").equals("")) {
            tempPrefs.put("before_dir_script", item.getProperty("before_dir_script", ""));
        }
        if (!item.getProperty("after_dir_script", "").equals("")) {
            tempPrefs.put("after_dir_script", item.getProperty("after_dir_script", ""));
        }
        if (!item.getProperty("custom_setKeyReExchangeDisabled", "").equals("")) {
            tempPrefs.put("custom_setKeyReExchangeDisabled", item.getProperty("custom_setKeyReExchangeDisabled", ""));
        }
        if (!item.getProperty("custom_setIdleConnectionTimeoutSeconds", "").equals("")) {
            tempPrefs.put("custom_setIdleConnectionTimeoutSeconds", item.getProperty("custom_setIdleConnectionTimeoutSeconds", ""));
        }
        if (!item.getProperty("custom_enableCompression", "").equals("")) {
            tempPrefs.put("custom_enableCompression", item.getProperty("custom_enableCompression", ""));
        }
        if (!item.getProperty("custom_enableFIPSMode", "").equals("")) {
            tempPrefs.put("custom_enableFIPSMode", item.getProperty("custom_enableFIPSMode", ""));
        }
        if (!item.getProperty("custom_setDHGroupExchangeBackwardsCompatible", "").equals("")) {
            tempPrefs.put("custom_setDHGroupExchangeBackwardsCompatible", item.getProperty("custom_setDHGroupExchangeBackwardsCompatible", ""));
        }
        if (!item.getProperty("custom_preferredCipher", "").equals("")) {
            tempPrefs.put("custom_preferredCipher", item.getProperty("custom_preferredCipher", ""));
        }
        if (!item.getProperty("custom_setDHGroupExchangeKeySize", "").equals("")) {
            tempPrefs.put("custom_setDHGroupExchangeKeySize", item.getProperty("custom_setDHGroupExchangeKeySize", ""));
        }
        if (!item.getProperty("custom_preferredKex", "").equals("")) {
            tempPrefs.put("custom_preferredKex", item.getProperty("custom_preferredKex", ""));
        }
        if (!item.getProperty("custom_setUseRSAKey", "").equals("")) {
            tempPrefs.put("custom_setUseRSAKey", item.getProperty("custom_setUseRSAKey", ""));
        }
        if (!item.getProperty("custom_preferredMac", "").equals("")) {
            tempPrefs.put("custom_preferredMac", item.getProperty("custom_preferredMac", ""));
        }
        if (!item.getProperty("custom_setMaximumPacketLength", "").equals("")) {
            tempPrefs.put("custom_setMaximumPacketLength", item.getProperty("custom_setMaximumPacketLength", ""));
        }
        if (!item.getProperty("custom_setSessionMaxPacketSize", "").equals("")) {
            tempPrefs.put("custom_setSessionMaxPacketSize", item.getProperty("custom_setSessionMaxPacketSize", ""));
        }
        if (!item.getProperty("custom_setSessionMaxWindowSpace", "").equals("")) {
            tempPrefs.put("custom_setSessionMaxWindowSpace", item.getProperty("custom_setSessionMaxWindowSpace", ""));
        }
        if (!item.getProperty("custom_enableETM", "").equals("")) {
            tempPrefs.put("custom_enableETM", item.getProperty("custom_enableETM", ""));
        }
        if (!item.getProperty("custom_enableNonStandardAlgorithms", "").equals("")) {
            tempPrefs.put("custom_enableNonStandardAlgorithms", item.getProperty("custom_enableNonStandardAlgorithms", ""));
        }
        if (!item.getProperty("custom_charEncoding", "").equals("")) {
            tempPrefs.put("custom_charEncoding", item.getProperty("custom_charEncoding", ""));
        }
        if (!item.getProperty("delete_xml_representation_files", "").equals("")) {
            tempPrefs.put("delete_xml_representation_files", item.getProperty("delete_xml_representation_files", ""));
        }
        if (!item.getProperty("no_bucket_check", "").equals("")) {
            tempPrefs.put("no_bucket_check", item.getProperty("no_bucket_check", "false"));
        }
        if (!item.getProperty("s3_sha256_request_header", "").equals("")) {
            tempPrefs.put("s3_sha256_request_header", item.getProperty("s3_sha256_request_header", "false"));
        }
        if (!item.getProperty("dfs_enabled", "").equals("")) {
            tempPrefs.put("dfs_enabled", item.getProperty("dfs_enabled", ""));
        }
        if (!item.getProperty("write_timeout", "").equals("")) {
            tempPrefs.put("write_timeout", item.getProperty("write_timeout", ""));
        }
        if (!item.getProperty("read_timeout", "").equals("")) {
            tempPrefs.put("read_timeout", item.getProperty("read_timeout", ""));
        }
        if (!item.getProperty("smb3_required_signing", "").equals("")) {
            tempPrefs.put("smb3_required_signing", item.getProperty("smb3_required_signing", ""));
        }
        if (!item.getProperty("sas_token", "").equals("")) {
            tempPrefs.put("sas_token", item.getProperty("sas_token", ""));
        }
        if (!item.getProperty("upload_blob_type", "").equals("")) {
            tempPrefs.put("upload_blob_type", item.getProperty("upload_blob_type", ""));
        }
        if (!item.getProperty("block_blob_upload_buffer_size", "").equals("")) {
            tempPrefs.put("block_blob_upload_buffer_size", item.getProperty("block_blob_upload_buffer_size", ""));
        }
        if (!item.getProperty("block_blob_upload_threads", "").equals("")) {
            tempPrefs.put("block_blob_upload_threads", item.getProperty("block_blob_upload_threads", ""));
        }
        if (!item.getProperty("team_drive", "").equals("false")) {
            tempPrefs.put("team_drive", item.getProperty("team_drive", ""));
        }
        if (!item.getProperty("gstorage_with_s3_api", "").equals("false")) {
            tempPrefs.put("gstorage_with_s3_api", item.getProperty("gstorage_with_s3_api", ""));
        }
        if (!item.getProperty("onedriveTenant", "").equals("")) {
            tempPrefs.put("onedriveTenant", item.getProperty("onedriveTenant", ""));
        }
        if (!item.getProperty("onedrive_user_id", "").equals("")) {
            tempPrefs.put("onedrive_user_id", item.getProperty("onedrive_user_id", ""));
        }
        if (!item.getProperty("onedrive_my_shares", "").equals("")) {
            tempPrefs.put("onedrive_my_shares", item.getProperty("onedrive_my_shares", ""));
        }
        if (!item.getProperty("onedrive_share_name", "").equals("")) {
            tempPrefs.put("onedrive_share_name", item.getProperty("onedrive_share_name", ""));
        }
        if (!item.getProperty("one_drive_conflict_behaviour", "").equals("")) {
            tempPrefs.put("one_drive_conflict_behaviour", item.getProperty("one_drive_conflict_behaviour", ""));
        }
        if (!item.getProperty("sharepoint_site_id", "").equals("")) {
            tempPrefs.put("sharepoint_site_id", item.getProperty("sharepoint_site_id", ""));
        }
        if (!item.getProperty("sharepoint_site_path", "").equals("")) {
            tempPrefs.put("sharepoint_site_path", item.getProperty("sharepoint_site_path", ""));
        }
        if (!item.getProperty("sharepoint_site_drive_name", "").equals("")) {
            tempPrefs.put("sharepoint_site_drive_name", item.getProperty("sharepoint_site_drive_name", ""));
        }
        if (!item.getProperty("login_config", "").equals("")) {
            tempPrefs.put("login_config", item.getProperty("login_config", ""));
        }
        if (!item.getProperty("box_client_id", "").equals("")) {
            tempPrefs.put("box_client_id", item.getProperty("box_client_id", ""));
        }
        if (!item.getProperty("box_enterprise_id", "").equals("")) {
            tempPrefs.put("box_enterprise_id", item.getProperty("box_enterprise_id", ""));
        }
        if (!item.getProperty("box_public_key_id", "").equals("")) {
            tempPrefs.put("box_public_key_id", item.getProperty("box_public_key_id", ""));
        }
        if (!item.getProperty("box_private_key", "").equals("")) {
            tempPrefs.put("box_private_key", item.getProperty("box_private_key", ""));
        }
        if (!item.getProperty("box_private_pass_phrase", "").equals("")) {
            tempPrefs.put("box_private_pass_phrase", item.getProperty("box_private_pass_phrase", ""));
        }
        if (!item.getProperty("box_store_jwt_json", "").equals("")) {
            tempPrefs.put("box_store_jwt_json", item.getProperty("box_store_jwt_json", "false"));
        }
        if (!item.getProperty("box_jwt_config_content", "").equals("")) {
            tempPrefs.put("box_jwt_config_content", item.getProperty("box_jwt_config_content", "false"));
        }
        return tempPrefs;
    }

    public void logout() throws Exception {
        this.config.put("logged_out", "true");
        this.close();
    }

    public Vector list(String path, Vector list) throws Exception {
        return null;
    }

    public InputStream download(String path, long startPos, long endPos, boolean binary) throws Exception {
        return this.download(path, startPos, endPos, binary, false);
    }

    public InputStream download(String path, long startPos, long endPos, boolean binary, boolean server_file) throws Exception {
        this.checkTunnel();
        String originalUrl = this.url;
        if (this.getConfig("tunnel_active", "false").equals("true")) {
            this.log("Using tunnel:" + this.url + "  -->  " + this.getConfig("tunnel_url", this.url));
            this.url = this.getConfig("tunnel_url", this.url);
        }
        try {
            this.in = this.getConfig("haDownload", "false").equals("true") ? new HADownload(this, path, startPos, endPos, binary, Integer.parseInt(this.getConfig("haDownloadDelay", "10"))) : this.download2(path, startPos, endPos, binary, server_file);
        }
        finally {
            this.url = originalUrl;
        }
        return this.in;
    }

    public InputStream getLimitedInputStream(InputStream in_tmp1, long startPos, long endPos) {
        class InputWrapper
        extends InputStream {
            InputStream in_tmp2 = null;
            boolean closed = false;
            long pos = 0L;
            long endPos = 0L;

            public InputWrapper(InputStream in_tmp2, long startPos, long endPos) {
                this.in_tmp2 = in_tmp2;
                this.pos = startPos;
                this.endPos = endPos;
            }

            @Override
            public int read() throws IOException {
                if (this.pos == this.endPos && this.endPos >= 0L) {
                    return -1;
                }
                int i = this.in_tmp2.read();
                ++this.pos;
                return i;
            }

            @Override
            public int read(byte[] b) throws IOException {
                return this.read(b, 0, b.length);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (this.pos >= this.endPos && this.endPos >= 0L) {
                    return -1;
                }
                int i = this.in_tmp2.read(b, off, len);
                if (i > 0) {
                    this.pos += (long)i;
                }
                if (this.endPos > 0L && this.pos > this.endPos) {
                    i = (int)((long)i - (this.pos - this.endPos));
                }
                return i;
            }

            @Override
            public void close() throws IOException {
                if (this.closed) {
                    return;
                }
                this.in_tmp2.close();
                this.closed = true;
                try {
                    if (GenericClient.this.thisObj instanceof SFTPClient) {
                        ((SFTPClient)GenericClient.this.thisObj).executeScript(GenericClient.this.config.getProperty("after_upload_script", ""), "");
                    }
                }
                catch (Exception e) {
                    GenericClient.this.log(e);
                }
            }
        }
        return new InputWrapper(in_tmp1, startPos, endPos);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void checkTunnel() {
        if (this.getConfig("use_tunnel", "false").equals("true") && this.getConfig("no_tunnels", "false").equals("false") && this.getConfig("tunnel_active", "false").equals("false")) {
            Object object = tunnelLock;
            synchronized (object) {
                Tunnel2.setLog(this.logQueue);
                try {
                    VRL vrl = new VRL(this.url);
                    if (vrl.getProtocol().equalsIgnoreCase("http") || vrl.getProtocol().equalsIgnoreCase("https")) {
                        final Tunnel2 t = new Tunnel2(this.url, vrl.getUsername(), vrl.getPassword(), false);
                        t.setAuth(this.config.getProperty("crushAuth", ""));
                        Properties serverTunnel = (Properties)this.getConfig("serverTunnel");
                        if (serverTunnel == null) {
                            serverTunnel = t.getTunnel();
                        }
                        if (serverTunnel == null || serverTunnel.size() == 0) {
                            this.setConfig("no_tunnels", "true");
                        }
                        if (serverTunnel != null && serverTunnel.size() > 0) {
                            this.setConfig("tunnel_active", "true");
                            this.setConfig("serverTunnel", serverTunnel);
                            final Properties tunnelInfo = new Properties();
                            this.setConfig("tunnelInfo", tunnelInfo);
                            if (serverTunnel != null && serverTunnel.size() > 0) {
                                tunnelInfo.put("tunnelActive", "true");
                                tunnelInfo.put("serverTunnel", serverTunnel);
                                t.setTunnel(serverTunnel);
                                ServerSocket ss = new ServerSocket(0);
                                int tunnelPort = ss.getLocalPort();
                                serverTunnel.put("localPort", String.valueOf(tunnelPort));
                                tunnelInfo.put("localPort", String.valueOf(tunnelPort));
                                this.setConfig("tunnel_url", "http://127.0.0.1:" + tunnelPort + "/");
                                ss.close();
                                this.log("Starting CrushTunnel:" + serverTunnel);
                                Worker.startWorker(new Runnable(){

                                    /*
                                     * Enabled aggressive block sorting
                                     * Enabled unnecessary exception pruning
                                     * Enabled aggressive exception aggregation
                                     */
                                    @Override
                                    public void run() {
                                        tunnelInfo.put("emptyTunnelCount", "0");
                                        Thread.currentThread().setName(String.valueOf(System.getProperty("crushsync.appname", "CrushSync")) + ":TunnelManager");
                                        try {
                                            try {
                                                t.startThreads();
                                                while (t.isActive()) {
                                                    if (Integer.parseInt(tunnelInfo.getProperty("emptyTunnelCount", "0")) >= 30) {
                                                        return;
                                                    }
                                                    Thread.sleep(1000L);
                                                    if (t.getQueueCount() <= 1) {
                                                        tunnelInfo.put("emptyTunnelCount", String.valueOf(Integer.parseInt(tunnelInfo.getProperty("emptyTunnelCount", "0")) + 1));
                                                    } else {
                                                        tunnelInfo.put("emptyTunnelCount", "0");
                                                    }
                                                    GenericClient.this.setConfig("tunnel_status", "(Tunnel is Active: Out=" + t.getSends() + ", In=" + t.getGets() + ")");
                                                }
                                                return;
                                            }
                                            catch (Exception e) {
                                                GenericClient.this.log(e);
                                                GenericClient.this.setConfig("tunnel_status", "");
                                                GenericClient.this.setConfig("tunnel_active", "false");
                                                GenericClient.this.log("Closing idle CrushTunnel:active=" + t.isActive());
                                                if (t.isActive()) {
                                                    t.stopThisTunnel();
                                                }
                                                GenericClient.this.log("Closing idle CrushTunnel:closed");
                                                tunnelInfo.put("tunnelActive", "false");
                                                return;
                                            }
                                        }
                                        finally {
                                            GenericClient.this.setConfig("tunnel_status", "");
                                            GenericClient.this.setConfig("tunnel_active", "false");
                                            GenericClient.this.log("Closing idle CrushTunnel:active=" + t.isActive());
                                            if (t.isActive()) {
                                                t.stopThisTunnel();
                                            }
                                            GenericClient.this.log("Closing idle CrushTunnel:closed");
                                            tunnelInfo.put("tunnelActive", "false");
                                        }
                                    }
                                });
                                this.log("Waiting for tunnel to start...");
                                while (!t.isReady() && t.isActive()) {
                                    Thread.sleep(100L);
                                }
                                this.log("Tunnel started:ready=" + t.isReady() + ":active=" + t.isActive());
                            }
                        }
                    }
                }
                catch (Exception e) {
                    this.log(e);
                }
            }
        }
    }

    protected InputStream download2(String path, long startPos, long endPos, boolean binary) throws Exception {
        return this.download2(path, startPos, endPos, binary, false);
    }

    protected InputStream download2(String path, long startPos, long endPos, boolean binary, boolean server_file) throws Exception {
        InputStream in3f1;
        Socket sock2;
        Properties socks;
        String header;
        int bytesRead;
        byte[] b_header;
        boolean check_downloads;
        boolean isPgpData;
        final String path2 = path;
        this.in3 = this.download3(path, startPos, endPos, binary, server_file);
        if (!Common.dmz_mode && this.getConfig("pgpDecryptDownload", "false").equals("true")) {
            isPgpData = true;
            check_downloads = System.getProperty("crushftp.pgp_check_downloads", "true").equals("true");
            if (!this.getConfig("decrypt_pgp_check_downloads", "").equals("")) {
                check_downloads = this.getConfig("decrypt_pgp_check_downloads", "false").equals("true");
            }
            if (check_downloads) {
                b_header = new byte[Common.encryptedNote.length()];
                this.in3 = new BufferedInputStream(this.in3);
                this.in3.mark(b_header.length + 1);
                bytesRead = this.in3.read(b_header);
                this.in3.reset();
                header = "";
                if (bytesRead > 0) {
                    header = new String(b_header, 0, bytesRead, "UTF8");
                }
                isPgpData = new PGPInspectLib().isPGPData(b_header) || header.toUpperCase().startsWith("-----BEGIN PGP MESSAGE-----");
            }
            this.log("pgpDecryptDownload:" + new VRL(this.getConfig("pgpPrivateKeyDownloadPath", "")).safe() + " isPgp:" + isPgpData + " checkedIsPgp:" + check_downloads);
            if (isPgpData || this.getConfig("pgpPrivateKeyDownloadPath").toString().toLowerCase().startsWith("password:")) {
                if (this.pgp == null) {
                    this.pgp = new PGPLib();
                }
                this.pgp.setUseExpiredKeys(true);
                socks = Common.getConnectedSocks(false);
                final Socket sock1 = (Socket)socks.get("sock1");
                sock2 = (Socket)socks.get("sock2");
                in3f1 = this.in3;
                System.setProperty("crushtunnel.debug", System.getProperty("crushftp.debug", "2"));
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        try {
                            byte[] key = null;
                            boolean pbe = false;
                            String keyLocation = GenericClient.this.getConfig("pgpPrivateKeyDownloadPath").toString();
                            if (keyLocation.toLowerCase().startsWith("password:")) {
                                pbe = true;
                            } else {
                                try {
                                    if (keyLocation.indexOf(":") < 0 || keyLocation.indexOf(":") < 3 && !keyLocation.toLowerCase().startsWith("s3:") && !keyLocation.toLowerCase().startsWith("b2:")) {
                                        keyLocation = "FILE://" + keyLocation;
                                    }
                                    key = GenericClient.loadPGPKeyFile(keyLocation);
                                }
                                catch (Exception e) {
                                    if (e instanceof FileNotFoundException) {
                                        throw new Exception(" PGP ERROR : Could not find the pgp private key! Path : " + keyLocation + " File Not Found Error message : " + e.getMessage());
                                    }
                                    throw e;
                                }
                            }
                            ByteArrayInputStream bytesIn1 = key == null ? null : new ByteArrayInputStream(key);
                            ByteArrayInputStream bytesIn2 = key == null ? null : new ByteArrayInputStream(key);
                            ByteArrayInputStream bytesIn3 = key == null ? null : new ByteArrayInputStream(key);
                            GenericClient.this.pgp.setCompression("UNCOMPRESSED");
                            OutputStream out4 = sock1.getOutputStream();
                            if (pbe) {
                                GenericClient.this.pgp.decryptStreamPBE(in3f1, Common.encryptDecrypt(GenericClient.this.getConfig("pgpPrivateKeyDownloadPassword", ""), false), out4);
                            } else if (GenericClient.this.getConfig("pgpDecryptVerify", "false").equals("true")) {
                                String publicKeyLocation = GenericClient.this.getConfig("pgp_verify_public_key_path").toString();
                                if (publicKeyLocation.indexOf(":") < 0 || publicKeyLocation.indexOf(":") < 3 && !publicKeyLocation.toLowerCase().startsWith("s3:") && !publicKeyLocation.toLowerCase().startsWith("b2:")) {
                                    publicKeyLocation = "FILE://" + publicKeyLocation;
                                }
                                byte[] publicKey = GenericClient.loadPGPKeyFile(publicKeyLocation);
                                SignatureCheckResult verified = GenericClient.this.pgp.decryptAndVerify(in3f1, bytesIn2, Common.encryptDecrypt(GenericClient.this.getConfig("pgpPrivateKeyDownloadPassword", "").toString(), false), new ByteArrayInputStream(publicKey), out4);
                                if (!verified.equals((Object)SignatureCheckResult.SignatureVerified)) {
                                    throw new Exception("Verification failed while decrypting! Result: " + (Object)((Object)verified));
                                }
                            } else {
                                try {
                                    if (new KeyStore().importPrivateKey(bytesIn1)[0].checkPassword(GenericClient.this.getConfig("pgpPrivateKeyDownloadPassword", "").toString())) {
                                        GenericClient.this.pgp.decryptStream(in3f1, bytesIn2, GenericClient.this.getConfig("pgpPrivateKeyDownloadPassword", "").toString(), out4);
                                    } else {
                                        GenericClient.this.pgp.decryptStream(in3f1, bytesIn2, Common.encryptDecrypt(GenericClient.this.getConfig("pgpPrivateKeyDownloadPassword", "").toString(), false), out4);
                                    }
                                }
                                catch (Exception e) {
                                    GenericClient.this.pgp.decryptStream(in3f1, bytesIn3, Common.encryptDecrypt(GenericClient.this.getConfig("pgpPrivateKeyDownloadPassword", "").toString(), false), out4);
                                }
                            }
                            if (bytesIn1 != null) {
                                bytesIn1.close();
                            }
                            if (bytesIn2 != null) {
                                bytesIn2.close();
                            }
                            if (out4 != null) {
                                out4.close();
                            }
                        }
                        catch (Exception e) {
                            GenericClient.this.log(e);
                            Common.log("SERVER", 1, e);
                            try {
                                in3f1.close();
                            }
                            catch (IOException e1) {
                                GenericClient.this.log(e1);
                                Common.log("SERVER", 1, e1);
                            }
                        }
                    }
                }, String.valueOf(Thread.currentThread().getName()) + "PGPDecryptStream:" + path);
                this.in3 = new InputStreamCloser(sock2.getInputStream(), in3f1);
            }
        }
        if (this.getConfig("pgpEncryptDownload", "").equals("true")) {
            this.log("pgpEncryptDownload:" + new VRL(this.getConfig("pgpPublicKeyDownloadPath", "")).safe());
        }
        if (!Common.dmz_mode && this.getConfig("pgpEncryptDownload", "false").equals("true")) {
            System.setProperty("crushtunnel.debug", System.getProperty("crushftp.debug", "2"));
            isPgpData = true;
            check_downloads = System.getProperty("crushftp.pgp_check_downloads", "true").equals("true");
            if (!this.getConfig("encrypt_pgp_check_downloads", "").equals("")) {
                check_downloads = this.getConfig("encrypt_pgp_check_downloads", "false").equals("true");
            }
            if (check_downloads) {
                b_header = new byte[Common.encryptedNote.length()];
                this.in3 = new BufferedInputStream(this.in3);
                this.in3.mark(b_header.length + 1);
                bytesRead = this.in3.read(b_header);
                this.in3.reset();
                header = "";
                if (bytesRead > 0) {
                    header = new String(b_header, 0, bytesRead, "UTF8");
                }
                isPgpData = new PGPInspectLib().isPGPData(b_header) || header.toUpperCase().startsWith("-----BEGIN PGP MESSAGE-----");
            }
            this.log("pgpEncryptDownload:" + new VRL(this.getConfig("pgpPublicKeyDownloadPath", "")).safe() + " isPgp:" + isPgpData + " checkedIsPgp:" + check_downloads);
            if (!isPgpData) {
                if (this.pgp == null) {
                    this.pgp = new PGPLib();
                }
                this.pgp.setUseExpiredKeys(true);
                socks = Common.getConnectedSocks(false);
                final Socket sock1 = (Socket)socks.get("sock1");
                sock2 = (Socket)socks.get("sock2");
                in3f1 = this.in3;
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        try {
                            String cypher;
                            byte[] key = null;
                            boolean pbe = false;
                            String keyLocation = GenericClient.this.getConfig("pgpPublicKeyDownloadPath").toString();
                            String privateKeyLocation = "";
                            byte[] private_key = null;
                            if (keyLocation.toLowerCase().startsWith("password:")) {
                                pbe = true;
                            } else {
                                try {
                                    if (keyLocation.indexOf(":") < 0 || keyLocation.indexOf(":") < 3 && !keyLocation.toLowerCase().startsWith("s3:") && !keyLocation.toLowerCase().startsWith("b2:")) {
                                        keyLocation = "FILE://" + keyLocation;
                                    }
                                    key = GenericClient.loadPGPKeyFile(keyLocation);
                                    if (GenericClient.this.getConfig("pgpSignDownload") != null && GenericClient.this.getConfig("pgpSignDownload", "false").equals("true")) {
                                        privateKeyLocation = GenericClient.this.getConfig("pgpPrivateKeyDownloadPath").toString().trim();
                                        if (privateKeyLocation.indexOf(":") < 0 || privateKeyLocation.indexOf(":") < 3 && !privateKeyLocation.toLowerCase().startsWith("s3:") && !privateKeyLocation.toLowerCase().startsWith("b2:")) {
                                            privateKeyLocation = "FILE://" + privateKeyLocation;
                                        }
                                        private_key = GenericClient.loadPGPKeyFile(privateKeyLocation);
                                    }
                                }
                                catch (Exception e) {
                                    if (e instanceof FileNotFoundException) {
                                        throw new Exception(" PGP ERROR : Could not found the pgp public key! Path : " + keyLocation + " File not Found Error message : " + e.getMessage());
                                    }
                                    throw e;
                                }
                            }
                            ByteArrayInputStream bytesIn = key == null ? null : new ByteArrayInputStream(key);
                            GenericClient.this.pgp.setCompression("UNCOMPRESSED");
                            OutputStream out4 = sock1.getOutputStream();
                            String filename = Common.last(path2);
                            if (filename.endsWith(".filepart")) {
                                filename = filename.substring(0, filename.lastIndexOf("."));
                            }
                            if (!(cypher = GenericClient.this.getConfig("encryption_cypher_download", "").trim()).equals("") && !cypher.equalsIgnoreCase("NULL")) {
                                if (!GenericClient.this.pgp.isOverrideKeyAlgorithmPreferences()) {
                                    GenericClient.this.pgp.setOverrideKeyAlgorithmPreferences(true);
                                }
                                GenericClient.this.pgp.setCypher(cypher);
                            }
                            if (pbe) {
                                GenericClient.this.pgp.encryptStreamPBE(in3f1, filename, Common.encryptDecrypt(keyLocation.substring(keyLocation.indexOf(":") + 1), false), out4, GenericClient.this.getConfig("pgpAsciiDownload", "false").equals("true"), System.getProperty("crushftp.pgp_integrity_protect", "false").equals("true"));
                            } else if (GenericClient.this.getConfig("pgpSignDownload") != null && GenericClient.this.getConfig("pgpSignDownload", "false").equals("true")) {
                                GenericClient.this.pgp.signAndEncryptStream(in3f1, filename, (InputStream)new ByteArrayInputStream(private_key), Common.encryptDecrypt(GenericClient.this.getConfig("pgpPrivateKeyDownloadPassword", "").toString(), false), bytesIn, out4, GenericClient.this.getConfig("pgpAsciiDownload", "false").equals("true"), System.getProperty("crushftp.pgp_integrity_protect", "false").equals("true"));
                            } else {
                                GenericClient.this.pgp.encryptStream(in3f1, filename, bytesIn, out4, GenericClient.this.getConfig("pgpAsciiDownload", "false").equals("true"), System.getProperty("crushftp.pgp_integrity_protect", "false").equals("true"));
                            }
                            if (bytesIn != null) {
                                bytesIn.close();
                            }
                            if (out4 != null) {
                                out4.close();
                            }
                        }
                        catch (Exception e) {
                            GenericClient.this.log(e);
                            Common.log("SERVER", 1, e);
                            try {
                                in3f1.close();
                            }
                            catch (IOException e1) {
                                GenericClient.this.log(e);
                                Common.log("SERVER", 1, e);
                            }
                        }
                    }
                }, String.valueOf(Thread.currentThread().getName()) + "PGPEncryptStream:" + path);
                this.in3 = new InputStreamCloser(sock2.getInputStream(), in3f1);
            }
        }
        return this.in3;
    }

    protected void setupTimeout(Properties byteCount, Socket transfer_socket) {
        int secs = Integer.parseInt(this.config.getProperty("timeout", "600"));
        if (secs != 0) {
            byteCount.put("t", String.valueOf(System.currentTimeMillis()));
            byteCount.put("b", "0");
            byteCount.put("secs", String.valueOf(secs));
            byteCount.put("transfer_socket", transfer_socket);
            try {
                transfer_socket.setSoTimeout(secs * 1000 + 10000);
            }
            catch (Exception exception) {
                // empty catch block
            }
            ftp_client_sockets.addElement(byteCount);
        }
    }

    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        return this.download3(path, startPos, endPos, binary, false);
    }

    protected InputStream download3(String path, long startPos, long endPos, boolean binary, boolean server_file) throws Exception {
        return this.download3(path, startPos, endPos, binary);
    }

    public OutputStream upload(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        this.checkTunnel();
        String originalUrl = this.url;
        if (this.getConfig("tunnel_active", "false").equals("true")) {
            this.log("Using tunnel:" + this.url + "  -->  " + this.getConfig("tunnel_url", this.url));
            this.url = this.getConfig("tunnel_url", this.url);
        }
        try {
            this.out = this.getConfig("haUpload", "false").equals("true") ? new HAUpload(this, path, startPos, truncate, binary, Integer.parseInt(this.getConfig("haUploadPriorWriteCount", "130")), Integer.parseInt(this.getConfig("haUploadDelay", "10"))) : this.upload2(path, startPos, truncate, binary);
        }
        finally {
            this.url = originalUrl;
        }
        return this.out;
    }

    public boolean upload_0_byte(String path) throws Exception {
        this.upload3(path, 0L, true, true).close();
        return true;
    }

    protected OutputStream upload2(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        final String path2 = path;
        ByteArrayOutputStream decrypted_file_bytes = null;
        if (startPos > 0L && !Common.dmz_mode && this.getConfig("pgpEncryptUpload", "false").equals("true") && this instanceof FileClient) {
            Properties stat = this.stat(path);
            if (stat != null && Long.parseLong(stat.getProperty("size")) < 0x3200000L) {
                decrypted_file_bytes = new ByteArrayOutputStream();
                Common.copyStreams(this.download(path, 0L, -1L, binary), decrypted_file_bytes, true, true);
                this.out3 = this.upload3(path, 0L, true, binary);
            } else {
                this.out3 = this.upload3(path, startPos, truncate, binary);
            }
        } else {
            this.out3 = this.upload3(path, startPos, truncate, binary);
        }
        if (this.getConfig("icap_scanning", "false").equals("true")) {
            this.log("ICAP scanning " + this.getConfig("icap_server_host_port", "") + " path=" + path + " max_bytes=" + Common.format_bytes_short(Long.parseLong(this.getConfig("icap_max_bytes", ""))));
            this.out3 = new ICAPProxyClient(this.getConfig("icap_server_host_port", "").split(":")[0].trim(), Integer.parseInt(this.getConfig("icap_server_host_port", "").split(":")[1].trim()), this.getConfig("icap_service", ""), path2, this.out3, Long.parseLong(this.getConfig("icap_max_bytes", "")));
        }
        final OutputStream out3f1 = this.out3;
        if (this.getConfig("pgpDecryptUpload", "").equals("true")) {
            this.log("pgpDecryptUpload:" + new VRL(this.getConfig("pgpPrivateKeyUploadPath", "")).safe());
        }
        if (this.getConfig("pgpEncryptUpload", "").equals("true")) {
            this.log("pgpEncryptUpload:" + new VRL(this.getConfig("pgpPublicKeyUploadPath", "")).safe());
        }
        if (!Common.dmz_mode && this.getConfig("pgpEncryptUpload", "false").equals("true")) {
            System.setProperty("crushtunnel.debug", System.getProperty("crushftp.debug", "2"));
            if (this.pgp == null) {
                this.pgp = new PGPLib();
            }
            this.pgp.setUseExpiredKeys(true);
            Properties socks = Common.getConnectedSocks(false);
            final Socket sock1 = (Socket)socks.remove("sock1");
            Socket sock2 = (Socket)socks.remove("sock2");
            final Properties status = new Properties();
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        String cypher;
                        String filename;
                        byte[] key = null;
                        boolean pbe = false;
                        String keyLocation = GenericClient.this.getConfig("pgpPublicKeyUploadPath").toString();
                        String privateKeyLocation = "";
                        byte[] private_key = null;
                        if (keyLocation.toLowerCase().startsWith("password:")) {
                            pbe = true;
                        } else {
                            try {
                                if (keyLocation.indexOf(":") < 0 || keyLocation.indexOf(":") < 3 && !keyLocation.toLowerCase().startsWith("s3:") && !keyLocation.toLowerCase().startsWith("b2:")) {
                                    keyLocation = "FILE://" + keyLocation;
                                }
                                key = GenericClient.loadPGPKeyFile(keyLocation);
                                if (GenericClient.this.getConfig("pgpSignUpload") != null && GenericClient.this.getConfig("pgpSignUpload", "false").equals("true")) {
                                    privateKeyLocation = GenericClient.this.getConfig("pgpPrivateKeyUploadPath").toString().trim();
                                    if (privateKeyLocation.indexOf(":") < 0 || privateKeyLocation.indexOf(":") < 3 && !privateKeyLocation.toLowerCase().startsWith("s3:") && !privateKeyLocation.toLowerCase().startsWith("b2:")) {
                                        privateKeyLocation = "FILE://" + privateKeyLocation;
                                    }
                                    private_key = GenericClient.loadPGPKeyFile(privateKeyLocation);
                                }
                            }
                            catch (Exception e) {
                                if (e instanceof FileNotFoundException) {
                                    throw new Exception(" PGP ERROR : Could not found the pgp public key! Path : " + keyLocation + " File not Found Error message : " + e.getMessage());
                                }
                                throw e;
                            }
                        }
                        ByteArrayInputStream bytesIn = key == null ? null : new ByteArrayInputStream(key);
                        GenericClient.this.pgp.setCompression("UNCOMPRESSED");
                        if (GenericClient.this.getConfig("pgpAsciiUpload", "false").equals("true")) {
                            GenericClient.this.pgp.setAsciiVersionHeader(String.valueOf(System.getProperty("appname", "CrushFTP").toUpperCase()) + "#                                        ");
                        }
                        if ((filename = Common.last(path2)).endsWith(".filepart")) {
                            filename = filename.substring(0, filename.lastIndexOf("."));
                        }
                        if (!(cypher = GenericClient.this.getConfig("encryption_cypher_upload", "").trim()).equals("") && !cypher.equalsIgnoreCase("NULL")) {
                            if (!GenericClient.this.pgp.isOverrideKeyAlgorithmPreferences()) {
                                GenericClient.this.pgp.setOverrideKeyAlgorithmPreferences(true);
                            }
                            GenericClient.this.pgp.setCypher(cypher);
                        }
                        if (pbe) {
                            GenericClient.this.pgp.encryptStreamPBE(sock1.getInputStream(), filename, Common.encryptDecrypt(keyLocation.substring(keyLocation.indexOf(":") + 1), false), out3f1, GenericClient.this.getConfig("pgpAsciiUpload", "false").equals("true"), System.getProperty("crushftp.pgp_integrity_protect", "false").equals("true"));
                        } else if (GenericClient.this.getConfig("pgpSignUpload") != null && GenericClient.this.getConfig("pgpSignUpload", "false").equals("true")) {
                            GenericClient.this.pgp.signAndEncryptStream(sock1.getInputStream(), filename, (InputStream)new ByteArrayInputStream(private_key), Common.encryptDecrypt(GenericClient.this.getConfig("pgpPrivateKeyUploadPassword", "").toString(), false), bytesIn, out3f1, GenericClient.this.getConfig("pgpAsciiUpload", "false").equals("true"), System.getProperty("crushftp.pgp_integrity_protect", "false").equals("true"));
                        } else {
                            GenericClient.this.pgp.encryptStream(sock1.getInputStream(), filename, bytesIn, out3f1, GenericClient.this.getConfig("pgpAsciiUpload", "false").equals("true"), System.getProperty("crushftp.pgp_integrity_protect", "false").equals("true"));
                        }
                        if (bytesIn != null) {
                            bytesIn.close();
                        }
                        status.put("status", "SUCCESS");
                    }
                    catch (Exception e) {
                        status.put("error", e);
                        status.put("status", "ERROR");
                        Common.log("SERVER", 1, e);
                        try {
                            out3f1.close();
                        }
                        catch (IOException e1) {
                            Common.log("SERVER", 1, e1);
                        }
                    }
                }
            }, String.valueOf(Thread.currentThread().getName()) + "PGPEncryptStream:" + path);
            boolean write_pgp_size_footer = this.getConfig("pgpAsciiUpload", "false").equals("false");
            if (!this.getConfig("pgpAsciiUpload", "false").equals("true") && !this.getConfig("hint_decrypted_size", "").equals("")) {
                write_pgp_size_footer = this.getConfig("hint_decrypted_size", "false").equals("true");
            }
            boolean write_pgp_size_header = this.getConfig("pgpAsciiUpload", "false").equals("true");
            if (!this.getConfig("hint_decrypted_size", "").equals("")) {
                write_pgp_size_footer &= this.getConfig("hint_decrypted_size", "false").equals("true");
            }
            this.out3 = new OutputStreamCloser(sock2.getOutputStream(), status, this.thisObj, path, write_pgp_size_footer && !this.getConfig("pgpAsciiUpload", "false").equals("true"), write_pgp_size_header, out3f1);
            if (decrypted_file_bytes != null) {
                this.out3.write(decrypted_file_bytes.toByteArray());
                decrypted_file_bytes.reset();
                decrypted_file_bytes = null;
            }
        }
        final OutputStream out3f2 = this.out3;
        if (!Common.dmz_mode && this.getConfig("pgpDecryptUpload", "false").equals("true")) {
            System.setProperty("crushtunnel.debug", System.getProperty("crushftp.debug", "2"));
            if (this.pgp == null) {
                this.pgp = new PGPLib();
            }
            this.pgp.setUseExpiredKeys(true);
            Properties socks = Common.getConnectedSocks(false);
            final Socket sock1 = (Socket)socks.get("sock1");
            Socket sock2 = (Socket)socks.get("sock2");
            final Properties status = new Properties();
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        byte[] key = null;
                        boolean pbe = false;
                        String keyLocation = GenericClient.this.getConfig("pgpPrivateKeyUploadPath").toString();
                        if (keyLocation.toLowerCase().startsWith("password:")) {
                            pbe = true;
                        } else {
                            try {
                                if (keyLocation.indexOf(":") < 0 || keyLocation.indexOf(":") < 3 && !keyLocation.toLowerCase().startsWith("s3:") && !keyLocation.toLowerCase().startsWith("b2:")) {
                                    keyLocation = "FILE://" + keyLocation;
                                }
                                key = GenericClient.loadPGPKeyFile(keyLocation);
                            }
                            catch (Exception e) {
                                if (e instanceof FileNotFoundException) {
                                    throw new Exception(" PGP ERROR : Could not found the pgp private key! Path : " + keyLocation + " File not Found Error message : " + e.getMessage());
                                }
                                throw e;
                            }
                        }
                        ByteArrayInputStream bytesIn1 = key == null ? null : new ByteArrayInputStream(key);
                        ByteArrayInputStream bytesIn2 = key == null ? null : new ByteArrayInputStream(key);
                        ByteArrayInputStream bytesIn3 = key == null ? null : new ByteArrayInputStream(key);
                        GenericClient.this.pgp.setCompression("UNCOMPRESSED");
                        if (pbe) {
                            GenericClient.this.pgp.decryptStreamPBE(sock1.getInputStream(), Common.encryptDecrypt(GenericClient.this.getConfig("pgpPrivateKeyUploadPassword", ""), false), out3f2);
                        } else if (GenericClient.this.getConfig("pgpDecryptVerify", "false").equals("true")) {
                            String publicKeyLocation = GenericClient.this.getConfig("pgp_verify_public_key_path").toString();
                            if (publicKeyLocation.indexOf(":") < 0 || publicKeyLocation.indexOf(":") < 3 && !publicKeyLocation.toLowerCase().startsWith("s3:") && !publicKeyLocation.toLowerCase().startsWith("b2:")) {
                                publicKeyLocation = "FILE://" + publicKeyLocation;
                            }
                            byte[] publicKey = GenericClient.loadPGPKeyFile(publicKeyLocation);
                            SignatureCheckResult verified = GenericClient.this.pgp.decryptAndVerify(sock1.getInputStream(), bytesIn2, Common.encryptDecrypt(GenericClient.this.getConfig("pgpPrivateKeyUploadPassword", "").toString(), false), new ByteArrayInputStream(publicKey), out3f2);
                            if (!verified.equals((Object)SignatureCheckResult.SignatureVerified)) {
                                throw new Exception("Verification failed while decrypting! Result: " + (Object)((Object)verified));
                            }
                        } else {
                            try {
                                if (new KeyStore().importPrivateKey(bytesIn1)[0].checkPassword(GenericClient.this.getConfig("pgpPrivateKeyUploadPassword", "").toString())) {
                                    GenericClient.this.pgp.decryptStream(sock1.getInputStream(), bytesIn2, GenericClient.this.getConfig("pgpPrivateKeyUploadPassword", "").toString(), out3f2);
                                } else {
                                    GenericClient.this.pgp.decryptStream(sock1.getInputStream(), bytesIn2, Common.encryptDecrypt(GenericClient.this.getConfig("pgpPrivateKeyUploadPassword", "").toString(), false), out3f2);
                                }
                            }
                            catch (Exception e) {
                                GenericClient.this.pgp.decryptStream(sock1.getInputStream(), bytesIn3, Common.encryptDecrypt(GenericClient.this.getConfig("pgpPrivateKeyUploadPassword", "").toString(), false), out3f2);
                            }
                        }
                        if (bytesIn1 != null) {
                            bytesIn1.close();
                        }
                        if (bytesIn2 != null) {
                            bytesIn2.close();
                        }
                        status.put("status", "SUCCESS");
                    }
                    catch (Exception e) {
                        status.put("error", e);
                        status.put("status", "ERROR");
                        Common.log("SERVER", 1, e);
                        try {
                            out3f2.close();
                        }
                        catch (IOException e1) {
                            Common.log("SERVER", 1, e1);
                        }
                    }
                }
            }, String.valueOf(Thread.currentThread().getName()) + "PGPDecryptStream:" + path);
            this.out3 = new OutputStreamCloser(sock2.getOutputStream(), status, this.thisObj, path, false, false, out3f2);
        }
        return this.out3;
    }

    protected OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        return null;
    }

    public boolean delete(String path) throws Exception {
        return false;
    }

    public boolean makedir(String path) throws Exception {
        return false;
    }

    public boolean makedirs(String path) throws Exception {
        return false;
    }

    public boolean rename(String rnfr, String rnto) throws Exception {
        return this.rename(rnfr, rnto, false);
    }

    public boolean rename(String rnfr, String rnto, boolean overwrite) throws Exception {
        return false;
    }

    public Properties stat(String path) throws Exception {
        return null;
    }

    public void setMod(String path, String val, String param) throws Exception {
    }

    public void setOwner(String path, String val, String param) throws Exception {
    }

    public void setGroup(String path, String val, String param) throws Exception {
    }

    public boolean mdtm(String path, long modified) throws Exception {
        return false;
    }

    public void close() throws Exception {
        if (this.in != null) {
            this.in.close();
            this.in = null;
        }
        if (this.out != null) {
            this.out.close();
            this.out = null;
        }
    }

    public String doCommand(String command) throws Exception {
        return "";
    }

    public void setConfigObj(Properties config) {
        config.putAll((Map<?, ?>)this.config);
        this.config = config;
    }

    public void setConfig(String key, Object o) {
        Properties p;
        Properties properties = p = key.startsWith("transfer_") ? this.transfer_info : this.config;
        if (o == null) {
            p.remove(key);
        } else {
            p.put(key, o);
        }
    }

    public String getConfig(String key, String s) {
        Properties p = key.startsWith("transfer_") ? this.transfer_info : this.config;
        return p.getProperty(key, s);
    }

    public Object getConfig(String key) {
        Properties p = key.startsWith("transfer_") ? this.transfer_info : this.config;
        return p.get(key);
    }

    public Properties getConfig() {
        return this.config;
    }

    public long getLength(String path) throws Exception {
        Properties p = this.stat(path);
        if (p == null) {
            return 0L;
        }
        return Long.parseLong(p.getProperty("size", "0"));
    }

    public long getLastModified(String path) throws Exception {
        Properties p = this.stat(path);
        return Long.parseLong(p.getProperty("modified", "0"));
    }

    public static String u(String s) throws Exception {
        String r = Common.makeBoundary(20);
        s = s.replaceAll(" ", r);
        s = URLEncoder.encode(s, "utf-8");
        s = s.replaceAll(r, "%20");
        return s;
    }

    public static void writeEntry(String key, String val, BufferedOutputStream dos, String boundary) throws Exception {
        dos.write((String.valueOf(boundary) + "\r\n").getBytes("UTF8"));
        dos.write(("Content-Disposition: form-data; name=\"" + key + "\"\r\n").getBytes("UTF8"));
        dos.write("\r\n".getBytes("UTF8"));
        dos.write((String.valueOf(val) + "\r\n").getBytes("UTF8"));
    }

    public static void writeEnd(BufferedOutputStream dos, String boundary) throws Exception {
        dos.write((String.valueOf(boundary) + "--\r\n").getBytes("UTF8"));
        dos.flush();
        dos.close();
    }

    public String getLastMd5() {
        return "not calculated";
    }

    public static Properties parseStat(String data) throws Exception {
        return GenericClient.parseStat(data, true);
    }

    public static Properties parseStat(String data, boolean check_invalid) throws Exception {
        if ((data = data.trim()).equals("")) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(data);
        Properties item = new Properties();
        item.put("privs", st.nextToken());
        if (item.getProperty("privs").toUpperCase().startsWith("D")) {
            item.put("permissions", "drwxrwxrwx");
            item.put("type", "DIR");
        } else {
            item.put("permissions", "-rwxrwxrwx");
            item.put("type", "FILE");
        }
        item.put("count", st.nextToken());
        item.put("num_items", item.getProperty("count"));
        item.put("owner", st.nextToken());
        item.put("group", st.nextToken());
        item.put("size", st.nextToken());
        String dateStr = st.nextToken();
        item.put("modified", dateStr);
        SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        Date d = yyyyMMddHHmmss.parse(dateStr);
        SimpleDateFormat mmm = new SimpleDateFormat("MMM", Locale.US);
        item.put("month", mmm.format(d));
        item.put("day", st.nextToken());
        String year = st.nextToken();
        item.put("time_or_year", year);
        String root_dir = st.nextToken();
        root_dir = data.substring(data.indexOf(" " + year + " " + root_dir) + (" " + year + " ").length());
        root_dir = root_dir.replaceAll("&amp;", "&").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("%5C", "\\");
        item.put("path", Common.all_but_last(root_dir));
        item.put("name", Common.last(root_dir));
        if (check_invalid && Integer.parseInt(System.getProperty("crushftp.version", "10")) >= 10 && (item.getProperty("name").indexOf(":") >= 0 || item.getProperty("name").indexOf("/") >= 0 || item.getProperty("name").indexOf("\\") >= 0)) {
            item.put("name", String.valueOf(item.getProperty("name")) + "_INVALID_NAME");
        }
        GenericClient.setFileDateInfo(item, d);
        if (item.getProperty("type").equalsIgnoreCase("DIR")) {
            item.put("size", "1");
        }
        return item;
    }

    public static Properties parseDmzStat(String data) throws Exception {
        if ((data = data.trim()).equals("") || data.indexOf(";") < 0) {
            return null;
        }
        String[] items = data.split(";");
        Properties item = new Properties();
        int x = 0;
        while (x < items.length) {
            if (!items[x].trim().equals("")) {
                String[] parts = items[x].split("=");
                String key = parts[0];
                String val = "";
                if (parts.length > 1) {
                    val = parts[1];
                }
                item.put(key, Common.url_decode(val));
            }
            ++x;
        }
        return item;
    }

    public static void setFileDateInfo(Properties dir_item, Date itemDate) {
        SimpleDateFormat mm = new SimpleDateFormat("MM", Locale.US);
        SimpleDateFormat dd = new SimpleDateFormat("dd", Locale.US);
        SimpleDateFormat yyyy = new SimpleDateFormat("yyyy", Locale.US);
        SimpleDateFormat hhmm = new SimpleDateFormat("HH:mm", Locale.US);
        dir_item.put("modified", String.valueOf(itemDate.getTime()));
        dir_item.put("created", String.valueOf(itemDate.getTime()));
        dir_item.put("month", months[Integer.parseInt(mm.format(itemDate))]);
        dir_item.put("day", dd.format(itemDate));
        String time_or_year = hhmm.format(itemDate);
        if (!yyyy.format(itemDate).equals(yyyy.format(new Date()))) {
            time_or_year = yyyy.format(itemDate);
        }
        dir_item.put("time_or_year", time_or_year);
    }

    public void set_MD5_and_upload_id(String path) throws Exception {
    }

    public String getUploadedByMetadata(String path) {
        return "";
    }

    public boolean hasThumbnails(Properties item) {
        return false;
    }

    public boolean downloadThumbnail(Properties info) throws Exception {
        return false;
    }

    public boolean isSearchSupported(Properties config) {
        return false;
    }

    public Vector search(String path, Vector list, Properties config) throws Exception {
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void startSocketCleaner() {
        Object object = socketCleanerLock;
        synchronized (object) {
            if (socketCleaner == null) {
                socketCleaner = new Thread(new Runnable(){

                    @Override
                    public void run() {
                        while (true) {
                            try {
                                Vector v = (Vector)ftp_client_sockets.clone();
                                while (v.size() > 20) {
                                    v.remove(0);
                                }
                                GenericClient.checkIdleClientSockets();
                                Thread.currentThread().setName("FTPClient socket cleanup thread:" + v);
                            }
                            catch (Exception e) {
                                Common.log("SERVER", 1, e);
                            }
                            try {
                                Thread.sleep(30000L);
                            }
                            catch (Exception exception) {
                                continue;
                            }
                            break;
                        }
                    }
                });
                socketCleaner.start();
            }
        }
    }

    protected InputStream getInsputStreamWrapper(URLConnection urlc, InputStream in_org) {
        class InputWrapper
        extends InputStream {
            InputStream in_org = null;
            boolean closed = false;
            URLConnection urlc = null;

            public InputWrapper(URLConnection urlc, InputStream in_org) {
                this.in_org = in_org;
                this.urlc = urlc;
            }

            @Override
            public int read() throws IOException {
                return this.in_org.read();
            }

            @Override
            public int read(byte[] b) throws IOException {
                return this.in_org.read(b);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return this.in_org.read(b, off, len);
            }

            @Override
            public void close() throws IOException {
                if (this.closed) {
                    return;
                }
                this.in_org.close();
                this.urlc.disconnect();
                this.closed = true;
            }
        }
        return new InputWrapper(urlc, this.in);
    }

    public static void checkIdleClientSockets() {
        Vector v = (Vector)ftp_client_sockets.clone();
        int xx = 0;
        while (xx < v.size()) {
            Properties byteCount = (Properties)v.elementAt(xx);
            boolean kill = false;
            Socket transfer_socket = (Socket)byteCount.get("transfer_socket");
            if (transfer_socket != null) {
                long secs = Long.parseLong(byteCount.getProperty("secs", "600"));
                if (byteCount.getProperty("status", "").equals("") && !transfer_socket.isClosed()) {
                    Thread.currentThread().setName("FTPClientTimeout::" + System.currentTimeMillis() + ":" + byteCount);
                    byteCount.put("idle", String.valueOf(System.currentTimeMillis() - Long.parseLong(byteCount.getProperty("t"))));
                    if (System.currentTimeMillis() - Long.parseLong(byteCount.getProperty("t")) > secs) {
                        kill = true;
                    }
                } else {
                    kill = true;
                }
                if (kill) {
                    ftp_client_sockets.remove(byteCount);
                    final Properties byteCount2 = byteCount;
                    byteCount.remove("transfer_socket");
                    try {
                        Worker.startWorker(new Runnable(){

                            @Override
                            public void run() {
                                try {
                                    Thread.currentThread().setName("FTPClient:closing socket for being done or idle:" + byteCount2);
                                    Socket sock = (Socket)byteCount2.remove("transfer_socket");
                                    if (sock != null) {
                                        sock.close();
                                    }
                                    Thread.currentThread().setName("FTPClient:Idle");
                                }
                                catch (IOException iOException) {
                                    // empty catch block
                                }
                            }
                        });
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                }
            }
            ++xx;
        }
    }

    public static String getKeyText(Element el, String key) {
        List l = el.getChildren();
        int x = 0;
        while (x < l.size()) {
            Element el2 = (Element)l.get(x);
            if (el2.getName().equals(key)) {
                return el2.getValue();
            }
            ++x;
        }
        return null;
    }

    public static Element getElement(Element el, String key) {
        List l = el.getChildren();
        int x = 0;
        while (x < l.size()) {
            Element el2 = (Element)l.get(x);
            if (el2.getName().equals(key)) {
                return el2;
            }
            ++x;
        }
        return null;
    }

    public static List getElements(Element el, String key) {
        ArrayList<Element> l2 = new ArrayList<Element>();
        List l = el.getChildren();
        int x = 0;
        while (x < l.size()) {
            Element el2 = (Element)l.get(x);
            if (el2.getName().equals(key)) {
                l2.add(el2);
            }
            ++x;
        }
        return l2;
    }

    public static byte[] loadPGPKeyFile(String keyLocation) throws Exception {
        byte[] key = null;
        if (System.getProperty("crushftp.v10_beta", "false").equals("true") && Common.System2.containsKey("crushftp.keystores." + keyLocation.toUpperCase().replace('\\', '/')) && !keyLocation.equals("")) {
            Properties p = (Properties)Common.System2.get("crushftp.keystores." + keyLocation.toUpperCase().replace('\\', '/'));
            key = (byte[])p.get("bytes");
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            VRL key_vrl = new VRL(keyLocation);
            GenericClient c_key = Common.getClient(Common.getBaseUrl(key_vrl.toString()), System.getProperty("appname", "CrushFTP"), new Vector());
            if (System.getProperty("crushftp.v10_beta", "false").equals("true") && key_vrl.getConfig() != null && key_vrl.getConfig().size() > 0) {
                c_key.setConfigObj(key_vrl.getConfig());
            }
            if (c_key instanceof S3CrushClient) {
                c_key.login(key_vrl.getUsername(), key_vrl.getPassword(), Common.all_but_last(key_vrl.getPath()));
            } else {
                c_key.login(key_vrl.getUsername(), key_vrl.getPassword(), "PGP Public Key Download");
            }
            Common.streamCopier(c_key.download(key_vrl.getPath(), 0L, -1L, true), baos, false, true, true);
            c_key.logout();
            key = baos.toByteArray();
            if (System.getProperty("crushftp.v10_beta", "false").equals("true")) {
                Properties p2 = new Properties();
                p2.put("bytes", key);
                if (System.getProperty("crushftp.v11_beta", "false").equals("true")) {
                    p2.put("name", "");
                    p2.put("type", "pgp");
                }
                Common.System2.put("crushftp.keystores." + keyLocation.toUpperCase().replace('\\', '/'), p2);
            }
        }
        return key;
    }
}

