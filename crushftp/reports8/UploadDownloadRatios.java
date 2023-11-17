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
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

public class UploadDownloadRatios {
    Properties server_info = null;
    Properties server_settings = null;

    public void init(Properties server_settings, Properties server_info) {
        this.server_settings = server_settings;
        this.server_info = server_info;
    }

    public void generate(Properties stats, Properties params, StringBuffer sb, Properties status) {
        try {
            if (ServerStatus.BG("auto_fix_stats_sessions")) {
                ServerStatus.thisObj.statTools.executeSql("update SESSIONS set END_TIME = START_TIME where END_TIME < ? and START_TIME < ?", new Object[]{new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2000-01-01 00:00:00"), new Date(System.currentTimeMillis() - 86400000L)});
            }
            Common.setupReportDates(params, params.getProperty("show", ""), params.getProperty("startDate"), params.getProperty("endDate"));
            SimpleDateFormat mmddyyyy = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
            Vector usernames = (Vector)params.get("usernames");
            String sql = "select USER_NAME as \"username\", sum(CASEWHEN(DIRECTION = 'UPLOAD',1,0)) as \"uploadCount\", sum(CASEWHEN(DIRECTION = 'DOWNLOAD',1,0)) as \"downloadCount\", cast(sum(CASEWHEN(DIRECTION = 'UPLOAD',TRANSFER_SIZE,0)) as bigint) as \"uploadBytes\", cast(sum(CASEWHEN(DIRECTION = 'DOWNLOAD', TRANSFER_SIZE,0)) as bigint) as \"downloadBytes\"\r FROM TRANSFERS t, SESSIONS s\r where s.RID = t.SESSION_RID\r and s.START_TIME >= ? and s.END_TIME <= ?\r /*START_USERNAMES*/and USER_NAME in (%usernames%)/*END_USERNAMES*/\r group by USER_NAME\r order by USER_NAME\r";
            sql = ReportTools.fixMsSql(sql);
            String tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/UploadDownloadRatios.sql");
            if (tmp != null) {
                sql = tmp;
            } else {
                Common.setFileText(sql, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/UploadDownloadRatios.sql");
            }
            sql = ReportTools.fixSqlUsernames(sql, usernames);
            Vector ratios = ServerStatus.thisObj.statTools.executeSqlQuery_mem(sql, new Object[]{mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00"))}, false, params);
            Properties results = new Properties();
            results.put("ratios", ratios);
            results.put("showBytes", params.getProperty("showBytes"));
            results.put("showCounts", params.getProperty("showCounts"));
            Common common_code = new Common();
            results.put("export", params.getProperty("export", ""));
            results.put("params", Common.removeNonStrings(params).toString());
            results.put("paramsObj", Common.removeNonStrings(params));
            if (ratios.size() > 0) {
                status.put("report_empty", "false");
            }
            sb.append(Common.getXMLString(results, "results", "WebInterface/Reports/UploadDownloadRatios.xsl"));
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

