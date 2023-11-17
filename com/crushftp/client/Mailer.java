/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.activation.DataHandler
 *  javax.activation.DataSource
 *  javax.activation.FileDataSource
 *  javax.activation.FileTypeMap
 *  javax.activation.MimetypesFileTypeMap
 *  javax.mail.Address
 *  javax.mail.Authenticator
 *  javax.mail.BodyPart
 *  javax.mail.Message
 *  javax.mail.Multipart
 *  javax.mail.Session
 *  javax.mail.internet.InternetAddress
 *  javax.mail.internet.MimeBodyPart
 *  javax.mail.internet.MimeMessage
 *  javax.mail.internet.MimeMessage$RecipientType
 *  javax.mail.internet.MimeMultipart
 *  javax.mail.internet.MimeUtility
 */
package com.crushftp.client;

import com.crushftp.client.ClientDataSource;
import com.crushftp.client.Common;
import com.crushftp.client.CrushOAuth2Provider;
import com.crushftp.client.File_B;
import com.crushftp.client.UnsafeSSLSocketFactory;
import com.crushftp.client.VRL;
import java.io.File;
import java.lang.reflect.Method;
import java.security.Security;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.net.ssl.SSLContext;

public class Mailer {
    public static Object used_lock = new Object();

    public static String send_mail(String server_ip, String to_user, String cc_user, String bcc_user, String from_user, String reply_to_user, String subject, String body, String smtp_server, String smtp_user, String smtp_pass, boolean smtp_ssl, boolean html, File_B[] attachments) {
        return Mailer.send_mail(server_ip, to_user, cc_user, bcc_user, from_user, reply_to_user, subject, body, smtp_server, smtp_user, smtp_pass, smtp_ssl, html, attachments, new Vector());
    }

    public static String send_mail(String server_ip, String to_user, String cc_user, String bcc_user, String from_user, String reply_to_user, String subject, String body, String smtp_server, String smtp_user, String smtp_pass, boolean smtp_ssl, boolean html, File_B[] attachments, Vector fileMimeTypes) {
        return Mailer.send_mail(server_ip, to_user, cc_user, bcc_user, from_user, reply_to_user, subject, body, smtp_server, smtp_user, smtp_pass, smtp_ssl, html, attachments, new Vector(), new Vector());
    }

