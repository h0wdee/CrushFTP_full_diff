/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  CrushTask.Start
 *  jline.console.ConsoleReader
 *  org.boris.winrun4j.AbstractService
 *  org.boris.winrun4j.ServiceException
 */
package com.crushftp.client;

import CrushTask.Start;
import com.crushftp.client.AgentUI;
import com.crushftp.client.Common;
import com.crushftp.client.CrushDrive;
import com.crushftp.client.FTPClient;
import com.crushftp.client.File_S;
import com.crushftp.client.GenericClient;
import com.crushftp.client.HTTPClient;
import com.crushftp.client.HTTPD;
import com.crushftp.client.HeapDumper;
import com.crushftp.client.MD5Calculator;
import com.crushftp.client.URLConnection;
import com.crushftp.client.VRL;
import com.crushftp.client.Variables;
import com.crushftp.client.Worker;
import com.crushftp.tunnel2.Tunnel2;
import com.crushftp.tunnel3.StreamController;
import com.crushtunnel.gui.GUIFrame;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import jline.console.ConsoleReader;
import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.ServiceException;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class Client
extends AbstractService
implements Runnable {
    boolean console_mode = true;
    public static final String version = "1.8.22";
    String current_dest_dir = "/";
    String current_source_dir = null;
    Vector logQueue = new Vector();
    Vector agent_log = new Vector();
    public DualReader br = new DualReader(null);
    String starting_url1 = null;
    String starting_url2 = null;
    Vector retry_active = new Vector();
    Vector source_used = new Vector();
    Vector source_free = new Vector();
    Vector destination_used = new Vector();
    Vector destination_free = new Vector();
    public Properties source_credentials = new Properties();
    public Properties destination_credentials = new Properties();
    boolean source_logged_in = false;
    boolean destination_logged_in = false;
    Properties prefs = new Properties();
    Variables vars = new Variables();
    boolean local_echo = false;
    Vector recent_transfers_upload = new Vector();
    Vector recent_transfers_download = new Vector();
    Vector after_next_command = new Vector();
    boolean abort_wait = false;
    Vector messages = null;
    Vector messages2 = null;
    boolean dual_log = true;
    boolean interactive = true;
    public Vector pending_transfer_queue = new Vector();
    public Vector pending_transfer_queue_inprogress = new Vector();
    public Vector failed_transfer_queue = new Vector();
    public Vector success_transfer_queue = new Vector();
    Vector tunnel_log = new Vector();
    SimpleDateFormat log_sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
    Properties source_config = new Properties();
    Properties dest_config = new Properties();
    static final String unique_client_id = Common.makeBoundary(10);
    public Properties stats = new Properties();
    public static SimpleDateFormat log_format = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss:S");
    long client_start_time = 0L;
    String transfer_log = null;
    int additional_errors = 0;
    String uid = Common.makeBoundary(4);
    boolean transfer_threads_started = false;
    boolean validate_mode = false;
    boolean only_log = false;
    boolean single_command_line_mode = false;
    static CrushDrive drive = null;
    public Properties stats_cache_source = null;
    public Properties stats_cache_dest = null;
    static String last_line_prompt = "";
    static Object last_line_lock = new Object();

    public Client(Vector messages, Vector messages2) {
        this.messages = messages;
        this.messages2 = messages2;
        System.getProperties().put("crushftp.worker.v9", "true");
        System.getProperties().put("crushftp.v10_beta", "true");
        Common.log = this.logQueue;
    }

    public Client() {
        System.getProperties().put("crushftp.worker.v9", "true");
        System.getProperties().put("crushftp.v10_beta", "true");
        Common.log = this.logQueue;
    }

    public static void main(String[] args) {
        String appname = System.getProperty("crushclient.appname", "CrushClient");
        try {
            appname = new File(Client.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            appname = Common.last(appname);
            appname = appname.substring(0, appname.lastIndexOf("."));
            if (!appname.equalsIgnoreCase("CrushTunnel")) {
                System.setProperty("crushclient.appname", appname);
            }
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
        final Client client = new Client();
        if (args != null && args.length > 0 && (args[0].equalsIgnoreCase("COPY") || args[0].equalsIgnoreCase("MOVE") || args[0].equalsIgnoreCase("DELETE") || args[0].equalsIgnoreCase("RENAME") || args[0].equalsIgnoreCase("TEST"))) {
            client.single_command_line_mode = true;
        } else {
            System.out.println(String.valueOf(System.getProperty("crushclient.appname", "CrushClient")) + ":" + version + (System.getProperty("crushclient.appname", "CrushClient").equals("CrushClient") ? ":https://www.crushftp.com/crush10wiki/Wiki.jsp?page=CrushClient" : ""));
        }
        System.getProperties().put("crushftp.worker.v9", "true");
        System.getProperties().put("crushftp.v10_beta", "true");
        if (args != null && args.length > 0 && args[0].equalsIgnoreCase("SCRIPT")) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int x = 1;
                while (x < args.length) {
                    if (!args[x].trim().equals("")) {
                        Common.streamCopier(new FileInputStream(args[x]), baos, false, true, false);
                        baos.write("\r\n".getBytes());
                    }
                    ++x;
                }
                baos.close();
                client.br.close();
                client.console_mode = false;
                Client client2 = client;
                client2.getClass();
                client.br = client2.new DualReader(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(baos.toByteArray()))));
                client.local_echo = true;
            }
            catch (Exception e) {
                client.printStackTrace(e, 1);
            }
        } else if (args != null && args.length > 0 && args[0].equalsIgnoreCase("INLINE_SCRIPT")) {
            try {
                client.validate_mode = true;
                client.console_mode = false;
                Client client3 = client;
                client3.getClass();
                client.br = client3.new DualReader(new BufferedReader(new InputStreamReader(new ByteArrayInputStream((String.valueOf(args[1]) + ";").replaceAll(";", "\r\n").getBytes("UTF8")))));
                client.local_echo = true;
            }
            catch (Exception e) {
                client.printStackTrace(e, 1);
            }
        } else if (client.single_command_line_mode) {
            System.out.println(String.valueOf(System.getProperty("crushclient.appname", "CrushClient")) + ":" + version + ":https://www.crushftp.com/crush10wiki/Wiki.jsp?page=CrushClientSingleCommand");
            try {
                client.validate_mode = true;
                client.console_mode = false;
                Client client4 = client;
                client4.getClass();
                client.br = client4.new DualReader(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(Client.build_command_script(args).getBytes("UTF8")))));
                client.local_echo = true;
            }
            catch (Exception e) {
                client.printStackTrace(e, 1);
            }
        } else if (args != null && args.length > 0 && (args[0].equalsIgnoreCase("-H") || args[0].equalsIgnoreCase("HELP"))) {
            System.out.println("Help info can be found here:\r\nhttps://www.crushftp.com/crush10wiki/Wiki.jsp?page=CrushClientSingleCommand");
            System.exit(0);
        } else if (args != null && args.length > 0 && args[0].equalsIgnoreCase("MOUNT")) {
            try {
                drive = new CrushDrive();
                VRL vrl = new VRL(args[2]);
                String result = drive.doConnect("" + vrl, vrl.getUsername(), vrl.getPassword(), args[1], false);
                System.out.println(result);
            }
            catch (Exception e) {
                System.out.println("Syntax:java -jar CrushTunnel.jar mount drive_letter url");
                e.printStackTrace();
            }
            System.exit(0);
        } else {
            if (args != null && args.length > 0 && args[0].equalsIgnoreCase("DRIVE")) {
                try {
                    String[] args2 = null;
                    if (args.length > 1) {
                        if (args[1].equals("-d")) {
                            System.setProperty("java.awt.headless", "true");
                        } else {
                            args2 = new String[]{args[1]};
                        }
                    }
                    CrushDrive.main(args2);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                while (true) {
                    try {
                        while (true) {
                            Thread.sleep(1000L);
                        }
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                        continue;
                    }
                    break;
                }
            }
            client.setupSignalHandler();
            if (args != null && args.length > 0) {
                client.starting_url1 = args[0];
            }
            if (args != null && args.length > 1) {
                client.starting_url2 = args[1];
            }
        }
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    Thread.currentThread().setName("Logging handler");
                    int aborts = 0;
                    while (true) {
                        if (client.logQueue.size() > 0) {
                            if (client.prefs.getProperty("client_debug", "false").equals("true")) {
                                client.line("" + client.logQueue.remove(0));
                                continue;
                            }
                            client.logQueue.remove(0);
                            continue;
                        }
                        try {
                            if (StreamController.old_msg != null) {
                                if (aborts++ < 3) {
                                    Worker.startWorker(new Runnable(){

                                        @Override
                                        public void run() {
                                            try {
                                                client.abort_wait = true;
                                                client.process_command("ABOR", true);
                                            }
                                            catch (Exception e) {
                                                client.printStackTrace(e, 1);
                                            }
                                        }
                                    });
                                }
                                client.line(StreamController.old_msg);
                                Thread.sleep(3000L);
                            }
                            Thread.sleep(100L);
                        }
                        catch (Exception exception) {
                            continue;
                        }
                        break;
                    }
                }
            });
            Worker.startWorker(client);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static String build_command_script(String[] args) {
        String src_url;
        Vector<String> commands = new Vector<String>();
        int offset = 0;
        int x = 0;
        while (x < args.length) {
            String config_command = "";
            String tail = "";
            if (args[x].startsWith("-")) {
                ++offset;
                if (args[x].toUpperCase().startsWith("-O")) {
                    config_command = "set";
                }
                if (args[x].toUpperCase().startsWith("-C")) {
                    config_command = "config";
                }
                if (args[x].toUpperCase().startsWith("-LC")) {
                    config_command = "lconfig";
                }
                if (args[x].toUpperCase().startsWith("-V")) {
                    config_command = "set";
                    tail = "client_debug true";
                }
                if (args[x].toUpperCase().startsWith("-H")) {
                    System.out.println("Help info can be found here:\r\nhttps://www.crushftp.com/crush10wiki/Wiki.jsp?page=CrushClientSingleCommand");
                    config_command = "quit";
                    tail = "now";
                }
                if (args[x].toUpperCase().startsWith("-O") || args[x].toUpperCase().startsWith("-C") || args[x].toUpperCase().startsWith("-LC")) {
                    if (args[x + 1].indexOf("=") >= 0) {
                        tail = args[x + 1].trim().replace('=', ' ');
                        ++x;
                        ++offset;
                    } else {
                        throw new RuntimeException("command parameters not understood.");
                    }
                }
                commands.addElement(String.valueOf(config_command) + " " + tail);
            }
            ++x;
        }
        String dst_url = src_url = args[offset + 1];
        if (!args[0].equalsIgnoreCase("TEST")) {
            dst_url = args[offset + 2];
        }
        if (args[0].equalsIgnoreCase("RENAME") || args[0].equalsIgnoreCase("DELETE") || args[0].equalsIgnoreCase("TEST")) {
            dst_url = src_url;
        }
        commands.addElement("ldis");
        commands.addElement("lconnect " + Common.getBaseUrl(src_url));
        if (!src_url.equals(dst_url)) {
            commands.addElement("connect " + Common.getBaseUrl(dst_url));
        }
        if (args[0].equalsIgnoreCase("COPY") || args[0].equalsIgnoreCase("MOVE")) {
            commands.addElement("put \"" + new VRL(src_url).getPath() + "\" \"" + new VRL(dst_url).getPath() + "\"");
            if (args[0].equalsIgnoreCase("MOVE")) {
                commands.addElement("ldel \"" + new VRL(src_url).getPath() + "\"");
            }
        } else if (args[0].equalsIgnoreCase("RENAME")) {
            commands.addElement("lrename \"" + new VRL(src_url).getPath() + "\" \"" + args[offset + 2] + "\"");
        } else if (args[0].equalsIgnoreCase("DELETE")) {
            commands.addElement("ldelete \"" + new VRL(src_url).getPath() + "\"");
        } else if (args[0].equalsIgnoreCase("TEST")) {
            commands.addElement("ldir \"" + new VRL(src_url).getPath() + "\"");
        }
        commands.addElement("quit");
        String cs = "";
        int x2 = 0;
        while (x2 < commands.size()) {
            cs = String.valueOf(cs) + commands.elementAt(x2) + "\r\n";
            ++x2;
        }
        return cs;
    }

    public synchronized GenericClient getClient(boolean source) throws Exception {
        if (source) {
            if (this.source_free.size() > 0) {
                GenericClient c = (GenericClient)this.source_free.remove(0);
                this.source_used.addElement(c);
                c.setConfigObj(this.source_config);
                c.setCache(this.stats_cache_source);
                return c;
            }
            VRL vrl = (VRL)this.source_credentials.get("vrl");
            if (!this.only_log && !this.single_command_line_mode) {
                this.line("Creating new client (src):" + vrl.getProtocol() + "://" + vrl.getHost() + (vrl.getPort() > -1 ? ":" + vrl.getPort() : "") + "/");
            }
            GenericClient c = Common.getClient(Common.getBaseUrl(vrl.toString()), String.valueOf(System.getProperty("crushclient.appname", "CrushClient")) + ":", this.logQueue);
            c.setConfigObj(this.source_config);
            if (this.prefs.getProperty("shared_session", "false").equals("true")) {
                c.setConfigObj((Properties)this.source_config.clone());
            }
            c.login(this.source_credentials.getProperty("username"), this.source_credentials.getProperty("password"), unique_client_id);
            if (c.getConfig("crushAuth") != null) {
                this.source_config.put("crushAuth", c.getConfig("crushAuth"));
            }
            c.setConfig("no_stat", "true");
            c.setConfig("expect_100", this.prefs.getProperty("expect_100", "true"));
            c.setConfig("version", version);
            c.setCache(this.stats_cache_source);
            this.source_used.addElement(c);
            return c;
        }
        if (this.destination_free.size() > 0) {
            GenericClient c = (GenericClient)this.destination_free.remove(0);
            this.destination_used.addElement(c);
            c.setConfigObj(this.dest_config);
            c.setCache(this.stats_cache_dest);
            return c;
        }
        VRL vrl = (VRL)this.destination_credentials.get("vrl");
        if (!this.only_log && !this.single_command_line_mode) {
            this.line("Creating new client (dst):" + vrl.getProtocol() + "://" + vrl.getHost() + (vrl.getPort() > -1 ? ":" + vrl.getPort() : "") + "/");
        }
        GenericClient c = Common.getClient(Common.getBaseUrl(vrl.toString()), String.valueOf(System.getProperty("crushclient.appname", "CrushClient")) + ":", this.logQueue);
        c.setConfigObj(this.dest_config);
        if (this.prefs.getProperty("shared_session", "false").equals("true")) {
            c.setConfigObj((Properties)this.dest_config.clone());
        } else if (this.dest_config.containsKey("pgpPublicKeyUploadPath")) {
            c.setConfig("pgpEncryptUpload", "true");
            c.setConfig("pgpDecryptDownload", "true");
            c.setConfig("pgpPublicKeyUploadPath", this.dest_config.getProperty("pgpPublicKeyUploadPath"));
            c.setConfig("pgpPrivateKeyDownloadPath", this.dest_config.getProperty("pgpPrivateKeyDownloadPath"));
        }
        c.login(this.destination_credentials.getProperty("username"), this.destination_credentials.getProperty("password"), unique_client_id);
        if (c.getConfig("crushAuth") != null) {
            this.dest_config.put("crushAuth", c.getConfig("crushAuth"));
        }
        c.setConfig("no_stat", "true");
        c.setConfig("expect_100", this.prefs.getProperty("expect_100", "true"));
        c.setConfig("version", version);
        c.setCache(this.stats_cache_dest);
        this.destination_used.addElement(c);
        return c;
    }

    public GenericClient freeClient(GenericClient c) {
        if (c == null) {
            return null;
        }
        c.setConfig("transfer_direction", null);
        c.setConfig("transfer_path", null);
        c.setConfig("transfer_path_src", null);
        c.setConfig("transfer_path_dst", null);
        c.setConfig("transfer_stats", null);
        c.setConfig("transfer_start", null);
        c.setConfig("transfer_bytes_total", null);
        c.setConfig("transfer_bytes", null);
        c.setConfig("transfer_history", null);
        c.setConfig("transfer_bytes_last", null);
        c.setConfig("transfer_bytes_last_interval", null);
        if (this.source_used.indexOf(c) >= 0) {
            this.source_used.remove(c);
            this.source_free.addElement(c);
        } else if (this.destination_used.indexOf(c) >= 0) {
            this.destination_used.remove(c);
            this.destination_free.addElement(c);
        }
        return null;
    }

    public void killClient(GenericClient c) {
        if (c == null) {
            return;
        }
        this.source_used.remove(c);
        this.source_free.remove(c);
        this.destination_used.remove(c);
        this.destination_free.remove(c);
        try {
            c.logout();
        }
        catch (Exception e) {
            this.printStackTrace(e, 1);
        }
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public void run() {
        try {
            this.current_source_dir = String.valueOf(new File("./").getCanonicalPath().replace('\\', '/')) + "/";
            if (!this.current_source_dir.startsWith("/")) {
                this.current_source_dir = "/" + this.current_source_dir;
            }
            if (this.starting_url1 != null) {
                this.process_command("connect " + this.starting_url1, false);
            }
            if (this.starting_url2 != null) {
                this.process_command("lconnect " + this.starting_url2, false);
            } else {
                this.process_command("lconnect file://" + new File("./").getCanonicalPath().replace('\\', '/') + "/", false);
            }
            Thread.currentThread().setName("Command processor");
            while (true) {
                this.print_prompt();
                command = null;
                echo = this.local_echo;
                if (this.after_next_command.size() > 0) {
                    command = this.after_next_command.remove(0).toString();
                    if (command.equalsIgnoreCase("SKIP")) {
                        command = null;
                    } else {
                        echo = true;
                    }
                }
                if (command == null) {
                    command = this.br.readLine();
                }
                if (command == null) {
                    this.br = new DualReader(null);
                    command = this.br.readLine();
                }
                try {
                    Thread.currentThread().setName("Command processor:" + command);
                    this.process_command(command, echo);
                    if (!command.equalsIgnoreCase("quit") || !this.local_echo) continue;
                    return;
                }
                catch (Exception e) {
                    this.printStackTrace(e, 1);
                    if (command.equalsIgnoreCase("quit")) ** break;
                    continue;
                    this.process_command("quit now", echo);
                    this.transfer_threads_started = false;
                    return;
                }
                break;
            }
        }
        catch (Exception e) {
            this.printStackTrace(e, 1);
            if (System.getProperty("java.awt.headless", "false").equals("false")) {
                System.exit(this.failed_transfer_queue.size() + this.additional_errors);
            }
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void run_transfers() {
        try {
            do {
                Thread.currentThread().setName("Transfer thread.");
                if (this.pending_transfer_queue.size() > 0) {
                    String command = null;
                    Vector vector = this.pending_transfer_queue;
                    synchronized (vector) {
                        if (this.pending_transfer_queue.size() > 0 && (command = this.pending_transfer_queue.remove(0).toString()) != null) {
                            this.pending_transfer_queue_inprogress.addElement(command);
                        }
                    }
                    try {
                        try {
                            if (command != null) {
                                Thread.currentThread().setName("Transfer thread:" + command);
                                this.process_command(command, true);
                            }
                        }
                        catch (Exception e) {
                            this.printStackTrace(e, 1);
                            if (command == null) continue;
                            this.pending_transfer_queue_inprogress.removeElement(command);
                            continue;
                        }
                    }
                    catch (Throwable throwable) {
                        if (command != null) {
                            this.pending_transfer_queue_inprogress.removeElement(command);
                        }
                        throw throwable;
                    }
                    if (command == null) continue;
                    this.pending_transfer_queue_inprogress.removeElement(command);
                    continue;
                }
                Thread.sleep(100L);
            } while (this.transfer_threads_started);
        }
        catch (Exception e) {
            this.printStackTrace(e, 1);
        }
    }

    /*
     * Unable to fully structure code
     */
    public String[] parseCommand(String command_str) {
        v = new Vector<String>();
        last_pos = 0;
        waiting_quote = false;
        skip_next = false;
        drop_space = false;
        delete_backslashes = false;
        x = 0;
        while (x < command_str.length()) {
            block11: {
                block10: {
                    block9: {
                        c = command_str.charAt(x);
                        if (skip_next || (c != ' ' || waiting_quote) && x != command_str.length() - 1) break block9;
                        pos = x;
                        if (x == command_str.length() - 1 && !waiting_quote) {
                            ++pos;
                        }
                        if (!drop_space) {
                            if (delete_backslashes) {
                                v.addElement(Common.replace_str(command_str.substring(last_pos, pos), "\\", ""));
                            } else {
                                v.addElement(command_str.substring(last_pos, pos));
                            }
                        }
                        last_pos = x + 1;
                        drop_space = false;
                        ** GOTO lbl-1000
                    }
                    if (c != '\"' || waiting_quote) break block10;
                    last_pos = x + 1;
                    waiting_quote = true;
                    ** GOTO lbl-1000
                }
                if (c != '\"' || !waiting_quote) break block11;
                v.addElement(command_str.substring(last_pos, x));
                last_pos = x + 1;
                waiting_quote = false;
                drop_space = true;
                ** GOTO lbl-1000
            }
            if (c == '\\') {
                if (x < command_str.length() - 1 && command_str.charAt(x + 1) == ' ') {
                    skip_next = true;
                    delete_backslashes = true;
                }
            } else lbl-1000:
            // 4 sources

            {
                skip_next = false;
            }
            ++x;
        }
        command = new String[v.size()];
        x = 0;
        while (x < v.size()) {
            command[x] = this.replace_vars(v.elementAt(x).toString());
            ++x;
        }
        return command;
    }

    public String replace_vars(String s) {
        s = Common.replace_str(s, "{space}", " ");
        return s;
    }

    public String getArgs(String[] command, int i, boolean slash, boolean source) {
        String the_dir;
        String string = the_dir = source ? this.current_source_dir : this.current_dest_dir;
        if (command.length > i) {
            the_dir = command[i].trim().replace('\\', '/');
            if (the_dir.length() > 2 && the_dir.charAt(1) == ':' && !the_dir.startsWith("/")) {
                the_dir = "/" + the_dir;
            }
            if (!the_dir.startsWith("/")) {
                the_dir = String.valueOf(source ? this.current_source_dir : this.current_dest_dir) + the_dir;
            }
            if (slash && !the_dir.endsWith("/")) {
                the_dir = String.valueOf(the_dir) + "/";
            }
            the_dir = Common.dots(the_dir);
        }
        if (slash && !the_dir.endsWith("/")) {
            the_dir = String.valueOf(the_dir) + "/";
        }
        return the_dir;
    }

    public void process_command(String command_str, boolean echo, boolean multithreaded) throws Exception {
        if (echo) {
            this.print_prompt();
            this.line(command_str);
        }
        this.process_command(command_str, multithreaded);
    }

    /*
     * Opcode count of 13428 triggered aggressive code reduction.  Override with --aggressivesizethreshold.
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    public Object process_command(String command_str, boolean multithreaded) throws Exception {
        block722: {
            block735: {
                block734: {
                    block733: {
                        block732: {
                            block731: {
                                block730: {
                                    block729: {
                                        block728: {
                                            block727: {
                                                block726: {
                                                    block725: {
                                                        block724: {
                                                            block723: {
                                                                block721: {
                                                                    source = false;
                                                                    if (command_str.length() > 0) {
                                                                        v0 = source = command_str.toUpperCase().charAt(0) == 'L';
                                                                    }
                                                                    if (source && (command_str.toUpperCase().startsWith("LS") || command_str.toUpperCase().startsWith("LIST"))) {
                                                                        source = false;
                                                                    }
                                                                    keys = this.prefs.keys();
                                                                    while (keys.hasMoreElements()) {
                                                                        key = keys.nextElement().toString();
                                                                        command_str = Common.replace_str(command_str, "{" + key + "}", this.prefs.getProperty(key, ""));
                                                                    }
                                                                    try {
                                                                        command_str = Common.textFunctions(this.vars.replace_vars_line_date(command_str, this.prefs, "{", "}"), "{", "}");
                                                                    }
                                                                    catch (Exception e) {
                                                                        this.printStackTrace(e, 1);
                                                                    }
                                                                    if (command_str.startsWith("#") || command_str.toUpperCase().startsWith("REM")) {
                                                                        return null;
                                                                    }
                                                                    if (command_str.indexOf("###:") >= 0) {
                                                                        command_str = command_str.substring(0, command_str.indexOf("###:"));
                                                                    }
                                                                    if (command_str.toUpperCase().startsWith("ECHO ")) {
                                                                        this.line(command_str.substring(command_str.indexOf(" ") + 1));
                                                                        return null;
                                                                    }
                                                                    command = this.parseCommand(command_str);
                                                                    if (command_str.endsWith("&")) {
                                                                        command = this.parseCommand(command_str.substring(0, command_str.length() - 1));
                                                                        if (this.prefs.getProperty("multithreaded", "true").equals("true")) {
                                                                            the_dir1 = this.getArgs(command, 1, false, true);
                                                                            the_dir2 = this.getArgs(command, 2, false, false);
                                                                            command_str_f = String.valueOf(command[0]) + " \"" + the_dir1 + "\" \"" + the_dir2 + "\"";
                                                                            Worker.startWorker(new Runnable(){

                                                                                @Override
                                                                                public void run() {
                                                                                    try {
                                                                                        Client.this.process_command(command_str_f, true);
                                                                                    }
                                                                                    catch (Exception e) {
                                                                                        Client.this.printStackTrace(e, 1);
                                                                                    }
                                                                                }
                                                                            });
                                                                            return null;
                                                                        }
                                                                    }
                                                                    if (command.length == 0) {
                                                                        return null;
                                                                    }
                                                                    v1 = credentials = source != false ? this.source_credentials : this.destination_credentials;
                                                                    if (!command[0].toUpperCase().startsWith("SET") && !command[0].toUpperCase().startsWith("LSET")) break block721;
                                                                    this.prefs.put(command[1], command[2]);
                                                                    this.line(String.valueOf(command[1]) + " set to " + command[2]);
                                                                    break block722;
                                                                }
                                                                if (!command[0].toUpperCase().startsWith("QUEUE")) break block723;
                                                                real_q_id = "Q:" + command[1];
                                                                q_command = command[2];
                                                                if (q_command.equalsIgnoreCase("ADD") && !this.prefs.containsKey(real_q_id)) {
                                                                    this.prefs.put(real_q_id, "");
                                                                }
                                                                keys = this.prefs.keys();
                                                                while (keys.hasMoreElements()) {
                                                                    key = keys.nextElement().toString();
                                                                    if (!key.startsWith("Q:") || !real_q_id.equalsIgnoreCase("Q:all") && !key.equalsIgnoreCase(real_q_id)) continue;
                                                                    q_id = key;
                                                                    queue = this.prefs.getProperty(q_id, "");
                                                                    if (q_command.equalsIgnoreCase("ADD")) {
                                                                        queue = String.valueOf(queue) + command_str.substring(command_str.indexOf(" ", command_str.indexOf(" ", 7) + 1) + 1) + "\r\n";
                                                                    } else if (q_command.equalsIgnoreCase("CLEAR") || q_command.equalsIgnoreCase("RESET")) {
                                                                        queue = "";
                                                                    } else if (q_command.equalsIgnoreCase("EXECUTE") || q_command.equalsIgnoreCase("RUN")) {
                                                                        queue_f = queue;
                                                                        Worker.startWorker(new Runnable(){

                                                                            @Override
                                                                            public void run() {
                                                                                try {
                                                                                    BufferedReader br2 = new BufferedReader(new StringReader(queue_f));
                                                                                    String data = "";
                                                                                    while ((data = br2.readLine()) != null) {
                                                                                        Client.this.process_command(data, false);
                                                                                    }
                                                                                }
                                                                                catch (Exception e) {
                                                                                    Client.this.printStackTrace(e, 1);
                                                                                }
                                                                            }
                                                                        }, "Running queue:" + q_id);
                                                                    }
                                                                    this.prefs.put(q_id, queue);
                                                                }
                                                                break block722;
                                                            }
                                                            if (command[0].toUpperCase().startsWith("CONNECT") || command[0].toUpperCase().startsWith("LCONNECT") || command[0].toUpperCase().startsWith("VALIDATE")) {
                                                                if (command[0].toUpperCase().startsWith("VALIDATE")) {
                                                                    this.validate_mode = true;
                                                                }
                                                                if (source != false ? this.source_logged_in == false : this.destination_logged_in == false) {
                                                                    if (this.single_command_line_mode && this.prefs.getProperty("segmented_transfer", "true").equals("true")) {
                                                                        this.dest_config.put("multi_segmented_download", "true");
                                                                        this.dest_config.put("multi", "true");
                                                                        this.source_config.put("multi_segmented_download", "true");
                                                                        this.source_config.put("multi", "true");
                                                                    } else if (this.single_command_line_mode && this.prefs.getProperty("segmented_transfer", "true").equals("false")) {
                                                                        this.dest_config.put("multi_segmented_download", "false");
                                                                        this.dest_config.put("multi", "false");
                                                                        this.source_config.put("multi_segmented_download", "false");
                                                                        this.source_config.put("multi", "false");
                                                                    }
                                                                    was_tunnel = false;
                                                                    if (this.prefs.getProperty("use_tunnel", "").equals("true")) {
                                                                        this.prefs.remove("use_tunnel");
                                                                        this.process_command("TUNNEL " + command[1], false);
                                                                        command[1] = "tunnel";
                                                                    }
                                                                    if (command[1].equalsIgnoreCase("tunnel")) {
                                                                        was_tunnel = true;
                                                                        command[1] = "http://" + VRL.vrlEncode(this.prefs.getProperty("last_tunnel_username")) + ":" + VRL.vrlEncode(this.prefs.getProperty("last_tunnel_password")) + "@127.0.0.1:" + this.prefs.getProperty("last_tunnel_port") + "/";
                                                                    }
                                                                    if (!command[1].endsWith("/")) {
                                                                        command[1] = String.valueOf(command[1]) + "/";
                                                                    }
                                                                    vrl = new VRL(command[1]);
                                                                    if (source) {
                                                                        this.current_source_dir = vrl.getPath();
                                                                    } else {
                                                                        this.current_dest_dir = vrl.getPath();
                                                                    }
                                                                    v2 = username = vrl.getUserInfo() != null && vrl.getUserInfo().equals("") != false ? "" : vrl.getUsername();
                                                                    if ((username == null || username.equals("")) && !vrl.getProtocol().equalsIgnoreCase("file") && this.interactive) {
                                                                        Client.last_line_prompt = "message";
                                                                        System.out.print("(" + vrl + ") Username: ");
                                                                        username = this.br.readLine();
                                                                    }
                                                                    v3 = password = vrl.getUserInfo() != null && vrl.getUserInfo().equals("") != false ? "" : vrl.getPassword();
                                                                    if ((password == null || password.equals("") || password.equalsIgnoreCase("-ASK-")) && !vrl.getProtocol().equalsIgnoreCase("file") && this.interactive) {
                                                                        Client.last_line_prompt = "message";
                                                                        System.out.print("(" + vrl + ") Password: ");
                                                                        if (password != null && password.equalsIgnoreCase("-ASK-")) {
                                                                            if (this.single_command_line_mode) {
                                                                                this.console_mode = true;
                                                                            }
                                                                            password = new DualReader(null).readPassword();
                                                                            if (this.single_command_line_mode) {
                                                                                this.console_mode = false;
                                                                            }
                                                                        } else {
                                                                            password = this.br.readPassword();
                                                                        }
                                                                    }
                                                                    if (was_tunnel) {
                                                                        credentials.put("display_vrl", new VRL(this.prefs.getProperty("last_tunnel_url")).safe());
                                                                    } else {
                                                                        credentials.put("display_vrl", vrl.safe());
                                                                    }
                                                                    credentials.put("vrl", vrl);
                                                                    credentials.put("username", username == null && vrl.getProtocol().equalsIgnoreCase("HTTPS") != false && this.dest_config.getProperty("keystore_path", "").equals("") == false ? "" : username);
                                                                    credentials.put("password", password == null && vrl.getProtocol().equalsIgnoreCase("HTTPS") != false && this.dest_config.getProperty("keystore_path", "").equals("") == false ? "" : password);
                                                                    c = null;
                                                                    try {
                                                                        if (!source && credentials.containsKey("pgpPublicKeyUploadPath")) {
                                                                            this.dest_config.put("pgpEncryptUpload", "true");
                                                                            this.dest_config.put("pgpDecryptDownload", "true");
                                                                            this.dest_config.put("pgpPublicKeyUploadPath", credentials.getProperty("pgpPublicKeyUploadPath"));
                                                                            this.dest_config.put("pgpPrivateKeyDownloadPath", credentials.getProperty("pgpPrivateKeyDownloadPath"));
                                                                        }
                                                                        c = this.getClient(source);
                                                                        if (vrl.getProtocol().toUpperCase().startsWith("FTP")) {
                                                                            c.setConfig("pasv", "true");
                                                                        }
                                                                        if (vrl.getProtocol().toUpperCase().startsWith("HTTP")) {
                                                                            try {
                                                                                hc = (HTTPClient)c;
                                                                                max_threads = Integer.parseInt(((Properties)Common.readXMLObject(new ByteArrayInputStream(hc.doAction("getUserInfo", "", "").getBytes("UTF8")))).getProperty("max_threads", "10"));
                                                                                if (max_threads == 0) {
                                                                                    max_threads = 100;
                                                                                }
                                                                                if (Integer.parseInt(this.prefs.getProperty("max_threads", "5")) > max_threads) {
                                                                                    this.line("max_threads lowered to server limit:" + max_threads);
                                                                                    if (max_threads == 1) {
                                                                                        max_threads = 2;
                                                                                    }
                                                                                    this.prefs.put("max_threads", String.valueOf(max_threads - 1));
                                                                                }
                                                                                this.dest_config.put("multi_segmented_download_threads", this.prefs.getProperty("multi_segmented_download_threads", "20"));
                                                                                this.dest_config.put("multi_segmented_upload_threads", this.prefs.getProperty("multi_segmented_upload_threads", "10"));
                                                                                this.line("Using " + this.prefs.getProperty("multi_segmented_download_threads", "20") + " threads for segmented downloads and " + this.prefs.getProperty("multi_segmented_upload_threads", "10") + " for segmented uploads.");
                                                                            }
                                                                            catch (Exception hc) {
                                                                                // empty catch block
                                                                            }
                                                                        }
                                                                        this.freeClient(c);
                                                                        if (source) {
                                                                            this.source_logged_in = true;
                                                                        } else {
                                                                            this.destination_logged_in = true;
                                                                        }
                                                                        if (was_tunnel) {
                                                                            this.line("Connect to:" + new VRL(this.prefs.getProperty("last_tunnel_url")).safe());
                                                                        } else if (!this.single_command_line_mode) {
                                                                            this.line("Connected to:" + vrl.safe());
                                                                        }
                                                                        return "true";
                                                                    }
                                                                    catch (Exception e) {
                                                                        this.printStackTrace(e, 1);
                                                                        error_str = String.valueOf(e.getMessage());
                                                                        c_tmp = c;
                                                                        Worker.startWorker(new Runnable(){

                                                                            @Override
                                                                            public void run() {
                                                                                try {
                                                                                    Client.this.killClient(c_tmp);
                                                                                }
                                                                                catch (Exception exception) {
                                                                                    // empty catch block
                                                                                }
                                                                            }
                                                                        });
                                                                        c = null;
                                                                        if (error_str.indexOf(":") >= 0) {
                                                                            error_str = error_str.substring(error_str.indexOf(":") + 1).trim();
                                                                        }
                                                                        if (error_str.toUpperCase().indexOf("EXCEPTION") >= 0 && error_str.indexOf(":") >= 0) {
                                                                            error_str = error_str.substring(error_str.indexOf(":") + 1).trim();
                                                                        }
                                                                        if (error_str.indexOf("<message>") >= 0) {
                                                                            error_str = error_str.substring(error_str.indexOf("<message>") + "<message>".length(), error_str.indexOf("</message>"));
                                                                        }
                                                                        if ((error_str = Common.url_decode(error_str)).indexOf("Expected 230 but got 530 Access denied") >= 0) {
                                                                            error_str = "Login failed";
                                                                        }
                                                                        if (error_str.indexOf("Check your username or password and try again") >= 0) {
                                                                            error_str = "Login failed";
                                                                        }
                                                                        if (error_str.indexOf("SFTP login failed") >= 0) {
                                                                            error_str = "Login failed";
                                                                        }
                                                                        error_str = "ERROR: " + error_str;
                                                                        br = new BufferedReader(new StringReader(error_str));
                                                                        data = "";
                                                                        ** while ((data = br.readLine()) != null)
                                                                    }
lbl-1000:
                                                                    // 1 sources

                                                                    {
                                                                        this.line(data);
                                                                        continue;
                                                                    }
lbl218:
                                                                    // 1 sources

                                                                    if (this.single_command_line_mode) {
                                                                        System.exit(59);
                                                                    }
                                                                    return error_str;
                                                                }
                                                                this.line("Already connected, disconnect first.");
                                                                return "false";
                                                            }
                                                            if (!command[0].toUpperCase().startsWith("PBE")) break block724;
                                                            pbe_pass = Common.encryptDecrypt(command[1], true);
                                                            this.prefs.put("md5_check", "false");
                                                            this.prefs.put("skip_modified_and_size", "false");
                                                            this.prefs.put("skip_modified", "true");
                                                            credentials.put("pgpPublicKeyUploadPath", "password:" + pbe_pass);
                                                            credentials.put("pgpPrivateKeyDownloadPath", "password:" + pbe_pass);
                                                            break block722;
                                                        }
                                                        if (!command[0].toUpperCase().startsWith("DELAY")) break block725;
                                                        Thread.sleep(Integer.parseInt(command[1]));
                                                        break block722;
                                                    }
                                                    if (!command[0].toUpperCase().startsWith("QUI") && !command[0].toUpperCase().startsWith("BYE") && !command[0].toUpperCase().startsWith("LQUI") && !command[0].toUpperCase().startsWith("LBYE") && !command[0].toUpperCase().startsWith("TERMINATE")) break block726;
                                                    end_time = System.currentTimeMillis();
                                                    if (this.local_echo && command_str.toUpperCase().indexOf("NOW") < 0) {
                                                        Thread.sleep(1000L);
                                                        this.process_command("WAIT", true);
                                                    }
                                                    transfer_history_bytes = Long.parseLong(this.stats.getProperty("upload_bytes", "0")) + Long.parseLong(this.stats.getProperty("download_bytes", "0"));
                                                    status = new StringBuffer();
                                                    Worker.startWorker(new Runnable(){

                                                        @Override
                                                        public void run() {
                                                            try {
                                                                GenericClient c_tmp;
                                                                while (Client.this.source_used.size() > 0) {
                                                                    c_tmp = (GenericClient)Client.this.source_used.remove(0);
                                                                    c_tmp.close();
                                                                    Client.this.freeClient(c_tmp);
                                                                }
                                                                while (Client.this.destination_used.size() > 0) {
                                                                    c_tmp = (GenericClient)Client.this.destination_used.remove(0);
                                                                    c_tmp.close();
                                                                    Client.this.freeClient(c_tmp);
                                                                }
                                                                while (Client.this.source_free.size() > 0) {
                                                                    c_tmp = (GenericClient)Client.this.source_free.remove(0);
                                                                    Client.this.killClient(c_tmp);
                                                                }
                                                                while (Client.this.destination_free.size() > 0) {
                                                                    c_tmp = (GenericClient)Client.this.destination_free.remove(0);
                                                                    Client.this.killClient(c_tmp);
                                                                }
                                                                Client.this.source_logged_in = false;
                                                                Client.this.destination_logged_in = false;
                                                                if (!Client.this.single_command_line_mode) {
                                                                    Client.this.line("Logged out.");
                                                                }
                                                                if (!Client.this.single_command_line_mode) {
                                                                    Client.this.line("Goodbye.");
                                                                }
                                                                Client.this.transfer_threads_started = false;
                                                            }
                                                            catch (Exception e) {
                                                                Client.this.printStackTrace(e, 1);
                                                            }
                                                            status.append("done");
                                                        }
                                                    });
                                                    loops = 0;
                                                    while (loops++ < 50 && status.length() == 0) {
                                                        Thread.sleep(100L);
                                                    }
                                                    this.printStats(true);
                                                    if (this.client_start_time == 0L) {
                                                        this.client_start_time = end_time - 2000L;
                                                    }
                                                    speed = (float)transfer_history_bytes / ((float)(end_time - this.client_start_time) / 1000.0f);
                                                    this.line(this.stats_summary());
                                                    this.stats.put("total_time", Common.format_time_pretty((end_time - this.client_start_time) / 1000L));
                                                    this.stats.put("total_speed", Common.format_bytes_short((long)speed));
                                                    if (!this.single_command_line_mode) {
                                                        this.line("Total time:" + this.stats.getProperty("total_time") + ", Avg Speed:" + this.stats.getProperty("total_speed"));
                                                    }
                                                    this.print_prompt();
                                                    if (!this.interactive && !command[0].toUpperCase().startsWith("TERMINATE") || !System.getProperty("java.awt.headless", "false").equals("false")) break block722;
                                                    if (this.validate_mode) {
                                                        System.exit(this.additional_errors);
                                                    } else {
                                                        System.exit(0);
                                                    }
                                                    break block722;
                                                }
                                                if (!command[0].toUpperCase().startsWith("DIS") && !command[0].toUpperCase().startsWith("LDIS")) break block727;
                                                if (source) {
                                                    this.source_config.clear();
                                                } else if (this.interactive) {
                                                    this.dest_config.clear();
                                                }
                                                credentials.remove("pgpPublicKeyUploadPath");
                                                credentials.remove("pgpPrivateKeyDownloadPath");
                                                if (!(source != false ? this.source_logged_in == false : this.destination_logged_in == false)) ** GOTO lbl293
                                                return this.line("Not connected.");
lbl-1000:
                                                // 1 sources

                                                {
                                                    c_tmp = (GenericClient)this.source_used.remove(0);
                                                    c_tmp.close();
                                                    this.freeClient(c_tmp);
lbl293:
                                                    // 2 sources

                                                    ** while (source && this.source_used.size() > 0)
                                                }
lbl294:
                                                // 2 sources

                                                while (!source && this.destination_used.size() > 0) {
                                                    c_tmp = (GenericClient)this.destination_used.remove(0);
                                                    c_tmp.close();
                                                    this.freeClient(c_tmp);
                                                }
                                                while (source && this.source_free.size() > 0) {
                                                    c_tmp = (GenericClient)this.source_free.remove(0);
                                                    this.killClient(c_tmp);
                                                }
                                                while (!source && this.destination_free.size() > 0) {
                                                    c_tmp = (GenericClient)this.destination_free.remove(0);
                                                    this.killClient(c_tmp);
                                                }
                                                tunnel_list = (Vector)this.prefs.get("tunnel_list");
                                                if (tunnel_list != null && tunnel_list.size() > 0) {
                                                    this.process_command("TUNNEL stop 1", false);
                                                }
                                                if (source) {
                                                    this.source_logged_in = false;
                                                } else {
                                                    this.destination_logged_in = false;
                                                }
                                                if (!this.single_command_line_mode) {
                                                    return this.line("Logged out.");
                                                }
                                                return "";
                                            }
                                            if (!command[0].toUpperCase().startsWith("ABOR")) break block728;
                                            this.prefs.put("aborting", "true");
                                            this.pending_transfer_queue.removeAllElements();
                                            this.pending_transfer_queue_inprogress.removeAllElements();
                                            this.retry_active.removeAllElements();
                                            while (this.source_used.size() > 0) {
                                                try {
                                                    c_tmp = (GenericClient)this.source_used.remove(0);
                                                    c_tmp.setConfig("abort_obj", c_tmp);
                                                    c_tmp.close();
                                                    this.freeClient(c_tmp);
                                                }
                                                catch (Exception e) {
                                                    this.printStackTrace(e, 1);
                                                }
                                            }
                                            while (this.destination_used.size() > 0) {
                                                try {
                                                    c_tmp = (GenericClient)this.destination_used.remove(0);
                                                    c_tmp.setConfig("abort_obj", c_tmp);
                                                    c_tmp.close();
                                                    this.freeClient(c_tmp);
                                                }
                                                catch (Exception e) {
                                                    this.printStackTrace(e, 1);
                                                }
                                            }
                                            Worker.startWorker(new Runnable(){

                                                @Override
                                                public void run() {
                                                    try {
                                                        Thread.sleep(3000L);
                                                    }
                                                    catch (InterruptedException interruptedException) {
                                                        // empty catch block
                                                    }
                                                    Client.this.prefs.remove("aborting");
                                                }
                                            });
                                            this.line("Aborting transfers...");
                                            Thread.sleep(1000L);
                                            this.line("Transfers aborted if any were running.");
                                            Thread.sleep(1000L);
                                            this.print_prompt();
                                            break block722;
                                        }
                                        if (!command[0].toUpperCase().startsWith("AGENTUI")) break block729;
                                        if (AgentUI.thisObj == null) {
                                            if (command.length > 1 && command[1].toUpperCase().startsWith("HTTP")) {
                                                if (!command[1].trim().endsWith("/")) {
                                                    command[1] = String.valueOf(command[1].trim()) + "/";
                                                }
                                                System.getProperties().put("com.crushftp.server.httphandler.path", command[1].trim());
                                            }
                                            this.messages = new Vector<E>();
                                            this.messages2 = AgentUI.messages2;
                                            this.dual_log = true;
                                            System.setProperty("com.crushftp.server.httphandler", "com.crushftp.client.AgentUI");
                                            Worker.startWorker(new Runnable(){

                                                @Override
                                                public void run() {
                                                    HTTPD.main(new String[0]);
                                                }
                                            });
                                            while (AgentUI.thisObj == null && !this.prefs.containsKey("aborting")) {
                                                Thread.sleep(100L);
                                            }
                                            AgentUI.thisObj.clients.put("command_line", this);
                                            this.line("WebUI started on port 33333.");
                                        } else {
                                            this.line("WebUI already started on port 33333.");
                                            Desktop.getDesktop().browse(new URI("http://127.0.0.1:33333/"));
                                        }
                                        this.print_prompt();
                                        break block722;
                                    }
                                    if (!command[0].toUpperCase().startsWith("AGENT")) break block730;
                                    if (AgentUI.thisObj == null) {
                                        if (command.length > 1 && command[1].toUpperCase().startsWith("HTTP")) {
                                            if (!command[1].trim().endsWith("/")) {
                                                command[1] = String.valueOf(command[1].trim()) + "/";
                                            }
                                            System.getProperties().put("com.crushftp.server.httphandler.path", command[1].trim());
                                        }
                                        this.messages = new Vector<E>();
                                        this.messages2 = AgentUI.messages2;
                                        this.dual_log = true;
                                        Worker.startWorker(new Runnable(){

                                            @Override
                                            public void run() {
                                                AgentUI.main(new String[0]);
                                            }
                                        });
                                        while (AgentUI.thisObj == null && !this.prefs.containsKey("aborting")) {
                                            Thread.sleep(100L);
                                        }
                                        AgentUI.thisObj.clients.put("command_line", this);
                                        this.line("WebUI started on port 33333.");
                                    } else {
                                        this.line("WebUI already started on port 33333.");
                                        Desktop.getDesktop().browse(new URI("http://127.0.0.1:33333/"));
                                    }
                                    this.print_prompt();
                                    break block722;
                                }
                                if (!command[0].toUpperCase().startsWith("LOG")) break block731;
                                this.transfer_log = this.getArgs(command, 1, true, source);
                                if (this.transfer_log.equalsIgnoreCase("NULL") || this.transfer_log.trim().equals("")) {
                                    this.line("Transfer logging disabled.");
                                    this.transfer_log = null;
                                } else {
                                    this.line("Transfer logging enabled:" + this.transfer_log);
                                }
                                this.print_prompt();
                                break block722;
                            }
                            if (!command[0].toUpperCase().startsWith("LOCAL_ECHO")) break block732;
                            this.local_echo = false;
                            this.line("Local Echo:" + this.local_echo);
                            this.print_prompt();
                            break block722;
                        }
                        if (!command[0].toUpperCase().startsWith("SERVICE")) break block733;
                        if (command.length > 1 && command[1].toUpperCase().startsWith("REMOVE")) {
                            extra_name = "";
                            if (command[2].indexOf("_") > 0) {
                                extra_name = command[2].substring(command[2].indexOf("_") + 1);
                            }
                            if (command[2].toUpperCase().startsWith("TUNNEL")) {
                                Common.remove_windows_service("CrushTunnel" + extra_name, "CrushTunnel.jar");
                            } else if (command[2].toUpperCase().startsWith("CLIENT")) {
                                Common.remove_windows_service(String.valueOf(System.getProperty("crushclient.appname", "CrushClient")) + extra_name, "CrushTunnel.jar");
                            } else if (command[2].toUpperCase().startsWith("DRIVE")) {
                                Common.remove_windows_service("CrushDrive" + extra_name, "CrushTunnel.jar");
                            } else if (command[2].toUpperCase().startsWith("SYNC")) {
                                Common.remove_windows_service("CrushSync" + extra_name, "CrushTunnel.jar");
                            }
                            new File_S("./service/elevate.exe").delete();
                            this.line(String.valueOf(command[2]) + extra_name + " service removed.");
                        } else if (command.length > 1 && command[1].toUpperCase().startsWith("CLIENT")) {
                            extra_name = "";
                            if (command[1].indexOf("_") > 0) {
                                extra_name = command[1].substring(command[1].indexOf("_") + 1);
                            }
                            Common.install_windows_service(512, String.valueOf(System.getProperty("crushclient.appname", "CrushClient")) + extra_name, "CrushTunnel.jar", false);
                            baos = new ByteArrayOutputStream();
                            Common.copyStreams(new FileInputStream("service/" + System.getProperty("crushclient.appname", "CrushClient") + extra_name + "Service.ini"), baos, true, true);
                            config = new String(baos.toByteArray());
                            new_args = "";
                            config = Common.replace_str(config, "arg.1=-d", new_args);
                            Common.copyStreams(new ByteArrayInputStream(config.getBytes()), new FileOutputStream("service/" + System.getProperty("crushclient.appname", "CrushClient") + extra_name + "Service.ini", false), true, true);
                            Common.startDaemon(true, String.valueOf(System.getProperty("crushclient.appname", "CrushClient")) + extra_name);
                            Thread.sleep(500L);
                            new File("./service/elevate.exe").delete();
                            this.line("Client" + extra_name + " service installed.");
                        } else if (command.length > 1 && command[1].toUpperCase().startsWith("DRIVE")) {
                            extra_name = "";
                            if (command[1].indexOf("_") > 0) {
                                extra_name = command[1].substring(command[1].indexOf("_") + 1);
                            }
                            Common.install_windows_service(512, "CrushDrive" + extra_name, "CrushTunnel.jar", false);
                            baos = new ByteArrayOutputStream();
                            Common.copyStreams(new FileInputStream("service/CrushDrive" + extra_name + "Service.ini"), baos, true, true);
                            config = new String(baos.toByteArray());
                            new_args = "arg.1=-d";
                            config = Common.replace_str(config, "arg.1=-d", new_args);
                            config = String.valueOf(config.substring(0, config.indexOf("vmarg.1="))) + "vmarg.1=-Dfile.encoding=UTF-8" + config.substring(config.indexOf("\r\n", config.indexOf("vmarg.1=")));
                            Common.copyStreams(new ByteArrayInputStream(config.getBytes()), new FileOutputStream("service/CrushDrive" + extra_name + "Service.ini", false), true, true);
                            Common.startDaemon(true, "CrushDrive" + extra_name);
                            Thread.sleep(500L);
                            new File("./service/elevate.exe").delete();
                            this.line("Drive" + extra_name + " service installed.");
                        } else if (command.length > 1 && command[1].toUpperCase().startsWith("SYNC")) {
                            extra_name = "";
                            if (command[1].indexOf("_") > 0) {
                                extra_name = command[1].substring(command[1].indexOf("_") + 1);
                            }
                            Common.install_windows_service(512, "CrushSync" + extra_name, "CrushTunnel.jar", false);
                            baos = new ByteArrayOutputStream();
                            Common.copyStreams(new FileInputStream("service/CrushSync" + extra_name + "Service.ini"), baos, true, true);
                            config = new String(baos.toByteArray());
                            new_args = "arg.1=-d";
                            config = Common.replace_str(config, "arg.1=-d", new_args);
                            config = String.valueOf(config.substring(0, config.indexOf("vmarg.1="))) + "vmarg.1=-Dfile.encoding=UTF-8" + config.substring(config.indexOf("\r\n", config.indexOf("vmarg.1=")));
                            Common.copyStreams(new ByteArrayInputStream(config.getBytes()), new FileOutputStream("service/CrushSync" + extra_name + "Service.ini", false), true, true);
                            Common.startDaemon(true, "CrushSync" + extra_name);
                            Thread.sleep(500L);
                            new File("./service/elevate.exe").delete();
                            this.line("Sync" + extra_name + " service installed.");
                        } else if (command.length > 1 && command[1].toUpperCase().startsWith("TUNNEL")) {
                            if (command.length < 7) {
                                this.line("Invalid parameters.");
                                this.line("service tunnel {protocol} {host} {port} {username} {password}");
                                this.line("Example: ");
                                this.line("service tunnel https www.crushftp.com demo demo");
                            } else {
                                extra_name = "";
                                if (command[1].indexOf("_") > 0) {
                                    extra_name = command[1].substring(command[1].indexOf("_") + 1);
                                }
                                Common.install_windows_service(512, "CrushTunnel" + extra_name, "CrushTunnel.jar", false);
                                baos = new ByteArrayOutputStream();
                                Common.copyStreams(new FileInputStream("service/CrushTunnel" + extra_name + "Service.ini"), baos, true, true);
                                config = new String(baos.toByteArray());
                                new_args = "";
                                new_args = String.valueOf(new_args) + "arg.1=protocol=" + command[2] + "\r\n";
                                new_args = String.valueOf(new_args) + "arg.2=host=" + command[3] + "\r\n";
                                new_args = String.valueOf(new_args) + "arg.3=port=" + command[4] + "\r\n";
                                new_args = String.valueOf(new_args) + "arg.4=username=" + command[5] + "\r\n";
                                new_args = String.valueOf(new_args) + "arg.5=password=" + command[6] + "\r\n";
                                config = Common.replace_str(config, "arg.1=-d", new_args);
                                Common.copyStreams(new ByteArrayInputStream(config.getBytes()), new FileOutputStream("service/CrushTunnel" + extra_name + "Service.ini", false), true, true);
                                Common.startDaemon(true, "CrushTunnel" + extra_name);
                                Thread.sleep(500L);
                                new File("./service/elevate.exe").delete();
                                this.line("Tunnel" + extra_name + " service installed.");
                            }
                        }
                        this.print_prompt();
                        break block722;
                    }
                    if (!command[0].equals("umount") && !command[0].equals("unmount")) break block734;
                    try {
                        if (Client.drive != null) {
                            result = Client.drive.doDisconnect();
                            this.line(result);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    this.line("Unmounted");
                    this.print_prompt();
                    break block722;
                }
                if (!command[0].equals("mount")) break block735;
                if (Client.drive != null) {
                    try {
                        Client.drive.doDisconnect();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                letter_tmp = command.length > 1 ? command[1] : "Z";
                try {
                    Client.drive = new CrushDrive();
                    vrl = (VRL)this.destination_credentials.get("vrl");
                    result = Client.drive.doConnect("" + vrl, vrl.getUsername(), vrl.getPassword(), letter_tmp, false);
                    this.line(result);
                    this.line("Mounted");
                }
                catch (Exception e) {
                    this.line("" + e);
                }
                this.print_prompt();
                break block722;
            }
            if (command[0].toUpperCase().startsWith("CONFIG") || command[0].toUpperCase().startsWith("LCONFIG")) {
                key = command[1];
                value = "";
                if (key.equalsIgnoreCase("reset")) {
                    if (source) {
                        this.source_config.clear();
                    } else {
                        this.dest_config.clear();
                    }
                } else {
                    value = command[2];
                    if (key.equals("keystore_pass") || key.equals("truststore_pass")) {
                        value = Common.encryptDecrypt(value, true);
                    }
                    if (source != false ? this.source_logged_in == false : this.destination_logged_in == false) {
                        if (source) {
                            this.source_config.put(key, value);
                        } else {
                            this.dest_config.put(key, value);
                        }
                    } else {
                        c = null;
                        try {
                            c = this.getClient(source);
                        }
                        catch (NullPointerException new_args) {
                            // empty catch block
                        }
                        c.setConfig(key, value);
                        this.freeClient(c);
                    }
                }
                return this.line("\"" + key + "\" -> \"" + value + "\": config command successful.");
            }
            if (command[0].toUpperCase().startsWith("VMPROP")) {
                key = command[1];
                value = "";
                System.getProperties().put(key, command[2]);
                return this.line("\"" + key + "\" -> \"" + value + "\": System Property configured.");
            }
            c = null;
            try {
                c = this.getClient(source);
            }
            catch (NullPointerException value) {
                // empty catch block
            }
            try {
                block739: {
                    block741: {
                        block742: {
                            block740: {
                                block737: {
                                    block736: {
                                        if (command[0].toUpperCase().startsWith("STAT") || command[0].toUpperCase().startsWith("LSTAT")) {
                                            if (source != false ? this.source_logged_in == false : this.destination_logged_in == false) {
                                                this.line("Not connected.");
                                                return null;
                                            }
                                            the_dir = this.getArgs(command, 1, true, source);
                                            stat = null;
                                            try {
                                                stat = c.stat(the_dir);
                                            }
                                            catch (Exception e) {
                                                this.printStackTrace(e, 1);
                                                var63_215 = "ERROR:" + e;
                                                this.freeClient(c);
                                                return var63_215;
                                            }
                                            Client.last_line_prompt = "message";
                                            if (stat != null) {
                                                var63_216 = this.line(Client.format_ls_la(stat));
                                                return var63_216;
                                            }
                                            var63_217 = this.line("Error: Not found.");
                                            return var63_217;
                                        }
                                        if (command[0].toUpperCase().startsWith("DIR") || command[0].toUpperCase().startsWith("LIST") || command[0].toUpperCase().startsWith("LS") || command[0].toUpperCase().startsWith("LDIR") || command[0].toUpperCase().startsWith("LLIST") || command[0].toUpperCase().startsWith("LLS")) {
                                            if (source != false ? this.source_logged_in == false : this.destination_logged_in == false) {
                                                this.line("Not connected.");
                                                return null;
                                            }
                                            the_dir = this.getArgs(command, 1, true, source);
                                            list = new Vector<E>();
                                            listingProp = new Properties();
                                            try {
                                                if (c instanceof HTTPClient) {
                                                    listingProp = ((HTTPClient)c).list2(the_dir, list);
                                                } else {
                                                    c.list(the_dir, list);
                                                }
                                                listingProp.put("listing", list);
                                            }
                                            catch (Exception e) {
                                                this.printStackTrace(e, 1);
                                                var63_218 = "ERROR:" + e;
                                                this.freeClient(c);
                                                return var63_218;
                                            }
                                            Client.last_line_prompt = "message";
                                            if (this.interactive) {
                                                x = 0;
                                                while (x < list.size()) {
                                                    this.line(Client.format_ls_la((Properties)list.elementAt(x)));
                                                    ++x;
                                                }
                                            }
                                            var63_219 = listingProp;
                                            return var63_219;
                                        }
                                        if (command[0].toUpperCase().startsWith("PASV") || command[0].toUpperCase().startsWith("LPASV")) {
                                            if (source != false ? this.source_logged_in == false : this.destination_logged_in == false) {
                                                this.line("Not connected.");
                                                return null;
                                            }
                                            if (c.getConfig("pasv") != null) {
                                                c.setConfig("pasv", String.valueOf(c.getConfig("pasv").toString().equals("true") == false));
                                                var63_220 = this.line("Passive enabled: " + c.getConfig("pasv"));
                                                return var63_220;
                                            }
                                            var63_221 = this.line("The protocol does not have or need passive mode.");
                                            return var63_221;
                                        }
                                        if (command[0].toUpperCase().startsWith("CD") || command[0].toUpperCase().startsWith("CWD") || command[0].toUpperCase().startsWith("LCD") || command[0].toUpperCase().startsWith("LCWD")) {
                                            if (source != false ? this.source_logged_in == false : this.destination_logged_in == false) {
                                                this.line("Not connected.");
                                                return null;
                                            }
                                            the_dir = this.getArgs(command, 1, true, source);
                                            stat = null;
                                            if (!the_dir.equals("/")) {
                                                stat = c.stat(the_dir);
                                            }
                                            if (stat == null && !the_dir.equals("/")) {
                                                if (this.local_echo) {
                                                    this.br.close();
                                                }
                                                var63_222 = this.line(String.valueOf(command[0]) + " \"" + the_dir + "\": No such file or directory.");
                                                return var63_222;
                                            }
                                            if (source) {
                                                this.current_source_dir = the_dir;
                                            } else {
                                                this.current_dest_dir = the_dir;
                                            }
                                            var63_223 = this.line("\"" + the_dir + "\" CWD command successful.");
                                            return var63_223;
                                        }
                                        if (command[0].toUpperCase().startsWith("PWD") || command[0].toUpperCase().startsWith("LPWD")) {
                                            if (source != false ? this.source_logged_in == false : this.destination_logged_in == false) {
                                                this.line("Not connected.");
                                                return null;
                                            }
                                            var63_224 = this.line("\"" + (source != false ? this.current_source_dir : this.current_dest_dir) + "\" PWD command successful.");
                                            return var63_224;
                                        }
                                        if (command[0].toUpperCase().startsWith("DEL") || command[0].toUpperCase().startsWith("RM") || command[0].toUpperCase().startsWith("LDEL") || command[0].toUpperCase().startsWith("LRM")) {
                                            if (this.only_log) ** GOTO lbl2804
                                            if (source != false ? this.source_logged_in == false : this.destination_logged_in == false) {
                                                this.line("Not connected.");
                                                return null;
                                            }
                                            the_dir = this.getArgs(command, 1, false, source);
                                            if (the_dir.indexOf("*") >= 0) {
                                                list = new Vector<E>();
                                                try {
                                                    parent_dir = source != false ? this.current_source_dir : this.current_dest_dir;
                                                    c.list(parent_dir, list);
                                                    count = 0;
                                                    x = 0;
                                                    while (x < list.size()) {
                                                        name = ((Properties)list.elementAt(x)).getProperty("name");
                                                        if (Common.do_search(command[1], name, false, 0)) {
                                                            ++count;
                                                        }
                                                        if (this.prefs.containsKey("aborting")) break;
                                                        ++x;
                                                    }
                                                    this.line("\"" + count + "\" items to delete...");
                                                    x = 0;
                                                    while (x < list.size()) {
                                                        if (this.prefs.containsKey("aborting")) {
                                                        }
                                                        name = ((Properties)list.elementAt(x)).getProperty("name");
                                                        if (Common.do_search(command[1], name, false, 0)) {
                                                            try {
                                                                c.delete(String.valueOf(parent_dir) + name);
                                                                this.line("\"" + name + "\" delete command successful.");
                                                            }
                                                            catch (Exception e) {
                                                                this.printStackTrace(e, 1);
                                                            }
                                                        }
                                                        ++x;
                                                    }
                                                }
                                                catch (Exception e) {
                                                    this.printStackTrace(e, 1);
                                                }
                                            }
                                            c.setConfig("file_recurse_delete", "true");
                                            ok = c.delete(the_dir);
                                            if (!ok) {
                                                var63_225 = this.line("\"" + the_dir + "\": Delete failed.");
                                                return var63_225;
                                            }
                                            var63_226 = this.line("\"" + the_dir + "\" delete command successful.");
                                            return var63_226;
                                        }
                                        if (command[0].toUpperCase().startsWith("MKD") || command[0].toUpperCase().startsWith("LMKD")) {
                                            if (this.only_log) ** GOTO lbl2804
                                            if (source != false ? this.source_logged_in == false : this.destination_logged_in == false) {
                                                this.line("Not connected.");
                                                return null;
                                            }
                                            the_dir = this.getArgs(command, 1, true, source);
                                            ok = c.makedir(the_dir);
                                            if (!ok) {
                                                ++this.additional_errors;
                                                if (this.local_echo) {
                                                    this.br.close();
                                                }
                                                var63_227 = this.line("\"" + the_dir + "\": MKD failed.");
                                                return var63_227;
                                            }
                                            e = this.stats;
                                            synchronized (e) {
                                                if (command[0].toUpperCase().startsWith("L")) {
                                                    this.stats.put("download_folders", String.valueOf(Integer.parseInt(this.stats.getProperty("download_folders", "0")) + 1));
                                                } else {
                                                    this.stats.put("upload_folders", String.valueOf((double)Float.parseFloat(this.stats.getProperty("upload_folders", "0.0")) + 0.5));
                                                }
                                            }
                                            var63_228 = this.line("\"" + the_dir + "\" MKD command successful.");
                                            return var63_228;
                                        }
                                        if (command[0].toUpperCase().startsWith("QUOTE") || command[0].toUpperCase().startsWith("LQUOTE")) {
                                            if (source != false ? this.source_logged_in == false : this.destination_logged_in == false) {
                                                this.line("Not connected.");
                                                return null;
                                            }
                                            if (c instanceof FTPClient) {
                                                var63_229 = this.line(((FTPClient)c).quote(command_str.substring(command_str.indexOf(" ") + 1)));
                                                return var63_229;
                                            }
                                            var63_230 = this.line("The quote command can only be used with the FTP protocol. (ftp:// , ftps://, ftpes://)");
                                            return var63_230;
                                        }
                                        if (command[0].toUpperCase().startsWith("REN") || command[0].toUpperCase().startsWith("MV") || command[0].toUpperCase().startsWith("LREN") || command[0].toUpperCase().startsWith("LMV")) {
                                            if (this.only_log) ** GOTO lbl2804
                                            if (source != false ? this.source_logged_in == false : this.destination_logged_in == false) {
                                                this.line("Not connected.");
                                                return null;
                                            }
                                            the_dir1 = this.getArgs(command, 1, false, source);
                                            ok = c.rename(the_dir1, the_dir2 = this.getArgs(command, 2, false, source), false);
                                            if (!ok) {
                                                ++this.additional_errors;
                                                var63_231 = this.line("\"" + the_dir1 + "\" -> \"" + the_dir2 + "\": rename failed.");
                                                return var63_231;
                                            }
                                            var63_232 = this.line("\"" + the_dir1 + "\" -> \"" + the_dir2 + "\": rename command successful.");
                                            return var63_232;
                                        }
                                        if (command[0].toUpperCase().startsWith("MDTM") || command[0].toUpperCase().startsWith("LMDTM")) {
                                            if (this.only_log) ** GOTO lbl2804
                                            if (source != false ? this.source_logged_in == false : this.destination_logged_in == false) {
                                                this.line("Not connected.");
                                                return null;
                                            }
                                            the_dir = this.getArgs(command, 1, false, source);
                                            ok = c.mdtm(the_dir, (sdf_yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US)).parse(date = command[2]).getTime());
                                            if (!ok) {
                                                var63_233 = this.line("\"" + the_dir + "\" : mdtm failed. " + date);
                                                return var63_233;
                                            }
                                            var63_234 = this.line("\"" + the_dir + "\": mdtm command successful. " + date);
                                            return var63_234;
                                        }
                                        if (command[0].toUpperCase().startsWith("TIMEOUT") || command[0].toUpperCase().startsWith("LTIMEOUT")) {
                                            if (source) {
                                                this.source_config.put("timeout", command[1]);
                                            } else {
                                                this.dest_config.put("timeout", command[1]);
                                            }
                                            var63_235 = this.line("timeout set : " + command[1]);
                                            return var63_235;
                                        }
                                        if (command[0].toUpperCase().startsWith("KILL")) {
                                            command_f = command;
                                            new Runnable(){

                                                @Override
                                                public void run() {
                                                    String this_id = Common.makeBoundary(4);
                                                    Client.this.stats.put("kill_id", this_id);
                                                    long end = Long.parseLong(command_f[1].trim());
                                                    if (command_f[2].trim().toLowerCase().startsWith("s")) {
                                                        end *= 1000L;
                                                    } else if (command_f[2].trim().toLowerCase().startsWith("m")) {
                                                        end *= 60000L;
                                                    } else if (command_f[2].trim().toLowerCase().startsWith("h")) {
                                                        end *= 3600000L;
                                                    } else if (command_f[2].trim().toLowerCase().startsWith("d")) {
                                                        end *= 86400000L;
                                                    }
                                                    end += System.currentTimeMillis();
                                                    try {
                                                        while (System.currentTimeMillis() > end && Client.this.stats.getProperty("kill_id").equals(this_id)) {
                                                            Thread.sleep(1000L);
                                                        }
                                                        if (Client.this.stats.getProperty("kill_id").equals(this_id) && !command_f[1].trim().equals("0")) {
                                                            Client.this.line("Killing app due to time limit expiration:" + command_f[1].trim() + command_f[2].trim());
                                                            System.exit(1);
                                                        }
                                                    }
                                                    catch (Exception exception) {
                                                        // empty catch block
                                                    }
                                                }
                                            };
                                            var63_236 = this.line("timeout set : " + command[1] + command[2]);
                                            return var63_236;
                                        }
                                        if (command[0].toUpperCase().startsWith("AFTER") || command[0].toUpperCase().startsWith("LAFTER")) {
                                            if (source != false ? this.source_logged_in == false : this.destination_logged_in == false) {
                                                this.line("Not connected.");
                                                return null;
                                            }
                                            s = command_str.substring(command[0].length() + 1).trim();
                                            this.after_next_command.addElement(s);
                                            this.after_next_command.insertElementAt("SKIP", 0);
                                            var63_237 = this.line("\"" + s + "\": Added to after queue.");
                                            return var63_237;
                                        }
                                        if (command[0].toUpperCase().startsWith("TUNNEL") || command[0].toUpperCase().startsWith("LTUNNEL")) {
                                            block709: {
                                                if (command[1].equalsIgnoreCase("gui")) {
                                                    new GUIFrame();
                                                    while (true) {
                                                        Thread.sleep(1000L);
                                                    }
                                                }
                                                if (command[1].equalsIgnoreCase("stop")) {
                                                    tunnel_list = (Vector)this.prefs.get("tunnel_list");
                                                    i = Integer.parseInt(command[2]);
                                                    if (tunnel_list.size() >= i) {
                                                        tunnel = null;
                                                        if (tunnel_list.elementAt(i - 1) instanceof StreamController) {
                                                            tunnel = ((StreamController)tunnel_list.elementAt((int)(i - 1))).tunnel;
                                                            ((StreamController)tunnel_list.elementAt(i - 1)).startStopTunnel(false);
                                                        } else if (tunnel_list.elementAt(i - 1) instanceof Tunnel2) {
                                                            tunnel = ((Tunnel2)tunnel_list.elementAt((int)(i - 1))).tunnel;
                                                            ((Tunnel2)tunnel_list.elementAt(i - 1)).startStopTunnel(false);
                                                        }
                                                        tunnel_list.remove(i - 1);
                                                        tunnel.put("tunnel_status", "stopped");
                                                        var63_238 = this.line("Tunnel id " + i + " stopped, local port " + tunnel.getProperty("localPort", "0") + " closed.");
                                                        return var63_238;
                                                    }
                                                    var63_239 = this.line("No such tunnel id:" + i);
                                                    return var63_239;
                                                }
                                                if (command[1].equalsIgnoreCase("list")) {
                                                    tunnel_list = (Vector<E>)this.prefs.get("tunnel_list");
                                                    if (tunnel_list == null) {
                                                        tunnel_list = new Vector<E>();
                                                    }
                                                    x = 0;
                                                    while (x < tunnel_list.size()) {
                                                        tunnel = null;
                                                        if (tunnel_list.elementAt(x) instanceof StreamController) {
                                                            tunnel = ((StreamController)tunnel_list.elementAt((int)x)).tunnel;
                                                        } else if (tunnel_list.elementAt(x) instanceof Tunnel2) {
                                                            tunnel = ((Tunnel2)tunnel_list.elementAt((int)x)).tunnel;
                                                        }
                                                        if (tunnel == null) {
                                                            this.line("id=" + (x + 1));
                                                        } else {
                                                            this.line("id=" + (x + 1) + ", " + tunnel.getProperty("tunnel_version", "tunnel2") + ", local port:" + tunnel.getProperty("localPort", "0") + ", status:" + tunnel.getProperty("tunnel_status"));
                                                        }
                                                        ++x;
                                                    }
                                                    var63_240 = this.line("Total tunnels:" + tunnel_list.size());
                                                    return var63_240;
                                                }
                                                if (command[1].equalsIgnoreCase("log")) {
                                                    while (this.tunnel_log.size() > 0) {
                                                        this.line(this.tunnel_log.remove(0).toString());
                                                    }
                                                    this.print_prompt();
                                                }
                                                if (command[1].equalsIgnoreCase("trust")) {
                                                    Common.trustEverything();
                                                    var63_241 = this.line("Tunnels no longer validate SSL certificates.");
                                                    return var63_241;
                                                }
                                                if (command[1].equalsIgnoreCase("start")) {
                                                    var63_242 = this.line("Tunnels cannot be restarted once stopped, please issue 'tunnel url' again to start a tunnel.");
                                                    return var63_242;
                                                }
                                                vrl = new VRL(command[1].endsWith("/") != false ? command[1] : String.valueOf(command[1]) + "/");
                                                tunnel_username = vrl.getUsername();
                                                if (tunnel_username == null || tunnel_username.equals("")) {
                                                    Client.last_line_prompt = "message";
                                                    System.out.print("Tunnel Username: ");
                                                    tunnel_username = this.br.readLine();
                                                }
                                                this.prefs.put("last_tunnel_username", tunnel_username);
                                                this.prefs.put("last_tunnel_url", "" + vrl);
                                                tunnel_username_f = tunnel_username;
                                                tunnel_password = vrl.getPassword();
                                                if (tunnel_password == null || tunnel_password.equals("")) {
                                                    Client.last_line_prompt = "message";
                                                    System.out.print("Tunnel Password: ");
                                                    tunnel_password = this.br.readPassword();
                                                }
                                                this.prefs.put("last_tunnel_password", tunnel_password);
                                                if (!this.prefs.containsKey("tunnel_list")) {
                                                    this.prefs.put("tunnel_list", new Vector<E>());
                                                }
                                                tunnel_list = (Vector)this.prefs.get("tunnel_list");
                                                tunnel_password_f = tunnel_password;
                                                try {
                                                    Tunnel2.setLog(this.tunnel_log);
                                                    t = new Tunnel2(vrl.toString(), tunnel_username_f, tunnel_password_f, false);
                                                    t.startThreads();
                                                    if (t.tunnel.size() == 0) {
                                                        t.stopThisTunnel();
                                                        c_test = null;
                                                        try {
                                                            c_test = Common.getClient(Common.getBaseUrl(vrl.toString()), String.valueOf(System.getProperty("crushclient.appname", "CrushClient")) + ":", this.logQueue);
                                                            c_test.login(tunnel_username_f, tunnel_password_f, Client.unique_client_id);
                                                        }
                                                        catch (Throwable data) {
                                                            c_test.logout();
                                                            Worker.startWorker(new Runnable(){

                                                                @Override
                                                                public void run() {
                                                                    int loops = 0;
                                                                    while (loops++ < 5) {
                                                                        if (Client.this.tunnel_log.size() > 0) {
                                                                            while (Client.this.tunnel_log.size() > 0) {
                                                                                Client.this.line(Client.this.tunnel_log.remove(0).toString());
                                                                            }
                                                                            continue;
                                                                        }
                                                                        try {
                                                                            Thread.sleep(1000L);
                                                                        }
                                                                        catch (InterruptedException interruptedException) {
                                                                            // empty catch block
                                                                        }
                                                                    }
                                                                }
                                                            });
                                                            throw data;
                                                        }
                                                        c_test.logout();
                                                        Worker.startWorker(new /* invalid duplicate definition of identical inner class */);
                                                        this.line("No tunnels configured for this account.");
                                                        break block709;
                                                    }
                                                    t.tunnel.put("tunnel_status", "running");
                                                    if (t.tunnel.getProperty("tunnel_version", "tunnel2").equalsIgnoreCase("tunnel3")) {
                                                        sc = new StreamController(vrl.toString(), tunnel_username_f, tunnel_password_f, Client.unique_client_id);
                                                        tunnel_list.addElement(sc);
                                                        sc.setLog(this.tunnel_log);
                                                        sc.startThreads();
                                                        sc.tunnel.put("tunnel_status", "running");
                                                    } else {
                                                        tunnel_list.addElement(t);
                                                    }
                                                    this.line("Tunnel started (id=" + tunnel_list.size() + ") " + t.tunnel.getProperty("tunnel_version", "tunnel2") + " on local port " + t.tunnel.getProperty("localPort", "0") + ", property count:" + t.tunnel.size());
                                                    this.prefs.put("last_tunnel_port", t.tunnel.getProperty("localPort", "0"));
                                                    Thread.sleep(500L);
                                                    Worker.startWorker(new Runnable(){

                                                        @Override
                                                        public void run() {
                                                            while (true) {
                                                                if (Client.this.tunnel_log.size() > 1000) {
                                                                    Client.this.tunnel_log.remove(0);
                                                                    continue;
                                                                }
                                                                try {
                                                                    Thread.sleep(1000L);
                                                                }
                                                                catch (InterruptedException interruptedException) {
                                                                    continue;
                                                                }
                                                                break;
                                                            }
                                                        }
                                                    });
                                                }
                                                catch (Exception e) {
                                                    this.line("Tunnel failed:" + e);
                                                    this.printStackTrace(e, 1);
                                                }
                                            }
                                            this.print_prompt();
                                        }
                                        if (command[0].toUpperCase().startsWith("CIPHER") || command[0].toUpperCase().startsWith("LCIPHER")) {
                                            if (command[1].equalsIgnoreCase("list")) {
                                                cipher_suites = URLConnection.cipher_suites;
                                                if (cipher_suites == null) {
                                                    urlc = URLConnection.openConnection(new VRL("https://www.crushftp.com/WebInterface/login.html"), new Properties());
                                                    urlc.getResponseCode();
                                                    urlc.disconnect();
                                                    cipher_suites = URLConnection.cipher_suites;
                                                }
                                                if (cipher_suites == null) {
                                                    this.line("Cannot get list until a connection has been made.");
                                                } else {
                                                    x = 0;
                                                    while (x < cipher_suites.size()) {
                                                        this.line("" + cipher_suites.elementAt(x));
                                                        ++x;
                                                    }
                                                }
                                                this.print_prompt();
                                                var63_243 = "" + cipher_suites;
                                                return var63_243;
                                            }
                                            if (command[1].equalsIgnoreCase("set")) {
                                                this.line("Preferred cipher set to: " + command[2]);
                                                URLConnection.preferred_cipher = command[2];
                                                this.print_prompt();
                                            }
                                            if (command[1].equalsIgnoreCase("get")) {
                                                this.line("Last used cipher: " + URLConnection.last_cipher);
                                                this.print_prompt();
                                            }
                                            if (!command[1].equalsIgnoreCase("trust")) ** GOTO lbl2804
                                            Common.trustEverything();
                                            this.print_prompt();
                                            var63_244 = this.line("SSL/TLS no longer validate SSL certificates.");
                                            return var63_244;
                                        }
                                        if (!command[0].toUpperCase().startsWith("GET") && !command[0].toUpperCase().startsWith("REGET") && !command[0].toUpperCase().startsWith("LAPPE")) break block736;
                                        if (this.only_log) ** GOTO lbl2804
                                        if (this.client_start_time == 0L) {
                                            this.client_start_time = System.currentTimeMillis();
                                        }
                                        if (!this.destination_logged_in) {
                                            this.line("Not connected to dest.");
                                            return null;
                                        }
                                        if (!this.source_logged_in) {
                                            this.line("Not connected to source.");
                                            return null;
                                        }
                                        this.freeClient(c);
                                        transfer_error = null;
                                        resume = command[0].toUpperCase().startsWith("GET") == false;
                                        while (!this.prefs.containsKey("aborting")) {
                                            block712: {
                                                transfer_error = null;
                                                c = null;
                                                the_dir = this.getArgs(command, 1, false, false);
                                                the_dir_source = this.getArgs(command, 2, false, true);
                                                if (this.prefs.getProperty("simple", "false").equals("true")) {
                                                    the_dir = command[1];
                                                }
                                                if (the_dir.indexOf("*") >= 0) {
                                                    list = new Vector<E>();
                                                    try {
                                                        this.startupThreads();
                                                        parent_dir = Common.all_but_last(the_dir);
                                                        c_dest = this.getClient(false);
                                                        try {
                                                            c_dest.list(parent_dir, list);
                                                        }
                                                        finally {
                                                            this.freeClient(c_dest);
                                                        }
                                                        this.pending_transfer_queue_inprogress.removeElement(command_str);
                                                        x = 0;
                                                        while (x < list.size() && !this.prefs.containsKey("aborting")) {
                                                            p = (Properties)list.elementAt(x);
                                                            if (Common.do_search(Common.last(the_dir), p.getProperty("name"), false, 0)) {
                                                                if (p.getProperty("name").indexOf("*") >= 0) {
                                                                    this.line("Skipping invalid filename for wildcard download:" + p.getProperty("name"));
                                                                } else {
                                                                    this.pending_transfer_queue.addElement(String.valueOf(command[0]) + " \"" + parent_dir + p.getProperty("name") + (p.getProperty("type").equalsIgnoreCase("DIR") != false ? "/" : "") + "\" \"" + the_dir_source + p.getProperty("name") + (p.getProperty("type").equalsIgnoreCase("DIR") != false ? "/" : "") + "\"");
                                                                    if (this.prefs.containsKey("aborting")) {
                                                                        list.removeAllElements();
                                                                        this.pending_transfer_queue.removeAllElements();
                                                                        this.pending_transfer_queue_inprogress.removeAllElements();
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            ++x;
                                                        }
                                                    }
                                                    catch (Exception e) {
                                                        this.printStackTrace(e, 1);
                                                        this.line("Error:" + e);
                                                    }
                                                    this.print_prompt();
                                                }
                                                if (the_dir_source.endsWith("/") && !the_dir.endsWith("/")) {
                                                    the_dir_source = String.valueOf(the_dir_source) + Common.last(the_dir);
                                                }
                                                in = null;
                                                out = null;
                                                c_source = null;
                                                c_dest = null;
                                                folder = false;
                                                source_size = 0L;
                                                success_end_str = "";
                                                aborted = false;
                                                try {
                                                    block714: {
                                                        c_source = this.getClient(true);
                                                        c_dest = this.getClient(false);
                                                        start_pos = 0L;
                                                        source_stat = null;
                                                        if (!the_dir_source.startsWith("/WebInterface/")) {
                                                            source_stat = c_source.stat(the_dir_source);
                                                        }
                                                        if (source_stat != null) {
                                                            source_size = Long.parseLong(source_stat.getProperty("size", "0"));
                                                        }
                                                        dest_stat = null;
                                                        if (!the_dir.startsWith("/WebInterface/")) {
                                                            dest_stat = c_dest.stat(the_dir);
                                                        }
                                                        if (dest_stat != null && dest_stat.getProperty("type").equalsIgnoreCase("DIR")) {
                                                            folder = true;
                                                        }
                                                        if (folder) {
                                                            this.startupThreads();
                                                            if (!the_dir_source.endsWith("/")) {
                                                                the_dir_source = String.valueOf(the_dir_source) + "/";
                                                            }
                                                            if (!the_dir.endsWith("/")) {
                                                                the_dir = String.valueOf(the_dir) + "/";
                                                            }
                                                            this.process_command("LMKD \"" + the_dir_source + "\"", true);
                                                            list = new Vector<E>();
                                                            c_dest.list(the_dir, list);
                                                            this.freeClient(c_source);
                                                            this.freeClient(c_dest);
                                                            this.pending_transfer_queue_inprogress.removeElement(command_str);
                                                            while (list.size() > 0) {
                                                                p = (Properties)list.remove(0);
                                                                if (this.prefs.containsKey("aborting")) {
                                                                    list.removeAllElements();
                                                                    this.pending_transfer_queue.removeAllElements();
                                                                    this.pending_transfer_queue_inprogress.removeAllElements();
                                                                    break block712;
                                                                }
                                                                try {
                                                                    if (p.getProperty("type").equalsIgnoreCase("DIR")) {
                                                                        this.process_command("LMKD \"" + the_dir_source + p.getProperty("name") + "\"", true);
                                                                    }
                                                                    this.pending_transfer_queue.addElement(String.valueOf(command[0]) + " \"" + the_dir + p.getProperty("name") + (p.getProperty("type").equalsIgnoreCase("DIR") != false ? "/" : "") + "\" \"" + the_dir_source + p.getProperty("name") + (p.getProperty("type").equalsIgnoreCase("DIR") != false ? "/" : "") + "\"");
                                                                    while (this.pending_transfer_queue.size() > 1000000) {
                                                                        Thread.sleep(100L);
                                                                    }
                                                                }
                                                                catch (Exception e) {
                                                                    this.printStackTrace(e, 1);
                                                                    this.line("Error:" + e);
                                                                }
                                                            }
                                                            break block712;
                                                        }
                                                        if (resume) {
                                                            start_pos = source_size;
                                                        }
                                                        start_pos1 = start_pos;
                                                        start_pos2 = start_pos;
                                                        the_dir = this.vars.replace_vars_line_url(the_dir, null, "{", "}");
                                                        the_dir_source = this.vars.replace_vars_line_url(the_dir_source, source_stat, "{", "}");
                                                        the_dir = this.vars.replace_vars_line_date(the_dir, null, "{", "}");
                                                        the_dir_source = this.vars.replace_vars_line_date(the_dir_source, source_stat, "{", "}");
                                                        skip = false;
                                                        if (this.prefs.getProperty("skip_modified_and_size", "true").equals("true") && source_stat != null) {
                                                            if (dest_stat != null && Math.abs(Long.parseLong(source_stat.getProperty("modified", "0")) - Long.parseLong(dest_stat.getProperty("modified", "50000"))) < 1000L && source_stat.getProperty("size").equals(dest_stat.getProperty("size"))) {
                                                                skip = true;
                                                                this.line(String.valueOf(the_dir) + ":Skipping item because of matching modified date and size (skip_modified_and_size)");
                                                                this.print_prompt();
                                                            }
                                                        } else if (this.prefs.getProperty("skip_modified", "false").equals("true") && source_stat != null) {
                                                            if (dest_stat != null && Math.abs(Long.parseLong(source_stat.getProperty("modified", "0")) - Long.parseLong(dest_stat.getProperty("modified", "50000"))) < 1000L) {
                                                                skip = true;
                                                                this.line(String.valueOf(the_dir) + ":Skipping item because of matching modifed date (skip_modified)");
                                                                this.print_prompt();
                                                            }
                                                        } else if (this.prefs.getProperty("skip_size", "false").equals("true") && source_stat != null && dest_stat != null && source_stat.getProperty("size").equals(dest_stat.getProperty("size"))) {
                                                            skip = true;
                                                            this.line(String.valueOf(the_dir) + ":Skipping item because of matching size (skip_size)");
                                                            this.print_prompt();
                                                        }
                                                        if (skip) {
                                                            var30_305 = this.stats;
                                                            synchronized (var30_305) {
                                                                this.stats.put("download_skipped_count", String.valueOf(Integer.parseInt(this.stats.getProperty("download_skipped_count", "0")) + 1));
                                                                this.stats.put("download_skipped_bytes", String.valueOf(Long.parseLong(this.stats.getProperty("download_skipped_bytes", "0")) + Long.parseLong(dest_stat.getProperty("size"))));
                                                            }
                                                            this.freeClient(c_source);
                                                            this.freeClient(c_dest);
                                                            this.print_prompt();
                                                            break block712;
                                                        }
                                                        if (command[0].toUpperCase().startsWith("LAPPE")) {
                                                            start_pos1 = 0L;
                                                            start_pos2 = 0L;
                                                            if (source_stat != null) {
                                                                start_pos2 = Long.parseLong(source_stat.getProperty("size", "0"));
                                                            }
                                                            resume = false;
                                                        }
                                                        in = in_f = c_dest.download(the_dir, start_pos1, -1L, true);
                                                        c_source.setConfig("transfer_direction", "PUT");
                                                        c_dest.setConfig("transfer_direction", "GET");
                                                        c_source.setConfig("transfer_path_dst", the_dir_source);
                                                        c_dest.setConfig("transfer_path_src", the_dir);
                                                        c_dest.setConfig("transfer_path_dst", the_dir_source);
                                                        c_source.setConfig("transfer_path_src", the_dir);
                                                        c_source.setConfig("transfer_stats", "true");
                                                        c_dest.setConfig("transfer_stats", "true");
                                                        out = out_f = c_source.upload(the_dir_source, start_pos2, true, true);
                                                        this.line("Download started:" + the_dir + " -> " + the_dir_source + (resume != false ? " : Resuming from position:" + start_pos : ""));
                                                        if (multithreaded) {
                                                            this.print_prompt();
                                                        }
                                                        the_dir_dest_f = the_dir;
                                                        the_dir_source_f = the_dir_source;
                                                        c_source_f = c_source;
                                                        c_dest_f = c_dest;
                                                        dest_stat_f = dest_stat;
                                                        start = System.currentTimeMillis();
                                                        c_dest_f.setConfig("transfer_start", String.valueOf(start));
                                                        if (dest_stat != null) {
                                                            c_dest_f.setConfig("transfer_bytes_total", String.valueOf(dest_stat.getProperty("size")));
                                                        }
                                                        c_source_f.setConfig("transfer_start", String.valueOf(start));
                                                        if (dest_stat != null) {
                                                            c_source_f.setConfig("transfer_bytes_total", String.valueOf(dest_stat.getProperty("size")));
                                                        }
                                                        c_dest_f.setConfig("transfer_bytes", "0");
                                                        c_source_f.setConfig("transfer_bytes", "0");
                                                        c_dest_f.setConfig("transfer_bytes_last", "0");
                                                        c_source_f.setConfig("transfer_history", new Vector<E>());
                                                        c_dest_f.setConfig("transfer_history", new Vector<E>());
                                                        c_source_f.setConfig("transfer_bytes_last", "0");
                                                        c_dest_f.setConfig("transfer_bytes_last_interval", "0");
                                                        c_source_f.setConfig("transfer_bytes_last_interval", "0");
                                                        c_dest_f.setConfig("abort_obj", null);
                                                        c_source_f.setConfig("abort_obj", null);
                                                        if (!multithreaded) {
                                                            Worker.startWorker(new Runnable(){

                                                                @Override
                                                                public void run() {
                                                                    while (c_source_f.getConfig("transfer_stats") != null) {
                                                                        Client.this.printStats(false);
                                                                        try {
                                                                            Thread.sleep(1000L);
                                                                        }
                                                                        catch (InterruptedException e) {
                                                                            Client.this.printStackTrace(e, 1);
                                                                        }
                                                                    }
                                                                }
                                                            });
                                                        }
                                                        total_bytes = 0L;
                                                        md5 = new MD5Calculator(this.prefs.getProperty("md5sum_native_exec", "false").equals("true"), "md5", this.prefs.getProperty("md5_check", "true").equals("true"));
                                                        try {
                                                            try {
                                                                b = new byte[32768];
                                                                bytes_read = 0;
                                                                v6 = slow_speed = this.prefs.getProperty("slow_transfer", "0").equals("0") == false;
                                                                if (slow_speed) {
                                                                    b = new byte[1];
                                                                }
                                                                last_stat_msg = start;
                                                                single_prefs_update_interval = Long.parseLong(this.prefs.getProperty("update_interval", "1"));
                                                                removed_pending = false;
                                                                while (bytes_read >= 0) {
                                                                    bytes_read = in_f.read(b);
                                                                    if (c_source.getConfig("abort_obj") == c_source || c_dest.getConfig("abort_obj") == c_dest) {
                                                                        aborted = true;
                                                                        throw new IOException("File transfer cancelled:" + the_dir);
                                                                    }
                                                                    if (bytes_read >= 0) {
                                                                        out_f.write(b, 0, bytes_read);
                                                                        md5.update(b, 0, bytes_read);
                                                                        if (slow_speed) {
                                                                            Thread.sleep(Integer.parseInt(this.prefs.getProperty("slow_transfer", "0")));
                                                                        }
                                                                        total_bytes += (long)bytes_read;
                                                                    }
                                                                    c_dest_f.setConfig("transfer_bytes", String.valueOf(total_bytes));
                                                                    c_source_f.setConfig("transfer_bytes", String.valueOf(total_bytes));
                                                                    if (this.single_command_line_mode && System.currentTimeMillis() - last_stat_msg > 1000L * single_prefs_update_interval) {
                                                                        if (this.prefs.getProperty("client_debug", "false").equals("true")) {
                                                                            this.printStats(false);
                                                                        }
                                                                        last_stat_msg = System.currentTimeMillis();
                                                                    }
                                                                    if (removed_pending) continue;
                                                                    removed_pending = true;
                                                                    this.pending_transfer_queue_inprogress.removeElement(command_str);
                                                                }
                                                                in_f.close();
                                                                out_f.close();
                                                                c_source_f.close();
                                                                c_dest_f.close();
                                                                c_source.setConfig("transfer_stats", null);
                                                                c_dest.setConfig("transfer_stats", null);
                                                                if (!multithreaded) {
                                                                    this.printStats(true);
                                                                }
                                                                speed_str = (speed = 10.0f * ((float)total_bytes / 1024.0f / ((float)(System.currentTimeMillis() - start) / 1000.0f))) > 10240.0f ? String.valueOf((float)((int)(speed / 1024.0f)) / 10.0f) + "MB/sec" : String.valueOf((float)((double)((int)speed) / 10.0)) + "KB/sec";
                                                                md5Str = md5.getHash();
                                                                if (source_size - start_pos > 0L && source_size - start_pos != total_bytes) {
                                                                    throw new IOException("File transfer failed (source/received size mismatch):Remote=" + source_size + " vs local=" + (total_bytes + start_pos) + ":" + the_dir + " md5=" + md5Str);
                                                                }
                                                                success_end_str = " : " + (System.currentTimeMillis() - start) + "ms, " + speed_str + ", size=" + total_bytes + ", md5=" + md5Str;
                                                                this.line("Download completed:" + the_dir_dest_f + " -> " + the_dir_source_f + success_end_str);
                                                                if (this.prefs.getProperty("keep_date", "true").equals("true")) {
                                                                    sdf_yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                                                                    if (dest_stat_f != null) {
                                                                        this.process_command("LMDTM \"" + the_dir_source_f + "\" " + sdf_yyyyMMddHHmmss.format(new Date(Long.parseLong(dest_stat_f.getProperty("modified")))), true);
                                                                    }
                                                                }
                                                                this.print_prompt();
                                                            }
                                                            catch (Exception e) {
                                                                transfer_error = e;
                                                                this.printStackTrace(e, 1);
                                                                this.line("Error:" + e);
                                                                md5.close();
                                                                break block714;
                                                            }
                                                        }
                                                        catch (Throwable var54_372) {
                                                            md5.close();
                                                            throw var54_372;
                                                        }
                                                        md5.close();
                                                    }
                                                    this.freeClient(c_source_f);
                                                    this.freeClient(c_dest_f);
                                                    this.recent_transfers_download.addElement(dest_stat_f);
                                                    e = this.stats;
                                                    synchronized (e) {
                                                        this.stats.put("download_count", String.valueOf(Integer.parseInt(this.stats.getProperty("download_count", "0")) + 1));
                                                        this.stats.put("download_bytes", String.valueOf(Long.parseLong(this.stats.getProperty("download_bytes", "0")) + total_bytes));
                                                        // MONITOREXIT @DISABLED, blocks:[210, 68, 10, 122, 63] lbl1337 : MonitorExitStatement: MONITOREXIT : e
                                                        if (true) ** GOTO lbl1355
                                                    }
                                                    do {
                                                        this.recent_transfers_upload.remove(0);
lbl1355:
                                                        // 2 sources

                                                    } while (this.recent_transfers_upload.size() > 1000);
                                                    while (this.recent_transfers_download.size() > 1000) {
                                                        this.recent_transfers_download.remove(0);
                                                    }
                                                    idle_time = 0;
                                                    while (this.source_used.size() == 0 && this.pending_transfer_queue.size() == 0 && !this.prefs.containsKey("aborting") && this.retry_active.size() == 0 && transfer_error == null) {
                                                        if (++idle_time > 10) break;
                                                        Thread.sleep(100L);
                                                    }
                                                    if (idle_time >= 10 && transfer_error == null && !this.single_command_line_mode) {
                                                        this.line("Transfer complete.  " + this.stats_summary());
                                                        this.print_prompt();
                                                    }
                                                }
                                                catch (Exception e) {
                                                    transfer_error = e;
                                                    try {
                                                        in.close();
                                                    }
                                                    catch (Exception var22_268) {
                                                        // empty catch block
                                                    }
                                                    try {
                                                        c_source.close();
                                                    }
                                                    catch (Exception var22_269) {
                                                        // empty catch block
                                                    }
                                                    try {
                                                        out.close();
                                                    }
                                                    catch (Exception var22_270) {
                                                        // empty catch block
                                                    }
                                                    c_dest.setConfig("abort_obj", null);
                                                    c_source.setConfig("abort_obj", null);
                                                    this.printStackTrace(e, 1);
                                                    this.line("Error:" + e);
                                                }
                                            }
                                            if (aborted || transfer_error == null || this.prefs.containsKey("aborting") || ("" + transfer_error).indexOf("403") >= 0 || ("" + transfer_error).indexOf("404") >= 0 || ("" + transfer_error).indexOf("denied") >= 0 || ("" + transfer_error).indexOf("abort") >= 0 || ("" + transfer_error).indexOf("cancel") >= 0 || ("" + transfer_error).indexOf("not allowed") >= 0 || ("" + transfer_error).toLowerCase().indexOf("no such file") >= 0 || ("" + transfer_error).toLowerCase().indexOf("no such file") >= 0 || ("" + transfer_error).toLowerCase().indexOf("NullPointer") >= 0) {
                                                if (transfer_error != null) {
                                                    this.line("Ended with error:" + transfer_error + ":" + command_str);
                                                    this.failed_transfer_queue.addElement(String.valueOf(command_str) + "###:" + transfer_error.getMessage() + ":" + this.log_sdf.format(new Date()));
                                                    this.add_transfer_log("ERROR:" + command_str + ":" + transfer_error);
                                                } else if (!folder) {
                                                    this.add_transfer_log("SUCCESS:" + command_str + success_end_str);
                                                    this.success_transfer_queue.addElement(String.valueOf(command_str) + success_end_str);
                                                }
                                                while (this.success_transfer_queue.size() > 1000) {
                                                    this.success_transfer_queue.remove(0);
                                                }
                                                while (this.failed_transfer_queue.size() > 1000) {
                                                    this.failed_transfer_queue.remove(0);
                                                }
                                                this.prefs.put("auto_retry_delay", "1000");
                                            }
                                            if (!this.prefs.getProperty("auto_retry", "true").equals("true")) continue;
                                            i = Integer.parseInt(this.prefs.getProperty("auto_retry_delay", "1000"));
                                            slept = 100L;
                                            start_pos1 = this.retry_active;
                                            synchronized (start_pos1) {
                                                this.retry_active.addElement("active");
                                                // MONITOREXIT @DISABLED, blocks:[210, 72, 120, 10] lbl1412 : MonitorExitStatement: MONITOREXIT : start_pos1
                                                if (true) ** GOTO lbl1429
                                            }
                                            do {
                                                Thread.sleep(100L);
                                                slept += 100L;
lbl1429:
                                                // 2 sources

                                            } while (slept < i && !this.prefs.containsKey("aborting"));
                                            if (i > 15000L) {
                                                i = 15000L;
                                            }
                                            this.prefs.put("auto_retry_delay", String.valueOf(i * 2L));
                                            resume = true;
                                            start_pos1 = this.retry_active;
                                            synchronized (start_pos1) {
                                                this.retry_active.removeElementAt(0);
                                            }
                                        }
                                    }
                                    if (!command[0].toUpperCase().startsWith("PUT") && !command[0].toUpperCase().startsWith("REPUT") && !command[0].toUpperCase().startsWith("APPE") && !command[0].toUpperCase().startsWith("PUTDEL") && !command[0].toUpperCase().startsWith("PUTSYNC") && !command[0].toUpperCase().startsWith("MOVE")) break block737;
                                    if (this.client_start_time == 0L) {
                                        this.client_start_time = System.currentTimeMillis();
                                    }
                                    if (!this.destination_logged_in) {
                                        this.line("Not connected to dest.");
                                        return null;
                                    }
                                    if (!this.source_logged_in) {
                                        this.line("Not connected to source.");
                                        return null;
                                    }
                                    this.freeClient(c);
                                    transfer_error = null;
                                    resume = command[0].toUpperCase().startsWith("REPUT") != false || command[0].toUpperCase().startsWith("APPE") != false;
                                    while (!this.prefs.containsKey("aborting")) {
                                        block738: {
                                            block717: {
                                                transfer_error = null;
                                                c = null;
                                                the_dir = this.getArgs(command, 1, false, true);
                                                the_dir_dest = this.getArgs(command, 2, false, false);
                                                if (this.prefs.getProperty("simple", "false").equals("true")) {
                                                    the_dir_dest = command[2];
                                                }
                                                if (the_dir.indexOf("*") >= 0) {
                                                    list = new Vector<E>();
                                                    try {
                                                        this.startupThreads();
                                                        parent_dir = Common.all_but_last(the_dir);
                                                        c1 = this.getClient(true);
                                                        try {
                                                            c1.list(parent_dir, list);
                                                        }
                                                        finally {
                                                            this.freeClient(c1);
                                                        }
                                                        this.pending_transfer_queue_inprogress.removeElement(command_str);
                                                        x = 0;
                                                        while (x < list.size() && !this.prefs.containsKey("aborting")) {
                                                            p = (Properties)list.elementAt(x);
                                                            if (Common.do_search(Common.last(the_dir), p.getProperty("name"), false, 0)) {
                                                                if (p.getProperty("name").indexOf("*") >= 0) {
                                                                    this.line("Skipping invalid filename for wildcard upload:" + p.getProperty("name"));
                                                                } else {
                                                                    this.pending_transfer_queue.addElement(String.valueOf(command[0]) + " \"" + parent_dir + p.getProperty("name") + (p.getProperty("type").equalsIgnoreCase("DIR") != false ? "/" : "") + "\" \"" + the_dir_dest + p.getProperty("name") + (p.getProperty("type").equalsIgnoreCase("DIR") != false ? "/" : "") + "\"");
                                                                    if (this.prefs.containsKey("aborting")) {
                                                                        list.removeAllElements();
                                                                        this.pending_transfer_queue.removeAllElements();
                                                                        this.pending_transfer_queue_inprogress.removeAllElements();
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            ++x;
                                                        }
                                                    }
                                                    catch (Exception e) {
                                                        this.printStackTrace(e, 1);
                                                        this.line("Error:" + e);
                                                    }
                                                    this.print_prompt();
                                                }
                                                if (the_dir_dest.endsWith("/") && !the_dir.endsWith("/")) {
                                                    the_dir_dest = String.valueOf(the_dir_dest) + Common.last(the_dir);
                                                }
                                                in = null;
                                                out = null;
                                                c1 = null;
                                                c2 = null;
                                                source_size = 0L;
                                                dest_size = 0L;
                                                folder = false;
                                                success_end_str = "";
                                                aborted = false;
                                                try {
                                                    block719: {
                                                        c1 = this.getClient(true);
                                                        c2 = this.getClient(false);
                                                        start_pos = 0L;
                                                        dest_stat = c2.stat(the_dir_dest);
                                                        if (dest_stat != null) {
                                                            dest_size = Long.parseLong(dest_stat.getProperty("size", "0"));
                                                        }
                                                        if (resume) {
                                                            start_pos = dest_size;
                                                        }
                                                        if ((source_stat = c1.stat(the_dir)) != null) {
                                                            source_size = Long.parseLong(source_stat.getProperty("size", "0"));
                                                        }
                                                        if (source_stat == null) {
                                                            this.line(String.valueOf(the_dir) + " not found.");
                                                            this.freeClient(c1);
                                                            this.freeClient(c2);
                                                        }
                                                        if (source_stat.getProperty("type").equalsIgnoreCase("DIR")) {
                                                            folder = true;
                                                        }
                                                        if (folder) {
                                                            this.startupThreads();
                                                            if (!the_dir_dest.endsWith("/")) {
                                                                the_dir_dest = String.valueOf(the_dir_dest) + "/";
                                                            }
                                                            if (!the_dir.endsWith("/")) {
                                                                the_dir = String.valueOf(the_dir) + "/";
                                                            }
                                                            if (!this.only_log) {
                                                                this.process_command("MKD \"" + the_dir_dest + "\"", true);
                                                            }
                                                            list = new Vector<E>();
                                                            c1.list(the_dir, list);
                                                            this.pending_transfer_queue_inprogress.removeElement(command_str);
                                                            if (command[0].toUpperCase().startsWith("PUTDEL")) {
                                                                local_lookup = new Properties();
                                                                x = 0;
                                                                while (x < list.size()) {
                                                                    p = (Properties)list.elementAt(x);
                                                                    local_lookup.put(p.getProperty("name"), p.getProperty("size"));
                                                                    ++x;
                                                                }
                                                                list_remote = new Vector<E>();
                                                                c2.list(the_dir_dest, list_remote);
                                                                this.freeClient(c1);
                                                                this.freeClient(c2);
                                                                x = 0;
                                                                while (x < list_remote.size()) {
                                                                    p = (Properties)list_remote.elementAt(x);
                                                                    if (!local_lookup.containsKey(p.getProperty("name"))) {
                                                                        msg = ":Deleting item because no matching local item was found.";
                                                                        if (!this.only_log) {
                                                                            this.line(String.valueOf(the_dir_dest) + p.getProperty("name") + msg);
                                                                        }
                                                                        if (p.getProperty("type").equalsIgnoreCase("DIR")) {
                                                                            if (this.only_log) {
                                                                                this.line("RMD \"" + the_dir_dest + p.getProperty("name") + "\" \"###:" + msg + "\"");
                                                                            } else {
                                                                                this.process_command("RMD \"" + the_dir_dest + p.getProperty("name") + "\"", true);
                                                                            }
                                                                        } else if (this.only_log) {
                                                                            this.line("DEL \"" + the_dir_dest + p.getProperty("name") + "\" \"###:" + msg + "\"");
                                                                        } else {
                                                                            this.process_command("DEL \"" + the_dir_dest + p.getProperty("name") + "\"", true);
                                                                        }
                                                                    }
                                                                    ++x;
                                                                }
                                                            } else if (command[0].toUpperCase().startsWith("PUTSYNC")) {
                                                                local_lookup = new Properties();
                                                                x = 0;
                                                                while (x < list.size()) {
                                                                    p = (Properties)list.elementAt(x);
                                                                    local_lookup.put(p.getProperty("name"), p.getProperty("size"));
                                                                    ++x;
                                                                }
                                                                list_remote = new Vector<E>();
                                                                c2.list(the_dir_dest, list_remote);
                                                                if (c2 instanceof HTTPClient && !this.dest_config.containsKey("sync_hour_offset") && (result = c2.doCommand("SITE TIME")).startsWith("214 ")) {
                                                                    diff = System.currentTimeMillis() - Long.parseLong(result.substring(4).trim());
                                                                    this.prefs.put("sync_hour_offset", String.valueOf(diff / 3600000L));
                                                                    this.dest_config.put("sync_hour_offset", this.prefs.getProperty("sync_hour_offset"));
                                                                }
                                                                this.freeClient(c1);
                                                                this.freeClient(c2);
                                                                remote_lookup = new Properties();
                                                                x = 0;
                                                                while (x < list_remote.size()) {
                                                                    p = (Properties)list_remote.elementAt(x);
                                                                    remote_lookup.put(p.getProperty("name"), p.getProperty("size"));
                                                                    if (!local_lookup.containsKey(p.getProperty("name"))) {
                                                                        if (Long.parseLong(p.getProperty("modified")) < Long.parseLong(this.prefs.getProperty("sync_last_run", "0")) - 3600000L * Long.parseLong(this.prefs.getProperty("sync_hour_offset", "0"))) {
                                                                            msg = ":Deleting item because no matching local item was found.";
                                                                            if (!this.only_log) {
                                                                                this.line(String.valueOf(the_dir_dest) + p.getProperty("name") + msg);
                                                                            }
                                                                            if (p.getProperty("type").equalsIgnoreCase("DIR")) {
                                                                                if (this.only_log) {
                                                                                    this.line("RMD \"" + the_dir_dest + p.getProperty("name") + "\" \"###:" + msg + "\"");
                                                                                } else {
                                                                                    this.process_command("RMD \"" + the_dir_dest + p.getProperty("name") + "\"", true);
                                                                                }
                                                                            } else if (this.only_log) {
                                                                                this.line("DEL \"" + the_dir_dest + p.getProperty("name") + "\" \"###:" + msg + "\"");
                                                                            } else {
                                                                                this.process_command("DEL \"" + the_dir_dest + p.getProperty("name") + "\"", true);
                                                                            }
                                                                        } else if (Long.parseLong(p.getProperty("modified")) > Long.parseLong(this.prefs.getProperty("pending_sync_last_run", "0")) - 3600000L * Long.parseLong(this.prefs.getProperty("sync_hour_offset", "0"))) {
                                                                            msg = ":Downloading item because no matching local item was found.";
                                                                            if (!this.only_log) {
                                                                                this.line(String.valueOf(the_dir_dest) + p.getProperty("name") + msg);
                                                                            }
                                                                            if (p.getProperty("type").equalsIgnoreCase("DIR")) {
                                                                                if (this.only_log) {
                                                                                    this.line("GET \"" + the_dir_dest + p.getProperty("name") + "\" \"###:" + msg + "\"");
                                                                                } else {
                                                                                    this.process_command("GET \"" + the_dir_dest + p.getProperty("name") + "\"", true);
                                                                                }
                                                                            } else if (this.only_log) {
                                                                                this.line("GET \"" + the_dir_dest + p.getProperty("name") + "\" \"###:" + msg + "\"");
                                                                            } else {
                                                                                this.process_command("GET \"" + the_dir_dest + p.getProperty("name") + "\" \"" + the_dir + p.getProperty("name") + "\"", true);
                                                                            }
                                                                        }
                                                                    }
                                                                    ++x;
                                                                }
                                                                x = list.size() - 1;
                                                                while (x >= 0) {
                                                                    p = (Properties)list.elementAt(x);
                                                                    if (!remote_lookup.containsKey(p.getProperty("name"))) {
                                                                        if (Long.parseLong(p.getProperty("modified")) < Long.parseLong(this.prefs.getProperty("sync_last_run", "0")) - 3600000L * Long.parseLong(this.prefs.getProperty("sync_hour_offset", "0"))) {
                                                                            msg = ":Deleting item because no matching local item was found.";
                                                                            if (!this.only_log) {
                                                                                this.line(String.valueOf(the_dir_dest) + p.getProperty("name") + msg);
                                                                            }
                                                                            if (p.getProperty("type").equalsIgnoreCase("DIR")) {
                                                                                if (this.only_log) {
                                                                                    this.line("LRMD \"" + the_dir + p.getProperty("name") + "\" \"###:" + msg + "\"");
                                                                                } else {
                                                                                    this.process_command("LRMD \"" + the_dir + p.getProperty("name") + "\"", true);
                                                                                }
                                                                            } else if (this.only_log) {
                                                                                this.line("LDEL \"" + the_dir + p.getProperty("name") + "\" \"###:" + msg + "\"");
                                                                            } else {
                                                                                this.process_command("LDEL \"" + the_dir + p.getProperty("name") + "\"", true);
                                                                            }
                                                                        } else if (Long.parseLong(p.getProperty("modified")) > Long.parseLong(this.prefs.getProperty("pending_sync_last_run", "0")) - 3600000L * Long.parseLong(this.prefs.getProperty("sync_hour_offset", "0")) && Long.parseLong(this.prefs.getProperty("pending_sync_last_run", "0")) > 0L) {
                                                                            list.remove(x);
                                                                        }
                                                                    }
                                                                    --x;
                                                                }
                                                            } else {
                                                                this.freeClient(c1);
                                                                this.freeClient(c2);
                                                            }
                                                            while (list.size() > 0) {
                                                                p = (Properties)list.remove(0);
                                                                if (this.prefs.containsKey("aborting")) {
                                                                    list.removeAllElements();
                                                                    this.pending_transfer_queue.removeAllElements();
                                                                    this.pending_transfer_queue_inprogress.removeAllElements();
                                                                    break block717;
                                                                }
                                                                try {
                                                                    if (p.getProperty("type").equalsIgnoreCase("DIR") && !this.only_log) {
                                                                        this.process_command("MKD \"" + the_dir_dest + p.getProperty("name") + "\"", true);
                                                                    }
                                                                    this.pending_transfer_queue.addElement(String.valueOf(command[0]) + " \"" + the_dir + p.getProperty("name") + (p.getProperty("type").equalsIgnoreCase("DIR") != false ? "/" : "") + "\" \"" + the_dir_dest + p.getProperty("name") + (p.getProperty("type").equalsIgnoreCase("DIR") != false ? "/" : "") + "\"");
                                                                    while (this.pending_transfer_queue.size() > 1000000) {
                                                                        Thread.sleep(100L);
                                                                    }
                                                                }
                                                                catch (Exception e) {
                                                                    this.printStackTrace(e, 1);
                                                                    this.line("Error:" + e);
                                                                }
                                                            }
                                                            break block717;
                                                        }
                                                        start_pos1 = start_pos;
                                                        start_pos2 = start_pos;
                                                        the_dir = this.vars.replace_vars_line_url(the_dir, source_stat, "{", "}");
                                                        the_dir_dest = this.vars.replace_vars_line_url(the_dir_dest, dest_stat, "{", "}");
                                                        the_dir = this.vars.replace_vars_line_date(the_dir, source_stat, "{", "}");
                                                        the_dir_dest = this.vars.replace_vars_line_date(the_dir_dest, dest_stat, "{", "}");
                                                        skip = false;
                                                        if (this.prefs.getProperty("skip_modified_and_size", "true").equals("true") && dest_stat != null) {
                                                            if (dest_stat != null && Math.abs(Long.parseLong(source_stat.getProperty("modified", "0")) - Long.parseLong(dest_stat.getProperty("modified", "50000"))) < 1000L && source_stat.getProperty("size").equals(dest_stat.getProperty("size"))) {
                                                                skip = true;
                                                                if (!this.only_log) {
                                                                    this.line(String.valueOf(the_dir) + ":Skipping item because of matching modified date and size (skip_modified_and_size)");
                                                                }
                                                                this.print_prompt();
                                                            }
                                                        } else if (this.prefs.getProperty("skip_modified", "false").equals("true") && dest_stat != null) {
                                                            if (dest_stat != null && Math.abs(Long.parseLong(source_stat.getProperty("modified", "0")) - Long.parseLong(dest_stat.getProperty("modified", "50000"))) < 1000L) {
                                                                skip = true;
                                                                if (!this.only_log) {
                                                                    this.line(String.valueOf(the_dir) + ":Skipping item because of matching modifed date (skip_modified)");
                                                                }
                                                                this.print_prompt();
                                                            }
                                                        } else if (this.prefs.getProperty("skip_size", "false").equals("true") && dest_stat != null && source_stat.getProperty("size").equals(dest_stat.getProperty("size"))) {
                                                            skip = true;
                                                            if (!this.only_log) {
                                                                this.line(String.valueOf(the_dir) + ":Skipping item because of matching size (skip_size)");
                                                            }
                                                            this.print_prompt();
                                                        }
                                                        if (skip) {
                                                            p = this.stats;
                                                            synchronized (p) {
                                                                this.stats.put("upload_skipped_count", String.valueOf(Integer.parseInt(this.stats.getProperty("upload_skipped_count", "0")) + 1));
                                                                this.stats.put("upload_skipped_bytes", String.valueOf(Long.parseLong(this.stats.getProperty("upload_skipped_bytes", "0")) + Long.parseLong(source_stat.getProperty("size"))));
                                                            }
                                                            this.freeClient(c1);
                                                            this.freeClient(c2);
                                                            this.print_prompt();
                                                            break block717;
                                                        }
                                                        if (this.only_log) {
                                                            this.line("PUT \"" + the_dir + "\" \"" + the_dir_dest + "\" \"###::Copying item because its missing or different.\"");
                                                            break block717;
                                                        }
                                                        if (command[0].toUpperCase().startsWith("APPE")) {
                                                            start_pos1 = 0L;
                                                            resume = false;
                                                        }
                                                        in = in_f = c1.download(the_dir, start_pos1, -1L, true);
                                                        c1.setConfig("transfer_direction", "GET");
                                                        c2.setConfig("transfer_direction", "PUT");
                                                        c1.setConfig("transfer_path_src", the_dir);
                                                        c2.setConfig("transfer_path_dst", the_dir_dest);
                                                        c2.setConfig("transfer_path_src", the_dir);
                                                        c1.setConfig("transfer_path_dst", the_dir_dest);
                                                        c1.setConfig("transfer_stats", "true");
                                                        c2.setConfig("transfer_stats", "true");
                                                        c2.setConfig("transfer_content_length", source_stat.getProperty("size"));
                                                        if (this.pending_transfer_queue.size() == 0 && this.prefs.getProperty("makedir_before", "true").equals("true")) {
                                                            c2.makedirs(Common.all_but_last(the_dir_dest));
                                                        }
                                                        out = out_f = c2.upload(String.valueOf(the_dir_dest) + this.prefs.getProperty("upload_temp_ext", ""), start_pos2, true, true);
                                                        this.line("Upload started:" + the_dir + " -> " + the_dir_dest + (resume != false ? " : Resuming from position:" + start_pos : ""));
                                                        if (multithreaded) {
                                                            this.print_prompt();
                                                        }
                                                        the_dir_dest_f = the_dir_dest;
                                                        the_dir_source_f = the_dir;
                                                        source_stat_f = source_stat;
                                                        start = System.currentTimeMillis();
                                                        c2.setConfig("transfer_start", String.valueOf(start));
                                                        c2.setConfig("transfer_bytes_total", String.valueOf(source_size));
                                                        c1.setConfig("transfer_start", String.valueOf(start));
                                                        c1.setConfig("transfer_bytes_total", String.valueOf(source_size));
                                                        c2.setConfig("transfer_bytes", "0");
                                                        c1.setConfig("transfer_bytes", "0");
                                                        c2.setConfig("transfer_history", new Vector<E>());
                                                        c1.setConfig("transfer_history", new Vector<E>());
                                                        c2.setConfig("transfer_bytes_last", "0");
                                                        c1.setConfig("transfer_bytes_last", "0");
                                                        c2.setConfig("transfer_bytes_last_interval", "0");
                                                        c2.setConfig("transfer_content_length", source_stat.getProperty("size"));
                                                        c1.setConfig("transfer_bytes_last_interval", "0");
                                                        c1.setConfig("abort_obj", null);
                                                        c2.setConfig("abort_obj", null);
                                                        c2.last_md5_buf = last_md5_buf = new StringBuffer();
                                                        if (!multithreaded) {
                                                            c2_f = c2;
                                                            Worker.startWorker(new Runnable(){

                                                                @Override
                                                                public void run() {
                                                                    while (c2_f.getConfig("transfer_stats") != null) {
                                                                        Client.this.printStats(false);
                                                                        try {
                                                                            Thread.sleep(1000L);
                                                                        }
                                                                        catch (InterruptedException e) {
                                                                            Client.this.printStackTrace(e, 1);
                                                                        }
                                                                    }
                                                                }
                                                            });
                                                        }
                                                        total_bytes = 0L;
                                                        md5 = new MD5Calculator(this.prefs.getProperty("md5sum_native_exec", "false").equals("true"), "md5", this.prefs.getProperty("md5_check", "true").equals("true"));
                                                        try {
                                                            try {
                                                                b = new byte[32768];
                                                                bytes_read = 0;
                                                                total_bytes = resume != false ? start_pos : 0L;
                                                                v11 = slow_speed = this.prefs.getProperty("slow_transfer", "0").equals("0") == false;
                                                                if (slow_speed) {
                                                                    b = new byte[1];
                                                                }
                                                                last_stat_msg = start;
                                                                single_prefs_update_interval = Long.parseLong(this.prefs.getProperty("update_interval", "1"));
                                                                removed_pending = false;
                                                                while (bytes_read >= 0) {
                                                                    bytes_read = in_f.read(b);
                                                                    if (c1.getConfig("abort_obj") == c1 || c2.getConfig("abort_obj") == c2) {
                                                                        aborted = true;
                                                                        throw new IOException("File transfer cancelled:" + the_dir_dest);
                                                                    }
                                                                    if (bytes_read >= 0) {
                                                                        out_f.write(b, 0, bytes_read);
                                                                        md5.update(b, 0, bytes_read);
                                                                        if (slow_speed) {
                                                                            Thread.sleep(Integer.parseInt(this.prefs.getProperty("slow_transfer", "0")));
                                                                        }
                                                                        total_bytes += (long)bytes_read;
                                                                    }
                                                                    c2.setConfig("transfer_bytes", String.valueOf(total_bytes));
                                                                    c1.setConfig("transfer_bytes", String.valueOf(total_bytes));
                                                                    if (this.single_command_line_mode && System.currentTimeMillis() - last_stat_msg > 1000L * single_prefs_update_interval) {
                                                                        if (this.prefs.getProperty("client_debug", "false").equals("true")) {
                                                                            this.printStats(false);
                                                                        }
                                                                        last_stat_msg = System.currentTimeMillis();
                                                                    }
                                                                    if (removed_pending) continue;
                                                                    removed_pending = true;
                                                                    this.pending_transfer_queue_inprogress.removeElement(command_str);
                                                                }
                                                                in_f.close();
                                                                out_f.close();
                                                                c1.close();
                                                                c2.close();
                                                                if (!this.prefs.getProperty("upload_temp_ext", "").equals("")) {
                                                                    c2.rename(String.valueOf(the_dir_dest_f) + this.prefs.getProperty("upload_temp_ext", ""), the_dir_dest_f, true);
                                                                }
                                                                c1.setConfig("transfer_stats", null);
                                                                c2.setConfig("transfer_stats", null);
                                                                if (!multithreaded) {
                                                                    this.printStats(true);
                                                                }
                                                                speed_str = (speed = 10.0f * ((float)total_bytes / 1024.0f / ((float)(System.currentTimeMillis() - start) / 1000.0f))) > 10240.0f ? String.valueOf((float)((int)(speed / 1024.0f)) / 10.0f) + "MB/sec" : String.valueOf((float)((double)((int)speed) / 10.0)) + "KB/sec";
                                                                md5Str = md5.getHash();
                                                                vrl = (VRL)credentials.get("vrl");
                                                                ended = System.currentTimeMillis();
                                                                loops = 0;
                                                                remote_last_md5 = last_md5_buf.toString();
                                                                while (remote_last_md5.trim().equals("") && System.currentTimeMillis() - ended < 10000L) {
                                                                    Thread.sleep(loops++);
                                                                    remote_last_md5 = last_md5_buf.toString();
                                                                }
                                                                if (vrl.getProtocol().toLowerCase().startsWith("http") && !md5Str.equals(remote_last_md5) && this.prefs.getProperty("md5_check", "true").equals("true") && !remote_last_md5.equals("UNSUPPORTED") && !remote_last_md5.toUpperCase().equals("DISABLED")) {
                                                                    c2.delete(the_dir_dest_f);
                                                                    throw new Exception(String.valueOf(the_dir_source_f) + " md5 mismatch:local=" + md5Str + " remote=" + remote_last_md5);
                                                                }
                                                                success_end_str = " : " + (System.currentTimeMillis() - start) + "ms, " + speed_str + ", size=" + total_bytes + ", md5=" + md5Str + (md5Str.equals(remote_last_md5) != false && this.prefs.getProperty("md5_check", "true").equals("true") != false ? " (validated)" : "");
                                                                this.line("Upload completed:" + the_dir_source_f + " -> " + the_dir_dest_f + success_end_str);
                                                                if (command[0].toUpperCase().startsWith("MOVE")) {
                                                                    if (c1.delete(the_dir)) {
                                                                        this.line("Local file deleted:" + the_dir_source_f);
                                                                    } else {
                                                                        this.line("Local file delete failed:" + the_dir_source_f);
                                                                    }
                                                                }
                                                                if (c1 != null) {
                                                                    c1 = this.freeClient(c1);
                                                                }
                                                                if (c2 != null) {
                                                                    c2 = this.freeClient(c2);
                                                                }
                                                                if (this.prefs.getProperty("keep_date", "true").equals("true")) {
                                                                    sdf_yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                                                                    if (!this.only_log) {
                                                                        this.process_command("MDTM \"" + the_dir_dest_f + "\" " + sdf_yyyyMMddHHmmss.format(new Date(Long.parseLong(source_stat_f.getProperty("modified")))), true);
                                                                    }
                                                                }
                                                                this.print_prompt();
                                                            }
                                                            catch (Exception e) {
                                                                transfer_error = e;
                                                                other_error = false;
                                                                try {
                                                                    in_f.close();
                                                                }
                                                                catch (Exception slow_speed) {
                                                                    // empty catch block
                                                                }
                                                                try {
                                                                    out_f.close();
                                                                }
                                                                catch (Exception e2) {
                                                                    transfer_error = e2;
                                                                    msg = e2.getMessage();
                                                                    if (msg.toUpperCase().indexOf("ERROR:") < 0) {
                                                                        msg = "Error: " + msg;
                                                                    }
                                                                    this.line(msg);
                                                                    other_error = true;
                                                                }
                                                                if (!other_error) {
                                                                    msg = e.getMessage();
                                                                    if (msg.toUpperCase().indexOf("ERROR:") < 0) {
                                                                        msg = "Error: " + msg;
                                                                    }
                                                                    this.line(msg);
                                                                }
                                                                md5.close();
                                                                try {
                                                                    c1.doCommand("ABOR");
                                                                    c1.setConfig("abort_obj", null);
                                                                    c1.close();
                                                                }
                                                                catch (Exception v12) {}
                                                                try {
                                                                    c2.doCommand("ABOR");
                                                                    c2.setConfig("abort_obj", null);
                                                                    c2.close();
                                                                }
                                                                catch (Exception v13) {}
                                                                break block719;
                                                            }
                                                        }
                                                        catch (Throwable var60_390) {
                                                            md5.close();
                                                            try {
                                                                c1.doCommand("ABOR");
                                                                c1.setConfig("abort_obj", null);
                                                                c1.close();
                                                            }
                                                            catch (Exception v14) {}
                                                            try {
                                                                c2.doCommand("ABOR");
                                                                c2.setConfig("abort_obj", null);
                                                                c2.close();
                                                            }
                                                            catch (Exception v15) {}
                                                            throw var60_390;
                                                        }
                                                        md5.close();
                                                        try {
                                                            c1.doCommand("ABOR");
                                                            c1.setConfig("abort_obj", null);
                                                            c1.close();
                                                        }
                                                        catch (Exception v16) {}
                                                        try {
                                                            c2.doCommand("ABOR");
                                                            c2.setConfig("abort_obj", null);
                                                            c2.close();
                                                        }
                                                        catch (Exception v17) {}
                                                    }
                                                    if (c1 != null) {
                                                        c1 = this.freeClient(c1);
                                                    }
                                                    if (c2 != null) {
                                                        c2 = this.freeClient(c2);
                                                    }
                                                    if (!folder) {
                                                        this.recent_transfers_upload.addElement(source_stat_f);
                                                    }
                                                    e = this.stats;
                                                    synchronized (e) {
                                                        this.stats.put("upload_count", String.valueOf(Integer.parseInt(this.stats.getProperty("upload_count", "0")) + 1));
                                                        this.stats.put("upload_bytes", String.valueOf(Long.parseLong(this.stats.getProperty("upload_bytes", "0")) + total_bytes - start_pos));
                                                        // MONITOREXIT @DISABLED, blocks:[10, 91, 123, 221, 78] lbl1978 : MonitorExitStatement: MONITOREXIT : e
                                                        if (true) ** GOTO lbl1993
                                                    }
                                                    do {
                                                        this.recent_transfers_upload.remove(0);
lbl1993:
                                                        // 2 sources

                                                    } while (this.recent_transfers_upload.size() > 1000);
                                                    while (this.recent_transfers_download.size() > 1000) {
                                                        this.recent_transfers_download.remove(0);
                                                    }
                                                    idle_time = 0;
                                                    while (this.source_used.size() == 0 && this.pending_transfer_queue.size() == 0 && !this.prefs.containsKey("aborting") && this.retry_active.size() == 0 && transfer_error == null && transfer_error == null) {
                                                        if (++idle_time > 10) break;
                                                        Thread.sleep(100L);
                                                    }
                                                    if (idle_time >= 10 && transfer_error == null && !this.single_command_line_mode) {
                                                        this.line("Transfer complete.  " + this.stats_summary());
                                                        this.print_prompt();
                                                    }
                                                }
                                                catch (Exception e) {
                                                    transfer_error = e;
                                                    if (in != null) {
                                                        in.close();
                                                    }
                                                    if (c1 != null) {
                                                        c1.close();
                                                    }
                                                    if (out != null) {
                                                        out.close();
                                                    }
                                                    this.freeClient(c1);
                                                    this.freeClient(c2);
                                                    this.printStackTrace(e, 1);
                                                    this.line("Error:" + e);
                                                }
                                            }
                                            if (aborted || (!resume || transfer_error == null) && (transfer_error == null || this.prefs.containsKey("aborting") || ("" + transfer_error).indexOf("403") >= 0 || ("" + transfer_error).indexOf("404") >= 0 || ("" + transfer_error).indexOf("denied") >= 0 || ("" + transfer_error).indexOf("abort") >= 0 || ("" + transfer_error).indexOf("cancel") >= 0 || ("" + transfer_error).indexOf("not allowed") >= 0 || ("" + transfer_error).indexOf("failure") >= 0 || ("" + transfer_error).indexOf("exceed") >= 0)) {
                                                if (transfer_error != null) {
                                                    if (!this.single_command_line_mode) {
                                                        this.line("Ended with error:" + transfer_error + ":" + command_str);
                                                    }
                                                    this.failed_transfer_queue.addElement(String.valueOf(command_str) + "###:" + transfer_error.getMessage() + ":" + this.log_sdf.format(new Date()));
                                                    this.add_transfer_log("ERROR:" + command_str + ":" + transfer_error);
                                                    if (("" + transfer_error).indexOf("exceed") >= 0) {
                                                        this.process_command("ABOR", true);
                                                    }
                                                } else if (!folder && !this.only_log) {
                                                    this.add_transfer_log("SUCCESS:" + command_str + success_end_str);
                                                    this.success_transfer_queue.addElement(String.valueOf(command_str) + success_end_str);
                                                }
                                                while (this.success_transfer_queue.size() > 1000) {
                                                    this.success_transfer_queue.remove(0);
                                                }
                                                while (this.failed_transfer_queue.size() > 1000) {
                                                    this.failed_transfer_queue.remove(0);
                                                }
                                                this.prefs.put("auto_retry_delay", "1000");
                                            }
                                            if (!this.prefs.getProperty("auto_retry", "true").equals("true") || !this.destination_logged_in || !this.source_logged_in || ("" + transfer_error).indexOf("ERROR:Transfer already in progress") >= 0) break block738;
                                            i = Integer.parseInt(this.prefs.getProperty("auto_retry_delay", "1000"));
                                            slept = 100L;
                                            start_pos1 = this.retry_active;
                                            synchronized (start_pos1) {
                                                this.retry_active.addElement("active");
                                                // MONITOREXIT @DISABLED, blocks:[115, 10, 92, 221] lbl2051 : MonitorExitStatement: MONITOREXIT : start_pos1
                                                if (true) ** GOTO lbl2064
                                            }
                                            do {
                                                Thread.sleep(100L);
                                                slept += 100L;
lbl2064:
                                                // 2 sources

                                            } while (slept < i && !this.prefs.containsKey("aborting"));
                                            if (i > 15000L) {
                                                i = 15000L;
                                            }
                                            this.prefs.put("auto_retry_delay", String.valueOf(i * 2L));
                                            resume = resume == false || transfer_error == null || ("" + transfer_error).indexOf("403") < 0 && ("" + transfer_error).indexOf("404") < 0 && ("" + transfer_error).indexOf("denied") < 0 && ("" + transfer_error).indexOf("not allowed") < 0 && ("" + transfer_error).indexOf("failure") < 0;
                                            start_pos1 = this.retry_active;
                                            synchronized (start_pos1) {
                                                this.retry_active.removeElementAt(0);
                                                continue;
                                            }
                                        }
                                        if (transfer_error == null) continue;
                                        this.line("Ended with error:" + transfer_error + ":" + command_str);
                                        this.failed_transfer_queue.addElement(String.valueOf(command_str) + "###:" + transfer_error.getMessage() + ":" + this.log_sdf.format(new Date()));
                                        this.add_transfer_log("ERROR:" + command_str + ":" + transfer_error);
                                    }
                                }
                                if (command[0].toUpperCase().startsWith("DIFFPUT") || command[0].toUpperCase().startsWith("DIFFGET")) {
                                    if (this.only_log) ** GOTO lbl2804
                                    if (this.client_start_time == 0L) {
                                        this.client_start_time = System.currentTimeMillis();
                                    }
                                    if (!this.destination_logged_in) {
                                        this.line("Not connected to dest.");
                                        return null;
                                    }
                                    if (!this.source_logged_in) {
                                        this.line("Not connected to source.");
                                        return null;
                                    }
                                    this.freeClient(c);
                                    c = null;
                                    upload = command[0].toUpperCase().startsWith("DIFFPUT");
                                    c_source = this.getClient(upload == false);
                                    c_dest = this.getClient(upload);
                                    the_dir = this.getArgs(command, 1, false, upload);
                                    the_dir_opposite = this.getArgs(command, 2, false, upload == false);
                                    if (the_dir_opposite.endsWith("/") && !the_dir.endsWith("/")) {
                                        the_dir_opposite = String.valueOf(the_dir_opposite) + Common.last(the_dir);
                                    }
                                    in = null;
                                    out = null;
                                    source_stat = c_source.stat(the_dir_opposite);
                                    dest_stat = c_dest.stat(the_dir);
                                    folder = false;
                                    if (dest_stat != null && dest_stat.getProperty("type").equalsIgnoreCase("DIR")) {
                                        folder = true;
                                    }
                                    if (folder) {
                                        this.startupThreads();
                                        if (!the_dir_opposite.endsWith("/")) {
                                            the_dir_opposite = String.valueOf(the_dir_opposite) + "/";
                                        }
                                        if (!the_dir.endsWith("/")) {
                                            the_dir = String.valueOf(the_dir) + "/";
                                        }
                                        this.process_command(String.valueOf(upload != false ? "" : "L") + "MKD \"" + the_dir_opposite + "\"", true);
                                        list = new Vector<E>();
                                        c_dest.list(the_dir, list);
                                        this.freeClient(c_source);
                                        this.freeClient(c_dest);
                                        while (list.size() > 0) {
                                            p = (Properties)list.remove(0);
                                            if (this.prefs.containsKey("aborting")) {
                                                list.removeAllElements();
                                                this.pending_transfer_queue.removeAllElements();
                                                this.pending_transfer_queue_inprogress.removeAllElements();
                                            }
                                            try {
                                                if (p.getProperty("type").equalsIgnoreCase("DIR")) {
                                                    this.process_command(String.valueOf(upload != false ? "" : "L") + "MKD \"" + the_dir_opposite + p.getProperty("name") + "\"", true);
                                                }
                                                this.pending_transfer_queue.addElement(String.valueOf(command[0]) + " \"" + the_dir + p.getProperty("name") + (p.getProperty("type").equalsIgnoreCase("DIR") != false ? "/" : "") + "\" \"" + the_dir_opposite + p.getProperty("name") + (p.getProperty("type").equalsIgnoreCase("DIR") != false ? "/" : "") + "\"");
                                                while (this.pending_transfer_queue.size() > 1000000) {
                                                    Thread.sleep(100L);
                                                }
                                            }
                                            catch (Exception e) {
                                                this.printStackTrace(e, 1);
                                                this.line("Error:" + e);
                                            }
                                        }
                                    }
                                    skip = false;
                                    if (this.prefs.getProperty("skip_modified_and_size", "true").equals("true") && source_stat != null && dest_stat != null) {
                                        if (dest_stat != null && Math.abs(Long.parseLong(source_stat.getProperty("modified", "0")) - Long.parseLong(dest_stat.getProperty("modified", "50000"))) < 1000L && source_stat.getProperty("size").equals(dest_stat.getProperty("size"))) {
                                            skip = true;
                                            this.line(String.valueOf(the_dir_opposite) + ":Skipping item because of matching modified date and size (skip_modified_and_size)");
                                            this.print_prompt();
                                        }
                                    } else if (this.prefs.getProperty("skip_modified", "false").equals("true") && source_stat != null && dest_stat != null) {
                                        if (dest_stat != null && Math.abs(Long.parseLong(source_stat.getProperty("modified", "0")) - Long.parseLong(dest_stat.getProperty("modified", "50000"))) < 1000L) {
                                            skip = true;
                                            this.line(String.valueOf(the_dir_opposite) + ":Skipping item because of matching modifed date (skip_modified)");
                                            this.print_prompt();
                                        }
                                    } else if (this.prefs.getProperty("skip_size", "false").equals("true") && source_stat != null && dest_stat != null && source_stat.getProperty("size").equals(dest_stat.getProperty("size"))) {
                                        skip = true;
                                        this.line(String.valueOf(the_dir_opposite) + ":Skipping item because of matching size: (skip_size)");
                                        this.print_prompt();
                                    }
                                    if (skip) {
                                        this.freeClient(c_source);
                                        this.freeClient(c_dest);
                                    }
                                    if (source_stat == null) {
                                        source_stat = new Properties();
                                        source_stat.put("size", "0");
                                    }
                                    if (dest_stat == null) {
                                        dest_stat = new Properties();
                                        dest_stat.put("size", "0");
                                    }
                                    this.line(String.valueOf(the_dir_opposite) + ":Diff transfer starting...modified=" + source_stat.getProperty("modified") + " vs. " + dest_stat.getProperty("modified") + "  size=" + source_stat.getProperty("size") + " vs. " + dest_stat.getProperty("size"));
                                    byteRanges = this.diff(the_dir_opposite, the_dir, source_stat, dest_stat, c_source, c_dest);
                                    total_bytes_summary = 0L;
                                    aborted = false;
                                    x = 0;
                                    while (x < byteRanges.size()) {
                                        try {
                                            c_source.setConfig("transfer_direction", upload != false ? "PUT" : "GET");
                                            c_dest.setConfig("transfer_direction", upload != false ? "PUT" : "GET");
                                            c_source.setConfig("transfer_path_src", the_dir);
                                            c_dest.setConfig("transfer_path_dst", the_dir_opposite);
                                            c_dest.setConfig("transfer_path_src", the_dir);
                                            c_source.setConfig("transfer_path_dst", the_dir_opposite);
                                            c_source.setConfig("transfer_stats", "true");
                                            c_dest.setConfig("transfer_stats", "true");
                                            range = byteRanges.elementAt(x).toString();
                                            start_pos = Long.parseLong(range.split("-")[0]);
                                            end_pos = -1L;
                                            if (range.split("-").length > 1) {
                                                end_pos = Long.parseLong(range.substring(range.indexOf("-") + 1));
                                            }
                                            start = System.currentTimeMillis();
                                            c_dest.setConfig("transfer_start", String.valueOf(start));
                                            item_size = end_pos - start_pos;
                                            if (item_size < 0L && upload) {
                                                item_size = Long.parseLong(dest_stat.getProperty("size"));
                                            }
                                            if (item_size < 0L && !upload) {
                                                item_size = Long.parseLong(dest_stat.getProperty("size"));
                                            }
                                            c_dest.setConfig("transfer_bytes_total", String.valueOf(item_size));
                                            c_source.setConfig("transfer_start", String.valueOf(start));
                                            c_source.setConfig("transfer_bytes_total", String.valueOf(item_size));
                                            c_dest.setConfig("transfer_bytes", "0");
                                            c_source.setConfig("transfer_bytes", "0");
                                            c_dest.setConfig("transfer_history", new Vector<E>());
                                            c_source.setConfig("transfer_history", new Vector<E>());
                                            c_dest.setConfig("transfer_bytes_last", "0");
                                            c_source.setConfig("transfer_bytes_last", "0");
                                            c_dest.setConfig("transfer_bytes_last_interval", "0");
                                            c_source.setConfig("transfer_bytes_last_interval", "0");
                                            c_dest.setConfig("abort_obj", null);
                                            c_source.setConfig("abort_obj", null);
                                            c_f = c_dest;
                                            if (!multithreaded) {
                                                Worker.startWorker(new Runnable(){

                                                    @Override
                                                    public void run() {
                                                        while (c_f.getConfig("transfer_stats") != null) {
                                                            Client.this.printStats(false);
                                                            try {
                                                                Thread.sleep(1000L);
                                                            }
                                                            catch (InterruptedException e) {
                                                                Client.this.printStackTrace(e, 1);
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                            in = in_f = c_dest.download(the_dir, start_pos, end_pos, true);
                                            truncate = false;
                                            if (Long.parseLong(dest_stat.getProperty("size")) < Long.parseLong(source_stat.getProperty("size")) && x == byteRanges.size() - 1) {
                                                truncate = true;
                                            }
                                            out = out_f = c_source.upload(the_dir_opposite, start_pos, truncate, true);
                                            this.line("\r\n" + the_dir_opposite + ":Started transferring part " + (x + 1) + " of " + byteRanges.size() + " at position " + start_pos + " to " + (end_pos == -1L ? "end" : String.valueOf(end_pos)) + ".");
                                            if (multithreaded) {
                                                this.print_prompt();
                                            }
                                            b = new byte[32768];
                                            bytes_read = 0;
                                            total_bytes = start_pos;
                                            try {
                                                v21 = slow_speed = this.prefs.getProperty("slow_transfer", "0").equals("0") == false;
                                                if (slow_speed) {
                                                    b = new byte[1];
                                                }
                                                while (bytes_read >= 0) {
                                                    bytes_read = in_f.read(b);
                                                    if (bytes_read >= 0) {
                                                        if (c_source.getConfig("abort_obj") == c_source || c_dest.getConfig("abort_obj") == c_dest) {
                                                            aborted = true;
                                                            throw new IOException("File transfer cancelled:" + the_dir);
                                                        }
                                                        out_f.write(b, 0, bytes_read);
                                                        if (slow_speed) {
                                                            Thread.sleep(Integer.parseInt(this.prefs.getProperty("slow_transfer", "0")));
                                                        }
                                                        total_bytes += (long)bytes_read;
                                                        total_bytes_summary += (long)bytes_read;
                                                    }
                                                    c_source.setConfig("transfer_bytes", String.valueOf(total_bytes));
                                                    c_dest.setConfig("transfer_bytes", String.valueOf(total_bytes));
                                                }
                                            }
                                            finally {
                                                try {
                                                    in_f.close();
                                                }
                                                catch (Exception var43_352) {}
                                                try {
                                                    out_f.close();
                                                }
                                                catch (Exception var43_353) {}
                                                c_source.setConfig("abort_obj", null);
                                                c_dest.setConfig("abort_obj", null);
                                            }
                                            c_source.setConfig("transfer_stats", null);
                                            c_dest.setConfig("transfer_stats", null);
                                            this.printStats(false);
                                            this.line("\r\n" + the_dir_opposite + ":Finished transferring part " + (x + 1) + " of " + byteRanges.size() + " at position " + start_pos + " to " + (end_pos == -1L ? "end" : String.valueOf(end_pos)) + ": " + (System.currentTimeMillis() - start) + "ms");
                                            if (multithreaded) {
                                                this.print_prompt();
                                            }
                                        }
                                        catch (Exception e) {
                                            if (in != null) {
                                                in.close();
                                            }
                                            if (c_dest != null) {
                                                c_dest.close();
                                            }
                                            if (out != null) {
                                                out.close();
                                            }
                                            this.printStackTrace(e, 1);
                                            this.line("Error:" + e);
                                        }
                                        ++x;
                                    }
                                    if (byteRanges.size() > 0) {
                                        if (upload) {
                                            this.recent_transfers_upload.addElement(dest_stat);
                                            x = this.stats;
                                            synchronized (x) {
                                                this.stats.put("upload_count", String.valueOf(Integer.parseInt(this.stats.getProperty("upload_count", "0")) + 1));
                                                this.stats.put("upload_bytes", String.valueOf(Long.parseLong(this.stats.getProperty("upload_bytes", "0")) + total_bytes_summary));
                                            }
                                        }
                                        this.recent_transfers_download.addElement(source_stat);
                                        x = this.stats;
                                        synchronized (x) {
                                            this.stats.put("download_count", String.valueOf(Integer.parseInt(this.stats.getProperty("download_count", "0")) + 1));
                                            this.stats.put("download_bytes", String.valueOf(Long.parseLong(this.stats.getProperty("download_bytes", "0")) + total_bytes_summary));
                                        }
                                        if (this.prefs.getProperty("keep_date", "true").equals("true")) {
                                            sdf_yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                                            this.process_command(String.valueOf(upload != false ? "" : "L") + "MDTM \"" + the_dir_opposite + "\" " + sdf_yyyyMMddHHmmss.format(new Date(Long.parseLong(dest_stat.getProperty("modified")))), true);
                                        }
                                    }
                                    while (this.recent_transfers_upload.size() > 1000) {
                                        this.recent_transfers_upload.remove(0);
                                    }
                                    while (this.recent_transfers_download.size() > 1000) {
                                        this.recent_transfers_download.remove(0);
                                    }
                                    c = null;
                                    this.freeClient(c_source);
                                    this.freeClient(c_dest);
                                    if (byteRanges.size() > 0) {
                                        this.line(String.valueOf(the_dir_opposite) + ":Diff transfer completed, " + byteRanges.size() + " parts transferred.  " + this.stats_summary());
                                    } else {
                                        this.line(String.valueOf(the_dir_opposite) + ":Diff transfer completed, no changes.  " + this.stats_summary());
                                    }
                                    if (!multithreaded) ** GOTO lbl2804
                                    this.print_prompt();
                                }
                                if (command[0].toUpperCase().startsWith("DIFFDIR") || command[0].toUpperCase().startsWith("LDIFFDIR")) {
                                    this.only_log = true;
                                    this.line("#" + command[0] + " Started");
                                    this.process_command(String.valueOf(command[0].charAt(0) == 'L' ? "L" : "") + "PUTDEL" + command_str.substring(command_str.indexOf(" ")), true);
                                }
                                if (command[0].toUpperCase().startsWith("CACHE") || command[0].toUpperCase().startsWith("LCACHE")) {
                                    if (command[0].charAt(0) == 'L' && this.stats_cache_source == null) {
                                        this.stats_cache_source = new Properties();
                                        this.stats_cache_source.put("max_items", command[1]);
                                        this.stats_cache_source.put("max_time", command[2]);
                                        this.source_config.put("dmz_stat_caching", "true");
                                        this.line("#" + command[0] + " enabled");
                                    }
                                    if (command[0].charAt(0) == 'L' && this.stats_cache_source != null) {
                                        this.source_config.put("dmz_stat_caching", "false");
                                        this.stats_cache_source.clear();
                                        this.stats_cache_source = null;
                                        this.line("#" + command[0] + " disabled");
                                    }
                                    if (command[0].charAt(0) != 'L' && this.stats_cache_dest == null) {
                                        this.stats_cache_dest = new Properties();
                                        this.stats_cache_dest.put("max_items", command[1]);
                                        this.stats_cache_dest.put("max_time", command[2]);
                                        this.dest_config.put("dmz_stat_caching", "true");
                                        this.line("#" + command[0] + " enabled");
                                    }
                                    if (command[0].charAt(0) == 'L' || this.stats_cache_dest == null) ** GOTO lbl2804
                                    this.dest_config.put("dmz_stat_caching", "false");
                                    this.stats_cache_dest.clear();
                                    this.stats_cache_dest = null;
                                    this.line("#" + command[0] + " disabled");
                                }
                                if (command[0].toUpperCase().startsWith("DIFF")) {
                                    if (!this.destination_logged_in) {
                                        this.line("Not connected to dest.");
                                        return null;
                                    }
                                    if (!this.source_logged_in) {
                                        this.line("Not connected to source.");
                                        return null;
                                    }
                                    this.freeClient(c);
                                    c = null;
                                    the_dir = this.getArgs(command, 1, false, true);
                                    the_dir_dest = this.getArgs(command, 2, false, false);
                                    if (the_dir_dest.endsWith("/")) {
                                        the_dir_dest = String.valueOf(the_dir_dest) + Common.last(the_dir);
                                    }
                                    this.line(String.valueOf(the_dir_dest) + ":Diff starting...");
                                    c_source = this.getClient(true);
                                    c_dest = this.getClient(false);
                                    stat1 = c_dest.stat(the_dir_dest);
                                    stat2 = c_source.stat(the_dir);
                                    if (stat1 == null) {
                                        stat1 = new Properties();
                                        stat1.put("size", "0");
                                    }
                                    if (stat2 == null) {
                                        stat2 = new Properties();
                                        stat2.put("size", "0");
                                    }
                                    byteRanges = this.diff(the_dir_dest, the_dir, stat1, stat2, c_dest, c_source);
                                    x = 0;
                                    while (x < byteRanges.size()) {
                                        try {
                                            range = byteRanges.elementAt(x).toString();
                                            start_pos = Long.parseLong(range.split("-")[0]);
                                            end_pos = -1L;
                                            if (range.split("-").length > 1) {
                                                end_pos = Long.parseLong(range.substring(range.indexOf("-") + 1));
                                            }
                                            truncate = false;
                                            if (Long.parseLong(stat2.getProperty("size")) < Long.parseLong(stat1.getProperty("size")) && x == byteRanges.size() - 1) {
                                                truncate = true;
                                            }
                                            this.line(String.valueOf(the_dir_dest) + ":Part " + (x + 1) + " of " + byteRanges.size() + ": " + the_dir + " -> " + the_dir_dest + " (" + start_pos + "-" + end_pos + ") truncate=" + truncate);
                                        }
                                        catch (Exception e) {
                                            this.printStackTrace(e, 1);
                                            this.line("Error:" + e);
                                        }
                                        ++x;
                                    }
                                    c = null;
                                    this.freeClient(c_source);
                                    this.freeClient(c_dest);
                                    var63_245 = this.line(String.valueOf(the_dir_dest) + ":Diff completed, " + byteRanges.size() + " parts are different.");
                                    return var63_245;
                                }
                                if (command[0].toUpperCase().startsWith("WAIT") || command[0].toUpperCase().startsWith("LWAIT")) {
                                    this.abort_wait = false;
                                    this.freeClient(c);
                                    c = null;
                                    secs = 0L;
                                    loops = 0L;
                                    found = false;
                                    if (command.length > 1) {
                                        secs = Long.parseLong(command[1]);
                                    }
                                    start_wait = System.currentTimeMillis();
                                    last_activity = System.currentTimeMillis();
                                    while (!(this.destination_used.size() <= 0 && this.source_used.size() <= 0 && this.pending_transfer_queue.size() <= 0 && this.retry_active.size() <= 0 && System.currentTimeMillis() - last_activity >= 2000L || this.abort_wait)) {
                                        if (secs > 0L && System.currentTimeMillis() - start_wait > secs * 1000L) {
                                            throw new Exception("Timeout while waiting for transfers.");
                                        }
                                        Thread.sleep(100L);
                                        if (loops++ > 10L) {
                                            found = true;
                                            loops = 0L;
                                            this.printStats(true);
                                        }
                                        if (this.destination_used.size() <= 0 && this.source_used.size() <= 0 && this.pending_transfer_queue.size() <= 0 && this.retry_active.size() <= 0) continue;
                                        last_activity = System.currentTimeMillis();
                                    }
                                    if (!found) {
                                        this.line("No transfers in progress.");
                                    }
                                    this.print_prompt();
                                    this.abort_wait = false;
                                }
                                if (command[0].toUpperCase().startsWith("INFO") || command[0].toUpperCase().startsWith("LINFO")) {
                                    if (command.length > 1 && command[1].equalsIgnoreCase("CLEAR")) {
                                        this.recent_transfers_download.removeAllElements();
                                        this.recent_transfers_upload.removeAllElements();
                                    }
                                    this.printStats(true);
                                    if (command.length > 1 && command[1].equalsIgnoreCase("ALL")) {
                                        this.printDownloadsUploads();
                                    }
                                    Client.last_line_prompt = "message";
                                    this.print_prompt();
                                }
                                if (command[0].toUpperCase().startsWith("DUMPSTACK")) {
                                    this.line(Common.dumpStack("1.8.22"));
                                    Client.last_line_prompt = "message";
                                    this.print_prompt();
                                }
                                if (command[0].toUpperCase().startsWith("DUMPMEMORY")) {
                                    this.line(new HeapDumper().dump());
                                    Client.last_line_prompt = "message";
                                    this.print_prompt();
                                }
                                if (!command[0].toUpperCase().startsWith("JOB")) break block739;
                                if (!command[1].equalsIgnoreCase("remote")) break block740;
                                c_dest = this.getClient(false);
                                config = c_dest.config;
                                urlc = URLConnection.openConnection((VRL)credentials.get("vrl"), config);
                                urlc.setRequestMethod("POST");
                                urlc.setRequestProperty("Cookie", "CrushAuth=" + config.getProperty("crushAuth", "") + ";");
                                urlc.setUseCaches(false);
                                urlc.setDoOutput(true);
                                c2f = "";
                                if (!config.getProperty("crushAuth", "").equals("")) {
                                    c2f = config.getProperty("crushAuth", "").substring(config.getProperty("crushAuth", "").length() - 4);
                                }
                                extra_params = "";
                                if (command.length > 3) {
                                    x = 3;
                                    while (x < command.length) {
                                        extra_params = String.valueOf(extra_params) + "&" + command[x];
                                        ++x;
                                    }
                                }
                                urlc.getOutputStream().write(("c2f=" + c2f + "&command=testJobSchedule&scheduleName=" + GenericClient.u(command[2]) + extra_params).getBytes("UTF8"));
                                this.line(String.valueOf(command[1]) + " " + command[2]);
                                code = 302;
                                result = "";
                                try {
                                    code = urlc.getResponseCode();
                                    result = Common.consumeResponse(urlc.getInputStream());
                                }
                                catch (Exception e) {
                                    Common.log("HTTP_CLIENT", 1, e);
                                }
                                if (code != 302 && urlc.getURL().toString().indexOf("/WebInterface/login.html") >= 0) {
                                    code = 302;
                                }
                                urlc.disconnect();
                                if (result.indexOf("<response>") >= 0) {
                                    result = result.substring(result.indexOf("<response>") + "<response>".length(), result.lastIndexOf("</response>"));
                                }
                                this.line(result);
                                this.freeClient(c_dest);
                                break block741;
                            }
                            if (!command[1].equalsIgnoreCase("status")) break block742;
                            c_dest = this.getClient(false);
                            config = c_dest.config;
                            urlc = URLConnection.openConnection((VRL)credentials.get("vrl"), config);
                            urlc.setRequestMethod("POST");
                            urlc.setRequestProperty("Cookie", "CrushAuth=" + config.getProperty("crushAuth", "") + ";");
                            urlc.setUseCaches(false);
                            urlc.setDoOutput(true);
                            c2f = "";
                            if (!config.getProperty("crushAuth", "").equals("")) {
                                c2f = config.getProperty("crushAuth", "").substring(config.getProperty("crushAuth", "").length() - 4);
                            }
                            urlc.getOutputStream().write(("c2f=" + c2f + "&command=getJobsSummary&type=text&end_time=" + System.currentTimeMillis() + "&scheduleName=" + command[2]).getBytes("UTF8"));
                            this.line(String.valueOf(command[1]) + " " + command[2]);
                            code = 302;
                            result = "";
                            try {
                                code = urlc.getResponseCode();
                                result = Common.consumeResponse(urlc.getInputStream());
                            }
                            catch (Exception e) {
                                Common.log("HTTP_CLIENT", 1, e);
                            }
                            if (code != 302 && urlc.getURL().toString().indexOf("/WebInterface/login.html") >= 0) {
                                code = 302;
                            }
                            urlc.disconnect();
                            if (result.indexOf("<response>") >= 0) {
                                result = result.substring(result.indexOf("<response>") + "<response>".length(), result.lastIndexOf("</response>"));
                            }
                            this.line(result);
                            this.freeClient(c_dest);
                            break block741;
                        }
                        job_log = new Vector<E>();
                        job_name = command[1];
                        job_status = new Properties();
                        Worker.startWorker(new Runnable(){

                            @Override
                            public void run() {
                                try {
                                    try {
                                        Client.runLocalJob(job_name, job_log);
                                    }
                                    catch (Exception e) {
                                        Common.log("HTTP_CLIENT", 1, e);
                                        job_status.put("error", e);
                                        job_status.put("status", "error");
                                        job_status.put("status", "complete");
                                    }
                                }
                                finally {
                                    job_status.put("status", "complete");
                                }
                            }
                        });
                        ** GOTO lbl2590
                        {
                            this.line("" + job_log.remove(0));
                            do {
                                if (job_log.size() > 0) continue block247;
                                Thread.sleep(100L);
lbl2590:
                                // 2 sources

                            } while (!job_status.containsKey("status") || job_log.size() > 0);
                        }
                        this.line(String.valueOf(command_str) + " " + job_status.getProperty("status") + ".");
                        if (job_status.containsKey("error")) {
                            throw (Exception)job_status.get("error");
                        }
                    }
                    Client.last_line_prompt = "message";
                    this.print_prompt();
                }
                if (command[0].toUpperCase().startsWith("USER")) {
                    c_dest = this.getClient(false);
                    config = c_dest.config;
                    urlc = URLConnection.openConnection((VRL)credentials.get("vrl"), config);
                    urlc.setRequestMethod("POST");
                    urlc.setRequestProperty("Cookie", "CrushAuth=" + config.getProperty("crushAuth", "") + ";");
                    urlc.setUseCaches(false);
                    urlc.setDoOutput(true);
                    c2f = "";
                    if (!config.getProperty("crushAuth", "").equals("")) {
                        c2f = config.getProperty("crushAuth", "").substring(config.getProperty("crushAuth", "").length() - 4);
                    }
                    if (command[1].equalsIgnoreCase("user_add") || command[1].equalsIgnoreCase("user_update") || command[1].equalsIgnoreCase("user_delete")) {
                        data_action = "replace";
                        if (command[1].equalsIgnoreCase("user_update")) {
                            data_action = "update";
                        } else if (command[1].equalsIgnoreCase("user_delete")) {
                            data_action = "delete";
                        }
                        urlc.getOutputStream().write(("c2f=" + c2f + "&command=setUserItem&xmlItem=user&data_action=" + data_action + "&serverGroup=" + GenericClient.u(command[2]) + "&username=" + GenericClient.u(command[3])).getBytes("UTF8"));
                        p = new Properties();
                        x = 4;
                        while (x < command.length) {
                            key = command[x].split("=")[0].trim();
                            val = command[x].substring(command[x].indexOf("=") + 1).trim();
                            if (Common.url_decode(key).equals("linked_vfs")) {
                                v = new Vector<String>();
                                users = null;
                                users = val.indexOf(":") >= 0 ? val.split(":") : val.split(",");
                                xx = 0;
                                while (xx < users.length) {
                                    v.add(users[xx]);
                                    ++xx;
                                }
                                p.put("linked_vfs", v);
                            } else {
                                p.put(Common.url_decode(key), Common.url_decode(val));
                            }
                            ++x;
                        }
                        p.put("root_dir", "/");
                        p.put("username", command[3]);
                        p.put("user_name", command[3]);
                        user_xml = Common.getXMLString(p, "user");
                        urlc.getOutputStream().write(("&user=" + GenericClient.u(user_xml)).getBytes("UTF8"));
                        permissions = new Properties();
                        permissions.put("/", "(read)(view)(resume)");
                        permissions_xml = Common.getXMLString(permissions, "privs");
                        if (data_action.equals("replace")) {
                            urlc.getOutputStream().write(("&permissions=" + GenericClient.u(permissions_xml)).getBytes("UTF8"));
                            vfs_items_xml = Common.getXMLString(new Vector<E>(), "VFS");
                            urlc.getOutputStream().write(("&vfs_items=" + GenericClient.u(vfs_items_xml)).getBytes("UTF8"));
                        }
                        this.line(String.valueOf(command[1]) + " " + command[3] + " " + p.toString());
                    } else if (command[1].equalsIgnoreCase("vfs_add") || command[1].equalsIgnoreCase("vfs_delete")) {
                        data_action = "update_vfs";
                        if (command[1].equalsIgnoreCase("vfs_delete")) {
                            data_action = "update_vfs_remove";
                        }
                        urlc.getOutputStream().write(("c2f=" + c2f + "&command=setUserItem&xmlItem=user&data_action=" + data_action + "&serverGroup=" + GenericClient.u(command[2]) + "&username=" + GenericClient.u(command[3])).getBytes("UTF8"));
                        vfs_items = new Vector<Properties>();
                        permissions = new Properties();
                        p = new Properties();
                        p_parent = new Properties();
                        vfs_items.addElement(p_parent);
                        vfs_item = new Vector<Properties>();
                        vfs_item.addElement(p);
                        p_parent.put("vfs_item", vfs_item);
                        x = 4;
                        while (x < command.length) {
                            key = command[x].split("=")[0].trim();
                            val = command[x].substring(command[x].indexOf("=") + 1).trim();
                            p.put(Common.url_decode(key), Common.url_decode(val));
                            ++x;
                        }
                        path = p.remove("path").toString();
                        p_parent.put("path", Common.all_but_last(path));
                        p_parent.put("name", p.remove("name"));
                        if (!path.endsWith("/")) {
                            path = String.valueOf(path) + "/";
                        }
                        if (!path.startsWith("/")) {
                            path = "/" + path;
                        }
                        p.put("type", p.getProperty("type", "DIR"));
                        if (p.containsKey("privs")) {
                            permissions.put(path.toUpperCase(), p.remove("privs"));
                        } else {
                            permissions.remove(path.toUpperCase());
                        }
                        permissions_xml = Common.getXMLString(permissions, "privs");
                        urlc.getOutputStream().write(("&permissions=" + GenericClient.u(permissions_xml)).getBytes("UTF8"));
                        vfs_items_xml = Common.getXMLString(vfs_items, "VFS");
                        urlc.getOutputStream().write(("&vfs_items=" + GenericClient.u(vfs_items_xml)).getBytes("UTF8"));
                        this.line(String.valueOf(command[1]) + " " + command[3] + " " + path + ":" + permissions.getProperty(path));
                    } else if (command[1].equalsIgnoreCase("group_add") || command[1].equalsIgnoreCase("group_delete") || command[1].equalsIgnoreCase("inheritance_add") || command[1].equalsIgnoreCase("inheritance_delete") || command[1].equalsIgnoreCase("group_delete_all") || command[1].equalsIgnoreCase("inheritance_delete_all")) {
                        data_action = "add";
                        xmlItem = "groups";
                        all = false;
                        if (command[1].equalsIgnoreCase("group_delete")) {
                            data_action = "delete";
                        } else if (command[1].equalsIgnoreCase("inheritance_add")) {
                            xmlItem = "inheritance";
                            data_action = "add";
                        } else if (command[1].equalsIgnoreCase("inheritance_delete")) {
                            xmlItem = "inheritance";
                            data_action = "delete";
                        } else if (command[1].equalsIgnoreCase("inheritance_delete_all")) {
                            xmlItem = "inheritance";
                            data_action = "delete";
                            all = true;
                        } else if (command[1].equalsIgnoreCase("group_delete_all")) {
                            xmlItem = "groups";
                            data_action = "delete";
                            all = true;
                        }
                        urlc.getOutputStream().write(("c2f=" + c2f + "&command=setUserItem&xmlItem=" + xmlItem + "&data_action=" + data_action + "&serverGroup=" + GenericClient.u(command[2])).getBytes("UTF8"));
                        object_name = command[4];
                        if (xmlItem.equals("groups")) {
                            urlc.getOutputStream().write(("&group_name=" + GenericClient.u(object_name)).getBytes("UTF8"));
                        } else {
                            urlc.getOutputStream().write(("&inheritance_name=" + GenericClient.u(object_name)).getBytes("UTF8"));
                        }
                        if (!all) {
                            urlc.getOutputStream().write(("&usernames=" + GenericClient.u(command[3])).getBytes("UTF8"));
                            this.line(String.valueOf(xmlItem) + " " + data_action + " " + command[3]);
                        } else {
                            urlc.getOutputStream().write("&usernames=".getBytes("UTF8"));
                            this.line(String.valueOf(xmlItem) + " " + data_action + " all");
                        }
                    }
                    code = 302;
                    result = "";
                    try {
                        code = urlc.getResponseCode();
                        result = Common.consumeResponse(urlc.getInputStream());
                    }
                    catch (Exception e) {
                        Common.log("HTTP_CLIENT", 1, e);
                    }
                    if (code != 302 && urlc.getURL().toString().indexOf("/WebInterface/login.html") >= 0) {
                        code = 302;
                    }
                    urlc.disconnect();
                    if (result.indexOf("<response>") >= 0) {
                        result = result.substring(result.indexOf("<response>") + "<response>".length(), result.lastIndexOf("</response>"));
                    }
                    this.line(result);
                    this.freeClient(c_dest);
                    Client.last_line_prompt = "message";
                    this.print_prompt();
                }
                if (command[0].equalsIgnoreCase("batch")) {
                    c_dest = this.getClient(false);
                    config = c_dest.config;
                    urlc = URLConnection.openConnection((VRL)credentials.get("vrl"), config);
                    urlc.setRequestMethod("POST");
                    urlc.setRequestProperty("Cookie", "CrushAuth=" + config.getProperty("crushAuth", "") + ";");
                    urlc.setUseCaches(false);
                    urlc.setDoOutput(true);
                    c2f = "";
                    if (!config.getProperty("crushAuth", "").equals("")) {
                        c2f = config.getProperty("crushAuth", "").substring(config.getProperty("crushAuth", "").length() - 4);
                    }
                    urlc.getOutputStream().write(("c2f=" + c2f + "&command=batchComplete").getBytes("UTF8"));
                    code = 302;
                    result = "";
                    try {
                        code = urlc.getResponseCode();
                        result = Common.consumeResponse(urlc.getInputStream());
                    }
                    catch (Exception e) {
                        Common.log("HTTP_CLIENT", 1, e);
                    }
                    if (code != 302 && urlc.getURL().toString().indexOf("/WebInterface/login.html") >= 0) {
                        code = 302;
                    }
                    urlc.disconnect();
                    if (result.indexOf("<response>") >= 0) {
                        result = result.substring(result.indexOf("<response>") + "<response>".length(), result.lastIndexOf("</response>"));
                    }
                    this.line(result);
                    this.freeClient(c_dest);
                }
                if (command[0].trim().equals("")) ** GOTO lbl2804
                var63_246 = this.line("Command not recognized or allowed.");
                return var63_246;
            }
            finally {
                this.freeClient(c);
            }
        }
        return null;
    }

    public Vector diff(final String path1, final String path2, final Properties stat1, final Properties stat2, final GenericClient c1, final GenericClient c2) throws Exception {
        final StringBuffer status1 = new StringBuffer();
        final StringBuffer status2 = new StringBuffer();
        final Vector chunksF1 = new Vector();
        final Vector chunksF2 = new Vector();
        if (stat1.getProperty("size", "0").equals("0") || stat2.getProperty("size", "0").equals("0")) {
            Vector<String> byteRanges = new Vector<String>();
            byteRanges.addElement("0--1");
            return byteRanges;
        }
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                try {
                    if (c1 instanceof HTTPClient) {
                        if (c1.stat(path1) != null && stat2 != null) {
                            Tunnel2.getRemoteMd5s(c1.url, path1, chunksF1, true, new StringBuffer().append(c1.getConfig("crushAuth")), status1, Long.parseLong(stat2.getProperty("size")));
                        }
                    } else if (c1.stat(path1) != null && stat2 != null) {
                        Tunnel2.getInputStreamMd5s(c1.download(path1, 0L, -1L, true), Long.parseLong(stat2.getProperty("size")), true, status1, chunksF1);
                    }
                }
                catch (Exception e) {
                    Client.this.printStackTrace(e, 1);
                }
                status1.append("done");
            }
        });
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                try {
                    if (c2 instanceof HTTPClient) {
                        if (c2.stat(path2) != null && stat1 != null) {
                            Tunnel2.getRemoteMd5s(c2.url, path2, chunksF2, true, new StringBuffer().append(c2.getConfig("crushAuth")), status2, Long.parseLong(stat1.getProperty("size")));
                        }
                    } else if (c2.stat(path2) != null && stat1 != null) {
                        Tunnel2.getInputStreamMd5s(c2.download(path2, 0L, -1L, true), Long.parseLong(stat1.getProperty("size")), true, status2, chunksF2);
                    }
                }
                catch (Exception e) {
                    Client.this.printStackTrace(e, 1);
                }
                status2.append("done");
            }
        });
        int pos = 0;
        while (true) {
            if (chunksF1.size() > pos && chunksF2.size() > pos) {
                Properties chunk1 = (Properties)chunksF1.elementAt(pos);
                Properties chunk2 = (Properties)chunksF2.elementAt(pos);
                if (chunk1.getProperty("md5").equals(chunk2.getProperty("md5")) && chunk1.getProperty("start").equals(chunk2.getProperty("start")) && chunk1.getProperty("size").equals(chunk2.getProperty("size"))) {
                    chunksF1.remove(pos);
                    chunksF2.remove(pos);
                    continue;
                }
                ++pos;
                continue;
            }
            if (status1.length() > 0 && status2.length() > 0) break;
            Thread.sleep(100L);
        }
        return Tunnel2.compareMd5s(chunksF1, chunksF2, false);
    }

    public void startupThreads() throws Exception {
        if (!this.transfer_threads_started) {
            this.transfer_threads_started = true;
            int x = 0;
            while (x < Integer.parseInt(this.prefs.getProperty("max_threads", "5"))) {
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        Client.this.run_transfers();
                    }
                });
                ++x;
            }
        }
    }

    public static String format_ls_la(Properties item) {
        StringBuffer item_str = new StringBuffer();
        item_str.append(item.getProperty("permissions"));
        item_str.append(String.valueOf(Common.lpad(item.getProperty("num_items", ""), 4)) + " ");
        item_str.append(String.valueOf(Common.rpad(item.getProperty("owner", ""), 8)) + " ");
        item_str.append(String.valueOf(Common.rpad(item.getProperty("group", ""), 8)) + " ");
        item_str.append(String.valueOf(Common.lpad(item.getProperty("size", ""), 13)) + " ");
        item_str.append(String.valueOf(Common.lpad(item.getProperty("month", "").trim(), 3)) + " ");
        item_str.append(String.valueOf(Common.lpad(item.getProperty("day", "").trim(), 2)) + " ");
        item_str.append(String.valueOf(Common.lpad(item.getProperty("time_or_year", ""), 5)) + " ");
        item_str.append(item.getProperty("name", ""));
        return item_str.toString();
    }

    public void setupSignalHandler() {
        SignalHandler sh = new SignalHandler(){

            @Override
            public void handle(Signal sig) {
                try {
                    Client.this.abort_wait = true;
                    Client.this.process_command("ABOR", true);
                }
                catch (Exception e) {
                    Client.this.printStackTrace(e, 1);
                }
            }
        };
        try {
            Signal.handle(new Signal("INT"), sh);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
    }

    public String stats_summary() {
        if (this.single_command_line_mode) {
            return "";
        }
        String skipped = "";
        if (!this.stats.getProperty("upload_skipped_bytes", "0").equals("0") || !this.stats.getProperty("upload_skipped_count", "0").equals("0")) {
            skipped = String.valueOf(skipped) + "Skipped Uploads:" + this.stats.getProperty("upload_skipped_count", "0") + " file(s), size=" + Common.format_bytes_short(Long.parseLong(this.stats.getProperty("upload_skipped_bytes", "0"))) + ".";
        }
        if (!this.stats.getProperty("download_skipped_bytes", "0").equals("0") || !this.stats.getProperty("download_skipped_count", "0").equals("0")) {
            skipped = String.valueOf(skipped) + "Skipped Downloads:" + this.stats.getProperty("download_skipped_count", "0") + " file(s), size=" + Common.format_bytes_short(Long.parseLong(this.stats.getProperty("download_skipped_bytes", "0"))) + ".";
        }
        return "Uploads:" + this.stats.getProperty("upload_count", "0") + " file(s), " + (int)Float.parseFloat(this.stats.getProperty("upload_folders", "0")) + " folder(s), size=" + Common.format_bytes_short(Long.parseLong(this.stats.getProperty("upload_bytes", "0"))) + ". Downloads:" + this.stats.getProperty("download_count", "0") + " file(s), " + this.stats.getProperty("download_folders", "0") + " folder(s), size=" + Common.format_bytes_short(Long.parseLong(this.stats.getProperty("download_bytes", "0"))) + ". " + skipped + " Queue:" + this.pending_transfer_queue.size();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String line(String s) {
        if (s.equals("")) {
            return "";
        }
        Object object = last_line_lock;
        synchronized (object) {
            last_line_prompt = "message";
            if (this.messages != null) {
                this.messages.addElement(String.valueOf(log_format.format(new Date())) + "|" + s);
                this.messages2.addElement(String.valueOf(log_format.format(new Date())) + "|" + s);
            }
            if (this.local_echo && (this.messages == null || this.dual_log)) {
                System.out.println(String.valueOf(this.log_sdf.format(new Date())) + "|" + s);
            } else if (this.messages == null || this.dual_log) {
                System.out.println(String.valueOf(this.log_sdf.format(new Date())) + "|" + s);
            }
        }
        return s;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void print_prompt() {
        if (this.only_log) {
            return;
        }
        Object object = last_line_lock;
        synchronized (object) {
            if (last_line_prompt.equals("stat")) {
                System.out.println("\r\n");
            }
            if (last_line_prompt.equals("message") || last_line_prompt.equals("stat")) {
                last_line_prompt = "prompt";
                if (!this.single_command_line_mode) {
                    System.out.print(String.valueOf(System.getProperty("crushclient.appname", "CrushClient")) + "> ");
                }
            }
        }
    }

    public Vector getStats() {
        Vector<String> v = new Vector<String>();
        try {
            int x = 0;
            while (x < this.source_used.size()) {
                GenericClient c_tmp = (GenericClient)this.source_used.elementAt(x);
                String transfer_path_src = c_tmp.getConfig("transfer_path_src", "");
                String transfer_path_dst = c_tmp.getConfig("transfer_path_dst", "");
                String direction = c_tmp.getConfig("transfer_direction", "");
                if (!transfer_path_src.equals("")) {
                    Vector<String> transfer_history = (Vector<String>)c_tmp.getConfig("transfer_history");
                    if (transfer_history == null) {
                        transfer_history = new Vector<String>();
                        c_tmp.setConfig("transfer_history", transfer_history);
                    }
                    long start = Long.parseLong(c_tmp.getConfig("transfer_start", "0").toString());
                    long transfer_bytes_total = Long.parseLong(c_tmp.getConfig("transfer_bytes_total", "0").toString());
                    long transfer_bytes = Long.parseLong(c_tmp.getConfig("transfer_bytes", "0").toString());
                    transfer_history.addElement(String.valueOf(System.currentTimeMillis()) + ";" + transfer_bytes + ";");
                    long transfer_history_start = Long.parseLong(transfer_history.elementAt(0).toString().split(";")[0]);
                    while (System.currentTimeMillis() - transfer_history_start > 10000L && transfer_history.size() > 1) {
                        transfer_history.removeElementAt(0);
                        transfer_history_start = Long.parseLong(transfer_history.elementAt(0).toString().split(";")[0]);
                    }
                    long transfer_history_bytes = Long.parseLong(transfer_history.elementAt(0).toString().split(";")[1]);
                    float speed = (float)(transfer_bytes - transfer_history_bytes) / ((float)(System.currentTimeMillis() - transfer_history_start) / 1000.0f);
                    v.addElement(c_tmp + ";" + transfer_path_src + ";" + transfer_path_dst + ";" + (System.currentTimeMillis() - start) / 1000L + ";" + transfer_bytes + ";" + transfer_bytes_total + ";" + direction + ";" + (long)speed);
                }
                ++x;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return v;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void printStats(boolean new_line) {
        try {
            int x = 0;
            while (x < this.source_used.size()) {
                GenericClient c_tmp = (GenericClient)this.source_used.elementAt(x);
                String transfer_path_src = c_tmp.getConfig("transfer_path_src", "");
                String transfer_path_dst = c_tmp.getConfig("transfer_path_dst", "");
                if (!transfer_path_src.equals("")) {
                    long start = Long.parseLong(c_tmp.getConfig("transfer_start").toString());
                    long transfer_bytes_total = Long.parseLong(c_tmp.getConfig("transfer_bytes_total").toString());
                    long transfer_bytes = Long.parseLong(c_tmp.getConfig("transfer_bytes").toString());
                    long transfer_bytes_last = Long.parseLong(c_tmp.getConfig("transfer_bytes_last").toString());
                    long transfer_bytes_last_interval = Long.parseLong(c_tmp.getConfig("transfer_bytes_last_interval").toString()) + 1L;
                    if (transfer_bytes_last != transfer_bytes) {
                        transfer_bytes_last_interval = 0L;
                    }
                    c_tmp.setConfig("transfer_bytes_last_interval", String.valueOf(transfer_bytes_last_interval + 1L));
                    c_tmp.setConfig("transfer_bytes_last", String.valueOf(transfer_bytes));
                    float speed = 10.0f * ((float)transfer_bytes / 1024.0f / ((float)(System.currentTimeMillis() - start) / 1000.0f));
                    speed = (float)((int)(speed * 100.0f)) / 100.0f;
                    String error_msg = "";
                    if (transfer_bytes_last_interval > 20L) {
                        speed = 0.0f;
                    }
                    if (transfer_bytes_last_interval > 40L) {
                        error_msg = " (Stalled) ";
                    }
                    if (transfer_bytes_last_interval > 60L) {
                        error_msg = " (Timing out...) ";
                    }
                    String speed_str = speed > 10240.0f ? String.valueOf((float)((int)(speed / 1024.0f)) / 10.0f) + "MB/sec" : String.valueOf((float)((double)((int)speed) / 10.0)) + "KB/sec";
                    Object object = last_line_lock;
                    synchronized (object) {
                        System.out.print("\r" + transfer_path_src + "->" + transfer_path_dst + error_msg + ":" + (System.currentTimeMillis() - start) / 1000L + " sec elapsed, " + Common.format_bytes_short(transfer_bytes) + " of " + Common.format_bytes_short(transfer_bytes_total) + " (" + (int)((float)transfer_bytes / (float)transfer_bytes_total * 100.0f) + "%) " + speed_str + "            " + (new_line ? "\n" : ""));
                        last_line_prompt = new_line ? "message" : "stats";
                    }
                }
                ++x;
            }
            if (this.source_used.size() == 0 && new_line && !this.single_command_line_mode) {
                System.out.println("Used:destination_used:" + this.destination_used.size() + ",source_used:" + this.source_used.size() + ",pending_transfer_queue:" + this.pending_transfer_queue.size() + ",retry_active:" + this.retry_active.size());
            }
            if (new_line && !this.single_command_line_mode) {
                System.out.println(this.stats_summary());
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void printDownloadsUploads() {
        try {
            Properties p;
            int x = 0;
            while (x < this.recent_transfers_download.size()) {
                p = (Properties)this.recent_transfers_download.elementAt(x);
                this.line("Download:" + new VRL(p.getProperty("url")).getPath());
                ++x;
            }
            x = 0;
            while (x < this.recent_transfers_upload.size()) {
                p = (Properties)this.recent_transfers_upload.elementAt(x);
                this.line("Upload:" + new VRL(p.getProperty("url")).getPath());
                ++x;
            }
        }
        catch (NullPointerException nullPointerException) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void add_transfer_log(String s) throws IOException {
        block8: {
            try {
                if (this.transfer_log == null) break block8;
                String date_str = "";
                Object object = log_format;
                synchronized (object) {
                    date_str = log_format.format(new Date());
                }
                object = this.transfer_log;
                synchronized (object) {
                    FileOutputStream l_out = new FileOutputStream(this.transfer_log, true);
                    l_out.write((String.valueOf(date_str) + "|" + s + "\r\n").getBytes());
                    l_out.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String printStackTrace(Exception e, int level, Vector messages2) {
        String s = "";
        if (System.getProperty("crushclient.debug", "1").equals("")) {
            System.getProperties().put("crushclient.debug", "1");
        }
        if (Integer.parseInt(System.getProperty("crushclient.debug", "1")) >= level) {
            s = String.valueOf(s) + Thread.currentThread().getName() + "\r\n";
            s = String.valueOf(s) + e.toString() + "\r\n";
            StackTraceElement[] ste = e.getStackTrace();
            int x = 0;
            while (x < ste.length) {
                s = String.valueOf(s) + ste[x].getClassName() + "." + ste[x].getMethodName() + ":" + ste[x].getLineNumber() + "\r\n";
                ++x;
            }
            SimpleDateFormat simpleDateFormat = log_format;
            synchronized (simpleDateFormat) {
                if (messages2 != null) {
                    messages2.addElement(String.valueOf(log_format.format(new Date())) + "|" + s);
                } else {
                    System.out.println(log_format.format(new Date()));
                    e.printStackTrace();
                }
            }
        }
        return s;
    }

    public String printStackTrace(Exception e, int level) {
        if (this.validate_mode) {
            ++this.additional_errors;
        }
        if (this.single_command_line_mode) {
            return "";
        }
        return Client.printStackTrace(e, level, this.messages2);
    }

    public int serviceMain(String[] args) throws ServiceException {
        System.setProperty("java.awt.headless", "true");
        final String[] args2 = args;
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    if (args2.length > 0) {
                        Tunnel2.main(args2);
                    } else {
                        AgentUI.main(args2);
                    }
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        while (!this.shutdown) {
            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
        System.exit(0);
        return 0;
    }

    public static void runLocalJob(String job_name, final Vector job_log) throws Exception {
        File job = new File("./jobs/" + job_name);
        Properties params = (Properties)Common.readXMLObject(String.valueOf(job.getPath()) + "/job.XML");
        params.put("debug", "true");
        Properties event = new Properties();
        event.put("event_plugin_list", "CrushTask");
        event.put("name", "ScheduledPluginEvent:" + params.getProperty("scheduleName"));
        params.put("new_job_id", Common.makeBoundary(20));
        event.putAll((Map<?, ?>)params);
        final Properties info = new Properties();
        info.put("action", "event");
        info.put("server_settings", new Properties());
        info.put("return_tracker", "true");
        info.put("event", event);
        info.put("items", new Vector());
        Start crush_task = new Start();
        crush_task.setSettings(params);
        job_log.addElement("Starting job " + job.getPath() + "...");
        final Properties status_obj = new Properties();
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                int loops = 0;
                while (!new File(info.getProperty("log_file", "")).exists() && loops < 20) {
                    try {
                        Thread.sleep(1000L);
                    }
                    catch (InterruptedException interruptedException) {
                        // empty catch block
                    }
                }
                job_log.addElement("Job log:" + info.getProperty("log_file", ""));
                long last_size = 0L;
                RandomAccessFile raf = null;
                byte[] b = new byte[1];
                while (!status_obj.containsKey("job_status") || new File(info.getProperty("log_file", "")).length() > last_size) {
                    block19: {
                        try {
                            try {
                                raf = new RandomAccessFile(new File(info.getProperty("log_file", "")), "r");
                                raf.seek(last_size);
                                last_size = raf.length();
                                String total_line = "";
                                int bytes_read = 0;
                                while (bytes_read >= 0) {
                                    bytes_read = raf.read(b);
                                    if (bytes_read > 0) {
                                        total_line = String.valueOf(total_line) + new String(b);
                                    }
                                    if (total_line.indexOf("\r\n") < 0) continue;
                                    job_log.addElement(total_line.trim());
                                    total_line = "";
                                }
                            }
                            catch (Exception e) {
                                job_log.addElement("" + e);
                                e.printStackTrace();
                                try {
                                    raf.close();
                                }
                                catch (Exception exception) {}
                                break block19;
                            }
                        }
                        catch (Throwable throwable) {
                            try {
                                raf.close();
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                            throw throwable;
                        }
                        try {
                            raf.close();
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    try {
                        Thread.sleep(1000L);
                    }
                    catch (InterruptedException interruptedException) {
                        // empty catch block
                    }
                }
                status_obj.put("log_status", "done");
            }
        });
        crush_task.run(info);
        status_obj.put("job_status", "done");
        int loops = 0;
        while (loops++ < 10 && status_obj.getProperty("log_status", "").equals("")) {
            Thread.sleep(1000L);
        }
    }

    public class DualReader {
        public ConsoleReader console = null;
        BufferedReader br2 = null;

        public DualReader(BufferedReader br2) {
            this.br2 = br2;
        }

        private void init() {
            if (Client.this.console_mode && this.console == null) {
                try {
                    this.console = new ConsoleReader();
                    this.console.setPrompt("");
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (this.br2 == null) {
                this.br2 = new BufferedReader(new InputStreamReader(System.in));
            }
        }

        public String readLine() throws IOException {
            this.init();
            if (Client.this.console_mode) {
                return this.console.readLine();
            }
            return this.br2.readLine();
        }

        public String readPassword() throws IOException {
            this.init();
            if (Client.this.console_mode) {
                return this.console.readLine(new Character('*'));
            }
            return this.br2.readLine();
        }

        public void close() throws IOException {
            if (this.br2 != null) {
                this.br2.close();
            }
            if (this.console != null) {
                this.console.close();
            }
        }
    }
}

