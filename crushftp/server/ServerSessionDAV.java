/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jdom.Content
 *  org.jdom.Document
 *  org.jdom.Element
 *  org.jdom.Namespace
 *  org.jdom.input.SAXBuilder
 *  org.jdom.output.Format
 *  org.jdom.output.XMLOutputter
 */
package crushftp.server;

import com.crushftp.client.Common;
import com.crushftp.client.GenericClient;
import com.crushftp.client.VRL;
import crushftp.gui.LOC;
import crushftp.handlers.Log;
import crushftp.server.LIST_handler;
import crushftp.server.ServerSessionHTTP;
import crushftp.server.ServerStatus;
import java.io.Reader;
import java.io.StringReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.SimpleTimeZone;
import java.util.Vector;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class ServerSessionDAV {
    ServerSessionHTTP thisSessionHTTP = null;
    SimpleDateFormat sdf_rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    SimpleDateFormat sdf_rfc1123_2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
    SAXBuilder sax = crushftp.handlers.Common.getSaxBuilder();
    XMLOutputter xmlOut = null;
    Socket sock = null;

    public ServerSessionDAV(Socket sock, ServerSessionHTTP thisSessionHTTP) {
        this.sock = sock;
        this.thisSessionHTTP = thisSessionHTTP;
        this.sdf_rfc1123.setCalendar(Calendar.getInstance(new SimpleTimeZone(0, "GMT")));
    }

    /*
     * Unable to fully structure code
     */
    public String doPropFind(Properties commandActions, String depth, boolean listProps, Vector fieldOrder) throws Exception {
        if (!this.thisSessionHTTP.pwd().toUpperCase().startsWith(this.thisSessionHTTP.thisSession.SG("root_dir").toUpperCase())) {
            this.thisSessionHTTP.cd(String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + (this.thisSessionHTTP.pwd().startsWith("/") != false ? this.thisSessionHTTP.pwd().substring(1) : this.thisSessionHTTP.pwd()));
        }
        D = Namespace.getNamespace((String)"D", (String)"DAV:");
        ns0 = Namespace.getNamespace((String)"ns0", (String)"DAV:");
        lp1 = Namespace.getNamespace((String)"lp1", (String)"DAV:");
        lp2 = Namespace.getNamespace((String)"lp2", (String)"http://apache.org/dav/props/");
        g0 = Namespace.getNamespace((String)"g0", (String)"DAV:");
        root = new Element("multistatus", D);
        root.addNamespaceDeclaration(ns0);
        doc = new Document(root);
        item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.pwd());
        if (item == null) {
            return null;
        }
        if (item.getProperty("type").toUpperCase().equals("DIR") && !this.thisSessionHTTP.pwd().endsWith("/")) {
            this.thisSessionHTTP.cd(String.valueOf(this.thisSessionHTTP.pwd()) + "/");
        }
        items = new Vector<Properties>();
        if (!depth.equals("0")) {
            if (depth.indexOf("inf") >= 0) {
                this.thisSessionHTTP.thisSession.uVFS.getListing(items, this.thisSessionHTTP.pwd(), 999, 10000, true);
            } else if (depth.equals("1")) {
                this.thisSessionHTTP.thisSession.uVFS.getListing(items, this.thisSessionHTTP.pwd());
            }
            p = new Properties();
            p.put("listing", items);
            this.thisSessionHTTP.thisSession.runPlugin("list", p);
        }
        items.insertElementAt(item, 0);
        quota = -12345L;
        total_quota = -12345L;
        try {
            quota = this.thisSessionHTTP.thisSession.get_quota_used(this.thisSessionHTTP.pwd());
        }
        catch (Exception var18_17) {
            // empty catch block
        }
        try {
            total_quota = this.thisSessionHTTP.thisSession.get_total_quota(this.thisSessionHTTP.pwd());
        }
        catch (Exception var18_18) {
            // empty catch block
        }
        if (quota == -12345L) {
            quota = 0L;
        }
        listAdditionalProps = false;
        if (commandActions.size() == 0) {
            commandActions.put("getcontentlength", "");
            commandActions.put("getlastmodified", "");
            commandActions.put("modificationdate", "");
            fieldOrder.addElement("getcontentlength");
            fieldOrder.addElement("getlastmodified");
            fieldOrder.addElement("modificationdate");
            commandActions.put("getcontenttype", "");
            fieldOrder.addElement("getcontenttype");
            if (item.getProperty("type").toUpperCase().equals("DIR")) {
                commandActions.put("resourceType", "");
                fieldOrder.addElement("resourceType");
                if (quota != -12345L) {
                    commandActions.put("quota", "");
                    commandActions.put("quotaused", "");
                    if (fieldOrder.indexOf("quota") < 0) {
                        fieldOrder.addElement("quota");
                    }
                    if (fieldOrder.indexOf("quotaused") < 0) {
                        fieldOrder.addElement("quotaused");
                    }
                }
            }
            listAdditionalProps = true;
        }
        hrefs = new Vector<String>();
        added = 0;
        x = 0;
        while (x < items.size()) {
            item = (Properties)items.elementAt(x);
            if (LIST_handler.checkName(item, this.thisSessionHTTP.thisSession, false, false)) {
                ++added;
                href = new Element("href", D);
                dir = this.thisSessionHTTP.pwd();
                if (!this.thisSessionHTTP.thisSession.SG("root_dir").equals("/")) {
                    dir = dir.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
                }
                if (!depth.equals("0") && x > 0) {
                    href.setText(String.valueOf(crushftp.handlers.Common.url_encode(dir, "/")) + crushftp.handlers.Common.url_encode(item.getProperty("name", "")));
                } else {
                    href.setText(crushftp.handlers.Common.url_encode(dir, "/"));
                }
                if (depth.indexOf("inf") >= 0) {
                    dir = item.getProperty("root_dir");
                    if (dir.startsWith(this.thisSessionHTTP.SG("root_dir"))) {
                        dir = dir.substring(this.thisSessionHTTP.SG("root_dir").length() - 1);
                    }
                    href.setText(String.valueOf(crushftp.handlers.Common.url_encode(dir, "/")) + crushftp.handlers.Common.url_encode(item.getProperty("name", "")));
                }
                if (!href.getText().endsWith("/") && item.getProperty("type").toUpperCase().equals("DIR")) {
                    href.setText(String.valueOf(href.getText()) + "/");
                }
                if (hrefs.indexOf(href.getText()) < 0) {
                    hrefs.addElement(href.getText());
                    response = new Element("response", D);
                    response.addNamespaceDeclaration(lp1);
                    response.addNamespaceDeclaration(lp2);
                    response.addNamespaceDeclaration(g0);
                    root.addContent((Content)response);
                    response.addContent((Content)href);
                    propstatGood = new Element("propstat", D);
                    propstatBad = new Element("propstat", D);
                    propGood = new Element("prop", D);
                    propBad = new Element("prop", D);
                    propstatGood.addContent((Content)propGood);
                    propstatBad.addContent((Content)propBad);
                    error404 = new Element("status", D).setText("HTTP/1.1 404 Not Found");
                    keylist = commandActions.keys();
                    useGood = false;
                    useBad = false;
                    vrl = new VRL(item.getProperty("url"));
                    if (listProps) {
                        full_path = vrl.getPath();
                        if (vrl.getProtocol().equalsIgnoreCase("file")) {
                            full_path = vrl.getCanonicalPath();
                        }
                        commandActions2 = (Properties)ServerSessionHTTP.proppatches.get(full_path);
                        c = null;
                        try {
                            c = this.thisSessionHTTP.thisSession.uVFS.getClient(item);
                            if (c.stat(vrl.getPath()) == null) ** GOTO lbl265
                            xx = 0;
                            while (xx < fieldOrder.size()) {
                                key = fieldOrder.elementAt(xx).toString();
                                if (commandActions2.containsKey(key)) {
                                    useGood = true;
                                    elementKey = new Element(key, "http://www.apple.com/SyncServices");
                                    propGood.addContent((Content)elementKey);
                                }
                                ++xx;
                            }
                        }
                        finally {
                            c = this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                        }
                    } else {
                        xx = 0;
                        while (xx < fieldOrder.size()) {
                            key = fieldOrder.elementAt(xx).toString();
                            if (key.equalsIgnoreCase("getlastmodified")) {
                                useGood = true;
                                getlastmodified = new Element(key, lp1);
                                propGood.addContent((Content)getlastmodified);
                                getlastmodified.setText(this.sdf_rfc1123.format(new Date(Long.parseLong(item.getProperty("modified", "0")))));
                            } else if (key.equalsIgnoreCase("modificationdate")) {
                                useGood = true;
                                getlastmodified = new Element(key, D);
                                propGood.addContent((Content)getlastmodified);
                                getlastmodified.setText(this.sdf_rfc1123_2.format(new Date(Long.parseLong(item.getProperty("modified", "0")))));
                            } else if (key.equalsIgnoreCase("resourcetype")) {
                                useGood = true;
                                resourcetype = new Element("resourcetype", lp1);
                                if (item.getProperty("type").toUpperCase().equals("DIR")) {
                                    resourcetype.addContent((Content)new Element("collection", D));
                                }
                                propGood.addContent((Content)resourcetype);
                            } else if (key.equalsIgnoreCase("getcontenttype")) {
                                useGood = true;
                                ext = item.getProperty("name");
                                if (ext.indexOf(".") >= 0) {
                                    ext = ext.substring(ext.indexOf(".")).toUpperCase();
                                }
                                if (Common.mimes.getProperty(ext, "").equals("")) {
                                    ext = "*";
                                }
                                contentType = Common.mimes.getProperty(ext, "");
                                if (item.getProperty("type").toUpperCase().equals("DIR")) {
                                    contentType = "httpd/unix-directory";
                                }
                                getcontenttype = new Element("getcontenttype", D);
                                getcontenttype.setText(contentType);
                                propGood.addContent((Content)getcontenttype);
                            } else if (key.equalsIgnoreCase("iscollection")) {
                                useGood = true;
                                iscollection = new Element("iscollection", D);
                                iscollection.setText("" + item.getProperty("type").toUpperCase().equals("DIR"));
                                propGood.addContent((Content)iscollection);
                            } else if (key.equalsIgnoreCase("getcontentlength")) {
                                if (item.getProperty("type").toUpperCase().equals("DIR")) {
                                    useBad = true;
                                    propBad.addContent((Content)new Element(key, g0));
                                    if (!propstatBad.isAncestor(error404)) {
                                        propstatBad.addContent((Content)error404);
                                    }
                                } else {
                                    propGood.addContent((Content)new Element(key, lp1).setText(item.getProperty("size", "0")));
                                    useGood = true;
                                }
                            } else if (key.equalsIgnoreCase("quota")) {
                                if (!item.getProperty("type").toUpperCase().equals("DIR") || total_quota == -12345L) {
                                    useBad = true;
                                    propBad.addContent((Content)new Element(key, D));
                                    if (!propstatBad.isAncestor(error404)) {
                                        propstatBad.addContent((Content)error404);
                                    }
                                } else {
                                    propGood.addContent((Content)new Element(key, D).setText(String.valueOf(total_quota * 2L / 1024L)));
                                    useGood = true;
                                }
                            } else if (key.equalsIgnoreCase("quotaused")) {
                                if (!item.getProperty("type").toUpperCase().equals("DIR")) {
                                    useBad = true;
                                    propBad.addContent((Content)new Element(key, D));
                                    if (!propstatBad.isAncestor(error404)) {
                                        propstatBad.addContent((Content)error404);
                                    }
                                } else {
                                    propGood.addContent((Content)new Element(key, D).setText(String.valueOf(quota * 2L / 1024L)));
                                    useGood = true;
                                }
                            } else if (key.equalsIgnoreCase("dotunderscore")) {
                                propGood.addContent((Content)new Element(key, "A", "http://www.apple.com/webdav_fs/props/").setText(""));
                                useGood = true;
                            } else if (key.equalsIgnoreCase("dotunderscore-size")) {
                                propGood.addContent((Content)new Element(key, "A", "http://www.apple.com/webdav_fs/props/").setText("0"));
                                useGood = true;
                            } else {
                                useBad = true;
                                empty = new Element(key, D);
                                propBad.addContent((Content)empty);
                                if (!propstatBad.isAncestor(error404)) {
                                    propstatBad.addContent((Content)error404);
                                }
                            }
                            ++xx;
                        }
                        if (listAdditionalProps && vrl.getProtocol().equalsIgnoreCase("file") && (commandActions2 = (Properties)ServerSessionHTTP.proppatches.get(vrl.getCanonicalPath())) != null) {
                            additionalKeyList = commandActions2.keys();
                            while (additionalKeyList.hasMoreElements()) {
                                key = additionalKeyList.nextElement().toString();
                                val = commandActions2.getProperty(key);
                                useGood = true;
                                elementKey = new Element(key, "X", "http://www.apple.com/SyncServices");
                                propGood.addContent((Content)elementKey);
                                elementKey.setText(val);
                            }
                        }
                    }
lbl265:
                    // 5 sources

                    if (useGood) {
                        propstatGood.addContent((Content)new Element("status", D).setText("HTTP/1.1 200 OK"));
                    }
                    if (useGood) {
                        response.addContent((Content)propstatGood);
                    }
                    if (useBad && !listAdditionalProps) {
                        response.addContent((Content)propstatBad);
                    }
                }
            }
            ++x;
        }
        if (added == 0) {
            href = new Element("href", D);
            dir = this.thisSessionHTTP.pwd();
            if (!this.thisSessionHTTP.thisSession.SG("root_dir").equals("/")) {
                dir = dir.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
            }
            href.setText(crushftp.handlers.Common.url_encode(dir, "/"));
            if (!href.getText().endsWith("/") && item.getProperty("type").toUpperCase().equals("DIR")) {
                href.setText(String.valueOf(href.getText()) + "/");
            }
            response = new Element("response", D);
            response.addNamespaceDeclaration(lp1);
            response.addNamespaceDeclaration(lp2);
            response.addNamespaceDeclaration(g0);
            root.addContent((Content)response);
            response.addContent((Content)href);
            propstatGood = new Element("propstat", D);
            propstatBad = new Element("propstat", D);
            propGood = new Element("prop", D);
            propBad = new Element("prop", D);
            propstatGood.addContent((Content)propGood);
            propstatBad.addContent((Content)propBad);
            useGood = false;
            useGood = true;
            getlastmodified = new Element("getlastmodified", lp1);
            propGood.addContent((Content)getlastmodified);
            getlastmodified.setText(this.sdf_rfc1123.format(new Date(Long.parseLong(item.getProperty("modified", "0")))));
            resourcetype = new Element("resourcetype", lp1);
            resourcetype.addContent((Content)new Element("collection", D));
            propGood.addContent((Content)resourcetype);
            ext = item.getProperty("name");
            if (ext.indexOf(".") >= 0) {
                ext = ext.substring(ext.indexOf(".")).toUpperCase();
            }
            if (Common.mimes.getProperty(ext, "").equals("")) {
                ext = "*";
            }
            contentType = "httpd/unix-directory";
            getcontenttype = new Element("getcontenttype", D);
            getcontenttype.setText(contentType);
            propGood.addContent((Content)getcontenttype);
            iscollection = new Element("iscollection", D);
            iscollection.setText("" + item.getProperty("type").toUpperCase().equals("DIR"));
            propGood.addContent((Content)iscollection);
            if (useGood) {
                propstatGood.addContent((Content)new Element("status", D).setText("HTTP/1.1 200 OK"));
            }
            if (useGood) {
                response.addContent((Content)propstatGood);
            }
        }
        if (this.thisSessionHTTP.xmlOut == null) {
            this.thisSessionHTTP.xmlOut = new XMLOutputter();
            f = Format.getPrettyFormat();
            f.setExpandEmptyElements(false);
            this.thisSessionHTTP.xmlOut.setFormat(f);
        }
        return this.thisSessionHTTP.xmlOut.outputString(doc);
    }

    public void propfind(long http_len_max, String initial_current_dir, String depth) throws Exception {
        Properties item;
        this.thisSessionHTTP.thisSession.add_log("WebDAV PROPFIND:" + initial_current_dir, "LIST");
        String xml = "";
        if (http_len_max > 0L) {
            xml = this.get_raw_http_command((int)http_len_max);
        } else {
            int timeout = this.sock.getSoTimeout();
            this.sock.setSoTimeout(3000);
            try {
                int empty_count = 0;
                while (http_len_max == -1L && empty_count < 100) {
                    String s = this.get_raw_http_command((int)http_len_max);
                    xml = String.valueOf(xml) + s;
                    if (!s.equals("")) continue;
                    ++empty_count;
                }
            }
            catch (SocketTimeoutException empty_count) {
                // empty catch block
            }
            this.sock.setSoTimeout(timeout);
        }
        this.thisSessionHTTP.thisSession.uiVG("user_log").addElement(xml);
        this.thisSessionHTTP.thisSession.drain_log();
        Properties commandActions = new Properties();
        Vector<String> commandActionsOrder = new Vector<String>();
        if (!xml.trim().equals("")) {
            try {
                Document doc = this.sax.build((Reader)new StringReader(xml));
                List items = doc.getRootElement().getChildren();
                Iterator i = items.iterator();
                if (i.hasNext()) {
                    Element element = (Element)i.next();
                    List items2 = element.getChildren();
                    for (Element element2 : items2) {
                        String key = element2.getName();
                        String val = element2.getText();
                        commandActions.put(key, val);
                        commandActionsOrder.addElement(key);
                    }
                }
            }
            catch (Throwable e) {
                Log.log("DAV_SERVER", 1, e);
            }
        }
        xml = this.doPropFind(commandActions, depth, false, commandActionsOrder);
        if (this.thisSessionHTTP.SG("username").equals("anonymous") && ((item = this.thisSessionHTTP.thisSession.uVFS.get_item(initial_current_dir)) == null || item.getProperty("privs").indexOf("(view)") < 0)) {
            this.thisSessionHTTP.DEAUTH();
            return;
        }
        if (xml != null) {
            Properties item2;
            this.write_command_http("HTTP/1.1 207 Multi-Status");
            this.write_standard_headers();
            this.write_command_http("Content-Length: " + (xml.getBytes("UTF8").length + 2));
            this.write_command_http("Content-Type: text/xml; charset=\"utf-8\"");
            String dir = initial_current_dir;
            if (dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                dir = initial_current_dir.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
            }
            if ((item2 = this.thisSessionHTTP.thisSession.uVFS.get_item(initial_current_dir)) != null && item2.getProperty("type", "").equalsIgnoreCase("DIR") && !dir.endsWith("/")) {
                this.write_command_http("Content-Location: " + crushftp.handlers.Common.url_encode(dir, "/") + "/");
            }
            this.write_command_http("");
            this.write_command_http(xml);
        } else {
            String msg = "Not Found: Resource does not exist";
            this.write_command_http("HTTP/1.1 404 Not Found: Resource does not exist");
            this.write_standard_headers();
            this.write_command_http("Content-Type: text/xml; charset=utf-8");
            this.write_command_http("Content-Length: " + msg.length());
            this.write_command_http("");
            this.write_command_raw(msg);
        }
    }

    public void proppatch(long http_len_max, String initial_current_dir, String depth) throws Exception {
        this.thisSessionHTTP.thisSession.add_log("WebDAV PROPATCH:" + initial_current_dir, "MDTM");
        String xml = this.get_raw_http_command((int)http_len_max);
        this.thisSessionHTTP.thisSession.uiVG("user_log").addElement(xml);
        this.thisSessionHTTP.thisSession.drain_log();
        Properties commandActions = new Properties();
        Vector<String> commandActionsOrder = new Vector<String>();
        try {
            Document doc = this.sax.build((Reader)new StringReader(xml));
            List items = doc.getRootElement().getChildren();
            for (Element element : items) {
                List items2 = element.getChildren();
                for (Element element2 : items2) {
                    List items3 = element2.getChildren();
                    for (Element element4 : items3) {
                        String key = element4.getName();
                        String val = element4.getText();
                        commandActions.put(key, val);
                        commandActionsOrder.addElement(key);
                    }
                }
            }
        }
        catch (Exception e) {
            Log.log("DAV_SERVER", 1, e);
        }
        Properties item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.pwd());
        GenericClient c = this.thisSessionHTTP.thisSession.uVFS.getClient(item);
        try {
            VRL vrl = new VRL(item.getProperty("url"));
            String full_path = vrl.getPath();
            if (vrl.getProtocol().equalsIgnoreCase("file")) {
                full_path = vrl.getCanonicalPath();
            }
            Properties commandActions2 = (Properties)ServerSessionHTTP.proppatches.get(full_path);
            if (c.stat(vrl.getPath()) == null) {
                commandActions2 = null;
            }
            if (commandActions2 == null) {
                commandActions2 = new Properties();
            }
            commandActions2.putAll((Map<?, ?>)commandActions);
            ServerSessionHTTP.proppatches.put(full_path, commandActions2);
        }
        finally {
            c = this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
        }
        this.thisSessionHTTP.savePropPatches();
        xml = this.doPropFind(commandActions, depth, true, commandActionsOrder);
        if (xml != null) {
            this.write_command_http("HTTP/1.1 207 Multi-Status");
            String dir = initial_current_dir;
            if (dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                dir = initial_current_dir.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
            }
            if ((item = this.thisSessionHTTP.thisSession.uVFS.get_item(initial_current_dir)) != null && item.getProperty("type", "").equalsIgnoreCase("DIR") && !dir.endsWith("/")) {
                this.write_command_http("Content-Location: " + crushftp.handlers.Common.url_encode(dir, "/") + "/");
            }
            this.write_standard_headers();
            this.write_command_http("Content-Length: " + (xml.getBytes("UTF8").length + 2));
            this.write_command_http("Content-Type: text/xml;charset=utf-8");
            this.write_command_http("");
            this.write_command_http(xml);
            this.thisSessionHTTP.thisSession.uiVG("user_log").addElement(xml);
            this.thisSessionHTTP.thisSession.drain_log();
        } else {
            String msg = "Not Found: Resource does not exist";
            this.write_command_http("HTTP/1.1 404 Not Found: Resource does not exist");
            this.write_standard_headers();
            this.write_command_http("Content-Type: text/xml;charset=utf-8");
            this.write_command_http("Content-Length: " + msg.length());
            this.write_command_http("");
            this.write_command_raw(msg);
        }
    }

    public String delete(String initial_current_dir, String error_message) throws Exception {
        String dir = initial_current_dir;
        this.thisSessionHTTP.thisSession.add_log("WebDAV DELETE:" + initial_current_dir, "DELE");
        if (dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
            dir = initial_current_dir.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
        }
        Properties item = this.thisSessionHTTP.thisSession.uVFS.get_item(initial_current_dir);
        Enumeration<Object> keys = ServerSessionHTTP.proppatches.keys();
        try {
            VRL vrl = new VRL(item.getProperty("url"));
            String full_path = vrl.getPath();
            if (vrl.getProtocol().equalsIgnoreCase("file")) {
                full_path = vrl.getCanonicalPath();
            }
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                if (!key.startsWith(full_path)) continue;
                ServerSessionHTTP.proppatches.remove(key);
            }
            this.thisSessionHTTP.savePropPatches();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.thisSessionHTTP.thisSession.uiPUT("the_command", "DELE");
        this.thisSessionHTTP.thisSession.uiPUT("the_command_data", this.thisSessionHTTP.pwd());
        error_message = String.valueOf(error_message) + this.thisSessionHTTP.thisSession.do_DELE(true, this.thisSessionHTTP.pwd());
        this.thisSessionHTTP.thisSession.uVFS.reset();
        if (error_message.length() == 0) {
            this.write_command_http("HTTP/1.1 204  No Content");
        } else if (item == null) {
            this.write_command_http("HTTP/1.1 404 Not found.");
        } else {
            this.write_command_http("HTTP/1.1 401 Access Denied.");
        }
        if (item != null && item.getProperty("type", "").equalsIgnoreCase("DIR") && !dir.endsWith("/")) {
            this.write_command_http("Content-Location: " + crushftp.handlers.Common.url_encode(dir, "/") + "/");
        }
        this.write_standard_headers();
        this.write_command_http("Content-Length: 0");
        this.write_command_http("");
        return error_message;
    }

    public void copy(String initial_current_dir, String move_destination, String overwrite) throws Exception {
        this.thisSessionHTTP.thisSession.uVFS.reset();
        this.thisSessionHTTP.thisSession.uiPUT("the_command_data", this.thisSessionHTTP.pwd());
        String from = this.thisSessionHTTP.pwd();
        this.thisSessionHTTP.thisSession.add_log("WebDAV COPY:" + initial_current_dir, "RETR");
        boolean ok = true;
        if (this.thisSessionHTTP.thisSession.check_access_privs(this.thisSessionHTTP.pwd(), "RETR")) {
            this.thisSessionHTTP.cd(new VRL(move_destination).getPath());
            if (!this.thisSessionHTTP.pwd().toUpperCase().startsWith(this.thisSessionHTTP.thisSession.SG("root_dir").toUpperCase())) {
                this.thisSessionHTTP.cd(String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + (this.thisSessionHTTP.pwd().startsWith("/") ? this.thisSessionHTTP.pwd().substring(1) : this.thisSessionHTTP.pwd()));
            }
            if (this.thisSessionHTTP.pwd().indexOf("\\") >= 0) {
                this.thisSessionHTTP.cd(this.thisSessionHTTP.pwd().replace('\\', '/'));
            }
            if (!this.thisSessionHTTP.pwd().startsWith("/")) {
                this.thisSessionHTTP.cd("/" + this.thisSessionHTTP.pwd());
            }
            this.thisSessionHTTP.thisSession.uiPUT("the_command_data", this.thisSessionHTTP.pwd());
        } else {
            ok = false;
        }
        Properties destItem = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.pwd());
        if (overwrite.equalsIgnoreCase("F") && destItem != null) {
            ok = false;
        }
        int responseCode = 201;
        boolean storOK = false;
        this.thisSessionHTTP.thisSession.add_log("WebDAV COPY:" + move_destination, "STOR");
        if (ok) {
            storOK = this.thisSessionHTTP.thisSession.check_access_privs(this.thisSessionHTTP.pwd(), "STOR");
        }
        if (ok && storOK) {
            String to = this.thisSessionHTTP.pwd();
            Properties source = this.thisSessionHTTP.thisSession.uVFS.get_item(from);
            Properties dest = this.thisSessionHTTP.thisSession.uVFS.get_item_parent(to);
            try {
                VRL vrl1 = new VRL(source.getProperty("url"));
                Properties stat2 = null;
                VRL vrl2 = new VRL(dest.getProperty("url"));
                GenericClient c2 = this.thisSessionHTTP.thisSession.uVFS.getClient(dest);
                try {
                    stat2 = c2.stat(vrl2.getPath());
                }
                finally {
                    c2 = this.thisSessionHTTP.thisSession.uVFS.releaseClient(c2);
                }
                if (stat2 != null) {
                    responseCode = 204;
                }
                if (stat2 != null && stat2.getProperty("type").equalsIgnoreCase("DIR") && overwrite.equalsIgnoreCase("T")) {
                    if (this.thisSessionHTTP.thisSession.check_access_privs(this.thisSessionHTTP.pwd(), "RMD") && this.thisSessionHTTP.thisSession.check_access_privs(this.thisSessionHTTP.pwd(), "DELE")) {
                        crushftp.handlers.Common.recurseDelete_U(vrl2.getCanonicalPath(), false);
                    } else {
                        ok = false;
                    }
                }
                if (ok) {
                    crushftp.handlers.Common.recurseCopy_U(vrl1.getCanonicalPath(), vrl2.getCanonicalPath(), overwrite.equalsIgnoreCase("T"));
                }
            }
            catch (Exception e) {
                ok = false;
                Log.log("DAV_SERVER", 0, e);
            }
            this.thisSessionHTTP.thisSession.uVFS.reset();
        }
        if (ok && storOK) {
            this.write_command_http("HTTP/1.1 " + responseCode + " No Content");
        } else if (overwrite.equalsIgnoreCase("F") && destItem != null) {
            this.write_command_http("HTTP/1.1 412 Access Denied.");
        } else if (ok && !storOK) {
            this.write_command_http("HTTP/1.1 409 does not exist");
        } else {
            this.write_command_http("HTTP/1.1 403 Access Denied.");
        }
        this.write_standard_headers();
        this.write_command_http("Content-Length: 0");
        this.write_command_http("");
    }

    public String mkcol(long http_len_max, String initial_current_dir, String error_message) throws Exception {
        this.thisSessionHTTP.thisSession.uiPUT("the_command", "MKDIR");
        String the_dir = this.thisSessionHTTP.pwd();
        if (!the_dir.endsWith("/")) {
            the_dir = String.valueOf(the_dir) + "/";
        }
        this.thisSessionHTTP.thisSession.uiPUT("the_command_data", the_dir);
        this.thisSessionHTTP.thisSession.add_log("WebDAV MKCOL:" + initial_current_dir, "MKDIR");
        error_message = http_len_max <= 0L ? String.valueOf(error_message) + this.thisSessionHTTP.thisSession.do_MKD(false, the_dir) : "Body not allowed.";
        this.thisSessionHTTP.thisSession.uVFS.reset();
        boolean good = false;
        Properties item = this.thisSessionHTTP.thisSession.uVFS.get_item(String.valueOf(this.thisSessionHTTP.pwd()) + "/");
        if (error_message.length() == 0) {
            good = true;
            this.thisSessionHTTP.thisSession.accessExceptions.put(String.valueOf(this.thisSessionHTTP.pwd()) + "/", item);
            this.write_command_http("HTTP/1.1 201 Created");
            int x = 0;
            while (x < this.thisSessionHTTP.headers.size()) {
                String data = this.thisSessionHTTP.headers.elementAt(x).toString();
                if (data.startsWith("Last-Modified: ")) {
                    String modified = data.substring(data.indexOf(":") + 1).trim();
                    this.thisSessionHTTP.thisSession.uiPUT("the_command", "MDTM");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                    this.thisSessionHTTP.thisSession.uiPUT("the_command_data", String.valueOf(this.thisSessionHTTP.pwd()) + " " + sdf.format(this.sdf_rfc1123.parse(modified)));
                    error_message = String.valueOf(error_message) + this.thisSessionHTTP.thisSession.do_MDTM();
                }
                ++x;
            }
            this.thisSessionHTTP.thisSession.uVFS.reset();
        } else if (error_message.indexOf(LOC.G("exists")) >= 0 && item.getProperty("type").equalsIgnoreCase("DIR")) {
            this.write_command_http("HTTP/1.1 405 Already exists.");
        } else if (http_len_max != 0L) {
            this.write_command_http("HTTP/1.1 415 Body not allowed.");
        } else {
            this.write_command_http("HTTP/1.1 409 Access Denied.");
        }
        this.write_standard_headers();
        if (good) {
            this.write_command_http("Content-Location: " + crushftp.handlers.Common.url_encode(the_dir, "/"));
        }
        this.write_command_http("Content-Length: 18");
        this.write_command_http("");
        this.write_command_http("Resource created");
        return error_message;
    }

    public void lock(long http_len_max, String initial_current_dir, String depth) throws Exception {
        String xml = "";
        if (http_len_max >= 0L) {
            xml = this.get_raw_http_command((int)http_len_max);
        }
        this.thisSessionHTTP.thisSession.uiVG("user_log").addElement(xml);
        this.thisSessionHTTP.thisSession.drain_log();
        this.thisSessionHTTP.thisSession.add_log("WebDAV LOCK:" + initial_current_dir, "CWD");
        Properties item = this.thisSessionHTTP.thisSession.uVFS.get_item(initial_current_dir);
        Element root = new Element("prop", "D", "DAV:");
        Document doc = new Document(root);
        Element lockdiscovery = new Element("lockdiscovery", "D", "DAV:");
        root.addContent((Content)lockdiscovery);
        Element activelock = new Element("activelock", "D", "DAV:");
        lockdiscovery.addContent((Content)activelock);
        Element locktype = new Element("locktype", "D", "DAV:");
        activelock.addContent((Content)locktype);
        Element write = new Element("write", "D", "DAV:");
        locktype.addContent((Content)write);
        Element lockscope = new Element("lockscope", "D", "DAV:");
        activelock.addContent((Content)lockscope);
        Element exclusive = new Element("exclusive", "D", "DAV:");
        lockscope.addContent((Content)exclusive);
        Element depthElement = new Element("depth", "D", "DAV:");
        activelock.addContent((Content)depthElement);
        depthElement.setText(depth);
        Element owner = new Element("owner", "D", "DAV:");
        activelock.addContent((Content)owner);
        Element href = new Element("href", "D", "DAV:");
        owner.addContent((Content)href);
        Element synclockinfo = new Element("synclockinfo", "SY", "http://www.apple.com/SyncServices");
        href.addContent((Content)synclockinfo);
        Element lock_user = new Element("lock-user", "SY", "SY:");
        synclockinfo.addContent((Content)lock_user);
        lock_user.setText(this.thisSessionHTTP.SG("username"));
        if (xml.indexOf("clientid>") >= 0) {
            Element clientid = new Element("clientid", "SY", "SY:");
            synclockinfo.addContent((Content)clientid);
            clientid.setText(xml.substring(xml.indexOf("clientid>") + 9, xml.indexOf("<", xml.indexOf("clientid>"))));
        }
        if (xml.indexOf("clientname>") >= 0) {
            Element clientname = new Element("clientname", "SY", "SY:");
            synclockinfo.addContent((Content)clientname);
            clientname.setText(xml.substring(xml.indexOf("clientname>") + 11, xml.indexOf("<", xml.indexOf("clientname>"))));
        }
        Element acquiredate = new Element("acquiredate", "SY", "SY:");
        synclockinfo.addContent((Content)acquiredate);
        SimpleDateFormat lockSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US);
        acquiredate.setText(lockSDF.format(new Date()));
        Element timeout = new Element("timeout", "D", "DAV:");
        activelock.addContent((Content)timeout);
        timeout.setText("Second-239");
        Properties lock = new Properties();
        lock.put("token", crushftp.handlers.Common.makeBoundary().toLowerCase());
        lock.put("stamp", String.valueOf(new Date().getTime()));
        lock.put("duration", "600");
        String urlLock = initial_current_dir;
        if (item != null) {
            urlLock = item.getProperty("url");
        }
        try {
            lock.put("resource", urlLock.toUpperCase());
        }
        catch (Exception exception) {
            // empty catch block
        }
        ServerSessionHTTP.locktokens.put(lock.getProperty("token"), lock);
        Element locktoken = new Element("locktoken", "D", "DAV:");
        activelock.addContent((Content)locktoken);
        Element href2 = new Element("href", "D", "DAV:");
        locktoken.addContent((Content)href2);
        href2.setText("locktoken:" + lock.getProperty("token"));
        if (this.xmlOut == null) {
            this.xmlOut = new XMLOutputter();
            Format f = Format.getPrettyFormat();
            f.setExpandEmptyElements(false);
            this.xmlOut.setFormat(f);
        }
        xml = this.xmlOut.outputString(doc);
        this.write_command_http("HTTP/1.1 200 OK");
        this.write_standard_headers();
        this.write_command_http("Content-Length: " + (xml.getBytes("UTF8").length + 2));
        this.write_command_http("Content-Type: text/xml;charset=utf-8");
        this.write_command_http("Lock-Token: <locktoken:" + lock.getProperty("token") + ">");
        String dir = initial_current_dir;
        if (dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
            dir = initial_current_dir.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
        }
        if ((item = this.thisSessionHTTP.thisSession.uVFS.get_item(initial_current_dir)) != null && item.getProperty("type", "").equalsIgnoreCase("DIR") && !dir.endsWith("/")) {
            this.write_command_http("Content-Location: " + crushftp.handlers.Common.url_encode(dir, "/") + "/");
        }
        this.write_command_http("");
        this.write_command_raw(String.valueOf(xml) + "\r\n");
        this.thisSessionHTTP.thisSession.uiVG("user_log").addElement(xml);
        this.thisSessionHTTP.thisSession.drain_log();
    }

    public void unlock(String initial_current_dir) throws Exception {
        Properties item = this.thisSessionHTTP.thisSession.uVFS.get_item(initial_current_dir);
        this.thisSessionHTTP.thisSession.add_log("WebDAV UNLOCK:" + initial_current_dir, "CWD");
        this.write_command_http("HTTP/1.1 204 No Content");
        this.write_standard_headers();
        this.write_command_http("Content-Length: 0");
        this.write_command_http("");
        Properties lock = new Properties();
        lock.put("token", String.valueOf(new Date().getTime()));
        lock.put("stamp", String.valueOf(new Date().getTime()));
        lock.put("duration", "600");
        String urlLock = initial_current_dir;
        if (item != null) {
            urlLock = item.getProperty("url");
        }
        lock.put("resource", urlLock.toUpperCase());
        ServerSessionHTTP.locktokens.put(lock.getProperty("token"), lock);
    }

    public String move(String move_destination, String error_message, String overwrite) throws Exception {
        this.thisSessionHTTP.thisSession.uVFS.reset();
        String rename_from = this.thisSessionHTTP.pwd();
        this.thisSessionHTTP.thisSession.uiPUT("the_command_data", rename_from);
        error_message = String.valueOf(error_message) + this.thisSessionHTTP.thisSession.do_RNFR(rename_from);
        this.thisSessionHTTP.thisSession.add_log("WebDAV Move:" + rename_from, "RNFR");
        this.thisSessionHTTP.cd(move_destination);
        try {
            this.thisSessionHTTP.cd(new VRL(move_destination).getPath());
        }
        catch (Exception e) {
            Log.log("DAV_SERVER", 2, e);
        }
        if (!this.thisSessionHTTP.pwd().toUpperCase().startsWith(this.thisSessionHTTP.thisSession.SG("root_dir").toUpperCase())) {
            this.thisSessionHTTP.cd(String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + (this.thisSessionHTTP.pwd().startsWith("/") ? this.thisSessionHTTP.pwd().substring(1) : this.thisSessionHTTP.pwd()));
        }
        if (this.thisSessionHTTP.pwd().indexOf("\\") >= 0) {
            this.thisSessionHTTP.cd(this.thisSessionHTTP.pwd().replace('\\', '/'));
        }
        if (!this.thisSessionHTTP.pwd().startsWith("/")) {
            this.thisSessionHTTP.cd("/" + this.thisSessionHTTP.pwd());
        }
        this.thisSessionHTTP.thisSession.uiPUT("the_command_data", this.thisSessionHTTP.pwd());
        Properties destItem = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.pwd());
        Properties destItemParent = this.thisSessionHTTP.thisSession.uVFS.get_item(crushftp.handlers.Common.all_but_last(this.thisSessionHTTP.pwd()));
        if (overwrite.equalsIgnoreCase("F") && destItem != null) {
            error_message = "item exists.";
        }
        this.thisSessionHTTP.thisSession.add_log("WebDAV Move:" + this.thisSessionHTTP.pwd(), "RNTO");
        if (error_message.length() == 0 && destItemParent != null) {
            error_message = String.valueOf(error_message) + this.thisSessionHTTP.thisSession.do_RNTO(overwrite.equals("true"), rename_from, move_destination);
        }
        this.thisSessionHTTP.thisSession.uVFS.reset();
        int responseCode = 201;
        if (destItem != null) {
            responseCode = 204;
        }
        if (destItemParent == null) {
            this.write_command_http("HTTP/1.1 404 Parent Not Found");
        } else if (ServerStatus.BG("omnipresence_hack") && error_message.length() == 0) {
            this.write_command_http("HTTP/1.1 207 Multi-Status");
        } else if (error_message.length() == 0) {
            this.write_command_http("HTTP/1.1 201 Moved");
        } else if (error_message.indexOf("RNFR") >= 0) {
            this.write_command_http("HTTP/1.1 404 Not Found");
        } else if (error_message.indexOf("item exists") >= 0) {
            this.write_command_http("HTTP/1.1 412 item exists");
        } else {
            this.write_command_http("HTTP/1.1 403 Access Denied.");
        }
        this.write_standard_headers();
        String xml = "";
        this.write_command_http("Content-Length: 0");
        this.write_command_http("");
        this.thisSessionHTTP.thisSession.uiVG("user_log").addElement(xml);
        this.thisSessionHTTP.thisSession.drain_log();
        return error_message;
    }

    public void acl(long http_len_max, String initial_current_dir) throws Exception {
        String xml = this.get_raw_http_command((int)http_len_max);
        this.thisSessionHTTP.thisSession.add_log("WebDAV ACL:" + initial_current_dir, "CWD");
        String dir = initial_current_dir;
        if (dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
            dir = initial_current_dir.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
        }
        Properties item = this.thisSessionHTTP.thisSession.uVFS.get_item(initial_current_dir);
        this.write_command_http("HTTP/1.1 200 OK");
        this.write_standard_headers();
        this.write_command_http("Content-Length: 0");
        this.write_command_http("");
    }

    public void write_command_http(String s) throws Exception {
        this.thisSessionHTTP.write_command_http(s);
    }

    public void write_command_raw(String s) throws Exception {
        this.thisSessionHTTP.write_command_raw(s);
    }

    public void write_standard_headers() throws Exception {
        this.thisSessionHTTP.write_standard_headers();
    }

    public String get_raw_http_command(int len) throws Exception {
        return this.thisSessionHTTP.get_raw_http_command(len);
    }
}

