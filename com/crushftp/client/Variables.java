/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.VRL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

public class Variables {
    static long UID_GLOBAL = System.currentTimeMillis() / 1000L;
    public static Vector recent_guids = new Vector();
    SimpleDateFormat MM = new SimpleDateFormat("MM", Locale.US);
    SimpleDateFormat MMM = new SimpleDateFormat("MMM", Locale.US);
    SimpleDateFormat MMMM = new SimpleDateFormat("MMMM", Locale.US);
    SimpleDateFormat dd = new SimpleDateFormat("dd", Locale.US);
    SimpleDateFormat DD = new SimpleDateFormat("DD", Locale.US);
    SimpleDateFormat D = new SimpleDateFormat("D", Locale.US);
    SimpleDateFormat F = new SimpleDateFormat("F", Locale.US);
    SimpleDateFormat w = new SimpleDateFormat("w", Locale.US);
    SimpleDateFormat W = new SimpleDateFormat("W", Locale.US);
    SimpleDateFormat d_ = new SimpleDateFormat("d", Locale.US);
    SimpleDateFormat yy = new SimpleDateFormat("yy", Locale.US);
    SimpleDateFormat yyyy = new SimpleDateFormat("yyyy", Locale.US);
    SimpleDateFormat mm = new SimpleDateFormat("mm", Locale.US);
    SimpleDateFormat hh = new SimpleDateFormat("hh", Locale.US);
    SimpleDateFormat HH = new SimpleDateFormat("HH", Locale.US);
    SimpleDateFormat k = new SimpleDateFormat("k", Locale.US);
    SimpleDateFormat K = new SimpleDateFormat("K", Locale.US);
    SimpleDateFormat ss = new SimpleDateFormat("ss", Locale.US);
    SimpleDateFormat aa = new SimpleDateFormat("aa", Locale.US);
    SimpleDateFormat S = new SimpleDateFormat("S", Locale.US);
    SimpleDateFormat SSS = new SimpleDateFormat("SSS", Locale.US);
    SimpleDateFormat EEE = new SimpleDateFormat("EEE", Locale.US);
    SimpleDateFormat EEEE = new SimpleDateFormat("EEEE", Locale.US);
    SimpleDateFormat Z = new SimpleDateFormat("Z", Locale.US);
    SimpleDateFormat z = new SimpleDateFormat("z", Locale.US);
    Date d = new Date();
    public boolean use_safe_url = false;
    public static Object uidg_lock = new Object();

    public void setDate(Date d) {
        if (d == null) {
            d = new Date();
        }
        this.d = d;
    }

