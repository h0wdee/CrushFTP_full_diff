/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.File_U;
import com.crushftp.client.GenericClient;
import com.crushftp.client.URLConnection;
import com.crushftp.client.VRL;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class GDriveClient
extends GenericClient {
    String bearer = "";
    static Properties resourceIdCache = new Properties();
    static Properties team_drives = new Properties();
    static Properties mkdir_status = new Properties();
    boolean is_jwt = false;

    public GDriveClient(String url, String header, Vector log) {
        super(header, log);
        this.fields = new String[]{"username", "password", "token_start", "team_drive", "gdrive_corpora"};
        System.setProperty("crushtunnel.debug", "2");
        if (!url.endsWith("/")) {
            url = String.valueOf(url) + "/";
        }
        this.url = url;
    }

    public static void main(String[] args) {
    }

    @Override
    public String login2(String username, String password, String clientid) throws Exception {
        password = VRL.vrlDecode(password);
        this.config.put("username", username);
        this.config.put("password", password);
        Properties p = null;
        if (username.equals("google_jwt") || username.startsWith("google_jwt")) {
            this.is_jwt = true;
            p = Common.oauth_renew_tokens(password, "~google_jwt~https://www.googleapis.com/auth/drive", "", "https://oauth2.googleapis.com/token");
        } else {
            p = Common.oauth_renew_tokens(password, username.split("~")[0], username.split("~")[1], "https://oauth2.googleapis.com/token");
        }
        this.log("Gdrive:  Login : Get barear response: " + p.keySet().toString());
        if (p.containsKey("access_token")) {
            this.bearer = p.getProperty("access_token");
            this.config.put("logged_out", "false");
            return "Success";
        }
        if (p.containsKey("refresh_token")) {
            this.bearer = p.getProperty("refresh_token");
            this.config.put("logged_out", "false");
            return "Success";
        }
        return "Failure!";
    }

    @Override
    public void logout() throws Exception {
        this.bearer = "";
        this.config.put("logged_out", "true");
        this.close();
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        if (!path.endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        if (path.equals("/")) {
            this.list2(path, list, "root");
            return list;
        }
        String resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + path);
        if (resourceId == null) {
            this.log("List : Could not found resource id in chache : " + path);
            this.loadResourceIds(path);
            resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + path);
        }
        this.list2(path, list, resourceId);
        return list;
    }

    private void list2(String path, Vector list, String resourceId) throws Exception {
        if (resourceId == null) {
            this.log("List : Could not found resource id for path : " + path);
        }
        SimpleDateFormat sdf_rfc1123_2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        String corpora = "";
        if (!path.equals("/") && this.config.getProperty("team_drive", "false").equals("true") && !this.config.getProperty("gdrive_corpora", "").equals("")) {
            if (this.config.getProperty("gdrive_corpora", "").equals("drive")) {
                if (team_drives.size() != 0 && team_drives.containsKey(Common.first(path.substring(1)))) {
                    corpora = "&corpora=drive&driveId=" + team_drives.getProperty(Common.first(path.substring(1)));
                } else {
                    this.getAllDrives("/", new Vector());
                    if (team_drives.containsKey(Common.first(path.substring(1)))) {
                        corpora = "&corpora=drive&driveId=" + team_drives.getProperty(Common.first(path.substring(1)));
                    }
                }
            } else {
                corpora = "&corpora=" + this.config.getProperty("gdrive_corpora", "");
            }
        }
        String next_page_token = "";
        int x = 0;
        while (x < 1000) {
            if (this.config.getProperty("logged_out", "false").equals("true")) {
                throw new Exception("Error: Cancel dir listing. The client is already closed.");
            }
            if (next_page_token.equals("") && x > 0) break;
            String page_token = "";
            if (!next_page_token.equals("")) {
                page_token = "&pageToken=" + next_page_token;
            }
            URLConnection urlc = URLConnection.openConnection(new VRL("https://www.googleapis.com/drive/v3/files?q='" + resourceId + "'+in+parents and trashed=false&fields=kind,nextPageToken,incompleteSearch,files/id,files/name,files/size,files/mimeType,files/trashed,files/modifiedTime,files/parents,files/hasThumbnail,files/thumbnailLink&pageSize=1000&includeItemsFromAllDrives=true&supportsAllDrives=true" + page_token + corpora), this.config);
            urlc.setDoOutput(true);
            urlc.setRequestMethod("GET");
            urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
            int code = urlc.getResponseCode();
            String json = Common.consumeResponse(urlc.getInputStream());
            this.log("HTTP_CLIENT", 2, "Path = " + path + " List result = " + json);
            next_page_token = ((JSONObject)JSONValue.parse(json)).containsKey("nextPageToken") ? ((JSONObject)JSONValue.parse(json)).get("nextPageToken").toString() : "";
            Object obj = ((JSONObject)JSONValue.parse(json)).get("files");
            if (obj instanceof JSONArray) {
                JSONArray ja = (JSONArray)obj;
                int xxx = 0;
                while (xxx < ja.size()) {
                    Object obj2 = ja.get(xxx);
                    if (obj2 instanceof JSONObject) {
                        Properties item = new Properties();
                        JSONObject jo = (JSONObject)obj2;
                        boolean folder = false;
                        if (jo.get("mimeType").equals("application/vnd.google-apps.folder")) {
                            folder = true;
                        }
                        Object[] a = jo.entrySet().toArray();
                        int i = 0;
                        while (i < a.length) {
                            String key2 = a[i].toString().split("=")[0];
                            item.put(key2.trim(), ("" + jo.get(key2)).trim());
                            ++i;
                        }
                        if (!item.getProperty("trashed", "").equals("true")) {
                            Date d = sdf_rfc1123_2.parse(item.getProperty("modifiedTime"));
                            if (folder || item.getProperty("size") != null) {
                                String line = String.valueOf(folder ? "d" : "-") + "rwxrwxrwx   1    owner   group   " + item.getProperty("size") + "   " + yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + item.getProperty("name");
                                Properties stat = GDriveClient.parseStat(line);
                                if (item.getProperty("hasThumbnail", "").equals("true")) {
                                    stat.put("thumbnailLink", item.getProperty("thumbnailLink", ""));
                                }
                                stat.put("resource_id", item.getProperty("id"));
                                stat.put("url", "gdrive://" + this.config.getProperty("username") + ":" + VRL.vrlEncode(this.config.getProperty("password")) + "@www.googleapis.com" + path + stat.getProperty("name"));
                                resourceIdCache.put(String.valueOf(this.config.getProperty("password")) + path + stat.getProperty("name"), item.getProperty("id"));
                                if (!folder) {
                                    stat.put("gdrive_mime_type", item.getProperty("mimeType", ""));
                                }
                                if (stat.getProperty("type", "").equalsIgnoreCase("DIR")) {
                                    resourceIdCache.put(String.valueOf(this.config.getProperty("password")) + path + stat.getProperty("name") + "/", item.getProperty("id"));
                                }
                                list.addElement(stat);
                            }
                        }
                    }
                    ++xxx;
                }
            }
            ++x;
        }
        if (path.equals("/") && this.config.getProperty("team_drive", "false").equals("true")) {
            this.getAllDrives(path, list);
        }
    }

    private void getAllDrives(String path, Vector list) throws IOException, Exception, SocketTimeoutException {
        String json2 = this.getTeamDrives(path, list, "");
        String nextPageToken = (String)((JSONObject)JSONValue.parse(json2)).get("nextPageToken");
        if (nextPageToken != null) {
            int x = 0;
            while (x < 7) {
                this.log("List : Team Drives - Next page token : " + nextPageToken + " index = " + x);
                String json_next = this.getTeamDrives(path, list, nextPageToken);
                nextPageToken = (String)((JSONObject)JSONValue.parse(json_next)).get("nextPageToken");
                if (nextPageToken == null) break;
                ++x;
            }
        }
    }

    private String getTeamDrives(String path, Vector list, String pageToken) throws IOException, Exception, SocketTimeoutException {
        String pageTokenParameter = "&pageToken=" + pageToken;
        if (pageToken.equals("")) {
            pageTokenParameter = "";
        }
        URLConnection urlc2 = URLConnection.openConnection(new VRL("https://www.googleapis.com/drive/v3/drives?pageSize=100" + pageTokenParameter), this.config);
        urlc2.setDoOutput(true);
        urlc2.setRequestMethod("GET");
        urlc2.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc2.getResponseCode();
        String json2 = Common.consumeResponse(urlc2.getInputStream());
        Object obj2 = ((JSONObject)JSONValue.parse(json2)).get("drives");
        if (obj2 instanceof JSONArray) {
            JSONArray ja = (JSONArray)obj2;
            SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
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
                    Date d = new Date();
                    String line = "drwxrwxrwx   1    owner   group   0   " + yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + item.getProperty("name");
                    Properties stat = GDriveClient.parseStat(line);
                    stat.put("resource_id", item.getProperty("id"));
                    stat.put("url", "gdrive://" + this.config.getProperty("username") + ":" + VRL.vrlEncode(this.config.getProperty("password")) + "@www.googleapis.com" + path + stat.getProperty("name"));
                    team_drives.put(stat.getProperty("name"), item.getProperty("id"));
                    resourceIdCache.put(String.valueOf(this.config.getProperty("password")) + path + stat.getProperty("name"), item.getProperty("id"));
                    resourceIdCache.put(String.valueOf(this.config.getProperty("password")) + path + stat.getProperty("name") + "/", item.getProperty("id"));
                    list.addElement(stat);
                }
                ++xxx;
            }
        }
        return json2;
    }

    @Override
    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        int code;
        String resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + path);
        if (resourceId == null) {
            this.stat(path);
            resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + path);
        }
        if (resourceId == null) {
            this.log("Download path not found:" + path + "\r\n");
            throw new IOException("Download path not found:" + path);
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://www.googleapis.com/drive/v3/files/" + resourceId + "?alt=media&supportsAllDrives=true"), this.config);
        urlc.setDoOutput(false);
        this.doStandardDocsAlterations(urlc, null);
        if (startPos > 0L || endPos >= 0L) {
            urlc.setRequestProperty("Range", "bytes=" + startPos + "-" + (endPos >= 0L ? String.valueOf(endPos) : ""));
        }
        if ((code = urlc.getResponseCode()) < 200 || code > 299) {
            String result = URLConnection.consumeResponse(urlc.getInputStream());
            urlc.disconnect();
            if (code == 403 && result.contains("Only files with binary content can be downloaded. Use Export with Docs Editors files.")) {
                String ext;
                String mime_type = "";
                mime_type = Common.last(path).contains(".") ? (!Common.mimes.getProperty(ext = Common.last(path).substring(Common.last(path).lastIndexOf(".")).toUpperCase(), "").equals("") ? Common.mimes.getProperty(ext, "") : this.getRelatedMimeType(path)) : this.getRelatedMimeType(path);
                urlc = URLConnection.openConnection(new VRL("https://www.googleapis.com/drive/v3/files/" + resourceId + "/export?mimeType=" + mime_type + "&supportsAllDrives=true"), this.config);
                urlc.setDoOutput(false);
                this.doStandardDocsAlterations(urlc, null);
                int code2 = urlc.getResponseCode();
                if (code2 < 200 || code2 > 299) {
                    result = URLConnection.consumeResponse(urlc.getInputStream());
                    urlc.disconnect();
                    this.log(String.valueOf(result) + "\r\n");
                    throw new Exception(result);
                }
            } else {
                this.log(String.valueOf(result) + "\r\n");
                throw new Exception(result);
            }
        }
        this.in = urlc.getInputStream();
        return this.in;
    }

    private String getRelatedMimeType(String path) throws Exception {
        Properties p = this.stat(path);
        String gdrive_mime_type = p.getProperty("gdrive_mime_type", "");
        if (gdrive_mime_type.equals("application/vnd.google-apps.document")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        if (gdrive_mime_type.equals("application/vnd.google-apps.presentation")) {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        }
        if (gdrive_mime_type.equals("application/vnd.google-apps.spreadsheet")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
        if (gdrive_mime_type.equals("application/vnd.google-apps.drawings")) {
            return "image/jpeg";
        }
        if (gdrive_mime_type.equals("application/vnd.google-apps.form")) {
            return "application/pdf";
        }
        if (gdrive_mime_type.equals("application/vnd.google-apps.photo")) {
            return "image/png";
        }
        if (gdrive_mime_type.equals("application/vnd.google-apps.script")) {
            return "application/vnd.google-apps.script+json";
        }
        throw new Exception("Unsupported mime type! Type: " + gdrive_mime_type);
    }

    @Override
    protected OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        String resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + Common.all_but_last(path));
        if (Common.all_but_last(path).equals("/")) {
            resourceId = "root";
        }
        if (resourceId == null) {
            this.stat(Common.all_but_last(path));
            resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + Common.all_but_last(path));
        }
        if (resourceId == null) {
            this.log("Upload path not found:" + Common.all_but_last(path) + "\r\n");
            throw new IOException("Upload path not found:" + Common.all_but_last(path));
        }
        try {
            this.delete(path);
        }
        catch (Exception exception) {
            // empty catch block
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable&supportsAllDrives=true"), this.config);
        urlc.setRequestMethod("POST");
        this.doStandardDocsAlterations(urlc, "application/json; charset=UTF-8");
        urlc.setDoOutput(true);
        JSONObject fileMetaInfo = new JSONObject();
        fileMetaInfo.put("name", Common.last(path));
        String[] folders = path.split("/");
        if (folders.length > 2) {
            JSONArray parents = new JSONArray();
            parents.add(resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + Common.all_but_last(path)));
            fileMetaInfo.put("parents", parents);
        }
        urlc.setRequestProperty("X-Upload-Content-Type", "application/octet-stream");
        OutputStream out = urlc.getOutputStream();
        out.write(fileMetaInfo.toString().getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        urlc.disconnect();
        if (code < 200 || code > 299) {
            this.log(String.valueOf(result) + "\r\n");
            throw new IOException(result);
        }
        String postLocation = urlc.getHeaderField("Location");
        class OutputWrapper
        extends OutputStream {
            boolean closed = false;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(0x280000);
            long pos = 0L;
            long pos2 = 0L;
            String final_size = "*";
            private final /* synthetic */ String val$path;
            private final /* synthetic */ String val$postLocation;

            OutputWrapper(String string, String string2) {
                this.val$path = string;
                this.val$postLocation = string2;
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
                if (this.baos.size() + len > 0x280000) {
                    int chunks = (this.baos.size() + len) / 0x280000;
                    int diff = this.baos.size() + len - chunks * 0x280000;
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

            @Override
            public void close() throws IOException {
                if (this.closed) {
                    return;
                }
                this.pos2 += (long)this.baos.size();
                this.final_size = String.valueOf(this.pos2);
                Properties p = GDriveClient.parse_json_reply(this.flushNow());
                if (p.getProperty("id") != null && !GDriveClient.this.config.getProperty("team_drive", "false").equals("true")) {
                    resourceIdCache.put(String.valueOf(GDriveClient.this.config.getProperty("password")) + this.val$path, p.getProperty("id"));
                }
                this.closed = true;
            }

            public String flushNow() throws IOException {
                String result = "";
                if (this.baos.size() > 0) {
                    try {
                        URLConnection urlc2 = URLConnection.openConnection(new VRL(this.val$postLocation), GDriveClient.this.config);
                        urlc2.setRequestMethod("PUT");
                        urlc2.setDoOutput(true);
                        urlc2.setRequestProperty("Content-Range", "bytes " + this.pos + "-" + (this.pos2 - 1L) + "/" + this.final_size);
                        GDriveClient.this.doStandardDocsAlterations(urlc2, "application/octet-stream");
                        urlc2.getOutputStream().write(this.baos.toByteArray());
                        urlc2.getResponseCode();
                        result = Common.consumeResponse(urlc2.getInputStream());
                        result = result.equals("") ? "Uploaded bytes: " + this.pos + "-" + (this.pos2 - 1L) + "/" + this.final_size : "Byte range : " + this.pos + "-" + (this.pos2 - 1L) + "/" + this.final_size + "Result : " + result;
                        GDriveClient.this.log(result);
                        urlc2.disconnect();
                    }
                    catch (Exception e) {
                        GDriveClient.this.log(e);
                        throw new IOException("" + e);
                    }
                }
                this.baos.reset();
                return result;
            }
        }
        out = new OutputWrapper(path, postLocation);
        return out;
    }

    @Override
    public boolean delete(String path) throws Exception {
        String resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + path);
        if (resourceId == null) {
            this.log("resource id not found:" + path + "\r\n");
            if (this.stat(path) != null) {
                resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + path);
            }
            if (resourceId == null) {
                this.log("Delete path not found:" + path + "\r\n");
                return true;
            }
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://www.googleapis.com/drive/v3/files/" + resourceId + "?supportsAllDrives=true"), this.config);
        urlc.setDoOutput(false);
        urlc.setRequestMethod("DELETE");
        this.doStandardDocsAlterations(urlc, null);
        int code = urlc.getResponseCode();
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        urlc.disconnect();
        if (code < 200 || code > 299) {
            this.log(String.valueOf(result) + "\r\n");
            return false;
        }
        String path2 = path;
        path2 = path.endsWith("/") ? path.substring(0, path.length() - 1) : String.valueOf(path2) + "/";
        if (resourceIdCache.containsKey(String.valueOf(this.config.getProperty("password")) + path)) {
            resourceIdCache.remove(String.valueOf(this.config.getProperty("password")) + path);
        }
        if (resourceIdCache.containsKey(String.valueOf(this.config.getProperty("password")) + path2)) {
            resourceIdCache.remove(String.valueOf(this.config.getProperty("password")) + path2);
        }
        return true;
    }

    @Override
    public boolean makedir(String path) throws Exception {
        this.log("Attempt to create directory on gdrive : " + path);
        URLConnection urlc = URLConnection.openConnection(new VRL("https://www.googleapis.com/drive/v3/files?supportsAllDrives=true"), this.config);
        urlc.setDoOutput(true);
        urlc.setRequestMethod("POST");
        this.doStandardDocsAlterations(urlc, "application/json; charset=UTF-8");
        JSONObject fileMetaInfo = new JSONObject();
        String[] folders = path.split("/");
        fileMetaInfo.put("name", folders[folders.length - 1]);
        fileMetaInfo.put("mimeType", "application/vnd.google-apps.folder");
        if (folders.length > 2) {
            JSONArray parents = new JSONArray();
            parents.add(resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + Common.all_but_last(path)));
            fileMetaInfo.put("parents", parents);
        }
        OutputStream out = urlc.getOutputStream();
        out.write(fileMetaInfo.toString().getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        urlc.disconnect();
        if (code < 200 || code > 299) {
            this.log(String.valueOf(result) + "\r\n");
            return false;
        }
        String path2 = path;
        path2 = path.endsWith("/") ? path.substring(0, path.length() - 1) : String.valueOf(path2) + "/";
        if (resourceIdCache.containsKey(String.valueOf(this.config.getProperty("password")) + path)) {
            resourceIdCache.remove(String.valueOf(this.config.getProperty("password")) + path);
        }
        if (resourceIdCache.containsKey(String.valueOf(this.config.getProperty("password")) + path2)) {
            resourceIdCache.remove(String.valueOf(this.config.getProperty("password")) + path2);
        }
        boolean was_created = false;
        int x = 1;
        while (x <= 6) {
            if (this.check_exists(path)) {
                was_created = true;
                break;
            }
            Thread.sleep(x * 300);
            ++x;
        }
        if (!was_created) {
            this.log("Folder was not created! Path : " + path + " Folder creation response : " + code + " " + urlc.getResponseMessage() + " " + result);
            return false;
        }
        return true;
    }

    private boolean check_exists(String path) throws Exception {
        boolean exists = false;
        String parent_path = path;
        if (parent_path.endsWith("/")) {
            parent_path = parent_path.substring(0, parent_path.length() - 1);
        }
        String name = Common.last(parent_path);
        parent_path = Common.all_but_last(parent_path);
        String parent_path_id = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + parent_path);
        if (parent_path.equals("/")) {
            parent_path_id = "root";
        }
        Vector list = new Vector();
        this.list2(parent_path, list, parent_path_id);
        int x = 0;
        while (x < list.size()) {
            Properties p = (Properties)list.elementAt(x);
            if (p.getProperty("name").equals(name)) {
                exists = true;
                break;
            }
            ++x;
        }
        return exists;
    }

    @Override
    public boolean makedirs(String path) throws Exception {
        boolean ok = true;
        String[] parts = path.split("/");
        String path2 = "";
        int x = 0;
        while (x < parts.length && ok) {
            path2 = String.valueOf(path2) + parts[x] + "/";
            if (x >= 1) {
                String resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + path2);
                if (resourceId == null) {
                    this.stat(path2);
                    resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + path2);
                }
                if (resourceId == null) {
                    if (mkdir_status.containsKey(String.valueOf(this.config.getProperty("password")) + path2)) {
                        int xx = 0;
                        while (xx < 30) {
                            Thread.sleep(200L);
                            if (!mkdir_status.containsKey(String.valueOf(this.config.getProperty("password")) + path2)) break;
                            ++xx;
                        }
                        resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + path2);
                    }
                    if (resourceId == null) {
                        mkdir_status.put(String.valueOf(this.config.getProperty("password")) + path2, "Create Directory.");
                        try {
                            ok = this.makedir(path2);
                        }
                        finally {
                            mkdir_status.remove(String.valueOf(this.config.getProperty("password")) + path2);
                        }
                    }
                }
            }
            ++x;
        }
        return ok;
    }

    @Override
    public boolean rename(String rnfr, String rnto, boolean overwrite) throws Exception {
        String resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + rnfr);
        if (resourceId == null) {
            this.stat(rnfr);
            resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + rnfr);
        }
        if (resourceId == null) {
            this.log("Delete path not found:" + rnfr + "\r\n");
            return true;
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://www.googleapis.com/drive/v3/files/" + resourceId + "?supportsAllDrives=true"), this.config);
        urlc.setDoOutput(true);
        urlc.setRequestMethod("PATCH");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        JSONObject fileMetaInfo = new JSONObject();
        fileMetaInfo.put("name", Common.last(rnto));
        OutputStream out = urlc.getOutputStream();
        out.write(fileMetaInfo.toString().getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        urlc.disconnect();
        if (code < 200 || code > 299) {
            this.log(String.valueOf(result) + "\r\n");
            return false;
        }
        String rnfr2 = rnfr;
        rnfr2 = rnfr.endsWith("/") ? rnfr.substring(0, rnfr.length() - 1) : String.valueOf(rnfr2) + "/";
        if (resourceIdCache.containsKey(String.valueOf(this.config.getProperty("password")) + rnfr)) {
            resourceIdCache.remove(String.valueOf(this.config.getProperty("password")) + rnfr);
        }
        if (resourceIdCache.containsKey(String.valueOf(this.config.getProperty("password")) + rnfr2)) {
            resourceIdCache.remove(String.valueOf(this.config.getProperty("password")) + rnfr2);
        }
        String rnto2 = rnto;
        if (resourceIdCache.containsKey(String.valueOf(this.config.getProperty("password")) + rnto)) {
            resourceIdCache.remove(String.valueOf(this.config.getProperty("password")) + rnto);
        }
        if (resourceIdCache.containsKey(String.valueOf(this.config.getProperty("password")) + rnto2)) {
            resourceIdCache.remove(String.valueOf(this.config.getProperty("password")) + rnto2);
        }
        rnto2 = rnto.endsWith("/") ? rnto.substring(0, rnto.length() - 1) : String.valueOf(rnto2) + "/";
        boolean was_created = false;
        int x = 1;
        while (x < 6) {
            if (this.check_exists(rnto)) {
                was_created = true;
                break;
            }
            Thread.sleep(x * 300);
            ++x;
        }
        if (!was_created) {
            throw new Exception("The path was not created : " + rnto);
        }
        return true;
    }

    @Override
    public Properties stat(String path) throws Exception {
        if (path.endsWith(":filetree")) {
            path = path.substring(0, path.indexOf(":filetree") - 1);
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
    public boolean mdtm(String path, long modified) throws Exception {
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

    public static Properties setup_bearer(String oauth_access_code, String server_url, String google_client_id, String google_client_secret) throws Exception {
        return Common.google_get_refresh_token(oauth_access_code, server_url, google_client_id, google_client_secret);
    }

    public static Properties parse_json_reply(String result) throws IOException {
        BufferedReader br = new BufferedReader(new StringReader(result));
        String line = "";
        Properties p = new Properties();
        try {
            while ((line = br.readLine()) != null) {
                String val;
                if (line.indexOf(":") < 0) continue;
                String key = line.split(":")[0].trim();
                if (key.indexOf("\"") >= 0) {
                    key = key.substring(1, key.lastIndexOf("\""));
                }
                if ((val = line.split(":")[1].trim()).indexOf("\"") >= 0) {
                    val = val.substring(1, val.lastIndexOf("\""));
                }
                p.put(key, val);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return p;
    }

    private void loadResourceIds(String path) {
        this.log("List : Searching for resource id. The full path : " + path);
        String[] parts = null;
        parts = path.equals("/") ? new String[]{""} : path.split("/");
        String path2 = "";
        int x = 0;
        while (x < parts.length) {
            path2 = String.valueOf(path2) + parts[x] + "/";
            String resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + path2);
            if (path2.equals("/")) {
                resourceId = "root";
            }
            try {
                this.list2(path2, new Vector(), resourceId);
                resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + path2);
                this.log("List : Searching for resource id. The path : " + path2 + " Resource id :" + resourceId);
            }
            catch (Exception e) {
                this.log("Load Resource Ids : " + e);
            }
            ++x;
        }
    }

    private String getBearer() throws Exception {
        Properties p = null;
        p = this.is_jwt ? Common.oauth_renew_tokens(this.config.getProperty("password"), "~google_jwt~https://www.googleapis.com/auth/drive", "", "https://oauth2.googleapis.com/token") : Common.oauth_renew_tokens(this.config.getProperty("password"), this.config.getProperty("username").split("~")[0], this.config.getProperty("username").split("~")[1], "https://oauth2.googleapis.com/token");
        if (p.containsKey("access_token")) {
            this.bearer = p.getProperty("access_token");
            this.config.put("token_start", String.valueOf(System.currentTimeMillis()));
        } else if (p.containsKey("refresh_token")) {
            this.bearer = p.getProperty("refresh_token");
            this.config.put("token_start", String.valueOf(System.currentTimeMillis()));
        }
        return this.bearer;
    }

    @Override
    public boolean hasThumbnails(Properties item) {
        return item.containsKey("thumbnailLink");
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public boolean downloadThumbnail(Properties info) throws Exception {
        try {
            String resourceId = info.getProperty("resource_id", "");
            String dest = info.getProperty("preview_destination");
            if (info.containsKey(info.getProperty("thumbnailLink"))) {
                String path = info.getProperty(this.url);
                Common.copy(path, dest, true);
                new File_U(dest).setLastModified(Long.parseLong(info.getProperty("modified")));
                return false;
            }
            int x = 0;
            while (true) {
                block10: {
                    if (x >= 3) {
                        return true;
                    }
                    try {
                        URLConnection urlc = URLConnection.openConnection(new VRL(info.getProperty("thumbnailLink")), this.config);
                        urlc.setDoOutput(false);
                        this.doStandardDocsAlterations(urlc, null);
                        int code = urlc.getResponseCode();
                        String result = "";
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
                        info.put(this.url, dest);
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
}

