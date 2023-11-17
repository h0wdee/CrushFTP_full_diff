/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.json.simple.JSONArray
 *  org.json.simple.JSONObject
 *  org.json.simple.JSONValue
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.File_U;
import com.crushftp.client.GenericClient;
import com.crushftp.client.URLConnection;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class DropBoxClient
extends GenericClient {
    Properties config = new Properties();
    Properties resourceIdCache = new Properties();
    SimpleDateFormat yyyyMMddtHHmmssSSSZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    SimpleDateFormat yyyyMMddtHHmmssZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
    SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
    String bearer = "";

    public DropBoxClient(String url, String header, Vector log) {
        super(header, log);
        this.fields = new String[]{"username", "password", "token_start", "token_expire"};
        if (!url.endsWith("/")) {
            url = String.valueOf(url) + "/";
        }
        this.url = url;
    }

    public static Properties setup_bearer(String oauth_access_code, String server_url, String client_id, String client_secret) throws Exception {
        String full_form = "code=" + URLEncoder.encode(oauth_access_code, "UTF-8");
        full_form = String.valueOf(full_form) + "&client_id=" + client_id;
        full_form = String.valueOf(full_form) + "&client_secret=" + client_secret;
        full_form = String.valueOf(full_form) + "&redirect_uri=" + server_url;
        full_form = String.valueOf(full_form) + "&grant_type=authorization_code";
        byte[] b = full_form.getBytes("UTF8");
        URLConnection urlc = URLConnection.openConnection(new VRL("https://api.dropboxapi.com/oauth2/token"), new Properties());
        urlc.setDoOutput(true);
        urlc.setRequestMethod("POST");
        OutputStream out = urlc.getOutputStream();
        out.write(full_form.getBytes("UTF8"));
        out.close();
        urlc.getResponseCode();
        String response = Common.consumeResponse(urlc.getInputStream());
        String refresh_token = ((JSONObject)JSONValue.parse((String)response)).get((Object)"refresh_token").toString();
        Properties p = new Properties();
        p.put("refresh_token", refresh_token);
        return p;
    }

    public Properties setup_bearer_refresh(String refresh_token, String client_id, String client_secret) throws Exception {
        String full_form = "";
        full_form = String.valueOf(full_form) + "&client_id=" + client_id;
        full_form = String.valueOf(full_form) + "&client_secret=" + client_secret;
        full_form = String.valueOf(full_form) + "&refresh_token=" + refresh_token;
        full_form = String.valueOf(full_form) + "&grant_type=refresh_token";
        byte[] b = full_form.getBytes("UTF8");
        URLConnection urlc = URLConnection.openConnection(new VRL("https://api.dropboxapi.com/oauth2/token"), this.config);
        urlc.setDoOutput(true);
        urlc.setRequestMethod("POST");
        OutputStream out = urlc.getOutputStream();
        out.write(full_form.getBytes("UTF8"));
        out.close();
        urlc.getResponseCode();
        String response = Common.consumeResponse(urlc.getInputStream());
        String access_token = ((JSONObject)JSONValue.parse((String)response)).get((Object)"access_token").toString();
        Properties p = new Properties();
        p.put("access_token", access_token);
        String expire_in = ((JSONObject)JSONValue.parse((String)response)).get((Object)"expires_in").toString();
        if (expire_in.endsWith(",")) {
            expire_in = expire_in.substring(0, expire_in.length() - 1);
        }
        p.put("expires_in", expire_in);
        p.put("time", String.valueOf(System.currentTimeMillis()));
        return p;
    }

    private String getBearer() throws Exception {
        if (this.config.containsKey("token_start") && this.config.containsKey("token_expire") && System.currentTimeMillis() - Long.parseLong(this.config.getProperty("token_start")) > (Long.parseLong(this.config.getProperty("token_expire")) - 600L) * 1000L) {
            Properties p = this.setup_bearer_refresh(this.config.getProperty("password"), this.config.getProperty("username").split("~")[0], this.config.getProperty("username").split("~")[1]);
            if (p.containsKey("access_token")) {
                this.bearer = p.getProperty("access_token");
                this.config.put("token_start", String.valueOf(System.currentTimeMillis()));
            } else if (p.containsKey("refresh_token")) {
                this.bearer = p.getProperty("refresh_token");
                this.config.put("token_start", String.valueOf(System.currentTimeMillis()));
            }
        }
        return this.bearer;
    }

    @Override
    public String login2(String username, String password, String clientid) throws Exception {
        try {
            String path = new VRL(this.url).getPath();
            if (path.indexOf("//") > 0) {
                throw new Exception("Failure! Invalid path : double slash !");
            }
            password = VRL.vrlDecode(password);
            this.config.put("username", username);
            this.config.put("password", password);
            Properties p = this.setup_bearer_refresh(password, username.split("~")[0], username.split("~")[1]);
            if (p.containsKey("expires_in")) {
                this.config.put("token_start", String.valueOf(System.currentTimeMillis()));
                String expire_in = p.getProperty("expires_in");
                if (expire_in.endsWith(",")) {
                    expire_in = expire_in.substring(0, expire_in.length() - 1);
                }
                this.config.put("token_expire", expire_in);
            }
            if (p.containsKey("access_token")) {
                this.bearer = p.getProperty("access_token");
            } else if (p.containsKey("refresh_token")) {
                this.bearer = p.getProperty("refresh_token");
            }
            URLConnection urlc = URLConnection.openConnection(new VRL("https://api.dropboxapi.com/2/check/user"), this.config);
            urlc.setDoOutput(true);
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
            urlc.setRequestProperty("Content-Type", "application/json");
            String query = "{\"query\": \"foo\"}";
            OutputStream out = urlc.getOutputStream();
            out.write(query.getBytes("UTF8"));
            out.close();
            String response = Common.consumeResponse(urlc.getInputStream());
            int code = urlc.getResponseCode();
            if (code < 200 || code > 299) {
                this.log(String.valueOf(response) + "\r\n");
                throw new Exception("Failure! Missing Access token!");
            }
            if (!((JSONObject)JSONValue.parse((String)response)).get((Object)"result").toString().equals("foo")) {
                throw new Exception("Failure! Missing Access token!");
            }
        }
        catch (Exception e) {
            throw new Exception("ERROR : Bad credentials : " + e);
        }
        return "Success";
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        if (path.indexOf("//") > 0) {
            throw new Exception("Failure! Invalid path : double slash !");
        }
        String temp_path = path;
        if (path.equals("/")) {
            temp_path = "";
        }
        String cursor = "";
        int x = 0;
        while (x <= 10) {
            if ((cursor = this.list2(temp_path, cursor, list)).equals("")) break;
            ++x;
        }
        return list;
    }

    private String list2(String path, String cursor, Vector list) throws Exception {
        String command_continue = "";
        if (!cursor.equals("")) {
            command_continue = "/continue";
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://api.dropboxapi.com/2/files/list_folder" + command_continue), this.config);
        urlc.setDoOutput(true);
        urlc.setDoInput(true);
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("Content-Type", "application/json");
        JSONObject postData = new JSONObject();
        if (!cursor.equals("")) {
            postData.put((Object)"cursor", (Object)cursor);
        } else {
            postData.put((Object)"path", (Object)path);
            postData.put((Object)"recursive", (Object)new Boolean(false));
            postData.put((Object)"include_deleted", (Object)new Boolean(false));
            postData.put((Object)"include_has_explicit_shared_members", (Object)new Boolean(false));
            postData.put((Object)"include_mounted_folders", (Object)new Boolean(false));
            postData.put((Object)"limit", (Object)new Integer(1000));
            postData.put((Object)"include_non_downloadable_files", (Object)new Boolean(false));
        }
        OutputStream out = urlc.getOutputStream();
        out.write(postData.toJSONString().getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        if (code < 200 || code > 299) {
            String result = URLConnection.consumeResponse(urlc.getInputStream());
            urlc.disconnect();
            this.log(String.valueOf(result) + "\r\n");
            throw new Exception(result);
        }
        String response = Common.consumeResponse(urlc.getInputStream());
        this.parseListResponse(path, response, list);
        try {
            boolean has_more = (Boolean)((JSONObject)JSONValue.parse((String)response)).get((Object)"has_more");
            cursor = has_more ? (String)((JSONObject)JSONValue.parse((String)response)).get((Object)"cursor") : "";
        }
        catch (Exception e) {
            cursor = "";
        }
        return cursor;
    }

    private Vector parseListResponse(String path, String response, Vector list) throws Exception {
        Object obj = ((JSONObject)JSONValue.parse((String)response)).get((Object)"entries");
        if (obj instanceof JSONArray) {
            JSONArray ja = (JSONArray)obj;
            int xxx = 0;
            while (xxx < ja.size()) {
                Object json_item = ja.get(xxx);
                if (json_item instanceof JSONObject) {
                    try {
                        Properties stat = this.parseItem(path, json_item);
                        list.addElement(stat);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                ++xxx;
            }
        }
        return null;
    }

    private Properties parseItem(String path, Object json_item) throws Exception {
        JSONObject jo = (JSONObject)json_item;
        if (!path.endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        Object[] a = jo.entrySet().toArray();
        Properties item = new Properties();
        int i = 0;
        while (i < a.length) {
            String key2 = a[i].toString().split("=")[0];
            item.put(key2.trim(), ("" + jo.get((Object)key2)).trim());
            ++i;
        }
        Date d = new Date();
        if (!item.getProperty("client_modified", "").equals("")) {
            String s = item.getProperty("client_modified", this.yyyyMMddtHHmmssSSSZ.format(d).toString());
            SimpleDateFormat format = null;
            try {
                format = s.length() == 20 ? this.yyyyMMddtHHmmssZ : this.yyyyMMddtHHmmssSSSZ;
                d = format.parse(s);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        boolean folder = true;
        if (item.getProperty(".tag", "folder").toLowerCase().equals("file")) {
            folder = false;
        }
        String line = String.valueOf(folder ? "d" : "-") + "rwxrwxrwx   1    owner   group   " + (folder ? "0" : item.getProperty("size", "0")) + "   " + this.yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + item.getProperty("name");
        Properties stat = null;
        stat = DropBoxClient.parseStat(line);
        stat.put("resource_id", item.getProperty("id", ""));
        if (!item.getProperty("content_hash", "").equals("")) {
            stat.put("dropbox_content_hash", item.getProperty("content_hash", ""));
        }
        if (!item.getProperty("has_explicit_shared_members", "").equals("")) {
            stat.put("dropbox_has_explicit_shared_members", item.getProperty("has_explicit_shared_members", "false"));
        }
        if (!item.getProperty("is_downloadable", "").equals("")) {
            stat.put("dropbox_is_downloadable", item.getProperty("is_downloadable", "false"));
        }
        if (!item.getProperty("server_modified", "").equals("")) {
            stat.put("dropbox_server_modified", item.getProperty("server_modified", ""));
        }
        stat.put("url", "dropbox://" + this.config.getProperty("username", "") + ":" + VRL.vrlEncode(this.config.getProperty("password", "")) + "@api.dropboxapi.com" + (path.equals("") ? "/" : path) + stat.getProperty("name"));
        this.resourceIdCache.put(String.valueOf(this.config.getProperty("password")) + path + stat.getProperty("name"), item.getProperty("id"));
        this.resourceIdCache.put(String.valueOf(this.config.getProperty("password")) + path + stat.getProperty("name") + "/", item.getProperty("id"));
        return stat;
    }

    @Override
    public Properties stat(String path) throws Exception {
        String temp_path = path;
        if (path.endsWith(":filetree")) {
            temp_path = path.substring(0, path.indexOf(":filetree") - 1);
        }
        if (path.endsWith("/")) {
            temp_path = path.substring(0, path.length() - 1);
        }
        if (path.equals("/")) {
            temp_path = "";
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://api.dropboxapi.com/2/files/get_metadata"), this.config);
        urlc.setDoOutput(true);
        urlc.setDoInput(true);
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("Content-Type", "application/json");
        JSONObject postData = new JSONObject();
        postData.put((Object)"path", (Object)temp_path);
        postData.put((Object)"include_media_info", (Object)new Boolean(false));
        postData.put((Object)"include_deleted", (Object)new Boolean(false));
        postData.put((Object)"include_has_explicit_shared_members", (Object)new Boolean(false));
        OutputStream out = urlc.getOutputStream();
        out.write(postData.toJSONString().getBytes("UTF8"));
        out.close();
        String response = Common.consumeResponse(urlc.getInputStream());
        int code = urlc.getResponseCode();
        if (code < 200 || code > 299) {
            this.log(String.valueOf(response) + "\r\n");
            return null;
        }
        Properties p = this.parseItem(Common.all_but_last(path), (JSONObject)JSONValue.parse((String)response));
        return p;
    }

    @Override
    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        String params = "";
        URLConnection urlc = URLConnection.openConnection(new VRL("https://content.dropboxapi.com/2/files/download"), this.config);
        urlc.setDoOutput(true);
        urlc.setDoInput(true);
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("Content-Type", "application/octet-stream");
        JSONObject postData = new JSONObject();
        postData.put((Object)"path", (Object)path.toLowerCase());
        urlc.setRequestProperty("Dropbox-API-Arg", postData.toJSONString());
        this.in = urlc.getInputStream();
        return this.in;
    }

    @Override
    protected OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        URLConnection urlc = URLConnection.openConnection(new VRL("https://content.dropboxapi.com/2/files/upload_session/start"), this.config);
        urlc.setDoOutput(true);
        urlc.setDoInput(true);
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("Content-Type", "application/octet-stream");
        JSONObject postData = new JSONObject();
        postData.put((Object)"close", (Object)new Boolean(false));
        urlc.setRequestProperty("Dropbox-API-Arg", postData.toJSONString());
        int code = urlc.getResponseCode();
        String response = Common.consumeResponse(urlc.getInputStream());
        if (code > 0 && (code < 200 || code > 299)) {
            this.log(String.valueOf(response) + "\r\n");
            throw new IOException("Path :" + path + " Error : " + response);
        }
        String upload_session_id = (String)((JSONObject)JSONValue.parse((String)response)).get((Object)"session_id");
        class OutputWrapper
        extends OutputStream {
            boolean closed = false;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(0xA00000);
            long pos = 0L;
            long final_size = 0L;
            boolean first_flush = true;
            final Properties status = new Properties();
            private final /* synthetic */ String val$path;
            private final /* synthetic */ String val$upload_session_id;

            OutputWrapper(String string, String string2) {
                this.val$path = string;
                this.val$upload_session_id = string2;
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
                if (this.baos.size() + len > 0xA00000) {
                    int chunks = (this.baos.size() + len) / 0xA00000;
                    int diff = this.baos.size() + len - chunks * 0xA00000;
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
                    }
                }
                catch (InterruptedException e) {
                    DropBoxClient.this.log(e);
                }
                try {
                    DropBoxClient.this.log("Dropbox : path : " + this.val$path + " final pos : " + this.final_size);
                    URLConnection urlc = URLConnection.openConnection(new VRL("https://content.dropboxapi.com/2/files/upload_session/finish"), DropBoxClient.this.config);
                    urlc.setDoOutput(true);
                    urlc.setDoInput(true);
                    urlc.setRequestMethod("POST");
                    urlc.setRequestProperty("Authorization", "Bearer " + DropBoxClient.this.getBearer());
                    urlc.setRequestProperty("Content-Type", "application/octet-stream");
                    JSONObject postData = new JSONObject();
                    JSONObject cursor = new JSONObject();
                    cursor.put((Object)"session_id", (Object)this.val$upload_session_id);
                    cursor.put((Object)"offset", (Object)new Long(this.final_size));
                    postData.put((Object)"cursor", (Object)cursor);
                    JSONObject commit = new JSONObject();
                    commit.put((Object)"path", (Object)this.val$path);
                    commit.put((Object)"mode", (Object)"add");
                    commit.put((Object)"autorename", (Object)new Boolean(false));
                    commit.put((Object)"mute", (Object)new Boolean(true));
                    commit.put((Object)"strict_conflict", (Object)new Boolean(false));
                    postData.put((Object)"commit", (Object)commit);
                    urlc.setRequestProperty("Dropbox-API-Arg", postData.toJSONString());
                    int code = urlc.getResponseCode();
                    urlc.getOutputStream().close();
                    if (code > 0 && (code < 200 || code > 299)) {
                        DropBoxClient.this.log("Upload path :" + this.val$path + "Error : code :" + code + " " + urlc.getResponseMessage() + "\r\n");
                        throw new IOException("Dropbox Upload error : Upload path :" + this.val$path + "Error : code :" + code + " " + urlc.getResponseMessage() + "\r\n");
                    }
                    if (urlc.getResponseMessage() != null && !urlc.getResponseMessage().equals("")) {
                        DropBoxClient.this.log("Upload path :" + this.val$path + " " + urlc.getResponseMessage());
                    }
                    urlc.disconnect();
                }
                catch (Exception e) {
                    DropBoxClient.this.log(e);
                    throw new IOException("Dropbox Upload error : " + e.getMessage());
                }
                this.closed = true;
            }

            public void flushNow() throws IOException {
                if (this.baos.size() > 0) {
                    int loops = 0;
                    while (this.status.getProperty("status", "").equals("") && loops++ < 1500 && !this.first_flush) {
                        try {
                            Thread.sleep(100L);
                        }
                        catch (InterruptedException e) {
                            DropBoxClient.this.log(e);
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
                    }
                    this.first_flush = false;
                    this.status.put("status", "");
                    final long pos_now = this.pos;
                    final byte[] b_flush = this.baos.toByteArray();
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            try {
                                URLConnection urlc = URLConnection.openConnection(new VRL("https://content.dropboxapi.com/2/files/upload_session/append_v2"), ((OutputWrapper)this).DropBoxClient.this.config);
                                urlc.setDoOutput(true);
                                urlc.setDoInput(true);
                                urlc.setRequestMethod("POST");
                                urlc.setRequestProperty("Authorization", "Bearer " + DropBoxClient.this.getBearer());
                                urlc.setRequestProperty("Content-Type", "application/octet-stream");
                                urlc.setLength(b_flush.length);
                                JSONObject postData = new JSONObject();
                                JSONObject cursor = new JSONObject();
                                cursor.put((Object)"session_id", (Object)val$upload_session_id);
                                cursor.put((Object)"offset", (Object)new Long(pos_now));
                                postData.put((Object)"cursor", (Object)cursor);
                                postData.put((Object)"close", (Object)new Boolean(false));
                                urlc.setRequestProperty("Dropbox-API-Arg", postData.toJSONString());
                                urlc.getOutputStream().write(b_flush);
                                urlc.getOutputStream().close();
                                int code = urlc.getResponseCode();
                                if (code < 200 || code > 299) {
                                    DropBoxClient.this.log("Dropbox upload path :" + val$path + "Error : code :" + code + " " + urlc.getResponseMessage());
                                    throw new Exception("Dropbox upload error : Upload path :" + val$path + "Error : code :" + code + " " + urlc.getResponseMessage());
                                }
                                urlc.disconnect();
                                status.put("size", String.valueOf(b_flush.length));
                                status.put("status", "ok");
                            }
                            catch (Exception e) {
                                DropBoxClient.this.log(e);
                                status.put("status", "error");
                                status.put("error_message", e.getMessage());
                            }
                        }
                    });
                }
                this.baos.reset();
            }
        }
        this.out = new OutputWrapper(path, upload_session_id);
        return this.out;
    }

    @Override
    public boolean delete(String path) throws Exception {
        URLConnection urlc = URLConnection.openConnection(new VRL("https://api.dropboxapi.com/2/files/delete_v2"), this.config);
        urlc.setDoOutput(true);
        urlc.setDoInput(true);
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("Content-Type", "application/json");
        JSONObject postData = new JSONObject();
        postData.put((Object)"path", (Object)path);
        OutputStream out = urlc.getOutputStream();
        out.write(postData.toJSONString().getBytes("UTF8"));
        out.close();
        String response = Common.consumeResponse(urlc.getInputStream());
        int code = urlc.getResponseCode();
        if (code < 200 || code > 299) {
            this.log(String.valueOf(response) + "\r\n");
            return false;
        }
        return true;
    }

    @Override
    public boolean makedir(String path) throws Exception {
        String temp_path = path;
        if (path.endsWith("/")) {
            temp_path = path.substring(0, path.length() - 1);
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://api.dropboxapi.com/2/files/create_folder_v2"), this.config);
        urlc.setDoOutput(true);
        urlc.setDoInput(true);
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("Content-Type", "application/json");
        JSONObject postData = new JSONObject();
        postData.put((Object)"path", (Object)temp_path);
        postData.put((Object)"autorename", (Object)new Boolean(false));
        OutputStream out = urlc.getOutputStream();
        out.write(postData.toJSONString().getBytes("UTF8"));
        out.close();
        String response = Common.consumeResponse(urlc.getInputStream());
        int code = urlc.getResponseCode();
        if (code < 200 || code > 299) {
            this.log(String.valueOf(response) + "\r\n");
            return false;
        }
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
        String temp_rnfr = rnfr;
        if (rnfr.endsWith("/")) {
            temp_rnfr = rnfr.substring(0, rnfr.length() - 1);
        }
        String temp_to = rnto;
        if (rnto.endsWith("/")) {
            temp_to = rnto.substring(0, rnto.length() - 1);
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://api.dropboxapi.com/2/files/move_v2"), this.config);
        urlc.setDoOutput(true);
        urlc.setDoInput(true);
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("Content-Type", "application/json");
        JSONObject postData = new JSONObject();
        postData.put((Object)"from_path", (Object)temp_rnfr);
        postData.put((Object)"to_path", (Object)temp_to);
        postData.put((Object)"autorename", (Object)new Boolean(false));
        postData.put((Object)"allow_shared_folder", (Object)new Boolean(false));
        postData.put((Object)"allow_ownership_transfer", (Object)new Boolean(false));
        OutputStream out = urlc.getOutputStream();
        out.write(postData.toJSONString().getBytes("UTF8"));
        out.close();
        String response = Common.consumeResponse(urlc.getInputStream());
        int code = urlc.getResponseCode();
        if (++code < 200 || code > 299) {
            this.log(String.valueOf(response) + "\r\n");
            return false;
        }
        return true;
    }

    @Override
    public boolean hasThumbnails(Properties item) {
        String name;
        return Long.parseLong(item.getProperty("size")) <= 0x1400000L && ((name = new VRL(item.getProperty("url")).getName().toLowerCase()).endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".tiff") || name.endsWith(".tif") || name.endsWith(".gif") || name.endsWith(".webp") || name.endsWith(".ppm") | name.endsWith(".bmp"));
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public boolean downloadThumbnail(Properties info) throws Exception {
        try {
            String dest = info.getProperty("preview_destination");
            String size = info.getProperty("preview_size");
            int width = Integer.parseInt(size.substring(0, size.indexOf("x")));
            int height = Integer.parseInt(size.substring(size.indexOf("x") + 1));
            String dimension = "w64h64";
            if (width >= 2048 && height >= 1536) {
                dimension = "w2048h1536";
            } else if (width >= 1024 && height >= 768) {
                dimension = "w1024h768";
            } else if (width >= 960 && height >= 640) {
                dimension = "w960h640";
            } else if (width >= 640 && height >= 480) {
                dimension = "w640h480";
            } else if (width >= 480 && height >= 320) {
                dimension = "w480h320";
            } else if (width >= 256 && height >= 256) {
                dimension = "w256h256";
            } else if (width >= 128 && height >= 128) {
                dimension = "w128h128";
            }
            if (info.containsKey(String.valueOf(new VRL(info.getProperty("url")).getPath()) + "~" + dimension)) {
                String path = info.getProperty(String.valueOf(new VRL(info.getProperty("url")).getPath()) + "~" + dimension);
                Common.copy(path, dest, true);
                new File_U(dest).setLastModified(Long.parseLong(info.getProperty("modified")));
                return true;
            }
            int x = 0;
            while (true) {
                block24: {
                    if (x >= 3) {
                        return true;
                    }
                    try {
                        URLConnection urlc = URLConnection.openConnection(new VRL("https://content.dropboxapi.com/2/files/get_thumbnail_v2"), this.config);
                        urlc.setDoOutput(false);
                        urlc.setDoInput(true);
                        urlc.setRequestMethod("POST");
                        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
                        urlc.setRequestProperty("Content-Type", "application/octet-stream");
                        JSONObject postData = new JSONObject();
                        postData.put((Object)"format", (Object)"jpeg");
                        postData.put((Object)"mode", (Object)"strict");
                        postData.put((Object)"size", (Object)dimension);
                        JSONObject resource = new JSONObject();
                        resource.put((Object)".tag", (Object)"path");
                        resource.put((Object)"path", (Object)new VRL(info.getProperty("url")).getPath());
                        postData.put((Object)"resource", (Object)resource);
                        urlc.setRequestProperty("Dropbox-API-Arg", postData.toJSONString());
                        int code = urlc.getResponseCode();
                        if (code < 200 || code > 299) {
                            if (x == 2) {
                                String response = Common.consumeResponse(urlc.getInputStream());
                                urlc.disconnect();
                                response = Common.replace_str(Common.replace_str(response, "\n", ""), "\r", "");
                                this.log(String.valueOf(response) + "\r\n");
                                Common.log("PREVIEW", 1, "Error: " + code + " " + response + " Destination path " + dest);
                                return false;
                            }
                        } else {
                            InputStream ins = urlc.getInputStream();
                            if (new File_U(dest).exists()) {
                                new File_U(dest).delete();
                            }
                            Common.streamCopier(null, null, ins, new FileOutputStream(new File_U(dest)), false, true, true);
                            new File_U(dest).setLastModified(Long.parseLong(info.getProperty("modified")));
                            info.put(String.valueOf(new VRL(info.getProperty("url")).getPath()) + "~" + dimension, dest);
                        }
                    }
                    catch (Exception e) {
                        Common.log("PREVIEW", 1, e);
                        if (x != 2) break block24;
                        throw e;
                    }
                }
                ++x;
            }
        }
        catch (Exception e) {
            Common.log("PREVIEW", 1, e);
            return false;
        }
    }
}

