/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.GenericClient;
import com.crushftp.client.URLConnection;
import com.crushftp.client.VRL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class CitrixClient
extends GenericClient {
    String bearer = "";
    static Properties resourceIdCache = new Properties();
    SimpleDateFormat sdf_rfc1123_2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
    Properties config = new Properties();

    public CitrixClient(String url, String header, Vector log) {
        super(header, log);
        this.fields = new String[]{"username", "password", "subdomain", "apicp"};
        System.setProperty("crushtunnel.debug", "2");
        if (!url.endsWith("/")) {
            url = String.valueOf(url) + "/";
        }
        this.url = url;
    }

    @Override
    public String login2(String username, String password, String clientid) throws Exception {
        password = VRL.vrlDecode(password);
        this.config.put("username", username);
        this.config.put("password", password.split("\\{split\\}")[0]);
        this.config.put("subdomain", password.split("\\{split\\}")[1]);
        this.config.put("apicp", password.split("\\{split\\}")[2]);
        Properties p = CitrixClient.setup_bearer_refresh(this.config.getProperty("subdomain"), this.config.getProperty("apicp"), this.config.getProperty("password"), username.split("~")[0], username.split("~")[1]);
        this.log("Gdrive:  Login : Get barear response: " + p.keySet().toString());
        if (p.containsKey("access_token")) {
            this.bearer = p.getProperty("access_token");
            return "Success";
        }
        if (p.containsKey("refresh_token")) {
            this.bearer = p.getProperty("refresh_token");
            return "Success";
        }
        return "Failure!";
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        if (path.equals("/")) {
            this.list2(path, list, "root");
            return list;
        }
        String resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + path.substring(0, path.length() - 1));
        if (resourceId == null) {
            this.loadResourceIds(Common.all_but_last(path));
            resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + path.substring(0, path.length() - 1));
        }
        this.list2(path, list, resourceId);
        return list;
    }

    private void list2(String path, Vector list, String resourceId) throws Exception {
        URLConnection urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("subdomain") + "." + "sf-api.com" + "/sf/v3/Items(" + resourceId + ")/Children?includeDeleted=false"), new Properties());
        urlc.setDoOutput(true);
        urlc.setRequestMethod("GET");
        urlc.setRequestProperty("Authorization", "Bearer " + this.bearer);
        int code = urlc.getResponseCode();
        String json = Common.consumeResponse(urlc.getInputStream());
        JSONArray ja = (JSONArray)((JSONObject)JSONValue.parse(json)).get("value");
        int x = 0;
        while (x < ja.size()) {
            Object obj = ja.get(x);
            if (obj instanceof JSONObject) {
                Properties stat = this.createItem(path, obj);
                list.addElement(stat);
            }
            ++x;
        }
    }

    private Properties createItem(String path, Object obj) throws ParseException, Exception {
        Properties item = new Properties();
        JSONObject jo = (JSONObject)obj;
        boolean folder = !jo.get("odata.type").equals("ShareFile.Api.Models.File");
        Object[] a = jo.entrySet().toArray();
        int i = 0;
        while (i < a.length) {
            String key2 = a[i].toString().split("=")[0];
            item.put(key2.trim(), ("" + jo.get(key2)).trim());
            ++i;
        }
        Date d = this.sdf_rfc1123_2.parse(item.getProperty("CreationDate"));
        String size = "0";
        if (!folder) {
            size = item.getProperty("FileSizeBytes");
        }
        String line = String.valueOf(folder ? "d" : "-") + "rwxrwxrwx   1    owner   group   " + size + "   " + this.yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + item.getProperty("Name");
        Properties stat = CitrixClient.parseStat(line);
        stat.put("resource_id", item.getProperty("Id"));
        stat.put("url", "citrix://" + this.config.getProperty("username") + ":" + VRL.vrlEncode(this.config.getProperty("password")) + "@" + "sf-api.com" + path + stat.getProperty("name"));
        resourceIdCache.put(String.valueOf(this.config.getProperty("password")) + path + stat.getProperty("name"), item.getProperty("Id"));
        if (stat.getProperty("type", "").equalsIgnoreCase("DIR")) {
            resourceIdCache.put(String.valueOf(this.config.getProperty("password")) + path + stat.getProperty("name") + "/", item.getProperty("Id"));
        }
        return stat;
    }

    @Override
    protected OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        String parentId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + Common.all_but_last(path).substring(0, Common.all_but_last(path).length() - 1));
        URLConnection urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("subdomain") + "." + "sf-api.com" + "/sf/v3/Items(" + parentId + ")/Upload"), new Properties());
        urlc.setDoOutput(true);
        urlc.setRequestMethod("GET");
        urlc.setRequestProperty("Authorization", "Bearer " + this.bearer);
        int code = urlc.getResponseCode();
        String json = Common.consumeResponse(urlc.getInputStream());
        String chunk_url = (String)((JSONObject)JSONValue.parse(json)).get("ChunkUri");
        URL url = new URL(chunk_url);
        HttpURLConnection urlc2 = (HttpURLConnection)url.openConnection();
        urlc2.setDoOutput(true);
        String boundary = Common.makeBoundary(11);
        urlc2.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary + "; charset=UTF-8");
        String content_header = "--" + boundary + "\r\n";
        content_header = String.valueOf(content_header) + "Content-Disposition: form-data; name=\"File1\"; filename=\"" + Common.last(path) + "\"\r\n";
        content_header = String.valueOf(content_header) + "Content-Type: application/octet-stream\r\n\r\n";
        urlc2.getOutputStream().write(content_header.toString().getBytes("UTF8"));
        this.out = urlc2.getOutputStream();
        class OutputWrapperHttp
        extends OutputStream {
            OutputStream out3 = null;
            boolean closed = false;
            HttpURLConnection urlc2 = null;
            private final /* synthetic */ String val$boundary;

            public OutputWrapperHttp(OutputStream out3, HttpURLConnection urlc2, String string) {
                this.val$boundary = string;
                this.out3 = out3;
                this.urlc2 = urlc2;
            }

            @Override
            public void write(int i) throws IOException {
                this.out3.write(i);
            }

            @Override
            public void write(byte[] b) throws IOException {
                this.write(b, 0, b.length);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                this.out3.write(b, off, len);
            }

            @Override
            public void close() throws IOException {
                if (this.closed) {
                    return;
                }
                this.out3.flush();
                this.out3.write(("\r\n--" + this.val$boundary + "--\r\n").getBytes("UTF8"));
                this.closed = true;
                int code = this.urlc2.getResponseCode();
                try {
                    String json = Common.consumeResponse(this.urlc2.getInputStream());
                    System.out.println("Citrix upload response :" + code + " : " + json);
                }
                catch (Exception e) {
                    CitrixClient.this.log(e);
                }
                this.out3.close();
                if (code > 299) {
                    throw new IOException(String.valueOf(this.urlc2.getResponseCode()) + ":" + this.urlc2.getResponseMessage());
                }
            }
        }
        this.out = new OutputWrapperHttp(this.out, urlc2, boundary);
        return this.out;
    }

    @Override
    public boolean mdtm(String path, long modified) throws Exception {
        String resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + path);
        if (resourceId == null) {
            this.log("MDTM - path not found:" + path + "\r\n");
            return false;
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("subdomain") + "." + "sf-api.com" + "/sf/v3/Items(" + resourceId + ")"), new Properties());
        urlc.setDoOutput(true);
        urlc.setRequestMethod("PATCH");
        urlc.setRequestProperty("Authorization", "Bearer " + this.bearer);
        urlc.setRequestProperty("Content-Type", "application/json");
        JSONObject fileMetaInfo = new JSONObject();
        fileMetaInfo.put("CreationDate", this.sdf_rfc1123_2.format(new Date(modified)));
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
        return true;
    }

    @Override
    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        String parentId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + path);
        URL url = new URL("https://" + this.config.getProperty("subdomain") + "." + "sf-api.com" + "/sf/v3/Items(" + parentId + ")/Download");
        HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
        urlc.setDoOutput(false);
        urlc.setRequestMethod("GET");
        urlc.setRequestProperty("Authorization", "Bearer " + this.bearer);
        int code = urlc.getResponseCode();
        if (code < 200 || code > 299) {
            String result = URLConnection.consumeResponse(urlc.getInputStream());
            urlc.disconnect();
            this.log(String.valueOf(result) + "\r\n");
            throw new Exception(result);
        }
        this.in = urlc.getInputStream();
        return this.in;
    }

    @Override
    public boolean makedir(String path) throws Exception {
        String parentId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + Common.all_but_last(path).substring(0, Common.all_but_last(path).length() - 1));
        URLConnection urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("subdomain") + "." + "sf-api.com" + "/sf/v3/Items(" + parentId + ")/Folder"), new Properties());
        urlc.setDoOutput(true);
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Authorization", "Bearer " + this.bearer);
        urlc.setRequestProperty("Content-Type", "application/json");
        JSONObject fileMetaInfo = new JSONObject();
        String[] folders = path.split("/");
        fileMetaInfo.put("name", folders[folders.length - 1]);
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
        return true;
    }

    @Override
    public boolean makedirs(String path) throws Exception {
        boolean ok = true;
        String[] parts = path.split("/");
        String path2 = "";
        int x = 0;
        while (x < parts.length && ok) {
            String resourceId;
            path2 = String.valueOf(path2) + parts[x] + "/";
            if (x >= 1 && (resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + path2)) == null) {
                ok = this.makedir(path2);
            }
            ++x;
        }
        return ok;
    }

    @Override
    public boolean delete(String path) throws Exception {
        String resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + path);
        if (resourceId == null) {
            this.log("Delete path not found:" + path + "\r\n");
            return true;
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("subdomain") + "." + "sf-api.com" + "/sf/v3/Items(" + resourceId + ")"), new Properties());
        urlc.setRequestProperty("Authorization", "Bearer " + this.bearer);
        urlc.setDoOutput(false);
        urlc.setRequestMethod("DELETE");
        int code = urlc.getResponseCode();
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        urlc.disconnect();
        if (code < 200 || code > 299) {
            this.log(String.valueOf(result) + "\r\n");
            return false;
        }
        resourceIdCache.remove(String.valueOf(this.config.getProperty("password")) + path);
        return true;
    }

    @Override
    public boolean rename(String rnfr, String rnto, boolean overwrite) throws Exception {
        String resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + rnfr);
        if (resourceId == null) {
            this.log("Rename path not found:" + rnfr + "\r\n");
            return false;
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("subdomain") + "." + "sf-api.com" + "/sf/v3/Items(" + resourceId + ")"), new Properties());
        urlc.setDoOutput(true);
        urlc.setRequestMethod("PATCH");
        urlc.setRequestProperty("Authorization", "Bearer " + this.bearer);
        urlc.setRequestProperty("Content-Type", "application/json");
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
        resourceIdCache.remove(String.valueOf(this.config.getProperty("password")) + rnfr);
        return true;
    }

    public static Properties setup_bearer(String subdomain, String apicp, String oauth_access_code, String server_url, String citrix_client_id, String citrix_client_secret) {
        try {
            String full_form = "code=" + URLEncoder.encode(oauth_access_code, "UTF-8");
            full_form = String.valueOf(full_form) + "&client_id=" + citrix_client_id;
            full_form = String.valueOf(full_form) + "&client_secret=" + citrix_client_secret;
            full_form = String.valueOf(full_form) + "&redirect_uri=" + server_url;
            full_form = String.valueOf(full_form) + "&grant_type=authorization_code";
            byte[] b = full_form.getBytes("UTF8");
            URLConnection urlc = URLConnection.openConnection(new VRL("https://" + subdomain + "." + apicp + "/oauth/token"), new Properties());
            urlc.setDoOutput(true);
            urlc.setRequestMethod("POST");
            OutputStream out = urlc.getOutputStream();
            out.write(full_form.getBytes("UTF8"));
            out.close();
            urlc.getResponseCode();
            String result = Common.consumeResponse(urlc.getInputStream());
            JSONObject obj = (JSONObject)JSONValue.parse(result);
            Properties p = new Properties();
            p.put("access_token", obj.get("access_token"));
            p.put("refresh_token", obj.get("refresh_token"));
            p.put("subdomain", subdomain);
            p.put("apicp", apicp);
            return p;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Properties setup_bearer_refresh(String subdomain, String apicp, String refresh_token, String citrix_client_id, String citrix_client_secret) throws Exception {
        String full_form = "grant_type=refresh_token";
        full_form = String.valueOf(full_form) + "&refresh_token=" + refresh_token;
        full_form = String.valueOf(full_form) + "&client_id=" + citrix_client_id;
        full_form = String.valueOf(full_form) + "&client_secret=" + citrix_client_secret;
        URLConnection urlc = URLConnection.openConnection(new VRL("https://" + subdomain + ".sharefile.com/oauth/token"), new Properties());
        urlc.setDoOutput(true);
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        OutputStream out = urlc.getOutputStream();
        out.write(full_form.getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        String result = Common.consumeResponse(urlc.getInputStream());
        if (code < 200 || code > 299) {
            throw new IOException(result);
        }
        Properties p = new Properties();
        p.put("access_token", ((JSONObject)JSONValue.parse(result)).get("access_token"));
        p.put("refresh_token", ((JSONObject)JSONValue.parse(result)).get("refresh_token"));
        return p;
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

    private void loadResourceIds(String path) throws Exception {
        String[] parts = null;
        parts = path.equals("/") ? new String[]{""} : path.split("/");
        String path2 = "";
        int x = 0;
        while (x < parts.length) {
            path2 = String.valueOf(path2) + parts[x] + "/";
            String resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("password")) + path.substring(0, path.length() - 1));
            if (path2.equals("/")) {
                resourceId = "root";
            }
            try {
                this.list2(path2, new Vector(), resourceId);
            }
            catch (Exception e) {
                this.log("Load Resource Ids : " + e);
            }
            ++x;
        }
    }
}

