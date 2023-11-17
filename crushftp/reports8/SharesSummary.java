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
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

public class SharesSummary {
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
            String share_sql = "SELECT s.USER_NAME as \"username\", t.START_TIME as \"share_time\", t.SESSION_RID as \"session_rid\", t.RID as \"transfer_rid\", PATH as \"path\", FILE_NAME as \"name\", URL as \"url\", cast(TRANSFER_SIZE as bigint) as \"size\"\r FROM TRANSFERS t, SESSIONS s\r where s.RID = t.SESSION_RID\r and DIRECTION = ?\r and t.START_TIME >= ? and t.START_TIME <= ?\r /*START_USERNAMES*/and USER_NAME in (%usernames%)/*END_USERNAMES*/\r order by 1 desc\r";
            String tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/SharesSummary.sql");
            if (tmp != null) {
                share_sql = tmp;
            } else {
                Common.setFileText(share_sql, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/SharesSummary.sql");
            }
            share_sql = ReportTools.fixSqlUsernames(share_sql, usernames);
            Vector shares = ServerStatus.thisObj.statTools.executeSqlQuery_mem(share_sql, new Object[]{"SHARE", mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00"))}, false, params);
            String meta_sql = "SELECT SESSION_RID as \"session_rid\", TRANSFER_RID as \"transfer_rid\", ITEM_KEY as \"item_key\", ITEM_VALUE as \"item_value\"\r FROM META_INFO where SESSION_RID in (%session_rids%)\r order by SESSION_RID, TRANSFER_RID, RID";
            tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/SharesSummary_meta.sql");
            if (tmp != null) {
                meta_sql = tmp;
            } else {
                Common.setFileText(meta_sql, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/SharesSummary_meta.sql");
            }
            String session_rids = "";
            int x = 0;
            while (x < shares.size()) {
                Properties p = (Properties)shares.elementAt(x);
                session_rids = String.valueOf(session_rids) + p.getProperty("session_rid") + ",";
                ++x;
            }
            if (session_rids.length() > 0) {
                session_rids = session_rids.substring(0, session_rids.length() - 1);
            }
            meta_sql = Common.replace_str(meta_sql, "%session_rids%", session_rids);
            if (!session_rids.trim().equals("")) {
                try (DVector metas = ServerStatus.thisObj.statTools.executeSqlQuery(meta_sql, new Object[0], false, params);){
                    int x2 = 0;
                    while (x2 < shares.size()) {
                        Properties share = (Properties)shares.elementAt(x2);
                        if (share.containsKey("url")) {
                            share.put("url", new VRL(share.getProperty("url")).safe());
                        }
                        int xx = 0;
                        while (xx < metas.size()) {
                            Properties pp = (Properties)metas.elementAt(xx);
                            if (pp.getProperty("session_rid").equals(share.getProperty("session_rid")) && pp.getProperty("transfer_rid").equals(share.getProperty("transfer_rid"))) {
                                Properties metaInfo = (Properties)share.get("metaInfo");
                                if (metaInfo == null) {
                                    metaInfo = new Properties();
                                    share.put("metaInfo", metaInfo);
                                }
                                metaInfo.put(pp.getProperty("item_key"), pp.getProperty("item_value"));
                                if (!share.containsKey(pp.getProperty("item_key"))) {
                                    share.put(pp.getProperty("item_key"), pp.getProperty("item_value"));
                                }
                            }
                            ++xx;
                        }
                        ++x2;
                    }
                }
            }
            Properties results = new Properties();
            results.put("shares", shares);
            results.put("showPaths", params.getProperty("showPaths"));
            results.put("showURLs", params.getProperty("showURLs"));
            results.put("showFormInfo", params.getProperty("showFormInfo"));
            Common common_code = new Common();
            results.put("export", params.getProperty("export", ""));
            results.put("params", Common.removeNonStrings(params).toString());
            results.put("paramsObj", Common.removeNonStrings(params));
            if (shares.size() > 0) {
                status.put("report_empty", "false");
            }
            sb.append(Common.getXMLString(results, "results", "WebInterface/Reports/SharesSummary.xsl"));
        }
        catch (Exception e) {
            Log.log("REPORT", 1, e);
        }
    }
}

