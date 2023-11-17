/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import com.crushftp.client.File_S;
import com.crushftp.client.Variables;
import crushftp.gui.LOC;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.handlers.SessionCrush;
import crushftp.handlers.UserTools;
import crushftp.server.ServerStatus;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class AlertTools {
    /*
     * Unable to fully structure code
     */
    public static void runAlerts(String alert_action, Properties info, Properties user_info, Properties user, SessionCrush the_user, Properties the_alert, boolean dmz_mode) {
        hours_key = "hours";
        alerts = ServerStatus.VG("alerts");
        if (user_info == null && the_user != null) {
            user_info = the_user.user_info;
        }
        ok = false;
        x = 0;
        while (x < alerts.size()) {
            block180: {
                block182: {
                    block191: {
                        block190: {
                            block189: {
                                block188: {
                                    block187: {
                                        block186: {
                                            block185: {
                                                block184: {
                                                    block183: {
                                                        block181: {
                                                            p = (Properties)alerts.elementAt(x);
                                                            if (p.getProperty("type").equalsIgnoreCase("Disk Space Below Threshold") && alert_action.equals("disk")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("Variable Watcher") && alert_action.equals("variables")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase(String.valueOf(System.getProperty("appname", "CrushFTP")) + " Update Available") && alert_action.equals("update")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase(String.valueOf(System.getProperty("appname", "CrushFTP")) + " Started") && alert_action.equals("started")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("User reached quota percentage") && alert_action.equals("user_upload_session")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("User Exceeded Upload Transfer Amount Per Session") && alert_action.equals("user_upload_session")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("User Exceeded Upload Transfer Amount Per Day") && alert_action.equals("user_upload_day")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("User Exceeded Upload Transfer Amount Per Month") && alert_action.equals("user_upload_month")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("User Exceeded Download Transfer Amount Per Session") && alert_action.equals("user_download_session")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("User Download Speed Below Minimum") && alert_action.equals("user_download_speed")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("User Upload Speed Below Minimum") && alert_action.equals("user_upload_speed")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("User Exceeded Download Transfer Amount Per Day") && alert_action.equals("user_download_day")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("User Exceeded Download Transfer Amount Per Month") && alert_action.equals("user_download_month")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("Proxy Blacklisted Site Attempted") && alert_action.equals("proxy_blacklist")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("IP Banned for Failed Logins") && alert_action.equals("ip_banned_logins")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("User Changed Password") && alert_action.equals("password_change")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("User Updated TwoFactor Secret") && alert_action.equals("twofactor_secret_change")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("Server Port Failed") && alert_action.equals("server_port_error")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("Invalid Email Attempted") && alert_action.equals("invalid_email")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("User Hammering") && alert_action.equals("user_hammering")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("Plugin Message") && alert_action.startsWith("pluginMessage_")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("Security Alert") && alert_action.equals("security_alert")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("Low Memory") != false && alert_action.equals("low_memory") != false || p.getProperty("type").equalsIgnoreCase("Low Memory1") != false && alert_action.equals("low_memory1") != false || p.getProperty("type").equalsIgnoreCase("Low Memory2") != false && alert_action.equals("low_memory2") != false || p.getProperty("type").equalsIgnoreCase("Low Memory3") && alert_action.equals("low_memory3")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("Big Directory") && alert_action.equals("big_dir")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("ServerBeat Alert") && alert_action.equals("serverbeat_alert")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("Slow Login") && alert_action.equals("slow_login")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("Login Frequency") && alert_action.equals("login_frequency")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("Login Frequency Banned") && alert_action.equals("login_frequency_banned")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("Job Changes") && alert_action.equals("job_update")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("Update Object") && alert_action.equals("update_object")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("VFS Bad Credentials") && alert_action.equals("vfs_bad_credentials")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("Repeated Login Failure") && alert_action.equals("repeated_login_failure")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("Repeated Login Failure User Banned") && alert_action.equals("repeated_login_failure_user_banned")) {
                                                                ok = true;
                                                            } else if (p.getProperty("type").equalsIgnoreCase("Skipped scheduled job") && alert_action.equals("skipped_scheduled_job")) {
                                                                ok = true;
                                                            }
                                                            if (!ok) break block180;
                                                            the_alert = p;
                                                            Log.log("ALERT", 0, "Checking for alert:" + alert_action);
                                                            recent_drives = (Properties)ServerStatus.thisObj.server_info.get("recent_drives");
                                                            if (recent_drives == null) {
                                                                recent_drives = new Properties();
                                                            }
                                                            subject = p.getProperty("subject", "");
                                                            body = p.getProperty("body", "");
                                                            to = p.getProperty("to", "");
                                                            cc = p.getProperty("cc", "");
                                                            bcc = p.getProperty("bcc", "");
                                                            from = p.getProperty("from", "");
                                                            if (!p.getProperty("type").equalsIgnoreCase("Disk Space Below Threshold") || !alert_action.equals("disk")) break block181;
                                                            drive = p.getProperty("drive", "/");
                                                            mb = Long.parseLong(p.getProperty("threshold_mb", "0")) * 1024L * 1024L;
                                                            free_bytes = Common.get_free_disk_space(drive);
                                                            ServerStatus.thisObj.server_info.put("recent_drives", recent_drives);
                                                            recent_drives.put(drive, String.valueOf(com.crushftp.client.Common.format_bytes_short2(free_bytes)) + " free");
                                                            subject = Common.replace_str(subject, "%free_bytes%", com.crushftp.client.Common.format_bytes_short2(free_bytes));
                                                            body = Common.replace_str(body, "%free_bytes%", com.crushftp.client.Common.format_bytes_short2(free_bytes));
                                                            subject = Common.replace_str(subject, "{free_bytes}", com.crushftp.client.Common.format_bytes_short2(free_bytes));
                                                            body = Common.replace_str(body, "{free_bytes}", com.crushftp.client.Common.format_bytes_short2(free_bytes));
                                                            if (free_bytes > mb) {
                                                                ok = false;
                                                            }
                                                            break block182;
                                                        }
                                                        if (!p.getProperty("type").equalsIgnoreCase("Repeated Login Failure") || !alert_action.equals("repeated_login_failure")) break block183;
                                                        failure_count = Integer.parseInt(user.getProperty("failure_count", "0")) + 1;
                                                        if (failure_count <= Integer.parseInt(p.getProperty("failure_count", "10"))) {
                                                            ok = false;
                                                        } else {
                                                            body = Common.replace_str(body, "{alert_msg}", info.getProperty("alert_msg", "0"));
                                                            subject = Common.replace_str(subject, "{alert_msg}", info.getProperty("alert_msg", "0"));
                                                            body = Common.replace_str(body, "{alert_msg2}", info.getProperty("alert_msg2", "0"));
                                                            subject = Common.replace_str(subject, "{alert_msg2}", info.getProperty("alert_msg2", "0"));
                                                            body = Common.replace_str(body, "{count}", info.getProperty("count", "0"));
                                                            subject = Common.replace_str(subject, "{count}", info.getProperty("count", "0"));
                                                            body = Common.replace_str(body, "{interval}", info.getProperty("interval", p.getProperty("login_interval")));
                                                            subject = Common.replace_str(subject, "{interval}", info.getProperty("interval", p.getProperty("login_interval")));
                                                            body = Common.replace_str(body, "{attempts}", info.getProperty("attempts", "0"));
                                                            subject = Common.replace_str(subject, "{attempts}", info.getProperty("attempts", "0"));
                                                            body = Common.replace_str(body, "{timeout}", info.getProperty("timeout", "0"));
                                                            subject = Common.replace_str(subject, "{timeout}", info.getProperty("timeout", "0"));
                                                            body = Common.replace_str(body, "{user_name}", info.getProperty("user_name", "0"));
                                                            subject = Common.replace_str(subject, "{user_name}", info.getProperty("user_name", "0"));
                                                            body = Common.replace_str(body, "{username}", info.getProperty("user_name", "0"));
                                                            subject = Common.replace_str(subject, "{username}", info.getProperty("user_name", "0"));
                                                            body = Common.replace_str(body, "{expire}", info.getProperty("expire", "0"));
                                                            subject = Common.replace_str(subject, "{expire}", info.getProperty("expire", "0"));
                                                            body = Common.replace_str(body, "{alert_login_interval}", String.valueOf(p.getProperty("login_interval")));
                                                            subject = Common.replace_str(subject, "{alert_login_interval}", String.valueOf(p.getProperty("login_interval")));
                                                            hours_key = "hours_" + info.getProperty("user_name", "0");
                                                        }
                                                        break block182;
                                                    }
                                                    if (!p.getProperty("type").equalsIgnoreCase("Repeated Login Failure User Banned") || !alert_action.equals("repeated_login_failure_user_banned")) break block184;
                                                    body = Common.replace_str(body, "{alert_msg}", info.getProperty("alert_msg", "0"));
                                                    subject = Common.replace_str(subject, "{alert_msg}", info.getProperty("alert_msg", "0"));
                                                    body = Common.replace_str(body, "{alert_msg2}", info.getProperty("alert_msg2", "0"));
                                                    subject = Common.replace_str(subject, "{alert_msg2}", info.getProperty("alert_msg2", "0"));
                                                    body = Common.replace_str(body, "{count}", info.getProperty("count", "0"));
                                                    subject = Common.replace_str(subject, "{count}", info.getProperty("count", "0"));
                                                    body = Common.replace_str(body, "{interval}", info.getProperty("interval", "0"));
                                                    subject = Common.replace_str(subject, "{interval}", info.getProperty("interval", "0"));
                                                    body = Common.replace_str(body, "{attempts}", info.getProperty("attempts", "0"));
                                                    subject = Common.replace_str(subject, "{attempts}", info.getProperty("attempts", "0"));
                                                    body = Common.replace_str(body, "{timeout}", info.getProperty("timeout", "0"));
                                                    subject = Common.replace_str(subject, "{timeout}", info.getProperty("timeout", "0"));
                                                    body = Common.replace_str(body, "{user_name}", info.getProperty("user_name", "0"));
                                                    subject = Common.replace_str(subject, "{user_name}", info.getProperty("user_name", "0"));
                                                    body = Common.replace_str(body, "{username}", info.getProperty("user_name", "0"));
                                                    subject = Common.replace_str(subject, "{username}", info.getProperty("user_name", "0"));
                                                    body = Common.replace_str(body, "{expire}", info.getProperty("expire", "0"));
                                                    subject = Common.replace_str(subject, "{expire}", info.getProperty("expire", "0"));
                                                    hours_key = "hours_" + info.getProperty("user_name", "0");
                                                    break block182;
                                                }
                                                if (!p.getProperty("type").equalsIgnoreCase("Variable Watcher") || !alert_action.equals("variables")) break block185;
                                                ok = false;
                                                cond = p.getProperty("variableCondition", "equals");
                                                var1 = ServerStatus.change_vars_to_values_static(p.getProperty("variable1", ""), null, new Properties(), null);
                                                var2 = ServerStatus.change_vars_to_values_static(p.getProperty("variable2", ""), null, new Properties(), null);
                                                keys = ServerStatus.thisObj.server_info.keys();
                                                while (keys.hasMoreElements()) {
                                                    key = keys.nextElement().toString();
                                                    val = ServerStatus.thisObj.server_info.getProperty(key);
                                                    if (var1.indexOf("%server_" + key + "%") >= 0) {
                                                        var1 = Common.replace_str(var1, "%server_" + key + "%", String.valueOf(val));
                                                    }
                                                    if (var1.indexOf("%" + key + "%") >= 0) {
                                                        var1 = Common.replace_str(var1, "%" + key + "%", String.valueOf(val));
                                                    }
                                                    if (var2.indexOf("%server_" + key + "%") >= 0) {
                                                        var2 = Common.replace_str(var2, "%server_" + key + "%", String.valueOf(val));
                                                    }
                                                    if (var2.indexOf("%" + key + "%") >= 0) {
                                                        var2 = Common.replace_str(var2, "%" + key + "%", String.valueOf(val));
                                                    }
                                                    if (var1.indexOf("{server_" + key + "}") >= 0) {
                                                        var1 = Common.replace_str(var1, "{server_" + key + "}", String.valueOf(val));
                                                    }
                                                    if (var1.indexOf("{" + key + "}") >= 0) {
                                                        var1 = Common.replace_str(var1, "{" + key + "}", String.valueOf(val));
                                                    }
                                                    if (var2.indexOf("{server_" + key + "}") >= 0) {
                                                        var2 = Common.replace_str(var2, "{server_" + key + "}", String.valueOf(val));
                                                    }
                                                    if (var2.indexOf("{" + key + "}") < 0) continue;
                                                    var2 = Common.replace_str(var2, "{" + key + "}", String.valueOf(val));
                                                }
                                                if (cond.equals("equals")) {
                                                    if (var1.equals(var2)) {
                                                        ok = true;
                                                    }
                                                } else if (cond.equals("contains")) {
                                                    if (var1.indexOf(var2) >= 0) {
                                                        ok = true;
                                                    }
                                                } else if (cond.equals("matches pattern")) {
                                                    if (com.crushftp.client.Common.do_search(var2, var1, false, 0)) {
                                                        ok = true;
                                                    }
                                                } else if (cond.equals("doesn't equal") || cond.equals("!equal")) {
                                                    if (!var1.equals(var2)) {
                                                        ok = true;
                                                    }
                                                } else if (cond.equals("doesn't contain") || cond.equals("!contain")) {
                                                    if (var1.indexOf(var2) < 0) {
                                                        ok = true;
                                                    }
                                                } else if (cond.equals("doesn't match pattern") || cond.equals("!match pattern")) {
                                                    if (!com.crushftp.client.Common.do_search(var2, var1, false, 0)) {
                                                        ok = true;
                                                    }
                                                } else if (cond.equals("greater than")) {
                                                    try {
                                                        if (Float.parseFloat(var1.trim()) > Float.parseFloat(var2.trim())) {
                                                            ok = true;
                                                        }
                                                    }
                                                    catch (Exception key) {}
                                                } else if (cond.equals("less than")) {
                                                    try {
                                                        if (Float.parseFloat(var1.trim()) < Float.parseFloat(var2.trim())) {
                                                            ok = true;
                                                        }
                                                    }
                                                    catch (Exception key) {}
                                                } else if (cond.equals("greater than or equal")) {
                                                    try {
                                                        if (Float.parseFloat(var1.trim()) >= Float.parseFloat(var2.trim())) {
                                                            ok = true;
                                                        }
                                                    }
                                                    catch (Exception key) {}
                                                } else if (cond.equals("less than or equal")) {
                                                    try {
                                                        if (Float.parseFloat(var1.trim()) <= Float.parseFloat(var2.trim())) {
                                                            ok = true;
                                                        }
                                                    }
                                                    catch (Exception key) {
                                                        // empty catch block
                                                    }
                                                }
                                                subject = Common.replace_str(subject, "%var1%", var1);
                                                subject = Common.replace_str(subject, "%var2%", var2);
                                                subject = Common.replace_str(subject, "%condition%", cond);
                                                subject = Common.replace_str(subject, "{var1}", var1);
                                                subject = Common.replace_str(subject, "{var2}", var2);
                                                subject = Common.replace_str(subject, "{condition}", cond);
                                                body = Common.replace_str(body, "%var1%", var1);
                                                body = Common.replace_str(body, "%var2%", var2);
                                                body = Common.replace_str(body, "%condition%", cond);
                                                body = Common.replace_str(body, "{var1}", var1);
                                                body = Common.replace_str(body, "{var2}", var2);
                                                body = Common.replace_str(body, "{condition}", cond);
                                                break block182;
                                            }
                                            if (p.getProperty("type").equalsIgnoreCase(String.valueOf(System.getProperty("appname", "CrushFTP")) + " Update Available") && alert_action.equals("update")) break block182;
                                            if (!p.getProperty("type").equalsIgnoreCase(String.valueOf(System.getProperty("appname", "CrushFTP")) + " Started") || !alert_action.equals("started")) break block186;
                                            subject = Common.replace_str(subject, "{message}", ServerStatus.hostname);
                                            body = Common.replace_str(body, "{message}", ServerStatus.hostname);
                                            to = Common.replace_str(to, "{message}", ServerStatus.hostname);
                                            cc = Common.replace_str(cc, "{message}", ServerStatus.hostname);
                                            bcc = Common.replace_str(bcc, "{message}", ServerStatus.hostname);
                                            from = Common.replace_str(from, "{message}", ServerStatus.hostname);
                                            subject = Common.replace_str(subject, "{host_name}", ServerStatus.hostname);
                                            body = Common.replace_str(body, "{host_name}", ServerStatus.hostname);
                                            to = Common.replace_str(to, "{host_name}", ServerStatus.hostname);
                                            cc = Common.replace_str(cc, "{host_name}", ServerStatus.hostname);
                                            bcc = Common.replace_str(bcc, "{host_name}", ServerStatus.hostname);
                                            from = Common.replace_str(from, "{host_name}", ServerStatus.hostname);
                                            break block182;
                                        }
                                        if (!p.getProperty("type").equalsIgnoreCase("User reached quota percentage") || !alert_action.equals("user_upload_session")) break block187;
                                        path = user_info.getProperty("current_dir");
                                        try {
                                            used = the_user.get_quota_used(path);
                                        }
                                        catch (Exception e) {
                                            ok = false;
                                            p.remove("no_email");
                                            used = -12345L;
                                        }
                                        try {
                                            total = the_user.get_total_quota(path);
                                        }
                                        catch (Exception e) {
                                            ok = false;
                                            p.remove("no_email");
                                            total = -12345L;
                                        }
                                        if (total != -12345L && used >= 0L) {
                                            perc = used * 100L;
                                            perc /= total;
                                        } else {
                                            perc = -1L;
                                        }
                                        if (perc >= (long)Integer.parseInt(p.getProperty("quota_perc", ""))) {
                                            body = String.valueOf(body) + "\nPercentage of quota has been reached for" + path;
                                        } else {
                                            ok = false;
                                        }
                                        break block182;
                                    }
                                    if (p.getProperty("type").equalsIgnoreCase("User Exceeded Upload Transfer Amount Per Session") && alert_action.equals("user_upload_session") || p.getProperty("type").equalsIgnoreCase("User Exceeded Upload Transfer Amount Per Day") && alert_action.equals("user_upload_day") || p.getProperty("type").equalsIgnoreCase("User Exceeded Upload Transfer Amount Per Month") && alert_action.equals("user_upload_month") || p.getProperty("type").equalsIgnoreCase("User Exceeded Download Transfer Amount Per Session") && alert_action.equals("user_download_session") || p.getProperty("type").equalsIgnoreCase("User Download Speed Below Minimum") && alert_action.equals("user_download_speed") || p.getProperty("type").equalsIgnoreCase("User Upload Speed Below Minimum") && alert_action.equals("user_upload_speed") || p.getProperty("type").equalsIgnoreCase("User Exceeded Download Transfer Amount Per Day") && alert_action.equals("user_download_day") || p.getProperty("type").equalsIgnoreCase("User Exceeded Download Transfer Amount Per Month") && alert_action.equals("user_download_month") || p.getProperty("type").equalsIgnoreCase("Proxy Blacklisted Site Attempted") && alert_action.equals("proxy_blacklist")) break block182;
                                    if (!p.getProperty("type").equalsIgnoreCase("IP Banned for Failed Logins") || !alert_action.equals("ip_banned_logins")) break block188;
                                    ip_restrictions = null;
                                    ip_restrictions = !ServerStatus.BG("save_temp_bans") && !info.getProperty("alert_timeout", "0").equals("0") ? ServerStatus.siVG("ip_restrictions_temp") : (Vector)ServerStatus.server_settings.get("ip_restrictions");
                                    value = ((Properties)ip_restrictions.get(0)).getProperty("reason");
                                    subject = Common.replace_str(subject, "%msg%", value);
                                    body = Common.replace_str(body, "%msg%", value);
                                    to = Common.replace_str(to, "%msg%", value);
                                    cc = Common.replace_str(cc, "%msg%", value);
                                    bcc = Common.replace_str(bcc, "%msg%", value);
                                    from = Common.replace_str(from, "%msg%", value);
                                    subject = Common.replace_str(subject, "{msg}", value);
                                    body = Common.replace_str(body, "{msg}", value);
                                    to = Common.replace_str(to, "{msg}", value);
                                    cc = Common.replace_str(cc, "{msg}", value);
                                    bcc = Common.replace_str(bcc, "{msg}", value);
                                    from = Common.replace_str(from, "{msg}", value);
                                    break block182;
                                }
                                if (p.getProperty("type").equalsIgnoreCase("User Changed Password") && alert_action.equals("password_change") || p.getProperty("type").equalsIgnoreCase("User Updated TwoFactor Secret") && alert_action.equals("twofactor_secret_change")) break block182;
                                if (!p.getProperty("type").equalsIgnoreCase("Server Port Failed") || !alert_action.equals("server_port_error")) break block189;
                                subject = Common.replace_str(subject, "%msg%", String.valueOf(info.getProperty("alert_msg", "")) + " Error " + info.getProperty("alert_error", ""));
                                body = Common.replace_str(body, "%msg%", String.valueOf(info.getProperty("alert_msg", "")) + " Error " + info.getProperty("alert_error", ""));
                                to = Common.replace_str(to, "%msg%", String.valueOf(info.getProperty("alert_msg", "")) + " Error " + info.getProperty("alert_error", ""));
                                cc = Common.replace_str(cc, "%msg%", String.valueOf(info.getProperty("alert_msg", "")) + " Error " + info.getProperty("alert_error", ""));
                                bcc = Common.replace_str(bcc, "%msg%", String.valueOf(info.getProperty("alert_msg", "")) + " Error " + info.getProperty("alert_error", ""));
                                from = Common.replace_str(from, "%msg%", String.valueOf(info.getProperty("alert_msg", "")) + " Error " + info.getProperty("alert_error", ""));
                                subject = Common.replace_str(subject, "{message}", String.valueOf(info.getProperty("alert_msg", "")) + " Error " + info.getProperty("alert_error", ""));
                                body = Common.replace_str(body, "{message}", String.valueOf(info.getProperty("alert_msg", "")) + " Error " + info.getProperty("alert_error", ""));
                                to = Common.replace_str(to, "{message}", String.valueOf(info.getProperty("alert_msg", "")) + " Error " + info.getProperty("alert_error", ""));
                                cc = Common.replace_str(cc, "{message}", String.valueOf(info.getProperty("alert_msg", "")) + " Error " + info.getProperty("alert_error", ""));
                                bcc = Common.replace_str(bcc, "{message}", String.valueOf(info.getProperty("alert_msg", "")) + " Error " + info.getProperty("alert_error", ""));
                                from = Common.replace_str(from, "{message}", String.valueOf(info.getProperty("alert_msg", "")) + " Error " + info.getProperty("alert_error", ""));
                                subject = Common.replace_str(subject, "{server_port}", info.getProperty("alert_msg", ""));
                                body = Common.replace_str(body, "{server_port}", info.getProperty("alert_msg", ""));
                                to = Common.replace_str(to, "{server_port}", info.getProperty("alert_msg", ""));
                                cc = Common.replace_str(cc, "{server_port}", info.getProperty("alert_msg", ""));
                                bcc = Common.replace_str(bcc, "{server_port}", info.getProperty("alert_msg", ""));
                                from = Common.replace_str(from, "{server_port}", info.getProperty("alert_msg", ""));
                                subject = Common.replace_str(subject, "{server_port_error}", info.getProperty("alert_error", ""));
                                body = Common.replace_str(body, "{server_port_error}", info.getProperty("alert_error", ""));
                                to = Common.replace_str(to, "{server_port_error}", info.getProperty("alert_error", ""));
                                cc = Common.replace_str(cc, "{server_port_error}", info.getProperty("alert_error", ""));
                                bcc = Common.replace_str(bcc, "{server_port_error}", info.getProperty("alert_error", ""));
                                from = Common.replace_str(from, "{server_port_error}", info.getProperty("alert_error", ""));
                                subject = Common.replace_str(subject, "{host_name}", ServerStatus.hostname);
                                body = Common.replace_str(body, "{host_name}", ServerStatus.hostname);
                                to = Common.replace_str(to, "{host_name}", ServerStatus.hostname);
                                cc = Common.replace_str(cc, "{host_name}", ServerStatus.hostname);
                                bcc = Common.replace_str(bcc, "{host_name}", ServerStatus.hostname);
                                from = Common.replace_str(from, "{host_name}", ServerStatus.hostname);
                                break block182;
                            }
                            if (!p.getProperty("type").equalsIgnoreCase("Invalid Email Attempted") || !alert_action.equals("invalid_email")) break block190;
                            subject = Common.replace_str(subject, "%result%", info.getProperty("result", ""));
                            subject = Common.replace_str(subject, "%subject%", info.getProperty("subject", ""));
                            subject = Common.replace_str(subject, "%body%", info.getProperty("body", ""));
                            subject = Common.replace_str(subject, "%to%", info.getProperty("to", ""));
                            subject = Common.replace_str(subject, "%cc%", info.getProperty("cc", ""));
                            subject = Common.replace_str(subject, "%bcc%", info.getProperty("bcc", ""));
                            subject = Common.replace_str(subject, "%from%", info.getProperty("from", ""));
                            body = Common.replace_str(body, "%result%", info.getProperty("result", ""));
                            body = Common.replace_str(body, "%subject%", info.getProperty("subject", ""));
                            body = Common.replace_str(body, "%body%", info.getProperty("body", ""));
                            body = Common.replace_str(body, "%to%", info.getProperty("to", ""));
                            body = Common.replace_str(body, "%cc%", info.getProperty("cc", ""));
                            body = Common.replace_str(body, "%bcc%", info.getProperty("bcc", ""));
                            body = Common.replace_str(body, "%from%", info.getProperty("from", ""));
                            to = Common.replace_str(to, "%to%", info.getProperty("to", ""));
                            to = Common.replace_str(to, "%cc%", info.getProperty("cc", ""));
                            to = Common.replace_str(to, "%bcc%", info.getProperty("bcc", ""));
                            to = Common.replace_str(to, "%from%", info.getProperty("from", ""));
                            cc = Common.replace_str(cc, "%to%", info.getProperty("to", ""));
                            cc = Common.replace_str(cc, "%cc%", info.getProperty("cc", ""));
                            cc = Common.replace_str(cc, "%bcc%", info.getProperty("bcc", ""));
                            cc = Common.replace_str(cc, "%from%", info.getProperty("from", ""));
                            bcc = Common.replace_str(bcc, "%to%", info.getProperty("to", ""));
                            bcc = Common.replace_str(bcc, "%cc%", info.getProperty("cc", ""));
                            bcc = Common.replace_str(bcc, "%bcc%", info.getProperty("bcc", ""));
                            bcc = Common.replace_str(bcc, "%from%", info.getProperty("from", ""));
                            from = Common.replace_str(from, "%to%", info.getProperty("to", ""));
                            from = Common.replace_str(from, "%cc%", info.getProperty("cc", ""));
                            from = Common.replace_str(from, "%bcc%", info.getProperty("bcc", ""));
                            from = Common.replace_str(from, "%from%", info.getProperty("from", ""));
                            subject = Common.replace_str(subject, "{result}", info.getProperty("result", ""));
                            subject = Common.replace_str(subject, "{subject}", info.getProperty("subject", ""));
                            subject = Common.replace_str(subject, "{body}", info.getProperty("body", ""));
                            subject = Common.replace_str(subject, "{to}", info.getProperty("to", ""));
                            subject = Common.replace_str(subject, "{cc}", info.getProperty("cc", ""));
                            subject = Common.replace_str(subject, "{bcc}", info.getProperty("bcc", ""));
                            subject = Common.replace_str(subject, "{from}", info.getProperty("from", ""));
                            body = Common.replace_str(body, "{result}", info.getProperty("result", ""));
                            body = Common.replace_str(body, "{subject}", info.getProperty("subject", ""));
                            body = Common.replace_str(body, "{body}", info.getProperty("body", ""));
                            body = Common.replace_str(body, "{to}", info.getProperty("to", ""));
                            body = Common.replace_str(body, "{cc}", info.getProperty("cc", ""));
                            body = Common.replace_str(body, "{bcc}", info.getProperty("bcc", ""));
                            body = Common.replace_str(body, "{from}", info.getProperty("from", ""));
                            to = Common.replace_str(to, "{to}", info.getProperty("to", ""));
                            to = Common.replace_str(to, "{cc}", info.getProperty("cc", ""));
                            to = Common.replace_str(to, "{bcc}", info.getProperty("bcc", ""));
                            to = Common.replace_str(to, "{from}", info.getProperty("from", ""));
                            cc = Common.replace_str(cc, "{to}", info.getProperty("to", ""));
                            cc = Common.replace_str(cc, "{cc}", info.getProperty("cc", ""));
                            cc = Common.replace_str(cc, "{bcc}", info.getProperty("bcc", ""));
                            cc = Common.replace_str(cc, "{from}", info.getProperty("from", ""));
                            bcc = Common.replace_str(bcc, "{to}", info.getProperty("to", ""));
                            bcc = Common.replace_str(bcc, "{cc}", info.getProperty("cc", ""));
                            bcc = Common.replace_str(bcc, "{bcc}", info.getProperty("bcc", ""));
                            bcc = Common.replace_str(bcc, "{from}", info.getProperty("from", ""));
                            from = Common.replace_str(from, "{to}", info.getProperty("to", ""));
                            from = Common.replace_str(from, "{cc}", info.getProperty("cc", ""));
                            from = Common.replace_str(from, "{bcc}", info.getProperty("bcc", ""));
                            from = Common.replace_str(from, "{from}", info.getProperty("from", ""));
                            break block182;
                        }
                        if (!p.getProperty("type").equalsIgnoreCase("User Hammering") || !alert_action.equals("user_hammering")) break block191;
                        loginsCounter = new Properties();
                        loginsUserInfos = new Properties();
                        now = System.currentTimeMillis();
                        sessionIds = new Properties();
                        v = (Vector)ServerStatus.siVG("user_list").clone();
                        xx = v.size() - 1;
                        while (xx >= 0) {
                            try {
                                ui = (Properties)v.elementAt(xx);
                                if (!ui.getProperty("hack_username", "false").equals("true")) {
                                    stamp = Long.parseLong(ui.getProperty("login_date_stamp_unique", "0"));
                                    user_name = ui.getProperty("user_name", "");
                                    if (!user_name.equals("") && !user_name.equalsIgnoreCase("anonymous") && now - stamp < (long)(Integer.parseInt(p.getProperty("login_interval", "60")) * 1000)) {
                                        id = ui.getProperty("CrushAuth", "");
                                        if (id.equals("")) {
                                            id = ui.getProperty("id");
                                        }
                                        if (!sessionIds.containsKey(id)) {
                                            loginsCounter.put(user_name, String.valueOf(Integer.parseInt(loginsCounter.getProperty(user_name, "0")) + 1));
                                        }
                                        sessionIds.put(id, "found");
                                        loginsUserInfos.put(user_name, ui);
                                        Log.log("ALERT", 1, "User Hammering : Found user at user list. User: " + user_name);
                                    }
                                }
                            }
                            catch (Exception e) {
                                Log.log("ALERT", 2, e);
                            }
                            --xx;
                        }
                        xx = 0;
                        if (true) ** GOTO lbl508
                        do {
                            try {
                                ui = (Properties)ServerStatus.siVG("recent_user_list").elementAt(xx);
                                stamp = Long.parseLong(ui.getProperty("login_date_stamp_unique", "0"));
                                user_name = ui.getProperty("user_name", "");
                                if (!user_name.equals("") && !user_name.equalsIgnoreCase("anonymous") && now - stamp < (long)(Integer.parseInt(p.getProperty("login_interval", "60")) * 1000)) {
                                    id = ui.getProperty("CrushAuth", "");
                                    if (id.equals("")) {
                                        id = ui.getProperty("id");
                                    }
                                    if (!sessionIds.containsKey(id)) {
                                        loginsCounter.put(user_name, String.valueOf(Integer.parseInt(loginsCounter.getProperty(user_name, "0")) + 1));
                                    }
                                    sessionIds.put(id, "found");
                                    loginsUserInfos.put(user_name, ui);
                                    Log.log("ALERT", 1, "User Hammering : Found user at recent user list. User: " + user_name);
                                }
                            }
                            catch (Exception e) {
                                Log.log("ALERT", 2, e);
                            }
                            ++xx;
lbl508:
                            // 2 sources

                        } while (xx < ServerStatus.siVG("recent_user_list").size());
                        keys = loginsCounter.keys();
                        found = false;
                        line = "";
                        hasLines = false;
                        if (body.indexOf("<LINE>") >= 0) {
                            line = body.substring(body.indexOf("<LINE>") + "<LINE>".length(), body.lastIndexOf("</LINE>"));
                            hasLines = true;
                        }
                        if ((recent_hammering = (Properties)ServerStatus.thisObj.server_info.get("recent_hammering")) == null) {
                            recent_hammering = new Properties();
                        }
                        ServerStatus.thisObj.server_info.put("recent_hammering", recent_hammering);
                        recent_hammering.clear();
                        newLines = "";
                        while (keys.hasMoreElements()) {
                            key = keys.nextElement().toString();
                            count = Integer.parseInt(loginsCounter.getProperty(key, "0"));
                            if (count <= Integer.parseInt(p.getProperty("login_count", "100"))) continue;
                            recent_hammering.put(key, String.valueOf(count) + " logins");
                            newLines = ServerStatus.thisObj.change_vars_to_values(line, null, (Properties)loginsUserInfos.get(key), null);
                            if (hasLines) {
                                newLines = String.valueOf(newLines) + "\r\n" + key + ":" + count + "\r\n";
                            } else {
                                body = String.valueOf(body) + "\r\n" + key + ":" + count + "\r\n";
                            }
                            found = true;
                            try {
                                info2 = new Properties();
                                info2.put("alert_type", "hammering");
                                info2.put("alert_sub_type", "user");
                                info2.put("alert_timeout", "0");
                                info2.put("alert_max", String.valueOf(p.getProperty("login_count", "100")));
                                info2.put("alert_count", String.valueOf(count));
                                info2.put("alert_msg", hasLines != false ? newLines.trim() : body.trim());
                                AlertTools.runAlerts("security_alert", info2, (Properties)loginsUserInfos.get(key), null, null, null, dmz_mode);
                            }
                            catch (Exception e) {
                                Log.log("BAN", 1, e);
                            }
                        }
                        body = Common.replace_str(body, "<LINE>" + line + "</LINE>", newLines).trim();
                        if (!found) {
                            ok = false;
                        }
                        break block182;
                    }
                    if (p.getProperty("type").equalsIgnoreCase("Plugin Message") && alert_action.startsWith("pluginMessage_")) {
                        subject = Common.replace_str(subject, "%message%", alert_action.substring(alert_action.indexOf("_") + 1));
                        body = Common.replace_str(body, "%message%", alert_action.substring(alert_action.indexOf("_") + 1));
                        subject = Common.replace_str(subject, "{message}", alert_action.substring(alert_action.indexOf("_") + 1));
                        body = Common.replace_str(body, "{message}", alert_action.substring(alert_action.indexOf("_") + 1));
                        if (p.getProperty("to").toUpperCase().startsWith("PLUGIN:")) {
                            plugin = p.getProperty("to").substring("plugin:".length()).trim();
                            items = new Vector<Properties>();
                            f = new File_S(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/temp/");
                            item = new Properties();
                            if (info != null) {
                                item.putAll((Map<?, ?>)info);
                            }
                            try {
                                item.put("url", f.toURI().toURL().toExternalForm());
                            }
                            catch (Exception sessionIds) {
                                // empty catch block
                            }
                            item.put("the_command", alert_action);
                            item.put("subject", subject);
                            item.put("body", body);
                            items.addElement(item);
                            ServerStatus.thisObj.append_log(String.valueOf(LOC.G("ALERT:")) + p.getProperty("type") + ":" + plugin, "ERROR");
                            event = new Properties();
                            event.put("event_plugin_list", plugin);
                            event.put("name", "PluginMessage_Alert:" + plugin);
                            ServerStatus.thisObj.events6.doEventPlugin(null, event, the_user, items);
                            ok = false;
                        }
                    } else if (p.getProperty("type").equalsIgnoreCase("Security Alert") && alert_action.equals("security_alert")) {
                        subject = Common.replace_str(subject, "{alert_type}", info.getProperty("alert_type", ""));
                        subject = Common.replace_str(subject, "{alert_sub_type}", info.getProperty("alert_sub_type", ""));
                        subject = Common.replace_str(subject, "{alert_timeout}", info.getProperty("alert_timeout", ""));
                        subject = Common.replace_str(subject, "{alert_max}", info.getProperty("alert_max", ""));
                        subject = Common.replace_str(subject, "{alert_msg}", info.getProperty("alert_msg", ""));
                        body = Common.replace_str(body, "{alert_type}", info.getProperty("alert_type", ""));
                        body = Common.replace_str(body, "{alert_sub_type}", info.getProperty("alert_sub_type", ""));
                        body = Common.replace_str(body, "{alert_timeout}", info.getProperty("alert_timeout", ""));
                        body = Common.replace_str(body, "{alert_max}", info.getProperty("alert_max", ""));
                        body = Common.replace_str(body, "{alert_msg}", info.getProperty("alert_msg", ""));
                        subject = Common.replace_str(subject, "%alert_type%", info.getProperty("alert_type", ""));
                        subject = Common.replace_str(subject, "%alert_sub_type%", info.getProperty("alert_sub_type", ""));
                        subject = Common.replace_str(subject, "%alert_timeout%", info.getProperty("alert_timeout", ""));
                        subject = Common.replace_str(subject, "%alert_max%", info.getProperty("alert_max", ""));
                        subject = Common.replace_str(subject, "%alert_msg%", info.getProperty("alert_msg", ""));
                        body = Common.replace_str(body, "%alert_type%", info.getProperty("alert_type", ""));
                        body = Common.replace_str(body, "%alert_sub_type%", info.getProperty("alert_sub_type", ""));
                        body = Common.replace_str(body, "%alert_timeout%", info.getProperty("alert_timeout", ""));
                        body = Common.replace_str(body, "%alert_max%", info.getProperty("alert_max", ""));
                        body = Common.replace_str(body, "%alert_msg%", info.getProperty("alert_msg", ""));
                    } else if (p.getProperty("type").equalsIgnoreCase("ServerBeat Alert") && alert_action.equals("serverbeat_alert")) {
                        subject = Common.replace_str(subject, "{alert_type}", info.getProperty("alert_type", ""));
                        subject = Common.replace_str(subject, "{alert_sub_type}", info.getProperty("alert_sub_type", ""));
                        subject = Common.replace_str(subject, "{alert_timeout}", info.getProperty("alert_timeout", ""));
                        subject = Common.replace_str(subject, "{alert_max}", info.getProperty("alert_max", ""));
                        subject = Common.replace_str(subject, "{alert_msg}", info.getProperty("alert_msg", ""));
                        body = Common.replace_str(body, "{alert_type}", info.getProperty("alert_type", ""));
                        body = Common.replace_str(body, "{alert_sub_type}", info.getProperty("alert_sub_type", ""));
                        body = Common.replace_str(body, "{alert_timeout}", info.getProperty("alert_timeout", ""));
                        body = Common.replace_str(body, "{alert_max}", info.getProperty("alert_max", ""));
                        body = Common.replace_str(body, "{alert_msg}", info.getProperty("alert_msg", ""));
                    } else if (p.getProperty("type").equalsIgnoreCase("Low Memory") != false && alert_action.equals("low_memory") != false || p.getProperty("type").equalsIgnoreCase("Low Memory1") != false && alert_action.equals("low_memory1") != false || p.getProperty("type").equalsIgnoreCase("Low Memory2") != false && alert_action.equals("low_memory2") != false || p.getProperty("type").equalsIgnoreCase("Low Memory3") && alert_action.equals("low_memory3")) {
                        from = Common.replace_str(from, "{alert_ram_free}", info.getProperty("alert_ram_free", ""));
                        to = Common.replace_str(to, "{alert_ram_free}", info.getProperty("alert_ram_free", ""));
                        body = Common.replace_str(body, "{alert_ram_free}", info.getProperty("alert_ram_free", ""));
                        subject = Common.replace_str(subject, "{alert_ram_free}", info.getProperty("alert_ram_free", ""));
                        bcc = Common.replace_str(bcc, "{alert_ram_free}", info.getProperty("alert_ram_free", ""));
                        cc = Common.replace_str(cc, "{alert_ram_free}", info.getProperty("alert_ram_free", ""));
                        from = Common.replace_str(from, "%alert_ram_free%", info.getProperty("alert_ram_free", ""));
                        to = Common.replace_str(to, "%alert_ram_free%", info.getProperty("alert_ram_free", ""));
                        body = Common.replace_str(body, "%alert_ram_free%", info.getProperty("alert_ram_free", ""));
                        subject = Common.replace_str(subject, "%alert_ram_free%", info.getProperty("alert_ram_free", ""));
                        bcc = Common.replace_str(bcc, "%alert_ram_free%", info.getProperty("alert_ram_free", ""));
                        cc = Common.replace_str(cc, "%alert_ram_free%", info.getProperty("alert_ram_free", ""));
                        from = Common.replace_str(from, "{alert_ram_max}", info.getProperty("alert_ram_max", ""));
                        to = Common.replace_str(to, "{alert_ram_max}", info.getProperty("alert_ram_max", ""));
                        body = Common.replace_str(body, "{alert_ram_max}", info.getProperty("alert_ram_max", ""));
                        subject = Common.replace_str(subject, "{alert_ram_max}", info.getProperty("alert_ram_max", ""));
                        bcc = Common.replace_str(bcc, "{alert_ram_max}", info.getProperty("alert_ram_max", ""));
                        cc = Common.replace_str(cc, "{alert_ram_max}", info.getProperty("alert_ram_max", ""));
                        from = Common.replace_str(from, "%alert_ram_max%", info.getProperty("alert_ram_max", ""));
                        to = Common.replace_str(to, "%alert_ram_max%", info.getProperty("alert_ram_max", ""));
                        body = Common.replace_str(body, "%alert_ram_max%", info.getProperty("alert_ram_max", ""));
                        subject = Common.replace_str(subject, "%alert_ram_max%", info.getProperty("alert_ram_max", ""));
                        bcc = Common.replace_str(bcc, "%alert_ram_max%", info.getProperty("alert_ram_max", ""));
                        cc = Common.replace_str(cc, "%alert_ram_max%", info.getProperty("alert_ram_max", ""));
                        from = Common.replace_str(from, "{alert_memory_threads}", info.getProperty("alert_memory_threads", ""));
                        to = Common.replace_str(to, "{alert_memory_threads}", info.getProperty("alert_memory_threads", ""));
                        body = Common.replace_str(body, "{alert_memory_threads}", info.getProperty("alert_memory_threads", ""));
                        subject = Common.replace_str(subject, "{alert_memory_threads}", info.getProperty("alert_memory_threads", ""));
                        bcc = Common.replace_str(bcc, "{alert_memory_threads}", info.getProperty("alert_memory_threads", ""));
                        cc = Common.replace_str(cc, "{alert_memory_threads}", info.getProperty("alert_memory_threads", ""));
                        from = Common.replace_str(from, "%alert_memory_threads%", info.getProperty("alert_memory_threads", ""));
                        to = Common.replace_str(to, "%alert_memory_threads%", info.getProperty("alert_memory_threads", ""));
                        body = Common.replace_str(body, "%alert_memory_threads%", info.getProperty("alert_memory_threads", ""));
                        subject = Common.replace_str(subject, "%alert_memory_threads%", info.getProperty("alert_memory_threads", ""));
                        bcc = Common.replace_str(bcc, "%alert_memory_threads%", info.getProperty("alert_memory_threads", ""));
                        cc = Common.replace_str(cc, "%alert_memory_threads%", info.getProperty("alert_memory_threads", ""));
                        body = String.valueOf(body) + "<br/>\\r\\n-------------------------------------------------------------------------------<br/>\\r\\n" + com.crushftp.client.Common.dumpStack(alert_action.toUpperCase()).replaceAll("(\\r|\\n)", "<br/>\r\n");
                    } else if (p.getProperty("type").equalsIgnoreCase("Big Directory") && alert_action.equals("big_dir")) {
                        body = Common.replace_str(body, "{alert_msg}", info.getProperty("alert_msg", ""));
                        subject = Common.replace_str(subject, "{alert_msg}", info.getProperty("alert_msg", ""));
                        body = Common.replace_str(body, "%alert_msg%", info.getProperty("alert_msg", ""));
                        subject = Common.replace_str(subject, "%alert_msg%", info.getProperty("alert_msg", ""));
                    } else if (p.getProperty("type").equalsIgnoreCase("Slow Login") && alert_action.equals("slow_login")) {
                        body = Common.replace_str(body, "{alert_msg}", info.getProperty("alert_msg", ""));
                        subject = Common.replace_str(subject, "{alert_msg}", info.getProperty("alert_msg", ""));
                        body = Common.replace_str(body, "%alert_msg%", info.getProperty("alert_msg", ""));
                        subject = Common.replace_str(subject, "%alert_msg%", info.getProperty("alert_msg", ""));
                    } else if (p.getProperty("type").equalsIgnoreCase("Login Frequency") && alert_action.equals("login_frequency")) {
                        body = Common.replace_str(body, "{alert_msg}", info.getProperty("count", "0"));
                        subject = Common.replace_str(subject, "{alert_msg}", info.getProperty("count", "0"));
                        body = Common.replace_str(body, "%alert_msg%", info.getProperty("count", "0"));
                        subject = Common.replace_str(subject, "%alert_msg%", info.getProperty("count", "0"));
                        body = Common.replace_str(body, "{alert_login_interval}", String.valueOf(p.getProperty("login_interval")));
                        subject = Common.replace_str(subject, "{alert_login_interval}", String.valueOf(p.getProperty("login_interval")));
                        body = Common.replace_str(body, "%alert_login_interval%", String.valueOf(p.getProperty("login_interval")));
                        subject = Common.replace_str(subject, "%alert_login_interval%", String.valueOf(p.getProperty("login_interval")));
                    } else if (p.getProperty("type").equalsIgnoreCase("Login Frequency Banned") && alert_action.equals("login_frequency_banned")) {
                        body = Common.replace_str(body, "{alert_msg}", info.getProperty("alert_msg", "0"));
                        subject = Common.replace_str(subject, "{alert_msg}", info.getProperty("alert_msg", "0"));
                        body = Common.replace_str(body, "{count}", info.getProperty("count", "0"));
                        subject = Common.replace_str(subject, "{count}", info.getProperty("count", "0"));
                        body = Common.replace_str(body, "{interval}", info.getProperty("interval", "0"));
                        subject = Common.replace_str(subject, "{interval}", info.getProperty("interval", "0"));
                        body = Common.replace_str(body, "{attempts}", info.getProperty("attempts", "0"));
                        subject = Common.replace_str(subject, "{attempts}", info.getProperty("attempts", "0"));
                        body = Common.replace_str(body, "{timeout}", info.getProperty("timeout", "0"));
                        subject = Common.replace_str(subject, "{timeout}", info.getProperty("timeout", "0"));
                        body = Common.replace_str(body, "{user_name}", info.getProperty("user_name", "0"));
                        subject = Common.replace_str(subject, "{user_name}", info.getProperty("user_name", "0"));
                        body = Common.replace_str(body, "{username}", info.getProperty("user_name", "0"));
                        subject = Common.replace_str(subject, "{username}", info.getProperty("user_name", "0"));
                        body = Common.replace_str(body, "{expire}", info.getProperty("expire", "0"));
                        subject = Common.replace_str(subject, "{expire}", info.getProperty("expire", "0"));
                        body = Common.replace_str(body, "{alert_login_interval}", String.valueOf(p.getProperty("login_interval")));
                        subject = Common.replace_str(subject, "{alert_login_interval}", String.valueOf(p.getProperty("login_interval")));
                        body = Common.replace_str(body, "%alert_login_interval%", String.valueOf(p.getProperty("login_interval")));
                        subject = Common.replace_str(subject, "%alert_login_interval%", String.valueOf(p.getProperty("login_interval")));
                    } else if (p.getProperty("type").equalsIgnoreCase("Job Changes") && alert_action.equals("job_update")) {
                        body = Common.replace_str(body, "{alert_schedule_name}", info.getProperty("alert_schedule_name", ""));
                        body = Common.replace_str(body, "{alert_schedule_changes}", info.getProperty("alert_schedule_changes", ""));
                        body = Common.replace_str(body, "{alert_schedule_audit_trail}", info.getProperty("alert_schedule_audit_trail", ""));
                        subject = Common.replace_str(subject, "{alert_schedule_name}", info.getProperty("alert_schedule_name", ""));
                        subject = Common.replace_str(subject, "{alert_schedule_changes}", info.getProperty("alert_schedule_changes", ""));
                        subject = Common.replace_str(subject, "{alert_schedule_audit_trail}", info.getProperty("alert_schedule_audit_trail", ""));
                    } else if (p.getProperty("type").equalsIgnoreCase("Update Object") && alert_action.equals("update_object")) {
                        if (com.crushftp.client.Common.do_search(p.getProperty("path_filter", "*"), info.getProperty("update_object_path", ""), false, 0) && com.crushftp.client.Common.do_search(p.getProperty("log_filter", "*"), info.getProperty("update_object_log", ""), false, 0)) {
                            body = Common.replace_str(body, "{update_object_log}", info.getProperty("update_object_log", ""));
                            body = Common.replace_str(body, "{update_object_path}", info.getProperty("update_object_path", ""));
                            subject = Common.replace_str(subject, "{update_object_log}", info.getProperty("update_object_log", ""));
                            subject = Common.replace_str(subject, "{update_object_path}", info.getProperty("update_object_path", ""));
                        } else {
                            ok = false;
                        }
                    } else if (p.getProperty("type").equalsIgnoreCase("VFS Bad Credentials") && alert_action.equals("vfs_bad_credentials")) {
                        body = Common.replace_str(body, "{username}", user_info.getProperty("user_name", ""));
                        body = Common.replace_str(body, "{url}", info.getProperty("url", ""));
                        body = Common.replace_str(body, "{url_path}", info.getProperty("url_path", ""));
                        body = Common.replace_str(body, "{path}", info.getProperty("vfs_path", ""));
                        body = Common.replace_str(body, "{protocol}", info.getProperty("protocol", ""));
                        body = Common.replace_str(body, "{port}", info.getProperty("port", ""));
                        body = Common.replace_str(body, "{error_message}", info.getProperty("error_message", ""));
                        body = ServerStatus.thisObj.change_vars_to_values(body, user, user_info, the_user);
                        subject = Common.replace_str(subject, "{username}", user_info.getProperty("user_name", ""));
                        subject = Common.replace_str(subject, "{url}", info.getProperty("url", ""));
                        subject = Common.replace_str(subject, "{url_path}", info.getProperty("url_path", ""));
                        subject = Common.replace_str(subject, "{path}", info.getProperty("path", ""));
                        subject = Common.replace_str(subject, "{protocol}", info.getProperty("protocol", ""));
                        subject = Common.replace_str(subject, "{port}", info.getProperty("port", ""));
                        subject = Common.replace_str(subject, "{error_message}", info.getProperty("error_message", ""));
                        subject = ServerStatus.thisObj.change_vars_to_values(subject, user, user_info, the_user);
                    } else if (p.getProperty("type").equalsIgnoreCase("Skipped scheduled job") && alert_action.startsWith("skipped_scheduled_job")) {
                        keys = info.keys();
                        while (keys.hasMoreElements()) {
                            key = keys.nextElement().toString();
                            subject = Common.replace_str(subject, "{" + key + "}", info.getProperty(key));
                            body = Common.replace_str(body, "{" + key + "}", info.getProperty(key));
                            to = Common.replace_str(to, "{" + key + "}", info.getProperty(key));
                            cc = Common.replace_str(cc, "{" + key + "}", info.getProperty(key));
                            bcc = Common.replace_str(bcc, "{" + key + "}", info.getProperty(key));
                            from = Common.replace_str(from, "{" + key + "}", info.getProperty(key));
                        }
                    } else {
                        ok = false;
                    }
                }
                if (ok) {
                    AlertTools.runAlertAction(p, info, alert_action, user_info, the_user, user, from, to, cc, bcc, subject, body, dmz_mode, hours_key);
                }
            }
            ++x;
        }
    }

    public static void runAlertAction(Properties p, Properties info, String alert_action, Properties user_info, SessionCrush the_user, Properties user, String from, String to, String cc, String bcc, String subject, String body, boolean dmz_mode, String hours_key) {
        SimpleDateFormat hh;
        String HH;
        Properties hours;
        int curHH;
        if (p.get(hours_key) == null) {
            p.put(hours_key, new Properties());
        }
        if (!((curHH = Integer.parseInt((hours = (Properties)p.get(hours_key)).getProperty(HH = (hh = new SimpleDateFormat("HH", Locale.US)).format(new Date()), "0"))) >= Integer.parseInt(p.getProperty("max_alert_emails", "60")) || to.equals("") && p.getProperty("alert_plugin", "").equals(""))) {
            int xx = 0;
            while (xx < 10) {
                hours.put("0" + xx, "0");
                ++xx;
            }
            xx = 10;
            while (xx < 25) {
                hours.put("" + xx, "0");
                ++xx;
            }
            hours.put(HH, String.valueOf(curHH + 1));
            if (the_user != null) {
                user = the_user.user;
            }
            to = ServerStatus.thisObj.change_vars_to_values(to, user, user_info, the_user);
            cc = ServerStatus.thisObj.change_vars_to_values(cc, user, user_info, the_user);
            bcc = ServerStatus.thisObj.change_vars_to_values(bcc, user, user_info, the_user);
            from = ServerStatus.thisObj.change_vars_to_values(from, user, user_info, the_user);
            subject = ServerStatus.thisObj.change_vars_to_values(subject, user, user_info, the_user);
            body = ServerStatus.thisObj.change_vars_to_values(body, user, user_info, the_user);
            if (ServerStatus.siBG("dmz_mode") && p != null && p.getProperty("alert_plugin", "").equals("")) {
                Vector queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                Properties action = new Properties();
                action.put("type", "GET:SINGLETON");
                action.put("id", Common.makeBoundary());
                action.put("need_response", "true");
                queue.addElement(action);
                action = UserTools.waitResponse(action, 30);
                String singleton_id = "";
                if (action != null) {
                    singleton_id = "" + action.get("singleton_id");
                }
                action = new Properties();
                action.put("type", "PUT:ALERT");
                action.put("id", Common.makeBoundary());
                action.put("the_alert_p", p);
                if (info != null) {
                    action.put("info", info);
                }
                action.put("alert_action", alert_action);
                if (user_info != null) {
                    action.put("user_info", user_info);
                }
                if (the_user != null && the_user.user != null) {
                    action.put("user", the_user.user);
                }
                if (from != null) {
                    action.put("from", from);
                }
                if (to != null) {
                    action.put("to", to);
                }
                if (cc != null) {
                    action.put("cc", cc);
                }
                if (bcc != null) {
                    action.put("bcc", bcc);
                }
                if (subject != null) {
                    action.put("subject", subject);
                }
                if (body != null) {
                    action.put("body", body);
                }
                action.put("dmz_mode", String.valueOf(dmz_mode));
                action.put("hours_key", hours_key);
                action.put("singleton_id", singleton_id);
                action.put("alert", p);
                queue.addElement(action);
            }
            if (!ServerStatus.BG("run_alerts_dmz")) {
                if (ServerStatus.siBG("dmz_mode") && p.getProperty("alert_plugin", "").equals("")) {
                    return;
                }
            }
            ServerStatus.thisObj.append_log(String.valueOf(LOC.G("ALERT:")) + p.getProperty("type"), "ERROR");
            Enumeration<Object> keys = ServerStatus.thisObj.server_info.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                String val = ServerStatus.thisObj.server_info.getProperty(key);
                if (body.indexOf("%server_" + key + "%") >= 0) {
                    body = Common.replace_str(body, "%server_" + key + "%", String.valueOf(val));
                }
                if (body.indexOf("%" + key + "%") >= 0) {
                    body = Common.replace_str(body, "%" + key + "%", String.valueOf(val));
                }
                if (subject.indexOf("%server_" + key + "%") >= 0) {
                    subject = Common.replace_str(subject, "%server_" + key + "%", String.valueOf(val));
                }
                if (subject.indexOf("%" + key + "%") < 0) continue;
                subject = Common.replace_str(subject, "%" + key + "%", String.valueOf(val));
            }
            Variables vars = new Variables();
            vars.setDate(new Date());
            body = vars.replace_vars_line_date(body, info, "{", "}");
            body = vars.replace_vars_line_date(body, info, "%", "%");
            subject = vars.replace_vars_line_date(subject, info, "{", "}");
            subject = vars.replace_vars_line_date(subject, info, "%", "%");
            Log.log("SERVER", 0, "Alert: " + p.getProperty("type") + " : Subject : " + subject + " To:" + to + " From:" + from);
            if (Log.log("ALERT", 0, "")) {
                Log.log("ALERT", 0, new Exception("New Alert:" + subject + ":" + p));
            }
            if (!p.getProperty("alert_plugin", "").equals("")) {
                Properties item = (Properties)p.clone();
                if (the_user != null && the_user.user != null) {
                    item.putAll((Map<?, ?>)the_user.user);
                }
                if (user_info != null) {
                    item.putAll((Map<?, ?>)user_info);
                }
                item.putAll((Map<?, ?>)p);
                item.put("url", "file://" + p.getProperty("type") + "/" + p.getProperty("name"));
                item.put("the_file_name", subject);
                item.put("the_file_path", body);
                item.put("to", to);
                item.put("cc", cc);
                item.put("bcc", bcc);
                item.put("from", from);
                item.put("subject", subject);
                item.put("body", body);
                item.put("dmz_mode", String.valueOf(dmz_mode));
                if (info != null) {
                    item.putAll((Map<?, ?>)info);
                }
                Vector<Properties> items = new Vector<Properties>();
                items.addElement(item);
                Properties event = new Properties();
                event.put("event_plugin_list", p.getProperty("alert_plugin", ""));
                event.put("name", "AlertPlugin:" + p.getProperty("type"));
                ServerStatus.thisObj.events6.doEventPlugin(null, event, the_user, items);
            }
            if (!to.equals("")) {
                String emailResult = com.crushftp.client.Common.send_mail(ServerStatus.SG("discovered_ip"), to, cc, bcc, from, subject, body, ServerStatus.SG("smtp_server"), ServerStatus.SG("smtp_user"), ServerStatus.SG("smtp_pass"), ServerStatus.BG("smtp_ssl"), ServerStatus.BG("smtp_html"), null);
                if (emailResult.toUpperCase().indexOf("SUCCESS") < 0) {
                    Log.log("SMTP", 0, String.valueOf(LOC.G("FAILURE:")) + " " + emailResult + "\r\n");
                    Log.log("SMTP", 0, String.valueOf(LOC.G("FROM:")) + " " + ServerStatus.thisObj.change_vars_to_values(p.getProperty("from"), user, user_info, the_user) + "\r\n");
                    Log.log("SMTP", 0, String.valueOf(LOC.G("TO:")) + " " + ServerStatus.thisObj.change_vars_to_values(p.getProperty("to"), user, user_info, the_user) + "\r\n");
                    Log.log("SMTP", 0, String.valueOf(LOC.G("CC:")) + " " + ServerStatus.thisObj.change_vars_to_values(p.getProperty("cc"), user, user_info, the_user) + "\r\n");
                    Log.log("SMTP", 0, String.valueOf(LOC.G("BCC:")) + " " + ServerStatus.thisObj.change_vars_to_values(p.getProperty("bcc"), user, user_info, the_user) + "\r\n");
                    Log.log("SMTP", 0, String.valueOf(LOC.G("SUBJECT:")) + " " + ServerStatus.thisObj.change_vars_to_values(p.getProperty("subject"), user, user_info, the_user) + "\r\n");
                    Log.log("SMTP", 0, String.valueOf(LOC.G("BODY:")) + " " + ServerStatus.thisObj.change_vars_to_values(p.getProperty("body"), user, user_info, the_user) + "\r\n");
                }
            }
        }
    }
}

