/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import com.crushftp.client.File_S;
import com.crushftp.client.Worker;
import crushftp.gui.LOC;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.handlers.SharedSessionReplicated;
import crushftp.server.AdminControls;
import crushftp.server.ServerStatus;
import crushftp.server.daemon.ServerBeat;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class JobScheduler {
    static long lastOldCheck = 0L;
    static final long day = 86400000L;

    public static Vector getJobList(boolean include_events) {
        Vector jobs = new Vector();
        String[] list = new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/").list();
        JobScheduler.addJobs(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/", list, jobs, include_events);
        return jobs;
    }

    public static void addJobs(String path, String[] list, Vector jobs, boolean include_events) {
        if (list != null) {
            Arrays.sort(list);
        }
        int x = 0;
        while (list != null && x < list.length) {
            File_S f = new File_S(String.valueOf(path) + list[x]);
            if (!(f.isFile() || !include_events && f.getName().startsWith("__"))) {
                if (!new File_S(String.valueOf(f.getPath()) + "/job.XML").exists()) {
                    String[] sub_list = f.list();
                    JobScheduler.addJobs(String.valueOf(path) + f.getName() + "/", sub_list, jobs, include_events);
                }
                jobs.addElement(f);
            }
            ++x;
        }
    }

    public static String safeName(String scheduleName) {
        scheduleName = Common.replace_str(scheduleName, ":", "_");
        scheduleName = Common.replace_str(scheduleName, "#", "_");
        scheduleName = Common.replace_str(scheduleName, "@", "_");
        scheduleName = Common.replace_str(scheduleName, "!", "_");
        scheduleName = Common.replace_str(scheduleName, "&", "_");
        scheduleName = Common.replace_str(scheduleName, "\\", "_");
        scheduleName = Common.replace_str(scheduleName, ";", "_");
        scheduleName = Common.replace_str(scheduleName, "<", "_");
        scheduleName = Common.replace_str(scheduleName, ">", "_");
        return scheduleName;
    }

    /*
     * Unable to fully structure code
     */
    public static void runSchedules(Properties pp) {
        if (!ServerStatus.BG("job_scheduler_enabled")) {
            return;
        }
        if (ServerStatus.siBG("restart_when_idle")) {
            return;
        }
        if (ServerStatus.siBG("shutdown_when_idle")) {
            return;
        }
        if (System.getProperty("crushftp.singleuser", "false").equals("true")) {
            return;
        }
        if (!ServerBeat.current_master) {
            if (ServerStatus.BG("single_job_scheduler_serverbeat")) {
                return;
            }
        }
        time = new SimpleDateFormat("hh:mm aa", Locale.US);
        day_lookup = new Properties();
        day_lookup.put("SUN", "(1)");
        day_lookup.put("MON", "(2)");
        day_lookup.put("TUE", "(3)");
        day_lookup.put("WED", "(4)");
        day_lookup.put("THU", "(5)");
        day_lookup.put("FRI", "(6)");
        day_lookup.put("SAT", "(7)");
        jobs = JobScheduler.getJobList(false);
        yyMMddHHmm = new SimpleDateFormat("yyMMddHHmm z", Locale.US);
        yyMMdd = new SimpleDateFormat("yyMMdd", Locale.US);
        x = 0;
        while (x < jobs.size()) {
            block73: {
                runSchedule = false;
                p = null;
                job = (File_S)jobs.elementAt(x);
                if (job.getName().startsWith("_")) break block73;
                try {
                    block75: {
                        block74: {
                            c = new GregorianCalendar();
                            p = (Properties)Common.readXMLObject(String.valueOf(job.getPath()) + "/job.XML");
                            if (p == null) break block73;
                            Log.log("JOB_SCHEDULER", 0, "Checking time on job:" + job.getName());
                            p.put("scheduleName", AdminControls.jobName(job));
                            if (!p.getProperty("enabled", "").equalsIgnoreCase("true")) break block73;
                            nextRun = Long.parseLong(p.getProperty("nextRun", "0"));
                            if (nextRun > System.currentTimeMillis()) {
                                Log.log("JOB_SCHEDULER", 0, "Next run of the job: " + job.getName() + ": " + new Date(nextRun));
                            }
                            c.setTimeInMillis(yyMMddHHmm.parse(yyMMddHHmm.format(new Date())).getTime());
                            if (p.getProperty("scheduleTime", "").trim().startsWith(",")) {
                                p.put("scheduleTime", p.getProperty("scheduleTime", "").trim().substring(1));
                            }
                            if (p.getProperty("scheduleTime", "").trim().endsWith(",")) {
                                p.put("scheduleTime", p.getProperty("scheduleTime", "").trim().substring(p.getProperty("scheduleTime", "").trim().length() - 1));
                            }
                            if (!p.getProperty("scheduleType", "").equals("minutely") || new Date().getTime() <= nextRun) break block74;
                            Log.log("JOB_SCHEDULER", 0, "Calculating next run of job: " + job.getName());
                            Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Minutely: " + "Scheduled time minutely amount: " + p.getProperty("minutelyAmount", ""));
                            Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Minutely: Before the calculation: " + ":" + c.getTimeInMillis() + ":" + new Date(c.getTimeInMillis()));
                            minutes = Integer.parseInt(p.getProperty("minutelyAmount"));
                            if (minutes == 0) {
                                minutes = 1;
                            }
                            if (minutes > 0) {
                                c.add(12, minutes);
                            } else {
                                c.add(13, minutes * -1);
                            }
                            runSchedule = true;
                            Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Minutely: Next run of the job: " + ":" + c.getTimeInMillis() + ":" + new Date(c.getTimeInMillis()));
                            break block75;
                        }
                        if (p.getProperty("scheduleType", "").equals("minutely")) break block75;
                        schedule_time = p.getProperty("scheduleTime", "").split(",");
                        if (p.getProperty("scheduleTime", "").indexOf(",0:") >= 0) {
                            schedule_time2 = "";
                            xx = 0;
                            while (xx < schedule_time.length) {
                                next_time = schedule_time[xx].trim();
                                if (next_time.startsWith("0:")) {
                                    next_time = "12:" + next_time.substring(2);
                                }
                                schedule_time2 = String.valueOf(schedule_time2) + (xx == 0 ? "" : ",") + next_time;
                                ++xx;
                            }
                            p.put("scheduleTime", schedule_time2);
                            schedule_time = p.getProperty("scheduleTime", "").split(",");
                        }
                        xx = 0;
                        while (!(xx >= schedule_time.length || runSchedule && nextRun != -1L)) {
                            block76: {
                                block77: {
                                    block79: {
                                        block82: {
                                            block81: {
                                                block80: {
                                                    block78: {
                                                        v0 = last_time = xx == schedule_time.length - 1;
                                                        if (schedule_time[xx].trim().equals("")) break block76;
                                                        if (nextRun != -1L && !time.format(new Date()).equals(time.format(time.parse(schedule_time[xx].trim()))) || System.currentTimeMillis() <= nextRun) break block77;
                                                        if (nextRun == -1L) {
                                                            Log.log("JOB_SCHEDULER", 0, "Calculating next run of job: " + job.getName());
                                                        } else {
                                                            Log.log("JOB_SCHEDULER", 0, "Scheduled to run:" + job.getName());
                                                        }
                                                        Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Scheduled type: " + p.getProperty("scheduleType", ""));
                                                        Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Scheduled time: " + p.getProperty("scheduleTime", ""));
                                                        if (!p.getProperty("scheduleType", "").equals("daily")) break block78;
                                                        Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Daily: Before the calculation: " + c.getTimeInMillis() + ":" + new Date(c.getTimeInMillis()));
                                                        Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Daily: Checking last_time on job: " + last_time);
                                                        next_time = null;
                                                        if (last_time) {
                                                            if (nextRun != -1L) {
                                                                c.add(12, -1);
                                                                c.add(5, Integer.parseInt(p.getProperty("dailyAmount")));
                                                            }
                                                            c.setTimeInMillis(yyMMdd.parse(yyMMdd.format(c.getTime())).getTime());
                                                            Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Daily: Midnight of the day specified: " + c.getTimeInMillis() + ":" + new Date(c.getTimeInMillis()));
                                                            next_time = schedule_time[0];
                                                        } else {
                                                            if (nextRun >= 0L) {
                                                                c.add(12, 1);
                                                                Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Daily: Advance one minute: " + c.getTimeInMillis() + ":" + new Date(c.getTimeInMillis()));
                                                            }
                                                            next_time = nextRun < 0L ? schedule_time[xx] : schedule_time[xx + 1];
                                                        }
                                                        if (nextRun == -1L) {
                                                            xxx = 0;
                                                            while (xxx < schedule_time.length) {
                                                                daily_time = schedule_time[xxx];
                                                                if (time.parse(daily_time.trim()).getTime() > time.parse(time.format(c.getTime())).getTime()) {
                                                                    next_time = daily_time;
                                                                    break;
                                                                }
                                                                ++xxx;
                                                            }
                                                        }
                                                        Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Daily: Checking next time on job: " + next_time + ":" + time.format(time.parse(next_time.trim())));
                                                        JobScheduler.advance_up_to_next_time(c, schedule_time, next_time);
                                                        runSchedule = true;
                                                        if (nextRun == -1L && System.currentTimeMillis() > c.getTimeInMillis()) {
                                                            before = time.format(c.getTime());
                                                            c.add(5, Integer.parseInt(p.getProperty("dailyAmount")));
                                                            after = time.format(c.getTime());
                                                            if (!before.equals(after) && time.parse(before).getTime() > time.parse(after).getTime()) {
                                                                nex_time_date2 = time.parse(before.trim());
                                                                while (!time.format(c.getTime()).equals(time.format(nex_time_date2))) {
                                                                    before_one_min = c.getTimeZone().inDaylightTime(new Date(c.getTimeInMillis()));
                                                                    c.add(12, 1);
                                                                    after_one_min = c.getTimeZone().inDaylightTime(new Date(c.getTimeInMillis()));
                                                                    if (before_one_min == after_one_min || before_one_min) continue;
                                                                    nex_time_date2 = new Date(nex_time_date2.getTime() + (long)c.getTimeZone().getDSTSavings());
                                                                }
                                                            }
                                                        }
                                                        Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Daily: Checking calendar on job: " + c.getTimeInMillis() + ":" + new Date(c.getTimeInMillis()));
                                                        break block77;
                                                    }
                                                    if (!p.getProperty("scheduleType", "").equals("weekly")) break block79;
                                                    day = day_lookup.getProperty(new SimpleDateFormat("EEE", Locale.US).format(new Date()).toUpperCase());
                                                    if (!p.getProperty("weekDays", "").trim().equals("")) break block80;
                                                    Log.log("SERVER", 0, "Cannot schedule " + job.getName() + " because of an invalid schedule configuration.  No week days chosen.");
                                                    break block77;
                                                }
                                                if (nextRun != -1L && p.getProperty("weekDays", "").indexOf(day) < 0) break block77;
                                                Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Weekly:" + " Week days:" + p.getProperty("weekDays", ""));
                                                c.setTime(new Date());
                                                Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Weekly: Before the calculation: " + c.getTimeInMillis() + ":" + new Date(c.getTimeInMillis()));
                                                weekDays = p.getProperty("weekDays", "").split("\\)");
                                                if (day.indexOf(weekDays[weekDays.length - 1]) < 0 || !last_time) break block81;
                                                while (c.get(12) != 0) {
                                                    c.add(12, -1);
                                                }
                                                while (c.get(7) != 1) {
                                                    c.add(10, -1);
                                                }
                                                while (c.get(7) == 1) {
                                                    c.add(10, -1);
                                                }
                                                c.add(10, 1);
                                                Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Weekly: " + " Scheduled time weekly amount: " + p.getProperty("weeklyAmount", ""));
                                                loops = 0;
                                                ** GOTO lbl195
                                                {
                                                    c.add(10, 1);
                                                    do {
                                                        if (c.get(7) == 1) continue block12;
                                                        while (c.get(7) != 1) {
                                                            c.add(10, 1);
                                                        }
                                                        ++loops;
lbl195:
                                                        // 2 sources

                                                    } while (loops < Integer.parseInt(p.getProperty("weeklyAmount")));
                                                }
                                                c.setTimeInMillis(yyMMdd.parse(yyMMdd.format(c.getTime())).getTime());
                                                Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Weekly: " + " Day from week days: " + day + ":" + c.getTimeInMillis() + ":" + new Date(c.getTimeInMillis()));
                                                break block82;
                                            }
                                            if (last_time && nextRun != -1L) {
                                                c.add(12, -1);
                                                c.add(5, 1);
                                                c.setTimeInMillis(yyMMdd.parse(yyMMdd.format(c.getTime())).getTime());
                                                Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Weekly: added one day : " + c.getTimeInMillis() + ":" + new Date(c.getTimeInMillis()));
                                            }
                                        }
                                        next_time = null;
                                        if (last_time || nextRun == -1L) {
                                            next_time = schedule_time[0];
                                        } else {
                                            c.add(12, 1);
                                            Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Weekly: Advance one minute: " + c.getTimeInMillis() + ":" + new Date(c.getTimeInMillis()));
                                            next_time = schedule_time[xx + 1];
                                        }
                                        Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Weekly: Next time : " + next_time);
                                        if (nextRun == -1L) {
                                            time_matched = false;
                                            next_time2 = next_time;
                                            Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Weekly: Next time for calculation: " + next_time);
                                            startDSTDay = JobScheduler.isStartOfDSTDay(c.getTime());
                                            if (startDSTDay) {
                                                c.add(5, -1);
                                            }
                                            while (!time.format(c.getTime()).equals(time.format(time.parse(next_time.trim()))) && !time_matched) {
                                                xxx = 0;
                                                while (xxx < schedule_time.length && !time_matched) {
                                                    next_time2 = schedule_time[xxx];
                                                    if (time.format(c.getTime()).equals(time.format(time.parse(next_time2.trim())))) {
                                                        time_matched = true;
                                                    }
                                                    ++xxx;
                                                }
                                                if (time_matched) continue;
                                                c.add(12, 1);
                                            }
                                            if (startDSTDay) {
                                                c.add(5, 1);
                                            }
                                            Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Weekly: Calculated next scheduled time: " + c.getTimeInMillis() + ":" + new Date(c.getTimeInMillis()));
                                        } else {
                                            Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Weekly Days: Next time: " + next_time);
                                            JobScheduler.advance_up_to_next_time(c, schedule_time, next_time);
                                            Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Weekly: Next scheduled time: " + c.getTimeInMillis() + ":" + new Date(c.getTimeInMillis()));
                                        }
                                        if (nextRun == -1L || last_time) {
                                            recalculate_last_day = false;
                                            day = day_lookup.getProperty(new SimpleDateFormat("EEE", Locale.US).format(c.getTime()).toUpperCase());
                                            while (p.getProperty("weekDays", "").indexOf(day) < 0) {
                                                before = c.getTimeZone().inDaylightTime(new Date(c.getTimeInMillis()));
                                                c.add(5, 1);
                                                day = day_lookup.getProperty(new SimpleDateFormat("EEE", Locale.US).format(c.getTime()).toUpperCase());
                                                after = c.getTimeZone().inDaylightTime(new Date(c.getTimeInMillis()));
                                                recalculate_last_day = before ^ after;
                                            }
                                            if (!recalculate_last_day && time.parse(time.format(c.getTime())).getTime() < time.parse(next_time.trim()).getTime()) {
                                                recalculate_last_day = true;
                                            }
                                            if (recalculate_last_day) {
                                                c.set(11, 0);
                                                c.set(12, 0);
                                                JobScheduler.advance_up_to_next_time(c, schedule_time, next_time);
                                            }
                                        }
                                        runSchedule = true;
                                        Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Weekly: Checking calendar on job: " + c.getTimeInMillis() + ":" + new Date(c.getTimeInMillis()));
                                        break block77;
                                    }
                                    if (p.getProperty("scheduleType", "").equals("monthly")) {
                                        dd = new SimpleDateFormat("dd", Locale.US);
                                        d = new SimpleDateFormat("d", Locale.US);
                                        day1 = "(" + d.format(new Date()) + ")";
                                        day2 = "(" + dd.format(new Date()) + ")";
                                        if (p.getProperty("monthDays", "").trim().equals("")) {
                                            Log.log("SERVER", 0, "Cannot schedule " + job.getName() + " because of an invalid schedule configuration.  No month days chosen.");
                                        } else if (nextRun == -1L || p.getProperty("monthDays", "").indexOf(day1) >= 0 || p.getProperty("monthDays", "").indexOf(day2) >= 0) {
                                            Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Monthly: Before the calculation: " + c.getTimeInMillis() + ":" + new Date(c.getTimeInMillis()));
                                            Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Monthly: Month days: " + p.getProperty("monthDays", ""));
                                            monthDays = p.getProperty("monthDays", "").split("\\)");
                                            if (last_time && (day1.indexOf(monthDays[monthDays.length - 1]) >= 0 || day2.indexOf(monthDays[monthDays.length - 1]) >= 0)) {
                                                Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Monthly: day1: " + day1);
                                                Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Monthly: day2: " + day2);
                                                c.setTime(new Date());
                                                while (c.get(5) != 1) {
                                                    c.add(5, -1);
                                                }
                                                if (nextRun != -1L) {
                                                    c.add(2, Integer.parseInt(p.getProperty("monthlyAmount")));
                                                }
                                                c.setTimeInMillis(yyMMdd.parse(yyMMdd.format(c.getTime())).getTime());
                                                Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Monthly: After processed days: " + c.getTimeInMillis() + ":" + new Date(c.getTimeInMillis()));
                                            }
                                            next_time = null;
                                            if (last_time || nextRun == -1L) {
                                                next_time = schedule_time[0];
                                            } else {
                                                c.add(12, 1);
                                                Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Monthly: One minute in advance: " + c.getTimeInMillis() + ":" + new Date(c.getTimeInMillis()));
                                                next_time = schedule_time[xx + 1];
                                                Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Monthly: Next time: " + next_time);
                                            }
                                            if (nextRun == -1L) {
                                                xxx = 0;
                                                while (xxx < schedule_time.length) {
                                                    daily_time = schedule_time[xxx];
                                                    if (time.parse(daily_time.trim()).getTime() > time.parse(time.format(c.getTime())).getTime()) {
                                                        next_time = daily_time;
                                                        break;
                                                    }
                                                    ++xxx;
                                                }
                                                Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Monthly: Nearest next time: " + next_time);
                                            }
                                            next_time = JobScheduler.advance_up_to_next_time(c, schedule_time, next_time);
                                            Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Monthly: Advance it up to the first time interval: " + c.getTimeInMillis() + ":" + new Date(c.getTimeInMillis()));
                                            if (nextRun == -1L) {
                                                day1 = "(" + d.format(c.getTime()) + ")";
                                                day2 = "(" + dd.format(c.getTime()) + ")";
                                                while (p.getProperty("monthDays", "").indexOf(day1) < 0 && p.getProperty("monthDays", "").indexOf(day2) < 0) {
                                                    c.add(12, 1);
                                                    day1 = "(" + d.format(c.getTime()) + ")";
                                                    day2 = "(" + dd.format(c.getTime()) + ")";
                                                }
                                                next_time = JobScheduler.advance_up_to_next_time(c, schedule_time, next_time);
                                                if (System.currentTimeMillis() > c.getTimeInMillis()) {
                                                    c.add(2, Integer.parseInt(p.getProperty("monthlyAmount")));
                                                }
                                            }
                                            runSchedule = true;
                                            Log.log("JOB_SCHEDULER", 1, String.valueOf(job.getName()) + ":" + " Monthly: Checking calendar on job: " + c.getTimeInMillis() + ":" + new Date(c.getTimeInMillis()));
                                        }
                                    }
                                }
                                if (nextRun == -1L) break;
                            }
                            ++xx;
                        }
                    }
                    if (!runSchedule && nextRun != -1L) break block73;
                    c.setTimeInMillis(yyMMddHHmm.parse(yyMMddHHmm.format(c.getTime())).getTime());
                    p.put("nextRun", String.valueOf(c.getTimeInMillis()));
                    Log.log("JOB_SCHEDULER", 1, "Calculated next run of the job:" + job.getName() + ":" + c.getTimeInMillis() + ":" + new Date(c.getTimeInMillis()));
                    uid = Common.makeBoundary(6);
                    Common.writeXMLObject(String.valueOf(job.getPath()) + "/job." + uid + "_new.XML", (Object)p, "job");
                    if (new File_S(String.valueOf(job.getPath()) + "/job." + uid + "_new.XML").length() > 0L) {
                        new File_S(String.valueOf(job.getPath()) + "/job." + uid + "_old.XML").delete();
                        if (new File_S(String.valueOf(job.getPath()) + "/job.XML").renameTo(new File_S(String.valueOf(job.getPath()) + "/job." + uid + "_old.XML")) && new File_S(String.valueOf(job.getPath()) + "/job." + uid + "_new.XML").renameTo(new File_S(String.valueOf(job.getPath()) + "/job.XML"))) {
                            new File_S(String.valueOf(job.getPath()) + "/job." + uid + "_old.XML").delete();
                        }
                        if (ServerStatus.siIG("enterprise_level") <= 0) {
                            throw new Exception("Job Scheduler feature is only for Enterprise licenses.");
                        }
                        if (nextRun >= 0L) {
                            p2 = p;
                            Worker.startWorker(new Runnable(){

                                @Override
                                public void run() {
                                    JobScheduler.runSchedule(p2);
                                }
                            });
                        }
                        break block73;
                    }
                    throw new Exception("Job Scheduler failed to update next run time for job, cancelling scheduled time:" + job);
                }
                catch (Exception e) {
                    Log.log("SERVER", 0, e);
                    Log.log("SERVER", 0, "" + p);
                }
            }
            ++x;
        }
        v1 = purge_old = System.currentTimeMillis() - JobScheduler.lastOldCheck > 3600000L;
        if (purge_old) {
            JobScheduler.lastOldCheck = System.currentTimeMillis();
            try {
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        Vector jobs = JobScheduler.getJobList(true);
                        lastOldCheck = System.currentTimeMillis();
                        int x = 0;
                        while (x < jobs.size()) {
                            Object p = null;
                            File_S job2 = (File_S)jobs.elementAt(x);
                            lastOldCheck = System.currentTimeMillis();
                            String name = job2.getName();
                            if (name.startsWith("__")) {
                                try {
                                    lastOldCheck = System.currentTimeMillis();
                                    Long.parseLong(name.substring(name.length() - 13));
                                    if (new File_S(String.valueOf(job2.getPath()) + "/job.XML").exists()) {
                                        if (System.currentTimeMillis() - new File_S(String.valueOf(job2.getPath()) + "/job.XML").lastModified() >= ServerStatus.LG("recent_temp_job_days") * 86400000L) {
                                            Common.recurseDelete(String.valueOf(job2.getPath()) + "/", false);
                                        }
                                    } else if (new File_S(String.valueOf(job2.getPath()) + "/inprogress/").exists()) {
                                        if (System.currentTimeMillis() - new File_S(String.valueOf(job2.getPath()) + "/inprogress/").lastModified() >= ServerStatus.LG("recent_temp_job_days") * 86400000L) {
                                            Common.recurseDelete(String.valueOf(job2.getPath()) + "/", false);
                                        }
                                    } else if (System.currentTimeMillis() - new File_S(String.valueOf(job2.getPath()) + "/").lastModified() >= ServerStatus.LG("recent_temp_job_days") * 86400000L) {
                                        Common.recurseDelete(String.valueOf(job2.getPath()) + "/", false);
                                    }
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                            lastOldCheck = System.currentTimeMillis();
                            File_S[] olds = (File_S[])job2.listFiles();
                            lastOldCheck = System.currentTimeMillis();
                            int xx = 0;
                            while (olds != null && xx < olds.length) {
                                lastOldCheck = System.currentTimeMillis();
                                File_S old = olds[xx];
                                name = old.getName();
                                if (name.toUpperCase().endsWith(".XML") && !name.equalsIgnoreCase("job.XML") && !name.equalsIgnoreCase("inprogress.XML") && !name.equalsIgnoreCase("inprogress") && System.currentTimeMillis() - old.lastModified() > ServerStatus.LG("recent_job_days") * 86400000L && System.currentTimeMillis() - old.lastModified() > 300000L) {
                                    old.delete();
                                }
                                ++xx;
                            }
                            lastOldCheck = System.currentTimeMillis();
                            ++x;
                        }
                    }
                }, "PurgeOldJobsChecker");
            }
            catch (IOException e) {
                Log.log("JOB_SCHEDULER", 0, e);
            }
        }
    }

    public static boolean jobRunning(String scheduleName) {
        boolean ok = true;
        File_S[] inprogress = (File_S[])new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/" + scheduleName + "/inprogress/").listFiles();
        if (inprogress != null) {
            int x = 0;
            while (ok && x < inprogress.length) {
                if (inprogress[x].getName().toUpperCase().endsWith(".XML") || !inprogress[x].getName().startsWith(".")) {
                    ok = false;
                }
                ++x;
            }
        }
        Vector jobs = (Vector)ServerStatus.thisObj.server_info.get("running_tasks");
        int x = 0;
        while (ok && x < jobs.size()) {
            Properties tracker = (Properties)jobs.elementAt(x);
            Properties settings = (Properties)tracker.get("settings");
            if (settings == null) {
                settings = new Properties();
            }
            if (scheduleName.equalsIgnoreCase(settings.getProperty("scheduleName", ""))) {
                ok = false;
            }
            ++x;
        }
        return !ok;
    }

    public static void runSchedule(Properties p) {
        boolean ok = true;
        if (p.getProperty("scheduleName", "").toUpperCase().endsWith("_SINGLE") || p.getProperty("single", "").equals("true")) {
            boolean bl = ok = !JobScheduler.jobRunning(p.getProperty("scheduleName", ""));
        }
        if (p.getProperty("singleServer", "").equals("true") && ok) {
            boolean bl = ok = !JobScheduler.jobRunning(p.getProperty("scheduleName", ""));
            if (ok) {
                Properties pp = new Properties();
                pp.put("scheduleName", p.getProperty("scheduleName"));
                pp.put("need_response", "true");
                SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.JobScheduler.jobRunning", "info", pp);
                long start = System.currentTimeMillis();
                while (pp.getProperty("response_num", "0").equals("0") && System.currentTimeMillis() - start < 5000L) {
                    try {
                        Thread.sleep(100L);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                Properties val = (Properties)pp.get("val");
                if (val != null) {
                    Enumeration<Object> keys = val.keys();
                    while (keys.hasMoreElements()) {
                        String key = keys.nextElement().toString();
                        if (!key.startsWith("running_") || !val.getProperty(key).equalsIgnoreCase("true")) continue;
                        ok = false;
                    }
                }
            }
        }
        if (ok) {
            try {
                class Runner
                implements Runnable {
                    Properties params;

                    public Runner(Properties params) {
                        this.params = params;
                    }

                    @Override
                    public void run() {
                        if (Log.log("JOB_SCHEDULER", 2, "")) {
                            Log.log("JOB_SCHEDULER", 2, "" + this.params);
                            try {
                                Common cfr_ignored_0 = ServerStatus.thisObj.common_code;
                                Log.log("JOB_SCHEDULER", 2, Common.getXMLString(this.params, "task", null, true));
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                        Properties event = new Properties();
                        event.putAll((Map<?, ?>)this.params);
                        event.put("event_plugin_list", this.params.getProperty("plugin"));
                        event.put("name", "ScheduledPluginEvent:" + this.params.getProperty("scheduleName"));
                        ServerStatus.thisObj.events6.doEventPlugin(null, event, null, new Vector());
                    }
                }
                Worker.startWorker(new Runner(p), "schedule:" + p.getProperty("scheduleName") + ":" + new Date());
            }
            catch (IOException iOException) {
                // empty catch block
            }
            Log.log("SERVER", 0, LOC.G("Ran Schedule") + ":" + p.getProperty("scheduleName") + ":" + new Date());
            Log.log("JOB_SCHEDULER", 1, "Schedule Config:" + p.toString());
        } else {
            Log.log("SERVER", 0, "Skipping scheduled job since its still running:" + p.getProperty("scheduleName"));
        }
    }

    public static boolean isStartOfDSTDay(Date date) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        boolean before = ((Calendar)c).getTimeZone().inDaylightTime(date);
        ((Calendar)c).add(5, 1);
        boolean after = ((Calendar)c).getTimeZone().inDaylightTime(c.getTime());
        return before != after && !before;
    }

    static String advance_up_to_next_time(Calendar c, String[] schedule_time, String next_time) throws Exception {
        SimpleDateFormat time = new SimpleDateFormat("hh:mm aa", Locale.US);
        Date nex_time_date = time.parse(next_time.trim());
        boolean dst_start = false;
        boolean match = false;
        while (!time.format(c.getTime()).equals(time.format(nex_time_date)) && !match) {
            boolean before = c.getTimeZone().inDaylightTime(new Date(c.getTimeInMillis()));
            c.add(12, 1);
            boolean after = c.getTimeZone().inDaylightTime(new Date(c.getTimeInMillis()));
            if (before != after && !before) {
                dst_start = true;
            }
            if (!dst_start) continue;
            int xxx = 0;
            while (xxx < schedule_time.length) {
                String next_time2 = schedule_time[xxx];
                Date nex_time2_date = new Date(time.parse(next_time2.trim()).getTime() + (long)(time.parse(time.format(c.getTime())).getTime() > time.parse(next_time2.trim()).getTime() ? c.getTimeZone().getDSTSavings() : 0));
                if (time.format(c.getTime()).equals(time.format(nex_time2_date))) {
                    if (!next_time.equals(time.format(nex_time2_date))) {
                        next_time = time.format(nex_time2_date);
                    }
                    match = true;
                }
                ++xxx;
            }
        }
        return next_time;
    }
}

