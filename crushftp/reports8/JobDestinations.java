/*
 * Decompiled with CFR 0.152.
 */
package crushftp.reports8;

import com.crushftp.client.File_S;
import com.crushftp.client.VRL;
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
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class JobDestinations {
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
                    p2.put("jobEnabled", p.getProperty("enabled", "false"));
                    if (nextRun == 0L) {
                        p2.put("nextRun", "disabled");
                    } else {
                        p2.put("nextRun", sdf.format(new Date(nextRun)));
                    }
                    if (p2.getProperty("scheduleType").equals("manually")) {
                        p2.put("nextRun", "disabled");
                    }
                    Vector<Properties> tasks2 = new Vector<Properties>();
                    Vector tasks = (Vector)p.get("tasks");
                    if (tasks != null) {
                        int xx = 0;
                        while (xx < tasks.size()) {
                            Properties t = (Properties)tasks.elementAt(xx);
                            Properties t2 = new Properties();
                            t2.putAll((Map<?, ?>)p2);
                            tasks2.addElement(t2);
                            JobFilesHandler.encryptDecryptRemoteLocations(t, false);
                            this.addItem("type", t, t2);
                            this.addItem("name", t, t2);
                            this.addItem("findFilter", t, t2);
                            this.addItem("depth", t, t2);
                            this.addItem("findUrl", t, t2);
                            this.addItem("filePath", t, t2);
                            this.addItem("destUrl", t, t2);
                            this.addItem("destPath", t, t2);
                            this.addItem("emailFrom", t, t2);
                            this.addItem("emailTo", t, t2);
                            ++xx;
                        }
                    }
                    p2.put("tasks", tasks2);
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
            sb.append(Common.getXMLString(results, "results", "WebInterface/Reports/JobDestinations.xsl"));
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

    public void addItem(String s, Properties t, Properties t2) {
        try {
            if (!t.getProperty(s, "").equals("")) {
                String val = t.getProperty(s);
                if (val.indexOf(":") >= 0) {
                    val = new VRL(val).safe();
                }
                t2.put(s, val);
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

