/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Base64;
import com.crushftp.client.Common;
import com.crushftp.client.GenericClient;
import com.crushftp.client.URLConnection;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class B2Client
extends GenericClient {
    String token = "";
    String downloadUrl = "";
    String account_id = "";
    String api_url = "";
    static Properties resourceIdCache = new Properties();
    static Properties bucketIdCache = new Properties();

    public B2Client(String url, String header, Vector log) {
        super(header, log);
        this.fields = new String[]{"username", "password", "uploaded_by", "uploaded_md5"};
        this.url = url;
    }

    @Override
    public String login2(String username, String password, String clientid) throws Exception {
        JSONObject allowed;
        this.config.put("username", username.trim());
        this.config.put("password", VRL.vrlDecode(password.trim()));
        URLConnection urlc = URLConnection.openConnection(new VRL("https://api.backblazeb2.com/b2api/v2/b2_authorize_account"), this.config);
        urlc.setRequestMethod("GET");
        urlc.setDoInput(true);
        urlc.setDoOutput(true);
        urlc.setUseCaches(false);
        urlc.setRequestProperty("Authorization", "Basic  " + Base64.encodeBytes((String.valueOf(this.config.getProperty("username")) + ":" + this.config.getProperty("password")).getBytes()));
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        if (urlc.getResponseCode() < 200 || urlc.getResponseCode() > 299) {
            this.log(String.valueOf(urlc.getResponseCode()) + result + "\r\n");
            throw new IOException(result);
        }
        JSONObject obj = (JSONObject)JSONValue.parse(result);
        this.token = (String)obj.get("authorizationToken");
        this.account_id = (String)obj.get("accountId");
        this.api_url = (String)obj.get("apiUrl");
        this.downloadUrl = (String)obj.get("downloadUrl");
        if (obj.containsKey("allowed") && obj.get("allowed") != null && (allowed = (JSONObject)obj.get("allowed")).containsKey("bucketName") && allowed.containsKey("bucketId")) {
            String bucketName = (String)allowed.get("bucketName");
            String bucketId = (String)allowed.get("bucketId");
            if (bucketName != null && bucketId != null && !bucketName.equals("") && !bucketId.equals("")) {
                bucketIdCache.put(String.valueOf(this.config.getProperty("password")) + bucketName, bucketId);
            }
        }
        this.config.put("logged_out", "false");
        return "Success!";
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        if (path.equals("/")) {
            return this.listBuckets(list);
        }
        return this.listFiles("b2_list_file_names", path, list, true, 1000);
    }

    public Vector listBuckets(Vector list) throws Exception {
        URLConnection urlc = URLConnection.openConnection(new VRL(String.valueOf(this.api_url) + "/b2api/v2/b2_list_buckets"), this.config);
        urlc.setRequestMethod("POST");
        urlc.setDoInput(true);
        urlc.setDoOutput(true);
        urlc.setUseCaches(false);
        urlc.setRequestProperty("Authorization", this.token);
        JSONObject postData = new JSONObject();
        postData.put("accountId", this.config.getProperty("username"));
        JSONArray bucketTypes = new JSONArray();
        bucketTypes.add("allPrivate");
        bucketTypes.add("allPublic");
        postData.put("bucketTypes", bucketTypes);
        OutputStream out = urlc.getOutputStream();
        out.write(postData.toString().getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        urlc.disconnect();
        if (code < 200 || code > 299) {
            this.log(String.valueOf(urlc.getResponseCode()) + result + "\r\n");
            throw new IOException(result);
        }
        Vector<Properties> list2 = new Vector<Properties>();
        SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        Object obj = ((JSONObject)JSONValue.parse(result)).get("buckets");
        if (obj instanceof JSONArray) {
            JSONArray ja = (JSONArray)obj;
            int xxx = 0;
            while (xxx < ja.size()) {
                Object obj2 = ja.get(xxx);
                if (obj2 instanceof JSONObject) {
                    JSONObject jo = (JSONObject)obj2;
                    Date d = new Date();
                    String line = "drwxrwxrwx   1    owner   group   0   " + yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + jo.get("bucketName");
                    Properties stat = B2Client.parseStat(line);
                    stat.put("url", "b2://" + this.config.getProperty("username") + ":" + VRL.vrlEncode(this.config.getProperty("password")) + "@api.backblaze.com/" + stat.getProperty("name") + "/");
                    stat.put("bucketId", jo.get("bucketId"));
                    bucketIdCache.put(String.valueOf(this.config.getProperty("password")) + stat.getProperty("name"), jo.get("bucketId"));
                    list2.addElement(stat);
                }
                ++xxx;
            }
        }
        list.addAll(list2);
        return list;
    }

    public Vector listFiles(String command, String path, Vector list, boolean ignore_bzEmpty, int max) throws Exception {
        String bucketName = path.substring(1, path.indexOf("/", 1));
        if ((path = path.substring(bucketName.length() + 1)).startsWith("/")) {
            path = path.substring(1);
        }
        String startFileName = "";
        int x = 0;
        while (x < 1000) {
            if (this.config.getProperty("logged_out", "false").equals("true")) {
                throw new Exception("Error: Cancel dir listing. The client is already closed.");
            }
            URLConnection urlc = URLConnection.openConnection(new VRL(String.valueOf(this.api_url) + "/b2api/v2/" + command), this.config);
            urlc.setRequestMethod("POST");
            urlc.setDoInput(true);
            urlc.setDoOutput(true);
            urlc.setUseCaches(false);
            urlc.setRequestProperty("Authorization", this.token);
            JSONObject postData = new JSONObject();
            if (!bucketIdCache.containsKey(String.valueOf(this.config.getProperty("password")) + bucketName)) {
                Vector buckets = new Vector();
                this.listBuckets(buckets);
            }
            postData.put("bucketId", bucketIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + bucketName));
            postData.put("maxFileCount", new Integer(max));
            if (!startFileName.equals("")) {
                postData.put("startFileName", startFileName);
            }
            postData.put("prefix", path);
            if (ignore_bzEmpty) {
                postData.put("delimiter", "/");
            }
            OutputStream out = urlc.getOutputStream();
            out.write(postData.toString().getBytes("UTF8"));
            out.close();
            String result = URLConnection.consumeResponse(urlc.getInputStream());
            int code = urlc.getResponseCode();
            if (code < 200 || code > 299) {
                this.log(String.valueOf(urlc.getResponseCode()) + result + "\r\n");
                throw new IOException(result);
            }
            urlc.disconnect();
            this.parseListItems(list, path, ignore_bzEmpty, bucketName, result);
            if (max <= 1 || ((JSONObject)JSONValue.parse(result)).get("nextFileName") == null) break;
            startFileName = (String)((JSONObject)JSONValue.parse(result)).get("nextFileName");
            ++x;
        }
        return list;
    }

    private void parseListItems(Vector list, String path, boolean ignore_bzEmpty, String bucketName, String result) throws ParseException, Exception {
        SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        Object obj = ((JSONObject)JSONValue.parse(result)).get("files");
        if (obj instanceof JSONArray) {
            JSONArray ja = (JSONArray)obj;
            int xxx = 0;
            while (xxx < ja.size()) {
                Object obj2 = ja.get(xxx);
                if (obj2 instanceof JSONObject) {
                    JSONObject jo = (JSONObject)obj2;
                    boolean folder = false;
                    String name = (String)jo.get("fileName");
                    if (!ignore_bzEmpty || !name.endsWith("/.bzEmpty")) {
                        long mdtm = -1L;
                        JSONObject file_info = (JSONObject)jo.get("fileInfo");
                        if (((String)jo.get("action")).endsWith("folder")) {
                            folder = true;
                            name = name.substring(path.length(), name.indexOf("/", path.length()));
                        } else {
                            name = name.substring(path.length());
                            if (file_info.get("modified_date_time") != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
                                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                                mdtm = sdf.parse((String)file_info.get("modified_date_time")).getTime();
                            } else if (file_info.get("src_last_modified_millis") != null) {
                                mdtm = Long.parseLong((String)file_info.get("src_last_modified_millis"));
                            }
                        }
                        Date d = new Date();
                        if (mdtm > 0L) {
                            d.setTime(mdtm);
                        }
                        String line = String.valueOf(folder ? "d" : "-") + "rwxrwxrwx   1    owner   group   " + jo.get("contentLength") + "   " + yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + name;
                        Properties stat = B2Client.parseStat(line);
                        stat.put("b2_file_name", (String)jo.get("fileName"));
                        if (jo.get("fileId") != null) {
                            stat.put("fileId", (String)jo.get("fileId"));
                        }
                        stat.put("url", "b2://" + this.config.getProperty("username") + ":" + VRL.vrlEncode(this.config.getProperty("password")) + "@api.backblaze.com/" + bucketName + "/" + path + stat.getProperty("name") + (folder ? "/" : ""));
                        if (file_info.get("uploaded_by") != null) {
                            stat.put("uploaded_by", (String)file_info.get("uploaded_by"));
                        }
                        list.addElement(stat);
                    }
                }
                ++xxx;
            }
        }
    }

    @Override
    public Properties stat(String path) throws Exception {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        Vector v = new Vector();
        this.list(Common.all_but_last(path), v);
        int x = 0;
        while (x < v.size()) {
            Properties p = (Properties)v.elementAt(x);
            if (p.getProperty("name").equals(Common.last(path))) {
                return p;
            }
            ++x;
        }
        return null;
    }

    @Override
    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        URLConnection urlc = URLConnection.openConnection(new VRL(String.valueOf(this.downloadUrl) + "/file" + path), this.config);
        urlc.setRequestMethod("GET");
        urlc.setDoInput(true);
        urlc.setDoOutput(true);
        urlc.setUseCaches(false);
        urlc.setRequestProperty("Content-Type", "application/json");
        urlc.setRequestProperty("Authorization", this.token);
        this.in = urlc.getInputStream();
        return this.in;
    }

    @Override
    protected OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        if (!path.substring(1).contains("/")) {
            throw new Exception("Cannot upload on Bucket level!!!");
        }
        String file_name = path.substring(("/" + Common.first(path.substring(1)) + "/").length());
        URLConnection urlc = URLConnection.openConnection(new VRL(String.valueOf(this.api_url) + "/b2api/v2/b2_start_large_file"), this.config);
        urlc.setRequestMethod("POST");
        urlc.setDoInput(true);
        urlc.setDoOutput(true);
        urlc.setUseCaches(false);
        urlc.setRequestProperty("Content-Type", "application/json");
        urlc.setRequestProperty("Authorization", this.token);
        JSONObject postData = new JSONObject();
        postData.put("fileName", Common.url_encode(file_name, "/.#@&?!\\=+~"));
        postData.put("contentType", "application/octet-stream");
        String bucket_id = bucketIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + Common.first(path.substring(1)), "");
        if (bucket_id.equals("")) {
            this.list("/", new Vector());
            bucket_id = bucketIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + Common.first(path.substring(1)), "");
        }
        postData.put("bucketId", bucket_id);
        OutputStream out = urlc.getOutputStream();
        out.write(postData.toString().getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        if (code < 200 || code > 299) {
            this.log(String.valueOf(urlc.getResponseCode()) + result + "\r\n");
            throw new IOException(result);
        }
        urlc.disconnect();
        JSONObject obj = (JSONObject)JSONValue.parse(result);
        String file_id = (String)obj.get("fileId");
        URLConnection urlc2 = URLConnection.openConnection(new VRL(String.valueOf(this.api_url) + "/b2api/v2/b2_get_upload_part_url"), this.config);
        urlc2.setRequestMethod("POST");
        urlc2.setDoInput(true);
        urlc2.setDoOutput(true);
        urlc2.setUseCaches(false);
        urlc2.setRequestProperty("Authorization", this.token);
        urlc2.setRequestProperty("Content-Type", "application/json");
        JSONObject postData2 = new JSONObject();
        postData2.put("fileId", file_id);
        OutputStream out2 = urlc2.getOutputStream();
        out2.write(postData2.toString().getBytes("UTF8"));
        out2.close();
        int code2 = urlc2.getResponseCode();
        String result2 = URLConnection.consumeResponse(urlc2.getInputStream());
        if (code2 < 200 || code2 > 299) {
            this.log(String.valueOf(urlc2.getResponseCode()) + result2 + "\r\n");
            throw new IOException(result2);
        }
        urlc2.disconnect();
        JSONObject obj2 = (JSONObject)JSONValue.parse(result2);
        String upload_auth_token = (String)obj2.get("authorizationToken");
        String upload_url = (String)obj2.get("uploadUrl");
        String upload_bucket_id = bucket_id;
        class OutputWrapper
        extends OutputStream {
            boolean closed = false;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(0x500000);
            long pos = 0L;
            long final_size = 0L;
            boolean first_flush = true;
            final Properties status = new Properties();
            int part_number = 0;
            JSONArray all_hexSha1 = new JSONArray();
            boolean is_not_small_file = false;
            private final /* synthetic */ String val$file_id;
            private final /* synthetic */ String val$upload_bucket_id;
            private final /* synthetic */ String val$file_name;
            private final /* synthetic */ String val$path;
            private final /* synthetic */ String val$upload_url;
            private final /* synthetic */ String val$upload_auth_token;

            OutputWrapper(String string, String string2, String string3, String string4, String string5, String string6) {
                this.val$file_id = string;
                this.val$upload_bucket_id = string2;
                this.val$file_name = string3;
                this.val$path = string4;
                this.val$upload_url = string5;
                this.val$upload_auth_token = string6;
            }

            @Override
            public void write(int i) throws IOException {
                this.write(new byte[]{(byte)i}, 0, 1);
            }

            @Override
            public void write(byte[] b) throws IOException {
                this.write(b, 0, b.length);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                if (this.baos.size() + len > 0x500000) {
                    int chunks = (this.baos.size() + len) / 0x500000;
                    int diff = this.baos.size() + len - chunks * 0x500000;
                    int offset_len = len - diff;
                    this.baos.write(b, off, offset_len);
                    long tmp_pos = this.baos.size();
                    this.flushNow();
                    this.pos += tmp_pos;
                    this.baos.write(b, offset_len, len - offset_len);
                } else {
                    this.baos.write(b, off, len);
                }
            }

            @Override
            public void close() throws IOException {
                if (this.closed) {
                    return;
                }
                if (!this.is_not_small_file && this.baos.size() <= 0x500000) {
                    try {
                        URLConnection urlc = URLConnection.openConnection(new VRL(String.valueOf(B2Client.this.api_url) + "/b2api/v2/b2_cancel_large_file"), B2Client.this.config);
                        urlc.setRequestMethod("POST");
                        urlc.setDoInput(true);
                        urlc.setDoOutput(true);
                        urlc.setUseCaches(false);
                        urlc.setRequestProperty("Authorization", B2Client.this.token);
                        urlc.setRequestProperty("Content-Type", "application/json");
                        JSONObject postData2 = new JSONObject();
                        postData2.put("fileId", this.val$file_id);
                        OutputStream out = urlc.getOutputStream();
                        out.write(postData2.toString().getBytes("UTF8"));
                        out.close();
                        int code = urlc.getResponseCode();
                        String result = URLConnection.consumeResponse(urlc.getInputStream());
                        if (code < 200 || code > 299) {
                            B2Client.this.log(String.valueOf(urlc.getResponseCode()) + result + "\r\n");
                        }
                        urlc.disconnect();
                    }
                    catch (Exception e) {
                        B2Client.this.log(e);
                    }
                    byte[] b_flush = this.baos.toByteArray();
                    Exception e = null;
                    boolean succeeded = false;
                    int i = 0;
                    while (i < 5 && !succeeded) {
                        block21: {
                            try {
                                URLConnection urlc3 = URLConnection.openConnection(new VRL(String.valueOf(B2Client.this.api_url) + "/b2api/v2/b2_get_upload_url"), B2Client.this.config);
                                urlc3.setRequestMethod("POST");
                                urlc3.setDoInput(true);
                                urlc3.setDoOutput(true);
                                urlc3.setUseCaches(false);
                                urlc3.setRequestProperty("Authorization", B2Client.this.token);
                                urlc3.setRequestProperty("Content-Type", "application/json");
                                JSONObject postData3 = new JSONObject();
                                postData3.put("bucketId", this.val$upload_bucket_id);
                                OutputStream out3 = urlc3.getOutputStream();
                                out3.write(postData3.toString().getBytes("UTF8"));
                                out3.close();
                                int code3 = urlc3.getResponseCode();
                                String result3 = URLConnection.consumeResponse(urlc3.getInputStream());
                                if (code3 < 200 || code3 > 299) {
                                    B2Client.this.log(String.valueOf(urlc3.getResponseCode()) + result3 + "\r\n");
                                }
                                urlc3.disconnect();
                                JSONObject obj3 = (JSONObject)JSONValue.parse(result3);
                                String small_upload_auth_token = (String)obj3.get("authorizationToken");
                                String small_upload_url = (String)obj3.get("uploadUrl");
                                URL url_java = new URL(small_upload_url);
                                HttpURLConnection urlc2 = (HttpURLConnection)url_java.openConnection();
                                urlc2.setRequestMethod("POST");
                                urlc2.setReadTimeout(30000);
                                urlc2.setConnectTimeout(6000);
                                urlc2.setDoOutput(true);
                                urlc2.setUseCaches(false);
                                urlc2.setRequestProperty("Authorization", small_upload_auth_token);
                                MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");
                                sha1Digest.update(b_flush, 0, b_flush.length);
                                byte[] sha1Buf = sha1Digest.digest();
                                String hexSha1 = String.format("%040x", new BigInteger(1, sha1Buf));
                                urlc2.setRequestProperty("X-Bz-File-Name", Common.url_encode(this.val$file_name, "/.#@&?!\\=+~"));
                                urlc2.setRequestProperty("X-Bz-Content-Sha1", hexSha1);
                                urlc2.setRequestProperty("Content-Type", "application/octet-stream");
                                urlc2.connect();
                                urlc2.getOutputStream().write(b_flush);
                                urlc2.getOutputStream().close();
                                int code2 = urlc2.getResponseCode();
                                String result2 = URLConnection.consumeResponse(urlc2.getInputStream());
                                if (code2 < 200 || code2 > 299) {
                                    B2Client.this.log("Upload path :" + this.val$path + "Error : code :" + code2 + " " + urlc2.getResponseMessage());
                                }
                                urlc2.disconnect();
                            }
                            catch (Exception e1) {
                                B2Client.this.log(e1);
                                if (e == null) {
                                    e = e1;
                                }
                                break block21;
                            }
                            e = null;
                            succeeded = true;
                        }
                        ++i;
                    }
                    if (e != null) {
                        throw new IOException(e);
                    }
                    this.closed = true;
                    return;
                }
                this.flushNow();
                try {
                    int loops = 0;
                    while (this.status.getProperty("status", "").equals("") && loops++ < 1000) {
                        Thread.sleep(100L);
                    }
                    if (loops >= 998) {
                        throw new IOException("100 second timeout while waiting for prior dropbox chunk to complete..." + loops + ":" + this.pos + ":" + this.pos);
                    }
                    if (this.status.getProperty("status", "").equals("error")) {
                        throw new IOException(this.status.getProperty("error_message", ""));
                    }
                    if (this.status.containsKey("size")) {
                        this.final_size += Long.parseLong(this.status.getProperty("size"));
                        this.status.remove("size");
                        this.all_hexSha1.add(this.status.remove("hexSha1"));
                    }
                }
                catch (InterruptedException e) {
                    B2Client.this.log(e);
                }
                try {
                    URLConnection urlc = URLConnection.openConnection(new VRL(String.valueOf(B2Client.this.api_url) + "/b2api/v2/b2_finish_large_file"), B2Client.this.config);
                    urlc.setDoOutput(true);
                    urlc.setDoInput(true);
                    urlc.setRequestMethod("POST");
                    urlc.setRequestProperty("Authorization", B2Client.this.token);
                    urlc.setRequestProperty("Content-Type", "application/json");
                    JSONObject postData = new JSONObject();
                    postData.put("fileId", this.val$file_id);
                    postData.put("partSha1Array", this.all_hexSha1);
                    OutputStream out = urlc.getOutputStream();
                    out.write(postData.toString().getBytes("UTF8"));
                    out.close();
                    int code = urlc.getResponseCode();
                    String result = URLConnection.consumeResponse(urlc.getInputStream());
                    if (code < 200 || code > 299) {
                        B2Client.this.log(String.valueOf(urlc.getResponseCode()) + result + "\r\n");
                        throw new IOException(result);
                    }
                    urlc.disconnect();
                }
                catch (Exception e) {
                    B2Client.this.log(e);
                    throw new IOException("Upload error : " + e.getMessage());
                }
                this.closed = true;
            }

            public void flushNow() throws IOException {
                if (this.baos.size() > 0) {
                    this.is_not_small_file = true;
                    int loops = 0;
                    while (this.status.getProperty("status", "").equals("") && loops++ < 1500 && !this.first_flush) {
                        try {
                            Thread.sleep(100L);
                        }
                        catch (InterruptedException e) {
                            B2Client.this.log(e);
                        }
                    }
                    if (loops >= 1495) {
                        throw new IOException("100 second timeout while waiting for prior dropbox chunk to complete..." + loops + ":" + this.pos + ":" + this.pos);
                    }
                    if (this.status.getProperty("status", "").equals("error")) {
                        throw new IOException(this.status.getProperty("error_message", ""));
                    }
                    if (this.status.containsKey("size")) {
                        this.final_size += Long.parseLong(this.status.getProperty("size"));
                        this.status.remove("size");
                        this.all_hexSha1.add(this.status.remove("hexSha1"));
                    }
                    this.first_flush = false;
                    this.status.put("status", "");
                    long pos_now = this.pos;
                    final byte[] b_flush = this.baos.toByteArray();
                    final int current_part_number = ++this.part_number;
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            try {
                                URL url_java = new URL(val$upload_url);
                                HttpURLConnection urlc = (HttpURLConnection)url_java.openConnection();
                                urlc.setRequestMethod("POST");
                                urlc.setDoInput(true);
                                urlc.setDoOutput(true);
                                urlc.setUseCaches(false);
                                urlc.setRequestProperty("Authorization", val$upload_auth_token);
                                urlc.setRequestProperty("X-Bz-Part-Number", String.valueOf(current_part_number));
                                urlc.setRequestProperty("Content-Length", String.valueOf(b_flush.length));
                                MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");
                                sha1Digest.update(b_flush, 0, b_flush.length);
                                byte[] sha1Buf = sha1Digest.digest();
                                String hexSha1 = String.format("%040x", new BigInteger(1, sha1Buf));
                                urlc.setRequestProperty("X-Bz-Content-Sha1", hexSha1);
                                urlc.connect();
                                urlc.getOutputStream().write(b_flush);
                                urlc.getOutputStream().close();
                                int code = urlc.getResponseCode();
                                String result = URLConnection.consumeResponse(urlc.getInputStream());
                                if (code < 200 || code > 299) {
                                    B2Client.this.log("Upload path :" + val$path + "Error : code :" + code + " " + urlc.getResponseMessage());
                                    throw new Exception("Upload error : Upload path :" + val$path + "Error : code :" + code + " " + urlc.getResponseMessage());
                                }
                                urlc.disconnect();
                                status.put("size", String.valueOf(b_flush.length));
                                status.put("hexSha1", hexSha1);
                                status.put("status", "ok");
                            }
                            catch (Exception e) {
                                B2Client.this.log(e);
                                status.put("status", "error");
                                status.put("error_message", e.getMessage());
                            }
                        }
                    });
                    this.baos.reset();
                }
            }
        }
        out = new OutputWrapper(file_id, upload_bucket_id, file_name, path, upload_url, upload_auth_token);
        return out;
    }

    @Override
    public boolean delete(String path) throws Exception {
        if (!path.substring(1).contains("/")) {
            throw new Exception("Cannot delete on Bucket level!");
        }
        Properties p = this.stat(path);
        Vector<Properties> files = new Vector<Properties>();
        if (!p.containsKey("fileId")) {
            String folder_path = path;
            this.listFiles("b2_list_file_names", folder_path, files, false, 1000);
            if (files.size() == 0) {
                throw new Exception("Could not found file id of the given path :" + path);
            }
        } else {
            files.add(p);
        }
        IOException e = null;
        int x = 0;
        while (x < files.size()) {
            block8: {
                try {
                    Properties pp = (Properties)files.get(x);
                    this.delete_version(pp.getProperty("fileId"), pp.getProperty("b2_file_name", ""));
                }
                catch (Exception de) {
                    this.log(de);
                    if (e != null) break block8;
                    e = new IOException(de.getMessage());
                }
            }
            ++x;
        }
        if (e != null) {
            throw e;
        }
        return true;
    }

    private void delete_version(String file_id, String b2_file_name) throws Exception {
        URLConnection urlc = URLConnection.openConnection(new VRL(String.valueOf(this.api_url) + "/b2api/v1/b2_delete_file_version"), this.config);
        urlc.setRequestMethod("POST");
        urlc.setDoInput(true);
        urlc.setDoOutput(true);
        urlc.setUseCaches(false);
        urlc.setRequestProperty("Authorization", this.token);
        JSONObject postData = new JSONObject();
        postData.put("fileId", file_id);
        postData.put("fileName", b2_file_name);
        OutputStream out = urlc.getOutputStream();
        out.write(postData.toString().getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        urlc.disconnect();
        if (code < 200 || code > 299) {
            this.log(String.valueOf(urlc.getResponseCode()) + result + "\r\n");
            throw new IOException(result);
        }
    }

    @Override
    public boolean makedir(String path) throws Exception {
        if (!path.substring(1).contains("/")) {
            throw new Exception("Cannot create folder on Bucket level!");
        }
        String bucket_id = bucketIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + Common.first(path.substring(1)), "");
        if (bucket_id.equals("")) {
            this.list("/", new Vector());
            bucket_id = bucketIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + Common.first(path.substring(1)), "");
        }
        String b2_path = path.substring(("/" + Common.first(path.substring(1)) + "/").length());
        URLConnection urlc = URLConnection.openConnection(new VRL(String.valueOf(this.api_url) + "/b2api/v2/b2_get_upload_url"), this.config);
        urlc.setRequestMethod("POST");
        urlc.setDoInput(true);
        urlc.setDoOutput(true);
        urlc.setUseCaches(false);
        urlc.setRequestProperty("Authorization", this.token);
        urlc.setRequestProperty("Content-Type", "application/json");
        JSONObject postData3 = new JSONObject();
        postData3.put("bucketId", bucket_id);
        OutputStream out = urlc.getOutputStream();
        out.write(postData3.toString().getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        if (code < 200 || code > 299) {
            this.log(String.valueOf(urlc.getResponseCode()) + result + "\r\n");
            urlc.disconnect();
            return false;
        }
        urlc.disconnect();
        JSONObject obj = (JSONObject)JSONValue.parse(result);
        String folder_upload_auth_token = (String)obj.get("authorizationToken");
        String folder_upload_url = (String)obj.get("uploadUrl");
        URLConnection urlc2 = URLConnection.openConnection(new VRL(folder_upload_url), this.config);
        urlc2.setRequestMethod("POST");
        urlc2.setDoInput(true);
        urlc2.setDoOutput(true);
        urlc2.setUseCaches(false);
        urlc2.setRequestProperty("Authorization", folder_upload_auth_token);
        urlc2.setRequestProperty("X-Bz-File-Name", Common.url_encode(String.valueOf(b2_path) + ".bzEmpty", "/.#@&?!\\=+~"));
        urlc2.setRequestProperty("X-Bz-Content-Sha1", "da39a3ee5e6b4b0d3255bfef95601890afd80709");
        urlc2.setRequestProperty("Content-Length", "0");
        urlc2.setRequestProperty("Content-Type", "text/plain");
        urlc2.getOutputStream().close();
        int code2 = urlc2.getResponseCode();
        if (code2 < 200 || code2 > 299) {
            this.log("Create directory path :" + path + "Error : code :" + code2 + " " + urlc2.getResponseMessage());
            this.log("Response" + URLConnection.consumeResponse(urlc2.getInputStream()));
            urlc2.disconnect();
            return false;
        }
        urlc2.disconnect();
        return true;
    }

    @Override
    public boolean makedirs(String path) throws Exception {
        boolean ok = true;
        String[] parts = path.split("/");
        String path2 = "";
        int x = 0;
        while (x < parts.length && ok) {
            path2 = String.valueOf(path2) + parts[x] + "/";
            if (x >= 1 && this.stat(path2) == null) {
                ok = this.makedir(path2);
            }
            ++x;
        }
        return ok;
    }

    @Override
    public boolean rename(String rnfr, String rnto, boolean overwrite) throws Exception {
        Properties p = this.stat(rnfr);
        if (!rnfr.substring(1).contains("/")) {
            this.log("Cannot rename folder! Bucket rename not allowed.");
            throw new Exception("FAILURE: Renaming of Cloud (sub)Directories is not supported, aborting rename operation.");
        }
        if (!p.containsKey("fileId")) {
            this.log("Cannot rename folder! B2 REST API does not support renaming folders.");
            throw new Exception("FAILURE: Renaming of Cloud (sub)Directories is not supported, aborting rename operation.");
        }
        String b2_rnto_path = rnto.substring(("/" + Common.first(rnto.substring(1)) + "/").length());
        URLConnection urlc = URLConnection.openConnection(new VRL(String.valueOf(this.api_url) + "/b2api/v2/b2_copy_file"), this.config);
        urlc.setRequestMethod("POST");
        urlc.setDoInput(true);
        urlc.setDoOutput(true);
        urlc.setUseCaches(false);
        urlc.setRequestProperty("Authorization", this.token);
        JSONObject postData = new JSONObject();
        postData.put("sourceFileId", p.getProperty("fileId"));
        postData.put("fileName", b2_rnto_path);
        OutputStream out = urlc.getOutputStream();
        out.write(postData.toString().getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        if (code < 200 || code > 299) {
            this.log("Rename from path :" + rnfr + " to " + rnto + " Error : code :" + code + " " + urlc.getResponseMessage());
            this.log("Result : " + URLConnection.consumeResponse(urlc.getInputStream()));
            urlc.disconnect();
            return false;
        }
        urlc.disconnect();
        return this.delete(rnfr);
    }

    @Override
    public boolean mdtm(String path, long modified) throws Exception {
        return this.update_file_info(path, modified);
    }

    private boolean update_file_info(final String path, long modified) throws Exception {
        Properties p = this.stat(path);
        URLConnection urlc = URLConnection.openConnection(new VRL(String.valueOf(this.api_url) + "/b2api/v2/b2_copy_file"), this.config);
        urlc.setRequestMethod("POST");
        urlc.setDoInput(true);
        urlc.setDoOutput(true);
        urlc.setUseCaches(false);
        urlc.setRequestProperty("Authorization", this.token);
        JSONObject postData = new JSONObject();
        postData.put("sourceFileId", p.getProperty("fileId"));
        String file_name = path.substring(("/" + Common.first(path.substring(1)) + "/").length());
        postData.put("fileName", Common.url_encode(file_name, "/.#@&?!\\=+~"));
        postData.put("metadataDirective", "REPLACE");
        postData.put("contentType", "application/octet-stream");
        JSONObject fileInfo = new JSONObject();
        if (modified > 0L) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            fileInfo.put("modified_date_time", sdf.format(new Date(modified)));
        }
        fileInfo.put("uploaded_by", this.config.getProperty("uploaded_by", ""));
        fileInfo.put("md5", this.config.getProperty("uploaded_md5", ""));
        postData.put("fileInfo", fileInfo);
        OutputStream out = urlc.getOutputStream();
        out.write(postData.toString().getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        if (code < 200 || code > 299) {
            this.log("Result : " + result);
            urlc.disconnect();
            return false;
        }
        urlc.disconnect();
        JSONObject jo = (JSONObject)JSONValue.parse(result);
        final String file_id = (String)jo.get("fileId");
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                try {
                    Vector file_versions = new Vector();
                    B2Client.this.listFiles("b2_list_file_versions", path, file_versions, false, 1000);
                    if (file_versions.size() == 0) {
                        throw new Exception("Could not found file id of the given path :" + path);
                    }
                    int x = 0;
                    while (x < file_versions.size()) {
                        Properties pp = (Properties)file_versions.get(x);
                        if (!pp.getProperty("fileId").equals(file_id)) {
                            B2Client.this.delete_version(pp.getProperty("fileId"), pp.getProperty("b2_file_name", ""));
                        }
                        ++x;
                    }
                }
                catch (Exception e) {
                    B2Client.this.log(e);
                }
            }
        });
        return true;
    }

    @Override
    public String getUploadedByMetadata(String path) {
        String user_name = "";
        try {
            Properties p = this.stat(path);
            if (p != null) {
                JSONObject postData = new JSONObject();
                postData.put("fileId", p.getProperty("fileId"));
                if (p.containsKey("uploaded_by") && !p.getProperty("uploaded_by").equals("")) {
                    return p.getProperty("uploaded_by", "");
                }
                URLConnection urlc = URLConnection.openConnection(new VRL(String.valueOf(this.api_url) + "/b2api/v2/b2_get_file_info"), this.config);
                urlc.setRequestMethod("POST");
                urlc.setDoInput(true);
                urlc.setDoOutput(true);
                urlc.setUseCaches(false);
                urlc.setRequestProperty("Authorization", this.token);
                urlc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                OutputStream out = urlc.getOutputStream();
                out.write(postData.toString().getBytes("UTF8"));
                out.close();
                int code = urlc.getResponseCode();
                String result = URLConnection.consumeResponse(urlc.getInputStream());
                if (code < 200 || code > 299) {
                    this.log(String.valueOf(urlc.getResponseCode()) + result + "\r\n");
                    urlc.disconnect();
                }
                urlc.disconnect();
                JSONObject jo = (JSONObject)JSONValue.parse(result);
                JSONObject file_info = (JSONObject)jo.get("fileInfo");
                if (file_info.containsKey("uploaded_by")) {
                    user_name = (String)file_info.get("uploaded_by");
                }
            }
        }
        catch (Exception e) {
            this.log(e);
        }
        return user_name;
    }

    @Override
    public void set_MD5_and_upload_id(String path) throws Exception {
        if (this.config.getProperty("uploaded_by", "").equals("") || this.config.getProperty("uploaded_md5", "").equals("")) {
            return;
        }
        this.update_file_info(path, 0L);
    }
}

