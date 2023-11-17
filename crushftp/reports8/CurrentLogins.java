/*
 * Decompiled with CFR 0.152.
 */
package crushftp.reports8;

import crushftp.handlers.Common;
import crushftp.handlers.Log;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class CurrentLogins {
    Properties server_info = null;
    Properties server_settings = null;

    public void init(Properties server_settings, Properties server_info) {
        this.server_settings = server_settings;
        this.server_info = server_info;
    }

    public void generate(Properties stats, Properties params, StringBuffer sb, Properties status) {
        try {
            Properties historyUser;
            Properties user_info;
            Common.setupReportDates(params, params.getProperty("show", ""), params.getProperty("startDate"), params.getProperty("endDate"));
            Properties si2 = new Properties();
            Vector<Properties> recent_users = new Vector<Properties>();
            si2.put("recent_users", recent_users);
            Vector user_list = (Vector)this.server_info.get("user_list");
            Properties userHistory = new Properties();
            Vector recent_user_list = (Vector)this.server_info.get("recent_user_list");
            int pos = 0;
            int x = user_list.size() - 1;
            while (x >= 0) {
                ++pos;
                try {
                    status.put("status", String.valueOf((int)(((float)pos / (float)user_list.size() + (float)recent_user_list.size()) * 100.0f)) + "%");
                }
                catch (Exception exception) {
                    // empty catch block
                }
                try {
                    user_info = (Properties)((Properties)user_list.elementAt(x)).clone();
                    user_info.remove("session");
                    user_info.remove("stat");
                    user_info.remove("events");
                    user_info.remove("current_password");
                    user_info.remove("new_pass1");
                    user_info.remove("new_pass2");
                    user_info.remove("user_log");
                    user_info.put("status", "active");
                    if (userHistory.get(user_info.getProperty("login_date_stamp", "")) == null) {
                        recent_users.addElement(user_info);
                        userHistory.put(user_info.getProperty("login_date_stamp", ""), user_info);
                    } else {
                        historyUser = (Properties)userHistory.get(user_info.getProperty("login_date_stamp", ""));
                        historyUser.put("session_upload_count", String.valueOf(Integer.parseInt(historyUser.getProperty("session_upload_count", "0")) + Integer.parseInt(user_info.getProperty("session_upload_count", "0"))));
                        historyUser.put("session_download_count", String.valueOf(Integer.parseInt(historyUser.getProperty("session_download_count", "0")) + Integer.parseInt(user_info.getProperty("session_download_count", "0"))));
                    }
                }
                catch (Exception e) {
                    Log.log("REPORT", 1, e);
                }
                --x;
            }
            x = recent_user_list.size() - 1;
            while (x >= 0) {
                ++pos;
                try {
                    status.put("status", String.valueOf((int)(((float)pos / (float)user_list.size() + (float)recent_user_list.size()) * 100.0f)) + "%");
                }
                catch (Exception e) {
                    // empty catch block
                }
                try {
                    user_info = (Properties)((Properties)recent_user_list.elementAt(x)).clone();
                    user_info.remove("session");
                    user_info.remove("stat");
                    user_info.remove("events");
                    user_info.remove("current_password");
                    user_info.remove("new_pass1");
                    user_info.remove("new_pass2");
                    user_info.remove("user_log");
                    user_info.put("status", "inactive");
                    if (userHistory.get(user_info.getProperty("login_date_stamp", "")) == null) {
                        recent_users.addElement(user_info);
                        userHistory.put(user_info.getProperty("login_date_stamp", ""), user_info);
                    } else {
                        historyUser = (Properties)userHistory.get(user_info.getProperty("login_date_stamp", ""));
                        historyUser.put("session_upload_count", String.valueOf(Integer.parseInt(historyUser.getProperty("session_upload_count", "0")) + Integer.parseInt(user_info.getProperty("session_upload_count", "0"))));
                        historyUser.put("session_download_count", String.valueOf(Integer.parseInt(historyUser.getProperty("session_download_count", "0")) + Integer.parseInt(user_info.getProperty("session_download_count", "0"))));
                    }
                }
                catch (Exception e) {
                    Log.log("REPORT", 1, e);
                }
                --x;
            }
            si2.put("showFiles", params.getProperty("showFiles"));
            si2.put("export", params.getProperty("export", ""));
            si2.put("params", Common.removeNonStrings(params).toString());
            si2.put("paramsObj", Common.removeNonStrings(params));
            Common common_code = new Common();
            status.put("report_empty", "false");
            sb.append(Common.getXMLString(si2, "results", "WebInterface/Reports/CurrentLogins.xsl"));
        }
        catch (Exception e) {
            Log.log("REPORT", 1, e);
        }
    }

    public void addCSVElements(StringBuffer sb, Object obj) {
        try {
            if (obj != null && obj instanceof Properties) {
                Properties p = (Properties)obj;
                Enumeration<Object> e = p.keys();
                while (e.hasMoreElements()) {
                    String key = e.nextElement().toString();
                    Object val = p.get(key);
                    sb.append(String.valueOf(key) + ",");
                    this.addCSVElements(sb, val);
                    sb.append(",");
                }
            } else if (obj != null && obj instanceof Vector) {
                Vector v = (Vector)obj;
                int x = 0;
                while (x < v.size()) {
                    this.addCSVElements(sb, v.elementAt(x));
                    sb.append("\r\n");
                    ++x;
                }
            } else if (obj != null) {
                String s = (String)obj;
                sb.append(s);
            }
        }
        catch (Exception e) {
            Log.log("REPORT", 1, e);
        }
    }

    public Vector doSort(Properties item, counts c) {
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

    public void updateItems(Properties p, Properties details, String username, SimpleDateFormat sdf, long start, long end) {
        long date = Long.parseLong(p.getProperty("date"));
        if (date < start || date > end) {
            return;
        }
        if (details.get(p.getProperty("url")) == null) {
            Properties pp = new Properties();
            pp.putAll((Map<?, ?>)p);
            pp.remove("speed");
            pp.remove("ip");
            pp.remove("date");
            pp.put("dates", new Vector());
            pp.put("speeds", new Vector());
            pp.put("users", new Vector());
            pp.put("ips", new Vector());
            details.put(p.getProperty("url"), pp);
        }
        Properties itemDetails = (Properties)details.get(p.getProperty("url"));
        int i = Integer.parseInt(itemDetails.getProperty("count", "0"));
        itemDetails.put("count", String.valueOf(++i));
        long speed = Long.parseLong(p.getProperty("speed"));
        long sumSpeed = Long.parseLong(itemDetails.getProperty("sumSpeed", "0")) + speed;
        itemDetails.put("sumSpeed", String.valueOf(sumSpeed));
        if (((Vector)itemDetails.get("speeds")).size() == 0) {
            itemDetails.put("averageSpeed", "0");
        } else {
            itemDetails.put("averageSpeed", String.valueOf(sumSpeed / (long)((Vector)itemDetails.get("speeds")).size()));
        }
        ((Vector)itemDetails.get("speeds")).addElement(p.getProperty("speed"));
        if (((Vector)itemDetails.get("ips")).indexOf(p.getProperty("ip")) < 0) {
            ((Vector)itemDetails.get("ips")).addElement(p.getProperty("ip"));
        }
        if (((Vector)itemDetails.get("users")).indexOf(username) < 0) {
            ((Vector)itemDetails.get("users")).addElement(username);
        }
        String dateStr = sdf.format(new Date(date));
        if (((Vector)itemDetails.get("dates")).indexOf(dateStr) < 0) {
            ((Vector)itemDetails.get("dates")).addElement(dateStr);
        }
    }

    class counts
    implements Comparator {
        Properties allItems = null;
        String sort = null;

        counts() {
        }

        public void setObj(Properties allItems, String sort) {
            this.allItems = allItems;
            this.sort = sort;
        }

        public int compare(Object p1, Object p2) {
            String val1 = ((Properties)p1).getProperty(this.sort, "0");
            String val2 = ((Properties)p2).getProperty(this.sort, "0");
            try {
                if (Integer.parseInt(val1) > Integer.parseInt(val2)) {
                    return -1;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                if (Integer.parseInt(val1) < Integer.parseInt(val2)) {
                    return 1;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            return val1.compareTo(val2) * -1;
        }
    }
}

