/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.GenericClient;
import com.crushftp.client.S3Client;
import com.crushftp.client.URLConnection;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class GStorageClient
extends GenericClient {
    String bearer = "";
    String bucketName = "";
    SimpleDateFormat sdf_rfc1123_2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
    boolean is_jwt = false;
    String[] gstorage_fields = new String[]{"username", "password", "token_start", "gstorage_with_s3_api"};
    private S3Client s3c = null;

    public GStorageClient(String url, String header, Vector log) {
        super(header, log);
        this.fields = Arrays.copyOf(this.gstorage_fields, this.gstorage_fields.length + S3Client.s3_fields.length);
        System.arraycopy(S3Client.s3_fields, 0, this.fields, this.gstorage_fields.length, S3Client.s3_fields.length);
        if (!url.endsWith("/")) {
            url = String.valueOf(url) + "/";
        }
        this.url = url;
    }

    @Override
    public String login2(String username, String password, String clientid) throws Exception {
        if (this.config.getProperty("gstorage_with_s3_api", "false").equals("true")) {
            String s3_url = "s3" + this.url.substring(8);
            this.s3c = new S3Client(s3_url, this.logHeader, this.logQueue);
            this.config.put("s3_sha256", "true");
            this.s3c.setConfigObj(this.config);
            return this.s3c.login2(username, password, clientid);
        }
        password = VRL.vrlDecode(password);
        this.config.put("username", username);
        this.config.put("password", password);
        Properties p = null;
        if (username.equals("google_jwt") || username.startsWith("google_jwt")) {
            this.is_jwt = true;
            p = Common.oauth_renew_tokens(password, "~google_jwt~https://www.googleapis.com/auth/devstorage.full_control", "", "https://oauth2.googleapis.com/token");
        } else {
            p = Common.oauth_renew_tokens(password, username.split("~")[0], username.split("~")[1], "https://oauth2.googleapis.com/token");
        }
        if (p.containsKey("access_token")) {
            this.bearer = p.getProperty("access_token");
        } else if (p.containsKey("refresh_token")) {
            this.bearer = p.getProperty("refresh_token");
        }
        String path0 = new VRL(this.url).getPath();
        try {
            this.bucketName = path0.substring(1, path0.indexOf("/", 1));
        }
        catch (Exception e) {
            this.log(e);
            throw new Exception("Error : Wrong bucket!");
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://storage.googleapis.com/storage/v1/b/" + this.bucketName), this.config);
        urlc.setDoOutput(true);
        urlc.setRequestMethod("GET");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        String result = Common.consumeResponse(urlc.getInputStream());
        int code = urlc.getResponseCode();
        if (code < 200 || code > 299) {
            this.log(result);
            throw new IOException(result);
        }
        this.config.put("logged_out", "false");
        return "Success";
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        if (this.s3c != null) {
            this.s3c.list(path, list, 1000);
            int x = 0;
            while (x < list.size()) {
                Properties p = (Properties)list.get(x);
                String url = p.getProperty("url");
                url = "gstorage" + url.substring(2);
                p.put("url", url);
                ++x;
            }
            return list;
        }
        return this.list(path, list, "%2F", true);
    }

    public Vector list(String path, Vector list, String delimiter, boolean includeDelimiter) throws Exception {
        String path0 = this.getBucketRelativePath(path);
        if (!path0.equals("") && !path0.endsWith("/")) {
            path0 = String.valueOf(path0) + "/";
        }
        String prefix = Common.url_encode(path0);
        String next = "";
        String pageToken = "";
        int count = 0;
        do {
            String result;
            if (this.config.getProperty("logged_out", "false").equals("true")) {
                throw new Exception("Error: Cancel dir listing. The client is already closed.");
            }
            ++count;
            if (!next.equals("")) {
                pageToken = "&pageToken=" + next;
            }
            next = "";
            URLConnection urlc = URLConnection.openConnection(new VRL("https://storage.googleapis.com/storage/v1/b/" + this.bucketName + "/o" + "?delimiter=" + delimiter + "&maxResults=1000&includeTrailingDelimiter=" + includeDelimiter + "&versions=false&prefix=" + prefix + pageToken), this.config);
            urlc.setDoOutput(false);
            urlc.setRequestMethod("GET");
            urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
            int code = urlc.getResponseCode();
            if (code < 200 || code > 299) {
                result = URLConnection.consumeResponse(urlc.getInputStream());
                urlc.disconnect();
                this.log(String.valueOf(result) + "\r\n");
                throw new Exception(result);
            }
            result = Common.consumeResponse(urlc.getInputStream());
            JSONObject obj = (JSONObject)JSONValue.parse(result);
            next = obj.containsKey("nextPageToken") ? obj.get("nextPageToken").toString() : "";
            Object obj2 = obj.get("items");
            if (!(obj2 instanceof JSONArray)) continue;
            JSONArray ja = (JSONArray)obj2;
            int xxx = 0;
            while (xxx < ja.size()) {
                Object team = ja.get(xxx);
                if (team instanceof JSONObject) {
                    Properties item = new Properties();
                    JSONObject jo = (JSONObject)team;
                    Object[] a = jo.entrySet().toArray();
                    int i = 0;
                    while (i < a.length) {
                        String key2 = a[i].toString().split("=")[0];
                        item.put(key2.trim(), ("" + jo.get(key2)).trim());
                        ++i;
                    }
                    if (!item.getProperty("name").equals(path0)) {
                        String objectName = item.getProperty("name");
                        boolean folder = item.getProperty("name").endsWith("/");
                        if (folder) {
                            item.put("name", item.getProperty("name").substring(0, item.getProperty("name").length() - 1));
                        }
                        item.put("name", Common.last(item.getProperty("name")));
                        Date d = new Date();
                        try {
                            d = this.sdf_rfc1123_2.parse(item.getProperty("updated"));
                        }
                        catch (Exception e) {
                            this.log(e);
                        }
                        String line = String.valueOf(folder ? "d" : "-") + "rwxrwxrwx   1    owner   group   " + item.getProperty("size") + "   " + this.yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + item.getProperty("name");
                        Properties stat = GStorageClient.parseStat(line);
                        stat.put("obejct_name", objectName);
                        stat.put("url", "gstorage://" + (String)this.getConfig("username") + ":" + VRL.vrlEncode((String)this.getConfig("password")) + "@storage.googleapis.com" + path + stat.getProperty("name"));
                        list.addElement(stat);
                    }
                }
                ++xxx;
            }
        } while (count != 1000 && !next.equals(""));
        return list;
    }

    @Override
    public Properties stat(String path) throws Exception {
        if (this.s3c != null) {
            Properties p = this.s3c.stat(path);
            if (p != null) {
                String url = p.getProperty("url");
                url = "gstorage" + url.substring(2);
                p.put("url", url);
            }
            return p;
        }
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
        int code;
        if (this.s3c != null) {
            return this.s3c.download3(path, startPos, endPos, binary);
        }
        String path0 = this.getBucketRelativePath(path);
        URLConnection urlc = URLConnection.openConnection(new VRL("https://storage.googleapis.com/storage/v1/b/" + this.bucketName + "/o/" + this.double_encode(path0) + "?alt=media"), this.config);
        urlc.setDoOutput(false);
        urlc.setRequestMethod("GET");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRemoveDoubleEncoding(true);
        if (startPos > 0L || endPos >= 0L) {
            urlc.setRequestProperty("Range", "bytes=" + startPos + "-" + (endPos >= 0L ? String.valueOf(endPos) : ""));
        }
        if ((code = urlc.getResponseCode()) < 200 || code > 299) {
            String result = URLConnection.consumeResponse(urlc.getInputStream());
            urlc.disconnect();
            this.log(String.valueOf(result) + "\r\n");
            throw new Exception(result);
        }
        this.in = urlc.getInputStream();
        return this.in;
    }

    @Override
    protected OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        if (this.s3c != null) {
            return this.s3c.upload3(path, startPos, truncate, binary);
        }
        String path0 = this.getBucketRelativePath(path);
        URLConnection urlc = URLConnection.openConnection(new VRL("https://storage.googleapis.com/upload/storage/v1/b/" + this.bucketName + "/o?uploadType=resumable"), this.config);
        urlc.setDoOutput(true);
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("X-Upload-Content-Type", "application/octet-stream");
        urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        JSONObject fileMetaInfo = new JSONObject();
        fileMetaInfo.put("name", Common.url_decode(path0));
        OutputStream out = urlc.getOutputStream();
        out.write(fileMetaInfo.toString().getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        if (code < 200 || code > 299) {
            String result = Common.consumeResponse(urlc.getInputStream());
            throw new IOException(result);
        }
        String uploadLocation = urlc.getHeaderField("Location");
        class OutputWrapper
        extends OutputStream {
            boolean closed = false;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(0x400000);
            long pos = 0L;
            long pos2 = 0L;
            String final_size = "*";
            final Properties status = new Properties();
            private final /* synthetic */ String val$uploadLocation;

            OutputWrapper(String string) {
                this.val$uploadLocation = string;
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
                if (this.baos.size() + len > 0x400000) {
                    int chunks = (this.baos.size() + len) / 0x400000;
                    int diff = this.baos.size() + len - chunks * 0x400000;
                    int offset_len = len - diff;
                    this.baos.write(b, off, offset_len);
                    this.pos2 += (long)this.baos.size();
                    long tmp_pos = this.baos.size();
                    this.flushNow();
                    this.pos += tmp_pos;
                    this.baos.write(b, offset_len, len - offset_len);
                } else {
                    this.baos.write(b, off, len);
                }
            }

            public void flushNow() throws IOException {
                if (this.baos.size() > 0) {
                    if (this.status.containsKey("status")) {
                        int loops = 0;
                        while (this.status.getProperty("status", "").equals("") && loops++ < 2000) {
                            try {
                                Thread.sleep(100L);
                            }
                            catch (InterruptedException e) {
                                GStorageClient.this.log(e);
                            }
                        }
                        if (loops >= 1998) {
                            throw new IOException("100 second timeout while waiting for prior gstorage chunk to complete..." + loops + ":" + this.pos + ":" + this.pos);
                        }
                        if (this.status.getProperty("status", "").startsWith("Error:")) {
                            throw new IOException(this.status.getProperty("status", ""));
                        }
                    }
                    final long chunk_pos = this.pos;
                    final long chunk_pos2 = this.pos2;
                    final String chunk_final_size = this.final_size;
                    final byte[] b_flush = this.baos.toByteArray();
                    this.status.put("status", "");
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            try {
                                URLConnection urlc2 = URLConnection.openConnection(new VRL(val$uploadLocation), ((OutputWrapper)this).GStorageClient.this.config);
                                urlc2.setRequestMethod("PUT");
                                urlc2.setDoOutput(true);
                                urlc2.setRequestProperty("Content-Range", "bytes " + chunk_pos + "-" + (chunk_pos2 - 1L) + "/" + chunk_final_size);
                                GStorageClient.this.doStandardDocsAlterations(urlc2, "application/octet-stream");
                                urlc2.getOutputStream().write(b_flush);
                                urlc2.getOutputStream().close();
                                String result = Common.consumeResponse(urlc2.getInputStream());
                                int code = urlc2.getResponseCode();
                                urlc2.disconnect();
                                if (code < 200 || code > 308) {
                                    throw new IOException(result);
                                }
                                status.put("status", "Success!");
                            }
                            catch (Exception e) {
                                GStorageClient.this.log(e);
                                status.put("status", "Error: " + e.getMessage());
                            }
                        }
                    });
                }
                this.baos.reset();
            }

            @Override
            public void close() throws IOException {
                if (this.closed) {
                    return;
                }
                if (this.status.containsKey("status")) {
                    try {
                        int loops = 0;
                        while (this.status.getProperty("status", "").equals("") && loops++ < 1000) {
                            Thread.sleep(100L);
                        }
                        if (loops >= 998) {
                            throw new IOException("100 second timeout while waiting for prior gstorage chunk to complete..." + loops + ":" + this.pos + ":" + this.pos);
                        }
                        if (this.status.getProperty("status", "").startsWith("Error:")) {
                            throw new IOException(this.status.getProperty("status", ""));
                        }
                    }
                    catch (InterruptedException e) {
                        GStorageClient.this.log(e);
                    }
                }
                this.pos2 += (long)this.baos.size();
                this.final_size = String.valueOf(this.pos2);
                if (this.baos.size() > 0) {
                    try {
                        URLConnection urlc2 = URLConnection.openConnection(new VRL(this.val$uploadLocation), GStorageClient.this.config);
                        urlc2.setRequestMethod("PUT");
                        urlc2.setDoOutput(true);
                        urlc2.setRequestProperty("Content-Range", "bytes " + this.pos + "-" + (this.pos2 - 1L) + "/" + this.final_size);
                        GStorageClient.this.doStandardDocsAlterations(urlc2, "application/octet-stream");
                        urlc2.getOutputStream().write(this.baos.toByteArray());
                        urlc2.getOutputStream().close();
                        int code = urlc2.getResponseCode();
                        String result = Common.consumeResponse(urlc2.getInputStream());
                        urlc2.disconnect();
                        if (code < 200 || code > 308) {
                            throw new IOException(result);
                        }
                        result = result.equals("") ? "Uploaded bytes: " + this.pos + "-" + (this.pos2 - 1L) + "/" + this.final_size : "Byte range : " + this.pos + "-" + (this.pos2 - 1L) + "/" + this.final_size + "Result : " + result;
                        urlc2.disconnect();
                    }
                    catch (Exception e) {
                        GStorageClient.this.log(e);
                    }
                } else {
                    try {
                        URLConnection urlc2 = URLConnection.openConnection(new VRL(this.val$uploadLocation), GStorageClient.this.config);
                        urlc2.setRequestMethod("PUT");
                        urlc2.setDoOutput(true);
                        urlc2.setRequestProperty("Content-Range", "bytes */0");
                        urlc2.setRequestProperty("Content-Length", "0");
                        GStorageClient.this.doStandardDocsAlterations(urlc2, "application/octet-stream");
                        urlc2.getOutputStream().close();
                        String result = Common.consumeResponse(urlc2.getInputStream());
                        int code = urlc2.getResponseCode();
                        urlc2.disconnect();
                        if (code < 200 || code > 308) {
                            throw new IOException(result);
                        }
                    }
                    catch (Exception e) {
                        GStorageClient.this.log(e);
                    }
                }
                this.closed = true;
            }
        }
        out = new OutputWrapper(uploadLocation);
        return out;
    }

    @Override
    public boolean mdtm(String path, long modified) throws Exception {
        if (this.s3c != null) {
            return this.s3c.mdtm(path, modified);
        }
        this.log("Google stortage does not support mdtm modifications on objects!");
        return true;
    }

    @Override
    public boolean makedir(String path) throws Exception {
        if (this.s3c != null) {
            return this.s3c.makedir(path);
        }
        String path0 = this.getBucketRelativePath(path);
        if (!path0.endsWith("/")) {
            path0 = String.valueOf(path0) + "/";
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://storage.googleapis.com/upload/storage/v1/b/" + this.bucketName + "/o?uploadType=media&name=" + this.double_encode(path0)), this.config);
        urlc.setDoOutput(true);
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("Content-Type", "Folder");
        urlc.setRequestProperty("Content-Length", "0");
        urlc.setRemoveDoubleEncoding(true);
        urlc.getOutputStream().close();
        int code = urlc.getResponseCode();
        if (code < 200 || code > 299) {
            String result = Common.consumeResponse(urlc.getInputStream());
            this.log("Delete Error :" + result);
            return false;
        }
        return true;
    }

    @Override
    public boolean makedirs(String path) throws Exception {
        if (this.s3c != null) {
            return this.s3c.makedirs(path);
        }
        boolean ok = true;
        String[] parts = path.split("/");
        String path2 = "";
        int x = 0;
        while (x < parts.length && ok) {
            path2 = String.valueOf(path2) + parts[x] + "/";
            if (x >= 2 && this.stat(path2) == null) {
                ok = this.makedir(path2);
            }
            ++x;
        }
        return ok;
    }

    @Override
    public boolean delete(String path) throws Exception {
        if (this.s3c != null) {
            return this.s3c.delete(path);
        }
        String path0 = this.getBucketRelativePath(path);
        Properties p = this.stat(path);
        Vector list = new Vector();
        if (p.getProperty("type", "FILE").equalsIgnoreCase("DIR") && !path0.endsWith("/")) {
            if (!path0.endsWith("/")) {
                path0 = String.valueOf(path0) + "/";
            }
            this.list(path, list, "", false);
            int x = list.size() - 1;
            while (x >= 0) {
                Properties item = (Properties)list.get(x);
                if (!this.deleteObject(item.getProperty("obejct_name"))) {
                    return false;
                }
                --x;
            }
            this.deleteObject(path0);
            return true;
        }
        return this.deleteObject(path0);
    }

    private boolean deleteObject(String path) throws Exception {
        URLConnection urlc = URLConnection.openConnection(new VRL("https://storage.googleapis.com/storage/v1/b/" + this.bucketName + "/o/" + this.double_encode(path)), this.config);
        urlc.setDoOutput(false);
        urlc.setDoInput(true);
        urlc.setDoOutput(false);
        urlc.setUseCaches(false);
        urlc.setRequestMethod("DELETE");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRemoveDoubleEncoding(true);
        int code = urlc.getResponseCode();
        if (code < 200 || code > 299) {
            String result = Common.consumeResponse(urlc.getInputStream());
            this.log("Delete Error :" + result);
            return false;
        }
        return true;
    }

    @Override
    public boolean rename(String rnfr, String rnto, boolean overwrite) throws Exception {
        if (this.s3c != null) {
            return this.s3c.rename(rnfr, rnto);
        }
        boolean result = true;
        String path_rnfr = this.getBucketRelativePath(rnfr);
        String path_rnto = this.getBucketRelativePath(rnto);
        Properties p = this.stat(rnfr);
        if (p.getProperty("type", "FILE").equalsIgnoreCase("DIR")) {
            Vector list = new Vector();
            if (!path_rnfr.endsWith("/")) {
                path_rnfr = String.valueOf(path_rnfr) + "/";
            }
            if (!path_rnto.endsWith("/")) {
                path_rnto = String.valueOf(path_rnto) + "/";
            }
            this.list(rnfr, list, "", false);
            int x = list.size() - 1;
            while (x >= 0) {
                String subrnto;
                Properties item = (Properties)list.get(x);
                String subrnfr = item.getProperty("obejct_name");
                if (!this.renameObject(subrnfr, subrnto = subrnfr.replaceAll(path_rnfr, path_rnto))) {
                    return false;
                }
                --x;
            }
            result = this.renameObject(path_rnfr, path_rnto);
            if (!result) {
                return false;
            }
        } else {
            result = this.renameObject(path_rnfr, path_rnto);
            if (!result) {
                return false;
            }
        }
        this.delete(rnfr);
        return result;
    }

    private boolean renameObject(String path_rnfr, String path_rnto) throws Exception {
        URLConnection urlc = URLConnection.openConnection(new VRL("https://storage.googleapis.com/storage/v1/b/" + this.bucketName + "/o/" + this.double_encode(path_rnfr) + "/rewriteTo/b/" + this.bucketName + "/o/" + this.double_encode(path_rnto)), this.config);
        urlc.setDoOutput(false);
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("Content-Length", "0");
        urlc.setRemoveDoubleEncoding(true);
        int code = urlc.getResponseCode();
        if (code < 200 || code > 299) {
            String result = Common.consumeResponse(urlc.getInputStream());
            this.log(result);
            return false;
        }
        return true;
    }

    public void doStandardDocsAlterations(URLConnection urlc, String contentType) throws Exception {
        urlc.setRequestProperty("Content-Type", contentType);
        urlc.setRequestProperty("Accept", null);
        urlc.setRequestProperty("Pragma", null);
        urlc.setRequestProperty("Cache", null);
        urlc.setRequestProperty("Cache-Control", null);
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setUseCaches(false);
    }

    private String getBearer() throws Exception {
        Properties p = null;
        p = this.is_jwt ? Common.oauth_renew_tokens(this.config.getProperty("password"), "~google_jwt~https://www.googleapis.com/auth/devstorage.full_control", "", "https://oauth2.googleapis.com/token") : Common.oauth_renew_tokens(this.config.getProperty("password"), this.config.getProperty("username").split("~")[0], this.config.getProperty("username").split("~")[1], "https://oauth2.googleapis.com/token");
        if (p.containsKey("access_token")) {
            this.bearer = p.getProperty("access_token");
            this.config.put("token_start", String.valueOf(System.currentTimeMillis()));
        } else if (p.containsKey("refresh_token")) {
            this.bearer = p.getProperty("refresh_token");
            this.config.put("token_start", String.valueOf(System.currentTimeMillis()));
        }
        return this.bearer;
    }

    private String double_encode(String text) {
        if (text.contains("/")) {
            text = text.replace("/", "%252F");
        }
        if (text.contains("+")) {
            text = text.replace("+", "%252B");
        }
        if (text.contains("=")) {
            text = text.replace("=", "%253D");
        }
        if (text.contains("#")) {
            text = text.replace("#", "%2523");
        }
        if (text.contains("~")) {
            text = text.replace("~", "%257E");
        }
        if (text.contains("!")) {
            text = text.replace("!", "%2521");
        }
        if (text.contains("\\")) {
            text = text.replace("\\", "%255C");
        }
        if (text.contains("%2F")) {
            text = text.replace("%2F", "%252F");
        }
        if (text.contains("%2B")) {
            text = text.replace("%2B", "%252B");
        }
        if (text.contains("%3D")) {
            text = text.replace("%3D", "%253D");
        }
        if (text.contains("%23")) {
            text = text.replace("%23", "%2523");
        }
        if (text.contains("%7E")) {
            text = text.replace("%7E", "%257E");
        }
        if (text.contains("%21")) {
            text = text.replace("%21", "%2521");
        }
        if (text.contains("%5C")) {
            text = text.replace("%5C", "%255C");
        }
        return text;
    }

    private String getBucketRelativePath(String path) {
        if (path.equals("/")) {
            return path;
        }
        String path0 = path.substring(path.indexOf(String.valueOf(this.bucketName) + "/") + (String.valueOf(this.bucketName) + "/").length());
        if (path0.startsWith("/")) {
            path0 = path0.substring(1);
        }
        return path0;
    }

    @Override
    public String getUploadedByMetadata(String path) {
        if (this.s3c != null) {
            return this.s3c.getUploadedByMetadata(path);
        }
        return super.getUploadedByMetadata(path);
    }

    @Override
    public void set_MD5_and_upload_id(String path) throws Exception {
        if (this.s3c != null) {
            this.s3c.set_MD5_and_upload_id(path);
        }
    }

    @Override
    public void logout() throws Exception {
        if (this.s3c != null) {
            this.s3c.logout();
        }
        super.logout();
    }
}

