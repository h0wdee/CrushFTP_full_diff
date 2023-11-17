/*
 * Decompiled with CFR 0.152.
 */
package crushftp.reports8;

import com.crushftp.client.Common;
import crushftp.handlers.Log;
import crushftp.reports8.ReportTools;
import crushftp.server.ServerStatus;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

public class AccountActivitySummary {
    Properties server_info = null;
    Properties server_settings = null;
    static Properties lookupCache = new Properties();
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.US);
    SimpleDateFormat sdfyy = new SimpleDateFormat("MM/dd/yy hh:mm:ss aa", Locale.US);

    public void init(Properties server_settings, Properties server_info) {
        this.server_settings = server_settings;
        this.server_info = server_info;
    }

    public void generate(Properties stats, Properties params, StringBuffer sb, Properties status) {
        try {
            String datediff;
            if (ServerStatus.BG("auto_fix_stats_sessions")) {
                ServerStatus.thisObj.statTools.executeSql("update SESSIONS set END_TIME = START_TIME where END_TIME < ? and START_TIME < ?", new Object[]{new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2000-01-01 00:00:00"), new Date(System.currentTimeMillis() - 86400000L)});
            }
            crushftp.handlers.Common.setupReportDates(params, params.getProperty("show", ""), params.getProperty("startDate"), params.getProperty("endDate"));
            SimpleDateFormat mmddyyyy = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
            Vector usernames = (Vector)params.get("usernames");
            String datediff2 = datediff = "datediff(ss,s.start_time,s.end_time) * 1000";
            String datediff2U = datediff = "datediff(ss,s.START_TIME,s.END_TIME) * 1000";
            if (ServerStatus.thisObj.statTools.mysql) {
                datediff = "TIME_TO_SEC(timediff(s.END_TIME,s.START_TIME))*1000";
            } else if (ServerStatus.thisObj.statTools.derby) {
                datediff = "{fn TIMESTAMPDIFF(SQL_TSI_SECOND,s.START_TIME,s.END_TIME)} * 1000";
            }
            String sql = "select USER_NAME as \"username\", IP as \"ip\", s.SESSION as \"protocol\", s.SERVER_GROUP as \"server_port\", s.START_TIME as \"start\",s.END_TIME as \"end\", " + datediff + " as \"durationRaw\", sum(CASEWHEN(DIRECTION = 'UPLOAD',1,0)) as \"uploadCount\", sum(CASEWHEN(DIRECTION = 'DOWNLOAD',1,0)) as \"downloadCount\", sum(CASEWHEN(DIRECTION = 'DELETE',1,0)) as \"deleteCount\", sum(CASEWHEN(DIRECTION = 'RENAME',1,0)) as \"renameCount\", cast(sum(CASEWHEN(DIRECTION = 'UPLOAD',TRANSFER_SIZE,0)) as bigint) as \"uploadBytes\", cast(sum(CASEWHEN(DIRECTION = 'DOWNLOAD', TRANSFER_SIZE,0)) as bigint) as \"downloadBytes\"\r" + " FROM SESSIONS s\r" + " left join TRANSFERS t\r on s.RID = t.SESSION_RID\r" + " where s.START_TIME >= ? and s.END_TIME <= ?\r" + " /*START_USERNAMES*/and USER_NAME in (%usernames%)/*END_USERNAMES*/\r" + " and s.SUCCESS_LOGIN = 'true'\r" + " group by USER_NAME, IP, s.START_TIME, s.END_TIME, s.RID, s.SESSION, s.SERVER_GROUP\r" + " order by USER_NAME\r";
            sql = ReportTools.fixMsSql(sql);
            String tmp = crushftp.handlers.Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/AccountActivitySummary.sql");
            if (tmp != null) {
                sql = tmp;
            } else {
                crushftp.handlers.Common.setFileText(sql, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/AccountActivitySummary.sql");
            }
            sql = crushftp.handlers.Common.replace_str(sql, "{fn TIMESTAMPDIFF(SQL_TSI_SECOND,s.end_time,s.start_time)} * 1000", "{fn TIMESTAMPDIFF(SQL_TSI_SECOND,s.START_TIME,s.END_TIME)} * 1000");
            sql = crushftp.handlers.Common.replace_str(sql, "{fn TIMESTAMPDIFF(SQL_TSI_SECOND,s.END_TIME,s.START_TIME)} * 1000", "{fn TIMESTAMPDIFF(SQL_TSI_SECOND,s.START_TIME,s.END_TIME)} * 1000");
            sql = ReportTools.fixSqlUsernames(sql, usernames);
            sql = crushftp.handlers.Common.replace_str(sql, datediff2, datediff);
            sql = crushftp.handlers.Common.replace_str(sql, datediff2U, datediff);
            Vector summary = ServerStatus.thisObj.statTools.executeSqlQuery_mem(sql, new Object[]{mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00"))}, false, params);
            Vector<Properties> jobs = new Vector<Properties>();
            Properties results = new Properties();
            int x = 0;
            while (x < summary.size()) {
                Properties p = (Properties)summary.elementAt(x);
                if (p.getProperty("server_port", "").trim().toLowerCase().equals("job")) {
                    jobs.add(p);
                } else {
                    long secs = (long)Float.parseFloat(p.getProperty("durationRaw", "0")) / 1000L;
                    if (secs < 0L) {
                        secs = 0L;
                        p.put("end", p.getProperty("start"));
                    }
                    results.put("downloadCount", String.valueOf(Long.parseLong(results.getProperty("downloadCount", "0")) + Long.parseLong(p.getProperty("downloadCount", "0"))));
                    results.put("uploadCount", String.valueOf(Long.parseLong(results.getProperty("uploadCount", "0")) + Long.parseLong(p.getProperty("uploadCount", "0"))));
                    results.put("deleteCount", String.valueOf(Long.parseLong(results.getProperty("deleteCount", "0")) + Long.parseLong(p.getProperty("deleteCount", "0"))));
                    results.put("renameCount", String.valueOf(Long.parseLong(results.getProperty("renameCount", "0")) + Long.parseLong(p.getProperty("renameCount", "0"))));
                    results.put("downloadBytes", String.valueOf((long)(Float.parseFloat(results.getProperty("downloadBytes", "0")) + (float)((long)Float.parseFloat(p.getProperty("downloadBytes", "0"))))));
                    results.put("uploadBytes", String.valueOf((long)(Float.parseFloat(results.getProperty("uploadBytes", "0")) + (float)((long)Float.parseFloat(p.getProperty("uploadBytes", "0"))))));
                    results.put("durationRaw", String.valueOf((long)(Float.parseFloat(results.getProperty("durationRaw", "0")) + (float)((long)Float.parseFloat(p.getProperty("durationRaw", "0"))))));
                    p.put("duration", String.valueOf(Common.format_time_pretty(secs)));
                    if (p.getProperty("protocol", "").contains(":")) {
                        String[] info = p.getProperty("protocol", "").split(":");
                        if (info.length != 0) {
                            p.put("protocol", info[0]);
                        }
                    } else {
                        p.put("protocol", "");
                    }
                    if (params.getProperty("reverseDNS", "").equals("true")) {
                        try {
                            String key = p.getProperty("ip");
                            if (lookupCache.getProperty(p.getProperty("ip"), "").equals("")) {
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
                }
                ++x;
            }
            if (jobs.size() > 0) {
                summary.removeAll(jobs);
            }
            results.put("duration", String.valueOf(Common.format_time_pretty(Long.parseLong(results.getProperty("durationRaw", "0")) / 1000L)));
            results.put("userCount", String.valueOf(summary.size()));
            results.put("summary", summary);
            results.put("reverseDNS", params.getProperty("reverseDNS"));
            crushftp.handlers.Common common_code = new crushftp.handlers.Common();
            results.put("export", params.getProperty("export", ""));
            results.put("params", crushftp.handlers.Common.removeNonStrings(params).toString());
            results.put("paramsObj", crushftp.handlers.Common.removeNonStrings(params));
            if (summary.size() > 0) {
                status.put("report_empty", "false");
            }
            sb.append(crushftp.handlers.Common.getXMLString(results, "results", "WebInterface/Reports/AccountActivitySummary.xsl"));
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