    public String replace_vars_line_url(String the_line, Properties item, String r1, String r2) {
        if (item == null) {
            return the_line;
        }
        if (the_line.indexOf(r1) < 0) {
            return the_line;
        }
        String addon = "";
        try {
            if (!item.getProperty("url", "").trim().equals("")) {
                addon = "";
                int addOnLoop = 0;
                while (addOnLoop < 2) {
                    if (addOnLoop > 0) {
                        addon = "_2";
                    }
                    if (item.containsKey("url" + addon)) {
                        int xx;
                        String[] paths;
                        VRL vrl = new VRL(Common.url_decode(item.getProperty("url" + addon)));
                        String filename = "";
                        if (vrl.getFile() != null) {
                            filename = Common.last(vrl.getFile());
                        }
                        the_line = Common.replace_str(the_line, String.valueOf(r1) + "path" + addon + r2, item.getProperty("the_file_path" + addon, ""));
                        the_line = Common.replace_str(the_line, String.valueOf(r1) + "name" + addon + r2, item.getProperty("the_file_name" + addon, ""));
                        the_line = Common.replace_str(the_line, String.valueOf(r1) + "name2" + addon + r2, item.getProperty("the_file_name2" + addon, ""));
                        the_line = Common.replace_str(the_line, String.valueOf(r1) + "parent_path" + addon + r2, Common.all_but_last(item.getProperty("the_file_path" + addon, "")));
                        if (filename.indexOf(".") >= 0) {
                            the_line = Common.replace_str(the_line, String.valueOf(r1) + "stem" + addon + r2, filename.substring(0, filename.lastIndexOf(".")));
                            the_line = Common.replace_str(the_line, String.valueOf(r1) + "stem_alt" + addon + r2, filename.substring(0, filename.indexOf(".")));
                            the_line = Common.replace_str(the_line, String.valueOf(r1) + "ext" + addon + r2, filename.substring(filename.lastIndexOf(".")));
                            the_line = Common.replace_str(the_line, String.valueOf(r1) + "ext_alt" + addon + r2, filename.substring(filename.indexOf(".")));
                            the_line = Common.replace_str(the_line, String.valueOf(r1) + "ext2" + addon + r2, filename.substring(filename.lastIndexOf(".") + 1));
                            String[] dots_a = filename.split("\\.");
                            int dots = 0;
                            while (dots < dots_a.length) {
                                the_line = Common.replace_str(the_line, String.valueOf(r1) + "dot" + dots + addon + r2, dots_a[dots]);
                                ++dots;
                            }
                            int loop = 0;
                            int dots2 = dots_a.length - 1;
                            while (dots2 >= 0) {
                                the_line = Common.replace_str(the_line, String.valueOf(r1) + loop + "dot" + addon + r2, dots_a[dots2]);
                                ++loop;
                                --dots2;
                            }
                        } else {
                            the_line = Common.replace_str(the_line, String.valueOf(r1) + "stem" + addon + r2, filename);
                            the_line = Common.replace_str(the_line, String.valueOf(r1) + "ext" + addon + r2, "");
                            the_line = Common.replace_str(the_line, String.valueOf(r1) + "ext2" + addon + r2, "");
                        }
                        if (item.containsKey("the_file_path" + addon)) {
                            paths = item.getProperty("the_file_path" + addon).split("/");
                            xx = 0;
                            while (xx < 100) {
                                if (xx < paths.length) {
                                    the_line = Common.replace_str(the_line, String.valueOf(r1) + "path" + addon + xx + r2, "/" + paths[paths.length - 1 - xx]);
                                    the_line = Common.replace_str(the_line, String.valueOf(r1) + xx + "path" + addon + r2, String.valueOf(paths[xx]) + "/");
                                } else {
                                    the_line = Common.replace_str(the_line, String.valueOf(r1) + "path" + addon + xx + r2, "");
                                    the_line = Common.replace_str(the_line, String.valueOf(r1) + xx + "path" + addon + r2, "");
                                }
                                ++xx;
                            }
                        }
                        if (item.containsKey("url" + addon)) {
                            paths = Common.url_decode(this.use_safe_url ? new VRL(item.getProperty("url" + addon)).safe() : item.getProperty("url" + addon)).split("/");
                            xx = 0;
                            while (xx < 100) {
                                if (xx < paths.length) {
                                    the_line = Common.replace_str(the_line, String.valueOf(r1) + "url" + addon + xx + r2, "/" + paths[paths.length - 1 - xx]);
                                    the_line = Common.replace_str(the_line, String.valueOf(r1) + xx + "url" + addon + r2, String.valueOf(paths[xx]) + "/");
                                } else {
                                    the_line = Common.replace_str(the_line, String.valueOf(r1) + "url" + addon + xx + r2, "");
                                    the_line = Common.replace_str(the_line, String.valueOf(r1) + xx + "url" + addon + r2, "");
                                }
                                if (xx < paths.length) {
                                    the_line = Common.replace_str(the_line, String.valueOf(r1) + "url" + addon + "/" + xx + r2, paths[paths.length - 1 - xx]);
                                    the_line = Common.replace_str(the_line, String.valueOf(r1) + xx + "url" + addon + "/" + r2, paths[xx]);
                                } else {
                                    the_line = Common.replace_str(the_line, String.valueOf(r1) + "url" + addon + "/" + xx + r2, "");
                                    the_line = Common.replace_str(the_line, String.valueOf(r1) + xx + "url" + addon + "/" + r2, "");
                                }
                                ++xx;
                            }
                        }
                    }
                    ++addOnLoop;
                }
            }
        }
        catch (Exception e) {
            Common.log("SERVER", 0, e);
        }
        return the_line;
    }

