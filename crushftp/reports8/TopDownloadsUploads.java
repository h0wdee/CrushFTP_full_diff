/*
 * Decompiled with CFR 0.152.
 */
package crushftp.reports8;

import com.crushftp.tunnel2.DVector;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.reports8.ReportTools;
import crushftp.server.ServerStatus;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

public class TopDownloadsUploads {
    Properties server_info = null;
    Properties server_settings = null;

    public void init(Properties server_settings, Properties server_info) {
        this.server_settings = server_settings;
        this.server_info = server_info;
    }

    public void generate(Properties stats, Properties params, StringBuffer sb, Properties status) {
        try {
            Properties p;
            if (ServerStatus.BG("auto_fix_stats_sessions")) {
                ServerStatus.thisObj.statTools.executeSql("update SESSIONS set END_TIME = START_TIME where END_TIME < ? and START_TIME < ?", new Object[]{new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2000-01-01 00:00:00"), new Date(System.currentTimeMillis() - 86400000L)});
            }
            Common.setupReportDates(params, params.getProperty("show", ""), params.getProperty("startDate"), params.getProperty("endDate"));
            SimpleDateFormat mmddyyyy = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
            Vector usernames = (Vector)params.get("usernames");
            String download_sql = "SELECT count(t.RID) as \"count\", cast(avg(SPEED) as bigint) as \"averageSpeed\", PATH as \"path\", FILE_NAME as \"name\", URL as \"url\", cast(TRANSFER_SIZE as bigint) as \"size\"\r FROM TRANSFERS t, SESSIONS s\r where s.RID = t.SESSION_RID\r and DIRECTION = ?\r and t.START_TIME >= ? and t.START_TIME <= ?\r /*START_USERNAMES*/and USER_NAME in (%usernames%)/*END_USERNAMES*/\r group by PATH,FILE_NAME,URL,TRANSFER_SIZE\r order by 1 desc\r limit ?\r";
            String upload_sql = "SELECT t.SESSION_RID as \"session_rid\", t.RID as \"transfer_rid\", cast(avg(SPEED) as bigint) as \"averageSpeed\", PATH as \"path\", FILE_NAME as \"name\", URL as \"url\", cast(TRANSFER_SIZE as bigint) as \"size\"\r FROM TRANSFERS t, SESSIONS s\r where s.RID = t.SESSION_RID\r and DIRECTION = ?\r and t.START_TIME >= ? and t.START_TIME <= ?\r /*START_USERNAMES*/and USER_NAME in (%usernames%)/*END_USERNAMES*/\r group by t.SESSION_RID, t.RID, PATH,FILE_NAME,URL,TRANSFER_SIZE\r order by 1 desc\r limit ?\r";
            String tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/TopDownloadsUploads_download.sql");
            if (tmp != null) {
                download_sql = tmp;
            } else {
                Common.setFileText(download_sql, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/TopDownloadsUploads_download.sql");
            }
            tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/TopDownloadsUploads_upload.sql");
            if (tmp != null) {
                upload_sql = tmp;
            } else {
                Common.setFileText(upload_sql, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/TopDownloadsUploads_upload.sql");
            }
            download_sql = ReportTools.fixSqlUsernames(download_sql, usernames);
            upload_sql = ReportTools.fixSqlUsernames(upload_sql, usernames);
            if (ServerStatus.thisObj.statTools.mssql) {
                download_sql = "SELECT top " + Integer.parseInt(params.getProperty("downloadCount")) + download_sql.substring("SELECT".length(), download_sql.indexOf("limit ?"));
                upload_sql = "SELECT top " + Integer.parseInt(params.getProperty("uploadCount")) + upload_sql.substring("SELECT".length(), upload_sql.indexOf("limit ?"));
            }
            Vector downloads = null;
            Vector uploads = null;
            if (ServerStatus.thisObj.statTools.mssql) {
                downloads = ServerStatus.thisObj.statTools.executeSqlQuery_mem(download_sql, new Object[]{"DOWNLOAD", mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00"))}, false, params);
                uploads = ServerStatus.thisObj.statTools.executeSqlQuery_mem(upload_sql, new Object[]{"UPLOAD", mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00"))}, false, params);
            } else {
                downloads = ServerStatus.thisObj.statTools.executeSqlQuery_mem(download_sql, new Object[]{"DOWNLOAD", mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00")), Integer.valueOf(params.getProperty("downloadCount"))}, false, params);
                uploads = ServerStatus.thisObj.statTools.executeSqlQuery_mem(upload_sql, new Object[]{"UPLOAD", mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00")), Integer.valueOf(params.getProperty("uploadCount"))}, false, params);
            }
            try {
                int x = 0;
                while (x < downloads.size()) {
                    p = (Properties)downloads.elementAt(x);
                    p.put("path", Common.all_but_last(p.getProperty("path")));
                    ++x;
                }
            }
            catch (Exception e) {
                Log.log("REPORT", 0, e);
            }
            try {
                int x = 0;
                while (x < uploads.size()) {
                    p = (Properties)uploads.elementAt(x);
                    p.put("path", Common.all_but_last(p.getProperty("path")));
                    ++x;
                }
            }
            catch (Exception e) {
                Log.log("REPORT", 0, e);
            }
            String meta_sql = "SELECT SESSION_RID as \"session_rid\", TRANSFER_RID as \"transfer_rid\", ITEM_KEY as \"item_key\", ITEM_VALUE as \"item_value\"\r FROM META_INFO where SESSION_RID in (%session_rids%)\r order by SESSION_RID, TRANSFER_RID, RID";
            tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/TopDownloadsUploads_meta.sql");
            if (tmp != null) {
                meta_sql = tmp;
            } else {
                Common.setFileText(meta_sql, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/TopDownloadsUploads_meta.sql");
            }
            String session_rids = "";
            int x = 0;
            while (x < uploads.size()) {
                Properties p2 = (Properties)uploads.elementAt(x);
                session_rids = String.valueOf(session_rids) + p2.getProperty("session_rid") + ",";
                ++x;
            }
            if (session_rids.length() > 0) {
                session_rids = session_rids.substring(0, session_rids.length() - 1);
            }
            meta_sql = Common.replace_str(meta_sql, "%session_rids%", session_rids);
            if (!session_rids.trim().equals("")) {
                DVector metas = ServerStatus.thisObj.statTools.executeSqlQuery(meta_sql, new Object[0], false, params);
                int x2 = 0;
                while (x2 < uploads.size()) {
                    Properties p3 = (Properties)uploads.elementAt(x2);
                    int xx = 0;
                    while (xx < metas.size()) {
                        Properties pp = (Properties)metas.elementAt(xx);
                        if (pp.getProperty("session_rid").equals(p3.getProperty("session_rid")) && pp.getProperty("transfer_rid").equals(p3.getProperty("transfer_rid"))) {
                            Properties metaInfo = (Properties)p3.get("metaInfo");
                            if (metaInfo == null) {
                                metaInfo = new Properties();
                                p3.put("metaInfo", metaInfo);
                            }
                            metaInfo.put(pp.getProperty("item_key"), pp.getProperty("item_value"));
                        }
                        ++xx;
                    }
                    ++x2;
                }
                metas.close();
            }
            Properties results = new Properties();
            results.put("downloads", downloads);
            results.put("uploads", uploads);
            results.put("showPaths", params.getProperty("showPaths"));
            results.put("showURLs", params.getProperty("showURLs"));
            results.put("showFormInfo", params.getProperty("showFormInfo"));
            Common common_code = new Common();
            results.put("export", params.getProperty("export", ""));
            results.put("params", Common.removeNonStrings(params).toString());
            results.put("paramsObj", Common.removeNonStrings(params));
            if (downloads.size() > 0 || uploads.size() > 0) {
                status.put("report_empty", "false");
            }
            sb.append(Common.getXMLString(results, "results", "WebInterface/Reports/TopDownloadsUploads.xsl"));
        }
        catch (Exception e) {
            Log.log("REPORT", 1, e);
        }
    }
}

