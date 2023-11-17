/*
 * Decompiled with CFR 0.152.
 */
package crushftp.reports8;

import com.crushftp.client.Common;
import com.crushftp.client.File_U;
import com.crushftp.client.Worker;
import crushftp.handlers.Log;
import crushftp.handlers.UserTools;
import crushftp.reports8.ReportTools;
import crushftp.server.ServerStatus;
import crushftp.server.VFS;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class NewFiles {
    Properties server_info = null;
    Properties server_settings = null;
    static Properties cachedDirListings = new Properties();

    public void init(Properties server_settings, Properties server_info) {
        this.server_settings = server_settings;
        this.server_info = server_info;
    }

    public void generate(Properties stats, Properties params, StringBuffer sb, Properties status) {
        try {
            if (cachedDirListings == null || cachedDirListings.size() == 0) {
                try {
                    cachedDirListings = (Properties)crushftp.handlers.Common.readXMLObject(String.valueOf(System.getProperty("crushftp.backup")) + "backup/NewFileReportCache.XML");
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (cachedDirListings == null) {
                    cachedDirListings = new Properties();
                }
            }
            crushftp.handlers.Common.setupReportDates(params, params.getProperty("show", ""), params.getProperty("startDate"), params.getProperty("endDate"));
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.US);
            SimpleDateFormat mmddyyyy = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
            Vector usernames = (Vector)params.get("usernames");
            ReportTools.fixListUsernames(usernames);
            sorter cd1 = new sorter();
            Properties userDetails = new Properties();
            cd1.setObj(userDetails, "username");
            Vector servers = (Vector)this.server_settings.get("server_list");
            Vector<String> compeltedServers = new Vector<String>();
            int loop = 0;
            while (loop < servers.size()) {
                Properties server_item = (Properties)servers.elementAt(loop);
                String server = server_item.getProperty("linkedServer", "").equals("") ? String.valueOf(server_item.getProperty("ip", "lookup")) + "_" + server_item.getProperty("port", "21") : server_item.getProperty("linkedServer", "");
                Log.log("REPORT", 2, "NewFiles server:" + server);
                if (compeltedServers.indexOf(server) < 0) {
                    compeltedServers.addElement(server);
                    Vector current_user_group_listing = new Vector();
                    UserTools.refreshUserList(server, current_user_group_listing);
                    int pos = 0;
                    int x = 0;
                    while (x < current_user_group_listing.size()) {
                        ++pos;
                        try {
                            status.put("status", String.valueOf((int)((float)pos / (float)current_user_group_listing.size() * 100.0f)) + "%");
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        String username = current_user_group_listing.elementAt(x).toString();
                        Log.log("REPORT", 2, "NewFiles username:" + username);
                        boolean user_ok = false;
                        int xx = 0;
                        while (xx < usernames.size()) {
                            if (Common.do_search(usernames.elementAt(xx).toString().toUpperCase(), username.toUpperCase(), false, 0)) {
                                user_ok = true;
                            }
                            ++xx;
                        }
                        if (user_ok || usernames.size() <= 0) {
                            VFS uVFS = UserTools.ut.getVFS(server, username);
                            Properties user = UserTools.ut.getUser(server, username, true);
                            Properties listerStatus = new Properties();
                            listerStatus.put("status", "running");
                            Vector v = new Vector();
                            class Lister
                            implements Runnable {
                                Vector v;
                                Properties params;
                                VFS uVFS;
                                Properties listerStatus;

                                public Lister(Vector v, Properties params, VFS uVFS, Properties listerStatus) {
                                    this.v = v;
                                    this.params = params;
                                    this.uVFS = uVFS;
                                    this.listerStatus = listerStatus;
                                }

                                @Override
                                public void run() {
                                    try {
                                        Vector<Properties> filters = new Vector<Properties>();
                                        String path = "/";
                                        if (this.params.getProperty("searchPath", "").equals("starts with") || this.params.getProperty("searchPath", "").equals("equals")) {
                                            filters.addElement(this.params);
                                            path = this.params.getProperty("path", "");
                                            if (this.params.getProperty("path", "").equals("")) {
                                                path = "/";
                                            }
                                        }
                                        this.uVFS.getListing(this.v, path, Integer.parseInt(this.params.getProperty("depth", "10")), 1000, false, filters);
                                    }
                                    catch (Exception e) {
                                        Log.log("REPORT", 1, e);
                                    }
                                    this.listerStatus.put("status", "");
                                }
                            }
                            Worker.startWorker(new Lister(v, params, uVFS, listerStatus), "Reports:NewFiles:getting file list");
                            long filesSize = 0L;
                            long fileCount = 0L;
                            Vector<Properties> validFiles = new Vector<Properties>();
                            while (!listerStatus.getProperty("status", "").equals("") || v.size() > 0) {
                                if (v.size() == 0) {
                                    Thread.sleep(100L);
                                    continue;
                                }
                                Properties p = (Properties)v.elementAt(0);
                                v.removeElementAt(0);
                                if (p == null) continue;
                                boolean ok = true;
                                String method = params.getProperty("searchPath", "");
                                if (method.equals("contains") && (String.valueOf(p.getProperty("root_dir", "")) + p.getProperty("name", "")).toUpperCase().indexOf(params.getProperty("path").toUpperCase()) < 0) {
                                    ok = false;
                                }
                                if (method.equals("starts with") && !(String.valueOf(p.getProperty("root_dir", "")) + p.getProperty("name", "")).toUpperCase().startsWith(params.getProperty("path").toUpperCase())) {
                                    ok = false;
                                }
                                if (method.equals("ends with") && !(String.valueOf(p.getProperty("root_dir", "")) + p.getProperty("name", "")).toUpperCase().endsWith(params.getProperty("path").toUpperCase())) {
                                    ok = false;
                                }
                                if (method.equals("equals") && !(String.valueOf(p.getProperty("root_dir", "")) + p.getProperty("name", "")).toUpperCase().equals(params.getProperty("path").toUpperCase())) {
                                    ok = false;
                                }
                                if (!ok) continue;
                                filesSize += Long.parseLong(p.getProperty("size", "0"));
                                ++fileCount;
                                validFiles.addElement(p);
                            }
                            Properties user_cache = (Properties)cachedDirListings.get(username.toUpperCase());
                            boolean firstRun = false;
                            if (user_cache == null) {
                                user_cache = new Properties();
                            }
                            cachedDirListings.put(username.toUpperCase(), user_cache);
                            int xx2 = validFiles.size() - 1;
                            while (xx2 >= 0) {
                                Properties p = (Properties)validFiles.elementAt(xx2);
                                boolean ok = true;
                                if (firstRun) {
                                    ok = false;
                                } else if (p.getProperty("modified").equals(user_cache.getProperty(crushftp.handlers.Common.replace_str(p.getProperty("url"), "#", "___---POUND---___"), ""))) {
                                    ok = false;
                                } else if (p.getProperty("name", "").startsWith(".")) {
                                    ok = false;
                                } else if (p.getProperty("type").equalsIgnoreCase("DIR") && !params.getProperty("onlyFolders", "").equals("true")) {
                                    ok = false;
                                } else if (p.getProperty("type").equalsIgnoreCase("FILE") && params.getProperty("onlyFolders", "").equals("true")) {
                                    ok = false;
                                }
                                if (p.getProperty("type").equalsIgnoreCase("DIR") && params.getProperty("onlyFolders", "").equals("true") && p.getProperty("root_dir", "").equals("/")) {
                                    ok = false;
                                }
                                if (!ok) {
                                    validFiles.removeElementAt(xx2);
                                }
                                user_cache.put(crushftp.handlers.Common.replace_str(p.getProperty("url"), "#", "___---POUND---___"), p.getProperty("modified"));
                                --xx2;
                            }
                            Enumeration<Object> keys = user_cache.keys();
                            while (keys.hasMoreElements()) {
                                String path;
                                String pathOriginal = path = keys.nextElement().toString();
                                if (path.toUpperCase().startsWith("FILE:")) {
                                    URL url = new URL(path);
                                    path = url.getPath();
                                    path = crushftp.handlers.Common.url_decode(crushftp.handlers.Common.replace_str(path, "#", "___---POUND---___"));
                                }
                                if (new File_U(path).exists() || new File_U(pathOriginal.substring("FILE:".length())).exists()) continue;
                                user_cache.remove(pathOriginal);
                            }
                            if (validFiles.size() > 0) {
                                String from = this.replaceVars(params.getProperty("from", ""), user);
                                String to = this.replaceVars(params.getProperty("to", ""), user);
                                String cc = this.replaceVars(params.getProperty("cc", ""), user);
                                String bcc = this.replaceVars(params.getProperty("bcc", ""), user);
                                String subject = this.replaceVars(params.getProperty("subject", ""), user);
                                String body = this.replaceVars(params.getProperty("body", ""), user);
                                String the_body_line = "";
                                try {
                                    the_body_line = body.substring(body.toUpperCase().indexOf("<LINE>") + "<LINE>".length(), body.toUpperCase().indexOf("</LINE>"));
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                                String lineData = "";
                                int xx3 = 0;
                                while (xx3 < validFiles.size()) {
                                    Properties p = (Properties)validFiles.elementAt(xx3);
                                    String the_line = the_body_line;
                                    the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_path%", p.getProperty("root_dir", ""));
                                    String name = p.getProperty("name", "");
                                    String ext = "";
                                    if (name.indexOf(".") >= 0) {
                                        ext = name.substring(0, name.lastIndexOf("."));
                                        name = name.substring(0, name.lastIndexOf("."));
                                    }
                                    the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_name%", name);
                                    the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_ext%", ext);
                                    the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_size%", p.getProperty("size", ""));
                                    the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_size_formatted%", Common.format_bytes_short2(Long.parseLong(p.getProperty("size", "0"))));
                                    the_line = crushftp.handlers.Common.replace_str(the_line, "%all%", p.toString());
                                    if (!(the_line = this.replaceVars(the_line, user)).trim().equals("")) {
                                        lineData = String.valueOf(lineData) + the_line + "\r\n";
                                    }
                                    ++xx3;
                                }
                                Log.log("REPORT", 2, "BODY:<LINE>" + lineData + "</LINE>");
                                try {
                                    body = crushftp.handlers.Common.replace_str(body, body.substring(body.toUpperCase().indexOf("<LINE>"), body.toUpperCase().indexOf("</LINE>") + "</LINE>".length()), lineData);
                                }
                                catch (Exception e) {
                                    Log.log("REPORT", 1, e);
                                }
                                String emailResult = "";
                                if (to.startsWith("REPORT_TEST_") || from.startsWith("REPORT_TEST_")) {
                                    emailResult = "Email Skipped";
                                } else {
                                    try {
                                        emailResult = Common.send_mail(this.server_settings.getProperty("discovered_ip"), to, cc, bcc, from, subject, body, this.server_settings.getProperty("smtp_server"), this.server_settings.getProperty("smtp_user"), this.server_settings.getProperty("smtp_pass"), this.server_settings.getProperty("smtp_ssl").equals("true"), this.server_settings.getProperty("smtp_html").equals("true"), null);
                                    }
                                    catch (Exception e) {
                                        Log.log("REPORT", 1, e);
                                    }
                                }
                                if (emailResult.toUpperCase().indexOf("SUCCESS") < 0 || Log.log("REPORT", 1, "")) {
                                    Log.log("REPORT", 0, "RESULT: " + emailResult + "\r\n");
                                    Log.log("REPORT", 0, "FROM: " + from + "\r\n");
                                    Log.log("REPORT", 0, "TO: " + to + "\r\n");
                                    Log.log("REPORT", 0, "CC: " + cc + "\r\n");
                                    Log.log("REPORT", 0, "BCC: " + bcc + "\r\n");
                                    Log.log("REPORT", 0, "SUBJECT: " + subject + "\r\n");
                                    Log.log("REPORT", 0, "BODY: " + body + "\r\n");
                                }
                                if (Log.log("REPORT", 1, "")) {
                                    try {
                                        throw new Exception("Who called?");
                                    }
                                    catch (Exception e) {
                                        Log.log("REPORT", 1, e);
                                    }
                                }
                                uVFS.disconnect();
                                uVFS.free();
                            }
                            Properties userObj = new Properties();
                            userObj.put("username", username);
                            userObj.put("fileCount", String.valueOf(validFiles.size()));
                            userDetails.put(username, userObj);
                        }
                        ++x;
                    }
                }
                ++loop;
            }
            Vector users = this.doSort(userDetails, cd1);
            Properties results = new Properties();
            results.put("users", users);
            results.put("export", params.getProperty("export", ""));
            Properties params2 = (Properties)params.clone();
            params2.remove("body");
            results.put("params", crushftp.handlers.Common.removeNonStrings(params2).toString());
            results.put("paramsObj", crushftp.handlers.Common.removeNonStrings(params));
            status.put("report_empty", "false");
            crushftp.handlers.Common cfr_ignored_0 = ServerStatus.thisObj.common_code;
            sb.append(crushftp.handlers.Common.getXMLString(results, "results", "WebInterface/Reports/NewFiles.xsl"));
            new crushftp.handlers.Common();
            crushftp.handlers.Common.writeXMLObject(String.valueOf(System.getProperty("crushftp.backup")) + "backup/NewFileReportCache.XML", (Object)cachedDirListings, "cached_dir");
        }
        catch (Exception e) {
            Log.log("REPORT", 1, e);
        }
    }

    public String replaceVars(String data, Properties user) {
        data = crushftp.handlers.Common.replace_str(data, "%user_name%", user.getProperty("user_name"));
        data = crushftp.handlers.Common.replace_str(data, "%user_pass%", ServerStatus.thisObj.common_code.decode_pass(user.getProperty("password")));
        data = crushftp.handlers.Common.replace_str(data, "%user_email%", user.getProperty("email"));
        return data;
    }

    public Vector doSort(Properties item, sorter c) {
        Enumeration<Object> e = item.keys();
        Vector<Object> v = new Vector<Object>();
        while (e.hasMoreElements()) {
            String key = e.nextElement().toString();
            v.addElement((Properties)item.get(key));
        }
        Object[] objs = v.toArray();
        Arrays.sort(objs, c);
        v.removeAllElements();
        int x = 0;
        while (x < objs.length) {
            v.addElement(objs[x]);
            ++x;
        }
        return v;
    }

    public void updateItems(Properties p, Properties details, String username, SimpleDateFormat sdf, long start, long end, Properties params) {
        long date = Long.parseLong(p.getProperty("date"));
        if (date < start || date > end) {
            return;
        }
        if (details.get(username.toUpperCase()) == null) {
            Properties pp = new Properties();
            pp.putAll((Map<?, ?>)p);
            pp.remove("speed");
            pp.remove("ip");
            pp.remove("date");
            pp.put("username", username);
            pp.put("files", new Vector());
            pp.put("filesDetail", new Vector());
            details.put(username.toUpperCase(), pp);
        }
        Properties itemDetails = (Properties)details.get(username.toUpperCase());
        boolean ok = true;
        String method = params.getProperty("searchFilename");
        if (method.equals("contains") && p.getProperty("name", "").toUpperCase().indexOf(params.getProperty("filename").toUpperCase()) < 0) {
            ok = false;
        }
        if (method.equals("starts with") && !p.getProperty("name", "").toUpperCase().startsWith(params.getProperty("filename").toUpperCase())) {
            ok = false;
        }
        if (method.equals("ends with") && !p.getProperty("name", "").toUpperCase().endsWith(params.getProperty("filename").toUpperCase())) {
            ok = false;
        }
        if (method.equals("equals") && !p.getProperty("name", "").toUpperCase().equals(params.getProperty("filename").toUpperCase())) {
            ok = false;
        }
        if ((method = params.getProperty("searchPath")).equals("contains") && p.getProperty("path", "").toUpperCase().indexOf(params.getProperty("path").toUpperCase()) < 0) {
            ok = false;
        }
        if (method.equals("starts with") && !p.getProperty("path", "").toUpperCase().startsWith(params.getProperty("path").toUpperCase())) {
            ok = false;
        }
        if (method.equals("ends with") && !p.getProperty("path", "").toUpperCase().endsWith(params.getProperty("path").toUpperCase())) {
            ok = false;
        }
        if (method.equals("equals") && !p.getProperty("path", "").toUpperCase().equals(params.getProperty("path").toUpperCase())) {
            ok = false;
        }
        if (ok) {
            Vector files = (Vector)itemDetails.get("files");
            Vector filesDetail = (Vector)itemDetails.get("filesDetail");
            if (files.indexOf(p.getProperty("url")) < 0) {
                files.addElement(p.getProperty("url"));
                p.put("dates", new Vector());
                Vector dates = (Vector)p.get("dates");
                String dateStr = sdf.format(new Date(date));
                dates.addElement(dateStr);
                filesDetail.addElement(p);
            } else {
                p = (Properties)filesDetail.elementAt(files.indexOf(p.getProperty("url")));
                Vector dates = (Vector)p.get("dates");
                String dateStr = sdf.format(new Date(date));
                dates.addElement(dateStr);
            }
        }
    }

    class sorter
    implements Comparator {
        Properties allItems = null;
        String sort = null;

        sorter() {
        }

        public void setObj(Properties allItems, String sort) {
            this.allItems = allItems;
            this.sort = sort;
        }

        public int compare(Object p1, Object p2) {
            String val1 = ((Properties)p1).getProperty(this.sort, "0");
            String val2 = ((Properties)p2).getProperty(this.sort, "0");
            try {
                if (Float.parseFloat(val1) > Float.parseFloat(val2)) {
                    return -1;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                if (Float.parseFloat(val1) < Float.parseFloat(val2)) {
                    return 1;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            return val1.compareTo(val2) * 1;
        }
    }
}

