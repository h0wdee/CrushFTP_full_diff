/*
 * Decompiled with CFR 0.152.
 */
package crushftp.reports8;

import com.crushftp.client.Common;
import com.crushftp.client.File_B;
import com.crushftp.client.File_S;
import com.crushftp.client.Variables;
import com.crushftp.client.Worker;
import crushftp.gui.LOC;
import crushftp.handlers.Log;
import crushftp.handlers.UserTools;
import crushftp.server.AdminControls;
import crushftp.server.ServerStatus;
import crushftp.server.daemon.ServerBeat;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

public class ReportTools {
    public String writeReport(String filename, String dir, Properties status, Properties config, Object reportItem, Properties server_settings, boolean export, String reportName, Properties params) {
        String s;
        block18: {
            s = "";
            String startDate = config.getProperty("startDate", "1/1/2000 00:00:00");
            String endDate = config.getProperty("endDate", "1/1/2100 00:00:00");
            if (startDate.indexOf("/") > 0 && startDate.indexOf(":") < 0) {
                config.setProperty("startDate", String.valueOf(startDate) + " 00:00:00");
            }
            if (endDate.indexOf("/") > 0 && endDate.indexOf(":") < 0) {
                config.setProperty("endDate", String.valueOf(endDate) + " 00:00:00");
            }
            config.put("log_date_format", ServerStatus.SG("log_date_format"));
            StringBuffer sb = new StringBuffer();
            Method generate = reportItem.getClass().getMethod("generate", new Properties().getClass(), new Properties().getClass(), new StringBuffer().getClass(), new Properties().getClass());
            Object[] objectArray = new Object[4];
            objectArray[1] = config;
            objectArray[2] = sb;
            objectArray[3] = status;
            generate.invoke(reportItem, objectArray);
            s = new String(sb.toString());
            if (status.getProperty("report_empty", "true").equals("true")) {
                status.put("report_text", s);
                s = "";
            }
            if (!status.getProperty("report_empty", "true").equals("true") || filename != null && filename.toUpperCase().indexOf("EMAIL") < 0) break block18;
            return "";
        }
        try {
            String ext = ".html";
            if (export && !s.equals("")) {
                s = s.substring("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".length() + 2);
                s = s.replaceAll("<html>", "");
                s = s.replaceAll("</html>", "");
                s = s.replaceAll("\\r", "");
                s = s.replaceAll("\\n", "");
                s = s.replaceAll("\\t", "");
                s = s.replaceAll("<br></br>", "\r");
            }
            if (export) {
                ext = ".csv";
            }
            new File_S(dir).mkdirs();
            if (filename == null) {
                filename = String.valueOf(reportName) + ext;
            }
            if (!filename.toUpperCase().endsWith(".CSV") && !filename.toUpperCase().endsWith(".HTML")) {
                filename = String.valueOf(filename) + ext;
            }
            new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "SavedReports/").mkdirs();
            if (!export) {
                String report_token = String.valueOf(reportName) + "_" + crushftp.handlers.Common.makeBoundary(8) + ".XML";
                Properties request = new Properties();
                request.put("report_token", report_token);
                request.put("s", s);
                String html_template = "";
                if (ServerStatus.BG("send_report_link")) {
                    AdminControls.saveReport(request, "(CONNECT)", true);
                    html_template = "<html><body><script>document.location.href='" + ServerStatus.SG("miniURLHost") + ServerStatus.SG("report_prefix") + "WebInterface/CrushReports/index.html?parameters=reportName=" + reportName + "&report_token=" + report_token + "';</script></body></html>";
                } else {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    crushftp.handlers.Common.streamCopier(new FileInputStream(new File_S(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/CrushReports/standalone_report.html")), baos, false, true, true);
                    html_template = new String(baos.toByteArray(), "UTF8");
                    int xml_loc = html_template.indexOf("//REPORT_XML");
                    if (status.getProperty("report_empty", "true").equals("true")) {
                        s = status.getProperty("report_text", "");
                    }
                    html_template = String.valueOf(html_template.substring(0, xml_loc)) + s + html_template.substring(xml_loc + "//REPORT_XML".length());
                }
                RandomAccessFile out = new RandomAccessFile(new File_S(String.valueOf(dir) + filename), "rw");
                out.setLength(0L);
                out.write(html_template.getBytes("UTF8"));
                out.close();
                crushftp.handlers.Common.updateOSXInfo(String.valueOf(dir) + filename);
            } else {
                if (status.getProperty("report_empty", "true").equals("true")) {
                    s = status.getProperty("report_text", "");
                }
                RandomAccessFile out = new RandomAccessFile(new File_S(String.valueOf(dir) + filename), "rw");
                out.setLength(0L);
                out.write(s.getBytes("UTF8"));
                out.close();
                crushftp.handlers.Common.updateOSXInfo(String.valueOf(dir) + filename);
            }
            if (params.getProperty("emailReport", "").equals("true")) {
                File_B[] attachments = new File_B[]{new File_B(String.valueOf(dir) + filename)};
                String emailResult = Common.send_mail(server_settings.getProperty("discovered_ip", "0.0.0.0"), this.replaceVars(params.getProperty("to", ""), params, config), this.replaceVars(params.getProperty("cc", ""), params, config), this.replaceVars(params.getProperty("bcc", ""), params, config), this.replaceVars(params.getProperty("from", ""), params, config), this.replaceVars(params.getProperty("subject", ""), params, config), this.replaceVars(params.getProperty("body", ""), params, config), server_settings.getProperty("smtp_server", ""), server_settings.getProperty("smtp_user", ""), server_settings.getProperty("smtp_pass", ""), server_settings.getProperty("smtp_ssl", "").equals("true"), server_settings.getProperty("smtp_report_html", "").equals("true"), attachments);
                if (emailResult.toUpperCase().indexOf("SUCCESS") < 0) {
                    Log.log("REPORT", 0, String.valueOf(LOC.G("FAILURE:")) + " " + emailResult + "\r\n");
                    Properties m = new Properties();
                    m.put("result", emailResult);
                    m.put("body", this.replaceVars(params.getProperty("body", ""), params, config));
                    m.put("subject", this.replaceVars(params.getProperty("subject", ""), params, config));
                    m.put("to", this.replaceVars(params.getProperty("to", ""), params, config));
                    m.put("from", this.replaceVars(params.getProperty("from", ""), params, config));
                    m.put("cc", this.replaceVars(params.getProperty("cc", ""), params, config));
                    m.put("bcc", this.replaceVars(params.getProperty("bcc", ""), params, config));
                    ServerStatus.thisObj.runAlerts("invalid_email", m, null, null);
                }
            }
        }
        catch (Exception ee) {
            Log.log("REPORT", 2, ee);
        }
        return s;
    }

    public static void skipEmail(String reportName, Properties params) {
        if (reportName.equalsIgnoreCase("NewFiles")) {
            params.put("from", "REPORT_TEST_" + params.getProperty("from"));
            params.put("to", "REPORT_TEST_" + params.getProperty("to"));
        }
    }

    public static void unSkipEmail(String reportName, Properties params) {
        if (reportName.equalsIgnoreCase("NewFiles") && params.getProperty("from").startsWith("REPORT_TEST_")) {
            params.put("from", params.getProperty("from").substring("REPORT_TEST_".length()));
            params.put("to", params.getProperty("to").substring("REPORT_TEST_".length()));
        }
    }

    public Object getReportItem(String reportName, Properties server_settings) {
        try {
            Class<?> c = ServerStatus.clasLoader.loadClass("crushftp.reports8." + reportName);
            Constructor<?> cons = c.getConstructor(null);
            Object o = cons.newInstance(null);
            Method generate = o.getClass().getMethod("init", new Properties().getClass(), new Properties().getClass());
            generate.invoke(o, server_settings, ServerStatus.thisObj.server_info);
            return o;
        }
        catch (Exception ee) {
            Log.log("REPORT", 2, ee);
            return null;
        }
    }

    public void runScheduledReports(Properties server_settings, Properties server_info) {
        if (!ServerBeat.current_master) {
            if (ServerStatus.BG("single_report_scheduler_serverbeat")) {
                return;
            }
        }
        if (System.getProperty("crushftp.singleuser", "false").equals("true")) {
            return;
        }
        boolean ranAReport = false;
        SimpleDateFormat time = new SimpleDateFormat("hh:mm aa", Locale.US);
        if (server_settings.get("reportSchedules") == null) {
            return;
        }
        Vector reportSchedules = (Vector)server_settings.get("reportSchedules");
        int x = 0;
        while (x < reportSchedules.size()) {
            boolean runReport = false;
            try {
                Properties p = (Properties)reportSchedules.elementAt(x);
                if (p.getProperty("nextRun", "123").equals("")) {
                    p.put("nextRun", "0");
                }
                long nextRun = Long.parseLong(p.getProperty("nextRun", "0"));
                long newNextRun = new Date().getTime();
                if (p.getProperty("reportType", "").equals("minutely") && new Date().getTime() > nextRun) {
                    runReport = true;
                    GregorianCalendar c = new GregorianCalendar();
                    c.setTime(new Date());
                    ((Calendar)c).add(12, Integer.parseInt(p.getProperty("minutelyAmount")));
                    newNextRun = c.getTime().getTime();
                } else if (time.format(new Date()).equals(time.format(time.parse(p.getProperty("reportTime")))) && new Date().getTime() > nextRun) {
                    if (p.getProperty("reportType", "").equals("daily")) {
                        while (time.format(new Date(newNextRun)).equals(time.format(new Date()))) {
                            newNextRun += 1000L;
                        }
                        runReport = true;
                    } else if (p.getProperty("reportType", "").equals("weekly")) {
                        String day = "";
                        String today_date = new SimpleDateFormat("EEE", Locale.US).format(new Date()).toUpperCase();
                        if (today_date.equals("SUN")) {
                            day = "(1)";
                        }
                        if (today_date.equals("MON")) {
                            day = "(2)";
                        }
                        if (today_date.equals("TUE")) {
                            day = "(3)";
                        }
                        if (today_date.equals("WED")) {
                            day = "(4)";
                        }
                        if (today_date.equals("THU")) {
                            day = "(5)";
                        }
                        if (today_date.equals("FRI")) {
                            day = "(6)";
                        }
                        if (today_date.equals("SAT")) {
                            day = "(7)";
                        }
                        if (p.getProperty("weekDays", "").indexOf(day) >= 0) {
                            if (p.getProperty("weekDays", "").indexOf(day) == p.getProperty("weekDays", "").length() - day.length()) {
                                GregorianCalendar c = new GregorianCalendar();
                                c.setTime(new Date());
                                while (c.get(7) != 1) {
                                    ((Calendar)c).add(5, -1);
                                }
                                ((Calendar)c).add(5, 7 * Integer.parseInt(p.getProperty("weeklyAmount")));
                                ((Calendar)c).add(10, -1);
                                newNextRun = c.getTime().getTime();
                            }
                            runReport = true;
                        }
                    } else if (p.getProperty("reportType", "").equals("monthly")) {
                        SimpleDateFormat d = new SimpleDateFormat("d", Locale.US);
                        SimpleDateFormat dd = new SimpleDateFormat("dd", Locale.US);
                        String day1 = "(" + d.format(new Date()) + ")";
                        String day2 = "(" + dd.format(new Date()) + ")";
                        if (p.getProperty("monthDays", "").indexOf(day1) >= 0 || p.getProperty("monthDays", "").indexOf(day2) >= 0) {
                            if (p.getProperty("monthDays", "").indexOf(day1) == p.getProperty("monthDays", "").length() - day1.length() || p.getProperty("monthDays", "").indexOf(day2) == p.getProperty("monthDays", "").length() - day2.length()) {
                                GregorianCalendar c = new GregorianCalendar();
                                c.setTime(new Date());
                                while (c.get(5) != 1) {
                                    ((Calendar)c).add(5, -1);
                                }
                                ((Calendar)c).add(2, Integer.parseInt(p.getProperty("monthlyAmount")));
                                ((Calendar)c).add(10, -1);
                                newNextRun = c.getTime().getTime();
                            }
                            runReport = true;
                        }
                    }
                }
                if (runReport || nextRun == -1L) {
                    p.put("nextRun", String.valueOf(newNextRun));
                    ranAReport = true;
                    Enumeration<Object> keys = p.keys();
                    Properties config = (Properties)p.get("config");
                    while (keys.hasMoreElements()) {
                        String key = keys.nextElement().toString();
                        if (!(p.get(key) instanceof String) || key.startsWith("schedule_")) continue;
                        config.put("schedule_" + key, p.get(key));
                    }
                    if (!p.getProperty("report_enabled", "true").equals("false")) {
                        class Runner
                        implements Runnable {
                            Properties server_settings;
                            Properties server_info;
                            Properties params;

                            public Runner(Properties server_settings, Properties server_info, Properties params) {
                                this.server_settings = server_settings;
                                this.server_info = server_info;
                                this.params = params;
                            }

                            @Override
                            public void run() {
                                Properties config = (Properties)this.params.get("config");
                                config = (Properties)config.clone();
                                Vector v = new Vector((Vector)config.get("usernames"));
                                config.put("usernames", v);
                                config.put("server_settings", this.server_settings);
                                config.put("server_info", this.server_info);
                                Properties status = new Properties();
                                String dir = this.params.getProperty("reportFolder");
                                String filename = this.params.getProperty("reportFilename");
                                filename = ReportTools.this.replaceVars(filename, this.params, config);
                                if (dir.indexOf(LOC.G("pick the folder")) >= 0 || dir.length() == 0 || filename.length() == 0) {
                                    return;
                                }
                                if (!dir.endsWith("/")) {
                                    dir = String.valueOf(dir) + "/";
                                }
                                if (this.params.getProperty("reportOverwrite").equals("false") && new File_S(String.valueOf(dir) + filename).exists()) {
                                    return;
                                }
                                config.put("export", this.params.getProperty("export", ""));
                                if (config.get("usernames") == null) {
                                    config.put("usernames", new Vector());
                                }
                                ReportTools.this.writeReport(filename, dir, status, config, ReportTools.this.getReportItem(config.getProperty("reportName"), this.server_settings), this.server_settings, this.params.getProperty("export", "").equals("true"), config.getProperty("reportName"), this.params);
                            }
                        }
                        Worker.startWorker(new Runner(server_settings, server_info, p), "report:" + p.getProperty("scheduleName") + ":" + new Date());
                        Log.log("REPORT", 0, "Ran Scheduled Report:" + p.getProperty("scheduleName") + ":" + new Date());
                        Log.log("REPORT", 1, "Report Config:" + p.toString());
                        try {
                            throw new Exception("Who called?");
                        }
                        catch (Exception e) {
                            Log.log("REPORT", 2, e);
                        }
                    } else {
                        Log.log("REPORT", 0, "Skipped Disabled Scheduled Report:" + p.getProperty("scheduleName") + ":" + new Date());
                    }
                }
            }
            catch (Exception e) {
                Log.log("REPORT", 1, e);
            }
            ++x;
        }
        if (ranAReport) {
            ServerStatus.thisObj.save_server_settings(false);
        }
    }

    public static String fixSqlUsernames(String sql, Vector usernames) {
        int xx;
        String userNamesStr = "";
        Properties groupsAll = new Properties();
        if (usernames.toString().indexOf("...") >= 0) {
            Vector the_list = (Vector)((Vector)ServerStatus.server_settings.get("server_list")).clone();
            xx = 0;
            while (xx < the_list.size()) {
                Properties server_item = (Properties)the_list.elementAt(xx);
                Properties groups = UserTools.getGroups(server_item.getProperty("linkedServer"));
                Enumeration<Object> keys = groups.keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement().toString();
                    groupsAll.put("..." + key.toUpperCase(), groups.get(key));
                }
                ++xx;
            }
        }
        boolean is_all_exclusion = true;
        xx = 0;
        while (xx < usernames.size()) {
            String username = usernames.elementAt(xx).toString().toUpperCase().trim();
            if (!username.startsWith("!")) {
                is_all_exclusion = false;
            } else {
                username = username.substring(1);
            }
            if (!username.equals("*")) {
                if (username.startsWith("...")) {
                    Vector usernames2 = (Vector)groupsAll.get(username);
                    int xxx = 0;
                    while (xxx < usernames2.size()) {
                        String username2 = usernames2.elementAt(xxx).toString().toUpperCase().trim();
                        userNamesStr = String.valueOf(userNamesStr) + "'" + username2.replace('\'', '_') + "',";
                        ++xxx;
                    }
                } else {
                    userNamesStr = String.valueOf(userNamesStr) + "'" + username.replace('\'', '_') + "',";
                }
            }
            ++xx;
        }
        if (userNamesStr.length() > 0 && sql.indexOf("%usernames%") >= 0) {
            userNamesStr = userNamesStr.substring(0, userNamesStr.length() - 1);
            sql = crushftp.handlers.Common.replace_str(sql, "%usernames%", userNamesStr);
            sql = crushftp.handlers.Common.replace_str(sql, "/*START_USERNAMES*/", "");
            sql = crushftp.handlers.Common.replace_str(sql, "/*END_USERNAMES*/", "");
            sql = crushftp.handlers.Common.replace_str(sql, "and USER_NAME in ", "and upper(USER_NAME) in ");
            sql = crushftp.handlers.Common.replace_str(sql, "and user_name in ", "and upper(USER_NAME) in ");
            if (is_all_exclusion) {
                sql = crushftp.handlers.Common.replace_str(sql, "and upper(USER_NAME) in ", "and upper(USER_NAME) not in ");
                sql = crushftp.handlers.Common.replace_str(sql, "and upper(user_name) in ", "and upper(USER_NAME) not in ");
            }
        } else if (sql.indexOf("/*START_USERNAMES*/") >= 0) {
            sql = String.valueOf(sql.substring(0, sql.indexOf("/*START_USERNAMES*/")).trim()) + " " + sql.substring(sql.indexOf("/*END_USERNAMES*/") + "/*END_USERNAMES*/".length()).trim();
        }
        if (ServerStatus.thisObj.statTools.mysql) {
            sql = crushftp.handlers.Common.replace_str(sql, "CASEWHEN", "IF");
        }
        return sql;
    }

    public static void fixListUsernames(Vector usernamesReal) {
        Vector usernames = (Vector)usernamesReal.clone();
        usernamesReal.removeAllElements();
        Properties groupsAll = new Properties();
        if (usernames.toString().indexOf("...") >= 0) {
            Vector the_list = (Vector)((Vector)ServerStatus.server_settings.get("server_list")).clone();
            int xx = 0;
            while (xx < the_list.size()) {
                Properties server_item = (Properties)the_list.elementAt(xx);
                Properties groups = UserTools.getGroups(server_item.getProperty("linkedServer"));
                Enumeration<Object> keys = groups.keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement().toString();
                    groupsAll.put("..." + key.toUpperCase(), groups.get(key));
                }
                ++xx;
            }
        }
        int xx = 0;
        while (xx < usernames.size()) {
            String username = usernames.elementAt(xx).toString().toUpperCase().trim();
            if (username.startsWith("...")) {
                Vector usernames2 = (Vector)groupsAll.get(username);
                int xxx = 0;
                while (xxx < usernames2.size()) {
                    usernamesReal.addElement(usernames2.elementAt(xxx).toString().toUpperCase().trim());
                    ++xxx;
                }
            } else {
                usernamesReal.addElement(username);
            }
            ++xx;
        }
    }

