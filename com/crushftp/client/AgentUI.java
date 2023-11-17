/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  CrushTask.Start
 */
package com.crushftp.client;

import CrushTask.Start;
import com.crushftp.client.AgentScheduler;
import com.crushftp.client.Base64;
import com.crushftp.client.Client;
import com.crushftp.client.Common;
import com.crushftp.client.GenericClient;
import com.crushftp.client.HTTPClient;
import com.crushftp.client.HTTPD;
import com.crushftp.client.HttpCommandHandler;
import com.crushftp.client.URLConnection;
import com.crushftp.client.VRL;
import com.crushftp.client.Variables;
import com.crushftp.client.Worker;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.filechooser.FileSystemView;

public class AgentUI
extends HttpCommandHandler {
    Properties prefs = new Properties();
    public Properties clients = new Properties();
    public static AgentUI thisObj = null;
    static String last_m = "";
    static SimpleDateFormat mm = new SimpleDateFormat("mm");
    public Properties valid_tokens = new Properties();
    long pong = System.currentTimeMillis();
    public static Vector messages2 = new Vector();
    static Properties ui_cache = null;
    static String home_folder = "./";
    Variables var = new Variables();

    public static void main(String[] args) {
        int local_port = -1;
        ServerSocket ss = null;
        try {
            boolean another_server = false;
            try {
                new Socket("127.0.0.1", Integer.parseInt(System.getProperty("crushftp.agent.port", "33333"))).close();
                another_server = true;
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (another_server) {
                throw new IOException("Another server found on port " + System.getProperty("crushftp.agent.port", "33333"));
            }
            ss = new ServerSocket(Integer.parseInt(System.getProperty("crushftp.agent.port", "33333")), 1000, InetAddress.getByName(System.getProperty("crushftp.agent.bind_ip", "127.0.0.1")));
        }
        catch (IOException e2) {
            try {
                ss = new ServerSocket(0, 1000, InetAddress.getByName(System.getProperty("crushftp.agent.bind_ip", "127.0.0.1")));
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        try {
            local_port = ss.getLocalPort();
            ss.close();
        }
        catch (IOException e2) {
            // empty catch block
        }
        System.getProperties().put("crushftp.agent.port", String.valueOf(local_port));
        System.out.println("HTTP_PORT:" + local_port);
        System.setProperty("crushftp.worker.v8", "true");
        System.setProperty("crushftp.v10_beta", "true");
        System.setProperty("com.crushftp.server.httphandler", "com.crushftp.client.AgentUI");
        final String[] args2 = args;
        try {
            if (args.length > 0 && (args[0].equals("script") || args[0].equals("inline_script"))) {
                Client.main(args);
                return;
            }
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    HTTPD.main(args2);
                }
            });
            if (args.length > 0) {
                if (new File(args[0]).exists()) {
                    home_folder = String.valueOf(new File(args[0]).getCanonicalPath().replace('\\', '/')) + "/";
                }
                System.getProperties().put("java.awt.headless", "true");
                System.getProperties().put("crushclient.temp_agent", "true");
            } else {
                Thread.sleep(1000L);
                if (System.getProperty("java.awt.headless", "false").equals("false")) {
                    Desktop.getDesktop().browse(new URI("http://127.0.0.1:33333/"));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            try {
                if (System.getProperty("java.awt.headless", "false").equals("false")) {
                    Desktop.getDesktop().browse(new URI("http://127.0.0.1:33333/"));
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            System.exit(1);
        }
    }

    public AgentUI() {
        thisObj = this;
        System.getProperties().put("jdk.tls.useExtendedMasterSecret", System.getProperty("crushftp.tls.resume_session", "false"));
        if (System.getProperty("crushclient.prefs", "").equals("")) {
            System.getProperties().put("crushclient.prefs", home_folder);
        }
        this.prefs.put("version", "1.0");
        try {
            this.load_prefs();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    Thread.currentThread().setName("Agent Scheduler");
                    while (true) {
                        try {
                            String current_m = mm.format(new Date());
                            if (!last_m.equals(current_m)) {
                                last_m = current_m;
                                Thread.sleep(3000L);
                                AgentScheduler.runSchedules(thisObj);
                            }
                        }
                        catch (Exception e) {
                            Client.printStackTrace(e, 1, messages2);
                        }
                        try {
                            Thread.sleep(1000L);
                        }
                        catch (InterruptedException interruptedException) {
                            // empty catch block
                        }
                        if (System.currentTimeMillis() - AgentUI.this.pong <= 60000L || !System.getProperty("crushclient.temp_agent", "false").equals("true")) continue;
                        System.exit(0);
                    }
                }
            });
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    Common.System2.put("running_tasks", new Vector());
                    Thread.currentThread().setName("Managed Agent Worker");
                    System.getProperties().put("crushftp.version", "10");
                    final Properties config = new Properties();
                    try {
                        AgentUI.this.registerAgent(config);
                    }
                    catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    long lastRegister = 0L;
                    while (true) {
                        try {
                            Properties job_tmp;
                            if (System.currentTimeMillis() - lastRegister > 60000L) {
                                AgentUI.this.registerAgent(config);
                                lastRegister = System.currentTimeMillis();
                            }
                            if ((job_tmp = AgentUI.this.getActionItem(config)) != null && job_tmp.size() > 0) {
                                Worker.startWorker(new Runnable(){

                                    @Override
                                    public void run() {
                                        try {
                                            String response_id = job_tmp.getProperty("response_id");
                                            if (!job_tmp.getProperty("job_log_date_format", "").equals("") && System.getProperty("crushftp.log_date_format", "").equals("")) {
                                                System.getProperties().put("crushftp.log_date_format", job_tmp.getProperty("job_log_date_format", ""));
                                            }
                                            new File(String.valueOf(System.getProperty("crushclient.prefs", "./")) + "logs/").mkdirs();
                                            System.getProperties().put("crushftp.log_location", String.valueOf(System.getProperty("crushclient.prefs", "./")) + "logs/" + System.getProperty("crushclient.appname", "CrushClient") + ".log");
                                            System.getProperties().put("crushftp.jobs_location", System.getProperty("crushclient.prefs", "./"));
                                            new File(String.valueOf(System.getProperty("crushclient.prefs", "./")) + "jobs/" + Common.dots(job_tmp.getProperty("scheduleName"))).mkdirs();
                                            Common.writeXMLObject(String.valueOf(System.getProperty("crushclient.prefs", "./")) + "jobs/" + Common.dots(job_tmp.getProperty("scheduleName")) + "/job.XML", job_tmp, "job");
                                            File job = new File(String.valueOf(System.getProperty("crushclient.prefs", "./")) + "jobs/" + job_tmp.getProperty("scheduleName"));
                                            Properties params = (Properties)Common.readXMLObject(String.valueOf(job.getPath()) + "/job.XML");
                                            params.put("debug", "true");
                                            Properties event = new Properties();
                                            event.put("event_plugin_list", "CrushTask");
                                            event.put("name", "ScheduledPluginEvent:" + params.getProperty("scheduleName"));
                                            params.put("new_job_id", Common.makeBoundary(20));
                                            event.putAll((Map<?, ?>)params);
                                            Properties info = new Properties();
                                            info.put("action", "event");
                                            info.put("server_settings", new Properties());
                                            info.put("event", event);
                                            info.put("items", new Vector());
                                            Start crush_task = new Start();
                                            crush_task.setSettings(params);
                                            crush_task.run(info);
                                            byte[] b = null;
                                            try {
                                                RandomAccessFile raf = new RandomAccessFile(info.getProperty("log_file"), "r");
                                                b = new byte[(int)raf.length()];
                                                raf.readFully(b);
                                                raf.close();
                                            }
                                            catch (Throwable e) {
                                                e.printStackTrace();
                                                b = new byte[]{};
                                            }
                                            job_tmp.put("log", new String(b));
                                            AgentUI.this.sendActionResponse(config, job_tmp, response_id);
                                        }
                                        catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                        catch (Exception e) {
                            Client.printStackTrace(e, 1, messages2);
                        }
                        try {
                            Thread.sleep(10000L);
                        }
                        catch (InterruptedException interruptedException) {
                        }
                    }
                }
            });
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    Thread.currentThread().setName("Logging");
                    SimpleDateFormat day = new SimpleDateFormat("yyyyMMdd");
                    String last_day = day.format(new Date());
                    block8: while (true) {
                        String log_file;
                        AgentUI.this.var.setDate(new Date());
                        RandomAccessFile raf = null;
                        System.getProperties().put("crushclient.debug", AgentUI.this.prefs.getProperty("log_level", "1"));
                        while (messages2.size() > 0) {
                            try {
                                if (raf == null && (AgentUI.this.prefs.getProperty("enable_logging", "false").equals("true") || AgentUI.this.prefs.getProperty("enable_logging", "false").equals("on"))) {
                                    log_file = AgentUI.this.prefs.getProperty("log_file", "logs/" + System.getProperty("crushclient.appname", "CrushClient") + "_{yyyy}{MM}{dd}.log");
                                    if (log_file.equals("") || log_file.startsWith(String.valueOf(System.getProperty("crushclient.appname", "CrushClient")) + ".log")) {
                                        log_file = String.valueOf(System.getProperty("crushclient.prefs", "./")) + "logs/" + System.getProperty("crushclient.appname", "CrushClient") + "_{yyyy}{MM}{dd}.log";
                                    }
                                    if (log_file.startsWith("./")) {
                                        log_file = String.valueOf(System.getProperty("crushclient.prefs", "./")) + "logs/" + log_file.substring(2);
                                    }
                                    if (log_file.indexOf("/") < 0 && log_file.indexOf("\\") < 0) {
                                        log_file = String.valueOf(System.getProperty("crushclient.prefs", "./")) + "logs/" + log_file;
                                    }
                                    log_file = AgentUI.this.var.replace_vars_line_date(log_file, null, "{", "}");
                                    new File(Common.all_but_last(log_file)).mkdirs();
                                    raf = new RandomAccessFile(log_file, "rw");
                                    raf.seek(raf.length());
                                }
                                String s = messages2.remove(0).toString();
                                if (raf == null) continue;
                                raf.write((String.valueOf(s) + "\r\n").getBytes("UTF8"));
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            if (raf != null) {
                                raf.close();
                            }
                            Thread.sleep(1000L);
                        }
                        catch (Exception e) {
                            // empty catch block
                        }
                        if (day.format(new Date()).equals(last_day)) continue;
                        last_day = day.format(new Date());
                        if (!AgentUI.this.prefs.getProperty("enable_logging", "false").equals("true")) continue;
                        log_file = AgentUI.this.prefs.getProperty("log_file", "logs/" + System.getProperty("crushclient.appname", "CrushClient") + "_{yyyy}{MM}{dd}.log");
                        if (log_file.equals("") || log_file.startsWith(String.valueOf(System.getProperty("crushclient.appname", "CrushClient")) + ".log")) {
                            log_file = String.valueOf(System.getProperty("crushclient.prefs", "./")) + "logs/" + System.getProperty("crushclient.appname", "CrushClient") + "_{yyyy}{MM}{dd}.log";
                        }
                        if (log_file.startsWith("./")) {
                            log_file = String.valueOf(System.getProperty("crushclient.prefs", "./")) + "logs/" + log_file.substring(2);
                        }
                        if (log_file.indexOf("/") < 0 && log_file.indexOf("\\") < 0) {
                            log_file = String.valueOf(System.getProperty("crushclient.prefs", "./")) + "logs/" + log_file;
                        }
                        log_file = AgentUI.this.var.replace_vars_line_date(log_file, null, "{", "}");
                        new File(Common.all_but_last(log_file)).mkdirs();
                        File log_f = new File(log_file);
                        try {
                            log_f.renameTo(new File(String.valueOf(log_f.getParentFile().getPath()) + "/" + last_day + "_" + log_f.getName()));
                            new RandomAccessFile(log_file, "rw").close();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            int days = Integer.parseInt(AgentUI.this.prefs.getProperty("log_history", "3"));
                            GregorianCalendar cal = new GregorianCalendar();
                            cal.setTime(new Date());
                            int x = days;
                            while (true) {
                                if (x >= 100) continue block8;
                                if (x > days) {
                                    new File(String.valueOf(log_f.getParentFile().getPath()) + "/" + day.format(cal.getTime()) + "_" + log_f.getName()).delete();
                                }
                                cal.add(5, -1 * days);
                                ++x;
                            }
                        }
                        catch (NumberFormatException e) {
                            e.printStackTrace();
                            continue;
                        }
                        break;
                    }
                }
            });
        }
        catch (IOException e) {
            Client.printStackTrace(e, 1, messages2);
        }
    }

    public void registerAgent(Properties config) throws Exception {
        try {
            Vector servers = (Vector)this.prefs.get("servers");
            if (servers != null && servers.size() > 0) {
                int x = 0;
                while (x < servers.size()) {
                    Properties p = (Properties)servers.elementAt(x);
                    if (!(p.getProperty("protocol", "").equals("") || p.getProperty("host", "").equals("") || p.getProperty("port", "").equals("") || p.getProperty("user", "").equals("") || p.getProperty("pass", "").equals(""))) {
                        String url = String.valueOf(p.getProperty("protocol")) + "://" + p.getProperty("host") + ":" + p.getProperty("port") + "/";
                        HTTPClient c = new HTTPClient(url, "AGENT:", messages2);
                        c.setConfigObj(config);
                        c.login(p.getProperty("user"), Common.encryptDecrypt(p.getProperty("pass"), false), "CrushClient");
                        c.doAction("agentRegister", "&name=" + p.getProperty("name"), "");
                    }
                    ++x;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Properties getActionItem(Properties config) throws Exception {
        Vector servers = (Vector)this.prefs.get("servers");
        if (servers != null && servers.size() > 0) {
            int x = 0;
            while (x < servers.size()) {
                Properties p = (Properties)servers.elementAt(x);
                if (!(p.getProperty("protocol", "").equals("") || p.getProperty("host", "").equals("") || p.getProperty("port", "").equals("") || p.getProperty("user", "").equals("") || p.getProperty("pass", "").equals(""))) {
                    String url = String.valueOf(p.getProperty("protocol")) + "://" + p.getProperty("host") + ":" + p.getProperty("port") + "/";
                    HTTPClient c = new HTTPClient(url, "AGENT:", messages2);
                    c.setConfigObj(config);
                    String result = c.doAction("agentQueue", "&name=" + p.getProperty("name"), "");
                    byte[] b = Base64.decode(result);
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b));
                    Properties action = (Properties)ois.readObject();
                    ois.close();
                    return action;
                }
                ++x;
            }
        }
        return null;
    }

    public void sendActionResponse(Properties config, Properties action, String response_id) throws Exception {
        Vector servers = (Vector)this.prefs.get("servers");
        if (servers != null && servers.size() > 0) {
            int x = 0;
            while (x < servers.size()) {
                Properties p = (Properties)servers.elementAt(x);
                if (!(p.getProperty("protocol", "").equals("") || p.getProperty("host", "").equals("") || p.getProperty("port", "").equals("") || p.getProperty("user", "").equals("") || p.getProperty("pass", "").equals(""))) {
                    String url = String.valueOf(p.getProperty("protocol")) + "://" + p.getProperty("host") + ":" + p.getProperty("port") + "/";
                    HTTPClient c = new HTTPClient(url, "AGENT:", messages2);
                    c.setConfigObj(config);
                    String b64 = Base64.encodeBytes(Common.getXMLString(action, "response").getBytes("UTF8"));
                    String string = c.doAction("agentResponse", "&name=" + p.getProperty("name") + "&response_id=" + response_id, "&response=" + Common.url_encode(b64));
                }
                ++x;
            }
        }
    }

    public static String l(String key) {
        String s = System.getProperties().getProperty("crushclient.localization." + key, key);
        s = Common.replace_str(s, "%appname%", System.getProperty("crushclient.appname", "CrushClient"));
        return s;
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
                Client.printStackTrace(e, 1, messages2);
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
            if (System.getProperty("com.crushftp.server.httphandler.path", String.valueOf(System.getProperty("crushclient.prefs", "./")) + "clientui/").toUpperCase().startsWith("HTTP") && ui_cache == null) {
                URLConnection urlc = URLConnection.openConnection(new VRL(System.getProperties().remove("com.crushftp.server.httphandler.path") + "WebInterface/" + System.getProperty("crushclient.appname", "CrushClient") + ".zip"), new Properties());
                ZipInputStream zis = new ZipInputStream(urlc.getInputStream());
                ZipEntry entry = zis.getNextEntry();
                while (entry != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        Common.streamCopier(zis, baos, false, false, true);
                    }
                    catch (InterruptedException e) {
                        Client.printStackTrace(e, 1, messages2);
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
                file = new File(String.valueOf(System.getProperty("com.crushftp.server.httphandler.path", String.valueOf(System.getProperty("crushclient.prefs", "./")) + "clientui/")) + path);
                if (file.exists()) {
                    f = file.toURI().toURL();
                    try {
                        Common.streamCopier(new FileInputStream(file), f_b, false, true, true);
                    }
                    catch (InterruptedException e) {
                        Client.printStackTrace(e, 1, messages2);
                    }
                } else if (ui_cache == null) {
                    path = Common.replace_str(path, "crushClient", "CrushClient");
                    f = this.getClass().getResource("/clientui/" + path);
                    if (f != null) {
                        try {
                            Common.streamCopier(this.getClass().getResourceAsStream("/clientui/" + path), f_b, false, true, true);
                        }
                        catch (InterruptedException e) {
                            Client.printStackTrace(e, 1, messages2);
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
        String contentType = "text/html";
        if (request.getProperty("command", "").equals("authenticate")) {
            if (new File(String.valueOf(System.getProperty("crushclient.prefs", "./")) + "prefs.XML").exists()) {
                this.prefs = (Properties)Common.readXMLObject(String.valueOf(System.getProperty("crushclient.prefs", "./")) + "prefs.XML");
            }
            if (this.prefs.getProperty("agent_password", "").equals("") || Common.getMD5(new ByteArrayInputStream(VRL.vrlDecode(request.getProperty("password")).getBytes())).equals(this.prefs.getProperty("agent_password", ""))) {
                String token = Common.makeBoundary(15);
                this.valid_tokens.put(token, String.valueOf(System.currentTimeMillis()));
                baos.write(("SUCCESS:" + token).getBytes());
            } else {
                baos.write("ERROR:Invalid password".getBytes());
            }
        } else if (request.getProperty("command", "").equals("ping")) {
            this.pong = System.currentTimeMillis();
            baos.write(System.getProperty("crushclient.temp_agent", "false").getBytes("UTF8"));
            baos.write("\r\n".getBytes());
        } else if (request.getProperty("command", "").equals("status")) {
            this.pong = System.currentTimeMillis();
            Properties p2 = new Properties();
            p2.put("version", "1.8.22");
            baos.write(Common.getXMLString(p2, "status").getBytes("UTF8"));
            contentType = "text/xml";
        } else if (!this.valid_tokens.containsKey(request.getProperty("auth_token", ""))) {
            baos.write("ERROR:NOT AUTHENTICATED\r\n".getBytes());
        } else if (request.getProperty("command", "").equals("log")) {
            Client client = (Client)this.clients.get(request.getProperty("client"));
            Vector<String> log_snippet = new Vector<String>();
            while (client != null && client.messages.size() > 0) {
                log_snippet.insertElementAt(client.messages.remove(0).toString(), 0);
            }
            int x = 0;
            while (x < log_snippet.size()) {
                baos.write(log_snippet.elementAt(x).toString().getBytes("UTF8"));
                baos.write("\r\n".getBytes());
                ++x;
            }
        } else if (request.getProperty("command", "").equals("stats")) {
            Client client = (Client)this.clients.get(request.getProperty("client"));
            Vector v = client.getStats();
            while (v.size() > 0) {
                baos.write(v.remove(0).toString().getBytes("UTF8"));
                baos.write("\r\n".getBytes());
            }
        } else if (request.getProperty("command", "").equals("queue")) {
            Client client = (Client)this.clients.get(request.getProperty("client"));
            client.line("QUEUE " + request.getProperty("queue_type", ""));
            Vector v = null;
            if (request.getProperty("queue_type", "").equals("success")) {
                v = (Vector)client.success_transfer_queue.clone();
            } else if (request.getProperty("queue_type", "").equals("failed")) {
                v = (Vector)client.failed_transfer_queue.clone();
            } else if (request.getProperty("queue_type", "").equals("pending")) {
                v = (Vector)client.pending_transfer_queue.clone();
                v.addAll(client.pending_transfer_queue_inprogress);
            }
            while (v.size() > 0) {
                baos.write(v.remove(0).toString().getBytes("UTF8"));
                baos.write("\r\n".getBytes());
            }
        } else if (request.getProperty("command", "").equals("stats_summary")) {
            Client client = (Client)this.clients.get(request.getProperty("client"));
            Enumeration<Object> keys = client.stats.keys();
            while (keys.hasMoreElements()) {
                String key = "" + keys.nextElement();
                baos.write((String.valueOf(key) + "=" + client.stats.getProperty(key) + ";").getBytes("UTF8"));
            }
            baos.write(("success_size=" + client.success_transfer_queue.size() + ";").getBytes("UTF8"));
            baos.write(("failed_size=" + client.failed_transfer_queue.size() + ";").getBytes("UTF8"));
            baos.write(("pending_size=" + (client.pending_transfer_queue.size() + client.pending_transfer_queue_inprogress.size()) + ";").getBytes("UTF8"));
            baos.write("\r\n".getBytes());
        } else if (request.getProperty("command", "").equals("create")) {
            Client client = this.getNewClient();
            client.uid = request.getProperty("client");
            client.prefs.putAll((Map<?, ?>)this.prefs);
            this.clients.put(client.uid, client);
            if (this.clients.size() == 1 && !this.prefs.getProperty("default_script", "").equals("")) {
                request.put("command_str", Common.replace_str(this.prefs.getProperty("default_script", ""), ";", "\r\n"));
                request.put("command", "process_command");
                this.processRequest(request, baos, session, ip, tmp);
            }
        } else if (request.getProperty("command", "").equals("list")) {
            Enumeration<Object> keys = this.clients.keys();
            while (keys.hasMoreElements()) {
                baos.write((String.valueOf(keys.nextElement().toString()) + "\r\n").getBytes());
            }
        } else if (request.getProperty("command", "").equals("encrypt_decrypt")) {
            String s = request.getProperty("pass");
            s = VRL.vrlDecode(s);
            s = Common.encryptDecrypt(s, request.getProperty("encrypt").equals("true"));
            if (request.getProperty("encrypt").equals("false")) {
                s = VRL.vrlEncode(s);
            }
            baos.write(s.getBytes());
        } else if (request.getProperty("command", "").equals("destroy")) {
            Client client = (Client)this.clients.remove(request.getProperty("client"));
            client.pending_transfer_queue.clear();
            client.pending_transfer_queue_inprogress.clear();
            client.process_command("QUIT", false);
        } else if (request.getProperty("command", "").equals("info")) {
            Client client = (Client)this.clients.get(request.getProperty("client"));
            Properties credentials = new Properties();
            Properties p1 = (Properties)client.source_credentials.clone();
            p1.remove("password");
            credentials.put("source", p1);
            Properties p2 = (Properties)client.destination_credentials.clone();
            p2.remove("password");
            credentials.put("destination", p2);
            baos.write(Common.getXMLString(credentials, "credentials").getBytes("UTF8"));
            contentType = "text/xml";
        } else if (request.getProperty("command", "").equals("process_command")) {
            Client client = (Client)this.clients.get(request.getProperty("client"));
            client.interactive = false;
            BufferedReader br = new BufferedReader(new StringReader(request.getProperty("command_str")));
            String s = "";
            while ((s = br.readLine()) != null) {
                Object o = null;
                if (s.toUpperCase().startsWith("LDISC") || s.toUpperCase().startsWith("DISC") || s.toUpperCase().startsWith("LCONNECT") || s.toUpperCase().startsWith("CONNECT") || s.toUpperCase().startsWith("LABORT") || s.toUpperCase().startsWith("ABORT")) {
                    client.pending_transfer_queue.clear();
                    client.pending_transfer_queue_inprogress.clear();
                }
                if (s.toUpperCase().startsWith("LPUT") || s.toUpperCase().startsWith("PUT") || s.toUpperCase().startsWith("LGET") || s.toUpperCase().startsWith("GET")) {
                    client.pending_transfer_queue.addElement(VRL.vrlDecode(s));
                    client.startupThreads();
                } else {
                    o = client.process_command(String.valueOf(VRL.vrlDecode(s)) + (request.getProperty("multithreaded", "false").equals("true") ? "&" : ""), false);
                }
                if (o != null && (request.getProperty("command_str").toUpperCase().startsWith("LIST") || request.getProperty("command_str").toUpperCase().startsWith("LLIST") || request.getProperty("command_str").toUpperCase().startsWith("DIR") || request.getProperty("command_str").toUpperCase().startsWith("LDIR"))) {
                    if (o instanceof Properties) {
                        Properties p = (Properties)o;
                        baos.write("{\"listing\":".getBytes("UTF8"));
                        baos.write(AgentUI.getJsonListObj(p, false).getBytes("UTF8"));
                        contentType = "application/jsonrequest;charset=utf-8";
                        Enumeration<Object> keys = p.keys();
                        while (keys.hasMoreElements()) {
                            String val;
                            String key = keys.nextElement().toString();
                            if (key.equals("listing") || (val = p.getProperty(key)) == null) continue;
                            baos.write((",\"" + key + "\":\"").getBytes("UTF8"));
                            baos.write((String.valueOf(val.trim().replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D").replaceAll("\\n", "%0A").replaceAll("\\\\", "%5C").replaceAll(":", "%3A").replaceAll("\ufffd", "%E2%80%99")) + "\"").getBytes("UTF8"));
                        }
                        baos.write("}".getBytes("UTF8"));
                        continue;
                    }
                    baos.write(("" + o).getBytes());
                    continue;
                }
                if (o == null) continue;
                baos.write(("" + o).getBytes());
            }
        } else if (request.getProperty("command", "").equals("clear_failed") || request.getProperty("command", "").equals("clear_success") || request.getProperty("command", "").equals("clear_queue")) {
            Client client = (Client)this.clients.get(request.getProperty("client"));
            client.line(request.getProperty("command", ""));
            if (request.getProperty("command", "").equals("clear_failed")) {
                if (request.getProperty("specific_item", "").trim().equals("")) {
                    client.failed_transfer_queue.clear();
                } else {
                    client.failed_transfer_queue.removeElement(request.getProperty("specific_item", "").trim());
                }
            } else if (request.getProperty("command", "").equals("clear_success")) {
                if (!request.getProperty("up_to_index", "").equals("")) {
                    int remove_group = Integer.parseInt(request.getProperty("up_to_index", ""));
                    int x = 0;
                    while (x < remove_group && client.success_transfer_queue.size() > 0) {
                        client.success_transfer_queue.removeElementAt(0);
                        ++x;
                    }
                } else {
                    client.stats.put("download_count", "0");
                    client.stats.put("upload_count", "0");
                    client.stats.put("download_bytes", "0");
                    client.stats.put("upload_bytes", "0");
                    client.stats.put("upload_skipped_count", "0");
                    client.stats.put("upload_skipped_bytes", "0");
                    client.stats.put("download_skipped_count", "0");
                    client.stats.put("download_skipped_bytes", "0");
                    client.success_transfer_queue.clear();
                }
            } else if (request.getProperty("command", "").equals("clear_queue") || request.getProperty("command", "").equals("clear_pending")) {
                if (request.getProperty("specific_item", "").trim().equals("")) {
                    client.pending_transfer_queue.clear();
                    client.pending_transfer_queue_inprogress.clear();
                } else {
                    client.pending_transfer_queue.removeElement(request.getProperty("specific_item", "-1").trim());
                }
            }
        } else if (request.getProperty("command", "").equals("abort_action")) {
            Client client = (Client)this.clients.get(request.getProperty("client"));
            String path = Common.url_decode(request.getProperty("path"));
            try {
                GenericClient c;
                GenericClient c2 = null;
                int x = 0;
                while (c2 == null && x < client.destination_used.size()) {
                    c = (GenericClient)client.destination_used.elementAt(x);
                    if (c.getConfig("transfer_path_dst", "").equals(path) && c.getConfig("transfer_direction", "").equals("PUT")) {
                        c2 = c;
                    }
                    ++x;
                }
                x = 0;
                while (c2 == null && x < client.source_used.size()) {
                    c = (GenericClient)client.source_used.elementAt(x);
                    if (c.getConfig("transfer_path_src", "").equals(path) && c.getConfig("transfer_direction", "").equals("GET")) {
                        c2 = c;
                    }
                    ++x;
                }
                client.line("ABORT " + path + ":" + (c2 != null));
                if (c2 != null) {
                    c2.setConfig("abort_obj", c2);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        } else if (request.getProperty("command", "").equals("run_schedule")) {
            Client client = (Client)this.clients.get(request.getProperty("client"));
            Vector schedules = (Vector)this.prefs.get("schedules");
            int x = 0;
            while (x < schedules.size()) {
                Properties p = (Properties)schedules.elementAt(x);
                if (p.getProperty("scheduleName").equals(request.getProperty("scheduleName"))) {
                    return this.runSchedule(client, p);
                }
                ++x;
            }
        } else if (request.getProperty("command", "").equals("version")) {
            baos.write("1.8.22".getBytes("UTF8"));
            baos.write("\r\n".getBytes());
        } else if (request.getProperty("command", "").equals("set_password")) {
            String pass_hash = Common.getMD5(new ByteArrayInputStream(VRL.vrlDecode(request.getProperty("password")).getBytes()));
            if (this.prefs.getProperty("agent_password", "").equals("") || pass_hash.equals(this.prefs.getProperty("agent_password", ""))) {
                if (request.getProperty("new_password", "").equals("")) {
                    this.prefs.put("agent_password", "");
                } else {
                    this.prefs.put("agent_password", Common.getMD5(new ByteArrayInputStream(VRL.vrlDecode(request.getProperty("new_password")).getBytes())));
                }
                this.save_prefs();
                baos.write("SUCCESS:Password updated\r\n".getBytes());
            } else {
                baos.write("ERROR:Invalid password\r\n".getBytes());
            }
            baos.write("\r\n".getBytes());
        } else if (request.getProperty("command", "").equals("load_prefs")) {
            this.prefs = this.load_prefs();
            Properties prefs2 = (Properties)this.prefs.clone();
            prefs2.remove("agent_password");
            baos.write(Common.getXMLString(prefs2, "prefs").getBytes("UTF8"));
            contentType = "text/xml";
            if (this.prefs.getProperty("temp_agent", "false").equals("true")) {
                System.getProperties().put("crushclient.temp_agent", "true");
            }
        } else if (request.getProperty("command", "").equals("save_prefs")) {
            String s = request.getProperty("prefs").replace('+', ' ');
            s = Common.replace_str(s, "%26", "&amp;");
            s = Common.replace_str(s, "%3C", "&lt;");
            s = Common.replace_str(s, "%3E", "&gt;");
            Properties prefs2 = (Properties)Common.readXMLObject(new ByteArrayInputStream(Common.url_decode(s).getBytes("UTF8")));
            if (this.prefs == null) {
                this.prefs = new Properties();
            }
            prefs2.remove("agent_password");
            this.prefs.putAll((Map<?, ?>)prefs2);
            this.save_prefs();
        } else if (request.getProperty("command", "").equals("get_user_home")) {
            baos.write(FileSystemView.getFileSystemView().getHomeDirectory().getCanonicalPath().replace('\\', '/').getBytes("UTF-8"));
            baos.write("\r\n".getBytes());
        } else if (request.getProperty("command", "").equals("get_log_home")) {
            String log_file = this.prefs.getProperty("log_file", "logs/" + System.getProperty("crushclient.appname", "CrushClient") + "_{yyyy}{MM}{dd}.log");
            if (log_file.equals("") || log_file.startsWith(String.valueOf(System.getProperty("crushclient.appname", "CrushClient")) + ".log")) {
                log_file = String.valueOf(System.getProperty("crushclient.prefs", "./")) + "logs/" + System.getProperty("crushclient.appname", "CrushClient") + "_{yyyy}{MM}{dd}.log";
            }
            if (log_file.startsWith("./")) {
                log_file = String.valueOf(System.getProperty("crushclient.prefs", "./")) + "logs/" + log_file.substring(2);
            }
            if (log_file.indexOf("/") < 0 && log_file.indexOf("\\") < 0) {
                log_file = String.valueOf(System.getProperty("crushclient.prefs", "./")) + "logs/" + log_file;
            }
            log_file = this.var.replace_vars_line_date(log_file, null, "{", "}");
            new File(Common.all_but_last(log_file)).mkdirs();
            baos.write(new File(Common.all_but_last(log_file)).getCanonicalPath().getBytes("UTF-8"));
            baos.write("\r\n".getBytes());
        } else if (request.getProperty("command", "").equals("quit")) {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        Thread.sleep(10000L);
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
                        Thread.sleep(200L);
                        HTTPD.shutdown();
                        System.exit(0);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    System.exit(0);
                }
            });
            baos.write("OK\r\n".getBytes());
        }
        return contentType;
    }

    public Properties load_prefs() {
        Properties prefs_system = new Properties();
        Properties prefs_user = new Properties();
        String prefs_loc = "./";
        String filename = String.valueOf(System.getProperty("crushclient.appname", "CrushClient")) + "User_prefs.XML";
        if (Common.machine_is_x()) {
            prefs_loc = String.valueOf(System.getProperty("user.home")) + "/Library/Preferences/";
        } else if (Common.machine_is_windows() && new File(String.valueOf(System.getProperty("user.home")) + "/AppData/").exists()) {
            prefs_loc = String.valueOf(System.getProperty("user.home")) + "/AppData/";
        }
        if (new File(String.valueOf(System.getProperty("crushclient.prefs", "./")) + "prefs.XML").exists()) {
            prefs_system = (Properties)Common.readXMLObject(String.valueOf(System.getProperty("crushclient.prefs", "./")) + "prefs.XML");
        }
        if (new File(String.valueOf(prefs_loc) + filename).exists()) {
            prefs_user = (Properties)Common.readXMLObject(String.valueOf(prefs_loc) + filename);
        }
        this.prefs.putAll((Map<?, ?>)prefs_system);
        this.prefs.putAll((Map<?, ?>)prefs_user);
        return this.prefs;
    }

    public synchronized void save_prefs() throws Exception {
        Vector<String> user_keys = new Vector<String>();
        String[] user_keys_str = new String[]{"bookmarks", "schedules", "log_file", "client_view_mode", "log_level", "log_history", "enable_logging", "transfers_log_file"};
        int x = 0;
        while (x < user_keys_str.length) {
            user_keys.addElement(user_keys_str[x]);
            ++x;
        }
        Properties prefs_user = new Properties();
        Properties prefs_system = new Properties();
        Enumeration<Object> keys = this.prefs.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            Object val = this.prefs.get(key);
            if (user_keys.indexOf(key) >= 0) {
                prefs_user.put(key, val);
                continue;
            }
            prefs_system.put(key, val);
        }
        new File(String.valueOf(System.getProperty("crushclient.prefs", "./")) + "prefs.XML.new").delete();
        Common.writeXMLObject(String.valueOf(System.getProperty("crushclient.prefs", "./")) + "prefs.XML.new", prefs_system, "prefs");
        new File(String.valueOf(System.getProperty("crushclient.prefs", "./")) + "prefs.XML").delete();
        new File(String.valueOf(System.getProperty("crushclient.prefs", "./")) + "prefs.XML.new").renameTo(new File(String.valueOf(System.getProperty("crushclient.prefs", "./")) + "prefs.XML"));
        String prefs_loc = "./";
        String filename = String.valueOf(System.getProperty("crushclient.appname", "CrushClient")) + "User_prefs.XML";
        if (Common.machine_is_x()) {
            prefs_loc = String.valueOf(System.getProperty("user.home")) + "/Library/Preferences/";
        } else if (Common.machine_is_windows() && new File(String.valueOf(System.getProperty("user.home")) + "/AppData/").exists()) {
            prefs_loc = String.valueOf(System.getProperty("user.home")) + "/AppData/";
        }
        new File(String.valueOf(prefs_loc) + filename + ".new").delete();
        Common.writeXMLObject(String.valueOf(prefs_loc) + filename + ".new", prefs_user, "prefs");
        new File(String.valueOf(prefs_loc) + filename).delete();
        new File(String.valueOf(prefs_loc) + filename + ".new").renameTo(new File(String.valueOf(prefs_loc) + filename));
    }

    public Client getNewClient() {
        Client client = new Client(new Vector(), messages2);
        client.dual_log = false;
        client.interactive = false;
        return client;
    }

    public String runSchedule(final Client client, final Properties schedule) {
        try {
            String script = schedule.getProperty("script", "").trim();
            if (new File(script).exists()) {
                ByteArrayOutputStream baos_script = new ByteArrayOutputStream();
                Common.streamCopier(new FileInputStream(script), baos_script, false, true, true);
                script = new String(baos_script.toByteArray(), "UTF8").trim();
            }
            script = String.valueOf(script) + "\r\nquit\r\n";
            String connect_prefix = "";
            int pos = script.indexOf("{bookmark:");
            int pos2 = script.indexOf("connect {bookmark:");
            while (pos >= 0) {
                String inner = script.substring(pos, script.indexOf("}", pos) + 1);
                Vector bookmarks = (Vector)this.prefs.get("bookmarks");
                String result = null;
                int x = 0;
                while (bookmarks != null && x < bookmarks.size()) {
                    Properties book = (Properties)bookmarks.elementAt(x);
                    if (book.getProperty("name", "").equalsIgnoreCase(inner.split(":")[1])) {
                        if (!book.getProperty("pbe_pass", "").equals("")) {
                            script = "pbe " + book.getProperty("pbe_pass") + "\r\n" + script;
                        }
                        String val = "";
                        String key = inner.split(":")[2];
                        if ((key = key.substring(0, key.length() - 1)).equalsIgnoreCase("pass")) {
                            val = VRL.vrlEncode(Common.encryptDecrypt(book.getProperty("pass"), false));
                        } else if (key.equalsIgnoreCase("url")) {
                            if (book.getProperty("protocol").startsWith("file:")) {
                                val = String.valueOf(book.getProperty("protocol")) + book.getProperty("defaultPath");
                            } else {
                                if (!book.getProperty("maxThreads", "").equals("")) {
                                    connect_prefix = String.valueOf(connect_prefix) + "set max_threads " + book.getProperty("maxThreads") + "\r\n";
                                }
                                if (!book.getProperty("secure_data", "").equals("")) {
                                    connect_prefix = String.valueOf(connect_prefix) + "config secure_data " + book.getProperty("secure_data") + "\r\n";
                                }
                                if (!book.getProperty("connect", "Default").equalsIgnoreCase("Default")) {
                                    connect_prefix = String.valueOf(connect_prefix) + "config pasv " + book.getProperty("connect").equalsIgnoreCase("PASV") + "\r\n";
                                }
                                if (!book.getProperty("ssh_private_key", "").equalsIgnoreCase("")) {
                                    connect_prefix = String.valueOf(connect_prefix) + "config ssh_private_key " + book.getProperty("ssh_private_key") + "\r\n";
                                }
                                if (!book.getProperty("ssh_private_key_pass", "").equalsIgnoreCase("")) {
                                    connect_prefix = String.valueOf(connect_prefix) + "config ssh_private_key_pass " + book.getProperty("ssh_private_key_pass") + "\r\n";
                                }
                                if (!book.getProperty("ssh_two_factor", "").equalsIgnoreCase("")) {
                                    connect_prefix = String.valueOf(connect_prefix) + "config ssh_two_factor " + book.getProperty("ssh_two_factor") + "\r\n";
                                }
                                if (!book.getProperty("verifyHost", "").equalsIgnoreCase("")) {
                                    connect_prefix = String.valueOf(connect_prefix) + "config verifyHost " + book.getProperty("verifyHost") + "\r\n";
                                }
                                if (!book.getProperty("addNewHost", "").equalsIgnoreCase("")) {
                                    connect_prefix = String.valueOf(connect_prefix) + "config addNewHost " + book.getProperty("addNewHost") + "\r\n";
                                }
                                if (!book.getProperty("knownHostFile", "").equalsIgnoreCase("")) {
                                    connect_prefix = String.valueOf(connect_prefix) + "config knownHostFile " + book.getProperty("knownHostFile") + "\r\n";
                                }
                                if (!book.getProperty("multi", "").equalsIgnoreCase("")) {
                                    connect_prefix = String.valueOf(connect_prefix) + "config multi " + book.getProperty("multi") + "\r\n";
                                }
                                if (!book.getProperty("multi_segmented_download", "").equalsIgnoreCase("")) {
                                    connect_prefix = String.valueOf(connect_prefix) + "config multi_segmented_download " + book.getProperty("multi_segmented_download") + "\r\n";
                                }
                                val = String.valueOf(book.getProperty("protocol")) + book.getProperty("user") + ":" + VRL.vrlEncode(Common.encryptDecrypt(book.getProperty("pass"), false)) + "@" + book.getProperty("host") + ":" + book.getProperty("port") + book.getProperty("defaultPath");
                            }
                            if (!val.endsWith("/")) {
                                val = String.valueOf(val) + "/";
                            }
                        } else {
                            val = book.getProperty(key);
                        }
                        result = val;
                        break;
                    }
                    ++x;
                }
                if (result == null) {
                    return "Bookmark not found:" + inner;
                }
                if (script.indexOf("connect {bookmark:", pos2) >= 0) {
                    script = String.valueOf(script.substring(0, script.indexOf("connect {bookmark:", pos2))) + connect_prefix + script.substring(script.indexOf("connect {bookmark:", pos2));
                }
                script = Common.replace_str(script, inner, result);
                pos = script.indexOf("{bookmark:");
                if (script.indexOf("lconnect {bookmark:") < 0) continue;
                pos2 = script.length();
            }
            client.console_mode = false;
            Client client2 = client;
            client2.getClass();
            client.br = client2.new Client.DualReader(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(script.getBytes("UTF8")))));
            client.local_echo = true;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    long pending_sync_last_run = System.currentTimeMillis();
                    client.prefs.put("pending_sync_last_run", String.valueOf(pending_sync_last_run));
                    client.prefs.put("sync_last_run", schedule.getProperty("sync_last_run", "0"));
                    client.run();
                    schedule.put("sync_last_run", String.valueOf(pending_sync_last_run));
                    try {
                        AgentUI.this.save_prefs();
                    }
                    catch (Exception e) {
                        Client.printStackTrace(e, 1, messages2);
                    }
                    try {
                        client.process_command("QUIT", false);
                        Thread.sleep(60000L);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    AgentUI.this.clients.remove(client.uid);
                }
            });
            return String.valueOf(schedule.getProperty("scheduleName")) + " started.";
        }
        catch (Exception e) {
            Client.printStackTrace(e, 1, messages2);
            return "" + e;
        }
    }

    public static String getJsonList(Properties listingProp, boolean exif_listings, boolean simple) {
        Vector listing = (Vector)listingProp.remove("listing");
        StringBuffer sb = new StringBuffer();
        sb.append("l = new Array();\r\n");
        String s = "";
        int x = 0;
        while (x < listing.size()) {
            Properties lp = (Properties)listing.elementAt(x);
            String eol = "\r\n";
            sb.append("lp = {};\r\n");
            s = "name";
            sb.append("lp." + s + "=\"" + lp.getProperty(s, "").replaceAll("%", "%25").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\\\\", "%5C").replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D") + "\";" + eol);
            s = "dir";
            if (!simple) {
                sb.append("lp." + s + "=\"" + lp.getProperty(s, "").replaceAll("%", "%25").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\\\\", "%5C").replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D") + "\";" + eol);
            }
            s = "type";
            sb.append("lp." + s + "=\"" + lp.getProperty(s) + "\";" + eol);
            s = "root_dir";
            sb.append("lp." + s + "=\"" + lp.getProperty(s, "").replaceAll("%", "%25").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\\\\", "%5C").replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D") + "\";" + eol);
            s = "href_path";
            sb.append("lp." + s + "=\"" + lp.getProperty(s, "").replaceAll("%", "%25").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\\\\", "%5C").replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D") + "\";" + eol);
            s = "privs";
            sb.append("lp." + s + "=\"" + lp.getProperty(s).replaceAll("%", "%25").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\\\\", "%5C").replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D") + "\";" + eol);
            s = "size";
            sb.append("lp." + s + "=\"" + lp.getProperty(s) + "\";" + eol);
            s = "modified";
            sb.append("lp." + s + "=\"" + lp.getProperty(s) + "\";" + eol);
            s = "created";
            sb.append("lp." + s + "=\"" + lp.getProperty(s) + "\";" + eol);
            s = "owner";
            if (!simple) {
                sb.append("lp." + s + "=\"" + lp.getProperty(s).replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\\\\", "%5C").replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D") + "\";" + eol);
            }
            s = "group";
            if (!simple) {
                sb.append("lp." + s + "=\"" + lp.getProperty(s).replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\\\\", "%5C").replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D") + "\";" + eol);
            }
            s = "permissionsNum";
            if (!simple) {
                sb.append("lp." + s + "=\"" + lp.getProperty(s) + "\";" + eol);
            }
            s = "keywords";
            if (!simple) {
                sb.append("lp." + s + "=\"" + lp.getProperty(s, "").trim().replaceAll("%", "%25").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\\\\", "%5C").replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D").replaceAll(":", "%3A").replaceAll("\ufffd", "%E2%80%99") + "\";" + eol);
            }
            s = "permissions";
            if (!simple) {
                sb.append("lp." + s + "=\"" + lp.getProperty(s) + "\";" + eol);
            }
            s = "num_items";
            if (!simple) {
                sb.append("lp." + s + "=\"" + lp.getProperty(s) + "\";" + eol);
            }
            s = "boot";
            if (!simple) {
                sb.append("lp." + s + "=\"" + lp.getProperty(s, "false") + "\";" + eol);
            }
            s = "preview";
            if (!simple) {
                sb.append("lp." + s + "=\"" + lp.getProperty(s) + "\";" + eol);
            }
            s = "dateFormatted";
            if (!simple) {
                sb.append("lp." + s + "=\"" + lp.getProperty(s) + "\";" + eol);
            }
            s = "createdDateFormatted";
            if (!simple) {
                sb.append("lp." + s + "=\"" + lp.getProperty(s) + "\";" + eol);
            }
            s = "sizeFormatted";
            if (!simple) {
                sb.append("lp." + s + "=\"" + lp.getProperty(s) + "\";" + eol);
            }
            if (exif_listings && !simple) {
                s = "width";
                sb.append("lp." + s + "=\"" + lp.getProperty(s) + "\";" + eol);
                s = "height";
                sb.append("lp." + s + "=\"" + lp.getProperty(s) + "\";" + eol);
                Enumeration<Object> keys = lp.keys();
                while (keys.hasMoreElements()) {
                    s = "" + keys.nextElement();
                    if (!s.startsWith("crushftp_")) continue;
                    sb.append("lp." + s + "=\"" + lp.getProperty(s, "").trim().replaceAll("%", "%25").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\\\\", "%5C").replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D").replaceAll(":", "%3A").replaceAll("\ufffd", "%E2%80%99") + "\";" + eol);
                }
            }
            sb.append("l[l.length] = lp;" + eol);
            ++x;
        }
        return "\r\n<listing>" + sb.toString() + "</listing>";
    }

    public static String getJsonListObj(Properties listingProp, boolean exif_listings) {
        Vector listing = (Vector)listingProp.remove("listing");
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        String s = "";
        String eol = "\r\n";
        String parent_privs = listingProp.getProperty("privs", "NONE");
        int x = 0;
        while (x < listing.size()) {
            Properties lp = (Properties)listing.elementAt(x);
            if (x > 0) {
                sb.append(",{").append(eol);
            } else {
                sb.append("{").append(eol);
            }
            s = "name";
            sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "").replaceAll("%", "%25").replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D").replaceAll("\\n", "%0A") + "\"," + eol);
            s = "dir";
            sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "").replaceAll("%", "%25").replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D").replaceAll("\\n", "%0A") + "\"," + eol);
            s = "type";
            sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "") + "\"," + eol);
            s = "root_dir";
            sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "").replaceAll("%", "%25").replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D").replaceAll("\\n", "%0A") + "\"," + eol);
            s = "href_path";
            sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "").replaceAll("%", "%25").replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D").replaceAll("\\n", "%0A") + "\"," + eol);
            s = "privs";
            if (!lp.getProperty(s, "").equals(parent_privs)) {
                sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "").replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D").replaceAll("\\n", "%0A") + "\"," + eol);
            }
            s = "size";
            sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "") + "\"," + eol);
            s = "modified";
            sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "") + "\"," + eol);
            s = "created";
            sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "") + "\"," + eol);
            s = "owner";
            sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "").trim().replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D").replaceAll("\\n", "%0A").replaceAll("\\\\", "%5C").replaceAll(":", "%3A").replaceAll("\ufffd", "%E2%80%99") + "\"," + eol);
            s = "group";
            sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "").trim().replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D").replaceAll("\\n", "%0A").replaceAll("\\\\", "%5C").replaceAll(":", "%3A").replaceAll("\ufffd", "%E2%80%99") + "\"," + eol);
            s = "permissionsNum";
            sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "") + "\"," + eol);
            s = "keywords";
            sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "").trim().replaceAll("%", "%25").replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D").replaceAll("\\n", "%0A").replaceAll("\\\\", "%5C").replaceAll(":", "%3A").replaceAll("\ufffd", "%E2%80%99") + "\"," + eol);
            s = "permissions";
            sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "") + "\"," + eol);
            s = "num_items";
            sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "") + "\"," + eol);
            s = "preview";
            sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "") + "\"," + eol);
            if (exif_listings) {
                s = "width";
                sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "") + "\"," + eol);
                s = "height";
                sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "") + "\"," + eol);
                Enumeration<Object> keys = lp.keys();
                while (keys.hasMoreElements()) {
                    s = "" + keys.nextElement();
                    if (!s.startsWith("crushftp_")) continue;
                    sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "").trim().replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D").replaceAll("\\n", "%0A").replaceAll("\\\\", "%5C").replaceAll(":", "%3A").replaceAll("\ufffd", "%E2%80%99") + "\"," + eol);
                }
            }
            if (!lp.getProperty(s = "vfs_protocol", "").equals("")) {
                sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "") + "\"," + eol);
            }
            s = "dateFormatted";
            sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "").replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D").replaceAll("\\n", "%0A") + "\"," + eol);
            s = "createdDateFormatted";
            sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "").replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D").replaceAll("\\n", "%0A") + "\"," + eol);
            s = "sizeFormatted";
            sb.append("\"" + s + "\" : \"" + lp.getProperty(s, "").replaceAll("\"", "%22").replaceAll("\t", "%09").replaceAll("\\r", "%0D").replaceAll("\\n", "%0A") + "\"" + eol);
            sb.append("}").append(eol);
            ++x;
        }
        sb.append("]");
        return sb.toString();
    }

    public static String getStatList(Properties listingProp) {
        Vector listing = (Vector)listingProp.remove("listing");
        StringBuffer sb = new StringBuffer();
        SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        int x = 0;
        while (x < listing.size()) {
            Properties lp = (Properties)listing.elementAt(x);
            sb.append(String.valueOf(lp.getProperty("permissions")) + " " + lp.getProperty("num_items") + " " + lp.getProperty("owner") + " " + lp.getProperty("group") + " " + lp.getProperty("size") + " " + yyyyMMddHHmmss.format(new Date(Long.parseLong(lp.getProperty("modified")))) + " " + lp.getProperty("day") + " " + lp.getProperty("time_or_year") + " " + (String.valueOf(lp.getProperty("root_dir")) + lp.getProperty("name")).replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\\\\", "%5C") + "\r\n");
            ++x;
        }
        return "\r\n<listing>" + sb.toString() + "</listing>";
    }

    public static String getDmzList(Properties listingProp) {
        Vector listing = (Vector)listingProp.remove("listing");
        StringBuffer sb = new StringBuffer();
        int x = 0;
        while (x < listing.size()) {
            Properties lp = (Properties)listing.elementAt(x);
            sb.append(AgentUI.formatDmzStat(lp)).append("\r\n");
            ++x;
        }
        return "\r\n<listing>" + sb.toString() + "</listing>";
    }

    public static String formatDmzStat(Properties lp) {
        Enumeration<Object> keys = lp.keys();
        String s = "";
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            String val = String.valueOf(lp.getProperty(key));
            s = String.valueOf(s) + key + "=" + Common.url_encode(val) + ";";
        }
        return s;
    }
}

