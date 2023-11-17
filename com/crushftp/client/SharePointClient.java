/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.GenericClient;
import com.crushftp.client.URLConnection;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;
import java.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class SharePointClient
extends GenericClient {
    String bearer = "";
    String bearer_realm = "";
    String app_id = "";

    public SharePointClient(String url, String header, Vector log) {
        super(header, log);
        this.fields = new String[]{"username", "password", "token_start", "token_expire", "sharepoint_site_id", "sharepoint_site_path", "sharepoint_site_drive_name"};
        this.url = url;
    }

    @Override
    public String login2(String username, String password, String clientid) throws Exception {
        username = VRL.vrlDecode(Common.url_decode(username));
        password = VRL.vrlDecode(Common.url_decode(password));
        this.config.put("username", username);
        this.config.put("password", password);
        Properties p = this.getAccessToken(this.config.getProperty("sharepoint_site_id", "").trim(), this.config.getProperty("sharepoint_site_path", "").trim(), this.config.getProperty("username", "").trim(), this.config.getProperty("password", "").trim());
        if (p.containsKey("expires_in")) {
            this.config.put("token_start", String.valueOf(System.currentTimeMillis()));
            this.config.put("token_expire", p.getProperty("expires_in"));
        }
        if (!p.containsKey("access_token")) {
            throw new Exception("Error : Could not get access token!");
        }
        this.bearer = p.getProperty("access_token");
        if (this.config.getProperty("sharepoint_site_drive_name", "").trim().contains(" ")) {
            this.config.put("sharepoint_site_drive_name", this.config.getProperty("sharepoint_site_drive_name", "").trim().replaceAll(" ", "%20"));
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("sharepoint_site_id", "") + this.config.getProperty("sharepoint_site_path", "") + "_api/web/GetFolderByServerRelativeUrl(@v)?@v=" + this.getRelativePath("")), this.config);
        urlc.setRequestMethod("GET");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("Accept", "application/json;odata=verbose");
        int code = urlc.getResponseCode();
        String result = Common.consumeResponse(urlc.getInputStream());
        if (code < 200 || code > 299) {
            throw new Exception(this.getErrorInfo(result, code));
        }
        return "Success!";
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        String temp_path = path;
        if (path.equals("/")) {
            temp_path = "";
        }
        if (temp_path.endsWith("/")) {
            temp_path = temp_path.substring(0, temp_path.length() - 1);
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("sharepoint_site_id", "") + this.config.getProperty("sharepoint_site_path", "") + "_api/web/GetFolderByServerRelativeUrl(@v)?@v=" + this.getRelativePath(temp_path) + "&$expand=Folders,Files"), this.config);
        urlc.setRequestMethod("GET");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("Accept", "application/json;odata=verbose");
        urlc.setRemoveDoubleEncoding(true);
        int code = urlc.getResponseCode();
        String result = Common.consumeResponse(urlc.getInputStream());
        if (code < 200 || code > 299) {
            this.log(result);
            throw new Exception(this.getErrorInfo(result, code));
        }
        this.log("HTTP_CLIENT", 2, "Sharepoint REST API Client: List path : " + path + " Result : " + result);
        JSONObject jo = (JSONObject)((JSONObject)JSONValue.parse(result)).get("d");
        JSONArray folders = (JSONArray)((JSONObject)jo.get("Folders")).get("results");
        JSONArray files = (JSONArray)((JSONObject)jo.get("Files")).get("results");
        this.parseItemArray(path, list, folders, true);
        this.parseItemArray(path, list, files, false);
        return list;
    }

    private void parseItemArray(String path, Vector list, JSONArray ja, boolean folder) throws Exception {
        int x = 0;
        while (x < ja.size()) {
            Object obj2 = ja.get(x);
            if (obj2 instanceof JSONObject) {
                JSONObject jop = (JSONObject)obj2;
                Date d = new Date();
                SimpleDateFormat sdf_rfc1123_2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                SimpleDateFormat sdf_rfc1123_3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                try {
                    d = ((String)jop.get("TimeLastModified")).contains(".") ? sdf_rfc1123_2.parse((String)jop.get("TimeLastModified")) : sdf_rfc1123_3.parse((String)jop.get("TimeLastModified"));
                }
                catch (Exception e) {
                    this.log(e);
                }
                SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                String name = (String)jop.get("Name");
                String size = "0";
                if (!folder) {
                    size = (String)jop.get("Length");
                }
                String line = String.valueOf(folder ? "d" : "-") + "rwxrwxrwx   1    owner   group   " + size + "   " + yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + name;
                Properties stat = SharePointClient.parseStat(line);
                stat.put("url", "sharepoint2://" + VRL.vrlEncode((String)this.getConfig("username")) + ":" + VRL.vrlEncode((String)this.getConfig("password")) + "@graph.microsoft.com" + path + stat.getProperty("name"));
                list.addElement(stat);
            }
            ++x;
        }
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
    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        URLConnection urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("sharepoint_site_id", "") + this.config.getProperty("sharepoint_site_path", "") + "_api/web/GetFileByServerRelativeUrl(@v)/$value?@v=" + this.getRelativePath(path)), this.config);
        urlc.setRequestMethod("GET");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        return urlc.getInputStream();
    }

    @Override
    protected OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        String temp_path = Common.all_but_last(path);
        if (path.equals("/")) {
            temp_path = "";
        }
        if (temp_path.endsWith("/")) {
            temp_path = temp_path.substring(0, temp_path.length() - 1);
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("sharepoint_site_id", "") + this.config.getProperty("sharepoint_site_path", "") + "_api/web/GetFolderByServerRelativeUrl(@v)/Files/Add(url='" + Common.last(path) + "', overwrite=true)?@v=" + this.getRelativePath(temp_path)), this.config);
        urlc.setRequestMethod("POST");
        urlc.setDoOutput(true);
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("Accept", "application/json;odata=nometadata");
        urlc.setRequestProperty("X-RequestDigest", "digest");
        urlc.setLength(0L);
        OutputStream os = urlc.getOutputStream();
        Object in = null;
        if (os != null) {
            os.close();
        }
        int code = urlc.getResponseCode();
        String result = Common.consumeResponse(urlc.getInputStream());
        if (code < 200 || code > 299) {
            this.log(result);
            throw new IOException(this.getErrorInfo(result, code));
        }
        class OutputWrapper
        extends OutputStream {
            boolean closed = false;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(0xA00000);
            boolean startupload = true;
            String id = UUID.randomUUID().toString();
            long pos = 0L;
            final Properties status = new Properties();
            private final /* synthetic */ String val$path;

            OutputWrapper(String string) {
                this.val$path = string;
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
                    this.flushNow();
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
                                SharePointClient.this.log(e);
                            }
                        }
                        if (this.status.getProperty("status", "").startsWith("Error:") || loops >= 1998) {
                            try {
                                this.cancelUpload();
                            }
                            catch (Exception ec) {
                                SharePointClient.this.log(ec);
                            }
                            try {
                                SharePointClient.this.delete(this.val$path);
                            }
                            catch (Exception e) {
                                SharePointClient.this.log(e);
                            }
                            if (this.status.getProperty("status", "").startsWith("Error:")) {
                                throw new IOException(this.status.getProperty("status", ""));
                            }
                            throw new IOException("100 second timeout while waiting for prior chunk to complete..." + loops + ":" + this.pos);
                        }
                    }
                    final long offset = this.pos;
                    final byte[] b_flush = this.baos.toByteArray();
                    final boolean start = this.startupload;
                    this.status.put("status", "");
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            try {
                                String type = "continueupload";
                                String offset_parameter = ",fileOffset=" + offset;
                                if (start) {
                                    type = "startupload";
                                    offset_parameter = "";
                                }
                                String error_message = "";
                                int x = 0;
                                while (x <= 5) {
                                    URLConnection urlc = URLConnection.openConnection(new VRL("https://" + ((OutputWrapper)this).SharePointClient.this.config.getProperty("sharepoint_site_id", "") + ((OutputWrapper)this).SharePointClient.this.config.getProperty("sharepoint_site_path", "") + "_api/web/GetFileByServerRelativeUrl(@v)/" + type + "(uploadId=guid'" + id + "'" + offset_parameter + ")?@v=" + SharePointClient.this.getRelativePath(val$path)), ((OutputWrapper)this).SharePointClient.this.config);
                                    urlc.setRequestMethod("POST");
                                    urlc.setDoOutput(true);
                                    urlc.setRequestProperty("Authorization", "Bearer " + SharePointClient.this.getBearer());
                                    urlc.setRequestProperty("X-RequestDigest", "digest");
                                    urlc.setRequestProperty("Content-Type", "application/octet-stream");
                                    OutputStream os = urlc.getOutputStream();
                                    os.write(b_flush);
                                    os.close();
                                    int code = urlc.getResponseCode();
                                    if (code >= 200 && code <= 299) break;
                                    String result = Common.consumeResponse(urlc.getInputStream());
                                    SharePointClient.this.log(result);
                                    error_message = result;
                                    ++x;
                                }
                                if (error_message.equals("")) {
                                    status.put("status", "Success!");
                                } else {
                                    status.put("status", "Error: " + error_message);
                                }
                            }
                            catch (Exception e) {
                                SharePointClient.this.log(e);
                                status.put("status", "Error: " + e.getMessage());
                            }
                        }
                    });
                }
                this.pos += (long)this.baos.size();
                this.baos.reset();
                this.startupload = false;
            }

            public void cancelUpload() throws Exception {
                URLConnection urlc = URLConnection.openConnection(new VRL("https://" + SharePointClient.this.config.getProperty("sharepoint_site_id", "") + SharePointClient.this.config.getProperty("sharepoint_site_path", "") + "_api/web/GetFileByServerRelativeUrl(@v)/cancelupload(uploadId=guid'" + this.id + "')?@v=" + SharePointClient.this.getRelativePath(this.val$path)), SharePointClient.this.config);
                urlc.setRequestMethod("POST");
                urlc.setDoOutput(false);
                urlc.setRequestProperty("Authorization", "Bearer " + SharePointClient.this.getBearer());
                urlc.setRequestProperty("X-RequestDigest", "digest");
                urlc.setLength(0L);
                int code = urlc.getResponseCode();
                if (code < 200 || code > 299) {
                    String result = Common.consumeResponse(urlc.getInputStream());
                    SharePointClient.this.log(result);
                    throw new Exception(SharePointClient.this.getErrorInfo(result, code));
                }
            }

            @Override
            public void close() throws IOException {
                block27: {
                    block28: {
                        if (this.closed) {
                            return;
                        }
                        if (this.startupload) {
                            try {
                                URLConnection urlc = URLConnection.openConnection(new VRL("https://" + SharePointClient.this.config.getProperty("sharepoint_site_id", "") + SharePointClient.this.config.getProperty("sharepoint_site_path", "") + "_api/web/GetFileByServerRelativeUrl(@v)/savebinarystream?@v=" + SharePointClient.this.getRelativePath(this.val$path)), SharePointClient.this.config);
                                urlc.setRequestMethod("POST");
                                urlc.setDoOutput(true);
                                urlc.setRequestProperty("Authorization", "Bearer " + SharePointClient.this.getBearer());
                                urlc.setRequestProperty("X-RequestDigest", "digest");
                                urlc.setRequestProperty("Content-Type", "application/octet-stream");
                                OutputStream os = urlc.getOutputStream();
                                if (this.baos.size() > 0) {
                                    os.write(this.baos.toByteArray());
                                } else {
                                    urlc.setLength(0L);
                                }
                                os.close();
                                int code = urlc.getResponseCode();
                                if (code < 200 || code > 299) {
                                    String result = Common.consumeResponse(urlc.getInputStream());
                                    SharePointClient.this.log(result);
                                    throw new Exception(SharePointClient.this.getErrorInfo(result, code));
                                }
                                break block27;
                            }
                            catch (Exception e) {
                                SharePointClient.this.log(e);
                                try {
                                    SharePointClient.this.delete(this.val$path);
                                }
                                catch (Exception ed) {
                                    SharePointClient.this.log(ed);
                                }
                                this.closed = true;
                                throw new IOException(e);
                            }
                        }
                        if (this.status.containsKey("status")) {
                            try {
                                int loops = 0;
                                while (this.status.getProperty("status", "").equals("") && loops++ < 1000) {
                                    Thread.sleep(100L);
                                }
                                if (loops >= 998) {
                                    throw new IOException("100 second timeout while waiting for prior sharepoint chunk to complete..." + loops + ":" + this.pos + ":" + this.pos);
                                }
                                if (!this.status.getProperty("status", "").startsWith("Error:")) break block28;
                                try {
                                    this.cancelUpload();
                                }
                                catch (Exception ec) {
                                    SharePointClient.this.log(ec);
                                }
                                try {
                                    SharePointClient.this.delete(this.val$path);
                                }
                                catch (Exception e) {
                                    SharePointClient.this.log(e);
                                }
                                throw new IOException(this.status.getProperty("status", ""));
                            }
                            catch (InterruptedException e) {
                                SharePointClient.this.log(e);
                            }
                        }
                    }
                    try {
                        URLConnection urlc = URLConnection.openConnection(new VRL("https://" + SharePointClient.this.config.getProperty("sharepoint_site_id", "") + SharePointClient.this.config.getProperty("sharepoint_site_path", "") + "_api/web/GetFileByServerRelativeUrl(@v)/finishupload(uploadId=guid'" + this.id + "',fileOffset=" + this.pos + ")?@v=" + SharePointClient.this.getRelativePath(this.val$path)), SharePointClient.this.config);
                        urlc.setRequestMethod("POST");
                        urlc.setDoOutput(true);
                        urlc.setRequestProperty("Authorization", "Bearer " + SharePointClient.this.getBearer());
                        urlc.setRequestProperty("X-RequestDigest", "digest");
                        urlc.setRequestProperty("Content-Type", "application/octet-stream");
                        OutputStream os = urlc.getOutputStream();
                        if (this.baos.size() > 0) {
                            os.write(this.baos.toByteArray());
                        } else {
                            urlc.setLength(0L);
                        }
                        os.close();
                        int code = urlc.getResponseCode();
                        if (code < 200 || code > 299) {
                            String result = Common.consumeResponse(urlc.getInputStream());
                            SharePointClient.this.log(result);
                            throw new Exception(SharePointClient.this.getErrorInfo(result, code));
                        }
                    }
                    catch (Exception e) {
                        SharePointClient.this.log(e);
                        try {
                            this.cancelUpload();
                        }
                        catch (Exception ec) {
                            SharePointClient.this.log(ec);
                        }
                        try {
                            SharePointClient.this.delete(this.val$path);
                        }
                        catch (Exception ed) {
                            SharePointClient.this.log(ed);
                        }
                        this.closed = true;
                        throw new IOException(e);
                    }
                }
                this.closed = true;
            }
        }
        return new OutputWrapper(path);
    }

    @Override
    public boolean delete(String path) throws Exception {
        Properties p = this.stat(path);
        if (p == null) {
            return true;
        }
        String temp_path = path;
        if (temp_path.endsWith("/")) {
            temp_path = temp_path.substring(0, temp_path.length() - 1);
        }
        String type = "File";
        if (p.getProperty("type").equalsIgnoreCase("DIR")) {
            type = "Folder";
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("sharepoint_site_id", "") + this.config.getProperty("sharepoint_site_path", "") + "_api/web/Get" + type + "ByServerRelativeUrl(@v)?@v=" + this.getRelativePath(temp_path)), this.config);
        urlc.setRequestMethod("DELETE");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("If-Match", "*");
        urlc.setRequestProperty("X-RequestDigest", "digest");
        int code = urlc.getResponseCode();
        String result = Common.consumeResponse(urlc.getInputStream());
        if (code < 200 || code > 299) {
            this.log(result);
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
    public boolean makedir(String path) throws Exception {
        String temp_path = path;
        if (temp_path.endsWith("/")) {
            temp_path = temp_path.substring(0, temp_path.length() - 1);
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("sharepoint_site_id", "") + this.config.getProperty("sharepoint_site_path", "") + "_api/web/folders"), this.config);
        urlc.setRequestMethod("POST");
        urlc.setDoOutput(true);
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("Accept", "application/json;odata=verbose");
        urlc.setRequestProperty("Content-Type", "application/json");
        urlc.setRequestProperty("X-RequestDigest", "digest");
        JSONObject data = new JSONObject();
        data.put("ServerRelativeUrl", Common.replace_str(String.valueOf(this.config.getProperty("sharepoint_site_path", "")) + this.config.getProperty("sharepoint_site_drive_name", "") + temp_path, "//", "/"));
        OutputStream out = urlc.getOutputStream();
        out.write(data.toJSONString().getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        String result = Common.consumeResponse(urlc.getInputStream());
        if (code < 200 || code > 299) {
            this.log(result);
            return false;
        }
        return true;
    }

    @Override
    public boolean rename(String rnfr, String rnto, boolean overwrite) throws Exception {
        Properties p = this.stat(rnfr);
        String type = "File";
        String flags = ",flags=1";
        if (p.getProperty("type").equalsIgnoreCase("DIR")) {
            type = "Folder";
            flags = "";
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("sharepoint_site_id", "") + this.config.getProperty("sharepoint_site_path", "") + "_api/web/Get" + type + "ByServerRelativeUrl(@v1)/moveto(newurl=@v2" + flags + ")?@v1=" + this.getRelativePath(rnfr) + "&@v2=" + this.getRelativePath(rnto)), this.config);
        urlc.setRequestMethod("POST");
        urlc.setDoOutput(true);
        urlc.setDoInput(true);
        urlc.setRequestProperty("Accept", "application/json;odata=verbose");
        urlc.setRequestProperty("X-RequestDigest", "digest");
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setRequestProperty("If-Match", "*");
        urlc.setRequestProperty("X-RequestDigest", "digest");
        urlc.setLength(0L);
        urlc.getOutputStream().close();
        int code = urlc.getResponseCode();
        if (code < 200 || code > 299) {
            String result = Common.consumeResponse(urlc.getInputStream());
            this.log(result);
            return false;
        }
        return true;
    }

    private Properties getAccessToken(String site_id, String sitePath, String client_id, String client_secret) throws Exception {
        if (this.bearer_realm.equals("") && this.app_id.equals("")) {
            URLConnection urlc = URLConnection.openConnection(new VRL("https://" + site_id + sitePath), this.config);
            urlc.setDoOutput(false);
            urlc.setRequestMethod("GET");
            urlc.setRequestProperty("Authorization", "Bearer");
            urlc.getResponseCode();
            String auth_reply = urlc.getHeaderField("WWW-Authenticate");
            if (auth_reply != null && auth_reply.contains("Bearer realm=\"") && auth_reply.contains("client_id=\"")) {
                int index_br = auth_reply.indexOf("Bearer realm=\"") + "Bearer realm=\"".length();
                this.bearer_realm = auth_reply.substring(index_br, auth_reply.indexOf("\"", index_br));
                int index_ai = auth_reply.indexOf("client_id=\"") + "client_id=\"".length();
                this.app_id = auth_reply.substring(index_ai, auth_reply.indexOf("\"", index_ai));
            } else {
                throw new Exception("Error: Could not get realm and application id. Check the site path!");
            }
        }
        String tenant_host = "accounts.accesscontrol.windows.net";
        if (site_id.endsWith(".sharepoint.cn")) {
            tenant_host = "accounts.accesscontrol.chinacloudapi.cn";
        }
        if (site_id.endsWith(".sharepoint.de")) {
            tenant_host = "login.microsoftonline.de";
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://" + tenant_host + "/" + this.bearer_realm + "/tokens/OAuth/2"), this.config);
        urlc.setDoOutput(true);
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String full_form = "grant_type=client_credentials";
        full_form = String.valueOf(full_form) + "&client_id=" + Common.url_encode_all(String.valueOf(client_id) + "@" + this.bearer_realm);
        full_form = String.valueOf(full_form) + "&client_secret=" + Common.url_encode_all(client_secret);
        full_form = String.valueOf(full_form) + "&resource=" + Common.url_encode_all(String.valueOf(this.app_id) + "/" + site_id + "@" + this.bearer_realm);
        OutputStream out = urlc.getOutputStream();
        out.write(full_form.getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        String result = Common.consumeResponse(urlc.getInputStream());
        if (code < 200 || code > 299) {
            throw new Exception(this.getErrorInfo(result, code));
        }
        String access_token = (String)((JSONObject)JSONValue.parse(result)).get("access_token");
        String expires_in = "" + ((JSONObject)JSONValue.parse(result)).get("expires_in");
        Properties p = new Properties();
        p.put("access_token", access_token);
        p.put("expires_in", expires_in);
        return p;
    }

    private String getBearer() throws Exception {
        if (this.config.containsKey("token_start") && this.config.containsKey("token_expire") && System.currentTimeMillis() - Long.parseLong(this.config.getProperty("token_start")) > (Long.parseLong(this.config.getProperty("token_expire")) - 600L) * 1000L) {
            Properties p = this.getAccessToken(this.config.getProperty("sharepoint_site_id").trim(), this.config.getProperty("sharepoint_site_path").trim(), this.config.getProperty("username").trim(), this.config.getProperty("password").trim());
            if (p.containsKey("access_token")) {
                this.bearer = p.getProperty("access_token");
                this.config.put("token_start", String.valueOf(System.currentTimeMillis()));
            }
            if (p.containsKey("expires_in")) {
                this.config.put("token_expire", p.getProperty("expires_in"));
            }
        }
        return this.bearer;
    }

    private String getRelativePath(String path) {
        String temp_path = path;
        temp_path = Common.replace_str(temp_path, "'", "%2527");
        String relative_path = "'" + this.config.getProperty("sharepoint_site_path", "") + this.config.getProperty("sharepoint_site_drive_name", "") + temp_path + "'";
        relative_path = Common.replace_str(relative_path, "//", "/");
        relative_path = Common.replace_str(relative_path, "&", "%2526");
        relative_path = Common.replace_str(relative_path, "+", "%252B");
        return relative_path;
    }

    public String getErrorInfo(String result, int code) {
        if (!result.equals("")) {
            String error = "ERROR: Error code: " + code + "\n";
            try {
                JSONObject jo = (JSONObject)JSONValue.parse(result);
                if (jo != null && jo.get("error") != null) {
                    if (jo.get("error") instanceof String) {
                        if (jo.get("error_description") != null) {
                            if (jo.get("error_description").toString().startsWith("AADSTS7000222")) {
                                error = String.valueOf(error) + "Description: AADSTS7000222 - The provided client secret keys for the app are expired! Create new keys for the app!";
                            } else if (jo.get("error_description").toString().startsWith("AADSTS700016")) {
                                error = String.valueOf(error) + "Description: AADSTS700016 - Invalid Client Id! The provided client id : " + this.config.getProperty("username", "");
                            } else if (jo.get("error_description").toString().startsWith("AADSTS7000215")) {
                                error = String.valueOf(error) + "Description: AADSTS7000215 - Invalid client secret provided!";
                            } else {
                                error = String.valueOf(error) + "Description: " + jo.get("error_description") + "\n";
                                if (jo.get("error_codes") != null) {
                                    error = String.valueOf(error) + "Codes: " + jo.get("error_codes") + "\n";
                                }
                            }
                        }
                    } else if (jo.get("error") instanceof JSONObject) {
                        JSONObject message_jo;
                        JSONObject err_jo = (JSONObject)jo.get("error");
                        if (err_jo.get("code") != null) {
                            error = String.valueOf(error) + "Error: " + err_jo.get("code") + "\n";
                        }
                        if (err_jo.get("message") != null && (message_jo = (JSONObject)err_jo.get("message")).get("value") != null) {
                            error = String.valueOf(error) + "Error Message: " + message_jo.get("value") + "\n";
                        }
                    }
                } else {
                    error = String.valueOf(error) + "Response : " + result;
                }
            }
            catch (Exception e) {
                this.log(e);
                error = String.valueOf(error) + "Response : " + result;
            }
            return error;
        }
        return result;
    }
}

