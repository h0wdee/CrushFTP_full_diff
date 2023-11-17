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

public class AuditSummary {
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
            String sql = "select USER_NAME as \"username\", sum(CASEWHEN(DIRECTION = 'UPLOAD',1,0)) as \"uploadCount\", sum(CASEWHEN(DIRECTION = 'DOWNLOAD',1,0)) as \"downloadCount\", sum(CASEWHEN(DIRECTION = 'DELETE',1,0)) as \"deleteCount\", sum(CASEWHEN(DIRECTION = 'RENAME',1,0)) as \"renameCount\", cast(sum(CASEWHEN(DIRECTION = 'UPLOAD',TRANSFER_SIZE,0)) as bigint) as \"uploadBytes\", cast(sum(CASEWHEN(DIRECTION = 'DOWNLOAD', TRANSFER_SIZE,0)) as bigint) as \"downloadBytes\", cast(sum(CASEWHEN(DIRECTION = 'DELETE', TRANSFER_SIZE,0)) as bigint) as \"deleteBytes\"\r FROM SESSIONS s\r left join TRANSFERS t\r on s.RID = t.SESSION_RID\r where s.START_TIME >= ? and s.END_TIME <= ?\r /*START_USERNAMES*/and USER_NAME in (%usernames%)/*END_USERNAMES*/\r group by USER_NAME\r order by USER_NAME\r";
            sql = ReportTools.fixMsSql(sql);
            String tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/AuditSummary_summary.sql");
            if (tmp != null) {
                sql = tmp;
            } else {
                Common.setFileText(sql, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/AuditSummary_summary.sql");
            }
            sql = ReportTools.fixSqlUsernames(sql, usernames);
            Vector summary = ServerStatus.thisObj.statTools.executeSqlQuery_mem(sql, new Object[]{mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00"))}, false, params);
            String sql_logins = "SELECT USER_NAME as \"username\", IP as \"ip\", START_TIME as \"date\", SUCCESS_LOGIN as \"success_login\"\r FROM SESSIONS s\r where USER_NAME = ? and START_TIME >= ? and START_TIME <= ? and SUCCESS_LOGIN = ?\r order by START_TIME\r";
            tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/AuditSummary_logins.sql");
            if (tmp != null && tmp.indexOf("success_login") < 0) {
                tmp = null;
            }
            if (tmp != null) {
                sql_logins = tmp;
            } else {
                Common.setFileText(sql_logins, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/AuditSummary_logins.sql");
            }
            String sql_transfers = "SELECT USER_NAME as \"username\", s.IP as \"ip\", s.SESSION as \"protocol\", s.SERVER_GROUP as \"server_port\", t.START_TIME as \"date\", PATH as \"path\", FILE_NAME as \"name\", URL as \"url\", cast(TRANSFER_SIZE as bigint) as \"size\", cast(SPEED as bigint)*1024 as \"speed\"\r FROM TRANSFERS t, SESSIONS s\r where s.RID = t.SESSION_RID\r and DIRECTION = ?\r and t.START_TIME >= ? and t.START_TIME <= ?\r and upper(PATH) like ?\r and upper(FILE_NAME) like ?\r order by t.START_TIME\r";
            tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/AuditSummary_transfers.sql");
            if (tmp != null) {
                sql_transfers = tmp;
            } else {
                Common.setFileText(sql_transfers, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/AuditSummary_transfers.sql");
            }
            if (sql_transfers.contains("upper(path)")) {
                Common.replace_str(sql_transfers, "upper(path)", "upper(PATH)");
            }
            if (sql_transfers.contains("upper(file_name)")) {
                Common.replace_str(sql_transfers, "upper(file_name)", "upper(FILE_NAME)");
            }
            if (!sql_transfers.contains("and upper(PATH) like ?\r")) {
                sql_transfers = String.valueOf(sql_transfers.substring(0, sql_transfers.indexOf(" order by"))) + " and upper(PATH) like ?\r" + sql_transfers.substring(sql_transfers.indexOf(" order by"), sql_transfers.length());
            }
            if (!sql_transfers.contains("and upper(FILE_NAME) like ?\r")) {
                sql_transfers = String.valueOf(sql_transfers.substring(0, sql_transfers.indexOf(" order by"))) + " and upper(FILE_NAME) like ?\r" + sql_transfers.substring(sql_transfers.indexOf(" order by"), sql_transfers.length());
            }
            String path = params.getProperty("path").toUpperCase();
            String filename = params.getProperty("filename").toUpperCase();
            String method = params.getProperty("searchFilename");
            if (method.equals("contains")) {
                filename = "%" + filename + "%";
            }
            if (method.equals("starts with")) {
                filename = filename + "%";
            }
            if (method.equals("ends with")) {
                filename = "%" + filename;
            }
            if (method.equals("equals")) {
                filename = filename;
            }
            if ((method = params.getProperty("searchPath")).equals("contains")) {
                path = "%" + path + "%";
            }
            if (method.equals("starts with")) {
                path = path + "%";
            }
            if (method.equals("ends with")) {
                path = "%" + path;
            }
            if (method.equals("equals")) {
                path = path;
            }
            DVector allDownloads = new DVector();
            DVector allUploads = new DVector();
            DVector allDeletes = new DVector();
            DVector allRenames = new DVector();
            try {
                if (params.getProperty("showDownloads").equals("true")) {
                    allDownloads = ServerStatus.thisObj.statTools.executeSqlQuery(sql_transfers, new Object[]{"DOWNLOAD", mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00")), path, filename}, false, params);
                }
                if (params.getProperty("showUploads").equals("true")) {
                    allUploads = ServerStatus.thisObj.statTools.executeSqlQuery(sql_transfers, new Object[]{"UPLOAD", mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00")), path, filename}, false, params);
                }
                if (params.getProperty("showDeletes").equals("true")) {
                    allDeletes = ServerStatus.thisObj.statTools.executeSqlQuery(sql_transfers, new Object[]{"DELETE", mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00")), path, filename}, false, params);
                }
                if (params.getProperty("showRenames").equals("true")) {
                    allRenames = ServerStatus.thisObj.statTools.executeSqlQuery(sql_transfers, new Object[]{"RENAME", mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00")), path, filename}, false, params);
                }
                Properties results = new Properties();
                int x = 0;
                while (x < summary.size()) {
                    Properties p = (Properties)summary.elementAt(x);
                    String protocol = "";
                    if (p.getProperty("protocol", "").contains(":")) {
                        String[] info = p.getProperty("protocol", "").split(":");
                        protocol = info[0];
                    }
                    String server_port = p.getProperty("server_port", "");
                    results.put("downloadCount", String.valueOf(Long.parseLong(results.getProperty("downloadCount", "0")) + Long.parseLong(p.getProperty("downloadCount", "0"))));
                    results.put("uploadCount", String.valueOf(Long.parseLong(results.getProperty("uploadCount", "0")) + Long.parseLong(p.getProperty("uploadCount", "0"))));
                    results.put("deleteCount", String.valueOf(Long.parseLong(results.getProperty("deleteCount", "0")) + Long.parseLong(p.getProperty("deleteCount", "0"))));
                    results.put("renameCount", String.valueOf(Long.parseLong(results.getProperty("renameCount", "0")) + Long.parseLong(p.getProperty("renameCount", "0"))));
                    results.put("downloadBytes", String.valueOf((long)(Float.parseFloat(results.getProperty("downloadBytes", "0")) + (float)((long)Float.parseFloat(p.getProperty("downloadBytes", "0"))))));
                    results.put("uploadBytes", String.valueOf((long)(Float.parseFloat(results.getProperty("uploadBytes", "0")) + (float)((long)Float.parseFloat(p.getProperty("uploadBytes", "0"))))));
                    results.put("deleteBytes", String.valueOf((long)(Float.parseFloat(results.getProperty("deleteBytes", "0")) + (float)((long)Float.parseFloat(p.getProperty("deleteBytes", "0"))))));
                    Vector<Properties> downloads = new Vector<Properties>();
                    int xx = 0;
                    while (xx < allDownloads.size()) {
                        Properties pp = (Properties)allDownloads.elementAt(xx);
                        pp.put("url", new VRL(pp.getProperty("url", "")).safe());
                        if (pp.getProperty("protocol", "").contains(":")) {
                            String[] info = pp.getProperty("protocol", "").split(":");
                            if (info.length != 0) {
                                pp.put("protocol", info[0]);
                            }
                        } else {
                            pp.put("protocol", protocol);
                        }
                        if (pp.getProperty("server_port", "").equals("") && !server_port.equals("")) {
                            pp.put("server_port", server_port);
                        }
                        if (pp.getProperty("username", "").equals(p.getProperty("username", ""))) {
                            downloads.addElement(pp);
                        }
                        ++xx;
                    }
                    Vector<Properties> deletes = new Vector<Properties>();
                    int xx2 = 0;
                    while (xx2 < allDeletes.size()) {
                        Properties pp = (Properties)allDeletes.elementAt(xx2);
                        pp.put("url", new VRL(pp.getProperty("url", "")).safe());
                        if (pp.getProperty("protocol", "").contains(":")) {
                            String[] info = pp.getProperty("protocol", "").split(":");
                            if (info.length != 0) {
                                pp.put("protocol", info[0]);
                            }
                        } else {
                            pp.put("protocol", protocol);
                        }
                        if (pp.getProperty("server_port", "").equals("") && !server_port.equals("")) {
                            pp.put("server_port", server_port);
                        }
                        if (pp.getProperty("username", "").equals(p.getProperty("username", ""))) {
                            deletes.addElement(pp);
                        }
                        ++xx2;
                    }
                    Vector<Properties> renames = new Vector<Properties>();
                    int xx3 = 0;
                    while (xx3 < allRenames.size()) {
                        String old_name;
                        Properties pp = (Properties)allRenames.elementAt(xx3);
                        String old_path = pp.getProperty("path").substring(0, pp.getProperty("path").indexOf(":"));
                        if (!old_path.endsWith(old_name = pp.getProperty("name").substring(0, pp.getProperty("name").indexOf(":")))) {
                            pp.put("path", String.valueOf(old_path) + old_name + pp.getProperty("path").substring(pp.getProperty("path").indexOf(":"), pp.getProperty("path").length()));
                        }
                        pp.put("url", new VRL(pp.getProperty("url", "")).safe());
                        if (pp.getProperty("protocol", "").contains(":")) {
                            String[] info = pp.getProperty("protocol", "").split(":");
                            if (info.length != 0) {
                                pp.put("protocol", info[0]);
                            }
                        } else {
                            pp.put("protocol", protocol);
                        }
                        if (pp.getProperty("server_port", "").equals("") && !server_port.equals("")) {
                            pp.put("server_port", server_port);
                        }
                        if (pp.getProperty("username", "").equals(p.getProperty("username", ""))) {
                            renames.addElement(pp);
                        }
                        ++xx3;
                    }
                    Vector<Properties> uploads = new Vector<Properties>();
                    int xx4 = 0;
                    while (xx4 < allUploads.size()) {
                        Properties pp = (Properties)allUploads.elementAt(xx4);
                        pp.put("url", new VRL(pp.getProperty("url", "")).safe());
                        if (pp.getProperty("protocol", "").contains(":")) {
                            String[] info = pp.getProperty("protocol", "").split(":");
                            if (info.length != 0) {
                                pp.put("protocol", info[0]);
                            }
                        } else {
                            pp.put("protocol", protocol);
                        }
                        if (pp.getProperty("server_port", "").equals("") && !server_port.equals("")) {
                            pp.put("server_port", server_port);
                        }
                        if (pp.getProperty("username", "").equals(p.getProperty("username", ""))) {
                            uploads.addElement(pp);
                        }
                        ++xx4;
                    }
                    p.put("downloads", downloads);
                    p.put("uploads", uploads);
                    p.put("deletes", deletes);
                    p.put("renames", renames);
                    Vector logins = new Vector();
                    if (params.getProperty("showLogins", "false").equals("true")) {
                        if (params.getProperty("successLogin", "Any").equalsIgnoreCase("Any")) {
                            Vector v = ServerStatus.thisObj.statTools.executeSqlQuery_mem(sql_logins, new Object[]{p.getProperty("username", ""), mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00")), "true"}, false, params);
                            logins.addAll(v);
                            v = ServerStatus.thisObj.statTools.executeSqlQuery_mem(sql_logins, new Object[]{p.getProperty("username", ""), mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00")), "false"}, false, params);
                            logins.addAll(v);
                        } else {
                            logins = ServerStatus.thisObj.statTools.executeSqlQuery_mem(sql_logins, new Object[]{p.getProperty("username", ""), mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00")), params.getProperty("successLogin", "true")}, false, params);
                        }
                        while (logins.size() > 1000) {
                            logins.remove(0);
                        }
                    }
                    p.put("logins", logins);
                    ++x;
                }
                results.put("userCount", String.valueOf(summary.size()));
                results.put("showUploads", params.getProperty("showUploads"));
                results.put("showDownloads", params.getProperty("showDownloads"));
                results.put("showDeletes", params.getProperty("showDeletes"));
                results.put("showRenames", params.getProperty("showRenames"));
                results.put("showLogins", params.getProperty("showLogins", "false"));
                results.put("showFiles", params.getProperty("showFiles"));
                results.put("showPaths", params.getProperty("showPaths"));
                results.put("showIPs", params.getProperty("showIPs"));
                results.put("showDates", params.getProperty("showDates"));
                results.put("showSizes", params.getProperty("showSizes"));
                results.put("successLogin", params.getProperty("successLogin", "Any"));
                results.put("summary", summary);
                Common common_code = new Common();
                results.put("export", params.getProperty("export", ""));
                results.put("params", Common.removeNonStrings(params).toString());
                results.put("paramsObj", Common.removeNonStrings(params));
                if (summary.size() > 0) {
                    status.put("report_empty", "false");
                }
                sb.append(Common.getXMLString(results, "results", "WebInterface/Reports/AuditSummary.xsl"));
            }
            finally {
                allDownloads.close();
                allUploads.close();
                allDeletes.close();
                allRenames.close();
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

