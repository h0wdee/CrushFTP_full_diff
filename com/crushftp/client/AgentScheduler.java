/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.AgentUI;
import com.crushftp.client.Client;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

public class AgentScheduler {
    static long lastOldCheck = 0L;
    static final long day = 86400000L;

    /*
     * Unable to fully structure code
     */
    public static void runSchedules(AgentUI aui) {
        time = new SimpleDateFormat("hh:mm aa", Locale.US);
        day_lookup = new Properties();
        day_lookup.put("SUN", "(1)");
        day_lookup.put("MON", "(2)");
        day_lookup.put("TUE", "(3)");
        day_lookup.put("WED", "(4)");
        day_lookup.put("THU", "(5)");
        day_lookup.put("FRI", "(6)");
        day_lookup.put("SAT", "(7)");
        v0 = purge_old = System.currentTimeMillis() - AgentScheduler.lastOldCheck > 3600000L;
        if (purge_old) {
            AgentScheduler.lastOldCheck = System.currentTimeMillis();
        }
        yyMMddHHmm = new SimpleDateFormat("yyMMddHHmm");
        yyMMdd = new SimpleDateFormat("yyMMdd");
        schedules = (Vector)aui.prefs.get("schedules");
        x = 0;
        while (x < schedules.size()) {
            block34: {
                runSchedule = false;
                p = null;
                try {
                    block36: {
                        block35: {
                            p = (Properties)schedules.elementAt(x);
                            if (p == null || !p.getProperty("enabled", "").equalsIgnoreCase("true")) break block34;
                            nextRun = Long.parseLong(p.getProperty("nextRun", "0"));
                            newNextRun = new Date().getTime();
                            if (!p.getProperty("scheduleType", "").equals("minutely") || new Date().getTime() <= nextRun) break block35;
                            newNextRun = yyMMddHHmm.parse(yyMMddHHmm.format(new Date(newNextRun))).getTime();
                            c = new GregorianCalendar();
                            c.setTime(new Date(newNextRun));
                            minutes = Integer.parseInt(p.getProperty("minutelyAmount"));
                            if (minutes == 0) {
                                minutes = 1;
                            }
                            if (minutes > 0) {
                                c.add(12, minutes);
                            } else {
                                c.add(13, minutes * -1);
                            }
                            newNextRun = c.getTime().getTime();
                            runSchedule = true;
                            break block36;
                        }
                        if (p.getProperty("scheduleType", "").equals("minutely")) break block36;
                        schedule_time = p.getProperty("scheduleTime", "").split(",");
                        xx = 0;
                        while (!(xx >= schedule_time.length || runSchedule && nextRun != -1L)) {
                            block37: {
                                block38: {
                                    block40: {
                                        block41: {
                                            block39: {
                                                v1 = last_time = xx == schedule_time.length - 1;
                                                if (schedule_time[xx].trim().equals("")) break block37;
                                                if (nextRun != -1L && !time.format(new Date()).equals(time.format(time.parse(schedule_time[xx].trim()))) || new Date().getTime() <= nextRun) break block38;
                                                if (!p.getProperty("scheduleType", "").equals("daily")) break block39;
                                                runSchedule = true;
                                                next_time = null;
                                                if (last_time) {
                                                    c = new GregorianCalendar();
                                                    c.setTime(new Date(newNextRun - 60000L));
                                                    c.add(5, Integer.parseInt(p.getProperty("dailyAmount")));
                                                    newNextRun = yyMMdd.parse(yyMMdd.format(c.getTime())).getTime();
                                                    next_time = schedule_time[0];
                                                } else {
                                                    newNextRun = yyMMddHHmm.parse(yyMMddHHmm.format(new Date(newNextRun))).getTime();
                                                    if (nextRun >= 0L) {
                                                        newNextRun += 60000L;
                                                    }
                                                    next_time = schedule_time[xx + 1];
                                                }
                                                while (!time.format(new Date(newNextRun)).equals(time.format(time.parse(next_time.trim())))) {
                                                    newNextRun += 60000L;
                                                }
                                                newNextRun = yyMMddHHmm.parse(yyMMddHHmm.format(new Date(newNextRun))).getTime();
                                                break block38;
                                            }
                                            if (!p.getProperty("scheduleType", "").equals("weekly")) break block40;
                                            day = day_lookup.getProperty(new SimpleDateFormat("EEE", Locale.US).format(new Date()).toUpperCase());
                                            if (nextRun != -1L && p.getProperty("weekDays", "").indexOf(day) < 0) break block38;
                                            if (p.getProperty("weekDays", "").indexOf(day) + 1 != p.getProperty("weekDays", "").split("\\)").length || !last_time) break block41;
                                            c = new GregorianCalendar();
                                            c.setTime(new Date());
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
                                            loops = 0;
                                            ** GOTO lbl99
                                            {
                                                c.add(10, 1);
                                                do {
                                                    if (c.get(7) == 1) continue block8;
                                                    while (c.get(7) != 1) {
                                                        c.add(10, 1);
                                                    }
                                                    ++loops;
lbl99:
                                                    // 2 sources

                                                } while (loops < Integer.parseInt(p.getProperty("weeklyAmount")));
                                            }
                                            newNextRun = c.getTime().getTime();
                                            newNextRun = yyMMdd.parse(yyMMdd.format(c.getTime())).getTime();
                                        }
                                        newNextRun = yyMMddHHmm.parse(yyMMddHHmm.format(new Date(newNextRun))).getTime();
                                        next_time = null;
                                        if (last_time || nextRun == -1L) {
                                            next_time = schedule_time[0];
                                        } else {
                                            newNextRun += 60000L;
                                            next_time = schedule_time[xx + 1];
                                        }
                                        while (!time.format(new Date(newNextRun)).equals(time.format(time.parse(next_time.trim())))) {
                                            newNextRun += 60000L;
                                        }
                                        if (nextRun == -1L || last_time) {
                                            day = day_lookup.getProperty(new SimpleDateFormat("EEE", Locale.US).format(new Date(newNextRun)).toUpperCase());
                                            while (p.getProperty("weekDays", "").indexOf(day) < 0) {
                                                day = day_lookup.getProperty(new SimpleDateFormat("EEE", Locale.US).format(new Date(newNextRun += 86400000L)).toUpperCase());
                                            }
                                        }
                                        runSchedule = true;
                                        break block38;
                                    }
                                    if (p.getProperty("scheduleType", "").equals("monthly")) {
                                        dd = new SimpleDateFormat("dd", Locale.US);
                                        d = new SimpleDateFormat("d", Locale.US);
                                        day1 = "(" + d.format(new Date()) + ")";
                                        day2 = "(" + dd.format(new Date()) + ")";
                                        if (nextRun == -1L || p.getProperty("monthDays", "").indexOf(day1) >= 0 || p.getProperty("monthDays", "").indexOf(day2) >= 0) {
                                            if (last_time && (p.getProperty("monthDays", "").indexOf(day1) == p.getProperty("monthDays", "").length() - day1.length() || p.getProperty("monthDays", "").indexOf(day2) == p.getProperty("monthDays", "").length() - day2.length())) {
                                                c = new GregorianCalendar();
                                                c.setTime(new Date());
                                                while (c.get(5) != 1) {
                                                    c.add(5, -1);
                                                }
                                                c.add(2, Integer.parseInt(p.getProperty("monthlyAmount")));
                                                newNextRun = c.getTime().getTime();
                                                newNextRun = yyMMdd.parse(yyMMdd.format(c.getTime())).getTime();
                                            }
                                            newNextRun = yyMMddHHmm.parse(yyMMddHHmm.format(new Date(newNextRun))).getTime();
                                            next_time = null;
                                            if (last_time || nextRun == -1L) {
                                                next_time = schedule_time[0];
                                            } else {
                                                newNextRun += 60000L;
                                                next_time = schedule_time[xx + 1];
                                            }
                                            while (!time.format(new Date(newNextRun)).equals(time.format(time.parse(next_time.trim())))) {
                                                newNextRun += 60000L;
                                            }
                                            if (nextRun == -1L) {
                                                day1 = "(" + d.format(new Date(newNextRun)) + ")";
                                                day2 = "(" + dd.format(new Date(newNextRun)) + ")";
                                                while (p.getProperty("monthDays", "").indexOf(day1) < 0 && p.getProperty("monthDays", "").indexOf(day2) < 0) {
                                                    day1 = "(" + d.format(new Date(newNextRun += 60000L)) + ")";
                                                    day2 = "(" + dd.format(new Date(newNextRun)) + ")";
                                                }
                                            }
                                            runSchedule = true;
                                        }
                                    }
                                }
                                if (nextRun == -1L) break;
                            }
                            ++xx;
                        }
                    }
                    if (runSchedule || nextRun == -1L) {
                        p.put("nextRun", String.valueOf(newNextRun));
                        aui.save_prefs();
                        if (nextRun >= 0L) {
                            AgentScheduler.runSchedule(aui, p);
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("" + p);
                }
            }
            ++x;
        }
    }

    public static boolean jobRunning(String scheduleName) {
        boolean ok = true;
        return !ok;
    }

    public static void runSchedule(AgentUI aui, Properties p) {
        boolean ok = true;
        if (p.getProperty("scheduleName", "").toUpperCase().endsWith("_SINGLE") || p.getProperty("single", "").equals("true")) {
            boolean bl = ok = !AgentScheduler.jobRunning(p.getProperty("scheduleName", ""));
        }
        if (ok) {
            Client client = aui.getNewClient();
            aui.clients.put(client.uid, client);
            client.prefs = p;
            aui.runSchedule(client, p);
            System.out.println("Ran Schedule :" + p.getProperty("scheduleName") + ":" + new Date());
        } else {
            System.out.println("Skipping scheduled job since its still running:" + p.getProperty("scheduleName"));
        }
    }
}

