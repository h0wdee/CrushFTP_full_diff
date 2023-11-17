/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.GenericClient;
import com.crushftp.client.URLConnection;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import com.crushftp.client.ZipTransfer;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class HTTPClient
extends GenericClient {
    Vector openConnections = new Vector();
    final HTTPClient thisObj = this;
    public static Properties ram_pending_bytes_multisegment = new Properties();

    public HTTPClient(String url, String header, Vector log) {
        super(header, log);
        this.fields = new String[]{"username", "password", "clientid", "keystore_path", "SAMLResponse", "timeout", "crushAuth", "dmz_stat_caching", "multi_segmented_download", "multi_segmented_download_threads", "path_shortening", "receive_compressed", "multi", "expect_100", "send_compressed", "error", "last_md5", "abort_obj", "multi_segmented_upload_threads"};
        if (!url.endsWith("/")) {
            url = String.valueOf(url) + "/";
        }
        this.url = url;
        this.config.put("protocol", "HTTP");
    }

    @Override
    public String login2(String username, String password, String clientid) throws Exception {
        username = VRL.vrlEncode(username);
        password = VRL.vrlEncode(password);
        this.config.put("username", username);
        this.config.put("password", password);
        if (clientid != null) {
            this.config.put("clientid", clientid);
        }
        if (!this.config.getProperty("crushAuth", "").equals("")) {
            return "Success";
        }
        if (username.equals("anonymous") && password.equals("anonymous")) {
            return "Success";
        }
        System.getProperties().put("sun.net.http.retryPost", "false");
        VRL u = new VRL(String.valueOf(this.url) + "WebInterface/function/");
        String result = "";
        if (u.getProtocol().equalsIgnoreCase("HTTPS") && !this.config.getProperty("keystore_path", "").equals("")) {
            u = new VRL(this.url);
            this.log("Connecting to:" + u.getHost() + ":" + u.getPort());
            URLConnection urlc = URLConnection.openConnection(u, this.config);
            try {
                urlc.setRequestMethod("POST");
                urlc.setUseCaches(false);
                urlc.setDoOutput(true);
                urlc.getOutputStream().write(("command=login&username=" + username + "&password=" + password + "&clientid=" + this.config.getProperty("clientid")).getBytes("UTF8"));
                if (!this.getConfig("SAMLResponse", "").equals("")) {
                    urlc.getOutputStream().write(("&SAMLResponse=" + this.getConfig("SAMLResponse")).getBytes("UTF8"));
                }
                urlc.setReadTimeout(Integer.parseInt(this.config.getProperty("timeout", "20000")));
                urlc.getResponseCode();
                if (urlc.getCookie("CrushAuth") == null) {
                    throw new SocketException("Login failed, no CrushAuth cookie");
                }
                this.config.put("crushAuth", urlc.getCookie("CrushAuth"));
                result = Common.consumeResponse(urlc.getInputStream());
            }
            finally {
                urlc.disconnect();
            }
            return "Success";
        }
        this.log("Connecting to:" + u.getHost() + ":" + u.getPort());
        URLConnection urlc = URLConnection.openConnection(u, this.config);
        try {
            urlc.setRequestMethod("POST");
            urlc.setUseCaches(false);
            urlc.setDoOutput(true);
            urlc.getOutputStream().write(("command=login&username=" + username + "&password=" + password + "&clientid=" + this.config.getProperty("clientid")).getBytes("UTF8"));
            if (!this.getConfig("SAMLResponse", "").equals("")) {
                urlc.getOutputStream().write(("&SAMLResponse=" + this.getConfig("SAMLResponse")).getBytes("UTF8"));
            }
            urlc.setReadTimeout(Integer.parseInt(this.config.getProperty("timeout", "20000")));
            urlc.getResponseCode();
            if (urlc.getCookie("CrushAuth") == null) {
                throw new SocketException("Login failed, no CrushAuth cookie");
            }
            this.config.put("crushAuth", urlc.getCookie("CrushAuth"));
            result = Common.consumeResponse(urlc.getInputStream());
        }
        finally {
            urlc.disconnect();
        }
        String message = "";
        if (result.indexOf("<message>") >= 0) {
            message = result.substring(result.indexOf("<message>"), result.indexOf("</message>") + "</message>".length());
        }
        if ((result = result.substring(result.indexOf("<response>"), result.indexOf("</response>") + "</response>".length())).indexOf("failure") >= 0) {
            throw new Exception(String.valueOf(result) + ":" + message);
        }
        return result;
    }

    @Override
    public void logout() throws Exception {
        this.close();
        try {
            this.doAction("logout", null, null);
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.config.put("crushAuth", "");
    }

    @Override
    public void close() throws Exception {
        if (this.in != null) {
            this.in.close();
        }
        if (this.out != null) {
            this.out.close();
        }
        while (this.openConnections.size() > 0) {
            URLConnection urlc = (URLConnection)this.openConnections.remove(0);
            urlc.disconnect();
        }
        this.in = null;
        this.out = null;
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        Properties listingProp = this.list2(path, list);
        return (Vector)listingProp.remove("listing");
    }

    public Properties list2(String path, Vector list) throws Exception {
        String result = this.doAction("list", path, "");
        String result2 = null;
        int x = 0;
        while (x < 5) {
            if (result.indexOf("<listing>") >= 0 && result.indexOf("</listing>") >= 0) {
                result2 = String.valueOf(result.substring(0, result.indexOf("<listing>"))) + result.substring(result.lastIndexOf("</listing>") + "</listing>".length());
                break;
            }
            Thread.sleep(1000L);
            result = this.doAction("list", path, "");
            if (result.startsWith("FAILURE: Hadoop: The url is not active:") || result.startsWith("FAILURE: Hadoop: List - ") || result.startsWith("FAILURE: ERROR : Bad credentials")) break;
            ++x;
        }
        if (result2 == null) {
            throw new IOException("Listing failed:" + path + ":" + result);
        }
        Properties listingProp = (Properties)Common.readXMLObject(new ByteArrayInputStream(result2.getBytes("UTF8")));
        listingProp.put("listing", list);
        result = result.substring(result.indexOf("<listing>") + "<listing>".length(), result.lastIndexOf("</listing>")).trim();
        if (this.statCache != null) {
            Enumeration<Object> keys = this.statCache.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                if (!(this.statCache.get(key) instanceof Properties)) continue;
                Properties h = (Properties)this.statCache.get(key);
                if (System.currentTimeMillis() - Long.parseLong(h.getProperty("time")) <= (long)this.max_cache_time) continue;
                this.statCache.remove(key);
            }
        }
        BufferedReader br = new BufferedReader(new StringReader(result));
        String data = "";
        while ((data = br.readLine()) != null) {
            Properties stat = HTTPClient.parseDmzStat(data);
            String path2 = path.substring(1);
            if (!path2.endsWith("/")) {
                path2 = String.valueOf(path2) + "/";
            }
            if (stat.getProperty("type", "FILE").equalsIgnoreCase("DIR")) {
                stat.put("url", String.valueOf(this.url) + path2 + stat.getProperty("name") + "/");
            } else {
                stat.put("url", String.valueOf(this.url) + path2 + stat.getProperty("name"));
            }
            list.addElement(stat);
            if (!this.config.getProperty("dmz_stat_caching", "true").equals("true") || this.statCache == null) continue;
            Properties h = new Properties();
            h.put("time", String.valueOf(System.currentTimeMillis()));
            h.put("stat", stat.clone());
            this.statCache.put(HTTPClient.noSlash(String.valueOf(path) + stat.getProperty("name")), h);
            if (this.statCache.containsKey(String.valueOf(path) + "._" + stat.getProperty("name"))) continue;
            h = new Properties();
            h.put("time", String.valueOf(System.currentTimeMillis()));
            this.statCache.put(HTTPClient.noSlash(String.valueOf(path) + "._" + stat.getProperty("name")), h);
        }
        if (this.statCache != null && list.size() > this.max_cache_list_count) {
            this.statCache.clear();
        }
        this.addFinderBadItems();
        return listingProp;
    }

    public void addFinderBadItems() throws Exception {
        if (this.statCache != null) {
            long aday = System.currentTimeMillis() + 86400000L;
            Properties h = new Properties();
            h.put("time", String.valueOf(aday));
            this.statCache.put(HTTPClient.noSlash("/Backups.backupdb"), h);
            h = new Properties();
            h.put("time", String.valueOf(aday));
            this.statCache.put(HTTPClient.noSlash("/.hidden"), h);
            h = new Properties();
            h.put("time", String.valueOf(aday));
            this.statCache.put(HTTPClient.noSlash("/mach_kernel"), h);
            h = new Properties();
            h.put("time", String.valueOf(aday));
            this.statCache.put(HTTPClient.noSlash("/DCIM"), h);
            h = new Properties();
            h.put("time", String.valueOf(aday));
            this.statCache.put(HTTPClient.noSlash("/.Spotlight-V100"), h);
            h = new Properties();
            Properties stat = HTTPClient.parseStat("-rwx------  1 user  group           0 20000101000000 01 2000 /.metadata_never_index");
            if (stat != null) {
                stat.put("url", String.valueOf(this.url) + ".metadata_never_index");
            }
            h.put("stat", stat.clone());
            h.put("time", String.valueOf(aday));
            this.statCache.put(HTTPClient.noSlash("/.metadata_never_index"), h);
        }
    }

    public ZipTransfer getZipTransfer(String path, Properties params, boolean compress) {
        return new ZipTransfer(this.url, this.config.getProperty("crushAuth", ""), path, params, compress);
    }

    @Override
    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        return this.download3(path, startPos, endPos, binary, null, -1);
    }

    protected InputStream download3(String path, long startPos, long endPos, boolean binary, String paths, int rev) throws Exception {
        if (Integer.parseInt(System.getProperty("crushftp.ram_used_percent", "0")) > 60 && this.config.getProperty("multi_segmented_download", "false").equals("true")) {
            this.config.put("multi_segmented_download", "false");
            this.log("Disabling multi segmented download due to lack of overall server memory.  Used percentage:" + Integer.parseInt(System.getProperty("crushftp.ram_used_percent", "0")) + "%:" + path);
        }
        this.log("download | " + path + " | " + startPos + " | " + endPos + " | " + binary + (paths != null ? " | downloadAsZip:" + paths : "") + (rev >= 0 ? " | revision:" + rev : "") + " multi:" + this.config.getProperty("multi_segmented_download", "false").equals("true"));
        URLConnection urlc = null;
        if (rev >= 0) {
            urlc = URLConnection.openConnection(new VRL(String.valueOf(this.url) + "WebInterface/function/"), this.config);
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Cookie", "CrushAuth=" + this.config.getProperty("crushAuth", "") + ";");
            urlc.setUseCaches(false);
            urlc.setDoOutput(true);
            urlc.getOutputStream().write(("command=download&c2f=" + this.config.getProperty("crushAuth", "").substring(this.config.getProperty("crushAuth", "").length() - 4) + "&path=" + Common.url_encode(path) + "&META_downloadRevision=" + rev).getBytes("UTF8"));
            urlc.setReadTimeout(Integer.parseInt(this.config.getProperty("timeout", "0")));
            urlc.getResponseCode();
        } else if (paths != null) {
            urlc = URLConnection.openConnection(new VRL(String.valueOf(this.url) + "WebInterface/function/"), this.config);
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Cookie", "CrushAuth=" + this.config.getProperty("crushAuth", "") + ";");
            urlc.setUseCaches(false);
            urlc.setDoOutput(true);
            urlc.getOutputStream().write(("command=downloadAsZip&c2f=" + this.config.getProperty("crushAuth", "").substring(this.config.getProperty("crushAuth", "").length() - 4) + "&path_shortening=" + this.config.getProperty("path_shortening", "true").equals("true") + "&path=" + Common.url_encode(path) + "&paths=" + Common.url_encode(paths)).getBytes("UTF8"));
            urlc.setReadTimeout(Integer.parseInt(this.config.getProperty("timeout", "0")));
            urlc.getResponseCode();
        } else {
            if (this.config.getProperty("multi_segmented_download", "false").equals("true")) {
                return this.getSegmentedDownload(path, startPos, endPos);
            }
            urlc = URLConnection.openConnection(new VRL(String.valueOf(this.url) + path.substring(1)), this.config);
            urlc.setRequestMethod("GET");
            urlc.setRequestProperty("Cookie", "CrushAuth=" + this.config.getProperty("crushAuth", "") + ";");
            if (startPos > 0L || endPos >= 0L) {
                urlc.setRequestProperty("Range", "bytes=" + startPos + "-" + (endPos >= 0L ? String.valueOf(endPos) : ""));
            }
            urlc.setUseCaches(false);
            urlc.setReceiveCompression(this.config.getProperty("receive_compressed", "false").equals("true") && startPos <= 0L);
        }
        urlc.setReadTimeout(Integer.parseInt(this.config.getProperty("timeout", "0")));
        if (Common.V() >= 10) {
            urlc.enableHighLatencyBuffer();
        }
        this.openConnections.addElement(urlc);
        InputStream tmp = urlc.getInputStream();
        if (urlc.responseCode > 299) {
            this.log("download-end | " + path + " | " + startPos + " | " + endPos + " | " + binary + " | ERROR:" + urlc.responseCode + ":" + urlc.message);
            throw new IOException(String.valueOf(urlc.responseCode) + ":" + urlc.message);
        }
        class InputWrapper
        extends InputStream {
            InputStream in3 = null;
            boolean closed = false;
            long bytes = 0L;
            private final /* synthetic */ String val$path;
            private final /* synthetic */ long val$startPos;
            private final /* synthetic */ long val$endPos;
            private final /* synthetic */ boolean val$binary;

            public InputWrapper(InputStream in3, String string, long l, long l2, boolean bl) {
                this.val$path = string;
                this.val$startPos = l;
                this.val$endPos = l2;
                this.val$binary = bl;
                this.in3 = in3;
            }

            @Override
            public int read() throws IOException {
                int i = this.in3.read();
                if (i > 0) {
                    ++this.bytes;
                }
                return i;
            }

            @Override
            public int read(byte[] b) throws IOException {
                return this.read(b, 0, b.length);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int i = this.in3.read(b, off, len);
                if (i > 0) {
                    this.bytes += (long)i;
                }
                return i;
            }

            @Override
            public void close() throws IOException {
                if (!this.closed) {
                    this.closed = true;
                    this.in3.close();
                    HTTPClient.this.log("download-end | " + this.val$path + " | " + this.val$startPos + " | " + this.val$endPos + " | " + this.val$binary + " | " + this.bytes + " bytes");
                }
            }
        }
        this.in = new InputWrapper(tmp, path, startPos, endPos, binary);
        return this.in;
    }

    public InputStream downloadAsZip(String path, String paths) throws Exception {
        return this.downloadAsZip(path, paths, true);
    }

    public InputStream downloadAsZip(String path, String paths, boolean path_shortening) throws Exception {
        this.config.put("path_shortening", String.valueOf(path_shortening));
        return this.download3(path, 0L, -1L, true, paths, -1);
    }

    public InputStream downloadRev(String path, int rev) throws Exception {
        return this.download3(path, 0L, -1L, true, null, rev);
    }

    public void sendMetaInfo(Properties metaInfo) throws Exception {
        if (metaInfo == null) {
            return;
        }
        String content = "";
        Enumeration<Object> keys = metaInfo.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            String val = metaInfo.getProperty(key);
            content = String.valueOf(content) + "&META_" + key + "=" + HTTPClient.u(val);
        }
        if (!content.trim().equals("")) {
            this.doAction("setMetaInfo", content, "");
        }
    }

    @Override
    protected OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        this.log("upload | " + path + " | " + startPos + " | " + truncate + " | " + binary + " | multi=" + this.config.getProperty("multi", "false").equals("true"));
        if (this.config.getProperty("multi", "false").equals("true")) {
            return this.upload_multi(path, startPos, truncate, binary);
        }
        if (this.statCache != null) {
            this.statCache.remove(HTTPClient.noSlash(path));
        }
        URLConnection urlc = URLConnection.openConnection(new VRL(String.valueOf(this.url) + path.substring(1)), this.config);
        this.openConnections.addElement(urlc);
        urlc.setRequestMethod("PUT");
        urlc.setRequestProperty("Cookie", "CrushAuth=" + this.config.getProperty("crushAuth", "") + ";RandomAccess=" + !truncate + ";");
        urlc.setExpect100(this.config.getProperty("expect_100", "false").equals("true"));
        if (startPos > 0L) {
            urlc.setRequestProperty("Range", "bytes=" + startPos + "-");
        }
        urlc.setUseCaches(false);
        urlc.setDoOutput(true);
        urlc.setChunkedStreamingMode(9999L);
        urlc.setSendCompression(this.config.getProperty("send_compressed", "false").equals("true"));
        if (!this.transfer_info.getProperty("transfer_content_length", "").equals("")) {
            urlc.setRequestProperty("Content-Length", this.transfer_info.getProperty("transfer_content_length", ""));
        }
        class OutputWrapper
        extends OutputStream {
            boolean closed = false;
            OutputStream out3 = null;
            long bytes = 0L;
            private final /* synthetic */ String val$path;
            private final /* synthetic */ URLConnection val$urlc;
            private final /* synthetic */ long val$startPos;
            private final /* synthetic */ boolean val$truncate;
            private final /* synthetic */ boolean val$binary;

            public OutputWrapper(OutputStream out3, String string, URLConnection uRLConnection, long l, boolean bl, boolean bl2) {
                this.val$path = string;
                this.val$urlc = uRLConnection;
                this.val$startPos = l;
                this.val$truncate = bl;
                this.val$binary = bl2;
                this.out3 = out3;
            }

            @Override
            public void write(int i) throws IOException {
                ++this.bytes;
                this.out3.write(i);
            }

            @Override
            public void write(byte[] b) throws IOException {
                this.write(b, 0, b.length);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                this.bytes += (long)len;
                try {
                    this.out3.write(b, off, len);
                }
                catch (IOException e) {
                    HTTPClient.this.log(String.valueOf(this.val$path) + ":" + this.val$urlc.getResponseCode() + ":" + this.val$urlc.getResponseMessage());
                    throw new IOException(String.valueOf(this.val$path) + ":" + this.val$urlc.getResponseCode() + ":" + this.val$urlc.getResponseMessage(), e);
                }
            }

            @Override
            public void close() throws IOException {
                if (!this.closed) {
                    this.closed = true;
                    try {
                        long current_size;
                        Properties stat;
                        Properties h;
                        try {
                            this.out3.close();
                        }
                        catch (SocketTimeoutException e) {
                            if (System.getProperties().getProperty("crushftp.debug_socks_log", "false").equals("true")) {
                                Common.dumpStack("HTTP_CLOSE_SOCK_TIMEOUT:" + this.val$path);
                            }
                            throw e;
                        }
                        InputStream urlc_in = this.val$urlc.getInputStream();
                        String response = URLConnection.consumeResponse(urlc_in, false);
                        try {
                            if (this.val$urlc.getResponseCode() != 201) {
                                HTTPClient.this.log("upload-error | " + this.val$path + " | " + this.val$startPos + " | " + this.val$truncate + " | " + this.val$binary + " | " + this.bytes + " bytes" + " | " + this.val$urlc.getResponseCode() + ":" + String.valueOf(this.val$urlc.getResponseMessage()).trim() + ":" + response.trim());
                                throw new IOException("Upload failed:" + this.val$path + ":" + this.val$urlc.getResponseCode() + ":" + String.valueOf(this.val$urlc.getResponseMessage()).trim());
                            }
                            String last_md5 = response.trim();
                            HTTPClient.this.config.put("last_md5", response.trim());
                            HTTPClient.this.last_md5_buf.append(response.trim());
                            HTTPClient.this.log("upload-end (" + this.val$urlc.getResponseCode() + ") | " + this.val$path + " | " + this.val$startPos + " | " + this.val$truncate + " | " + this.val$binary + " | " + this.bytes + " bytes");
                            if (this.val$urlc.getRequestProps().getProperty("X-PROXY_UPLOAD_ACK_SOCKET", "false").equals("true")) {
                                this.val$urlc.setDoOutput(false);
                                this.val$urlc.outputProxy.write(("HEAD " + this.val$path + " HTTP/1.0\r\nX-UPLOAD-ACK-MD5: " + last_md5 + "\r\n\r\n").getBytes());
                                this.val$urlc.outputProxy.flush();
                                this.val$urlc.readResponseHeaders();
                                response = URLConnection.consumeResponse(urlc_in, false);
                                this.val$urlc.disconnect();
                            }
                        }
                        finally {
                            urlc_in.close();
                        }
                        if (HTTPClient.this.config.getProperty("dmz_stat_caching", "true").equals("true") && HTTPClient.this.statCache != null && (h = (Properties)HTTPClient.this.statCache.get(HTTPClient.noSlash(this.val$path))) != null && (stat = (Properties)h.get("stat")) != null && this.val$startPos + this.bytes > (current_size = Long.parseLong(stat.getProperty("size", "0")))) {
                            stat.put("size", String.valueOf(this.val$startPos + this.bytes));
                        }
                    }
                    catch (IOException e) {
                        String result = "";
                        try {
                            result = HTTPClient.this.doAction("getLastUploadError", "", "");
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        if (result.equalsIgnoreCase("SUCCESS")) {
                            throw e;
                        }
                        throw new IOException(Common.url_decode(result));
                    }
                }
            }
        }
        this.out = new OutputWrapper(urlc.getOutputStream(), path, urlc, startPos, truncate, binary);
        return this.out;
    }

    @Override
    public boolean upload_0_byte(String path) throws Exception {
        if (this.statCache != null) {
            this.statCache.remove(HTTPClient.noSlash(path));
        }
        return this.doAction("upload_0_byte", path, "").trim().equalsIgnoreCase("OK");
    }

    @Override
    public boolean delete(String path) throws Exception {
        if (this.statCache != null) {
            this.statCache.remove(HTTPClient.noSlash(path));
        }
        return this.doAction("delete", path, "").equals("");
    }

    @Override
    public boolean makedir(String path) throws Exception {
        if (this.statCache != null) {
            this.statCache.remove(HTTPClient.noSlash(path));
        }
        return this.doAction("makedir", path, "").equals("");
    }

    @Override
    public boolean makedirs(String path) throws Exception {
        if (this.statCache != null) {
            this.statCache.remove(HTTPClient.noSlash(path));
        }
        return this.makedir(path);
    }

    @Override
    public boolean rename(String rnfr, String rnto, boolean overwrite) throws Exception {
        String result;
        if (this.statCache != null) {
            this.statCache.remove(HTTPClient.noSlash(rnfr));
        }
        if (this.statCache != null) {
            this.statCache.remove(HTTPClient.noSlash(rnto));
        }
        if ((result = this.doAction("rename", rnfr, rnto, overwrite)).indexOf("FAILURE: Renaming of Cloud (sub)Directories is not supported, aborting rename operation.") >= 0) {
            throw new Exception("FAILURE: Renaming of Cloud (sub)Directories is not supported, aborting rename operation.");
        }
        return result.equals("");
    }

    @Override
    public Properties stat(String path) throws Exception {
        Properties h;
        this.addFinderBadItems();
        if (this.config.getProperty("dmz_stat_caching", "true").equals("true") && this.statCache != null && (h = (Properties)this.statCache.get(HTTPClient.noSlash(path))) != null) {
            if (System.currentTimeMillis() - Long.parseLong(h.getProperty("time")) < (long)this.max_cache_time) {
                Properties stat = (Properties)h.get("stat");
                if (stat == null) {
                    return null;
                }
                if (this.config.containsKey(path)) {
                    stat.put("size", this.config.getProperty(path));
                }
                stat = (Properties)stat.clone();
                return stat;
            }
            this.statCache.remove(HTTPClient.noSlash(path));
        }
        if (this.config.getProperty("dmz_stat_caching", "true").equals("true") && this.statCache != null) {
            this.statCache.put(String.valueOf(Common.all_but_last(path)) + "...count", String.valueOf(Integer.parseInt(this.statCache.getProperty(String.valueOf(Common.all_but_last(path)) + "...count", "0")) + 1));
            if (!path.equals("/") && Integer.parseInt(this.statCache.getProperty(String.valueOf(Common.all_but_last(path)) + "...count", "0")) >= 5) {
                this.list(Common.all_but_last(path), new Vector());
                this.statCache.put(String.valueOf(Common.all_but_last(path)) + "...count", "0");
                h = (Properties)this.statCache.get(HTTPClient.noSlash(path));
                if (h != null) {
                    Properties stat = (Properties)h.get("stat");
                    if (stat == null) {
                        return null;
                    }
                    if (this.config.containsKey(path)) {
                        stat.put("size", this.config.getProperty(path));
                    }
                    stat = (Properties)stat.clone();
                    return stat;
                }
            }
        }
        Properties stat = HTTPClient.parseDmzStat(this.doAction("stat", path, ""));
        if (this.config.containsKey(path)) {
            stat.put("size", this.config.getProperty(path));
        }
        if (stat != null) {
            stat.put("url", String.valueOf(this.url) + path.substring(1));
        }
        if (this.config.getProperty("dmz_stat_caching", "true").equals("true") && this.statCache != null) {
            Properties h2 = new Properties();
            h2.put("time", String.valueOf(System.currentTimeMillis()));
            if (stat != null) {
                h2.put("stat", stat.clone());
                this.statCache.put(HTTPClient.noSlash(path), h2);
            }
        }
        if (stat != null) {
            this.log("stat-server | " + path + " | " + stat.getProperty("size") + " | " + stat.getProperty("modified"));
        }
        if (stat == null && System.getProperty("crushftp.isTestCall" + Thread.currentThread().getId(), "false").equals("true")) {
            throw new Exception("Item not found..." + path);
        }
        return stat;
    }

    @Override
    public boolean mdtm(String path, long modified) throws Exception {
        if (this.statCache != null) {
            this.statCache.remove(HTTPClient.noSlash(path));
        }
        return this.doAction("mdtm", path, new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(modified))).equals("");
    }

    public String doAction(String command, String param1, String param2) throws Exception {
        return this.doAction(command, param1, param2, "", "", "", false);
    }

    public String doAction(String command, String param1, String param2, boolean overwrite) throws Exception {
        return this.doAction(command, param1, param2, "", "", "", overwrite);
    }

    public String doAction(String command, String param1, String param2, String param3, String param4, String param5) throws Exception {
        return this.doAction(command, param1, param2, param3, param4, param5, false);
    }

    public String doAction(String command, String param1, String param2, String param3, String param4, String param5, boolean overwrite) throws Exception {
        int loops = 0;
        while (loops++ < 6) {
            this.log(String.valueOf(command) + " | " + param1 + " | " + param2);
            URLConnection urlc = URLConnection.openConnection(new VRL(this.url), this.config);
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Cookie", "CrushAuth=" + this.config.getProperty("crushAuth", "") + ";");
            urlc.setUseCaches(false);
            urlc.setDoOutput(true);
            if (command.equalsIgnoreCase("logout")) {
                urlc.autoClose = true;
            }
            String c2f = "";
            if (!this.config.getProperty("crushAuth", "").equals("")) {
                c2f = this.config.getProperty("crushAuth", "").substring(this.config.getProperty("crushAuth", "").length() - 4);
            }
            if (command.equalsIgnoreCase("delete")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=delete&names=" + HTTPClient.u(param1)).getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("logout")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=logout").getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("getUserName")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=getUserName").getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("getCrushAuth")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=getCrushAuth").getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("makedir")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=makedir&path=" + HTTPClient.u(param1)).getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("rename")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=rename&name1=" + HTTPClient.u(param1) + "&name2=" + HTTPClient.u(param2) + "&path=/&overwrite=" + overwrite).getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("stat")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=stat&path=" + HTTPClient.u(param1) + "&format=stat_dmz").getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("mdtm")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=mdtm&path=" + HTTPClient.u(param1) + "&date=" + HTTPClient.u(param2)).getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("openFile")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=openFile&upload_path=" + HTTPClient.u(param1) + "&upload_id=" + HTTPClient.u(param2) + "&upload_size=" + HTTPClient.u(param3) + "&start_resume_loc=" + HTTPClient.u(param4)).getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("closeFile")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=closeFile&filePath=" + HTTPClient.u(param1) + "&upload_id=" + HTTPClient.u(param2) + "&total_bytes=" + HTTPClient.u(param3) + "&total_chunks=" + HTTPClient.u(param4) + "&lastModified=" + HTTPClient.u(param5)).getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("changePassword")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=changePassword&current_password=" + HTTPClient.u(param1) + "&new_password1=" + HTTPClient.u(param2) + "&new_password2=" + HTTPClient.u(param2)).getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("getTime")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=getTime").getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("siteCommand")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=siteCommand&siteCommand=" + HTTPClient.u(param1)).getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("getQuota")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=getQuota&path=" + HTTPClient.u(param1)).getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("upload_0_byte")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=upload_0_byte&path=" + HTTPClient.u(param1)).getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("list")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=getXMLListing&path=" + HTTPClient.u(param1) + "&format=stat_dmz").getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("setMetaInfo")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=setMetaInfo" + param1 + param2).getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("agentRegister")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=agentRegister" + param1 + param2).getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("agentQueue")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=agentQueue" + param1 + param2).getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("agentResponse")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=agentResponse" + param1 + param2).getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("getUserInfo")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=getUserInfo").getBytes("UTF8"));
            } else if (command.equalsIgnoreCase("getLastUploadError")) {
                urlc.getOutputStream().write(("c2f=" + c2f + "&command=getLastUploadError").getBytes("UTF8"));
            }
            int code = 302;
            String result = "";
            try {
                urlc.setReadTimeout(Integer.parseInt(this.config.getProperty("timeout", "20000")));
                code = urlc.getResponseCode();
                result = Common.consumeResponse(urlc.getInputStream());
            }
            catch (Exception e) {
                Common.log("HTTP_CLIENT", 1, e);
            }
            if (code != 302 && urlc.getURL().toString().indexOf("/WebInterface/login.html") >= 0) {
                code = 302;
            }
            this.setConfig("error", null);
            urlc.disconnect();
            if (code == 302 && command.equalsIgnoreCase("logout")) {
                code = 200;
            }
            if (code == 302 && loops <= 2) {
                Thread.sleep(4500L);
                continue;
            }
            if (code == 302 || result.indexOf("FAILURE:Access Denied. (c2f)") >= 0) {
                this.config.put("crushAuth", "");
                this.login(this.config.getProperty("username"), this.config.getProperty("password"), this.config.getProperty("clientid"));
                Thread.sleep(4500L);
                continue;
            }
            if (code == -1 && loops < 4) {
                Thread.sleep(100L);
                continue;
            }
            if (result.startsWith("FAILURE: Hadoop: The url is not active:") || result.startsWith("FAILURE: Hadoop: List - ")) {
                return result;
            }
            if (!command.equals("closeFile") && result.indexOf("<response>") >= 0) {
                result = result.substring(result.indexOf("<response>") + "<response>".length(), result.lastIndexOf("</response>"));
            }
            if (!command.equalsIgnoreCase("list") && !command.equalsIgnoreCase("agentQueue")) {
                if (command.equalsIgnoreCase("closeFile") && result.toLowerCase().indexOf("decompressing") >= 0) {
                    this.log("Finalizing upload..." + param1);
                } else if (result.contains("url=") && result.indexOf(";", result.indexOf("url=")) >= 0) {
                    String url = result.substring(result.indexOf("url=") + "url=".length(), result.indexOf(";", result.indexOf("url=")));
                    this.log(Common.replace_str(result, url, new VRL(Common.url_decode(url)).safe()));
                } else {
                    this.log(result);
                }
            }
            return result;
        }
        this.setConfig("error", "Logged out.");
        this.config.put("crushAuth", "");
        throw new Exception("Logged out.");
    }

    protected OutputStream upload_multi(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        String upload_id;
        String result;
        if (this.statCache != null) {
            this.statCache.remove(HTTPClient.noSlash(path));
        }
        if ((result = this.doAction("openFile", path, upload_id = Common.makeBoundary(12), "-1", String.valueOf(startPos), "")).indexOf("ERROR:") >= 0) {
            throw new IOException(result);
        }
        class OutputWrapper
        extends OutputStream {
            boolean closed = false;
            long upload_bytes = 0L;
            int chunkNum = 1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String upload_id = null;
            Vector threads = new Vector();
            Object threads_lock = new Object();
            Properties status = new Properties();
            private final /* synthetic */ String val$path;
            private final /* synthetic */ long val$startPos;
            private final /* synthetic */ boolean val$truncate;
            private final /* synthetic */ boolean val$binary;

            public OutputWrapper(String upload_id, String string, long l, boolean bl, boolean bl2) {
                this.val$path = string;
                this.val$startPos = l;
                this.val$truncate = bl;
                this.val$binary = bl2;
                this.upload_id = upload_id;
            }

            @Override
            public void write(int i) throws IOException {
                ++this.upload_bytes;
                this.baos.write(i);
            }

            @Override
            public void write(byte[] b) throws IOException {
                this.write(b, 0, b.length);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                if (this.status.containsKey("error")) {
                    throw new IOException(String.valueOf(this.status.getProperty("error")) + ":" + this.val$path);
                }
                this.upload_bytes += (long)len;
                try {
                    this.baos.write(b, off, len);
                }
                catch (Exception e) {
                    HTTPClient.this.log("http_multi_upload:" + this.val$path + ":" + e);
                    throw new IOException("http_multi_upload:" + this.val$path, e);
                }
                if (this.baos.size() > 0x500000) {
                    this.flushChunk();
                }
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            public void flushChunk() {
                if (this.baos.size() == 0) {
                    return;
                }
                final int thisChunkNum = this.chunkNum++;
                byte[] b1 = this.baos.toByteArray();
                final byte[] b2 = new byte[this.baos.size()];
                System.arraycopy(b1, 0, b2, 0, b2.length);
                this.baos.reset();
                Properties properties = ram_pending_bytes_multisegment;
                synchronized (properties) {
                    ram_pending_bytes_multisegment.put("upload", String.valueOf(Long.parseLong(ram_pending_bytes_multisegment.getProperty("upload", "0")) + (long)b2.length));
                }
                try {
                    Runnable r = new Runnable(){

                        /*
                         * Exception decompiling
                         */
                        @Override
                        public void run() {
                            /*
                             * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
                             * 
                             * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [24[UNCONDITIONALDOLOOP]], but top level block is 0[TRYBLOCK]
                             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
                             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
                             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
                             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
                             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
                             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
                             *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
                             *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
                             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
                             *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
                             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
                             *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
                             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
                             *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
                             *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
                             *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
                             *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
                             *     at org.benf.cfr.reader.Main.main(Main.java:54)
                             */
                            throw new IllegalStateException("Decompilation failed");
                        }
                    };
                    long start = System.currentTimeMillis();
                    while (System.currentTimeMillis() - start < 300000L) {
                        Object object = this.threads_lock;
                        synchronized (object) {
                            if (this.threads.size() < Integer.parseInt(HTTPClient.this.config.getProperty("multi_segmented_upload_threads", "5"))) {
                                this.threads.addElement(r);
                                break;
                            }
                        }
                        Thread.sleep(1L);
                    }
                    Worker.startWorker(r);
                }
                catch (Exception e) {
                    HTTPClient.this.log(e);
                }
            }

            @Override
            public void close() throws IOException {
                String throw_error_msg = "";
                if (!this.closed) {
                    long current_size;
                    Properties stat;
                    Properties h;
                    block17: {
                        this.closed = true;
                        try {
                            if (HTTPClient.this.config.get("abort_obj") == HTTPClient.this.thisObj) {
                                HTTPClient.this.doAction("closeFile", this.val$path, this.upload_id, String.valueOf(this.upload_bytes), String.valueOf(this.chunkNum - 1), "0");
                                throw new Exception("File upload aborted. " + this.val$path);
                            }
                            this.flushChunk();
                            if (this.status.containsKey("error")) {
                                String result = HTTPClient.this.doAction("closeFile", this.val$path, this.upload_id, String.valueOf(this.upload_bytes), String.valueOf(this.chunkNum - 1), "0");
                                if (result.indexOf("<response>") >= 0) {
                                    result = result.substring(result.indexOf("<response>") + "<response>".length(), result.indexOf("</response>")).trim();
                                }
                                if (result.toUpperCase().indexOf("ERROR:ERROR") >= 0) {
                                    result = result.substring(result.lastIndexOf("ERROR:"));
                                }
                                throw_error_msg = result;
                                throw new IOException(String.valueOf(result) + ":" + this.val$path);
                            }
                            long start = System.currentTimeMillis();
                            while (this.threads.size() > 0 && System.currentTimeMillis() - start < 300000L) {
                                Thread.sleep(100L);
                            }
                            if (System.currentTimeMillis() - start >= 300000L) {
                                HTTPClient.this.log("upload-error | " + this.val$path + " | " + this.val$startPos + " | " + this.val$truncate + " | " + this.val$binary + " | " + this.upload_bytes + " bytes" + " | 5 MIN TIMEOUT ON CLOSE_FILE");
                                throw new IOException("Upload failed:" + this.val$path + " | " + this.val$startPos + " | " + this.val$truncate + " | " + this.val$binary + " | " + this.upload_bytes + " bytes" + " | 5 MIN TIMEOUT ON CLOSE_FILE");
                            }
                            String result = "Decompressing...";
                            String md5 = "";
                            long start_close = System.currentTimeMillis();
                            long minutes = this.upload_bytes / 0x40000000L;
                            if (minutes < 5L) {
                                minutes = 5L;
                            }
                            minutes *= 60000L;
                            while (result.equals("Decompressing...") && System.currentTimeMillis() - start_close < minutes) {
                                throw_error_msg = "";
                                result = HTTPClient.this.doAction("closeFile", this.val$path, this.upload_id, String.valueOf(this.upload_bytes), String.valueOf(this.chunkNum - 1), "0");
                                if (result.indexOf("ERROR:") < 0 && result.indexOf("md5") >= 0) {
                                    md5 = result.substring(result.indexOf("<md5>") + "<md5>".length(), result.lastIndexOf("</md5>"));
                                }
                                if ((result = result.substring(result.indexOf("<response>") + "<response>".length(), result.lastIndexOf("</response>"))).toUpperCase().indexOf("ERROR:ERROR") >= 0) {
                                    result = result.substring(result.lastIndexOf("ERROR:"));
                                }
                                throw_error_msg = result;
                            }
                            if (md5.equals("")) {
                                md5 = "UNSUPPORTED";
                            }
                            HTTPClient.this.config.put("last_md5", md5.trim());
                            HTTPClient.this.last_md5_buf.append(md5.trim());
                            if (result.equals("")) {
                                HTTPClient.this.log("upload-end (" + result + ") | " + this.val$path + " | " + this.val$startPos + " | " + this.val$truncate + " | " + this.val$binary + " | " + this.upload_bytes + " bytes");
                                break block17;
                            }
                            HTTPClient.this.log("upload-error | " + this.val$path + " | " + this.val$startPos + " | " + this.val$truncate + " | " + this.val$binary + " | " + this.upload_bytes + " bytes" + " | " + result.trim());
                            throw new IOException("Upload failed:" + this.val$path + " | " + this.val$startPos + " | " + this.val$truncate + " | " + this.val$binary + " | " + this.upload_bytes + " bytes" + " | " + result.trim());
                        }
                        catch (Exception e) {
                            HTTPClient.this.log(e);
                        }
                    }
                    if (HTTPClient.this.config.getProperty("dmz_stat_caching", "true").equals("true") && HTTPClient.this.statCache != null && (h = (Properties)HTTPClient.this.statCache.get(HTTPClient.noSlash(this.val$path))) != null && (stat = (Properties)h.get("stat")) != null && this.val$startPos + this.upload_bytes > (current_size = Long.parseLong(stat.getProperty("size", "0")))) {
                        stat.put("size", String.valueOf(this.val$startPos + this.upload_bytes));
                    }
                    if (!throw_error_msg.equals("")) {
                        throw new IOException(throw_error_msg);
                    }
                }
            }

            static /* synthetic */ HTTPClient access$0(OutputWrapper outputWrapper) {
                return outputWrapper.HTTPClient.this;
            }
        }
        this.out = new OutputWrapper(upload_id, path, startPos, truncate, binary);
        return this.out;
    }

    public static String noSlash(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    @Override
    public String doCommand(String command) {
        if (command.startsWith("SITE PASS")) {
            command = command.substring("site pass".length()).trim();
            String split = command.split(" ")[0];
            command = command.substring(split.length() + 1).trim();
            try {
                String result = this.doAction("changePassword", command.split(split)[0], command.split(split)[1]);
                return "200 " + result;
            }
            catch (Exception e) {
                return "500 " + e;
            }
        }
        if (command.startsWith("SITE PGP_HEADER_SIZE")) {
            command = command.substring(command.indexOf(" ") + 1);
            command = command.substring(command.indexOf(" ") + 1);
            long size = Long.parseLong(command.substring(0, command.indexOf(" ")).trim());
            command = command.substring(command.indexOf(" ") + 1);
            String path = command.trim();
            try {
                String result = this.doAction("siteCommand", "PGP_HEADER_SIZE " + size + " " + path, "");
                return "214 " + result;
            }
            catch (Exception e) {
                return "500 " + e;
            }
        }
        if (command.startsWith("SITE BLOCK_UPLOADS")) {
            try {
                String result = this.doAction("siteCommand", "BLOCK_UPLOADS", "");
                return "214 " + result;
            }
            catch (Exception e) {
                return "500 " + e;
            }
        }
        if (command.startsWith("SITE TIME")) {
            try {
                String result = this.doAction("getTime", "", "");
                return "214 " + result;
            }
            catch (Exception e) {
                return "500 " + e;
            }
        }
        if (command.startsWith("SITE QUOTA")) {
            try {
                String result = this.doAction("getQuota", command.substring("SITE QUOTA".length()).trim(), "");
                return "214 " + result;
            }
            catch (Exception e) {
                return "500 " + e;
            }
        }
        if (command.startsWith("ABOR")) {
            try {
                String result = this.doAction("siteCommand", command, "");
                return "214 " + result;
            }
            catch (Exception e) {
                return "500 " + e;
            }
        }
        return "500 unknown command:" + command;
    }

    public InputStream getSegmentedDownload(String path, long startPos, long endPos) throws Exception {
        URLConnection urlc = URLConnection.openConnection(new VRL(String.valueOf(this.url) + "WebInterface/function/"), this.config);
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Cookie", "CrushAuth=" + this.config.getProperty("crushAuth", "") + ";");
        if (startPos > 0L || endPos >= 0L) {
            urlc.setRequestProperty("Range", "bytes=" + startPos + "-" + (endPos >= 0L ? String.valueOf(endPos) : ""));
        }
        urlc.setUseCaches(false);
        urlc.setReceiveCompression(this.config.getProperty("receive_compressed", "false").equals("true") && startPos <= 0L);
        urlc.setDoOutput(true);
        urlc.getOutputStream().write(("command=download&c2f=" + this.config.getProperty("crushAuth", "").substring(this.config.getProperty("crushAuth", "").length() - 4) + "&path=" + Common.url_encode(path) + "&transfer_type=download").getBytes("UTF8"));
        urlc.setReadTimeout(Integer.parseInt(this.config.getProperty("timeout", "0")));
        urlc.getResponseCode();
        if (urlc.responseCode > 299) {
            this.log("download-end | " + path + " | " + startPos + " | " + endPos + " | " + true + " | ERROR:" + urlc.responseCode + ":" + urlc.message);
            throw new IOException(String.valueOf(urlc.responseCode) + ":" + urlc.message);
        }
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        String transfer_key = result.substring(result.indexOf("<response>") + "<response>".length(), result.indexOf("</response>")).trim();
        urlc.disconnect();
        Properties transfer_lock = Common.getConnectedSocks(false);
        Socket sock1 = (Socket)transfer_lock.remove("sock1");
        Socket sock2 = (Socket)transfer_lock.remove("sock2");
        transfer_lock.put("num", "1");
        String safe_url = new VRL(this.url).safe();
        Vector grabChunkThreads = new Vector();
        Properties chunks = new Properties();
        this.in = new BufferedInputStream(sock2.getInputStream());
        Properties shared_status = new Properties();
        class ChunkStreamer
        implements Runnable {
            Properties shared_status = null;
            private final /* synthetic */ Socket val$sock1;
            private final /* synthetic */ Properties val$chunks;
            private final /* synthetic */ String val$safe_url;
            private final /* synthetic */ String val$path;
            private final /* synthetic */ Properties val$transfer_lock;

            public ChunkStreamer(Properties shared_status, Socket socket, Properties properties, String string, String string2, Properties properties2) {
                this.val$sock1 = socket;
                this.val$chunks = properties;
                this.val$safe_url = string;
                this.val$path = string2;
                this.val$transfer_lock = properties2;
                this.shared_status = shared_status;
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             * Unable to fully structure code
             */
            @Override
            public void run() {
                block32: {
                    block31: {
                        num = 1;
                        sock_out = null;
                        try {
                            try {
                                sock_out = new BufferedOutputStream(this.val$sock1.getOutputStream());
                                delay = 1;
                                pos = 0L;
                                last_chunk = System.currentTimeMillis();
                                while (System.currentTimeMillis() - last_chunk < 300000L && HTTPClient.this.in != null) {
                                    Thread.currentThread().setName("Download chunkStreamer:" + this.val$safe_url + ":" + this.val$path + ":memory_chunks=" + this.val$chunks.size() + ":num=" + num + ":pos=" + pos);
                                    if (delay > 1000) {
                                        delay = 1000;
                                    }
                                    if (this.val$transfer_lock.containsKey("total_chunks") && num >= Integer.parseInt(this.val$transfer_lock.getProperty("total_chunks"))) break block31;
                                    if (this.val$chunks.containsKey(String.valueOf(num))) {
                                        last_chunk = System.currentTimeMillis();
                                        delay = 1;
                                        b = null;
                                        var9_8 = this.val$transfer_lock;
                                        synchronized (var9_8) {
                                            b = (byte[])this.val$chunks.remove(String.valueOf(num));
                                            this.val$transfer_lock.put("pending_bytes", String.valueOf(Long.parseLong(this.val$transfer_lock.getProperty("pending_bytes", "0")) - (long)b.length));
                                        }
                                        var9_8 = HTTPClient.ram_pending_bytes_multisegment;
                                        synchronized (var9_8) {
                                            HTTPClient.ram_pending_bytes_multisegment.put("download", String.valueOf(Long.parseLong(HTTPClient.ram_pending_bytes_multisegment.getProperty("download", "0")) - (long)b.length));
                                        }
                                        pos += (long)b.length;
                                        sock_out.write(b);
                                        ++num;
                                        last_chunk = System.currentTimeMillis();
                                        continue;
                                    }
                                    Thread.sleep(delay++);
                                }
                                break block31;
                            }
                            catch (Exception e) {
                                Common.log("HTTP_CLIENT", 1, e);
                                try {
                                    sock_out.close();
                                    this.val$sock1.close();
                                }
                                catch (IOException var11_9) {
                                    // empty catch block
                                }
                                keys = this.val$chunks.keys();
                                ** while (keys.hasMoreElements())
                            }
                        }
                        catch (Throwable var10_24) {
                            try {
                                sock_out.close();
                                this.val$sock1.close();
                            }
                            catch (IOException keys) {
                                // empty catch block
                            }
                            keys = this.val$chunks.keys();
                            ** while (keys.hasMoreElements())
                        }
lbl-1000:
                        // 1 sources

                        {
                            key = "" + keys.nextElement();
                            b = (byte[])this.val$chunks.remove(key);
                            var14_21 = HTTPClient.ram_pending_bytes_multisegment;
                            synchronized (var14_21) {
                                HTTPClient.ram_pending_bytes_multisegment.put("download", String.valueOf(Long.parseLong(HTTPClient.ram_pending_bytes_multisegment.getProperty("download", "0")) - (long)b.length));
                                continue;
                            }
                        }
lbl64:
                        // 1 sources

                        this.shared_status.put("status", "done");
                        break block32;
lbl-1000:
                        // 1 sources

                        {
                            key = "" + keys.nextElement();
                            b = (byte[])this.val$chunks.remove(key);
                            var14_22 = HTTPClient.ram_pending_bytes_multisegment;
                            synchronized (var14_22) {
                                HTTPClient.ram_pending_bytes_multisegment.put("download", String.valueOf(Long.parseLong(HTTPClient.ram_pending_bytes_multisegment.getProperty("download", "0")) - (long)b.length));
                                continue;
                            }
                        }
lbl86:
                        // 1 sources

                        this.shared_status.put("status", "done");
                        throw var10_24;
                    }
                    try {
                        sock_out.close();
                        this.val$sock1.close();
                    }
                    catch (IOException keys) {
                        // empty catch block
                    }
                    keys = this.val$chunks.keys();
                    while (keys.hasMoreElements()) {
                        key = "" + keys.nextElement();
                        b = (byte[])this.val$chunks.remove(key);
                        var14_23 = HTTPClient.ram_pending_bytes_multisegment;
                        synchronized (var14_23) {
                            HTTPClient.ram_pending_bytes_multisegment.put("download", String.valueOf(Long.parseLong(HTTPClient.ram_pending_bytes_multisegment.getProperty("download", "0")) - (long)b.length));
                        }
                    }
                    this.shared_status.put("status", "done");
                }
            }
        }
        Worker.startWorker(new ChunkStreamer(shared_status, sock1, chunks, safe_url, path, transfer_lock));
        class ChunksController
        implements Runnable {
            int max_threads = 0;
            int thread_count_needed = 1;
            long max_bytes = 0x2800000L;
            Properties shared_status = null;
            private final /* synthetic */ String val$safe_url;
            private final /* synthetic */ String val$path;
            private final /* synthetic */ Properties val$chunks;
            private final /* synthetic */ Properties val$transfer_lock;
            private final /* synthetic */ Vector val$grabChunkThreads;
            private final /* synthetic */ String val$transfer_key;

            public ChunksController(Properties shared_status, int max_threads, String string, String string2, Properties properties, Properties properties2, Vector vector, String string3) {
                this.val$safe_url = string;
                this.val$path = string2;
                this.val$chunks = properties;
                this.val$transfer_lock = properties2;
                this.val$grabChunkThreads = vector;
                this.val$transfer_key = string3;
                this.shared_status = shared_status;
                this.max_threads = max_threads;
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                try {
                    while (this.shared_status.getProperty("status", "").equals("")) {
                        Thread.currentThread().setName("Download chunkStreamerController:" + this.val$safe_url + ":" + this.val$path + ":memory_chunks=" + this.val$chunks.size() + " GrabChunkMaxThreads=" + this.max_threads);
                        this.thread_count_needed = Integer.parseInt(this.val$transfer_lock.getProperty("thread_count_needed", "1"));
                        this.max_bytes = 0x2800000L * (long)(this.val$grabChunkThreads.size() + 1);
                        Properties properties = this.val$transfer_lock;
                        synchronized (properties) {
                            if (Long.parseLong(this.val$transfer_lock.getProperty("pending_bytes", "0")) > this.max_bytes) {
                                --this.thread_count_needed;
                                if (this.thread_count_needed < 0) {
                                    this.thread_count_needed = 0;
                                }
                            }
                            if (Long.parseLong(this.val$transfer_lock.getProperty("pending_bytes", "0")) < this.max_bytes && (this.val$grabChunkThreads.size() < this.thread_count_needed || this.thread_count_needed == 0) && this.thread_count_needed < this.max_threads) {
                                ++this.thread_count_needed;
                            }
                            if (this.thread_count_needed > 0 && Integer.parseInt(System.getProperty("crushftp.ram_used_percent", "0")) > 60) {
                                this.thread_count_needed = 1;
                            }
                            this.val$transfer_lock.put("thread_count_needed", String.valueOf(this.thread_count_needed));
                        }
                        if (!(this.val$grabChunkThreads.size() >= this.thread_count_needed || this.val$transfer_lock.containsKey("total_chunks") && Integer.parseInt(this.val$transfer_lock.getProperty("num", "1")) >= Integer.parseInt(this.val$transfer_lock.getProperty("total_chunks")))) {
                            class GrabChunk2
                            implements Runnable {
                                String transfer_path_for_mem_dumps2;
                                int thread_num;
                                Vector grabChunkThreads2;
                                private final /* synthetic */ Vector val$grabChunkThreads;
                                private final /* synthetic */ Properties val$transfer_lock;
                                private final /* synthetic */ String val$safe_url;
                                private final /* synthetic */ String val$path;
                                private final /* synthetic */ String val$transfer_key;
                                private final /* synthetic */ Properties val$chunks;

                                public GrabChunk2(String string, Vector vector, Properties properties, String string2, String string3, Properties properties2) {
                                    this.val$path = string;
                                    this.val$grabChunkThreads = vector;
                                    this.val$transfer_lock = properties;
                                    this.val$safe_url = string2;
                                    this.val$transfer_key = string3;
                                    this.val$chunks = properties2;
                                    this.transfer_path_for_mem_dumps2 = string;
                                    this.thread_num = 0;
                                    this.grabChunkThreads2 = vector;
                                    this.thread_num = vector.size() + 1;
                                }

                                /*
                                 * WARNING - Removed try catching itself - possible behaviour change.
                                 */
                                @Override
                                public void run() {
                                    try {
                                        block22: while (true) {
                                            if (HTTPClient.this.in == null) {
                                                return;
                                            }
                                            Vector vector = this.val$grabChunkThreads;
                                            synchronized (vector) {
                                                if (this.val$grabChunkThreads.size() > Integer.parseInt(this.val$transfer_lock.getProperty("thread_count_needed")) || Long.parseLong(this.val$transfer_lock.getProperty("pending_bytes", "0")) > 0x2800000L * (long)(this.val$grabChunkThreads.size() + 1)) {
                                                    this.val$grabChunkThreads.remove(this);
                                                    break;
                                                }
                                            }
                                            int num = 0;
                                            Properties properties = this.val$transfer_lock;
                                            synchronized (properties) {
                                                if (this.val$transfer_lock.getProperty("num") == null) {
                                                    break;
                                                }
                                                num = Integer.parseInt(this.val$transfer_lock.getProperty("num"));
                                                if (this.val$transfer_lock.containsKey("total_chunks") && num >= Integer.parseInt(this.val$transfer_lock.getProperty("total_chunks"))) {
                                                    break;
                                                }
                                                this.val$transfer_lock.put("num", String.valueOf(num + 1));
                                            }
                                            URLConnection urlc = null;
                                            int chunk_error_count = 0;
                                            int scale_back_delay = 0;
                                            while (true) {
                                                block33: {
                                                    if (chunk_error_count >= 60) continue block22;
                                                    if (HTTPClient.this.in != null) break block33;
                                                    return;
                                                }
                                                try {
                                                    Thread.currentThread().setName("Download chunkStreamer:grabChunk:" + this.val$safe_url + ":" + this.val$path + ":num=" + num + ":chunk_error_count=" + chunk_error_count + " Thread=" + this.thread_num + "/" + this.val$transfer_lock.getProperty("thread_count_needed", "20"));
                                                    urlc = URLConnection.openConnection(new VRL(String.valueOf(HTTPClient.this.url) + "D/" + this.val$transfer_key + "~" + num), HTTPClient.this.config);
                                                    HTTPClient.this.openConnections.addElement(urlc);
                                                    urlc.setRequestMethod("GET");
                                                    urlc.setRequestProperty("Cookie", "CrushAuth=" + HTTPClient.this.config.getProperty("crushAuth", "") + ";");
                                                    urlc.setUseCaches(false);
                                                    urlc.setReceiveCompression(false);
                                                    urlc.setDoOutput(false);
                                                    urlc.setReadTimeout(Integer.parseInt(HTTPClient.this.config.getProperty("timeout", "0")));
                                                    if (Common.V() >= 10) {
                                                        urlc.enableHighLatencyBuffer();
                                                    }
                                                    urlc.getResponseCode();
                                                    if (urlc.responseCode == 404) {
                                                        urlc.disconnect();
                                                        HTTPClient.this.openConnections.remove(urlc);
                                                        Thread.sleep(scale_back_delay += 100);
                                                        continue;
                                                    }
                                                    if (urlc.responseCode == 200) {
                                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                        Common.copyStreams(urlc.getInputStream(), baos, true, true);
                                                        urlc.disconnect();
                                                        HTTPClient.this.openConnections.remove(urlc);
                                                        byte[] b = baos.toByteArray();
                                                        Properties properties2 = ram_pending_bytes_multisegment;
                                                        synchronized (properties2) {
                                                            ram_pending_bytes_multisegment.put("download", String.valueOf(Long.parseLong(ram_pending_bytes_multisegment.getProperty("download", "0")) + (long)b.length));
                                                        }
                                                        properties2 = this.val$transfer_lock;
                                                        synchronized (properties2) {
                                                            this.val$chunks.put(String.valueOf(num), b);
                                                            if (b.length == 0 && !this.val$transfer_lock.containsKey("total_chunks")) {
                                                                this.val$transfer_lock.put("total_chunks", String.valueOf(num));
                                                            }
                                                            this.val$transfer_lock.put("pending_bytes", String.valueOf(Long.parseLong(this.val$transfer_lock.getProperty("pending_bytes", "0")) + (long)b.length));
                                                            continue block22;
                                                        }
                                                    }
                                                    throw new IOException(String.valueOf(urlc.getResponseCode()) + ":" + urlc.getResponseMessage());
                                                }
                                                catch (Exception e) {
                                                    ++chunk_error_count;
                                                    Common.log("HTTP_CLIENT", 1, e);
                                                    try {
                                                        Thread.sleep(1000L);
                                                    }
                                                    catch (Exception exception) {
                                                        // empty catch block
                                                    }
                                                }
                                            }
                                            break;
                                        }
                                    }
                                    finally {
                                        this.val$grabChunkThreads.remove(this);
                                    }
                                }
                            }
                            GrabChunk2 gb = new GrabChunk2(this.val$path, this.val$grabChunkThreads, this.val$transfer_lock, this.val$safe_url, this.val$transfer_key, this.val$chunks);
                            this.val$grabChunkThreads.addElement(gb);
                            Worker.startWorker(gb);
                        }
                        this.max_bytes = 0x2800000L * (long)(this.val$grabChunkThreads.size() + 1);
                        this.val$transfer_lock.put("max_bytes", String.valueOf(this.max_bytes));
                        this.val$transfer_lock.put("current_thread_count", String.valueOf(this.val$grabChunkThreads.size()));
                        Thread.sleep(1000L);
                    }
                }
                catch (Exception e) {
                    Common.log("HTTP_CLIENT", 1, e);
                }
            }
        }
        Worker.startWorker(new ChunksController(shared_status, Integer.parseInt(this.config.getProperty("multi_segmented_download_threads", "20")), safe_url, path, chunks, transfer_lock, grabChunkThreads, transfer_key));
        return this.in;
    }
}

