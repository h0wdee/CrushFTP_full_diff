/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import com.crushftp.client.File_B;
import com.crushftp.client.File_S;
import com.crushftp.client.File_U;
import com.crushftp.client.VRL;
import crushftp.db.SearchHandler;
import crushftp.handlers.Common;
import crushftp.handlers.JobFilesHandler;
import crushftp.handlers.Log;
import crushftp.handlers.SessionCrush;
import crushftp.handlers.UserTools;
import crushftp.server.ServerSessionAJAX;
import crushftp.server.ServerStatus;
import crushftp.server.VFS;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class TaskBridge {
    public static String doShare(String body, Vector itemsFound, Properties info) throws Exception {
        String original_share_key = "";
        String share_key = "";
        if (body.indexOf("{share:") >= 0) {
            original_share_key = share_key = body.substring(body.indexOf("{share:") + 1, body.indexOf("}", body.indexOf("{share:")));
        } else {
            original_share_key = share_key = body.substring(body.indexOf("{share_") + 1, body.indexOf("}", body.indexOf("{share_")));
            share_key = share_key.replace('_', ':');
        }
        SimpleDateFormat date_time = null;
        Vector web_customizations = null;
        String user_name = "default";
        String linkedServer = null;
        Properties user = null;
        String user_ip = null;
        if (info != null) {
            SessionCrush the_session = (SessionCrush)info.get("ServerSessionObject");
            if (the_session == null) {
                the_session = (SessionCrush)info.get("ServerSession");
            }
            if (the_session != null) {
                date_time = the_session.date_time;
                web_customizations = (Vector)the_session.user.get("web_customizations");
                user_name = the_session.uiSG("user_name");
                linkedServer = the_session.server_item.getProperty("linkedServer");
                user = the_session.user;
                user_ip = the_session.user_info.getProperty("user_ip");
            }
        }
        if (user_name.equals("default") && share_key.split(":").length > 3) {
            user_name = share_key.split(":")[3];
        }
        SimpleDateFormat ex1 = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        try {
            ((Calendar)cal).add(5, Integer.parseInt(share_key.split(":")[2]));
        }
        catch (Exception e) {
            throw new Exception(e + share_key);
        }
        Properties request = new Properties();
        request.put("expire", ex1.format(cal.getTime()));
        String share_paths = "";
        String share_user = user_name;
        if (share_user.equals("default")) {
            share_user = info.getProperty("share_user", share_user);
        }
        if (user_ip == null) {
            user_ip = info.getProperty("share_ip", "0.0.0.0");
        }
        Properties fake_user_info = new Properties();
        ServerStatus.thisObj.statTools.add_login_stat(linkedServer == null ? "MainUsers" : linkedServer, share_user, user_ip, true, fake_user_info);
        int x = 0;
        while (x < itemsFound.size()) {
            Properties item = (Properties)itemsFound.elementAt(x);
            share_paths = String.valueOf(share_paths) + com.crushftp.client.Common.url_encode(item.getProperty("the_file_path")) + "\r\n";
            item.put("privs", "(read)(view)(resume)(write)(delete)(slideshow)(rename)(makedir)(deletedir)(share)(inherited)");
            TaskBridge.doEventItem(item, "SHARE", user_ip, fake_user_info.getProperty("sessionID"), fake_user_info.getProperty("SESSION_RID"));
            ++x;
        }
        request.put("paths", share_paths);
        request.put("baseUrl", "");
        request.put("direct_link", "false");
        request.put("emailTo", info.getProperty("emailTo", ""));
        request.put("emailCc", info.getProperty("emailCc", ""));
        request.put("emailBcc", info.getProperty("emailBcc", ""));
        request.put("emailFrom", info.getProperty("emailFrom", ""));
        request.put("emailSubject", "");
        request.put("emailBody", "{web_link}");
        request.put("sendEmail", "false");
        request.put("shareUsername", "false");
        request.put("allowUploads", "false");
        request.put("attach", "false");
        if (share_key.split(":")[1].equals("reference")) {
            request.put("publishType", "reference");
        } else if (share_key.split(":")[1].equals("move")) {
            request.put("publishType", "move");
        } else if (share_key.split(":")[1].equals("copy")) {
            request.put("publishType", "copy");
        }
        if (share_key.split(":").length > 4 && share_key.split(":")[4].equalsIgnoreCase("full")) {
            request.put("allowUploads", "true");
        }
        if (share_key.split(":").length > 5 && !share_key.split(":")[5].equalsIgnoreCase("")) {
            request.put("logins", share_key.split(":")[5].trim());
        }
        String response = ServerSessionAJAX.createShare(itemsFound, request, web_customizations, user_name, linkedServer == null ? "MainUsers" : linkedServer, user, date_time, null);
        String share_msg = com.crushftp.client.Common.url_decode(response.substring(response.indexOf("<message>") + 9, response.indexOf("</message>")));
        String share_url = com.crushftp.client.Common.url_decode(response.substring(response.indexOf("<url>") + 5, response.indexOf("</url>")));
        if (share_msg.indexOf("{share_complete}") < 0) {
            share_url = "";
        }
        body = com.crushftp.client.Common.replace_str(body, "{" + original_share_key + "}", share_url);
        body = com.crushftp.client.Common.replace_str(body, "{" + original_share_key + "_message}", share_msg);
        body = com.crushftp.client.Common.replace_str(body, "{" + original_share_key + ":message}", share_msg);
        int x2 = 0;
        while (x2 < itemsFound.size()) {
            Properties item = (Properties)itemsFound.elementAt(x2);
            Properties p = (Properties)request.clone();
            p.putAll((Map<?, ?>)item);
            item.putAll((Map<?, ?>)p);
            ++x2;
        }
        return body;
    }

    public static String convertFormEmail(String the_line, Vector lastUploadStats) {
        Properties formEmail = Common.buildFormEmail(ServerStatus.server_settings, lastUploadStats);
        return Common.replaceFormVariables(formEmail, the_line);
    }

    public static String convertSessionVars(String the_line, Properties info) {
        SessionCrush the_session = null;
        if (info != null && (the_session = (SessionCrush)info.get("ServerSessionObject")) == null) {
            the_session = (SessionCrush)info.get("ServerSession");
        }
        if (the_session != null) {
            the_line = ServerStatus.thisObj.change_vars_to_values(the_line, the_session);
        }
        return the_line;
    }

    public static String convertCustomData(String the_line, String r1, String r2) {
        while (the_line.indexOf(String.valueOf(r1) + "customData_") >= 0) {
            String custom = the_line.substring(the_line.indexOf(String.valueOf(r1) + "customData_") + (String.valueOf(r1) + "customData_").length());
            custom = custom.substring(0, custom.indexOf(r2));
            Properties customData = (Properties)ServerStatus.server_settings.get("customData");
            String val = customData.getProperty(custom, "");
            the_line = com.crushftp.client.Common.replace_str(the_line, String.valueOf(r1) + "customData_" + custom + r2, val);
        }
        return the_line;
    }

    public static String convertUserManager(String the_line, String r1, String r2) {
        Properties groups;
        String inner;
        String username;
        String serverGroup;
        String error;
        String result_type;
        String params;
        while (the_line.indexOf(String.valueOf(r1) + "group_start") >= 0 && the_line.indexOf(String.valueOf(r1) + "group_end" + r2) >= 0) {
            String r;
            block26: {
                Vector users;
                params = the_line.substring(the_line.indexOf(String.valueOf(r1) + "group_start") + (String.valueOf(r1) + "group_start").length());
                params = params.substring(0, params.indexOf("}"));
                result_type = "user_name:MainUsers";
                if (params.indexOf(":") >= 0) {
                    result_type = params.substring(params.indexOf(":") + 1).trim();
                }
                String inner2 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "group_start" + params + r2) + (String.valueOf(r1) + "group_start" + params + r2).length(), the_line.indexOf(String.valueOf(r1) + "group_end" + r2));
                r = "";
                Properties groups2 = UserTools.getGroups(result_type.split(":")[1]);
                if (groups2 == null || (users = (Vector)groups2.get(inner2.trim())) == null) break block26;
                String separator = result_type.split(":")[2].trim();
                int xx = 0;
                while (xx < users.size()) {
                    block29: {
                        block28: {
                            block27: {
                                if (!result_type.split(":")[0].equals("user_name")) break block27;
                                r = String.valueOf(r) + users.elementAt(xx).toString();
                                break block28;
                            }
                            Properties u = UserTools.ut.getUser(result_type.split(":")[1], users.elementAt(xx).toString(), true);
                            if (u == null || Integer.parseInt(u.getProperty("max_logins", "0")) < 0) break block29;
                            if (!u.getProperty(result_type.split(":")[0], "").equals("")) {
                                r = String.valueOf(r) + u.getProperty(result_type.split(":")[0], "");
                            }
                        }
                        r = String.valueOf(r) + separator;
                    }
                    ++xx;
                }
                if (r.endsWith(separator)) {
                    r = r.substring(0, r.length() - 1);
                }
            }
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "group_start" + params + r2))) + r + the_line.substring(the_line.indexOf(String.valueOf(r1) + "group_end" + r2) + (String.valueOf(r1) + "group_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "group_add_start") >= 0 && the_line.indexOf(String.valueOf(r1) + "group_add_end" + r2) >= 0) {
            params = the_line.substring(the_line.indexOf(String.valueOf(r1) + "group_add_start") + (String.valueOf(r1) + "group_add_start").length());
            params = params.substring(0, params.indexOf("}"));
            result_type = "MainUsers:no_user_to_add";
            if (params.indexOf(":") >= 0) {
                result_type = params.substring(params.indexOf(":") + 1).trim();
            }
            error = "";
            try {
                serverGroup = result_type.split(":")[0];
                username = result_type.split(":")[1];
                inner = the_line.substring(the_line.indexOf(String.valueOf(r1) + "group_add_start" + params + r2) + (String.valueOf(r1) + "group_add_start" + params + r2).length(), the_line.indexOf(String.valueOf(r1) + "group_add_end" + r2));
                groups = UserTools.getGroups(serverGroup);
                if (groups != null) {
                    Vector<String> users = groups.get(inner.trim());
                    if (users == null) {
                        users = new Vector<String>();
                        groups.put(inner.trim(), users);
                        if (!username.equals("no_user_to_add") && UserTools.ut.getUser(serverGroup, username, false) != null && !users.contains(username)) {
                            users.add(username);
                        }
                        UserTools.writeGroups(serverGroup, groups);
                    } else if (!username.equals("no_user_to_add") && UserTools.ut.getUser(serverGroup, username, false) != null && !users.contains(username)) {
                        users.add(username);
                        UserTools.writeGroups(serverGroup, groups);
                    }
                }
            }
            catch (Exception e) {
                error = "Error on group create : " + e.getMessage();
                com.crushftp.client.Common.log("SERVER", 1, e);
            }
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "group_add_start" + params + r2))) + error + the_line.substring(the_line.indexOf(String.valueOf(r1) + "group_add_end" + r2) + (String.valueOf(r1) + "group_add_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "group_remove_user_start") >= 0 && the_line.indexOf(String.valueOf(r1) + "group_remove_user_end" + r2) >= 0) {
            params = the_line.substring(the_line.indexOf(String.valueOf(r1) + "group_remove_user_start") + (String.valueOf(r1) + "group_remove_user_start").length());
            params = params.substring(0, params.indexOf("}"));
            result_type = "MainUsers:no_user_to_remove";
            if (params.indexOf(":") >= 0) {
                result_type = params.substring(params.indexOf(":") + 1).trim();
            }
            error = "";
            try {
                serverGroup = result_type.split(":")[0];
                username = result_type.split(":")[1];
                if (!username.equals("no_user_to_remove")) {
                    Object users;
                    inner = the_line.substring(the_line.indexOf(String.valueOf(r1) + "group_remove_user_start" + params + r2) + (String.valueOf(r1) + "group_remove_user_start" + params + r2).length(), the_line.indexOf(String.valueOf(r1) + "group_remove_user_end" + r2));
                    groups = UserTools.getGroups(serverGroup);
                    if (groups != null && (users = groups.get(inner.trim())) != null && ((Vector)users).contains(username)) {
                        ((Vector)users).remove(username);
                        UserTools.writeGroups(serverGroup, groups);
                    }
                }
            }
            catch (Exception e) {
                error = "Error on remove user from group : " + e.getMessage();
                com.crushftp.client.Common.log("SERVER", 1, e);
            }
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "group_remove_user_start" + params + r2))) + error + the_line.substring(the_line.indexOf(String.valueOf(r1) + "group_remove_user_end" + r2) + (String.valueOf(r1) + "group_remove_user_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "reload_start") >= 0 && the_line.indexOf(String.valueOf(r1) + "reload_end" + r2) >= 0) {
            String reload_user = the_line.substring(the_line.indexOf(String.valueOf(r1) + "reload_start" + r2) + (String.valueOf(r1) + "reload_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "reload_end" + r2));
            UserTools.ut.forceMemoryReloadUser(reload_user);
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "reload_start" + r2))) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "reload_end" + r2) + (String.valueOf(r1) + "reload_end" + r2).length());
        }
        return the_line;
    }

    public static String convertServerInfo(String the_line, String r1, String r2, Properties info, String original_the_line) {
        Enumeration<Object> keys = ServerStatus.thisObj.server_info.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            String val = ServerStatus.thisObj.server_info.getProperty(key);
            if (the_line.indexOf(String.valueOf(r1) + "server_" + key + r2) < 0) continue;
            boolean was_equal = original_the_line.equals(the_line);
            the_line = com.crushftp.client.Common.replace_str(the_line, String.valueOf(r1) + "server_" + key + r2, String.valueOf(val));
            if (!was_equal || the_line.indexOf(r1) >= 0) continue;
            info.put("last_scope", "static");
        }
        if (the_line.indexOf(String.valueOf(r1) + "thread_dump" + r2) >= 0) {
            the_line = com.crushftp.client.Common.replace_str(the_line, String.valueOf(r1) + "thread_dump" + r2, com.crushftp.client.Common.dumpStack("THREAD_DUMP"));
        }
        return the_line;
    }

    public static String createUser(String the_line, Properties tracker) {
        Properties user_dir;
        Properties new_user = new Properties();
        Vector<Properties> user_dirs = new Vector<Properties>();
        Enumeration<Object> keys = tracker.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            if (key.indexOf("newuser_") < 0) continue;
            String user_key = key.substring("newuser_".length(), key.length());
            String user_value = tracker.getProperty(key);
            if (user_key.indexOf("dir") >= 0) {
                user_dir = new Properties();
                String user_dir_index = user_key.substring("dir".length(), user_key.length());
                String user_dir_priv = tracker.getProperty("newuser_priv" + user_dir_index);
                user_dir.put("dir", user_value);
                user_dir.put("priv", user_dir_priv);
                user_dirs.add(user_dir);
                continue;
            }
            new_user.put(user_key, user_value);
        }
        if (new_user.containsKey("password")) {
            String pass = new_user.getProperty("password", "");
            pass = ServerStatus.thisObj.common_code.encode_pass(pass, ServerStatus.SG("password_encryption"), new_user.getProperty("salt", ""));
            new_user.put("password", pass);
        }
        new_user.put("root_dir", "/");
        UserTools.writeUser(new_user.getProperty("serverGroup"), new_user.getProperty("username"), new_user);
        UserTools.ut.getUser(new_user.getProperty("serverGroup"), new_user.getProperty("username"), true);
        VFS vfs = UserTools.ut.getVFS(new_user.getProperty("serverGroup"), new_user.getProperty("username"));
        Properties virtual = (Properties)vfs.homes.elementAt(0);
        int x = 0;
        while (x < user_dirs.size()) {
            user_dir = (Properties)user_dirs.get(x);
            String dir = user_dir.getProperty("dir");
            if (dir.endsWith("/")) {
                dir = dir.substring(0, dir.length() - 1);
            }
            String name = com.crushftp.client.Common.last(dir);
            Properties item = new Properties();
            if (dir.indexOf("FILE:") >= 0) {
                Vector<Properties> vItems = new Vector<Properties>();
                Properties vItem = new Properties();
                vItem.put("url", user_dir.getProperty("dir"));
                vItem.put("type", "DIR");
                vItems.add(vItem);
                item.put("vItems", vItems);
                item.put("type", "FILE");
            } else {
                item.put("type", "DIR");
            }
            item.put("virtualPath", "/" + name);
            item.put("name", name);
            virtual.put("/" + name, item);
            Vector priv = (Vector)virtual.get("vfs_permissions_object");
            Properties priv_p = (Properties)priv.get(0);
            priv_p.put("/" + name.toUpperCase() + "/", user_dir.getProperty("priv"));
            ++x;
        }
        UserTools.writeVFS(new_user.getProperty("serverGroup"), new_user.getProperty("username"), vfs);
        return the_line;
    }

    public static String convertPreview(Properties item) {
        try {
            return com.crushftp.client.Common.dots(String.valueOf(SearchHandler.getPreviewPath(item.getProperty("url"), "1", 1)) + "../index.txt");
        }
        catch (Exception e) {
            com.crushftp.client.Common.log("SERVER", 1, e);
            return "" + e;
        }
    }

    public static String getPreviewPath(Properties item) {
        try {
            String the_dir = SearchHandler.getPreviewPath(item.getProperty("url").toString(), "1", 1);
            String index = String.valueOf(ServerStatus.SG("previews_path")) + the_dir.substring(1);
            String path_org = "";
            if (new File_U(com.crushftp.client.Common.all_but_last(index)).exists()) {
                path_org = index.substring(0, index.indexOf("/p1/"));
                return path_org;
            }
            return "Error: The specified preview path does not exists!";
        }
        catch (Exception e) {
            com.crushftp.client.Common.log("SERVER", 1, e);
            return "Error: " + e;
        }
    }

    public static String sendEmail(String to, String cc, String bcc, String from, String reply_to, String subject, String body, File_B[] files) {
        return TaskBridge.sendEmail(to, cc, bcc, from, reply_to, subject, body, files, new Vector());
    }

    public static String sendEmail(String to, String cc, String bcc, String from, String reply_to, String subject, String body, File_B[] files, Vector fileMimeTypes) {
        return com.crushftp.client.Common.send_mail(ServerStatus.SG("discovered_ip"), to, cc, bcc, from, reply_to, subject, body, ServerStatus.SG("smtp_server"), ServerStatus.SG("smtp_user"), ServerStatus.SG("smtp_pass"), ServerStatus.BG("smtp_ssl"), ServerStatus.BG("smtp_html"), files, fileMimeTypes);
    }

    public static String sendEmail(String to, String cc, String bcc, String from, String reply_to, String subject, String body, File_B[] files, Vector fileMimeTypes, Vector remoteFiles) {
        if (com.crushftp.client.Common.dmz_mode) {
            Vector queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
            Properties action = new Properties();
            action.put("type", "PUT:SEND_EMAIL");
            action.put("id", com.crushftp.client.Common.makeBoundary(11));
            action.put("need_response", "true");
            action.put("to", to == null ? "" : to);
            action.put("cc", cc == null ? "" : cc);
            action.put("bcc", bcc == null ? "" : bcc);
            action.put("from", from == null ? "" : from);
            action.put("reply_to", reply_to == null ? "" : reply_to);
            action.put("subject", subject == null ? "" : subject);
            action.put("body", body == null ? "" : body);
            queue.addElement(action);
            Log.log("SERVER", 2, "PUT:SEND_EMAIL:Sending email over to Internal server..." + action);
            action = UserTools.waitResponse(action, 60);
            Log.log("SERVER", 2, "PUT:SEND_EMAIL:Got response.." + action);
            if (action != null && action.containsKey("email_response")) {
                return action.getProperty("email_response", "DMZ send through internal failed.");
            }
            return "DMZ send through internal failed.";
        }
        return com.crushftp.client.Common.send_mail(ServerStatus.SG("discovered_ip"), to, cc, bcc, from, reply_to, subject, body, ServerStatus.SG("smtp_server"), ServerStatus.SG("smtp_user"), ServerStatus.SG("smtp_pass"), ServerStatus.BG("smtp_ssl"), ServerStatus.BG("smtp_html"), files, fileMimeTypes, remoteFiles);
    }

    public static void runPluginMessageAlert(String msg, Properties info) {
        ServerStatus.thisObj.runAlerts(msg, info, null, null);
    }

    public static Properties doEventItem(Properties fileItem, String event_type, String user_ip, String sessionID, String SESSION_RID) {
        fileItem = (Properties)fileItem.clone();
        VRL vrl = new VRL(fileItem.getProperty("url"));
        Log.log("FTP_SERVER", 2, "Tracking " + event_type + ":" + vrl.getPath());
        fileItem.put("the_command", event_type);
        if (fileItem.containsKey("root_dir")) {
            fileItem.put("the_file_path", String.valueOf(fileItem.getProperty("root_dir")) + fileItem.getProperty("the_file_name", fileItem.getProperty("name")) + (fileItem.getProperty("type").equals("DIR") ? "/" : ""));
        } else {
            fileItem.put("the_file_path", vrl.getPath());
        }
        fileItem.put("the_command_data", fileItem.getProperty("the_file_path"));
        fileItem.put("the_file_name", fileItem.getProperty("name"));
        fileItem.put("the_file_size", fileItem.getProperty("size"));
        fileItem.put("the_file_speed", "0");
        fileItem.put("the_file_start", String.valueOf(new Date().getTime()));
        fileItem.put("the_file_end", String.valueOf(new Date().getTime()));
        fileItem.put("the_file_error", "");
        fileItem.put("the_file_type", fileItem.getProperty("type", ""));
        fileItem.put("the_file_status", "SUCCESS");
        ServerStatus.thisObj.statTools.add_item_stat(user_ip, sessionID, SESSION_RID, fileItem, event_type);
        return fileItem;
    }

    public static Properties getEmailTemplate(String template_name) {
        return Common.get_email_template(template_name);
    }

    public static Object readJobXMLObject(File_S f) {
        return JobFilesHandler.readXMLObject(f);
    }

    public static Object readJobXMLObject(Properties p) {
        return JobFilesHandler.readXMLObject(p, false);
    }

    public static void writeXMLObject(String path, Object obj, String root) {
        JobFilesHandler.writeXMLObject(path, obj, root);
    }

    public static Object change_vars_to_values_static(String s, Properties user, Properties user_info) {
        return ServerStatus.change_vars_to_values_static(s, user, user_info, null);
    }

    public static Object BG(String s) {
        return String.valueOf(ServerStatus.BG(s));
    }

    public static String SG(String s) {
        return ServerStatus.SG(s);
    }

    public static Object siVG(String s) {
        return ServerStatus.siVG(s);
    }

    public static Object addTotalBytesReceived(String s) {
        ServerStatus.thisObj.total_server_bytes_received += Long.parseLong(s);
        return String.valueOf(ServerStatus.thisObj.total_server_bytes_received);
    }

    public static Object addTotalBytesSent(String s) {
        ServerStatus.thisObj.total_server_bytes_sent += Long.parseLong(s);
        return String.valueOf(ServerStatus.thisObj.total_server_bytes_sent);
    }

    public static Object add_login_stat(String username, String user_ip, String session_id) {
        ServerStatus serverStatus = ServerStatus.thisObj;
        long rid = serverStatus.statTools.u();
        try {
            ServerStatus.thisObj.statTools.add_login_stat("Job", username, user_ip, true, session_id, rid);
        }
        catch (Exception e) {
            com.crushftp.client.Common.log("SERVER", 1, e);
        }
        return String.valueOf(rid);
    }

    public static Object add_item_stat(String session_id, String rid, Properties item2, String direction) {
        ServerStatus.thisObj.statTools.add_item_stat("127.0.0.2", session_id, rid, item2, direction);
        return rid;
    }

    public static Object stats_update_sessions(String rid) {
        if (ServerStatus.BG("job_statistics_enabled")) {
            ServerStatus.thisObj.statTools.executeSql(ServerStatus.SG("stats_update_sessions"), new Object[]{new Date(), rid});
        }
        return String.valueOf(rid);
    }
}

