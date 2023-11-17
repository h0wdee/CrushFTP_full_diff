/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.CrushSyncDaemon;
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

public class CrushSyncUI
extends HttpCommandHandler {
    public static long pong = System.currentTimeMillis();
    static Properties ui_cache = null;
    public static CrushSyncDaemon sync = null;
    public static boolean ignore_quit = false;

    public static void main(String[] args) {
        System.setProperty("crushftp.worker.v8", "true");
        System.setProperty("com.crushftp.server.httphandler", "com.crushftp.client.CrushSyncUI");
        System.setProperty("crushftp.agent.port", "33335");
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
            String base_path = "./";
            if (args != null && args.length > 0 && !(base_path = args[0]).replace('\\', '/').endsWith("/")) {
                base_path = String.valueOf(base_path) + "/";
            }
            sync = new CrushSyncDaemon(base_path);
            System.out.println("********************************************************************");
            System.out.println(String.valueOf(CrushSyncDaemon.app_name) + " " + "3.12.17" + " started.");
            System.out.println("********************************************************************");
            CrushSyncDaemon.msg("********************************************************************");
            CrushSyncDaemon.msg(String.valueOf(CrushSyncDaemon.app_name) + " " + "3.12.17" + " started.");
            CrushSyncDaemon.msg("********************************************************************");
            if (args != null && args.length > 1 && args[1].equalsIgnoreCase("-D")) {
                CrushSyncDaemon.msg(sync.doStartup());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            CrushSyncDaemon.msg("ERROR:" + e);
            CrushSyncDaemon.msg(e);
            CrushSyncDaemon.msg("Quitting!");
            System.exit(1);
        }
    }

    public CrushSyncUI() {
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    Thread.currentThread().setName("CrushSyncUI Web Watcher");
                    while (true) {
                        try {
                            Thread.sleep(1000L);
                        }
                        catch (InterruptedException interruptedException) {
                            // empty catch block
                        }
                        if (System.currentTimeMillis() - pong <= 60000L) continue;
                        CrushSyncDaemon.msg("Idle timeout, quitting...(" + (System.currentTimeMillis() - pong) + " ms)");
                        CrushSyncUI.this.doQuit();
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
                CrushSyncDaemon.msg(e);
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
                URLConnection urlc = URLConnection.openConnection(new VRL(System.getProperties().remove("com.crushftp.server.httphandler.path") + "WebInterface/CrushSyncUI.zip"), new Properties());
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
                    path = Common.replace_str(path, "crushsync", "CrushSync");
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
        int x = 0;
        while (x < 10000 && sync == null) {
            Thread.sleep(1L);
            ++x;
        }
        pong = System.currentTimeMillis();
        String contentType = "text/html";
        if (request.getProperty("command", "").equals("ping")) {
            baos.write(System.getProperty("crushsync.temp_agent", "false").getBytes("UTF8"));
            baos.write("\r\n".getBytes());
        } else if (request.getProperty("command", "").equals("status")) {
            Properties p2 = new Properties();
            p2.put("working_dir", new File(CrushSyncUI.sync.base_path).getCanonicalPath());
            p2.put("version", String.valueOf("3.12.17"));
            p2.put("status", CrushSyncUI.sync.status);
            if (CrushSyncUI.sync.growls.size() > 0) {
                Object o = CrushSyncUI.sync.growls.remove(0);
                if (new File(String.valueOf(CrushSyncUI.sync.base_path) + "debug").exists()) {
                    CrushSyncDaemon.msg(String.valueOf(request.getProperty("command", "")) + ":" + CrushSyncUI.sync.status + ":" + o);
                }
                p2.put("growl", "" + o);
            } else if (new File(String.valueOf(CrushSyncUI.sync.base_path) + "debug").exists()) {
                CrushSyncDaemon.msg(String.valueOf(request.getProperty("command", "")) + ":" + CrushSyncUI.sync.status);
            }
            baos.write(Common.getXMLString(p2, "sync").getBytes("UTF8"));
            contentType = "text/xml";
        } else {
            CrushSyncDaemon.msg("COMMAND:" + request.getProperty("command", ""));
            if (request.getProperty("command", "").equals("encrypt_decrypt")) {
                String s = request.getProperty("pass");
                s = VRL.vrlDecode(s);
                s = Common.encryptDecrypt(s, request.getProperty("encrypt").equals("true"));
                if (request.getProperty("encrypt").equals("false")) {
                    s = VRL.vrlEncode(s);
                }
                baos.write(s.getBytes());
            } else if (request.getProperty("command", "").equals("quit")) {
                this.doQuit();
            } else if (request.getProperty("command", "").equals("checkPrefsConfig")) {
                baos.write(sync.checkPrefsConfig().getBytes());
                baos.write("\r\n".getBytes());
            } else if (request.getProperty("command", "").equals("doStartup")) {
                baos.write(sync.doStartup().getBytes());
                baos.write("\r\n".getBytes());
            } else if (request.getProperty("command", "").equals("sync_now")) {
                baos.write(sync.sync_now().getBytes());
                baos.write("\r\n".getBytes());
            } else if (request.getProperty("command", "").equals("get_sync_folder")) {
                baos.write(sync.get_sync_folder().getBytes());
                baos.write("\r\n".getBytes());
            } else if (request.getProperty("command", "").equals("start_syncs")) {
                baos.write(sync.start_syncs().getBytes());
                baos.write("\r\n".getBytes());
            } else if (request.getProperty("command", "").equals("stop_syncs")) {
                baos.write(sync.stop_syncs().getBytes());
                baos.write("\r\n".getBytes());
            } else if (request.getProperty("command", "").equals("load_prefs")) {
                CrushSyncUI.sync.prefs = sync.loadPrefs();
                Properties prefs2 = (Properties)CrushSyncUI.sync.prefs.clone();
                baos.write(Common.getXMLString(prefs2, "prefs").getBytes("UTF8"));
                contentType = "text/xml";
            } else if (request.getProperty("command", "").equals("save_prefs")) {
                String s = request.getProperty("prefs").replace('+', ' ');
                s = Common.replace_str(s, "%26", "&amp;");
                s = Common.replace_str(s, "%3C", "&lt;");
                s = Common.replace_str(s, "%3E", "&gt;");
                Properties prefs2 = (Properties)Common.readXMLObject(new ByteArrayInputStream(Common.url_decode(s).getBytes("UTF8")));
                if (CrushSyncUI.sync.prefs == null) {
                    CrushSyncUI.sync.prefs = new Properties();
                }
                CrushSyncUI.sync.prefs.putAll((Map<?, ?>)prefs2);
                if (!CrushSyncUI.sync.prefs.getProperty("save_password", "true").equals("true")) {
                    CrushSyncUI.sync.prefs.put("sync_password", "");
                }
                sync.savePrefs(CrushSyncUI.sync.prefs);
            } else if (request.getProperty("command", "").equals("get_log_home")) {
                baos.write(new File(CrushSyncUI.sync.base_path).getCanonicalPath().getBytes("UTF-8"));
                baos.write("\r\n".getBytes());
            }
            if (!request.getProperty("command", "").equals("encrypt_decrypt")) {
                if (baos.size() < 150) {
                    CrushSyncDaemon.msg("RESPONSE:" + new String(baos.toByteArray()));
                } else {
                    CrushSyncDaemon.msg("RESPONSE:" + baos.size() + " bytes");
                }
            }
        }
        return contentType;
    }

    public void doQuit() {
        if (ignore_quit) {
            return;
        }
        CrushSyncDaemon.msg("Starting quit...");
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        int x = 0;
                        while (x < 10) {
                            Thread.sleep(1000L);
                            CrushSyncDaemon.msg("Doing forced shutdown in " + (10 - x) + " secs if we don't close sooner.");
                            ++x;
                        }
                    }
                    catch (InterruptedException interruptedException) {
                        // empty catch block
                    }
                    System.exit(0);
                }
            });
            sync.stop_syncs();
        }
        catch (Exception exception) {
            // empty catch block
        }
        HTTPD.shutdown();
        System.exit(0);
    }
}

