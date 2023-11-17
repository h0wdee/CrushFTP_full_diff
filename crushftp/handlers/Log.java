/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import com.crushftp.client.Common;
import com.crushftp.client.VRL;
import crushftp.server.ServerStatus;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

public class Log {
    public static Vector dmz_log_queue = new Vector();

    public static boolean log(String tag, int level, String s) {
        return Log.log(tag, level, s, System.currentTimeMillis());
    }

    public static boolean log(String tag, int level, String s, long time) {
        if (ServerStatus.IG("log_debug_level") >= level) {
            block20: {
                try {
                    int end;
                    int start;
                    String url;
                    if (s.trim().length() <= 0) break block20;
                    try {
                        if (ServerStatus.BG("dmz_log_in_internal_server") && Common.dmz_mode) {
                            Properties action = new Properties();
                            action.put("tag", tag);
                            action.put("level", String.valueOf(level));
                            action.put("t", String.valueOf(System.currentTimeMillis()));
                            action.put("message", s);
                            dmz_log_queue.addElement(action);
                            while (dmz_log_queue.size() > 5000) {
                                dmz_log_queue.remove(0);
                            }
                        }
                    }
                    catch (NullPointerException action) {
                        // empty catch block
                    }
                    if (ServerStatus.thisObj != null && ServerStatus.thisObj.logDateFormat != null) {
                        s = String.valueOf(ServerStatus.thisObj.logDateFormat.format(new Date(time))) + "|" + s;
                    }
                    Properties p = new Properties();
                    p.put("tag", tag);
                    p.put("level", String.valueOf(level));
                    if (s.indexOf("url=") >= 0) {
                        url = s.substring(s.indexOf("url=") + 4).trim();
                        if (url.indexOf(",") >= 0) {
                            url = url.indexOf("@") >= url.indexOf(",") && url.indexOf(",", url.indexOf("@")) > 0 ? url.substring(0, url.indexOf(",", url.indexOf("@"))).trim() : url.substring(0, url.indexOf(",")).trim();
                        }
                        try {
                            VRL vrl = new VRL(url);
                            if (vrl.getUsername() != null && !vrl.getUsername().equals("")) {
                                String url2 = String.valueOf(vrl.getProtocol()) + "://" + vrl.getUsername() + ":********" + "@" + vrl.getHost() + ":" + vrl.getPort() + vrl.getPath();
                                s = Common.replace_str(s, url, url2);
                            }
                        }
                        catch (Exception vrl) {
                            // empty catch block
                        }
                    }
                    if (s.indexOf("password=") >= 0) {
                        url = s.substring(s.indexOf("password=") + 9).trim();
                        if (url.indexOf(",") >= 0) {
                            if (url.indexOf("@") >= 0) {
                                int loc = url.indexOf(",", url.indexOf("@"));
                                if (loc < 0) {
                                    loc = url.indexOf(", ");
                                }
                                url = url.substring(0, loc).trim();
                            } else {
                                url = url.substring(0, url.indexOf(",")).trim();
                            }
                        }
                        s = Common.replace_str(s, url, "************");
                    }
                    if (s.indexOf("(pgpPrivateKeyDownloadPassword=") > 0 && !s.substring(start = s.indexOf("(pgpPrivateKeyDownloadPassword=") + "(pgpPrivateKeyDownloadPassword=".length(), end = s.indexOf(")", start)).equals("")) {
                        s = Common.replace_str(s, s.substring(start, end), "************");
                    }
                    if (s.indexOf("(pgpPrivateKeyUploadPassword=") > 0 && !s.substring(start = s.indexOf("(pgpPrivateKeyUploadPassword=") + "(pgpPrivateKeyUploadPassword=".length(), end = s.indexOf(")", start)).equals("")) {
                        s = Common.replace_str(s, s.substring(start, end), "************");
                    }
                    p.put("data", s);
                    Common.log.addElement(p);
                }
                catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        return false;
    }

    public static boolean log(String tag, int level, Throwable e) {
        if (ServerStatus.IG("log_debug_level") >= level) {
            Log.log(tag, level, Thread.currentThread().getName());
            Log.log(tag, level, e.toString());
            StackTraceElement[] ste = e.getStackTrace();
            int x = 0;
            while (x < ste.length) {
                Log.log(tag, level, String.valueOf(ste[x].getClassName()) + "." + ste[x].getMethodName() + ":" + ste[x].getLineNumber());
                ++x;
            }
            if (level >= 2) {
                System.out.println(new Date());
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public static boolean log(String tag, int level, Exception e) {
        return Log.log(tag, level, (Throwable)e);
    }
}

