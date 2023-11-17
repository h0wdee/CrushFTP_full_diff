/*
 * Decompiled with CFR 0.152.
 */
package crushftp.reports8;

import com.crushftp.client.Common;
import com.crushftp.client.Worker;
import crushftp.handlers.Log;
import crushftp.handlers.SessionCrush;
import crushftp.handlers.UserTools;
import crushftp.reports8.ReportTools;
import crushftp.server.ServerStatus;
import crushftp.server.VFS;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class UserFolderSizes {
    Properties server_info = null;
    Properties server_settings = null;

    public void init(Properties server_settings, Properties server_info) {
        this.server_settings = server_settings;
        this.server_info = server_info;
    }

    public void generate(Properties stats, Properties params, StringBuffer sb, Properties status) {
        try {
            crushftp.handlers.Common.setupReportDates(params, params.getProperty("show", ""), params.getProperty("startDate"), params.getProperty("endDate"));
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.US);
            SimpleDateFormat mmddyyyy = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
            long start = mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")).getTime();
            long end = mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00")).getTime();
            Vector usernames = (Vector)params.get("usernames");
            ReportTools.fixListUsernames(usernames);
            sorter cd1 = new sorter();
            Properties userDetails = new Properties();
            cd1.setObj(userDetails, "username");
            Vector sgs = (Vector)this.server_settings.get("server_groups");
            int i = 0;
            while (i < sgs.size()) {
                String server = sgs.elementAt(i).toString();
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
                    boolean user_ok = false;
                    int xx = 0;
                    while (xx < usernames.size()) {
                        if (Common.do_search(usernames.elementAt(xx).toString().toUpperCase(), username.toUpperCase(), false, 0)) {
                            user_ok = true;
                        }
                        ++xx;
                    }
                    if (user_ok || usernames.size() <= 0) {
                        long total_quota;
                        VFS uVFS = UserTools.ut.getVFS(server, username);
                        Properties user_obj = UserTools.ut.getUser(server, username, true);
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
                        Worker.startWorker(new Lister(v, params, uVFS, listerStatus), "Reports:UserFolderSizes:getting file list");
                        long filesSize = 0L;
                        long fileCount = 0L;
                        String firstDir = null;
                        while (!listerStatus.getProperty("status", "").equals("") || v.size() > 0) {
                            String method;
                            if (v.size() == 0) {
                                Thread.sleep(100L);
                                continue;
                            }
                            Properties p = (Properties)v.elementAt(0);
                            v.removeElementAt(0);
                            if (p == null) continue;
                            boolean ok = true;
                            if (firstDir == null) {
                                firstDir = p.getProperty("root_dir", "");
                            }
                            if ((method = params.getProperty("searchPath", "")).equals("contains") && p.getProperty("root_dir", "").toUpperCase().indexOf(params.getProperty("path").toUpperCase()) < 0) {
                                ok = false;
                            }
                            if (method.equals("starts with") && !p.getProperty("root_dir", "").toUpperCase().startsWith(params.getProperty("path").toUpperCase())) {
                                ok = false;
                            }
                            if (method.equals("ends with") && !p.getProperty("root_dir", "").toUpperCase().endsWith(params.getProperty("path").toUpperCase())) {
                                ok = false;
                            }
                            if (method.equals("equals") && !p.getProperty("root_dir", "").toUpperCase().equals(params.getProperty("path").toUpperCase())) {
                                ok = false;
                            }
                            if (!ok) continue;
                            filesSize += Long.parseLong(p.getProperty("size", "0"));
                            ++fileCount;
                        }
                        Properties user = new Properties();
                        user.put("username", username);
                        user.put("fileCount", String.valueOf(fileCount));
                        user.put("fileSize", String.valueOf(filesSize));
                        user.put("fileSizeFormatted", Common.format_bytes_short2(filesSize));
                        if (firstDir == null) {
                            firstDir = "/";
                        }
                        if ((total_quota = SessionCrush.get_total_quota(firstDir, uVFS, new Properties())) == -12345L && !user_obj.getProperty("quota_mb", "").equals("")) {
                            total_quota = Long.parseLong(user_obj.getProperty("quota_mb", "")) * 1024L * 1024L;
                        }
                        user.put("quota", String.valueOf(total_quota));
                        user.put("quotaFormatted", Common.format_bytes_short2(total_quota));
                        if (total_quota == -12345L) {
                            user.put("quota", "");
                            user.put("quotaFormatted", "unlimited");
                        }
                        userDetails.put(username, user);
                        uVFS.disconnect();
                        uVFS.free();
                    }
                    ++x;
                }
                ++i;
            }
            Vector users = this.doSort(userDetails, cd1);
            Properties results = new Properties();
            results.put("users", users);
            results.put("export", params.getProperty("export", ""));
            results.put("params", crushftp.handlers.Common.removeNonStrings(params).toString());
            results.put("paramsObj", crushftp.handlers.Common.removeNonStrings(params));
            if (users.size() > 0) {
                status.put("report_empty", "false");
            }
            crushftp.handlers.Common cfr_ignored_0 = ServerStatus.thisObj.common_code;
            sb.append(crushftp.handlers.Common.getXMLString(results, "results", "WebInterface/Reports/UserFolderSizes.xsl"));
        }
        catch (Exception e) {
            Log.log("REPORT", 1, e);
        }
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