    public static String send_mail(String server_ip, String to_user, String cc_user, String bcc_user, String from_user, String reply_to_user, String subject, String body, String smtp_server, String smtp_user, String smtp_pass, boolean smtp_ssl, boolean html, File_B[] attachments, Vector fileMimeTypes, Vector remoteFiles) {
        if (smtp_server.indexOf(",") < 0) {
            return Mailer.send_mail_single(server_ip, to_user, cc_user, bcc_user, from_user, reply_to_user, subject, body, smtp_server, smtp_user, smtp_pass, smtp_ssl, html, attachments, fileMimeTypes, remoteFiles);
        }
        String result = "ERROR:SMTP failed!";
        int x = 0;
        while (x < smtp_server.trim().split(",").length) {
            result = Mailer.send_mail_single(server_ip, to_user, cc_user, bcc_user, from_user, reply_to_user, subject, body, smtp_server.trim().split(",")[x].trim(), smtp_user, smtp_pass, smtp_ssl, html, attachments, fileMimeTypes, remoteFiles);
            if (result.indexOf("Success!") >= 0) {
                return result;
            }
            ++x;
        }
        return result;
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static String send_mail_single(String server_ip, String to_user, String cc_user, String bcc_user, String from_user, String reply_to_user, String subject, String body, String smtp_server, String smtp_user, String smtp_pass, boolean smtp_ssl, boolean html, File_B[] attachments, Vector fileMimeTypes, Vector remoteFiles) {
        try {
            smtp_pass = Common.encryptDecrypt(smtp_pass, false);
        }
        catch (Exception e) {
            Common.log("SMTP", 0, e);
        }
        plain_body = body;
        if (html && (body.indexOf("\r") >= 0 || body.indexOf("\n") >= 0) && body.toUpperCase().indexOf("<BR") < 0 && body.toUpperCase().indexOf("<SPAN") < 0) {
            body = Common.replace_str(body, "\r\n", "<br/>");
            body = Common.replace_str(body, "\r", "<br/>");
            if (!(body = Common.replace_str(body, "\n", "<br/>")).toUpperCase().startsWith("<HTML>") && body.toUpperCase().indexOf("<BODY") < 0) {
                body = "<HTML><BODY>" + body + "</BODY></HTML>";
            } else if (!body.toUpperCase().startsWith("<HTML>")) {
                body = "<HTML>" + body + "</BODY></HTML>";
            }
        }
        if (html && body.indexOf("<") >= 0 && body.toUpperCase().indexOf("<HTML") < 0) {
            body = "<html><body>" + body + "</body></html>";
        }
        resultMessage = "";
        smtp_port = 25;
        if (smtp_server.indexOf(":") >= 0) {
            try {
                smtp_port = Integer.parseInt(smtp_server.substring(smtp_server.indexOf(":") + 1));
            }
            catch (Exception var19_20) {
                // empty catch block
            }
            smtp_server = smtp_server.substring(0, smtp_server.indexOf(":"));
        }
        xoauth2 = false;
        if (smtp_server.endsWith("gmail.com") != false && smtp_user.contains("~") != false || smtp_server.endsWith("office365.com") != false && smtp_user.contains("~") != false || smtp_server.endsWith("outlook.com") && smtp_user.contains("~")) {
            xoauth2 = true;
        }
        client_id = "";
        client_secret = "";
        refresh_token = "";
        tenant = "";
        is_goolgle = smtp_server.endsWith("gmail.com");
        if (xoauth2) {
            if (Security.getProvider(String.valueOf(System.getProperty("appname", "CrushFTP")) + "_SASL_OAuth2") == null) {
                Security.addProvider(new CrushOAuth2Provider());
            }
            try {
                if (is_goolgle) {
                    refresh_token = smtp_pass;
                    client_id = smtp_user.split("~")[smtp_user.split("~").length - 2].trim();
                    if (!client_id.equals("google_jwt")) {
                        client_secret = Common.encryptDecrypt(smtp_user.split("~")[smtp_user.split("~").length - 1].trim(), false);
                    }
                    smtp_user = from_user;
                } else {
                    client_id = smtp_user.split("~")[0];
                    client_secret = Common.encryptDecrypt(smtp_user.split("~")[1].trim(), false);
                    if (smtp_user.split("~").length > 2) {
                        tenant = smtp_user.split("~")[2].trim();
                    }
                    refresh_token = smtp_pass;
                    smtp_user = from_user;
                }
            }
            catch (Exception e) {
                Common.log("SMTP", 1, e);
            }
            if (refresh_token.equals("")) {
                new Exception("Error : Refresh token is required!");
            }
            if (client_id.equals("")) {
                new Exception("Error : Client id is required!");
            }
            if (client_secret.equals("")) {
                new Exception("Error : Client secret is required!");
            }
            smtp_pass = "";
        }
        high_priority = false;
        if (subject.indexOf("{high_priority}") >= 0) {
            subject = Common.replace_str(subject, "{high_priority}", "");
            high_priority = true;
        }
        loops = 0;
        while (loops < 5) {
            block117: {
                block123: {
                    block122: {
                        block124: {
                            block120: {
                                block121: {
                                    block119: {
                                        resultMessage = "";
                                        transport = null;
                                        try {
                                            block115: {
                                                toList = new Vector<E>();
                                                ccList = new Vector<E>();
                                                bccList = new Vector<E>();
                                                if (to_user != null && !to_user.trim().equals("")) {
                                                    Mailer.emailParser(to_user, toList);
                                                }
                                                if (cc_user != null && !cc_user.trim().equals("")) {
                                                    Mailer.emailParser(cc_user, ccList);
                                                }
                                                if (bcc_user != null && !bcc_user.trim().equals("")) {
                                                    Mailer.emailParser(bcc_user, bccList);
                                                }
                                                props = (Properties)System.getProperties().clone();
                                                props.put("mail.transport.protocol", "smtp");
                                                props.put("mail.smtp.host", smtp_server);
                                                props.put("mail.smtp.port", String.valueOf(smtp_port));
                                                props.put("mail.smtp.auth", "false");
                                                props.put("mail.smtp.ssl.protocols", System.getProperty("crushftp.tls_version_client", "SSLV2Hello,SSLv3,TLSv1,TLSv1.1,TLSv1.2,TLSv1.3").replace(',', ' '));
                                                if (Integer.parseInt(System.getProperty("crushftp.log_debug_level", "0")) >= 2) {
                                                    props.put("mail.debug", "true");
                                                }
                                                props.put("mail.smtp.ssl.socketFactory", new UnsafeSSLSocketFactory());
                                                if (!System.getProperty("crushftp.smtp_helo_ip", "").equals("")) {
                                                    props.put("mail.smtp.localhost", System.getProperty("crushftp.smtp_helo_ip", ""));
                                                }
                                                if (smtp_ssl) {
                                                    props.put("mail.smtp.starttls.required", "true");
                                                } else {
                                                    props.put("mail.smtp.starttls.required", "false");
                                                }
                                                allow_tls = true;
                                                if (System.getProperty("crushftp.smtp_start_tls_allowed", "true").equals("false")) {
                                                    allow_tls = false;
                                                }
                                                if (System.getProperty("crushftp.smtp_tls", "true").equals("false")) {
                                                    allow_tls = false;
                                                }
                                                props.put("mail.smtp.starttls.enable", String.valueOf(allow_tls));
                                                Common.log("SMTP", 2, "SMTP STARTTLS ENABLED:" + allow_tls);
                                                if (smtp_port == 465) {
                                                    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                                                }
                                                props.put("mail.smtp.socketFactory.port", String.valueOf(smtp_port));
                                                props.put("mail.smtp.sasl.enable", System.getProperty("crushftp.smtp.sasl", "false"));
                                                if (xoauth2) {
                                                    props.put("mail.smtp.sasl.enable", "true");
                                                    props.put("mail.smtp.sasl.mechanisms", "XOAUTH2");
                                                }
                                                session = null;
                                                if (!smtp_user.trim().equals("") && !xoauth2) {
                                                    props.put("mail.smtp.auth", "true");
                                                    auth = new SMTPAuthenticator(smtp_user, smtp_pass);
                                                    try {
                                                        session = Session.getInstance((Properties)props, (Authenticator)auth);
                                                        transport = session.getTransport("smtp");
                                                        if (smtp_user.trim().equals("")) {
                                                            transport.connect();
                                                        }
                                                        transport.connect(smtp_user, smtp_pass);
                                                    }
                                                    catch (Exception e) {
                                                        Common.log("SMTP", 1, e);
                                                        try {
                                                            transport.close();
                                                        }
                                                        catch (Exception ee) {
                                                            Common.log("SMTP", 1, "Connection close : " + ee);
                                                        }
                                                        if (("" + e).contains("SSLHandshakeException")) {
                                                            try {
                                                                Common.log("SMTP", 1, "Retry: Default SSL parameters.");
                                                                props.put("mail.smtp.ssl.protocols", SSLContext.getDefault().getSupportedSSLParameters().getProtocols());
                                                                session = Session.getInstance((Properties)props, (Authenticator)auth);
                                                                if (smtp_user.trim().equals("")) {
                                                                    transport.connect();
                                                                } else {
                                                                    transport.connect(smtp_user, smtp_pass);
                                                                }
                                                            }
                                                            catch (Exception ee) {
                                                                Common.log("SMTP", 1, ee);
                                                            }
                                                        }
                                                        if (transport == null || transport.isConnected()) ** GOTO lbl218
                                                        throw e;
                                                    }
                                                } else {
                                                    if (xoauth2) {
                                                        token = new Properties();
                                                        if (!is_goolgle) {
                                                            if (tenant.equals("")) {
                                                                tenant = "common";
                                                            }
                                                            token = Common.oauth_renew_tokens(refresh_token, client_id, client_secret, "https://login.microsoftonline.com/" + tenant + "/oauth2/v2.0/token");
                                                            smtp_pass = token.getProperty("access_token", "");
                                                        } else {
                                                            token = client_id.equals("google_jwt") == false ? Common.oauth_renew_tokens(refresh_token, client_id, client_secret, "https://oauth2.googleapis.com/token") : Common.oauth_renew_tokens(refresh_token, String.valueOf(smtp_user) + "~" + client_id + "~" + "https://mail.google.com", client_secret, "https://oauth2.googleapis.com/token");
                                                        }
                                                        props.put("mail.sasl.mechanisms.oauth2.oauthToken", token.getProperty("access_token", ""));
                                                        if (!token.getProperty("access_token", "").equals("")) {
                                                            Common.log("SMTP", 2, "XOAUTH : Got the access token.");
                                                        }
                                                    }
                                                    try {
                                                        session = Session.getInstance((Properties)props, null);
                                                        transport = session.getTransport("smtp");
                                                        if (smtp_user.trim().equals("")) {
                                                            transport.connect();
                                                        } else {
                                                            transport.connect(smtp_user, smtp_pass);
                                                        }
                                                    }
                                                    catch (Exception e) {
                                                        Common.log("SMTP", 1, e);
                                                        try {
                                                            transport.close();
                                                        }
                                                        catch (Exception ee) {
                                                            Common.log("SMTP", 1, "Connection close : " + ee);
                                                        }
                                                        if (("" + e).contains("SSLHandshakeException") || ("" + e).contains("MessagingException")) {
                                                            try {
                                                                Common.log("SMTP", 1, "Retry: Default SSL parameters.");
                                                                props.put("mail.smtp.ssl.protocols", SSLContext.getDefault().getSupportedSSLParameters().getProtocols());
                                                                session = Session.getInstance((Properties)props, null);
                                                                if (smtp_user.trim().equals("")) {
                                                                    transport.connect();
                                                                } else {
                                                                    transport.connect(smtp_user, smtp_pass);
                                                                }
                                                            }
                                                            catch (Exception ee) {
                                                                Common.log("SMTP", 1, ee);
                                                            }
                                                        }
                                                        if (transport == null || transport.isConnected()) break block115;
                                                        throw e;
                                                    }
                                                }
                                            }
                                            msg = new MimeMessage(session);
                                            to = new InternetAddress[toList.size()];
                                            x = 0;
                                            while (true) {
                                                block118: {
                                                    if (x < toList.size()) break block118;
                                                    cc = new InternetAddress[ccList.size()];
                                                    x = 0;
                                                    if (true) ** GOTO lbl252
                                                }
                                                if (!toList.elementAt(x).toString().trim().equals("")) {
                                                    to[x] = new InternetAddress(toList.elementAt(x).toString().trim());
                                                }
                                                ++x;
                                            }
                                        }
                                        catch (Exception e) {
                                            if (loops >= 4) {
                                                e.printStackTrace();
                                                Common.log("SMTP", 1, e);
                                                Common.log("SMTP", 1, "to:" + to_user);
                                                Common.log("SMTP", 1, "from:" + from_user);
                                                Common.log("SMTP", 1, "subject:" + subject);
                                                Common.log("SMTP", 1, "body:" + body);
                                                resultMessage = String.valueOf(resultMessage) + "Server:" + smtp_server + "\r\n" + "Port" + ":" + smtp_port + "\r\n" + "User" + ":" + smtp_user + "\r\n" + "Error" + ":" + e;
                                                return resultMessage;
                                            }
                                        }
                                        do {
                                            if (!ccList.elementAt(x).toString().trim().equals("")) {
                                                cc[x] = new InternetAddress(ccList.elementAt(x).toString().trim());
                                            }
                                            ++x;
lbl252:
                                            // 2 sources

                                        } while (x < ccList.size());
                                        bcc = new InternetAddress[bccList.size()];
                                        x = 0;
                                        while (x < bccList.size()) {
                                            if (!bccList.elementAt(x).toString().trim().equals("")) {
                                                bcc[x] = new InternetAddress(bccList.elementAt(x).toString().trim());
                                            }
                                            ++x;
                                        }
                                        from = null;
                                        from = from_user.indexOf("<") >= 0 && from_user.indexOf(">") >= 0 && from_user.indexOf(">") >= from_user.indexOf("<") ? new InternetAddress(Mailer.emailStripperOnly(from_user.trim()), from_user.substring(0, from_user.indexOf("<")).trim()) : new InternetAddress(Mailer.emailStripper(from_user.trim()));
                                        reply_to = null;
                                        if (reply_to_user != null && !reply_to_user.trim().equals("")) {
                                            reply_to = reply_to_user.indexOf("<") >= 0 && reply_to_user.indexOf(">") >= 0 && reply_to_user.indexOf(">") >= reply_to_user.indexOf("<") ? new InternetAddress(Mailer.emailStripperOnly(reply_to_user.trim()), reply_to_user.substring(0, reply_to_user.indexOf("<")).trim()) : new InternetAddress(Mailer.emailStripper(reply_to_user.trim()));
                                        }
                                        if (!html) break block119;
                                        textpart = new MimeBodyPart();
                                        textpart.setText(plain_body, "UTF-8");
                                        textpart.addHeaderLine("Content-Type: text/plain; charset=\"UTF-8\"");
                                        textpart.addHeaderLine("Content-Transfer-Encoding: quoted-printable");
                                        htmlpart = new MimeBodyPart();
                                        htmlpart.setText(body, "UTF-8");
                                        htmlpart.addHeaderLine("Content-Type: text/html; charset=\"UTF-8\"");
                                        htmlpart.addHeaderLine("Content-Transfer-Encoding: quoted-printable");
                                        moreItems = new Vector<MimeBodyPart>();
                                        if (attachments == null) break block120;
                                        x = 0;
                                        if (true) ** GOTO lbl300
                                    }
                                    if (attachments == null && (remoteFiles == null || remoteFiles.size() <= 0)) break block121;
                                    messageBodyPart = new MimeBodyPart();
                                    messageBodyPart.setText(body);
                                    multipart = new MimeMultipart();
                                    multipart.addBodyPart((BodyPart)messageBodyPart);
                                    if (attachments == null) break block122;
                                    x = 0;
                                    if (true) ** GOTO lbl345
                                }
                                msg.setText(body, "UTF-8");
                                break block123;
                                do {
                                    if (attachments[x] != null && !(attachment = attachments[x]).isDirectory()) {
                                        messageBodyPart = new MimeBodyPart();
                                        source = new FileDataSource((File)attachment);
                                        if (fileMimeTypes != null && fileMimeTypes.size() > 0) {
                                            source.setFileTypeMap((FileTypeMap)Mailer.getCustomMimetypesFileTypeMap(fileMimeTypes));
                                        }
                                        messageBodyPart.setDataHandler(new DataHandler((DataSource)source));
                                        messageBodyPart.setFileName(attachment.getName());
                                        moreItems.addElement(messageBodyPart);
                                    }
                                    ++x;
lbl300:
                                    // 2 sources

                                } while (x < attachments.length);
                            }
                            if (remoteFiles != null && remoteFiles.size() > 0) {
                                x = 0;
                                while (x < remoteFiles.size()) {
                                    p = (Properties)remoteFiles.get(x);
                                    messageBodyPart = new MimeBodyPart();
                                    if (p.get("vrl") != null && p.get("prefs") != null) {
                                        source = new ClientDataSource((VRL)p.get("vrl"), (Properties)p.get("prefs"));
                                        p.put("dataSource", source);
                                        if (fileMimeTypes != null && fileMimeTypes.size() > 0) {
                                            source.setFileTypeMap(Mailer.getCustomMimetypesFileTypeMap(fileMimeTypes));
                                        }
                                        messageBodyPart.setDataHandler(new DataHandler((DataSource)source));
                                        messageBodyPart.setFileName(((VRL)p.get("vrl")).getName());
                                        moreItems.addElement(messageBodyPart);
                                    }
                                    ++x;
                                }
                            }
                            if (moreItems.size() <= 0) break block124;
                            mp2 = new MimeMultipart("mixed");
                            mp2.addBodyPart((BodyPart)htmlpart);
                            x = 0;
                            if (true) ** GOTO lbl332
                        }
                        mp2 = new MimeMultipart("alternative");
                        mp2.addBodyPart((BodyPart)textpart);
                        mp2.addBodyPart((BodyPart)htmlpart);
                        msg.setContent((Multipart)mp2);
                        break block123;
                        do {
                            mp2.addBodyPart((BodyPart)moreItems.elementAt(x));
                            ++x;
lbl332:
                            // 2 sources

                        } while (x < moreItems.size());
                        msg.setContent((Multipart)mp2);
                        break block123;
                        do {
                            if (attachments[x] != null && !(attachment = attachments[x]).isDirectory()) {
                                messageBodyPart = new MimeBodyPart();
                                source = new FileDataSource((File)attachment);
                                if (fileMimeTypes != null && fileMimeTypes.size() > 0) {
                                    source.setFileTypeMap((FileTypeMap)Mailer.getCustomMimetypesFileTypeMap(fileMimeTypes));
                                }
                                messageBodyPart.setDataHandler(new DataHandler((DataSource)source));
                                messageBodyPart.setFileName(attachment.getName());
                                multipart.addBodyPart((BodyPart)messageBodyPart);
                            }
                            ++x;
lbl345:
                            // 2 sources

                        } while (x < attachments.length);
                    }
                    x = 0;
                    while (remoteFiles != null && x < remoteFiles.size()) {
                        p = (Properties)remoteFiles.get(x);
                        messageBodyPart = new MimeBodyPart();
                        if (p.get("vrl") != null && p.get("prefs") != null) {
                            source = new ClientDataSource((VRL)p.get("vrl"), (Properties)p.get("prefs"));
                            p.put("dataSource", source);
                            if (fileMimeTypes != null && fileMimeTypes.size() > 0) {
                                source.setFileTypeMap(Mailer.getCustomMimetypesFileTypeMap(fileMimeTypes));
                            }
                            messageBodyPart.setDataHandler(new DataHandler((DataSource)source));
                            messageBodyPart.setFileName(((VRL)p.get("vrl")).getName());
                            multipart.addBodyPart((BodyPart)messageBodyPart);
                        }
                        ++x;
                    }
                    msg.setContent((Multipart)multipart);
                }
                msg.setFrom((Address)from);
                if (reply_to != null) {
                    msg.setReplyTo(new Address[]{reply_to});
                }
                if (to.length > 0) {
                    msg.setRecipients(MimeMessage.RecipientType.TO, (Address[])to);
                }
                if (cc.length > 0) {
                    msg.setRecipients(MimeMessage.RecipientType.CC, (Address[])cc);
                }
                if (bcc.length > 0) {
                    msg.setRecipients(MimeMessage.RecipientType.BCC, (Address[])bcc);
                }
                if (System.getProperty("crushftp.smtp_subject_utf8", "false").equals("true")) {
                    msg.setSubject(subject, "UTF-8");
                } else if (System.getProperty("crushftp.smtp_subject_encoded", "false").equals("true")) {
                    msg.setSubject(MimeUtility.encodeText((String)subject, (String)"UTF-8", (String)"B"));
                } else {
                    msg.setSubject(subject);
                }
                if (high_priority) {
                    msg.setHeader("X-Priority", "1");
                    msg.setHeader("x-msmail-priority", "high");
                }
                msg.setSentDate(new Date());
                msg.saveChanges();
                transport.sendMessage((Message)msg, msg.getAllRecipients());
                return "Success!";
                finally {
                    try {
                        transport.close();
                    }
                    catch (Exception x) {}
                    if (remoteFiles == null || remoteFiles.size() <= 0) break block117;
                    x = 0;
                    ** while (x < remoteFiles.size())
                }
lbl-1000:
                // 1 sources

                {
                    p = (Properties)remoteFiles.get(x);
                    if (p.get("dataSource") != null) {
                        try {
                            ((ClientDataSource)p.get("dataSource")).logout();
                        }
                        catch (Exception e) {
                            Common.log("SMTP", 1, e);
                        }
                    }
                    ++x;
                    continue;
                }
            }
            ++loops;
        }
        Common.log("SMTP", 2, "to:" + to_user);
        Common.log("SMTP", 2, "from:" + from_user);
        Common.log("SMTP", 2, "subject:" + subject);
        Common.log("SMTP", 2, "body:" + body);
        return resultMessage;
    }

    public static void emailParser(String emails, Vector retList) {
        try {
            emails = Common.replace_str(emails, ";", ",");
            String[] items = emails.split(",");
            int x = 0;
            while (x < items.length) {
                block12: {
                    try {
                        if (items[x].toUpperCase().indexOf("@G.") >= 0 && items[x].toUpperCase().indexOf("<") < 0 && items[x].toUpperCase().indexOf(">") < 0) {
                            String serverGroup = Common.dots(items[x].substring(items[x].indexOf("@G.") + 3).trim());
                            Common.log("SMTP", 2, "Mailer:Looking up server group:" + serverGroup);
                            Class<?> ut = Class.forName("crushftp.handlers.UserTools");
                            Method getGroups = ut.getDeclaredMethod("getGroups", String.class);
                            Properties groups = (Properties)getGroups.invoke(null, serverGroup);
                            if (groups != null) {
                                Common.log("SMTP", 2, "Mailer:Got groups:" + groups.size());
                                Vector group = (Vector)groups.get(items[x].substring(0, items[x].toUpperCase().indexOf("@G.")).trim());
                                if (group != null) {
                                    Common.log("SMTP", 2, "Mailer:Got group:" + group.size());
                                    int xx = 0;
                                    while (xx < group.size()) {
                                        String tmp_email;
                                        String username = group.elementAt(xx).toString();
                                        Common.log("SMTP", 2, "Mailer:Finding user:" + username);
                                        Method getUser = ut.getDeclaredMethod("getUser", String.class, String.class, Boolean.class);
                                        Properties user = (Properties)getUser.invoke(null, serverGroup, username, new Boolean(true));
                                        if (user != null && !user.getProperty("email", "").equals("") && !(tmp_email = Mailer.emailStripper(user.getProperty("email", ""))).trim().equals("")) {
                                            retList.addElement(tmp_email);
                                        }
                                        ++xx;
                                    }
                                }
                            }
                        } else {
                            String tmp_email = Mailer.emailStripper(items[x]);
                            if (!tmp_email.trim().equals("")) {
                                retList.addElement(tmp_email);
                            }
                        }
                    }
                    catch (Throwable e) {
                        Common.log("SMTP", 1, e);
                        String tmp_email = Mailer.emailStripper(items[x]);
                        if (tmp_email.trim().equals("")) break block12;
                        retList.addElement(tmp_email);
                    }
                }
                ++x;
            }
        }
        catch (Exception e) {
            Common.log("SMTP", 1, e);
        }
    }

    public static String emailStripper(String email) {
        if (email.indexOf("<") >= 0 && email.indexOf(">") >= 0 && email.indexOf(">") >= email.indexOf("<")) {
            email = email.replace(',', ' ');
        }
        return email;
    }

    public static String emailStripperOnly(String email) {
        if (email.indexOf("<") >= 0 && email.indexOf(">") >= 0 && email.indexOf(">") >= email.indexOf("<")) {
            email = email.substring(email.indexOf("<") + 1, email.indexOf(">"));
        }
        return email;
    }

    private static MimetypesFileTypeMap getCustomMimetypesFileTypeMap(Vector v) {
        MimetypesFileTypeMap mimeFileTypes = (MimetypesFileTypeMap)FileTypeMap.getDefaultFileTypeMap();
        int x = 0;
        while (x < v.size()) {
            mimeFileTypes.addMimeTypes((String)v.get(x));
            ++x;
        }
        return mimeFileTypes;
    }
}

