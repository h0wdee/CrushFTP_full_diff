/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jdom.Element
 */
package com.crushftp.client;

import com.crushftp.client.Base64;
import com.crushftp.client.Common;
import com.crushftp.client.GenericClient;
import com.crushftp.client.URLConnection;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Vector;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.jdom.Element;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class S3Client
extends GenericClient {
    Vector in_progress = new Vector();
    public static long ram_used_download = 0L;
    public static long ram_used_upload = 0L;
    static Object ram_lock = new Object();
    SimpleDateFormat yyyyMMddtHHmmssSSSZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
    SimpleDateFormat yyyyMMddtHHmmssZ = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US);
    SimpleDateFormat yyyyMMddtHHmmssSSS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S", Locale.US);
    SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
    SecretKeySpec secretKey = null;
    String region_host = "s3.amazonaws.com";
    private String region_name = "us-east-1";
    public static Properties valid_credentials_cache = new Properties();
    String cache_reference = null;
    public static Properties s3_global_cache = new Properties();
    public static Properties s3_global_cache_counts = new Properties();
    public static Properties s3_imdsv2 = new Properties();
    String http_protocol = "https";
    public static Object s3_buffer_lock = new Object();
    public static String[] s3_fields = new String[]{"username", "password", "timeout", "real_password", "real_token", "iam_expire", "real_username", "s3_accelerate", "no_bucket_check", "s3_bucket_in_path", "s3_sha256", "server_side_encrypt", "server_side_encrypt_kms", "s3_buffer", "s3_buffer_download", "s3_threads_upload", "s3_threads_download", "vfs_user", "baseURL", "s3_partial", "multithreaded_s3_download", "s3_max_buffer_download", "error", "error_msg", "s3_acl", "s3_storage_class", "s3_stat_head_calls", "s3_meta_md5_and_upload_by", "s3_list_v2", "uploaded_by", "uploaded_md5", "login_error", "s3_lookup_meta_modified", "s3_rename_allowed_speed"};
    protected static final char[] hexArray = "0123456789abcdef".toCharArray();

    public static void main(String[] args) throws IOException {
        System.setProperty("crushtunnel.debug", "2");
        System.getProperties().put("crushftp.s3_sha256", "false");
    }

    public S3Client(String url, String header, Vector log) {
        super(header, log);
        this.fields = s3_fields;
        if (!this.yyyyMMddtHHmmssZ.getTimeZone().getID().equals("GMT")) {
            this.yyyyMMddtHHmmssZ.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        if (!this.yyyyMMddtHHmmssSSSZ.getTimeZone().getID().equals("GMT")) {
            this.yyyyMMddtHHmmssSSSZ.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        if (!this.yyyyMMddtHHmmssSSS.getTimeZone().getID().equals("GMT")) {
            this.yyyyMMddtHHmmssSSS.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        if (!this.yyyyMMddHHmmss.getTimeZone().getID().equals("GMT")) {
            this.yyyyMMddHHmmss.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        if (!url.endsWith("/")) {
            url = String.valueOf(url) + "/";
        }
        this.url = url;
        VRL s3_vrl = new VRL(url);
        if (s3_vrl.getPort() == 80 || s3_vrl.getPort() == 8080 || s3_vrl.getPort() == 9090 || String.valueOf(s3_vrl.getPort()).endsWith("80")) {
            this.http_protocol = "http";
        }
        this.region_host = s3_vrl.getHost().toLowerCase();
        if (s3_vrl.getPort() != 443) {
            this.region_host = String.valueOf(s3_vrl.getHost().toLowerCase()) + ":" + s3_vrl.getPort();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String login2(String username, String password, String clientid) throws Exception {
        Properties caches;
        this.cache_reference = String.valueOf(this.config.getProperty("vfs_user")) + ":" + this.config.getProperty("baseURL") + (System.getProperty("crushftp.s3_global_cache", "true").equals("true") ? "" : Common.makeBoundary(10));
        if (!s3_global_cache.containsKey(this.cache_reference)) {
            caches = new Properties();
            caches.put("list_cache", new Properties());
            caches.put("cache_resume", new Properties());
            caches.put("stat_cache", new Properties());
            caches.put("recently_created_folder_cache", new Properties());
            caches.put("time", String.valueOf(System.currentTimeMillis()));
            Properties properties = s3_global_cache;
            synchronized (properties) {
                s3_global_cache.put(this.cache_reference, caches);
            }
        }
        caches = s3_global_cache;
        synchronized (caches) {
            s3_global_cache_counts.put(this.cache_reference, String.valueOf(Integer.parseInt(s3_global_cache_counts.getProperty(this.cache_reference, "0")) + 1));
        }
        this.config.put("username", username.trim());
        this.config.put("password", password.trim());
        try {
            int timeout = Integer.parseInt(this.config.getProperty("timeout", "20000"));
            if (timeout < 10000) {
                this.config.put("timeout", "10000");
            }
        }
        catch (Exception e) {
            this.config.put("timeout", "20000");
        }
        this.updateIamAuth();
        String md5hash = Common.getMD5(new ByteArrayInputStream((String.valueOf(username) + password + clientid).getBytes()));
        this.secretKey = new SecretKeySpec(this.config.getProperty("real_password", this.config.getProperty("password")).getBytes("UTF8"), "HmacSHA1");
        if (this.config.getProperty("s3_accelerate", "false").equals("true")) {
            this.region_name = this.region_host.substring(3).substring(0, this.region_host.substring(3).indexOf("."));
            this.region_host = "s3-accelerate.amazonaws.com";
        } else {
            this.region_name = this.region_host.substring(3).substring(0, this.region_host.substring(3).indexOf("."));
        }
        if (this.region_name.equals("amazonaws")) {
            this.region_name = "us-east-1";
        }
        if (!valid_credentials_cache.containsKey(md5hash) || System.getProperty("crushftp.s3.always_auth", "false").equals("false")) {
            String result = "";
            if (this.config.getProperty("no_bucket_check", "false").equals("true")) {
                String path0 = this.lower(new VRL(this.url).getPath());
                String tempurl = Common.getBaseUrl(this.url, false);
                if (!this.config.getProperty("s3_bucket_in_path", "false").equals("true")) {
                    this.url = tempurl;
                }
                String path = path0;
                String bucketName = "";
                if (!path.equals("/") && !(path = path.substring((bucketName = path.substring(1, path.indexOf("/", 1))).length() + 1)).endsWith("/")) {
                    path = String.valueOf(path) + "/";
                }
                String query = "?delimiter=%2F&marker=&max-keys=1&prefix=" + this.handle_path_special_chars(path.substring(1), true);
                if (this.config.getProperty("s3_bucket_in_path", "false").equals("true")) {
                    query = String.valueOf(bucketName) + "/" + query;
                }
                URLConnection urlc = null;
                urlc = URLConnection.openConnection(new VRL(String.valueOf(this.http_protocol) + "://" + (bucketName.equals("") || this.config.getProperty("s3_bucket_in_path", "false").equals("true") ? "" : String.valueOf(bucketName) + ".") + this.region_host + "/" + query), this.config);
                urlc.setRemoveDoubleEncoding(true);
                urlc.setDoOutput(false);
                urlc.setReadTimeout(Integer.parseInt(this.config.getProperty("timeout", "0")));
                this.doStandardAmazonAlterations(urlc, null, bucketName);
                int code = urlc.getResponseCode();
                result = URLConnection.consumeResponse(urlc.getInputStream());
                urlc.disconnect();
                if (code < 200 || code > 299) {
                    Properties error_config = new Properties();
                    error_config.put("login_error", "true");
                    result = this.getErrorInfo(result, code, urlc.getConfig("canonical_request"), error_config);
                    this.log(String.valueOf(result) + "\r\n");
                    throw new IOException(result);
                }
            } else {
                URLConnection urlc = this.doAction("HEAD", new VRL(this.url).getPath(), null, false, true, this.config.getProperty("s3_bucket_in_path", "false").equals("true"));
                int code = urlc.getResponseCode();
                if (code < 200 || code > 299) {
                    urlc.disconnect();
                    urlc = this.doAction("GET", new VRL(this.url).getPath(), null, false, true, this.config.getProperty("s3_bucket_in_path", "false").equals("true"));
                    code = urlc.getResponseCode();
                    if (code < 200 || code > 299) {
                        result = URLConnection.consumeResponse(urlc.getInputStream());
                        Properties error_config = new Properties();
                        error_config.put("login_error", "true");
                        result = this.getErrorInfo(result, code, urlc.getConfig("canonical_request"), error_config);
                        urlc.disconnect();
                        this.log(String.valueOf(result) + "\r\n");
                        throw new IOException(result);
                    }
                }
                urlc.disconnect();
            }
            valid_credentials_cache.put(md5hash, "Success");
        }
        this.config.put("logged_out", "false");
        return valid_credentials_cache.getProperty(md5hash);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void logout() throws Exception {
        if (this.cache_reference == null) {
            this.cache_reference = String.valueOf(this.config.getProperty("vfs_user")) + ":" + this.config.getProperty("baseURL");
        }
        Cloneable cloneable = s3_global_cache;
        synchronized (cloneable) {
            s3_global_cache_counts.put(this.cache_reference, String.valueOf(Integer.parseInt(s3_global_cache_counts.getProperty(this.cache_reference, "0")) - 1));
            if (Integer.parseInt(s3_global_cache_counts.getProperty(this.cache_reference, "0")) == 0) {
                s3_global_cache.remove(this.cache_reference);
                s3_global_cache_counts.remove(this.cache_reference);
            }
        }
        cloneable = this.in_progress;
        synchronized (cloneable) {
            while (this.in_progress.size() > 0) {
                Thread t = (Thread)this.in_progress.remove(0);
                t.interrupt();
            }
        }
        this.config.put("logged_out", "true");
        this.close();
    }

    @Override
    public Vector list(String path0, Vector list) throws Exception {
        return this.list(path0, list, 1000);
    }

    public Vector list(String path0, Vector list, int max_keys) throws Exception {
        Properties ctmp;
        path0 = this.lower(path0);
        String tempurl = Common.getBaseUrl(this.url, false);
        if (!this.config.getProperty("s3_bucket_in_path", "false").equals("true")) {
            this.url = tempurl;
        }
        String path = path0;
        String bucketName = "";
        if (!path.equals("/") && !(path = path.substring((bucketName = path.substring(1, path.indexOf("/", 1))).length() + 1)).endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        Vector<Properties> list2 = null;
        if (this.get_cache_item("list_cache").containsKey(path0)) {
            Properties ctmp2 = (Properties)this.get_cache_item("list_cache").get(path0);
            if (System.currentTimeMillis() - Long.parseLong(ctmp2.getProperty("time")) < 30000L) {
                list2 = (Vector<Properties>)Common.CLONE(ctmp2.get("o"));
            }
        }
        boolean allow_cache = false;
        if (list2 == null) {
            allow_cache = true;
            String last_key = null;
            list2 = new Vector<Properties>();
            int xx = 0;
            while (xx < 2000) {
                if (this.config.getProperty("logged_out", "false").equals("true")) {
                    throw new Exception("Error: Cancel dir listing. The client is already closed.");
                }
                String query = "?delimiter=%2F" + (last_key != null ? "&marker=" + this.handle_path_special_chars(last_key, true) : "") + "&max-keys=" + max_keys + "&prefix=" + this.handle_path_special_chars(path.substring(1), true);
                if (this.config.getProperty("s3_list_v2", "false").equals("true")) {
                    query = "?" + (last_key != null ? "continuation-token=" + this.handle_path_special_chars(last_key, true) + "&" : "") + "delimiter=%2F" + "&list-type=2" + "&max-keys=" + max_keys + "&prefix=" + this.handle_path_special_chars(path.substring(1), true);
                }
                if (this.config.getProperty("s3_bucket_in_path", "false").equals("true")) {
                    query = String.valueOf(bucketName) + "/" + query;
                }
                String result = "";
                URLConnection urlc = null;
                int retries = 0;
                while (retries < 20) {
                    block43: {
                        if (retries > 0) {
                            Thread.sleep(this.set_power_of_two_delay(1L, retries));
                        }
                        urlc = URLConnection.openConnection(new VRL(String.valueOf(this.http_protocol) + "://" + (bucketName.equals("") || this.config.getProperty("s3_bucket_in_path", "false").equals("true") ? "" : String.valueOf(bucketName) + ".") + this.region_host + "/" + query), this.config);
                        urlc.setRemoveDoubleEncoding(true);
                        urlc.setDoOutput(false);
                        int timeout = Integer.parseInt(this.config.getProperty("timeout", "20000"));
                        if (timeout < 10000) {
                            timeout = 10000;
                        }
                        urlc.setReadTimeout(timeout);
                        this.doStandardAmazonAlterations(urlc, null, bucketName);
                        int code = 0;
                        try {
                            code = urlc.getResponseCode();
                        }
                        catch (SocketTimeoutException e) {
                            this.log(e);
                            this.log("Number of retry listing : " + retries++);
                            break block43;
                        }
                        result = URLConnection.consumeResponse(urlc.getInputStream());
                        urlc.disconnect();
                        if (code >= 200 && code <= 299) break;
                        result = this.getErrorInfo(result, code, urlc.getConfig("canonical_request"), null);
                        this.log(String.valueOf(result) + "\r\n");
                        if (retries >= 30 || result.indexOf("InternalError") < 0) {
                            throw new IOException(result);
                        }
                        this.log("Retrying failed dir listing query...");
                        Thread.sleep(retries * 1000);
                    }
                    ++retries;
                }
                if (result.length() == 0) {
                    if (max_keys != 1) {
                        Properties ctmp3 = new Properties();
                        ctmp3.put("time", String.valueOf(System.currentTimeMillis()));
                        ctmp3.put("o", Common.CLONE(list2));
                        this.get_cache_item("list_cache").put(path0, ctmp3);
                    }
                    return list;
                }
                int item_count = 0;
                Element root = Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(result.getBytes("UTF8"))).getRootElement();
                if (bucketName.toString().equals("")) {
                    Element buckets = S3Client.getElement(root, "Buckets");
                    List buckets2 = buckets.getChildren();
                    item_count = buckets2.size();
                    int x = 0;
                    while (x < buckets2.size()) {
                        Element bucket = (Element)buckets2.get(x);
                        this.log(String.valueOf(S3Client.getKeyText(bucket, "Name")) + "\r\n");
                        Date d = this.yyyyMMddtHHmmssSSS.parse(S3Client.getKeyText(bucket, "CreationDate"));
                        String line = "drwxrwxrwx   1    owner   group   0   " + this.yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + S3Client.getKeyText(bucket, "Name");
                        Properties stat = S3Client.parseStat(line);
                        stat.put("url", String.valueOf(Common.url_decode(tempurl)) + (bucketName.toString().equals("") ? "" : String.valueOf(bucketName.toString()) + "/") + path.substring(1) + stat.getProperty("name"));
                        list.addElement(stat);
                        ++x;
                    }
                } else {
                    List prefixes = S3Client.getElements(root, "CommonPrefixes");
                    item_count += prefixes.size();
                    int x = 0;
                    while (prefixes != null && x < prefixes.size()) {
                        Element content = (Element)prefixes.get(x);
                        String name = S3Client.getKeyText(content, "Prefix");
                        name = name.substring(0, name.length() - 1);
                        if (!System.getProperty("crushftp.lowercase_all_s3_paths", "false").equals("true") || name.equals(name.toLowerCase())) {
                            if (!this.config.getProperty("s3_list_v2", "false").equals("true")) {
                                last_key = name;
                            }
                            this.log("S3_CLIENT", 2, String.valueOf(name) + "\r\n");
                            boolean folder = true;
                            Date d = new Date(0L);
                            String line = String.valueOf(folder ? "d" : "-") + "rwxrwxrwx   1    owner   group   0   " + this.yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + name;
                            Properties stat = S3Client.parseStat(line);
                            stat.put("check_all_recursive_deletes", "true");
                            stat.put("url", String.valueOf(Common.url_decode(tempurl)) + (bucketName.toString().equals("") ? "" : String.valueOf(bucketName.toString()) + "/") + path.substring(1) + stat.getProperty("name"));
                            this.log("S3_CLIENT", 2, stat + "\r\n");
                            list2.addElement(stat);
                        }
                        ++x;
                    }
                    List contents = S3Client.getElements(root, "Contents");
                    item_count += contents.size();
                    int x2 = 0;
                    while (x2 < contents.size()) {
                        Element content = (Element)contents.get(x2);
                        String name = S3Client.getKeyText(content, "Key");
                        if (!(max_keys != 1 && name.equals(path.substring(1)) || System.getProperty("crushftp.lowercase_all_s3_paths", "false").equals("true") && !name.equals(name.toLowerCase()))) {
                            Properties meta;
                            boolean folder;
                            if (!this.config.getProperty("s3_list_v2", "false").equals("true")) {
                                last_key = name;
                            }
                            this.log("S3_CLIENT", 2, String.valueOf(name) + "\r\n");
                            boolean bl = folder = name.endsWith("/") || urlc.getHeaderField("Content-Type").indexOf("x-directory") >= 0;
                            if (folder) {
                                name = name.substring(0, name.length() - 1);
                            }
                            String lastModified = S3Client.getKeyText(content, "LastModified");
                            Date d = null;
                            SimpleDateFormat sdf_temp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                            sdf_temp.setTimeZone(TimeZone.getTimeZone("GMT"));
                            d = lastModified.endsWith("Z") && !lastModified.contains(".") ? sdf_temp.parse(lastModified) : this.yyyyMMddtHHmmssSSS.parse(lastModified);
                            if (this.config.getProperty("s3_lookup_meta_modified", "false").equals("true") && (meta = this.getMetadata("/" + bucketName.toString() + path + name)) != null && meta.containsKey("x-amz-meta-modified")) {
                                d = new Date(Long.parseLong(meta.getProperty("x-amz-meta-modified")));
                            }
                            String line = String.valueOf(folder ? "d" : "-") + "rwxrwxrwx   1    owner   group   " + S3Client.getKeyText(content, "Size") + "   " + this.yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + name;
                            Properties stat = S3Client.parseStat(line);
                            stat.put("url", String.valueOf(Common.url_decode(tempurl)) + (bucketName.toString().equals("") ? "" : String.valueOf(bucketName.toString()) + "/") + path.substring(1) + stat.getProperty("name"));
                            this.log("S3_CLIENT", 2, stat + "\r\n");
                            list2.addElement(stat);
                        }
                        ++x2;
                    }
                    if (this.config.getProperty("s3_list_v2", "false").equals("true")) {
                        Element next = S3Client.getElement(root, "NextContinuationToken");
                        last_key = next != null ? next.getValue() : null;
                    }
                }
                try {
                    boolean isTruncated = S3Client.getElement(root, "IsTruncated").getText().equalsIgnoreCase("true");
                    if (isTruncated && max_keys > 1 && item_count < max_keys) {
                        isTruncated = false;
                    }
                    if (!isTruncated || last_key == null) break;
                    if (max_keys == 1) {
                    }
                }
                catch (Exception e) {}
                break;
                ++xx;
            }
        }
        list.addAll(list2);
        if (max_keys != 1 && allow_cache) {
            ctmp = new Properties();
            ctmp.put("time", String.valueOf(System.currentTimeMillis()));
            ctmp.put("o", Common.CLONE(list2));
            this.get_cache_item("list_cache").put(path0, ctmp);
        }
        if (!bucketName.equals("") && max_keys != 1) {
            list2 = null;
            if (this.get_cache_item("cache_resume").containsKey(path0)) {
                ctmp = (Properties)this.get_cache_item("cache_resume").get(path0);
                if (System.currentTimeMillis() - Long.parseLong(ctmp.getProperty("time")) < 30000L) {
                    list2 = (Vector)Common.CLONE(ctmp.get("o"));
                }
            }
            boolean s3_partial = System.getProperty("crushftp.s3_partial", "true").equals("true");
            if (this.config.containsKey("s3_partial")) {
                s3_partial = this.config.getProperty("s3_partial", "true").equals("true");
            }
            if (list2 == null && s3_partial) {
                list2 = this.get_uploads_in_progress_or_failed_uploads(list, path, bucketName);
                if (list2 != null) {
                    Properties ctmp4 = new Properties();
                    ctmp4.put("time", String.valueOf(System.currentTimeMillis()));
                    ctmp4.put("o", Common.CLONE(list2));
                    this.get_cache_item("cache_resume").put(path0, ctmp4);
                } else {
                    list2 = new Vector<Properties>();
                }
            } else if (list2 == null) {
                list2 = new Vector<Properties>();
            }
            list.addAll(list2);
        }
        return list;
    }

    private Vector get_uploads_in_progress_or_failed_uploads(Vector list, String path, String bucketName) throws Exception {
        path = this.lower(path);
        String result = "";
        String query = "";
        int retries = 0;
        while (retries < 20) {
            if (retries > 0) {
                Thread.sleep(this.set_power_of_two_delay(10L, retries));
            }
            this.log("Looking for failed transfers, or in progress transfers.");
            query = "?delimiter=%2F&prefix=" + this.handle_path_special_chars(path.substring(1), true) + "&uploads";
            boolean s3_sha256 = System.getProperty("crushftp.s3_sha256", "false").equals("true");
            if (this.config.containsKey("s3_sha256")) {
                s3_sha256 = this.config.getProperty("s3_sha256", "false").equals("true");
            }
            if (s3_sha256 || !this.config.getProperty("server_side_encrypt_kms", "").equals("")) {
                query = String.valueOf(query) + "=";
            }
            URLConnection urlc = URLConnection.openConnection(new VRL(String.valueOf(this.http_protocol) + "://" + (bucketName.equals("") || this.config.getProperty("s3_bucket_in_path", "false").equals("true") ? "" : String.valueOf(bucketName) + ".") + this.region_host + "/" + (this.config.getProperty("s3_bucket_in_path", "false").equals("true") ? String.valueOf(bucketName) + "/" : "") + query), this.config);
            urlc.setDoOutput(false);
            this.doStandardAmazonAlterations(urlc, null, bucketName);
            int code = urlc.getResponseCode();
            result = URLConnection.consumeResponse(urlc.getInputStream());
            urlc.disconnect();
            if (code >= 200 && code <= 299) break;
            result = this.getErrorInfo(result, code, urlc.getConfig("canonical_request"), null);
            this.log(String.valueOf(result) + "\r\n");
            if (retries >= 30 || result.indexOf("InternalError") < 0) {
                throw new IOException(result);
            }
            this.log("Retrying failed transfer lookup...");
            ++retries;
        }
        Vector<Properties> list2 = new Vector<Properties>();
        if (result.length() == 0) {
            return null;
        }
        Element root = Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(result.getBytes("UTF8"))).getRootElement();
        List uploads = S3Client.getElements(root, "Upload");
        this.log("S3_CLIENT", 2, "In progress uploads:" + uploads.size());
        int x = 0;
        while (x < uploads.size()) {
            Element content = (Element)uploads.get(x);
            String name = S3Client.getKeyText(content, "Key");
            this.log("S3_CLIENT", 2, "In progress upload:" + x + ":" + name);
            if ((!path.substring(1).equals("") || name.indexOf("/") <= 0) && name.startsWith(path.substring(1)) && name.indexOf("/", path.length()) <= 0) {
                this.log(String.valueOf(name) + "\r\n");
                boolean folder = name.endsWith("/");
                if (folder) {
                    name = name.substring(0, name.length() - 1);
                }
                Date d = this.yyyyMMddtHHmmssSSS.parse(S3Client.getKeyText(content, "Initiated"));
                query = String.valueOf(this.handle_path_special_chars(name, false)) + "?uploadId=" + S3Client.getKeyText(content, "UploadId");
                URLConnection urlc = URLConnection.openConnection(new VRL(String.valueOf(this.http_protocol) + "://" + (bucketName.equals("") || this.config.getProperty("s3_bucket_in_path", "false").equals("true") ? "" : String.valueOf(bucketName) + ".") + this.region_host + "/" + (this.config.getProperty("s3_bucket_in_path", "false").equals("true") ? String.valueOf(bucketName) + "/" : "") + query), this.config);
                urlc.setRemoveDoubleEncoding(true);
                urlc.setDoOutput(false);
                this.doStandardAmazonAlterations(urlc, null, bucketName);
                int code = urlc.getResponseCode();
                result = URLConnection.consumeResponse(urlc.getInputStream());
                urlc.disconnect();
                if (code >= 200 && code <= 299) {
                    Element partRoot = Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(result.getBytes("UTF8"))).getRootElement();
                    ArrayList parts = S3Client.getElements(partRoot, "Part");
                    if (parts == null) {
                        parts = new ArrayList();
                    }
                    long totalSize = 0L;
                    Vector<Properties> resumeParts = new Vector<Properties>();
                    int xx = 0;
                    while (xx < parts.size()) {
                        Element part = (Element)parts.get(xx);
                        totalSize += Long.parseLong(S3Client.getKeyText(part, "Size"));
                        d = this.yyyyMMddtHHmmssSSS.parse(S3Client.getKeyText(part, "LastModified"));
                        Properties chunk_part = new Properties();
                        chunk_part.put("etag", S3Client.getKeyText(part, "ETag"));
                        resumeParts.addElement(chunk_part);
                        ++xx;
                    }
                    String line = String.valueOf(folder ? "d" : "-") + "-w--w--w-   " + parts.size() + "    owner   group   " + totalSize + "   " + this.yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + name;
                    Properties stat = S3Client.parseStat(line);
                    stat.put("resumeParts", resumeParts);
                    stat.put("uploadId", S3Client.getKeyText(content, "UploadId"));
                    String path2 = path;
                    if (path2.endsWith(stat.getProperty("name"))) {
                        path2 = Common.all_but_last(path2);
                    }
                    stat.put("url", String.valueOf(Common.url_decode(this.url)) + (bucketName.toString().equals("") ? "" : String.valueOf(bucketName.toString()) + "/") + path2.substring(1) + stat.getProperty("name"));
                    this.log(stat + "\r\n");
                    list2.addElement(stat);
                }
            }
            ++x;
        }
        return list2;
    }

    @Override
    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        path = this.lower(path);
        this.in = this.download4(path, startPos, endPos, binary, null);
        return this.in;
    }

    protected InputStream download4(String path0_tmp, long startPos0, long endPos, boolean binary, final String bucketName0) throws Exception {
        final String path0 = this.lower(path0_tmp);
        final long startPos = startPos0 < 0L ? 0L : startPos0;
        final Properties stat = this.stat(path0);
        if (stat.getProperty("type", "FILE").toUpperCase().equals("FILE") && stat.getProperty("size", "").equals("0")) {
            return new ByteArrayInputStream(new byte[0]);
        }
        int s3_threads_download_temp = 3;
        try {
            if (!this.config.getProperty("s3_threads_download", "3").equals("")) {
                s3_threads_download_temp = Integer.parseInt(this.config.getProperty("s3_threads_download", "3"));
            }
        }
        catch (Exception e) {
            this.log("S3_CLIENT", 2, e);
        }
        final int s3_threads_download = s3_threads_download_temp;
        int s3_buffer_download_temp = 3;
        try {
            if (!this.config.getProperty("s3_buffer_download", "3").equals("")) {
                s3_buffer_download_temp = Integer.parseInt(this.config.getProperty("s3_buffer_download", "3"));
            }
        }
        catch (Exception e) {
            this.log("S3_CLIENT", 2, e);
        }
        final int s3_buffer_download = s3_buffer_download_temp;
        Properties socks = Common.getConnectedSocks(false);
        final Socket sock1 = (Socket)socks.get("sock1");
        final Socket sock2 = (Socket)socks.get("sock2");
        final Vector<String> chunks_needed = new Vector<String>();
        if (stat.containsKey("segments")) {
            chunks_needed.addAll((Vector)stat.get("segments"));
        }
        final Properties chunks = new Properties();
        final Properties status = new Properties();
        long len = Long.parseLong(stat.getProperty("size"));
        if (endPos > 0L && endPos < len) {
            len = endPos;
        }
        final long lenF = len;
        final boolean segmented = stat.containsKey("segments");
        if (!stat.containsKey("segments")) {
            if ((this.config.getProperty("multithreaded_s3_download", "true").equals("false") || s3_threads_download == 1) && endPos <= 0L) {
                String bucketName = bucketName0;
                if (!stat.containsKey("uid") || bucketName == null) {
                    String path = path0;
                    bucketName = path.substring(1);
                    if (bucketName.indexOf("?") >= 0) {
                        bucketName = bucketName.substring(0, bucketName.indexOf("?"));
                    }
                    if (bucketName.indexOf("/") >= 0) {
                        bucketName = bucketName.substring(0, bucketName.indexOf("/"));
                        path = path.substring(path.indexOf("/", 1));
                    }
                    if (path.equals("/" + bucketName)) {
                        path = "/";
                    }
                    stat.put("uid", path.substring(1));
                }
                URLConnection urlc = this.doAction("GET", "/" + bucketName + "/" + stat.getProperty("uid"), null, false, true, this.config.getProperty("s3_bucket_in_path", "false").equals("true"));
                urlc.enableHighLatencyBuffer();
                long offset = startPos;
                urlc.setRequestProperty("Range", "bytes=" + offset + "-");
                int code = urlc.getResponseCode();
                if (code < 200 || code > 299) {
                    String result = URLConnection.consumeResponse(urlc.getInputStream());
                    result = this.getErrorInfo(result, code, urlc.getConfig("canonical_request"), null);
                    urlc.disconnect();
                    this.log(String.valueOf(result) + "\r\n");
                    throw new IOException(result);
                }
                this.in = urlc.getInputStream();
                return this.in;
            }
            long pos = startPos;
            long amount = (long)s3_buffer_download * 1024L * 1024L;
            if (pos < 0L) {
                pos = 0L;
            }
            int i = 1;
            while (pos < len) {
                chunks_needed.addElement(String.valueOf(i) + ":" + pos + "-" + (pos + amount));
                pos += amount + 1L;
                ++i;
            }
        }
        if (startPos > 0L && segmented) {
            boolean delete = false;
            int x = chunks_needed.size() - 1;
            while (x >= 0) {
                if (delete) {
                    chunks_needed.remove(x);
                } else {
                    String part = chunks_needed.elementAt(x).toString();
                    if (startPos >= Long.parseLong(part.split(":")[1].split("-")[0]) && startPos <= Long.parseLong(part.split(":")[1].split("-")[1])) {
                        if (startPos == Long.parseLong(part.split(":")[1].split("-")[1])) {
                            chunks_needed.remove(x);
                        }
                        delete = true;
                    }
                }
                --x;
            }
        }
        status.put("first", "true");
        status.put("ram", "0");
        status.put("current_pos", "0");
        this.log("Chunks needed:" + chunks_needed);
        Runnable grabChunk = new Runnable(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                String s_master = Thread.currentThread().getName();
                String last_chunk_info = "";
                long s3_max_buffer_download = 100L;
                try {
                    if (!S3Client.this.config.getProperty("s3_max_buffer_download", "100").equals("")) {
                        s3_max_buffer_download = Long.parseLong(S3Client.this.config.getProperty("s3_max_buffer_download", "100"));
                    }
                }
                catch (Exception e) {
                    S3Client.this.log("S3_CLIENT", 2, e);
                }
                while (chunks_needed.size() > 0 && !status.containsKey("error")) {
                    block47: {
                        try {
                            if (ram_used_download / 1024L / 1024L > s3_max_buffer_download && !status.containsKey("error")) {
                                Thread.sleep(1000L);
                            }
                        }
                        catch (InterruptedException e) {
                            // empty catch block
                        }
                        if (status.containsKey("error")) break;
                        String part = null;
                        String original_part = null;
                        boolean first = false;
                        Vector vector = chunks_needed;
                        synchronized (vector) {
                            first = status.getProperty("first", "false").equals("true");
                            status.put("first", "false");
                            if (chunks_needed.size() > 0) {
                                part = chunks_needed.remove(0).toString();
                            }
                        }
                        if (part == null) break;
                        original_part = part;
                        String bucketName = bucketName0;
                        if (!stat.containsKey("uid") || bucketName == null) {
                            String path = path0;
                            bucketName = path.substring(1);
                            if (bucketName.indexOf("?") >= 0) {
                                bucketName = bucketName.substring(0, bucketName.indexOf("?"));
                            }
                            if (bucketName.indexOf("/") >= 0) {
                                bucketName = bucketName.substring(0, bucketName.indexOf("/"));
                                path = path.substring(path.indexOf("/", 1));
                            }
                            if (path.equals("/" + bucketName)) {
                                path = "/";
                            }
                            stat.put("uid", path.substring(1));
                        }
                        try {
                            String s;
                            long start;
                            ByteArrayOutputStream baos;
                            URLConnection urlc;
                            block46: {
                                if (part.indexOf(":-1-") >= 0) {
                                    part = Common.replace_str(part, ":-1-", ":0-");
                                }
                                try {
                                    if (!status.containsKey("error") && !first && Long.parseLong(part.split(":")[1].split("-")[0]) - Long.parseLong(status.getProperty("current_pos", "0")) > (long)s3_buffer_download * 1024L * 1024L * 4L * (long)s3_threads_download) {
                                        String s2 = String.valueOf(s_master.substring(0, s_master.lastIndexOf(":") + 1)) + " PAUSED : Chunk pos:" + Long.parseLong(part.split(":")[1].split("-")[0]) + " Versus current pos:" + Long.parseLong(status.getProperty("current_pos", "0")) + " current ram usage:" + Common.format_bytes_short(Long.parseLong(status.getProperty("ram"))) + " of " + Common.format_bytes_short(ram_used_download);
                                        Thread.currentThread().setName(s2);
                                        Thread.sleep(100L);
                                        Vector vector2 = chunks_needed;
                                        synchronized (vector2) {
                                            if (chunks_needed.size() > s3_threads_download + 1) {
                                                chunks_needed.insertElementAt(original_part, s3_threads_download);
                                            } else {
                                                chunks_needed.addElement(original_part);
                                            }
                                            continue;
                                        }
                                    }
                                }
                                catch (NumberFormatException e) {
                                    S3Client.this.log("Part:" + part);
                                    S3Client.this.log("Buffer:" + s3_buffer_download);
                                    S3Client.this.log("Threads:" + s3_threads_download);
                                    S3Client.this.log("Status:" + status);
                                    throw e;
                                }
                                urlc = S3Client.this.doAction("GET", "/" + bucketName + "/" + stat.getProperty("uid") + (segmented ? "_" + part.split(":")[0] : ""), null, false, true, S3Client.this.config.getProperty("s3_bucket_in_path", "false").equals("true"));
                                urlc.enableHighLatencyBuffer();
                                long offset = startPos - Long.parseLong(part.split(":")[1].split("-")[0]);
                                if (!segmented) {
                                    urlc.setRequestProperty("Range", "bytes=" + part.split(":")[1]);
                                } else if (first && segmented && offset != 0L) {
                                    part = String.valueOf(part.split(":")[0]) + ":" + (Long.parseLong(part.split(":")[1].split("-")[0]) + offset) + "-" + part.split(":")[1].split("-")[1];
                                    urlc.setRequestProperty("Range", "bytes=" + offset + "-");
                                }
                                if (status.containsKey("error")) break;
                                int code = urlc.getResponseCode();
                                if (code < 200 || code > 299) {
                                    String result = URLConnection.consumeResponse(urlc.getInputStream());
                                    urlc.disconnect();
                                    result = S3Client.this.getErrorInfo(result, code, urlc.getConfig("canonical_request"), null);
                                    S3Client.this.log(String.valueOf(result) + "\r\n");
                                    throw new IOException(result);
                                }
                                last_chunk_info = part.split(":")[1].split("-")[0];
                                baos = new ByteArrayOutputStream();
                                InputStream inp = urlc.getInputStream();
                                start = System.currentTimeMillis();
                                try {
                                    try {
                                        byte[] b = new byte[65535];
                                        int bytesRead = 0;
                                        while (bytesRead >= 0) {
                                            bytesRead = inp.read(b);
                                            if (bytesRead >= 0) {
                                                baos.write(b, 0, bytesRead);
                                            }
                                            if (!Common.log("S3_CLIENT", 2, "")) continue;
                                            s = String.valueOf(s_master.substring(0, s_master.lastIndexOf(":") + 1)) + " Chunk " + last_chunk_info + " " + " download_time (" + (System.currentTimeMillis() - start) + "ms) (chunk_size:" + baos.size() + ") " + Common.format_bytes_short(Long.parseLong(status.getProperty("ram"))) + " of " + Common.format_bytes_short(ram_used_download);
                                            Thread.currentThread().setName(s);
                                        }
                                    }
                                    catch (Exception e) {
                                        chunks_needed.insertElementAt(original_part, 0);
                                        Common.log("SERVER", 1, e + ", retrying chunk " + s_master + ":" + last_chunk_info);
                                        Common.log("SERVER", 1, e);
                                        try {
                                            inp.close();
                                        }
                                        catch (Exception exception) {
                                            // empty catch block
                                        }
                                        baos.close();
                                        break block46;
                                    }
                                }
                                catch (Throwable throwable) {
                                    try {
                                        inp.close();
                                    }
                                    catch (Exception exception) {
                                        // empty catch block
                                    }
                                    baos.close();
                                    throw throwable;
                                }
                                try {
                                    inp.close();
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                                baos.close();
                            }
                            urlc.disconnect();
                            S3Client.this.log("S3_CLIENT", 2, String.valueOf(s_master) + ":chunk download time " + (System.currentTimeMillis() - start) + "ms for chunk:" + last_chunk_info);
                            if (status.containsKey("error")) break block47;
                            Object object = ram_lock;
                            synchronized (object) {
                                byte[] b = baos.toByteArray();
                                chunks.put(part.split(":")[1].split("-")[0], b);
                                status.put("ram", String.valueOf(Long.parseLong(status.getProperty("ram")) + (long)b.length));
                                s = String.valueOf(s_master.substring(0, s_master.lastIndexOf(":") + 1)) + " Chunk " + last_chunk_info + " " + Common.format_bytes_short(Long.parseLong(status.getProperty("ram"))) + " of " + Common.format_bytes_short(ram_used_download += (long)b.length);
                                Thread.currentThread().setName(s);
                            }
                        }
                        catch (Exception e) {
                            status.put("error", e);
                            S3Client.this.log("S3_CLIENT", 2, e);
                        }
                    }
                    if (status.getProperty("run_once", "false").equals("true")) break;
                }
            }
        };
        Runnable downloadChunks = new Runnable(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             * Unable to fully structure code
             * Enabled aggressive block sorting
             * Enabled unnecessary exception pruning
             * Enabled aggressive exception aggregation
             */
            @Override
            public void run() {
                s_master = Thread.currentThread().getName();
                S3Client.this.in_progress.addElement(Thread.currentThread());
                current_pos = startPos;
                if (current_pos < 0L) {
                    current_pos = 0L;
                }
                try {
                    try {
                        out_tmp = sock2.getOutputStream();
                        last_chunk_time = System.currentTimeMillis();
                        while (true) {
                            if (current_pos >= lenF || status.containsKey("error") || sock1.isClosed()) {
                                if (!status.containsKey("error")) break;
                                throw (Exception)status.get("error");
                            }
                            s = String.valueOf(s_master.substring(0, s_master.lastIndexOf(":") + 1)) + "Waiting for " + current_pos + " and using " + Common.format_bytes_short(Long.parseLong(status.getProperty("ram"))) + " of " + Common.format_bytes_short(S3Client.ram_used_download);
                            Thread.currentThread().setName(s);
                            if (chunks.containsKey(String.valueOf(current_pos))) {
                                S3Client.this.log("S3_CLIENT", 2, String.valueOf(s_master) + ":waited " + (System.currentTimeMillis() - last_chunk_time) + "ms for chunk pos:" + current_pos);
                                b = null;
                                var9_9 = S3Client.ram_lock;
                                synchronized (var9_9) {
                                    b = (byte[])chunks.remove(String.valueOf(current_pos));
                                    status.put("ram", String.valueOf(Long.parseLong(status.getProperty("ram")) - (long)b.length));
                                    s = String.valueOf(s_master.substring(0, s_master.lastIndexOf(":") + 1)) + Common.format_bytes_short(Long.parseLong(status.getProperty("ram"))) + " of " + Common.format_bytes_short(S3Client.ram_used_download -= (long)b.length);
                                    Thread.currentThread().setName(s);
                                }
                                last_chunk_time = System.currentTimeMillis();
                                status.put("current_pos", String.valueOf(current_pos += (long)b.length));
                                out_tmp.write(b);
                                continue;
                            }
                            Thread.sleep(10L);
                        }
                        sock2.close();
                    }
                    catch (Exception e) {
                        status.put("error", e);
                        S3Client.this.config.put("error", "" + e);
                        S3Client.this.config.put("error_msg", String.valueOf(e.getMessage()));
                        S3Client.this.log("S3_CLIENT", 2, e);
                        try {
                            S3Client.this.close();
                            sock2.close();
                            sock1.close();
                        }
                        catch (Exception var5_6) {
                            // empty catch block
                        }
                        var11_10 = S3Client.this.in_progress;
                        synchronized (var11_10) {
                            S3Client.this.in_progress.remove(Thread.currentThread());
                        }
                        chunks_needed.removeAllElements();
                        if (status.containsKey("error")) {
                            try {
                                Thread.sleep(5000L);
                            }
                            catch (InterruptedException var11_11) {
                                // empty catch block
                            }
                        }
                        keys = chunks.keys();
                        if (true) ** GOTO lbl120
                    }
                }
                catch (Throwable var10_28) {
                    keys = S3Client.this.in_progress;
                    synchronized (keys) {
                        S3Client.this.in_progress.remove(Thread.currentThread());
                    }
                    chunks_needed.removeAllElements();
                    if (status.containsKey("error")) {
                        try {
                            Thread.sleep(5000L);
                        }
                        catch (InterruptedException keys) {
                            // empty catch block
                        }
                    }
                    keys = chunks.keys();
                    if (true) ** GOTO lbl135
                }
                keys = S3Client.this.in_progress;
                synchronized (keys) {
                    S3Client.this.in_progress.remove(Thread.currentThread());
                }
                chunks_needed.removeAllElements();
                if (status.containsKey("error")) {
                    try {
                        Thread.sleep(5000L);
                    }
                    catch (InterruptedException keys) {
                        // empty catch block
                    }
                }
                keys = chunks.keys();
                if (true) ** GOTO lbl150
                do {
                    key = keys.nextElement().toString();
                    var13_19 = S3Client.ram_lock;
                    synchronized (var13_19) {
                        b = (byte[])chunks.remove(key);
                        status.put("ram", String.valueOf(Long.parseLong(status.getProperty("ram")) - (long)b.length));
                        s = String.valueOf(s_master.substring(0, s_master.lastIndexOf(":") + 1)) + Common.format_bytes_short(Long.parseLong(status.getProperty("ram"))) + " of " + Common.format_bytes_short(S3Client.ram_used_download -= (long)b.length);
                        Thread.currentThread().setName(s);
                    }
lbl120:
                    // 2 sources

                } while (keys.hasMoreElements());
                return;
                do {
                    key = keys.nextElement().toString();
                    var13_20 = S3Client.ram_lock;
                    synchronized (var13_20) {
                        b = (byte[])chunks.remove(key);
                        status.put("ram", String.valueOf(Long.parseLong(status.getProperty("ram")) - (long)b.length));
                        s = String.valueOf(s_master.substring(0, s_master.lastIndexOf(":") + 1)) + Common.format_bytes_short(Long.parseLong(status.getProperty("ram"))) + " of " + Common.format_bytes_short(S3Client.ram_used_download -= (long)b.length);
                        Thread.currentThread().setName(s);
                    }
lbl135:
                    // 2 sources

                } while (keys.hasMoreElements());
                throw var10_28;
                do {
                    key = keys.nextElement().toString();
                    var13_21 = S3Client.ram_lock;
                    synchronized (var13_21) {
                        b = (byte[])chunks.remove(key);
                        status.put("ram", String.valueOf(Long.parseLong(status.getProperty("ram")) - (long)b.length));
                        s = String.valueOf(s_master.substring(0, s_master.lastIndexOf(":") + 1)) + Common.format_bytes_short(Long.parseLong(status.getProperty("ram"))) + " of " + Common.format_bytes_short(S3Client.ram_used_download -= (long)b.length);
                        Thread.currentThread().setName(s);
                    }
lbl150:
                    // 2 sources

                } while (keys.hasMoreElements());
            }
        };
        int x = 0;
        while (x < s3_threads_download) {
            Worker.startWorker(grabChunk, "S3 chunked file downloader:" + (x + 1) + "/" + s3_threads_download + ":" + path0 + ":");
            ++x;
        }
        Worker.startWorker(downloadChunks, "S3 buffer processor:" + path0 + ":");
        this.in = sock1.getInputStream();
        return this.in;
    }

    private String getExt(String path) {
        path = this.lower(path);
        Properties mimes = Common.mimes;
        String ext = "NULL";
        if (path.toString().lastIndexOf(".") >= 0) {
            ext = path.toString().substring(path.toString().lastIndexOf(".")).toUpperCase();
        }
        if (mimes.getProperty(ext, "").equals("")) {
            ext = "NULL";
        }
        try {
            Common.updateMimes();
        }
        catch (Exception e) {
            this.log("S3_CLIENT", 2, e);
        }
        return ext;
    }

    @Override
    protected OutputStream upload3(String path_tmp, long startPos, boolean truncate, boolean binary) throws Exception {
        String path = this.lower(path_tmp);
        Vector tempResumeParts = null;
        String tempUploadId = "";
        boolean resume = false;
        boolean needCopyResume = false;
        ByteArrayOutputStream temp_buf = new ByteArrayOutputStream();
        if (startPos > 0L) {
            Properties stat = this.stat(path);
            if (stat.containsKey("resumeParts")) {
                tempResumeParts = (Vector)stat.get("resumeParts");
                tempUploadId = stat.getProperty("uploadId");
                resume = true;
            }
            if (tempResumeParts == null) {
                resume = false;
                if (Long.parseLong(stat.getProperty("size")) < 0x500000L) {
                    Common.streamCopier(this.download3(path, -1L, -1L, true), temp_buf, false, true, false);
                } else {
                    needCopyResume = true;
                }
            }
        }
        if (!resume) {
            String bucketName = path.substring(1);
            String tempPath = path;
            if (bucketName.indexOf("/") >= 0) {
                bucketName = bucketName.substring(0, bucketName.indexOf("/"));
                if (!this.config.getProperty("s3_bucket_in_path", "false").equals("true")) {
                    tempPath = tempPath.substring(tempPath.indexOf("/", 1));
                }
            }
            String result = "";
            int retries = 0;
            while (retries < 20) {
                if (retries > 0) {
                    Thread.sleep(this.set_power_of_two_delay(10L, retries));
                }
                String query = "?delimiter=%2F&prefix=" + this.handle_path_special_chars(path.substring(1), true) + "&uploads";
                boolean s3_sha256 = System.getProperty("crushftp.s3_sha256", "false").equals("true");
                if (this.config.containsKey("s3_sha256")) {
                    s3_sha256 = this.config.getProperty("s3_sha256", "false").equals("true");
                }
                if (s3_sha256 || !this.config.getProperty("server_side_encrypt_kms", "").equals("")) {
                    query = String.valueOf(query) + "=";
                }
                URLConnection urlc = URLConnection.openConnection(new VRL(String.valueOf(this.http_protocol) + "://" + (bucketName.equals("") || this.config.getProperty("s3_bucket_in_path", "false").equals("true") ? "" : String.valueOf(bucketName) + ".") + this.region_host + this.handle_path_special_chars(tempPath, false) + query), this.config);
                urlc.setRemoveDoubleEncoding(true);
                urlc.setRequestMethod("POST");
                if (!this.config.getProperty("server_side_encrypt_kms", "").equals("")) {
                    urlc.setRequestProperty("x-amz-server-side-encryption", "aws:kms");
                    urlc.setRequestProperty("x-amz-server-side-encryption-aws-kms-key-id", this.config.getProperty("server_side_encrypt_kms", ""));
                } else if (this.config.getProperty("server_side_encrypt", "false").equals("true")) {
                    urlc.setRequestProperty("x-amz-server-side-encryption", "AES256");
                }
                if (!this.config.getProperty("s3_acl", "private").equals("") && !this.config.getProperty("s3_acl", "private").equals("private")) {
                    urlc.setRequestProperty("x-amz-acl", this.config.getProperty("s3_acl", "private"));
                }
                if (!this.config.getProperty("s3_storage_class", "STANDARD").equals("STANDARD")) {
                    urlc.setRequestProperty("x-amz-storage-class", this.config.getProperty("s3_storage_class", "STANDARD"));
                }
                urlc.setDoOutput(false);
                urlc.setLength(0L);
                this.doStandardAmazonAlterations(urlc, Common.mimes.getProperty(this.getExt(path)), bucketName);
                int code = urlc.getResponseCode();
                result = URLConnection.consumeResponse(urlc.getInputStream());
                urlc.disconnect();
                if (code >= 200 && code <= 299) break;
                result = this.getErrorInfo(result, code, urlc.getConfig("canonical_request"), null);
                this.log(String.valueOf(result) + "\r\n");
                if (retries >= 30 || result.indexOf("InternalError") < 0) {
                    throw new IOException(result);
                }
                this.log("Retrying failed upload start..." + path);
                ++retries;
            }
            Element root = Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(result.getBytes("UTF8"))).getRootElement();
            tempUploadId = S3Client.getKeyText(root, "UploadId");
            if (tempResumeParts == null) {
                tempResumeParts = new Vector();
            }
        }
        if (needCopyResume) {
            this.doCopyResume(tempResumeParts, new Vector(), new StringBuffer().append("1"), new StringBuffer().append("1"), tempUploadId, path, path, startPos);
        }
        StringBuffer partNumber = new StringBuffer();
        partNumber.append("1");
        Vector resumeParts = tempResumeParts;
        String uploadId = tempUploadId;
        long maxBufferSize_temp = 0x500000L;
        try {
            if (!this.config.getProperty("s3_buffer", "5").equals("")) {
                maxBufferSize_temp = Long.parseLong(this.config.getProperty("s3_buffer", "5")) * 1024L * 1024L;
            }
        }
        catch (Exception e) {
            this.log("S3_CLIENT", 2, e);
        }
        long maxBufferSize = maxBufferSize_temp;
        Vector resumePartsDone = new Vector();
        this.get_cache_item("list_cache").remove(Common.all_but_last(path));
        class OutputWrapper
        extends OutputStream {
            boolean closed = false;
            long start = System.currentTimeMillis();
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            long totalBytes = 0L;
            private final /* synthetic */ long val$maxBufferSize;
            private final /* synthetic */ StringBuffer val$partNumber;
            private final /* synthetic */ Vector val$resumeParts;
            private final /* synthetic */ Vector val$resumePartsDone;
            private final /* synthetic */ String val$uploadId;
            private final /* synthetic */ String val$path;

            OutputWrapper(long l, StringBuffer stringBuffer, Vector vector, Vector vector2, String string, String string2) {
                this.val$maxBufferSize = l;
                this.val$partNumber = stringBuffer;
                this.val$resumeParts = vector;
                this.val$resumePartsDone = vector2;
                this.val$uploadId = string;
                this.val$path = string2;
            }

            @Override
            public void write(int i) throws IOException {
                this.write(new byte[]{(byte)i}, 0, 1);
            }

            @Override
            public void write(byte[] b) throws IOException {
                this.write(b, 0, b.length);
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                if (this.closed) {
                    throw new IOException("Stream already closed.");
                }
                this.buf.write(b, off, len);
                if (len > 0) {
                    this.totalBytes += (long)len;
                }
                if ((System.currentTimeMillis() - this.start > 60000L || (long)this.buf.size() > this.val$maxBufferSize) && this.buf.size() > 0x500000) {
                    int i = Integer.parseInt(this.val$partNumber.toString());
                    this.val$partNumber.setLength(0);
                    this.val$partNumber.append(String.valueOf(i + 1));
                    final int partNumber2 = i;
                    final ByteArrayOutputStream buf2 = this.buf;
                    Object object = s3_buffer_lock;
                    synchronized (object) {
                        ram_used_upload += (long)buf2.size();
                    }
                    this.val$resumeParts.addElement(new Properties());
                    int s3_threads_upload = 3;
                    try {
                        if (!S3Client.this.config.getProperty("s3_threads_upload", "3").equals("")) {
                            s3_threads_upload = Integer.parseInt(S3Client.this.config.getProperty("s3_threads_upload", "3"));
                        }
                    }
                    catch (Exception e) {
                        S3Client.this.log("S3_CLIENT", 2, e);
                    }
                    int loops = 0;
                    while (this.val$resumeParts.size() - this.val$resumePartsDone.size() >= s3_threads_upload + 1 && loops++ < 6000) {
                        try {
                            Thread.sleep(100L);
                        }
                        catch (InterruptedException interruptedException) {
                            // empty catch block
                        }
                    }
                    Worker.startWorker(new Runnable(){

                        /*
                         * WARNING - Removed try catching itself - possible behaviour change.
                         */
                        @Override
                        public void run() {
                            Thread.currentThread().setName("Buf Flusher:" + partNumber2);
                            int buf2_size = buf2.size();
                            try {
                                S3Client.this.flushNow(val$resumePartsDone, buf2, val$resumeParts, val$uploadId, val$path, partNumber2, partNumber2, false);
                            }
                            catch (IOException e) {
                                S3Client.this.log("S3_CLIENT", 0, e);
                            }
                            Object object = s3_buffer_lock;
                            synchronized (object) {
                                ram_used_upload -= (long)buf2_size;
                            }
                        }
                    });
                    this.buf = new ByteArrayOutputStream();
                    this.start = System.currentTimeMillis();
                }
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void close() throws IOException {
                if (this.closed) {
                    return;
                }
                int loops = 0;
                int i = Integer.parseInt(this.val$partNumber.toString());
                if (this.buf.size() > 0 || i == 1) {
                    int buf_size = this.buf.size();
                    Object object = s3_buffer_lock;
                    synchronized (object) {
                        ram_used_upload += (long)this.buf.size();
                    }
                    this.val$resumeParts.addElement(new Properties());
                    S3Client.this.flushNow(this.val$resumePartsDone, this.buf, this.val$resumeParts, this.val$uploadId, this.val$path, i, i, this.buf.size() == 0 && i == 1);
                    object = s3_buffer_lock;
                    synchronized (object) {
                        ram_used_upload -= (long)buf_size;
                    }
                }
                S3Client.this.log("S3_CLIENT", 0, "S3 thread count at close:" + (this.val$resumeParts.size() - this.val$resumePartsDone.size()));
                while (this.val$resumePartsDone.size() < this.val$resumeParts.size() && loops++ < 6000) {
                    try {
                        Thread.sleep(100L);
                    }
                    catch (InterruptedException buf_size) {
                        // empty catch block
                    }
                }
                this.closed = true;
                S3Client.this.finishUpload(this.val$resumeParts, this.val$path, this.val$uploadId);
                Date d = new Date();
                String line = "-rwxrwxrwx   1    owner   group   0   " + S3Client.this.yyyyMMddHHmmss.format(d) + "   " + S3Client.this.dd.format(d) + " " + S3Client.this.yyyy.format(d) + " " + this.val$path;
                try {
                    Properties stat = S3Client.parseStat(line);
                    String tempPath = this.val$path;
                    if (this.val$path.substring(1).indexOf("/") >= 0) {
                        tempPath = tempPath.substring(tempPath.indexOf("/", 1));
                    }
                    stat.put("url", new VRL(S3Client.this.url).getPath().equals("/") ? String.valueOf(S3Client.this.url) + this.val$path.substring(1) : String.valueOf(S3Client.this.url) + tempPath.substring(1));
                    stat.put("size", String.valueOf(this.totalBytes));
                    Properties ctmp = (Properties)S3Client.this.get_cache_item("list_cache").get(Common.all_but_last(this.val$path));
                    if (ctmp != null) {
                        Vector v = (Vector)ctmp.get("o");
                        v.addElement(stat);
                    }
                    S3Client.this.get_cache_item("stat_cache").put(this.val$path, stat);
                }
                catch (Exception e) {
                    S3Client.this.log("S3_CLIENT", 2, e);
                }
            }
        }
        this.out = new OutputWrapper(maxBufferSize, partNumber, resumeParts, resumePartsDone, uploadId, path);
        if (temp_buf.size() > 0) {
            this.out.write(temp_buf.toByteArray());
        }
        return this.out;
    }

    public void flushNow(Vector resumePartsDone, ByteArrayOutputStream buf2, Vector resumeParts, String uploadId, String path, int partNumber, int partNumberPos, boolean ignoreZero) throws IOException {
        if (buf2.size() == 0 && !ignoreZero) {
            return;
        }
        String bucketName = path.substring(1);
        String tempPath = path;
        if (bucketName.indexOf("/") >= 0) {
            bucketName = bucketName.substring(0, bucketName.indexOf("/"));
            if (!this.config.getProperty("s3_bucket_in_path", "false").equals("true")) {
                tempPath = tempPath.substring(tempPath.indexOf("/", 1));
            }
        }
        Properties chunk_part = (Properties)resumeParts.elementAt(partNumberPos - 1);
        chunk_part.put("start", String.valueOf(System.currentTimeMillis()));
        int loops = 0;
        while (loops < 20) {
            if (loops > 0) {
                try {
                    Thread.sleep(this.set_power_of_two_delay(50L, loops));
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
            }
            URLConnection urlc = null;
            try {
                urlc = uploadId == null ? URLConnection.openConnection(new VRL(String.valueOf(this.http_protocol) + "://" + (bucketName.equals("") || this.config.getProperty("s3_bucket_in_path", "false").equals("true") ? "" : String.valueOf(bucketName) + ".") + this.region_host + this.handle_path_special_chars(tempPath, false) + "_" + partNumber), this.config) : URLConnection.openConnection(new VRL(String.valueOf(this.http_protocol) + "://" + (bucketName.equals("") || this.config.getProperty("s3_bucket_in_path", "false").equals("true") ? "" : String.valueOf(bucketName) + ".") + this.region_host + this.handle_path_special_chars(tempPath, false) + "?partNumber=" + partNumber + "&uploadId=" + uploadId), this.config);
                chunk_part.put("urlc", urlc);
                urlc.setRemoveDoubleEncoding(true);
                urlc.setRequestMethod("PUT");
                urlc.setDoOutput(true);
                urlc.setLength(buf2.size());
                this.doStandardAmazonAlterations(urlc, null, bucketName);
                long start = System.currentTimeMillis();
                chunk_part.put("start", String.valueOf(start));
                try {
                    OutputStream tmp_out = urlc.getOutputStream();
                    this.log("S3_CLIENT", 2, urlc + ":Writing part " + partNumber + " with " + buf2.size() + " bytes to AWS...");
                    ByteArrayInputStream inb = new ByteArrayInputStream(buf2.toByteArray());
                    int bytesRead = 0;
                    byte[] b = new byte[32768];
                    while (bytesRead >= 0) {
                        bytesRead = inb.read(b);
                        if (bytesRead > 0) {
                            tmp_out.write(b, 0, bytesRead);
                        }
                        chunk_part.put("start", String.valueOf(System.currentTimeMillis()));
                    }
                    inb.close();
                    tmp_out.close();
                    this.log("S3_CLIENT", 2, urlc + ":Done writing part " + partNumber + " to AWS.");
                }
                catch (Exception e) {
                    this.log("S3_CLIENT", 1, e);
                }
                int code = urlc.getResponseCode();
                this.log("S3_CLIENT", 2, urlc + ":Got part " + partNumber + " response:" + code + " time:" + (System.currentTimeMillis() - start) + "ms");
                String result = URLConnection.consumeResponse(urlc.getInputStream());
                urlc.disconnect();
                if (code < 200 || code > 299) {
                    result = this.getErrorInfo(result, code, urlc.getConfig("canonical_request"), null);
                    this.log(String.valueOf(result) + "\r\n");
                    throw new IOException(result);
                }
                buf2.reset();
                this.log("S3_CLIENT", 2, urlc + ":Got part " + partNumber + " chunk id:" + urlc.getHeaderField("ETag") + " time:" + (System.currentTimeMillis() - start) + "ms");
                chunk_part.put("time", String.valueOf(System.currentTimeMillis() - start));
                chunk_part.put("etag", urlc.getHeaderField("ETag"));
                this.log("S3_CLIENT", 2, "resumeParts:" + resumeParts);
                resumePartsDone.addElement(String.valueOf(partNumber));
                break;
            }
            catch (IOException e) {
                if (loops > 3) {
                    chunk_part.put("start", String.valueOf(System.currentTimeMillis() + (long)(1000 * loops)));
                }
                if (loops > 8) {
                    throw e;
                }
                this.log("S3_CLIENT", 1, e);
            }
            finally {
                chunk_part.remove("urlc");
                urlc.disconnect();
            }
            ++loops;
        }
    }

    public void doCopyResume(Vector resumeParts, Vector resumePartsDone, StringBuffer partNumber, StringBuffer partNumberPos, String uploadId, String old_path, String new_path, long startPos) throws Exception {
        String bucketName = new_path.substring(1);
        String tempPath = new_path;
        if (bucketName.indexOf("/") >= 0) {
            bucketName = bucketName.substring(0, bucketName.indexOf("/"));
            if (!this.config.getProperty("s3_bucket_in_path", "false").equals("true")) {
                tempPath = tempPath.substring(tempPath.indexOf("/", 1));
            }
        }
        long total_bytes = startPos;
        long pos = 0L;
        while (total_bytes > 0L) {
            long chunk_size = total_bytes;
            if (chunk_size > 0x40000000L) {
                chunk_size = 0x40000000L;
            }
            URLConnection urlc = URLConnection.openConnection(new VRL(String.valueOf(this.http_protocol) + "://" + (bucketName.equals("") || this.config.getProperty("s3_bucket_in_path", "false").equals("true") ? "" : String.valueOf(bucketName) + ".") + this.region_host + this.handle_path_special_chars(tempPath, false) + "?partNumber=" + partNumber + "&uploadId=" + uploadId), this.config);
            urlc.setRemoveDoubleEncoding(true);
            urlc.setRequestMethod("PUT");
            urlc.setRequestProperty("x-amz-copy-source", Common.url_decode(this.handle_path_special_chars(old_path, true)));
            urlc.setRequestProperty("x-amz-copy-source-range", "bytes=" + pos + "-" + (pos + chunk_size - 1L));
            if (!this.config.getProperty("s3_acl", "private").equals("") && !this.config.getProperty("s3_acl", "private").equals("private")) {
                urlc.setRequestProperty("x-amz-acl", this.config.getProperty("s3_acl", "private"));
            }
            if (!this.config.getProperty("s3_storage_class", "STANDARD").equals("STANDARD")) {
                urlc.setRequestProperty("x-amz-storage-class", this.config.getProperty("s3_storage_class", "STANDARD"));
            }
            this.doStandardAmazonAlterations(urlc, null, bucketName);
            int code = urlc.getResponseCode();
            String result = URLConnection.consumeResponse(urlc.getInputStream());
            urlc.disconnect();
            if (code < 200 || code > 299) {
                result = this.getErrorInfo(result, code, urlc.getConfig("canonical_request"), null);
                this.log(String.valueOf(result) + "\r\n");
                throw new IOException(result);
            }
            Element root = Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(result.getBytes("UTF8"))).getRootElement();
            Properties chunk_part = new Properties();
            chunk_part.put("etag", S3Client.getKeyText(root, "ETag"));
            resumeParts.addElement(chunk_part);
            resumePartsDone.addElement(partNumber.toString());
            int partNum = Integer.parseInt(partNumber.toString()) + 1;
            partNumber.setLength(0);
            partNumber.append(String.valueOf(partNum));
            int partNumPos = Integer.parseInt(partNumberPos.toString()) + 1;
            partNumberPos.setLength(0);
            partNumberPos.append(String.valueOf(partNumPos));
            total_bytes -= chunk_size;
            pos += chunk_size;
        }
    }

    public void finishUpload(Vector resumeParts, String path, String uploadId) throws IOException {
        if (uploadId == null) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("<CompleteMultipartUpload>");
        int x = 0;
        while (x < resumeParts.size()) {
            Properties chunk_part = (Properties)resumeParts.elementAt(x);
            if (chunk_part.getProperty("etag") != null) {
                sb.append("<Part>");
                sb.append("<PartNumber>" + (x + 1) + "</PartNumber>");
                sb.append("<ETag>" + chunk_part.getProperty("etag") + "</ETag>");
                sb.append("</Part>");
            }
            ++x;
        }
        sb.append("</CompleteMultipartUpload>");
        byte[] b = sb.toString().getBytes("UTF8");
        String bucketName = path.substring(1);
        String tempPath = path;
        if (bucketName.indexOf("/") >= 0) {
            bucketName = bucketName.substring(0, bucketName.indexOf("/"));
            if (!this.config.getProperty("s3_bucket_in_path", "false").equals("true")) {
                tempPath = tempPath.substring(tempPath.indexOf("/", 1));
            }
        }
        int code = -1;
        String result = "";
        int loops = 0;
        String canonical_request = "";
        while (code == -1 && loops++ < 60) {
            try {
                URLConnection urlc = URLConnection.openConnection(new VRL(String.valueOf(this.http_protocol) + "://" + (bucketName.equals("") || this.config.getProperty("s3_bucket_in_path", "false").equals("true") ? "" : String.valueOf(bucketName) + ".") + this.region_host + this.handle_path_special_chars(tempPath, false) + "?uploadId=" + uploadId), this.config);
                urlc.setRemoveDoubleEncoding(true);
                urlc.setRequestMethod("POST");
                urlc.setDoOutput(true);
                urlc.setLength(b.length);
                this.doStandardAmazonAlterations(urlc, null, bucketName);
                this.out3 = urlc.getOutputStream();
                this.out3.write(b);
                this.out3.close();
                this.log("S3_CLIENT", 2, sb.toString());
                code = urlc.getResponseCode();
                result = URLConnection.consumeResponse(urlc.getInputStream());
                canonical_request = urlc.getConfig("canonical_request");
                urlc.disconnect();
                if (code >= 200 && code < 299 && result.indexOf("InternalError") < 0) break;
                this.log("Trying to finish the upload again, no response from AWS:" + code + "\r\n");
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                Thread.sleep(this.set_power_of_two_delay(10L, loops));
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
        if (code < 200 || code > 299) {
            result = this.getErrorInfo(result, code, canonical_request, null);
            this.log(String.valueOf(result) + "\r\n");
            throw new IOException(result);
        }
    }

    @Override
    public boolean delete(String path) throws Exception {
        path = this.lower(path);
        int code = 0;
        String result = "";
        int loops = 1;
        boolean s3_one_delete_attempt = System.getProperty("crushftp.s3_one_delete_attempt", "false").equals("true");
        while (code != 404 && loops++ < 100) {
            Properties stat;
            if (loops > 0) {
                Thread.sleep(this.set_power_of_two_delay(1L, loops));
            }
            if ((stat = this.stat(path)) == null) {
                stat = new Properties();
            }
            boolean is_folder = stat.getProperty("type", "FILE").equalsIgnoreCase("DIR");
            URLConnection urlc = this.doAction("DELETE", String.valueOf(path) + (stat.getProperty("type", "FILE").equalsIgnoreCase("DIR") ? "/" : "") + (!stat.getProperty("uploadId", "").equals("") ? "?uploadId=" + stat.getProperty("uploadId", "") : ""), null, false, true, this.config.getProperty("s3_bucket_in_path", "false").equals("true"));
            code = urlc.getResponseCode();
            result = URLConnection.consumeResponse(urlc.getInputStream());
            urlc.disconnect();
            if ((code < 200 || code > 299) && code != 404) {
                result = this.getErrorInfo(result, code, urlc.getConfig("canonical_request"), null);
                this.log(String.valueOf(result) + "\r\n");
                return false;
            }
            this.get_cache_item("cache_resume").clear();
            this.updateCache(stat, path, "remove");
            if (this.get_cache_item("recently_created_folder_cache").containsKey(path)) {
                this.get_cache_item("recently_created_folder_cache").remove(path);
            }
            if (stat.size() == 0 || s3_one_delete_attempt && this.stat(path) == null) break;
            if (!is_folder || this.stat(path) == null) continue;
            return false;
        }
        return loops < 99;
    }

    private void updateCache(Properties stat, String path, String action) throws Exception {
        Properties p;
        int x;
        Vector v;
        path = this.lower(path);
        Properties ctmp = (Properties)this.get_cache_item("list_cache").get(Common.all_but_last(path));
        if (ctmp != null) {
            v = (Vector)ctmp.get("o");
            x = 0;
            while (x < v.size()) {
                p = (Properties)v.elementAt(x);
                if (p.getProperty("url", "").equals(stat.getProperty("url", ""))) {
                    if (action.equals("remove")) {
                        v.remove(x);
                    } else if (action.equals("modified")) {
                        p.put("modified", stat.getProperty("modified"));
                    }
                    this.get_cache_item("list_cache").put(Common.all_but_last(path), ctmp);
                    break;
                }
                ++x;
            }
        }
        if ((ctmp = (Properties)this.get_cache_item("cache_resume").get(Common.all_but_last(path))) != null) {
            v = (Vector)ctmp.get("o");
            x = 0;
            while (x < v.size()) {
                p = (Properties)v.elementAt(x);
                if (p.getProperty("url", "").equals(stat.getProperty("url", ""))) {
                    if (action.equals("remove")) {
                        v.remove(x);
                    } else if (action.equals("modified")) {
                        p.put("modified", stat.getProperty("modified"));
                    }
                    this.get_cache_item("cache_resume").put(Common.all_but_last(path), ctmp);
                    break;
                }
                ++x;
            }
        }
        Properties stat_cache = this.get_cache_item("stat_cache");
        if (action.equals("remove") && stat_cache.containsKey(path)) {
            stat_cache.remove(path);
        }
        if (action.equals("modified") && stat_cache.containsKey(path)) {
            ((Properties)stat_cache.get(path)).put("modified", stat.getProperty("modified"));
        }
    }

    @Override
    public boolean makedir(String path) throws Exception {
        if (this.is_folder_recently_created(path = this.lower(path))) {
            return true;
        }
        StringBuffer bucketNameSB = new StringBuffer();
        URLConnection urlc = this.doAction("PUT", path, bucketNameSB, true, true, this.config.getProperty("s3_bucket_in_path", "false").equals("true"));
        urlc.setLength(0L);
        boolean resign_header = false;
        if (!this.config.getProperty("s3_acl", "private").equals("") && !this.config.getProperty("s3_acl", "private").equals("private")) {
            urlc.setRequestProperty("x-amz-acl", this.config.getProperty("s3_acl", "private"));
            resign_header = true;
        }
        if (!this.config.getProperty("s3_storage_class", "STANDARD").equals("STANDARD")) {
            urlc.setRequestProperty("x-amz-storage-class", this.config.getProperty("s3_storage_class", "STANDARD"));
            resign_header = true;
        }
        if (resign_header) {
            this.doStandardAmazonAlterations(urlc, Common.mimes.getProperty(this.getExt(path)), bucketNameSB.toString());
        }
        int code = urlc.getResponseCode();
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        urlc.disconnect();
        if (code < 200 || code > 299) {
            result = this.getErrorInfo(result, code, urlc.getConfig("canonical_request"), null);
            this.log(String.valueOf(result) + "\r\n");
            return false;
        }
        this.get_cache_item("stat_cache").clear();
        this.get_cache_item("list_cache").clear();
        this.get_cache_item("cache_resume").clear();
        this.get_cache_item("recently_created_folder_cache").put(path, String.valueOf(System.currentTimeMillis()));
        return true;
    }

    @Override
    public boolean makedirs(String path) throws Exception {
        path = this.lower(path);
        boolean ok = true;
        String[] parts = path.startsWith("/") ? path.substring(1, path.length()).split("/") : path.split("/");
        String path2 = "/";
        int x = 0;
        while (x < parts.length && ok) {
            path2 = String.valueOf(path2) + parts[x] + "/";
            if (x >= 1 && this.stat(path2) == null) {
                this.makedir(path2);
            }
            ++x;
        }
        return ok;
    }

    @Override
    public boolean rename(String rnfr, String rnto, boolean overwrite) throws Exception {
        rnfr = this.lower(rnfr);
        rnto = this.lower(rnto);
        this.get_cache_item("stat_cache").remove(rnfr);
        this.get_cache_item("stat_cache").remove(rnto);
        Properties stat = this.stat(rnfr);
        if (stat.getProperty("type").equalsIgnoreCase("DIR")) {
            this.log("S3_CLIENT", 0, "S3 API does not support renaming folders.");
            this.log("S3 API does not support renaming folders.");
            throw new Exception("FAILURE: Renaming of Cloud (sub)Directories is not supported, aborting rename operation.");
        }
        StringBuffer bucketNameSB = new StringBuffer();
        URLConnection urlc = this.doAction("PUT", rnto, bucketNameSB, true, true, this.config.getProperty("s3_bucket_in_path", "false").equals("true"));
        urlc.setRequestProperty("x-amz-copy-source", Common.url_decode(this.handle_path_special_chars(rnfr, true)).substring(1));
        if (!this.config.getProperty("s3_acl", "private").equals("") && !this.config.getProperty("s3_acl", "private").equals("private")) {
            urlc.setRequestProperty("x-amz-acl", this.config.getProperty("s3_acl", "private"));
        }
        if (!this.config.getProperty("s3_storage_class", "STANDARD").equals("STANDARD")) {
            urlc.setRequestProperty("x-amz-storage-class", this.config.getProperty("s3_storage_class", "STANDARD"));
        }
        Properties metadata = this.getMetadata(rnfr);
        Enumeration<Object> keys = metadata.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            urlc.setRequestProperty(key, metadata.getProperty(key));
        }
        if (Long.parseLong(stat.getProperty("size", "104857600")) > 0x40000000L) {
            throw new Exception("FAILURE: This file is too large to rename! The maximum supported file size: 1GB.");
        }
        int s3_rename_allowed_speed = 5;
        try {
            s3_rename_allowed_speed = Integer.parseInt(this.config.getProperty("s3_rename_allowed_speed", "5"));
        }
        catch (Exception e) {
            this.log("S3_CLIENT", 2, e);
        }
        int read_timeout = 2000 + s3_rename_allowed_speed * 1000;
        if (Long.parseLong(stat.getProperty("size", "104857600")) > (long)(0x100000 * s3_rename_allowed_speed)) {
            read_timeout = 2000 + (int)(Long.parseLong(stat.getProperty("size", "104857600")) / (long)(0x100000 * s3_rename_allowed_speed)) * 1000;
        }
        this.log("S3_CLIENT", 1, "Path: " + rnfr + " Rename read timeout : " + read_timeout);
        urlc.setReadTimeout(read_timeout);
        urlc.setRequestProperty("x-amz-metadata-directive", "REPLACE");
        urlc.setRequestProperty("Content-Length", "0");
        this.doStandardAmazonAlterations(urlc, Common.mimes.getProperty(this.getExt(this.handle_path_special_chars(rnto, false))), bucketNameSB.toString());
        int code = urlc.getResponseCode();
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        urlc.disconnect();
        this.get_cache_item("list_cache").clear();
        this.get_cache_item("cache_resume").clear();
        if (code < 200 || code > 299) {
            result = this.getErrorInfo(result, code, urlc.getConfig("canonical_request"), null);
            this.log(String.valueOf(result) + "\r\n");
            return false;
        }
        this.delete(rnfr);
        return true;
    }

    @Override
    public Properties stat(String path) throws Exception {
        if ((path = this.lower(path)).endsWith(":filetree")) {
            path = path.substring(0, path.indexOf(":filetree") - 1);
        }
        if (this.config.getProperty("s3_stat_head_calls", "true").equals("true")) {
            return this.stat_head_calls(path);
        }
        Vector v = new Vector();
        this.list(Common.all_but_last(path), v);
        return this.stat_list(path, v);
    }

    private Properties stat_head_calls(String path) throws Exception {
        path = this.lower(path);
        Vector list = null;
        Properties ctmp = (Properties)this.get_cache_item("list_cache").get(Common.all_but_last(path));
        if (ctmp != null && System.currentTimeMillis() - Long.parseLong(ctmp.getProperty("time")) < 30000L) {
            list = (Vector)Common.CLONE(ctmp.get("o"));
        }
        Properties info = null;
        if (list != null) {
            info = this.stat_list(path, list);
        }
        if (info == null) {
            Properties p;
            boolean cache_now = true;
            if (this.get_cache_item("stat_cache").containsKey(path)) {
                cache_now = false;
                p = (Properties)this.get_cache_item("stat_cache").get(path);
                if (System.currentTimeMillis() - Long.parseLong(p.getProperty("time", "0")) < 30000L) {
                    if (p.getProperty("exists", "").equals("false")) {
                        return null;
                    }
                    return (Properties)Common.CLONE(p.get("info"));
                }
                this.get_cache_item("stat_cache").remove(path);
                cache_now = true;
            }
            if (!path.endsWith("/")) {
                info = this.getS3ObjectInfo(path);
            }
            if (info == null) {
                String path2;
                Vector v = new Vector();
                if ((v = this.list(path, v, 1)).size() > 0) {
                    String bucketName = path.substring(1);
                    path2 = path;
                    if (bucketName.indexOf("?") >= 0) {
                        bucketName = bucketName.substring(0, bucketName.indexOf("?"));
                    }
                    if (bucketName.indexOf("/") >= 0) {
                        bucketName = bucketName.substring(0, bucketName.indexOf("/") + 1);
                        path2 = path2.substring(path2.indexOf("/", 1));
                    }
                    if (path2.endsWith("/")) {
                        path2 = path2.substring(0, path2.length() - 1);
                    }
                    if (path2.equals("")) {
                        path2 = "/";
                    }
                    Date d = new Date(0L);
                    String line = "drwxrwxrwx   1    owner   group   0   " + this.yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + Common.last(path2);
                    Properties stat = S3Client.parseStat(line);
                    if (Common.url_decode(this.url).endsWith(bucketName)) {
                        bucketName = "";
                    }
                    stat.put("url", String.valueOf(Common.url_decode(this.url)) + bucketName + path2.substring(1));
                    stat.put("check_all_recursive_deletes", "true");
                    if (ctmp != null && list != null) {
                        list.add(stat);
                    }
                    info = stat;
                    cache_now = true;
                }
                boolean s3_partial = System.getProperty("crushftp.s3_partial", "true").equals("true");
                if (this.config.containsKey("s3_partial")) {
                    s3_partial = this.config.getProperty("s3_partial", "true").equals("true");
                }
                if (info == null && s3_partial) {
                    Vector v2;
                    path2 = path;
                    String bucketName = "";
                    if (!path2.equals("/")) {
                        bucketName = path2.substring(1, path2.indexOf("/", 1));
                        path2 = path2.substring(bucketName.length() + 1);
                    }
                    if (path2.endsWith("/")) {
                        path2 = path2.substring(0, path2.length() - 1);
                    }
                    if ((v2 = new Vector()) != null) {
                        v2 = this.get_uploads_in_progress_or_failed_uploads(v2, path2, bucketName);
                        info = this.stat_list(path, v2);
                    }
                }
            } else if (info != null) {
                String bucketName = path.substring(1);
                String path2 = path;
                if (bucketName.indexOf("?") >= 0) {
                    bucketName = bucketName.substring(0, bucketName.indexOf("?"));
                }
                if (bucketName.indexOf("/") >= 0) {
                    bucketName = bucketName.substring(0, bucketName.indexOf("/") + 1);
                    path2 = path2.substring(path2.indexOf("/", 1));
                }
                if (path2.endsWith("/")) {
                    path2 = path2.substring(0, path2.length() - 1);
                }
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date d = sdf.parse(info.getProperty("LAST-MODIFIED"));
                String line = "-rwxrwxrwx   1    owner   group   " + info.getProperty("CONTENT-LENGTH", "0") + "   " + this.yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + Common.last(path2);
                Properties stat = S3Client.parseStat(line);
                if (Common.url_decode(this.url).endsWith(bucketName)) {
                    bucketName = "";
                }
                stat.put("url", String.valueOf(Common.url_decode(this.url)) + bucketName + path2.substring(1));
                if (ctmp != null && list != null) {
                    list.add(stat);
                }
                info = stat;
            }
            if (cache_now) {
                p = new Properties();
                if (info == null) {
                    p.put("exists", "false");
                } else {
                    p.put("info", Common.CLONE(info));
                }
                p.put("time", String.valueOf(System.currentTimeMillis()));
                this.get_cache_item("stat_cache").put(path, p);
            }
        }
        return info;
    }

    private Properties stat_list(String path, Vector v) throws Exception {
        path = this.lower(path);
        String last_path = Common.last(path);
        int x = 0;
        while (x < v.size()) {
            Properties p = (Properties)v.elementAt(x);
            if (p.getProperty("name").equals(last_path)) {
                return p;
            }
            if (last_path.endsWith("/") && (String.valueOf(p.getProperty("name")) + "/").equals(last_path)) {
                return p;
            }
            ++x;
        }
        return null;
    }

    @Override
    public boolean mdtm(String path, long modified) throws Exception {
        Properties p;
        path = this.lower(path);
        StringBuffer bucketNameSB = new StringBuffer();
        final URLConnection urlc = this.doAction("PUT", path, bucketNameSB, true, true, this.config.getProperty("s3_bucket_in_path", "false").equals("true"));
        urlc.setLength(0L);
        urlc.setRequestProperty("x-amz-copy-source", Common.url_decode(this.handle_path_special_chars(path, true)));
        urlc.setRequestProperty("x-amz-meta-modified", String.valueOf(modified));
        if (!this.config.getProperty("s3_storage_class", "STANDARD").equals("STANDARD")) {
            urlc.setRequestProperty("x-amz-storage-class", this.config.getProperty("s3_storage_class", "STANDARD"));
        }
        if ((p = this.getS3ObjectInfo(path)) != null && !this.config.getProperty("s3_meta_md5_and_upload_by", "true").equals("false")) {
            if (p.containsKey("X-AMZ-META-UPLOADED-BY")) {
                urlc.setRequestProperty("x-amz-meta-uploaded-by", p.getProperty("X-AMZ-META-UPLOADED-BY", ""));
            } else if (!this.config.getProperty("uploaded_by", "").equals("")) {
                urlc.setRequestProperty("x-amz-meta-uploaded-by", this.config.getProperty("uploaded_by", ""));
            }
            if (p.containsKey("X-AMZ-META-MD5")) {
                urlc.setRequestProperty("x-amz-meta-md5", p.getProperty("X-AMZ-META-MD5", ""));
            } else if (!this.config.getProperty("uploaded_md5", "").equals("")) {
                urlc.setRequestProperty("x-amz-meta-md5", this.config.getProperty("uploaded_md5", ""));
            }
            if (p.containsKey("X-AMZ-SERVER-SIDE-ENCRYPTION")) {
                urlc.setRequestProperty("x-amz-server-side-encryption", "" + p.get("X-AMZ-SERVER-SIDE-ENCRYPTION"));
            }
        }
        urlc.setRequestProperty("x-amz-metadata-directive", "REPLACE");
        if (!this.config.getProperty("s3_acl", "private").equals("") && !this.config.getProperty("s3_acl", "private").equals("private")) {
            urlc.setRequestProperty("x-amz-acl", this.config.getProperty("s3_acl", "private"));
        }
        this.doStandardAmazonAlterations(urlc, Common.mimes.getProperty(this.getExt(path)), bucketNameSB.toString());
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                try {
                    int code = urlc.getResponseCode();
                    String result = URLConnection.consumeResponse(urlc.getInputStream());
                    urlc.disconnect();
                    if (code < 200 || code > 299) {
                        result = S3Client.this.getErrorInfo(result, code, urlc.getConfig("canonical_request"), null);
                        S3Client.this.log(String.valueOf(result) + "\r\n");
                    }
                }
                catch (Exception e) {
                    S3Client.this.log(e);
                }
            }
        });
        String bucketName = "";
        if (!path.equals("/")) {
            bucketName = path.substring(1, path.indexOf("/", 1));
            path = path.substring(bucketName.length() + 1);
        }
        Properties stat = new Properties();
        stat.put("url", String.valueOf(this.url) + (bucketName.toString().equals("") ? "" : String.valueOf(bucketName.toString()) + "/") + path.substring(1));
        stat.put("modified", String.valueOf(modified));
        this.updateCache(stat, path, "modified");
        return true;
    }

    public Properties getMetadata(String path) throws IOException, SocketTimeoutException {
        path = this.lower(path);
        Properties header_properties = new Properties();
        Properties s3ObjectInfo = this.getS3ObjectInfo(path);
        if (s3ObjectInfo == null) {
            return null;
        }
        Enumeration<Object> keys = s3ObjectInfo.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            if (!key.startsWith("X-AMZ-META-")) continue;
            header_properties.put(key.toLowerCase(), s3ObjectInfo.get(key));
        }
        return header_properties;
    }

    private Properties getS3ObjectInfo(String path) throws IOException, SocketTimeoutException {
        path = this.lower(path);
        StringBuffer bucketNameSB = new StringBuffer();
        URLConnection urlc = this.doAction("HEAD", path, bucketNameSB, false, true, this.config.getProperty("s3_bucket_in_path", "false").equals("true"));
        urlc.setLength(0L);
        this.doStandardAmazonAlterations(urlc, Common.mimes.getProperty(this.getExt(path)), bucketNameSB.toString());
        int code = urlc.getResponseCode();
        if (code < 200 || code > 299) {
            this.log("S3_CLIENT", 1, "S3 object info : path = " + path + "error message: " + urlc.getResponseMessage() + "\r\n");
            return null;
        }
        Properties p = (Properties)urlc.headers.clone();
        urlc.disconnect();
        return p;
    }

    @Override
    public String getUploadedByMetadata(String path) {
        path = this.lower(path);
        Properties p = null;
        try {
            p = this.getMetadata(path);
        }
        catch (Exception e) {
            this.log("S3_CLIENT", 1, e);
        }
        if (p != null && p.containsKey("x-amz-meta-uploaded-by") && !p.getProperty("x-amz-meta-uploaded-by").equals("")) {
            return p.getProperty("x-amz-meta-uploaded-by");
        }
        return "";
    }

    @Override
    public void set_MD5_and_upload_id(String path) throws Exception {
        if (!this.config.getProperty("s3_meta_md5_and_upload_by", "true").equals("false")) {
            path = this.lower(path);
            StringBuffer bucketNameSB = new StringBuffer();
            final URLConnection urlc = this.doAction("PUT", path, bucketNameSB, true, true, this.config.getProperty("s3_bucket_in_path", "false").equals("true"));
            urlc.setLength(0L);
            urlc.setRequestProperty("x-amz-copy-source", Common.url_decode(this.handle_path_special_chars(path, true)));
            Properties p = this.getS3ObjectInfo(path);
            if (p != null) {
                if (p.containsKey("X-AMZ-META-UPLOADED-BY")) {
                    urlc.setRequestProperty("x-amz-meta-uploaded-by", p.getProperty("X-AMZ-META-UPLOADED-BY", ""));
                } else if (!this.config.getProperty("uploaded_by", "").equals("")) {
                    urlc.setRequestProperty("x-amz-meta-uploaded-by", this.config.getProperty("uploaded_by", ""));
                }
                if (p.containsKey("X-AMZ-META-MD5")) {
                    urlc.setRequestProperty("x-amz-meta-md5", p.getProperty("X-AMZ-META-MD5", ""));
                } else if (!this.config.getProperty("uploaded_md5", "").equals("")) {
                    urlc.setRequestProperty("x-amz-meta-md5", this.config.getProperty("uploaded_md5", ""));
                }
                if (p.containsKey("X-AMZ-META-MODIFIED")) {
                    urlc.setRequestProperty("x-amz-meta-modified", p.getProperty("X-AMZ-META-MODIFIED", ""));
                }
                if (p.containsKey("X-AMZ-SERVER-SIDE-ENCRYPTION")) {
                    urlc.setRequestProperty("x-amz-server-side-encryption", "" + p.get("X-AMZ-SERVER-SIDE-ENCRYPTION"));
                }
            }
            urlc.setRequestProperty("x-amz-metadata-directive", "REPLACE");
            if (!this.config.getProperty("s3_acl", "private").equals("") && !this.config.getProperty("s3_acl", "private").equals("private")) {
                urlc.setRequestProperty("x-amz-acl", this.config.getProperty("s3_acl", "private"));
            }
            if (!this.config.getProperty("s3_storage_class", "STANDARD").equals("STANDARD")) {
                urlc.setRequestProperty("x-amz-storage-class", this.config.getProperty("s3_storage_class", "STANDARD"));
            }
            this.doStandardAmazonAlterations(urlc, Common.mimes.getProperty(this.getExt(path)), bucketNameSB.toString());
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        int code = urlc.getResponseCode();
                        String result = URLConnection.consumeResponse(urlc.getInputStream());
                        urlc.disconnect();
                        if (code < 200 || code > 299) {
                            S3Client.this.log(String.valueOf(result) + "\r\n");
                        }
                    }
                    catch (Exception e) {
                        S3Client.this.log(e);
                    }
                }
            });
        }
    }

    public void doStandardAmazonAlterations(URLConnection urlc, String contentType, String bucketName) {
        try {
            this.updateIamAuth();
        }
        catch (Exception e) {
            this.log("S3_CLIENT", 0, e);
        }
        urlc.setRequestProperty("Content-Type", contentType);
        urlc.setRequestProperty("Accept", null);
        urlc.setRequestProperty("Pragma", null);
        if (this.config.getProperty("s3_bucket_in_path", "false").equals("true")) {
            urlc.setRequestProperty("Host", this.region_host);
        } else if (!bucketName.equals("")) {
            urlc.setRequestProperty("Host", String.valueOf(bucketName) + "." + this.region_host);
        }
        urlc.setRequestProperty("Cache", null);
        urlc.setRequestProperty("Cache-Control", null);
        if (this.config.containsKey("real_token")) {
            urlc.setRequestProperty("x-amz-security-token", this.config.getProperty("real_token"));
        }
        boolean s3_sha256 = System.getProperty("crushftp.s3_sha256", "false").equals("true");
        if (this.config.containsKey("s3_sha256")) {
            s3_sha256 = this.config.getProperty("s3_sha256", "false").equals("true");
        }
        if (!s3_sha256 && this.config.getProperty("server_side_encrypt_kms", "").equals("")) {
            urlc.setRequestProperty("Authorization", "AWS " + this.config.getProperty("real_username", this.config.getProperty("username")) + ":" + this.calculateAmazonSignature(urlc));
        } else {
            try {
                if (this.config.getProperty("s3_sha256_request_header", "false").equals("true")) {
                    urlc.setRequestProperty("x-amz-content-sha256", "UNSIGNED-PAYLOAD");
                    urlc.setRequestProperty("Authorization", this.calculateAmazonSignaturev4(urlc));
                } else {
                    urlc.setRequestProperty("Authorization", this.calculateAmazonSignaturev4(urlc));
                    urlc.setRequestProperty("x-amz-content-sha256", "UNSIGNED-PAYLOAD");
                }
                urlc.setRequestProperty("x-amz-date", this.yyyyMMddtHHmmssZ.format(urlc.getDate()));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        urlc.setUseCaches(false);
    }

    public URLConnection doAction(String verb, String path, StringBuffer bucketNameSB, boolean do_secure, boolean handle_special_chars, boolean bucket_in_path) {
        String bucketName = (path = this.lower(path)).substring(1);
        if (bucketName.indexOf("?") >= 0) {
            bucketName = bucketName.substring(0, bucketName.indexOf("?"));
        }
        if (bucketName.indexOf("/") >= 0) {
            bucketName = bucketName.substring(0, bucketName.indexOf("/"));
            if (!bucket_in_path) {
                path = path.substring(path.indexOf("/", 1));
            }
        }
        if (path.equals("/" + bucketName)) {
            path = "/";
        }
        if (handle_special_chars) {
            path = path.contains("?uploadId=") ? String.valueOf(this.handle_path_special_chars(path.substring(0, path.indexOf("?uploadId=")), false)) + path.substring(path.indexOf("?uploadId="), path.length()) : this.handle_path_special_chars(path, false);
        }
        VRL vrl = new VRL(String.valueOf(this.http_protocol) + "://" + (bucketName.equals("") || bucket_in_path ? "" : String.valueOf(bucketName) + ".") + this.region_host + path);
        this.log("S3_CLIENT", 1, "S3 URL:" + verb + ":" + vrl.safe());
        URLConnection urlc = URLConnection.openConnection(vrl, (Properties)Common.CLONE(this.config));
        urlc.setRemoveDoubleEncoding(true);
        urlc.setRequestMethod(verb);
        urlc.setDoOutput(false);
        try {
            int timeout = Integer.parseInt(this.config.getProperty("timeout", "20000"));
            if (timeout < 10000) {
                this.config.put("timeout", "10000");
            }
            urlc.setReadTimeout(timeout);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        if (do_secure && !this.config.getProperty("server_side_encrypt_kms", "").equals("")) {
            urlc.setRequestProperty("x-amz-server-side-encryption", "aws:kms");
            urlc.setRequestProperty("x-amz-server-side-encryption-aws-kms-key-id", this.config.getProperty("server_side_encrypt_kms", ""));
        } else if (do_secure && this.config.getProperty("server_side_encrypt", "false").equals("true")) {
            urlc.setRequestProperty("x-amz-server-side-encryption", "AES256");
        }
        this.doStandardAmazonAlterations(urlc, null, bucketName);
        if (bucketNameSB != null) {
            bucketNameSB.setLength(0);
            bucketNameSB.append(bucketName);
        }
        return urlc;
    }

    public String calculateAmazonSignature(URLConnection urlc) {
        String data = String.valueOf(urlc.getRequestMethod()) + "\n";
        data = String.valueOf(data) + "\n";
        data = String.valueOf(data) + urlc.getContentType() + "\n";
        data = String.valueOf(data) + urlc.sdf_rfc1123.format(urlc.getDate()) + "\n";
        String bucketName = urlc.getRequestProps().getProperty("HOST", urlc.getURL().getHost());
        if (this.region_host.contains(":")) {
            bucketName = String.valueOf(urlc.getRequestProps().getProperty("HOST", urlc.getURL().getHost())) + ":" + urlc.getURL().getPort();
        }
        bucketName = this.config.getProperty("s3_bucket_in_path", "false").equals("true") ? urlc.getURL().getPath().substring(1, urlc.getURL().getPath().indexOf("/", 1)) : (bucketName.equalsIgnoreCase(this.region_host) ? "" : bucketName.substring(0, bucketName.toLowerCase().indexOf("." + this.region_host)));
        Properties props = urlc.getRequestProps();
        Vector<String> recs = new Vector<String>();
        Enumeration<Object> keys = props.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            if (!key.startsWith("x-amz-")) continue;
            recs.addElement(String.valueOf(key) + ":" + props.getProperty(key).trim());
        }
        int j = 0;
        while (j < recs.size()) {
            String a = recs.elementAt(j).toString();
            int k = j;
            while (k < recs.size()) {
                String b = recs.elementAt(k).toString();
                if (a.toLowerCase().substring(0, a.indexOf(":")).compareTo(b.toLowerCase().substring(0, b.indexOf(":"))) > 0) {
                    recs.setElementAt(b, j);
                    recs.setElementAt(a, k);
                    a = b;
                }
                ++k;
            }
            ++j;
        }
        int x = 0;
        while (x < recs.size()) {
            data = String.valueOf(data) + recs.elementAt(x) + "\n";
            ++x;
        }
        String tmp_path = "";
        tmp_path = bucketName.equals("") ? "/" : (urlc.getURL().getPath().indexOf("&uploads") >= 0 ? "/" + bucketName + urlc.getURL().getPath().substring(0, urlc.getURL().getPath().indexOf("?")) + "?uploads" : (urlc.getURL().getPath().indexOf("?delimiter") >= 0 ? (this.config.getProperty("s3_bucket_in_path", "false").equals("true") ? urlc.getURL().getPath().substring(0, urlc.getURL().getPath().indexOf("?")) : "/" + bucketName + urlc.getURL().getPath().substring(0, urlc.getURL().getPath().indexOf("?"))) : (this.config.getProperty("s3_bucket_in_path", "false").equals("true") ? urlc.getURL().getPath() : "/" + bucketName + urlc.getURL().getPath())));
        tmp_path = Common.url_encode(tmp_path, "/.#@&?!\\=+~");
        if (urlc.getRemoveDoubleEncoding()) {
            tmp_path = URLConnection.remove_double_encoding_of_special_chars(tmp_path);
        }
        data = String.valueOf(data) + tmp_path;
        this.log("S3_CLIENT", 2, "Signing data:----------------\n" + data + "\n----------------");
        urlc.putConfig("canonical_request", data);
        String sign = "";
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(this.secretKey);
            sign = Base64.encodeBytes(mac.doFinal(data.getBytes("UTF8")));
        }
        catch (Exception e) {
            this.log("S3_CLIENT", 1, e);
        }
        return sign;
    }

    protected void setRegionName() {
        String region = this.region_host;
        if (region.contains(":")) {
            region = region.substring(0, region.indexOf(":"));
        }
        if (region.endsWith(".oraclecloud.com") && region.contains("-")) {
            region = region.substring(0, region.indexOf(".oraclecloud.com"));
            this.region_name = region.substring(region.lastIndexOf(".") + 1);
        } else if (!region.equals("s3.amazonaws.com")) {
            this.region_name = region.substring(3).substring(0, region.substring(3).indexOf("."));
        }
    }

    public String calculateAmazonSignaturev4(URLConnection urlc) {
        this.setRegionName();
        return S3Client.calculateAmazonSignaturev4(urlc.getRequestMethod(), urlc.getRequestProps(), urlc.getConfig(), urlc.getURL(), "", urlc.getDate(), this.config.getProperty("s3_bucket_in_path", "false").equals("true"), this.config.getProperty("real_username", this.config.getProperty("username")), this.config.getProperty("real_password", this.config.getProperty("password")), this.region_name, this.region_host, this.yyyyMMddtHHmmssZ, urlc.getRemoveDoubleEncoding());
    }

    public static String calculateAmazonSignaturev4(String verb, Properties request, Properties config, VRL vrl, String src_signed_headers, Date date, boolean bucket_in_path, String user_name, String secret_key, String region_name, String region_host, SimpleDateFormat yyyyMMddtHHmmssZ, boolean remove_double_encoding) {
        try {
            SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyyMMdd", Locale.US);
            yyyymmdd.setTimeZone(TimeZone.getTimeZone("GMT"));
            String amzdate = yyyyMMddtHHmmssZ.format(date);
            String datestamp = yyyymmdd.format(date);
            String bucketName = request.getProperty("HOST", vrl.getHost());
            if (region_host.contains(":")) {
                bucketName = String.valueOf(request.getProperty("HOST", vrl.getHost())) + ":" + vrl.getPort();
            }
            bucketName = bucket_in_path && (vrl.getPath().equals("/") || vrl.getPath().startsWith("/?")) ? "" : (bucket_in_path ? vrl.getPath().substring(1, vrl.getPath().indexOf("/", 1)) : (bucketName.equalsIgnoreCase(region_host) ? "" : bucketName.substring(0, bucketName.toLowerCase().indexOf("." + region_host))));
            String tmp_path = "";
            tmp_path = bucketName.equals("") ? "/" : (vrl.getPath().indexOf("?") >= 0 ? vrl.getPath().substring(0, vrl.getPath().indexOf("?")) : vrl.getPath());
            String canonical_uri = Common.url_encode(tmp_path, "/.#@&?!\\=+~");
            if (remove_double_encoding) {
                canonical_uri = URLConnection.remove_double_encoding_of_special_chars(canonical_uri);
            }
            String canonical_headers = "";
            String signed_headers = "";
            if ((src_signed_headers = ";" + src_signed_headers + ";").indexOf(";date;") >= 0) {
                signed_headers = String.valueOf(signed_headers) + ";date";
                canonical_headers = String.valueOf(canonical_headers) + "date:" + request.getProperty("date") + "\n";
            }
            signed_headers = String.valueOf(signed_headers) + ";host";
            if (bucket_in_path) {
                canonical_headers = String.valueOf(canonical_headers) + "host:" + region_host + "\n";
            } else {
                String host = vrl.getHost();
                if (region_host.contains(":")) {
                    host = String.valueOf(host) + ":" + vrl.getPort();
                }
                canonical_headers = String.valueOf(canonical_headers) + "host:" + host + "\n";
            }
            if (request.containsKey("x-amz-acl")) {
                signed_headers = String.valueOf(signed_headers) + ";x-amz-acl";
                canonical_headers = String.valueOf(canonical_headers) + "x-amz-acl:" + request.getProperty("x-amz-acl") + "\n";
            }
            if (request.containsKey("x-amz-content-sha256")) {
                signed_headers = String.valueOf(signed_headers) + ";x-amz-content-sha256";
                canonical_headers = String.valueOf(canonical_headers) + "x-amz-content-sha256:" + request.getProperty("x-amz-content-sha256") + "\n";
            }
            if (request.containsKey("x-amz-copy-source")) {
                signed_headers = String.valueOf(signed_headers) + ";x-amz-copy-source";
                canonical_headers = String.valueOf(canonical_headers) + "x-amz-copy-source:" + request.getProperty("x-amz-copy-source") + "\n";
            }
            if (request.containsKey("x-amz-copy-source-range")) {
                signed_headers = String.valueOf(signed_headers) + ";x-amz-copy-source-range";
                canonical_headers = String.valueOf(canonical_headers) + "x-amz-copy-source-range:" + request.getProperty("x-amz-copy-source-range") + "\n";
            }
            signed_headers = String.valueOf(signed_headers) + ";x-amz-date";
            canonical_headers = String.valueOf(canonical_headers) + "x-amz-date:" + amzdate + "\n";
            if (request.containsKey("x-amz-meta-md5")) {
                signed_headers = String.valueOf(signed_headers) + ";x-amz-meta-md5";
                canonical_headers = String.valueOf(canonical_headers) + "x-amz-meta-md5:" + request.getProperty("x-amz-meta-md5") + "\n";
            }
            if (request.containsKey("x-amz-meta-modified")) {
                signed_headers = String.valueOf(signed_headers) + ";x-amz-meta-modified";
                canonical_headers = String.valueOf(canonical_headers) + "x-amz-meta-modified:" + request.getProperty("x-amz-meta-modified") + "\n";
            }
            if (request.containsKey("x-amz-meta-uploaded-by")) {
                signed_headers = String.valueOf(signed_headers) + ";x-amz-meta-uploaded-by";
                canonical_headers = String.valueOf(canonical_headers) + "x-amz-meta-uploaded-by:" + request.getProperty("x-amz-meta-uploaded-by") + "\n";
            }
            if (request.containsKey("x-amz-metadata-directive")) {
                signed_headers = String.valueOf(signed_headers) + ";x-amz-metadata-directive";
                canonical_headers = String.valueOf(canonical_headers) + "x-amz-metadata-directive:" + request.getProperty("x-amz-metadata-directive") + "\n";
            }
            if (request.containsKey("x-amz-security-token")) {
                signed_headers = String.valueOf(signed_headers) + ";x-amz-security-token";
                canonical_headers = String.valueOf(canonical_headers) + "x-amz-security-token:" + request.getProperty("x-amz-security-token") + "\n";
            }
            if (request.containsKey("x-amz-server-side-encryption-aws-kms-key-id")) {
                signed_headers = String.valueOf(signed_headers) + ";x-amz-server-side-encryption;x-amz-server-side-encryption-aws-kms-key-id";
                canonical_headers = String.valueOf(canonical_headers) + "x-amz-server-side-encryption:aws:kms\nx-amz-server-side-encryption-aws-kms-key-id:" + request.getProperty("x-amz-server-side-encryption-aws-kms-key-id") + "\n";
            } else if (request.containsKey("x-amz-server-side-encryption")) {
                signed_headers = String.valueOf(signed_headers) + ";x-amz-server-side-encryption";
                canonical_headers = String.valueOf(canonical_headers) + "x-amz-server-side-encryption:" + request.getProperty("x-amz-server-side-encryption") + "\n";
            }
            if (request.containsKey("x-amz-storage-class")) {
                signed_headers = String.valueOf(signed_headers) + ";x-amz-storage-class";
                canonical_headers = String.valueOf(canonical_headers) + "x-amz-storage-class:" + request.getProperty("x-amz-storage-class") + "\n";
            }
            if (signed_headers.startsWith(";")) {
                signed_headers = signed_headers.substring(1);
            }
            String request_parameters = "";
            if (vrl.getPath().indexOf("?") >= 0) {
                request_parameters = vrl.getPath().substring(vrl.getPath().indexOf("?") + 1);
            }
            String canonical_querystring = Common.url_encode(request_parameters, ".#@&?!\\=+~");
            if (remove_double_encoding) {
                canonical_querystring = URLConnection.remove_double_encoding_of_special_chars(canonical_querystring);
            }
            String payload_hash = "UNSIGNED-PAYLOAD";
            String canonical_request = String.valueOf(verb) + '\n' + canonical_uri + '\n' + canonical_querystring + '\n' + canonical_headers + '\n' + signed_headers + '\n' + payload_hash;
            Common.log("S3_CLIENT", 2, "canonical_request:----------------\n" + canonical_request + "\n----------------");
            config.put("canonical_request", canonical_request);
            String algorithm = "AWS4-HMAC-SHA256";
            String credential_scope = String.valueOf(datestamp) + '/' + region_name + '/' + "s3" + '/' + "aws4_request";
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.reset();
            md.update(canonical_request.getBytes("UTF8"));
            String string_to_sign = String.valueOf(algorithm) + '\n' + amzdate + '\n' + credential_scope + '\n' + S3Client.bytesToHex(md.digest());
            Common.log("S3_CLIENT", 2, "string_to_sign:----------------\n" + string_to_sign + "\n----------------");
            byte[] signing_key = S3Client.getSignatureKey(secret_key, datestamp, region_name, "s3");
            String signature = S3Client.bytesToHex(S3Client.HmacSHA256(string_to_sign, signing_key));
            String authorization_header = String.valueOf(algorithm) + " " + "Credential=" + user_name + "/" + credential_scope + ", " + "SignedHeaders=" + signed_headers + ", " + "Signature=" + signature;
            return authorization_header;
        }
        catch (Exception e) {
            Common.log("S3_CLIENT", 2, e);
            return null;
        }
    }

    static byte[] HmacSHA256(String data, byte[] key) throws Exception {
        String algorithm = "HmacSHA256";
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(data.getBytes("UTF8"));
    }

    static byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) throws Exception {
        byte[] kSecret = ("AWS4" + key).getBytes("UTF8");
        byte[] kDate = S3Client.HmacSHA256(dateStamp, kSecret);
        byte[] kRegion = S3Client.HmacSHA256(regionName, kDate);
        byte[] kService = S3Client.HmacSHA256(serviceName, kRegion);
        byte[] kSigning = S3Client.HmacSHA256("aws4_request", kService);
        return kSigning;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int j = 0;
        while (j < bytes.length) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0xF];
            ++j;
        }
        return new String(hexChars);
    }

    public void updateIamAuth() throws Exception {
        if (this.config.getProperty("username").equalsIgnoreCase("iam_lookup")) {
            long expire = Long.parseLong(this.config.getProperty("iam_expire", "0"));
            if (System.currentTimeMillis() - expire > -3600000L) {
                Properties credentials = this.getCredentials(this.config.getProperty("password"));
                if (!credentials.getProperty("Code").equalsIgnoreCase("Success")) {
                    throw new Exception("" + credentials);
                }
                this.config.put("real_username", credentials.getProperty("AccessKeyId"));
                this.config.put("real_password", credentials.getProperty("SecretAccessKey"));
                this.config.put("real_token", credentials.getProperty("Token"));
                this.config.put("iam_expire", String.valueOf(this.yyyyMMddtHHmmssSSSZ.parse(credentials.getProperty("Expiration")).getTime()));
                this.secretKey = new SecretKeySpec(this.config.getProperty("real_password", this.config.getProperty("password")).getBytes("UTF8"), "HmacSHA1");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Properties getCredentials(String instance_profile_name) throws IOException {
        String result;
        int code;
        URLConnection urlc;
        String token = "";
        if (System.getProperty("crushftp.s3_ec2_imdsv2", "false").equals("true")) {
            Properties imdsv2 = new Properties();
            if (s3_imdsv2.containsKey("imdsv2")) {
                imdsv2 = (Properties)s3_imdsv2.get("imdsv2");
            }
            if (System.currentTimeMillis() - Long.parseLong(imdsv2.getProperty("time", String.valueOf(System.currentTimeMillis() - 21600005L))) > 21600000L) {
                try {
                    urlc = URLConnection.openConnection(new VRL("http://169.254.169.254/latest/api/token"), this.config);
                    urlc.setRequestMethod("PUT");
                    urlc.setRequestProperty("X-aws-ec2-metadata-token-ttl-seconds", "21600");
                    urlc.setReadTimeout(3000);
                    code = urlc.getResponseCode();
                    result = URLConnection.consumeResponse(urlc.getInputStream());
                    urlc.disconnect();
                    if (code < 200 || code > 299) {
                        this.log(String.valueOf(result) + "\r\n");
                        throw new IOException(result);
                    }
                    token = result.trim();
                    Properties p = new Properties();
                    p.put("token", token);
                    p.put("time", String.valueOf(System.currentTimeMillis()));
                    Properties properties = s3_imdsv2;
                    synchronized (properties) {
                        s3_imdsv2.put("imdsv2", p);
                    }
                    urlc.disconnect();
                }
                catch (Exception e) {
                    this.log(e);
                    this.log("S3_CLIENT", 1, e);
                }
            } else {
                token = imdsv2.getProperty("token", "");
            }
        }
        boolean contianer_credentials_relative_uri = System.getProperty("crushftp.s3_use_contianer_credentials_relative_uri", "false").equals("true");
        if (instance_profile_name.equals("lookup") && !contianer_credentials_relative_uri) {
            try {
                urlc = URLConnection.openConnection(new VRL("http://169.254.169.254/latest/meta-data/iam/security-credentials/"), this.config);
                urlc.setReadTimeout(3000);
                if (!token.equals("")) {
                    urlc.setRequestProperty("X-aws-ec2-metadata-token", token);
                }
                code = urlc.getResponseCode();
                result = URLConnection.consumeResponse(urlc.getInputStream());
                urlc.disconnect();
                if (code < 200 || code > 299) {
                    this.log(String.valueOf(result) + "\r\n");
                    throw new IOException(result);
                }
                instance_profile_name = result.trim();
                urlc.disconnect();
            }
            catch (Exception e) {
                this.log(e);
                this.log("S3_CLIENT", 1, e);
            }
        }
        String url = "http://169.254.169.254/latest/meta-data/iam/security-credentials/" + instance_profile_name;
        if (contianer_credentials_relative_uri) {
            url = "http://169.254.170.2" + System.getenv("AWS_CONTAINER_CREDENTIALS_RELATIVE_URI");
        }
        URLConnection urlc2 = URLConnection.openConnection(new VRL(url), this.config);
        if (!token.equals("")) {
            urlc2.setRequestProperty("X-aws-ec2-metadata-token", token);
        }
        int code2 = urlc2.getResponseCode();
        String result2 = URLConnection.consumeResponse(urlc2.getInputStream());
        urlc2.disconnect();
        if (code2 < 200 || code2 > 299) {
            this.log(String.valueOf(result2) + "\r\n");
            throw new IOException(result2);
        }
        Properties p = new Properties();
        try {
            Object obj = JSONValue.parse(result2);
            if (obj instanceof JSONArray) {
                JSONArray ja = (JSONArray)obj;
                int xxx = 0;
                while (xxx < ja.size()) {
                    Object obj2 = ja.get(xxx);
                    if (obj2 instanceof JSONObject) {
                        JSONObject jo = (JSONObject)obj2;
                        Object[] a = jo.keySet().toArray();
                        int i = 0;
                        while (i < a.length) {
                            String key2 = a[i].toString();
                            p.put(key2, "" + jo.get(key2));
                            ++i;
                        }
                    }
                    ++xxx;
                }
            } else if (obj instanceof JSONObject) {
                JSONObject jo = (JSONObject)obj;
                Object[] a = jo.keySet().toArray();
                int i = 0;
                while (i < a.length) {
                    String key2 = a[i].toString();
                    p.put(key2, "" + jo.get(key2));
                    ++i;
                }
            }
        }
        catch (Exception e) {
            this.log(e);
            this.log("S3_CLIENT", 1, e);
        }
        if (contianer_credentials_relative_uri && p.size() > 0) {
            p.put("Code", "Success");
        }
        return p;
    }

    private boolean is_folder_recently_created(String path) throws Exception {
        path = this.lower(path);
        if (this.get_cache_item("recently_created_folder_cache").containsKey(path)) {
            if (System.currentTimeMillis() - Long.parseLong(this.get_cache_item("recently_created_folder_cache").getProperty(path, "0")) < 30000L) {
                return true;
            }
            this.get_cache_item("recently_created_folder_cache").remove(path);
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Properties get_cache_item(String cache_type) throws Exception {
        Properties properties = s3_global_cache;
        synchronized (properties) {
            if (s3_global_cache.containsKey(this.cache_reference)) {
                Properties p = (Properties)s3_global_cache.get(this.cache_reference);
                return (Properties)p.get(cache_type);
            }
        }
        return new Properties();
    }

    protected String handle_path_special_chars(String path, boolean encode) {
        String handled_path = path = this.lower(path);
        if (handled_path.contains("%")) {
            handled_path = handled_path.replace("%", encode ? "%25" : "%2525");
        }
        if (handled_path.contains(" ")) {
            handled_path = handled_path.replace(" ", encode ? "%20" : "%2520");
        }
        if (handled_path.contains("+")) {
            handled_path = handled_path.replace("+", encode ? "%2B" : "%252B");
        }
        if (handled_path.contains("&")) {
            handled_path = handled_path.replace("&", encode ? "%26" : "%2526");
        }
        if (handled_path.contains("$")) {
            handled_path = handled_path.replace("$", encode ? "%24" : "%2524");
        }
        if (handled_path.contains("@")) {
            handled_path = handled_path.replace("@", encode ? "%40" : "%2540");
        }
        if (handled_path.contains("=")) {
            handled_path = handled_path.replace("=", encode ? "%3D" : "%253D");
        }
        if (handled_path.contains(":")) {
            handled_path = handled_path.replace(":", encode ? "%3A" : "%253A");
        }
        if (handled_path.contains(",")) {
            handled_path = handled_path.replace(",", encode ? "%2C" : "%252C");
        }
        if (handled_path.contains("?")) {
            handled_path = handled_path.replace("?", encode ? "%3F" : "%253F");
        }
        if (handled_path.contains("!")) {
            handled_path = handled_path.replace("!", encode ? "%21" : "%2521");
        }
        if (handled_path.contains("#")) {
            handled_path = handled_path.replace("#", encode ? "%23" : "%2523");
        }
        if (encode) {
            handled_path = Common.url_encode(handled_path, "\\");
        }
        return handled_path;
    }

    public String lower(String s) {
        if (System.getProperty("crushftp.lowercase_all_s3_paths", "false").equals("true")) {
            return s.toLowerCase();
        }
        return s;
    }

    public long set_power_of_two_delay(long delay, int retry_attempt) {
        long power_of_two_delay = (long)((double)delay * Math.pow(2.0, retry_attempt));
        if (power_of_two_delay >= 1500L) {
            return 1500L;
        }
        return power_of_two_delay;
    }

    public String getErrorInfo(String result, int code, String canonical_request, Properties error_config) {
        try {
            if (!result.equals("")) {
                Element error;
                this.log("S3_CLIENT", 2, result);
                if (result.indexOf("<?xml version=") >= 0 && (error = Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(result.getBytes("UTF8"))).getRootElement()) != null) {
                    String error_code = S3Client.getElement(error, "Code") == null ? "" : S3Client.getElement(error, "Code").getText();
                    String error_message = S3Client.getElement(error, "Message") == null ? "" : S3Client.getElement(error, "Message").getText();
                    String error_resource = S3Client.getElement(error, "Resource") == null ? "" : S3Client.getElement(error, "Resource").getText();
                    result = "Error Code : " + code + " " + error_code + " Error Message: " + error_message + "Resource : " + error_resource + " URL:" + new VRL(this.url).safe();
                    if (error_code.contains("SignatureDoesNotMatch") && canonical_request != null && !canonical_request.equals("")) {
                        String canonical_request_aws;
                        String string = canonical_request_aws = S3Client.getElement(error, "CanonicalRequest") == null ? "" : S3Client.getElement(error, "CanonicalRequest").getText();
                        if (canonical_request_aws.equals("")) {
                            String string2 = canonical_request_aws = S3Client.getElement(error, "StringToSign") == null ? "" : S3Client.getElement(error, "StringToSign").getText();
                        }
                        if (!canonical_request_aws.equals("")) {
                            if (error_config != null && error_config.getProperty("login_error", "").equals("true") && canonical_request_aws.trim().equals(canonical_request.trim())) {
                                result = "ERROR : Bad credentials : Invalid Password! Error Code : " + code + " " + error_code;
                            } else {
                                String difference = "";
                                BufferedReader reader1 = new BufferedReader(new StringReader(canonical_request.trim()));
                                BufferedReader reader2 = new BufferedReader(new StringReader(canonical_request_aws.trim()));
                                String line1 = null;
                                String line2 = null;
                                int line = 1;
                                while ((line1 = reader1.readLine()) != null && (line2 = reader2.readLine()) != null) {
                                    if (!line1.equals(line2)) {
                                        difference = String.valueOf(difference) + "Line " + line + " Signed : " + line1 + " Expected : " + line2 + "\n\r";
                                    }
                                    ++line;
                                }
                                result = String.valueOf(result) + " Difference : " + difference;
                            }
                        }
                    }
                    if (error_code.contains("InvalidAccessKeyId")) {
                        result = "ERROR : Bad credentials : Invalid User name! Error Code : " + code + " " + error_code + " The given Access Key Id : " + this.config.getProperty("username", "");
                    }
                }
            }
        }
        catch (Exception e) {
            this.log("S3_CLIENT", 2, e);
        }
        return result;
    }
}

