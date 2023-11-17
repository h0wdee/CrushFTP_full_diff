/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jdom.Document
 *  org.jdom.Element
 *  org.jdom.input.SAXBuilder
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
import java.io.Reader;
import java.io.StringReader;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Vector;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class AzureClient
extends GenericClient {
    String sas_sv = "";
    Properties stat_cache = new Properties();
    String sas_token = null;

    public AzureClient(String url, String header, Vector log) {
        super(header, log);
        this.fields = new String[]{"container_type", "username", "password", "upload_blob_type", "share", "ignore_mdtm_on_list", "data_lake_storagegen2", "block_blob_upload_threads", "block_blob_upload_buffer_size", "uploaded_by", "uploaded_md5", "timeout", "sas_token"};
        this.url = new VRL(url).toString();
        String[] parts = url.split("/");
        String container_type = "";
        if (this.url.contains("@file.core.windows.net")) {
            container_type = "@file.core.windows.net/";
            this.config.put("container_type", "share");
        } else if (this.url.contains("@blob.core.windows.net")) {
            container_type = "@blob.core.windows.net/";
            this.config.put("container_type", "blob");
            if (this.config.getProperty("upload_blob_type", "").equals("")) {
                this.config.put("upload_blob_type", "appendblob");
            }
        } else {
            new Exception("Bad host! It should be either - file.core.windows.net - for azure share file container or - blob.core.windows.net - for azure blob container.");
        }
        int share_index = this.url.indexOf(container_type) + container_type.length();
        if (this.url.indexOf("/", share_index) < 0) {
            this.url = String.valueOf(this.url) + "/";
        }
        String share = this.url.substring(share_index, this.url.indexOf("/", share_index));
        this.config.put("share", share);
        String relative_path = this.url.substring(share_index + share.length());
        this.config.put("relative_path", relative_path);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public String login2(String username, String password, String clientid) throws Exception {
        this.config.put("username", username.trim());
        this.config.put("password", Common.url_decode(VRL.vrlDecode(password.trim())));
        if (!this.getSASToken().equals("") && this.config.getProperty("container_type").equals("blob") && !this.config.getProperty("relative_path", "/").equals("/")) {
            int x = 0;
            while (x < 3) {
                try {
                    String result = this.list_api_call("/" + this.config.getProperty("share", "") + this.config.getProperty("relative_path", "/"), this.config.getProperty("relative_path", "/").substring(1), "", 1);
                    this.config.put("logged_out", "false");
                    return "Success";
                }
                catch (UnknownHostException e) {
                    this.log("ERROR : Bad credentials : Invalid User name! Error message: " + e);
                    Thread.sleep(5000L);
                    if (x == 2) {
                        throw e;
                    }
                }
                catch (Exception e) {
                    this.log(e);
                    throw e;
                }
                ++x;
            }
        }
        String restype = "?restype=";
        if (!this.getSASToken().equals("") && !this.config.getProperty("container_type").equals("share")) {
            restype = "?comp=list&delimiter=%2F&restype=";
        }
        restype = this.config.getProperty("container_type").equals("blob") ? "?comp=list&delimiter=%2F&restype=container" : "?comp=metadata&restype=directory";
        int x = 0;
        while (x < 3) {
            block20: {
                URLConnection urlc = null;
                urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("username") + this.getUrl() + "/" + this.config.getProperty("share", "") + restype + this.getSASToken()), this.config);
                urlc.setRequestMethod("GET");
                urlc.setDoInput(true);
                urlc.setDoOutput(true);
                urlc.setUseCaches(false);
                this.signRequestSK(urlc);
                String result = "";
                try {
                    result = URLConnection.consumeResponse(urlc.getInputStream());
                }
                catch (UnknownHostException e) {
                    this.log("ERROR : Bad credentials : Invalid User name! Error message: " + e);
                    Thread.sleep(5000L);
                    if (x == 2) {
                        throw e;
                    }
                    if (urlc != null) {
                        urlc.disconnect();
                    }
                    break block20;
                }
                if (urlc.getResponseCode() == 302) {
                    String location = urlc.getHeaderField("Location");
                    urlc.disconnect();
                    urlc = URLConnection.openConnection(new VRL(location), this.config);
                    urlc.setRequestMethod("GET");
                    urlc.setDoInput(true);
                    urlc.setDoOutput(true);
                    urlc.setUseCaches(false);
                    this.signRequestSK(urlc);
                }
                if (urlc.getResponseCode() == 200) {
                    this.config.put("logged_out", "false");
                    return "Success";
                }
                if (urlc.getResponseCode() >= 200) {
                    if (urlc.getResponseCode() <= 299) return "Failure!";
                }
                Properties error_config = new Properties();
                error_config.put("login_error", "true");
                error_config.put("sas_token", this.getSASToken().equals("") ? "false" : "true");
                result = this.getErrorInfo(result, urlc.getResponseCode(), urlc.getConfig("signing_header"), error_config);
                this.log("AZURE_CLIENT", 1, String.valueOf(urlc.getResponseCode()) + " " + result);
                throw new IOException(result);
            }
            ++x;
        }
        return "Failure!";
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        if (!path.endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        String prefix = "";
        if (this.config.getProperty("container_type").equals("blob") && !path.equals("/" + this.config.getProperty("share") + "/")) {
            prefix = path.substring(path.indexOf("/" + this.config.getProperty("share") + "/") + this.config.getProperty("share").length() + 2, path.length());
        }
        String result = "";
        int xx = 0;
        while (xx < 6) {
            try {
                result = this.list_api_call(path, prefix, "", 5000);
                break;
            }
            catch (Exception e) {
                if (xx == 5) {
                    throw e;
                }
                if (e instanceof UnknownHostException) {
                    this.log(e);
                    try {
                        Thread.sleep(5000L);
                    }
                    catch (InterruptedException interruptedException) {}
                } else {
                    Thread.sleep(150L);
                }
                ++xx;
            }
        }
        this.log("AZURE_CLIENT", 2, "List : all result : " + result);
        String marker = "";
        if (result.contains("<NextMarker>") && result.contains("</NextMarker>") && result.indexOf("<NextMarker>") < result.indexOf("</NextMarker>")) {
            marker = result.substring(result.indexOf("<NextMarker>") + "<NextMarker>".length(), result.indexOf("</NextMarker>"));
        }
        if (this.config.getProperty("container_type").equals("share")) {
            this.parseShareList(path, list, result);
        } else if (this.config.getProperty("container_type").equals("blob")) {
            this.parseBlobList(prefix, path, list, result, false);
        }
        if (!marker.equals("")) {
            int x = 0;
            while (x < 1000) {
                if (this.config.getProperty("logged_out", "false").equals("true")) {
                    throw new Exception("Error: Cancel dir listing. The client is already closed.");
                }
                int xx2 = 0;
                while (xx2 < 6) {
                    try {
                        result = this.list_api_call(path, prefix, marker, 5000);
                        break;
                    }
                    catch (Exception e) {
                        if (xx2 == 5) {
                            throw e;
                        }
                        if (e instanceof UnknownHostException) {
                            this.log(e);
                            try {
                                Thread.sleep(5000L);
                            }
                            catch (InterruptedException interruptedException) {}
                        } else {
                            Thread.sleep(150L);
                        }
                        ++xx2;
                    }
                }
                marker = "";
                if (result.contains("<NextMarker>") && result.contains("</NextMarker>") && result.indexOf("<NextMarker>") < result.indexOf("</NextMarker>")) {
                    marker = result.substring(result.indexOf("<NextMarker>") + "<NextMarker>".length(), result.indexOf("</NextMarker>"));
                }
                if (this.config.getProperty("container_type").equals("share")) {
                    this.parseShareList(path, list, result);
                } else if (this.config.getProperty("container_type").equals("blob")) {
                    this.parseBlobList(prefix, path, list, result, false);
                }
                if (marker.equals("")) break;
                ++x;
            }
        }
        return list;
    }

    private String list_api_call(String path, String prefix, String marker, int max_result) throws Exception {
        String restype = "";
        String url_path = path;
        if (this.config.getProperty("container_type").equals("blob")) {
            restype = !marker.equals("") ? "delimiter=%2F&marker=" + marker + "&maxresults=" + max_result + "&prefix=" + this.double_encode(prefix).replace("/", "%2F") + "&restype=container" : "delimiter=%2F&maxresults=" + max_result + "&prefix=" + this.double_encode(prefix).replace("/", "%2F") + "&restype=container";
            url_path = "/" + this.config.getProperty("share") + "/";
        } else if (this.config.getProperty("container_type").equals("share")) {
            restype = "include=Timestamps&maxresults=" + max_result + "&restype=directory";
            if (!this.getSASToken().equals("") && !this.sas_sv.equals("") && this.isOLderSV(this.sas_sv, "2020-04-08")) {
                restype = "maxresults=5000&restype=directory";
            }
            if (!marker.equals("")) {
                restype = "include=Timestamps&marker=" + marker + "&maxresults=" + max_result + "&restype=directory";
                if (!this.getSASToken().equals("") && !this.sas_sv.equals("") && this.isOLderSV(this.sas_sv, "2020-04-08")) {
                    restype = "marker=" + marker + "&maxresults=" + max_result + "&restype=directory";
                }
            }
        }
        String result = "";
        URLConnection urlc = null;
        try {
            urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("username") + this.getUrl() + this.double_encode(url_path) + "?comp=list&" + restype + this.getSASToken()), this.config);
            urlc.setRequestMethod("GET");
            urlc.setUseCaches(false);
            urlc.setRemoveDoubleEncoding(true);
            this.signRequestSK(urlc);
            int code = urlc.getResponseCode();
            if (code == 302) {
                String location = urlc.getHeaderField("Location");
                urlc.disconnect();
                urlc = URLConnection.openConnection(new VRL(location), this.config);
                urlc.setRequestMethod("GET");
                urlc.setUseCaches(false);
                this.signRequestSK(urlc);
            }
            result = URLConnection.consumeResponse(urlc.getInputStream());
            code = urlc.getResponseCode();
            if (code < 200 || code > 299) {
                Properties error_config = new Properties();
                error_config.put("login_error", "true");
                error_config.put("sas_token", this.getSASToken().equals("") ? "false" : "true");
                result = this.getErrorInfo(result, urlc.getResponseCode(), urlc.getConfig("signing_header"), error_config);
                this.log("AZURE_CLIENT", 1, String.valueOf(urlc.getResponseCode()) + " " + result);
                throw new IOException(result);
            }
        }
        finally {
            if (urlc != null) {
                urlc.disconnect();
            }
        }
        return result;
    }

    private void parseBlobList(String prefix, String path, Vector list, String result, boolean use_original_name) {
        if (result.contains("<Blobs>") && result.contains("</Blobs>") && result.indexOf("<Blobs>") < result.indexOf("</Blobs>")) {
            result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + result.substring(result.indexOf("<Blobs>"), result.indexOf("</Blobs>") + "</Blobs>".length());
            SimpleDateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            try {
                SAXBuilder sax = Common.getSaxBuilder();
                Document doc = sax.build((Reader)new StringReader(result));
                for (Element element : doc.getRootElement().getChildren()) {
                    Properties p = null;
                    Iterator i2 = element.getChildren().iterator();
                    if (((Element)element.getChildren().get(0)).getText().equals(prefix)) continue;
                    SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                    String item_name = "";
                    String item_path = "";
                    boolean is_dir = false;
                    Date date = new Date();
                    String size = "0";
                    while (i2.hasNext()) {
                        Element element2 = (Element)i2.next();
                        if (element2.getName().equals("Name") && !element2.getText().equals("")) {
                            item_name = Common.last(element2.getText());
                            if (item_name.endsWith("/")) {
                                item_name = item_name.substring(0, item_name.length() - 1);
                                is_dir = true;
                            }
                            if (use_original_name) {
                                String string = item_path = element2.getText().startsWith("/") ? element2.getText() : "/" + element2.getText();
                                if (item_path.endsWith("/")) {
                                    item_path = item_path.substring(0, item_path.length() - 1);
                                }
                            } else {
                                item_path = String.valueOf(path) + item_name;
                            }
                        }
                        if (!element2.getName().equals("Properties")) continue;
                        for (Element element3 : element2.getChildren()) {
                            if (element3.getName().equals("Content-Length")) {
                                size = element3.getText();
                            }
                            if (!element3.getName().equals("Last-Modified")) continue;
                            try {
                                date = fmt.parse(element3.getText());
                            }
                            catch (Exception e) {
                                this.log("AZURE_CLIENT", 2, e);
                            }
                        }
                    }
                    String line = String.valueOf(is_dir ? "d" : "-") + "rwxrwxrwx   1    owner   group   " + size + "   " + yyyyMMddHHmmss.format(date) + "   " + this.dd.format(date) + " " + this.yyyy.format(date) + " " + item_path;
                    p = AzureClient.parseStat(line);
                    String url_path = String.valueOf(path) + p.getProperty("name");
                    p.put("url", "azure://" + this.config.getProperty("username") + ":" + VRL.vrlEncode(this.config.getProperty("password")) + "@blob.core.windows.net" + url_path);
                    p.put("owner", "owner");
                    p.put("group", "group");
                    this.log("AZURE_CLIENT", 2, "List : " + path + p.getProperty("name"));
                    list.add(p);
                }
            }
            catch (Exception e) {
                this.log(e);
                this.log("AZURE_CLIENT", 1, e);
            }
        }
    }

    private void parseShareList(String path, Vector list, String result) {
        if (result.contains("<Entries>") && result.contains("</Entries>") && result.indexOf("<Entries>") < result.indexOf("</Entries>")) {
            result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + result.substring(result.indexOf("<Entries>"), result.indexOf("</Entries>") + "</Entries>".length());
            Vector<Properties> list2 = new Vector<Properties>();
            try {
                SAXBuilder sax = Common.getSaxBuilder();
                Document doc = sax.build((Reader)new StringReader(result));
                boolean has_mdtm_info = false;
                SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                for (Element element : doc.getRootElement().getChildren()) {
                    Properties p = new Properties();
                    String item_name = "";
                    String item_path = "";
                    boolean is_dir = false;
                    Date date = new Date();
                    String size = "0";
                    String last_modified = "";
                    if (!element.getName().equals("File")) {
                        is_dir = true;
                    }
                    Iterator i2 = element.getChildren().iterator();
                    p.put("size", "0");
                    while (i2.hasNext()) {
                        Element element2 = (Element)i2.next();
                        if (element2.getName().equals("Name")) {
                            item_name = element2.getText();
                            item_path = String.valueOf(path) + element2.getText();
                        }
                        if (!element2.getName().equals("Properties") || element2.getChildren().size() <= 0) continue;
                        Element size_elememnt = element2.getChild("Content-Length");
                        if (size_elememnt != null) {
                            size = size_elememnt.getText();
                        }
                        if (element2.getChild("LastWriteTime") == null) continue;
                        Element last_modified_element = element2.getChild("LastWriteTime");
                        last_modified = last_modified_element.getText();
                        has_mdtm_info = true;
                    }
                    String line = String.valueOf(is_dir ? "d" : "-") + "rwxrwxrwx   1    owner   group   " + size + "   " + yyyyMMddHHmmss.format(date) + "   " + this.dd.format(date) + " " + this.yyyy.format(date) + " " + item_path;
                    p = AzureClient.parseStat(line);
                    p.put("url", "azure://" + this.config.getProperty("username") + ":" + VRL.vrlEncode(this.config.getProperty("password")) + "@file.core.windows.net" + path + p.getProperty("name"));
                    p.put("owner", "owner");
                    p.put("group", "group");
                    if (is_dir) {
                        p.put("check_all_recursive_deletes", "true");
                    }
                    if (has_mdtm_info) {
                        try {
                            this.parseLastModified(p, last_modified);
                        }
                        catch (Exception e) {
                            this.log("AZURE_CLIENT", 2, e);
                        }
                    }
                    if (p.getProperty("modified", "").equals("")) {
                        p.put("modified", String.valueOf(System.currentTimeMillis()));
                    }
                    this.log("AZURE_CLIENT", 2, "List : " + path + p.getProperty("name"));
                    list2.add(p);
                }
                if (!this.config.getProperty("ignore_mdtm_on_list", "false").equals("true") && !has_mdtm_info) {
                    int thread_count = Integer.parseInt(System.getProperty("crushftp.azure_share_list_threads_count", "10"));
                    if (thread_count > 100) {
                        thread_count = 100;
                    }
                    Vector completed_mdtm_list = new Vector();
                    Vector running_threads = new Vector();
                    String sasToken = this.getSASToken();
                    int x = 0;
                    while (x < list2.size()) {
                        Properties item = (Properties)list2.elementAt(x);
                        this.getMDTM_of_fileShare(item, path, sasToken, completed_mdtm_list, running_threads);
                        int xx = 0;
                        while (xx < 600 && running_threads.size() >= thread_count) {
                            Thread.sleep(100L);
                            if (xx > 598) {
                                throw new Exception("Error : Azure Share directory listing : 60 second timeout while waiting for get mdmt infos to complete... Path : " + path + " index:" + xx);
                            }
                            ++xx;
                        }
                        ++x;
                    }
                    int xx = 0;
                    while (xx < 600 && completed_mdtm_list.size() < list2.size()) {
                        Thread.sleep(100L);
                        if (xx > 598) {
                            throw new Exception("Error : Azure Share directory listing : 60 second timeout while waiting for get mdmt infos to complete... Path : " + path);
                        }
                        ++xx;
                    }
                    completed_mdtm_list.clear();
                }
                list.addAll(list2);
            }
            catch (Exception e) {
                this.log(e);
                this.log("AZURE_CLIENT", 1, e);
            }
        }
    }

    public void getMDTM_of_fileShare(final Properties item, String path, String sasToken, final Vector completed_mdtm_list, final Vector running_threads) throws Exception {
        Runnable r = new Runnable(){

            /*
             * Exception decompiling
             */
            @Override
            public void run() {
                /*
                 * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
                 * 
                 * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [2[TRYBLOCK]], but top level block is 3[TRYBLOCK]
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
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
                 *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
                 *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
                 *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
                 *     at org.benf.cfr.reader.Main.main(Main.java:54)
                 */
                throw new IllegalStateException("Decompilation failed");
            }
        };
        running_threads.addElement(r);
        Worker.startWorker(r, "Azure Share get mdtm info for : " + path);
    }

    private void parseLastModified(Properties mdtm_status, String last_modified) throws Exception {
        SimpleDateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        SimpleDateFormat MMfmt = new SimpleDateFormat("MM", Locale.US);
        SimpleDateFormat ddfmt = new SimpleDateFormat("dd", Locale.US);
        SimpleDateFormat yyyyfmt = new SimpleDateFormat("yyyy", Locale.US);
        Date d = new Date(0L);
        String time_zone = "";
        if (!last_modified.equals("")) {
            if (last_modified.indexOf(" ") > 0) {
                String[] dtz = last_modified.split(" ");
                time_zone = dtz[dtz.length - 1];
            }
            if (last_modified.indexOf("-") > 0) {
                fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            }
            if (!time_zone.equals("")) {
                fmt.setTimeZone(TimeZone.getTimeZone(time_zone));
            }
            try {
                d = fmt.parse(last_modified);
            }
            catch (Exception e) {
                this.log("AZURE_CLIENT", 1, e);
            }
        }
        if (!time_zone.equals("")) {
            MMfmt.setTimeZone(TimeZone.getTimeZone(time_zone));
        }
        mdtm_status.put("month", MMfmt.format(d));
        if (!time_zone.equals("")) {
            ddfmt.setTimeZone(TimeZone.getTimeZone(time_zone));
        }
        mdtm_status.put("day", ddfmt.format(d));
        if (!time_zone.equals("")) {
            ddfmt.setTimeZone(TimeZone.getTimeZone(time_zone));
        }
        mdtm_status.put("time_or_year", yyyyfmt.format(d));
        mdtm_status.put("modified", String.valueOf(d.getTime()));
        this.log("AZURE_CLIENT", 2, "List : parsed modified :" + d.getTime());
    }

    /*
     * Enabled aggressive exception aggregation
     */
    @Override
    public Properties stat(String path) throws Exception {
        if (path.endsWith(":filetree")) {
            path = path.substring(0, path.indexOf(":filetree") - 1);
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        this.log("AZURE_CLIENT", 2, "Stat : " + path);
        if (this.stat_cache.containsKey(path)) {
            Properties stat = (Properties)this.stat_cache.get(path);
            if (System.currentTimeMillis() - Long.parseLong(stat.getProperty("time", "0")) < 10000L) {
                return (Properties)stat.get("item");
            }
            this.stat_cache.remove(path);
        }
        if (path.equals("/" + this.config.getProperty("share"))) {
            SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
            String line = "drwxrwxrwx   1    owner   group   0   " + yyyyMMddHHmmss.format(new Date()) + "   " + this.dd.format(new Date()) + " " + this.yyyy.format(new Date()) + " " + path;
            Properties p_root = AzureClient.parseStat(line);
            String host = "";
            if (this.config.getProperty("container_type").equals("share")) {
                host = "file.core.windows.net";
            } else if (this.config.getProperty("container_type").equals("blob")) {
                host = "blob.core.windows.net";
            }
            p_root.put("url", "azure://" + this.config.getProperty("username") + ":" + VRL.vrlEncode(this.config.getProperty("password")) + "@" + host + path);
            p_root.put("owner", "owner");
            p_root.put("group", "group");
            return p_root;
        }
        String params = "";
        if (!this.getSASToken().equals("")) {
            params = "?" + this.getSASToken().substring(1);
        }
        Properties p = new Properties();
        int code = 0;
        int x = 0;
        while (x < 3) {
            URLConnection urlc = null;
            try {
                urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("username") + this.getUrl() + this.double_encode(path) + params), this.config);
                urlc.setRequestMethod("HEAD");
                urlc.setDoInput(true);
                urlc.setUseCaches(false);
                urlc.setRemoveDoubleEncoding(true);
                this.signRequestSK(urlc);
                code = urlc.getResponseCode();
                String result = URLConnection.consumeResponse(urlc.getInputStream());
                this.log("AZURE_CLIENT", 2, String.valueOf(urlc.getResponseCode()) + " " + result);
                if (code == 200) {
                    p = this.get_file_from_request_header(urlc, path, true);
                    Properties stat = new Properties();
                    stat.put("item", p);
                    stat.put("time", String.valueOf(System.currentTimeMillis()));
                    this.stat_cache.put(path, stat);
                    Properties properties = p;
                    return properties;
                }
                if (code == 404 && result.equals("")) {
                    break;
                }
            }
            catch (UnknownHostException e) {
                this.log(e);
                try {
                    Thread.sleep(10000L);
                }
                catch (InterruptedException stat) {
                    // empty catch block
                }
            }
            finally {
                if (urlc != null) {
                    urlc.disconnect();
                }
            }
            ++x;
        }
        if (this.config.getProperty("container_type").equals("share")) {
            if (code == 404) {
                x = 0;
                while (x < 3) {
                    URLConnection urlc2 = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("username") + this.getUrl() + this.double_encode(path) + "?comp=metadata&restype=directory" + this.getSASToken()), this.config);
                    try {
                        urlc2.setRequestMethod("HEAD");
                        urlc2.setDoInput(true);
                        urlc2.setUseCaches(false);
                        urlc2.setRemoveDoubleEncoding(true);
                        this.signRequestSK(urlc2);
                        if (urlc2.getResponseCode() == 200) {
                            p = this.get_file_from_request_header(urlc2, path, false);
                            Properties stat = new Properties();
                            stat.put("item", p);
                            stat.put("time", String.valueOf(System.currentTimeMillis()));
                            this.stat_cache.put(path, stat);
                            Properties properties = p;
                            return properties;
                        }
                        String result2 = URLConnection.consumeResponse(urlc2.getInputStream());
                        this.log("AZURE_CLIENT", 2, String.valueOf(urlc2.getResponseCode()) + result2);
                        return null;
                    }
                    catch (UnknownHostException e) {
                        this.log(e);
                        try {
                            Thread.sleep(10000L);
                        }
                        catch (InterruptedException stat) {
                            // empty catch block
                        }
                    }
                    finally {
                        if (urlc2 != null) {
                            urlc2.disconnect();
                        }
                    }
                    ++x;
                }
            }
            return null;
        }
        if (this.config.getProperty("container_type").equals("blob")) {
            try {
                String prefix = path.substring(path.indexOf("/" + this.config.getProperty("share") + "/") + this.config.getProperty("share").length() + 2, path.length());
                String restype = "maxresults=1&prefix=" + this.double_encode(prefix).replace("/", "%2F") + "%2F&restype=container";
                String url_path = "/" + this.config.getProperty("share") + "/";
                URLConnection urlc3 = null;
                int x2 = 0;
                while (x2 < 3) {
                    block49: {
                        try {
                            urlc3 = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("username") + this.getUrl() + this.double_encode(url_path) + "?comp=list&" + restype + this.getSASToken()), this.config);
                            urlc3.setRequestMethod("GET");
                            urlc3.setDoInput(true);
                            urlc3.setDoOutput(true);
                            urlc3.setUseCaches(false);
                            urlc3.setRemoveDoubleEncoding(true);
                            this.signRequestSK(urlc3);
                            urlc3.getResponseCode();
                            String result = URLConnection.consumeResponse(urlc3.getInputStream());
                            this.log("AZURE_CLIENT", 2, String.valueOf(urlc3.getResponseCode()) + " " + result);
                            Vector list = new Vector();
                            this.parseBlobList(prefix, path, list, result, false);
                            if (list.size() == 1) {
                                Date date = new Date();
                                SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                                String line = "drwxrwxrwx   1    owner   group   0   " + yyyyMMddHHmmss.format(date) + "   " + this.dd.format(date) + " " + this.yyyy.format(date) + " " + path;
                                p = AzureClient.parseStat(line);
                                p.put("url", "azure://" + this.config.getProperty("username") + ":" + VRL.vrlEncode(this.config.getProperty("password")) + "@blob.core.windows.net" + path);
                                p.put("owner", "owner");
                                p.put("group", "group");
                                Properties stat = new Properties();
                                stat.put("item", p);
                                stat.put("time", String.valueOf(System.currentTimeMillis()));
                                this.stat_cache.put(path, stat);
                                Properties properties = p;
                                return properties;
                            }
                            if (list.size() != 0) break block49;
                            break;
                        }
                        catch (UnknownHostException e) {
                            this.log(e);
                            Thread.sleep(10000L);
                        }
                        finally {
                            if (urlc3 != null) {
                                urlc3.disconnect();
                            }
                        }
                    }
                    ++x2;
                }
            }
            catch (Exception e) {
                this.log(e);
                this.log("AZURE_CLIENT", 2, "Azure stat error. Path=" + path + " Error :" + e);
            }
        }
        return null;
    }

    private Properties get_file_from_request_header(URLConnection urlc, String path, boolean is_file) throws Exception {
        String name = path;
        Properties p = null;
        SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        if (name.endsWith("/")) {
            name = name.substring(0, name.length() - 1);
        }
        name = name.substring(name.lastIndexOf("/") + 1, name.length());
        String size = "0";
        Date date = new Date();
        if (this.config.getProperty("data_lake_storagegen2", "false").equals("true") && urlc.getHeaderField("X-MS-META-HDI_ISFOLDER") != null && urlc.getHeaderField("X-MS-META-HDI_ISFOLDER").equals("true")) {
            is_file = false;
        }
        if (is_file) {
            size = urlc.getHeaderField("CONTENT-LENGTH");
        }
        String line = String.valueOf(!is_file ? "d" : "-") + "rwxrwxrwx   1    owner   group   " + size + "   " + yyyyMMddHHmmss.format(date) + "   " + this.dd.format(date) + " " + this.yyyy.format(date) + " " + path;
        p = AzureClient.parseStat(line);
        p.put("url", "azure://" + this.config.getProperty("username") + ":" + VRL.vrlEncode(this.config.getProperty("password")) + "@" + this.getUrl().substring(1) + path);
        p.put("owner", "owner");
        p.put("group", "group");
        if (!is_file) {
            p.put("check_all_recursive_deletes", "true");
        }
        String last_modified = urlc.getHeaderField("LAST-MODIFIED");
        try {
            this.parseLastModified(p, last_modified);
        }
        catch (Exception e) {
            this.log(e);
            this.log("AZURE_CLIENT", 1, e);
        }
        return p;
    }

    @Override
    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        String params = "";
        if (!this.getSASToken().equals("")) {
            params = "?" + this.getSASToken().substring(1);
        }
        boolean keep_open = false;
        int x = 0;
        while (x < 3) {
            URLConnection urlc = null;
            try {
                urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("username") + this.getUrl() + this.double_encode(path) + params), this.config);
                urlc.setRequestMethod("GET");
                urlc.setDoInput(true);
                urlc.setUseCaches(false);
                urlc.setRemoveDoubleEncoding(true);
                this.signRequestSK(urlc);
                if (urlc.getResponseCode() != 200) {
                    String result = URLConnection.consumeResponse(urlc.getInputStream());
                    result = this.getErrorInfo(result, urlc.getResponseCode(), urlc.getConfig("signing_header"), null);
                    this.log("AZURE_CLIENT", 2, String.valueOf(urlc.getResponseCode()) + " " + result);
                    throw new Exception("Download Error: " + urlc.getResponseCode() + " " + result);
                }
                this.in = urlc.getInputStream();
                keep_open = true;
                InputStream inputStream = this.getInsputStreamWrapper(urlc, this.in);
                return inputStream;
            }
            catch (UnknownHostException e) {
                this.log(e);
                try {
                    Thread.sleep(10000L);
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
                if (x == 2) {
                    this.log("AZURE_CLIENT", 1, e);
                    throw e;
                }
            }
            finally {
                if (!keep_open && urlc != null) {
                    urlc.disconnect();
                }
            }
            ++x;
        }
        return null;
    }

    @Override
    protected OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        String path2 = path;
        if (!path.startsWith("/")) {
            path2 = "/" + path;
        }
        String upload_path = path2;
        long resume_pos = startPos;
        String params = "";
        if (this.config.getProperty("container_type").equals("blob") && (this.config.getProperty("upload_blob_type", "blockblob").equals("") || this.config.getProperty("upload_blob_type", "blockblob").equals("null"))) {
            this.config.put("upload_blob_type", "blockblob");
        }
        if (!this.config.getProperty("container_type").equals("blob") || !this.config.getProperty("upload_blob_type", "appendblob").equals("blockblob")) {
            if (!this.getSASToken().equals("")) {
                params = "?" + this.getSASToken().substring(1);
            }
            String result = "";
            int x = 0;
            while (x < 3) {
                URLConnection urlc = null;
                try {
                    urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("username") + this.getUrl() + this.double_encode(upload_path) + params), this.config);
                    urlc.setRequestMethod("PUT");
                    urlc.setRequestProperty("x-ms-content-length", "0");
                    urlc.setRequestProperty("x-ms-type", "file");
                    if (Common.last(path).contains(".")) {
                        String ext = Common.last(path).substring(Common.last(path).lastIndexOf(".")).toUpperCase();
                        Common.mimes.getProperty(ext, "");
                        if (!Common.mimes.getProperty(ext, "").equals("")) {
                            urlc.setRequestProperty("Content-Type", Common.mimes.getProperty(ext, ""));
                        }
                    }
                    urlc.setRequestProperty("Content-Length", "0");
                    if (this.config.getProperty("container_type").equals("blob")) {
                        urlc.setRequestProperty("x-ms-blob-type", "AppendBlob");
                    }
                    if (this.getSASToken().equals("") || !this.getSASToken().equals("") && !this.sas_sv.equals("") && this.isOLderSV("2019-02-01", this.sas_sv)) {
                        urlc.setRequestProperty("x-ms-file-permission", "inherit");
                        urlc.setRequestProperty("x-ms-file-attributes", "None");
                        urlc.setRequestProperty("x-ms-file-creation-time", "now");
                        urlc.setRequestProperty("x-ms-file-last-write-time", "now");
                    }
                    urlc.setDoOutput(true);
                    urlc.setRemoveDoubleEncoding(true);
                    urlc.setUseCaches(false);
                    this.signRequestSK(urlc);
                    result = URLConnection.consumeResponse(urlc.getInputStream());
                    if (urlc.getResponseCode() == 201) break;
                    result = this.getErrorInfo(result, urlc.getResponseCode(), urlc.getConfig("signing_header"), null);
                    this.log("AZURE_CLIENT", 1, String.valueOf(urlc.getResponseCode()) + " " + result);
                    throw new Exception("Upload Error :" + urlc.getResponseCode() + " " + result);
                }
                catch (UnknownHostException e) {
                    this.log(e);
                    try {
                        Thread.sleep(10000L);
                    }
                    catch (InterruptedException interruptedException) {
                        // empty catch block
                    }
                    if (x == 2) {
                        throw e;
                    }
                }
                finally {
                    if (urlc != null) {
                        urlc.disconnect();
                    }
                }
                ++x;
            }
        }
        int chunk_size = 0x400000;
        if (this.config.getProperty("container_type").equals("blob") && this.config.getProperty("upload_blob_type", "appendblob").equals("blockblob") && this.config.containsKey("block_blob_upload_buffer_size")) {
            int number = 4;
            try {
                number = Integer.parseInt(this.config.getProperty("block_blob_upload_buffer_size", "4"));
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (number < 4) {
                number = 4;
            }
            chunk_size = number * 1024 * 1024;
        }
        class OutputWrapper
        extends OutputStream {
            boolean closed = false;
            int chunk_size = 0x400000;
            ByteArrayOutputStream baos = null;
            long pos = 0L;
            long pos2 = 0L;
            long file_pos = 0L;
            Vector flushing_threads = new Vector();
            Vector blockIds = new Vector();
            Vector committedBlockIds = new Vector();
            int blockindex = 0;
            String blockId = "block-";
            Properties errors = new Properties();
            Properties block_bytes = new Properties();
            boolean initialized = false;
            int max_threads = 0;
            private final /* synthetic */ long val$resume_pos;
            private final /* synthetic */ String val$upload_path;
            private final /* synthetic */ String val$path;

            public OutputWrapper(int chunk_size, String string, long l, String string2) {
                this.val$path = string;
                this.val$resume_pos = l;
                this.val$upload_path = string2;
                this.chunk_size = chunk_size;
                this.baos = new ByteArrayOutputStream(this.chunk_size);
                if (AzureClient.this.config.getProperty("container_type").equals("blob") && AzureClient.this.config.getProperty("upload_blob_type", "appendblob").equals("blockblob")) {
                    int number = 0;
                    try {
                        number = Integer.parseInt(AzureClient.this.config.getProperty("block_blob_upload_threads", "3"));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    if (number < 1) {
                        number = 1;
                    }
                    this.max_threads = --number;
                }
                if (string.endsWith("/")) {
                    AzureClient.this.stat_cache.remove(string.substring(0, string.length() - 1));
                }
                if (AzureClient.this.stat_cache.contains(string)) {
                    AzureClient.this.stat_cache.remove(string);
                }
            }

            public String getBlockId() throws IOException {
                if (this.val$resume_pos > 0L && !this.initialized) {
                    try {
                        this.committedBlockIds = this.getBlockList(this.val$upload_path, "committed");
                        String prev_id = new String(Base64.decode((String)this.committedBlockIds.get(this.committedBlockIds.size() - 1)), "UTF8");
                        this.blockindex = Integer.parseInt(prev_id.substring("block-".length()));
                        ++this.blockindex;
                        this.initialized = true;
                    }
                    catch (IOException e) {
                        AzureClient.this.log(e);
                        AzureClient.this.log("AZURE_CLIENT", 1, e);
                        throw new IOException("Failed on block id parse: " + e.getMessage());
                    }
                } else {
                    ++this.blockindex;
                }
                return String.valueOf(this.blockId) + new DecimalFormat("00000000").format(this.blockindex);
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
                if (this.baos.size() + len > this.chunk_size) {
                    int offset = this.chunk_size - this.baos.size();
                    this.baos.write(b, off, offset);
                    this.pos2 = this.baos.size();
                    this.flushCurrentBlock(this.max_threads);
                    this.file_pos += this.pos2 - this.pos;
                    this.baos.write(b, offset, len - offset);
                    this.pos = 0L;
                    this.pos2 = 0L;
                } else {
                    this.baos.write(b, off, len);
                }
            }

            public void flushCurrentBlock(int max_threads) throws IOException {
                int loops = 0;
                while (this.flushing_threads.size() > max_threads && loops++ < 2000) {
                    try {
                        Thread.sleep(100L);
                    }
                    catch (InterruptedException e) {
                        AzureClient.this.log(e);
                        AzureClient.this.log("AZURE_CLIENT", 1, e);
                    }
                }
                if (loops >= 1998) {
                    throw new IOException("200 second timeout while waiting for prior Azure chunk to complete..." + loops + ":" + this.pos + ":" + this.pos2 + " file_pos=" + this.file_pos);
                }
                if (this.errors.containsKey("error")) {
                    throw new IOException(this.errors.getProperty("error"));
                }
                final long file_pos_now = this.file_pos;
                final long pos_now = this.pos;
                final long pos2_now = this.pos2;
                final String current_block_id = this.getBlockId();
                this.block_bytes.put(current_block_id, this.baos);
                this.blockIds.add(Base64.encodeBytes(current_block_id.getBytes()));
                this.baos = new ByteArrayOutputStream(this.chunk_size);
                final Object flush_lock = new Object();
                this.flushing_threads.addElement(flush_lock);
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        String error_message = "";
                        int loops = 0;
                        while (loops < 3) {
                            try {
                                this.flushNow((ByteArrayOutputStream)block_bytes.get(current_block_id), file_pos_now, pos_now, pos2_now, AzureClient.this.getUrl(), current_block_id);
                                error_message = "";
                                break;
                            }
                            catch (Exception e) {
                                error_message = "Upload error on range : " + pos_now + "-" + pos2_now + " Error message : " + e.getMessage();
                                ++loops;
                            }
                        }
                        if (block_bytes.containsKey(current_block_id)) {
                            block_bytes.remove(current_block_id);
                        }
                        if (!errors.containsKey("error") && !error_message.equals("")) {
                            errors.put("error", error_message);
                        }
                        flushing_threads.remove(flush_lock);
                    }
                }, "Azure upload thread for : " + AzureClient.this.config.getProperty("username", "") + " path: " + this.val$path);
            }

            @Override
            public void close() throws IOException {
                if (this.closed) {
                    return;
                }
                AzureClient.this.config.put("closed", "true");
                this.closed = true;
                this.wait_for_prior_chunk();
                if (this.errors.containsKey("error")) {
                    throw new IOException(this.errors.getProperty("error"));
                }
                this.pos2 += (long)this.baos.size();
                if (this.pos2 > this.pos) {
                    this.flushCurrentBlock(0);
                    this.wait_for_prior_chunk();
                }
                if (AzureClient.this.config.getProperty("container_type").equals("blob") && AzureClient.this.config.getProperty("upload_blob_type", "appendblob").equals("blockblob")) {
                    this.commitBlockList(this.val$upload_path);
                }
                if (this.errors.containsKey("error")) {
                    throw new IOException(this.errors.getProperty("error"));
                }
                if (this.val$path.endsWith("/")) {
                    AzureClient.this.stat_cache.remove(this.val$path.substring(0, this.val$path.length() - 1));
                }
                if (AzureClient.this.stat_cache.contains(this.val$path)) {
                    AzureClient.this.stat_cache.remove(this.val$path);
                }
            }

            private void wait_for_prior_chunk() throws IOException {
                try {
                    int loops = 0;
                    while (this.flushing_threads.size() > 0 && loops++ < 2000) {
                        Thread.sleep(300L);
                    }
                    if (loops >= 1998) {
                        throw new IOException("200 second timeout while waiting for prior Azure chunk to complete..." + loops + ":" + this.pos + ":" + this.pos2 + " file_pos=" + this.file_pos);
                    }
                }
                catch (InterruptedException e) {
                    AzureClient.this.log(e);
                    AzureClient.this.log("AZURE_CLIENT", 1, e);
                }
            }

            public String flushNow(ByteArrayOutputStream baos, long file_pos_now, long pos_now, long pos2_now, String url_now, String block_id) throws IOException {
                if (AzureClient.this.config.getProperty("container_type").equals("share")) {
                    int x = 0;
                    while (x < 3) {
                        block34: {
                            URLConnection urlc_size = URLConnection.openConnection(new VRL("https://" + AzureClient.this.config.getProperty("username") + url_now + AzureClient.this.double_encode(this.val$upload_path) + "?comp=properties" + AzureClient.this.getSASToken()), AzureClient.this.config);
                            urlc_size.setRequestMethod("PUT");
                            urlc_size.setRequestProperty("x-ms-content-length", String.valueOf(file_pos_now + (pos2_now - pos_now)));
                            urlc_size.setRequestProperty("Content-Length", "0");
                            if (AzureClient.this.getSASToken().equals("") || !AzureClient.this.getSASToken().equals("") && !AzureClient.this.sas_sv.equals("") && AzureClient.this.isOLderSV("2019-02-01", AzureClient.this.sas_sv)) {
                                urlc_size.setRequestProperty("x-ms-file-permission", "preserve");
                                urlc_size.setRequestProperty("x-ms-file-attributes", "preserve");
                                urlc_size.setRequestProperty("x-ms-file-creation-time", "preserve");
                                urlc_size.setRequestProperty("x-ms-file-last-write-time", "preserve");
                            }
                            urlc_size.setDoOutput(true);
                            urlc_size.setUseCaches(false);
                            urlc_size.setRemoveDoubleEncoding(true);
                            try {
                                AzureClient.this.signRequestSK(urlc_size);
                            }
                            catch (Exception e1) {
                                AzureClient.this.log("AZURE_CLIENT", 1, e1);
                                throw new IOException(e1.getMessage());
                            }
                            String result_size = "";
                            try {
                                result_size = Common.consumeResponse(urlc_size.getInputStream());
                            }
                            catch (UnknownHostException e) {
                                AzureClient.this.log(e);
                                try {
                                    Thread.sleep(10000L);
                                }
                                catch (InterruptedException interruptedException) {
                                    // empty catch block
                                }
                                if (x == 2) {
                                    throw new IOException("" + e);
                                }
                                break block34;
                            }
                            catch (Exception e2) {
                                AzureClient.this.log("AZURE_CLIENT", 1, e2);
                                throw new IOException(e2.getMessage());
                            }
                            if (urlc_size.getResponseCode() != 201) {
                                AzureClient.this.log("AZURE_CLIENT", 1, String.valueOf(urlc_size.getResponseCode()) + " " + result_size);
                            }
                            urlc_size.disconnect();
                            break;
                        }
                        ++x;
                    }
                }
                String commands = "comp=range";
                if (AzureClient.this.config.getProperty("container_type").equals("blob")) {
                    if (AzureClient.this.config.getProperty("upload_blob_type", "blockblob").equals("appendblob")) {
                        commands = "comp=appendblock";
                    }
                    if (AzureClient.this.config.getProperty("upload_blob_type", "blockblob").equals("blockblob") || AzureClient.this.config.getProperty("upload_blob_type", "blockblob").equals("") || AzureClient.this.config.getProperty("upload_blob_type", "blockblob").equals("null")) {
                        commands = "blockid=" + AzureClient.this.double_encode(Base64.encodeBytes(block_id.getBytes())) + "&comp=block";
                        AzureClient.this.log("AZURE_CLIENT", 2, "Block upload blockid=" + block_id.getBytes());
                    }
                }
                int x = 0;
                while (x < 3) {
                    URLConnection urlc2 = null;
                    String result = "";
                    try {
                        urlc2 = URLConnection.openConnection(new VRL("https://" + AzureClient.this.config.getProperty("username") + url_now + AzureClient.this.double_encode(this.val$upload_path) + "?" + commands + AzureClient.this.getSASToken()), AzureClient.this.config);
                        urlc2.setRequestMethod("PUT");
                        if (AzureClient.this.config.getProperty("container_type").equals("share")) {
                            urlc2.setRequestProperty("x-ms-range", "bytes=" + file_pos_now + "-" + (file_pos_now + (pos2_now - pos_now - 1L)));
                            urlc2.setRequestProperty("x-ms-write", "update");
                        } else if (AzureClient.this.config.getProperty("container_type").equals("blob") && AzureClient.this.config.getProperty("upload_blob_type", "appendblob").equals("blockblob")) {
                            urlc2.setRemoveDoubleEncoding(true);
                        }
                        urlc2.setRequestProperty("Content-Length", String.valueOf(pos2_now - pos_now));
                        urlc2.setDoOutput(true);
                        urlc2.setUseCaches(false);
                        urlc2.setRemoveDoubleEncoding(true);
                        try {
                            AzureClient.this.signRequestSK(urlc2);
                        }
                        catch (Exception e1) {
                            if (AzureClient.this.config.getProperty("upload_blob_type", "appendblob").equals("blockblob")) {
                                AzureClient.this.log("AZURE_CLIENT", 2, "Error on Block upload blockid=" + block_id.getBytes());
                            }
                            AzureClient.this.log("AZURE_CLIENT", 1, e1);
                            throw new IOException(e1.getMessage());
                        }
                        baos.writeTo(urlc2.getOutputStream());
                        urlc2.getOutputStream().close();
                        baos = null;
                        result = Common.consumeResponse(urlc2.getInputStream());
                        if (urlc2.getResponseCode() == 201) break;
                        if (AzureClient.this.config.getProperty("upload_blob_type", "appendblob").equals("blockblob")) {
                            AzureClient.this.log("AZURE_CLIENT", 2, "Error on Block upload blockid=" + block_id.getBytes());
                        }
                        AzureClient.this.log("AZURE_CLIENT", 1, String.valueOf(urlc2.getResponseCode()) + result);
                        throw new IOException(result);
                    }
                    catch (UnknownHostException e) {
                        AzureClient.this.log(e);
                        try {
                            Thread.sleep(10000L);
                        }
                        catch (InterruptedException interruptedException) {
                            // empty catch block
                        }
                        if (x == 2) {
                            throw e;
                        }
                    }
                    catch (Exception e) {
                        if (AzureClient.this.config.getProperty("upload_blob_type", "appendblob").equals("blockblob")) {
                            AzureClient.this.log("AZURE_CLIENT", 2, "Error on Block upload blockid=" + block_id.getBytes());
                        }
                        AzureClient.this.log("AZURE_CLIENT", 1, e);
                    }
                    finally {
                        if (urlc2 != null) {
                            urlc2.disconnect();
                        }
                    }
                    ++x;
                }
                this.block_bytes.remove(block_id);
                return "";
            }

            public Vector getBlockList(String upload_path, String type) {
                Vector<String> blockIds = new Vector<String>();
                int x = 0;
                while (x < 3) {
                    block28: {
                        String result;
                        URLConnection urlc;
                        block27: {
                            urlc = null;
                            String commands = "blocklisttype=" + type + "&comp=blocklist";
                            urlc = URLConnection.openConnection(new VRL("https://" + AzureClient.this.config.getProperty("username") + AzureClient.this.getUrl() + AzureClient.this.double_encode(upload_path) + "?" + commands + AzureClient.this.getSASToken()), AzureClient.this.config);
                            urlc.setRequestMethod("GET");
                            urlc.setDoInput(true);
                            urlc.setUseCaches(false);
                            urlc.setRemoveDoubleEncoding(true);
                            AzureClient.this.signRequestSK(urlc);
                            result = "";
                            if (urlc.getResponseCode() != 200) break block27;
                            result = Common.consumeResponse(urlc.getInputStream());
                            if (result.contains("<BlockList>") && result.contains("</BlockList>") && result.indexOf("<BlockList>") < result.indexOf("</BlockList>")) {
                                result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + result.substring(result.indexOf("<BlockList>"), result.indexOf("</BlockList>") + "</BlockList>".length());
                                SAXBuilder sax = Common.getSaxBuilder();
                                Document doc = sax.build((Reader)new StringReader(result));
                                for (Element element : doc.getRootElement().getChildren()) {
                                    for (Element element2 : element.getChildren()) {
                                        for (Element element3 : element2.getChildren()) {
                                            String text;
                                            if (!element3.getName().equals("Name") || (text = element3.getText()) == null) continue;
                                            AzureClient.this.log("AZURE_CLIENT", 2, "Block upload - List " + type + "_" + new String(Base64.decode(text), "UTF8"));
                                            blockIds.add(text);
                                        }
                                    }
                                }
                            }
                            if (urlc == null) break;
                            try {
                                urlc.disconnect();
                            }
                            catch (Exception exception) {}
                            break;
                        }
                        try {
                            result = Common.consumeResponse(urlc.getInputStream());
                            AzureClient.this.log("AZURE_CLIENT", 1, String.valueOf(urlc.getResponseCode()) + " " + urlc.getResponseMessage() + " " + result);
                        }
                        catch (UnknownHostException e) {
                            AzureClient.this.log(e);
                            try {
                                Thread.sleep(10000L);
                            }
                            catch (InterruptedException interruptedException) {
                                // empty catch block
                            }
                            if (urlc != null) {
                                try {
                                    urlc.disconnect();
                                }
                                catch (Exception exception) {}
                            }
                            break block28;
                        }
                        catch (Exception e) {
                            try {
                                AzureClient.this.log(e);
                                AzureClient.this.log("AZURE_CLIENT", 1, e);
                                break block28;
                            }
                            catch (Throwable throwable) {
                                throw throwable;
                            }
                            finally {
                                if (urlc != null) {
                                    try {
                                        urlc.disconnect();
                                    }
                                    catch (Exception exception) {}
                                }
                            }
                        }
                        if (urlc == null) break block28;
                        try {
                            urlc.disconnect();
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    ++x;
                }
                return blockIds;
            }

            public void commitBlockList(String upload_path) throws IOException {
                String block_list = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<BlockList>\r\n";
                if (this.val$resume_pos > 0L) {
                    this.committedBlockIds = this.getBlockList(upload_path, "committed");
                    int x = 0;
                    while (x < this.committedBlockIds.size()) {
                        block_list = String.valueOf(block_list) + "<Committed>" + this.committedBlockIds.get(x) + "</Committed>\r\n";
                        ++x;
                    }
                    block_list = this.prepareBlockIds(upload_path, block_list, "Uncommitted");
                } else {
                    block_list = this.prepareBlockIds(upload_path, block_list, "Latest");
                }
                block_list = String.valueOf(block_list) + "</BlockList>\r\n";
                String commands = "comp=blocklist";
                int x = 0;
                while (x < 3) {
                    URLConnection urlc2 = null;
                    try {
                        urlc2 = URLConnection.openConnection(new VRL("https://" + AzureClient.this.config.getProperty("username") + AzureClient.this.getUrl() + AzureClient.this.double_encode(upload_path) + "?" + commands + AzureClient.this.getSASToken()), AzureClient.this.config);
                        urlc2.setRequestMethod("PUT");
                        urlc2.setDoOutput(true);
                        urlc2.setUseCaches(false);
                        urlc2.setRequestProperty("Content-Length", String.valueOf(block_list.getBytes().length));
                        if (Common.last(upload_path).contains(".")) {
                            String ext = Common.last(this.val$path).substring(Common.last(upload_path).lastIndexOf(".")).toUpperCase();
                            Common.mimes.getProperty(ext, "");
                            if (!Common.mimes.getProperty(ext, "").equals("")) {
                                urlc2.setRequestProperty("x-ms-blob-content-type", Common.mimes.getProperty(ext, ""));
                            }
                        }
                        urlc2.setRemoveDoubleEncoding(true);
                        try {
                            AzureClient.this.signRequestSK(urlc2);
                        }
                        catch (Exception e1) {
                            AzureClient.this.log("AZURE_CLIENT", 1, e1);
                            throw new IOException(e1.getMessage());
                        }
                        urlc2.getOutputStream().write(block_list.getBytes());
                        urlc2.getOutputStream().close();
                        if (urlc2.getResponseCode() == 201) break;
                        try {
                            String result = Common.consumeResponse(urlc2.getInputStream());
                            AzureClient.this.log("AZURE_CLIENT", 1, String.valueOf(urlc2.getResponseCode()) + result);
                            throw new IOException(result);
                        }
                        catch (Exception e) {
                            AzureClient.this.log("AZURE_CLIENT", 1, e);
                            throw new IOException(e.getMessage());
                        }
                    }
                    catch (UnknownHostException e) {
                        AzureClient.this.log(e);
                        try {
                            Thread.sleep(10000L);
                        }
                        catch (InterruptedException interruptedException) {
                            // empty catch block
                        }
                        if (x == 2) {
                            throw e;
                        }
                    }
                    finally {
                        if (urlc2 != null) {
                            urlc2.disconnect();
                        }
                    }
                    ++x;
                }
            }

            private String prepareBlockIds(String upload_path, String block_list, String type) throws IOException {
                String error_message = "Azure block blob upload failed! Could not upload blocks: ";
                boolean missing_blocks = false;
                Vector uncommittedBlockIds = this.getBlockList(upload_path, "uncommitted");
                int x = 0;
                while (x < this.blockIds.size()) {
                    if (uncommittedBlockIds.size() > 0) {
                        if (uncommittedBlockIds.contains((String)this.blockIds.get(x))) {
                            block_list = String.valueOf(block_list) + "<" + type + ">" + this.blockIds.get(x) + "</" + type + ">\r\n";
                        } else {
                            missing_blocks = true;
                            error_message = String.valueOf(error_message) + this.blockIds.get(x) + " ; ";
                        }
                    } else {
                        missing_blocks = true;
                        error_message = String.valueOf(error_message) + this.blockIds.get(x) + " ; ";
                    }
                    ++x;
                }
                if (missing_blocks) {
                    throw new IOException(error_message);
                }
                return block_list;
            }
        }
        this.out = new OutputWrapper(chunk_size, path, resume_pos, upload_path);
        return this.out;
    }

    /*
     * WARNING - Removed back jump from a try to a catch block - possible behaviour change.
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public boolean delete(String path) throws Exception {
        p = this.stat(path);
        is_folder = false;
        restype = "";
        if (p.getProperty("type").equals("FILE")) {
            restype = "file";
        } else {
            is_folder = true;
            restype = "directory";
        }
        commands = "?restype=" + restype;
        if (this.config.getProperty("container_type").equals("blob")) {
            commands = "";
            if (restype.equals("directory")) {
                if (!path.endsWith("/")) {
                    path = String.valueOf(path) + "/";
                }
                prefix = "";
                list = new Vector<Properties>();
                if (!path.equals("/" + this.config.getProperty("share") + "/")) {
                    prefix = path.substring(path.indexOf("/" + this.config.getProperty("share") + "/") + this.config.getProperty("share").length() + 2, path.length());
                }
                restype = "maxresults=5000&prefix=" + this.double_encode(prefix).replace("/", "%2F") + "&restype=container";
                url_path = "/" + this.config.getProperty("share") + "/";
                result = "";
                x = 0;
                while (x < 3) {
                    urlc = null;
                    try {
                        urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("username") + this.getUrl() + this.double_encode(url_path) + "?comp=list&" + restype + this.getSASToken()), this.config);
                        urlc.setRequestMethod("GET");
                        urlc.setDoInput(true);
                        urlc.setDoOutput(true);
                        urlc.setUseCaches(false);
                        urlc.setRemoveDoubleEncoding(true);
                        this.signRequestSK(urlc);
                        urlc.getResponseCode();
                        result = URLConnection.consumeResponse(urlc.getInputStream());
                        break;
                    }
                    catch (UnknownHostException e) {
                        this.log(e);
                        Thread.sleep(10000L);
                    }
                    finally {
                        if (urlc != null) {
                            urlc.disconnect();
                        }
                    }
                    ++x;
                }
                this.parseBlobList(prefix, path, list, result, true);
                this.log("AZURE_CLIENT", 1, "Blob folder delete : Searching for sub items. List size : " + list.size());
                x = 0;
                block28: while (true) {
                    if (x >= list.size()) {
                        i = 0;
                        break;
                    }
                    item1 = (Properties)list.elementAt(x);
                    xx = x;
                    while (true) {
                        if (xx >= list.size()) {
                            ++x;
                            continue block28;
                        }
                        item2 = (Properties)list.elementAt(xx);
                        s1 = item1.getProperty("path", "").trim();
                        s2 = item2.getProperty("path", "").trim();
                        swap = false;
                        if (s2.compareTo(s1) > 0) {
                            swap = true;
                        }
                        if (swap) {
                            list.setElementAt(item2, x);
                            list.setElementAt(item1, xx);
                            item1 = item2;
                        }
                        ++xx;
                    }
                    break;
                }
                while (i < list.size()) {
                    pp = (Properties)list.get(i);
                    this.log("AZURE_CLIENT", 2, "Blob folder delete : Delete blob : " + pp.getProperty("path"));
                    d_path = String.valueOf(pp.getProperty("path")) + pp.getProperty("name");
                    if (pp.getProperty("type").equals("DIR") && !d_path.endsWith("/")) {
                        d_path = String.valueOf(d_path) + "/";
                    }
                    if (!this.getSASToken().equals("") && commands.equals("")) {
                        commands = "?" + this.getSASToken().substring(1);
                    }
                    x = 0;
                    while (x < 3) {
                        urlc2 = null;
                        try {
                            urlc2 = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("username") + this.getUrl() + "/" + this.config.getProperty("share") + this.double_encode(d_path) + commands), this.config);
                            urlc2.setRequestMethod("DELETE");
                            urlc2.setDoInput(true);
                            urlc2.setUseCaches(false);
                            urlc2.setRemoveDoubleEncoding(true);
                            this.signRequestSK(urlc2);
                            if (urlc2.getResponseCode() == 202) break;
                            this.log("AZURE_CLIENT", 1, "Blob folder delete : Could not delete blob : " + pp.getProperty("path"));
                            this.log("AZURE_CLIENT", 1, "Blob folder delete : Respond message : " + urlc2.getResponseMessage());
                            this.log("AZURE_CLIENT", 1, "Blob folder delete : Result : " + URLConnection.consumeResponse(urlc2.getInputStream()));
                            this.log("Blob folder delete : Could not delete blob : " + pp.getProperty("path"));
                            if (!this.config.getProperty("data_lake_storagegen2", "false").equals("true")) {
                                return false;
                            }
                        }
                        catch (UnknownHostException e) {
                            this.log(e);
                            try {
                                Thread.sleep(10000L);
                            }
                            catch (InterruptedException swap) {
                                // empty catch block
                            }
                            if (urlc2 != null) {
                                try {
                                    urlc2.disconnect();
                                }
                                catch (IOException e1) {
                                    this.log("AZURE_CLIENT", 1, e1);
                                }
                            }
                            if (x == 2 && !this.config.getProperty("data_lake_storagegen2", "false").equals("true")) {
                                return false;
                            }
                        }
                        finally {
                            if (urlc2 != null) {
                                urlc2.disconnect();
                            }
                        }
                        ++x;
                    }
                    ++i;
                }
            }
        }
        if (!this.getSASToken().equals("")) {
            if (commands.equals("")) {
                commands = "?" + this.getSASToken().substring(1);
            } else if (!commands.contains(this.getSASToken().substring(1))) {
                commands = String.valueOf(commands) + this.getSASToken();
            }
        }
        delete_path = path;
        if (this.config.getProperty("data_lake_storagegen2", "false").equals("true") && path.endsWith("/")) {
            delete_path = path.substring(0, path.length() - 1);
        }
        code = 0;
        x = 0;
        while (x < 3) {
            urlc = null;
            try {
                urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("username") + this.getUrl() + this.double_encode(delete_path) + commands), this.config);
                urlc.setRequestMethod("DELETE");
                urlc.setDoInput(true);
                urlc.setUseCaches(false);
                urlc.setRemoveDoubleEncoding(true);
                this.signRequestSK(urlc);
                code = urlc.getResponseCode();
                if (code == 202) {
                    if (path.endsWith("/")) {
                        path = path.substring(0, path.length() - 1);
                    }
                    if (this.stat_cache.contains(path) == false) return true;
                    this.stat_cache.remove(path);
                    return true;
                }
                result = URLConnection.consumeResponse(urlc.getInputStream());
                result = this.getErrorInfo(result, urlc.getResponseCode(), urlc.getConfig("signing_header"), null);
                this.log("AZURE_CLIENT", 1, String.valueOf(urlc.getResponseCode()) + " " + result);
                break;
            }
            catch (UnknownHostException e) {
                this.log(e);
                Thread.sleep(10000L);
                if (x == 2) {
                    this.log("AZURE_CLIENT", 1, e);
                    return false;
                }
            }
            finally {
                if (urlc != null) {
                    urlc.disconnect();
                }
            }
            ++x;
        }
        if (this.config.getProperty("data_lake_storagegen2", "false").equals("true") == false) return false;
        commands = "";
        if (is_folder) {
            commands = "?recursive=true";
        }
        if (!this.getSASToken().equals("")) {
            if (commands.equals("")) {
                commands = "?" + this.getSASToken().substring(1);
            } else if (!commands.contains(this.getSASToken().substring(1))) {
                commands = String.valueOf(commands) + this.getSASToken();
            }
        }
        try {
            x = 0;
            while (true) {
                block75: {
                    if (x >= 3) {
                        return false;
                    }
                    urlc2 = null;
                    try {
                        urlc2 = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("username") + ".dfs.core.windows.net" + this.double_encode(delete_path) + commands), this.config);
                        urlc2.setRequestMethod("DELETE");
                        urlc2.setDoInput(true);
                        urlc2.setUseCaches(false);
                        urlc2.setRemoveDoubleEncoding(true);
                        this.signRequestSK(urlc2);
                        code = urlc2.getResponseCode();
                        if (code >= 200 && code < 299) {
                            if (path.endsWith("/")) {
                                path = path.substring(0, path.length() - 1);
                            }
                            if (this.stat_cache.contains(path)) {
                                this.stat_cache.remove(path);
                            }
                            if (urlc2 == null) return true;
                        }
                        ** GOTO lbl-1000
                    }
                    catch (UnknownHostException e) {
                        this.log(e);
                        Thread.sleep(10000L);
                        if (x == 2) {
                            this.log("AZURE_CLIENT", 1, e);
                            if (urlc2 == null) return false;
                            urlc2.disconnect();
                            return false;
                        }
                        if (urlc2 != null) {
                            urlc2.disconnect();
                        }
                        break block75;
                    }
                    catch (Throwable var11_20) {
                        if (urlc2 == null) throw var11_20;
                        urlc2.disconnect();
                        throw var11_20;
                    }
                    urlc2.disconnect();
                    return true;
lbl-1000:
                    // 1 sources

                    {
                        result = URLConnection.consumeResponse(urlc2.getInputStream());
                        this.log(result);
                    }
                    if (urlc2 != null) {
                        urlc2.disconnect();
                    }
                }
                ++x;
            }
        }
        catch (Exception e) {
            this.log(e);
        }
        return false;
    }

    @Override
    public boolean makedirs(String path) throws Exception {
        boolean ok = true;
        String[] parts = path.split("/");
        String path2 = "";
        if (parts.length < 2 && !this.config.getProperty("share").equals(parts[1])) {
            return false;
        }
        String share_part = "/" + parts[1] + "/";
        int x = 2;
        while (x < parts.length && ok) {
            path2 = String.valueOf(path2) + parts[x] + "/";
            if (x >= 1 && this.stat(String.valueOf(share_part) + path2) == null) {
                ok = this.makedir(String.valueOf(share_part) + path2);
            }
            ++x;
        }
        return ok;
    }

    @Override
    public boolean makedir(String path) throws Exception {
        if (this.config.getProperty("container_type").equals("share")) {
            int x = 0;
            while (x < 3) {
                URLConnection urlc = null;
                try {
                    urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("username") + ".file.core.windows.net" + this.double_encode(path) + "?restype=directory" + this.getSASToken()), this.config);
                    urlc.setRequestMethod("PUT");
                    urlc.setRequestProperty("Content-Length", "0");
                    urlc.setDoOutput(true);
                    urlc.setUseCaches(false);
                    if (this.getSASToken().equals("") || !this.getSASToken().equals("") && !this.sas_sv.equals("") && this.isOLderSV("2019-02-01", this.sas_sv)) {
                        urlc.setRequestProperty("x-ms-file-permission", "inherit");
                        urlc.setRequestProperty("x-ms-file-attributes", "None");
                        urlc.setRequestProperty("x-ms-file-creation-time", "now");
                        urlc.setRequestProperty("x-ms-file-last-write-time", "now");
                    }
                    urlc.setRemoveDoubleEncoding(true);
                    this.signRequestSK(urlc);
                    String result = Common.consumeResponse(urlc.getInputStream());
                    int code = urlc.getResponseCode();
                    if (code == 201) {
                        return true;
                    }
                    String signing_header = urlc.getConfig("signing_header");
                    result = this.getErrorInfo(result, code, signing_header, null);
                    this.log("AZURE_CLIENT", 2, String.valueOf(code) + " " + result);
                    return false;
                }
                catch (UnknownHostException e) {
                    this.log(e);
                    Thread.sleep(10000L);
                    if (x == 2) {
                        this.log("AZURE_CLIENT", 1, e);
                    }
                }
                catch (Exception e1) {
                    this.log(e1);
                    this.log("AZURE_CLIENT", 1, e1);
                }
                finally {
                    if (urlc != null) {
                        urlc.disconnect();
                    }
                }
                ++x;
            }
            return false;
        }
        if (this.config.getProperty("container_type").equals("blob")) {
            if (this.config.getProperty("upload_blob_type", "appendblob").equals("blockblob")) {
                String path2 = path;
                if (!path.startsWith("/")) {
                    path2 = "/" + path;
                }
                if (this.config.getProperty("data_lake_storagegen2", "false").equals("true") && path2.endsWith("/")) {
                    path2 = path2.substring(0, path2.length() - 1);
                }
                String params = "";
                if (!this.getSASToken().equals("")) {
                    params = "?" + this.getSASToken().substring(1);
                }
                String addition_name = "";
                int x = 0;
                while (x < 3) {
                    URLConnection urlc = null;
                    try {
                        urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("username") + this.getUrl() + this.double_encode(path2) + addition_name + params), this.config);
                        urlc.setRequestMethod("PUT");
                        urlc.setRequestProperty("x-ms-content-length", "0");
                        urlc.setRequestProperty("Content-Length", "0");
                        urlc.setRequestProperty("x-ms-blob-type", "BlockBlob");
                        if (this.getSASToken().equals("") || !this.getSASToken().equals("") && !this.sas_sv.equals("") && this.isOLderSV("2019-02-01", this.sas_sv)) {
                            urlc.setRequestProperty("x-ms-file-permission", "inherit");
                            urlc.setRequestProperty("x-ms-file-attributes", "None");
                            urlc.setRequestProperty("x-ms-file-creation-time", "now");
                            urlc.setRequestProperty("x-ms-file-last-write-time", "now");
                        }
                        if (this.config.getProperty("data_lake_storagegen2", "false").equals("true")) {
                            urlc.setRequestProperty("x-ms-meta-hdi_isfolder", "true");
                        }
                        urlc.setDoOutput(true);
                        urlc.setUseCaches(false);
                        urlc.setRemoveDoubleEncoding(true);
                        this.signRequestSK(urlc);
                        String result = Common.consumeResponse(urlc.getInputStream());
                        int code = urlc.getResponseCode();
                        this.log("AZURE_CLIENT", 2, String.valueOf(code) + " " + result);
                        return code == 201;
                        {
                        }
                    }
                    catch (UnknownHostException e) {
                        this.log(e);
                        Thread.sleep(10000L);
                        if (x == 2) {
                            this.log("AZURE_CLIENT", 1, e);
                        }
                    }
                    finally {
                        if (urlc != null) {
                            urlc.disconnect();
                        }
                    }
                    ++x;
                }
                return false;
            }
            String path2 = path;
            if (!path.startsWith("/")) {
                path2 = "/" + path;
            }
            String params = "";
            if (!this.getSASToken().equals("")) {
                params = "?" + this.getSASToken().substring(1);
            }
            int x = 0;
            while (x < 3) {
                URLConnection urlc = null;
                try {
                    urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("username") + this.getUrl() + this.double_encode(path2) + params), this.config);
                    urlc.setRequestMethod("PUT");
                    urlc.setRequestProperty("x-ms-content-length", "0");
                    urlc.setRequestProperty("Content-Length", "0");
                    urlc.setRequestProperty("x-ms-blob-type", "AppendBlob");
                    if (this.getSASToken().equals("") || !this.getSASToken().equals("") && !this.sas_sv.equals("") && this.isOLderSV("2019-02-01", this.sas_sv)) {
                        urlc.setRequestProperty("x-ms-file-permission", "inherit");
                        urlc.setRequestProperty("x-ms-file-attributes", "None");
                        urlc.setRequestProperty("x-ms-file-creation-time", "now");
                        urlc.setRequestProperty("x-ms-file-last-write-time", "now");
                    }
                    urlc.setDoOutput(true);
                    urlc.setUseCaches(false);
                    urlc.setRemoveDoubleEncoding(true);
                    this.signRequestSK(urlc);
                    String result = Common.consumeResponse(urlc.getInputStream());
                    int code = urlc.getResponseCode();
                    String signing_header = urlc.getConfig("signing_header");
                    urlc.disconnect();
                    if (code == 201) {
                        return true;
                    }
                    result = this.getErrorInfo(result, code, signing_header, null);
                    this.log("Makedir Error: " + result);
                    return false;
                }
                catch (UnknownHostException e) {
                    this.log(e);
                    Thread.sleep(10000L);
                    if (x == 2) {
                        this.log("AZURE_CLIENT", 1, e);
                    }
                }
                finally {
                    if (urlc != null) {
                        urlc.disconnect();
                    }
                }
                ++x;
            }
        }
        return false;
    }

    @Override
    public boolean mdtm(String path, long modified) throws Exception {
        String restype = "";
        if (this.config.getProperty("container_type").equals("blob")) {
            restype = "";
        }
        if (this.config.getProperty("container_type").equals("share")) {
            Properties p = this.stat(path);
            restype = p.getProperty("type").equals("FILE") ? "&restype=file" : "&restype=directory";
        }
        int x = 0;
        while (x < 3) {
            block22: {
                String signing_header;
                int code;
                String result;
                URLConnection urlc;
                block20: {
                    block21: {
                        urlc = null;
                        urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("username") + this.getUrl() + this.double_encode(path) + "?comp=metadata" + restype + this.getSASToken()), this.config);
                        urlc.setRequestMethod("PUT");
                        SimpleDateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
                        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
                        String date = String.valueOf(fmt.format(new Date(modified))) + " GMT";
                        urlc.setRequestProperty("Last-Modified", date);
                        urlc.setRequestProperty("Content-Length", "0");
                        Properties p = this.getMetadata(path);
                        if (p.containsKey("X-MS-META-UPLOADED_BY")) {
                            urlc.setRequestProperty("x-ms-meta-uploaded_by", p.getProperty("X-MS-META-UPLOADED_BY", ""));
                        } else if (!this.config.getProperty("uploaded_by", "").equals("")) {
                            urlc.setRequestProperty("x-ms-meta-uploaded_by", this.config.getProperty("uploaded_by", ""));
                        }
                        if (p.containsKey("X-MS-META-MD5")) {
                            urlc.setRequestProperty("x-ms-meta-md5", p.getProperty("X-MS-META-MD5", ""));
                        } else if (!this.config.getProperty("uploaded_md5", "").equals("")) {
                            urlc.setRequestProperty("x-ms-meta-md5", this.config.getProperty("uploaded_md5", ""));
                        }
                        urlc.setDoOutput(true);
                        urlc.setUseCaches(false);
                        urlc.setRemoveDoubleEncoding(true);
                        this.signRequestSK(urlc);
                        result = Common.consumeResponse(urlc.getInputStream());
                        code = urlc.getResponseCode();
                        signing_header = urlc.getConfig("signing_header");
                        urlc.disconnect();
                        if (code != 200 && code != 202) break block20;
                        if (urlc == null) break block21;
                        urlc.disconnect();
                    }
                    return true;
                }
                try {
                    result = this.getErrorInfo(result, code, signing_header, null);
                    this.log("MDTM Error: " + code + " " + result);
                }
                catch (UnknownHostException e) {
                    this.log(e);
                    Thread.sleep(10000L);
                    if (x == 2) {
                        this.log("AZURE_CLIENT", 1, e);
                    }
                    if (urlc != null) {
                        urlc.disconnect();
                    }
                    break block22;
                }
                catch (Exception e1) {
                    try {
                        this.log(e1);
                        this.log("AZURE_CLIENT", 1, e1);
                        break block22;
                    }
                    catch (Throwable throwable) {
                        throw throwable;
                    }
                    finally {
                        if (urlc != null) {
                            urlc.disconnect();
                        }
                    }
                }
                if (urlc == null) break block22;
                urlc.disconnect();
            }
            ++x;
        }
        return false;
    }

    @Override
    public void set_MD5_and_upload_id(String path) throws Exception {
        String restype = "";
        if (this.config.getProperty("container_type").equals("blob")) {
            restype = "";
        }
        if (this.config.getProperty("container_type").equals("share")) {
            Properties p = this.stat(path);
            restype = p.getProperty("type").equals("FILE") ? "&restype=file" : "&restype=directory";
        }
        int x = 0;
        while (x < 3) {
            URLConnection urlc = null;
            try {
                urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("username") + this.getUrl() + this.double_encode(path) + "?comp=metadata" + restype + this.getSASToken()), this.config);
                urlc.setRequestMethod("PUT");
                urlc.setRequestProperty("Content-Length", "0");
                Properties p = this.getMetadata(path);
                if (p.containsKey("X-MS-META-UPLOADED_BY")) {
                    urlc.setRequestProperty("x-ms-meta-uploaded_by", p.getProperty("X-MS-META-UPLOADED_BY", ""));
                } else if (!this.config.getProperty("uploaded_by", "").equals("")) {
                    urlc.setRequestProperty("x-ms-meta-uploaded_by", this.config.getProperty("uploaded_by", ""));
                }
                if (p.containsKey("X-MS-META-MD5")) {
                    urlc.setRequestProperty("x-ms-meta-md5", p.getProperty("X-MS-META-MD5", ""));
                } else if (!this.config.getProperty("uploaded_md5", "").equals("")) {
                    urlc.setRequestProperty("x-ms-meta-md5", this.config.getProperty("uploaded_md5", ""));
                }
                urlc.setDoOutput(true);
                urlc.setUseCaches(false);
                urlc.setRemoveDoubleEncoding(true);
                this.signRequestSK(urlc);
                String result = "";
                result = Common.consumeResponse(urlc.getInputStream());
                urlc.getResponseCode();
                this.log("AZURE_CLIENT", 2, String.valueOf(urlc.getResponseCode()) + result);
                if (urlc.getResponseCode() == 200 || urlc.getResponseCode() == 202) {
                    break;
                }
            }
            catch (UnknownHostException e) {
                this.log(e);
                Thread.sleep(10000L);
                if (x == 2) {
                    this.log("AZURE_CLIENT", 1, e);
                }
            }
            catch (Exception e2) {
                this.log(e2);
                this.log("AZURE_CLIENT", 1, e2);
            }
            finally {
                if (urlc != null) {
                    urlc.disconnect();
                }
            }
            ++x;
        }
    }

    /*
     * WARNING - Removed back jump from a try to a catch block - possible behaviour change.
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public Properties getMetadata(String path) {
        p = new Properties();
        try {
            restype = "";
            if (this.config.getProperty("container_type").equals("blob")) {
                restype = "";
            }
            if (this.config.getProperty("container_type").equals("share")) {
                p = this.stat(path);
                restype = p.getProperty("type").equals("FILE") != false ? "&restype=file" : "&restype=directory";
            }
            x = 0;
            while (true) {
                block15: {
                    if (x >= 3) {
                        return p;
                    }
                    urlc = null;
                    try {
                        urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("username") + this.getUrl() + this.double_encode(path) + "?comp=metadata" + restype + this.getSASToken()), this.config);
                        urlc.setRequestMethod("GET");
                        fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
                        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
                        date = String.valueOf(fmt.format(new Date())) + " GMT";
                        urlc.setRequestProperty("Last-Modified", date);
                        urlc.setDoOutput(true);
                        urlc.setUseCaches(false);
                        urlc.setRemoveDoubleEncoding(true);
                        this.signRequestSK(urlc);
                        result = "";
                        result = Common.consumeResponse(urlc.getInputStream());
                        if (urlc.getResponseCode() == 200 || urlc.getResponseCode() == 202) {
                            var10_11 = p = (Properties)urlc.headers.clone();
                            if (urlc == null) return var10_11;
                        }
                        ** GOTO lbl-1000
                    }
                    catch (UnknownHostException e) {
                        this.log(e);
                        Thread.sleep(10000L);
                        if (x == 2) {
                            this.log("AZURE_CLIENT", 1, e);
                        }
                        if (urlc != null) {
                            urlc.disconnect();
                        }
                        break block15;
                    }
                    catch (Throwable var9_12) {
                        if (urlc == null) throw var9_12;
                        urlc.disconnect();
                        throw var9_12;
                    }
                    urlc.disconnect();
                    return var10_11;
lbl-1000:
                    // 1 sources

                    {
                        this.log("AZURE_CLIENT", 2, String.valueOf(urlc.getResponseCode()) + " " + result);
                        urlc.disconnect();
                    }
                    if (urlc != null) {
                        urlc.disconnect();
                    }
                }
                ++x;
            }
        }
        catch (Exception e) {
            this.log(e);
            this.log("AZURE_CLIENT", 1, e);
        }
        return p;
    }

    @Override
    public String getUploadedByMetadata(String path) {
        Properties p = null;
        try {
            p = this.getMetadata(path);
        }
        catch (Exception e) {
            this.log(e);
            this.log("AZURE_CLIENT", 1, e);
        }
        if (p != null && p.containsKey("X-MS-META-UPLOADED_BY") && !p.getProperty("X-MS-META-UPLOADED_BY").equals("")) {
            return p.getProperty("X-MS-META-UPLOADED_BY");
        }
        return "";
    }

    @Override
    public boolean rename(String rnfr, String rnto, boolean overwrite) throws Exception {
        Properties p = this.stat(rnfr);
        if (p.getProperty("type").equals("FILE")) {
            int x = 0;
            while (x < 3) {
                URLConnection urlc = null;
                try {
                    urlc = URLConnection.openConnection(new VRL("https://" + this.config.getProperty("username") + this.getUrl() + this.double_encode(rnto) + (!this.getSASToken().equals("") ? "?" + this.getSASToken() : "")), this.config);
                    urlc.setRequestMethod("PUT");
                    urlc.setRequestProperty("Content-Length", "0");
                    urlc.setDoOutput(true);
                    urlc.setUseCaches(false);
                    urlc.setRequestProperty("x-ms-copy-source", "https://" + this.config.getProperty("username") + this.getUrl() + Common.url_encode(rnfr, "/") + (!this.getSASToken().equals("") ? "?" + Common.url_decode(this.getSASToken()) : ""));
                    if (this.getSASToken().equals("") || !this.getSASToken().equals("") && !this.sas_sv.equals("") && this.isOLderSV("2019-02-01", this.sas_sv)) {
                        urlc.setRequestProperty("x-ms-file-permission-copy-mode", "source");
                        urlc.setRequestProperty("x-ms-file-attributes", "source");
                        urlc.setRequestProperty("x-ms-file-creation-time", "source");
                        urlc.setRequestProperty("x-ms-file-last-write-time", "source");
                    }
                    urlc.setRemoveDoubleEncoding(true);
                    this.signRequestSK(urlc);
                    String result = Common.consumeResponse(urlc.getInputStream());
                    if (urlc.getResponseCode() != 202) {
                        urlc.disconnect();
                        return false;
                    }
                    if (this.stat(rnto) != null) {
                        boolean bl = this.delete(rnfr);
                        return bl;
                    }
                    return false;
                }
                catch (UnknownHostException e) {
                    this.log(e);
                    Thread.sleep(10000L);
                    if (x == 2) {
                        this.log(e);
                        this.log("AZURE_CLIENT", 1, e);
                    }
                }
                catch (Exception e2) {
                    this.log(e2);
                    this.log("AZURE_CLIENT", 1, e2);
                }
                finally {
                    if (urlc != null) {
                        urlc.disconnect();
                    }
                }
                ++x;
            }
        } else {
            this.log("AZURE_CLIENT", 0, "Azure API does not support renaming folders.");
            this.log("Azure API does not support renaming folders.");
            throw new Exception("FAILURE: Renaming of Cloud (sub)Directories is not supported, aborting rename operation.");
        }
        return false;
    }

    public void signRequestSK(URLConnection urlc) throws Exception {
        String path;
        String sas_token;
        String ms_version = "2020-04-08";
        if (this.config.getProperty("container_type").equals("blob")) {
            ms_version = "2019-02-02";
        }
        if (!this.config.getProperty("timeout", "60").equals("") && Integer.parseInt(this.config.getProperty("timeout", "60")) < 60) {
            urlc.setReadTimeout(Integer.parseInt(this.config.getProperty("timeout", "20")) * 1000);
        }
        if (!(sas_token = this.getSASToken()).equals("")) {
            urlc.setRequestProperty("x-ms-version", this.sas_sv.equals("") ? ms_version : this.sas_sv);
            urlc.setRemoveDoubleEncoding(true);
            return;
        }
        urlc.setRequestProperty("x-ms-version", ms_version);
        String date = urlc.sdf_rfc1123.format(urlc.getDate());
        String sb = "";
        sb = String.valueOf(sb) + urlc.getRequestMethod() + "\n";
        sb = String.valueOf(sb) + "\n";
        sb = String.valueOf(sb) + "\n";
        sb = urlc.getRequestProps().getProperty("Content-Length") != null && !urlc.getRequestProps().getProperty("Content-Length").equals("0") ? String.valueOf(sb) + urlc.getRequestProps().getProperty("Content-Length") + "\n" : String.valueOf(sb) + "\n";
        sb = String.valueOf(sb) + "\n";
        sb = urlc.getRequestProps().getProperty("Content-Type") != null && !urlc.getRequestProps().getProperty("Content-Type").equals("0") ? String.valueOf(sb) + urlc.getRequestProps().getProperty("Content-Type") + "\n" : String.valueOf(sb) + "application/x-www-form-urlencoded; charset=UTF-8\n";
        sb = String.valueOf(sb) + "\n";
        sb = String.valueOf(sb) + "\n";
        sb = String.valueOf(sb) + "\n";
        sb = String.valueOf(sb) + "\n";
        sb = String.valueOf(sb) + "\n";
        String range = "\n";
        if (urlc.getRequestProps().getProperty("Range") != null) {
            range = String.valueOf(urlc.getRequestProps().getProperty("Range")) + "\n";
        }
        sb = String.valueOf(sb) + range;
        if (urlc.getRequestProps().getProperty("x-ms-blob-content-length") != null) {
            sb = String.valueOf(sb) + "x-ms-blob-content-length:" + urlc.getRequestProps().getProperty("x-ms-blob-content-length") + "\n";
        }
        if (urlc.getRequestProps().getProperty("x-ms-blob-content-type") != null) {
            sb = String.valueOf(sb) + "x-ms-blob-content-type:" + urlc.getRequestProps().getProperty("x-ms-blob-content-type") + "\n";
        }
        if (urlc.getRequestProps().getProperty("x-ms-blob-type") != null) {
            sb = String.valueOf(sb) + "x-ms-blob-type:" + urlc.getRequestProps().getProperty("x-ms-blob-type") + "\n";
        }
        if (urlc.getRequestProps().getProperty("x-ms-content-length") != null) {
            sb = String.valueOf(sb) + "x-ms-content-length:" + urlc.getRequestProps().getProperty("x-ms-content-length") + "\n";
        }
        if (urlc.getRequestProps().getProperty("x-ms-copy-source") != null) {
            sb = String.valueOf(sb) + "x-ms-copy-source:" + urlc.getRequestProps().getProperty("x-ms-copy-source") + "\n";
        }
        sb = String.valueOf(sb) + "x-ms-date:" + date + "\n";
        if (urlc.getRequestProps().getProperty("x-ms-file-attributes") != null) {
            sb = String.valueOf(sb) + "x-ms-file-attributes:" + urlc.getRequestProps().getProperty("x-ms-file-attributes") + "\n";
        }
        if (urlc.getRequestProps().getProperty("x-ms-file-creation-time") != null) {
            sb = String.valueOf(sb) + "x-ms-file-creation-time:" + urlc.getRequestProps().getProperty("x-ms-file-creation-time") + "\n";
        }
        if (urlc.getRequestProps().getProperty("x-ms-file-last-write-time") != null) {
            sb = String.valueOf(sb) + "x-ms-file-last-write-time:" + urlc.getRequestProps().getProperty("x-ms-file-last-write-time") + "\n";
        }
        if (urlc.getRequestProps().getProperty("x-ms-file-permission") != null) {
            sb = String.valueOf(sb) + "x-ms-file-permission:" + urlc.getRequestProps().getProperty("x-ms-file-permission") + "\n";
        }
        if (urlc.getRequestProps().getProperty("x-ms-file-permission-copy-mode") != null) {
            sb = String.valueOf(sb) + "x-ms-file-permission-copy-mode:" + urlc.getRequestProps().getProperty("x-ms-file-permission-copy-mode") + "\n";
        }
        if (urlc.getRequestProps().getProperty("x-ms-meta-hdi_isfolder") != null) {
            sb = String.valueOf(sb) + "x-ms-meta-hdi_isfolder:" + urlc.getRequestProps().getProperty("x-ms-meta-hdi_isfolder") + "\n";
        }
        if (urlc.getRequestProps().getProperty("x-ms-meta-md5") != null) {
            sb = String.valueOf(sb) + "x-ms-meta-md5:" + urlc.getRequestProps().getProperty("x-ms-meta-md5") + "\n";
        }
        if (urlc.getRequestProps().getProperty("x-ms-meta-uploaded_by") != null) {
            sb = String.valueOf(sb) + "x-ms-meta-uploaded_by:" + urlc.getRequestProps().getProperty("x-ms-meta-uploaded_by") + "\n";
        }
        if (urlc.getRequestProps().getProperty("x-ms-page-write") != null) {
            sb = String.valueOf(sb) + "x-ms-page-write:" + urlc.getRequestProps().getProperty("x-ms-page-write") + "\n";
        }
        if (urlc.getRequestProps().getProperty("x-ms-range") != null) {
            sb = String.valueOf(sb) + "x-ms-range:" + urlc.getRequestProps().getProperty("x-ms-range") + "\n";
        }
        if (urlc.getRequestProps().getProperty("x-ms-type") != null) {
            sb = String.valueOf(sb) + "x-ms-type:" + urlc.getRequestProps().getProperty("x-ms-type") + "\n";
        }
        sb = String.valueOf(sb) + "x-ms-version:" + ms_version + "\n";
        if (urlc.getRequestProps().getProperty("x-ms-write") != null) {
            sb = String.valueOf(sb) + "x-ms-write:" + urlc.getRequestProps().getProperty("x-ms-write") + "\n";
        }
        if ((path = urlc.getURL().getPath()).contains("?")) {
            String path2 = Common.url_encode(path.substring(0, path.indexOf("?")), "/%.#@&!\\=+");
            if (urlc.getRemoveDoubleEncoding()) {
                path2 = URLConnection.remove_double_encoding_of_special_chars(path.substring(0, path.indexOf("?")));
            }
            sb = String.valueOf(sb) + "/" + this.config.getProperty("username") + path2 + "\n";
            String commands = path.substring(path.indexOf("?") + 1, path.length());
            commands = commands.replaceAll("&", "\n").replaceAll("=", ":");
            if (urlc.getRemoveDoubleEncoding()) {
                commands = Common.url_decode(commands);
            }
            sb = String.valueOf(sb) + commands;
        } else {
            sb = urlc.getRemoveDoubleEncoding() ? String.valueOf(sb) + "/" + this.config.getProperty("username") + URLConnection.remove_double_encoding_of_special_chars(Common.url_encode(path, " /%")) : String.valueOf(sb) + "/" + this.config.getProperty("username") + Common.url_encode(path, "/%.#@&!\\=+");
        }
        urlc.putConfig("signing_header", sb.toString());
        this.log("AZURE_CLIENT", 2, "Signing header : " + sb.replace("\n", " "));
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(Base64.decode(this.config.getProperty("password")), "HmacSHA256"));
        String authKey = new String(Base64.encodeBytes(mac.doFinal(sb.toString().getBytes("UTF-8"))));
        String auth = "SharedKey " + this.config.getProperty("username") + ":" + authKey;
        urlc.setRequestProperty("x-ms-date", date);
        urlc.setRequestProperty("Authorization", auth);
    }

    private String getUrl() {
        String url = ".file.core.windows.net";
        if (this.config.getProperty("container_type").equals("blob")) {
            url = ".blob.core.windows.net";
        }
        return url;
    }

    private String getSASToken() {
        if (this.sas_token == null) {
            String token = this.config.getProperty("sas_token", "");
            try {
                token = Common.encryptDecrypt(token, false);
                token = VRL.vrlDecode(token);
                token = Common.url_decode(token);
            }
            catch (Exception e) {
                this.log("AZURE_CLIENT", 2, e);
            }
            if (token.startsWith("?")) {
                token = "&" + token.substring(1);
            }
            if (!token.equals("") && !token.startsWith("&")) {
                token = "&" + token;
            }
            try {
                if (token.indexOf("sv=") > 0) {
                    int sv_index_start = token.indexOf("sv=") + 3;
                    int sv_index_end = token.indexOf("&", sv_index_start);
                    if (sv_index_end < 0) {
                        sv_index_end = token.length();
                    }
                    this.sas_sv = token.substring(sv_index_start, sv_index_end);
                }
            }
            catch (Exception e) {
                this.log("AZURE_CLIENT", 2, e);
            }
            if (token.contains("sig=")) {
                String sig;
                String sig_double_encoded = sig = token.substring(token.indexOf("sig=") + 4, token.indexOf("&", token.indexOf("sig=") + 4) > 0 ? token.indexOf("&", token.indexOf("sig=") + 4) : token.length());
                sig_double_encoded = this.double_encodesig_sig(sig_double_encoded);
                token = Common.replace_str(token, sig, sig_double_encoded);
            }
            this.sas_token = token;
        }
        return this.sas_token;
    }

    private String double_encodesig_sig(String text) {
        if (text.contains("/")) {
            text = text.replace("/", "%252F");
        }
        if (text.contains("%2F")) {
            text = text.replace("%2F", "%252F");
        }
        if (text.contains("\\")) {
            text = text.replace("\\", "%255C");
        }
        if (text.contains("%5C")) {
            text = text.replace("%5C", "%255C");
        }
        text = this.double_encode(text);
        return text;
    }

    private String double_encode(String text) {
        if (text.contains(" ")) {
            text = text.replace(" ", "%2520");
        }
        if (text.contains("@")) {
            text = text.replace("@", "%2540");
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
        if (text.contains(",")) {
            text = text.replace(",", "%252C");
        }
        if (text.contains("~")) {
            text = text.replace("~", "%257E");
        }
        if (text.contains("!")) {
            text = text.replace("!", "%2521");
        }
        if (text.contains("&")) {
            text = text.replace("&", "%2526");
        }
        if (text.contains("$")) {
            text = text.replace("$", "%2524");
        }
        if (text.contains("(")) {
            text = text.replace("(", "%2528");
        }
        if (text.contains(")")) {
            text = text.replace(")", "%2529");
        }
        if (text.contains(")")) {
            text = text.replace(")", "%2529");
        }
        if (text.contains("'")) {
            text = text.replace("'", "%2527");
        }
        if (text.contains("%20")) {
            text = text.replace("%20", "%2520");
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
        if (text.contains("%2C")) {
            text = text.replace("%2C", "%252C");
        }
        if (text.contains("%7E")) {
            text = text.replace("%7E", "%257E");
        }
        if (text.contains("%21")) {
            text = text.replace("%21", "%2521");
        }
        if (text.contains("%26")) {
            text = text.replace("%26", "%2526");
        }
        if (text.contains("%24")) {
            text = text.replace("%24", "%2524");
        }
        if (text.contains("%28")) {
            text = text.replace("%28", "%2528");
        }
        if (text.contains("%29")) {
            text = text.replace("%29", "%2529");
        }
        if (text.contains("%27")) {
            text = text.replace("%27", "%2527");
        }
        return text;
    }

    private boolean isOLderSV(String date1, String date2) {
        boolean result = false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date d1 = sdf.parse(date1);
            Date d2 = sdf.parse(date2);
            if (d1.before(d2)) {
                result = true;
            }
        }
        catch (Exception e) {
            this.log("AZURE_CLIENT", 2, e);
        }
        return result;
    }

    public String getErrorInfo(String result, int code, String signing_header, Properties error_config) {
        try {
            if (!result.equals("")) {
                Element error;
                this.log("AZURE_CLIENT", 2, result);
                if (result.indexOf("<?xml version=") >= 0 && (error = Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(result.getBytes("UTF8"))).getRootElement()) != null) {
                    String error_message;
                    String error_code = AzureClient.getElement(error, "Code") == null ? "" : AzureClient.getElement(error, "Code").getText();
                    String string = error_message = AzureClient.getElement(error, "Message") == null ? "" : AzureClient.getElement(error, "Message").getText();
                    if (error_config != null && error_config.getProperty("sas_token", "").equals("true") && error_code.contains("AuthenticationFailed")) {
                        result = "ERROR : Bad credentials : Invalid Azure SAS Token! Error Code : " + code + " " + error_message;
                        return result;
                    }
                    result = "Error Code : " + code + " " + error_code + " Error Message: " + error_message;
                    if (error_code.contains("AuthenticationFailed") && error_config != null && error_config.getProperty("sas_token", "").equals("true") && signing_header != null && !signing_header.equals("")) {
                        String signing_header_azure;
                        String string2 = signing_header_azure = AzureClient.getElement(error, "AuthenticationErrorDetail") == null ? "" : AzureClient.getElement(error, "AuthenticationErrorDetail").getText();
                        if (signing_header_azure.contains("Server used following string to sign: '")) {
                            int start_index = signing_header_azure.indexOf("Server used following string to sign: '") + "Server used following string to sign: '".length();
                            if ((signing_header_azure = signing_header_azure.substring(start_index)).endsWith("'.")) {
                                signing_header_azure = signing_header_azure.substring(0, signing_header_azure.length() - 2);
                            }
                        } else {
                            signing_header_azure = "";
                        }
                        if (!signing_header_azure.equals("")) {
                            if (error_config != null && error_config.getProperty("login_error", "").equals("true") && signing_header_azure.trim().equals(signing_header.trim())) {
                                result = "ERROR : Bad credentials : Invalid Password! Error Code : " + code + " " + error_code;
                            } else {
                                String difference = "";
                                BufferedReader reader1 = new BufferedReader(new StringReader(signing_header.trim()));
                                BufferedReader reader2 = new BufferedReader(new StringReader(signing_header_azure.trim()));
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
                }
            }
        }
        catch (Exception e) {
            this.log("AZURE_CLIENT", 2, e);
        }
        return result;
    }

    static /* synthetic */ void access$0(AzureClient azureClient, Properties properties, String string) throws Exception {
        azureClient.parseLastModified(properties, string);
    }
}

