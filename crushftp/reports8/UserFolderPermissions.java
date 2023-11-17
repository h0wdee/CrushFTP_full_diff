/*
 * Decompiled with CFR 0.152.
 */
package crushftp.reports8;

import com.crushftp.client.Common;
import com.crushftp.client.GenericClient;
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

public class UserFolderPermissions {
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
            boolean errors_only = params.getProperty("errors_only", "").equals("true");
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
                        Vector linked_vfs = (Vector)tempUser.get("linked_vfs");
                        if (linked_vfs == null) {
                            linked_vfs = new Vector();
                        }
                        boolean linked_vfs_ok = false;
                        if (!params.getProperty("linked_vfs_users", "").equals("")) {
                            String[] vfs_users = params.getProperty("linked_vfs_users", "").split(",");
                            int xx2 = 0;
                            while (xx2 < vfs_users.length && !linked_vfs_ok) {
                                int xxx = 0;
                                while (xxx < linked_vfs.size() && !linked_vfs_ok) {
                                    if (vfs_users[xx2].trim().equalsIgnoreCase(linked_vfs.elementAt(xxx).toString().trim())) {
                                        linked_vfs_ok = true;
                                    }
                                    ++xxx;
                                }
                                ++xx2;
                            }
                        } else {
                            linked_vfs_ok = true;
                        }
                        if (linked_vfs_ok) {
                            int xx3 = 0;
                            while (xx3 < linked_vfs.size()) {
                                if (!linked_vfs.elementAt(xx3).toString().trim().equals("")) {
                                    try {
                                        VFS tempVFS = UserTools.ut.getVFS(server, linked_vfs.elementAt(xx3).toString());
                                        uVFS.addLinkedVFS(tempVFS);
                                    }
                                    catch (Exception e) {
                                        Log.log("REPORT", 1, e);
                                    }
                                }
                                ++xx3;
                            }
                            Vector<Object> v = new Vector<Object>();
                            try {
                                Properties perms = uVFS.getCombinedPermissions();
                                Enumeration<Object> keys = perms.keys();
                                while (keys.hasMoreElements()) {
                                    String key = keys.nextElement().toString();
                                    if (key.equals("/")) continue;
                                    Properties p = uVFS.get_item(key);
                                    if (errors_only && p != null) {
                                        try {
                                            VRL item_vrl = new VRL(p.getProperty("url", ""));
                                            if (item_vrl.getProtocol().equalsIgnoreCase("VIRTUAL")) continue;
                                            try (GenericClient c = uVFS.getClient(p);){
                                                c.login(item_vrl.getUsername(), item_vrl.getPassword(), null);
                                                c.list(item_vrl.getPath(), new Vector());
                                                c.logout();
                                            }
                                        }
                                        catch (Exception e) {
                                            if (e.getMessage() != null) {
                                                p.put("error_message", e.getMessage());
                                            } else {
                                                p.put("error_message", "Unknown error.");
                                            }
                                            v.addElement(VRL.safe(p));
                                        }
                                        continue;
                                    }
                                    if (p != null) {
                                        v.addElement(VRL.safe(p));
                                        continue;
                                    }
                                    if (p != null) continue;
                                    try {
                                        int homeIndex = Integer.parseInt(uVFS.getPrivPath(key, true, false));
                                        Properties pp = uVFS.get_item_parent(key, homeIndex);
                                        if (pp == null) continue;
                                        v.addElement(VRL.safe(pp));
                                    }
                                    catch (Exception e) {
                                        Log.log("REPORT", 1, "Permission path : " + key + " " + e);
                                    }
                                }
                                sorter cd2 = new sorter();
                                cd2.setObj(new Properties(), "url");
                                Object[] tmp = v.toArray();
                                Arrays.sort(tmp, cd2);
                                v.removeAllElements();
                                int xx4 = 0;
                                while (xx4 < tmp.length) {
                                    v.addElement(tmp[xx4]);
                                    ++xx4;
                                }
                            }
                            catch (Exception e) {
                                Log.log("REPORT", 1, e);
                            }
                            Properties user = new Properties();
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
                                int xx5 = 0;
                                while (xx5 < vv.size()) {
                                    if (vv.elementAt(xx5).toString().equalsIgnoreCase(username)) {
                                        group_list = String.valueOf(group_list) + key + ",";
                                    }
                                    ++xx5;
                                }
                            }
                            if (group_list.length() > 0) {
                                group_list = group_list.substring(0, group_list.length() - 1);
                            }
                            user.put("groups", group_list);
                            user.put("listing", v);
                            userDetails.put(username, user);
                            uVFS.disconnect();
                            uVFS.free();
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

