/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jdom.Content
 *  org.jdom.Document
 *  org.jdom.Element
 *  org.jdom.Namespace
 *  org.jdom.output.Format
 *  org.jdom.output.XMLOutputter
 */
package crushftp.server;

import com.crushftp.client.Common;
import com.crushftp.client.GenericClient;
import crushftp.gui.LOC;
import crushftp.handlers.Log;
import crushftp.handlers.WebTransfer;
import crushftp.server.LIST_handler;
import crushftp.server.ServerSessionHTTP;
import crushftp.server.ServerStatus;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Vector;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class ServerSessionS3 {
    ServerSessionHTTP thisSessionHTTP = null;
    SimpleDateFormat sdf_rfc1123_2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

    public ServerSessionS3(ServerSessionHTTP thisSessionHTTP) {
        this.thisSessionHTTP = thisSessionHTTP;
    }

    public Properties process(Properties request) throws Exception {
        String etag;
        Document doc;
        String path;
        block58: {
            String verb = request.getProperty("header0", "").substring(0, request.getProperty("header0", "").indexOf(" "));
            String actual_path = this.thisSessionHTTP.pwd();
            String params = this.thisSessionHTTP.pwd();
            if (actual_path.indexOf("?") >= 0) {
                actual_path = actual_path.substring(0, actual_path.indexOf("?"));
            }
            params = params.indexOf("?") >= 0 ? params.substring(actual_path.indexOf("?") + 1) : "";
            boolean list_buckets = actual_path.equals(this.thisSessionHTTP.thisSession.SG("root_dir"));
            path = "";
            try {
                path = this.unBucketPath(this.thisSessionHTTP.pwd(), request);
                this.thisSessionHTTP.cd(path);
            }
            catch (Exception e) {
                Log.log("HTTP_SERVER", 1, e);
            }
            Element root = new Element("ListBucketResult");
            Namespace ns0 = Namespace.getNamespace((String)"ns0", (String)"http://s3.amazonaws.com/doc/2006-03-01/");
            root.addNamespaceDeclaration(ns0);
            doc = new Document(root);
            etag = "";
            Properties item = null;
            try {
                item = this.thisSessionHTTP.thisSession.uVFS.get_item(path);
            }
            catch (Exception e) {
                Log.log("HTTP_SERVER", 1, e);
            }
            if (verb.equalsIgnoreCase("PUT") && !this.thisSessionHTTP.headerLookup.getProperty("X-AMZ-COPY-SOURCE", "").equals("")) {
                try {
                    String path_src;
                    String rnfr_error_message;
                    String tmp_path = this.thisSessionHTTP.headerLookup.getProperty("X-AMZ-COPY-SOURCE", "");
                    if (!tmp_path.startsWith("/")) {
                        tmp_path = "/" + tmp_path;
                    }
                    if ((rnfr_error_message = this.thisSessionHTTP.thisSession.do_RNFR(path_src = this.unBucketPath(tmp_path, request))).equals("")) {
                        String rnto_error_message = this.thisSessionHTTP.thisSession.do_RNTO(true, path_src, path);
                        if (rnto_error_message.equals("")) {
                            item = this.thisSessionHTTP.thisSession.uVFS.get_item(path);
                            root.setName("CopyObjectResult");
                            root.addContent((Content)this.getElement("LastModified", this.sdf_rfc1123_2.format(new Date(Long.parseLong(item.getProperty("modified", "0"))))));
                            root.addContent((Content)this.getElement("ETag", Common.getMD5(new ByteArrayInputStream(new byte[0]))));
                            break block58;
                        }
                        Log.log("SERVER", 1, "S3 server: Copy Object error: " + rnfr_error_message + " Path: " + path);
                        return this.wrtie_http_empty_response_message(path, "403", LOC.G(rnfr_error_message));
                    }
                    Log.log("SERVER", 1, "S3 server: Copy Object error: " + rnfr_error_message + " Path: " + path);
                    return this.wrtie_http_empty_response_message(path, "403", LOC.G(rnfr_error_message));
                }
                catch (Exception e) {
                    Log.log("HTTP_SERVER", 1, e);
                    return this.wrtie_http_empty_response_message(path, "500", "Internal Server Error.");
                }
            }
            if (verb.equalsIgnoreCase("DELETE")) {
                try {
                    String error_message = this.thisSessionHTTP.thisSession.do_DELE(false, path);
                    if (error_message.equals("") || error_message.equals("%DELE-not found%")) {
                        return this.wrtie_http_empty_response_message(path, "204", "No Content");
                    }
                    Log.log("SERVER", 1, "S3 server: Delete error: " + error_message + " Path: " + path);
                    return this.wrtie_http_empty_response_message(path, "403", LOC.G(error_message));
                }
                catch (Exception e) {
                    Log.log("HTTP_SERVER", 1, e);
                    return this.wrtie_http_empty_response_message(path, "500", "Internal Server Error.");
                }
            }
            if (verb.equalsIgnoreCase("POST") && params.indexOf("uploads") >= 0) {
                Properties p = new Properties();
                try {
                    String tmp_path = request.getProperty("prefix", actual_path);
                    request.put("prefix", "");
                    if (!tmp_path.startsWith("/")) {
                        tmp_path = "/" + tmp_path;
                    }
                    tmp_path = this.unBucketPath(tmp_path, request);
                    String UploadId = String.valueOf(Common.makeBoundary(20)) + "_" + Common.makeBoundary(20) + "--";
                    root.setName("InitiateMultipartUploadResult");
                    root.addContent((Content)this.getElement("Bucket", "bucket"));
                    root.addContent((Content)this.getElement("Key", tmp_path));
                    root.addContent((Content)this.getElement("UploadId", UploadId));
                    p.put("command", "openFile");
                    p.put("transfer_type", "upload");
                    p.put("upload_path", tmp_path);
                    p.put("upload_size", "-1");
                    p.put("upload_id", UploadId);
                    p.put("start_resume_loc", "0");
                    p.put("internal_response", "true");
                }
                catch (Exception e) {
                    Log.log("HTTP_SERVER", 1, e);
                    return this.wrtie_http_empty_response_message(path, "500", "Internal Server Error.");
                }
                this.thisSessionHTTP.ssa.processItems(p, null, Common.makeBoundary(3));
            } else {
                if (verb.equalsIgnoreCase("POST") && params.indexOf("uploadId") >= 0) {
                    try {
                        String result = this.thisSessionHTTP.get_raw_http_command((int)this.thisSessionHTTP.http_len_max);
                        int total_chunks = 0;
                        Element root2 = Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(result.getBytes("UTF8"))).getRootElement();
                        List part = GenericClient.getElements(root2, "Part");
                        int x = 0;
                        while (x < part.size()) {
                            ++total_chunks;
                            ++x;
                        }
                        String UploadId = request.getProperty("uploadId");
                        root.setName("CompleteMultipartUploadResult");
                        root.addContent((Content)this.getElement("Bucket", "bucket"));
                        root.addContent((Content)this.getElement("Key", path));
                        root.addContent((Content)this.getElement("UploadId", UploadId));
                        Properties p = new Properties();
                        p.put("command", "closeFile");
                        p.put("transfer_type", "upload");
                        p.put("upload_path", path);
                        p.put("upload_size", "-1");
                        p.put("upload_id", UploadId);
                        p.put("total_chunks", String.valueOf(total_chunks));
                        p.put("start_resume_loc", "0");
                        p.put("internal_response", "true");
                        this.thisSessionHTTP.ssa.processItems(p, null, Common.makeBoundary(3));
                    }
                    catch (Exception e) {
                        Log.log("HTTP_SERVER", 1, e);
                        return this.wrtie_http_empty_response_message(path, "500", "Internal Server Error.");
                    }
                }
                if (verb.equalsIgnoreCase("PUT") && params.indexOf("uploadId") >= 0) {
                    try {
                        if (request.containsKey("expect")) {
                            this.thisSessionHTTP.write_command_http("HTTP/1.1 100 Continue");
                            this.thisSessionHTTP.write_command_http("");
                        }
                        Properties html5_transfers = ServerStatus.siPG("html5_transfers");
                        WebTransfer transfer_lock = (WebTransfer)html5_transfers.get(String.valueOf(this.thisSessionHTTP.thisSession.uiSG("user_protocol")) + this.thisSessionHTTP.thisSession.uiSG("user_name") + this.thisSessionHTTP.thisSession.uiSG("user_ip") + "_" + request.getProperty("uploadId"));
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        int bytes_read = 0;
                        long remaining = this.thisSessionHTTP.http_len_max;
                        byte[] b = new byte[0x500000];
                        while (remaining > 0L) {
                            if ((long)b.length > remaining) {
                                b = new byte[(int)remaining];
                            }
                            if ((bytes_read = this.thisSessionHTTP.original_is.read(b, 0, b.length)) <= 0) continue;
                            remaining -= (long)bytes_read;
                            baos.write(b, 0, bytes_read);
                        }
                        etag = Common.getMD5(new ByteArrayInputStream(baos.toByteArray()));
                        transfer_lock.addChunk(String.valueOf(request.getProperty("partNumber")), baos.toByteArray());
                    }
                    catch (Exception e) {
                        Log.log("HTTP_SERVER", 1, e);
                        return this.wrtie_http_empty_response_message(path, "500", "Internal Server Error.");
                    }
                }
                if (verb.equalsIgnoreCase("HEAD")) {
                    if (item != null && !item.getProperty("type").equalsIgnoreCase("DIR")) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
                        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                        Date d = new Date(Long.parseLong(item.getProperty("modified", "0")));
                        this.thisSessionHTTP.write_command_http("HTTP/1.1 200 OK");
                        this.thisSessionHTTP.write_command_http("Last-Modified: " + sdf.format(d));
                        this.thisSessionHTTP.write_command_http("Content-Length: " + item.getProperty("size", "0"));
                        this.thisSessionHTTP.write_command_http("ETag: " + Common.getMD5(new ByteArrayInputStream(new byte[0])));
                        this.thisSessionHTTP.write_command_http("Connection: close");
                        this.thisSessionHTTP.write_command_http("");
                        Properties result = new Properties();
                        result.put("action", "head");
                        result.put("path", path);
                        return result;
                    }
                    this.thisSessionHTTP.write_command_http("HTTP/1.1 404 Not Found");
                    String html404 = ServerStatus.SG("web404Text");
                    html404 = ServerStatus.thisObj.change_vars_to_values(html404, this.thisSessionHTTP.thisSession);
                    this.thisSessionHTTP.write_command_http("Content-Length: " + (html404.getBytes("UTF8").length + 2));
                    this.thisSessionHTTP.write_standard_headers();
                    this.thisSessionHTTP.write_command_http("");
                    this.thisSessionHTTP.write_command_http(html404);
                    this.thisSessionHTTP.write_command_http("Connection: close");
                    this.thisSessionHTTP.write_command_http("");
                    Properties result = new Properties();
                    result.put("action", "head");
                    result.put("path", path);
                    return result;
                }
                if (item == null && verb.equalsIgnoreCase("PUT") && path.endsWith("/") && this.thisSessionHTTP.http_len_max == 0L) {
                    try {
                        String error_message = this.thisSessionHTTP.thisSession.do_MKD(true, path);
                        if (error_message.equals("")) {
                            return this.wrtie_http_empty_response_message(path, "204", "No Content");
                        }
                        Log.log("SERVER", 1, "S3 server: MKD error: " + error_message + " Path: " + path);
                        return this.wrtie_http_empty_response_message(path, "403", LOC.G(error_message));
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 1, "S3 server: MKD error: " + e + " Path: " + path);
                        return this.wrtie_http_empty_response_message(path, "500", "Internal server error.");
                    }
                }
                if (item == null && verb.equalsIgnoreCase("PUT")) {
                    Properties result = new Properties();
                    result.put("action", "put");
                    result.put("path", path);
                    return result;
                }
                if (item != null && !item.getProperty("type").equalsIgnoreCase("DIR")) {
                    Properties result = new Properties();
                    if (verb.equalsIgnoreCase("GET")) {
                        request.put("command", "download");
                        request.put("path", path);
                    } else {
                        result.put("action", "serve file");
                        result.put("path", path);
                        result.put("item", item);
                    }
                    return result;
                }
                if (list_buckets) {
                    root.addContent((Content)this.getElement("Name", "bucket"));
                    root.addContent((Content)this.getElement("Prefix", ""));
                    root.addContent((Content)this.getElement("Marker", ""));
                    root.addContent((Content)this.getElement("MaxKeys", "1000"));
                    root.addContent((Content)this.getElement("Delimiter", "/"));
                    root.addContent((Content)this.getElement("IsTruncated", "false"));
                    Element contents = new Element("CommonPrefixes");
                    contents.addContent((Content)this.getElement("Prefix", "bucket"));
                    root.addContent((Content)contents);
                } else {
                    Vector<Properties> items = new Vector<Properties>();
                    int max_keys = 1000;
                    if (request.containsKey("max-keys") && Integer.parseInt(request.getProperty("max-keys", "1000")) == 1) {
                        max_keys = 1;
                    }
                    if (max_keys > 1) {
                        try {
                            this.thisSessionHTTP.thisSession.uVFS.getListing(items, path);
                        }
                        catch (Exception e) {
                            Log.log("HTTP_SERVER", 1, e);
                        }
                    }
                    if (item != null && !request.getProperty("prefix", "").equals("")) {
                        String name;
                        if (item.getProperty("type", "").equalsIgnoreCase("DIR")) {
                            if (!item.getProperty("size", "0").equals("0")) {
                                item.put("size", "0");
                            }
                            item.put("type", "FILE");
                        }
                        if (!(name = request.getProperty("prefix", "")).endsWith("/")) {
                            name = String.valueOf(name) + "/";
                        }
                        item.put("name", name);
                        items.add(item);
                    }
                    Properties p = new Properties();
                    p.put("listing", items);
                    this.thisSessionHTTP.thisSession.runPlugin("list", p);
                    root.addContent((Content)this.getElement("Name", "bucket"));
                    root.addContent((Content)this.getElement("Prefix", ""));
                    root.addContent((Content)this.getElement("Marker", ""));
                    root.addContent((Content)this.getElement("MaxKeys", "1000"));
                    root.addContent((Content)this.getElement("Delimiter", "/"));
                    root.addContent((Content)this.getElement("IsTruncated", "false"));
                    try {
                        int x = 0;
                        while (x < items.size()) {
                            item = (Properties)items.elementAt(x);
                            if (LIST_handler.checkName(item, this.thisSessionHTTP.thisSession, false, false)) {
                                Element contents;
                                if (!item.getProperty("type", "").equalsIgnoreCase("DIR")) {
                                    contents = new Element("Contents");
                                    contents.addContent((Content)this.getElement("Key", item.getProperty("name")));
                                    contents.addContent((Content)this.getElement("Size", item.getProperty("size")));
                                    contents.addContent((Content)this.getElement("StorageClass", "STANDARD"));
                                    contents.addContent((Content)this.getElement("LastModified", this.sdf_rfc1123_2.format(new Date(Long.parseLong(item.getProperty("modified", "0"))))));
                                    root.addContent((Content)contents);
                                } else {
                                    contents = new Element("CommonPrefixes");
                                    contents.addContent((Content)this.getElement("Prefix", String.valueOf(item.getProperty("name")) + "/"));
                                    root.addContent((Content)contents);
                                }
                            }
                            ++x;
                        }
                    }
                    catch (Exception e) {
                        Log.log("HTTP_SERVER", 1, e);
                    }
                }
            }
        }
        if (this.thisSessionHTTP.xmlOut == null) {
            this.thisSessionHTTP.xmlOut = new XMLOutputter();
            Format f = Format.getPrettyFormat();
            f.setExpandEmptyElements(false);
            this.thisSessionHTTP.xmlOut.setFormat(f);
        }
        String xml = this.thisSessionHTTP.xmlOut.outputString(doc);
        this.thisSessionHTTP.write_command_http("HTTP/1.1 200 S3_OK");
        this.thisSessionHTTP.write_standard_headers();
        this.thisSessionHTTP.write_command_http("Content-Length: " + (xml.getBytes("UTF8").length + 2));
        this.thisSessionHTTP.write_command_http("Content-Type: text/xml; charset=\"utf-8\"");
        if (!etag.equals("")) {
            this.thisSessionHTTP.write_command_http("ETag: " + etag);
        }
        this.thisSessionHTTP.write_command_http("");
        this.thisSessionHTTP.write_command_http(xml);
        Properties result = new Properties();
        result.put("action", "s3");
        result.put("path", path);
        return result;
    }

    private Properties wrtie_http_empty_response_message(String path, String code, String http_message) throws Exception {
        this.thisSessionHTTP.write_command_http("HTTP/1.1 " + code + " " + http_message);
        this.thisSessionHTTP.write_standard_headers();
        this.thisSessionHTTP.write_command_http("Content-Length: 0");
        this.thisSessionHTTP.write_command_http("");
        Properties result = new Properties();
        result.put("action", "s3");
        result.put("path", path);
        return result;
    }

    public Element getElement(String key, String val) {
        Element el = new Element(key);
        el.setText(val);
        return el;
    }

    public String unBucketPath(String path, Properties request) {
        if (!this.thisSessionHTTP.thisSession.SG("root_dir").equals("")) {
            if (path.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                path = path.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length());
            }
            if (path.indexOf("?") >= 0) {
                path = path.substring(0, path.indexOf("?"));
            }
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (path.indexOf("/") <= 0) {
                path = String.valueOf(path) + "/";
            }
            path = path.substring(path.indexOf("/") + 1);
            path = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + path;
            path = String.valueOf(path) + request.getProperty("prefix", "");
        }
        return path;
    }
}

