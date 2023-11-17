/*
 * Decompiled with CFR 0.152.
 */
package crushftp.reports8;

import com.crushftp.client.Common;
import com.crushftp.client.VRL;
import crushftp.handlers.Log;
import crushftp.handlers.UserTools;
import crushftp.reports8.ReportTools;
import crushftp.server.ServerStatus;
import crushftp.server.VFS;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class UserFolderAccess {
    Properties server_info = null;
    Properties server_settings = null;

    public void init(Properties server_settings, Properties server_info) {
        this.server_settings = server_settings;
        this.server_info = server_info;
    }

    public void generate(Properties stats, Properties params, StringBuffer sb, Properties status) {
        String last_username = "";
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
            VRL vrl = null;
            if (params.getProperty("search_url", "").equals("") && params.getProperty("search_name", "").equals("")) {
                throw new Exception("Invalid search, must have name, url, or both.");
            }
            String url_search = "";
            try {
                if (!params.getProperty("search_url", "").equals("")) {
                    url_search = params.getProperty("search_url", "file:///").replace('\\', '/');
                    vrl = new VRL(url_search);
                    url_search = vrl.getProtocol().equalsIgnoreCase("FILE") ? String.valueOf(vrl.getProtocol()) + "://" + vrl.getPath() : String.valueOf(vrl.getProtocol()) + "://" + vrl.getHost() + vrl.getPath();
                }
            }
            catch (Exception e) {
                Log.log("REPORT", 1, e);
                url_search = params.getProperty("search_url", "file:///");
            }
            String url_search_vrl_safe = new VRL(url_search).safe();
            url_search = url_search.toUpperCase();
            Vector sgs = (Vector)this.server_settings.get("server_groups");
            int i = 0;
            while (i < sgs.size()) {
                String server = sgs.elementAt(i).toString();
                Vector current_user_group_listing = new Vector();
                Properties groups = UserTools.getGroups(server);
                UserTools.refreshUserList(server, current_user_group_listing);
                int x = 0;
                while (x < current_user_group_listing.size()) {
                    String username;
                    last_username = username = current_user_group_listing.elementAt(x).toString();
                    boolean user_ok = usernames.size() == 0;
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
                        String enabled = UserTools.ut.getEndUserProperty(server, username, "max_logins", "0");
                        VFS uVFS = UserTools.ut.getVFS(server, username);
                        UserTools.setupVFSLinking(server, username, uVFS, tempUser);
                        Vector<Properties> v = new Vector<Properties>();
                        Properties user = new Properties();
                        int xx2 = 0;
                        while (xx2 < uVFS.homes.size()) {
                            Properties virtual = (Properties)uVFS.homes.elementAt(xx2);
                            Enumeration<Object> keys = virtual.keys();
                            while (keys.hasMoreElements()) {
                                String key = keys.nextElement().toString();
                                if (key.equals("vfs_permissions_object")) continue;
                                Properties vItem = (Properties)virtual.get(key);
                                Vector vItems = (Vector)vItem.get("vItems");
                                try {
                                    if (vItems == null) continue;
                                    Properties p = (Properties)vItems.elementAt(0);
                                    vrl = new VRL(p.getProperty("url"));
                                    String url = "";
                                    url = vrl.getProtocol().equalsIgnoreCase("FILE") ? String.valueOf(vrl.getProtocol()) + "://" + vrl.getPath() : String.valueOf(vrl.getProtocol()) + "://" + vrl.getHost() + vrl.getPath();
                                    if (key.toUpperCase().indexOf(params.getProperty("search_name", "").toUpperCase()) < 0) continue;
                                    Log.log("REPORT", 2, "Checking URL:" + new VRL(url).safe() + "   versus search:" + url_search_vrl_safe);
                                    if (!url.toUpperCase().startsWith(url_search) && !url_search.toUpperCase().startsWith(url.toUpperCase())) continue;
                                    Log.log("REPORT", 2, "MATCHED URL:" + new VRL(url).safe());
                                    Properties pp = new Properties();
                                    pp.put("name", key);
                                    pp.put("url", url);
                                    pp.put("privs", uVFS.get_item(key).getProperty("privs", ""));
                                    v.addElement(pp);
                                }
                                catch (Exception e) {
                                    Log.log("REPORT", 1, e);
                                }
                            }
                            ++xx2;
                        }
                        user.put("username", username);
                        user.put("site_privs", tempUser.getProperty("site", ""));
                        user.put("allowed_protocols", tempUser.getProperty("allowed_protocols", ""));
                        user.put("notes", tempUser.getProperty("notes", ""));
                        user.put("email", tempUser.getProperty("email", ""));
                        user.put("enabled", String.valueOf(!enabled.equals("-1")));
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
                        user.put("listing", v);
                        if (v.size() > 0) {
                            userDetails.put(username, user);
                        }
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
            sb.append(crushftp.handlers.Common.getXMLString(results, "results", "WebInterface/Reports/UserFolderAccess.xsl"));
        }
        catch (Exception e) {
            Log.log("REPORT", 1, "Report failed:" + last_username + ":" + e);
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

