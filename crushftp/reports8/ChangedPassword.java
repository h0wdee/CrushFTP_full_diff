/*
 * Decompiled with CFR 0.152.
 */
package crushftp.reports8;

import com.crushftp.client.Common;
import crushftp.handlers.Log;
import crushftp.handlers.UserTools;
import crushftp.reports8.ReportTools;
import crushftp.server.ServerStatus;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

public class ChangedPassword {
    Properties server_info = null;
    Properties server_settings = null;

    public void init(Properties server_settings, Properties server_info) {
        this.server_settings = server_settings;
        this.server_info = server_info;
    }

    public void generate(Properties stats, Properties params, StringBuffer sb, Properties status) {
        try {
            crushftp.handlers.Common.setupReportDates(params, params.getProperty("show", ""), params.getProperty("startDate"), params.getProperty("endDate"));
            Vector usernames = (Vector)params.get("usernames");
            ReportTools.fixListUsernames(usernames);
            sorter cd1 = new sorter();
            Properties userDetails = new Properties();
            cd1.setObj(userDetails, "username");
            if (ServerStatus.BG("auto_fix_stats_sessions")) {
                ServerStatus.thisObj.statTools.executeSql("update SESSIONS set END_TIME = START_TIME where END_TIME < ? and START_TIME < ?", new Object[]{new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2000-01-01 00:00:00"), new Date(System.currentTimeMillis() - 86400000L)});
            }
            crushftp.handlers.Common.setupReportDates(params, params.getProperty("show", ""), params.getProperty("startDate"), params.getProperty("endDate"));
            SimpleDateFormat mmddyyyy = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
            String sql = "select SESSION, s.START_TIME as \"START_TIME\", IP, USER_NAME as \"USER_NAME\"  FROM SESSIONS s\r where s.START_TIME >= ? and s.END_TIME <= ?\r and SERVER_GROUP = 'CHANGE_PASS'\r order by RID\r";
            sql = ReportTools.fixMsSql(sql);
            String tmp = crushftp.handlers.Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/ChangedPassword.sql");
            if (tmp != null) {
                sql = tmp;
            } else {
                crushftp.handlers.Common.setFileText(sql, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/ChangedPassword.sql");
            }
            sql = ReportTools.fixSqlUsernames(sql, usernames);
            Vector summary = ServerStatus.thisObj.statTools.executeSqlQuery_mem(sql, new Object[]{mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00"))}, false, params);
            Vector sgs = (Vector)this.server_settings.get("server_groups");
            int i = 0;
            while (i < sgs.size()) {
                String server_group = sgs.elementAt(i).toString();
                Vector current_user_group_listing = new Vector();
                UserTools.refreshUserList(server_group, current_user_group_listing);
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
                        Properties user = UserTools.ut.getUser(server_group, username, false);
                        if (user == null) {
                            Log.log("SERVER", 0, "Username not loadable, but folder existed in users:" + username);
                        } else {
                            Properties user2 = new Properties();
                            user2.put("username", username);
                            user2.put("server_group", server_group);
                            user2.put("password_changed", "");
                            user2.put("password_info", "");
                            user2.put("changed_by_ip", "");
                            int xx2 = 0;
                            while (xx2 < summary.size()) {
                                Properties p = (Properties)summary.elementAt(xx2);
                                if (p.getProperty("USER_NAME").equalsIgnoreCase(username) && p.getProperty("SESSION").startsWith(String.valueOf(server_group) + "_")) {
                                    user2.put("password_changed", p.getProperty("START_TIME"));
                                    user2.put("password_info", p.getProperty("SESSION"));
                                    user2.put("changed_by_ip", p.getProperty("IP"));
                                }
                                ++xx2;
                            }
                            userDetails.put(username, user2);
                        }
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
            status.put("report_empty", "false");
            crushftp.handlers.Common cfr_ignored_0 = ServerStatus.thisObj.common_code;
            sb.append(crushftp.handlers.Common.getXMLString(results, "results", "WebInterface/Reports/ChangedPassword.xsl"));
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

