/*
 * Decompiled with CFR 0.152.
 */
package crushftp.reports8;

import com.crushftp.client.VRL;
import com.crushftp.tunnel2.DVector;
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

public class UploadDownloadSummary {
    Properties server_info = null;
    Properties server_settings = null;
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.US);
    SimpleDateFormat sdfyy = new SimpleDateFormat("MM/dd/yy hh:mm:ss aa", Locale.US);

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
            String tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/UploadDownloadSummary_summary.sql");
            if (tmp != null) {
                sql = tmp;
            } else {
                Common.setFileText(sql, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/UploadDownloadSummary_summary.sql");
            }
            sql = ReportTools.fixSqlUsernames(sql, usernames);
            Vector summary = ServerStatus.thisObj.statTools.executeSqlQuery_mem(sql, new Object[]{mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/21 00 00:00:00"))}, false, params);
            String sql_transfers = "SELECT USER_NAME as \"username\", s.IP as \"ip\", t.START_TIME as \"date\", PATH as \"path\", FILE_NAME as \"name\", URL as \"url\", cast(TRANSFER_SIZE as bigint) as \"size\", cast(SPEED as bigint)*1024 as \"speed\"\r FROM TRANSFERS t, SESSIONS s\r where s.RID = t.SESSION_RID\r and DIRECTION = ?\r and t.START_TIME >= ? and t.START_TIME <= ?\r order by t.START_TIME\r";
            tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/UploadDownloadSummary_transfers.sql");
            if (tmp != null) {
                sql_transfers = tmp;
            } else {
                Common.setFileText(sql_transfers, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/UploadDownloadSummary_transfers.sql");
            }
            DVector allDownloads = new DVector();
            DVector allUploads = new DVector();
            try {
                if (params.getProperty("showDownloads").equals("true")) {
                    allDownloads = ServerStatus.thisObj.statTools.executeSqlQuery(sql_transfers, new Object[]{"DOWNLOAD", mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00"))}, false, params);
                }
                if (params.getProperty("showUploads").equals("true")) {
                    allUploads = ServerStatus.thisObj.statTools.executeSqlQuery(sql_transfers, new Object[]{"UPLOAD", mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00"))}, false, params);
                }
                Properties results = new Properties();
                int x = 0;
                while (x < summary.size()) {
                    Properties p = (Properties)summary.elementAt(x);
                    results.put("downloadCount", String.valueOf(Long.parseLong(results.getProperty("downloadCount", "0")) + Long.parseLong(p.getProperty("downloadCount", "0"))));
                    results.put("uploadCount", String.valueOf(Long.parseLong(results.getProperty("uploadCount", "0")) + Long.parseLong(p.getProperty("uploadCount", "0"))));
                    results.put("downloadBytes", String.valueOf((long)(Float.parseFloat(results.getProperty("downloadBytes", "0")) + (float)((long)Float.parseFloat(p.getProperty("downloadBytes", "0"))))));
                    results.put("uploadBytes", String.valueOf((long)(Float.parseFloat(results.getProperty("uploadBytes", "0")) + (float)((long)Float.parseFloat(p.getProperty("uploadBytes", "0"))))));
                    Vector<Properties> downloads = new Vector<Properties>();
                    int xx = 0;
                    while (xx < allDownloads.size()) {
                        Properties pp = (Properties)allDownloads.elementAt(xx);
                        if (pp.getProperty("username", "").equals(p.getProperty("username", ""))) {
                            downloads.addElement(VRL.safe(pp));
                        }
                        ++xx;
                    }
                    Vector<Properties> uploads = new Vector<Properties>();
                    int xx2 = 0;
                    while (xx2 < allUploads.size()) {
                        Properties pp = (Properties)allUploads.elementAt(xx2);
                        if (pp.getProperty("username", "").equals(p.getProperty("username", ""))) {
                            uploads.addElement(VRL.safe(pp));
                        }
                        ++xx2;
                    }
                    p.put("downloads", downloads);
                    p.put("uploads", uploads);
                    ++x;
                }
                results.put("userCount", String.valueOf(summary.size()));
                results.put("showUploads", params.getProperty("showUploads"));
                results.put("showDownloads", params.getProperty("showDownloads"));
                results.put("showFiles", params.getProperty("showFiles"));
                results.put("showPaths", params.getProperty("showPaths"));
                results.put("showIPs", params.getProperty("showIPs"));
                results.put("showDates", params.getProperty("showDates"));
                results.put("showSizes", params.getProperty("showSizes"));
                results.put("summary", summary);
                Common common_code = new Common();
                results.put("export", params.getProperty("export", ""));
                results.put("params", Common.removeNonStrings(params).toString());
                results.put("paramsObj", Common.removeNonStrings(params));
                if (summary.size() > 0) {
                    status.put("report_empty", "false");
                }
                sb.append(Common.getXMLString(results, "results", "WebInterface/Reports/UploadDownloadSummary.xsl"));
            }
            finally {
                allDownloads.close();
                allUploads.close();
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