    public String replaceVars(String filename, Properties params, Properties config) {
        filename = crushftp.handlers.Common.replace_str(filename, "%name%", config.getProperty("reportName"));
        filename = crushftp.handlers.Common.replace_str(filename, "%schedule%", params.getProperty("scheduleName"));
        filename = crushftp.handlers.Common.replace_str(filename, "%mm%", new SimpleDateFormat("MM", Locale.US).format(new Date()));
        filename = crushftp.handlers.Common.replace_str(filename, "{name}", config.getProperty("reportName"));
        filename = crushftp.handlers.Common.replace_str(filename, "{schedule}", params.getProperty("scheduleName"));
        try {
            filename = crushftp.handlers.Common.replace_str(filename, "{hostname}", InetAddress.getLocalHost().getHostName());
        }
        catch (Exception e) {
            Log.log("REPORT", 2, e);
        }
        Variables vars = new Variables();
        vars.setDate(new Date());
        filename = vars.replace_vars_line_date(filename, config, "{", "}");
        filename = vars.replace_vars_line_date(filename, config, "%", "%");
        return filename;
    }

    public static String fixMsSql(String sql) {
        if (ServerStatus.thisObj.statTools.mssql) {
            sql = crushftp.handlers.Common.replace_str(sql, "CASEWHEN(direction = 'UPLOAD',1,0)", "CASE WHEN(DIRECTION = 'UPLOAD') then 1 else 0 end");
            sql = crushftp.handlers.Common.replace_str(sql, "CASEWHEN(direction = 'DOWNLOAD',1,0)", "CASE WHEN(DIRECTION = 'DOWNLOAD') then 1 else 0 end");
            sql = crushftp.handlers.Common.replace_str(sql, "CASEWHEN(direction = 'UPLOAD',transfer_size,0)", "CASE WHEN(DIRECTION = 'UPLOAD') then TRANSFER_SIZE else 0 end");
            sql = crushftp.handlers.Common.replace_str(sql, "CASEWHEN(direction = 'UPLOAD', transfer_size,0)", "CASE WHEN(DIRECTION = 'UPLOAD') then TRANSFER_SIZE else 0 end");
            sql = crushftp.handlers.Common.replace_str(sql, "CASEWHEN(direction = 'DOWNLOAD', transfer_size,0)", "CASE WHEN(DIRECTION = 'DOWNLOAD') then TRANSFER_SIZE else 0 end");
            sql = crushftp.handlers.Common.replace_str(sql, "CASEWHEN(direction = 'DOWNLOAD',transfer_size,0)", "CASE WHEN(DIRECTION = 'DOWNLOAD') then TRANSFER_SIZE else 0 end");
            sql = crushftp.handlers.Common.replace_str(sql, "CASEWHEN(direction = 'RENAME',1,0)", "CASE WHEN(DIRECTION = 'RENAME') then 1 else 0 end");
            sql = crushftp.handlers.Common.replace_str(sql, "CASEWHEN(direction = 'DELETE',1,0)", "CASE WHEN(DIRECTION = 'DELETE') then 1 else 0 end");
            sql = crushftp.handlers.Common.replace_str(sql, "CASEWHEN(direction = 'DELETE', TRANSFER_SIZE,0)", "CASE WHEN(DIRECTION = 'DELETE') then TRANSFER_SIZE else 0 end");
            sql = crushftp.handlers.Common.replace_str(sql, "CASEWHEN(DIRECTION = 'UPLOAD',1,0)", "CASE WHEN(DIRECTION = 'UPLOAD') then 1 else 0 end");
            sql = crushftp.handlers.Common.replace_str(sql, "CASEWHEN(DIRECTION = 'DOWNLOAD',1,0)", "CASE WHEN(DIRECTION = 'DOWNLOAD') then 1 else 0 end");
            sql = crushftp.handlers.Common.replace_str(sql, "CASEWHEN(DIRECTION = 'UPLOAD',TRANSFER_SIZE,0)", "CASE WHEN(DIRECTION = 'UPLOAD') then TRANSFER_SIZE else 0 end");
            sql = crushftp.handlers.Common.replace_str(sql, "CASEWHEN(DIRECTION = 'UPLOAD', TRANSFER_SIZE,0)", "CASE WHEN(DIRECTION = 'UPLOAD') then TRANSFER_SIZE else 0 end");
            sql = crushftp.handlers.Common.replace_str(sql, "CASEWHEN(DIRECTION = 'DOWNLOAD', TRANSFER_SIZE,0)", "CASE WHEN(DIRECTION = 'DOWNLOAD') then TRANSFER_SIZE else 0 end");
            sql = crushftp.handlers.Common.replace_str(sql, "CASEWHEN(DIRECTION = 'DOWNLOAD',TRANSFER_SIZE,0)", "CASE WHEN(DIRECTION = 'DOWNLOAD') then TRANSFER_SIZE else 0 end");
            sql = crushftp.handlers.Common.replace_str(sql, "CASEWHEN(DIRECTION = 'RENAME',1,0)", "CASE WHEN(DIRECTION = 'RENAME') then 1 else 0 end");
            sql = crushftp.handlers.Common.replace_str(sql, "CASEWHEN(DIRECTION = 'DELETE',1,0)", "CASE WHEN(DIRECTION = 'DELETE') then 1 else 0 end");
            sql = crushftp.handlers.Common.replace_str(sql, "CASEWHEN(DIRECTION = 'DELETE', TRANSFER_SIZE,0)", "CASE WHEN(DIRECTION = 'DELETE') then TRANSFER_SIZE else 0 end");
        }
        return sql;
    }
}

