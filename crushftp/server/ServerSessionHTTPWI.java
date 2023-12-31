/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.compress.archivers.zip.ZipArchiveEntry
 *  org.apache.commons.compress.archivers.zip.ZipFile
 */
package crushftp.server;

import com.crushftp.client.Common;
import com.crushftp.client.File_B;
import com.crushftp.client.File_S;
import com.crushftp.client.File_U;
import crushftp.handlers.Log;
import crushftp.handlers.SessionCrush;
import crushftp.server.ServerSessionHTTP;
import crushftp.server.ServerStatus;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

public class ServerSessionHTTPWI {
    public static File_B serveFile(ServerSessionHTTP sessionHTTP, Vector header, OutputStream original_os, boolean onlyGetFile, String downloadFilename) throws Exception {
        Properties p;
        int x;
        Vector v;
        String data = "";
        boolean acceptsGZIP = false;
        File file = null;
        String theFile = "";
        String ifnonematch = "0";
        String domain = "";
        boolean headerOnly = false;
        int x2 = 0;
        while (x2 < header.size()) {
            data = header.elementAt(x2).toString();
            if (data.startsWith("Accept-Encoding: ")) {
                if ((data = data.substring(data.indexOf(" ") + 1)).toUpperCase().indexOf("GZIP") >= 0 && ServerStatus.BG("allow_gzip")) {
                    acceptsGZIP = true;
                }
            } else if (x2 == 0 && (data.startsWith("HEAD ") || data.startsWith("GET ")) && (data.indexOf(" /WebInterface/") >= 0 || data.toUpperCase().indexOf(" /FAVICON.ICO") >= 0 || data.indexOf(" /.well-known/acme-challenge/") >= 0)) {
                if (data.toUpperCase().startsWith("HEAD ")) {
                    headerOnly = true;
                }
                theFile = data.substring(data.indexOf(" ") + 1, data.lastIndexOf(" HTTP"));
                if (data.toUpperCase().indexOf(" /FAVICON.ICO") >= 0) {
                    theFile = "/WebInterface" + theFile;
                }
                if (data.indexOf(" /.well-known/acme-challenge/") >= 0) {
                    theFile = "/WebInterface/" + crushftp.handlers.Common.last(theFile);
                }
                theFile = crushftp.handlers.Common.url_decode(theFile);
                theFile = crushftp.handlers.Common.replace_str(theFile, "..", "");
                theFile = crushftp.handlers.Common.replace_str(theFile, "\\", "/");
                if ((theFile = crushftp.handlers.Common.replace_str(theFile, "//", "/")).indexOf("?") >= 0) {
                    theFile = theFile.substring(0, theFile.indexOf("?"));
                }
                if (data.indexOf(" /WebInterface/images/Preview/") >= 0) {
                    file = new File_B(new File_U(String.valueOf(ServerStatus.SG("previews_path")) + theFile.substring("/WebInterface/images/".length())));
                } else {
                    file = new File_B(new File_S(Common.dots(new File_S(String.valueOf(System.getProperty("crushftp.web")) + theFile.substring(1)).getCanonicalPath())));
                    if (file.getCanonicalPath().indexOf("WebInterface") < 0) {
                        file = null;
                    }
                }
            } else if (data.toUpperCase().startsWith("IF-MODIFIED-SINCE: ")) {
                ifnonematch = data.substring(data.toUpperCase().indexOf("IF-MODIFIED-SINCE:") + "IF-MODIFIED-SINCE:".length()).trim();
                try {
                    ifnonematch = String.valueOf(sessionHTTP.sdf_rfc1123.parse(ifnonematch).getTime());
                }
                catch (Exception exception) {}
            } else if (data.toUpperCase().startsWith("IF-NONE-MATCH: ")) {
                ifnonematch = data.substring(data.toUpperCase().indexOf("IF-NONE-MATCH:") + "IF-NONE-MATCH:".length()).trim();
            } else if ((data.toUpperCase().startsWith("HOST: ") || data.toUpperCase().startsWith("X-FORWARDED-HOST: ")) && (domain = data.substring(data.toUpperCase().indexOf(" ") + 1).trim().toUpperCase()).indexOf(":") >= 0) {
                domain = domain.substring(0, domain.indexOf(":"));
            }
            ++x2;
        }
        if (file != null) {
            String root_wi_path = crushftp.handlers.Common.dots(new File_U(ServerStatus.SG("previews_path")).getCanonicalPath().replace('\\', '/'));
            String request_file = crushftp.handlers.Common.dots(file.getCanonicalPath().replace('\\', '/'));
            if (request_file.toUpperCase().indexOf("/WEBINTERFACE/") < 0 && request_file.toUpperCase().indexOf("/PREVIEW/") < 0) {
                file = null;
            }
        }
        if (theFile.toUpperCase().endsWith(".JPG") || theFile.toUpperCase().endsWith(".GIF") || theFile.toUpperCase().endsWith(".JAR") || theFile.toUpperCase().endsWith(".SWF")) {
            acceptsGZIP = false;
        }
        Common.updateMimes();
        Properties mimes = Common.mimes;
        String ext = "";
        if (theFile.toString().lastIndexOf(".") >= 0) {
            ext = theFile.toString().substring(theFile.toString().lastIndexOf(".")).toUpperCase();
        }
        if (mimes.getProperty(ext, "").equals("")) {
            ext = "*";
        }
        if (file.getName().equalsIgnoreCase("login.html")) {
            try {
                v = ServerStatus.VG("login_page_list");
                x = 0;
                while (x < v.size()) {
                    p = (Properties)v.elementAt(x);
                    if (Common.do_search(p.getProperty("domain"), domain, false, 0)) {
                        File_S tmpLogin = new File_S(String.valueOf(file.getCanonicalFile().getParentFile().getPath()) + "/" + p.getProperty("page"));
                        if (tmpLogin.exists()) {
                            file = new File_B(tmpLogin);
                        }
                        break;
                    }
                    ++x;
                }
            }
            catch (Exception e) {
                Log.log("HTTP_SERVER", 1, e);
            }
        }
        if (file.getName().equalsIgnoreCase("favicon.ico")) {
            try {
                v = ServerStatus.VG("login_page_list");
                x = 0;
                while (x < v.size()) {
                    p = (Properties)v.elementAt(x);
                    if (Common.do_search(p.getProperty("domain"), domain, false, 0)) {
                        file = new File_B(new File_S(String.valueOf(file.getCanonicalFile().getParentFile().getPath()) + "/" + p.getProperty("favicon", "favicon.ico")));
                        Log.log("HTTP_SERVER", 2, "Favicon  domain:" + domain + " file path: " + file.getPath());
                        break;
                    }
                    ++x;
                }
            }
            catch (Exception e) {
                Log.log("HTTP_SERVER", 1, e);
            }
        }
        if (onlyGetFile) {
            return file;
        }
        long lastModified = System.currentTimeMillis();
        if (file.exists()) {
            lastModified = file.lastModified();
        }
        ByteArrayOutputStream tempBytes = new ByteArrayOutputStream();
        String template = "";
        if (!file.exists() && file.getPath().indexOf(".zip") >= 0) {
            String zipFile = file.getPath().substring(0, file.getPath().indexOf(".zip") + 4);
            ZipFile zf = new ZipFile(zipFile);
            byte[] b = new byte[32768];
            int bytesRead = 0;
            ZipArchiveEntry zae = zf.getEntry(file.getPath().substring(zipFile.length() + 1).replace('\\', '/'));
            lastModified = zae.getTime();
            InputStream otherFile = zf.getInputStream(zae);
            while (bytesRead >= 0) {
                bytesRead = otherFile.read(b);
                if (bytesRead <= 0) continue;
                tempBytes.write(b, 0, bytesRead);
            }
            otherFile.close();
            zf.close();
            if ((mimes.getProperty(ext, "").toUpperCase().endsWith("HTML") || mimes.getProperty(ext, "").toUpperCase().endsWith("/CSS") || mimes.getProperty(ext, "").toUpperCase().endsWith("/JAVASCRIPT") || mimes.getProperty(ext, "").toUpperCase().endsWith("/X-JAVA-JNLP-FILE")) && theFile.toUpperCase().startsWith("/WEBINTERFACE/")) {
                String template2;
                template = new String(tempBytes.toByteArray(), "UTF8");
                if (!template.equals(template2 = crushftp.handlers.Common.replace_str(template, "/WebInterface/", String.valueOf(sessionHTTP.proxy) + "WebInterface/"))) {
                    lastModified = System.currentTimeMillis();
                }
                template = template2;
            }
        }
        if (file.exists() && (mimes.getProperty(ext, "").toUpperCase().endsWith("HTML") || mimes.getProperty(ext, "").toUpperCase().endsWith("/CSS") || mimes.getProperty(ext, "").toUpperCase().endsWith("/JAVASCRIPT") || mimes.getProperty(ext, "").toUpperCase().endsWith("/X-JAVA-JNLP-FILE")) && theFile.toUpperCase().startsWith("/WEBINTERFACE/")) {
            byte[] b = new byte[(int)file.length()];
            int bytesRead = 0;
            FileInputStream otherFile = new FileInputStream(file);
            while (bytesRead >= 0) {
                bytesRead = otherFile.read(b);
                if (bytesRead <= 0) continue;
                template = String.valueOf(template) + new String(b, 0, bytesRead, "UTF8");
            }
            otherFile.close();
            String template2 = crushftp.handlers.Common.replace_str(template, "/WebInterface/", String.valueOf(sessionHTTP.proxy) + "WebInterface/");
            if (!template.equals(template2)) {
                lastModified = System.currentTimeMillis();
            }
            template = template2;
            if (ServerStatus.BG("email_reset_token")) {
                template = crushftp.handlers.Common.replace_str(template, "function emailPassword()", "function emailPassword() {location.href = '" + sessionHTTP.proxy + "WebInterface/jQuery/reset.html';}\r\nfunction emailPassword2()");
                template = crushftp.handlers.Common.replace_str(template, "javascript:emailPassword();", String.valueOf(sessionHTTP.proxy) + "WebInterface/jQuery/reset.html");
                template = crushftp.handlers.Common.replace_str(template, "javascript:showResetPanel();", String.valueOf(sessionHTTP.proxy) + "WebInterface/jQuery/reset.html");
            }
        }
        if (mimes.getProperty(ext, "").toUpperCase().endsWith("/HTML") && theFile.toUpperCase().startsWith("/WEBINTERFACE/")) {
            if (ServerStatus.SG("default_logo").equals("logo.gif")) {
                ServerStatus.server_settings.put("default_logo", "logo.png");
            }
            template = ServerStatus.thisObj.change_vars_to_values(template, null);
            if (!ServerStatus.SG("default_logo").equals("") && !ServerStatus.SG("default_logo").equalsIgnoreCase("logo.png")) {
                template = crushftp.handlers.Common.replace_str(template, "<a id=\"defaultLogoLink\" href=\"http://www." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".com/\">", "<a id=\"defaultLogoLink\" href=\"javascript:void();\">");
                template = crushftp.handlers.Common.replace_str(template, "<div id=\"headerdiv\"><a href=\"http://www." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".com/\">", "<div id=\"headerdiv\">");
                template = crushftp.handlers.Common.replace_str(template, "/logo.png\" /></a>", "/logo.png\" />");
                template = crushftp.handlers.Common.replace_str(template, "/logo.gif\" /></a>", "/logo.gif\" />");
                template = crushftp.handlers.Common.replace_str(template, "/logo.png", "/" + ServerStatus.SG("default_logo"));
                template = crushftp.handlers.Common.replace_str(template, "/logo.gif", "/" + ServerStatus.SG("default_logo"));
            }
            String script = ServerStatus.SG("login_custom_script");
            if (!ServerStatus.BG("login_autocomplete_off")) {
                if (!script.contains("$(document).ready(function(){\n$(\"#username\").attr(\"autocomplete\",\"off\");\n$(\"#password\").attr(\"autocomplete\",\"off\");\n});") && !script.contains("$(document).ready(function(){\n$(\"#username\").attr(\"autocomplete\",\"on\");\n$(\"#password\").attr(\"autocomplete\",\"on\");\n});")) {
                    script = script.toLowerCase().indexOf("</script>") < 0 ? "$(document).ready(function(){\n$(\"#username\").attr(\"autocomplete\",\"on\");\n$(\"#password\").attr(\"autocomplete\",\"on\");\n});" + script : "<script>\n$(document).ready(function(){\n$(\"#username\").attr(\"autocomplete\",\"on\");\n$(\"#password\").attr(\"autocomplete\",\"on\");\n});\"</script>\n" + script;
                } else if (script.contains("$(document).ready(function(){\n$(\"#username\").attr(\"autocomplete\",\"off\");\n$(\"#password\").attr(\"autocomplete\",\"off\");\n});")) {
                    script = crushftp.handlers.Common.replace_str(script, "$(document).ready(function(){\n$(\"#username\").attr(\"autocomplete\",\"off\");\n$(\"#password\").attr(\"autocomplete\",\"off\");\n});", "$(document).ready(function(){\n$(\"#username\").attr(\"autocomplete\",\"on\");\n$(\"#password\").attr(\"autocomplete\",\"on\");\n});");
                }
            } else if (script.contains("$(document).ready(function(){\n$(\"#username\").attr(\"autocomplete\",\"on\");\n$(\"#password\").attr(\"autocomplete\",\"on\");\n});")) {
                script = crushftp.handlers.Common.replace_str(script, "$(document).ready(function(){\n$(\"#username\").attr(\"autocomplete\",\"on\");\n$(\"#password\").attr(\"autocomplete\",\"on\");\n});", "$(document).ready(function(){\n$(\"#username\").attr(\"autocomplete\",\"off\");\n$(\"#password\").attr(\"autocomplete\",\"off\");\n});");
            }
            if (ServerStatus.BG("login_autocomplete_off")) {
                if (script.toLowerCase().indexOf("</script>") < 0) {
                    script = String.valueOf(script) + "\r\n$(document).ready(function(){$('#rememberMePanel').hide();window.dontShowRememberMeOptionOnLoginPage = true;});";
                    script = String.valueOf(script) + "\r\n$(document).ready(function(){$('#username').attr('autocomplete','off');$('#password').attr('autocomplete','off');});\r\n";
                } else {
                    script = String.valueOf(script) + "<script>\r\n$(document).ready(function(){$('#rememberMePanel').hide();window.dontShowRememberMeOptionOnLoginPage = true;});";
                    script = String.valueOf(script) + "\r\n$(document).ready(function(){$('#username').attr('autocomplete','off');$('#password').attr('autocomplete','off');});\r\n</script>\n";
                }
            }
            if (script.toLowerCase().indexOf("<script>") < 0) {
                script = "<script>" + script + "</script>";
            }
            template = crushftp.handlers.Common.replace_str(template, "<!--##CUSTOMSCRIPT##-->", script);
            template = System.getProperty("crushftp.singleuser", "false").equals("true") ? crushftp.handlers.Common.replace_str(template, "<!--##HEADER##-->", "<br/><center><font color='red' size='+4'><b>Server Maintenance Mode</b></font></center>\r\n" + ServerStatus.SG("login_header")) : crushftp.handlers.Common.replace_str(template, "<!--##HEADER##-->", ServerStatus.SG("login_header"));
            template = crushftp.handlers.Common.replace_str(template, "<!--##FOOTER##-->", ServerStatus.SG("login_footer"));
            if (ServerStatus.BG("hide_email_password")) {
                template = crushftp.handlers.Common.replace_str(template, "forgotPasswordDiv\" style=\"width:250;\"", "forgotPasswordDiv\" style=\"position:absolute;visibility:hidden;\"");
                template = crushftp.handlers.Common.replace_str(template, "<p class=\"lostpassword\">", "<p class=\"lostpassword\" style=\"visibility:hidden;\">");
            }
            if (sessionHTTP.thisSession != null && !sessionHTTP.thisSession.SG("metaTag").equals("metaTag")) {
                template = crushftp.handlers.Common.replace_str(template, "<!-- META -->", sessionHTTP.thisSession.SG("metaTag"));
            }
            template = crushftp.handlers.Common.replace_str(template, "id=\"flashHtmlRow\" style=\"visibility:visible;\"", "id=\"flashHtmlRow\" style=\"position:absolute;left:-5000px;\"");
            template = crushftp.handlers.Common.replace_str(template, "<title>" + System.getProperty("appname", "CrushFTP") + "</title>", "<title>" + ServerStatus.SG("default_title") + "</title>");
            template = crushftp.handlers.Common.replace_str(template, "<title>" + System.getProperty("appname", "CrushFTP") + " WebInterface :: Login</title>", "<title>" + ServerStatus.SG("default_title") + "</title>");
            template = crushftp.handlers.Common.replace_str(template, "<title>WebInterface</title>", "<title>" + ServerStatus.SG("default_title") + "</title>");
            template = crushftp.handlers.Common.replace_str(template, "\"/WebInterface/custom.js\"", "\"" + sessionHTTP.proxy + "WebInterface/custom.js?random=" + System.currentTimeMillis() + "\"");
            if ((template = crushftp.handlers.Common.replace_str(template, "\"/WebInterface/custom.css\"", "\"" + sessionHTTP.proxy + "WebInterface/custom.css?random=" + System.currentTimeMillis() + "\"")).contains("/*RECAPTCHA_VERSION*/")) {
                template = crushftp.handlers.Common.replace_str(template, "/*RECAPTCHA_VERSION*/", sessionHTTP.server_item.getProperty("recaptcha_version", "1"));
            }
            if (sessionHTTP.server_item.getProperty("recaptcha_enabled", "false").equals("true") && (sessionHTTP.server_item.getProperty("recaptcha_version", "2").equals("2") || sessionHTTP.server_item.getProperty("recaptcha_version", "2").equals("3"))) {
                template = crushftp.handlers.Common.replace_str(template, "<script type=\"text/javascript\" src=\"https://www.google.com/recaptcha/api/js/recaptcha_ajax.js\"></script>", "<script type=\"text/javascript\" src=\"https://www.google.com/recaptcha/api.js\"></script>");
                template = crushftp.handlers.Common.replace_str(template, "/*RECAPTCHA*/", "showRecaptcha('recaptcha_div');");
            }
            if (!sessionHTTP.server_item.getProperty("recaptcha_public_key", "").equals("") && (sessionHTTP.server_item.getProperty("recaptcha_version", "2").equals("2") || sessionHTTP.server_item.getProperty("recaptcha_version", "2").equals("3"))) {
                template = crushftp.handlers.Common.replace_str(template, "<script type=\"text/javascript\" src=\"https://www.google.com/recaptcha/api/js/recaptcha_ajax.js\"></script>", "<script type=\"text/javascript\" src=\"https://www.google.com/recaptcha/api.js\"></script>");
                template = crushftp.handlers.Common.replace_str(template, "/*RECAPTCHA_PUBLIC_KEY*/", sessionHTTP.server_item.getProperty("recaptcha_public_key", ""));
            }
            if (sessionHTTP.server_item.getProperty("recaptcha_enabled", "false").equals("false") && sessionHTTP.server_item.getProperty("recaptcha_public_key", "").equals("")) {
                template = crushftp.handlers.Common.replace_str(template, "<script type=\"text/javascript\" src=\"https://www.google.com/recaptcha/api/js/recaptcha_ajax.js\"></script>", "");
            }
            if (template.indexOf("var passwordRule =") >= 0) {
                if (ServerStatus.BG("webinterface_show_password_rule")) {
                    String search_part = template.substring(template.indexOf("var passwordRule ="), template.indexOf(";", template.indexOf("var passwordRule =")));
                    String replace_part = "var passwordRule = {";
                    Properties user = null;
                    user = sessionHTTP != null && sessionHTTP.thisSession != null ? sessionHTTP.thisSession.user : new Properties();
                    Properties password_rules = SessionCrush.build_password_rules(user);
                    replace_part = String.valueOf(replace_part) + "random_password_length:" + password_rules.getProperty("random_password_length") + ",";
                    replace_part = String.valueOf(replace_part) + "min_password_numbers:" + password_rules.getProperty("min_password_numbers") + ",";
                    replace_part = String.valueOf(replace_part) + "min_password_lowers:" + password_rules.getProperty("min_password_lowers") + ",";
                    replace_part = String.valueOf(replace_part) + "min_password_uppers:" + password_rules.getProperty("min_password_uppers") + ",";
                    replace_part = String.valueOf(replace_part) + "min_password_specials:" + password_rules.getProperty("min_password_specials");
                    replace_part = String.valueOf(replace_part) + "}";
                    template = crushftp.handlers.Common.replace_str(template, search_part, replace_part);
                }
            }
            if (sessionHTTP.server_item.getProperty("gsign_enabled", "false").equals("true")) {
                template = crushftp.handlers.Common.replace_str(template, "<!--GSIGNIN_SCRIPT-->", "<script>window.GSignClientID=\"" + sessionHTTP.server_item.getProperty("gsign_client_id", "") + "\";</script>");
            }
            if (sessionHTTP.server_item.getProperty("mssign_enabled", "false").equals("true")) {
                String ms_sing_in_script = "<script type=\"text/javascript\">\n            window.MSSignClientID = \"" + sessionHTTP.server_item.getProperty("mssign_client_id", "") + "\";\n" + "            window.MSTenantID = \"" + sessionHTTP.server_item.getProperty("mssign_tenant_id", "") + "\";\n" + "        </script>";
                template = crushftp.handlers.Common.replace_str(template, "<!--MSSIGNIN_SCRIPT-->", ms_sing_in_script);
            }
            if (sessionHTTP.server_item.getProperty("azureb2c_enabled", "false").equals("true")) {
                String azureb2c_script = "<script type=\"text/javascript\">\n            window.MSSignClientIDB2C = \"" + sessionHTTP.server_item.getProperty("azureb2c_client_id", "") + "\";\n" + "            window.tenantNameB2C = \"" + sessionHTTP.server_item.getProperty("azureb2c_tenant_name", "") + "\";\n" + "            window.userFlowNameB2C = \"" + sessionHTTP.server_item.getProperty("azureb2c_userflow_name", "") + "\";\n" + "        </script>";
                template = crushftp.handlers.Common.replace_str(template, "<!--AZURE_B2C_SINGIN_SCRIPT-->", azureb2c_script);
            }
            if (sessionHTTP.server_item.getProperty("cognito_enabled", "false").equals("true")) {
                String amazon_cognito_script = "<script type=\"text/javascript\">\n            window.AmazonCognitoClientID = \"" + sessionHTTP.server_item.getProperty("cognito_client_id", "") + "\";\n" + "            window.AmazonCognitoDomainPrefix = \"" + sessionHTTP.server_item.getProperty("cognito_domain_prefix", "") + "\";\n" + "        </script>";
                template = crushftp.handlers.Common.replace_str(template, "<!--AMAZON_COGNITO_SINGIN_SCRIPT-->", amazon_cognito_script);
            }
        }
        long checkDate = new Date().getTime();
        try {
            checkDate = Long.parseLong(ifnonematch);
        }
        catch (Exception replace_part) {
            // empty catch block
        }
        sessionHTTP.writeCookieAuth = false;
        if (!file.exists() && tempBytes.size() == 0) {
            sessionHTTP.write_command_http("HTTP/1.1 404 Not Found");
            sessionHTTP.write_standard_headers();
            sessionHTTP.write_command_http("Content-Length: 0");
            sessionHTTP.write_command_http("");
        } else if (checkDate > 0L && checkDate >= lastModified && !file.getName().endsWith(".html")) {
            sessionHTTP.write_command_http("HTTP/1.1 304 Not Modified");
            sessionHTTP.write_standard_headers();
            sessionHTTP.write_command_http("Last-Modified: " + sessionHTTP.sdf_rfc1123.format(new Date(lastModified)));
            sessionHTTP.write_command_http("ETag: " + lastModified);
            sessionHTTP.write_command_http("Content-Length: 0");
            sessionHTTP.write_command_http("");
        } else {
            sessionHTTP.write_command_http("HTTP/1.1 200 OK");
            if (acceptsGZIP) {
                sessionHTTP.write_command_http("Transfer-Encoding: chunked");
            }
            sessionHTTP.write_standard_headers();
            if (!template.equals("")) {
                sessionHTTP.write_command_http("Pragma: no-cache");
            }
            String mime = mimes.getProperty(ext, "");
            if (downloadFilename != null) {
                mime = "application/binary";
                sessionHTTP.write_command_http("Content-Disposition: attachment; filename=\"" + crushftp.handlers.Common.replace_str(crushftp.handlers.Common.url_decode(downloadFilename), "\r", "_") + "\"");
            }
            if (mime.equals("text/html")) {
                sessionHTTP.write_command_http("Content-type: " + mime + "; charset=UTF-8");
            } else {
                sessionHTTP.write_command_http("Content-type: " + mime);
            }
            sessionHTTP.write_command_http("Last-Modified: " + sessionHTTP.sdf_rfc1123.format(new Date(lastModified)));
            sessionHTTP.write_command_http("ETag: " + lastModified);
            sessionHTTP.write_command_http("X-UA-Compatible: chrome=1");
            sessionHTTP.write_command_http("Accept-Ranges: bytes");
            if (acceptsGZIP) {
                sessionHTTP.write_command_http("Vary: Accept-Encoding");
                sessionHTTP.write_command_http("Content-Encoding: gzip");
            } else if (template.length() != 0) {
                sessionHTTP.write_command_http("Content-Length: " + template.getBytes("UTF8").length);
            } else {
                sessionHTTP.write_command_http("Content-Length: " + (tempBytes.size() > 0 ? (long)tempBytes.size() : file.length()));
            }
            sessionHTTP.write_command_http("");
            if (!headerOnly) {
                InputStream in = null;
                try {
                    byte[] rawBytes = new byte[65535];
                    int bytesRead = 0;
                    in = template.length() != 0 ? new ByteArrayInputStream(template.getBytes("UTF8")) : (tempBytes.size() != 0 ? new ByteArrayInputStream(tempBytes.toByteArray()) : new FileInputStream(file));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    OutputStream out = baos;
                    if (acceptsGZIP) {
                        out = new GZIPOutputStream(baos);
                    }
                    while (bytesRead >= 0) {
                        bytesRead = in.read(rawBytes);
                        if (bytesRead > 0) {
                            out.write(rawBytes, 0, bytesRead);
                        } else if (acceptsGZIP) {
                            ((GZIPOutputStream)out).finish();
                        }
                        if (baos.size() <= 0) continue;
                        if (acceptsGZIP) {
                            original_os.write((String.valueOf(Long.toHexString(baos.size())) + "\r\n").getBytes());
                        }
                        baos.writeTo(original_os);
                        if (acceptsGZIP) {
                            original_os.write("\r\n".getBytes());
                        }
                        baos.reset();
                    }
                    in.close();
                    out.close();
                    if (acceptsGZIP) {
                        original_os.write("0\r\n\r\n".getBytes());
                    }
                    original_os.flush();
                }
                catch (Exception e) {
                    Log.log("HTTP_SERVER", 1, e);
                }
                if (in != null) {
                    in.close();
                }
            }
        }
        original_os.flush();
        return null;
    }
}