    public synchronized String replace_vars_line_date(String the_line, Properties item, String r1, String r2) {
        if (the_line.indexOf(r1) < 0) {
            return the_line;
        }
        if (the_line.indexOf(String.valueOf(r1) + "uid" + r2) >= 0) {
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "uid" + r2, String.valueOf(Variables.uid()));
        }
        if (the_line.indexOf(String.valueOf(r1) + "uidg" + r2) >= 0) {
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "uidg" + r2, String.valueOf(Variables.uidg()));
        }
        int loop = 0;
        while (loop < 20) {
            if (the_line.indexOf(String.valueOf(r1) + "random" + loop + r2) >= 0) {
                the_line = Common.replace_str(the_line, String.valueOf(r1) + "random" + loop + r2, String.valueOf(Common.makeBoundary(loop)));
            }
            ++loop;
        }
        try {
            while (the_line.indexOf(String.valueOf(r1) + "randomnumber") >= 0) {
                String s = the_line.substring(the_line.indexOf(String.valueOf(r1) + "randomnumber") + (String.valueOf(r1) + "randomnumber").length());
                s = s.substring(0, s.indexOf(r2));
                the_line = Common.replace_str(the_line, String.valueOf(r1) + "randomnumber" + s + r2, String.valueOf(Common.getRandomInt(Integer.parseInt(s))));
            }
        }
        catch (Exception e) {
            Common.log("SERVER", 0, e);
        }
        int dateadd = 0;
        if (the_line.indexOf(String.valueOf(r1) + "dateadd") >= 0) {
            String inner = the_line.substring(the_line.indexOf(String.valueOf(r1) + "dateadd") + (String.valueOf(r1) + "dateadd").length(), the_line.indexOf(r2, the_line.indexOf(String.valueOf(r1) + "dateadd")));
            dateadd = Integer.parseInt(inner.substring(inner.indexOf(":") + 1).trim());
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "dateadd"))) + the_line.substring(the_line.indexOf(r2, the_line.indexOf(String.valueOf(r1) + "dateadd")) + 1);
        }
        Date now = this.d;
        GregorianCalendar yesterday = new GregorianCalendar();
        yesterday.setTime(new Date());
        yesterday.add(5, -1);
        GregorianCalendar tomorrow = new GregorianCalendar();
        tomorrow.setTime(new Date());
        tomorrow.add(5, 1);
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(this.d);
        if (dateadd != 0) {
            now = Variables.getDateAdd(now, dateadd);
        }
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "millis" + r2, String.valueOf(now.getTime()));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "yesterday" + r2, String.valueOf(yesterday.getTime().getTime()));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "tomorrow" + r2, String.valueOf(tomorrow.getTime().getTime()));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "time" + r2, String.valueOf(System.currentTimeMillis()));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "now" + r2, String.valueOf(System.currentTimeMillis()));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "MM" + r2, this.MM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "MMM" + r2, this.MMM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "MMMM" + r2, this.MMMM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "dd" + r2, this.dd.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "d" + r2, this.d_.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "yy" + r2, this.yy.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "yyyy" + r2, this.yyyy.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "mm" + r2, this.mm.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "hh" + r2, this.hh.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "HH" + r2, this.HH.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "k" + r2, this.k.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "K" + r2, this.K.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "ss" + r2, this.ss.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "aa" + r2, this.aa.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "S" + r2, this.S.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "SSS" + r2, this.SSS.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "EEE" + r2, this.EEE.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "EEEE" + r2, this.EEEE.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "u" + r2, String.valueOf(c.get(7)));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "Z" + r2, this.Z.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "z" + r2, this.z.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "DD" + r2, this.DD.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "D" + r2, this.D.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "F" + r2, this.F.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "w" + r2, this.w.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "W" + r2, this.W.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "J" + r2, String.valueOf(Variables.date_to_julian(now)));
        now = yesterday.getTime();
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!millis" + r2, String.valueOf(now.getTime()));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!MM" + r2, this.MM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!MMM" + r2, this.MMM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!MMMM" + r2, this.MMMM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!dd" + r2, this.dd.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!d" + r2, this.d_.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!yy" + r2, this.yy.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!yyyy" + r2, this.yyyy.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!mm" + r2, this.mm.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!hh" + r2, this.hh.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!HH" + r2, this.HH.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!k" + r2, this.k.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!K" + r2, this.K.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!ss" + r2, this.ss.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!aa" + r2, this.aa.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!S" + r2, this.S.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!SSS" + r2, this.SSS.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!EEE" + r2, this.EEE.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!EEEE" + r2, this.EEEE.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!u" + r2, String.valueOf(yesterday.get(7)));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!Z" + r2, this.Z.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!z" + r2, this.z.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!DD" + r2, this.DD.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!D" + r2, this.D.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!F" + r2, this.F.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!w" + r2, this.w.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!W" + r2, this.W.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!J" + r2, String.valueOf(Variables.date_to_julian(now)));
        now = tomorrow.getTime();
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "millis!" + r2, String.valueOf(now.getTime()));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "MM!" + r2, this.MM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "MMM!" + r2, this.MMM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "MMMM!" + r2, this.MMMM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "dd!" + r2, this.dd.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "d!" + r2, this.d_.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "yy!" + r2, this.yy.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "yyyy!" + r2, this.yyyy.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "mm!" + r2, this.mm.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "hh!" + r2, this.hh.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "HH!" + r2, this.HH.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "k!" + r2, this.k.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "K!" + r2, this.K.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "ss!" + r2, this.ss.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "aa!" + r2, this.aa.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "S!" + r2, this.S.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "SSS!" + r2, this.SSS.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "EEE!" + r2, this.EEE.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "EEEE!" + r2, this.EEEE.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "u!" + r2, String.valueOf(tomorrow.get(7)));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "Z!" + r2, this.Z.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "z!" + r2, this.z.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "DD!" + r2, this.DD.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "D!" + r2, this.D.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "F!" + r2, this.F.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "w!" + r2, this.w.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "W!" + r2, this.W.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "J!" + r2, String.valueOf(Variables.date_to_julian(now)));
        GregorianCalendar lastweek = new GregorianCalendar();
        lastweek.setTime(this.d);
        lastweek.add(5, -7);
        now = lastweek.getTime();
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!millis" + r2, String.valueOf(now.getTime()));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!MM" + r2, this.MM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!MMM" + r2, this.MMM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!MMMM" + r2, this.MMMM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!dd" + r2, this.dd.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!d" + r2, this.d_.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!yy" + r2, this.yy.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!yyyy" + r2, this.yyyy.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!mm" + r2, this.mm.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!hh" + r2, this.hh.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!HH" + r2, this.HH.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!k" + r2, this.k.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!K" + r2, this.K.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!ss" + r2, this.ss.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!aa" + r2, this.aa.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!S" + r2, this.S.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!SSS" + r2, this.SSS.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!EEE" + r2, this.EEE.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!EEEE" + r2, this.EEEE.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!u" + r2, String.valueOf(lastweek.get(7)));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!Z" + r2, this.Z.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!z" + r2, this.z.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!DD" + r2, this.DD.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!D" + r2, this.D.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!F" + r2, this.F.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!w" + r2, this.w.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!W" + r2, this.W.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!J" + r2, String.valueOf(Variables.date_to_julian(now)));
        GregorianCalendar nextweek = new GregorianCalendar();
        nextweek.setTime(this.d);
        nextweek.add(5, 7);
        now = nextweek.getTime();
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "millis!!" + r2, String.valueOf(now.getTime()));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "MM!!" + r2, this.MM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "MMM!!" + r2, this.MMM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "MMMM!!" + r2, this.MMMM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "dd!!" + r2, this.dd.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "d!!" + r2, this.d_.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "yy!!" + r2, this.yy.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "yyyy!!" + r2, this.yyyy.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "mm!!" + r2, this.mm.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "hh!!" + r2, this.hh.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "HH!!" + r2, this.HH.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "k!!" + r2, this.k.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "K!!" + r2, this.K.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "ss!!" + r2, this.ss.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "aa!!" + r2, this.aa.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "S!!" + r2, this.S.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "SSS!!" + r2, this.SSS.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "EEE!!" + r2, this.EEE.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "EEEE!!" + r2, this.EEEE.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "u!!" + r2, String.valueOf(nextweek.get(7)));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "Z!!" + r2, this.Z.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "z!!" + r2, this.z.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "DD!!" + r2, this.DD.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "D!!" + r2, this.D.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "F!!" + r2, this.F.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "w!!" + r2, this.w.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "W!!" + r2, this.W.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "J!!" + r2, String.valueOf(Variables.date_to_julian(now)));
        GregorianCalendar lastmonth = new GregorianCalendar();
        lastmonth.setTime(this.d);
        lastmonth.add(2, -1);
        now = lastmonth.getTime();
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!millis" + r2, String.valueOf(now.getTime()));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!MM" + r2, this.MM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!MMM" + r2, this.MMM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!MMMM" + r2, this.MMMM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!dd" + r2, this.dd.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!d" + r2, this.d_.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!yy" + r2, this.yy.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!yyyy" + r2, this.yyyy.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!mm" + r2, this.mm.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!hh" + r2, this.hh.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!HH" + r2, this.HH.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!k" + r2, this.k.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!K" + r2, this.K.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!ss" + r2, this.ss.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!aa" + r2, this.aa.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!S" + r2, this.S.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!SSS" + r2, this.SSS.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!EEE" + r2, this.EEE.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!EEEE" + r2, this.EEEE.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!u" + r2, String.valueOf(lastmonth.get(7)));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!Z" + r2, this.Z.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!z" + r2, this.z.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!DD" + r2, this.DD.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!D" + r2, this.D.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!F" + r2, this.F.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!w" + r2, this.w.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!W" + r2, this.W.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "!!!J" + r2, String.valueOf(Variables.date_to_julian(now)));
        GregorianCalendar nextmonth = new GregorianCalendar();
        nextmonth.setTime(this.d);
        nextmonth.add(2, 1);
        now = nextmonth.getTime();
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "millis!!!" + r2, String.valueOf(now.getTime()));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "MM!!!" + r2, this.MM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "MMM!!!" + r2, this.MMM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "MMMM!!!" + r2, this.MMMM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "dd!!!" + r2, this.dd.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "d!!!" + r2, this.d_.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "yy!!!" + r2, this.yy.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "yyyy!!!" + r2, this.yyyy.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "mm!!!" + r2, this.mm.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "hh!!!" + r2, this.hh.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "HH!!!" + r2, this.HH.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "k!!!" + r2, this.k.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "K!!!" + r2, this.K.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "ss!!!" + r2, this.ss.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "aa!!!" + r2, this.aa.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "S!!!" + r2, this.S.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "SSS!!!" + r2, this.SSS.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "EEE!!!" + r2, this.EEE.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "EEEE!!!" + r2, this.EEEE.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "u!!!" + r2, String.valueOf(nextmonth.get(7)));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "Z!!!" + r2, this.Z.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "z!!!" + r2, this.z.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "DD!!!" + r2, this.DD.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "D!!!" + r2, this.D.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "F!!!" + r2, this.F.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "w!!!" + r2, this.w.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "W!!!" + r2, this.W.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "J!!!" + r2, String.valueOf(Variables.date_to_julian(now)));
        now = new Date();
        c.setTime(now);
        if (dateadd != 0) {
            now = Variables.getDateAdd(now, dateadd);
        }
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".MM" + r2, this.MM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".MMM" + r2, this.MMM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".MMMM" + r2, this.MMMM.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".dd" + r2, this.dd.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".d" + r2, this.d_.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".yy" + r2, this.yy.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".yyyy" + r2, this.yyyy.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".mm" + r2, this.mm.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".hh" + r2, this.hh.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".HH" + r2, this.HH.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".k" + r2, this.k.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".K" + r2, this.K.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".ss" + r2, this.ss.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".aa" + r2, this.aa.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".S" + r2, this.S.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".SSS" + r2, this.SSS.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".EEE" + r2, this.EEE.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".EEEE" + r2, this.EEEE.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".u" + r2, String.valueOf(c.get(7)));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".Z" + r2, this.Z.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".z" + r2, this.z.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".DD" + r2, this.DD.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".D" + r2, this.D.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".F" + r2, this.F.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".w" + r2, this.w.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".W" + r2, this.W.format(now));
        the_line = Common.replace_str(the_line, String.valueOf(r1) + ".J" + r2, String.valueOf(Variables.date_to_julian(now)));
        try {
            now = new Date(Long.parseLong(item.getProperty("the_file_start")));
            if (dateadd != 0) {
                now = Variables.getDateAdd(now, dateadd);
            }
            c.setTime(now);
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "MM_" + r2, this.MM.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "MMM_" + r2, this.MMM.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "MMMM_" + r2, this.MMMM.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "dd_" + r2, this.dd.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "d_" + r2, this.d_.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "yy_" + r2, this.yy.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "yyyy_" + r2, this.yyyy.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "mm_" + r2, this.mm.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "hh_" + r2, this.hh.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "HH_" + r2, this.HH.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "k_" + r2, this.k.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "K_" + r2, this.K.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "ss_" + r2, this.ss.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "aa_" + r2, this.aa.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "S_" + r2, this.S.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "SSS_" + r2, this.SSS.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "EEE_" + r2, this.EEE.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "EEEE_" + r2, this.EEEE.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "u_" + r2, String.valueOf(c.get(7)));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "Z_" + r2, this.Z.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "z_" + r2, this.z.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "DD_" + r2, this.DD.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "D_" + r2, this.D.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "F_" + r2, this.F.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "w_" + r2, this.w.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "W_" + r2, this.W.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "J_" + r2, String.valueOf(Variables.date_to_julian(now)));
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            now = new Date(Long.parseLong(item.getProperty("the_file_end")));
            if (dateadd != 0) {
                now = Variables.getDateAdd(now, dateadd);
            }
            c.setTime(now);
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_MM" + r2, this.MM.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_MMM" + r2, this.MMM.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_MMMM" + r2, this.MMMM.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_dd" + r2, this.dd.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_d" + r2, this.d_.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_yy" + r2, this.yy.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_yyyy" + r2, this.yyyy.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_mm" + r2, this.mm.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_hh" + r2, this.hh.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_HH" + r2, this.HH.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_k" + r2, this.k.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_K" + r2, this.K.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_ss" + r2, this.ss.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_aa" + r2, this.aa.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_S" + r2, this.S.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_SSS" + r2, this.SSS.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_EEE" + r2, this.EEE.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_EEEE" + r2, this.EEEE.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_u" + r2, String.valueOf(c.get(7)));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_Z" + r2, this.Z.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_z" + r2, this.z.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_DD" + r2, this.DD.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_D" + r2, this.D.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_F" + r2, this.F.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_w" + r2, this.w.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_W" + r2, this.W.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "_J" + r2, String.valueOf(Variables.date_to_julian(now)));
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            now = new Date(Long.parseLong(item.getProperty("modified", String.valueOf(System.currentTimeMillis()))));
            if (dateadd != 0) {
                now = Variables.getDateAdd(now, dateadd);
            }
            c.setTime(now);
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-MM" + r2, this.MM.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-MMM" + r2, this.MMM.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-MMMM" + r2, this.MMMM.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-dd" + r2, this.dd.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-d" + r2, this.d_.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-yy" + r2, this.yy.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-yyyy" + r2, this.yyyy.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-mm" + r2, this.mm.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-hh" + r2, this.hh.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-HH" + r2, this.HH.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-k" + r2, this.k.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-K" + r2, this.K.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-ss" + r2, this.ss.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-aa" + r2, this.aa.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-S" + r2, this.S.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-SSS" + r2, this.SSS.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-EEE" + r2, this.EEE.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-EEEE" + r2, this.EEEE.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-u" + r2, String.valueOf(c.get(7)));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-Z" + r2, this.Z.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-z" + r2, this.z.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-DD" + r2, this.DD.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-D" + r2, this.D.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-F" + r2, this.F.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-w" + r2, this.w.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-W" + r2, this.W.format(now));
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "-J" + r2, String.valueOf(Variables.date_to_julian(now)));
        }
        catch (Exception exception) {
            // empty catch block
        }
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "r" + r2, "\r");
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "n" + r2, "\n");
        the_line = Common.replace_str(the_line, String.valueOf(r1) + "t" + r2, "\t");
        if (the_line.indexOf(String.valueOf(r1) + "plus" + r2) > 0) {
            the_line = Common.replace_str(the_line, String.valueOf(r1) + "plus" + r2, "+");
        }
        return the_line;
    }

    public static Date getDateAdd(Date d_tmp, int days) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d_tmp);
        gc.add(5, days);
        return gc.getTime();
    }

    public static String uid() {
        String chars = "1234567890";
        String rand = "";
        int i = 0;
        while (i < 8) {
            rand = String.valueOf(rand) + chars.charAt((int)(Math.random() * (double)(chars.length() - 1)));
            ++i;
        }
        return rand;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static String uidg() {
        var0 = Variables.uidg_lock;
        synchronized (var0) {
            while (true) {
                block7: {
                    if (Variables.recent_guids.indexOf(String.valueOf(System.currentTimeMillis() / 1000L)) >= 0) break block7;
                    Variables.UID_GLOBAL = System.currentTimeMillis() / 1000L;
                    Variables.recent_guids.addElement(String.valueOf(Variables.UID_GLOBAL));
                    if (true) ** GOTO lbl20
                }
                try {
                    Thread.sleep(100L);
                }
                catch (Exception var1_1) {
                    // empty catch block
                }
            }
            do {
                Variables.recent_guids.removeElementAt(0);
lbl20:
                // 2 sources

            } while (Variables.recent_guids.size() > 0 && System.currentTimeMillis() - Long.parseLong("" + Variables.recent_guids.elementAt(0)) > 10800000L);
            return String.valueOf(Variables.UID_GLOBAL);
        }
    }

    public static double eval_math(String str) {
        class Parser {
            int pos = -1;
            int c;
            private final /* synthetic */ String val$str;

            Parser(String string) {
                this.val$str = string;
            }

            void eatChar() {
                this.c = ++this.pos < this.val$str.length() ? (int)this.val$str.charAt(this.pos) : -1;
            }

            void eatSpace() {
                while (Character.isWhitespace(this.c)) {
                    this.eatChar();
                }
            }

            double parse() {
                this.eatChar();
                double v = this.parseExpression();
                if (this.c != -1) {
                    throw new RuntimeException("Unexpected: " + (char)this.c);
                }
                return v;
            }

            double parseExpression() {
                double v = this.parseTerm();
                while (true) {
                    this.eatSpace();
                    if (this.c == 43) {
                        this.eatChar();
                        v += this.parseTerm();
                        continue;
                    }
                    if (this.c != 45) break;
                    this.eatChar();
                    v -= this.parseTerm();
                }
                return v;
            }

            double parseTerm() {
                double v = this.parseFactor();
                while (true) {
                    this.eatSpace();
                    if (this.c == 47) {
                        this.eatChar();
                        v /= this.parseFactor();
                        continue;
                    }
                    if (this.c != 42 && this.c != 40) break;
                    if (this.c == 42) {
                        this.eatChar();
                    }
                    v *= this.parseFactor();
                }
                return v;
            }

            double parseFactor() {
                double v;
                boolean negate = false;
                this.eatSpace();
                if (this.c == 40) {
                    this.eatChar();
                    v = this.parseExpression();
                    if (this.c == 41) {
                        this.eatChar();
                    }
                } else {
                    if (this.c == 43 || this.c == 45) {
                        negate = this.c == 45;
                        this.eatChar();
                        this.eatSpace();
                    }
                    StringBuilder sb = new StringBuilder();
                    while (this.c >= 48 && this.c <= 57 || this.c == 46) {
                        sb.append((char)this.c);
                        this.eatChar();
                    }
                    if (sb.length() == 0) {
                        throw new RuntimeException("Unexpected: " + (char)this.c);
                    }
                    v = Double.parseDouble(sb.toString());
                }
                this.eatSpace();
                if (this.c == 94) {
                    this.eatChar();
                    v = Math.pow(v, this.parseFactor());
                }
                if (negate) {
                    v = -v;
                }
                return v;
            }
        }
        return new Parser(str).parse();
    }

    public static double date_to_julian(Date d) {
        GregorianCalendar date = new GregorianCalendar();
        date.setTime(d);
        double extra = 100.0 * (double)date.get(1) + (double)(date.get(2) + 1) - 190002.5;
        return 367.0 * (double)date.get(1) - Math.floor(7.0 * ((double)date.get(1) + Math.floor(((double)(date.get(2) + 1) + 9.0) / 12.0)) / 4.0) + Math.floor(275.0 * (double)(date.get(2) + 1) / 9.0) + (double)date.get(5) + ((double)date.get(11) + ((double)date.get(12) + (double)date.get(13) / 60.0) / 60.0) / 24.0 + 1721013.5 - 0.5 * extra / Math.abs(extra) + 0.5;
    }
}

