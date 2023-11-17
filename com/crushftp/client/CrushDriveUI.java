/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.CrushDrive;
import com.crushftp.client.HTTPD;
import com.crushftp.client.HttpCommandHandler;
import com.crushftp.client.URLConnection;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CrushDriveUI
extends HttpCommandHandler {
    long pong = System.currentTimeMillis();
    static Properties ui_cache = null;
    public static CrushDrive drive = null;

    public static void main(String[] args) {
        System.setProperty("crushftp.worker.v8", "true");
        System.setProperty("com.crushftp.server.httphandler", "com.crushftp.client.CrushDriveUI");
        System.setProperty("crushftp.agent.port", "33334");
        final String[] args2 = args;
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    HTTPD.main(args2);
                }
            });
            System.setProperty("java.net.useSystemProxies", "true");
            Common.trustEverything();
            drive = new CrushDrive();
            if (args != null && args.length > 0) {
                String s = args[0];
                if (!s.replace('\\', '/').endsWith("/")) {
                    s = String.valueOf(s) + "/";
                }
                CrushDrive.base_path = s;
                System.setProperty("crushdrive.prefs", s);
            }
            System.out.println("********************************************************************");
            System.out.println(String.valueOf(CrushDrive.app_name) + " " + CrushDrive.version + " started.");
            System.out.println("********************************************************************");
            CrushDrive.msg("********************************************************************");
            CrushDrive.msg(String.valueOf(CrushDrive.app_name) + " " + CrushDrive.version + " started.");
            CrushDrive.msg("********************************************************************");
            drive.doLoad();
            Thread.sleep(1000L);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        if (CrushDriveUI.drive.prefs.getProperty("drive_auto", "false").equals("true")) {
            drive.doConnect();
        }
    }

    public CrushDriveUI() {
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    Thread.currentThread().setName("CrushDriveUI Web Watcher");
                    while (true) {
                        try {
                            Thread.sleep(1000L);
                        }
                        catch (InterruptedException interruptedException) {
                            // empty catch block
                        }
                        if (System.currentTimeMillis() - CrushDriveUI.this.pong <= 60000L) continue;
                        CrushDrive.msg("Idle timeout, quitting...(" + (System.currentTimeMillis() - CrushDriveUI.this.pong) + " ms)");
                        CrushDriveUI.this.doQuit();
                    }
                }
            });
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    @Override
    public void handleCommand(String path, Properties headers, Properties request, OutputStream out, InputStream in, Properties session, String ip) throws IOException {
        if (path.equals("agent")) {
            String contentType = "text/plain";
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ByteArrayOutputStream tmp = this.getRequest(headers, request, in);
                contentType = this.processRequest(request, baos, session, ip, tmp);
            }
            catch (Exception e) {
                e.printStackTrace();
                baos.write(("" + e).getBytes());
            }
            this.write_command_http("HTTP/1.1 200 OK", out);
            this.write_command_http("Date: " + this.sdf_rfc1123.format(new Date()), out);
            this.write_command_http("Server: CrushHTTPD", out);
            this.write_command_http("P3P: policyref=\"p3p.xml\", CP=\"IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT\"", out);
            this.write_command_http("Keep-Alive: timeout=15, max=20", out);
            this.write_command_http("Content-Type: " + contentType, out);
            this.write_command_http("Connection: Keep-Alive", out);
            this.write_command_http("Last-Modified: " + this.sdf_rfc1123.format(new Date()), out);
            this.write_command_http("ETag: " + System.currentTimeMillis(), out);
            this.write_command_http("Content-Length: " + baos.size(), out);
            this.write_command_http("", out);
            out.write(baos.toByteArray());
        } else {
            if (System.getProperty("com.crushftp.server.httphandler.path", "./clientui/").toUpperCase().startsWith("HTTP") && ui_cache == null) {
                URLConnection urlc = URLConnection.openConnection(new VRL(System.getProperties().remove("com.crushftp.server.httphandler.path") + "WebInterface/CrushDriveUI.zip"), new Properties());
                ZipInputStream zis = new ZipInputStream(urlc.getInputStream());
                ZipEntry entry = zis.getNextEntry();
                while (entry != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        Common.streamCopier(zis, baos, false, false, true);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (ui_cache == null) {
                        ui_cache = new Properties();
                    }
                    ui_cache.put(entry.getName(), baos);
                    entry = zis.getNextEntry();
                }
                zis.close();
                urlc.disconnect();
            }
            ByteArrayOutputStream f_b = new ByteArrayOutputStream();
            File file = null;
            URL f = null;
            if (ui_cache != null) {
                f = new URL("file://clientui/" + path);
                f_b = (ByteArrayOutputStream)ui_cache.get("clientui/" + path);
            } else {
                file = new File(String.valueOf(System.getProperty("com.crushftp.server.httphandler.path", "./clientui/")) + path);
                if (file.exists()) {
                    f = file.toURI().toURL();
                    try {
                        Common.streamCopier(new FileInputStream(file), f_b, false, true, true);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (ui_cache == null) {
                    path = Common.replace_str(path, "crushdrive", "CrushDrive");
                    f = this.getClass().getResource("/clientui/" + path);
                    if (f != null) {
                        try {
                            Common.streamCopier(this.getClass().getResourceAsStream("/clientui/" + path), f_b, false, true, true);
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (f != null) {
                this.write_command_http("HTTP/1.1 200 OK", out);
            } else {
                this.write_command_http("HTTP/1.1 404 Not found", out);
            }
            this.write_command_http("Date: " + this.sdf_rfc1123.format(new Date()), out);
            this.write_command_http("Server: CrushHTTPD", out);
            this.write_command_http("P3P: policyref=\"p3p.xml\", CP=\"IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT\"", out);
            this.write_command_http("Keep-Alive: timeout=15, max=20", out);
            this.write_command_http("Content-Type: " + Common.getContentType(Common.last(f.getPath())), out);
            this.write_command_http("Connection: Keep-Alive", out);
            this.write_command_http("Last-Modified: " + this.sdf_rfc1123.format(new Date()), out);
            this.write_command_http("ETag: " + System.currentTimeMillis(), out);
            this.write_command_http("Content-Length: " + f_b.size(), out);
            this.write_command_http("", out);
            if (f != null) {
                out.write(f_b.toByteArray());
            }
        }
        out.flush();
    }

    @Override
    public String processRequest(Properties request, ByteArrayOutputStream baos, Properties session, String ip, ByteArrayOutputStream tmp) throws Exception {
        this.pong = System.currentTimeMillis();
        String contentType = "text/html";
        if (request.getProperty("command", "").equals("ping")) {
            baos.write(System.getProperty("crushdrive.temp_agent", "false").getBytes("UTF8"));
            baos.write("\r\n".getBytes());
        } else if (request.getProperty("command", "").equals("status")) {
            Properties p2 = new Properties();
            p2.put("connected", String.valueOf(CrushDriveUI.drive.connected));
            p2.put("version", String.valueOf(CrushDrive.version));
            baos.write(Common.getXMLString(p2, "drive").getBytes("UTF8"));
            contentType = "text/xml";
            if (new File(String.valueOf(CrushDrive.base_path) + "debug").exists()) {
                CrushDrive.msg(String.valueOf(request.getProperty("command", "")) + ":" + CrushDriveUI.drive.connected);
            }
        } else {
            CrushDrive.msg(request.getProperty("command", ""));
            if (request.getProperty("command", "").equals("encrypt_decrypt")) {
                String s = request.getProperty("pass");
                s = VRL.vrlDecode(s);
                s = Common.encryptDecrypt(s, request.getProperty("encrypt").equals("true"));
                if (request.getProperty("encrypt").equals("false")) {
                    s = VRL.vrlEncode(s);
                }
                baos.write(s.getBytes());
            } else if (request.getProperty("command", "").equals("connect")) {
                String drive_password = Common.encryptDecrypt(CrushDriveUI.drive.prefs.getProperty("drive_password"), false);
                if (drive_password.equals("")) {
                    drive_password = Common.encryptDecrypt(request.getProperty("drive_password"), false);
                }
                String s = drive.doConnect(CrushDriveUI.drive.prefs.getProperty("base_url"), CrushDriveUI.drive.prefs.getProperty("drive_username"), drive_password, CrushDriveUI.drive.prefs.getProperty("drive_letter"), CrushDriveUI.drive.prefs.getProperty("buffered", "false").equals("true"));
                baos.write(s.getBytes());
            } else if (request.getProperty("command", "").equals("disconnect")) {
                String s = drive.doDisconnect();
                baos.write(s.getBytes());
            } else if (request.getProperty("command", "").equals("get_letters")) {
                baos.write(drive.getLetters().getBytes("UTF8"));
            } else if (request.getProperty("command", "").equals("quit")) {
                this.doQuit();
            } else if (request.getProperty("command", "").equals("load_prefs")) {
                CrushDriveUI.drive.prefs = CrushDrive.loadPrefs();
                Properties prefs2 = (Properties)CrushDriveUI.drive.prefs.clone();
                baos.write(Common.getXMLString(prefs2, "prefs").getBytes("UTF8"));
                contentType = "text/xml";
            } else if (request.getProperty("command", "").equals("get_log_home")) {
                baos.write(new File(CrushDrive.base_path).getCanonicalPath().getBytes("UTF-8"));
                baos.write("\r\n".getBytes());
            } else if (request.getProperty("command", "").equals("save_prefs")) {
                String s = request.getProperty("prefs").replace('+', ' ');
                s = Common.replace_str(s, "%26", "&amp;");
                s = Common.replace_str(s, "%3C", "&lt;");
                s = Common.replace_str(s, "%3E", "&gt;");
                Properties prefs2 = (Properties)Common.readXMLObject(new ByteArrayInputStream(Common.url_decode(s).getBytes("UTF8")));
                if (CrushDriveUI.drive.prefs == null) {
                    CrushDriveUI.drive.prefs = new Properties();
                }
                CrushDriveUI.drive.prefs.putAll((Map<?, ?>)prefs2);
                if (!CrushDriveUI.drive.prefs.getProperty("save_password", "true").equals("true")) {
                    CrushDriveUI.drive.prefs.put("drive_password", "");
                }
                CrushDrive.savePrefs(CrushDriveUI.drive.prefs);
            }
        }
        return contentType;
    }

    public void doQuit() {
        CrushDrive.msg("Starting quit...");
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        int x = 0;
                        while (x < 10) {
                            Thread.sleep(1000L);
                            CrushDrive.msg("Doing forced shutdown in " + (10 - x) + " secs if we don't close sooner.");
                            ++x;
                        }
                    }
                    catch (InterruptedException interruptedException) {
                        // empty catch block
                    }
                    System.exit(0);
                }
            });
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        int x = 0;
                        while (x < 10) {
                            Thread.sleep(1000L);
                            drive.flushLog();
                            ++x;
                        }
                    }
                    catch (InterruptedException interruptedException) {
                        // empty catch block
                    }
                    System.exit(0);
                }
            });
            drive.doDisconnect();
        }
        catch (Exception exception) {
            // empty catch block
        }
        HTTPD.shutdown();
        try {
            drive.flushLog();
            Thread.sleep(1000L);
            drive.flushLog();
            Thread.sleep(1000L);
            drive.flushLog();
            Thread.sleep(1000L);
            drive.flushLog();
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
        System.exit(0);
    }
}

