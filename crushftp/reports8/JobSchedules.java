/*
 * Decompiled with CFR 0.152.
 */
package crushftp.reports8;

import com.crushftp.client.File_S;
import crushftp.handlers.Common;
import crushftp.handlers.JobFilesHandler;
import crushftp.handlers.JobScheduler;
import crushftp.handlers.Log;
import crushftp.server.ServerStatus;
import crushftp.server.daemon.ServerBeat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

public class JobSchedules {
    Properties server_info = null;
    Properties server_settings = null;

    public void init(Properties server_settings, Properties server_info) {
        this.server_settings = server_settings;
        this.server_info = server_info;
    }

    public void generate(Properties stats, Properties params, StringBuffer sb, Properties status) {
        try {
            Common.setupReportDates(params, params.getProperty("show", ""), params.getProperty("startDate"), params.getProperty("endDate"));
            sorter cd1 = new sorter();
            cd1.setObj(new Properties(), "scheduleName");
            Vector jobs2 = new Vector();
            Vector jobs = JobScheduler.getJobList(false);
            if (!ServerBeat.current_master) {
                return;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
            int x = 0;
            while (x < jobs.size()) {
                File_S job = (File_S)jobs.elementAt(x);
                Properties p = (Properties)JobFilesHandler.readXMLObject(String.valueOf(job.getPath()) + "/job.XML");
                if (p != null) {
                    long nextRun = Long.parseLong(p.getProperty("nextRun", "0"));
                    Properties p2 = new Properties();
                    if (p.getProperty("scheduleType", "").equals("false")) {
                        p.put("scheduleType", "manually");
                    }
                    jobs2.addElement(p2);
                    p2.put("scheduleName", p.getProperty("scheduleName", ""));
                    p2.put("scheduleNote", p.getProperty("scheduleNote", ""));
                    p2.put("scheduleType", p.getProperty("scheduleType", ""));
                    if (!p.getProperty("scheduleType", "").equals("manually") && !p.getProperty("scheduleType", "").equals("false")) {
                        String details = "";
                        if (p.getProperty("scheduleType", "").equals("minutely")) {
                            details = String.valueOf(p.getProperty("minutelyAmount", "1")) + " minutes";
                        }
                        if (p.getProperty("scheduleType", "").equals("weekly")) {
                            if (p.getProperty("weekDays", "").indexOf("(1)") >= 0) {
                                details = String.valueOf(details) + ",Sun";
                            }
                            if (p.getProperty("weekDays", "").indexOf("(2)") >= 0) {
                                details = String.valueOf(details) + ",Mon";
                            }
                            if (p.getProperty("weekDays", "").indexOf("(3)") >= 0) {
                                details = String.valueOf(details) + ",Tue";
                            }
                            if (p.getProperty("weekDays", "").indexOf("(4)") >= 0) {
                                details = String.valueOf(details) + ",Wed";
                            }
                            if (p.getProperty("weekDays", "").indexOf("(5)") >= 0) {
                                details = String.valueOf(details) + ",Thu";
                            }
                            if (p.getProperty("weekDays", "").indexOf("(6)") >= 0) {
                                details = String.valueOf(details) + ",Fri";
                            }
                            if (p.getProperty("weekDays", "").indexOf("(7)") >= 0) {
                                details = String.valueOf(details) + ",Sat";
                            }
                            if (details.startsWith(",")) {
                                details = details.substring(1);
                            }
                            details = String.valueOf(p.getProperty("weeklyAmount", "1")) + " weeks:" + details;
                        } else if (p.getProperty("scheduleType", "").equals("daily")) {
                            details = String.valueOf(p.getProperty("dailyAmount", "1")) + " days";
                        } else if (p.getProperty("scheduleType", "").equals("monthly")) {
                            details = String.valueOf(p.getProperty("monthlyAmount", "1")) + " months";
                        }
                        if (!p.getProperty("scheduleType", "").equals("minutely")) {
                            details = String.valueOf(details) + "@" + p.getProperty("scheduleTime", "");
                        }
                        if (p.getProperty("scheduleType", "").equals("monthly")) {
                            details = String.valueOf(details) + " on day:" + Common.replace_str(p.getProperty("monthDays", ""), ")(", ",").replaceAll("\\(", "").replaceAll("\\)", "");
                        }
                        p2.put("scheduleType", details);
                    }
                    p2.put("jobEnabled", p.getProperty("enabled", "false"));
                    if (nextRun == 0L) {
                        p2.put("nextRun", "disabled");
                    } else {
                        p2.put("nextRun", sdf.format(new Date(nextRun)));
                    }
                    if (p2.getProperty("scheduleType").equals("manually")) {
                        p2.put("nextRun", "disabled");
                    }
                }
                ++x;
            }
            jobs2 = this.doSort(jobs2, cd1);
            Properties results = new Properties();
            results.put("jobs", jobs2);
            results.put("export", params.getProperty("export", ""));
            results.put("params", Common.removeNonStrings(params).toString());
            results.put("paramsObj", Common.removeNonStrings(params));
            status.put("report_empty", "false");
            Common cfr_ignored_0 = ServerStatus.thisObj.common_code;
            sb.append(Common.getXMLString(results, "results", "WebInterface/Reports/JobSchedules.xsl"));
        }
        catch (Exception e) {
            Log.log("REPORT", 1, e);
        }
    }

    public Vector doSort(Vector v, sorter c) {
        Object[] objs = v.toArray();
        Arrays.sort(objs, c);
        v.removeAllElements();
        int x = 0;
        while (x < objs.length) {
            v.addElement(objs[x]);
            ++x;
        }
        return v;
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

