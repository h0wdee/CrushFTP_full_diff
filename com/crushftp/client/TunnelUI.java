/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.HTTPD;
import com.crushftp.client.HttpCommandHandler;
import com.crushftp.client.URLConnection;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import com.crushftp.tunnel3.StreamController;
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

public class TunnelUI
extends HttpCommandHandler {
    long pong = System.currentTimeMillis();
    static Properties ui_cache = null;
    static boolean connected = false;
    static Properties prefs = null;
    StreamController sc = null;
    String launch_url = "";

    public static void main(String[] args) {
        System.setProperty("crushftp.worker.v8", "true");
        System.setProperty("com.crushftp.server.httphandler", "com.crushftp.client.TunnelUI");
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
            if (args != null && args.length > 0) {
                String s = args[0];
                if (!s.replace('\\', '/').endsWith("/")) {
                    s = String.valueOf(s) + "/";
                }
                System.setProperty("crushtunnel.prefs", s);
                System.setProperty("crushtunnel.log", String.valueOf(s) + "CrushTunnel.log");
            }
            System.out.println("********************************************************************");
            System.out.println("Tunnel " + StreamController.version + " started.");
            System.out.println("********************************************************************");
            Thread.sleep(1000L);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public TunnelUI() {
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    Thread.currentThread().setName("CrushTunnelUI Web Watcher");
                    while (true) {
                        try {
                            Thread.sleep(1000L);
                        }
                        catch (InterruptedException interruptedException) {
                            // empty catch block
                        }
                        if (System.currentTimeMillis() - TunnelUI.this.pong <= 30000L) continue;
                        System.exit(0);
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
                URLConnection urlc = URLConnection.openConnection(new VRL(System.getProperties().remove("com.crushftp.server.httphandler.path") + "WebInterface/CrushTunnelUI.zip"), new Properties());
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
                    path = Common.replace_str(path, "crushtunnel", "CrushTunnel");
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

    public String doConnect(String username, String password) {
        try {
            if (this.sc != null) {
                this.sc.startStopTunnel(false);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.sc = new StreamController(prefs.getProperty("base_url"), username, password, null);
            this.sc.startThreads();
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    long start = System.currentTimeMillis();
                    while (System.currentTimeMillis() - start < 30000L) {
                        if (TunnelUI.this.sc != null && TunnelUI.this.sc.tunnel != null && TunnelUI.this.sc.tunnel.containsKey("localPort")) {
                            TunnelUI.this.launch_url = "http://127.0.0.1:" + TunnelUI.this.sc.tunnel.getProperty("localPort", "0") + "/";
                            break;
                        }
                        try {
                            Thread.sleep(100L);
                        }
                        catch (InterruptedException interruptedException) {
                            // empty catch block
                        }
                    }
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
            return "" + e;
        }
        connected = true;
        return "";
    }

    @Override
    public String processRequest(Properties request, ByteArrayOutputStream baos, Properties session, String ip, ByteArrayOutputStream tmp) throws Exception {
        String contentType = "text/html";
        if (request.getProperty("command", "").equals("ping")) {
            this.pong = System.currentTimeMillis();
            baos.write(System.getProperty("crushtunnel.temp_agent", "false").getBytes("UTF8"));
            baos.write("\r\n".getBytes());
        } else if (request.getProperty("command", "").equals("status")) {
            this.pong = System.currentTimeMillis();
            Properties p2 = new Properties();
            p2.put("connected", String.valueOf(connected));
            if (!this.launch_url.equals("")) {
                p2.put("launch_url", this.launch_url);
                this.launch_url = "";
            }
            p2.put("version", String.valueOf(StreamController.version));
            if (this.sc != null) {
                p2.putAll((Map<?, ?>)this.sc.stats);
            }
            baos.write(Common.getXMLString(p2, "tunnel").getBytes("UTF8"));
            contentType = "text/xml";
        } else if (request.getProperty("command", "").equals("encrypt_decrypt")) {
            String s = request.getProperty("pass");
            s = VRL.vrlDecode(s);
            s = Common.encryptDecrypt(s, request.getProperty("encrypt").equals("true"));
            if (request.getProperty("encrypt").equals("false")) {
                s = VRL.vrlEncode(s);
            }
            baos.write(s.getBytes());
        } else if (request.getProperty("command", "").equals("connect")) {
            String s = this.doConnect(prefs.getProperty("tunnel_username"), Common.encryptDecrypt(prefs.getProperty("tunnel_password"), false));
            baos.write(s.getBytes());
        } else if (request.getProperty("command", "").equals("disconnect")) {
            if (this.sc != null) {
                this.sc.startStopTunnel(false);
            }
            connected = false;
            String s = "";
            baos.write(s.getBytes());
        } else if (request.getProperty("command", "").equals("quit")) {
            System.exit(0);
        } else if (request.getProperty("command", "").equals("load_prefs")) {
            prefs = new File(String.valueOf(System.getProperty("crushtunnel.prefs", "./")) + "prefs.XML").exists() ? (Properties)Common.readXMLObject(String.valueOf(System.getProperty("crushtunnel.prefs", "./")) + "prefs.XML") : new Properties();
            Properties prefs2 = (Properties)prefs.clone();
            baos.write(Common.getXMLString(prefs2, "prefs").getBytes("UTF8"));
            contentType = "text/xml";
        } else if (request.getProperty("command", "").equals("save_prefs")) {
            String s = request.getProperty("prefs").replace('+', ' ');
            s = Common.replace_str(s, "%26", "&amp;");
            s = Common.replace_str(s, "%3C", "&lt;");
            s = Common.replace_str(s, "%3E", "&gt;");
            Properties prefs2 = (Properties)Common.readXMLObject(new ByteArrayInputStream(Common.url_decode(s).getBytes("UTF8")));
            if (prefs == null) {
                prefs = new Properties();
            }
            prefs.putAll((Map<?, ?>)prefs2);
            if (!prefs.getProperty("save_password", "true").equals("true")) {
                prefs.put("tunnel_password", "");
            }
            new File(String.valueOf(System.getProperty("crushtunnel.prefs", "./")) + "prefs.XML.new").delete();
            Common.writeXMLObject(String.valueOf(System.getProperty("crushtunnel.prefs", "./")) + "prefs.XML.new", prefs, "prefs");
            new File(String.valueOf(System.getProperty("crushtunnel.prefs", "./")) + "prefs.XML").delete();
            new File(String.valueOf(System.getProperty("crushtunnel.prefs", "./")) + "prefs.XML.new").renameTo(new File(String.valueOf(System.getProperty("crushtunnel.prefs", "./")) + "prefs.XML"));
        }
        return contentType;
    }
}

