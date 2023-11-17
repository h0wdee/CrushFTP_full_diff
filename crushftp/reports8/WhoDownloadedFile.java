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
import java.util.Comparator;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

public class WhoDownloadedFile {
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
            String sql = "SELECT USER_NAME as \"username\"\r FROM SESSIONS\r where START_TIME >= ? and END_TIME <= ?\r /*START_USERNAMES*/and USER_NAME in (%usernames%)/*END_USERNAMES*/\r group by USER_NAME\r order by USER_NAME\r";
            String tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/WhoDownloadedFile.sql");
            if (tmp != null) {
                sql = tmp;
            } else {
                Common.setFileText(sql, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/WhoDownloadedFile.sql");
            }
            sql = ReportTools.fixSqlUsernames(sql, usernames);
            try (DVector users = ServerStatus.thisObj.statTools.executeSqlQuery(sql, new Object[]{mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00"))}, false, params);){
                String sql_files = "SELECT USER_NAME as \"username\", URL as \"url\", PATH as \"path\", FILE_NAME as \"name\", t.START_TIME as \"date\"\r FROM TRANSFERS t, SESSIONS s\r where t.SESSION_RID = s.RID\r and s.START_TIME >= ? and s.END_TIME <= ?\r and t.DIRECTION = 'DOWNLOAD'\r and upper(PATH) like ?\r and upper(FILE_NAME) like ?\r /*START_USERNAMES*/and USER_NAME in (%usernames%)/*END_USERNAMES*/\r group by USER_NAME, URL, PATH, FILE_NAME, t.START_TIME\r order by USER_NAME, url, t.START_TIME\r";
                tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/WhoDownloadedFile_files.sql");
                if (tmp != null) {
                    sql_files = tmp;
                } else {
                    Common.setFileText(sql_files, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/WhoDownloadedFile_files.sql");
                }
                sql_files = ReportTools.fixSqlUsernames(sql_files, usernames);
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
                DVector allFiles = ServerStatus.thisObj.statTools.executeSqlQuery(sql_files, new Object[]{mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00")), path, filename}, false, params);
                Vector<Properties> users_with_activity = new Vector<Properties>();
                Properties users_with_activity_map = new Properties();
                Properties file_tracked = new Properties();
                try {
                    int x = 0;
                    while (x < users.size()) {
                        Properties p = (Properties)users.elementAt(x);
                        int xx = 0;
                        while (xx < allFiles.size()) {
                            Properties pp = (Properties)allFiles.elementAt(xx);
                            if (pp.getProperty("username", "").equalsIgnoreCase(p.getProperty("username", ""))) {
                                Vector<Properties> filesDetail;
                                pp.put("username", pp.getProperty("username").toLowerCase());
                                p.put("username", p.getProperty("username").toLowerCase());
                                if (!users_with_activity_map.containsKey(p.getProperty("username", "").toLowerCase())) {
                                    users_with_activity_map.put(p.getProperty("username", "").toLowerCase(), "");
                                    users_with_activity.addElement(p);
                                }
                                if ((filesDetail = (Vector<Properties>)p.get("filesDetail")) == null) {
                                    filesDetail = new Vector<Properties>();
                                }
                                p.put("filesDetail", filesDetail);
                                Vector<String> dates = new Vector<String>();
                                dates.addElement(pp.getProperty("date"));
                                pp.put("dates", dates);
                                int xxx = xx;
                                while (++xxx < allFiles.size()) {
                                    Properties temp = (Properties)allFiles.elementAt(xxx);
                                    if (!temp.getProperty("url").equals(pp.getProperty("url"))) break;
                                    dates.addElement(temp.getProperty("date"));
                                }
                                if (!file_tracked.containsKey(String.valueOf(pp.getProperty("username").toLowerCase()) + ":" + pp.getProperty("url"))) {
                                    file_tracked.put(String.valueOf(pp.getProperty("username").toLowerCase()) + ":" + pp.getProperty("url"), "");
                                    filesDetail.addElement(pp);
                                }
                            }
                            ++xx;
                        }
                        ++x;
                    }
                }
                finally {
                    allFiles.close();
                }
                Properties results = new Properties();
                results.put("users", users_with_activity);
                Common common_code = new Common();
                results.put("export", params.getProperty("export", ""));
                results.put("params", Common.removeNonStrings(params).toString());
                results.put("paramsObj", Common.removeNonStrings(params));
                if (users.size() > 0) {
                    status.put("report_empty", "false");
                }
                sb.append(Common.getXMLString(results, "results", "WebInterface/Reports/WhoDownloadedFile.xsl"));
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

