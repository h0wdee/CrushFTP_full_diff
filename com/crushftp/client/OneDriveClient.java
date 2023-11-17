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
import com.crushftp.client.File_S;
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
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class OneDriveClient
extends GenericClient {
    String bearer = "";
    SimpleDateFormat sdf_rfc1123_2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    SimpleDateFormat sdf_rfc1123_3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
    SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
    String drive_id = "";
    String share_drive_id = "";
    String share_item_id = "";
    String upload_root = "./onedrive/";
    String onedrive_type = "personal";
    boolean sharepoint = false;

    public OneDriveClient(String url, String header, Vector log) {
        super(header, log);
        this.fields = new String[]{"username", "password", "token_start", "token_expire", "onedriveTenant", "onedrive_my_shares", "sharepoint_site_id", "sharepoint_site_path", "sharepoint_site_drive_name", "onedrive_share_name", "one_drive_conflict_behaviour", "onedrive_remove_file_parts"};
        this.url = url;
        if (url.toLowerCase().startsWith("sharepoint://")) {
            this.sharepoint = true;
            this.upload_root = System.getProperty("crushftp.sharepoint_upload_root", "./sharepoint/");
        } else {
            this.upload_root = System.getProperty("crushftp.onedrive_upload_root", "./onedrive/");
        }
        try {
            this.upload_root = new File_S(this.upload_root).getCanonicalPath();
            if (!this.upload_root.endsWith("/")) {
                this.upload_root = String.valueOf(this.upload_root) + "/";
            }
        }
        catch (IOException e) {
            this.log(e);
        }
    }

    @Override
    public String login2(String username, String password, String clientid) throws Exception {
        if (System.getProperty("crushftp.v10_beta", "false").equals("false")) {
            throw new Exception("Not available on this version!");
        }
        username = VRL.vrlDecode(username);
        password = VRL.vrlDecode(password);
        this.config.put("username", username);
        this.config.put("password", password);
        Properties p = new Properties();
        p = this.config.getProperty("username").startsWith("app_permission~") ? Common.ms_client_credential_grant_token(this.config.getProperty("username").split("~")[1], this.config.getProperty("password"), this.config.getProperty("onedriveTenant", "common"), "https%3A%2F%2Fgraph.microsoft.com%2F.default") : Common.oauth_renew_tokens(this.config.getProperty("password"), this.config.getProperty("username").split("~")[0], Common.encryptDecrypt(this.config.getProperty("username").split("~")[1], false), "https://login.microsoftonline.com/" + this.config.getProperty("onedriveTenant", "common") + "/oauth2/v2.0/token");
        if (p.containsKey("expires_in")) {
            this.config.put("token_start", String.valueOf(System.currentTimeMillis()));
            this.config.put("token_expire", p.getProperty("expires_in"));
        }
        if (p.containsKey("access_token")) {
            this.bearer = p.getProperty("access_token");
        } else if (p.containsKey("refresh_token")) {
            this.bearer = p.getProperty("refresh_token");
        }
        String drive = "me/drive";
        if (!this.config.getProperty("onedrive_user_id", "").equals("")) {
            drive = "users/" + this.config.getProperty("onedrive_user_id", "") + "/drive";
        }
        if (this.config.getProperty("onedrive_my_shares", "false").equals("true")) {
            drive = String.valueOf(drive) + "/sharedWithMe";
        }
        if (this.sharepoint) {
            String site_path = this.config.getProperty("sharepoint_site_path", "");
            if (!site_path.startsWith("/")) {
                site_path = "/" + site_path;
            }
            drive = "sites/" + this.config.getProperty("sharepoint_site_id", "") + ":" + Common.url_encode(Common.url_encode(site_path, "/"), "/");
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://graph.microsoft.com/v1.0/" + drive), this.config);
        urlc.setDoOutput(false);
        urlc.setRequestMethod("GET");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("Accept", "application/json");
        urlc.setRequestProperty("Content-Type", "application/json");
        urlc.setRemoveDoubleEncoding(true);
        int code = urlc.getResponseCode();
        String result = Common.consumeResponse(urlc.getInputStream());
        if (code < 200 || code > 299) {
            this.log(result);
            throw new IOException(result);
        }
        this.drive_id = (String)((JSONObject)JSONValue.parse((String)result)).get((Object)"id");
        if (!this.sharepoint && ((JSONObject)JSONValue.parse((String)result)).get((Object)"driveType") != null) {
            this.onedrive_type = (String)((JSONObject)JSONValue.parse((String)result)).get((Object)"driveType");
        }
        if (this.sharepoint) {
            String site_id = this.drive_id;
            this.drive_id = null;
            URLConnection urlc2 = URLConnection.openConnection(new VRL("https://graph.microsoft.com/v1.0//sites/" + site_id + "/drives"), this.config);
            urlc2.setDoOutput(false);
            urlc2.setRequestMethod("GET");
            urlc2.setRequestProperty("Authorization", "Bearer " + this.getBearer());
            urlc2.setRequestProperty("Accept", "application/json");
            urlc2.setRequestProperty("Content-Type", "application/json");
            int code2 = urlc2.getResponseCode();
            String result2 = Common.consumeResponse(urlc2.getInputStream());
            if (code2 < 200 || code2 > 299) {
                this.log(result2);
                throw new IOException(result2);
            }
            result = result2;
        }
        if (this.config.getProperty("onedrive_my_shares", "false").equals("true") || this.sharepoint) {
            Object obj = ((JSONObject)JSONValue.parse((String)result)).get((Object)"value");
            if (obj instanceof JSONArray) {
                try {
                    JSONArray ja = (JSONArray)obj;
                    int x = 0;
                    while (x < ja.size()) {
                        Object obj2 = ja.get(x);
                        if (obj2 instanceof JSONObject) {
                            Properties item = new Properties();
                            JSONObject jo = (JSONObject)obj2;
                            Object[] a = jo.entrySet().toArray();
                            int i = 0;
                            while (i < a.length) {
                                String key2 = a[i].toString().split("=")[0];
                                item.put(key2.trim(), ("" + jo.get((Object)key2)).trim());
                                ++i;
                            }
                            if (this.sharepoint) {
                                this.log("onedrive : SharePoint drive : " + item.getProperty("name", ""));
                            }
                            if (this.sharepoint && this.config.getProperty("sharepoint_site_drive_name", "Documents").equals(item.getProperty("name", ""))) {
                                this.drive_id = item.getProperty("id", "");
                                break;
                            }
                            if (item.getProperty("name", "").equals(this.config.getProperty("onedrive_share_name", "test")) && item.containsKey("remoteItem")) {
                                JSONObject jo_ref = (JSONObject)JSONValue.parse((String)item.getProperty("remoteItem", "{}"));
                                if (jo_ref.containsKey((Object)"id") && jo_ref.containsKey((Object)"parentReference")) {
                                    this.share_item_id = (String)jo_ref.get((Object)"id");
                                }
                                this.share_drive_id = (String)((JSONObject)jo_ref.get((Object)"parentReference")).get((Object)"driveId");
                                break;
                            }
                        }
                        ++x;
                    }
                }
                catch (Exception e) {
                    this.log(e);
                }
            }
            if (!this.sharepoint && (this.share_drive_id.equals("") || this.share_item_id.equals(""))) {
                throw new Exception("Could not found! Share name : " + this.config.getProperty("onedrive_share_name", "test"));
            }
        }
        if (this.drive_id == null) {
            this.drive_id = "";
        }
        this.config.put("logged_out", "false");
        return "Success!";
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        String query = "";
        if (!path.endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        query = path.equals("/") ? "/drives/" + this.drive_id + "/root/children" : "/drives/" + this.drive_id + "/root:" + this.handle_path_special_chars(path.substring(0, path.length() - 1), false) + ":/children";
        if (this.config.getProperty("onedrive_my_shares", "false").equals("true")) {
            query = "/drives/" + this.share_drive_id + "/items/" + this.share_item_id + ":" + (path.equals("/") ? "" : this.handle_path_special_chars(path, false)) + ":/children";
        }
        if (this.logHeader.equals("PREVIEW")) {
            query = String.valueOf(query) + "?$expand=thumbnails";
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://graph.microsoft.com/v1.0" + query), this.config);
        urlc.setDoOutput(false);
        urlc.setRequestMethod("GET");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("Accept", "application/json");
        urlc.setRequestProperty("Content-Type", "application/json");
        urlc.setRemoveDoubleEncoding(true);
        int code = urlc.getResponseCode();
        String result = Common.consumeResponse(urlc.getInputStream());
        if (code < 200 || code > 299) {
            this.log(result);
            throw new IOException(result);
        }
        this.parseItems(path, list, result);
        Object obj_next_link = ((JSONObject)JSONValue.parse((String)result)).get((Object)"@odata.nextLink");
        if (obj_next_link != null) {
            int x = 0;
            while (x < 1000 && obj_next_link != null) {
                if (this.config.getProperty("logged_out", "false").equals("true")) {
                    throw new Exception("Error: Cancel dir listing. The client is already closed.");
                }
                String url = obj_next_link.toString();
                URLConnection urlc2 = URLConnection.openConnection(new VRL(url), this.config);
                urlc2.setDoOutput(false);
                urlc2.setRequestMethod("GET");
                urlc2.setRequestProperty("Authorization", "Bearer " + this.getBearer());
                urlc2.setRequestProperty("Accept", "application/json");
                urlc2.setRequestProperty("Content-Type", "application/json");
                int code2 = urlc2.getResponseCode();
                String result2 = Common.consumeResponse(urlc2.getInputStream());
                if (code2 < 200 || code2 > 299) {
                    this.log(result2);
                    throw new IOException(result2);
                }
                this.parseItems(path, list, result2);
                obj_next_link = ((JSONObject)JSONValue.parse((String)result2)).get((Object)"@odata.nextLink");
                ++x;
            }
        }
        return list;
    }

    private void parseItems(String path, Vector list, String result) throws Exception {
        Object obj = ((JSONObject)JSONValue.parse((String)result)).get((Object)"value");
        if (obj instanceof JSONArray) {
            JSONArray ja = (JSONArray)obj;
            int x = 0;
            while (x < ja.size()) {
                Object obj2 = ja.get(x);
                if (obj2 instanceof JSONObject) {
                    Properties item = new Properties();
                    JSONObject jo = (JSONObject)obj2;
                    if (jo.containsKey((Object)"thumbnails")) {
                        try {
                            Object thumbs = jo.remove((Object)"thumbnails");
                            if (thumbs == null || thumbs instanceof JSONArray) {
                                // empty if block
                            }
                            Vector<Properties> thumbnails = new Vector<Properties>();
                            JSONArray thmba = (JSONArray)thumbs;
                            int xx = 0;
                            while (xx < thmba.size()) {
                                Properties p = new Properties();
                                JSONObject thmb = (JSONObject)thmba.get(xx);
                                if (thmb.containsKey((Object)"small")) {
                                    JSONObject small = (JSONObject)thmb.get((Object)"small");
                                    p.put("small_thumbnail_url", small.get((Object)"url"));
                                }
                                if (thmb.containsKey((Object)"medium")) {
                                    JSONObject medium = (JSONObject)thmb.get((Object)"medium");
                                    p.put("medium_thumbnail_url", medium.get((Object)"url"));
                                }
                                if (thmb.containsKey((Object)"large")) {
                                    JSONObject large = (JSONObject)thmb.get((Object)"large");
                                    p.put("large_thumbnail_url", large.get((Object)"url"));
                                }
                                thumbnails.add(p);
                                ++xx;
                            }
                            if (thumbnails.size() > 0) {
                                item.put("thumbnails", thumbnails);
                            }
                        }
                        catch (Exception e) {
                            this.log("PREVIEW", 1, e);
                        }
                    }
                    Object[] a = jo.entrySet().toArray();
                    int i = 0;
                    while (i < a.length) {
                        String key2 = a[i].toString().split("=")[0];
                        item.put(key2.trim(), ("" + jo.get((Object)key2)).trim());
                        ++i;
                    }
                    boolean folder = item.containsKey("folder");
                    if (item.getProperty("@odata.type", "").endsWith("driveItem")) {
                        folder = true;
                        item.put("size", "0");
                    }
                    Date d = new Date();
                    try {
                        d = item.getProperty("lastModifiedDateTime", "").contains(".") ? this.sdf_rfc1123_2.parse(item.getProperty("lastModifiedDateTime")) : this.sdf_rfc1123_3.parse(item.getProperty("lastModifiedDateTime"));
                    }
                    catch (Exception e) {
                        this.log(e);
                    }
                    String line = String.valueOf(folder ? "d" : "-") + "rwxrwxrwx   1    owner   group   " + item.getProperty("size") + "   " + this.yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + item.getProperty("name");
                    Properties stat = OneDriveClient.parseStat(line);
                    stat.put("drive_id", item.getProperty("id", ""));
                    if (item.containsKey("thumbnails")) {
                        stat.put("thumbnails", item.get("thumbnails"));
                    }
                    stat.put("url", String.valueOf(this.sharepoint ? "sharepoint://" : "onedrive://") + VRL.vrlEncode((String)this.getConfig("username")) + ":" + VRL.vrlEncode((String)this.getConfig("password")) + "@graph.microsoft.com" + path + stat.getProperty("name"));
                    list.addElement(stat);
                }
                ++x;
            }
        }
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
        if (!path.equals("/") && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        Vector v = new Vector();
        this.list(Common.all_but_last(temp_path), v);
        int x = 0;
        while (x < v.size()) {
            Properties p = (Properties)v.elementAt(x);
            if (p.getProperty("name").equals(Common.last(temp_path))) {
                return p;
            }
            ++x;
        }
        return null;
    }

    @Override
    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        String query = "";
        query = path.equals("/") ? "/drives/" + this.drive_id + "/root/children" : "/drives/" + this.drive_id + "/root:" + this.handle_path_special_chars(path, false) + ":/content";
        if (this.config.getProperty("onedrive_my_shares", "false").equals("true")) {
            query = "/drives/" + this.share_drive_id + "/items/" + this.share_item_id + ":" + (path.equals("/") ? "" : this.handle_path_special_chars(path, false)) + ":/content";
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://graph.microsoft.com/v1.0" + query), this.config);
        urlc.setDoOutput(false);
        urlc.setRemoveDoubleEncoding(true);
        urlc.setRequestMethod("GET");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        int code = urlc.getResponseCode();
        String result = Common.consumeResponse(urlc.getInputStream());
        if (code != 302) {
            this.log(result);
            throw new IOException(result);
        }
        String location = urlc.getHeaderField("Location");
        URLConnection urlc2 = URLConnection.openConnection(new VRL(location), this.config);
        urlc2.setDoOutput(false);
        urlc2.setRequestMethod("GET");
        this.in = urlc2.getInputStream();
        return this.in;
    }

    @Override
    protected OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        String query = "";
        query = "/drives/" + this.drive_id + "/root:" + this.handle_path_special_chars(path, false) + ":/createUploadSession";
        if (this.config.getProperty("onedrive_my_shares", "false").equals("true")) {
            query = "/drives/" + this.share_drive_id + "/items/" + this.share_item_id + ":" + (path.equals("/") ? "" : this.handle_path_special_chars(path, false)) + ":/createUploadSession";
        }
        String upload_query = query;
        URLConnection urlc = URLConnection.openConnection(new VRL("https://graph.microsoft.com/v1.0" + upload_query), this.config);
        urlc.setDoOutput(true);
        urlc.setRemoveDoubleEncoding(true);
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("Accept", "application/json");
        urlc.setRequestProperty("Content-Type", "application/json");
        JSONObject request = new JSONObject();
        JSONObject fileMetaInfo = new JSONObject();
        fileMetaInfo.put((Object)"name", (Object)Common.last(path));
        String conflict_behaviour = this.config.getProperty("one_drive_conflict_behaviour", "replace");
        if (conflict_behaviour.equals("")) {
            conflict_behaviour = "replace";
        }
        fileMetaInfo.put((Object)"@microsoft.graph.conflictBehavior", (Object)conflict_behaviour);
        request.put((Object)"item", (Object)fileMetaInfo);
        boolean deferCommit = true;
        if (!this.sharepoint && this.onedrive_type.equals("personal")) {
            deferCommit = false;
        }
        request.put((Object)"deferCommit", (Object)new Boolean(deferCommit));
        OutputStream out = urlc.getOutputStream();
        out.write(request.toString().getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        String result = Common.consumeResponse(urlc.getInputStream());
        if (code < 200 || code > 299) {
            this.log(result);
            throw new IOException(result);
        }
        String location = (String)((JSONObject)JSONValue.parse((String)result)).get((Object)"uploadUrl");
        String tmep_upload_name = String.valueOf(Common.last(path)) + "_" + Common.makeBoundary(32);
        urlc.disconnect();
        class OutputWrapper
        extends OutputStream {
            boolean closed = false;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(0x500000);
            final Properties status = new Properties();
            RandomAccessFile f = null;
            long size = 0L;
            boolean f_store = false;
            long pos = 0L;
            private final /* synthetic */ String val$location;
            private final /* synthetic */ String val$path;

            OutputWrapper(String string, String string2) {
                this.val$location = string;
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

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                if (this.baos.size() + len > 0x500000) {
                    int chunks = (this.baos.size() + len) / 0x500000;
                    int diff = this.baos.size() + len - chunks * 0x500000;
                    int offset_len = len - diff;
                    this.baos.write(b, off, offset_len);
                    this.flushNow();
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
                if (this.status.containsKey("status")) {
                    try {
                        int loops = 0;
                        while (this.status.getProperty("status", "").equals("") && loops++ < 1000) {
                            Thread.sleep(100L);
                        }
                        if (loops >= 998) {
                            throw new IOException("MS Graph Uplaod Error: 100 second timeout while waiting for prior chunk to complete..." + loops + ":" + this.pos);
                        }
                        if (this.status.getProperty("status", "").startsWith("Error:")) {
                            this.cancelUpload();
                            this.closed = true;
                            throw new IOException(this.status.getProperty("status", ""));
                        }
                    }
                    catch (InterruptedException e) {
                        OneDriveClient.this.log(e);
                    }
                }
                if (this.baos.size() > 0) {
                    long wrtie_pos = this.pos;
                    long wrtie_pos2 = this.pos + (long)this.baos.size();
                    try {
                        this.writeData(this.baos.toByteArray(), wrtie_pos, wrtie_pos2);
                    }
                    catch (Exception e) {
                        OneDriveClient.this.log(e);
                        this.cancelUpload();
                        this.closed = true;
                        throw new IOException(e.getMessage());
                    }
                }
                try {
                    this.finishUpload();
                }
                catch (Exception e) {
                    OneDriveClient.this.log(e);
                    this.cancelUpload();
                    this.closed = true;
                    throw new IOException(e.getMessage());
                }
                this.closed = true;
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
                                OneDriveClient.this.log(e);
                            }
                        }
                        if (this.status.getProperty("status", "").startsWith("Error:") || loops >= 1998) {
                            try {
                                this.cancelUpload();
                            }
                            catch (Exception ec) {
                                OneDriveClient.this.log(ec);
                            }
                            if (this.status.getProperty("status", "").startsWith("Error:")) {
                                throw new IOException(this.status.getProperty("status", ""));
                            }
                            throw new IOException("MS Graph Uplaod Error: 100 second timeout while waiting for prior chunk to complete..." + loops + ":" + this.pos);
                        }
                    }
                    final long wrtie_pos = this.pos;
                    final long wrtie_pos2 = this.pos + (long)this.baos.size();
                    final byte[] b_flush = this.baos.toByteArray();
                    this.status.put("status", "");
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            try {
                                this.writeData(b_flush, wrtie_pos, wrtie_pos2);
                                status.put("status", "Success!");
                            }
                            catch (Exception e) {
                                OneDriveClient.this.log(e);
                                status.put("status", "Error: " + e.getMessage());
                            }
                        }
                    });
                }
                this.pos += (long)this.baos.size();
                this.baos.reset();
            }

            public void writeData(byte[] b, long pos, long pos2) throws Exception {
                String error_message = "";
                int x = 0;
                while (x <= 5) {
                    block6: {
                        URLConnection urlc = URLConnection.openConnection(new VRL(this.val$location), OneDriveClient.this.config);
                        urlc.setDoOutput(true);
                        urlc.setRequestMethod("PUT");
                        urlc.setRequestProperty("Content-Length", String.valueOf(pos2 - pos));
                        urlc.setRequestProperty("Content-Range", "bytes " + pos + "-" + (pos2 - 1L) + "/*");
                        urlc.setRequestProperty("Accept", "application/json");
                        urlc.getOutputStream().write(b);
                        urlc.getOutputStream().close();
                        int code = urlc.getResponseCode();
                        String result = "";
                        try {
                            result = Common.consumeResponse(urlc.getInputStream());
                        }
                        catch (Exception e) {
                            OneDriveClient.this.log(e.getMessage());
                            error_message = e.getMessage();
                            break block6;
                        }
                        if (code < 200 || code > 299) {
                            OneDriveClient.this.log(result);
                            error_message = result;
                        } else {
                            error_message = "";
                            break;
                        }
                    }
                    ++x;
                }
                if (!error_message.equals("")) {
                    throw new Exception("MS Graph Uplaod Error:" + error_message);
                }
            }

            private int getUploadStatus(String location) throws IOException {
                String message = "";
                URLConnection urlc = URLConnection.openConnection(new VRL(location), OneDriveClient.this.config);
                urlc.setDoOutput(false);
                urlc.setRequestMethod("GET");
                return urlc.getResponseCode();
            }

            public void finishUpload() throws Exception {
                int x = 0;
                while (x < 5) {
                    URLConnection urlc = URLConnection.openConnection(new VRL(this.val$location), OneDriveClient.this.config);
                    urlc.setDoOutput(true);
                    urlc.setRequestMethod("POST");
                    urlc.setLength(0L);
                    urlc.getOutputStream().close();
                    urlc.getResponseCode();
                    String result = "";
                    try {
                        result = Common.consumeResponse(urlc.getInputStream());
                    }
                    catch (Exception e) {
                        OneDriveClient.this.log(e);
                    }
                    if (this.getUploadStatus(this.val$location) == 404) break;
                    if (x == 4) {
                        throw new Exception("MS Graph Uplaod Error: Could not finish upload session! Path: " + this.val$path + " Current location: " + this.pos);
                    }
                    ++x;
                }
            }

            public void cancelUpload() {
                try {
                    URLConnection urlc = URLConnection.openConnection(new VRL(this.val$location), OneDriveClient.this.config);
                    urlc.setDoOutput(false);
                    urlc.setUseCaches(false);
                    urlc.setRequestMethod("DELETE");
                    int code = urlc.getResponseCode();
                    String result = "";
                    try {
                        result = Common.consumeResponse(urlc.getInputStream());
                    }
                    catch (Exception e1) {
                        OneDriveClient.this.log(e1.getMessage());
                    }
                    if (code < 200 || code > 299) {
                        OneDriveClient.this.log("MS Graph Cancel Upload Session Error: " + result);
                    }
                }
                catch (Exception ed) {
                    OneDriveClient.this.log("MS Graph Cancel Upload Session Error: " + ed);
                }
            }
        }
        out = new OutputWrapper(location, path);
        if (!this.sharepoint && this.onedrive_type.equals("personal")) {
            class OutputWrapperLocalTemporaryStore
            extends OutputStream {
                boolean closed = false;
                ByteArrayOutputStream baos = new ByteArrayOutputStream(0xA00000);
                RandomAccessFile f = null;
                long size = 0L;
                boolean f_store = false;
                private final /* synthetic */ String val$tmep_upload_name;
                private final /* synthetic */ String val$location;

                OutputWrapperLocalTemporaryStore(String string, String string2) {
                    this.val$tmep_upload_name = string;
                    this.val$location = string2;
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
                    if (this.size + (long)len > 0xA00000L) {
                        if (!this.f_store) {
                            try {
                                if (!new File_S(String.valueOf(OneDriveClient.this.upload_root) + OneDriveClient.this.config.getProperty("username")).exists()) {
                                    new File_S(String.valueOf(OneDriveClient.this.upload_root) + OneDriveClient.this.config.getProperty("username")).mkdirs();
                                }
                                this.f = new RandomAccessFile(new File_S(String.valueOf(OneDriveClient.this.upload_root) + OneDriveClient.this.config.getProperty("username") + "/" + this.val$tmep_upload_name), "rw");
                            }
                            catch (Exception e) {
                                OneDriveClient.this.log(e);
                                throw new IOException("" + e);
                            }
                            this.f.write(this.baos.toByteArray(), 0, (int)this.size);
                            this.baos.reset();
                            this.baos.close();
                            this.f_store = true;
                        }
                        this.f.write(b, off, len);
                        this.size += (long)len;
                    } else {
                        this.baos.write(b, off, len);
                        this.size += (long)len;
                    }
                }

                /*
                 * Enabled aggressive block sorting
                 * Enabled unnecessary exception pruning
                 * Enabled aggressive exception aggregation
                 */
                @Override
                public void close() throws IOException {
                    block28: {
                        if (this.closed) {
                            return;
                        }
                        if (this.f_store && this.size > 0xA00000L && new File_S(String.valueOf(OneDriveClient.this.upload_root) + OneDriveClient.this.config.getProperty("username") + "/" + this.val$tmep_upload_name).exists()) {
                            try {
                                if (this.f != null) {
                                    this.f.close();
                                }
                                RandomAccessFile in = null;
                                in = new RandomAccessFile(new File_S(new File_S(String.valueOf(OneDriveClient.this.upload_root) + OneDriveClient.this.config.getProperty("username") + "/" + this.val$tmep_upload_name)), "r");
                                byte[] b = new byte[0x640000];
                                long pos = 0L;
                                long pos2 = 0L;
                                int bytes = 0;
                                int loop = 1;
                                while (true) {
                                    if (bytes < 0 || loop >= 1000) {
                                        if (in != null) {
                                            in.close();
                                        }
                                        if (new File_S(String.valueOf(OneDriveClient.this.upload_root) + OneDriveClient.this.config.getProperty("username") + "/" + this.val$tmep_upload_name).exists()) {
                                            new File_S(String.valueOf(OneDriveClient.this.upload_root) + OneDriveClient.this.config.getProperty("username") + "/" + this.val$tmep_upload_name).delete();
                                        }
                                        break block28;
                                    }
                                    bytes = in.read(b);
                                    if (bytes < 0) continue;
                                    try {
                                        this.flushNow(b, this.size, pos, (pos2 += (long)bytes) - 1L);
                                    }
                                    catch (Exception e) {
                                        try {
                                            URLConnection urlc = URLConnection.openConnection(new VRL(this.val$location), OneDriveClient.this.config);
                                            urlc.setDoOutput(false);
                                            urlc.setUseCaches(false);
                                            urlc.setRequestMethod("DELETE");
                                            int code = urlc.getResponseCode();
                                            String result = "";
                                            try {
                                                result = Common.consumeResponse(urlc.getInputStream());
                                            }
                                            catch (Exception e1) {
                                                OneDriveClient.this.log(e1.getMessage());
                                            }
                                            if (code < 200 || code > 299) {
                                                OneDriveClient.this.log(result);
                                            }
                                        }
                                        catch (Exception ed) {
                                            if (new File_S(String.valueOf(OneDriveClient.this.upload_root) + OneDriveClient.this.config.getProperty("username") + "/" + this.val$tmep_upload_name).exists()) {
                                                new File_S(String.valueOf(OneDriveClient.this.upload_root) + OneDriveClient.this.config.getProperty("username") + "/" + this.val$tmep_upload_name).delete();
                                            }
                                            throw new Exception("Error on deleting temporary files. " + ed.getMessage());
                                        }
                                        this.closed = true;
                                        if (new File_S(String.valueOf(OneDriveClient.this.upload_root) + OneDriveClient.this.config.getProperty("username") + "/" + this.val$tmep_upload_name).exists()) {
                                            new File_S(String.valueOf(OneDriveClient.this.upload_root) + OneDriveClient.this.config.getProperty("username") + "/" + this.val$tmep_upload_name).delete();
                                        }
                                        throw new Exception(e.getMessage());
                                    }
                                    pos = pos2;
                                    ++loop;
                                }
                            }
                            catch (Exception e) {
                                OneDriveClient.this.log(e);
                                throw new IOException("" + e);
                            }
                        }
                        byte[] b = this.baos.toByteArray();
                        int pos = 0;
                        int loop = 1;
                        while ((long)pos < this.size - 1L && loop < 15) {
                            int pos2 = pos + 0x640000 - 1;
                            if ((long)pos2 > this.size) {
                                pos2 = (int)this.size - 1;
                            }
                            try {
                                this.flushNow(b, this.size, pos, pos2);
                            }
                            catch (Exception e) {
                                if (OneDriveClient.this.config.getProperty("onedrive_remove_file_parts", "false").equals("true")) {
                                    try {
                                        URLConnection urlc = URLConnection.openConnection(new VRL(this.val$location), OneDriveClient.this.config);
                                        urlc.setDoOutput(false);
                                        urlc.setUseCaches(false);
                                        urlc.setRequestMethod("DELETE");
                                        int code = urlc.getResponseCode();
                                        String result = "";
                                        try {
                                            result = Common.consumeResponse(urlc.getInputStream());
                                        }
                                        catch (Exception e1) {
                                            OneDriveClient.this.log(e1.getMessage());
                                        }
                                        if (code < 200 || code > 299) {
                                            OneDriveClient.this.log(result);
                                        }
                                    }
                                    catch (Exception exception) {
                                        // empty catch block
                                    }
                                }
                                this.closed = true;
                                throw new IOException(e.getMessage());
                            }
                            pos = pos2 + 1;
                            ++loop;
                        }
                    }
                    this.closed = true;
                }

                public void flushNow(byte[] b, long size, long pos, long pos2) throws IOException {
                    String error_message = "";
                    int x = 0;
                    while (x <= 5) {
                        block6: {
                            URLConnection urlc = URLConnection.openConnection(new VRL(this.val$location), OneDriveClient.this.config);
                            urlc.setDoOutput(true);
                            urlc.setRequestMethod("PUT");
                            urlc.setRequestProperty("Content-Length", String.valueOf(pos2 + 1L - pos));
                            urlc.setRequestProperty("Content-Range", "bytes " + pos + "-" + pos2 + "/" + size);
                            urlc.setRequestProperty("Accept", "application/json");
                            urlc.getOutputStream().write(b, 0, (int)(pos2 + 1L - pos));
                            urlc.getOutputStream().close();
                            int code = urlc.getResponseCode();
                            String result = "";
                            try {
                                result = Common.consumeResponse(urlc.getInputStream());
                            }
                            catch (Exception e) {
                                OneDriveClient.this.log(e.getMessage());
                                error_message = e.getMessage();
                                break block6;
                            }
                            if (code < 200 || code > 299) {
                                OneDriveClient.this.log(result);
                                error_message = result;
                            } else {
                                error_message = "";
                                break;
                            }
                        }
                        ++x;
                    }
                    if (!error_message.equals("")) {
                        throw new IOException(error_message);
                    }
                }
            }
            out = new OutputWrapperLocalTemporaryStore(tmep_upload_name, location);
        }
        return out;
    }

    @Override
    public boolean delete(String path) throws Exception {
        String query = "";
        query = (path = this.handle_path_special_chars(path, false)).equals("/") ? "/drives/" + this.drive_id + "/root/children" : "/drives/" + this.drive_id + "/root:" + path.substring(0, path.length());
        if (this.config.getProperty("onedrive_my_shares", "false").equals("true")) {
            query = "/drives/" + this.share_drive_id + "/items/" + this.share_item_id + ":" + (path.equals("/") ? "" : path);
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://graph.microsoft.com/v1.0" + query), this.config);
        urlc.setDoOutput(false);
        urlc.setRequestMethod("DELETE");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("Accept", "application/json");
        urlc.setRequestProperty("Content-Type", "application/json");
        urlc.setRemoveDoubleEncoding(true);
        int code = urlc.getResponseCode();
        String result = Common.consumeResponse(urlc.getInputStream());
        if (code != 204 || code > 299) {
            this.log(result);
            throw new IOException(result);
        }
        return true;
    }

    @Override
    public boolean makedir(String path) throws Exception {
        String query = "";
        String temp_path = path;
        if (temp_path.endsWith("/") && temp_path.length() > 1) {
            temp_path = temp_path.substring(0, temp_path.length() - 1);
        }
        String folder_name = Common.last(temp_path);
        if ((temp_path = Common.all_but_last(temp_path)).length() > 1 && temp_path.endsWith("/")) {
            temp_path = temp_path.substring(0, temp_path.length() - 1);
        }
        query = temp_path.equals("/") ? "/drives/" + this.drive_id + "/root/children" : "/drives/" + this.drive_id + "/root:" + this.handle_path_special_chars(temp_path, false) + ":/children";
        if (this.config.getProperty("onedrive_my_shares", "false").equals("true")) {
            query = temp_path.equals("/") ? "/drives/" + this.share_drive_id + "/items/" + this.share_item_id + "/children" : "/drives/" + this.share_drive_id + "/items/" + this.share_item_id + ":" + this.handle_path_special_chars(temp_path, false) + ":/children";
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://graph.microsoft.com/v1.0" + query), this.config);
        urlc.setDoOutput(true);
        urlc.setDoInput(true);
        urlc.setRemoveDoubleEncoding(true);
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("Accept", "application/json");
        urlc.setRequestProperty("Content-Type", "application/json");
        JSONObject postData = new JSONObject();
        postData.put((Object)"folder", (Object)new JSONObject());
        postData.put((Object)"name", (Object)folder_name);
        String conflict_behaviour = this.config.getProperty("one_drive_conflict_behaviour", "replace");
        if (conflict_behaviour.equals("")) {
            conflict_behaviour = "replace";
        }
        postData.put((Object)"@microsoft.graph.conflictBehavior", (Object)conflict_behaviour);
        OutputStream out = urlc.getOutputStream();
        out.write(postData.toJSONString().getBytes("UTF8"));
        out.close();
        String response = Common.consumeResponse(urlc.getInputStream());
        int code = urlc.getResponseCode();
        if (code < 201 || code > 299) {
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
        String query = "";
        query = rnfr.equals("/") ? "/drives/" + this.drive_id + "/root/children" : "/drives/" + this.drive_id + "/root:" + this.handle_path_special_chars(rnfr, false);
        if (this.config.getProperty("onedrive_my_shares", "false").equals("true")) {
            query = "/drives/" + this.share_drive_id + "/items/" + this.share_item_id + ":" + this.handle_path_special_chars(rnfr, false);
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://graph.microsoft.com/v1.0" + query), this.config);
        urlc.setRemoveDoubleEncoding(true);
        urlc.setDoOutput(true);
        urlc.setDoInput(true);
        urlc.setRequestMethod("PATCH");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("Accept", "application/json");
        urlc.setRequestProperty("Content-Type", "application/json");
        JSONObject postData = new JSONObject();
        postData.put((Object)"name", (Object)Common.last(rnto));
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
    public boolean hasThumbnails(Properties item) {
        return item.containsKey("thumbnails");
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public boolean downloadThumbnail(Properties info) throws Exception {
        try {
            if (!info.containsKey("thumbnails")) return false;
            Vector list = (Vector)info.get("thumbnails");
            if (list.size() <= 0) return false;
            String url = "";
            Properties p = (Properties)list.get(0);
            String size = info.getProperty("preview_size");
            String dest = info.getProperty("preview_destination");
            int width = Integer.parseInt(size.substring(0, size.indexOf("x")));
            int height = Integer.parseInt(size.substring(size.indexOf("x") + 1));
            url = width >= 800 && height >= 800 && p.containsKey("large_thumbnail_url") ? p.getProperty("large_thumbnail_url") : (width >= 176 && height >= 176 && p.containsKey("medium_thumbnail_url") ? p.getProperty("medium_thumbnail_url") : p.getProperty("small_thumbnail_url"));
            if (info.containsKey(url)) {
                String path = info.getProperty(url);
                Common.copy(path, dest, true);
                new File_U(dest).setLastModified(Long.parseLong(info.getProperty("modified")));
                return true;
            }
            int x = 0;
            while (true) {
                block10: {
                    if (x >= 3) {
                        return true;
                    }
                    try {
                        URLConnection urlc = URLConnection.openConnection(new VRL(url), this.config);
                        urlc.setDoOutput(false);
                        urlc.setRequestMethod("GET");
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
                            break block10;
                        }
                        InputStream ins = urlc.getInputStream();
                        if (new File_U(dest).exists()) {
                            new File_U(dest).delete();
                        }
                        Common.streamCopier(null, null, ins, new FileOutputStream(new File_U(dest)), false, true, true);
                        new File_U(dest).setLastModified(Long.parseLong(info.getProperty("modified")));
                        info.put(url, dest);
                        return true;
                    }
                    catch (Exception e) {
                        Common.log("PREVIEW", 1, e);
                        if (x != 2) break block10;
                        throw e;
                    }
                }
                ++x;
            }
        }
        catch (Exception e) {
            Common.log("PREVIEW", 1, e);
        }
        return false;
    }

    private Properties get_access_tokens(String refresh_token, String oauth_client_id, String oauth_client_secret, String tenant) throws Exception {
        String full_form = "client_id=" + oauth_client_id;
        full_form = String.valueOf(full_form) + "&client_secret=" + oauth_client_secret;
        full_form = String.valueOf(full_form) + "&refresh_token=" + refresh_token;
        full_form = String.valueOf(full_form) + "&grant_type=refresh_token";
        URLConnection urlc = URLConnection.openConnection(new VRL("https://login.microsoftonline.com/" + tenant + "/oauth2/v2.0/token"), this.config);
        urlc.setDoOutput(true);
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        OutputStream out = urlc.getOutputStream();
        out.write(full_form.getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        String result = Common.consumeResponse(urlc.getInputStream());
        if (code < 200 || code > 299) {
            throw new Exception(result);
        }
        String access_token = (String)((JSONObject)JSONValue.parse((String)result)).get((Object)"access_token");
        String expires_in = "" + ((JSONObject)JSONValue.parse((String)result)).get((Object)"expires_in");
        Properties p = new Properties();
        p.put("refresh_token", refresh_token);
        p.put("access_token", access_token);
        if (expires_in.endsWith(",")) {
            expires_in = expires_in.substring(0, expires_in.length() - 1);
        }
        p.put("expires_in", expires_in);
        return p;
    }

    private String getBearer() throws Exception {
        if (this.config.containsKey("token_start") && this.config.containsKey("token_expire") && System.currentTimeMillis() - Long.parseLong(this.config.getProperty("token_start")) > (Long.parseLong(this.config.getProperty("token_expire")) - 600L) * 1000L) {
            Properties p = new Properties();
            p = this.config.getProperty("username").startsWith("app_permission~") ? Common.ms_client_credential_grant_token(this.config.getProperty("username").split("~")[1], this.config.getProperty("password"), this.config.getProperty("onedriveTenant", "common"), "https%3A%2F%2Fgraph.microsoft.com%2F.default") : Common.oauth_renew_tokens(this.config.getProperty("password"), this.config.getProperty("username").split("~")[0], Common.encryptDecrypt(this.config.getProperty("username").split("~")[1], false), "https://login.microsoftonline.com/" + this.config.getProperty("onedriveTenant", "common") + "/oauth2/v2.0/token");
            if (p.containsKey("access_token")) {
                this.bearer = p.getProperty("access_token");
                this.config.put("token_start", String.valueOf(System.currentTimeMillis()));
            } else if (p.containsKey("refresh_token")) {
                this.bearer = p.getProperty("refresh_token");
                this.config.put("token_start", String.valueOf(System.currentTimeMillis()));
            }
            if (p.containsKey("expires_in")) {
                this.config.put("token_expire", p.getProperty("expires_in"));
            }
        }
        return this.bearer;
    }

    protected String handle_path_special_chars(String path, boolean encode) {
        String handled_path = path;
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
        if (handled_path.contains("~")) {
            handled_path = handled_path.replace("~", encode ? "%7E" : "%257E");
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
}

