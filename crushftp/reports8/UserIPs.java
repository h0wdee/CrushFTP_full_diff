/*
 * Decompiled with CFR 0.152.
 */
package crushftp.reports8;

import com.crushftp.tunnel2.DVector;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.reports8.ReportTools;
import crushftp.server.ServerStatus;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

public class UserIPs {
    Properties server_info = null;
    Properties server_settings = null;
    static Properties lookupCache = new Properties();

    public void init(Properties server_settings, Properties server_info) {
        this.server_settings = server_settings;
        this.server_info = server_info;
    }

    public void generate(Properties stats, Properties params, StringBuffer sb, Properties status) {
        try {
            Common.setupReportDates(params, params.getProperty("show", ""), params.getProperty("startDate"), params.getProperty("endDate"));
            SimpleDateFormat mmddyyyy = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
            Vector usernames = (Vector)params.get("usernames");
            String sql = "SELECT USER_NAME as \"username\", count(distinct IP) as \"count\"\r FROM SESSIONS\r where START_TIME >= ? and END_TIME <= ?\r /*START_USERNAMES*/and USER_NAME in (%usernames%)/*END_USERNAMES*/\r group by USER_NAME\r order by USER_NAME\r";
            String tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/UserIPs.sql");
            if (tmp != null) {
                sql = tmp;
            } else {
                Common.setFileText(sql, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/UserIPs.sql");
            }
            sql = ReportTools.fixSqlUsernames(sql, usernames);
            Vector ips = ServerStatus.thisObj.statTools.executeSqlQuery_mem(sql, new Object[]{mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00"))}, false, params);
            String sql_ips = "SELECT USER_NAME as \"username\", IP as \"ip\", count(IP) as \"count\"\r FROM SESSIONS\r where START_TIME >= ? and END_TIME <= ?\r /*START_USERNAMES*/and USER_NAME in (%usernames%)/*END_USERNAMES*/\r group by USER_NAME, IP\r order by USER_NAME\r";
            tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/UserIPs_all.sql");
            if (tmp != null) {
                sql_ips = tmp;
            } else {
                Common.setFileText(sql_ips, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/UserIPs_all.sql");
            }
            sql_ips = ReportTools.fixSqlUsernames(sql_ips, usernames);
            try (DVector allIps = ServerStatus.thisObj.statTools.executeSqlQuery(sql_ips, new Object[]{mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00"))}, false, params);){
                Properties uniques = new Properties();
                int x = 0;
                while (x < ips.size()) {
                    Properties p = (Properties)ips.elementAt(x);
                    int xx = 0;
                    while (xx < allIps.size()) {
                        Properties pp = (Properties)allIps.elementAt(xx);
                        if (params.getProperty("reverseDNS", "").equals("true")) {
                            try {
                                String key = pp.getProperty("ip");
                                if (lookupCache.getProperty(pp.getProperty("ip"), "").equals("")) {
                                    InetAddress addr = InetAddress.getByName(key);
                                    lookupCache.put(key, addr.getHostName());
                                }
                                key = String.valueOf(key) + " (" + lookupCache.getProperty(key) + ")";
                                p.put("ip", key);
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                        if (pp.getProperty("ip") != null) {
                            if (pp.getProperty("username", "").equals(p.getProperty("username", "")) && pp.getProperty("count") != null) {
                                p.put(pp.getProperty("ip"), pp.getProperty("count"));
                            }
                            uniques.put(pp.getProperty("ip"), "");
                        }
                        ++xx;
                    }
                    ++x;
                }
                Properties results = new Properties();
                results.put("unique_ip_count", String.valueOf(uniques.size()));
                results.put("ips", ips);
                results.put("showIPs", params.getProperty("showIPs"));
                results.put("reverseDNS", params.getProperty("reverseDNS"));
                results.put("showCounts", params.getProperty("showCounts"));
                Common common_code = new Common();
                results.put("export", params.getProperty("export", ""));
                results.put("params", Common.removeNonStrings(params).toString());
                results.put("paramsObj", Common.removeNonStrings(params));
                if (ips.size() > 0) {
                    status.put("report_empty", "false");
                }
                sb.append(Common.getXMLString(results, "results", "WebInterface/Reports/UserIPs.xsl"));
            }
        }
        catch (Exception e) {
            Log.log("REPORT", 1, e);
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

