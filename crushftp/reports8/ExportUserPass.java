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

public class ExportUserPass {
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
            Vector sgs = (Vector)this.server_settings.get("server_groups");
            int i = 0;
            while (i < sgs.size()) {
                String server = sgs.elementAt(i).toString();
                Vector current_user_group_listing = new Vector();
                Properties groups = UserTools.getGroups(server);
                Properties inheritance = UserTools.getInheritance(server);
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
                        Properties user = UserTools.ut.getUser(server, username, false);
                        if (user == null) {
                            Log.log("SERVER", 0, "Username not loadable, but folder existed in users:" + username);
                        } else {
                            String password = new crushftp.handlers.Common().decode_pass(user.getProperty("password", ""));
                            user.put("username", username);
                            user.put("password", password);
                            String group_member_of = "";
                            if (groups != null) {
                                Enumeration<Object> keys = groups.keys();
                                while (keys.hasMoreElements()) {
                                    String key = keys.nextElement().toString();
                                    Vector v = (Vector)groups.get(key);
                                    if (!(groups.get(key) instanceof Vector) || groups.get(key) == null || !((Vector)groups.get(key)).contains(username)) continue;
                                    group_member_of = String.valueOf(key) + ",";
                                }
                                if (group_member_of.endsWith(",")) {
                                    group_member_of = group_member_of.substring(0, group_member_of.length() - 1);
                                }
                            }
                            String inherit_from = "";
                            if (inheritance != null && inheritance.containsKey(username) && inheritance.get(username) instanceof Vector && inheritance.get(username) != null) {
                                Vector v2 = (Vector)inheritance.get(username);
                                int xx2 = 0;
                                while (xx2 < v2.size()) {
                                    inherit_from = String.valueOf(inherit_from) + v2.get(xx2) + ",";
                                    ++xx2;
                                }
                                if (inherit_from.endsWith(",")) {
                                    inherit_from = inherit_from.substring(0, inherit_from.length() - 1);
                                }
                            }
                            user.put("groups", group_member_of);
                            user.put("inheritance", inherit_from);
                            userDetails.put(username, user);
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
            sb.append(crushftp.handlers.Common.getXMLString(results, "results", "WebInterface/Reports/ExportUserPass.xsl"));
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

