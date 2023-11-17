/*
 * Decompiled with CFR 0.152.
 */
package crushftp.reports8;

import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.reports8.ReportTools;
import crushftp.server.ServerStatus;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

public class FailedLogins {
    Properties server_info = null;
    Properties server_settings = null;

    public void init(Properties server_settings, Properties server_info) {
        this.server_settings = server_settings;
        this.server_info = server_info;
    }

    public void generate(Properties stats, Properties params, StringBuffer sb, Properties status) {
        try {
            Common.setupReportDates(params, params.getProperty("show", ""), params.getProperty("startDate"), params.getProperty("endDate"));
            SimpleDateFormat mmddyyyy = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
            Vector usernames = (Vector)params.get("usernames");
            String sql = "SELECT USER_NAME as \"username\", START_TIME as \"start_time\", IP as \"ip\"\r FROM SESSIONS\r where SUCCESS_LOGIN = 'false' and START_TIME >= ? and END_TIME <= ?\r /*START_USERNAMES*/and USER_NAME in (%usernames%)/*END_USERNAMES*/\rgroup by USER_NAME, START_TIME, IP\r order by USER_NAME\r";
            String tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/FailedLogins.sql");
            if (tmp != null) {
                sql = tmp;
            } else {
                Common.setFileText(sql, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/FailedLogins.sql");
            }
            sql = ReportTools.fixSqlUsernames(sql, usernames);
            Vector ips = ServerStatus.thisObj.statTools.executeSqlQuery_mem(sql, new Object[]{mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00"))}, false, params);
            if (params.getProperty("excludeAnonymous", "false").equals("true")) {
                Vector temp = new Vector();
                temp.addAll(ips);
                try {
                    int x = 0;
                    while (x < ips.size()) {
                        Properties p = (Properties)ips.get(x);
                        if (p.getProperty("username", "").toLowerCase().equals("anonymous")) {
                            temp.remove(p);
                        }
                        ++x;
                    }
                }
                catch (Exception e) {
                    Log.log("REPORT", 1, e);
                }
                ips = temp;
            }
            Properties results = new Properties();
            results.put("ips", ips);
            Common common_code = new Common();
            results.put("export", params.getProperty("export", ""));
            results.put("params", Common.removeNonStrings(params).toString());
            results.put("paramsObj", Common.removeNonStrings(params));
            if (ips.size() > 0) {
                status.put("report_empty", "false");
            }
            sb.append(Common.getXMLString(results, "results", "WebInterface/Reports/FailedLogins.xsl"));
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

