/*
 * Decompiled with CFR 0.152.
 */
package crushftp.reports8;

import com.crushftp.client.Common;
import crushftp.handlers.Log;
import crushftp.handlers.UserTools;
import crushftp.reports8.ReportTools;
import crushftp.server.ServerStatus;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class UserEvents {
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
            try {
                status.put("status", "100%");
            }
            catch (Exception exception) {
                // empty catch block
            }
            Vector sgs = (Vector)this.server_settings.get("server_groups");
            int i = 0;
            while (i < sgs.size()) {
                String server = sgs.elementAt(i).toString();
                Vector current_user_group_listing = new Vector();
                Properties groups = UserTools.getGroups(server);
                UserTools.refreshUserList(server, current_user_group_listing);
                int x = 0;
                while (x < current_user_group_listing.size()) {
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
                        Properties tempUser = UserTools.ut.getUser(server, username, true);
                        if (tempUser == null) {
                            tempUser = new Properties();
                        }
                        Properties user = new Properties();
                        user.put("username", username);
                        Vector v = new Vector();
                        if (tempUser.containsKey("events")) {
                            v = (Vector)tempUser.get("events");
                        }
                        user.put("events", v);
                        int xx2 = 0;
                        while (x < v.size()) {
                            Properties event = (Properties)v.elementAt(xx2);
                            Enumeration<Object> event_keys = event.keys();
                            while (event_keys.hasMoreElements()) {
                                String event_key = event_keys.nextElement().toString();
                                if (!(event.get(event_key) instanceof Vector) && !(event.get(event_key) instanceof Properties)) continue;
                                event.remove(event_key);
                            }
                            ++x;
                        }
                        String group_list = "";
                        Enumeration<Object> keys = groups.keys();
                        while (keys.hasMoreElements()) {
                            String key = keys.nextElement().toString();
                            Vector vv = (Vector)groups.get(key);
                            int xx3 = 0;
                            while (xx3 < vv.size()) {
                                if (vv.elementAt(xx3).toString().equalsIgnoreCase(username)) {
                                    group_list = String.valueOf(group_list) + key + ",";
                                }
                                ++xx3;
                            }
                        }
                        if (group_list.length() > 0) {
                            group_list = group_list.substring(0, group_list.length() - 1);
                        }
                        user.put("groups", group_list);
                        userDetails.put(username, user);
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
            sb.append(crushftp.handlers.Common.getXMLString(results, "results", "WebInterface/Reports/UserFolderPermissions.xsl"));
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
            String val1 = ((Properties)p1).getProperty(this.sort, "0").toUpperCase();
            String val2 = ((Properties)p2).getProperty(this.sort, "0").toUpperCase();
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

