/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.compress.archivers.ArchiveEntry
 *  org.apache.commons.compress.archivers.zip.ZipArchiveEntry
 *  org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
 */
package crushftp.server;

import com.crushftp.client.AgentUI;
import com.crushftp.client.Base64;
import com.crushftp.client.CitrixClient;
import com.crushftp.client.DropBoxClient;
import com.crushftp.client.FileClient;
import com.crushftp.client.File_B;
import com.crushftp.client.File_S;
import com.crushftp.client.File_U;
import com.crushftp.client.GDriveClient;
import com.crushftp.client.GenericClient;
import com.crushftp.client.HTTPClient;
import com.crushftp.client.URLConnection;
import com.crushftp.client.VRL;
import com.crushftp.client.Variables;
import com.crushftp.client.Worker;
import com.crushftp.tunnel2.Tunnel2;
import com.crushftp.tunnel3.StreamController;
import crushftp.db.SearchHandler;
import crushftp.gui.LOC;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.handlers.PreviewWorker;
import crushftp.handlers.SessionCrush;
import crushftp.handlers.SharedSession;
import crushftp.handlers.SharedSessionReplicated;
import crushftp.handlers.SyncTools;
import crushftp.handlers.TaskBridge;
import crushftp.handlers.UserTools;
import crushftp.handlers.WebTransfer;
import crushftp.server.AdminControls;
import crushftp.server.As2Msg;
import crushftp.server.LIST_handler;
import crushftp.server.RETR_handler;
import crushftp.server.STOR_handler;
import crushftp.server.ServerSessionHTTP;
import crushftp.server.ServerSessionHTTPWI;
import crushftp.server.ServerSessionTunnel3;
import crushftp.server.ServerStatus;
import crushftp.server.VFS;
import crushftp.server.daemon.DMZServerCommon;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

public class ServerSessionAJAX {
    static Vector reset_requests = new Vector();
    ServerSessionHTTP thisSessionHTTP = null;

    public ServerSessionAJAX(ServerSessionHTTP thisSessionHTTP) {
        this.thisSessionHTTP = thisSessionHTTP;
    }

    public boolean checkLogin1(Properties request) throws Exception {
        if (this.thisSessionHTTP.thisSession.server_item.getProperty("recaptcha_enabled", "false").equals("true") && !this.validate_recaptcha(request)) {
            return false;
        }
        if (request.getProperty("clientid", "").equalsIgnoreCase(String.valueOf(System.getProperty("appname", "CrushFTP")) + "Drive") && ServerStatus.siIG("enterprise_level") <= 0) {
            return false;
        }
        if (ServerStatus.BG("username_uppercase")) {
            request.put("username", request.getProperty("username").toUpperCase());
        }
        if (ServerStatus.BG("lowercase_usernames")) {
            request.put("username", request.getProperty("username").toLowerCase());
        }
        this.thisSessionHTTP.thisSession.uiPUT("user_name", "anonymous");
        this.thisSessionHTTP.thisSession.uiPUT("user_name_original", VRL.vrlDecode(this.thisSessionHTTP.thisSession.uiSG("user_name")));
        this.thisSessionHTTP.thisSession.uiPUT("current_password", VRL.vrlDecode(request.getProperty("password")));
        this.thisSessionHTTP.thisSession.uiPUT("user_name", VRL.vrlDecode(request.getProperty("username")));
        this.thisSessionHTTP.thisSession.uiPUT("clientid", request.getProperty("clientid", ""));
        this.thisSessionHTTP.thisSession.uiPUT("SAMLResponse", request.getProperty("SAMLResponse", ""));
        this.thisSessionHTTP.thisSession.uiPUT("user_name_original", this.thisSessionHTTP.thisSession.uiSG("user_name"));
        this.thisSessionHTTP.this_thread.setName(String.valueOf(this.thisSessionHTTP.thisSession.uiSG("user_name")) + ":(" + this.thisSessionHTTP.thisSession.uiSG("user_number") + ")-" + this.thisSessionHTTP.thisSession.uiSG("user_ip") + " (control)");
        this.thisSessionHTTP.thisSession.uiPUT("skip_proxy_check", "false");
        this.thisSessionHTTP.thisSession.runPlugin("beforeLogin", null);
        boolean good = this.thisSessionHTTP.thisSession.login_user_pass();
        this.thisSessionHTTP.setupSession();
        if (!good) {
            ServerStatus.siPUT2("failed_logins", "" + (ServerStatus.IG("failed_logins") + 1));
            this.thisSessionHTTP.thisSession.logLogin(false, "");
        }
        return good;
    }

    public String checkLogin2(String response1, Properties request) {
        String response2;
        block17: {
            String session_id;
            response2 = "";
            if (this.thisSessionHTTP.thisSession.uiBG("password_expired")) {
                response2 = ServerStatus.BG("expire_password_email_token_only") ? "<loginResult><response>failure</response><message>Password Expired. A Reset password email was sent. Use the reset password link from the email to change your password.</message></loginResult>" : "<loginResult><response>password_expired</response><message>You must change your password.</message></loginResult>";
                this.thisSessionHTTP.thisSession.logLogin(false, "Expired password.");
            } else if (this.thisSessionHTTP.thisSession.BG("recaptcha_required_web")) {
                if (!request.getProperty("recaptcha_response_field", "").equals("") || !request.getProperty("g-recaptcha-response", "").equals("")) {
                    try {
                        if (this.validate_recaptcha(request)) {
                            session_id = this.thisSessionHTTP.thisSession.getId();
                            response2 = "<loginResult><response>success</response><c2f>" + session_id.substring(session_id.length() - 4) + "</c2f></loginResult>";
                            this.thisSessionHTTP.thisSession.logLogin(true, "Recapcha validated.");
                            break block17;
                        }
                        response2 = "<loginResult><response>failure</response><message>reCAPTCHA failed.</message></loginResult>";
                        this.thisSessionHTTP.thisSession.logLogin(false, "Recapcha failed.");
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 0, e);
                        response2 = "<loginResult><response>failure</response><message>ERROR:" + e + "</message></loginResult>";
                        this.thisSessionHTTP.thisSession.logLogin(false, "" + e);
                    }
                } else {
                    response2 = "<loginResult><response>RECAPTCHA</response><message>reCAPTCHA required.</message></loginResult>";
                    this.thisSessionHTTP.thisSession.logLogin(false, "Recapcha missing.");
                }
            } else {
                session_id = this.thisSessionHTTP.thisSession.getId();
                response2 = "<loginResult><response>success</response><c2f>" + session_id.substring(session_id.length() - 4) + "</c2f></loginResult>";
                this.thisSessionHTTP.thisSession.logLogin(true, "");
            }
        }
        this.thisSessionHTTP.createCookieSession(false);
        Enumeration<Object> keys = request.keys();
        Properties request2 = (Properties)request.clone();
        while (keys.hasMoreElements()) {
            String key = "" + keys.nextElement();
            String val = request.getProperty(key, "");
            if (key.toUpperCase().indexOf("PASSWORD") >= 0) {
                val = "******************";
            }
            request2.put(key, val);
            this.thisSessionHTTP.thisSession.user_info.put("post_" + key, val);
        }
        this.thisSessionHTTP.thisSession.user_info.put("post_parameters", request2);
        Properties p = this.thisSessionHTTP.thisSession.do_event5("LOGIN", null);
        this.thisSessionHTTP.writeCookieAuth = true;
        if (p != null && p.containsKey("allowLogin") && p.getProperty("allowLogin", "true").equals("false")) {
            return String.valueOf(response1) + "<loginResult><response>failure</response><message>A plugin rejected the login. Login failed.</message></loginResult>";
        }
        SessionCrush session = (SessionCrush)SharedSession.find("crushftp.sessions").get(this.thisSessionHTTP.thisSession.getId());
        if (session == null) {
            response2 = "<loginResult><response>failure</response><message>session expired</message></loginResult>";
            this.thisSessionHTTP.thisSession.logLogin(false, "Session expired.");
        } else {
            session.put("expire_time", "0");
            if (this.thisSessionHTTP.thisSession.IG("max_login_time") != 0) {
                session.put("expire_time", String.valueOf(System.currentTimeMillis() + (long)(this.thisSessionHTTP.thisSession.IG("max_login_time") * 60000)));
            }
            if (request.containsKey("clientid")) {
                session.put("clientid", request.getProperty("clientid"));
                this.thisSessionHTTP.thisSession.user_info.put("clientid", request.getProperty("clientid"));
            }
            session.put("SESSION_RID", this.thisSessionHTTP.thisSession.uiSG("SESSION_RID"));
            if (this.thisSessionHTTP.thisSession.user_info.containsKey("eventResultText")) {
                response1 = this.thisSessionHTTP.thisSession.user_info.getProperty("eventResultText");
            }
        }
        return String.valueOf(response1) + response2;
    }

    public boolean validate_recaptcha(Properties request) throws Exception {
        if (this.thisSessionHTTP.thisSession.server_item.getProperty("recaptcha_version", "2").equals("1")) {
            return true;
        }
        if (this.thisSessionHTTP.thisSession.server_item.getProperty("recaptcha_version", "2").equals("2") || this.thisSessionHTTP.thisSession.server_item.getProperty("recaptcha_version", "2").equals("3")) {
            boolean ok = false;
            String s = "secret=" + this.thisSessionHTTP.thisSession.server_item.getProperty("recaptcha_private_key", "") + "&remoteip=" + this.thisSessionHTTP.thisSession.uiSG("user_ip") + "&response=" + request.getProperty("g-recaptcha-response", "");
            Log.log("SERVER", 2, "Recaptcha:response=" + request.getProperty("g-recaptcha-response", ""));
            String result = "";
            if (com.crushftp.client.Common.dmz_mode) {
                Vector queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                Properties action = new Properties();
                action.put("type", "GET:SINGLETON");
                action.put("id", Common.makeBoundary());
                action.put("need_response", "true");
                queue.addElement(action);
                action = UserTools.waitResponse(action, 60);
                String singleton_id = "";
                if (action != null) {
                    singleton_id = "" + action.get("singleton_id");
                }
                action = new Properties();
                action.put("type", "GET:RECAPTCHA_RESPONSE");
                action.put("id", Common.makeBoundary());
                action.put("recapcha_info", s);
                action.put("need_response", "true");
                action.put("singleton_id", singleton_id);
                queue.addElement(action);
                action = UserTools.waitResponse(action, 60);
                if (action != null) {
                    result = action.getProperty("responseText", "");
                }
            } else {
                result = ServerSessionAJAX.getRecaptchaResponse(s);
            }
            BufferedReader br = new BufferedReader(new StringReader(result));
            float score = 1.0f;
            while ((result = br.readLine()) != null) {
                if (result.indexOf("true") >= 0) {
                    ok = true;
                }
                if (result.indexOf("score") >= 0) {
                    score = Float.parseFloat(result.substring(result.indexOf(":") + 1, result.indexOf(",")).trim());
                }
                Log.log("SERVER", 2, "Recaptcha:" + result);
            }
            if (score < Float.parseFloat(this.thisSessionHTTP.thisSession.server_item.getProperty("recaptcha_score_min", "0.5"))) {
                ok = false;
            }
            br.close();
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    public static String getRecaptchaResponse(String s) throws Exception {
        Properties recaptcha_response;
        if (!ServerStatus.thisObj.server_info.containsKey("recaptcha_response")) {
            ServerStatus.thisObj.server_info.put("recaptcha_response", new Properties());
        }
        if ((recaptcha_response = (Properties)ServerStatus.thisObj.server_info.get("recaptcha_response")).containsKey(s)) {
            Properties response = (Properties)recaptcha_response.get(s);
            if (System.currentTimeMillis() - Long.parseLong(response.getProperty("time")) > 120000L) {
                recaptcha_response.remove(response);
            } else {
                return response.getProperty("response", "");
            }
        }
        HttpURLConnection urlc = (HttpURLConnection)new URL("https://www.google.com/recaptcha/api/siteverify").openConnection();
        urlc.setDoOutput(true);
        OutputStream out = urlc.getOutputStream();
        out.write(s.getBytes("UTF8"));
        out.close();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Common.streamCopier(urlc.getInputStream(), baos, false, true, true);
        urlc.disconnect();
        String result = new String(baos.toByteArray());
        Properties response = new Properties();
        response.put("time", String.valueOf(System.currentTimeMillis()));
        response.put("response", result);
        recaptcha_response.put(s, response);
        return result;
    }

    public boolean processItemAnonymous(Properties request, Properties urlRequestItems, String req_id) throws Exception {
        String command = request.getProperty("command", "");
        if (command.equalsIgnoreCase("ping")) {
            String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
            response = String.valueOf(response) + "<pong>" + System.currentTimeMillis() + "</pong>";
            this.thisSessionHTTP.done = true;
            return this.writeResponse(response);
        }
        if (command.equalsIgnoreCase("loginSettings")) {
            String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
            response = String.valueOf(response) + "<info><save_password>" + ServerStatus.BG("allow_save_pass_phone") + "</save_password></info>";
            this.thisSessionHTTP.done = true;
            return this.writeResponse(response);
        }
        if (command.equalsIgnoreCase("discard")) {
            this.thisSessionHTTP.done = true;
            return this.writeResponse("");
        }
        if (request.getProperty("encoded", "true").equals("true") && (command.equalsIgnoreCase("login") || request.getProperty("the_action", "").equalsIgnoreCase("login"))) {
            this.thisSessionHTTP.createCookieSession(true);
            request.put("username", request.getProperty("username", "").trim());
            String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
            if (this.checkLogin1(request)) {
                response = this.checkLogin2(response, request);
                if (!request.getProperty("redirect", "").equals("")) {
                    this.thisSessionHTTP.sendRedirect("/WebInterface/error.html");
                    this.write_command_http("Connection: close");
                    this.write_command_http("Content-Length: 0");
                    this.write_command_http("");
                    return true;
                }
                this.thisSessionHTTP.thisSession.active();
            } else {
                this.thisSessionHTTP.thisSession.uiPUT("user_name", "");
                this.thisSessionHTTP.thisSession.uiPUT("user_name_original", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                Properties user_tmp = null;
                boolean hack = false;
                String user_name = VRL.vrlDecode(request.getProperty("username"));
                if (ServerStatus.BG("block_hack_username_immediately") && !user_name.equals("") && !user_name.equalsIgnoreCase("anonymous")) {
                    hack = this.thisSessionHTTP.thisSession.checkHackUsernames(user_name, false);
                }
                if (!hack) {
                    user_tmp = UserTools.ut.getUser(this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer"), user_name, true);
                }
                if (user_tmp != null && user_tmp.getProperty("recaptcha_required_web", "").equals("true")) {
                    response = String.valueOf(response) + "<loginResult><response>RECAPTCHA</response><message>reCAPTCHA required.</message></loginResult>";
                } else if (this.thisSessionHTTP.thisSession.uiSG("plugin_message").indexOf("CHALLENGE:") >= 0) {
                    response = String.valueOf(response) + "<loginResult><response>challenge</response><message>" + Common.url_encode(this.thisSessionHTTP.thisSession.uiSG("lastLog")) + "</message></loginResult>";
                } else if (this.thisSessionHTTP.thisSession.uiSG("lastLog").indexOf("CHALLENGE_OTP:") >= 0) {
                    String last_log = this.thisSessionHTTP.thisSession.uiSG("lastLog");
                    response = String.valueOf(response) + "<loginResult><response>challenge_otp</response><message>" + Common.url_encode(last_log.substring(last_log.indexOf(":") + 1)) + "</message></loginResult>";
                } else if (this.thisSessionHTTP.thisSession.uiSG("lastLog").indexOf("<response>") >= 0 && this.thisSessionHTTP.thisSession.uiSG("lastLog").indexOf("<message>") >= 0) {
                    String message = this.thisSessionHTTP.thisSession.uiSG("lastLog").substring(this.thisSessionHTTP.thisSession.uiSG("lastLog").indexOf("<message>") + "<message>".length(), this.thisSessionHTTP.thisSession.uiSG("lastLog").indexOf("</message>"));
                    response = String.valueOf(response) + "<loginResult><response>failure</response><message>" + Common.url_encode(Common.url_decode(message)) + "</message></loginResult>";
                } else {
                    response = String.valueOf(response) + "<loginResult><response>failure</response><message>Check your username or password and try again.\r\n" + Common.url_encode(this.thisSessionHTTP.thisSession.uiSG("lastLog")) + "</message></loginResult>";
                }
            }
            return this.writeResponse(response);
        }
        if (request.containsKey("u") || request.containsKey("p")) {
            this.thisSessionHTTP.createCookieSession(true);
            this.thisSessionHTTP.thisSession.uiPUT("user_name", "anonymous");
            this.thisSessionHTTP.thisSession.uiPUT("user_name_original", this.thisSessionHTTP.thisSession.uiSG("user_name"));
            request.put("password", request.getProperty("p", ""));
            request.put("username", request.getProperty("u", ""));
            this.thisSessionHTTP.thisSession.uiPUT("skip_two_factor", "true");
            boolean good = this.checkLogin1(request);
            this.thisSessionHTTP.thisSession.uiPUT("skip_two_factor", "false");
            if (good) {
                this.checkLogin2("", request);
                String autoPath = request.getProperty("path", "/");
                urlRequestItems.remove("p");
                this.thisSessionHTTP.thisSession.active();
                autoPath = autoPath.replace('\\', '/');
                while (autoPath.indexOf("//") >= 0) {
                    autoPath = autoPath.replaceAll("//", "/");
                }
                if (!this.thisSessionHTTP.thisSession.user_info.getProperty("redirect_url", "").equals("")) {
                    autoPath = this.thisSessionHTTP.thisSession.user_info.getProperty("redirect_url", "");
                    this.thisSessionHTTP.thisSession.user_info.put("redirect_url", "");
                }
                if (!autoPath.endsWith("/") && !autoPath.equals("") && autoPath.indexOf("/WebInterface/") < 0 && ServerStatus.BG("direct_link_access")) {
                    String header0 = this.thisSessionHTTP.headers.elementAt(0).toString();
                    header0 = String.valueOf(header0.substring(0, header0.indexOf(" "))) + " " + com.crushftp.client.Common.dots(autoPath) + header0.substring(header0.lastIndexOf(" "));
                    this.thisSessionHTTP.headers.setElementAt(header0, 0);
                    request.remove("path");
                    return false;
                }
                Properties wi_customizations = new Properties();
                Properties metas = new Properties();
                Enumeration<Object> keys = request.keys();
                while (keys.hasMoreElements()) {
                    String val;
                    String key = "" + keys.nextElement();
                    if (key.toLowerCase().startsWith("wi_")) {
                        val = request.remove(key).toString();
                        val = Common.url_decode(val);
                        wi_customizations.put(key.substring(3), val);
                        continue;
                    }
                    if (!key.toLowerCase().startsWith("meta_")) continue;
                    val = request.remove(key).toString();
                    val = Common.url_decode(val);
                    metas.put(key.substring(5), val);
                }
                if (wi_customizations.size() > 0) {
                    this.thisSessionHTTP.thisSession.put("wi_customizations", wi_customizations);
                }
                if (metas.size() > 0) {
                    this.thisSessionHTTP.thisSession.put("metas", metas);
                }
                if (ServerStatus.BG("direct_link_to_webinterface") && !autoPath.startsWith("/WebInterface/")) {
                    this.thisSessionHTTP.sendRedirect("/#" + autoPath);
                    this.write_command_http("Content-Length: 0");
                    this.write_command_http("");
                    return true;
                }
                this.thisSessionHTTP.sendRedirect(autoPath);
                this.write_command_http("Content-Length: 0");
                this.write_command_http("");
                return true;
            }
            if (!this.thisSessionHTTP.thisSession.user_info.getProperty("redirect_url", "").equals("")) {
                this.thisSessionHTTP.sendRedirect(this.thisSessionHTTP.thisSession.user_info.getProperty("redirect_url", ""));
                this.write_command_http("Content-Length: 0");
                this.write_command_http("");
                return true;
            }
            if (!request.getProperty("u", "").equals("") && !request.getProperty("p", "").equals("")) {
                this.thisSessionHTTP.sendRedirect("/WebInterface/login.html?u=" + request.getProperty("u") + (ServerStatus.BG("webinterface_redirect_with_password") ? "&p=" + request.getProperty("p") : ""));
            } else if (!request.getProperty("u", "").equals("") && request.getProperty("p", "").equals("")) {
                this.thisSessionHTTP.sendRedirect("/WebInterface/login.html?u=" + request.getProperty("u"));
            } else if (request.getProperty("u", "").equals("") && !request.getProperty("p", "").equals("")) {
                this.thisSessionHTTP.sendRedirect("/WebInterface/login.html?p=" + request.getProperty("p"));
            } else if (request.getProperty("u", "").equals("") && request.getProperty("p", "").equals("") && !request.getProperty("path", "").equals("")) {
                this.thisSessionHTTP.sendRedirect("/WebInterface/login.html?path=" + request.getProperty("path"));
            }
            this.write_command_http("Content-Length: 0");
            this.write_command_http("");
            return true;
        }
        if (command.equalsIgnoreCase("emailpassword") || request.getProperty("the_action", "").equalsIgnoreCase("emailpassword")) {
            String response = ServerSessionAJAX.doEmailPass(request, this.thisSessionHTTP.thisSession, req_id);
            response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n<emailPass><response>" + response + "</response></emailPass>";
            return this.writeResponse(response);
        }
        if (command.equalsIgnoreCase("request_reset")) {
            String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
            String reset_username_email = Common.url_decode(request.getProperty("reset_username_email"));
            String responseText = "";
            String token = Common.makeBoundary();
            if (com.crushftp.client.Common.dmz_mode) {
                Vector queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                Properties action = new Properties();
                action.put("type", "GET:SINGLETON");
                action.put("id", Common.makeBoundary());
                action.put("need_response", "true");
                queue.addElement(action);
                action = UserTools.waitResponse(action, 60);
                String singleton_id = "";
                if (action != null) {
                    singleton_id = "" + action.get("singleton_id");
                }
                action = new Properties();
                action.put("type", "GET:RESET_TOKEN");
                action.put("id", Common.makeBoundary());
                action.put("reset_host", this.thisSessionHTTP.hostString);
                action.put("reset_username_email", reset_username_email);
                action.put("lang", request.getProperty("language", "en"));
                action.put("currentURL", request.getProperty("currentURL"));
                try {
                    Properties tmp_vfs_item = (Properties)UserTools.ut.getVirtualVFS(this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer"), "template").get("/internal");
                    VRL tmp_url = new VRL(((Properties)((Vector)tmp_vfs_item.get("vItems")).elementAt(0)).getProperty("url"));
                    action.put("internal_port", String.valueOf(tmp_url.getPort()));
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                }
                action.put("need_response", "true");
                action.put("reset_token", token);
                action.put("singleton_id", singleton_id);
                queue.addElement(action);
                action = UserTools.waitResponse(action, 300);
                if (action != null) {
                    responseText = "" + action.get("responseText");
                }
            } else {
                responseText = ServerSessionAJAX.doResetToken(this.thisSessionHTTP.hostString, reset_username_email, request.getProperty("currentURL"), this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer"), token, true, request.getProperty("language", "en"));
            }
            response = String.valueOf(response) + "<commandResult><response>" + Common.url_encode(responseText) + "</response></commandResult>";
            return this.writeResponse(response);
        }
        if (command.equalsIgnoreCase("reset_password")) {
            String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
            String responseText = "This password reset link is invalid or expired.";
            if (com.crushftp.client.Common.dmz_mode) {
                Vector queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                Properties action = new Properties();
                action.put("type", "GET:RESET_TOKEN_PASS");
                action.put("id", Common.makeBoundary());
                action.put("resetToken", request.getProperty("resetToken"));
                action.put("password1", request.getProperty("password1"));
                try {
                    Properties tmp_vfs_item = (Properties)UserTools.ut.getVirtualVFS(this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer"), "template").get("/internal");
                    VRL tmp_url = new VRL(((Properties)((Vector)tmp_vfs_item.get("vItems")).elementAt(0)).getProperty("url"));
                    action.put("internal_port", String.valueOf(tmp_url.getPort()));
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                }
                action.put("need_response", "true");
                queue.addElement(action);
                action = UserTools.waitResponse(action, 300);
                if (action != null) {
                    responseText = "" + action.get("responseText");
                }
            } else {
                responseText = ServerSessionAJAX.doResetTokenPass(request.getProperty("resetToken"), this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer"), request.getProperty("password1"), this.thisSessionHTTP.thisSession.user_info);
            }
            response = String.valueOf(response) + "<commandResult><response>" + responseText + "</response></commandResult>";
            return this.writeResponse(response);
        }
        if (command.equalsIgnoreCase("getSessionTimeout")) {
            long time = Long.parseLong(this.thisSessionHTTP.thisSession.getProperty("last_activity", String.valueOf(System.currentTimeMillis())));
            long timeout = 60L * ServerStatus.LG("http_session_timeout");
            if (this.thisSessionHTTP.thisSession.user != null) {
                long timeout2 = Long.parseLong(this.thisSessionHTTP.thisSession.user.getProperty("max_idle_time", "10"));
                if (timeout2 < 0L) {
                    timeout = timeout2 * -1L;
                } else if (timeout2 != 0L && timeout2 < timeout) {
                    timeout = 60L * timeout2;
                }
            }
            long remaining = timeout - (new Date().getTime() / 1000L - time / 1000L);
            try {
                if (!this.thisSessionHTTP.thisSession.getProperty("expire_time").equals("0")) {
                    remaining = (Long.parseLong(this.thisSessionHTTP.thisSession.getProperty("expire_time")) - System.currentTimeMillis()) / 1000L;
                }
            }
            catch (Exception singleton_id) {
                // empty catch block
            }
            if (!this.thisSessionHTTP.thisSession.uiBG("user_logged_in")) {
                Thread.sleep(10000L);
                if (!this.thisSessionHTTP.thisSession.uiBG("user_logged_in")) {
                    remaining = 0L;
                }
            }
            String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
            response = String.valueOf(response) + "<commandResult><response>" + remaining + "</response>";
            if (!this.thisSessionHTTP.thisSession.getProperty("admin_message", "").equals("")) {
                response = String.valueOf(response) + "<msg>" + Common.url_encode(this.thisSessionHTTP.thisSession.getProperty("admin_message", "")) + "</msg>";
                this.thisSessionHTTP.thisSession.put("admin_message", "");
            }
            response = String.valueOf(response) + "</commandResult>";
            return this.writeResponse(response);
        }
        if (command.equalsIgnoreCase("register_gdrive_api")) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write("Finished.".getBytes());
            this.thisSessionHTTP.thisSession.put("gdrive_api_code", request.getProperty("code"));
            this.write_command_http("HTTP/1.1 200 OK");
            this.write_command_http("Cache-Control: no-store");
            this.write_command_http("Content-Type: text/html");
            this.thisSessionHTTP.write_standard_headers();
            this.write_command_http("Content-Length: " + baos.size());
            this.write_command_http("");
            this.thisSessionHTTP.original_os.write(baos.toByteArray());
            this.thisSessionHTTP.original_os.flush();
            return true;
        }
        if (command.equalsIgnoreCase("lookup_gdrive_api_code")) {
            String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
            if (this.thisSessionHTTP.thisSession.getProperty("gdrive_api_code") != null) {
                String gdrive_api_code = "" + this.thisSessionHTTP.thisSession.remove("gdrive_api_code");
                Properties p = GDriveClient.setup_bearer(gdrive_api_code, request.getProperty("server_url"), request.getProperty("google_client_info").split("~")[0], request.getProperty("google_client_info").split("~")[1]);
                response = String.valueOf(response) + "<commandResult><response>" + p.getProperty("refresh_token", p.getProperty("access_token")) + "</response></commandResult>";
            } else {
                response = String.valueOf(response) + "<commandResult><response></response></commandResult>";
            }
            return this.writeResponse(response);
        }
        if (command.equalsIgnoreCase("register_citrix_api")) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write("Finished.".getBytes());
            this.thisSessionHTTP.thisSession.put("citrix_api_subdomain", request.getProperty("subdomain"));
            this.thisSessionHTTP.thisSession.put("citrix_api_apicp", request.getProperty("apicp"));
            this.thisSessionHTTP.thisSession.put("citrix_api_code", request.getProperty("code"));
            this.write_command_http("HTTP/1.1 200 OK");
            this.write_command_http("Cache-Control: no-store");
            this.write_command_http("Content-Type: text/html");
            this.thisSessionHTTP.write_standard_headers();
            this.write_command_http("Content-Length: " + baos.size());
            this.write_command_http("");
            this.thisSessionHTTP.original_os.write(baos.toByteArray());
            this.thisSessionHTTP.original_os.flush();
            return true;
        }
        if (command.equalsIgnoreCase("register_dropbox_api")) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write("Finished.".getBytes());
            this.thisSessionHTTP.thisSession.put("dropbox_api_code", request.getProperty("code"));
            this.write_command_http("HTTP/1.1 200 OK");
            this.write_command_http("Cache-Control: no-store");
            this.write_command_http("Content-Type: text/html");
            this.thisSessionHTTP.write_standard_headers();
            this.write_command_http("Content-Length: " + baos.size());
            this.write_command_http("");
            this.thisSessionHTTP.original_os.write(baos.toByteArray());
            this.thisSessionHTTP.original_os.flush();
            return true;
        }
        if (command.equalsIgnoreCase("lookup_dropbox_api_code")) {
            String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
            if (this.thisSessionHTTP.thisSession.getProperty("dropbox_api_code") != null) {
                String refresh_token = "";
                try {
                    String dropbox_api_code = "" + this.thisSessionHTTP.thisSession.remove("dropbox_api_code");
                    int redirect_index_start = request.getProperty("server_url").indexOf("redirect_uri=") + 13;
                    int redirect_index_end = request.getProperty("server_url").indexOf("&", redirect_index_start);
                    if (redirect_index_end < redirect_index_start) {
                        redirect_index_end = request.getProperty("server_url", "").length();
                    }
                    String redirect_uri = request.getProperty("server_url").substring(redirect_index_start, redirect_index_end);
                    Properties p = DropBoxClient.setup_bearer(dropbox_api_code, redirect_uri, request.getProperty("dropbox_client_info").split("~")[0], request.getProperty("dropbox_client_info").split("~")[1]);
                    refresh_token = p.getProperty("refresh_token", "");
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                }
                response = String.valueOf(response) + "<commandResult><response>" + refresh_token + "</response></commandResult>";
            } else {
                response = String.valueOf(response) + "<commandResult><response></response></commandResult>";
            }
            return this.writeResponse(response);
        }
        if (command.equalsIgnoreCase("register_google_mail_api")) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write("Finished.".getBytes());
            this.thisSessionHTTP.thisSession.put("google_mail_api_code", request.getProperty("code"));
            this.write_command_http("HTTP/1.1 200 OK");
            this.write_command_http("Cache-Control: no-store");
            this.write_command_http("Content-Type: text/html");
            this.thisSessionHTTP.write_standard_headers();
            this.write_command_http("Content-Length: " + baos.size());
            this.write_command_http("");
            this.thisSessionHTTP.original_os.write(baos.toByteArray());
            this.thisSessionHTTP.original_os.flush();
            return true;
        }
        if (command.equalsIgnoreCase("lookup_google_mail_api_code")) {
            String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
            if (this.thisSessionHTTP.thisSession.getProperty("google_mail_api_code") != null) {
                String google_mail_api_code = "" + this.thisSessionHTTP.thisSession.remove("google_mail_api_code");
                Properties p = null;
                try {
                    p = com.crushftp.client.Common.google_get_refresh_token(google_mail_api_code, request.getProperty("server_url"), request.getProperty("google_client_info").split("~")[0], request.getProperty("google_client_info").split("~")[1]);
                }
                catch (Exception e) {
                    Log.log("SERVER", 0, e);
                }
                if (!p.containsKey("error")) {
                    response = String.valueOf(response) + "<commandResult><response>" + p.getProperty("refresh_token", p.getProperty("access_token", "")) + "</response></commandResult>";
                } else {
                    Log.log("SERVER", 0, "ERROR: " + p.getProperty("error"));
                }
            } else {
                response = String.valueOf(response) + "<commandResult><response></response></commandResult>";
            }
            return this.writeResponse(response);
        }
        if (command.equalsIgnoreCase("register_oauth_mail_api")) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write("Finished.".getBytes());
            this.thisSessionHTTP.thisSession.put("oauth_mail_api_code", request.getProperty("code"));
            this.write_command_http("HTTP/1.1 200 OK");
            this.write_command_http("Cache-Control: no-store");
            this.write_command_http("Content-Type: text/html");
            this.thisSessionHTTP.write_standard_headers();
            this.write_command_http("Content-Length: " + baos.size());
            this.write_command_http("");
            this.thisSessionHTTP.original_os.write(baos.toByteArray());
            this.thisSessionHTTP.original_os.flush();
            return true;
        }
        if (command.equalsIgnoreCase("encryptPassword")) {
            String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
            String pass = request.getProperty("password");
            if (pass.length() > 6000) {
                response = String.valueOf(response) + "<commandResult><response>FAILURE:Access Denied.</response></commandResult>";
            } else {
                if (request.getProperty("vrl_decode", "true").equals("true")) {
                    pass = VRL.vrlDecode(pass);
                }
                if (request.getProperty("url_decode", "true").equals("true")) {
                    pass = Common.url_decode(pass);
                }
                if (request.getProperty("vrl_decode", "true").equals("true")) {
                    pass = VRL.vrlDecode(pass);
                }
                if (com.crushftp.client.Common.dmz_mode) {
                    Vector queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                    Properties action = new Properties();
                    action.put("type", "GET:ENCRYPTED_PASS");
                    action.put("id", Common.makeBoundary());
                    action.put("encrypt_type", request.getProperty("encrypt_type", ""));
                    action.put("password", pass);
                    action.put("need_response", "true");
                    queue.addElement(action);
                    action = UserTools.waitResponse(action, 300);
                    String encryptedPass = "";
                    if (action != null) {
                        encryptedPass = "" + action.get("response");
                    }
                    response = String.valueOf(response) + "<result><response>" + Common.url_encode(encryptedPass) + "</response></result>";
                } else {
                    String encrypt_type = request.getProperty("encrypt_type", "");
                    if (encrypt_type.trim().equals("")) {
                        encrypt_type = ServerStatus.SG("password_encryption");
                    }
                    response = String.valueOf(response) + "<result><response>" + Common.url_encode(ServerStatus.thisObj.common_code.encode_pass(pass, encrypt_type, "")) + "</response></result>";
                }
            }
            return this.writeResponse(response);
        }
        if (command.equalsIgnoreCase("taskResponse")) {
            Vector v = ServerStatus.siVG("running_tasks");
            String result = "Invalid task, or key.";
            int x = v.size() - 1;
            while (x >= 0) {
                Properties tracker = (Properties)v.elementAt(x);
                if (tracker.getProperty("id").equals(request.getProperty("job_id", "")) && tracker.containsKey(request.getProperty("task_key", ""))) {
                    tracker.put(request.getProperty("task_key"), request.getProperty("task_val"));
                    Thread.sleep(2300L);
                    result = tracker.getProperty(String.valueOf(request.getProperty("task_key")) + "_result", "No result.");
                }
                --x;
            }
            String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
            response = String.valueOf(response) + "<result><response>" + Common.url_encode(result) + "</response></result>";
            return this.writeResponse(response);
        }
        return false;
    }

    public static String doResetTokenPass(String resetToken, String linkedServer, String password1, Properties user_info) {
        String responseText = "";
        Properties resetTokens = ServerStatus.siPG("resetTokens");
        if (resetTokens == null) {
            resetTokens = new Properties();
        }
        ServerStatus.thisObj.server_info.put("resetTokens", resetTokens);
        if (resetTokens.containsKey(resetToken)) {
            Properties reset = (Properties)resetTokens.get(resetToken);
            Properties password_rules = SessionCrush.build_password_rules(reset);
            if (reset.getProperty("site", "").indexOf("(SITE_PASS)") < 0) {
                responseText = "ERROR: Your username does not allow password changes.";
            } else {
                Common cfr_ignored_0 = ServerStatus.thisObj.common_code;
                if (!Common.checkPasswordRequirements(Common.url_decode(password1), reset.getProperty("password_history", ""), password_rules).equals("")) {
                    Common cfr_ignored_1 = ServerStatus.thisObj.common_code;
                    responseText = "ERROR: " + Common.checkPasswordRequirements(Common.url_decode(password1), reset.getProperty("password_history", ""), password_rules);
                } else {
                    resetTokens.remove(resetToken);
                    if (user_info.get("session") != null && user_info.get("session") instanceof SessionCrush) {
                        String salt = UserTools.ut.getUser(linkedServer, reset.getProperty("username"), false).getProperty("salt", "");
                        SessionCrush session = (SessionCrush)user_info.get("session");
                        if (session.user != null) {
                            salt = session.user.getProperty("salt", "");
                        }
                        if (salt.equals("random")) {
                            salt = Common.makeBoundary(8);
                            UserTools.ut.put_in_user(linkedServer, reset.getProperty("username"), "salt", salt, true, true);
                        }
                    }
                    UserTools.ut.put_in_user(linkedServer, reset.getProperty("username"), "password", ServerStatus.thisObj.common_code.encode_pass(Common.url_decode(password1), ServerStatus.SG("password_encryption"), UserTools.ut.getUser(linkedServer, reset.getProperty("username"), false).getProperty("salt", "")), true, true);
                    UserTools.ut.put_in_user(linkedServer, reset.getProperty("username"), "password_history", Common.getPasswordHistory(Common.url_decode(password1), reset.getProperty("password_history", ""), password_rules), true, true);
                    if (!reset.getProperty("expire_password_days", "0").equals("0") && !reset.getProperty("expire_password_days", "0").equals("")) {
                        GregorianCalendar gc = new GregorianCalendar();
                        gc.setTime(new Date());
                        ((Calendar)gc).add(5, Integer.parseInt(reset.getProperty("expire_password_days", "0")));
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.US);
                        String s = sdf.format(gc.getTime());
                        UserTools.ut.put_in_user(linkedServer, reset.getProperty("username"), "expire_password_when", s, true, true);
                    }
                    UserTools.ut.put_in_user(linkedServer, reset.getProperty("username"), "password_expire_advance_days_sent2", "false", true, true);
                    UserTools.ut.force_put_in_user_flush();
                    responseText = "Password changed.  Please login using your new password.";
                    if (user_info.containsKey("user_name")) {
                        user_info.put("user_name", reset.getProperty("username"));
                    }
                    SessionCrush tempSession = new SessionCrush(null, 1, "127.0.0.1", 0, "0.0.0.0", "MainUsers", new Properties());
                    tempSession.user = reset;
                    tempSession.user_info = user_info;
                    ServerStatus.thisObj.runAlerts("password_change", null, user_info, tempSession);
                    Common.send_change_pass_email(tempSession);
                }
            }
        } else {
            responseText = "ERROR: The link is invalid or expired.";
        }
        return responseText;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String doResetToken(String hostString, String reset_username_email, String currentURL, String linkedServer, String token, boolean sendEmail, String lang) throws IOException {
        Properties temp_user;
        long minute = System.currentTimeMillis() - 60000L;
        boolean ok = false;
        Vector vector = reset_requests;
        synchronized (vector) {
            reset_requests.addElement(String.valueOf(System.currentTimeMillis()));
            int x = reset_requests.size() - 1;
            while (x >= 0) {
                if (Long.parseLong("" + reset_requests.elementAt(x)) < minute) {
                    reset_requests.remove(x);
                }
                --x;
            }
            if (reset_requests.size() < ServerStatus.IG("max_password_resets_per_minute")) {
                ok = true;
            }
        }
        if (!ok) {
            Log.log("SERVER", 0, "Too many reset requests per minute, blocking attempt.  Last minute has " + reset_requests.size() + " requests.");
            return LOC.G("Server is busy, try again later.");
        }
        Vector matchingUsernames = new Vector();
        reset_username_email = Common.url_decode(reset_username_email);
        if (ServerStatus.BG("secondary_login_via_email") && reset_username_email.indexOf("@") >= 0 && UserTools.user_email_cache.containsKey(String.valueOf(linkedServer) + ":" + reset_username_email.toUpperCase())) {
            String username2 = UserTools.user_email_cache.getProperty(String.valueOf(linkedServer) + ":" + reset_username_email.toUpperCase());
            Log.log("SERVER", 0, "Using XF table for email " + reset_username_email + " to convert to:" + username2);
            reset_username_email = username2;
            temp_user = UserTools.ut.getUser(linkedServer, reset_username_email, false);
            if (temp_user != null) {
                matchingUsernames.addElement(temp_user);
            }
        }
        String responseText = "";
        if (matchingUsernames.size() == 0) {
            if (reset_username_email.indexOf("@") >= 0) {
                matchingUsernames = UserTools.findUserEmail(linkedServer, reset_username_email);
            } else {
                temp_user = UserTools.ut.getUser(linkedServer, reset_username_email, true);
                if (temp_user != null) {
                    matchingUsernames.addElement(temp_user);
                }
            }
        }
        int x = matchingUsernames.size() - 1;
        while (x >= 0) {
            Properties user = (Properties)matchingUsernames.elementAt(x);
            if (user.getProperty("email", "").equals("") || !user.getProperty("username").equalsIgnoreCase(reset_username_email) && !user.getProperty("email", "").equalsIgnoreCase(reset_username_email)) {
                matchingUsernames.removeElementAt(x);
            } else {
                matchingUsernames.setElementAt(user, x);
            }
            --x;
        }
        if (matchingUsernames.size() == 1) {
            Properties user = (Properties)matchingUsernames.elementAt(0);
            Properties resetTokens = ServerStatus.siPG("resetTokens");
            if (resetTokens == null) {
                resetTokens = new Properties();
            }
            ServerStatus.thisObj.server_info.put("resetTokens", resetTokens);
            Properties reset = UserTools.ut.getUser(linkedServer, user.getProperty("username"), true);
            reset.put("generated", String.valueOf(System.currentTimeMillis()));
            resetTokens.put(token, reset);
            if (sendEmail) {
                try {
                    Common.sendResetPasswordTokenEmail(reset, lang, hostString, token);
                }
                catch (Exception e) {
                    Log.log("SERVER", 0, e);
                }
            }
            responseText = ServerStatus.SG("password_reset_message_browser");
            File f = new File(String.valueOf(System.getProperty("crushftp.web")) + "localizations/password_reset_message_browser_" + lang + ".html");
            if (f.exists() && f.length() < 0x100000L) {
                responseText = Common.getFileText(f.getPath());
            }
        } else {
            responseText = ServerStatus.SG("password_reset_message_browser_bad");
            Log.log("SERVER", 0, "Unable to locate the user...found matching usernames:" + matchingUsernames);
        }
        return responseText;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String doEmailPass(Properties p, SessionCrush thisSession, String req_id) {
        String lookupUsername = p.getProperty("username");
        Properties lookupUser = null;
        if (com.crushftp.client.Common.dmz_mode) {
            Vector queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
            Properties action = new Properties();
            action.put("type", "GET:USER");
            action.put("id", Common.makeBoundary());
            action.put("username", lookupUsername);
            action.put("need_response", "true");
            queue.addElement(action);
            action = UserTools.waitResponse(action, 60);
            if (action != null) {
                lookupUser = (Properties)action.get("user");
            }
        } else {
            lookupUser = DMZServerCommon.doGetUser(thisSession.server_item, lookupUsername, "", thisSession.uiSG("user_ip"), new Properties());
        }
        thisSession.user = null;
        String result = "";
        String standardError = LOC.G("An email has been sent if the user was found.  If no email is received, then the username / email didn't exist or you are not allowed to have your password emailed to you.");
        long minute = System.currentTimeMillis() - 60000L;
        boolean ok = false;
        Vector vector = reset_requests;
        synchronized (vector) {
            reset_requests.addElement(String.valueOf(System.currentTimeMillis()));
            int x = reset_requests.size() - 1;
            while (x >= 0) {
                if (Long.parseLong("" + reset_requests.elementAt(x)) < minute) {
                    reset_requests.remove(x);
                }
                --x;
            }
            if (reset_requests.size() < ServerStatus.IG("max_password_resets_per_minute")) {
                ok = true;
            }
        }
        if (!ok) {
            Log.log("SERVER", 0, "Too many reset requests per minute, blocking attempt.  Last minute has " + reset_requests.size() + " requests.");
            result = LOC.G("Server is busy, try again later.");
        } else if (ServerStatus.SG("smtp_server").equals("")) {
            result = LOC.G("This server is not configured to send email password reminders.");
        } else if (lookupUser == null) {
            lookupUser = new Properties();
            result = standardError;
        } else if (lookupUser != null && lookupUser.containsKey("account_path") && lookupUser.containsKey("publishType")) {
            lookupUser = new Properties();
            result = standardError;
        } else if (lookupUser.getProperty("site", "").toUpperCase().indexOf("(SITE_EMAILPASSWORD)") >= 0) {
            String pass = lookupUser.getProperty("password", "");
            pass = pass.startsWith("SHA:") || pass.startsWith("SHA512:") || pass.startsWith("SHA256:") || pass.startsWith("SHA3:") || pass.startsWith("MD5:") || pass.startsWith("CRYPT3:") || pass.startsWith("ARGOND:") ? LOC.G("(Your password is encrypted and cannot be revealed.  Please contact your server administrator to have it reset.)") : new Common().decode_pass(pass);
            lookupUser.put("user_name", lookupUser.getProperty("username"));
            String to = lookupUser.getProperty("email", "");
            String from = ServerStatus.SG("smtp_from");
            if (from.equals("")) {
                from = to;
            }
            if (!to.equals("")) {
                if (ServerStatus.BG("expire_emailed_passwords")) {
                    Properties tempUser = UserTools.ut.getUser(thisSession.server_item.getProperty("linkedServer"), lookupUsername, false);
                    tempUser.put("expire_password_when", "01/01/1978 12:00:00 AM");
                    if (!com.crushftp.client.Common.dmz_mode) {
                        UserTools.writeUser(thisSession.server_item.getProperty("linkedServer"), lookupUsername, tempUser);
                    }
                }
                String subject = ServerStatus.SG("emailReminderSubjectText");
                String body = ServerStatus.SG("emailReminderBodyText");
                body = Common.replace_str(body, "%user_pass%", pass);
                body = Common.replace_str(body, "{user_pass}", pass);
                Properties user_info2 = (Properties)thisSession.user_info.clone();
                user_info2.putAll((Map<?, ?>)lookupUser);
                subject = ServerStatus.thisObj.change_vars_to_values(subject, lookupUser, user_info2, null);
                body = ServerStatus.thisObj.change_vars_to_values(body, lookupUser, user_info2, null);
                result = com.crushftp.client.Common.send_mail(ServerStatus.SG("discovered_ip"), to, "", "", from, subject, body, ServerStatus.SG("smtp_server"), ServerStatus.SG("smtp_user"), ServerStatus.SG("smtp_pass"), ServerStatus.SG("smtp_ssl").equals("true"), ServerStatus.SG("smtp_html").equals("true"), null);
                thisSession.add_log_formatted(String.valueOf(LOC.G("Password Emailed to user:")) + lookupUsername + "  " + to + "   " + LOC.G("Email Result:") + result, "POST", req_id);
                result = result.toUpperCase().indexOf("ERROR") >= 0 ? LOC.G("An error occured when generating the email.") : standardError;
            } else {
                result = standardError;
            }
        } else {
            result = standardError;
        }
        return result;
    }

    public boolean getUserName(Properties request) throws Exception {
        if (request.getProperty("command", "").equalsIgnoreCase("getUserName")) {
            String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
            if (ServerStatus.BG("csrf") && !request.getProperty("c2f", "").equals("")) {
                String session_id = this.thisSessionHTTP.thisSession.getId();
                try {
                    if (!request.getProperty("c2f", "").equalsIgnoreCase(session_id.substring(session_id.length() - 4))) {
                        this.thisSessionHTTP.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                        response = String.valueOf(response) + "<commandResult><response>FAILURE:Access Denied. (c2f)</response></commandResult>";
                        return this.writeResponse(response);
                    }
                }
                catch (Exception e) {
                    Log.log("HTTP_SERVER", 2, e);
                    this.thisSessionHTTP.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                    response = String.valueOf(response) + "<loginResult><response>failure</response></loginResult>";
                    return this.writeResponse(response);
                }
            }
            response = this.thisSessionHTTP.thisSession.uiBG("user_logged_in") && !this.thisSessionHTTP.thisSession.uiSG("user_name").equals("") ? String.valueOf(response) + "<loginResult><response>success</response><username>" + this.thisSessionHTTP.thisSession.uiSG("user_name") + "</username></loginResult>" : String.valueOf(response) + "<loginResult><response>failure</response></loginResult>";
            return this.writeResponse(response);
        }
        return false;
    }

    /*
     * Opcode count of 22600 triggered aggressive code reduction.  Override with --aggressivesizethreshold.
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    public boolean processItems(Properties request, Vector byteRanges, String req_id) throws Exception {
        block903: {
            block918: {
                block916: {
                    block917: {
                        block914: {
                            block915: {
                                block911: {
                                    block889: {
                                        block912: {
                                            block908: {
                                                block909: {
                                                    block910: {
                                                        block906: {
                                                            block907: {
                                                                block904: {
                                                                    block905: {
                                                                        block901: {
                                                                            block902: {
                                                                                site = this.thisSessionHTTP.thisSession.SG("site");
                                                                                command = request.getProperty("command", "");
                                                                                if (site.indexOf("(CONNECT)") < 0) {
                                                                                    request.put("serverGroup_original", request.getProperty("serverGroup", ""));
                                                                                    linkedServer = this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer");
                                                                                    if (linkedServer.equals("@AutoDomain")) {
                                                                                        linkedServer = this.thisSessionHTTP.thisSession.uiSG("user_name").substring(this.thisSessionHTTP.thisSession.uiSG("user_name").lastIndexOf("@") + 1);
                                                                                    }
                                                                                    request.put("serverGroup", linkedServer);
                                                                                    request.put("serverGroup_backup", request.getProperty("serverGroup", ""));
                                                                                    groupName = this.thisSessionHTTP.thisSession.getAdminGroupName(request);
                                                                                    this.thisSessionHTTP.thisSession.put("admin_group_name", groupName);
                                                                                    if (site.indexOf("(USER_ADMIN)") >= 0 && groupName.indexOf(",") >= 0) {
                                                                                        request.put("admin_group_name", groupName.substring(1, groupName.indexOf(",", 1)));
                                                                                    } else if (site.indexOf("(USER_ADMIN)") >= 0) {
                                                                                        request.put("admin_group_name", groupName);
                                                                                    }
                                                                                }
                                                                                session_id = this.thisSessionHTTP.thisSession.getId();
                                                                                if (ServerStatus.BG("csrf") && !request.getProperty("command", "").equals("")) {
                                                                                    if (request.getProperty("command", "").equals("getUserInfo") && request.getProperty("c2f", "").equals("false")) {
                                                                                        this.thisSessionHTTP.writeCookieAuth = true;
                                                                                    }
                                                                                    if (!request.getProperty("command", "").equals("upload_debug_info") && ("," + ServerStatus.SG("whitelist_web_commands") + ",").indexOf("," + request.getProperty("command") + ",") < 0 && this.thisSessionHTTP.thisSession.user_info.getProperty("authorization_header", "false").equals("false") && !request.getProperty("c2f", "").equalsIgnoreCase(session_id.substring(session_id.length() - 4))) {
                                                                                        this.thisSessionHTTP.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                                                                                        return this.writeResponse("<commandResult><response>FAILURE:Access Denied. (c2f)</response></commandResult>");
                                                                                    }
                                                                                }
                                                                                if (command.equalsIgnoreCase("getServerItem")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.getServerItem(this.thisSessionHTTP.thisSession.SG("admin_group_name"), request, site, this.thisSessionHTTP.thisSession.user), "result_value", "OK"), false, 200, false, false, false);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("getDashboardItems")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.getDashboardItems(request, site), "result_value", "OK"), false, 200, false, false, false);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("getDashboardHistory")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.getDashboardHistory(request, site), "result_value", "OK"), false, 200, false, false, false);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("getServerInfoItems")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.getServerInfoItems(request, site), "result_value", "OK"), false, 200, false, false, false);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("getServerSettingItems")) {
                                                                                    request.put("admin_group_name", this.thisSessionHTTP.thisSession.SG("admin_group_name"));
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.getServerSettingItems(request, site), "result_value", "OK"), false, 200, false, false, false);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("getStatHistory")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.getStatHistory(request), false, 200, true, false, false);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("getDataFlowItems")) {
                                                                                    if (ServerStatus.BG("v10_beta") && AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.getDataFlowItems(request, site), "result_value", "OK"), false, 200, false, false, false);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("getJobsSummary")) {
                                                                                    request.put("calling_user", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        v = AdminControls.getJobsSummary(request, site);
                                                                                        if (!request.getProperty("scheduleName", "").equals("")) {
                                                                                            vv = new Vector<E>();
                                                                                            x = 0;
                                                                                            while (x < v.size()) {
                                                                                                settings = (Properties)((Properties)v.get(x)).get("settings");
                                                                                                if (settings.getProperty("scheduleName", "").equals(request.getProperty("scheduleName", ""))) {
                                                                                                    vv.add(v.get(x));
                                                                                                }
                                                                                                ++x;
                                                                                            }
                                                                                            v = vv;
                                                                                        }
                                                                                        return this.writeResponse(AdminControls.buildXML(v, "running_tasks", "OK"), false, 200, false, false, false);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("getJobsSummaryDashboard")) {
                                                                                    request.put("calling_user", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        v = new Vector<E>();
                                                                                        if (ServerStatus.BG("job_summary_on_dashboard")) {
                                                                                            v = AdminControls.getJobsSummary(request, site);
                                                                                            if (!request.getProperty("scheduleName", "").equals("")) {
                                                                                                vv = new Vector<E>();
                                                                                                x = 0;
                                                                                                while (x < v.size()) {
                                                                                                    settings = (Properties)((Properties)v.get(x)).get("settings");
                                                                                                    if (settings.getProperty("scheduleName", "").equals(request.getProperty("scheduleName", ""))) {
                                                                                                        vv.add(v.get(x));
                                                                                                    }
                                                                                                    ++x;
                                                                                                }
                                                                                                v = vv;
                                                                                            }
                                                                                        }
                                                                                        return this.writeResponse(AdminControls.buildXML(v, "running_tasks", "OK"), false, 200, false, false, false);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("getJobsSettings")) {
                                                                                    request.put("calling_user", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        p = AdminControls.getJobsSettings(request, site);
                                                                                        return this.writeResponse(AdminControls.buildXML(p, "job_settings", "OK"), false, 200, false, false, false);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("getJobInfo")) {
                                                                                    Common.urlDecodePost(request);
                                                                                    request.put("calling_user", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.getJobInfo(request, site), "running_tasks", "OK"), false, 200, false, false, false);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("getSessionList")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.getSessionList(request), "session_list", "OK"), false, 200, true, false, false);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("getLog")) {
                                                                                    Common.urlDecodePost(request);
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.getLog(request, site), "log_data", "OK"), false, 200, false, false, false);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("getLogSnippet")) {
                                                                                    Common.urlDecodePost(request);
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        try {
                                                                                            return this.writeResponse(AdminControls.buildXML(AdminControls.getLogSnippet(request, site), "log_data", "OK"), false, 200, false, false, false);
                                                                                        }
                                                                                        catch (Exception e) {
                                                                                            return this.writeResponse("<commandResult><response>ERROR:" + e + " </response></commandResult>");
                                                                                        }
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("getJob")) {
                                                                                    Common.urlDecodePost(request);
                                                                                    request.put("calling_user", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                    if (site.indexOf("(USER_ADMIN)") < 0 && !this.thisSessionHTTP.thisSession.user.getProperty("admin_group_name", "").equals("")) {
                                                                                        site = String.valueOf(site) + "(USER_ADMIN)";
                                                                                        request.put("admin_group_name", this.thisSessionHTTP.thisSession.user.getProperty("admin_group_name", ""));
                                                                                    }
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.getJob(request, site), "result_value", "OK"), false, 200, false, false, true);
                                                                                    }
                                                                                    if (site.indexOf("(JOB_LIST)") < 0) {
                                                                                        return this.writeResponse(AdminControls.buildXML(new Vector<E>(), "result_value", "OK"), false, 200, false, false, true);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                last_activity_time = this.thisSessionHTTP.thisSession.getProperty("last_activity");
                                                                                this.thisSessionHTTP.thisSession.active();
                                                                                if (command.equalsIgnoreCase("logout")) {
                                                                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                                                                    response = String.valueOf(response) + "<commandResult><response>Logged out.</response></commandResult>";
                                                                                    this.thisSessionHTTP.logout_all();
                                                                                    this.thisSessionHTTP.deleteCookieAuth = true;
                                                                                    this.writeResponse(response);
                                                                                    return true;
                                                                                }
                                                                                if (command.equalsIgnoreCase("getCrushAuth")) {
                                                                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                                                                    response = String.valueOf(response) + "<auth>CrushAuth=" + this.thisSessionHTTP.thisSession.getId() + "</auth>";
                                                                                    return this.writeResponse(response);
                                                                                }
                                                                                if (command.equalsIgnoreCase("renameJob")) {
                                                                                    Common.urlDecodePost(request);
                                                                                    if (site.indexOf("(USER_ADMIN)") < 0 && !this.thisSessionHTTP.thisSession.user.getProperty("admin_group_name", "").equals("")) {
                                                                                        site = String.valueOf(site) + "(USER_ADMIN)";
                                                                                    }
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.renameJob(request, site, true), "result_value", "OK"), false, 200, false, false, true);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response_status>FAILURE:Access Denied.</response_status></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("removeJob")) {
                                                                                    Common.urlDecodePost(request);
                                                                                    if (site.indexOf("(USER_ADMIN)") < 0 && !this.thisSessionHTTP.thisSession.user.getProperty("admin_group_name", "").equals("")) {
                                                                                        site = String.valueOf(site) + "(USER_ADMIN)";
                                                                                    }
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.removeJob(request, site, true), "result_value", "OK"), false, 200, false, false, true);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response_status>FAILURE:Access Denied.</response_status></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("changeJobStatus")) {
                                                                                    Common.urlDecodePost(request);
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.changeJobStatus(request, site), "result_value", "OK"), false, 200, false, false, true);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response_status>FAILURE:Access Denied.</response_status></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("addJob")) {
                                                                                    request.put("calling_user", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                    request.put("calling_linkedServer", this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer"));
                                                                                    if (site.indexOf("(USER_ADMIN)") < 0 && !this.thisSessionHTTP.thisSession.user.getProperty("admin_group_name", "").equals("")) {
                                                                                        site = String.valueOf(site) + "(USER_ADMIN)";
                                                                                    }
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.addJob(request, site, true), "result_value", "OK"), false, 200, false, false, true);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response_status>FAILURE:Access Denied.</response_status></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("addToJobs")) {
                                                                                    request.put("calling_user", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                    request.put("calling_linkedServer", this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer"));
                                                                                    if (site.indexOf("(USER_ADMIN)") < 0 && !this.thisSessionHTTP.thisSession.user.getProperty("admin_group_name", "").equals("")) {
                                                                                        site = String.valueOf(site) + "(USER_ADMIN)";
                                                                                    }
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.addToJobs(request, site, true), "result_value", "OK"), false, 200, false, false, true);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response_status>FAILURE:Access Denied.</response_status></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("makedirJob")) {
                                                                                    request.put("calling_user", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                    request.put("calling_linkedServer", this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer"));
                                                                                    if (site.indexOf("(USER_ADMIN)") < 0 && !this.thisSessionHTTP.thisSession.user.getProperty("admin_group_name", "").equals("")) {
                                                                                        site = String.valueOf(site) + "(USER_ADMIN)";
                                                                                    }
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.makedirJob(request, site, true), "result_value", "OK"), false, 200, false, false, true);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response_status>FAILURE:Access Denied.</response_status></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("renamedirJob")) {
                                                                                    request.put("calling_user", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                    request.put("calling_linkedServer", this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer"));
                                                                                    if (site.indexOf("(USER_ADMIN)") < 0 && !this.thisSessionHTTP.thisSession.user.getProperty("admin_group_name", "").equals("")) {
                                                                                        site = String.valueOf(site) + "(USER_ADMIN)";
                                                                                    }
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.renamedirJob(request, site, true), "result_value", "OK"), false, 200, false, false, true);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response_status>FAILURE:Access Denied.</response_status></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("deletedirJob")) {
                                                                                    request.put("calling_user", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                    request.put("calling_linkedServer", this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer"));
                                                                                    if (site.indexOf("(USER_ADMIN)") < 0 && !this.thisSessionHTTP.thisSession.user.getProperty("admin_group_name", "").equals("")) {
                                                                                        site = String.valueOf(site) + "(USER_ADMIN)";
                                                                                    }
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.deletedirJob(request, site, true), "result_value", "OK"), false, 200, false, false, true);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response_status>FAILURE:Access Denied.</response_status></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("startTunnel2")) {
                                                                                    response = "";
                                                                                    SharedSession.find("crushftp.usernames").put(String.valueOf(Common.getPartialIp("127.0.0.1")) + "_" + this.thisSessionHTTP.thisSession.getId() + "_user", SharedSession.find("crushftp.usernames").getProperty(String.valueOf(Common.getPartialIp(this.thisSessionHTTP.thisSession.uiSG("user_ip"))) + "_" + this.thisSessionHTTP.thisSession.getId() + "_user"), false);
                                                                                    SharedSession.find("crushftp.usernames").put(String.valueOf(Common.getPartialIp("127.0.0.1")) + "_" + this.thisSessionHTTP.thisSession.getId() + "_ip", this.thisSessionHTTP.thisSession.uiSG("user_ip"), false);
                                                                                    userTunnels = String.valueOf(this.thisSessionHTTP.thisSession.user.getProperty("tunnels", "").trim()) + ",";
                                                                                    tunnels = (Vector)ServerStatus.VG("tunnels").clone();
                                                                                    tunnels.addAll(ServerStatus.VG("tunnels_dmz"));
                                                                                    tunnel = null;
                                                                                    x = 0;
                                                                                    while (x < tunnels.size()) {
                                                                                        pp = (Properties)tunnels.elementAt(x);
                                                                                        if (userTunnels.indexOf(String.valueOf(pp.getProperty("id")) + ",") >= 0 && pp.getProperty("id").equals(request.getProperty("tunnelId"))) {
                                                                                            tunnel = (Properties)pp.clone();
                                                                                            break;
                                                                                        }
                                                                                        ++x;
                                                                                    }
                                                                                    if (tunnel != null) {
                                                                                        if (ServerStatus.siIG("enterprise_level") <= 0) {
                                                                                            tunnel.put("channelsOutMax", "1");
                                                                                            tunnel.put("channelsInMax", "1");
                                                                                            tunnel.put("channelRampUp", "1");
                                                                                            tunnel.put("stableSeconds", "1");
                                                                                        }
                                                                                        if (Tunnel2.getTunnel(this.thisSessionHTTP.thisSession.getId()) == null) {
                                                                                            t = new Tunnel2(this.thisSessionHTTP.thisSession.getId(), tunnel);
                                                                                            if (tunnel.getProperty("reverse", "false").equals("true")) {
                                                                                                t.setAllowReverseMode(true);
                                                                                                t.startThreads();
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                    return this.writeResponse(response);
                                                                                }
                                                                                if (command.equalsIgnoreCase("stopTunnel2")) {
                                                                                    response = Tunnel2.stopTunnel(this.thisSessionHTTP.thisSession.getId());
                                                                                    return this.writeResponse(response);
                                                                                }
                                                                                if (command.equalsIgnoreCase("startTunnel3")) {
                                                                                    response = "";
                                                                                    SharedSession.find("crushftp.usernames").put(String.valueOf(Common.getPartialIp("127.0.0.1")) + "_" + this.thisSessionHTTP.thisSession.getId() + "_user", SharedSession.find("crushftp.usernames").getProperty(String.valueOf(Common.getPartialIp(this.thisSessionHTTP.thisSession.uiSG("user_ip"))) + "_" + this.thisSessionHTTP.thisSession.getId() + "_user"), false);
                                                                                    SharedSession.find("crushftp.usernames").put(String.valueOf(Common.getPartialIp("127.0.0.1")) + "_" + this.thisSessionHTTP.thisSession.getId() + "_ip", this.thisSessionHTTP.thisSession.uiSG("user_ip"), false);
                                                                                    SharedSession.find("crushftp.usernames").put(String.valueOf(Common.getPartialIp("127.0.0.1")) + "_" + request.getProperty("clientid") + "_ip", this.thisSessionHTTP.thisSession.uiSG("user_ip"), false);
                                                                                    userTunnels = String.valueOf(this.thisSessionHTTP.thisSession.user.getProperty("tunnels", "").trim()) + ",";
                                                                                    tunnels = (Vector)ServerStatus.VG("tunnels").clone();
                                                                                    tunnels.addAll(ServerStatus.VG("tunnels_dmz"));
                                                                                    tunnel = null;
                                                                                    x = 0;
                                                                                    while (x < tunnels.size()) {
                                                                                        pp = (Properties)tunnels.elementAt(x);
                                                                                        if (userTunnels.indexOf(String.valueOf(pp.getProperty("id")) + ",") >= 0 && pp.getProperty("id").equals(request.getProperty("tunnelId"))) {
                                                                                            tunnel = (Properties)pp.clone();
                                                                                            break;
                                                                                        }
                                                                                        ++x;
                                                                                    }
                                                                                    if (tunnel != null) {
                                                                                        if (tunnel.getProperty("configurable", "false").equals("true")) {
                                                                                            tunnel.put("bindIp", request.getProperty("bindIp"));
                                                                                            tunnel.put("localPort", request.getProperty("localPort"));
                                                                                            tunnel.put("destIp", request.getProperty("destIp"));
                                                                                            tunnel.put("destPort", request.getProperty("destPort"));
                                                                                            tunnel.put("channelsOutMax", request.getProperty("channelsOutMax"));
                                                                                            tunnel.put("channelsInMax", request.getProperty("channelsInMax"));
                                                                                            tunnel.put("reverse", request.getProperty("reverse"));
                                                                                        }
                                                                                        if (ServerStatus.siIG("enterprise_level") <= 0) {
                                                                                            tunnel.put("channelsOutMax", "1");
                                                                                            tunnel.put("channelsInMax", "1");
                                                                                            tunnel.put("channelRampUp", "1");
                                                                                            tunnel.put("stableSeconds", "1");
                                                                                        }
                                                                                        if (ServerSessionTunnel3.getStreamController(this.thisSessionHTTP.thisSession.getId(), tunnel.getProperty("id")) == null) {
                                                                                            sc = new StreamController(tunnel);
                                                                                            ServerSessionTunnel3.running_tunnels.put(String.valueOf(this.thisSessionHTTP.thisSession.getId()) + "_" + tunnel.getProperty("id"), sc);
                                                                                            sc.startServerTunnel();
                                                                                            if (tunnel.getProperty("reverse", "false").equals("true")) {
                                                                                                sc.startReverseThreads();
                                                                                            }
                                                                                            response = "Started";
                                                                                        } else {
                                                                                            response = "Already started.";
                                                                                        }
                                                                                    }
                                                                                    return this.writeResponse(response);
                                                                                }
                                                                                if (command.equalsIgnoreCase("stopTunnel3")) {
                                                                                    response = ServerSessionTunnel3.stopTunnel(this.thisSessionHTTP.thisSession.getId(), request.getProperty("tunnelId"));
                                                                                    return this.writeResponse(response);
                                                                                }
                                                                                if (command.equalsIgnoreCase("getSyncTableData") && ServerStatus.siIG("enterprise_level") > 0) {
                                                                                    try {
                                                                                        item2 = this.thisSessionHTTP.thisSession.uVFS.get_item(String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + request.getProperty("path"));
                                                                                        privs = item2.getProperty("privs");
                                                                                        syncID = Common.parseSyncPart(privs, "name");
                                                                                        if (syncID != null) {
                                                                                            baos = new ByteArrayOutputStream();
                                                                                            oos = new ObjectOutputStream(baos);
                                                                                            uploadOnly = Common.parseSyncPart(privs, "uploadOnly");
                                                                                            if (ServerStatus.BG("syncs_debug")) {
                                                                                                Log.log("SYNC", 0, "Sync UploadOnly:" + uploadOnly);
                                                                                            }
                                                                                            if (uploadOnly.equalsIgnoreCase("true")) {
                                                                                                oos.writeObject(new Vector<E>());
                                                                                            } else {
                                                                                                o = null;
                                                                                                if (com.crushftp.client.Common.dmz_mode) {
                                                                                                    queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                                                                                    action = new Properties();
                                                                                                    action.put("type", "GET:SYNC");
                                                                                                    action.put("id", Common.makeBoundary());
                                                                                                    action.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                                    action.put("request", request);
                                                                                                    action.put("syncID", syncID.toUpperCase());
                                                                                                    action.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
                                                                                                    action.put("need_response", "true");
                                                                                                    queue.addElement(action);
                                                                                                    action = UserTools.waitResponse(action, 300);
                                                                                                    o = action.remove("object_response");
                                                                                                } else {
                                                                                                    vfs_path = request.getProperty("path", "");
                                                                                                    if (vfs_path.equals("")) {
                                                                                                        vfs_path = "/";
                                                                                                    }
                                                                                                    if (!vfs_path.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                                                                                        vfs_path = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + vfs_path.substring(1);
                                                                                                    }
                                                                                                    o = Common.getSyncTableData(syncID.toUpperCase(), Long.parseLong(request.getProperty("lastRID")), request.getProperty("table"), this.thisSessionHTTP.thisSession.uiSG("clientid"), vfs_path, this.thisSessionHTTP.thisSession.uVFS, request.getProperty("prior_md5s_item_path", ""));
                                                                                                }
                                                                                                oos.writeObject(o);
                                                                                                if (ServerStatus.BG("syncs_debug")) {
                                                                                                    Log.log("SYNC", 0, "Sync table data result size:" + o.size());
                                                                                                }
                                                                                            }
                                                                                            oos.close();
                                                                                            this.write_command_http("HTTP/1.1 200 OK");
                                                                                            this.write_command_http("Cache-Control: no-store");
                                                                                            this.write_command_http("Content-Type: application/binary");
                                                                                            this.thisSessionHTTP.write_standard_headers();
                                                                                            this.write_command_http("Content-Length: " + baos.size());
                                                                                            this.write_command_http("");
                                                                                            this.thisSessionHTTP.original_os.write(baos.toByteArray());
                                                                                            this.thisSessionHTTP.original_os.flush();
                                                                                            if (ServerStatus.BG("syncs_debug")) {
                                                                                                Log.log("SYNC", 0, "Sync table data byte size:" + baos.size());
                                                                                            }
                                                                                        } else {
                                                                                            error_msg = "Sync was not found for your current folder.";
                                                                                            this.write_command_http("HTTP/1.1 404 OK");
                                                                                            this.write_command_http("Cache-Control: no-store");
                                                                                            this.write_command_http("Content-Type: application/binary");
                                                                                            this.thisSessionHTTP.write_standard_headers();
                                                                                            this.write_command_http("Content-Length: " + error_msg.length() + 2);
                                                                                            this.write_command_http("");
                                                                                            this.write_command_http(error_msg);
                                                                                            if (ServerStatus.BG("syncs_debug")) {
                                                                                                Log.log("SYNC", 0, error_msg);
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                    catch (Exception e) {
                                                                                        if (ServerStatus.BG("syncs_debug")) {
                                                                                            Log.log("SYNC", 0, e);
                                                                                        }
                                                                                        throw e;
                                                                                    }
                                                                                    return true;
                                                                                }
                                                                                if (command.equalsIgnoreCase("syncConflict") && ServerStatus.siIG("enterprise_level") > 0) {
                                                                                    item = this.thisSessionHTTP.thisSession.uVFS.get_item(String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + request.getProperty("path"));
                                                                                    syncID = Common.parseSyncPart(item.getProperty("privs"), "name");
                                                                                    if (syncID != null) {
                                                                                        if (com.crushftp.client.Common.dmz_mode) {
                                                                                            queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                                                                            action = new Properties();
                                                                                            action.put("type", "GET:SYNC");
                                                                                            action.put("id", Common.makeBoundary());
                                                                                            action.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                            action.put("request", request);
                                                                                            action.put("syncID", syncID.toUpperCase());
                                                                                            action.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
                                                                                            action.put("need_response", "true");
                                                                                            queue.addElement(action);
                                                                                            action = UserTools.waitResponse(action, 60);
                                                                                        } else {
                                                                                            SyncTools.addJournalEntry(syncID.toUpperCase(), request.getProperty("item_path"), "CONFLICT", "", "");
                                                                                        }
                                                                                        response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                                                                        response = String.valueOf(response) + "<result><response_status>success</response_status></result>";
                                                                                        return this.writeResponse(response);
                                                                                    }
                                                                                    error_msg = "Sync was not found for your current folder.";
                                                                                    this.write_command_http("HTTP/1.1 404 OK");
                                                                                    this.write_command_http("Cache-Control: no-store");
                                                                                    this.write_command_http("Content-Type: application/binary");
                                                                                    this.thisSessionHTTP.write_standard_headers();
                                                                                    this.write_command_http("Content-Length: " + error_msg.length() + 2);
                                                                                    this.write_command_http("");
                                                                                    this.write_command_http(error_msg);
                                                                                    return true;
                                                                                }
                                                                                if (command.equalsIgnoreCase("purgeSync") && ServerStatus.siIG("enterprise_level") > 0) {
                                                                                    item = this.thisSessionHTTP.thisSession.uVFS.get_item(String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + request.getProperty("path"));
                                                                                    syncID = Common.parseSyncPart(item.getProperty("privs"), "name");
                                                                                    if (syncID != null) {
                                                                                        request.put("syncID", syncID.toUpperCase());
                                                                                        if (com.crushftp.client.Common.dmz_mode) {
                                                                                            queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                                                                            action = new Properties();
                                                                                            action.put("type", "GET:SYNC");
                                                                                            action.put("id", Common.makeBoundary());
                                                                                            action.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                            action.put("request", request);
                                                                                            action.put("syncID", syncID.toUpperCase());
                                                                                            action.put("root_dir", this.thisSessionHTTP.thisSession.SG("root_dir"));
                                                                                            action.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
                                                                                            action.put("need_response", "true");
                                                                                            queue.addElement(action);
                                                                                            action = UserTools.waitResponse(action, 60);
                                                                                        } else {
                                                                                            AdminControls.purgeSync(request, this.thisSessionHTTP.thisSession.uVFS, this.thisSessionHTTP.thisSession.SG("root_dir"));
                                                                                        }
                                                                                        response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                                                                        response = String.valueOf(response) + "<result><response_status>success</response_status></result>";
                                                                                        return this.writeResponse(response);
                                                                                    }
                                                                                    error_msg = "Sync was not found for your current folder.";
                                                                                    this.write_command_http("HTTP/1.1 404 OK");
                                                                                    this.write_command_http("Cache-Control: no-store");
                                                                                    this.write_command_http("Content-Type: application/binary");
                                                                                    this.thisSessionHTTP.write_standard_headers();
                                                                                    this.write_command_http("Content-Length: " + error_msg.length() + 2);
                                                                                    this.write_command_http("");
                                                                                    this.write_command_http(error_msg);
                                                                                    return true;
                                                                                }
                                                                                if (command.equalsIgnoreCase("getCrushSyncPrefs") && ServerStatus.siIG("enterprise_level") > 0) {
                                                                                    v = new Vector();
                                                                                    request.put("user_name", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                    request.put("user_ip", this.thisSessionHTTP.thisSession.uiSG("user_ip"));
                                                                                    request.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
                                                                                    request.put("site", site);
                                                                                    if (Integer.parseInt(Common.replace_str(request.getProperty("version", "0"), ".", "")) >= Integer.parseInt(Common.replace_str(SyncTools.minSyncVersion, ".", ""))) {
                                                                                        v = (Vector)SyncTools.getSyncPrefs(request);
                                                                                    } else {
                                                                                        p = new Properties();
                                                                                        p.put("UPDATE_REQUIRED", "true");
                                                                                        p.put("MIN_VERSION", SyncTools.minSyncVersion);
                                                                                        v.addElement(p);
                                                                                    }
                                                                                    baos = new ByteArrayOutputStream();
                                                                                    oos = new ObjectOutputStream(baos);
                                                                                    oos.writeObject(v);
                                                                                    oos.close();
                                                                                    this.write_command_http("HTTP/1.1 200 OK");
                                                                                    this.write_command_http("Cache-Control: no-store");
                                                                                    this.write_command_http("Content-Type: application/binary");
                                                                                    this.thisSessionHTTP.write_standard_headers();
                                                                                    this.write_command_http("Content-Length: " + baos.size());
                                                                                    this.write_command_http("");
                                                                                    this.thisSessionHTTP.original_os.write(baos.toByteArray());
                                                                                    this.thisSessionHTTP.original_os.flush();
                                                                                    return true;
                                                                                }
                                                                                if (command.equalsIgnoreCase("syncCommandResult") && ServerStatus.siIG("enterprise_level") > 0) {
                                                                                    request.put("user_name", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                    request.put("user_ip", this.thisSessionHTTP.thisSession.uiSG("user_ip"));
                                                                                    request.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
                                                                                    request.put("site", site);
                                                                                    SyncTools.sendSyncResult(request);
                                                                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                                                                    response = String.valueOf(response) + "<result><response_status>success</response_status></result>";
                                                                                    return this.writeResponse(response);
                                                                                }
                                                                                if (command.equalsIgnoreCase("getSyncAgents") && ServerStatus.siIG("enterprise_level") > 0) {
                                                                                    if (last_activity_time != null) {
                                                                                        this.thisSessionHTTP.thisSession.put("last_activity", last_activity_time);
                                                                                    }
                                                                                    Common.urlDecodePost(request);
                                                                                    v = new Vector();
                                                                                    request.put("user_name", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                    request.put("user_ip", this.thisSessionHTTP.thisSession.uiSG("user_ip"));
                                                                                    request.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
                                                                                    request.put("site", site);
                                                                                    v = (Vector)SyncTools.getSyncAgents(request);
                                                                                    return this.writeResponse(AdminControls.buildXML(v, "agents", "Success"));
                                                                                }
                                                                                if (command.equalsIgnoreCase("sendSyncCommand") && ServerStatus.siIG("enterprise_level") > 0) {
                                                                                    if (last_activity_time != null) {
                                                                                        this.thisSessionHTTP.thisSession.put("last_activity", last_activity_time);
                                                                                    }
                                                                                    Common.urlDecodePost(request);
                                                                                    o = new Properties();
                                                                                    request.put("user_name", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                    request.put("user_ip", this.thisSessionHTTP.thisSession.uiSG("user_ip"));
                                                                                    request.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
                                                                                    request.put("site", site);
                                                                                    o = SyncTools.sendSyncCommand(request);
                                                                                    return this.writeResponse(AdminControls.buildXML(o, "response", "Success"));
                                                                                }
                                                                                if (command.equalsIgnoreCase("getQuota")) {
                                                                                    path = Common.url_decode(request.getProperty("path", "").replace('+', ' '));
                                                                                    if (path.startsWith("///") && !path.startsWith("////")) {
                                                                                        path = "/" + path;
                                                                                    }
                                                                                    if (!path.startsWith("/")) {
                                                                                        path = "/" + path;
                                                                                    }
                                                                                    if (!path.endsWith("/")) {
                                                                                        path = String.valueOf(path) + "/";
                                                                                    }
                                                                                    the_dir = com.crushftp.client.Common.dots(path);
                                                                                    if (!the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                                                                        the_dir = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + the_dir.substring(1);
                                                                                    }
                                                                                    quota = "-12345";
                                                                                    if (com.crushftp.client.Common.dmz_mode) {
                                                                                        queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                                                                        action = new Properties();
                                                                                        action.put("type", "GET:QUOTA");
                                                                                        action.put("id", Common.makeBoundary());
                                                                                        action.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                        action.put("password", this.thisSessionHTTP.thisSession.uiSG("current_password"));
                                                                                        action.put("request", request);
                                                                                        the_dir2 = the_dir;
                                                                                        if (the_dir2.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                                                                            the_dir2 = the_dir2.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
                                                                                        }
                                                                                        action.put("the_dir", the_dir2);
                                                                                        action.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
                                                                                        action.put("need_response", "true");
                                                                                        queue.addElement(action);
                                                                                        action = UserTools.waitResponse(action, 300);
                                                                                        quota = action.remove("object_response").toString().trim();
                                                                                    } else {
                                                                                        this.thisSessionHTTP.thisSession;
                                                                                        quota = String.valueOf(this.thisSessionHTTP.thisSession.get_quota(the_dir)) + ":" + SessionCrush.get_quota(the_dir, this.thisSessionHTTP.thisSession.uVFS, this.thisSessionHTTP.thisSession.SG("parent_quota_dir"), this.thisSessionHTTP.thisSession.quotaDelta, this.thisSessionHTTP.thisSession, false);
                                                                                    }
                                                                                    return this.writeResponse(String.valueOf(quota));
                                                                                }
                                                                                if (command.equalsIgnoreCase("getMd5s")) {
                                                                                    path_str = null;
                                                                                    try {
                                                                                        path_str = new String(Base64.decode(request.getProperty("path")));
                                                                                    }
                                                                                    catch (Exception e) {
                                                                                        path_str = com.crushftp.client.Common.dots(Common.url_decode(request.getProperty("path")));
                                                                                    }
                                                                                    if (com.crushftp.client.Common.dmz_mode) {
                                                                                        queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                                                                        action = new Properties();
                                                                                        action.put("type", "PUT:GETMD5S");
                                                                                        action.put("id", Common.makeBoundary());
                                                                                        action.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                        root_item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir"));
                                                                                        c = this.thisSessionHTTP.thisSession.uVFS.getClient(root_item);
                                                                                        action.put("crushAuth", c.getConfig("crushAuth"));
                                                                                        this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                                                                        action.put("user_ip", this.thisSessionHTTP.thisSession.uiSG("user_ip"));
                                                                                        action.put("request", request);
                                                                                        action.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
                                                                                        action.put("need_response", "true");
                                                                                        queue.addElement(action);
                                                                                        action = UserTools.waitResponse(action, 600);
                                                                                        response = action.remove("object_response").toString();
                                                                                        return this.writeResponse(response);
                                                                                    }
                                                                                    item = this.thisSessionHTTP.thisSession.uVFS.get_item(String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + path_str);
                                                                                    md5s = new Vector<E>();
                                                                                    request2 = request;
                                                                                    md5Worker = new multiThreadMd5(item, md5s, request2);
                                                                                    if (item != null) {
                                                                                        Worker.startWorker(md5Worker);
                                                                                    }
                                                                                    this.write_command_http("HTTP/1.1 200 OK");
                                                                                    this.write_command_http("Cache-Control: no-store");
                                                                                    this.write_command_http("Content-Type: text/plain");
                                                                                    this.write_command_http("Transfer-Encoding: chunked");
                                                                                    this.thisSessionHTTP.write_standard_headers();
                                                                                    this.write_command_http("");
                                                                                    while (item != null && md5Worker.isActive() || md5s.size() > 0) {
                                                                                        if (md5s.size() > 0) {
                                                                                            md5 = md5s.remove(0).toString();
                                                                                            this.write_command_http(Long.toHexString(md5.length() + 2));
                                                                                            this.write_command_http(md5);
                                                                                            this.write_command_http("");
                                                                                            continue;
                                                                                        }
                                                                                        Thread.sleep(100L);
                                                                                    }
                                                                                    this.write_command_http("0");
                                                                                    this.write_command_http("");
                                                                                    return true;
                                                                                }
                                                                                if (command.equalsIgnoreCase("messageForm") && !request.containsKey("registration_username") && !request.containsKey("meta_registration_username")) {
                                                                                    response = "";
                                                                                    if (com.crushftp.client.Common.dmz_mode) {
                                                                                        queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                                                                        action = new Properties();
                                                                                        action.put("type", "PUT:MESSAGEFORM");
                                                                                        action.put("id", Common.makeBoundary());
                                                                                        action.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                        root_item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir"));
                                                                                        c = this.thisSessionHTTP.thisSession.uVFS.getClient(root_item);
                                                                                        action.put("crushAuth", c.getConfig("crushAuth"));
                                                                                        this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                                                                        action.put("request", request);
                                                                                        action.put("need_response", "true");
                                                                                        queue.addElement(action);
                                                                                        action = UserTools.waitResponse(action, 30);
                                                                                        response = action.remove("object_response").toString();
                                                                                    } else {
                                                                                        response = ServerSessionAJAX.handle_message_form(request, this.thisSessionHTTP.thisSession);
                                                                                    }
                                                                                    return this.writeResponse(response);
                                                                                }
                                                                                if (command.equalsIgnoreCase("selfRegistration") || request.containsKey("registration_username") || request.containsKey("meta_registration_username")) {
                                                                                    response = "";
                                                                                    if (com.crushftp.client.Common.dmz_mode) {
                                                                                        queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                                                                        action = new Properties();
                                                                                        action.put("type", "PUT:SELFREGISTRATION");
                                                                                        action.put("id", Common.makeBoundary());
                                                                                        action.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                        root_item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir"));
                                                                                        c = this.thisSessionHTTP.thisSession.uVFS.getClient(root_item);
                                                                                        action.put("crushAuth", c.getConfig("crushAuth"));
                                                                                        this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                                                                        action.put("user_ip", this.thisSessionHTTP.thisSession.uiSG("user_ip"));
                                                                                        action.put("request", request);
                                                                                        action.put("req_id", req_id);
                                                                                        action.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
                                                                                        action.put("need_response", "true");
                                                                                        queue.addElement(action);
                                                                                        action = UserTools.waitResponse(action, 30);
                                                                                        response = action.remove("object_response").toString();
                                                                                    } else {
                                                                                        response = ServerSessionAJAX.selfRegistration(request, this.thisSessionHTTP.thisSession, req_id);
                                                                                    }
                                                                                    return this.writeResponse("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n<commandResult><response>" + response + "</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("setMetaInfo")) {
                                                                                    keys = request.keys();
                                                                                    metaInfo = new Properties();
                                                                                    while (keys.hasMoreElements()) {
                                                                                        key = keys.nextElement().toString();
                                                                                        if (!key.toUpperCase().startsWith("META_")) continue;
                                                                                        val = request.getProperty(key);
                                                                                        key = key.substring("META_".length());
                                                                                        metaInfo.put(key, val);
                                                                                        if (key.toUpperCase().startsWith("GLOBAL_")) {
                                                                                            if (ServerStatus.thisObj.server_info.get("global_variables") == null) {
                                                                                                ServerStatus.thisObj.server_info.put("global_variables", new Properties());
                                                                                            }
                                                                                            global_variables = (Properties)ServerStatus.thisObj.server_info.get("global_variables");
                                                                                            global_variables.put(key, val);
                                                                                            continue;
                                                                                        }
                                                                                        if (!key.toUpperCase().startsWith("USER_INFO_")) continue;
                                                                                        this.thisSessionHTTP.thisSession.user_info.put(key, val);
                                                                                    }
                                                                                    this.thisSessionHTTP.thisSession.put("last_metaInfo", metaInfo);
                                                                                    this.thisSessionHTTP.thisSession.add_log("[" + this.thisSessionHTTP.thisSession.uiSG("user_number") + ":" + this.thisSessionHTTP.thisSession.uiSG("user_name") + ":" + this.thisSessionHTTP.thisSession.uiSG("user_ip") + "] DATA: *metaInfo confirmed:" + metaInfo + "*", "HTTP");
                                                                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n<commandResult><response>Success</response></commandResult>";
                                                                                    return this.writeResponse(response);
                                                                                }
                                                                                if (command.equalsIgnoreCase("setServerItem")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        request.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                        return this.writeResponse(AdminControls.buildXML(null, "response", AdminControls.setServerItem(request, site)));
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("setReportSchedules")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(null, "response", AdminControls.setReportSchedules(request, site)));
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("deleteReportSchedules")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(null, "response", AdminControls.deleteReportSchedules(request, site)));
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("getUser")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.getUser(request, site, this.thisSessionHTTP.thisSession), "user_items", "OK"), true, 200, false, false, true);
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("getUserVersions")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        if (site.indexOf("(USER_ADMIN)") > 0 && site.indexOf("(CONNECT)") < 0 && site.indexOf("(USER_VIEW)") < 0 && site.indexOf("(USER_EDIT)") < 0) {
                                                                                            info = null;
                                                                                            if (!this.thisSessionHTTP.thisSession.containsKey("user_admin_info")) {
                                                                                                AdminControls.getUserList(request, site, this.thisSessionHTTP.thisSession);
                                                                                            }
                                                                                            info = (Properties)this.thisSessionHTTP.thisSession.get("user_admin_info");
                                                                                            list = (Vector)info.get("list");
                                                                                            if (!list.contains(request.getProperty("username", ""))) {
                                                                                                return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                            }
                                                                                            allowed_keys = Common.convertToVector(this.thisSessionHTTP.SG("allowed_config").split(","));
                                                                                            if (!this.thisSessionHTTP.thisSession.user.getProperty("allowed_config", "").equals("") && !allowed_keys.contains("rollback_user")) {
                                                                                                return this.writeResponse("<commandResult><response>FAILURE:Limited Admin.  Access Denied..</response></commandResult>");
                                                                                            }
                                                                                        }
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.getUserVersions(request), "user_versions", "OK"));
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("getDeletedUsers")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.getDeletedUsers(request), "deleted_users", "OK"));
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("setUserItem") && AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                    data_item = AdminControls.buildXML(null, "response", AdminControls.setUserItem(request, this.thisSessionHTTP.thisSession, site));
                                                                                    if (data_item.indexOf("<password>") >= 0 && data_item.indexOf("</password>") >= 0) {
                                                                                        data_item = String.valueOf(data_item.substring(0, data_item.indexOf("<password>") + "<password>".length())) + "*******" + data_item.substring(data_item.indexOf("</password>"));
                                                                                    } else if (data_item.indexOf("current_password") >= 0) {
                                                                                        data_item = String.valueOf(data_item.substring(0, data_item.indexOf(":") + 1)) + "*******";
                                                                                    } else if (data_item.toUpperCase().indexOf("PASSWORD") >= 0) {
                                                                                        data_item = String.valueOf(data_item.substring(0, data_item.indexOf(":") + 1)) + "*******";
                                                                                    }
                                                                                    return this.writeResponse(data_item);
                                                                                }
                                                                                if (command.equalsIgnoreCase("setUserItem")) {
                                                                                    return this.writeResponse(this.setUserItem(request, site));
                                                                                }
                                                                                if (command.equals("refreshUser")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        UserTools.ut.forceMemoryReload(request.getProperty("username"));
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>Success</response></commandResult>");
                                                                                }
                                                                                if (command.equals("resetLdapCaches")) {
                                                                                    cleared_count = 0;
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        keys = ServerStatus.thisObj.server_info.keys();
                                                                                        while (keys.hasMoreElements()) {
                                                                                            key = "" + keys.nextElement();
                                                                                            if (!key.startsWith("ldap_cache_")) continue;
                                                                                            p = (Properties)ServerStatus.thisObj.server_info.get(key);
                                                                                            cleared_count += p.size();
                                                                                            p.clear();
                                                                                        }
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>Success</response><message>" + cleared_count + " items cleared.</message></commandResult>");
                                                                                }
                                                                                if (command.equals("testURL")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        System.getProperties().put("crushftp.isTestCall" + Thread.currentThread().getId(), "true");
                                                                                        try {
                                                                                            msg = "";
                                                                                            try {
                                                                                                c = Common.getClient(request.getProperty("url"), "test", null);
                                                                                                try {
                                                                                                    keys = request.keys();
                                                                                                    while (keys.hasMoreElements()) {
                                                                                                        key = keys.nextElement().toString();
                                                                                                        if (key.equalsIgnoreCase("url") || key.equalsIgnoreCase("command") || key.equalsIgnoreCase("c2f")) continue;
                                                                                                        val = request.getProperty(key);
                                                                                                        c.setConfig(key, val);
                                                                                                    }
                                                                                                    vrl = new VRL(request.getProperty("url"));
                                                                                                    c.login(vrl.getUsername(), vrl.getPassword(), null);
                                                                                                    c.list(vrl.getPath(), new Vector<E>());
                                                                                                    c.logout();
                                                                                                }
                                                                                                finally {
                                                                                                    c.close();
                                                                                                }
                                                                                                msg = "Success";
                                                                                            }
                                                                                            catch (Exception e) {
                                                                                                Log.log("SERVER", 0, e);
                                                                                                msg = "ERROR:" + e;
                                                                                            }
                                                                                            vfs_path = this.writeResponse("<commandResult><response>" + msg + "</response></commandResult>");
                                                                                            return vfs_path;
                                                                                        }
                                                                                        finally {
                                                                                            System.getProperties().remove("crushftp.isTestCall" + Thread.currentThread().getId());
                                                                                        }
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equals("getUserXMLListing")) {
                                                                                    if (request.getProperty("serverGroup_original", "").equals("extra_vfs")) {
                                                                                        request.put("serverGroup", "extra_vfs");
                                                                                    }
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        System.getProperties().put("crushftp.isTestCall" + Thread.currentThread().getId(), request.getProperty("isTestCall", "false"));
                                                                                        try {
                                                                                            listingProp = AdminControls.getUserXMLListing(request, site, this.thisSessionHTTP.thisSession);
                                                                                            altList = "";
                                                                                            response = "";
                                                                                            if (!listingProp.getProperty("error", "").equals("")) {
                                                                                                ServerStatus.thisObj.common_code;
                                                                                                response = Common.getXMLString(listingProp, "listingInfo", null);
                                                                                            } else if (request.getProperty("format", "").equalsIgnoreCase("JSON")) {
                                                                                                altList = AgentUI.getJsonList(listingProp, ServerStatus.BG("exif_listings"), true);
                                                                                            } else if (request.getProperty("format", "").equalsIgnoreCase("STAT")) {
                                                                                                altList = AgentUI.getStatList(listingProp);
                                                                                            }
                                                                                            try {
                                                                                                ServerStatus.thisObj.common_code;
                                                                                                response = Common.getXMLString(listingProp, "listingInfo", null);
                                                                                            }
                                                                                            catch (Exception e) {
                                                                                                Log.log("HTTP_SERVER", 1, e);
                                                                                            }
                                                                                            if (!altList.equals("")) {
                                                                                                response = String.valueOf(response.substring(0, response.indexOf("</privs>") + "</privs>".length())) + altList + response.substring(response.indexOf("</privs>") + "</privs>".length());
                                                                                            }
                                                                                            md5 = this.writeResponse(response);
                                                                                            return md5;
                                                                                        }
                                                                                        finally {
                                                                                            System.getProperties().remove("crushftp.isTestCall" + Thread.currentThread().getId());
                                                                                        }
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("getUserList")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        try {
                                                                                            p = AdminControls.getUserList(request, site, this.thisSessionHTTP.thisSession);
                                                                                            return this.writeResponse(AdminControls.buildXML(p, "user_list", "OK"));
                                                                                        }
                                                                                        catch (Exception e) {
                                                                                            return this.writeResponse("<commandResult><response>FAILURE:" + e + "</response></commandResult>");
                                                                                        }
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("getUserXML")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip")) || site.indexOf("(REPORT_EDIT)") >= 0 && request.getProperty("xmlItem", "").equals("group")) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.getUserXML(request, site, this.thisSessionHTTP.thisSession), "result_item", "OK"));
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equals("getAdminXMLListing")) {
                                                                                    listingProp = new Properties();
                                                                                    if (!request.getProperty("get_from_agentid", "").equals("")) {
                                                                                        items = new Vector();
                                                                                        path = Common.url_decode(request.getProperty("path", "").replace('+', ' '));
                                                                                        request.put("admin_password", Common.url_decode(request.getProperty("admin_password").replace('+', ' ')));
                                                                                        if (path.startsWith("///") && !path.startsWith("////")) {
                                                                                            path = "/" + path;
                                                                                        }
                                                                                        if (!path.startsWith("/")) {
                                                                                            path = "/" + path;
                                                                                        }
                                                                                        if (!path.endsWith("/")) {
                                                                                            path = String.valueOf(path) + "/";
                                                                                        }
                                                                                        path = com.crushftp.client.Common.dots(path);
                                                                                        request.put("path", path);
                                                                                        request.put("user_name", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                                                        request.put("user_ip", this.thisSessionHTTP.thisSession.uiSG("user_ip"));
                                                                                        request.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
                                                                                        request.put("site", site);
                                                                                        try {
                                                                                            items = (Vector)SyncTools.getSyncXMLList(request);
                                                                                        }
                                                                                        catch (Exception e) {
                                                                                            Log.log("HTTP_SERVER", 1, e);
                                                                                        }
                                                                                        listingProp.put("privs", "(read)(view)");
                                                                                        listingProp.put("path", path);
                                                                                        x = 0;
                                                                                        while (x < items.size()) {
                                                                                            p = (Properties)items.elementAt(x);
                                                                                            vrl = new VRL(p.getProperty("url"));
                                                                                            p.put("href_path", vrl.getPath());
                                                                                            ++x;
                                                                                        }
                                                                                        listingProp.put("listing", items);
                                                                                        altList = "";
                                                                                        if (listingProp != null && request.getProperty("format", "").equalsIgnoreCase("JSON")) {
                                                                                            altList = AgentUI.getJsonList(listingProp, ServerStatus.BG("exif_listings"), true);
                                                                                        } else if (listingProp != null && request.getProperty("format", "").equalsIgnoreCase("STAT")) {
                                                                                            altList = AgentUI.getStatList(listingProp);
                                                                                        }
                                                                                        response = "";
                                                                                        try {
                                                                                            ServerStatus.thisObj.common_code;
                                                                                            response = Common.getXMLString(listingProp, "listingInfo", null);
                                                                                        }
                                                                                        catch (Exception e) {
                                                                                            Log.log("HTTP_SERVER", 1, e);
                                                                                        }
                                                                                        if (!altList.equals("")) {
                                                                                            response = String.valueOf(response.substring(0, response.indexOf("</privs>") + "</privs>".length())) + altList + response.substring(response.indexOf("</privs>") + "</privs>".length());
                                                                                        }
                                                                                        return this.writeResponse(response);
                                                                                    }
                                                                                    if (site.indexOf("(CONNECT)") >= 0 || site.indexOf("(USER_EDIT)") >= 0 || site.indexOf("(SHARE_EDIT)") >= 0 || site.indexOf("(LOG_ACCESS)") >= 0) {
                                                                                        return this.writeResponse(AdminControls.returnAdminListing(request, this.thisSessionHTTP.thisSession, site));
                                                                                    }
                                                                                    if (this.thisSessionHTTP.thisSession.getProperty("admin_group_name").equals("")) {
                                                                                        return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                    }
                                                                                    groupName = this.thisSessionHTTP.thisSession.getAdminGroupName(request);
                                                                                    if (request.getProperty("file_mode").equals("server")) {
                                                                                        listingProp = AdminControls.getAdminXMLListing(request, this.thisSessionHTTP.thisSession, site);
                                                                                        vrl = new VRL(listingProp.getProperty("path"));
                                                                                        if (!UserTools.parentPathOK(this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer"), this.thisSessionHTTP.thisSession.uiSG("user_name"), vrl.toString())) {
                                                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                        }
                                                                                        return this.writeResponse(AdminControls.returnAdminListing(request, this.thisSessionHTTP.thisSession, site));
                                                                                    }
                                                                                    path = Common.url_decode(request.getProperty("path", "").replace('+', ' '));
                                                                                    if (path.startsWith("///") && !path.startsWith("////")) {
                                                                                        path = "/" + path;
                                                                                    }
                                                                                    if (!path.startsWith("/")) {
                                                                                        path = "/" + path;
                                                                                    }
                                                                                    if (!path.endsWith("/")) {
                                                                                        path = String.valueOf(path) + "/";
                                                                                    }
                                                                                    path = com.crushftp.client.Common.dots(path);
                                                                                    request.put("command", "getXMLListing");
                                                                                    response = this.getXmlListingResponse(groupName, request, path, true, UserTools.ut.getVFS(this.thisSessionHTTP.thisSession.uiSG("listen_ip_port"), groupName));
                                                                                    return this.writeResponse(response, request.getProperty("format", "").equalsIgnoreCase("JSONOBJ"));
                                                                                }
                                                                                if (command.equalsIgnoreCase("searchUserSettings")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.buildXML(AdminControls.searchUserSettings(request, site, this.thisSessionHTTP.thisSession), "user_settings_list", "OK"));
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("adminAction")) {
                                                                                    result = AdminControls.adminAction(Common.urlDecodePost(request), site, this.thisSessionHTTP.thisSession.uiSG("user_ip"));
                                                                                    if (result instanceof String && result.toString().equals("")) {
                                                                                        return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                    }
                                                                                    return this.writeResponse(AdminControls.buildXML(result, "result_item", "OK"));
                                                                                }
                                                                                if (command.equalsIgnoreCase("checkForUpdate")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.checkForUpdate(request, site));
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("updateNow")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.updateNow(request, site));
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("updateWebNow")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.updateWebNow(request, site));
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("updateNowProgress")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.updateNowProgress(request, site));
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("cancelUpdateProgress")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.cancelUpdateProgress(request, site));
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("getRestartShutdownIdleStatus")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.getRestartShutdownIdleStatus(request, site));
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("updateIdle")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.updateIdle(request, site));
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("restartIdle")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.restartIdle(request, site));
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("shutdownIdle")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.shutdownIdle(request, site));
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("startLogins")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.startLogins(request, site));
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("stopLogins")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.stopLogins(request, site));
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (command.equalsIgnoreCase("dumpStack")) {
                                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                        return this.writeResponse(AdminControls.dumpStack(request, site));
                                                                                    }
                                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                                }
                                                                                if (!command.equalsIgnoreCase("prometheusMetrics")) break block901;
                                                                                if (ServerStatus.siIG("enterprise_level") > 0) break block902;
                                                                                response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                                                                response = String.valueOf(response) + "<commandResult><response>";
                                                                                response = String.valueOf(response) + "The server does not have an enterprise license, so prometheus exporting is not allowed.\r\n<br/>";
                                                                                response = String.valueOf(response) + "</response></commandResult>";
                                                                                this.writeResponse(response);
                                                                                break block903;
                                                                            }
                                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                s = AdminControls.prometheusMetrics(request, site);
                                                                                this.thisSessionHTTP.done = true;
                                                                                this.write_command_http("HTTP/1.1 200 OK");
                                                                                this.write_command_http("Content-Type: text/plain");
                                                                                this.thisSessionHTTP.write_standard_headers();
                                                                                utf8 = s.getBytes("UTF8");
                                                                                this.write_command_http("Content-Length: " + utf8.length);
                                                                                this.write_command_http("X-UA-Compatible: chrome=1");
                                                                                this.write_command_http("");
                                                                                this.thisSessionHTTP.original_os.write(utf8);
                                                                                this.thisSessionHTTP.original_os.flush();
                                                                                return true;
                                                                            }
                                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                        }
                                                                        if (command.equalsIgnoreCase("upload_debug_info")) {
                                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                if (request.getProperty("save", "true").equals("true")) {
                                                                                    this.thisSessionHTTP.done = true;
                                                                                    request.put("outputstream", this.thisSessionHTTP.original_os);
                                                                                    AdminControls.upload_debug_info(request, site);
                                                                                    return true;
                                                                                }
                                                                                return this.writeResponse("<commandResult><response>" + AdminControls.upload_debug_info(request, site) + "</response></commandResult>");
                                                                            }
                                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                        }
                                                                        if (command.equalsIgnoreCase("dumpHeap")) {
                                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                                return this.writeResponse(AdminControls.dumpHeap(request, site));
                                                                            }
                                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                        }
                                                                        if (!command.equalsIgnoreCase("setMaxServerMemory")) break block904;
                                                                        if (!AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) break block905;
                                                                        AdminControls.setMaxServerMemory(request, site);
                                                                        break block903;
                                                                    }
                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                }
                                                                if (command.equalsIgnoreCase("setEncryptionPassword")) {
                                                                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                        return this.writeResponse(AdminControls.setEncryptionPassword(request, site));
                                                                    }
                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                }
                                                                if (!command.equalsIgnoreCase("restartProcess")) break block906;
                                                                if (!AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) break block907;
                                                                AdminControls.restartProcess(request, site);
                                                                break block903;
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("system.gc")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                result = AdminControls.forceGC(request, site);
                                                                return this.writeResponse("<commandResult><response>" + result + "</response></commandResult>");
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("pgpGenerateKeyPair")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.pgpGenerateKeyPair(request, site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("runReport")) {
                                                            if (request.containsKey("report_token") || AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                if (com.crushftp.client.Common.dmz_mode && request.containsKey("report_token")) {
                                                                    the_dir = this.thisSessionHTTP.thisSession.SG("root_dir");
                                                                    item = this.thisSessionHTTP.thisSession.uVFS.get_item(the_dir);
                                                                    config = new Properties();
                                                                    config.put("protocol", "HTTP");
                                                                    urlc = null;
                                                                    loops = 0;
                                                                    while (loops++ < 100) {
                                                                        urlc = URLConnection.openConnection(new VRL(Common.getBaseUrl(item.getProperty("url"))), config);
                                                                        urlc.setRequestMethod("POST");
                                                                        c = this.thisSessionHTTP.thisSession.uVFS.getClient(item);
                                                                        urlc.setRequestProperty("Cookie", "CrushAuth=" + c.getConfig("crushAuth") + ";");
                                                                        urlc.setUseCaches(false);
                                                                        urlc.setDoOutput(true);
                                                                        b = ("command=runReport&c2f=" + c.getConfig("crushAuth", "").substring(c.getConfig("crushAuth", "").length() - 4) + "&reportName=" + request.getProperty("reportName", "") + "&report_token=" + request.getProperty("report_token", "")).getBytes("UTF8");
                                                                        urlc.setLength(b.length);
                                                                        pout = urlc.getOutputStream();
                                                                        pout.write(b);
                                                                        pout.flush();
                                                                        if (urlc.getResponseCode() == 302) {
                                                                            c.setConfig("error", "Logged out.");
                                                                            urlc.disconnect();
                                                                            this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                                                            Thread.sleep(100L);
                                                                            continue;
                                                                        }
                                                                        this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                                                        break;
                                                                    }
                                                                    this.write_command_http("HTTP/1.1 " + urlc.getResponseCode() + " OK");
                                                                    this.write_command_http("Content-Type: " + urlc.getHeaderField("CONTENT-TYPE"));
                                                                    this.write_command_http("Content-Length: " + urlc.getHeaderField("CONTENT-LENGTH"));
                                                                    this.thisSessionHTTP.write_standard_headers();
                                                                    this.write_command_http("X-UA-Compatible: chrome=1");
                                                                    this.write_command_http("");
                                                                    pin = urlc.getInputStream();
                                                                    b = new byte[32768];
                                                                    max_len = Integer.parseInt(urlc.getHeaderField("CONTENT-LENGTH").trim());
                                                                    bytesRead = 0;
                                                                    while (max_len > 0) {
                                                                        if (b.length > max_len) {
                                                                            b = new byte[max_len];
                                                                        }
                                                                        bytesRead = pin.read(b);
                                                                        if (bytesRead <= 0) continue;
                                                                        max_len -= bytesRead;
                                                                        this.thisSessionHTTP.original_os.write(b, 0, bytesRead);
                                                                    }
                                                                    this.thisSessionHTTP.original_os.flush();
                                                                    pin.close();
                                                                    urlc.disconnect();
                                                                } else {
                                                                    xml = AdminControls.runReport(request, site);
                                                                    this.write_command_http("HTTP/1.1 200 OK");
                                                                    this.write_command_http("Cache-Control: no-store");
                                                                    this.write_command_http("Content-Type: text/" + (request.getProperty("export", "false").equals("true") ? "plain" : "html") + ";charset=utf-8");
                                                                    this.thisSessionHTTP.write_standard_headers();
                                                                    utf8 = xml.getBytes("UTF8");
                                                                    this.write_command_http("Content-Length: " + utf8.length);
                                                                    appname = String.valueOf(request.getProperty("reportName")) + (request.getProperty("export", "false").equals("true") ? ".csv" : ".html");
                                                                    if (request.getProperty("saveReport", "").equalsIgnoreCase("true")) {
                                                                        this.write_command_http("Content-Disposition: attachment; filename=\"" + Common.replace_str(Common.url_decode(appname), "\r", "_") + "\"");
                                                                    }
                                                                    this.write_command_http("X-UA-Compatible: chrome=1");
                                                                    this.write_command_http("");
                                                                    this.thisSessionHTTP.original_os.write(utf8);
                                                                    this.thisSessionHTTP.original_os.flush();
                                                                }
                                                                return true;
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("testReportSchedule")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.testReportSchedule(request, site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("testPGP")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.testPGP(request, site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("testJobSchedule")) {
                                                            Common.urlDecodePost(request);
                                                            request.put("calling_user", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                            request.put("calling_linkedServer", this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer"));
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.testJobSchedule(request, site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("testSMTP")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.testSMTP(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("importUsers")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.importUsers(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("sendPassEmail")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                Common.urlDecodePost(request);
                                                                if (site.indexOf("(CONNECT)") < 0) {
                                                                    request.put("serverGroup", this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer"));
                                                                }
                                                                username = request.getProperty("user_name", "");
                                                                info = (Properties)this.thisSessionHTTP.thisSession.get("user_admin_info");
                                                                if (site.indexOf("(CONNECT)") < 0 && site.indexOf("(USER_EDIT)") < 0 && (list = (Vector)info.get("list")).indexOf(username) < 0) {
                                                                    throw new Exception("Username " + username + " not found.");
                                                                }
                                                                return this.writeResponse(AdminControls.sendPassEmail(request, this.thisSessionHTTP.thisSession, site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("sendEventEmail")) {
                                                            Common.urlDecodePost(request);
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.sendEventEmail(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("getTempAccounts")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.getTempAccounts(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("addTempAccount")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.addTempAccount(request, site, true));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("removeTempAccount")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.removeTempAccount(Common.urlDecodePost(request), site, true));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("getTempAccountFiles")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.getTempAccountFiles(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("removeTempAccountFile")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.removeTempAccountFile(Common.urlDecodePost(request), site, true));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("addTempAccountFile")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.addTempAccountFile(Common.urlDecodePost(request), site, true));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("deleteReplication")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.deleteReplication(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("migrateUsersVFS")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.migrateUsersVFS(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("convertUsers")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.convertUsers(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("generateSSL")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.generateSSL(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("generateCSR")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.generateCSR(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("importReply")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.importReply(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("testKeystore")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.testKeystore(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("listSSL")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.listSSL(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("deleteSSL")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.deleteSSL(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("renameSSL")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.renameSSL(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("exportSSL")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.exportSSL(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("addPrivateSSL")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.addPrivateSSL(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("addPublicSSL")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.addPublicSSL(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("telnetSocket")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.telnetSocket(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("testDB")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.testDB(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("testQuery")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.testQuery(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("validateAppMD5s")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.validateAppMD5s(Common.urlDecodePost(request)));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (command.equalsIgnoreCase("pluginMethodCall")) {
                                                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                                return this.writeResponse(AdminControls.pluginMethodCall(Common.urlDecodePost(request), site));
                                                            }
                                                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                        }
                                                        if (!command.equalsIgnoreCase("generateToken")) break block908;
                                                        if (site.indexOf("(USER_ADMIN)") < 0 && site.indexOf("(CONNECT)") < 0) break block909;
                                                        if (site.indexOf("(CONNECT)") < 0) {
                                                            if (ServerStatus.BG("generatetoken_limited_admin_group_only")) {
                                                                ok = false;
                                                                v = new Vector<E>();
                                                                AdminControls.getLimitedAdminUserList(request, this.thisSessionHTTP.thisSession, v);
                                                                x = 0;
                                                                while (x < v.size()) {
                                                                    if (v.elementAt(x).toString().equalsIgnoreCase(request.getProperty("username", ""))) {
                                                                        ok = true;
                                                                    }
                                                                    ++x;
                                                                }
                                                                if (!ok) {
                                                                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                                                }
                                                            }
                                                        }
                                                        if (!com.crushftp.client.Common.dmz_mode) break block910;
                                                        queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                                        action = new Properties();
                                                        action.put("type", "GET:SINGLETON");
                                                        action.put("id", Common.makeBoundary());
                                                        action.put("need_response", "true");
                                                        queue.addElement(action);
                                                        action = UserTools.waitResponse(action, 60);
                                                        singleton_id = "";
                                                        if (action != null) {
                                                            singleton_id = "" + action.get("singleton_id");
                                                        }
                                                        action = new Properties();
                                                        action.put("type", "GET:GENERATE_TOKEN");
                                                        action.put("id", Common.makeBoundary());
                                                        action.put("request", request);
                                                        action.put("site", site);
                                                        action.put("need_response", "true");
                                                        action.put("singleton_id", singleton_id);
                                                        queue.addElement(action);
                                                        action = UserTools.waitResponse(action, 60);
                                                        if (action != null) {
                                                            this.writeResponse(action.getProperty("responseText", ""));
                                                        }
                                                        break block903;
                                                    }
                                                    request.put("method", "generateToken");
                                                    request.put("pluginName", "CrushSSO");
                                                    request.put("pluginSubItem", request.getProperty("pluginSubItem", ""));
                                                    response = AdminControls.pluginMethodCall(Common.urlDecodePost(request), site);
                                                    if (SharedSessionReplicated.send_queues.size() > 0 && ServerStatus.thisObj.server_info.get("crushSSO_tokens") != null) {
                                                        SharedSessionReplicated.send(Common.makeBoundary(), "SYNC_CRUSHSSO_TOKENS", "tokens", ServerStatus.thisObj.server_info.get("crushSSO_tokens"));
                                                    }
                                                    return this.writeResponse(response);
                                                }
                                                return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                            }
                                            if (command.equalsIgnoreCase("convertXMLSQLUsers")) {
                                                if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                    return this.writeResponse(AdminControls.convertXMLSQLUsers(Common.urlDecodePost(request), site));
                                                }
                                                return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                            }
                                            if (command.equalsIgnoreCase("register" + System.getProperty("appname", "CrushFTP"))) {
                                                if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                    return this.writeResponse(AdminControls.registerCrushFTP(Common.urlDecodePost(request), site));
                                                }
                                                return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                            }
                                            if (command.equalsIgnoreCase("restorePrefs")) {
                                                if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                    return this.writeResponse(AdminControls.restorePrefs(Common.urlDecodePost(request), site));
                                                }
                                                return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                            }
                                            if (command.equalsIgnoreCase("unblockUsername")) {
                                                if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                    return this.writeResponse(AdminControls.unblockUsername(Common.urlDecodePost(request), site));
                                                }
                                                return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                            }
                                            if (command.equalsIgnoreCase("testOTP")) {
                                                if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                    return this.writeResponse(AdminControls.testOTP(Common.urlDecodePost(request), site));
                                                }
                                                return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                            }
                                            if (command.equalsIgnoreCase("unban")) {
                                                if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                                    return this.writeResponse(AdminControls.unban(request));
                                                }
                                                return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                                            }
                                            if (command.equalsIgnoreCase("upload") || request.getProperty("the_action", "").equalsIgnoreCase("STOR")) {
                                                result = false;
                                                code = 200;
                                                response = "<commandResult><response>";
                                                activeUpload = (Properties)com.crushftp.client.Common.System2.get("crushftp.activeUpload.info" + this.thisSessionHTTP.thisSession.getId());
                                                if (activeUpload != null && activeUpload.getProperty("fileupload", "").indexOf("ERROR:") >= 0) {
                                                    response = String.valueOf(response) + activeUpload.getProperty("fileupload", "");
                                                    code = 500;
                                                } else {
                                                    response = String.valueOf(response) + "Success";
                                                }
                                                this.thisSessionHTTP.done = true;
                                                response = String.valueOf(response) + "</response><last_md5>" + activeUpload.getProperty("last_md5", "") + "</last_md5></commandResult>";
                                                result = this.writeResponse(response, true, code, true, false, true);
                                                if (this.thisSessionHTTP.chunked) {
                                                    Thread.sleep(1000L);
                                                }
                                                return result;
                                            }
                                            if (command.equalsIgnoreCase("upload_0_byte")) {
                                                if (com.crushftp.client.Common.dmz_mode) {
                                                    the_dir = request.getProperty("path");
                                                    if (!the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                                        the_dir = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + the_dir.substring(1);
                                                    }
                                                    item = this.thisSessionHTTP.thisSession.uVFS.get_item(Common.all_but_last(the_dir));
                                                    config = new Properties();
                                                    config.put("protocol", "HTTP");
                                                    urlc = null;
                                                    loops = 0;
                                                    while (loops++ < 100) {
                                                        urlc = URLConnection.openConnection(new VRL(Common.getBaseUrl(item.getProperty("url"))), config);
                                                        urlc.setRequestMethod("POST");
                                                        c = this.thisSessionHTTP.thisSession.uVFS.getClient(item);
                                                        urlc.setRequestProperty("Cookie", "CrushAuth=" + c.getConfig("crushAuth") + ";");
                                                        urlc.setUseCaches(false);
                                                        urlc.setDoOutput(true);
                                                        b = ("command=upload_0_byte&c2f=" + c.getConfig("crushAuth", "").substring(c.getConfig("crushAuth", "").length() - 4) + "&path=" + request.getProperty("path", "")).getBytes("UTF8");
                                                        urlc.setLength(b.length);
                                                        pout = urlc.getOutputStream();
                                                        pout.write(b);
                                                        pout.flush();
                                                        if (urlc.getResponseCode() == 302) {
                                                            c.setConfig("error", "Logged out.");
                                                            urlc.disconnect();
                                                            this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                                            Thread.sleep(100L);
                                                            continue;
                                                        }
                                                        this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                                        break;
                                                    }
                                                    baos = new ByteArrayOutputStream();
                                                    com.crushftp.client.Common.streamCopier(urlc.getInputStream(), baos, false, true, true);
                                                    urlc.disconnect();
                                                    return this.writeResponse(new String(baos.toByteArray()));
                                                }
                                                error = "";
                                                the_dir = Common.url_decode(request.getProperty("path", ""));
                                                if (the_dir.equals("/")) {
                                                    the_dir = this.thisSessionHTTP.thisSession.SG("root_dir");
                                                }
                                                if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.thisSessionHTTP.thisSession.SG("root_dir").toUpperCase())) {
                                                    the_dir = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + the_dir.substring(1);
                                                }
                                                if (this.thisSessionHTTP.thisSession.check_access_privs(the_dir, "STOR")) {
                                                    item = this.thisSessionHTTP.thisSession.uVFS.get_item_parent(the_dir);
                                                    c = this.thisSessionHTTP.thisSession.uVFS.getClient(item);
                                                    try {
                                                        c.upload(new VRL(item.getProperty("url")).getPath(), 0L, true, true).close();
                                                    }
                                                    catch (Exception e) {
                                                        error = String.valueOf(error) + e;
                                                    }
                                                    c.close();
                                                    this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                                }
                                                response = "<commandResult><response>" + (error.equals("") ? "OK" : error) + "</response></commandResult>";
                                                return this.writeResponse(response);
                                            }
                                            if (command.equalsIgnoreCase("blockUploads")) {
                                                this.thisSessionHTTP.thisSession.put("blockUploads", "true");
                                                response = "<commandResult><response>OK</response></commandResult>";
                                                return this.writeResponse(response);
                                            }
                                            if (command.equalsIgnoreCase("unblockUploads")) {
                                                this.thisSessionHTTP.thisSession.put("blockUploads", "false");
                                                response = "<commandResult><response>OK</response></commandResult>";
                                                return this.writeResponse(response);
                                            }
                                            if (command.equalsIgnoreCase("getLastUploadError")) {
                                                response = "<commandResult><response>";
                                                activeUpload = (Properties)com.crushftp.client.Common.System2.get("crushftp.activeUpload.info" + this.thisSessionHTTP.thisSession.getId());
                                                response = activeUpload != null && activeUpload.getProperty("fileupload", "").indexOf("ERROR:") >= 0 ? String.valueOf(response) + activeUpload.getProperty("fileupload", "") : (!this.thisSessionHTTP.thisSession.user_info.getProperty("last_upload_error", "").equals("") ? String.valueOf(response) + Common.url_encode(this.thisSessionHTTP.thisSession.user_info.getProperty("last_upload_error", "")) : String.valueOf(response) + "Success");
                                                this.thisSessionHTTP.done = true;
                                                response = String.valueOf(response) + "</response></commandResult>";
                                                result = this.writeResponse(response);
                                                if (this.thisSessionHTTP.chunked) {
                                                    Thread.sleep(1000L);
                                                }
                                                return result;
                                            }
                                            if (command.equalsIgnoreCase("getQR")) {
                                                block887: {
                                                    Thread.sleep(1000L);
                                                    b = new byte[10];
                                                    Common.rn.nextBytes(b);
                                                    generatedKey = com.crushftp.client.Common.encodeBase32(b);
                                                    qrTokens = ServerStatus.siPG("qrTokens");
                                                    if (qrTokens == null) {
                                                        qrTokens = new Properties();
                                                    }
                                                    ServerStatus.thisObj.server_info.put("qrTokens", qrTokens);
                                                    qrTokens.put(String.valueOf(request.getProperty("qrid")) + ":" + System.currentTimeMillis(), generatedKey);
                                                    qr_url = "https://chart.googleapis.com/chart?chs=200x200&chld=M%7C0&cht=qr&chl=otpauth%3A%2F%2Ftotp%2F" + this.thisSessionHTTP.hostString + "%3A" + this.thisSessionHTTP.thisSession.uiSG("user_name") + "%3Fsecret%3D" + generatedKey + "%26issuer%3D" + this.thisSessionHTTP.hostString + "%26algorithm%3D" + "SHA1" + "%26digits%3D" + 6 + "%26period%3D" + 30;
                                                    config = new Properties();
                                                    qr_baos = new ByteArrayOutputStream();
                                                    try {
                                                        urlc = URLConnection.openConnection(new VRL(qr_url), config);
                                                        urlc.setRequestMethod("GET");
                                                        urlc.setReadTimeout(5000);
                                                        code = urlc.getResponseCode();
                                                        if (code == 302) {
                                                            urlc.disconnect();
                                                            Log.log("HTTP_SERVER", 1, " QR code : Redirect.");
                                                            location = urlc.getHeaderField("Location");
                                                            urlc = URLConnection.openConnection(new VRL(location), config);
                                                            urlc.setRequestMethod("GET");
                                                            urlc.setReadTimeout(5000);
                                                        }
                                                        Common.streamCopier(urlc.getInputStream(), qr_baos, false, true, true);
                                                        if (qr_baos.size() == 0) {
                                                            Log.log("HTTP_SERVER", 1, " QR code : Retry.");
                                                            url_java = new URL(qr_url);
                                                            urlc2 = (HttpURLConnection)url_java.openConnection();
                                                            urlc2.setRequestMethod("GET");
                                                            urlc2.setReadTimeout(5000);
                                                            Common.streamCopier(urlc2.getInputStream(), qr_baos, false, true, true);
                                                        }
                                                    }
                                                    catch (Exception e) {
                                                        Log.log("HTTP_SERVER", 1, e);
                                                        Log.log("HTTP_SERVER", 1, "QR code : dmz reroute");
                                                        config.put("use_dmz", "true");
                                                        try {
                                                            urlc = URLConnection.openConnection(new VRL(qr_url), config);
                                                            urlc.setRequestMethod("GET");
                                                            urlc.setReadTimeout(5000);
                                                            code = urlc.getResponseCode();
                                                            if (code == 302) {
                                                                urlc.disconnect();
                                                                Log.log("HTTP_SERVER", 1, " QR code : DMZ reroute. Redirect.");
                                                                location = urlc.getHeaderField("Location");
                                                                urlc = URLConnection.openConnection(new VRL(location), config);
                                                                urlc.setRequestMethod("GET");
                                                                urlc.setReadTimeout(5000);
                                                            }
                                                            Common.streamCopier(urlc.getInputStream(), qr_baos, false, true, true);
                                                        }
                                                        catch (Exception ee) {
                                                            Log.log("HTTP_SERVER", 1, ee);
                                                        }
                                                        if (qr_baos.size() != 0) break block887;
                                                        try {
                                                            url_java = new URL(qr_url);
                                                            urlc2 = (HttpURLConnection)url_java.openConnection();
                                                            urlc2.setRequestMethod("GET");
                                                            urlc2.setReadTimeout(5000);
                                                            Common.streamCopier(urlc2.getInputStream(), qr_baos, false, true, true);
                                                        }
                                                        catch (Exception ee) {
                                                            Log.log("HTTP_SERVER", 1, ee);
                                                        }
                                                    }
                                                }
                                                this.write_command_http("HTTP/1.1 200 OK");
                                                validSecs = 1;
                                                this.write_command_http("Cache-Control: post-check=" + validSecs + ",pre-check=" + validSecs * 10);
                                                this.write_command_http("Content-Type: image/png");
                                                this.thisSessionHTTP.write_standard_headers();
                                                this.write_command_http("X-UA-Compatible: chrome=1");
                                                this.write_command_http("Content-length: " + qr_baos.size());
                                                this.write_command_http("Connection: close");
                                                this.write_command_http("");
                                                this.thisSessionHTTP.original_os.write(qr_baos.toByteArray());
                                                this.thisSessionHTTP.original_os.flush();
                                                return true;
                                            }
                                            if (command.equalsIgnoreCase("confirmQR")) {
                                                Thread.sleep(1000L);
                                                response = "<commandResult><response>";
                                                qrTokens = ServerStatus.siPG("qrTokens");
                                                if (qrTokens == null) {
                                                    qrTokens = new Properties();
                                                }
                                                keys = qrTokens.keys();
                                                response = "ERROR:QR token not found.";
                                                while (keys.hasMoreElements()) {
                                                    key = keys.nextElement().toString();
                                                    if (System.currentTimeMillis() - Long.parseLong(key.split(":")[1]) > 60000L) {
                                                        qrTokens.remove(key);
                                                        continue;
                                                    }
                                                    if (!this.thisSessionHTTP.thisSession.user.getProperty("twofactor_secret", "").equals("")) {
                                                        response = "ERROR:Server administrator must remove existing two factor auth first.";
                                                        continue;
                                                    }
                                                    if (!key.startsWith(String.valueOf(request.getProperty("qrid")) + ":")) continue;
                                                    generatedKey = "" + qrTokens.remove(key);
                                                    if (com.crushftp.client.Common.dmz_mode) {
                                                        queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                                        action = new Properties();
                                                        action.put("type", "PUT:TWO_FACTOR_SECRET");
                                                        root_item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir"));
                                                        c = this.thisSessionHTTP.thisSession.uVFS.getClient(root_item);
                                                        action.put("crushAuth", c.getConfig("crushAuth"));
                                                        this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                                        action.put("id", Common.makeBoundary());
                                                        action.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                        action.put("linkedServer", this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer"));
                                                        action.put("generatedKey", generatedKey);
                                                        action.put("need_response", "true");
                                                        queue.addElement(action);
                                                        action = UserTools.waitResponse(action, 60);
                                                        response = action.getProperty("response", "");
                                                        continue;
                                                    }
                                                    twofactor_secret = ServerStatus.thisObj.common_code.encode_pass(generatedKey, "DES", "");
                                                    if (ServerStatus.BG("twofactor_secret_auto_otp_enable")) {
                                                        p = UserTools.ut.getUser(this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer"), this.thisSessionHTTP.thisSession.uiSG("user_name"), false);
                                                        if (!p.getProperty("otp_auth", "").equals("true")) {
                                                            p.put("otp_auth", "true");
                                                            p.put("otp_auth_ftp", "true");
                                                            p.put("otp_auth_ftps", "true");
                                                            p.put("otp_auth_http", "true");
                                                            p.put("otp_auth_https", "true");
                                                            p.put("otp_auth_sftp", "true");
                                                            p.put("otp_auth_webdav", "true");
                                                            p.put("otp_token_timeout", ServerStatus.SG("otp_token_timeout"));
                                                            p.put("twofactor_secret", twofactor_secret);
                                                            UserTools.writeUser(this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer"), this.thisSessionHTTP.thisSession.uiSG("user_name"), p, true, true);
                                                        }
                                                    } else {
                                                        UserTools.ut.put_in_user(this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer"), this.thisSessionHTTP.thisSession.uiSG("user_name"), "twofactor_secret", twofactor_secret, true, true);
                                                    }
                                                    response = "Success";
                                                    this.thisSessionHTTP.thisSession.user.put("twofactor_secret", twofactor_secret);
                                                    Log.log("SERVER", 0, "Saving two factor secret to user profile:" + this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                                    ServerStatus.thisObj.runAlerts("twofactor_secret_change", this.thisSessionHTTP.thisSession);
                                                }
                                                response = "<commandResult><response>" + response + "</response></commandResult>";
                                                return this.writeResponse(response);
                                            }
                                            if (command.equalsIgnoreCase("getPreview")) {
                                                request.put("path", Common.replace_str(request.getProperty("path"), "+", "%2B"));
                                                Common.urlDecodePost(request);
                                                if (com.crushftp.client.Common.dmz_mode) {
                                                    the_dir = request.getProperty("path");
                                                    if (!the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                                        the_dir = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + the_dir.substring(1);
                                                    }
                                                    item = this.thisSessionHTTP.thisSession.uVFS.get_item(the_dir);
                                                    config = new Properties();
                                                    config.put("protocol", "HTTP");
                                                    urlc = null;
                                                    loops = 0;
                                                    while (loops++ < 100) {
                                                        urlc = URLConnection.openConnection(new VRL(Common.getBaseUrl(item.getProperty("url"))), config);
                                                        urlc.setRequestMethod("POST");
                                                        c = this.thisSessionHTTP.thisSession.uVFS.getClient(item);
                                                        urlc.setRequestProperty("Cookie", "CrushAuth=" + c.getConfig("crushAuth") + ";");
                                                        urlc.setUseCaches(false);
                                                        urlc.setDoOutput(true);
                                                        b = ("command=getPreview&c2f=" + c.getConfig("crushAuth", "").substring(c.getConfig("crushAuth", "").length() - 4) + "&size=" + request.getProperty("size", "1") + "&frame=" + request.getProperty("frame", "1") + "&object_type=" + request.getProperty("object_type", "image") + "&download=" + request.getProperty("download", "false") + "&path=" + Common.url_encode(request.getProperty("path"))).getBytes("UTF8");
                                                        urlc.setLength(b.length);
                                                        pout = urlc.getOutputStream();
                                                        pout.write(b);
                                                        pout.flush();
                                                        if (urlc.getResponseCode() == 302) {
                                                            c.setConfig("error", "Logged out.");
                                                            urlc.disconnect();
                                                            this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                                            Thread.sleep(100L);
                                                            continue;
                                                        }
                                                        this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                                        break;
                                                    }
                                                    this.write_command_http("HTTP/1.1 " + urlc.getResponseCode() + " OK");
                                                    this.write_command_http("Content-Type: " + urlc.getHeaderField("CONTENT-TYPE"));
                                                    this.write_command_http("Content-Length: " + urlc.getHeaderField("CONTENT-LENGTH"));
                                                    this.thisSessionHTTP.write_standard_headers();
                                                    this.write_command_http("X-UA-Compatible: chrome=1");
                                                    this.write_command_http("");
                                                    pin = urlc.getInputStream();
                                                    b = new byte[32768];
                                                    max_len = Integer.parseInt(urlc.getHeaderField("CONTENT-LENGTH").trim());
                                                    bytesRead = 0;
                                                    while (max_len > 0) {
                                                        if (b.length > max_len) {
                                                            b = new byte[max_len];
                                                        }
                                                        bytesRead = pin.read(b);
                                                        if (bytesRead <= 0) continue;
                                                        max_len -= bytesRead;
                                                        this.thisSessionHTTP.original_os.write(b, 0, bytesRead);
                                                    }
                                                    this.thisSessionHTTP.original_os.flush();
                                                    pin.close();
                                                    urlc.disconnect();
                                                } else {
                                                    paths_raw = new Vector<String>();
                                                    paths_updated = new Vector<String>();
                                                    path_str = request.getProperty("path").split(";");
                                                    path = "";
                                                    x = 0;
                                                    while (x < path_str.length) {
                                                        path = path_str[x];
                                                        if (!path.trim().equals("")) {
                                                            paths_raw.addElement(path);
                                                            this.thisSessionHTTP.setupCurrentDir(path);
                                                            Log.log("HTTP_SERVER", 2, "getPreview:" + path);
                                                            if (path.startsWith("@")) {
                                                                if ((path = path.substring(1)).indexOf("..") >= 0) {
                                                                    path = "";
                                                                }
                                                                if (!path.equals("folder")) {
                                                                    if (path.indexOf(".") >= 0) {
                                                                        path = path.substring(path.lastIndexOf(".") + 1);
                                                                    }
                                                                    path = new File_S(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/images/mimetypes/" + path + ".png").exists() ? "/WebInterface/images/mimetypes/" + path + ".png" : "file";
                                                                }
                                                                if (!path.startsWith("/")) {
                                                                    path = "/WebInterface/images/" + path + ".png/p1/" + request.getProperty("size") + ".png";
                                                                }
                                                            } else if (request.getProperty("object_type", "image").equals("exif")) {
                                                                Log.log("HTTP_SERVER", 2, "getPreview2:" + this.thisSessionHTTP.pwd());
                                                                item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.pwd());
                                                                path = "/WebInterface/images" + SearchHandler.getPreviewPath(item.getProperty("url"), "1", 1);
                                                                path = String.valueOf(Common.all_but_last(Common.all_but_last(path))) + "info.xml";
                                                                Log.log("HTTP_SERVER", 2, "getPreview3:" + path);
                                                            } else {
                                                                Log.log("HTTP_SERVER", 2, "getPreview2:" + this.thisSessionHTTP.pwd());
                                                                item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.pwd());
                                                                path = "/WebInterface/images" + SearchHandler.getPreviewPath(item.getProperty("url"), request.getProperty("size"), Integer.parseInt(request.getProperty("frame", "1")));
                                                                Log.log("HTTP_SERVER", 2, "getPreview3:" + path);
                                                            }
                                                            paths_updated.addElement(path);
                                                        }
                                                        ++x;
                                                    }
                                                    if (paths_updated.size() > 0) {
                                                        path = paths_updated.elementAt(0).toString();
                                                    }
                                                    s = "GET " + path + " HTTP/1.1";
                                                    this.thisSessionHTTP.headers.setElementAt(s, 0);
                                                    downloadFilename = null;
                                                    if (request.getProperty("download", "false").equals("true") && (downloadFilename = Common.last(request.getProperty("path"))).indexOf(".") >= 0) {
                                                        downloadFilename = String.valueOf(downloadFilename.substring(0, downloadFilename.lastIndexOf("."))) + ".jpg";
                                                    }
                                                    if (paths_updated.size() == 1) {
                                                        ServerSessionHTTPWI.serveFile(this.thisSessionHTTP, this.thisSessionHTTP.headers, this.thisSessionHTTP.original_os, false, downloadFilename);
                                                    } else {
                                                        this.write_command_http("HTTP/1.1 200 OK");
                                                        validSecs = 1;
                                                        this.write_command_http("Cache-Control: post-check=" + validSecs + ",pre-check=" + validSecs * 10);
                                                        this.write_command_http("Content-Type: application/zip");
                                                        this.thisSessionHTTP.write_standard_headers();
                                                        this.write_command_http("Content-Disposition: attachment; filename=\"images.zip\"");
                                                        this.write_command_http("X-UA-Compatible: chrome=1");
                                                        this.write_command_http("Connection: close");
                                                        this.write_command_http("");
                                                        zaous = new ZipArchiveOutputStream(this.thisSessionHTTP.original_os);
                                                        x = 0;
                                                        while (x < paths_raw.size()) {
                                                            path = paths_raw.elementAt(x).toString();
                                                            downloadFilename = Common.last(path);
                                                            if (downloadFilename.indexOf(".") >= 0) {
                                                                downloadFilename = String.valueOf(downloadFilename.substring(0, downloadFilename.lastIndexOf("."))) + ".jpg";
                                                            }
                                                            zae = new ZipArchiveEntry(downloadFilename);
                                                            zaous.putArchiveEntry((ArchiveEntry)zae);
                                                            this.thisSessionHTTP.setupCurrentDir(path);
                                                            item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.pwd());
                                                            f = new File_S(String.valueOf(ServerStatus.SG("previews_path")) + SearchHandler.getPreviewPath(item.getProperty("url"), request.getProperty("size"), Integer.parseInt(request.getProperty("frame", "1"))));
                                                            com.crushftp.client.Common.copyStreams(new FileInputStream(f), zaous, true, false);
                                                            zaous.closeArchiveEntry();
                                                            ++x;
                                                        }
                                                        zaous.finish();
                                                        zaous.close();
                                                    }
                                                }
                                                return true;
                                            }
                                            if (command.equalsIgnoreCase("setPreview")) {
                                                Common.urlDecodePost(request);
                                                if (com.crushftp.client.Common.dmz_mode) {
                                                    the_dir = request.getProperty("path");
                                                    if (!the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                                        the_dir = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + the_dir.substring(1);
                                                    }
                                                    item = this.thisSessionHTTP.thisSession.uVFS.get_item(the_dir);
                                                    config = new Properties();
                                                    config.put("protocol", "HTTP");
                                                    urlc = null;
                                                    loops = 0;
                                                    while (loops++ < 100) {
                                                        urlc = URLConnection.openConnection(new VRL(Common.getBaseUrl(item.getProperty("url"))), config);
                                                        urlc.setRequestMethod("POST");
                                                        c = this.thisSessionHTTP.thisSession.uVFS.getClient(item);
                                                        urlc.setRequestProperty("Cookie", "CrushAuth=" + c.getConfig("crushAuth") + ";");
                                                        urlc.setUseCaches(false);
                                                        urlc.setDoOutput(true);
                                                        b = ("command=setPreview&c2f=" + c.getConfig("crushAuth", "").substring(c.getConfig("crushAuth", "").length() - 4) + "&serverGroup_original=" + request.getProperty("serverGroup_original", "") + "&exif_key=" + request.getProperty("exif_key", "") + "&exif_val=" + com.crushftp.client.Common.xss_strip(request.getProperty("exif_val", "")) + "&form_id=" + request.getProperty("form_id", "") + "&type=" + request.getProperty("type", "") + "&object_type=" + request.getProperty("object_type", "") + "&serverGroup_backup=" + request.getProperty("serverGroup_backup", "") + "&instance=" + request.getProperty("instance", "") + "&serverGroup=" + request.getProperty("serverGroup", "") + "&path=" + Common.url_encode(request.getProperty("path"))).getBytes("UTF8");
                                                        urlc.setLength(b.length);
                                                        pout = urlc.getOutputStream();
                                                        pout.write(b);
                                                        pout.flush();
                                                        if (urlc.getResponseCode() == 302) {
                                                            c.setConfig("error", "Logged out.");
                                                            urlc.disconnect();
                                                            this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                                            Thread.sleep(100L);
                                                            continue;
                                                        }
                                                        this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                                        break;
                                                    }
                                                    baos = new ByteArrayOutputStream();
                                                    com.crushftp.client.Common.streamCopier(urlc.getInputStream(), baos, false, true, true);
                                                    urlc.disconnect();
                                                    return this.writeResponse(new String(baos.toByteArray()));
                                                }
                                                customForm = null;
                                                found_exif_key = false;
                                                if (request.getProperty("exif_key", "").startsWith(String.valueOf(System.getProperty("appname", "CrushFTP").toLowerCase()) + "_") && (customForms = (Vector)ServerStatus.server_settings.get("CustomForms")) != null) {
                                                    x = 0;
                                                    while (x < customForms.size()) {
                                                        p = (Properties)customForms.elementAt(x);
                                                        if (p.getProperty("name", "").equals(request.getProperty("form_id", ""))) {
                                                            customForm = p;
                                                            break;
                                                        }
                                                        ++x;
                                                    }
                                                    if (customForm != null) {
                                                        if (!customForm.containsKey("entries")) {
                                                            customForm.put("entries", new Vector<E>());
                                                        }
                                                        entries = (Vector)customForm.get("entries");
                                                        x = 0;
                                                        while (x < entries.size()) {
                                                            p = (Properties)entries.elementAt(x);
                                                            if (!p.getProperty("type").trim().equals("label")) {
                                                                if (p.getProperty("name").equals(request.getProperty("exif_key"))) {
                                                                    found_exif_key = true;
                                                                }
                                                                if (p.getProperty("value").indexOf("{user_name}") >= 0 && request.getProperty("exif_key").indexOf(String.valueOf(System.getProperty("appname", "CrushFTP").toLowerCase()) + "_locked_user") >= 0) {
                                                                    request.put("exif_val", com.crushftp.client.Common.xss_strip(ServerStatus.change_vars_to_values_static(p.getProperty("value", ""), this.thisSessionHTTP.thisSession.user, this.thisSessionHTTP.thisSession.user_info, this.thisSessionHTTP.thisSession)));
                                                                }
                                                            }
                                                            ++x;
                                                        }
                                                    }
                                                }
                                                error_message = "No preview converters found.";
                                                paths = request.getProperty("path").replace('>', '_').replace('<', '_').split(";");
                                                x = 0;
                                                while (x < paths.length) {
                                                    path = paths[x];
                                                    this.thisSessionHTTP.setupCurrentDir(path);
                                                    item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.pwd());
                                                    srcFile = com.crushftp.client.Common.dots(new VRL(item.getProperty("url")).getPath());
                                                    if (!this.thisSessionHTTP.thisSession.check_access_privs(path, "STOR")) {
                                                        error_message = "You need upload permissions to edit exif tags on a file:" + request.getProperty("path") + "\r\n";
                                                    }
                                                    if (request.getProperty("object_type", "image").equals("exif")) {
                                                        xx = 0;
                                                        while (xx < ServerStatus.thisObj.previewWorkers.size()) {
                                                            preview = (PreviewWorker)ServerStatus.thisObj.previewWorkers.elementAt(xx);
                                                            if (preview.prefs.getProperty("preview_enabled", "false").equalsIgnoreCase("true") && preview.checkExtension(Common.last(path), item) && !request.getProperty("exif_key").startsWith(String.valueOf(System.getProperty("appname", "CrushFTP").toLowerCase()) + "_")) {
                                                                preview.setExifInfo(srcFile, PreviewWorker.getDestPath2(String.valueOf(item.getProperty("url")) + "/p1/"), request.getProperty("exif_key"), com.crushftp.client.Common.xss_strip(request.getProperty("exif_val")));
                                                                error_message = "Success";
                                                                break;
                                                            }
                                                            ++xx;
                                                        }
                                                        if (found_exif_key) {
                                                            metaInfo = PreviewWorker.getMetaInfo(PreviewWorker.getDestPath2(String.valueOf(item.getProperty("url")) + "/p1/"));
                                                            if (metaInfo.getProperty(String.valueOf(System.getProperty("appname", "CrushFTP").toLowerCase()) + "_locked_user", "").equals("") || metaInfo.getProperty(String.valueOf(System.getProperty("appname", "CrushFTP").toLowerCase()) + "_locked_user", "").equalsIgnoreCase(this.thisSessionHTTP.thisSession.uiSG("user_name")) || this.thisSessionHTTP.thisSession.SG("site").indexOf("(CONNECT)") >= 0) {
                                                                metaInfo.put(request.getProperty("exif_key"), com.crushftp.client.Common.xss_strip(request.getProperty("exif_val")));
                                                                PreviewWorker.setMetaInfo(PreviewWorker.getDestPath2(String.valueOf(item.getProperty("url")) + "/p1/"), metaInfo);
                                                                error_message = "Success";
                                                                break;
                                                            }
                                                            error_message = "FAILURE: Item already locked.";
                                                            break;
                                                        }
                                                    }
                                                    if (!error_message.equals("Success")) break;
                                                    ++x;
                                                }
                                                response = "<commandResult><response>" + error_message + "</response></commandResult>";
                                                return this.writeResponse(response);
                                            }
                                            if (!command.equalsIgnoreCase("siteCommand")) break block911;
                                            Common.urlDecodePost(request);
                                            response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                            this.thisSessionHTTP.thisSession.uiPUT("the_command", "SITE");
                                            the_command_data = request.getProperty("siteCommand", "");
                                            this.thisSessionHTTP.thisSession.uiPUT("the_command_data", the_command_data);
                                            responseText = null;
                                            if (!the_command_data.equalsIgnoreCase("BLOCK_UPLOADS") && !the_command_data.startsWith("ABOR")) break block912;
                                            responseText = "blocked";
                                            if (SharedSessionReplicated.send_queues.size() > 0) {
                                                pp = new Properties();
                                                pp.put("the_command_data", the_command_data);
                                                pp.put("CrushAuth", this.thisSessionHTTP.thisSession.getId());
                                                SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.server.ServerSessionAjax.doFileAbortBlock", "info", pp);
                                            }
                                            this.thisSessionHTTP.thisSession.doFileAbortBlock(the_command_data, true);
                                            break block889;
                                        }
                                        c = this.thisSessionHTTP.thisSession.uVFS.getClient(this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir")));
                                        try {
                                            block888: {
                                                block913: {
                                                    vrl = new VRL(this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir")).getProperty("url"));
                                                    responseText = c.doCommand("SITE " + the_command_data);
                                                    if (responseText != null) break block913;
                                                    activeUpload = (Properties)com.crushftp.client.Common.System2.get("crushftp.activeUpload.info" + this.thisSessionHTTP.thisSession.getId());
                                                    keys = activeUpload.keys();
                                                    responseText = "";
                                                    if (true) ** GOTO lbl1951
                                                    while (true) {
                                                        key = keys.nextElement().toString();
                                                        val = activeUpload.remove(key).toString();
                                                        responseText = String.valueOf(responseText) + key + ":" + val + "\r\n";
lbl1951:
                                                        // 2 sources

                                                        if (keys.hasMoreElements()) {
                                                            continue;
                                                        }
                                                        break block888;
                                                        break;
                                                    }
                                                }
                                                responseText = responseText.substring(4);
                                            }
                                            this.thisSessionHTTP.thisSession.uVFS.reset();
                                        }
                                        finally {
                                            c = this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                        }
                                    }
                                    response = String.valueOf(response) + "<commandResult><response>" + responseText + "</response></commandResult>";
                                    return this.writeResponse(response);
                                }
                                if (command.equalsIgnoreCase("sitePlugin") && site.toUpperCase().indexOf("(SITE_PLUGIN)") >= 0) {
                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                    this.thisSessionHTTP.thisSession.uiPUT("the_command", "SITE");
                                    the_command_data = request.getProperty("siteCommand", "");
                                    this.thisSessionHTTP.thisSession.uiPUT("the_command_data", the_command_data);
                                    fileItem = (Properties)request.clone();
                                    fileItem.put("url", "ftp://127.0.0.1:56789/");
                                    fileItem.put("the_file_path", request.getProperty("the_file_path", "/"));
                                    fileItem.put("the_file_size", "1");
                                    fileItem.put("event_name", request.getProperty("event"));
                                    fileItem.put("the_file_name", request.getProperty("the_file_name", "none"));
                                    this.thisSessionHTTP.thisSession.do_event5("SITE", fileItem);
                                    responseText = fileItem.getProperty("execute_log", "No Result");
                                    response = String.valueOf(response) + "<commandResult><response>" + responseText + "</response></commandResult>";
                                    return this.writeResponse(response);
                                }
                                if (command.equalsIgnoreCase("getUploadStatus")) {
                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                    activeUpload = (Properties)com.crushftp.client.Common.System2.get("crushftp.activeUpload.info" + this.thisSessionHTTP.thisSession.getId());
                                    responseText = "";
                                    if (activeUpload != null && (responseText = activeUpload.getProperty(request.getProperty("itemName"))) != null) {
                                        if (responseText.toUpperCase().startsWith("DONE:")) {
                                            activeUpload.remove(request.getProperty("itemName"));
                                        } else if (!responseText.toUpperCase().startsWith("PROGRESS:") && !responseText.startsWith("ERROR:")) {
                                            responseText = "ERROR:" + responseText;
                                        }
                                    }
                                    response = String.valueOf(response) + "<commandResult><response>" + responseText + "</response></commandResult>";
                                    return this.writeResponse(response);
                                }
                                if (command.equalsIgnoreCase("getTime")) {
                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                    response = String.valueOf(response) + "<commandResult><response>" + System.currentTimeMillis() + "</response></commandResult>";
                                    return this.writeResponse(response);
                                }
                                if (command.equalsIgnoreCase("lookup_form_field")) {
                                    return this.writeResponse(this.lookupFormField(request), true);
                                }
                                if (command.equalsIgnoreCase("batchComplete")) {
                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                    response = String.valueOf(response) + "<commandResult><response>SUCCESS</response></commandResult>";
                                    this.thisSessionHTTP.thisSession.do_event5("BATCH_COMPLETE", null);
                                    return this.writeResponse(response);
                                }
                                if (command.equalsIgnoreCase("decrypt")) {
                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                    names = Common.url_decode(request.getProperty("names")).replace('>', '_').replace('<', '_').split("\r\n");
                                    error_message = "";
                                    x = 0;
                                    while (x < names.length) {
                                        block890: {
                                            try {
                                                the_dir = names[x];
                                                if (!the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                                    the_dir = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + the_dir.substring(1);
                                                }
                                                this.thisSessionHTTP.cd(the_dir);
                                                if (!this.thisSessionHTTP.thisSession.check_access_privs(this.thisSessionHTTP.pwd(), "RETR") || !this.thisSessionHTTP.thisSession.check_access_privs(this.thisSessionHTTP.pwd(), "STOR")) {
                                                    error_message = String.valueOf(error_message) + "ERROR:You need download, and upload permissions to decrypt a file:" + the_dir + "\r\n";
                                                    break block890;
                                                }
                                                this.thisSessionHTTP.thisSession.uiPUT("the_command", "DECRYPT");
                                                this.thisSessionHTTP.thisSession.uiPUT("the_command_data", this.thisSessionHTTP.pwd());
                                                item = this.thisSessionHTTP.thisSession.uVFS.get_item(the_dir);
                                                vrl = new VRL(item.getProperty("url"));
                                                c1 = this.thisSessionHTTP.thisSession.uVFS.getClient(item);
                                                c2 = this.thisSessionHTTP.thisSession.uVFS.getClient(item);
                                                try {
                                                    in = c1.download(vrl.getPath(), 0L, -1L, true);
                                                    if (!this.thisSessionHTTP.thisSession.user.getProperty("fileEncryptionKey", "").equals("") || !this.thisSessionHTTP.thisSession.user.getProperty("fileDecryptionKey", "").equals("")) {
                                                        in = Common.getDecryptedStream(in, this.thisSessionHTTP.thisSession.user.getProperty("fileEncryptionKey", ""), this.thisSessionHTTP.thisSession.user.getProperty("fileDecryptionKey", ""), this.thisSessionHTTP.thisSession.user.getProperty("fileDecryptionKeyPass", ""));
                                                    } else if (!ServerStatus.SG("fileEncryptionKey").equals("") || ServerStatus.BG("fileDecryption")) {
                                                        in = Common.getDecryptedStream(in, ServerStatus.SG("fileEncryptionKey"), ServerStatus.SG("fileDecryptionKey"), ServerStatus.SG("fileDecryptionKeyPass"));
                                                    }
                                                    out = c2.upload(String.valueOf(vrl.getPath()) + ".decrypting", 0L, true, true);
                                                    com.crushftp.client.Common.copyStreams(in, out, true, true);
                                                    c1.rename(String.valueOf(vrl.getPath()) + ".decrypting", vrl.getPath(), true);
                                                    error_message = String.valueOf(error_message) + the_dir + " decrypted.";
                                                }
                                                finally {
                                                    c1 = this.thisSessionHTTP.thisSession.uVFS.releaseClient(c1);
                                                    c2 = this.thisSessionHTTP.thisSession.uVFS.releaseClient(c2);
                                                }
                                            }
                                            catch (Exception e) {
                                                Log.log("HTTP_SERVER", 0, e);
                                                error_message = String.valueOf(error_message) + "ERROR:" + e.getMessage() + "\r\n";
                                            }
                                        }
                                        ++x;
                                    }
                                    response = String.valueOf(response) + "<commandResult><response>" + error_message + "</response></commandResult>";
                                    this.thisSessionHTTP.thisSession.uVFS.reset();
                                    return this.writeResponse(response.replace('%', ' '));
                                }
                                if (command.equalsIgnoreCase("encrypt")) {
                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                    names = Common.url_decode(request.getProperty("names")).replace('>', '_').replace('<', '_').split("\r\n");
                                    error_message = "";
                                    x = 0;
                                    while (x < names.length) {
                                        block892: {
                                            the_dir = names[x];
                                            if (!the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                                the_dir = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + the_dir.substring(1);
                                            }
                                            this.thisSessionHTTP.cd(the_dir);
                                            if (!this.thisSessionHTTP.thisSession.check_access_privs(this.thisSessionHTTP.pwd(), "RETR") || !this.thisSessionHTTP.thisSession.check_access_privs(this.thisSessionHTTP.pwd(), "STOR")) {
                                                error_message = String.valueOf(error_message) + "ERROR:You need download, and upload permissions to encrypt a file:" + the_dir + "\r\n";
                                            } else {
                                                this.thisSessionHTTP.thisSession.uiPUT("the_command", "DECRYPT");
                                                this.thisSessionHTTP.thisSession.uiPUT("the_command_data", this.thisSessionHTTP.pwd());
                                                item = this.thisSessionHTTP.thisSession.uVFS.get_item(the_dir);
                                                vrl = new VRL(item.getProperty("url"));
                                                c1 = this.thisSessionHTTP.thisSession.uVFS.getClient(item);
                                                c2 = this.thisSessionHTTP.thisSession.uVFS.getClient(item);
                                                try {
                                                    try {
                                                        in = new BufferedInputStream(c1.download(vrl.getPath(), 0L, -1L, true));
                                                        in.mark(2000);
                                                        b = new byte[500];
                                                        totalBytes = 0;
                                                        bytesRead = 0;
                                                        baos = new ByteArrayOutputStream();
                                                        while (bytesRead >= 0 && totalBytes < 1000) {
                                                            bytesRead = in.read(b);
                                                            if (bytesRead < 0) continue;
                                                            baos.write(b, 0, bytesRead);
                                                            totalBytes += bytesRead;
                                                        }
                                                        in.reset();
                                                        s = new String(baos.toByteArray(), "UTF8");
                                                        if (s.indexOf("-----BEGIN PGP MESSAGE-----") < 0 || s.indexOf(String.valueOf(System.getProperty("appname", "CrushFTP").toUpperCase()) + "#") < 0) {
                                                            out = c2.upload(String.valueOf(vrl.getPath()) + ".encrypting", 0L, true, true);
                                                            if (!this.thisSessionHTTP.thisSession.user.getProperty("filePublicEncryptionKey", "").equals("")) {
                                                                out = !this.thisSessionHTTP.thisSession.user.getProperty("encryption_cypher", "").equals("") ? Common.getEncryptedStream(out, this.thisSessionHTTP.thisSession.user.getProperty("filePublicEncryptionKey", ""), 0L, false, c2, vrl.getPath(), this.thisSessionHTTP.thisSession.user.getProperty("encryption_cypher", "")) : Common.getEncryptedStream(out, this.thisSessionHTTP.thisSession.user.getProperty("filePublicEncryptionKey", ""), 0L, false, c2, vrl.getPath());
                                                            } else if (ServerStatus.BG("fileEncryption") && !ServerStatus.SG("filePublicEncryptionKey").equals("")) {
                                                                out = Common.getEncryptedStream(out, ServerStatus.SG("filePublicEncryptionKey"), 0L, ServerStatus.BG("file_encrypt_ascii"), c2, vrl.getPath());
                                                            } else if (!this.thisSessionHTTP.thisSession.user.getProperty("fileEncryptionKey", "").equals("")) {
                                                                out = !this.thisSessionHTTP.thisSession.user.getProperty("encryption_cypher", "").equals("") ? Common.getEncryptedStream(out, this.thisSessionHTTP.thisSession.user.getProperty("fileEncryptionKey", ""), 0L, false, c2, vrl.getPath(), this.thisSessionHTTP.thisSession.user.getProperty("encryption_cypher", "")) : Common.getEncryptedStream(out, this.thisSessionHTTP.thisSession.user.getProperty("fileEncryptionKey", ""), 0L, false, c2, vrl.getPath());
                                                            } else if (ServerStatus.BG("fileEncryption")) {
                                                                out = Common.getEncryptedStream(out, ServerStatus.SG("fileEncryptionKey"), 0L, false, c2, vrl.getPath());
                                                            }
                                                            com.crushftp.client.Common.copyStreams(in, out, true, true);
                                                            c1.rename(String.valueOf(vrl.getPath()) + ".encrypting", vrl.getPath(), true);
                                                            error_message = String.valueOf(error_message) + the_dir + " encrypted.";
                                                        } else {
                                                            Log.log("HTTP_SERVER", 0, "Ignoring encryption request, already encrypted:" + the_dir);
                                                            error_message = String.valueOf(error_message) + the_dir + " : Ignoring encryption request, already encrypted.";
                                                            in.close();
                                                        }
                                                    }
                                                    catch (Exception e) {
                                                        Log.log("HTTP_SERVER", 0, e);
                                                        error_message = String.valueOf(error_message) + "ERROR:" + e.getMessage() + "\r\n";
                                                        c1 = this.thisSessionHTTP.thisSession.uVFS.releaseClient(c1);
                                                        c2 = this.thisSessionHTTP.thisSession.uVFS.releaseClient(c2);
                                                        break block892;
                                                    }
                                                }
                                                catch (Throwable var24_673) {
                                                    c1 = this.thisSessionHTTP.thisSession.uVFS.releaseClient(c1);
                                                    c2 = this.thisSessionHTTP.thisSession.uVFS.releaseClient(c2);
                                                    throw var24_673;
                                                }
                                                c1 = this.thisSessionHTTP.thisSession.uVFS.releaseClient(c1);
                                                c2 = this.thisSessionHTTP.thisSession.uVFS.releaseClient(c2);
                                            }
                                        }
                                        ++x;
                                    }
                                    response = String.valueOf(response) + "<commandResult><response>" + error_message + "</response></commandResult>";
                                    this.thisSessionHTTP.thisSession.uVFS.reset();
                                    return this.writeResponse(response.replace('%', ' '));
                                }
                                if (command.equalsIgnoreCase("unzip")) {
                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                    names = Common.url_decode(request.getProperty("names")).replace('>', '_').replace('<', '_').split("\r\n");
                                    error_message = "";
                                    x = 0;
                                    while (x < names.length) {
                                        the_dir = names[x].replace('>', '_').replace('<', '_');
                                        if (the_dir.toUpperCase().endsWith(".ZIP")) {
                                            if (!the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                                the_dir = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + the_dir.substring(1);
                                            }
                                            this.thisSessionHTTP.cd(the_dir);
                                            if (!(this.thisSessionHTTP.thisSession.check_access_privs(this.thisSessionHTTP.pwd(), "RETR") && this.thisSessionHTTP.thisSession.check_access_privs(this.thisSessionHTTP.pwd(), "STOR") && this.thisSessionHTTP.thisSession.check_access_privs(Common.all_but_last(this.thisSessionHTTP.pwd()), "MKD"))) {
                                                error_message = String.valueOf(error_message) + "You need download, upload, and make directory permissions to unzip a file:" + the_dir + "\r\n";
                                            } else {
                                                this.thisSessionHTTP.thisSession.uiPUT("the_command", "UNZIP");
                                                this.thisSessionHTTP.thisSession.uiPUT("the_command_data", this.thisSessionHTTP.pwd());
                                                item = this.thisSessionHTTP.thisSession.uVFS.get_item(the_dir);
                                                vrl = new VRL(item.getProperty("url"));
                                                c = this.thisSessionHTTP.thisSession.uVFS.getClient(item);
                                                try {
                                                    Common.unzip(vrl.getPath(), c, this.thisSessionHTTP.thisSession, Common.all_but_last(the_dir));
                                                }
                                                finally {
                                                    c = this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                                }
                                            }
                                        }
                                        ++x;
                                    }
                                    response = String.valueOf(response) + "<commandResult><response>" + error_message + "</response></commandResult>";
                                    this.thisSessionHTTP.thisSession.uVFS.reset();
                                    return this.writeResponse(response.replace('%', ' '));
                                }
                                if (command.equalsIgnoreCase("zip")) {
                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                    names = Common.url_decode(request.getProperty("names")).replace('>', '_').replace('<', '_').split("\r\n");
                                    error_message = "";
                                    zipFiles = new Vector<E>();
                                    firstItemName = new StringBuffer();
                                    x = 0;
                                    while (x < names.length) {
                                        the_dir = names[x];
                                        if (!the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                            the_dir = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + the_dir.substring(1);
                                        }
                                        this.thisSessionHTTP.cd(the_dir);
                                        if (!this.thisSessionHTTP.thisSession.check_access_privs(this.thisSessionHTTP.pwd(), "RETR") || !this.thisSessionHTTP.thisSession.check_access_privs(this.thisSessionHTTP.pwd(), "STOR")) {
                                            error_message = String.valueOf(error_message) + "You need download, upload permissions to zip a file:" + the_dir + "\r\n";
                                        } else {
                                            this.thisSessionHTTP.thisSession.uiPUT("the_command", "ZIP");
                                            this.thisSessionHTTP.thisSession.uiPUT("the_command_data", this.thisSessionHTTP.pwd());
                                            item = this.thisSessionHTTP.thisSession.uVFS.get_item(the_dir);
                                            if (firstItemName.length() == 0) {
                                                firstItemName.append(item.getProperty("name"));
                                            }
                                            this.thisSessionHTTP.thisSession.uVFS.getListing(zipFiles, the_dir, 999, 50000, true);
                                        }
                                        ++x;
                                    }
                                    the_dir = Common.url_decode(request.getProperty("path"));
                                    if (!the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                        the_dir = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + the_dir.substring(1);
                                    }
                                    if (!this.thisSessionHTTP.thisSession.check_access_privs(the_dir, "STOR")) {
                                        error_message = String.valueOf(error_message) + "You need upload permissions to zip a file:" + request.getProperty("path") + "\r\n";
                                    } else {
                                        error_message = String.valueOf(error_message) + "Started zipping...\r\n";
                                        outputItem = this.thisSessionHTTP.thisSession.uVFS.get_item(the_dir);
                                        root_dir = new VRL(outputItem.getProperty("url")).getPath();
                                        Worker.startWorker(new Runnable(){

                                            @Override
                                            public void run() {
                                                String zipName = String.valueOf(firstItemName.toString()) + "_" + Common.makeBoundary(3);
                                                try {
                                                    Common.zip(root_dir, zipFiles, String.valueOf(root_dir) + zipName + ".zipping");
                                                    new File_U(String.valueOf(root_dir) + zipName + ".zipping").renameTo(new File_U(String.valueOf(root_dir) + zipName + ".zip"));
                                                }
                                                catch (Exception e) {
                                                    Common.debug(0, e);
                                                    new File_U(String.valueOf(root_dir) + zipName + ".zipping").renameTo(new File_U(String.valueOf(root_dir) + zipName + ".bad"));
                                                }
                                            }
                                        }, "Zipping:" + the_dir + ":" + request.getProperty("names"));
                                    }
                                    response = String.valueOf(response) + "<commandResult><response>" + error_message + "</response></commandResult>";
                                    this.thisSessionHTTP.thisSession.uVFS.reset();
                                    return this.writeResponse(response.replace('%', ' '));
                                }
                                if (command.equalsIgnoreCase("delete")) {
                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                    names = Common.url_decode(request.getProperty("names")).replace('>', '_').replace('<', '_').split("\r\n");
                                    error_message = "";
                                    x = 0;
                                    while (x < names.length) {
                                        the_dir = names[x];
                                        if (the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                            the_dir = the_dir.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
                                        }
                                        this.thisSessionHTTP.cd(the_dir);
                                        this.thisSessionHTTP.thisSession.uiPUT("the_command", "DELE");
                                        this.thisSessionHTTP.thisSession.uiPUT("the_command_data", this.thisSessionHTTP.pwd());
                                        lastMessage = "";
                                        xx = 0;
                                        while (xx < 10) {
                                            lastMessage = this.thisSessionHTTP.thisSession.do_DELE(true, the_dir);
                                            if (lastMessage.equals("%DELE-not found%")) {
                                                lastMessage = "";
                                            }
                                            if (lastMessage.indexOf("%DELE-error%") < 0) break;
                                            Thread.sleep(1000L);
                                            ++xx;
                                        }
                                        error_message = String.valueOf(error_message) + lastMessage;
                                        ++x;
                                    }
                                    response = String.valueOf(response) + "<commandResult><response>" + LOC.G(error_message) + "</response></commandResult>";
                                    this.thisSessionHTTP.thisSession.uVFS.reset();
                                    return this.writeResponse(response.replace('%', ' '));
                                }
                                if (command.equalsIgnoreCase("rename")) {
                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                    the_dir = Common.url_decode(request.getProperty("path"));
                                    if (the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                        the_dir = the_dir.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
                                    }
                                    this.thisSessionHTTP.cd(the_dir);
                                    item_name = Common.url_decode(request.getProperty("name1", ""));
                                    this.thisSessionHTTP.thisSession.uiPUT("the_command", "RNFR");
                                    this.thisSessionHTTP.thisSession.uiPUT("the_command_data", item_name);
                                    error_message = this.thisSessionHTTP.thisSession.do_RNFR(String.valueOf(the_dir) + item_name);
                                    if (error_message.equals("") || error_message.equals("%RNFR-bad%")) {
                                        try {
                                            fromPath = SearchHandler.getPreviewPath(this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.rnfr_file_path).getProperty("url"), "1", 1);
                                            item_name2 = Common.url_decode(request.getProperty("name2", ""));
                                            this.thisSessionHTTP.thisSession.uiPUT("the_command", "RNTO");
                                            this.thisSessionHTTP.thisSession.uiPUT("the_command_data", item_name2);
                                            if (!item_name.equals(item_name2)) {
                                                if (!item_name2.contains("/")) {
                                                    item_name2 = String.valueOf(the_dir) + item_name2;
                                                }
                                                if (!item_name2.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                                    item_name2 = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + item_name2;
                                                }
                                                error_message = String.valueOf(error_message) + this.thisSessionHTTP.thisSession.do_RNTO(request.getProperty("overwrite", "false").equals("true"), this.thisSessionHTTP.thisSession.rnfr_file_path, item_name2);
                                                if (error_message.equals("") && fromPath != null && (toPath = SearchHandler.getPreviewPath((rnto = this.thisSessionHTTP.thisSession.uVFS.get_item(item_name2)).getProperty("url"), "1", 1)) != null) {
                                                    new File_U(String.valueOf(ServerStatus.SG("previews_path")) + Common.all_but_last(Common.all_but_last(fromPath)).substring(1)).renameTo(new File_U(String.valueOf(ServerStatus.SG("previews_path")) + Common.all_but_last(Common.all_but_last(toPath)).substring(1)));
                                                }
                                                this.thisSessionHTTP.thisSession.uVFS.reset();
                                            }
                                        }
                                        catch (Exception e) {
                                            Log.log("HTTP_SERVER", 1, e);
                                            error_message = String.valueOf(error_message) + "ERROR:" + e.getMessage() + "\r\n";
                                        }
                                    }
                                    response = String.valueOf(response) + "<commandResult><response>" + LOC.G(error_message) + "</response></commandResult>";
                                    this.thisSessionHTTP.thisSession.uVFS.reset();
                                    return this.writeResponse(response.replace('%', ' '));
                                }
                                if (command.equalsIgnoreCase("makedir")) {
                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                    the_dir = Common.url_decode(request.getProperty("path")).trim().replace(':', '_');
                                    if (the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                        the_dir = the_dir.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
                                    }
                                    this.thisSessionHTTP.thisSession.uiPUT("the_command", "MKD");
                                    this.thisSessionHTTP.thisSession.uiPUT("the_command_data", the_dir);
                                    error_message = this.thisSessionHTTP.thisSession.do_MKD(true, the_dir);
                                    this.thisSessionHTTP.thisSession.uVFS.reset();
                                    if (error_message.indexOf("%MKD-exists%") >= 0) {
                                        error_message = "";
                                    }
                                    response = String.valueOf(response) + "<commandResult><response>" + LOC.G(error_message) + "</response></commandResult>";
                                    return this.writeResponse(response.replace('%', ' '));
                                }
                                if (command.equalsIgnoreCase("stat")) {
                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                    the_dir = Common.url_decode(request.getProperty("path"));
                                    if (the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                        the_dir = the_dir.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
                                    }
                                    this.thisSessionHTTP.thisSession.uiPUT("the_command", "STAT");
                                    this.thisSessionHTTP.thisSession.uiPUT("the_command_data", the_dir);
                                    item = null;
                                    x = 0;
                                    while (x < 5 && item == null) {
                                        if (this.thisSessionHTTP.thisSession.uVFS.get_item(Common.all_but_last(String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + the_dir)) != null) {
                                            item = this.thisSessionHTTP.thisSession.uVFS.get_item(String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + the_dir);
                                            break;
                                        }
                                        Log.log("SERVER", 2, "Path not found, (" + x + ") retrying:" + the_dir);
                                        Thread.sleep(1000L);
                                        ++x;
                                    }
                                    item_str = new StringBuffer();
                                    if (request.getProperty("calcFolder", "").equals("true")) {
                                        listing = new Vector<E>();
                                        size = 0L;
                                        this.thisSessionHTTP.thisSession.uVFS.getListing(listing, String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + the_dir, 20, 10000, true, null);
                                        x = 0;
                                        while (x < listing.size()) {
                                            p = (Properties)listing.elementAt(x);
                                            size += Long.parseLong(p.getProperty("size", "0"));
                                            ++x;
                                        }
                                        item.put("size", String.valueOf(size));
                                    }
                                    if (request.getProperty("format", "").equalsIgnoreCase("stat_dmz")) {
                                        if (item != null) {
                                            item_str.append(AgentUI.formatDmzStat(item));
                                        }
                                    } else if (item != null && LIST_handler.checkName(item, this.thisSessionHTTP.thisSession, true, true)) {
                                        LIST_handler.generateLineEntry(item, item_str, false, the_dir, true, this.thisSessionHTTP.thisSession, false);
                                    }
                                    response = String.valueOf(response) + "<commandResult><response>" + item_str.toString().trim() + "</response></commandResult>";
                                    return this.writeResponse(response);
                                }
                                if (command.equalsIgnoreCase("exists")) {
                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                    item_str = new StringBuffer();
                                    the_dirs = Common.url_decode(request.getProperty("paths")).split(";");
                                    x = 0;
                                    while (x < the_dirs.length) {
                                        the_dir = the_dirs[x];
                                        if (the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                            the_dir = the_dir.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
                                        }
                                        this.thisSessionHTTP.thisSession.uiPUT("the_command", "STAT");
                                        this.thisSessionHTTP.thisSession.uiPUT("the_command_data", the_dir);
                                        item = this.thisSessionHTTP.thisSession.uVFS.get_item(String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + the_dir);
                                        item_str.append(the_dirs[x]).append(":").append(String.valueOf(item != null)).append("\r\n");
                                        ++x;
                                    }
                                    response = String.valueOf(response) + "<commandResult><response>" + item_str.toString().trim() + "</response></commandResult>";
                                    return this.writeResponse(response);
                                }
                                if (command.equalsIgnoreCase("mdtm")) {
                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                    the_dir = Common.url_decode(request.getProperty("path"));
                                    the_file = Common.normalize2(Common.last(the_dir));
                                    x = 0;
                                    while (x < ServerStatus.SG("unsafe_filename_chars").length()) {
                                        the_file = the_file.replace(ServerStatus.SG("unsafe_filename_chars").charAt(x), '_');
                                        ++x;
                                    }
                                    if ((the_dir = String.valueOf(Common.all_but_last(the_dir)) + the_file).startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                        the_dir = the_dir.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
                                    }
                                    this.thisSessionHTTP.thisSession.uiPUT("the_command", "MDTM");
                                    this.thisSessionHTTP.thisSession.uiPUT("the_command_data", String.valueOf(the_dir) + " " + request.getProperty("date"));
                                    error_message = this.thisSessionHTTP.thisSession.do_MDTM();
                                    this.thisSessionHTTP.thisSession.uVFS.reset();
                                    response = String.valueOf(response) + "<commandResult><response>" + LOC.G(error_message) + "</response></commandResult>";
                                    return this.writeResponse(response.replace('%', ' '));
                                }
                                if (command.equalsIgnoreCase("publish") || command.equalsIgnoreCase("publish_attach")) {
                                    response = "";
                                    if (command.equalsIgnoreCase("publish_attach") && ServerStatus.siIG("enterprise_level") <= 0) {
                                        response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                        response = String.valueOf(response) + "<commandResult><response>";
                                        response = String.valueOf(response) + "The server does not have an enterprise license, so sharing from email is not allowed.\r\n<br/>";
                                        response = String.valueOf(response) + "</response></commandResult>";
                                    } else if (com.crushftp.client.Common.dmz_mode) {
                                        queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                        action = new Properties();
                                        action.put("type", "PUT:SHARE");
                                        action.put("id", Common.makeBoundary());
                                        action.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                        root_item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir"));
                                        c = this.thisSessionHTTP.thisSession.uVFS.getClient(root_item);
                                        action.put("crushAuth", c.getConfig("crushAuth"));
                                        this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                        action.put("user_ip", this.thisSessionHTTP.thisSession.uiSG("user_ip"));
                                        action.put("request", request);
                                        action.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
                                        action.put("need_response", "true");
                                        queue.addElement(action);
                                        action = UserTools.waitResponse(action, 300);
                                        response = action.remove("object_response").toString();
                                    } else {
                                        if (command.equalsIgnoreCase("publish_attach")) {
                                            request.put("emailTo", "");
                                            tempUsername = Common.url_decode(request.getProperty("temp_username", ""));
                                            tempPassword = Common.url_decode(request.getProperty("temp_password", ""));
                                            if (tempUsername.equals("")) {
                                                tempUsername = Common.makeBoundary(ServerStatus.IG("temp_accounts_length"));
                                                tempPassword = Common.makeBoundary(ServerStatus.IG("temp_accounts_length"));
                                            }
                                            tmp_home = String.valueOf(ServerStatus.SG("temp_accounts_path")) + "storage/" + tempUsername + tempPassword + "/";
                                            new File_U(tmp_home).mkdirs();
                                            request.put("temp_username", tempUsername);
                                            request.put("temp_password", tempPassword);
                                            fname = request.getProperty("paths");
                                            if (fname.startsWith("/")) {
                                                fname = fname.substring(1);
                                            }
                                            if (fname.endsWith("/")) {
                                                fname = fname.substring(0, fname.length() - 1);
                                            }
                                            permissions = this.thisSessionHTTP.thisSession.uVFS.getPermission0();
                                            permissions.put(String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + request.getProperty("paths").substring(1).toUpperCase(), "(read)(write)(makedir)(deletedir)(view)(delete)(resume)(share)(invisible)");
                                            dir_item = new Properties();
                                            dir_item.put("url", new File_U(tmp_home).toURI().toURL().toExternalForm());
                                            dir_item.put("type", "file");
                                            v = new Vector<Properties>();
                                            v.addElement(dir_item);
                                            virtual = (Properties)this.thisSessionHTTP.thisSession.uVFS.homes.elementAt(0);
                                            path = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + fname + "/";
                                            vItem = new Properties();
                                            vItem.put("virtualPath", path);
                                            vItem.put("name", fname);
                                            vItem.put("type", "FILE");
                                            vItem.put("vItems", v);
                                            virtual.put(path.substring(0, path.length() - 1), vItem);
                                        }
                                        path_items = new Vector<Properties>();
                                        paths = null;
                                        paths = request.getProperty("paths").indexOf(";") >= 0 ? Common.url_decode(request.getProperty("paths")).split(";") : Common.url_decode(request.getProperty("paths")).split("\r\n");
                                        x = 0;
                                        while (x < paths.length) {
                                            the_dir = paths[x].trim();
                                            if (!the_dir.equals("")) {
                                                if (the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                                    the_dir = the_dir.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
                                                }
                                                this.thisSessionHTTP.setupCurrentDir(the_dir);
                                                Log.log("HTTP_SERVER", 2, "Sharing:" + the_dir + "  vs.  " + this.thisSessionHTTP.pwd());
                                                item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.pwd());
                                                Log.log("HTTP_SERVER", 2, "Sharing:" + item);
                                                if (item == null) {
                                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                                    response = String.valueOf(response) + "<commandResult><response>";
                                                    response = String.valueOf(response) + "<username></username>";
                                                    response = String.valueOf(response) + "<password></password>";
                                                    response = String.valueOf(response) + "<message>ERROR : One or more paths cannot be found:" + the_dir + "</message>";
                                                    response = String.valueOf(response) + "<url></url>";
                                                    response = String.valueOf(response) + "<error_response>ERROR : One or more paths cannot be found:" + the_dir + "</error_response>";
                                                    response = String.valueOf(response) + "</response></commandResult>";
                                                    return this.writeResponse(response);
                                                }
                                                if (!item.getProperty("privs", "").contains("(share)")) {
                                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                                    response = String.valueOf(response) + "<commandResult><response>";
                                                    response = String.valueOf(response) + "<username></username>";
                                                    response = String.valueOf(response) + "<password></password>";
                                                    response = String.valueOf(response) + "<message>ERROR : Permission denied! Path: " + the_dir + "</message>";
                                                    response = String.valueOf(response) + "<url></url>";
                                                    response = String.valueOf(response) + "<error_response>ERROR : Permission denied! Path: " + the_dir + "</error_response>";
                                                    response = String.valueOf(response) + "</response></commandResult>";
                                                    return this.writeResponse(response);
                                                }
                                                vrl = new VRL(item.getProperty("url"));
                                                stat = null;
                                                if (!vrl.getProtocol().equalsIgnoreCase("VIRTUAL")) {
                                                    c = this.thisSessionHTTP.thisSession.uVFS.getClient(item);
                                                    try {
                                                        c.login(vrl.getUsername(), vrl.getPassword(), null);
                                                        stat = c.stat(vrl.getPath());
                                                    }
                                                    finally {
                                                        c = this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                                    }
                                                    stat.put("privs", item.getProperty("privs"));
                                                    stat.put("root_dir", item.getProperty("root_dir"));
                                                    path_items.addElement(stat);
                                                } else {
                                                    list = new Vector<E>();
                                                    this.thisSessionHTTP.thisSession.uVFS.getListing(list, this.thisSessionHTTP.pwd(), 1, 9999, true);
                                                    has_subitem = false;
                                                    xx = 0;
                                                    while (xx < list.size()) {
                                                        sub_list = (Properties)list.elementAt(xx);
                                                        if (sub_list.getProperty("root_dir", "").equals(this.thisSessionHTTP.pwd())) {
                                                            sub_list = this.thisSessionHTTP.thisSession.uVFS.get_item(String.valueOf(this.thisSessionHTTP.pwd()) + sub_list.getProperty("name", ""));
                                                            if (sub_list != null && !new VRL(sub_list.getProperty("url")).getProtocol().equalsIgnoreCase("VIRTUAL")) {
                                                                sub_list.put("privs", item.getProperty("privs"));
                                                                sub_list.put("root_dir", item.getProperty("root_dir"));
                                                                has_subitem = true;
                                                                path_items.addElement(sub_list);
                                                            }
                                                        }
                                                        ++xx;
                                                    }
                                                    if (!has_subitem) {
                                                        response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                                        response = String.valueOf(response) + "<commandResult><response>";
                                                        response = String.valueOf(response) + "<username></username>";
                                                        response = String.valueOf(response) + "<password></password>";
                                                        response = String.valueOf(response) + "<message>ERROR : Empty virtual folder cannot be shared!</message>";
                                                        response = String.valueOf(response) + "<url></url>";
                                                        response = String.valueOf(response) + "<error_response>ERROR : Empty virtual folder</error_response>";
                                                        response = String.valueOf(response) + "</response></commandResult>";
                                                        return this.writeResponse(response);
                                                    }
                                                }
                                            }
                                            ++x;
                                        }
                                        request.put("emailBody", Common.replace_str(request.getProperty("emailBody", ""), "&amp;", "&"));
                                        request.put("emailBody", Common.replace_str(request.getProperty("emailBody", ""), "%26amp%3B", "&"));
                                        user_name = this.thisSessionHTTP.thisSession.uiSG("user_name");
                                        response = ServerSessionAJAX.createShare(path_items, request, (Vector)this.thisSessionHTTP.thisSession.user.get("web_customizations"), user_name, this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer"), this.thisSessionHTTP.thisSession.user, this.thisSessionHTTP.thisSession.date_time, this.thisSessionHTTP.thisSession, command.equalsIgnoreCase("publish_attach"));
                                        if (request.containsKey("keywords")) {
                                            request.put("names", request.getProperty("paths"));
                                            ServerSessionAJAX.processKeywordsEdit(request, this.thisSessionHTTP.thisSession);
                                        }
                                    }
                                    return this.writeResponse(response);
                                }
                                if (command.equalsIgnoreCase("customEvent")) {
                                    response = "";
                                    if (com.crushftp.client.Common.dmz_mode) {
                                        queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                        action = new Properties();
                                        action.put("type", "PUT:CUSTOM");
                                        action.put("id", Common.makeBoundary());
                                        root_item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir"));
                                        c = this.thisSessionHTTP.thisSession.uVFS.getClient(root_item);
                                        action.put("crushAuth", c.getConfig("crushAuth"));
                                        this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                        action.put("user_ip", this.thisSessionHTTP.thisSession.uiSG("user_ip"));
                                        action.put("request", request);
                                        action.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
                                        action.put("need_response", "true");
                                        queue.addElement(action);
                                        action = UserTools.waitResponse(action, 300);
                                        response = action.remove("object_response").toString();
                                    } else {
                                        request = Common.urlDecodePost(request);
                                        path_items = new Vector<Properties>();
                                        paths = null;
                                        paths = request.getProperty("paths").indexOf("|") >= 0 ? request.getProperty("paths").split("\\|") : (request.getProperty("paths").indexOf(";") >= 0 ? request.getProperty("paths").split(";") : Common.url_decode(request.getProperty("paths")).split("\r\n"));
                                        short_folder = paths[0];
                                        x = 0;
                                        while (x < paths.length) {
                                            the_dir = paths[x].trim();
                                            if (!the_dir.equals("")) {
                                                if (the_dir.length() < short_folder.length()) {
                                                    short_folder = paths[x];
                                                }
                                            }
                                            ++x;
                                        }
                                        short_folder = Common.all_but_last(short_folder);
                                        x = 0;
                                        while (x < paths.length) {
                                            the_dir = paths[x].trim();
                                            if (!the_dir.equals("")) {
                                                if (the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                                    the_dir = the_dir.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
                                                }
                                                this.thisSessionHTTP.setupCurrentDir(the_dir);
                                                Log.log("HTTP_SERVER", 2, "Custom:" + the_dir + "  vs.  " + this.thisSessionHTTP.pwd());
                                                item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.pwd());
                                                Log.log("HTTP_SERVER", 2, "Custom:" + item);
                                                try {
                                                    item.put("root_dir", item.getProperty("root_dir").substring((String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + short_folder).length() - 2));
                                                }
                                                catch (Exception e) {
                                                    Log.log("SERVER", 2, e);
                                                    Log.log("SERVER", 2, "Short_folder:" + short_folder);
                                                    Log.log("SERVER", 2, "root_dir:" + item.getProperty("root_dir"));
                                                    Log.log("SERVER", 2, "session root_dir:" + this.thisSessionHTTP.thisSession.SG("root_dir"));
                                                }
                                                path_items.addElement(item);
                                            }
                                            ++x;
                                        }
                                        response = ServerSessionAJAX.createCustom(path_items, request, this.thisSessionHTTP.thisSession);
                                    }
                                    return this.writeResponse(response, request.getProperty("json", "false").equals("true"));
                                }
                                if (command.equalsIgnoreCase("problemEvent")) {
                                    response = "";
                                    if (com.crushftp.client.Common.dmz_mode) {
                                        queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                        action = new Properties();
                                        action.put("type", "PUT:PROBLEM");
                                        action.put("id", Common.makeBoundary());
                                        root_item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir"));
                                        c = this.thisSessionHTTP.thisSession.uVFS.getClient(root_item);
                                        action.put("crushAuth", c.getConfig("crushAuth"));
                                        this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                        action.put("user_ip", this.thisSessionHTTP.thisSession.uiSG("user_ip"));
                                        action.put("request", request);
                                        action.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
                                        action.put("need_response", "true");
                                        queue.addElement(action);
                                        action = UserTools.waitResponse(action, 300);
                                        response = action.remove("object_response").toString();
                                    } else {
                                        request = Common.urlDecodePost(request);
                                        path_items = new Vector<Properties>();
                                        paths = Common.url_decode(request.getProperty("path")).split("\r\n");
                                        short_folder = paths[0];
                                        x = 0;
                                        while (x < paths.length) {
                                            the_dir = paths[x].trim();
                                            if (!the_dir.equals("")) {
                                                if (the_dir.length() < short_folder.length()) {
                                                    short_folder = paths[x];
                                                }
                                            }
                                            ++x;
                                        }
                                        short_folder = Common.all_but_last(short_folder);
                                        x = 0;
                                        while (x < paths.length) {
                                            the_dir = paths[x].trim();
                                            if (!the_dir.equals("")) {
                                                if (the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                                    the_dir = the_dir.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
                                                }
                                                this.thisSessionHTTP.setupCurrentDir(the_dir);
                                                Log.log("HTTP_SERVER", 2, "ProblemEvent:" + the_dir + "  vs.  " + this.thisSessionHTTP.pwd());
                                                item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.pwd());
                                                Log.log("HTTP_SERVER", 2, "ProblemEvent:" + item);
                                                try {
                                                    item.put("root_dir", item.getProperty("root_dir").substring((String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + short_folder).length() - 2));
                                                }
                                                catch (Exception e) {
                                                    Log.log("SERVER", 2, e);
                                                    Log.log("SERVER", 2, "Short_folder:" + short_folder);
                                                    Log.log("SERVER", 2, "root_dir:" + item.getProperty("root_dir"));
                                                    Log.log("SERVER", 2, "session root_dir:" + this.thisSessionHTTP.thisSession.SG("root_dir"));
                                                }
                                                path_items.addElement(item);
                                            }
                                            ++x;
                                        }
                                        response = ServerSessionAJAX.createProblem(path_items, request, this.thisSessionHTTP.thisSession);
                                    }
                                    return this.writeResponse(response, request.getProperty("json", "false").equals("true"));
                                }
                                if (command.equalsIgnoreCase("cut_paste") || command.equalsIgnoreCase("copy_paste")) {
                                    if (this.thisSessionHTTP.thisSession.get("paste_ids") == null) {
                                        this.thisSessionHTTP.thisSession.put("paste_ids", new Properties());
                                    }
                                    paste_ids = (Properties)this.thisSessionHTTP.thisSession.get("paste_ids");
                                    paste_uid = Common.makeBoundary();
                                    status = new StringBuffer().append("Starting...");
                                    paste_ids.put(paste_uid, status);
                                    names = Common.url_decode(request.getProperty("names")).replace('>', '_').replace('<', '_').split("\r\n");
                                    destPath = Common.url_decode(request.getProperty("destPath")).replace('>', '_').replace('<', '_');
                                    command2 = command;
                                    r = new Runnable(){

                                        @Override
                                        public void run() {
                                            SessionCrush.doPaste(ServerSessionAJAX.this.thisSessionHTTP.thisSession, status, names, destPath, command2);
                                        }
                                    };
                                    if (request.getProperty("single_thread", "").equals("true")) {
                                        r.run();
                                    } else {
                                        Worker.startWorker(r);
                                    }
                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                    response = String.valueOf(response) + "<commandResult><response>";
                                    response = String.valueOf(response) + paste_uid;
                                    response = String.valueOf(response) + "</response></commandResult>";
                                    return this.writeResponse(response.replace('%', ' '));
                                }
                                if (command.equalsIgnoreCase("paste_status")) {
                                    paste_ids = (Properties)this.thisSessionHTTP.thisSession.get("paste_ids");
                                    status = (StringBuffer)paste_ids.get(paste_uid = String.valueOf(request.getProperty("id")));
                                    if (status == null) {
                                        status = new StringBuffer();
                                    }
                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                    destPath = status;
                                    synchronized (destPath) {
                                        if (status.toString().indexOf("ERROR:") >= 0 || status.toString().indexOf("COMPLETED:") >= 0) {
                                            paste_ids.remove(paste_uid);
                                        }
                                        response = String.valueOf(response) + "<commandResult><response>";
                                        response = String.valueOf(response) + status.toString();
                                        response = String.valueOf(response) + "</response></commandResult>";
                                    }
                                    return this.writeResponse(response.replace('%', ' '));
                                }
                                if (command.equalsIgnoreCase("paste_cancel")) {
                                    paste_ids = (Properties)this.thisSessionHTTP.thisSession.get("paste_ids");
                                    status = (StringBuffer)paste_ids.get(paste_uid = String.valueOf(request.getProperty("id")));
                                    if (status == null) {
                                        status = new StringBuffer();
                                    }
                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                    destPath = status;
                                    synchronized (destPath) {
                                        status.setLength(0);
                                        status.append("CANCELLED");
                                        response = String.valueOf(response) + "<commandResult><response>";
                                        response = String.valueOf(response) + status.toString();
                                        response = String.valueOf(response) + "</response></commandResult>";
                                    }
                                    return this.writeResponse(response.replace('%', ' '));
                                }
                                if (command.equalsIgnoreCase("editKeywords")) {
                                    response = "";
                                    if (com.crushftp.client.Common.dmz_mode) {
                                        queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                        action = new Properties();
                                        action.put("type", "PUT:EDIT_KEYWORDS");
                                        action.put("id", Common.makeBoundary());
                                        root_item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir"));
                                        c = this.thisSessionHTTP.thisSession.uVFS.getClient(root_item);
                                        action.put("crushAuth", c.getConfig("crushAuth"));
                                        this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                        action.put("request", request);
                                        action.put("need_response", "true");
                                        queue.addElement(action);
                                        action = UserTools.waitResponse(action, 300);
                                        response = action.remove("object_response").toString();
                                    } else {
                                        response = ServerSessionAJAX.processKeywordsEdit(request, this.thisSessionHTTP.thisSession);
                                    }
                                    return this.writeResponse(response.replace('%', ' '));
                                }
                                if (command.equalsIgnoreCase("getKeywords")) {
                                    the_dir = Common.url_decode(request.getProperty("path").trim()).trim();
                                    if (the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                        the_dir = the_dir.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
                                    }
                                    this.thisSessionHTTP.setupCurrentDir(the_dir);
                                    item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.pwd());
                                    the_dir = SearchHandler.getPreviewPath(item.getProperty("url"), "1", 1);
                                    index = String.valueOf(ServerStatus.SG("previews_path")) + the_dir.substring(1);
                                    if (!new File_S(Common.all_but_last(index)).exists()) {
                                        new File_S(Common.all_but_last(index)).mkdirs();
                                    }
                                    baos = new ByteArrayOutputStream();
                                    if (new File_S(String.valueOf(Common.all_but_last(Common.all_but_last(index))) + "index.txt").exists()) {
                                        com.crushftp.client.Common.streamCopier(new FileInputStream(new File_S(String.valueOf(Common.all_but_last(Common.all_but_last(index))) + "index.txt")), baos, false, true, true);
                                    }
                                    this.write_command_http("HTTP/1.1 200 OK");
                                    this.write_command_http("Cache-Control: no-store");
                                    this.write_command_http("Content-Type: text/plain");
                                    this.thisSessionHTTP.write_standard_headers();
                                    utf8 = com.crushftp.client.Common.xss_strip(new String(baos.toByteArray(), "utf8")).getBytes("utf8");
                                    this.write_command_http("Content-Length: " + utf8.length);
                                    this.write_command_http("Content-Disposition: attachment; filename=\"" + item.getProperty("name", "index") + "_keywords.txt\"");
                                    this.write_command_http("X-UA-Compatible: chrome=1");
                                    this.write_command_http("");
                                    this.thisSessionHTTP.original_os.write(utf8);
                                    this.thisSessionHTTP.original_os.flush();
                                    return true;
                                }
                                if (command.equalsIgnoreCase("search")) {
                                    info = "";
                                    if (com.crushftp.client.Common.dmz_mode) {
                                        queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                        action = new Properties();
                                        action.put("type", "GET:SEARCH");
                                        action.put("id", Common.makeBoundary());
                                        root_item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir"));
                                        c = this.thisSessionHTTP.thisSession.uVFS.getClient(root_item);
                                        action.put("crushAuth", c.getConfig("crushAuth"));
                                        this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                        action.put("request", request);
                                        action.put("need_response", "true");
                                        queue.addElement(action);
                                        action = UserTools.waitResponse(action, 900);
                                        info = (String)action.remove("object_response");
                                        if (info == null) {
                                            info = "";
                                        }
                                        if (info.equals("")) {
                                            info = "{\r\n";
                                            info = String.valueOf(info) + "\t\"privs\" : \"\",\r\n";
                                            info = String.valueOf(info) + "\t\"path\" : \"\",\r\n";
                                            info = String.valueOf(info) + "\t\"defaultStrings\" : \"\",\r\n";
                                            info = String.valueOf(info) + "\t\"site\" : \"\",\r\n";
                                            info = String.valueOf(info) + "\t\"quota\" : \"\",\r\n";
                                            info = String.valueOf(info) + "\t\"quota_bytes\" : \"\",\r\n";
                                            info = String.valueOf(info) + "\t\"listing\" : []\r\n";
                                            info = String.valueOf(info) + "}\r\n";
                                        }
                                    } else {
                                        info = ServerSessionAJAX.search(request, this.thisSessionHTTP.thisSession);
                                    }
                                    ok = this.writeResponse(info, false, 200, true, true, true);
                                    this.thisSessionHTTP.thisSession.put("search_status", "0/1");
                                    return ok;
                                }
                                if (command.equalsIgnoreCase("getSearchStatus")) {
                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                    if (com.crushftp.client.Common.dmz_mode) {
                                        queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                        action = new Properties();
                                        action.put("type", "GET:SEARCH_STATUS");
                                        action.put("id", Common.makeBoundary());
                                        root_item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir"));
                                        c = this.thisSessionHTTP.thisSession.uVFS.getClient(root_item);
                                        action.put("crushAuth", c.getConfig("crushAuth"));
                                        this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                        action.put("need_response", "true");
                                        queue.addElement(action);
                                        action = UserTools.waitResponse(action, 300);
                                        info = action.remove("object_response").toString();
                                        response = String.valueOf(response) + "<commandResult><response>" + info + "</response></commandResult>";
                                    } else {
                                        response = String.valueOf(response) + "<commandResult><response>" + this.thisSessionHTTP.thisSession.uiSG("search_status").trim() + "</response></commandResult>";
                                        this.thisSessionHTTP.thisSession.uVFS.reset();
                                    }
                                    return this.writeResponse(response);
                                }
                                if (command.equalsIgnoreCase("getTunnels")) {
                                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                                    userTunnels = String.valueOf(this.thisSessionHTTP.thisSession.user.getProperty("tunnels", "").trim()) + ",";
                                    tunnels = (Vector)ServerStatus.VG("tunnels").clone();
                                    tunnels.addAll(ServerStatus.VG("tunnels_dmz"));
                                    baot = new ByteArrayOutputStream();
                                    lesserSpeed = this.thisSessionHTTP.thisSession.IG("speed_limit_upload");
                                    if (lesserSpeed > 0 && lesserSpeed > ServerStatus.IG("max_server_upload_speed") && ServerStatus.IG("max_server_upload_speed") > 0 || lesserSpeed == 0) {
                                        lesserSpeed = ServerStatus.IG("max_server_upload_speed");
                                    }
                                    x = 0;
                                    while (x < tunnels.size()) {
                                        baot2 = new ByteArrayOutputStream();
                                        pp = (Properties)tunnels.elementAt(x);
                                        if (userTunnels.indexOf(String.valueOf(pp.getProperty("id")) + ",") >= 0 && !pp.getProperty("tunnelType", "HTTP").equals("SSH")) {
                                            pp.put("max_speed", String.valueOf(lesserSpeed));
                                            pp.store(baot2, "");
                                            s = Common.url_encode(new String(baot2.toByteArray(), "UTF8"));
                                            baot.write(s.getBytes("UTF8"));
                                            baot.write(";;;".getBytes());
                                        }
                                        ++x;
                                    }
                                    tunnelsStr = new String(baot.toByteArray(), "UTF8").replace('%', '~');
                                    response = String.valueOf(response) + "<commandResult><response>" + tunnelsStr + "</response></commandResult>";
                                    this.thisSessionHTTP.thisSession.uVFS.reset();
                                    return this.writeResponse(response);
                                }
                                if (!command.startsWith("downloadAsZip")) break block914;
                                this.thisSessionHTTP.thisSession.uiPUT("the_command", "RETR");
                                this.thisSessionHTTP.done = true;
                                this.write_command_http("HTTP/1.1 200 OK");
                                validSecs = 30;
                                this.write_command_http("Cache-Control: post-check=" + validSecs + ",pre-check=" + validSecs * 10);
                                this.write_command_http("Content-Type: application/zip");
                                this.thisSessionHTTP.write_standard_headers();
                                paths = Common.url_decode(request.getProperty("paths", ""));
                                if (paths.startsWith(":")) {
                                    paths = paths.substring(1);
                                }
                                itemList = paths.split(":");
                                current_dir = request.getProperty("path", "/");
                                if (!request.getProperty("path_shortening", "true").equals("true")) break block915;
                                commonStartPath = "";
                                x = 0;
                                if (true) ** GOTO lbl2897
                                while (true) {
                                    file_path = itemList[x];
                                    if (file_path.split("\\/").length > commonStartPath.split("\\/").length) {
                                        commonStartPath = file_path;
                                    }
                                    ++x;
lbl2897:
                                    // 2 sources

                                    if (x >= itemList.length) ** GOTO lbl2910
                                }
lbl-1000:
                                // 1 sources

                                {
                                    commonStartPath = Common.all_but_last(commonStartPath);
                                    ok = true;
                                    x = 0;
                                    while (x < itemList.length && ok) {
                                        file_path = itemList[x];
                                        if (!file_path.equals("")) {
                                            if (!file_path.startsWith(commonStartPath)) {
                                                ok = false;
                                            }
                                        }
                                        ++x;
                                    }
                                    if (ok) break;
lbl2910:
                                    // 2 sources

                                    ** while (!commonStartPath.equals((Object)""))
                                }
lbl2911:
                                // 2 sources

                                if ((current_dir = commonStartPath).equals("")) {
                                    current_dir = "/";
                                }
                            }
                            fname = "archive.zip";
                            if (itemList.length == 1) {
                                fname = String.valueOf(itemList[0]) + ".zip";
                            }
                            if (fname.indexOf("/") >= 0) {
                                fname = Common.last(itemList[0]);
                                if (fname.endsWith("/")) {
                                    fname = fname.substring(0, fname.length() - 1);
                                }
                                fname = String.valueOf(fname) + ".zip";
                            }
                            if (request.getProperty("zipName", "").length() > 0) {
                                fname = request.getProperty("zipName", "");
                            }
                            fname = Common.replace_str(Common.url_decode(fname), "\r", "_");
                            this.write_command_http("Content-Disposition: attachment; filename=\"" + (this.thisSessionHTTP.thisSession.uiSG("header_user-agent").toUpperCase().indexOf("MSIE") >= 0 || this.thisSessionHTTP.thisSession.uiSG("header_user-agent").toUpperCase().indexOf("TRIDENT") >= 0 ? Common.url_encode(fname) : fname) + "\"");
                            this.write_command_http("X-UA-Compatible: chrome=1");
                            this.write_command_http("Connection: close");
                            this.write_command_http("");
                            if (com.crushftp.client.Common.dmz_mode) {
                                root_loc = itemList[itemList.length - 1];
                                if (!root_loc.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                    root_loc = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + root_loc.substring(1);
                                }
                                item = this.thisSessionHTTP.thisSession.uVFS.get_item(root_loc);
                                c = this.thisSessionHTTP.thisSession.uVFS.getClient(item);
                                try {
                                    in = ((HTTPClient)c).downloadAsZip(current_dir, paths, request.getProperty("path_shortening", "true").equals("true"));
                                    try {
                                        b = new byte[32768];
                                        bytesRead = 0;
                                        while (bytesRead >= 0) {
                                            bytesRead = in.read(b);
                                            if (bytesRead <= 0) continue;
                                            this.thisSessionHTTP.original_os.write(b, 0, bytesRead);
                                            this.thisSessionHTTP.thisSession.active_transfer();
                                        }
                                        this.thisSessionHTTP.original_os.flush();
                                    }
                                    finally {
                                        in.close();
                                        this.thisSessionHTTP.original_os.close();
                                    }
                                }
                                catch (Exception e) {
                                    Log.log("HTTP_SERVER", 1, e);
                                }
                                c.close();
                                this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                return true;
                            }
                            if (!current_dir.toUpperCase().startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                current_dir = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + current_dir.substring(1);
                            }
                            item = this.thisSessionHTTP.thisSession.uVFS.get_item("/");
                            if (item == null) {
                                item = new Properties();
                            }
                            sizeLookup = new Properties();
                            if (request.getProperty("filters", "").trim().length() > 0) {
                                filters = Common.url_decode(request.getProperty("filters", "")).split("\r\n");
                                x = 0;
                                while (x < filters.length) {
                                    name = filters[x].split(":")[0];
                                    size = filters[x].split(":")[1];
                                    sizeLookup.put(name, size);
                                    ++x;
                                }
                            }
                            loc = 0;
                            activeThreads = new Vector<E>();
                            while (loc < itemList.length) {
                                if (itemList[loc].length() > 0) {
                                    itemList[loc] = Common.dots(itemList[loc]);
                                    current_dir2 = itemList[loc];
                                    if (!current_dir2.toUpperCase().startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                        current_dir2 = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + current_dir2.substring(1);
                                    }
                                    this.thisSessionHTTP.cd(current_dir2);
                                    item2 = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.pwd());
                                    if (item2 != null && this.thisSessionHTTP.thisSession.check_access_privs(this.thisSessionHTTP.pwd(), "RETR")) {
                                        v2 = sizeLookup.size() <= 0 && (new VRL(item2.getProperty("url")).getProtocol().equalsIgnoreCase("file") || new VRL(item2.getProperty("url")).getProtocol().equalsIgnoreCase("smb")) ? false : (singleThread = true);
                                        if (item2.getProperty("type").equalsIgnoreCase("FILE")) {
                                            this.thisSessionHTTP.retr.zipFiles.addElement(item2);
                                        } else {
                                            Common.startMultiThreadZipper(this.thisSessionHTTP.thisSession.uVFS, this.thisSessionHTTP.retr, this.thisSessionHTTP.pwd(), 0, singleThread, activeThreads);
                                        }
                                    }
                                }
                                if (activeThreads.size() >= 10) {
                                    Thread.sleep(100L);
                                }
                                ++loc;
                            }
                            x = this.thisSessionHTTP.retr.zipFiles.size() - 1;
                            while (x >= 0 && sizeLookup.size() > 0) {
                                zitem = (Properties)this.thisSessionHTTP.retr.zipFiles.elementAt(x);
                                root_dir = zitem.getProperty("root_dir");
                                if (root_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                    root_dir = root_dir.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
                                }
                                size = sizeLookup.getProperty(String.valueOf(root_dir) + zitem.getProperty("name"));
                                if (size != null) {
                                    if (Long.parseLong(size) == Long.parseLong(zitem.getProperty("size"))) {
                                        this.thisSessionHTTP.retr.zipFiles.removeElementAt(x);
                                    } else if (Long.parseLong(size) < Long.parseLong(zitem.getProperty("size"))) {
                                        zitem.put("rest", size);
                                    }
                                }
                                --x;
                            }
                            otherFile = new VRL(item.getProperty("url"));
                            this.thisSessionHTTP.cd(current_dir);
                            this.thisSessionHTTP.thisSession.uiPUT("file_transfer_mode", "BINARY");
                            this.thisSessionHTTP.retr.data_os = this.thisSessionHTTP.original_os;
                            this.thisSessionHTTP.retr.httpDownload = true;
                            this.thisSessionHTTP.retr.zipping = true;
                            this.thisSessionHTTP.thisSession.uiPUT("no_zip_compression", request.getProperty("no_zip_compression", "false"));
                            this.thisSessionHTTP.thisSession.uiPUT("zip64", request.getProperty("zip64", "false"));
                            the_dir = this.thisSessionHTTP.pwd();
                            pp = new Properties();
                            pp.put("the_dir", the_dir);
                            this.thisSessionHTTP.thisSession.runPlugin("transfer_path", pp);
                            the_dir = pp.getProperty("the_dir", the_dir);
                            this.thisSessionHTTP.retr.init_vars(the_dir, this.thisSessionHTTP.thisSession.uiLG("start_resume_loc"), -1L, this.thisSessionHTTP.thisSession, item, false, "", otherFile, null);
                            this.thisSessionHTTP.retr.runOnce = true;
                            this.thisSessionHTTP.retr.run();
                            return true;
                        }
                        if (command.equals("getXMLListing")) {
                            the_dir = com.crushftp.client.Common.dots(Common.url_decode(request.getProperty("path", "")));
                            if (the_dir.equals("/")) {
                                the_dir = this.thisSessionHTTP.thisSession.SG("root_dir");
                            }
                            if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.thisSessionHTTP.thisSession.SG("root_dir").toUpperCase())) {
                                the_dir = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + the_dir.substring(1);
                            }
                            this.thisSessionHTTP.cd(the_dir);
                            response = "";
                            try {
                                response = this.getXmlListingResponse(this.thisSessionHTTP.thisSession.uiSG("user_name"), request, the_dir, false, this.thisSessionHTTP.thisSession.uVFS);
                            }
                            catch (Exception e) {
                                this.thisSessionHTTP.thisSession.add_log("[" + this.thisSessionHTTP.thisSession.uiSG("user_number") + ":" + this.thisSessionHTTP.thisSession.uiSG("user_name") + ":" + this.thisSessionHTTP.thisSession.uiSG("user_ip") + "] ERROR: " + the_dir + ": " + e, "HTTP");
                                Log.log("SERVER", 0, e);
                                throw e;
                            }
                            return this.writeResponse(response, false, 200, false, request.getProperty("format", "").equalsIgnoreCase("JSONOBJ"), true);
                        }
                        if (command.equals("getHistory")) {
                            response = "";
                            if (com.crushftp.client.Common.dmz_mode) {
                                queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                action = new Properties();
                                action.put("type", "PUT:GETHISTORY");
                                action.put("id", Common.makeBoundary());
                                action.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                root_item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir"));
                                c = this.thisSessionHTTP.thisSession.uVFS.getClient(root_item);
                                action.put("crushAuth", c.getConfig("crushAuth"));
                                this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                action.put("user_ip", this.thisSessionHTTP.thisSession.uiSG("user_ip"));
                                action.put("request", request);
                                action.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
                                action.put("need_response", "true");
                                queue.addElement(action);
                                action = UserTools.waitResponse(action, 300);
                                response = action.remove("object_response").toString();
                            } else {
                                response = ServerSessionAJAX.getHistory(request, this.thisSessionHTTP.thisSession);
                            }
                            return this.writeResponse(response, false, 200, true, true, true);
                        }
                        if (command.equals("manageShares")) {
                            response = "";
                            if (com.crushftp.client.Common.dmz_mode) {
                                queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                action = new Properties();
                                action.put("type", "PUT:MANAGESHARES");
                                action.put("id", Common.makeBoundary());
                                action.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                root_item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir"));
                                c = this.thisSessionHTTP.thisSession.uVFS.getClient(root_item);
                                action.put("crushAuth", c.getConfig("crushAuth"));
                                this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                action.put("user_ip", this.thisSessionHTTP.thisSession.uiSG("user_ip"));
                                action.put("request", request);
                                action.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
                                action.put("need_response", "true");
                                queue.addElement(action);
                                action = UserTools.waitResponse(action, 300);
                                response = action.remove("object_response").toString();
                            } else {
                                response = ServerSessionAJAX.manageShares(this.thisSessionHTTP.thisSession);
                            }
                            return this.writeResponse(response);
                        }
                        if (command.equals("deleteTempAccount")) {
                            response = "";
                            if (com.crushftp.client.Common.dmz_mode) {
                                queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                action = new Properties();
                                action.put("type", "PUT:DELETESHARE");
                                action.put("id", Common.makeBoundary());
                                action.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                root_item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir"));
                                c = this.thisSessionHTTP.thisSession.uVFS.getClient(root_item);
                                action.put("crushAuth", c.getConfig("crushAuth"));
                                this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                action.put("user_ip", this.thisSessionHTTP.thisSession.uiSG("user_ip"));
                                action.put("request", request);
                                action.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
                                action.put("need_response", "true");
                                queue.addElement(action);
                                action = UserTools.waitResponse(action, 300);
                                response = action.remove("object_response").toString();
                            } else {
                                response = ServerSessionAJAX.deleteShare(request, this.thisSessionHTTP.thisSession);
                            }
                            return this.writeResponse(response);
                        }
                        if (command.equals("editTempAccount")) {
                            response = "";
                            if (com.crushftp.client.Common.dmz_mode) {
                                queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                action = new Properties();
                                action.put("type", "PUT:EDITSHARE");
                                action.put("id", Common.makeBoundary());
                                action.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                root_item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir"));
                                c = this.thisSessionHTTP.thisSession.uVFS.getClient(root_item);
                                action.put("crushAuth", c.getConfig("crushAuth"));
                                this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                action.put("user_ip", this.thisSessionHTTP.thisSession.uiSG("user_ip"));
                                action.put("request", request);
                                action.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
                                action.put("need_response", "true");
                                queue.addElement(action);
                                action = UserTools.waitResponse(action, 300);
                                response = action.remove("object_response").toString();
                            } else {
                                response = ServerSessionAJAX.editShare(request, this.thisSessionHTTP.thisSession);
                            }
                            return this.writeResponse(response);
                        }
                        if (!command.equals("getCustomForm")) break block916;
                        response = "";
                        name = this.thisSessionHTTP.thisSession.SG(request.getProperty("form"));
                        if (name.indexOf(":") < 0 && !name.equals("messageForm")) {
                            name = String.valueOf(name) + ":" + name;
                        }
                        if (!this.thisSessionHTTP.thisSession.getProperty(request.getProperty("form"), "").equals("")) {
                            name = this.thisSessionHTTP.thisSession.getProperty(request.getProperty("form"), "");
                        }
                        if (name == null || name.length() <= 0 || name.indexOf(":") <= 0) break block917;
                        try {
                            name = name.indexOf(":") == name.lastIndexOf(":") ? name.substring(name.lastIndexOf(":") + 1).trim() : name.substring(name.lastIndexOf(":", name.lastIndexOf(":") - 1) + 1, name.lastIndexOf(":")).trim();
                            customForms = (Vector)ServerStatus.server_settings.get("CustomForms");
                            x = 0;
                            if (true) ** GOTO lbl3215
                            while (true) {
                                form = (Properties)com.crushftp.client.Common.CLONE((Properties)customForms.elementAt(x));
                                form.put("always", String.valueOf(!this.thisSessionHTTP.thisSession.SG(request.getProperty("form")).endsWith(":once")));
                                if (form.getProperty("name").equalsIgnoreCase(name)) {
                                    entries = (Vector)form.get("entries");
                                    xx = 0;
                                    while (xx < entries.size()) {
                                        entry = (Properties)entries.elementAt(xx);
                                        entry.put("item_type", entry.getProperty("type"));
                                        if (this.thisSessionHTTP.thisSession.containsKey(entry.getProperty("name", "label").trim())) {
                                            entry.put("value", Common.url_decode(this.thisSessionHTTP.thisSession.getProperty(entry.getProperty("name").trim())));
                                        }
                                        entry.put("value", ServerStatus.change_vars_to_values_static(entry.getProperty("value", ""), this.thisSessionHTTP.thisSession.user, this.thisSessionHTTP.thisSession.user_info, this.thisSessionHTTP.thisSession));
                                        entry.put("label", ServerStatus.change_vars_to_values_static(entry.getProperty("label", ""), this.thisSessionHTTP.thisSession.user, this.thisSessionHTTP.thisSession.user_info, this.thisSessionHTTP.thisSession));
                                        ++xx;
                                    }
                                    try {
                                        ServerStatus.thisObj.common_code;
                                        response = Common.getXMLString(form, "customForm", null);
                                    }
                                    catch (Exception e) {
                                        Log.log("HTTP_SERVER", 1, e);
                                    }
                                }
                                ++x;
lbl3215:
                                // 2 sources

                                if (x < customForms.size()) {
                                    continue;
                                }
                                break;
                            }
                        }
                        catch (Exception e) {
                            Log.log("HTTP_SERVER", 1, e);
                        }
                    }
                    return this.writeResponse(response);
                }
                if (command.equals("getUserInfo")) {
                    return this.writeResponse(this.getUserInfo(request, site));
                }
                if (command.equals("changePassword")) {
                    response = "";
                    if (com.crushftp.client.Common.dmz_mode) {
                        queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                        action = new Properties();
                        action.put("type", "PUT:CHANGE_PASSWORD");
                        action.put("id", Common.makeBoundary());
                        try {
                            root_item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir"));
                            c = this.thisSessionHTTP.thisSession.uVFS.getClient(root_item);
                            action.put("crushAuth", c.getConfig("crushAuth"));
                            this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 1, e);
                        }
                        action.put("request", request);
                        action.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                        action.put("current_password", request.getProperty("current_password"));
                        action.put("site", site);
                        action.put("need_response", "true");
                        queue.addElement(action);
                        action = UserTools.waitResponse(action, 30);
                        response = action.remove("object_response").toString();
                    } else {
                        response = ServerSessionAJAX.changePassword(request, site, this.thisSessionHTTP.thisSession);
                    }
                    return this.writeResponse(response);
                }
                if (command.equals("changePhone")) {
                    response = "";
                    if (com.crushftp.client.Common.dmz_mode) {
                        queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                        action = new Properties();
                        action.put("type", "PUT:CHANGE_PHONE");
                        action.put("id", Common.makeBoundary());
                        try {
                            root_item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir"));
                            c = this.thisSessionHTTP.thisSession.uVFS.getClient(root_item);
                            action.put("crushAuth", c.getConfig("crushAuth"));
                            this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 1, e);
                        }
                        action.put("request", request);
                        action.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                        action.put("site", site);
                        action.put("need_response", "true");
                        queue.addElement(action);
                        action = UserTools.waitResponse(action, 30);
                        response = action.remove("object_response").toString();
                    } else {
                        response = this.thisSessionHTTP.thisSession.change_phone_number(request.getProperty("phone", ""));
                    }
                    return this.writeResponse(response);
                }
                if (request.getProperty("command", "").equalsIgnoreCase("agentRegister")) {
                    result = "";
                    if (com.crushftp.client.Common.dmz_mode) {
                        queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                        action = new Properties();
                        action.put("type", "PUT:AGENT_REGISTER");
                        action.put("id", Common.makeBoundary());
                        action.put("request", request);
                        action.put("need_response", "true");
                        queue.addElement(action);
                        action = UserTools.waitResponse(action, 60);
                        if (action != null) {
                            result = action.getProperty("response", "");
                        }
                    } else {
                        result = AdminControls.registerAgent(request, true);
                    }
                    if (result.equals("")) {
                        this.write_command_http("HTTP/1.1 200 OK");
                        this.thisSessionHTTP.write_standard_headers();
                        this.write_command_http("Connection: close");
                        this.write_command_http("Content-Length: 0");
                        this.write_command_http("");
                    } else {
                        this.write_command_http("HTTP/1.1 500 Internal Server Error");
                        this.write_command_http("Cache-Control: no-store");
                        this.write_command_http("Content-Type: application/binary");
                        this.thisSessionHTTP.write_standard_headers();
                        this.write_command_http("Content-Length: " + result.length() + 2);
                        this.write_command_http("");
                        this.write_command_http(result);
                    }
                    return true;
                }
                if (request.getProperty("command", "").equalsIgnoreCase("agentQueue")) {
                    result = new Properties();
                    if (com.crushftp.client.Common.dmz_mode) {
                        queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                        action = new Properties();
                        action.put("type", "GET:AGENT_QUEUE");
                        action.put("id", Common.makeBoundary());
                        action.put("request", request);
                        action.put("need_response", "true");
                        queue.addElement(action);
                        action = UserTools.waitResponse(action, 60);
                        if (action != null) {
                            try {
                                result = (Properties)action.get("response");
                            }
                            catch (Exception e) {
                                Log.log("HTTP_SERVER", 1, e);
                            }
                        }
                    } else {
                        result = AdminControls.getActionFromAgentQueue(request, true);
                    }
                    baos = new ByteArrayOutputStream();
                    oos = new ObjectOutputStream(baos);
                    oos.writeObject(result);
                    oos.close();
                    b64 = Base64.encodeBytes(baos.toByteArray());
                    this.write_command_http("HTTP/1.1 200 OK");
                    this.thisSessionHTTP.write_standard_headers();
                    this.write_command_http("Connection: close");
                    this.write_command_http("Content-Length: " + b64.length());
                    this.write_command_http("");
                    this.thisSessionHTTP.original_os.write(b64.getBytes());
                    return true;
                }
                if (request.getProperty("command", "").equalsIgnoreCase("agentResponse")) {
                    result = new Properties();
                    if (com.crushftp.client.Common.dmz_mode) {
                        queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                        action = new Properties();
                        action.put("type", "GET:AGENT_RESPONSE");
                        action.put("id", Common.makeBoundary());
                        action.put("request", request);
                        action.put("need_response", "true");
                        queue.addElement(action);
                        action = UserTools.waitResponse(action, 60);
                        if (action != null) {
                            try {
                                result = (Properties)action.get("response");
                            }
                            catch (Exception e) {
                                Log.log("HTTP_SERVER", 1, e);
                            }
                        }
                    } else {
                        result = AdminControls.getAgentResponse(request, true);
                    }
                    baos = new ByteArrayOutputStream();
                    oos = new ObjectOutputStream(baos);
                    oos.writeObject(result);
                    oos.close();
                    b64 = Base64.encodeBytes(baos.toByteArray());
                    this.write_command_http("HTTP/1.1 200 OK");
                    this.thisSessionHTTP.write_standard_headers();
                    this.write_command_http("Connection: close");
                    this.write_command_http("Content-Length: " + b64.length());
                    this.write_command_http("");
                    this.thisSessionHTTP.original_os.write(b64.getBytes());
                    return true;
                }
                if (request.getProperty("command", "").equalsIgnoreCase("agentList")) {
                    block896: {
                        response = "";
                        try {
                            if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                                ServerStatus.thisObj.common_code;
                                response = Common.getXMLString(AdminControls.getAgentList(request), "agents", null);
                                break block896;
                            }
                            return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                        }
                        catch (Exception e) {
                            Log.log("HTTP_SERVER", 1, e);
                            response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                            response = String.valueOf(response) + "<commandResult><response>Error: " + e + "</response></commandResult>";
                        }
                    }
                    return this.writeResponse(response);
                }
                if ((site.indexOf("(CONNECT)") >= 0 || site.indexOf("(JOB_EDIT)") >= 0 || site.indexOf("(JOB_VIEW)") >= 0 || site.indexOf("(JOB_LIST)") >= 0 || site.indexOf("(JOB_RUN)") >= 0 || site.indexOf("(USER_ADMIN)") >= 0 || site.indexOf("(LOG_ACCESS)") >= 0 || site.indexOf("(SERVER_VIEW)") >= 0 || site.indexOf("(USER_EDIT)") >= 0 || site.indexOf("(SERVER_EDIT)") >= 0 || site.indexOf("(PREF_EDIT)") >= 0 || site.indexOf("(REPORT_EDIT)") >= 0 || site.indexOf("(SHARE_EDIT)") >= 0 || site.indexOf("(JOB_MONITOR)") >= 0 || site.indexOf("(JOB_LIST_HISTORY)") >= 0 || site.indexOf("(PREF_VIEW)") >= 0 || site.indexOf("(REPORT_VIEW)") >= 0 || site.indexOf("(REPORT_RUN)") >= 0 || site.indexOf("(SHARE_VIEW)") >= 0 || site.indexOf("(USER_VIEW)") >= 0 || site.indexOf("(UPDATE_RUN)") >= 0) && request.getProperty("command", "").equalsIgnoreCase("getServerRoots")) {
                    if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                        return this.writeResponse(AdminControls.buildXML(AdminControls.getServerRoots(request, site, this.thisSessionHTTP.thisSession), "result_value", "OK"), false, 200, false, false, true);
                    }
                    return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
                }
                if (command.equalsIgnoreCase("download")) {
                    keys = request.keys();
                    metaInfo = new Properties();
                    while (keys.hasMoreElements()) {
                        key = keys.nextElement().toString();
                        if (!key.toUpperCase().startsWith("META_")) continue;
                        val = request.getProperty(key);
                        key = key.substring("META_".length());
                        metaInfo.put(key, val);
                        if (key.toUpperCase().startsWith("GLOBAL_")) {
                            if (ServerStatus.thisObj.server_info.get("global_variables") == null) {
                                ServerStatus.thisObj.server_info.put("global_variables", new Properties());
                            }
                            global_variables = (Properties)ServerStatus.thisObj.server_info.get("global_variables");
                            global_variables.put(key, val);
                            continue;
                        }
                        if (!key.toUpperCase().startsWith("USER_INFO_")) continue;
                        this.thisSessionHTTP.thisSession.user_info.put(key, val);
                    }
                    this.thisSessionHTTP.thisSession.uiPUT("the_command", "RETR");
                    the_dir = Common.url_decode(request.getProperty("path"));
                    if (!the_dir.toUpperCase().startsWith(this.thisSessionHTTP.thisSession.SG("root_dir").toUpperCase())) {
                        the_dir = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + (the_dir.startsWith("/") ? the_dir.substring(1) : the_dir);
                    }
                    this.thisSessionHTTP.cd(the_dir);
                    item = null;
                    otherFile = null;
                    ok = this.thisSessionHTTP.thisSession.check_access_privs(this.thisSessionHTTP.pwd(), "RETR") && Common.filter_check("D", Common.last(this.thisSessionHTTP.pwd()), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSessionHTTP.SG("file_filter"));
                    item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.pwd());
                    if (item != null) {
                        if (item.getProperty("type", "").equalsIgnoreCase("DIR")) {
                            if (!this.thisSessionHTTP.pwd().endsWith("/")) {
                                ok = false;
                            } else if (!Common.filter_check("DIR", item.getProperty("name"), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSessionHTTP.SG("file_filter"))) {
                                ok = false;
                            }
                        } else if (item.getProperty("type", "").equalsIgnoreCase("FILE") && !Common.filter_check("F", item.getProperty("name"), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSessionHTTP.SG("file_filter"))) {
                            ok = false;
                        }
                    }
                    if (ok) {
                        otherFile = new VRL(item.getProperty("url"));
                    }
                    if (otherFile == null && this.thisSessionHTTP.pwd().toUpperCase().endsWith(".ZIP")) {
                        this.thisSessionHTTP.cd(this.thisSessionHTTP.pwd().substring(0, this.thisSessionHTTP.pwd().length() - 4));
                        ok = this.thisSessionHTTP.thisSession.check_access_privs(this.thisSessionHTTP.pwd(), "RETR") && Common.filter_check("D", Common.last(this.thisSessionHTTP.pwd()), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSessionHTTP.SG("file_filter"));
                        item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.pwd());
                        if (item != null && ok && item.getProperty("privs").indexOf("(read)") < 0) {
                            ok = false;
                        }
                        if (ok) {
                            otherFile = new VRL(item.getProperty("url"));
                            otherFile = new VRL(String.valueOf(Common.all_but_last(otherFile.toString())) + otherFile.getName() + ".zip");
                        } else {
                            this.thisSessionHTTP.cd(String.valueOf(this.thisSessionHTTP.pwd()) + ".zip");
                        }
                    }
                    if (ok) {
                        if (!metaInfo.getProperty("downloadRevision", "").equals("") && ServerStatus.siIG("enterprise_level") > 0) {
                            otherFile2 = null;
                            rev = Integer.parseInt(metaInfo.getProperty("downloadRevision", ""));
                            while (rev >= 0) {
                                privs = item.getProperty("privs");
                                if (privs.indexOf("(sync") >= 0) {
                                    path = this.thisSessionHTTP.pwd();
                                    if (path.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                        path = path.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
                                    }
                                    if (com.crushftp.client.Common.dmz_mode) {
                                        fname = Common.last(path);
                                        this.write_command_http("HTTP/1.1 200 OK");
                                        validSecs = 30;
                                        this.write_command_http("Cache-Control: post-check=" + validSecs + ",pre-check=" + validSecs * 10);
                                        this.write_command_http("Content-Type: application/binary");
                                        this.write_command_http("Content-Disposition: attachment; filename=\"" + (this.thisSessionHTTP.thisSession.uiSG("header_user-agent").toUpperCase().indexOf("MSIE") >= 0 || this.thisSessionHTTP.thisSession.uiSG("header_user-agent").toUpperCase().indexOf("TRIDENT") >= 0 ? Common.url_encode(fname) : fname) + "\"");
                                        this.write_command_http("X-UA-Compatible: chrome=1");
                                        this.write_command_http("Connection: close");
                                        this.write_command_http("");
                                        c = this.thisSessionHTTP.thisSession.uVFS.getClient(item);
                                        try {
                                            in = ((HTTPClient)c).downloadRev(path, rev);
                                            try {
                                                b = new byte[32768];
                                                bytesRead = 0;
                                                while (bytesRead >= 0) {
                                                    bytesRead = in.read(b);
                                                    if (bytesRead <= 0) continue;
                                                    this.thisSessionHTTP.original_os.write(b, 0, bytesRead);
                                                    this.thisSessionHTTP.thisSession.active_transfer();
                                                }
                                                this.thisSessionHTTP.original_os.flush();
                                            }
                                            finally {
                                                in.close();
                                                this.thisSessionHTTP.original_os.close();
                                            }
                                        }
                                        catch (Exception e) {
                                            Log.log("HTTP_SERVER", 1, e);
                                        }
                                        c.close();
                                        this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                        return true;
                                    }
                                    revPath = Common.parseSyncPart(privs, "revisionsPath");
                                    if (!revPath.equals("")) {
                                        otherFile2 = new VRL(String.valueOf(revPath) + path + "/" + rev + "/" + otherFile.getName());
                                    }
                                }
                                if (otherFile2 != null && new File_S(otherFile2.getPath()).exists()) break;
                                otherFile2 = null;
                                --rev;
                            }
                            if (otherFile2 != null) {
                                otherFile = otherFile2;
                                item.put("url", otherFile2.toString());
                            }
                        }
                    } else {
                        v3 = this.thisSessionHTTP.thisSession.check_access_privs(Common.all_but_last(this.thisSessionHTTP.pwd()), "RETR") && Common.filter_check("D", Common.last(this.thisSessionHTTP.pwd()), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSessionHTTP.SG("file_filter")) ? true : (ok1 = false);
                        v4 = this.thisSessionHTTP.thisSession.check_access_privs(this.thisSessionHTTP.pwd(), "RETR") && Common.filter_check("D", Common.last(this.thisSessionHTTP.pwd()), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSessionHTTP.SG("file_filter")) ? true : (ok2 = false);
                        if (ok1 || ok2) {
                            this.write_command_http("HTTP/1.1 404 Not Found");
                        } else {
                            this.write_command_http("HTTP/1.1 403 Access Denied.");
                        }
                        this.write_command_http("Content-Length: 0");
                        this.write_command_http("");
                        return true;
                    }
                    if (request.getProperty("transfer_type", "").equals("download")) {
                        response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                        transfer_lock = new WebTransfer("download", this.thisSessionHTTP.thisSession.getId(), this.thisSessionHTTP.thisSession.uiSG("user_name"));
                        transfer_lock.putObj("transfer_path", the_dir);
                        transfer_lock.putObj("transfer_id", request.getProperty("download_id", Common.makeBoundary(6)));
                        transfer_lock.putObj("start_resume_loc", request.getProperty("start_resume_loc", "0"));
                        transfer_lock.putObj("metaInfo", metaInfo);
                        c = this.thisSessionHTTP.thisSession.uVFS.getClient(item);
                        stat = null;
                        try {
                            stat = c.stat(otherFile.getPath());
                        }
                        finally {
                            c = this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                        }
                        transfer_lock.putObj("transfer_size", stat.getProperty("size"));
                        thisSession2 = this.thisSessionHTTP.thisSession;
                        Worker.startWorker(new Runnable(){

                            @Override
                            public void run() {
                                try {
                                    ServerSessionAJAX.processHTML5Download(transfer_lock, thisSession2);
                                }
                                catch (Exception e) {
                                    Log.log("SERVER", 1, e);
                                }
                            }
                        });
                        response = String.valueOf(response) + "<commandResult><response>" + transfer_lock.getVal("transfer_id") + "</response></commandResult>";
                        return this.writeResponse(response);
                    }
                    this.downloadItem(otherFile, item, item.getProperty("name"), byteRanges, request.containsKey("range"), request.getProperty("mimeType", ""));
                    return true;
                }
                if (command.equalsIgnoreCase("savetext")) {
                    response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                    file_content = Common.url_decode(request.getProperty("file_content", "")).trim();
                    id = Common.makeBoundary();
                    b = file_content.getBytes("UTF8");
                    request.put("command", "openFile");
                    request.put("upload_size", String.valueOf(b.length));
                    request.put("upload_id", id);
                    request.put("start_resume_loc", "0");
                    request.put("internal_response", "");
                    if (!request.containsKey("filePath") && request.containsKey("upload_path")) {
                        request.put("filePath", request.getProperty("upload_path", ""));
                    }
                    this.processItems(request, byteRanges, req_id);
                    if (!request.getProperty("internal_response").startsWith("ERROR")) {
                        if (request.get("transfer_lock") instanceof Properties) {
                            transfer_lock = (Properties)request.remove("transfer_lock");
                            transfer_lock.put("1", b);
                        }
                        if (request.get("transfer_lock") instanceof WebTransfer) {
                            transfer_lock = (WebTransfer)request.get("transfer_lock");
                            transfer_lock.addChunk("1", b);
                        }
                        request.put("total_chunks", "1");
                        request.put("command", "closeFile");
                        request.put("internal_response", "");
                        this.processItems(request, byteRanges, req_id);
                    }
                    response = String.valueOf(response) + "<commandResult><response>" + request.getProperty("internal_response") + "</response></commandResult>";
                    return this.writeResponse(response);
                }
                if (!command.equalsIgnoreCase("openFile") || !request.getProperty("transfer_type", "upload").equals("upload")) break block918;
                if (this.thisSessionHTTP.thisSession.getProperty("blockUploads", "false").equals("true")) {
                    Thread.sleep(5000L);
                    this.thisSessionHTTP.thisSession.put("blockUploads", "false");
                }
                response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                upload_path = this.thisSessionHTTP.thisSession.getStandardizedDir(request.getProperty("upload_path"));
                dir_item = this.thisSessionHTTP.thisSession.uVFS.get_item(upload_path);
                file_filter = true;
                if (dir_item == null) ** GOTO lbl-1000
                if (!Common.filter_check("F", String.valueOf(dir_item.getProperty("name")) + (dir_item.getProperty("type").equalsIgnoreCase("DIR") && !dir_item.getProperty("name").endsWith("/") ? "/" : ""), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSessionHTTP.thisSession.SG("file_filter"))) {
                    file_filter = false;
                } else if (!Common.last(upload_path).endsWith("/") && !Common.filter_check("F", Common.last(upload_path), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSessionHTTP.thisSession.SG("file_filter"))) {
                    file_filter = false;
                }
                result_msg = "";
                transfer_lock = new WebTransfer("upload", this.thisSessionHTTP.thisSession.getId(), this.thisSessionHTTP.thisSession.uiSG("user_name"));
                if (this.thisSessionHTTP.thisSession.check_access_privs(upload_path, "STOR") && Common.filter_check("U", Common.last(upload_path), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSessionHTTP.thisSession.SG("file_filter")) && file_filter && (dir_item == null || dir_item.getProperty("type").equalsIgnoreCase("file") || dir_item.getProperty("simple", "").equalsIgnoreCase("true"))) {
                    transfer_lock.setInfo(request);
                    this.thisSessionHTTP.thisSession.user_info.put("file_length", request.getProperty("upload_size"));
                    metaInfo = new Properties();
                    keys = request.keys();
                    while (keys.hasMoreElements()) {
                        name = "" + keys.nextElement();
                        data_item = request.getProperty(name);
                        if (!name.toUpperCase().startsWith("META_")) continue;
                        if (metaInfo.containsKey(name = name.substring(5))) {
                            metaInfo.put(name, String.valueOf(metaInfo.getProperty(name)) + "," + (String)data_item);
                        } else {
                            metaInfo.put(name, data_item);
                        }
                        if (name.toUpperCase().startsWith("GLOBAL_")) {
                            if (com.crushftp.client.Common.System2.get("global_variables") == null) {
                                com.crushftp.client.Common.System2.put("global_variables", new Properties());
                            }
                            global_variables = (Properties)com.crushftp.client.Common.System2.get("global_variables");
                            global_variables.put(name, data_item);
                            continue;
                        }
                        if (!name.toUpperCase().startsWith("USER_INFO_")) continue;
                        this.thisSessionHTTP.thisSession.user_info.put("ui_" + name.substring("USER_INFO_".length()), data_item);
                    }
                    this.thisSessionHTTP.thisSession.put("last_metaInfo", metaInfo);
                    transfer_lock.putObj("metaInfo", metaInfo);
                    thisSession2 = this.thisSessionHTTP.thisSession;
                    data_item = thisSession2;
                    synchronized (data_item) {
                        html5_transfers_session = (Properties)thisSession2.get("html5_transfers_session");
                        if (html5_transfers_session == null) {
                            html5_transfers_session = new Properties();
                        }
                        thisSession2.put("html5_transfers_session", html5_transfers_session);
                        if (html5_transfers_session.containsKey(transfer_lock.getVal("transfer_path"))) {
                            return this.writeResponse("<commandResult><response>ERROR:Transfer already in progress for your session:" + transfer_lock.getVal("transfer_path") + "</response></commandResult>");
                        }
                        html5_transfers_session.put(transfer_lock.getVal("transfer_path"), String.valueOf(System.currentTimeMillis()));
                        Thread.sleep(1L);
                    }
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            try {
                                ServerSessionAJAX.processHTML5Upload(transfer_lock, thisSession2);
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 1, e);
                            }
                        }
                    });
                    loops = 0;
                    ok = true;
                    while (!(loops++ >= 600 || transfer_lock.hasObj("stor") && ((STOR_handler)transfer_lock.getObj((String)"stor")).active2.getProperty("streamOpenStatus", "").equals("OPEN"))) {
                        if (transfer_lock.hasObj("stor") && ((STOR_handler)transfer_lock.getObj((String)"stor")).active2.getProperty("streamOpenStatus", "").equals("CLOSED")) {
                            result_msg = String.valueOf(LOC.G("ERROR")) + ": " + ((STOR_handler)transfer_lock.getObj((String)"stor")).stop_message;
                            ok = false;
                            break;
                        }
                        Thread.sleep(100L);
                    }
                    transfer_lock.removeObj("stor");
                    if (ok) {
                        result_msg = request.getProperty("upload_id");
                    }
                } else {
                    if (!Common.filter_check("U", Common.last(upload_path), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSessionHTTP.thisSession.SG("file_filter")) || !file_filter) {
                        this.thisSessionHTTP.thisSession.add_log_formatted("550 STOR error: Upload attempt was rejected because the block matching names! File name :" + Common.last(upload_path) + " Filters :" + ServerStatus.SG("filename_filters_str"), "STOR");
                    }
                    this.thisSessionHTTP.thisSession.doErrorEvent(new Exception("Upload attempt was rejected because the block matching names! File name :" + Common.last(upload_path) + " Filters :" + ServerStatus.SG("filename_filters_str") + this.thisSessionHTTP.thisSession.SG("file_filter")));
                    result_msg = String.valueOf(LOC.G("ERROR")) + ": " + LOC.G("Access denied. (You do not have permission or the file extension is not allowed.)");
                }
                response = String.valueOf(response) + "<commandResult><response>" + result_msg + "</response></commandResult>";
                Thread.sleep(200L);
                if (!request.containsKey("internal_response")) {
                    return this.writeResponse(response);
                }
                request.put("internal_response", result_msg);
                request.put("transfer_lock", transfer_lock);
                return true;
            }
            if (command.equalsIgnoreCase("closeFile")) {
                transfer_type = request.getProperty("transfer_type", "upload").equals("upload") ? "upload" : "download";
                html5_transfers = ServerStatus.siPG("html5_transfers");
                transfer_lock = (WebTransfer)html5_transfers.get(String.valueOf(this.thisSessionHTTP.thisSession.uiSG("user_protocol")) + this.thisSessionHTTP.thisSession.uiSG("user_name") + this.thisSessionHTTP.thisSession.uiSG("user_ip") + "_" + request.getProperty(String.valueOf(transfer_type) + "_id"));
                response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                result_msg = "";
                if (transfer_lock != null && request.getProperty("transfer_type", "upload").equals("upload")) {
                    if (!request.getProperty("lastModified", "0").equals("0")) {
                        transfer_lock.putObj("lastModified", request.getProperty("lastModified", "0"));
                    }
                    transfer_lock.putObj("total_chunks", request.getProperty("total_chunks"));
                    start = System.currentTimeMillis();
                    x = 0;
                    while (System.currentTimeMillis() - start < 60000L && !transfer_lock.hasObj("status")) {
                        Thread.sleep(x < 100 ? x : 100);
                        ++x;
                    }
                    result_msg = transfer_lock.getVal("status", "Decompressing...");
                    response = String.valueOf(response) + "<commandResult><response>" + result_msg + "</response><md5>" + transfer_lock.getVal("md5") + "</md5></commandResult>";
                } else if (transfer_lock != null && request.getProperty("transfer_type", "upload").equals("download")) {
                    result_msg = String.valueOf(transfer_lock.getVal("status")) + "</response><total_chunks>" + transfer_lock.getVal("total_chunks");
                    response = String.valueOf(response) + "<commandResult><response>" + result_msg + "</total_chunks></commandResult>";
                    transfer_lock.putObj("blockDownloads", "true");
                    html5_transfers.remove(String.valueOf(this.thisSessionHTTP.thisSession.uiSG("user_protocol")) + this.thisSessionHTTP.thisSession.uiSG("user_name") + this.thisSessionHTTP.thisSession.uiSG("user_ip") + "_" + transfer_lock.getVal("transfer_id"));
                } else if (transfer_lock == null && request.getProperty("transfer_type", "upload").equals("download")) {
                    result_msg = "Already closed.";
                    response = String.valueOf(response) + "<commandResult><response>" + result_msg + "</response><total_chunks>-1</total_chunks></commandResult>";
                } else {
                    result_msg = "ERROR:No such " + transfer_type + " open.";
                    response = String.valueOf(response) + "<commandResult><response>" + result_msg + "</response></commandResult>";
                }
                if (!request.containsKey("internal_response")) {
                    return this.writeResponse(response);
                }
                request.put("internal_response", result_msg);
                return true;
            }
            if (command.equalsIgnoreCase("isOpen")) {
                transfer_type = request.getProperty("transfer_type", "true").equals("true") ? "upload" : "download";
                html5_transfers = ServerStatus.siPG("html5_transfers");
                transfer_lock = (WebTransfer)html5_transfers.get(String.valueOf(this.thisSessionHTTP.thisSession.uiSG("user_protocol")) + this.thisSessionHTTP.thisSession.uiSG("user_name") + this.thisSessionHTTP.thisSession.uiSG("user_ip") + "_" + request.getProperty(String.valueOf(transfer_type) + "_id"));
                response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                response = transfer_lock != null && transfer_lock.getVal("status", "").startsWith("ERROR:") ? String.valueOf(response) + "<commandResult><response>" + transfer_lock.getVal("status", "").replace('>', '_').replace('<', '_') + "</response></commandResult>" : String.valueOf(response) + "<commandResult><response>" + (transfer_lock != null ? "OPEN" : "CLOSED") + "</response></commandResult>";
                return this.writeResponse(response);
            }
            if (command.equalsIgnoreCase("loginDomain1")) {
                uid = Common.makeBoundary();
                ServerStatus.siPG("domain_cross_reference").put("0.0.0.0:" + uid, String.valueOf(System.currentTimeMillis()) + ":" + this.thisSessionHTTP.thisSession.getId());
                response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                response = String.valueOf(response) + "<commandResult><response>" + uid + "</response></commandResult>";
                return this.writeResponse(response);
            }
            if (command.equalsIgnoreCase("letsencrypt")) {
                status = "Success";
                if (request.getProperty("domains", "").equals("") || request.getProperty("keystore_path", "").equals("")) {
                    status = "ERROR: Domains, Keystore ... must be filled!";
                } else {
                    try {
                        Common.runPlugin("LetsEncrypt", request, "");
                        if (request.containsKey("ERROR")) {
                            status = "ERROR: " + request.get("ERROR");
                        }
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 1, e);
                        status = "ERROR: " + e;
                    }
                }
                response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                response = String.valueOf(response) + "<commandResult><response>" + status + "</response></commandResult>";
                return this.writeResponse(response);
            }
            if (command.equalsIgnoreCase("testVFS")) {
                limited_request = new Properties();
                limited_request.put("command", "testVFS");
                limited_request.put("path", Common.dots(request.getProperty("path")));
                if (site.indexOf("(CONNECT)") >= 0 || site.indexOf("(USER_ADMIN)") >= 0 || site.indexOf("(USER_VIEW)") >= 0 || site.indexOf("(USER_EDIT)") >= 0) {
                    limited_request.put("username", request.getProperty("username"));
                    limited_request.put("serverGroup", request.getProperty("serverGroup", this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer")));
                } else {
                    limited_request.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                    limited_request.put("serverGroup", this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer"));
                }
                response = "";
                o = AdminControls.getUser(limited_request, "(USER_EDIT)", this.thisSessionHTTP.thisSession);
                if (o instanceof Properties) {
                    block900: {
                        user = (Properties)o;
                        limited_request.put("format", "JSON");
                        limited_request.put("file_mode", "user");
                        limited_request.put("permissions", user.get("permissions"));
                        v = (Vector)user.get("vfs_items");
                        vfs_items = new Vector<Properties>();
                        x = 0;
                        while (x < v.size()) {
                            vv = (Vector)v.get(x);
                            item = (Properties)vv.get(0);
                            vfs_item = new Properties();
                            vfs_item.put("name", item.getProperty("name"));
                            vfs_item.put("path", item.getProperty("path"));
                            vfs_item.put("type", item.getProperty("type"));
                            vfs_item.put("vfs_item", vv);
                            vfs_items.add(vfs_item);
                            ++x;
                        }
                        limited_request.put("vfs_items", vfs_items);
                        System.getProperties().put("crushftp.isTestCall" + Thread.currentThread().getId(), "true");
                        try {
                            try {
                                listingProp = AdminControls.getUserXMLListing(limited_request, "(USER_EDIT)", this.thisSessionHTTP.thisSession);
                                if (!listingProp.getProperty("error", "").equals("")) {
                                    response = listingProp.getProperty("error", "");
                                    if (!ServerStatus.BG("test_vfs_return_error")) {
                                        response = "FAILURE";
                                    }
                                } else if (listingProp.get("listing") instanceof Vector) {
                                    response = "SUCCESS";
                                }
                                response = "<commandResult><response>" + response + "</response></commandResult>";
                            }
                            catch (Exception e) {
                                Log.log("HTTP_SERVER", 1, e);
                                if (!ServerStatus.BG("test_vfs_return_error")) {
                                    e = new Exception("FAILURE");
                                }
                                response = "<commandResult><response>ERROR: " + e.getMessage() + "</response></commandResult>";
                                System.getProperties().remove("crushftp.isTestCall" + Thread.currentThread().getId());
                                break block900;
                            }
                        }
                        catch (Throwable vv) {
                            System.getProperties().remove("crushftp.isTestCall" + Thread.currentThread().getId());
                            throw vv;
                        }
                        System.getProperties().remove("crushftp.isTestCall" + Thread.currentThread().getId());
                    }
                    return this.writeResponse(response);
                }
                if (!ServerStatus.BG("test_vfs_return_error")) {
                    o = "FAILURE";
                }
                response = "<commandResult><response>ERROR:" + o + "</response></commandResult>";
                return this.writeResponse(response);
            }
            if (command.equals("testAllVFS")) {
                response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                    result = AdminControls.testAllVFSOfUser(request, site);
                    if (result instanceof Vector) {
                        return this.writeResponse(AdminControls.buildXML(result, "result_value", "OK"), false, 200, false, false, false);
                    }
                    if (result instanceof String) {
                        response = "<commandResult><response>ERROR: " + result + "</response></commandResult>";
                    }
                    response = "<commandResult><response>" + response + "</response></commandResult>";
                } else {
                    response = String.valueOf(response) + "<commandResult><response>FAILURE:Access Denied.</response></commandResult>";
                }
                return this.writeResponse(response);
            }
            if (command.equalsIgnoreCase("lookup_citrix_api_code")) {
                response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                if (site.indexOf("(CONNECT)") >= 0 || site.indexOf("(USER_ADMIN)") >= 0 || site.indexOf("(JOB_EDIT)") >= 0) {
                    if (this.thisSessionHTTP.thisSession.getProperty("citrix_api_code") != null) {
                        citrix_api_code = "" + this.thisSessionHTTP.thisSession.remove("citrix_api_code");
                        citrix_api_subdomain = "" + this.thisSessionHTTP.thisSession.remove("citrix_api_subdomain");
                        p = CitrixClient.setup_bearer(citrix_api_subdomain, citrix_api_apicp = "" + this.thisSessionHTTP.thisSession.remove("citrix_api_apicp"), citrix_api_code, request.getProperty("redirect_uri"), request.getProperty("citrix_client_info").split("~")[0], request.getProperty("citrix_client_info").split("~")[1]);
                        refresh_token = p.getProperty("refresh_token", "");
                        if (refresh_token.equals("")) {
                            refresh_token = p.getProperty("access_token");
                        }
                        response = String.valueOf(response) + "<commandResult><response>" + refresh_token + "{split}" + p.getProperty("subdomain", "") + "{split}" + p.getProperty("apicp", "") + "</response></commandResult>";
                    } else {
                        response = String.valueOf(response) + "<commandResult><response></response></commandResult>";
                    }
                } else {
                    response = String.valueOf(response) + "<commandResult><response></response></commandResult>";
                }
                return this.writeResponse(response);
            }
            if (command.equalsIgnoreCase("lookup_microsoft_graph_api_code")) {
                response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                if (ServerStatus.BG("v10_beta") && (site.indexOf("(CONNECT)") >= 0 || site.indexOf("(USER_ADMIN)") >= 0 || site.indexOf("(JOB_EDIT)") >= 0)) {
                    if (this.thisSessionHTTP.thisSession.getProperty("microsoft_graph_api_code") != null) {
                        microsoft_graph_api_code = "" + this.thisSessionHTTP.thisSession.remove("microsoft_graph_api_code");
                        p = com.crushftp.client.Common.get_smtp_oauth_refresh_token("https://login.microsoftonline.com/" + request.getProperty("tenant", "common") + "/oauth2/v2.0/", microsoft_graph_api_code, request.getProperty("server_url"), request.getProperty("client_id").split("~")[0], com.crushftp.client.Common.encryptDecrypt(request.getProperty("client_secret"), false), Common.replace_str(request.getProperty("scope", ""), "+", "%20"));
                        response = String.valueOf(response) + "<commandResult><response>" + p.getProperty("refresh_token", p.getProperty("access_token")) + "</response></commandResult>";
                    }
                    if (this.thisSessionHTTP.thisSession.getProperty("microsoft_graph_api_adminconsent") != null) {
                        response = String.valueOf(response) + "<commandResult><response>" + this.thisSessionHTTP.thisSession.getProperty("microsoft_graph_api_adminconsent") + "</response></commandResult>";
                        this.thisSessionHTTP.thisSession.put("microsoft_graph_api_adminconsent", null);
                    } else {
                        response = String.valueOf(response) + "<commandResult><response></response></commandResult>";
                    }
                } else {
                    response = String.valueOf(response) + "<commandResult><response></response></commandResult>";
                }
                return this.writeResponse(response);
            }
            if (command.equalsIgnoreCase("lookup_oath_api")) {
                response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                if (site.indexOf("(CONNECT)") >= 0) {
                    if (this.thisSessionHTTP.thisSession.getProperty("oauth_mail_api_code") != null) {
                        oauth_mail_api_code = "" + this.thisSessionHTTP.thisSession.remove("oauth_mail_api_code");
                        p = com.crushftp.client.Common.get_smtp_oauth_refresh_token(ServerStatus.SG("smtp_client_url"), oauth_mail_api_code, request.getProperty("server_url"), request.getProperty("smtp_client_id"), com.crushftp.client.Common.encryptDecrypt(request.getProperty("smtp_client_secret"), false), request.getProperty("smtp_client_scope").replace('+', ' '));
                        response = String.valueOf(response) + "<commandResult><response>" + p.getProperty("refresh_token", p.getProperty("access_token")) + "</response></commandResult>";
                    } else {
                        response = String.valueOf(response) + "<commandResult><response></response></commandResult>";
                    }
                } else {
                    response = String.valueOf(response) + "<commandResult><response></response></commandResult>";
                }
                return this.writeResponse(response);
            }
            if (command.equalsIgnoreCase("testMicrosoftMail")) {
                response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                if (site.indexOf("(CONNECT)") >= 0 || site.indexOf("(USER_ADMIN)") >= 0 || site.indexOf("(JOB_EDIT)") >= 0) {
                    try {
                        if (request.getProperty("ms_mails_client_id", "").equals("")) {
                            throw new Exception("Missing client id.");
                        }
                        if (request.getProperty("ms_mails_client_secret", "").equals("")) {
                            throw new Exception("Missing secret id.");
                        }
                        if (request.getProperty("ms_mails_tenant", "").equals("")) {
                            throw new Exception("Missing tenant.");
                        }
                        p = com.crushftp.client.Common.ms_client_credential_grant_token(com.crushftp.client.Common.url_decode(request.getProperty("ms_mails_client_id", "")), com.crushftp.client.Common.encryptDecrypt(com.crushftp.client.Common.url_decode(request.getProperty("ms_mails_client_secret", "")), false), com.crushftp.client.Common.url_decode(request.getProperty("ms_mails_tenant", "")), request.getProperty("ms_mails_scope", "https%3A%2F%2Fgraph.microsoft.com%2F.default"));
                        access_token = p.getProperty("access_token", "");
                        if (access_token.equals("")) {
                            throw new Exception("Authentication failed.");
                        }
                        if (access_token.split("\\.").length >= 1 && !(data = new String(decoded_bytes = Base64.decode(signature = access_token.split("\\.")[1]), "UTF-8")).contains("Mail.ReadWrite") && !data.contains("Mail.Read")) {
                            throw new Exception("Missing permission of : Mail.ReadWrite and Mail.Read.");
                        }
                        response = String.valueOf(response) + "<commandResult><response>Success!</response></commandResult>";
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 1, e);
                        response = String.valueOf(response) + "<commandResult><response>ERROR: " + com.crushftp.client.Common.url_encode(e.getMessage()) + "</response></commandResult>";
                    }
                } else {
                    response = String.valueOf(response) + "<commandResult><response>ERROR:Access Denied.</response></commandResult>";
                }
                return this.writeResponse(response);
            }
            if (command.equalsIgnoreCase("saveRefreshToken")) {
                response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                try {
                    customizations = (Vector)this.thisSessionHTTP.thisSession.user.get("web_customizations");
                    enabled = false;
                    x = 0;
                    while (x < customizations.size()) {
                        pp = (Properties)customizations.elementAt(x);
                        key = pp.getProperty("key");
                        if (key.equals("promptForRefreshToken") && pp.getProperty("value", "").equals("true")) {
                            enabled = true;
                        }
                        ++x;
                    }
                    if (enabled) {
                        token = request.getProperty("refresh_token", "");
                        if (token.equals("") || token.contains("@") || token.contains(":") | token.contains("/")) {
                            throw new Exception("Illegal characters in refresh token!");
                        }
                        root_item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir"));
                        virtual = (Properties)this.thisSessionHTTP.thisSession.uVFS.homes.elementAt(0);
                        found = false;
                        keys = virtual.keys();
                        while (keys.hasMoreElements()) {
                            key = keys.nextElement().toString();
                            if (key.equals("vfs_permissions_object") || !(home = (Properties)virtual.get(key)).containsKey("vItems")) continue;
                            vItems = (Vector)home.get("vItems");
                            xx = 0;
                            while (xx < vItems.size()) {
                                vitem = (Properties)vItems.elementAt(xx);
                                url = vitem.getProperty("url", "");
                                vrl = new VRL(vitem.getProperty("url"));
                                if (vrl.getProtocol().toLowerCase().equals("dropbox") && vrl.getPassword().equals("")) {
                                    url = Common.replace_str(url, ":@", ":" + token + "@");
                                    vitem.put("url", url);
                                    found = true;
                                }
                                ++xx;
                            }
                        }
                        if (found) {
                            UserTools.writeVFS(this.thisSessionHTTP.thisSession.uiSG("listen_ip_port"), this.thisSessionHTTP.thisSession.user.getProperty("username", ""), this.thisSessionHTTP.thisSession.uVFS);
                            response = String.valueOf(response) + "<commandResult><response>Success!</response></commandResult>";
                        }
                    } else {
                        response = String.valueOf(response) + "<commandResult><response>ERROR:Access Denied.</response></commandResult>";
                    }
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                    response = String.valueOf(response) + "<commandResult><response>ERROR: " + com.crushftp.client.Common.url_encode(e.getMessage()) + "</response></commandResult>";
                }
                return this.writeResponse(response);
            }
            if (command.equalsIgnoreCase("getSubscribeReverseNotificationEvents")) {
                response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                if (com.crushftp.client.Common.dmz_mode) {
                    queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                    action = new Properties();
                    action.put("type", "GET:GET_SUBSCRIBE_REVERSE_NOTIFICATION_EVENTS");
                    action.put("id", Common.makeBoundary());
                    action.put("serverGroup", this.thisSessionHTTP.thisSession.uiSG("listen_ip_port"));
                    action.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                    action.put("path", request.getProperty("path", ""));
                    action.put("request", request);
                    action.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
                    action.put("need_response", "true");
                    queue.addElement(action);
                    action = UserTools.waitResponse(action, 300);
                    result = action.remove("response").toString();
                    response = result.startsWith("Subscribe Error: ") ? "<commandResult><response>ERROR: " + com.crushftp.client.Common.url_encode(result) + "</response></commandResult>" : String.valueOf(response) + "<commandResult><response>" + result + "</response></commandResult>";
                } else {
                    try {
                        result = UserTools.getSubscribeReverseNotificationEvents(this.thisSessionHTTP.thisSession.uiSG("listen_ip_port"), this.thisSessionHTTP.thisSession.uiSG("user_name"), request);
                        response = String.valueOf(response) + "<commandResult><response>" + result + "</response></commandResult>";
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 1, e);
                        response = String.valueOf(response) + "<commandResult><response>ERROR: " + com.crushftp.client.Common.url_encode(e.getMessage()) + "</response></commandResult>";
                    }
                }
                return this.writeResponse(response);
            }
            if (command.equalsIgnoreCase("saveSubscribeReverseNotificationEvents")) {
                response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                if (com.crushftp.client.Common.dmz_mode) {
                    queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                    action = new Properties();
                    action.put("type", "PUT:SAVE_SUBSCRIBE_REVERSE_NOTIFICATION_EVENTS");
                    action.put("id", Common.makeBoundary());
                    action.put("serverGroup", this.thisSessionHTTP.thisSession.uiSG("listen_ip_port"));
                    action.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                    action.put("path", request.getProperty("path", ""));
                    action.put("privs", request.getProperty("privs", ""));
                    action.put("request", request);
                    action.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
                    action.put("need_response", "true");
                    queue.addElement(action);
                    action = UserTools.waitResponse(action, 300);
                    result = action.remove("response").toString();
                    response = result.equals("") ? String.valueOf(response) + "<commandResult><response>Success!</response></commandResult>" : String.valueOf(response) + "<commandResult><response>ERROR: " + com.crushftp.client.Common.url_encode(result) + "</response></commandResult>";
                } else {
                    try {
                        the_dir = Common.url_decode(request.getProperty("path"));
                        if (the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                            the_dir = the_dir.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
                        }
                        this.thisSessionHTTP.thisSession.uiPUT("the_command", "STAT");
                        this.thisSessionHTTP.thisSession.uiPUT("the_command_data", the_dir);
                        item = null;
                        x = 0;
                        while (x < 5 && item == null) {
                            if (this.thisSessionHTTP.thisSession.uVFS.get_item(Common.all_but_last(String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + the_dir)) != null) {
                                item = this.thisSessionHTTP.thisSession.uVFS.get_item(String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + the_dir);
                                break;
                            }
                            Log.log("SERVER", 2, "Path not found, (" + x + ") retrying:" + the_dir);
                            Thread.sleep(1000L);
                            ++x;
                        }
                        if (item == null) {
                            throw new Exception("Could not found the given path!Path : " + the_dir);
                        }
                        result = UserTools.saveSubscribeReverseNotificationEvents(this.thisSessionHTTP.thisSession.uiSG("listen_ip_port"), this.thisSessionHTTP.thisSession.uiSG("user_name"), request);
                        response = result.equals("") ? String.valueOf(response) + "<commandResult><response>Success!</response></commandResult>" : String.valueOf(response) + "<commandResult><response>ERROR: " + result + "</response></commandResult>";
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 1, e);
                        response = String.valueOf(response) + "<commandResult><response>ERROR: " + com.crushftp.client.Common.url_encode(e.getMessage()) + "</response></commandResult>";
                    }
                }
                return this.writeResponse(response);
            }
            if (command.equalsIgnoreCase("loadKeyStores")) {
                if (ServerStatus.BG("v11_beta") && AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                    p = AdminControls.loadKeyStores(request, site);
                    return this.writeResponse(AdminControls.buildXML(p, "keys", "OK"), false, 200, false, false, false);
                }
                return this.writeResponse("<commandResult><response>FAILURE:Access Denied.</response></commandResult>");
            }
            if (command.equalsIgnoreCase("saveKeyStores")) {
                response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                if (ServerStatus.BG("v11_beta") && AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                    result = AdminControls.saveKeyStores(request, site);
                    response = String.valueOf(response) + "<commandResult><response>" + result + "</response></commandResult>";
                } else {
                    response = String.valueOf(response) + "<commandResult><response>FAILURE:Access Denied.</response></commandResult>";
                }
                this.writeResponse(response);
            } else if (command.equalsIgnoreCase("clearCache")) {
                response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                if (AdminControls.checkRole(command, site, this.thisSessionHTTP.thisSession.uiSG("user_ip"))) {
                    result = AdminControls.clearCache(request, site);
                    response = String.valueOf(response) + "<commandResult><response>" + result + "</response></commandResult>";
                    this.writeResponse(response);
                } else {
                    response = String.valueOf(response) + "<commandResult><response>FAILURE:Access Denied.</response></commandResult>";
                }
                this.writeResponse(response);
            }
        }
        return false;
    }

    public String getUserInfo(Properties request, String site) throws Exception {
        Properties site_item;
        GenericClient c;
        Properties responseProp = new Properties();
        Properties extraCustomizations = (Properties)this.thisSessionHTTP.thisSession.get("extraCustomizations");
        if (extraCustomizations != null) {
            responseProp.putAll((Map<?, ?>)extraCustomizations);
        }
        extraCustomizations = null;
        extraCustomizations = (Properties)this.thisSessionHTTP.thisSession.user.get("extraCustomizations");
        if (extraCustomizations != null) {
            responseProp.putAll((Map<?, ?>)extraCustomizations);
        }
        Vector customizations = null;
        try {
            Properties userCust;
            Properties pp;
            Properties ppp;
            String key;
            Enumeration<Object> keys;
            customizations = (Vector)this.thisSessionHTTP.thisSession.user.get("web_customizations");
            if (customizations == null) {
                customizations = (Vector)UserTools.ut.getUser(this.thisSessionHTTP.thisSession.uiSG("listen_ip_port"), "default", false).get("web_customizations");
            }
            if (customizations == null) {
                customizations = new Vector();
            }
            customizations = (Vector)com.crushftp.client.Common.CLONE(customizations);
            if (com.crushftp.client.Common.dmz_mode) {
                Vector queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                Properties action = new Properties();
                action.put("type", "GET:HANDLE_CUSTOMIZATIONS");
                action.put("id", Common.makeBoundary());
                Properties root_item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir"));
                c = this.thisSessionHTTP.thisSession.uVFS.getClient(root_item);
                action.put("crushAuth", c.getConfig("crushAuth"));
                this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                action.put("customizations", customizations);
                action.put("need_response", "true");
                queue.addElement(action);
                action = UserTools.waitResponse(action, 300);
                Object response = action.remove("object_response");
                if (response instanceof Vector) {
                    customizations = (Vector)response;
                } else {
                    ServerSessionAJAX.handleCustomizations(customizations, this.thisSessionHTTP.thisSession);
                }
            } else {
                ServerSessionAJAX.handleCustomizations(customizations, this.thisSessionHTTP.thisSession);
            }
            if (System.getProperties().get("crushftp.httpCustomizations.global") != null) {
                Properties globalCust = (Properties)System.getProperties().get("crushftp.httpCustomizations.global");
                keys = globalCust.keys();
                while (keys.hasMoreElements()) {
                    key = keys.nextElement().toString();
                    ppp = new Properties();
                    ppp.put("key", key);
                    ppp.put("value", globalCust.getProperty(key));
                    customizations.addElement(ppp);
                }
            }
            if (System.getProperties().get("crushftp.httpCustomizations.user") != null && (pp = (Properties)(userCust = (Properties)System.getProperties().get("crushftp.httpCustomizations.user")).get(this.thisSessionHTTP.thisSession.uiSG("user_name").toUpperCase())) != null) {
                Enumeration<Object> keys2 = pp.keys();
                while (keys2.hasMoreElements()) {
                    String key2 = keys2.nextElement().toString();
                    Properties ppp2 = new Properties();
                    ppp2.put("key", key2);
                    ppp2.put("value", pp.getProperty(key2));
                    customizations.addElement(ppp2);
                }
            }
            if (this.thisSessionHTTP.thisSession.get("wi_customizations") != null) {
                Properties wi_customizations = (Properties)this.thisSessionHTTP.thisSession.get("wi_customizations");
                keys = wi_customizations.keys();
                while (keys.hasMoreElements()) {
                    key = keys.nextElement().toString();
                    ppp = new Properties();
                    ppp.put("key", key);
                    ppp.put("value", wi_customizations.getProperty(key));
                    customizations.addElement(ppp);
                }
            }
            if (this.thisSessionHTTP.thisSession.get("metas") != null) {
                Properties metas = (Properties)this.thisSessionHTTP.thisSession.get("metas");
                keys = metas.keys();
                while (keys.hasMoreElements()) {
                    key = keys.nextElement().toString();
                    if (responseProp.containsKey(key)) continue;
                    responseProp.put("META_" + key, metas.getProperty(key));
                }
            }
            responseProp.put("customizations", customizations);
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
        }
        try {
            Vector buttons = (Vector)this.thisSessionHTTP.thisSession.user.get("web_buttons");
            if (buttons == null) {
                buttons = (Vector)UserTools.ut.getUser(this.thisSessionHTTP.thisSession.uiSG("listen_ip_port"), "default", false).get("buttons");
            }
            if (buttons == null) {
                buttons = new Vector();
            }
            buttons = (Vector)buttons.clone();
            this.addMissingButtons(buttons);
            ServerSessionAJAX.fixButtons(buttons);
            if (this.thisSessionHTTP.thisSession.BG("hide_download")) {
                int x = buttons.size() - 1;
                while (x >= 0) {
                    Properties button = (Properties)buttons.elementAt(x);
                    if (button.getProperty("key", "").startsWith("(download)")) {
                        buttons.remove(x);
                    }
                    --x;
                }
            }
            responseProp.put("buttons", buttons);
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
        }
        responseProp.put("user_priv_options", site);
        if (ServerStatus.BG("user_reveal_version")) {
            responseProp.put("app_version", ServerStatus.version_info_str);
            if (com.crushftp.client.Common.dmz_mode) {
                responseProp.put("internal_app_version", this.thisSessionHTTP.thisSession.user_info.getProperty("internal_app_version", ""));
            }
        } else {
            responseProp.put("app_version", ServerStatus.version_info_str.substring(0, ServerStatus.version_info_str.indexOf(".")));
        }
        if (ServerStatus.BG("user_reveal_hostname")) {
            responseProp.put("hostname", ServerStatus.hostname);
            if (com.crushftp.client.Common.dmz_mode) {
                responseProp.put("internal_hostname", this.thisSessionHTTP.thisSession.user_info.getProperty("internal_hostname", ""));
            }
        }
        responseProp.put("app_enterprise", ServerStatus.siSG("enterprise_level"));
        responseProp.put("unique_upload_id", Common.makeBoundary(3));
        responseProp.put("display_alt_logo", ServerStatus.SG("display_alt_logo"));
        Properties password_rules = SessionCrush.build_password_rules(this.thisSessionHTTP.thisSession.user);
        responseProp.put("random_password_length", password_rules.getProperty("random_password_length"));
        responseProp.put("unsafe_password_chars", password_rules.getProperty("unsafe_password_chars"));
        responseProp.put("min_password_length", password_rules.getProperty("min_password_length"));
        responseProp.put("min_password_numbers", password_rules.getProperty("min_password_numbers"));
        responseProp.put("min_password_lowers", password_rules.getProperty("min_password_lowers"));
        responseProp.put("min_password_uppers", password_rules.getProperty("min_password_uppers"));
        responseProp.put("min_password_specials", password_rules.getProperty("min_password_specials"));
        responseProp.put("temp_accounts_length", ServerStatus.SG("temp_accounts_length"));
        responseProp.put("temp_upload_ext", this.thisSessionHTTP.thisSession.SG("temp_upload_ext"));
        if (ServerStatus.siIG("enterprise_level") > 0) {
            responseProp.put("alt_http_domains", this.thisSessionHTTP.thisSession.SG("alt_http_domains"));
        }
        responseProp.put("account_expire", this.thisSessionHTTP.thisSession.SG("account_expire"));
        responseProp.put("timezone_offset", this.thisSessionHTTP.thisSession.user.getProperty("timezone_offset", "0"));
        responseProp.put("server_time", String.valueOf(System.currentTimeMillis()));
        responseProp.put("root_dir_name", this.thisSessionHTTP.thisSession.SG("root_dir").replace('/', ' ').trim());
        if (site.indexOf("(CONNECT)") < 0 && site.indexOf("(USER_ADMIN)") >= 0) {
            responseProp.put("allowed_config", this.thisSessionHTTP.thisSession.SG("allowed_config"));
        }
        responseProp.put("v9_beta", "true");
        responseProp.put("twofactor_secret_confirmed", this.thisSessionHTTP.thisSession.user.getProperty("twofactor_secret", "").equals("") ? "false" : "true");
        responseProp.put("unsafe_filename_chars", ServerStatus.SG("unsafe_filename_chars"));
        request.put("path", Common.url_decode(request.getProperty("path", "/")));
        String the_dir = request.getProperty("path", "/");
        if (the_dir.equals("")) {
            the_dir = "/";
        }
        if (!the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
            the_dir = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + the_dir.substring(1);
        }
        if ((site_item = this.thisSessionHTTP.thisSession.uVFS.get_item(the_dir, 0)) != null) {
            if (!(request.getProperty("path", "/").equals("/") || request.getProperty("path", "/").equals("") || request.getProperty("path", "/").equals("/ftp/"))) {
                c = null;
                try {
                    c = this.thisSessionHTTP.thisSession.uVFS.getClient(site_item);
                    c.doCommand("CWD " + request.getProperty("path", "/"));
                }
                finally {
                    if (c != null) {
                        c = this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                    }
                }
            }
            try {
                if (ServerStatus.BG("allow_impulse")) {
                    this.getUserInfo("IMPULSEINFO", site_item, the_dir);
                }
            }
            catch (Exception c2) {
                // empty catch block
            }
            try {
                VRL vrl;
                if (this.thisSessionHTTP.thisSession.uiBG("prompt_for_refresh_token") && (vrl = new VRL(site_item.getProperty("url", ""))).getProtocol().toLowerCase().equals("dropbox") && vrl.getPassword().equals("")) {
                    responseProp.put("refresh_token_missing", "true");
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
            }
        }
        responseProp.put("user_name", this.thisSessionHTTP.thisSession.uiSG("user_name"));
        responseProp.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
        responseProp.put("email", this.thisSessionHTTP.thisSession.user.getProperty("email", ""));
        responseProp.put("first_name", this.thisSessionHTTP.thisSession.user.getProperty("first_name", ""));
        responseProp.put("last_name", this.thisSessionHTTP.thisSession.user.getProperty("last_name", ""));
        responseProp.put("phone", this.thisSessionHTTP.thisSession.user.getProperty("phone", ""));
        responseProp.put("subscribe_reverse_notification_event", this.thisSessionHTTP.thisSession.user.getProperty("subscribe_reverse_notification_event", ""));
        responseProp.put("max_threads", (Object)Common.check_protocol(this.thisSessionHTTP.thisSession.uiSG("user_protocol"), this.thisSessionHTTP.thisSession.SG("allowed_protocols")));
        String response = "";
        try {
            Common cfr_ignored_0 = ServerStatus.thisObj.common_code;
            response = Common.getXMLString(responseProp, "userInfo", null);
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
        }
        response = Common.replace_str(response, ">/WebInterface/", ">" + this.thisSessionHTTP.proxy + "WebInterface/");
        return response;
    }

    public String lookupFormField(Properties request) throws Exception {
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
        Vector customForms = (Vector)ServerStatus.server_settings.get("CustomForms");
        if (customForms != null) {
            Properties customForm = null;
            int x = 0;
            while (x < customForms.size()) {
                Properties p = (Properties)customForms.elementAt(x);
                if (p.getProperty("name", "").equals(request.getProperty("form_name"))) {
                    customForm = p;
                    break;
                }
                ++x;
            }
            if (customForm != null) {
                Properties entry = null;
                if (!customForm.containsKey("entries")) {
                    customForm.put("entries", new Vector());
                }
                Vector entries = (Vector)customForm.get("entries");
                int xx = 0;
                while (xx < entries.size()) {
                    Properties p = (Properties)entries.elementAt(xx);
                    if (!p.getProperty("name", "").trim().equals(request.getProperty("from_element_name"))) {
                        entry = p;
                        break;
                    }
                    ++xx;
                }
                if (entry != null) {
                    Vector<Properties> search_entries = new Vector<Properties>();
                    String q = request.getProperty("q", "");
                    Vector<String> search_user_names = new Vector<String>();
                    search_user_names.add(this.thisSessionHTTP.thisSession.uiSG("user_name"));
                    if (this.thisSessionHTTP.thisSession.user_info.containsKey("ldap_role_template_users") && this.thisSessionHTTP.thisSession.user_info.get("ldap_role_template_users") != null) {
                        Vector ldap_role_template_users = (Vector)this.thisSessionHTTP.thisSession.user_info.get("ldap_role_template_users");
                        int x2 = 0;
                        while (x2 < ldap_role_template_users.size()) {
                            search_user_names.add((String)ldap_role_template_users.get(x2));
                            ++x2;
                        }
                    }
                    if (entry.getProperty("lookup_type", "text").trim().equals("")) {
                        entry.put("lookup_type", "text");
                    }
                    if (entry.getProperty("lookup_type", "text").trim().equals("text")) {
                        Properties p;
                        String val;
                        BufferedReader br;
                        int x3 = 0;
                        while (x3 < search_user_names.size()) {
                            String username = (String)search_user_names.get(x3);
                            File_S userText = new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "forms/" + com.crushftp.client.Common.dots(String.valueOf(request.getProperty("form_element_name")) + "_" + username + ".txt"));
                            Log.log("HTTP_SERVER", 2, "Looking for lookup file:" + userText);
                            if (userText.exists()) {
                                br = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(userText), "UTF8"));
                                String data = "";
                                while ((data = br.readLine()) != null) {
                                    String key;
                                    Log.log("HTTP_SERVER", 2, userText + ":Checking data:" + data);
                                    val = key = data.trim();
                                    p = new Properties();
                                    if (data.indexOf(":") >= 0) {
                                        key = data.split(":")[0].trim();
                                        val = data.split(":").length == 1 ? "" : data.split(":")[1].trim();
                                    }
                                    p.put("key", key);
                                    p.put("val", val);
                                    if (key.toUpperCase().indexOf(q.toUpperCase()) < 0) continue;
                                    search_entries.addElement(p);
                                }
                                br.close();
                            }
                            ++x3;
                        }
                        Properties groups = UserTools.getGroups(this.thisSessionHTTP.thisSession.server_item.getProperty("linkedServer"));
                        Enumeration<Object> keys = groups.keys();
                        while (keys.hasMoreElements()) {
                            String group_name = keys.nextElement().toString();
                            Vector v = (Vector)groups.get(group_name);
                            boolean found = false;
                            int x4 = 0;
                            while (x4 < search_user_names.size()) {
                                String username = (String)search_user_names.get(x4);
                                if (v.indexOf(username) >= 0) {
                                    found = true;
                                }
                                ++x4;
                            }
                            if (!found) continue;
                            File_S groupText = new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "forms/" + com.crushftp.client.Common.dots(String.valueOf(request.getProperty("form_element_name")) + "_" + group_name + ".txt"));
                            Log.log("HTTP_SERVER", 2, "Looking for lookup file:" + groupText);
                            if (!groupText.exists()) continue;
                            BufferedReader br2 = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(groupText), "UTF8"));
                            String data = "";
                            while ((data = br2.readLine()) != null) {
                                String key;
                                Log.log("HTTP_SERVER", 2, groupText + ":Checking data:" + data);
                                String val2 = key = data.trim();
                                Properties p2 = new Properties();
                                if (data.indexOf(":") >= 0) {
                                    key = data.split(":")[0].trim();
                                    val2 = data.split(":").length == 1 ? "" : data.split(":")[1].trim();
                                }
                                key = String.valueOf(group_name) + " - " + key;
                                p2.put("key", key);
                                p2.put("val", val2);
                                if (key.toUpperCase().indexOf(q.toUpperCase()) < 0) continue;
                                search_entries.addElement(p2);
                            }
                            br2.close();
                        }
                        File_S globalText = new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "forms/" + com.crushftp.client.Common.dots(String.valueOf(request.getProperty("form_element_name")) + ".txt"));
                        Log.log("HTTP_SERVER", 2, "Looking for lookup file:" + globalText);
                        if (globalText.exists() && search_entries.size() == 0) {
                            br = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(globalText), "UTF8"));
                            String data = "";
                            while ((data = br.readLine()) != null) {
                                String key;
                                Log.log("HTTP_SERVER", 2, globalText + ":Checking data:" + data);
                                val = key = data.trim();
                                p = new Properties();
                                if (data.indexOf(":") >= 0) {
                                    key = data.split(":")[0].trim();
                                    val = data.split(":").length == 1 ? "" : data.split(":")[1].trim();
                                }
                                p.put("key", key);
                                p.put("val", val);
                                if (key.toUpperCase().indexOf(q.toUpperCase()) < 0) continue;
                                search_entries.addElement(p);
                            }
                            br.close();
                        }
                    } else if (entry.getProperty("lookup_type", "").trim().equals("task")) {
                        Properties item = (Properties)entry.clone();
                        item.put("url", "file://" + entry.getProperty("name") + "/" + q);
                        item.put("the_file_name", q);
                        item.put("the_file_path", entry.getProperty("name"));
                        Vector items = new Vector();
                        items.addElement(item);
                        Properties event = new Properties();
                        event.put("event_plugin_list", entry.getProperty("entry_plugin", ""));
                        event.put("name", "FormPlugin:" + request.getProperty("form_name") + ":" + entry.getProperty("entry_plugin", ""));
                        Properties info = new Properties();
                        boolean async = ServerStatus.BG("event_asynch");
                        ServerStatus.server_settings.put("event_async", "false");
                        info = ServerStatus.thisObj.events6.doEventPlugin(info, event, this.thisSessionHTTP.thisSession, items);
                        ServerStatus.server_settings.put("event_async", String.valueOf(async));
                        items = (Vector)info.get("newItems");
                        int x5 = items.size() - 1;
                        while (x5 >= 0) {
                            Properties p = (Properties)items.elementAt(x5);
                            if (!p.getProperty("key", "").toUpperCase().startsWith(q.toUpperCase())) {
                                items.removeElementAt(x5);
                            }
                            --x5;
                        }
                        search_entries.addAll(items);
                    } else if (entry.getProperty("lookup_type", "").trim().equals("plugin")) {
                        try {
                            Object parent = Common.getPlugin(request.getProperty("pluginName"), null, request.getProperty("pluginSubItem", ""));
                            if (parent == null && request.getProperty("pluginSubItem", "").equals("")) {
                                parent = Common.getPlugin(request.getProperty("pluginName"), null, "false");
                            }
                            Method method = parent.getClass().getMethod(request.getProperty("method", "lookupList"), new Properties().getClass());
                            Object o = method.invoke(parent, request);
                            Common cfr_ignored_0 = ServerStatus.thisObj.common_code;
                            response = Common.getXMLString(o, "list", null);
                        }
                        catch (Exception ee) {
                            Log.log("HTTP_SERVER", 1, ee);
                            response = String.valueOf(response) + "Error:" + Common.url_encode(ee.toString());
                        }
                    }
                    Common.do_sort(search_entries, "name", "key");
                    StringBuffer r = new StringBuffer();
                    r.append("[\r\n");
                    int x6 = 0;
                    while (x6 < search_entries.size()) {
                        Properties p = (Properties)search_entries.elementAt(x6);
                        if (x6 > 0) {
                            r.append(",\r\n");
                        }
                        r.append("\t{\"id\":\"" + p.getProperty("val") + "\",\"name\":\"" + p.getProperty("key") + "\"}");
                        ++x6;
                    }
                    r.append("]\r\n");
                    response = r.toString();
                }
            }
        }
        return response;
    }

    public String setUserItem(Properties request, String site) throws Exception {
        String status = "OK";
        try {
            if (site.indexOf("(USER_ADMIN)") < 0) {
                throw new Exception("Access Denied.");
            }
            try {
                Properties info = null;
                if (!this.thisSessionHTTP.thisSession.containsKey("user_admin_info")) {
                    AdminControls.getUserList(request, site, this.thisSessionHTTP.thisSession);
                }
                info = (Properties)this.thisSessionHTTP.thisSession.get("user_admin_info");
                Vector list = (Vector)info.get("list");
                boolean writeGroupsInheritance = false;
                Vector allowed_keys = Common.convertToVector(this.thisSessionHTTP.SG("allowed_config").split(","));
                if (request.getProperty("xmlItem", "").equals("groups")) {
                    String key;
                    String groupName = this.thisSessionHTTP.thisSession.getAdminGroupName(request);
                    Properties groups = (Properties)Common.readXMLObjectError(new ByteArrayInputStream(Common.url_decode(request.getProperty("groups").replace('+', ' ')).getBytes("UTF8")));
                    if (groups == null) {
                        groups = new Properties();
                    }
                    Properties real_groups = UserTools.getGroups(request.getProperty("serverGroup"));
                    Enumeration<Object> keys = real_groups.keys();
                    Vector<String> prior_items = new Vector<String>();
                    while (keys.hasMoreElements()) {
                        key = keys.nextElement().toString();
                        if (!key.toUpperCase().startsWith(String.valueOf(groupName.toUpperCase()) + "_")) continue;
                        prior_items.add(key);
                        real_groups.remove(key);
                    }
                    keys = groups.keys();
                    while (keys.hasMoreElements()) {
                        key = keys.nextElement().toString();
                        real_groups.put(String.valueOf(groupName) + "_" + key, groups.get(key));
                    }
                    UserTools.writeGroups(request.getProperty("serverGroup"), real_groups, true, request);
                } else if (request.getProperty("xmlItem", "").equals("inheritance")) {
                    String groupName = this.thisSessionHTTP.thisSession.getAdminGroupName(request);
                    Properties inheritance = (Properties)Common.readXMLObjectError(new ByteArrayInputStream(Common.url_decode(request.getProperty("inheritance").replace('+', ' ')).getBytes("UTF8")));
                    if (inheritance == null) {
                        inheritance = new Properties();
                    }
                    Enumeration<Object> keys = inheritance.keys();
                    while (keys.hasMoreElements()) {
                        String key = keys.nextElement().toString();
                        if (list.indexOf(key) < 0) {
                            inheritance.remove(key);
                        }
                        Vector sub_users = (Vector)inheritance.get(key);
                        int x = sub_users.size() - 1;
                        while (x >= 0) {
                            String sub_user = sub_users.elementAt(x).toString();
                            if (list.indexOf(sub_user) < 0) {
                                sub_users.remove(x);
                            }
                            if (!request.getProperty("old_username", "").equals("") && request.getProperty("old_username", "").equals(sub_user)) {
                                sub_users.remove(x);
                                sub_users.add(x, request.getProperty("username", ""));
                            }
                            --x;
                        }
                        if (sub_users.size() != 0 && sub_users.elementAt(0).toString().equals(groupName)) continue;
                        sub_users.insertElementAt(groupName, 0);
                    }
                    Properties real_inheritance = UserTools.getInheritance(request.getProperty("serverGroup"));
                    keys = real_inheritance.keys();
                    while (keys.hasMoreElements()) {
                        String key = keys.nextElement().toString();
                        if (list.indexOf(key) < 0 || !inheritance.containsKey(key)) continue;
                        real_inheritance.remove(key);
                    }
                    real_inheritance.putAll((Map<?, ?>)inheritance);
                    UserTools.writeInheritance(request.getProperty("serverGroup"), real_inheritance);
                } else if (request.getProperty("xmlItem", "").equals("user")) {
                    if (request.getProperty("serverGroup_original", "").equals("extra_vfs")) {
                        throw new Exception("Access Denied Extra VFS.");
                    }
                    if (!request.containsKey("usernames")) {
                        request.put("usernames", request.getProperty("username", ""));
                    }
                    String[] usernames = Common.url_decode(request.getProperty("usernames").replace('+', ' ')).split(";");
                    int x = 0;
                    while (x < usernames.length) {
                        String username = usernames[x].trim();
                        if (!username.equals("")) {
                            Properties groups = UserTools.getGroups(request.getProperty("serverGroup"));
                            if (groups == null) {
                                groups = new Properties();
                            }
                            String groupName = this.thisSessionHTTP.thisSession.getAdminGroupName(request);
                            if (UserTools.ut.getUser(request.getProperty("serverGroup"), groupName, false) == null) {
                                throw new Exception("Group template user does not exist! Group name = " + groupName);
                            }
                            Vector<String> group = (Vector<String>)groups.get(groupName);
                            Vector pendingSelfRegistration = (Vector)groups.get("pendingSelfRegistration");
                            if (group == null) {
                                group = new Vector<String>();
                            }
                            groups.put(groupName, group);
                            if (pendingSelfRegistration == null) {
                                pendingSelfRegistration = new Vector();
                                groups.put("pendingSelfRegistration", pendingSelfRegistration);
                            }
                            if (username.equalsIgnoreCase(groupName)) {
                                throw new Exception("You cannot edit this user, it is only for reference.");
                            }
                            Properties inheritance = UserTools.getInheritance(request.getProperty("serverGroup"));
                            if (request.getProperty("data_action").equals("delete")) {
                                if (!this.thisSessionHTTP.thisSession.user.getProperty("allowed_config", "").equals("") && !allowed_keys.contains("delete_user")) {
                                    throw new Exception("Access Denied: Delete user.");
                                }
                                if (!username.equalsIgnoreCase("default") || info.getProperty("default_edittable", "false").equals("true")) {
                                    Properties user;
                                    if (list.indexOf(username) < 0) {
                                        throw new Exception("Username " + username + " not found.");
                                    }
                                    if (request.getProperty("expire_user", "false").equals("true") && (user = UserTools.ut.getUser(request.getProperty("serverGroup"), username, true)) != null) {
                                        UserTools.expireUserVFSTask(user, request.getProperty("serverGroup"), username);
                                    }
                                    UserTools.deleteUser(request.getProperty("serverGroup"), username);
                                    group.remove(username);
                                    pendingSelfRegistration.remove(username);
                                    inheritance.remove(username);
                                    UserTools.writeGroups(request.getProperty("serverGroup"), groups);
                                    UserTools.writeInheritance(request.getProperty("serverGroup"), inheritance);
                                }
                            } else {
                                String linked_user;
                                boolean vfs_no_overwrite;
                                int xx;
                                Properties user;
                                if (request.getProperty("data_action").equals("new") && !this.thisSessionHTTP.thisSession.user.getProperty("allowed_config", "").equals("") && !allowed_keys.contains("create_user")) {
                                    throw new Exception("Access Denied: User creation.");
                                }
                                Properties new_user = null;
                                if (request.containsKey("user")) {
                                    new_user = (Properties)Common.readXMLObject(new ByteArrayInputStream(Common.url_decode(request.getProperty("user").replace('+', ' ')).getBytes("UTF8")));
                                } else {
                                    new_user = UserTools.ut.getUser(request.getProperty("serverGroup"), username, false);
                                    new_user.remove("password");
                                    new_user.put("skip_save", "true");
                                }
                                if (new_user.containsKey("password")) {
                                    String pass;
                                    if (!this.thisSessionHTTP.thisSession.user.getProperty("allowed_config", "").equals("") && !allowed_keys.contains("password")) {
                                        new_user.put("password", "");
                                    } else if (!(new_user.getProperty("password").equals("SHA3:XXXXXXXXXXXXXXXXXXXX") || (pass = new_user.getProperty("password", "")).startsWith("SHA:") || pass.startsWith("SHA512:") || pass.startsWith("SHA256:") || pass.startsWith("SHA3:") || pass.startsWith("MD5:") || pass.startsWith("CRYPT3:") || pass.startsWith("BCRYPT:") || pass.startsWith("MD5CRYPT:") || pass.startsWith("PBKDF2SHA256:") || pass.startsWith("SHA512CRYPT:") || pass.startsWith("ARGOND:"))) {
                                        pass = ServerStatus.thisObj.common_code.encode_pass(pass, ServerStatus.SG("password_encryption"), new_user.getProperty("salt", ""));
                                        new_user.put("password", pass);
                                    }
                                }
                                if ((user = UserTools.ut.getUser(request.getProperty("serverGroup"), username, false)) != null && user.getProperty("username", "").equalsIgnoreCase("TEMPLATE")) {
                                    user = null;
                                }
                                if (user == null) {
                                    new_user.put("created_time", String.valueOf(System.currentTimeMillis()));
                                    new_user.put("created_by_username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                    new_user.put("created_by_email", this.thisSessionHTTP.thisSession.uiSG("user_email"));
                                } else {
                                    if (new_user.containsKey("password") && new_user.getProperty("password").equals("SHA3:XXXXXXXXXXXXXXXXXXXX")) {
                                        new_user.put("password", user.getProperty("password", ""));
                                    }
                                    new_user.put("created_time", user.getProperty("created_time", "0"));
                                    new_user.put("created_by_username", user.getProperty("created_by_username", ""));
                                    new_user.put("created_by_email", user.getProperty("created_by_email", ""));
                                }
                                if (!this.thisSessionHTTP.thisSession.user.getProperty("allowed_config", "").equals("")) {
                                    Enumeration<Object> keys = new_user.keys();
                                    while (keys.hasMoreElements()) {
                                        boolean ok = false;
                                        String key = keys.nextElement().toString();
                                        xx = 0;
                                        while (xx < allowed_keys.size()) {
                                            String allowed_key = allowed_keys.get(xx).toString().trim();
                                            if (key.equals("linked_vfs") && allowed_key.equalsIgnoreCase("VFS_LINKING")) {
                                                ok = true;
                                            }
                                            if (key.equals("allowed_protocols") && allowed_key.equalsIgnoreCase("CONNECTIONS_PER_PROTOCOL")) {
                                                ok = true;
                                            }
                                            if (allowed_key.equalsIgnoreCase(key)) {
                                                ok = true;
                                            }
                                            if (key.equals("password") && user == null) {
                                                ok = true;
                                            }
                                            if (key.equals("root_dir") && allowed_key.equals("VFS")) {
                                                ok = true;
                                            }
                                            ++xx;
                                        }
                                        if (ok || key.startsWith("created_")) continue;
                                        if (user != null && user.get(key) != null) {
                                            new_user.put(key, user.get(key));
                                            continue;
                                        }
                                        new_user.remove(key);
                                    }
                                    new_user.remove("allowed_config");
                                }
                                if (!new_user.containsKey("root_dir")) {
                                    new_user.put("root_dir", "/");
                                }
                                boolean bl = vfs_no_overwrite = !ServerStatus.BG("limited_admin_vfs_overwrite");
                                if (!this.thisSessionHTTP.thisSession.user.getProperty("allowed_config", "").equals("")) {
                                    vfs_no_overwrite &= !this.thisSessionHTTP.SG("allowed_config").toUpperCase().contains("PASS") && !this.thisSessionHTTP.SG("allowed_config").toUpperCase().contains("SALT") && !this.thisSessionHTTP.SG("allowed_config").toUpperCase().contains("EMAIL") && !this.thisSessionHTTP.SG("allowed_config").toUpperCase().contains("SITE");
                                }
                                new_user.put("userVersion", "6");
                                Vector linked_vfs = (Vector)new_user.get("linked_vfs");
                                Vector org_linked_vfs = new Vector();
                                if (user != null && user.containsKey("linked_vfs") && user.get("linked_vfs") != null) {
                                    org_linked_vfs = (Vector)user.get("linked_vfs");
                                }
                                xx = 0;
                                while (linked_vfs != null && xx < linked_vfs.size()) {
                                    linked_user = linked_vfs.elementAt(xx).toString().trim();
                                    if (!(linked_vfs.elementAt(xx).toString().equals("null") || list.indexOf(linked_user) >= 0 || vfs_no_overwrite && org_linked_vfs.contains(linked_user))) {
                                        throw new Exception("Linked_VFS username " + linked_user + " not found.");
                                    }
                                    ++xx;
                                }
                                if (vfs_no_overwrite) {
                                    int xxx = 0;
                                    while (xxx < org_linked_vfs.size()) {
                                        linked_user = org_linked_vfs.elementAt(xxx).toString().trim();
                                        if (!linked_vfs.contains(linked_user) && list.indexOf(linked_user) < 0) {
                                            linked_vfs.add(linked_user);
                                        }
                                        ++xxx;
                                    }
                                }
                                VFS tempVFS = AdminControls.processVFSSubmission(request, username, site, this.thisSessionHTTP.thisSession, false, null);
                                new_user.put("updated_by_username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
                                new_user.put("updated_by_email", this.thisSessionHTTP.thisSession.uiSG("user_email"));
                                new_user.put("updated_time", String.valueOf(System.currentTimeMillis()));
                                if (request.getProperty("data_action").equals("update")) {
                                    if (list.indexOf(username) < 0) {
                                        throw new Exception("Username " + username + " not found.");
                                    }
                                    Common.updateObjectLog(new_user, user, null);
                                    new_user = user;
                                    if (pendingSelfRegistration.indexOf(username) >= 0) {
                                        pendingSelfRegistration.remove(username);
                                        if (group.indexOf(username) < 0) {
                                            group.addElement(username);
                                        }
                                        Vector<String> vv = new Vector<String>();
                                        vv.addElement(groupName);
                                        inheritance.put(username, vv);
                                        writeGroupsInheritance = true;
                                    }
                                } else if (user != null) {
                                    if (request.getProperty("serverGroup_original", "").equals("extra_vfs") ? list.indexOf(username.substring(0, username.lastIndexOf("~"))) < 0 : list.indexOf(username) < 0) {
                                        throw new Exception("Username " + username + " not found.");
                                    }
                                    if (username.trim().equalsIgnoreCase("default") && !info.getProperty("default_edittable", "false").equals("true")) {
                                        throw new Exception("This user is for reference only.  You cannot edit this user.");
                                    }
                                    if (pendingSelfRegistration.indexOf(username) >= 0) {
                                        pendingSelfRegistration.remove(username);
                                        if (group.indexOf(username) < 0) {
                                            group.addElement(username);
                                        }
                                        Vector<String> vv = new Vector<String>();
                                        vv.addElement(groupName);
                                        inheritance.put(username, vv);
                                        writeGroupsInheritance = true;
                                    }
                                } else if (!request.getProperty("serverGroup_original", "").equals("extra_vfs")) {
                                    Vector<String> vv = new Vector<String>();
                                    vv.addElement(groupName);
                                    inheritance.put(username, vv);
                                    pendingSelfRegistration.remove(username);
                                    if (group.indexOf(username) < 0) {
                                        group.addElement(username);
                                    }
                                    writeGroupsInheritance = true;
                                    if (list.indexOf(username) < 0) {
                                        list.addElement(username);
                                    }
                                }
                                if (UserTools.testLimitedAdminAccess(new_user, groupName, request.getProperty("serverGroup"))) {
                                    Properties email_info;
                                    String bcc;
                                    String cc;
                                    String body;
                                    Properties old_user;
                                    Properties template;
                                    if (!new_user.getProperty("skip_save", "").equals("true")) {
                                        Properties user_tmp;
                                        UserTools.writeUser(request.getProperty("serverGroup"), username, new_user);
                                        if (!request.getProperty("old_username", "").equals("")) {
                                            Enumeration<Object> keys = inheritance.keys();
                                            while (keys.hasMoreElements()) {
                                                Vector parents = (Vector)inheritance.get(keys.nextElement().toString());
                                                if (!parents.contains(request.getProperty("old_username", ""))) continue;
                                                parents.remove(request.getProperty("old_username", ""));
                                                parents.add(username);
                                                writeGroupsInheritance = true;
                                            }
                                        }
                                        if ((user_tmp = UserTools.ut.getUser(request.getProperty("serverGroup"), username, true)).getProperty("site", "").toUpperCase().indexOf("(USER_ADMIN)") >= 0) {
                                            new_user.put("site", Common.replace_str(user_tmp.getProperty("site").toUpperCase(), "(USER_ADMIN)", ""));
                                            UserTools.writeUser(request.getProperty("serverGroup"), username, new_user);
                                        }
                                    }
                                    if (request.containsKey("vfs_items") || request.containsKey("permissions")) {
                                        Properties virtual = (Properties)tempVFS.homes.elementAt(0);
                                        Enumeration<Object> keys = virtual.keys();
                                        boolean ok = true;
                                        while (keys.hasMoreElements() && ok) {
                                            Properties vfs_item;
                                            String key = keys.nextElement().toString();
                                            if (key.equals("vfs_permissions_object") || (vfs_item = (Properties)virtual.get(key)).getProperty("type").equalsIgnoreCase("DIR") || (ok = UserTools.testLimitedAdminAccess(vfs_item.get("vItems"), groupName, request.getProperty("serverGroup_backup", request.getProperty("serverGroup"))))) continue;
                                            Log.log("HTTP_SERVER", 0, new Date() + ":User " + this.thisSessionHTTP.thisSession.uiSG("user_name") + " Violated Security Constraint for a USER_ADMIN:" + groupName);
                                        }
                                        if (ok) {
                                            if (vfs_no_overwrite) {
                                                Properties org_virtual = UserTools.ut.getVirtualVFS(request.getProperty("serverGroup"), username);
                                                Enumeration<Object> org_key = org_virtual.keys();
                                                while (org_key.hasMoreElements()) {
                                                    Properties vfs_item;
                                                    String key = org_key.nextElement().toString();
                                                    if (key.equals("vfs_permissions_object") || key.equals("/") || UserTools.testLimitedAdminAccess((vfs_item = (Properties)org_virtual.get(key)).get("vItems"), groupName, request.getProperty("serverGroup_backup", request.getProperty("serverGroup")))) continue;
                                                    virtual.put(key, org_virtual.get(key));
                                                }
                                            }
                                            UserTools.writeVFS(request.getProperty("serverGroup"), username, tempVFS);
                                        }
                                    }
                                    if (writeGroupsInheritance) {
                                        UserTools.writeGroups(request.getProperty("serverGroup"), groups);
                                        UserTools.writeInheritance(request.getProperty("serverGroup"), inheritance);
                                    }
                                    if ((template = Common.get_email_template("Change Email")) != null && (old_user = UserTools.ut.getUser(request.getProperty("serverGroup"), username, false)) != null && new_user != null && !old_user.getProperty("email", "").equals(new_user.getProperty("email", ""))) {
                                        String body2 = template.getProperty("emailBody");
                                        body2 = Common.replace_str(body2, "{old_email}", old_user.getProperty("email"));
                                        body2 = Common.replace_str(body2, "{new_email}", new_user.getProperty("email"));
                                        String subject = template.getProperty("emailSubject");
                                        subject = Common.replace_str(subject, "{old_email}", old_user.getProperty("email"));
                                        subject = Common.replace_str(subject, "{new_email}", new_user.getProperty("email"));
                                        new_user.put("username", username);
                                        new_user.put("user_name", username);
                                        body2 = ServerStatus.change_vars_to_values_static(body2, new_user, new_user, null);
                                        subject = ServerStatus.change_vars_to_values_static(subject, new_user, new_user, null);
                                        String cc2 = ServerStatus.change_vars_to_values_static(template.getProperty("emailCC"), new_user, new_user, null);
                                        String bcc2 = ServerStatus.change_vars_to_values_static(template.getProperty("emailBCC"), new_user, new_user, null);
                                        final Properties email_info2 = new Properties();
                                        email_info2.put("server", ServerStatus.SG("smtp_server"));
                                        email_info2.put("user", ServerStatus.SG("smtp_user"));
                                        email_info2.put("pass", ServerStatus.SG("smtp_pass"));
                                        email_info2.put("ssl", ServerStatus.SG("smtp_ssl"));
                                        email_info2.put("html", ServerStatus.SG("smtp_html"));
                                        email_info2.put("from", template.getProperty("emailFrom"));
                                        email_info2.put("reply_to", template.getProperty("emailReplyTo"));
                                        email_info2.put("to", String.valueOf(new_user.getProperty("email")) + "," + old_user.getProperty("email"));
                                        email_info2.put("cc", cc2);
                                        email_info2.put("bcc", bcc2);
                                        email_info2.put("subject", subject);
                                        email_info2.put("body", body2);
                                        Worker.startWorker(new Runnable(){

                                            @Override
                                            public void run() {
                                                try {
                                                    ServerStatus.thisObj.sendEmail(email_info2);
                                                }
                                                catch (Exception e) {
                                                    Log.log("SERVER", 1, e);
                                                }
                                            }
                                        }, "Send Change Email");
                                    }
                                    if ((template = Common.get_email_template("Change User Email")) != null) {
                                        body = template.getProperty("emailBody");
                                        body = Common.replace_str(body, "{new_email}", new_user.getProperty("email"));
                                        String subject = template.getProperty("emailSubject");
                                        subject = Common.replace_str(subject, "{new_email}", new_user.getProperty("email"));
                                        new_user.put("username", username);
                                        new_user.put("user_name", username);
                                        body = ServerStatus.change_vars_to_values_static(body, new_user, new_user, null);
                                        subject = ServerStatus.change_vars_to_values_static(subject, new_user, new_user, null);
                                        cc = ServerStatus.change_vars_to_values_static(template.getProperty("emailCC"), new_user, new_user, null);
                                        bcc = ServerStatus.change_vars_to_values_static(template.getProperty("emailBCC"), new_user, new_user, null);
                                        email_info = new Properties();
                                        email_info.put("server", ServerStatus.SG("smtp_server"));
                                        email_info.put("user", ServerStatus.SG("smtp_user"));
                                        email_info.put("pass", ServerStatus.SG("smtp_pass"));
                                        email_info.put("ssl", ServerStatus.SG("smtp_ssl"));
                                        email_info.put("html", ServerStatus.SG("smtp_html"));
                                        email_info.put("from", template.getProperty("emailFrom"));
                                        email_info.put("reply_to", template.getProperty("emailReplyTo"));
                                        email_info.put("to", cc);
                                        email_info.put("cc", cc);
                                        email_info.put("bcc", bcc);
                                        email_info.put("subject", subject);
                                        email_info.put("body", body);
                                        Worker.startWorker(new Runnable(){

                                            @Override
                                            public void run() {
                                                try {
                                                    ServerStatus.thisObj.sendEmail(email_info);
                                                }
                                                catch (Exception e) {
                                                    Log.log("SERVER", 1, e);
                                                }
                                            }
                                        }, "Send Change User Email");
                                    }
                                    if (request.getProperty("data_action").equals("new") && (template = Common.get_email_template("New User Email")) != null) {
                                        body = template.getProperty("emailBody");
                                        body = Common.replace_str(body, "{user_email}", new_user.getProperty("email"));
                                        String subject = template.getProperty("emailSubject");
                                        subject = Common.replace_str(subject, "{user_email}", new_user.getProperty("email"));
                                        new_user.put("username", username);
                                        new_user.put("user_name", username);
                                        body = ServerStatus.change_vars_to_values_static(body, new_user, new_user, null);
                                        subject = ServerStatus.change_vars_to_values_static(subject, new_user, new_user, null);
                                        cc = ServerStatus.change_vars_to_values_static(template.getProperty("emailCC"), new_user, new_user, null);
                                        bcc = ServerStatus.change_vars_to_values_static(template.getProperty("emailBCC"), new_user, new_user, null);
                                        email_info = new Properties();
                                        email_info.put("server", ServerStatus.SG("smtp_server"));
                                        email_info.put("user", ServerStatus.SG("smtp_user"));
                                        email_info.put("pass", ServerStatus.SG("smtp_pass"));
                                        email_info.put("ssl", ServerStatus.SG("smtp_ssl"));
                                        email_info.put("html", ServerStatus.SG("smtp_html"));
                                        email_info.put("from", template.getProperty("emailFrom"));
                                        email_info.put("reply_to", template.getProperty("emailReplyTo"));
                                        email_info.put("to", cc);
                                        email_info.put("cc", cc);
                                        email_info.put("bcc", bcc);
                                        email_info.put("subject", subject);
                                        email_info.put("body", body);
                                        Worker.startWorker(new Runnable(){

                                            @Override
                                            public void run() {
                                                try {
                                                    ServerStatus.thisObj.sendEmail(email_info);
                                                }
                                                catch (Exception e) {
                                                    Log.log("SERVER", 1, e);
                                                }
                                            }
                                        }, "Send New User Email");
                                    }
                                    UserTools.ut.getUser(request.getProperty("serverGroup"), username, true);
                                    UserTools.ut.forceMemoryReload(username);
                                } else {
                                    String msg = ":User " + this.thisSessionHTTP.thisSession.uiSG("user_name") + " Violated Security Constraint for a USER_ADMIN:" + groupName;
                                    Log.log("HTTP_SERVER", 0, new Date() + msg);
                                    throw new Exception(msg);
                                }
                            }
                        }
                        ++x;
                    }
                } else {
                    status = "OK";
                }
            }
            catch (Exception e) {
                Log.log("HTTP_SERVER", 1, e);
                status = "FAILURE:" + e.getMessage();
            }
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            status = e.toString();
        }
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
        response = String.valueOf(response) + "<result><response_status>" + status + "</response_status></result>";
        return response;
    }

    public static String changePassword(Properties request, String site, SessionCrush thisSession) {
        String result = "Not Allowed";
        if (site.indexOf("(SITE_PASS)") >= 0) {
            String current_password_salt;
            if (thisSession.uiBG("no_password_change_on_user")) {
                return result;
            }
            String current_password = Common.url_decode(request.getProperty("current_password", "")).trim();
            String new_password1 = Common.url_decode(request.getProperty("new_password1", "")).trim();
            String new_password2 = Common.url_decode(request.getProperty("new_password2", "")).trim();
            String new_password3 = new_password1;
            String user_password = ServerStatus.thisObj.common_code.decode_pass(thisSession.user.getProperty("password"));
            String string = current_password_salt = thisSession.user.getProperty("salt", "").equals("random") ? "" : thisSession.user.getProperty("salt", "");
            if (user_password.startsWith("MD5:")) {
                current_password = ServerStatus.thisObj.common_code.encode_pass(current_password, "MD5", current_password_salt);
                new_password3 = ServerStatus.thisObj.common_code.encode_pass(new_password3, "MD5", thisSession.user.getProperty("salt", ""));
            } else if (user_password.startsWith("SHA:")) {
                current_password = ServerStatus.thisObj.common_code.encode_pass(current_password, "SHA", current_password_salt);
                new_password3 = ServerStatus.thisObj.common_code.encode_pass(new_password3, "SHA", thisSession.user.getProperty("salt", ""));
            } else if (user_password.startsWith("SHA512:")) {
                current_password = ServerStatus.thisObj.common_code.encode_pass(current_password, "SHA512", current_password_salt);
                new_password3 = ServerStatus.thisObj.common_code.encode_pass(new_password3, "SHA512", thisSession.user.getProperty("salt", ""));
            } else if (user_password.startsWith("ARGOND:")) {
                current_password = ServerStatus.thisObj.common_code.encode_pass(current_password, ServerStatus.SG("password_encryption"), current_password_salt);
                new_password3 = ServerStatus.thisObj.common_code.encode_pass(new_password3, ServerStatus.SG("password_encryption"), thisSession.user.getProperty("salt", ""));
            } else if (user_password.startsWith("SHA256:")) {
                current_password = ServerStatus.thisObj.common_code.encode_pass(current_password, "SHA256", current_password_salt);
                new_password3 = ServerStatus.thisObj.common_code.encode_pass(new_password3, "SHA256", thisSession.user.getProperty("salt", ""));
            } else if (user_password.startsWith("SHA3:")) {
                current_password = ServerStatus.thisObj.common_code.encode_pass(current_password, "SHA3", current_password_salt);
                new_password3 = ServerStatus.thisObj.common_code.encode_pass(new_password3, "SHA3", thisSession.user.getProperty("salt", ""));
            } else if (user_password.startsWith("CRYPT3:")) {
                if (ServerStatus.thisObj.common_code.crypt3(current_password, user_password).equals(user_password)) {
                    current_password = user_password;
                }
            } else if (user_password.startsWith("BCRYPT:")) {
                if (ServerStatus.thisObj.common_code.bcrypt(current_password, user_password).equals(user_password)) {
                    current_password = user_password;
                }
            } else if (user_password.startsWith("MD5CRYPT:")) {
                if (ServerStatus.thisObj.common_code.md5crypt(current_password, user_password).equals(user_password)) {
                    current_password = user_password;
                }
            } else if (user_password.startsWith("PBKDF2SHA256:")) {
                if (ServerStatus.thisObj.common_code.pbkdf2sha256(current_password, user_password).equals(user_password)) {
                    current_password = user_password;
                }
            } else if (user_password.startsWith("SHA512CRYPT:")) {
                if (ServerStatus.thisObj.common_code.sha512crypt(current_password, user_password, 0).equals(user_password)) {
                    current_password = user_password;
                } else if (ServerStatus.thisObj.common_code.sha512crypt(current_password, user_password, 5000).equals(user_password)) {
                    current_password = user_password;
                }
            }
            if (current_password.length() > 0 && new_password1.length() > 0) {
                if (current_password.equals(user_password) && new_password1.equals(new_password2) && !new_password3.equals(user_password) && !thisSession.uiSG("user_name").equalsIgnoreCase("anonymous")) {
                    thisSession.uiPUT("current_password", user_password);
                    result = thisSession.do_ChangePass(thisSession.uiSG("user_name"), new_password1);
                } else if (!current_password.equals(user_password)) {
                    result = LOC.G("You did not enter the correct current password.");
                } else if (!new_password1.equals(new_password2)) {
                    result = LOC.G("You did not enter the same password for verification the second time.");
                }
            }
        }
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
        response = String.valueOf(response) + "<commandResult><response>" + result + "</response></commandResult>";
        return response;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void processHTML5Upload(WebTransfer transfer_lock, SessionCrush thisSession) throws Exception {
        Properties html5_transfers = ServerStatus.siPG("html5_transfers");
        html5_transfers.put(String.valueOf(thisSession.uiSG("user_protocol")) + thisSession.uiSG("user_name") + thisSession.uiSG("user_ip") + "_" + transfer_lock.getVal("transfer_id"), transfer_lock);
        STOR_handler stor2 = null;
        try {
            long pos = Long.parseLong(transfer_lock.removeObj("start_resume_loc").toString());
            Properties metaInfo = (Properties)transfer_lock.removeObj("metaInfo");
            Properties result = ServerSessionHTTP.getStorOutputStream(thisSession, thisSession.getStandardizedDir(transfer_lock.getVal("transfer_path")), pos, false, metaInfo);
            OutputStream of_stream = (OutputStream)result.remove("out");
            transfer_lock.putAllObj(result);
            String base_name = "processHTML5Upload:" + transfer_lock;
            Thread.currentThread().setName(base_name);
            stor2 = (STOR_handler)result.remove("stor");
            Properties active = (Properties)result.get("active");
            transfer_lock.putObj("stor", stor2);
            long transfer_size = Long.parseLong(transfer_lock.removeObj("transfer_size").toString());
            int num = 1;
            int end_loops = 0;
            long last_chunk_time1 = System.currentTimeMillis();
            long last_chunk_time2 = System.currentTimeMillis();
            int last_transfer_lock_size = 0;
            while (end_loops < 120) {
                try {
                    String msg;
                    if (!transfer_lock.hasChunk(String.valueOf(num))) {
                        if (transfer_lock.hasChunk("total_chunks") || num < 20) {
                            Thread.sleep(10L);
                        } else {
                            Thread.sleep(500L);
                            Log.log("HTTP_SERVER", 2, "Waiting for disk/memory chunk :" + transfer_lock.getVal("transfer_id") + ":" + num);
                        }
                        if (transfer_lock.hasObj("total_chunks")) {
                            ++end_loops;
                        }
                        if (last_transfer_lock_size != transfer_lock.getChunkCount()) {
                            last_chunk_time2 = System.currentTimeMillis();
                            last_transfer_lock_size = transfer_lock.getChunkCount();
                        }
                    } else {
                        end_loops = 0;
                        byte[] b = null;
                        int chunk_num = num++;
                        Log.log("HTTP_SERVER", 2, "Got disk/memory chunk " + transfer_lock.getVal("transfer_id") + ":" + chunk_num);
                        if (transfer_lock.getChunk(String.valueOf(chunk_num)) instanceof String) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            String src_file = "" + transfer_lock.removeChunk(String.valueOf(chunk_num));
                            Common.streamCopier(new FileInputStream(new File_S(src_file)), baos, false, true, true);
                            b = baos.toByteArray();
                            new File_S(src_file).delete();
                            Log.log("HTTP_SERVER", 2, "Reading chunk from disk :" + src_file + ":" + b.length);
                        } else {
                            b = (byte[])transfer_lock.removeChunk(String.valueOf(chunk_num));
                            Log.log("HTTP_SERVER", 2, "Reading chunk from memory :" + chunk_num + ":" + b.length);
                        }
                        of_stream.write(b);
                        pos += (long)b.length;
                        last_chunk_time1 = System.currentTimeMillis();
                        last_chunk_time2 = System.currentTimeMillis();
                        transfer_lock.putObj("current_num", String.valueOf(num));
                        Thread.currentThread().setName(String.valueOf(base_name) + ":pending_bytes=" + transfer_lock.getBytes() + ", current_num=" + num);
                    }
                    if (System.currentTimeMillis() - last_chunk_time1 > (long)(ServerStatus.IG("html5_chunk_timeout") * 1000)) {
                        msg = "Transfer failure! " + transfer_lock.getVal("transfer_path") + ": + Timeout1 waiting for chunk from browser chunked transferer:1=" + (System.currentTimeMillis() - last_chunk_time1) + ":2=" + (System.currentTimeMillis() - last_chunk_time2) + ":num=" + num + ":id=" + transfer_lock.getVal("transfer_id");
                        Log.log("SERVER", 0, msg);
                        throw new IOException(msg);
                    }
                    if (System.currentTimeMillis() - last_chunk_time2 > 1200000L) {
                        msg = "Transfer failure! " + transfer_lock.getVal("transfer_path") + ": + Timeout2 waiting for chunk from browser chunked transferer:1=" + (System.currentTimeMillis() - last_chunk_time1) + ":2=" + (System.currentTimeMillis() - last_chunk_time2) + ":num=" + num + ":id=" + transfer_lock.getVal("transfer_id");
                        Log.log("SERVER", 0, msg);
                        throw new IOException(msg);
                    }
                    if (thisSession.getProperty("blockUploads", "false").equals("true")) {
                        throw new Exception("Transfer failed: User Cancelled");
                    }
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                    stor2.inError = true;
                    if (stor2.stop_message != null && !stor2.stop_message.equals("")) break;
                    stor2.stop_message = "ERROR:" + e.getMessage();
                    break;
                }
                try {
                    if (!transfer_lock.hasObj("total_chunks") || num != Integer.parseInt(transfer_lock.getVal("total_chunks", "-1")) + 1) continue;
                    break;
                }
                catch (Exception e) {
                    Log.log("SERVER", 0, e);
                }
            }
            transfer_lock.removeAllChunks();
            if (transfer_lock.hasObj("lastModified")) {
                try {
                    long fileModifiedDate;
                    stor2.fileModifiedDate = fileModifiedDate = Long.parseLong(transfer_lock.getVal("lastModified", "0"));
                }
                catch (Exception e) {
                    Log.log("SERVER", 2, e);
                }
            }
            try {
                of_stream.close();
            }
            catch (IOException e1) {
                Log.log("SERVER", 0, e1);
            }
            if (transfer_size > 0L && pos < transfer_size && !stor2.inError) {
                String msg = "ERROR: Transfer failure! " + transfer_lock.getVal("transfer_path") + ":" + "Transfer size mismatch:(" + num + ")" + pos + " != " + transfer_size + ":" + transfer_lock.getVal("transfer_id");
                Log.log("SERVER", 0, msg);
                stor2.inError = true;
                stor2.stop_message = msg;
            }
            try {
                int loops = 0;
                while (active.getProperty("active", "").equals("true")) {
                    Thread.sleep(loops++ < 100 ? loops : 100);
                }
                stor2.c.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
            transfer_lock.putObj("md5", stor2.getLastMd5Path(transfer_lock.getVal("transfer_path")).toLowerCase());
            if (end_loops >= 120) {
                transfer_lock.putObj("status", "ERROR:End of file not received:" + num + " != " + transfer_lock.getVal("total_chunks"));
            } else {
                transfer_lock.putObj("status", stor2.stop_message.equals("") ? "" : "ERROR:" + stor2.stop_message);
            }
            if (thisSession.uiPG("lastUploadStat") != null) {
                ServerStatus.thisObj.statTools.insertMetaInfo(thisSession.uiSG("SESSION_RID"), metaInfo, thisSession.uiPG("lastUploadStat").getProperty("TRANSFER_RID"));
            }
        }
        catch (Throwable throwable) {
            if (stor2 != null && !stor2.stop_message.equals("")) {
                Thread.sleep(20000L);
            }
            SessionCrush sessionCrush = thisSession;
            synchronized (sessionCrush) {
                Properties html5_transfers_session = (Properties)thisSession.get("html5_transfers_session");
                if (html5_transfers_session == null) {
                    html5_transfers_session = new Properties();
                }
                thisSession.put("html5_transfers_session", html5_transfers_session);
                html5_transfers_session.remove(transfer_lock.getVal("transfer_path"));
            }
            html5_transfers.remove(String.valueOf(thisSession.uiSG("user_protocol")) + thisSession.uiSG("user_name") + thisSession.uiSG("user_ip") + "_" + transfer_lock.getVal("transfer_id"));
            throw throwable;
        }
        if (stor2 != null && !stor2.stop_message.equals("")) {
            Thread.sleep(20000L);
        }
        SessionCrush sessionCrush = thisSession;
        synchronized (sessionCrush) {
            Properties html5_transfers_session = (Properties)thisSession.get("html5_transfers_session");
            if (html5_transfers_session == null) {
                html5_transfers_session = new Properties();
            }
            thisSession.put("html5_transfers_session", html5_transfers_session);
            html5_transfers_session.remove(transfer_lock.getVal("transfer_path"));
        }
        html5_transfers.remove(String.valueOf(thisSession.uiSG("user_protocol")) + thisSession.uiSG("user_name") + thisSession.uiSG("user_ip") + "_" + transfer_lock.getVal("transfer_id"));
        if (!ServerStatus.SG("http_chunk_temp_storage").equals("")) {
            Vector list = new Vector();
            String tmp = ServerStatus.SG("http_chunk_temp_storage");
            if (!tmp.endsWith("/")) {
                tmp = String.valueOf(tmp) + "/";
            }
            Common.getAllFileListing(list, tmp, 5, false);
            int x = 0;
            while (x < list.size()) {
                File_S f = (File_S)list.elementAt(x);
                if (f.getName().startsWith(transfer_lock.getVal("transfer_id")) && f.getName().endsWith(".tmp")) {
                    f.delete();
                } else if (f.getName().endsWith(".tmp") && f.lastModified() < System.currentTimeMillis() - 0x6DDD00L) {
                    f.delete();
                }
                ++x;
            }
        }
        stor2.kill();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    public static void processHTML5Download(WebTransfer transfer_lock, SessionCrush thisSession) throws Exception {
        transfer_lock.putObj("status", "");
        pos = Long.parseLong(transfer_lock.removeObj("start_resume_loc").toString());
        metaInfo = (Properties)transfer_lock.removeObj("metaInfo");
        result = ServerSessionHTTP.getRetrInputStream(thisSession, thisSession.getStandardizedDir(transfer_lock.getVal("transfer_path")), pos, metaInfo);
        if_stream = (InputStream)result.remove("in");
        retr2 = (RETR_handler)result.remove("retr");
        active = (Properties)result.get("active");
        transfer_size = Long.parseLong(transfer_lock.removeObj("transfer_size").toString());
        html5_transfers = ServerStatus.siPG("html5_transfers");
        html5_transfers.put(String.valueOf(thisSession.getId()) + "_" + transfer_lock.getVal("transfer_id"), transfer_lock);
        base_name = "processHTML5Download:" + transfer_lock;
        Thread.currentThread().setName(base_name);
        num = 1;
        end_loops = 0;
        last_chunk_time1 = System.currentTimeMillis();
        last_chunk_time2 = System.currentTimeMillis();
        last_transfer_lock_size = 0;
        baos = new ByteArrayOutputStream();
        chunk_size = 0x100000;
        b = new byte[32768];
        html5_max_pending_download_chunks = ServerStatus.IG("html5_max_pending_download_chunks");
        max_chunk_memory = 0x100000L * ServerStatus.LG("html5_max_pending_download_mb");
        block13: while (true) {
            block32: {
                try {
                    loops = 1;
                    start_wait = System.currentTimeMillis();
                    while (transfer_lock.getChunkCount() > html5_max_pending_download_chunks || transfer_lock.getChunkCount() > 2 && Integer.parseInt(System.getProperty("crushftp.ram_used_percent", "0")) > 60 || transfer_lock.getBytes() > max_chunk_memory) {
                        if (last_transfer_lock_size != transfer_lock.getChunkCount()) {
                            last_chunk_time2 = System.currentTimeMillis();
                            last_transfer_lock_size = transfer_lock.getChunkCount();
                        }
                        Thread.sleep(loops);
                        if (loops < 100) {
                            ++loops;
                        }
                        if (System.currentTimeMillis() - start_wait > 300000L) {
                            throw new IOException("Timeout waiting for client to download..." + transfer_lock.getVal("transfer_path"));
                        }
                        Thread.currentThread().setName(String.valueOf(base_name) + ":pending_bytes=" + transfer_lock.getBytes() + ", current_num=" + num + ", artificial delay:" + (System.currentTimeMillis() - start_wait) + "ms, transfer_lock size=" + transfer_lock.getChunkCount() + " Ram:" + Integer.parseInt(System.getProperty("crushftp.ram_used_percent", "0")) + "%");
                        if (retr2.inError || transfer_lock.getVal("status", "").startsWith("ERROR:")) break;
                    }
                    if (retr2.inError || transfer_lock.getVal("status", "").startsWith("ERROR:")) break;
                    bytes_read = if_stream.read(b);
                    if (bytes_read >= 0) {
                        baos.write(b, 0, bytes_read);
                    }
                    if (baos.size() > chunk_size || bytes_read < 0 && baos.size() > 0) {
                        if (transfer_lock.getBytes() < 0x1400000L && chunk_size < 0x1400000) {
                            chunk_size += 524288;
                        }
                        var30_31 = transfer_lock;
                        synchronized (var30_31) {
                            pos += (long)baos.size();
                            chunk = new Properties();
                            b2 = new byte[baos.size()];
                            System.arraycopy(baos.toByteArray(), 0, b2, 0, baos.size());
                            chunk.put("b", b2);
                            chunk.put("bytes_read", String.valueOf(b2.length));
                            chunk.put("time", String.valueOf(System.currentTimeMillis()));
                            transfer_lock.addChunk(String.valueOf(num), chunk);
                            if (Log.log("HTTP_SERVER", 2, "")) {
                                Log.log("HTTP_SERVER", 2, "Download segment generated:id=" + transfer_lock.getVal("transfer_id") + ":num=" + num + ":bytes=" + bytes_read + ":pending=" + transfer_lock.getBytes() + ":transfer_lock_size=" + transfer_lock.getChunkCount() + " Ram:" + Integer.parseInt(System.getProperty("crushftp.ram_used_percent", "0")) + "%");
                            }
                            transfer_lock.putObj("current_num", String.valueOf(num));
                            baos.reset();
                            Thread.currentThread().setName(String.valueOf(base_name) + ":pending_bytes=" + transfer_lock.getBytes() + ", current_num=" + num);
                        }
                        last_chunk_time1 = System.currentTimeMillis();
                        last_chunk_time2 = System.currentTimeMillis();
                        ++num;
                    }
                    if (bytes_read < 0) {
                        transfer_lock.putObj("total_chunks", String.valueOf(--num));
                    }
                    if (System.currentTimeMillis() - last_chunk_time1 > (long)(ServerStatus.IG("html5_chunk_timeout") * 1000) || System.currentTimeMillis() - last_chunk_time2 > 1200000L) {
                        msg = "Transfer failure! " + transfer_lock.getVal("transfer_path") + ": + Timeout waiting for chunk from browser chunked transferer:1=" + (System.currentTimeMillis() - last_chunk_time1) + ":2=" + (System.currentTimeMillis() - last_chunk_time2) + ":num=" + num + ":id=" + transfer_lock.getVal("transfer_id");
                        Log.log("SERVER", 0, msg);
                        throw new IOException(msg);
                    }
                    try {
                        if (thisSession.getProperty("blockDownloads", "false").equals("true")) {
                            throw new Exception("Transfer failed: User Cancelled");
                        }
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 2, e);
                    }
                    if (transfer_lock.getVal("blockDownloads", "false").equals("true")) {
                        throw new Exception("Transfer failed: User Cancelled");
                    }
                    break block32;
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                    retr2.inError = true;
                    retr2.stop_message = "ERROR:" + e.getMessage();
                    x = num;
                    ** while (x >= 0)
                }
lbl-1000:
                // 1 sources

                {
                    transfer_lock.removeChunk(String.valueOf(x));
                    --x;
                    continue;
lbl105:
                    // 1 sources

                    break block13;
                }
            }
            try {
                if (!transfer_lock.hasObj("total_chunks")) continue;
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                continue;
            }
            break;
        }
        try {
            if_stream.close();
        }
        catch (IOException e1) {
            Log.log("SERVER", 0, e1);
        }
        if (pos < transfer_size && !retr2.inError && !retr2.checkPgp()) {
            msg = "ERROR: Transfer failure! " + transfer_lock.getVal("transfer_path") + ":" + "Transfer size mismatch:(" + num + ")" + pos + " != " + transfer_size + ":" + transfer_lock.getVal("transfer_id");
            Log.log("SERVER", 0, msg);
            retr2.inError = true;
            retr2.stop_message = msg;
            transfer_lock.removeAllChunks();
        }
        try {
            loops = 0;
            while (active.getProperty("active", "").equals("true")) {
                Thread.sleep(loops++ < 100 ? loops : 100);
            }
            retr2.c.close();
        }
        catch (Exception var26_27) {
            // empty catch block
        }
        if (end_loops >= 120) {
            transfer_lock.putObj("status", "ERROR:End of file not received:" + num + " != " + transfer_lock.getVal("total_chunks"));
        } else {
            transfer_lock.putObj("status", retr2.stop_message.equals("") != false ? "" : "ERROR:" + retr2.stop_message);
        }
        if (thisSession.uiPG("lastDownloadStat") != null) {
            ServerStatus.thisObj.statTools.insertMetaInfo(thisSession.uiSG("SESSION_RID"), metaInfo, thisSession.uiPG("lastDownloadStat").getProperty("TRANSFER_RID"));
        }
        retr2.kill();
    }

    public void addMissingButtons(Vector buttons) throws Exception {
        boolean found = false;
        int x = 0;
        while (x < buttons.size()) {
            if (buttons.elementAt(x).toString().indexOf("admin") >= 0) {
                found = true;
            }
            ++x;
        }
        if (!found && !com.crushftp.client.Common.dmz_mode) {
            Properties button = new Properties();
            button.put("key", "(admin):Admin");
            button.put("value", "");
            button.put("for_menu", "true");
            button.put("for_context_menu", "false");
            if (this.thisSessionHTTP.thisSession.SG("site").indexOf("(CONNECT)") >= 0 || this.thisSessionHTTP.thisSession.SG("site").indexOf("(SERVER_VIEW)") >= 0 || this.thisSessionHTTP.thisSession.SG("site").indexOf("(SERVER_EDIT)") >= 0 || this.thisSessionHTTP.thisSession.SG("site").indexOf("(UPDATE_RUN)") >= 0) {
                button.put("value", String.valueOf(this.thisSessionHTTP.proxy) + "WebInterface/admin/index.html");
            } else if (this.thisSessionHTTP.thisSession.SG("site").indexOf("(USER_ADMIN)") >= 0 || this.thisSessionHTTP.thisSession.SG("site").indexOf("(USER_VIEW)") >= 0 || this.thisSessionHTTP.thisSession.SG("site").indexOf("(USER_EDIT)") >= 0 || this.thisSessionHTTP.thisSession.SG("site").indexOf("(UPDATE_RUN)") >= 0) {
                button.put("value", String.valueOf(this.thisSessionHTTP.proxy) + "WebInterface/UserManager/index.html");
            } else if (this.thisSessionHTTP.thisSession.SG("site").indexOf("(PREF_VIEW)") >= 0 || this.thisSessionHTTP.thisSession.SG("site").indexOf("(PREF_EDIT)") >= 0) {
                button.put("value", String.valueOf(this.thisSessionHTTP.proxy) + "WebInterface/Preferences/index.html");
            } else if (this.thisSessionHTTP.thisSession.SG("site").indexOf("(SHARE_VIEW)") >= 0 || this.thisSessionHTTP.thisSession.SG("site").indexOf("(SHARE_EDIT)") >= 0) {
                button.put("value", String.valueOf(this.thisSessionHTTP.proxy) + "WebInterface/TempAccounts/index.html");
            } else if (this.thisSessionHTTP.thisSession.SG("site").indexOf("(REPORT_VIEW)") >= 0 || this.thisSessionHTTP.thisSession.SG("site").indexOf("(REPORT_EDIT)") >= 0) {
                button.put("value", String.valueOf(this.thisSessionHTTP.proxy) + "WebInterface/admin/index.html");
            } else if (this.thisSessionHTTP.thisSession.SG("site").indexOf("(JOB_EDIT)") >= 0 || this.thisSessionHTTP.thisSession.SG("site").indexOf("(JOB_VIEW)") >= 0 || this.thisSessionHTTP.thisSession.SG("site").indexOf("(JOB_LIST)") >= 0 || this.thisSessionHTTP.thisSession.SG("site").indexOf("(JOB_LIST_HISTORY)") >= 0 || this.thisSessionHTTP.thisSession.SG("site").indexOf("(JOB_MONITOR)") >= 0) {
                button.put("value", String.valueOf(this.thisSessionHTTP.proxy) + "WebInterface/Jobs/index.html");
            }
            if (!button.getProperty("value", "").equals("")) {
                buttons.insertElementAt(button, 0);
            }
        }
        Properties button = new Properties();
        button.put("key", "(copyDirectLink):Copy Link");
        button.put("for_menu", "false");
        button.put("for_context_menu", "true");
        button.put("value", "javascript:performAction('copyDirectLink');");
        buttons.insertElementAt(button, 0);
    }

    public String getXmlListingResponse(String username, Properties request, String the_dir, boolean realPaths, VFS tmpVFS) throws Exception {
        Vector listing = null;
        the_dir = com.crushftp.client.Common.dots(the_dir);
        boolean access_blocked = false;
        Pattern pattern = null;
        String block_access = this.thisSessionHTTP.thisSession.SG("block_access").trim();
        block_access = String.valueOf(block_access) + "\r\n";
        block_access = String.valueOf(block_access) + ServerStatus.SG("block_access").trim();
        block_access = block_access.trim();
        BufferedReader br = new BufferedReader(new StringReader(block_access));
        String searchPattern = "";
        while ((searchPattern = br.readLine()) != null) {
            searchPattern = searchPattern.trim();
            try {
                pattern = null;
                pattern = com.crushftp.client.Common.getPattern(searchPattern, true);
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
            }
            if (!searchPattern.startsWith("~") && pattern != null && pattern.matcher(the_dir).matches()) {
                access_blocked = true;
                continue;
            }
            if (!searchPattern.startsWith("~") || !com.crushftp.client.Common.do_search(searchPattern.substring(1), the_dir, false, 0)) continue;
            access_blocked = true;
        }
        Properties item = tmpVFS.get_item(the_dir);
        boolean exists = true;
        if (item == null) {
            item = tmpVFS.get_item_parent(the_dir);
            exists = false;
        }
        String privs = "";
        if (item != null) {
            privs = item.getProperty("privs", "");
        }
        Properties listingProp = new Properties();
        if (!access_blocked) {
            File_U temp_dir;
            if (item != null && item.getProperty("url", "").indexOf("|") >= 0 && item.getProperty("proxy_item", "false").equals("true")) {
                if (item.getProperty("permissions").charAt(2) != 'w') {
                    privs = Common.replace_str(privs, "(write)", "");
                }
                if (item.getProperty("permissions").charAt(2) == 'w') {
                    privs = Common.replace_str(privs, "(read)", "");
                }
            }
            listingProp = ServerSessionAJAX.getListingInfo(listing, the_dir, privs, false, tmpVFS, realPaths, true, this.thisSessionHTTP.thisSession, false);
            if (ServerStatus.BG("ignore_failed_directory_listings")) {
                Log.log("SERVER", 1, "Ignoring file listing error:" + listingProp.remove("error"));
                listingProp.remove("error_msg");
                exists = true;
            }
            if (listingProp.get("error") != null) {
                if (listingProp.get("error") instanceof Exception) {
                    Exception list_exception = (Exception)listingProp.get("error");
                    Log.log("SERVER", 1, list_exception);
                    if (list_exception.getMessage().startsWith("Hadoop: The url is not active:") || list_exception.getMessage().startsWith("Hadoop: List - ")) {
                        return "FAILURE: " + list_exception.getMessage();
                    }
                    if (list_exception.getMessage().startsWith("ERROR : Bad credentials")) {
                        return "FAILURE: " + list_exception.getMessage();
                    }
                    return "{ \"error_msg\" : \"error_item\" }";
                }
                throw new Exception("" + listingProp.get("error"));
            }
            if (listingProp.get("listing") == null) {
                Log.log("HTTP_SERVER", 2, "getXMLListing:Got listing of:" + ((Vector)listingProp.get("listing")).size());
            }
            if (new VRL(item.getProperty("url", "")).getProtocol().equalsIgnoreCase("file") && (temp_dir = new File_U(String.valueOf(new VRL(item.getProperty("url")).getPath()) + "/.message")).exists()) {
                RandomAccessFile message_is = new RandomAccessFile(temp_dir, "r");
                byte[] temp_array = new byte[(int)message_is.length()];
                message_is.readFully(temp_array);
                message_is.close();
                listingProp.put("comment", com.crushftp.client.Common.xss_strip(new String(temp_array)));
            }
        }
        if (ServerStatus.server_settings.get("defaultStrings") != null && ServerStatus.server_settings.get("defaultStrings") instanceof Properties) {
            listingProp.put("defaultStrings", ServerStatus.server_settings.get("defaultStrings"));
        }
        listingProp.put("site", this.thisSessionHTTP.thisSession.SG("site"));
        listingProp.put("quota", "");
        long quota = -12345L;
        if (com.crushftp.client.Common.dmz_mode) {
            Vector queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
            Properties action = new Properties();
            action.put("type", "GET:QUOTA");
            action.put("id", Common.makeBoundary());
            action.put("username", this.thisSessionHTTP.thisSession.uiSG("user_name"));
            action.put("password", this.thisSessionHTTP.thisSession.uiSG("current_password"));
            Properties root_item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir"));
            GenericClient c = this.thisSessionHTTP.thisSession.uVFS.getClient(root_item);
            action.put("crushAuth", c.getConfig("crushAuth"));
            this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
            action.put("request", request);
            String the_dir2 = the_dir;
            if (the_dir2.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                the_dir2 = the_dir2.substring(this.thisSessionHTTP.thisSession.SG("root_dir").length() - 1);
            }
            action.put("the_dir", the_dir2);
            action.put("clientid", this.thisSessionHTTP.thisSession.uiSG("clientid"));
            action.put("need_response", "true");
            queue.addElement(action);
            action = UserTools.waitResponse(action, 300);
            quota = Long.parseLong(action.remove("object_response").toString().split(":")[0]);
        } else {
            quota = this.thisSessionHTTP.thisSession.get_quota(the_dir);
        }
        if (quota != -12345L) {
            listingProp.put("quota", com.crushftp.client.Common.format_bytes_short2(quota));
            listingProp.put("quota_bytes", String.valueOf(quota));
        }
        listingProp.put("bytes_sent", String.valueOf(this.thisSessionHTTP.thisSession.IG("bytes_sent")));
        listingProp.put("bytes_received", String.valueOf(this.thisSessionHTTP.thisSession.IG("bytes_received")));
        listingProp.put("max_upload_size", String.valueOf(this.thisSessionHTTP.thisSession.IG("max_upload_size")));
        listingProp.put("max_upload_amount_day", String.valueOf(this.thisSessionHTTP.thisSession.IG("max_upload_amount_day")));
        listingProp.put("max_upload_amount_month", String.valueOf(this.thisSessionHTTP.thisSession.IG("max_upload_amount_month")));
        listingProp.put("max_upload_amount", String.valueOf(this.thisSessionHTTP.thisSession.IG("max_upload_amount")));
        listingProp.put("max_upload_amount_available", String.valueOf(this.thisSessionHTTP.thisSession.IG("max_upload_amount") - this.thisSessionHTTP.thisSession.IG("bytes_received")));
        listingProp.put("max_download_amount", String.valueOf(this.thisSessionHTTP.thisSession.IG("max_download_amount")));
        listingProp.put("max_download_amount_day", String.valueOf(this.thisSessionHTTP.thisSession.IG("max_download_amount_day")));
        listingProp.put("max_download_amount_month", String.valueOf(this.thisSessionHTTP.thisSession.IG("max_download_amount_month")));
        listingProp.put("max_download_amount_available", String.valueOf(this.thisSessionHTTP.thisSession.IG("max_download_amount") - this.thisSessionHTTP.thisSession.IG("bytes_sent")));
        if (ServerStatus.BG("calculate_transfer_usage_listings")) {
            if (this.thisSessionHTTP.thisSession.IG("max_upload_amount_day") > 0) {
                listingProp.put("max_upload_amount_day_available", String.valueOf((long)this.thisSessionHTTP.thisSession.IG("max_upload_amount_day") * 1024L - ServerStatus.thisObj.statTools.getTransferAmountToday(this.thisSessionHTTP.thisSession.uiSG("user_ip"), this.thisSessionHTTP.thisSession.uiSG("user_name"), this.thisSessionHTTP.thisSession.uiPG("stat"), "uploads", this.thisSessionHTTP.thisSession)));
            }
            if (this.thisSessionHTTP.thisSession.IG("max_upload_amount_month") > 0) {
                listingProp.put("max_upload_amount_month_available", String.valueOf((long)this.thisSessionHTTP.thisSession.IG("max_upload_amount_month") * 1024L - ServerStatus.thisObj.statTools.getTransferAmountThisMonth(this.thisSessionHTTP.thisSession.uiSG("user_ip"), this.thisSessionHTTP.thisSession.uiSG("user_name"), this.thisSessionHTTP.thisSession.uiPG("stat"), "uploads", this.thisSessionHTTP.thisSession)));
            }
            if (this.thisSessionHTTP.thisSession.IG("max_download_amount_day") > 0) {
                listingProp.put("max_download_amount_day_available", String.valueOf((long)this.thisSessionHTTP.thisSession.IG("max_download_amount_day") * 1024L - ServerStatus.thisObj.statTools.getTransferAmountToday(this.thisSessionHTTP.thisSession.uiSG("user_ip"), this.thisSessionHTTP.thisSession.uiSG("user_name"), this.thisSessionHTTP.thisSession.uiPG("stat"), "downloads", this.thisSessionHTTP.thisSession)));
            }
            if (this.thisSessionHTTP.thisSession.IG("max_download_amount_month") > 0) {
                listingProp.put("max_download_amount_month_available", String.valueOf((long)this.thisSessionHTTP.thisSession.IG("max_download_amount_month") * 1024L - ServerStatus.thisObj.statTools.getTransferAmountThisMonth(this.thisSessionHTTP.thisSession.uiSG("user_ip"), this.thisSessionHTTP.thisSession.uiSG("user_name"), this.thisSessionHTTP.thisSession.uiPG("stat"), "downloads", this.thisSessionHTTP.thisSession)));
            }
        }
        if (this.thisSessionHTTP.thisSession.IG("ratio") != 0) {
            listingProp.put("ratio_amount", String.valueOf(this.thisSessionHTTP.thisSession.LG("ratio")));
            listingProp.put("ratio_sent", String.valueOf(this.thisSessionHTTP.thisSession.uiLG("bytes_sent") + this.thisSessionHTTP.thisSession.uiLG("ratio_bytes_sent")));
            listingProp.put("ratio_received", String.valueOf(this.thisSessionHTTP.thisSession.uiLG("bytes_received") + this.thisSessionHTTP.thisSession.uiLG("ratio_bytes_received")));
            listingProp.put("ratio_available", String.valueOf((this.thisSessionHTTP.thisSession.uiLG("bytes_received") + this.thisSessionHTTP.thisSession.uiLG("ratio_bytes_received")) * this.thisSessionHTTP.thisSession.LG("ratio") - (this.thisSessionHTTP.thisSession.uiLG("bytes_sent") + this.thisSessionHTTP.thisSession.uiLG("ratio_bytes_sent"))));
        }
        Vector original_listing = (Vector)listingProp.get("listing");
        String altList = "";
        if (request.getProperty("format", "").equalsIgnoreCase("JSON")) {
            altList = AgentUI.getJsonList(listingProp, ServerStatus.BG("exif_listings"), false);
        } else if (request.getProperty("format", "").equalsIgnoreCase("JSONOBJ")) {
            altList = AgentUI.getJsonListObj(listingProp, ServerStatus.BG("exif_listings"));
        } else if (request.getProperty("format", "").equalsIgnoreCase("STAT")) {
            altList = AgentUI.getStatList(listingProp);
        } else if (request.getProperty("format", "").equalsIgnoreCase("STAT_DMZ")) {
            altList = AgentUI.getDmzList(listingProp);
        }
        if (request.getProperty("format", "").equalsIgnoreCase("JSONOBJ")) {
            Properties p;
            int x;
            Properties combinedPermissions = tmpVFS.getCombinedPermissions();
            boolean aclPermissions = combinedPermissions.getProperty("acl_permissions", "false").equals("true");
            privs = listingProp.getProperty("privs", "");
            if (aclPermissions) {
                privs = tmpVFS.getPriv(the_dir, item);
            } else if (new VRL(item.getProperty("url")).getProtocol().toUpperCase().startsWith("HTTP")) {
                Vector list2 = new Vector();
                tmpVFS.getListing(list2, Common.all_but_last(the_dir), 1, 9999, true);
                x = 0;
                while (x < list2.size()) {
                    p = (Properties)list2.elementAt(x);
                    if (p != null && p.getProperty("name", "").equals(item.getProperty("name", ""))) {
                        privs = p.getProperty("privs");
                        break;
                    }
                    ++x;
                }
            }
            String info = "{\r\n";
            info = String.valueOf(info) + "\t\"privs\" : \"" + privs.trim().replaceAll("\\r", "%0D").replaceAll("\\n", "%0A").replaceAll("\"", "%22").replaceAll("\t", "%09") + "\",\r\n";
            info = String.valueOf(info) + "\t\"comment\" : \"" + Common.url_encode(listingProp.getProperty("comment", "").trim()) + "\",\r\n";
            info = String.valueOf(info) + "\t\"path\" : \"" + listingProp.getProperty("path", "").trim().replaceAll("\"", "%22").replaceAll("\t", "%09") + "\",\r\n";
            info = String.valueOf(info) + "\t\"defaultStrings\" : \"" + listingProp.getProperty("defaultStrings", "").trim() + "\",\r\n";
            info = String.valueOf(info) + "\t\"site\" : \"" + listingProp.getProperty("site", "").trim() + "\",\r\n";
            info = String.valueOf(info) + "\t\"quota\" : \"" + listingProp.getProperty("quota", "").trim() + "\",\r\n";
            info = String.valueOf(info) + "\t\"quota_bytes\" : \"" + listingProp.getProperty("quota_bytes", "").trim() + "\",\r\n";
            info = String.valueOf(info) + "\t\"bytes_sent\" : \"" + listingProp.getProperty("bytes_sent", "").trim() + "\",\r\n";
            info = String.valueOf(info) + "\t\"bytes_received\" : \"" + listingProp.getProperty("bytes_received", "").trim() + "\",\r\n";
            info = String.valueOf(info) + "\t\"max_upload_amount_day\" : \"" + listingProp.getProperty("max_upload_amount_day", "").trim() + "\",\r\n";
            info = String.valueOf(info) + "\t\"max_upload_amount_month\" : \"" + listingProp.getProperty("max_upload_amount_month", "").trim() + "\",\r\n";
            info = String.valueOf(info) + "\t\"max_upload_amount\" : \"" + listingProp.getProperty("max_upload_amount", "").trim() + "\",\r\n";
            info = String.valueOf(info) + "\t\"max_upload_amount_available\" : \"" + listingProp.getProperty("max_upload_amount_available", "").trim() + "\",\r\n";
            info = String.valueOf(info) + "\t\"max_upload_amount_day_available\" : \"" + listingProp.getProperty("max_upload_amount_day_available", "").trim() + "\",\r\n";
            info = String.valueOf(info) + "\t\"max_upload_amount_month_available\" : \"" + listingProp.getProperty("max_upload_amount_month_available", "").trim() + "\",\r\n";
            info = String.valueOf(info) + "\t\"max_download_amount\" : \"" + listingProp.getProperty("max_download_amount", "").trim() + "\",\r\n";
            info = String.valueOf(info) + "\t\"max_download_amount_day\" : \"" + listingProp.getProperty("max_download_amount_day", "").trim() + "\",\r\n";
            info = String.valueOf(info) + "\t\"max_download_amount_month\" : \"" + listingProp.getProperty("max_download_amount_month", "").trim() + "\",\r\n";
            info = String.valueOf(info) + "\t\"max_download_amount_available\" : \"" + listingProp.getProperty("max_download_amount_available", "").trim() + "\",\r\n";
            info = String.valueOf(info) + "\t\"max_download_amount_day_available\" : \"" + listingProp.getProperty("max_download_amount_day_available", "").trim() + "\",\r\n";
            info = String.valueOf(info) + "\t\"max_download_amount_month_available\" : \"" + listingProp.getProperty("max_download_amount_month_available", "").trim() + "\",\r\n";
            if (this.thisSessionHTTP.thisSession.IG("ratio") != 0) {
                info = String.valueOf(info) + "\t\"ratio_amount\" : \"" + listingProp.getProperty("ratio_amount", "0").trim() + "\",\r\n";
                info = String.valueOf(info) + "\t\"ratio_sent\" : \"" + listingProp.getProperty("ratio_sent", "0").trim() + "\",\r\n";
                info = String.valueOf(info) + "\t\"ratio_received\" : \"" + listingProp.getProperty("ratio_received", "0").trim() + "\",\r\n";
                info = String.valueOf(info) + "\t\"ratio_available\" : \"" + listingProp.getProperty("ratio_available", "0").trim() + "\",\r\n";
            }
            if (this.thisSessionHTTP.thisSession.BG("DisallowListingDirectories")) {
                listing = new Vector();
                listingProp.put("listing", listing);
                altList = "[]";
            }
            if (this.thisSessionHTTP.thisSession.BG("WebServerMode")) {
                x = 0;
                while (original_listing != null && x < original_listing.size()) {
                    p = (Properties)original_listing.elementAt(x);
                    if (p.getProperty("name").toUpperCase().equals("INDEX.HTML")) {
                        info = String.valueOf(info) + "\t\"web_server_mode\" : \"true\",\r\n";
                        break;
                    }
                    ++x;
                }
            }
            if (!exists) {
                info = String.valueOf(info) + " \"error_msg\" : \"invalid_item\",\r\n";
            }
            info = String.valueOf(info) + "\t\"listing\" : " + altList + "\r\n";
            info = String.valueOf(info) + "}\r\n";
            return info;
        }
        String response = "";
        try {
            Common cfr_ignored_0 = ServerStatus.thisObj.common_code;
            response = Common.getXMLString(listingProp, "listingInfo", null);
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
        }
        if (!altList.equals("")) {
            response = String.valueOf(response.substring(0, response.indexOf("</privs>") + "</privs>".length())) + altList + response.substring(response.indexOf("</privs>") + "</privs>".length());
        }
        return response;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Properties getListingInfo(Vector listing, final String the_dir, String privs, boolean ignoreRootDir, VFS tmpVFS, boolean realPaths, boolean hideHidden, final SessionCrush thisSession, boolean allowDuplicates) {
        Vector<Properties> items = new Vector<Properties>();
        Exception error = null;
        try {
            thisSession.date_time = SessionCrush.updateDateCustomizations(thisSession.date_time, thisSession.user);
            if (listing == null) {
                listing = new Vector();
                Log.log("HTTP_SERVER", 3, "Getting dir listing for:" + thisSession.uiSG("user_name") + " with VFS from:" + tmpVFS);
                Log.log("HTTP_SERVER", 3, Thread.currentThread().getName());
                Log.log("HTTP_SERVER", 3, new Exception("From where?"));
                tmpVFS.getListing(listing, the_dir);
                Log.log("HTTP_SERVER", 3, "Found " + listing.size() + " items in " + the_dir + ".");
            }
            SimpleDateFormat date_time = (SimpleDateFormat)thisSession.date_time.clone();
            Properties pp = new Properties();
            pp.put("listing", listing);
            thisSession.runPlugin("list", pp);
            Properties name_hash = new Properties();
            Log.log("HTTP_SERVER", 3, "Going through listing checking filters...");
            GregorianCalendar cal = new GregorianCalendar();
            int x = 0;
            while (x < listing.size()) {
                Properties list_item = (Properties)((Properties)listing.elementAt(x)).clone();
                if (LIST_handler.checkName(list_item, thisSession, false, false) && !list_item.getProperty("hide_smb", "false").equals("true")) {
                    Date d;
                    if (thisSession.DG("timezone_offset") != 0.0) {
                        d = new Date(Long.parseLong(list_item.getProperty("modified")));
                        cal.setTime(d);
                        cal.setTimeInMillis((long)((double)cal.getTimeInMillis() + thisSession.DG("timezone_offset") * 1000.0 * 60.0 * 60.0));
                        list_item.put("modified", String.valueOf(cal.getTime().getTime()));
                        list_item.put("created", "0");
                        if (!list_item.getProperty("created", "0").equals("0")) {
                            d = new Date(Long.parseLong(list_item.getProperty("created", "0")));
                            cal.setTime(d);
                            cal.setTimeInMillis((long)((double)cal.getTimeInMillis() + thisSession.DG("timezone_offset") * 1000.0 * 60.0 * 60.0));
                            list_item.put("created", String.valueOf(cal.getTime().getTime()));
                        }
                    }
                    if (name_hash.get(list_item.getProperty("name")) == null || !hideHidden) {
                        if (!allowDuplicates) {
                            name_hash.put(list_item.getProperty("name"), "DONE");
                        }
                        if (!list_item.containsKey("preview")) {
                            Properties status;
                            if (ServerStatus.BG("find_list_previews")) {
                                String preview_the_dir = "/this_dir_does_not_exist";
                                try {
                                    if (list_item.getProperty("url").startsWith("virtual://")) {
                                        Properties tmpItem = null;
                                        if (!ServerStatus.BG("vfs_lazy_load")) {
                                            tmpItem = tmpVFS.get_item(String.valueOf(list_item.getProperty("root_dir")) + list_item.getProperty("name"));
                                        }
                                        if (tmpItem != null) {
                                            list_item = tmpItem;
                                        }
                                    }
                                    preview_the_dir = SearchHandler.getPreviewPath(list_item.getProperty("url"), "1", 1);
                                }
                                catch (Exception e) {
                                    Log.log("HTTP_SERVER", 1, e);
                                    Log.log("HTTP_SERVER", 1, list_item.toString());
                                }
                                String index = "";
                                if (preview_the_dir != null) {
                                    index = String.valueOf(ServerStatus.SG("previews_path")) + preview_the_dir.substring(1);
                                    list_item.put("keywords", "");
                                }
                                if (preview_the_dir != null && new File_U(Common.all_but_last(Common.all_but_last(index))).exists()) {
                                    String preview_the_dir_parent = Common.all_but_last(Common.all_but_last(preview_the_dir));
                                    int frames = 1;
                                    while (new File_U(String.valueOf(ServerStatus.SG("previews_path")) + preview_the_dir_parent + "p" + frames).exists()) {
                                        ++frames;
                                    }
                                    if (list_item.getProperty("name").toUpperCase().endsWith(".ZIP") && !ServerStatus.BG("zip_icon_preview_allowed")) {
                                        list_item.put("preview", "0");
                                    } else {
                                        list_item.put("preview", String.valueOf(frames - 1));
                                    }
                                    String indexText = "";
                                    if (ServerStatus.BG("exif_keywords")) {
                                        Properties info;
                                        if (new File_U(String.valueOf(Common.all_but_last(Common.all_but_last(index))) + "info.xml").exists() && (info = (Properties)Common.readXMLObject_U(new File_U(String.valueOf(Common.all_but_last(Common.all_but_last(index))) + "info.xml"))) != null) {
                                            indexText = com.crushftp.client.Common.xss_strip(info.getProperty("keywords", ""));
                                        }
                                    } else if (new File_U(String.valueOf(Common.all_but_last(Common.all_but_last(index))) + "index.txt").exists()) {
                                        RandomAccessFile out = new RandomAccessFile(new File_U(String.valueOf(Common.all_but_last(Common.all_but_last(index))) + "index.txt"), "r");
                                        byte[] b = new byte[(int)out.length()];
                                        out.readFully(b);
                                        out.close();
                                        indexText = com.crushftp.client.Common.xss_strip(new String(b));
                                    }
                                    if (ServerStatus.BG("exif_listings")) {
                                        Properties p;
                                        String local_path = String.valueOf(Common.all_but_last(Common.all_but_last(index))) + "info.xml";
                                        boolean found = false;
                                        if (FileClient.memCache && (p = (Properties)PreviewWorker.exif_cache.get(local_path)) != null && list_item.getProperty("modified", "0").equals(p.getProperty("modified", "1"))) {
                                            com.crushftp.client.Common.putAllSafe(list_item, p);
                                            found = true;
                                        }
                                        if (!found && new File_U(local_path).exists()) {
                                            Properties info = (Properties)Common.readXMLObject_U(new File_U(String.valueOf(Common.all_but_last(Common.all_but_last(index))) + "info.xml"));
                                            if (info != null) {
                                                Properties p2 = new Properties();
                                                p2.put("width", info.getProperty("imagewidth", ""));
                                                p2.put("height", info.getProperty("imageheight", ""));
                                                Enumeration<Object> keys = info.keys();
                                                while (keys.hasMoreElements()) {
                                                    String key = "" + keys.nextElement();
                                                    if (!key.startsWith(String.valueOf(System.getProperty("appname", "CrushFTP").toLowerCase()) + "_")) continue;
                                                    p2.put(key, com.crushftp.client.Common.xss_strip(info.getProperty(key)));
                                                }
                                                com.crushftp.client.Common.putAllSafe(list_item, p2);
                                                if (FileClient.memCache) {
                                                    p2.put("modified", list_item.getProperty("modified", "2"));
                                                    PreviewWorker.exif_cache.put(local_path, p2);
                                                    Properties properties = PreviewWorker.exif_cache;
                                                    synchronized (properties) {
                                                        ServerStatus.siPUT("exif_item_count", String.valueOf(ServerStatus.siLG("exif_item_count") + 1L));
                                                    }
                                                }
                                            }
                                        } else if (!found && FileClient.memCache) {
                                            p = new Properties();
                                            p.put("modified", list_item.getProperty("modified", "3"));
                                            PreviewWorker.exif_cache.put(local_path, p);
                                            Properties properties = PreviewWorker.exif_cache;
                                            synchronized (properties) {
                                                ServerStatus.siPUT("exif_item_count", String.valueOf(ServerStatus.siLG("exif_item_count") + 1L));
                                            }
                                        }
                                    }
                                    list_item.put("keywords", indexText == null ? "" : indexText);
                                    if (!new File_U(String.valueOf(ServerStatus.SG("previews_path")) + preview_the_dir_parent + "p1/1.jpg").exists()) {
                                        list_item.put("preview", "0");
                                    }
                                } else {
                                    list_item.put("preview", "0");
                                }
                            } else {
                                list_item.put("keywords", "");
                                list_item.put("preview", "0");
                            }
                            if (list_item.getProperty("type", "").equals("DIR") && thisSession.BG("dir_calc")) {
                                final Properties list_item2 = list_item;
                                final Vector inside_a_dir_list = new Vector();
                                status = new Properties();
                                Worker.startWorker(new Runnable(){

                                    @Override
                                    public void run() {
                                        try {
                                            thisSession.uVFS.getListing(inside_a_dir_list, String.valueOf(the_dir) + list_item2.getProperty("name") + "/", Integer.parseInt(thisSession.uVFS.user.getProperty("dir_depth", "1")), 1000, true, null);
                                        }
                                        catch (Exception e) {
                                            Log.log("SERVER", 1, e);
                                        }
                                        status.put("done", "true");
                                    }
                                });
                                long total_size = 0L;
                                int count = 0;
                                while (inside_a_dir_list.size() > 0 || !status.containsKey("done")) {
                                    if (inside_a_dir_list.size() > 0) {
                                        Properties p = (Properties)inside_a_dir_list.remove(0);
                                        if (p.getProperty("type", "").toUpperCase().equals("DIR")) continue;
                                        total_size += Long.parseLong(p.getProperty("size"));
                                        ++count;
                                        continue;
                                    }
                                    Thread.sleep(100L);
                                }
                                list_item.put("size", String.valueOf(total_size));
                                list_item.put("num_items", String.valueOf(count));
                            } else if (list_item.getProperty("type", "").equals("DIR") && thisSession.BG("dir_calc_count")) {
                                final Vector inside_a_dir_list = new Vector();
                                final Properties list_item2 = list_item;
                                status = new Properties();
                                Worker.startWorker(new Runnable(){

                                    @Override
                                    public void run() {
                                        try {
                                            thisSession.uVFS.getListing(inside_a_dir_list, String.valueOf(the_dir) + list_item2.getProperty("name") + "/", 1, 1000, true);
                                        }
                                        catch (Exception e) {
                                            Log.log("SERVER", 1, e);
                                        }
                                        status.put("done", "true");
                                    }
                                });
                                int count = 0;
                                while (inside_a_dir_list.size() > 0 || !status.containsKey("done")) {
                                    if (inside_a_dir_list.size() > 0) {
                                        Properties p = (Properties)inside_a_dir_list.remove(0);
                                        if (!LIST_handler.checkName(p, thisSession, false, false)) continue;
                                        ++count;
                                        continue;
                                    }
                                    Thread.sleep(100L);
                                }
                                list_item2.put("num_items", "" + (count - 1));
                            }
                            list_item.put("sizeFormatted", com.crushftp.client.Common.format_bytes2(list_item.getProperty("size")));
                            list_item.put("date", String.valueOf(list_item.getProperty("month", "")) + " " + list_item.getProperty("day", "") + " " + list_item.getProperty("time_or_year", ""));
                            d = new Date(Long.parseLong(list_item.getProperty("modified", "0")));
                            if (d.getTime() < 30000000L) {
                                list_item.put("dateFormatted", "");
                            } else {
                                list_item.put("dateFormatted", date_time.format(d));
                            }
                            list_item.put("modified", list_item.getProperty("modified", "0"));
                            list_item.put("created", list_item.getProperty("created", "0"));
                            list_item.put("createdDateFormatted", date_time.format(new Date(Long.parseLong(list_item.getProperty("created", "0")))));
                            if (list_item.getProperty("privs").indexOf("(comment") >= 0) {
                                String comment = Common.url_decode(list_item.getProperty("privs").substring(list_item.getProperty("privs").indexOf("(comment") + 8, list_item.getProperty("privs").indexOf(")", list_item.getProperty("privs").indexOf("(comment"))));
                                list_item.put("comment", ServerStatus.thisObj.change_vars_to_values(comment.trim(), thisSession));
                            }
                        }
                        if (ServerStatus.BG("reveal_vfs_protocol_end_user") && !com.crushftp.client.Common.dmz_mode) {
                            list_item.put("vfs_protocol", new VRL(list_item.getProperty("url")).getProtocol().toLowerCase());
                        }
                        list_item.remove("url");
                        list_item.put("itemType", list_item.getProperty("type"));
                        String the_dir2 = list_item.getProperty("root_dir", "/");
                        if (the_dir2.equals("/")) {
                            the_dir2 = thisSession.SG("root_dir");
                        }
                        if (!ignoreRootDir) {
                            if (the_dir2.startsWith("/") && !the_dir2.toUpperCase().startsWith(thisSession.SG("root_dir").toUpperCase())) {
                                the_dir2 = String.valueOf(thisSession.SG("root_dir")) + the_dir2.substring(1);
                            }
                            the_dir2 = the_dir2.substring(thisSession.SG("root_dir").length() - 1);
                        }
                        list_item.put("root_dir", the_dir2);
                        if (list_item.getProperty("privs").indexOf("(inherited)") < 0 && thisSession.uVFS.getCombinedPermissions().getProperty("acl_permissions", "false").equals("false")) {
                            list_item.put("privs", Common.replace_str(list_item.getProperty("privs", ""), "(delete)", ""));
                            list_item.put("privs", Common.replace_str(list_item.getProperty("privs", ""), "(rename)", ""));
                        }
                        items.addElement(list_item);
                    }
                }
                ++x;
            }
        }
        catch (Exception e) {
            thisSession.add_log("List path : " + the_dir + " ERROR : " + e, "DIR_LIST");
            thisSession.uVFS.reset();
            tmpVFS.reset();
            Log.log("HTTP_SERVER", 1, e);
            error = e;
        }
        Common.do_sort(items, "name");
        int x = 0;
        while (x < items.size()) {
            Properties lp = (Properties)items.elementAt(x);
            if (lp.getProperty("dir", "").indexOf("\"") >= 0) {
                lp.put("dir", lp.getProperty("dir", "").replaceAll("\\\"", "%22").replaceAll("\t", "%09"));
            }
            if (lp.getProperty("name", "").indexOf("\"") >= 0) {
                lp.put("name", lp.getProperty("name", "").replaceAll("\\\"", "%22").replaceAll("\t", "%09"));
            }
            if (lp.getProperty("name", "").endsWith(" ") || lp.getProperty("name", "").startsWith(" ")) {
                lp.put("name", lp.getProperty("name", "").replaceAll(" ", "%20"));
            }
            if (lp.getProperty("path", "").indexOf("\"") >= 0) {
                lp.put("path", lp.getProperty("path", "").replaceAll("\\\"", "%22").replaceAll("\t", "%09"));
            }
            if (lp.getProperty("root_dir", "").indexOf("\"") >= 0) {
                lp.put("root_dir", lp.getProperty("root_dir", "").replaceAll("\\\"", "%22").replaceAll("\t", "%09"));
            }
            String itemName = lp.getProperty("name");
            String itemPath = String.valueOf(the_dir) + lp.getProperty("name");
            if (realPaths) {
                try {
                    Properties tmpItem = tmpVFS.get_item(itemPath);
                    VRL vrl = new VRL(tmpItem.getProperty("url"));
                    lp.put("root_dir", Common.all_but_last(vrl.getPath()));
                }
                catch (Exception tmpItem) {
                    // empty catch block
                }
            }
            String root_dir = lp.getProperty("root_dir");
            String href_path = String.valueOf(lp.getProperty("root_dir")) + lp.getProperty("name");
            if (href_path.startsWith("//") && !href_path.startsWith("////")) {
                href_path = "//" + href_path;
            }
            try {
                lp.put("source", "/WebInterface/function/?command=getPreview&size=3&path=" + itemPath);
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                lp.put("href_path", href_path);
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                lp.put("root_dir", root_dir);
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                lp.put("name", itemName);
            }
            catch (Exception exception) {
                // empty catch block
            }
            ++x;
        }
        Properties listingProp = new Properties();
        listingProp.put("privs", privs);
        String itemPath = the_dir;
        try {
            listingProp.put("path", itemPath);
        }
        catch (Exception exception) {
            // empty catch block
        }
        listingProp.put("listing", items);
        if (error != null) {
            listingProp.put("error", error);
        }
        return listingProp;
    }

    /*
     * Unable to fully structure code
     */
    public String downloadItem(VRL otherFile, Properties item, String fileName, Vector byteRanges, boolean simpleRanges, String mimeType) throws Exception {
        block48: {
            block50: {
                block51: {
                    block46: {
                        block49: {
                            block47: {
                                if (byteRanges.size() > 0) {
                                    this.write_command_http("HTTP/1.1 206 Partial Content");
                                } else {
                                    this.write_command_http("HTTP/1.1 200 OK");
                                }
                                this.thisSessionHTTP.write_standard_headers();
                                byteRangeBoundary = Common.makeBoundary();
                                contentType = "application/binary";
                                if (!mimeType.equals("")) {
                                    contentType = mimeType;
                                }
                                if (byteRanges.size() <= 1 || simpleRanges) {
                                    this.write_command_http("Content-Type: " + contentType);
                                } else if (byteRanges.size() > 1) {
                                    this.write_command_http("Content-Type: multipart/byteranges; boundary=" + byteRangeBoundary);
                                }
                                fileName = Common.replace_str(Common.url_decode(fileName), "\r", "_");
                                if (ServerStatus.BG("pgp_http_downloads_variable_size") && item.getProperty("privs", "").contains("(pgpDecryptDownload=true)") && contentType.equals("application/binary") && fileName.toUpperCase().endsWith(".PGP")) {
                                    fileName = fileName.substring(0, fileName.length() - 4);
                                }
                                if (contentType.equals("application/binary")) {
                                    this.write_command_http("Content-Disposition: attachment; filename=\"" + (this.thisSessionHTTP.thisSession.uiSG("header_user-agent").toUpperCase().indexOf("MSIE") >= 0 || this.thisSessionHTTP.thisSession.uiSG("header_user-agent").toUpperCase().indexOf("TRIDENT") >= 0 ? Common.url_encode(fileName) : fileName) + "\"");
                                }
                                this.write_command_http("X-UA-Compatible: chrome=1");
                                c = this.thisSessionHTTP.thisSession.uVFS.getClient(item);
                                stat = null;
                                try {
                                    stat = c.stat(otherFile.getPath());
                                }
                                finally {
                                    c = this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                                }
                                this.write_command_http("Last-Modified: " + this.thisSessionHTTP.sdf_rfc1123.format(new Date(Long.parseLong(stat.getProperty("modified")))));
                                this.write_command_http("ETag: " + Long.parseLong(stat.getProperty("modified")));
                                amountEnd = stat.getProperty("size");
                                x = 0;
                                while (x < byteRanges.size()) {
                                    pp = (Properties)byteRanges.elementAt(x);
                                    if (pp.getProperty("end", "").equals("")) {
                                        pp.put("end", String.valueOf(Long.parseLong(amountEnd) - 1L));
                                    }
                                    ++x;
                                }
                                if (stat != null || !otherFile.getName().toUpperCase().endsWith(".ZIP")) break block47;
                                Common.startMultiThreadZipper(this.thisSessionHTTP.thisSession.uVFS, this.thisSessionHTTP.retr, Common.dots(this.thisSessionHTTP.pwd()), 2000, item.getProperty("url").toUpperCase().startsWith("FTP:/") != false || item.getProperty("url").toUpperCase().startsWith("HTTP:/") != false, new Vector<E>());
                                this.write_command_http("Connection: close");
                                this.thisSessionHTTP.done = true;
                                break block48;
                            }
                            content_length = 0L;
                            try {
                                content_length = Long.parseLong(stat.getProperty("size"));
                            }
                            catch (Exception var14_18) {
                                // empty catch block
                            }
                            if (byteRanges.size() != 1) break block49;
                            pp = (Properties)byteRanges.elementAt(0);
                            this.write_command_http("Content-Range: bytes " + pp.getProperty("start") + "-" + Long.parseLong(pp.getProperty("end")) + "/" + content_length);
                            calculatedContentLength = Long.parseLong(pp.getProperty("end")) - Long.parseLong(pp.getProperty("start"));
                            if (calculatedContentLength == 0L) {
                                calculatedContentLength = 1L;
                            }
                            this.write_command_http("Content-Length: " + calculatedContentLength);
                            break block50;
                        }
                        if (byteRanges.size() > 1) break block51;
                        ok = true;
                        if (!this.thisSessionHTTP.thisSession.user.getProperty("filePublicEncryptionKey", "").equals("") || ServerStatus.BG("fileEncryption")) {
                            if (otherFile != null && otherFile.getProtocol().equalsIgnoreCase("file")) {
                                b = new byte[200];
                                try {
                                    tempIn = new FileInputStream(new File_U(Common.url_decode(otherFile.getFile())));
                                    tempIn.read(b);
                                    tempIn.close();
                                    s = new String(b);
                                    if (s.indexOf(String.valueOf(System.getProperty("appname", "CrushFTP").toUpperCase()) + "#") < 0) break block46;
                                    if ((s = s.substring(s.indexOf(String.valueOf(System.getProperty("appname", "CrushFTP").toUpperCase()) + "#") + "CRUSHFTP#".length())).indexOf("\r") >= 0) {
                                        s = s.substring(0, s.indexOf("\r")).trim();
                                    }
                                    if (s.indexOf("\n") >= 0) {
                                        s = s.substring(0, s.indexOf("\n")).trim();
                                    }
                                    if (s.equals("")) {
                                        ok = false;
                                    }
                                }
                                catch (Exception e) {
                                    ok = false;
                                    Log.log("SERVER", 1, e);
                                }
                            } else if (!otherFile.getProtocol().equalsIgnoreCase("s3crush")) {
                                ok = false;
                            }
                        }
                    }
                    if (ok && item.getProperty("privs", "").indexOf("(dynamic_size)") >= 0) {
                        ok = false;
                    }
                    if (ok && item != null && item.getProperty("privs", "").contains("(pgpDecryptDownload=true)") && !otherFile.getProtocol().equalsIgnoreCase("file") && !otherFile.getProtocol().equalsIgnoreCase("s3crush")) {
                        ok = false;
                    }
                    if (!ok) ** GOTO lbl-1000
                    if (!ServerStatus.BG("pgp_http_downloads_variable_size") && !item.getProperty("privs", "").contains("(pgpDecryptDownload=true)")) {
                        this.write_command_http("Content-Length: " + content_length);
                    } else lbl-1000:
                    // 2 sources

                    {
                        this.thisSessionHTTP.done = true;
                        this.write_command_http("Connection: close");
                    }
                    break block50;
                }
                if (byteRanges.size() > 1) {
                    if (simpleRanges) {
                        calculatedContentLength = 0L;
                        x = 0;
                        while (x < byteRanges.size()) {
                            pp = (Properties)byteRanges.elementAt(x);
                            calculatedContentLength += Long.parseLong(pp.getProperty("end")) - Long.parseLong(pp.getProperty("start"));
                            ++x;
                        }
                        this.write_command_http("Content-Length: " + calculatedContentLength);
                    } else {
                        calculatedContentLength = 2L;
                        x = 0;
                        while (x < byteRanges.size()) {
                            pp = (Properties)byteRanges.elementAt(x);
                            calculatedContentLength += (long)(("--" + byteRangeBoundary).length() + 2);
                            calculatedContentLength += (long)(("Content-Type: " + contentType).length() + 2);
                            calculatedContentLength += (long)(("Content-range: bytes " + pp.getProperty("start") + "-" + pp.getProperty("end") + "/" + content_length).length() + 2);
                            calculatedContentLength += 2L;
                            calculatedContentLength += Long.parseLong(pp.getProperty("end")) - Long.parseLong(pp.getProperty("start"));
                            calculatedContentLength += 2L;
                            ++x;
                        }
                        if ((calculatedContentLength += (long)(("--" + byteRangeBoundary + "--").length() + 2)) == 0L) {
                            calculatedContentLength = 1L;
                        }
                        this.write_command_http("Content-Length: " + calculatedContentLength);
                    }
                }
            }
            this.write_command_http("Accept-Ranges: bytes");
        }
        if (this.thisSessionHTTP.thisSession.uiSG("header_user-agent").toUpperCase().indexOf("MSIE") >= 0 || this.thisSessionHTTP.thisSession.uiSG("header_user-agent").toUpperCase().indexOf("TRIDENT") >= 0) {
            fileName = Common.url_encode(fileName);
        }
        this.write_command_http("");
        if (byteRanges.size() == 0) {
            pp = new Properties();
            pp.put("start", "0");
            pp.put("end", "-1");
            byteRanges.addElement(pp);
        }
        content_length = 0L;
        try {
            content_length = Long.parseLong(stat.getProperty("size"));
        }
        catch (Exception calculatedContentLength) {
            // empty catch block
        }
        x = 0;
        while (x < byteRanges.size()) {
            pp = (Properties)byteRanges.elementAt(x);
            if (byteRanges.size() > 1 && !simpleRanges) {
                if (x == 0) {
                    this.write_command_http("");
                }
                this.write_command_http("--" + byteRangeBoundary);
                this.write_command_http("Content-Type: " + contentType);
                this.write_command_http("Content-range: bytes " + pp.getProperty("start") + "-" + pp.getProperty("end") + "/" + content_length);
                this.write_command_http("");
            }
            this.thisSessionHTTP.thisSession.uiPUT("file_transfer_mode", "BINARY");
            this.thisSessionHTTP.retr.data_os = this.thisSessionHTTP.original_os;
            this.thisSessionHTTP.retr.httpDownload = true;
            the_dir = this.thisSessionHTTP.pwd();
            ppp = new Properties();
            ppp.put("the_dir", the_dir);
            this.thisSessionHTTP.thisSession.runPlugin("transfer_path", ppp);
            the_dir = ppp.getProperty("the_dir", the_dir);
            this.thisSessionHTTP.retr.init_vars(the_dir, Long.parseLong(pp.getProperty("start")), Long.parseLong(pp.getProperty("end")) + 1L, this.thisSessionHTTP.thisSession, item, false, "", otherFile, null);
            this.thisSessionHTTP.retr.runOnce = true;
            this.thisSessionHTTP.retr.run();
            if (this.thisSessionHTTP.retr.stop_message.length() > 0) {
                return this.thisSessionHTTP.retr.stop_message;
            }
            if (byteRanges.size() > 1 && !simpleRanges) {
                this.write_command_http("");
            }
            ++x;
        }
        if (byteRanges.size() > 1 && !simpleRanges) {
            this.write_command_http("--" + byteRangeBoundary + "--");
        }
        return "";
    }

    public static void processAs2HeaderLine(String key, String val, String data, Properties as2Info) {
        as2Info.put(key.trim().toLowerCase(), val.trim());
        if (data.toLowerCase().startsWith("message-id:")) {
            String as2Filename = data.substring(data.indexOf(":") + 1).trim();
            if ((as2Filename = as2Filename.substring(1)).indexOf("@") >= 0) {
                as2Filename = as2Filename.substring(0, as2Filename.indexOf("@"));
            }
            as2Filename = Common.replace_str(as2Filename, "<", "");
            as2Filename = Common.replace_str(as2Filename, ">", "");
            as2Info.put("as2Filename", as2Filename);
        } else if (data.toLowerCase().startsWith("content-type:")) {
            as2Info.put("contentType", data.substring(data.indexOf(":") + 1).trim());
        } else if (data.toLowerCase().startsWith("disposition-notification-options:")) {
            as2Info.put("signMdn", String.valueOf(data.substring(data.indexOf(":") + 1).trim().indexOf("pkcs7-signature") >= 0));
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public boolean buildPostItem(Properties request, long http_len_max, Vector headers, String req_id) throws Exception {
        Properties as2Info = new Properties();
        boolean write100Continue = false;
        int x = 1;
        while (x < headers.size()) {
            String data;
            String key = data = headers.elementAt(x).toString();
            String val = "";
            try {
                val = data.substring(data.indexOf(":") + 1).trim();
                key = data.substring(0, data.indexOf(":")).trim().toLowerCase();
            }
            catch (Exception e) {
                Log.log("HTTP_SERVER", 3, e);
            }
            as2Info.put(key, val);
            if (data.toLowerCase().startsWith("expect: 100-continue")) {
                write100Continue = true;
            } else {
                ServerSessionAJAX.processAs2HeaderLine(key, val, data, as2Info);
            }
            ++x;
        }
        if (!as2Info.getProperty("as2-to", "").equals("")) {
            boolean validate_by_decrypt_payload = false;
            String actual_as2_username = this.thisSessionHTTP.thisSession.uiSG("user_name");
            if (!this.thisSessionHTTP.thisSession.uiBG("user_logged_in")) {
                String prepend_as2 = ServerStatus.BG("as2_prepend_as2_username") ? "AS2_" : "";
                actual_as2_username = String.valueOf(prepend_as2) + as2Info.getProperty("as2-to", "");
                if (ServerStatus.BG("uppercase_usernames")) {
                    actual_as2_username = String.valueOf(prepend_as2) + as2Info.getProperty("as2-to", "").toUpperCase();
                }
                if (ServerStatus.BG("lowercase_usernames")) {
                    actual_as2_username = String.valueOf(prepend_as2) + as2Info.getProperty("as2-to", "").toLowerCase();
                }
                if (actual_as2_username.indexOf("-_-") >= 0) {
                    actual_as2_username = actual_as2_username.substring(0, actual_as2_username.indexOf("-_-"));
                }
                if (!ServerStatus.BG("as2_from_as_to_for_username")) {
                    this.thisSessionHTTP.thisSession.login_user_pass(true, true, actual_as2_username, "");
                } else {
                    actual_as2_username = String.valueOf(prepend_as2) + as2Info.getProperty("as2-from", "");
                    if (ServerStatus.BG("uppercase_usernames")) {
                        actual_as2_username = String.valueOf(prepend_as2) + as2Info.getProperty("as2-from", "").toUpperCase();
                    }
                    if (ServerStatus.BG("lowercase_usernames")) {
                        actual_as2_username = String.valueOf(prepend_as2) + as2Info.getProperty("as2-from", "").toLowerCase();
                    }
                    if (actual_as2_username.indexOf("-_-") >= 0) {
                        actual_as2_username = actual_as2_username.substring(0, actual_as2_username.indexOf("-_-"));
                    }
                    this.thisSessionHTTP.thisSession.login_user_pass(true, true, actual_as2_username, "");
                }
                if (!this.thisSessionHTTP.thisSession.uiBG("user_logged_in")) {
                    return false;
                }
                this.thisSessionHTTP.thisSession.user_info.put("user_logged_in", "false");
                validate_by_decrypt_payload = true;
            }
            Properties user = this.thisSessionHTTP.thisSession.user;
            if (validate_by_decrypt_payload) {
                if (user == null) {
                    return false;
                }
                if (user.getProperty("as2EncryptKeystorePath", this.thisSessionHTTP.thisSession.getProperty("as2EncryptKeystorePath", "")).equals("")) {
                    return false;
                }
                if (user.getProperty("as2EncryptKeyAlias", this.thisSessionHTTP.thisSession.getProperty("as2EncryptKeyAlias", "")).equals("")) {
                    return false;
                }
            }
            if (write100Continue) {
                this.thisSessionHTTP.writeCookieAuth = false;
                this.write_command_http("HTTP/1.1 100 Continue");
                this.write_command_http("");
            }
            Vector payloads = new Vector();
            String messageId = as2Info.getProperty("message-id", "-NONE-");
            if (messageId.startsWith("<")) {
                messageId = messageId.substring(1, messageId.length() - 1);
            }
            String contentType = as2Info.getProperty("contentType");
            boolean mdnResponse = false;
            if (as2Info.getProperty("contentType").toLowerCase().indexOf("disposition-notification") >= 0 || as2Info.getProperty("subject", "").toUpperCase().indexOf("DELIVERY NOTIFICATION") >= 0 || as2Info.getProperty("subject", "").toUpperCase().indexOf("DISPOSITION NOTIFICATION") >= 0 || !as2Info.getProperty("mdnbytes", "0").equals("0")) {
                mdnResponse = true;
            }
            if (as2Info.containsKey("filename")) {
                as2Info.put("as2Filename", as2Info.getProperty("filename"));
            }
            com.crushftp.client.Common.putAllSafe(this.thisSessionHTTP.thisSession.user_info, as2Info);
            String data0 = headers.elementAt(0).toString();
            data0 = data0.substring(data0.indexOf(" ") + 1, data0.lastIndexOf(" "));
            if (!data0.endsWith("/")) {
                data0 = String.valueOf(data0) + "/";
            }
            this.thisSessionHTTP.cd(data0);
            this.thisSessionHTTP.setupSession();
            String the_dir = this.thisSessionHTTP.pwd();
            if (!the_dir.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                the_dir = String.valueOf(this.thisSessionHTTP.thisSession.SG("root_dir")) + the_dir.substring(1);
            }
            String boundary = "";
            String mdn = null;
            As2Msg m = new As2Msg();
            Properties info = new Properties();
            Object outData = null;
            boolean inerror = false;
            String keystorePass = user.getProperty("as2EncryptKeystorePassword", this.thisSessionHTTP.thisSession.getProperty("as2EncryptKeystorePassword", ""));
            String signstorePass = user.getProperty("as2SignKeystorePassword", this.thisSessionHTTP.thisSession.getProperty("as2SignKeystorePassword", ""));
            String keyPass = user.getProperty("as2EncryptKeystorePassword", this.thisSessionHTTP.thisSession.getProperty("as2EncryptKeystorePassword", ""));
            if (user.getProperty("userVersion", "4").equals("4") || user.getProperty("userVersion", "4").equals("5")) {
                keystorePass = ServerStatus.thisObj.common_code.encode_pass(keystorePass, "DES", "");
                keyPass = ServerStatus.thisObj.common_code.encode_pass(keyPass, "DES", "");
            }
            try {
                this.thisSessionHTTP.cd(String.valueOf(the_dir) + as2Info.getProperty("as2Filename"));
                byte[] inData = null;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                this.thisSessionHTTP.doPutFile(http_len_max, this.thisSessionHTTP.done, headers, baos, String.valueOf(the_dir) + as2Info.getProperty("as2Filename"), false, 0L, null);
                inData = baos.toByteArray();
                String tmpFilename = String.valueOf(System.currentTimeMillis()) + ".as2dump";
                if (Log.log("HTTP_SERVER", 4, "Raw File Data Dumped to disk:" + tmpFilename)) {
                    RandomAccessFile tmp = new RandomAccessFile(new File_U(tmpFilename), "rw");
                    tmp.write(inData);
                    tmp.close();
                }
                this.thisSessionHTTP.done = true;
                this.thisSessionHTTP.keepGoing = false;
                try {
                    outData = m.decryptData(info, inData, as2Info.getProperty("contentType"), user.getProperty("as2EncryptKeystoreFormat", this.thisSessionHTTP.thisSession.getProperty("as2EncryptKeystoreFormat", "PKCS12")), user.getProperty("as2EncryptKeystorePath", this.thisSessionHTTP.thisSession.getProperty("as2EncryptKeystorePath", ".keystore")), keystorePass, keyPass, user.getProperty("as2EncryptKeyAlias", this.thisSessionHTTP.thisSession.getProperty("as2EncryptKeyAlias", "")));
                }
                catch (Exception e) {
                    if (validate_by_decrypt_payload) {
                        Log.log("AS2_SERVER", 0, "AS2 LOGIN FAILED!  Decryption of data failed so user " + actual_as2_username + " is not valid.");
                        return false;
                    }
                    throw e;
                }
                info.put("content-disposition", as2Info.getProperty("content-disposition", ""));
                info.put("as2Filename", as2Info.getProperty("as2Filename"));
                String[] dnos = as2Info.getProperty("Disposition-Notification-Options".toLowerCase(), "").split(";");
                String mic_alg = null;
                try {
                    int x2 = 0;
                    while (x2 < dnos.length && mic_alg == null) {
                        if (dnos[x2].trim().toLowerCase().startsWith("signed-receipt-micalg")) {
                            String[] mic_algs = dnos[x2].trim().toLowerCase().split("=")[1].trim().split(",");
                            int xx = 0;
                            while (xx < mic_algs.length && mic_alg == null) {
                                if (mic_algs[xx].trim().equalsIgnoreCase("sha-256")) {
                                    mic_alg = "sha-256";
                                } else if (mic_algs[xx].trim().equalsIgnoreCase("sha1")) {
                                    mic_alg = "sha1";
                                } else if (mic_algs[xx].trim().equalsIgnoreCase("md5")) {
                                    mic_alg = "md5";
                                }
                                ++xx;
                            }
                        }
                        ++x2;
                    }
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                }
                if (mic_alg == null) {
                    mic_alg = "sha1";
                }
                info.put("mic_alg", mic_alg);
                try {
                    payloads = m.getPayloadsAndMic(info, outData, user.getProperty("as2SignKeystoreFormat", this.thisSessionHTTP.thisSession.getProperty("as2SignKeystoreFormat", "PKCS12")), user.getProperty("as2SignKeystorePath", this.thisSessionHTTP.thisSession.getProperty("as2SignKeystorePath", "")), signstorePass, user.getProperty("as2SignKeyAlias", this.thisSessionHTTP.thisSession.getProperty("as2SignKeyAlias", "")), mic_alg);
                    if (validate_by_decrypt_payload) {
                        this.thisSessionHTTP.thisSession.user_info.put("user_logged_in", "true");
                    }
                }
                catch (Exception e) {
                    if (validate_by_decrypt_payload) {
                        Log.log("AS2_SERVER", 0, "AS2 LOGIN FAILED!  Signature verification of data failed so user " + actual_as2_username + " is not valid.");
                        Log.log("AS2_SERVER", 0, e);
                        return false;
                    }
                    throw e;
                }
                if (info.getProperty("contentType", "").toLowerCase().indexOf("disposition-notification") >= 0 || mdnResponse) {
                    mdnResponse = true;
                    if (contentType.toLowerCase().indexOf("disposition-notification") < 0 && info.getProperty("contentType", "").toLowerCase().indexOf("disposition-notification") >= 0) {
                        contentType = info.getProperty("contentType", "");
                    }
                }
            }
            catch (Exception e) {
                com.crushftp.client.Common.putAllSafe(this.thisSessionHTTP.thisSession.user_info, as2Info);
                Log.log("SERVER", 0, e);
                payloads = new Vector();
                inerror = true;
                this.thisSessionHTTP.thisSession.doErrorEvent(e);
            }
            if (!mdnResponse) {
                Log.log("AS2_SERVER", 1, "AS2:Payloads:" + payloads.size() + ":" + payloads);
                Log.log("AS2_SERVER", 1, "AS2:info:" + info);
                Log.log("AS2_SERVER", 2, "AS2:as2Info:" + as2Info);
                String disp_state = "automatic-action/MDN-sent-automatically; " + (inerror ? "failed" : "processed");
                mdn = m.createMDN(info.getProperty("mic", ""), info.getProperty("mic_alg", "sha1"), as2Info.getProperty("signMdn", "false").equals("true"), as2Info.getProperty("as2-to", ""), messageId, disp_state, "Received AS2 message.", user.getProperty("as2EncryptKeystoreFormat", this.thisSessionHTTP.thisSession.getProperty("as2EncryptKeystoreFormat", "PKCS12")), user.getProperty("as2EncryptKeystorePath", this.thisSessionHTTP.thisSession.getProperty("as2EncryptKeystorePath", ".keystore")), keystorePass, keyPass, user.getProperty("as2EncryptKeyAlias", this.thisSessionHTTP.thisSession.getProperty("as2EncryptKeyAlias", "")));
                Log.log("AS2_SERVER", 1, "AS2:MDN:" + mdn);
                BufferedReader sr = new BufferedReader(new StringReader(mdn));
                while (boundary.equals("")) {
                    boundary = sr.readLine().trim();
                }
                sr.close();
            }
            com.crushftp.client.Common.putAllSafe(this.thisSessionHTTP.thisSession.user_info, as2Info);
            int x3 = 0;
            while (x3 < payloads.size()) {
                boolean ok = false;
                if (this.thisSessionHTTP.thisSession.check_access_privs(this.thisSessionHTTP.pwd(), "STOR") && Common.filter_check("U", Common.last(this.thisSessionHTTP.pwd()), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSessionHTTP.thisSession.SG("file_filter"))) {
                    ok = true;
                } else {
                    this.thisSessionHTTP.thisSession.add_log_formatted("550 STOR error: Upload attempt was rejected because the block matching names! File name :" + Common.last(this.thisSessionHTTP.pwd()) + " Filters :" + ServerStatus.SG("filename_filters_str"), "STOR");
                }
                if (ok) {
                    Properties uploadStat1 = null;
                    Properties uploadStat2 = null;
                    Object o = payloads.elementAt(x3);
                    if (o instanceof File_U) {
                        File_U f = (File_U)o;
                        this.thisSessionHTTP.cd(String.valueOf(Common.all_but_last(this.thisSessionHTTP.pwd())) + f.getName());
                        Properties result = ServerSessionHTTP.getStorOutputStream(this.thisSessionHTTP.thisSession, String.valueOf(Common.all_but_last(this.thisSessionHTTP.pwd())) + f.getName(), 0L, false, null);
                        STOR_handler stor = (STOR_handler)result.remove("stor");
                        OutputStream of_stream = (OutputStream)result.remove("out");
                        com.crushftp.client.Common.copyStreams(new FileInputStream(f), of_stream, true, true);
                        while (stor != null && stor.active2.getProperty("active", "").equals("true")) {
                            Thread.sleep(100L);
                        }
                        stor = null;
                        uploadStat1 = this.thisSessionHTTP.thisSession.uiPG("lastUploadStat");
                    } else {
                        String filename;
                        Properties payload = (Properties)o;
                        byte[] b = (byte[])payload.get("data");
                        Properties mdnInfo = null;
                        String ext = "";
                        if (mdnResponse) {
                            ext = ".mdn";
                            try {
                                try {
                                    mdnInfo = m.parseMDN(b, contentType);
                                }
                                catch (Exception e) {
                                    this.thisSessionHTTP.thisSession.doErrorEvent(e);
                                    Log.log("HTTP_SERVER", 1, e);
                                }
                                if (mdnInfo == null) {
                                    mdnInfo = new Properties();
                                    BufferedReader br = new BufferedReader(new StringReader(new String(b)));
                                    String line = "";
                                    while ((line = br.readLine()) != null) {
                                        if (line.indexOf(":") <= 0) continue;
                                        String key = line.substring(0, line.indexOf(":")).toLowerCase().trim();
                                        mdnInfo.put(key, line.substring(line.indexOf(":") + 1).trim());
                                    }
                                }
                                payload.put("name", mdnInfo.getProperty("Original-Message-ID".toLowerCase()).replace('<', '_').replace('>', '_').replace('/', '_'));
                                b = mdnInfo.toString().getBytes("UTF8");
                            }
                            catch (Exception e) {
                                com.crushftp.client.Common.putAllSafe(this.thisSessionHTTP.thisSession.user_info, as2Info);
                                this.thisSessionHTTP.thisSession.doErrorEvent(e);
                                Log.log("HTTP_SERVER", 1, e);
                            }
                        }
                        String originalDir = Common.all_but_last(this.thisSessionHTTP.pwd());
                        String payload_name = payload.getProperty("name", "");
                        if (this.thisSessionHTTP.thisSession.BG("as2_append_guid")) {
                            payload_name = String.valueOf(payload_name) + Variables.uidg() + "_" + Common.makeBoundary(5);
                        }
                        filename = (filename = String.valueOf(originalDir) + payload_name).endsWith(".") && ext.startsWith(".") ? String.valueOf(filename) + ext.substring(1) : String.valueOf(filename) + ext;
                        this.thisSessionHTTP.cd(filename);
                        Properties result = ServerSessionHTTP.getStorOutputStream(this.thisSessionHTTP.thisSession, String.valueOf(filename) + ".zipstream", 0L, false, null);
                        STOR_handler stor = (STOR_handler)result.remove("stor");
                        Properties active = (Properties)result.get("active");
                        OutputStream of_stream = (OutputStream)result.remove("out");
                        ZipOutputStream zout = new ZipOutputStream(of_stream);
                        zout.setLevel(0);
                        String tmp_path = filename;
                        if (tmp_path.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                            tmp_path = Common.last(tmp_path);
                        }
                        zout.putNextEntry(new ZipEntry(tmp_path));
                        com.crushftp.client.Common.copyStreams(new ByteArrayInputStream(b), zout, true, false);
                        while (!active.getProperty("streamOpenStatus", "").equals("PENDING") && !active.getProperty("streamOpenStatus", "").equals("OPEN")) {
                            Thread.sleep(100L);
                        }
                        zout.closeEntry();
                        if (!mdnResponse) {
                            String filename2 = String.valueOf(originalDir) + messageId.replace('<', '_').replace('>', '_').replace('/', '_') + ".out.mdn";
                            this.thisSessionHTTP.cd(filename2);
                            tmp_path = filename2;
                            if (tmp_path.startsWith(this.thisSessionHTTP.thisSession.SG("root_dir"))) {
                                tmp_path = Common.last(tmp_path);
                            }
                            zout.putNextEntry(new ZipEntry(tmp_path));
                            com.crushftp.client.Common.copyStreams(new ByteArrayInputStream(mdn.getBytes("UTF8")), zout, true, false);
                            zout.closeEntry();
                            uploadStat2 = this.thisSessionHTTP.thisSession.uiPG("lastUploadStat");
                        }
                        zout.finish();
                        zout.close();
                        while (active.getProperty("active", "").equals("true")) {
                            Thread.sleep(100L);
                        }
                        stor = null;
                        com.crushftp.client.Common.putAllSafe(this.thisSessionHTTP.thisSession.user_info, as2Info);
                        try {
                            if (mdnInfo != null) {
                                this.thisSessionHTTP.thisSession.uVFS.reset();
                                Log.log("AS2_SERVER", 0, "MDN_Filename:" + filename);
                                Properties as2_item = new Properties();
                                as2_item.put("b", b);
                                as2_item.put("msgInfo", as2Info);
                                as2_item.put("mdnInfo", mdnInfo);
                                Properties mdn_item = this.thisSessionHTTP.thisSession.uVFS.get_item(filename);
                                mdn_item.put("path", new VRL(mdn_item.getProperty("url")).getPath());
                                mdn_item.put("the_file_path", mdn_item.getProperty("path"));
                                mdn_item.put("the_file_name", mdn_item.getProperty("name"));
                                as2_item.put("mdn_item", mdn_item);
                                Log.log("AS2_SERVER", 0, "as2_item for:" + mdnInfo.getProperty("Original-Message-ID".toLowerCase()) + " prop=" + as2_item);
                                As2Msg.mdnResponses.put(mdnInfo.getProperty("Original-Message-ID".toLowerCase()), as2_item);
                                if (com.crushftp.client.Common.dmz_mode) {
                                    Vector queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                    Properties action = new Properties();
                                    action.put("type", "PUT:AS2MDN");
                                    action.put("id", Common.makeBoundary());
                                    action.put("as2_item", as2_item);
                                    action.put("need_response", "false");
                                    queue.addElement(action);
                                }
                                Thread.sleep(5000L);
                                As2Msg.mdnResponses.remove(mdnInfo.getProperty("Original-Message-ID".toLowerCase()));
                            }
                        }
                        catch (Exception e) {
                            this.thisSessionHTTP.thisSession.doErrorEvent(e);
                            Log.log("HTTP_SERVER", 1, e);
                        }
                        uploadStat1 = this.thisSessionHTTP.thisSession.uiPG("lastUploadStat");
                    }
                    try {
                        if (uploadStat1 != null) {
                            com.crushftp.client.Common.putAllSafe(uploadStat1, as2Info);
                        }
                        if (uploadStat2 != null) {
                            com.crushftp.client.Common.putAllSafe(uploadStat2, as2Info);
                        }
                        int loops = 0;
                        while (this.thisSessionHTTP.thisSession.uiPG("lastUploadStat") == null && loops++ < 100) {
                            Thread.sleep(100L);
                        }
                        if (this.thisSessionHTTP.thisSession.uiPG("lastUploadStat") == null) {
                            com.crushftp.client.Common.putAllSafe(this.thisSessionHTTP.thisSession.user_info, as2Info);
                            this.write_command_http("HTTP/1.1 500 Error");
                            this.write_command_http("Pragma: no-cache");
                            this.thisSessionHTTP.write_standard_headers();
                            this.write_command_http("Content-Length: " + ("file transfer error".length() + 2));
                            this.write_command_http("");
                            this.write_command_http("file transfer error");
                            return false;
                        }
                    }
                    catch (Exception e) {
                        this.thisSessionHTTP.thisSession.doErrorEvent(e);
                        Log.log("HTTP_SERVER", 0, e);
                    }
                }
                ++x3;
            }
            String destUrl = null;
            if (!as2Info.getProperty("receipt-delivery-option", "").equals("")) {
                destUrl = as2Info.getProperty("receipt-delivery-option", "");
            }
            this.write_command_http("HTTP/1.1 200 OK");
            this.thisSessionHTTP.write_standard_headers();
            this.write_command_http("From: AS2");
            this.write_command_http("Message-ID: <AS2-" + new Date().getTime() + "-" + Common.makeBoundary(3) + "@" + as2Info.getProperty("as2-to", "") + "_" + as2Info.getProperty("as2-from", "") + ">");
            Thread.sleep(1L);
            this.write_command_http("Mime-Version: 1.0");
            if (boundary.length() > 0) {
                this.write_command_http("AS2-To: " + as2Info.getProperty("as2-from", ""));
                this.write_command_http("AS2-From: " + as2Info.getProperty("as2-to", ""));
                this.write_command_http("Subject: Message Delivery Notification");
                this.write_command_http("AS2-Version: 1.1");
                if (as2Info.getProperty("signMdn", "false").equals("true")) {
                    this.write_command_http("Content-Type: multipart/signed; boundary=\"" + boundary.substring(2) + "\"; protocol=\"application/pkcs7-signature\"; report-type=disposition-notification; micalg=" + info.getProperty("mic_alg", "sha1") + "; charset=utf-8");
                } else {
                    this.write_command_http("Content-Type: multipart/report; boundary=\"" + boundary.substring(2) + "\"; report-type=disposition-notification; micalg=" + info.getProperty("mic_alg", "sha1") + "; charset=utf-8");
                }
            }
            if (destUrl == null && mdn != null) {
                this.write_command_http("Content-Length: " + mdn.length());
                this.write_command_http("");
                this.write_command_http(mdn);
            } else {
                this.write_command_http("Content-Length: 0");
                this.write_command_http("");
                if (mdn != null) {
                    String results = m.doAsyncMDNPost(null, "", "", as2Info, false, mdn, boundary, destUrl, user.getProperty("as2EncryptKeystorePath", this.thisSessionHTTP.thisSession.getProperty("as2EncryptKeystorePath", ".keystore")), keystorePass, keyPass, true, "(current_server)");
                    this.thisSessionHTTP.thisSession.add_log("[" + this.thisSessionHTTP.thisSession.uiSG("user_number") + ":" + this.thisSessionHTTP.thisSession.uiSG("user_name") + ":" + this.thisSessionHTTP.thisSession.uiSG("user_ip") + "] DATA: *" + results.trim() + "*", "HTTP");
                }
            }
            if (outData != null && outData instanceof File_U) {
                int loops = 0;
                while (!((File_U)outData).delete() && loops++ < 10) {
                    Thread.sleep(100L);
                }
            }
            if (this.thisSessionHTTP.thisSession != null) {
                if (com.crushftp.client.Common.dmz_mode) {
                    Vector queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                    Properties action = new Properties();
                    action.put("type", "PUT:BATCH_COMPLETE");
                    action.put("id", Common.makeBoundary());
                    Properties root_item = this.thisSessionHTTP.thisSession.uVFS.get_item(this.thisSessionHTTP.thisSession.SG("root_dir"));
                    GenericClient c = this.thisSessionHTTP.thisSession.uVFS.getClient(root_item);
                    action.put("crushAuth", c.getConfig("crushAuth"));
                    this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
                    action.put("user_ip", this.thisSessionHTTP.thisSession.uiSG("user_ip"));
                    action.put("request", request);
                    action.put("need_response", "false");
                    queue.addElement(action);
                } else {
                    this.thisSessionHTTP.thisSession.do_event5("BATCH_COMPLETE", null);
                }
            }
            return false;
        }
        if (http_len_max < 10240000L) {
            if (as2Info.getProperty("content-type", "").indexOf("multipart") >= 0) {
                String boundary = as2Info.getProperty("content-type", "").substring(as2Info.getProperty("content-type", "").toUpperCase().indexOf("BOUNDARY=") + "BOUNDARY=".length()).trim();
                Vector items = this.thisSessionHTTP.parsePostArguments(boundary, http_len_max, false, req_id);
                int x4 = 0;
                while (x4 < items.size()) {
                    Properties pp = (Properties)items.elementAt(x4);
                    request.putAll((Map<?, ?>)pp);
                    ++x4;
                }
            } else {
                this.thisSessionHTTP.thisSession.start_idle_timer(-20);
                String postData = this.thisSessionHTTP.get_raw_http_command((int)http_len_max);
                this.thisSessionHTTP.thisSession.stop_idle_timer();
                String[] postItems = postData.split("&");
                request.put("type", "text");
                boolean noResult = false;
                boolean merged_line = false;
                String merged = "";
                int x5 = 0;
                while (x5 < postItems.length) {
                    if (!postItems[x5].trim().equals("")) {
                        String name = Common.url_decode(postItems[x5].substring(0, postItems[x5].indexOf("=")));
                        String data_item = Common.url_decode(postItems[x5].substring(postItems[x5].indexOf("=") + 1));
                        request.put(name, data_item);
                        if (name.toUpperCase().indexOf("PASS") >= 0) {
                            data_item = "***********";
                        }
                        if (data_item.indexOf("<password>") >= 0 && data_item.indexOf("</password>") >= 0) {
                            data_item = String.valueOf(data_item.substring(0, data_item.indexOf("<password>") + "<password>".length())) + "*******" + data_item.substring(data_item.indexOf("</password>"));
                        } else if (data_item.toUpperCase().indexOf("PASS") >= 0 && !name.toUpperCase().equals("COMMAND")) {
                            data_item = String.valueOf(data_item.substring(0, data_item.indexOf(":") + 1)) + "*******";
                        }
                        if (name.equals("command") && data_item.equals("syncCommandResult")) {
                            noResult = true;
                        }
                        if (name.equals("command") && data_item.equals("getServerItem")) {
                            merged_line = true;
                        }
                        if (name.equals("command") && data_item.equals("getJobsSummary")) {
                            merged_line = true;
                        }
                        if (merged_line) {
                            merged = String.valueOf(merged) + "," + name + ":" + data_item;
                        }
                        if (noResult && name.equals("result")) {
                            this.thisSessionHTTP.thisSession.add_log_formatted(String.valueOf(name) + ": len=" + data_item.length(), "POST", req_id);
                        } else if (!merged_line) {
                            this.thisSessionHTTP.thisSession.add_log_formatted(String.valueOf(name) + ":" + data_item, "POST", req_id);
                        }
                    }
                    ++x5;
                }
                if (merged_line) {
                    this.thisSessionHTTP.thisSession.add_log_formatted(merged.substring(1), "POST", req_id);
                }
            }
            if (request.getProperty("encoded", "false").equals("true")) {
                Enumeration<Object> keys = request.keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement().toString();
                    request.put(key, Common.url_decode(request.getProperty(key)));
                }
            }
        }
        return true;
    }

    public void getUserInfo(String command, Properties site_item, String the_dir) throws Exception {
        String[] lines = new String[]{};
        String vfsRootItem = this.thisSessionHTTP.thisSession.uVFS.getRootVFS(the_dir, -1);
        if (the_dir.startsWith(vfsRootItem)) {
            the_dir = the_dir.substring(vfsRootItem.length() - 1);
        }
        if (new VRL(site_item.getProperty("url")).getProtocol().equalsIgnoreCase("FTP")) {
            GenericClient c = this.thisSessionHTTP.thisSession.uVFS.getClient(site_item);
            try {
                Properties extraCustomizations;
                String linesStr = c.doCommand("SITE " + command + " " + the_dir);
                if (linesStr != null) {
                    lines = linesStr.substring(4).split(";;;");
                }
                if ((extraCustomizations = (Properties)this.thisSessionHTTP.thisSession.get("extraCustomizations")) == null) {
                    extraCustomizations = new Properties();
                }
                int x = 0;
                while (x < lines.length) {
                    String key = lines[x].substring(0, lines[x].indexOf("=")).trim();
                    String val = lines[x].substring(lines[x].indexOf("=") + 1).trim();
                    extraCustomizations.put(key, val);
                    ++x;
                }
                this.thisSessionHTTP.thisSession.put("extraCustomizations", extraCustomizations);
            }
            finally {
                c = this.thisSessionHTTP.thisSession.uVFS.releaseClient(c);
            }
        }
    }

    public void write_command_http(String s) throws Exception {
        this.write_command_http(s, true);
    }

    public void write_command_http(String s, boolean log) throws Exception {
        this.thisSessionHTTP.write_command_http(s, log, true);
    }

    public boolean writeResponse(String response) throws Exception {
        return this.writeResponse(response, true, 200, true, false, true);
    }

    public boolean writeResponse(String response, boolean json) throws Exception {
        return this.writeResponse(response, true, 200, true, json, true);
    }

    public boolean writeResponse(String response, boolean log, int code, boolean convertVars, boolean json, boolean log_header) throws Exception {
        boolean acceptsGZIP = false;
        return this.writeResponse(response, log, code, convertVars, json, acceptsGZIP, log_header);
    }

    public boolean writeResponse(String response, boolean log, int code, boolean convertVars, boolean json, boolean acceptsGZIP, boolean log_header) throws Exception {
        if (convertVars) {
            response = ServerStatus.thisObj.change_vars_to_values(response, this.thisSessionHTTP.thisSession);
        }
        this.write_command_http("HTTP/1.1 " + code + " OK", log_header);
        this.write_command_http("Cache-Control: no-store", log_header);
        this.write_command_http("Pragma: no-cache", log_header);
        if (json) {
            this.write_command_http("Content-Type: application/jsonrequest;charset=utf-8");
        } else {
            this.write_command_http("Content-Type: text/" + (response.indexOf("<?xml") >= 0 ? "xml" : "plain") + ";charset=utf-8");
        }
        if (acceptsGZIP) {
            this.thisSessionHTTP.write_command_http("Vary: Accept-Encoding");
            this.thisSessionHTTP.write_command_http("Content-Encoding: gzip");
            this.thisSessionHTTP.write_command_http("Transfer-Encoding: chunked");
            this.thisSessionHTTP.write_command_http("Date: " + this.thisSessionHTTP.sdf_rfc1123.format(new Date()), log, true);
            this.thisSessionHTTP.write_command_http("Server: " + ServerStatus.SG("http_server_header"), log, true);
            this.thisSessionHTTP.write_command_http("P3P: CP=\"IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT\"", log, true);
            if (!ServerStatus.SG("Access-Control-Allow-Origin").equals("")) {
                String origin = this.thisSessionHTTP.headerLookup.getProperty("ORIGIN", "");
                int x = 0;
                while (x < ServerStatus.SG("Access-Control-Allow-Origin").split(",").length) {
                    boolean ok = false;
                    if (origin.equals("")) {
                        ok = true;
                    } else if (ServerStatus.SG("Access-Control-Allow-Origin").split(",")[x].toUpperCase().trim().equalsIgnoreCase(origin.toUpperCase().trim())) {
                        ok = true;
                    }
                    if (ok) {
                        this.write_command_http("Access-Control-Allow-Origin: " + ServerStatus.SG("Access-Control-Allow-Origin").split(",")[x].trim());
                    }
                    ++x;
                }
                this.write_command_http("Access-Control-Allow-Headers: authorization,content-type");
                this.write_command_http("Access-Control-Allow-Credentials: true");
                this.write_command_http("Access-Control-Allow-Methods: GET,POST,OPTIONS,PUT,PROPFIND,DELETE,MKCOL,MOVE,COPY,HEAD,PROPPATCH,LOCK,UNLOCK,ACL,TR");
            }
            this.write_command_http("", log);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] b = response.getBytes("UTF8");
            GZIPOutputStream out = new GZIPOutputStream(baos);
            ((OutputStream)out).write(b);
            out.finish();
            if (baos.size() > 0) {
                this.thisSessionHTTP.original_os.write((String.valueOf(Long.toHexString(baos.size())) + "\r\n").getBytes());
                baos.writeTo(this.thisSessionHTTP.original_os);
                this.thisSessionHTTP.original_os.write("\r\n".getBytes());
                baos.reset();
            }
            this.thisSessionHTTP.original_os.write("0\r\n\r\n".getBytes());
            this.thisSessionHTTP.original_os.flush();
        } else {
            this.thisSessionHTTP.write_standard_headers(log);
            int len = response.getBytes("UTF8").length + 2;
            if (len == 2) {
                len = 0;
            }
            this.write_command_http("Content-Length: " + len, log_header);
            this.write_command_http("", log);
            if (len > 0) {
                this.thisSessionHTTP.write_command_http(response, log, convertVars);
            }
        }
        this.thisSessionHTTP.thisSession.drain_log();
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String processKeywords(SessionCrush thisSession, String[] names, String keywords_raw) throws Exception {
        String response = "";
        String keyword = com.crushftp.client.Common.xss_strip(keywords_raw.trim());
        response = String.valueOf(response) + "<commandResult><response>";
        boolean ok = false;
        int x = 0;
        while (x < names.length) {
            String the_dir = Common.url_decode(Common.all_but_last(names[x]));
            if (the_dir.startsWith(thisSession.SG("root_dir"))) {
                the_dir = the_dir.substring(thisSession.SG("root_dir").length() - 1);
            }
            String path = thisSession.getStandardizedDir(the_dir);
            Properties item = thisSession.uVFS.get_item(String.valueOf(path) + Common.last(names[x]));
            the_dir = SearchHandler.getPreviewPath(item.getProperty("url"), "1", 1);
            String index = String.valueOf(ServerStatus.SG("previews_path")) + the_dir.substring(1);
            new File_S(Common.all_but_last(index)).mkdirs();
            if (ServerStatus.BG("exif_keywords")) {
                String srcFile = com.crushftp.client.Common.dots(new VRL(item.getProperty("url")).getPath());
                int xx = 0;
                while (xx < ServerStatus.thisObj.previewWorkers.size()) {
                    PreviewWorker preview = (PreviewWorker)ServerStatus.thisObj.previewWorkers.elementAt(xx);
                    if (preview.prefs.getProperty("preview_enabled", "false").equalsIgnoreCase("true") && preview.checkExtension(Common.last(the_dir), item)) {
                        preview.setExifInfo(srcFile, PreviewWorker.getDestPath2(String.valueOf(item.getProperty("url")) + "/p1/"), "keywords", com.crushftp.client.Common.xss_strip(keywords_raw).trim());
                        ok = true;
                        break;
                    }
                    ++xx;
                }
            } else if (new File_S(Common.all_but_last(Common.all_but_last(index))).exists()) {
                RandomAccessFile out = new RandomAccessFile(new File_S(String.valueOf(Common.all_but_last(Common.all_but_last(index))) + "index.txt"), "rw");
                out.seek(out.length());
                out.write((String.valueOf(keyword) + "\r\n").getBytes());
                out.close();
                ok = true;
                if (FileClient.memCache) {
                    Properties properties = SearchHandler.keywords_cache;
                    synchronized (properties) {
                        String s = (String)SearchHandler.keywords_cache.remove(index);
                        if (s != null) {
                            long total_size = ServerStatus.siLG("keywords_cache_size") - (long)s.length() - (long)index.length() - 10L;
                            ServerStatus.siPUT("keywords_cache_size", String.valueOf(total_size));
                        }
                    }
                }
            } else {
                response = String.valueOf(response) + "Keyword not added.  This file is not indexed. (" + names[x] + ")\r\n";
            }
            SearchHandler.buildEntry(item, thisSession.uVFS, "new", null);
            ++x;
        }
        if (ok) {
            response = String.valueOf(response) + "Keyword Added.\r\n";
        }
        return response;
    }

    public static void fixButtons(Vector buttons) {
        int x = buttons.size() - 1;
        while (x >= 0) {
            Properties button = (Properties)buttons.elementAt(x);
            button.put("requiredPriv", "");
            if (!button.containsKey("for_menu")) {
                button.put("for_menu", button.getProperty("forMenu", "true"));
            }
            if (!button.containsKey("for_context_menu")) {
                button.put("for_context_menu", button.getProperty("forContextMenu", "true"));
            }
            if (button.getProperty("key").equals("(upload):Crush Uploader") || button.getProperty("value").startsWith("javascript:loadCrushUpplet")) {
                buttons.remove(x);
            } else if (button.getProperty("key").equals("(zip):.ZIP") || button.getProperty("value").startsWith("javascript:zip_items")) {
                button.put("requiredPriv", "(read)");
                button.put("key", "(zip):ZipDownload");
                button.put("value", "javascript:performAction('zip');");
            } else if (button.getProperty("key").equals("(custom):Manage Download Basket") || button.getProperty("value").startsWith("javascript:manageBasket")) {
                button.put("requiredPriv", "(read)");
                button.put("key", "(showbasket):Show Basket");
                button.put("value", "javascript:performAction('showBasket');");
            } else if (button.getProperty("key").equals("(custom):Add To Download Basket")) {
                button.put("requiredPriv", "(read)");
                button.put("key", "(addbasket):Add To Basket");
                button.put("value", "javascript:performAction('addToBasket');");
            } else if (button.getProperty("key").startsWith("(rename):")) {
                button.put("requiredPriv", "(rename)");
            } else if (button.getProperty("key").startsWith("(delete):")) {
                button.put("requiredPriv", "(delete)");
            } else if (button.getProperty("key").startsWith("(download):")) {
                button.put("requiredPriv", "(read)");
            } else if (button.getProperty("key").startsWith("(zip):")) {
                button.put("requiredPriv", "(read)");
            } else if (button.getProperty("key").startsWith("(mkdir):")) {
                button.put("requiredPriv", "(makedir)");
            } else if (button.getProperty("key").startsWith("(upload):")) {
                button.put("requiredPriv", "(write)");
            } else if (button.getProperty("key").startsWith("(search):")) {
                button.put("requiredPriv", "(view)");
            } else if (button.getProperty("key").startsWith("(cut):")) {
                button.put("requiredPriv", "(rename)");
            } else if (button.getProperty("key").startsWith("(copy):")) {
                button.put("requiredPriv", "(read)");
            } else if (button.getProperty("key").startsWith("(paste):")) {
                button.put("requiredPriv", "(write)");
            } else if (button.getProperty("key").startsWith("(slideshow):")) {
                button.put("requiredPriv", "(slideshow)");
            } else if (button.getProperty("key").startsWith("(share):")) {
                button.put("requiredPriv", "(share)");
            } else if (button.getProperty("key").indexOf("Logout") >= 0) {
                button.put("for_menu", "true");
                button.put("for_context_menu", "false");
            }
            if (button.getProperty("value").indexOf("showPopup") >= 0) {
                button.put("value", Common.replace_str(button.getProperty("value"), "showPopup", "performAction"));
            }
            if (button.getProperty("value").indexOf("showDownloadBasket") >= 0) {
                button.put("value", "javascript:performAction('showBasket');");
                button.put("requiredPriv", "(read)");
            }
            if (button.getProperty("value").indexOf("addToBasket") >= 0) {
                button.put("value", "javascript:performAction('addToBasket');");
                button.put("requiredPriv", "(read)");
            }
            if (button.getProperty("value").indexOf("download()") >= 0) {
                button.put("value", "javascript:performAction('download');");
                button.put("requiredPriv", "(read)");
            }
            if (button.getProperty("value").indexOf("shareOption") >= 0 || button.getProperty("value").indexOf("shareDiv") >= 0) {
                button.put("value", "javascript:performAction('share');");
                button.put("requiredPriv", "(share)");
            }
            if (button.getProperty("value").indexOf("deleteDiv") >= 0) {
                button.put("value", "javascript:performAction('delete');");
                button.put("requiredPriv", "(delete)");
            }
            if (button.getProperty("value").indexOf("doCut") >= 0) {
                button.put("value", "javascript:performAction('cut');");
                button.put("requiredPriv", "(rename)");
            }
            if (button.getProperty("value").indexOf("doPaste") >= 0) {
                button.put("value", "javascript:performAction('paste');");
                button.put("requiredPriv", "(write)");
            }
            if (button.getProperty("value").indexOf("userOptions") >= 0) {
                button.put("value", "javascript:performAction('userOptions');");
            }
            if (button.getProperty("value").indexOf("slideshow") >= 0) {
                button.put("value", "javascript:performAction('slideshow');");
            }
            if ((button.getProperty("value").indexOf("makedir") >= 0 || button.getProperty("value").indexOf("createFolder") >= 0) && button.getProperty("value").indexOf("customShare") < 0) {
                button.put("value", "javascript:performAction('createFolder');");
            }
            if (button.getProperty("value").indexOf("search") >= 0) {
                button.put("value", "javascript:performAction('search');");
            }
            if (button.getProperty("value").indexOf("Login") >= 0) {
                button.put("value", "javascript:doLogout();");
            }
            if (button.getProperty("value").indexOf("/login.html") >= 0) {
                button.put("value", "javascript:doLogout();");
            }
            if (button.getProperty("value").indexOf("Logout") >= 0) {
                button.put("value", "javascript:doLogout();");
            }
            --x;
        }
    }

    public static String createShare(Vector path_items, Properties request, Vector web_customizations, String user_name, String linkedServer, Properties user, SimpleDateFormat date_time, SessionCrush thisSession) throws Exception {
        return ServerSessionAJAX.createShare(path_items, request, web_customizations, user_name, linkedServer, user, date_time, thisSession, false);
    }

    /*
     * Unable to fully structure code
     */
    public static String createShare(Vector path_items, Properties request, Vector web_customizations, String user_name, String linkedServer, Properties user, SimpleDateFormat date_time, SessionCrush thisSession, boolean publish_attach) throws Exception {
        block285: {
            block282: {
                if (ServerStatus.BG("secondary_login_via_email") && thisSession != null && user_name.indexOf("@") >= 0 && UserTools.user_email_cache.containsKey(String.valueOf(thisSession.uiSG("listen_ip_port")) + ":" + user_name.toUpperCase())) {
                    user_name = UserTools.user_email_cache.getProperty(String.valueOf(thisSession.uiSG("listen_ip_port")) + ":" + user_name.toUpperCase());
                }
                if (date_time == null) {
                    date_time = new SimpleDateFormat("MM/dd/yy", Locale.US);
                }
                response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
                paths = null;
                paths = request.getProperty("paths").indexOf(";") >= 0 ? Common.url_decode(request.getProperty("paths")).trim().split(";") : Common.url_decode(request.getProperty("paths")).trim().split("\r\n");
                tempUsername = Common.url_decode(request.getProperty("temp_username", ""));
                tempPassword = Common.url_decode(request.getProperty("temp_password", ""));
                if (!request.getProperty("shareUsername", "false").equalsIgnoreCase("true")) {
                    if (tempUsername.equals("")) {
                        tempUsername = Common.makeBoundarySimple(ServerStatus.IG("temp_accounts_length"));
                        tempPassword = Common.makeBoundarySimple(ServerStatus.IG("temp_accounts_length"));
                    } else if (new File_U(String.valueOf(ServerStatus.SG("temp_accounts_path")) + "storage/" + tempUsername + tempPassword + "/").exists() && !publish_attach || tempUsername.length() + tempPassword.length() < ServerStatus.IG("temp_accounts_length") * 2 || new File_S(String.valueOf(System.getProperty("crushftp.users")) + linkedServer + "/" + tempUsername).exists()) {
                        response = String.valueOf(response) + "<commandResult><response>";
                        response = String.valueOf(response) + "<username></username>";
                        response = String.valueOf(response) + "<password></password>";
                        response = String.valueOf(response) + "<message>Denied. User/pass token length must each be " + ServerStatus.IG("temp_accounts_length") + " characters.</message>";
                        response = String.valueOf(response) + "<url></url>";
                        response = String.valueOf(response) + "<error_response>denied</error_response>";
                        response = String.valueOf(response) + "</response></commandResult>";
                        return response;
                    }
                }
                ex1 = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
                ex2 = new SimpleDateFormat("MMddyyyyHHmm", Locale.US);
                shareToDomain = "";
                shareBodyEmailClient = "";
                shareFromDomain = "";
                maxLen = 255;
                if (Common.machine_is_windows()) {
                    maxLen -= new File_U(String.valueOf(ServerStatus.SG("temp_accounts_path")) + "accounts/").getCanonicalPath().length();
                }
                flash_shareAllowUploadsPrivs = "(read)(view)(resume)(write)(delete)(slideshow)(rename)(makedir)(deletedir)";
                maxExpireDays = 0;
                preserve_relative_path = false;
                hideShareFromRow = false;
                default_email_from = "";
                if (web_customizations != null) {
                    x = 0;
                    while (x < web_customizations.size()) {
                        cust = (Properties)web_customizations.elementAt(x);
                        if (cust.getProperty("key").equals("shareToDomain")) {
                            shareToDomain = cust.getProperty("value");
                        }
                        if (cust.getProperty("key").equals("EMAILFROM")) {
                            default_email_from = cust.getProperty("value");
                        }
                        if (cust.getProperty("key").equals("shareBodyEmailClient")) {
                            shareBodyEmailClient = cust.getProperty("value");
                        }
                        if (cust.getProperty("key").equals("shareFromDomain")) {
                            shareFromDomain = cust.getProperty("value");
                        }
                        if (cust.getProperty("key").equals("flash_shareAllowUploadsPrivs")) {
                            flash_shareAllowUploadsPrivs = cust.getProperty("value");
                        }
                        if (cust.getProperty("key").equals("EXPIREDAYSMAX")) {
                            maxExpireDays = Integer.parseInt(cust.getProperty("value").trim());
                        }
                        if (cust.getProperty("key").equals("sharePreserveRelativePath")) {
                            preserve_relative_path = cust.getProperty("value").trim().equalsIgnoreCase("true");
                        }
                        if (cust.getProperty("key").equals("hideShareFromRow")) {
                            hideShareFromRow = cust.getProperty("value").trim().equalsIgnoreCase("true");
                        }
                        ++x;
                    }
                }
                requestExpire = ex1.parse(request.getProperty("expire", "1/1/1970 00:01").replace('+', ' '));
                gc = new GregorianCalendar();
                gc.setTime(new Date());
                gc.add(5, maxExpireDays);
                if (!(maxExpireDays <= 0 || request.containsKey("expire") && requestExpire.getTime() <= gc.getTime().getTime())) {
                    requestExpire = gc.getTime();
                }
                expire_date = ex2.format(requestExpire);
                request.put("expire", ex1.format(requestExpire));
                temp2 = Common.dots(Common.url_decode(request.getProperty("TempAccount", ""))).replace('/', '_').replace(',', '_').replace('=', '_');
                if (!temp2.equals("") && !temp2.startsWith("_") && temp2.indexOf("_") > 0) {
                    temp2 = temp2.substring(temp2.indexOf("_"));
                }
                folderName = "u=" + tempUsername + ",,p=" + tempPassword + ",,m=" + user_name + ",,t=TempAccount" + temp2 + ",,ex=" + expire_date;
                if (request.getProperty("logins", "").trim().equals("-1")) {
                    request.remove("logins");
                }
                if (!request.getProperty("logins", "").equals("")) {
                    folderName = String.valueOf(folderName) + ",,i=" + request.getProperty("logins", "");
                }
                userHome = String.valueOf(ServerStatus.SG("temp_accounts_path")) + "accounts/" + folderName + "/";
                userStorage = String.valueOf(ServerStatus.SG("temp_accounts_path")) + "storage/" + tempUsername + tempPassword + "/";
                permissions = new Properties();
                msg = "";
                baseUrl = Common.url_decode(request.getProperty("baseUrl"));
                baseUrl = Common.replace_str(baseUrl, "{username}", tempUsername);
                baseUrl = Common.replace_str(baseUrl, "{password}", tempPassword);
                baseUrl = Common.replace_str(baseUrl, "{user}", tempUsername);
                baseUrl = Common.replace_str(baseUrl, "{pass}", tempPassword);
                webLink = "";
                webLink = baseUrl.indexOf("?") >= 0 ? baseUrl : String.valueOf(baseUrl) + "?u=" + tempUsername + "&p=" + tempPassword;
                webLinkEnd = "?u=" + tempUsername + "&p=" + tempPassword;
                if (request.getProperty("direct_link", "false").equalsIgnoreCase("true") && paths.length == 1 && !paths[0].trim().endsWith("/")) {
                    webLink = String.valueOf(webLink) + "&path=/" + (preserve_relative_path != false ? (String.valueOf(Common.all_but_last(paths[0])) + Common.url_encode(Common.last(paths[0]))).substring(1) : Common.url_encode(Common.last(paths[0])));
                    webLinkEnd = String.valueOf(webLinkEnd) + "&path=/" + (preserve_relative_path != false ? (String.valueOf(Common.all_but_last(paths[0])) + Common.url_encode(Common.last(paths[0]))).substring(1) : Common.url_encode(Common.last(paths[0])));
                }
                request.put("account_path", userHome);
                request.put("storage_path", userStorage);
                request.put("master", user_name);
                if (user != null) {
                    master_email = user.getProperty("email", "");
                    if (master_email.equals("")) {
                        master_email = user.getProperty("ldap_mail", "");
                    }
                    if (master_email.equals("") && !request.getProperty("emailFrom", "").equals("")) {
                        master_email = request.getProperty("emailFrom", "");
                    }
                    request.put("master_email", master_email);
                }
                request.put("created", ex1.format(new Date()));
                request.put("username", tempUsername);
                request.put("password", tempPassword);
                request.put("web_link", webLink);
                request.put("web_link_end", webLinkEnd);
                stop_share = false;
                if (!shareToDomain.equals("") && request.getProperty("sendEmail", "").equals("true")) {
                    emails = String.valueOf(request.getProperty("emailTo")) + "," + request.getProperty("emailCc") + "," + request.getProperty("emailBcc");
                    tos = emails.replace('+', ' ').trim().replace(';', ',').split(",");
                    ok = true;
                    x = 0;
                    while (x < tos.length && ok) {
                        to = tos[x];
                        if (!to.trim().equals("")) {
                            allowed_domain = false;
                            xx = 0;
                            while (xx < shareToDomain.split(",").length) {
                                if (to.toUpperCase().trim().endsWith(shareToDomain.split(",")[xx].toUpperCase().trim())) {
                                    allowed_domain = true;
                                    break;
                                }
                                ++xx;
                            }
                            if (!allowed_domain) {
                                ok = false;
                                break;
                            }
                        }
                        ++x;
                    }
                    if (!ok) {
                        msg = "The To, Cc, or Bcc email does not end with one of the required domain(s): " + shareToDomain;
                    }
                    if (!msg.equals("")) {
                        stop_share = true;
                    }
                }
                if (!shareFromDomain.equals("") && request.getProperty("sendEmail", "").equals("true")) {
                    ok = false;
                    from = request.getProperty("emailFrom", "").replace('+', ' ').trim();
                    xx = 0;
                    while (xx < shareFromDomain.split(",").length) {
                        if (from.toUpperCase().trim().endsWith(shareFromDomain.split(",")[xx].toUpperCase().trim())) {
                            ok = true;
                            break;
                        }
                        ++xx;
                    }
                    if (!ok) {
                        msg = String.valueOf(msg) + "The FROM: email does not end with one of the required domain(s): " + shareFromDomain + ".";
                    }
                    if (!ok && shareFromDomain.indexOf("@") >= 0) {
                        msg = "The FROM: email is invalid: " + from;
                    }
                    if (ok) {
                        ok = false;
                        reply_to = request.getProperty("emailReplyTo", "").replace('+', ' ').trim();
                        xx = 0;
                        while (xx < shareFromDomain.split(",").length) {
                            if (!shareFromDomain.split(",")[xx].toUpperCase().trim().equals("") && (reply_to.toUpperCase().trim().endsWith(shareFromDomain.split(",")[xx].toUpperCase().trim()) || reply_to.trim().equals(""))) {
                                ok = true;
                                break;
                            }
                            ++xx;
                        }
                        if (!ok && shareFromDomain.indexOf("@") >= 0) {
                            msg = String.valueOf(msg) + "The REPLY TO: email is invalid: " + from;
                        } else if (!ok) {
                            msg = String.valueOf(msg) + "The REPLY TO: email does not end with one of the required domain(s): " + shareFromDomain + ".";
                        }
                    }
                    if (!msg.equals("")) {
                        stop_share = true;
                    }
                }
                last_name = Common.url_encode(Common.last(paths[0]));
                files = new Vector<File_U>();
                remote_files = new Vector<Properties>();
                lastStat = null;
                total_size = 0L;
                x = 0;
                while (x < path_items.size() && msg.equals("")) {
                    block279: {
                        block281: {
                            block280: {
                                stat = (Properties)path_items.elementAt(x);
                                vrl = new VRL(stat.getProperty("url"));
                                privs = stat.getProperty("privs", "(read)(share)(delete)(view)");
                                pgp_addon = "";
                                xx = 0;
                                while (xx < privs.split("\\(").length) {
                                    priv = privs.split("\\(")[xx];
                                    if (!priv.equals("") && (priv = priv.substring(0, priv.length() - 1).trim()).indexOf("=") >= 0 && priv.indexOf("pgp") >= 0) {
                                        pgp_addon = String.valueOf(pgp_addon) + "(" + priv.split("=")[0] + "=" + priv.substring(priv.indexOf("=") + 1) + ")";
                                    }
                                    ++xx;
                                }
                                permissions.put("/", "(read)(view)(resume)(slideshow)" + pgp_addon);
                                if (privs.indexOf("(share)") < 0) {
                                    msg = String.valueOf(msg) + "Not allowed to share these files:" + stat.getProperty("root_dir") + stat.getProperty("name") + "\r\n<br/>";
                                    stop_share = true;
                                }
                                if (!request.getProperty("shareUsername", "false").equalsIgnoreCase("true")) break block279;
                                if (ServerStatus.siIG("enterprise_level") > 0) break block280;
                                msg = String.valueOf(msg) + "The server does not have an enterprise license, so sharing to usernames is not allowed.\r\n<br/>";
                                stop_share = true;
                                break block281;
                            }
                            tempUsername = "";
                            tempPassword = "";
                            webLink = String.valueOf(baseUrl) + "Shares/" + user_name + "/";
                            webLinkEnd = "Shares/" + user_name + "/";
                            if (request.getProperty("direct_link", "false").equalsIgnoreCase("true") && paths.length == 1 && !paths[0].trim().endsWith("/")) {
                                webLink = String.valueOf(baseUrl) + "Shares/" + user_name + "/" + Common.url_encode(Common.last(paths[0]));
                                webLinkEnd = String.valueOf(webLinkEnd) + "Shares/" + user_name + "/" + Common.url_encode(Common.last(paths[0]));
                            }
                            request.put("master", user_name);
                            request.put("username", tempUsername);
                            request.put("password", tempPassword);
                            request.put("web_link", webLink);
                            request.put("web_link_end", webLinkEnd);
                            shareUsernames = Common.url_decode(request.getProperty("shareUsernames").replace('+', ' ').trim()).split(",");
                            share_to_emails = "";
                            xx = 0;
                            while (xx < shareUsernames.length) {
                                shareUsernames[xx] = shareUsernames[xx].trim().replace('/', '_').replace('\\', '_').replace('%', '_').replace(':', '_').replace(';', '_').replace('>', '_').replace('<', '_').replace('\"', '_').replace('*', '_');
                                toUser = String.valueOf(shareUsernames[xx].trim()) + ".SHARED";
                                shareVFS = null;
                                try {
                                    if (shareUsernames[xx].length() > 50) {
                                        msg = String.valueOf(msg) + "No such username exsits: " + shareUsernames[xx].trim() + "\r\n<br/>";
                                        stop_share = true;
                                        break;
                                    }
                                    if (!UserTools.ut.getUser(linkedServer, toUser, false).getProperty("user_name").equals(toUser)) {
                                        throw new NullPointerException();
                                    }
                                    if (ServerStatus.BG("validate_internal_share_username") && UserTools.ut.getUser(linkedServer, shareUsernames[xx].trim(), false) == null) {
                                        msg = String.valueOf(msg) + "No such username exsits: " + shareUsernames[xx].trim() + "\r\n<br/>";
                                        stop_share = true;
                                        break;
                                    }
                                    shareVFS = UserTools.ut.getVFS(linkedServer, toUser);
                                }
                                catch (NullPointerException e) {
                                    sharedUser = new Properties();
                                    sharedUser.put("password", "");
                                    sharedUser.put("version", "1.0");
                                    sharedUser.put("root_dir", "/");
                                    sharedUser.put("userVersion", "5");
                                    sharedUser.put("max_logins", "-1");
                                    UserTools.writeUser(linkedServer, toUser, sharedUser);
                                    shareVFS = UserTools.ut.getVFS(linkedServer, toUser);
                                }
                                if (user != null && default_email_from.equals("")) {
                                    user_email = user.getProperty("email", "");
                                    if (user_email.equals("")) {
                                        user_email = user.getProperty("ldap_mail", "");
                                    }
                                    if (user_email.equals("") && !request.getProperty("emailFrom", "").equals("")) {
                                        user_email = request.getProperty("emailFrom", "");
                                    }
                                    request.put("emailFrom", user_email);
                                }
                                request.put("emailCc", "");
                                request.put("emailBcc", "");
                                UserTools.addPriv(linkedServer, toUser, "/Shares/", "(view)(read)", 0, shareVFS);
                                sPrivs1 = request.getProperty("shareUsernamePermissions").toLowerCase();
                                sPrivs2 = "";
                                priv_parts = stat.getProperty("privs").toLowerCase().split("\\(");
                                loop = 0;
                                while (loop < priv_parts.length) {
                                    priv = priv_parts[loop];
                                    if (!(priv = priv.trim()).equals("")) {
                                        if (sPrivs1.indexOf("(" + (priv = priv.substring(0, priv.lastIndexOf(")"))) + ")") >= 0) {
                                            sPrivs2 = String.valueOf(sPrivs2) + "(" + priv + ")";
                                        } else if (priv.startsWith("quota") && sPrivs1.indexOf("(quota") >= 0) {
                                            sPrivs2 = String.valueOf(sPrivs2) + "(" + priv + ")";
                                        }
                                    }
                                    ++loop;
                                }
                                Log.log("HTTP_SERVER", 2, "Requested privs:" + sPrivs1);
                                Log.log("HTTP_SERVER", 2, "Adding privs:" + sPrivs2);
                                existingItem = shareVFS.get_item("/Shares/" + user_name + "/" + stat.getProperty("name"));
                                if (existingItem == null) ** GOTO lbl316
                                share_url = vrl.toString();
                                if (share_url.startsWith("file:/") && !share_url.startsWith("file://") && existingItem.getProperty("url").startsWith("file://")) {
                                    share_url = share_url.replaceAll("file:/", "file://");
                                }
                                if (share_url.startsWith("FILE:/") && !share_url.startsWith("FILE://") && existingItem.getProperty("url").startsWith("FILE://")) {
                                    share_url = share_url.replaceAll("FILE:/", "FILE://");
                                }
                                if (existingItem.getProperty("url").startsWith("file:/") && !existingItem.getProperty("url").startsWith("file://") && share_url.startsWith("file://")) {
                                    share_url = share_url.replaceAll("file://", "file:/");
                                }
                                if (existingItem.getProperty("url").startsWith("FILE:/") && !existingItem.getProperty("url").startsWith("FILE://") && share_url.startsWith("FILE://")) {
                                    share_url = share_url.replaceAll("FILE://", "FILE:/");
                                }
                                if (existingItem.getProperty("url").equals(share_url)) {
                                    msg = String.valueOf(msg) + "<p>ERROR: Skip internal share of " + shareUsernames[xx].trim() + " (" + "/Shares/" + user_name + "/" + stat.getProperty("name") + ")  as already exists!<p>";
                                } else {
                                    xxx = 1;
                                    while (xxx < 999) {
                                        tmp_name = stat.getProperty("name");
                                        tmp_name = tmp_name.indexOf(".") < 0 ? String.valueOf(tmp_name) + "_(" + xxx + ")" : String.valueOf(tmp_name.substring(0, tmp_name.lastIndexOf("."))) + "_(" + xxx + ")" + tmp_name.substring(tmp_name.lastIndexOf("."));
                                        if (shareVFS.get_item("/Shares/" + user_name + "/" + tmp_name) == null) {
                                            if (request.getProperty("emailSubject", "").contains(stat.getProperty("name"))) {
                                                request.put("emailSubject", request.getProperty("emailSubject", "").replaceAll(stat.getProperty("name"), tmp_name));
                                            }
                                            if (request.getProperty("emailBody", "").contains(stat.getProperty("name"))) {
                                                request.put("emailBody", request.getProperty("emailBody", "").replaceAll(stat.getProperty("name"), tmp_name));
                                            }
                                            stat.put("name", tmp_name);
                                            break;
                                        }
                                        ++xxx;
                                    }
lbl316:
                                    // 3 sources

                                    if (!stop_share) {
                                        Log.log("HTTP_SERVER", 2, "Adding priv to path:/Shares/" + user_name + "/" + stat.getProperty("name"));
                                        UserTools.addPriv(linkedServer, toUser, "/Shares/" + user_name + "/" + stat.getProperty("name") + (stat.getProperty("type", "DIR").equalsIgnoreCase("DIR") != false ? "/" : ""), String.valueOf(sPrivs2) + pgp_addon, 0, shareVFS);
                                        UserTools.addFolder(linkedServer, toUser, "/", "Shares");
                                        UserTools.addFolder(linkedServer, toUser, "/Shares/", user_name);
                                        moreItems = new Properties();
                                        moreItems.put("expires_on", UserTools.expire_vfs.format(ex1.parse(request.getProperty("expire").replace('+', ' '))));
                                        moreItems.put("created_on", UserTools.expire_vfs.format(new Date()));
                                        moreItems.put("share_comments", request.getProperty("share_comments", ""));
                                        if (!vrl.getProtocol().equalsIgnoreCase("FILE") && user != null && thisSession != null) {
                                            vItem = new Properties();
                                            vfs = UserTools.ut.getVFS(linkedServer, user_name);
                                            UserTools.setupVFSLinking(linkedServer, user_name, vfs, user);
                                            try {
                                                path = Common.replace_str(String.valueOf(SessionCrush.getRootDir(null, vfs, user, false)) + paths[x], "//", "/");
                                                dir_item = vfs.get_item(path);
                                                if (dir_item != null && dir_item.containsKey("vItem")) {
                                                    enumeration = ((Properties)dir_item.get("vItem")).propertyNames();
                                                    while (enumeration.hasMoreElements()) {
                                                        key = (String)enumeration.nextElement();
                                                        if (vItem.containsKey(key) || key.equals("url") || key.equals("path") || key.equals("privs")) continue;
                                                        vItem.put(key, ((Properties)dir_item.get("vItem")).get(key));
                                                    }
                                                }
                                            }
                                            catch (Exception e) {
                                                Log.log("HTTP_SERVER", 2, e);
                                            }
                                            if (vItem.size() > 0) {
                                                moreItems.putAll((Map<?, ?>)vItem);
                                            }
                                        }
                                        moreItems.put("expires_on", UserTools.expire_vfs.format(ex1.parse(request.getProperty("expire").replace('+', ' '))));
                                        moreItems.put("created_on", UserTools.expire_vfs.format(new Date()));
                                        moreItems.put("share_comments", request.getProperty("share_comments", ""));
                                        if (stat.getProperty("type", "FILE").equalsIgnoreCase("DIR") && !stat.getProperty("url", "").endsWith("/")) {
                                            stat.put("url", String.valueOf(stat.getProperty("url", "")) + "/");
                                        }
                                        UserTools.addItem(linkedServer, toUser, "/Shares/" + user_name + "/", stat.getProperty("name"), stat.getProperty("url"), stat.getProperty("type", "FILE"), moreItems, true, "");
                                        real_to_user = UserTools.ut.getUser(linkedServer, shareUsernames[xx].trim(), false);
                                        if (real_to_user != null && !real_to_user.getProperty("email", "").equals("")) {
                                            share_to_emails = String.valueOf(share_to_emails) + "," + real_to_user.getProperty("email", "");
                                        } else if (shareUsernames[xx].trim().indexOf("@") >= 0) {
                                            share_to_emails = String.valueOf(share_to_emails) + "," + shareUsernames[xx].trim();
                                        } else {
                                            user_email = user.getProperty("email", "");
                                            if (!thisSession.user.getProperty("ldap_mail", "").equals("")) {
                                                pp = new Properties();
                                                u = (Properties)thisSession.user.clone();
                                                pp.put("user", u);
                                                pp.put("username", shareUsernames[xx].trim());
                                                pp.put("password", "");
                                                pp.put("anyPass", "true");
                                                pp.put("publickey_lookup", "true");
                                                pp = thisSession.runPlugin("login", pp);
                                                user_email = u.getProperty("ldap_mail", "");
                                                if (!user_email.equals("")) {
                                                    share_to_emails = String.valueOf(share_to_emails) + "," + user_email;
                                                }
                                            } else {
                                                if (user_email.equals("")) {
                                                    user_email = user.getProperty("ldap_mail", "");
                                                }
                                                if (!user_email.equals("")) {
                                                    share_to_emails = String.valueOf(share_to_emails) + "," + shareUsernames[xx].trim() + user_email.substring(user_email.indexOf("@"));
                                                }
                                            }
                                        }
                                        UserTools.ut.forceMemoryReload(shareUsernames[xx].trim());
                                    }
                                }
                                ++xx;
                            }
                            if (stop_share) break;
                            if (share_to_emails.length() > 1) {
                                share_to_emails = share_to_emails.substring(1);
                            }
                            request.put("emailTo", share_to_emails);
                            lastStat = stat;
                        }
                        request.put("publishType", "directShare");
                    }
                    if (stop_share) break;
                    uid = "";
                    same_count = 0;
                    if (!preserve_relative_path) {
                        xx = 0;
                        while (xx < path_items.size() && msg.equals("")) {
                            if (x != xx) {
                                stat2 = (Properties)path_items.elementAt(xx);
                                if (stat.getProperty("name").equalsIgnoreCase(stat2.getProperty("name"))) {
                                    ++same_count;
                                }
                            }
                            ++xx;
                        }
                        if (same_count > 0) {
                            uid = "_" + Common.makeBoundary(4);
                            if (vrl.getName().indexOf(".") >= 0) {
                                uid = String.valueOf(uid) + vrl.getName().substring(vrl.getName().lastIndexOf("."));
                            }
                        }
                    }
                    if (user != null) {
                        keys = user.keys();
                        while (keys.hasMoreElements()) {
                            key = keys.nextElement().toString();
                            if (!key.startsWith("ldap_")) continue;
                            request.put(key, user.get(key));
                        }
                        if (ServerStatus.BG("temp_account_share_web_javascript") && !user.getProperty("javascript", "").equals("")) {
                            request.put("javascript", user.getProperty("javascript", ""));
                        }
                        if (ServerStatus.BG("temp_account_share_web_css") && !user.getProperty("css", "").equals("")) {
                            request.put("css", user.getProperty("css", ""));
                        }
                        if (ServerStatus.BG("temp_account_share_web_customizations") && user.get("web_customizations") != null) {
                            request.put("web_customizations", user.get("web_customizations"));
                        }
                        if (ServerStatus.BG("temp_account_share_web_buttons") && user.get("web_buttons") != null) {
                            request.put("web_buttons", user.get("web_buttons"));
                        }
                        if (ServerStatus.BG("temp_account_share_web_forms") && user.get("uploadForm") != null) {
                            request.put("uploadForm", user.get("uploadForm"));
                        }
                        if (ServerStatus.BG("temp_account_share_web_forms") && user.get("messageForm") != null) {
                            request.put("messageForm", user.get("messageForm"));
                        }
                        if (ServerStatus.BG("temp_account_pgp_settings") && user.get("fileDecryptionKey") != null && user.get("filePublicEncryptionKey") != null) {
                            if (user.get("fileDecryptionKey") != null) {
                                request.put("fileDecryptionKey", user.getProperty("fileDecryptionKey", ""));
                            }
                            if (user.get("fileDecryptionKeyPass") != null) {
                                request.put("fileDecryptionKeyPass", user.getProperty("fileDecryptionKeyPass", ""));
                            }
                            if (user.get("filePublicEncryptionKey") != null) {
                                request.put("filePublicEncryptionKey", user.getProperty("filePublicEncryptionKey", ""));
                            }
                            if (user.get("encryption_cypher") != null) {
                                request.put("encryption_cypher", user.getProperty("encryption_cypher", ""));
                            }
                        }
                        if ((events = (Vector)user.get("events")) != null) {
                            events2 = new Vector<Properties>();
                            xx = 0;
                            while (xx < events.size()) {
                                event = (Properties)events.elementAt(xx);
                                event_user_action_list = String.valueOf(event.getProperty("event_user_action_list", "")) + "(";
                                if (event_user_action_list.indexOf("(share_") >= 0) {
                                    event = (Properties)event.clone();
                                    event.put("linkUser", user.getProperty("username"));
                                    event.put("linkEvent", event.getProperty("name"));
                                    event.put("resolveShareEvent", "true");
                                    events2.addElement(event);
                                }
                                ++xx;
                            }
                            if (events2.size() > 0) {
                                request.put("events", events2);
                            }
                        }
                    }
                    if (request.getProperty("allowUploads", "false").equals("true")) {
                        custom_privs = request.getProperty("shareUsernamePermissions", "").toLowerCase();
                        if (custom_privs.equals("")) {
                            custom_privs = flash_shareAllowUploadsPrivs;
                        }
                        if (!custom_privs.equals("")) {
                            sPrivs1 = custom_privs;
                            sPrivs2 = "";
                            priv_parts = stat.getProperty("privs").toLowerCase().split("\\(");
                            loop = 0;
                            while (loop < priv_parts.length) {
                                priv = priv_parts[loop];
                                if (!(priv = priv.trim()).equals("")) {
                                    if (sPrivs1.indexOf("(" + (priv = priv.substring(0, priv.lastIndexOf(")"))) + ")") >= 0) {
                                        sPrivs2 = String.valueOf(sPrivs2) + "(" + priv + ")";
                                    } else if (priv.startsWith("quota") && sPrivs1.indexOf("(quota") >= 0) {
                                        sPrivs2 = String.valueOf(sPrivs2) + "(" + priv + ")";
                                    }
                                }
                                ++loop;
                            }
                            flash_shareAllowUploadsPrivs = sPrivs2;
                        }
                    }
                    if (request.getProperty("publishType").equalsIgnoreCase("copy")) {
                        if (privs.indexOf("(read)") >= 0) {
                            new File_U(String.valueOf(userHome) + "VFS/").mkdirs();
                            new File_U(userStorage).mkdirs();
                            itemName = vrl.getName();
                            if (itemName.endsWith("/")) {
                                itemName = itemName.substring(0, itemName.length() - 1);
                            }
                            if (request.getProperty("allowUploads", "false").equals("true")) {
                                if (privs.contains("(quota")) {
                                    flash_shareAllowUploadsPrivs = String.valueOf(flash_shareAllowUploadsPrivs) + privs.substring(privs.indexOf("(quota"), privs.indexOf(")", privs.indexOf("(quota") + "(quota".length()) + 1);
                                }
                                if (privs.contains("(real_quota)")) {
                                    flash_shareAllowUploadsPrivs = String.valueOf(flash_shareAllowUploadsPrivs) + "(real_quota)";
                                }
                                permissions.put("/" + itemName.toUpperCase() + "/", String.valueOf(flash_shareAllowUploadsPrivs) + pgp_addon);
                            }
                            try {
                                Common.writeXMLObject_U(String.valueOf(userHome) + "VFS.XML", permissions, "VFS");
                            }
                            catch (Exception sPrivs1) {
                                // empty catch block
                            }
                            try {
                                Common.writeXMLObject_U(String.valueOf(userHome) + "INFO.XML", request, "INFO");
                            }
                            catch (Exception sPrivs1) {
                                // empty catch block
                            }
                            if (vrl.getProtocol().equalsIgnoreCase("FILE")) {
                                Common.recurseCopyThreaded_U(vrl.getPath(), String.valueOf(userStorage) + vrl.getName() + (stat.getProperty("type", "FILE").equalsIgnoreCase("DIR") != false ? "/" : ""), true, false);
                            } else {
                                c1 = thisSession.uVFS.getClient(stat);
                                c1.login(vrl.getUsername(), vrl.getPassword(), null);
                                vrl_dest = new VRL(String.valueOf(new File_U(String.valueOf(userStorage) + vrl.getName()).toURI().toString()) + (stat.getProperty("type", "FILE").equalsIgnoreCase("DIR") != false ? "/" : ""));
                                Common.recurseCopy(vrl, vrl_dest, c1, null, 0, true, new StringBuffer());
                                c1 = thisSession.uVFS.releaseClient(c1);
                            }
                            if (request.getProperty("attach_real", "").equalsIgnoreCase("true")) {
                                if (vrl.getProtocol().toUpperCase().equals("FILE")) {
                                    files.addElement(new File_U(vrl.getPath()));
                                } else if (stat.getProperty("type", "FILE").equalsIgnoreCase("FILE") && Long.parseLong(stat.getProperty("size", "0")) > 0L) {
                                    if (Long.parseLong(stat.getProperty("size", "0")) < (long)(0x100000 * ServerStatus.IG("share_attached_file_size_limit"))) {
                                        p = new Properties();
                                        p.put("vrl", vrl);
                                        settings = new Properties();
                                        settings.putAll((Map<?, ?>)stat);
                                        settings.remove("url");
                                        p.put("prefs", settings);
                                        remote_files.add(p);
                                    }
                                }
                            }
                            vItem = new Properties();
                            url = new File_U(String.valueOf(userStorage) + vrl.getName()).toURI().toURL().toExternalForm();
                            if (stat.getProperty("type", "FILE").equalsIgnoreCase("DIR") && !url.endsWith("/")) {
                                url = String.valueOf(url) + "/";
                            }
                            vItem.put("url", url);
                            vItem.put("type", stat.getProperty("type", "FILE").toLowerCase());
                            v = new Vector<Properties>();
                            v.addElement(vItem);
                            try {
                                Common.writeXMLObject_U(String.valueOf(userHome) + "VFS/" + itemName + uid, v, "VFS");
                            }
                            catch (Exception loop) {}
                        } else {
                            msg = String.valueOf(msg) + "Not allowed to read from this location.\r\n<br/>";
                            stop_share = true;
                        }
                    } else if (request.getProperty("publishType").equalsIgnoreCase("move")) {
                        if (privs.indexOf("(read)") >= 0 && privs.indexOf("(delete)") >= 0) {
                            new File_U(String.valueOf(userHome) + "VFS/").mkdirs();
                            new File_U(userStorage).mkdirs();
                            itemName = "storage";
                            if (paths.length > 1 || stat.getProperty("type", "FILE").equalsIgnoreCase("FILE")) {
                                itemName = vrl.getName();
                            }
                            if (itemName.endsWith("/")) {
                                itemName = itemName.substring(0, itemName.length() - 1);
                            }
                            if (request.getProperty("allowUploads", "false").equals("true")) {
                                permissions.put("/" + itemName.toUpperCase() + "/", String.valueOf(flash_shareAllowUploadsPrivs) + pgp_addon);
                            }
                            try {
                                Common.writeXMLObject_U(String.valueOf(userHome) + "VFS.XML", permissions, "VFS");
                            }
                            catch (Exception vItem) {
                                // empty catch block
                            }
                            try {
                                Common.writeXMLObject_U(String.valueOf(userHome) + "INFO.XML", request, "INFO");
                            }
                            catch (Exception vItem) {
                                // empty catch block
                            }
                            Common.recurseCopyThreaded_U(vrl.getPath(), String.valueOf(userStorage) + vrl.getName(), true, true);
                            if (request.getProperty("attach_real", "").equalsIgnoreCase("true")) {
                                files.addElement(new File_U(String.valueOf(userStorage) + vrl.getName()));
                            }
                            vItem = new Properties();
                            vItem.put("url", new File_U(String.valueOf(userStorage) + vrl.getName()).toURI().toURL().toExternalForm());
                            vItem.put("type", stat.getProperty("type", "FILE").toLowerCase());
                            v = new Vector<Properties>();
                            v.addElement(vItem);
                            try {
                                Common.writeXMLObject_U(String.valueOf(userHome) + "VFS/" + itemName + uid, v, "VFS");
                            }
                            catch (Exception v) {}
                        } else {
                            msg = String.valueOf(msg) + "Not allowed to read and delete from this location.\r\n<br/>";
                            stop_share = true;
                        }
                    } else if (request.getProperty("publishType").equalsIgnoreCase("reference")) {
                        if (user != null && privs.contains("(quota") && privs.contains("(real_quota)") && (vfs = UserTools.ut.getVFS(linkedServer, user_name)) != null) {
                            test = vfs.getPrivPath(String.valueOf(stat.getProperty("root_dir", "/")) + stat.getProperty("name", "/"));
                            parent_quota_item = vfs.get_item_parent(test);
                            request.put("parent_quota_dir", parent_quota_item.getProperty("url", ""));
                        }
                        if (privs.indexOf("(read)") >= 0) {
                            new File_U(String.valueOf(userHome) + "VFS/").mkdirs();
                            new File_U(userStorage).mkdirs();
                            itemName = vrl.getName();
                            if (itemName.endsWith("/")) {
                                itemName = itemName.substring(0, itemName.length() - 1);
                            }
                            privs2 = "";
                            if (request.getProperty("allowUploads", "false").equals("true")) {
                                if (privs.contains("(quota")) {
                                    flash_shareAllowUploadsPrivs = String.valueOf(flash_shareAllowUploadsPrivs) + "(quota)";
                                }
                                if (privs.contains("(real_quota)")) {
                                    flash_shareAllowUploadsPrivs = String.valueOf(flash_shareAllowUploadsPrivs) + "(real_quota)";
                                }
                                privs3 = flash_shareAllowUploadsPrivs;
                                privs3 = Common.replace_str(privs3, "(", "");
                                if ((privs3 = Common.replace_str(privs3, ")", ",")).endsWith(",")) {
                                    privs3 = privs3.substring(0, privs3.length() - 1);
                                }
                                xx = 0;
                                while (xx < privs3.split(",").length) {
                                    s = "(" + privs3.split(",")[xx] + ")";
                                    if (privs.indexOf(s) >= 0) {
                                        privs2 = String.valueOf(privs2) + s;
                                    } else if (s.startsWith("(quota") && privs.indexOf("(quota") >= 0) {
                                        privs2 = String.valueOf(privs2) + s.substring(0, s.length() - 1) + privs.substring(privs.indexOf("(quota") + "(quota".length(), privs.indexOf(")", privs.indexOf("(quota") + "(quota".length()) + 1);
                                    }
                                    ++xx;
                                }
                                if (preserve_relative_path) {
                                    permissions.put(paths[x].toUpperCase(), String.valueOf(privs2) + pgp_addon);
                                } else {
                                    permissions.put("/" + itemName.toUpperCase() + "/", String.valueOf(privs2) + pgp_addon);
                                }
                            }
                            try {
                                Common.writeXMLObject_U(String.valueOf(userHome) + "VFS.XML", permissions, "VFS");
                            }
                            catch (Exception privs3) {
                                // empty catch block
                            }
                            try {
                                Common.writeXMLObject_U(String.valueOf(userHome) + "INFO.XML", request, "INFO");
                            }
                            catch (Exception privs3) {
                                // empty catch block
                            }
                            if (request.getProperty("attach_real", "").equalsIgnoreCase("true")) {
                                if (vrl.getProtocol().toUpperCase().equals("FILE")) {
                                    files.addElement(new File_U(vrl.getPath()));
                                } else if (stat.getProperty("type", "FILE").equalsIgnoreCase("FILE") && Long.parseLong(stat.getProperty("size", "0")) > 0L) {
                                    if (Long.parseLong(stat.getProperty("size", "0")) < (long)(0x100000 * ServerStatus.IG("share_attached_file_size_limit"))) {
                                        p = new Properties();
                                        p.put("vrl", vrl);
                                        settings = new Properties();
                                        settings.putAll((Map<?, ?>)stat);
                                        settings.remove("url");
                                        p.put("prefs", settings);
                                        remote_files.add(p);
                                    }
                                }
                            }
                            vItem = new Properties();
                            if (!vrl.getProtocol().equalsIgnoreCase("FILE")) {
                                url = vrl.getOriginalUrl();
                                if (stat.get("type").equals("DIR") && !url.endsWith("/")) {
                                    url = String.valueOf(url) + "/";
                                }
                                vItem.put("url", ServerStatus.thisObj.common_code.encode_pass(url, "DES", ""));
                                vItem.put("encrypted", "true");
                            } else {
                                vItem.put("url", Common.url_decode(vrl.getOriginalUrl()));
                                if (stat.get("type").equals("DIR") && !vItem.getProperty("url").endsWith("/")) {
                                    vItem.put("url", String.valueOf(vrl.getOriginalUrl()) + "/");
                                }
                                vItem.put("encrypted", "false");
                            }
                            vItem.put("type", stat.get("type"));
                            if (user != null) {
                                vfs = UserTools.ut.getVFS(linkedServer, user_name);
                                UserTools.setupVFSLinking(linkedServer, user_name, vfs, user);
                                p = new Properties();
                                try {
                                    p = vfs.get_item_parent(paths[x]);
                                    if (p != null) {
                                        enumeration = p.propertyNames();
                                        while (enumeration.hasMoreElements()) {
                                            key = (String)enumeration.nextElement();
                                            if (key.equals("url") || key.equals("type") || key.equals("vItem")) continue;
                                            vItem.put(key, p.get(key));
                                        }
                                    }
                                    if (thisSession != null && (dir_item = vfs.get_item(path = Common.replace_str(String.valueOf(SessionCrush.getRootDir(null, vfs, user, false)) + paths[x], "//", "/"))) != null && dir_item.containsKey("vItem")) {
                                        enumeration = ((Properties)dir_item.get("vItem")).propertyNames();
                                        while (enumeration.hasMoreElements()) {
                                            key = (String)enumeration.nextElement();
                                            if (vItem.containsKey(key)) continue;
                                            vItem.put(key, ((Properties)dir_item.get("vItem")).get(key));
                                        }
                                    }
                                }
                                catch (Exception e) {
                                    Log.log("HTTP_SERVER", 2, e);
                                }
                            }
                            v = new Vector<Properties>();
                            v.addElement(vItem);
                            try {
                                relative_path = "";
                                if (preserve_relative_path) {
                                    new File_U(String.valueOf(userHome) + "VFS/" + Common.all_but_last(paths[x]).substring(1)).mkdirs();
                                    relative_path = Common.all_but_last(paths[x]).substring(1);
                                }
                                Common.writeXMLObject_U(String.valueOf(userHome) + "VFS/" + relative_path + itemName + uid, v, "VFS");
                            }
                            catch (Exception relative_path) {
                                // empty catch block
                            }
                            if (ServerStatus.BG("replicate_shares")) {
                                p = new Properties();
                                p.put("userHome", userHome);
                                p.put("userStorage", userStorage);
                                p.put("permissions", permissions);
                                p.put("request", request);
                                p.put("itemName", itemName);
                                p.put("uid", uid);
                                p.put("v", v);
                                SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.share.create", "info", p);
                            }
                        } else {
                            msg = String.valueOf(msg) + "Not allowed to read from this location.\r\n<br/>";
                            stop_share = true;
                        }
                    }
                    if (stop_share) break;
                    lastStat = stat;
                    total_size += Long.parseLong(stat.getProperty("size", "0"));
                    stat.put("temp_home", new VRL(String.valueOf(new File_U(userHome).getCanonicalPath().replace('\\', '/')) + "/").toString());
                    stat.put("web_link", webLink);
                    stat.put("web_link_end", webLinkEnd);
                    stat.put("temp_username", tempUsername);
                    stat.put("temp_password", tempPassword);
                    stat.put("expire_date", expire_date);
                    emailTo_tmp = Common.url_decode(request.getProperty("emailTo", "").replace('+', ' ').trim()).replace(';', ',');
                    emailFrom_tmp = Common.url_decode(request.getProperty("emailFrom", "").replace('+', ' ').trim());
                    emailTo_tmp = Common.replace_str(emailTo_tmp, "{from}", emailFrom_tmp);
                    stat.put("emailTo", emailTo_tmp);
                    stat.put("shareUsernames", request.getProperty("shareUsernames", ""));
                    stat.put("shareUsername", request.getProperty("shareUsername", ""));
                    stat.put("the_file_name", vrl.getName());
                    eventItem = null;
                    if (thisSession != null) {
                        eventItem = TaskBridge.doEventItem(stat, "SHARE", thisSession.uiSG("user_ip"), thisSession.uiSG("sessionID"), thisSession.user_info.getProperty("SESSION_RID"));
                        thisSession.do_event5("SHARE", eventItem);
                        metaInfo = new Properties();
                        metaInfo.put("username", request.getProperty("username", ""));
                        metaInfo.put("password", request.getProperty("password", ""));
                        metaInfo.put("shareUsernamePermissions", request.getProperty("shareUsernamePermissions", ""));
                        metaInfo.put("sendEmail", request.getProperty("sendEmail", ""));
                        metaInfo.put("emailSubject", Common.url_decode(request.getProperty("emailSubject", "")));
                        if (!request.getProperty("emailTo", "").equals("")) {
                            metaInfo.put("emailTo", request.getProperty("emailTo", ""));
                        }
                        metaInfo.put("web_link", request.getProperty("web_link", ""));
                        metaInfo.put("web_link_end", request.getProperty("web_link_end", ""));
                        metaInfo.put("publishType", request.getProperty("publishType", ""));
                        metaInfo.put("allowUploads", request.getProperty("allowUploads", ""));
                        if (!request.getProperty("emailFrom", "").equals("")) {
                            metaInfo.put("emailFrom", request.getProperty("emailFrom", ""));
                        }
                        if (!request.getProperty("emailCc", "").equals("")) {
                            metaInfo.put("emailCc", request.getProperty("emailCc", ""));
                        }
                        metaInfo.put("expire", request.getProperty("expire", ""));
                        metaInfo.put("attach", request.getProperty("attach", ""));
                        metaInfo.put("share_comments", request.getProperty("share_comments", ""));
                        if (!request.getProperty("shareUsernames", "").equals("")) {
                            metaInfo.put("shareUsernames", request.getProperty("shareUsernames", ""));
                        }
                        if (!request.getProperty("shareUsername", "").equals("")) {
                            metaInfo.put("shareUsername", request.getProperty("shareUsername", ""));
                        }
                        ServerStatus.thisObj.statTools.insertMetaInfo(thisSession.uiSG("SESSION_RID"), metaInfo, eventItem.getProperty("transfer_rid"));
                    }
                    if (request.getProperty("attach", "").equalsIgnoreCase("true")) {
                        try {
                            newPath = SearchHandler.getPreviewPath(stat.getProperty("url"), "2", 1);
                            f = new File_U(String.valueOf(ServerStatus.SG("previews_path")) + newPath.substring(1));
                            if (f.exists()) {
                                files.addElement(f);
                            }
                        }
                        catch (Exception e) {
                            Log.log("HTTP_SERVER", 2, e);
                        }
                    }
                    ++x;
                }
                response = String.valueOf(response) + "<commandResult><response>";
                if (stop_share) break block282;
                Log.log("HTTP_SERVER", 0, "Share comments: " + request.getProperty("share_comments", ""));
                x = files.size() - 1;
                while (x >= 0) {
                    block284: {
                        block283: {
                            f = (File_U)files.elementAt(x);
                            if (f.isDirectory()) break block283;
                            if (f.length() <= (long)(0x100000 * ServerStatus.IG("share_attached_file_size_limit"))) break block284;
                        }
                        files.removeElementAt(x);
                    }
                    --x;
                }
                files2 = null;
                if (files.size() > 0) {
                    files2 = new File_B[files.size()];
                }
                x = 0;
                while (x < files.size()) {
                    files2[x] = new File_B((File_U)files.elementAt(x));
                    ++x;
                }
                emailFrom = Common.url_decode(request.getProperty("emailFrom", "").replace('+', ' ').trim());
                if (hideShareFromRow && default_email_from.equals("")) {
                    emailFrom = ServerStatus.SG("smtp_from");
                }
                emailReplyTo = Common.url_decode(request.getProperty("emailReplyTo", "").replace('+', ' ').trim());
                emailTo = Common.url_decode(request.getProperty("emailTo", "").replace('+', ' ').trim()).replace(';', ',');
                emailCc = Common.url_decode(request.getProperty("emailCc", "").replace('+', ' ').trim()).replace(';', ',');
                emailBcc = Common.url_decode(request.getProperty("emailBcc", "").replace('+', ' ').trim()).replace(';', ',');
                emailTo = Common.replace_str(emailTo, "{from}", emailFrom);
                emailCc = Common.replace_str(emailCc, "{from}", emailFrom);
                emailBcc = Common.replace_str(emailBcc, "{from}", emailFrom);
                emailReplyTo = Common.replace_str(emailReplyTo, "{from}", emailFrom);
                emailFrom = Common.replace_str(emailFrom, "{master}", request.getProperty("master"));
                emailTo = Common.replace_str(emailTo, "{master}", request.getProperty("master"));
                emailCc = Common.replace_str(emailCc, "{master}", request.getProperty("master"));
                emailBcc = Common.replace_str(emailBcc, "{master}", request.getProperty("master"));
                emailReplyTo = Common.replace_str(emailReplyTo, "{master}", request.getProperty("master"));
                emailSubject = Common.url_decode(request.getProperty("emailSubject", "").replace('+', ' ').trim());
                emailBody = String.valueOf(Common.url_decode(request.getProperty("emailBody", ""))) + "\r\n\r\n";
                emailBody = Common.replace_str(emailBody, "&lt;", "<");
                if ((emailBody = Common.replace_str(emailBody, "&gt;", ">")).indexOf("<") < 0) {
                    emailBody = emailBody.replace('+', ' ').trim();
                }
                if (shareBodyEmailClient.equals("")) {
                    shareBodyEmailClient = emailBody;
                }
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "&lt;", "<");
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "&gt;", ">");
                sdf_time = new SimpleDateFormat("HH:mm", Locale.US);
                d = ex2.parse(expire_date);
                loginCount = request.getProperty("logins", "");
                if (loginCount.trim().equals("")) {
                    loginCount = "unlimited";
                }
                emailBody = Common.replace_str(emailBody, "<LINE>", "{line_start}");
                emailBody = Common.replace_str(emailBody, "<line>", "{line_start}");
                emailBody = Common.replace_str(emailBody, "</LINE>", "{line_end}");
                if ((emailBody = Common.replace_str(emailBody, "</line>", "{line_end}")).indexOf("{line_start}") >= 0) {
                    while (emailBody.indexOf("{line_start}") >= 0 && emailBody.indexOf("{line_end}") >= 0) {
                        line = emailBody.substring(emailBody.indexOf("{line_start}") + "{line_start}".length(), emailBody.indexOf("{line_end}"));
                        lines = "";
                        xx = 0;
                        while (xx < paths.length) {
                            line2 = line;
                            if (!paths[xx].trim().equals("")) {
                                line2 = Common.replace_str(line2, "{web_link}", webLink);
                                line2 = Common.replace_str(line2, "{web_link_end}", webLinkEnd);
                                line2 = Common.replace_str(line2, "{username}", tempUsername);
                                line2 = Common.replace_str(line2, "{password}", tempPassword);
                                line2 = Common.replace_str(line2, "{user}", tempUsername);
                                line2 = Common.replace_str(line2, "{pass}", tempPassword);
                                line2 = Common.replace_str(line2, "{url}", Common.url_decode(request.getProperty("baseUrl")));
                                line2 = Common.replace_str(line2, "{to}", emailTo);
                                line2 = Common.replace_str(line2, "{to_all}", request.getProperty("to_all", ""));
                                line2 = Common.replace_str(line2, "{from}", emailFrom);
                                line2 = Common.replace_str(line2, "{reply_to}", emailReplyTo);
                                line2 = Common.replace_str(line2, "{cc}", emailCc);
                                line2 = Common.replace_str(line2, "{bcc}", emailBcc);
                                line2 = Common.replace_str(line2, "{subject}", emailSubject);
                                line2 = Common.replace_str(line2, "{master}", request.getProperty("master"));
                                line2 = Common.replace_str(line2, "{paths}", Common.url_decode(request.getProperty("paths")));
                                line2 = Common.replace_str(line2, "{path}", Common.all_but_last(paths[xx].trim()));
                                line2 = Common.replace_str(line2, "{name}", Common.url_decode(Common.last(paths[xx].trim())));
                                line2 = Common.replace_str(line2, "{datetime}", ex1.format(d).trim());
                                line2 = Common.replace_str(line2, "{date}", date_time.format(d).trim());
                                line2 = Common.replace_str(line2, "{time}", sdf_time.format(d).trim());
                                line2 = Common.replace_str(line2, "{comments}", request.getProperty("share_comments", ""));
                                if (lastStat != null) {
                                    line2 = Common.replace_str(line2, "{size}", com.crushftp.client.Common.format_bytes_short2(Long.parseLong(lastStat.getProperty("size", "0"))));
                                }
                                line2 = Common.replace_str(line2, "{total_size}", com.crushftp.client.Common.format_bytes_short2(total_size));
                                line2 = Common.replace_str(line2, "{logins}", loginCount);
                                x = 0;
                                while (x < 100) {
                                    s = "";
                                    if (paths[xx].split("/").length > x) {
                                        s = paths[xx].split("/")[x];
                                    }
                                    line2 = Common.replace_str(line2, "{" + x + "path}", s);
                                    ++x;
                                }
                                x = 0;
                                while (x < 100) {
                                    s = "";
                                    i = paths[xx].split("/").length - 1 - x;
                                    if (i >= 0) {
                                        s = paths[xx].split("/")[i];
                                    }
                                    line2 = Common.replace_str(line2, "{path" + x + "}", s);
                                    ++x;
                                }
                                lines = String.valueOf(lines) + line2;
                            }
                            ++xx;
                        }
                        emailBody = Common.replace_str(emailBody, "{line_start}" + line + "{line_end}", lines);
                    }
                }
                x = 0;
                while (x < 100) {
                    s = "";
                    if (paths[0].split("/").length > x) {
                        s = paths[0].split("/")[x];
                    }
                    emailBody = Common.replace_str(emailBody, "{" + x + "path}", s);
                    ++x;
                }
                x = 0;
                while (x < 100) {
                    s = "";
                    i = paths[0].split("/").length - 1 - x;
                    if (i >= 0) {
                        s = paths[0].split("/")[i];
                    }
                    emailBody = Common.replace_str(emailBody, "{path" + x + "}", s);
                    ++x;
                }
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "<LINE>", "{line_start}");
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "<line>", "{line_start}");
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "</LINE>", "{line_end}");
                if ((shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "</line>", "{line_end}")).indexOf("{line_start}") >= 0) {
                    while (shareBodyEmailClient.indexOf("{line_start}") >= 0 && shareBodyEmailClient.indexOf("{line_end}") >= 0) {
                        line = shareBodyEmailClient.substring(shareBodyEmailClient.indexOf("{line_start}") + "{line_start}".length(), shareBodyEmailClient.indexOf("{line_end}"));
                        lines = "";
                        xx = 0;
                        while (xx < paths.length) {
                            line2 = line;
                            if (!paths[xx].trim().equals("")) {
                                line2 = Common.replace_str(line2, "{web_link}", webLink);
                                line2 = Common.replace_str(line2, "{web_link_end}", webLinkEnd);
                                line2 = Common.replace_str(line2, "{username}", tempUsername);
                                line2 = Common.replace_str(line2, "{password}", tempPassword);
                                line2 = Common.replace_str(line2, "{user}", tempUsername);
                                line2 = Common.replace_str(line2, "{pass}", tempPassword);
                                line2 = Common.replace_str(line2, "{url}", Common.url_decode(request.getProperty("baseUrl")));
                                line2 = Common.replace_str(line2, "{to}", emailTo);
                                line2 = Common.replace_str(line2, "{to_all}", request.getProperty("to_all", ""));
                                line2 = Common.replace_str(line2, "{from}", emailFrom);
                                line2 = Common.replace_str(line2, "{reply_to}", emailReplyTo);
                                line2 = Common.replace_str(line2, "{cc}", emailCc);
                                line2 = Common.replace_str(line2, "{bcc}", emailBcc);
                                line2 = Common.replace_str(line2, "{subject}", emailSubject);
                                line2 = Common.replace_str(line2, "{master}", request.getProperty("master"));
                                line2 = Common.replace_str(line2, "{paths}", Common.url_decode(request.getProperty("paths")));
                                line2 = Common.replace_str(line2, "{path}", Common.all_but_last(paths[xx].trim()));
                                line2 = Common.replace_str(line2, "{name}", Common.url_decode(Common.last(paths[xx].trim())));
                                line2 = Common.replace_str(line2, "{datetime}", ex1.format(d).trim());
                                line2 = Common.replace_str(line2, "{date}", date_time.format(d).trim());
                                line2 = Common.replace_str(line2, "{time}", sdf_time.format(d).trim());
                                line2 = Common.replace_str(line2, "{comments}", request.getProperty("share_comments", ""));
                                if (lastStat != null) {
                                    line2 = Common.replace_str(line2, "{size}", com.crushftp.client.Common.format_bytes_short2(Long.parseLong(lastStat.getProperty("size", "0"))));
                                }
                                line2 = Common.replace_str(line2, "{total_size}", com.crushftp.client.Common.format_bytes_short2(total_size));
                                line2 = Common.replace_str(line2, "{logins}", loginCount);
                                x = 0;
                                while (x < 100) {
                                    s = "";
                                    if (paths[xx].split("/").length > x) {
                                        s = paths[xx].split("/")[x];
                                    }
                                    line2 = Common.replace_str(line2, "{" + x + "path}", s);
                                    ++x;
                                }
                                x = 0;
                                while (x < 100) {
                                    s = "";
                                    i = paths[xx].split("/").length - 1 - x;
                                    if (i >= 0) {
                                        s = paths[xx].split("/")[i];
                                    }
                                    line2 = Common.replace_str(line2, "{path" + x + "}", s);
                                    ++x;
                                }
                                lines = String.valueOf(lines) + line2;
                            }
                            ++xx;
                        }
                        shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{line_start}" + line + "{line_end}", lines);
                    }
                }
                x = 0;
                while (x < 100) {
                    s = "";
                    if (paths[0].split("/").length > x) {
                        s = paths[0].split("/")[x];
                    }
                    shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{" + x + "path}", s);
                    ++x;
                }
                x = 0;
                while (x < 100) {
                    s = "";
                    i = paths[0].split("/").length - 1 - x;
                    if (i >= 0) {
                        s = paths[0].split("/")[i];
                    }
                    shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{path" + x + "}", s);
                    ++x;
                }
                emailBody = Common.replace_str(emailBody, "<web_link>", webLink);
                emailBody = Common.replace_str(emailBody, "<web_link_end>", webLinkEnd);
                emailBody = Common.replace_str(emailBody, "<username>", tempUsername);
                emailBody = Common.replace_str(emailBody, "<password>", tempPassword);
                emailBody = Common.replace_str(emailBody, "%user%", tempUsername);
                emailBody = Common.replace_str(emailBody, "%pass%", tempPassword);
                emailBody = Common.replace_str(emailBody, "{user}", tempUsername);
                emailBody = Common.replace_str(emailBody, "{pass}", tempPassword);
                emailBody = Common.replace_str(emailBody, "<url>", Common.url_decode(request.getProperty("baseUrl")));
                emailBody = Common.replace_str(emailBody, "{web_link}", webLink);
                emailBody = Common.replace_str(emailBody, "{web_link_end}", webLinkEnd);
                emailBody = Common.replace_str(emailBody, "{username}", tempUsername);
                emailBody = Common.replace_str(emailBody, "{password}", tempPassword);
                emailBody = Common.replace_str(emailBody, "{url}", Common.url_decode(request.getProperty("baseUrl")));
                emailBody = Common.replace_str(emailBody, "{to}", emailTo);
                emailBody = Common.replace_str(emailBody, "{to_all}", request.getProperty("to_all", ""));
                emailBody = Common.replace_str(emailBody, "{from}", emailFrom);
                emailBody = Common.replace_str(emailBody, "{reply_to}", emailReplyTo);
                emailBody = Common.replace_str(emailBody, "{cc}", emailCc);
                emailBody = Common.replace_str(emailBody, "{bcc}", emailBcc);
                emailBody = Common.replace_str(emailBody, "{subject}", emailSubject);
                emailBody = Common.replace_str(emailBody, "{master}", request.getProperty("master"));
                emailBody = Common.replace_str(emailBody, "{paths}", Common.url_decode(request.getProperty("paths")));
                emailBody = Common.replace_str(emailBody, "{name}", Common.url_decode(last_name));
                emailBody = Common.replace_str(emailBody, "{comments}", Common.url_decode(request.getProperty("share_comments")));
                if (lastStat != null) {
                    emailBody = Common.replace_str(emailBody, "{size}", com.crushftp.client.Common.format_bytes_short2(Long.parseLong(lastStat.getProperty("size", "0"))));
                }
                emailBody = Common.replace_str(emailBody, "{total_size}", com.crushftp.client.Common.format_bytes_short2(total_size));
                emailBody = Common.replace_str(emailBody, "{logins}", loginCount);
                if (thisSession != null && thisSession.user != null) {
                    if (thisSession.user_info != null) {
                        ui_keys = thisSession.user_info.keys();
                        while (ui_keys.hasMoreElements()) {
                            key = ui_keys.nextElement().toString();
                            emailBody = Common.replace_str(emailBody, "{user_" + key + "}", thisSession.user_info.getProperty(key, ""));
                        }
                    }
                    keys = thisSession.user.keys();
                    while (keys.hasMoreElements()) {
                        key = keys.nextElement().toString();
                        emailBody = Common.replace_str(emailBody, "{user_" + key + "}", thisSession.user.getProperty(key, ""));
                    }
                }
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "<web_link>", webLink);
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "<web_link_end>", webLinkEnd);
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "<username>", tempUsername);
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "<password>", tempPassword);
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "%user%", tempUsername);
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "%pass%", tempPassword);
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{user}", tempUsername);
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{pass}", tempPassword);
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "<url>", Common.url_decode(request.getProperty("baseUrl")));
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{web_link}", webLink);
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{web_link_end}", webLinkEnd);
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{username}", tempUsername);
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{password}", tempPassword);
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{url}", Common.url_decode(request.getProperty("baseUrl")));
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{to}", emailTo);
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{to_all}", request.getProperty("to_all", ""));
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{from}", emailFrom);
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{reply_to}", emailReplyTo);
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{cc}", emailCc);
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{bcc}", emailBcc);
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{subject}", emailSubject);
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{master}", request.getProperty("master"));
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{paths}", Common.url_decode(request.getProperty("paths")));
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{name}", Common.url_decode(last_name));
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{comments}", Common.url_decode(request.getProperty("share_comments", "")));
                if (lastStat != null) {
                    shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{size}", com.crushftp.client.Common.format_bytes_short2(Long.parseLong(lastStat.getProperty("size", "0"))));
                }
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{total_size}", com.crushftp.client.Common.format_bytes_short2(total_size));
                shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{logins}", loginCount);
                x = 0;
                while (x < 100) {
                    s = "";
                    if (paths[0].split("/").length > x) {
                        s = paths[0].split("/")[x];
                    }
                    emailSubject = Common.replace_str(emailSubject, "{" + x + "path}", s);
                    ++x;
                }
                x = 0;
                while (x < 100) {
                    s = "";
                    i = paths[0].split("/").length - 1 - x;
                    if (i >= 0) {
                        s = paths[0].split("/")[i];
                    }
                    emailSubject = Common.replace_str(emailSubject, "{path" + x + "}", s);
                    ++x;
                }
                if (thisSession != null && thisSession.user != null) {
                    if (thisSession.user_info != null) {
                        ui_keys = thisSession.user_info.keys();
                        while (ui_keys.hasMoreElements()) {
                            key = ui_keys.nextElement().toString();
                            shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{user_" + key + "}", thisSession.user_info.getProperty(key, ""));
                        }
                    }
                    keys = thisSession.user.keys();
                    while (keys.hasMoreElements()) {
                        key = keys.nextElement().toString();
                        shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{user_" + key + "}", thisSession.user.getProperty(key, ""));
                    }
                }
                emailSubject = Common.replace_str(emailSubject, "{username}", tempUsername);
                emailSubject = Common.replace_str(emailSubject, "{password}", tempPassword);
                emailSubject = Common.replace_str(emailSubject, "{web_link}", webLink);
                emailSubject = Common.replace_str(emailSubject, "{web_link_end}", webLinkEnd);
                emailSubject = Common.replace_str(emailSubject, "{to}", emailTo);
                emailSubject = Common.replace_str(emailSubject, "{to_all}", request.getProperty("to_all", ""));
                emailSubject = Common.replace_str(emailSubject, "{from}", emailFrom);
                emailSubject = Common.replace_str(emailSubject, "{reply_to}", emailReplyTo);
                emailSubject = Common.replace_str(emailSubject, "{cc}", emailCc);
                emailSubject = Common.replace_str(emailSubject, "{bcc}", emailBcc);
                emailSubject = Common.replace_str(emailSubject, "{logins}", loginCount);
                emailSubject = Common.replace_str(emailSubject, "{master}", request.getProperty("master"));
                emailSubject = com.crushftp.client.Common.textFunctions(emailSubject, "{", "}");
                emailSubject = com.crushftp.client.Common.textFunctions(emailSubject, "[", "]");
                emailSubject = Common.replace_str(emailSubject, "{total_size}", com.crushftp.client.Common.format_bytes_short2(total_size));
                if (thisSession != null && thisSession.user != null) {
                    if (thisSession.user_info != null) {
                        ui_keys = thisSession.user_info.keys();
                        while (ui_keys.hasMoreElements()) {
                            key = ui_keys.nextElement().toString();
                            emailSubject = Common.replace_str(emailSubject, "{user_" + key + "}", thisSession.user_info.getProperty(key, ""));
                        }
                    }
                    keys = thisSession.user.keys();
                    while (keys.hasMoreElements()) {
                        key = keys.nextElement().toString();
                        emailSubject = Common.replace_str(emailSubject, "{user_" + key + "}", thisSession.user.getProperty(key, ""));
                    }
                }
                request.put("logins", loginCount);
                try {
                    if (user != null) {
                        date_time = SessionCrush.updateDateCustomizations(date_time, user);
                    }
                    emailBody = Common.replace_str(emailBody, "<datetime>", ex1.format(d).trim());
                    emailBody = Common.replace_str(emailBody, "<date>", date_time.format(d).trim());
                    emailBody = Common.replace_str(emailBody, "<time>", sdf_time.format(d).trim());
                    emailBody = Common.replace_str(emailBody, "{datetime}", ex1.format(d).trim());
                    emailBody = Common.replace_str(emailBody, "{date}", date_time.format(d).trim());
                    emailBody = Common.replace_str(emailBody, "{time}", sdf_time.format(d).trim());
                    emailBody = Common.replace_str(emailBody, "{logins}", loginCount);
                    emailBody = com.crushftp.client.Common.textFunctions(emailBody, "{", "}");
                    emailBody = com.crushftp.client.Common.textFunctions(emailBody, "[", "]");
                }
                catch (Exception e) {
                    Log.log("HTTP_SERVER", 1, e);
                }
                request.put("date", date_time.format(d).trim());
                request.put("datetime", ex1.format(d).trim());
                request.put("time", sdf_time.format(d).trim());
                try {
                    if (user != null) {
                        date_time = SessionCrush.updateDateCustomizations(date_time, user);
                    }
                    request.put("date", date_time.format(d).trim());
                    request.put("datetime", ex1.format(d).trim());
                    request.put("time", sdf_time.format(d).trim());
                    shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "<datetime>", ex1.format(d).trim());
                    shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "<date>", date_time.format(d).trim());
                    shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "<time>", sdf_time.format(d).trim());
                    shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{datetime}", ex1.format(d).trim());
                    shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{date}", date_time.format(d).trim());
                    shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{time}", sdf_time.format(d).trim());
                    shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{logins}", loginCount);
                    shareBodyEmailClient = Common.replace_str(shareBodyEmailClient, "{master}", request.getProperty("master"));
                    shareBodyEmailClient = com.crushftp.client.Common.textFunctions(shareBodyEmailClient, "{", "}");
                    shareBodyEmailClient = com.crushftp.client.Common.textFunctions(shareBodyEmailClient, "[", "]");
                }
                catch (Exception e) {
                    Log.log("HTTP_SERVER", 1, e);
                }
                v = new Variables();
                emailBody = v.replace_vars_line_date(emailBody, request, "{", "}");
                shareBodyEmailClient = v.replace_vars_line_date(shareBodyEmailClient, request, "{", "}");
                emailSubject = v.replace_vars_line_date(emailSubject, request, "{", "}");
                if (request.getProperty("sendEmail", "").equals("true")) {
                    result = "";
                    result = remote_files.size() > 0 ? com.crushftp.client.Common.send_mail(ServerStatus.SG("discovered_ip"), emailTo, emailCc, emailBcc, emailFrom, emailReplyTo, emailSubject, emailBody, ServerStatus.SG("smtp_server"), ServerStatus.SG("smtp_user"), ServerStatus.SG("smtp_pass"), ServerStatus.SG("smtp_ssl").equals("true"), ServerStatus.SG("smtp_html").equals("true"), files2, new Vector<E>(), remote_files) : com.crushftp.client.Common.send_mail(ServerStatus.SG("discovered_ip"), emailTo, emailCc, emailBcc, emailFrom, emailReplyTo, emailSubject, emailBody, ServerStatus.SG("smtp_server"), ServerStatus.SG("smtp_user"), ServerStatus.SG("smtp_pass"), ServerStatus.SG("smtp_ssl").equals("true"), ServerStatus.SG("smtp_html").equals("true"), files2);
                    msg = result.toUpperCase().indexOf("SUCCESS") < 0 ? "ERROR: {share_complete} {email_failed} " + msg : "{share_complete}  {share_email_sent}. &nbsp;&nbsp;&nbsp;" + msg;
                } else {
                    msg = "{share_complete} &nbsp;&nbsp;&nbsp;" + msg;
                }
                msg = String.valueOf(msg) + "<a href=\"mailto:" + emailTo + "?ignore=false";
                if (!emailCc.trim().equals("")) {
                    msg = String.valueOf(msg) + "&cc=" + emailCc;
                }
                if (!emailBcc.trim().equals("")) {
                    msg = String.valueOf(msg) + "&bcc=" + emailBcc;
                }
                if (!emailSubject.trim().equals("")) {
                    msg = String.valueOf(msg) + "&subject=" + Common.url_encode(emailSubject);
                }
                if (!shareBodyEmailClient.trim().equals("")) {
                    msg = String.valueOf(msg) + "&body=" + Common.url_encode(shareBodyEmailClient);
                }
                msg = String.valueOf(msg) + "\">{share_open_in_email_client}</a>";
                if (thisSession != null) {
                    thisSession.do_event5("BATCH_COMPLETE", null);
                }
                break block285;
            }
            if (request.getProperty("shareUsername", "false").equalsIgnoreCase("false")) {
                msg = "ERROR: " + msg;
            }
        }
        response = String.valueOf(response) + "<username>" + tempUsername + "</username>";
        response = String.valueOf(response) + "<password>" + tempPassword + "</password>";
        response = String.valueOf(response) + "<expire_date>" + expire_date + "</expire_date>";
        response = String.valueOf(response) + "<expire>" + request.getProperty("expire") + "</expire>";
        response = String.valueOf(response) + "<date>" + request.getProperty("date") + "</date>";
        response = String.valueOf(response) + "<datetime>" + request.getProperty("datetime") + "</datetime>";
        response = String.valueOf(response) + "<time>" + request.getProperty("time") + "</time>";
        response = String.valueOf(response) + "<logins>" + request.getProperty("logins") + "</logins>";
        response = String.valueOf(response) + "<message>" + Common.url_encode(msg) + "</message>";
        response = String.valueOf(response) + "<url>" + Common.url_encode(webLink) + "</url>";
        response = String.valueOf(response) + "<error_response></error_response>";
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static Properties doEventItem(Properties fileItem, SessionCrush theSession, String event_type) {
        if (theSession == null) {
            return null;
        }
        fileItem = TaskBridge.doEventItem(fileItem, event_type, theSession.uiSG("user_ip"), theSession.uiSG("sessionID"), theSession.user_info.getProperty("SESSION_RID"));
        theSession.do_event5(event_type, fileItem);
        return fileItem;
    }

    public static String createCustom(Vector path_items, Properties request, SessionCrush thisSession) throws Exception {
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
        if (request.getProperty("json", "false").equals("true")) {
            response = "";
        }
        Properties last_stat = new Properties();
        int x = 0;
        while (x < path_items.size()) {
            Properties stat = (Properties)path_items.elementAt(x);
            request.putAll((Map<?, ?>)stat);
            stat.putAll((Map<?, ?>)request);
            last_stat = ServerSessionAJAX.doEventItem(stat, thisSession, "CUSTOM");
            ++x;
        }
        if (thisSession != null) {
            thisSession.do_event5("BATCH_COMPLETE", null);
        }
        response = request.getProperty("json", "false").equals("true") ? "{\r\n\"success\": true\r\n}\r\n" : String.valueOf(response) + "<commandResult><response>SUCCESS</response><execute_log>" + Common.url_encode(last_stat.getProperty("execute_log", "")) + "</execute_log></commandResult>";
        return response;
    }

    public static String createProblem(Vector path_items, Properties request, SessionCrush thisSession) throws Exception {
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
        if (request.getProperty("json", "false").equals("true")) {
            response = "";
        }
        int x = 0;
        while (x < path_items.size()) {
            Properties stat = (Properties)path_items.elementAt(x);
            request.putAll((Map<?, ?>)stat);
            stat.putAll((Map<?, ?>)request);
            System.getProperties().put("crushftp.isTestCall" + Thread.currentThread().getId(), "true");
            try {
                String msg = "";
                try {
                    GenericClient c = thisSession.uVFS.getClient(stat);
                    try {
                        c.list(stat.getProperty("path"), new Vector());
                    }
                    finally {
                        thisSession.uVFS.releaseClient(c);
                    }
                    msg = "Success";
                }
                catch (Exception e) {
                    Log.log("SERVER", 0, e);
                    msg = "ERROR:" + e;
                }
                stat.put("test_results", msg);
            }
            finally {
                System.getProperties().remove("crushftp.isTestCall" + Thread.currentThread().getId());
            }
            ServerSessionAJAX.doEventItem(stat, thisSession, "PROBLEM");
            ++x;
        }
        if (thisSession != null) {
            thisSession.do_event5("BATCH_COMPLETE", null);
        }
        response = request.getProperty("json", "false").equals("true") ? "{\r\n\"success\": true\r\n}\r\n" : String.valueOf(response) + "<commandResult><response>SUCCESS</response></commandResult>";
        return response;
    }

    public static String getHistory(Properties request, SessionCrush thisSession) throws Exception {
        String the_dir = Common.url_decode(request.getProperty("path", ""));
        if ((the_dir = com.crushftp.client.Common.dots(the_dir)).equals("/")) {
            the_dir = thisSession.SG("root_dir");
        }
        if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(thisSession.SG("root_dir").toUpperCase())) {
            the_dir = String.valueOf(thisSession.SG("root_dir")) + the_dir.substring(1);
        }
        Properties item = thisSession.uVFS.get_item(the_dir);
        String info = "";
        if (item != null && item.getProperty("type", "").equalsIgnoreCase("FILE") && item.getProperty("privs", "").indexOf("(sync") >= 0) {
            thisSession.uiPUT("current_dir", the_dir);
            String path = the_dir;
            String revPath = Common.parseSyncPart(item.getProperty("privs", ""), "revisionsPath");
            FileClient fc = new FileClient("file:///", "", new Vector());
            fc.setConfig("zip_list", "false");
            Vector<Properties> listing = new Vector<Properties>();
            int rev = 0;
            while (rev < 100) {
                if (!revPath.equals("")) {
                    Properties info2;
                    VRL vrl;
                    if (path.startsWith(thisSession.SG("root_dir"))) {
                        path = path.substring(thisSession.SG("root_dir").length() - 1);
                    }
                    if (!new File_U((vrl = new VRL(String.valueOf(revPath) + path + "/" + rev + "/" + item.getProperty("name"))).getPath()).exists()) break;
                    Properties lp = fc.stat(vrl.getPath());
                    lp.remove("url");
                    lp.put("root_dir", item.getProperty("root_dir"));
                    lp.put("href_path", String.valueOf(lp.getProperty("root_dir")) + lp.getProperty("name"));
                    lp.put(String.valueOf(System.getProperty("appname", "CrushFTP").toLowerCase()) + "_rev", String.valueOf(rev));
                    File_U info_xml = new File_U(String.valueOf(revPath) + path + "/" + rev + "/info.XML");
                    if (info_xml.exists() && (info2 = (Properties)Common.readXMLObject_U(info_xml)) != null) {
                        lp.putAll((Map<?, ?>)info2);
                    }
                    listing.addElement(lp);
                }
                ++rev;
            }
            Properties listingProp = new Properties();
            listingProp.put("listing", listing);
            String altList = AgentUI.getJsonListObj(listingProp, true);
            info = String.valueOf(info) + "{\r\n";
            info = String.valueOf(info) + "\t\"listing\" : " + altList + "\r\n";
            info = String.valueOf(info) + "}\r\n";
        } else {
            info = String.valueOf(info) + "{\r\n";
            info = String.valueOf(info) + "\t\"listing\" : []\r\n";
            info = String.valueOf(info) + "}\r\n";
        }
        return info;
    }

    public static String manageShares(SessionCrush thisSession) throws Exception {
        Vector<Properties> listing;
        block31: {
            listing = new Vector<Properties>();
            try {
                String tempAccountsPath = ServerStatus.SG("temp_accounts_path");
                File_U[] accounts = (File_U[])new File_U(String.valueOf(tempAccountsPath) + "accounts/").listFiles();
                thisSession.date_time = SessionCrush.updateDateCustomizations(thisSession.date_time, thisSession.user);
                String user_name = thisSession.uiSG("user_name");
                if (ServerStatus.BG("secondary_login_via_email") && user_name.indexOf("@") >= 0 && UserTools.user_email_cache.containsKey(String.valueOf(thisSession.uiSG("listen_ip_port")) + ":" + user_name.toUpperCase())) {
                    user_name = UserTools.user_email_cache.getProperty(String.valueOf(thisSession.uiSG("listen_ip_port")) + ":" + user_name.toUpperCase());
                }
                if (accounts == null) break block31;
                int x = 0;
                while (x < accounts.length) {
                    block32: {
                        try {
                            File_U f = accounts[x];
                            if (f.getName().indexOf(",,") < 0 || !f.isDirectory()) break block32;
                            String[] tokens = f.getName().split(",,");
                            Properties pp = new Properties();
                            int xx = 0;
                            while (xx < tokens.length) {
                                String key = tokens[xx].substring(0, tokens[xx].indexOf("="));
                                String val = tokens[xx].substring(tokens[xx].indexOf("=") + 1);
                                pp.put(key.toUpperCase(), val);
                                ++xx;
                            }
                            if (!user_name.equalsIgnoreCase(pp.getProperty("M"))) break block32;
                            SimpleDateFormat sdf1 = new SimpleDateFormat("MMddyyyyHHmm", Locale.US);
                            SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
                            Properties info = (Properties)Common.readXMLObject_U(String.valueOf(f.getPath()) + "/INFO.XML");
                            info.putAll((Map<?, ?>)pp);
                            info.put("password", pp.getProperty("P"));
                            info.remove("type");
                            info.remove("master");
                            try {
                                info.put("expire", thisSession.date_time.format(sdf1.parse(info.getProperty("EX"))));
                                info.put("expireMillis", String.valueOf(sdf1.parse(info.getProperty("EX")).getTime()));
                            }
                            catch (Exception e) {
                                Log.log("HTTP_SERVER", 1, e);
                            }
                            try {
                                info.put("createdMillis", String.valueOf(sdf2.parse(info.getProperty("created")).getTime()));
                            }
                            catch (Exception e) {
                                Log.log("HTTP_SERVER", 1, e);
                            }
                            info.remove("EX");
                            info.remove("T");
                            info.remove("P");
                            info.remove("M");
                            info.remove("U");
                            info.put("downloads", "?");
                            info.put("login_allowance", pp.getProperty("I", "-1"));
                            try {
                                if (com.crushftp.client.Common.dmz_mode) {
                                    Vector queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                                    Properties action = new Properties();
                                    action.put("type", "GET:DOWNLOAD_COUNT");
                                    action.put("id", Common.makeBoundary());
                                    action.put("username", info.getProperty("username"));
                                    action.put("need_response", "true");
                                    queue.addElement(action);
                                    action = UserTools.waitResponse(action, 60);
                                    if (action != null) {
                                        info.put("downloads", action.get("responseText"));
                                    }
                                } else {
                                    info.put("downloads", String.valueOf(ServerStatus.thisObj.statTools.getUserDownloadCount(info.getProperty("username"))));
                                }
                            }
                            catch (Exception e) {
                                Log.log("HTTP_SERVER", 1, e);
                            }
                            String details = "";
                            Enumeration<Object> keys = info.keys();
                            while (keys.hasMoreElements()) {
                                String key = keys.nextElement().toString();
                                String val = info.getProperty(key, "");
                                val = Common.url_decode(val);
                                info.put(key, val);
                                details = String.valueOf(details) + key + ":" + val + "\r-------------------------------\r";
                                if (!key.startsWith("ldap_")) continue;
                                info.remove(key);
                            }
                            String details2 = details;
                            info.put("details", details2);
                            info.put("usernameShare", "false");
                            listing.addElement(info);
                        }
                        catch (Exception e) {
                            Log.log("HTTP_SERVER", 1, e);
                        }
                    }
                    ++x;
                }
            }
            catch (Exception e) {
                Log.log("HTTP_SERVER", 1, e);
            }
        }
        Vector user_list = new Vector();
        UserTools.refreshUserList(thisSession.server_item.getProperty("linkedServer"), user_list);
        int x = 0;
        while (x < user_list.size()) {
            String newUser = com.crushftp.client.Common.dots(user_list.elementAt(x).toString());
            if (newUser.toUpperCase().endsWith(".SHARED")) {
                Log.log("SERVER", 2, "ManageShares:Checking username:" + newUser + " (" + (x + 1) + "/" + user_list.size() + ")");
                VFS tempVFS = UserTools.ut.getVFS(thisSession.server_item.getProperty("linkedServer"), newUser);
                if (tempVFS != null) {
                    Vector items = new Vector();
                    tempVFS.getListing(items, "/Shares/" + thisSession.uiSG("user_name") + "/");
                    int xx = 0;
                    while (xx < items.size()) {
                        VRL vrl;
                        Properties item_info2 = (Properties)items.elementAt(xx);
                        Properties item_info = tempVFS.get_item(String.valueOf(item_info2.getProperty("root_dir")) + item_info2.getProperty("name"));
                        if (item_info == null) {
                            item_info = item_info2;
                        }
                        if ((vrl = new VRL(item_info.getProperty("url"))).getProtocol().equalsIgnoreCase("virtual") || vrl.getProtocol().equalsIgnoreCase("file") && !new File_U(vrl.getPath()).exists()) {
                            Properties request_fake = new Properties();
                            request_fake.put("tempUsername", String.valueOf(newUser.substring(0, newUser.lastIndexOf("."))) + ":" + Common.url_encode("" + vrl));
                            ServerSessionAJAX.deleteShare(request_fake, thisSession);
                        } else {
                            Log.log("SERVER", 2, "ManageShares:Checking username:" + newUser + ":with item(" + (xx + 1) + ":" + items.size() + ")");
                            Properties vItem = (Properties)item_info.get("vItem");
                            Properties sharedUser = new Properties();
                            sharedUser.put("web_link", "");
                            sharedUser.put("username", newUser.substring(0, newUser.lastIndexOf(".")));
                            sharedUser.put("password", "");
                            sharedUser.put("emailFrom", "");
                            sharedUser.put("emailReplyTo", "");
                            sharedUser.put("emailTo", "Username Share : " + newUser.substring(0, newUser.lastIndexOf(".")));
                            sharedUser.put("emailCc", "");
                            sharedUser.put("emailBcc", "");
                            sharedUser.put("emailSubject", "");
                            sharedUser.put("emailBody", "");
                            sharedUser.put("paths", String.valueOf(item_info2.getProperty("root_dir")) + item_info.getProperty("name"));
                            if (vItem != null) {
                                sharedUser.put("expire", vItem.getProperty("expires_on", "never"));
                            }
                            if (sharedUser.getProperty("expire", "").trim().equals("")) {
                                sharedUser.put("expire", "never");
                            }
                            sharedUser.put("details", "");
                            sharedUser.put("attach", "false");
                            sharedUser.put("usernameShare", "true");
                            sharedUser.put("allowUploads", tempVFS.getCombinedPermissions().getProperty(("/Shares/" + thisSession.uiSG("user_name") + "/" + item_info.getProperty("name") + "/").toUpperCase(), "(none)"));
                            sharedUser.put("publishType", "Internal Username Share");
                            if (vItem != null && vItem.containsKey("created_on")) {
                                sharedUser.put("created", vItem.getProperty("created_on"));
                                sharedUser.put("createdMillis", String.valueOf(UserTools.expire_vfs.parse(vItem.getProperty("created_on")).getTime()));
                            } else {
                                sharedUser.put("created", thisSession.date_time.format(new Date(Long.parseLong(item_info.getProperty("modified")))));
                                sharedUser.put("createdMillis", item_info.getProperty("modified"));
                            }
                            sharedUser.put("details", "");
                            if (vItem != null) {
                                sharedUser.put("share_comments", vItem.getProperty("share_comments", ""));
                            }
                            listing.addElement(sharedUser);
                        }
                        ++xx;
                    }
                }
            }
            ++x;
        }
        Log.log("SERVER", 2, "ManageShares:list size:" + listing.size());
        String response = "";
        try {
            Common cfr_ignored_0 = ServerStatus.thisObj.common_code;
            response = Common.getXMLString(listing, "listingInfo", null);
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
        }
        return response;
    }

    public static String deleteShare(Properties request, SessionCrush thisSession) {
        String user_name = thisSession.uiSG("user_name");
        if (ServerStatus.BG("secondary_login_via_email") && user_name.indexOf("@") >= 0 && UserTools.user_email_cache.containsKey(String.valueOf(thisSession.uiSG("listen_ip_port")) + ":" + user_name.toUpperCase())) {
            user_name = UserTools.user_email_cache.getProperty(String.valueOf(thisSession.uiSG("listen_ip_port")) + ":" + user_name.toUpperCase());
        }
        String response = ServerSessionAJAX.deleteShare(request, thisSession.server_item.getProperty("linkedServer"), user_name);
        try {
            if (ServerStatus.BG("replicate_shares")) {
                Properties p = new Properties();
                p.put("request", request);
                p.put("userGroup", thisSession.server_item.getProperty("linkedServer"));
                p.put("username", thisSession.uiSG("user_name"));
                SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.delete.share", "info", p);
            }
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
        }
        return response;
    }

    public static String deleteShare(Properties request, String userGroup, String username) {
        try {
            String tempAccountsPath = ServerStatus.SG("temp_accounts_path");
            File_U[] accounts = (File_U[])new File_U(String.valueOf(tempAccountsPath) + "accounts/").listFiles();
            String tempUsers_value = Common.replace_str(request.getProperty("tempUsername"), "&amp;", "&");
            String[] tempUsers = tempUsers_value.split(";");
            int loop = 0;
            while (loop < tempUsers.length) {
                String curTempUser = tempUsers[loop].trim().replace('+', ' ');
                if (curTempUser.indexOf(":") >= 0) {
                    String userid = com.crushftp.client.Common.dots(Common.url_decode(curTempUser.substring(0, curTempUser.indexOf(":")))).replace('/', '_').replace('\\', '_');
                    Log.log("HTTP_SERVER", 2, "Deleting userid:" + userid);
                    String paths = curTempUser.substring(curTempUser.indexOf(":") + 1).trim();
                    paths = com.crushftp.client.Common.dots(Common.last(Common.url_decode(paths)));
                    Log.log("HTTP_SERVER", 2, "Deleting userid paths:" + paths);
                    VFS tempVFS = UserTools.ut.getVFS(userGroup, String.valueOf(userid) + ".SHARED");
                    Properties virtual = (Properties)tempVFS.homes.elementAt(0);
                    Log.log("HTTP_SERVER", 2, "Loaded VFS:" + virtual);
                    virtual.remove("/Shares/" + username + "/" + paths);
                    virtual.remove("/Shares/" + username + "/" + paths.substring(0, paths.length() - 1));
                    Properties permissions = (Properties)((Vector)virtual.get("vfs_permissions_object")).elementAt(0);
                    if (permissions != null) {
                        permissions.remove(("/Shares/" + username + "/" + paths).toUpperCase());
                        permissions.remove(("/Shares/" + username + "/" + paths.substring(0, paths.length() - 1)).toUpperCase());
                    }
                    Log.log("HTTP_SERVER", 2, "Removing entry:/Shares/" + username + "/" + paths);
                    Vector tempList = new Vector();
                    tempVFS.getListing(tempList, "/Shares/" + username + "/");
                    if (tempList.size() == 0) {
                        virtual.remove("/Shares/" + username + "/");
                        virtual.remove("/Shares/" + username);
                        if (permissions != null) {
                            permissions.remove(("/Shares/" + username + "/").toUpperCase());
                            permissions.remove(("/Shares/" + username).toUpperCase());
                        }
                    }
                    tempList = new Vector();
                    tempVFS.getListing(tempList, "/Shares/");
                    if (tempList.size() == 0) {
                        virtual.remove("/Shares/");
                        virtual.remove("/Shares");
                        if (permissions != null) {
                            permissions.remove("/Shares/".toUpperCase());
                            permissions.remove("/Shares".toUpperCase());
                        }
                    }
                    Properties sharedUser = new Properties();
                    sharedUser.put("password", "");
                    sharedUser.put("version", "1.0");
                    sharedUser.put("root_dir", "/");
                    sharedUser.put("userVersion", "5");
                    sharedUser.put("max_logins", "-1");
                    Vector rootList = new Vector();
                    tempVFS.getListing(rootList, "/");
                    if (rootList.size() == 0) {
                        UserTools.deleteUser(userGroup, String.valueOf(userid) + ".SHARED");
                    } else {
                        UserTools.writeUser(userGroup, String.valueOf(userid) + ".SHARED", sharedUser);
                        UserTools.writeVFS(userGroup, String.valueOf(userid) + ".SHARED", tempVFS);
                    }
                } else {
                    int x = 0;
                    while (accounts != null && x < accounts.length) {
                        try {
                            File_U f = accounts[x];
                            if (f.getName().indexOf(",,") >= 0 && f.isDirectory()) {
                                String[] tokens = f.getName().split(",,");
                                Properties pp = new Properties();
                                int xx = 0;
                                while (xx < tokens.length) {
                                    String key = tokens[xx].substring(0, tokens[xx].indexOf("="));
                                    String val = tokens[xx].substring(tokens[xx].indexOf("=") + 1);
                                    pp.put(key.toUpperCase(), val);
                                    ++xx;
                                }
                                if (username.equalsIgnoreCase(pp.getProperty("M")) && curTempUser.equalsIgnoreCase(pp.getProperty("U"))) {
                                    Common.recurseDelete_U(String.valueOf(tempAccountsPath) + "storage/" + pp.getProperty("U") + pp.getProperty("P"), false);
                                    Common.recurseDelete_U(f.getCanonicalPath(), false);
                                }
                            }
                        }
                        catch (Exception e) {
                            Log.log("HTTP_SERVER", 1, e);
                        }
                        ++x;
                    }
                }
                ++loop;
            }
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
        }
        String response = "";
        try {
            Common cfr_ignored_0 = ServerStatus.thisObj.common_code;
            response = Common.getXMLString(request, "listingInfo", null, true);
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
        }
        return response;
    }

    public static String selfRegistration(Properties request, SessionCrush thisSession, String req_id) {
        String response = "Success";
        try {
            Common.urlDecodePost(request);
            Enumeration<Object> keys = request.keys();
            Properties metaInfo = new Properties();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                if (!key.toUpperCase().startsWith("META_")) continue;
                String val = request.getProperty(key);
                key = key.substring("META_".length());
                metaInfo.put(key, val);
                if (!key.toUpperCase().startsWith("GLOBAL_")) continue;
                if (ServerStatus.thisObj.server_info.get("global_variables") == null) {
                    ServerStatus.thisObj.server_info.put("global_variables", new Properties());
                }
                Properties global_variables = (Properties)ServerStatus.thisObj.server_info.get("global_variables");
                global_variables.put(key, val);
            }
            Properties newUser = new Properties();
            String username = Common.dots(metaInfo.getProperty("registration_username"));
            if (username.indexOf("/") >= 0) {
                throw new Exception("Invalid slash character in username...");
            }
            Properties customForm = null;
            Vector customForms = (Vector)ServerStatus.server_settings.get("CustomForms");
            String pendingSelfRegistration = "pendingSelfRegistration";
            if (customForms != null) {
                int x = 0;
                while (x < customForms.size()) {
                    Properties p = (Properties)customForms.elementAt(x);
                    if (p.getProperty("name", "").equals(metaInfo.getProperty("form_name")) && thisSession.SG("messageForm").indexOf(metaInfo.getProperty("form_name")) >= 0) {
                        customForm = p;
                        break;
                    }
                    ++x;
                }
                if (customForm != null) {
                    if (!customForm.containsKey("entries")) {
                        customForm.put("entries", new Vector());
                    }
                    Vector entries = (Vector)customForm.get("entries");
                    int x2 = 0;
                    while (x2 < entries.size()) {
                        Properties p = (Properties)entries.elementAt(x2);
                        if (!p.getProperty("type").trim().equals("label")) {
                            String val = metaInfo.getProperty(p.getProperty("name", "").trim());
                            if (val != null && p.getProperty("name").trim().startsWith("registration_")) {
                                newUser.put(p.getProperty("name").trim().substring("registration_".length()), val);
                            } else if (p.getProperty("name").trim().startsWith("pendingSelfRegistration")) {
                                pendingSelfRegistration = val.trim();
                            }
                        }
                        ++x2;
                    }
                }
            }
            newUser.put("root_dir", "/");
            newUser.put("user_name", username);
            newUser.put("max_logins", "-1");
            String originalPass = newUser.getProperty("password", newUser.getProperty("password_hidden", Common.makeBoundary()));
            newUser.put("password", ServerStatus.thisObj.common_code.encode_pass(newUser.getProperty("password", newUser.getProperty("password_hidden", Common.makeBoundary())), ServerStatus.SG("password_encryption"), ""));
            Properties password_rules = SessionCrush.build_password_rules(thisSession.user);
            if (!Common.checkPasswordRequirements(originalPass, "", password_rules).equals("")) {
                response = "Failure:" + Common.checkPasswordRequirements(originalPass, "", password_rules);
                thisSession.add_log_formatted("Attempt to register a username using a weak password:" + username + ":" + response, "POST", req_id);
            } else if (UserTools.ut.getUser(thisSession.server_item.getProperty("linkedServer"), username, false) == null || UserTools.ut.getUser(thisSession.server_item.getProperty("linkedServer"), username, false).getProperty("username").equals("template")) {
                UserTools.writeUser(thisSession.server_item.getProperty("linkedServer"), username, newUser);
                Properties groups = UserTools.getGroups(thisSession.server_item.getProperty("linkedServer"));
                Vector<String> groupUsers = (Vector<String>)groups.get(pendingSelfRegistration);
                if (groupUsers == null) {
                    groupUsers = new Vector<String>();
                }
                groups.put(pendingSelfRegistration, groupUsers);
                if (groupUsers.indexOf(username) < 0) {
                    groupUsers.addElement(username);
                }
                if (!pendingSelfRegistration.equals("pendingSelfRegistration")) {
                    Properties inheritance = UserTools.getInheritance(Common.dots(request.getProperty("serverGroup")));
                    Vector<String> vv = new Vector<String>();
                    vv.addElement(pendingSelfRegistration);
                    inheritance.put(username, vv);
                    UserTools.writeInheritance(thisSession.server_item.getProperty("linkedServer"), inheritance);
                } else {
                    pendingSelfRegistration = "pendingSelfRegistration";
                    groupUsers = (Vector<String>)groups.get(pendingSelfRegistration);
                    if (groupUsers == null) {
                        groupUsers = new Vector<String>();
                    }
                    groups.put(pendingSelfRegistration, groupUsers);
                    if (groupUsers.indexOf(username) < 0) {
                        groupUsers.addElement(username);
                    }
                }
                UserTools.writeGroups(thisSession.server_item.getProperty("linkedServer"), groups);
                ServerStatus.thisObj.statTools.insertMetaInfo(thisSession.uiSG("SESSION_RID"), metaInfo, "0");
                Properties metaInfo2 = (Properties)metaInfo.clone();
                metaInfo2.remove("registration_password");
                thisSession.add_log("[" + thisSession.uiSG("user_number") + ":" + thisSession.uiSG("user_name") + ":" + thisSession.uiSG("user_ip") + "] DATA: *messageForm confirmed:" + metaInfo2 + "*", "HTTP");
                Properties fileItem = (Properties)metaInfo.clone();
                fileItem.put("url", "ftp://127.0.0.1:56789/");
                fileItem.put("the_file_path", "/");
                fileItem.put("the_file_size", "1");
                fileItem.put("event_name", "registration");
                fileItem.put("the_file_name", "registration");
                fileItem.put("metaInfo", metaInfo);
                thisSession.uiVG("lastUploadStats").addElement(fileItem);
                thisSession.do_event5("WELCOME", fileItem);
            } else {
                thisSession.add_log_formatted("Attempt to register an existing username:" + username, "POST", req_id);
                response = "Failure";
            }
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
            response = "Failure";
        }
        return response;
    }

    public static String editShare(Properties request, SessionCrush thisSession) {
        String response = ServerSessionAJAX.editShare(request, thisSession.uiSG("user_name"), (Vector)thisSession.user.get("web_customizations"));
        try {
            if (ServerStatus.BG("replicate_shares")) {
                Properties p = new Properties();
                p.put("request", request);
                Vector v = (Vector)thisSession.user.get("web_customizations");
                if (v == null) {
                    v = new Vector();
                }
                p.put("web_customizations", v);
                p.put("username", thisSession.uiSG("user_name"));
                SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.edit.share", "info", p);
            }
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
        }
        return response;
    }

    public static String editShare(Properties request, String username, Vector web_customizations) {
        try {
            String tempAccountsPath = ServerStatus.SG("temp_accounts_path");
            File_U[] accounts = (File_U[])new File_U(String.valueOf(tempAccountsPath) + "accounts/").listFiles();
            String[] tempUsers = request.getProperty("tempUsername").split(";");
            int loop = 0;
            while (loop < tempUsers.length) {
                String curTempUser = tempUsers[loop].trim().replace('+', ' ');
                int x = 0;
                while (accounts != null && x < accounts.length) {
                    try {
                        File_U f = accounts[x];
                        if (f.getName().indexOf(",,") >= 0 && f.isDirectory()) {
                            String[] tokens = f.getName().split(",,");
                            Properties pp = new Properties();
                            int xx = 0;
                            while (xx < tokens.length) {
                                String key = tokens[xx].substring(0, tokens[xx].indexOf("="));
                                String val = tokens[xx].substring(tokens[xx].indexOf("=") + 1);
                                pp.put(key.toUpperCase(), val);
                                ++xx;
                            }
                            if (username.equalsIgnoreCase(pp.getProperty("M")) && curTempUser.equalsIgnoreCase(pp.getProperty("U"))) {
                                int maxExpireDays = 0;
                                if (web_customizations != null) {
                                    int xx2 = 0;
                                    while (xx2 < web_customizations.size()) {
                                        Properties cust = (Properties)web_customizations.elementAt(xx2);
                                        if (cust.getProperty("key").equals("EXPIREDAYSMAX")) {
                                            maxExpireDays = Integer.parseInt(cust.getProperty("value").trim());
                                        }
                                        ++xx2;
                                    }
                                }
                                SimpleDateFormat ex1 = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
                                SimpleDateFormat ex2 = new SimpleDateFormat("MMddyyyyHHmm", Locale.US);
                                Date requestExpire = ex1.parse(request.getProperty("expire", "1/1/1970 00:01").replace('+', ' '));
                                GregorianCalendar gc = new GregorianCalendar();
                                gc.setTime(new Date());
                                ((Calendar)gc).add(5, maxExpireDays);
                                if (!(maxExpireDays <= 0 || request.containsKey("expire") && requestExpire.getTime() <= gc.getTime().getTime())) {
                                    requestExpire = gc.getTime();
                                }
                                String expire_date = ex2.format(requestExpire);
                                request.put("expire", ex1.format(requestExpire));
                                String folderName = "u=" + pp.getProperty("U") + ",,p=" + pp.getProperty("P") + ",,m=" + pp.getProperty("M") + ",,t=" + pp.getProperty("T") + ",,ex=" + expire_date;
                                if (request.getProperty("logins", "").trim().equals("-1")) {
                                    request.remove("logins");
                                }
                                if (!request.getProperty("logins", "").equals("")) {
                                    folderName = String.valueOf(folderName) + ",,i=" + request.getProperty("logins", "");
                                }
                                Properties info = (Properties)Common.readXMLObject_U(String.valueOf(f.getPath()) + "/INFO.XML");
                                info.put("EX", expire_date);
                                Common.writeXMLObject_U(String.valueOf(f.getPath()) + "/INFO.XML", info, "INFO");
                                f.renameTo(new File_U(String.valueOf(Common.all_but_last(f.getPath())) + folderName));
                            }
                        }
                    }
                    catch (Exception e) {
                        Log.log("HTTP_SERVER", 1, e);
                    }
                    ++x;
                }
                ++loop;
            }
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
        }
        String response = "";
        try {
            Common cfr_ignored_0 = ServerStatus.thisObj.common_code;
            response = Common.getXMLString(request, "listingInfo", null, true);
        }
        catch (Exception e) {
            Log.log("HTTP_SERVER", 1, e);
        }
        return response;
    }

    public static String processKeywordsEdit(Properties request, SessionCrush thisSession) throws Exception {
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n";
        String[] names = null;
        names = request.getProperty("names").indexOf(";") >= 0 ? Common.url_decode(request.getProperty("names")).replace('>', '_').replace('<', '_').split(";") : Common.url_decode(request.getProperty("names")).replace('>', '_').replace('<', '_').split("\r\n");
        String keyword = com.crushftp.client.Common.xss_strip(request.getProperty("keywords").trim());
        response = String.valueOf(response) + "<commandResult><response>";
        boolean ok = false;
        int x = 0;
        while (x < names.length) {
            String the_dir = Common.url_decode(Common.all_but_last(names[x]));
            if (the_dir.startsWith(thisSession.SG("root_dir"))) {
                the_dir = the_dir.substring(thisSession.SG("root_dir").length() - 1);
            }
            thisSession.uiPUT("current_dir", thisSession.getStandardizedDir(the_dir));
            Properties item = thisSession.uVFS.get_item(String.valueOf(thisSession.uiSG("current_dir")) + Common.last(names[x]));
            if (!item.getProperty("privs", "").toLowerCase().contains("(write)")) {
                Log.log("HTTP_SERVER", 1, "Edit Keyword: ERROR: Permission denied! " + VRL.safe(item));
                response = String.valueOf(response) + "Permission denied.";
            } else {
                the_dir = SearchHandler.getPreviewPath(item.getProperty("url"), "1", 1);
                String index = String.valueOf(ServerStatus.SG("previews_path")) + the_dir.substring(1);
                if (!new File_U(Common.all_but_last(index)).exists()) {
                    new File_U(Common.all_but_last(index)).mkdirs();
                }
                if (ServerStatus.BG("exif_keywords")) {
                    String srcFile = com.crushftp.client.Common.dots(new VRL(item.getProperty("url")).getPath());
                    int xx = 0;
                    while (xx < ServerStatus.thisObj.previewWorkers.size()) {
                        PreviewWorker preview = (PreviewWorker)ServerStatus.thisObj.previewWorkers.elementAt(xx);
                        if (preview.prefs.getProperty("preview_enabled", "false").equalsIgnoreCase("true") && preview.checkExtension(Common.last(the_dir), item)) {
                            Properties p;
                            String keywords = Common.url_decode(request.getProperty("keywords")).trim();
                            if (request.getProperty("append", "false").equals("true") && !(p = preview.getExifInfo(srcFile, PreviewWorker.getDestPath2(String.valueOf(item.getProperty("url")) + "/p1/"))).getProperty("keywords", "").equals("")) {
                                keywords = String.valueOf(p.getProperty("keywords", "")) + keywords;
                            }
                            preview.setExifInfo(srcFile, PreviewWorker.getDestPath2(String.valueOf(item.getProperty("url")) + "/p1/"), "keywords", keywords);
                            ok = true;
                            break;
                        }
                        ++xx;
                    }
                } else {
                    long size = new File_U(String.valueOf(Common.all_but_last(Common.all_but_last(index))) + "index.txt").length();
                    if (!request.getProperty("append", "false").equals("true")) {
                        new File_U(String.valueOf(Common.all_but_last(Common.all_but_last(index))) + "index.txt").delete();
                    }
                    RandomAccessFile out = new RandomAccessFile(new File_U(String.valueOf(Common.all_but_last(Common.all_but_last(index))) + "index.txt"), "rw");
                    if (request.getProperty("append", "false").equals("true")) {
                        out.seek(size);
                    }
                    out.write((String.valueOf(keyword) + "\r\n").getBytes());
                    out.close();
                    ok = true;
                }
                SearchHandler.buildEntry(item, thisSession.uVFS, "new", null);
            }
            ++x;
        }
        if (ok) {
            response = String.valueOf(response) + "Keywords Edited.\r\n";
        }
        response = String.valueOf(response) + "</response></commandResult>";
        return response;
    }

    public static String handle_message_form(Properties request, SessionCrush thisSession) {
        Enumeration<Object> keys = request.keys();
        Properties metaInfo = new Properties();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            if (!key.toUpperCase().startsWith("META_")) continue;
            String val = request.getProperty(key);
            key = key.substring("META_".length());
            metaInfo.put(key, val);
            if (key.toUpperCase().startsWith("GLOBAL_")) {
                if (ServerStatus.thisObj.server_info.get("global_variables") == null) {
                    ServerStatus.thisObj.server_info.put("global_variables", new Properties());
                }
                Properties global_variables = (Properties)ServerStatus.thisObj.server_info.get("global_variables");
                global_variables.put(key, val);
                continue;
            }
            if (!key.toUpperCase().startsWith("USER_INFO_")) continue;
            thisSession.user_info.put(key, val);
        }
        ServerStatus.thisObj.statTools.insertMetaInfo(thisSession.uiSG("SESSION_RID"), metaInfo, "0");
        thisSession.add_log("[" + thisSession.uiSG("user_number") + ":" + thisSession.uiSG("user_name") + ":" + thisSession.uiSG("user_ip") + "] DATA: *messageForm confirmed:" + metaInfo + "*", "HTTP");
        Properties fileItem = (Properties)metaInfo.clone();
        fileItem.put("url", "ftp://127.0.0.1:56789/");
        fileItem.put("the_file_path", "/");
        fileItem.put("the_file_size", "1");
        fileItem.put("event_name", "welcome");
        fileItem.put("the_file_name", "welcome");
        Properties info = thisSession.do_event5("WELCOME", fileItem);
        if (info == null) {
            info = new Properties();
        }
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n<commandResult><response>Success</response><new_job_id>" + info.getProperty("new_job_id", "") + "</new_job_id></commandResult>";
        return response;
    }

    /*
     * Unable to fully structure code
     */
    public static String search(Properties request, SessionCrush thisSession) throws IOException, Exception, InterruptedException {
        thisSession.put("search_status", "0/1");
        the_dir = Common.url_decode(request.getProperty("path"));
        if (the_dir.startsWith(thisSession.SG("root_dir"))) {
            the_dir = the_dir.substring(thisSession.SG("root_dir").length() - 1);
        }
        thisSession.uiPUT("current_dir", thisSession.getStandardizedDir(the_dir));
        keywords = Common.url_decode(request.getProperty("keyword").replace('+', ' ')).trim().split(" ");
        exact = request.getProperty("exact", "").equalsIgnoreCase("true");
        all_keywords = request.getProperty("all_keywords", "false").equalsIgnoreCase("true");
        date1 = request.getProperty("date1", "").equalsIgnoreCase("true");
        date1_action = Common.url_decode(request.getProperty("date1_action", "").replace('+', ' '));
        date1_value = request.getProperty("date1_value", "");
        date2 = request.getProperty("date2", "").equalsIgnoreCase("true");
        date2_action = Common.url_decode(request.getProperty("date2_action", "").replace('+', ' '));
        date2_value = request.getProperty("date2_value", "");
        size1 = request.getProperty("size1", "").equalsIgnoreCase("true");
        size1_action = Common.url_decode(request.getProperty("size1_action", "").replace('+', ' '));
        size1_value = request.getProperty("size1_value", "");
        size2 = request.getProperty("size2", "").equalsIgnoreCase("true");
        size2_action = Common.url_decode(request.getProperty("size2_action", "").replace('+', ' '));
        size2_value = request.getProperty("size2_value", "");
        type1 = request.getProperty("type1", "").equalsIgnoreCase("true");
        type1_action = Common.url_decode(request.getProperty("type1_action", "").replace('+', ' '));
        or_type = request.getProperty("include_type", "or").equalsIgnoreCase("or");
        keywords_only = request.getProperty("keywords_only", "false").equals("true");
        thisSession.uiPUT("the_command", "LIST");
        thisSession.uiPUT("the_command_data", the_dir);
        foundItems = new Vector<Properties>();
        mmddyyyy = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        processedCount = 0L;
        errors = 0;
        listing = new Vector<E>();
        searchHandler = new SearchHandler(thisSession, listing, thisSession.uiSG("current_dir"), Integer.parseInt(request.getProperty("depth", "20")));
        Worker.startWorker(searchHandler);
        root_item = thisSession.uVFS.get_item(thisSession.uiSG("root_dir"));
        ** GOTO lbl194
        {
            Thread.sleep(100L);
            do {
                if (searchHandler.isActive() && listing.size() == 0) continue block5;
                if (listing.size() == 0) break block5;
                pp = (Properties)listing.elementAt(0);
                listing.removeElementAt(0);
                thisSession.put("search_status", String.valueOf(++processedCount) + "/" + ((long)listing.size() + processedCount));
                try {
                    date_ok = true;
                    size_ok = true;
                    type_ok = true;
                    name_count = 0;
                    if (date1) {
                        modified1 = Long.parseLong(pp.getProperty("modified"));
                        modified2 = mmddyyyy.parse(date1_value).getTime();
                        if (date1_action.equalsIgnoreCase("before") && modified2 <= modified1) {
                            date_ok = false;
                        } else if (date1_action.equalsIgnoreCase("after") && modified2 >= modified1) {
                            date_ok = false;
                        }
                    }
                    if (date2) {
                        modified1 = Long.parseLong(pp.getProperty("modified"));
                        modified2 = mmddyyyy.parse(date2_value).getTime();
                        if (date2_action.equalsIgnoreCase("before") && modified2 <= modified1) {
                            date_ok = false;
                        } else if (date2_action.equalsIgnoreCase("after") && modified2 >= modified1) {
                            date_ok = false;
                        }
                    }
                    if (size1) {
                        file_size1 = Long.parseLong(pp.getProperty("size"));
                        file_size2 = Long.parseLong(size1_value) * 1024L;
                        if (size1_action.equalsIgnoreCase("bigger than") && file_size2 >= file_size1) {
                            size_ok = false;
                        } else if (size1_action.equalsIgnoreCase("smaller than") && file_size2 <= file_size1) {
                            size_ok = false;
                        }
                    }
                    if (size2) {
                        file_size1 = Long.parseLong(pp.getProperty("size"));
                        file_size2 = Long.parseLong(size2_value) * 1024L;
                        if (size2_action.equalsIgnoreCase("bigger than") && file_size2 >= file_size1) {
                            size_ok = false;
                        } else if (size2_action.equalsIgnoreCase("smaller than") && file_size2 <= file_size1) {
                            size_ok = false;
                        }
                    }
                    if (type1) {
                        item_type1 = pp.getProperty("type");
                        if (type1_action.equalsIgnoreCase("file") && !item_type1.equalsIgnoreCase("file")) {
                            type_ok = false;
                        } else if (type1_action.equalsIgnoreCase("folder") && !item_type1.equalsIgnoreCase("dir")) {
                            type_ok = false;
                        }
                    }
                    if (date_ok && size_ok && type_ok && keywords.length > 0) {
                        if (!keywords_only) {
                            loop = 0;
                            last_loc = 0;
                            while (loop < keywords.length) {
                                if (name_count > 0 && !all_keywords && or_type) break;
                                Log.log("HTTP_SERVER", 2, "search item name:" + pp.getProperty("name") + " vs. " + keywords[loop]);
                                if (!exact && !all_keywords && pp.getProperty("name").toUpperCase().indexOf(keywords[loop].toUpperCase().trim()) >= 0) {
                                    ++name_count;
                                }
                                if (exact && pp.getProperty("name").toUpperCase().equals(Common.url_decode(request.getProperty("keyword").replace('+', ' ')).trim())) {
                                    ++name_count;
                                }
                                if (all_keywords && pp.getProperty("name").toUpperCase().indexOf(keywords[loop].toUpperCase().trim(), last_loc) >= 0) {
                                    last_loc = pp.getProperty("name").toUpperCase().indexOf(keywords[loop].toUpperCase().trim(), last_loc);
                                    ++name_count;
                                }
                                ++loop;
                            }
                            if (all_keywords && name_count < keywords.length) {
                                name_count = 0;
                            }
                        }
                        if (name_count == 0 && (ServerStatus.BG("search_keywords_also") || ServerStatus.BG("search_file_contents_also"))) {
                            Log.log("HTTP_SERVER", 2, "name still not found, trying more...");
                            indexText = pp.getProperty("keywords", "");
                            if (!pp.containsKey("keywords") || ServerStatus.BG("search_file_contents_also")) {
                                if (ServerStatus.BG("search_file_contents_also")) {
                                    baos = new ByteArrayOutputStream();
                                    vrl = new VRL(pp.getProperty("url"));
                                    if (vrl.getProtocol().equalsIgnoreCase("FILE")) {
                                        temp_item = new File_U(vrl.getPath());
                                        if (temp_item.exists() && temp_item.isFile()) {
                                            Log.log("HTTP_SERVER", 2, "Getting file contents to search..." + vrl.safe() + ":" + temp_item.length());
                                            if (temp_item.length() < 0x500000L) {
                                                Common.streamCopier(new FileInputStream(temp_item), baos, false, true, true);
                                            } else {
                                                Log.log("HTTP_SERVER", 2, "Skipping file contents (too big) to search..." + vrl.safe() + ":" + temp_item.length());
                                            }
                                        }
                                    } else {
                                        c = thisSession.uVFS.getClient(pp);
                                        try {
                                            path = pp.getProperty("path") == null ? vrl.getPath() : pp.getProperty("path");
                                            size = c.getLength(path);
                                            Log.log("HTTP_SERVER", 2, "Getting file contents to search..." + vrl.safe() + ":" + size);
                                            if (size < 0x500000L) {
                                                Common.streamCopier(c.download(path, 0L, -1L, true), baos, false, true, true);
                                            } else {
                                                Log.log("HTTP_SERVER", 2, "Skipping file contents (too big) to search..." + vrl.safe() + ":" + size);
                                            }
                                        }
                                        finally {
                                            thisSession.uVFS.releaseClient(c);
                                        }
                                    }
                                    indexText = new String(baos.toByteArray(), "UTF8");
                                } else {
                                    indexText = SearchHandler.getKeywords(pp.getProperty("url"));
                                }
                            }
                            x = 0;
                            while (x < keywords.length) {
                                if (name_count > 0 && or_type) break;
                                Log.log("HTTP_SERVER", 2, "search item indexText:" + indexText + " vs. " + keywords[x]);
                                if (!exact && indexText.toUpperCase().indexOf(keywords[x].toUpperCase().trim()) >= 0) {
                                    ++name_count;
                                }
                                if (exact && indexText.toUpperCase().indexOf("\r\n" + keywords[x].toUpperCase().trim() + "\r\n") >= 0) {
                                    ++name_count;
                                }
                                ++x;
                            }
                            if (exact && keywords_only && keywords.length > 1 && indexText.toUpperCase().indexOf(Common.url_decode(request.getProperty("keyword").replace('+', ' ')).trim()) >= 0) {
                                name_count = keywords.length;
                                Log.log("HTTP_SERVER", 2, "Search:Found full text exact match.");
                            }
                        }
                        if (!or_type && name_count < keywords.length) {
                            Log.log("HTTP_SERVER", 2, "search item or_type:" + or_type + " name_count:" + name_count + " versus keyword count:" + keywords.length);
                            name_count = 0;
                        }
                    }
                    Log.log("HTTP_SERVER", 2, "search item or_type:" + or_type + " name_count:" + name_count + " date_ok:" + date_ok + " size_ok:" + size_ok + " type_ok:" + type_ok);
                    if (name_count <= 0 || !date_ok || !size_ok || !type_ok) continue;
                    Log.log("HTTP_SERVER", 2, "search item result:" + pp.getProperty("name"));
                    if (pp.getProperty("db", "false").equals("true")) {
                        ppp = SearchHandler.findItem(pp, thisSession.uVFS, null, thisSession.uiSG("root_dir"));
                        Log.log("HTTP_SERVER", 2, "search item result:" + ppp);
                        if (ppp == null) continue;
                        pp.putAll((Map<?, ?>)ppp);
                    }
                    if (pp.getProperty("name").equals("VFS") || pp.getProperty("privs", "").indexOf("(invisible)") >= 0 || pp.getProperty("privs", "").indexOf("(view)") < 0 || pp.getProperty("url", "").equals(root_item.getProperty("url"))) continue;
                    Log.log("HTTP_SERVER", 2, "search item adding found item");
                    foundItems.addElement(pp);
                    privs = pp.getProperty("privs", "");
                    if (privs.indexOf("(comment") >= 0) {
                        privs = String.valueOf(privs.substring(0, privs.indexOf("(comment"))) + privs.substring(privs.indexOf(")", privs.indexOf("(comment")));
                    }
                    privs = Common.replace_str(privs, "(inherited)", "");
                    current_dir2 = pp.getProperty("root_dir");
                    if (current_dir2.toUpperCase().startsWith(thisSession.SG("root_dir").toUpperCase())) {
                        current_dir2 = current_dir2.substring(thisSession.SG("root_dir").length() - 1);
                    }
                    pp.put("privs", String.valueOf(privs) + "(comment" + Common.url_encode("Containing Folder:<a href='" + current_dir2 + "'>" + current_dir2 + "</a>") + ")");
                }
                catch (Exception e) {
                    Log.log("HTTP_SERVER", 1, e);
                    if (errors++ > 1000) break block5;
                }
lbl194:
                // 6 sources

            } while (searchHandler.isActive() || listing.size() > 0);
        }
        listingProp = ServerSessionAJAX.getListingInfo(foundItems, thisSession.SG("root_dir"), "(VIEW)", false, thisSession.uVFS, false, true, thisSession, true);
        altList = AgentUI.getJsonListObj(listingProp, ServerStatus.BG("exif_listings"));
        info = "{\r\n";
        info = String.valueOf(info) + "\t\"privs\" : \"" + listingProp.getProperty("privs", "").trim().replaceAll("\"", "%22").replaceAll("\t", "%09") + "\",\r\n";
        info = String.valueOf(info) + "\t\"path\" : \"" + listingProp.getProperty("path", "").trim().replaceAll("\"", "%22").replaceAll("\t", "%09") + "\",\r\n";
        info = String.valueOf(info) + "\t\"defaultStrings\" : \"" + listingProp.getProperty("defaultStrings", "").trim() + "\",\r\n";
        info = String.valueOf(info) + "\t\"site\" : \"" + listingProp.getProperty("site", "").trim() + "\",\r\n";
        info = String.valueOf(info) + "\t\"quota\" : \"" + listingProp.getProperty("quota", "").trim() + "\",\r\n";
        info = String.valueOf(info) + "\t\"quota_bytes\" : \"" + listingProp.getProperty("quota_bytes", "").trim() + "\",\r\n";
        info = String.valueOf(info) + "\t\"listing\" : " + altList + "\r\n";
        info = String.valueOf(info) + "}\r\n";
        return info;
    }

    public static void handleCustomizations(Vector customizations, SessionCrush session) {
        boolean hasLogo = false;
        Properties footer = null;
        int x = 0;
        while (x < customizations.size()) {
            Properties pp = (Properties)customizations.elementAt(x);
            String key = pp.getProperty("key");
            if (key.startsWith("flash_")) {
                key = key.substring("flash_".length());
                pp.put("key", key);
            }
            pp.put("value", ServerStatus.thisObj.change_vars_to_values(pp.getProperty("value", ""), session));
            try {
                if (pp.getProperty("value", "").indexOf("{user") >= 0) {
                    pp.put("value", com.crushftp.client.Common.textFunctions(pp.getProperty("value"), "{", "}"));
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (key.equals("logo")) {
                hasLogo = true;
            }
            if (key.equals("footer")) {
                footer = pp;
            }
            if (key.equals("promptForRefreshToken")) {
                session.uiPUT("prompt_for_refresh_token", "true");
            }
            ++x;
        }
        if (!hasLogo && !ServerStatus.SG("default_logo").equals("")) {
            Properties pp = new Properties();
            pp.put("key", "logo");
            pp.put("value", ServerStatus.SG("default_logo"));
            customizations.addElement(pp);
        }
        if (footer == null) {
            footer = new Properties();
            footer.put("key", "footer");
            footer.put("value", "");
            customizations.addElement(footer);
        }
        footer.put("value", String.valueOf(footer.getProperty("value")) + ServerStatus.SG("webFooterText"));
    }
}

